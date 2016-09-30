package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.misc.VM;

public class GoogleHostTest {

	private static final double ACCEPTABLE_DIFFERENCE = 0.00000001;
	private static GoogleHost host;
	private static final int HOST_ID = 0;
	private static final int NUMBER_OF_PRIORITIES = 3;
	private static final double HOST_CAPACITY = 100.5;
	private static GoogleVm vm0_1, vm0_2, vm1_1, vm1_2, vm2_1, vm2_2;

	@Before
	public void setUp(){
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(HOST_CAPACITY)));
		host = new GoogleHost(HOST_ID, peList1,
				new VmSchedulerMipsBased(peList1), NUMBER_OF_PRIORITIES);

		int numberOfVMs = 0;

		vm0_1 = new GoogleVm(numberOfVMs++, 1, 23.7, 1.0, 0, 0, 0);
		vm0_2 = new GoogleVm(numberOfVMs++, 1, 26.3, 1.0, 0.2, 0, 0);

		vm1_1 = new GoogleVm(numberOfVMs++, 1, 24.3, 1.0, 0, 1, 0);
		vm1_2 = new GoogleVm(numberOfVMs++, 1, 0.7, 1.0, 0.1, 1, 0);


		vm2_1 = new GoogleVm(numberOfVMs++, 1,24.99, 1.0, 0, 2, 0);
		vm2_2 = new GoogleVm(numberOfVMs++, 1, 0.01, 1.0, 0.1, 2, 0);

		Assert.assertTrue(host.vmCreate(vm0_1));
		Assert.assertTrue(host.vmCreate(vm0_2));
		Assert.assertTrue(host.vmCreate(vm1_1));
		Assert.assertTrue(host.vmCreate(vm1_2));
		Assert.assertTrue(host.vmCreate(vm2_1));
		Assert.assertTrue(host.vmCreate(vm2_2));
	}


	@Test
	public void testInitializing() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 1);
		
		Assert.assertEquals(1, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToInUseMips().size());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitializingWithInvalidPriority() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		new GoogleHost(1, peList1, new VmSchedulerMipsBased(peList1), 0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitializingWithInvalidPriority2() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		new GoogleHost(1, peList1, new VmSchedulerMipsBased(peList1), -1);
	}
	
	@Test
	public void testCompareTo() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 1);
		
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		GoogleHost host2 = new GoogleHost(2, peList2,
				new VmSchedulerMipsBased(peList2), 1);
		
		Assert.assertEquals(1, host1.compareTo(host2));
		Assert.assertEquals(-1, host2.compareTo(host1));
	}
	
	@Test
	public void testOrdering() {
		// host 1
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 1);
		
		// host 2
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		GoogleHost host2 = new GoogleHost(2, peList2, new VmSchedulerMipsBased(
				peList2), 1);

		// host 3
		List<Pe> peList3 = new ArrayList<Pe>();
		peList3.add(new Pe(0, new PeProvisionerSimple(700)));
		GoogleHost host3 = new GoogleHost(3, peList3, new VmSchedulerMipsBased(
				peList3), 1);

		// host 4
		List<Pe> peList4 = new ArrayList<Pe>();
		peList4.add(new Pe(0, new PeProvisionerSimple(900)));
		GoogleHost host4 = new GoogleHost(4, peList4, new VmSchedulerMipsBased(
				peList4), 1);
			
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
		GoogleVm vm0 = new GoogleVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		priorityToMipsInUse.put(0, cpuReq);
		SortedSet<Vm> priority0Vms = new TreeSet<Vm>();
		priority0Vms.add(vm0);
		priorityToVms.put(0, priority0Vms);
		
		// priority 1
		GoogleVm vm1 = new GoogleVm(1, 1, cpuReq, 1.0, 0, 1, 0);
		priorityToMipsInUse.put(1, cpuReq);
		SortedSet<Vm> priority1Vms = new TreeSet<Vm>();
		priority1Vms.add(vm1);
		priorityToVms.put(1, priority1Vms);
				
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 2);

		//host is empty
		Assert.assertNull(host1.nextVmForPreempting());

		host1.setPriorityToInUseMips(priorityToMipsInUse);
		host1.setPriorityToVms(priorityToVms);
		
		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(1).size());
		
		// preempting
		Assert.assertEquals(vm1, host1.nextVmForPreempting());
		
		// simulating removing vm1
		priorityToMipsInUse.put(1, 0d);
		priority1Vms = new TreeSet<Vm>();
		priorityToVms.put(1, priority1Vms);
		
		host1.setPriorityToInUseMips(priorityToMipsInUse);
		host1.setPriorityToVms(priorityToVms);

		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(1).size());

		// preempting
		Assert.assertEquals(vm0, host1.nextVmForPreempting());

		// removing all vms
		host1.vmDestroy(vm0);
		host1.vmDestroy(vm1);

		//host is empty
		Assert.assertNull(host1.nextVmForPreempting());
	}

	@Test
	public void testNextVmForPreempting2(){
		Assert.assertEquals(0.5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2_2, host.nextVmForPreempting());

		GoogleVm vmTest = new GoogleVm(7, 1, 0.3, 1.0, 0.2, 0, 0);
		Assert.assertTrue(host.vmCreate(vmTest));
		Assert.assertEquals(0.2, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2_2, host.nextVmForPreempting());

		host.vmDestroy(vmTest);

		GoogleVm vm2_3 = new GoogleVm(7, 1, 0.1, 1.0, 0.2, 2, 0);
		Assert.assertTrue(host.vmCreate(vm2_3));
		Assert.assertEquals(0.4, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2_3, host.nextVmForPreempting());

		host.vmDestroy(vm2_2);
		Assert.assertEquals(0.41, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2_3, host.nextVmForPreempting());

		host.vmDestroy(vm2_3);
		host.vmDestroy(vm2_1);
		Assert.assertEquals(25.5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm1_2, host.nextVmForPreempting());

		GoogleVm bigVM = new GoogleVm(7, 1, 25.6, 1.0, 0.1001, 1, 0);
		Assert.assertFalse(host.vmCreate(bigVM));
		Assert.assertEquals(25.5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm1_2, host.nextVmForPreempting());

		GoogleVm vm1_3 = new GoogleVm(7, 1, 25.5, 1.0, 0.1001, 1, 0);
		Assert.assertTrue(host.vmCreate(vm1_3));
		Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm1_3, host.nextVmForPreempting());

		host.vmDestroy(vm1_1);
		Assert.assertEquals(24.3, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm1_3, host.nextVmForPreempting());

		host.vmDestroy(vm1_2);
		host.vmDestroy(vm1_3);
		Assert.assertEquals(50.5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_2, host.nextVmForPreempting());

		GoogleVm vm0_3 = new GoogleVm(7, 1, 25.5, 1.0, 0.2, 0, 0);
		Assert.assertTrue(host.vmCreate(vm0_3));
		Assert.assertEquals(25, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_3, host.nextVmForPreempting());


		GoogleVm vm0_4 = new GoogleVm(8, 1, 24.9, 1.0, 0.1, 0, 0);
		Assert.assertTrue(host.vmCreate(vm0_4));
		Assert.assertEquals(0.1, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_3, host.nextVmForPreempting());

		Assert.assertFalse(host.vmCreate(null));
		Assert.assertEquals(0.1, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_3, host.nextVmForPreempting());

		host.vmDestroy(vm0_3);
		Assert.assertEquals(25.6, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_2, host.nextVmForPreempting());

		host.vmDestroy(vm0_2);
		Assert.assertEquals(51.9, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_4, host.nextVmForPreempting());

		host.vmDestroy(vm0_4);
		Assert.assertEquals(76.8, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm0_1, host.nextVmForPreempting());

		host.vmDestroy(vm0_1);
		Assert.assertEquals(100.5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		Assert.assertNull(host.nextVmForPreempting());
	}


	@Test
	public void testVmCreate() {
		double cpuReq = 1.0;
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 2);
		
		// checking initial environment
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(1).size());
		
		// creating vm0 (priority 0)
		GoogleVm vm0 = new GoogleVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		
		Assert.assertTrue(host1.vmCreate(vm0));

		// checking environment
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, host1.getPriorityToVms().get(0).first());
		Assert.assertEquals(0, host1.getPriorityToVms().get(1).size());
		
		Assert.assertEquals(100 - cpuReq, host1.getAvailableMips(),ACCEPTABLE_DIFFERENCE);
		
		// creating vm1 (priority 1)
		GoogleVm vm1 = new GoogleVm(2, 1, cpuReq, 1.0, 0, 1, 0);
		
		Assert.assertTrue(host1.vmCreate(vm1));

		// checking environment
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, host1.getPriorityToVms().get(0).first());
		Assert.assertEquals(1, host1.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, host1.getPriorityToVms().get(1).first());
		
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

		Host googleHost = new GoogleHost(id, peList1, schedulerMipsBased, 3);

		Vm vm1 = new GoogleVm(id++, userId, 1 * cpuReq, memReq, subTime, priority - 1, runTime);
		Vm vm2 = new GoogleVm(id++, userId, 5 * cpuReq, memReq, subTime, priority - 1 , runTime);
		Vm vm3 = new GoogleVm(id++, userId, 2 * cpuReq, memReq, subTime, priority, runTime);
		Vm vm4 = new GoogleVm(id++, userId, 3 * cpuReq, memReq, subTime, priority, runTime);
		Vm vm5 = new GoogleVm(id++, userId, 4 * cpuReq, memReq, subTime, priority + 1, runTime);
		Vm vm6 = new GoogleVm(id++, userId, 6 * cpuReq, memReq, subTime, priority + 1, runTime);

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

		Host googleHost = new GoogleHost(id, peList1, vmSchedulerMipsBased, 3);

		Vm vm1 = new GoogleVm(id++, userId, 1 * cpuReq, memReq, subTime, priority - 1, runTime);
		Vm vm2 = new GoogleVm(id++, userId, 5 * cpuReq, memReq, subTime, priority - 1 , runTime);
		Vm vm3 = new GoogleVm(id++, userId, 2 * cpuReq, memReq, subTime, priority, runTime);
		Vm vm4 = new GoogleVm(id++, userId, 3 * cpuReq, memReq, subTime, priority, runTime);
		Vm vm5 = new GoogleVm(id++, userId, 4 * cpuReq, memReq, subTime, priority + 1, runTime);
		Vm vm6 = new GoogleVm(id++, userId, 6 * cpuReq, memReq, subTime, priority + 1, runTime);

		Assert.assertEquals(cpuCapacity, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm1));
		Assert.assertEquals(cpuCapacity - cpuReq, googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm2));
		Assert.assertEquals(cpuCapacity - (6 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm3));
		Assert.assertEquals(cpuCapacity - (8 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);
		System.out.println(googleHost.getAvailableMips());

		googleHost.vmDestroy(vm2);
		Assert.assertEquals(cpuCapacity - (3 * cpuReq), googleHost.getAvailableMips(), ACCETABLE_DIFFERENCE);
		System.out.println(googleHost.getAvailableMips());

		// detect imprecision of 12 decimal places in available mips
		Assert.assertTrue(googleHost.vmCreate(vm4));
		System.out.println(googleHost.getAvailableMips());
//        Assert.assertEquals(cpuCapacity - (6 * cpuReq), schedulerMipsBased.getAvailableMips(), ACCETABLE_DIFFERENCE);

		Assert.assertTrue(googleHost.vmCreate(vm5));
//        Assert.assertEquals(cpuCapacity - (10 * cpuReq), schedulerMipsBased.getAvailableMips(), ACCETABLE_DIFFERENCE);
		System.out.println(googleHost.getAvailableMips());

		googleHost.vmDestroy(vm1);
//        Assert.assertEquals(cpuCapacity - (9 * cpuReq), schedulerMipsBased.getAvailableMips(), ACCETABLE_DIFFERENCE);
		System.out.println(googleHost.getAvailableMips());

		Assert.assertTrue(googleHost.vmCreate(vm6));
//        Assert.assertEquals(cpuCapacity - (15 * cpuReq), schedulerMipsBased.getAvailableMips(), ACCETABLE_DIFFERENCE);
		System.out.println(googleHost.getAvailableMips());

		googleHost.vmDestroy(vm3);
		System.out.println(googleHost.getAvailableMips());
		googleHost.vmDestroy(vm4);
		System.out.println(googleHost.getAvailableMips());
		googleHost.vmDestroy(vm5);
		System.out.println(googleHost.getAvailableMips());
		googleHost.vmDestroy(vm6);
		System.out.println(googleHost.getAvailableMips());


	}

		@Test
	public void testVmDestroy() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1,
				new VmSchedulerMipsBased(peList1), 2);

		double cpuReq = 1.0;
		
		GoogleVm vm0 = new GoogleVm(1, 1, cpuReq, 1.0, 0, 0, 0);
		GoogleVm vm1 = new GoogleVm(2, 1, cpuReq, 1.0, 0, 1, 0);

		Assert.assertTrue(host1.vmCreate(vm0));
		Assert.assertTrue(host1.vmCreate(vm1));
		
		// checking initial environment
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(vm0, host1.getPriorityToVms().get(0).first());
		Assert.assertEquals(1, host1.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, host1.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(100 - 2 * cpuReq, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

		// destroying vm0
		host1.vmDestroy(vm0);
		
		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(cpuReq, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(1, host1.getPriorityToVms().get(1).size());
		Assert.assertEquals(vm1, host1.getPriorityToVms().get(1).first());
		
		Assert.assertEquals(100 - cpuReq, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);		

		// destroying vm1
		host1.vmDestroy(vm1);
		
		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getPriorityToInUseMips().get(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(0, host1.getPriorityToVms().get(1).size());

		Assert.assertEquals(100, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);		
	}
	
	@Test
	public void testIsSuitableFor() {
		double cpuReq = 1.0;

		int totalVms = 20;
		int freeCapacity = 5;
		
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(totalVms + freeCapacity)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		for (int id = 0; id < totalVms; id++) {
			if (id % 2 == 0) {
				host1.vmCreate(new GoogleVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				host1.vmCreate(new GoogleVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}
		}

		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(1).size());
		
		Assert.assertEquals(freeCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);		
		
		// checking if is suitable for priority 1
		for (int requiredMips = 1; requiredMips <= freeCapacity; requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new GoogleVm(100, 1, requiredMips, 1.0, 0, 1, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new GoogleVm(100, 1, freeCapacity + 1, 1.0, 0, 1, 0)));

		// checking if is suitable for priority 0
		for (int requiredMips = 1; requiredMips <= freeCapacity
				+ (totalVms / 2); requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new GoogleVm(100, 1, requiredMips, 1.0, 0, 0, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new GoogleVm(100, 1,
				freeCapacity + (totalVms / 2) + 1, 1.0, 0, 0, 0)));
	}

	@Test
	public void testIsSuitableFor2(){

		//testing with double
		double cpuReq = 1.0;

		int totalVms = 20;
		double freeCapacity = 0.5;

		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(totalVms + freeCapacity)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		for (int id = 0; id < totalVms; id++) {
			if (id % 2 == 0) {
				host1.vmCreate(new GoogleVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				host1.vmCreate(new GoogleVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}
		}

		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(1).size());

		Assert.assertEquals(freeCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

		// checking if is suitable for priority 1
		for (int requiredMips = 1; requiredMips <= freeCapacity; requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new GoogleVm(100, 1, requiredMips, 1.0, 0, 1, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new GoogleVm(100, 1, freeCapacity + 1, 1.0, 0, 1, 0)));

		// checking if is suitable for priority 0
		for (int requiredMips = 1; requiredMips <= freeCapacity
				+ (totalVms / 2); requiredMips++) {
			Assert.assertTrue(host1.isSuitableForVm(new GoogleVm(100, 1, requiredMips, 1.0, 0, 0, 0)));
		}

		Assert.assertFalse(host1.isSuitableForVm(new GoogleVm(100, 1,
				freeCapacity + (totalVms / 2) + 1, 1.0, 0, 0, 0)));

	}

	@Test
	public void testIsSuitableFor3(){

		final int VM_ID = 7;
		final int USER_ID = 7;

		Assert.assertFalse(host.isSuitableForVm(null));

		//testing initial state
		Vm vm2_3 = new GoogleVm(VM_ID, USER_ID, 0.000001, 0, 0, 2, 0);
		Assert.assertTrue(host.isSuitableForVm(vm2_3));

		Vm vm2_4 = new GoogleVm(VM_ID, USER_ID, 0.6, 0, 0, 2, 0);
		Assert.assertFalse(host.isSuitableForVm(vm2_4));

		Vm vm1_3 = new GoogleVm(VM_ID, USER_ID, 25.5, 0, 0, 1, 0);
		Assert.assertTrue(host.isSuitableForVm(vm1_3));

		Vm vm1_4 = new GoogleVm(VM_ID, USER_ID, 25.5000001, 0, 0, 1, 0);
		Assert.assertFalse(host.isSuitableForVm(vm1_4));

		Vm vm0_3 = new GoogleVm(VM_ID, USER_ID, 50.5, 0, 0, 0, 0);
		Assert.assertTrue(host.isSuitableForVm(vm0_3));

		Vm vm0_4 = new GoogleVm(VM_ID, USER_ID, 50.5000001, 0, 0, 0, 0);
		Assert.assertFalse(host.isSuitableForVm(vm0_4));

		//testing after destroy a vm with priority 0
		host.vmDestroy(vm0_1); //available mips equals 24.2
		Assert.assertEquals(24.2, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

		vm2_3 = new GoogleVm(VM_ID, USER_ID, 24.19999, 0, 0, 2, 0);
		Assert.assertTrue(host.isSuitableForVm(vm2_3));

		vm2_4 = new GoogleVm(VM_ID, USER_ID, 24.200001, 0, 0, 2, 0);
		Assert.assertFalse(host.isSuitableForVm(vm2_4));

		vm1_3 = new GoogleVm(VM_ID, USER_ID, 49.2, 0, 0, 1, 0);
		Assert.assertTrue(host.isSuitableForVm(vm1_3));

		vm1_4 = new GoogleVm(VM_ID, USER_ID, 49.3, 0, 0, 1, 0);
		Assert.assertFalse(host.isSuitableForVm(vm1_4));

		vm0_3 = new GoogleVm(VM_ID, USER_ID, 74.2, 0, 0, 0, 0);
		Assert.assertTrue(host.isSuitableForVm(vm0_3));

		vm0_4 = new GoogleVm(VM_ID, USER_ID, 75, 0, 0, 0, 0);
		Assert.assertFalse(host.isSuitableForVm(vm0_4));

		// testing after destroy a vm with priority 1
		host.vmDestroy(vm1_2);
		Assert.assertEquals(24.9, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

		vm2_3 = new GoogleVm(VM_ID, USER_ID, 24.9, 0, 0, 2, 0);
		Assert.assertTrue(host.isSuitableForVm(vm2_3));

		vm2_4 = new GoogleVm(VM_ID, USER_ID, 25.000001, 0, 0, 2, 0);
		Assert.assertFalse(host.isSuitableForVm(vm2_4));

		vm1_3 = new GoogleVm(VM_ID, USER_ID, 49.9, 0, 0, 1, 0);
		Assert.assertTrue(host.isSuitableForVm(vm1_3));

		vm1_4 = new GoogleVm(VM_ID, USER_ID, 50, 0, 0, 1, 0);
		Assert.assertFalse(host.isSuitableForVm(vm1_4));

		vm0_3 = new GoogleVm(VM_ID, USER_ID, 74.2, 0, 0, 0, 0);
		Assert.assertTrue(host.isSuitableForVm(vm0_3));

		vm0_4 = new GoogleVm(VM_ID, USER_ID, 75, 0, 0, 0, 0);
		Assert.assertFalse(host.isSuitableForVm(vm0_4));
	}

	@Test
	public void testHashCode(){

		// creating hosts
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		GoogleHost host2 = new GoogleHost(2, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		// assert expected hashcode
		Assert.assertEquals(1, host1.hashCode());
		Assert.assertEquals(2, host2.hashCode());

		// comparing hashcode of different hosts
		Assert.assertFalse(host1.hashCode() == host2.hashCode());
	}
	
	@Test
	public void testGetAvailableMipsByPriority(){
		// creating hosts
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100.5)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 3);
		
		//priority 0
		GoogleVm vm0 = new GoogleVm(0, 1, 50, 1.0, 0, 0, 0);
				
		Assert.assertTrue(host1.vmCreate(vm0));
		
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
	
		//priority 1
		GoogleVm vm1 = new GoogleVm(1, 1, 20, 1.0, 0, 1, 0);

		Assert.assertTrue(host1.vmCreate(vm1)); 
		
		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
		
		//priority 1
		GoogleVm vm2 = new GoogleVm(2, 1, 20, 1.0, 0, 1, 0);

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
		GoogleVm vm3 = new GoogleVm(3, 1, 20, 1.0, 0, 2, 0);

		Assert.assertTrue(host1.vmCreate(vm3));

		Assert.assertEquals(50.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// creating vm4 with less than 1 mips, with priority 0
		GoogleVm vm4 = new GoogleVm(4, 1, 0.01, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm4));

		Assert.assertEquals(50.49, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.49, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10.49, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// creating vm5 with less than 1 mips, with priority 1
		GoogleVm vm5 = new GoogleVm(5, 1, 0.01, 1.0, 0, 1, 0);
		Assert.assertTrue(host1.vmCreate(vm5));

		Assert.assertEquals(50.49, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(30.48, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10.48, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// creating vm6 with less than 1 mips, with priority 2
		GoogleVm vm6 = new GoogleVm(6, 1, 0.01, 1.0, 0, 2, 0);
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
		GoogleVm vm7 = new GoogleVm(7, 1, 60.47, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm7));
		Assert.assertEquals(40.02, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(20.01, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

		// asserting that is not possible create vm8 because vmCreate method is not responsible by preempt Vms with low priority
		GoogleVm vm8 = new GoogleVm(7, 1, 40.02, 1.0, 0, 0, 0);
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
				priority0Vms.add(new GoogleVm(id, 1, cpuReq, 1.0, 0, 0, 0));
			} else {
				priority1Vms.add(new GoogleVm(id, 1, cpuReq, 1.0, 0, 1, 0));
			}
		}

		priorityToMipsInUse.put(0, 10 * cpuReq);
		priorityToMipsInUse.put(1, 10 * cpuReq);

		priorityToVms.put(0,priority0Vms);
		priorityToVms.put(1,priority1Vms);

		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 2);

		host1.setPriorityToInUseMips(priorityToMipsInUse);
		host1.setPriorityToVms(priorityToVms);

		// checking
		Assert.assertEquals(2, host1.getPriorityToInUseMips().size());
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 * cpuReq, host1.getPriorityToInUseMips().get(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host1.getPriorityToVms().size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(0).size());
		Assert.assertEquals(10, host1.getPriorityToVms().get(1).size());

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
			Vm gVm = new GoogleVm(id, 1, cpuReq2, 1.0, 0, priority, 0);
			host1.vmCreate(gVm);
		}

		//allocating vm with priority 1
		for (int i = 0; i < 10; i++) {
			int priority = 1;
			int id = 30 + i;
			double cpuReq2 = 0.1;
			Assert.assertEquals(89, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
			Assert.assertEquals(79 - (cpuReq2 * i), host1.getAvailableMipsByPriority(priority), ACCEPTABLE_DIFFERENCE);
			Vm gVm = new GoogleVm(id, 1, cpuReq2, 1.0, 0, priority, 0);
			host1.vmCreate(gVm);
		}
	}

	@Test
	public void testDecimalAccuracy(){

		// setting a new acceptable difference considering accuracy
		double NEW_ACCEPTABLE_DIFFERENCE = 0.000000001;

		// creating a new host with capacity that will be rounded to 1
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(1.0000000001))); // round to 1
		GoogleHost host1 = new GoogleHost(HOST_ID + 1, peList2,
				new VmSchedulerMipsBased(peList2), NUMBER_OF_PRIORITIES);

		// creating a new host with capacity that won't be rounded to 1
		List<Pe> peList3 = new ArrayList<Pe>();
		peList3.add(new Pe(0, new PeProvisionerSimple(1.000000001)));
		GoogleHost host2 = new GoogleHost(HOST_ID + 2, peList3,
				new VmSchedulerMipsBased(peList3), NUMBER_OF_PRIORITIES);

		// vm1 with mips required = 1
		GoogleVm vm1 = new GoogleVm(0, 1, 1, 1.0, 0, 0, 0);

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
		vm1 = new GoogleVm(0, 1, 1.000000001, 1.0, 0, 0, 0);
		Assert.assertFalse(host1.vmCreate(vm1));
		Assert.assertTrue(host2.vmCreate(vm1));

		// asserting new available mips of two hosts
		Assert.assertEquals(host1.getAvailableMips(), 1, NEW_ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(host2.getAvailableMips(), 0, NEW_ACCEPTABLE_DIFFERENCE);

		// destroying vm1 from host2 to do new testes and asserting the new available mips
		host2.vmDestroy(vm1);
		Assert.assertEquals(host2.getAvailableMips(), 1 + 1.0E-9, NEW_ACCEPTABLE_DIFFERENCE);

		// vm1 with mips required bigger than host1 and host2 capacity
		vm1 = new GoogleVm(0, 1, 1.0000000016, 1.0, 0, 0, 0);
		Assert.assertFalse(host1.vmCreate(vm1));
		Assert.assertFalse(host2.vmCreate(vm1));
		Assert.assertEquals(host1.getAvailableMips(), 1, NEW_ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(host2.getAvailableMips(), 1 + 1.0E-9, NEW_ACCEPTABLE_DIFFERENCE);


		// vm1 with mips required bigger than host1 and host2 capacity
		vm1 = new GoogleVm(0, 1, 1.0000000011, 1.0, 0, 0, 0);
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
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(1.0000000001))); // round to 1
		GoogleHost host1 = new GoogleHost(HOST_ID + 1, peList2,
				new VmSchedulerMipsBased(peList2), NUMBER_OF_PRIORITIES);

		// creating a new host with capacity that won't be rounded to 1
		List<Pe> peList3 = new ArrayList<Pe>();
		peList3.add(new Pe(0, new PeProvisionerSimple(1.000000001)));
		GoogleHost host2 = new GoogleHost(HOST_ID + 2, peList3,
				new VmSchedulerMipsBased(peList3), NUMBER_OF_PRIORITIES);

		// setting vms
		GoogleVm vm1 = new GoogleVm(1, 1, 1, 1.0, 0, 0, 0);
		GoogleVm vm2 = new GoogleVm(2, 1, 0.999999999, 1.0, 0, 0, 0);
		GoogleVm vm3 = new GoogleVm(3, 1, 0.000000001, 1.0, 0, 0, 0);

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
	
	@Test
	public void testGetUsageByPriority() {
		// creating hosts
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 3);

		// priority 0
		GoogleVm vm0 = new GoogleVm(0, 1, 50, 1.0, 0, 0, 0);

		Assert.assertTrue(host1.vmCreate(vm0));

		Assert.assertEquals(50, host1.getUsageByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getUsageByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getUsageByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getUsageByPriority(3), ACCEPTABLE_DIFFERENCE);
		
		// priority 1
		GoogleVm vm1 = new GoogleVm(1, 1, 20, 1.0, 0, 1, 0);

		Assert.assertTrue(host1.vmCreate(vm1));

		Assert.assertEquals(50, host1.getUsageByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(20, host1.getUsageByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getUsageByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getUsageByPriority(3), ACCEPTABLE_DIFFERENCE);
	
		// priority 2
		GoogleVm vm2 = new GoogleVm(2, 1, 20, 1.0, 0, 2, 0);

		Assert.assertTrue(host1.vmCreate(vm2));

		Assert.assertEquals(50, host1.getUsageByPriority(0), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(20, host1.getUsageByPriority(1), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(20, host1.getUsageByPriority(2), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host1.getUsageByPriority(3), ACCEPTABLE_DIFFERENCE);

	}
}