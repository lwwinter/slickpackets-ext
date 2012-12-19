/* Direct clone of src/slick-packets-ext1.xml functionality */

//package org.timecrunch;

import java.io.File;
import java.util.LinkedList;

public class SPExtTest {

	public static final String CONFIG_FILE = "src"+File.separator+"spext-config.xml";

	public static void main (String[] args) {
		Simulator s = new Simulator();
		
		// read from spext-config.xml
		// read XML, create configuration, and create graph
		s.loadNetworkConfig(CONFIG_FILE);
		NetworkConfig networkConfig = s.getNetworkConfig();
		NetworkGraph networkGraph = s.getNetworkGraph();

	    // get a path form source to destination
	    Host sender = networkConfig.getHostsMap().get("SRC") ;
	    Host receiver = networkConfig.getHostsMap().get("DST") ; 
	    
		// 2 behaviors
		// sender sends 1 kbit dummy packets at 10 kbps for 2 seconds
		SendAtRateBehavior b1 = new SendAtRateBehavior(sender,receiver,10000,1000,
				PacketType.SLICK_PACKET_EXT,0,2000000);

		// link between routers fails after 1 second
		Link failLink = networkConfig.getLinksMap().get("LNK2");
		LinkFailureBehavior b2 = new LinkFailureBehavior(failLink,1000000);

		s.addBehavior(b1);
		s.addBehavior(b2);

		s.registerMembersForScheduling();

		GlobalSimSettings.LogDrops = true;
		GlobalSimSettings.LogEventArrive = true;
		GlobalSimSettings.LogTrace = true ;
		GlobalSimSettings.LogDelays = true ;
		
		s.start();
		
		SimLogger.getInstance().summerize();
	}
}
