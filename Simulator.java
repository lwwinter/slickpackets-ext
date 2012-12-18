//package org.timecrunch;

import java.util.ArrayList;

public class Simulator {
	protected SimScheduler mScheduler;
	protected ArrayList<Host> mHosts;
	protected ArrayList<Link> mLinks;
	protected ArrayList<Behavior> mBehaviors;
	protected NetworkConfig mConfig;
	protected NetworkGraph mGraph;

	public Simulator() {
		// null constructor for manual loading (vs xml)
		mScheduler = new SimScheduler();
		mScheduler.registerSimulator(this);
		mHosts = new ArrayList<Host>();
		mLinks = new ArrayList<Link>();
		mBehaviors = new ArrayList<Behavior>();
	}

	public Simulator(String config_file) {
		mScheduler = new SimScheduler();
		mScheduler.registerSimulator(this);
		mHosts = new ArrayList<Host>();
		mLinks = new ArrayList<Link>();
		mBehaviors = new ArrayList<Behavior>();
		loadNetworkConfig(config_file);
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

	public void loadNetworkConfig() {
		SlickXMLParser loader = new SlickXMLParser();
		finishNetworkConfig(loader);
	}

	public void loadNetworkConfig(String config_file) {
		SlickXMLParser loader = new SlickXMLParser(config_file);
		finishNetworkConfig(loader);
	}

	private void finishNetworkConfig(SlickXMLParser loader) {
		mConfig = loader.getNetworkConfig();
		mGraph = new NetworkGraph(mConfig);
		for(Host h : mConfig.getHostsList()){
			addHost(h);
		}
		for(Link l : mConfig.getLinkList()){
			addLink(l);
		}
		for(Behavior b : mConfig.getBehaviorList()) {
			addBehavior(b);
		}
	}

	public NetworkConfig getNetworkConfig() {
		return mConfig;
	}

	public NetworkGraph getNetworkGraph() {
		return mGraph;
	}
}
