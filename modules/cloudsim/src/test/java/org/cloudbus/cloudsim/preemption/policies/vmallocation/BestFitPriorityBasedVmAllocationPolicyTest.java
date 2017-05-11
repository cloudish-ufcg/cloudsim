package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.PreemptiveHostComparator;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by jvmafra on 03/04/17.
 */
public class BestFitPriorityBasedVmAllocationPolicyTest {

    private SortedSet<PreemptiveHost> sortedHosts;
    private PreemptiveHost host1, host2, host3, host4;
    private BestFitPriorityBasedVmAllocationPolicy preemptablePolicy;
    private static final int PRIORITY_0 = 0;
    private static final int PRIORITY_1 = 1;
    private static final int PRIORITY_2 = 2;
    private Properties properties;
    private SimulationTimeUtil simulationTimeUtil;

    @Before
    public void setUp() {

        this.simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(simulationTimeUtil.clock()).thenReturn(0d);

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

        host4 = new PreemptiveHost(4, peList3,
                new VmSchedulerMipsBased(peList3), new FCFSBasedPreemptionPolicy(properties));

        List<PreemptiveHost> hosts = new ArrayList<PreemptiveHost>();
        hosts.add(host1);
        hosts.add(host2);
        hosts.add(host3);
        hosts.add(host4);

        sortedHosts = new TreeSet<PreemptiveHost>(new PreemptiveHostComparator(0));

        for (PreemptiveHost googleHost : hosts) {
            sortedHosts.add(googleHost);
        }

        preemptablePolicy = new BestFitPriorityBasedVmAllocationPolicy(hosts, simulationTimeUtil);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalHostList(){
        preemptablePolicy = new BestFitPriorityBasedVmAllocationPolicy(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalVmToSelect(){
        preemptablePolicy.selectHost(null);
    }

    @Test
    public void testSelectHostForVm() {
        PreemptableVm vm1 = new PreemptableVm(1, 1, 0.9, 1.0, 0, PRIORITY_0, 0, 1);
        PreemptableVm vm2 = new PreemptableVm(2, 1, 2, 1.0, 0, PRIORITY_0, 0, 1);
        PreemptableVm vm3 = new PreemptableVm(3, 1, 3, 1.0, 0, PRIORITY_0, 0, 1);
        PreemptableVm vm4 = new PreemptableVm(4, 1, 4, 1.0, 0, PRIORITY_0, 0, 1);
        PreemptableVm vm5 = new PreemptableVm(5, 1, 5.1, 1.0, 0, PRIORITY_0, 0, 1);

        // host1 has 1 mips available is suitable for Vm1
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm1));

        // host1 has 1 mips available, but it's not suitable for Vm2. Host2 has 3 mips available and it's suitable
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm2));

