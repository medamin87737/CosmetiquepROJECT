package com.esb.cosmetiqueshop.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private static final SimpleDateFormat DISPLAY =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);

    private DateUtils() {
    }

    public static String formatTimestamp(long millis) {
        return DISPLAY.format(new Date(millis));
    }

    public static String formatDisplay(long millis) {
        return formatTimestamp(millis);
    }

    public static long now() {
        return System.currentTimeMillis();
    }
}