package org.cloudbus.cloudsim.preemption;

public class UsageEntry {

	private int hostId; 
	private double time;
	private double usage;
	private	int numberOfVms;
	private int priority;
	private double availableMips;
	
	public UsageEntry(int hostId, double time, double usage,
			int numberOfVms, int priority, double availableMips) {		
		this.hostId = hostId;
		this.time = time;
		this.usage = usage;
		this.numberOfVms = numberOfVms;
		this.priority = priority;
		this.availableMips = availableMips;
	}

	public int getHostId() {
		return hostId;
	}

	public double getTime() {
		return time;
	}

	public double getUsage() {
		return usage;
	}

	public int getNumberOfVms() {
		return numberOfVms;
	}

	public int getPriority() {
		return priority;
	}

	public double getAvailableMips() {
		return availableMips;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		UsageEntry that = (UsageEntry) o;

		if (hostId != that.hostId)
			return false;
		if (Double.compare(that.time, time) != 0)
			return false;
		if (Double.compare(that.usage, usage) != 0)
			return false;
		if (Double.compare(that.availableMips, availableMips) != 0)
			return false;
		if (Integer.compare(that.priority, priority) != 0)
			return false;
		if (Integer.compare(that.numberOfVms, numberOfVms) != 0)
			return false;
		return true;
	}

//	@Override
//	public int hashCode() {
//		int result;
//		long temp;
//		result = hostId;
//		temp = Double.doubleToLongBits(time);
//		result = 31 * result + (int) (temp ^ (temp >>> 32));
//		temp = Double.doubleToLongBits(usage);
//		result = 31 * result + (int) (temp ^ (temp >>> 32));
//		temp = Double.doubleToLongBits(availableMips);
//		result = 31 * result + (int) (temp ^ (temp >>> 32));
//		result = 31 * result + priorityToUsage.hashCode();
//		result = 31 * result + priorityToNumberOfVms.hashCode();
//		return result;
//	}
}
