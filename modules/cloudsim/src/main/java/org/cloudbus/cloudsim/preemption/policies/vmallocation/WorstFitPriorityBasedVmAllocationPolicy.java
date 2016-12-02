package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.List;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;

public class WorstFitPriorityBasedVmAllocationPolicy extends
		PreemptableVmAllocationPolicy {

	public WorstFitPriorityBasedVmAllocationPolicy(List<PreemptiveHost> hosts) {
		super(hosts, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean preempt(PreemptableVm vm) {
		// TODO Auto-generated method stub
		return false;
	}

}
