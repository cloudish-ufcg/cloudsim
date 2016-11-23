package org.cloudbus.cloudsim.preemption.policies.hostselection;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.util.VmAvailabilityBasedPreemptiveHostComparator;

import java.util.*;

/**
 * Created by Alessandro Lia Fook Santos and João Victor Mafra on 22/11/16.
 */
public class WorstFitVmAvailabilityBasedHostSelectionPolicy implements HostSelectionPolicy {

    List<PreemptiveHost> preemptiveHosts;

    public WorstFitVmAvailabilityBasedHostSelectionPolicy(List<PreemptiveHost> hosts) {
        preemptiveHosts = new ArrayList<>(hosts);
    }

    @Override
    public PreemptiveHost select(SortedSet<PreemptiveHost> hosts, Vm vm) {

        verifyVm(vm);

        if (! preemptiveHosts.isEmpty()) {

            PreemptableVm pVm = (PreemptableVm) vm;
            VmAvailabilityBasedPreemptiveHostComparator comparator = new VmAvailabilityBasedPreemptiveHostComparator(pVm);
            Collections.sort(preemptiveHosts, comparator);

            PreemptiveHost firstHost = preemptiveHosts.get(0);

            if (firstHost.isSuitableForVm(vm)) {
                return firstHost;
            }
        }
        return null;
    }

    private void verifyVm(Vm vm) {
        if (vm == null)
            throw new IllegalArgumentException("The Vm can not be null.");
    }

    @Override
    public void addHost(PreemptiveHost host) {
        preemptiveHosts.add(host);
    }

    @Override
    public void removeHost(PreemptiveHost host) {
        preemptiveHosts.remove(host);
    }
}
