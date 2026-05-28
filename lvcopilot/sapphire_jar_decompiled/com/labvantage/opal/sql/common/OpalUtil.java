/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

import sapphire.util.SafeSQL;

public class OpalUtil {
    private String LABVANTAGE_CVS_ID = "$Revision: 54136 $";

    public static SafeSQL getKeysFromRSetItemsSQLStmt(String rsetid) {
        StringBuffer sqlStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlStmt.append("SELECT SDCID, KEYID1, KEYID2, KEYID3  ");
        sqlStmt.append("FROM RSETITEMS  ");
        sqlStmt.append("WHERE RSETID = " + safeSQL.addVar(rsetid) + "  ");
        safeSQL.setPreparedSQL(sqlStmt.toString());
        return safeSQL;
    }
}

