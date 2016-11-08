package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.util.DecimalUtil;

public abstract class PreemptionPolicy {
	
	public static final int DEFAULT_NUMBER_OF_PRIORITIES = 3;
	public static final int DECIMAL_ACCURACY = 9;
	
	private Map<Integer, Double> priorityToInUseMips = new HashMap<Integer, Double>();
	private Map<Integer, SortedSet<Vm>> priorityToVms = new HashMap<Integer, SortedSet<Vm>>();	
	private int numberOfPriorities = DEFAULT_NUMBER_OF_PRIORITIES;
	private double totalMips;
		
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

	public Map<Integer, SortedSet<Vm>> getPriorityToVms() {
		return priorityToVms;
	}

	public void setPriorityToVms(Map<Integer, SortedSet<Vm>> priorityToVms) {
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
