package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import org.cloudbus.cloudsim.preemption.PreemptableVm;

/**
 * TODO
 * 
 * @author Giovanni Farias
 *
 */
public interface Preemptable {

	public boolean preempt(PreemptableVm vm);
}
