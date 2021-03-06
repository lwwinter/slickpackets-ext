//package org.timecrunch;

import java.util.ArrayList;
import java.util.HashMap;

// Congestion-Aware Router
public class CARouter extends Host {
	// CONSTANTS
	// circumvent event/packet queue - update state knowledge immediately on reception
	public static final boolean DEFAULT_GIVE_STATE_UPDATES_PRIORITY = true;
	// currently, states must drop 10% below their threshold (100% or 80%) to downgrade state
	// TODO: work with this parameter to minimize state updates due to oscillation
	public static final double DOWNGRADE_THRESHOLD_PADDING = 0.1;
	public static final double HIGH_LOAD_THRESHOLD = 0.8;
	// OVERLOAD occurs after any packet is dropped due to a full queue (100%)

	// GLOBALS
	// TODO: handle case where this is false (assume always true for now; not xml-configurable)
	public static boolean GiveStateUpdatesPriority = DEFAULT_GIVE_STATE_UPDATES_PRIORITY;

	// MEMBERS
	protected long mThroughput; // router throughput in bits/s
	protected HashMap<Packet,Long> mArrivalTimes;
	private long mBitUsProcessed; // used to model quantized delay even for any throughput
	protected CongestionState mCongestionState;
	protected long mLastStateUpdateTime;
	protected HashMap<Host,CongestionState> mNeighborStates;
	
	public CARouter(long throughput, int qsize) {
		this(throughput,qsize,null);
	}

	public CARouter(long throughput, int qsize, ArrayList<Link> links) {
		super(new FifoQScheme(qsize),links);
		mThroughput = throughput;
		mArrivalTimes = new HashMap<Packet,Long>();
		mBitUsProcessed = 0;
		mCongestionState = CongestionState.NORMAL_LOAD;
		mLastStateUpdateTime = -1;

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
	}

	public int queueCapacity() {
		return ((FifoQScheme)mQueue).capacity();
	}

