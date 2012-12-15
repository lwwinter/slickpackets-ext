import java.util.LinkedList;

//package org.timecrunch;

public class SourceRoutedPacket extends Packet implements IRoutable {
	protected Host mDest;

	public SourceRoutedPacket(LinkedList<Link> path , Host dest, int payload) {
		this(path, dest,payload,DEFAULT_EVENT_GROUP_ID);
	}

	public SourceRoutedPacket(LinkedList<Link> path, Host dest, int payload, int egid) {
		super(payload,egid);
		mDest = dest;
		mHeader.add(new SourceRoutedPacketHeader(path));
	}

	public Host getDestination() {
		return mDest;
	}
	
}
