package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import gnu.trove.map.hash.THashMap;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jvmafra on 04/04/17.
 */
public class BestFitAvailabilityAwareVmAllocationPolicyTest {


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

    private BestFitAvailabilityAwareVmAllocationPolicy bestFitVmAllocationPolicyWithMock;
    private BestFitAvailabilityAwareVmAllocationPolicy bestFitVmAllocationPolicy;
    private Map<Integer, Double> priorityToSLOTarget;
    private SimulationTimeUtil simulationTimeUtil;
    private Properties properties;
    private PreemptiveHost host1;
    private PreemptiveHost host2;
    private PreemptiveHost host3;


    @Before
    public void setUp() throws Exception {


        // environment with VmAvailabilityBasedPreemptionPolicy

        priorityToSLOTarget = new THashMap<>();
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
        int priority = 1;

        MockedVm = Mockito.mock(PreemptableVm.class);
        Mockito.when(MockedVm.getPriority()).thenReturn(priority);
        Mockito.when(MockedVm.getMips()).thenReturn(cpuReq);

        simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);

        bestFitVmAllocationPolicyWithMock = new BestFitAvailabilityAwareVmAllocationPolicy(hostsWithMockedPolicy);
        bestFitVmAllocationPolicyWithMock.setSimulationTimeUtil(simulationTimeUtil);

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

        hostId = 1;
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

