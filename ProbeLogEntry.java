//package org.timecrunch;

public class ProbeLogEntry {
	// CONSTANTS
	public static final int PROBE_ENTRIES_PER_HOST = 3;
	// how long before the packet a probe must arrive to be considered better
	public static final long PROBE_ARRIVAL_THRESHOLD = 0; // TODO: Should be non-zero; 0 ideal for initial testing

	// MEMBERS
	protected int mLastPacketPSN;
	protected int mLastProbePSN; // 1 SlickPacketExt can spawn multiple probes with same PSN

	public ProbeLogEntry() {
		mLastPacketPSN = 0;
		mLastProbePSN = 0;
	}

	public int getLastPacketPSN() {
		return mLastPacketPSN;
	}

	public int getLastProbePSN() {
		return mLastProbePSN;
	}

	// true if psn1 < psn2 with wraparound for non-zero psn1; assumes neither psn is 0
	private boolean psnLessThan(int psn1,int psn2) {
		// TODO: Find a better way to do this (avoid heavy branching logic!)
		if(psn2 != 0) {
			if(psn1 < 0) {
				if(psn2 < 0) {
					return (psn1 < psn2);
				} else { // mLastPacketPSN > 0
					return (psn1 > Integer.MIN_VALUE/2);
				}
			} else { // psn1 > 0 (cannot have probe with psn == 0)
				if(psn2 > 0) {
					return (psn1 < psn2);
				} else { // mLastPacketPSN > 0
					return (psn1 < Integer.MAX_VALUE/2);
				}
			}
		}

		return false;
	}

	// TODO: Add 'expectedNextPSN', if we don't get the expected, check path before PACK (helps curb spoofing)
	public boolean logProbeArrival(int psn) {
		if(psn == 0) {
			SimLogger.logError("Probe arrived with PSN = 0");
			return false;
		}

		if(mLastPacketPSN == 0 || psnLessThan(mLastPacketPSN,psn)) { // probe is newer than any packet that's arrived
			if(mLastProbePSN == 0 || psnLessThan(mLastProbePSN,psn)) { // this psn hasn't been ACK'd
				mLastProbePSN = psn;
				return true;
			}
		}

		return false;
	}

	// Returns ProbePacket for ACK if Probe-ACK required, null otherwise
	public void logPacketArrival(int psn) {
		if(psn == 0) return;

		if(mLastPacketPSN == 0) { // this is the first packet
			mLastPacketPSN = psn;
			return;
		}

		if(psnLessThan(mLastPacketPSN,psn)) { // this is our most-recent packet
			mLastPacketPSN = psn;
		}

	}
}
