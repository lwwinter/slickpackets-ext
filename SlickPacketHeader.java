import java.util.ArrayList;
import java.util.LinkedList;

//package org.timecrunch;

public class SlickPacketHeader extends PacketHeader {

	// These are already included as part of SourceRoutedPacketHeader
	protected LinkedList<Link> mPath ;
	// not necessarily part of a source-routed header but very easy for header processing
	protected int mIndex;
	protected ArrayList<LinkedList<Link>> mFailovers;
	
	public SlickPacketHeader() {
		this(null,null);
	}

	public SlickPacketHeader(LinkedList<Link> path, ArrayList<LinkedList<Link>> failovers) {
		super(PacketType.SLICK_PACKET);
		mPath = path ;
		mIndex = 0;
		mFailovers = failovers;
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
}
