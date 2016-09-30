package org.cloudbus.cloudsim.googletrace;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.googletrace.util.DecimalUtil;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class GoogleHost extends Host implements Comparable<Host> {
	
	private Map<Integer, Double> priorityToInUseMips;
	private Map<Integer, SortedSet<Vm>> priorityToVms;
	private int numberOfPriorities;
	public static final int DECIMAL_ACCURACY = 9;
	
	private Map<Double, UsageEntry> usageMap;

	public GoogleHost(int id, List<? extends Pe> peList, VmScheduler vmScheduler, int numberOfPriorities) {
		super(id, new RamProvisionerSimple(Integer.MAX_VALUE),
				new BwProvisionerSimple(Integer.MAX_VALUE), Integer.MAX_VALUE,
				peList, vmScheduler);
		
		if (numberOfPriorities < 1) {
			throw new IllegalArgumentException("Number of priorities must be bigger than zero.");
		}
		
		setPriorityToVms(new HashMap<Integer, SortedSet<Vm>>());
		setPriorityToInUseMips(new HashMap<Integer, Double>());
		setUsageMap(new HashMap<Double, UsageEntry>());
		setNumberOfPriorities(numberOfPriorities);
		
		// initializing maps
		for (int priority = 0; priority < numberOfPriorities; priority++) {
			getPriorityToVms().put(priority, new TreeSet<Vm>());
			getPriorityToInUseMips().put(priority, new Double(0));
		}
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
		for (int i = getNumberOfPriorities() - 1; i >= 0; i--) {
			if (!getPriorityToVms().get(i).isEmpty()) {
				return getPriorityToVms().get(i).last();
			}
		}
		return null;
	}

	@Override
	public boolean isSuitableForVm(Vm vm) {

		if (vm == null) {
			return false;
		}

		else if (getVmScheduler().getAvailableMips() >= vm.getMips()) {
			return true;

		} else {
			GoogleVm gVm = (GoogleVm) vm;
			double availableMips = getAvailableMipsByPriority(gVm.getPriority()) ;
			return (availableMips >= vm.getMips());
		}		
	}
	
	@Override
	public boolean vmCreate(Vm vm) {
		/*
		 * TODO The Host class add the VM into a List. We don't need that list.
		 * We may optimize the code.
		 */
		if (vm == null) {
			return false;
		}
		GoogleVm gVm = (GoogleVm) vm;

		Log.printConcatLine(CloudSim.clock(), ": Creating VM#", vm.getId(), "(priority ", gVm.getPriority(),") on host #", getId());
		
		boolean result = super.vmCreate(vm);
		
		if (result) {
			// updating maps
			
			getPriorityToVms().get(gVm.getPriority()).add(gVm);
			double priorityCurrentUse = getPriorityToInUseMips().get(gVm.getPriority()); 
			getPriorityToInUseMips().put( gVm.getPriority(),
					DecimalUtil.format(priorityCurrentUse + gVm.getMips(), DECIMAL_ACCURACY));
			
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
		for (Integer priority : getPriorityToInUseMips().keySet()) {
			totalUsage += getPriorityToInUseMips().get(priority);
		}
		return DecimalUtil.format(totalUsage, DECIMAL_ACCURACY);
	}
	@Override
	public void vmDestroy(Vm vm) {
		super.vmDestroy(vm);

		// updating maps
		GoogleVm gVm = (GoogleVm) vm;
		
		getPriorityToVms().get(gVm.getPriority()).remove(vm);
		double priorityCurrentUse = getPriorityToInUseMips().get(gVm.getPriority()); 
		
		getPriorityToInUseMips().put( gVm.getPriority(),
				DecimalUtil.format(priorityCurrentUse - gVm.getMips(), DECIMAL_ACCURACY));
	}

	public Map<Integer, Double> getPriorityToInUseMips() {
		return priorityToInUseMips;
	}

	protected void setPriorityToInUseMips(
			Map<Integer, Double> priorityToMipsInUse) {
		this.priorityToInUseMips = priorityToMipsInUse;
	}

	public Map<Integer, SortedSet<Vm>> getPriorityToVms() {
		return priorityToVms;
	}

	protected void setPriorityToVms(Map<Integer, SortedSet<Vm>> priorityToVms) {
		this.priorityToVms = priorityToVms;
	}

	public int getNumberOfPriorities() {
		return numberOfPriorities;
	}
	
	protected void setNumberOfPriorities(int numberOfPriorities) {
		this.numberOfPriorities = numberOfPriorities;
	}
	
	/*
	 * TODO we need to refactor this code. we should not use cast here We also
	 * need to check where getTotalMips from Host class is being used because
	 * its return is int type
	 */
	public double getAvailableMipsByPriority(int priority) {
		double inUseByNonPreemptiveVms = 0;

		for (int i = 0; i <= priority; i++) {
			inUseByNonPreemptiveVms += getPriorityToInUseMips().get(i);
		}

		return DecimalUtil.format(((VmSchedulerMipsBased) getVmScheduler()).getTotalMips()
				- inUseByNonPreemptiveVms, DECIMAL_ACCURACY);
	}

	@Override
	public double getTotalMips(){
		return ((VmSchedulerMipsBased) getVmScheduler()).getTotalMips();
	}
	
	public List<UsageEntry> getUsageEntries() {
		return new LinkedList<UsageEntry>(usageMap.values());
	}
	
	private void setUsageMap(Map<Double, UsageEntry> usageEntries) {
		this.usageMap = usageEntries;
	}
	
	private Map<Double, UsageEntry> getUsageMap() {
		return usageMap;
	}
	
	public void updateUtilization(double time) {
		getUsageMap().put(time, new UsageEntry(getId(), time, getPriorityToInUseMips(),
						getPriorityToVms(), getTotalUsage(), getAvailableMips()));
//		//TODO remove it
//		double totalUsage = 0;
//		for (Integer priority : getPriorityToInUseMips().keySet()) {
//			totalUsage += getPriorityToInUseMips().get(priority);
//		}
//		System.out.println("currenTime=" + time + ", totalUsage=" + totalUsage);
	}

	public void resetUtilizationMap() {
		getUsageMap().clear();
	}
	
	public double getUsageByPriority(int priority) {
		if (getPriorityToInUseMips().get(priority) != null) {
			return getPriorityToInUseMips().get(priority);
		}
		return 0;
	}
}