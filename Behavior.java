//package org.timecrunch;

public abstract class Behavior implements ISchedulerSource {
	// CONSTANTS
	public static final long DEFAULT_START_TIME = 0;

	// MEMBERS 
	private static int gBehaviorId = 1;
	private int mId;
	protected String mStringId;
	protected SimScheduler mSched;
	protected ISchedulerSource mTarget;
	protected long mStartTime;

	public Behavior(ISchedulerSource target) {
		this(target,DEFAULT_START_TIME);
	}

	public Behavior(ISchedulerSource target, long startTime) {
		mId = gBehaviorId++;
		mStringId = "Behavior#"+mId;
		mTarget = target;
		mStartTime = startTime;
	}

	public String getId() {
		return mStringId;
	}

	public void setId(String id) {
		mStringId = id;
	}

	// Useful as stream id, etc.
	public int getBehaviorId() {
		return mId;
	}

	public void registerScheduler(SimScheduler sched) {
		mSched = sched;
		registerFirstEvent();
	}

	public SimScheduler getScheduler() {
		return mSched;
	}

	protected abstract void registerFirstEvent();

	// from interface ISchedulerSoucre
	//public void schedCallback(SchedulableType type);

}
