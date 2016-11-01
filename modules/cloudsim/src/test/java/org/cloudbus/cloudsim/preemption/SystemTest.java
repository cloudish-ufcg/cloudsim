package org.cloudbus.cloudsim.preemption;

import java.util.*;
import java.io.File;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.datastore.DatacenterUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.HostUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.PreemptableVmDataStore;
import org.cloudbus.cloudsim.preemption.policies.hostselection.HostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.hostselection.WorstFitMipsBasedHostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.Preemptable;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
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

    private PreemptiveDatacenter datacenter;
    private SimEvent event;
    private PreemptiveHost host;
    private SimulationTimeUtil timeUtil;
    private HostSelectionPolicy hostSelector;
    private PreemptableVmAllocationPolicy preemptableVmAllocationPolicy;
    private String datacenterFile;
    private String datacenterUrl;
    private final double HOST_CAPACITY = 6603.25;

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
        peList1.add(new Pe(0, new PeProvisionerSimple(HOST_CAPACITY)));

        host = new PreemptiveHost(1, peList1, new VmSchedulerMipsBased(
                peList1), 3);
        hostList.add(host);

        // mocking
        DatacenterCharacteristics characteristics = Mockito.mock(DatacenterCharacteristics.class);
        Mockito.when(characteristics.getHostList()).thenReturn(hostList);

        Mockito.when(characteristics.getNumberOfPes()).thenReturn(1);

        timeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(timeUtil.clock()).thenReturn(0d);

        hostSelector = new WorstFitMipsBasedHostSelectionPolicy();

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
    public void testSystemSingleHost() {

        Log.disable();

        datacenter.getVmAllocationPolicy().setSimulationTimeUtil(timeUtil);

        //asserting host on data center and host total capacity
        Assert.assertEquals(host, datacenter.getHostList().get(0));
        Assert.assertEquals(HOST_CAPACITY, host.getAvailableMips(), ACCEPTABLE_DIFFERENCE);

        // creating vms model P0S0, total of vms 6603
        // with cpu total requisition of 3301.5

        int numberOfVms = 6603;
        List<Vm> vmP0S0 = new ArrayList<>(numberOfVms);
        List<Vm> vmP1S0 = new ArrayList<>(numberOfVms);
        List<Vm> vmP2S0 = new ArrayList<>(numberOfVms);
        List<Vm> vmP0S1 = new ArrayList<>(numberOfVms);

        populateVmLists(numberOfVms, vmP0S0, vmP1S0, vmP2S0, vmP0S1);

        //allocating vms with submit time 0
        executingSimularionRuntime0(ACCEPTABLE_DIFFERENCE, HOST_CAPACITY, numberOfVms, vmP0S0, vmP1S0, vmP2S0);

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

}
