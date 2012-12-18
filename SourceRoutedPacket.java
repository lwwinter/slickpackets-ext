import java.util.LinkedList;

//package org.timecrunch;

public class SourceRoutedPacket extends Packet implements IRoutable {
	public SourceRoutedPacket(Host dest, int payload) {
		this(dest,payload,null,DEFAULT_EVENT_GROUP_ID);
	}

	public SourceRoutedPacket(Host dest, int payload, LinkedList<Link> path, int egid) {
		super(payload,egid);
		mHeader.add(new SourceRoutedPacketHeader(path));
		setDest(dest);
	}
	
}
