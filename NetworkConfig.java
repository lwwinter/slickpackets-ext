import java.util.HashMap;


public class NetworkConfig {
	
	private HashMap<String, Router> routersMap;
	private HashMap<String, Link> linksMap;
	private String networkId;
	
	NetworkConfig(String networkId){
		this.networkId = networkId;
		this.routersMap = new HashMap<String, Router>();
		this.linksMap = new HashMap<String, Link>();
	}

	public HashMap<String, Router> getRoutersMap() {
		return routersMap;
	}

	public void setRoutersMap(HashMap<String, Router> routersMap) {
		this.routersMap = routersMap;
	}

	public HashMap<String, Link> getLinksMap() {
		return linksMap;
	}

	public void setLinksMap(HashMap<String, Link> linksMap) {
		this.linksMap = linksMap;
	}

	public String getNetworkId() {
		return networkId;
	}

	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}
	
	public void addRouter(String key, Router routerObj){
		this.routersMap.put(key, routerObj);
	}
	
	public void addLink(String key, Link linkObj){
		this.linksMap.put(key, linkObj);
	}

}
