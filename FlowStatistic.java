import java.util.ArrayList;


public class FlowStatistic {

	int mEventGroupId;
	
	int totalPackets ;
	int lostPackets ;
	int arrivePackets ;
	long totalLatency ;
	
	
	public FlowStatistic(int egid){
		mEventGroupId = egid ;
		
		totalPackets = 0  ;
		lostPackets = 0  ;
		arrivePackets = 0  ;
		totalLatency = 0  ;
	}
	
	
	public static FlowStatistic getStatistic(ArrayList<FlowStatistic> flowData, int egid){
		for(FlowStatistic s : flowData){
			if(s.mEventGroupId==egid)
				return s ;
		}
		return null;
	}
	
	
}
