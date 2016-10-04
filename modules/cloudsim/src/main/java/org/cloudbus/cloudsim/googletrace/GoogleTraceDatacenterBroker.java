/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.googletrace.datastore.GoogleInputTraceDataStore;
import org.cloudbus.cloudsim.googletrace.datastore.GoogleOutputTaskDataStore;

/**
 * DatacentreBroker represents a broker acting on behalf of a user.
 *
 * @author Giovanni Farias
 */
public class GoogleTraceDatacenterBroker extends SimEntity {

    private static final int BROKER_BASE = 500;
    // broker events
    protected static final int LOAD_NEXT_TASKS_EVENT = BROKER_BASE + 1;
    protected static final int STORE_FINISHED_TASKS_EVENT = BROKER_BASE + 2;

    private static final int DEFAULT_TASK_INTERVAL_SIZE = 5;

    protected TreeSet<GoogleTask> createdTasks;
    protected List<GoogleTaskState> finishedTasks;
    
    Properties properties;

    /**
     * The id's list of available datacenters.
     */
    protected List<Integer> datacenterIdsList;

    /**
     * The datacenter characteristics map where each key
     * is a datacenter id and each value is its characteristics..
     */
    protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

    private int intervalIndex;
    private int taskLoadingIntervalSize; // in minutes
    private int taskStoringIntervalSize; // in minutes

    private GoogleInputTraceDataStore inputTraceDataStore;

    private GoogleOutputTaskDataStore taskDataStore;

