package org.cloudbus.cloudsim.preemption.util;

import java.util.Comparator;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.PreemptiveHost;

public class VmAvailabilityBasedPreemptiveHostComparator implements
        Comparator<PreemptiveHost> {

    private PreemptableVm vm;

    public VmAvailabilityBasedPreemptiveHostComparator(PreemptableVm vm){
        setVm(vm);
    }

    @Override
    public int compare(PreemptiveHost host1, PreemptiveHost host2) {
        int result = (-1)
                * (new Double(host1.getAvailableMipsByVm(getVm()))
                .compareTo(new Double(host2.getAvailableMipsByVm(getVm()))));

        if (result == 0)
            return new Integer(host1.getId()).compareTo(new Integer(host2.getId()));

        return result;
    }

    private PreemptableVm getVm() {
        return vm;
    }

    private void setVm(PreemptableVm vm) {
        this.vm = vm;
    }
}