package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import gnu.trove.map.hash.THashMap;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.IncreasingCapacityPreemptiveHostComparator;
import org.cloudbus.cloudsim.preemption.util.PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator;
import org.cloudbus.cloudsim.preemption.util.PriorityAndAvailabilityBasedPreemptiveHostComparator;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

import java.util.*;

/**
 * Created by jvmafra on 03/04/17.
 */
public class BestFitAvailabilityAwareVmAllocationPolicy extends PriorityAndAvailabilityBasedVMAllocationPolicy{

    protected Map<Integer, TreeSet<PreemptiveHost>> priorityToSortedHostAvailabilityAware = new THashMap<>();
    protected Map<Integer, TreeSet<PreemptiveHost>> priorityToSortedHostFCFS = new THashMap<>();

    public BestFitAvailabilityAwareVmAllocationPolicy(List<PreemptiveHost> hostList, SimulationTimeUtil simulationTimeUtil) {
        super(hostList);


        verifyHosts(hostList);

        setSimulationTimeUtil(simulationTimeUtil);

        int numberOfPriorities = hostList.get(0).getNumberOfPriorities();

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

            List<Pe> peList1 = new ArrayList<Pe>();
            peList1.add(new Pe(0, new PeProvisionerSimple(pVm.getMips())));

            Properties properties = new Properties();
            properties.setProperty("number_of_priorities", "3");

            PreemptiveHost fakeHost = new PreemptiveHost(Integer.MIN_VALUE, peList1,
                    new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

            /*
            @TODO Decide if the vm has to be violating SLO in this time
            @TODO or in the next time to choose the way of select the host.
            */
            if (pVm.getCurrentAvailability(simulationTimeUtil.clock()) > getSLOTarget(pVm.getPriority())) {
                return getPriorityToSortedHostFCFS().get(pVm.getPriority()).ceiling(fakeHost);

            } else {
                return getPriorityToSortedHostAvailabilityAware().get(pVm.getPriority()).ceiling(fakeHost);
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
