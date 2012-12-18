import java.util.ArrayList;
import java.util.LinkedList;

public class RouteCacheEntry {
	public LinkedList<Link> path;
	public ArrayList<LinkedList<Link>> failovers;
	public boolean stale;
	public Packet mDelayedPacket;

	public RouteCacheEntry() {
		this(null,null);
	}

	public RouteCacheEntry(LinkedList<Link> path) {
		this(path,null);
	}

	public RouteCacheEntry(LinkedList<Link> path, ArrayList<LinkedList<Link>> failovers) {
		this.path = path;
		this.failovers = failovers;
		this.stale = false;
	}
}
