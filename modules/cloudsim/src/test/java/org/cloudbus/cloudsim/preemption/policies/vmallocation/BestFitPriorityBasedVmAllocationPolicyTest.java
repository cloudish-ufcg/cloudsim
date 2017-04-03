package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.PreemptiveHostComparator;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by jvmafra on 03/04/17.
 */
public class BestFitPriorityBasedVmAllocationPolicyTest {

    private SortedSet<PreemptiveHost> sortedHosts;
    private PreemptiveHost host1, host2, host3;
    private BestFitPriorityBasedVmAllocationPolicy preemptablePolicy;
    private static final int PRIORITY_0 = 0;
    private static final int PRIORITY_1 = 1;
    private static final int PRIORITY_2 = 2;
    private static final int NUMBER_OF_PRIORITIES = 3;
    private static final double ACCEPTABLE_DIFERENCE = 0.000001;
    private Properties properties;

    @Before
    public void setUp() {
        List<Pe> peList1 = new ArrayList<Pe>();
        peList1.add(new Pe(0, new PeProvisionerSimple(1)));

        properties = new Properties();
        properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");

        host1 = new PreemptiveHost(1, peList1,
                new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

        List<Pe> peList2 = new ArrayList<Pe>();
        peList2.add(new Pe(0, new PeProvisionerSimple(3)));
        host2 = new PreemptiveHost(2, peList2,
                new VmSchedulerMipsBased(peList2), new FCFSBasedPreemptionPolicy(properties));

        List<Pe> peList3 = new ArrayList<Pe>();
        peList3.add(new Pe(0, new PeProvisionerSimple(5)));
        host3 = new PreemptiveHost(3, peList3,
                new VmSchedulerMipsBased(peList3), new FCFSBasedPreemptionPolicy(properties));

        List<PreemptiveHost> hosts = new ArrayList<PreemptiveHost>();
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);

        sortedHosts = new TreeSet<PreemptiveHost>(new PreemptiveHostComparator(0));

        for (PreemptiveHost googleHost : hosts) {
            sortedHosts.add(googleHost);
        }

        preemptablePolicy = new BestFitPriorityBasedVmAllocationPolicy(hosts);
    }

    @Test
    public void testSelectHostForVm() {
        PreemptableVm vm1 = new PreemptableVm(1, 1, 0.9, 1.0, 0, PRIORITY_0, 0);
        PreemptableVm vm2 = new PreemptableVm(1, 1, 2, 1.0, 0, PRIORITY_0, 0);
        PreemptableVm vm3 = new PreemptableVm(1, 1, 3, 1.0, 0, PRIORITY_0, 0);
        PreemptableVm vm4 = new PreemptableVm(1, 1, 4, 1.0, 0, PRIORITY_0, 0);
        PreemptableVm vm5 = new PreemptableVm(1, 1, 5.1, 1.0, 0, PRIORITY_0, 0);

        // host1 has 1 mips available is suitable for Vm1
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm1));

        // host1 has 1 mips available, but it's not suitable for Vm2. Host2 has 3 mips available and it's suitable
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm2));

        // host1 has 1 mips available, but it's not suitable for Vm3. Host2 has 3 mips available and it's suitable
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm3));

        // host3 has 1 mips available and it's suitable for Vm4
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm4));

        // Any host is suitable for this vm
        Assert.assertNull(preemptablePolicy.selectHost(vm5));
    }


    @Test
    public void testSelectHostForVm2() {
        PreemptableVm vm1 = new PreemptableVm(1, 1, 0.9, 1.0, 0, PRIORITY_0, 0);
        PreemptableVm vm2 = new PreemptableVm(1, 1, 2.2, 1.0, 0, PRIORITY_0, 0);
        PreemptableVm vm3 = new PreemptableVm(1, 1, 0.8, 1.0, 0, PRIORITY_0, 0);
        PreemptableVm vm4 = new PreemptableVm(1, 1, 4.1, 1.0, 0, PRIORITY_0, 0);

        // host1 has 1 mips available and it is suitable for Vm1.
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm1));

        // host3 has 5 mips available and it is suitable for Vm4
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm4));
        // allocating vm in this host
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4, host3));
        Assert.assertEquals(vm4.getHost(), host3);

        // new order of hosts by available mips (ascending order): host3 (0.9 mips), host1 (1 mips), host2 (3 mips)
        // host3 has 0.9 mips available is suitable for Vm1.
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm1));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1, host3));
        Assert.assertEquals(vm1.getHost(), host3);


        // new order of hosts by available mips (ascending order): host3 (0 mips), host1 (1 mips), host2 (3 mips)
        // host1 has 1 mips available is suitable for Vm3
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm3));


        // host2 has 3 mips available is suitable for Vm2
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm2));
        // allocating vm in this host
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host2));
        Assert.assertEquals(vm2.getHost(), host2);


        // new order of hosts by available mips (ascending order): host3 (0 mips), host2 (0.8 mips), host1 (1 mips)
        // host3 has 0.9 mips available is suitable for Vm3.
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm3));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3, host2));
        Assert.assertEquals(vm3.getHost(), host2);


    }

    @Test
    public void testSelectHostForVm3() {
        PreemptableVm vm1 = new PreemptableVm(1, 1, 0.9, 1.0, 0, PRIORITY_0, 0);
        PreemptableVm vm2 = new PreemptableVm(2, 1, 4.1, 1.0, 0, PRIORITY_1, 0);
        PreemptableVm vm3 = new PreemptableVm(3, 1, 2.8, 1.0, 0, PRIORITY_0, 0);
        PreemptableVm vm4 = new PreemptableVm(4, 1, 2, 1.0, 0, PRIORITY_2, 0);
        PreemptableVm vm5 = new PreemptableVm(5, 1, 1.1, 1.0, 0, PRIORITY_2, 0);

        // host1 has 1 mips available and it is suitable for Vm1.
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm1));

        // host3 has 5 mips available and it is suitable for Vm2
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm2));
        // allocating vm in this host
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host3));
        Assert.assertEquals(vm2.getHost(), host3);

        // natural order: host3: 0.9, host1: 1, host2: 3
        // order of hosts by available mips for vms with priority 0 is the same (host: 1, host2: 3, host: 5)
        // host1 has 1 mips available is suitable for Vm1.
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm1));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1, host1));
        Assert.assertEquals(vm1.getHost(), host1);


        // natural order: host1: 0.1, host3: 0.9, host2: 3
        // order of hosts by available mips for vms with priority 0 is the same (ascending order): (host: 0.1, host2: 3, host: 5)
        // host1 has 1 mips available is suitable for Vm3
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm3));


        // host2 has 3 mips available is suitable for Vm4
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm4));
        // allocating vm in this host
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host2));
        Assert.assertEquals(vm2.getHost(), host2);


        // natural order: host1 (0.1 mips), host3 (0.9 mips), host2 (1 mips)
        // order by priority 0: host1 (0.1 mips), host2 (3 mips), host3 (5 mips)
        // host2 has 3 mips available and it is suitable for Vm3.
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm3));


        Assert.assertNull(preemptablePolicy.selectHost(vm5));


    }

}