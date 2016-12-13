package org.cloudbus.cloudsim.preemption;

import java.util.*;
import java.io.File;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.hostselection.WorstFitMipsBasedHostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.WorstFitPriorityBasedVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Created by Alessandro Lia Fook Santos and Joao Victor Mafra on 01/11/16.
 */
public class SystemTest {

    private static final double ACCEPTABLE_DIFFERENCE = 0.000001;
    private double hostCapacity;
    private Properties properties;

    private PreemptiveDatacenter datacenter;
    private SimEvent event;
    private PreemptiveHost host;
    private SimulationTimeUtil timeUtil;
    private PreemptableVmAllocationPolicy preemptableVmAllocationPolicy;
    private DatacenterCharacteristics characteristics;

    private int NUMBER_OF_VMS;
    private List<Vm> vmP0S0;
    private List<Vm> vmP1S0;
    private List<Vm> vmP2S0;
    private List<Vm> vmP0S1;

    private String datacenterOutputFile;
    private String datacenterInputFile;
    private String datacenterInputUrl;
    private String datacenterOutputUrl;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {

        Log.disable();

        event = Mockito.mock(SimEvent.class);

        int num_user = 1; // number of grid users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);


        // inicializating the lists of vms
        NUMBER_OF_VMS = 6603;
        vmP0S0 = new ArrayList<>(NUMBER_OF_VMS);
        vmP1S0 = new ArrayList<>(NUMBER_OF_VMS);
        vmP2S0 = new ArrayList<>(NUMBER_OF_VMS);
        vmP0S1 = new ArrayList<>(NUMBER_OF_VMS);

        // mocking properties to config simulation
        datacenterInputFile = "new_trace_test.sqlite3";
        datacenterInputUrl = "jdbc:sqlite:" + datacenterInputFile;

        datacenterOutputFile = "outputUtilizationTest.sqlite3";
        datacenterOutputUrl = "jdbc:sqlite:" + datacenterOutputFile;

        properties = Mockito.mock(Properties.class);
        // time in micro
        Mockito.when(properties.getProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP)).thenReturn("3");
        Mockito.when(properties.getProperty("logging")).thenReturn("yes");
        Mockito.when(properties.getProperty("input_trace_database_url")).thenReturn(datacenterInputUrl);
        Mockito.when(properties.getProperty("loading_interval_size")).thenReturn("300000000");
        Mockito.when(properties.getProperty("storing_interval_size")).thenReturn("240000000");
        Mockito.when(properties.getProperty("output_tasks_database_url")).thenReturn(datacenterOutputUrl);
        Mockito.when(properties.getProperty("utilization_database_url")).thenReturn(datacenterOutputUrl);
        Mockito.when(properties.getProperty("utilization_storing_interval_size")).thenReturn("480000000");
        Mockito.when(properties.getProperty("datacenter_database_url")).thenReturn(datacenterOutputUrl);
        Mockito.when(properties.getProperty("collect_datacenter_summary_info")).thenReturn("yes");
        Mockito.when(properties.getProperty("datacenter_storing_interval_size")).thenReturn("300000000");
        Mockito.when(properties.getProperty("datacenter_collect_info_interval_size")).thenReturn("240000000");
        Mockito.when(properties.getProperty("make_checkpoint")).thenReturn("no");
        Mockito.when(properties.getProperty("checkpoint_interval_size")).thenReturn("300000000");
        Mockito.when(properties.getProperty("checkpoint_dir")).thenReturn(datacenterOutputUrl);
        Mockito.when(properties.getProperty("preemption_policy_class")).thenReturn("org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy");


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
                                                new LinkedList<>(), 0, properties);

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

        Mockito.when(properties.getProperty("number_of_hosts")).thenReturn("1");
        Mockito.when(properties.getProperty("total_cpu_capacity")).thenReturn("6603.25");

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

    @Test
    public void testSystemMultipleHostWithTrace(){
        Mockito.when(properties.getProperty("number_of_hosts")).thenReturn("3");
        Mockito.when(properties.getProperty("total_cpu_capacity")).thenReturn("6603");

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

    //TODO processBackfiling is called more than once at same time because of method processEventForAllVms()
//    @Test
//    //testing the operation of the system for a single host
//    public void testSystemSingleHost() {
//
//        datacenter.getVmAllocationPolicy().setSimulationTimeUtil(timeUtil);
//
//        //asserting host on data center and host total capacity
//        Assert.assertEquals(host, datacenter.getHostList().get(0));
//        Assert.assertEquals(hostCapacity, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
//
//        // creating vms model P0S0, total of vms 6603
//        // with cpu total requisition of 3301.5
//        populateVmLists();
//
//        //allocating vms with submit time 0
//        executingSimularionRuntime0();
//
//        //allocating vms with submit time 1
//        executingSimulationRuntime1();
//
//        //executing simulation at runtime 2
//        executingSimulationRuntime2();
//
//        //executing simulation to verify preemption and running of vms through running time of simulation
//
//        executingSimulationRuntime3();
//        executingSimulationRuntime4();
//        executingSimulationRuntime5();
//        executingSimulationRuntime6();
//        executingSimulationRuntime7();
//        executingSimulationRuntime8();
//
//        verifyAvailabilityOfSingleHost();
//    }

    private void executingSimulationRuntime8() {
        // passing time to 8
        Mockito.when(timeUtil.clock()).thenReturn(8.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        // finishing vms of priority 0, submit time 0, runtime 8 that are finished in simulation time 7

        processEventForAllVms();

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


    private void executingSimulationRuntime7() {
        // passing time to 7
        Mockito.when(timeUtil.clock()).thenReturn(7.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        // finishing vms of priority 1, submit time 0, runtime 5

        processEventForAllVms();

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

    private void executingSimulationRuntime6() {
        // passing time to 6
        Mockito.when(timeUtil.clock()).thenReturn(6.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        // finishing the last one VM with priority 2

        processEventForAllVms();

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

    private void executingSimulationRuntime5() {
        // passing time to 5
        Mockito.when(timeUtil.clock()).thenReturn(5.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);


        // finishing vms of priority 2, submit time 0, runtime 2 and
        // finishing vms of priority 0, submit time 1, runtime 2
        // finishing vm id 6603 priority 1, submit time 5, runtime 5

        processEventForAllVms();

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

    private void executingSimulationRuntime4() {
        // passing time to 4
        Mockito.when(timeUtil.clock()).thenReturn(4.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        // finishing vms of priority 2, submit time 0, and runtime 2 that are finished at time 4

        processEventForAllVms();

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

    private void executingSimulationRuntime3() {
        // passing time to 3
        Mockito.when(timeUtil.clock()).thenReturn(3.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);


        // finishing vms of priority 0, submit time 1, and runtime 2, because the runtime is completed at time 3
        processEventForAllVms();

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

    private void executingSimulationRuntime2() {
        testNumberOfPreemptionsAndBackfillingOfSingleHostTimeLessThan3();
    }

    private void executingSimulationRuntime1() {
        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0
        Mockito.when(timeUtil.clock()).thenReturn(1.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);


        //allocate 6603 vms of priority 0, submit time 1, and Cpu requisition of 0.6
        //with total requested Cpu equals 3961.8
        // P0_0 = allocated / P0_1 = 5502 allocated / P1_0 = 1 allocated
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            Mockito.when(event.getData()).thenReturn(vmP0S1.get(i));
            datacenter.processEvent(event);
        }

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

    private void executingSimularionRuntime0() {
        // start time on 0 and mock the hostSelector to return desired host
        Mockito.when(timeUtil.clock()).thenReturn(0d);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);

        //allocate 6603 vms os priority 0, submit time 0, and Cpu requisition of 0.5
        //with total requested Cpu equals 3301.5
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            Mockito.when(event.getData()).thenReturn(vmP0S0.get(i));
            datacenter.processEvent(event);
        }

        //testing capacity of host
        Assert.assertEquals(hostCapacity - 3301.5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 3301.5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 3301.5, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 3301.5, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(NUMBER_OF_VMS, datacenter.getVmsRunning().size());

        //allocate 6603 vms os priority 1, submit time 0, and Cpu requisition of 0.3
        //with total requested Cpu equals 1980.9

        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            Mockito.when(event.getData()).thenReturn(vmP1S0.get(i));
            datacenter.processEvent(event);
        }

        //testing capacity of host
        Assert.assertEquals(hostCapacity - 5282.4, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 3301.5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5282.4, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 5282.4, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(2 * NUMBER_OF_VMS, datacenter.getVmsRunning().size());

        //allocate 6603 vms of priority 2, submit time 0, and Cpu requisition of 0.2
        //with total requested Cpu equals 1320.6

        for (int i = 0; i < NUMBER_OF_VMS; i++) {
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

//    @Test
//    // testing the operation of the system for more than one host
//    public void testSystemMultipleHost() {
//
//        hostCapacity = 2201;
//
//        List<Host> hostList = new ArrayList<>();
//        List<Pe> peList1 = new ArrayList<>();
//        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));
//
//        PreemptiveHost host1 = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
//                peList1), new FCFSBasedPreemptionPolicy(properties));
//        PreemptiveHost host2 = new PreemptiveHost(2, peList1, new VmSchedulerMipsBased(
//                peList1), new FCFSBasedPreemptionPolicy(properties));
//        PreemptiveHost host3 = new PreemptiveHost(3, peList1, new VmSchedulerMipsBased(
//                peList1), new FCFSBasedPreemptionPolicy(properties));
//
//        hostList.add(host1);
//        hostList.add(host2);
//        hostList.add(host3);
//
//        List<PreemptiveHost> preemptiveHostList = new ArrayList<>();
//        for (Host host : hostList) {
//            preemptiveHostList.add((PreemptiveHost) host);
//        }
//
//        preemptableVmAllocationPolicy = new WorstFitPriorityBasedVmAllocationPolicy(preemptiveHostList);
//
//        datacenter.setVmAllocationPolicy(preemptableVmAllocationPolicy);
//
//        Mockito.when(characteristics.getHostList()).thenReturn(hostList);
//
//        datacenter.getVmAllocationPolicy().setSimulationTimeUtil(timeUtil);
//
//        //asserting hosts in datacenter and host total capacity
//        Assert.assertEquals(host1.getId(), datacenter.getHostList().get(0).getId());
//        Assert.assertEquals(host2.getId(), datacenter.getHostList().get(1).getId());
//        Assert.assertEquals(host3.getId(), datacenter.getHostList().get(2).getId());
//
//        for (int i = 0; i < 3; i++) {
//            Assert.assertEquals(hostCapacity, datacenter.getHostList().get(i).getAvailableMips(), ACCEPTABLE_DIFFERENCE);
//        }
//
//        // creating vms model P0S0, total of vms 6603
//        // with cpu total requisition of 3301.5
//
//        populateVmLists();
//
//        //allocating vms with submit time 0
//        executingSimulationMultipleHostsRuntime0();
//
//        //allocating vms with submit time 1
//        executingSimulationMultipleHostRuntime1();
//
//        //executing simulation at runtime 2
//        executingSimulationMultipleHostsRuntime2();
//
//        //executing simulation to verify preemption and running of vms through running time of simulation
//
//        executingSimulationMultipleHostsRuntime3();
//        executingSimulationMultipleHostsRuntime4();
//        executingSimulationMultipleHostsRuntime5();
//        executingSimulationMultipleHostsRuntime6();
//        executingSimulationMultipleHostsRuntime7();
//        executingSimulationMultipleHostsRuntime8();
//
//        // verify expected availability for the vms
//        verifyAvailabilityOfMultipleHosts();
//    }

    private void executingSimulationMultipleHostsRuntime8() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        double capacityTotal = host1.getTotalMips();

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0
        Mockito.when(timeUtil.clock()).thenReturn(8.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        //try to destroy any vm
        processEventForAllVms();

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
        Mockito.when(timeUtil.clock()).thenReturn(7.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        //try to destroy any vm
        processEventForAllVms();

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
        Mockito.when(timeUtil.clock()).thenReturn(6.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        //try to destroy any vm
        processEventForAllVms();

        //testing capacity of host
        Assert.assertEquals(capacityTotal - 1761.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.5, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1100.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1761.1, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.5, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1761.1, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.5, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

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

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0
        Mockito.when(timeUtil.clock()).thenReturn(5.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        //try to destroy any vm
        processEventForAllVms();

        //testing capacity of host
        Assert.assertEquals(capacityTotal - 1761.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1761.2, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1761.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1100.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1100.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1761.1, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.8, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1760.5, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1761.1, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1761.2, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1761.1, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(13211, datacenter.getVmsRunning().size());

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime4() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        double capacityTotal = host1.getTotalMips();

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0
        Mockito.when(timeUtil.clock()).thenReturn(4.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        //try to destroy any vm
        processEventForAllVms();

        //testing capacity of host
        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1320.7, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1320.7, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1320.7, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1981.3, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1981, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1980.7, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(5, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(17606, datacenter.getVmsRunning().size());

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime3() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        double capacityTotal = host1.getTotalMips();

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0
        Mockito.when(timeUtil.clock()).thenReturn(3.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        //try to destroy any vm
        processEventForAllVms();

        //testing capacity of host
        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1320.7, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1320.7, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1320.7, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(capacityTotal - 1981.3, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1981, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(capacityTotal - 1980.7, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.1, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(3304, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(17606, datacenter.getVmsRunning().size());

        verifyNumberOfPreemptionAndBackfillingOfMultipleHostsTimeGreaterThan1();
    }

    private void executingSimulationMultipleHostsRuntime2() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0
        Mockito.when(timeUtil.clock()).thenReturn(2.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY);

        //try to destroy any vm
        processEventForAllVms();

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

        // passing time to 1 and allocate all vms of submit time equals 1 and priority 0
        Mockito.when(timeUtil.clock()).thenReturn(1.0);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);


        //allocate 6603 vms of priority 0, submit time 1, and Cpu requisition of 0.6
        //with total requested Cpu equals 3961.8
        // P0_0 = allocated / P0_1 = 5502 allocated / P1_0 = 1 allocated
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            Mockito.when(event.getData()).thenReturn(vmP0S1.get(i));
            datacenter.processEvent(event);
        }

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

        // start time on 0 and mock the hostSelector to return desired host
        Mockito.when(timeUtil.clock()).thenReturn(0d);
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);

        //allocate 6603 vms os priority 0, submit time 0, and Cpu requisition of 0.5
        //with total requested Cpu equals 3301.5
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            Mockito.when(event.getData()).thenReturn(vmP0S0.get(i));
            datacenter.processEvent(event);
        }

        //testing capacity of each host
        Assert.assertEquals(hostCapacity - 1100.5, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1100.5, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1100.5, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(NUMBER_OF_VMS, datacenter.getVmsRunning().size());


        //allocate 6603 vms os priority 1, submit time 0, and Cpu requisition of 0.3
        //with total requested Cpu equals 1980.9
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            Mockito.when(event.getData()).thenReturn(vmP1S0.get(i));
            datacenter.processEvent(event);
        }

        //testing capacity of each host
        Assert.assertEquals(hostCapacity - 1760.8, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1760.8, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1760.8, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1760.8, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1760.8, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1760.8, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1760.8, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1100.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1760.8, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1760.8, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());
        Assert.assertEquals(NUMBER_OF_VMS * 2, datacenter.getVmsRunning().size());

        //allocate 6603 vms of priority 2, submit time 0, and Cpu requisition of 0.2
        //with total requested Cpu equals 1320.6

        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            Mockito.when(event.getData()).thenReturn(vmP2S0.get(i));
            datacenter.processEvent(event);
        }

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
        for (int i = 0; i < 3299; i++) {
            PreemptableVm vm = (PreemptableVm) vmP2S0.get(i);
            Assert.assertEquals(0.5, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }


        finishTime = 5.0;
        for (int i = 3299; i < 6598; i++) {
            PreemptableVm vm = (PreemptableVm) vmP2S0.get(i);
            Assert.assertEquals(0.4, vm.getRuntime() / (finishTime - vm.getSubmitTime()), ACCEPTABLE_DIFFERENCE_FOR_AVAILABILITY);
        }

        finishTime = 6.0;
        for (int i = 6598; i < NUMBER_OF_VMS; i++) {
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

            //TODO This VM doesn't suffer backfilling anymore. Why?
            if (task.getTaskId() == 13207) { // the only vms that are chosen from backfilling in time 3

                Assert.assertEquals(1, task.getNumberOfBackfillingChoices());
            }

            else {

                Assert.assertEquals(0, task.getNumberOfBackfillingChoices());
            }
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
                // 3299 vms with priority 2 had availability = 0.5, 3299 had avail = 0.4 and 5 had avail = 0.33
                if (task.getTaskId() < 16506) { // the group of vms that return to running in time 3
                    Assert.assertEquals(0.5, avail, ACCEPTABLE_DIFFERENCE);

                } else if (task.getTaskId() < 19805) { // the group of vms that return to running in time 4
                    // TODO VM 16506 has availability = 0.5 now. Why?
                    Assert.assertEquals(0.4, avail, ACCEPTABLE_DIFFERENCE);

                } else { // the group of vms that return to running in time 5
                    Assert.assertEquals(0.333333, avail, ACCEPTABLE_DIFFERENCE);
                }

                Assert.assertEquals(task.getNumberOfPreemptions(), 1);
            }


            Assert.assertEquals(0, task.getNumberOfBackfillingChoices());
        }

    }
}
