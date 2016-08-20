package me.gong.lavarun.plugin.util;

import net.md_5.bungee.api.ChatColor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class StringUtils {


    public static <T> String concat(List<T> objects) {
        return concat(objects, 0);
    }

    public static <T> String concat(List<T> objects, int beginIndex) {
        return concat(objects, " ", beginIndex);
    }

    public static <T> String concat(List<T> objects, String separator) {
        return concat(objects, separator, 0);
    }

    public static <T> String concat(List<T> objects, String separator, int beginIndex) {
        return concat(objects.toArray(new Object[objects.size()]), separator, beginIndex);
    }

    public static <T> String concat(T[] objects) {
        return concat(objects, 0);
    }

    public static <T> String concat(T[] objects, int beginIndex) {
        return concat(objects, " ", beginIndex);
    }

    public static <T> String concat(T[] objects, String separator) {
        return concat(objects, separator, 0);
    }

    public static <T> String concat(T[] objects, String separator, int beginIndex) {
        if (objects.length == 0) {
            return "";
        }

        int index = -1;
        StringBuilder ret = new StringBuilder();

        for (Object s : objects) {
            index++;

            if (index >= beginIndex) if (s.toString().length() > 0) {
                ret.append(separator).append(s);
            }
        }

        return ret.length() >= separator.length() ? ret.substring(separator.length()) : ret.toString();
    }

    public static String concatGramar(List<String> strings) {
        if(strings.isEmpty() || strings.size() == 1) return strings.isEmpty() ? "" : strings.get(0);
        return concat(strings.subList(0, strings.size() - 2), ", ")+" and "+strings.get(strings.size()-1);
    }

    public static String capitalize(String s, String fullstring) {
        s = s.trim();

        if (fullstring == null || fullstring.isEmpty()) {
            fullstring = s;
        }

        if (fullstring.length() < 3) {
            return s.toUpperCase();
        }

        if (s.contains(" ")) {
            StringBuilder ret = new StringBuilder();

            for (String a : s.split(" ")) {
                ret.append(" ").append(capitalize(a, s));
            }

            return ret.toString();
        } else {
            if (s.contains(".")) {
                StringBuilder t = new StringBuilder();

                for (String b : s.split("\\.")) {
                    t.append(".").append(capitalize(b, s));
                }

                return t.substring(1);
            } else {
                return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
            }
        }
    }

    public static void printObject(Object o) {
        System.out.println("### " + o.getClass().getSimpleName() + ": ");

        for (Field f : o.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object oF = f.get(o);
                String s = f.get(o)+"";
                if(oF instanceof Object[]) s = Arrays.toString(((Object[]) oF));
                System.out.println("### -    " + f.getType().getSimpleName() + " " + f.getName() + ": " + s);
                f.setAccessible(false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("### END " + o.getClass().getSimpleName().toUpperCase() + "###");
    }

    public static String format(String score) {
        return ChatColor.translateAlternateColorCodes('&', score);
    }
}
