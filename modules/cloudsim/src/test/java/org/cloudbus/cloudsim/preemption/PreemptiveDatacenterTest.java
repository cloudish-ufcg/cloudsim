package org.cloudbus.cloudsim.preemption;

import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.WorstFitPriorityBasedVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gnu.trove.map.hash.THashMap;

public class PreemptiveDatacenterTest {

    private static final double ACCEPTABLE_DIFFERENCE = 0.000001;
    private static final int PROD = 0;
    private static final int BATCH = 1;
    private static final int FREE = 2;

    private PreemptiveDatacenter datacenter;
    private SimEvent event;
    private PreemptiveHost host;
    private SimulationTimeUtil timeUtil;
    private HostSelectionPolicy hostSelector;
    private WorstFitPriorityBasedVmAllocationPolicy preemptableVmAllocationPolicy;
    private String datacenterFile;
    private String datacenterUrl;
    private Properties properties;
    private Map<Integer, Double> admittedRequests;

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

        mockProperties();

        host = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
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

        preemptableVmAllocationPolicy = new WorstFitPriorityBasedVmAllocationPolicy(googleHostList);

        datacenter = new PreemptiveDatacenter("datacenter",
                characteristics, preemptableVmAllocationPolicy,
                new LinkedList<Storage>(), 0, properties);

        datacenter.setSimulationTimeUtil(timeUtil);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

        admittedRequests = new HashMap<Integer, Double>();
        admittedRequests.put(PROD, 0d);
        admittedRequests.put(BATCH, 0d);
        admittedRequests.put(FREE, 0d);

