package org.timecrunch;

import java.util.ArrayList;

public abstract class Link implements ISchedulerSource {
	// may want to implement unique link id differently (or not at all) - may be useful though
	private static int gLinkId = 0;
	private int mLinkId;

	protected IQScheme mQueue;
	private Host[] mHosts;
	protected boolean mEnabled; // false = failed/disabled

	public Link(IQScheme qscheme, ArrayList<Host> hosts) {
		mQueue = qscheme;
		mHosts = hosts.toArray(mHosts);
		mLinkId = gLinkId++;
	}

	// Corresponds to departure event
	public abstract void sendTo(Packet p, Host dest);

	// Corresponds to arrival event
	public abstract void recvFrom(Packet p, Host src);

	public abstract boolean isLinkFree();

	public boolean isEnabled() {
		return mEnabled;
	}

	public void toggleEnabled() {
		mEnabled =!mEnabled;
	}

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	public Host[] getHosts() {
		return mHosts;
	}

	// returns true if enqueue succeeded, false otherwise (ie packet drop)
	public boolean enqueueEvent(ISchedulable event) {
		return mQueue.enqueue(event);
	}

	public ISchedulable dequeueEvent() {
		return mQueue.dequeue();
	}

	public int queueSize() {
		return mQueue.size();
	}

	// from Interface ISchedulerSource
	//public abstract void schedCallback(ISchedulable event);

}
