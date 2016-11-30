package org.cloudbus.cloudsim.preemption.policies.hostselection;

import junit.framework.Assert;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

/**
 * Created by Alessandro Lia Fook Santos on 23/11/16.
 */
public class WorstFitVmAvailabilityBasedHostSelectionPolicyTest {

    private static final double HOST_CAPACITY = 0.5;
    public final double ACCEPTABLE_DIFFERENCE = 0.00001;

    public List<PreemptiveHost> hosts;
    public HostSelectionPolicy selectionPolicyWFVA;

    public VmAvailabilityBasedPreemptionPolicy preemptionPolicy1;
    public VmAvailabilityBasedPreemptionPolicy preemptionPolicy2;
    public VmAvailabilityBasedPreemptionPolicy preemptionPolicy3;
    public VmAvailabilityBasedPreemptionPolicy preemptionPolicy4;

    public PreemptiveHost host1;
    public PreemptiveHost host2;
    public PreemptiveHost host3;
    public PreemptiveHost host4;

    @Before
    public void setUp() throws Exception {


        // environment with VmAvailabilityBasedPreemptionPolicy

        preemptionPolicy1 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        preemptionPolicy2 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        preemptionPolicy3 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);
        preemptionPolicy4 = Mockito.mock(VmAvailabilityBasedPreemptionPolicy.class);

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

        selectionPolicyWFVA = new WorstFitVmAvailabilityBasedHostSelectionPolicy(hosts);
    }

    //tests for environment with VmAvailabilityBasedPreemptionPolicy
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
        selectionPolicyWFVA.select(new TreeSet<>(), null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInsertIllegalListOfHosts(){
        selectionPolicyWFVA.select(null, null);
    }

    @Test
    public void testSelectHostForVM(){
        double cpuReq = 0.5;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(10.0);

        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host1);
    }

    @Test
    public void testSelectHostForVM2(){
        double cpuReq = 0.5;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(5.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(5.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(5.0);

        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host2);
    }

    @Test
    public void testSelectHostForVM3(){
        double cpuReq = 0.5;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(5.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(9.9);
        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(10.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(1.0);

        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host3);
    }

    @Test
    public void testSelectHostForVM4(){
        double cpuReq = 0.5;
        double memReq = 0;
        double runtime = 10;

        PreemptableVm vm0 = new PreemptableVm(0, 1, cpuReq, memReq, 0, 0, runtime);

        Mockito.when(preemptionPolicy1.getAvailableMipsByVm(vm0)).thenReturn(1.0);
        Mockito.when(preemptionPolicy2.getAvailableMipsByVm(vm0)).thenReturn(1.0);
        Mockito.when(preemptionPolicy3.getAvailableMipsByVm(vm0)).thenReturn(1.0);
        Mockito.when(preemptionPolicy4.getAvailableMipsByVm(vm0)).thenReturn(1.0000000001);

        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host4);
    }

    @Test
    public void testSelectHostForVM5(){
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

        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), null);
    }

    @Test
    public void testSelectHostForVM6(){
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

        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host3);
    }

    @Test
    public void testSelectHostForVM7(){
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

        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host3);
    }

    @Test
    public void testSelectHostForVM8(){

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


        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), null);
    }

    @Test
    public void testSelectHostForVM9(){
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

        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host2);

        selectionPolicyWFVA.removeHost(host2);
        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host1);

        selectionPolicyWFVA.removeHost(host1);
        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host3);

        selectionPolicyWFVA.removeHost(host3);
        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host4);

        selectionPolicyWFVA.addHost(host1);
        selectionPolicyWFVA.addHost(host2);
        selectionPolicyWFVA.addHost(host3);
        Assert.assertEquals(selectionPolicyWFVA.select(null, vm0), host2);
    }
}