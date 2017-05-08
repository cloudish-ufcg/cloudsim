package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;

/**
 * 
 * @author Giovanni Farias
 *
 */
public abstract class PreemptableVmAllocationPolicy extends VmAllocationPolicy {
	
	private SortedSet<PreemptableVm> vmsRunning = new TreeSet<PreemptableVm>();
	private SortedSet<PreemptableVm> vmsWaiting = new TreeSet<PreemptableVm>();
	
	protected SimulationTimeUtil simulationTimeUtil;
	protected double lastProcessTime = -1;

	public PreemptableVmAllocationPolicy (List<? extends Host> hostList){
		super(hostList);
	}
	
	public abstract void preProcess();

	//TODO Think about remove this method

	public abstract Host selectHost(Vm vm);
	
	public abstract void addHostIntoStructure(PreemptiveHost host);
	
	public abstract void removeHostFromStructure(PreemptiveHost host);
	
	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean allocateHostForVm(Vm vm) {
		return allocateHostForVm(vm, false);
	}
	
	public boolean allocateHostForVm(Vm vm, boolean isBackfilling) {
		if (vm == null) {
			throw new IllegalArgumentException("The Vm can not be null.");
		}
		
		PreemptableVm pVm = (PreemptableVm) vm;
		
		if (simulationTimeUtil.clock() > lastProcessTime) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": Time passed out. Pre processing and updating last process time to ", simulationTimeUtil.clock());
			lastProcessTime = simulationTimeUtil.clock();
			preProcess();			
		}
		
		PreemptiveHost host = (PreemptiveHost) selectHost(pVm);	
		
		if (host == null) {
			Log.printConcatLine(simulationTimeUtil.clock(),
					": There is not resource to allocate VM #" + vm.getId() + " now, it will be tryed in the future.");
			if (!getVmsWaiting().contains(pVm)) {
				getVmsWaiting().add(pVm);
			}
			return false;
		}
		
		return allocateHostForVm(pVm, host, isBackfilling);
	}
	
    public boolean allocateHostForVm(Vm vm, Host host, boolean isBackfilling) {
    	if (vm == null) {
			throw new IllegalArgumentException("The Vm can not be null.");
		}
		
		if (host == null) {
			return false;
		}
		
        PreemptiveHost pHost = (PreemptiveHost) host;
        PreemptableVm pVm = (PreemptableVm) vm;
        
        removeHostFromStructure(pHost);
        boolean result = pHost.vmCreate(pVm);
        
		if (!result) {
			while (!result) {
				PreemptableVm vmToPreempt = (PreemptableVm) pHost.nextVmForPreempting();
				
				// TODO do we nedd this check?
				if (vmToPreempt != null && vmToPreempt.getPriority() >= pVm.getPriority()) {
					Log.printConcatLine(simulationTimeUtil.clock(),
							": Preempting VM #" + vmToPreempt.getId() + " (priority " + vmToPreempt.getPriority()
									+ ") to allocate VM #" + vm.getId() + " (priority " + pVm.getPriority() + ")");
				
					// preempting vm
					vmToPreempt.preempt(simulationTimeUtil.clock());
					pHost.vmDestroy(vmToPreempt);					
					vmToPreempt.setBeingInstantiated(true);
					
					getVmsRunning().remove(vmToPreempt);
					getVmsWaiting().add(vmToPreempt);
					
					result = pHost.vmCreate(pVm);					
				}
			}
		}    
        
		// updating allocation structures
		getVmsRunning().add(pVm);
		getVmsWaiting().remove(pVm);

		pVm.setStartExec(simulationTimeUtil.clock());
		pVm.allocatingToHost(host.getId());
		
		if (isBackfilling) {
			pVm.setNumberOfBackfillingChoice(pVm.getNumberOfBackfillingChoice() + 1);				
		}

		if (pVm.isBeingInstantiated()) {
			pVm.setBeingInstantiated(false);
		}

		Log.printConcatLine(simulationTimeUtil.clock(), ": VM #", vm.getId(), " was allocated on host #", host.getId(),
				" successfully.");
		
		addHostIntoStructure(pHost);

        return result;
	}
    
	@Override
    public boolean allocateHostForVm(Vm vm, Host host) {
		return allocateHostForVm(vm, host, false);
    }

	@Override
    public void deallocateHostForVm(Vm vm) {
		
    	if (vm == null)
            throw new IllegalArgumentException("The Vm can not be null.");
		
        PreemptiveHost host = (PreemptiveHost) vm.getHost();

        if (host != null) {        	
			if (!getVmsRunning().remove(vm)) {
				Log.printConcatLine(simulationTimeUtil.clock(), ": VM #",
						vm.getId(), " is not running to be removed.");
			}
			
			removeHostFromStructure(host);
            host.vmDestroy(vm);
            addHostIntoStructure(host);
        }
    }

	@Override
	public Host getHost(Vm vm) {
		return null;
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return null;
	}

	public void setSimulationTimeUtil(SimulationTimeUtil simulationTimeUtil) {
		this.simulationTimeUtil = simulationTimeUtil;
	}
	
	public SortedSet<PreemptableVm> getVmsRunning() {
		return vmsRunning;
	}

	public boolean thereIsVmsRunning() {
		return !getVmsRunning().isEmpty();
	}

	public SortedSet<PreemptableVm> getVmsWaiting() {
		return vmsWaiting;
	}

	public boolean thereIsVmsWaiting() {
		return !getVmsWaiting().isEmpty();
	}

	public List<PreemptableVm> tryToAllocateWaitingQueue() {
		Log.printConcatLine(simulationTimeUtil.clock(), ": Trying to allocate the VMs of waiting queue.");
		
		boolean isBackfilling = false;
		List<PreemptableVm> allocatedVMs = new ArrayList<PreemptableVm>();
		
		PreemptiveHost oneHost = (PreemptiveHost) getHostList().get(0);
		SortedSet<PreemptableVm> waitingQueue = oneHost.getPreemptionPolicy().sortVms(getVmsWaiting());	
		
		// trying to allocate the VMs of waiting queue
		for (PreemptableVm currentVm : waitingQueue) {			
			if (!allocateHostForVm(currentVm, isBackfilling)) {
				isBackfilling = true;
			} else {
				allocatedVMs.add(currentVm);
			}
		}
		
		return allocatedVMs;
	}
}