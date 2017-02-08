package org.cloudbus.cloudsim.preemption;

public class GreedNoRejectionAdmissionController implements AdmissionController {

	@Override
	public void calculateQuota() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean accept(PreemptableVm vm) {
		return true;
	}

}
