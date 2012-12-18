//package org.timecrunch;

public class CongestionStateUpdate extends Packet implements IRoutable {
	// too small & simple to merit implementation as a standalone class
	protected class CongestionStateUpdateHeader extends PacketHeader {
		CongestionStateUpdateHeader() {
			super(PacketType.CONGESTION_STATE_UPDATE);
		}
	}

	// CONSTANTS
	// state can actually be encoded in 2 bits leaving room for 64 neighbor-ids/Host
	// TODO: may want to more explicitly model this in header
	private static final int DEFAULT_PAYLOAD = 8;

	// MEMBERS
	protected Host mSender;
	protected CongestionState mState;

	public CongestionStateUpdate(Host sender, CongestionState cs) {
		super(DEFAULT_PAYLOAD,DEFAULT_EVENT_GROUP_ID);
		mHeader.add(new CongestionStateUpdateHeader());
		mSender = sender;
		mState = cs;
	}

	public Host getSender() {
		return mSender;
	}

	public CongestionState getState() {
		return mState;
	}
}
