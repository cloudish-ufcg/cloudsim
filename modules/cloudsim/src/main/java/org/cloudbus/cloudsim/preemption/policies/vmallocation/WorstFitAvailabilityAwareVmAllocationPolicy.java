package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.*;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.PreemptiveHostComparator;
import org.cloudbus.cloudsim.preemption.util.PriorityAndAvailabilityBasedPreemptiveHostComparator;

import gnu.trove.map.hash.THashMap;

/**
 * Created by Alessandro Lia Fook Santos and Giovanni Farias on 02/12/16.
 */
public class WorstFitAvailabilityAwareVmAllocationPolicy extends PreemptableVmAllocationPolicy {

	protected Map<Integer, PriorityQueue<PreemptiveHost>> priorityToHostsFCFSAware = new THashMap<>();
    protected Map<Integer, PriorityQueue<PreemptiveHost>> priorityToHostsAvailabilityAware = new THashMap<>();
    protected Map<Integer, Double> priorityToSLOTarget = new THashMap<Integer, Double>();
    
    public WorstFitAvailabilityAwareVmAllocationPolicy(List<PreemptiveHost> hostList) {
        super(hostList);

        setSimulationTimeUtil(new SimulationTimeUtil());

        if (hostList == null || hostList.isEmpty()) {
            throw new IllegalArgumentException("The host list must not be null or empty.");
        } 
        
		PreemptiveHost aHost = hostList.get(0);		
		int numberOfPriorities = aHost.getNumberOfPriorities();
		configureSLOTargets(hostList.get(0));

		for (int priority = 0; priority < numberOfPriorities; priority++) {

			PreemptiveHostComparator comparatorFCFS = new PreemptiveHostComparator(priority);
			PriorityAndAvailabilityBasedPreemptiveHostComparator comparatorAvailabilityAware = new PriorityAndAvailabilityBasedPreemptiveHostComparator(
					priority);

			getPriorityToHostsFCFSAware().put(priority, new PriorityQueue<PreemptiveHost>(comparatorFCFS));
			getPriorityToHostsAvailabilityAware().put(priority,
					new PriorityQueue<PreemptiveHost>(comparatorAvailabilityAware));
		}

		for (PreemptiveHost host : hostList) {
			for (int priority = 0; priority < numberOfPriorities; priority++) {
				getPriorityToHostsFCFSAware().get(priority).add(host);
				getPriorityToHostsAvailabilityAware().get(priority).add(host);
			}
		}
    }
    
    public void preProcess() {

        priorityToHostsAvailabilityAware = new THashMap<>();

        int numberOfPriorities = ((PreemptiveHost) getHostList().get(0)).getNumberOfPriorities();

        for (int priority = 0; priority < numberOfPriorities; priority++) {

			PriorityAndAvailabilityBasedPreemptiveHostComparator comparatorAvailabilityAware = new PriorityAndAvailabilityBasedPreemptiveHostComparator(
					priority);
			getPriorityToHostsAvailabilityAware().put(priority,
					new PriorityQueue<PreemptiveHost>(comparatorAvailabilityAware));

        }

        for (Host host : getHostList()) {
            PreemptiveHost pHost = (PreemptiveHost) host;
            for (int priority = 0; priority < numberOfPriorities; priority++) {
                getPriorityToHostsAvailabilityAware().get(priority).add(pHost);
            }
        }
    }
    
    @Override
    public boolean preempt(PreemptableVm vm) {

    	if (vm == null) {
    		throw new IllegalArgumentException("The Vm to be preempted can not be null.");
    	}
    	
        PreemptiveHost host = (PreemptiveHost) vm.getHost();

		if (host == null) {
			Log.printConcatLine(simulationTimeUtil.clock(), ": VM #", vm.getId(), " does not have a host.");
			return false;
		}

        vm.preempt(simulationTimeUtil.clock());
        removeHostFromStructure(host);
        host.vmDestroy(vm);
        addHostIntoStructure(host);
        vm.setBeingInstantiated(true);
        return true;
    }

	@Override
	public void addHostIntoStructure(PreemptiveHost host) {	    
        for (int priority = 0; priority < host.getNumberOfPriorities(); priority++) {
            getPriorityToHostsFCFSAware().get(priority).add(host);
            getPriorityToHostsAvailabilityAware().get(priority).add(host);
        }
		
	}

	@Override
	public void removeHostFromStructure(PreemptiveHost host) {
        for (int priority = 0; priority < host.getNumberOfPriorities(); priority++) {
            getPriorityToHostsFCFSAware().get(priority).remove(host);
            getPriorityToHostsAvailabilityAware().get(priority).remove(host);
        }		
	}

    @Override
    public Host selectHost(Vm vm) {

    	if (vm == null) {
    		throw new IllegalArgumentException("The Vm to be allocated can not be null.");
    	}

        if (!getHostList().isEmpty()) {
            PreemptableVm pVm = (PreemptableVm) vm;
            PreemptiveHost firstHost;
            /*
            @TODO Decide if the vm has to be violating SLO in this time
            @TODO or in the next time to choose the way of select the host.
            */
            if (pVm.getCurrentAvailability(simulationTimeUtil.clock()) > getSLOTarget(pVm.getPriority())) {
                firstHost = getPriorityToHostsFCFSAware().get(pVm.getPriority()).peek();

            } else {
                firstHost = getPriorityToHostsAvailabilityAware().get(pVm.getPriority()).peek();
            }

            if (firstHost.isSuitableForVm(pVm)) {
                return firstHost;
            }
        }
        return null;
    }
    
    protected double getSLOTarget(int priority) {
        return priorityToSLOTarget.get(priority);
    }

    protected Map<Integer, PriorityQueue<PreemptiveHost>> getPriorityToHostsFCFSAware() {
        return this.priorityToHostsFCFSAware;
    }

    protected Map<Integer, PriorityQueue<PreemptiveHost>> getPriorityToHostsAvailabilityAware() {
        return this.priorityToHostsAvailabilityAware;
    }

    private void configureSLOTargets(PreemptiveHost host) {
        VmAvailabilityBasedPreemptionPolicy policy = (VmAvailabilityBasedPreemptionPolicy) host.getPreemptionPolicy();
        priorityToSLOTarget = policy.getPriorityToSLOTarget();
    }
}