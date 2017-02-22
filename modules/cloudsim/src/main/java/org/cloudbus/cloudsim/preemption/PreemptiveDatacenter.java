/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.preemption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.Predicate;
import org.cloudbus.cloudsim.preemption.datastore.DatacenterUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.HostUsageDataStore;
import org.cloudbus.cloudsim.preemption.datastore.PreemptableVmDataStore;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.vmallocation.PreemptableVmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.util.PriorityAndAvailabilityBasedVmComparator;
import org.cloudbus.cloudsim.preemption.util.PriorityAndTTVBasedPreemptableVmComparator;

/**
 * TODO
 *  
 * @author Giovanni Farias
 * 
 */
public class PreemptiveDatacenter extends Datacenter {

	private static final int INVALID_INTERVAL_SIZE = -1;

	private static final int DATACENTER_BASE = 600;

	// datacenter events
	public static final int SCHEDULE_DATACENTER_EVENTS_EVENT = DATACENTER_BASE + 1;
	public static final int STORE_HOST_UTILIZATION_EVENT = DATACENTER_BASE + 2;
	public static final int COLLECT_DATACENTER_INFO_EVENT = DATACENTER_BASE + 3;
	public static final int STORE_DATACENTER_INFO_EVENT = DATACENTER_BASE + 4;
	public static final int MAKE_DATACENTER_CHECKPOINT_EVENT = DATACENTER_BASE + 5;
	public static final int INITIALIZE_FROM_CHECKPOINT_EVENT = DATACENTER_BASE + 6;
	public static final int TRY_TO_ALLOCATE_WAITING_QUEUE_EVENT = DATACENTER_BASE + 7;
	public static final int UPDATE_QUOTAS_EVENT = DATACENTER_BASE + 8;
	
	// default interval sizes 
	// TODO review this default values, maybe it should be in microseconds
    public static final int DEFAULT_UTILIZATION_STORING_INTERVAL_SIZE = 1440; // onde day in minutes
	private static final int DEFAULT_DATACENTER_INFO_STORING_INTERVAL_SIZE = 1440; // one day in minutes
	private static final int DEFAULT_DATACENTER_COLLECT_INFO_INTERVAL_SIZE = 5; // in minutes
	private static final int DEFAULT_CHECKPOINT_INTERVAL_SIZE = 1440; // one day in minutes
	private static final int DEFAULT_UPDATE_QUOTA_INTERVAL_SIZE = 5;


	PreemptableVmAllocationPolicy vmAllocationPolicy;
	
	SimulationTimeUtil simulationTimeUtil = new SimulationTimeUtil();

	private SortedSet<PreemptableVm> vmsRunning = new TreeSet<PreemptableVm>();
	private SortedSet<PreemptableVm> vmsForScheduling = new TreeSet<PreemptableVm>();
	private List<DatacenterInfo> datacenterInfo;
	
	// data stores
	private HostUsageDataStore hostUsageDataStore;
	private DatacenterUsageDataStore datacenterDataStore;
	
	private double hostUsageStoringIntervalSize;
	private double datacenterCollectInfoIntervalSize;
	private double datacenterStoringInfoIntervalSize;
	private double checkpointIntervalSize;
	private double allocateWaitingQueueIntervalSize = INVALID_INTERVAL_SIZE; 
	private double updateQuotaIntervalSize;

	private boolean collectDatacenterInfo = false;
	private boolean makeCheckpoint = false;
	private Properties properties;
	private static double lastProcessTime;
	private AdmissionController admController;
	private Map<Integer, Double> admittedRequests;
	
