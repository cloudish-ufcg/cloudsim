package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.util.PriorityAndTTVBasedPreemptableVmComparator;
import org.cloudbus.cloudsim.preemption.util.VmAvailabilityBasedPreemptableVmComparator;

public class TTVBasedPreemptionPolicy extends VmAvailabilityBasedPreemptionPolicy {

	public TTVBasedPreemptionPolicy(Properties properties) {
		super(properties);
		for (int priority = 0; priority < getNumberOfPriorities(); priority++) {

			VmAvailabilityBasedPreemptableVmComparator comparator = new VmAvailabilityBasedPreemptableVmComparator(
					getPriorityToSLOTarget().get(priority), simulationTimeUtil);
			priorityToVms.put(priority, new TreeSet<PreemptableVm>(comparator));
		}
	}

	@Override
	protected void refreshPriorityToVms() {

		if (getLastTimeSortedVms() != simulationTimeUtil.clock()) {

			for (int i = getNumberOfPriorities() - 1; i >= 0; i--) {

				PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(
						priorityToSLOTarget, simulationTimeUtil);

				SortedSet<PreemptableVm> sortedSet = new TreeSet<PreemptableVm>(
						comparator);
				sortedSet.addAll(priorityToRunningVms.get(i).values());

				priorityToVms.put(i, sortedSet);
			}
			setLastTimeSortedVms(simulationTimeUtil.clock());
		}
	}
}
