package org.cloudbus.cloudsim.preemption.util;

import java.util.Comparator;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;

public class PriorityAndAvailabilityBasedVmComparator implements
		Comparator<PreemptableVm> {

	SimulationTimeUtil simulationTimeUtil;
	
	public PriorityAndAvailabilityBasedVmComparator(SimulationTimeUtil simulationTimeUtil){
		this.simulationTimeUtil = simulationTimeUtil;
	}
	
	@Override
	public int compare(PreemptableVm vm1, PreemptableVm vm2) {
		if (vm1.getPriority() < vm2.getPriority()) {
			return -1;
		} else if (vm1.getPriority() > vm2.getPriority()) {
			return 1;
		} else if (vm1.getCurrentAvailability(simulationTimeUtil.clock()) < vm2
				.getCurrentAvailability(simulationTimeUtil.clock())) {
			return -1;
		} else if (vm1.getCurrentAvailability(simulationTimeUtil.clock()) < vm2
				.getCurrentAvailability(simulationTimeUtil.clock())) {
			return 1;
		} else if (vm1.getSubmitTime() < vm2.getSubmitTime()) {
			return -1;
		} else if (vm1.getSubmitTime() == vm2.getSubmitTime()) {
			return new Integer(vm1.getId()).compareTo(new Integer(vm2.getId()));
		}
		return 1;
	}
}
