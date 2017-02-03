package org.cloudbus.cloudsim.preemption.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Assert;

import org.cloudbus.cloudsim.preemption.PreemptableVm;
import org.cloudbus.cloudsim.preemption.SimulationTimeUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by João Mafra on 02/02/17.
 */
public class PriorityAndTTVBasedPreemptableVmComparatorTest {

    private int submitTime = 0;
    private long runtime = 10;
    private PreemptableVm vm0, vm1;
    private Map<Integer, Double> sloTargets;

    @Before
    public void setUp(){
        sloTargets = new HashMap<>();

        // setting sloTargets
        sloTargets.put(0, 1.0);
        sloTargets.put(1, 0.9);
        sloTargets.put(2, 0.5);
    }

    @Test
    public void testComparatorForDifferentPriorities(){
        SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);

		PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(
				new HashMap<Integer, Double>(), simulationTimeUtil);

        vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 0, runtime);
        vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 1, runtime);

        // vm0 has priority greater than vm1
        Assert.assertEquals(-1, comparator.compare(vm0, vm1));
        Assert.assertEquals(1, comparator.compare(vm1, vm0));

    }

    @Test
    public void testComparatorForSamePriorities1(){
        SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);

        PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(sloTargets,simulationTimeUtil);

        // currentRuntime = 3, minRuntime = 5 (SLOTarget = 100%), TTV = -2
        vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 0, runtime);
        vm0.setStartExec(2);

        // currentRuntime = 2, minRuntime = 5 (SLOTarget = 100%), TTV = -3
        vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 0, runtime);
        vm1.setStartExec(3);

        // vm0TTV > vm1TTV
        Assert.assertEquals(1, comparator.compare(vm0, vm1));
        Assert.assertEquals(-1, comparator.compare(vm1, vm0));

    }

    @Test
    public void testComparatorForSamePriorities2(){
        SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);

        PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(sloTargets,simulationTimeUtil);

        // currentRuntime = 4, minRuntime = 2.5 (SLOTarget = 50%), TTV = 1.5
        vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 2, runtime);
        vm0.setStartExec(1);

        // currentRuntime = 5, minRuntime = 2.5 (SLOTarget = 50%), TTV = 2.5
        vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 2, runtime);
        vm1.setStartExec(0);

        // vm1TTV > vm0TTV
        Assert.assertEquals(-1, comparator.compare(vm0, vm1));
        Assert.assertEquals(1, comparator.compare(vm1, vm0));

    }

    @Test
    public void testComparatorForSamePriorities3(){
        SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);

        PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(sloTargets,simulationTimeUtil);

        // currentRuntime = 4, minRuntime = 2.5 (SLOTarget = 50%), TTV = 1.5
        vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 2, runtime);
        vm0.setStartExec(1);

        // currentRuntime = 1, minRuntime = 2.5 (SLOTarget = 50%), TTV = -1.5
        vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 2, runtime);
        vm1.setStartExec(4);

        // vm0TTV > vm1TTV
        Assert.assertEquals(1, comparator.compare(vm0, vm1));
        Assert.assertEquals(-1, comparator.compare(vm1, vm0));

    }

    @Test
    public void testComparatorForSamePrioritiesAndTTV(){
        SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);

        PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(sloTargets,simulationTimeUtil);

        // currentRuntime = 4, minRuntime = 2.5 (SLOTarget = 50%), TTV = 1.5
        vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 2, runtime);
        vm0.setStartExec(1);

        // currentRuntime = 4, minRuntime = 2.5 (SLOTarget = 50%), TTV = 1.5
        vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 2, runtime);
        vm1.setStartExec(1);

        // vm0ID < vm1ID
        Assert.assertEquals(-1, comparator.compare(vm0, vm1));
        Assert.assertEquals(1, comparator.compare(vm1, vm0));

    }

    @Test
    public void testComparatorForSamePrioritiesAndTTV2(){
        SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);

        PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(sloTargets,simulationTimeUtil);

        // currentRuntime = 1, minRuntime = 2.5 (SLOTarget = 50%), TTV = -1.5
        vm0 = new PreemptableVm(4, 1, 5, 0, submitTime, 2, runtime);
        vm0.setStartExec(4);

        // currentRuntime = 1, minRuntime = 2.5 (SLOTarget = 50%), TTV = -1.5
        vm1 = new PreemptableVm(3, 1, 5, 0, submitTime, 2, runtime);
        vm1.setStartExec(4);

        // vm0ID > vm1ID
        Assert.assertEquals(1, comparator.compare(vm0, vm1));
        Assert.assertEquals(-1, comparator.compare(vm1, vm0));

    }
    @Test
    public void testSortingVMs(){
        SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);

        PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(sloTargets, simulationTimeUtil);

        // currentRuntime = 1, minRuntime = 2.5 (SLOTarget = 50%), TTV = -1.5
        PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 2, runtime);
        vm0.setStartExec(4);

        // currentRuntime = 4, minRuntime = 2.5 (SLOTarget = 50%), TTV = 1.5
        PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 2, runtime);
        vm1.setStartExec(1);
        
        // currentRuntime = 3, minRuntime = 5 (SLOTarget = 100%), TTV = -2
        PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 0, submitTime, 0, runtime);
        vm2.setStartExec(2);

        // currentRuntime = 2, minRuntime = 5 (SLOTarget = 100%), TTV = -3
        PreemptableVm vm3 = new PreemptableVm(3, 1, 5, 0, submitTime, 0, runtime);
        vm3.setStartExec(3);
        
        // currentRuntime = 5, minRuntime = 2.5 (SLOTarget = 50%), TTV = 2.5
        PreemptableVm vm4 = new PreemptableVm(6, 1, 5, 0, submitTime, 2, runtime);
        vm4.setStartExec(0);
        
        // currentRuntime = 2, minRuntime = 5 (SLOTarget = 100%), TTV = -3
        PreemptableVm vm5 = new PreemptableVm(5, 1, 5, 0, submitTime, 0, runtime);
        vm5.setStartExec(3);
        
        // currentRuntime = 4, minRuntime = 2.5 (SLOTarget = 50%), TTV = 1.5
        PreemptableVm vm6 = new PreemptableVm(6, 1, 5, 0, submitTime, 0, runtime);
        vm6.setStartExec(1);

        List<PreemptableVm> sortedVms = new ArrayList<PreemptableVm>();
        
        sortedVms.add(vm0);
        sortedVms.add(vm1);
        sortedVms.add(vm2);
        sortedVms.add(vm3);
        sortedVms.add(vm4);
        sortedVms.add(vm5);
        sortedVms.add(vm6);

        Collections.sort(sortedVms, comparator);
        
        Assert.assertEquals(vm3, sortedVms.get(0)); //vm3 is smaller priority, TTV and ID
        Assert.assertEquals(vm5, sortedVms.get(1));
        Assert.assertEquals(vm2, sortedVms.get(2));
        Assert.assertEquals(vm6, sortedVms.get(3));
        Assert.assertEquals(vm0, sortedVms.get(4)); //vm0 has priority 2
        Assert.assertEquals(vm1, sortedVms.get(5));
        Assert.assertEquals(vm4, sortedVms.get(6));
    }   
    
      
    @Test
    public void testSortingVMs2(){
        SimulationTimeUtil simulationTimeUtil = Mockito.mock(SimulationTimeUtil.class);
        Mockito.when(simulationTimeUtil.clock()).thenReturn(5d);

        PriorityAndTTVBasedPreemptableVmComparator comparator = new PriorityAndTTVBasedPreemptableVmComparator(sloTargets, simulationTimeUtil);

        // currentRuntime = 1, minRuntime = 2.5 (SLOTarget = 50%), TTV = -1.5
        PreemptableVm vm0 = new PreemptableVm(0, 1, 5, 0, submitTime, 2, runtime);
        vm0.setStartExec(4);

        // currentRuntime = 4, minRuntime = 2.5 (SLOTarget = 50%), TTV = 1.5
        PreemptableVm vm1 = new PreemptableVm(1, 1, 5, 0, submitTime, 2, runtime);
        vm1.setStartExec(1);
        
        // currentRuntime = 3, minRuntime = 5 (SLOTarget = 100%), TTV = -2
        PreemptableVm vm2 = new PreemptableVm(2, 1, 5, 0, submitTime, 0, runtime);
        vm2.setStartExec(2);

        // currentRuntime = 2, minRuntime = 5 (SLOTarget = 100%), TTV = -3
        PreemptableVm vm3 = new PreemptableVm(3, 1, 5, 0, submitTime, 0, runtime);
        vm3.setStartExec(3);
        
        // currentRuntime = 5, minRuntime = 2.5 (SLOTarget = 50%), TTV = 2.5
        PreemptableVm vm4 = new PreemptableVm(6, 1, 5, 0, submitTime, 2, runtime);
        vm4.setStartExec(0);
        
        // currentRuntime = 2, minRuntime = 5 (SLOTarget = 100%), TTV = -3
        PreemptableVm vm5 = new PreemptableVm(5, 1, 5, 0, submitTime, 0, runtime);
        vm5.setStartExec(3);
        
        // currentRuntime = 4, minRuntime = 2.5 (SLOTarget = 50%), TTV = 1.5
        PreemptableVm vm6 = new PreemptableVm(6, 1, 5, 0, submitTime, 0, runtime);
        vm6.setStartExec(1);

        SortedSet<PreemptableVm> sortedVms = new TreeSet<PreemptableVm>(comparator);
        
        sortedVms.add(vm0);
        sortedVms.add(vm1);
        sortedVms.add(vm2);
        sortedVms.add(vm3);
        sortedVms.add(vm4);
        sortedVms.add(vm5);
        sortedVms.add(vm6);

        // checking and removing
        Assert.assertEquals(vm3, sortedVms.first());
        Assert.assertEquals(vm4, sortedVms.last());
        
        sortedVms.remove(vm5);
        
        Assert.assertEquals(vm3, sortedVms.first());
        Assert.assertEquals(vm4, sortedVms.last());
        
        sortedVms.remove(vm3);
        
        Assert.assertEquals(vm2, sortedVms.first());
        Assert.assertEquals(vm4, sortedVms.last());
        
        sortedVms.remove(vm4);
        
        Assert.assertEquals(vm2, sortedVms.first());
        Assert.assertEquals(vm1, sortedVms.last());
        
        sortedVms.remove(vm2);
        sortedVms.remove(vm1);
        
        Assert.assertEquals(vm6, sortedVms.first());
        Assert.assertEquals(vm0, sortedVms.last());
        
        sortedVms.remove(vm0);
        
        Assert.assertEquals(vm6, sortedVms.first());
        Assert.assertEquals(vm6, sortedVms.last());
    }
}
