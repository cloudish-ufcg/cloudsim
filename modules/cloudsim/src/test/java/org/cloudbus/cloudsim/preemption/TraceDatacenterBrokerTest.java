package org.cloudbus.cloudsim.preemption;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.policies.hostselection.WorstFitMipsBasedHostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Created by Alessandro Lia Fook Santos and Joao Victor Mafra on 20/10/16.
 */
public class TraceDatacenterBrokerTest {

    private SimEvent event;
    private TraceDatacenterBroker broker;
    private PreemptiveDatacenter datacenter;
    private Properties properties;
    private static String DATASTORE_SQLITE_DRIVER = "org.sqlite.JDBC";

    private static String databaseOutputFile = "traceDatacenterBrokerTestOutput.sqlite3";
    private static String databaseOutputUrl = "jdbc:sqlite:" + databaseOutputFile;

    private static String databaseFile = "inputTraceTest.sqlite3";
    private static String databaseURL = "jdbc:sqlite:" + databaseFile;

    private static double DEFAULT_RUNTIME = 1000;
    private static int NUMBER_OF_TASKS = 100;

    @BeforeClass
    public static void setUp() throws Exception{
        createAndPopulateTestDatabase();
    }

    private static void createAndPopulateTestDatabase() throws ClassNotFoundException, SQLException {
        Class.forName(DATASTORE_SQLITE_DRIVER);
        Connection connection = DriverManager.getConnection(databaseURL);

        if (connection != null) {
            // Creating the database
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS tasks("
                    + "submitTime REAL, "
                    + "jid REAL, "
                    + "tid INTEGER, "
                    + "user TEXT, "
                    + "schedulingClass INTEGER, "
                    + "priority INTEGER, "
                    + "runtime REAL, "
                    + "endTime REAL, "
                    + "cpuReq REAL, "
                    + "memReq REAL, "
                    + "userClass TEXT" + ")");
            statement.close();

            String INSERT_CLOUDLET_SQL = "INSERT INTO tasks"
                    + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // populating the database
            for (int i = 1; i <= NUMBER_OF_TASKS; i++) {
                PreparedStatement insertMemberStatement = connection
                        .prepareStatement(INSERT_CLOUDLET_SQL);
                // submit time
                if (i < NUMBER_OF_TASKS/2 + 1){
                    insertMemberStatement.setDouble(1, getTimeInMicro(0));
                } else {
                    insertMemberStatement.setDouble(1, getTimeInMicro(1));
                }
                insertMemberStatement.setDouble(2, -1); // jid is not important for now
                insertMemberStatement.setInt(3, -1); // tid is not important for now
                insertMemberStatement.setNString(4, "user"); // user is not important for now
                insertMemberStatement.setInt(5, -1); // scheduling class is not important for now
                insertMemberStatement.setInt(6, -1); // priority is not important for now
                insertMemberStatement.setDouble(7, DEFAULT_RUNTIME); // runtime
                insertMemberStatement.setDouble(8, i + DEFAULT_RUNTIME); // endtime
                insertMemberStatement.setDouble(9, 1); // cpuReq is not important for now
                insertMemberStatement.setDouble(10, 1); // memReq is not important for now
                insertMemberStatement.setNString(11, "userClass"); // userClass is not important for now
                insertMemberStatement.execute();
            }
            connection.close();
        }
    }
    private static double getTimeInMicro(double timeInMinutes) {
        return timeInMinutes * 60 * 1000000;
    }


