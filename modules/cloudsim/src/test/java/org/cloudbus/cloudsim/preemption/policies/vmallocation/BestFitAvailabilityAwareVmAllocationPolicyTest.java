package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import gnu.trove.map.hash.THashMap;
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


    private static final double HOST_CAPACITY = 10;
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

    private BestFitAvailabilityAwareVmAllocationPolicy bestFitVmAllocationPolicy;
    private BestFitAvailabilityAwareVmAllocationPolicy allocationPolicyWithNoMock;
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
        int priority = 0;

        MockedVm = Mockito.mock(PreemptableVm.class);
        Mockito.when(MockedVm.getPriority()).thenReturn(priority);
        Mockito.when(MockedVm.getMips()).thenReturn(cpuReq);

        simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);

        bestFitVmAllocationPolicy = new BestFitAvailabilityAwareVmAllocationPolicy(hostsWithMockedPolicy);
        bestFitVmAllocationPolicy.setSimulationTimeUtil(simulationTimeUtil);

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

        allocationPolicyWithNoMock = new BestFitAvailabilityAwareVmAllocationPolicy(hostsWithNoMockedPolicy);
        allocationPolicyWithNoMock.setSimulationTimeUtil(simulationTimeUtil);
    }


    @Test
    public void testSelectHostForVM(){

        Mockito.when(MockedVm.getCurrentAvailability(0d)).thenReturn(0.9);
        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(8.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(5.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(1.0);

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);

        bestFitVmAllocationPolicy.preProcess();

        Assert.assertEquals(hostWithMockedPolicy4, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getCurrentAvailability(0d)).thenReturn(1.0);

        Assert.assertEquals(hostWithMockedPolicy1, bestFitVmAllocationPolicy.selectHost(MockedVm));

    }


    @Test
    public void testSelectHostForVM2(){

        Mockito.when(MockedVm.getCurrentAvailability(0d)).thenReturn(1.0);
        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(8.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(5.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(1.0);

        Mockito.when(MockedVm.getMips()).thenReturn(0.9);
        Assert.assertEquals(hostWithMockedPolicy4, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(1.1);
        Assert.assertEquals(hostWithMockedPolicy3, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(5.0);
        Assert.assertEquals(hostWithMockedPolicy3, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(7.0);
        Assert.assertEquals(hostWithMockedPolicy2, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(8.1);
        Assert.assertEquals(hostWithMockedPolicy1, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(10.1);
        Assert.assertNull(bestFitVmAllocationPolicy.selectHost(MockedVm));
    }


    @Test
    public void testSelectHostForVM3(){

        Mockito.when(MockedVm.getCurrentAvailability(0d)).thenReturn(0.99);
        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(10.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(8.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(5.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriority(MockedVm.getPriority())).thenReturn(1.0);

        Mockito.when(mockedPreemptionPolicy1.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(1.0);
        Mockito.when(mockedPreemptionPolicy2.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(2.0);
        Mockito.when(mockedPreemptionPolicy3.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(3.0);
        Mockito.when(mockedPreemptionPolicy4.getAvailableMipsByPriorityAndAvailability(MockedVm.getPriority())).thenReturn(5.0);

        bestFitVmAllocationPolicy.preProcess();
        Mockito.when(MockedVm.getMips()).thenReturn(0.9);
        Assert.assertEquals(hostWithMockedPolicy1, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(1.1);
        Assert.assertEquals(hostWithMockedPolicy2, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(2.0);
        Assert.assertEquals(hostWithMockedPolicy2, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(2.5);
        Assert.assertEquals(hostWithMockedPolicy3, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(4.8);
        Assert.assertEquals(hostWithMockedPolicy4, bestFitVmAllocationPolicy.selectHost(MockedVm));

        Mockito.when(MockedVm.getMips()).thenReturn(5.1);
        Assert.assertNull(bestFitVmAllocationPolicy.selectHost(MockedVm));
    }





}
