package org.cloudbus.cloudsim.preemption.util;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;

import java.util.Comparator;

/**
 * Created by Alessandro Lia Fook Santos on 05/04/17.
 */
public class PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator implements Comparator<PreemptiveHost> {

    int priority;

    public PriorityAndAvailabilityBasedIncreasingCapacityPreemptiveHostComparator(int priority) {
        setPriority(priority);
    }

    @Override
    public int compare(PreemptiveHost thisHost, PreemptiveHost otherHost) {

        double thisHostAvailableMips = thisHost.getAvailableMipsByPriorityAndAvailability(getPriority());
        double otherHostAvailableMips = otherHost.getAvailableMipsByPriorityAndAvailability(getPriority());

        int result =  new Double(thisHostAvailableMips).compareTo(new Double(otherHostAvailableMips));

        if (result == 0)
            return new Integer(thisHost.getId()).compareTo(new Integer(otherHost.getId()));

        return result;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
