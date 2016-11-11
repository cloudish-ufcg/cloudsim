package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.preemption.DatacenterInfo;
import org.cloudbus.cloudsim.preemption.TaskState;
import org.cloudbus.cloudsim.preemption.UsageEntry;
import org.cloudbus.cloudsim.preemption.datastore.DataStore;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * Created by alessandro.fook on 11/11/16.
 */
public class MergeOfResultFilesTest {


    private Properties properties;
    private static final String DATABASE_FILE = "CompleteDatabaseMergeTest.sqlite3";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_FILE;
    private static final String DATASTORE_SQLITE_DRIVER = "org.sqlite.JDBC";


    // tasks characteristics
    private List<TaskState> taskStates;
    private static final int NUMBER_OF_TASKS = 100;
    private TaskState task;

    private int taskId, priority;
    private double cpuReq, finishingTime, runtime, submitTime;

    // UsageEntry characteristics

    private int HostId = 0;
    private UsageEntry entry0, entry1, entry2;
    private List<UsageEntry> entryList;

    private static final int NUMBER_OF_ENTRIES = 10;
    private static final double P0_USAGE = 40.558585;
    private static final double P1_USAGE = 21.9875422;
    private static final double P2_USAGE = 5.875646232;
    private static final int P0_VMS = 12;
    private static final int P1_VMS = 1;
    private static final int P2_VMS = 3;
    private static final double AVAILABLE_MIPS = 0.78598;


    //DatacenterInfo characteristics

    private double time;
    private static int VMS_RUNNING = 59;
    private static final int USAGE_BY_PRIORITY_0 = 40;
    private static final int USAGE_BY_PRIORITY_1 = 15;
    private static final int USAGE_BY_PRIORITY_2 = 4;
    private static final int VMS_FOR_SCHEDULING = 12;
    private static final int VMS_FOR_SCHEDULING_P_0 = 1;
    private static final int VMS_FOR_SCHEDULING_P_1 = 3;
    private static final int VMS_FOR_SCHEDULING_P_2 = 8;

    private DatacenterInfo info;
    private List<DatacenterInfo> datacenterInfoList;


    @Before
    public void setUp() throws Exception {

        properties = new Properties();

        // creating tasks

        taskStates = new ArrayList<>();

        taskId = 0;
        priority = 1;
        cpuReq = 0.02;
        submitTime = 0;
        runtime = 0.001;
        finishingTime = 0;

        for (int i = 0; i < NUMBER_OF_TASKS; i++) {

            task = new TaskState(taskId++, cpuReq, submitTime, finishingTime++, runtime, priority - 1, 0, 0, 0);
            taskStates.add(task);
        }

        // creating datacenterInfo
        time = 5; // time of the info has collected
        datacenterInfoList = new ArrayList<>();

        for (int i = 0; i < 20; i++) {

            info = new DatacenterInfo(time + (5 * i), VMS_RUNNING, USAGE_BY_PRIORITY_0, USAGE_BY_PRIORITY_1,
                    USAGE_BY_PRIORITY_2, VMS_FOR_SCHEDULING, VMS_FOR_SCHEDULING_P_0, VMS_FOR_SCHEDULING_P_1,
                    VMS_FOR_SCHEDULING_P_2);

            datacenterInfoList.add(info);
        }

        // creating UsageEntry
        time = 10; // time of the UsageEntry has collected

        entryList = new ArrayList<>();

        TreeSet<UsageEntry> tree = new TreeSet<>();
        Comparator<UsageEntry> comparator = new Comparator<UsageEntry>() {
            @Override
            public int compare(UsageEntry usageEntry, UsageEntry t1) {
                return usageEntry.getPriority() - t1.getPriority();
            }
        };

        for (int i = 0; i < NUMBER_OF_ENTRIES; i++) {

            entry0 = new UsageEntry(HostId, time + (10 * i), P0_USAGE, P0_VMS, 3, AVAILABLE_MIPS);
            entry1 = new UsageEntry(HostId, time + (10 * i), P1_USAGE, P1_VMS, 3, AVAILABLE_MIPS);
            entry2 = new UsageEntry(HostId, time + (10 * i), P2_USAGE, P2_VMS, 3, AVAILABLE_MIPS);

            entryList.add(entry0);
            entryList.add(entry1);
            entryList.add(entry2);
        }
    }

