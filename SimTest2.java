//package org.timecrunch;

import java.util.ArrayList;
import java.util.LinkedList;

public class SimTest2 {

	public static void main (String[] args) {
		Simulator s = new Simulator();

		// Simple straight-line sender/receiver setup with 2 routers between
		EndHost sender = new EndHost();
		sender.setId("EndHost: sender");

		Router r1 = new Router(10000000,50); // 10 Mbps router; 50 queue size
		r1.setId("Router: r1"); 

		Router r2 = new Router(5000000,25); // 5 Mbps router; 25 queue size
		r2.setId("Router: r1");

		EndHost receiver = new EndHost();
		receiver.setId("EndHost: receiver");

		// All links: 5 Mbps, 10 ms delay
		ArrayList<Host> temp;
		temp = new ArrayList<Host>();
		temp.add(sender);
		temp.add(r1);
		SimpleLink senderToR1 = new SimpleLink(5000000,10000,temp);
		senderToR1.setId("Link: sender <-> r1");

		temp = new ArrayList<Host>();
		temp.add(r1);
		temp.add(r2);
		SimpleLink r1ToR2 = new SimpleLink(5000000,10000,temp);
		r1ToR2.setId("Link: r1 <-> r2");

		temp = new ArrayList<Host>();
		temp.add(r2);
		temp.add(receiver);
		SimpleLink r2ToReceiver = new SimpleLink(5000000,10000,temp);
		r2ToReceiver.setId("Link : r2 <-> receiver");

		// 2 behaviors
		// sender sends 1 kbit dummy packets at 10 kbps for 2 seconds
		LinkedList<Link> path = new LinkedList<Link>();
		SendAtRateBehavior b1 = new SendAtRateBehavior(path,sender,receiver,10000,1000,
				PacketType.NO_TYPE,0,2000000);
		// link between routers fails after 1 second
		LinkFailureBehavior b2 = new LinkFailureBehavior(r1ToR2,1000000);

		// Add Links to Hosts
		sender.addLink(senderToR1);
		r1.addLink(r1ToR2);
		r1.addLink(senderToR1); // for dummy packet, order is important here (forwards on Links[0])
		r2.addLink(r2ToReceiver);
		r2.addLink(r1ToR2);
		receiver.addLink(r2ToReceiver);

		// Add ISchedulerSources to Simulator
		s.addHost(sender);
		s.addHost(r1);
		s.addHost(r2);
		s.addHost(receiver);

		s.addLink(senderToR1);
		s.addLink(r1ToR2);
		s.addLink(r2ToReceiver);

		s.addBehavior(b1);
		s.addBehavior(b2);

		s.registerMembersForScheduling();

		GlobalSimSettings.LogDrops = true;
		//GlobalSimSettings.LogTrace = true ;

		s.start();
	}
}
