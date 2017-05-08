package org.cloudbus.cloudsim.preemption.comparator.host.capacityCost;

import org.cloudbus.cloudsim.preemption.CapacityCost;

import java.util.Comparator;

/**
 * Created by Alessandro Lia Fook Santos on 08/05/17.
 */
public class CapacityCostComparatorByCost implements Comparator<CapacityCost>{

    @Override
    public int compare(CapacityCost capacityCost1, CapacityCost capacityCost2) {

        double cost1 = capacityCost1.getCost();
        double cost2 = capacityCost2.getCost();

        if (cost1 != cost2)
            return Double.compare(cost1, cost2);

        double capacity1 = capacityCost1.getCapacity();
        double capacity2 = capacityCost2.getCapacity();

        if (capacity1 != capacity2)
            return Double.compare(capacity1, capacity2);

        return Integer.compare(capacityCost1.getpHost().getId(),
                capacityCost2.getpHost().getId());
    }
}
