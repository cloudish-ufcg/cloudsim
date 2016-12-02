package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.util.PreemptiveHostComparator;

/**
 * 
 * @author Giovanni Farias
 *
 */
public abstract class PreemptableVmAllocationPolicy extends VmAllocationPolicy {

	/**
	 * The map between each VM and its allocated host. The map key is a VM UID
	 * and the value is the allocated host for that VM.
	 */

	SimulationTimeUtil simulationTimeUtil;


	public PreemptableVmAllocationPolicy (List<Host> hostList){
		super(new ArrayList<>());
	}
	
	public abstract boolean preempt(PreemptableVm vm);

	public abstract Host selectHost(Vm vm);


	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSimulationTimeUtil(SimulationTimeUtil simulationTimeUtil) {
		this.simulationTimeUtil = simulationTimeUtil;
	}
}

