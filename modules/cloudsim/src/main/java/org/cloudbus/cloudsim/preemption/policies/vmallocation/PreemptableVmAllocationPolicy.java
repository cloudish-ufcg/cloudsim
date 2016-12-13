package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;

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

	public PreemptableVmAllocationPolicy (List<? extends Host> hostList){
		super(hostList);
	}
	
	public abstract void preProcess();

	public abstract boolean preempt(PreemptableVm vm);

	public abstract Host selectHost(Vm vm);
	
	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Host getHost(Vm vm) {
		return null;
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return null;
	}

	public void setSimulationTimeUtil(SimulationTimeUtil simulationTimeUtil) {
		this.simulationTimeUtil = simulationTimeUtil;
	}
}

