package org.cloudbus.cloudsim.examples;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.cloudbus.cloudsim.preemption.TaskState;
import org.cloudbus.cloudsim.preemption.datastore.TaskDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by jvmafra on 07/11/16.
 */
public class MergeOfResultFiles {

    public static final String DATABASE_URL_PROP = "output_tasks_database_url";

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

            properties.setProperty(DATABASE_URL_PROP, task.path_before);
            TaskDataStore dataStore = new TaskDataStore(properties);
            List<TaskState> tasks_before = dataStore.getTasksFinishedBefore(time);

            properties.setProperty(DATABASE_URL_PROP, task.path_after);
            dataStore = new TaskDataStore(properties);
            List<TaskState> tasks_after = dataStore.getAllTasks();

            List<TaskState> listOfAllTasks = new ArrayList<>();
            listOfAllTasks.addAll(tasks_before);
            listOfAllTasks.addAll(tasks_after);

            properties.setProperty(DATABASE_URL_PROP, task.path_output);
            dataStore = new TaskDataStore(properties);
            dataStore.addTaskList(listOfAllTasks);


        // TODO
        } else if (parsedCommand.equals("utilization")){

        } else if (parsedCommand.equals("datacenter")){

        }


    }

    private static class TaskCommand {
        @Parameter(names = "--before", description = "path of DB before time of checkpoint")
        String path_before= "";

        @Parameter(names = "--after", description = "path of DB after time of checkpoint")
        String path_after = "";

        @Parameter(names = "--time", description = "time of checkpoint")
        String time = "";

        @Parameter(names = "--output", description = "path where will be saved the new file")
        String path_output = "";
    }

    private static class UtilizationCommand {
        @Parameter(names = "--before", description = "path of DB before time of checkpoint")
        String path_before= "";

        @Parameter(names = "--after", description = "path of DB after time of checkpoint")
        String path_after = "";

        @Parameter(names = "--time", description = "time of checkpoint")
        String time = "";

        @Parameter(names = "--output", description = "path where will be saved the new file")
        String path_output = "";
    }

    private static class DatacenterCommand {
        @Parameter(names = "--before", description = "path of DB before time of checkpoint")
        String path_before= "";

        @Parameter(names = "--after", description = "path of DB after time of checkpoint")
        String path_after = "";

        @Parameter(names = "--time", description = "time of checkpoint")
        String time = "";

        @Parameter(names = "--output", description = "path where will be saved the new file")
        String path_output = "";
    }


}
