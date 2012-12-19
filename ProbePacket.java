import java.util.LinkedList;

//package org.timecrunch;

public class ProbePacket extends Packet implements ISourceRoutable {
	// too small & simple to merit implementation as a standalone class
	protected class ProbePacketHeader extends PacketHeader {
		ProbePacketHeader() {
			super(PacketType.PROBE_PACKET);
		}
	}

	protected Host mFrom;
	protected Host mTo;
	protected LinkedList<Link> mPath;
	protected int mIndex;

	public ProbePacket(Host src, Host dest, int payload) {
		this(src,dest,payload,null,DEFAULT_EVENT_GROUP_ID);
	}

	public ProbePacket(Host src, Host dest, int payload, LinkedList<Link> path, int egid) {
		super(payload,egid);
		mHeader.add(new ProbePacketHeader());
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

	public Link getNextLink(){
		if(mPath != null) {
			return  mPath.get(mIndex++);
		}

		return null;
	}
}
