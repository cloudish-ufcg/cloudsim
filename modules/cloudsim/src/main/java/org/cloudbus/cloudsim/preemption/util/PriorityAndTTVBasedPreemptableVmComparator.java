package org.cloudbus.cloudsim.preemption.util;

import java.util.Comparator;
import java.util.Map;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;

public class PriorityAndTTVBasedPreemptableVmComparator implements
		Comparator<PreemptableVm> {

	// TODO Do we really need this structure here?
	private Map<Integer, Double>  sloTargets;
	private SimulationTimeUtil simulationTimeUtil;
	
	public PriorityAndTTVBasedPreemptableVmComparator(Map<Integer, Double> sloTargets, SimulationTimeUtil simulationTimeUtil) {
		checkInput(sloTargets, simulationTimeUtil);

		this.sloTargets = sloTargets;
		this.simulationTimeUtil = simulationTimeUtil;
	}

	private void checkInput(Map<Integer, Double> sloTargets, SimulationTimeUtil simulationTimeUtil) {
		if (sloTargets == null || sloTargets.isEmpty()){
			throw new IllegalArgumentException("sloTarget map can not be null or empty");

		} else if (simulationTimeUtil == null){
			throw new IllegalArgumentException("simulationTimeUtil must not be null");
		}
	}

	@Override
	public int compare(PreemptableVm vm1, PreemptableVm vm2) {
		if (vm1.equals(vm2)) {
			return 0;
		}

		if (vm1.getPriority() < vm2.getPriority()) {
			return -1;
		} else if (vm1.getPriority() > vm2.getPriority()) {
			return 1;
		} else {
			int result = new Double(vm1.getTTV(simulationTimeUtil.clock()))
					.compareTo(new Double(vm2.getTTV(simulationTimeUtil.clock())));

			if (result == 0) {
				return new Integer(vm1.getId()).compareTo(new Integer(vm2
						.getId()));
			}

			return result;
		}
	}

	public Map<Integer, Double> getSloTargets() {
		return sloTargets;
	}

	public SimulationTimeUtil getSimulationTimeUtil() {
		return simulationTimeUtil;
	}
}
