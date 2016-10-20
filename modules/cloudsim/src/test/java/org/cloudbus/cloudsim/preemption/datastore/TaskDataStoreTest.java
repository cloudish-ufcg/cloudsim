package org.cloudbus.cloudsim.preemption.datastore;

import java.io.File;
import org.cloudbus.cloudsim.preemption.TaskState;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Alessandro Lia Fook Santos and Joao Victor Mafra on 20/10/16.
 */
public class TaskDataStoreTest {

    public static final double TIME = 1;

    private static String databaseFile = "TaskDataStoreTest.sqlite3";
    private static String databaseURL = "jdbc:sqlite:" + databaseFile;

    private static Properties properties;

    TaskDataStore dataStore;

    private List<TaskState> taskStates;

    private TaskState task1, task2, task3, task4;

    private int taskId, priority;
    private double cpuReq, finishingTime, runtime, submitTime;

    @Before
    public void setUp() {

        // creating the dataStore
        properties = new Properties();
        properties.setProperty(TaskDataStore.DATABASE_URL_PROP, databaseURL);

        // creating data store
        dataStore = new TaskDataStore(properties);

        // creating the list of task states
        taskStates = new ArrayList<>();

        taskId = 0;
        priority = 1;
        cpuReq = 0.02;
        submitTime = 0;
        runtime = 0.001;
        finishingTime = 0.001;

        task1 = new TaskState(taskId++, cpuReq, submitTime, finishingTime, runtime, priority - 1);
        task2 = new TaskState(taskId++, cpuReq, submitTime, finishingTime, runtime, priority);
        task3 = new TaskState(taskId++, cpuReq, submitTime, finishingTime, runtime, priority + 1);
    }

    @After
    public void tearDown() {
        new File(databaseFile).delete();
    }

    @Test
    public void testAddNullList() {
        Assert.assertFalse(dataStore.addTaskList(null));
    }

    @Test
    public void testAddEmptyList() {
        Assert.assertTrue(dataStore.addTaskList(taskStates));
        Assert.assertEquals(0, dataStore.getAllTasks().size());
    }

    @Test
    public void testAddList() {

        taskStates.add(task1);
        Assert.assertTrue(dataStore.addTaskList(taskStates));
        Assert.assertArrayEquals(taskStates.toArray(), dataStore.getAllTasks().toArray());

    }

    @Test
    public void testAddList2() {

        taskStates.add(task1);
        taskStates.add(task2);
        Assert.assertTrue(dataStore.addTaskList(taskStates));
        Assert.assertArrayEquals(taskStates.toArray(), dataStore.getAllTasks().toArray());

        taskStates.add(task3);
        Assert.assertFalse(Arrays.equals(taskStates.toArray(), dataStore.getAllTasks().toArray()));
    }

    @Test
    public void testAddList3() {

        int taskId = 0;

        for (int i = 0; i < 50; i++) {
            task1 = new TaskState(taskId++, cpuReq, submitTime, finishingTime, runtime, priority - 1);
            taskStates.add(task1);
        }

        Assert.assertTrue(dataStore.addTaskList(taskStates));
        Assert.assertArrayEquals(taskStates.toArray(), dataStore.getAllTasks().toArray());

        List<TaskState> taskStates2 = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            task1 = new TaskState(taskId++, cpuReq, submitTime, finishingTime, runtime, priority - 1);
            taskStates.add(task1);
            taskStates2.add(task1);
        }

        Assert.assertTrue(dataStore.addTaskList(taskStates2));
        Assert.assertArrayEquals(taskStates.toArray(), dataStore.getAllTasks().toArray());
        Assert.assertFalse(Arrays.equals(taskStates2.toArray(), dataStore.getAllTasks().toArray()));
    }
}
