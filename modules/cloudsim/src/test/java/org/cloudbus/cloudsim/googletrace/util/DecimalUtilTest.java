package org.cloudbus.cloudsim.googletrace.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by João Victor Mafra and Alessandro Lia Fook on 23/09/16.
 */
public class DecimalUtilTest {

    private static final int DECIMAL_ACCURACY = 15;
    private static final double ACCEPTABLE_DIFERENCE = 0.0000000000000000000000000000000000001;

    @Test
    public void testFormat1(){
        double NUMBER = 0.53999999999999999997810;
        // between 2 and 19, the result must be 0.54
        for (int i = 2; i <= 19; i++){
            Assert.assertEquals(0.54, DecimalUtil.format(NUMBER, i), ACCEPTABLE_DIFERENCE);
        }

        // accuracy 1
        Assert.assertEquals(0.5, DecimalUtil.format(NUMBER, DECIMAL_ACCURACY - 14), ACCEPTABLE_DIFERENCE);

        // accuracy 0
        Assert.assertEquals(1, DecimalUtil.format(NUMBER, DECIMAL_ACCURACY - 15), ACCEPTABLE_DIFERENCE);

        // accuracy 20
        Assert.assertEquals(0.5399999999999999999980, DecimalUtil.format(NUMBER, DECIMAL_ACCURACY + 5), ACCEPTABLE_DIFERENCE);

        // accuracy 21
        Assert.assertEquals(0.53999999999999999999790, DecimalUtil.format(NUMBER, DECIMAL_ACCURACY + 6), ACCEPTABLE_DIFERENCE);

        // accuracy 22
        Assert.assertEquals(0.53999999999999999999780, DecimalUtil.format(NUMBER, DECIMAL_ACCURACY), ACCEPTABLE_DIFERENCE);

        System.out.println(DecimalUtil.format(NUMBER, DECIMAL_ACCURACY));
        Assert.assertEquals(0.54, 0.53999999999999999999780, ACCEPTABLE_DIFERENCE);
        System.out.println(0.54 -  0.53999999999999994444780);

        // accuracy 23
        Assert.assertEquals(0.53999999999999999999781, DecimalUtil.format(NUMBER, DECIMAL_ACCURACY + 8), ACCEPTABLE_DIFERENCE);
    }

    @Test
    public void testFormat2(){
        double NUMBER = 0.53999999999999999997810;

    }

}
