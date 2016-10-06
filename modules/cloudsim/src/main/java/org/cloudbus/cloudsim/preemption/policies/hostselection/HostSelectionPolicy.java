package org.cloudbus.cloudsim.preemption.policies.hostselection;

import java.util.SortedSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;

/**
 * TODO 
 * 
 * @author Giovanni Farias
 *
 */
public interface HostSelectionPolicy {

	public PreemptiveHost select(SortedSet<PreemptiveHost> hosts, Vm vm);
	
}
