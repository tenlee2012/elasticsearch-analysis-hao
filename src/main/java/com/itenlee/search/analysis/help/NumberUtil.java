package com.itenlee.search.analysis.help;

/**
 * @author tenlee
 * @date 2020/6/23
 */
public class NumberUtil {
    static public Integer parseInt(String s, Integer defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    static public Integer parseInt(String s) {
        return parseInt(s, null);
    }
}
