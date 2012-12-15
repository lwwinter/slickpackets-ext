/* - This class served as the original SimpleLink, but suffered some problems.
 * - It only has 1 queue but supports multi-directional transmission. This means
 *   that a large packet can get queued to the global queue and get processed before
 *   a small packet being transmitted in the other direction, violating causality.
 * - Even broadcast is not correct, however; this link will deliver broadcasted packets
 *   to EVERY host, including the source that sent it.
 * - SimplexLink extends link and implements much of this functionality but is only
 *   unidirectional, which is valid for the single queue.
 * - The new SimpleLink class is two SimplexLinks. It still directly extends Link.
 * - This class could serve as the basis for EthernetLink or WirelessLink due to broadcast.
*/

//package org.timecrunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Long;
import java.lang.Integer;

public class UniversalLink extends Link {
	protected long mBandwidth;
	protected long mLatency;
	protected HashMap<Integer,Long> mLinkClearTimes;
	protected HashMap<Packet,Long> mCallbackTimes;

	public UniversalLink(long bandwidth, long latency, ArrayList<Host> hosts) {
		super(new InfFifoQScheme(), hosts);
		mBandwidth = bandwidth;
		mLatency = latency;
		mLinkClearTimes = new HashMap<Integer,Long>();
		mCallbackTimes = new HashMap<Packet,Long>();
		weight = mLatency ;
	}

	public long getDelayUntilFree(Host src) {
		Long clearTime = mLinkClearTimes.get(new Integer(src.getId()));
		// TODO: Consider adding NoRegisteredSchedulerException vs null-check
		if(clearTime == null || mSched == null) {
			return 0;
		}

		long delay = clearTime.longValue() - mSched.getGlobalSimTime();
		if(delay < 0) {
			return 0;
		}

		return delay;
	}

	// Corresponds to departure event, null dest --> broadcast
	public void deliver(Packet p, Host dest) {
		// TODO: Consider moving delays here (at least propagation)
		if(dest == null) {
			for(Host h : getHosts()) {
				// FIXME: sender technically delivers to itself too
				h.recvOn(p,this);
			}
		} else {
			dest.recvOn(p,this);
		}

		// TODO: Consider adding NoRegisteredSchedulerException vs null-check
		if(queueSize() > 0 && mSched != null) {
			Long callbackTime = mCallbackTimes.remove(p);
			if(callbackTime != null) {
				SimEvent e = new SimEvent(SchedulableType.DELIVERY,this,callbackTime.longValue());
				mSched.addEvent(e);
			}
		}
	}

	// Corresponds to arrival event
	public void recvFrom(Packet p, Host src) {
		// TODO: Consider adding NoRegisteredSchedulerException vs null-check
		if(mSched == null) {
				SimLogger.logDrop(p,this);
				return;
		}

		boolean successfulQueue = enqueueEvent(p);
		if(successfulQueue) { // always succeeds for SimpleLink; included for easy extension
			// TODO: Don't like floor (vs rounded) division; should be fine at microsecond resolution
			long transmitDelay = (p.size()*1000000)/mBandwidth;
			if(GlobalSimSettings.LogDelays) {
				PacketDelays pd = p.getDelays();
				pd.logTransmissionDelay(transmitDelay);
				pd.logPropagationDelay(mLatency);
			}

			long tempTimestamp = mSched.getGlobalSimTime() + transmitDelay;
			mLinkClearTimes.put(new Integer(src.getId()),new Long(tempTimestamp));
			tempTimestamp += mLatency;
			if(queueSize() == 1) {
				SimEvent e = new SimEvent(SchedulableType.DELIVERY,this,tempTimestamp);
				mSched.addEvent(e);
			} else {
				mCallbackTimes.put(p,new Long(tempTimestamp));
			}
		}
	}

	// from Interface ISchedulerSource
	public void schedCallback(SchedulableType type) {
		// Perform action when queued event is selected in the scheduler
		switch(type) {
			case DELIVERY: {
				Packet p = (Packet)dequeueEvent();
				deliver((Packet)p,null); // SimpleLink just broadcasts
				break;
			}

			default: {
				SimLogger.logEventLoss(SchedulableType.INVALID,this);
				break;
			}
		}
	}

}
