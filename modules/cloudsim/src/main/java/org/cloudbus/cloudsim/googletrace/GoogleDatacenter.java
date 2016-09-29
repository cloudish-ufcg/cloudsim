/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
	
	public static final int STORE_HOST_UTILIZATION_EVENT = DATACENTER_BASE + 1;
	public static final int STORE_DATACENTER_DATA_EVENT = DATACENTER_BASE + 2;
	
    public static final int DEFAULT_UTILIZATION_STORING_INTERVAL_SIZE = 5;

	PreemptableVmAllocationPolicy vmAllocationPolicy;
	
	SimulationTimeUtil simulationTimeUtil = new SimulationTimeUtil();

	private SortedSet<GoogleVm> vmsRunning = new TreeSet<GoogleVm>();
	private SortedSet<GoogleVm> vmsForScheduling = new TreeSet<GoogleVm>();
	private UtilizationDataStore hostUsageDataStore;
	
	private double storingIntervalSize;
	
	//TODO make it configurable
	private double datacenterStoringIntervalSize = 5;
	
	public GoogleDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			PreemptableVmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval, Properties properties) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		
		hostUsageDataStore = new UtilizationDataStore(properties);
		
        int storingIntervalSize = properties
                .getProperty("utilization_storing_interval_size") == null ? DEFAULT_UTILIZATION_STORING_INTERVAL_SIZE
                : Integer.parseInt(properties.getProperty("utilization_storing_interval_size"));
        
        setStoringIntervalSize(storingIntervalSize);
	}
	
	@Override
	protected void processOtherEvent(SimEvent ev) {
		switch (ev.getTag()) {
			case STORE_HOST_UTILIZATION_EVENT:
				storeHostUtilization();
				break;
				
			case STORE_DATACENTER_DATA_EVENT:
				storeDatacenterData();
				break;
	
			// other unknown tags are processed by this method
			default:
				super.processOtherEvent(ev);
				break;
		}
	}
	
	
	private void storeDatacenterData() {
		Log.printConcatLine(simulationTimeUtil.clock(), ": Storing datacenter data into database.");

		for (Host host : getHostList()) {
			GoogleHost gHost = (GoogleHost) host;
			System.out.println("time=" + simulationTimeUtil.clock() + "hostId:" + gHost.getId() + ", totalMips=" + gHost.getTotalMips() + ", totalUsage=" + gHost.getTotalUsage());
			System.out.println("Priority0: usage=" + gHost.getUsageByPriority(0)+ ", available=" + gHost.getAvailableMipsByPriority(0) + ", running=" + gHost.getPriorityToVms().get(0).size());
			System.out.println("Priority1: usage=" + gHost.getUsageByPriority(1)+ ", available=" + gHost.getAvailableMipsByPriority(1) + ", running=" + gHost.getPriorityToVms().get(1).size());
			System.out.println("Priority2: usage=" + gHost.getUsageByPriority(2)+ ", available=" + gHost.getAvailableMipsByPriority(2) + ", running=" + gHost.getPriorityToVms().get(2).size());
		}
		
		System.out.println("#VMs Running: " + getVmsRunning().size());
		System.out.println("#VMs For Scheduling: " + getVmsForScheduling().size());
		for (int i = 0; i < 3; i++) {
			int numberOfVms = 0;
			for (GoogleVm vm : getVmsForScheduling()) {
				if (vm.getPriority() == i) {
					numberOfVms++;
				} else if (vm.getPriority() > i) {
					break;
				}
			}
			System.out.println("#VMs For Scheduling (priority"+i+"): " + numberOfVms);	
		}
		
		// creating next event if the are more vms to be concluded
		if (!getVmsRunning().isEmpty() || !getVmsForScheduling().isEmpty()) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": Scheduling next store datacenter data event will be in ",
					SimulationTimeUtil.getTimeInMicro(getStoringIntervalSize()),
					" microseconds.");
			send(getId(), SimulationTimeUtil.getTimeInMicro(datacenterStoringIntervalSize),
					STORE_DATACENTER_DATA_EVENT);
		}
	}

	private void storeHostUtilization() {
		Log.printConcatLine(simulationTimeUtil.clock(), ": Storing host utilization  into database.");
		
		List<HostUtilizationEntry> utilizationEntries = new ArrayList<HostUtilizationEntry>();
		
		for (Host host : getHostList()) {
			int hostId = host.getId();
			GoogleHost gHost = (GoogleHost) host;
			Map<Double, Double> utilizationMap = gHost.getUtilizationMap();
			
			for (Double time : utilizationMap.keySet()) {				
				utilizationEntries.add(new HostUtilizationEntry(hostId, time, utilizationMap.get(time)));
			}
			
			gHost.resetUtilizationMap();
		}
		
		Log.printConcatLine(simulationTimeUtil.clock(), ":", utilizationEntries.size()," will be stored into database now.");
		
		hostUsageDataStore.addUtilizationEntries(utilizationEntries);

		// creating next event if the are more vms to be concluded
		if (!getVmsRunning().isEmpty() || !getVmsForScheduling().isEmpty()) {
			Log.printConcatLine(
					simulationTimeUtil.clock(),
					": Scheduling next store host utilization event in be in ",
					SimulationTimeUtil.getTimeInMicro(getStoringIntervalSize()),
					" microseconds.");
			send(getId(), SimulationTimeUtil.getTimeInMicro(getStoringIntervalSize()), STORE_HOST_UTILIZATION_EVENT);
		}
	}
	
	public List<HostUtilizationEntry> getHostUtilizationEntries() {
		return hostUsageDataStore.getAllUtilizationEntries();
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
			
			// We don't need to update the vm processing because there aren't cloudlets running in the vm
//			vm.updateVmProcessing(simulationTimeUtil.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
//					.getAllocatedMipsForVm(vm));
			
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
					tryingToAllocateVms(host);
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
	private void tryingToAllocateVms(Host host) {	
		Log.printConcatLine(simulationTimeUtil.clock(), ": Trying to allocate more VMs on host #", host.getId() + " after a detroying.");
		
		GoogleHost gHost = (GoogleHost) host;
		
//		Map<Integer, Double> priorityToAvailableMips = new HashMap<Integer, Double>();		
//		for (int i = 0; i < gHost.getNumberOfPriorities(); i++) {
//			priorityToAvailableMips.put(i, gHost.getAvailableMipsByPriority(i));
//		}
//		
//		System.out.println("priorityToAvailableMips=" + priorityToAvailableMips);
		
		
//		double availableMips = host.getAvailableMips();
//		double mipsForRequestingNow = 0;
//		List<GoogleVm> vmsToRequestNow = new ArrayList<GoogleVm>();
		
		
		/*
		 * TODO
		 * We need to think in retrying to allocate VMs that were preempted while allocating new VMs.
		 */
		
		// choosing the vms to request now
		for (GoogleVm currentVm : new ArrayList<GoogleVm>(getVmsForScheduling())) {
//			System.out.println("vmId="+ currentVm.getId());
//			System.out.println("vmMips="+ currentVm.getMips());
//			System.out.println("isSuitableFor? " + host.isSuitableForVm(currentVm));
//			System.out.println("priorityToAvailableMips=" + gHost.getAvailableMipsByPriority(priority));
//			System.out.println("currentVm.getMips() <= priorityToAvailableMips.get(currentVm.getPriority())? " + (currentVm.getMips() <= priorityToAvailableMips.get(currentVm.getPriority())));
			
//			if (host.isSuitableForVm(currentVm) && currentVm.getMips() <= priorityToAvailableMips.get(currentVm.getPriority())) {
			if (host.isSuitableForVm(currentVm)) {
				//TODO remove it
//				if (mipsForRequestingNow + currentVm.getMips() <= availableMips) {
//					mipsForRequestingNow += currentVm.getMips();
//					vmsToRequestNow.add(currentVm);
//				}
				
//				vmsToRequestNow.add(currentVm);
		
				Log.printConcatLine(simulationTimeUtil.clock(), ": Trying to Allocate VM #", currentVm.getId(), " now on host #", gHost.getId());
				allocateHostForVm(false, currentVm, gHost);
				
				// updating available mips for the same and smaller priorities
//				for (int i = currentVm.getPriority(); i < gHost.getNumberOfPriorities(); i++) {
//					double currenAvailable = priorityToAvailableMips.get(i);
//					priorityToAvailableMips.put(i, DecimalUtil.format(currenAvailable - currentVm.getMips(), GoogleHost.DECIMAL_ACCURACY));		
//				}
				
			}
//			System.out.println("priorityToAvailableMips=" + priorityToAvailableMips);
		}
		
//		Log.printConcatLine(simulationTimeUtil.clock(), ": Trying to Allocate "
//				+ vmsToRequestNow.size() + " VMs now.");
//		
//		// trying to allocate Host
//		for (GoogleVm requestedVm : vmsToRequestNow) {
//			allocateHostForVm(false, requestedVm, gHost);				
//		}
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

	protected double getStoringIntervalSize() {
		return storingIntervalSize;
	}

	protected void setStoringIntervalSize(double storingIntervalSize) {
		this.storingIntervalSize = storingIntervalSize;
	}
	
	
}
