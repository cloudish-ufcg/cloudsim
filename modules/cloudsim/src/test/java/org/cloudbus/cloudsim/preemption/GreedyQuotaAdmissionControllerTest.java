package org.cloudbus.cloudsim.preemption;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Alessandro Lia Fook Santos on 08/02/17.
 */
public class GreedyQuotaAdmissionControllerTest {

    private GreedyQuotaAdmissionController admController;
    private Map<Integer, Double> sloTargets;
    private static final double ACCEPTABLE_DIFFERENCE = 0.000001;

    private static final int PROD = 0;
    private static final int BATCH = 1;
    private static final int  FREE = 2;


    @Before
    public void setUp() {

        sloTargets = new HashMap<Integer, Double>();
        sloTargets.put(new Integer(PROD), new Double(1));
        sloTargets.put(new Integer(BATCH), new Double(0.9));
        sloTargets.put(new Integer(FREE), new Double(0.5));

        admController = new GreedyQuotaAdmissionController(100, sloTargets, 1);
    }

    @Test
    public void testCalculateQuota01(){

        Map<Integer, Double> admitedRequests = new HashMap<Integer, Double>();

        admitedRequests.put(PROD, 0d);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 0d);

        admController.calculateQuota(admitedRequests);
        Assert.assertEquals(100, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(90, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(50, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

    }

    @Test
    public void testCalculateQuota02(){

        Map<Integer, Double> admitedRequests = new HashMap<Integer, Double>();

        admitedRequests.put(PROD, 10.0);
        admitedRequests.put(BATCH, 10.0);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(90, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(72, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(35, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void testCalculateQuota03(){

        Map<Integer, Double> admitedRequests = new HashMap<Integer, Double>();

        admitedRequests.put(PROD, 0d);
        admitedRequests.put(BATCH, 20.0);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(72, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(35, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 0d);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 30.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(90, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(35, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 30.0);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 0d);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(70, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(63, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(35, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 20.0);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(80, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(72, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(35, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void calculateQuota04(){

        Map<Integer, Double> admitedRequests = new HashMap<Integer, Double>();

        admitedRequests.put(PROD, 0.5);
        admitedRequests.put(BATCH, 20.0);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(99.5, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(71.55, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(34.75, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 0.0625);
        admitedRequests.put(BATCH, 0.03125);
        admitedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(99.9375, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(89.915625, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(49.946875, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 0.625);
        admitedRequests.put(BATCH, 0.03125);
        admitedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(99.375, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(89.409375, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(49.665625, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);
    }


    @Test
    public void testAccept01(){

        int id = 0;
        int userId = 0;
        double cpuReq = 99;
        double memReq = 0;
        double submitTime = 0;
        int priority = 0;
        double runtime = 10;

        PreemptableVm vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm));

        priority = 1;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));

        priority = 2;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
    }


    @Test
    public void testAccept02(){

        int userId = 0;
        double memReq = 0;
        double submitTime = 0;
        double runtime = 10;

        int priority = 0;
        PreemptableVm vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(1, userId, 70, memReq, submitTime, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(2, userId, 30, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm));
        Assert.assertTrue(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        priority = 1;
        vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 70, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 30, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertTrue(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        priority = 2;
        vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 70, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 30, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertFalse(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        Map<Integer, Double> admitedRequests = new HashMap<Integer, Double>();

        admitedRequests.put(PROD, 0.5);
        admitedRequests.put(BATCH, 20.0);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(99.5, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(71.55, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(34.75, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        priority = 0;
        vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 70, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 30, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertTrue(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        priority = 1;
        vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 70, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 30, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertTrue(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        priority = 2;
        vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 70, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 30, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertFalse(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        admitedRequests.put(PROD, 0.0625);
        admitedRequests.put(BATCH, 0.03125);
        admitedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(99.9375, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(89.915625, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(49.946875, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        priority = 0;
        vm = new PreemptableVm(0, userId, 99.94, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 89.92, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 49.95, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertTrue(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        priority = 1;
        vm = new PreemptableVm(0, userId, 99.94, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 89.92, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 49.95, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertFalse(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        priority = 2;
        vm = new PreemptableVm(0, userId, 99.94, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 89.92, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 49.95, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertFalse(admController.accept(vm2));
        Assert.assertFalse(admController.accept(vm3));

        admitedRequests.put(PROD, 0.625);
        admitedRequests.put(BATCH, 0.03125);
        admitedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(99.375, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(89.409375, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(49.665625, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        priority = 0;
        vm = new PreemptableVm(0, userId, 99.37, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 89.409375, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 49.665625, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm));
        Assert.assertTrue(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        priority = 1;
        vm = new PreemptableVm(0, userId, 99.37, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 89.409375, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 49.665625, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertTrue(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));

        priority = 2;
        vm = new PreemptableVm(0, userId, 99.37, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 89.409375, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 49.665625, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));
        Assert.assertFalse(admController.accept(vm2));
        Assert.assertTrue(admController.accept(vm3));
    }
}