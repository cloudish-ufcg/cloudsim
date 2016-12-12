package org.cloudbus.cloudsim.preemption.util;

import junit.framework.Assert;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.junit.Test;
import org.mockito.Mockito;

public class PriorityAndAvailabilityBasedVmComparatorTest {

	private int submitTime = 0;
	private long runtime = 10;
	
	@Test
	public void testCompareWithSamePriority() {
		SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
		Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);
		
		PriorityAndAvailabilityBasedVmComparator comparator = new PriorityAndAvailabilityBasedVmComparator(simulationTimeUtil );
		
		// availability on time 5 is 0.6
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 0, runtime);
		vm0.setStartExec(2);
		
		// availability on time 5 is 0.8 
		PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 0, runtime);
		vm1.setStartExec(1);
	
		// smaller availability before
		Assert.assertEquals(-1, comparator.compare(vm0, vm1));
		Assert.assertEquals(1, comparator.compare(vm1, vm0));
	}
	
	@Test
	public void testCompareWithSamePriorityAndAvailability() {
		SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
		Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);
		
		PriorityAndAvailabilityBasedVmComparator comparator = new PriorityAndAvailabilityBasedVmComparator(simulationTimeUtil );
		
		// availability on time 5 is 1.0
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, 0, 0, runtime);
		vm0.setStartExec(0);
		
		// availability on time 5 is 1.0 
		PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, 1, 0, runtime);
		vm1.setStartExec(1);
	
		// smaller submitTime before
		Assert.assertEquals(-1, comparator.compare(vm0, vm1));
		Assert.assertEquals(1, comparator.compare(vm1, vm0));
	}

	
	@Test
	public void testCompareWithDifferentPriority() {
		SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
		Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);
		
		PriorityAndAvailabilityBasedVmComparator comparator = new PriorityAndAvailabilityBasedVmComparator(simulationTimeUtil );
		
		// availability on time 5 is 0.6
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 1, runtime);
		vm0.setStartExec(2);
		
		// availability on time 5 is 0.8 
		PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 0, runtime);
		vm1.setStartExec(1);
	
		// higher priority before
		Assert.assertEquals(1, comparator.compare(vm0, vm1));
		Assert.assertEquals(-1, comparator.compare(vm1, vm0));
	}
}
