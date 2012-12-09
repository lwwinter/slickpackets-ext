//package org.timecrunch;

public class TimeCrunch {

	public static void main (String[] args) {
		// TODO: for the moment, args[0] is our config file
		if(args.length > 0) {
			Simulator s = new Simulator(args[0]);
			s.start();
		} else {
			System.out.println("Invalid command line arguments. Expected input file.");
			System.exit(1);
		}
	}
}
