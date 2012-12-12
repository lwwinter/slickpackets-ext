/* SendAtRateBehavior.java
 * Host sends some packet-type of specified size (in bits) to some destination Host at
 * a specified sending rate for the specified duration.
 * Negative duration implies infinite sending (events will be queued indefinitely).
 * Sending rate is still limited by link bandwidth.
 * Deriving behaviors this way is INSANELY quick and dirty, but it should work for now
 */

//package org.timecrunch;

public class SendAtRateBehavior extends Behavior {
	// CONSTANTS
	protected static final int DEFAULT_PACKET_SIZE = 240; // 30 bytes
	protected static final PacketType DEFAULT_PACKET_TYPE = PacketType.SOURCE_ROUTED; // simplest routable type
	protected static final long DEFAULT_DURATION = -1; // infinite

	// MEMBERS
	protected Host mDest; // destination host
	protected int mSendingRate; // sending rate in bits/s
	protected int mPacketSize; // size of packets in bits (optional)
	protected PacketType mPacketType; // type of packet to instantiate (optional)
	protected long mDuration; // duration of behavior (optional)
	private long mBitUsProcessed; // used to model quantized delay for any sending rate

	public SendAtRateBehavior(Host target, Host dest, int rate) {
		this(target,dest,rate,DEFAULT_PACKET_SIZE,DEFAULT_PACKET_TYPE,DEFAULT_START_TIME,DEFAULT_DURATION);
	}

	public SendAtRateBehavior(Host target, Host dest, int rate, int size, PacketType type,
			long startTime, long duration) {
		super(target,startTime);
		mDest = dest;
		mSendingRate = rate;
		mPacketSize = size;
		mPacketType = type;
		mDuration = duration;
		mBitUsProcessed = 0;
	}

	protected Host getTargetHost() {
		return (Host)mTarget;
	}

	protected void registerFirstEvent() {
		// setup very first event with the scheduler
		SimEvent e = new SimEvent(SchedulableType.HOST_SEND,this,mStartTime);
		mSched.addEvent(e);
	}

	// from interface ISchedulerSoucre
	public void schedCallback(SchedulableType type) {
		switch(type) {
			case HOST_SEND:
				sendPacket();
				break;
			default:
				SimLogger.logEventLoss(SchedulableType.INVALID,this);
				break;
		}
	}

	private void sendPacket() {
		// Create packet
		Packet p;
		switch(mPacketType) {
			case SOURCE_ROUTED:
				p = new SourceRoutedPacket(mDest,mPacketSize,getBehaviorId());
				break;
			default:
				p = new Packet(mPacketSize,getBehaviorId());
				break;
		}

		// push to associated Host on 'null' Link
		getTargetHost().recvOn(p,null);

		// queue next packet
		long nextSend = mSched.getGlobalSimTime() + getSendingDelay(mPacketSize);
		if(nextSend < mStartTime + mDuration) {
			SimEvent e = new SimEvent(SchedulableType.HOST_SEND,this,nextSend);
			mSched.addEvent(e);
		}
	}

	private long getSendingDelay(int size) {
		mBitUsProcessed += size*1000000;
		long rateLimitedDelay = mBitUsProcessed/mSendingRate;
		mBitUsProcessed %= mSendingRate;
		return rateLimitedDelay;
	}

}
