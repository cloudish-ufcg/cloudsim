package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;

import gnu.trove.map.hash.THashMap;

public abstract class PriorityBasedVMAllocationPolicy extends PreemptableVmAllocationPolicy {

	protected Map<Integer, SortedSet<PreemptiveHost>> priorityToSortedHost = new THashMap<>();
	
	public PriorityBasedVMAllocationPolicy(List<PreemptiveHost> hosts) {
		super(hosts);
	}

	@Override
	public void preProcess() {
		/*
		 * There is no need for pre-processing data in this policy
		 */
	}

	@Override
	public void addHostIntoStructure(PreemptiveHost host) {	    
		for (int priority = 0; priority < host.getNumberOfPriorities(); priority++) {
			getPriorityToSortedHost().get(priority).add(host);
		}		
	}

	@Override
	public void removeHostFromStructure(PreemptiveHost host) {
		for (int priority = 0; priority < host.getNumberOfPriorities(); priority++) {
			getPriorityToSortedHost().get(priority).remove(host);
		}		
	}

	@Override
	public List<Host> getHostList() {
		List<Host> hostList = new ArrayList<Host>(getPriorityToSortedHost().get(0));
		return hostList;
	}

	public Map<Integer, SortedSet<PreemptiveHost>> getPriorityToSortedHost() {
		return priorityToSortedHost;
	}
}
