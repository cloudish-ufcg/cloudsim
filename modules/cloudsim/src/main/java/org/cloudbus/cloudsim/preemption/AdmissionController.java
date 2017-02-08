package org.cloudbus.cloudsim.preemption;

public interface AdmissionController {
	
	public void calculateQuota();
	
	public boolean accept(PreemptableVm vm);

}
