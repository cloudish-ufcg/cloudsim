package org.cloudbus.cloudsim.preemption.util;

import java.util.Comparator;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;

public class VmAvailabilityBasedPreemptableVmComparator implements
		Comparator<PreemptableVm> {
	
	private double sloTarget;
	private SimulationTimeUtil simulationTimeUtil;

	public VmAvailabilityBasedPreemptableVmComparator(double sloTarget) {
		this(sloTarget, new SimulationTimeUtil());
	}
	
	public VmAvailabilityBasedPreemptableVmComparator(double sloTarget, SimulationTimeUtil simulationTimeUtil) {
		this.sloTarget = sloTarget;
		this.simulationTimeUtil = simulationTimeUtil;
	}

	@Override
	public int compare(PreemptableVm vm1, PreemptableVm vm2) {

		if (vm1.equals(vm2)){
			return 0;
		}

		double vm1Diff = vm1.getCurrentAvailability(simulationTimeUtil.clock()) - getSloTarget();
		double vm2Diff = vm2.getCurrentAvailability(simulationTimeUtil.clock()) - getSloTarget();
		int result = new Double(vm1Diff).compareTo(new Double(vm2Diff));

        if (result == 0) {        	        	
        	int result2 = new Double(vm1.getSubmitTime()).compareTo(new Double(vm2.getSubmitTime()));
        	
        	if (result2 == 0) {
        		return new Integer(vm1.getId()).compareTo(new Integer(vm2.getId()));
        	}

			return result2;
        }

		return result;
	}
	
	protected void setSimulationTimeUtil(SimulationTimeUtil simulationTimeUtil) {
		this.simulationTimeUtil = simulationTimeUtil;
	}

	public double getSloTarget() {
		return sloTarget;
	}
}
