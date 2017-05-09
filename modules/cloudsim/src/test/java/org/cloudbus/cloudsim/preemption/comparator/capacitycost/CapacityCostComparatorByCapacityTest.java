package org.cloudbus.cloudsim.preemption.comparator.capacitycost;

import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.comparator.capacitycost.CapacityCostComparatorByCapacity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.TreeSet;

/**
 * Created by Alessandro Lia Fook Santos on 08/05/17.
 */
public class CapacityCostComparatorByCapacityTest {


    private CapacityCostComparatorByCapacity comparator;

    private CapacityCost cost1;
    private CapacityCost cost2;
    private CapacityCost cost3;
    private CapacityCost cost4;

    private PreemptiveHost host1;
    private PreemptiveHost host2;
    private PreemptiveHost host3;
    private PreemptiveHost host4;

    @Before
    public void SetUp() {

        comparator = new CapacityCostComparatorByCapacity();

        host1 = Mockito.mock(PreemptiveHost.class);
        Mockito.when(host1.getId()).thenReturn(1);

        host2 = Mockito.mock(PreemptiveHost.class);
        Mockito.when(host2.getId()).thenReturn(2);

        host3 = Mockito.mock(PreemptiveHost.class);
        Mockito.when(host3.getId()).thenReturn(3);

        host4 = Mockito.mock(PreemptiveHost.class);
        Mockito.when(host4.getId()).thenReturn(4);

    }

    @Test
    public void testCompareByCapacityWithSameCost(){

        cost1 = new CapacityCost(0.01, 10, host2);
        cost2 = new CapacityCost(0.009, 10, host4);
        cost3 = new CapacityCost(0.02, 10, host1);
        cost4 = new CapacityCost(0.008, 10, host3);

        Assert.assertEquals(1, comparator.compare(cost1, cost2));
        Assert.assertEquals(-1, comparator.compare(cost2, cost1));

        Assert.assertEquals(-1, comparator.compare(cost4, cost1));
        Assert.assertEquals(-1, comparator.compare(cost4, cost2));
        Assert.assertEquals(-1, comparator.compare(cost4, cost3));

        Assert.assertEquals(1, comparator.compare(cost1, cost4));
        Assert.assertEquals(1, comparator.compare(cost2, cost4));
        Assert.assertEquals(1, comparator.compare(cost3, cost4));

        Assert.assertEquals(1, comparator.compare(cost3, cost1));
        Assert.assertEquals(1, comparator.compare(cost3, cost2));
        Assert.assertEquals(1, comparator.compare(cost3, cost4));

        Assert.assertEquals(-1, comparator.compare(cost1, cost3));
        Assert.assertEquals(-1, comparator.compare(cost2, cost3));
        Assert.assertEquals(-1, comparator.compare(cost4, cost3));

    }
    
    @Test
    public void testCompareByCapacityWithDifferentCost(){

        cost1 = new CapacityCost(0.01, 0, host2);
        cost2 = new CapacityCost(0.009, 10, host4);
        cost3 = new CapacityCost(0.02, 20, host1);
        cost4 = new CapacityCost(0.008, 30, host3);

        Assert.assertEquals(1, comparator.compare(cost1, cost2));
        Assert.assertEquals(-1, comparator.compare(cost2, cost1));

        Assert.assertEquals(-1, comparator.compare(cost4, cost1));
        Assert.assertEquals(-1, comparator.compare(cost4, cost2));
        Assert.assertEquals(-1, comparator.compare(cost4, cost3));

        Assert.assertEquals(1, comparator.compare(cost1, cost4));
        Assert.assertEquals(1, comparator.compare(cost2, cost4));
        Assert.assertEquals(1, comparator.compare(cost3, cost4));

        Assert.assertEquals(1, comparator.compare(cost3, cost1));
        Assert.assertEquals(1, comparator.compare(cost3, cost2));
        Assert.assertEquals(1, comparator.compare(cost3, cost4));

        Assert.assertEquals(-1, comparator.compare(cost1, cost3));
        Assert.assertEquals(-1, comparator.compare(cost2, cost3));
        Assert.assertEquals(-1, comparator.compare(cost4, cost3));

    }

