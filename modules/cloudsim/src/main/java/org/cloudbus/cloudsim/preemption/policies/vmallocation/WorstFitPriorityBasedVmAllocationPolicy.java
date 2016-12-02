package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.*;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.util.PreemptiveHostComparator;

public class WorstFitPriorityBasedVmAllocationPolicy extends
		PreemptableVmAllocationPolicy {

	private Map<String, Host> vmTable;

	private Map<Integer, SortedSet<PreemptiveHost>> priorityToSortedHost;

	public WorstFitPriorityBasedVmAllocationPolicy(List<PreemptiveHost> hosts) {
		super(new ArrayList<Host>(0));

		if (hosts == null){
			throw new IllegalArgumentException(
					"The set of host can not be null.");
		}

		setSimulationTimeUtil(new SimulationTimeUtil());
		priorityToSortedHost = new HashMap<Integer, SortedSet<PreemptiveHost>>();
		int numberOfPriorities = hosts.get(0).getNumberOfPriorities();

		for (int priority = 0; priority < numberOfPriorities; priority++) {

			PreemptiveHostComparator comparator = new PreemptiveHostComparator(priority);
			getPriorityToSortedHost().put(priority, new TreeSet<PreemptiveHost>(comparator));

		}

		// creating priority host skins
		for (PreemptiveHost host : hosts) {
			for (int priority = 0; priority < numberOfPriorities; priority++) {
				getPriorityToSortedHost().get(priority).add(host);
			}
		}

		setVmTable(new HashMap<String, Host>());
	}

	@Override
	public boolean preempt(PreemptableVm vm) {
		Log.printConcatLine(simulationTimeUtil.clock(),
				": Preempting VM #", vm.getId(), " in VMAllocationPolicy.");

		Host host = getVmTable().remove(vm.getUid());

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

	private void addPriorityHost(Host host) {
		PreemptiveHost gHost = (PreemptiveHost) host;
		for (int priority = 0; priority < gHost.getNumberOfPriorities(); priority++) {
			getPriorityToSortedHost().get(priority).add(gHost);
		}
	}

	private void removePriorityHost(Host host) {
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

		if (result) {
			getVmTable().put(vm.getUid(), host);
		}
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

		if (result) {
			getVmTable().put(vm.getUid(), host);
		}
		return result;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		if (host != null) {
			// just to update the sorted set
			removePriorityHost(host);
			host.vmDestroy(vm);
			addPriorityHost(host);
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

	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	@Override
	public List<Host> getHostList() {
		List<Host> hostList = new ArrayList<Host>(getPriorityToSortedHost().get(0));
		return hostList;
	}

	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

	public Map<Integer, SortedSet<PreemptiveHost>> getPriorityToSortedHost() {
		return priorityToSortedHost;
	}

	public void setPriorityToSortedHost(
			Map<Integer, SortedSet<PreemptiveHost>> priorityToSortedHost) {
		this.priorityToSortedHost = priorityToSortedHost;
	}






}
