package org.cloudbus.cloudsim.preemption.util;

import java.math.BigDecimal;

/**
 * Created by alessandro.fook on 23/09/16.
 */
public class DecimalUtil {

    public static double format(double value, int decimal_accuracy){
        BigDecimal result = new BigDecimal(value);
        result = result.setScale(decimal_accuracy, BigDecimal.ROUND_HALF_UP);
        return result.doubleValue();
    }

}
