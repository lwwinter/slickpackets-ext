import java.util.ArrayList;
import java.util.LinkedList;

public class RouteCacheEntry {
	// CONSTANTS
	protected static final int PROBE_SEQ_NUM_INITIALIZER = 1;
	public static final long RTT_INITIALIZER = -1;
	public static final long LAST_PROBE_ALLOWED_TIME_INITIALIZER = -1;

	public LinkedList<Link> path;
	public ArrayList<LinkedList<Link>> failovers;
	public boolean stale;
	// NOTE: probeSeqNum MUST wrap around safely, negative is ok though
	public int probeSeqNum;
	public long rtt;
	public long lastProbeAllowedTime;

	public RouteCacheEntry() {
		this((LinkedList<Link>)null,(ArrayList<LinkedList<Link>>)null);
	}

	public RouteCacheEntry(LinkedList<Link> path) {
		this(path,null);
	}

	public RouteCacheEntry(LinkedList<Link> path, ArrayList<LinkedList<Link>> failovers) {
		this.path = path;
		this.failovers = failovers;
		this.stale = false;
		this.probeSeqNum = PROBE_SEQ_NUM_INITIALIZER;
		this.rtt = estimateRtt();
		this.lastProbeAllowedTime = LAST_PROBE_ALLOWED_TIME_INITIALIZER;
	}

	// pseudo-copy constructor 1 - creates new entry (with new path/failovers) from an entry
	public RouteCacheEntry(RouteCacheEntry rce, LinkedList<Link> path) {
		this(rce,path,null);
	}

	// pseudo-copy constructor 2 - creates new entry (with new path/failovers) from an entry
	public RouteCacheEntry(RouteCacheEntry rce, LinkedList<Link> path,
			ArrayList<LinkedList<Link>> failovers) {
		this.path = path;
		this.failovers = failovers;
		this.stale = false;
		this.probeSeqNum = rce.probeSeqNum;
		this.rtt = estimateRtt(rce.rtt); // old value is still better than reinitializing completely
		this.lastProbeAllowedTime = LAST_PROBE_ALLOWED_TIME_INITIALIZER; // immediately allow probes
	}

	public void setPath(LinkedList<Link> path) {
		this.path = path;
		this.rtt = estimateRtt(this.rtt); // old value is still better than reinitializing completely
		this.stale = false;
	}

	private long estimateRtt() {
		return estimateRtt(RTT_INITIALIZER);
	}

	/* TODO: Currently estimates RTT as 2x latency of primary path to destination.
	 * This is effectively cheating by using knowledge of the full network topology
	 * (including link latencies). It will always be on the low end though, since
	 * other delays are ignored using this method. SlickPacketsExt in particular may
	 * benefit from a setup phase where reachability is checked and RTT may be estimated. */
	// errVal = value to use if any problem is encountered, ie path is null
	private long estimateRtt(long errVal) {
		if(this.path == null || this.path.size() == 0) {
			return errVal;
		}

		long counter = 0;
		for(Link l : this.path) {
			counter += l.getLatency();
		}

		return 2*counter;
	}
}
