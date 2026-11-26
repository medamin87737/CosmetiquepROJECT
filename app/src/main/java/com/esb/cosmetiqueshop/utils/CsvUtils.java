package com.esb.cosmetiqueshop.utils;

/**
 * Échappement CSV + protection injection formules.
 */
public final class CsvUtils {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private CsvUtils() {
    }

    public static byte[] utf8Bom() {
        return UTF8_BOM;
    }

    public static String escapeCell(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw;
        if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-")
                || value.startsWith("@")) {
            value = "'" + value;
        }
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            value = "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}