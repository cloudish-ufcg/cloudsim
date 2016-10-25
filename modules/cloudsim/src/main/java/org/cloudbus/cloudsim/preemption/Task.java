package org.cloudbus.cloudsim.preemption;

public class Task implements Comparable<Task>{

    private int id, priority;
    private double submitTime, runtime, cpuReq, memReq, startTime, finishTime;

    public Task(int id, double submitTime, double runTime,
                      double cpuReq, double memReq, int priority) {
        this.id = id;
        this.submitTime = submitTime;
        this.runtime = runTime;
        this.cpuReq = cpuReq;
        this.memReq = memReq;
        this.priority = priority;
    }

    @Override
    public int compareTo(Task other) {
        if (getSubmitTime() < other.getSubmitTime()) {
            return -1;
        } else if (getSubmitTime() > other.getSubmitTime()) {
            return 1;
        } else if (getPriority() < other.getPriority()) {
            return -1;
        } else if (getPriority() > other.getPriority()) {
            return 1;
        }
        return new Integer(getId()).compareTo(new Integer(other.getId()));
    }


    public int getId() {
        return id;
    }

    public double getSubmitTime() {
        return submitTime;
    }

    public double getRuntime() {
        return runtime;
    }

    public double getCpuReq() {
        return cpuReq;
    }

    public double getMemReq() {
        return memReq;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task that = (Task) o;

        if (id != that.id) return false;
        if (priority != that.priority) return false;
        if (Double.compare(that.submitTime, submitTime) != 0) return false;
        if (Double.compare(that.runtime, runtime) != 0) return false;
        if (Double.compare(that.cpuReq, cpuReq) != 0) return false;
        if (Double.compare(that.memReq, memReq) != 0) return false;
        if (Double.compare(that.startTime, startTime) != 0) return false;
        return Double.compare(that.finishTime, finishTime) == 0;

    }
}
