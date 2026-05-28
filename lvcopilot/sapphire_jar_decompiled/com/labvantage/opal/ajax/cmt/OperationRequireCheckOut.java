/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.cmt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class OperationRequireCheckOut
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "").trim();
        Result result = OperationRequireCheckOut.doOperationRequireCheckout(sdcid, ajaxResponse.getRequestParameter("keyid1", ""), ajaxResponse.getRequestParameter("keyid2", ""), ajaxResponse.getRequestParameter("keyid3", ""), ajaxResponse.getRequestParameter("propertytreenodeid", ""), this.getConnectionId(), null);
        StringBuffer message = new StringBuffer();
        if (result.checkedOutToOthers != null && result.checkedOutToOthers.getRowCount() > 0) {
            message.append(this.getTranslationProcessor().translate("Operation not allowed"));
            message.append("<br>");
            message.append(this.getTranslationProcessor().translate("Selected item(s) are checked out:"));
            for (int i = 0; i < result.checkedOutToOthers.size(); ++i) {
                message.append("<br>&#8226;&nbsp;");
                String linkSDCId = result.checkedOutToOthers.getString(i, "linksdcid");
                message.append(result.checkedOutToOthers.getString(i, "linkkeyid1"));
                if ("PropertyTree".equals(linkSDCId)) {
                    message.append(", " + result.checkedOutToOthers.getString(i, "propertytreenodeid"));
                } else {
                    int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(linkSDCId, "keycolumns"));
                    message.append(keycolumns > 1 ? ", " + result.checkedOutToOthers.getString(i, "linkkeyid2") : "");
                    message.append(keycolumns > 2 ? ", " + result.checkedOutToOthers.getString(i, "linkkeyid3") : "");
                }
                String checkedoutbyuserid = result.checkedOutToOthers.getString(i, "checkedoutbyuserid", "");
                String checkedoutbydepartmentid = result.checkedOutToOthers.getString(i, "checkedoutbydepartmentid", "");
                if (checkedoutbydepartmentid.length() > 0) {
                    message.append(" (").append(this.getTranslationProcessor().translate("Checked out to department")).append(" ").append(checkedoutbydepartmentid).append(")");
                    continue;
                }
                message.append(" (").append(this.getTranslationProcessor().translate("Checked out by")).append(" ").append(checkedoutbyuserid).append(")");
            }
        }
        String checkOutAbleStr = "";
        if (result.checkOutAble != null && result.checkOutAble.getRowCount() > 0) {
            JSONObject checkOutAbleJson = new JSONObject();
            try {
                checkOutAbleJson.put("sdcid", sdcid);
                checkOutAbleJson.put("keyid1", result.checkOutAble.getColumnValues("keyid1", ";"));
                if (!"PropertyTree".equalsIgnoreCase(sdcid)) {
                    checkOutAbleJson.put("keyid2", result.checkOutAble.getColumnValues("keyid2", ";"));
                    checkOutAbleJson.put("keyid3", result.checkOutAble.getColumnValues("keyid3", ";"));
                    checkOutAbleJson.put("propertytreenodeid", "");
                } else {
                    checkOutAbleJson.put("keyid2", "");
                    checkOutAbleJson.put("keyid3", "");
                    checkOutAbleJson.put("propertytreenodeid", result.checkOutAble.getColumnValues("propertytreenodeid", ";"));
                }
                checkOutAbleStr = checkOutAbleJson.toString();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("message", message.toString());
        ajaxResponse.addCallbackArgument("checkoutsdi", checkOutAbleStr);
        ajaxResponse.addCallbackArgument("operationid", ajaxResponse.getRequestParameter("operationid"));
        ajaxResponse.addCallbackArgument("intarget", ajaxResponse.getRequestParameter("intarget"));
        ajaxResponse.addCallbackArgument("embeddedinlist", ajaxResponse.getRequestParameter("embeddedinlist"));
        ajaxResponse.print();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Result doOperationRequireCheckout(String sdcid, String keyId1, String keyId2, String keyId3, String propertyTreeNodeId, String connectionId, File rakFile) {
        Result result;
        block35: {
            DataSet checkedOutToOthers;
            DataSet checkedOutToMe;
            DataSet checkOutAble;
            block36: {
                result = new Result();
                SDCProcessor sdcProcessor = new SDCProcessor(rakFile, connectionId);
                DAMProcessor damProcessor = new DAMProcessor(rakFile, connectionId);
                QueryProcessor queryProcessor = new QueryProcessor(rakFile, connectionId);
                ConnectionProcessor connectionProcessor = new ConnectionProcessor(rakFile, connectionId);
                TranslationProcessor translationProcessor = new TranslationProcessor(rakFile, connectionId);
                if (sdcid.length() <= 0) break block35;
                String tableid = sdcProcessor.getProperty(sdcid, "tableid");
                String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
                String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
                StringBuilder sb = new StringBuilder();
                checkOutAble = new DataSet();
                checkedOutToMe = new DataSet();
                checkedOutToOthers = new DataSet();
                boolean isPropertyTreeSDC = "PropertyTree".equals(sdcid);
                SafeSQL safeSQL = new SafeSQL();
                String changeControlledFlag = CMTPolicy.getPolicy(rakFile, connectionId, sdcid, sdcid).getChangeControlledFlag();
                if (!"Y".equals(changeControlledFlag) && !"T".equals(changeControlledFlag)) break block36;
                String keyid1 = StringUtil.replaceAll(keyId1.trim(), "%3B", ";");
                String keyid2 = StringUtil.replaceAll(keyId2.trim(), "%3B", ";");
                String keyid3 = StringUtil.replaceAll(keyId3.trim(), "%3B", ";");
                String inputpropertytreenodeid = StringUtil.replaceAll(propertyTreeNodeId.trim(), "%3B", ";");
                DataSet changeLogDS = null;
                int keycolumns = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
                if (keyid1.contains(";")) {
                    try {
                        String rsetid = damProcessor.createRSet(sdcid, keyid1, keyid2, keyid3);
                        String sql = "select c.changelogid, c.linksdcid, c.linkkeyid1, c.linkkeyid2, c.linkkeyid3, c.checkedoutbyuserid, c.checkedoutbydepartmentid";
                        sql = sql + (isPropertyTreeSDC ? ", c.propertytreenodeid" : "");
                        sql = sql + " from changelog c, rsetitems r";
                        sql = sql + " where c.linksdcid = r.sdcid";
                        sql = sql + " and c.linkkeyid1 = r.keyid1";
                        if (!isPropertyTreeSDC) {
                            sql = sql + " and c.linkkeyid2 = r.keyid2";
                            sql = sql + " and c.linkkeyid3 = r.keyid3";
                        }
                        sql = sql + " and c.changelogstatus = 'Checked Out'";
                        sql = sql + " and r.rsetid = ?";
                        changeLogDS = queryProcessor.getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
                        damProcessor.clearRSet(rsetid);
                    }
                    catch (SapphireException e) {
                        e.printStackTrace();
                    }
                } else {
                    String sql = "select changelogid, linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, checkedoutbyuserid, checkedoutbydepartmentid";
                    sql = sql + (isPropertyTreeSDC ? ", propertytreenodeid" : "");
                    sql = sql + " from changelog";
                    sql = sql + " where linksdcid = " + safeSQL.addVar(sdcid);
                    sql = sql + " and linkkeyid1 = " + safeSQL.addVar(keyid1);
                    if (!isPropertyTreeSDC) {
                        sql = sql + (keycolumns > 1 ? " and linkkeyid2 = " + safeSQL.addVar(keyid2) : "");
                        sql = sql + (keycolumns > 2 ? " and linkkeyid3 = " + safeSQL.addVar(keyid3) : "");
                    }
                    sql = sql + " and changelogstatus = 'Checked Out'";
                    changeLogDS = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
                }
                if (changeLogDS == null) {
                    changeLogDS = new DataSet();
                }
                String sysuserid = connectionProcessor.getSapphireConnection().getSysuserId();
                List<String> userDepartmentList = OpalUtil.toList(connectionProcessor.getSapphireConnection().getDepartmentList(), ";");
                HashMap<String, String> filter = new HashMap<String, String>();
                DataSet inputDS = new DataSet();
                inputDS.addColumnValues("sdcid", 0, sdcid, ";");
                inputDS.addColumnValues("keyid1", 0, keyid1, ";");
                if (!isPropertyTreeSDC) {
                    inputDS.addColumnValues("keyid2", 0, keyid2, ";");
                    inputDS.addColumnValues("keyid3", 0, keyid3, ";");
                } else {
                    inputDS.addColumnValues("propertytreenodeid", 0, inputpropertytreenodeid, ";");
                }
                inputDS.padColumn("sdcid");
                for (int i = 0; i < inputDS.size(); ++i) {
                    boolean isCheckOutRequired;
                    block34: {
                        String checkedoutbydepartmentid;
                        String checkedoutbyuserid;
                        DataSet pTreeNonFullChangeLogs;
                        DataSet pTreeFullChangeLogs;
                        String propertytreenodeid;
                        block37: {
                            block38: {
                                String inputsdcid = inputDS.getString(i, "sdcid");
                                String inputkeyid1 = inputDS.getString(i, "keyid1");
                                String inputkeyid2 = null;
                                String inputkeyid3 = null;
                                propertytreenodeid = null;
                                if (!isPropertyTreeSDC) {
                                    inputkeyid2 = inputDS.getString(i, "keyid2", "(null)");
                                    inputkeyid3 = inputDS.getString(i, "keyid3", "(null)");
                                } else {
                                    propertytreenodeid = inputDS.getString(i, "propertytreenodeid", "");
                                }
                                filter.clear();
                                filter.put("linksdcid", inputsdcid);
                                filter.put("linkkeyid1", inputkeyid1);
                                if (!isPropertyTreeSDC) {
                                    filter.put("linkkeyid2", inputkeyid2);
                                    filter.put("linkkeyid3", inputkeyid3);
                                    int row = changeLogDS.findRow(filter);
                                    if (row != -1) {
                                        String checkedoutbyuserid2 = changeLogDS.getString(row, "checkedoutbyuserid", "");
                                        String checkedoutbydepartmentid2 = changeLogDS.getString(row, "checkedoutbydepartmentid", "");
                                        if (checkedoutbydepartmentid2.length() > 0) {
                                            if (!userDepartmentList.contains(checkedoutbydepartmentid2)) {
                                                checkedOutToOthers.copyRow(changeLogDS, row, 1);
                                                continue;
                                            }
                                            checkedOutToMe.copyRow(changeLogDS, row, 1);
                                            continue;
                                        }
                                        if (!sysuserid.equalsIgnoreCase(checkedoutbyuserid2)) {
                                            checkedOutToOthers.copyRow(changeLogDS, row, 1);
                                            continue;
                                        }
                                        checkedOutToMe.copyRow(changeLogDS, row, 1);
                                        continue;
                                    }
                                    if ("T".equals(changeControlledFlag)) {
                                        String templateflag;
                                        safeSQL.reset();
                                        String sql = "select templateflag from " + tableid;
                                        sql = sql + " where " + keycolid1 + " = " + safeSQL.addVar(inputkeyid1);
                                        if (keycolid2.length() > 0) {
                                            sql = sql + " and " + keycolid2 + " = " + safeSQL.addVar(inputkeyid2);
                                            if (keycolid3.length() > 0) {
                                                sql = sql + " and " + keycolid3 + " = " + safeSQL.addVar(inputkeyid3);
                                            }
                                        }
                                        if (!"Y".equals(templateflag = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues()).getString(0, "templateflag", "N"))) continue;
                                        checkOutAble.copyRow(inputDS, i, 1);
                                        continue;
                                    }
                                    checkOutAble.copyRow(inputDS, i, 1);
                                    continue;
                                }
                                DataSet pTreeChangeLogs = changeLogDS.getFilteredDataSet(filter);
                                HashMap<String, String> filterMap = new HashMap<String, String>();
                                filterMap.put("propertytreenodeid", "__FULL");
                                pTreeFullChangeLogs = pTreeChangeLogs.getFilteredDataSet(filterMap);
                                pTreeNonFullChangeLogs = pTreeChangeLogs.getFilteredDataSet(filterMap, true);
                                boolean isCheckOutOk = true;
                                isCheckOutRequired = true;
                                if (propertytreenodeid != null && propertytreenodeid.length() != 0 && !"__FULL".equals(propertytreenodeid)) break block37;
                                if (pTreeNonFullChangeLogs.getRowCount() <= 0) break block38;
                                for (int j = 0; j < pTreeNonFullChangeLogs.getRowCount(); ++j) {
                                    checkedOutToOthers.copyRow(pTreeNonFullChangeLogs, j, 1);
                                }
                                break block34;
                            }
                            if (pTreeFullChangeLogs.getRowCount() > 0) {
                                String checkedoutbyuserid3 = pTreeFullChangeLogs.getString(0, "checkedoutbyuserid", "");
                                String checkedoutbydepartmentid3 = pTreeFullChangeLogs.getString(0, "checkedoutbydepartmentid", "");
                                if (sysuserid.equals(checkedoutbyuserid3) || userDepartmentList.contains(checkedoutbydepartmentid3)) {
                                    isCheckOutRequired = false;
                                    break block34;
                                } else {
                                    checkedOutToOthers.copyRow(pTreeFullChangeLogs, 0, 1);
                                }
                                break block34;
                            } else {
                                isCheckOutRequired = true;
                            }
                            break block34;
                        }
                        int findRow = pTreeNonFullChangeLogs.findRow("propertytreenodeid", propertytreenodeid);
                        if (findRow > -1) {
                            checkedoutbyuserid = pTreeNonFullChangeLogs.getString(findRow, "checkedoutbyuserid", "");
                            checkedoutbydepartmentid = pTreeNonFullChangeLogs.getString(findRow, "checkedoutbydepartmentid", "");
                            if (sysuserid.equals(checkedoutbyuserid) || userDepartmentList.contains(checkedoutbydepartmentid)) {
                                isCheckOutRequired = false;
                                break block34;
                            } else {
                                checkedOutToOthers.copyRow(pTreeNonFullChangeLogs, findRow, 1);
                                break;
                            }
                        }
                        if (pTreeFullChangeLogs.getRowCount() > 0) {
                            checkedoutbyuserid = pTreeFullChangeLogs.getString(0, "checkedoutbyuserid", "");
                            checkedoutbydepartmentid = pTreeFullChangeLogs.getString(0, "checkedoutbydepartmentid", "");
                            if (sysuserid.equals(checkedoutbyuserid) || userDepartmentList.contains(checkedoutbydepartmentid)) {
                                isCheckOutRequired = false;
                            } else {
                                checkedOutToOthers.copyRow(pTreeFullChangeLogs, 0, 1);
                                break;
                            }
                        }
                    }
                    if (!isCheckOutRequired) continue;
                    checkOutAble.copyRow(inputDS, i, 1);
                }
            }
            if (checkedOutToOthers.getRowCount() > 0) {
                result.checkedOutToOthers = checkedOutToOthers;
            }
            if (checkedOutToMe.getRowCount() > 0) {
                result.checkedOutToMe = checkedOutToMe;
            }
            if (checkOutAble.getRowCount() > 0) {
                result.checkOutAble = checkOutAble;
            }
        }
        return result;
    }

    public static class Result {
        public DataSet checkedOutToOthers = new DataSet();
        public DataSet checkedOutToMe = new DataSet();
        public DataSet checkOutAble = new DataSet();
    }
}

