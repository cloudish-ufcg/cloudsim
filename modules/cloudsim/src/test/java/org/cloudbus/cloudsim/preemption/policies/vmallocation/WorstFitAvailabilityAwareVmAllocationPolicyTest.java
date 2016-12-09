package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.*;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Created by Alessandro Lia Fook Santos on 02/12/16.
 */
public class WorstFitAvailabilityAwareVmAllocationPolicyTest {

    private static final double HOST_CAPACITY = 0.5;
    private final double ACCEPTABLE_DIFFERENCE = 0.00001;

    private List<PreemptiveHost> hostsWithMockedPolicy;
    private List<PreemptiveHost> hostsWithNoMockedPolicy;

    private VmAvailabilityBasedPreemptionPolicy mockedPreemptionPolicy1;
    private VmAvailabilityBasedPreemptionPolicy mockedPreemptionPolicy2;
    private VmAvailabilityBasedPreemptionPolicy mockedPreemptionPolicy3;
    private VmAvailabilityBasedPreemptionPolicy mockedPreemptionPolicy4;

    private VmAvailabilityBasedPreemptionPolicy preemptionPolicy1;
    private VmAvailabilityBasedPreemptionPolicy preemptionPolicy2;
    private VmAvailabilityBasedPreemptionPolicy preemptionPolicy3;

    private PreemptiveHost hostWithMockedPolicy1;
    private PreemptiveHost hostWithMockedPolicy2;
    private PreemptiveHost hostWithMockedPolicy3;
    private PreemptiveHost hostWithMockedPolicy4;
    private PreemptableVm MockedVm;
    private PreemptableVm vm0;
    private PreemptableVm vm1;

    private WorstFitAvailabilityAwareVmAllocationPolicy allocationPolicyWithMock;
    private WorstFitAvailabilityAwareVmAllocationPolicy allocationPolicyWithNoMock;
    private Map<Integer, Double> priorityToSLOTarget;
    private SimulationTimeUtil simulationTimeUtil;
    private Properties properties;
    private PreemptiveHost host1;
    private PreemptiveHost host2;
    private PreemptiveHost host3;

