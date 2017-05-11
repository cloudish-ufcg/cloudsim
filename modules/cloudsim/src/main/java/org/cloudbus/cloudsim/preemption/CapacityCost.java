package org.cloudbus.cloudsim.preemption;

public class CapacityCost {
	
	private double capacity;
	private double cost;
	private PreemptiveHost host;
	
	public CapacityCost(double capacity, double cost, PreemptiveHost host) {
		setCapacity(capacity);
		setCost(cost);
		setHost(host);
	}

	public CapacityCost(PreemptableVm vm, PreemptiveHost host) {
		setCapacity(vm.getMips());
		setCost(0d);
		setHost(host);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CapacityCost cost1 = (CapacityCost) o;

		if (Double.compare(cost1.getCapacity(), getCapacity()) != 0) return false;
		if (Double.compare(cost1.getCost(), getCost()) != 0) return false;
		return getHost().equals(cost1.getHost());
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(getCapacity());
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getCost());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + getHost().hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "CapacityCost{" +
				"capacity=" + capacity +
				", cost=" + cost +
				", host=" + host +
				'}';
	}

	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public PreemptiveHost getHost() {
		return host;
	}

	public void setHost(PreemptiveHost host) {
		this.host = host;
	}
}
