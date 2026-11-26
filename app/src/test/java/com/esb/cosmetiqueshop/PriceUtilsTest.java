package com.esb.cosmetiqueshop;

import com.esb.cosmetiqueshop.utils.PriceUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PriceUtilsTest {

    @Test
    public void formatMillimes_threeDecimals() {
        assertEquals("12.500 DT", PriceUtils.formatMillimes(12500));
        assertEquals("0.000 DT", PriceUtils.formatMillimes(0));
    }

    @Test
    public void dtToMillimes_roundTrip() {
        assertEquals(150000, PriceUtils.dtToMillimes(150.0));
    }
}