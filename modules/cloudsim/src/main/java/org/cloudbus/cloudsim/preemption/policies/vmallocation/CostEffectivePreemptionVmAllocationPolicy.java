package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.comparator.host.HostComparatorByAvailableCapacity;

import gnu.trove.map.hash.THashMap;

public class CostEffectivePreemptionVmAllocationPolicy extends PreemptableVmAllocationPolicy {

	private SortedSet<PreemptiveHost> sortedHostsByAvailableCapacity;
	private Map<Integer, TreeSet<CapacityCost>> priorityToCapacityCosts;
	
	private double minCPUReq = 0;
	private double maxCPUReq = 0.5;
	
	public CostEffectivePreemptionVmAllocationPolicy(List<? extends Host> hostList, SimulationTimeUtil simulationTimeUtil) {
		super(hostList);
		
		/*
		 * set simulationTimeUtils
		 * init priorityToCapacityCost (first pre process ?)
		 * init sortedHostsByAvailableCapacity
		 */

		setSimulationTimeUtil(simulationTimeUtil);
		
		HostComparatorByAvailableCapacity comparatorByAvailableCapcity = new HostComparatorByAvailableCapacity();
		sortedHostsByAvailableCapacity = new TreeSet<PreemptiveHost>(comparatorByAvailableCapcity);
		sortedHostsByAvailableCapacity.addAll(getHostList());
		
		preProcess();		
	}
	
	public CostEffectivePreemptionVmAllocationPolicy(List<? extends Host> hostList) {
		this(hostList, new SimulationTimeUtil());
	}

	// This method will calculate the cost of capacities and update the
	// structure with these costs
	@Override
	public void preProcess() {
		
		priorityToCapacityCosts = new THashMap<>();

        int numberOfPriorities = ((PreemptiveHost) getHostList().get(0)).getNumberOfPriorities();

        for (int priority = 0; priority < numberOfPriorities; priority++) {

        	//TODO create the comparator of capacity costs and put into of capacity costs map
        	
//            PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator comparatorAvailabilityAware =
//                    new PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator(priority);
//
//            getPriorityToHostsAvailabilityAware().put(priority,
//                    new TreeSet<PreemptiveHost>(comparatorAvailabilityAware));

        }

		for (Host host : getHostList()) {
			PreemptiveHost pHost = (PreemptiveHost) host;

			Map<Integer, List<CapacityCost>> capacityCosts = pHost.getCapacityCosts(getMinCPUReq(), getMaxCPUReq());

			for (Integer priority : capacityCosts.keySet()) {
				getPriorityToCapacityCosts().get(priority).addAll(capacityCosts.get(priority));
			}
		}

	}

	// TODO this method may be removed from the interface. The preemption is
	// being done inside of allocate method
	@Override
	public boolean preempt(PreemptableVm vm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Host selectHost(Vm vm) {
		// TODO Implement selection of host in two stages: 1 considering only
		// the availableMips and 2 considering the cost of preemptions
		
		return null;
	}

	/*
	 * TODO These methods will be used to keep the structure updated. Maybe we
	 * need to change the signatures of them.
	 */
	@Override
	public void addHostIntoStructure(PreemptiveHost host) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeHostFromStructure(PreemptiveHost host) {
		// TODO Auto-generated method stub

	}

	public SortedSet<PreemptiveHost> getSortedHostsByAvailableCapacity() {
		return sortedHostsByAvailableCapacity;
	}

	public Map<Integer, TreeSet<CapacityCost>> getPriorityToCapacityCosts() {
		return priorityToCapacityCosts;
	}

	public double getMinCPUReq() {
		return minCPUReq;
	}

	public double getMaxCPUReq() {
		return maxCPUReq;
	}
}
