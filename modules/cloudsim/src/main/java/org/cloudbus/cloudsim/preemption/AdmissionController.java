package org.cloudbus.cloudsim.preemption;

import java.util.Map;

public interface AdmissionController {
	
	public void calculateQuota(Map<Integer, Double> admittedRequests);
	
	public boolean accept(PreemptableVm vm, Map<Integer, Double> admittedRequests);

}
