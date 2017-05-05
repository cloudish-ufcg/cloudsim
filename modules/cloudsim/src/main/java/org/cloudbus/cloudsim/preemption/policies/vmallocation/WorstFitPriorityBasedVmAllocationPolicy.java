package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.util.PreemptiveHostComparator;

public class WorstFitPriorityBasedVmAllocationPolicy extends
		PriorityBasedVMAllocationPolicy {

	public WorstFitPriorityBasedVmAllocationPolicy(List<PreemptiveHost> hosts) {
		super(hosts);
		
		if (hosts == null){
			throw new IllegalArgumentException(
					"The set of host can not be null.");
		}

		setSimulationTimeUtil(new SimulationTimeUtil());
		int numberOfPriorities = hosts.get(0).getNumberOfPriorities();

		for (int priority = 0; priority < numberOfPriorities; priority++) {
			PreemptiveHostComparator comparator = new PreemptiveHostComparator(priority);
			getPriorityToSortedHost().put(priority, new TreeSet<>(comparator));
		}

		for (PreemptiveHost host : hosts) {
			for (int priority = 0; priority < numberOfPriorities; priority++) {
				getPriorityToSortedHost().get(priority).add(host);
			}
		}
	}

	@Override
	public Host selectHost(Vm vm) {
		if (vm == null){
			throw new IllegalArgumentException("The Vm can not be null.");
		}

		PreemptableVm gVm = (PreemptableVm) vm;

		if (getPriorityToSortedHost().containsKey(gVm.getPriority())) {
			SortedSet<PreemptiveHost> hosts = getPriorityToSortedHost().get(gVm.getPriority());

			if (!hosts.isEmpty()) {
				PreemptiveHost firstHost = hosts.first();
				if (firstHost.isSuitableForVm(vm)) {
					return firstHost;
				}
			}
		}

		return null;
	}
}
