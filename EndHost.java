//package org.timecrunch;

import java.util.ArrayList;

public class EndHost extends Host {

	// TODO: (semi-)scriptable endhost behavior?
	public EndHost(ArrayList<Link> links) {
		super(new InfFairQScheme(),links);
	}

	// Corresponds to departure event
	public void sendOn(Packet p, Link egress) {
		if(egress.isEnabled() == false) {
			linkNotEnabledHandler(p);
			return;
		}

		// If the link says it is free, the packet is passed to the Link
		long delay = egress.getDelayUntilFree(this);

		if(delay > 0) {
			// delay event until link will be free
			SimEvent e = new SimEvent(SchedulableType.DEPARTURE,this,
					mSched.getGlobalSimTime() + delay);
			mSched.addEvent(e);
			return;
		}

		// TODO: process delay statistics

		egress.recvFrom(p,this);
	}

	// Corresponds to arrival event
	public void recvOn(Packet p, Link ingress) {
		// TODO: attempt to receive packet and add to queue
	}

	@Override
	public Link forward(Packet p) {
		return super.forward(p); // TODO: extend forwarding behavior
	}

}
