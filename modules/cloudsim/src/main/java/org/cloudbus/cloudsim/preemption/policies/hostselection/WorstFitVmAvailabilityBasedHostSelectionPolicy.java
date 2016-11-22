package org.cloudbus.cloudsim.preemption.policies.hostselection;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;

import java.util.SortedSet;

/**
 * Created by alessandro.fook on 22/11/16.
 */
public class WorstFitVmAvailabilityBasedHostSelectionPolicy implements HostSelectionPolicy {
    
    @Override
    public PreemptiveHost select(SortedSet<PreemptiveHost> hosts, Vm vm) {
        return null;
    }

    @Override
    public void addHost(PreemptiveHost host) {

    }

    @Override
    public void removeHost(PreemptiveHost host) {

    }
}
