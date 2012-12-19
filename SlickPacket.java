import java.util.ArrayList;
import java.util.LinkedList;

//package org.timecrunch;

public class SlickPacket extends Packet implements ISlickPackets {
	public SlickPacket(Host dest, int payload) {
		this(dest,payload,null,null,DEFAULT_EVENT_GROUP_ID);
	}

	public SlickPacket(Host dest, int payload, LinkedList<Link> path,
			ArrayList<LinkedList<Link>> failovers, int egid) {
		super(payload,egid);
		mHeader.add(new SlickPacketHeader(path,failovers));
		setDest(dest);
	}

	public void setNewPath(LinkedList<Link> path) {
		SlickPacketHeader sph = (SlickPacketHeader)mHeader.getLast();
		sph.setNewPath(path,null);
	}

	public LinkedList<Link> getPath() {
		SlickPacketHeader sph = (SlickPacketHeader)mHeader.getLast();
		return sph.getPath();
	}

	public Link getNextLink() {
		SlickPacketHeader sph = (SlickPacketHeader)mHeader.getLast();
		return sph.getNextLink();
	}

	public LinkedList<Link> getFailover() {
		SlickPacketHeader sph = (SlickPacketHeader)mHeader.getLast();
		return sph.getFailover();
	}

	public void setNewPath(LinkedList<Link> path, ArrayList<LinkedList<Link>> failovers) {
		SlickPacketHeader sph = (SlickPacketHeader)mHeader.getLast();
		sph.setNewPath(path,failovers);
	}

}
