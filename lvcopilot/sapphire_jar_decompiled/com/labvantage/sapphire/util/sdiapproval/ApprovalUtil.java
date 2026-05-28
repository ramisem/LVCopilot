/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.util.sdiapproval;

import com.labvantage.sapphire.SDI;
import java.util.ArrayList;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ApprovalUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 89418 $";

    public static String getApprovalsteps(String approvaltypeid, QueryProcessor qp) {
        StringBuffer sbResponse = new StringBuffer();
        if (approvaltypeid != null && approvaltypeid.length() > 0) {
            String[] typeids = StringUtil.split(approvaltypeid, ";");
            StringBuffer sbApprovalTypeIds = new StringBuffer();
            for (int i = 0; i < typeids.length; ++i) {
                sbApprovalTypeIds.append(typeids[i] + ";");
            }
            if (sbApprovalTypeIds.length() > 0) {
                sbApprovalTypeIds.setLength(sbApprovalTypeIds.length() - 1);
            }
            try {
                StringBuffer sql = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT DISTINCT approvaltypeid, approvalstep, roleid, mandatoryflag, usersequence, forcepeerflag");
                sql.append(" FROM approvaltypestep WHERE approvaltypeid IN (" + safeSQL.addIn(sbApprovalTypeIds.toString(), ";") + ")");
                sql.append(" ORDER BY approvaltypeid, usersequence");
                DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null) {
                    for (int i = 0; i < ds.size(); ++i) {
                        sbResponse.append(ds.getValue(i, "approvaltypeid")).append(";");
                        sbResponse.append(ds.getValue(i, "approvalstep")).append(";");
                        sbResponse.append(ds.getValue(i, "roleid")).append(";");
                        sbResponse.append(ds.getValue(i, "mandatoryflag")).append(";");
                        sbResponse.append(ds.getValue(i, "forcepeerflag")).append(";");
                        sbResponse.append(ds.getValue(i, "usersequence")).append("|");
                    }
                    if (sbResponse.length() > 1) {
                        sbResponse.setLength(sbResponse.length() - 1);
                    }
                }
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
                sbResponse.setLength(0);
                sbResponse.append("Error :Failed to return approval steps.");
            }
        }
        return sbResponse.toString();
    }

    public static Boolean isSDIApprovalAllPassed(DBAccess database, SDI sdi) throws SapphireException {
        String sdcid = sdi.getSdcid();
        String keyid1 = sdi.getKeyid1();
        String keyid2 = sdi.getKeyid2();
        String keyid3 = sdi.getKeyid3();
        Boolean returnValue = null;
        String sql = "SELECT approvalflag FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?";
        database.createPreparedResultSet("approvaldata", sql, new String[]{sdcid, keyid1, keyid2, keyid3});
        DataSet ds = new DataSet(database.getResultSet("approvaldata"));
        int passCtr = 0;
        int failCtr = 0;
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String approvalFlag = ds.getValue(i, "approvalflag", "").trim();
                if (approvalFlag.equalsIgnoreCase("P")) {
                    ++passCtr;
                    continue;
                }
                if (!approvalFlag.equalsIgnoreCase("F")) continue;
                ++failCtr;
            }
            if (ds.size() == failCtr + passCtr) {
                returnValue = new Boolean(failCtr <= 0);
            }
        }
        return returnValue;
    }

    public static DataSet getSDIApprovalFlags(DBAccess database, DataSet dsApproval) throws SapphireException {
        DataSet dsResult = new DataSet();
        if (dsApproval != null && dsApproval.size() > 0) {
            dsApproval.sort("sdcid, keyid1, keyid2, keyid3");
            ArrayList<DataSet> sdis = dsApproval.getGroupedDataSets("sdcid, keyid1, keyid2, keyid3");
            for (int idx = 0; idx < sdis.size(); ++idx) {
                DataSet ds = sdis.get(idx);
                String sdcid = ds.getValue(0, "sdcid");
                String keyid1 = ds.getValue(0, "keyid1");
                String keyid2 = ds.getValue(0, "keyid2", "(null)");
                String keyid3 = ds.getValue(0, "keyid3", "(null)");
                String tracelogId = ds.getValue(0, "tracelogid");
                SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
                Boolean result = ApprovalUtil.isSDIApprovalAllPassed(database, sdi);
                if (result == null) continue;
                int newRow = dsResult.addRow();
                String approvalFlag = result != false ? "Pass" : "Fail";
                dsResult.setString(newRow, "sdcid", sdcid);
                dsResult.setString(newRow, "keyid1", keyid1);
                dsResult.setString(newRow, "keyid2", keyid2);
                dsResult.setString(newRow, "keyid3", keyid3);
                dsResult.setString(newRow, "approvalflag", approvalFlag);
                dsResult.setString(newRow, "tracelogid", tracelogId);
            }
        }
        return dsResult;
    }

    public static boolean isSDILastModifiedByCurrentUser(SDI sdi, SDCProcessor sdcp, QueryProcessor qp, String currentUser, PageContext pageContext) {
        boolean modified = false;
        String sdcId = sdi.getSdcid();
        String tableId = sdcp.getProperty(sdcId, "tableid");
        String keycolid1 = sdcp.getProperty(sdcId, "keycolid1");
        String keycolid2 = sdcp.getProperty(sdcId, "keycolid2");
        String keycolid3 = sdcp.getProperty(sdcId, "keycolid3");
        String keyid1list = sdi.getKeyid1();
        String keyid2list = sdi.getKeyid2();
        String keyid3list = sdi.getKeyid3();
        SafeSQL safeSQL = new SafeSQL();
        DAMProcessor dp = new DAMProcessor(pageContext);
        String sql = "";
        if (sdcId.equalsIgnoreCase("Sample")) {
            sql = "SELECT s_sample.modby modby1,s_sample.createby createby1,sdidataitem.modby modby2,sdidataitem.createby createby2,sdidataapproval.modby modby3,sdidataapproval.createby createby3 ";
            if (keyid1list.contains(";")) {
                try {
                    String rsetId = dp.createRSet(sdcId, keyid1list, keyid2list, keyid3list);
                    sql = sql + " from rsetitems,sdidataitem,s_sample ";
                    sql = sql + " left join sdidataapproval on sdidataapproval.keyid1=s_sample.s_sampleid ";
                    sql = sql + " where s_sample.s_sampleid = rsetitems.keyid1";
                    sql = sql + " AND rsetitems.rsetid=" + safeSQL.addVar(rsetId);
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            } else {
                sql = sql + " from sdidataitem,s_sample ";
                sql = sql + " left join sdidataapproval on sdidataapproval.keyid1=s_sample.s_sampleid ";
                sql = sql + " WHERE s_sample.s_sampleid= " + safeSQL.addVar(keyid1list);
            }
            sql = sql + " and sdidataitem.sdcid='Sample'";
            sql = sql + " and sdidataitem.keyid1=s_sample.s_sampleid";
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            for (int r = 0; r < ds.getRowCount(); ++r) {
                if (!currentUser.equals(ds.getString(r, "modby1")) && !currentUser.equals(ds.getString(r, "createby1")) && !currentUser.equals(ds.getString(r, "modby2")) && !currentUser.equals(ds.getString(r, "createby2")) && !currentUser.equals(ds.getString(r, "modby3")) && !currentUser.equals(ds.getString(r, "createby3"))) continue;
                modified = true;
                break;
            }
        } else {
            if (keyid1list.contains(";")) {
                try {
                    sql = "SELECT modby, createby FROM " + tableId + ",rsetitems  WHERE " + keycolid1 + " =rsetitems.keyid1";
                    String rsetId = dp.createRSet(sdcId, keyid1list, keyid2list, keyid3list);
                    if (keycolid2.length() > 0) {
                        sql = sql + " AND " + keycolid2 + " =rsetitems.keyid2";
                    }
                    if (keycolid3.length() > 0) {
                        sql = sql + " AND " + keycolid3 + " =rsetitems.keyid3";
                    }
                    sql = sql + " AND rsetitems.rsetid=" + safeSQL.addVar(rsetId);
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            } else {
                sql = "SELECT modby, createby FROM " + tableId + " WHERE " + keycolid1 + " = " + safeSQL.addVar(keyid1list);
                if (keycolid2.length() > 0) {
                    sql = sql + " AND " + keycolid2 + " = " + safeSQL.addVar(keyid2list);
                }
                if (keycolid3.length() > 0) {
                    sql = sql + " AND " + keycolid3 + " = " + safeSQL.addVar(keyid3list);
                }
            }
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            for (int r = 0; r < ds.getRowCount(); ++r) {
                if (!currentUser.equals(ds.getString(r, "modby")) && !currentUser.equals(ds.getString(r, "createby"))) continue;
                modified = true;
                break;
            }
        }
        return modified;
    }
}

