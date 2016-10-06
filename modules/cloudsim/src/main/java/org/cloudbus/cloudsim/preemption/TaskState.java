package org.cloudbus.cloudsim.preemption;

public class TaskState {

	private int taskId;
	private double cpuReq;
	private double runtime;
	private double submitTime;
	private double finishTime;
	private int priority;

	public TaskState(int taskId, double cpuReq, double submitTime,
			double finishTime, double runtime, int priority) {
		this.taskId = taskId;
		this.cpuReq = cpuReq;
		this.submitTime = submitTime;
		this.finishTime = finishTime;
		this.runtime = runtime;
		this.priority = priority;
	}

	public int getTaskId() {
		return taskId;
	}

	public double getCpuReq() {
		return cpuReq;
	}

	public double getRuntime() {
		return runtime;
	}

	public double getSubmitTime() {
		return submitTime;
	}

	public double getFinishTime() {
		return finishTime;
	}

	public int getPriority() {
		return priority;
	}
}
