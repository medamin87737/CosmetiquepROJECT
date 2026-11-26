package com.esb.cosmetiqueshop.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Prix en millimes → affichage DT (3 décimales).
 */
public final class PriceUtils {

    private static final DecimalFormat DT_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DT_FORMAT = new DecimalFormat("0.000", symbols);
    }

    private PriceUtils() {
    }

    public static String formatMillimes(int millimes) {
        double dt = millimes / 1000.0;
        return DT_FORMAT.format(dt) + " DT";
    }

    public static String formatMillimesNoSuffix(int millimes) {
        double dt = millimes / 1000.0;
        return DT_FORMAT.format(dt);
    }

    public static int dtToMillimes(double dt) {
        return (int) Math.round(dt * 1000.0);
    }
}