package org.cloudbus.cloudsim.preemption;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.policies.preemption.TTVBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.BestFitAvailabilityAwareVmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.BestFitPriorityBasedVmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

/**
 * Created by jvmafra on 24/04/17.
 */
public class SystemTestBestFitTTV {

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
        properties.setProperty(TTVBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
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
        properties.setProperty("preemption_policy_class", "org.cloudbus.cloudsim.preemption.policies.preemption.TTVBasedPreemptionPolicy");
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
                peList1), new TTVBasedPreemptionPolicy(properties));

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
        preemptableVmAllocationPolicy = new BestFitPriorityBasedVmAllocationPolicy(googleHostList);

        // creating data center
        datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                new LinkedList<Storage>(), 0, properties);

        datacenter.setSimulationTimeUtil(timeUtil);

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());
    }


    @After
    public void tearDown() {
        new java.io.File(datacenterOutputFile).delete();
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
    public void testSystemThreeHostsBestFitTTVNewTrace() throws Exception {

        Log.disable();

        hostCapacity = 3.3;

        // creating host
        List<Pe> peList1 = new ArrayList<Pe>();
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));
        properties.setProperty("preemption_policy_class",
                "org.cloudbus.cloudsim.preemption.policies.preemption.TTVBasedPreemptionPolicy");

        host1 = new PreemptiveHost(0, peList1, new VmSchedulerMipsBased(
                peList1), new TTVBasedPreemptionPolicy(properties));
        host2 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new TTVBasedPreemptionPolicy(properties));
        host3 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(
                peList1), new TTVBasedPreemptionPolicy(properties));

        // creating list of hosts
        List<PreemptiveHost> hostList = new ArrayList<PreemptiveHost>();
        List<Host> hostList2 = new ArrayList<Host>();
        hostList.add(host1);
        hostList.add(host2);
        hostList.add(host3);

        hostList2.add(host1);
        hostList2.add(host2);
        hostList2.add(host3);

        preemptableVmAllocationPolicy = new BestFitAvailabilityAwareVmAllocationPolicy(hostList);

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
        testSimulationThreeHostsRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        testSimulationThreeHostsRuntime1();

        advanceTime(2.0);
        //allocating vms with submit time 2
        testSimulationThreeHostsRuntime2();

        advanceTime(3.0);
        testSimulationThreeHostsRuntime3();

        advanceTime(4.0);
        testSimulationThreeHostsRuntime4();

        advanceTime(5.0);
        testSimulationThreeHostsRuntime5();

        advanceTime(6.0);
        testSimulationThreeHostsRuntime6();

        advanceTime(7.0);
        testSimulationThreeHostsRuntime7();

        advanceTime(8.0);
        testSimulationThreeHostsRuntime8();

        advanceTime(9.0);
        testSimulationThreeHostsRuntime9();

        verifyAvailabilityOfThreeHosts();

    }

    private void verifyAvailabilityOfThreeHosts() {
        double finishedTime;

        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() <= 2 || pVm.getId() == 9)
                finishedTime = 7.0;
            else
                finishedTime = 6.0;

            Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
        }

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 10 && pVm.getId() <= 19) {
                if (pVm.getId() == 10 || pVm.getId() == 11 || pVm.getId() == 17){
                    finishedTime = 4.0;
                    Assert.assertEquals(1d, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                } else {
                    finishedTime = 7.0;
                    Assert.assertEquals(0.571428571, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                }
            }
            else if (pVm.getId() >= 22 && pVm.getId() <= 24) {
                finishedTime = 5.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            }
            else if (pVm.getId() == 20 || pVm.getId() == 21 || pVm.getId() == 25) {
                finishedTime = 6.0;
                Assert.assertEquals(0.8, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            }
            else if (pVm.getId() == 26 || pVm.getId() == 27){
                finishedTime = 7.0;
                Assert.assertEquals(0.666666667, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            } else {
                finishedTime = 8.0;
                Assert.assertEquals(0.571428571, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            }
        }

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() >= 30 && pVm.getId() <= 39) {
                if (pVm.getId() == 39){
                    finishedTime = 8.0;
                    Assert.assertEquals(0.25, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                } else {
                    finishedTime = 7.0;
                    Assert.assertEquals(0.285714286, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                }

            }
            else if (pVm.getId() >= 40 && pVm.getId() <= 49) {

                if (pVm.getId() == 40) {
                    finishedTime = 6;
                    Assert.assertEquals(0.4, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                }
                else {
                    finishedTime = 8.0;
                    Assert.assertEquals(0.285714286, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                }
            }
            else { // 50-59
                finishedTime = 9.0;
                Assert.assertEquals(0.285714286, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testVmNeverAllocated(PreemptableVm pVm) {
        testVmForScheduling(pVm);
        Assert.assertEquals(-1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
    }

    private void testVmStatisticsWithDefaultValues(PreemptableVm pVm) {
        Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
        Assert.assertEquals(0, pVm.getNumberOfPreemptions());
        Assert.assertEquals(0, pVm.getNumberOfMigrations());
    }

    private void testVmStatisticsWithOnePreemption(PreemptableVm pVm) {
        Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
        Assert.assertEquals(1, pVm.getNumberOfPreemptions());
        Assert.assertEquals(0, pVm.getNumberOfMigrations());
    }

    private void testVmStatisticsWithOnePreemptionAndOneMigration(PreemptableVm pVm) {
        Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
        Assert.assertEquals(1, pVm.getNumberOfPreemptions());
        Assert.assertEquals(1, pVm.getNumberOfMigrations());
    }

    private void testVmOutOfSystem(PreemptableVm pVm) {
        Assert.assertNull(pVm.getHost());
        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
    }

    private void testVmIsNotCreated(PreemptableVm pVm) {
        testVmOutOfSystem(pVm);
        Assert.assertEquals(-1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
    }

    private void testVmRunning(int hostId, PreemptableVm pVm) {
        Assert.assertEquals(hostId, pVm.getHost().getId());
        Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
    }

    private void testVmForScheduling(PreemptableVm pVm) {
        Assert.assertNull(pVm.getHost());
        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
        Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
    }

    private void testVmsP0StatisticsTimeEquals0() {

        int hostId = 0;

        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            testVmStatisticsWithDefaultValues(pVm);

            if (pVm.getId() < 9) {

                if (pVm.getId() > 5) {
                    hostId = 1;
                }
                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {
                testVmIsNotCreated(pVm);
            }
        }
    }

    private void testVmsP0StatisticsTime7To9(){
        for (Vm vm : vmsP0) { // all vms with priority 0 have already finished

            PreemptableVm pVm = (PreemptableVm) vm;

            testVmStatisticsWithDefaultValues(pVm);
            Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
            Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

            if (vm.getId() == 9){
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testVmsP1StatisticsTime8And9() {
        for (Vm vm :
                vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (vm.getId() == 10 || vm.getId() == 11 || vm.getId() == 17){  // finished
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

            } else if ((vm.getId() >= 12 && vm.getId() <= 16) || vm.getId() == 18 || vm.getId() == 19){ // finished

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                if (vm.getId() == 13 || vm.getId() == 18 || vm.getId() == 19){
                    testVmStatisticsWithOnePreemption(pVm);
                } else {
                    testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                }

            }

            else if (vm.getId() >= 20 && vm.getId() <= 27){

                if (vm.getId() >= 22 && vm.getId() <= 24){ // finished
                    testVmStatisticsWithDefaultValues(pVm);
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                } else { // 20, 21, 25 finished in this time

                    if (pVm.getId() == 20 || pVm.getId() == 21){
                        testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    } else if (pVm.getId() == 25){
                        testVmStatisticsWithOnePreemption(pVm);
                        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    } else { // vms 26 and 27 finished
                        testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    }
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }
            } else { //  vms 28 and 29 finished
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            }
        }
    }

    private void testVmsP2StatisticsTime4And5(){
        for (Vm vm :
                vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40) {
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                testVmStatisticsWithOnePreemption(pVm);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));

            } else {

                if (pVm.getId() == 40){
                    testVmStatisticsWithDefaultValues(pVm);
                    Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                } else {
                    testVmStatisticsWithDefaultValues(pVm);
                    testVmNeverAllocated(pVm);
                }

            }

        }
    }

    private void testVmsP0StatisticsTime1To5() {

        int hostId = 0;

        for (Vm vm : vmsP0) {

            if (vm.getId() > 5)
                hostId = 1;

            PreemptableVm pVm = (PreemptableVm) vm;

            Assert.assertEquals(hostId, pVm.getHost().getId());
            Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
            testVmStatisticsWithDefaultValues(pVm);

            if (pVm.getId() != 9)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            else
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }
    }

    private void testVmsP1StatisticsTime1To3(){
        int hostId = 0;

        for (Vm vm :
                vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (vm.getId() == 10 || vm.getId() == 11 || vm.getId() == 17){
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(hostId, pVm.getHost().getId());
                hostId++;
            } else if ((vm.getId() >= 12 && vm.getId() <= 16) || vm.getId() == 18 || vm.getId() == 19){
                testVmStatisticsWithOnePreemption(pVm);
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
            }

            else if (vm.getId() >= 20 && vm.getId() <= 27){
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (vm.getId() == 20 || vm.getId() == 21){
                    Assert.assertEquals(1, pVm.getHost().getId());
                } else {
                    Assert.assertEquals(2, pVm.getHost().getId());
                }
            } else {
                testVmStatisticsWithDefaultValues(pVm);
                testVmNeverAllocated(pVm);
            }
        }
    }



    private void testSimulationThreeHostsRuntime0() {

        Assert.assertEquals(0d, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.4, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2.4, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.4, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(29, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTimeEquals0();


        int hostId = 0;

        for (Vm vm :
                vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            testVmStatisticsWithDefaultValues(pVm);

            if (pVm.getId() < 20) {

                if (pVm.getId() > 10 && pVm.getId() <= 16) {
                    hostId = 1;

                } else if (pVm.getId() > 16) {
                    hostId = 2;
                }

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else
                testVmIsNotCreated(pVm);
        }

        for (Vm vm :
                vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            testVmStatisticsWithDefaultValues(pVm);

            if (pVm.getId() < 40) {
                testVmRunning(hostId, pVm);
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else
                testVmIsNotCreated(pVm);
        }
    }

    private void testSimulationThreeHostsRuntime1() {

        Assert.assertEquals(0d, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(29, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(21, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime1To5();

        testVmsP1StatisticsTime1To3();

        for (Vm vm :
                vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40) {
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                testVmStatisticsWithOnePreemption(pVm);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));

            } else if (pVm.getId() < 50){
                testVmStatisticsWithDefaultValues(pVm);
                testVmNeverAllocated(pVm);
            } else {
                testVmIsNotCreated(pVm);
            }

        }
    }

    private void testSimulationThreeHostsRuntime2() {

        Assert.assertEquals(0d, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(39, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(21, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime1To5();

        testVmsP1StatisticsTime1To3();

        for (Vm vm :
                vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40) {
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                testVmStatisticsWithOnePreemption(pVm);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));

            } else {
                testVmStatisticsWithDefaultValues(pVm);
                testVmNeverAllocated(pVm);
            }

        }
    }

    private void testSimulationThreeHostsRuntime3() {
        testSimulationThreeHostsRuntime2();
    }


    private void testSimulationThreeHostsRuntime4() {


        // vms 10, 11 and 17 were destroyed in this time

        Assert.assertEquals(0d, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(34, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(23, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime1To5();

        int hostId = 0;

        for (Vm vm :
                vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (vm.getId() == 10 || vm.getId() == 11 || vm.getId() == 17){  // finish in this time
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

            } else if ((vm.getId() >= 12 && vm.getId() <= 16) || vm.getId() == 18 || vm.getId() == 19){
                if (vm.getId() >= 14) {
                    hostId = 2;
                }

                Assert.assertEquals(hostId, pVm.getHost().getId());
                hostId++;


                if (vm.getId() == 13 || vm.getId() == 18 || vm.getId() == 19){
                    testVmStatisticsWithOnePreemption(pVm);
                } else {
                    testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                }
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            }

            else if (vm.getId() >= 20 && vm.getId() <= 27){ // vms 20, 21, 25-27 were preempted now

                if (vm.getId() >= 22 && vm.getId() <= 24){
                    testVmStatisticsWithDefaultValues(pVm);
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    Assert.assertEquals(2, pVm.getHost().getId());
                } else {
                    testVmStatisticsWithOnePreemption(pVm);
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                }
            } else { //  vms 28 and 29
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getHost().getId());
            }
        }

        testVmsP2StatisticsTime4And5();

    }

    private void testSimulationThreeHostsRuntime5() {

        // vms 22, 23 and 14 were destroyed in this time

        Assert.assertEquals(0d, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(31, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(23, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime1To5();


        int hostId = 0;

        for (Vm vm :
                vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (vm.getId() == 10 || vm.getId() == 11 || vm.getId() == 17){  // finished
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

            } else if ((vm.getId() >= 12 && vm.getId() <= 16) || vm.getId() == 18 || vm.getId() == 19){

                if (vm.getId() >= 14) {
                    hostId = 2;
                }

                Assert.assertEquals(hostId, pVm.getHost().getId());
                hostId++;

                if (vm.getId() == 13 || vm.getId() == 18 || vm.getId() == 19){
                    testVmStatisticsWithOnePreemption(pVm);
                } else {
                    testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                }
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            }

            else if (vm.getId() >= 20 && vm.getId() <= 27){

                if (vm.getId() >= 22 && vm.getId() <= 24){ // finish in this time
                    testVmStatisticsWithDefaultValues(pVm);
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                } else { // 20, 21, 25 were allocated now

                    if (pVm.getId() == 20 || pVm.getId() == 21){
                        testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                        Assert.assertEquals(2, pVm.getHost().getId());
                        Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    } else if (pVm.getId() == 25){
                        testVmStatisticsWithOnePreemption(pVm);
                        Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                        Assert.assertEquals(2, pVm.getHost().getId());
                    } else {
                        testVmStatisticsWithOnePreemption(pVm);
                        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                    }
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }
            } else { //  vms 28 and 29
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getHost().getId());
            }
        }


        testVmsP2StatisticsTime4And5();

    }

    private void testSimulationThreeHostsRuntime6() {
        // vms 3-8, 20, 21 and 25, 40 were destroyed in this time

        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.5, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2.8, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.5, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0d, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(11, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(33, datacenter.getVmsRunning().size());

        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            testVmStatisticsWithDefaultValues(pVm);

            if (pVm.getId() >= 3 && pVm.getId() <= 8){ // finished vms
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else {
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));

                if (vm.getId() == 9){
                    Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertEquals(1, pVm.getHost().getId());
                } else {
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertEquals(0, pVm.getHost().getId());
                }
            }
        }


        int hostId = 0;

        for (Vm vm :
                vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (vm.getId() == 10 || vm.getId() == 11 || vm.getId() == 17){  // finished
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

            } else if ((vm.getId() >= 12 && vm.getId() <= 16) || vm.getId() == 18 || vm.getId() == 19){

                if (vm.getId() >= 14) {
                    hostId = 2;
                }

                Assert.assertEquals(hostId, pVm.getHost().getId());
                hostId++;


                if (vm.getId() == 13 || vm.getId() == 18 || vm.getId() == 19){
                    testVmStatisticsWithOnePreemption(pVm);
                } else {
                    testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                }

                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            }

            else if (vm.getId() >= 20 && vm.getId() <= 27){

                if (vm.getId() >= 22 && vm.getId() <= 24){ // finished
                    testVmStatisticsWithDefaultValues(pVm);
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                } else { // 20, 21, 25 finished in this time

                    if (pVm.getId() == 20 || pVm.getId() == 21){
                        testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    } else if (pVm.getId() == 25){
                        testVmStatisticsWithOnePreemption(pVm);
                        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    } else { // vms 26 and 27
                        testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                        Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                        Assert.assertEquals(0, pVm.getHost().getId());
                    }
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }
            } else { //  vms 28 and 29
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getHost().getId());
            }
        }


        for (Vm vm :
                vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40) { // 30-39

                testVmStatisticsWithOnePreemption(pVm);
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                if (pVm.getId() == 39){
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                } else {
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    Assert.assertEquals(2, pVm.getHost().getId());
                }

            } else if (pVm.getId() < 50){ // 40-49, 40 has finished
                testVmStatisticsWithDefaultValues(pVm);

                if (pVm.getId() == 40){
                    Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                } else {
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    Assert.assertEquals(6d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


                    if (pVm.getId() == 41 || pVm.getId() == 42){
                        Assert.assertEquals(0, pVm.getHost().getId());
                    } else {
                        Assert.assertEquals(1, pVm.getHost().getId());
                    }

                }


            } else {
                testVmStatisticsWithDefaultValues(pVm);
                testVmNeverAllocated(pVm);

            }

        }

    }

    private void testSimulationThreeHostsRuntime7(){

        // vms 0-2, 9, 12-16, 18, 19, 26, 27, 30-38 were destroyed in this time

        Assert.assertEquals(1.5, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.5, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2.3, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(3.3, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(22, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime7To9();

        for (Vm vm :
                vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (vm.getId() == 10 || vm.getId() == 11 || vm.getId() == 17){  // finished
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

            } else if ((vm.getId() >= 12 && vm.getId() <= 16) || vm.getId() == 18 || vm.getId() == 19){ // finished

                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                if (vm.getId() == 13 || vm.getId() == 18 || vm.getId() == 19){
                    testVmStatisticsWithOnePreemption(pVm);
                } else {
                    testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                }

            }

            else if (vm.getId() >= 20 && vm.getId() <= 27){

                if (vm.getId() >= 22 && vm.getId() <= 24){ // finished
                    testVmStatisticsWithDefaultValues(pVm);
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                } else { // 20, 21, 25 finished in this time

                    if (pVm.getId() == 20 || pVm.getId() == 21){
                        testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    } else if (pVm.getId() == 25){
                        testVmStatisticsWithOnePreemption(pVm);
                        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    } else { // vms 26 and 27 finished
                        testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                        Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                        Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    }
                    Assert.assertEquals(1d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }
            } else { //  vms 28 and 29 running yet
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getHost().getId());
            }
        }


        for (Vm vm :
                vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40) { // 30-38 finished, 39 running


                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                if (pVm.getId() == 39){
                    testVmStatisticsWithOnePreemptionAndOneMigration(pVm);
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    Assert.assertEquals(1, pVm.getHost().getId());

                } else {
                    testVmStatisticsWithOnePreemption(pVm);
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                }

            } else if (pVm.getId() < 50){ // 40-49, 40 has finished
                testVmStatisticsWithDefaultValues(pVm);

                if (pVm.getId() == 40){
                    Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                } else {
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                    Assert.assertEquals(6d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);


                    if (pVm.getId() == 41 || pVm.getId() == 42){
                        Assert.assertEquals(0, pVm.getHost().getId());
                    } else {
                        Assert.assertEquals(1, pVm.getHost().getId());
                    }

                }

            } else { // 50-59 were allocated in this time
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (vm.getId() >= 50 && vm.getId() <= 52){
                    Assert.assertEquals(1, pVm.getHost().getId());
                } else {
                    Assert.assertEquals(0, pVm.getHost().getId());
                }

                Assert.assertEquals(7d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            }

        }



    }


    private void testSimulationThreeHostsRuntime8(){
        // vms 41-49, 39, 28 and 29 have finished

        Assert.assertEquals(1.9, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.9, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(2.7, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2.7, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(3.3, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(10, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime7To9();

        testVmsP1StatisticsTime8And9();

        for (Vm vm :
                vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40) { // 30-39 finished


                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() == 39){
                    testVmStatisticsWithOnePreemptionAndOneMigration(pVm);

                } else {
                    testVmStatisticsWithOnePreemption(pVm);
                }

            } else if (pVm.getId() < 50){ // 40-49 finished

                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() == 40){
                    Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(6d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }

            } else { // 50-59 running
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (vm.getId() >= 50 && vm.getId() <= 52){
                    Assert.assertEquals(1, pVm.getHost().getId());
                } else {
                    Assert.assertEquals(0, pVm.getHost().getId());
                }

                Assert.assertEquals(7d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            }

        }


    }

    private void testSimulationThreeHostsRuntime9(){
        // vms 50-59 have finished

        Assert.assertEquals(3.3, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(3.3, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(3.3, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(0, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime7To9();

        testVmsP1StatisticsTime8And9();

        for (Vm vm :
                vmsP2) {
            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40) { // 30-39 finished


                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() == 39){
                    testVmStatisticsWithOnePreemptionAndOneMigration(pVm);

                } else {
                    testVmStatisticsWithOnePreemption(pVm);
                }

            } else if (pVm.getId() < 50){ // 40-49 finished

                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() == 40){
                    Assert.assertEquals(4d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(6d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }

            } else { // 50-59 finished
                testVmStatisticsWithDefaultValues(pVm);
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(7d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            }

        }
    }


}
