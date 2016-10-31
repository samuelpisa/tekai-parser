package com.tekai.javaexpansions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

    private Strings(){}

    public static String coalesce(String... strings) {
        for (String string : strings) {
            if (string != null) return string;
        }
        return null;
    }

    public static String matches(String string, String regularExpression) {
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            if (matcher.groupCount() > 0) {
                return matcher.group(1);
            } else {
                return matcher.group();
            }
        } else {
            return "";
        }
    }
}
