package org.cloudbus.cloudsim.googletrace;

public class HostUtilizationEntry {
	
	private int hostId;
	private double time;
	private double utilization;
	
	public HostUtilizationEntry(int hostId, double time, double utilization) {
		setHostId(hostId);
		setTime(time);
		setUtilization(utilization);
	}

	public int getHostId() {
		return hostId;
	}

	protected void setHostId(int hostId) {
		this.hostId = hostId;
	}

	public double getTime() {
		return time;
	}

	protected void setTime(double time) {
		this.time = time;
	}

	public double getUtilization() {
		return utilization;
	}

	protected void setUtilization(double utilization) {
		this.utilization = utilization;
	}
}
