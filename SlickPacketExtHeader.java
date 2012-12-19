import java.util.ArrayList;
import java.util.LinkedList;

//package org.timecrunch;

public class SlickPacketExtHeader extends PacketHeader {

	protected LinkedList<Link> mPath ;
	// not necessarily part of a source-routed header but very easy for header processing
	protected int mIndex;
	protected ArrayList<LinkedList<Link>> mFailovers;
	protected int mProbeSeqNum;
	protected boolean mSplitProbe;
	protected Host mSrc;
	
	public SlickPacketExtHeader() {
		this(null,null,SlickPacketExt.PROBES_NOT_ALLOWED);
	}

	public SlickPacketExtHeader(LinkedList<Link> path, ArrayList<LinkedList<Link>> failovers,
			int probeSeqNum) {
		super(PacketType.SLICK_PACKET_EXT);
		mPath = path ;
		mIndex = 0;
		mFailovers = failovers;
		mProbeSeqNum = probeSeqNum;
		mSplitProbe = false;
		mSrc = null;
	}

	//additional data/functionality, like path to take
	
	public Link getNextLink(){
		if(mPath != null) {
			return  mPath.get(mIndex++);
		}

		return null;
	}

	public LinkedList<Link> getFailover() {
		// Should have already incremented mIndex before handling failures (must check Link)
		if(mFailovers != null) {
			if(mIndex > 0) {
				return mFailovers.get(mIndex-1);
			} else {
				return mFailovers.get(0);
			}
		}

		return null;
	}
	
	public void setNewPath(LinkedList<Link> path, ArrayList<LinkedList<Link>> failovers){
		mPath = path ;
		mIndex = 0;
		mFailovers = failovers;
	}

	public LinkedList<Link> getPath() {
		return mPath;
	}

	public void setSrc(Host src) {
		mSrc = src;
	}

	public boolean canSendProbes() {
		return (mProbeSeqNum != SlickPacketExt.PROBES_NOT_ALLOWED);
	}

	public boolean hasSplitProbe() {
		return mSplitProbe;
	}
}
