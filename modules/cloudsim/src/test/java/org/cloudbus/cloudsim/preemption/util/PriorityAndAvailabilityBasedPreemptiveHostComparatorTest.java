package org.cloudbus.cloudsim.preemption.util;

import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

/**
 * Created by Alessandro Lia Fook Santos on 06/12/16.
 */
public class PriorityAndAvailabilityBasedPreemptiveHostComparatorTest {


    public final int PROD = 0;
    public final int BATCH = 1;
    public final int FREE = 2;

    public final int BIGGER = 1;
    public final int SMALLER = -1;

    PreemptiveHost host1, host2, host3;
    PriorityAndAvailabilityBasedPreemptiveHostComparator comparatorProd;
    PriorityAndAvailabilityBasedPreemptiveHostComparator comparatorBatch;
    PriorityAndAvailabilityBasedPreemptiveHostComparator comparatorFree;

    @Before
    public void setUp() {

        host1 = Mockito.mock(PreemptiveHost.class);
        host2 = Mockito.mock(PreemptiveHost.class);
        host3 = Mockito.mock(PreemptiveHost.class);

        Mockito.when(host1.getId()).thenReturn(1);
        Mockito.when(host2.getId()).thenReturn(2);
        Mockito.when(host3.getId()).thenReturn(3);

        comparatorProd = new PriorityAndAvailabilityBasedPreemptiveHostComparator(PROD);
        comparatorBatch = new PriorityAndAvailabilityBasedPreemptiveHostComparator(BATCH);
        comparatorFree = new PriorityAndAvailabilityBasedPreemptiveHostComparator(FREE);
    }

