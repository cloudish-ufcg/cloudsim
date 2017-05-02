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
		PreemptiveHost pHost = (PreemptiveHost) host;
		PreemptableVm pVm = (PreemptableVm) vm;
		removePriorityHost(pHost);
		boolean result = pHost.vmCreate(pVm);

		if (!result) {
			while (!result) {
				PreemptableVm vmToPreempt = (PreemptableVm) pHost.nextVmForPreempting();
				
				// TODO do we nedd this check?
				if (vmToPreempt != null && vmToPreempt.getPriority() >= pVm.getPriority()) {
					Log.printConcatLine(simulationTimeUtil.clock(),
							": Preempting VM #" + vmToPreempt.getId() + " (priority " + vmToPreempt.getPriority()
									+ ") to allocate VM #" + vm.getId() + " (priority " + pVm.getPriority() + ")");
					
					// preempting vm
					vmToPreempt.preempt(simulationTimeUtil.clock());
					pHost.vmDestroy(vmToPreempt);					
					vmToPreempt.setBeingInstantiated(true);
					
					getVmsRunning().remove(vmToPreempt);
					getVmsWaiting().add(vmToPreempt);
					
					result = pHost.vmCreate(pVm);					
				}
			}
		}

		// updating allocation structures
		getVmsRunning().add(pVm);
		getVmsWaiting().remove(pVm);

		pVm.setStartExec(simulationTimeUtil.clock());
		pVm.allocatingToHost(host.getId());
		
		if (pVm.isBeingInstantiated()) {
			pVm.setBeingInstantiated(false);
		}
		
		Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
				vm.getId(), " was allocated on host #", host.getId(),
				" successfully.");

		addPriorityHost(host);
		return result;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = vm.getHost();

		if (host != null) {		
			if (!getVmsRunning().remove(vm)) {
				Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
						vm.getId(), " is not running to be removed.");
			}
			
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
