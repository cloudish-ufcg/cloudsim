package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Log;
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
	
	@Override
	public SortedSet<PreemptableVm> sortVms(SortedSet<PreemptableVm> vms) {
		Log.printConcatLine(simulationTimeUtil.clock(), ": Sorting the waiting queue based on priority + TTV.");

		SortedSet<PreemptableVm> sortedVms = new TreeSet<PreemptableVm>(new PriorityAndTTVBasedPreemptableVmComparator(getPriorityToSLOTarget(), simulationTimeUtil));
		sortedVms.addAll(vms);
		return sortedVms;
	}
}
