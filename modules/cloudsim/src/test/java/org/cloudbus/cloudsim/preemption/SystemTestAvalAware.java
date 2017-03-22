package org.cloudbus.cloudsim.preemption;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

/**
 * Created by jvmafra on 20/03/17.
 */
public class SystemTestAvalAware {

    private static final double ACCEPTABLE_DIFFERENCE = 0.000001;
    private double hostCapacity;
    private Properties properties;

    private PreemptiveDatacenter datacenter;
    private SimEvent event;
    private PreemptiveHost host;
    private SimulationTimeUtil timeUtil;
    private PreemptableVmAllocationPolicy preemptableVmAllocationPolicy;
    private DatacenterCharacteristics characteristics;

    private int NUMBER_OF_VMS_P0 = 10;
    private int NUMBER_OF_VMS_P1 = 20;
    private int NUMBER_OF_VMS_P2 = 30;
    private List<Vm> vmsP0;
    private List<Vm> vmsP1;
    private List<Vm> vmsP2;


    private String datacenterOutputFile;
    private String datacenterInputFile;
    private String datacenterInputUrl;
    private String datacenterOutputUrl;

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
        vmsP0 = new ArrayList<>(NUMBER_OF_VMS_P0);
        vmsP1 = new ArrayList<>(NUMBER_OF_VMS_P1);
        vmsP2 = new ArrayList<>(NUMBER_OF_VMS_P2);

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

    private void advanceTime(double time) {

        while (CloudSim.clock() < time){
            CloudSim.runClockTick();
        }

        if (CloudSim.clock() == time)
            CloudSim.runClockTick();

        System.out.println("runtime " + time);
        System.out.println("waiting");
        Comparator<PreemptableVm> comparator = (PreemptableVm o1, PreemptableVm o2)->o1.getId() - o2.getId();
        ArrayList list = new ArrayList<>(datacenter.getVmsForScheduling());
        Collections.sort(list, comparator);
        System.out.println(list.toString());
        System.out.println("running");
        comparator = (PreemptableVm o1, PreemptableVm o2)->o1.getLastHostId() - o2.getLastHostId();
        list = new ArrayList<>(datacenter.getVmsRunning());
        Collections.sort(list, comparator);
        System.out.println(list.toString());
    }

