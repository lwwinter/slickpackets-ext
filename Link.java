//package org.timecrunch;

import java.util.ArrayList;

public abstract class Link implements ISchedulerSource {
	// may want to implement unique link id differently (or not at all) - may be useful though
	private static int gLinkId = 0;
	private int mLinkId;
	protected String mId;

	protected IQScheme mQueue;
	//private Host[] mHosts;
	private ArrayList<Host> mHosts;
	protected boolean mEnabled; // false = failed/disabled; true by default
	protected SimScheduler mSched;
	
	protected long weight;

	public Link(IQScheme qscheme, ArrayList<Host> hosts) {
		mQueue = qscheme;
		//mHosts = hosts.toArray(mHosts);
		mLinkId = gLinkId++;
		mId = "Link#" + mLinkId;
		mSched = null;
		if(hosts != null) {
			mHosts = new ArrayList<Host>(hosts);
		} else {
			mHosts = new ArrayList<Host>();
		}
		mEnabled = true;
	}

	// Corresponds to departure event
	public abstract void deliver(Packet p, Host dest);

	// Corresponds to arrival event
	public abstract void recvFrom(Packet p, Host src);

	public abstract long getDelayUntilFree(Host src);

	public boolean isEnabled() {
		return mEnabled;
	}

	// TODO: Consider dropping any packets on the link on failure
	public void toggleEnabled() {
		mEnabled =!mEnabled;
	}

	// TODO: Consider dropping any packets on the link on failure
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	public Host[] getHosts() {
		//return mHosts;
		Host[] temp = new Host[0];
		return mHosts.toArray(temp);
	}

	// returns true if enqueue succeeded, false otherwise (ie packet drop)
	protected boolean enqueueEvent(ISchedulable event) {
		return mQueue.enqueue(event);
	}

	protected ISchedulable dequeueEvent() {
		return mQueue.dequeue();
	}

	public int queueSize() {
		return mQueue.size();
	}

	public int getLinkId() {
		return mLinkId;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		if(id != null) {
			mId = id;
		}
	}

	// from Interface ISchedulerSource
	//public abstract void schedCallback(SchedulableType type);

	public void registerScheduler(SimScheduler sched) {
		mSched = sched;
	}
		

	public SimScheduler getScheduler() {
		return mSched;
	}

}