        // host1 has 1 mips available, but it's not suitable for Vm3. Host2 has 3 mips available and it's suitable
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm3));

        // host3 and host4 have 5 mips available and they are suitable for Vm4. Host3 must be selected because id3 < id4
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm4));

        // Any host is suitable for this vm
        Assert.assertNull(preemptablePolicy.selectHost(vm5));
    }


    @Test
    public void testSelectHostForVm2() {
        PreemptableVm vm1 = new PreemptableVm(1, 1, 0.9, 1.0, 0, PRIORITY_0, 0, 1);
        PreemptableVm vm2 = new PreemptableVm(1, 1, 2.2, 1.0, 0, PRIORITY_0, 0, 1);
        PreemptableVm vm3 = new PreemptableVm(1, 1, 0.8, 1.0, 0, PRIORITY_0, 0, 1);
        PreemptableVm vm4 = new PreemptableVm(1, 1, 4.1, 1.0, 0, PRIORITY_0, 0, 1);

        // host1 has 1 mips available and it is suitable for Vm1.
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm1));

        // host3 and host4 have 5 mips available and they are suitable for Vm4. Host3 must be selected
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm4));
        // allocating vm in this host
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4, host3));
        Assert.assertEquals(vm4.getHost(), host3);

        // new order of hosts by available mips (ascending order): host3 (0.9 mips), host1 (1 mips), host2 (3 mips), host4 (5 mips)
        // host3 has 0.9 mips available is suitable for Vm1.
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm1));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1, host3));
        Assert.assertEquals(vm1.getHost(), host3);


        // new order of hosts by available mips (ascending order): host3 (0 mips), host1 (1 mips), host2 (3 mips), host4 (5 mips)
        // host1 has 1 mips available is suitable for Vm3
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm3));


        // host2 has 3 mips available is suitable for Vm2
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm2));
        // allocating vm in this host
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host2));
        Assert.assertEquals(vm2.getHost(), host2);


        // new order of hosts by available mips (ascending order): host3 (0 mips), host2 (0.8 mips), host1 (1 mips), host4 (5 mips)
        // host3 has 0.9 mips available is suitable for Vm3.
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm3));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3, host2));
        Assert.assertEquals(vm3.getHost(), host2);


    }

    @Test
    public void testSelectHostForVm3() {
        PreemptableVm vm1 = new PreemptableVm(1, 1, 0.9, 1.0, 0, PRIORITY_0, 0, 1);
        PreemptableVm vm2 = new PreemptableVm(2, 1, 4.1, 1.0, 0, PRIORITY_1, 0, 0.9);
        PreemptableVm vm3 = new PreemptableVm(3, 1, 2.8, 1.0, 0, PRIORITY_0, 0, 1);
        PreemptableVm vm4 = new PreemptableVm(4, 1, 2, 1.0, 0, PRIORITY_2, 0, 0.5);
        PreemptableVm vm5 = new PreemptableVm(5, 1, 5.1, 1.0, 0, PRIORITY_2, 0, 0.5);
        PreemptableVm vm6 = new PreemptableVm(5, 1, 3, 1.0, 0, PRIORITY_1, 0, 0.9);

        // host1 has 1 mips available and it is suitable for Vm1.
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm1));

        // host3 and host4 have 5 mips available and they are suitable for Vm2. Host3 must be selected
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm2));
        // allocating vm in this host
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host3));
        Assert.assertEquals(vm2.getHost(), host3);

        // natural order: host3: 0.9, host1: 1, host2: 3
        // order of hosts by available mips for vms with priority 0 is the same (host: 1, host2: 3, host3: 5, host4: 5)
        // host1 has 1 mips available is suitable for Vm1.
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm1));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1, host1));
        Assert.assertEquals(vm1.getHost(), host1);


        // natural order: host1: 0.1, host3: 0.9, host2: 3, host4: 5 mips
        // order of hosts by available mips for vms with priority 0 is the same (ascending order): (host1: 0.1, host2: 3, host3: 5, host4: 5)
        // host1 has 1 mips available is suitable for Vm3
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm3));


        // host2 has 3 mips available is suitable for Vm4
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm4));
        // allocating vm in this host
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4, host2));
        Assert.assertEquals(vm4.getHost(), host2);


        // natural order: host1 (0.1 mips), host3 (0.9 mips), host2 (1 mips), host4 (5 mips)
        // order by priority 0: host1 (0.1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // host2 has 3 mips available and it is suitable for Vm3.
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm3));

        // any host is suitable for vm with priority 2 and cpuReq = 5.1
        Assert.assertNull(preemptablePolicy.selectHost(vm5));

        // natural order: host1 (0.1 mips), host3 (0.9 mips), host2 (1 mips), host4 (5 mips)
        // order by priority 1: host1 (0.1 mips), host3 (0.9 mips), host2 (3 mips), host4 (5 mips)
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm6));

    }

    @Test
    public void testSelectHostForVm4() {

        int PROD = 0;

        int id = 0;
        int userId = 0;
        double memReq = 1.0;
        double submitTime = 0;
        double runtime = 0;

        double cpuReq = 2.0;
        PreemptableVm vm0 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm1 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm2 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm3 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);

        cpuReq = 0.9;
        PreemptableVm vm4 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);

        cpuReq = 0.1;

        PreemptableVm vm5 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);


        // natural order: host1 (1 mips), host2 (3 mips), host4 (5 mips), host3 (5 mips)
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm4)); // vm request 0.9 mips
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm5)); // vm request 0.1 mips
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm0)); // vm request 2.0 mips

        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm0, host4)); // allocate vm0 in host4
        Assert.assertEquals(vm0.getHost(), host4);

        // natural order: host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (3 mips)
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm1)); // vm request 2.0 mips lesser id prevails
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1)); // allocate vm1 in host2
        Assert.assertEquals(vm1.getHost(), host2);

        // natural order: host1 (1 mips), host2 (1 mips), host4 (3 mips), host3 (5 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm5, host2)); // allocate vm9 in host2
        Assert.assertEquals(vm5.getHost(), host2);

        // natural order: host2 (0.9 mips), host1 (1 mips), host4 (3 mips), host3 (5 mips)
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm4));

        preemptablePolicy.deallocateHostForVm(vm5);// deallocate vm9(0.1 mips) to host2
        Assert.assertEquals(vm5.getHost(), null);

        // natural order: host1 (1 mips), host2 (1 mips), host4 (3 mips), host3 (5 mips)
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm4));
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm5));
        Assert.assertEquals(host4, preemptablePolicy.selectHost(vm2));

        // allocate vms to change host3 available mips to 2
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4, host3));
        Assert.assertEquals(vm4.getHost(), host3);
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm5, host3));
        Assert.assertEquals(vm5.getHost(), host3);
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host3));
        Assert.assertEquals(vm2.getHost(), host3);

        // natural order: host1 (1 mips), host2 (1 mips), host3 (2 mips), host4 (3 mips)
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm3));
    }

    @Test
    public void testAllocateHostForVm5(){

        int PROD = 0;

        int id = 0;
        int userId = 0;
        double memReq = 1.0;
        double submitTime = 0;
        double runtime = 0;

        double cpuReq = 2.0;
        PreemptableVm vm0 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm1 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm2 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm3 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm4 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);

        cpuReq = 0.9;
        PreemptableVm vm5 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm6 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm7 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm8 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);

        cpuReq = 0.1;

        PreemptableVm vm9 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm10 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm11 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);
        PreemptableVm vm12 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, PROD, runtime, 1);


        // natural order: host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm0));
        Assert.assertEquals(vm0.getHost(), host2);

        // natural order: host1 (1 mips), host2 (1 mips), host3 (5 mips), host4 (5 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
        Assert.assertEquals(vm1.getHost(), host3);

        // natural order: host1 (1 mips), host2 (1 mips), host3 (3 mips), host4 (5 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2));
        Assert.assertEquals(vm2.getHost(), host3);

        // natural order: host1 (1 mips), host2 (1 mips), host3 (1 mips), host4 (5 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3));
        Assert.assertEquals(vm3.getHost(), host4);

        // natural order: host1 (1 mips), host2 (1 mips), host3 (1 mips), host4 (3 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4));
        Assert.assertEquals(vm4.getHost(), host4);

        // natural order: host1 (1 mips), host2 (1 mips), host3 (1 mips), host4 (1 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm5));
        Assert.assertEquals(vm5.getHost(), host1);

        // natural order: host1 (0.9 mips), host2 (1 mips), host3 (1 mips), host4 (1 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm6));
        Assert.assertEquals(vm6.getHost(), host2);

        // natural order: host1 (0.9 mips), host2 (0.9 mips), host3 (1 mips), host4 (1 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm7));
        Assert.assertEquals(vm7.getHost(), host3);

        // natural order: host1 (0.9 mips), host2 (0.9 mips), host3 (0.9 mips), host4 (1 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm8));
        Assert.assertEquals(vm8.getHost(), host4);

        // natural order: host1 (0.9 mips), host2 (0.9 mips), host3 (0.9 mips), host4 (0.9 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm9));
        Assert.assertEquals(vm9.getHost(), host1);

        // natural order: host1 (0 mips), host2 (0.9 mips), host3 (0.9 mips), host4 (0.9 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm10));
        Assert.assertEquals(vm10.getHost(), host2);

        // natural order: host1 (0 mips), host2 (0 mips), host3 (0.9 mips), host4 (0.9 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm11));
        Assert.assertEquals(vm11.getHost(), host3);

        // natural order: host1 (0 mips), host2 (0 mips), host3 (0 mips), host4 (0.9 mips)
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm12));
        Assert.assertEquals(vm12.getHost(), host4);

        // natural order: host1 (0 mips), host2 (0 mips), host3 (0 mips), host4 (0 mips)
    }

    @Test
    public void testDeallocateVMNonExistent() {
        PreemptableVm vm1 = new PreemptableVm(1, 1, 1.0, 1.0, 0, 0, 0, 1);
        preemptablePolicy.deallocateHostForVm(vm1);

        Assert.assertTrue(host1.getVmList().isEmpty());
        Assert.assertTrue(host2.getVmList().isEmpty());
        Assert.assertTrue(host3.getVmList().isEmpty());
        Assert.assertTrue(host4.getVmList().isEmpty());
    }

    @Test
    public void testDeallocateVMNonexistentAfterDeallocateExistingVM() {
        PreemptableVm vm1 = new PreemptableVm(1, 1, 5.0, 1.0, 0, 0, 0, 1);

//		// mocking host selector
//		Mockito.when(hostSelector.select(sortedHosts, vm1)).thenReturn(host1);

        // checking
        Assert.assertTrue(host1.getVmList().isEmpty());
        Assert.assertTrue(host2.getVmList().isEmpty());
        Assert.assertTrue(host3.getVmList().isEmpty());
        Assert.assertTrue(host4.getVmList().isEmpty());

        // allocate vm in host 3 that fits exactly
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1));
        Assert.assertEquals(host3, vm1.getHost());
        Assert.assertEquals(1, host3.getVmList().size());
        Assert.assertTrue(host1.getVmList().isEmpty());
        Assert.assertTrue(host2.getVmList().isEmpty());
        Assert.assertTrue(host3.getVmList().contains(vm1));
        Assert.assertTrue(host4.getVmList().isEmpty());

        // deallocate existing VM1
        preemptablePolicy.deallocateHostForVm(vm1);
        Assert.assertTrue(host1.getVmList().isEmpty());
        Assert.assertTrue(host2.getVmList().isEmpty());
        Assert.assertTrue(host3.getVmList().isEmpty());
        Assert.assertTrue(host4.getVmList().isEmpty());

        // trying to deallocate VM1 again
        preemptablePolicy.deallocateHostForVm(vm1);
        Assert.assertTrue(host1.getVmList().isEmpty());
        Assert.assertTrue(host2.getVmList().isEmpty());
        Assert.assertTrue(host3.getVmList().isEmpty());
        Assert.assertTrue(host4.getVmList().isEmpty());
    }

    @Test
    public void testPreemptionAndDestroy(){

        int PROD = 0;
        int BATCH = 1;
        int FREE = 2;

        int id = 0;
        int userId = 0;
        double memReq = 1.0;
        double submitTime = 0;
        double runtime = 0;

        int priority = BATCH;
        double availabilityTarget = 0.9;

        double cpuReq = 0.499999998;
		PreemptableVm vm0 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        cpuReq = 3.0;
        PreemptableVm vm1 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        cpuReq = 2.999999999;
        PreemptableVm vm2 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        cpuReq = 2.000000001;
        PreemptableVm vm3 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        priority = FREE;
        availabilityTarget = 0.5;
        PreemptableVm vm4 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        cpuReq = 0.000000002;
        PreemptableVm vm5 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        cpuReq = 1.0;
        PreemptableVm vm6 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        priority = PROD;
        availabilityTarget = 1;
        PreemptableVm vm7 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        cpuReq = 3.0;
        PreemptableVm vm8 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        cpuReq = 5.0;
        PreemptableVm vm9 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);
        PreemptableVm vm10 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);


        // natural order(PROD): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(FREE): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm0));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm0, host1));
        Assert.assertEquals(host1, vm0.getHost());

        // natural order(PROD): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host1 (0.000000002 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(FREE): host1 (0.000000002 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm1));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm1, host2));
        Assert.assertEquals(host2, vm1.getHost());

        // natural order(PROD): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host2 (0 mips), host1 (0.000000002 mips), host3 (5 mips), host4 (5 mips)
        // natural order(FREE): host2 (0 mips), host1 (0.000000002 mips), host3 (5 mips), host4 (5 mips)
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm2));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm2, host3));
        Assert.assertEquals(host3, vm2.getHost());

        // natural order(PROD): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host2 (0 mips), host1 (0.000000002 mips), host3 (2.000000001 mips), host4 (5 mips)
        // natural order(FREE): host2 (0 mips), host1 (0.000000002 mips), host3 (2.000000001 mips), host4 (5 mips)
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm3));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm3, host3));
        Assert.assertEquals(host3, vm3.getHost());

        // natural order(PROD): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host2 (0 mips), host1 (0.000000002 mips), host3 (2.000000001 mips), host4 (5 mips)
        // natural order(FREE): host2 (0 mips), host3 (0 mips), host1 (0.000000002 mips), host4 (5 mips)
        Assert.assertEquals(host4, preemptablePolicy.selectHost(vm4));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm4, host4));
        Assert.assertEquals(host4, vm4.getHost());

        // natural order(PROD): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host2 (0 mips), host1 (0.000000002 mips), host3 (2.000000001 mips), host4 (5 mips)
        // natural order(FREE): host2 (0 mips), host3 (0 mips), host1 (0.000000002 mips), host4 (3 mips)
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm5));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm5, host1));
        Assert.assertEquals(host1, vm5.getHost());

        // natural order(PROD): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host2 (0 mips), host1 (0.000000002 mips), host3 (2.000000001 mips), host4 (5 mips)
        // natural order(FREE): host1 (0 mips), host2 (0 mips), host3 (0 mips), host4 (3 mips)
        Assert.assertEquals(host4, preemptablePolicy.selectHost(vm6));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm6,host4));
        Assert.assertEquals(host4, vm6.getHost());

        // natural order(PROD): host1 (1 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host2 (0 mips), host1 (0.000000002 mips), host3 (2.000000001 mips), host4 (5 mips)
        // natural order(FREE): host1 (0 mips), host2 (0 mips), host3 (0 mips), host4 (2 mips)
        Assert.assertEquals(host1, preemptablePolicy.selectHost(vm7));
        
        /*
         * vm7 can be allocated but it will preempt vm0 and vm5
         */
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm7, host1));
        Assert.assertEquals(host1, vm7.getHost());
        Assert.assertNull(vm0.getHost());
        Assert.assertNull(vm5.getHost());

        // natural order(PROD): host1 (0 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host1 (0 mips), host2 (0 mips), host3 (2.000000001 mips), host4 (5 mips)
        // natural order(FREE): host1 (0 mips), host2 (0 mips), host3 (0 mips), host4 (2 mips)
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm9));
        
        /*
         * vm9 can be allocated but it will preempt vm2 and vm3. For here, vm9 can not be created in host3
         */
        Assert.assertFalse(host3.vmCreate(vm9));
        
        // deallocating vms
        preemptablePolicy.deallocateHostForVm(vm2);
        preemptablePolicy.deallocateHostForVm(vm3);
        Assert.assertNull(vm2.getHost());
        Assert.assertNull(vm3.getHost());
        
        // natural order(PROD): host1 (0 mips), host2 (3 mips), host3 (5 mips), host4 (5 mips)
        // natural order(BATCH): host1 (0 mips), host2 (0 mips), host3 (5 mips), host4 (5 mips)
        // natural order(FREE): host1 (0 mips), host2 (0 mips), host4 (2 mips), host3 (5 mips)
        Assert.assertEquals(host3, preemptablePolicy.selectHost(vm9));
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm9, host3));
        Assert.assertEquals(host3, vm9.getHost());
        
        // natural order(PROD): host1 (0 mips), host3 (0 mips), host2 (3 mips), host4 (5 mips)
        // natural order(BATCH): host1 (0 mips), host2 (0 mips), host3 (0 mips), host4 (5 mips)
        // natural order(FREE): host1 (0 mips), host2 (0 mips), host3 (0 mips), host4 (2 mips)
        Assert.assertEquals(host2, preemptablePolicy.selectHost(vm8));
        
        /*
         * vm8 can be allocated but it will preempt vm1
         */
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm8, host2));
        Assert.assertEquals(host2, vm8.getHost());
        Assert.assertNull(vm1.getHost());

        // natural order(PROD): host1 (0 mips), host2 (0 mips),  host3 (0 mips), host4 (5 mips)
        // natural order(BATCH): host1 (0 mips), host2 (0 mips), host3 (0 mips), host4 (5 mips)
        // natural order(FREE): host1 (0 mips), host2 (0 mips), host3 (0 mips), host4 (2 mips)
        Assert.assertEquals(host4, preemptablePolicy.selectHost(vm10));
        
        /*
         * vm10 can be allocated but it will preempt vm4 and vm6
         */
        Assert.assertTrue(preemptablePolicy.allocateHostForVm(vm10, host4));
        Assert.assertEquals(host4, vm10.getHost());
        Assert.assertNull(vm4.getHost());
        Assert.assertNull(vm6.getHost());
    }
}