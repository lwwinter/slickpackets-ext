//package org.timecrunch;

import java.util.PriorityQueue;
import java.util.Comparator;
import java.lang.Long;

public class SimScheduler {
	// CONSTANTS
	private static final long DEFAULT_START_TIME = 0;
	private static final int DEFAULT_INITIAL_QUEUE_LENGTH = 11;

	// MEMBERS
	protected long mInitialSimTime;
	protected long mSimStartTime;
	protected long mSimTime = DEFAULT_START_TIME;
	protected PriorityQueue<SimEvent> mSchedule;
	protected Simulator mSimulator;

	public SimScheduler(){
		this(DEFAULT_START_TIME);
	}

	public SimScheduler(long initialSimTime){
		mInitialSimTime = initialSimTime;
		mSimStartTime = initialSimTime;
		mSimTime = initialSimTime;
		Comparator<SimEvent> comparator = new EventTimeComparator();
		mSchedule = new PriorityQueue<SimEvent>(DEFAULT_INITIAL_QUEUE_LENGTH,comparator);
		mSimulator = null;
	}

	public void registerSimulator(Simulator s) {
		mSimulator = s;
	}

	public void run(long duration) {
		SimEvent e;
		long simEndTime;
		if(duration < 0) {
			simEndTime = Long.MAX_VALUE;
		} else {
			simEndTime = mSimStartTime + duration;
		}
		while(mSchedule.size() > 0) {
			e = mSchedule.poll();
			if(e != null) {
				mSimTime = e.getTriggerTime();
				//System.out.println("Current SimTime: " + mSimTime); // DEBUG
				//System.out.println("Processing event for "+e.getSourceId()); // DEBUG
				e.callback();
			}

			// TODO: handle long wraparound safely
			if(mSimTime > simEndTime) {
				break;
			}
		}
		mSimStartTime = mSimTime;
	}

	public void addEvent(SimEvent e) {
		/* NOTE: May want/need concept of EventGroups (ie to preserve in-order streams).
		 * However, only 1 event at a time will ever be scheduled from hosts/links/etc
		 * and queueing schemes for these have been selected to avoid this problem. */
		mSchedule.add(e);
		//System.out.println("SimEvent: Added " + e.getType() + " from " + e.getSourceId() + "; triggerTime = " + e.getTriggerTime()); // DEBUG
	}

	public long getGlobalSimTime() {
		return mSimTime;
	}

	// Should generally not use this method until FIXME is handled
	public void resetSimTime() {
		// FIXME: rebuild events for the initial time
		mSimStartTime = mInitialSimTime;
		mSimTime = mInitialSimTime;
	}

	public NetworkGraph getNetworkGraph() {
		if(mSimulator != null) {
			return mSimulator.getNetworkGraph();
		} else {
			return null;
		}
	}
}
