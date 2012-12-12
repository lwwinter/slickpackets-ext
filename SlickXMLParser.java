
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
	public static void main(String[] args) {
		try {
			File f = new File("src\\slick-config.xml");
			//File f = new File("slick-config.xml");
			String path = f.getAbsolutePath();
			InputStream in = new FileInputStream(path);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(in);
			NetworkConfig networkConfig = new NetworkConfig("DEFAULT_NETWORK");
			while (parser.hasNext()) {

		        printEventInfo(parser, networkConfig);

		    }
		    parser.close();
		    
		    NetworkGraph networkGraph = new NetworkGraph(networkConfig);
		    ArrayList<Link> p= networkGraph.getPath(networkConfig.getHostsMap().get("END1"), networkConfig.getHostsMap().get("END2")) ;
		    for(Link l :p){
		    	System.out.print(l.mId+"=>");
		    	
		    }
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	private static void printEventInfo(XMLStreamReader reader, NetworkConfig networkConfig) throws Exception {
		int eventCode = reader.next();
		Router router = null;
		EndHost endHost = null ;
		SimpleLink link = null;
	    switch (eventCode) {
	    	case XMLStreamConstants.START_ELEMENT :
	    		//System.out.println("event = START_ELEMENT");
	    		System.out.println("Localname = "+reader.getLocalName());
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
	    			}
	    			else if ("endhost".equalsIgnoreCase(reader.getAttributeValue(null, "type"))){
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
	
}
