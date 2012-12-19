//package org.timecrunch;

import java.util.ArrayList;
import java.util.LinkedList;

public interface ISlickPackets extends ISourceRoutable {

	public LinkedList<Link> getFailover();

	public void switchToFailover(LinkedList<Link> failover);

	public void setNewPath(LinkedList<Link> path, ArrayList<LinkedList<Link>> failovers);

}
