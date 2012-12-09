//package org.timecrunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Long;
import java.lang.Integer;

public class SimpleLink extends Link {
	protected long mBandwidth;
	protected long mLatency;
	protected HashMap<Integer,Long> mLinkClearTimes;
	protected HashMap<Packet,Long> mCallbackTimes;

	public SimpleLink(long bandwidth, long latency, ArrayList<Host> hosts) {
		super(new InfFifoQScheme(), hosts);
		mBandwidth = bandwidth;
		mLatency = latency;
		mLinkClearTimes = new HashMap<Integer,Long>();
		mCallbackTimes = new HashMap<Packet,Long>();
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
				SimLogger.logDrop(p,this,mSched.getGlobalSimTime());
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
