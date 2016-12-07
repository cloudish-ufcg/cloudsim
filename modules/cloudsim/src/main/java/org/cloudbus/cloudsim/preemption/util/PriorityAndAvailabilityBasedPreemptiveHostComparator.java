package org.cloudbus.cloudsim.preemption.util;

import org.cloudbus.cloudsim.preemption.PreemptiveHost;

import java.util.Comparator;

/**
 * Created by Alessandro Lia Fook Santos on 06/12/16.
 */
public class PriorityAndAvailabilityBasedPreemptiveHostComparator implements Comparator<PreemptiveHost> {

    int priority;

    public PriorityAndAvailabilityBasedPreemptiveHostComparator(int priority) {
        setPriority(priority);
    }

    @Override
    public int compare(PreemptiveHost thisHost, PreemptiveHost otherHost) {

        double thisHostAvailableMips = thisHost.getAvailableMipsByPriorityAndAvailability(getPriority());
        double otherHostAvailableMips = otherHost.getAvailableMipsByPriorityAndAvailability(getPriority());

        int result =  (-1) * new Double(thisHostAvailableMips).compareTo(new Double(otherHostAvailableMips));

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
