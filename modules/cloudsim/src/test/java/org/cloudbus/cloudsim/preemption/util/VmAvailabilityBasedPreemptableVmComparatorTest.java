package org.cloudbus.cloudsim.preemption.util;

import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class VmAvailabilityBasedPreemptableVmComparatorTest {

	private int submitTime = 0;
	private long runtime = 10;

	@Test
	public void testCompare() {
		double sloTarget = 0.5;		
		VmAvailabilityBasedPreemptableVmComparator comparator = new VmAvailabilityBasedPreemptableVmComparator(sloTarget);
		SimulationTimeUtil timeUtil = Mockito.mock(SimulationTimeUtil.class);
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		comparator.setSimulationTimeUtil(timeUtil);

		// availability on time 5 is 0.8
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 0, runtime, 1);
		vm0.setStartExec(1);
		
		// availability on time 5 is 0.6 
		PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 0, runtime, 1);
		vm1.setStartExec(2);

		// vm0 is farer from violate slo target (0.5)
		Assert.assertEquals(1, comparator.compare(vm0, vm1));
		Assert.assertEquals(-1, comparator.compare(vm1, vm0));
	}

	@Test
	public void testCompare2() {
		double sloTarget = 0.5;		
		VmAvailabilityBasedPreemptableVmComparator comparator = new VmAvailabilityBasedPreemptableVmComparator(sloTarget);
		SimulationTimeUtil timeUtil = Mockito.mock(SimulationTimeUtil.class);
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		comparator.setSimulationTimeUtil(timeUtil);

		// availability on time 5 is 1.0
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 0, runtime, 1);
		vm0.setStartExec(0);
		
		// availability on time 5 is 0.8
		PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 0, runtime, 1);
		vm1.setStartExec(1);
		
		// availability on time 5 is 0.6
		PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 0, submitTime, 0, runtime, 1);
		vm2.setStartExec(2);
		
		// availability on time 5 is 0.4 
		PreemptableVm vm3 = new PreemptableVm(3, 1, 5, 0, submitTime, 0, runtime, 1);
		vm3.setStartExec(3);

		// checking
		// vm0 is farthest VM from violate slo target (0.5)
		Assert.assertEquals(0, comparator.compare(vm0, vm0));
		Assert.assertEquals(1, comparator.compare(vm0, vm1));
		Assert.assertEquals(1, comparator.compare(vm0, vm2));
		Assert.assertEquals(1, comparator.compare(vm0, vm3));
		
		// vm1
		Assert.assertEquals(-1, comparator.compare(vm1, vm0));
		Assert.assertEquals(0, comparator.compare(vm1, vm1));
		Assert.assertEquals(1, comparator.compare(vm1, vm2));
		Assert.assertEquals(1, comparator.compare(vm1, vm3));
		
		// vm2
		Assert.assertEquals(-1, comparator.compare(vm2, vm0));
		Assert.assertEquals(-1, comparator.compare(vm2, vm1));
		Assert.assertEquals(0, comparator.compare(vm2, vm2));
		Assert.assertEquals(1, comparator.compare(vm2, vm3));
		
		// vm3
		Assert.assertEquals(-1, comparator.compare(vm3, vm0));
		Assert.assertEquals(-1, comparator.compare(vm3, vm1));
		Assert.assertEquals(-1, comparator.compare(vm3, vm2));
		Assert.assertEquals(0, comparator.compare(vm3, vm3));
	}
	
	
	@Test
	public void testSort() {
		double sloTarget = 0.5;		
		VmAvailabilityBasedPreemptableVmComparator comparator = new VmAvailabilityBasedPreemptableVmComparator(sloTarget);
		SimulationTimeUtil timeUtil = Mockito.mock(SimulationTimeUtil.class);
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		comparator.setSimulationTimeUtil(timeUtil);

		// availability on time 5 is 1.0
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 0, runtime, 1);
		vm0.setStartExec(0);
		
		// availability on time 5 is 0.8
		PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 0, runtime, 1);
		vm1.setStartExec(1);
		
		// availability on time 5 is 0.6
		PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 0, submitTime, 0, runtime, 1);
		vm2.setStartExec(2);
		
		// availability on time 5 is 0.4 
		PreemptableVm vm3 = new PreemptableVm(3, 1, 5, 0, submitTime, 0, runtime, 1);
		vm3.setStartExec(3);
		
		// adding vms into a sorted set in a not sorted way
		SortedSet<PreemptableVm> sortedSet = new TreeSet<PreemptableVm>(comparator);
		sortedSet.add(vm1);
		sortedSet.add(vm3);
		sortedSet.add(vm0);
		sortedSet.add(vm2);
		
		// checking sort
		Assert.assertEquals(4, sortedSet.size());
		Assert.assertEquals(vm3, sortedSet.first());
		Assert.assertEquals(vm0, sortedSet.last());
		Assert.assertTrue(sortedSet.contains(vm1));
		Assert.assertTrue(sortedSet.contains(vm2));
		
		// removing vm0
		sortedSet.remove(vm0);
		
		// checking sort
		Assert.assertEquals(3, sortedSet.size());
		Assert.assertEquals(vm3, sortedSet.first());
		Assert.assertEquals(vm1, sortedSet.last());
		Assert.assertTrue(sortedSet.contains(vm2));
		
		// removing vm3
		sortedSet.remove(vm3);
		
		// checking sort
		Assert.assertEquals(2, sortedSet.size());
		Assert.assertEquals(vm2, sortedSet.first());
		Assert.assertEquals(vm1, sortedSet.last());
	}
	
	@Test
	public void testSort2() {
		double sloTarget = 0.5;		
		VmAvailabilityBasedPreemptableVmComparator comparator = new VmAvailabilityBasedPreemptableVmComparator(sloTarget);
		SimulationTimeUtil timeUtil = Mockito.mock(SimulationTimeUtil.class);
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		comparator.setSimulationTimeUtil(timeUtil);

		// availability on time 5 is 1.0
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 0, runtime, 1);
		vm0.setStartExec(0);
		
		// availability on time 5 is 0.8
		PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 0, runtime, 1);
		vm1.setStartExec(0);
		
		// availability on time 5 is 0.6
		PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 0, submitTime, 0, runtime, 1);
		vm2.setStartExec(0);
		
		// availability on time 5 is 0.4 
		PreemptableVm vm3 = new PreemptableVm(3, 1, 5, 0, submitTime, 0, runtime, 1);
		vm3.setStartExec(0);
	
		PreemptableVm vm4 = new PreemptableVm(4, 1, 5, 0, submitTime, 0, runtime, 1);
		vm4.setStartExec(0);
		
		// adding vms into a sorted set in a not sorted way
		SortedSet<PreemptableVm> sortedSet = new TreeSet<PreemptableVm>(comparator);
		sortedSet.add(vm1);
		sortedSet.add(vm4);
		sortedSet.add(vm3);
		sortedSet.add(vm0);
		sortedSet.add(vm2);

		// checking sort
		Assert.assertEquals(5, sortedSet.size());
		Assert.assertEquals(vm0, sortedSet.first());
		Assert.assertEquals(vm4, sortedSet.last());
		Assert.assertTrue(sortedSet.contains(vm1));
		Assert.assertTrue(sortedSet.contains(vm2));
		Assert.assertTrue(sortedSet.contains(vm3));
		
		// removing vms
		Assert.assertNotNull(sortedSet.remove(vm0));
		Assert.assertNotNull(sortedSet.remove(vm4));
		Assert.assertNotNull(sortedSet.remove(vm2));
		Assert.assertNotNull(sortedSet.remove(vm1));
		
		// checking sort
		Assert.assertEquals(1, sortedSet.size());
		Assert.assertEquals(vm3, sortedSet.first());
		Assert.assertEquals(vm3, sortedSet.last());
	}
	
	@Test
	public void testSort3() {
		double sloTarget = 1;		
		VmAvailabilityBasedPreemptableVmComparator comparator = new VmAvailabilityBasedPreemptableVmComparator(sloTarget);
		SimulationTimeUtil timeUtil = Mockito.mock(SimulationTimeUtil.class);
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		comparator.setSimulationTimeUtil(timeUtil);

		// availability on time 5 is 1.0
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 0, runtime, 1);
		vm0.setStartExec(0);
		
		// availability on time 5 is 0.8
		PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 0, runtime, 1);
		vm1.setStartExec(0);
		
		// availability on time 5 is 0.6
		PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 0, submitTime, 0, runtime, 1);
		vm2.setStartExec(0);
		
		// availability on time 5 is 0.4 
		PreemptableVm vm3 = new PreemptableVm(3, 1, 5, 0, submitTime, 0, runtime, 1);
		vm3.setStartExec(0);
	
		PreemptableVm vm4 = new PreemptableVm(4, 1, 5, 0, submitTime, 0, runtime, 1);
		vm4.setStartExec(0);
		
		// adding vms into a sorted set in a not sorted way
		SortedSet<PreemptableVm> sortedSet = new TreeSet<PreemptableVm>(comparator);
		sortedSet.add(vm1);
		sortedSet.add(vm4);
		sortedSet.add(vm3);
		sortedSet.add(vm0);
		sortedSet.add(vm2);

		// checking sort
		Assert.assertEquals(5, sortedSet.size());
		Assert.assertEquals(vm0, sortedSet.first());
		Assert.assertEquals(vm4, sortedSet.last());
		Assert.assertTrue(sortedSet.contains(vm1));
		Assert.assertTrue(sortedSet.contains(vm2));
		Assert.assertTrue(sortedSet.contains(vm3));
		
		// removing vm0
		sortedSet.remove(vm0);
		sortedSet.remove(vm3);
		
		// checking sort
		Assert.assertEquals(3, sortedSet.size());
		Assert.assertEquals(vm1, sortedSet.first());
		Assert.assertEquals(vm4, sortedSet.last());
		Assert.assertTrue(sortedSet.contains(vm2));
		
		// removing vm3
		sortedSet.remove(vm1);
		sortedSet.remove(vm2);
		
		// checking sort
		Assert.assertEquals(1, sortedSet.size());
		Assert.assertEquals(vm4, sortedSet.first());
		Assert.assertEquals(vm4, sortedSet.last());
	}
	
	@Test
	public void testSort4() {
		double sloTarget = 1;		
		VmAvailabilityBasedPreemptableVmComparator comparator = new VmAvailabilityBasedPreemptableVmComparator(sloTarget);
		SimulationTimeUtil timeUtil = Mockito.mock(SimulationTimeUtil.class);
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		comparator.setSimulationTimeUtil(timeUtil);

		// availability on time 5 is 1.0
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 0, runtime, 1);
		vm0.setStartExec(0);
		
		// availability on time 5 is 1.0
		PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 0, runtime, 1);
		vm1.setStartExec(0);
		
		// availability on time 5 is 1.0
		PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 0, submitTime, 0, runtime, 1);
		vm2.setStartExec(0);
		
		// availability on time 5 is 1.0 
		PreemptableVm vm3 = new PreemptableVm(3, 1, 5, 0, submitTime, 0, runtime, 1);
		vm3.setStartExec(0);
	
		// availability on time 5 is 1.0 
		PreemptableVm vm4 = new PreemptableVm(4, 1, 5, 0, 5, 0, runtime, 1);
		vm4.setStartExec(5);
		
		// adding vms into a sorted set in a not sorted way
		SortedSet<PreemptableVm> sortedSet = new TreeSet<PreemptableVm>(comparator);
		sortedSet.add(vm1);
		sortedSet.add(vm4);
		sortedSet.add(vm3);
		sortedSet.add(vm0);
		sortedSet.add(vm2);
		
		// checking sort
		Assert.assertEquals(5, sortedSet.size());
		Assert.assertEquals(vm4, sortedSet.first());
		Assert.assertEquals(vm3, sortedSet.last());
		Assert.assertTrue(sortedSet.contains(vm1));
		Assert.assertTrue(sortedSet.contains(vm2));
		Assert.assertTrue(sortedSet.contains(vm0));
		
		// removing vm4 and vm3
		sortedSet.remove(vm4);
		sortedSet.remove(vm3);
		
		// checking sort
		Assert.assertEquals(3, sortedSet.size());
		Assert.assertEquals(vm0, sortedSet.first());
		Assert.assertEquals(vm2, sortedSet.last());
		Assert.assertTrue(sortedSet.contains(vm1));
		
		// removing vm2 and vm0
		sortedSet.remove(vm0);
		sortedSet.remove(vm2);
		
		// checking sort
		Assert.assertEquals(1, sortedSet.size());
		Assert.assertEquals(vm1, sortedSet.first());
		Assert.assertEquals(vm1, sortedSet.last());
	}
}