    public GoogleTraceDatacenterBroker(String name, Properties properties) throws Exception {
        super(name);

        setCreatedTasks(new TreeSet<GoogleTask>());
        setFinishedTasks(new ArrayList<GoogleTaskState>());

        int taskLoadingIntervalSize = properties
                .getProperty("loading_interval_size") == null ? DEFAULT_TASK_INTERVAL_SIZE
                : Integer.parseInt(properties.getProperty("loading_interval_size"));

        int taskStoringIntervalSize = properties
                .getProperty("storing_interval_size") == null ? DEFAULT_TASK_INTERVAL_SIZE
                : Integer.parseInt(properties.getProperty("storing_interval_size"));

        setIntervalIndex(0);
        setTaskLoadingIntervalSize(taskLoadingIntervalSize);
        setTaskStoringIntervalSize(taskStoringIntervalSize);

        setDatacenterIdsList(new LinkedList<Integer>());
        setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

        inputTraceDataStore = new GoogleInputTraceDataStore(properties);
        taskDataStore = new GoogleOutputTaskDataStore(properties);
        
        this.properties = properties;
        
    }

    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // Resource characteristics request
            case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
                processResourceCharacteristicsRequest(ev);
                break;
            // Resource characteristics answer
            case CloudSimTags.RESOURCE_CHARACTERISTICS:
                processResourceCharacteristics(ev);
                break;
            // if the simulation finishes
            case CloudSimTags.END_OF_SIMULATION:
            	storeFinishedTasks(true);
                break;
            case CloudSimTags.VM_DESTROY_ACK:
                processVmDestroyAck(ev);
                break;
            // load next google tasks from trace file
            case LOAD_NEXT_TASKS_EVENT:
                loadNextGoogleTasks();
                break;
            // store already finished tasks
            case STORE_FINISHED_TASKS_EVENT:
                storeFinishedTasks(false);
                break;
            // other unknown tags are processed by this method
            default:
                processOtherEvent(ev);
                break;
        }
    }

    private void storeFinishedTasks(boolean endOfSimulation) {    	
        List<GoogleTaskState> toStore = new ArrayList<GoogleTaskState>(getFinishedTasks());
        if (toStore != null && !toStore.isEmpty()
                && taskDataStore.addTaskList(toStore)) {
            Log.printConcatLine(CloudSim.clock(), ": ", toStore.size(),
                    " VMs stored now.");
            getFinishedTasks().removeAll(toStore);
        }
        
        // abruptally terminating the simulation
        if (endOfSimulation) {
        	CloudSim.abruptallyTerminate();
        	return;
        }
        
        // creating next event if the are more events to be treated
        if (inputTraceDataStore.hasMoreEvents(getIntervalIndex(),
                SimulationTimeUtil.getTimeInMicro(getTaskStoringIntervalSize()))) {
            send(getId(), SimulationTimeUtil.getTimeInMicro(getTaskStoringIntervalSize()), STORE_FINISHED_TASKS_EVENT);
        }
    }

    private void processVmDestroyAck(SimEvent ev) {
        GoogleVm vm = (GoogleVm) ev.getData();

        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #",
                vm.getId(), " is being destroyed now.");
        
        double now = CloudSim.clock();

        GoogleTaskState taskState = new GoogleTaskState(vm.getId(), vm.getMips(), vm.getSubmitTime(), now,
                vm.getRuntime(), vm.getPriority());
        finishedTasks.add(taskState);
    }

    /**
     * Process the return of a request for the characteristics of a Datacenter.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    protected void processResourceCharacteristics(SimEvent ev) {
        DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
        getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

        if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
            loadNextGoogleTasks();
            
            // creating the first task store event
            send(getId(), SimulationTimeUtil.getTimeInMicro(getTaskStoringIntervalSize()), STORE_FINISHED_TASKS_EVENT);
            
            // scheduling datacenter events
            sendNow(getDatacenterId(), GoogleDatacenter.SCHEDULE_DATACENTER_EVENTS_EVENT);
            
            // creating end of simulation event
            if (properties.getProperty("end_of_simulation_time") != null) {
            	long endOfSimulationTime = Long.parseLong(properties.getProperty("end_of_simulation_time"));
            	
            	send(getDatacenterId(), endOfSimulationTime, CloudSimTags.END_OF_SIMULATION);
            }
        }
    }

    private void loadNextGoogleTasks() {
        Log.printLine("Loading next google tasks. Interval index " + getIntervalIndex());

        List<GoogleTask> nextGoogleTasks = inputTraceDataStore
                .getGoogleTaskInterval(getIntervalIndex(), SimulationTimeUtil.getTimeInMicro(getTaskLoadingIntervalSize()));

        // if nextGoogleTasks == null there are not more tasks
        if (nextGoogleTasks != null) {
            getCreatedTasks().addAll(nextGoogleTasks);
            submitTasks();
            setIntervalIndex(++intervalIndex);

            send(getId(), SimulationTimeUtil.getTimeInMicro(getTaskLoadingIntervalSize()), LOAD_NEXT_TASKS_EVENT);
        }
    }

    /**
     * Process a request for the characteristics of a PowerDatacenter.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    protected void processResourceCharacteristicsRequest(SimEvent ev) {
        setDatacenterIdsList(CloudSim.getCloudResourceList());
        setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

        Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Cloud Resource List received with ",
                getDatacenterIdsList().size(), " resource(s)");

        for (Integer datacenterId : getDatacenterIdsList()) {
            sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
        }
    }

    /**
     * Process non-default received events that aren't processed by
     * the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
     * This method should be overridden by subclasses in other to process
     * new defined events.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     * @todo to ensure the method will be overridden, it should be defined
     * as abstract in a super class from where new brokers have to be extended.
     */
    protected void processOtherEvent(SimEvent ev) {
        if (ev == null) {
            Log.printConcatLine(getName(), ".processOtherEvent(): ", "Error - an event is null.");
            return;
        }

        Log.printConcatLine(getName(), ".processOtherEvent(): Error - event unknown by this DatacenterBroker.");
    }

    /**
     * Submit cloudlets to the created VMs.
     *
     * @pre $none
     * @post $none
     * @see #submitCloudletList(java.util.List)
     */
    protected void submitTasks() {
        Log.printConcatLine("Scheduling the creation of VMs.");

        while (!getCreatedTasks().isEmpty()) {
            scheduleRequestsForVm(getCreatedTasks().first());
        }
    }

    private void scheduleRequestsForVm(GoogleTask task) {

        GoogleVm vm = new GoogleVm(task.getId(), getId(), task.getCpuReq(),
                task.getMemReq(), task.getSubmitTime(), task.getPriority(), task.getRuntime());

        int datacenterId = getDatacenterId();

        Log.printLine(CloudSim.clock() + ": " + getName()
                + ": Trying to Create VM #" + vm.getId() + " in "
                + datacenterId);

        getCreatedTasks().remove(task);

        double delay = task.getSubmitTime() - CloudSim.clock();
        send(datacenterId, delay, CloudSimTags.VM_CREATE, vm);
    }

    /*
     * TODO This method is returning always the first element because our
     * simulation has only one datacenter. In the future, if we would like to
     * simulate more than one datacenter, we can implement a policy to choose
     * the best datacenter
     */
    private int getDatacenterId() {

        return getDatacenterIdsList().get(0);
    }

    @Override
    public void shutdownEntity() {
        Log.printConcatLine(getName(), " is shutting down...");
    }

    @Override
    public void startEntity() {
        Log.printConcatLine(getName(), " is starting...");
        schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
    }

    public SortedSet<GoogleTask> getCreatedTasks() {
        return createdTasks;
    }

    protected void setCreatedTasks(TreeSet<GoogleTask> createdTasks) {
        this.createdTasks = createdTasks;
    }

    /**
     * Gets the datacenter ids list.
     *
     * @return the datacenter ids list
     */
    protected List<Integer> getDatacenterIdsList() {
        return datacenterIdsList;
    }

    /**
     * Sets the datacenter ids list.
     *
     * @param datacenterIdsList the new datacenter ids list
     */
    protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
        this.datacenterIdsList = datacenterIdsList;
    }

    /**
     * Gets the datacenter characteristics list.
     *
     * @return the datacenter characteristics list
     */
    protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
        return datacenterCharacteristicsList;
    }

    /**
     * Sets the datacenter characteristics list.
     *
     * @param datacenterCharacteristicsList the datacenter characteristics list
     */
    protected void setDatacenterCharacteristicsList(
            Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
        this.datacenterCharacteristicsList = datacenterCharacteristicsList;
    }

    public List<GoogleTaskState> getStoredTasks() {
        return taskDataStore.getAllTasks();
    }

    public void submitTasks(List<GoogleTask> taskList) {
        getCreatedTasks().addAll(taskList);
    }

    public int getIntervalIndex() {
        return intervalIndex;
    }

    private void setIntervalIndex(int intervalIndex) {
        this.intervalIndex = intervalIndex;
    }

    public int getTaskLoadingIntervalSize() {
        return taskLoadingIntervalSize;
    }

    private void setTaskLoadingIntervalSize(int intervalSize) {
        this.taskLoadingIntervalSize = intervalSize;
    }

    public int getTaskStoringIntervalSize() {
        return taskStoringIntervalSize;
    }

    private void setTaskStoringIntervalSize(int storingIntervalSize) {
        this.taskStoringIntervalSize = storingIntervalSize;
    }

    public List<GoogleTaskState> getFinishedTasks() {
        return finishedTasks;
    }

    public void setFinishedTasks(List<GoogleTaskState> finishedTasks) {
        this.finishedTasks = finishedTasks;
    }
}
