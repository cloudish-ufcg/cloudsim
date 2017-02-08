package org.cloudbus.cloudsim.preemption;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alessandro Lia Fook Santos on 08/02/17.
 */
public class GreedyQuotaAdmissionController implements AdmissionController {

    private double datacenterCapacity;
    private Map<Integer, Double> sloTargets;
    private Map<Integer, Double> quota;
    private double confidanceLevel;

    public GreedyQuotaAdmissionController(double datacenterCapacity, Map<Integer, Double> sloTargets, double confidenceLevel){
        setDatacenterCapacity(datacenterCapacity);
        setSloTargets(sloTargets);
        setQuota(sloTargets);
        setConfidanceLevel(confidenceLevel);
    }

    @Override
    public void calculateQuota(Map<Integer, Double> admittedRequests) {

        for (Integer priority: sloTargets.keySet()) {

            double allocatedResources = 0d;

            for (int i = 0; i < priority; i++) {
                allocatedResources += admittedRequests.get(priority);
            }

            double capacity = getDatacenterCapacity() - calculateCapacity(allocatedResources);
            double actualQuota = (capacity / getSloTargets().get(priority)) * getConfidanceLevel();

            getQuota().put(priority, new Double(actualQuota));
        }
    }

    @Override
    public boolean accept(PreemptableVm vm) {

        double quota = getQuota().get(vm.getPriority());

        if (vm.getMips() <= quota) {
            return true;

        } else {
            return false;
        }
    }

    public Map<Integer, Double> getQuota() {
        return quota;
    }

    public void setQuota(Map<Integer, Double> sloTargets) {

        this.quota = new HashMap<Integer, Double>();

        for (Integer priority : sloTargets.keySet()) {
            quota.put(priority, 0d);
        }
    }

    private double calculateCapacity(double allocatedResources){
        return getDatacenterCapacity() - allocatedResources;
    }


    public double getDatacenterCapacity() {
        return datacenterCapacity;
    }


    public void setDatacenterCapacity(double datacenterCapacity) {
        this.datacenterCapacity = datacenterCapacity;
    }

    public Map<Integer, Double> getSloTargets() {
        return sloTargets;
    }

    public void setSloTargets(Map<Integer, Double> sloTargets) {
        this.sloTargets = sloTargets;
    }

    public double getConfidanceLevel() {
        return confidanceLevel;
    }

    public void setConfidanceLevel(double confidanceLevel) {
        this.confidanceLevel = confidanceLevel;
    }
}
