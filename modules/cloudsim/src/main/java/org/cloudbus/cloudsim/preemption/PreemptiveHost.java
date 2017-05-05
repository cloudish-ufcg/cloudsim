package org.cloudbus.cloudsim.preemption;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.preemption.policies.preemption.FCFSBasedPreemptionPolicy;
import org.cloudbus.cloudsim.preemption.policies.preemption.PreemptionPolicy;
import org.cloudbus.cloudsim.preemption.util.DecimalUtil;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import gnu.trove.map.hash.THashMap;

public class PreemptiveHost extends Host implements Comparable<Host> {
	
	public static final int DECIMAL_ACCURACY = 9;
		
	private Map<Double, UsageInfo> usageMap;
	private PreemptionPolicy preemptionPolicy;

	public PreemptiveHost(int id, List<? extends Pe> peList, VmScheduler vmScheduler, PreemptionPolicy preemptionPolicy) {
		super(id, new RamProvisionerSimple(Integer.MAX_VALUE),
				new BwProvisionerSimple(Integer.MAX_VALUE), Integer.MAX_VALUE,
				peList, vmScheduler);
		
		setUsageMap(new THashMap<Double, UsageInfo>());
		setPreemptionPolicy(preemptionPolicy);
		preemptionPolicy.setTotalMips(((VmSchedulerMipsBased) getVmScheduler())
				.getTotalMips());
	}

	public PreemptiveHost(double mips, int numberOfPriorities) {
		this(Integer.MIN_VALUE, new ArrayList<Pe>() {
			{
				add(new Pe(0, new PeProvisionerSimple(mips)));
			}
		}, new VmSchedulerMipsBased(new ArrayList<Pe>() {
			{
				add(new Pe(0, new PeProvisionerSimple(mips)));
			}
		}), new FCFSBasedPreemptionPolicy(new Properties() {
			{
				setProperty(FCFSBasedPreemptionPolicy.NUMBER_OF_PRIORITIES_PROP, String.valueOf(numberOfPriorities));
			}
		})); 
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

    public boolean equals(Object obj) {

		if(obj instanceof PreemptiveHost) {
			PreemptiveHost host = (PreemptiveHost) obj;
			return host.getId() == this.getId();
		}
		return false;
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
		return DecimalUtil.format(((VmSchedulerMipsBased)getVmScheduler()).getMipsInUse());
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

	@Override
	public double getTotalMips(){
		return ((VmSchedulerMipsBased) getVmScheduler()).getTotalMips();
	}
	
	public List<UsageEntry> getUsageEntries(double clock) {
		List<UsageEntry> usageEntries = new LinkedList<UsageEntry>();

		List<UsageInfo> usageInfos = getUsageInfos();
		resetUsageMap();

		for (UsageInfo usageInfo : usageInfos) {

			if (usageInfo.getTime() < clock) {
				usageEntries.addAll(usageInfo.getUsageEntries());

			} else {
				putUsageInfo(usageInfo);
			}
		}
		return usageEntries;
	}

	public List<UsageInfo> getUsageInfos() {

		List<UsageInfo> usageInfos = new LinkedList<>();
		usageInfos.addAll(getUsageMap().values());

		return usageInfos;
	}

	public void putUsageInfo(UsageInfo usageInfo) {
		getUsageMap().put(usageInfo.getTime(), usageInfo);
	}

	public void resetUsageMap() {
		getUsageMap().clear();
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

	public PreemptionPolicy getPreemptionPolicy() {
		return preemptionPolicy;
	}
	
	private void setPreemptionPolicy(PreemptionPolicy preemptionPolicy) {
		this.preemptionPolicy = preemptionPolicy;
	}


	public double getAvailableMipsByPriorityAndAvailability(int priority) {
		return preemptionPolicy.getAvailableMipsByPriorityAndAvailability(priority);
	}

	public double getAvailableMipsByVm(PreemptableVm vm) {
		return preemptionPolicy.getAvailableMipsByVm(vm);
	}

	public Map<Integer, List<CapacityCost>> getCapacityCosts(double minCPUReq, double maxCPUReq) {
		// TODO Auto-generated method stub
		return null;
	}}