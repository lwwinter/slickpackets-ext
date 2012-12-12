//package org.timecrunch;

import java.util.LinkedList;

// Extend Packet as needed (generally needs corresponding PacketHeader)
public class Packet implements ISchedulable {
	// CONSTANTS
	protected static final int DEFAULT_EVENT_GROUP_ID = 0;

	// MEMBERS
	LinkedList<PacketHeader> mHeader; // Linked-list of PacketHeaders for easy encapsulation
	int mEventGroupId;
	int mPayloadSize;
	protected PacketDelays mPacketDelays;

	public Packet(int payload) {
		this(payload,DEFAULT_EVENT_GROUP_ID);
	}

	public Packet(int payload, int egid) {
		mPayloadSize = payload;
		mEventGroupId = egid;
		if(GlobalSimSettings.LogDelays) {
			mPacketDelays = new PacketDelays();
		} else {
			mPacketDelays = null;
		}
		mHeader = new LinkedList<PacketHeader>();
		mHeader.add(new PacketHeader(PacketType.NO_TYPE));
	}

	// copy constructor
	public Packet(Packet p) {
		mEventGroupId = p.eventGroupId();
		mPayloadSize = p.size();
		PacketDelays pd = p.getDelays();
		if(pd != null) {
			pd = new PacketDelays(p.getDelays());
		}
		mPacketDelays = pd;
	}

	public int eventGroupId() {
		return mEventGroupId;
	}

	public int size() {
		return mPayloadSize;
	}

	public ISchedulable clone() {
		return new Packet(this);
	}

	public PacketDelays getDelays() {
		return mPacketDelays;
	}

	public PacketType getType() {
		return mHeader.getLast().getType();
	}

	public PacketHeader getHeader() {
		return mHeader.getLast();
	}

	public PacketHeader decapsulate() {
		if(mHeader.size() > 1) {
			return mHeader.removeLast();
		}

		return null;
	}
}
