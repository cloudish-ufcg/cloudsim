package org.cloudbus.cloudsim.googletrace;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.cloudbus.cloudsim.Vm;

public class UsageEntry {

	private int hostId;
	private double time;
	private double totalUsage;
	private double availableMips;
	private Map<Integer, Double> priorityToUsage;
	private Map<Integer, Integer> priorityToNumberOfVms;
	
	public UsageEntry(int hostId, double time, Map<Integer, Double> priorityToInUseMips,
			Map<Integer, SortedSet<Vm>> priorityToVms, double totalUsage, double availableMips) {
		
		this.hostId = hostId;
		this.time = time;
		this.totalUsage = totalUsage;
		this.availableMips = availableMips;
		this.priorityToUsage =  new HashMap<Integer, Double>(priorityToInUseMips);
		this.priorityToNumberOfVms = new HashMap<Integer, Integer>();
		
		for (Integer priority : priorityToVms.keySet()) {
			priorityToNumberOfVms.put(priority, priorityToVms.get(priority).size());
		}
	}

	public UsageEntry(int hostId, double time, double p0Usage, double p1Usage,
			double p2Usage, int p0Vms, int p1Vms, int p2Vms, double availableMips) {
		
		this.hostId = hostId;
		this.time = time;
		this.totalUsage = p0Usage + p1Usage + p2Usage;
		this.availableMips = availableMips;
		this.priorityToUsage =  new HashMap<Integer, Double>();
		priorityToUsage.put(0, p0Usage);
		priorityToUsage.put(1, p1Usage);
		priorityToUsage.put(2, p2Usage);
		
		this.priorityToNumberOfVms = new HashMap<Integer, Integer>();
		priorityToNumberOfVms.put(0, p0Vms);
		priorityToNumberOfVms.put(1, p1Vms);
		priorityToNumberOfVms.put(2, p2Vms);
	}

	public int getHostId() {
		return hostId;
	}

	public double getTime() {
		return time;
	}

	public double getTotalUsage() {
		return totalUsage;
	}

	public double getAvailableMips() {
		return availableMips;
	}

	public double getUsageByPriority(int priority) {
		return priorityToUsage.get(priority);
	}

	public int getNumberOfVmsByPriority(int priority) {
		return priorityToNumberOfVms.get(priority);
	}
	
}
