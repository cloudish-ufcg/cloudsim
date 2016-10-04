/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.googletrace.datastore.DatacenterDataStore;
import org.cloudbus.cloudsim.googletrace.datastore.UtilizationDataStore;
import org.cloudbus.cloudsim.googletrace.policies.vmallocation.PreemptableVmAllocationPolicy;

/**
 * TODO
 *  
 * @author Giovanni Farias
 * 
 */
public class GoogleDatacenter extends Datacenter {

	private static final int DATACENTER_BASE = 600;

	// datacenter events
	public static final int SCHEDULE_DATACENTER_EVENTS_EVENT = DATACENTER_BASE + 1;
	public static final int STORE_HOST_UTILIZATION_EVENT = DATACENTER_BASE + 2;
	public static final int COLLECT_DATACENTER_INFO_EVENT = DATACENTER_BASE + 3;
	public static final int STORE_DATACENTER_INFO_EVENT = DATACENTER_BASE + 4;
	
	// default interval sizes 
    public static final int DEFAULT_UTILIZATION_STORING_INTERVAL_SIZE = 1440; // onde day in minutes
	private static final int DEFAULT_DATACENTER_INFO_STORING_INTERVAL_SIZE = 1440; // one day in minutes
	private static final int DEFAULT_DATACENTER_COLLECT_INFO_INTERVAL_SIZE = 5; // in minutes

	PreemptableVmAllocationPolicy vmAllocationPolicy;
	
	SimulationTimeUtil simulationTimeUtil = new SimulationTimeUtil();

	private SortedSet<GoogleVm> vmsRunning = new TreeSet<GoogleVm>();
	private SortedSet<GoogleVm> vmsForScheduling = new TreeSet<GoogleVm>();
	private List<DatacenterInfo> datacenterInfo;
	
	// data stores
	private UtilizationDataStore hostUsageDataStore;
	private DatacenterDataStore datacenterDataStore;
	
	private double hostUsageStoringIntervalSize;
	private double datacenterCollectInfoIntervalSize;
	private double datacenterStoringInfoIntervalSize;

	private boolean collectDatacenterInfo = false;
	
