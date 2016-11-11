package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.util.DecimalUtil;

public abstract class PreemptionPolicy {
	
	public static final int DEFAULT_NUMBER_OF_PRIORITIES = 3;
	public static final int DECIMAL_ACCURACY = 9;
	
	private Map<Integer, Double> priorityToInUseMips = new HashMap<Integer, Double>();
	private Map<Integer, SortedSet<PreemptableVm>> priorityToVms = new HashMap<Integer, SortedSet<PreemptableVm>>();	
	private int numberOfPriorities = DEFAULT_NUMBER_OF_PRIORITIES;
	private double totalMips;
	protected SimulationTimeUtil simulationTimeUtil;
	public static final String NUMBER_OF_PRIORITIES_PROP = "number_of_priorities";
		
	public abstract boolean isSuitableFor(PreemptableVm vm);
	
	public abstract Vm nextVmForPreempting();
	
	public void allocating(PreemptableVm vm) {
		if (vm == null) {
			return;
		}
		
		getPriorityToVms().get(vm.getPriority()).add(vm);

		double priorityCurrentUse = getPriorityToInUseMips().get(vm.getPriority()); 
		getPriorityToInUseMips().put(vm.getPriority(),
				DecimalUtil.format(priorityCurrentUse + vm.getMips(), DECIMAL_ACCURACY));
		
	}
	
	public void deallocating(PreemptableVm vm) {
		
		if (vm == null) {
			return;
		}

		getPriorityToVms().get(vm.getPriority()).remove(vm);
		double priorityCurrentUse = getPriorityToInUseMips().get(vm.getPriority()); 
		
		getPriorityToInUseMips().put( vm.getPriority(),
				DecimalUtil.format(priorityCurrentUse - vm.getMips(), DECIMAL_ACCURACY));
		
	}
	
	public int getNumberOfPriorities() {
		return numberOfPriorities;
	}
	
	protected void setNumberOfPriorities(int numberOfPriorities) {
		this.numberOfPriorities = numberOfPriorities;
	}
	
	public Map<Integer, Double> getPriorityToInUseMips() {
		return priorityToInUseMips;
	}

	public void setPriorityToInUseMips(
			Map<Integer, Double> priorityToMipsInUse) {
		this.priorityToInUseMips = priorityToMipsInUse;
	}

	public Map<Integer, SortedSet<PreemptableVm>> getPriorityToVms() {
		return priorityToVms;
	}

	public void setPriorityToVms(Map<Integer, SortedSet<PreemptableVm>> priorityToVms) {
		this.priorityToVms = priorityToVms;
	}

	public double getTotalMips() {
		return totalMips;
	}

	public void setTotalMips(double totalMips) {
		this.totalMips = totalMips;
	}
	
	public double getAvailableMipsByPriority(int priority) {
		double inUseByNonPreemptiveVms = 0;

		for (int i = 0; i <= priority; i++) {
			inUseByNonPreemptiveVms += getPriorityToInUseMips().get(i);
		}

		return DecimalUtil.format(getTotalMips() - inUseByNonPreemptiveVms,
				DECIMAL_ACCURACY);
	}
}
