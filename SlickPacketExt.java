import java.util.ArrayList;
import java.util.LinkedList;

//package org.timecrunch;

public class SlickPacketExt extends Packet implements ISlickPackets {
	public static final int PROBES_NOT_ALLOWED = 0;
	public SlickPacketExt(Host src, Host dest, int probeSeqNum, int payload) {
		this(src,dest,probeSeqNum,payload,null,null,DEFAULT_EVENT_GROUP_ID);
	}

	public SlickPacketExt(Host src, Host dest, int probeSeqNum, int payload, LinkedList<Link> path,
			ArrayList<LinkedList<Link>> failovers, int egid) {
		super(payload,egid);
		SlickPacketExtHeader header = new SlickPacketExtHeader(path,failovers,probeSeqNum);
		header.setSrc(src);
		mHeader.add(header);
		setDest(dest);		
	}

	public void setNewPath(LinkedList<Link> path) {
		SlickPacketExtHeader speh = (SlickPacketExtHeader)mHeader.getLast();
		speh.setNewPath(path,null);
	}

	public LinkedList<Link> getPath() {
		SlickPacketExtHeader speh = (SlickPacketExtHeader)mHeader.getLast();
		return speh.getPath();
	}

	public Link getNextLink() {
		SlickPacketExtHeader speh = (SlickPacketExtHeader)mHeader.getLast();
		return speh.getNextLink();
	}

	public LinkedList<Link> getFailover() {
		SlickPacketExtHeader speh = (SlickPacketExtHeader)mHeader.getLast();
		return speh.getFailover();
	}

	public void setNewPath(LinkedList<Link> path, ArrayList<LinkedList<Link>> failovers) {
		SlickPacketExtHeader speh = (SlickPacketExtHeader)mHeader.getLast();
		speh.setNewPath(path,failovers);
	}

}
