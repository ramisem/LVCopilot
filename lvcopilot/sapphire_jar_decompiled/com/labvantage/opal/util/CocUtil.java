/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.elements.coc.SDICOC;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CocUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54130 $";

    public static HashMap getCustodianPwdRequiredMap(QueryProcessor qp, SQLGenerator sqlGenerator) {
        HashMap<String, String> map = new HashMap<String, String>();
        DataSet ds = qp.getSqlDataSet(sqlGenerator.getUserCustodianInfo());
        for (int i = 0; i < ds.size(); ++i) {
            String id = ds.getValue(i, "ID");
            String type = ds.getValue(i, "TYPE");
            String flag = ds.getValue(i, "FLAG");
            if (type.equalsIgnoreCase("SYSUSER")) {
                if (flag.equalsIgnoreCase("N")) {
                    map.put(id, "Y");
                    continue;
                }
                map.put(id, "N");
                continue;
            }
            if (flag.equalsIgnoreCase("Y")) {
                map.put(id, "Y");
                continue;
            }
            map.put(id, "N");
        }
        return map;
    }

    public static Object getSDICOC(RequestContext context, QueryProcessor qp, SDCProcessor sp, SQLGenerator sqlGenerator) {
        SDICOC sdicoc = new SDICOC();
        ArrayList<String> excludelist = new ArrayList<String>();
        try {
            String key;
            DataSet ds;
            String rsetid;
            boolean createRSet;
            String keyid1 = context.getProperty("keyid1");
            String sysuserid = context.getProperty("currentuser");
            String manual = context.getProperty("manual");
            if (manual == null || manual.length() == 0) {
                manual = "N";
            }
            StringBuilder sql = new StringBuilder();
            boolean bl = createRSet = StringUtil.split(keyid1, ";").length > 1000;
            if (createRSet) {
                DAMProcessor damProcessor = new DAMProcessor(qp.getConnectionid());
                rsetid = damProcessor.createRSet("Sample", keyid1, null, null);
                sql.append("SELECT T1.S_SDICOCID, T1.KEYID1, T1.FROMCUSTODIANID,");
                sql.append(" T1.TOCUSTODIANID, COALESCE( T3.CONTROLSUBSTANCEFLAG, 'N' ) CONTROLSUBSTANCEFLAG");
                sql.append(" FROM S_SDICOC T1, S_SAMPLE T3");
                sql.append(" WHERE T1.SDCID = ?");
                sql.append(" AND T1.KEYID1 IN ( SELECT T4.S_SAMPLEID FROM S_SAMPLE T4 WHERE T4.COCREQUIREDFLAG = 'Y'");
                sql.append(" AND T4.S_SAMPLEID IN ( SELECT R.KEYID1 FROM RSETITEMS R WHERE R.RSETID = ").append("?").append(" ) )");
                sql.append(" AND T1.S_SDICOCID = ( SELECT MAX(T2.S_SDICOCID) FROM");
                sql.append(" S_SDICOC T2 WHERE T2.SDCID = T1.SDCID AND T2.KEYID1 = T1.KEYID1 )");
                sql.append(" AND T3.S_SAMPLEID = T1.KEYID1");
                Object[] p = new Object[]{"Sample", rsetid};
                ds = qp.getPreparedSqlDataSet(sql.toString(), p);
                damProcessor.clearRSet(rsetid);
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT T1.S_SDICOCID, T1.KEYID1, T1.FROMCUSTODIANID,");
                sql.append(" T1.TOCUSTODIANID, COALESCE( T3.CONTROLSUBSTANCEFLAG, 'N' ) CONTROLSUBSTANCEFLAG");
                sql.append(" FROM S_SDICOC T1, S_SAMPLE T3");
                sql.append(" WHERE T1.SDCID = ").append(safeSQL.addVar("Sample"));
                sql.append(" AND T1.KEYID1 IN ( SELECT T4.S_SAMPLEID FROM S_SAMPLE T4 WHERE T4.COCREQUIREDFLAG = 'Y'");
                sql.append(" AND T4.S_SAMPLEID IN ( ").append(safeSQL.addIn(keyid1, ";")).append(" ) )");
                sql.append(" AND T1.S_SDICOCID = ( SELECT MAX(T2.S_SDICOCID) FROM");
                sql.append(" S_SDICOC T2 WHERE T2.SDCID = T1.SDCID AND T2.KEYID1 = T1.KEYID1 )");
                sql.append(" AND T3.S_SAMPLEID = T1.KEYID1");
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    key = ds.getValue(i, "KEYID1");
                    excludelist.add(key);
                    String currentcustodianid = ds.getValue(i, "TOCUSTODIANID");
                    String lastcustodianid = ds.getValue(i, "FROMCUSTODIANID");
                    String controlsubstance = ds.getValue(i, "CONTROLSUBSTANCEFLAG");
                    if (sysuserid.equals(currentcustodianid) && !manual.equalsIgnoreCase("Y")) continue;
                    sdicoc.add(key);
                    sdicoc.setCocStarted(key, true);
                    sdicoc.setCurrentCustodian(key, currentcustodianid);
                    sdicoc.setLastCustodian(key, lastcustodianid);
                    if (!controlsubstance.equals("Y")) continue;
                    sdicoc.setControlsubstance(key);
                }
            }
            sql.setLength(0);
            if (createRSet) {
                DAMProcessor damProcessor = new DAMProcessor(qp.getConnectionid());
                rsetid = damProcessor.createRSet("Sample", keyid1, null, null);
                String rsetid2 = null;
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT S_SAMPLEID, CONTROLSUBSTANCEFLAG FROM S_SAMPLE");
                sql.append(" WHERE COCREQUIREDFLAG = 'Y'");
                sql.append(" AND S_SAMPLEID IN ( SELECT R.KEYID1 FROM RSETITEMS R WHERE R.RSETID = ").append(safeSQL.addVar(rsetid)).append(" )");
                if (excludelist.size() > 0) {
                    if (excludelist.size() > 1000) {
                        rsetid2 = damProcessor.createRSet("Sample", OpalUtil.toDelimitedString(excludelist, ";"), null, null);
                        sql.append(" AND S_SAMPLEID NOT IN ( SELECT R2.KEYID1 FROM RSETITEMS R2 WHERE R2.RSETID = ").append(safeSQL.addVar(rsetid2)).append(")");
                    } else {
                        sql.append(" AND S_SAMPLEID NOT IN ( '").append(safeSQL.addIn(excludelist)).append("' )");
                    }
                }
                sql.append(" ORDER BY S_SAMPLEID");
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                damProcessor.clearRSet(rsetid);
                if (OpalUtil.isNotEmpty(rsetid2)) {
                    damProcessor.clearRSet(rsetid2);
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT S_SAMPLEID, CONTROLSUBSTANCEFLAG FROM S_SAMPLE");
                sql.append(" WHERE COCREQUIREDFLAG = 'Y'");
                sql.append(" AND S_SAMPLEID IN (").append(safeSQL.addIn(keyid1, ";")).append(")");
                if (excludelist.size() > 0) {
                    sql.append(" AND S_SAMPLEID NOT IN ( ").append(safeSQL.addIn(OpalUtil.toDelimitedString(excludelist, "','"))).append(" )");
                }
                sql.append(" ORDER BY S_SAMPLEID");
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    key = ds.getValue(i, "s_sampleid");
                    String controlsubstance = ds.getValue(i, "CONTROLSUBSTANCEFLAG");
                    if (sdicoc.contains(key)) continue;
                    sdicoc.add(key);
                    if (!controlsubstance.equals("Y")) continue;
                    sdicoc.setControlsubstance(key);
                }
            }
            sdicoc.setSysuserid(sysuserid);
            return sdicoc;
        }
        catch (Exception e) {
            Logger.logError(e.getMessage(), e);
            return "Exception caught: " + e.getMessage();
        }
    }

    public static Object getSDICOC(PropertyList pagedata, QueryProcessor qp, SDCProcessor sp, SQLGenerator sqlGenerator) {
        SDICOC sdicoc = new SDICOC();
        ArrayList<String> excludelist = new ArrayList<String>();
        try {
            String key;
            DataSet ds;
            String rsetid;
            boolean createRSet;
            String sdcid = pagedata.getProperty("sdcid");
            String tableid = sp.getProperty(sdcid, "tableid");
            String keycolid1 = sp.getProperty(sdcid, "keycolid1");
            String keyid1 = pagedata.getProperty("keyid1");
            String sysuserid = pagedata.getProperty("currentuser");
            String manual = pagedata.getProperty("manual");
            if (manual == null || manual.length() == 0) {
                manual = "N";
            }
            StringBuilder sql = new StringBuilder();
            boolean bl = createRSet = StringUtil.split(keyid1, ";").length > 1000;
            if (createRSet) {
                DAMProcessor damProcessor = new DAMProcessor(qp.getConnectionid());
                rsetid = damProcessor.createRSet(sdcid, keyid1, null, null);
                sql.append("SELECT T1.S_SDICOCID, T1.KEYID1, T1.FROMCUSTODIANID,");
                sql.append(" T1.TOCUSTODIANID, COALESCE( T3.CONTROLSUBSTANCEFLAG, 'N' ) CONTROLSUBSTANCEFLAG");
                sql.append(" FROM S_SDICOC T1, ").append(tableid).append(" T3");
                sql.append(" WHERE T1.SDCID = ").append("?").append("");
                sql.append(" AND T1.KEYID1 IN ( SELECT T4.").append(keycolid1).append(" FROM ").append(tableid).append(" T4 WHERE T4.COCREQUIREDFLAG = 'Y'");
                sql.append(" AND T4.").append(keycolid1).append(" IN ( SELECT R.KEYID1 FROM RSETITEMS R WHERE R.RSETID = ").append("?").append(" ) )");
                sql.append(" AND T1.S_SDICOCID = ( SELECT MAX(T2.S_SDICOCID) FROM");
                sql.append(" S_SDICOC T2 WHERE T2.SDCID = T1.SDCID AND T2.KEYID1 = T1.KEYID1 )");
                sql.append(" AND T3.").append(keycolid1).append(" = T1.KEYID1");
                Object[] p = new Object[]{sdcid, rsetid};
                ds = qp.getPreparedSqlDataSet(sql.toString(), p);
                damProcessor.clearRSet(rsetid);
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT T1.S_SDICOCID, T1.KEYID1, T1.FROMCUSTODIANID,");
                sql.append(" T1.TOCUSTODIANID, COALESCE( T3.CONTROLSUBSTANCEFLAG, 'N' ) CONTROLSUBSTANCEFLAG");
                sql.append(" FROM S_SDICOC T1, ").append(tableid).append(" T3");
                sql.append(" WHERE T1.SDCID = ").append(safeSQL.addVar(sdcid)).append("");
                sql.append(" AND T1.KEYID1 IN ( SELECT T4.").append(keycolid1).append(" FROM ").append(tableid).append(" T4 WHERE T4.COCREQUIREDFLAG = 'Y'");
                sql.append(" AND T4.").append(keycolid1).append(" IN ( ").append(safeSQL.addIn(keyid1, ";")).append(" ) )");
                sql.append(" AND T1.S_SDICOCID = ( SELECT MAX(T2.S_SDICOCID) FROM");
                sql.append(" S_SDICOC T2 WHERE T2.SDCID = T1.SDCID AND T2.KEYID1 = T1.KEYID1 )");
                sql.append(" AND T3.").append(keycolid1).append(" = T1.KEYID1");
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    key = ds.getValue(i, "KEYID1");
                    excludelist.add(key);
                    String currentcustodianid = ds.getValue(i, "TOCUSTODIANID");
                    String lastcustodianid = ds.getValue(i, "FROMCUSTODIANID");
                    String controlsubstance = ds.getValue(i, "CONTROLSUBSTANCEFLAG");
                    if (sysuserid.equals(currentcustodianid) && !manual.equalsIgnoreCase("Y")) continue;
                    sdicoc.add(key);
                    sdicoc.setCocStarted(key, true);
                    sdicoc.setCurrentCustodian(key, currentcustodianid);
                    sdicoc.setLastCustodian(key, lastcustodianid);
                    if (!controlsubstance.equals("Y")) continue;
                    sdicoc.setControlsubstance(key);
                }
            }
            sql.setLength(0);
            if (createRSet) {
                DAMProcessor damProcessor = new DAMProcessor(qp.getConnectionid());
                rsetid = damProcessor.createRSet(sdcid, keyid1, null, null);
                String rsetid2 = null;
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT ").append(keycolid1).append(", CONTROLSUBSTANCEFLAG FROM ").append(tableid);
                sql.append(" WHERE COCREQUIREDFLAG = 'Y'");
                sql.append(" AND ").append(keycolid1).append(" IN ( SELECT R.KEYID1 FROM RSETITEMS R WHERE R.RSETID = ").append(safeSQL.addVar(rsetid)).append(" )");
                if (excludelist.size() > 0) {
                    if (excludelist.size() > 1000) {
                        rsetid2 = damProcessor.createRSet("Sample", OpalUtil.toDelimitedString(excludelist, ";"), null, null);
                        sql.append(" AND ").append(keycolid1).append(" NOT IN ( SELECT R2.KEYID1 FROM RSETITEMS R2 WHERE R2.RSETID = ").append(safeSQL.addVar(rsetid2)).append(" )");
                    } else {
                        sql.append(" AND ").append(keycolid1).append(" NOT IN ( '").append(safeSQL.addIn(excludelist)).append("' )");
                    }
                }
                sql.append(" ORDER BY ").append(keycolid1);
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                damProcessor.clearRSet(rsetid);
                if (OpalUtil.isNotEmpty(rsetid2)) {
                    damProcessor.clearRSet(rsetid2);
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT ").append(keycolid1).append(", CONTROLSUBSTANCEFLAG FROM ").append(tableid);
                sql.append(" WHERE COCREQUIREDFLAG = 'Y'");
                sql.append(" AND ").append(keycolid1).append(" IN (").append(safeSQL.addIn(keyid1, ";")).append(")");
                if (excludelist.size() > 0) {
                    key = OpalUtil.toDelimitedString(excludelist, "','");
                    sql.append(" AND ").append(keycolid1).append(" NOT IN ( ").append(safeSQL.addIn(key)).append(" )");
                }
                sql.append(" ORDER BY ").append(keycolid1);
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    key = ds.getValue(i, keycolid1);
                    String controlsubstance = ds.getValue(i, "CONTROLSUBSTANCEFLAG");
                    if (sdicoc.contains(key)) continue;
                    sdicoc.add(key);
                    if (!controlsubstance.equals("Y")) continue;
                    sdicoc.setControlsubstance(key);
                }
            }
            sdicoc.setSysuserid(sysuserid);
            return sdicoc;
        }
        catch (Exception e) {
            Logger.logError(e.getMessage(), e);
            return "Exception caught: " + e.getMessage();
        }
    }

    public static List<String> getCertifiedAnalysts(QueryProcessor queryProcessor, SQLGenerator sqlGenerator) {
        ArrayList<String> list = new ArrayList<String>();
        DataSet ds = queryProcessor.getSqlDataSet(sqlGenerator.getCertifiedUsers());
        for (int i = 0; i < ds.size(); ++i) {
            list.add(ds.getValue(i, "SYSUSERID"));
        }
        return list;
    }
}

