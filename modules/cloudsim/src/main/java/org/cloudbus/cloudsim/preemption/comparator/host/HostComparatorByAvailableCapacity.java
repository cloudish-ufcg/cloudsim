package org.cloudbus.cloudsim.preemption.comparator.host;

import java.util.Comparator;

import org.cloudbus.cloudsim.preemption.PreemptiveHost;

/**
 * 
 * @author giovannifs
 *
 */
public class HostComparatorByAvailableCapacity implements Comparator<PreemptiveHost> {

	@Override
	public int compare(PreemptiveHost host1, PreemptiveHost host2) {
		int result = new Double(host1.getAvailableMips()).compareTo(new Double(host2.getAvailableMips()));
		
		if (result == 0) {
			return new Double(host1.getId()).compareTo(new Double(host2.getId()));
		}
		return result;
	}

}
