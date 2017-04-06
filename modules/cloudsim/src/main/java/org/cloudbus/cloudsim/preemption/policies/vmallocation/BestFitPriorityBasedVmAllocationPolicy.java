package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.util.IncreasingCapacityPreemptiveHostComparator;

/**
 * Created by jvmafra on 03/04/17.
 */
public class BestFitPriorityBasedVmAllocationPolicy extends PriorityBasedVMAllocationPolicy {

	private int numberOfPriorities;

    public BestFitPriorityBasedVmAllocationPolicy(List<PreemptiveHost> hosts, SimulationTimeUtil simulationTimeUtil) {
        super(new ArrayList<PreemptiveHost>(0));

        if (hosts == null){
            throw new IllegalArgumentException(
                    "The set of host can not be null.");
        }

        setSimulationTimeUtil(simulationTimeUtil);
        numberOfPriorities = hosts.get(0).getNumberOfPriorities();

        for (int priority = 0; priority < numberOfPriorities; priority++) {

            IncreasingCapacityPreemptiveHostComparator comparator = new IncreasingCapacityPreemptiveHostComparator(
                    priority);
            getPriorityToSortedHost().put(priority, new TreeSet<>(comparator));
        }

        // creating priority host skins
        for (PreemptiveHost host : hosts) {
            for (int priority = 0; priority < numberOfPriorities; priority++) {
                getPriorityToSortedHost().get(priority).add(host);
            }
        }
    }

    public BestFitPriorityBasedVmAllocationPolicy(List<PreemptiveHost> hosts) {
        this(hosts, new SimulationTimeUtil());
    }

    @Override
    public Host selectHost(Vm vm) {
        validateVm(vm);

        PreemptableVm gVm = (PreemptableVm) vm;

        if (getPriorityToSortedHost().containsKey(gVm.getPriority())) {
			TreeSet<PreemptiveHost> hosts = (TreeSet<PreemptiveHost>) getPriorityToSortedHost().get(gVm.getPriority());

            if (!hosts.isEmpty()) {
            	/*
            	 * Creating a host with the VM's capacity. This host would fit perfectly the vm.
            	 */
            	PreemptiveHost fakeHost = new PreemptiveHost(gVm.getMips(), numberOfPriorities);
                return hosts.ceiling(fakeHost);
            }
        }
        return null;
    }

    private void validateVm(Vm vm) {
        if (vm == null){
            throw new IllegalArgumentException("The Vm can not be null.");
        }
    }
}
