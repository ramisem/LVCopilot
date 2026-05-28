/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.ora;

import sapphire.util.SafeSQL;

public class Certification {
    private String LABVANTAGE_CVS_ID = "$Revision: 53247 $";

    public static SafeSQL getUserCertificationDetails(String analystIdList, String paramlistIdList) {
        StringBuffer sbSQLStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbSQLStmt.append("SELECT CERTIFICATIONTYPE,RESOURCESDCID, RESOURCEKEYID1, RESOURCEKEYID2, ");
        sbSQLStmt.append("RESOURCEKEYID3, CERTIFIEDFORSDCID, CERTIFIEDFORKEYID1, CERTIFIEDFORKEYID2, ");
        sbSQLStmt.append("CERTIFIEDFORKEYID3  ");
        sbSQLStmt.append("FROM S_SDICERTIFICATION  ");
        sbSQLStmt.append("WHERE CERTIFICATIONSTATUS IN ('Valid', 'In Training') ");
        sbSQLStmt.append("AND RESOURCESDCID = 'User' ");
        sbSQLStmt.append("AND RESOURCEKEYID1 IN ( " + safeSQL.addIn(analystIdList) + " ) ");
        sbSQLStmt.append("AND ( EXPIRATIONDT IS NULL  ");
        sbSQLStmt.append("     OR  ( SYSDATE < DECODE(GRACEPERIODUNITS, 'Days', EXPIRATIONDT + NVL(GRACEPERIOD, 0),  ");
        sbSQLStmt.append("                                       'Weeks', EXPIRATIONDT + 7 * NVL(GRACEPERIOD, 0),  ");
        sbSQLStmt.append("                                       'Months', ADD_MONTHS(EXPIRATIONDT, NVL(GRACEPERIOD, 0)),  ");
        sbSQLStmt.append("                                       'Years', ADD_MONTHS(EXPIRATIONDT, 12 * NVL(GRACEPERIOD, 0)),  ");
        sbSQLStmt.append("                                       EXPIRATIONDT)  ");
        sbSQLStmt.append("            ) ");
        sbSQLStmt.append("     ) ");
        sbSQLStmt.append("AND ( CERTIFICATIONTYPE IN( 'Control Substance' ) ");
        sbSQLStmt.append(" OR (CERTIFICATIONTYPE IN( 'Analyst Training' ) ");
        sbSQLStmt.append(" AND CERTIFIEDFORSDCID = 'ParamList' ");
        sbSQLStmt.append(" AND CERTIFIEDFORKEYID1 IN ( " + safeSQL.addIn(paramlistIdList) + " )");
        sbSQLStmt.append(" ) ");
        sbSQLStmt.append(" ) ");
        safeSQL.setPreparedSQL(sbSQLStmt.toString());
        return safeSQL;
    }

    public static SafeSQL getInstrumentCertificationDetails(String instrumentIdList) {
        StringBuffer sbSQLStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sbSQLStmt.append("SELECT C.CERTIFICATIONTYPE,C.RESOURCESDCID, C.RESOURCEKEYID1,  ");
        sbSQLStmt.append("C.RESOURCEKEYID2, C.RESOURCEKEYID3, I.INSTRUMENTTYPE  ");
        sbSQLStmt.append("FROM S_SDICERTIFICATION C, INSTRUMENT I  ");
        sbSQLStmt.append("WHERE C.CERTIFICATIONSTATUS = 'Valid' ");
        sbSQLStmt.append("AND C.CERTIFICATIONTYPE = 'Instrument' ");
        sbSQLStmt.append("AND C.RESOURCESDCID = 'Instrument' ");
        sbSQLStmt.append("AND C.RESOURCEKEYID1 = I.INSTRUMENTID ");
        sbSQLStmt.append("AND ( C.EXPIRATIONDT IS NULL ");
        sbSQLStmt.append("    OR  (  SYSDATE < DECODE(C.GRACEPERIODUNITS, 'Days', C.EXPIRATIONDT + NVL(C.GRACEPERIOD, 0),  ");
        sbSQLStmt.append("                                         'Weeks', C.EXPIRATIONDT + 7 * NVL(C.GRACEPERIOD, 0),  ");
        sbSQLStmt.append("                                         'Months', ADD_MONTHS(C.EXPIRATIONDT, NVL(C.GRACEPERIOD, 0)),  ");
        sbSQLStmt.append("                                         'Years', ADD_MONTHS(C.EXPIRATIONDT, 12 * NVL(C.GRACEPERIOD, 0)),  ");
        sbSQLStmt.append("                                         C.EXPIRATIONDT)  ");
        sbSQLStmt.append("       ) ");
        sbSQLStmt.append("    ) ");
        sbSQLStmt.append("AND C.RESOURCEKEYID1 IN (" + safeSQL.addIn(instrumentIdList) + " ) ");
        safeSQL.setPreparedSQL(sbSQLStmt.toString());
        return safeSQL;
    }
}

