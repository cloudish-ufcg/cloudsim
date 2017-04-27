package org.cloudbus.cloudsim.preemption.util;

import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class IncreasingCapacityPreemptiveHostComparatorTest {

	PreemptiveHost host1;
	PreemptiveHost host2;
	
	IncreasingCapacityPreemptiveHostComparator comparator;
	
	@Before
    public void setUp() {
		comparator = new IncreasingCapacityPreemptiveHostComparator(0);

        host1 = Mockito.mock(PreemptiveHost.class);
        host2 = Mockito.mock(PreemptiveHost.class);
    }

    @Test
    public void testHost2HasMoreAvailableMipsThanHost1(){
        Mockito.when(host1.getAvailableMipsByPriority(0)).thenReturn(45.4);
        Mockito.when(host2.getAvailableMipsByPriority(0)).thenReturn(45.5);

        Assert.assertEquals(comparator.compare(host1,host2), -1);
        Assert.assertEquals(comparator.compare(host2,host1), 1);
    }
    
    @Test
    public void testHost2HasSameAvailableMipsThanHost1AndBiggerId(){
        Mockito.when(host1.getAvailableMipsByPriority(0)).thenReturn(45.5);
        Mockito.when(host2.getAvailableMipsByPriority(0)).thenReturn(45.5);
        Mockito.when(host1.getId()).thenReturn(1);
        Mockito.when(host2.getId()).thenReturn(2);

        Assert.assertEquals(comparator.compare(host1,host2), -1);
        Assert.assertEquals(comparator.compare(host2,host1), 1);
    }
    
    @Test
    public void testHost2HasLessAvailableMipsThanHost1(){
        Mockito.when(host1.getAvailableMipsByPriority(0)).thenReturn(45.4);
        Mockito.when(host2.getAvailableMipsByPriority(0)).thenReturn(45.0);

        Assert.assertEquals(comparator.compare(host1,host2), 1);
        Assert.assertEquals(comparator.compare(host2,host1), -1);
    }

}
