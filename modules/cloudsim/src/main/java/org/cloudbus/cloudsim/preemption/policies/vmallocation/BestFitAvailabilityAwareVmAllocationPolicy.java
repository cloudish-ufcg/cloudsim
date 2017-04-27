package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.util.IncreasingCapacityPreemptiveHostComparator;
import org.cloudbus.cloudsim.preemption.util.PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator;

import gnu.trove.map.hash.THashMap;

/**
 * Created by jvmafra on 03/04/17.
 */
public class BestFitAvailabilityAwareVmAllocationPolicy extends PriorityAndAvailabilityBasedVMAllocationPolicy{

    private Map<Integer, TreeSet<PreemptiveHost>> priorityToSortedHostAvailabilityAware = new THashMap<>();
    private Map<Integer, TreeSet<PreemptiveHost>> priorityToSortedHostFCFS = new THashMap<>();
    private int numberOfPriorities;

    public BestFitAvailabilityAwareVmAllocationPolicy(List<PreemptiveHost> hostList, SimulationTimeUtil simulationTimeUtil) {
        super(hostList);


        verifyHosts(hostList);

        setSimulationTimeUtil(simulationTimeUtil);

        numberOfPriorities = hostList.get(0).getNumberOfPriorities();

        for (int priority = 0; priority < numberOfPriorities; priority++) {

            IncreasingCapacityPreemptiveHostComparator comparatorFCFS = new IncreasingCapacityPreemptiveHostComparator(priority);
            PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator comparatorAvailabilityAware = new PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator(priority);

            getPriorityToSortedHostFCFS().put(priority, new TreeSet<PreemptiveHost>(comparatorFCFS));
            getPriorityToSortedHostAvailabilityAware().put(priority, new TreeSet<PreemptiveHost>(comparatorAvailabilityAware));
        }

        for (PreemptiveHost host : hostList) {
            for (int priority = 0; priority < numberOfPriorities; priority++) {
                getPriorityToSortedHostFCFS().get(priority).add(host);
                getPriorityToSortedHostAvailabilityAware().get(priority).add(host);
            }
        }

        setPriorityToSLOTarget(hostList);
    }

    public BestFitAvailabilityAwareVmAllocationPolicy(List<PreemptiveHost> hosts){ this(hosts, new SimulationTimeUtil()); }


    @Override
    public void preProcess() {

        priorityToSortedHostAvailabilityAware = new THashMap<>();

        int numberOfPriorities = ((PreemptiveHost) getHostList().get(0)).getNumberOfPriorities();

        for (int priority = 0; priority < numberOfPriorities; priority++) {

            PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator comparatorAvailabilityAware =
                    new PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator(priority);

            getPriorityToSortedHostAvailabilityAware().put(priority,
                    new TreeSet<PreemptiveHost>(comparatorAvailabilityAware));

        }

        for (Host host : getHostList()) {
            PreemptiveHost pHost = (PreemptiveHost) host;
            for (int priority = 0; priority < numberOfPriorities; priority++) {
                getPriorityToSortedHostAvailabilityAware().get(priority).add(pHost);
            }
        }
    }

    @Override
    public Host selectHost(Vm vm) {

        verifyVm(vm);

        if (!getHostList().isEmpty()) {
        	PreemptableVm pVm = (PreemptableVm) vm;
        	
        	TreeSet<PreemptiveHost> hosts;
            
            if (pVm.getCurrentAvailability(simulationTimeUtil.clock()) > getSLOTarget(pVm.getPriority())) {
                hosts = (TreeSet<PreemptiveHost>) getPriorityToSortedHostFCFS().get(pVm.getPriority());

            } else {
                hosts = (TreeSet<PreemptiveHost>) getPriorityToSortedHostAvailabilityAware().get(pVm.getPriority());
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

    public Map<Integer, TreeSet<PreemptiveHost>> getPriorityToSortedHostAvailabilityAware(){
        return priorityToSortedHostAvailabilityAware;
    }


    public Map<Integer, TreeSet<PreemptiveHost>> getPriorityToSortedHostFCFS(){
        return priorityToSortedHostFCFS;
    }

    @Override
    protected void addPriorityHost(Host host) {
        PreemptiveHost gHost = (PreemptiveHost) host;
        for (int priority = 0; priority < gHost.getNumberOfPriorities(); priority++) {
            getPriorityToSortedHostFCFS().get(priority).add(gHost);
            getPriorityToSortedHostAvailabilityAware().get(priority).add(gHost);
        }
    }

    @Override
    protected void removePriorityHost(Host host) {
        PreemptiveHost gHost = (PreemptiveHost) host;
        for (int priority = 0; priority < gHost.getNumberOfPriorities(); priority++) {
            getPriorityToSortedHostFCFS().get(priority).remove(gHost);
            getPriorityToSortedHostAvailabilityAware().get(priority).remove(gHost);
        }
    }
}
