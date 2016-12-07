package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
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
    public final double ACCEPTABLE_DIFFERENCE = 0.00001;

    public List<PreemptiveHost> hosts;

    public VmAvailabilityBasedPreemptionPolicy preemptionPolicy1;
    public VmAvailabilityBasedPreemptionPolicy preemptionPolicy2;
    public VmAvailabilityBasedPreemptionPolicy preemptionPolicy3;
    public VmAvailabilityBasedPreemptionPolicy preemptionPolicy4;

    public PreemptiveHost host1;
    public PreemptiveHost host2;
    public PreemptiveHost host3;
    public PreemptiveHost host4;
    private PreemptableVm vm0;
    private WorstFitAvailabilityAwareVmAllocationPolicy allocationPolicy;
    private Map<Integer, Double> priorityToSLOTarget;
    private SimulationTimeUtil simulationTimeUtil;

    @Before
    public void setUp() throws Exception {


        // environment with VmAvailabilityBasedPreemptionPolicy

        priorityToSLOTarget = new HashMap<>();
        priorityToSLOTarget.put(0, 1.0);
        priorityToSLOTarget.put(1, 0.9);
        priorityToSLOTarget.put(2, 0.5);

        preemptionPolicy1 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        Mockito.when(preemptionPolicy1.getPriorityToSLOTarget()).thenReturn(priorityToSLOTarget);
        Mockito.when(preemptionPolicy1.getNumberOfPriorities()).thenReturn(3);

        preemptionPolicy2 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        Mockito.when(preemptionPolicy2.getPriorityToSLOTarget()).thenReturn(priorityToSLOTarget);
        Mockito.when(preemptionPolicy2.getNumberOfPriorities()).thenReturn(3);

        preemptionPolicy3 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        Mockito.when(preemptionPolicy3.getPriorityToSLOTarget()).thenReturn(priorityToSLOTarget);
        Mockito.when(preemptionPolicy3.getNumberOfPriorities()).thenReturn(3);

        preemptionPolicy4 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        Mockito.when(preemptionPolicy4.getPriorityToSLOTarget()).thenReturn(priorityToSLOTarget);
        Mockito.when(preemptionPolicy4.getNumberOfPriorities()).thenReturn(3);

        hosts = new ArrayList<>();

        List<Pe> peList1 = new ArrayList<>();

        peList1.add(new Pe(0, new PeProvisionerSimple(HOST_CAPACITY)));

        int hostId = 0;
        host1 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), preemptionPolicy1);

        host2 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), preemptionPolicy2);

        host3 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), preemptionPolicy3);

        host4 = new PreemptiveHost(hostId++, peList1,
                new VmSchedulerMipsBased(peList1), preemptionPolicy4);

        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);
        hosts.add(host4);

        int vmId = 1;
        int userId = 1;
        double cpuReq = 0.5;
        double memReq = 0;
        double submitTime = 0d;
        int priority = 0;
        double runtime = 10;

        vm0 = Mockito.mock(PreemptableVm.class);
        Mockito.when(vm0.getPriority()).thenReturn(priority);
        Mockito.when(vm0.getMips()).thenReturn(cpuReq);

        simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);

        allocationPolicy = new WorstFitAvailabilityAwareVmAllocationPolicy(hosts);
        allocationPolicy.setSimulationTimeUtil(simulationTimeUtil);

        Mockito.when(vm0.getCurrentAvailability(simulationTimeUtil.clock())).thenReturn(1.0);

    }

    //tests for environment with VmAvailabilityBasedPreemptionPolicy

    @Test
    public void testSelectHostForVM(){

        Mockito.when(vm0.getCurrentAvailability(0d)).thenReturn(1.0);
        Mockito.when(preemptionPolicy1.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(10.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(10.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(10.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(10.0);

        allocationPolicy.preProcess();

        Assert.assertEquals(allocationPolicy.selectHost(vm0), host1);
    }

    @Test
    public void testSelectHostForVM2(){

        Mockito.when(preemptionPolicy1.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(5.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(10.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(5.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(5.0);

        allocationPolicy.preProcess();

        Assert.assertEquals(allocationPolicy.selectHost(vm0), host2);
    }

    @Test
    public void testSelectHostForVM3(){

        Mockito.when(preemptionPolicy1.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(5.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(9.9);
        Mockito.when(preemptionPolicy3.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(10.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(1.0);

        allocationPolicy.preProcess();

        Assert.assertEquals(allocationPolicy.selectHost(vm0), host3);
    }

    @Test
    public void testSelectHostForVM4(){

        Mockito.when(preemptionPolicy1.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(1.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(1.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(1.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(1.0000000001);

        allocationPolicy.preProcess();

        Assert.assertEquals(allocationPolicy.selectHost(vm0), host4);
    }

    @Test
    public void testSelectHostForVM5(){

        double cpuReq = 0.6;
        int priority = 0;

        Mockito.when(vm0.getPriority()).thenReturn(priority);
        Mockito.when(vm0.getMips()).thenReturn(cpuReq);

        Mockito.when(preemptionPolicy1.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.4);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.4);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.4);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);

        Assert.assertNull(allocationPolicy.selectHost(vm0));
    }

    @Test
    public void testSelectHostForVM6(){

        double cpuReq = 0.0000001;
        int priority = 0;

        Mockito.when(vm0.getPriority()).thenReturn(priority);
        Mockito.when(vm0.getMips()).thenReturn(cpuReq);

        Mockito.when(preemptionPolicy1.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.00000002);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);

        allocationPolicy.preProcess();

        Assert.assertEquals(host3, allocationPolicy.selectHost(vm0));
    }

    @Test
    public void testSelectHostForVM7(){

        double cpuReq = 0.0000001;
        int priority = 0;

        Mockito.when(vm0.getPriority()).thenReturn(priority);
        Mockito.when(vm0.getMips()).thenReturn(cpuReq);

        Mockito.when(preemptionPolicy1.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.00000001);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);

        allocationPolicy.preProcess();

        Assert.assertEquals(host3, allocationPolicy.selectHost(vm0));
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

        Mockito.when(vm0.getPriority()).thenReturn(priority);
        Mockito.when(vm0.getMips()).thenReturn(cpuReq);


        cpuReq = 0.499999996;
        PreemptableVm vm1 = new PreemptableVm(vmId++, userId, cpuReq, memReq, submitTime, priority, runtime);
        host1.vmCreate(vm1); // create the vm into the host to reduce his available mips

        Mockito.when(preemptionPolicy1.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.000000002);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);

        allocationPolicy.preProcess();

        Assert.assertNull(allocationPolicy.selectHost(vm0));
    }

    @Test
    public void testSelectHostForVM9(){

        double cpuReq = 0.0000001;
        int priority = 0;

        Mockito.when(vm0.getPriority()).thenReturn(priority);
        Mockito.when(vm0.getMips()).thenReturn(cpuReq);


        Mockito.when(preemptionPolicy1.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.0000002);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.0000004);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.0000001);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByPriorityAndAvailability(vm0.getPriority())).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);


        // selected is the host2
        allocationPolicy.preProcess();
        Assert.assertEquals(host2, allocationPolicy.selectHost(vm0));

        //remove host2 then the new selected is host1
        allocationPolicy.getHostList().remove(host2);
        allocationPolicy.preProcess();
        Assert.assertEquals(host1, allocationPolicy.selectHost(vm0));

        //remove host1 then the new selectedHost is host3
        allocationPolicy.getHostList().remove(host1);
        allocationPolicy.preProcess();
        Assert.assertEquals(host3, allocationPolicy.selectHost(vm0));

        // remove host3 then the new selectedHost is host4
        allocationPolicy.getHostList().remove(host3);
        allocationPolicy.preProcess();
        Assert.assertEquals(host4, allocationPolicy.selectHost(vm0));

        //insert all hosts again and return to the initial state where the selected host is host2

        allocationPolicy.getHostList().add(host1);
        allocationPolicy.getHostList().add(host2);
        allocationPolicy.getHostList().add(host3);
        allocationPolicy.preProcess();
        Assert.assertEquals(host2, allocationPolicy.selectHost(vm0));
    }
}