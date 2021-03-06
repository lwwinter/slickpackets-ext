//package org.timecrunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class EndHost extends Host {
	// caches routes for Source-Routing to minimize computation time
	protected HashMap<Host,RouteCacheEntry> mRouteCache;

	public EndHost() {
		this(null);
	}

	public EndHost(ArrayList<Link> links) {
		super(new InfFairQScheme(),links);
		mRouteCache = new HashMap<Host,RouteCacheEntry>();
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
			mDelayedEvent = p; // save packet for next callback (since it was dequeued)
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
		Link link = null ;

		switch(p.getType()) {
			case CONGESTION_STATE_UPDATE:
				// ignore packet - should already be processed
				break;

			case SLICK_PACKET_EXT:
				// intentional fallthrough
			case SLICK_PACKET:
				link = slickPacketHandler((ISlickPackets)p);
				break;

			case PROBE_PACKET:
				// intentional fallthrough
			case PROBE_ACK:
				//intentional fallthrough
			case SOURCE_ROUTED:
				link = sourceRoutedPacketHandler((ISourceRoutable)p);
				break;

			default:
				link = super.forward(p);
				break;
		}

		return link;
	}

	private Link sourceRoutedPacketHandler(ISourceRoutable sr) {
		if(sr.getPath() == null) { // only set path if not already set
			LinkedList<Link> path;
			NetworkGraph ng = mSched.getNetworkGraph();
			Host dest = sr.getDest();
			if(ng != null && dest != null) {
				RouteCacheEntry rce = mRouteCache.get(dest);
				if(rce == null || rce.stale || rce.path == null) {
					path = ng.getPath(this,dest);
					rce = new RouteCacheEntry(path);
					mRouteCache.put(dest,rce);
				} else {
					path = rce.path;
				}

				// copy path and set in header
				// TODO: should be safe (and faster) to not copy - check
				sr.setNewPath(new LinkedList<Link>(path));
			}
		}

		// else no destination or no access to NetworkGraph - returns null
		return sr.getNextLink();
	}

	private Link slickPacketHandler(ISlickPackets sp) {
		LinkedList<Link> path;
		ArrayList<LinkedList<Link>> failovers;
		NetworkGraph ng = mSched.getNetworkGraph();
		Host dest = sp.getDest();
		if(ng != null && dest != null) {
			RouteCacheEntry rce = mRouteCache.get(dest);
			if(rce == null || rce.stale || rce.path == null || rce.failovers == null) {
				path = ng.getPath(this,dest);
				failovers = ng.computeFailovers(path,this,dest);
				rce = new RouteCacheEntry(path,failovers);
				mRouteCache.put(dest,rce);
			} else {
				path = rce.path;
				failovers = rce.failovers;
			}

			// copy path and set in header
			// TODO: should be safe (and faster) to not copy - check
			sp.setNewPath(new LinkedList<Link>(path),failovers);
		}

		// else no destination or no access to NetworkGraph - returns null
		return sp.getNextLink();
	}

	//public void schedCallback(SchedulableType type); // handled in superclass (Host)

}
