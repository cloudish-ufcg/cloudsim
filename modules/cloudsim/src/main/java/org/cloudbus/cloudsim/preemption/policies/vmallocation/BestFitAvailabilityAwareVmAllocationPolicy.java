package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;

import java.util.List;

/**
 * Created by jvmafra on 03/04/17.
 */
public class BestFitAvailabilityAwareVmAllocationPolicy extends PreemptableVmAllocationPolicy{

    public BestFitAvailabilityAwareVmAllocationPolicy(List<? extends Host> hostList) {
        super(hostList);
    }

    @Override
    public void preProcess() {

    }

    @Override
    public boolean preempt(PreemptableVm vm) {
        return false;
    }

    @Override
    public Host selectHost(Vm vm) {
        return null;
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
        return false;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        return false;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {

    }
}
