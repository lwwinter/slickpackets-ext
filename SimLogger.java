//package org.timecrunch;

// TODO: Add logging to file?
// TODO: Allow summarized logging (ie total delay vs current verbose)
public class SimLogger {

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
		}
	}

	// Prints to stdout
	public static void logError(String s) {
		if(GlobalSimSettings.LogErrors) {
			System.out.println("ERROR: "+s);
		}
	}

	// Prints to stdout
	public static void logEventLoss(SchedulableType type, ISchedulerSource lossPoint) {
		if(GlobalSimSettings.LogEventLoss) {
			SimScheduler sched = lossPoint.getScheduler();
			long timestamp = (sched != null) ? sched.getGlobalSimTime() : -1;
			System.out.println("EventLoss("+timestamp+"): type = " + type + " not understood by " +
					lossPoint.getId());
		}
	}
	
	// Prints to stdout
	public static void logEventArrival(Packet p, ISchedulerSource endHost) {
		if(GlobalSimSettings.LogEventArrive) {
			SimScheduler sched = endHost.getScheduler();
			long timestamp = (sched != null) ? sched.getGlobalSimTime() : -1;
			System.out.println("EventArrive("+timestamp+"): packet "+p.getPacketId()+", type="+p.getType()+", egid=" + p.eventGroupId() + " arrived at " +
					endHost.getId());
		}
	}

	// Prints to stdout
	public static void logTrace(Packet p, ISchedulerSource location) {
		if(GlobalSimSettings.LogTrace) {
			SimScheduler sched = location.getScheduler();
			long timestamp = (sched != null) ? sched.getGlobalSimTime() : -1;
			System.out.println("Trace("+timestamp+"): packet "+p.getPacketId()+", type="+p.getType()+", egid=" + p.eventGroupId() + " @ " + location.getId());
		}
	}

	// Prints to stdout
	public static void logWarning(String s) {
		if(GlobalSimSettings.LogWarnings) {
			System.out.println("WARNING: "+s);
		}
	}
}
