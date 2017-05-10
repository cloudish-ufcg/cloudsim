package org.cloudbus.cloudsim.preemption.policies.preemption;

import java.util.List;
import java.util.SortedSet;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.CapacityCost;
import org.cloudbus.cloudsim.preemption.PreemptableVm;

public class CapacityCostBasedPreemptionPolicy extends PreemptionPolicy {

	private double lastUpdate;
	private List<CapacityCost> capacityCosts;
	
	public CapacityCostBasedPreemptionPolicy() {
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public boolean isSuitableFor(PreemptableVm vm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Vm nextVmForPreempting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<PreemptableVm> sortVms(SortedSet<PreemptableVm> sortedVms) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CapacityCost> getCapacityCosts(double minCPUReq, double maxCPUReq) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void allocating(PreemptableVm vm) {
		// TODO Auto-generated method stub
		super.allocating(vm);
	}
	
	@Override
	public void deallocating(PreemptableVm vm) {
		// TODO Auto-generated method stub
		super.deallocating(vm);
	}

	@Override
	public double getAvailableMipsByVm(PreemptableVm vm) {
		throw new RuntimeException("Method is not supported by this class.");
	}
	
	@Override
	public double getAvailableMipsByPriorityAndAvailability(int priority) {
		throw new RuntimeException("Method is not supported by this class.");
	}
}
