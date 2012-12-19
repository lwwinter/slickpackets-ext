import java.util.LinkedList;

//package org.timecrunch;

public class ProbePacket extends Packet implements ISourceRoutable {
	// too small & simple to merit implementation as a standalone class
	protected class ProbePacketHeader extends PacketHeader {
		ProbePacketHeader() {
			super(PacketType.PROBE_PACKET);
		}
	}

	// CONSTANTS
	// FIXME: Should probably model this more explicitly in header
	private static final int DEFAULT_PAYLOAD = 64; // FIXME: very wrong, but half IPv6 for now (no failover paths)

	protected Host mFrom;
	protected Host mTo;
	protected int mProbeSeqNum;
	protected LinkedList<Link> mPath;
	protected int mIndex;

	public ProbePacket(Host src, Host dest, int probeSeqNum) {
		this(src,dest,probeSeqNum,null,0,DEFAULT_EVENT_GROUP_ID);
	}

	public ProbePacket(Host src, Host dest, int probeSeqNum, LinkedList<Link> path,
			int index, int egid) {
		super(DEFAULT_PAYLOAD,egid);
		mHeader.add(new ProbePacketHeader());
		mFrom = src;
		mTo = dest;
		mProbeSeqNum = probeSeqNum;
		mPath = path;
		mIndex = index;
	}

	public int getProbeSeqNum() {
		return mProbeSeqNum;
	}

	public void setNewPath(LinkedList<Link> path){
		mPath = path ;
		mIndex = 0;
	}

	public LinkedList<Link> getPath() {
		return mPath;
	}

	public LinkedList<Link> getReversePath() {
		if(mPath == null || mPath.size() == 0) {
			return null;
		}

		LinkedList<Link> revPath = new LinkedList<Link>();
		for(Link l : mPath) {
			revPath.addFirst(l);
		}

		return revPath;
	}

	public Link getNextLink(){
		if(mPath != null) {
			return  mPath.get(mIndex++);
		}

		return null;
	}

	public Host getSrc() {
		return mFrom;
	}

	@Override
	public Host getDest() {
		return mTo;
	}
}
