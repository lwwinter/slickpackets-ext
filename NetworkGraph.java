

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.*;


public class NetworkGraph {


	private NetworkConfig networkConfig ;
	private SimpleWeightedGraph<Host,Link> graph ; 
	
	public NetworkGraph(NetworkConfig n){
		networkConfig = n ;
		graph = new SimpleWeightedGraph<Host,Link>(Link.class) ;
		buildGraph();
	}
	
	private void buildGraph(){
		
		Host[] hosts = (Host[]) networkConfig.getRoutersMap().values().toArray() ;
		for(Host host: hosts){
			graph.addVertex(host);
		}
		
		Link[] links = (Link[]) networkConfig.getLinksMap().values().toArray() ;
		for(Link link: links){
			Host[] ends = link.getHosts() ;
			// assume it's simple link
			// TODO : support universal link 
			graph.addEdge(ends[0], ends[1], link);
			
			// TODO set links weight
			// graph.setEdgeWeight(link, link.weight);
		}
		
	}
	

	public GraphPath<Host,Link> getPath(Host source , Host destination){
		
		@SuppressWarnings("unchecked")
		GraphPath<Host,Link> path =  (GraphPath<Host,Link>) DijkstraShortestPath.findPathBetween(graph,source,destination);
	
		
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
