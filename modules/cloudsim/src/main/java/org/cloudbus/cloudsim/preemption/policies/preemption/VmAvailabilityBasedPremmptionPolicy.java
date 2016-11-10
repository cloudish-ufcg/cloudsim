package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;

public class VmAvailabilityBasedPremmptionPolicy extends PreemptionPolicy {

	public static final String SLO_TARGET_PREFIX_PROP = "slo_availability_target_priority_";
	
	private Map<Integer, Double> priorityToSLOTarget = new HashMap<Integer, Double>(); 
	SimulationTimeUtil simulationTimeUtil = new SimulationTimeUtil();
	
	public VmAvailabilityBasedPremmptionPolicy(Properties properties) {
		if (properties.getProperty(NUMBER_OF_PRIORITIES_PROP) != null) {
			int numberOfPriorities = Integer.parseInt(properties.getProperty(NUMBER_OF_PRIORITIES_PROP));
			if (numberOfPriorities < 1) {
				throw new IllegalArgumentException("Number of priorities must be bigger than zero.");
			}
			setNumberOfPriorities(numberOfPriorities);
		}
				
		Map<Integer, Double> sloAvailabilityTargets = getSLOAvailabilityTargets(properties);
		
		if (sloAvailabilityTargets.keySet().size() != getNumberOfPriorities()) {
			throw new IllegalArgumentException("The number of priorities and slo targets set are not in concordance.");
		}
		setPriorityToSLOTarget(sloAvailabilityTargets);
				
		// initializing maps
		for (int priority = 0; priority < getNumberOfPriorities(); priority++) {
			getPriorityToVms().put(priority, new TreeSet<Vm>());
			getPriorityToInUseMips().put(priority, new Double(0));
		}
	}
	
	public static Map<Integer, Double> getSLOAvailabilityTargets(Properties properties) {
		Map<Integer, Double> sloTargets = new HashMap<Integer, Double>();
		
		if (properties == null) {
			throw new IllegalArgumentException("The SLO availability target must be set for each priority");
		}
		
		for (Object objectKey : properties.keySet()) {
			String key = objectKey.toString();
			if (key.startsWith(SLO_TARGET_PREFIX_PROP)) {
				try {
					int priority = Integer.parseInt(key.replace(
							SLO_TARGET_PREFIX_PROP, ""));
					double sloTarget = Double.parseDouble(properties
							.getProperty(key));
					
					if (sloTarget < 0) {
						throw new IllegalArgumentException(
								"The SLO availability target must be a positive double.");
					}
					
					sloTargets.put(priority, sloTarget);
				} catch (Exception e) {
					throw new IllegalArgumentException(
							"The SLO availability target is not properly set for each priority");
				}
			}
		}
		return sloTargets;
	}

	@Override
	public boolean isSuitableFor(PreemptableVm vm) {
		if (vm == null) {
			return false;
		}
		
		double availableMipsForVmPriority = getAvailableMipsByPriority(vm.getPriority()) ;
		if (availableMipsForVmPriority >= vm.getMips()) {
			return true;
		}
		
		double mipsToBeAvilableBasedOnVmAvailability = calcMipsOfSamePriorityToBeAvailable(vm);
		return ((availableMipsForVmPriority + mipsToBeAvilableBasedOnVmAvailability) >= vm.getMips());
	}

	protected double calcMipsOfSamePriorityToBeAvailable(PreemptableVm arrivingVm) {
		double mipsToBeAvailable = 0;		
		double sloTarget = getPriorityToSLOTarget().get(arrivingVm.getPriority());
		
		// check if arriving VM isn't violating the SLO target
		double arrivingDiff = arrivingVm.getCurrentAvailability(simulationTimeUtil.clock()) - sloTarget;
		if (arrivingDiff > 0) {
			return 0;
		}
		
		for (Vm vm : getPriorityToVms().get(arrivingVm.getPriority())) {
			PreemptableVm runningVm = (PreemptableVm) vm;

			double runningDiff = runningVm.getCurrentAvailability(simulationTimeUtil.clock()) - sloTarget;
			
			if (runningDiff > arrivingDiff) {
				mipsToBeAvailable += runningVm.getMips();
			}
		}
		
		return mipsToBeAvailable;
	}

	@Override
	public Vm nextVmForPreempting() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<Integer, Double> getPriorityToSLOTarget() {
		return priorityToSLOTarget;
	}

	private void setPriorityToSLOTarget(Map<Integer, Double> priorityToSLOTarget) {
		this.priorityToSLOTarget = priorityToSLOTarget;
	}

	protected void setSimulationTimeUtil(SimulationTimeUtil simulationTimeUtil) {
		this.simulationTimeUtil = simulationTimeUtil;
	}
}
