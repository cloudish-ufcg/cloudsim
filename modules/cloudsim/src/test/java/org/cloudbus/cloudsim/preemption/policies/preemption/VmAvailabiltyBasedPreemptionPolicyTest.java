package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.Properties;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class VmAvailabiltyBasedPreemptionPolicyTest {

	private static final double ACCEPTABLE_DIFFERENCE = 0.00;
	
	Properties properties;
	VmAvailabilityBasedPreemptionPolicy policy;
	SimulationTimeUtil timeUtil;
	
	@Before
	public void setUp() {
		properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "1");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		policy = new VmAvailabilityBasedPreemptionPolicy(properties);
		policy.setTotalMips(10);
		
		timeUtil = Mockito.mock(SimulationTimeUtil.class);
	}
	
	@Test
	public void testInitialization() {
		policy = new VmAvailabilityBasedPreemptionPolicy(properties);
		policy.setTotalMips(10);
		
		// checking
		Assert.assertEquals(3, policy.getNumberOfPriorities());
		Assert.assertEquals(1, policy.getPriorityToSLOTarget().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.9, policy.getPriorityToSLOTarget().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.5, policy.getPriorityToSLOTarget().get(2), ACCEPTABLE_DIFFERENCE);		
		Assert.assertEquals(10, policy.getTotalMips(), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization() {
		// setting properties
		Properties properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "1");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPreemptionPolicy(properties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization2() {
		// setting properties
		Properties properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "1");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
				
		new VmAvailabilityBasedPreemptionPolicy(properties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization3() {
		// setting properties
		Properties properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "0");
				
		new VmAvailabilityBasedPreemptionPolicy(properties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization4() {
		// setting properties
		Properties properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "-1");
				
		new VmAvailabilityBasedPreemptionPolicy(properties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization5() {
		// setting properties
		Properties properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "zero", "1");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPreemptionPolicy(properties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization6() {
		// setting properties
		Properties properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "one");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPreemptionPolicy(properties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization7() {
		// setting properties
		Properties properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "-1");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPreemptionPolicy(properties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization8() {
		// setting properties
		Properties properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "0.0", "1");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPreemptionPolicy(properties);
	}
		
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 1, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		policy.allocating(vm0);
		vm0.setStartExec(0);
		
		// checking after allocating vm with priority 1
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
		
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
		
		PreemptableVm vm1 = new PreemptableVm(2, 1, cpuReq, memReq, 5, 1, runtime);
		
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		// assert first vm would be preempted by second
		Assert.assertEquals(cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vm1), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable2() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 1, runtime);
		PreemptableVm vm1 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 1, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating both vms
		policy.allocating(vm0);
		vm0.setStartExec(0);
		
		policy.allocating(vm1);
		vm1.setStartExec(0);
		
		// checking after allocating VMs with priority 0
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
		
		Assert.assertEquals(10, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(1).first());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(1).last());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
				
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, memReq, 5, 1, runtime);
		
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		// assert two first vms would be preempted by third one
		Assert.assertEquals(2 * cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vm2), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailableWithPriority0() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 0, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating both vms
		policy.allocating(vm0);
		vm0.setStartExec(0);
		
		policy.allocating(vm1);
		vm1.setStartExec(0);
		
		// checking after allocating VMs with priority 0
		Assert.assertEquals(10, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(0).first());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(0).last());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
				
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, memReq, 5, 0, runtime);
		
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		// assert two first vms wouldn't be preempted by third one because priority 0 SLO is 1
		Assert.assertEquals(0, policy.calcMipsOfSamePriorityToBeAvailable(vm2), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable3() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		// mocking time 2
		Mockito.when(timeUtil.clock()).thenReturn(2d);

		policy = new VmAvailabilityBasedPreemptionPolicy(properties, timeUtil);
		policy.setTotalMips(10);
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 1, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating both vms
		policy.allocating(vm0);
		vm0.setStartExec(0);
		
		// checking after allocating VM with priority 1
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
		
		Assert.assertEquals(5, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq, memReq, 2, 1, runtime);

		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(2), ACCEPTABLE_DIFFERENCE);
		
		// allocating second VM
		policy.allocating(vm1);
		vm1.setStartExec(2);
		
		// checking after allocating second VM with priority 1
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
		
		Assert.assertEquals(10, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(1).first());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(1).last());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, memReq, 5, 1, runtime);
		
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		// assert two first vms would be preempted by third one
		Assert.assertEquals(2 * cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vm2), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable4() {
		// testing with cpuReq double and priority 1
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 4.55;
		
		// mocking time 2
		Mockito.when(timeUtil.clock()).thenReturn(2d);

		policy = new VmAvailabilityBasedPreemptionPolicy(properties, timeUtil);
		policy.setTotalMips(10);
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 1, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating both vms
		policy.allocating(vm0);
		vm0.setStartExec(0);
		
		// checking after allocating VM with priority 1
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
		
		Assert.assertEquals(4.55, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
			
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq, memReq, 2, 1, runtime);

		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getAvailabilityWhileAllocating(2), ACCEPTABLE_DIFFERENCE);
		
		// allocating second VM
		policy.allocating(vm1);
		vm1.setStartExec(2);
		
		// checking after allocating second VM with priority 1
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
	
		Assert.assertEquals(9.1, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(1).first());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(1).last());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, memReq, 5, 1, runtime);
		
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		// assert two first vms would be preempted by third one
		Assert.assertEquals(2 * cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vm2), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable5() {
		// testing if VMs with priorities different from arrivingVm don't impact this method
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 2.55;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 1, runtime);
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 2, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating vms
		policy.allocating(vm0);
		vm0.setStartExec(0);

		policy.allocating(vm1);
		vm1.setStartExec(0);
		
		policy.allocating(vm2);
		vm2.setStartExec(0);
		
		// checking after allocating VMs
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2, policy.getPriorityToVms().get(2).first());
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
	
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);

		// new VMs have smaller availabilities
		// assert only VM with the same priority would be preempted by new ones
		PreemptableVm vmP0 = new PreemptableVm(4, 1, cpuReq, memReq, 5, 0, runtime);
		Assert.assertEquals(0, vmP0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.calcMipsOfSamePriorityToBeAvailable(vmP0), ACCEPTABLE_DIFFERENCE);

		PreemptableVm vmP1 = new PreemptableVm(5, 1, cpuReq, memReq, 5, 1, runtime);
		Assert.assertEquals(0, vmP1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vmP1), ACCEPTABLE_DIFFERENCE);
		
		PreemptableVm vmP2 = new PreemptableVm(6, 1, cpuReq, memReq, 5, 2, runtime);
		Assert.assertEquals(0, vmP2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vmP2), ACCEPTABLE_DIFFERENCE);
	}

	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable6() {
		// testing if VMs with priorities different from arrivingVm don't impact this method
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 2.55;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 1, runtime);
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 2, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating vms
		policy.allocating(vm0);
		vm0.setStartExec(0);

		policy.allocating(vm1);
		vm1.setStartExec(0);
		
		policy.allocating(vm2);
		vm2.setStartExec(0);
		
		// checking after allocating VMs
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2, policy.getPriorityToVms().get(2).first());
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
	
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);

		// new VMs have smaller availabilities
		// assert only VM with the same priority would be preempted by new ones
		PreemptableVm vmP0 = new PreemptableVm(4, 1, cpuReq, memReq, 5, 0, runtime);
		Assert.assertEquals(0, vmP0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.calcMipsOfSamePriorityToBeAvailable(vmP0), ACCEPTABLE_DIFFERENCE);

		PreemptableVm vmP1 = new PreemptableVm(5, 1, cpuReq, memReq, 5, 1, runtime);
		Assert.assertEquals(0, vmP1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vmP1), ACCEPTABLE_DIFFERENCE);
		
		PreemptableVm vmP2 = new PreemptableVm(6, 1, cpuReq, memReq, 5, 2, runtime);
		Assert.assertEquals(0, vmP2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vmP2), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable7() {
		// testing if arrivingVM with priority different from runningVm won't preempt those ones
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 2.55;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		// allocating vms
		policy.allocating(vm0);
		vm0.setStartExec(0);
		
		// checking after allocating VMs
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getPriorityToVms().get(1).size());
		Assert.assertTrue(policy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getPriorityToVms().get(2).size());
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		// mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
		
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);

		// vm with priority 1
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq, memReq, 5, 1, runtime);
		
		// checking vm availability and zero mips will be available
		Assert.assertEquals(0, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.calcMipsOfSamePriorityToBeAvailable(vm1), ACCEPTABLE_DIFFERENCE);
		
		// vm with priority 2		
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, memReq, 5, 2, runtime);
		// checking vm availability and zero mips will be available
		Assert.assertEquals(0, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.calcMipsOfSamePriorityToBeAvailable(vm2), ACCEPTABLE_DIFFERENCE);
	}
	
	/*
	 * The arrivingVm doesn't violate its SLO target
	 */
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable8() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 2, runtime);
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 2, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating both vms
		policy.allocating(vm0);
		vm0.setStartExec(0);
		
		policy.allocating(vm1);
		vm1.setStartExec(0);
		
		// checking after allocating VMs with priority 0
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(10, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, policy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(2).first());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(2).last());
		
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
		
		//simulating VM is running since time 2
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 2, runtime);
		vm2.setStartExec(2);
				
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.6, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		// assert two first vms wouldn't be preempted by third one
		Assert.assertEquals(0, policy.calcMipsOfSamePriorityToBeAvailable(vm2), ACCEPTABLE_DIFFERENCE);
	}

	/*
	 * There are availableMips independent of vm priority
	 */
	@Test
	public void testIsSuitableFor() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;		
		double submitTime = 0;
		
		PreemptableVm vmP0 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 0, runtime);
		Assert.assertEquals(0, vmP0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);

		// allocating vm
		policy.allocating(vmP0);
		vmP0.setStartExec(0);
		
		// checking after allocating VM
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vmP0, policy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getPriorityToVms().get(1).size());
		Assert.assertTrue(policy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getPriorityToVms().get(2).size());
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		// vm with priority 1
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, submitTime, 1, runtime)));
		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, submitTime, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 6, memReq, submitTime, 1, runtime)));

		// vm with priority 2
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, submitTime, 2, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, submitTime, 2, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, submitTime, 2, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, submitTime, 2, runtime)));
		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 6, memReq, submitTime, 2, runtime)));
	}
	
	/*
	 * There are availableMips for the vm priority (preempting vm with lower
	 * priorities)
	 */
	@Test
	public void testIsSuitableFor2() {
		double memReq = 0;
		double runtime = 10;
		double cpuReq = 5;		
		double submitTime = 0;
		
		PreemptableVm vmP2 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 2, runtime);
		Assert.assertEquals(0, vmP2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);

		// allocating vm
		policy.allocating(vmP2);
		vmP2.setStartExec(0);
		
		// checking after allocating VM
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getPriorityToVms().get(0).size());
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getPriorityToVms().get(1).size());
		Assert.assertTrue(policy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vmP2, policy.getPriorityToVms().get(2).first());
		
		// vm with priority 1
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, submitTime, 1, runtime)));		
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 6, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, submitTime, 1, runtime)));

		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, submitTime, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, submitTime, 1, runtime)));

		// vm with priority 0
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 6, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, submitTime, 0, runtime)));

		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, submitTime, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, submitTime, 0, runtime)));
	}	
	
	/*
	 * There are availableMips for the vm priority (preempting vms with lower
	 * priorities)
	 */
	@Test
	public void testIsSuitableFor3() {
		double memReq = 0;
		double runtime = 10;
		double cpuReq = 5;		
		double submitTime = 0;
		
		PreemptableVm vmP2 = new PreemptableVm(0, 1, cpuReq, memReq, submitTime, 2, runtime);
		Assert.assertEquals(0, vmP2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);

		PreemptableVm vmP1 = new PreemptableVm(0, 1, cpuReq/2, memReq, submitTime, 1, runtime);
		Assert.assertEquals(0, vmP1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating vms
		policy.allocating(vmP2);
		vmP2.setStartExec(0);
		
		policy.allocating(vmP1);
		vmP1.setStartExec(0);
		
		// checking after allocating VM
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getPriorityToVms().get(0).size());
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
		
		Assert.assertEquals(cpuReq/2, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vmP1, policy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vmP2, policy.getPriorityToVms().get(2).first());
		
		//checking availableMipsByPriority
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(7.5, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2.5, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// vm with priority 0
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.4, memReq, submitTime, 0, runtime)));	
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.5, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.6, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 7.4, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 7.5, memReq, submitTime, 0, runtime)));		
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 7.6, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, submitTime, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, submitTime, 0, runtime)));
		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, submitTime, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, submitTime, 0, runtime)));

		// vm with priority 1
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.4, memReq, submitTime, 1, runtime)));	
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.5, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.6, memReq, submitTime, 1, runtime)));		
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 7.4, memReq, submitTime, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 7.5, memReq, submitTime, 1, runtime)));
		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.6, memReq, submitTime, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, submitTime, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, submitTime, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, submitTime, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, submitTime, 1, runtime)));

		// vm with priority 2
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, submitTime, 2, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.4, memReq, submitTime, 2, runtime)));	
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.5, memReq, submitTime, 2, runtime)));
		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 2.6, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.4, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.5, memReq, submitTime, 2, runtime)));		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.6, memReq, submitTime, 2, runtime)));		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 6, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, submitTime, 2, runtime)));		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, submitTime, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, submitTime, 2, runtime)));
	}	
	
	/*
	 * There are availableMips for the vm (preempting vms with same priority but
	 * lower difference for SLO target
	 */
	@Test
	public void testIsSuitableFor4() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq/2, memReq, 0, 1, runtime);
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq/2, memReq, 0, 2, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating vms
		policy.allocating(vm0);
		vm0.setStartExec(0);

		policy.allocating(vm1);
		vm1.setStartExec(0);
		
		policy.allocating(vm2);
		vm2.setStartExec(0);
		
		// checking after allocating VMs
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(cpuReq/2, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(cpuReq/2, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2, policy.getPriorityToVms().get(2).first());
		
		//checking availableMipsByPriority
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2.5, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// mocking time 5
		double time = 5d;
		Mockito.when(timeUtil.clock()).thenReturn(time);
		policy.setSimulationTimeUtil(timeUtil);
	
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);

		// new VMs have smaller availabilities
		// vm with priority 0		
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.4, memReq, time, 0, runtime)));	
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.5, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.6, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, time, 0, runtime)));
		
		// slo target for priority 0 is 1 that's because yhe other vm with priority 0 won't be preempted
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.4, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.5, memReq, time, 0, runtime)));		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.6, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, time, 0, runtime)));
		
		// vm with priority 1
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, time, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.4, memReq, time, 1, runtime)));	
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.5, memReq, time, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.6, memReq, time, 1, runtime)));		
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, time, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, time, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, time, 1, runtime)));
		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.4, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.5, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.6, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, time, 1, runtime)));

		// vm with priority 2
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, time, 2, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.4, memReq, time, 2, runtime)));	
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.5, memReq, time, 2, runtime)));
		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 2.6, memReq, time, 2, runtime)));		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.4, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.5, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.6, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, time, 2, runtime)));
	}
	
	/*
	 * There are availableMips for the vm (preempting vms with same priority but
	 * lower difference for SLO target
	 */
	@Test
	public void testIsSuitableFor5() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq/2, memReq, 0, 1, runtime);
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq/2, memReq, 0, 2, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating vms
		policy.allocating(vm0);
		vm0.setStartExec(0);

		policy.allocating(vm1);
		vm1.setStartExec(0);
		
		policy.allocating(vm2);
		vm2.setStartExec(0);
		
		// checking after allocating VMs
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(cpuReq/2, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(cpuReq/2, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2, policy.getPriorityToVms().get(2).first());
		
		//checking availableMipsByPriority
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2.5, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// The time is the same, then the availabilities are the same and must
		// not preempt VMs with the same priority
		double time = 0d;
	
		// checking current vm availabilities
		Assert.assertEquals(0, vm0.getCurrentAvailability(time), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(time), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(time), ACCEPTABLE_DIFFERENCE);

		// new VMs have the same availabilities of running ones
		// vm with priority 0
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.4, memReq, time, 0, runtime)));	
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.5, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.6, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, time, 0, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, time, 0, runtime)));
		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.4, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.5, memReq, time, 0, runtime)));		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.6, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, time, 0, runtime)));		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, time, 0, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, time, 0, runtime)));
		
		// vm with priority 1
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, time, 1, runtime)));
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.4, memReq, time, 1, runtime)));	
		Assert.assertTrue(policy.isSuitableFor(new PreemptableVm(1, 1, 2.5, memReq, time, 1, runtime)));
		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 2.6, memReq, time, 1, runtime)));		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.4, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.5, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.6, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, time, 1, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, time, 1, runtime)));

		// vm with priority 2
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 1, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 2.4, memReq, time, 2, runtime)));	
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 2.5, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 2.6, memReq, time, 2, runtime)));		
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 4, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 4.9, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 5.1, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.4, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.5, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 7.6, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 9.9, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 10.1, memReq, time, 2, runtime)));
		Assert.assertFalse(policy.isSuitableFor(new PreemptableVm(1, 1, 11, memReq, time, 2, runtime)));
	}
	
	/*
	 * There are not availableMips for the vm preempting vms with same priority
	 * because the new VM are not violating the SLO target.
	 */
	@Test
	public void testIsSuitableFor6() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq/2, memReq, 0, 1, runtime);
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq/2, memReq, 0, 2, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating vms
		policy.allocating(vm0);
		vm0.setStartExec(0);

		policy.allocating(vm1);
		vm1.setStartExec(0);
		
		policy.allocating(vm2);
		vm2.setStartExec(0);
		
		// checking after allocating VMs
		Assert.assertEquals(cpuReq, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(cpuReq/2, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(cpuReq/2, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2, policy.getPriorityToVms().get(2).first());
		
		//checking availableMipsByPriority
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2.5, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// The time is passing five units and new VMs are not violating the SLO target
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
	
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);

		double submitTime = 0d;
		// new VMs with priority 0 are violating the SLO target (0.6 < 1.0),
		// then preemptions happen because priorities and SLO targets
		// vm with priority 0
		PreemptableVm arrivingVM = new PreemptableVm(1, 1, 2.5, memReq, submitTime, 0, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertTrue(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 2.6, memReq, submitTime, 0, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertTrue(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 5, memReq, submitTime, 0, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertTrue(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 5.1, memReq, submitTime, 0, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 10, memReq, submitTime, 0, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 10.1, memReq, submitTime, 0, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));
		
		// new VMs with priority 1 are violating the SLO target (0.6 < 0.9),
		// then preemptions happen because priorities and SLO targets
		// vm with priority 1
		arrivingVM = new PreemptableVm(1, 1, 2.5, memReq, submitTime, 1, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertTrue(policy.isSuitableFor(arrivingVM));

		arrivingVM = new PreemptableVm(1, 1, 2.6, memReq, submitTime, 1, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertTrue(policy.isSuitableFor(arrivingVM));		
		
		arrivingVM = new PreemptableVm(1, 1, 5, memReq, submitTime, 1, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertTrue(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 5.1, memReq, submitTime, 1, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 10, memReq, submitTime, 1, runtime);
		arrivingVM.setStartExec(2);		
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 10.1, memReq, submitTime, 1, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));

		// new VMs with priority 2 are violating the SLO target (0.6 > 0.5),
		// then preemptions happen because priorities and SLO targets
		// vm with priority 2
		arrivingVM = new PreemptableVm(1, 1, 2.5, memReq, submitTime, 2, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));

		arrivingVM = new PreemptableVm(1, 1, 2.6, memReq, submitTime, 2, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));		

		arrivingVM = new PreemptableVm(1, 1, 5, memReq, submitTime, 2, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 5.1, memReq, submitTime, 2, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));
		
		arrivingVM = new PreemptableVm(1, 1, 10, memReq, submitTime, 2, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));
		arrivingVM = new PreemptableVm(1, 1, 10.1, memReq, submitTime, 2, runtime);
		arrivingVM.setStartExec(2);
		Assert.assertFalse(policy.isSuitableFor(arrivingVM));
	}
	
	/*
	 * There is only one VM per priority and availability is not important in
	 * this scenario
	 */
	@Test
	public void testNextVmForPreempting() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq/2, memReq, 0, 1, runtime);
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq/2, memReq, 0, 2, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating vms
		policy.allocating(vm0);
		vm0.setStartExec(0);

		policy.allocating(vm1);
		vm1.setStartExec(0);
		
		policy.allocating(vm2);
		vm2.setStartExec(0);
		
		//checking availableMipsByPriority
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2.5, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// The time is passing five units and new VMs are not violating the SLO target
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
	
		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);

		// checking next VM for preempting
		Assert.assertEquals(vm2, policy.nextVmForPreempting());
		
		policy.deallocating(vm2);
		
		//checking availableMipsByPriority
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2.5, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2.5, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// checking next VM for preempting
		Assert.assertEquals(vm1, policy.nextVmForPreempting());
		
		policy.deallocating(vm1);
		
		//checking availableMipsByPriority
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// checking next VM for preempting
		Assert.assertEquals(vm0, policy.nextVmForPreempting());
		
		policy.deallocating(vm0);
		
		//checking availableMipsByPriority
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
	}
	
	/*
	 * There is not VM for preempting
	 */
	@Test
	public void testNextVmForPreempting2() {
		//checking availableMipsByPriority
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// checking there is not VM for preempting
		Assert.assertNull(policy.nextVmForPreempting());
		
		//checking availableMipsByPriority
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
	}
	
	/*
	 * There are more than one VM with the same priority and availability will impact on
	 * this scenario
	 */
	@Test
	public void testNextVmForPreempting3() {
		// Mocking time 5
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		
		policy = new VmAvailabilityBasedPreemptionPolicy(properties, timeUtil);
		policy.setTotalMips(10);
		
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 1;
		
		// creating and allocating VMs in different times
		PreemptableVm vm0P0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);
		vm0P0.setStartExec(0);
		policy.allocating(vm0P0);
		
		PreemptableVm vm1P0 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 0, runtime);
		vm1P0.setStartExec(2);
		policy.allocating(vm1P0);

		PreemptableVm vm2P0 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 0, runtime);
		vm2P0.setStartExec(4);
		policy.allocating(vm2P0);
		
		PreemptableVm vm0P1 = new PreemptableVm(3, 1, cpuReq, memReq, 0, 1, runtime);
		vm0P1.setStartExec(0);
		policy.allocating(vm0P1);

		PreemptableVm vm1P1 = new PreemptableVm(4, 1, cpuReq, memReq, 0, 1, runtime);
		vm1P1.setStartExec(2);
		policy.allocating(vm1P1);
		
		PreemptableVm vm2P1 = new PreemptableVm(5, 1, cpuReq, memReq, 0, 1, runtime);
		vm2P1.setStartExec(4);
		policy.allocating(vm2P1);
		
		PreemptableVm vm0P2 = new PreemptableVm(6, 1, cpuReq, memReq, 0, 2, runtime);
		vm0P2.setStartExec(0);
		policy.allocating(vm0P2);
		
		PreemptableVm vm1P2 = new PreemptableVm(7, 1, cpuReq, memReq, 0, 2, runtime);
		vm1P2.setStartExec(2);
		policy.allocating(vm1P2);
		
		PreemptableVm vm2P2 = new PreemptableVm(8, 1, cpuReq, memReq, 0, 2, runtime);
		vm2P2.setStartExec(4);
		policy.allocating(vm2P2);
		
		// checking after allocating VMs
		Assert.assertEquals(3 * cpuReq, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(3, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm2P0, policy.getPriorityToVms().get(0).first());
		Assert.assertEquals(vm0P0, policy.getPriorityToVms().get(0).last());
		
		Assert.assertEquals(3 * cpuReq, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(3, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm2P1, policy.getPriorityToVms().get(1).first());
		Assert.assertEquals(vm0P1, policy.getPriorityToVms().get(1).last());
		
		Assert.assertEquals(3 * cpuReq, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(3, policy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2P2, policy.getPriorityToVms().get(2).first());
		Assert.assertEquals(vm0P2, policy.getPriorityToVms().get(2).last());
		
		//checking availableMipsByPriority
		Assert.assertEquals(7, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(4, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

	
		// checking current vm availabilities
		Assert.assertEquals(1, vm0P0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm0P1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, vm0P2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		Assert.assertEquals(0.6, vm1P0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.6, vm1P1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.6, vm1P2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		
		Assert.assertEquals(0.2, vm2P0.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.2, vm2P1.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0.2, vm2P2.getCurrentAvailability(5), ACCEPTABLE_DIFFERENCE);

		// Firstly all VMs with priorities 2 will be preempted according to VM
		// availability farer from violating SLO target
		Assert.assertEquals(vm0P2, policy.nextVmForPreempting());		
		policy.deallocating(vm0P2);
		
		//checking availableMipsByPriority
		Assert.assertEquals(7, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(4, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		Assert.assertEquals(vm1P2, policy.nextVmForPreempting());		
		policy.deallocating(vm1P2);
		
		//checking availableMipsByPriority
		Assert.assertEquals(7, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(4, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(3, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// Firstly all VMs with priorities 2 will be preempted
		Assert.assertEquals(vm2P2, policy.nextVmForPreempting());		
		policy.deallocating(vm2P2);
		
		//checking availableMipsByPriority
		Assert.assertEquals(7, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(4, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(4, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// After all VMs with priorities 1 will be preempted according to VM
		// availability farer from violating SLO target
		Assert.assertEquals(vm0P1, policy.nextVmForPreempting());		
		policy.deallocating(vm0P1);
		
		//checking availableMipsByPriority
		Assert.assertEquals(7, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(5, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// availability farer from violating SLO target
		Assert.assertEquals(vm1P1, policy.nextVmForPreempting());		
		policy.deallocating(vm1P1);
		
		//checking availableMipsByPriority
		Assert.assertEquals(7, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(6, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(6, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// availability farer from violating SLO target
		Assert.assertEquals(vm2P1, policy.nextVmForPreempting());		
		policy.deallocating(vm2P1);
		
		//checking availableMipsByPriority
		Assert.assertEquals(7, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(7, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(7, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// Lastly all VMs with priorities 0 will be preempted according to VM
		// availability farer from violating SLO target
		Assert.assertEquals(vm0P0, policy.nextVmForPreempting());		
		policy.deallocating(vm0P0);
		
		//checking availableMipsByPriority
		Assert.assertEquals(8, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(8, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(8, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// availability farer from violating SLO target
		Assert.assertEquals(vm1P0, policy.nextVmForPreempting());		
		policy.deallocating(vm1P0);
		
		//checking availableMipsByPriority
		Assert.assertEquals(9, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(9, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(9, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		// availability farer from violating SLO target
		Assert.assertEquals(vm2P0, policy.nextVmForPreempting());		
		policy.deallocating(vm2P0);
		
		//checking availableMipsByPriority
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10, policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// Finally, there is not more VMs for preempting 
		Assert.assertNull(policy.nextVmForPreempting());		
	}

	@Test
	public void testGetAvailableMipsByPriorityAndAvailability1(){
		double memReq = 0;
		double runtime = 10;
		double cpuReq = 5;

		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 0, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		policy.allocating(vm0);
		vm0.setStartExec(0);

		// No vm can be preempted by availability, so the method return the same as getAvailableMipsByPriority
		Assert.assertEquals(policy.getAvailableMipsByPriorityAndAvailability(0), policy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(policy.getAvailableMipsByPriorityAndAvailability(1), policy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(policy.getAvailableMipsByPriorityAndAvailability(2), policy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
	}


	@Test
	public void testGetAvailableMipsByPriorityAndAvailability2(){
		policy.setTotalMips(20);
		double memReq = 0;
		double runtime = 10;
		double cpuReq = 5;

		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 1, runtime);
		PreemptableVm vm2 = new PreemptableVm(3, 1, cpuReq, memReq, 0, 2, runtime);


		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		policy.allocating(vm0);
		vm0.setStartExec(0);

		Assert.assertEquals(0, vm1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		policy.allocating(vm1);
		vm1.setStartExec(0);

		Assert.assertEquals(0, vm2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		policy.allocating(vm2);
		vm2.setStartExec(0);

		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);

		// All vms have availability = 1

		// Just vm0 can not be preempted
		Assert.assertEquals(policy.getAvailableMipsByPriorityAndAvailability(0), 15, ACCEPTABLE_DIFFERENCE);

		// Just vm0 can not be preempted
		Assert.assertEquals(policy.getAvailableMipsByPriorityAndAvailability(1), 15, ACCEPTABLE_DIFFERENCE);

		// Vm1 isn't considered
		Assert.assertEquals(policy.getAvailableMipsByPriority(1), 10, ACCEPTABLE_DIFFERENCE);

		// vm0 and vm1 can not be preempted
		Assert.assertEquals(policy.getAvailableMipsByPriorityAndAvailability(2), 10, ACCEPTABLE_DIFFERENCE);

		Assert.assertEquals(policy.getAvailableMipsByPriority(2), 5, ACCEPTABLE_DIFFERENCE);
	}


	@Test
	public void testGetAvailableMipsByPriorityAndAvailability3(){
		policy.setTotalMips(20);
		double memReq = 0;
		double runtime = 10;
		double cpuReq = 5;


		PreemptableVm vm1P1 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 1, runtime);
		PreemptableVm vm2P1 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 1, runtime);
		PreemptableVm vm1P2 = new PreemptableVm(2, 1, cpuReq, memReq, 0, 2, runtime);
		PreemptableVm vm2P2 = new PreemptableVm(3, 1, cpuReq, memReq, 0, 2, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm1P1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		policy.allocating(vm1P1);

		// This vm will present availability = 0.9 (To calculate mips available, it's is not meeting SLO target)
		vm1P1.setStartExec(0.5);


		Assert.assertEquals(0, vm2P1.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		policy.allocating(vm2P1);

		// This vm will present availability = 0,9002 (To calculate mips available, it's is meeting SLO target)
		vm2P1.setStartExec(0.499);


		Assert.assertEquals(0, vm1P2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		policy.allocating(vm1P2);

		// This vm will present availability = 0.5 (To calculate mips available, it's is not meeting SLO target)
		vm1P2.setStartExec(2.5);


		Assert.assertEquals(0, vm2P2.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		policy.allocating(vm2P2);

		// This vm will present availability = 0,5002 (To calculate mips available, it's is meeting SLO target)
		vm2P2.setStartExec(2.499);

		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);

		// Everybody can be preempted to allocate a Vm with priority 0
		Assert.assertEquals(policy.getAvailableMipsByPriorityAndAvailability(0), policy.getTotalMips(), ACCEPTABLE_DIFFERENCE);

		// The both vms with priority 2 can be preempted, but just one vm with priority 1 isn't meeting the SLO target
		Assert.assertEquals(policy.getAvailableMipsByPriorityAndAvailability(1), 15, ACCEPTABLE_DIFFERENCE);

		// Just one vm with priority 2 can be preempted, because it's not meeting the SLO target
		Assert.assertEquals(policy.getAvailableMipsByPriorityAndAvailability(2), 5, ACCEPTABLE_DIFFERENCE);
	}

//	
//	double submitTime = 0d;
//	// new VMs with priority 0 are violating the SLO target (0.6 < 1.0),
//	// then preemptions happen because priorities and SLO targets
//	// vm with priority 0
//	PreemptableVm arrivingVM = new PreemptableVm(1, 1, 2.5, memReq, submitTime, 0, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertTrue(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 2.6, memReq, submitTime, 0, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertTrue(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 5, memReq, submitTime, 0, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertTrue(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 5.1, memReq, submitTime, 0, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertTrue(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 10, memReq, submitTime, 0, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertTrue(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 10.1, memReq, submitTime, 0, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));
//	
//	// new VMs with priority 1 are violating the SLO target (0.6 < 0.9),
//	// then preemptions happen because priorities and SLO targets
//	// vm with priority 1
//	arrivingVM = new PreemptableVm(1, 1, 2.5, memReq, submitTime, 1, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertTrue(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 2.6, memReq, submitTime, 1, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertTrue(policy.isSuitableFor(arrivingVM));		
//	
//	arrivingVM = new PreemptableVm(1, 1, 5, memReq, submitTime, 1, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertTrue(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 5.1, memReq, submitTime, 1, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 10, memReq, submitTime, 1, runtime);
//	arrivingVM.setStartExec(2);		
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 10.1, memReq, submitTime, 1, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));
//	
//	// new VMs with priority 2 are violating the SLO target (0.6 > 0.5),
//	// then preemptions happen because priorities and SLO targets
//	// vm with priority 2
//	arrivingVM = new PreemptableVm(1, 1, 2.5, memReq, submitTime, 2, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 2.6, memReq, submitTime, 2, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));		
//	
//	arrivingVM = new PreemptableVm(1, 1, 5, memReq, submitTime, 2, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 5.1, memReq, submitTime, 2, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));
//	
//	arrivingVM = new PreemptableVm(1, 1, 10, memReq, submitTime, 2, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));
//	arrivingVM = new PreemptableVm(1, 1, 10.1, memReq, submitTime, 2, runtime);
//	arrivingVM.setStartExec(2);
//	Assert.assertFalse(policy.isSuitableFor(arrivingVM));
}
