

import java.util.ArrayList;
import java.util.LinkedList;


import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.*;


public class NetworkGraph {


	private NetworkConfig networkConfig ;
	private SimpleWeightedGraph<Host,Link> graph ; 
	
	public NetworkGraph(ArrayList<Host> hosts, ArrayList<Link> links) {
		loadNewNetworkConfig(hosts,links);
	}
	
	public NetworkGraph(NetworkConfig n){
		loadNewNetworkConfig(n);
		//System.out.println(graph.toString());
	}

	private void buildGraph() {
		buildGraph(networkConfig.getHostsList(),networkConfig.getLinkList());
	}

	private void buildGraph(ArrayList<Host> hosts, ArrayList<Link> links){
		for(Host host: hosts){
			graph.addVertex(host);
		}

		for(Link link: links){
			Host[] ends = link.getHosts() ;
			// assume it's simple link
			// TODO : support universal link 
			if(ends.length == 2) {
				graph.addEdge(ends[0], ends[1], link);

				// TODO need implement org.jgrapht.graph.DefaultWeightedEdge for Link
				graph.setEdgeWeight(link, link.weight);
			}
		}
		
	}
	

	public LinkedList<Link> getPath(Host source , Host destination){

		ArrayList<Link> p =  (ArrayList<Link>) DijkstraShortestPath.findPathBetween(graph,source,destination);
		LinkedList<Link> path = new LinkedList<Link>();
		if(p != null) {
			for(Link l : p ){
				path.add(l);
			}
		}
		
		return path;
	}

	public ArrayList<LinkedList<Link>> computeFailovers(LinkedList<Link> path, Host source, Host dest) {
		Host curHost = source;
		ArrayList<LinkedList<Link>> failovers = new ArrayList<LinkedList<Link>>();
		LinkedList<Link> altPath;
		Host[] ends;
		for(Link linkToRemove : path) {
			// temporarily remove link from graph
			graph.removeEdge(linkToRemove);

			// compute failover path
			altPath = getPath(curHost,dest);
			if(altPath.size() == 0) {
				failovers.add(null); // easier to check for null when there is no alternate path
			} else {
				failovers.add(altPath);
			}

			// restore link to graph
			ends = linkToRemove.getHosts();
			graph.addEdge(ends[0],ends[1],linkToRemove);

			// proceed to next node in graph
			curHost = getConnectedHost(linkToRemove,curHost);
			if(curHost == null) { // should never happen since we started with a good path
				break;
			}
		}

		return failovers;
	}

	// this will get complicated for >2 host links, ie ethernet and will probably need to be changed
	private Host getConnectedHost(Link link, Host source) {
		Host[] hosts = link.getHosts();
		for(Host h : hosts) {
			if(h.getHostId() != source.getHostId()) {
				return h;
			}
		}

		return null;
	}
	
	public void loadNewNetworkConfig(ArrayList<Host> hosts, ArrayList<Link> links) {
		networkConfig = null;
		graph = new SimpleWeightedGraph<Host,Link>(Link.class);
		buildGraph(hosts,links);
	}

	public void loadNewNetworkConfig(NetworkConfig n){
		networkConfig = n ;
		graph = new SimpleWeightedGraph<Host,Link>(Link.class) ;
		buildGraph();
	}
	
	public void removeLink(Link link){
		graph.removeEdge(link);
	}
	
	public void removeHost(Host host){
		graph.removeVertex(host);
	}
	
	public void addLink(Host src, Host dst ,Link link){
		graph.addEdge(src, dst, link);
	}
	
	public void addHost(Host host){
		graph.addVertex(host);
	}

	// useful for debugging
	public static void printPath(LinkedList<Link> path) {
		if(path == null) {
			System.out.println("PATH-LINK: null");
		}

		for(Link l : path) {
			System.out.println("PATH-LINK: "+l.getId());
		}
	}

}
