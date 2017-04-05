package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.PriorityAndAvailabilityBasedPreemptiveHostComparator;

import gnu.trove.map.hash.THashMap;

public abstract class PriorityAndAvailabilityBasedVMAllocationPolicy extends PreemptableVmAllocationPolicy {

    protected Map<Integer, PriorityQueue<PreemptiveHost>> priorityToPriorityQueueHostFCFS = new THashMap<>();
    protected Map<Integer, PriorityQueue<PreemptiveHost>> priorityToPriorityQueueHostAvailabilityAware = new THashMap<>();
    protected Map<Integer, Double> priorityToSLOTarget = new THashMap<Integer, Double>();
	
	public PriorityAndAvailabilityBasedVMAllocationPolicy(List<PreemptiveHost> hostList) {
		super(hostList);
	}
	
    public void preProcess() {

        priorityToPriorityQueueHostAvailabilityAware = new THashMap<>();

        int numberOfPriorities = ((PreemptiveHost) getHostList().get(0)).getNumberOfPriorities();

        for (int priority = 0; priority < numberOfPriorities; priority++) {

			PriorityAndAvailabilityBasedPreemptiveHostComparator comparatorAvailabilityAware = new PriorityAndAvailabilityBasedPreemptiveHostComparator(
					priority);
			getPriorityToPriorityQueueHostAvailabilityAware().put(priority,
					new PriorityQueue<PreemptiveHost>(comparatorAvailabilityAware));

        }

        for (Host host : getHostList()) {
            PreemptiveHost pHost = (PreemptiveHost) host;
            for (int priority = 0; priority < numberOfPriorities; priority++) {
                getPriorityToPriorityQueueHostAvailabilityAware().get(priority).add(pHost);
            }
        }
    }

    protected void verifyHosts(List<PreemptiveHost> hostList) {
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
            getPriorityToPriorityQueueHostFCFS().get(priority).add(gHost);
            getPriorityToPriorityQueueHostAvailabilityAware().get(priority).add(gHost);
        }
    }

    protected void removePriorityHost(Host host) {
        PreemptiveHost gHost = (PreemptiveHost) host;
        for (int priority = 0; priority < gHost.getNumberOfPriorities(); priority++) {
            getPriorityToPriorityQueueHostFCFS().get(priority).remove(gHost);
            getPriorityToPriorityQueueHostAvailabilityAware().get(priority).remove(gHost);
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

//    @Override
//    public Host selectHost(Vm vm) {
//
//        verifyVm(vm);
//
//        if (!getHostList().isEmpty()) {
//            PreemptableVm pVm = (PreemptableVm) vm;
//            PreemptiveHost firstHost;
//            /*
//            @TODO Decide if the vm has to be violating SLO in this time
//            @TODO or in the next time to choose the way of select the host.
//            */
//            if (pVm.getCurrentAvailability(simulationTimeUtil.clock()) > getSLOTarget(pVm.getPriority())) {
//                firstHost = getPriorityToPriorityQueueHostFCFS().get(pVm.getPriority()).peek();
//
//            } else {
//                firstHost = getPriorityToPriorityQueueHostAvailabilityAware().get(pVm.getPriority()).peek();
//            }
//
//            if (firstHost.isSuitableForVm(pVm)) {
//                return firstHost;
//            }
//        }
//        return null;
//    }

    protected double getSLOTarget(int priority) {
        return priorityToSLOTarget.get(priority);
    }

    protected void verifyVm(Vm vm) {
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

    protected Map<Integer, PriorityQueue<PreemptiveHost>> getPriorityToPriorityQueueHostFCFS() {
        return this.priorityToPriorityQueueHostFCFS;
    }

    protected Map<Integer, PriorityQueue<PreemptiveHost>> getPriorityToPriorityQueueHostAvailabilityAware() {
        return this.priorityToPriorityQueueHostAvailabilityAware;
    }

    protected void setPriorityToSLOTarget(List<PreemptiveHost> hostList) {
        //TODO REFACTOR!!!
        VmAvailabilityBasedPreemptionPolicy policy = (VmAvailabilityBasedPreemptionPolicy) hostList.get(0).getPreemptionPolicy();
        priorityToSLOTarget = policy.getPriorityToSLOTarget();
    }	
}
