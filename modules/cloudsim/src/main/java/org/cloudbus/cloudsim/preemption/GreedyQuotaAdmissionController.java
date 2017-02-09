package org.cloudbus.cloudsim.preemption;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alessandro Lia Fook Santos on 08/02/17.
 */
public class GreedyQuotaAdmissionController implements AdmissionController {

    private double datacenterCapacity;
    private Map<Integer, Double> sloTargets;
    private Map<Integer, Double> quotaByPriority;
    private double confidanceFactor;

    public GreedyQuotaAdmissionController(double datacenterCapacity, Map<Integer, Double> sloTargets, double confidenceLevel){
        setDatacenterCapacity(datacenterCapacity);
        setSloTargets(sloTargets);
        setConfidanceFactor(confidenceLevel);
        startQuotaByPriority(sloTargets);
    }

    @Override
    public void calculateQuota(Map<Integer, Double> admittedRequests) {

        for (Integer priority: sloTargets.keySet()) {

            double allocatedResources = 0d;

            for (int i = 0; i <= priority; i++) {
                allocatedResources += admittedRequests.get(i);
            }

            double capacity = calculateCapacity(allocatedResources);
            double actualQuota = (capacity / getSloTargets().get(priority)) * getConfidanceFactor();

            getQuotaByPriority().put(priority, new Double(actualQuota));
        }
    }

    @Override
    public boolean accept(PreemptableVm vm) {

        double quota = getQuotaByPriority().get(vm.getPriority());

        if (vm.getMips() <= quota) {
            return true;

        } else {
            return false;
        }
    }

    public Map<Integer, Double> getQuotaByPriority() {
        return quotaByPriority;
    }

    public void setQuotaByPriority(Map<Integer, Double> sloTargets) {

        this.quotaByPriority = new HashMap<Integer, Double>();

        for (Integer priority : sloTargets.keySet()) {
            quotaByPriority.put(priority, 0d);
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

    public double getConfidanceFactor() {
        return confidanceFactor;
    }

    public void setConfidanceFactor(double confidanceFactor) {
        this.confidanceFactor = confidanceFactor;
    }

    public void startQuotaByPriority(Map<Integer,Double> sloTargets) {

        Map<Integer, Double> admittedRequests = new HashMap<Integer, Double>();
        setQuotaByPriority(new HashMap<Integer, Double>());

        for (Integer priority : sloTargets.keySet()) {
            admittedRequests.put(priority, 0d);
            getQuotaByPriority().put(priority, 0d);
        }

        calculateQuota(admittedRequests);
    }
}
