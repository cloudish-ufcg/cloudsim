package org.cloudbus.cloudsim.preemption;

import java.util.*;
import java.io.File;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.WorstFitPriorityBasedVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.*;
import org.mockito.Mockito;


/**
 * Created by Alessandro Lia Fook Santos and Joao Victor Mafra on 01/11/16.
 */
public class SystemTestFCFS {

    private static final double ACCEPTABLE_DIFFERENCE = 0.000001;
    private static final int PROD = 0;
    private static final int BATCH = 1;
    private static final int FREE = 2;
    private double hostCapacity;
    private Properties properties;

    private PreemptiveDatacenter datacenter;
    private SimEvent event;
    private PreemptiveHost host;
    private PreemptiveHost host1;
    private PreemptiveHost host2;
    private PreemptiveHost host3;
    private PreemptiveHost host4;
    private PreemptiveHost host5;
    private PreemptiveHost host6;
    private PreemptiveHost host7;
    private PreemptiveHost host8;
    private PreemptiveHost host9;
    private PreemptiveHost host10;
    private SimulationTimeUtil timeUtil;
    private PreemptableVmAllocationPolicy preemptableVmAllocationPolicy;
    private DatacenterCharacteristics characteristics;


    // trace with 10 prod vms, 20 batch vms and 30 free vms
    // ---------------------------------------------------------------------
    private int NUMBER_OF_VMS_P0;
    private int NUMBER_OF_VMS_P1;
    private int NUMBER_OF_VMS_P2;
    private List<Vm> vmsP0;
    private List<Vm> vmsP1;
    private List<Vm> vmsP2;
    // ---------------------------------------------------------------------

    private String datacenterOutputFile;
    private String datacenterInputFile;
    private String datacenterInputUrl;
    private String datacenterOutputUrl;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {

//        Log.disable();

        event = Mockito.mock(SimEvent.class);

        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);


        // trace with 10 prod vms, 20 batch vms and 30 free vms
        // ---------------------------------------------------------------------
        // inicializating the lists of vms
        NUMBER_OF_VMS_P0 = 10;
        NUMBER_OF_VMS_P1 = 20;
        NUMBER_OF_VMS_P2 = 30;
        vmsP0 = new ArrayList<>(NUMBER_OF_VMS_P0);
        vmsP1 = new ArrayList<>(NUMBER_OF_VMS_P1);
        vmsP2 = new ArrayList<>(NUMBER_OF_VMS_P2);
        // ---------------------------------------------------------------------

        // mocking properties to config simulation
        datacenterInputFile = "new_trace_test.sqlite3";
        datacenterInputUrl = "jdbc:sqlite:" + datacenterInputFile;

        datacenterOutputFile = "outputUtilizationTest.sqlite3";
        datacenterOutputUrl = "jdbc:sqlite:" + datacenterOutputFile;

        properties = new Properties();
        properties.setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
        // time in micro
        properties.setProperty("logging", "yes");
        properties.setProperty("input_trace_database_url", datacenterInputUrl);
        properties.setProperty("loading_interval_size", "300000000");
        properties.setProperty("storing_interval_size", "240000000");
        properties.setProperty("output_tasks_database_url", datacenterOutputUrl);
        properties.setProperty("utilization_database_url", datacenterOutputUrl);
        properties.setProperty("utilization_storing_interval_size", "480000000");
        properties.setProperty("datacenter_database_url", datacenterOutputUrl);
        properties.setProperty("collect_datacenter_summary_info", "yes");
        properties.setProperty("datacenter_storing_interval_size", "300000000");
        properties.setProperty("datacenter_collect_info_interval_size", "240000000");
        properties.setProperty("make_checkpoint", "no");
        properties.setProperty("checkpoint_interval_size", "300000000");
        properties.setProperty("checkpoint_dir", datacenterOutputUrl);
        properties.setProperty("preemption_policy_class", "org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy");
        properties.setProperty("end_of_simulation_time", "600000000");
        properties.setProperty("update_quota_interval_size", "600000000");
        properties.setProperty("number_of_priorities", "3");
        properties.setProperty("slo_availability_target_priority_0", "1");
        properties.setProperty("slo_availability_target_priority_1", "0.9");
        properties.setProperty("slo_availability_target_priority_2", "0.5");

