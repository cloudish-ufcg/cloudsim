package org.cloudbus.cloudsim.preemption.util;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;

import java.util.Comparator;

/**
 * Created by Alessandro Lia Fook and João Victor Mafra on 20/09/16.
 */
public class PreemptiveHostComparator implements Comparator<PreemptiveHost>{

    private int priority;


    public PreemptiveHostComparator(int priority){
        setPriority(priority);
    }


    @Override
    public int compare(PreemptiveHost o1, PreemptiveHost o2) {
        int result = (-1)
                * (new Double(o1.getAvailableMipsByPriority(getPriority()))
                .compareTo(new Double(o2.getAvailableMipsByPriority(getPriority()))));

        if (result == 0)
            return new Integer(o1.getId()).compareTo(new Integer(o2.getId()));

        return result;
    }


    private void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
