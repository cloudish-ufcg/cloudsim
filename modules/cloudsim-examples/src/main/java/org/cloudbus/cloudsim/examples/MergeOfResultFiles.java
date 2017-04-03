package org.cloudbus.cloudsim.examples;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudbus.cloudsim.preemption.DatacenterInfo;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.TaskState;
import org.cloudbus.cloudsim.preemption.UsageEntry;
import org.cloudbus.cloudsim.preemption.datastore.*;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Created by Joao Victor Mafra and Alessandro Lia Fook Santos on 07/11/16.
 */

public class MergeOfResultFiles {

	private static Properties properties;
	private static double INTERVAL_SIZE = 8640.0;

    public static void main(String[] args) throws Exception{

        JCommander jc = new JCommander();
        executeCommand(jc, args);
    }

    public static void executeCommand(JCommander jc, String[] args) throws Exception{

        Command task = new Command();
        jc.addCommand("task", task);

        Command utilization = new Command();
        jc.addCommand("utilization", utilization);

        Command datacenter = new Command();
        jc.addCommand("datacenter", datacenter);
        
        Command conclude = new Command();
        jc.addCommand("conclude", conclude);

        try {
            jc.parse(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            jc.usage();
            return;
        }

        String parsedCommand = jc.getParsedCommand();

        if (parsedCommand == null) {
            jc.usage();
            return;
        }

        properties = new Properties();

        if (parsedCommand.equals("task")){

            double time = Double.parseDouble(task.time);

            int factor;

            if (task.id_factor.equals("")){
            	factor = 0;
			} else {
            	factor = Integer.parseInt(task.id_factor);
			}

            properties.setProperty(TaskDataStore.DATABASE_URL_PROP, task.path_before);
            TaskDataStore dataStore = new TaskDataStore(properties);
            List<TaskState> tasks_before = dataStore.getTasksFinishedBefore(time);

            properties.setProperty(TaskDataStore.DATABASE_URL_PROP, task.path_after);
            dataStore = new TaskDataStore(properties);
            List<TaskState> tasks_after = modifyStateId(dataStore.getAllTasks(), factor);

            List<TaskState> listOfAllTasks = new ArrayList<>();
            listOfAllTasks.addAll(tasks_before);
            listOfAllTasks.addAll(tasks_after);

            properties.setProperty(TaskDataStore.DATABASE_URL_PROP, task.path_output);
			storeTasks(listOfAllTasks);

        } else if (parsedCommand.equals("utilization")){

            double time = Double.parseDouble(utilization.time);

            properties.setProperty(HostUsageDataStore.DATABASE_URL_PROP, utilization.path_before);
            HostUsageDataStore inputDataStore = new HostUsageDataStore(properties);

			properties.setProperty(HostUsageDataStore.DATABASE_URL_PROP, utilization.path_output);
			HostUsageDataStore outputDataStore = new HostUsageDataStore(properties);

			loadAndStoreUsageEntries(0, time, inputDataStore, outputDataStore);

            properties.setProperty(HostUsageDataStore.DATABASE_URL_PROP, utilization.path_after);
			inputDataStore = new HostUsageDataStore(properties);

			loadAndStoreUsageEntries(time, inputDataStore.getMaxTraceTime() + 1, inputDataStore, outputDataStore);

        } else if (parsedCommand.equals("datacenter")){

            double time = Double.parseDouble(datacenter.time);

            properties.setProperty(DatacenterUsageDataStore.DATABASE_URL_PROP, datacenter.path_before);
            DatacenterUsageDataStore dataStore = new DatacenterUsageDataStore(properties);
            List<DatacenterInfo> datacenter_before = dataStore.getDatacenterInfoFinishedBefore(time);

            properties.setProperty(DatacenterUsageDataStore.DATABASE_URL_PROP, datacenter.path_after);
            dataStore = new DatacenterUsageDataStore(properties);
            List<DatacenterInfo> datacenter_after = dataStore.getAllDatacenterInfo();

            List<DatacenterInfo> listOfAllDatacenterInfo = new ArrayList<>();
            listOfAllDatacenterInfo.addAll(datacenter_before);
            listOfAllDatacenterInfo.addAll(datacenter_after);

            properties.setProperty(DatacenterUsageDataStore.DATABASE_URL_PROP, datacenter.path_output);

			storeDatacenterInfo(listOfAllDatacenterInfo);

		} else if (parsedCommand.equals("conclude")) {

			double time = Double.parseDouble(conclude.time);

			properties.setProperty(TaskDataStore.DATABASE_URL_PROP, conclude.path_before);
			TaskDataStore dataStore = new TaskDataStore(properties);
			List<TaskState> tasks_before = dataStore.getTasksFinishedBefore(time);

			System.out
					.println("Number os tasks before: " + tasks_before.size());
			properties.setProperty(PreemptableVmDataStore.CHECKPOINT_URL, conclude.path_after);

			PreemptableVmDataStore vmDataStore = new PreemptableVmDataStore(properties);
			List<TaskState> concludingTasks = new ArrayList<>();
			concludeAndAddTasksToList(time, vmDataStore, concludingTasks);
			System.out.println("Number of checkpoint tasks: " + concludingTasks.size());

			List<TaskState> listOfAllTasks = new ArrayList<>();
			listOfAllTasks.addAll(tasks_before);
			listOfAllTasks.addAll(concludingTasks);

			System.out.println("Number of all tasks: " + listOfAllTasks.size());

			properties.setProperty(TaskDataStore.DATABASE_URL_PROP, conclude.path_output);

			storeTasks(listOfAllTasks);
		}
	}

	private static void concludeAndAddTasksToList(double time, PreemptableVmDataStore vmDataStore,
												  List<TaskState> concludingTasks) {

		System.out.println("Concluding running vms of checkpoint.");

		// runningVm
		List<PreemptableVm> allRunningVms = vmDataStore.getAllRunningVms();
		System.out.println("Number of running Vms: " + allRunningVms.size());

		for (PreemptableVm runningVm : allRunningVms) {

            TaskState taskState = new TaskState(runningVm.getId(),
                    runningVm.getMips(), runningVm.getSubmitTime(), time,
                    runningVm.getActualRuntime(time),
                    runningVm.getPriority(),
                    runningVm.getNumberOfPreemptions(),
                    runningVm.getNumberOfBackfillingChoice(),
                    runningVm.getNumberOfMigrations(), runningVm.getFirstTimeAllocated());
            concludingTasks.add(taskState);
        }

		System.out.println("Concluding waiting vms of checkpoint.");
		// waitingVm
		List<PreemptableVm> allWaitingVms = vmDataStore.getAllWaitingVms();
		System.out.println("Number of waiting Vms: " + allWaitingVms.size());

		for (PreemptableVm waitingVm : allWaitingVms) {

            TaskState taskState = new TaskState(waitingVm.getId(),
                    waitingVm.getMips(), waitingVm.getSubmitTime(), time,
                    waitingVm.getActualRuntime(time),
                    waitingVm.getPriority(),
                    waitingVm.getNumberOfPreemptions(),
                    waitingVm.getNumberOfBackfillingChoice(),
                    waitingVm.getNumberOfMigrations(), waitingVm.getFirstTimeAllocated());
            concludingTasks.add(taskState);
        }
	}

	private static void loadAndStoreUsageEntries(double minTime, double maxTime, HostUsageDataStore inputDataStore,
												 HostUsageDataStore outputDataStore) throws SQLException,
																							ClassNotFoundException {

		int intervalIndex = 0;
		List<UsageEntry> loadedEntries = inputDataStore.getUsageEntryInterval(intervalIndex, INTERVAL_SIZE, minTime, maxTime);

		while (loadedEntries != null){
			storeUsageEntries(loadedEntries, outputDataStore);
            loadedEntries.clear();
            loadedEntries = inputDataStore.getUsageEntryInterval(++intervalIndex, INTERVAL_SIZE, minTime, maxTime);
        }
	}

	private static void storeTasks(List<TaskState> listOfAllTasks) {

		TaskDataStore dataStore = new TaskDataStore(properties);
		int count = 0;
		int subListSize = (int) Math.ceil(listOfAllTasks.size() * 0.02);

		for (int i = 0; i < listOfAllTasks.size(); i += subListSize) {
			System.out.println("Interval = " + count++);
			System.out.println("Min: " + i);
			System.out.println("Max: "
					+ Math.min(i + subListSize, listOfAllTasks.size()));
			List<TaskState> subList = listOfAllTasks.subList(i,
					Math.min(i + subListSize, listOfAllTasks.size()));
			dataStore.addTaskList(subList);
		}
	}

	private static void storeUsageEntries(List<UsageEntry> listOfUsageEntries, HostUsageDataStore outputDataStore) {

		int count = 0;
		int subListSize = (int) Math.ceil(listOfUsageEntries.size() * 0.02);

		for (int i = 0; i < listOfUsageEntries.size(); i += subListSize) {
			System.out.println("Interval = " + count++);
			System.out.println("Min: " + i);
			System.out.println("Max: "
					+ Math.min(i + subListSize, listOfUsageEntries.size()));
			List<UsageEntry> subList = listOfUsageEntries.subList(i,
					Math.min(i + subListSize, listOfUsageEntries.size()));
			outputDataStore.addUsageEntries(subList);
		}
	}

	private static void storeDatacenterInfo(List<DatacenterInfo> listOfDatacenterInfo) {

		DatacenterUsageDataStore dataStore = new DatacenterUsageDataStore(properties);
		int count = 0;
		int subListSize = (int) Math.ceil(listOfDatacenterInfo.size() * 0.05);

		for (int i = 0; i < listOfDatacenterInfo.size(); i += subListSize) {
			System.out.println("Interval = " + count++);
			System.out.println("Min: " + i);
			System.out.println("Max: "
					+ Math.min(i + subListSize, listOfDatacenterInfo.size()));
			List<DatacenterInfo> subList = listOfDatacenterInfo.subList(i,
					Math.min(i + subListSize, listOfDatacenterInfo.size()));
			dataStore.addDatacenterInfo(subList);
		}
	}

	private static class Command {
		@Parameter(names = "--before", description = "path of DB before time of checkpoint")
		String path_before = "";

		@Parameter(names = "--after", description = "path of DB after time of checkpoint")
		String path_after = "";

		@Parameter(names = "--time", description = "time of checkpoint")
		String time = "";

		@Parameter(names = "--output", description = "path where will be saved the new file")
		String path_output = "";

		@Parameter(names = "--factor", description = "factor to which vms ids will be added")
		String id_factor = "";
	}

	private static List<TaskState> modifyStateId(List<TaskState> listOfTasks, int factor){

    	List<TaskState> finalListOfTasks = new ArrayList<TaskState>();
    	TaskState taskState;

    	int i = 0;
    	for (TaskState task : listOfTasks){

			taskState = new TaskState(task.getTaskId() + factor, task.getCpuReq(), task.getSubmitTime(), task.getFinishTime(),
					task.getRuntime(), task.getPriority(), task.getNumberOfPreemptions(), task.getNumberOfBackfillingChoices(),
					task.getNumberOfMigrations(), task.getFirstTimeAllocated());

    		finalListOfTasks.add(taskState);
		}

		return finalListOfTasks;
	}
}
