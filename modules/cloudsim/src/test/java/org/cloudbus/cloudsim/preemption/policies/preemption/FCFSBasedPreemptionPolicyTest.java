package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.Properties;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FCFSBasedPreemptionPolicyTest {

	private static final double ACCEPTABLE_DIFFERENCE = 0.00001;
	PreemptionPolicy preemptionPolicy;
	
	@Before
	public void setUp() {
		Properties properties = new Properties();
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
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

}
