package org.timecrunch;

public class SimEvent {
	protected long mTriggerTime;
	protected ISchedulable mEvent;
	protected ISchedulerSource mSource;

	public SimEvent(ISchedulable event, ISchedulerSource source, SimScheduler scheduler) {
		this(event,source,scheduler.getGlobalSimTime());
	}

	public SimEvent(ISchedulable event, ISchedulerSource source, long triggerTime) {
		mEvent = event;
		mSource = source;
		mTriggerTime = triggerTime;
	}

	public void callback() {
		// shouldn't need triggerTime -> can only be called when globalSimTime = triggerTime
		mSource.schedCallback(mEvent);
	}

	public long getTriggerTime() {
		return mTriggerTime;
	}
}
