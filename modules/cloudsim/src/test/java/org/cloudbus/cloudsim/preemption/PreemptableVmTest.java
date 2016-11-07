package org.cloudbus.cloudsim.preemption;

import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.junit.Assert;
import org.junit.Test;

public class PreemptableVmTest {

	private static final double ACCEPTABLE_DIFFERENCE = 0.000001;

	@Test
	public void testCompareTo() {
		int submitTime = 0;
		int priority = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, 0);		
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, submitTime + 1, priority + 1, 0);
		
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(1, vm2.compareTo(vm1));
		
	}
	
	@Test
	public void testCompareTo2() {
		int submitTime = 0;
		int priority = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, 0);		
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, submitTime, priority, 0);
		
		// The vms are not considered equal because of the id
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(1, vm2.compareTo(vm1));
		
	}

	@Test
	public void testCompareTo3() {
		int submitTime = 0;
		int priority = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, 0);		
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, submitTime + 1, priority, 0);
		
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(1, vm2.compareTo(vm1));
		
	}

	
	@Test
	public void testCompareTo4() {
		int submitTime = 0;
		int priority = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, 0);		
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, submitTime, priority + 1, 0);
		
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(1, vm2.compareTo(vm1));
		
	}
	
	@Test
	public void testCompareTo5() {
		int submitTime = 0;
		int priority = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime + 1, priority, 0);		
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, submitTime, priority + 1, 0);
		
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(1, vm2.compareTo(vm1));
		
	}

	@Test
	public void testCompareTo6() {
		int submitTime = 0;
		int priority = 0;

		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority + 1, 0);
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, submitTime, priority, 0);

		Assert.assertEquals(1, vm1.compareTo(vm2));
		Assert.assertEquals(-1, vm2.compareTo(vm1));

	}

	@Test
	public void testCompareTo7() {
		int submitTime = 0;
		int priority = 0;

		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime + 1, priority, 0);
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, submitTime, priority, 0);

		Assert.assertEquals(1, vm1.compareTo(vm2));
		Assert.assertEquals(-1, vm2.compareTo(vm1));
	}

	
	@Test
	public void testActualRuntime() {
		int submitTime = 0;
		int priority = 0;
		double runtime = 10;
		double time = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, runtime);
		
		// checking
		Assert.assertEquals(PreemptableVm.NOT_EXECUTING_TIME, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		
		vm1.setStartExec(time);
		
		// checking
		Assert.assertEquals(0, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		
		time += 5;
		
		vm1.preempt(time);
		
		// checking
		Assert.assertEquals(PreemptableVm.NOT_EXECUTING_TIME, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(time, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testActualRuntime2() {
		int submitTime = 0;
		int priority = 0;
		double runtime = 10;
		double time = 15;
		double expectedActualRuntime = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, runtime);
		
		// checking
		Assert.assertEquals(PreemptableVm.NOT_EXECUTING_TIME, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		
		// starting execution
		vm1.setStartExec(time);
		
		// checking
		Assert.assertEquals(15, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		
		// passing the time
		time += 5; //20
		expectedActualRuntime += 5; //5

		// preempting
		vm1.preempt(time);
		
		// checking
		Assert.assertEquals(PreemptableVm.NOT_EXECUTING_TIME, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		
		// passing the time
		time += 5; //25
		
		// executing again
		vm1.setStartExec(time);

		// checking
		Assert.assertEquals(time, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		
		// passing the time
		time += 5; //30
		expectedActualRuntime += 5; //10

		// checking
		Assert.assertEquals(time - 5, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		
		// preempting
		vm1.preempt(time);

		// checking
		Assert.assertEquals(PreemptableVm.NOT_EXECUTING_TIME, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
	}
	
	@Test
	public void testAchievedRuntime() {
		int submitTime = 0;
		int priority = 0;
		double runtime = 10;
		double time = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, runtime);
		
		// checking
		Assert.assertEquals(PreemptableVm.NOT_EXECUTING_TIME, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		Assert.assertFalse(vm1.achievedRuntime(time));
		
		// starting execution
		vm1.setStartExec(time);
		
		// checking (starting at time = 0 to time = 9)
		for (int i = 0; i < runtime; i++) {
			Assert.assertEquals(time, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
			Assert.assertFalse(vm1.achievedRuntime(time));
			time++;
		}

		// checking time 10
		Assert.assertEquals(runtime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(vm1.achievedRuntime(time));
		
		Assert.assertEquals(runtime, vm1.getActualRuntime(runtime), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(vm1.achievedRuntime(runtime));
	}

	@Test
	public void testAchievedRuntimeWithPreemption() {
		int submitTime = 0;
		int priority = 0;
		double runtime = 20;
		double time = 0;
		double expectedActualRuntime = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, runtime);
		
		// checking
		Assert.assertEquals(PreemptableVm.NOT_EXECUTING_TIME, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		Assert.assertFalse(vm1.achievedRuntime(time));
		
		// starting execution
		vm1.setStartExec(time);
		
		// passing the time and checking 
		for (time = 0; time < runtime / 2; time++) {
			Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
			Assert.assertFalse(vm1.achievedRuntime(time));
			expectedActualRuntime++;
		}
		
		// preempting at time 10
		vm1.preempt(time);
	
		// checking
		Assert.assertEquals(PreemptableVm.NOT_EXECUTING_TIME, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		Assert.assertFalse(vm1.achievedRuntime(time));
			
		// passing the time
		time += 10; // time = 20
		
		// starting execution at time = 20
		vm1.setStartExec(time);

		// checking
		Assert.assertEquals(time, vm1.getStartExec(), ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE); // 20
		Assert.assertFalse(vm1.achievedRuntime(time));

		// passing the time and checking 
		for (int i = 0; i < runtime / 2; i++) {
			Assert.assertEquals(expectedActualRuntime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
			Assert.assertFalse(vm1.achievedRuntime(time));
			expectedActualRuntime++;
			time++;
		}
		
		Assert.assertEquals(runtime, vm1.getActualRuntime(time), ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(vm1.achievedRuntime(time));

		// asserting that Vm achieved runtime at current time + 1 too
		Assert.assertTrue(vm1.achievedRuntime(time + 1));
	}

	
	@Test
	public void testSorting() {
		int submitTime = 10;
		int priority = 0;
		
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, 0);		
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, submitTime + 1, priority, 0);
		PreemptableVm vm3 = new PreemptableVm(3, 1, 1.0, 1.0, submitTime, priority + 1, 0);
		
		// testing compareTo
		Assert.assertEquals(-1, vm1.compareTo(vm2));
		Assert.assertEquals(-1, vm1.compareTo(vm3));
		
		Assert.assertEquals(1, vm3.compareTo(vm1));
		Assert.assertEquals(1, vm3.compareTo(vm2));
		Assert.assertEquals(0, vm3.compareTo(vm3));
				
		// test sorting
		SortedSet<Vm> sortedVms = new TreeSet<Vm>();		
		Assert.assertTrue(sortedVms.isEmpty());
		
		sortedVms.add(vm3);
		sortedVms.add(vm1);
		sortedVms.add(vm2);

		// checking sorting
		Assert.assertEquals(3, sortedVms.size());
		Assert.assertTrue(sortedVms.contains(vm1));
		Assert.assertTrue(sortedVms.contains(vm2));
		Assert.assertTrue(sortedVms.contains(vm3));
		
		Assert.assertEquals(vm1, sortedVms.first());
		Assert.assertEquals(vm3, sortedVms.last());
		
		// adding more elements
		PreemptableVm vm4 = new PreemptableVm(4, 1, 1.0, 1.0, submitTime + 1, priority + 1, 0);
		PreemptableVm vm5 = new PreemptableVm(5, 1, 1.0, 1.0, submitTime, priority + 2, 0);
		
		// testing compareTo
		Assert.assertEquals(-1, vm1.compareTo(vm4));
		Assert.assertEquals(-1, vm1.compareTo(vm5));
		
		Assert.assertEquals(-1, vm3.compareTo(vm4));
		Assert.assertEquals(-1, vm3.compareTo(vm5));
		
		Assert.assertEquals(1, vm5.compareTo(vm1));
		Assert.assertEquals(1, vm5.compareTo(vm2));
		Assert.assertEquals(1, vm5.compareTo(vm3));
		Assert.assertEquals(1, vm5.compareTo(vm4));

		sortedVms.add(vm5);
		sortedVms.add(vm4);
		
		// checking sorting
		Assert.assertEquals(5, sortedVms.size());
		Assert.assertTrue(sortedVms.contains(vm1));
		Assert.assertTrue(sortedVms.contains(vm2));
		Assert.assertTrue(sortedVms.contains(vm3));
		Assert.assertTrue(sortedVms.contains(vm4));
		Assert.assertTrue(sortedVms.contains(vm5));

		Assert.assertEquals(vm1, sortedVms.first());
		Assert.assertEquals(vm5, sortedVms.last());

		// changing the first element
		PreemptableVm vm6 = new PreemptableVm(4, 1, 1.0, 1.0, submitTime - 1, priority, 0);
		sortedVms.add(vm6);
		Assert.assertEquals(6, sortedVms.size());
		Assert.assertTrue(sortedVms.contains(vm1));
		Assert.assertTrue(sortedVms.contains(vm2));
		Assert.assertTrue(sortedVms.contains(vm3));
		Assert.assertTrue(sortedVms.contains(vm4));
		Assert.assertTrue(sortedVms.contains(vm5));
		Assert.assertTrue(sortedVms.contains(vm6));
		Assert.assertEquals(vm6, sortedVms.first());
		Assert.assertEquals(vm5, sortedVms.last());
	}
	
	@Test
	public void testGetNumberOfMigrations() {
		int submitTime = 10;
		int priority = 0;
		
		// creating a vm
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, 0);		
		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		// first host allocation
		vm1.allocatingToHost(0);		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		// allocating in other host
		vm1.allocatingToHost(1);		
		Assert.assertEquals(1, vm1.getNumberOfMigrations());
	}

	@Test
	public void testGetNumberOfMigrations2() {
		int submitTime = 10;
		int priority = 0;
		
		// creating a vm
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, 0);		
		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		// first host allocation
		vm1.allocatingToHost(0);		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		// allocating in the same host
		vm1.allocatingToHost(0);		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
	}
	
	@Test
	public void testGetNumberOfMigrationsAfterPreemption() {
		int submitTime = 10;
		int priority = 0;
		
		// creating a vm
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, 0);		
		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		// first host allocation
		vm1.allocatingToHost(0);		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		vm1.preempt(10);
		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		// allocating in the same host
		vm1.allocatingToHost(0);		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
	}
	
	@Test
	public void testGetNumberOfMigrationsAfterPreemption2() {
		int submitTime = 10;
		int priority = 0;
		
		// creating a vm
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, submitTime, priority, 0);		
		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		// first host allocation
		vm1.allocatingToHost(0);		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		vm1.preempt(10);
		
		Assert.assertEquals(0, vm1.getNumberOfMigrations());
		
		// allocating in the same host
		vm1.allocatingToHost(1);		
		Assert.assertEquals(1, vm1.getNumberOfMigrations());
	}
}
