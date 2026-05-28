/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import java.util.Comparator;
import java.util.HashMap;

public class DataSetAlphanumericSorter
implements Comparator {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    String sortColumn;

    public DataSetAlphanumericSorter(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public int compare(Object firstObjToCompare, Object secondObjToCompare) {
        HashMap hm1 = (HashMap)firstObjToCompare;
        HashMap hm2 = (HashMap)secondObjToCompare;
        String firstString = (String)hm1.get(this.sortColumn);
        String secondString = (String)hm2.get(this.sortColumn);
        if (secondString == null || firstString == null) {
            return 0;
        }
        int lengthFirstStr = firstString.length();
        int lengthSecondStr = secondString.length();
        int index1 = 0;
        int index2 = 0;
        while (index1 < lengthFirstStr && index2 < lengthSecondStr) {
            int result;
            char ch1 = firstString.charAt(index1);
            char ch2 = secondString.charAt(index2);
            char[] space1 = new char[lengthFirstStr];
            char[] space2 = new char[lengthSecondStr];
            int loc1 = 0;
            int loc2 = 0;
            do {
                space1[loc1++] = ch1;
            } while (++index1 < lengthFirstStr && Character.isDigit(ch1 = firstString.charAt(index1)) == Character.isDigit(space1[0]));
            do {
                space2[loc2++] = ch2;
            } while (++index2 < lengthSecondStr && Character.isDigit(ch2 = secondString.charAt(index2)) == Character.isDigit(space2[0]));
            String str1 = new String(space1);
            String str2 = new String(space2);
            if (Character.isDigit(space1[0]) && Character.isDigit(space2[0])) {
                Integer firstNumberToCompare = new Integer(Integer.parseInt(str1.trim()));
                Integer secondNumberToCompare = new Integer(Integer.parseInt(str2.trim()));
                result = firstNumberToCompare.compareTo(secondNumberToCompare);
            } else {
                result = str1.compareTo(str2);
            }
            if (result == 0) continue;
            return result;
        }
        return lengthFirstStr - lengthSecondStr;
    }
}

