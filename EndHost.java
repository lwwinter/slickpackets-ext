package org.timecrunch;

import java.util.ArrayList;

public class EndHost extends Host {

	// TODO: (semi-)scriptable endhost behavior?
	public EndHost(ArrayList<Link> links) {
		super(new InfFairQScheme(),links);
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
