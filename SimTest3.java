//package org.timecrunch;

import java.util.LinkedList;

public class SimTest3 {

	public static void main (String[] args) {
		Simulator s = new Simulator();
		
		// read from slick-config.xml
		// read XML, create configuration, and create graph
		SlickXMLParser parser = new SlickXMLParser();
		NetworkConfig networkConfig = parser.getNetworkConfig() ;
	    NetworkGraph networkGraph = new NetworkGraph(networkConfig);

	    // get a path form source to destination
	    Host sender = networkConfig.getHostsMap().get("SRC") ;
	    Host receiver = networkConfig.getHostsMap().get("DST") ;
	    LinkedList<Link> path = networkGraph.getPath(sender, receiver) ;  
	    
		// 2 behaviors
		// sender sends 1 kbit dummy packets at 10 kbps for 2 seconds
		SendAtRateBehavior b1 = new SendAtRateBehavior(path, sender,receiver,10000,1000,
				PacketType.SOURCE_ROUTED,0,2000000);
		// link between routers fails after 1 second
		Link failLink = networkConfig.getLinksMap().get("LNK2");
		LinkFailureBehavior b2 = new LinkFailureBehavior(failLink,1000000);

		// Add Links to Hosts, and add hosts to link
		// done in NetworkConfig

		// Add ISchedulerSources to Simulator
		for(Host h : networkConfig.getHostsList()){
			s.addHost(h);
		}
		for(Link l : networkConfig.getLinkList()){
			s.addLink(l);
		}

		s.addBehavior(b1);
		s.addBehavior(b2);

		s.registerMembersForScheduling();

		GlobalSimSettings.LogDrops = true;
		//GlobalSimSettings.LogTrace = true ;

		s.start();
	}
}
