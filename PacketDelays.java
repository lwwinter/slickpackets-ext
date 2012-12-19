//package org.timecrunch;

public class PacketDelays {
	protected long mQueueingDelay;
	protected long mLinkBusyDelay;
	protected long mTransmissionDelay;
	protected long mPropagationDelay;

	public PacketDelays() {
		mQueueingDelay = 0;
		mLinkBusyDelay = 0;
		mTransmissionDelay = 0;
		mPropagationDelay = 0;
	}

	public PacketDelays(PacketDelays pd) {
		mQueueingDelay = pd.getQueueingDelay();
		mLinkBusyDelay = pd.getLinkBusyDelay();
		mTransmissionDelay = pd.getTransmissionDelay();
		mPropagationDelay = pd.getPropagationDelay();
	}

	public long getQueueingDelay() {
		return mQueueingDelay;
	}

	public long getLinkBusyDelay() {
		return mLinkBusyDelay;
	}

	public long getTransmissionDelay() {
		return mTransmissionDelay;
	}

	public long getPropagationDelay() {
		return mPropagationDelay;
	}

	public long getTotalDelay() {
		return mQueueingDelay+mLinkBusyDelay+mTransmissionDelay+mPropagationDelay;
	}
	
	public void logQueueingDelay(long delay) {
		mQueueingDelay += delay;
	}

	public void logLinkBusyDelay(long delay) {
		mLinkBusyDelay += delay;
	}

	public void logTransmissionDelay(long delay) {
		mTransmissionDelay += delay;
	}

	public void logPropagationDelay(long delay) {
		mPropagationDelay += delay;
	}	

}