    @After
    public void tearDown() throws Exception {
        new File(DATABASE_FILE).delete();
    }


    // methods for creating dataBases

    private void createAndPopulateTaskDatabase(String databaseUrl) throws ClassNotFoundException, SQLException {
        Class.forName(DATASTORE_SQLITE_DRIVER);
        Connection connection = DriverManager.getConnection(databaseUrl);

        if (connection != null) {
            // Creating the database
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS tasks("
                    + "taskId INTEGER NOT NULL, "
                    + "cpuReq REAL, "
                    + "submitTime REAL, "
                    + "finishTime REAL, "
                    + "runtime REAL, "
                    + "priority INTEGER, "
                    + "preemptions INTEGER, "
                    + "backfillingChoices INTEGER, "
                    + "migrations INTEGER, "
                    + "PRIMARY KEY (taskId)"
                    + ")");
            statement.close();

            String INSERT_CLOUDLET_SQL = "INSERT INTO tasks"
                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // populating the database
            for (int i = 0; i < NUMBER_OF_ENTRIES; i++) {
                PreparedStatement insertMemberStatement = connection
                        .prepareStatement(INSERT_CLOUDLET_SQL);
                TaskState taskState = taskStates.get(i);
                insertMemberStatement.setInt(1, taskState.getTaskId());
                insertMemberStatement.setDouble(2, taskState.getCpuReq());
                insertMemberStatement.setDouble(3, taskState.getSubmitTime());
                insertMemberStatement.setDouble(4, taskState.getFinishTime());
                insertMemberStatement.setDouble(5, taskState.getRuntime());
                insertMemberStatement.setInt(6, taskState.getPriority());
                insertMemberStatement.setInt(7, taskState.getNumberOfPreemptions());
                insertMemberStatement.setInt(8, taskState.getNumberOfBackfillingChoices());
                insertMemberStatement.setInt(9, taskState.getNumberOfMigrations());
                insertMemberStatement.execute();
            }
            connection.close();
        }
    }

    private void createAndPopulateUsageEntryDatabase(String databaseUrl) throws ClassNotFoundException, SQLException {
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

    private void createAndPopulateDatacenterInfoDatabase(String databaseUrl) throws ClassNotFoundException, SQLException {
        Class.forName(DATASTORE_SQLITE_DRIVER);
        Connection connection = DriverManager.getConnection(databaseUrl);

        if (connection != null) {
            // Creating the database
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS tasks("
                    + "taskId INTEGER NOT NULL, "
                    + "cpuReq REAL, "
                    + "submitTime REAL, "
                    + "finishTime REAL, "
                    + "runtime REAL, "
                    + "priority INTEGER, "
                    + "preemptions INTEGER, "
                    + "backfillingChoices INTEGER, "
                    + "migrations INTEGER, "
                    + "PRIMARY KEY (taskId)"
                    + ")");
            statement.close();

            String INSERT_CLOUDLET_SQL = "INSERT INTO tasks"
                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // populating the database
            for (int i = 0; i < NUMBER_OF_ENTRIES; i++) {
                PreparedStatement insertMemberStatement = connection
                        .prepareStatement(INSERT_CLOUDLET_SQL);
                TaskState taskState = taskStates.get(i);
                insertMemberStatement.setInt(1, taskState.getTaskId());
                insertMemberStatement.setDouble(2, taskState.getCpuReq());
                insertMemberStatement.setDouble(3, taskState.getSubmitTime());
                insertMemberStatement.setDouble(4, taskState.getFinishTime());
                insertMemberStatement.setDouble(5, taskState.getRuntime());
                insertMemberStatement.setInt(6, taskState.getPriority());
                insertMemberStatement.setInt(7, taskState.getNumberOfPreemptions());
                insertMemberStatement.setInt(8, taskState.getNumberOfBackfillingChoices());
                insertMemberStatement.setInt(9, taskState.getNumberOfMigrations());
                insertMemberStatement.execute();
            }
            connection.close();
        }
    }

    public void testMergeTask() {}
    public void testMergeUsageEntry() {}
    public void testMergeDatacenterInfo() {}

}