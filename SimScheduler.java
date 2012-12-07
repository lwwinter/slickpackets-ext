package org.timecrunch;

import java.util.LinkedList;

public class SimScheduler {
	// CONSTANTS
	private static final long DEFAULT_START_TIME = 0;

	// MEMBERS
	protected long globalSimTime;
	// TODO: Find a better structure for this
	protected LinkedList<SimEvent> mSchedule;

	public SimScheduler(){
		this(DEFAULT_START_TIME);
	}

	public SimScheduler(long initialSimTime){
		globalSimTime = initialSimTime;
		mSchedule = new LinkedList<SimEvent>();
	}

	public void run(long duration) {
		SimEvent e;
		while(mSchedule.size() > 0) {
			e = mSchedule.removeFirst();
			e.callback();

			// TODO: update sim time
			if(globalSimTime > duration) {
				break;
			}
		}
	}

	public void addEvent(SimEvent e) {
		// TODO: Just uploading base skeleton code for now
		// FIXME: Need to handle EventGroups properly (ie preserve in-order streams)
	}

	public long getGlobalSimTime() {
		return globalSimTime;
	}

}
