package org.cloudbus.cloudsim.preemption.util;

import java.util.Map;
import java.util.Properties;

import gnu.trove.map.hash.THashMap;

public class Utils {

	public static final String SLO_TARGET_PREFIX_PROP = "slo_availability_target_priority_";
	
	public static Map<Integer, Double> getSLOAvailabilityTargets(Properties properties) {
	    Map<Integer, Double> sloTargets = new THashMap<Integer, Double>();
	
	    if (properties == null) {
	        throw new IllegalArgumentException("The SLO availability target must be set for each priority");
	    }
	
	    for (Object objectKey : properties.keySet()) {
	        String key = objectKey.toString();
	        if (key.startsWith(Utils.SLO_TARGET_PREFIX_PROP)) {
	            try {
	                int priority = Integer.parseInt(key.replace(
	                        Utils.SLO_TARGET_PREFIX_PROP, ""));
	                double sloTarget = Double.parseDouble(properties
	                        .getProperty(key));
	
	                if (sloTarget < 0) {
	                    throw new IllegalArgumentException(
	                            "The SLO availability target must be a positive double.");
	                }
	
	                sloTargets.put(priority, sloTarget);
	            } catch (Exception e) {
	                throw new IllegalArgumentException(
	                        "The SLO availability target is not properly set for each priority");
	            }
	        }
	    }
	    return sloTargets;
	}

}
