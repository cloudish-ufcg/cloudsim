package org.cloudbus.cloudsim.preemption;

public class DatacenterInfo {

	private double time;
	private int vmsRunning;
	int usageByPriority0;
	int usageByPriority1;
	int usageByPriority2;
	int vmsForScheduling;
	int vmsForSchedulingP0;
	int vmsForSchedulingP1;
	int vmsForSchedulingP2;
	
	public DatacenterInfo(double time, int vmsRunning, int usageByPriority0,
			int usageByPriority1, int usageByPriority2, int vmsForScheduling,
			int vmsForSchedulingP0, int vmsForSchedulingP1,
			int vmsForSchedulingP2) {
		this.time = time;
		this.vmsRunning = vmsRunning;
		this.usageByPriority0 = usageByPriority0;
		this.usageByPriority1 = usageByPriority1;
		this.usageByPriority2 = usageByPriority2;
		this.vmsForScheduling = vmsForScheduling;
		this.vmsForSchedulingP0 = vmsForSchedulingP0;
		this.vmsForSchedulingP1 = vmsForSchedulingP1;
		this.vmsForSchedulingP2 = vmsForSchedulingP2;
	}

	public double getTime() {
		return time;
	}

	public int getVmsRunning() {
		return vmsRunning;
	}

	public int getUsageByPriority0() {
		return usageByPriority0;
	}

	public int getUsageByPriority1() {
		return usageByPriority1;
	}

	public int getUsageByPriority2() {
		return usageByPriority2;
	}

	public int getVmsForScheduling() {
		return vmsForScheduling;
	}

	public int getVmsForSchedulingP0() {
		return vmsForSchedulingP0;
	}

	public int getVmsForSchedulingP1() {
		return vmsForSchedulingP1;
	}

	public int getVmsForSchedulingP2() {
		return vmsForSchedulingP2;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DatacenterInfo that = (DatacenterInfo) o;

		if (Double.compare(that.time, time) != 0) return false;
		if (vmsRunning != that.vmsRunning) return false;
		if (usageByPriority0 != that.usageByPriority0) return false;
		if (usageByPriority1 != that.usageByPriority1) return false;
		if (usageByPriority2 != that.usageByPriority2) return false;
		if (vmsForScheduling != that.vmsForScheduling) return false;
		if (vmsForSchedulingP0 != that.vmsForSchedulingP0) return false;
		if (vmsForSchedulingP1 != that.vmsForSchedulingP1) return false;
		return vmsForSchedulingP2 == that.vmsForSchedulingP2;

	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(time);
		result = (int) (temp ^ (temp >>> 32));
		result = 31 * result + vmsRunning;
		result = 31 * result + usageByPriority0;
		result = 31 * result + usageByPriority1;
		result = 31 * result + usageByPriority2;
		result = 31 * result + vmsForScheduling;
		result = 31 * result + vmsForSchedulingP0;
		result = 31 * result + vmsForSchedulingP1;
		result = 31 * result + vmsForSchedulingP2;
		return result;
	}
}
