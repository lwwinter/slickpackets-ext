//package org.timecrunch;

import java.util.ArrayList;

public class Router extends Host {
	
	protected long mPacketProcessTime;  // per packet process time, assume it's constant.
	protected long mMaxQueingTime;      // queueing delay when queue was full
	protected HashMap<Packet,Long> mCallbackTimes;
	
	public Router(long MaxQueingTime, long packetProcessTime , int qsize, ArrayList<Link> links) {
		super(new FifoQScheme(qsize),links);
		mMaxQueingTime = MaxQueingTime ;
		mPacketProcessTime = packetProcessTime ;
		mCallbackTimes = new HashMap<Packet,Long>();
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
			//TODO: read backup path from slick packet
			
		}

		//TODO: read link utilization, implement slick packet ext. 
		//if it's high loaded, sent probe packet; over loaded, using backup path
		
		
		// If the link says it is free, the packet is passed to the Link
		long delay = egress.getDelayUntilFree(this);

		if(delay > 0) {
			// delay event until link will be free
			SimEvent e = new SimEvent(SchedulableType.DEPARTURE,this,
					mSched.getGlobalSimTime() + delay);
			mSched.addEvent(e);
			return;
		}

		// process delay statistics
		if(GlobalSimSettings.LogDelays) {
			PacketDelays pd = p.getDelays();
			pd.logLinkBusyDelay(delay);
		}
		
		egress.recvFrom(p,this);
	}

	// Corresponds to arrival event
	public void recvOn(Packet p, Link ingress) {
		// attempt to receive packet and add to queue
		if(mSched == null) {
			SimLogger.logDrop(p,this,mSched.getGlobalSimTime());
			return;
		}

		boolean successfulQueue = enqueueEvent(p);
		if(successfulQueue) { // successful added to queue
			//TODO: consider non linear queueing delay?
			long queueingDelay = queueUtilization()*mMaxQueingTime;
			if(GlobalSimSettings.LogDelays) {
				PacketDelays pd = p.getDelays();
				pd.logQueueingDelay(queueingDelay);
			}

			long tempTimestamp = mSched.getGlobalSimTime() + queueingDelay;
			tempTimestamp += mPacketProcessTime;
			if(queueSize() == 1) {
				SimEvent e = new SimEvent(SchedulableType.DELIVERY,this,tempTimestamp);
				mSched.addEvent(e);
			} else {
				mCallbackTimes.put(p,new Long(tempTimestamp));
			}
		}
		// queue is full, drop packet?
		else {
			SimLogger.logDrop(p,this,mSched.getGlobalSimTime());
		}
		
		
		
	}

	@Override
	public void schedCallback(SchedulableType type) {
		// perform action when queued events are selected in the scheduler
		super.schedCallback(type); // TODO: extend callback behavior?
	}

	@Override
	public Link forward(Packet p) {
		return super.forward(p); // TODO: extend forwarding behavior
	}

}
