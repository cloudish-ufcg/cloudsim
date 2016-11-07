package org.cloudbus.cloudsim.preemption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PreemptiveHostTest {

	private static final double ACCEPTABLE_DIFFERENCE = 0.00000001;
	private static final int HOST_ID = 0;
	private static final int NUMBER_OF_PRIORITIES = 3;
	Properties properties;

	@Before
	public void setUp(){
		
		properties = new Properties();
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
	}

	@Test
	public void testInitializing() {
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "1");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));
		
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitializingWithInvalidPriority() {
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "0");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitializingWithInvalidPriority2() {
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "-1");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));
	}
	
	@Test
	public void testCompareTo() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));
		
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		PreemptiveHost host2 = new PreemptiveHost(2, peList2,
				new VmSchedulerMipsBased(peList2), new FCFSBasedPreemptionPolicy(properties));
		
		Assert.assertEquals(1, host1.compareTo(host2));
		Assert.assertEquals(-1, host2.compareTo(host1));
	}
	
	@Test
	public void testOrdering() {
		// host 1
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));
		
		// host 2
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		PreemptiveHost host2 = new PreemptiveHost(2, peList2, new VmSchedulerMipsBased(
				peList2), new FCFSBasedPreemptionPolicy(properties));

		// host 3
		List<Pe> peList3 = new ArrayList<Pe>();
		peList3.add(new Pe(0, new PeProvisionerSimple(700)));
		PreemptiveHost host3 = new PreemptiveHost(3, peList3, new VmSchedulerMipsBased(
				peList3), new FCFSBasedPreemptionPolicy(properties));

		// host 4
		List<Pe> peList4 = new ArrayList<Pe>();
		peList4.add(new Pe(0, new PeProvisionerSimple(900)));
		PreemptiveHost host4 = new PreemptiveHost(4, peList4, new VmSchedulerMipsBased(
				peList4), new FCFSBasedPreemptionPolicy(properties));
			
		// checking sorting
		SortedSet<Host> hosts = new TreeSet<Host>();
		hosts.add(host4);
		hosts.add(host2);
		hosts.add(host3);
		
		Assert.assertEquals(3, hosts.size());
		Assert.assertEquals(host4, hosts.first());
		Assert.assertEquals(host2, hosts.last());
		
		// adding one more host
		hosts.add(host1);
		
		Assert.assertEquals(4, hosts.size());
		Assert.assertEquals(host4, hosts.first());
		Assert.assertEquals(host1, hosts.last());
	}
	
	@Test
	public void testNextVmForPreempting() {
		// setting environment
		Map<Integer, Double> priorityToMipsInUse = new HashMap<Integer, Double>();
		Map<Integer, SortedSet<Vm>> priorityToVms = new HashMap<Integer, SortedSet<Vm>>();
		double cpuReq = 1.0;

		//priority 0
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		priorityToMipsInUse.put(0, cpuReq);
		SortedSet<Vm> priority0Vms = new TreeSet<Vm>();
		priority0Vms.add(vm0);
		priorityToVms.put(0, priority0Vms);
		
		// priority 1
		PreemptableVm vm1 = new PreemptableVm(1, 1, cpuReq, 1.0, 0, 1, 0);
		priorityToMipsInUse.put(1, cpuReq);
		SortedSet<Vm> priority1Vms = new TreeSet<Vm>();
		priority1Vms.add(vm1);
		priorityToVms.put(1, priority1Vms);
		
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

		//host is empty
		Assert.assertNull(host1.nextVmForPreempting());

		host1.getPreemptionPolicy().setPriorityToInUseMips(priorityToMipsInUse);
		host1.getPreemptionPolicy().setPriorityToVms(priorityToVms);
		
		// checking
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());
		
		// preempting
		Assert.assertEquals(vm1, host1.nextVmForPreempting());
		
		// simulating removing vm1
		priorityToMipsInUse.put(1, 0d);
		priority1Vms = new TreeSet<Vm>();
		priorityToVms.put(1, priority1Vms);
		
		host1.getPreemptionPolicy().setPriorityToInUseMips(priorityToMipsInUse);
		host1.getPreemptionPolicy().setPriorityToVms(priorityToVms);

		// checking
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());

		// preempting
		Assert.assertEquals(vm0, host1.nextVmForPreempting());

		// removing all vms
		host1.vmDestroy(vm0);
		host1.vmDestroy(vm1);

		//host is empty
		Assert.assertNull(host1.nextVmForPreempting());
	}

	@Test
	public void testVmCreate() {
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		double cpuReq = 1.0;
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));
		
		// checking initial environment
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());
		
		// creating vm0 (priority 0)
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		
		Assert.assertTrue(host1.vmCreate(vm0));

		// checking environment
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, host1.getPreemptionPolicy().getPriorityToVms().get(0).first());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());
		
		Assert.assertEquals(100 - cpuReq, host1.getAvailableMips(),ACCEPTABLE_DIFFERENCE);
		
		// creating vm1 (priority 1)
		PreemptableVm vm1 = new PreemptableVm(2, 1, cpuReq, 1.0, 0, 1, 0);
		
		Assert.assertTrue(host1.vmCreate(vm1));

		// checking environment
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, host1.getPreemptionPolicy().getPriorityToVms().get(0).first());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, host1.getPreemptionPolicy().getPriorityToVms().get(1).first());
		
		Assert.assertEquals(100 - 2 * cpuReq, host1.getAvailableMips(),ACCEPTABLE_DIFFERENCE);
	}

	@Test
	public void testVmCreate2() {

		double ACCETABLE_DIFFERENCE = 0.000000000000001;

		int id = 0;
		int userId = 1;
		double cpuReq = 0.00000001;
		double memReq = 0;
		double subTime = 0;
		int priority = 1;
		double runTime = 0.4;

		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(5 * cpuReq)));
		VmScheduler schedulerMipsBased = new VmSchedulerMipsBased(peList1);

		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		Host googleHost = new PreemptiveHost(id, peList1, schedulerMipsBased, new FCFSBasedPreemptionPolicy(properties));

		Vm vm1 = new PreemptableVm(id++, userId, 1 * cpuReq, memReq, subTime, priority - 1, runTime);
		Vm vm2 = new PreemptableVm(id++, userId, 5 * cpuReq, memReq, subTime, priority - 1 , runTime);
		Vm vm3 = new PreemptableVm(id++, userId, 2 * cpuReq, memReq, subTime, priority, runTime);
		Vm vm4 = new PreemptableVm(id++, userId, 3 * cpuReq, memReq, subTime, priority, runTime);
		Vm vm5 = new PreemptableVm(id++, userId, 4 * cpuReq, memReq, subTime, priority + 1, runTime);
		Vm vm6 = new PreemptableVm(id++, userId, 6 * cpuReq, memReq, subTime, priority + 1, runTime);

		Assert.assertEquals(5 * cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm1));
		Assert.assertEquals(4*cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertFalse(googleHost.vmCreate(vm2));
		Assert.assertEquals(4*cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm3));
		Assert.assertEquals(2*cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		googleHost.vmDestroy(vm3);
		Assert.assertEquals(4*cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm5));
		Assert.assertEquals(0*cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		googleHost.vmDestroy(vm1);
		Assert.assertEquals(1*cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		googleHost.vmDestroy(vm5);
		Assert.assertEquals(5*cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertFalse(googleHost.vmCreate(vm6));
		Assert.assertEquals(5*cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm4));
		Assert.assertEquals(2*cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);
	}

	@Test
	public void testVmCreate3(){

		double ACCETABLE_DIFFERENCE = 0.000000000000001;

		int id = 0;
		int userId = 1;
		double cpuReq = 0.00000001;
		double memReq = 0;
		double subTime = 0;
		int priority = 1;
		double runTime = 0.4;

		double cpuCapacity = 6603.25;

		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(cpuCapacity)));
		VmScheduler vmSchedulerMipsBased = new VmSchedulerMipsBased(peList1);

		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		Host googleHost = new PreemptiveHost(id, peList1, vmSchedulerMipsBased, new FCFSBasedPreemptionPolicy(properties));

		Vm vm1 = new PreemptableVm(id++, userId, 1 * cpuReq, memReq, subTime, priority - 1, runTime);
		Vm vm2 = new PreemptableVm(id++, userId, 5 * cpuReq, memReq, subTime, priority - 1 , runTime);
		Vm vm3 = new PreemptableVm(id++, userId, 2 * cpuReq, memReq, subTime, priority, runTime);
		Vm vm4 = new PreemptableVm(id++, userId, 3 * cpuReq, memReq, subTime, priority, runTime);
		Vm vm5 = new PreemptableVm(id++, userId, 4 * cpuReq, memReq, subTime, priority + 1, runTime);
		Vm vm6 = new PreemptableVm(id++, userId, 6 * cpuReq, memReq, subTime, priority + 1, runTime);

		Assert.assertEquals(cpuCapacity, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm1));
		Assert.assertEquals(cpuCapacity - cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm2));
		Assert.assertEquals(cpuCapacity - (6 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm3));
		Assert.assertEquals(cpuCapacity - (8 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		googleHost.vmDestroy(vm2);
		Assert.assertEquals(cpuCapacity - (3 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		// detect imprecision of 12 decimal places in available mips
		Assert.assertTrue(googleHost.vmCreate(vm4));
        Assert.assertEquals(cpuCapacity - (6 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm5));
        Assert.assertEquals(cpuCapacity - (10 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		googleHost.vmDestroy(vm1);
        Assert.assertEquals(cpuCapacity - (9 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm6));
        Assert.assertEquals(cpuCapacity - (15 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		googleHost.vmDestroy(vm3);
		googleHost.vmDestroy(vm4);
		googleHost.vmDestroy(vm5);
		googleHost.vmDestroy(vm6);


	}
	
	@Test
	public void testVmCreateWithNull() {
		properties.setProperty(
				FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1),
				new FCFSBasedPreemptionPolicy(properties));

		Assert.assertFalse(host1.vmCreate(null));
	}

		@Test
	public void testVmDestroy() {
		
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

		double cpuReq = 1.0;
		
		PreemptableVm vm0 = new PreemptableVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		PreemptableVm vm1 = new PreemptableVm(2, 1, cpuReq, 1.0, 0, 1, 0);

		Assert.assertTrue(host1.vmCreate(vm0));
		Assert.assertTrue(host1.vmCreate(vm1));
		
		// checking initial environment
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, host1.getPreemptionPolicy().getPriorityToVms().get(0).first());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, host1.getPreemptionPolicy().getPriorityToVms().get(1).first());
		
		Assert.assertEquals(100 - 2 * cpuReq, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

		// destroying vm0
		host1.vmDestroy(vm0);
		
		// checking
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(1, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, host1.getPreemptionPolicy().getPriorityToVms().get(1).first());
		
		Assert.assertEquals(100 - cpuReq, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);		

		// destroying vm1
		host1.vmDestroy(vm1);
		
		// checking
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(0, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());

		Assert.assertEquals(100, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);		
	}
	
	@Test
	public void testIsSuitableFor() {
		double cpuReq = 1.0;

		int totalVms = 20;
		int freeCapacity = 5;
		
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(totalVms + freeCapacity)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
				peList1), new FCFSBasedPreemptionPolicy(properties));

		for (int id = 0; id < totalVms; id++) {
			if (id % 2 == 0) {
				host1.vmCreate(new PreemptableVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				host1.vmCreate(new PreemptableVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}
		}

		// checking
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(10, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(10, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());
		
		Assert.assertEquals(freeCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);		
		
		// checking if is suitable for priority 1
		for (int requiredMips = 1; requiredMips <= freeCapacity; requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new PreemptableVm(100, 1, requiredMips, 1.0, 0, 1, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new PreemptableVm(100, 1, freeCapacity + 1, 1.0, 0, 1, 0)));

		// checking if is suitable for priority 0
		for (int requiredMips = 1; requiredMips <= freeCapacity
				+ (totalVms / 2); requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new PreemptableVm(100, 1, requiredMips, 1.0, 0, 0, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new PreemptableVm(100, 1,
				freeCapacity + (totalVms / 2) + 1, 1.0, 0, 0, 0)));
	}
	
	@Test
	public void testHashCode(){

		// creating hosts
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
				peList1), new FCFSBasedPreemptionPolicy(properties));

		PreemptiveHost host2 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(
				peList1), new FCFSBasedPreemptionPolicy(properties));

		// assert expected hashcode
		Assert.assertEquals(1, host1.hashCode());
		Assert.assertEquals(2, host2.hashCode());

		// comparing hashcode of different hosts
		Assert.assertFalse(host1.hashCode() == host2.hashCode());
	}
	
	@Test
	public void testGetAvailableMipsByPriority(){
		// creating hosts
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100.5)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
				peList1), new FCFSBasedPreemptionPolicy(properties));
		
		//priority 0
		PreemptableVm vm0 = new PreemptableVm(0, 1, 50, 1.0, 0, 0, 0);
				
		Assert.assertTrue(host1.vmCreate(vm0));
		
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
	
		//priority 1
		PreemptableVm vm1 = new PreemptableVm(1, 1, 20, 1.0, 0, 1, 0);

		Assert.assertTrue(host1.vmCreate(vm1)); 
		
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		//priority 1
		PreemptableVm vm2 = new PreemptableVm(2, 1, 20, 1.0, 0, 1, 0);

		Assert.assertTrue(host1.vmCreate(vm2)); 
		
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// destroying vm1
		host1.vmDestroy(vm1);
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		//priority 2
		PreemptableVm vm3 = new PreemptableVm(3, 1, 20, 1.0, 0, 2, 0);

		Assert.assertTrue(host1.vmCreate(vm3));

		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// creating vm4 with less than 1 mips, with priority 0
		PreemptableVm vm4 = new PreemptableVm(4, 1, 0.01, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm4));

		Assert.assertEquals(50.49, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.49, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10.49, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// creating vm5 with less than 1 mips, with priority 1
		PreemptableVm vm5 = new PreemptableVm(5, 1, 0.01, 1.0, 0, 1, 0);
		Assert.assertTrue(host1.vmCreate(vm5));

		Assert.assertEquals(50.49, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.48, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10.48, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// creating vm6 with less than 1 mips, with priority 2
		PreemptableVm vm6 = new PreemptableVm(6, 1, 0.01, 1.0, 0, 2, 0);
		Assert.assertTrue(host1.vmCreate(vm6));
		Assert.assertEquals(50.49, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.48, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10.47, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// destroying vm0
		host1.vmDestroy(vm0);
		Assert.assertEquals(100.49, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(80.48, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(60.47, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// creating vm7
		PreemptableVm vm7 = new PreemptableVm(7, 1, 60.47, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm7));
		Assert.assertEquals(40.02, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(20.01, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// asserting that is not possible create vm8 because vmCreate method is not responsible by preempt Vms with low priority
		PreemptableVm vm8 = new PreemptableVm(7, 1, 40.02, 1.0, 0, 0, 0);
		Assert.assertFalse(host1.vmCreate(vm8));
	}

	@Test
	public void testGetAvailableMipsByPriority2() {
		// setting environment
		Map<Integer, Double> priorityToMipsInUse = new HashMap<Integer, Double>();
		Map<Integer, SortedSet<Vm>> priorityToVms = new HashMap<Integer, SortedSet<Vm>>();
		double cpuReq = 1.0;

		SortedSet<Vm> priority0Vms = new TreeSet<Vm>();
		SortedSet<Vm> priority1Vms = new TreeSet<Vm>();

		for (int id = 0; id < 20; id++) {
			if (id % 2 == 0) {
				priority0Vms.add(new PreemptableVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				priority1Vms.add(new PreemptableVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}
		}

		priorityToMipsInUse.put(0, 10 * cpuReq);
		priorityToMipsInUse.put(1, 10 * cpuReq);

		priorityToVms.put(0,priority0Vms);
		priorityToVms.put(1,priority1Vms);

		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "2");
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		PreemptiveHost host1 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
				peList1), new FCFSBasedPreemptionPolicy(properties));

		host1.getPreemptionPolicy().setPriorityToInUseMips(priorityToMipsInUse);
		host1.getPreemptionPolicy().setPriorityToVms(priorityToVms);

		// checking
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, host1.getPreemptionPolicy().getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPreemptionPolicy().getPriorityToVms().size());
		Assert.assertEquals(10, host1.getPreemptionPolicy().getPriorityToVms().get(0).size());
		Assert.assertEquals(10, host1.getPreemptionPolicy().getPriorityToVms().get(1).size());

		// checking
		Assert.assertEquals(80, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(90, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

		//checking with vmCreate method

		//allocating vm with priority 0
		for (int i = 0; i < 10; i++) {
			int priority = 0;
			int id = 20 + i;
			double cpuReq2 = 0.1;
			Assert.assertEquals(90 - (cpuReq2 * i), host1.getAvailableMipsByPriority(priority), ACCEPTABLE_DIFFERENCE);
			Vm gVm = new PreemptableVm(id, 1, cpuReq2, 1.0, 0, priority, 0);
			host1.vmCreate(gVm);
		}

		//allocating vm with priority 1
		for (int i = 0; i < 10; i++) {
			int priority = 1;
			int id = 30 + i;
			double cpuReq2 = 0.1;
			Assert.assertEquals(89, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
			Assert.assertEquals(79 - (cpuReq2 * i), host1.getAvailableMipsByPriority(priority), ACCEPTABLE_DIFFERENCE);
			Vm gVm = new PreemptableVm(id, 1, cpuReq2, 1.0, 0, priority, 0);
			host1.vmCreate(gVm);
		}
	}

	@Test
	public void testDecimalAccuracy(){

		// setting a new acceptable difference considering accuracy
		double NEW_ACCEPTABLE_DIFFERENCE = 0.000000001;

		// creating a new host with capacity that will be rounded to 1
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, String.valueOf(NUMBER_OF_PRIORITIES));
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(1.0000000001))); // round to 1
		PreemptiveHost host1 = new PreemptiveHost(HOST_ID + 1, peList2,
				new VmSchedulerMipsBased(peList2), new FCFSBasedPreemptionPolicy(properties));

		// creating a new host with capacity that won't be rounded to 1
		List<Pe> peList3 = new ArrayList<Pe>();
		peList3.add(new Pe(0, new PeProvisionerSimple(1.000000001)));
		PreemptiveHost host2 = new PreemptiveHost(HOST_ID + 2, peList3,
				new VmSchedulerMipsBased(peList3), new FCFSBasedPreemptionPolicy(properties));

		// vm1 with mips required = 1
		PreemptableVm vm1 = new PreemptableVm(0, 1, 1, 1.0, 0, 0, 0);

		// asserting that vm1 can be allocated at host1 and host2
		Assert.assertTrue(host1.vmCreate(vm1));
		Assert.assertTrue(host2.vmCreate(vm1));

		// asserting new available mips of two hosts
		Assert.assertEquals(host1.getAvailableMips(), 0, NEW_ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(host2.getAvailableMips(), 1.0E-9, NEW_ACCEPTABLE_DIFFERENCE);

		// destroying vm1 from hosts to do new testes and asserting the new available mips
		host1.vmDestroy(vm1);
		Assert.assertEquals(host1.getAvailableMips(), 1, NEW_ACCEPTABLE_DIFFERENCE);
		host2.vmDestroy(vm1);
		Assert.assertEquals(host2.getAvailableMips(), 1 + 1.0E-9, NEW_ACCEPTABLE_DIFFERENCE);

		// vm1 with mips required that can not be allocated at host1, but can be allocated at host2
		vm1 = new PreemptableVm(0, 1, 1.000000001, 1.0, 0, 0, 0);
		Assert.assertFalse(host1.vmCreate(vm1));
		Assert.assertTrue(host2.vmCreate(vm1));

		// asserting new available mips of two hosts
		Assert.assertEquals(host1.getAvailableMips(), 1, NEW_ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(host2.getAvailableMips(), 0, NEW_ACCEPTABLE_DIFFERENCE);

		// destroying vm1 from host2 to do new testes and asserting the new available mips
		host2.vmDestroy(vm1);
		Assert.assertEquals(host2.getAvailableMips(), 1 + 1.0E-9, NEW_ACCEPTABLE_DIFFERENCE);

		// vm1 with mips required bigger than host1 and host2 capacity
		vm1 = new PreemptableVm(0, 1, 1.0000000016, 1.0, 0, 0, 0);
		Assert.assertFalse(host1.vmCreate(vm1));
		Assert.assertFalse(host2.vmCreate(vm1));
		Assert.assertEquals(host1.getAvailableMips(), 1, NEW_ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(host2.getAvailableMips(), 1 + 1.0E-9, NEW_ACCEPTABLE_DIFFERENCE);


		// vm1 with mips required bigger than host1 and host2 capacity
		vm1 = new PreemptableVm(0, 1, 1.0000000011, 1.0, 0, 0, 0);
		Assert.assertFalse(host1.vmCreate(vm1));
		Assert.assertFalse(host2.vmCreate(vm1));
		Assert.assertEquals(host1.getAvailableMips(), 1, NEW_ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(host2.getAvailableMips(), 1 + 1.0E-9, NEW_ACCEPTABLE_DIFFERENCE);


	}

	@Test
	public void testDecimalAccuracy2(){
		// setting a new acceptable difference considering accuracy
		double NEW_ACCEPTABLE_DIFFERENCE = 0.000000001;

		// creating a new host with capacity that will be rounded to 1
		properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, String.valueOf(NUMBER_OF_PRIORITIES));
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(1.0000000001))); // round to 1
		PreemptiveHost host1 = new PreemptiveHost(HOST_ID + 1, peList2,
				new VmSchedulerMipsBased(peList2), new FCFSBasedPreemptionPolicy(properties));

		// creating a new host with capacity that won't be rounded to 1
		List<Pe> peList3 = new ArrayList<Pe>();
		peList3.add(new Pe(0, new PeProvisionerSimple(1.000000001)));
		PreemptiveHost host2 = new PreemptiveHost(HOST_ID + 2, peList3,
				new VmSchedulerMipsBased(peList3), new FCFSBasedPreemptionPolicy(properties));

		// setting vms
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1, 1.0, 0, 0, 0);
		PreemptableVm vm2 = new PreemptableVm(2, 1, 0.999999999, 1.0, 0, 0, 0);
		PreemptableVm vm3 = new PreemptableVm(3, 1, 0.000000001, 1.0, 0, 0, 0);

		// asserting that vm2 can be allocated at host1 and new capacity of host
		Assert.assertTrue(host1.vmCreate(vm2));
		Assert.assertEquals(host1.getAvailableMips(), 0.000000001, NEW_ACCEPTABLE_DIFFERENCE);

		// asserting that vm3 can be allocated at host1 and new capacity of host
		Assert.assertTrue(host1.vmCreate(vm3));
		Assert.assertEquals(host1.getAvailableMips(), 0, NEW_ACCEPTABLE_DIFFERENCE);

		// destroying vm2 and vm3 from host1 to do new testes and asserting the new available mips
		host1.vmDestroy(vm2);
		Assert.assertEquals(host1.getAvailableMips(), 0.999999999, NEW_ACCEPTABLE_DIFFERENCE);
		host1.vmDestroy(vm3);
		Assert.assertEquals(host1.getAvailableMips(), 1, NEW_ACCEPTABLE_DIFFERENCE);

		// same test, but now replacing insertion order of vms (first vm3 and second vm2)
		Assert.assertTrue(host1.vmCreate(vm3));
		Assert.assertEquals(host1.getAvailableMips(), 0.999999999, NEW_ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(host1.vmCreate(vm2));
		Assert.assertEquals(host1.getAvailableMips(), 0, NEW_ACCEPTABLE_DIFFERENCE);

		// asserting that vm1 can be allocated at host2 and new capacity of host
		Assert.assertTrue(host2.vmCreate(vm1));
		Assert.assertEquals(host2.getAvailableMips(), 0.000000001, NEW_ACCEPTABLE_DIFFERENCE);

		// asserting that vm3 can be allocated at host2 and new capacity of host
		Assert.assertTrue(host2.vmCreate(vm3));
		Assert.assertEquals(host2.getAvailableMips(), 0, NEW_ACCEPTABLE_DIFFERENCE);


		// destroying vm1 and vm3 from host2 to do new testes and asserting the new available mips
		host2.vmDestroy(vm1);
		Assert.assertEquals(host2.getAvailableMips(), 1, NEW_ACCEPTABLE_DIFFERENCE);
		host2.vmDestroy(vm3);
		Assert.assertEquals(host2.getAvailableMips(), 1.000000001, NEW_ACCEPTABLE_DIFFERENCE);

		// same test, but now replacing insertion order of vms (first vm3 and second vm1)
		Assert.assertTrue(host2.vmCreate(vm3));
		Assert.assertEquals(host2.getAvailableMips(), 1, NEW_ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(host2.vmCreate(vm1));
		Assert.assertEquals(host2.getAvailableMips(), 0, NEW_ACCEPTABLE_DIFFERENCE);
	}
}