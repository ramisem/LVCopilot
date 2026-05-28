/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFunctions {
    public static String getString(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }

    public static String leftTrim(String givenString) {
        if (givenString == null) {
            return "";
        }
        for (int i = 0; i < givenString.length(); ++i) {
            if (givenString.charAt(i) == ' ') continue;
            return givenString.substring(i);
        }
        return "";
    }

    public static String rightTrim(String givenString) {
        if (givenString == null) {
            return "";
        }
        for (int i = givenString.length() - 1; i >= 0; --i) {
            if (givenString.charAt(i) == ' ') continue;
            return givenString.substring(0, ++i);
        }
        return "";
    }

    public static String trim(String givenString) {
        if (givenString == null) {
            return null;
        }
        return givenString.trim();
    }

    public static String left(String givenString, int length) {
        if (givenString == null) {
            return null;
        }
        if (length >= givenString.length()) {
            return givenString;
        }
        return givenString.substring(0, length);
    }

    public static String right(String givenString, int length) {
        if (givenString == null) {
            return null;
        }
        if (length >= givenString.length()) {
            return givenString;
        }
        return givenString.substring(givenString.length() - length);
    }

    public static String fill(String givenString, int fillLength) {
        if (givenString == null || givenString.length() == 0) {
            return "";
        }
        int length = givenString.length();
        int iteration = fillLength / length;
        int leftOver = fillLength - iteration * length;
        StringBuffer resultBuffer = new StringBuffer();
        for (int i = 0; i < iteration; ++i) {
            resultBuffer.append(givenString);
        }
        if (leftOver > 0) {
            resultBuffer.append(givenString.substring(0, leftOver));
        }
        return resultBuffer.toString();
    }

    public static int len(String givenString) {
        if (givenString == null) {
            return 0;
        }
        return givenString.length();
    }

    public static String lower(String givenString) {
        if (givenString == null) {
            return null;
        }
        return givenString.toLowerCase();
    }

    public static boolean match(String givenString, String pattern) {
        if (givenString == null || givenString.length() == 0 || pattern == null || pattern.length() == 0) {
            return false;
        }
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(givenString);
        return m.matches();
    }

    public static String mid(String givenString, int start, int end) {
        if (givenString == null) {
            return null;
        }
        if (start - 1 >= givenString.length() || start - 1 < 0 || start - 1 > end - 1 || start + end - 2 >= givenString.length()) {
            return "";
        }
        int calculatedEnd = start - 1 + end;
        if (calculatedEnd >= givenString.length()) {
            return givenString.substring(start - 1);
        }
        return givenString.substring(start - 1, calculatedEnd);
    }

    public static String mid(String givenString, int start) {
        if (givenString == null || givenString.length() == 0) {
            return null;
        }
        if (start - 1 >= givenString.length() || start - 1 < 0) {
            return "";
        }
        return givenString.substring(start - 1);
    }

    public static int pos(String givenString, String findStringPosition) {
        if (givenString == null) {
            return -1;
        }
        return givenString.indexOf(findStringPosition);
    }

    public static String replace(String givenString, String stringToReplace, String newString) {
        if (givenString == null) {
            return null;
        }
        if (stringToReplace == null) {
            stringToReplace = "";
        }
        if (newString == null) {
            newString = "";
        }
        return givenString.replaceAll(stringToReplace, newString);
    }

    public static String space(int noOfSpace) {
        StringBuffer spaceStringBuffer = new StringBuffer();
        if (noOfSpace <= 0 || noOfSpace > 999999999) {
            noOfSpace = 1;
        }
        for (int i = 0; i < noOfSpace; ++i) {
            spaceStringBuffer.append(" ");
        }
        return spaceStringBuffer.toString();
    }

    public static String upper(String givenString) {
        if (givenString == null) {
            return null;
        }
        return givenString.toUpperCase();
    }

    public static String wordcap(String givenString) {
        if (givenString == null || givenString.length() == 0) {
            return "";
        }
        givenString = givenString.toLowerCase();
        char[] charArray = givenString.toCharArray();
        for (int i = 0; i < charArray.length; ++i) {
            if (i != 0 && charArray[i - 1] != ' ' || charArray[i] < 'a' || charArray[i] > 'z') continue;
            int n = i;
            charArray[n] = (char)(charArray[n] - 32);
        }
        return new String(charArray);
    }
}

