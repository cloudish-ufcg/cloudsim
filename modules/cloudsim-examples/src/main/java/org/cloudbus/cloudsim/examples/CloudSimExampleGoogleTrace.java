/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
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
import org.cloudbus.cloudsim.preemption.DatacenterInfo;
import org.cloudbus.cloudsim.preemption.PreemptiveDatacenter;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.Task;
import org.cloudbus.cloudsim.preemption.TaskState;
import org.cloudbus.cloudsim.preemption.TraceDatacenterBroker;
import org.cloudbus.cloudsim.preemption.UsageEntry;
import org.cloudbus.cloudsim.preemption.VmSchedulerMipsBased;
import org.cloudbus.cloudsim.preemption.policies.hostselection.WorstFitMipsBasedHostSelectionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.WorstFitAvailabilityAwareVmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.WorstFitPriorityBasedVmAllocationPolicy;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

/**
 * An example showing how to pause and resume the simulation, and create
 * simulation entities (a DatacenterBroker in this example) dynamically.
 */
public class CloudSimExampleGoogleTrace {

    private static List<Task> googleTasks;

    // //////////////////////// STATIC METHODS ///////////////////////

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {
        System.out.println("Starting CloudSimExample Google Trace ...");

        long now = System.currentTimeMillis();

        try {
            Properties properties = new Properties();
            FileInputStream input = new FileInputStream(args[0]);
            properties.load(input);

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
            @SuppressWarnings("unused")
            PreemptiveDatacenter datacenter0 = createGoogleDatacenter("cloud-0", properties);

            TraceDatacenterBroker broker = createGoogleTraceBroker(
                    "Google_Broker_0", properties);
//			int brokerId = broker.getId();
//
//			readDB(traceDatabaseURL);
//			createGoogleTasks(traceDatabaseURL, brokerId);
//
//			broker.submitTasks(googleTasks);

            CloudSim.startSimulation();

            List<TaskState> newList = broker.getStoredTasks();

            CloudSim.stopSimulation();

            printGoogleTaskStates(newList);
            newList.clear();

            List<UsageEntry> utilizationEntries = datacenter0.getHostUtilizationEntries();
            System.out.println("Utilization Entries: " + utilizationEntries.size());
            utilizationEntries.clear();

            List<DatacenterInfo> datacenterInfo = datacenter0.getAllDatacenterInfo();
            System.out.println("DatacenterInfo Entries: " + datacenterInfo.size());
            datacenterInfo.clear();


            Log.printLine("Execution Time "
                    + (((System.currentTimeMillis() - now) / 1000) / 60)
                    + " minutes");

            System.out.println("Execution Time "
                    + (((System.currentTimeMillis() - now) / 1000) / 60)
                    + " minutes");

            Log.printLine("CloudSimExample finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static void createGoogleTasks(String databaseURL, int userId)
            throws SQLException {
        // reading traceFile
        Log.printLine("Reading trace from URL ...");
        Connection conn = DriverManager.getConnection(databaseURL);

        googleTasks = new ArrayList<Task>();

        if (conn != null) {
            Log.printLine("Connected to the database");
            Statement statement = conn.createStatement();
            // ResultSet results = statement
            // .executeQuery("SELECT * FROM tasks WHERE submitTime > '0' LIMIT 100"
            // );

            // ResultSet results = statement
            // .executeQuery("SELECT submitTime, runtime, cpuReq, memReq FROM tasks LIMIT 100000"
            // );

            ResultSet results = statement
                    .executeQuery("SELECT * FROM tasks WHERE cpuReq > '0' AND memReq > '0' LIMIT 100000");
            // COUNT: 25834519
            // COUNT: 24776760

            int count = 0;
            while (results.next()) {
                count++;
                // System.out.println(results.getInt("COUNT(*)"));
                // System.out.println(results.getDouble("submitTime") <
                // fiveMinutes);
                System.out.println(results.getDouble("submitTime")
                        + ", "
                        // + results.getDouble("jid") + ", "
                        // + results.getInt("tid") + ", "
                        // + results.getString("user") + ", "
                        // + results.getInt("schedulingClass") + ", "
                        // + results.getInt("priority") + ", "
                        + (results.getDouble("runtime")) + ","
                        // + results.getDouble("endTime") + ", "
                        + results.getDouble("cpuReq") + ", "
                        + results.getDouble("memReq"));// + ", "
                // + results.getString("userClass"));

//				GoogleTask task = new GoogleTask(count,
//						results.getDouble("submitTime"),
//						results.getDouble("runtime"),
//						results.getDouble("cpuReq"),
//						results.getDouble("memReq"));
//				googleTasks.add(task);
            }
        }
    }

    private static void readDB(String databaseURL) throws SQLException {
        long fiveMinutes = 5 * 60 * 1000000;

        Connection conn = DriverManager.getConnection(databaseURL);

        if (conn != null) {
            Log.printLine("Connected to the database");
            Statement statement = conn.createStatement();

            int count = 0;
            long sum = 0;
            // while (sum < 20000000) {
            // ResultSet results = statement
            // .executeQuery("SELECT COUNT(*) FROM tasks WHERE cpuReq > '0' AND memReq > '0' AND submitTime >= '"
            // + (count * fiveMinutes)
            // + "' AND submitTime < '"
            // + ((count + 1) * fiveMinutes) + "'");

            ResultSet results = statement
                    .executeQuery("SELECT MAX(submitTime) FROM tasks WHERE cpuReq > '0' AND memReq > '0'");

            while (results.next()) {
                long requests = results.getLong("MAX(submitTime)");
                System.out.println(count + " - Number of request: " + requests);
                sum += requests;
            }
            count++;
            // }
        }
    }

    private static PreemptiveDatacenter createGoogleDatacenter(String name,
                                                               Properties properties) throws Exception {

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
        	PreemptionPolicy preemptionPolicy; 
        	if (properties.getProperty("preemption_policy_class") != null) {
        		Log.printLine("Creating a hosts with preemption policy " + properties.getProperty("preemption_policy_class"));
        		preemptionPolicy = (PreemptionPolicy) createInstance("preemption_policy_class", properties);
        	} else {
        		Log.printLine("Creating a hosts with defatult preemption policy FCFS based .");
        		preemptionPolicy = new FCFSBasedPreemptionPolicy(properties);
        	}
        	
            List<Pe> peList1 = new ArrayList<Pe>();

            peList1.add(new Pe(0, new PeProvisionerSimple(mipsPerHost)));

            PreemptiveHost host = new PreemptiveHost(hostId, peList1,
                    new VmSchedulerMipsBased(peList1), preemptionPolicy);

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
//			datacenter = new GoogleDatacenter(name, characteristics,
//					new VmAllocationPolicySimple(hostList), storageList, 0);
            datacenter = new PreemptiveDatacenter(name, characteristics,
                    new WorstFitPriorityBasedVmAllocationPolicy(hostList),
                    storageList, 0, properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static TraceDatacenterBroker createGoogleTraceBroker(String name,
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

    /**
     * Prints the Cloudlet objects
     *
     * @param newList list of Cloudlets
     */
    public static void printGoogleTaskStates(List<TaskState> newList) {
        int size = newList.size();
        TaskState googleTask;

        String indent = "    ";
        System.out.println();
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID" + indent + "VM ID" + indent + indent
                + "Time" + indent + "Finish Time" + indent + indent + "Priority" + indent + indent + "Availability" + indent + indent + "Preemptions" + indent + indent + "Migrations");

        DecimalFormat dft = new DecimalFormat("###.####");
        double totalVm0Availability = 0;
        int count0 = 0;
        double totalVm1Availability = 0;
        int count1 = 0;
        double totalVm2Availability = 0;
        int count2 = 0;
        
        double fulfillmentP0 = 0;
        double fulfillmentP1 = 0;
        double fulfillmentP2 = 0;
        
        int totalPreemptions = 0;
        int totalMigrations = 0;

        for (int i = 0; i < size; i++) {
            googleTask = newList.get(i);
            System.out.println(indent + googleTask.getTaskId() + indent + indent);

//			if (googleTask.getStatus() == Cloudlet.SUCCESS) {
            System.out.println("SUCCESS");

//            System.out.println("The vm:" + googleTask.getTaskId() + " has: runtime=" + googleTask.getRuntime() +
//                                " finished time=" + " submited time=" + googleTask.getFinishTime());

            double vmAvailabilty = googleTask.getRuntime() / (googleTask.getFinishTime() - googleTask.getSubmitTime());
            if (googleTask.getPriority() == 0) {
                totalVm0Availability += vmAvailabilty;
                count0++;
                if (vmAvailabilty >= 1) {
                	fulfillmentP0++;
                }
            } else if (googleTask.getPriority() == 1) {
                totalVm1Availability += vmAvailabilty;
                count1++;
                if (vmAvailabilty >= 0.9) {
                	fulfillmentP1++;
                }
            } else {
                totalVm2Availability += vmAvailabilty;
                count2++;
                if (vmAvailabilty >= 0.5) {
                	fulfillmentP2++;
                }
            }

            totalPreemptions += googleTask.getNumberOfPreemptions();
            totalMigrations += googleTask.getNumberOfMigrations();
            
			System.out.println(indent + indent + indent
					+ googleTask.getTaskId() + indent + indent + indent
					+ googleTask.getRuntime() + indent + indent + indent
					+ googleTask.getFinishTime() + indent + indent + indent
					+ googleTask.getPriority() + indent + indent + indent
					+ dft.format(vmAvailabilty) + indent + indent
					+ googleTask.getNumberOfPreemptions() + indent + indent
					+ googleTask.getNumberOfMigrations());
//				System.out.println(indent + indent + googleTask.getResourceId()
//						+ indent + indent + indent + googleTask.getTaskId()
//						+ indent + indent + indent + googleTask.getRuntime()
//						+ indent + indent + googleTask.getStartTime() + indent
//						+ indent + indent + googleTask.getFinishTime() + indent
//						+ indent + indent + googleTask.getPriority() + indent
//						+ indent + indent + dft.format(vmAvailabilty));
//			}
        }

        System.out.println("total of tasks: " + (count0 + count1 + count2));

        System.out.println("========== MEAN VM AVAILABILITY (priority 0) is " + dft.format((totalVm0Availability / count0)) + " =========");
        System.out.println("========== MEAN VM AVAILABILITY (priority 1) is " + dft.format((totalVm1Availability / count1)) + " =========");
        System.out.println("========== MEAN VM AVAILABILITY (priority 2) is " + dft.format((totalVm2Availability / count2)) + " =========");

        System.out.println("========== MEAN VM AVAILABILITY is " + dft.format(((totalVm0Availability + totalVm1Availability + totalVm2Availability) / size)) + " =========");
        
        System.out.println("Total of Preemptions: " + totalPreemptions);
        System.out.println("Total of Migrations: " + totalMigrations);
        
        System.out.println("violatingP0: " + fulfillmentP0);
        System.out.println("totalP0: " + count0);
        System.out.println("violatingP1: " + fulfillmentP1);
        System.out.println("totalP1: " + count1);
        System.out.println("violatingP2: " + fulfillmentP2);
        System.out.println("totalP2: " + count2);
        
        System.out.println("========== % Fulfillment SLO (priority 0) is " + dft.format((fulfillmentP0 / count0)) + " =========");
        System.out.println("========== % Fulfillment SLO (priority 1) is " + dft.format((fulfillmentP1 / count1)) + " =========");
        System.out.println("========== % Fulfillment SLO (priority 2) is " + dft.format((fulfillmentP2 / count2)) + " =========");

    }
    
    private static Object createInstance(String propName, Properties properties) throws Exception {
		return Class.forName(properties.getProperty(propName)).getConstructor(Properties.class)
				.newInstance(properties);
	}
}