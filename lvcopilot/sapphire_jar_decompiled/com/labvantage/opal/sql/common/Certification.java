/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

import sapphire.util.SafeSQL;

public class Certification {
    private String LABVANTAGE_CVS_ID = "$Revision: 53247 $";

    public static SafeSQL getSampleDetails(String sampleIdList) {
        StringBuffer sbSQLStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbSQLStmt.append("SELECT S_SAMPLEID, CONTROLSUBSTANCEFLAG  ");
        sbSQLStmt.append("FROM S_SAMPLE  ");
        sbSQLStmt.append("WHERE CONTROLSUBSTANCEFLAG = 'Y' ");
        sbSQLStmt.append("AND S_SAMPLEID IN ( " + safeSQL.addIn(sampleIdList) + " ) ");
        safeSQL.setPreparedSQL(sbSQLStmt.toString());
        return safeSQL;
    }

    public static SafeSQL getParamlistDetails(String paramlistIdList) {
        StringBuffer sbSQLStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbSQLStmt.append("SELECT PARAMLISTID, PARAMLISTVERSIONID, VARIANTID, VERSIONSTATUS, ");
        sbSQLStmt.append("S_TRAININGREQFLAG, S_OVERRIDEALLOWEDFLAG, S_INSTRUMENTTYPE ");
        sbSQLStmt.append("FROM PARAMLIST ");
        sbSQLStmt.append("WHERE PARAMLISTID IN ( " + safeSQL.addIn(paramlistIdList) + " ) ");
        safeSQL.setPreparedSQL(sbSQLStmt.toString());
        return safeSQL;
    }

    public static SafeSQL getInstrumentDetails(String instrumentIdList) {
        StringBuffer sbSQLStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbSQLStmt.append("SELECT INSTRUMENTID, INSTRUMENTTYPE, OVERRIDEALLOWEDFLAG  ");
        sbSQLStmt.append("FROM INSTRUMENT  ");
        sbSQLStmt.append("WHERE INSTRUMENTID IN ( " + safeSQL.addIn(instrumentIdList) + " ) ");
        safeSQL.setPreparedSQL(sbSQLStmt.toString());
        return safeSQL;
    }
}

