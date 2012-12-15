//package org.timecrunch;

import java.util.ArrayList;

public class EndHost extends Host {

	public EndHost() {
		this(null);
	}

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

		/* EndHost has no processing delay, and we don't care about link delay before the packet
		 * initially leaves the EndHost. Timing begins when it first leaves (so that sending rates
		 * are limited by link capacity). */

		egress.recvFrom(p,this);

		// Queue next callback to the global scheduler (packet stays in local queue)
		// TODO: Consider adding NoRegisteredSchedulerException vs null-check
		if(queueSize() > 0 && mSched != null) {
			SimEvent e = new SimEvent(SchedulableType.DEPARTURE,this,mSched.getGlobalSimTime());
			mSched.addEvent(e);
		}
	}

	// Corresponds to arrival event
	public void recvOn(Packet p, Link ingress) {
		//attempt to receive packet and add to queue
		SimLogger.logTrace(p,this);
		if(ingress==null){	// trigger endhost to send a packet (queue for departure)
			// forwarding will be handled by schedCallback's DEPARTURE handler in Host
			boolean successfulQueue = enqueueEvent(p);
			if(successfulQueue) { // successfully added to queue - will always succeed for EndHost
				if(queueSize() == 1) {
					// no delay will be logged within EndHost
					SimEvent e = new SimEvent(SchedulableType.DEPARTURE,this,mSched.getGlobalSimTime());
					mSched.addEvent(e);
				}
			}
		}
		else{				// receive packet
			SimLogger.logEventArrival(p,this);
		}
	}

	@Override
	public Link forward(Packet p) {
		return super.forward(p); // TODO: extend forwarding behavior
	}

	//public void schedCallback(SchedulableType type); // handled in superclass (Host)

}
