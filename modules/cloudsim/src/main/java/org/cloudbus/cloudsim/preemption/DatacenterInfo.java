package org.cloudbus.cloudsim.preemption;

public class DatacenterInfo {

	private double time;
	private int vmsRunning;
	int vmsRunningP0;
	int vmsRunningP1;
	int vmsRunningP2;
	int vmsForScheduling;
	int vmsForSchedulingP0;
	int vmsForSchedulingP1;
	int vmsForSchedulingP2;
	double resourcesRunningP0, resourcesRunningP1, resourcesRunningP2;
	double resourcesWaitingP0, resourcesWaitingP1, resourcesWaitingP2;
	
	public DatacenterInfo(double time, int vmsRunning, int vmsRunningP0,
			int vmsRunningP1, int vmsRunningP2, int vmsForScheduling,
			int vmsForSchedulingP0, int vmsForSchedulingP1,
			int vmsForSchedulingP2) {
		this(time, vmsRunning, vmsRunningP0, 0, vmsRunningP1, 0, vmsRunningP2, 0,
				vmsForScheduling, vmsForSchedulingP0, 0, vmsForSchedulingP1, 0, vmsForSchedulingP2, 0);
	}
	
	public DatacenterInfo(double time, int vmsRunning, int vmsRunningP0, double resourcesRunningP0,
			int vmsRunningP1, double resourcesRunningP1, int vmsRunningP2, double resourcesRunningP2, 
			int vmsForScheduling, int vmsForSchedulingP0, double resourcesWaitingP0, int vmsForSchedulingP1,
			double resourcesWaitingP1, int vmsForSchedulingP2, double resourcesWaitingP2) {
		this.time = time;
		this.vmsRunning = vmsRunning;
		this.vmsRunningP0 = vmsRunningP0;
		this.resourcesRunningP0 = resourcesRunningP0;
		this.vmsRunningP1 = vmsRunningP1;
		this.resourcesRunningP1 = resourcesRunningP1;
		this.vmsRunningP2 = vmsRunningP2;
		this.resourcesRunningP2 = resourcesRunningP2;
		this.vmsForScheduling = vmsForScheduling;
		this.vmsForSchedulingP0 = vmsForSchedulingP0;
		this.resourcesWaitingP0 = resourcesWaitingP0;
		this.vmsForSchedulingP1 = vmsForSchedulingP1;
		this.resourcesWaitingP1 = resourcesWaitingP1;
		this.vmsForSchedulingP2 = vmsForSchedulingP2;
		this.resourcesWaitingP2 = resourcesWaitingP2;
	}
	
	public double getTime() {
		return time;
	}

	public int getVmsRunning() {
		return vmsRunning;
	}

	public int getVmsRunningP0() {
		return vmsRunningP0;
	}

	public int getVmsRunningP1() {
		return vmsRunningP1;
	}

	public int getVmsRunningP2() {
		return vmsRunningP2;
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

	public double getResourcesRunningP0() {
		return resourcesRunningP0;
	}

	public double getResourcesRunningP1() {
		return resourcesRunningP1;
	}

	public double getResourcesRunningP2() {
		return resourcesRunningP2;
	}

	public double getResourcesWaitingP0() {
		return resourcesWaitingP0;
	}

	public double getResourcesWaitingP1() {
		return resourcesWaitingP1;
	}

	public double getResourcesWaitingP2() {
		return resourcesWaitingP2;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DatacenterInfo that = (DatacenterInfo) o;

		if (Double.compare(that.time, time) != 0) return false;
		if (vmsRunning != that.vmsRunning) return false;
		if (vmsRunningP0 != that.vmsRunningP0) return false;
		if (resourcesRunningP0 != that.getResourcesRunningP0()) return false;
		if (vmsRunningP1 != that.vmsRunningP1) return false;
		if (resourcesRunningP1 != that.getResourcesRunningP1()) return false;
		if (vmsRunningP2 != that.vmsRunningP2) return false;
		if (resourcesRunningP2 != that.getResourcesRunningP2()) return false;
		if (vmsForScheduling != that.vmsForScheduling) return false;
		if (vmsForSchedulingP0 != that.vmsForSchedulingP0) return false;
		if (resourcesWaitingP0 != that.getResourcesWaitingP0()) return false;
		if (vmsForSchedulingP1 != that.vmsForSchedulingP1) return false;
		if (resourcesWaitingP1 != that.getResourcesWaitingP1()) return false;
		if (resourcesWaitingP2 != that.getResourcesWaitingP2()) return false;
		return vmsForSchedulingP2 == that.vmsForSchedulingP2;
	}
}
