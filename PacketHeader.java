//package org.timecrunch;

public class PacketHeader {
	protected PacketType mPacketType;
	protected Host mDest;

	public PacketHeader(PacketType type) {
		this(type,null);
	}

	public PacketHeader(PacketType type, Host dest) {
		mPacketType = type;
		mDest = dest;
	}

	public PacketType getType() {
		return mPacketType;
	}

	public Host getDest() {
		return mDest;
	}

	public void setDest(Host dest) {
		mDest = dest;
	}
}
