//package org.timecrunch;

import java.lang.String;

public interface ISchedulerSource {

	String getId();

	void setId(String id);

	void schedCallback(SchedulableType type);

	void registerScheduler(SimScheduler sched);

	SimScheduler getScheduler();

}