    @Test
    public void testCompareToWithOnePriorityClass() {

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.1);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.2);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.3);

        Assert.assertEquals(BIGGER, comparatorProd.compare(host1, host2));
        Assert.assertEquals(BIGGER, comparatorProd.compare(host1, host3));
        Assert.assertEquals(BIGGER, comparatorProd.compare(host2, host3));

        Assert.assertEquals(SMALLER, comparatorProd.compare(host2, host1));
        Assert.assertEquals(SMALLER, comparatorProd.compare(host3, host1));
        Assert.assertEquals(SMALLER, comparatorProd.compare(host3, host2));
    }

    @Test
    public void testCompareToWithTwoPriorityClass() {

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.1);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.2);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.3);

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(BATCH)).thenReturn(0.1);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(BATCH)).thenReturn(0.1);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(BATCH)).thenReturn(0.3);

        Assert.assertEquals(BIGGER, comparatorProd.compare(host1, host2));
        Assert.assertEquals(BIGGER, comparatorProd.compare(host1, host3));
        Assert.assertEquals(BIGGER, comparatorProd.compare(host2, host3));

        Assert.assertEquals(SMALLER, comparatorProd.compare(host2, host1));
        Assert.assertEquals(SMALLER, comparatorProd.compare(host3, host1));
        Assert.assertEquals(SMALLER, comparatorProd.compare(host3, host2));

        Assert.assertEquals(BIGGER, comparatorBatch.compare(host2, host1));
        Assert.assertEquals(BIGGER, comparatorBatch.compare(host1, host3));
        Assert.assertEquals(BIGGER, comparatorBatch.compare(host2, host3));

        Assert.assertEquals(SMALLER, comparatorBatch.compare(host1, host2));
        Assert.assertEquals(SMALLER, comparatorBatch.compare(host3, host1));
        Assert.assertEquals(SMALLER, comparatorBatch.compare(host3, host2));
    }

    @Test
    public void testCompareToWithThreePriorityClass() {

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.1);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.2);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.3);

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(BATCH)).thenReturn(0.1);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(BATCH)).thenReturn(0.1);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(BATCH)).thenReturn(0.3);

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(FREE)).thenReturn(0.3);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(FREE)).thenReturn(0.2);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(FREE)).thenReturn(0.1);

        Assert.assertEquals(BIGGER, comparatorProd.compare(host1, host2));
        Assert.assertEquals(BIGGER, comparatorProd.compare(host1, host3));
        Assert.assertEquals(BIGGER, comparatorProd.compare(host2, host3));

        Assert.assertEquals(SMALLER, comparatorProd.compare(host2, host1));
        Assert.assertEquals(SMALLER, comparatorProd.compare(host3, host1));
        Assert.assertEquals(SMALLER, comparatorProd.compare(host3, host2));

        Assert.assertEquals(BIGGER, comparatorBatch.compare(host2, host1));
        Assert.assertEquals(BIGGER, comparatorBatch.compare(host1, host3));
        Assert.assertEquals(BIGGER, comparatorBatch.compare(host2, host3));

        Assert.assertEquals(SMALLER, comparatorBatch.compare(host1, host2));
        Assert.assertEquals(SMALLER, comparatorBatch.compare(host3, host1));
        Assert.assertEquals(SMALLER, comparatorBatch.compare(host3, host2));

        Assert.assertEquals(SMALLER, comparatorFree.compare(host1, host2));
        Assert.assertEquals(SMALLER, comparatorFree.compare(host1, host3));
        Assert.assertEquals(SMALLER, comparatorFree.compare(host2, host3));

        Assert.assertEquals(BIGGER, comparatorFree.compare(host2, host1));
        Assert.assertEquals(BIGGER, comparatorFree.compare(host3, host1));
        Assert.assertEquals(BIGGER, comparatorFree.compare(host3, host2));
    }

    @Test
    public void testSort() {

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.1);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.2);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.3);

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(BATCH)).thenReturn(0.1);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(BATCH)).thenReturn(0.1);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(BATCH)).thenReturn(0.3);

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(FREE)).thenReturn(0.3);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(FREE)).thenReturn(0.2);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(FREE)).thenReturn(0.1);

        SortedSet<PreemptiveHost> hostsProd = new TreeSet<>(comparatorProd);
        SortedSet<PreemptiveHost> hostsBatch = new TreeSet<>(comparatorBatch);
        SortedSet<PreemptiveHost> hostsFree = new TreeSet<>(comparatorFree);

        List<PreemptiveHost> expectedProd = new ArrayList<>();
        List<PreemptiveHost> expectedBatch = new ArrayList<>();
        List<PreemptiveHost> expectedFree = new ArrayList<>();

        expectedProd.add(host3);
        expectedProd.add(host2);
        expectedProd.add(host1);

        hostsProd.add(host1);
        hostsProd.add(host2);
        hostsProd.add(host3);

        Assert.assertArrayEquals(expectedProd.toArray(), hostsProd.toArray());

        expectedBatch.add(host3);
        expectedBatch.add(host1);
        expectedBatch.add(host2);

        hostsBatch.add(host1);
        hostsBatch.add(host3);
        hostsBatch.add(host2);

        Assert.assertArrayEquals(expectedBatch.toArray(), hostsBatch.toArray());

        expectedFree.add(host1);
        expectedFree.add(host2);
        expectedFree.add(host3);

        hostsFree.add(host3);
        hostsFree.add(host2);
        hostsFree.add(host1);

        Assert.assertArrayEquals(expectedFree.toArray(), hostsFree.toArray());
    }

    @Test
    public void testCeiling() {

        Mockito.when(host1.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.1);
        Mockito.when(host2.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.2);
        Mockito.when(host3.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.3);

        TreeSet<PreemptiveHost> hostsProd = new TreeSet<>(comparatorProd.reversed());

        hostsProd.add(host1);
        hostsProd.add(host2);
        hostsProd.add(host3);

        Assert.assertEquals(host2, hostsProd.ceiling(host2));

        PreemptiveHost host4 = Mockito.mock(PreemptiveHost.class);
        Mockito.when(host4.getId()).thenReturn(4);
        Mockito.when(host4.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.6);
        Assert.assertEquals(null, hostsProd.ceiling(host4));

        PreemptiveHost host5 = Mockito.mock(PreemptiveHost.class);
        Mockito.when(host5.getId()).thenReturn(4);
        Mockito.when(host5.getAvailableMipsByPriorityAndAvailability(PROD)).thenReturn(0.29);

        Assert.assertEquals(host3, hostsProd.ceiling(host5));
    }
}