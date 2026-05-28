/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MultiSDIAttributeMaintAjaxSaver
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 104239 $";

    private DataSet getSaveDataSet(String x, String y, String value, String rowstatus, boolean isAttributeAcross) {
        DataSet temp = new DataSet();
        temp.addColumn("keyid1", 0);
        temp.addColumn("keyid2", 0);
        temp.addColumn("keyid3", 0);
        temp.addColumn("sdcid", 0);
        temp.addColumn("attributeid", 0);
        temp.addColumn("attributeinstance", 1);
        temp.addColumn("attributesdcid", 0);
        temp.addColumn("usersequence", 0);
        temp.addColumn("__rowstatus", 0);
        temp.addColumn("attributesourcetype", 0);
        temp.addColumn("textvalue", 0);
        String[] xKeyed = StringUtil.split(isAttributeAcross ? y : x, ";");
        String[] yKeyed = StringUtil.split(isAttributeAcross ? x : y, ";");
        int r = temp.addRow();
        temp.setValue(r, "keyid1", xKeyed[0]);
        if (xKeyed.length > 1 && !xKeyed[1].equalsIgnoreCase("(null)")) {
            temp.setValue(r, "keyid2", xKeyed[1]);
            if (xKeyed.length > 2 && !xKeyed[2].equalsIgnoreCase("(null)")) {
                temp.setValue(r, "keyid3", xKeyed[2]);
            } else {
                temp.setValue(r, "keyid3", "(null)");
            }
        } else {
            temp.setValue(r, "keyid2", "(null)");
            temp.setValue(r, "keyid3", "(null)");
        }
        temp.setValue(r, "attributeid", yKeyed[0]);
        temp.setValue(r, "attributeinstance", yKeyed[1]);
        temp.setValue(r, "attributesdcid", yKeyed[2]);
        temp.setValue(r, "sdcid", yKeyed[2]);
        temp.setValue(r, "__rowstatus", rowstatus.toUpperCase());
        temp.setValue(r, "attributesourcetype", "adhoc");
        temp.setValue(r, "textvalue", value);
        return temp;
    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "MaintAttributeHandler");
        String mode = ajaxResponse.getRequestParameter("mode", "edit");
        String auditReason = ajaxResponse.getRequestParameter("esigreason", "");
        String auditOperation = ajaxResponse.getRequestParameter("esigoperation", "");
        String auditSignedflag = ajaxResponse.getRequestParameter("esigsignedflag", "");
        String changes = ajaxResponse.getRequestParameter("changes", "");
        String newlyaddedStr = ajaxResponse.getRequestParameter("newlyaddedArr", "");
        String attForDel = ajaxResponse.getRequestParameter("attForDel", "");
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String expandedGroups = ajaxResponse.getRequestParameter("expandedGroups", "");
        String multiSDIAttributeMaint_pagedata = ajaxResponse.getRequestParameter("multiSDIAttributeMaint_pagedata", "");
        boolean isAttributeAcross = false;
        PropertyList pagedata = null;
        PropertyListCollection autosavecolumns = null;
        String valSeparator = "#;#";
        String recSeparator = "~;~";
        if (changes.length() > 0) {
            JSONArray jayChanges = null;
            try {
                jayChanges = new JSONArray(changes);
                if (multiSDIAttributeMaint_pagedata.length() > 0) {
                    pagedata = new PropertyList(new JSONObject(multiSDIAttributeMaint_pagedata));
                    isAttributeAcross = "Attribute Across".equals(pagedata.getProperty("gridlayout"));
                    autosavecolumns = pagedata.getCollection("savecolumns");
                }
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Changes invalid."));
            }
            if (jayChanges != null) {
                String yKey;
                String value;
                ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
                boolean hasPrimaryChange = false;
                DataSet ds = null;
                ArrayList<String> updateColumnList = new ArrayList<String>();
                String keycolid1 = "";
                String keycolid2 = "";
                String keycolid3 = "";
                String keycols = "1";
                StringBuffer keyid1sb = new StringBuffer();
                StringBuffer keyid2sb = new StringBuffer();
                StringBuffer keyid3sb = new StringBuffer();
                for (int i = 0; i < jayChanges.length(); ++i) {
                    try {
                        String attraxis;
                        JSONObject jobCell = jayChanges.getJSONObject(i);
                        String x = jobCell.getString("x");
                        String y = jobCell.getString("y");
                        String keyidaxis = isAttributeAcross ? y : x;
                        String string = attraxis = isAttributeAcross ? x : y;
                        if (attraxis != null && attraxis.indexOf("_") > 0) {
                            hasPrimaryChange = true;
                        }
                        String[] keyids = StringUtil.split(keyidaxis, ";");
                        keyid1sb.append(";" + keyids[0]);
                        keyid2sb.append(";" + keyids[1]);
                        keyid3sb.append(";" + keyids[2]);
                        continue;
                    }
                    catch (Exception e) {
                        this.logger.warn("Failed to save change on line " + i + ".");
                    }
                }
                if (hasPrimaryChange || autosavecolumns != null && autosavecolumns.size() > 0 && keyid1sb.length() > 1) {
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(sdcid);
                    sdiRequest.setKeyid1List(keyid1sb.substring(1));
                    sdiRequest.setKeyid2List(keyid2sb.substring(1));
                    sdiRequest.setKeyid3List(keyid3sb.substring(1));
                    sdiRequest.setRequestItem("primary");
                    ds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
                    keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                    keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
                    keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
                    keycols = this.getSDCProcessor().getProperty(sdcid, "keycolumns");
                }
                HashMap<String, String> findMap = new HashMap<String, String>();
                DataSet editAttributedata = new DataSet();
                try {
                    for (int i = 0; i < jayChanges.length(); ++i) {
                        JSONObject jobCell = jayChanges.getJSONObject(i);
                        String y = jobCell.getString(isAttributeAcross ? "x" : "y");
                        boolean checkM = false;
                        if (y != null && hasPrimaryChange) {
                            Pattern pattern = Pattern.compile("^(\\d+.*|-\\d+.*)");
                            Matcher matcher = pattern.matcher(y);
                            checkM = matcher.matches();
                        }
                        if (y != null && checkM) {
                            int row;
                            String columnid = y.substring(y.indexOf("_") + 1);
                            if (!updateColumnList.contains(columnid)) {
                                updateColumnList.add(columnid);
                                ds.addColumn(columnid, 0);
                            }
                            String[] keyids = StringUtil.split(jobCell.getString(isAttributeAcross ? "y" : "x"), ";");
                            value = jobCell.getString("value");
                            findMap.clear();
                            findMap.put(keycolid1, keyids[0]);
                            if ("2".equals(keycols) || "3".equals(keycols)) {
                                findMap.put(keycolid2, keyids[1]);
                                if ("3".equals(keycols)) {
                                    findMap.put(keycolid3, keyids[2]);
                                }
                            }
                            if ((row = ds.findRow(findMap)) < 0) continue;
                            ds.setValue(row, columnid, value);
                            continue;
                        }
                        DataSet temp = new DataSet();
                        String newFlag = "N";
                        if (newlyaddedStr != null && !"".equals(newlyaddedStr) && !"No Elements".equals(newlyaddedStr)) {
                            String[] newlyAddedArr = newlyaddedStr.split(recSeparator);
                            String xKeyNew = "";
                            String yKeyNew = "";
                            for (int k = 0; k < newlyAddedArr.length; ++k) {
                                String[] inData = newlyAddedArr[k].split(valSeparator);
                                String[] keyID1 = inData[2].split(";");
                                if (isAttributeAcross) {
                                    xKeyNew = inData[0] + ";" + inData[3] + ";" + inData[1];
                                    yKeyNew = inData[2];
                                } else {
                                    yKeyNew = inData[0] + ";" + inData[3] + ";" + inData[1];
                                    xKeyNew = inData[2];
                                }
                                if (!xKeyNew.equals(jobCell.getString("x")) || !yKeyNew.equals(jobCell.getString("y")) || !inData[4].equals("new")) continue;
                                newFlag = "Y";
                                break;
                            }
                        }
                        if ("Y".equals(newFlag)) {
                            if (jobCell.getString("x") != null && !"".equals(jobCell.getString("x"))) {
                                temp = this.getSaveDataSet(jobCell.getString("x"), jobCell.getString("y"), jobCell.getString("value"), "I", isAttributeAcross);
                            }
                        } else if (jobCell.getString("x") != null && !"".equals(jobCell.getString("x"))) {
                            temp = this.getSaveDataSet(jobCell.getString("x"), jobCell.getString("y"), jobCell.getString("value"), "U", isAttributeAcross);
                        }
                        if (attForDel != null && !"".equals(attForDel)) {
                            String[] forDelete = attForDel.split(recSeparator);
                            for (int k = 0; k < forDelete.length; ++k) {
                                String[] inData = forDelete[k].split(valSeparator);
                                String xKeyNew = inData[0] + ";" + inData[3] + ";" + inData[1];
                                String[] keyID1 = inData[2].split(";");
                                String yKeyNew = inData[2];
                                int size = temp.addRow();
                                temp.addColumn("keyid1", 0);
                                temp.addColumn("keyid2", 0);
                                temp.addColumn("keyid3", 0);
                                temp.addColumn("sdcid", 0);
                                temp.addColumn("attributeid", 0);
                                temp.addColumn("attributeinstance", 1);
                                temp.addColumn("attributesdcid", 0);
                                temp.addColumn("usersequence", 0);
                                temp.addColumn("__rowstatus", 0);
                                temp.addColumn("attributesourcetype", 0);
                                temp.addColumn("textvalue", 0);
                                temp.setValue(size, "keyid1", keyID1[0]);
                                temp.setValue(size, "keyid2", keyID1[1]);
                                temp.setValue(size, "keyid3", keyID1[2]);
                                temp.setValue(size, "attributeid", inData[0]);
                                temp.setValue(size, "attributeinstance", inData[3]);
                                temp.setValue(size, "attributesdcid", inData[1]);
                                temp.setValue(size, "sdcid", inData[1]);
                                temp.setValue(size, "__rowstatus", "D");
                                temp.setValue(size, "attributesourcetype", "adhoc");
                                temp.setValue(size, "textvalue", "");
                            }
                        }
                        if (temp == null || temp.getRowCount() <= 0) continue;
                        editAttributedata.copyRow(temp, -1, 1);
                    }
                    if (editAttributedata.getRowCount() > 0) {
                        BaseSDIAttributeAction.saveAttributeData(editAttributedata, this.getActionProcessor(), new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId())), auditReason, auditOperation, auditSignedflag);
                    }
                }
                catch (Exception e) {
                    this.logger.warn("Failed to save attribute changes.");
                }
                if (mode.equalsIgnoreCase("add")) {
                    String xKey = ajaxResponse.getRequestParameter("x", "");
                    yKey = ajaxResponse.getRequestParameter("y", "");
                    if (xKey.length() > 0 && yKey.length() > 0) {
                        DataSet temp = this.getSaveDataSet(xKey, yKey, "", "I", isAttributeAcross);
                        try {
                            BaseSDIAttributeAction.saveAttributeData(temp, this.getActionProcessor(), new M18NUtil(connectionInfo), auditReason, auditOperation, auditSignedflag);
                        }
                        catch (Exception e) {
                            this.logger.warn("Failed to add attribute " + xKey + ".");
                        }
                    } else {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("No attribute details provided."));
                    }
                }
                if (mode.equalsIgnoreCase("remove")) {
                    String xKey = ajaxResponse.getRequestParameter("x", "");
                    yKey = ajaxResponse.getRequestParameter("y", "");
                    if (xKey.length() > 0) {
                        String[] xKeyArr = xKey.split(recSeparator);
                        String[] yKeyArr = yKey.split(recSeparator);
                        DataSet deleteAttributeData = new DataSet();
                        for (int i = 0; i < xKeyArr.length; ++i) {
                            DataSet temp = this.getSaveDataSet(xKeyArr[i], yKeyArr[i], "", "D", isAttributeAcross);
                            if (temp == null || temp.getRowCount() <= 0) continue;
                            deleteAttributeData.copyRow(temp, -1, 1);
                        }
                        if (deleteAttributeData.getRowCount() > 0) {
                            try {
                                BaseSDIAttributeAction.saveAttributeData(deleteAttributeData, this.getActionProcessor(), new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId())), auditReason, auditOperation, auditSignedflag);
                            }
                            catch (SapphireException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("No attribute details provided."));
                    }
                }
                if (ds != null && ds.getRowCount() > 0) {
                    try {
                        String columnid;
                        if (autosavecolumns != null && autosavecolumns.size() > 0) {
                            DataSet dsCopy = ds.copy();
                            for (int c = 0; c < autosavecolumns.size(); ++c) {
                                columnid = autosavecolumns.getPropertyList(c).getProperty("columnid");
                                String valueexpr = autosavecolumns.getPropertyList(c).getProperty("value");
                                for (int i = 0; i < ds.getRowCount(); ++i) {
                                    if (valueexpr.indexOf("$G{") == 0) {
                                        HashMap<String, Object> bindMap = new HashMap<String, Object>();
                                        bindMap.put("primary", dsCopy.get(i));
                                        bindMap.put("user", connectionInfo.getUserAttributeMap());
                                        bindMap.put("currentrow", new Integer(i));
                                        bindMap.put("primarydataset", dsCopy);
                                        value = GroovyUtil.getInstance(connectionInfo).evaluateSecure(valueexpr, bindMap);
                                        ds.setValue(i, columnid, value);
                                        continue;
                                    }
                                    ds.setValue(i, columnid, valueexpr);
                                }
                                if (updateColumnList.contains(columnid)) continue;
                                updateColumnList.add(columnid);
                            }
                        }
                        PropertyList propertyList = new PropertyList();
                        propertyList.setProperty("sdcid", sdcid);
                        propertyList.setProperty("keyid1", ds.getColumnValues(keycolid1, ";"));
                        if (keycolid2.length() > 0) {
                            propertyList.setProperty("keyid2", ds.getColumnValues(keycolid2, ";"));
                        }
                        if (keycolid3.length() > 0) {
                            propertyList.setProperty("keyid3", ds.getColumnValues(keycolid3, ";"));
                        }
                        for (int i = 0; i < updateColumnList.size(); ++i) {
                            columnid = (String)updateColumnList.get(i);
                            propertyList.setProperty(columnid, ds.getColumnValues(columnid, ";"));
                        }
                        String actionId = "EditSDI";
                        String[] keyids = StringUtil.split(ds.getColumnValues(keycolid1, ";"), ";");
                        if (sdcid.equals("SDIWorkItem")) {
                            actionId = "EditSDIWorkItem";
                            StringBuffer keyid1s = new StringBuffer();
                            StringBuffer keyid2s = new StringBuffer();
                            StringBuffer keyid3s = new StringBuffer();
                            StringBuffer workitemids = new StringBuffer();
                            StringBuffer workiteminstances = new StringBuffer();
                            for (int k = 0; k < keyids.length; ++k) {
                                DataSet dssdiwi = this.getQueryProcessor().getPreparedSqlDataSet("select sdcid, keyid1,keyid2,keyid3,workitemid,workiteminstance from sdiworkitem where sdiworkitemid=?", (Object[])new String[]{keyids[k]});
                                propertyList.setProperty("sdcid", dssdiwi.getValue(0, "sdcid"));
                                keyid1s.append(";").append(dssdiwi.getValue(0, "keyid1"));
                                keyid2s.append(";").append(dssdiwi.getValue(0, "keyid2"));
                                keyid3s.append(";").append(dssdiwi.getValue(0, "keyid3"));
                                workitemids.append(";").append(dssdiwi.getValue(0, "workitemid"));
                                workiteminstances.append(";").append(dssdiwi.getValue(0, "workiteminstance"));
                            }
                            propertyList.setProperty("keyid1", keyid1s.substring(1));
                            propertyList.setProperty("keyid2", keyid2s.substring(1));
                            propertyList.setProperty("keyid3", keyid3s.substring(1));
                            propertyList.setProperty("workitemid", workitemids.substring(1));
                            propertyList.setProperty("workiteminstance", workiteminstances.substring(1));
                        }
                        if (sdcid.equals("DataSet")) {
                            actionId = "EditDataSet";
                            StringBuffer keyid1s = new StringBuffer();
                            StringBuffer keyid2s = new StringBuffer();
                            StringBuffer keyid3s = new StringBuffer();
                            StringBuffer paramlistids = new StringBuffer();
                            StringBuffer paramlistversionids = new StringBuffer();
                            StringBuffer variantids = new StringBuffer();
                            StringBuffer datasets = new StringBuffer();
                            for (int k = 0; k < keyids.length; ++k) {
                                DataSet dssdiwi = this.getQueryProcessor().getPreparedSqlDataSet("select sdcid, keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset from sdidata where sdidataid=?", (Object[])new String[]{keyids[k]});
                                propertyList.setProperty("sdcid", dssdiwi.getValue(0, "sdcid"));
                                keyid1s.append(";").append(dssdiwi.getValue(0, "keyid1"));
                                keyid2s.append(";").append(dssdiwi.getValue(0, "keyid2"));
                                keyid3s.append(";").append(dssdiwi.getValue(0, "keyid3"));
                                paramlistids.append(";").append(dssdiwi.getValue(0, "paramlistid"));
                                paramlistversionids.append(";").append(dssdiwi.getValue(0, "paramlistversionid"));
                                variantids.append(";").append(dssdiwi.getValue(0, "variantid"));
                                datasets.append(";").append(dssdiwi.getValue(0, "dataset"));
                            }
                            propertyList.setProperty("keyid1", keyid1s.substring(1));
                            propertyList.setProperty("keyid2", keyid2s.substring(1));
                            propertyList.setProperty("keyid3", keyid3s.substring(1));
                            propertyList.setProperty("paramlistid", paramlistids.substring(1));
                            propertyList.setProperty("paramlistversionid", paramlistversionids.substring(1));
                            propertyList.setProperty("variantid", variantids.substring(1));
                            propertyList.setProperty("dataset", datasets.substring(1));
                        }
                        if (auditReason.length() > 0) {
                            propertyList.setProperty("auditreason", auditReason);
                            propertyList.setProperty("auditactivity", auditOperation);
                            propertyList.setProperty("auditsignedflag", auditSignedflag);
                        }
                        this.getActionProcessor().processAction(actionId, "1", propertyList);
                    }
                    catch (SapphireException e) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Failed to Save primary.") + e.getMessage());
                    }
                }
                ajaxResponse.addCallbackArgument("message", "");
                ajaxResponse.addCallbackArgument("expandedGroups", expandedGroups);
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No changes provided."));
        }
        ajaxResponse.print();
    }
}

