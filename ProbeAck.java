import java.util.LinkedList;

//package org.timecrunch;

public class ProbeAck extends Packet implements ISourceRoutable {
	// too small & simple to merit implementation as a standalone class
	protected class ProbeAckHeader extends PacketHeader {
		ProbeAckHeader() {
			super(PacketType.PROBE_ACK);
		}
	}

	// CONSTANTS
	// FIXME: Should probably model this more explicitly in header
	private static final int DEFAULT_PAYLOAD = 64; // FIXME: very wrong, but half IPv6 for now (no failover paths)

	protected Host mFrom;
	protected Host mTo;
	protected LinkedList<Link> mPath;
	protected int mIndex;

	public ProbeAck(Host src, Host dest) {
		this(src,dest,null,DEFAULT_EVENT_GROUP_ID);
	}

	// Explicitly assumes that links are bidirectional
	// TODO: May need to address this assumption
	public ProbeAck(ProbePacket pp) {
		this(pp.getDest(),pp.getSrc(),pp.getReversePath(),pp.eventGroupId());
	}

	public ProbeAck(Host src, Host dest, LinkedList<Link> path, int egid) {
		super(DEFAULT_PAYLOAD,egid);
		mHeader.add(new ProbeAckHeader());
		mFrom = src;
		mTo = dest;
		mPath = path;
		mIndex = 0;
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
