package org.cloudbus.cloudsim.preemption.policies.vmallocation;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.util.VmAvailabilityBasedPreemptiveHostComparator;

import java.util.Collections;
import java.util.List;

/**
 * Created by Alessandro Lia Fook Santos on 02/12/16.
 */
public class WorstFitAvailabilityAwareVmAllocationPolicy extends PreemptableVmAllocationPolicy {

    public WorstFitAvailabilityAwareVmAllocationPolicy(List<Host> hostList) {
        super(hostList);
    }

    @Override
    public boolean preempt(PreemptableVm vm) {

        Host host = vm.getHost();

        if (validateHostForVm(vm, host))
            return false;

        vm.preempt(simulationTimeUtil.clock());
        host.vmDestroy(vm);
        vm.setBeingInstantiated(true);
        return true;
    }

    private boolean validateHostForVm(PreemptableVm vm, Host host) {
        if (host == null) {
            Log.printConcatLine(simulationTimeUtil.clock(),
                    ": VM #", vm.getId(), " do not have a host.");

            return true;
        }
        return false;
    }

    @Override
    public Host selectHost(Vm vm) {

        verifyVm(vm);
        if (! getHostList().isEmpty()) {

            PreemptableVm pVm = (PreemptableVm) vm;
            VmAvailabilityBasedPreemptiveHostComparator comparator = new VmAvailabilityBasedPreemptiveHostComparator(pVm);
            Collections.sort(getHostList(), comparator);
            PreemptiveHost firstHost = (PreemptiveHost) getHostList().get(0);

            if (firstHost.isSuitableForVm(pVm)) {
                return firstHost;
            }
        }
        return null;
    }

    private void verifyVm(Vm vm) {
        if (vm == null)
            throw new IllegalArgumentException("The Vm can not be null.");
    }
    @Override
    public boolean allocateHostForVm(Vm vm) {
        Host host = selectHost(vm);

        if (host == null) {
            return false;
        }

        // just to update the sorted set
        boolean result = host.vmCreate(vm);
        System.out.println(result);

        return result;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {

        if (host == null) {
            return false;
        }

        // just to update the sorted set
        boolean result = host.vmCreate(vm);

        return result;
    }

    @Override
    public void deallocateHostForVm(Vm vm) {

        Host host = vm.getHost();
        validateHostForVm((PreemptableVm)vm,  host);

        if (host != null) {
            host.vmDestroy(vm);
        }
    }
}
