package org.cloudbus.cloudsim.preemption;

import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.datastore.DatacenterUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.HostUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.PreemptableVmDataStore;
import org.cloudbus.cloudsim.preemption.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PreemptiveDatacenterTest {

    private static final double ACCEPTABLE_DIFFERENCE = 0.000001;

    private PreemptiveDatacenter datacenter;
    private SimEvent event;
    private PreemptiveHost host;
    private SimulationTimeUtil timeUtil;
    private HostSelectionPolicy hostSelector;
    private PreemptableVmAllocationPolicy preemptableVmAllocationPolicy;
    private String datacenterFile;
    private String datacenterUrl;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {

        event = Mockito.mock(SimEvent.class);

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

        datacenterFile = "outputUtilizationTest.sqlite3";
        datacenterUrl = "jdbc:sqlite:" + datacenterFile;
        Properties properties = Mockito.mock(Properties.class);
        Mockito.when(properties.getProperty(HostUsageDataStore.DATABASE_URL_PROP)).thenReturn(datacenterUrl);
        Mockito.when(properties.getProperty(DatacenterUsageDataStore.DATABASE_URL_PROP)).thenReturn(datacenterUrl);
        Mockito.when(properties.getProperty("make_checkpoint")).thenReturn("yes");
        Mockito.when(properties.getProperty("checkpoint_interval_size")).thenReturn("3");
        Mockito.when(properties.getProperty("collect_datacenter_summary_info")).thenReturn("yes");
        Mockito.when(properties.getProperty("datacenter_storing_interval_size")).thenReturn("5");
        Mockito.when(properties.getProperty("datacenter_collect_info_interval_size")).thenReturn("5");

        datacenter = new PreemptiveDatacenter("datacenter",
                characteristics, preemptableVmAllocationPolicy,
                new LinkedList<Storage>(), 0, properties);

        datacenter.setSimulationTimeUtil(timeUtil);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());
    }

    @After
    public void tearDown() {
        new File(datacenterFile).delete();
    }

    @Test
    public void testAllocateVm() {
        int priority = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(1, 1, 5, 1, 0, priority, runtime);

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0);

        datacenter.processEvent(event);

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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions, backfilling and migrations
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        // checking number of preemptions, backfilling and migrations
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 1.0, 0, priority, runtime);

        // allocating second vm
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions, backfilling and migrations
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);

        PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 1.0, 0, priority, runtime);

        // checking and simulating host selector
        Assert.assertFalse(host.isSuitableForVm(vm2));
        Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(null);

        // allocating third vm
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm2);
        datacenter.processEvent(event);

        // checking
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions, backfilling and migrations
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

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        // checking number of preemptions, backfilling and migrations
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        //Destroy Vm0
        Mockito.when(timeUtil.clock()).thenReturn(runtime);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        //checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

        // checking number of preemptions, backfilling and migrations
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        // checking number of preemptions, backfilling and migrations
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        PreemptableVm vm1 = new PreemptableVm(1, 1, 5.1, 1.0, 0, priority, runtime + 0.1);

        // allocating second vm
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions, backfilling and migrations
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);

        //destroying vm0
        Mockito.when(timeUtil.clock()).thenReturn(runtime);

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().first());

        // checking number of preemptions, backfilling and migrations
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        // checking number of preemptions, backfilling and migrations
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        PreemptableVm vm1 = new PreemptableVm(1, 1, 4.9, 1.0, 0, priority, runtime);

        // allocating second vm
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions, backfilling and migrations
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm2);
        datacenter.processEvent(event);

        // checking
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions, backfilling and migrations
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm2, datacenter.getVmsRunning().last());

        // checking number of preemptions, backfilling and migrations
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);

        PreemptableVm vm1 = new PreemptableVm(1, 1, 4.9, 1.0, 0, priority, runtime);

        // allocating second vm
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1);
        datacenter.processEvent(event);

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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm2);
        datacenter.processEvent(event);

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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm3);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);

        //destroying vm0
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
        Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm2)).thenReturn(host);
        Mockito.when(hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority), vm3)).thenReturn(host);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event); // has an error in sum of available mips when allocating a vm
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
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm3);
        datacenter.processEvent(event);

        // checking
        Assert.assertEquals(2 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());

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
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);

        // allocating vm2 with priority 2
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm2);
        datacenter.processEvent(event);

        //checking state of data center
        Assert.assertEquals(1 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm2, datacenter.getVmsRunning().last());

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
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);

        // allocating vm1 with priority 1 to preempt vm2
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1);
        datacenter.processEvent(event);

        //checking state of data center
        Assert.assertEquals(0 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(vm2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);

        // allocating vm0 with priority 0 to preempt vm1
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        //checking state of data center
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
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);

        // finishing vm0 to reallocate vm1
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.5);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);

        // finishing vm1 to reallocate vm2
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm1);
        datacenter.processEvent(event);

        Assert.assertEquals(1 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);

        // checking number of preemptions and backfilling
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm2, datacenter.getVmsRunning().last());
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);

        // finishing vm2
        Mockito.when(timeUtil.clock()).thenReturn(runtime + 0.1);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm2);
        datacenter.processEvent(event);

        // checking
        Assert.assertEquals(2 * cpuReq, host.getAvailableMips(),
                ACCEPTABLE_DIFFERENCE);

        // checking number of preemptions and backfilling
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm3, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);

        // finishing vm3 to return to initial state
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm3);
        datacenter.processEvent(event);

        Assert.assertEquals(10, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

        // checking number of preemptions and backfilling
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);
    }

    @Test
    public void testVmDestroyWithVmForSchedulingDifferentPriorities() {
        double runtime = 10;
        PreemptableVm vm0P0 = new PreemptableVm(0, 1, 5, 1.0, 0, 0, runtime - 0.1);

        // allocating vm priority 0
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0P0);
        datacenter.processEvent(event);

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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1P1);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0P0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfMigrations(), 0);

        // allocating vm priority 2
        PreemptableVm vm2P2 = new PreemptableVm(2, 1, 2, 1.0, 0, 2, runtime);

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm2P2);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0P0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2P2.getNumberOfMigrations(), 0);

        // allocating vm priority 0
        PreemptableVm vm3P0 = new PreemptableVm(3, 1, 3, 1.0, 0, 0, runtime);

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm3P0);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0P0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2P2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3P0.getNumberOfMigrations(), 0);

        // destroying vm0P0
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm0P0);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0P0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2P2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3P0.getNumberOfMigrations(), 0);
    }

    @Test
    public void testVmDestroyWithVmForSchedulingDifferentPriorities2() {
        double runtime = 10;

        List<PreemptableVm> vmsP2 = new ArrayList<PreemptableVm>();

        // creating vms priorities 2
        for (int i = 0; i < 10; i++) {
            PreemptableVm vmP2 = new PreemptableVm(i, 1, 1.0, 1.0, 0, 2, runtime);

            Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
            Mockito.when(event.getData()).thenReturn(vmP2);
            datacenter.processEvent(event);

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

        for (PreemptableVm vm : vmsP2) {
            Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        List<PreemptableVm> vmsP1 = new ArrayList<PreemptableVm>();

        // creating vms priorities 1
        for (int i = 0; i < 10; i++) {
            PreemptableVm vmP1 = new PreemptableVm(i + 10, 1, 1.0, 1.0, 0, 1,
                    runtime - 0.1);

            Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
            Mockito.when(event.getData()).thenReturn(vmP1);
            datacenter.processEvent(event);

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

            for (PreemptableVm vm : datacenter.getVmsForScheduling()) {
                Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            }
        }

        for (PreemptableVm vm : vmsP2) {
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vmP0);
        datacenter.processEvent(event);

        // checking
        Assert.assertEquals(15, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(6, datacenter.getVmsRunning().size());
        Assert.assertEquals(vmP0, datacenter.getVmsRunning().first());
        for (PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        for (PreemptableVm vm : datacenter.getVmsRunning()) {
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vmsP1.get(0));
        datacenter.processEvent(event);

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

        for (PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        for (PreemptableVm vm : datacenter.getVmsRunning()) {
            if (vm.equals(vmsP1.get(5))) {
                Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            } else {
                Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            }

        }

        // destroying vm priority 1
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vmsP1.get(1));
        datacenter.processEvent(event);

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


        for (PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        for (PreemptableVm vm : datacenter.getVmsRunning()) {
            if (vm.equals(vmsP1.get(5)) || vm.equals(vmsP1.get(6))) {
                Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            } else {
                Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            }

        }

        // destroying vm priority 0
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vmP0);
        datacenter.processEvent(event);

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

        for (PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        for (PreemptableVm vm : datacenter.getVmsRunning()) {
            if (vmsP2.contains(vm) || vmsP1.indexOf(vm) > 4) {
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0P0);
        datacenter.processEvent(event);

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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1P1);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0P0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfMigrations(), 0);

        PreemptableVm vm2P2 = new PreemptableVm(2, 1, 3, 1.0, 0, 2, runtime);

        // allocating third vm
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm2P2);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0P0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfMigrations(), 0);

        // allocation one more VM with priority 0
        PreemptableVm vm3P0 = new PreemptableVm(3, 1, 6, 1.0, 0, 0, runtime);

        // checking and simulating host selector
        Assert.assertFalse(host.isSuitableForVm(vm3P0));
        Mockito.when(
                hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(0), vm3P0)).thenReturn(null);

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm3P0);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0P0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3P0.getNumberOfMigrations(), 0);

        // destroying vm0P0
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm0P0);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0P0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3P0.getNumberOfMigrations(), 0);

        // destroying vm0P1
        Mockito.when(timeUtil.clock()).thenReturn(runtime);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm1P1);
        datacenter.processEvent(event);

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
        Assert.assertEquals(vm0P0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1P1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1P1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1P1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2P2.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm2P2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2P2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3P0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3P0.getNumberOfMigrations(), 0);
    }

    @Test
    public void testBackfilling() {
        double runtime = 10;

        // creating VMs with different priorities
        PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 1.0, 0, 0, runtime - 0.3); // 5 mips
        PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 1.0, 0, 0, runtime - 0.2); // 5 mips
        PreemptableVm vm2 = new PreemptableVm(2, 1, 6, 1.0, 0, 0, runtime); // 6 mips
        PreemptableVm vm3 = new PreemptableVm(3, 1, 4, 1.0, 0, 1, runtime); // 4 mips
        PreemptableVm vm4 = new PreemptableVm(4, 1, 1, 1.0, 0, 2, runtime); // 1 mips

        // trying to allocate all Vms. At the first moment, just Vm0 and Vm1 will be allocated
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);

        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);
        Mockito.when(event.getData()).thenReturn(vm1);
        datacenter.processEvent(event);
        Mockito.when(event.getData()).thenReturn(vm2);
        datacenter.processEvent(event);
        Mockito.when(event.getData()).thenReturn(vm3);
        datacenter.processEvent(event);
        Mockito.when(event.getData()).thenReturn(vm4);
        datacenter.processEvent(event);

        // asserting that any Vm was preempted or was chose to backfilling
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm4.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm4.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm4.getNumberOfMigrations(), 0);


        // destroying Vm0. So, Vm3 and Vm4 will be allocated, because we have 5 mips free
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.3);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm0);
        datacenter.processEvent(event);

        // asserting that Vm3 and Vm4 were chose by backfilling
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 1);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm4.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm4.getNumberOfBackfillingChoice(), 1);
        Assert.assertEquals(vm4.getNumberOfMigrations(), 0);

        // destroying Vm1. So, Vm4 will be preempted to allocate Vm2
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.2);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vm1);
        datacenter.processEvent(event);


        // asserting that Vm3 and Vm4 were chose by backfilling, and Vm4 was preempted once
        Assert.assertEquals(vm0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm0.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm3.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm3.getNumberOfBackfillingChoice(), 1);
        Assert.assertEquals(vm3.getNumberOfMigrations(), 0);
        Assert.assertEquals(vm4.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm4.getNumberOfBackfillingChoice(), 1);
        Assert.assertEquals(vm4.getNumberOfMigrations(), 0);
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
        List<Vm> vmP0S0 = new ArrayList<>(numberOfVms);
        List<Vm> vmP1S0 = new ArrayList<>(numberOfVms);
        List<Vm> vmP2S0 = new ArrayList<>(numberOfVms);
        List<Vm> vmP0S1 = new ArrayList<>(numberOfVms);

        populateVmLists(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

        //allocating vms with submit time 0
        executingSimularionRuntime0(ACCEPTABLE_DIFFERENCE, hostCpuCapacity, numberOfVms, vmP0S0, vmP1S0, vmP2S0);

        //allocating vms with submit time 1
        executingSimulationRuntime1(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

        //executing simulation at runtime 2
        executingSimulationRuntime2(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

        //executing simulation to verify preemption and running of vms through running time of simulation

        executingSimulationRuntime3(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
        executingSimulationRuntime4(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
        executingSimulationRuntime5(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
        executingSimulationRuntime6(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
        executingSimulationRuntime7(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);
        executingSimulationRuntime8(ACCEPTABLE_DIFFERENCE, numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

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

    private void populateVmLists(int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        int vmId = 0;
        int priority = 0;
        double runtime = 8;
        double subtime = 0;
        double cpuReq = 0.5;


        for (int i = 0; i < numberOfVms; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP0S0.add(vm);
        }

        // creating vms model P1S0, total of vms 6603
        // with cpu total requisition of 1980.9

        priority = 1;
        runtime = 5;
        cpuReq = 0.3;


        for (int i = 0; i < numberOfVms; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP1S0.add(vm);
        }

        // creating vms model P2S0, total of vms 6603
        // with cpu total requisition of 1320.6

        priority = 2;
        runtime = 2;
        cpuReq = 0.2;


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


        for (int i = 0; i < numberOfVms; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP0S1.add(vm);
        }
    }

    private void executingSimulationRuntime8(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        // passing time to 8
        Mockito.when(timeUtil.clock()).thenReturn(8.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        // finishing vms of priority 0, submit time 0, runtime 8 that are finished in simulation time 7

        processEvent(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

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

    private void processEvent(int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        for (int i = 0; i < numberOfVms; i++) {

            Mockito.when(event.getData()).thenReturn(vmP0S0.get(i));
            datacenter.processEvent(event);

            Mockito.when(event.getData()).thenReturn(vmP1S0.get(i));
            datacenter.processEvent(event);

            Mockito.when(event.getData()).thenReturn(vmP2S0.get(i));
            datacenter.processEvent(event);

            Mockito.when(event.getData()).thenReturn(vmP0S1.get(i));
            datacenter.processEvent(event);
        }
    }

    private void executingSimulationRuntime7(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        // passing time to 7
        Mockito.when(timeUtil.clock()).thenReturn(7.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        // finishing vms of priority 1, submit time 0, runtime 5

        processEvent(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

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

    private void executingSimulationRuntime6(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        // passing time to 6
        Mockito.when(timeUtil.clock()).thenReturn(6.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        // finishing the last one VM with priority 2

        processEvent(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

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

    private void executingSimulationRuntime5(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        // passing time to 5
        Mockito.when(timeUtil.clock()).thenReturn(5.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);


        // finishing vms of priority 2, submit time 0, runtime 2 and
        // finishing vms of priority 0, submit time 1, runtime 2
        // both are finished at time 5

        processEvent(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

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

    private void executingSimulationRuntime4(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        // passing time to 4
        Mockito.when(timeUtil.clock()).thenReturn(4.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        // finishing vms of priority 2, submit time 0, and runtime 2 that are finished at time 4

        processEvent(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

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

    private void executingSimulationRuntime3(double ACCEPTABLE_DIFFERENCE, int numberOfVms, List<Vm> vmP0S0, List<Vm> vmP1S0, List<Vm> vmP2S0, List<Vm> vmP0S1) {
        // passing time to 3
        Mockito.when(timeUtil.clock()).thenReturn(3.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);


        // finishing vms of priority 0, submit time 1, and runtime 2, because the runtime is completed at time 3
        processEvent(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);


        //allocate 6603 vms os priority 0, submit time 1, and Cpu requisition of 0.6
        //with total requested Cpu equals 3961.8
        for (int i = 0; i < numberOfVms; i++) {
            Mockito.when(event.getData()).thenReturn(vmP0S1.get(i));
            datacenter.processEvent(event);
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
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);

        //allocate 6603 vms os priority 0, submit time 0, and Cpu requisition of 0.5
        //with total requested Cpu equals 3301.5
        for (int i = 0; i < numberOfVms; i++) {
            Mockito.when(event.getData()).thenReturn(vmP0S0.get(i));
            datacenter.processEvent(event);
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
            Mockito.when(event.getData()).thenReturn(vmP1S0.get(i));
            datacenter.processEvent(event);
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
            Mockito.when(event.getData()).thenReturn(vmP2S0.get(i));
            datacenter.processEvent(event);
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

    @Test
    public void testStoreHostUtilizationEvent1(){
        HostUsageDataStore hostUsage = Mockito.mock(HostUsageDataStore.class);
        datacenter.setHostUsageDataStore(hostUsage);
        Mockito.when(event.getTag()).thenReturn(PreemptiveDatacenter.STORE_HOST_UTILIZATION_EVENT);

        datacenter.processEvent(event);

        UsageInfo info = new UsageInfo(host.getId(), 0, 0, 0, 0, 0, 0, 0, 0);
        List<UsageEntry> list = new ArrayList<>();
        list.addAll(info.getUsageEntries());

        Mockito.verify(hostUsage, times(0)).addUsageEntries(list);
        Mockito.verify(hostUsage, times(1)).addUsageEntries(new ArrayList<UsageEntry>());

    }

    @Test
    public void testStoreHostUtilizationEvent2(){
        HostUsageDataStore hostUsage = Mockito.mock(HostUsageDataStore.class);
        datacenter.setHostUsageDataStore(hostUsage);
        Mockito.when(event.getTag()).thenReturn(PreemptiveDatacenter.STORE_HOST_UTILIZATION_EVENT);

        UsageInfo info = new UsageInfo(host.getId(), 0, 0, 0, 0, 0, 0, 0, 0);
        List<UsageEntry> list = new ArrayList<>();
        list.addAll(info.getUsageEntries());

        host.getUsageMap().put(1.0, info);

        datacenter.processEvent(event);

        Mockito.verify(hostUsage, times(1)).addUsageEntries(list);
        Mockito.verify(hostUsage, times(0)).addUsageEntries(new ArrayList<UsageEntry>());

    }

    @Test
    public void testStoreHostUtilizationEvent3(){
        List<Host> hostList = new ArrayList<Host>();
        List<Pe> peList1 = new ArrayList<Pe>();
        peList1.add(new Pe(0, new PeProvisionerSimple(10)));

        PreemptiveHost host2 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(
                peList1), 3);

        hostList.add(host);
        hostList.add(host2);

        Mockito.when(datacenter.getHostList()).thenReturn(hostList);

        HostUsageDataStore hostUsage = Mockito.mock(HostUsageDataStore.class);
        datacenter.setHostUsageDataStore(hostUsage);

        Mockito.when(event.getTag()).thenReturn(PreemptiveDatacenter.STORE_HOST_UTILIZATION_EVENT);

        UsageInfo info = new UsageInfo(host.getId(), 0, 0, 0, 0, 0, 0, 0, 0);
        UsageInfo info2 = new UsageInfo(host2.getId(), 0, 0, 0, 0, 0, 0, 0, 0);

        List<UsageEntry> list = new ArrayList<>();
        list.addAll(info.getUsageEntries());
        list.addAll(info2.getUsageEntries());

        host.getUsageMap().put(1.0, info);
        host2.getUsageMap().put(1.0, info2);

        datacenter.processEvent(event);

        Mockito.verify(hostUsage, times(1)).addUsageEntries(list);
        Mockito.verify(hostUsage, times(0)).addUsageEntries(new ArrayList<UsageEntry>());

    }

    @Test
    public void testCollectAndStoreDatacenterInfo(){
        // setting next event to collect datacenter info
        Mockito.when(event.getTag()).thenReturn(PreemptiveDatacenter.COLLECT_DATACENTER_INFO_EVENT);

        // testing if list of datacenterInfo is empty now
        Assert.assertEquals(datacenter.getDatacenterInfo().size(), 0);

        // executing process
        datacenter.processEvent(event);

        // testing if list of datacenterInfo has one element now
        Assert.assertEquals(datacenter.getDatacenterInfo().size(), 1);

        // setting next event to store datacenter info
        Mockito.when(event.getTag()).thenReturn(PreemptiveDatacenter.STORE_DATACENTER_INFO_EVENT);

        // executing process
        datacenter.processEvent(event);

        // testing if list of datacenterInfo is empty again
        Assert.assertEquals(datacenter.getDatacenterInfo().size(), 0);

    }

    @Test
    public void testEndOfSimulation(){
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.END_OF_SIMULATION);

        int priority = 0;
        double runtime = 2;
        double subtime = 1;
        double cpuReq = 0.6;
        int vmId = 0;

        PreemptableVm vm1 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 1, runtime);
        PreemptableVm vm4 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 1, runtime);
        PreemptableVm vm5 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 2, runtime);
        PreemptableVm vm6 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 2, runtime);
        vm1.setHost(host);
        vm2.setHost(host);
        vm3.setHost(host);
        vm4.setHost(host);
        vm5.setHost(host);
        vm6.setHost(host);


        datacenter.getVmsRunning().add(vm1);
        datacenter.getVmsRunning().add(vm3);
        datacenter.getVmsRunning().add(vm5);

        datacenter.getVmsForScheduling().add(vm2);
        datacenter.getVmsForScheduling().add(vm4);
        datacenter.getVmsForScheduling().add(vm6);


        Assert.assertEquals(datacenter.getVmsRunning().size(), 3);
        Assert.assertEquals(datacenter.getVmsForScheduling().size(), 3);

        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());

    }

    @Test
    public void testInitFromCheckpoint(){
        int priority = 0;
        double runtime = 2;
        double subtime = 1;
        double cpuReq = 0.6;
        int vmId = 0;

        PreemptableVm vm1 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 1, runtime);
        PreemptableVm vm4 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 1, runtime);
        PreemptableVm vm5 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 2, runtime);
        PreemptableVm vm6 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 2, runtime);

        vm1.setLastHostId(host.getId());
        vm3.setLastHostId(host.getId());
        vm5.setLastHostId(host.getId());

        List<PreemptableVm> runningVms = new ArrayList<>();
        List<PreemptableVm> waitingVms = new ArrayList<>();

        runningVms.add(vm1);
        runningVms.add(vm3);
        runningVms.add(vm5);

        waitingVms.add(vm2);
        waitingVms.add(vm4);
        waitingVms.add(vm6);

        PreemptableVmDataStore vmDataStore = Mockito.mock(PreemptableVmDataStore.class);
        Mockito.when(vmDataStore.getAllRunningVms()).thenReturn(runningVms);
        Mockito.when(vmDataStore.getAllWaitingVms()).thenReturn(waitingVms);

        datacenter.initializeFromCheckpoint(vmDataStore);

        Assert.assertArrayEquals(datacenter.getVmsRunning().toArray(), runningVms.toArray());
        Assert.assertArrayEquals(datacenter.getVmsForScheduling().toArray(), waitingVms.toArray());

        List<PreemptableVm> runningVmsArray = new ArrayList<>(datacenter.getVmsRunning());
        List<PreemptableVm> waitingVmsArray = new ArrayList<>(datacenter.getVmsForScheduling());

        for (int i = 0; i < runningVms.size(); i++){
            Assert.assertEquals(host.hashCode(), runningVmsArray.get(i).getHost().hashCode());
            Assert.assertNull(waitingVmsArray.get(i).getHost());
        }

    }

    @Test
    public void testInitFromCheckpoint2() {

        List<Pe> peList1 = new ArrayList<Pe>();
        peList1.add(new Pe(0, new PeProvisionerSimple(10)));

        PreemptiveHost host2 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(peList1), 3);

        datacenter.getHostList().add(host2);


        int priority = 0;
        double runtime = 2;
        double subtime = 1;
        double cpuReq = 0.6;
        int vmId = 0;

        PreemptableVm vm1 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm4 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm5 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm6 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);

        vm1.setLastHostId(host.getId());
        vm3.setLastHostId(host.getId());
        vm5.setLastHostId(host.getId());

        PreemptableVm vm7 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm8 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm9 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm10 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm11 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm12 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);

        vm7.setLastHostId(host2.getId());
        vm9.setLastHostId(host2.getId());
        vm11.setLastHostId(host2.getId());

        List<PreemptableVm> runningVms = new ArrayList<>();
        List<PreemptableVm> waitingVms = new ArrayList<>();

        runningVms.add(vm1);
        runningVms.add(vm3);
        runningVms.add(vm5);
        runningVms.add(vm7);
        runningVms.add(vm9);
        runningVms.add(vm11);

        waitingVms.add(vm2);
        waitingVms.add(vm4);
        waitingVms.add(vm6);
        waitingVms.add(vm8);
        waitingVms.add(vm10);
        waitingVms.add(vm12);

        PreemptableVmDataStore vmDataStore = Mockito.mock(PreemptableVmDataStore.class);
        Mockito.when(vmDataStore.getAllRunningVms()).thenReturn(runningVms);
        Mockito.when(vmDataStore.getAllWaitingVms()).thenReturn(waitingVms);

        datacenter.initializeFromCheckpoint(vmDataStore);

        Assert.assertArrayEquals(datacenter.getVmsRunning().toArray(), runningVms.toArray());
        Assert.assertArrayEquals(datacenter.getVmsForScheduling().toArray(), waitingVms.toArray());

        List<PreemptableVm> runningVmsList = new ArrayList<>(datacenter.getVmsRunning());
        List<PreemptableVm> waitingVmsList = new ArrayList<>(datacenter.getVmsForScheduling());

        for (int i = 0; i < runningVms.size(); i++){

            if (runningVmsList.get(i).getId() < 6) {
                Assert.assertEquals(host.hashCode(), runningVmsList.get(i).getHost().hashCode());

            } else {
                Assert.assertEquals(host2.hashCode(), runningVmsList.get(i).getHost().hashCode());
            }

            Assert.assertNull(waitingVmsList.get(i).getHost());
        }



    }

    @Test
    public void testHostUpdateUsageInInitFromCheckpoint(){
        PreemptiveHost host2 = Mockito.mock(PreemptiveHost.class);
        Mockito.when(host2.getId()).thenReturn(2);

        datacenter.getHostList().add(host2);

        int priority = 0;
        double runtime = 2;
        double subtime = 1;
        double cpuReq = 0.6;
        int vmId = 0;

        PreemptableVm vm1 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 1, runtime);
        PreemptableVm vm4 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 1, runtime);
        PreemptableVm vm5 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 2, runtime);
        PreemptableVm vm6 = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority + 2, runtime);

        vm1.setLastHostId(host2.getId());
        vm3.setLastHostId(host2.getId());
        vm5.setLastHostId(host2.getId());

        List<PreemptableVm> runningVms = new ArrayList<>();
        List<PreemptableVm> waitingVms = new ArrayList<>();

        runningVms.add(vm1);
        runningVms.add(vm3);
        runningVms.add(vm5);

        waitingVms.add(vm2);
        waitingVms.add(vm4);
        waitingVms.add(vm6);

        PreemptableVmDataStore vmDataStore = Mockito.mock(PreemptableVmDataStore.class);
        Mockito.when(vmDataStore.getAllRunningVms()).thenReturn(runningVms);
        Mockito.when(vmDataStore.getAllWaitingVms()).thenReturn(waitingVms);
        Mockito.when(host2.vmCreate(vm1)).thenReturn(true);
        Mockito.when(host2.vmCreate(vm3)).thenReturn(true);
        Mockito.when(host2.vmCreate(vm5)).thenReturn(true);


        datacenter.initializeFromCheckpoint(vmDataStore);

        Mockito.verify(host2, times(3)).updateUsage(0d);
    }
    
	@Test
	public void testGetMigrationsOfVm() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(5)));
		
		PreemptiveHost host2 = new PreemptiveHost(2, peList1,
				new VmSchedulerMipsBased(peList1), 3);
		
		datacenter.getHostList().add(host2);

		int priority = 0;
		double runtime = 2;
		double subtime = 1;
		int vmId = 0;

		PreemptableVm vm1 = new PreemptableVm(vmId++, 0, 5, 0, subtime,
				priority + 2, runtime);

		// allocating first vm
        datacenter.allocateHostForVm(false, vm1, host, false);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);

        PreemptableVm vm2 = new PreemptableVm(vmId++, 0, 6, 0, subtime,
				priority + 1, runtime);

        // allocating 
        datacenter.allocateHostForVm(false, vm2, host, false);
        
        // checking
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm2, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);

		// reallocating 
        datacenter.allocateHostForVm(false, vm1, host2, false);
        
        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm2, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 1);
        
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);		
	}
}
