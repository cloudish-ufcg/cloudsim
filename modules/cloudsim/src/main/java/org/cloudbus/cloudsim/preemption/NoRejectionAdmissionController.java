package org.cloudbus.cloudsim.preemption;

import java.util.Map;

public class NoRejectionAdmissionController implements AdmissionController {

	@Override
	public void calculateQuota(Map<Integer, Double> admittedRequests) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean accept(PreemptableVm vm) {
		return true;
	}

}
