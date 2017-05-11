package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.*;

import gnu.trove.map.hash.THashMap;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.CostSkin;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.comparator.capacitycost.CapacityCostComparatorByCapacity;
import org.cloudbus.cloudsim.preemption.util.DecimalUtil;

public class CapacityCostBasedPreemptionPolicy extends PreemptionPolicy {


    private static final int PROD = 0;
    private static final int BATCH = 1;
    private static final int FREE = 2;

    private static final int NOT_STARTED = -1;

    private double lastUpdate;
    private TreeSet<CapacityCost> capacityCosts;
    private SortedSet<CostSkin> costSkins;
    private THashMap<Integer, PreemptableVm> vms;

    public CapacityCostBasedPreemptionPolicy() {
        this.capacityCosts = new TreeSet<>(new CapacityCostComparatorByCapacity());
        this.costSkins = new TreeSet<>();
        this.lastUpdate = NOT_STARTED;
    }

    @Override
    public boolean isSuitableFor(PreemptableVm vm) {

        CapacityCost capacityCost = new CapacityCost(vm, getHost());
        CapacityCost suitableCapacityCost = getCapacityCosts().ceiling(capacityCost);

        return suitableCapacityCost.getCost() < calculateCost(vm);
    }

    @Override
    public Vm nextVmForPreempting() {
        return getCostSkins().last().getVm();
    }

    @Override
    public SortedSet<PreemptableVm> sortVms(SortedSet<PreemptableVm> sortedVms) {
        throw new RuntimeException("This class does not support this method.");
    }

    @Override
    public SortedSet<CapacityCost> getCapacityCosts(double minCPUReq, double maxCPUReq) {

        if (getLastUpdate() != simulationTimeUtil.clock()) {
            updateStructures(minCPUReq, maxCPUReq);
            setLastUpdate(simulationTimeUtil.clock());
        }

        return getCapacityCosts();
    }

    @Override
    public void allocating(PreemptableVm vm) {

        if (vm == null) {
            return;
        }

        getVms().put(vm.getId(), vm);

        double priorityCurrentUse = getPriorityToInUseMips().get(vm.getPriority());
        getPriorityToInUseMips().put(vm.getPriority(),
                DecimalUtil.format(priorityCurrentUse + vm.getMips()));

        updateStructures();
    }

    private void updateStructures(double minCPUReq, double maxCPUReq) {

        updateCostSkins();
        updateCapacityCosts(minCPUReq, maxCPUReq);
    }

    private void updateStructures() {

        int size = getCapacityCosts().size();

        if (size > 0) {

            double minCPUReq = getCapacityCosts().first().getCapacity();
            double maxCPUReq = getCapacityCosts().last().getCapacity();
            updateStructures(minCPUReq, maxCPUReq);
        }
    }

    private void updateCapacityCosts(double minCPUReq, double maxCPUReq) {

        getCapacityCosts().clear();

        CapacityCost capacityCost;
        double cost = 0;
        double availableMips = getAvailableMipsByPriority(FREE);


        if (checkLimits(minCPUReq, maxCPUReq, availableMips)) {
            capacityCost = new CapacityCost(availableMips, cost, getHost());
            getCapacityCosts().add(capacityCost);
        }

        Iterator iterator = ((TreeSet<CostSkin>) getCostSkins()).descendingIterator();

        while (iterator.hasNext()) {

            CostSkin costSkin = (CostSkin) iterator.next();
            PreemptableVm vm = costSkin.getVm();

            double vmMips = vm.getMips();
            availableMips += vmMips;
            cost += calculateCost(vm);

            if (checkLimits(minCPUReq, maxCPUReq, availableMips)) {
                capacityCost = new CapacityCost(availableMips, cost, getHost());
                getCapacityCosts().add(capacityCost);
            }
        }
    }

    private boolean checkLimits(double minCPUReq, double maxCPUReq, double availableMips) {
        return availableMips >= minCPUReq && availableMips <= maxCPUReq;
    }

    private void updateCostSkins() {

        getCostSkins().clear();

        for (PreemptableVm vm : getVms().values()) {
            addCostSkin(vm);
        }
    }

    @Override
    public void deallocating(PreemptableVm vm) {

        if (vm == null) {
            return;
        }

        getVms().remove(vm.getId());
        removeCostSkin(vm);

        double priorityCurrentUse = getPriorityToInUseMips().get(vm.getPriority());
        getPriorityToInUseMips().put(vm.getPriority(),
                DecimalUtil.format(priorityCurrentUse - vm.getMips()));

        updateStructures();
    }

    private void addCostSkin(PreemptableVm vm) {
        CostSkin costSkin = generateCostSkin(vm);
        getCostSkins().add(costSkin);
    }

    private void removeCostSkin(PreemptableVm vm) {
        CostSkin costSkin = generateCostSkin(vm);
        getCostSkins().remove(costSkin);
    }

    private CostSkin generateCostSkin(PreemptableVm vm) {
        double cost = calculateCost(vm);
        return new CostSkin(vm, cost);
    }

    public static double calculateCost(PreemptableVm vm) {

        if (vm == null)
            return 0;

        double cost = (1 / vm.getTTV());

        if (vm.getPriority() == PROD)
            cost = Double.MAX_VALUE;

        else if (vm.getPriority() == BATCH)
            cost += 5;

        else
            cost += 1;

        return cost;
    }

    public double getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(double lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public TreeSet<CapacityCost> getCapacityCosts() {
        return capacityCosts;
    }

    public void setCapacityCosts(TreeSet<CapacityCost> capacityCosts) {
        this.capacityCosts = capacityCosts;
    }

    public SortedSet<CostSkin> getCostSkins() {
        return costSkins;
    }

    public void setCostSkins(SortedSet<CostSkin> costSkins) {
        this.costSkins = costSkins;
    }

    public THashMap<Integer, PreemptableVm> getVms() {
        return vms;
    }

    public void setVms(THashMap<Integer, PreemptableVm> vms) {
        this.vms = vms;
    }

    @Override
    public double getAvailableMipsByVm(PreemptableVm vm) {
        throw new RuntimeException("Method is not supported by this class.");
    }

    @Override
    public double getAvailableMipsByPriorityAndAvailability(int priority) {
        throw new RuntimeException("Method is not supported by this class.");
    }
}
