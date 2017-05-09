package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.util.DecimalUtil;
import org.cloudbus.cloudsim.preemption.util.PriorityAndAvailabilityBasedVmComparator;
import org.cloudbus.cloudsim.preemption.util.Utils;
import org.cloudbus.cloudsim.preemption.util.VmAvailabilityBasedPreemptableVmComparator;

import gnu.trove.map.hash.THashMap;

public class VmAvailabilityBasedPreemptionPolicy extends PreemptionPolicy {

    protected Map<Integer, Double> priorityToSLOTarget = new THashMap<Integer, Double>();
    protected Map<Integer, Map<Integer, PreemptableVm>> priorityToRunningVms = new THashMap<Integer, Map<Integer, PreemptableVm>>();

    public VmAvailabilityBasedPreemptionPolicy(Properties properties) {
        this(properties, new SimulationTimeUtil());
    }

    /**
     * The constructor was created just for test purposes.
     *
     * @param properties
     * @param simulationTimeUtil
     */
    public VmAvailabilityBasedPreemptionPolicy(Properties properties, SimulationTimeUtil simulationTimeUtil) {
        if (properties.getProperty(NUMBER_OF_PRIORITIES_PROP) != null) {
            int numberOfPriorities = Integer.parseInt(properties.getProperty(NUMBER_OF_PRIORITIES_PROP));
            if (numberOfPriorities < 1) {
                throw new IllegalArgumentException("Number of priorities must be bigger than zero.");
            }
            setNumberOfPriorities(numberOfPriorities);
        }

        Map<Integer, Double> sloAvailabilityTargets = Utils.getSLOAvailabilityTargets(properties);

        if (sloAvailabilityTargets.keySet().size() != getNumberOfPriorities()) {
            throw new IllegalArgumentException("The number of priorities and slo targets set are not in concordance.");
        }
        setPriorityToSLOTarget(sloAvailabilityTargets);
        setSimulationTimeUtil(simulationTimeUtil);

        // initializing maps
        for (int priority = 0; priority < getNumberOfPriorities(); priority++) {
            priorityToRunningVms.put(priority, new THashMap<Integer, PreemptableVm>());
            getPriorityToInUseMips().put(priority, new Double(0));
        }
    }

    @Override
    public boolean isSuitableFor(PreemptableVm vm) {
        if (vm == null) {
            return false;
        }

        double availableMipsForVmPriority = getAvailableMipsByPriority(vm.getPriority());
        if (availableMipsForVmPriority >= vm.getMips()) {
            return true;
        }

        double mipsToBeAvailableBasedOnVmAvailability = calcMipsOfSamePriorityToBeAvailable(vm);
        return ((availableMipsForVmPriority + mipsToBeAvailableBasedOnVmAvailability) >= vm.getMips());
    }

    protected double calcMipsOfSamePriorityToBeAvailable(PreemptableVm arrivingVm) {
        double mipsToBeAvailable = 0;
        double sloTarget = getPriorityToSLOTarget().get(arrivingVm.getPriority());

        // check if arriving VM isn't violating the SLO target
        double arrivingDiff = arrivingVm.getCurrentAvailability(simulationTimeUtil.clock()) - sloTarget;
        if (arrivingDiff > 0) {
            return 0;
        }

        for (Vm vm : priorityToRunningVms.get(arrivingVm.getPriority()).values()) {
            PreemptableVm runningVm = (PreemptableVm) vm;

            double runningDiff = runningVm.getCurrentAvailability(simulationTimeUtil.clock()) - sloTarget;

            if (runningDiff > arrivingDiff && runningDiff > 0) {
                mipsToBeAvailable += runningVm.getMips();
            }
        }

        return mipsToBeAvailable;
    }

    @Override
    public double getAvailableMipsByPriorityAndAvailability(int priority) {

        double mipsToBeAvailable = getAvailableMipsByPriority(priority);

        double sloTarget = getPriorityToSLOTarget().get(priority);

        for (Vm vm : priorityToRunningVms.get(priority).values()) {

            PreemptableVm pVM = (PreemptableVm) vm;
            double currentAvailability = pVM.getCurrentAvailability(simulationTimeUtil.clock());

            if (currentAvailability > sloTarget)
                mipsToBeAvailable += pVM.getMips();

        }
        return mipsToBeAvailable;
    }

    @Override
    public Vm nextVmForPreempting() {
        for (int i = getNumberOfPriorities() - 1; i >= 0; i--) {

            VmAvailabilityBasedPreemptableVmComparator comparator = new VmAvailabilityBasedPreemptableVmComparator(
                    priorityToSLOTarget.get(i), simulationTimeUtil);

            SortedSet<PreemptableVm> sortedSet = new TreeSet<PreemptableVm>(comparator);
            sortedSet.addAll(priorityToRunningVms.get(i).values());

            if (!sortedSet.isEmpty()) {
                return sortedSet.last();
            }
        }
        return null;
    }

    @Override
    public void allocating(PreemptableVm vm) {
        if (vm == null) {
            return;
        }
        
        priorityToRunningVms.get(vm.getPriority()).put(vm.getId(), vm);

        double priorityCurrentUse = getPriorityToInUseMips().get(vm.getPriority());
        getPriorityToInUseMips().put(vm.getPriority(),
                DecimalUtil.format(priorityCurrentUse + vm.getMips()));

    }

    @Override
    public void deallocating(PreemptableVm vm) {

        if (vm == null) {
            return;
        }

        priorityToRunningVms.get(vm.getPriority()).remove(vm.getId());

        double priorityCurrentUse = getPriorityToInUseMips().get(vm.getPriority());
        getPriorityToInUseMips().put(vm.getPriority(),
                DecimalUtil.format(priorityCurrentUse - vm.getMips()));

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

    @Override
    public Map<Integer, SortedSet<PreemptableVm>> getPriorityToVms() {
        Map<Integer, SortedSet<PreemptableVm>> priorityToVms = new THashMap<Integer, SortedSet<PreemptableVm>>();

        for (int i = 0; i < getNumberOfPriorities(); i++) {
            VmAvailabilityBasedPreemptableVmComparator comparator = new VmAvailabilityBasedPreemptableVmComparator(
                    priorityToSLOTarget.get(i), simulationTimeUtil);

            SortedSet<PreemptableVm> sortedSet = new TreeSet<PreemptableVm>(comparator);
            sortedSet.addAll(priorityToRunningVms.get(i).values());
            priorityToVms.put(i, sortedSet);
        }

        return priorityToVms;
    }

    @Override
    public double getAvailableMipsByVm(PreemptableVm vm) {
        double availableMipsByPriority = getAvailableMipsByPriority(vm
                .getPriority());
        double mipsToBeAvilableOfSamePriority = calcMipsOfSamePriorityToBeAvailable(vm);
        return availableMipsByPriority + mipsToBeAvilableOfSamePriority;
    }

	@Override
	public SortedSet<PreemptableVm> sortVms(SortedSet<PreemptableVm> vms) {
		Log.printConcatLine(simulationTimeUtil.clock(),
				": Sorting the waiting queue based on priority + current availability.");
		
		SortedSet<PreemptableVm> sortedVms = new TreeSet<PreemptableVm>(new PriorityAndAvailabilityBasedVmComparator(simulationTimeUtil));
		sortedVms.addAll(vms);
		return sortedVms;
	}

	@Override
	public Map<Integer, List<CapacityCost>> getCapacityCosts(double minCPUReq, double maxCPUReq) {
		throw new RuntimeException("This class does not support this method.");
	}
}
