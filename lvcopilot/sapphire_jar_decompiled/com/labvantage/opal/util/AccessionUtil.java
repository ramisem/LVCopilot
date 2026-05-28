/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import java.util.HashSet;
import java.util.Set;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class AccessionUtil {
    public static Set<String> getInactiveRoles(QueryProcessor qp) {
        HashSet<String> inactiveRoleList = new HashSet<String>();
        DataSet ds = qp.getSqlDataSet("SELECT roleid FROM role WHERE activeflag='N' order by roleid");
        for (int i = 0; i < ds.getRowCount(); ++i) {
            inactiveRoleList.add(ds.getString(i, "roleid"));
        }
        return inactiveRoleList;
    }
}

