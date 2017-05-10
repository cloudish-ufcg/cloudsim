package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.PreemptableVm;

public class FCFSBasedPreemptionPolicy extends PreemptionPolicy {

	public FCFSBasedPreemptionPolicy(Properties properties) {		
		if (properties.getProperty(NUMBER_OF_PRIORITIES_PROP) != null) {
			int numberOfPriorities = Integer.parseInt(properties.getProperty(NUMBER_OF_PRIORITIES_PROP));
			if (numberOfPriorities < 1) {
				throw new IllegalArgumentException("Number of priorities must be bigger than zero.");
			}
			setNumberOfPriorities(numberOfPriorities);
		}
		
		// initializing maps
		for (int priority = 0; priority < getNumberOfPriorities(); priority++) {
			getPriorityToVms().put(priority, new TreeSet<PreemptableVm>());
			getPriorityToInUseMips().put(priority, new Double(0));
		}
	}

	@Override
	public boolean isSuitableFor(PreemptableVm vm) {
		if (vm == null) {
			return false;
		}
		double availableMips = getAvailableMipsByPriority(vm.getPriority()) ;
		return (availableMips >= vm.getMips());
	}

	@Override
	public Vm nextVmForPreempting() {
		for (int i = getNumberOfPriorities() - 1; i >= 0; i--) {

			if (!getPriorityToVms().get(i).isEmpty()) {
				return getPriorityToVms().get(i).last();
			}
		}
		return null;
	}

	@Override
	public double getAvailableMipsByPriorityAndAvailability(int priority) {
		return getAvailableMipsByPriority(priority);
	}

	@Override
	public double getAvailableMipsByVm(PreemptableVm vm) {
		return getAvailableMipsByPriority(vm.getPriority());
	}
	
	@Override
	public SortedSet<PreemptableVm> sortVms(SortedSet<PreemptableVm> vms) {
		Log.printConcatLine(simulationTimeUtil.clock(), ": The waiting queue is sorted based on priority + FCFS. This is the default order");
		// The default order is based on FCFS policy
		return new TreeSet<PreemptableVm>(vms);
	}

	@Override
	public List<CapacityCost> getCapacityCosts(double minCPUReq, double maxCPUReq) {
		throw new RuntimeException("This class does not support this method.");
	}
}
