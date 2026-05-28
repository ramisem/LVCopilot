/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.mss;

import sapphire.util.SafeSQL;

public class Query {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54139 $";

    public static SafeSQL getQueryAndArgDetails(String queryid, String basedOnSdcId) {
        return com.labvantage.opal.sql.ora.Query.getQueryAndArgDetails(queryid, basedOnSdcId);
    }

    public static SafeSQL getQueryAndArgDetails2(String queryid, String basedOnSdcId) {
        StringBuffer sbSql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbSql.append("SELECT q.selectclause, q.fromclause, IsNull (q.whereclause,'') whereclause,");
        sbSql.append("  IsNull (q.orderbyclause, '') orderbyclause, qa.arginto ");
        sbSql.append("FROM query q LEFT OUTER JOIN queryarg qa ");
        sbSql.append("ON q.queryid = qa.queryid ");
        sbSql.append("AND q.basedonid = qa.basedonid ");
        sbSql.append("WHERE q.queryid = " + safeSQL.addVar(queryid) + " ");
        sbSql.append("AND q.basedonid = " + safeSQL.addVar(basedOnSdcId) + " ");
        sbSql.append("ORDER BY qa.usersequence");
        safeSQL.setPreparedSQL(sbSql.toString());
        return safeSQL;
    }

    public static SafeSQL getQueryDetails(String queryid) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT T1.SELECTCLAUSE, T1.FROMCLAUSE, ISNULL(T1.WHERECLAUSE, '') WHERECLAUSE,");
        sql.append(" ISNULL(T1.ORDERBYCLAUSE, '') ORDERBYCLAUSE");
        sql.append(" FROM QUERY T1");
        sql.append(" WHERE T1.QUERYID = " + safeSQL.addVar(queryid));
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }
}

