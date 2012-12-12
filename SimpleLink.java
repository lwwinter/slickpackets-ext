//package org.timecrunch;

/* SimpleLink.java
 * Simple duplex link composed of two SimplexLinks
 */

import java.util.ArrayList;
import java.util.Collections;
import java.lang.Long;

public class SimpleLink extends Link {
	protected long mBandwidth;
	protected long mLatency;
	protected SimplexLink mAtoB;
	protected SimplexLink mBtoA;

	public SimpleLink(long bandwidth, long latency, ArrayList<Host> hosts) {
		super(null, hosts);
		assert (hosts.size() == 2) : new String("SimpleLink takes exactly 2 hosts");
		mBandwidth = bandwidth;
		mLatency = latency;
		mAtoB = new SimplexLink(bandwidth,latency,hosts);
		ArrayList<Host> rev = new ArrayList<Host>();
		rev.add(hosts.get(1));
		rev.add(hosts.get(0));
		mBtoA = new SimplexLink(bandwidth,latency,rev);
	}

	private SimplexLink getLinkBySource(Host src) {
		SimplexLink temp;
		int hid = src.getHostId();
		if(hid == mAtoB.getSrcHost().getHostId()) {
			temp = mAtoB;
		} else if(hid == mBtoA.getSrcHost().getHostId()) {
			temp = mBtoA;
		} else {
			temp = null;
		}

		return temp;
	}

	private SimplexLink getLinkByDest(Host dest) {
		SimplexLink temp;
		int hid = dest.getHostId();
		if(hid == mAtoB.getDstHost().getHostId()) {
			temp = mAtoB;
		} else if(hid == mBtoA.getDstHost().getHostId()) {
			temp = mBtoA;
		} else {
			temp = null;
		}

		return temp;
	}

	public long getDelayUntilFree(Host src) {
		SimplexLink link = getLinkBySource(src);
		if(link != null) {
			return link.getDelayUntilFree();
		}

		// TODO: Consider this an error - SHOULD block simulation event
		return Long.MAX_VALUE;
	}

	// Corresponds to departure event, null dest --> broadcast
	public void deliver(Packet p, Host dest) {
		// Should never be called - callbacks occur within SimplexLink
	/*
		SimplexLink link = getLinkByDest(dest);
		if(link != null) {
			link.deliver(p);
		} else {
			SimLogger.logDrop(p,this);
		}
	*/
	}

	// Corresponds to arrival event
	public void recvFrom(Packet p, Host src) {
		// TODO: Consider adding NoRegisteredSchedulerException vs null-check
		if(mSched == null) {
				SimLogger.logDrop(p,this);
				return;
		} else {
			SimLogger.logTrace(p,this);
		}

		SimplexLink link = getLinkBySource(src);
		link.recvFrom(p,src);
	}

	// from Interface ISchedulerSource
	public void schedCallback(SchedulableType type) {
		// Scheduler events should callback to the SimplexLinks, not this class!
		switch(type) {
			default: {
				SimLogger.logEventLoss(SchedulableType.INVALID,this);
				break;
			}
		}
	}

	@Override
	protected boolean enqueueEvent(ISchedulable event) {
		// Don't directly enqueue to this structure, but to its SimplexLinks
		return false;
	}

	@Override
	protected ISchedulable dequeueEvent() {
		// Don't directly dequeue from this structure
		return null;
	}

	@Override
	public int queueSize() {
		// TODO: this is a somewhat invalid measure
		return mAtoB.queueSize() + mBtoA.queueSize();
	}

	@Override
	public void registerScheduler(SimScheduler sched) {
		super.registerScheduler(sched);
		mAtoB.registerScheduler(sched);
		mBtoA.registerScheduler(sched);
	}
}