        validateAdmitedRequests();
    }

    private void mockProperties() {
        datacenterFile = "outputUtilizationTest.sqlite3";
        datacenterUrl = "jdbc:sqlite:" + datacenterFile;
        properties = Mockito.mock(Properties.class);
        Mockito.when(properties.getProperty(HostUsageDataStore.DATABASE_URL_PROP)).thenReturn(datacenterUrl);
        Mockito.when(properties.getProperty(DatacenterUsageDataStore.DATABASE_URL_PROP)).thenReturn(datacenterUrl);
        Mockito.when(properties.getProperty("make_checkpoint")).thenReturn("yes");
        Mockito.when(properties.getProperty("checkpoint_interval_size")).thenReturn("3");
        Mockito.when(properties.getProperty("collect_datacenter_summary_info")).thenReturn("yes");
        Mockito.when(properties.getProperty("datacenter_storing_interval_size")).thenReturn("5");
        Mockito.when(properties.getProperty("datacenter_collect_info_interval_size")).thenReturn("5");
        Mockito.when(properties.getProperty("number_of_priorities")).thenReturn("3");
    }

    private void validateAdmitedRequests() {
        for (Integer priority : admittedRequests.keySet()) {
            Assert.assertEquals(admittedRequests.get(priority),
                                datacenter.getAdmittedRequests().get(priority),
                                ACCEPTABLE_DIFFERENCE);
        }
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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), vm0.getMips() + admittedRequests.get(vm0.getPriority()));
        validateAdmitedRequests();
    }

    private void setGreedyAdmController() {
        Map<Integer, Double> sloTargets = new THashMap<>();
        sloTargets.put(PROD, 1.0);
        sloTargets.put(BATCH, 0.9);
        sloTargets.put(FREE, 0.5);

        datacenter.setAdmController(new GreedyQuotaAdmissionController(10, sloTargets, 1));
    }

    @Test
    public void testAllocateVmWithRejection(){

        setGreedyAdmController();

        int priority = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(1, 1, 5, 1, 0, priority, runtime);
        PreemptableVm vm1 = new PreemptableVm(2, 1, 11, 1, 0, priority, runtime);

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1);

        datacenter.processEvent(event);

        validateAdmitedRequests();

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), vm0.getMips() + admittedRequests.get(vm0.getPriority()));
        validateAdmitedRequests();
    }

    @Test
    public void testAllocateVmWithRejection2(){

        setGreedyAdmController();
        int priority = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(1, 1, 5, 1, 0, priority, runtime);
        PreemptableVm vm1 = new PreemptableVm(2, 1, 4.99999999, 1, 0, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(3, 1, 0.00000001, 1, 0, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(4, 1, 5.00000001, 1, 0, priority, runtime);
        PreemptableVm vm4 = new PreemptableVm(3, 1, 0.00000002, 1, 0, priority, runtime);

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0);

        // allocate vm0 to datacenter has available mips equals 10.0
        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        admittedRequests.put(vm0.getPriority(), vm0.getMips() + admittedRequests.get(vm0.getPriority()));
        validateAdmitedRequests();


        // try to allocate vm3 that request 5.00000001 mips

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm3);

        //try allocate vm3 to datacenter has available mips equals 5.0
        datacenter.processEvent(event);

        //status of the datacenter does not change
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());

        validateAdmitedRequests();

        // try to allocate vm1 that request 4.99999999 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1);

        // allocate vm1 to datacenter has available mips equals 5.0
        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        admittedRequests.put(vm1.getPriority(), vm1.getMips() + admittedRequests.get(vm1.getPriority()));
        validateAdmitedRequests();

        // try to allocate vm4 that request 0.00000002 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm4);

        // allocate vm1 to datacenter has available mips equals 0.00000001
        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());

        validateAdmitedRequests();

        // try to allocate vm2 that request 0.00000001 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm2);

        // allocate vm1 to datacenter has available mips equals 0.00000001
        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(3, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0, datacenter.getVmsRunning().first());
        Assert.assertTrue(datacenter.getVmsRunning().contains(vm1));
        Assert.assertEquals(vm2, datacenter.getVmsRunning().last());

        admittedRequests.put(vm2.getPriority(), vm2.getMips() + admittedRequests.get(vm2.getPriority()));
        validateAdmitedRequests();
    }

    @Test
    public void testAllocateVmWithRejection3(){

        setGreedyAdmController();
        double runtime = 10;

        PreemptableVm vm0P0 = new PreemptableVm(1, 1, 5, 1, 0, PROD, runtime);

        PreemptableVm vm1P1 = new PreemptableVm(2, 1, 4.99999999, 1, 0, BATCH, runtime);
        PreemptableVm vm2P1 = new PreemptableVm(3, 1, 0.55555557, 1, 0, BATCH, runtime);
        PreemptableVm vm3P1 = new PreemptableVm(4, 1, 0.55555556, 1, 0, BATCH, runtime);

        PreemptableVm vm4P2 = new PreemptableVm(5, 1, 8.88888888, 1, 0, FREE, runtime);
        PreemptableVm vm5P2 = new PreemptableVm(6, 1, 0.00000002, 1, 0, FREE, runtime);
        PreemptableVm vm6P2 = new PreemptableVm(7, 1, 0.00000001, 1, 0, FREE, runtime);

        PreemptableVm vm7P0 = new PreemptableVm(8, 1, 5, 1, 0, PROD, runtime);

        // set create of vm0P0 that requests 5.0 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0P0);

        // allocate vm0P0 to datacenter has available mips equals 10.0
        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());

        admittedRequests.put(vm0P0.getPriority(), vm0P0.getMips() + admittedRequests.get(vm0P0.getPriority()));
        validateAdmitedRequests();

        // set create of vm1P1 that requests 4.99999999 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1P1);

        // allocate vm1P1 to datacenter has available mips equals 5.0
        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        admittedRequests.put(vm1P1.getPriority(), vm1P1.getMips() + admittedRequests.get(vm1P1.getPriority()));
        validateAdmitedRequests();

        // set create of vm2P1 that requests 1.11111113 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm2P1);

        // allocate vm2P1 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        // vm2P1 was rejected and status did not change
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        validateAdmitedRequests();

        // set create of vm3P1 that requests 1.11111112 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm3P1);

        // allocate vm3P1 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm3P1, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        admittedRequests.put(vm3P1.getPriority(), vm3P1.getMips() + admittedRequests.get(vm3P1.getPriority()));
        validateAdmitedRequests();

        // set create of vm4P2 that requests 8.88888888 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm4P2);

        // allocate vm4P2 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm3P1, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        validateAdmitedRequests();

        // set create of vm5P2 that requests 0.00000002 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm5P2);

        // allocate vm5P2 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        // vm4P2 was rejected and status did not change
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm3P1, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        validateAdmitedRequests();

        // set create of vm6P2 that requests 0.00000001 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm6P2);

        // allocate vm6P2 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        // vm6P2 was rejected and status did not change
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm3P1, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        validateAdmitedRequests();

        // set create of vm7P0 that requests 5 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm7P0);

        // allocate vm7P0 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        // vm7P0 was rejected and status did not change
        Assert.assertEquals(2, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm1P1, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(vm3P1, datacenter.getVmsForScheduling().last());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm7P0, datacenter.getVmsRunning().last());

        admittedRequests.put(vm7P0.getPriority(), vm7P0.getMips() + admittedRequests.get(vm7P0.getPriority()));
        validateAdmitedRequests();
    }

    @Test
    public void testAllocateVmWithRejection4(){

        setGreedyAdmController();
        double runtime = 10;

        PreemptableVm vm0P0 = new PreemptableVm(1, 1, 5, 1, 0, PROD, runtime);

        PreemptableVm vm1P1 = new PreemptableVm(2, 1, 4.99999999, 1, 0, BATCH, runtime);
        PreemptableVm vm2P1 = new PreemptableVm(3, 1, 0.55555557, 1, 0, BATCH, runtime);

        PreemptableVm vm4P2 = new PreemptableVm(5, 1, 8.88888888, 1, 0, FREE, runtime);
        PreemptableVm vm5P2 = new PreemptableVm(6, 1, 0.00000002, 1, 0, FREE, runtime);
        PreemptableVm vm6P2 = new PreemptableVm(7, 1, 0.00000001, 1, 0, FREE, runtime);

        PreemptableVm vm3P1 = new PreemptableVm(4, 1, 0.55555556, 1, 0, BATCH, runtime);
        PreemptableVm vm7P0 = new PreemptableVm(8, 1, 5, 1, 0, PROD, runtime);

        // set create of vm0P0 that requests 5.0 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0P0);

        // allocate vm0P0 to datacenter has available mips equals 10.0
        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());

        admittedRequests.put(vm0P0.getPriority(), vm0P0.getMips() + admittedRequests.get(vm0P0.getPriority()));
        validateAdmitedRequests();

        // set create of vm1P1 that requests 4.99999999 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1P1);

        // allocate vm1P1 to datacenter has available mips equals 5.0
        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        admittedRequests.put(vm1P1.getPriority(), vm1P1.getMips() + admittedRequests.get(vm1P1.getPriority()));
        validateAdmitedRequests();

        // set create of vm2P1 that requests 1.11111113 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm2P1);

        // allocate vm2P1 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        // vm2P1 was rejected and status did not change
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        validateAdmitedRequests();

        // set create of vm4P2 that requests 8.88888888 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm4P2);

        // allocate vm4P2 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        validateAdmitedRequests();

        // set create of vm5P2 that requests 0.00000002 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm5P2);

        // allocate vm5P2 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        // vm4P2 was rejected and status did not change
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm5P2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        admittedRequests.put(vm5P2.getPriority(), vm5P2.getMips() + admittedRequests.get(vm5P2.getPriority()));
        validateAdmitedRequests();

        // set create of vm6P2 that requests 0.00000001 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm6P2);

        // allocate vm6P2 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        // vm6P2 was rejected and status did not change
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm5P2, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        validateAdmitedRequests();

        // set create of vm3P1 that requests 1.11111112 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm3P1);

        // allocate vm3P1 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        Assert.assertEquals(2, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm3P1, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(vm5P2, datacenter.getVmsForScheduling().last());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        admittedRequests.put(vm3P1.getPriority(), vm3P1.getMips() + admittedRequests.get(vm3P1.getPriority()));
        validateAdmitedRequests();


        // set create of vm7P0 that requests 5 mips
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm7P0);

        // allocate vm7P0 to datacenter has available mips equals 0.000000001
        datacenter.processEvent(event);

        // vm7P0 was rejected and status did not change
        Assert.assertEquals(3, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(vm1P1, datacenter.getVmsForScheduling().first());
        Assert.assertTrue(datacenter.getVmsForScheduling().contains(vm3P1));
        Assert.assertEquals(vm5P2, datacenter.getVmsForScheduling().last());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm7P0, datacenter.getVmsRunning().last());

        admittedRequests.put(vm7P0.getPriority(), vm7P0.getMips() + admittedRequests.get(vm7P0.getPriority()));
        validateAdmitedRequests();
    }

    public void printQuotas() {
        for(int i = 0; i < 3; i++) {
            System.out.println(((GreedyQuotaAdmissionController)
                    datacenter.getAdmController()).getPriorityToQuotas().get(i));
        }
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
        // checking admited request values
        admittedRequests.put(vm0.getPriority(), vm0.getMips() + admittedRequests.get(vm0.getPriority()));
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm1.getPriority(), vm1.getMips() + admittedRequests.get(vm1.getPriority()));
        validateAdmitedRequests();
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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), vm0.getMips() + admittedRequests.get(vm0.getPriority()));
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm1.getPriority(), vm1.getMips() + admittedRequests.get(vm1.getPriority()));
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm2.getPriority(), vm2.getMips() + admittedRequests.get(vm2.getPriority()));
        validateAdmitedRequests();
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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), vm0.getMips() + admittedRequests.get(vm0.getPriority()));
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm0.getPriority()) - vm0.getMips());
        validateAdmitedRequests();
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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm0.getPriority()) + vm0.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm1.getPriority()) + vm1.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm0.getPriority()) - vm0.getMips());
        validateAdmitedRequests();
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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm0.getPriority()) + vm0.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm1.getPriority(), admittedRequests.get(vm1.getPriority()) + vm1.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm2.getPriority(), admittedRequests.get(vm2.getPriority()) + vm2.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm0.getPriority()) - vm0.getMips());
        validateAdmitedRequests();
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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm0.getPriority()) + vm0.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm1.getPriority(), admittedRequests.get(vm1.getPriority()) + vm1.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm2.getPriority(), admittedRequests.get(vm2.getPriority()) + vm2.getMips());
        validateAdmitedRequests();


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

        // checking admited request values
        admittedRequests.put(vm3.getPriority(), admittedRequests.get(vm3.getPriority()) + vm3.getMips());
        validateAdmitedRequests();


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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm0.getPriority()) - vm0.getMips());
        validateAdmitedRequests();
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

        // checking admited request values
        admittedRequests.put(vm3.getPriority(), admittedRequests.get(vm3.getPriority()) + vm3.getMips());
        validateAdmitedRequests();


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

        // checking admited request values
        admittedRequests.put(vm2.getPriority(), admittedRequests.get(vm2.getPriority()) + vm2.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm1.getPriority(), admittedRequests.get(vm1.getPriority()) + vm1.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm0.getPriority()) + vm0.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm0.getPriority(), admittedRequests.get(vm0.getPriority()) - vm0.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm1.getPriority(), admittedRequests.get(vm1.getPriority()) - vm1.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm2.getPriority(), admittedRequests.get(vm2.getPriority()) - vm2.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm3.getPriority(), admittedRequests.get(vm3.getPriority()) - vm3.getMips());
        validateAdmitedRequests();
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

        // checking admited request values
        admittedRequests.put(vm0P0.getPriority(), admittedRequests.get(vm0P0.getPriority()) + vm0P0.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm1P1.getPriority(), admittedRequests.get(vm1P1.getPriority()) + vm1P1.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm2P2.getPriority(), admittedRequests.get(vm2P2.getPriority()) + vm2P2.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm3P0.getPriority(), admittedRequests.get(vm3P0.getPriority()) + vm3P0.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm0P0.getPriority(), admittedRequests.get(vm0P0.getPriority()) - vm0P0.getMips());
        validateAdmitedRequests();
    }

    @Test
    public void testVmDestroyWithVmForSchedulingDifferentPriorities2() {
        Log.disable();
        double runtime = 10;
        int vmId = 0;
        int userId = 1;

        List<PreemptableVm> vmsP2 = new ArrayList<>();

        // creating vms priorities 2
        for (int i = 0; i < 10; i++) {

            PreemptableVm vmP2 = new PreemptableVm(vmId++, userId, 1.0, 1.0, 0, 2, runtime);

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

            // checking admited request values
            admittedRequests.put(vmP2.getPriority(), admittedRequests.get(vmP2.getPriority()) + vmP2.getMips());
            validateAdmitedRequests();

            vmsP2.add(vmP2);
        }

        for (PreemptableVm vm : vmsP2) {
            Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
        }

        List<PreemptableVm> vmsP1 = new ArrayList<PreemptableVm>();

        // creating vms priorities 1
        for (int i = 0; i < 10; i++) {
            PreemptableVm vmP1 = new PreemptableVm(vmId++, userId, 1.0, 1.0, 0, 1,
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

            // checking admited request values
            admittedRequests.put(vmP1.getPriority(), admittedRequests.get(vmP1.getPriority()) + vmP1.getMips());
            validateAdmitedRequests();

            vmsP1.add(vmP1);

            for (PreemptableVm vm : datacenter.getVmsForScheduling()) {
                Assert.assertEquals(vm.getNumberOfPreemptions(), 1);
                Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
            }
        }

        for (PreemptableVm vm : vmsP1) {
            Assert.assertEquals(vm.getNumberOfPreemptions(), 0);
            Assert.assertEquals(vm.getNumberOfBackfillingChoice(), 0);
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

        PreemptableVm vmP0 = new PreemptableVm(vmId++, userId, 5, 1.0, 0, 0, runtime - 0.1);

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

        // checking admited request values
        admittedRequests.put(vmP0.getPriority(), admittedRequests.get(vmP0.getPriority()) + vmP0.getMips());
        validateAdmitedRequests();

        // destroying vm priority 1
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vmsP1.get(0));
        //mocking return of the selection policy to expected vm
        Mockito.when(hostSelector.select(
                Mockito.any(SortedSet.class), Mockito.any(PreemptableVm.class))).thenReturn(null);
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(1), vmsP1.get(5))).thenReturn(host);
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

        // checking admited request values
            admittedRequests.put(vmsP1.get(0).getPriority(),
                                admittedRequests.get(vmsP1.get(0).getPriority()) - vmsP1.get(0).getMips());
        validateAdmitedRequests();

        // destroying vm priority 1
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vmsP1.get(1));
        //mocking return of the selection policy to expected vm
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(1), vmsP1.get(6))).thenReturn(host);
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

        // checking admited request values
        admittedRequests.put(vmsP1.get(1).getPriority(),
                admittedRequests.get(vmsP1.get(1).getPriority()) - vmsP1.get(1).getMips());
        validateAdmitedRequests();

        // destroying vm priority 0
        Mockito.when(timeUtil.clock()).thenReturn(runtime - 0.1);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);
        Mockito.when(event.getData()).thenReturn(vmP0);

        //mocking return of the selection policy to expected vms
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(1), vmsP1.get(7))).thenReturn(host);
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(1), vmsP1.get(8))).thenReturn(host);
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(1), vmsP1.get(9))).thenReturn(host);

        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(2), vmsP2.get(0))).thenReturn(host);
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(2), vmsP2.get(1))).thenReturn(host);

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

        // checking admited request values
        admittedRequests.put(vmP0.getPriority(),
                admittedRequests.get(vmP0.getPriority()) - vmP0.getMips());
        validateAdmitedRequests();
    }

    @Test
    public void testVmDestroyWithPreemptionAfter() {
        Log.disable();
        double runtime = 10;
        int vmId = 0;
        int userId = 1;
        double cpuReq = 5;

        PreemptableVm vm0P0 = new PreemptableVm(vmId++, userId, cpuReq, 1.0, 0, 0, runtime - 0.1);

        // allocating first vm
        Assert.assertEquals(10, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm0P0);
        datacenter.processEvent(event);


        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());

        Assert.assertEquals(5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5, host.getAvailableMipsByPriority(0),
                ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5, host.getAvailableMipsByPriority(1),
                ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5, host.getAvailableMipsByPriority(2),
                ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(vm0P0.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm0P0.getNumberOfBackfillingChoice(), 0);

        // checking admited request values
        admittedRequests.put(vm0P0.getPriority(),
                admittedRequests.get(vm0P0.getPriority()) + vm0P0.getMips());
        validateAdmitedRequests();

        cpuReq = 2;
        PreemptableVm vm1P1 = new PreemptableVm(vmId++, userId, cpuReq, 1.0, 0, 1, runtime);

        // allocating second vm
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(event.getData()).thenReturn(vm1P1);
        datacenter.processEvent(event);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm0P0, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1P1, datacenter.getVmsRunning().last());

        Assert.assertEquals(3, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
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

        // checking admited request values
        admittedRequests.put(vm1P1.getPriority(),
                admittedRequests.get(vm1P1.getPriority()) + vm1P1.getMips());
        validateAdmitedRequests();

        cpuReq = 3;
        PreemptableVm vm2P2 = new PreemptableVm(vmId++, userId, cpuReq, 1.0, 0, 2, runtime);

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

        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
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

        // checking admited request values
        admittedRequests.put(vm2P2.getPriority(),
                admittedRequests.get(vm2P2.getPriority()) + vm2P2.getMips());
        validateAdmitedRequests();

        // allocation one more VM with priority 0
        cpuReq = 6;
        PreemptableVm vm3P0 = new PreemptableVm(vmId++, userId, cpuReq, 1.0, 0, 0, runtime);

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

        // checking admited request values
        admittedRequests.put(vm3P0.getPriority(),
                admittedRequests.get(vm3P0.getPriority()) + vm3P0.getMips());
        validateAdmitedRequests();

        // destroying vm0P0
        Mockito.when(
                hostSelector.select(preemptableVmAllocationPolicy.getPriorityToSortedHost().get(0), vm3P0)).thenReturn(host);

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

        // checking admited request values
        admittedRequests.put(vm0P0.getPriority(),
                admittedRequests.get(vm0P0.getPriority()) - vm0P0.getMips());
        validateAdmitedRequests();

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

        // checking admited request values
        admittedRequests.put(vm1P1.getPriority(),
                admittedRequests.get(vm1P1.getPriority()) - vm1P1.getMips());
        validateAdmitedRequests();
    }

    @Test
    public void testBackfilling() {
        double runtime = 10;
        int vmId = 0;
        int userId = 1;

        // creating VMs with different priorities
        PreemptableVm vm0 = new PreemptableVm(vmId++, userId, 5, 1.0, 0, 0, runtime - 0.3); // 5 mips
        PreemptableVm vm1 = new PreemptableVm(vmId++, userId, 5, 1.0, 0, 0, runtime - 0.2); // 5 mips
        PreemptableVm vm2 = new PreemptableVm(vmId++, userId, 6, 1.0, 0, 0, runtime); // 6 mips
        PreemptableVm vm3 = new PreemptableVm(vmId++, userId, 4, 1.0, 0, 1, runtime); // 4 mips
        PreemptableVm vm4 = new PreemptableVm(vmId++, userId, 1, 1.0, 0, 2, runtime); // 1 mips

        // trying to allocate all Vms. At the first moment, just Vm0 and Vm1 will be allocated
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        Mockito.when(hostSelector.select(
                Mockito.any(SortedSet.class), Mockito.any(PreemptableVm.class))).thenReturn(null);
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(0), vm0)).thenReturn(host);
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(0), vm1)).thenReturn(host);

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
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(1), vm3)).thenReturn(host);
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(2), vm4)).thenReturn(host);
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
        Mockito.when(hostSelector.select(
                preemptableVmAllocationPolicy.getPriorityToSortedHost().get(0), vm2)).thenReturn(host);
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

        Mockito.when(timeUtil.clock()).thenReturn(2.0);

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
                peList1), new FCFSBasedPreemptionPolicy(properties));

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

        Mockito.when(timeUtil.clock()).thenReturn(2.0);

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

        PreemptiveHost host2 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

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
				new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));
        

		int priority = 0;
		double runtime = 2;
		double subtime = 1;
		int vmId = 0;

		PreemptableVm vm1 = new PreemptableVm(vmId++, 0, 5, 0, subtime,
				priority + 2, runtime);

		// allocating first vm
		datacenter.allocateHostForVm(false, vm1);

        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertEquals(host, vm1.getHost());

        PreemptableVm vm2 = new PreemptableVm(vmId++, 0, 6, 0, subtime,
				priority + 1, runtime);

        // allocating 
        datacenter.allocateHostForVm(false, vm2);
        
        // checking
        Assert.assertEquals(1, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(1, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm2, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsForScheduling().first());
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 0);
        Assert.assertNull(vm1.getHost());
        
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(host, vm2.getHost());

		// adding host2 on structure to be considered on selection        
        preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority).add(host2);
        preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority + 1).add(host2);
        preemptableVmAllocationPolicy.getPriorityToSortedHost().get(priority + 2).add(host2);
        
        datacenter.allocateHostForVm(false, vm1);
        
        // checking
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());
        Assert.assertEquals(vm2, datacenter.getVmsRunning().first());
        Assert.assertEquals(vm1, datacenter.getVmsRunning().last());
        Assert.assertEquals(vm1.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm1.getNumberOfPreemptions(), 1);
        Assert.assertEquals(vm1.getNumberOfMigrations(), 1);
        Assert.assertEquals(host2, vm1.getHost());
        
        Assert.assertEquals(vm2.getNumberOfBackfillingChoice(), 0);
        Assert.assertEquals(vm2.getNumberOfPreemptions(), 0);
        Assert.assertEquals(vm2.getNumberOfMigrations(), 0);
        Assert.assertEquals(host, vm2.getHost());
	}
}
