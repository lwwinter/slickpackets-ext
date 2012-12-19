import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//package org.timecrunch;

// TODO: Add logging to file?
// TODO: Allow summarized logging (ie total delay vs current verbose)
public class SimLogger {
	
	private final static SimLogger simLogger = new SimLogger();
	private final static String logFilaname = "src"+File.separator+"logFile.csv" ;
	private final static String output = "src"+File.separator+"statistic" ;
	//private final static String logFilaname = "logFile.csv" ;
	//private final static String output = "statistic" ;

	private FileWriter fileWriter ;
	private BufferedWriter writer ;
	private ArrayList<FlowStatistic> flowData;
	
	private SimLogger(){
		// create log file
		try {
			fileWriter = new FileWriter(logFilaname,false);  // not append
		} catch (IOException e) {
			e.printStackTrace();
		}
		writer = new BufferedWriter(fileWriter);
		flowData = new ArrayList<FlowStatistic>();
	}

	
	// Currently prints information to stdout
	public static void logDelays(Packet p) {
		if(GlobalSimSettings.LogDelays) {
			PacketDelays pd = p.getDelays();
			System.out.println("Delays: queueing = " + pd.getQueueingDelay() + ", link_busy = " +
					pd.getLinkBusyDelay() + ", transmission = " + pd.getTransmissionDelay() +
					", propagation = " + pd.getPropagationDelay());
		}
	}

	// Currently prints information to stdout
	public static void logDrop(Packet p, ISchedulerSource dropPoint) {
		if(GlobalSimSettings.LogDrops) {
			long timestamp = -1;
			SimScheduler sched = dropPoint.getScheduler();
			if(sched != null) {
				timestamp = sched.getGlobalSimTime();
			}

			System.out.println("Drop("+timestamp+"): @ " + dropPoint.getId());
			logDelays(p);
			writeLog("Drop, "+p.eventGroupId()+", "+dropPoint.getId()+", "+ timestamp);
			collectData(p,false);
		}
	}

	// Prints to stdout
	public static void logError(String s) {
		if(GlobalSimSettings.LogErrors) {
			System.out.println("ERROR: "+s);
			writeLog("ERROR ");
		}
	}

	// Prints to stdout
	public static void logEventLoss(SchedulableType type, ISchedulerSource lossPoint) {
		if(GlobalSimSettings.LogEventLoss) {
			SimScheduler sched = lossPoint.getScheduler();
			long timestamp = (sched != null) ? sched.getGlobalSimTime() : -1;
			System.out.println("EventLoss("+timestamp+"): type = " + type + " not understood by " +

			writeLog("EventLoss, type = " + type + ", not understood by " +
					lossPoint.getId());
		}
	}
	
	// Prints to stdout
	public static void logEventArrival(Packet p, ISchedulerSource endHost) {
		if(GlobalSimSettings.LogEventArrive) {
			long timestamp = -1;
			SimScheduler sched = endHost.getScheduler();
			if(sched != null) {
				timestamp = sched.getGlobalSimTime();
			}
			
			System.out.println("EventArrive("+timestamp+"): packet "+p.getPacketId()+", type="+p.getType()+", egid=" + p.eventGroupId() + " arrived at " +
 					endHost.getId());

			writeLog("Arrive, "+p.eventGroupId()+", "+endHost.getId()+", "+ timestamp);
			collectData(p,true);
		}
	}

	// Prints to stdout
	public static void logTrace(Packet p, ISchedulerSource location) {
		if(GlobalSimSettings.LogTrace) {
			long timestamp = -1;
			SimScheduler sched = location.getScheduler();
			if(sched != null) {
				timestamp = sched.getGlobalSimTime();
			}
			
			System.out.println("Trace("+timestamp+"): packet "+p.getPacketId()+", type="+p.getType()+", egid=" + p.eventGroupId() + " @ " + location.getId());
			writeLog("Trace, "+p.eventGroupId()+", "+location.getId()+", "+ timestamp);
		}
	}

	// Prints to stdout
	public static void logWarning(String s) {
		if(GlobalSimSettings.LogWarnings) {
			System.out.println("WARNING: "+s);
			writeLog("Warning, "+s);
		}
	}
	
	public static SimLogger getInstance(){
		return simLogger ;
	}
	
	
	// write log 
	public static void writeLog(String msg){
		try {
			simLogger.writer.write(msg+"\n") ;
			simLogger.writer.flush() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	//collect data
	public static void collectData(Packet p, boolean isArrive){

		
		FlowStatistic data = FlowStatistic.getStatistic(simLogger.flowData, p.mEventGroupId);
		if(data==null){
			data = new FlowStatistic(p.mEventGroupId);
			simLogger.flowData.add(data);
		}
		data.totalPackets++;
		
		if(isArrive){
			data.arrivePackets++;
			data.totalLatency += p.getDelays().getTotalDelay() ;
		}
		else{
			data.lostPackets++;
		}
	}
	
	public void summerize(){
		FileWriter fw = null ;  
		BufferedWriter bw;
		try {
			fw = new FileWriter(output,false);  // not append
			bw = new BufferedWriter(fw);
			
			for(FlowStatistic data : flowData){
				bw.write("Flow["+data.mEventGroupId+"]===================== \n") ;
				bw.write("total  packets: "+data.totalPackets+"\n") ;
				bw.write("arrive packets: "+data.arrivePackets+"\n") ;
				bw.write("loss   packets: "+data.lostPackets+"\n") ;
				bw.write("loss rate: "+(float)data.lostPackets/data.totalPackets+"\n\n") ;
				bw.write("total latency: "+data.totalLatency+"\n") ;
				bw.write("avg.  latency: "+(double)data.totalLatency/data.arrivePackets+"\n") ;
				bw.flush();
				//debug
				/*
				System.out.print("Flow["+data.mEventGroupId+"]===================== \n") ;
				System.out.print("total  packets: "+data.totalPackets+"\n") ;
				System.out.print("arrive packets: "+data.arrivePackets+"\n") ;
				System.out.print("loss   packets: "+data.lostPackets+"\n") ;
				System.out.print("loss rate: "+(float)data.lostPackets/data.totalPackets+"\n\n") ;
				System.out.print("total latency: "+data.totalLatency+"\n") ;
				System.out.print("avg.  latency: "+(double)data.totalLatency/data.arrivePackets+"\n") ;
				*/
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
