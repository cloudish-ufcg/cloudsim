import com.beust.jcommander.JCommander;
import org.cloudbus.cloudsim.examples.MergeOfResultFiles;
import org.cloudbus.cloudsim.preemption.DatacenterInfo;
import org.cloudbus.cloudsim.preemption.TaskState;
import org.cloudbus.cloudsim.preemption.UsageEntry;
import org.cloudbus.cloudsim.preemption.datastore.DatacenterUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.HostUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.TaskDataStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * Created by Alessandro Lia Fook and João Victor Mafra on 11/11/16.
 */
public class MergeOfResultFilesTest {


    private Properties properties;
    private static final String DATABASE_FILE = "CompleteDatabaseMergeTest.sqlite3";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_FILE;
    private static final String DATASTORE_SQLITE_DRIVER = "org.sqlite.JDBC";

    private static final int NUMBER_OF_ELEMENTS_IN_ARGS = 9;

    // tasks characteristics
    private List<TaskState> taskStates;
    private static final int NUMBER_OF_TASKS = 100;
    private TaskState task;

    private int taskId, priority;
    private double firstTimeAllocated;

    private double cpuReq, finishingTime, runtime, submitTime;

    // UsageEntry characteristics
    private int hostId = 0;
    private UsageEntry entry0, entry1, entry2;

    private List<UsageEntry> entryList;
    private static final int NUMBER_OF_USAGE_ENTRIES = 10;
    private static final double P0_USAGE = 40.558585;
    private static final double P1_USAGE = 21.9875422;
    private static final double P2_USAGE = 5.875646232;
    private static final int P0_VMS = 12;
    private static final int P1_VMS = 1;
    private static final int P2_VMS = 3;


    private static final double AVAILABLE_MIPS = 0.78598;

    //DatacenterInfo characteristics
    private static final int NUMBER_OF_DATACENTER_INFO = 20;
    private double time;
    private static int VMS_RUNNING = 59;
    private static final int VMS_RUNNING_P0 = 40;
    private static final int VMS_RUNNING_P1 = 15;
    private static final int VMS_RUNNING_P2 = 4;
    private static final int VMS_FOR_SCHEDULING = 12;
    private static final int VMS_FOR_SCHEDULING_P_0 = 1;
    private static final int VMS_FOR_SCHEDULING_P_1 = 3;
    private static final int VMS_FOR_SCHEDULING_P_2 = 8;
    private double RESOURCES_RUNNING_P0 = 0.581111;
    private double RESOURCES_RUNNING_P1 = 2000.00000009;
    private double RESOURCES_RUNNING_P2 = 0d;
    private double RESOURCES_WAITING_P0 = 123.99999;
    private double RESOURCES_WAITING_P1 = 0.000000001;

    private double RESOURCES_WAITING_P2 = 6000.99999999;
    private DatacenterInfo info;

    private List<DatacenterInfo> datacenterInfoList;
    private String[] args;
    JCommander jc;


    @Before
    public void setUp() throws Exception {
        args = new String[NUMBER_OF_ELEMENTS_IN_ARGS];

        jc = new JCommander();

        properties = Mockito.mock(Properties.class);

        // creating tasks

        taskStates = new ArrayList<>();

        taskId = 0;
        priority = 1;
        cpuReq = 0.02;
        submitTime = 0;
        runtime = 0.001;
        finishingTime = 0;
        firstTimeAllocated = 1.5;

        for (int i = 0; i < NUMBER_OF_TASKS; i++) {

            task = new TaskState(taskId++, cpuReq, submitTime, finishingTime++, runtime, priority - 1, 0, 0, 0, firstTimeAllocated);
            taskStates.add(task);
        }

        // creating datacenterInfo
        time = 5; // time of the info was collected
        datacenterInfoList = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_DATACENTER_INFO; i++) {

            info = new DatacenterInfo(time + (5 * i), VMS_RUNNING, VMS_RUNNING_P0, RESOURCES_RUNNING_P0,
                    VMS_RUNNING_P1, RESOURCES_RUNNING_P1, VMS_RUNNING_P2, RESOURCES_RUNNING_P2,
                    VMS_FOR_SCHEDULING, VMS_FOR_SCHEDULING_P_0, RESOURCES_WAITING_P0, VMS_FOR_SCHEDULING_P_1,
                    RESOURCES_WAITING_P1, VMS_FOR_SCHEDULING_P_2, RESOURCES_WAITING_P2);

            datacenterInfoList.add(info);
        }

