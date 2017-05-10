package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.comparator.capacitycost.CapacityCostComparatorByCapacity;
import org.cloudbus.cloudsim.preemption.comparator.capacitycost.CapacityCostComparatorByCost;
import org.cloudbus.cloudsim.preemption.comparator.host.HostComparatorByAvailableCapacity;

public class CostFitVmAllocationPolicy extends PreemptableVmAllocationPolicy {

	private SortedSet<PreemptiveHost> sortedHostsByAvailableCapacity;
	private SortedSet<CapacityCost> sortedCapacityCostsByCapacity;
	
	private double minCPUReq = 0;
	private double maxCPUReq = 0.5;
	private int numberOfPriorities;
	
	public CostFitVmAllocationPolicy(List<? extends Host> hostList, SimulationTimeUtil simulationTimeUtil) {
		super(hostList);

		setSimulationTimeUtil(simulationTimeUtil);
		
		HostComparatorByAvailableCapacity comparatorByAvailableCapcity = new HostComparatorByAvailableCapacity();
		sortedHostsByAvailableCapacity = new TreeSet<PreemptiveHost>(comparatorByAvailableCapcity);
		sortedHostsByAvailableCapacity.addAll(getHostList());
		
		numberOfPriorities = ((PreemptiveHost) getHostList().get(0)).getNumberOfPriorities();
		
		CapacityCostComparatorByCapacity comparator = new CapacityCostComparatorByCapacity();
		sortedCapacityCostsByCapacity = new TreeSet<>(comparator);
		
		preProcess();		
	}
	
	public CostFitVmAllocationPolicy(List<? extends Host> hostList) {
		this(hostList, new SimulationTimeUtil());
	}

	@Override
	public void preProcess() {		
		getSortedCapacityCostsByCapacity().clear();

		for (Host host : getHostList()) {
			PreemptiveHost pHost = (PreemptiveHost) host;
			List<CapacityCost> capacityCosts = pHost.getCapacityCosts(getMinCPUReq(), getMaxCPUReq());

			getSortedCapacityCostsByCapacity().addAll(capacityCosts);
		}
	}

	@Override
	public Host selectHost(Vm vm) {
		// phase 1: selecting host considering only available capacity
		PreemptiveHost selectedHost = selectHostByAvailableCapcity(vm);
				
		if (selectedHost != null) {
			return selectedHost; 
		}
		
		// phase 2: selecting host according to lowest capacity cost
		return selectHostByCapacityCost((PreemptableVm) vm);
	}

	private Host selectHostByCapacityCost(PreemptableVm vm) {
		if (vm == null){
            throw new IllegalArgumentException("The Vm can not be null.");
        }
		
		// creating fake requested capacity
		CapacityCost requestedCapacity = new CapacityCost(vm.getMips(), 0,
				new PreemptiveHost(vm.getMips(), numberOfPriorities));

		CapacityCostComparatorByCost comparatorByCost = new CapacityCostComparatorByCost();
		SortedSet<CapacityCost> elegibleCapacities = new TreeSet<>(comparatorByCost);
		
		elegibleCapacities.addAll(getSortedCapacityCostsByCapacity().tailSet(requestedCapacity));

		if (!elegibleCapacities.isEmpty()) {
			CapacityCost lowestCapacityCost = elegibleCapacities.first();

			/*
			 * In this case, the method isSuitableForVM must check which has the
			 * lowest cost: put vm in the waiting queue or preempt some vms to
			 * allocate it?
			 */
			if (lowestCapacityCost.getHost().isSuitableForVm(vm)) {
				return lowestCapacityCost.getHost();
			}
		}

        return null;
	}

	private PreemptiveHost selectHostByAvailableCapcity(Vm vm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addHostIntoStructure(PreemptiveHost host) {
		getSortedHostsByAvailableCapacity().add(host);

		List<CapacityCost> costsToAdd = host.getCapacityCosts(getMinCPUReq(), getMaxCPUReq());
		getSortedCapacityCostsByCapacity().addAll(costsToAdd);
	}

	@Override
	public void removeHostFromStructure(PreemptiveHost host) {
		getSortedHostsByAvailableCapacity().remove(host);

		List<CapacityCost> costsToRemove = host.getCapacityCosts(getMinCPUReq(), getMaxCPUReq());
		getSortedCapacityCostsByCapacity().removeAll(costsToRemove);
	}

	public SortedSet<PreemptiveHost> getSortedHostsByAvailableCapacity() {
		return sortedHostsByAvailableCapacity;
	}

	public SortedSet<CapacityCost> getSortedCapacityCostsByCapacity() {
		return sortedCapacityCostsByCapacity;
	}

	public double getMinCPUReq() {
		return minCPUReq;
	}

	public double getMaxCPUReq() {
		return maxCPUReq;
	}
}
