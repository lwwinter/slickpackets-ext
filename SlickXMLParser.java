
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;



public class SlickXMLParser {

	/**
	 * @param args
	 */
    // File.separator used for platform independence, '/' supposed to work cross-platform too
	private final static String defaultPath = "src"+File.separator+"slick-config.xml" ;
	private NetworkConfig networkConfig ;
	
	public SlickXMLParser() {
		this(defaultPath);
	}
	
	public SlickXMLParser(String xmlPath) {
		//System.out.println("Loading "+xmlPath); // DEBUG
		try {
			File f = new File(xmlPath);
			String path = f.getAbsolutePath();
			InputStream in = new FileInputStream(path);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(in);
			networkConfig = new NetworkConfig("DEFAULT_NETWORK");
			while (parser.hasNext()) {
		        printEventInfo(parser, networkConfig);
		    }
		    parser.close();
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	// TODO: Implement default values (can use public static final values straight from classes)
	private void printEventInfo(XMLStreamReader reader, NetworkConfig networkConfig) throws Exception {
		int eventCode = reader.next();
		Router router = null;
		EndHost endHost = null ;
		SimpleLink link = null;
	    switch (eventCode) {
	    	case XMLStreamConstants.START_ELEMENT :
	    		//System.out.println("event = START_ELEMENT");
	    		//System.out.println("Localname = "+reader.getLocalName());
	    		if(reader.getLocalName().equalsIgnoreCase("network")){
	    			String netId = reader.getAttributeValue(null, "id");
	    			networkConfig.setNetworkId(netId);
	    		}else if(reader.getLocalName().equalsIgnoreCase("host")){
	    			if("router".equalsIgnoreCase(reader.getAttributeValue(null, "type"))){
	    				long throughput = Long.parseLong(reader.getAttributeValue(null, "throughput"));
		    			int qsize = Integer.parseInt(reader.getAttributeValue(null, "queue-size"));
		    			router = new Router(throughput, qsize, new ArrayList<Link>());
		    			router.setId(reader.getAttributeValue(null, "id"));
		    			networkConfig.addHost(router.getId(),(Host) router);
	    			}else if("endhost".equalsIgnoreCase(reader.getAttributeValue(null, "type"))){
	    				endHost = new EndHost(new ArrayList<Link>());
	    				endHost.setId(reader.getAttributeValue(null, "id"));
	    				networkConfig.addHost(endHost.getId(),(Host) endHost) ;
	    			}
	    			
	    		}else if(reader.getLocalName().equalsIgnoreCase("link")){
	    			if("SimpleLink".equalsIgnoreCase(reader.getAttributeValue(null, "type"))){
	    				long bandwidth = Long.parseLong(reader.getAttributeValue(null, "bandwidth"));
	    				long latency = Long.parseLong(reader.getAttributeValue(null, "latency"));
	    				String fromHost = reader.getAttributeValue(null, "from");
	    				String toHost = reader.getAttributeValue(null, "to");
	    				Host hostFrom =  networkConfig.getHostsMap().get(fromHost);
	    				Host hostTo = networkConfig.getHostsMap().get(toHost);
	    				ArrayList<Host> mHosts = new  ArrayList<Host>();
	    				mHosts.add(hostTo);
	    				mHosts.add(hostFrom);
	    				link = new SimpleLink(bandwidth, latency, mHosts);
	    				link.setId(reader.getAttributeValue(null, "id"));
	    				networkConfig.addLink(link.getId(), link);
	    				hostFrom.mLinks.add(link);
	    				hostTo.mLinks.add(link);
	    			}
	    		}else if(reader.getLocalName().equalsIgnoreCase("behavior")){
					if("SendAtRate".equalsIgnoreCase(reader.getAttributeValue(null, "type"))){
						String fromHost = reader.getAttributeValue(null, "from");
	    				String toHost = reader.getAttributeValue(null, "to");
						Host hostFrom =  networkConfig.getHostsMap().get(fromHost);
	    				Host hostTo = networkConfig.getHostsMap().get(toHost);
						int sendrate = Integer.parseInt(reader.getAttributeValue(null, "rate"));
		    			int psize = Integer.parseInt(reader.getAttributeValue(null, "packet-size"));
						String type = reader.getAttributeValue(null,"packet-type");
						PacketType ptype;
						if(type != null) {	
							if(type.equalsIgnoreCase("none")) {
								ptype = PacketType.NO_TYPE;
							} else if(type.equalsIgnoreCase("source-routed")) {
								ptype = PacketType.SOURCE_ROUTED;
							} else {
								if(GlobalSimSettings.LogWarnings) {
									SimLogger.logWarning("Invalid packet type for SendAtRateBehavior");
								}
								ptype = SendAtRateBehavior.DEFAULT_PACKET_TYPE;
							}
						} else {
							ptype = SendAtRateBehavior.DEFAULT_PACKET_TYPE;
						}
						long start = Long.parseLong(reader.getAttributeValue(null, "start"));
		    			long duration = Long.parseLong(reader.getAttributeValue(null, "duration"));
		    			SendAtRateBehavior sarb = new SendAtRateBehavior(hostFrom, hostTo, sendrate,
								psize, ptype, start, duration);
		    			sarb.setId(reader.getAttributeValue(null, "id"));
		    			networkConfig.addBehavior(sarb.getId(),(Behavior)sarb);
					}else if("LinkFailure".equalsIgnoreCase(reader.getAttributeValue(null,"type"))) {
						String targetLink = reader.getAttributeValue(null, "link");
						Link tlink = networkConfig.getLinksMap().get(targetLink);
						long start = Long.parseLong(reader.getAttributeValue(null, "start"));
						LinkFailureBehavior lfb = new LinkFailureBehavior(tlink,start);
						lfb.setId(reader.getAttributeValue(null, "id"));
		    			networkConfig.addBehavior(lfb.getId(),(Behavior)lfb);
					}
				}
	    		break;

	    	/*case XMLStreamConstants.END_ELEMENT :
	    		System.out.println("event = END_ELEMENT");
	    		System.out.println("Localname = "+reader.getLocalName());
	    		break;

	    	case XMLStreamConstants.ATTRIBUTE :
	    		System.out.println("event = PROCESSING_INSTRUCTION");
	    		System.out.println("PIData = " + reader.getAttributeLocalName(0));
	    		break;*/
	    }
	}
	
	public NetworkConfig getNetworkConfig(){
		return networkConfig;
	}
	
}
