package org.cloudbus.cloudsim.preemption;

public class TaskState {

	private int taskId;
	private double cpuReq;
	private double runtime;
	private double submitTime;
	private double finishTime;
	private double firstTimeAllocated;
	private int priority;
	private int preemptions;
	private int backfillingChoices;
	private int migrations;

	public TaskState(int taskId, double cpuReq, double submitTime,
			double finishTime, double runtime, int priority, int preemptions, int backfillingChoices, int migrations, double firstTimeAllocated) {
		this.taskId = taskId;
		this.cpuReq = cpuReq;
		this.submitTime = submitTime;
		this.finishTime = finishTime;
		this.runtime = runtime;
		this.priority = priority;
		this.preemptions = preemptions;
		this.backfillingChoices = backfillingChoices;
		this.migrations = migrations;
		this.firstTimeAllocated = firstTimeAllocated;
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

	@Override
	public boolean equals(Object o) {

		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TaskState that = (TaskState) o;
		if(this.taskId != that.getTaskId()) return false;
		if(this.cpuReq != that.getCpuReq()) return false;
		if(this.submitTime != that.getSubmitTime()) return false;
		if (this.finishTime != that.getFinishTime()) return false;
		if (this.runtime != that.getRuntime()) return false;
		if(this.priority != that.getPriority()) return  false;
		if(this.preemptions != that.getNumberOfPreemptions()) return false;
		if(this.backfillingChoices != that.getNumberOfBackfillingChoices()) return false;
		if(this.migrations != that.getNumberOfMigrations()) return false;
		if(this.firstTimeAllocated != that.getFirstTimeAllocated()) return false;

		return getTaskId() == that.getTaskId();
	}

	public int getNumberOfPreemptions() {
		return preemptions;
	}

	public int getNumberOfBackfillingChoices() {
		return backfillingChoices;
	}

	public int getNumberOfMigrations() {
		return migrations;
	}

	public double getFirstTimeAllocated() {
		return firstTimeAllocated;
	}

	public void setFirstTimeAllocated(double firstTimeAllocated) {
		this.firstTimeAllocated = firstTimeAllocated;
	}

	@Override
	public String toString() {
		return "TaskState{" +
				"taskId=" + taskId +
				", cpuReq=" + cpuReq +
				", runtime=" + runtime +
				", submitTime=" + submitTime +
				", finishTime=" + finishTime +
				", firstTimeAllocated=" + firstTimeAllocated +
				", priority=" + priority +
				", preemptions=" + preemptions +
				", backfillingChoices=" + backfillingChoices +
				", migrations=" + migrations +
				'}';
	}
}
