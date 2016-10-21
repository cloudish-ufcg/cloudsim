package org.cloudbus.cloudsim.preemption;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.preemption.policies.hostselection.WorstFitMipsBasedHostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.*;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;


/**
 * Created by Alessandro Lia Fook Santos and Joao Victor Mafra on 20/10/16.
 */
public class TraceDatacenterBrokerTest {

    private SimEvent event;
    private TraceDatacenterBroker broker;
    private PreemptiveDatacenter datacenter;
    private Properties properties;
    private static String databaseOutputFile = "traceDatacenterBrokerTestOutput.sqlite3";
    private static String databaseOutputUrl = "jdbc:sqlite:" + databaseOutputFile;

    @Before
    public void setUp() {

        event = Mockito.mock(SimEvent.class);
        System.out.println("Starting CloudSimExample Google Trace ...");

        long now = System.currentTimeMillis();

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
        properties.setProperty("input_trace_database_url", "jdbc:sqlite:/local/alessandro.fook/workspace/google-trace-processor/simulated_trace_data.sqlite3");
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

        for (int hostId = 0; hostId < numberOfHosts; hostId++) {
            List<Pe> peList1 = new ArrayList<Pe>();

            peList1.add(new Pe(0, new PeProvisionerSimple(mipsPerHost)));

            PreemptiveHost host = new PreemptiveHost(hostId, peList1,
                    new VmSchedulerMipsBased(peList1), 3);

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
                            new WorstFitMipsBasedHostSelectionPolicy()),
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

    @Test
    public void testLoadNextTaskEvents() {

        Mockito.when(event.getTag()).thenReturn(broker.LOAD_NEXT_TASKS_EVENT);
        broker.processEvent(event);
        Assert.assertEquals(19809, broker.getSubmittedTasks());

        broker.processEvent(event);
        Assert.assertEquals(26412, broker.getSubmittedTasks());
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

        Mockito.when(event.getTag()).thenReturn(broker.STORE_FINISHED_TASKS_EVENT);
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
            TaskState taskState = new TaskState(id++, cpuReq, submitTime, now, runTime, priority);
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
