package org.cloudbus.cloudsim.preemption.policies.hostselection;

import junit.framework.Assert;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Alessandro Lia Fook Santos on 23/11/16.
 */
public class WorstFitVmAvailabilityBasedHostSelectionPolicyTest {

    private static final double HOST_CAPACITY = 0.5;

    List<PreemptiveHost> hosts;
    HostSelectionPolicy selectionPolicy;
    VmAvailabilityBasedPreemptionPolicy preemptionPolicy1;
    VmAvailabilityBasedPreemptionPolicy preemptionPolicy2;
    VmAvailabilityBasedPreemptionPolicy preemptionPolicy3;
    VmAvailabilityBasedPreemptionPolicy preemptionPolicy4;

    PreemptiveHost host1;
    PreemptiveHost host2;
    PreemptiveHost host3;
    PreemptiveHost host4;

    @Before
    public void setUp() throws Exception {

        int hostId = 0;

        preemptionPolicy1 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        preemptionPolicy2 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        preemptionPolicy3 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        preemptionPolicy4 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);

        hosts = new ArrayList<>();

        List<Pe> peList1 = new ArrayList<>();

        peList1.add(new Pe(0, new PeProvisionerSimple(HOST_CAPACITY)));


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

        selectionPolicy = new WorstFitVmAvailabilityBasedHostSelectionPolicy(hosts);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidInitialization(){
        new WorstFitVmAvailabilityBasedHostSelectionPolicy(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidInitialization2(){
        new WorstFitVmAvailabilityBasedHostSelectionPolicy(new ArrayList<>());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInsertIllegalVM(){
        selectionPolicy.select(new TreeSet<>(), null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInsertIllegalVM2(){
        selectionPolicy.select(null, null);
    }

    @Test
    public void testInsertAnyVM(){
        double cpuReq = 0.5;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(10.0);

        Assert.assertEquals(selectionPolicy.select(null, vm0), host1);
    }

    @Test
    public void testInsertAnyVM2(){
        double cpuReq = 0.5;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(5.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(5.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(5.0);

        Assert.assertEquals(selectionPolicy.select(null, vm0), host2);
    }

    @Test
    public void testInsertAnyVM3(){
        double cpuReq = 0.5;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(5.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(9.9);
        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(1.0);

        Assert.assertEquals(selectionPolicy.select(null, vm0), host3);
    }

    @Test
    public void testInsertAnyVM4(){
        double cpuReq = 0.5;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(1.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(1.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(1.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(1.0000000001);

        Assert.assertEquals(selectionPolicy.select(null, vm0), host4);
    }

    @Test
    public void testInsertAnyVM5(){
        double cpuReq = 0.5;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(1.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(1.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(1.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(1.0000000001);

        Assert.assertEquals(selectionPolicy.select(null, vm0), host4);
    }

    @Test
    public void testInsertAnyVM6(){
        double cpuReq = 0.6;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(0.4);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(0.4);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(0.4);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);

        Assert.assertEquals(selectionPolicy.select(null, vm0), null);
    }

    @Test
    public void testInsertAnyVM7(){
        double cpuReq = 0.0000001;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(0.00000002);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);

        Assert.assertEquals(selectionPolicy.select(null, vm0), host3);
    }

    @Test
    public void testInsertAnyVM8(){
        double cpuReq = 0.0000001;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(0.00000001);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);

        Assert.assertEquals(selectionPolicy.select(null, vm0), host3);
    }

    @Test
    public void testInsertAnyVM9(){

        double cpuReq = 0.0000001;
        double memReq = 0;
        double runtime = 10;
        int id = 0;

        PreemptableVm vm0 = new PreemptableVm(id, 1, cpuReq, memReq, 0, 0, runtime);

        cpuReq = 0.499999996;
        PreemptableVm vm1 = new PreemptableVm(id++, 1, cpuReq, memReq, 0, 0, runtime);
        host1.vmCreate(vm1); // create the vm into the host to reduce his available mips

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(0.000000004);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(0.000000002);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);

        Assert.assertEquals(selectionPolicy.select(null, vm0), null);
    }

    @Test
    public void testInsertAnyVM10(){
        double cpuReq = 0.0000001;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(0.0000002);
        Mockito.when(preemptionPolicy1.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(0.0000004);
        Mockito.when(preemptionPolicy2.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(0.0000001);
        Mockito.when(preemptionPolicy3.isSuitableFor(vm0)).thenReturn(false);

        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(0.0000000001);
        Mockito.when(preemptionPolicy4.isSuitableFor(vm0)).thenReturn(false);

        Assert.assertEquals(selectionPolicy.select(null, vm0), host2);

        selectionPolicy.removeHost(host2);
        Assert.assertEquals(selectionPolicy.select(null, vm0), host1);

        selectionPolicy.removeHost(host1);
        Assert.assertEquals(selectionPolicy.select(null, vm0), host3);

        selectionPolicy.addHost(host1);
        selectionPolicy.addHost(host2);
        Assert.assertEquals(selectionPolicy.select(null, vm0), host2);
    }
}