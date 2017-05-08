package org.cloudbus.cloudsim.preemption.comparator.host.capacityCost;

import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * Created by Alessandro Lia Fook Santos on 08/05/17.
 */
public class CapacityCostComparatorByCostTest {

    private CapacityCostComparatorByCost comparator;

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

        comparator = new CapacityCostComparatorByCost();

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
    public void testCompareByCost() {

        cost1 = new CapacityCost(0.1, 0.01, host1);
        cost2 = new CapacityCost(0.1, 0.009, host1);
        cost3 = new CapacityCost(0.1, 0.011, host1);
        cost4 = new CapacityCost(0.1, 0.0089, host1);

        testSingleAttributeComparation();
    }

    @Test
    public void testCompareByCapacity() {

        cost1 = new CapacityCost(0.016, 0.1, host1);
        cost2 = new CapacityCost(0.013, 0.1, host1);
        cost3 = new CapacityCost(0.019, 0.1, host1);
        cost4 = new CapacityCost(0.011, 0.1, host1);

        testSingleAttributeComparation();
    }

    @Test
    public void testCompareById() {

        cost1 = new CapacityCost(0.01, 0.1, host3);
        cost2 = new CapacityCost(0.01, 0.1, host2);
        cost3 = new CapacityCost(0.01, 0.1, host4);
        cost4 = new CapacityCost(0.01, 0.1, host1);

        testSingleAttributeComparation();
    }

    private void testSingleAttributeComparation() {

        Assert.assertEquals(1, comparator.compare(cost1, cost2));
        Assert.assertEquals(-1, comparator.compare(cost2, cost1));

        Assert.assertEquals(1, comparator.compare(cost3, cost1));
        Assert.assertEquals(1, comparator.compare(cost3, cost2));
        Assert.assertEquals(1, comparator.compare(cost3, cost4));

        Assert.assertEquals(-1, comparator.compare(cost1, cost3));
        Assert.assertEquals(-1, comparator.compare(cost2, cost3));
        Assert.assertEquals(-1, comparator.compare(cost4, cost3));

        Assert.assertEquals(1, comparator.compare(cost1, cost4));
        Assert.assertEquals(1, comparator.compare(cost2, cost4));
        Assert.assertEquals(1, comparator.compare(cost3, cost4));

        Assert.assertEquals(-1, comparator.compare(cost4, cost1));
        Assert.assertEquals(-1, comparator.compare(cost4, cost2));
        Assert.assertEquals(-1, comparator.compare(cost4, cost3));
    }

    @Test
    public void testTreeSetOrder() {

        cost1 = new CapacityCost(0.011, 0.1, host3);
        cost2 = new CapacityCost(0.02, 0.11, host2);
        cost3 = new CapacityCost(0.01, 0.1, host4);
        cost4 = new CapacityCost(0.01, 0.1, host1);

        TreeSet<CapacityCost> costs = new TreeSet<>(comparator);

        costs.add(cost1);
        costs.add(cost3);
        Assert.assertEquals(cost3, costs.first());
        Assert.assertEquals(cost1, costs.last());

        costs.add(cost2);
        Assert.assertEquals(cost3, costs.first());
        Assert.assertEquals(cost2, costs.last());

        costs.add(cost4);
        Assert.assertEquals(cost4, costs.first());
        Assert.assertEquals(cost2, costs.last());

        TreeSet<CapacityCost> tailCosts = (TreeSet) costs.tailSet(cost2);

        Assert.assertTrue(tailCosts.contains(cost2));
        Assert.assertEquals(1, tailCosts.size());
    }
}