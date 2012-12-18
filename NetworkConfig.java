
import java.util.ArrayList;
import java.util.HashMap;


public class NetworkConfig {
	
	private HashMap<String, Host> hostsMap;
	private HashMap<String, Link> linksMap;
	private HashMap<String, Behavior> behaviorsMap;
	private String networkId;

	NetworkConfig(String networkId){
		this.networkId = networkId;
		this.hostsMap = new HashMap<String, Host>();
		this.linksMap = new HashMap<String, Link>();
		this.behaviorsMap = new HashMap<String, Behavior>();
	}

	public HashMap<String, Host> getHostsMap() {
		return hostsMap;
	}

	public void setHostsMap(HashMap<String, Host> hostsMap) {
		this.hostsMap = hostsMap;
	}


	public HashMap<String, Link> getLinksMap() {
		return linksMap;
	}

	public void setLinksMap(HashMap<String, Link> linksMap) {
		this.linksMap = linksMap;
	}

	public HashMap<String, Behavior> getBehaviorMap() {
		return behaviorsMap;
	}

	public void setBehaviorsMap(HashMap<String, Behavior> behaviorsMap) {
		this.behaviorsMap = behaviorsMap;
	}

	public String getNetworkId() {
		return networkId;
	}
	
	public ArrayList<Host> getHostsList() {
		ArrayList<Host> list = new ArrayList<Host>(); 
		list.addAll(hostsMap.values());
		return list ;
	}
	
	public ArrayList<Link> getLinkList() {
		ArrayList<Link> list = new ArrayList<Link>(); 
		list.addAll(linksMap.values());
		return list ;
	}

	public ArrayList<Behavior> getBehaviorList() {
		ArrayList<Behavior> list = new ArrayList<Behavior>();
		list.addAll(behaviorsMap.values());
		return list;
	}

	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}
	
	public void addHost(String key, Host hostObj){
		this.hostsMap.put(key, hostObj);
		/*
		for(Host h : hostsMap.values()){
			System.out.println(h.mId);
		}
		*/
	}
	
	public void addLink(String key, Link linkObj){
		this.linksMap.put(key, linkObj);
	}

	public void addBehavior(String key, Behavior behaviorObj) {
		this.behaviorsMap.put(key,behaviorObj);
	}

}
