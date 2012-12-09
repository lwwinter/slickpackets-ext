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
	public static void logDrop(Packet p, ISchedulerSource dropPoint, long timestamp) {
		if(GlobalSimSettings.LogDrops) {
			System.out.println("Drop: @ " + dropPoint.getId() +", simTime = " + timestamp);
			logDelays(p);
		}
	}

	// Prints to stdout
	public static void logEventLoss(SchedulableType type, ISchedulerSource lossPoint) {
		if(GlobalSimSettings.LogEventLoss) {
			System.out.println("EventLoss: type = " + type + " not understood by " +
					lossPoint.getId());
		}
	}
}
