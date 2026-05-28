/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

import com.labvantage.sapphire.SDI;
import sapphire.util.SafeSQL;

public class Workitem {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54148 $";

    public static SafeSQL getWorkitemDetails(String workitemid, String sdcid) {
        return Workitem.getWorkitemDetails(workitemid, "1", sdcid);
    }

    public static SafeSQL getWorkitemDetails(String workitemid, String workitemversionid, String sdcid) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT DISTINCT KEYID1, KEYID2, KEYID3");
        sql.append(" FROM WORKITEMITEM");
        sql.append(" WHERE WORKITEMID = " + safeSQL.addVar(workitemid) + " AND WORKITEMVERSIONID = " + safeSQL.addVar(workitemversionid) + " ");
        sql.append(" AND SDCID = " + safeSQL.addVar(sdcid));
        sql.append(" ORDER BY KEYID1, KEYID2, KEYID3");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    public static SafeSQL getSdiWorkitemDetails(SDI sdi, String workitemid, String workiteminstance, String itemsdcid) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT DISTINCT ITEMKEYID1, ITEMKEYID2, ITEMKEYID3, ITEMINSTANCE");
        sql.append(" FROM SDIWORKITEMITEM");
        sql.append(" WHERE WORKITEMID = " + safeSQL.addVar(workitemid));
        sql.append(" AND WORKITEMINSTANCE = " + safeSQL.addVar(workiteminstance));
        sql.append(" AND SDCID = " + safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND KEYID1 = " + safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND KEYID2 = " + safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND KEYID3 = " + safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" AND ITEMSDCID = " + safeSQL.addVar(itemsdcid));
        sql.append(" ORDER BY ITEMKEYID1, ITEMKEYID2, ITEMKEYID3");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }

    public static SafeSQL getSdiWorkitemDataSets(SDI sdi, String workitemid, String workiteminstance) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT DISTINCT PARAMLISTID, PARAMLISTVERSIONID, VARIANTID, DATASET");
        sql.append(" FROM SDIDATA");
        sql.append(" WHERE SDCID = " + safeSQL.addVar(sdi.getSdcid()));
        sql.append(" AND KEYID1 = " + safeSQL.addVar(sdi.getKeyid1()));
        sql.append(" AND KEYID2 = " + safeSQL.addVar(sdi.getKeyid2()));
        sql.append(" AND KEYID3 = " + safeSQL.addVar(sdi.getKeyid3()));
        sql.append(" AND SOURCEWORKITEMID = " + safeSQL.addVar(workitemid));
        sql.append(" AND SOURCEWORKITEMINSTANCE = " + safeSQL.addVar(workiteminstance));
        sql.append(" ORDER BY PARAMLISTID, PARAMLISTVERSIONID, VARIANTID");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }
}