        // creating UsageEntry
        time = 10; // time of the UsageEntry was collected

        entryList = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_USAGE_ENTRIES; i++) {

            entry0 = new UsageEntry(hostId, time + (10 * i), P0_USAGE, P0_VMS, 0, AVAILABLE_MIPS);
            entry1 = new UsageEntry(hostId, time + (10 * i), P1_USAGE, P1_VMS, 1, AVAILABLE_MIPS);
            entry2 = new UsageEntry(hostId, time + (10 * i), P2_USAGE, P2_VMS, 2, AVAILABLE_MIPS);

            entryList.add(entry0);
            entryList.add(entry1);
            entryList.add(entry2);
        }

        createAndPopulateDatacenterInfoDatabase(DATABASE_URL, datacenterInfoList);
        createAndPopulateTaskDatabase(DATABASE_URL, taskStates);
        createAndPopulateUsageEntryDatabase(DATABASE_URL, entryList);
    }

    @After
    public void tearDown() throws Exception {
        new File(DATABASE_FILE).delete();
    }


    // methods for creating databases

    private void createAndPopulateTaskDatabase(String databaseUrl, List<TaskState> taskStates) throws ClassNotFoundException, SQLException {
        Class.forName(DATASTORE_SQLITE_DRIVER);
        Connection connection = DriverManager.getConnection(databaseUrl);

        if (connection != null) {
            // Creating the database
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS googletasks("
                    + "taskId INTEGER NOT NULL, "
                    + "cpuReq REAL, "
                    + "submitTime REAL, "
                    + "finishTime REAL, "
                    + "runtime REAL, "
                    + "priority INTEGER, "
                    + "preemptions INTEGER, "
                    + "backfillingChoices INTEGER, "
                    + "migrations INTEGER, "
                    + "firstTimeAllocated REAL, "
                    + "PRIMARY KEY (taskId)"
                    + ")");
            statement.close();

            String INSERT_CLOUDLET_SQL = "INSERT INTO googletasks"
                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // populating the database
            for (TaskState taskState: taskStates) {
                PreparedStatement insertMemberStatement = connection
                        .prepareStatement(INSERT_CLOUDLET_SQL);
                insertMemberStatement.setInt(1, taskState.getTaskId());
                insertMemberStatement.setDouble(2, taskState.getCpuReq());
                insertMemberStatement.setDouble(3, taskState.getSubmitTime());
                insertMemberStatement.setDouble(4, taskState.getFinishTime());
                insertMemberStatement.setDouble(5, taskState.getRuntime());
                insertMemberStatement.setInt(6, taskState.getPriority());
                insertMemberStatement.setInt(7, taskState.getNumberOfPreemptions());
                insertMemberStatement.setInt(8, taskState.getNumberOfBackfillingChoices());
                insertMemberStatement.setInt(9, taskState.getNumberOfMigrations());
                insertMemberStatement.setDouble(10, taskState.getFirstTimeAllocated());
                insertMemberStatement.execute();
            }
            connection.close();
        }
    }

    private void createAndPopulateUsageEntryDatabase(String databaseUrl, List<UsageEntry> entryList) throws ClassNotFoundException, SQLException {
        Class.forName(DATASTORE_SQLITE_DRIVER);
        Connection connection = DriverManager.getConnection(databaseUrl);

        if (connection != null) {
            // Creating the database
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS usage("
                    + "hostId INTEGER NOT NULL, "
                    + "time REAL NOT NULL, "
                    + "usage REAL, "
                    + "vms INTEGER, "
                    + "priority INTEGER, "
                    + "availableMips REAL, "
                    + "PRIMARY KEY (hostId, time, priority)"
                    + ")");
            statement.close();

            String INSERT_CLOUDLET_SQL = "INSERT INTO usage"
                    + " VALUES(?, ?, ?, ?, ?, ?)";

            // populating the database
            PreparedStatement insertMemberStatement = connection
                    .prepareStatement(INSERT_CLOUDLET_SQL);

            for (UsageEntry entry : entryList) {
                insertMemberStatement.setInt(1, entry.getHostId());
                insertMemberStatement.setDouble(2, entry.getTime());
                insertMemberStatement.setDouble(3, entry.getUsage());
                insertMemberStatement.setInt(4, entry.getNumberOfVms());
                insertMemberStatement.setInt(5, entry.getPriority());
                insertMemberStatement.setDouble(6, entry.getAvailableMips());
                insertMemberStatement.addBatch();
            }

            insertMemberStatement.executeBatch();
            connection.close();
        }
    }

    private void createAndPopulateDatacenterInfoDatabase(String databaseUrl, List<DatacenterInfo> datacenterInfoList) throws ClassNotFoundException, SQLException {
        Class.forName(DATASTORE_SQLITE_DRIVER);
        Connection connection = DriverManager.getConnection(databaseUrl);

        if (connection != null) {
            // Creating the database
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS datacenterusage("
                    + "time REAL NOT NULL, "
                    + "vmsRunning INTEGER, "
                    + "vmsRunningP0 INTEGER, "
                    + "resourcesRunningP0 REAL, "
                    + "vmsRunningP1 INTEGER, "
                    + "resourcesRunningP1 REAL, "
                    + "vmsRunningP2 INTEGER, "
                    + "resourcesRunningP2 REAL, "
                    + "vmsForScheduling INTEGER, "
                    + "vmsForSchedulingP0 INTEGER, "
                    + "resourcesWaitingP0 REAL, "
                    + "vmsForSchedulingP1 INTEGER, "
                    + "resourcesWaitingP1 REAL, "
                    + "vmsForSchedulingP2 INTEGER, "
                    + "resourcesWaitingP2 REAL, "
                    + "PRIMARY KEY (time)"
                    + ")");
            statement.close();

            String INSERT_CLOUDLET_SQL = "INSERT INTO datacenterusage"
                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // populating the database
            for (DatacenterInfo datacenterInfo: datacenterInfoList) {
                PreparedStatement insertMemberStatement = connection
                        .prepareStatement(INSERT_CLOUDLET_SQL);
                insertMemberStatement.setDouble(1, datacenterInfo.getTime());
                insertMemberStatement.setInt(2, datacenterInfo.getVmsRunning());
                insertMemberStatement.setInt(3, datacenterInfo.getVmsRunningP0());
                insertMemberStatement.setDouble(4, datacenterInfo.getResourcesRunningP0());
                insertMemberStatement.setInt(5, datacenterInfo.getVmsRunningP1());
                insertMemberStatement.setDouble(6, datacenterInfo.getResourcesRunningP1());
                insertMemberStatement.setInt(7, datacenterInfo.getVmsRunningP2());
                insertMemberStatement.setDouble(8, datacenterInfo.getResourcesRunningP2());
                insertMemberStatement.setInt(9, datacenterInfo.getVmsForScheduling());
                insertMemberStatement.setInt(10, datacenterInfo.getVmsForSchedulingP0());
                insertMemberStatement.setDouble(11, datacenterInfo.getResourcesWaitingP0());
                insertMemberStatement.setInt(12, datacenterInfo.getVmsForSchedulingP1());
                insertMemberStatement.setDouble(13, datacenterInfo.getResourcesWaitingP1());
                insertMemberStatement.setInt(14, datacenterInfo.getVmsForSchedulingP2());
                insertMemberStatement.setDouble(15, datacenterInfo.getResourcesWaitingP2());
                insertMemberStatement.execute();
            }
            connection.close();
        }
    }
    @Test
    public void testMergeTask() throws Exception{
        List<TaskState> taskStatesAfter = taskStates.subList(70, 100);
        List<TaskState> taskStatesBefore = taskStates.subList(0, 70);

        String databaseFileBefore = "DatabaseTaskTestBefore.sqlite3";
        String databaseURLBefore = "jdbc:sqlite:" + databaseFileBefore;

        String databaseFileAfter = "DatabaseTaskTestAfter.sqlite3";
        String databaseURLAfter = "jdbc:sqlite:" + databaseFileAfter;

        createAndPopulateTaskDatabase(databaseURLBefore, taskStatesBefore);
        createAndPopulateTaskDatabase(databaseURLAfter, taskStatesAfter);

        String outputFile =  "DatabaseTaskTestOutput.sqlite3";
        String outputURL = "jdbc:sqlite:" + outputFile;

        String[] args = {"task", "--before", databaseURLBefore, "--after", databaseURLAfter, "--time", "69", "--output", outputURL};


        MergeOfResultFiles.executeCommand(jc, args);

        Mockito.when(properties.getProperty(TaskDataStore.DATABASE_URL_PROP)).thenReturn(outputURL);
        TaskDataStore dataStoreOutput = new TaskDataStore(properties);

        Mockito.when(properties.getProperty(TaskDataStore.DATABASE_URL_PROP)).thenReturn(DATABASE_URL);
        TaskDataStore dataStoreBase = new TaskDataStore(properties);

        Assert.assertEquals(dataStoreOutput.getAllTasks().size(), NUMBER_OF_TASKS);
        Assert.assertEquals(dataStoreBase.getAllTasks(), dataStoreOutput.getAllTasks());

        new File(databaseFileBefore).delete();
        new File(databaseFileAfter).delete();
        new File(outputFile).delete();
    }

    @Test
    public void testMergeTask2() throws Exception{
        List<TaskState> taskStatesAfter = taskStates.subList(70, 100);
        List<TaskState> taskStatesBefore = taskStates.subList(0, 71);

        String databaseFileBefore = "DatabaseTaskTestBefore.sqlite3";
        String databaseURLBefore = "jdbc:sqlite:" + databaseFileBefore;

        String databaseFileAfter = "DatabaseTaskTestAfter.sqlite3";
        String databaseURLAfter = "jdbc:sqlite:" + databaseFileAfter;

        createAndPopulateTaskDatabase(databaseURLBefore, taskStatesBefore);
        createAndPopulateTaskDatabase(databaseURLAfter, taskStatesAfter);

        String outputFile =  "DatabaseTaskTestOutput.sqlite3";
        String outputURL = "jdbc:sqlite:" + outputFile;

        String[] args = {"task", "--before", databaseURLBefore, "--after", databaseURLAfter, "--time", "69", "--output", outputURL};


        MergeOfResultFiles.executeCommand(jc, args);

        Mockito.when(properties.getProperty(TaskDataStore.DATABASE_URL_PROP)).thenReturn(outputURL);
        TaskDataStore dataStoreOutput = new TaskDataStore(properties);

        Mockito.when(properties.getProperty(TaskDataStore.DATABASE_URL_PROP)).thenReturn(DATABASE_URL);
        TaskDataStore dataStoreBase = new TaskDataStore(properties);

        Assert.assertEquals(dataStoreOutput.getAllTasks().size(), NUMBER_OF_TASKS);
        Assert.assertEquals(dataStoreBase.getAllTasks(), dataStoreOutput.getAllTasks());

        new File(databaseFileBefore).delete();
        new File(databaseFileAfter).delete();
        new File(outputFile).delete();
    }

    @Test
    public void testMergeTask3() throws Exception{
        List<TaskState> taskStatesAfter = taskStates.subList(70, 100);
        List<TaskState> taskStatesBefore = taskStates.subList(0, 100);

        String databaseFileBefore = "DatabaseTaskTestBefore.sqlite3";
        String databaseURLBefore = "jdbc:sqlite:" + databaseFileBefore;

        String databaseFileAfter = "DatabaseTaskTestAfter.sqlite3";
        String databaseURLAfter = "jdbc:sqlite:" + databaseFileAfter;

        createAndPopulateTaskDatabase(databaseURLBefore, taskStatesBefore);
        createAndPopulateTaskDatabase(databaseURLAfter, taskStatesAfter);

        String outputFile =  "DatabaseTaskTestOutput.sqlite3";
        String outputURL = "jdbc:sqlite:" + outputFile;

        String[] args = {"task", "--before", databaseURLBefore, "--after", databaseURLAfter, "--time", "69", "--output", outputURL};


        MergeOfResultFiles.executeCommand(jc, args);

        Mockito.when(properties.getProperty(TaskDataStore.DATABASE_URL_PROP)).thenReturn(outputURL);
        TaskDataStore dataStoreOutput = new TaskDataStore(properties);

        Mockito.when(properties.getProperty(TaskDataStore.DATABASE_URL_PROP)).thenReturn(DATABASE_URL);
        TaskDataStore dataStoreBase = new TaskDataStore(properties);

        Assert.assertEquals(dataStoreOutput.getAllTasks().size(), NUMBER_OF_TASKS);
        Assert.assertEquals(dataStoreBase.getAllTasks(), dataStoreOutput.getAllTasks());

        new File(databaseFileBefore).delete();
        new File(databaseFileAfter).delete();
        new File(outputFile).delete();
    }

    @Test
    public void testMergeUsageEntry() throws Exception{
        List<UsageEntry> usageEntriesAfter = entryList.subList(21, 30);
        List<UsageEntry> usageEntriesBefore = entryList.subList(0, 21);

        String databaseFileBefore = "DatabaseEntriesTestBefore.sqlite3";
        String databaseURLBefore = "jdbc:sqlite:" + databaseFileBefore;

        String databaseFileAfter = "DatabaseEntriesTestAfter.sqlite3";
        String databaseURLAfter = "jdbc:sqlite:" + databaseFileAfter;

        createAndPopulateUsageEntryDatabase(databaseURLBefore, usageEntriesBefore);
        createAndPopulateUsageEntryDatabase(databaseURLAfter, usageEntriesAfter);

        String outputFile =  "DatabaseEntriesTestOutput.sqlite3";
        String outputURL = "jdbc:sqlite:" + outputFile;

        String[] args = {"utilization", "--before", databaseURLBefore, "--after", databaseURLAfter, "--time", "80", "--output", outputURL};


        MergeOfResultFiles.executeCommand(jc, args);

        Mockito.when(properties.getProperty(HostUsageDataStore.DATABASE_URL_PROP)).thenReturn(outputURL);
        HostUsageDataStore dataStoreOutput = new HostUsageDataStore(properties);

        Mockito.when(properties.getProperty(HostUsageDataStore.DATABASE_URL_PROP)).thenReturn(DATABASE_URL);
        HostUsageDataStore dataStoreBase = new HostUsageDataStore(properties);

        Assert.assertEquals(dataStoreOutput.getAllUsageEntries().size(), 3*NUMBER_OF_USAGE_ENTRIES);
        Assert.assertEquals(dataStoreBase.getAllUsageEntries(), dataStoreOutput.getAllUsageEntries());

        new File(databaseFileBefore).delete();
        new File(databaseFileAfter).delete();
        new File(outputFile).delete();

    }

    @Test
    public void testMergeUsageEntry2() throws Exception{
        List<UsageEntry> usageEntriesAfter = entryList.subList(21, 30);
        List<UsageEntry> usageEntriesBefore = entryList.subList(0, 22);

        String databaseFileBefore = "DatabaseEntriesTestBefore.sqlite3";
        String databaseURLBefore = "jdbc:sqlite:" + databaseFileBefore;

        String databaseFileAfter = "DatabaseEntriesTestAfter.sqlite3";
        String databaseURLAfter = "jdbc:sqlite:" + databaseFileAfter;

        createAndPopulateUsageEntryDatabase(databaseURLBefore, usageEntriesBefore);
        createAndPopulateUsageEntryDatabase(databaseURLAfter, usageEntriesAfter);

        String outputFile =  "DatabaseEntriesTestOutput.sqlite3";
        String outputURL = "jdbc:sqlite:" + outputFile;

        String[] args = {"utilization", "--before", databaseURLBefore, "--after", databaseURLAfter, "--time", "80", "--output", outputURL};


        MergeOfResultFiles.executeCommand(jc, args);

        Mockito.when(properties.getProperty(HostUsageDataStore.DATABASE_URL_PROP)).thenReturn(outputURL);
        HostUsageDataStore dataStoreOutput = new HostUsageDataStore(properties);

        Mockito.when(properties.getProperty(HostUsageDataStore.DATABASE_URL_PROP)).thenReturn(DATABASE_URL);
        HostUsageDataStore dataStoreBase = new HostUsageDataStore(properties);

        Assert.assertEquals(dataStoreOutput.getAllUsageEntries().size(), 3*NUMBER_OF_USAGE_ENTRIES);
        Assert.assertEquals(dataStoreBase.getAllUsageEntries(), dataStoreOutput.getAllUsageEntries());

        new File(databaseFileBefore).delete();
        new File(databaseFileAfter).delete();
        new File(outputFile).delete();

    }

    @Test
    public void testMergeUsageEntry3() throws Exception{
        List<UsageEntry> usageEntriesAfter = entryList.subList(21, 30);
        List<UsageEntry> usageEntriesBefore = entryList.subList(0, 30);

        String databaseFileBefore = "DatabaseEntriesTestBefore.sqlite3";
        String databaseURLBefore = "jdbc:sqlite:" + databaseFileBefore;

        String databaseFileAfter = "DatabaseEntriesTestAfter.sqlite3";
        String databaseURLAfter = "jdbc:sqlite:" + databaseFileAfter;

        createAndPopulateUsageEntryDatabase(databaseURLBefore, usageEntriesBefore);
        createAndPopulateUsageEntryDatabase(databaseURLAfter, usageEntriesAfter);

        String outputFile =  "DatabaseEntriesTestOutput.sqlite3";
        String outputURL = "jdbc:sqlite:" + outputFile;

        String[] args = {"utilization", "--before", databaseURLBefore, "--after", databaseURLAfter, "--time", "80", "--output", outputURL};


        MergeOfResultFiles.executeCommand(jc, args);

        Mockito.when(properties.getProperty(HostUsageDataStore.DATABASE_URL_PROP)).thenReturn(outputURL);
        HostUsageDataStore dataStoreOutput = new HostUsageDataStore(properties);

        Mockito.when(properties.getProperty(HostUsageDataStore.DATABASE_URL_PROP)).thenReturn(DATABASE_URL);
        HostUsageDataStore dataStoreBase = new HostUsageDataStore(properties);

        Assert.assertEquals(dataStoreOutput.getAllUsageEntries().size(), 3*NUMBER_OF_USAGE_ENTRIES);
        Assert.assertEquals(dataStoreBase.getAllUsageEntries(), dataStoreOutput.getAllUsageEntries());

        new File(databaseFileBefore).delete();
        new File(databaseFileAfter).delete();
        new File(outputFile).delete();

    }

    @Test
    public void testMergeDatacenterInfo() throws Exception {
        List<DatacenterInfo> datacenterInfoListAfter = datacenterInfoList.subList(15, 20);
        List<DatacenterInfo> datacenterInfoListBefore = datacenterInfoList.subList(0, 15);

        String databaseFileBefore = "DatabaseDatacenterTestBefore.sqlite3";
        String databaseURLBefore = "jdbc:sqlite:" + databaseFileBefore;

        String databaseFileAfter = "DatabaseDatacenterTestAfter.sqlite3";
        String databaseURLAfter = "jdbc:sqlite:" + databaseFileAfter;

        createAndPopulateDatacenterInfoDatabase(databaseURLBefore, datacenterInfoListBefore);
        createAndPopulateDatacenterInfoDatabase(databaseURLAfter, datacenterInfoListAfter);

        String outputFile =  "DatabaseDatacenterTestOutput.sqlite3";
        String outputURL = "jdbc:sqlite:" + outputFile;

        String[] args = {"datacenter", "--before", databaseURLBefore, "--after", databaseURLAfter, "--time", "75", "--output", outputURL};


        MergeOfResultFiles.executeCommand(jc, args);

        Mockito.when(properties.getProperty(DatacenterUsageDataStore.DATABASE_URL_PROP)).thenReturn(outputURL);
        DatacenterUsageDataStore dataStoreOutput = new DatacenterUsageDataStore(properties);

        Mockito.when(properties.getProperty(DatacenterUsageDataStore.DATABASE_URL_PROP)).thenReturn(DATABASE_URL);
        DatacenterUsageDataStore dataStoreBase = new DatacenterUsageDataStore(properties);

        Assert.assertEquals(dataStoreOutput.getAllDatacenterInfo().size(), NUMBER_OF_DATACENTER_INFO);
        Assert.assertEquals(dataStoreBase.getAllDatacenterInfo(), dataStoreOutput.getAllDatacenterInfo());

        new File(databaseFileBefore).delete();
        new File(databaseFileAfter).delete();
        new File(outputFile).delete();

    }

    @Test
    public void testMergeDatacenterInfo2() throws Exception {
        List<DatacenterInfo> datacenterInfoListAfter = datacenterInfoList.subList(15, 20);
        List<DatacenterInfo> datacenterInfoListBefore = datacenterInfoList.subList(0, 16);

        String databaseFileBefore = "DatabaseDatacenterTestBefore.sqlite3";
        String databaseURLBefore = "jdbc:sqlite:" + databaseFileBefore;

        String databaseFileAfter = "DatabaseDatacenterTestAfter.sqlite3";
        String databaseURLAfter = "jdbc:sqlite:" + databaseFileAfter;

        createAndPopulateDatacenterInfoDatabase(databaseURLBefore, datacenterInfoListBefore);
        createAndPopulateDatacenterInfoDatabase(databaseURLAfter, datacenterInfoListAfter);

        String outputFile =  "DatabaseDatacenterTestOutput.sqlite3";
        String outputURL = "jdbc:sqlite:" + outputFile;

        String[] args = {"datacenter", "--before", databaseURLBefore, "--after", databaseURLAfter, "--time", "75", "--output", outputURL};


        MergeOfResultFiles.executeCommand(jc, args);

        Mockito.when(properties.getProperty(DatacenterUsageDataStore.DATABASE_URL_PROP)).thenReturn(outputURL);
        DatacenterUsageDataStore dataStoreOutput = new DatacenterUsageDataStore(properties);

        Mockito.when(properties.getProperty(DatacenterUsageDataStore.DATABASE_URL_PROP)).thenReturn(DATABASE_URL);
        DatacenterUsageDataStore dataStoreBase = new DatacenterUsageDataStore(properties);

        Assert.assertEquals(dataStoreOutput.getAllDatacenterInfo().size(), NUMBER_OF_DATACENTER_INFO);
        Assert.assertEquals(dataStoreBase.getAllDatacenterInfo(), dataStoreOutput.getAllDatacenterInfo());

        new File(databaseFileBefore).delete();
        new File(databaseFileAfter).delete();
        new File(outputFile).delete();

    }

    @Test
    public void testMergeDatacenterInfo3() throws Exception {
        List<DatacenterInfo> datacenterInfoListAfter = datacenterInfoList.subList(15, 20);
        List<DatacenterInfo> datacenterInfoListBefore = datacenterInfoList.subList(0, 20);

        String databaseFileBefore = "DatabaseDatacenterTestBefore.sqlite3";
        String databaseURLBefore = "jdbc:sqlite:" + databaseFileBefore;

        String databaseFileAfter = "DatabaseDatacenterTestAfter.sqlite3";
        String databaseURLAfter = "jdbc:sqlite:" + databaseFileAfter;

        createAndPopulateDatacenterInfoDatabase(databaseURLBefore, datacenterInfoListBefore);
        createAndPopulateDatacenterInfoDatabase(databaseURLAfter, datacenterInfoListAfter);

        String outputFile =  "DatabaseDatacenterTestOutput.sqlite3";
        String outputURL = "jdbc:sqlite:" + outputFile;

        String[] args = {"datacenter", "--before", databaseURLBefore, "--after", databaseURLAfter, "--time", "75", "--output", outputURL};


        MergeOfResultFiles.executeCommand(jc, args);

        Mockito.when(properties.getProperty(DatacenterUsageDataStore.DATABASE_URL_PROP)).thenReturn(outputURL);
        DatacenterUsageDataStore dataStoreOutput = new DatacenterUsageDataStore(properties);

        Mockito.when(properties.getProperty(DatacenterUsageDataStore.DATABASE_URL_PROP)).thenReturn(DATABASE_URL);
        DatacenterUsageDataStore dataStoreBase = new DatacenterUsageDataStore(properties);

        Assert.assertEquals(dataStoreOutput.getAllDatacenterInfo().size(), NUMBER_OF_DATACENTER_INFO);
        Assert.assertEquals(dataStoreBase.getAllDatacenterInfo(), dataStoreOutput.getAllDatacenterInfo());

        new File(databaseFileBefore).delete();
        new File(databaseFileAfter).delete();
        new File(outputFile).delete();

    }

}