package org.cloudbus.cloudsim.preemption.datastore;

import java.io.File;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by Alessandro Lia Fook Santos and Joao Victor Mafra on 19/10/16.
 */
public class PreemptableVmDataStoreTest {

    public static final double TIME = 1;

    private static String databaseFile = "VmDataStoreTest.sqlite3";
    private static Properties properties;

    PreemptableVmDataStore dataStore;

    private SortedSet<PreemptableVm> running;
    private SortedSet<PreemptableVm> waiting;

    private PreemptiveHost host1, host2;

    private PreemptableVm vm1, vm2, vm3, vm4, vm5;

    private int id, priority;
    private static final int USER_ID = 0;
    private double cpuReq, memReq, submitTime, runtime;


    @Before
    public void setUp() {

        // creating the dataStore
        properties = new Properties();
        properties.setProperty(PreemptableVmDataStore.CHECKPOINT_DIR_PROP, databaseFile);

        // creating data store
        dataStore = new PreemptableVmDataStore(properties, TIME);

        // creating structures were the vms are inserted
        running = new TreeSet<>();
        waiting = new TreeSet<>();

        //creating hostsWithMockedPolicy
        host1 = Mockito.mock(PreemptiveHost.class);
        host2 = Mockito.mock(PreemptiveHost.class);

        // creating vms
        id = 0;
        priority = 1;
        cpuReq = 0.002;
        memReq = 0.001;
        submitTime = 0;
        runtime = 1.25;

        vm1 = new PreemptableVm(id++, USER_ID, cpuReq, memReq, submitTime, priority - 1, runtime, 1);
        vm2 = new PreemptableVm(id++, USER_ID, cpuReq, memReq, submitTime, priority - 1, runtime, 1);
        vm3 = new PreemptableVm(id++, USER_ID, cpuReq, memReq, submitTime, priority, runtime, 0.9);
        vm4 = new PreemptableVm(id++, USER_ID, cpuReq, memReq, submitTime, priority + 1, runtime, 0.5);

        //setting host for the vms
        vm1.setHost(host1);
        vm2.setHost(host2);
        vm3.setHost(host1);
        vm4.setHost(host2);

        // mocking the id of the host
        Mockito.when(vm1.getHost().getId()).thenReturn(1);
        Mockito.when(vm2.getHost().getId()).thenReturn(2);
        Mockito.when(vm3.getHost().getId()).thenReturn(1);
        Mockito.when(vm4.getHost().getId()).thenReturn(2);

        // assert initial state
        Assert.assertEquals(0, dataStore.getAllRunningVms().size());
        Assert.assertEquals(0, dataStore.getAllWaitingVms().size());

    }

    @After
    public void tearDown() {
        new File(properties.getProperty(PreemptableVmDataStore.CHECKPOINT_DIR_PROP)
                + "vms-" + properties.getProperty("number_of_hosts") + "-hosts-"
                + String.valueOf(TIME)).delete();
    }

    @Test
    public void testAddSetEqualsNull() {

        SortedSet<PreemptableVm> setNull = null;

        Assert.assertFalse(dataStore.addWaitingVms(setNull));
        Assert.assertFalse(dataStore.addRunningVms(setNull));

    }

    @Test
    public void testAddEmptySet() {

        Assert.assertTrue(dataStore.addWaitingVms(waiting));
        Assert.assertTrue(dataStore.addRunningVms(running));

        Assert.assertEquals(0, dataStore.getAllRunningVms().size());
        Assert.assertEquals(0, dataStore.getAllWaitingVms().size());
    }

    @Test
    public void testAddRunning1() {

        running.add(vm1);

        Assert.assertTrue(dataStore.addWaitingVms(waiting));
        Assert.assertTrue(dataStore.addRunningVms(running));

        Assert.assertEquals(1, dataStore.getAllRunningVms().size());
        Assert.assertEquals(0, dataStore.getAllWaitingVms().size());

        for (PreemptableVm vm : dataStore.getAllRunningVms()) {
            Assert.assertTrue(running.contains(vm));
        }

        for (PreemptableVm vm : dataStore.getAllWaitingVms()) {
            Assert.assertTrue(waiting.contains(vm));
        }
    }

    @Test
    public void testAddWaiting1() {

        waiting.add(vm1);

        Assert.assertTrue(dataStore.addWaitingVms(waiting));
        Assert.assertTrue(dataStore.addRunningVms(running));

        Assert.assertEquals(0, dataStore.getAllRunningVms().size());
        Assert.assertEquals(1, dataStore.getAllWaitingVms().size());

        for (PreemptableVm vm : dataStore.getAllRunningVms()) {
            Assert.assertTrue(running.contains(vm));
        }

        for (PreemptableVm vm : dataStore.getAllWaitingVms()) {
            Assert.assertTrue(waiting.contains(vm));
        }
    }

