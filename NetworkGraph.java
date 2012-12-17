

import java.util.ArrayList;
import java.util.LinkedList;


import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.*;


public class NetworkGraph {


	private NetworkConfig networkConfig ;
	private SimpleWeightedGraph<Host,Link> graph ; 
	
	
	
	public NetworkGraph(NetworkConfig n){
		networkConfig = n ;
		graph = new SimpleWeightedGraph<Host,Link>(Link.class) ;
		buildGraph();
		//System.out.println(graph.toString());
	}
	
	private void buildGraph(){
		
		ArrayList<Host> hosts = networkConfig.getHostsList() ;
		for(Host host: hosts){
			graph.addVertex(host);
		}
		
		ArrayList<Link> links = networkConfig.getLinkList() ;
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
		for(Link l : p ){
			path.add(l);
		}
		
		return path;
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
	
}
