//package org.timecrunch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

// Congestion-Aware End-Host
public class CAEndHost extends Host {
	// caches routes for Source-Routing to minimize computation time
	protected HashMap<Host,RouteCacheEntry> mRouteCache;
	protected HashMap<Host,CongestionState> mNeighborStates;
	protected HashMap<Host,ProbeLogEntry> mProbeLog;

	public CAEndHost() {
		this(null);
	}

	public CAEndHost(ArrayList<Link> links) {
		super(new InfFairQScheme(),links);
		mRouteCache = new HashMap<Host,RouteCacheEntry>();

		// initialize neighbors' congestion states
		mNeighborStates = new HashMap<Host,CongestionState>();
		if(links != null) {
			Host[] temp;
			for(Link l : links) {
				temp = l.getHosts();
				for(Host h : temp) {
					if(h.getHostId() != getHostId()) {
						mNeighborStates.put(h,CongestionState.NORMAL_LOAD);
					}
				}
			}
		}

		mProbeLog = new HashMap<Host,ProbeLogEntry>();
	}

	// Corresponds to departure event
	public void sendOn(Packet p, Link egress) {
		// TODO: Expects links to only have 2 hosts, otherwise behavior is erratic
		Host[] dests = egress.getHosts();
		Host nextHop = null;
		for(Host h : dests) {
			if(h.getHostId() != getHostId()) {
				nextHop = h;
				break;
			}
		}

		CongestionState state;
		if(nextHop != null) {
			state = mNeighborStates.get(nextHop);
		} else { // assume normal load if no data
			SimLogger.logError("nextHop not found on Link: "+egress.getId()+", SrcHost: "+getId());
			state = CongestionState.NORMAL_LOAD;
		}

		// attempt to push packet onto Link
		if(egress.isEnabled() == false ||
				(state == CongestionState.OVERLOAD && p.getType() == PacketType.SLICK_PACKET)) {
			linkNotEnabledHandler(p);
			return;
			//TODO: read backup path from slick packet (within linkNotEnabledHandler?)
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

		if(state == CongestionState.HIGH_LOAD && p.getType() == PacketType.SLICK_PACKET_EXT) {
			// spawn Probe packet along alternate path
			/* FIXME: for now, we assume that we can send on both outgoing links at once, despite
			 * our explicit lack of this feature with respect to other queued packets.
			 * We should really model this as the next packet queued after the SlickPacket! */
			SlickPacketExt spe = (SlickPacketExt)p;
			ProbePacket pp = spe.spawnProbe();
			if(pp != null) {
				Link altLink = forward(pp);

				// attempt to push packet onto Link
				// FIXME: Should PROBABLY check if link is free, but for 2-host links, doesn't matter
				if(altLink.isEnabled() == false) {
					// TODO: Might be nice to avoid this if Host through altLink is in OVERLOAD too
					linkNotEnabledHandler(pp);
				} else {
					altLink.recvFrom(pp,this);
				}
			}

			// Note: NOW we'll actually send the SlickPacketExt
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
		} else { // receive packet
			switch(p.getType()) {
				case PROBE_PACKET: {
					ProbePacket pp = (ProbePacket)p;
					Host src = pp.getSrc();
					if(src != null) {
						ProbeLogEntry ple = mProbeLog.get(src);
						if(ple == null) {
							ple = new ProbeLogEntry();
							mProbeLog.put(src,ple);
						}

						boolean doPack = ple.logProbeArrival(pp.getProbeSeqNum());
						if(doPack) {
							ProbeAck pa = new ProbeAck(pp);
							this.recvOn(pa,null); // add Probe-ACK to queue and send
						}
					}
					//System.out.println("Probe#"+pp.getProbeSeqNum()+" arrived at "+getId()); // DEBUG
					break;
				}

				case PROBE_ACK: {
					ProbeAck pa = (ProbeAck)p;
					Host src = pa.getSrc();
					NetworkGraph ng = mSched.getNetworkGraph();
					if(ng != null && src != null) {
						RouteCacheEntry rce = mRouteCache.get(src);
						if(rce != null) {
							rce.setPath(pa.getReversePath());
							rce.failovers = ng.computeFailovers(rce.path,this,src);
							//NetworkGraph.printPath(rce.path); // DEBUG
						} else {
							SimLogger.logError("Received ProbeAck from "+src.getId()+" which has no RouteCacheEntry");
						}
					}
					//System.out.println("ProbeAck from "+src.getId()+" arrived at "+getId()); // DEBUG
					break;
				}

				case SLICK_PACKET_EXT: {
					SlickPacketExt spe = (SlickPacketExt)p;
					Host src = spe.getSrc();
					if(src != null) {
						ProbeLogEntry ple = mProbeLog.get(src);
						if(ple == null) {
							ple = new ProbeLogEntry();
							mProbeLog.put(src,ple);
						}

						ple.logPacketArrival(spe.getProbeSeqNum());
					}

					SimLogger.logEventArrival(p,this);
					break;
				}

				case CONGESTION_STATE_UPDATE: {
					/* FIXME: GiveStateUpdatesPriority always true at the moment.
					 * If it's not, we may have to deal with dropping congestion state updates
					 * due to a full queue, and additional design will likely be needed.
					 */
					CongestionStateUpdate csu = (CongestionStateUpdate)p;
					mNeighborStates.put(csu.getSender(),csu.getState());
					//System.out.println("CAHost: "+getId()+" received CongestionStateUpdate: "+csu.getState()+" from "+csu.getSender().getId()); // DEBUG
					break;
				}

				default:
					SimLogger.logEventArrival(p,this);
					break;
			}
		}
	}

	@Override
	public void addLink(Link c) {
		super.addLink(c);
		Host[] hosts = c.getHosts();
		for(Host h : hosts) {
			if(h.getHostId() != getHostId()) {
				mNeighborStates.put(h,CongestionState.NORMAL_LOAD);
			}
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
				link = slickPacketExtHandler((SlickPacketExt)p);
				break;
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

	private Link slickPacketExtHandler(SlickPacketExt spe) {
		Link link = slickPacketHandler((ISlickPackets)spe);
		Host dest = spe.getDest();
		if(dest != null) {
			RouteCacheEntry rce = mRouteCache.get(dest);
			// only allow 1 SlickPacketExt to spawn probes per RTT
			if(rce.lastProbeAllowedTime == -1 ||
					(rce.lastProbeAllowedTime + rce.rtt >= mSched.getGlobalSimTime())) {
				spe.setProbeSeqNum(rce.probeSeqNum++);
				if(rce.probeSeqNum == 0) {
					rce.probeSeqNum = 1;
				}
			}
		}

		return link;
	}

	//public void schedCallback(SchedulableType type); // handled in superclass (Host)

}
