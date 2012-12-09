//package org.timecrunch;

public class SimEvent {
	protected long mTriggerTime;
	protected SchedulableType mType;
	protected ISchedulerSource mSource;

	public SimEvent(SchedulableType type, ISchedulerSource source) {
		this(type,source,source.getScheduler().getGlobalSimTime());
	}

	public SimEvent(SchedulableType type, ISchedulerSource source, long triggerTime) {
		mType = type;
		mSource = source;
		mTriggerTime = triggerTime;
	}

	public void callback() {
		// shouldn't need triggerTime -> can only be called when globalSimTime = triggerTime
		mSource.schedCallback(mType);
	}

	public long getTriggerTime() {
		return mTriggerTime;
	}
}
