package org.cloudbus.cloudsim.preemption.policies.vmallocation;

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
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.hostselection.WorstFitMipsBasedHostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.PreemptiveHostComparator;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PreemptableVmAllocationTest {

	private SortedSet<PreemptiveHost> sortedHosts;
	private HostSelectionPolicy hostSelector;
	private PreemptiveHost host1, host2, host3;
	private PreemptableVmAllocationPolicy preemptablePolicy;
	private static final int PRIORITY_0 = 0;
	private static final int PRIORITY_1 = 1;
	private static final int PRIORITY_2 = 2;
	private static final int NUMBER_OF_PRIORITIES = 3;
	private static final double ACCEPTABLE_DIFERENCE = 0.000001;
	private Properties properties;
	
	@Before
	public void setUp() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		
		properties = new Properties();
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "1");
		
		host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));
		
		List<Pe> peList2 = new ArrayList<Pe>();
		peList2.add(new Pe(0, new PeProvisionerSimple(500)));
		host2 = new PreemptiveHost(2, peList2,
				new VmSchedulerMipsBased(peList2), new FCFSBasedPreemptionPolicy(properties));
		
		List<PreemptiveHost> hosts = new ArrayList<PreemptiveHost>();
		hosts.add(host1);
		hosts.add(host2);
		
		sortedHosts = new TreeSet<PreemptiveHost>(new PreemptiveHostComparator(0));
		
		for (PreemptiveHost googleHost : hosts) {
			sortedHosts.add(googleHost);
		}		

		hostSelector = Mockito.mock(WorstFitMipsBasedHostSelectionPolicy.class);

		preemptablePolicy = new PreemptableVmAllocationPolicy(hosts, "");
		preemptablePolicy.setHostSelector(hostSelector);
	}
	
	@Test
	public void testAllocateHostForVm() {
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);

		// checking
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		//asserting that vm1 is allocated on host1 looking at VmTable
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
	}

	@Test
	public void testAllocateHostForVm2() {
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);
		Mockito.when(hostSelector.select(sortedHosts, vm2)).thenReturn(host2);

		// checking
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		
		// allocating the 2nd vm
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2));
		
		// checking 
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());

		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// Asserting VmTable after allocations
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host2, preemptablePolicy.getVmTable().get(vm2.getUid()));
	}


	@Test
	public void testAllocateVMsToSameHost(){
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm3 = new PreemptableVm(3, 1, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm4 = new PreemptableVm(4, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);
		Mockito.when(hostSelector.select(sortedHosts, vm2)).thenReturn(host1);
		Mockito.when(hostSelector.select(sortedHosts, vm3)).thenReturn(host1);
		Mockito.when(hostSelector.select(sortedHosts, vm4)).thenReturn(host2);

		// checking vm1 allocation
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));

		// checking vm2 allocation
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2));
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(2, host1.getVmList().size());
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));

		// checking vm3 allocation
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3));
		Assert.assertEquals(host1, vm3.getHost());
		Assert.assertEquals(3, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(3, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm3.getUid()));


		// checking vm4 allocation
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4));
		Assert.assertEquals(host2, vm4.getHost());
		Assert.assertEquals(3, host1.getVmList().size());
		Assert.assertEquals(1, host2.getVmList().size());
		Assert.assertEquals(4, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm3.getUid()));
		Assert.assertEquals(host2, preemptablePolicy.getVmTable().get(vm4.getUid()));

	}
	
	@Test
	public void testDeallocateHostForVm() {
		// setting environment
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// deallocating
		preemptablePolicy.deallocateHostForVm(vm1);

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}
	
	
	@Test
	public void testDeallocateHostForVm2() {
		// setting environment
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host2.vmCreate(vm2));
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host2);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// deallocating vm1
		preemptablePolicy.deallocateHostForVm(vm1);

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// deallocating vm2
		preemptablePolicy.deallocateHostForVm(vm2);

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());

		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());

		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testDeallocateVMNonExistent(){
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
		preemptablePolicy.deallocateHostForVm(vm1);
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
	}

	@Test
	public void testDeallocateVMNonexistentAfterDeallocateExistingVM(){
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);

		// checking
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertFalse(preemptablePolicy.getVmTable().isEmpty());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));

		// deallocate existing VM1
		preemptablePolicy.deallocateHostForVm(vm1);
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
		Assert.assertNull(preemptablePolicy.getVmTable().get(vm1.getUid()));

		// trying to deallocate VM1 again
		preemptablePolicy.deallocateHostForVm(vm1);
		Assert.assertTrue(preemptablePolicy.getVmTable().isEmpty());
		Assert.assertNull(preemptablePolicy.getVmTable().get(vm1.getUid()));

	}

	@Test
	public void testDeallocateMoreThanOneVMFromSameHost(){
		// setting environment
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));

		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm2));

		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(2, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm2), vm2.getHost());

		// deallocating VM1
		preemptablePolicy.deallocateHostForVm(vm1);
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), vm1.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm1));
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// deallocationg VM2
		preemptablePolicy.deallocateHostForVm(vm2);
		Assert.assertEquals(preemptablePolicy.getHost(vm2), vm2.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm2));
		Assert.assertNull(vm2.getHost());
		Assert.assertTrue(host1.getVmList().isEmpty());
		Assert.assertTrue(host2.getVmList().isEmpty());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());

	}
	
	@Test
	public void testOptimizeAllocation() {
		Assert.assertNull(preemptablePolicy
				.optimizeAllocation(new ArrayList<Vm>()));
	}
	
	@Test
	public void testPreempt() {
		// setting environment
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host2.vmCreate(vm2));
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host2);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertEquals(host2, vm2.getHost());
		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// preempting
		Assert.assertTrue(preemptablePolicy.preempt(vm1));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), vm1.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm1));
		Assert.assertEquals(0, host1.getVmList().size());

		Assert.assertEquals(1, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting
		Assert.assertTrue(preemptablePolicy.preempt(vm2));
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm2), vm2.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm2));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());

		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());

		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testPreemptInvalidVm() {
		// setting environment
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, 0, 0, 0);
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting invalid vm
		Assert.assertFalse(preemptablePolicy.preempt(vm2));
		
		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testPreemptSameVmMoreThanOneTime() {
		// setting environment
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));
		
		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting vm1
		Assert.assertTrue(preemptablePolicy.preempt(vm1));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());

		// preempting vm1 again
		Assert.assertFalse(preemptablePolicy.preempt(vm1));

		// checking
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());

		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}


	@Test
	public void preemptMoreThanOneVmFromSameHost(){
		// setting environment
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm1));

		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, 0, 0, 0);
		Assert.assertTrue(host1.vmCreate(vm2));

		Map<String, Host> vmTable = new HashMap<String, Host>();
		vmTable.put(vm1.getUid(), host1);
		vmTable.put(vm2.getUid(), host1);
		preemptablePolicy.setVmTable(vmTable);

		// checking
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(2, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());

		// preempting vm1
		Assert.assertTrue(preemptablePolicy.preempt(vm1));
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());

		// preempting vm2
		Assert.assertTrue(preemptablePolicy.preempt(vm2));
		Assert.assertNull(vm2.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());

		// trying preempt two Vms again
		Assert.assertFalse(preemptablePolicy.preempt(vm1));
		Assert.assertFalse(preemptablePolicy.preempt(vm2));
	}

	@Test
	public void testAllocateVMAtNullHost(){
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);

		// mocking host selector
		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(null);

		// checking
		Assert.assertFalse(preemptablePolicy.allocateHostForVm(vm1));
		Assert.assertNull(vm1.getHost());
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());
	}

	@Test
	public void testAllocateAtSpecificHost(){
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm3 = new PreemptableVm(3, 1, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm4 = new PreemptableVm(4, 1, 1.0, 1.0, 0, 0, 0);

		// checking vm1 allocation at null host
		Assert.assertFalse(preemptablePolicy.allocateHostForVm(vm1, null));
		Assert.assertNull(vm1.getHost());
		Assert.assertNull(preemptablePolicy.getHost(vm1));
		Assert.assertEquals(0, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(0, preemptablePolicy.getVmTable().size());

		// checking vm1 allocation at host 1
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1, host1));
		Assert.assertEquals(host1, vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), vm1.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm1), host1);
		Assert.assertEquals(1, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(1, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));

		// checking vm2 allocation at host 1
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host1));
		Assert.assertEquals(host1, vm2.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm2), vm2.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm2), host1);
		Assert.assertEquals(2, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(2, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));

		// checking vm3 allocation at host 1
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3, host1));
		Assert.assertEquals(host1, vm3.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm3), vm3.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm3), host1);
		Assert.assertEquals(3, host1.getVmList().size());
		Assert.assertEquals(0, host2.getVmList().size());
		Assert.assertEquals(3, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm3.getUid()));

		// checking vm4 allocation at host 2
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4, host2));
		Assert.assertEquals(host2, vm4.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm4), vm4.getHost());
		Assert.assertEquals(preemptablePolicy.getHost(vm4), host2);
		Assert.assertEquals(3, host1.getVmList().size());
		Assert.assertEquals(1, host2.getVmList().size());
		Assert.assertEquals(4, preemptablePolicy.getVmTable().size());
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm1.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm2.getUid()));
		Assert.assertEquals(host1, preemptablePolicy.getVmTable().get(vm3.getUid()));
		Assert.assertEquals(host2, preemptablePolicy.getVmTable().get(vm4.getUid()));
	}

	@Test
	public void testGetHostByUserId(){
		PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm2 = new PreemptableVm(2, 1, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm3 = new PreemptableVm(3, 2, 1.0, 1.0, 0, 0, 0);
		PreemptableVm vm4 = new PreemptableVm(4, 3, 1.0, 1.0, 0, 0, 0);

		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1, host1));
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host1));
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3, host1));
		Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4, host2));

		Assert.assertEquals(host1, preemptablePolicy.getHost(1, 1));
		Assert.assertEquals(host1, preemptablePolicy.getHost(2, 1));
		Assert.assertEquals(host1, preemptablePolicy.getHost(3, 2));
		Assert.assertEquals(host2, preemptablePolicy.getHost(4, 3));
		
		// checking invalid parameters
		Assert.assertNull(preemptablePolicy.getHost(5, 3));
		Assert.assertNull(preemptablePolicy.getHost(2, 4));
	}

	@Test
	public void testPriorityToSortedHostsMap(){
		// creating 3 hosts with 3 priorities
		List<PreemptiveHost> listaHosts = new ArrayList<PreemptiveHost>();
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100.5)));
		
		properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, String.valueOf(NUMBER_OF_PRIORITIES));
		
		host1 = new PreemptiveHost(1, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

		host2 = new PreemptiveHost(2, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

		host3 = new PreemptiveHost(3, peList1,
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));
		listaHosts.add(host1);
		listaHosts.add(host2);
		listaHosts.add(host3);

		// creating policy with these hosts
		preemptablePolicy = new PreemptableVmAllocationPolicy(listaHosts, "");
		preemptablePolicy.setHostSelector(hostSelector);

		// asserting that the HashMap has 3 elements mapping priority to a sortedList
		Assert.assertTrue(preemptablePolicy.getPriorityToSortedHost().size() == NUMBER_OF_PRIORITIES);


		// asserting that each priority has a sortedHostList with length = 3
		// asserting that the preemptiveHosts has the same elements as the sortedHostList in each priority
		for (int i = 0; i < host1.getNumberOfPriorities(); i++){
			Assert.assertTrue(preemptablePolicy.getPriorityToSortedHost().get(i).size() == 3);
			Assert.assertArrayEquals(preemptablePolicy.getHostList().toArray(), preemptablePolicy.getPriorityToSortedHost().get(i).toArray());
		}

		// allocating VM with priority 1
		PreemptableVm vmPriority1 = new PreemptableVm(1, 1, 50.2, 1.0, 0, 1, 0);
		Mockito.when(hostSelector.select(preemptablePolicy.getPriorityToSortedHost().get(vmPriority1.getPriority()), vmPriority1)).thenReturn(host1);
		preemptablePolicy.allocateHostForVm(vmPriority1);

		// testing if the sortedHost for priority0 is the same
		List<PreemptiveHost> expectedListPriority0 = new ArrayList<>();
		expectedListPriority0.add(host1);
		expectedListPriority0.add(host2);
		expectedListPriority0.add(host3);
		Assert.assertArrayEquals(preemptablePolicy.getPriorityToSortedHost().get(PRIORITY_0).toArray(), expectedListPriority0.toArray());
		Assert.assertEquals(host1.getAvailableMipsByPriority(PRIORITY_0), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host2.getAvailableMipsByPriority(PRIORITY_0), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host3.getAvailableMipsByPriority(PRIORITY_0), 100.5, ACCEPTABLE_DIFERENCE);

		// testing if the sortedHost for priority1 has changed
		List<PreemptiveHost> expectedListPriority1 = new ArrayList<>();
		expectedListPriority1.add(host2);
		expectedListPriority1.add(host3);
		expectedListPriority1.add(host1);
		Assert.assertArrayEquals(preemptablePolicy.getPriorityToSortedHost().get(PRIORITY_1).toArray(), expectedListPriority1.toArray());
		Assert.assertEquals(host1.getAvailableMipsByPriority(PRIORITY_1), 50.3, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host2.getAvailableMipsByPriority(PRIORITY_1), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host3.getAvailableMipsByPriority(PRIORITY_1), 100.5, ACCEPTABLE_DIFERENCE);

		// testing if the sortedHost for priority2 has changed
		List<PreemptiveHost> expectedListPriority2 = new ArrayList<>();
		expectedListPriority2.add(host2);
		expectedListPriority2.add(host3);
		expectedListPriority2.add(host1);
		Assert.assertArrayEquals(preemptablePolicy.getPriorityToSortedHost().get(PRIORITY_2).toArray(), expectedListPriority2.toArray());
		Assert.assertEquals(host1.getAvailableMipsByPriority(PRIORITY_2), 50.3, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host2.getAvailableMipsByPriority(PRIORITY_2), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host3.getAvailableMipsByPriority(PRIORITY_2), 100.5, ACCEPTABLE_DIFERENCE);

		// allocating VM with priority 2
		PreemptableVm vmPriority2 = new PreemptableVm(1, 1, 50.3, 1.0, 0, 2, 0);
		Mockito.when(hostSelector.select(preemptablePolicy.getPriorityToSortedHost().get(vmPriority2.getPriority()), vmPriority2)).thenReturn(host2);
		preemptablePolicy.allocateHostForVm(vmPriority2);

		// testing if the sortedHost for priority0 is the same
		Assert.assertArrayEquals(preemptablePolicy.getPriorityToSortedHost().get(PRIORITY_0).toArray(), expectedListPriority0.toArray());
		Assert.assertEquals(host1.getAvailableMipsByPriority(PRIORITY_0), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host2.getAvailableMipsByPriority(PRIORITY_0), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host3.getAvailableMipsByPriority(PRIORITY_0), 100.5, ACCEPTABLE_DIFERENCE);


		// testing if the sortedHost for priority1 is the same
		Assert.assertArrayEquals(preemptablePolicy.getPriorityToSortedHost().get(PRIORITY_1).toArray(), expectedListPriority1.toArray());
		Assert.assertEquals(host1.getAvailableMipsByPriority(PRIORITY_1), 50.3, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host2.getAvailableMipsByPriority(PRIORITY_1), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host3.getAvailableMipsByPriority(PRIORITY_1), 100.5, ACCEPTABLE_DIFERENCE);


		// testing if the sortedHost for priority2 has changed
		expectedListPriority2.clear();
		expectedListPriority2.add(host3);
		expectedListPriority2.add(host1);
		expectedListPriority2.add(host2);
		Assert.assertArrayEquals(preemptablePolicy.getPriorityToSortedHost().get(PRIORITY_2).toArray(), expectedListPriority2.toArray());
		Assert.assertEquals(host1.getAvailableMipsByPriority(PRIORITY_2), 50.3, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host2.getAvailableMipsByPriority(PRIORITY_2), 50.2, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host3.getAvailableMipsByPriority(PRIORITY_2), 100.5, ACCEPTABLE_DIFERENCE);


		// allocating VM with priority 1 again
		PreemptableVm vmPriority1_2 = new PreemptableVm(1, 1, 4.2, 1.0, 0, 1, 0);
		Mockito.when(hostSelector.select(preemptablePolicy.getPriorityToSortedHost().get(vmPriority1_2.getPriority()), vmPriority1_2)).thenReturn(host3);
		preemptablePolicy.allocateHostForVm(vmPriority1_2);

		// testing if the sortedHost for priority0 is the same
		Assert.assertArrayEquals(preemptablePolicy.getPriorityToSortedHost().get(PRIORITY_0).toArray(), expectedListPriority0.toArray());
		Assert.assertEquals(host1.getAvailableMipsByPriority(PRIORITY_0), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host2.getAvailableMipsByPriority(PRIORITY_0), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host3.getAvailableMipsByPriority(PRIORITY_0), 100.5, ACCEPTABLE_DIFERENCE);

		// testing if the sortedHost for priority1 is the same, changing only the available mips for this priority at host3
		Assert.assertArrayEquals(preemptablePolicy.getPriorityToSortedHost().get(PRIORITY_1).toArray(), expectedListPriority1.toArray());
		Assert.assertEquals(host1.getAvailableMipsByPriority(PRIORITY_1), 50.3, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host2.getAvailableMipsByPriority(PRIORITY_1), 100.5, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host3.getAvailableMipsByPriority(PRIORITY_1), 96.3, ACCEPTABLE_DIFERENCE);

		// testing if the sortedHost for priority2 is the same, changing only the available mips for this priority at host3
		Assert.assertArrayEquals(preemptablePolicy.getPriorityToSortedHost().get(PRIORITY_2).toArray(), expectedListPriority2.toArray());
		Assert.assertEquals(host1.getAvailableMipsByPriority(PRIORITY_2), 50.3, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host2.getAvailableMipsByPriority(PRIORITY_2), 50.2, ACCEPTABLE_DIFERENCE);
		Assert.assertEquals(host3.getAvailableMipsByPriority(PRIORITY_2), 96.3, ACCEPTABLE_DIFERENCE);

	}
}
