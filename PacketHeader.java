//package org.timecrunch;

public class PacketHeader {
	protected PacketType mPacketType;

	public PacketHeader(PacketType type) {
		mPacketType = type;
	}

	public PacketType getType() {
		return mPacketType;
	}
}