        // creating host
        List<Pe> peList1 = new ArrayList<Pe>();
        hostCapacity = 10;
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));

        host = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));

        // creating list of hosts
        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        // mocking the characteristics for data center
        characteristics = Mockito.mock(DatacenterCharacteristics.class);
        Mockito.when(characteristics.getHostList()).thenReturn(hostList);

        Mockito.when(characteristics.getNumberOfPes()).thenReturn(1);

        timeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(timeUtil.clock()).thenReturn(0d);

        List<PreemptiveHost> googleHostList = new ArrayList<PreemptiveHost>();
        for (Host host : hostList) {
            googleHostList.add((PreemptiveHost) host);
        }
        preemptableVmAllocationPolicy = new WorstFitPriorityBasedVmAllocationPolicy(googleHostList);

        // creating data center
        datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                new LinkedList<Storage>(), 0, properties);

        datacenter.setSimulationTimeUtil(timeUtil);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());
    }

    @After
    public void tearDown() {
        new File(datacenterOutputFile).delete();
    }


    //testing the operation of the system for a single host
    //TODO processBackfiling is called more than once at same time because of method processEventForAllVms()

    private void advanceTime(double time) {

        while (CloudSim.clock() < time) {
            CloudSim.runClockTick();
        }

        if (CloudSim.clock() == time)
            CloudSim.runClockTick();
    }


    @Test
    public void testSystemSingleHostFCFSNewTrace() throws Exception{
        Log.enable();

        hostCapacity = 10;

        // creating host
        List<Pe> peList1 = new ArrayList<Pe>();
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));
        properties.setProperty("preemption_policy_class",
                "org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy");

        host = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));

        // creating list of hosts
        List<PreemptiveHost> hostList = new ArrayList<PreemptiveHost>();
        hostList.add(host);


        preemptableVmAllocationPolicy = new WorstFitPriorityBasedVmAllocationPolicy(hostList);

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        CloudSim.runStart();

        // creating data center
            datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                    new LinkedList<Storage>(), 0, properties);

        //asserting host on data center and host total capacity
        Assert.assertEquals(host, datacenter.getHostList().get(0));
        Assert.assertEquals(hostCapacity, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        // creating vms model P0S0, total of vms 6603
        // with cpu total requisition of 3301.5
        populateVmListsNewTrace();

        submitEventsNewTrace();
        CloudSim.runClockTick();


        advanceTime(0.0);
        //allocating vms with submit time 0
        testSimulationNewTraceRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        testSimulationNewTraceRuntime1();

        advanceTime(2.0);
        //allocating vms with submit time 2
        testSimulationNewTraceRuntime2();

        advanceTime(3.0);
        testSimulationNewTraceRuntime3();

        advanceTime(4.0);
        testSimulationNewTraceRuntime4();

        advanceTime(5.0);
        testSimulationNewTraceRuntime5();

        advanceTime(6.0);
        testSimulationNewTraceRuntime6();

        advanceTime(7.0);
        testSimulationNewTraceRuntime7();

        advanceTime(8.0);
        testSimulationNewTraceRuntime8();

        verifyAvailabilityOfSingleHostNewTrace();
    }

    private void verifyAvailabilityOfSingleHostNewTrace() {
        double finishedTime = 0;

        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() < 3 || pVm.getId() == 9) {
                finishedTime = 7.0;
            } else {
                finishedTime = 6.0;
            }

            Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
        }

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() >= 10 && pVm.getId() <= 19) {
                finishedTime = 4.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            } else if (pVm.getId() >= 20 && pVm.getId() <= 23) {
                finishedTime = 5.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            } else {
                finishedTime = 8.0;
                Assert.assertEquals(0.571428571, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            }

        }

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() >= 30 && pVm.getId() <= 39) {
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                finishedTime = 6.0;
                Assert.assertEquals(0.333333333, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 40 && pVm.getId() <= 49) {
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                finishedTime = 8.0;
                Assert.assertEquals(0.285714286, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else {
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                finishedTime = 8.0;
                Assert.assertEquals(0.333333333, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            }

        }


    }

    private void testSimulationNewTraceRuntime8() {
        Assert.assertEquals(hostCapacity, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(0, datacenter.getVmsRunning().size());
    }

    private void testSimulationNewTraceRuntime7() {
        Assert.assertEquals(3, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 3, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 3 - 4, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(26, datacenter.getVmsRunning().size());

        for (PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, vm.getNumberOfPreemptions());

            if (vm.getId() <= 2)
                Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (vm.getId() == 9)
                Assert.assertEquals(1.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (vm.getId() <= 29)
                Assert.assertEquals(4.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(6.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
    }

    private void testSimulationNewTraceRuntime6() {
        Assert.assertEquals(1, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 2, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 3, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 3 - 4, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(30, datacenter.getVmsRunning().size());

        for (PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, vm.getNumberOfPreemptions());

            if (vm.getId() <= 2)
                Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (vm.getId() == 9)
                Assert.assertEquals(1.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (vm.getId() <= 29)
                Assert.assertEquals(4.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(6.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
    }

    private void testSimulationNewTraceRuntime5() {
        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5 - 3, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5 - 3 - 2, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(20, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(26, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingOfSingleHostWithFCFSPolicyTime5();
    }

    private void testNumberOfPreemptionsAndBackfillingOfSingleHostWithFCFSPolicyTime5() {
        for (PreemptableVm vm : datacenter.getVmsRunning()) {

            if (vm.getId() >= 30 && vm.getId() <= 39) {
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }

            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

            if (vm.getId() == 9)
                Assert.assertEquals(1.00, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (vm.getId() >= 24 && vm.getId() <= 29)
                Assert.assertEquals(4.00, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }

        for (PreemptableVm vm : datacenter.getVmsForScheduling()) {

            Assert.assertEquals(0, vm.getNumberOfPreemptions());
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(-1.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }

    }

    private void testSimulationNewTraceRuntime4() {
        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5 - 5, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5 - 5, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(30, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(20, datacenter.getVmsRunning().size());

        for (PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, vm.getNumberOfPreemptions());

            if (vm.getId() == 9 || vm.getId() >= 20 && vm.getId() < 24)
                Assert.assertEquals(1.00, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (vm.getId() >= 24)
                Assert.assertEquals(4.00, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }

        for (PreemptableVm vm : datacenter.getVmsForScheduling()) {

            if (vm.getId() >= 30 && vm.getId() <= 39) {
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
                Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
                Assert.assertEquals(-1.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
        }

    }

    private void testSimulationNewTraceRuntime3() {
        testSimulationNewTraceRuntime2();
    }

    private void testSimulationNewTraceRuntime2() {
        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5 - 5, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5 - 5, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(36, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(24, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingOfSingleHostWithFCFSPolicyTime1();

    }

    private void testSimulationNewTraceRuntime1() {

        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5 - 5, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5 - 5, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(26, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(24, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingOfSingleHostWithFCFSPolicyTime1();
    }

    private void testNumberOfPreemptionsAndBackfillingOfSingleHostWithFCFSPolicyTime1() {
        for (PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, vm.getNumberOfPreemptions());

            if (vm.getId() == 9 || vm.getId() >= 20)
                Assert.assertEquals(1.00, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }

        for (PreemptableVm vm : datacenter.getVmsForScheduling()) {

            if (vm.getId() >= 30 && vm.getId() <= 39) {
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
                Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
                Assert.assertEquals(-1.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
        }
    }

    private void testSimulationNewTraceRuntime0() {
        Assert.assertEquals(0.5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 4.5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 4.5 - 3, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 4.5 - 3 - 2, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(29, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingOfSingleHostWithFCFSPolicyTime0();
    }

    private void testNumberOfPreemptionsAndBackfillingOfSingleHostWithFCFSPolicyTime0() {
        for (PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, vm.getNumberOfPreemptions());
            Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
    }


    private void populateVmListsNewTrace() {

        int vmId = 0;
        int priority = 0;
        double runtime = 7;
        double submitTime = 0;
        double cpuReq = 0.5;


        for (int i = 0; i < NUMBER_OF_VMS_P0; i++) {

            if (i == NUMBER_OF_VMS_P0 - 1) {
                submitTime = 1;
            }

            if (i >= 3) {
                runtime = 6;
            }

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, submitTime, priority, runtime);
            vmsP0.add(vm);
        }

        // creating vms model P1S0, total of vms 6603
        // with cpu total requisition of 1980.9

        priority = 1;
        runtime = 4;
        cpuReq = 0.3;
        submitTime = 0;


        for (int i = 0; i < NUMBER_OF_VMS_P1; i++) {

            if (i >= 10) {
                cpuReq = 0.5;
                submitTime = 1;
            }

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, submitTime, priority, runtime);
            vmsP1.add(vm);
        }

        // creating vms model P2S0, total of vms 6603
        // with cpu total requisition of 1320.6

        priority = 2;
        runtime = 2;
        cpuReq = 0.2;
        submitTime = 0;


        for (int i = 0; i < NUMBER_OF_VMS_P2; i++) {

            if (i >= 10 && i <= 19) {
                submitTime = 1;
            } else if (i >= 20 && i <= 29) {
                submitTime = 2;
            }

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, submitTime, priority, runtime);
            vmsP2.add(vm);
        }
    }

    private void submitEventsNewTrace() {

        // time = 0
        int NUMBER_OF_VMS_P0 = 10;
        int NUMBER_OF_VMS_P1 = 20;
        int NUMBER_OF_VMS_P2 = 30;

        for (int i = 0; i < NUMBER_OF_VMS_P0; i++) {

            PreemptableVm vm = (PreemptableVm) vmsP0.get(i);

            CloudSim.send(datacenter.getId(), datacenter.getId(), vm.getSubmitTime(), CloudSimTags.VM_CREATE, vmsP0.get(i));
        }

        for (int i = 0; i < NUMBER_OF_VMS_P1; i++) {

            PreemptableVm vm = (PreemptableVm) vmsP1.get(i);

            CloudSim.send(datacenter.getId(), datacenter.getId(), vm.getSubmitTime(), CloudSimTags.VM_CREATE, vmsP1.get(i));
        }

        for (int i = 0; i < NUMBER_OF_VMS_P2; i++) {

            PreemptableVm vm = (PreemptableVm) vmsP2.get(i);

            CloudSim.send(datacenter.getId(), datacenter.getId(), vm.getSubmitTime(), CloudSimTags.VM_CREATE, vmsP2.get(i));
        }
    }

    @Test
    public void testSystemThreeHostsFCFSNewTrace() throws Exception{
        Log.disable();

        hostCapacity = 3.3;

        // creating host
        List<Pe> peList1 = new ArrayList<Pe>();
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));
        properties.setProperty("preemption_policy_class",
                "org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy");

        host1 = new PreemptiveHost(0, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        host2 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        host3 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));

        // creating list of hosts
        List<PreemptiveHost> hostList = new ArrayList<PreemptiveHost>();
        List<Host> hostList2 = new ArrayList<Host>();
        hostList.add(host1);
        hostList.add(host2);
        hostList.add(host3);

        hostList2.add(host1);
        hostList2.add(host2);
        hostList2.add(host3);

        preemptableVmAllocationPolicy = new WorstFitPriorityBasedVmAllocationPolicy(hostList);

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        CloudSim.runStart();

        // creating data center
        Mockito.when(characteristics.getHostList()).thenReturn(hostList2);
            datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                    new LinkedList<Storage>(), 0, properties);

        //asserting host on data center and host total capacity
        Assert.assertEquals(host1, datacenter.getHostList().get(0));
        Assert.assertEquals(host2, datacenter.getHostList().get(1));
        Assert.assertEquals(host3, datacenter.getHostList().get(2));

        Assert.assertEquals(hostCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        // creating vms model P0S0, total of vms 6603
        // with cpu total requisition of 3301.5
        populateVmListsNewTrace();

        submitEventsNewTrace();
        CloudSim.runClockTick();


        advanceTime(0.0);
        //allocating vms with submit time 0
        testSimulationNewTraceThreeHostsRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        testSimulationNewTraceThreeHostsRuntime1();

        advanceTime(2.0);
        //allocating vms with submit time 2
        testSimulationNewTraceThreeHostsRuntime2();

        advanceTime(3.0);
        testSimulationNewTraceThreeHostsRuntime3();

        advanceTime(4.0);
        testSimulationNewTraceThreeHostsRuntime4();

        advanceTime(5.0);
        testSimulationNewTraceThreeHostsRuntime5();

        advanceTime(6.0);
        testSimulationNewTraceThreeHostsRuntime6();

        advanceTime(7.0);
        testSimulationNewTraceThreeHostsRuntime7();

        advanceTime(8.0);
        testSimulationNewTraceThreeHostsRuntime8();

        advanceTime(9.0);
        testSimulationNewTraceThreeHostsRuntime9();

        verifyAvailabilityOfThreeHostsNewTrace();
    }

    private void verifyAvailabilityOfThreeHostsNewTrace() {
        double finishTime;

        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 3 && pVm.getId() <= 8) {
                finishTime = 6.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);
            } else {
                finishTime = 7.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);
            }

        }

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            finishTime = 4.0;

            if (pVm.getId() >= 10 && pVm.getId() <= 19) {
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 20 || pVm.getId() == 21) {
                finishTime = 5.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 22 && pVm.getId() <= 27) {
                finishTime = 8.0;
                Assert.assertEquals(0.571428571, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else {
                finishTime = 9.0;
                Assert.assertEquals(0.5, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);
            }

        }

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            finishTime = 2.0;

            if (pVm.getId() >= 30 && pVm.getId() <= 33) {
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 34 && pVm.getId() <= 37) {
                finishTime = 3.0;
                Assert.assertEquals(0.666666667, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 38 && pVm.getId() <= 39) {
                finishTime = 4.0;
                Assert.assertEquals(0.5, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 40 && pVm.getId() <= 41) {
                finishTime = 5.0;
                Assert.assertEquals(0.5, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 42) {
                finishTime = 6.0;
                Assert.assertEquals(0.4, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 43 && pVm.getId() <= 44) {
                finishTime = 7.0;
                Assert.assertEquals(0.333333333, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 45 && pVm.getId() <= 49) {
                finishTime = 8.0;
                Assert.assertEquals(0.285714286, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else {
                finishTime = 8.0;
                Assert.assertEquals(0.333333333, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);
            }


        }


    }

    private void testSimulationNewTraceThreeHostsRuntime0() {

        Assert.assertEquals(0.2, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.6, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.2, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.9, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.9, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(29, datacenter.getVmsRunning().size());


        testVmsP0StatisticsTimeEquals0();

        int hostId;

        hostId = 0;

        for (Vm vm : vmsP1) {

            if (vm.getId() < 20) {
                PreemptableVm pVm = (PreemptableVm) vm;

                if (hostId > 2)
                    hostId = 0;
                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }

        hostId = 1;

        for (Vm vm : vmsP2) {

            if (vm.getId() < 40) {

                PreemptableVm pVm = (PreemptableVm) vm;

                if (hostId > 2 && pVm.getId() > 33)
                    hostId = 0;

                else if (hostId > 2)
                    hostId = 1;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testSimulationNewTraceThreeHostsRuntime1() {

        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.4, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.4, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(24, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(26, datacenter.getVmsRunning().size());

        testVmP0StatisticsTimeGreaterThan0();

        testVmsP1StatisticsTime1To3();

        int hostId = 1;

        for (Vm vm : vmsP2) {

            if (vm.getId() < 50) {

                PreemptableVm pVm = (PreemptableVm) vm;

                if (pVm.getId() < 40)
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                else
                    Assert.assertEquals(-1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


                if (pVm.getId() < 34) {

                    if (hostId > 2)
                        hostId = 1;

                    Assert.assertEquals(hostId++, pVm.getHost().getId());
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());

                } else {

                    Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));

                    if (pVm.getId() < 40) {
                        Assert.assertEquals(1, pVm.getNumberOfPreemptions());

                    } else {
                        Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    }

                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }
            }
        }

    }

    private void testSimulationNewTraceThreeHostsRuntime2() {

        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.4, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.4, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(30, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(26, datacenter.getVmsRunning().size());

        testVmP0StatisticsTimeGreaterThan0();

        testVmsP1StatisticsTime1To3();

        int hostId = 1;

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(-1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            if (pVm.getId() < 34) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() < 38) {

                if (hostId > 2)
                    hostId = 1;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());


            } else {

                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() < 40)
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                else
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }
        }
    }

    private void testSimulationNewTraceThreeHostsRuntime3() {

        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.4, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.4, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(26, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(26, datacenter.getVmsRunning().size());

        testVmP0StatisticsTimeGreaterThan0();

        testVmsP1StatisticsTime1To3();
        int hostId = 1;

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 42)
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(-1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


            if (pVm.getId() < 34) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() < 38) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            } else if (pVm.getId() < 42) {

                if (hostId > 2)
                    hostId = 1;
                if (pVm.getId() < 40)
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                else
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else {

                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() < 40)
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                else
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }
        }
    }

    private void testSimulationNewTraceThreeHostsRuntime4() {

        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(19, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(21, datacenter.getVmsRunning().size());

        testVmP0StatisticsTimeGreaterThan0();
        int hostId = 1;

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (vm.getId() > 19 && vm.getId() < 28) {

                if (hostId > 2)
                    hostId = 0;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() < 20)
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                else if (pVm.getId() < 22)
                    Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                else
                    Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 28) {

                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(-1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            }

        }

        hostId = 1;

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 42)
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() == 42)
                Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(-1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


            if (pVm.getId() < 34) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() < 38) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 42) {

                if (hostId > 2)
                    hostId = 1;

                if (pVm.getId() == 42)
                    hostId = 0;

                if (pVm.getId() < 40)
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                else
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                if (pVm.getId() > 39) {
                    Assert.assertEquals(hostId++, pVm.getHost().getId());
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                }

            } else {

                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }
        }
    }

    private void testSimulationNewTraceThreeHostsRuntime5() {

        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 1, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 1 - 0.2, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.2, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.2, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(15, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(21, datacenter.getVmsRunning().size());


        testVmP0StatisticsTimeGreaterThan0();


        int hostId = 0;

        for (Vm vm : vmsP1) {
            // running - 22 to 27 and 28 to 29
            PreemptableVm pVm = (PreemptableVm) vm;

            if (vm.getId() > 21 && vm.getId() < 28) {

                if (hostId > 2)
                    hostId = 0;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 28) {

                if (pVm.getId() == 28) {
                    Assert.assertEquals(1, pVm.getHost().getId());
                } else {
                    Assert.assertEquals(2, pVm.getHost().getId());
                }

                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            }

        }

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 42)
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() == 42)
                Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 45)
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


            if (pVm.getId() <= 33) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 37) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 39) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 41) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 44) {

                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));

                if (pVm.getId() == 42) {
                    Assert.assertEquals(0, pVm.getHost().getId());
                    Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

                } else if (pVm.getId() == 43) {
                    Assert.assertEquals(1, pVm.getHost().getId());
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

                } else {
                    Assert.assertEquals(2, pVm.getHost().getId());
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                }

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else {

                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }
        }

    }

    private void testSimulationNewTraceThreeHostsRuntime6() {

        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1 - 1, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1 - 1 - 1.2, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 1.5, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 1.5 - 1.2, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.3, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 1.5, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 1.5 - 1, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(29, datacenter.getVmsRunning().size());


        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 0) {
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getHost().getId());
            } else if (pVm.getId() == 1) {
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(1, pVm.getHost().getId());
            } else if (pVm.getId() == 2) {
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
            } else if (pVm.getId() == 9) {
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getHost().getId());
            } else {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            }

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() == 9) {
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertEquals(0.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }


        int hostId = 0;

        for (Vm vm : vmsP1) {
            // running - 22 to 27 and 28 to 29
            PreemptableVm pVm = (PreemptableVm) vm;


            if (pVm.getId() >= 10 && vm.getId() <= 19) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 20 && pVm.getId() <= 21) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 22 && pVm.getId() <= 29) {
                if (hostId > 2) {
                    hostId = 0;
                }

                if (vm.getId() == 28) {
                    Assert.assertEquals(1, pVm.getHost().getId());
                } else if (vm.getId() == 29) {
                    Assert.assertEquals(2, pVm.getHost().getId());
                } else {
                    Assert.assertEquals(hostId++, pVm.getHost().getId());
                }

                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() >= 28) {
                    Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }

            }
        }

        hostId = 0;
        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 42)
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() == 42)
                Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 45)
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(6.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


            if (pVm.getId() <= 33) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 37) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 39) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 42) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 59) {

                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() == 43) {
                    Assert.assertEquals(1, pVm.getHost().getId());
                } else if (pVm.getId() == 44) {
                    Assert.assertEquals(2, pVm.getHost().getId());
                } else if (pVm.getId() == 45) {
                    Assert.assertEquals(0, pVm.getHost().getId());
                } else {
                    if (hostId > 2) {
                        hostId = 0;
                    }

                    Assert.assertEquals(hostId++, pVm.getHost().getId());
                }
            }
        }

    }

    private void testSimulationNewTraceThreeHostsRuntime7() {

        Assert.assertEquals(1.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1 - 1.2, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.8, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 0.8, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(23, datacenter.getVmsRunning().size());

        testVmP0StatisticsTimeGreaterThan6();


        int hostId = 0;

        for (Vm vm : vmsP1) {
            // running - 22 to 27 and 28 to 29
            PreemptableVm pVm = (PreemptableVm) vm;


            if (pVm.getId() >= 10 && vm.getId() <= 19) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 20 && pVm.getId() <= 21) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 22 && pVm.getId() <= 29) {
                if (hostId > 2) {
                    hostId = 0;
                }

                if (vm.getId() == 28) {
                    Assert.assertEquals(1, pVm.getHost().getId());
                } else if (vm.getId() == 29) {
                    Assert.assertEquals(2, pVm.getHost().getId());
                } else {
                    Assert.assertEquals(hostId++, pVm.getHost().getId());
                }

                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() >= 28) {
                    Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }

            }
        }

        hostId = 0;
        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 42)
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() == 42)
                Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 45)
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(6.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


            if (pVm.getId() <= 33) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 37) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 39) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 42) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 44) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 59) {

                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() == 45) {
                    Assert.assertEquals(0, pVm.getHost().getId());

                } else {
                    if (hostId > 2) {
                        hostId = 0;
                    }

                    Assert.assertEquals(hostId++, pVm.getHost().getId());
                }
            }
        }

    }

    private void testSimulationNewTraceThreeHostsRuntime8() {

        Assert.assertEquals(hostCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(2.8, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(2.8, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());

        testVmP0StatisticsTimeGreaterThan6();


        for (Vm vm : vmsP1) {
            // running - 22 to 27 and 28 to 29
            PreemptableVm pVm = (PreemptableVm) vm;


            if (pVm.getId() >= 10 && vm.getId() <= 19) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 20 && pVm.getId() <= 21) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 22 && pVm.getId() <= 29) {

                if (vm.getId() == 28) {
                    Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertEquals(1, pVm.getHost().getId());

                } else if (vm.getId() == 29) {
                    Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertEquals(2, pVm.getHost().getId());

                } else {
                    Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                }

                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            }
        }

        testVmP2StatisticTimeGreaterThan7();

    }


    private void testSimulationNewTraceThreeHostsRuntime9() {

        Assert.assertEquals(hostCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(0, datacenter.getVmsRunning().size());

        testVmP0StatisticsTimeGreaterThan6();
        testVmP2StatisticTimeGreaterThan7();


        for (Vm vm : vmsP1) {
            // running - 22 to 27 and 28 to 29
            PreemptableVm pVm = (PreemptableVm) vm;


            if (pVm.getId() >= 10 && vm.getId() <= 19) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 20 && pVm.getId() <= 21) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 22 && pVm.getId() <= 29) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (vm.getId() == 28 || vm.getId() == 29) {
                    Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }

                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            }
        }


    }

    private void testVmP2StatisticTimeGreaterThan7() {
        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 42)
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() == 42)
                Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if (pVm.getId() < 45)
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(6.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


            if (pVm.getId() <= 33) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 37) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 39) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 42) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() <= 59) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }
        }
    }


    private void testVmP0StatisticsTimeGreaterThan6() {

        for (Vm vm : vmsP0) {
            PreemptableVm pVm = (PreemptableVm) vm;
            Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
            Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() < 9)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }
    }

    private void testVmP0StatisticsTimeGreaterThan0() {
        int hostId = 0;

        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (hostId > 2)
                hostId = 0;
            Assert.assertEquals(hostId++, pVm.getHost().getId());
            Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() < 9)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }
    }

    private void testVmsP0StatisticsTimeEquals0() {
        int hostId = 0;

        for (Vm vm : vmsP0) {

            if (vm.getId() < 9) {
                PreemptableVm pVm = (PreemptableVm) vm;

                if (hostId > 2)
                    hostId = 0;
                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testVmsP1StatisticsTime1To3() {
        int hostId = 0;

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;
            if (vm.getId() < 22) {

                if (hostId > 2)
                    hostId = 0;
                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() < 20)
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                else
                    Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


            } else {

                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(-1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            }

        }
    }

    @Test
    public void testSystem10HostsFCFSNewTrace() throws Exception{
        Log.disable();

        hostCapacity = 1.0;

        // creating host
        List<Pe> peList1 = new ArrayList<Pe>();
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));
        properties.setProperty("preemption_policy_class",
                "org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy");

        // creating list of hosts
        List<PreemptiveHost> hostList = new ArrayList<PreemptiveHost>();
        List<Host> hostList2 = new ArrayList<Host>();

        host1 = new PreemptiveHost(0, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host1);
        hostList.add(host1);

        host2 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host2);
        hostList.add(host2);

        host3 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host3);
        hostList.add(host3);

        host4 = new PreemptiveHost(3, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host4);
        hostList.add(host4);

        host5 = new PreemptiveHost(4, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host5);
        hostList.add(host5);

        host6 = new PreemptiveHost(5, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host6);
        hostList.add(host6);

        host7 = new PreemptiveHost(6, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host7);
        hostList.add(host7);

        host8 = new PreemptiveHost(7, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host8);
        hostList.add(host8);

        host9 = new PreemptiveHost(8, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host9);
        hostList.add(host9);

        host10 = new PreemptiveHost(9, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        hostList2.add(host10);
        hostList.add(host10);

        preemptableVmAllocationPolicy = new WorstFitPriorityBasedVmAllocationPolicy(hostList);

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        CloudSim.runStart();

        // creating data center
        Mockito.when(characteristics.getHostList()).thenReturn(hostList2);
            datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                    new LinkedList<Storage>(), 0, properties);

        //asserting host on data center and host total capacity
        Assert.assertEquals(host1, datacenter.getHostList().get(0));
        Assert.assertEquals(host2, datacenter.getHostList().get(1));
        Assert.assertEquals(host3, datacenter.getHostList().get(2));
        Assert.assertEquals(host4, datacenter.getHostList().get(3));
        Assert.assertEquals(host5, datacenter.getHostList().get(4));
        Assert.assertEquals(host6, datacenter.getHostList().get(5));
        Assert.assertEquals(host7, datacenter.getHostList().get(6));
        Assert.assertEquals(host8, datacenter.getHostList().get(7));
        Assert.assertEquals(host9, datacenter.getHostList().get(8));
        Assert.assertEquals(host10, datacenter.getHostList().get(9));


        for (Host host : datacenter.getHostList()) {
            Assert.assertEquals(hostCapacity, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        }

        // creating vms model P0S0, total of vms 6603
        // with cpu total requisition of 3301.5
        populateVmListsNewTrace();

        submitEventsNewTrace();
        CloudSim.runClockTick();


        advanceTime(0.0);
        //allocating vms with submit time 0
        testSimulationNewTrace10HostsRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        testSimulationNewTrace10HostsRuntime1();

        advanceTime(2.0);
        //allocating vms with submit time 2
        testSimulationNewTrace10HostsRuntime2();

        advanceTime(3.0);
        testSimulationNewTrace10HostsRuntime3();

        advanceTime(4.0);
        testSimulationNewTrace10HostsRuntime4();

        advanceTime(5.0);
        testSimulationNewTrace10HostsRuntime5();

        advanceTime(6.0);
        testSimulationNewTrace10HostsRuntime6();

        advanceTime(7.0);
        testSimulationNewTrace10HostsRuntime7();

        advanceTime(8.0);
        testSimulationNewTrace10HostsRuntime8();

        advanceTime(9.0);
        testSimulationNewTrace10HostsRuntime9();

        verifyAvailabilityOf10HostsNewTrace();
    }

    private void verifyAvailabilityOf10HostsNewTrace() {
        double finishTime;

        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 3 && pVm.getId() <= 8) {
                finishTime = 6.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);
            } else {
                finishTime = 7.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);
            }

        }

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;


            if (pVm.getId() == 11) {
                finishTime = 7.0;
                Assert.assertEquals(0.571428, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 10 && pVm.getId() <= 19) {
                finishTime = 4.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 20) {
                finishTime = 5.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 21 && pVm.getId() <= 28) {
                finishTime = 8.0;
                Assert.assertEquals(0.571428, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else {
                finishTime = 9.0;
                Assert.assertEquals(0.5, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);
            }
        }

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            finishTime = 2.0;

            if (pVm.getId() >= 33 && pVm.getId() <= 39) {
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 30 && pVm.getId() <= 32) {
                finishTime = 3.0;
                Assert.assertEquals(0.666666, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 40 || pVm.getId() == 41) {

                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 42 && pVm.getId() <= 45) {
                finishTime = 4.0;
                Assert.assertEquals(0.666666, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 46) {
                finishTime = 5.0;
                Assert.assertEquals(0.5, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 47) {
                finishTime = 6.0;
                Assert.assertEquals(0.4, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 48 && pVm.getId() < 50) {

                finishTime = 7.0;
                Assert.assertEquals(0.333333, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 50) {

                finishTime = 7.0;
                Assert.assertEquals(0.4, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);

            }else {
                finishTime = 8.0;
                Assert.assertEquals(0.333333333, pVm.getCurrentAvailability(finishTime), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testSimulationNewTrace10HostsRuntime9() {

        for (Host host :
                datacenter.getHostList()) {

            PreemptiveHost pHost = (PreemptiveHost) host;

            Assert.assertEquals(hostCapacity, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
            Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
            Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
            Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
        }

        verifyVmsP0Time7To9();

        int hostId = 1;
        for (Vm vm : vmsP1) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 10 || (pVm.getId() >= 11 && pVm.getId() <= 19)) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 20) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 21 && pVm.getId() <= 28) { // running

                if (hostId == 8)
                    hostId = 9;

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else { // 29 finished
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }

            if (pVm.getId() == 11) {
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
            } else {
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
        }

        hostId = 6;
        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 32) { // finish

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 40 || pVm.getId() == 41) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 42 && pVm.getId() <= 50) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() >= 47 && pVm.getId() <= 50) {
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());
                } else {
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

                if (pVm.getId() >= 46 && pVm.getId() <= 50) {
                    Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(2.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }


            } else { // running 51 - 59

                if (hostId > 8)
                    hostId = 3;

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                Assert.assertEquals(6.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            }
        }


    }

    private void testSimulationNewTrace10HostsRuntime8() {

        for (Host host :
                datacenter.getHostList()) {
            PreemptiveHost pHost = (PreemptiveHost) host;

            if (pHost.getId() != 8) {
                Assert.assertEquals(hostCapacity, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertEquals(hostCapacity / 2, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity / 2, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity / 2, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            }
        }

        verifyVmsP0Time7To9();


        int hostId = 1;
        for (Vm vm : vmsP1) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 10 || (pVm.getId() >= 11 && pVm.getId() <= 19)) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 20) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 21 && pVm.getId() <= 28) { // running

                if (hostId == 8)
                    hostId = 9;

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else { // 29 running
                Assert.assertEquals(29, pVm.getId());
                Assert.assertEquals(8, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }

            if (pVm.getId() == 11) {
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
            } else {
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
        }

        hostId = 6;
        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 32) { // finish

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 40 || pVm.getId() == 41) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 42 && pVm.getId() <= 50) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() >= 47 && pVm.getId() <= 50) {
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());
                } else {
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

                if (pVm.getId() >= 46 && pVm.getId() <= 50) {
                    Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(2.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }


            } else { // running 51 - 59

                if (hostId > 8)
                    hostId = 3;

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                Assert.assertEquals(6.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            }
        }
    }

    private void testSimulationNewTrace10HostsRuntime7() {
        for (Host host : datacenter.getHostList()) {

            PreemptiveHost pHost = (PreemptiveHost) host;

            if (pHost.getId() == 0) {
                Assert.assertEquals(hostCapacity, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

            } else if (pHost.getId() == 1 || pHost.getId() == 2 || pHost.getId() == 9) {
                Assert.assertEquals(0.5, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            } else if (pHost.getId() == 3 || pHost.getId() == 4 || pHost.getId() == 5) {
                Assert.assertEquals(0.3, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.3, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            } else { // 6, 7 e 8
                Assert.assertEquals(0.1, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.1, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            }

        }

        Assert.assertEquals(18, datacenter.getVmsRunning().size());
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());

        verifyVmsP0Time7To9();


        int hostId = 1;
        for (Vm vm : vmsP1) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 10 || (pVm.getId() >= 11 && pVm.getId() <= 19)) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 20) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 21 && pVm.getId() <= 28) { // running

                if (hostId == 8)
                    hostId = 9;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else { // 29 running
                Assert.assertEquals(29, pVm.getId());
                Assert.assertEquals(8, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }

            if (pVm.getId() == 11) {
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
            } else {
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

        }

        hostId = 6;
        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 32) { // finish

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 40 || pVm.getId() == 41) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 42 && pVm.getId() <= 50) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() >= 47 && pVm.getId() <= 50) {
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());
                } else {
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

                if (pVm.getId() >= 46 && pVm.getId() <= 50) {
                    Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(2.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }


            } else { // running 51 - 59

                if (hostId > 8)
                    hostId = 3;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                Assert.assertEquals(6.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            }
        }
    }

    private void testSimulationNewTrace10HostsRuntime6() {

        for (Host host : datacenter.getHostList()) {

            PreemptiveHost pHost = (PreemptiveHost) host;

            if (pHost.getId() == 0) {
                Assert.assertEquals(0.2, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.2, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.2, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            } else if (pHost.getId() == 1 || pHost.getId() == 2 || pHost.getId() == 9) {
                Assert.assertEquals(0, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertEquals(0.1, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.1, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            }

        }

        Assert.assertEquals(26, datacenter.getVmsRunning().size());
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());

        int hostId = 0;

        for (Vm vm : vmsP0) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 3 && pVm.getId() <= 8) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            } else { // running
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                if (hostId == 3)
                    hostId = 9;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
            }

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() == 9) {
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }

        verifyP1VmsStatiticsTime5To6();

        hostId = 3;
        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 32) { // finish

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 40 || pVm.getId() == 41) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 42 && pVm.getId() <= 47) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() == 47) {
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());
                } else {
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

                if (pVm.getId() == 46 || pVm.getId() == 47) {
                    Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(2.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }


            } else { // running 48 - 59

                if (hostId > 8)
                    hostId = 3;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() >= 48 && pVm.getId() <= 50) {
                    Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());

                } else { // 51-59
                    Assert.assertEquals(6.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                }
            }
        }
    }

    private void testSimulationNewTrace10HostsRuntime5() {
        testHostsAvailableMipsTimes4To5And10Hosts();
        Assert.assertEquals(21, datacenter.getVmsRunning().size());
        Assert.assertEquals(12, datacenter.getVmsForScheduling().size());

        verifyP0VmsStatisticsTime1To5();

        verifyP1VmsStatiticsTime5To6();


        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 32) { // finish

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 40 || pVm.getId() == 41) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 42 && pVm.getId() <= 46) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

                if (pVm.getId() == 46) {
                    Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(2.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }

            } else if (pVm.getId() == 47) { // running

                Assert.assertEquals(0, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 48 && pVm.getId() <= 50) { // waiting
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
            } else {
                // 51 to 59 waiting
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            }

        }


    }

    private void testSimulationNewTrace10HostsRuntime4() {
        testHostsAvailableMipsTimes4To5And10Hosts();
        Assert.assertEquals(21, datacenter.getVmsRunning().size());
        Assert.assertEquals(14, datacenter.getVmsForScheduling().size());

        verifyP0VmsStatisticsTime1To5();

        int hostId = 1;
        for (Vm vm : vmsP1) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 10 || (pVm.getId() >= 12 && pVm.getId() <= 19)) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 11) {
                Assert.assertEquals(0, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 20) {
                Assert.assertEquals(8, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 21 && pVm.getId() <= 28) {

                if (hostId == 8)
                    hostId = 9;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {
                Assert.assertEquals(29, pVm.getId());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }

            if (pVm.getId() == 11) {
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
            } else {
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

        }

        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 32) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 40 || pVm.getId() == 41) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 42 && pVm.getId() <= 45) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(2.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 46) {

                Assert.assertEquals(0, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 47 && pVm.getId() <= 50) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
            } else {

                // 51 to 59
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            }

        }


    }

    private void testSimulationNewTrace10HostsRuntime3() {
        testHostsAvailableMipsTimes1To3And10Hosts();
        Assert.assertEquals(29, datacenter.getVmsRunning().size());
        Assert.assertEquals(19, datacenter.getVmsForScheduling().size());

        verifyP0VmsStatisticsTime1To5();
        verifyP1VmsStatisticsTime1To3();

        int hostId = 3;
        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 32) {

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 40 || pVm.getId() == 41) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 42 && pVm.getId() <= 45) {

                if (hostId > 9)
                    hostId = 0;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(2.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

                // test if it is last vm to set value of hostId for verify next group of vms
                if (pVm.getId() == 45) {
                    hostId = 0;
                }

            } else if (pVm.getId() >= 46 && pVm.getId() <= 50) {

                if (hostId == 3)
                    hostId = 7;
                else if (hostId == 8) {
                    hostId = 9;
                }

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
            } else {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            }

        }
    }

    private void testSimulationNewTrace10HostsRuntime2() {
        testHostsAvailableMipsTimes1To3And10Hosts();

        Assert.assertEquals(29, datacenter.getVmsRunning().size());
        Assert.assertEquals(24, datacenter.getVmsForScheduling().size());

        verifyP0VmsStatisticsTime1To5();
        verifyP1VmsStatisticsTime1To3();

        int hostId = 0;
        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 32) {
                if (hostId > 9)
                    hostId = 0;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) {
                hostId = 3;
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 40) {
                Assert.assertEquals(7, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() == 41) {
                Assert.assertEquals(9, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 42 && pVm.getId() <= 45) {

                if (hostId > 9)
                    hostId = 0;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(2.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
            } else {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            }

        }


    }

    private void testSimulationNewTrace10HostsRuntime1() {
        testHostsAvailableMipsTimes1To3And10Hosts();

        Assert.assertEquals(29, datacenter.getVmsRunning().size());
        Assert.assertEquals(21, datacenter.getVmsForScheduling().size());

        verifyP0VmsStatisticsTime1To5();

        verifyP1VmsStatisticsTime1To3();

        int hostId = 0;
        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 32) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) {
                if (hostId > 9)
                    hostId = 0;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

            } else if (pVm.getId() == 40) {
                Assert.assertEquals(7, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

            } else if (pVm.getId() == 41) {
                Assert.assertEquals(9, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            } else {
                if (pVm.getId() >= 42 && pVm.getId() <= 49) {
                    Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                } else {
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                }
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            }

            Assert.assertEquals(0, pVm.getNumberOfMigrations());
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

        }


    }

    private void testHostsAvailableMipsTimes1To3And10Hosts() {
        for (Host host : datacenter.getHostList()) {

            PreemptiveHost pHost = (PreemptiveHost) host;
            Assert.assertEquals(0, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
            Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);

            if (pHost.getId() == 8) {
                Assert.assertEquals(0, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

            } else {
                Assert.assertEquals(0.2, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testHostsAvailableMipsTimes4To5And10Hosts() {
        for (Host host : datacenter.getHostList()) {

            PreemptiveHost pHost = (PreemptiveHost) host;
            Assert.assertEquals(0, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
            Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);

            if (pHost.getId() == 0) {
                Assert.assertEquals(0.2, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

            } else {
                Assert.assertEquals(0, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testSimulationNewTrace10HostsRuntime0() {

        for (Host host : datacenter.getHostList()) {

            PreemptiveHost pHost = (PreemptiveHost) host;

            if (pHost.getId() == 7) {
                Assert.assertEquals(0.2, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.2, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.2, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            } else if (pHost.getId() == 8) {
                Assert.assertEquals(0.1, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.1, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            } else if (pHost.getId() == 9) {
                Assert.assertEquals(0.2, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(hostCapacity, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.4, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.2, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertEquals(0, pHost.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.5, pHost.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0.2, pHost.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pHost.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);
            }

        }

        Assert.assertEquals(29, datacenter.getVmsRunning().size());
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());

        int hostId = 0;

        for (Vm vm : vmsP0) {
            PreemptableVm pVm = (PreemptableVm) vm;
            if (vm.getId() < 9) {

                if (hostId > 9)
                    hostId = 0;
                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }

        hostId = 0;
        for (Vm vm : vmsP1) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 10 || pVm.getId() == 11) {
                Assert.assertEquals(9, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 12 && pVm.getId() <= 19) {

                if (hostId > 9)
                    hostId = 0;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());
        }

        hostId = 0;
        for (Vm vm : vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 30 || pVm.getId() == 32) {
                Assert.assertEquals(8, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 31) {
                Assert.assertEquals(9, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 33 && pVm.getId() <= 39) {
                if (hostId > 9)
                    hostId = 0;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

        }


    }


    private void verifyP0VmsStatisticsTime1To5() {
        int hostId = 0;

        for (Vm vm : vmsP0) {
            PreemptableVm pVm = (PreemptableVm) vm;
            if (hostId > 9)
                hostId = 0;

            Assert.assertEquals(hostId++, pVm.getHost().getId());
            Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() == 9) {
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void verifyP1VmsStatisticsTime1To3() {
        int hostId = 0;
        for (Vm vm : vmsP1) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 10) {
                Assert.assertEquals(9, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 11) {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 20) {
                Assert.assertEquals(8, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 12 && pVm.getId() <= 19) {

                if (hostId > 9)
                    hostId = 0;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }

            if (pVm.getId() == 11) {
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
            } else {
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            }

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());
        }
    }

    private void verifyP1VmsStatiticsTime5To6() {
        int hostId = 1;
        for (Vm vm : vmsP1) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 10 || (pVm.getId() >= 12 && pVm.getId() <= 19)) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 11) { // running
                Assert.assertEquals(0, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 20) { // finish
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() >= 21 && pVm.getId() <= 28) { // running

                if (hostId == 8)
                    hostId = 9;

                Assert.assertEquals(hostId++, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else { // 29 running
                Assert.assertEquals(29, pVm.getId());
                Assert.assertEquals(8, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }

            if (pVm.getId() == 11) {
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
            } else {
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

        }
    }

    private void verifyVmsP0Time7To9() {
        for (Vm vm : vmsP0) {
            PreemptableVm pVm = (PreemptableVm) vm;

            Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
            Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() == 9) {
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }


}
