package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.*;

import gnu.trove.map.hash.THashMap;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.PreemptiveHostComparator;
import org.cloudbus.cloudsim.preemption.util.PriorityAndAvailabilityBasedPreemptiveHostComparator;

/**
 * Created by Alessandro Lia Fook Santos on 02/12/16.
 */
public class WorstFitAvailabilityAwareVmAllocationPolicy extends PreemptableVmAllocationPolicy {

    private Map<Integer, PriorityQueue<PreemptiveHost>> priorityToSortedHostFCFS;
    private Map<Integer, PriorityQueue<PreemptiveHost>> priorityToSortedHostAvailabilityAware;
    private Map<Integer, Double> priorityToSLOTarget = new THashMap<Integer, Double>();

    public WorstFitAvailabilityAwareVmAllocationPolicy(List<PreemptiveHost> hostList) {
        super(hostList);
        setSimulationTimeUtil(new SimulationTimeUtil());

        verifyHosts(hostList);

        priorityToSortedHostFCFS = new THashMap<>();
        priorityToSortedHostAvailabilityAware = new THashMap<>();

        int numberOfPriorities = hostList.get(0).getNumberOfPriorities();

        for (int priority = 0; priority < numberOfPriorities; priority++) {

            PreemptiveHostComparator comparatorFCFS = new PreemptiveHostComparator(priority);
            PriorityAndAvailabilityBasedPreemptiveHostComparator comparatorAvailabilityAware = new PriorityAndAvailabilityBasedPreemptiveHostComparator(priority);

            getPriorityToSortedHostFCFS().put(priority, new PriorityQueue(comparatorFCFS));
            getPriorityToSortedHostAvailabilityAware().put(priority, new PriorityQueue(comparatorAvailabilityAware));
        }

        for (PreemptiveHost host : hostList) {
            for (int priority = 0; priority < numberOfPriorities; priority++) {
                getPriorityToSortedHostFCFS().get(priority).add(host);
                getPriorityToSortedHostAvailabilityAware().get(priority).add(host);
            }
        }

        setPriorityToSLOTarget(hostList);


    }

    public void preProcess() {

        priorityToSortedHostAvailabilityAware = new THashMap<>();

        int numberOfPriorities = ((PreemptiveHost) getHostList().get(0)).getNumberOfPriorities();

        for (int priority = 0; priority < numberOfPriorities; priority++) {

            PriorityAndAvailabilityBasedPreemptiveHostComparator comparatorAvailabilityAware = new PriorityAndAvailabilityBasedPreemptiveHostComparator(priority);
            getPriorityToSortedHostAvailabilityAware().put(priority, new PriorityQueue(comparatorAvailabilityAware));

        }

        for (Host host : getHostList()) {
            PreemptiveHost pHost = (PreemptiveHost) host;
            for (int priority = 0; priority < numberOfPriorities; priority++) {
                getPriorityToSortedHostAvailabilityAware().get(priority).add(pHost);
            }
        }

    }

    private void verifyHosts(List<PreemptiveHost> hostList) {
        if (hostList == null) {
            throw new IllegalArgumentException(
                    "The set of host can not be null.");
        } else if (hostList.isEmpty()) {
            throw new IllegalArgumentException(
                    "The set of host can not be empty.");
        }
    }

    @Override
    public boolean preempt(PreemptableVm vm) {

        verifyVm(vm);
        Host host = vm.getHost();

        if (!validateHostForVm(vm, host))
            return false;

        vm.preempt(simulationTimeUtil.clock());
        removePriorityHost(host);
        host.vmDestroy(vm);
        addPriorityHost(host);
        vm.setBeingInstantiated(true);
        return true;
    }

    protected void addPriorityHost(Host host) {
        PreemptiveHost gHost = (PreemptiveHost) host;
        for (int priority = 0; priority < gHost.getNumberOfPriorities(); priority++) {
            getPriorityToSortedHostFCFS().get(priority).add(gHost);
            getPriorityToSortedHostAvailabilityAware().get(priority).add(gHost);
        }
    }

    protected void removePriorityHost(Host host) {
        PreemptiveHost gHost = (PreemptiveHost) host;
        for (int priority = 0; priority < gHost.getNumberOfPriorities(); priority++) {
            getPriorityToSortedHostFCFS().get(priority).remove(gHost);
            getPriorityToSortedHostAvailabilityAware().get(priority).remove(gHost);
        }
    }

    private boolean validateHostForVm(PreemptableVm vm, Host host) {

        if (host == null) {
            Log.printConcatLine(simulationTimeUtil.clock(),
                    ": VM #", vm.getId(), " do not have a host.");

            return false;
        }
        return true;
    }

    @Override
    public Host selectHost(Vm vm) {

        verifyVm(vm);

        if (!getHostList().isEmpty()) {
            PreemptableVm pVm = (PreemptableVm) vm;
            PreemptiveHost firstHost;
            /*
            @TODO Decide if the vm has to be violating SLO in this time
            @TODO or in the next time to choose the way of select the host.
            */
            if (pVm.getCurrentAvailability(simulationTimeUtil.clock()) > getSLOTarget(pVm.getPriority())) {
                firstHost = getPriorityToSortedHostFCFS().get(pVm.getPriority()).peek();

            } else {
                firstHost = getPriorityToSortedHostAvailabilityAware().get(pVm.getPriority()).peek();
            }

            if (firstHost.isSuitableForVm(pVm)) {
                return firstHost;
            }
        }
        return null;
    }

    private double getSLOTarget(int priority) {
        return priorityToSLOTarget.get(priority);
    }

    private void verifyVm(Vm vm) {
        if (vm == null)
            throw new IllegalArgumentException("The Vm can not be null.");
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        throw new RuntimeException("Do not support this method in " +
                "WorstFitAvailabilityAwareAllocationPolicy");

    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        verifyVm(vm);
        if (!validateHostForVm((PreemptableVm) vm, host)) {
            return false;
        }

        removePriorityHost(host);
        boolean result = host.vmCreate(vm);
        addPriorityHost(host);

        return result;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        verifyVm(vm);

        Host host = vm.getHost();

        if (validateHostForVm((PreemptableVm) vm, host)) {
            removePriorityHost(host);
            host.vmDestroy(vm);
            addPriorityHost(host);
        }
    }

    private Map<Integer, PriorityQueue<PreemptiveHost>> getPriorityToSortedHostFCFS() {
        return this.priorityToSortedHostFCFS;
    }

    private Map<Integer, PriorityQueue<PreemptiveHost>> getPriorityToSortedHostAvailabilityAware() {
        return this.priorityToSortedHostAvailabilityAware;
    }


    private void setPriorityToSLOTarget(List<PreemptiveHost> hostList) {
        //TODO REFACTOR!!!
        VmAvailabilityBasedPreemptionPolicy policy = (VmAvailabilityBasedPreemptionPolicy) hostList.get(0).getPreemptionPolicy();
        priorityToSLOTarget = policy.getPriorityToSLOTarget();
    }

}