/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class RuleUtil {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public static String getStringList(Collection items, String separator) {
        StringBuffer out = new StringBuffer();
        for (String item : items) {
            out.append(separator).append(item);
        }
        return out.length() == 0 ? "" : out.substring(separator.length());
    }

    public static List getList(DataSet ds, String columnid) {
        return Arrays.asList(StringUtil.split(ds.getColumnValues(columnid, ";"), ";"));
    }
}

