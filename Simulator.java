//package org.timecrunch;

import java.util.ArrayList;

public class Simulator {
	protected SimScheduler mScheduler;
	protected ArrayList<Host> mHosts;
	protected ArrayList<Link> mLinks;
	protected ArrayList<Behavior> mBehaviors;

	public Simulator() {
		// null constructor for manual loading (vs xml)
		mScheduler = new SimScheduler();
		mHosts = new ArrayList<Host>();
		mLinks = new ArrayList<Link>();
		mBehaviors = new ArrayList<Behavior>();
	}

	public Simulator(String config_file) {
		mScheduler = new SimScheduler();
		mHosts = new ArrayList<Host>();
		mLinks = new ArrayList<Link>();
		mBehaviors = new ArrayList<Behavior>();

		SimLoader loader = new SimLoader();
		loader.load(config_file,this);

		registerMembersForScheduling();
	}

	public void registerMembersForScheduling() {
		for(Host h : mHosts) {
			h.registerScheduler(mScheduler);
		}

		for(Link l : mLinks) {
			l.registerScheduler(mScheduler);
		}

		for(Behavior b : mBehaviors) {
			b.registerScheduler(mScheduler);
		}
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

	public void addBehavior(Behavior b) {
		mBehaviors.add(b);
	}
}
