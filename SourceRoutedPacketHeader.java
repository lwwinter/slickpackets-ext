import java.util.LinkedList;

//package org.timecrunch;

public class SourceRoutedPacketHeader extends PacketHeader {

	protected LinkedList<Link> mPath ;
	// not necessarily part of a source-routed header but very easy for header processing
	protected int mIndex;
	
	public SourceRoutedPacketHeader() {
		this(null);
	}

	public SourceRoutedPacketHeader(LinkedList<Link> path) {
		super(PacketType.SOURCE_ROUTED);
		mPath = path ;
		mIndex = 0;
	}

	//additional data/functionality, like path to take
	
	public Link getNextLink(){
		if(mPath != null) {
			return  mPath.get(mIndex++);
		}

		return null;
	}
	
	public void setNewPath(LinkedList<Link> path){
		mPath = path ;
		mIndex = 0;
	}	
}
