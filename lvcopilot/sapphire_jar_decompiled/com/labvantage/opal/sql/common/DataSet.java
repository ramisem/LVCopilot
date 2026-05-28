/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

public class DataSet {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public static String getIsDatasetMandatorySql(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT MODIFIABLEFLAG FROM SDIDATA WHERE ");
        sql.append(" SDCID = '" + sdcid + "'");
        sql.append(" AND KEYID1 = '" + keyid1 + "'");
        sql.append(" AND KEYID2 = '" + keyid2 + "'");
        sql.append(" AND KEYID3 = '" + keyid3 + "'");
        sql.append(" AND PARAMLISTID = '" + paramlistid + "'");
        sql.append(" AND PARAMLISTVERSIONID = '" + paramlistversionid + "'");
        sql.append(" AND VARIANTID = '" + variantid + "'");
        sql.append(" AND DATASET = '" + dataset + "'");
        return sql.toString();
    }

    public static String getDoesDataitemExistsSql(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT PARAMID FROM SDIDATAITEM ");
        sql.append(" WHERE SDCID = '" + sdcid + "'");
        sql.append(" AND KEYID1 = '" + keyid1 + "'");
        sql.append(" AND KEYID2 = '" + keyid2 + "'");
        sql.append(" AND KEYID3 = '" + keyid3 + "'");
        sql.append(" AND PARAMLISTID = '" + paramlistid + "'");
        sql.append(" AND PARAMLISTVERSIONID = '" + paramlistversionid + "'");
        sql.append(" AND VARIANTID = '" + variantid + "'");
        sql.append(" AND DATASET = '" + dataset + "'");
        sql.append(" AND PARAMID = '" + paramid + "'");
        sql.append(" AND PARAMTYPE = '" + paramtype + "'");
        return sql.toString();
    }
}

