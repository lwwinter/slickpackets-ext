package org.timecrunch;

public class Packet implements ISchedulable {
	// TODO: Header values as needed, ie timestamps for measurement
	// TODO: Will probably extend as needed
	int mEventGroupId;
	int mPayloadSize;

	public Packet(int payload) {
		this(payload,0);
	}

	public Packet(int payload, int egid) {
		mPayloadSize = payload;
		mEventGroupId = egid;
	}

	public int eventGroupId() {
		return mEventGroupId;
	}

	public int size() {
		return mPayloadSize;
	}

}
