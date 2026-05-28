/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.ora;

import sapphire.util.SafeSQL;

public class StorageUnit {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 54170 $";

    public static SafeSQL getStorageUnitHierarchySql(String storageUnit) {
        StringBuffer sqlStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlStmt.append("SELECT * ");
        sqlStmt.append("FROM STORAGEUNIT ");
        sqlStmt.append("CONNECT BY PRIOR STORAGEUNITID = PARENTID ");
        sqlStmt.append("START WITH STORAGEUNITID = ");
        sqlStmt.append(safeSQL.addVar(storageUnit));
        sqlStmt.append(" ");
        sqlStmt.append("ORDER BY LEVEL");
        safeSQL.setPreparedSQL(sqlStmt.toString());
        return safeSQL;
    }

    public static String getStorageUnitAncestorSyncSql(String storageUnitId) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT STORAGEUNITID, PARENTID, STORAGEENVID, LEVEL");
        sqlStmt.append(" FROM STORAGEUNIT");
        sqlStmt.append(" WHERE STORAGEENVID IS NOT NULL AND STORAGEENVID <> '(null)' AND ROWNUM = 1");
        sqlStmt.append(" CONNECT BY PRIOR PARENTID = STORAGEUNITID");
        sqlStmt.append(" START WITH STORAGEUNITID = '").append(storageUnitId).append("'");
        sqlStmt.append(" ORDER BY LEVEL");
        return sqlStmt.toString();
    }

    public static String getStorageUnitAncestorInfoSql(String storageUnitId) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT STORAGEUNITID, PARENTID, NVL( STORAGEENVID, '(null)' ) STORAGEENVID,");
        sqlStmt.append(" NVL( ANCESTORID, '(null)' ) ANCESTORID");
        sqlStmt.append(" FROM STORAGEUNIT");
        sqlStmt.append(" WHERE STORAGEUNITID = '").append(storageUnitId).append("'");
        return sqlStmt.toString();
    }

    public static String getStorageUnitChildrenAncestorSyncSql(String storageUnitId) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT STORAGEUNITID, STORAGEENVID, LEVEL");
        sqlStmt.append(" FROM STORAGEUNIT");
        sqlStmt.append(" WHERE STORAGEENVID IS NULL OR STORAGEENVID = '(null)'");
        sqlStmt.append(" CONNECT BY PRIOR STORAGEUNITID = PARENTID");
        sqlStmt.append(" START WITH PARENTID = '").append(storageUnitId).append("'");
        return sqlStmt.toString();
    }

    public static String getStorageUnitAndChildrensLabelPathSql(String storageUnitId) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("select t1.storageunitid,  ");
        sqlStmt.append("(select sys_connect_by_path(t2.storageunitlabel, '/') labelpath ");
        sqlStmt.append("from storageunit t2  ");
        sqlStmt.append("where t2.storageunitid = t1.storageunitid ");
        sqlStmt.append("connect by prior t2.storageunitid = t2.parentid ");
        sqlStmt.append("start with parentid is null ) labelpath ");
        sqlStmt.append("from storageunit t1 ");
        sqlStmt.append("connect by prior t1.storageunitid = t1.parentid ");
        sqlStmt.append("start with t1.storageunitid = '" + storageUnitId + "' ");
        return sqlStmt.toString();
    }

    public static SafeSQL getStorageEnvCondTypesSql(String keyid1) {
        StringBuffer sqlStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlStmt.append("SELECT sc.storagecondtypeid, se.storageenvid, ");
        sqlStmt.append("  se.storagecondid, sc.datatypeflag, sc.defaultunits, ");
        sqlStmt.append("  sc.condreftype, sc.condsdcid, sc.whereclause, ");
        sqlStmt.append("  se.condvalue, se.condunits, se.condtext, se.upperlimit, ");
        sqlStmt.append("  se.lowerlimit ");
        sqlStmt.append("FROM storagecondtype sc LEFT OUTER JOIN storageenvcond se ");
        sqlStmt.append("ON sc.storagecondtypeid = se.storagecondtypeid ");
        sqlStmt.append(" AND se.STORAGEENVID = ").append(safeSQL.addVar(keyid1)).append(" ");
        sqlStmt.append("ORDER BY sc.storagecondtypeid ");
        safeSQL.setPreparedSQL(sqlStmt.toString());
        return safeSQL;
    }
}

