package org.cloudbus.cloudsim.preemption;

/**
 * Created by Alessandro Lia Fook Santos on 10/05/17.
 */
public class CostSkin implements Comparable<CostSkin>{

    private double cost;
    private PreemptableVm vm;

    public CostSkin(PreemptableVm vm, double cost) {

        this.vm = vm;
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public PreemptableVm getVm() {
        return vm;
    }

    public void setVm(PreemptableVm vm) {
        this.vm = vm;
    }

    @Override
    public int compareTo(CostSkin costSkin) {

        if(this.cost != costSkin.getCost())
            return Double.compare(getCost(), costSkin.getCost());

        else
            return Integer.compare(getVm().getId(), costSkin.getVm().getId());
    }
}
