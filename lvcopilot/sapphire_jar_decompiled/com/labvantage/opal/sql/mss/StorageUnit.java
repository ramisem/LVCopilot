/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.sql.mss;

import sapphire.util.SafeSQL;

public class StorageUnit {
    private String LABVANTAGE_CVS_ID = "$Revision: 54171 $";

    public static SafeSQL getStorageUnitHierarchySql(String storageUnit) {
        StringBuffer sqlStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlStmt.append("SELECT STORAGEUNIT.* ");
        sqlStmt.append("FROM STORAGEUNIT, LV_ORA_GetSUTree(");
        sqlStmt.append(safeSQL.addVar(storageUnit)).append(",'sps',default, default ) t2 ");
        sqlStmt.append("WHERE storageunit.storageunitid = t2.storageunitid ");
        sqlStmt.append("ORDER BY T2.TLEVEL");
        safeSQL.setPreparedSQL(sqlStmt.toString());
        return safeSQL;
    }

    public static String getStorageUnitAncestorSyncSql(String storageUnitId) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT TOP 1 T1.STORAGEUNITID, T1.PARENTID, T1.STORAGEENVID, ");
        sqlStmt.append("T2.TLEVEL LEVEL ");
        sqlStmt.append("FROM STORAGEUNIT T1, ");
        sqlStmt.append("LV_ORA_GetSUTree( '").append(storageUnitId);
        sqlStmt.append("', 'pss', default, default ) T2 ");
        sqlStmt.append("WHERE T1.STORAGEENVID IS NOT NULL ");
        sqlStmt.append("AND { fn LENGTH( T1.STORAGEENVID )} > 0  ");
        sqlStmt.append("AND T1.STORAGEENVID <> '(null)' ");
        sqlStmt.append("AND T1.STORAGEUNITID = T2.STORAGEUNITID ");
        sqlStmt.append("ORDER BY T2.TLEVEL ");
        return sqlStmt.toString();
    }

    public static String getStorageUnitAncestorInfoSql(String storageUnitId) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT STORAGEUNITID, PARENTID, ");
        sqlStmt.append("ISNULL( STORAGEENVID, '(null)' ) STORAGEENVID, ");
        sqlStmt.append("ISNULL( ANCESTORID, '(null)' ) ANCESTORID  ");
        sqlStmt.append("FROM STORAGEUNIT ");
        sqlStmt.append("WHERE STORAGEUNITID = '").append(storageUnitId).append("' ");
        return sqlStmt.toString();
    }

    public static String getStorageUnitChildrenAncestorSyncSql(String storageUnitId) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT T1.STORAGEUNITID, T1.STORAGEENVID, ");
        sqlStmt.append("T2.TLEVEL LEVEL ");
        sqlStmt.append("FROM STORAGEUNIT T1, ");
        sqlStmt.append("LV_ORA_GetSUTree( '").append(storageUnitId);
        sqlStmt.append("', 'spp', default, default) T2 ");
        sqlStmt.append("WHERE T1.STORAGEUNITID = T2.STORAGEUNITID  ");
        sqlStmt.append("AND( T1.STORAGEENVID IS NULL ");
        sqlStmt.append("       OR { fn LENGTH( T1.STORAGEENVID )} = 0  ");
        sqlStmt.append("       OR T1.STORAGEENVID = '(null)' ) ");
        return sqlStmt.toString();
    }

    public static String getStorageUnitAndChildrensLabelPathSql(String storageUnitId) {
        StringBuffer sqlStmt = new StringBuffer();
        sqlStmt.append("SELECT storageUnit.storageunitid, t2.labelpath ");
        sqlStmt.append("FROM storageUnit INNER JOIN ");
        sqlStmt.append("LV_ORA_GetSUTree('" + storageUnitId + "', 'sps', DEFAULT, DEFAULT) t2 ");
        sqlStmt.append("ON StorageUnit.StorageUnitId = t2.storageunitid ");
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

