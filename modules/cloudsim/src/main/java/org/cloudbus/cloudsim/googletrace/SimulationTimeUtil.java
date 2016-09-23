package org.cloudbus.cloudsim.googletrace;

import org.cloudbus.cloudsim.core.CloudSim;

/**
 * This class was created only to make possible to simulate time passing in
 * automated tests
 * 
 * @author Giovanni Farias
 * 
 */
public class SimulationTimeUtil {

	public double clock() {
		return CloudSim.clock();
	}
	
    public static double getTimeInMicro(double timeInMinute) {
        return timeInMinute * 60 * 1000000;
    }
}
