/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.ora;

import sapphire.util.SafeSQL;

public class Coc {
    static String LABVANTAGE_CVS_ID = "$Revision: 53237 $";

    public static String getCertifiedUsers() {
        StringBuffer sql = new StringBuffer();
        sql.append("select SYSUSERID");
        sql.append(" from sysuser");
        sql.append(" where EXISTS (SELECT 1");
        sql.append(" FROM S_SDICERTIFICATION C2");
        sql.append(" WHERE C2.RESOURCESDCID = 'User'");
        sql.append(" AND C2.RESOURCEKEYID1 = SYSUSER.SYSUSERID");
        sql.append(" AND C2.CERTIFICATIONTYPE = 'Control Substance'");
        sql.append(" AND C2.CERTIFICATIONSTATUS IN ('Valid', 'In Training')");
        sql.append(" AND (C2.EXPIRATIONDT IS NULL OR (SYSDATE < DECODE(C2.GRACEPERIODUNITS,");
        sql.append(" 'Days', C2.EXPIRATIONDT+NVL(C2.GRACEPERIOD,0),");
        sql.append(" 'Weeks', C2.EXPIRATIONDT+7*NVL(C2.GRACEPERIOD,0) ,");
        sql.append(" 'Months', ADD_MONTHS(C2.EXPIRATIONDT, NVL(C2.GRACEPERIOD,0)) ,");
        sql.append(" 'Years', ADD_MONTHS(C2.EXPIRATIONDT, 12*NVL(C2.GRACEPERIOD,0)) ,");
        sql.append(" C2.EXPIRATIONDT)))) ORDER BY SYSUSERID");
        return sql.toString();
    }

    public static String getUserCustodianInfo() {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT 'SYSUSER' TYPE, SYSUSER.SYSUSERID ID, NVL( SYSUSER.DISABLEDFLAG, 'N') FLAG FROM SYSUSER");
        sql.append(" UNION");
        sql.append(" SELECT 'CUSTODIAN' TYPE, CUSTODIAN.CUSTODIANID ID, NVL( CUSTODIAN.PASSWORDFLAG, 'N') FLAG FROM CUSTODIAN");
        return sql.toString();
    }

    public static String getSdiCocDetails(String keyid1, String tableid, String sdcid, String keycolid1) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT T1.S_SDICOCID, T1.KEYID1, T1.FROMCUSTODIANID,");
        sql.append(" T1.TOCUSTODIANID, NVL( T3.CONTROLSUBSTANCEFLAG, 'N' ) CONTROLSUBSTANCEFLAG");
        sql.append(" FROM S_SDICOC T1, " + tableid + " T3");
        sql.append(" WHERE T1.SDCID = '" + sdcid + "'");
        sql.append(" AND T1.KEYID1 IN ( SELECT T4." + keycolid1 + " FROM " + tableid);
        sql.append(" T4 WHERE T4.COCREQUIREDFLAG = 'Y' AND T4." + keycolid1 + " IN ('" + keyid1 + "' ) )");
        sql.append(" AND T1.S_SDICOCID = ( SELECT MAX(T2.S_SDICOCID) FROM");
        sql.append(" S_SDICOC T2 WHERE T2.SDCID = T1.SDCID AND T2.KEYID1 = T1.KEYID1 )");
        sql.append(" AND T3." + keycolid1 + " = T1.KEYID1");
        return sql.toString();
    }

    public static SafeSQL getCustodianAndUserInfo(String userid) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT SYSUSER.SYSUSERID ID, 'SYSUSER' TYPE, NVL( SYSUSER.DISABLEDFLAG, 'N' ) DISABLEDFLAG");
        sql.append(" FROM SYSUSER WHERE SYSUSERID = " + safeSQL.addVar(userid));
        sql.append(" UNION");
        sql.append(" SELECT CUSTODIAN.CUSTODIANID ID, 'CUSTODIAN' TYPE, NVL( CUSTODIAN.PASSWORDFLAG, 'N' ) PASSWORDFLAG");
        sql.append(" FROM CUSTODIAN WHERE CUSTODIANID = " + safeSQL.addVar(userid));
        sql.append(" ORDER BY TYPE DESC");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    public static String getCocRequiredMap(String keyid1, String tableid, String keycolid1, String excludekeys) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT " + keycolid1 + ", CONTROLSUBSTANCEFLAG FROM " + tableid);
        sb.append(" WHERE COCREQUIREDFLAG = 'Y'");
        sb.append(" AND " + keycolid1 + " IN ('" + keyid1 + "')");
        if (excludekeys.length() > 0) {
            String key = excludekeys.substring(0, excludekeys.length() - 3);
            sb.append(" AND " + keycolid1 + " NOT IN ( '" + key + "' )");
        }
        sb.append(" ORDER BY " + keycolid1);
        return sb.toString();
    }
}

