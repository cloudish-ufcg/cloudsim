package org.cloudbus.cloudsim.preemption;

import java.util.HashMap;
import java.util.Map;

import gnu.trove.map.hash.THashMap;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.preemption.util.DecimalUtil;

/**
 * Created by Alessandro Lia Fook Santos on 08/02/17.
 */
public class GreedyQuotaAdmissionController implements AdmissionController {


	private double datacenterCapacity;
    private Map<Integer, Double> sloTargets;
    private Map<Integer, Double> priorityToQuotas;
    private double confidanceFactor;

    public GreedyQuotaAdmissionController(double datacenterCapacity, Map<Integer, Double> sloTargets,
                                          double confidenceLevel) {
        setDatacenterCapacity(datacenterCapacity);
        setSloTargets(sloTargets);
        setConfidanceFactor(confidenceLevel);
        initializeQuotas();
    }

    @Override
    public void calculateQuota(Map<Integer, Double> admittedRequests) {

            for (Integer priority : getSloTargets().keySet()) {

                double greaterPrioritiesResources = 0d;

                for (int i = 0; i < priority; i++) {
                    greaterPrioritiesResources += admittedRequests.get(i);
                }

                double classCapacity = getDatacenterCapacity() - greaterPrioritiesResources;
                double classQuota = (classCapacity / getSloTargets().get(priority)) * getConfidanceFactor();

                getPriorityToQuotas().put(priority, DecimalUtil.format(classQuota));
            }

    }

    @Override
    public boolean accept(PreemptableVm vm, Map<Integer, Double> admittedRequests) {
        double currentQuota = getPriorityToQuotas().get(vm.getPriority());

        if (admittedRequests.get(vm.getPriority()) + vm.getMips() <= currentQuota) {
            return true;
        } 
        return false;
    }

    public Map<Integer, Double> getPriorityToQuotas() {
        return priorityToQuotas;
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

    public void initializeQuotas() {

        Map<Integer, Double> admittedRequests = new THashMap<Integer, Double>();

        priorityToQuotas = new THashMap<Integer, Double>();
        
        for (Integer priority : getSloTargets().keySet()) {
            admittedRequests.put(priority, 0d);
            getPriorityToQuotas().put(priority, 0d);
        }

        calculateQuota(admittedRequests);
    }
}
