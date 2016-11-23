package org.cloudbus.cloudsim.preemption.policies.hostselection;

import org.cloudbus.cloudsim.preemption.PreemptiveHost;

import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

/**
 * Created by Alessandro Lia Fook Santos on 23/11/16.
 */
public class HostSelectionPolicyFactory {

    public static final String WORST_FIT_MIPS = "worst_fit_mips";
    public static final String WORST_FIT_PRIORITY = "worst_fit_priority";
    public static final String VM_AVAILABILITY = "vm_availability";

    public static HostSelectionPolicy fabricHostSelectionPolicy (String hostSelector, List<PreemptiveHost> hosts) {

        switch (hostSelector) {

            case WORST_FIT_MIPS:
                return new WorstFitMipsBasedHostSelectionPolicy();

            case WORST_FIT_PRIORITY:
                return new WorstFitPriorityBasedHostSelectionPolicy(hosts);

            case VM_AVAILABILITY:
                return new WorstFitVmAvailabilityBasedHostSelectionPolicy(hosts);

            default:
                return null;
        }
    }
}