	public PreemptiveDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			PreemptableVmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval, Properties properties) throws Exception {
		this(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, properties, new NoRejectionAdmissionController());
	}
	
	public PreemptiveDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			PreemptableVmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval, Properties properties, AdmissionController admController) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		
		this.hostUsageDataStore = new HostUsageDataStore(properties);
		this.datacenterDataStore = new DatacenterUsageDataStore(properties);
		this.properties = properties;
		this.admController = admController;
		this.admittedRequests = new HashMap<Integer, Double>();
	
		if (properties.getProperty("number_of_priorities") != null) {
			int numberOfPriorities = Integer.parseInt(properties.getProperty("number_of_priorities"));
			if (numberOfPriorities <= 0) {
				throw new IllegalArgumentException("The number of priorities must be a positive integer.");
			}
			
			for (int priority = 0; priority < numberOfPriorities; priority++) {
				admittedRequests.put(priority, 0d);
			}
		}

        double hostUsageStoringIntervalSize = properties
                .getProperty("utilization_storing_interval_size") == null ? DEFAULT_UTILIZATION_STORING_INTERVAL_SIZE
                : Integer.parseInt(properties.getProperty("utilization_storing_interval_size"));
        
        
        setHostUsageStoringIntervalSize(hostUsageStoringIntervalSize);
        setDatacenterInfo(new LinkedList<DatacenterInfo>());
        
        // There is no last event processed, then lastProcessTime must be invalid one
        lastProcessTime = INVALID_INTERVAL_SIZE;
       
		if (properties.getProperty("make_checkpoint") != null
				&& properties.getProperty("make_checkpoint")
						.equals("yes")) {
			makeCheckpoint = true;			
			double checkpointIntervalSize = properties
					.getProperty("checkpoint_interval_size") == null ? DEFAULT_CHECKPOINT_INTERVAL_SIZE
							: Double.parseDouble(properties.getProperty("checkpoint_interval_size"));
			
			setCheckpointIntervalSize(checkpointIntervalSize);
		}
		
		if (properties.getProperty("allocate_waiting_queue_interval_size") != null) {
			double allocateWaitingQueueIntervalSize = Double
					.parseDouble(properties.getProperty("allocate_waiting_queue_interval_size"));
			setAllocateWaitingQueueIntervalSize(allocateWaitingQueueIntervalSize);
		}
        
		if (properties.getProperty("collect_datacenter_summary_info") != null
				&& properties.getProperty("collect_datacenter_summary_info")
						.equals("yes")) {
			collectDatacenterInfo = true;
			
			double datacenterStoringInfoIntervalSize = properties
					.getProperty("datacenter_storing_interval_size") == null ? DEFAULT_DATACENTER_INFO_STORING_INTERVAL_SIZE
							: Double.parseDouble(properties.getProperty("datacenter_storing_interval_size"));

			setDatacenterStoringInfoIntervalSize(datacenterStoringInfoIntervalSize);
						
			double datacenterCollectInfoIntervalSize = properties
					.getProperty("datacenter_collect_info_interval_size") == null ? DEFAULT_DATACENTER_COLLECT_INFO_INTERVAL_SIZE
							: Double.parseDouble(properties.getProperty("datacenter_collect_info_interval_size"));

			setDatacenterCollectInfoIntervalSize(datacenterCollectInfoIntervalSize);
		}
		
		if (properties.getProperty("update_quota_interval_size") != null) {
			double updateQuotaIntervalSize = properties
					.getProperty("update_quota_interval_size") == null ? DEFAULT_UPDATE_QUOTA_INTERVAL_SIZE
							: Double.parseDouble(properties.getProperty("update_quota_interval_size"));

			setUpdateQuotaIntervalSize(updateQuotaIntervalSize);
		}
        
	}
	
	@Override
	protected void processOtherEvent(SimEvent ev) {

		if (ev == null) {
			Log.printConcatLine(getName(), ".processOtherEvent(): Error - an event is null.");
			return;
		}

		switch (ev.getTag()) {

			case INITIALIZE_FROM_CHECKPOINT_EVENT:
				PreemptableVmDataStore vmDataStore = new PreemptableVmDataStore(properties);
				initializeFromCheckpoint(vmDataStore);
				break;

			case SCHEDULE_DATACENTER_EVENTS_EVENT:
				scheduleDatacenterEvents();
				break;
				
			case TRY_TO_ALLOCATE_WAITING_QUEUE_EVENT:
				tryToAllocateWaitingQueue();
				break;
		
			case STORE_HOST_UTILIZATION_EVENT:
				storeHostUtilization(false);
				break;
				
			case COLLECT_DATACENTER_INFO_EVENT:
				collectDatacenterInfo(false);
				break;
				
			case STORE_DATACENTER_INFO_EVENT:
				storeDatacenterInfo(false);
				break;
				
			case UPDATE_QUOTAS_EVENT:
				updateQuotas();
				break;
				
			case MAKE_DATACENTER_CHECKPOINT_EVENT:
				makeCheckpoint();
				break;
				
			case CloudSimTags.END_OF_SIMULATION:
				collectDatacenterInfo(true);
				terminateSimulation();
				storeHostUtilization(true);
				storeDatacenterInfo(true);
				break;
	
			// other unknown tags are processed by this method
			default:
				super.processOtherEvent(ev);
				break;
		}
	}

    private void updateQuotas() {
		getAdmController().calculateQuota(getAdmittedRequests());
		
		// scheduling next update quota event
		Log.printConcatLine(simulationTimeUtil.clock(),
				": Scheduling next update quota event will be in ",
				getUpdateQuotaIntervalSize(), " time units.");

		Integer endOfSimulation = Integer.parseInt(properties.getProperty("end_of_simulation_time"));

		if (simulationTimeUtil.clock() + getUpdateQuotaIntervalSize() < endOfSimulation){
			// this method allow the quota event to be ran before destroy events (serial = -1)
			sendPriorityEvent(getId(), getUpdateQuotaIntervalSize(),
					PreemptiveDatacenter.UPDATE_QUOTAS_EVENT, null, -1);
		}

		
	}

	private void tryToAllocateWaitingQueue() {
    	if (firstProcessNow()) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": First processing at this time. Pre processing and updating last process time to ", simulationTimeUtil.clock());
			lastProcessTime = simulationTimeUtil.clock();
			getVmAllocationPolicy().preProcess();
			
			allocatingWaitingQueue();
		} else {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": The waiting queue already was tried to be alocated at this time.");
		}	
    	
    	send(getId(), getAllocateWaitingQueueIntervalSize(), PreemptiveDatacenter.TRY_TO_ALLOCATE_WAITING_QUEUE_EVENT);
	}

	protected void initializeFromCheckpoint(PreemptableVmDataStore vmDataStore) {
        Log.printLine(simulationTimeUtil.clock() + ": Initializing datacenter from checkpoint.");
        System.out.println(simulationTimeUtil.clock() + ": Initializing datacenter from checkpoint.");
		List<PreemptableVm> runningVms = vmDataStore.getAllRunningVms();
		List<PreemptableVm> waitingVms = vmDataStore.getAllWaitingVms();
		Map<Integer, PreemptiveHost> mapOfHosts = generateMapOfHosts();

		if (waitingVms != null && runningVms != null){
			Log.printLine(CloudSim.clock() + ": There are " + runningVms.size()
					+ " runningVms and " + waitingVms.size()
					+ " waitingVms on checkpoint.");

			getVmsForScheduling().addAll(waitingVms);
			
			for (PreemptableVm vm : waitingVms) {
				getAdmittedRequests().put(vm.getPriority(), admittedRequests.get(vm.getPriority()) + vm.getMips());
			}
			
			for (PreemptableVm vm: runningVms){
				vm.setStartExec(simulationTimeUtil.clock());
				PreemptiveHost host = mapOfHosts.get(vm.getLastHostId());
				if (!getVmAllocationPolicy().allocateHostForVm(vm, host)) {
					throw new SimulationException(
							"Error allocating VM to a specific host "
									+ host.getId()
									+ " while initializing from a checkpoint file.");
				}

				vm.allocatingToHost(host.getId());

				double remainingTime = vm.getRuntime() - vm.getActualRuntime(simulationTimeUtil.clock());
				Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
						vm.getId(), " will be destroyed in ", remainingTime,
						" microseconds.");
				System.out.println(simulationTimeUtil.clock() + ": VM #" +
						vm.getId() + " will be destroyed in " + remainingTime +
						" microseconds.");
				sendFirst(getId(), remainingTime, CloudSimTags.VM_DESTROY_ACK, vm);

				host.updateUsage(simulationTimeUtil.clock());
				
				getAdmittedRequests().put(vm.getPriority(), admittedRequests.get(vm.getPriority()) + vm.getMips());

				getVmsRunning().add(vm);

			}
		}
		sendNow(getId(), SCHEDULE_DATACENTER_EVENTS_EVENT);

	}

	private Map<Integer, PreemptiveHost> generateMapOfHosts(){
		List<PreemptiveHost> hostList = getHostList();
		Map<Integer, PreemptiveHost> mapOfHosts = new HashMap<>();

		for (PreemptiveHost host: hostList){
			mapOfHosts.put(host.getId(), host);
		}

		return mapOfHosts;

	}


	private void makeCheckpoint() {
		Log.printConcatLine(simulationTimeUtil.clock(), ": Building datacenter checkpoint.");
		
		PreemptableVmDataStore vmDataStore = new PreemptableVmDataStore(properties, simulationTimeUtil.clock());
		
		if (!vmDataStore.addWaitingVms(getVmsForScheduling())) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": There was an error while making checkpoint of vms for scheduling.");
		}
		
		if (!vmDataStore.addRunningVms(getVmsRunning())) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": There was an error while making checkpoint of vms running.");	
		}

		//scheduling next checkpoint event
		if (!getVmsRunning().isEmpty() || !getVmsForScheduling().isEmpty()) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": Scheduling next checkpoint event will be in ",
					getCheckpointIntervalSize(), " time units.");
			send(getId(), getCheckpointIntervalSize(),
					PreemptiveDatacenter.MAKE_DATACENTER_CHECKPOINT_EVENT);
		}
	}

	private void scheduleDatacenterEvents() {
		Log.printConcatLine(simulationTimeUtil.clock(), ": Scheduling the first datacenter events.");
		
		// creating the first utilization store event
		send(getId(), getHostUsageStoringIntervalSize(),
				PreemptiveDatacenter.STORE_HOST_UTILIZATION_EVENT);
		
		// creating the first update quota event
		sendPriorityEvent(getId(), getUpdateQuotaIntervalSize(),
				PreemptiveDatacenter.UPDATE_QUOTAS_EVENT, null, -1);
		
		// creating the first datacenter store event
		if (collectDatacenterInfo) {
			send(getId(), getDatacenterCollectInfoIntervalSize(), PreemptiveDatacenter.COLLECT_DATACENTER_INFO_EVENT);
			
			send(getId(), getDatacenterStoringInfoIntervalSize(), PreemptiveDatacenter.STORE_DATACENTER_INFO_EVENT);
		}
		
		// creating the first checkpoint event
		if (makeCheckpoint) {
			send(getId(), getCheckpointIntervalSize(), PreemptiveDatacenter.MAKE_DATACENTER_CHECKPOINT_EVENT);
		}
		
		// periodically trying to allocate waiting queue
		if (getAllocateWaitingQueueIntervalSize() != INVALID_INTERVAL_SIZE) {
			send(getId(), getAllocateWaitingQueueIntervalSize(), PreemptiveDatacenter.TRY_TO_ALLOCATE_WAITING_QUEUE_EVENT);
		}		
	}

	private void terminateSimulation() {
		Log.printConcatLine(simulationTimeUtil.clock(),
				": Finishing all VMs (", getVmsRunning().size(),
				" running and ", getVmsForScheduling().size(), " waiting).");

		Set<Integer> brokerIds = new HashSet<Integer>();
		
		// terminating Vms running
		for (PreemptableVm vmRunning : getVmsRunning()) {
			PreemptiveHost host = (PreemptiveHost) vmRunning.getHost();
			getVmAllocationPolicy().deallocateHostForVm(vmRunning);
		
			double now = simulationTimeUtil.clock();
			
			vmRunning.setRuntime(vmRunning.getActualRuntime(now));
			sendNow(vmRunning.getUserId(), CloudSimTags.VM_DESTROY_ACK, vmRunning);

			host.updateUsage(simulationTimeUtil.clock());
			
			brokerIds.add(vmRunning.getUserId());
		}

		getVmsRunning().clear();

		// terminating Vms waiting
		for (PreemptableVm vmForScheduling : getVmsForScheduling()) {
			double now = simulationTimeUtil.clock();
			
			vmForScheduling.setRuntime(vmForScheduling.getActualRuntime(now));
			sendNow(vmForScheduling.getUserId(), CloudSimTags.VM_DESTROY_ACK, vmForScheduling);
			
			brokerIds.add(vmForScheduling.getUserId());
		}

		getVmsForScheduling().clear();

		// sending end of simulation event to broker
		Log.printConcatLine(simulationTimeUtil.clock(),
				": Sending end of simulation to ", brokerIds.size(),
				" brokers.");
		
		for (Integer brokerId : brokerIds) {
			sendNow(brokerId, CloudSimTags.END_OF_SIMULATION);
		}
	}

	private void collectDatacenterInfo(boolean endOfSimulation) {
		Log.printConcatLine(simulationTimeUtil.clock(), ": Collecting datacenter info.");

		int vmsRunningP0 = 0;
		int vmsRunningP1 = 0;
		int vmsRunningP2 = 0;
		double resourcesRunningP0 = 0;
		double resourcesRunningP1 = 0;
		double resourcesRunningP2 = 0;
		int vmsRunning = getVmsRunning().size();
		int vmsForScheduling = getVmsForScheduling().size();
		
		for (PreemptableVm vm : getVmsRunning()) {
			if (vm.getPriority() == 0) {
				vmsRunningP0++;
				resourcesRunningP0 += vm.getMips();
			} else if (vm.getPriority() == 1) {
				vmsRunningP1++;
				resourcesRunningP1 += vm.getMips();
			} else if (vm.getPriority() == 2) {
				vmsRunningP2++;
				resourcesRunningP2 += vm.getMips();
			} else {
				System.out.println("#VMs with invalid priority "
						+ vm.getPriority());
			}
		}
		
		int vmsForSchedulingP0 = 0;
		int vmsForSchedulingP1 = 0;
		int vmsForSchedulingP2 = 0;
		double resourcesWaitingP0 = 0;
		double resourcesWaitingP1 = 0;
		double resourcesWaitingP2 = 0;
		
		for (PreemptableVm vm : getVmsForScheduling()) {
			if (vm.getPriority() == 0) {
				vmsForSchedulingP0++;
				resourcesWaitingP0 += vm.getMips();
			} else if (vm.getPriority() == 1) {
				vmsForSchedulingP1++;
				resourcesWaitingP1 += vm.getMips();
			} else if (vm.getPriority() == 2) {
				vmsForSchedulingP2++;
				resourcesWaitingP2 += vm.getMips();
			} else {
				System.out.println("#VMs with invalid priority "
						+ vm.getPriority());
			}
		}

		getDatacenterInfo().add(
				new DatacenterInfo(simulationTimeUtil.clock(), vmsRunning,
						vmsRunningP0, resourcesRunningP0, vmsRunningP1,
						resourcesRunningP1, vmsRunningP2, resourcesRunningP2,
						vmsForScheduling, vmsForSchedulingP0,
						resourcesWaitingP0, vmsForSchedulingP1,
						resourcesWaitingP1, vmsForSchedulingP2,
						resourcesWaitingP2));

		// creating next event if the are more vms to be concluded
		if ((!getVmsRunning().isEmpty() || !getVmsForScheduling().isEmpty()) && !endOfSimulation) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": Scheduling next collect datacenter info event will be in ",
					getHostUsageStoringIntervalSize(),
					" time units.");
			send(getId(), getDatacenterCollectInfoIntervalSize(),
					COLLECT_DATACENTER_INFO_EVENT);
		}
	}
	
	private void storeDatacenterInfo(boolean endOfSimulation) {
		if (datacenterDataStore.addDatacenterInfo(getDatacenterInfo())) {
			getDatacenterInfo().clear();
		}

		// creating next event if the are more vms to be concluded
		if ((!getVmsRunning().isEmpty() || !getVmsForScheduling().isEmpty()) && !endOfSimulation) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": Scheduling next store datacenter info event in be in ",
					getHostUsageStoringIntervalSize(), " time units.");
			send(getId(), getHostUsageStoringIntervalSize(),
					STORE_DATACENTER_INFO_EVENT);
		}		
	}

	private void storeHostUtilization(boolean endOfSimulation) {
		Log.printConcatLine(simulationTimeUtil.clock(), ": Storing host usage into database.");

		List<UsageEntry> usageEntries = new LinkedList<UsageEntry>();
		
		for (Host host : getHostList()) {

			PreemptiveHost gHost = (PreemptiveHost) host;
			usageEntries.addAll(gHost.getUsageEntries(this.simulationTimeUtil.clock()));
		}
		
		Log.printConcatLine(simulationTimeUtil.clock(), ":", usageEntries.size()," will be stored into database now.");
		
		hostUsageDataStore.addUsageEntries(usageEntries);
		
		// creating next event if the are more vms to be concluded
		if ((!getVmsRunning().isEmpty() || !getVmsForScheduling().isEmpty()) && !endOfSimulation) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": Scheduling next store host utilization event in be in ",
					getHostUsageStoringIntervalSize(), " time units.");
			send(getId(), getHostUsageStoringIntervalSize(),
					STORE_HOST_UTILIZATION_EVENT);
		}
	}
	
	public List<UsageEntry> getHostUtilizationEntries() {
		return hostUsageDataStore.getAllUsageEntries();
	}

	/**
	 * Process the event for an User/Broker who wants to create a VM in this Datacenter. This
	 * Datacenter will then send the status back to the User/Broker. It is important to note that
	 * the creation of VM does not have cost here. 
	 * 
	 * @param ev information about the event just happened
	 * @param ack indicates if the event's sender expects to receive 
         * an acknowledge message when the event finishes to be processed
         * 
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	protected void processVmCreate(SimEvent ev, boolean ack) {
		PreemptableVm vm = (PreemptableVm) ev.getData();

		if (admController.accept(vm, getAdmittedRequests())) {

			// updating the admitted requests for specific priority
			int priority = vm.getPriority();
			double currentAdmitted = admittedRequests.get(vm.getPriority());
			getAdmittedRequests().put(priority, currentAdmitted + vm.getMips());

			allocateHostForVm(ack, vm, null, false);
		} else {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": VM #", vm.getId(), " - ", vm.getPriority(), " was not accepted by Admission Controler.");
			System.out.println(simulationTimeUtil.clock() + 
					": VM #" + vm.getId() + " - " + vm.getPriority() + " was not accepted by Admission Controler.");
		}		
	}

	protected boolean allocateHostForVm(boolean ack, PreemptableVm vm, PreemptiveHost host, boolean isBackfilling) {
		Log.printConcatLine(simulationTimeUtil.clock(),
				": Trying to allocate host for VM #", vm.getId());
		
		if (firstProcessNow()) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": Time passed out. Pre processing and updating last process time to ", simulationTimeUtil.clock());
			lastProcessTime = simulationTimeUtil.clock();
			getVmAllocationPolicy().preProcess();			
		}
		
		if (host == null) {			
			host = (PreemptiveHost) getVmAllocationPolicy().selectHost(vm);	
		}
		
		boolean result = tryingAllocateOnHost(vm, host);

		if (ack) {
			sendingAck(vm, result);
		}
		
		if (result) {
			getVmsRunning().add(vm);
			
			//TODO we can move these calls to into vmCreate method in Host
			vm.setStartExec(simulationTimeUtil.clock());
			vm.allocatingToHost(host.getId());
			
			if (isBackfilling) {
				vm.setNumberOfBackfillingChoice(vm.getNumberOfBackfillingChoice() + 1);				
			}
			
			Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
					vm.getId(), " was allocated on host #", host.getId(),
					" successfully.");
			
			//updating host utilization
			host.updateUsage(simulationTimeUtil.clock());
			
			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}
			
			getVmsForScheduling().remove(vm);
			
			double remainingTime = vm.getRuntime() - vm.getActualRuntime(simulationTimeUtil.clock());
			Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
					vm.getId(), " will be destroyed in ", remainingTime,
					" microseconds.");
			sendFirst(getId(), remainingTime, CloudSimTags.VM_DESTROY_ACK, vm);			
			
		}
		return result;
	}

	private boolean firstProcessNow() {
		return simulationTimeUtil.clock() > lastProcessTime;
	}

	protected void sendFirst(int entityId, double delay, int cloudSimTag, Object data) {
		if (entityId < 0) {
			Log.printConcatLine(getName(), ".send(): Error - "
					+ "invalid entity id ", entityId);
			return;
		}

		// if delay is -ve, then it doesn't make sense. So resets to 0.0
		if (delay < 0) {
			delay = 0;
		}

		if (Double.isInfinite(delay)) {
			throw new IllegalArgumentException(
					"The specified delay is infinite value");
		}

		int srcId = getId();
		if (entityId != srcId) {// only delay messages between different
								// entities
			delay += getNetworkDelay(srcId, entityId);
		}

		scheduleFirst(entityId, delay, cloudSimTag, data);
	}
	
	protected void sendPriorityEvent(int entityId, double delay, int cloudSimTag, Object data, long serial) {
		if (entityId < 0) {
			Log.printConcatLine(getName(), ".send(): Error - "
					+ "invalid entity id ", entityId);
			return;
		}

		// if delay is -ve, then it doesn't make sense. So resets to 0.0
		if (delay < 0) {
			delay = 0;
		}

		if (Double.isInfinite(delay)) {
			throw new IllegalArgumentException(
					"The specified delay is infinite value");
		}

		int srcId = getId();
		if (entityId != srcId) {// only delay messages between different
								// entities
			delay += getNetworkDelay(srcId, entityId);
		}
		
		if (!CloudSim.running()) {
			return;
		}
		
		CloudSim.send(getId(), entityId, delay, cloudSimTag, data, serial);
	}
	
	private boolean tryingAllocateOnHost(PreemptableVm vm, PreemptiveHost host) {
		if (host == null) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": There is not resource to allocate VM #" + vm.getId()
							+ " now, it will be tryed in the future.");
			if (!getVmsForScheduling().contains(vm)) {
				getVmsForScheduling().add(vm);
			}
			return false;
		}
		
		// trying to allocate
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);

		if (!result) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": There is not resource to allocate VM #" + vm.getId()
							+ " right now.");
			
			PreemptableVm vmToPreempt = (PreemptableVm) host.nextVmForPreempting();
			if (vmToPreempt != null && vmToPreempt.getPriority() >= vm.getPriority()) {
				Log.printConcatLine(simulationTimeUtil.clock(),
						": Preempting VM #" + vmToPreempt.getId()
								+ " (priority " + vmToPreempt.getPriority()
								+ ") to allocate VM #" + vm.getId()
								+ " (priority " + vm.getPriority() + ")");
				getVmAllocationPolicy().preempt(vmToPreempt);
				getVmsRunning().remove(vmToPreempt);
				getVmsForScheduling().add(vmToPreempt);
				return tryingAllocateOnHost(vm, host);
			} else if (!getVmsForScheduling().contains(vm)) {
				Log.printConcatLine(simulationTimeUtil.clock(),
						": There are not VMs to preempt. VM #" + vm.getId()
								+ " will be scheduled in the future.");
				getVmsForScheduling().add(vm);
			}
		}
		return result;
	}

	private void sendingAck(PreemptableVm vm, boolean result) {
		int[] data = new int[3];
		data[0] = getId();
		data[1] = vm.getId();

		if (result) {
			data[2] = CloudSimTags.TRUE;
		} else {
			data[2] = CloudSimTags.FALSE;
		}
		send(vm.getUserId(), 0, CloudSimTags.VM_CREATE_ACK, data);
	}
	
	@Override
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		PreemptableVm vm = (PreemptableVm) ev.getData();
		
		if (vm.achievedRuntime(simulationTimeUtil.clock())) {
			if (getVmsRunning().remove(vm)) {		
				Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
						vm.getId(), " will be terminated.");
								
				PreemptiveHost host = (PreemptiveHost) vm.getHost();
				getVmAllocationPolicy().deallocateHostForVm(vm);
			
				if (ack) {
					sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, vm);
				}

				//updating host utilization				
				host.updateUsage(simulationTimeUtil.clock());
				
				// updating the admitted resources
				getAdmittedRequests().put(
						vm.getPriority(),
						getAdmittedRequests().get(vm.getPriority())	- vm.getMips());

				if (!getVmsForScheduling().isEmpty() && !nextEventIsDestroy()) {
					allocatingWaitingQueue();
				}
			} else {
				Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
						vm.getId(), " was terminated previously.");
			}
		} else {
			Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
					vm.getId(), " doesn't achieve the runtime yet.");
		}
	}

	private boolean nextEventIsDestroy() {
		if (numEventsWaiting(new Predicate() {
			
			@Override
			public boolean match(SimEvent event) {
				return ((event.getTag() == CloudSimTags.VM_DESTROY_ACK) || (event
						.getTag() == CloudSimTags.VM_DESTROY));
			}
		}) > 0) {
			Log.printConcatLine(simulationTimeUtil.clock(), ": Is destroy the next event ?", true);
			return true;
		}
		Log.printConcatLine(simulationTimeUtil.clock(), ": Is destroy the next event ?", false);
		return false;
	}

	private void allocatingWaitingQueue() {	
		Log.printConcatLine(simulationTimeUtil.clock(), ": Trying to allocate the VMs in waiting queue.");
		
		boolean isBackfilling = false;
		
		ArrayList<PreemptableVm> waitingQueue = new ArrayList<PreemptableVm>(getVmsForScheduling());
		
		// TODO Think better about how to configure it
		String preemptionPolicyClass = properties
				.getProperty("preemption_policy_class");
		if (preemptionPolicyClass != null
				&& "org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy"
						.equals(preemptionPolicyClass)) {

			Log.printConcatLine(simulationTimeUtil.clock(),
					": Sorting the waiting queue based on priority + current availability.");

			Collections.sort(waitingQueue,
					new PriorityAndAvailabilityBasedVmComparator(
							simulationTimeUtil));
		} else if (preemptionPolicyClass != null
				&& "org.cloudbus.cloudsim.preemption.policies.preemption.TTVBasedPreemptionPolicy"
						.equals(preemptionPolicyClass)) {

			Log.printConcatLine(simulationTimeUtil.clock(),
					": Sorting the waiting queue based on priority + TTV.");

			Map<Integer, Double> sloTargets = VmAvailabilityBasedPreemptionPolicy
					.getSLOAvailabilityTargets(properties);
			
			Collections.sort(waitingQueue, new PriorityAndTTVBasedPreemptableVmComparator(sloTargets, simulationTimeUtil));
		}
		for (PreemptableVm currentVm : waitingQueue) {
				if (!allocateHostForVm(false, currentVm, null, isBackfilling)) {
					isBackfilling = true;
				}
		}
	}

	public SortedSet<PreemptableVm> getVmsRunning() {
		return vmsRunning;
	}

	public SortedSet<PreemptableVm> getVmsForScheduling() {
		return vmsForScheduling;
	}
	
	@Override
	public PreemptableVmAllocationPolicy getVmAllocationPolicy() {
		return vmAllocationPolicy;
	}
	
	@Override
	protected void setVmAllocationPolicy(VmAllocationPolicy vmAllocationPolicy) {
		this.vmAllocationPolicy = (PreemptableVmAllocationPolicy) vmAllocationPolicy;
	}

	protected void setSimulationTimeUtil(SimulationTimeUtil simulationTimeUtil) {
		this.simulationTimeUtil = simulationTimeUtil;
	}

	protected double getHostUsageStoringIntervalSize() {
		return hostUsageStoringIntervalSize;
	}

	protected void setHostUsageStoringIntervalSize(double storingIntervalSize) {
		this.hostUsageStoringIntervalSize = storingIntervalSize;
	}

	public List<DatacenterInfo> getDatacenterInfo() {
		return datacenterInfo;
	}

	public void setDatacenterInfo(List<DatacenterInfo> datacenterInfo) {
		this.datacenterInfo = datacenterInfo;
	}

	public List<DatacenterInfo> getAllDatacenterInfo() {
		return datacenterDataStore.getAllDatacenterInfo();
	}

	public double getDatacenterCollectInfoIntervalSize() {
		return datacenterCollectInfoIntervalSize;
	}

	public void setDatacenterCollectInfoIntervalSize(
			double datacenterCollectInfoIntervalSize) {
		this.datacenterCollectInfoIntervalSize = datacenterCollectInfoIntervalSize;
	}

	public double getDatacenterStoringInfoIntervalSize() {
		return datacenterStoringInfoIntervalSize;
	}

	public void setDatacenterStoringInfoIntervalSize(
			double datacenterStoringInfoIntervalSize) {
		this.datacenterStoringInfoIntervalSize = datacenterStoringInfoIntervalSize;
	}

	public double getCheckpointIntervalSize() {
		return checkpointIntervalSize;
	}

	public void setCheckpointIntervalSize(double checkpointIntervalSize) {
		this.checkpointIntervalSize = checkpointIntervalSize;
	}
	
	private double getUpdateQuotaIntervalSize() {
		return updateQuotaIntervalSize;
	}
	
	public void setUpdateQuotaIntervalSize(double updateQuotaIntervalSize) {
		this.updateQuotaIntervalSize = updateQuotaIntervalSize;
	}

	public void setHostUsageDataStore(HostUsageDataStore hostUsageDataStore) {
		this.hostUsageDataStore = hostUsageDataStore;
	}

	public double getAllocateWaitingQueueIntervalSize() {
		return allocateWaitingQueueIntervalSize;
	}

	public void setAllocateWaitingQueueIntervalSize(
			double allocateWaitingQueueIntervalSize) {
		this.allocateWaitingQueueIntervalSize = allocateWaitingQueueIntervalSize;
	}

	public Map<Integer, Double> getAdmittedRequests() {
		return admittedRequests;
	}

	public AdmissionController getAdmController() {
		return admController;
	}

}