    @Test
    public void testSystemSingleHostWithAvalabilityAwarePolicy() {
        Log.enable();

        // creating host
        List<Pe> peList1 = new ArrayList<Pe>();
        peList1.add(new Pe(0, new PeProvisionerSimple(hostCapacity)));
        properties.setProperty("preemption_policy_class",
                "org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy");

        host = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), new VmAvailabilityBasedPreemptionPolicy(properties));

        // creating list of hosts
        List<PreemptiveHost> hostList = new ArrayList<PreemptiveHost>();
        hostList.add(host);


        preemptableVmAllocationPolicy = new WorstFitAvailabilityAwareVmAllocationPolicy(hostList);

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
        testSimulationRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        testSimulationRuntime1();

        advanceTime(2.0);
        //allocating vms with submit time 2
        testSimulationRuntime2();

        advanceTime(3.0);
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

    @Test
    public void testSystemMultipleHostsWithAvailabilityAwarePolicy() {
        Log.enable();

        hostCapacity = 3.3;
        properties.setProperty("preemption_policy_class",
                "org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy");

        // creating host
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
        populateVmListsNewTrace();

        submitEventsNewTrace();
        CloudSim.runClockTick();


        advanceTime(0.0);
        //allocating vms with submit time 0
        testSimulationMultipleHostsRuntime0();

        advanceTime(1.0);
        //allocating vms with submit time 1
        testSimulationMultipleHostsRuntime1();

        advanceTime(2.0);
        //allocating vms with submit time 2
        testSimulationMultipleHostsRuntime2();

        advanceTime(3.0);
        //nothing happens
        testSimulationMultipleHostsRuntime3();

        advanceTime(4.0);
        //allocating vms with submit time 1
        testSimulationMultipleHostsRuntime4();

        advanceTime(5.0);
        testSimulationMultipleHostsRuntime5();

        advanceTime(6.0);
        testSimulationMultipleHostsRuntime6();

        advanceTime(7.0);
        testSimulationMultipleHostsRuntime7();

        advanceTime(8.0);
        testSimulationMultipleHostsRuntime8();

        advanceTime(9.0);
        testSimulationMultipleHostsRuntime9();

        verifyAvailabilityOfMultipleHosts();
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

            if (i >= 3){
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

            if (i >= 10 && i <= 19){
                submitTime = 1;
            } else if (i >= 20 && i <= 29){
                submitTime = 2;
            }

            Vm vm = new PreemptableVm(vmId++, 0, cpuReq, 0, submitTime, priority, runtime);
            vmsP2.add(vm);
        }
    }

    private void testSimulationMultipleHostsRuntime0() {

        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        Assert.assertEquals(0.2, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.2, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.2 - 0.4, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 0.9, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 0.9 - 0.8, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 0.9, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 0.9 - 0.8, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(29, datacenter.getVmsRunning().size()); //12107

        testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime0();
    }

    private void testSimulationMultipleHostsRuntime1() {
        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        Assert.assertEquals(0, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 2, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 1 - 0.3, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 1.3, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.3, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.3, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.3, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.3, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(29, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(21, datacenter.getVmsRunning().size()); //12107

        testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime1();
    }

    private void testSimulationMultipleHostsRuntime2() {
        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        Assert.assertEquals(0, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 2, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 1 - 0.3, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 1.3, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.3, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.3, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.3, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.5 - 0.3, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(39, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(21, datacenter.getVmsRunning().size()); //12107

        testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime2();
    }


    private void testSimulationMultipleHostsRuntime3() {
        testSimulationMultipleHostsRuntime2();
    }

    private void testSimulationMultipleHostsRuntime4() {
        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        Assert.assertEquals(0, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 2, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 0.9, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 0.9 - 0.4, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.6, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.6 - 0.2, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.6, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.6 - 0.2, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(32, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(25, datacenter.getVmsRunning().size()); //12107

        testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime4();

    }

    private void testSimulationMultipleHostsRuntime5() {
        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        Assert.assertEquals(0, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 2, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 0.9, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2 - 0.9 - 0.4, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.6, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.6 - 0.2, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.6, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1.5 - 1.6 - 0.2, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(30, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(25, datacenter.getVmsRunning().size()); //12107

        testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime5();
    }

    private void testSimulationMultipleHostsRuntime6() {
        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        Assert.assertEquals(0.1, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 1, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1 - 1.4, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 1 - 1.4 - 0.8, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 0.5, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 2.1, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 2.1 - 0.6, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity - 0.5, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 1.6, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 1.6 - 1.2, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(13, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(30, datacenter.getVmsRunning().size()); //12107

        testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime6();
    }

    private void testSimulationMultipleHostsRuntime7() {
        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        Assert.assertEquals(1.3, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.2, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(1.2, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 2, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 1.6, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.5 - 1.6, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(28, datacenter.getVmsRunning().size()); //12107

        testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime7();
    }

    private void testSimulationMultipleHostsRuntime8() {
        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        Assert.assertEquals(2.9, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(3.1, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.4, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity - 0.2, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(3, datacenter.getVmsRunning().size());

        testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime8();
    }

    private void testSimulationMultipleHostsRuntime9() {
        PreemptiveHost host1 = (PreemptiveHost) datacenter.getHostList().get(0);
        PreemptiveHost host2 = (PreemptiveHost) datacenter.getHostList().get(1);
        PreemptiveHost host3 = (PreemptiveHost) datacenter.getHostList().get(2);

        Assert.assertEquals(hostCapacity, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host1.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host2.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(hostCapacity, host3.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size());
        Assert.assertEquals(0, datacenter.getVmsRunning().size());
    }

    private void verifyAvailabilityOfMultipleHosts(){
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

            if (pVm.getId() >= 10 && pVm.getId() <= 12) {

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                finishedTime = 4.0;
                Assert.assertEquals(1, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());


            } else if (pVm.getId() >= 13 && pVm.getId() <= 19)  {

                Assert.assertEquals(1, pVm.getNumberOfPreemptions());

                finishedTime = 7.0;
                Assert.assertEquals(0.571428571, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() >= 20 && pVm.getId() <= 21){

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                finishedTime = 5.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());

            } else if (pVm.getId() >= 22 && pVm.getId() <= 23){
                Assert.assertEquals(1, pVm.getNumberOfPreemptions());

                finishedTime = 6.0;
                Assert.assertEquals(0.8, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            }  else if (pVm.getId() >= 24 && pVm.getId() <= 27){

                Assert.assertEquals(1, pVm.getNumberOfPreemptions());

                finishedTime = 7.0;
                Assert.assertEquals(0.666666667, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(1, pVm.getNumberOfMigrations());

            } else if (pVm.getId() >= 28 && pVm.getId() <= 29){

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                finishedTime = 8.0;
                Assert.assertEquals(0.571428571, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
            } else {
                System.out.println("Some VMP1 was not verifyed");
            }



        }

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            if (pVm.getId() >= 30 && pVm.getId() <= 39) {

                Assert.assertEquals(1, pVm.getNumberOfPreemptions());

                finishedTime = 8.0;

                Assert.assertEquals(0.25, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

                if (pVm.getId() >= 30 && pVm.getId() <= 33){
                    Assert.assertEquals(1, pVm.getNumberOfMigrations());
                } else {
                    Assert.assertEquals(0, pVm.getNumberOfMigrations());
                }

            } else if (pVm.getId() >= 40 && pVm.getId() <= 43){
                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                finishedTime = 6.0;

                Assert.assertEquals(0.4, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 44 && pVm.getId() <= 49){

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                finishedTime = 8.0;

                Assert.assertEquals(0.285714286, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 50 && pVm.getId() <= 56){

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                finishedTime = 8.0;

                Assert.assertEquals(0.333333333, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else if (pVm.getId() >= 57 && pVm.getId() <= 59){

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                finishedTime = 9.0;

                Assert.assertEquals(0.285714286, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, pVm.getNumberOfMigrations());
                Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());

            } else {
                System.out.println("Some VMP2 was not verifyied");
            }
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

    private void testSimulationRuntime0() {

        Assert.assertEquals(0.5, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5.5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2.5, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.5, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(29, datacenter.getVmsRunning().size()); //12107

        testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime0();

    }

    private void testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime0() {

        for(PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, vm.getNumberOfPreemptions());
        }

    }

    private void testSimulationRuntime1() {

        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(30, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(20, datacenter.getVmsRunning().size()); //12107


        testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime1();
    }

    private void testSimulationRuntime2() {

        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(40, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(20, datacenter.getVmsRunning().size()); //12107


        testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime1();
    }

    private void testSimulationRuntime3() {

        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(40, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(20, datacenter.getVmsRunning().size()); //12107


        testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime1();
    }

    private void testSimulationRuntime4() {

        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(40, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(20, datacenter.getVmsRunning().size()); //12107


        testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime1();
    }

    private void testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime1() {

        for (PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, vm.getNumberOfPreemptions());
        }

        for (PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

            if (vm.getId() >= 10 && vm.getId() <= 19 || vm.getId() >= 30 && vm.getId() <= 39)
                Assert.assertEquals(1, vm.getNumberOfPreemptions());

            else
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
        }

    }


    private void testSimulationRuntime5() {

        for(int i = 10; i < vmsP1.size(); i++) {

            PreemptableVm vm = (PreemptableVm) vmsP1.get(i);
            Assert.assertEquals(1.0, vm.getCurrentAvailability(5.0), ACCEPTABLE_DIFFERENCE);
            Assert.assertEquals(0, vm.getNumberOfPreemptions());
        }

        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(2, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(20, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(30, datacenter.getVmsRunning().size()); //12107

        testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime5();
    }

    private void testNumberOfPreemptionsAndBackfillingOfSingleHostWithAvailAwarePolicyTime5() {

        for (PreemptableVm vm : datacenter.getVmsRunning()) {

            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

            if (vm.getId() >= 10 && vm.getId() <= 19) {
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
            }

            else {
                System.out.println(vm.getId());
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }
        }

        for (PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

            if (vm.getId() >= 30 && vm.getId() <= 39)
                Assert.assertEquals(1, vm.getNumberOfPreemptions());

            else
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
        }

    }

    private void testSimulationRuntime6() {

        for(int i = 3; i < 9; i++) {

            PreemptableVm vm = (PreemptableVm) vmsP0.get(i);
            Assert.assertEquals(1.0, vm.getCurrentAvailability(6.0), ACCEPTABLE_DIFFERENCE);
            Assert.assertEquals(0, vm.getNumberOfPreemptions());
        }

        Assert.assertEquals(0, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(8, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(5, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(5, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(39, datacenter.getVmsRunning().size()); //12107

    }

    private void testSimulationRuntime7() {

        for(int i = 0; i < 3; i++) {

            PreemptableVm vm = (PreemptableVm) vmsP0.get(i);
            Assert.assertEquals(1.0, vm.getCurrentAvailability(7.0), ACCEPTABLE_DIFFERENCE);
            Assert.assertEquals(0, vm.getNumberOfPreemptions());
        }

        PreemptableVm vm = (PreemptableVm) vmsP0.get(9);
        Assert.assertEquals(1.0, vm.getCurrentAvailability(7.0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, vm.getNumberOfPreemptions());

        for (Vm vmP2 : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vmP2;

            if (pVm.getId() >= 40 && pVm.getId() <= 49) {
                Assert.assertEquals(0.33333333, pVm.getCurrentAvailability(7), ACCEPTABLE_DIFFERENCE);
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }
        }

        Assert.assertEquals(4, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(10, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(7, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(4, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(25, datacenter.getVmsRunning().size()); //12107
    }

    private void testSimulationRuntime8() {

        Assert.assertEquals(10, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(10, host.getAvailableMipsByPriority(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(10, host.getAvailableMipsByPriority(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(10, host.getAvailableMipsByPriority(2), ACCEPTABLE_DIFFERENCE);

        //testing size of lists
        Assert.assertEquals(0, datacenter.getVmsForScheduling().size()); //14305
        Assert.assertEquals(0, datacenter.getVmsRunning().size()); //12107
    }

    private void verifyAvailabilityOfSingleHost() {


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

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() <= 19) {

                Assert.assertEquals(1, pVm.getNumberOfPreemptions());

                finishedTime = 8.0;
                Assert.assertEquals(0.5, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                finishedTime = 5.0;
                Assert.assertEquals(1.0, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
            }
        }

        for (Vm vm : vmsP2) {

            PreemptableVm pVm = (PreemptableVm) vm;

            Assert.assertEquals(0, pVm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, pVm.getNumberOfMigrations());

            if (pVm.getId() >= 40) {

                Assert.assertEquals(0, pVm.getNumberOfPreemptions());

                finishedTime = 7.0;

                if (pVm.getId() >= 50) {
                    finishedTime = 8.0;
                }
                Assert.assertEquals(0.33333333, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);

            } else {

                Assert.assertEquals(1, pVm.getNumberOfPreemptions());

                if (pVm.getId() >= 35) {
                    finishedTime = 8.0;
                    Assert.assertEquals(0.25, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                } else {
                    finishedTime = 7.0;
                    Assert.assertEquals(0.28571429, pVm.getCurrentAvailability(finishedTime), ACCEPTABLE_DIFFERENCE);
                }
            }
        }
    }



    private void testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime1() {

        for(PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, vm.getNumberOfPreemptions());
        }

        for(PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

            if (vm.getId() == 28 || vm.getId() == 29 || (vm.getId() >= 40 && vm.getId() <= 49)){
                Assert.assertEquals(0, vm.getNumberOfPreemptions());

            } else {
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
            }

        }

    }

    private void testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime2() {

        for(PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());
            Assert.assertEquals(0, vm.getNumberOfPreemptions());
        }

        for(PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

            if (vm.getId() == 28 || vm.getId() == 29 || (vm.getId() >= 40 && vm.getId() <= 59)){
                Assert.assertEquals(0, vm.getNumberOfPreemptions());

            } else {
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
            }

        }

    }

    private void testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime4() {

        for(PreemptableVm vm : datacenter.getVmsRunning()) {
            if (vm.getId() >= 13 && vm.getId() <= 19){
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }

            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

        }

        for(PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

            if ((vm.getId() >= 22 && vm.getId() <= 27) || (vm.getId() >= 30 && vm.getId() <= 39)){
                Assert.assertEquals(1, vm.getNumberOfPreemptions());

            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }

        }

    }

    private void testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime5() {

        for(PreemptableVm vm : datacenter.getVmsRunning()) {
            if ((vm.getId() >= 13 && vm.getId() <= 19) || (vm.getId() == 22 || vm.getId() == 23)){
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }

            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

        }

        for(PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

            if ((vm.getId() >= 24 && vm.getId() <= 27) || (vm.getId() >= 30 && vm.getId() <= 39)){
                Assert.assertEquals(1, vm.getNumberOfPreemptions());

            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }

        }

    }

    private void testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime6() {

        for(PreemptableVm vm : datacenter.getVmsRunning()) {
            if ((vm.getId() >= 13 && vm.getId() <= 19) || (vm.getId() >= 24 && vm.getId() <= 27)){
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }

            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

        }

        for(PreemptableVm vm : datacenter.getVmsForScheduling()) {
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

            if (vm.getId() >= 30 && vm.getId() <= 39){
                Assert.assertEquals(1, vm.getNumberOfPreemptions());

            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }

        }

    }

    private void testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime7() {

        for(PreemptableVm vm : datacenter.getVmsRunning()) {
            if ((vm.getId() >= 30 && vm.getId() <= 39)){
                Assert.assertEquals(1, vm.getNumberOfPreemptions());
            } else {
                Assert.assertEquals(0, vm.getNumberOfPreemptions());
            }

            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

        }

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());

    }

    private void testNumberOfPreemptionsAndBackfillingOfMultipleHostWithAvailAwarePolicyTime8() {

        for(PreemptableVm vm : datacenter.getVmsRunning()) {
            Assert.assertEquals(0, vm.getNumberOfPreemptions());
            Assert.assertEquals(0, vm.getNumberOfBackfillingChoice());

        }

        Assert.assertTrue(datacenter.getVmsForScheduling().isEmpty());

    }


}
