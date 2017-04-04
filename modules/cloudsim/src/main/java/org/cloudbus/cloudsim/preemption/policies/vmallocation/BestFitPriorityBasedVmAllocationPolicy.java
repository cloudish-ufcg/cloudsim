package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.IncreasingCapacityPreemptiveHostComparator;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

import gnu.trove.map.hash.THashMap;

/**
 * Created by jvmafra on 03/04/17.
 */
public class BestFitPriorityBasedVmAllocationPolicy extends PriorityBasedVMAllocationPolicy {


//    private Map<Integer, SortedSet<PreemptiveHost>> priorityToSortedHost;


    public BestFitPriorityBasedVmAllocationPolicy(List<PreemptiveHost> hosts, SimulationTimeUtil simulationTimeUtil) {
        super(new ArrayList<Host>(0));

        if (hosts == null){
            throw new IllegalArgumentException(
                    "The set of host can not be null.");
        }

        setSimulationTimeUtil(simulationTimeUtil);
        priorityToSortedHost = new THashMap<>();
        int numberOfPriorities = hosts.get(0).getNumberOfPriorities();

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

            // TODO rethink and refactor it
            if (!hosts.isEmpty()) {
                List<Pe> peList1 = new ArrayList<Pe>();
                peList1.add(new Pe(0, new PeProvisionerSimple(gVm.getMips())));

                Properties properties = new Properties();
                properties.setProperty("number_of_priorities", "3");

                PreemptiveHost fakeHost = new PreemptiveHost(Integer.MIN_VALUE, peList1,
                        new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

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
