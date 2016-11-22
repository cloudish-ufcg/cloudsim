package org.cloudbus.cloudsim.preemption;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.DecimalUtil;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class PreemptiveHost extends Host implements Comparable<Host> {
	
	public static final int DECIMAL_ACCURACY = 9;
		
	private Map<Double, UsageInfo> usageMap;
	private PreemptionPolicy preemptionPolicy;

	public PreemptiveHost(int id, List<? extends Pe> peList, VmScheduler vmScheduler, PreemptionPolicy preemptionPolicy) {
		super(id, new RamProvisionerSimple(Integer.MAX_VALUE),
				new BwProvisionerSimple(Integer.MAX_VALUE), Integer.MAX_VALUE,
				peList, vmScheduler);
		
		setUsageMap(new HashMap<Double, UsageInfo>());
		setPreemptionPolicy(preemptionPolicy);
		preemptionPolicy.setTotalMips(((VmSchedulerMipsBased) getVmScheduler())
				.getTotalMips());
	}

	@Override
    public int compareTo(Host other) {
        /*
		 * If this object has bigger amount of available mips it should be
		 * considered before the other one.
		 */
        int result = (-1) * (new Double(getAvailableMips()).compareTo(new Double(other
                .getAvailableMips())));

        if (result == 0)
            return new Integer(getId()).compareTo(new Integer(other.getId()));

        return result;
    }

    @Override
    public int hashCode() {
        return getId();
    }
    
	public Vm nextVmForPreempting() {
		return preemptionPolicy.nextVmForPreempting();
	}

	@Override
	public boolean isSuitableForVm(Vm vm) {
		if (vm == null) {
			return false;
		} else if (getVmScheduler().getAvailableMips() >= vm.getMips()) {
			return true;
		} 

		return preemptionPolicy.isSuitableFor((PreemptableVm) vm);
	}
	
	@Override
	public boolean vmCreate(Vm vm) {
		if (vm == null) {
			return false;
		}

		PreemptableVm preemptableVm = (PreemptableVm) vm;

		Log.printConcatLine(CloudSim.clock(), ": Creating VM#", vm.getId(), "(priority ", preemptableVm.getPriority(),") on host #", getId());
		
		boolean result = super.vmCreate(vm);
		
		if (result) {
			// TODO add a comment here
			preemptableVm.setStartExec(CloudSim.clock());
			preemptionPolicy.allocating(preemptableVm);
			
			double totalUsage = getTotalUsage();
			Log.printConcatLine(CloudSim.clock(), ": Host #", getId(), " currentTotalUsage=", totalUsage, ", currentAvailableMips=", getAvailableMips());

			if ((totalUsage - getTotalMips()) > 0.00001) {
				throw new SimulationException("The total usage (" + totalUsage
						+ ") on host #" + getId()
						+ " was bigger than the total capacity ("
						+ getTotalMips() + ") while creating VM #" + vm.getId()
						+ ".");
			}
		}
		return result;
	}
	
	public double getTotalUsage() {
		double totalUsage = 0;
		for (Integer priority : preemptionPolicy.getPriorityToInUseMips().keySet()) {
			totalUsage += preemptionPolicy.getPriorityToInUseMips().get(priority);
		}
		return DecimalUtil.format(totalUsage, DECIMAL_ACCURACY);
	}
	
	@Override
	public void vmDestroy(Vm vm) {
		super.vmDestroy(vm);

		PreemptableVm preemptableVm = (PreemptableVm) vm;		
		preemptionPolicy.deallocating(preemptableVm);
	}

	public int getNumberOfPriorities() {
		return preemptionPolicy.getNumberOfPriorities();
	}
	
	public double getAvailableMipsByPriority(int priority) {
		return preemptionPolicy.getAvailableMipsByPriority(priority);
	}

	public double getAvailableMipsByVm(PreemptableVm vm) {
		return preemptionPolicy.getAvailableMipsByVm(vm);
	}
	
	@Override
	public double getTotalMips(){
		return ((VmSchedulerMipsBased) getVmScheduler()).getTotalMips();
	}
	
	public List<UsageEntry> getUsageEntries() {
		List<UsageEntry> usageEntries = new LinkedList<UsageEntry>();
		for (UsageInfo usageInfo : getUsageMap().values()) {
			usageEntries.addAll(usageInfo.getUsageEntries());
		}
		return usageEntries;
	}
	
	private void setUsageMap(Map<Double, UsageInfo> usageMap) {
		this.usageMap = usageMap;
	}
	
	protected Map<Double, UsageInfo> getUsageMap() {
		return usageMap;
	}
	
	public void updateUsage(double time) {
		getUsageMap().put( time,
				new UsageInfo(getId(), time, preemptionPolicy
						.getPriorityToInUseMips(), preemptionPolicy
						.getPriorityToVms(), getTotalUsage(),
						getAvailableMips()));
	}

	public void resetUsageMap() {
		getUsageMap().clear();
	}
	
	public PreemptionPolicy getPreemptionPolicy() {
		return preemptionPolicy;
	}
	
	private void setPreemptionPolicy(PreemptionPolicy preemptionPolicy) {
		this.preemptionPolicy = preemptionPolicy;
	}

}