//package org.timecrunch;

public class SourceRoutedPacket extends Packet implements IRoutable {
	protected Host mDest;

	public SourceRoutedPacket(Host dest, int payload) {
		this(dest,payload,DEFAULT_EVENT_GROUP_ID);
	}

	public SourceRoutedPacket(Host dest, int payload, int egid) {
		super(payload,egid);
		mDest = dest;
		mHeader.add(new SourceRoutedPacketHeader());
	}

	public Host getDestination() {
		return mDest;
	}
}
