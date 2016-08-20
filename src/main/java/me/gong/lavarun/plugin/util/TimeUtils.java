package me.gong.lavarun.plugin.util;

public class TimeUtils {

    public static long fromBukkitTime(long bukkitTime) {
        return (bukkitTime * 50L);
    }

    public static long toBukkitTime(long millisTime) {
        return (millisTime / 50L);
    }

    public static boolean hasTimeElapsed(long initial, long elapsed) {
        return ((System.currentTimeMillis() - initial) >= elapsed);
    }

    public static long getTimeLeft(long initial, long future) {
        return ((initial + future) - System.currentTimeMillis());
    }

    public static String convertToString(long time) {
        return convertToString(time, TimeUnit.BEST);
    }

    public static double convert(long time) {
        return convert(time, TimeUnit.BEST);
    }

    public static String convertToString(long time, TimeUnit unit) {
        return convertToString(time, 0, unit);
    }

    public static double convert(long time, TimeUnit unit) {
        return convert(time, 0, unit);
    }

    public static String convertToString(long time, int trimLevel, TimeUnit unit) {
        return convertToString(time, trimLevel, unit, true);
    }

    public static double convert(long time, int trimLevel, TimeUnit unit) {
        return Double.valueOf(convertToString(time, trimLevel, unit, false));
    }

    public static String convertToString(long time, int trimLevel, TimeUnit unit, boolean stringValue) {
        if(time < 0) return (stringValue ? "Permanent" : -1 + "");

        if(unit == TimeUnit.BEST)
            /*if(time < 1000) unit = TimeUnit.MILLISECOND;
            else*/ if(time < 60000L) unit = TimeUnit.SECOND;
            else if(time < 3600000L) unit = TimeUnit.MINUTE;
            else if(time < 86400000L) unit = TimeUnit.HOUR;
            else if(time < 604800000L) unit = TimeUnit.DAY;
            else if(time < 18748800000L) unit = TimeUnit.WEEK;
            else if(time < 224985600000L) unit = TimeUnit.MONTH;
            else unit = TimeUnit.YEAR;

        double trimmedNumber = NumberUtils.trim(trimLevel, time / unit.getDivideBy());

        return (stringValue ? trimmedNumber + " " + unit.getStringValue() : String.valueOf(trimmedNumber));
    }

    public enum TimeUnit {

        YEAR("years", 224985600000.0D),
        MONTH("months", 18748800000.0D),
        WEEK("weeks", 604800000.0D),
        DAY("days", 86400000.0D),
        HOUR("hours", 3600000.0D),
        MINUTE("minutes", 60000.0D),
        SECOND("seconds", 1000.0D),
        MILLISECOND("milliseconds", 1.0D),
        BEST("ERROR", 0);

        private String value;
        private double divideBy;

        TimeUnit(String value, double divideBy) {
            this.value = value;
            this.divideBy = divideBy;
        }

        public String getStringValue() {
            return value;
        }

        public double getDivideBy() {
            return divideBy;
        }

    }

}
