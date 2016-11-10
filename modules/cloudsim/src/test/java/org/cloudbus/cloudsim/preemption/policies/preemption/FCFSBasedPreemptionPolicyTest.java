package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FCFSBasedPreemptionPolicyTest {

	private static final double ACCEPTABLE_DIFFERENCE = 0.00001;
	PreemptionPolicy preemptionPolicy;
	Properties properties;
	
	@Before
	public void setUp() {
		properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		preemptionPolicy = new FCFSBasedPreemptionPolicy(properties);
		preemptionPolicy.setTotalMips(10);
		
		// checking initial state
		Assert.assertEquals(3, preemptionPolicy.getNumberOfPriorities());
		Assert.assertEquals(10, preemptionPolicy.getTotalMips(), ACCEPTABLE_DIFFERENCE);
				
		for (int priority = 0; priority < 3; priority++) {
			Assert.assertEquals(0d, preemptionPolicy.getPriorityToInUseMips().get(priority), ACCEPTABLE_DIFFERENCE);
			Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(priority).isEmpty());
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidInitialization() {
		properties.setProperty(
				PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "-1");
		new FCFSBasedPreemptionPolicy(properties);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidInitialization2() {
		properties.setProperty(
				PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "0");
		new FCFSBasedPreemptionPolicy(properties);
	}
	
	@Test
	public void testDefaultInitialization() {
		properties = new Properties();
		PreemptionPolicy policy = new FCFSBasedPreemptionPolicy(properties);
		
		Assert.assertEquals(3, policy.getNumberOfPriorities());
	}

	@Test
	public void testAllocating(){
		double memReq = 0;
		double submitTime = 0;
		double runtime = 0;
		
		PreemptableVm vm = new PreemptableVm(1, 1, 5, memReq, submitTime, 0, runtime);
		
		preemptionPolicy.allocating(vm);
		
		// checking after allocating vm with priority 0
		Assert.assertEquals(5, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm, preemptionPolicy.getPriorityToVms().get(0).first());
		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(2).isEmpty());
	}
	
	@Test
	public void testAllocating2(){
		double memReq = 0;
		double submitTime = 0;
		double runtime = 0;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, memReq, submitTime, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(1, 1, 3, memReq, submitTime, 1, runtime);
		PreemptableVm vm2 = new PreemptableVm(2, 1, 2, memReq, submitTime, 2, runtime);
		
		preemptionPolicy.allocating(vm0);
		
		// checking after allocating VM with priority 0
		Assert.assertEquals(5, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, preemptionPolicy.getPriorityToVms().get(0).first());

		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(1).isEmpty());		
		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(2).isEmpty());
		
		preemptionPolicy.allocating(vm1);
		
		// checking after allocating VM with priority 0 and 1
		Assert.assertEquals(5, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, preemptionPolicy.getPriorityToVms().get(0).first());

		Assert.assertEquals(3, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, preemptionPolicy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(2).isEmpty());
		
		preemptionPolicy.allocating(vm2);

		// checking after allocating VM with priority 0, 1 and 2
		Assert.assertEquals(5, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, preemptionPolicy.getPriorityToVms().get(0).first());

		Assert.assertEquals(3, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, preemptionPolicy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(2, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2, preemptionPolicy.getPriorityToVms().get(2).first());
	}
	
	@Test
	public void testDeallocating(){
		double memReq = 0;
		double submitTime = 0;
		double runtime = 0;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, memReq, submitTime, 0, runtime);
		
		preemptionPolicy.allocating(vm0);
		
		// checking after allocating VM with priority 0
		Assert.assertEquals(5, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, preemptionPolicy.getPriorityToVms().get(0).first());

		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(1).isEmpty());		
		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(2).isEmpty());
		
		preemptionPolicy.deallocating(vm0);
		
		// checking after deallocating VM with priority 0
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(0).isEmpty());

		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(1).isEmpty());		
		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(2).isEmpty());
	}
	
	@Test
	public void testDeallocating2(){
		double memReq = 0;
		double submitTime = 0;
		double runtime = 0;
		
		PreemptableVm vm0 = new PreemptableVm(0, 1, 5, memReq, submitTime, 0, runtime);
		PreemptableVm vm1 = new PreemptableVm(1, 1, 3, memReq, submitTime, 1, runtime);
		PreemptableVm vm2 = new PreemptableVm(2, 1, 2, memReq, submitTime, 2, runtime);
		
		preemptionPolicy.allocating(vm0);
		preemptionPolicy.allocating(vm1);
		preemptionPolicy.allocating(vm2);
		
		// checking after allocating VM with priority 0, 1 and 2
		Assert.assertEquals(5, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, preemptionPolicy.getPriorityToVms().get(0).first());

		Assert.assertEquals(3, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, preemptionPolicy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(2, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2, preemptionPolicy.getPriorityToVms().get(2).first());
		
		// deallocating vm0
		preemptionPolicy.deallocating(vm0);
		
		// checking after deallocating VM with priority 0		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(0).isEmpty());

		Assert.assertEquals(3, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, preemptionPolicy.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(2, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2, preemptionPolicy.getPriorityToVms().get(2).first());	
	
		// deallocating vm1
		preemptionPolicy.deallocating(vm1);
		
		// checking after deallocating VM with priority 1		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(0).isEmpty());

		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(2, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(2).size());
		Assert.assertEquals(vm2, preemptionPolicy.getPriorityToVms().get(2).first());	

		// deallocating vm2
		preemptionPolicy.deallocating(vm2);
		
		// checking after deallocating VM with priority 2		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(0).isEmpty());

		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(1).isEmpty());
		
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(preemptionPolicy.getPriorityToVms().get(2).isEmpty());
	}
	
	@Test
	public void testIsSuitableFor() {
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		preemptionPolicy = new FCFSBasedPreemptionPolicy(properties);
		
		double cpuReq = 1.0;

		int totalVms = 20;
		int freeCapacity = 5;
		
		preemptionPolicy.setTotalMips(totalVms + freeCapacity);
		
		// allocating Vms
		for (int id = 0; id < totalVms; id++) {
			if (id % 2 == 0) {
				preemptionPolicy.allocating(new PreemptableVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				preemptionPolicy.allocating(new PreemptableVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}
		}

		// checking
		Assert.assertEquals(2, preemptionPolicy.getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, preemptionPolicy.getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, preemptionPolicy.getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().size());
		Assert.assertEquals(10, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(10, preemptionPolicy.getPriorityToVms().get(1).size());
		
		Assert.assertEquals(freeCapacity + 10 * cpuReq, preemptionPolicy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(freeCapacity, preemptionPolicy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		
		// checking if is suitable for priority 1
		for (int requiredMips = 1; requiredMips <= freeCapacity; requiredMips++) {
			Assert.assertTrue(preemptionPolicy.isSuitableFor(new PreemptableVm(100, 1, requiredMips, 1.0, 0, 1, 0)));
		}

		Assert.assertFalse(preemptionPolicy.isSuitableFor(new PreemptableVm(100, 1, freeCapacity + 1, 1.0, 0, 1, 0)));

		// checking if is suitable for priority 0
		for (int requiredMips = 1; requiredMips <= freeCapacity
				+ (totalVms / 2); requiredMips++) {
			Assert.assertTrue(preemptionPolicy.isSuitableFor(new PreemptableVm(100, 1, requiredMips, 1.0, 0, 0, 0)));
		}

		Assert.assertFalse(preemptionPolicy.isSuitableFor(new PreemptableVm(100, 1,
				freeCapacity + (totalVms / 2) + 1, 1.0, 0, 0, 0)));
	}
	
	@Test
	public void testIsSuitableFor2() {
		//testing with double
		double cpuReq = 1.0;

		int totalVms = 20;
		double freeCapacity = 0.5;

		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		preemptionPolicy = new FCFSBasedPreemptionPolicy(properties);
		preemptionPolicy.setTotalMips(totalVms + freeCapacity);
		
		// allocating Vms
		for (int id = 0; id < totalVms; id++) {
			if (id % 2 == 0) {
				preemptionPolicy.allocating(new PreemptableVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				preemptionPolicy.allocating(new PreemptableVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}
		}

		// checking
		Assert.assertEquals(2, preemptionPolicy.getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, preemptionPolicy.getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, preemptionPolicy.getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().size());
		Assert.assertEquals(10, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(10, preemptionPolicy.getPriorityToVms().get(1).size());

		Assert.assertEquals(freeCapacity + 10 * cpuReq, preemptionPolicy.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(freeCapacity, preemptionPolicy.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

		// checking if is suitable for priority 1
		for (int requiredMips = 1; requiredMips <= freeCapacity; requiredMips++) {
			Assert.assertTrue(preemptionPolicy.isSuitableFor(new PreemptableVm(100, 1, requiredMips, 1.0, 0, 1, 0)));
		}

		Assert.assertFalse(preemptionPolicy.isSuitableFor(new PreemptableVm(100, 1, freeCapacity + 1, 1.0, 0, 1, 0)));

		// checking if is suitable for priority 0
		for (int requiredMips = 1; requiredMips <= freeCapacity
				+ (totalVms / 2); requiredMips++) {
			Assert.assertTrue(preemptionPolicy.isSuitableFor(new PreemptableVm(100, 1, requiredMips, 1.0, 0, 0, 0)));
		}

		Assert.assertFalse(preemptionPolicy.isSuitableFor(new PreemptableVm(100, 1,
				freeCapacity + (totalVms / 2) + 1, 1.0, 0, 0, 0)));
	}
	
	@Test
	public void testIsSuitableFor3() {

		preemptionPolicy.setTotalMips(100.5);
		
		int vmId = 0;

		PreemptableVm vm0_1 = new PreemptableVm(vmId++, 1, 23.7, 1.0, 0, 0, 0);
		PreemptableVm vm0_2 = new PreemptableVm(vmId++, 1, 26.3, 1.0, 0.2, 0, 0);

		PreemptableVm vm1_1 = new PreemptableVm(vmId++, 1, 24.3, 1.0, 0, 1, 0);
		PreemptableVm vm1_2 = new PreemptableVm(vmId++, 1, 0.7, 1.0, 0.1, 1, 0);

		PreemptableVm vm2_1 = new PreemptableVm(vmId++, 1,24.99, 1.0, 0, 2, 0);
		PreemptableVm vm2_2 = new PreemptableVm(vmId++, 1, 0.01, 1.0, 0.1, 2, 0);

		preemptionPolicy.allocating(vm0_1);
		preemptionPolicy.allocating(vm0_2);
		preemptionPolicy.allocating(vm1_1);
		preemptionPolicy.allocating(vm1_2);
		preemptionPolicy.allocating(vm2_1);
		preemptionPolicy.allocating(vm2_2);
		
		final int VM_ID = 7;
		final int USER_ID = 7;

		Assert.assertFalse(preemptionPolicy.isSuitableFor(null));

		//testing initial state
		PreemptableVm vm2_3 = new PreemptableVm(VM_ID, USER_ID, 0.000001, 0, 0, 2, 0);
		Assert.assertTrue(preemptionPolicy.isSuitableFor(vm2_3));

		PreemptableVm vm2_4 = new PreemptableVm(VM_ID, USER_ID, 0.6, 0, 0, 2, 0);
		Assert.assertFalse(preemptionPolicy.isSuitableFor(vm2_4));

		PreemptableVm vm1_3 = new PreemptableVm(VM_ID, USER_ID, 25.5, 0, 0, 1, 0);
		Assert.assertTrue(preemptionPolicy.isSuitableFor(vm1_3));

		PreemptableVm vm1_4 = new PreemptableVm(VM_ID, USER_ID, 25.5000001, 0, 0, 1, 0);
		Assert.assertFalse(preemptionPolicy.isSuitableFor(vm1_4));

		PreemptableVm vm0_3 = new PreemptableVm(VM_ID, USER_ID, 50.5, 0, 0, 0, 0);
		Assert.assertTrue(preemptionPolicy.isSuitableFor(vm0_3));

		PreemptableVm vm0_4 = new PreemptableVm(VM_ID, USER_ID, 50.5000001, 0, 0, 0, 0);
		Assert.assertFalse(preemptionPolicy.isSuitableFor(vm0_4));

		//testing after destroy a vm with priority 0
		preemptionPolicy.deallocating(vm0_1); //available mips equals 24.2
		Assert.assertEquals(24.2, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		vm2_3 = new PreemptableVm(VM_ID, USER_ID, 24.19999, 0, 0, 2, 0);
		Assert.assertTrue(preemptionPolicy.isSuitableFor(vm2_3));

		vm2_4 = new PreemptableVm(VM_ID, USER_ID, 24.200001, 0, 0, 2, 0);
		Assert.assertFalse(preemptionPolicy.isSuitableFor(vm2_4));

		vm1_3 = new PreemptableVm(VM_ID, USER_ID, 49.2, 0, 0, 1, 0);
		Assert.assertTrue(preemptionPolicy.isSuitableFor(vm1_3));

		vm1_4 = new PreemptableVm(VM_ID, USER_ID, 49.3, 0, 0, 1, 0);
		Assert.assertFalse(preemptionPolicy.isSuitableFor(vm1_4));

		vm0_3 = new PreemptableVm(VM_ID, USER_ID, 74.2, 0, 0, 0, 0);
		Assert.assertTrue(preemptionPolicy.isSuitableFor(vm0_3));

		vm0_4 = new PreemptableVm(VM_ID, USER_ID, 75, 0, 0, 0, 0);
		Assert.assertFalse(preemptionPolicy.isSuitableFor(vm0_4));

		// testing after destroy a vm with priority 1
		preemptionPolicy.deallocating(vm1_2);
		Assert.assertEquals(24.9, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		vm2_3 = new PreemptableVm(VM_ID, USER_ID, 24.9, 0, 0, 2, 0);
		Assert.assertTrue(preemptionPolicy.isSuitableFor(vm2_3));

		vm2_4 = new PreemptableVm(VM_ID, USER_ID, 25.000001, 0, 0, 2, 0);
		Assert.assertFalse(preemptionPolicy.isSuitableFor(vm2_4));

		vm1_3 = new PreemptableVm(VM_ID, USER_ID, 49.9, 0, 0, 1, 0);
		Assert.assertTrue(preemptionPolicy.isSuitableFor(vm1_3));

		vm1_4 = new PreemptableVm(VM_ID, USER_ID, 50, 0, 0, 1, 0);
		Assert.assertFalse(preemptionPolicy.isSuitableFor(vm1_4));

		vm0_3 = new PreemptableVm(VM_ID, USER_ID, 74.2, 0, 0, 0, 0);
		Assert.assertTrue(preemptionPolicy.isSuitableFor(vm0_3));

		vm0_4 = new PreemptableVm(VM_ID, USER_ID, 75, 0, 0, 0, 0);
		Assert.assertFalse(preemptionPolicy.isSuitableFor(vm0_4));
	}
	
	@Test
	public void tesNextVmForPreempting() {
		// setting environment
		Map<Integer, Double> priorityToMipsInUse = new HashMap<Integer, Double>();
		Map<Integer, SortedSet<PreemptableVm>> priorityToVms = new HashMap<Integer, SortedSet<PreemptableVm>>();
		double cpuReq = 1.0;

		//priority 0
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		priorityToMipsInUse.put(0, cpuReq);
		SortedSet<PreemptableVm> priority0Vms = new TreeSet<PreemptableVm>();
		priority0Vms.add(vm0);
		priorityToVms.put(0, priority0Vms);
		
		// priority 1
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq, 1.0, 0, 1, 0);
		priorityToMipsInUse.put(1, cpuReq);
		SortedSet<PreemptableVm> priority1Vms = new TreeSet<PreemptableVm>();
		priority1Vms.add(vm1);
		priorityToVms.put(1, priority1Vms);
		
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		preemptionPolicy = new FCFSBasedPreemptionPolicy(properties);
		preemptionPolicy.setTotalMips(100);
		
		//host is empty
		Assert.assertNull(preemptionPolicy.nextVmForPreempting());

		preemptionPolicy.setPriorityToInUseMips(priorityToMipsInUse);
		preemptionPolicy.setPriorityToVms(priorityToVms);
		
		// checking
		Assert.assertEquals(2, preemptionPolicy.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().size());
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(1).size());
		
		// preempting
		Assert.assertEquals(vm1, preemptionPolicy.nextVmForPreempting());
		
		// simulating removing vm1
		priorityToMipsInUse.put(1, 0d);
		priority1Vms = new TreeSet<PreemptableVm>();
		priorityToVms.put(1, priority1Vms);
		
		preemptionPolicy.setPriorityToInUseMips(priorityToMipsInUse);
		preemptionPolicy.setPriorityToVms(priorityToVms);

		// checking
		Assert.assertEquals(2, preemptionPolicy.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, preemptionPolicy.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, preemptionPolicy.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().size());
		Assert.assertEquals(1, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(1).size());
		// preempting
		Assert.assertEquals(vm0, preemptionPolicy.nextVmForPreempting());

		// removing all vms
		preemptionPolicy.deallocating(vm0);
		preemptionPolicy.deallocating(vm1);

		//host is empty
		Assert.assertNull(preemptionPolicy.nextVmForPreempting());
	}
	
	@Test
	public void testNextVmForPreempting2(){
		preemptionPolicy.setTotalMips(100.5);
		
		int vmId = 0;

		PreemptableVm vm0_1 = new PreemptableVm(vmId++, 1, 23.7, 1.0, 0, 0, 0);
		PreemptableVm vm0_2 = new PreemptableVm(vmId++, 1, 26.3, 1.0, 0.2, 0, 0);

		PreemptableVm vm1_1 = new PreemptableVm(vmId++, 1, 24.3, 1.0, 0, 1, 0);
		PreemptableVm vm1_2 = new PreemptableVm(vmId++, 1, 0.7, 1.0, 0.1, 1, 0);

		PreemptableVm vm2_1 = new PreemptableVm(vmId++, 1,24.99, 1.0, 0, 2, 0);
		PreemptableVm vm2_2 = new PreemptableVm(vmId++, 1, 0.01, 1.0, 0.1, 2, 0);

		preemptionPolicy.allocating(vm0_1);
		preemptionPolicy.allocating(vm0_2);
		preemptionPolicy.allocating(vm1_1);
		preemptionPolicy.allocating(vm1_2);
		preemptionPolicy.allocating(vm2_1);
		preemptionPolicy.allocating(vm2_2);
		
		// checking availableMips on host
		Assert.assertEquals(0.5, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2_2, preemptionPolicy.nextVmForPreempting());

		// checking number of vms for each priority
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(2).size());
		
		// allocating a new vm with priority 0 
		PreemptableVm vmTest = new PreemptableVm(7, 1, 0.3, 1.0, 0.2, 0, 0);		
		preemptionPolicy.allocating(vmTest);
		
		// checking number of vms for each priority
		Assert.assertEquals(3, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(2).size());		

		// checking availableMips on host
		Assert.assertEquals(0.2, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2_2, preemptionPolicy.nextVmForPreempting());

		// deallocating vmTest
		preemptionPolicy.deallocating(vmTest);

		// checking number of vms for each priority
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(2).size());
		
		// allocating a new vm with priority 2
		PreemptableVm vm2_3 = new PreemptableVm(7, 1, 0.1, 1.0, 0.2, 2, 0);
		preemptionPolicy.allocating(vm2_3);
		
		// checking number of vms for each priority
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(3, preemptionPolicy.getPriorityToVms().get(2).size());

		// checking availableMips on host
		Assert.assertEquals(0.4, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2_3, preemptionPolicy.nextVmForPreempting());

		// deallocating vm with priority 2
		preemptionPolicy.deallocating(vm2_2);
		
		// checking number of vms for each priority
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(2).size());
		
		Assert.assertEquals(0.41, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2_3, preemptionPolicy.nextVmForPreempting());

		// deallocating all vms with priority 2
		preemptionPolicy.deallocating(vm2_3);
		preemptionPolicy.deallocating(vm2_1);
		
		// checking number of vms for each priority
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(2).size());
		
		Assert.assertEquals(25.5, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm1_2, preemptionPolicy.nextVmForPreempting());

		// allocating a new Vm with priority 1
		PreemptableVm vm1_3 = new PreemptableVm(7, 1, 25.5, 1.0, 0.1001, 1, 0);
		preemptionPolicy.allocating(vm1_3);
		
		// checking number of vms for each priority
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(3, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(2).size());
		
		Assert.assertEquals(0, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm1_3, preemptionPolicy.nextVmForPreempting());

		// deallocating vm with priority 1
		preemptionPolicy.deallocating(vm1_1);
		
		// checking number of vms for each priority
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(2).size());
		
		Assert.assertEquals(24.3, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm1_3, preemptionPolicy.nextVmForPreempting());
		
		// deallocating all vms with priority 1
		preemptionPolicy.deallocating(vm1_2);
		preemptionPolicy.deallocating(vm1_3);

		// checking number of vms for each priority
		Assert.assertEquals(2, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(2).size());
		
		Assert.assertEquals(50.5, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_2, preemptionPolicy.nextVmForPreempting());

		// allocating a new Vm with priority 0
		PreemptableVm vm0_3 = new PreemptableVm(7, 1, 25.5, 1.0, 0.2, 0, 0);
		preemptionPolicy.allocating(vm0_3);
		
		// checking number of vms for each priority
		Assert.assertEquals(3, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(2).size());
		
		Assert.assertEquals(25, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_3, preemptionPolicy.nextVmForPreempting());

		// allocating a new vm with priority 0 and submitTime before the last one
		PreemptableVm vm0_4 = new PreemptableVm(8, 1, 24.9, 1.0, 0.1, 0, 0);
		preemptionPolicy.allocating(vm0_4);
		
		// checking number of vms for each priority
		Assert.assertEquals(4, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(2).size());
		
		Assert.assertEquals(0.1, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_3, preemptionPolicy.nextVmForPreempting());

		// trying to allocate null Vm
		preemptionPolicy.allocating(null);
		
		// checking number of vms for each priority
		Assert.assertEquals(4, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(2).size());
		
		Assert.assertEquals(0.1, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_3, preemptionPolicy.nextVmForPreempting());
			
		// deallocating vms with priority 0
		preemptionPolicy.deallocating(vm0_3);
		
		Assert.assertEquals(25.6, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_2, preemptionPolicy.nextVmForPreempting());
				
		preemptionPolicy.deallocating(vm0_2);
		
		Assert.assertEquals(51.9, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_4, preemptionPolicy.nextVmForPreempting());
		
		preemptionPolicy.deallocating(vm0_4);
		
		Assert.assertEquals(76.8, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_1, preemptionPolicy.nextVmForPreempting());
		
		preemptionPolicy.deallocating(vm0_1);
		
		Assert.assertEquals(100.5, preemptionPolicy.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertNull(preemptionPolicy.nextVmForPreempting());
		
		// checking number of vms for each priority
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(1).size());
		Assert.assertEquals(0, preemptionPolicy.getPriorityToVms().get(2).size());

	}
}