    @Test
    public void testeAddRunning2() {
        running.add(vm1);
        running.add(vm2);

        Assert.assertTrue(dataStore.addWaitingVms(waiting));
        Assert.assertTrue(dataStore.addRunningVms(running));

        Assert.assertEquals(2, dataStore.getAllRunningVms().size());
        Assert.assertEquals(0, dataStore.getAllWaitingVms().size());

        for (PreemptableVm vm : dataStore.getAllRunningVms()) {
            Assert.assertTrue(running.contains(vm));
        }

        for (PreemptableVm vm : dataStore.getAllWaitingVms()) {
            Assert.assertTrue(waiting.contains(vm));
        }

        Assert.assertArrayEquals(running.toArray(), dataStore.getAllRunningVms().toArray());

        running.remove(vm1);
        running.remove(vm2);

        running.add(vm3);
        running.add(vm4);

        Assert.assertTrue(dataStore.addWaitingVms(waiting));
        Assert.assertTrue(dataStore.addRunningVms(running));

        Assert.assertEquals(4, dataStore.getAllRunningVms().size());
        Assert.assertEquals(0, dataStore.getAllWaitingVms().size());

        Assert.assertTrue(dataStore.getAllRunningVms().contains(vm1));
        Assert.assertTrue(dataStore.getAllRunningVms().contains(vm2));
        Assert.assertTrue(dataStore.getAllRunningVms().contains(vm3));
        Assert.assertTrue(dataStore.getAllRunningVms().contains(vm4));

    }

    @Test
    public void testeAddWaiting2() {
        waiting.add(vm1);
        waiting.add(vm2);

        Assert.assertTrue(dataStore.addWaitingVms(waiting));
        Assert.assertTrue(dataStore.addRunningVms(running));

        Assert.assertEquals(0, dataStore.getAllRunningVms().size());
        Assert.assertEquals(2, dataStore.getAllWaitingVms().size());

        for (PreemptableVm vm : dataStore.getAllRunningVms()) {
            Assert.assertTrue(running.contains(vm));
        }

        for (PreemptableVm vm : dataStore.getAllWaitingVms()) {
            Assert.assertTrue(waiting.contains(vm));
        }

        Assert.assertArrayEquals(waiting.toArray(), dataStore.getAllWaitingVms().toArray());

        waiting.remove(vm1);
        waiting.remove(vm2);

        waiting.add(vm3);
        waiting.add(vm4);

        Assert.assertTrue(dataStore.addWaitingVms(waiting));
        Assert.assertTrue(dataStore.addRunningVms(running));

        Assert.assertEquals(0, dataStore.getAllRunningVms().size());
        Assert.assertEquals(4, dataStore.getAllWaitingVms().size());

        Assert.assertTrue(dataStore.getAllWaitingVms().contains(vm1));
        Assert.assertTrue(dataStore.getAllWaitingVms().contains(vm2));
        Assert.assertTrue(dataStore.getAllWaitingVms().contains(vm3));
        Assert.assertTrue(dataStore.getAllWaitingVms().contains(vm4));

    }

    @Test
    public void testeAddWaitingAndRunning() {
        running.add(vm1);
        waiting.add(vm2);

        Assert.assertTrue(dataStore.addWaitingVms(waiting));
        Assert.assertTrue(dataStore.addRunningVms(running));

        Assert.assertEquals(1, dataStore.getAllRunningVms().size());
        Assert.assertEquals(1, dataStore.getAllWaitingVms().size());

        for (PreemptableVm vm : dataStore.getAllRunningVms()) {
            Assert.assertTrue(running.contains(vm));
        }

        for (PreemptableVm vm : dataStore.getAllWaitingVms()) {
            Assert.assertTrue(waiting.contains(vm));
        }

        Assert.assertArrayEquals(waiting.toArray(), dataStore.getAllWaitingVms().toArray());
        Assert.assertArrayEquals(running.toArray(), dataStore.getAllRunningVms().toArray());

        running.remove(vm1);
        waiting.remove(vm2);
        running.add(vm3);
        waiting.add(vm4);

        Assert.assertTrue(dataStore.addWaitingVms(waiting));
        Assert.assertTrue(dataStore.addRunningVms(running));

        Assert.assertEquals(2, dataStore.getAllRunningVms().size());
        Assert.assertEquals(2, dataStore.getAllWaitingVms().size());

        Assert.assertTrue(dataStore.getAllWaitingVms().contains(vm2));
        Assert.assertTrue(dataStore.getAllWaitingVms().contains(vm4));

        Assert.assertTrue(dataStore.getAllRunningVms().contains(vm1));
        Assert.assertTrue(dataStore.getAllRunningVms().contains(vm3));
    }

    @Test
    public void testAddWaitingAndRunning2() {

        int vmId = 0;

        //adding 50 vms to the set of running
        for (int i = 0; i < 50; i++) {
            vm1 = new PreemptableVm(vmId++, USER_ID, cpuReq, memReq, submitTime, priority - 1, runtime, 1);
            vm1.setHost(host1);
            running.add(vm1);
        }

        // adding the set of running into the BD
        Assert.assertTrue(dataStore.addRunningVms(running));

        // testing the status of BD
        Assert.assertArrayEquals(running.toArray(), dataStore.getAllRunningVms().toArray());
        Assert.assertEquals(0, dataStore.getAllWaitingVms().size());

        //adding 50 vms to the set of waiting
        for (int i = 0; i < 50; i++) {
            vm1 = new PreemptableVm(vmId++, USER_ID, cpuReq, memReq, submitTime, priority - 1, runtime, 1);
            vm1.setHost(host1);
            waiting.add(vm1);
        }

        // adding the set of waiting into the BD
        Assert.assertTrue(dataStore.addWaitingVms(waiting));

        // testing the status of BD
        Assert.assertArrayEquals(running.toArray(), dataStore.getAllRunningVms().toArray());
        Assert.assertArrayEquals(waiting.toArray(), dataStore.getAllWaitingVms().toArray());
    }
}
