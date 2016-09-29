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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof HostUtilizationEntry)) return false;

		HostUtilizationEntry that = (HostUtilizationEntry) o;

		if (getHostId() != that.getHostId()) return false;
		if (Double.compare(that.getTime(), getTime()) != 0) return false;
		return Double.compare(that.getUtilization(), getUtilization()) == 0;

	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = getHostId();
		temp = Double.doubleToLongBits(getTime());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getUtilization());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}
