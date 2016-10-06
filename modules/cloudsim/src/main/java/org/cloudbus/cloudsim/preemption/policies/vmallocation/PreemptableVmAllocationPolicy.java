package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
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
public class PreemptableVmAllocationPolicy extends VmAllocationPolicy implements
		Preemptable {

	/**
	 * The map between each VM and its allocated host. The map key is a VM UID
	 * and the value is the allocated host for that VM.
	 */
	private Map<String, Host> vmTable;
	SimulationTimeUtil simulationTimeUtil;

	private HostSelectionPolicy hostSelector;
	private Map<Integer, SortedSet<PreemptiveHost>> priorityToSortedHost;

	public PreemptableVmAllocationPolicy(List<PreemptiveHost> hosts, HostSelectionPolicy hostSelector) {
		super(new ArrayList<Host>(0));
		setHostSelector(hostSelector);
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
		Host host = getVmTable().remove(vm.getUid());
		if (host == null) {
			return false;
		}
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
		for (int prioriry = 0; prioriry < gHost.getNumberOfPriorities(); prioriry++) {
			getPriorityToSortedHost().get(prioriry).add(gHost);
		}
	}

	private void removePriorityHost(Host host) {
		PreemptiveHost gHost = (PreemptiveHost) host;
		for (int prioriry = 0; prioriry < gHost.getNumberOfPriorities(); prioriry++) {
			getPriorityToSortedHost().get(prioriry).remove(gHost);
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
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
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
	
	public HostSelectionPolicy getHostSelector() {
		return hostSelector;
	}

	protected void setHostSelector(HostSelectionPolicy hostSelector) {
		this.hostSelector = hostSelector;
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

	public Host selectHost(Vm vm) {
		PreemptableVm gVm = (PreemptableVm) vm;
		if (getPriorityToSortedHost().containsKey(gVm.getPriority())) {
			PreemptiveHost host = getHostSelector().select(getPriorityToSortedHost().get(gVm.getPriority()), vm);
			if (host != null) {
				return host;
			}
		}
		return null;
	}

	public void setSimulationTimeUtil(SimulationTimeUtil simulationTimeUtil) {
		this.simulationTimeUtil = simulationTimeUtil;
	}
}

