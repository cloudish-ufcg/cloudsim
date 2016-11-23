package org.cloudbus.cloudsim.preemption.policies.hostselection;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.util.PreemptiveHostComparator;

import java.util.*;

/**
 * Created by Alessandro Lia Fook Santos and João Victor Mafra on 22/11/16.
 */
public class WorstFitPriorityBasedHostSelectionPolicy implements HostSelectionPolicy {

    private Map<Integer, SortedSet<PreemptiveHost>> preemptiveHosts;
    private int numberOfPriorities;

    public WorstFitPriorityBasedHostSelectionPolicy(List<PreemptiveHost> hosts) {

        verifyHosts(hosts);

        this.numberOfPriorities = hosts.get(0).getNumberOfPriorities();
        this.preemptiveHosts = new HashMap<>();


        for (int priority = 0; priority < this.numberOfPriorities; priority++) {
            PreemptiveHostComparator comparator = new PreemptiveHostComparator(priority);
            preemptiveHosts.put(priority, new TreeSet<>(comparator));
            preemptiveHosts.get(priority).addAll(hosts);
        }
    }

    private void verifyHosts(List<PreemptiveHost> hosts) {
        if (hosts == null)
            throw new IllegalArgumentException(
                    "The list of host can not be null.");

        else if (hosts.isEmpty())
            throw new IllegalArgumentException(
                    "The list of host can not be empty.");
    }

    @Override
    public PreemptiveHost select(SortedSet<PreemptiveHost> hostSet, Vm vm) {

        verifyVm(vm);

		/*
		 * Only first host is checked because the hosts are sorted from bigger
		 * to smaller available capacity
		 */
        if (!preemptiveHosts.isEmpty()) {
            PreemptableVm pVm = (PreemptableVm) vm;
            SortedSet<PreemptiveHost> hosts = preemptiveHosts.get(pVm.getPriority());
            PreemptiveHost firstHost = hosts.first();

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

        for (int priority = 0; priority < this.numberOfPriorities; priority++){
            preemptiveHosts.get(priority).add(host);
        }
    }

    @Override
    public void removeHost(PreemptiveHost host) {

        for (int priority = 0; priority < this.numberOfPriorities; priority++){
            preemptiveHosts.get(priority).remove(host);
        }
    }
}
