//package org.timecrunch;

import java.util.LinkedList;

public interface ISourceRoutable extends IRoutable {

	public void setNewPath(LinkedList<Link> path);

	public LinkedList<Link> getPath();

	public Link getNextLink();

}
