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
		LinkedList<Link> rem;
		int indexToGet = (mIndex > 0) ? mIndex-1 : 0;
		if(mFailovers != null) {
			rem = mFailovers.get(indexToGet);
		} else {
			rem = null;
		}

		if(rem == null) {
			return null;
		}

		int tempIndex = 0;
		LinkedList<Link> ret = new LinkedList<Link>();
		// mIndex should have been already incremented
		while(tempIndex < indexToGet) {
			ret.add(mPath.get(tempIndex++));
		}

		ret.addAll(rem);

		if(ret.size() == 0) {
			return null;
		}

		return ret;
	}

	// keeps mIndex intact
	public void switchToFailover(LinkedList<Link> failover) {
		mPath = failover;
		mFailovers = null;
		mIndex--; // should have been incremented to check if link failed
	}

	public void setNewPath(LinkedList<Link> path, ArrayList<LinkedList<Link>> failovers){
		mPath = path ;
		mIndex = 0;
		mFailovers = failovers;
	}

	public LinkedList<Link> getPath() {
		return mPath;
	}

	public Host getSrc() {
		return mSrc;
	}

	public void setSrc(Host src) {
		mSrc = src;
	}

	public int getProbeSeqNum() {
		return mProbeSeqNum;
	}

	public void setProbeSeqNum(int psn) {
		mProbeSeqNum = psn;
	}

	public boolean canSendProbes() {
		return (mProbeSeqNum != SlickPacketExt.PROBES_NOT_ALLOWED);
	}

	public boolean hasSplitProbe() {
		return mSplitProbe;
	}

	public ProbePacket spawnProbe(int egid) {
		if(!canSendProbes()) {
			return null;
		}

		ProbePacket pp = new ProbePacket(mSrc,mDest,mProbeSeqNum,getFailover(),
				((mIndex>0) ? mIndex-1 : 0),egid);
		mSplitProbe = true;

		return pp;
	}

}
