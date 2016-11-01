package org.cloudbus.cloudsim.preemption;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.datastore.DatacenterUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.HostUsageDataStore;
import org.cloudbus.cloudsim.preemption.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PreemptiveDatacenterTest {

    private static final double ACCEPTABLE_DIFFERENCE = 0.000001;

    PreemptiveDatacenter datacenter;
    PreemptiveHost host;
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

        host = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
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

        List<PreemptiveHost> googleHostList = new ArrayList<PreemptiveHost>();
        for (Host host : hostList) {
            googleHostList.add((PreemptiveHost) host);
        }

        preemptableVmAllocationPolicy = new PreemptableVmAllocationPolicy(googleHostList, hostSelector);

        Properties properties = Mockito.mock(Properties.class);
        Mockito.when(properties.getProperty(HostUsageDataStore.DATABASE_URL_PROP)).thenReturn("jdbc:sqlite:outputUtilizationTest.sqlite3");
        Mockito.when(properties.getProperty(DatacenterUsageDataStore.DATABASE_URL_PROP)).thenReturn("jdbc:sqlite:outputDatacenterTest.sqlite3");


        datacenter = new PreemptiveDatacenter("datacenter",
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

        PreemptableVm vm0 = new PreemptableVm(1, 1, 5, 1, 0, priority, runtime);

        datacenter.allocateHostForVm(false, vm0, null, false);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        // checking number of preemptions, backfilling and migrations
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
    }

    @Test
    public void testAllocateTwoVmWithSamePriority() {
        int priority = 0;
        double runtime = 10;
        PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 1.0, 0, priority, runtime);

        // allocating first vm
        datacenter.allocateHostForVm(false, vm0, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 1.0, 0, priority, runtime);

        // allocating second vm
        datacenter.allocateHostForVm(false, vm1, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);

    }

    @Test
    public void testAllocateThreeVmWithSamePriorityWithWaiting() {
        int priority = 0;
        double runtime = 10;
        PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 1.0, 0, priority, runtime);

        // allocating first vm
        datacenter.allocateHostForVm(false, vm0, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 1.0, 0, priority, runtime);

        // allocating second vm
        datacenter.allocateHostForVm(false, vm1, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);

        PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 1.0, 0, priority, runtime);

        // checking and simulating host selector
        Assert.assertFalse(host.isSuitableForVm(vm2));
        Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);

        // allocating third vm
        datacenter.allocateHostForVm(false, vm2, null, false);

        // checking
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
    }


    @Test
    public void testVmDestroy() {
        int priority = 0;
        double runtime = 10;

        // allocating first Vm
        PreemptableVm vm0 = new PreemptableVm(1, 1, 5, 1, 0, priority, runtime);
        datacenter.allocateHostForVm(false, vm0, null, false);

        //checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        //Destroy Vm0
        Mockito.when(timeUtil.clock()).thenReturn(runtime);
        SimEvent destroyVm = Mockito.mock(SimEvent.class);
        Mockito.when(destroyVm.getData()).thenReturn((Object) vm0);
        datacenter.processVmDestroy(destroyVm, false);

        //checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
    }

    @Test
    public void testVmDestroyWithTwoVm() {
        int priority = 0;
        double runtime = 10;
        PreemptableVm vm0 = new PreemptableVm(0, 1, 4.9, 1.0, 0, priority, 0);

        // allocating first vm
        datacenter.allocateHostForVm(false, vm0, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        PreemptableVm vm1 = new PreemptableVm(1, 1, 5.1, 1.0, 0, priority, runtime + 0.1);

        // allocating second vm
        datacenter.allocateHostForVm(false, vm1, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);

        //destroying vm0
        Mockito.when(timeUtil.clock()).thenReturn(runtime);
        SimEvent destroyVm = Mockito.mock(SimEvent.class);
        Mockito.when(destroyVm.getData()).thenReturn((Object) vm0);

        datacenter.processVmDestroy(destroyVm, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().first());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
    }

    @Test
    public void testVmDestroyWithVmForScheduling() {

        int priority = 0;
        double runtime = 10;
        PreemptableVm vm0 = new PreemptableVm(0, 1, 5.1, 1.0, 0, priority, runtime - 0.1);

        // allocating first vm
        datacenter.allocateHostForVm(false, vm0, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        PreemptableVm vm1 = new PreemptableVm(1, 1, 4.9, 1.0, 0, priority, runtime);

        // allocating second vm
        datacenter.allocateHostForVm(false, vm1, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);

        PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 1.0, 0, priority, runtime);

        // checking and simulating host selector
        Assert.assertFalse(host.isSuitableForVm(vm2));
        Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);

        // allocating third vm
        datacenter.allocateHostForVm(false, vm2, null, false);

        // checking
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);

    }

    @Test
    public void testVmDestroyWithVmForScheduling2() {

        int priority = 0;
        double runtime = 10;
        PreemptableVm vm0 = new PreemptableVm(0, 1, 5.1, 1.0, 0, priority, runtime - 0.1);

        // allocating first vm
        datacenter.allocateHostForVm(false, vm0, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);

        PreemptableVm vm1 = new PreemptableVm(1, 1, 4.9, 1.0, 0, priority, runtime);

        // allocating second vm
        datacenter.allocateHostForVm(false, vm1, null, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);

        PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 1.0, 0, priority, runtime);

        // checking and simulating host selector
        Assert.assertFalse(host.isSuitableForVm(vm2));
        Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);

        // allocating third vm
        datacenter.allocateHostForVm(false, vm2, null, false);

        // checking
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        PreemptableVm vm3 = new PreemptableVm(3, 1, 0.1, 1.0, 0, priority, runtime);

        // checking and simulating host selector
        Assert.assertFalse(host.isSuitableForVm(vm2));
        Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm3)).thenReturn(null);

        // allocating third vm
        datacenter.allocateHostForVm(false, vm3, null, false);

        // checking
        Assert.assertEquals(2, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(vm3, datacenter.getVmsForScheduling().last());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);


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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
    }

    @Test
    public void testVmDestroyWithPreempt() {

        int priority = 1;
        double runtime = 10;
        double cpuReq = 0.0000001;
        double ACCEPTABLE_DIFFERENCE = 0.000000001;

        PreemptableVm vm0 = new PreemptableVm(0, 1, 2 * cpuReq, 1.0, 0, priority - 1,
                runtime - 0.5);
        PreemptableVm vm1 = new PreemptableVm(1, 1, 2 * cpuReq, 1.0, 0, priority, 0.4);
        PreemptableVm vm2 = new PreemptableVm(2, 1, cpuReq, 1.0, 0, priority + 1, 0.1);
        PreemptableVm vm3 = new PreemptableVm(3, 1, 9.9999998, 1.0, 0, priority - 1,
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
        datacenter.allocateHostForVm(false, vm3, null, false);

        // checking
        Assert.assertEquals(2 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);

        // allocating vm2 with priority 2
        datacenter.allocateHostForVm(false, vm2, null, false);
        Assert.assertEquals(1 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm2, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);

        // allocating vm1 with priority 1 to preempt vm2
        datacenter.allocateHostForVm(false, vm1, null, false);
        Assert.assertEquals(0 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);

        // allocating vm0 with priority 0 to preempt vm1
        datacenter.allocateHostForVm(false, vm0, null, false);
        Assert.assertEquals(0 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(vm1, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().last());
        Assert.assertEquals(2, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);

        // finishing vm0 to reallocate vm1
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.5);
        Mockito.when(destroyVm.getData()).thenReturn((Object) vm0);
        datacenter.processVmDestroy(destroyVm, false);

        Assert.assertEquals(0 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);

        // finishing vm1 to reallocate vm2
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
        Mockito.when(destroyVm.getData()).thenReturn((Object) vm1);
        datacenter.processVmDestroy(destroyVm, false);

        Assert.assertEquals(1 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);

        // checking number of preemptions and backfilling
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm2, datacenter.getVmsRunning().last());
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);

        // finishing vm2
        Mockito.when(timeUtil.clock()).thenReturn(runtime + 0.1);
        Mockito.when(destroyVm.getData()).thenReturn((Object) vm2);
        datacenter.processVmDestroy(destroyVm, false);

        // checking
        Assert.assertEquals(2 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);

        // checking number of preemptions and backfilling
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);

        // finishing vm3 to return to initial state
        Mockito.when(destroyVm.getData()).thenReturn((Object) vm3);
        datacenter.processVmDestroy(destroyVm, false);

        Assert.assertEquals(10, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
    }

    @Test
    public void testVmDestroyWithVmForSchedulingDifferentPriorities() {
        double runtime = 10;
        PreemptableVm vm0P0 = new PreemptableVm(0, 1, 5, 1.0, 0, 0, runtime - 0.1);

        // allocating vm priority 0
        datacenter.allocateHostForVm(false, vm0P0, null, false);

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

        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);

        PreemptableVm vm1P1 = new PreemptableVm(1, 1, 2, 1.0, 0, 1, runtime);

        // allocating vm priority 1
        datacenter.allocateHostForVm(false, vm1P1, null, false);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);

        // allocating vm priority 2
        PreemptableVm vm2P2 = new PreemptableVm(2, 1, 2, 1.0, 0, 2, runtime);

        datacenter.allocateHostForVm(false, vm2P2, null, false);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 0);

        // allocating vm priority 0
        PreemptableVm vm3P0 = new PreemptableVm(3, 1, 3, 1.0, 0, 0, runtime);

        datacenter.allocateHostForVm(false, vm3P0, null, false);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);
    }

    @Test
    public void testVmDestroyWithVmForSchedulingDifferentPriorities2() {
        double runtime = 10;

        List<PreemptableVm> vmsP2 = new ArrayList<PreemptableVm>();

        // creating vms priorities 2
        for (int i = 0; i < 10; i++) {
            PreemptableVm vmP2 = new PreemptableVm(i, 1, 1.0, 1.0, 0, 2, runtime);
            datacenter.allocateHostForVm(false, vmP2, null, false);

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

        for (PreemptableVm vm: vmsP2){
            Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        List<PreemptableVm> vmsP1 = new ArrayList<PreemptableVm>();

        // creating vms priorities 1
        for (int i = 0; i < 10; i++) {
            PreemptableVm vmP1 = new PreemptableVm(i + 10, 1, 1.0, 1.0, 0, 1,
                    runtime - 0.1);

            datacenter.allocateHostForVm(false, vmP1, null, false);

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

            for (PreemptableVm vm: datacenter.getVmsForScheduling()){
                Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            }
        }

        for (PreemptableVm vm: vmsP2){
            Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        // asserting all vmsForScheduling have priority 2
        for (PreemptableVm vmP2 : vmsP2) {
            Assert.assertTrue(datacenter.getVmsForScheduling().contains(vmP2));
        }
        Assert.assertEquals(vmsP2.size(), datacenter.getVmsForScheduling()
                .size());

        // asserting all vmsForRunning have priority 1
        for (PreemptableVm vmP1 : vmsP1) {
            Assert.assertTrue(datacenter.getVmsRunning().contains(vmP1));
        }
        Assert.assertEquals(vmsP1.size(), datacenter.getVmsRunning().size());

        PreemptableVm vmP0 = new PreemptableVm(20, 1, 5, 1.0, 0, 0, runtime - 0.1);
        // allocating vm priority 0
        datacenter.allocateHostForVm(false, vmP0, null, false);

        // checking
        Assert.assertEquals(15, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(6, datacenter.getVmsRunning().size());
        Assert.assertEquals(vmP0, datacenter.getVmsRunning().first());
        for (PreemptableVm vm: datacenter.getVmsForScheduling()){
            Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        for (PreemptableVm vm: datacenter.getVmsRunning()){
            Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        // asserting all vms priority2 are in vmsForScheduling
        for (PreemptableVm vmP2 : vmsP2) {
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

        for (PreemptableVm vm: datacenter.getVmsForScheduling()){
            Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        for (PreemptableVm vm: datacenter.getVmsRunning()){
            if (vm.equals(vmsP1.get(5))){
                Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            } else {
                Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            }

        }

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


        for (PreemptableVm vm: datacenter.getVmsForScheduling()){
            Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        for (PreemptableVm vm: datacenter.getVmsRunning()){
            if (vm.equals(vmsP1.get(5)) || vm.equals(vmsP1.get(6))){
                Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            } else {
                Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            }

        }

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

        for (PreemptableVm vm: datacenter.getVmsForScheduling()){
            Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        for (PreemptableVm vm: datacenter.getVmsRunning()){
            if (vmsP2.contains(vm) || vmsP1.indexOf(vm) > 4){
                Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            } else {
                Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            }

        }

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

        PreemptableVm vm0P0 = new PreemptableVm(0, 1, 5, 1.0, 0, 0, runtime - 0.1);

        // allocating first vm
        datacenter.allocateHostForVm(false, vm0P0, null, false);
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

        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);

        PreemptableVm vm1P1 = new PreemptableVm(1, 1, 2, 1.0, 0, 1, runtime);

        // allocating second vm
        datacenter.allocateHostForVm(false, vm1P1, null, false);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);

        PreemptableVm vm2P2 = new PreemptableVm(2, 1, 3, 1.0, 0, 2, runtime);

        // allocating third vm
        datacenter.allocateHostForVm(false, vm2P2, null, false);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);

        // allocation one more VM with priority 0
        PreemptableVm vm3P0 = new PreemptableVm(3, 1, 6, 1.0, 0, 0, runtime);

        // checking and simulating host selector
        Assert.assertFalse(host.isSuitableForVm(vm3P0));
        Mockito.when(
                hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(0), vm3P0)).thenReturn(null);

        datacenter.allocateHostForVm(false, vm3P0, null, false);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);

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

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);
    }

    @Test
    public void testBackfilling(){
        double runtime = 10;

        // creating VMs with different priorities
        PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 1.0, 0, 0, runtime - 0.3); // 5 mips
        PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 1.0, 0, 0, runtime - 0.2); // 5 mips
        PreemptableVm vm2 = new PreemptableVm(2, 1, 6, 1.0, 0, 0, runtime); // 6 mips
        PreemptableVm vm3 = new PreemptableVm(3, 1, 4, 1.0, 0, 1, runtime); // 4 mips
        PreemptableVm vm4 = new PreemptableVm(4, 1, 1, 1.0, 0, 2, runtime); // 1 mips

        // trying to allocate all Vms. At the first moment, just Vm0 and Vm1 will be allocated
        datacenter.allocateHostForVm(false, vm0, null, false);
        datacenter.allocateHostForVm(false, vm1, null, false);
        datacenter.allocateHostForVm(false, vm2, null, false);
        datacenter.allocateHostForVm(false, vm3, null, false);
        datacenter.allocateHostForVm(false, vm4, null, false);


        // asserting that any Vm was preempted or was chose to backfilling
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm4.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm4.getNumberOfBackfillingChoice(), 0);


        // destroying Vm0. So, Vm3 and Vm4 will be allocated, because we have 5 mips free
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.3);
        SimEvent destroyVm = Mockito.mock(SimEvent.class);
        Mockito.when(destroyVm.getData()).thenReturn((Object) vm0);
        datacenter.processVmDestroy(destroyVm, false);

        // asserting that Vm3 and Vm4 were chose by backfilling
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 1);
        Assert.assertEquals(vm4.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm4.getNumberOfBackfillingChoice(), 1);

        // destroying Vm1. So, Vm4 will be preempted to allocate Vm2
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.2);
        destroyVm = Mockito.mock(SimEvent.class);
        Mockito.when(destroyVm.getData()).thenReturn((Object) vm1);
        datacenter.processVmDestroy(destroyVm, false);


        // asserting that Vm3 and Vm4 were chose by backfilling, and Vm4 was preempted once
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 1);
        Assert.assertEquals(vm4.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm4.getNumberOfBackfillingChoice(), 1);



    }

    @Test
    public void testVmDestroyWithPreempt2() {

        Log.disable();

        double ACCEPTABLE_DIFFERENCE = 0.000000001;

        //setting host on data center with capacity of google trace
        double hostCpuCapacity = 6603.25;
        int numberOfPriorities = 3;
        List<Pe> peList1 = new ArrayList<Pe>();
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCpuCapacity)));

        host = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), numberOfPriorities);
        datacenter.getHostList().remove(0);
        datacenter.getHostList().add(host);
        datacenter.getVmAllocationPolicy().setSimulationTimeUtil(timeUtil);

        //asserting host on data center and host total capacity
        Assert.assertEquals(host, datacenter.getHostList().get(0));
        Assert.assertEquals(hostCpuCapacity, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        // creating vms model P0S0, total of vms 6603
        // with cpu total requisition of 3301.5

        int numberOfVms = 6603;
        int vmId = 0;
        int priority = 0;
        double runtime = 8;
        double subtime = 0;
        double cpuReq = 0.5;

        List<Vm> vmP0S0 = new ArrayList<>(numberOfVms);

        for (int i = 0; i < numberOfVms; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP0S0.add(vm);
        }

        // creating vms model P1S0, total of vms 6603
        // with cpu total requisition of 1980.9

        priority = 1;
        runtime = 5;
        cpuReq = 0.3;

        List<Vm> vmP1S0 = new ArrayList<>(numberOfVms);

        for (int i = 0; i < numberOfVms; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP1S0.add(vm);
        }

        // creating vms model P2S0, total of vms 6603
        // with cpu total requisition of 1320.6

        priority = 2;
        runtime = 2;
        cpuReq = 0.2;

        List<Vm> vmP2S0 = new ArrayList<>(numberOfVms);

        for (int i = 0; i < numberOfVms; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP2S0.add(vm);
        }

        // creating vms model P0S1, total of vms 6603
        // with cpu total requisition of 3961.8

        priority = 0;
        runtime = 2;
        subtime = 1;
        cpuReq = 0.6;

        List<Vm> vmP0S1 = new ArrayList<>(numberOfVms);

        for (int i = 0; i < numberOfVms; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP0S1.add(vm);
        }

        //allocating vms with submit time 0
        executingSimularionRuntime0(ACCEPTABLE_DIFFERENCE, hostCpuCapacity, numberOfVms, vmP0S0, vmP1S0, vmP2S0);

        //allocating vms with submit time 1
        executingSimulationRuntime1(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

        //executing simulation at runtime 2
        executingSimulationRuntime2(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

        //executing simulation to verify preemption and running of vms through running time of simulation

        SimEvent destroyVm = Mockito.mock(SimEvent.class);

        executingSimulationRuntime3(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1, destroyVm);
        executingSimulationRuntime4(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1, destroyVm);
        executingSimulationRuntime5(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1, destroyVm);
        executingSimulationRuntime6(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1, destroyVm);
        executingSimulationRuntime7(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1, destroyVm);
        executingSimulationRuntime8(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1, destroyVm);

        double ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY = 0.01;

        double finishTime = 8.0;
        // asserting VM availability of P0S0
        for (int i = 0; i < numberOfVms; i++) {
            PreemptableVm vm = (PreemptableVm) vmP0S0.get(i);
            Assert.assertEquals(1, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }

        finishTime = 7.0;
        // asserting VM availability of P1S0
        for (int i = 0; i < numberOfVms; i++) {
            PreemptableVm vm = (PreemptableVm) vmP1S0.get(i);
            Assert.assertEquals(0.714, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }

        // asserting VM availability of P2S0
        finishTime = 4.0;
        for (int i = 0; i < 3301; i++) {
            PreemptableVm vm = (PreemptableVm) vmP2S0.get(i);
            Assert.assertEquals(0.5, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }


        finishTime = 5.0;
        for (int i = 3301; i < 6602; i++) {
            PreemptableVm vm = (PreemptableVm) vmP2S0.get(i);
            Assert.assertEquals(0.4, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }

        finishTime = 6.0;
        for (int i = 6602; i < numberOfVms; i++) {
            PreemptableVm vm = (PreemptableVm) vmP2S0.get(i);
            Assert.assertEquals(0.33, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }

        finishTime = 3.0;
        // asserting VM availability of P0S1
        for (int i = 0; i < 5502; i++) {
            PreemptableVm vm = (PreemptableVm) vmP0S1.get(i);
            Assert.assertEquals(1, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }


        finishTime = 5.0;
        // asserting VM availability of P0S1
        for (int i = 5502; i < numberOfVms; i++) {
            PreemptableVm vm = (PreemptableVm) vmP0S1.get(i);
            Assert.assertEquals(0.5, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }


    }

    private void executingSimulationRuntime8(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1, SimEvent destroyVm) {
        // passing time to 8
        Mockito.when(timeUtil.clock()).thenReturn(8.0);

        // finishing vms of priority 0, submit time 0, runtime 8 that are finished in simulation time 7

        for (int i = 0; i < numberOfVms; i++) {

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP1S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP2S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S1.get(i));
            datacenter.processVmDestroy(destroyVm, false);
        }

        //testing new available considering the end of 6603 vms described before

        Assert.assertEquals(6603.25, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(6603.25, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(6603.25, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(6603.25, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

        testNumberOfPreemptionsAndBackfillingChoices(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
    }

    private void executingSimulationRuntime7(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1, SimEvent destroyVm) {
        // passing time to 7
        Mockito.when(timeUtil.clock()).thenReturn(7.0);

        // finishing vms of priority 1, submit time 0, runtime 5

        for (int i = 0; i < numberOfVms; i++) {

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP1S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP2S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S1.get(i));
            datacenter.processVmDestroy(destroyVm, false);
        }

        //testing new available considering the end of vms described above
        Assert.assertEquals(3301.75, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(6603, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingChoices(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
    }

    private void executingSimulationRuntime6(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1, SimEvent destroyVm) {
        // passing time to 6
        Mockito.when(timeUtil.clock()).thenReturn(6.0);

        // finishing the last one VM with priority 2

        for (int i = 0; i < numberOfVms; i++) {

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP1S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP2S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S1.get(i));
            datacenter.processVmDestroy(destroyVm, false);
        }

        //testing new available considering the end of vms described above
        Assert.assertEquals(1320.85, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1320.85, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1320.85, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2 * 6603, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingChoices(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
    }

    private void executingSimulationRuntime5(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1, SimEvent destroyVm) {
        // passing time to 5
        Mockito.when(timeUtil.clock()).thenReturn(5.0);

        // finishing vms of priority 2, submit time 0, runtime 2 and
        // finishing vms of priority 0, submit time 1, runtime 2
        // both are finished at time 5

        for (int i = 0; i < numberOfVms; i++) {

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP1S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP2S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S1.get(i));
            datacenter.processVmDestroy(destroyVm, false);
        }

        //testing new available considering the end of vms described before, and reallocate of
        /*after deallocate 3301 vms of vmP2S0 available mips are 660.25
		* after deallocate 1101 vms of P0S1 available mips are 1320.85
		* after allocate 1 vm of vmP2S0, the available mips are 660.05
		* */
        Assert.assertEquals(1320.65, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1320.85, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1320.65, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(13207, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingChoices(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
    }

    private void executingSimulationRuntime4(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1, SimEvent destroyVm) {
        // passing time to 4
        Mockito.when(timeUtil.clock()).thenReturn(4.0);

        // finishing vms of priority 2, submit time 0, and runtime 2 that are finished at time 4

        for (int i = 0; i < numberOfVms; i++) {

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP1S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP2S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S1.get(i));
            datacenter.processVmDestroy(destroyVm, false);
        }

        //testing new available considering the end of vms described above, and reallocate of
		/*after deallocate of 3301 vms of vmP2S0 available mips are 660.25
		* after allocate 3301 vms of vmP2S0, the available mips are 0.05
		* */
        Assert.assertEquals(0.05, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2641.15, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(660.25, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.05, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(17608, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingChoices(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
    }

    private void executingSimulationRuntime3(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1, SimEvent destroyVm) {
        // passing time to 3
        Mockito.when(timeUtil.clock()).thenReturn(3.0);

        // finishing vms of priority 0, submit time 1, and runtime 2, because the runtime is completed at time 3
        for (int i = 0; i < numberOfVms; i++) {

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP1S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP2S0.get(i));
            datacenter.processVmDestroy(destroyVm, false);

            Mockito.when(destroyVm.getData()).thenReturn((Object) vmP0S1.get(i));
            datacenter.processVmDestroy(destroyVm, false);
        }

        //testing new available considering the end of vms described above, and reallocating of
		/*after deallocate available mips is 3301.75
		* after allocate remaining vms of vmP0S1, the available mips is 2641.15
		* after allocate all vms(6603) of vmP1S0, the available mips is 660.25
		* after allocate 3301 vms of vmP2S0, the available mips are 0.05
		* */
        Assert.assertEquals(0.05, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2641.15, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(660.25, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.05, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertEquals(3302, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(17608, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingChoices(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
    }

    private void executingSimulationRuntime2(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        testNumberOfPreemptionsAndBackfillingChoicesTimeLessThan3(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
    }

    private void executingSimulationRuntime1(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0
        Mockito.when(timeUtil.clock()).thenReturn(1.0);

        //allocate 6603 vms os priority 0, submit time 1, and Cpu requisition of 0.6
        //with total requested Cpu equals 3961.8
        for (int i = 0; i < numberOfVms; i++) {
            datacenter.allocateHostForVm(false, (PreemptableVm) vmP0S1.get(i), null, false);
        }

        //testing capacity of host
        //TODO discuss about imprecision on results
        //TODO results can be different because of backfilling and vms of priority 1 and 2 can not be preempted reallocated at same time
        Assert.assertEquals(0.55, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.55, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.55, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.55, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        //TODO results can be different because of backfilling and vms of priority 1 and 2 can not be preempted reallocated at same time
        Assert.assertEquals(14307, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(12105, datacenter.getVmsRunning().size()); //12107


        testNumberOfPreemptionsAndBackfillingChoicesTimeLessThan3(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
    }

    private void executingSimularionRuntime0(double ACCEPTABLE_DIFFERENCE, double hostCpuCapacity, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0) {
        // start time on 0 and mock the hostSelector to return desired host
        Mockito.when(timeUtil.clock()).thenReturn(0d);
        Mockito.when(hostSelector.select(Mockito.any(SortedSet.class), Mockito.any(Vm.class))).thenReturn(host);

        //allocate 6603 vms os priority 0, submit time 0, and Cpu requisition of 0.5
        //with total requested Cpu equals 3301.5
        for (int i = 0; i < numberOfVms; i++) {
            datacenter.allocateHostForVm(false, (PreemptableVm) vmP0S0.get(i), null, false);
        }

        //testing capacity of host
        Assert.assertEquals(hostCpuCapacity - 3301.5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCpuCapacity - 3301.5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCpuCapacity - 3301.5, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCpuCapacity - 3301.5, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(numberOfVms, datacenter.getVmsRunning().size());

        //allocate 6603 vms os priority 1, submit time 0, and Cpu requisition of 0.3
        //with total requested Cpu equals 1980.9

        for (int i = 0; i < numberOfVms; i++) {
            datacenter.allocateHostForVm(false, (PreemptableVm) vmP1S0.get(i), null, false);
        }

        //testing capacity of host
        Assert.assertEquals(hostCpuCapacity - 5282.4, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCpuCapacity - 3301.5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCpuCapacity - 5282.4, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCpuCapacity - 5282.4, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2 * numberOfVms, datacenter.getVmsRunning().size());

        //allocate 6603 vms of priority 2, submit time 0, and Cpu requisition of 0.2
        //with total requested Cpu equals 1320.6

        for (int i = 0; i < numberOfVms; i++) {
            datacenter.allocateHostForVm(false, (PreemptableVm) vmP2S0.get(i), null, false);
        }
        //testing capacity of host
        //TODO discuss about the imprecision of results (expetecd 0.25, and actual is 0.249999999186723)
        Assert.assertEquals(0.25, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1320.85, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.25, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(3 * numberOfVms, datacenter.getVmsRunning().size());


        // testing number of preemptions and number of backfilling choices for all vms
        for (int i = 0; i < numberOfVms; i++) {
            PreemptableVm actualVMP00 = (PreemptableVm) vmP0S0.get(i);
            PreemptableVm actualVMP10 = (PreemptableVm) vmP1S0.get(i);
            PreemptableVm actualVMP20 = (PreemptableVm) vmP2S0.get(i);

            Assert.assertEquals(actualVMP00.getNumberOfPreemptions(), 0);
            Assert.assertEquals(actualVMP10.getNumberOfPreemptions(), 0);
            Assert.assertEquals(actualVMP20.getNumberOfPreemptions(), 0);

            Assert.assertEquals(actualVMP00.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP10.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP20.getNumberOfBackfillingChoice(), 0);
        }


    }

    // asserting that just VMP10 id 6603 and VMP20 id 13206 were chose to backfilling once
    private void testNumberOfPreemptionsAndBackfillingChoices(int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        for (int i = 0; i < numberOfVms; i++) {

            PreemptableVm actualVMP00 = (PreemptableVm) vmP0S0.get(i);
            PreemptableVm actualVMP10 = (PreemptableVm) vmP1S0.get(i);
            PreemptableVm actualVMP20 = (PreemptableVm) vmP2S0.get(i);
            PreemptableVm actualVMP01 = (PreemptableVm) vmP0S1.get(i);

            if (actualVMP10.getId() == 6603) {
                Assert.assertEquals(actualVMP10.getNumberOfPreemptions(), 1);
                Assert.assertEquals(actualVMP10.getNumberOfBackfillingChoice(), 1);
            } else {
                Assert.assertEquals(actualVMP10.getNumberOfPreemptions(), 1);
                Assert.assertEquals(actualVMP10.getNumberOfBackfillingChoice(), 0);
            }

            if (actualVMP20.getId() == 13206) {
                Assert.assertEquals(actualVMP20.getNumberOfPreemptions(), 1);
                Assert.assertEquals(actualVMP20.getNumberOfBackfillingChoice(), 1);
            } else {
                Assert.assertEquals(actualVMP20.getNumberOfPreemptions(), 1);
                Assert.assertEquals(actualVMP20.getNumberOfBackfillingChoice(), 0);
            }

            Assert.assertEquals(actualVMP00.getNumberOfPreemptions(), 0);
            Assert.assertEquals(actualVMP01.getNumberOfPreemptions(), 0);

            Assert.assertEquals(actualVMP00.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP01.getNumberOfBackfillingChoice(), 0);
        }
    }

    // asserting that all vms with priority 1 and 2 were preempted once
    private void testNumberOfPreemptionsAndBackfillingChoicesTimeLessThan3(int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        for (int i = 0; i < numberOfVms; i++) {

            PreemptableVm actualVMP00 = (PreemptableVm) vmP0S0.get(i);
            PreemptableVm actualVMP10 = (PreemptableVm) vmP1S0.get(i);
            PreemptableVm actualVMP20 = (PreemptableVm) vmP2S0.get(i);
            PreemptableVm actualVMP01 = (PreemptableVm) vmP0S1.get(i);


            Assert.assertEquals(actualVMP10.getNumberOfPreemptions(), 1);
            Assert.assertEquals(actualVMP10.getNumberOfBackfillingChoice(), 0);

            Assert.assertEquals(actualVMP00.getNumberOfPreemptions(), 0);
            Assert.assertEquals(actualVMP01.getNumberOfPreemptions(), 0);
            Assert.assertEquals(actualVMP20.getNumberOfPreemptions(), 1);

            Assert.assertEquals(actualVMP00.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP01.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP20.getNumberOfBackfillingChoice(), 0);
        }
    }
}
