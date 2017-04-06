package org.cloudbus.cloudsim.preemption.util;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;

import java.util.Comparator;

/**
 * Created by Alessandro Lia Fook and João Victor Mafra on 20/09/16.
 */
public class IncreasingCapacityPreemptiveHostComparator implements Comparator<PreemptiveHost>{

    private int priority;


    public IncreasingCapacityPreemptiveHostComparator(int priority){
        setPriority(priority);
    }


    @Override
    public int compare(PreemptiveHost o1, PreemptiveHost o2) {
        int result = (new Double(o1.getAvailableMipsByPriority(getPriority()))
                .compareTo(new Double(o2.getAvailableMipsByPriority(getPriority()))));

        if (result == 0) {
        	return new Integer(o1.getId()).compareTo(new Integer(o2.getId()));
        }

        return result;
    }

    private void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
