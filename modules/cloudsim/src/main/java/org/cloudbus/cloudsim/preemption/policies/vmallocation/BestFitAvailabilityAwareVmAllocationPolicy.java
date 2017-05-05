package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.IncreasingCapacityPreemptiveHostComparator;
import org.cloudbus.cloudsim.preemption.util.PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator;

import gnu.trove.map.hash.THashMap;

/**
 * Created by jvmafra and giovannifs on 03/04/17.
 */
public class BestFitAvailabilityAwareVmAllocationPolicy extends PreemptableVmAllocationPolicy {

	private Map<Integer, TreeSet<PreemptiveHost>> priorityToHostsFCFSAware = new THashMap<>();
    private Map<Integer, TreeSet<PreemptiveHost>> priorityToHostsAvailabilityAware = new THashMap<>();
    private Map<Integer, Double> priorityToSLOTarget = new THashMap<Integer, Double>();
    private int numberOfPriorities;

    public BestFitAvailabilityAwareVmAllocationPolicy(List<PreemptiveHost> hostList, SimulationTimeUtil simulationTimeUtil) {
        super(hostList);

        if (hostList == null || hostList.isEmpty()) {
            throw new IllegalArgumentException("The host list must not be null or empty.");
        } 

        setSimulationTimeUtil(simulationTimeUtil);

        PreemptiveHost aHost = hostList.get(0);
		numberOfPriorities = aHost.getNumberOfPriorities();
		configureSLOTargets(aHost);

        for (int priority = 0; priority < numberOfPriorities; priority++) {

            IncreasingCapacityPreemptiveHostComparator comparatorFCFS = new IncreasingCapacityPreemptiveHostComparator(priority);
            PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator comparatorAvailabilityAware = new PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator(priority);

            getPriorityToHostsFCFSAware().put(priority, new TreeSet<PreemptiveHost>(comparatorFCFS));
            getPriorityToHostsAvailabilityAware().put(priority, new TreeSet<PreemptiveHost>(comparatorAvailabilityAware));
        }

        for (PreemptiveHost host : hostList) {
            for (int priority = 0; priority < numberOfPriorities; priority++) {
                getPriorityToHostsFCFSAware().get(priority).add(host);
                getPriorityToHostsAvailabilityAware().get(priority).add(host);
            }
        }
    }

    public BestFitAvailabilityAwareVmAllocationPolicy(List<PreemptiveHost> hosts){ 
    	this(hosts, new SimulationTimeUtil()); 
    }


    @Override
    public void preProcess() {

        priorityToHostsAvailabilityAware = new THashMap<>();

        int numberOfPriorities = ((PreemptiveHost) getHostList().get(0)).getNumberOfPriorities();

        for (int priority = 0; priority < numberOfPriorities; priority++) {

            PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator comparatorAvailabilityAware =
                    new PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator(priority);

            getPriorityToHostsAvailabilityAware().put(priority,
                    new TreeSet<PreemptiveHost>(comparatorAvailabilityAware));

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
        	throw new IllegalArgumentException("The Vm can not be null.");
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
    public Host selectHost(Vm vm) {

        if (vm == null) {
        	throw new IllegalArgumentException("The Vm to be allocated can not be null.");
        }

        if (!getHostList().isEmpty()) {
        	PreemptableVm pVm = (PreemptableVm) vm;
        	
        	TreeSet<PreemptiveHost> hosts;
            
            if (pVm.getCurrentAvailability(simulationTimeUtil.clock()) > getSLOTarget(pVm.getPriority())) {
                hosts = (TreeSet<PreemptiveHost>) getPriorityToHostsFCFSAware().get(pVm.getPriority());

            } else {
                hosts = (TreeSet<PreemptiveHost>) getPriorityToHostsAvailabilityAware().get(pVm.getPriority());
            }
            
			/*
			 * Checking if the smallest host is suitable for this VM, if yes
			 * this is the host that should be selected
			 */
            if (hosts.first().isSuitableForVm(pVm)) {            	
            	return hosts.first();
            
				/*
				 * Checking if the greatest host is suitable for this VM, if yes
				 * there is at least one host that is suitable for this VM
				 */
            } else if (hosts.last().isSuitableForVm(pVm)){
            	/*
            	 * Creating a host with the VM's capacity. This host would fit perfectly the vm.
            	 */
            	PreemptiveHost fakeHost = new PreemptiveHost(pVm.getMips(), numberOfPriorities);
            	return hosts.ceiling(fakeHost);            		
            }       
        }
        return null;
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
    

    public Map<Integer, TreeSet<PreemptiveHost>> getPriorityToHostsAvailabilityAware(){
        return priorityToHostsAvailabilityAware;
    }


    public Map<Integer, TreeSet<PreemptiveHost>> getPriorityToHostsFCFSAware(){
        return priorityToHostsFCFSAware;
    }
    
    protected double getSLOTarget(int priority) {
        return priorityToSLOTarget.get(priority);
    }
    
	private void configureSLOTargets(PreemptiveHost host) {
		VmAvailabilityBasedPreemptionPolicy policy = (VmAvailabilityBasedPreemptionPolicy) host.getPreemptionPolicy();
		priorityToSLOTarget = policy.getPriorityToSLOTarget();
	}
}