    @Test
    public void testCompareByHostId() {

        cost1 = new CapacityCost(0.01, 10, host3);
        cost2 = new CapacityCost(0.01, 10, host2);
        cost3 = new CapacityCost(0.01, 10, host4);
        cost4 = new CapacityCost(0.01, 10, host1);

        Assert.assertEquals(1, comparator.compare(cost1, cost2));
        Assert.assertEquals(-1, comparator.compare(cost2, cost1));

        Assert.assertEquals(-1, comparator.compare(cost4, cost1));
        Assert.assertEquals(-1, comparator.compare(cost4, cost2));
        Assert.assertEquals(-1, comparator.compare(cost4, cost3));

        Assert.assertEquals(1, comparator.compare(cost1, cost4));
        Assert.assertEquals(1, comparator.compare(cost2, cost4));
        Assert.assertEquals(1, comparator.compare(cost3, cost4));

        Assert.assertEquals(1, comparator.compare(cost3, cost1));
        Assert.assertEquals(1, comparator.compare(cost3, cost2));
        Assert.assertEquals(1, comparator.compare(cost3, cost4));

        Assert.assertEquals(-1, comparator.compare(cost1, cost3));
        Assert.assertEquals(-1, comparator.compare(cost2, cost3));
        Assert.assertEquals(-1, comparator.compare(cost4, cost3));
    }

    @Test
    public void testTreeSetOfCapacityCostWithSameCost() {

        cost1 = new CapacityCost(0.01, 10, host2);
        cost2 = new CapacityCost(0.01, 10, host4);
        cost3 = new CapacityCost(0.02, 10, host1);
        cost4 = new CapacityCost(0.008, 10, host3);

        TreeSet<CapacityCost> costs = new TreeSet<>(comparator);

        costs.add(cost1);
        costs.add(cost2);

        Assert.assertEquals(cost1, costs.first());
        Assert.assertEquals(cost2, costs.last());

        costs.add(cost3);
        Assert.assertEquals(cost1, costs.first());
        Assert.assertEquals(cost3, costs.last());

        costs.add(cost4);
        Assert.assertEquals(cost4, costs.first());
        Assert.assertEquals(cost3, costs.last());

        CapacityCost cost = new CapacityCost(0.009, 10, host4);

        TreeSet<CapacityCost> costsTail = (TreeSet) costs.tailSet(cost);

        Assert.assertFalse(costsTail.contains(cost4));
        Assert.assertEquals(cost1, costsTail.first());
        Assert.assertEquals(cost3, costsTail.last());
        Assert.assertTrue(costsTail.contains(cost2));
    }
    
    @Test
    public void testTreeSetOfCapacityCostWithDifferentCost() {

        cost1 = new CapacityCost(0.01, 0, host2);
        cost2 = new CapacityCost(0.01, 10, host4);
        cost3 = new CapacityCost(0.02, 20, host1);
        cost4 = new CapacityCost(0.008, 30, host3);

        TreeSet<CapacityCost> costs = new TreeSet<>(comparator);

        costs.add(cost1);
        costs.add(cost2);

        Assert.assertEquals(cost1, costs.first());
        Assert.assertEquals(cost2, costs.last());

        costs.add(cost3);
        Assert.assertEquals(cost1, costs.first());
        Assert.assertEquals(cost3, costs.last());

        costs.add(cost4);
        Assert.assertEquals(cost4, costs.first());
        Assert.assertEquals(cost3, costs.last());

        CapacityCost cost = new CapacityCost(0.009, 10, host4);

        TreeSet<CapacityCost> costsTail = (TreeSet) costs.tailSet(cost);

        Assert.assertFalse(costsTail.contains(cost4));
        Assert.assertEquals(cost1, costsTail.first());
        Assert.assertEquals(cost3, costsTail.last());
        Assert.assertTrue(costsTail.contains(cost2));
    }
}