import java.util.LinkedList;

//package org.timecrunch;

public class SourceRoutedPacketHeader extends PacketHeader {

	private LinkedList<Link> mPath ;
	
	public SourceRoutedPacketHeader(LinkedList<Link> path) {
		super(PacketType.SOURCE_ROUTED);
		mPath = path ;
	}

	//additional data/functionality, like path to take
	
	public Link getNextLink(){
		return  mPath.removeFirst() ;
	}
	
	public void setNewpath(LinkedList<Link> path){
		mPath = path ;
	}
	
}
