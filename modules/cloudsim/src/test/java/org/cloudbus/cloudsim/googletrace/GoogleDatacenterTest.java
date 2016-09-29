package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.googletrace.datastore.UtilizationDataStore;
import org.cloudbus.cloudsim.googletrace.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.googletrace.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GoogleDatacenterTest {

	private static final double ACCEPTABLE_DIFFERENCE = 0.000001;
	
	GoogleDatacenter datacenter;
	GoogleHost host;
	SimulationTimeUtil timeUtil;
	HostSelectionPolicy hostSelector;
	PreemptableVmAllocationPolicy preemptableVmAllocationPolicy;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		int num_user = 1; // number of grid users
		Calendar calendar = Calendar.getInstance();
		boolean trace_flag = false; // mean trace events
		
		// Initialize the CloudSim library
		CloudSim.init(num_user, calendar, trace_flag);		

		List<Host> hostList = new ArrayList<Host>();
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(10)));
		
		host = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 3);		
		hostList.add(host);

		// mocking
		DatacenterCharacteristics characteristics = Mockito.mock(DatacenterCharacteristics.class);
		Mockito.when(characteristics.getHostList()).thenReturn(hostList);
		
		Mockito.when(characteristics.getNumberOfPes()).thenReturn(1);
		
		timeUtil = Mockito.mock(SimulationTimeUtil.class);
		Mockito.when(timeUtil.clock()).thenReturn(0d);
		
		hostSelector = Mockito.mock(HostSelectionPolicy.class);
		Mockito.when(hostSelector.select(Mockito.any(SortedSet.class), Mockito.any(Vm.class))).thenReturn(host);
		
		List<GoogleHost> googleHostList = new ArrayList<GoogleHost>();
		for (Host host : hostList) {
			googleHostList.add((GoogleHost) host);
		}
		
		preemptableVmAllocationPolicy = new PreemptableVmAllocationPolicy(googleHostList, hostSelector);

		Properties properties = Mockito.mock(Properties.class);
		Mockito.when(properties.getProperty(UtilizationDataStore.DATABASE_URL_PROP)).thenReturn("jdbc:sqlite:inputUtilizationTest.sqlite3");

		datacenter = new GoogleDatacenter("datacenter",
				characteristics, preemptableVmAllocationPolicy,
				new LinkedList<Storage>(), 0, properties);
		
		datacenter.setSimulationTimeUtil(timeUtil);

		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertTrue(datacenter.getVmsRunning().isEmpty());
	}
	
	@Test
	public void testAllocateVm() {		
		int priority = 0;
		double runtime = 10;
		
		GoogleVm vm0 = new GoogleVm(1, 1, 5, 1, 0, priority, runtime);		

		datacenter.allocateHostForVm(false, vm0, null);
		
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
	}
	
	@Test
	public void testAllocateTwoVmWithSamePriority() {		
		int priority = 0;
		double runtime = 10;
		GoogleVm vm0 = new GoogleVm(0, 1, 5, 1.0, 0, priority, runtime);		

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0, null);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		
		GoogleVm vm1 = new GoogleVm(1, 1, 5, 1.0, 0, priority, runtime);		

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1, null);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

	}
	
	@Test
	public void testAllocateThreeVmWithSamePriorityWithWating() {		
		int priority = 0;
		double runtime = 10;
		GoogleVm vm0 = new GoogleVm(0, 1, 5, 1.0, 0, priority, runtime);		

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0, null);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		
		GoogleVm vm1 = new GoogleVm(1, 1, 5, 1.0, 0, priority, runtime);		

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1, null);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		GoogleVm vm2 = new GoogleVm(2, 1, 5, 1.0, 0, priority, runtime);
		
		// checking and simulating host selector
		Assert.assertFalse(host.isSuitableForVm(vm2));
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);
		
		// allocating third vm
		datacenter.allocateHostForVm(false, vm2, null);
		
		// checking
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());
	}

	@Test
	public void testAllocateVmsToInsertInVmsForScheduling() {
		int priority = 0;
		double runtime = 10;
		GoogleVm vm0 = new GoogleVm(0, 1, 5, 1.0, 0, priority, runtime);		

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0, null);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		
		GoogleVm vm1 = new GoogleVm(1, 1, 5, 1.0, 0, priority, runtime);		

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1, null);
		
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		GoogleVm vm2 = new GoogleVm(2, 1, 5, 1.0, 0, priority, runtime);
		
		// checking and simulating host selector
		Assert.assertFalse(host.isSuitableForVm(vm2));
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);
		
		// allocating third vm
		datacenter.allocateHostForVm(false, vm2, null);
		
		// checking
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());
	}

	@Test
	public void testVmDestroy() {
		int priority = 0;
		double runtime = 10;

		// allocating first Vm
		GoogleVm vm0 = new GoogleVm(1, 1, 5, 1, 0, priority, runtime);
		datacenter.allocateHostForVm(false, vm0, null);

		//checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

		//Destroy Vm0
		Mockito.when(timeUtil.clock()).thenReturn(runtime);
		SimEvent destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm0);
		datacenter.processVmDestroy(destroyVm, false);

		//checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

	}

	@Test
	public void testVmDestroyWithTwoVm() {
		int priority = 0;
		double runtime = 10;
		GoogleVm vm0 = new GoogleVm(0, 1, 4.9, 1.0, 0, priority, 0);

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

		GoogleVm vm1 = new GoogleVm(1, 1, 5.1, 1.0, 0, priority, runtime + 0.1);

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		//destroying vm0
		Mockito.when(timeUtil.clock()).thenReturn(runtime);
		SimEvent destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm0);

		datacenter.processVmDestroy(destroyVm, false);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().first());
	}
	
	@Test
	public void testVmDestroyWithVmForScheduling(){

		int priority = 0;
		double runtime = 10;
		GoogleVm vm0 = new GoogleVm(0, 1, 5.1, 1.0, 0, priority, runtime - 0.1);

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

		GoogleVm vm1 = new GoogleVm(1, 1, 4.9, 1.0, 0, priority, runtime);

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		GoogleVm vm2 = new GoogleVm(2, 1, 5, 1.0, 0, priority, runtime);

		// checking and simulating host selector
		Assert.assertFalse(host.isSuitableForVm(vm2));
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);

		// allocating third vm
		datacenter.allocateHostForVm(false, vm2, null);

		// checking
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		//destroying vm0
		Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(host);
		SimEvent destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm0);
		datacenter.processVmDestroy(destroyVm, false);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm2, datacenter.getVmsRunning().last());
	}

	@Test
	public void testVmDestroyWithVmForScheduling2(){

		int priority = 0;
		double runtime = 10;
		GoogleVm vm0 = new GoogleVm(0, 1, 5.1, 1.0, 0, priority, runtime - 0.1);

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

		GoogleVm vm1 = new GoogleVm(1, 1, 4.9, 1.0, 0, priority, runtime);

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		GoogleVm vm2 = new GoogleVm(2, 1, 5, 1.0, 0, priority, runtime);

		// checking and simulating host selector
		Assert.assertFalse(host.isSuitableForVm(vm2));
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);

		// allocating third vm
		datacenter.allocateHostForVm(false, vm2, null);

		// checking
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		GoogleVm vm3 = new GoogleVm(3, 1, 0.1, 1.0, 0, priority, runtime);

		// checking and simulating host selector
		Assert.assertFalse(host.isSuitableForVm(vm2));
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm3)).thenReturn(null);

		// allocating third vm
		datacenter.allocateHostForVm(false, vm3, null);

		// checking
		Assert.assertEquals(2, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(vm3, datacenter.getVmsForScheduling().last());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());


		//destroying vm0
		Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(host);
		Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm3)).thenReturn(host);
		SimEvent destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm0);
		datacenter.processVmDestroy(destroyVm, false); // has an error in sum of available mips when allocating a vm
														// that are for scheduling

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(3, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().first());
		Assert.assertTrue(datacenter.getVmsRunning().contains(vm2));
		Assert.assertEquals(vm3, datacenter.getVmsRunning().last());
	}

	@Test
	public void testVmDestroyWithPreempt() {

		int priority = 1;
		double runtime = 10;
		double cpuReq = 0.0000001;
		double ACCEPTABLE_DIFFERENCE = 0.000000001;

		GoogleVm vm0 = new GoogleVm(0, 1, 2 * cpuReq, 1.0, 0, priority - 1,
				runtime - 0.5);
		GoogleVm vm1 = new GoogleVm(1, 1, 2 * cpuReq, 1.0, 0, priority, 0.4);
		GoogleVm vm2 = new GoogleVm(2, 1, cpuReq, 1.0, 0, priority + 1, 0.1);
		GoogleVm vm3 = new GoogleVm(3, 1, 9.9999998, 1.0, 0, priority - 1,
				runtime);

		SimEvent destroyVm = Mockito.mock(SimEvent.class);

		Mockito.when(
				hostSelector.select(preemptableVmAllocationPolicy
						.getPriorityToSortedHost().get(priority), vm0))
				.thenReturn(host);
		Mockito.when(
				hostSelector.select(preemptableVmAllocationPolicy
						.getPriorityToSortedHost().get(priority), vm1))
				.thenReturn(host);
		Mockito.when(
				hostSelector.select(preemptableVmAllocationPolicy
						.getPriorityToSortedHost().get(priority), vm2))
				.thenReturn(host);
		Mockito.when(
				hostSelector.select(preemptableVmAllocationPolicy
						.getPriorityToSortedHost().get(priority), vm3))
				.thenReturn(host);

		// initial tests

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

		// allocating vm3 to reduce host capacity
		datacenter.allocateHostForVm(false, vm3, null);

		// checking
		Assert.assertEquals(2 * cpuReq, host.getAvailableMips(),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm3, datacenter.getVmsRunning().first());

		// allocating vm2 with priority 2
		datacenter.allocateHostForVm(false, vm2, null);
		Assert.assertEquals(1 * cpuReq, host.getAvailableMips(),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm2, datacenter.getVmsRunning().last());

		// allocating vm1 with priority 1 to preempt vm2
		datacenter.allocateHostForVm(false, vm1, null);
		Assert.assertEquals(0 * cpuReq, host.getAvailableMips(),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		// allocating vm0 with priority 0 to preempt vm1
		datacenter.allocateHostForVm(false, vm0, null);
		Assert.assertEquals(0 * cpuReq, host.getAvailableMips(),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm1, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().last());
		Assert.assertEquals(2, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm3, datacenter.getVmsRunning().last());

		// finishing vm0 to reallocate vm1
		Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.5);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm0);
		datacenter.processVmDestroy(destroyVm, false);

		Assert.assertEquals(0 * cpuReq, host.getAvailableMips(),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

		// finishing vm1 to reallocate vm2
		Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm1);
		datacenter.processVmDestroy(destroyVm, false);

		Assert.assertEquals(1 * cpuReq, host.getAvailableMips(),
				ACCEPTABLE_DIFFERENCE);
		System.out.println(host.getAvailableMips());
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm2, datacenter.getVmsRunning().last());

		// finishing vm2
		Mockito.when(timeUtil.clock()).thenReturn(runtime + 0.1);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm2);
		datacenter.processVmDestroy(destroyVm, false);

		// checking
		Assert.assertEquals(2 * cpuReq, host.getAvailableMips(),
				ACCEPTABLE_DIFFERENCE);
		System.out.println(host.getAvailableMips());
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm3, datacenter.getVmsRunning().first());

		// finishing vm3 to return to initial state
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm3);
		datacenter.processVmDestroy(destroyVm, false);

		Assert.assertEquals(10, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
		System.out.println(host.getAvailableMips());
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertTrue(datacenter.getVmsRunning().isEmpty());
	}

	@Test
	public void testVmDestroyWithVmForSchedulingDifferentPriotities() {
		double runtime = 10;
		GoogleVm vm0P0 = new GoogleVm(0, 1, 5, 1.0, 0, 0, runtime - 0.1);

		// allocating vm priority 0
		datacenter.allocateHostForVm(false, vm0P0, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
		Assert.assertEquals(10 - 5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 5, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 5, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		GoogleVm vm1P1 = new GoogleVm(1, 1, 2, 1.0, 0, 1, runtime);

		// allocating vm priority 1
		datacenter.allocateHostForVm(false, vm1P1, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

		Assert.assertEquals(10 - 5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 7, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 7, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// allocating vm priority 2
		GoogleVm vm2P2 = new GoogleVm(2, 1, 2, 1.0, 0, 2, runtime);

		datacenter.allocateHostForVm(false, vm2P2, null);

		// checking
		Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(3, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
		Assert.assertTrue(datacenter.getVmsRunning().contains(vm1P1));
		Assert.assertEquals(vm2P2, datacenter.getVmsRunning().last());

		Assert.assertEquals(10 - 5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 7, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 9, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// allocating vm priority 0
		GoogleVm vm3P0 = new GoogleVm(3, 1, 3, 1.0, 0, 0, runtime);

		datacenter.allocateHostForVm(false, vm3P0, null);

		// checking
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm2P2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(3, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
		Assert.assertTrue(datacenter.getVmsRunning().contains(vm3P0));
		Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

		Assert.assertEquals(10 - 8, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 10, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 10, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// destroying vm0P0
		Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
		SimEvent destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm0P0);
		datacenter.processVmDestroy(destroyVm, false);

		// checking
		Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(3, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm3P0, datacenter.getVmsRunning().first());
		Assert.assertTrue(datacenter.getVmsRunning().contains(vm1P1));
		Assert.assertEquals(vm2P2, datacenter.getVmsRunning().last());

		Assert.assertEquals(10 - 3, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 5, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(10 - 7, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);
	}

	@Test
	public void testVmDestroyWithVmForSchedulingDifferentPriotities2() {
		double runtime = 10;

		List<GoogleVm> vmsP2 = new ArrayList<GoogleVm>();

		// creating vms priorities 2
		for (int i = 0; i < 10; i++) {
			GoogleVm vmP2 = new GoogleVm(i, 1, 1.0, 1.0, 0, 2, runtime);
			datacenter.allocateHostForVm(false, vmP2, null);

			// checking
			Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
			Assert.assertEquals(i + 1, datacenter.getVmsRunning().size());
			Assert.assertTrue(datacenter.getVmsRunning().contains(vmP2));

			Assert.assertEquals(10, host.getAvailableMipsByPriority(0),
					ACCEPTABLE_DIFFERENCE);
			Assert.assertEquals(10, host.getAvailableMipsByPriority(1),
					ACCEPTABLE_DIFFERENCE);
			Assert.assertEquals(10 - (i + 1),
					host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

			vmsP2.add(vmP2);
		}

		List<GoogleVm> vmsP1 = new ArrayList<GoogleVm>();

		// creating vms priorities 1
		for (int i = 0; i < 10; i++) {
			GoogleVm vmP1 = new GoogleVm(i + 10, 1, 1.0, 1.0, 0, 1,
					runtime - 0.1);

			datacenter.allocateHostForVm(false, vmP1, null);

			// checking
			Assert.assertEquals(i + 1, datacenter.getVmsForScheduling().size());
			Assert.assertEquals(10, datacenter.getVmsRunning().size());
			Assert.assertTrue(datacenter.getVmsRunning().contains(vmP1));

			Assert.assertEquals(10, host.getAvailableMipsByPriority(0),
					ACCEPTABLE_DIFFERENCE);
			Assert.assertEquals(10 - (i + 1),
					host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
			Assert.assertEquals(0, host.getAvailableMipsByPriority(2),
					ACCEPTABLE_DIFFERENCE);

			vmsP1.add(vmP1);
		}

		// asserting all vmsForScheduling are priority 2
		for (GoogleVm vmP2 : vmsP2) {
			Assert.assertTrue(datacenter.getVmsForScheduling().contains(vmP2));
		}
		Assert.assertEquals(vmsP2.size(), datacenter.getVmsForScheduling()
				.size());

		// asserting all vmsForRunning are priority 1
		for (GoogleVm vmP1 : vmsP1) {
			Assert.assertTrue(datacenter.getVmsRunning().contains(vmP1));
		}
		Assert.assertEquals(vmsP1.size(), datacenter.getVmsRunning().size());

		GoogleVm vmP0 = new GoogleVm(20, 1, 5, 1.0, 0, 0, runtime - 0.1);
		// allocating vm priority 0
		datacenter.allocateHostForVm(false, vmP0, null);

		// checking
		Assert.assertEquals(15, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(6, datacenter.getVmsRunning().size());
		Assert.assertEquals(vmP0, datacenter.getVmsRunning().first());

		// asserting all vms priority2 are in vmsForScheduling
		for (GoogleVm vmP2 : vmsP2) {
			Assert.assertTrue(datacenter.getVmsForScheduling().contains(vmP2));
		}

		// asserting vms priority 1 are running and waiting
		for (int i = 0; i < 5; i++) {
			Assert.assertTrue(datacenter.getVmsRunning().contains(vmsP1.get(i)));
		}

		for (int i = 5; i < 10; i++) {
			Assert.assertTrue(datacenter.getVmsForScheduling().contains(
					vmsP1.get(i)));
		}

		Assert.assertEquals(10 - 5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// destroying vm priority 1
		Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
		SimEvent destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vmsP1.get(0));
		datacenter.processVmDestroy(destroyVm, false);

		// checking
		Assert.assertEquals(14, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(6, datacenter.getVmsRunning().size());
		Assert.assertEquals(vmP0, datacenter.getVmsRunning().first());

		Assert.assertEquals(10 - 5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// destroying vm priority 1
		destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vmsP1.get(1));
		datacenter.processVmDestroy(destroyVm, false);

		// checking
		Assert.assertEquals(13, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(6, datacenter.getVmsRunning().size());
		Assert.assertEquals(vmP0, datacenter.getVmsRunning().first());

		Assert.assertEquals(10 - 5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// destroying vm priority 0
		Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
		destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0);
		datacenter.processVmDestroy(destroyVm, false);

		// checking
		Assert.assertEquals(8, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(10, datacenter.getVmsRunning().size());
		Assert.assertEquals(vmsP1.get(2), datacenter.getVmsRunning().first());

		Assert.assertEquals(10, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// asserting vms priority 2 are running and waiting
		for (int i = 0; i < 2; i++) {
			Assert.assertTrue(datacenter.getVmsRunning().contains(vmsP2.get(i)));
		}

		for (int i = 2; i < 10; i++) {
			Assert.assertTrue(datacenter.getVmsForScheduling().contains(
					vmsP2.get(i)));
		}
	}

	@Test
	public void testVmDestroyWithPreemptionAfter() {
		double runtime = 10;

		GoogleVm vm0P0 = new GoogleVm(0, 1, 5, 1.0, 0, 0, runtime - 0.1);

		// allocating first vm
		datacenter.allocateHostForVm(false, vm0P0, null);
		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(1, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());

		Assert.assertEquals(5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(5, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(5, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		GoogleVm vm1P1 = new GoogleVm(1, 1, 2, 1.0, 0, 1, runtime);

		// allocating second vm
		datacenter.allocateHostForVm(false, vm1P1, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

		Assert.assertEquals(5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(3, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(3, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		GoogleVm vm2P2 = new GoogleVm(2, 1, 3, 1.0, 0, 2, runtime);

		// allocating third vm
		datacenter.allocateHostForVm(false, vm2P2, null);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(3, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
		Assert.assertTrue(datacenter.getVmsRunning().contains(vm1P1));
		Assert.assertEquals(vm2P2, datacenter.getVmsRunning().last());

		Assert.assertEquals(5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(3, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// allocation one more VM with priority 0
		GoogleVm vm3P0 = new GoogleVm(3, 1, 6, 1.0, 0, 0, runtime);

		// checking and simulating host selector
		Assert.assertFalse(host.isSuitableForVm(vm3P0));
		Mockito.when(
				hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(0), vm3P0)).thenReturn(null);

		datacenter.allocateHostForVm(false, vm3P0, null);

		// checking
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm3P0, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(3, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
		Assert.assertTrue(datacenter.getVmsRunning().contains(vm1P1));
		Assert.assertEquals(vm2P2, datacenter.getVmsRunning().last());

		Assert.assertEquals(5, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(3, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(0, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// destroying vm0P0
		Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
		SimEvent destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm0P0);
		datacenter.processVmDestroy(destroyVm, false);

		// checking
		Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
		Assert.assertEquals(vm2P2, datacenter.getVmsForScheduling().first());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm3P0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

		Assert.assertEquals(4, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(2, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);

		// destroying vm0P1
		Mockito.when(timeUtil.clock()).thenReturn(runtime);
		destroyVm = Mockito.mock(SimEvent.class);
		Mockito.when(destroyVm.getData()).thenReturn((Object) vm1P1);
		datacenter.processVmDestroy(destroyVm, false);

		// checking
		Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
		Assert.assertEquals(2, datacenter.getVmsRunning().size());
		Assert.assertEquals(vm3P0, datacenter.getVmsRunning().first());
		Assert.assertEquals(vm2P2, datacenter.getVmsRunning().last());

		Assert.assertEquals(4, host.getAvailableMipsByPriority(0),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(4, host.getAvailableMipsByPriority(1),
				ACCEPTABLE_DIFFERENCE);
		Assert.assertEquals(1, host.getAvailableMipsByPriority(2),
				ACCEPTABLE_DIFFERENCE);
	}
}
