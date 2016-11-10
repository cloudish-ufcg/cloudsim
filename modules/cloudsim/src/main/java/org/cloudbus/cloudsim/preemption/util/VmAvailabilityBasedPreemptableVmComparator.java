package org.cloudbus.cloudsim.preemption.util;

import java.util.Comparator;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;

public class VmAvailabilityBasedPreemptableVmComparator implements
		Comparator<PreemptableVm> {
	
	private double sloTarget;
	private SimulationTimeUtil simulationTimeUtil = new SimulationTimeUtil();

	public VmAvailabilityBasedPreemptableVmComparator(double sloTarget) {
		this.sloTarget = sloTarget;
	}

	@Override
	public int compare(PreemptableVm vm1, PreemptableVm vm2) {
		double vm1Diff = vm1.getCurrentAvailability(simulationTimeUtil.clock()) - getSloTarget();
		double vm2Diff = vm2.getCurrentAvailability(simulationTimeUtil.clock()) - getSloTarget();
		int result = new Double(vm1Diff).compareTo(new Double(vm2Diff));
		
        if (result == 0)
            return new Integer(vm1.getId()).compareTo(new Integer(vm2.getId()));
        
		return result;
	}
	
	protected void setSimulationTimeUtil(SimulationTimeUtil simulationTimeUtil) {
		this.simulationTimeUtil = simulationTimeUtil;
	}

	public double getSloTarget() {
		return sloTarget;
	}
}