	public double queueUtilization() {
		return mQueue.size()/((double)((FifoQScheme)mQueue).capacity());
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

		// Calculate and log queueing delay
		if(GlobalSimSettings.LogDelays) {
			long arrivalTime = mArrivalTimes.get(p);
			long qDelay = mSched.getGlobalSimTime()-arrivalTime;
			PacketDelays pd = p.getDelays();
			pd.logQueueingDelay(qDelay);
		}

		// If the link says it is free, the packet is passed to the Link
		long delay = egress.getDelayUntilFree(this);

		if(delay > 0) {
			// delay event until link will be free
			long tempTimestamp = mSched.getGlobalSimTime() + delay;
			SimEvent e = new SimEvent(SchedulableType.DEPARTURE,this,tempTimestamp);
			mSched.addEvent(e);
			mDelayedEvent = p;

			// process link-delay statistics
			if(GlobalSimSettings.LogDelays) {
				PacketDelays pd = p.getDelays();
				// set timestamp as new 'arrival' time to avoid duplicate queue-delay logging
				mArrivalTimes.put(p,tempTimestamp);
				pd.logLinkBusyDelay(delay);
			}
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
		
		egress.recvFrom(p,this);

		// Queue next callback to the global scheduler (packet stays in local queue)
		// TODO: Consider adding NoRegisteredSchedulerException vs null-check
		if(queueSize() > 0 && mSched != null) {
			Packet pNext = (Packet)mQueue.peek();
			SimEvent e = new SimEvent(SchedulableType.DEPARTURE,this,mSched.getGlobalSimTime() +
					getProcessingDelay(pNext));
			mSched.addEvent(e);
		}
	}

	// Corresponds to arrival event
	public void recvOn(Packet p, Link ingress) {
		// attempt to receive packet and add to queue
		if(mSched == null) {
			SimLogger.logDrop(p,this);
			return;
		} else {
			SimLogger.logTrace(p,this);
		}

		/* FIXME: GiveStateUpdatesPriority always true at the moment.
		 * If it's not, we may have to deal with dropping congestion state updates
		 * due to a full queue, and additional design will likely be needed.
		 */
		if(GiveStateUpdatesPriority && p.getType() == PacketType.CONGESTION_STATE_UPDATE) {
			CongestionStateUpdate csu = (CongestionStateUpdate)p;
			mNeighborStates.put(csu.getSender(),csu.getState());
			//System.out.println("CARouter: "+getId()+" received CongestionStateUpdate: "+csu.getState()+" from "+csu.getSender().getId()); // DEBUG
			return;
		}

		boolean successfulQueue = enqueueEvent(p);
		if(successfulQueue) { // successfully added to queue
			long now = mSched.getGlobalSimTime();
			mArrivalTimes.put(p,now);
			if(queueSize() == 1) {
				// total delay will be logged as difference in timestamps in sendOn()
				SimEvent e = new SimEvent(SchedulableType.DEPARTURE,this,now+getProcessingDelay(p));
				mSched.addEvent(e);
			}

			// compute queue utilization and update router state if necessary
			// used to be else-state for queueSize() == 1, but then can't test with queue-size 1
			double qutil = queueUtilization();
			//System.out.println(getId()+": qutil="+qutil+", cs="+mCongestionState); // DEBUG
			switch(mCongestionState) {
				case NORMAL_LOAD: {
					if(qutil > HIGH_LOAD_THRESHOLD) {
						updateCongestionState(CongestionState.HIGH_LOAD);
					}
					break;
				}
				case HIGH_LOAD: {
					if(qutil < HIGH_LOAD_THRESHOLD - DOWNGRADE_THRESHOLD_PADDING) {
						updateCongestionState(CongestionState.NORMAL_LOAD);
					}
					break;
				}
				case OVERLOAD: {
					if(qutil < HIGH_LOAD_THRESHOLD - DOWNGRADE_THRESHOLD_PADDING) {
						updateCongestionState(CongestionState.NORMAL_LOAD);
					} else if(qutil < 1.0 - DOWNGRADE_THRESHOLD_PADDING) {
						updateCongestionState(CongestionState.HIGH_LOAD);
					}
					break;
				}
				default:
					SimLogger.logError("Received invalid CongestionState");
					break;
			}

		} else { // queue is full, drop packet
			if(mCongestionState != CongestionState.OVERLOAD) {
				updateCongestionState(CongestionState.OVERLOAD);
			}

			SimLogger.logDrop(p,this);
		}
	}

	protected void updateCongestionState(CongestionState cs) {
		//System.out.println("CARouter: "+getId()+" updates CongestionState to "+cs); // DEBUG
		mCongestionState = cs;
		if(GiveStateUpdatesPriority) {
			// send congestions state update packets on all links
			/* FIXME: A link could be blocked - we SHOULD model this, but for now, we
			 * just preempt whatever's being sent and broadcast on all links immediately.
			 * We should also model link transmission delay, but it's not happening now.
			 * At present, our broadcast packets immediately make it onto the link.
			 * So only link latency is really modeled here.*/
			Link[] myLinks = getLinks();
			CongestionStateUpdate csu;
			// TODO: may be valid to send same csu packet instance to all neighbors
			for(Link l : myLinks) {
				csu = new CongestionStateUpdate(this,mCongestionState);
				l.recvFrom(csu,this);
			}
		} else {
		// TODO: Always true for now due to coding time constraints
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
	public void schedCallback(SchedulableType type) {
		// perform action when queued events are selected in the scheduler
		super.schedCallback(type); // TODO: extend callback behavior?
	}

	@Override
	public Link forward(Packet p) {
		Link link = null ;

		// TODO: May be able to clean this up even further with proper ordering and fallthroughs
		// TODO: Safer to ignore unknown packets; explicitly handle NO_TYPE with super.forward(p)
		switch(p.getType()) {
			case CONGESTION_STATE_UPDATE:
				// ignore packet - should already be processed
				break;

			case SLICK_PACKET_EXT:
				// intentional fallthrough
			case SLICK_PACKET: {
				ISlickPackets sp = (ISlickPackets)p;
				link = sp.getNextLink();
				break;
			}

			case PROBE_PACKET:
				// intentional fallthrough
			case PROBE_ACK:
				// intentional fallthrough
			case SOURCE_ROUTED: {
				ISourceRoutable sp = (ISourceRoutable)p;
				link = sp.getNextLink() ;
				break;
			}

			default:
				link = super.forward(p);
				break;
		}

		return link;
	}

	private long getProcessingDelay(Packet p) {
		mBitUsProcessed += p.size()*1000000;
		long processingDelay = mBitUsProcessed/mThroughput;
		mBitUsProcessed %= mThroughput;
		return processingDelay;
	}

}
