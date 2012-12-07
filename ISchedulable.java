package org.timecrunch;

public interface ISchedulable {
	int eventGroupId();

	// For classes where size doesn't make sense, simply return 1
	int size();
}
