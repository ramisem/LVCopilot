/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

import sapphire.util.SafeSQL;

public class Chart {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54170 $";

    public static SafeSQL getDataItems(String sampleid, String paramlistid, String paramlistversionid, String variantid, String dataset) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT PARAMID, PARAMTYPE, REPLICATEID");
        sql.append(" FROM SDIDATAITEM");
        sql.append(" WHERE KEYID1 = " + safeSQL.addVar(sampleid));
        sql.append(" AND PARAMLISTID = " + safeSQL.addVar(paramlistid));
        sql.append(" AND PARAMLISTVERSIONID = " + safeSQL.addVar(paramlistversionid));
        sql.append(" AND VARIANTID = " + safeSQL.addVar(variantid));
        sql.append(" AND DATASET = " + safeSQL.addVar(dataset));
        sql.append(" ORDER BY USERSEQUENCE");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    public static String getQueryArgsForTrendChartQuery() {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT T1.QUERYID, T1.ARGID, T1.ARGINTO, T1.USERSEQUENCE");
        sql.append(" FROM QUERYARG T1");
        sql.append(" WHERE QUERYID IN ( SELECT T2.KEYID1 FROM CATEGORYITEM T2");
        sql.append(" WHERE T2.CATEGORYID = 'TrendChart' )");
        sql.append(" ORDER BY T1.USERSEQUENCE");
        return sql.toString();
    }

    public static SafeSQL getQueryArgsForQuery(String queryid) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT ARGID, ARGINTO, USERSEQUENCE FROM QUERYARG");
        sql.append(" WHERE QUERYID = " + safeSQL.addVar(queryid));
        sql.append(" ORDER BY USERSEQUENCE");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }
}

