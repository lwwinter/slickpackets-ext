import java.util.ArrayList;
import java.util.LinkedList;

//package org.timecrunch;

public class SlickPacket extends Packet implements IRoutable {
	public SlickPacket(Host dest, int payload) {
		this(dest,payload,null,null,DEFAULT_EVENT_GROUP_ID);
	}

	public SlickPacket(Host dest, int payload, LinkedList<Link> path,
			ArrayList<LinkedList<Link>> failovers, int egid) {
		super(payload,egid);
		mHeader.add(new SlickPacketHeader(path,failovers));
		setDest(dest);
	}
	
}
