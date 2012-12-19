//package org.timecrunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Long;

public class SimplexLink extends Link {
	protected long mBandwidth;
	protected long mLatency;
	protected Host mSrcHost;
	protected Host mDstHost;
	protected long mLinkClearTime;
	protected HashMap<Packet,Long> mCallbackTimes;

	public SimplexLink(long bandwidth, long latency, ArrayList<Host> hosts) {
		super(new InfFifoQScheme(),hosts);
		assert (hosts.size() == 2) : new String("SimplexLink takes exactly 2 hosts in order: (src,dest)");
		mBandwidth = bandwidth;
		mLatency = latency;
		mSrcHost = hosts.get(0);
		mDstHost = hosts.get(1);
		mLinkClearTime = 0;
		mCallbackTimes = new HashMap<Packet,Long>();
		weight = mLatency ;
	}

	public Host getSrcHost() {
		return mSrcHost;
	}

	public Host getDstHost() {
		return mDstHost;
	}

	@Override
	public long getLatency() {
		return mLatency;
	}

	public long getDelayUntilFree(Host src) {
		// 'src' may be anything (including null) for SimplexLink's call
		return getDelayUntilFree();
	}

	public long getDelayUntilFree() {
		// TODO: Consider adding NoRegisteredSchedulerException vs null-check
		if(mSched == null) {
			return 0;
		}

		long delay = mLinkClearTime - mSched.getGlobalSimTime();
		if(delay < 0) {
			return 0;
		}

		return delay;
	}

	public void deliver(Packet p, Host dest) {
		// 'dest' may be anything (including null) for SimplexLink's call
		deliver(p);
	}

	// Corresponds to departure event, null dest --> broadcast
	public void deliver(Packet p) {
		// TODO: Consider moving delays here (at least propagation)
		mDstHost.recvOn(p,this);

		// Queue next callback to the global scheduler (packet stays in local queue)
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
		if(successfulQueue) { // always succeeds for SimplexLink; included for easy extension
			// TODO: Don't like floor (vs rounded) division; should be fine at microsecond resolution
			long transmitDelay = (1000000*(long)p.size())/mBandwidth;
			if(GlobalSimSettings.LogDelays) {
				PacketDelays pd = p.getDelays();
				pd.logTransmissionDelay(transmitDelay);
				pd.logPropagationDelay(mLatency);
			}

			long tempTimestamp = mSched.getGlobalSimTime() + transmitDelay;
			mLinkClearTime = tempTimestamp;
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
