package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.util.DecimalUtil;

import gnu.trove.map.hash.THashMap;

public abstract class PreemptionPolicy {

    public static final int DEFAULT_NUMBER_OF_PRIORITIES = 3;
    public static final String NUMBER_OF_PRIORITIES_PROP = "number_of_priorities";

    private Map<Integer, Double> priorityToInUseMips = new THashMap<Integer, Double>();
    private Map<Integer, SortedSet<PreemptableVm>> priorityToVms = new THashMap<Integer, SortedSet<PreemptableVm>>();
    protected SimulationTimeUtil simulationTimeUtil = new SimulationTimeUtil();
    private int numberOfPriorities = DEFAULT_NUMBER_OF_PRIORITIES;
    private double totalMips;
    private PreemptiveHost host;

    public abstract boolean isSuitableFor(PreemptableVm vm);

    public abstract Vm nextVmForPreempting();

    public abstract SortedSet<PreemptableVm> sortVms(SortedSet<PreemptableVm> sortedVms);

    public void allocating(PreemptableVm vm) {
        if (vm == null) {
            return;
        }

        getPriorityToVms().get(vm.getPriority()).add(vm);

        double priorityCurrentUse = getPriorityToInUseMips().get(vm.getPriority());
        getPriorityToInUseMips().put(vm.getPriority(),
                DecimalUtil.format(priorityCurrentUse + vm.getMips()));

    }

    public void deallocating(PreemptableVm vm) {

        if (vm == null) {
            return;
        }

        getPriorityToVms().get(vm.getPriority()).remove(vm);
        double priorityCurrentUse = getPriorityToInUseMips().get(vm.getPriority());

        getPriorityToInUseMips().put(vm.getPriority(),
                DecimalUtil.format(priorityCurrentUse - vm.getMips()));

    }

    public int getNumberOfPriorities() {
        return numberOfPriorities;
    }

    protected void setNumberOfPriorities(int numberOfPriorities) {
        this.numberOfPriorities = numberOfPriorities;
    }

    public Map<Integer, Double> getPriorityToInUseMips() {
        return priorityToInUseMips;
    }

    public void setPriorityToInUseMips(
            Map<Integer, Double> priorityToMipsInUse) {
        this.priorityToInUseMips = priorityToMipsInUse;
    }

    public Map<Integer, SortedSet<PreemptableVm>> getPriorityToVms() {
        return priorityToVms;
    }

    public void setPriorityToVms(Map<Integer, SortedSet<PreemptableVm>> priorityToVms) {
        this.priorityToVms = priorityToVms;
    }

    public double getTotalMips() {
        return totalMips;
    }

    public void setTotalMips(double totalMips) {
        this.totalMips = totalMips;
    }

    public double getAvailableMipsByPriority(int priority) {
        double inUseByNonPreemptiveVms = 0;

        for (int i = 0; i <= priority; i++) {
            inUseByNonPreemptiveVms += getPriorityToInUseMips().get(i);
        }

        return DecimalUtil.format(getTotalMips() - inUseByNonPreemptiveVms);
    }

    public abstract double getAvailableMipsByVm(PreemptableVm vm);

    public abstract double getAvailableMipsByPriorityAndAvailability(int priority);

    public abstract SortedSet<CapacityCost> getCapacityCosts(double minCPUReq, double maxCPUReq);

    public PreemptiveHost getHost() {
        return host;
    }

    public void setHost(PreemptiveHost host) {
        this.host = host;
    }
}
