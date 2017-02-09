package org.cloudbus.cloudsim.preemption;

import java.util.Map;

public class NoRejectionAdmissionController implements AdmissionController {

	@Override
	public void calculateQuota(Map<Integer, Double> admittedRequests) {

	}

	@Override
	public boolean accept(PreemptableVm vm) {
		return true;
	}

	@Override
	public void release(PreemptableVm vm) {

	}

}
