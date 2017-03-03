package org.cloudbus.cloudsim.preemption.util;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by alessandro.fook on 23/09/16.
 */
public class DecimalUtil {

    static {Locale.setDefault(Locale.ROOT);}

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#########");

    public static double format(double value){

        String truncatedValue = DECIMAL_FORMAT.format(value);
        double doubleValue = Double.parseDouble(truncatedValue);

        return doubleValue;
    }
}
