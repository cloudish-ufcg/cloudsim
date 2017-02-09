package org.cloudbus.cloudsim.preemption;

import java.util.Map;

public interface AdmissionController {
	
	public void calculateQuota(Map<Integer, Double> admittedRequests);
	
	public boolean accept(PreemptableVm vm);

	public void release(PreemptableVm vm);

}
