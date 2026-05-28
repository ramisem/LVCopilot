/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.common;

public class StorageUnit {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public static String getDeleteStorageEnvCondSql(String keyid1) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("DELETE FROM STORAGEENVCOND ");
        sqlStmt.append("WHERE STORAGEENVID = '").append(keyid1).append("'");
        return sqlStmt.toString();
    }

    public static String getUnitsSql() {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT UNITSID FROM UNITS ORDER BY UNITSID");
        return sqlStmt.toString();
    }

    public static String getRefTypeSql() {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT REFTYPEID FROM REFTYPE ORDER BY REFTYPEID");
        return sqlStmt.toString();
    }
}

