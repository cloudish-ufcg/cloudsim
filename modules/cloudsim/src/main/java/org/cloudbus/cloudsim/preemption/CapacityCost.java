package org.cloudbus.cloudsim.preemption;

public class CapacityCost {
	
	private double capacity;
	private double cost;
	private PreemptiveHost pHost;
	
	public CapacityCost(double capacity, double cost, PreemptiveHost pHost) {
		setCapacity(capacity);
		setCost(cost);
		setpHost(pHost);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CapacityCost cost1 = (CapacityCost) o;

		if (Double.compare(cost1.getCapacity(), getCapacity()) != 0) return false;
		if (Double.compare(cost1.getCost(), getCost()) != 0) return false;
		return getpHost().equals(cost1.getpHost());
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(getCapacity());
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getCost());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + getpHost().hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "CapacityCost{" +
				"capacity=" + capacity +
				", cost=" + cost +
				", pHost=" + pHost +
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

	public PreemptiveHost getpHost() {
		return pHost;
	}

	public void setpHost(PreemptiveHost pHost) {
		this.pHost = pHost;
	}
}