        bestFitVmAllocationPolicy = new BestFitAvailabilityAwareVmAllocationPolicy(hostsWithNoMockedPolicy);
        bestFitVmAllocationPolicy.setSimulationTimeUtil(simulationTimeUtil);
    }


    @Test
    public void testSelectHostForVM(){

        // SLA is not being fulfilled
        Mockito.when(MockedVm.getCurrentAvailability(0d)).thenReturn(0.89);
        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(8.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(5.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(1.0);

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);

        bestFitVmAllocationPolicyWithMock.preProcess();

        Assert.assertEquals(hostWithMockedPolicy4, bestFitVmAllocationPolicyWithMock.selectHost(MockedVm));

        // SLA is being fulfilled
        Mockito.when(MockedVm.getCurrentAvailability(0d)).thenReturn(0.91);
        Assert.assertEquals(hostWithMockedPolicy1, bestFitVmAllocationPolicyWithMock.selectHost(MockedVm));

        // Availability is equals to target
        Mockito.when(MockedVm.getCurrentAvailability(0d)).thenReturn(0.9);
        Assert.assertEquals(hostWithMockedPolicy4, bestFitVmAllocationPolicyWithMock.selectHost(MockedVm));

    }

    @Test
    public void testSelectHostForVM2(){

        Mockito.when(MockedVm.getCurrentAvailability(0d)).thenReturn(0.9);

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(1.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(2.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(3.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(5.0);

        bestFitVmAllocationPolicyWithMock.preProcess();
        Mockito.when(MockedVm.getMips()).thenReturn(0.9);
        Assert.assertEquals(hostWithMockedPolicy1, bestFitVmAllocationPolicyWithMock.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(1.1);
        Assert.assertEquals(hostWithMockedPolicy2, bestFitVmAllocationPolicyWithMock.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(2.0);
        Assert.assertEquals(hostWithMockedPolicy2, bestFitVmAllocationPolicyWithMock.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(2.5);
        Assert.assertEquals(hostWithMockedPolicy3, bestFitVmAllocationPolicyWithMock.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(4.8);
        Assert.assertEquals(hostWithMockedPolicy4, bestFitVmAllocationPolicyWithMock.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(5.1);
        Assert.assertNull(bestFitVmAllocationPolicyWithMock.selectHost(MockedVm));
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
        // hosts order by available: host1: 0.5 mips, host2: 0.5 mips, host3: 0.5 mips
        PreemptiveHost selectedHost = (PreemptiveHost) bestFitVmAllocationPolicy.selectHost(vm0);
        Assert.assertEquals(host1, selectedHost);

        bestFitVmAllocationPolicy.allocateHostForVm(vm0, selectedHost);
        Assert.assertEquals(host1, vm0.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0));

        //change time to 1s
        Mockito.when(simulationTimeUtil.clock()).thenReturn(1d);

        //test methods of the vm allocation
        bestFitVmAllocationPolicy.preProcess();

        // hosts order by available: host1: 0.5 mips (vm0 can be preempted), host2: 0.5 mips, host3: 0.5 mips
        selectedHost = (PreemptiveHost) bestFitVmAllocationPolicy.selectHost(vm1);
        Assert.assertEquals(host1, selectedHost);

        bestFitVmAllocationPolicy.preempt(vm0);
        Assert.assertFalse(host1.getVmList().contains(vm0));
        Assert.assertNull(vm0.getHost());
        Assert.assertNull(vm1.getHost());


        bestFitVmAllocationPolicy.allocateHostForVm(vm1, selectedHost);
        Assert.assertEquals(host1, vm1.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm1));

        // hosts order by available: host1: 0 mips (vm1 allocated and can not be preempted), host2: 0.5 mips, host3: 0.5 mips
        Assert.assertNull(vm0.getHost());
        selectedHost = (PreemptiveHost) bestFitVmAllocationPolicy.selectHost(vm0);
        Assert.assertEquals(host2, selectedHost);

        bestFitVmAllocationPolicy.allocateHostForVm(vm0, selectedHost);
        Assert.assertEquals(host2, vm0.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm0));

        Assert.assertEquals(0.25, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.5, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void testAllocateHostForVm2() {

        // cpuReq = 0.3
        PreemptableVm vm0P1 = new PreemptableVm(1, 1, 0.3, 0, 0, 1, 10);

        // cpuReq = 0.2
        PreemptableVm vm1P1 = new PreemptableVm(1, 1, 0.2, 0, 0, 1, 10);

        // checking initial state
        Assert.assertNull(vm0.getHost());
        Assert.assertNull(vm1.getHost());
        Assert.assertTrue(host1.getVmList().isEmpty());
        Assert.assertTrue(host2.getVmList().isEmpty());
        Assert.assertTrue(host3.getVmList().isEmpty());

        Mockito.when(simulationTimeUtil.clock()).thenReturn(0d);


        //test methods to administrate vm allocation
        // hosts order by available: host1: 0.5 mips, host2: 0.5 mips, host3: 0.5 mips
        // vm1 requirement: 0.25
        PreemptiveHost selectedHost = (PreemptiveHost) bestFitVmAllocationPolicy.selectHost(vm1);
        Assert.assertEquals(host1, selectedHost);

        bestFitVmAllocationPolicy.allocateHostForVm(vm1, selectedHost);
        Assert.assertEquals(host1, vm1.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm1));

        //change time to 1s
        Mockito.when(simulationTimeUtil.clock()).thenReturn(1d);

        //test methods of the vm allocation
        bestFitVmAllocationPolicy.preProcess();

        // hosts order by available: host1: 0.5 mips (vm1 can be preempted), host2: 0.5 mips, host3: 0.5 mips
        selectedHost = (PreemptiveHost) bestFitVmAllocationPolicy.selectHost(vm0P1);
        Assert.assertEquals(host1, selectedHost);

        bestFitVmAllocationPolicy.preempt(vm1);
        Assert.assertFalse(host1.getVmList().contains(vm1));
        Assert.assertNull(vm1.getHost());
        Assert.assertNull(vm0P1.getHost());


        bestFitVmAllocationPolicy.allocateHostForVm(vm0P1, selectedHost);
        Assert.assertEquals(host1, vm0P1.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0P1));

        // hosts order by available: host1: 0.2 mips (vm0P1 allocated and can not be preempted), host2: 0.5 mips, host3: 0.5 mips
        Assert.assertNull(vm1.getHost());
        Assert.assertNull(vm1P1.getHost());

        // VM1P1 requirement: 0.2
        selectedHost = (PreemptiveHost) bestFitVmAllocationPolicy.selectHost(vm1P1);
        Assert.assertEquals(host1, selectedHost);

        bestFitVmAllocationPolicy.allocateHostForVm(vm1P1, selectedHost);
        Assert.assertEquals(host1, vm1P1.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm1P1));

        Assert.assertEquals(0, host1.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.5, host2.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.5, host3.getAvailableMips(), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void testAllocateHostForVm3() {
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
        bestFitVmAllocationPolicy.preProcess();

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

        // order of hosts by available for priority2: host1: 0.5, host2: 0.5, host3: 0.5
        Host selectedHost = bestFitVmAllocationPolicy.selectHost(vm0P2);
        Assert.assertEquals(host1, selectedHost);

        bestFitVmAllocationPolicy.allocateHostForVm(vm0P2, selectedHost);
        Assert.assertEquals(host1, vm0P2.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0P2));

        // order of hosts by available for priority2: host1: 0, host2: 0.5, host3: 0.5
        selectedHost = bestFitVmAllocationPolicy.selectHost(vm1P2);
        Assert.assertEquals(host2, selectedHost);

        bestFitVmAllocationPolicy.allocateHostForVm(vm1P2, selectedHost);
        Assert.assertEquals(host2, vm1P2.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm1P2));

        // order of hosts by available for priority1: host1: 0.5, host2: 0.5, host3: 0.5
        selectedHost = bestFitVmAllocationPolicy.selectHost(vm0);
        Assert.assertEquals(host1, selectedHost);

        bestFitVmAllocationPolicy.preempt(vm0P2);
        Assert.assertFalse(host1.getVmList().contains(vm0P2));

        bestFitVmAllocationPolicy.allocateHostForVm(vm0, selectedHost);
        Assert.assertEquals(host1, vm0.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0));
        Assert.assertNull(vm0P2.getHost());

        // order of hosts by available for priority1: host1: 0, host2: 0.5, host3: 0.5
        selectedHost = bestFitVmAllocationPolicy.selectHost(vm1);
        Assert.assertEquals(host2, selectedHost);

        bestFitVmAllocationPolicy.allocateHostForVm(vm1, selectedHost);
        Assert.assertEquals(host2, vm1P2.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm1P2));
        Assert.assertEquals(host2, vm1.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm1));

        //advance time to 1s
        Mockito.when(simulationTimeUtil.clock()).thenReturn(1d);
        bestFitVmAllocationPolicy.preProcess();

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

        // order of hosts by available for priority2: host1: 0, host2: 0.25 (vm1P2 can be preempted), host3: 0.5
        selectedHost = bestFitVmAllocationPolicy.selectHost(vm0P2);
        Assert.assertEquals(host3, selectedHost);

        bestFitVmAllocationPolicy.allocateHostForVm(vm0P2, selectedHost);
        Assert.assertEquals(host3, vm0P2.getHost());
        Assert.assertTrue(host3.getVmList().contains(vm0P2));

        //advance time to 1s
        Mockito.when(simulationTimeUtil.clock()).thenReturn(2d);
        bestFitVmAllocationPolicy.preProcess();

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


        // order of hosts by available for priority1: host1: 0.5 (vm0 can be preempted), host2: 0.5 (vm1
        // and vm1P2 can be preempted), host3: 0.5 (Vm0P2 can be preempted)
        selectedHost = bestFitVmAllocationPolicy.selectHost(vm0P1);
        Assert.assertEquals(host1, selectedHost);

        bestFitVmAllocationPolicy.preempt(vm0);
        Assert.assertNull(vm0.getHost());
        Assert.assertNull(vm0P1.getHost());

        bestFitVmAllocationPolicy.allocateHostForVm(vm0P1, selectedHost);
        Assert.assertEquals(host1, vm0P1.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0P1));
        Assert.assertFalse(host1.getVmList().contains(vm0));

        // order of hosts by available for priority1: host1: 0.5 (vm0P1 can be preempted), host2: 0.5 (vm1
        // and vm1P2 can be preempted), host3: 0.5 (Vm0P2 can be preempted)
        selectedHost = bestFitVmAllocationPolicy.selectHost(vm0P0);
        Assert.assertEquals(host1, selectedHost);

        bestFitVmAllocationPolicy.preempt(vm0P1);
        Assert.assertNull(vm0P0.getHost());
        Assert.assertNull(vm0P1.getHost());

        bestFitVmAllocationPolicy.allocateHostForVm(vm0P0, selectedHost);
        Assert.assertEquals(host1, vm0P0.getHost());
        Assert.assertTrue(host1.getVmList().contains(vm0P0));
        Assert.assertFalse(host1.getVmList().contains(vm0P1));

        // order of hosts by available for priority1: host1: 0 (vm0P0 can't be preempted), host2: 0.5 (vm1
        // and vm1P2 can be preempted), host3: 0.5 (Vm0P2 can be preempted)
        selectedHost = bestFitVmAllocationPolicy.selectHost(vm1P0);
        Assert.assertEquals(host2, selectedHost);

        bestFitVmAllocationPolicy.preempt(vm1P2);
        Assert.assertNull(vm1P2.getHost());
        Assert.assertNull(vm1P0.getHost());
        Assert.assertEquals(host2, vm1.getHost());
        Assert.assertTrue(host2.getVmList().contains(vm1));
        Assert.assertFalse(host2.getVmList().contains(vm1P0));

        bestFitVmAllocationPolicy.allocateHostForVm(vm1P0, selectedHost);
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
        bestFitVmAllocationPolicy.deallocateHostForVm(vm0P0);
        Assert.assertNull(vm0P0.getHost());
        Assert.assertTrue(host1.getVmList().isEmpty());

        bestFitVmAllocationPolicy.deallocateHostForVm(vm1);
        Assert.assertNull(vm1.getHost());
        Assert.assertFalse(host1.getVmList().contains(vm1));

        bestFitVmAllocationPolicy.deallocateHostForVm(vm1P0);
        Assert.assertNull(vm1P0.getHost());
        Assert.assertTrue(host1.getVmList().isEmpty());

        bestFitVmAllocationPolicy.deallocateHostForVm(vm0P2);
        Assert.assertNull(vm0P2.getHost());
        Assert.assertTrue(host3.getVmList().isEmpty());
    }

}
