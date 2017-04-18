package org.cloudbus.cloudsim.preemption;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
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
 * Created by Alessandro Lia Fook Santos on 12/04/17.
 */
public class SystemTestBestFitFCFS {

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
    public void testSystemThreeHostsBestFitFCFSNewTrace() throws Exception {

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

        preemptableVmAllocationPolicy = new BestFitPriorityBasedVmAllocationPolicy(hostList);

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
        Iterator iterator = vmsP1.iterator();

        while (iterator.hasNext()) {

            PreemptableVm pVm = (PreemptableVm) iterator.next();

            if (pVm.getId() < 20) {

                if (pVm.getId() > 10 && pVm.getId() <= 16)
                    hostId = 1;

                else if (pVm.getId() > 16)
                    hostId = 2;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else
                break;
        }

        iterator = vmsP2.iterator();

        while (iterator.hasNext()) {

            PreemptableVm pVm = (PreemptableVm) iterator.next();

            if (pVm.getId() < 40) {

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            } else
                break;
        }
    }

    private void testVmsP0StatisticsTimeEquals0() {

        int hostId = 0;

        for (Vm vm : vmsP0) {

            if (vm.getId() < 9) {
                PreemptableVm pVm = (PreemptableVm) vm;

                if (vm.getId() > 5)
                    hostId = 1;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testSimulationThreeHostsRuntime1() {

        Assert.assertEquals(0d, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.0, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.4, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.0, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(26, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(24, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime1To5();

        int hostId = 0;

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 15 || pVm.getId() == 16) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() < 24) {

                if (pVm.getId() > 10 && pVm.getId() <= 14)
                    hostId = 1;

                else if (pVm.getId() >= 17)
                    hostId = 2;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() < 20)
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                else
                    Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }


        Iterator iterator = vmsP2.iterator();

        while (iterator.hasNext()) {

            PreemptableVm pVm = (PreemptableVm) iterator.next();

            if (pVm.getId() < 50) {

                if (pVm.getId() < 32) {

                    Assert.assertEquals(hostId, pVm.getHost().getId());
                    Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                } else if (pVm.getId() < 40) {

                    Assert.assertNull(pVm.getHost());
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                } else {

                    Assert.assertNull(pVm.getHost());
                    Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                    Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                    Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }
            } else
                break;
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
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() != 9)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            else
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }
    }

    private void testSimulationThreeHostsRuntime2() {

        Assert.assertEquals(0d, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(35, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(23, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime1To5();

        int hostId = 0;

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 15) {

                Assert.assertEquals(2, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 16) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() < 24) {

                if (pVm.getId() > 10 && pVm.getId() <= 14)
                    hostId = 1;

                else if (pVm.getId() >= 17)
                    hostId = 2;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() < 20)
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                else
                    Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }


        for (Vm vm :
                vmsP2) {


            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 32) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() < 40) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testSimulationThreeHostsRuntime3() {
        testSimulationThreeHostsRuntime2();
    }

    private void testSimulationThreeHostsRuntime4() {

        Assert.assertEquals(0d, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.0, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.0, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.0, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(29, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(21, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime1To5();

        int hostId = 0;

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 15 || pVm.getId() == 16) {

                if (pVm.getId() == 15)
                    hostId = 2;
                else
                    hostId = 0;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() < 20) {


                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() < 28) {

                if (pVm.getId() == 26 || pVm.getId() == 27)
                    hostId = 1;

                else
                    hostId = 2;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() < 24)
                    Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                else
                    Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }


        for (Vm vm :
                vmsP2) {


            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 32) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 32) {

                hostId = 1;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() < 40) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testSimulationThreeHostsRuntime5() {

        Assert.assertEquals(0d, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.3, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(20, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(24, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime1To5();

        int hostId;

        testVmsP1StatisticsTime5And6();

        for (Vm vm :
                vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 33) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                if (pVm.getId() == 32) {
                    Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());

                } else {
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }

            } else if (pVm.getId() < 40) {

                if (pVm.getId() == 33) {
                    hostId = 1;
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());

                } else {
                    hostId = 2;
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }

                Assert.assertEquals(hostId, pVm.getHost().getId());

                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertTrue(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(-1, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testVmsP1StatisticsTime5And6() {

        int hostId;

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 16) {

                hostId = 0;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() < 24) {


                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));

                if (pVm.getId() == 15) {

                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                } else {

                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());

                    if (pVm.getId() >= 20)
                        Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                    else
                        Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }

            } else {

                if (pVm.getId() == 26 || pVm.getId() == 27)
                    hostId = 1;

                else
                    hostId = 2;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() < 28)
                    Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                else
                    Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testSimulationThreeHostsRuntime6() {

        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.5, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.4, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2.8, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.8, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.4, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(31, datacenter.getVmsRunning().size());

        int hostId;

        for (Vm vm :
                vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 9)
                hostId = 1;
            else
                hostId = 0;

            if (pVm.getId() <= 2 || pVm.getId() == 9) {

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));

            } else {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
            }

            Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() == 9)
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
        }

        testVmsP1StatisticsTime5And6();

        testVmsP2StatisticsTime6And7();
    }

    private void testVmsP2StatisticsTime6And7() {

        int hostId;

        for (Vm vm :
                vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 33) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

                if (pVm.getId() == 32) {
                    Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());

                } else {
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }

            } else if (pVm.getId() < 40) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

                if (pVm.getId() == 33)
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());
                else
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());


                if (pVm.getId() < 32)
                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                else
                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());


                if (pVm.getId() == 32)
                    Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());

                else
                    Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));

                if (pVm.getId() <= 45)
                    hostId = 2;

                else if (pVm.getId() <= 52)
                    hostId = 0;

                else
                    hostId = 1;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(6.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }
    }

    private void testSimulationThreeHostsRuntime7() {

        Assert.assertEquals(1.9, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.9, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.9, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2.3, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.9, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(26, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime7To9();

        int hostId;

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;


            if (pVm.getId() < 24) {

                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));

                if (pVm.getId() == 15 || pVm.getId() == 16) {

                    Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());

                } else {

                    Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }

                if (pVm.getId() >= 20) {
                    Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                } else {
                    Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                }

                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else {

                if (pVm.getId() == 26 || pVm.getId() == 27)
                    hostId = 1;

                else
                    hostId = 2;

                Assert.assertEquals(hostId, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

                if (pVm.getId() < 28)
                    Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
                else
                    Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            }
        }

        testVmsP2StatisticsTime6And7();
    }

    private void testVmsP0StatisticsTime7To9() {
        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 9)
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            Assert.assertNull(pVm.getHost());
            Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
            Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));
            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());
        }
    }

    private void testSimulationThreeHostsRuntime8() {

        Assert.assertEquals(3.3, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host1.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(3.3, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host2.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(2.3, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.3, host3.getAvailableMipsByPriority(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2.3, host3.getAvailableMipsByPriority(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2.3, host3.getAvailableMipsByPriority(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(2, datacenter.getVmsRunning().size());

        testVmsP0StatisticsTime7To9();

        for (Vm vm :
                vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 28 || pVm.getId() == 29) {
                Assert.assertEquals(2, pVm.getHost().getId());
                Assert.assertTrue(datacenter.getVmsRunning().contains(pVm));

            } else {
                Assert.assertNull(pVm.getHost());
                Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
            }

            if (pVm.getId() == 15 || pVm.getId() == 16) {

                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            } else {

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }

            if (pVm.getId() >= 28)
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            else if (pVm.getId() >= 24)
                Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            else if (pVm.getId() >= 20)
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            else
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
        }

        testVmsP2StatisticsTime8And9();

    }

    private void testVmsP2StatisticsTime8And9() {

        for (Vm vm :
                vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            Assert.assertNull(pVm.getHost());
            Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));
            Assert.assertFalse(datacenter.getVmsForScheduling().contains(pVm));

            if (pVm.getId() == 32 || pVm.getId() == 33)
                Assert.assertEquals(1, pVm.getNumberOfMigrations());
            else
                Assert.assertEquals(0, pVm.getNumberOfMigrations());


            if (pVm.getId() < 32 || pVm.getId() >= 40)
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
            else
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());


            if (pVm.getId() == 32)
                Assert.assertEquals(1, pVm.getNumberOfBackfillingChoice());
            else
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            if (pVm.getId() < 40)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else
                Assert.assertEquals(6.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

        }
    }

    private void testSimulationThreeHostsRuntime9() {

        Assert.assertEquals(hostCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        testVmsP0StatisticsTime7To9();

        for (Vm vm :
                vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            Assert.assertNull(pVm.getHost());
            Assert.assertFalse(datacenter.getVmsRunning().contains(pVm));

            if (pVm.getId() == 15 || pVm.getId() == 16) {

                Assert.assertEquals(1, pVm.getNumberOfPreemptions());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            } else {

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            }

            if (pVm.getId() >= 28)
                Assert.assertEquals(5.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            else if (pVm.getId() >= 24)
                Assert.assertEquals(4.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            else if (pVm.getId() >= 20)
                Assert.assertEquals(1.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            else
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
        }

        testVmsP2StatisticsTime8And9();
    }

    private void verifyAvailabilityOfThreeHosts() {

        double finishedTime;

        for (Vm vm : vmsP0) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 3 || pVm.getId() == 9)
                finishedTime = 7.0;
            else
                finishedTime = 6.0;

            Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
        }

        for (Vm vm : vmsP1) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() == 15) {
                finishedTime = 5.0;
                Assert.assertEquals(0.8, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 16) {
                finishedTime = 7.0;
                Assert.assertEquals(0.571428, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() <= 23) {

                if (pVm.getId() <= 19)
                    finishedTime = 4.0;
                else
                    finishedTime = 5.0;

                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() <= 27) {
                finishedTime = 8.0;
                Assert.assertEquals(0.571428, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else {
                finishedTime = 9.0;
                Assert.assertEquals(0.5, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            }
        }

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 32) {
                finishedTime = 2.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() == 32) {
                finishedTime = 5.0;
                Assert.assertEquals(0.4, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else if (pVm.getId() <= 39){
                finishedTime = 6.0;
                Assert.assertEquals(0.333333, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else {
                finishedTime = 8.0;

                if (pVm.getId() <= 49)
                    Assert.assertEquals(0.285714, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                else
                    Assert.assertEquals(0.333333, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            }
        }
    }
}
