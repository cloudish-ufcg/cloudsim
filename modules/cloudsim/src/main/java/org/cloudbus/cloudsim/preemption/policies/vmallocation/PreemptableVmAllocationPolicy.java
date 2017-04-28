package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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

	
	private SortedSet<PreemptableVm> vmsRunning = new TreeSet<PreemptableVm>();
	private SortedSet<PreemptableVm> vmsWaiting = new TreeSet<PreemptableVm>();

	
	protected SimulationTimeUtil simulationTimeUtil;

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
	
	public SortedSet<PreemptableVm> getVmsRunning() {
		return vmsRunning;
	}

	public boolean thereIsVmsRunning() {
		return !getVmsRunning().isEmpty();
	}

	public SortedSet<PreemptableVm> getVmsWaiting() {
		return vmsWaiting;
	}

	public boolean thereIsVmsWaiting() {
		return !getVmsWaiting().isEmpty();
	}
}

