package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;

public abstract class PriorityBasedVMAllocationPolicy extends PreemptableVmAllocationPolicy {

	protected Map<Integer, SortedSet<PreemptiveHost>> priorityToSortedHost;
	
	public PriorityBasedVMAllocationPolicy(List<? extends Host> hostList) {
		super(hostList);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preProcess() {
		/*
		 * There is no need for pre-processing data in this policy
		 */
	}

	@Override
	public boolean preempt(PreemptableVm vm) {
		Log.printConcatLine(simulationTimeUtil.clock(),
				": Preempting VM #", vm.getId(), " in VMAllocationPolicy.");

		Host host = vm.getHost();

		if (host == null) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": VM #", vm.getId(), " is not present in VMTable.");

			return false;
		}

		Log.printConcatLine(simulationTimeUtil.clock(),
				": VM #", vm.getId(), " is allocated in Host #", host.getId());

		vm.preempt(simulationTimeUtil.clock());

		// just to update the sorted set
		removePriorityHost(host);
		host.vmDestroy(vm);
		addPriorityHost(host);
		vm.setBeingInstantiated(true);
		return true;
	}

	protected void addPriorityHost(Host host) {
		PreemptiveHost gHost = (PreemptiveHost) host;
		for (int priority = 0; priority < gHost.getNumberOfPriorities(); priority++) {
			getPriorityToSortedHost().get(priority).add(gHost);
		}
	}

	protected void removePriorityHost(Host host) {
		PreemptiveHost gHost = (PreemptiveHost) host;
		for (int priority = 0; priority < gHost.getNumberOfPriorities(); priority++) {
			getPriorityToSortedHost().get(priority).remove(gHost);
		}
	}


	@Override
	public boolean allocateHostForVm(Vm vm) {
		Host host = selectHost(vm);
		if (host == null) {
			return false;
		}

		// just to update the sorted set
		removePriorityHost(host);
		boolean result = host.vmCreate(vm);
		addPriorityHost(host);

		return result;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host == null) {
			return false;
		}
		// just to update the sorted set
		removePriorityHost(host);
		boolean result = host.vmCreate(vm);
		addPriorityHost(host);

		return result;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {

		Host host = vm.getHost();
		if (host != null) {
			// just to update the sorted set
			removePriorityHost(host);
			host.vmDestroy(vm);
			addPriorityHost(host);
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
