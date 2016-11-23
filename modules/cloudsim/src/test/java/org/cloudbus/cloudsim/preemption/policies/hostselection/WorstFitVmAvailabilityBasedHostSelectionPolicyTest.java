package org.cloudbus.cloudsim.preemption.policies.hostselection;

import org.cloudbus.cloudsim.preemption.PreemptiveHost;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.cloudbus.cloudsim.preemption.policies.preemption.VmAvailabilityBasedPreemptionPolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by Alessandro Lia Fook Santos on 23/11/16.
 */
public class WorstFitVmAvailabilityBasedHostSelectionPolicyTest {


    List<PreemptiveHost> hosts;
    HostSelectionPolicy selectionPolicy;
    VmAvailabilityBasedPreemptionPolicy preemptionPolicy;

    Properties propeties;
    SimulationTimeUtil timeUtil;

    PreemptiveHost host1;
    PreemptiveHost host2;
    PreemptiveHost host3;
    PreemptiveHost host4;
    PreemptiveHost host5;
    PreemptiveHost host6;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void select() throws Exception {

    }

}