    @Before
    public void init() {

        event = Mockito.mock(SimEvent.class);
        System.out.println("Starting CloudSimExample Google Trace ...");

        try {
            properties = createProperties();

            if (properties.getProperty("logging") != null && properties.getProperty("logging").equals("no")) {
                Log.disable();
            }
            Log.printLine("Starting CloudSimExample Google Trace ...");


            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1; // number of grid users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at
            // list one of them to run a CloudSim simulation
            datacenter = createPreemptiveDatacenter("cloud-0", properties);
            datacenter.startEntity();

            broker = createTraceDatacenterBroker("Google_Broker_0", properties);
            broker.startEntity();
            broker.getDatacenterIdsList().add(datacenter.getId());

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Properties createProperties() {

        Properties properties = new Properties();

        properties.setProperty("logging", "no");
        properties.setProperty("input_trace_database_url", databaseURL);
        properties.setProperty("number_of_hosts", "1");
        properties.setProperty("total_cpu_capacity", "6603.25");
        properties.setProperty("loading_interval_size", "1");
        properties.setProperty("storing_interval_size", "4");
        properties.setProperty("output_tasks_database_url", databaseOutputUrl);
        properties.setProperty("utilization_database_url", databaseOutputUrl);
        properties.setProperty("utilization_storing_interval_size", "8");
        properties.setProperty("datacenter_database_url", databaseOutputUrl);
        properties.setProperty("collect_datacenter_summary_info", "no");
        properties.setProperty("make_checkpoint", "no");

        return properties;
    }

    private static PreemptiveDatacenter createPreemptiveDatacenter(String name,
                                                                   Properties properties) {

        int numberOfHosts = Integer.parseInt(properties
                .getProperty("number_of_hosts"));
        double totalMipsCapacity = Double.parseDouble(properties
                .getProperty("total_cpu_capacity"));
        double mipsPerHost = totalMipsCapacity / numberOfHosts;

        Log.printLine("Creating a datacenter with " + totalMipsCapacity
                + " total capacity and " + numberOfHosts
                + " hosts, each one with " + mipsPerHost + " mips.");

        List<PreemptiveHost> hostList = new ArrayList<PreemptiveHost>();

        properties.setProperty(PreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, "3");
		
        for (int hostId = 0; hostId < numberOfHosts; hostId++) {
            List<Pe> peList1 = new ArrayList<Pe>();

            peList1.add(new Pe(0, new PeProvisionerSimple(mipsPerHost)));

            PreemptiveHost host = new PreemptiveHost(hostId, peList1,
                    new VmSchedulerMipsBased(peList1), new FCFSBasedPreemptionPolicy(properties));

            hostList.add(host);
        }

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.1; // the cost of using storage in this
        // resource
        double costPerBw = 0.1; // the cost of using bw in this resource

        // we are not adding SAN devices by now
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        PreemptiveDatacenter datacenter = null;
        try {
            datacenter = new PreemptiveDatacenter(name, characteristics,
                    new PreemptableVmAllocationPolicy(hostList,
                            "worst_fit_mips"),
                    storageList, 0, properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static TraceDatacenterBroker createTraceDatacenterBroker(String name,
                                                                     Properties properties) {

        TraceDatacenterBroker broker = null;
        try {
            broker = new TraceDatacenterBroker(name, properties);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    @After
    public void tearDown() {
        new File(databaseOutputFile).delete();
    }

    @AfterClass
    public static void tearDownClass() {
        new File(databaseFile).delete();
    }

    @Test
    public void testLoadNextTaskEvents() {

        Mockito.when(event.getTag()).thenReturn(TraceDatacenterBroker.LOAD_NEXT_TASKS_EVENT);
        broker.processEvent(event);
        Assert.assertEquals(50, broker.getSubmittedTasks());

        broker.processEvent(event);
        Assert.assertEquals(100, broker.getSubmittedTasks());
    }

    @Test
    public void testDestroyVMAck() {

        List<TaskState> taskStates = new ArrayList<>();
        populateListOfTaskStates(taskStates);
        int userId = 0;
        double memReq = 0.24;
        int concludedTasks = 0;

        for (TaskState taskState : taskStates) {
            PreemptableVm vm = new PreemptableVm(taskState.getTaskId(), userId, taskState.getCpuReq(), memReq,
                                                 taskState.getSubmitTime(), taskState.getPriority(),
                                                 taskState.getPriority());

            Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY_ACK);
            Mockito.when(event.getData()).thenReturn(vm);
            broker.processEvent(event);
            concludedTasks++;

            Assert.assertEquals(concludedTasks, broker.getConcludedTasks());
        }

        Assert.assertArrayEquals(taskStates.toArray(), broker.getFinishedTasks().toArray());
    }

    @Test
    public void testStoredFinishedTasks() {

        List<TaskState> taskStates = new ArrayList<>();
        populateListOfTaskStates(taskStates);
        int userId = 0;
        double memReq = 0.24;
        int concludedTasks = 0;

        for (TaskState taskState : taskStates) {
            PreemptableVm vm = new PreemptableVm(taskState.getTaskId(), userId, taskState.getCpuReq(), memReq,
                    taskState.getSubmitTime(), taskState.getPriority(),
                    taskState.getPriority());

            Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY_ACK);
            Mockito.when(event.getData()).thenReturn(vm);
            broker.processEvent(event);
            concludedTasks++;

            Assert.assertEquals(concludedTasks, broker.getConcludedTasks());
        }

        Assert.assertArrayEquals(taskStates.toArray(), broker.getFinishedTasks().toArray());

        Mockito.when(event.getTag()).thenReturn(TraceDatacenterBroker.STORE_FINISHED_TASKS_EVENT);
        broker.processEvent(event);

        Assert.assertEquals(50, broker.getConcludedTasks());
        Assert.assertEquals(0, broker.getFinishedTasks().size());
    }

    @Test
    public void testEndOfSimulation() {

        List<TaskState> taskStates = new ArrayList<>();
        populateListOfTaskStates(taskStates);
        int userId = 0;
        double memReq = 0.24;
        int concludedTasks = 0;

        for (TaskState taskState : taskStates) {
            PreemptableVm vm = new PreemptableVm(taskState.getTaskId(), userId, taskState.getCpuReq(), memReq,
                    taskState.getSubmitTime(), taskState.getPriority(),
                    taskState.getPriority());

            Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_DESTROY_ACK);
            Mockito.when(event.getData()).thenReturn(vm);
            broker.processEvent(event);
            concludedTasks++;

            Assert.assertEquals(concludedTasks, broker.getConcludedTasks());
        }

        Assert.assertArrayEquals(taskStates.toArray(), broker.getFinishedTasks().toArray());

        Mockito.when(event.getTag()).thenReturn(CloudSimTags.END_OF_SIMULATION);
        broker.processEvent(event);

        Assert.assertEquals(50, broker.getConcludedTasks());
        Assert.assertEquals(0, broker.getFinishedTasks().size());
    }

    public void populateListOfTaskStates(List<TaskState> taskStates) {

        int id = 0;
        double submitTime = 0;
        double runTime = 1;
        double cpuReq = 0.02;
        int priority = 1;

        for (int i = 0; i < 50; i++) {
            double now = CloudSim.clock();
            TaskState taskState = new TaskState(id++, cpuReq, submitTime, now, runTime, priority, 0, 0, 0);
            taskStates.add(taskState);
        }
    }

    @Test
    public void testProcessOtherEvent() {
        Mockito.when(event.getTag()).thenReturn(CloudSimTags.VM_CREATE);
        broker.processEvent(event);
    }

    @Test
    public void testProcessNullEvent() {
        broker.processEvent(null);
    }
}
