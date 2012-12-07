package org.timecrunch;

import java.util.ArrayList;

public class SimpleLink extends Link {
	protected long mBandwidth;
	protected long mLatency;
	protected long mPacketLastAddedTime;
	protected long mLastPacketSize;

	public SimpleLink(long bandwidth, long latency, ArrayList<Host> hosts) {
		super(new InfFifoQScheme(), hosts);
		mBandwidth = bandwidth;
		mLatency = latency;
		mPacketLastAddedTime = 0;
		mLastPacketSize = 0;
	}

	@Override
	public boolean isLinkFree() {
		return true; // TODO: placeholder
	}

	// Corresponds to departure event
	public void sendTo(Packet p, Host dest) {
		// TODO
	}

	// Corresponds to arrival event
	public void recvFrom(Packet p, Host src) {
		// TODO
	}

	public void schedCallback(ISchedulable event) {
		// TODO: perform action when queued events are selected in the scheduler
		// Probably just sendTo(p,nextRouter) or something similar here
	}

}
