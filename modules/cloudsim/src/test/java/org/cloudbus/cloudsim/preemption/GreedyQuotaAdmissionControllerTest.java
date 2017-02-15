package org.cloudbus.cloudsim.preemption;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertEquals(100, admController.getPriorityToQuotas().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.11111111111, admController.getPriorityToQuotas().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getPriorityToQuotas().get(2), ACCEPTABLE_DIFFERENCE);

    }

    @Test
    public void testCalculateQuota02(){

        Map<Integer, Double> admittedRequests = new HashMap<Integer, Double>();

        admittedRequests.put(PROD, 10.0);
        admittedRequests.put(BATCH, 10.0);
        admittedRequests.put(FREE, 10.0);

        admController.calculateQuota(admittedRequests);

        Assert.assertEquals(100, admController.getPriorityToQuotas().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(100, admController.getPriorityToQuotas().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(160, admController.getPriorityToQuotas().get(2), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void testCalculateQuota03(){

        Map<Integer, Double> admitedRequests = new HashMap<Integer, Double>();

        admitedRequests.put(PROD, 0d);
        admitedRequests.put(BATCH, 20.0);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getPriorityToQuotas().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getPriorityToQuotas().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(160, admController.getPriorityToQuotas().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 0d);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 30.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getPriorityToQuotas().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.11111111111, admController.getPriorityToQuotas().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getPriorityToQuotas().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 30.0);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 0d);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getPriorityToQuotas().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(77.777777778, admController.getPriorityToQuotas().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(140, admController.getPriorityToQuotas().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 20.0);
        admitedRequests.put(BATCH, 0d);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getPriorityToQuotas().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(88.888888889, admController.getPriorityToQuotas().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(160, admController.getPriorityToQuotas().get(2), ACCEPTABLE_DIFFERENCE);
    }


    @Test
    public void calculateQuota04(){

        Map<Integer, Double> admitedRequests = new HashMap<Integer, Double>();

        admitedRequests.put(PROD, 0.5);
        admitedRequests.put(BATCH, 20.0);
        admitedRequests.put(FREE, 10.0);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getPriorityToQuotas().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.555555556, admController.getPriorityToQuotas().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(159, admController.getPriorityToQuotas().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 0.0625);
        admitedRequests.put(BATCH, 0.03125);
        admitedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getPriorityToQuotas().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.041666667, admController.getPriorityToQuotas().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(199.8125, admController.getPriorityToQuotas().get(2), ACCEPTABLE_DIFFERENCE);

        admitedRequests.put(PROD, 0.625);
        admitedRequests.put(BATCH, 0.03125);
        admitedRequests.put(FREE, 0.01250);

        admController.calculateQuota(admitedRequests);

        Assert.assertEquals(100, admController.getPriorityToQuotas().get(0), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(110.416666667, admController.getPriorityToQuotas().get(1), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(198.6875, admController.getPriorityToQuotas().get(2), ACCEPTABLE_DIFFERENCE);
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

        Map<Integer, Double> admittedRequests = new HashMap<Integer, Double>();

        admittedRequests.put(PROD, 0d);
        admittedRequests.put(BATCH, 0d);
        admittedRequests.put(FREE, 0d);
        
        PreemptableVm vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        //checking values of quotaByPriority
        Assert.assertEquals(100, admController.getPriorityToQuotas().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getPriorityToQuotas().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getPriorityToQuotas().get(FREE), ACCEPTABLE_DIFFERENCE);

        // updating admitted requests
        admittedRequests.put(PROD, 99d);
        admittedRequests.put(BATCH, 0d);
        admittedRequests.put(FREE, 0d);
        
        priority = 1;
        cpuReq = 111.111112111;
        // Quota for priority 1 is 100 / 0.9 = 111,111111111
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm, admittedRequests));

        //checking values of quotaByPriority
        Assert.assertEquals(100, admController.getPriorityToQuotas().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getPriorityToQuotas().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getPriorityToQuotas().get(FREE), ACCEPTABLE_DIFFERENCE);

        cpuReq = 111.111111111;
        // Quota for priority 1 is 100 / 0.9 = 111,111111111
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        //checking values of quotaByPriority
        Assert.assertEquals(100, admController.getPriorityToQuotas().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getPriorityToQuotas().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getPriorityToQuotas().get(FREE), ACCEPTABLE_DIFFERENCE);

        // updating admitted requests
        admittedRequests.put(PROD, 99d);
        admittedRequests.put(BATCH, 111.111111111);
        admittedRequests.put(FREE, 0d);

        
        priority = 2;
        cpuReq = 200.0000001;
        // Quota for priority 2 is 100 / 0.5 = 200
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm, admittedRequests));

        //checking values of quotaByPriority
        Assert.assertEquals(100, admController.getPriorityToQuotas().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getPriorityToQuotas().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getPriorityToQuotas().get(FREE), ACCEPTABLE_DIFFERENCE);

        cpuReq = 199.00000009;
        // Quota for priority 2 is 100 / 0.5 = 200
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        cpuReq = 200;
        // Quota for priority 2 is 100 / 0.5 = 200
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        //checking values of quotaByPriority
        Assert.assertEquals(100, admController.getPriorityToQuotas().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(111.111111111, admController.getPriorityToQuotas().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(200, admController.getPriorityToQuotas().get(FREE), ACCEPTABLE_DIFFERENCE);
    }

    @Test
    public void testAccept2(){
        int id = 0;
        int userId = 0;
        double cpuReq = 50;
        double memReq = 0;
        double submitTime = 0;
        int priority = 0;
        double runtime = 10;

        Map<Integer, Double> admittedRequests = new HashMap<Integer, Double>();

        // updating admitted requests
        admittedRequests.put(PROD, 50d);
        admittedRequests.put(BATCH, 20d);
        admittedRequests.put(FREE, 10d);

        admController.calculateQuota(admittedRequests);

        //checking values of quotaByPriority
        Assert.assertEquals(100, admController.getPriorityToQuotas().get(PROD), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(55.555555556, admController.getPriorityToQuotas().get(BATCH), ACCEPTABLE_DIFFERENCE);
        Assert.assertEquals(60, admController.getPriorityToQuotas().get(FREE), ACCEPTABLE_DIFFERENCE);

        // quota is 100, requested is 50 and admitted is 50 too
        PreemptableVm vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        cpuReq = 49.999999999;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        cpuReq = 50.0000000001;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm, admittedRequests));


        // priority 1

        priority = 1;
        cpuReq = 35.555555556;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        cpuReq = 35.555555555;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        cpuReq = 35.555555557;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm, admittedRequests));


        // priority 2

        priority = 2;
        cpuReq = 50;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        cpuReq = 49.999999999;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertTrue(admController.accept(vm, admittedRequests));

        cpuReq = 50.0000000001;
        vm = new PreemptableVm(id, userId, cpuReq, memReq, submitTime, priority, runtime);
        Assert.assertFalse(admController.accept(vm, admittedRequests));

    }



}