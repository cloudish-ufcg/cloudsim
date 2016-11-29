package org.cloudbus.cloudsim.preemption.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class VmAvailabilityBasedPreemptiveHostComparatorTest {

	private static final double ACCEPTABLE_DIFFERENCE = 0.0000001;

	PreemptiveHost host1;
	PreemptiveHost host2;
	VmAvailabilityBasedPreemptionPolicy preemptionPolicy1;
	VmAvailabilityBasedPreemptionPolicy preemptionPolicy2; 
	
	SimulationTimeUtil timeUtil;
	
	@Before
	public void setUp() {
		Properties properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "1");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
		
		timeUtil = Mockito.mock(SimulationTimeUtil.class);
		Mockito.when(timeUtil.clock()).thenReturn(0d);
		
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(10)));
		preemptionPolicy1 = new VmAvailabilityBasedPreemptionPolicy(properties);
		preemptionPolicy1.setSimulationTimeUtil(timeUtil);
		host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1), preemptionPolicy1);

		peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(10)));
		preemptionPolicy2 = new VmAvailabilityBasedPreemptionPolicy(properties);
		preemptionPolicy2.setSimulationTimeUtil(timeUtil);
		host2 = new PreemptiveHost(2, peList1,
				new VmSchedulerMipsBased(peList1), preemptionPolicy2);
	}
	
	@Test
	public void testEmptyHosts() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);

		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);
		
		Assert.assertEquals(10,  host1.getAvailableMipsByVm(vm0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10,  host2.getAvailableMipsByVm(vm0), ACCEPTABLE_DIFFERENCE);
		
		VmAvailabilityBasedPreemptiveHostComparator comparator = new VmAvailabilityBasedPreemptiveHostComparator(vm0);
		
		// hostFCFS1 is smaller because of id
		Assert.assertEquals(-1, comparator.compare(host1, host2));
		Assert.assertEquals(1, comparator.compare(host2, host1));
	}

	@Test
	public void testHostsWithVMsViolatingSLO() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		double submitTime = 0;
		
		// availability on time 5 is 0.8
		PreemptableVm allocatedVm0 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 1, runtime, 0.9);
		host1.vmCreate(allocatedVm0);
		allocatedVm0.setStartExec(1);
		Assert.assertTrue(allocatedVm0.isViolatingAvailabilityTarget(5d));
		
		// availability on time 5 is 0.6 
		PreemptableVm allocatedVm1 = new PreemptableVm(1, 1, cpuReq, memReq, submitTime, 1, runtime, 0.9);
		host2.vmCreate(allocatedVm1);
		allocatedVm1.setStartExec(2);
		Assert.assertTrue(allocatedVm1.isViolatingAvailabilityTarget(5d));
				
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		
		Assert.assertEquals(0.8, allocatedVm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.6, allocatedVm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		PreemptableVm newVm = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 1, runtime);
		VmAvailabilityBasedPreemptiveHostComparator comparator = new VmAvailabilityBasedPreemptiveHostComparator(newVm);
		
		Assert.assertEquals(5,  host1.getAvailableMipsByVm(newVm), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(5,  host2.getAvailableMipsByVm(newVm), ACCEPTABLE_DIFFERENCE);
		
		// hostFCFS1 is smaller because of id because new priority 1 vm will not preempt both because they are violating SLO
		Assert.assertEquals(-1, comparator.compare(host1, host2));
		Assert.assertEquals(1, comparator.compare(host2, host1));
	}
	
	@Test
	public void testHostsWithVMsNotViolatingSLO() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		double submitTime = 0;
		
		// availability on time 5 is 0.8
		PreemptableVm allocatedVm0 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 2, runtime, 0.5);
		host1.vmCreate(allocatedVm0);
		allocatedVm0.setStartExec(1);
		Assert.assertFalse(allocatedVm0.isViolatingAvailabilityTarget(5d));
		
		// availability on time 5 is 0.6 
		PreemptableVm allocatedVm1 = new PreemptableVm(1, 1, cpuReq, memReq, submitTime, 2, runtime, 0.5);
		host2.vmCreate(allocatedVm1);
		allocatedVm1.setStartExec(2);
		Assert.assertFalse(allocatedVm1.isViolatingAvailabilityTarget(5d));
				
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		
		Assert.assertEquals(0.8, allocatedVm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.6, allocatedVm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		PreemptableVm newVm = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 2, runtime);
		VmAvailabilityBasedPreemptiveHostComparator comparator = new VmAvailabilityBasedPreemptiveHostComparator(newVm);
		
		Assert.assertEquals(10,  host1.getAvailableMipsByVm(newVm), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10,  host2.getAvailableMipsByVm(newVm), ACCEPTABLE_DIFFERENCE);
		
		// hostFCFS1 is smaller because of id and new priority 2 vm can preempt both because they aren't violating SLO
		Assert.assertEquals(-1, comparator.compare(host1, host2));
		Assert.assertEquals(1, comparator.compare(host2, host1));
	}
	
	@Test
	public void testHostsWithVMsViolatingAndNotViolatingSLO() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		double submitTime = 0;
		
		// availability on time 5 is 0.8
		PreemptableVm allocatedVm0 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 2, runtime, 0.5);
		host1.vmCreate(allocatedVm0);
		allocatedVm0.setStartExec(1);
		Assert.assertFalse(allocatedVm0.isViolatingAvailabilityTarget(5d));
		
		// availability on time 5 is 0.4 
		PreemptableVm allocatedVm1 = new PreemptableVm(1, 1, cpuReq, memReq, submitTime, 2, runtime, 0.5);
		host2.vmCreate(allocatedVm1);
		allocatedVm1.setStartExec(3);
		Assert.assertTrue(allocatedVm1.isViolatingAvailabilityTarget(5d));
				
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		
		Assert.assertEquals(0.8, allocatedVm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.4, allocatedVm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		PreemptableVm newVm = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 2, runtime);
		VmAvailabilityBasedPreemptiveHostComparator comparator = new VmAvailabilityBasedPreemptiveHostComparator(newVm);
		
		Assert.assertEquals(10,  host1.getAvailableMipsByVm(newVm), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(5,  host2.getAvailableMipsByVm(newVm), ACCEPTABLE_DIFFERENCE);
		
		// hostFCFS1 is smaller because has more availableMips for the new vm
		Assert.assertEquals(-1, comparator.compare(host1, host2));
		Assert.assertEquals(1, comparator.compare(host2, host1));
		
		// new vm is priority 0
		PreemptableVm newVmP0 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 0, runtime);
		comparator = new VmAvailabilityBasedPreemptiveHostComparator(newVmP0);
		
		Assert.assertEquals(10,  host1.getAvailableMipsByVm(newVmP0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10,  host2.getAvailableMipsByVm(newVmP0), ACCEPTABLE_DIFFERENCE);
		
		// hostFCFS1 is smaller because of id, the available mips are the same for vm with priority 0
		Assert.assertEquals(-1, comparator.compare(host1, host2));
		Assert.assertEquals(1, comparator.compare(host2, host1));
		
		// new vm is priority 1
		PreemptableVm newVmP1 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 1, runtime);
		comparator = new VmAvailabilityBasedPreemptiveHostComparator(newVmP1);
		
		Assert.assertEquals(10,  host1.getAvailableMipsByVm(newVmP1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10,  host2.getAvailableMipsByVm(newVmP1), ACCEPTABLE_DIFFERENCE);
		
		// hostFCFS1 is smaller because of id, the available mips are the same for vm with priority 1
		Assert.assertEquals(-1, comparator.compare(host1, host2));
		Assert.assertEquals(1, comparator.compare(host2, host1));
	}
	
	@Test
	public void testHostsWithVMsViolatingAndNotViolatingSLO2() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		double submitTime = 0;
		
		// availability on time 5 is 0.8
		PreemptableVm allocatedVm0 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 2, runtime, 0.5);
		host2.vmCreate(allocatedVm0);
		allocatedVm0.setStartExec(1);
		Assert.assertFalse(allocatedVm0.isViolatingAvailabilityTarget(5d));
		
		// availability on time 5 is 0.4 
		PreemptableVm allocatedVm1 = new PreemptableVm(1, 1, cpuReq, memReq, submitTime, 2, runtime, 0.5);
		host1.vmCreate(allocatedVm1);
		allocatedVm1.setStartExec(3);
		Assert.assertTrue(allocatedVm1.isViolatingAvailabilityTarget(5d));
				
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		
		Assert.assertEquals(0.8, allocatedVm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.4, allocatedVm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		PreemptableVm newVm = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 2, runtime);
		VmAvailabilityBasedPreemptiveHostComparator comparator = new VmAvailabilityBasedPreemptiveHostComparator(newVm);
		
		Assert.assertEquals(5,  host1.getAvailableMipsByVm(newVm), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10,  host2.getAvailableMipsByVm(newVm), ACCEPTABLE_DIFFERENCE);
		
		// hostFCFS2 is smaller because has more availableMips for the new vm
		Assert.assertEquals(1, comparator.compare(host1, host2));
		Assert.assertEquals(-1, comparator.compare(host2, host1));
		
		// new vm is priority 0
		PreemptableVm newVmP0 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 0, runtime);
		comparator = new VmAvailabilityBasedPreemptiveHostComparator(newVmP0);
		
		Assert.assertEquals(10,  host1.getAvailableMipsByVm(newVmP0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10,  host2.getAvailableMipsByVm(newVmP0), ACCEPTABLE_DIFFERENCE);
		
		// hostFCFS1 is smaller because of id, the available mips are the same for vm with priority 0
		Assert.assertEquals(-1, comparator.compare(host1, host2));
		Assert.assertEquals(1, comparator.compare(host2, host1));
		
		// new vm is priority 1
		PreemptableVm newVmP1 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 1, runtime);
		comparator = new VmAvailabilityBasedPreemptiveHostComparator(newVmP1);
		
		Assert.assertEquals(10,  host1.getAvailableMipsByVm(newVmP1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10,  host2.getAvailableMipsByVm(newVmP1), ACCEPTABLE_DIFFERENCE);
		
		// hostFCFS1 is smaller because of id, the available mips are the same for vm with priority 1
		Assert.assertEquals(-1, comparator.compare(host1, host2));
		Assert.assertEquals(1, comparator.compare(host2, host1));
	}
}
