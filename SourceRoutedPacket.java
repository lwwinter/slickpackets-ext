import java.util.LinkedList;

//package org.timecrunch;

public class SourceRoutedPacket extends Packet implements ISourceRoutable {
	public SourceRoutedPacket(Host dest, int payload) {
		this(dest,payload,null,DEFAULT_EVENT_GROUP_ID);
	}

	public SourceRoutedPacket(Host dest, int payload, LinkedList<Link> path, int egid) {
		super(payload,egid);
		mHeader.add(new SourceRoutedPacketHeader(path));
		setDest(dest);
	}

	public void setNewPath(LinkedList<Link> path) {
		SourceRoutedPacketHeader srph = (SourceRoutedPacketHeader)mHeader.getLast();
		srph.setNewPath(path);
	}

	public LinkedList<Link> getPath() {
		SourceRoutedPacketHeader srph = (SourceRoutedPacketHeader)mHeader.getLast();
		return srph.getPath();
	}

	public Link getNextLink() {
		SourceRoutedPacketHeader srph = (SourceRoutedPacketHeader)mHeader.getLast();
		return srph.getNextLink();
	}
}
