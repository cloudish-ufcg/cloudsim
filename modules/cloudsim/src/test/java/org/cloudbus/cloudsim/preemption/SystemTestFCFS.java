package org.cloudbus.cloudsim.preemption;

import java.util.*;
import java.io.File;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.WorstFitAvailabilityAwareVmAllocationPolicy;
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
    private SimulationTimeUtil timeUtil;
    private PreemptableVmAllocationPolicy preemptableVmAllocationPolicy;
    private DatacenterCharacteristics characteristics;

    // trace with 6603 vms for each priority
    //----------------------------------------------------------------------
    private int NUMBER_OF_VMS;
    private List<Vm> vmP0S0;
    private List<Vm> vmP1S0;
    private List<Vm> vmP2S0;
    private List<Vm> vmP0S1;
    // ---------------------------------------------------------------------


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

        // trace with 6603 vms for each priority
        //----------------------------------------------------------------------
        // inicializating the lists of vms
        NUMBER_OF_VMS = 6603;
        vmP0S0 = new ArrayList<>(NUMBER_OF_VMS);
        vmP1S0 = new ArrayList<>(NUMBER_OF_VMS);
        vmP2S0 = new ArrayList<>(NUMBER_OF_VMS);
        vmP0S1 = new ArrayList<>(NUMBER_OF_VMS);
        //----------------------------------------------------------------------


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
        hostCapacity = 6603.25;
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


    @Test
    public void testSystemSingleHostWithTrace() {

        properties.setProperty("number_of_hosts", "1");
        properties.setProperty("total_cpu_capacity", "6603.25");

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);

        PreemptiveDatacenter datacenter0 = createGoogleDatacenter("cloud-0", properties);
        TraceDatacenterBroker broker = createGoogleTraceBroker("Google_Broker_0", properties);

        CloudSim.startSimulation();
        List<TaskState> listOfStoredTasks = broker.getStoredTasks();
        verifyResultsSingleHost(listOfStoredTasks);
    }

    //testing the operation of the system for a single host
    //TODO processBackfiling is called more than once at same time because of method processEventForAllVms()

    @Test
    public void testSystemSingleHost() {

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        CloudSim.runStart();

        // creating data center
        try {
            datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                    new LinkedList<Storage>(), 0, properties);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //asserting host on data center and host total capacity
        Assert.assertEquals(host, datacenter.getHostList().get(0));
        Assert.assertEquals(hostCapacity, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        // creating vms model P0S0, total of vms 6603
        // with cpu total requisition of 3301.5
        populateVmLists();

        submitEvents();
        CloudSim.runClockTick();


        advanceTime(0.0);
        //allocating vms with submit time 0
        testSimulationRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        testSimulationRuntime1();

        advanceTime(2.0);
        //       executing simulation at runtime 2
        testSimulationRuntime2();


        advanceTime(3.0);
//        executing simulation to verify preemption and running of vms through running time of simulation
        testSimulationRuntime3();

        advanceTime(4.0);
        testSimulationRuntime4();

        advanceTime(5.0);
        testSimulationRuntime5();

        advanceTime(6.0);
        testSimulationRuntime6();

        advanceTime(7.0);
        testSimulationRuntime7();

        advanceTime(8.0);
        testSimulationRuntime8();

        verifyAvailabilityOfSingleHost();
    }

    private void advanceTime(double time) {

        while (CloudSim.clock() < time) {
            CloudSim.runClockTick();
        }

        if (CloudSim.clock() == time)
            CloudSim.runClockTick();
    }

    private void submitEvents() {
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            CloudSim.send(datacenter.getId(), datacenter.getId(), 0d, CloudSimTags.VM_CREATE, vmP0S0.get(i));
            CloudSim.send(datacenter.getId(), datacenter.getId(), 0d, CloudSimTags.VM_CREATE, vmP1S0.get(i));
            CloudSim.send(datacenter.getId(), datacenter.getId(), 0d, CloudSimTags.VM_CREATE, vmP2S0.get(i));
            CloudSim.send(datacenter.getId(), datacenter.getId(), 1d, CloudSimTags.VM_CREATE, vmP0S1.get(i));
        }
    }

    @Test
    public void testSystemMultipleHostWithTrace() {
        properties.setProperty("number_of_hosts", "3");
        properties.setProperty("total_cpu_capacity", "6603");

        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);

        PreemptiveDatacenter datacenter0 = createGoogleDatacenter("cloud-0", properties);
        TraceDatacenterBroker broker = createGoogleTraceBroker("Google_Broker_0", properties);

        CloudSim.startSimulation();
        List<TaskState> listOfStoredTasks = broker.getStoredTasks();

        verifyResultsMultipleHosts(listOfStoredTasks);

    }

    private void testSimulationRuntime8() {
        // passing time to 8
        Mockito.when(timeUtil.clock()).thenReturn(8.0);

        // finishing vms of priority 0, submit time 0, runtime 8 that are finished in simulation time 7

        CloudSim.runClockTick();

        //testing new available considering the end of 6603 vms described before

        Assert.assertEquals(6603.25, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(6603.25, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(6603.25, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(6603.25, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertTrue(datacenter.getVmsRunning().isEmpty());

        testNumberOfPreemptionsAndBackfillingOfSingleHost();
    }


    private void testSimulationRuntime7() {

        //testing new available considering the end of vms described above
        Assert.assertEquals(3301.75, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(6603, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingOfSingleHost();
    }

    private void testSimulationRuntime6() {

        //testing new available considering the end of vms described above
        Assert.assertEquals(1321.15, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1321.15, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1321.15, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(6603 + 6602, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingOfSingleHost();
    }

    private void testSimulationRuntime5() {

        // finishing vms of priority 2, submit time 0, runtime 2 and
        // finishing vms of priority 0, submit time 1, runtime 2
        // finishing vm id 6603 priority 1, submit time 5, runtime 5

        //testing new available considering the end of vms described before, and reallocate of
        /*after deallocate 3301 vms of vmP2S0 available mips are 660.25
        * after deallocate 1101 vms of P0S1 available mips are 1320.85
		* after allocate 1 vm of vmP2S0, the available mips are 660.05
		* */
        Assert.assertEquals(1320.95, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1321.15, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1320.95, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists consider the ending of vms and reallocating as described before
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(13206, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingOfSingleHost();
    }

    private void testSimulationRuntime4() {

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

        testNumberOfPreemptionsAndBackfillingOfSingleHost();
    }

    private void testSimulationRuntime3() {

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

        testNumberOfPreemptionsAndBackfillingOfSingleHost();
    }

    private void testSimulationRuntime2() {
        testNumberOfPreemptionsAndBackfillingOfSingleHostTimeLessThan3();
    }

    private void testSimulationRuntime1() {

        //testing capacity of host
        //TODO discuss about imprecision on results
        //TODO results can be different because of backfilling and vms of priority 1 and 2 can not be preempted reallocated at same time
        Assert.assertEquals(0.25, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.55, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.25, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.25, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        //TODO results can be different because of backfilling and vms of priority 1 and 2 can not be preempted reallocated at same time
        Assert.assertEquals(14306, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(12106, datacenter.getVmsRunning().size()); //12107


        testNumberOfPreemptionsAndBackfillingOfSingleHostTimeLessThan3();
    }

    private void testSimulationRuntime0() {
        // start time on 0 and mock the hostSelector to return desired host
        //testing capacity of host
        //TODO discuss about the imprecision of results (expetecd 0.25, and actual is 0.249999999186723)
        Assert.assertEquals(0.25, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3301.75, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1320.85, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.25, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(3 * NUMBER_OF_VMS, datacenter.getVmsRunning().size());


        // testing number of preemptions and number of backfilling choices for all vms
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
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

    @Test
    // testing the operation of the system for more than one host
    public void testSystemMultipleHost() {

        hostCapacity = 2201;

        List<Host> hostList = new ArrayList<>();
        List<Pe> peList1 = new ArrayList<>();
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));

        PreemptiveHost host1 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        PreemptiveHost host2 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));
        PreemptiveHost host3 = new PreemptiveHost(3, peList1, new VmSchedulerMipsBased(
                peList1), new FCFSBasedPreemptionPolicy(properties));

        hostList.add(host1);
        hostList.add(host2);
        hostList.add(host3);

        List<PreemptiveHost> preemptiveHostList = new ArrayList<>();
        for (Host host : hostList) {
            preemptiveHostList.add((PreemptiveHost) host);
        }

        preemptableVmAllocationPolicy = new WorstFitPriorityBasedVmAllocationPolicy(preemptiveHostList);

        Mockito.when(characteristics.getHostList()).thenReturn(hostList);

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        CloudSim.runStart();

        // creating data center
        try {
            datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                    new LinkedList<Storage>(), 0, properties);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //asserting hosts in datacenter and host total capacity
        Assert.assertEquals(host1.getId(), datacenter.getHostList().get(0).getId());
        Assert.assertEquals(host2.getId(), datacenter.getHostList().get(1).getId());
        Assert.assertEquals(host3.getId(), datacenter.getHostList().get(2).getId());

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(hostCapacity, datacenter.getHostList().get(i).getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        }

        // creating vms model P0S0, total of vms 6603
        // with cpu total requisition of 3301.5
        populateVmLists();

        submitEvents();
        CloudSim.runClockTick();

        advanceTime(0.0);
        //allocating vms with submit time 0
        executingSimulationMultipleHostsRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        executingSimulationMultipleHostRuntime1();

        advanceTime(2.0);
//        //executing simulation at runtime 2
        executingSimulationMultipleHostsRuntime2();

        advanceTime(3.0);
//        //executing simulation to verify preemption and running of vms through running time of simulation
        executingSimulationMultipleHostsRuntime3();

        advanceTime(4.0);
        executingSimulationMultipleHostsRuntime4();

        advanceTime(5.0);
        executingSimulationMultipleHostsRuntime5();

        advanceTime(6.0);
        executingSimulationMultipleHostsRuntime6();

        advanceTime(7.0);
        executingSimulationMultipleHostsRuntime7();

        advanceTime(8.0);
        executingSimulationMultipleHostsRuntime8();

//        // verify expected availability for the vms
        verifyAvailabilityOfMultipleHosts();
    }

    private void executingSimulationMultipleHostsRuntime8() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        double capacityTotal = host1.getTotalMips();

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0

        //testing capacity of host
        Assert.assertEquals(capacityTotal, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(0, datacenter.getVmsRunning().size());

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime7() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        double capacityTotal = host1.getTotalMips();

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0

        //testing capacity of host
        Assert.assertEquals(capacityTotal - 1100.5, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1100.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1100.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1100.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(6603, datacenter.getVmsRunning().size());

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime6() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        double capacityTotal = host1.getTotalMips();

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0
        //testing capacity of host
        Assert.assertEquals(capacityTotal - 1760.8, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1100.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1760.8, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1760.8, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(13206, datacenter.getVmsRunning().size());

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime5() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        double capacityTotal = host1.getTotalMips();


        //testing capacity of host
        Assert.assertEquals(capacityTotal - 1761, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1761, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1761, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1100.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1760.8, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1761, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1761, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1761, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(13209, datacenter.getVmsRunning().size());

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime4() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        double capacityTotal = host1.getTotalMips();

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0

        //testing capacity of host
        Assert.assertEquals(0, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1320.7, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1320.7, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1320.7, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1981, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1981, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1981, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(3, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(17607, datacenter.getVmsRunning().size());

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime3() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        double capacityTotal = host1.getTotalMips();

        //testing capacity of host
        Assert.assertEquals(0, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1320.7, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1320.7, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1320.7, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1981, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1981, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1981, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(3303, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(17607, datacenter.getVmsRunning().size());

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime2() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        //testing capacity of host
        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(14307, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(12105, datacenter.getVmsRunning().size()); //12107

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostRuntime1() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        //testing capacity of host
        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(14307, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(12105, datacenter.getVmsRunning().size()); //12107

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime0() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        //testing capacity of each host
        Assert.assertEquals(hostCapacity - 2201, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1760.8, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2201, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 2201, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1760.8, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2201, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 2201, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1760.8, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2201, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(NUMBER_OF_VMS * 3, datacenter.getVmsRunning().size());

        // testing number of preemptions and number of backfilling choices for all vms
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
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

    // Util Methods

    private void populateVmLists() {
        int vmId = 0;
        int priority = 0;
        double runtime = 8;
        double subtime = 0;
        double cpuReq = 0.5;


        for (int i = 0; i < NUMBER_OF_VMS; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP0S0.add(vm);
        }

        // creating vms model P1S0, total of vms 6603
        // with cpu total requisition of 1980.9

        priority = 1;
        runtime = 5;
        cpuReq = 0.3;


        for (int i = 0; i < NUMBER_OF_VMS; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP1S0.add(vm);
        }

        // creating vms model P2S0, total of vms 6603
        // with cpu total requisition of 1320.6

        priority = 2;
        runtime = 2;
        cpuReq = 0.2;


        for (int i = 0; i < NUMBER_OF_VMS; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP2S0.add(vm);
        }

        // creating vms model P0S1, total of vms 6603
        // with cpu total requisition of 3961.8

        priority = 0;
        runtime = 2;
        subtime = 1;
        cpuReq = 0.6;


        for (int i = 0; i < NUMBER_OF_VMS; i++) {

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, subtime, priority, runtime);
            vmP0S1.add(vm);
        }
    }

    private void processEventForAllVms() {
        for (int i = 0; i < NUMBER_OF_VMS; i++) {

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

    // asserting that just VMP10 id 6603 and VMP20 id 13206 were chose to backfilling once
    private void testNumberOfPreemptionsAndBackfillingOfSingleHost() {
        for (int i = 0; i < NUMBER_OF_VMS; i++) {

            PreemptableVm actualVMP00 = (PreemptableVm) vmP0S0.get(i);
            PreemptableVm actualVMP10 = (PreemptableVm) vmP1S0.get(i);
            PreemptableVm actualVMP20 = (PreemptableVm) vmP2S0.get(i);
            PreemptableVm actualVMP01 = (PreemptableVm) vmP0S1.get(i);

            if (actualVMP10.getId() == 6603) {
                Assert.assertEquals(actualVMP10.getNumberOfPreemptions(), 0);
                Assert.assertEquals(actualVMP10.getNumberOfBackfillingChoice(), 0);
            } else {
                Assert.assertEquals(actualVMP10.getNumberOfPreemptions(), 1);
                Assert.assertEquals(actualVMP10.getNumberOfBackfillingChoice(), 0);
            }

            Assert.assertEquals(actualVMP20.getNumberOfPreemptions(), 1);
            Assert.assertEquals(actualVMP20.getNumberOfBackfillingChoice(), 0);

            Assert.assertEquals(actualVMP00.getNumberOfPreemptions(), 0);
            Assert.assertEquals(actualVMP01.getNumberOfPreemptions(), 0);

            Assert.assertEquals(actualVMP00.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP01.getNumberOfBackfillingChoice(), 0);
        }
    }

    // asserting that all vms with priority 1 and 2 were preempted once
    private void testNumberOfPreemptionsAndBackfillingOfSingleHostTimeLessThan3() {

        for (int i = 0; i < NUMBER_OF_VMS; i++) {

            PreemptableVm actualVMP00 = (PreemptableVm) vmP0S0.get(i);
            PreemptableVm actualVMP10 = (PreemptableVm) vmP1S0.get(i);
            PreemptableVm actualVMP20 = (PreemptableVm) vmP2S0.get(i);
            PreemptableVm actualVMP01 = (PreemptableVm) vmP0S1.get(i);

            if (actualVMP10.getId() == vmP1S0.get(0).getId()) {
                Assert.assertEquals(actualVMP10.getNumberOfPreemptions(), 0);
            } else {
                Assert.assertEquals(actualVMP10.getNumberOfPreemptions(), 1);
            }

            Assert.assertEquals(actualVMP10.getNumberOfBackfillingChoice(), 0);

            Assert.assertEquals(actualVMP00.getNumberOfPreemptions(), 0);
            Assert.assertEquals(actualVMP01.getNumberOfPreemptions(), 0);
            Assert.assertEquals(actualVMP20.getNumberOfPreemptions(), 1);

            Assert.assertEquals(actualVMP00.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP01.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP20.getNumberOfBackfillingChoice(), 0);
        }
    }

    private void verifyAvailabilityOfSingleHost() {

        double ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY = 0.01;

        double finishTime = 8.0;
        // asserting VM availability of P0S0
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            PreemptableVm vm = (PreemptableVm) vmP0S0.get(i);
            Assert.assertEquals(1, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }

        finishTime = 7.0;
        // asserting VM availability of P1S0
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            PreemptableVm vm = (PreemptableVm) vmP1S0.get(i);
            if (vm.getId() != 6603) {
                Assert.assertEquals(0.714, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
            }
        }

        // asserting VM availability for vm id 6603 with priority 1
        finishTime = 5.0;
        PreemptableVm vm_id6603 = (PreemptableVm) vmP1S0.get(0);
        Assert.assertEquals(1, vm_id6603.getRuntime() / (finishTime - vm_id6603.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);

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
        for (int i = 6602; i < NUMBER_OF_VMS; i++) {
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
        for (int i = 5502; i < NUMBER_OF_VMS; i++) {
            PreemptableVm vm = (PreemptableVm) vmP0S1.get(i);
            Assert.assertEquals(0.5, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }
    }

    private void verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1() {

        for (int i = 0; i < NUMBER_OF_VMS; i++) {

            PreemptableVm actualVMP00 = (PreemptableVm) vmP0S0.get(i);
            PreemptableVm actualVMP10 = (PreemptableVm) vmP1S0.get(i);
            PreemptableVm actualVMP20 = (PreemptableVm) vmP2S0.get(i);
            PreemptableVm actualVMP01 = (PreemptableVm) vmP0S1.get(i);

            Assert.assertEquals(actualVMP00.getNumberOfPreemptions(), 0);
            Assert.assertEquals(actualVMP10.getNumberOfPreemptions(), 1);
            Assert.assertEquals(actualVMP20.getNumberOfPreemptions(), 1);
            Assert.assertEquals(actualVMP01.getNumberOfPreemptions(), 0);

            Assert.assertEquals(actualVMP00.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP10.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP20.getNumberOfBackfillingChoice(), 0);
            Assert.assertEquals(actualVMP01.getNumberOfBackfillingChoice(), 0);
        }
    }

    private void verifyAvailabilityOfMultipleHosts() {

        double ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY = 0.01;

        double finishTime = 8.0;
        // asserting VM availability of P0S0
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            PreemptableVm vm = (PreemptableVm) vmP0S0.get(i);
            Assert.assertEquals(1, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }

        finishTime = 7.0;
        // asserting VM availability of P1S0
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            PreemptableVm vm = (PreemptableVm) vmP1S0.get(i);
            Assert.assertEquals(0.714, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }

        // asserting VM availability of P2S0
        finishTime = 4.0;
        for (int i = 0; i <= 3299; i++) {
            PreemptableVm vm = (PreemptableVm) vmP2S0.get(i);
            Assert.assertEquals(0.5, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }


        finishTime = 5.0;
        for (int i = 3300; i <= 6599; i++) {
            PreemptableVm vm = (PreemptableVm) vmP2S0.get(i);
            Assert.assertEquals(0.4, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }

        finishTime = 6.0;
        for (int i = 6600; i < NUMBER_OF_VMS; i++) {
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
        for (int i = 5502; i < NUMBER_OF_VMS; i++) {
            PreemptableVm vm = (PreemptableVm) vmP0S1.get(i);
            Assert.assertEquals(0.5, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }
    }

    private static PreemptiveDatacenter createGoogleDatacenter(String name,
                                                               Properties properties) {

        int numberOfHosts = Integer.parseInt(properties
                .getProperty("number_of_hosts"));
        double totalMipsCapacity = Double.parseDouble(properties
                .getProperty("total_cpu_capacity"));
        double mipsPerHost = totalMipsCapacity / numberOfHosts;

        Log.printLine("Creating a datacenter with " + totalMipsCapacity
                + " total capacity and " + numberOfHosts
                + " hosts, each one with " + mipsPerHost + " mips.");

        List<PreemptiveHost> hostList = new ArrayList<PreemptiveHost>();

        for (int hostId = 0; hostId < numberOfHosts; hostId++) {
            List<Pe> peList1 = new ArrayList<Pe>();

            peList1.add(new Pe(0, new PeProvisionerSimple(mipsPerHost)));

            PreemptiveHost host = new PreemptiveHost(hostId, peList1,
                    new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

            hostList.add(host);
        }

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.1; // the cost of using storage in this
        // resource
        double costPerBw = 0.1; // the cost of using bw in this resource

        // we are not adding SAN devices by now
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        PreemptiveDatacenter datacenter = null;
        try {
//
            datacenter = new PreemptiveDatacenter(name, characteristics,
                    new WorstFitPriorityBasedVmAllocationPolicy(hostList),
                    storageList, 0, properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static TraceDatacenterBroker createGoogleTraceBroker(String name,
                                                                 Properties properties) {

        TraceDatacenterBroker broker = null;
        try {
            broker = new TraceDatacenterBroker(name, properties);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    //testing statistics of vms after run the simulation
    private static void verifyResultsSingleHost(List<TaskState> listOfTasks) {

        for (TaskState task : listOfTasks) {

            double avail = task.getRuntime() / (task.getFinishTime() - task.getSubmitTime());

            if (task.getPriority() == 0) {

                Assert.assertEquals(task.getNumberOfPreemptions(), 0);

                if (task.getTaskId() < 6604 || task.getTaskId() < 25312) {
                    Assert.assertEquals(1, avail, ACCEPTABLE_DIFFERENCE);

                } else {
                    Assert.assertEquals(0.5, avail, ACCEPTABLE_DIFFERENCE);
                }


            } else if (task.getPriority() == 1) {

                if (task.getTaskId() == 6604) { // the only vm that stay in the host in runtime 1
                    Assert.assertEquals(0, task.getNumberOfPreemptions());
                    Assert.assertEquals(1, avail, ACCEPTABLE_DIFFERENCE);

                } else {

                    Assert.assertEquals(1, task.getNumberOfPreemptions());
                    Assert.assertEquals(0.714285, avail, ACCEPTABLE_DIFFERENCE);
                }

            } else {

                if (task.getTaskId() < 16508) { // the group of vms that return to running in time 3
                    Assert.assertEquals(0.5, avail, ACCEPTABLE_DIFFERENCE);

                } else if (task.getTaskId() < 19809) { // the group of vms that return to running in time 4
                    Assert.assertEquals(0.4, avail, ACCEPTABLE_DIFFERENCE);

                } else { // the group of vms that return to running in time 5
                    Assert.assertEquals(0.333333, avail, ACCEPTABLE_DIFFERENCE);
                }

                Assert.assertEquals(task.getNumberOfPreemptions(), 1);
            }

            Assert.assertEquals(0, task.getNumberOfBackfillingChoices());
        }
    }


    private static void verifyResultsMultipleHosts(List<TaskState> listOfStoredTasks) {

        for (TaskState task : listOfStoredTasks) {

            double avail = task.getRuntime() / (task.getFinishTime() - task.getSubmitTime());

            if (task.getPriority() == 0) {

                Assert.assertEquals(task.getNumberOfPreemptions(), 0);

                if (task.getTaskId() < 6604 || task.getTaskId() < 25312) {
                    Assert.assertEquals(1, avail, ACCEPTABLE_DIFFERENCE);

                } else {
                    Assert.assertEquals(0.5, avail, ACCEPTABLE_DIFFERENCE);
                }


            } else if (task.getPriority() == 1) {

                Assert.assertEquals(1, task.getNumberOfPreemptions());
                Assert.assertEquals(0.714285, avail, ACCEPTABLE_DIFFERENCE);

            } else { // priority 2
                // 3300 vms with priority 2 had availability = 0.5, 3300 had avail = 0.4 and 3 had avail = 0.33
                if (task.getTaskId() <= 16506) {// the group of vms that return to running in time 3
                    Assert.assertEquals(0.5, avail, ACCEPTABLE_DIFFERENCE);

                } else if (task.getTaskId() <= 19806) { // the group of vms that return to running in time 4
                    Assert.assertEquals(0.4, avail, ACCEPTABLE_DIFFERENCE);

                } else { // the group of vms that return to running in time 5
                    Assert.assertEquals(0.333333, avail, ACCEPTABLE_DIFFERENCE);
                }

                Assert.assertEquals(task.getNumberOfPreemptions(), 1);
            }


            Assert.assertEquals(0, task.getNumberOfBackfillingChoices());
        }
    }

    @Ignore
    @Test
    public void testSystemSingleHostWithAvailableAwarePolicy() {

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        CloudSim.runStart();

        // creating host
        List<Pe> peList1 = new ArrayList<Pe>();
        hostCapacity = 6603.25;
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));

        host = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new VmAvailabilityBasedPreemptionPolicy(properties));

        // creating list of hosts
        List<PreemptiveHost> hostList = new ArrayList<PreemptiveHost>();
        hostList.add(host);


        preemptableVmAllocationPolicy = new WorstFitAvailabilityAwareVmAllocationPolicy(hostList);

        // creating data center
        try {
            datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                    new LinkedList<Storage>(), 0, properties);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //asserting host on data center and host total capacity
        Assert.assertEquals(host, datacenter.getHostList().get(0));
        Assert.assertEquals(hostCapacity, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        // creating vms model P0S0, total of vms 6603
        // with cpu total requisition of 3301.5
        populateVmLists();

        submitEvents();
        CloudSim.runClockTick();


        advanceTime(0.0);
        //allocating vms with submit time 0
        testSimulationRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        testSimulationRuntime1();

        advanceTime(2.0);
        //       executing simulation at runtime 2
        testSimulationRuntime2();


        advanceTime(3.0);
//        executing simulation to verify preemption and running of vms through running time of simulation
        testSimulationRuntime3();

        advanceTime(4.0);
        testSimulationRuntime4();

        advanceTime(5.0);
        testSimulationRuntime5();

        advanceTime(6.0);
        testSimulationRuntime6();

        advanceTime(7.0);
        testSimulationRuntime7();

        advanceTime(8.0);
        testSimulationRuntime8();

        verifyAvailabilityOfSingleHost();
    }

    @Ignore
    @Test
    // testing the operation of the system for more than one host
    public void testSystemMultipleHostWithAvailabilitAware() {

        hostCapacity = 2201;

        List<Host> hostList = new ArrayList<>();
        List<Pe> peList1 = new ArrayList<>();
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));

        PreemptiveHost host1 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new VmAvailabilityBasedPreemptionPolicy(properties));
        PreemptiveHost host2 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(
                peList1), new VmAvailabilityBasedPreemptionPolicy(properties));
        PreemptiveHost host3 = new PreemptiveHost(3, peList1, new VmSchedulerMipsBased(
                peList1), new VmAvailabilityBasedPreemptionPolicy(properties));

        hostList.add(host1);
        hostList.add(host2);
        hostList.add(host3);

        List<PreemptiveHost> preemptiveHostList = new ArrayList<>();
        for (Host host : hostList) {
            preemptiveHostList.add((PreemptiveHost) host);
        }

        preemptableVmAllocationPolicy = new WorstFitAvailabilityAwareVmAllocationPolicy(preemptiveHostList);

        Mockito.when(characteristics.getHostList()).thenReturn(hostList);

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        CloudSim.runStart();

        // creating data center
        try {
            datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                    new LinkedList<Storage>(), 0, properties);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //asserting hosts in datacenter and host total capacity
        Assert.assertEquals(host1.getId(), datacenter.getHostList().get(0).getId());
        Assert.assertEquals(host2.getId(), datacenter.getHostList().get(1).getId());
        Assert.assertEquals(host3.getId(), datacenter.getHostList().get(2).getId());

        for (int i = 0; i < 3; i++) {
            Assert.assertEquals(hostCapacity, datacenter.getHostList().get(i).getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        }

        // creating vms model P0S0, total of vms 6603
        // with cpu total requisition of 3301.5
        populateVmLists();

        submitEvents();
        CloudSim.runClockTick();

        advanceTime(0.0);
        //allocating vms with submit time 0
        executingSimulationMultipleHostsRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        executingSimulationMultipleHostRuntime1();

        advanceTime(2.0);
//        //executing simulation at runtime 2
        executingSimulationMultipleHostsRuntime2();

        advanceTime(3.0);
//        //executing simulation to verify preemption and running of vms through running time of simulation
        executingSimulationMultipleHostsRuntime3();

        advanceTime(4.0);
        executingSimulationMultipleHostsRuntime4();

        advanceTime(5.0);
        executingSimulationMultipleHostsRuntime5();

        advanceTime(6.0);
        executingSimulationMultipleHostsRuntime6();

        advanceTime(7.0);
        executingSimulationMultipleHostsRuntime7();

        advanceTime(8.0);
        executingSimulationMultipleHostsRuntime8();

//        // verify expected availability for the vms
        verifyAvailabilityOfMultipleHosts();
    }


    @Test
    public void testSystemSingleHostFCFSNewTrace() {
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
        try {
            datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                    new LinkedList<Storage>(), 0, properties);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

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

            if(vm.getId() <= 2)
                Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if(vm.getId() == 9)
                Assert.assertEquals(1.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if(vm.getId() <= 29)
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

            if(vm.getId() <= 2)
                Assert.assertEquals(0d, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if(vm.getId() == 9)
                Assert.assertEquals(1.0, vm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if(vm.getId() <= 29)
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

            if(vm.getId() == 9)
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

            if(vm.getId() == 9 || vm.getId() >= 20 && vm.getId() < 24)
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

            if(vm.getId() == 9 || vm.getId() >= 20)
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
    public void testSystemMultipleHostFCFSNewTrace() {
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
        try {
            datacenter = new PreemptiveDatacenter("datacenter", characteristics, preemptableVmAllocationPolicy,
                    new LinkedList<Storage>(), 0, properties);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

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
        testSimulationNewTraceMultipleHostRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        testSimulationNewTraceMultipleHostRuntime1();

        advanceTime(2.0);
        //allocating vms with submit time 2
        testSimulationNewTraceMultipleHostRuntime2();

        advanceTime(3.0);
        testSimulationNewTraceMultipleHosRuntime3();

        advanceTime(4.0);
        testSimulationNewTraceMultipleHostRuntime4();

//        advanceTime(5.0);
//        testSimulationNewTraceRuntime5();
//
//        advanceTime(6.0);
//        testSimulationNewTraceRuntime6();
//
//        advanceTime(7.0);
//        testSimulationNewTraceRuntime7();
//
//        advanceTime(8.0);
//        testSimulationNewTraceRuntime8();
//
//        verifyAvailabilityOfSingleHostNewTrace();
    }

    private void testSimulationNewTraceMultipleHostRuntime0() {

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

    private void testSimulationNewTraceMultipleHostRuntime1() {

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

                    if (pVm.getId() < 40){
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

    private void testSimulationNewTraceMultipleHostRuntime2() {

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

    private void testSimulationNewTraceMultipleHosRuntime3() {

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

        testVmP0StatisticsTimeGreaterThan0();

        testVmsP1StatisticsTime1To3();
        int hostId = 1;

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            if (pVm.getId() < 40)
                Assert.assertEquals(0d, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if(pVm.getId() < 42)
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

    private void testSimulationNewTraceMultipleHostRuntime4() {

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

            } else if (pVm.getId() >= 28){

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
            else if(pVm.getId() < 42)
                Assert.assertEquals(3.0, pVm.getFirstTimeAllocated(), ACCEPTABLE_DIFFERENCE);
            else if(pVm.getId() == 42)
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
}
