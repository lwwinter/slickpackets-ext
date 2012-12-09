//package org.timecrunch;

public class Packet implements ISchedulable {
	// TODO: Header values as needed
	// TODO: Will probably extend as needed
	int mEventGroupId;
	int mPayloadSize;
	protected PacketDelays mPacketDelays;

	public Packet(int payload) {
		this(payload,0);
	}

	public Packet(int payload, int egid) {
		mPayloadSize = payload;
		mEventGroupId = egid;
		if(GlobalSimSettings.LogDelays) {
			mPacketDelays = new PacketDelays();
		} else {
			mPacketDelays = null;
		}
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
}