    @Before
    public void setUp() throws Exception {


        // environment with VmAvailabilityBasedPreemptionPolicy

        priorityToSLOTarget = new HashMap<>();
        priorityToSLOTarget.put(0, 1.0);
        priorityToSLOTarget.put(1, 0.9);
        priorityToSLOTarget.put(2, 0.5);

        // environment with mocked entities
        mockedPreemptionPolicy1 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        Mockito.when(mockedPreemptionPolicy1.getPriorityToSLOTarget()).thenReturn(priorityToSLOTarget);
        Mockito.when(mockedPreemptionPolicy1.getNumberOfPriorities()).thenReturn(3);

        mockedPreemptionPolicy2 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        Mockito.when(mockedPreemptionPolicy2.getPriorityToSLOTarget()).thenReturn(priorityToSLOTarget);
        Mockito.when(mockedPreemptionPolicy2.getNumberOfPriorities()).thenReturn(3);

        mockedPreemptionPolicy3 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        Mockito.when(mockedPreemptionPolicy3.getPriorityToSLOTarget()).thenReturn(priorityToSLOTarget);
        Mockito.when(mockedPreemptionPolicy3.getNumberOfPriorities()).thenReturn(3);

        mockedPreemptionPolicy4 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        Mockito.when(mockedPreemptionPolicy4.getPriorityToSLOTarget()).thenReturn(priorityToSLOTarget);
        Mockito.when(mockedPreemptionPolicy4.getNumberOfPriorities()).thenReturn(3);

        hostsWithMockedPolicy = new ArrayList<>();

        List<Pe> peList1 = new ArrayList<>();

        peList1.add(new Pe(0, new PeProvisionerSimple(HOST_CAPACITY)));

        int hostId = 0;
        hostWithMockedPolicy1 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), mockedPreemptionPolicy1);

        hostWithMockedPolicy2 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), mockedPreemptionPolicy2);

        hostWithMockedPolicy3 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), mockedPreemptionPolicy3);

        hostWithMockedPolicy4 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), mockedPreemptionPolicy4);

        hostsWithMockedPolicy.add(hostWithMockedPolicy1);
        hostsWithMockedPolicy.add(hostWithMockedPolicy2);
        hostsWithMockedPolicy.add(hostWithMockedPolicy3);
        hostsWithMockedPolicy.add(hostWithMockedPolicy4);

        double cpuReq = 0.5;
        int priority = 0;

        MockedVm = Mockito.mock(PreemptableVm.class);
        Mockito.when(MockedVm.getPriority()).thenReturn(priority);
        Mockito.when(MockedVm.getMips()).thenReturn(cpuReq);

        simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);

        allocationPolicyWithMock = new WorstFitAvailabilityAwareVmAllocationPolicy(hostsWithMockedPolicy);
        allocationPolicyWithMock.setSimulationTimeUtil(simulationTimeUtil);

        Mockito.when(MockedVm.getCurrentAvailability(simulationTimeUtil.clock())).thenReturn(1.0);

        // environment with no mocked entities

        properties = new Properties();
        properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");

        properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "0", "1");
        properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "1", "0.9");
        properties.setProperty(VmAvailabilityBasedPreemptionPolicy.SLO_TARGET_PREFIX_PROP + "2", "0.5");

        int vmId = 1;
        int userId = 1;
        cpuReq = 0.5;
        double memReq = 0;
        double submitTime = 0d;
        priority = 1;
        double runtime = 10;
        vm0 = new PreemptableVm(vmId++, userId, cpuReq, memReq, submitTime, priority, runtime);
        vm1 = new PreemptableVm(vmId++, userId, (cpuReq / 2), memReq, submitTime, priority, runtime);

        preemptionPolicy1 = new VmAvailabilityBasedPreemptionPolicy(properties, simulationTimeUtil);
        preemptionPolicy2 = new VmAvailabilityBasedPreemptionPolicy(properties, simulationTimeUtil);
        preemptionPolicy3 = new VmAvailabilityBasedPreemptionPolicy(properties, simulationTimeUtil);

        host1 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), preemptionPolicy1);

        host2 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), preemptionPolicy2);

        host3 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), preemptionPolicy3);

        hostsWithNoMockedPolicy = new ArrayList<>();
        hostsWithNoMockedPolicy.add(host1);
        hostsWithNoMockedPolicy.add(host2);
        hostsWithNoMockedPolicy.add(host3);

        allocationPolicyWithNoMock = new WorstFitAvailabilityAwareVmAllocationPolicy(hostsWithNoMockedPolicy);
        allocationPolicyWithNoMock.setSimulationTimeUtil(simulationTimeUtil);
    }

    @Test
    public void testSelectHostForVM(){

        Mockito.when(MockedVm.getCurrentAvailability(0d)).thenReturn(1.0);
        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(10.0);

        allocationPolicyWithMock.preProcess();

        Assert.assertEquals(allocationPolicyWithMock.selectHost(MockedVm), hostWithMockedPolicy1);
    }

    @Test
    public void testSelectHostForVM2(){

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(5.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(5.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(5.0);

        allocationPolicyWithMock.preProcess();

        Assert.assertEquals(allocationPolicyWithMock.selectHost(MockedVm), hostWithMockedPolicy2);
    }

    @Test
    public void testSelectHostForVM3(){

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(5.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(9.9);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(1.0);

        allocationPolicyWithMock.preProcess();

        Assert.assertEquals(allocationPolicyWithMock.selectHost(MockedVm), hostWithMockedPolicy3);
    }

    @Test
    public void testSelectHostForVM4(){

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(1.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(1.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(1.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(1.0000000001);

        allocationPolicyWithMock.preProcess();

        Assert.assertEquals(allocationPolicyWithMock.selectHost(MockedVm), hostWithMockedPolicy4);
    }

    @Test
    public void testSelectHostForVM5(){

        double cpuReq = 0.6;
        int priority = 0;

        Mockito.when(MockedVm.getPriority()).thenReturn(priority);
        Mockito.when(MockedVm.getMips()).thenReturn(cpuReq);

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.4);
        Mockito.when(mockedPreemptionPolicy1.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.4);
        Mockito.when(mockedPreemptionPolicy2.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.4);
        Mockito.when(mockedPreemptionPolicy3.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.0000000001);
        Mockito.when(mockedPreemptionPolicy4.isSuitableFor(MockedVm)).thenReturn(false);

        Assert.assertNull(allocationPolicyWithMock.selectHost(MockedVm));
    }

    @Test
    public void testSelectHostForVM6(){

        double cpuReq = 0.0000001;
        int priority = 0;

        Mockito.when(MockedVm.getPriority()).thenReturn(priority);
        Mockito.when(MockedVm.getMips()).thenReturn(cpuReq);

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.000000004);
        Mockito.when(mockedPreemptionPolicy1.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.000000004);
        Mockito.when(mockedPreemptionPolicy2.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.00000002);
        Mockito.when(mockedPreemptionPolicy3.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.0000000001);
        Mockito.when(mockedPreemptionPolicy4.isSuitableFor(MockedVm)).thenReturn(false);

        allocationPolicyWithMock.preProcess();

        Assert.assertEquals(hostWithMockedPolicy3, allocationPolicyWithMock.selectHost(MockedVm));
    }

    @Test
    public void testSelectHostForVM7(){

        double cpuReq = 0.0000001;
        int priority = 0;

        Mockito.when(MockedVm.getPriority()).thenReturn(priority);
        Mockito.when(MockedVm.getMips()).thenReturn(cpuReq);

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.000000004);
        Mockito.when(mockedPreemptionPolicy1.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.000000004);
        Mockito.when(mockedPreemptionPolicy2.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.00000001);
        Mockito.when(mockedPreemptionPolicy3.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.0000000001);
        Mockito.when(mockedPreemptionPolicy4.isSuitableFor(MockedVm)).thenReturn(false);

        allocationPolicyWithMock.preProcess();

        Assert.assertEquals(hostWithMockedPolicy3, allocationPolicyWithMock.selectHost(MockedVm));
    }

    @Test
    public void testSelectHostForVM8(){

        int vmId = 0;
        int userId = 1;
        double cpuReq = 0.0000001;
        double memReq = 0;
        double submitTime = 0;
        int priority = 0;
        double runtime = 10;

        Mockito.when(MockedVm.getPriority()).thenReturn(priority);
        Mockito.when(MockedVm.getMips()).thenReturn(cpuReq);


        cpuReq = 0.499999996;
        PreemptableVm vm1 = new PreemptableVm(vmId++, userId, cpuReq, memReq, submitTime, priority, runtime);
        hostWithMockedPolicy1.vmCreate(vm1); // create the vm into the host to reduce his available mips

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.000000004);
        Mockito.when(mockedPreemptionPolicy1.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.000000004);
        Mockito.when(mockedPreemptionPolicy2.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.000000002);
        Mockito.when(mockedPreemptionPolicy3.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.0000000001);
        Mockito.when(mockedPreemptionPolicy4.isSuitableFor(MockedVm)).thenReturn(false);

        allocationPolicyWithMock.preProcess();

        Assert.assertNull(allocationPolicyWithMock.selectHost(MockedVm));
    }

    @Test
    public void testSelectHostForVM9(){

        double cpuReq = 0.0000001;
        int priority = 0;

        Mockito.when(MockedVm.getPriority()).thenReturn(priority);
        Mockito.when(MockedVm.getMips()).thenReturn(cpuReq);


        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.0000002);
        Mockito.when(mockedPreemptionPolicy1.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.0000004);
        Mockito.when(mockedPreemptionPolicy2.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.0000001);
        Mockito.when(mockedPreemptionPolicy3.isSuitableFor(MockedVm)).thenReturn(false);

        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(0.0000000001);
        Mockito.when(mockedPreemptionPolicy4.isSuitableFor(MockedVm)).thenReturn(false);


        // selected is the hostWithMockedPolicy2
        allocationPolicyWithMock.preProcess();
        Assert.assertEquals(hostWithMockedPolicy2, allocationPolicyWithMock.selectHost(MockedVm));

        //remove hostWithMockedPolicy2 then the new selected is hostWithMockedPolicy1
        allocationPolicyWithMock.getHostList().remove(hostWithMockedPolicy2);
        allocationPolicyWithMock.preProcess();
        Assert.assertEquals(hostWithMockedPolicy1, allocationPolicyWithMock.selectHost(MockedVm));

        //remove hostWithMockedPolicy1 then the new selectedHost is hostWithMockedPolicy3
        allocationPolicyWithMock.getHostList().remove(hostWithMockedPolicy1);
        allocationPolicyWithMock.preProcess();
        Assert.assertEquals(hostWithMockedPolicy3, allocationPolicyWithMock.selectHost(MockedVm));

        // remove hostWithMockedPolicy3 then the new selectedHost is hostWithMockedPolicy4
        allocationPolicyWithMock.getHostList().remove(hostWithMockedPolicy3);
        allocationPolicyWithMock.preProcess();
        Assert.assertEquals(hostWithMockedPolicy4, allocationPolicyWithMock.selectHost(MockedVm));

        //insert all hostsWithMockedPolicy again and return to the initial state where the selected host is hostWithMockedPolicy2

        allocationPolicyWithMock.getHostList().add(hostWithMockedPolicy1);
        allocationPolicyWithMock.getHostList().add(hostWithMockedPolicy2);
        allocationPolicyWithMock.getHostList().add(hostWithMockedPolicy3);
        allocationPolicyWithMock.preProcess();
        Assert.assertEquals(hostWithMockedPolicy2, allocationPolicyWithMock.selectHost(MockedVm));
    }

    @Test
    public void testSelectHostForVM10(){

        double cpuReq = 10.0;
        int priority = 0;

        Mockito.when(MockedVm.getPriority()).thenReturn(priority);
        Mockito.when(MockedVm.getMips()).thenReturn(cpuReq);
        Mockito.when(MockedVm.getCurrentAvailability(simulationTimeUtil.clock())).thenReturn(1.1);

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy1.isSuitableFor(MockedVm)).thenReturn(true);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy2.isSuitableFor(MockedVm)).thenReturn(true);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy3.isSuitableFor(MockedVm)).thenReturn(true);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy4.isSuitableFor(MockedVm)).thenReturn(true);

        Assert.assertEquals(hostWithMockedPolicy1, allocationPolicyWithMock.selectHost(MockedVm));

        // simulate a allocation of other vm in hostWithMockedPolicy1
        allocationPolicyWithMock.removePriorityHost(hostWithMockedPolicy1);
        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(9.0);
        allocationPolicyWithMock.addPriorityHost(hostWithMockedPolicy1);
        Assert.assertEquals(hostWithMockedPolicy2, allocationPolicyWithMock.selectHost(MockedVm));

        // simulate a deallocation of other vm in hostWithMockedPolicy4
        allocationPolicyWithMock.removePriorityHost(hostWithMockedPolicy4);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(11.0);
        allocationPolicyWithMock.addPriorityHost(hostWithMockedPolicy4);
        Assert.assertEquals(hostWithMockedPolicy4, allocationPolicyWithMock.selectHost(MockedVm));
    }

    @Test
    public void testAllocateHostForVm1() {
        // checking initial state
        Assert.assertNull(vm0.getHost());
        Assert.assertNull(vm1.getHost());
        Assert.assertTrue(host1.getVmList().isEmpty());
        Assert.assertTrue(host2.getVmList().isEmpty());
        Assert.assertTrue(host3.getVmList().isEmpty());

        Mockito.when(simulationTimeUtil.clock()).thenReturn(0d);


        //test methods to administrate vm allocation
        PreemptiveHost selectedHost = (PreemptiveHost) allocationPolicyWithNoMock.selectHost(vm0);
        Assert.assertEquals(host1, selectedHost);

        allocationPolicyWithNoMock.allocateHostForVm(vm0, selectedHost);
        Assert.assertEquals(host1, vm0.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0));


        //change time to 1s
        Mockito.when(simulationTimeUtil.clock()).thenReturn(1d);

        //test methods of the vm allocation
        allocationPolicyWithNoMock.preProcess();
        selectedHost = (PreemptiveHost) allocationPolicyWithNoMock.selectHost(vm1);
        Assert.assertEquals(host1, selectedHost);

        allocationPolicyWithNoMock.preempt(vm0);
        Assert.assertFalse(host1.getVmList().contains(vm0));
        Assert.assertNull(vm0.getHost());
        Assert.assertNull(vm1.getHost());


        allocationPolicyWithNoMock.allocateHostForVm(vm1);
        Assert.assertEquals(host1, vm1.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm1));

        Assert.assertNull(vm0.getHost());
        selectedHost = (PreemptiveHost) allocationPolicyWithNoMock.selectHost(vm0);
        Assert.assertEquals(host2, selectedHost);

        allocationPolicyWithNoMock.allocateHostForVm(vm0);
        Assert.assertEquals(host2, vm0.getHost());
    }

    @Test
    public void testAllocationForVM2() {

        int vmId = 3;
        int userId = 1;
        double cpuReq = 0.5;
        double memReq = 0;
        double submitTime = 0d;
        int priority = 2;
        double runtime = 10;

        PreemptableVm vm0P2 = new PreemptableVm(vmId++, userId, cpuReq, memReq, submitTime, priority, runtime);
        PreemptableVm vm1P2 = new PreemptableVm(vmId++, userId, (cpuReq / 2), memReq, submitTime, priority, runtime);

        priority = 0;
        submitTime = 2;
        PreemptableVm vm0P0 = new PreemptableVm(vmId++, userId, cpuReq, memReq, submitTime, priority, runtime);
        PreemptableVm vm1P0 = new PreemptableVm(vmId++, userId, (cpuReq / 2), memReq, submitTime, priority, runtime);

        Mockito.when(simulationTimeUtil.clock()).thenReturn(0d);
        allocationPolicyWithNoMock.preProcess();

        // checking initial state
        Assert.assertNull(vm0.getHost());
        Assert.assertNull(vm1.getHost());
        Assert.assertNull(vm0P0.getHost());
        Assert.assertNull(vm1P0.getHost());
        Assert.assertNull(vm0P2.getHost());
        Assert.assertNull(vm1P2.getHost());

        Assert.assertTrue(host1.getVmList().isEmpty());
        Assert.assertTrue(host2.getVmList().isEmpty());
        Assert.assertTrue(host3.getVmList().isEmpty());

        // test methods of vm allocation policy

        Host selectedHost = allocationPolicyWithNoMock.selectHost(vm0P2);
        Assert.assertEquals(host1, selectedHost);

        allocationPolicyWithNoMock.allocateHostForVm(vm0P2, selectedHost);
        Assert.assertEquals(host1, vm0P2.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0P2));

        selectedHost = allocationPolicyWithNoMock.selectHost(vm1P2);
        Assert.assertEquals(host2, selectedHost);

        allocationPolicyWithNoMock.allocateHostForVm(vm1P2, selectedHost);
        Assert.assertEquals(host2, vm1P2.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm1P2));

        selectedHost = allocationPolicyWithNoMock.selectHost(vm0);
        Assert.assertEquals(host1, selectedHost);

        allocationPolicyWithNoMock.preempt(vm0P2);
        Assert.assertFalse(host1.getVmList().contains(vm0P2));

        allocationPolicyWithNoMock.allocateHostForVm(vm0, selectedHost);
        Assert.assertEquals(host1, vm0.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0));
        Assert.assertNull(vm0P2.getHost());

        selectedHost = allocationPolicyWithNoMock.selectHost(vm1);
        Assert.assertEquals(host2, selectedHost);

        allocationPolicyWithNoMock.allocateHostForVm(vm1, selectedHost);
        Assert.assertEquals(host2, vm1P2.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm1P2));
        Assert.assertEquals(host2, vm1.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm1));

        //advance time to 1s
        Mockito.when(simulationTimeUtil.clock()).thenReturn(1d);
        allocationPolicyWithNoMock.preProcess();

        //verify status of the system
        Assert.assertEquals(host1, vm0.getHost());
        Assert.assertEquals(1, host1.getVmList().size());
        Assert.assertEquals(host2, vm1.getHost());
        Assert.assertEquals(host2, vm1P2.getHost());
        Assert.assertEquals(2, host2.getVmList().size());
        Assert.assertNull(vm0P0.getHost());
        Assert.assertNull(vm1P0.getHost());
        Assert.assertNull(vm0P2.getHost());

        Assert.assertTrue(host1.getVmList().contains(vm0));
        Assert.assertTrue(host2.getVmList().contains(vm1));
        Assert.assertTrue(host2.getVmList().contains(vm1P2));
        Assert.assertTrue(host3.getVmList().isEmpty());

        //test methods of the vm allocation policy
        selectedHost = allocationPolicyWithNoMock.selectHost(vm0P2);
        Assert.assertEquals(host3, selectedHost);

        allocationPolicyWithNoMock.allocateHostForVm(vm0P2, selectedHost);
        Assert.assertEquals(host3, vm0P2.getHost());
        Assert.assertTrue(host3.getVmList().contains(vm0P2));

        //advance time to 1s
        Mockito.when(simulationTimeUtil.clock()).thenReturn(2d);
        allocationPolicyWithNoMock.preProcess();

        //verify status of the system
        Assert.assertEquals(host1, vm0.getHost());
        Assert.assertEquals(host2, vm1.getHost());
        Assert.assertEquals(host2, vm1P2.getHost());
        Assert.assertEquals(host3, vm0P2.getHost());
        Assert.assertNull(vm0P0.getHost());
        Assert.assertNull(vm1P0.getHost());

        Assert.assertTrue(host1.getVmList().contains(vm0));
        Assert.assertEquals(1, host1.getVmList().size());
        Assert.assertTrue(host2.getVmList().contains(vm1));
        Assert.assertTrue(host2.getVmList().contains(vm1P2));
        Assert.assertEquals(2, host2.getVmList().size());
        Assert.assertTrue(host3.getVmList().contains(vm0P2));
        Assert.assertEquals(1, host3.getVmList().size());

        //test methods of the vm allocation policy

        priority = 1;
        PreemptableVm vm0P1 = new PreemptableVm(vmId++, userId, cpuReq, memReq, submitTime, priority, runtime);

        selectedHost = allocationPolicyWithNoMock.selectHost(vm0P1);
        Assert.assertEquals(host1, selectedHost);

        allocationPolicyWithNoMock.preempt(vm0);
        Assert.assertNull(vm0.getHost());
        Assert.assertNull(vm0P1.getHost());

        allocationPolicyWithNoMock.allocateHostForVm(vm0P1, selectedHost);
        Assert.assertEquals(host1, vm0P1.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0P1));
        Assert.assertFalse(host1.getVmList().contains(vm0));

        selectedHost = allocationPolicyWithNoMock.selectHost(vm0P0);
        Assert.assertEquals(host1, selectedHost);

        allocationPolicyWithNoMock.preempt(vm0P1);
        Assert.assertNull(vm0P0.getHost());
        Assert.assertNull(vm0P1.getHost());

        allocationPolicyWithNoMock.allocateHostForVm(vm0P0, selectedHost);
        Assert.assertEquals(host1, vm0P0.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0P0));
        Assert.assertFalse(host1.getVmList().contains(vm0P1));

        selectedHost = allocationPolicyWithNoMock.selectHost(vm1P0);
        Assert.assertEquals(host2, selectedHost);

        allocationPolicyWithNoMock.preempt(vm1P2);
        Assert.assertNull(vm1P2.getHost());
        Assert.assertNull(vm1P0.getHost());
        Assert.assertEquals(host2, vm1.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm1));
        Assert.assertFalse(host2.getVmList().contains(vm1P0));

        allocationPolicyWithNoMock.allocateHostForVm(vm1P0, selectedHost);
        Assert.assertEquals(host2, vm1P0.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm1P0));
        Assert.assertTrue(host2.getVmList().contains(vm1));

        // check final status of timeStamp 2s

        Assert.assertNull(vm0.getHost());
        Assert.assertNull(vm1P2.getHost());
        Assert.assertEquals(host1, vm0P0.getHost());
        Assert.assertEquals(host2, vm1.getHost());
        Assert.assertEquals(host2, vm1P0.getHost());
        Assert.assertEquals(host3, vm0P2.getHost());

        Assert.assertTrue(host1.getVmList().contains(vm0P0));
        Assert.assertEquals(1, host1.getVmList().size());
        Assert.assertTrue(host2.getVmList().contains(vm1));
        Assert.assertTrue(host2.getVmList().contains(vm1P0));
        Assert.assertEquals(2, host2.getVmList().size());
        Assert.assertTrue(host3.getVmList().contains(vm0P2));
        Assert.assertEquals(1, host3.getVmList().size());


        // deallocating every vm
        allocationPolicyWithNoMock.deallocateHostForVm(vm0P0);
        Assert.assertNull(vm0P0.getHost());
        Assert.assertTrue(host1.getVmList().isEmpty());

        allocationPolicyWithNoMock.deallocateHostForVm(vm1);
        Assert.assertNull(vm1.getHost());
        Assert.assertFalse(host1.getVmList().contains(vm1));

        allocationPolicyWithNoMock.deallocateHostForVm(vm1P0);
        Assert.assertNull(vm1P0.getHost());
        Assert.assertTrue(host1.getVmList().isEmpty());

        allocationPolicyWithNoMock.deallocateHostForVm(vm0P2);
        Assert.assertNull(vm0P2.getHost());
        Assert.assertTrue(host3.getVmList().isEmpty());
    }
}