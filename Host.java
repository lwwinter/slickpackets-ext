package org.timecrunch;

import java.util.ArrayList;

public abstract class Host implements ISchedulerSource {
	// may want to implement unique host id differently (or not at all) - may be useful though
	private static int gHostId = 0;
	private int mHostId;

	protected IQScheme mQueue;

	/* 'links' array will not change during runtime.
	 * If this behavior is desired, subclass Link and implement accordingly. */
	private Link[] mLinks;

	public Host(IQScheme qscheme, ArrayList<Link> links) {
		mQueue = qscheme;
		mLinks = links.toArray(mLinks);
		mHostId = gHostId++;
	}

	// Corresponds to departure event
	public abstract void sendOn(Packet p, Link egress);

	// Corresponds to arrival event
	public abstract void recvOn(Packet p, Link ingress);

	public Link[] getLinks() {
		return mLinks;
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

	public int getHostId() {
		return mHostId;
	}

	// from Interface ISchedulerSource
	//public abstract void schedCallback(ISchedulable event);

}
