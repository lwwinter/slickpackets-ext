//package org.timecrunch;

import java.util.ArrayList;
import java.util.HashMap;

public class Router extends Host {
	protected long mThroughput; // router throughput in bits/s
	protected HashMap<Packet,Long> mArrivalTimes;
	private long mBitUsProcessed; // used to model quantized delay even for any throughput
	
	public Router(long throughput, int qsize) {
		this(throughput,qsize,null);
	}

	public Router(long throughput, int qsize, ArrayList<Link> links) {
		super(new FifoQScheme(qsize),links);
		mThroughput = throughput;
		mArrivalTimes = new HashMap<Packet,Long>();
		mBitUsProcessed = 0;
	}

	public int queueCapacity() {
		return ((FifoQScheme)mQueue).capacity();
	}

	public double queueUtilization() {
		return mQueue.size()/((double)((FifoQScheme)mQueue).capacity());
	}

	// Corresponds to departure event
	public void sendOn(Packet p, Link egress) {
		// attempt to push packet onto Link
		if(egress.isEnabled() == false) {
			linkNotEnabledHandler(p);
			return;
			//TODO: read backup path from slick packet (within linkNotEnabledHandler?)
		}

		//TODO: read link utilization, implement slick packet ext. (within forward?) 
		// If it's high loaded, sent probe packet; over loaded, using backup path

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

		boolean successfulQueue = enqueueEvent(p);
		if(successfulQueue) { // successfully added to queue
			long now = mSched.getGlobalSimTime();
			mArrivalTimes.put(p,now);
			if(queueSize() == 1) {
				// total delay will be logged as difference in timestamps in sendOn()
				SimEvent e = new SimEvent(SchedulableType.DEPARTURE,this,now+getProcessingDelay(p));
				mSched.addEvent(e);
			}
		} else { // queue is full, drop packet
			SimLogger.logDrop(p,this);
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
