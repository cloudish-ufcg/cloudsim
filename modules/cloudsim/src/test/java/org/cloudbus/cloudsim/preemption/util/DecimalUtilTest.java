package org.cloudbus.cloudsim.preemption.util;

import org.cloudbus.cloudsim.preemption.util.DecimalUtil;
import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;

/**
 * Created by João Victor Mafra and Alessandro Lia Fook on 23/09/16.
 */
public class DecimalUtilTest {

    private static final double ACCEPTABLE_DIFERENCE = 0.0000000001;

    @Test
    public void testFormat1(){

        double NUMBER = 0.53999999710999999999999999999;

        Assert.assertEquals(0.539999997, DecimalUtil.format(NUMBER), ACCEPTABLE_DIFERENCE);

        //Asserting limit of round
        NUMBER = 0.539999999701010;

        Assert.assertEquals(0.54, DecimalUtil.format(NUMBER), ACCEPTABLE_DIFERENCE);

        //Asserting round half up
        NUMBER = 0.5399999995701010;

        Assert.assertEquals(0.54, DecimalUtil.format(NUMBER), ACCEPTABLE_DIFERENCE);

        //Asserting round hald down
        NUMBER = 0.5399999994701010;

        Assert.assertEquals(0.539999999, DecimalUtil.format(NUMBER), ACCEPTABLE_DIFERENCE);
    }
}
