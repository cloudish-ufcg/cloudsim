package org.cloudbus.cloudsim.preemption.policies.hostselection;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Alessandro Lia Fook Santos and João Victor Mafra on 22/11/16.
 */
public class WorstFitPriorityBasedHostSelectionPolicy implements HostSelectionPolicy {

    SortedSet<PreemptiveHost> preemptiveHosts;

    public WorstFitPriorityBasedHostSelectionPolicy(List<PreemptiveHost> hosts) {
        preemptiveHosts = new TreeSet<>(hosts);
    }

    @Override
    public PreemptiveHost select(SortedSet<PreemptiveHost> hosts, Vm vm) {

        verifyVm(vm);

		/*
		 * Only first host is checked because the hosts are sorted from bigger
		 * to smaller available capacity
		 */
        if (!preemptiveHosts.isEmpty()) {
            PreemptiveHost firstHost = preemptiveHosts.first();

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
