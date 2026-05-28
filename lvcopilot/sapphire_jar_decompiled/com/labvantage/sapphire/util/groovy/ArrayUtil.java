/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.groovy;

import sapphire.util.StringUtil;

public class ArrayUtil {
    public static final String DEFAULT_SEPARATOR = ";";

    public static String positionToArrayItemId(String arrayid, String positions, int numOfRows, int numOfCols, String direction) {
        String[] positionArr = StringUtil.split(positions, DEFAULT_SEPARATOR);
        String arrayitemids = "";
        for (int i = 0; i < positionArr.length; ++i) {
            int colnum;
            int rownum;
            int currentPos = Integer.parseInt(positionArr[i]) - 1;
            if ("H".equals(direction)) {
                rownum = currentPos / numOfCols;
                colnum = currentPos % numOfCols;
                if (arrayitemids.length() > 0) {
                    arrayitemids = arrayitemids + DEFAULT_SEPARATOR;
                }
                arrayitemids = arrayitemids + arrayid + "_" + rownum + "_" + colnum;
                continue;
            }
            if ("V".equals(direction)) {
                rownum = currentPos % numOfRows;
                colnum = currentPos / numOfRows;
                if (arrayitemids.length() > 0) {
                    arrayitemids = arrayitemids + DEFAULT_SEPARATOR;
                }
                arrayitemids = arrayitemids + arrayid + "_" + rownum + "_" + colnum;
                continue;
            }
            if ("HS".equals(direction)) {
                rownum = currentPos / numOfCols;
                colnum = currentPos % numOfCols;
                if (rownum % 2 == 1) {
                    colnum = numOfCols - colnum - 1;
                }
                if (arrayitemids.length() > 0) {
                    arrayitemids = arrayitemids + DEFAULT_SEPARATOR;
                }
                arrayitemids = arrayitemids + arrayid + "_" + rownum + "_" + colnum;
                continue;
            }
            if (!"VS".equals(direction)) continue;
            rownum = currentPos % numOfRows;
            colnum = currentPos / numOfRows;
            if (colnum % 2 == 1) {
                rownum = numOfRows - rownum - 1;
            }
            if (arrayitemids.length() > 0) {
                arrayitemids = arrayitemids + DEFAULT_SEPARATOR;
            }
            arrayitemids = arrayitemids + arrayid + "_" + rownum + "_" + colnum;
        }
        return arrayitemids;
    }
}

