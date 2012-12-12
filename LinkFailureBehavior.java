//package org.timecrunch;

public class LinkFailureBehavior extends Behavior {

	public LinkFailureBehavior(Link target) {
		this(target,DEFAULT_START_TIME);
	}

	public LinkFailureBehavior(Link target, long startTime) {
		super(target,startTime);
	}

	public Link getTargetLink() {
		return (Link)mTarget;
	}

	protected void registerFirstEvent() {
		// setup very first event with the scheduler
		SimEvent e = new SimEvent(SchedulableType.LINK_FAIL,this,mStartTime);
		mSched.addEvent(e);
	}

	// from interface ISchedulerSoucre
	public void schedCallback(SchedulableType type) {
		switch(type) {
			case LINK_FAIL:
				getTargetLink().setEnabled(false);
				break;
			default:
				SimLogger.logEventLoss(SchedulableType.INVALID,this);
				break;
		}
	}
}
