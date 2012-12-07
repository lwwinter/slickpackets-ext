package org.timecrunch;

import java.util.ArrayList;

public class Simulator {
	protected SimScheduler mScheduler;
	protected ArrayList<Host> mHosts;
	protected ArrayList<Link> mLinks;

	public Simulator(String config_file) {
		mScheduler = new SimScheduler();
		mHosts = new ArrayList<Host>();
		mLinks = new ArrayList<Link>();

		SimLoader loader = new SimLoader();
		loader.load(config_file,this);
	}

	public void start() {
		mScheduler.run(-1);
	}

	public void start(long duration) {
		mScheduler.run(duration);
	}

	public void addHost(Host h) {
		mHosts.add(h);
	}

	public void addLink(Link l) {
		mLinks.add(l);
	}
}
