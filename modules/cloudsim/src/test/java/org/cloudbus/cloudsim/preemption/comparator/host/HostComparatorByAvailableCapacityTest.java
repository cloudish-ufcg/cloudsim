package org.cloudbus.cloudsim.preemption.comparator.host;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Test;
import org.mockito.Mockito;

import junit.framework.Assert;

public class HostComparatorByAvailableCapacityTest {

	@Test
	public void testComparatorWithDiffAvailableCapacity() {
		List<Pe> peList1 = new ArrayList<>();
        peList1.add(new Pe(0, new PeProvisionerSimple(10)));

        List<Pe> peList2 = new ArrayList<>();
        peList2.add(new Pe(0, new PeProvisionerSimple(20)));
        
        PreemptionPolicy preemptionPolicy = Mockito.mock(PreemptionPolicy.class);

        // creating the hosts
        PreemptiveHost host1 = new PreemptiveHost(1, peList1,
        		new VmSchedulerMipsBased(peList1), preemptionPolicy);
        PreemptiveHost host2 = new PreemptiveHost(2, peList2,
                new VmSchedulerMipsBased(peList2), preemptionPolicy);
        
        // checking
        HostComparatorByAvailableCapacity comparator = new HostComparatorByAvailableCapacity();
        Assert.assertEquals(-1, comparator.compare(host1, host2));
        Assert.assertEquals(1, comparator.compare(host2, host1));
	}
	
	@Test
	public void testComparatorWithSameAvailableCapacityAndDiffId() {
		List<Pe> peList1 = new ArrayList<>();
        peList1.add(new Pe(0, new PeProvisionerSimple(20)));

        List<Pe> peList2 = new ArrayList<>();
        peList2.add(new Pe(0, new PeProvisionerSimple(20)));
        
        PreemptionPolicy preemptionPolicy = Mockito.mock(PreemptionPolicy.class);

        // creating the hosts
        PreemptiveHost host1 = new PreemptiveHost(1, peList1,
        		new VmSchedulerMipsBased(peList1), preemptionPolicy);
        PreemptiveHost host2 = new PreemptiveHost(2, peList2,
                new VmSchedulerMipsBased(peList2), preemptionPolicy);
        
        // checking
        HostComparatorByAvailableCapacity comparator = new HostComparatorByAvailableCapacity();
        Assert.assertEquals(-1, comparator.compare(host1, host2));
        Assert.assertEquals(1, comparator.compare(host2, host1));
	}
	
	@Test
	public void testComporatorWithSameAvailableCapacityAndSameId() {
		List<Pe> peList1 = new ArrayList<>();
        peList1.add(new Pe(0, new PeProvisionerSimple(20)));

        List<Pe> peList2 = new ArrayList<>();
        peList2.add(new Pe(0, new PeProvisionerSimple(20)));
        
        PreemptionPolicy preemptionPolicy = Mockito.mock(PreemptionPolicy.class);

        // creating the hosts
        PreemptiveHost host1 = new PreemptiveHost(1, peList1,
        		new VmSchedulerMipsBased(peList1), preemptionPolicy);
        PreemptiveHost host2 = new PreemptiveHost(1, peList2,
                new VmSchedulerMipsBased(peList2), preemptionPolicy);
        
        // checking
        HostComparatorByAvailableCapacity comparator = new HostComparatorByAvailableCapacity();
        Assert.assertEquals(0, comparator.compare(host1, host2));
        Assert.assertEquals(0, comparator.compare(host2, host1));
	}
	
	@Test
	public void testSortHosts() {
		List<Pe> peList1 = new ArrayList<>();
        peList1.add(new Pe(0, new PeProvisionerSimple(5)));

        List<Pe> peList2 = new ArrayList<>();
        peList2.add(new Pe(0, new PeProvisionerSimple(10)));
        
        List<Pe> peList3 = new ArrayList<>();
        peList3.add(new Pe(0, new PeProvisionerSimple(20)));
        
        PreemptionPolicy preemptionPolicy = Mockito.mock(PreemptionPolicy.class);

        // creating the hosts
        PreemptiveHost host1 = new PreemptiveHost(1, peList1,
        		new VmSchedulerMipsBased(peList1), preemptionPolicy);
        PreemptiveHost host2 = new PreemptiveHost(2, peList2,
                new VmSchedulerMipsBased(peList2), preemptionPolicy);
        PreemptiveHost host3 = new PreemptiveHost(3, peList3,
                new VmSchedulerMipsBased(peList3), preemptionPolicy);
        
        // checking
        HostComparatorByAvailableCapacity comparator = new HostComparatorByAvailableCapacity();
        
        SortedSet<PreemptiveHost> sortedHosts = new TreeSet<>(comparator);
        sortedHosts.add(host2);
        sortedHosts.add(host1);
        sortedHosts.add(host3);
        
        Assert.assertEquals(host1, sortedHosts.first());
        Assert.assertEquals(host3, sortedHosts.last());
        Assert.assertTrue(sortedHosts.contains(host2));
	}
	



}