	public GoogleDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			PreemptableVmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval, Properties properties) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		
		hostUsageDataStore = new UtilizationDataStore(properties);
		datacenterDataStore = new DatacenterDataStore(properties);
		
        int hostUsageStoringIntervalSize = properties
                .getProperty("utilization_storing_interval_size") == null ? DEFAULT_UTILIZATION_STORING_INTERVAL_SIZE
                : Integer.parseInt(properties.getProperty("utilization_storing_interval_size"));
        
        setHostUsageStoringIntervalSize(hostUsageStoringIntervalSize);
        setDatacenterInfo(new LinkedList<DatacenterInfo>());
        
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
        
	}
	
	@Override
	protected void processOtherEvent(SimEvent ev) {
		switch (ev.getTag()) {
			case SCHEDULE_DATACENTER_EVENTS_EVENT:
				scheduleDatacenterEvents();
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
				
			case CloudSimTags.END_OF_SIMULATION:
				terminateSimulation();
				storeHostUtilization(true);
				collectDatacenterInfo(true);
				storeDatacenterInfo(true);
				break;
	
			// other unknown tags are processed by this method
			default:
				super.processOtherEvent(ev);
				break;
		}
	}
	

	private void scheduleDatacenterEvents() {
		// creating the first utilization store event
		send(getId(),
				SimulationTimeUtil.getTimeInMicro(getHostUsageStoringIntervalSize()),
				GoogleDatacenter.STORE_HOST_UTILIZATION_EVENT);
        
		if (collectDatacenterInfo) {
			// creating the first datacenter store event
			// TODO we need to consider the datacenter storing interval related to utilization 
			send(getId(), SimulationTimeUtil.getTimeInMicro(getDatacenterCollectInfoIntervalSize()), GoogleDatacenter.COLLECT_DATACENTER_INFO_EVENT);
			
			send(getId(), SimulationTimeUtil.getTimeInMicro(getDatacenterStoringInfoIntervalSize()), GoogleDatacenter.STORE_DATACENTER_INFO_EVENT);			
		}	
	}

	private void terminateSimulation() {
		Log.printConcatLine(simulationTimeUtil.clock(),
				": Finishing all VMs (", getVmsRunning().size(),
				" running and ", getVmsForScheduling().size(), " waiting).");

		Set<Integer> brokerIds = new HashSet<Integer>();
		
		// terminating Vms running
		for (GoogleVm vmRunning : getVmsRunning()) {
			GoogleHost host = (GoogleHost) vmRunning.getHost();
			getVmAllocationPolicy().deallocateHostForVm(vmRunning);
		
			double now = simulationTimeUtil.clock();
			
			vmRunning.setRuntime(vmRunning.getActualRuntime(now));
			sendNow(vmRunning.getUserId(), CloudSimTags.VM_DESTROY_ACK, vmRunning);

			host.updateUtilization(simulationTimeUtil.clock());
			
			brokerIds.add(vmRunning.getUserId());
		}
		
		// terminating Vms waiting
		for (GoogleVm vmForScheduling : getVmsForScheduling()) {
			double now = simulationTimeUtil.clock();
			
			vmForScheduling.setRuntime(vmForScheduling.getActualRuntime(now));
			sendNow(vmForScheduling.getUserId(), CloudSimTags.VM_DESTROY_ACK, vmForScheduling);
			
			brokerIds.add(vmForScheduling.getUserId());
		}

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
		int vmsRunning = getVmsRunning().size();
		int vmsForScheduling = getVmsForScheduling().size();
		
		for (GoogleVm vm : getVmsRunning()) {
			if (vm.getPriority() == 0) {
				vmsRunningP0++;
			} else if (vm.getPriority() == 1) {
				vmsRunningP1++;
			} else if (vm.getPriority() == 2) {
				vmsRunningP2++;
			} else {
				System.out.println("#VMs with invalid priority "
						+ vm.getPriority());
			}
		}
		
		int vmsForSchedulingP0 = 0;
		int vmsForSchedulingP1 = 0;
		int vmsForSchedulingP2 = 0;
		
		for (GoogleVm vm : getVmsForScheduling()) {
			if (vm.getPriority() == 0) {
				vmsForSchedulingP0++;
			} else if (vm.getPriority() == 1) {
				vmsForSchedulingP1++;
			} else if (vm.getPriority() == 2) {
				vmsForSchedulingP2++;
			} else {
				System.out.println("#VMs with invalid priority "
						+ vm.getPriority());
			}
		}
		
		getDatacenterInfo().add(
				new DatacenterInfo(simulationTimeUtil.clock(), vmsRunning,
						vmsRunningP0, vmsRunningP1, vmsRunningP2,
						vmsForScheduling, vmsForSchedulingP0,
						vmsForSchedulingP1, vmsForSchedulingP2));
		
		// creating next event if the are more vms to be concluded
		if ((!getVmsRunning().isEmpty() || !getVmsForScheduling().isEmpty()) && !endOfSimulation) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": Scheduling next collect datacenter info event will be in ",
					SimulationTimeUtil.getTimeInMicro(getHostUsageStoringIntervalSize()),
					" microseconds.");
			send(getId(), SimulationTimeUtil.getTimeInMicro(getDatacenterCollectInfoIntervalSize()),
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
					SimulationTimeUtil.getTimeInMicro(getHostUsageStoringIntervalSize()),
					" microseconds.");
			send(getId(), SimulationTimeUtil.getTimeInMicro(getHostUsageStoringIntervalSize()), STORE_DATACENTER_INFO_EVENT);
		}		
	}

	private void storeHostUtilization(boolean endOfSimulation) {
		Log.printConcatLine(simulationTimeUtil.clock(), ": Storing host usage into database.");

		List<UsageEntry> usageEntries = new ArrayList<UsageEntry>();
		
		for (Host host : getHostList()) {
			GoogleHost gHost = (GoogleHost) host;

			usageEntries.addAll(gHost.getUsageEntries());
						
			gHost.resetUtilizationMap();
		}
		
		Log.printConcatLine(simulationTimeUtil.clock(), ":", usageEntries.size()," will be stored into database now.");
		
		hostUsageDataStore.addUsageEntries(usageEntries);
		
		// creating next event if the are more vms to be concluded
		if ((!getVmsRunning().isEmpty() || !getVmsForScheduling().isEmpty()) && !endOfSimulation) {
			Log.printConcatLine(
					simulationTimeUtil.clock(),
					": Scheduling next store host utilization event in be in ",
					SimulationTimeUtil.getTimeInMicro(getHostUsageStoringIntervalSize()),
					" microseconds.");
			send(getId(), SimulationTimeUtil.getTimeInMicro(getHostUsageStoringIntervalSize()), STORE_HOST_UTILIZATION_EVENT);
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
		GoogleVm vm = (GoogleVm) ev.getData();

		allocateHostForVm(ack, vm, null);
	}

	protected void allocateHostForVm(boolean ack, GoogleVm vm, GoogleHost host) {
		
		if (host == null) {			
			host = (GoogleHost) getVmAllocationPolicy().selectHost(vm);	
		}
		
		boolean result = tryingAllocateOnHost(vm, host);

		if (ack) {
			sendingAck(vm, result);
		}
		
		if (result) {
			getVmsRunning().add(vm);
			vm.setStartExec(simulationTimeUtil.clock());
			
			Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
					vm.getId(), " was allocated on host #", host.getId(),
					" successfully.");
			
			//updating host utilization
			host.updateUtilization(simulationTimeUtil.clock());
			
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
	}

	protected void sendFirst(int entityId, double delay, int cloudSimTag, Object data) {
		if (entityId < 0) {
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

		if (entityId < 0) {
			Log.printConcatLine(getName(), ".send(): Error - "
					+ "invalid entity id ", entityId);
			return;
		}

		int srcId = getId();
		if (entityId != srcId) {// only delay messages between different
								// entities
			delay += getNetworkDelay(srcId, entityId);
		}

		scheduleFirst(entityId, delay, cloudSimTag, data);
	}

	private boolean tryingAllocateOnHost(GoogleVm vm, GoogleHost host) {
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
			
			GoogleVm vmToPreempt = (GoogleVm) host.nextVmForPreempting();
			if (vmToPreempt != null && vmToPreempt.getPriority() > vm.getPriority()) {
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

	private void sendingAck(GoogleVm vm, boolean result) {
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
		GoogleVm vm = (GoogleVm) ev.getData();
		
		if (vm.achievedRuntime(simulationTimeUtil.clock())) {
			if (getVmsRunning().remove(vm)) {		
				Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
						vm.getId(), " will be terminated.");
				
				GoogleHost host = (GoogleHost) vm.getHost();
				getVmAllocationPolicy().deallocateHostForVm(vm);
			
				if (ack) {
					sendNow(vm.getUserId(), CloudSimTags.VM_DESTROY_ACK, vm);
				}

				//updating host utilization
				host.updateUtilization(simulationTimeUtil.clock());
							
				if (!getVmsForScheduling().isEmpty()) {
					processBackfilling(host);
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

	/*
	 * TODO we need to review this code. only the available mips is not the correct way to do it
	 */
	private void processBackfilling(Host host) {	
		Log.printConcatLine(simulationTimeUtil.clock(), ": Trying to allocate more VMs on host #", host.getId() + " after a detroying.");
		
		GoogleHost gHost = (GoogleHost) host;
		
		/*
		 * TODO
		 * We need to think in retrying to allocate VMs that were preempted while allocating new VMs.
		 */
		
		// choosing the vms to request now
		for (GoogleVm currentVm : new ArrayList<GoogleVm>(getVmsForScheduling())) {
			
			if (host.isSuitableForVm(currentVm)) {
				Log.printConcatLine(simulationTimeUtil.clock(),
						": Trying to Allocate VM #", currentVm.getId(),
						" now on host #", gHost.getId());
				allocateHostForVm(false, currentVm, gHost);

			}
		}
	}

	public SortedSet<GoogleVm> getVmsRunning() {
		return vmsRunning;
	}

	public SortedSet<GoogleVm> getVmsForScheduling() {
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
}
