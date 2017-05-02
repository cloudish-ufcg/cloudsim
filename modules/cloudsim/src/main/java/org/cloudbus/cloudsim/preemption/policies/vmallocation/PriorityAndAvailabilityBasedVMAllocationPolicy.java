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

        PreemptiveHost pHost = (PreemptiveHost) host;
        PreemptableVm pVm = (PreemptableVm) vm;
        
        removePriorityHost(pHost);
        boolean result = pHost.vmCreate(pVm);
        
		if (!result) {
			while (!result) {
				PreemptableVm vmToPreempt = (PreemptableVm) pHost.nextVmForPreempting();
				
				// TODO do we nedd this check?
				if (vmToPreempt != null && vmToPreempt.getPriority() >= pVm.getPriority()) {
					Log.printConcatLine(simulationTimeUtil.clock(),
							": Preempting VM #" + vmToPreempt.getId() + " (priority " + vmToPreempt.getPriority()
									+ ") to allocate VM #" + vm.getId() + " (priority " + pVm.getPriority() + ")");
				
					// preempting vm
					vmToPreempt.preempt(simulationTimeUtil.clock());
					pHost.vmDestroy(vmToPreempt);					
					vmToPreempt.setBeingInstantiated(true);
					
					getVmsRunning().remove(vmToPreempt);
					getVmsWaiting().add(vmToPreempt);
					
					result = pHost.vmCreate(pVm);					
				}
			}
		}    
        
		// updating allocation structures
		getVmsRunning().add(pVm);
		getVmsWaiting().remove(pVm);

		pVm.setStartExec(simulationTimeUtil.clock());
		pVm.allocatingToHost(host.getId());

		if (pVm.isBeingInstantiated()) {
			pVm.setBeingInstantiated(false);
		}

		Log.printConcatLine(simulationTimeUtil.clock(), ": VM #", vm.getId(), " was allocated on host #", host.getId(),
				" successfully.");
		
        addPriorityHost(pHost);

        return result;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        verifyVm(vm);

        Host host = vm.getHost();

        if (validateHostForVm((PreemptableVm) vm, host)) {        	
			if (!getVmsRunning().remove(vm)) {
				Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
						vm.getId(), " is not running to be removed.");
			}
			
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
