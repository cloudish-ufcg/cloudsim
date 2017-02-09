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
        Assert.assertEquals(111.11111111111, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

    }

    @Test
    public void testCalculateQuota02(){

        Map<Integer, Double> admittedRequests = new HashMap<Integer, Double>();

        admittedRequests.put(PROD, 10.0);
        admittedRequests.put(BATCH, 10.0);
        admittedRequests.put(FREE, 10.0);

        admController.calculateQuota(admittedRequests);

        Assert.assertEquals(90, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.888888889, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(140, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void testCalculateQuota03(){

        Map<Integer, Double> admitedRequests = new HashMap<Integer, Double>();

        admitedRequests.put(PROD, 0d);
        admitedRequests.put(BATCH, 20.0);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.888888889, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(140, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 0d);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 30.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.11111111111, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(140, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 30.0);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 0d);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(70, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(77.777777778, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(140, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 20.0);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(80, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.888888889, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(140, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void calculateQuota04(){

        Map<Integer, Double> admitedRequests = new HashMap<Integer, Double>();

        admitedRequests.put(PROD, 0.5);
        admitedRequests.put(BATCH, 20.0);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(99.5, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.333333333, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 0.0625);
        admitedRequests.put(BATCH, 0.03125);
        admitedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(99.9375, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.006944444, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 0.625);
        admitedRequests.put(BATCH, 0.03125);
        admitedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(99.375, admController.getQuotaByPriority().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.381944444, admController.getQuotaByPriority().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(2), ACCEPTABLE_DIFFERENCE);
    }


    @Test
    public void testAccept(){

        int id = 0;
        int userId = 0;
        double cpuReq = 99;
        double memReq = 0;
        double submitTime = 0;
        int priority = 0;
        double runtime = 10;

        PreemptableVm vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm));

        //checking values of quotaByPriority
        Assert.assertEquals(1, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        priority = 1;
        cpuReq = 111.111112111;
        // Quota for priority 1 is 100 / 0.9 = 111,111111111
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));

        //checking values of quotaByPriority
        Assert.assertEquals(1, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        cpuReq = 111.111111111;
        // Quota for priority 1 is 100 / 0.9 = 111,111111111
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm));

        //checking values of quotaByPriority
        Assert.assertEquals(1, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        priority = 2;
        cpuReq = 200.0000001;
        // Quota for priority 2 is 100 / 0.5 = 200
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm));

        //checking values of quotaByPriority
        Assert.assertEquals(1, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        cpuReq = 199.00000009;
        // Quota for priority 2 is 100 / 0.5 = 200
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm));

        //checking values of quotaByPriority
        Assert.assertEquals(1, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.00000001, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
    }


    @Test
    public void testRelease01() {
        int id = 0;
        int userId = 0;
        double cpuReq = 99;
        double memReq = 0;
        double submitTime = 0;
        int priority = 0;
        double runtime = 10;

        PreemptableVm vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);

        Assert.assertTrue(admController.accept(vm));
        Assert.assertEquals(1, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);

        admController.release(vm);
        Assert.assertEquals(100, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void testAcceptAndRelease() {

        int userId = 0;
        double memReq = 0;
        double submitTime = 0;
        double runtime = 10;

        int priority = 0;
        PreemptableVm vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(1, userId, 70, memReq, submitTime, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(2, userId, 30, memReq, submitTime, priority, runtime);

        //checking values of quotaByPriority
        Assert.assertEquals(100, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm));

        //checking values of quotaByPriority
        Assert.assertEquals(0, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm2));
        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm);
        //checking values of quotaByPriority
        Assert.assertEquals(100, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(30, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm3));
        //checking values of quotaByPriority
        Assert.assertEquals(0, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        priority = 1;
        vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 111, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 111.2, memReq, submitTime, priority, runtime);

        Assert.assertTrue(admController.accept(vm));

        //checking values of quotaByPriority
        Assert.assertEquals(0, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(11.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm2));
        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm);
        //checking values of quotaByPriority
        Assert.assertEquals(0, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(0, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm3));

        priority = 2;
        vm = new PreemptableVm(0, userId, 200, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 150, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 200.0001, memReq, submitTime, priority, runtime);

        Assert.assertTrue(admController.accept(vm));
        //checking values of quotaByPriority
        Assert.assertEquals(0, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm2));
        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm);
        //checking values of quotaByPriority
        Assert.assertEquals(0, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(0, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.111111111, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(50, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm3));

    }

    @Test
    public void testAcceptAndRelease02() {


        Map<Integer, Double> admittedRequests = new HashMap<Integer, Double>();

        admittedRequests.put(PROD, 0.5);
        admittedRequests.put(BATCH, 20.0);
        admittedRequests.put(FREE, 10.0);

        admController.calculateQuota(admittedRequests);

        Assert.assertEquals(99.5, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.333333333, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        int userId = 0;
        double memReq = 0;
        double submitTime = 0;
        double runtime = 10;
        int priority = 0;

        PreemptableVm vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(1, userId, 99.4, memReq, submitTime, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(2, userId, 99.5, memReq, submitTime, priority, runtime);

        Assert.assertFalse(admController.accept(vm));

        //checking values of quotaByPriority
        Assert.assertEquals(99.5, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.333333333, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));

        //checking values of quotaByPriority
        Assert.assertEquals(0.1, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.333333333, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);
        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm2);
        //checking values of quotaByPriority
        Assert.assertEquals(99.5, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.333333333, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm3));
        //checking values of quotaByPriority
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.333333333, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);


        priority = 1;
        vm = new PreemptableVm(0, userId, 100, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 88.323333333, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 88.333333333, memReq, submitTime, priority, runtime);

        Assert.assertFalse(admController.accept(vm));

        //checking values of quotaByPriority
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.333333333, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));

        //checking values of quotaByPriority
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0.01, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);
        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm2);
        //checking values of quotaByPriority
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.333333333, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm3));

        //checking values of quotaByPriority
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        priority = 2;
        vm = new PreemptableVm(0, userId, 140, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 139.001, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 139, memReq, submitTime, priority, runtime);

        Assert.assertFalse(admController.accept(vm));
        //checking values of quotaByPriority
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(139, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm3));

        //checking values of quotaByPriority
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void testAcceptAndRelease03() {


        Map<Integer, Double> admittedRequests = new HashMap<Integer, Double>();

        int userId = 0;
        double memReq = 0;
        double submitTime = 0;
        double runtime = 10;

        admittedRequests.put(PROD, 0.0625);
        admittedRequests.put(BATCH, 0.03125);
        admittedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admittedRequests);

        //checking values of quotaByPriority
        Assert.assertEquals(99.9375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        int priority = 0;
        PreemptableVm vm = new PreemptableVm(0, userId, 99.9376, memReq, submitTime, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(1, userId, 99.9375, memReq, submitTime, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(2, userId, 0.000001, memReq, submitTime, priority, runtime);

        Assert.assertFalse(admController.accept(vm));
        //checking values of quotaByPriority
        Assert.assertEquals(99.9375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm2);

        //checking values of quotaByPriority
        Assert.assertEquals(99.9375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm3));

        //checking values of quotaByPriority
        Assert.assertEquals(99.937499, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        priority = 1;
        vm = new PreemptableVm(0, userId, 111.006944445, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 111.006944444, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 50, memReq, submitTime, priority, runtime);

        Assert.assertFalse(admController.accept(vm));
        //checking values of quotaByPriority
        Assert.assertEquals(99.937499, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(99.937499, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm2);
        //checking values of quotaByPriority
        Assert.assertEquals(99.937499, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm3));
        //checking values of quotaByPriority
        Assert.assertEquals(99.937499, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(61.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        priority = 2;
        vm = new PreemptableVm(0, userId, 199.7876, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 199.7875, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 20, memReq, submitTime, priority, runtime);

        Assert.assertFalse(admController.accept(vm));
        //checking values of quotaByPriority
        Assert.assertEquals(99.937499, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(61.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(99.937499, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(61.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(0d, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm2);
        //checking values of quotaByPriority
        Assert.assertEquals(99.937499, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(61.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm3));
        //checking values of quotaByPriority
        Assert.assertEquals(99.937499, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(61.006944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(179.7875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void testAcceptAndRelease04() {

        Map<Integer, Double> admittedRequests = new HashMap<Integer, Double>();

        int userId = 0;
        double memReq = 0;
        double submitTime = 0;
        double runtime = 10;

        admittedRequests.put(PROD, 0.625);
        admittedRequests.put(BATCH, 0.03125);
        admittedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admittedRequests);

        //checking values of quotaByPriority
        Assert.assertEquals(99.375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.381944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        int priority = 0;

        PreemptableVm vm = new PreemptableVm(0, userId, 99.37, memReq, submitTime, priority, runtime);
        PreemptableVm vm2 = new PreemptableVm(1, userId, 89.409375, memReq, submitTime, priority, runtime);
        PreemptableVm vm3 = new PreemptableVm(2, userId, 49.665625, memReq, submitTime, priority, runtime);

        Assert.assertTrue(admController.accept(vm));
        //checking values of quotaByPriority
        Assert.assertEquals(0.005, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.381944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);
        Assert.assertFalse(admController.accept(vm2));
        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm);
        //checking values of quotaByPriority
        Assert.assertEquals(99.375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.381944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(9.965625, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.381944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm2);
        //checking values of quotaByPriority
        Assert.assertEquals(99.375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.381944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);


        Assert.assertTrue(admController.accept(vm3));
        //checking values of quotaByPriority
        Assert.assertEquals(49.709375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.381944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        priority = 1;
        vm = new PreemptableVm(0, userId, 110.381944445, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 89.409375, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 49.665625, memReq, submitTime, priority, runtime);

        Assert.assertFalse(admController.accept(vm));
        //checking values of quotaByPriority
        Assert.assertEquals(49.709375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.381944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(49.709375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(20.972569444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm3));

        admController.release(vm2);
        //checking values of quotaByPriority
        Assert.assertEquals(49.709375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.381944444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm3));
        //checking values of quotaByPriority
        Assert.assertEquals(49.709375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(60.716319444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        priority = 2;
        vm = new PreemptableVm(0, userId, 198.6626, memReq, submitTime, priority, runtime);
        vm2 = new PreemptableVm(1, userId, 200, memReq, submitTime, priority, runtime);
        vm3 = new PreemptableVm(2, userId, 49.665625, memReq, submitTime, priority, runtime);

        Assert.assertFalse(admController.accept(vm));
        //checking values of quotaByPriority
        Assert.assertEquals(49.709375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(60.716319444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertFalse(admController.accept(vm2));
        //checking values of quotaByPriority
        Assert.assertEquals(49.709375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(60.716319444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        Assert.assertTrue(admController.accept(vm3));
        //checking values of quotaByPriority
        Assert.assertEquals(49.709375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(60.716319444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(148.996875, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);

        admController.release(vm3);
        //checking values of quotaByPriority
        Assert.assertEquals(49.709375, admController.getQuotaByPriority().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(60.716319444, admController.getQuotaByPriority().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6625, admController.getQuotaByPriority().get(FREE), ACCEPTABLE_DIFFERENCE);
    }
}