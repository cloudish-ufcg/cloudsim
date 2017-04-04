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
public class WorstFitAvailabilityAwareVmAllocationPolicy extends PriorityAndAvailabilityBasedVMAllocationPolicy {

    public WorstFitAvailabilityAwareVmAllocationPolicy(List<PreemptiveHost> hostList) {
        super(hostList);

        setSimulationTimeUtil(new SimulationTimeUtil());

		verifyHosts(hostList);

		int numberOfPriorities = hostList.get(0).getNumberOfPriorities();

		for (int priority = 0; priority < numberOfPriorities; priority++) {

			PreemptiveHostComparator comparatorFCFS = new PreemptiveHostComparator(priority);
			PriorityAndAvailabilityBasedPreemptiveHostComparator comparatorAvailabilityAware = new PriorityAndAvailabilityBasedPreemptiveHostComparator(
					priority);

			getPriorityToSortedHostFCFS().put(priority, new PriorityQueue<PreemptiveHost>(comparatorFCFS));
			getPriorityToSortedHostAvailabilityAware().put(priority,
					new PriorityQueue<PreemptiveHost>(comparatorAvailabilityAware));
		}

		for (PreemptiveHost host : hostList) {
			for (int priority = 0; priority < numberOfPriorities; priority++) {
				getPriorityToSortedHostFCFS().get(priority).add(host);
				getPriorityToSortedHostAvailabilityAware().get(priority).add(host);
			}
		}

		setPriorityToSLOTarget(hostList);
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
}