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
	
	Properties propeties;
	VmAvailabilityBasedPremmptionPolicy policy;
	SimulationTimeUtil timeUtil;
	
	@Before
	public void setUp() {
		propeties = new Properties();
		propeties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "1");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		policy = new VmAvailabilityBasedPremmptionPolicy(propeties);
		policy.setTotalMips(10);
		
		timeUtil = Mockito.mock(SimulationTimeUtil.class);
	}
	
	@Test
	public void testInitialization() {
		policy = new VmAvailabilityBasedPremmptionPolicy(propeties);
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
		Properties propeties = new Properties();
		propeties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "1");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPremmptionPolicy(propeties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization2() {
		// setting properties
		Properties propeties = new Properties();
		propeties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "1");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
				
		new VmAvailabilityBasedPremmptionPolicy(propeties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization3() {
		// setting properties
		Properties propeties = new Properties();
		propeties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "0");
				
		new VmAvailabilityBasedPremmptionPolicy(propeties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization4() {
		// setting properties
		Properties propeties = new Properties();
		propeties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "-1");
				
		new VmAvailabilityBasedPremmptionPolicy(propeties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization5() {
		// setting properties
		Properties propeties = new Properties();
		propeties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "zero", "1");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPremmptionPolicy(propeties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization6() {
		// setting properties
		Properties propeties = new Properties();
		propeties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "one");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPremmptionPolicy(propeties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization7() {
		// setting properties
		Properties propeties = new Properties();
		propeties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "-1");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPremmptionPolicy(propeties);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidInitialization8() {
		// setting properties
		Properties propeties = new Properties();
		propeties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "0.0", "1");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
		propeties.setProperty(VmAvailabilityBasedPremmptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");
				
		new VmAvailabilityBasedPremmptionPolicy(propeties);
	}
		
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 0, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		policy.allocating(vm0);
		vm0.setStartExec(0);
		
		// checking after allocating vm with priority 0
		Assert.assertEquals(5, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		// mocking time 5 
		Mockito.when(timeUtil.clock()).thenReturn(5d);
		policy.setSimulationTimeUtil(timeUtil);
		
		PreemptableVm vm1 = new PreemptableVm(2, 1, cpuReq, memReq, 5, 0, runtime);
		
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
		
		// assert two first vms would be preempted by third one
		Assert.assertEquals(2 * cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vm2), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testCalcMipsOfSamePriorityToBeAvailable3() {
		double memReq = 0;
		double runtime = 10;				
		double cpuReq = 5;
		
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 0, runtime);

		// checking current vm availability
		Assert.assertEquals(0, vm0.getCurrentAvailability(0), ACCEPTABLE_DIFFERENCE);
		
		// allocating both vms
		policy.allocating(vm0);
		vm0.setStartExec(0);
		
		// checking after allocating VM with priority 0
		Assert.assertEquals(5, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, policy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(2).isEmpty());
		
		// mocking time 2
		Mockito.when(timeUtil.clock()).thenReturn(2d);
		policy.setSimulationTimeUtil(timeUtil);
				
		PreemptableVm vm1 = new PreemptableVm(2, 1, cpuReq, memReq, 2, 0, runtime);

		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(2), ACCEPTABLE_DIFFERENCE);
		
		// allocating second VM
		policy.allocating(vm1);
		vm1.setStartExec(2);
		
		// checking after allocating second VM with priority 0
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
		
		PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, memReq, 5, 0, runtime);
		
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
		
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, memReq, 0, 1, runtime);

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
		
		// mocking time 2
		Mockito.when(timeUtil.clock()).thenReturn(2d);
		policy.setSimulationTimeUtil(timeUtil);
				
		PreemptableVm vm1 = new PreemptableVm(2, 1, cpuReq, memReq, 2, 1, runtime);

		// checking current vm availabilities
		Assert.assertEquals(1, vm0.getCurrentAvailability(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getCurrentAvailability(2), ACCEPTABLE_DIFFERENCE);
		
		// allocating second VM
		policy.allocating(vm1);
		vm1.setStartExec(2);
		
		// checking after allocating second VM with priority 1
		Assert.assertEquals(0, policy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(policy.getPriorityToVms().get(0).isEmpty());
	
		Assert.assertEquals(9.1, policy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, policy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm0, policy.getPriorityToVms().get(1).first());
		Assert.assertEquals(vm1, policy.getPriorityToVms().get(1).last());
		
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
		Assert.assertEquals(cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vmP0), ACCEPTABLE_DIFFERENCE);

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
		Assert.assertEquals(cpuReq, policy.calcMipsOfSamePriorityToBeAvailable(vmP0), ACCEPTABLE_DIFFERENCE);

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
}
