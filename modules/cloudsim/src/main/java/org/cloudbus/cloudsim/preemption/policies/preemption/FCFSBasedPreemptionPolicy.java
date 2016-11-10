package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.Properties;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Vm;
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
			getPriorityToVms().put(priority, new TreeSet<Vm>());
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
}
