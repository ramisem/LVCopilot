/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

public class CustomFunctions {
    public static double doubleIt(double n) {
        return n * 2.0;
    }

    public static String oneSpace() {
        return " ";
    }

    public static double customSum(Double[] nlist) {
        double sum = 0.0;
        if (nlist.length == 0) {
            return 1 / nlist.length;
        }
        for (int i = 0; i < nlist.length; ++i) {
            sum += nlist[i].doubleValue();
        }
        Double dsum = new Double(sum);
        return sum;
    }

    public static double customSum(Double n1, double n2) {
        double sum = n1 + n2;
        return sum;
    }

    public static String customMid(String givenString, int start, int length) {
        int calculatedStart = start - 1;
        int calculatedEnd = start + length - 1;
        if (givenString == null) {
            return null;
        }
        if (calculatedStart >= givenString.length() || calculatedStart < 0 || calculatedStart > calculatedEnd) {
            return "";
        }
        if (calculatedEnd >= givenString.length()) {
            return givenString.substring(calculatedStart);
        }
        return givenString.substring(calculatedStart, calculatedEnd);
    }
}

