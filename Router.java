package org.timecrunch;

import java.util.ArrayList;

public class Router extends Host {

	public Router(int qsize, ArrayList<Link> links) {
		super(new FifoQScheme(qsize),links);
	}

	public int queueCapacity() {
		return ((FifoQScheme)mQueue).capacity();
	}

	public double queueUtilization() {
		return mQueue.size()/((double)((FifoQScheme)mQueue).capacity());
	}

	// Corresponds to departure event
	public void sendOn(Packet p, Link egress) {
		// TODO: attempt to push packet onto Link
	}

	// Corresponds to arrival event
	public void recvOn(Packet p, Link ingress) {
		// TODO: attempt to receive packet and add to queue
	}

	public void schedCallback(ISchedulable event) {
		// TODO: perform action when queued events are selected in the scheduler
		// Probably just sendTo(p,nextRouter) or something similar here
	}

}
