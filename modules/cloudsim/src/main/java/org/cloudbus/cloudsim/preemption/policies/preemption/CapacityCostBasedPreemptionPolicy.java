package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.*;

import gnu.trove.map.hash.THashMap;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.CostSkin;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.util.DecimalUtil;

public class CapacityCostBasedPreemptionPolicy extends PreemptionPolicy {


    private static final int PROD = 0;
    private static final int BATCH = 1;
    private static final int FREE = 2;

    private static final int NOT_STARTED = -1;

    private double lastUpdate;
    private List<CapacityCost> capacityCosts;
    private SortedSet<CostSkin> costSkins;
    private THashMap<Integer, PreemptableVm> vms;

    public CapacityCostBasedPreemptionPolicy() {
        this.capacityCosts = new LinkedList<>();
        this.costSkins = new TreeSet<>();
        this.lastUpdate = NOT_STARTED;
    }

    @Override
    public boolean isSuitableFor(PreemptableVm vm) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Vm nextVmForPreempting() {
        return getCostSkins().last().getVm();
    }

    @Override
    public SortedSet<PreemptableVm> sortVms(SortedSet<PreemptableVm> sortedVms) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CapacityCost> getCapacityCosts(double minCPUReq, double maxCPUReq) {

        if (getLastUpdate() != simulationTimeUtil.clock()) {
            updateStructures(minCPUReq, maxCPUReq);
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

            double minCPUReq = getCapacityCosts().get(0).getCapacity();
            double maxCPUReq = getCapacityCosts().get(size - 1).getCapacity();
            updateStructures(minCPUReq, maxCPUReq);
        }
    }

    private void updateCapacityCosts(double minCPUReq, double maxCPUReq) {

        getCapacityCosts().clear();

        double availableMips = getAvailableMipsByPriority(FREE);

        getCapacityCosts().add(generateCapacityCost(availableMips, null));

        Iterator iterator = ((TreeSet<CostSkin>) getCostSkins()).descendingIterator();

        while (iterator.hasNext()) {

            CostSkin costSkin = (CostSkin) iterator.next();
            PreemptableVm vm = costSkin.getVm();

            double vmMips = vm.getMips();
            availableMips += vmMips;

            getCapacityCosts().add(generateCapacityCost(availableMips, vm));
        }
    }

    private CapacityCost generateCapacityCost(double availableMips, PreemptableVm vm) {
        CapacityCost capacityCost;
        capacityCost = new CapacityCost(availableMips, calculateCost(vm), getHost());
        return capacityCost;
    }

    private void updateCostSkins() {

        getCostSkins().clear();

        for (PreemptableVm vm : getVms().values()) {

            CostSkin costSkin = generateCostSkin(vm);
            getCostSkins().add(costSkin);
        }
    }

    @Override
    public void deallocating(PreemptableVm vm) {

        if (vm == null) {
            return;
        }

        getVms().remove(vm.getId());

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

    private double calculateCost(PreemptableVm vm) {

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

    public List<CapacityCost> getCapacityCosts() {
        return capacityCosts;
    }

    public void setCapacityCosts(List<CapacityCost> capacityCosts) {
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
