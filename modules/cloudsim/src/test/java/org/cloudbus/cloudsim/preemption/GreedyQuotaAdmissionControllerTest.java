package org.cloudbus.cloudsim.preemption;

import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Alessandro Lia Fook Santos on 08/02/17.
 */
public class GreedyQuotaAdmissionControllerTest {

    public GreedyQuotaAdmissionController admController;
    public Map<Integer, Double> sloTargets;


    @Before
    public void setUp() {

        sloTargets = new HashMap<Integer, Double>();
        sloTargets.put(new Integer(0), new Double(1));
        sloTargets.put(new Integer(1), new Double(0.9));
        sloTargets.put(new Integer(2), new Double(0.5));

        admController = new GreedyQuotaAdmissionController(6603.5, sloTargets, 1);
    }

}