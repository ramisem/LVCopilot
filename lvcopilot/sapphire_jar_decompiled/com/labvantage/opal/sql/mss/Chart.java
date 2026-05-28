/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.mss;

import sapphire.util.SafeSQL;

public class Chart {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54171 $";

    public static SafeSQL getSdiDataitemForTypeAndDs(String dataset, String paramType) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT T1.KEYID1, T1.PARAMID, T1.ENTEREDVALUE,");
        sql.append(" T1.PARAMTYPE, T1.REPLICATEID, T1.DISPLAYVALUE, T1.TRANSFORMVALUE, T1.S_QCEVALSTATUS");
        sql.append(" FROM SDIDATAITEM T1");
        sql.append(" WHERE T1.KEYID1 + T1.PARAMLISTID + CAST( T1.PARAMLISTVERSIONID AS VARCHAR )");
        sql.append(" + T1.VARIANTID + CAST( T1.DATASET AS VARCHAR ) IN (");
        sql.append(safeSQL.addIn(dataset));
        sql.append(") AND T1.PARAMID + T1.PARAMTYPE IN ( " + safeSQL.addIn(paramType) + " )");
        safeSQL.setPreparedSQL(sql.toString());
        return safeSQL;
    }
}

