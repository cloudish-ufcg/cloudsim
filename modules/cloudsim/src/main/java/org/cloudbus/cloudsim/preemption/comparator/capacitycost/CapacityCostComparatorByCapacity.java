package org.cloudbus.cloudsim.preemption.comparator.capacitycost;

import org.cloudbus.cloudsim.preemption.CapacityCost;

import java.util.Comparator;

/**
 * Created by Alessandro Lia Fook Santos on 08/05/17.
 */
public class CapacityCostComparatorByCapacity implements Comparator<CapacityCost> {

    @Override
    public int compare(CapacityCost capacityCost, CapacityCost capacityCost2) {

        double capacity1 = capacityCost.getCapacity();
        double capacity2 = capacityCost2.getCapacity();

        if (capacity1 != capacity2)
            return Double.compare(capacity1, capacity2);

        return Integer.compare(capacityCost.getHost().getId(),
                capacityCost2.getHost().getId());
    }
}
