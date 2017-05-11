package org.cloudbus.cloudsim.preemption.policies.preemption;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.preemption.*;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by Alessandro Lia Fook Santos on 11/05/17.
 */
public class CapacityCostBasedPreemptionPolicyTest {


    private static final int PROD = 0;
    private static final int BATCH = 1;
    private static final int FREE = 2;
    private static final double ACCEPTABLE_DIFFERENCE = 0.0000001;

    SimulationTimeUtil simulationTimeUtil;
    PreemptiveHost host;
    PreemptionPolicy preemptionPolicy;
    SortedSet<CapacityCost> costs;

    @Before
    public void setUp() {

        simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);

        host = Mockito.mock(PreemptiveHost.class);
        Mockito.when(host.getId()).thenReturn(0);
        Mockito.when(host.getAvailableMips()).thenReturn(0.2);

        preemptionPolicy = new CapacityCostBasedPreemptionPolicy();
        preemptionPolicy.setSimulationTimeUtil(simulationTimeUtil);
        preemptionPolicy.setHost(host);

        costs = new TreeSet<>();
    }

    @Test
    public void allocating() throws Exception {

        Mockito.when(simulationTimeUtil.clock()).thenReturn(0d);
        costs = preemptionPolicy.getCapacityCosts(0d, 0.5);

        Assert.assertEquals(1, costs.size());
        Assert.assertEquals(0.2, costs.first().getCapacity(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, costs.first().getCost(), ACCEPTABLE_DIFFERENCE);

        int id = 0;
        int userId = 0;
        double cpuReq = 0.2;
        double memReq = 0.1;
        double submitTime = 0;
        int priority = PROD;
        double runtime = 2;
        double availabilityTarget = 1;

        PreemptableVm vm1 = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);
        preemptionPolicy.allocating(vm1);

        costs = preemptionPolicy.getCapacityCosts(0d, 0.5);
        Assert.assertEquals(2, costs.size());

        Assert.assertEquals(0.2, costs.first().getCapacity(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, costs.first().getCost(), ACCEPTABLE_DIFFERENCE);

        Assert.assertEquals(0.4, costs.last().getCapacity(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(Double.MAX_VALUE, costs.last().getCost(), ACCEPTABLE_DIFFERENCE);

        priority = BATCH;
        cpuReq = 0.1;
        PreemptableVm vm2 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

        preemptionPolicy.allocating(vm2);
        costs = preemptionPolicy.getCapacityCosts(0d, 0.5);

        Iterator<CapacityCost> iterator = costs.iterator();
        CapacityCost cost = iterator.next();

        costs = preemptionPolicy.getCapacityCosts(0d, 0.5);
        Assert.assertEquals(3, costs.size());

        Assert.assertEquals(0.2, cost.getCapacity(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, cost.getCost(), ACCEPTABLE_DIFFERENCE);

        cost = iterator.next();
        Assert.assertEquals(0.3, cost.getCapacity(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(6, cost.getCost(), ACCEPTABLE_DIFFERENCE);

        cost = iterator.next();
        Assert.assertEquals(0.4, cost.getCapacity(), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(Double.MAX_VALUE, cost.getCost(), ACCEPTABLE_DIFFERENCE);

        priority = FREE;
        cpuReq = 0.3;
        PreemptableVm vm3 = new PreemptableVm(id++, userId, cpuReq, memReq, submitTime, priority, runtime, availabilityTarget);

    }

    @Test
    public void isSuitableFor() throws Exception {
    }

    @Test
    public void nextVmForPreempting() throws Exception {
    }

    @Test
    public void getCapacityCosts() throws Exception {
    }

    @Test
    public void deallocating() throws Exception {
    }

}