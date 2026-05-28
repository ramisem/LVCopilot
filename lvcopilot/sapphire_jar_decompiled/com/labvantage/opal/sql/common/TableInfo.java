/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

import sapphire.util.SafeSQL;

public class TableInfo {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 54146 $";

    public static SafeSQL getTableSDCSQL(String tableid) {
        StringBuffer sqlStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlStmt.append("SELECT TABLEID, SDCID ");
        sqlStmt.append("FROM SDC ");
        sqlStmt.append("WHERE TABLEID = " + safeSQL.addVar(tableid) + " ");
        safeSQL.setPreparedSQL(sqlStmt.toString());
        return safeSQL;
    }

    public static SafeSQL getTableKeysSQL(String tableid) {
        StringBuffer sqlStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlStmt.append("SELECT T1.COLUMNID ");
        sqlStmt.append("FROM SYSREFCOLUMN T1, SYSREF T2 ");
        sqlStmt.append("WHERE T2.TABLEID = " + safeSQL.addVar(tableid) + " ");
        sqlStmt.append("AND T2.REFTYPEFLAG = 'P' ");
        sqlStmt.append("AND T1.REFID = T2.REFID ");
        sqlStmt.append("ORDER BY T1.COLUMNSEQUENCE ");
        safeSQL.setPreparedSQL(sqlStmt.toString());
        return safeSQL;
    }
}

