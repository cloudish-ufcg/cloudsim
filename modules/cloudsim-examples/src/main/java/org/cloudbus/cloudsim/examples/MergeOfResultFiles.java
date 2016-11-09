package org.cloudbus.cloudsim.examples;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.cloudbus.cloudsim.preemption.DatacenterInfo;
import org.cloudbus.cloudsim.preemption.TaskState;
import org.cloudbus.cloudsim.preemption.UsageEntry;
import org.cloudbus.cloudsim.preemption.datastore.DatacenterUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.HostUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.TaskDataStore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by jvmafra on 07/11/16.
 */
public class MergeOfResultFiles {

    public static void main(String[] args) throws Exception{
        JCommander jc = new JCommander();

        TaskCommand task = new TaskCommand();
        jc.addCommand("task", task);

        UtilizationCommand utilization = new UtilizationCommand();
        jc.addCommand("utilization", utilization);

        DatacenterCommand datacenter = new DatacenterCommand();
        jc.addCommand("datacenter", datacenter);


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
            printGoogleTaskStates(final_states);


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

            properties.setProperty(HostUsageDataStore.DATABASE_URL_PROP, task.path_output);
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

        }


    }

    private static class Command {
        @Parameter(names = "--before", description = "path of DB before time of checkpoint")
        String path_before= "";

        @Parameter(names = "--after", description = "path of DB after time of checkpoint")
        String path_after = "";

        @Parameter(names = "--time", description = "time of checkpoint")
        String time = "";

        @Parameter(names = "--output", description = "path where will be saved the new file")
        String path_output = "";
    }

    private static class TaskCommand extends Command {

    }

    private static class UtilizationCommand  extends Command{

    }

    private static class DatacenterCommand extends Command{

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

            double vmAvailabilty = googleTask.getRuntime() / (googleTask.getFinishTime() - googleTask.getSubmitTime());
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

        System.out.println("========== MEAN VM AVAILABILITY (priority 0) is " + dft.format((totalVm0Availability / count0)) + " =========");
        System.out.println("========== MEAN VM AVAILABILITY (priority 1) is " + dft.format((totalVm1Availability / count1)) + " =========");
        System.out.println("========== MEAN VM AVAILABILITY (priority 2) is " + dft.format((totalVm2Availability / count2)) + " =========");

        System.out.println("========== MEAN VM AVAILABILITY is " + dft.format(((totalVm0Availability + totalVm1Availability + totalVm2Availability) / size)) + " =========");

        System.out.println("Total of Preemptions: " + totalPreemptions);
        System.out.println("Total of Migrations: " + totalMigrations);

    }


}
