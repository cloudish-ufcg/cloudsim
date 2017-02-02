package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.util.PriorityAndTTVBasedPreemptableVmComparator;

public class TTVBasedPreemptionPolicy extends VmAvailabilityBasedPreemptionPolicy {

	public TTVBasedPreemptionPolicy(Properties properties) {
		super(properties);
	}

	@Override
	public Vm nextVmForPreempting() {
		for (int i = getNumberOfPriorities() - 1; i >= 0; i--) {

			PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(
					priorityToSLOTarget, simulationTimeUtil);

			SortedSet<PreemptableVm> sortedSet = new TreeSet<PreemptableVm>(
					comparator);
			sortedSet.addAll(priorityToRunningVms.get(i).values());

			if (!sortedSet.isEmpty()) {
				return sortedSet.last();
			}
		}
		return null;
	}

}
