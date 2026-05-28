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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateCheckOutSDI
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyId1List = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid1", ""), "%3B", ";");
        String keyId2List = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid2", ""), "%3B", ";");
        String keyId3List = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid3", ""), "%3B", ";");
        String pTreeNodeIds = StringUtil.replaceAll(ajaxResponse.getRequestParameter("propertytreenodeid", ""), "%3B", ";");
        message = ValidateCheckOutSDI.doValidateCheckOutSDI(sdcid, keyId1List, keyId2List, keyId3List, pTreeNodeIds, this.getConnectionId());
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }

    private static String doValidateCheckOutSDI(String sdcid, String keyId1List, String keyId2List, String keyId3List, String pTreeNodeIds, String connectionId) {
        String message = "";
        TranslationProcessor tp = new TranslationProcessor(connectionId);
        QueryProcessor queryProcessor = new QueryProcessor(connectionId);
        if (sdcid.length() > 0 && keyId1List.length() > 0) {
            String[] pTreeNodeIdArray;
            int keycolumns = Integer.parseInt(new SDCProcessor(connectionId).getProperty(sdcid, "keycolumns"));
            String[] key1array = StringUtil.split(keyId1List, ";");
            String[] key2array = keycolumns > 1 ? StringUtil.split(keyId2List, ";") : null;
            String[] key3array = keycolumns > 2 ? StringUtil.split(keyId3List, ";") : null;
            String[] stringArray = pTreeNodeIdArray = OpalUtil.isNotEmpty(pTreeNodeIds) ? StringUtil.split(pTreeNodeIds, ";") : null;
            if (!"PropertyTree".equals(sdcid)) {
                SDCProcessor sdcProcessor = new SDCProcessor(connectionId);
                boolean isVersioned = "Y".equals(new SDCProcessor(connectionId).getProperty(sdcid, "versionedflag"));
                if (isVersioned) {
                    try {
                        DAMProcessor damProcessor = new DAMProcessor(connectionId);
                        String rsetid = damProcessor.createRSet(sdcid, keyId1List, keyId2List, keyId3List);
                        String tableid = sdcProcessor.getProperty(sdcid, "tableid");
                        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
                        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
                        String sql = "select count(" + keycolid1 + ") expirecount from " + tableid + ", rsetitems r";
                        sql = sql + " where r.keyid1 = " + tableid + "." + keycolid1;
                        sql = sql + " and r.keyid2 = " + tableid + "." + keycolid2;
                        if (keycolumns > 2) {
                            sql = sql + " and r.keyid3 = " + tableid + "." + keycolid3;
                        }
                        sql = sql + " and versionstatus = 'E'";
                        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql = sql + " and r.rsetid = ?", (Object[])new String[]{rsetid});
                        if (OpalUtil.isNotEmpty(ds) && ds.getInt(0, "expirecount") > 0) {
                            message = tp.translate("Expired item is not allowed to be Checked out");
                        }
                        damProcessor.clearRSet(rsetid);
                    }
                    catch (SapphireException e) {
                        e.printStackTrace();
                    }
                }
                if (OpalUtil.isNotEmpty(message)) {
                    return message;
                }
            }
            int index = 0;
            SafeSQL safeSQL = new SafeSQL();
            block2: for (String key1 : key1array) {
                safeSQL.reset();
                String sql = "select changelogid, checkedoutbyuserid, checkedoutbydepartmentid, propertytreenodeid from changelog where linksdcid = " + safeSQL.addVar(sdcid) + " and linkkeyid1 = " + safeSQL.addVar(key1) + " and changelogstatus = '" + "Checked Out" + "'";
                if (key2array != null && key2array.length > index) {
                    sql = sql + " and linkkeyid2 = " + safeSQL.addVar(key2array[index]);
                }
                if (key3array != null && key3array.length > index) {
                    sql = sql + " and linkkeyid3 = " + safeSQL.addVar(key3array[index]);
                }
                String pTreeNodeId = "";
                if (pTreeNodeIdArray != null && pTreeNodeIdArray.length >= index) {
                    pTreeNodeId = pTreeNodeIdArray[index];
                }
                ++index;
                DataSet changeLogInfo = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (changeLogInfo != null) {
                    String checkedoutbyuserid;
                    if (changeLogInfo.size() <= 0) continue;
                    if ("PropertyTree".equals(sdcid)) {
                        if (pTreeNodeId.length() > 0) {
                            for (int i = 0; i < changeLogInfo.getRowCount(); ++i) {
                                String checkedOutBy;
                                String checkedOutNodeId = changeLogInfo.getString(i, "propertytreenodeid", "");
                                if (checkedOutNodeId.length() == 0) {
                                    checkedOutBy = changeLogInfo.getString(i, "checkedoutbyuserid", "Department: " + changeLogInfo.getString(i, "checkedoutbydepartmentid"));
                                    message = tp.translate("Selected Tree has already been checked out by: ") + checkedOutBy;
                                    continue block2;
                                }
                                if (!checkedOutNodeId.equals(pTreeNodeId)) continue;
                                checkedOutBy = changeLogInfo.getString(i, "checkedoutbyuserid", "Department: " + changeLogInfo.getString(i, "checkedoutbydepartmentid"));
                                message = tp.translate("Selected Node has already been checked out by: ") + checkedOutBy;
                                continue block2;
                            }
                            continue;
                        }
                        checkedoutbyuserid = changeLogInfo.getColumnValues("checkedoutbyuserid", ";");
                        message = tp.translate("Selected Tree (or some Nodes) has already been checked out by: ") + " [" + checkedoutbyuserid + "]";
                        continue;
                    }
                    checkedoutbyuserid = changeLogInfo.getString(0, "checkedoutbyuserid", "");
                    message = tp.translate("Selected item has already been checked out by: ") + " [" + checkedoutbyuserid + "]";
                    break;
                }
                message = tp.translate("Failed fetching change log data. If problem persists, contact your Administrator.");
            }
        } else {
            message = tp.translate("Please select an item to Check Out");
        }
        return message;
    }
}

