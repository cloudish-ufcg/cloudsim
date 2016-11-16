package org.cloudbus.cloudsim.examples;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cloudbus.cloudsim.preemption.DatacenterInfo;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.TaskState;
import org.cloudbus.cloudsim.preemption.UsageEntry;
import org.cloudbus.cloudsim.preemption.datastore.DatacenterUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.HostUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.PreemptableVmDataStore;
import org.cloudbus.cloudsim.preemption.datastore.TaskDataStore;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Created by jvmafra on 07/11/16.
 */
public class MergeOfResultFiles {


    public static void main(String[] args) throws Exception{
        JCommander jc = new JCommander();

        executeCommand(jc, args);


    }

    public static void executeCommand(JCommander jc, String[] args) {
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

        if (parsedCommand.equals("task")){
            double time = Double.parseDouble(task.time);
            Properties properties = new Properties();

            properties.setProperty(TaskDataStore.DATABASE_URL_PROP, task.path_before);
            TaskDataStore dataStore = new TaskDataStore(properties);
            List<TaskState> tasks_before = dataStore.getTasksFinishedBefore(time);

            properties.setProperty(TaskDataStore.DATABASE_URL_PROP, task.path_after);
            dataStore = new TaskDataStore(properties);
            List<TaskState> tasks_after = dataStore.getAllTasks();

            List<TaskState> listOfAllTasks = new ArrayList<>();
            listOfAllTasks.addAll(tasks_before);
            listOfAllTasks.addAll(tasks_after);

            properties.setProperty(TaskDataStore.DATABASE_URL_PROP, task.path_output);
            dataStore = new TaskDataStore(properties);
            dataStore.addTaskList(listOfAllTasks);

            List<TaskState> final_states = dataStore.getAllTasks();
            System.out.println("Number of tasks in final output = " + final_states.size());


        } else if (parsedCommand.equals("utilization")){
            double time = Double.parseDouble(utilization.time);
            Properties properties = new Properties();

            properties.setProperty(HostUsageDataStore.DATABASE_URL_PROP, utilization.path_before);
            HostUsageDataStore dataStore = new HostUsageDataStore(properties);
            List<UsageEntry> usage_before = dataStore.getUsageEntriesFinishedBefore(time);

            properties.setProperty(HostUsageDataStore.DATABASE_URL_PROP, utilization.path_after);
            dataStore = new HostUsageDataStore(properties);
            List<UsageEntry> usage_after = dataStore.getAllUsageEntries();

            List<UsageEntry> listOfAllUsageEntries = new ArrayList<>();
            listOfAllUsageEntries.addAll(usage_before);
            listOfAllUsageEntries.addAll(usage_after);

            properties.setProperty(HostUsageDataStore.DATABASE_URL_PROP, utilization.path_output);
            dataStore = new HostUsageDataStore(properties);
            dataStore.addUsageEntries(listOfAllUsageEntries);

        } else if (parsedCommand.equals("datacenter")){
            double time = Double.parseDouble(datacenter.time);
            Properties properties = new Properties();

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
            dataStore = new DatacenterUsageDataStore(properties);
            dataStore.addDatacenterInfo(listOfAllDatacenterInfo);

		} else if (parsedCommand.equals("conclude")) {
			double time = Double.parseDouble(conclude.time);
			Properties properties = new Properties();

			properties.setProperty(TaskDataStore.DATABASE_URL_PROP,
					conclude.path_before);
			TaskDataStore dataStore = new TaskDataStore(properties);
			List<TaskState> tasks_before = dataStore
					.getTasksFinishedBefore(time);

			System.out
					.println("Number os tasks before: " + tasks_before.size());
			properties.setProperty(PreemptableVmDataStore.CHECKPOINT_URL,
					conclude.path_after);

			PreemptableVmDataStore vmDataStore = new PreemptableVmDataStore(
					properties);

			List<TaskState> concludingTasks = new ArrayList<TaskState>();

			System.out.println("Concluding running vms of checkpoint.");
			// runningVm
			List<PreemptableVm> allRunningVms = vmDataStore.getAllRunningVms();
			System.out
					.println("Number of running Vms: " + allRunningVms.size());
			for (PreemptableVm runningVm : allRunningVms) {
				TaskState taskState = new TaskState(runningVm.getId(),
						runningVm.getMips(), runningVm.getSubmitTime(), time,
						runningVm.getActualRuntime(time),
						runningVm.getPriority(),
						runningVm.getNumberOfPreemptions(),
						runningVm.getNumberOfBackfillingChoice(),
						runningVm.getNumberOfMigrations());
				concludingTasks.add(taskState);
			}

			System.out.println("Concluding waiting vms of checkpoint.");
			// waitingVm
			List<PreemptableVm> allWaitingVms = vmDataStore.getAllWaitingVms();
			System.out
					.println("Number of waiting Vms: " + allWaitingVms.size());
			for (PreemptableVm waitingVm : allWaitingVms) {
				TaskState taskState = new TaskState(waitingVm.getId(),
						waitingVm.getMips(), waitingVm.getSubmitTime(), time,
						waitingVm.getActualRuntime(time),
						waitingVm.getPriority(),
						waitingVm.getNumberOfPreemptions(),
						waitingVm.getNumberOfBackfillingChoice(),
						waitingVm.getNumberOfMigrations());
				concludingTasks.add(taskState);
			}

			System.out.println("Number of checkpoint tasks: "
					+ concludingTasks.size());

			List<TaskState> listOfAllTasks = new ArrayList<>();
			listOfAllTasks.addAll(tasks_before);
			listOfAllTasks.addAll(concludingTasks);

			System.out.println("Number of all tasks: " + listOfAllTasks.size());

			properties.setProperty(TaskDataStore.DATABASE_URL_PROP,
					conclude.path_output);

			dataStore = new TaskDataStore(properties);

			// dataStore.addTaskList(listOfAllTasks);
			int count = 0;
			int subListSize = (int) Math.round(listOfAllTasks.size() * 0.05);
			for (int i = 0; i < listOfAllTasks.size(); i += subListSize) {
				System.out.println("Interval = " + count++);
				System.out.println("Min: " + i);
				System.out.println("Max: "
						+ Math.min(i + subListSize, listOfAllTasks.size()));
				List<TaskState> subList = listOfAllTasks.subList(i,
						Math.min(i + subListSize, listOfAllTasks.size()));
				dataStore.addTaskList(subList);
			}

			// for (TaskState taskState : listOfAllTasks) {
			// listOfAllTasks.
			//
			// }

			// List<TaskState> final_states = dataStore.getAllTasks();
			// System.out.println("Number of tasks in final output = " +
			// final_states.size());
			// printGoogleTaskStates(final_states);

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
	}

	public static void printGoogleTaskStates(List<TaskState> newList) {
		int size = newList.size();
		TaskState googleTask;

		DecimalFormat dft = new DecimalFormat("###.####");
		double totalVm0Availability = 0;
		int count0 = 0;
		double totalVm1Availability = 0;
		int count1 = 0;
		double totalVm2Availability = 0;
		int count2 = 0;
		int totalPreemptions = 0;
		int totalMigrations = 0;

		for (int i = 0; i < size; i++) {
			googleTask = newList.get(i);

			double vmAvailabilty = googleTask.getRuntime()
					/ (googleTask.getFinishTime() - googleTask.getSubmitTime());
			if (googleTask.getPriority() == 0) {
				totalVm0Availability += vmAvailabilty;
				count0++;
			} else if (googleTask.getPriority() == 1) {
				totalVm1Availability += vmAvailabilty;
				count1++;
			} else {
				totalVm2Availability += vmAvailabilty;
				count2++;
			}

			totalPreemptions += googleTask.getNumberOfPreemptions();
			totalMigrations += googleTask.getNumberOfMigrations();

		}

		System.out.println("total of tasks: " + (count0 + count1 + count2));

		System.out.println("========== MEAN VM AVAILABILITY (priority 0) is "
				+ dft.format((totalVm0Availability / count0)) + " =========");
		System.out.println("========== MEAN VM AVAILABILITY (priority 1) is "
				+ dft.format((totalVm1Availability / count1)) + " =========");
		System.out.println("========== MEAN VM AVAILABILITY (priority 2) is "
				+ dft.format((totalVm2Availability / count2)) + " =========");

		System.out
				.println("========== MEAN VM AVAILABILITY is "
						+ dft.format(((totalVm0Availability
								+ totalVm1Availability + totalVm2Availability) / size))
						+ " =========");

		System.out.println("Total of Preemptions: " + totalPreemptions);
		System.out.println("Total of Migrations: " + totalMigrations);

	}

}
