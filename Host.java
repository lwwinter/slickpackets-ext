//package org.timecrunch;

import java.util.ArrayList;

public abstract class Host implements ISchedulerSource {
	// may want to implement unique host id differently (or not at all) - may be useful though
	private static int gHostId = 0;
	private int mHostId; // global unique id
	protected String mId; // string id that may be specified in config; not guaranteed to be unique

	protected IQScheme mQueue;

	/* 'links' array will not change during runtime.
	 * If this behavior is desired, subclass Link and implement accordingly. */
	protected ArrayList<Link> mLinks;
	protected SimScheduler mSched;

	public Host(IQScheme qscheme) {
		this(qscheme,null);
	}

	public Host(IQScheme qscheme, ArrayList<Link> links) {
		mQueue = qscheme;
		mHostId = gHostId++;
		mId = "Host#" + mHostId;
		mSched = null;
		if(links == null) {
			mLinks = new ArrayList<Link>(links);
		} else {
			mLinks = new ArrayList<Link>();
		}
	}

	// Corresponds to departure event
	public abstract void sendOn(Packet p, Link egress);

	// Corresponds to arrival event
	public abstract void recvOn(Packet p, Link ingress);

	// Takes a packet and returns the link to forward on
	public Link forward(Packet p) {
		// default fallback behavior - send on first link
		if(mLinks.size() == 0) {
			return null;
		}

		return mLinks.get(0);
	}

	public Link[] getLinks() {
		Link[] temp = new Link[0];
		return mLinks.toArray(temp);
	}

	public void addLink(Link c) {
		mLinks.add(c);
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

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		if(id != null) {
			mId = id;
		}
	}

	public void linkNotEnabledHandler(Packet p) {
		// Default behavior: drop packet; can @Override in subclasses
		SimLogger.logDrop(p,this);
	}

	// from Interface ISchedulerSource
	public void schedCallback(SchedulableType type) {
		// Perform action when queued event is selected in the scheduler
		switch(type) {
			case DEPARTURE: {
				Packet p = (Packet)dequeueEvent();
				Link link = forward(p);
				if(link == null) {
					SimLogger.logEventLoss(type,this);
					return;
				}

				sendOn(p,link);
				break;
			}

			default: {
				SimLogger.logEventLoss(SchedulableType.INVALID,this);
				break;
			}
		}
	}

	public void registerScheduler(SimScheduler sched) {
		mSched = sched;
	}
		

	public SimScheduler getScheduler() {
		return mSched;
	}

}
