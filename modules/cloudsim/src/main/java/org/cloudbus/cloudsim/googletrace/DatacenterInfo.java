package org.cloudbus.cloudsim.googletrace;

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
}
