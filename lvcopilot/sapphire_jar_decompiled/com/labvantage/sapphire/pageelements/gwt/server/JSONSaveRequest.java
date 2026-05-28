/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.pageelements.gwt.server.DataItemCrossTabModel;
import com.labvantage.sapphire.pageelements.gwt.server.GWTDataEntry;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIDataItemTable;
import com.labvantage.sapphire.pageelements.gwt.server.command.SDIMaint;
import com.labvantage.sapphire.pageelements.gwt.server.command.Table;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class JSONSaveRequest
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    static HashMap sdcKeycount = new HashMap();

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            String serveroperation = request.getParameter("serveroperation");
            String auditactivity = request.getParameter("auditactivity");
            String auditreason = request.getParameter("auditreason");
            String auditsignedflag = request.getParameter("auditsignedflag");
            if ("Remeasure".equals(serveroperation) || "Add Replicate".equals(serveroperation)) {
                ActionBlock ab = new ActionBlock();
                String sdcid = request.getParameter("sdcid") != null ? request.getParameter("sdcid") : "Sample";
                StringBuilder keyid1Sb = new StringBuilder();
                StringBuilder paramlistidSb = new StringBuilder();
                StringBuilder paramlistversionidSb = new StringBuilder();
                StringBuilder variantidSb = new StringBuilder();
                StringBuilder datasetSb = new StringBuilder();
                StringBuilder paramidSb = new StringBuilder();
                StringBuilder paramtypeSb = new StringBuilder();
                String[] selecteditems = StringUtil.split("Remeasure".equals(serveroperation) ? request.getParameter("selecteddatasets") : request.getParameter("selecteddataitems"), "%3B");
                for (int i = 0; i < selecteditems.length; ++i) {
                    String[] itemkeys = StringUtil.split(selecteditems[i], ";");
                    keyid1Sb.append(";" + itemkeys[0]);
                    paramlistidSb.append(";" + itemkeys[1]);
                    paramlistversionidSb.append(";" + itemkeys[2]);
                    variantidSb.append(";" + itemkeys[3]);
                    datasetSb.append(";" + itemkeys[4]);
                    if (!"Add Replicate".equals(serveroperation)) continue;
                    paramidSb.append(";" + itemkeys[5]);
                    paramtypeSb.append(";" + itemkeys[6]);
                }
                if ("Remeasure".equals(serveroperation)) {
                    PropertyList remeasurePL = new PropertyList();
                    remeasurePL.setProperty("sdcid", sdcid);
                    String keyid1 = keyid1Sb.substring(1);
                    keyid1 = keyid1.indexOf(";") > 1 ? keyid1.substring(0, keyid1.indexOf(";")) : keyid1;
                    remeasurePL.setProperty("keyid1", keyid1);
                    remeasurePL.setProperty("paramlistid", paramlistidSb.substring(1));
                    remeasurePL.setProperty("paramlistversionid", paramlistversionidSb.substring(1));
                    remeasurePL.setProperty("variantid", variantidSb.substring(1));
                    remeasurePL.setProperty("dataset", datasetSb.substring(1));
                    remeasurePL.setProperty("newdsstatus", "Initial");
                    remeasurePL.setProperty("auditreason", auditreason);
                    remeasurePL.setProperty("auditactivity", auditactivity);
                    remeasurePL.setProperty("auditsignedflag", auditsignedflag);
                    ab.setAction("remeasure", "RemeasureDataSet", "1", remeasurePL);
                    PropertyList updatedatastatusPL = new PropertyList();
                    updatedatastatusPL.setProperty("sdcid", sdcid);
                    updatedatastatusPL.setProperty("keyid1", keyid1);
                    updatedatastatusPL.setProperty("eventnotify", "Y");
                    ab.setAction("updatedatastatus", "UpdateDatasetStatus", "1", updatedatastatusPL);
                } else {
                    String numofreplicate = request.getParameter("numofreplicate");
                    PropertyList addreplicatePL = new PropertyList();
                    addreplicatePL.setProperty("sdcid", sdcid);
                    addreplicatePL.setProperty("keyid1", keyid1Sb.substring(1));
                    addreplicatePL.setProperty("paramlistid", paramlistidSb.substring(1));
                    addreplicatePL.setProperty("paramlistversionid", paramlistversionidSb.substring(1));
                    addreplicatePL.setProperty("variantid", variantidSb.substring(1));
                    addreplicatePL.setProperty("dataset", datasetSb.substring(1));
                    addreplicatePL.setProperty("paramid", paramidSb.substring(1));
                    addreplicatePL.setProperty("paramtype", paramtypeSb.substring(1));
                    addreplicatePL.setProperty("numreplicate", numofreplicate);
                    addreplicatePL.setProperty("propsmatch", "Y");
                    addreplicatePL.setProperty("propsmatch", "Y");
                    addreplicatePL.setProperty("auditreason", auditreason);
                    addreplicatePL.setProperty("auditactivity", auditactivity);
                    addreplicatePL.setProperty("auditsignedflag", auditsignedflag);
                    ab.setAction("addreplicate", "AddReplicate", "1", addreplicatePL);
                }
                if ("Sample".equalsIgnoreCase(sdcid)) {
                    PropertyList SyncSDIDataSetStatusPL = new PropertyList();
                    SyncSDIDataSetStatusPL.setProperty("sdcid", sdcid);
                    SyncSDIDataSetStatusPL.setProperty("keyid1", keyid1Sb.substring(1));
                    ab.setAction("SyncSDIDataSetStatus", "SyncSDIDataSetStatus", "1", SyncSDIDataSetStatusPL);
                }
                if ("Add Replicate".equals(serveroperation)) {
                    PropertyList UpdateQCBatchStatusPL = new PropertyList();
                    UpdateQCBatchStatusPL.setProperty("sdcid", sdcid);
                    UpdateQCBatchStatusPL.setProperty("keyid1", keyid1Sb.substring(1));
                    ab.setAction("UpdateQCBatchStatus", "UpdateQCBatchStatus", "1", UpdateQCBatchStatusPL);
                }
                try {
                    this.getActionProcessor().processActionBlock(ab);
                }
                catch (ActionException ae) {
                    String lastMessage = ae.getErrorHandler().getLastErrorMessage();
                    if (lastMessage.charAt(lastMessage.length() - 1) == '|') {
                        lastMessage = lastMessage.substring(0, lastMessage.length() - 1);
                    }
                    if (lastMessage.charAt(lastMessage.length() - 1) == '|') {
                        lastMessage = lastMessage.substring(0, lastMessage.length() - 1);
                    }
                    if (lastMessage.indexOf("Reason:") > 0) {
                        lastMessage = lastMessage.substring(lastMessage.indexOf("Reason:") + 7);
                    }
                    TranslationProcessor tp = this.getTranslationProcessor();
                    this.write("Failed to " + serveroperation + ".<p>" + tp.translate("Reason") + ":" + tp.translate(lastMessage) + "</p>");
                }
                catch (Throwable t) {
                    this.write("Failed to " + serveroperation + ". " + t.getMessage());
                }
            } else {
                JSONObject jsonResponseObj;
                String sdcid;
                String jsonString = request.getParameter("jsonsaveobject");
                boolean isSaveAndRelease = request.getParameter("saveAndRelease") != null && "Y".equals(request.getParameter("saveAndRelease"));
                RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
                String connectionid = requestContext.getConnectionId();
                String webpageid = request.getParameter("webpageid");
                String elementid = request.getParameter("elementid");
                RequestProcessor requestProcessor = new RequestProcessor(connectionid);
                PropertyList pagedata = requestProcessor.getWebPageProperties(webpageid, (RequestContext)request.getAttribute("RequestContext"));
                PropertyList element = pagedata.getPropertyList(elementid);
                JSONObject saveObject = new JSONObject(jsonString);
                String redosdcid = request.getParameter("redosdcid");
                if (redosdcid != null) {
                    saveObject.put("redosdcid", request.getParameter("redosdcid"));
                    saveObject.put("redokeyid1", request.getParameter("redokeyid1"));
                    saveObject.put("redokeyid2", request.getParameter("redokeyid2"));
                    saveObject.put("redokeyid3", request.getParameter("redokeyid3"));
                }
                String calculatemodifiedtestsonly = requestContext.getProperty("calculatemodifiedtestsonly");
                String calculatemodifieddatasetsonly = requestContext.getProperty("calculatemodifieddatasetsonly");
                String hascalcexcludechanges = requestContext.getProperty("hascalcexcludechanges");
                saveObject.put("calculatemodifiedtestsonly", calculatemodifiedtestsonly);
                saveObject.put("calculatemodifieddatasetsonly", calculatemodifieddatasetsonly);
                saveObject.put("hascalcexcludechanges", hascalcexcludechanges);
                String string = sdcid = saveObject.isNull("sdcid") ? "" : saveObject.getString("sdcid");
                if (sdcKeycount.get(sdcid) == null) {
                    int keycount = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                    sdcKeycount.put(sdcid, keycount);
                }
                if ((jsonResponseObj = this.saveJSONObjectRequest(new ActionProcessor(connectionid), saveObject, auditreason, auditactivity, auditsignedflag, isSaveAndRelease, element)) != null) {
                    jsonResponseObj.write(response.getWriter());
                } else {
                    this.write("Failed to save data. No changes made, nothing to Save.");
                }
            }
        }
        catch (SapphireException e) {
            this.write("Failed to save data. " + e.getMessage());
        }
        catch (Exception e) {
            this.write("Failed to save data. " + e.getMessage());
        }
    }

    public JSONObject saveJSONObjectRequest(ActionProcessor actionProcessor, JSONObject saveObject, String auditreason, String auditactivity, String auditsignedflag, boolean isSaveAndRelease, PropertyList element) throws Exception {
        int i;
        DataSet touchDataSet;
        DataSet ds;
        String primarystatusColumn;
        boolean isSyncAQCStatus;
        boolean isSyncPrimaryStatus = element != null && !"N".equals(element.getProperty("syncprimarystatus"));
        ActionBlock actionBlock = JSONSaveRequest.buildDataEntryActionBlock(saveObject, isSaveAndRelease, isSyncPrimaryStatus, isSyncAQCStatus = element != null && !"N".equals(element.getProperty("syncaqcstatus")), primarystatusColumn = element != null ? element.getProperty("primarystatuscolumn") : "", ds = new DataSet(), touchDataSet = new DataSet(), this.getConnectionId());
        int actionCount = actionBlock.getActionCount();
        if (actionCount == 0) {
            return null;
        }
        JSONSaveRequest.setAuditProps(actionBlock, auditreason, auditactivity, auditsignedflag);
        try {
            actionProcessor.processActionBlock(actionBlock);
        }
        catch (ActionException ae) {
            String lastMessage = ae.getErrorHandler().getLastErrorMessage();
            if (lastMessage.charAt(lastMessage.length() - 1) == '|') {
                lastMessage = lastMessage.substring(0, lastMessage.length() - 1);
            }
            if (lastMessage.charAt(lastMessage.length() - 1) == '|') {
                lastMessage = lastMessage.substring(0, lastMessage.length() - 1);
            }
            if (lastMessage.indexOf("Reason:") > 0) {
                lastMessage = lastMessage.substring(lastMessage.indexOf("Reason:") + 7);
            }
            TranslationProcessor tp = new TranslationProcessor(actionProcessor.getConnectionid());
            throw new Exception("<p>" + tp.translate("Reason") + ":" + tp.translate(lastMessage) + "</p>");
        }
        HashSet<String> primarySet = new HashSet<String>();
        boolean nokeyid2 = "(null)".equals(ds.getValue(0, "keyid2"));
        boolean nokeyid3 = "(null)".equals(ds.getValue(0, "keyid3"));
        for (i = 0; i < ds.getRowCount(); ++i) {
            primarySet.add(ds.getValue(i, "keyid1") + (nokeyid2 ? "" : ";" + ds.getValue(i, "keyid2")) + (nokeyid3 ? "" : ";" + ds.getValue(i, "keyid3")));
        }
        for (i = 0; i < touchDataSet.getRowCount(); ++i) {
            primarySet.add(touchDataSet.getValue(i, "keyid1") + (nokeyid2 ? "" : ";" + touchDataSet.getValue(i, "keyid2")) + (nokeyid3 ? "" : ";" + touchDataSet.getValue(i, "keyid3")));
        }
        if (actionBlock.getDistinctActions().contains("EnterDataItem")) {
            HashMap enterDataItem = null;
            actionBlock.getActionProperties("EnterDataItem");
            if (enterDataItem != null) {
                DataSet allModifiedDataItems = (DataSet)enterDataItem.get("crosssdi_all_modifieddataitems");
                String allSDIsOnPage = saveObject.optString("dataentrypage_allsdis");
                if (allModifiedDataItems != null && allModifiedDataItems.size() > 0 && allSDIsOnPage.length() > 0) {
                    allModifiedDataItems.sort("keyid1,keyid2,keyid3");
                    String lastKey = "";
                    for (int i2 = 0; i2 < allModifiedDataItems.getRowCount(); ++i2) {
                        String key = allModifiedDataItems.getValue(i2, "keyid1") + ";" + allModifiedDataItems.getValue(i2, "keyid2", "(null)") + ";" + allModifiedDataItems.getValue(i2, "keyid3", "(null)");
                        if (key.equals(lastKey)) continue;
                        if (("|" + allSDIsOnPage + "|").contains(key)) {
                            primarySet.add(allModifiedDataItems.getValue(i2, "keyid1") + (nokeyid2 ? "" : ";" + allModifiedDataItems.getValue(i2, "keyid2")) + (nokeyid3 ? "" : ";" + allModifiedDataItems.getValue(i2, "keyid3")));
                        }
                        lastKey = key;
                    }
                }
            }
        }
        StringBuffer keyid1list = new StringBuffer();
        StringBuffer keyid2list = new StringBuffer();
        StringBuffer keyid3list = new StringBuffer();
        Iterator primaryitr = primarySet.iterator();
        String[] keys = null;
        while (primaryitr.hasNext()) {
            keys = StringUtil.split((String)primaryitr.next(), ";");
            keyid1list.append(";" + keys[0]);
            if (!nokeyid2) {
                keyid2list.append(";" + keys[1]);
            }
            if (nokeyid3) continue;
            keyid3list.append(";" + keys[2]);
        }
        JSONObject jsonResponseObj = null;
        String sdcid = saveObject.isNull("sdcid") ? "" : saveObject.getString("sdcid");
        String keyid1 = "";
        String keyid2 = "";
        String keyid3 = "";
        if (keyid1list.length() > 0) {
            keyid1 = keyid1list.substring(1);
            keyid2 = nokeyid2 ? "" : keyid2list.substring(1);
            String string = keyid3 = nokeyid3 ? "" : keyid3list.substring(1);
            if (element != null) {
                jsonResponseObj = this.retrieveJSONObjectCrossTabModel(actionProcessor.getConnectionid(), sdcid, keyid1, keyid2, keyid3, element);
            }
        }
        return jsonResponseObj;
    }

    public static ActionBlock buildDataEntryActionBlock(SDIMaint sdiMaint, HashMap props, String connectionId) throws Exception {
        if (sdiMaint.getDataset("dataitem") != null) {
            boolean isSaveAndRelease = props == null ? false : "Y".equals(props.get("saveAndRelease"));
            boolean isSyncPrimaryStatus = props == null ? false : "Y".equals(props.get("isSyncPrimaryStatus"));
            boolean isSyncAQCStatus = props == null ? false : "Y".equals(props.get("isSyncAQCStatus"));
            String primaryStatusColumn = props == null ? "" : (String)props.get("primaryStatusColumn");
            SDIDataItemTable sdidataitemTable = (SDIDataItemTable)sdiMaint.getDataset("dataitem");
            Table dataentry = sdidataitemTable.getDataentry();
            JSONObject columnIndexMap = new JSONObject();
            String[] cols = dataentry.getColumns();
            for (int i = 0; i < cols.length; ++i) {
                columnIndexMap.put(cols[i], i);
            }
            JSONObject rowStatusMap = new JSONObject();
            JSONObject rowValueMap = new JSONObject();
            for (int i = 0; i < dataentry.getRowCount(); ++i) {
                String rowstatus = dataentry.getValue(i, "rowstatus") == null ? "D" : dataentry.getValue(i, "rowstatus");
                rowStatusMap.put("" + i, rowstatus);
                JSONArray rowArray = new JSONArray();
                for (int c = 0; c < cols.length; ++c) {
                    rowArray.put(c, dataentry.getValue(i, cols[c]));
                }
                rowValueMap.put("" + i, rowArray);
            }
            JSONObject sdidataChanges = new JSONObject();
            JSONObject saveObject = new JSONObject();
            saveObject.put("sdcid", sdiMaint.getSdcid());
            saveObject.put("columns", columnIndexMap);
            saveObject.put("rowStatusMap", rowStatusMap);
            saveObject.put("rowValueMap", rowValueMap);
            saveObject.put("sdidataChanges", sdidataChanges);
            return JSONSaveRequest.buildDataEntryActionBlock(saveObject, isSaveAndRelease, isSyncPrimaryStatus, isSyncAQCStatus, primaryStatusColumn, null, null, connectionId);
        }
        return null;
    }

    public static ActionBlock buildDataEntryActionBlock(JSONObject saveObject, boolean isSaveAndRelease, boolean isSyncPrimaryStatus, boolean isSyncAQCStatus, String primaryStatusColumn, DataSet ds, DataSet touchDataSet, String connectionId) throws Exception {
        int i;
        JSONObject columnIndexMap = saveObject.isNull("columns") ? null : saveObject.getJSONObject("columns");
        JSONObject rowStatusMap = saveObject.isNull("rowStatusMap") ? null : saveObject.getJSONObject("rowStatusMap");
        JSONObject rowValueMap = saveObject.isNull("rowValueMap") ? null : saveObject.getJSONObject("rowValueMap");
        String sdcid = saveObject.isNull("sdcid") ? "" : saveObject.getString("sdcid");
        String redosdcid = saveObject.isNull("redosdcid") ? "" : saveObject.getString("redosdcid");
        String redokeyid1 = saveObject.isNull("redokeyid1") ? "" : saveObject.getString("redokeyid1");
        String redokeyid2 = saveObject.isNull("redokeyid2") ? "" : saveObject.getString("redokeyid2");
        String redokeyid3 = saveObject.isNull("redokeyid3") ? "" : saveObject.getString("redokeyid3");
        String calculatemodifieddatasetsonly = saveObject.isNull("calculatemodifieddatasetsonly") ? "" : saveObject.getString("calculatemodifieddatasetsonly");
        String calculatemodifiedtestsonly = saveObject.isNull("calculatemodifiedtestsonly") ? "" : saveObject.getString("calculatemodifiedtestsonly");
        String hascalcexcludechanges = saveObject.isNull("hascalcexcludechanges") ? "" : saveObject.getString("hascalcexcludechanges");
        JSONObject sdidataChanges = saveObject.getJSONObject("sdidataChanges");
        JSONArray updatedataitemcolumnlist = sdidataChanges.isNull("updatedataitemcolumnlist") ? new JSONArray() : sdidataChanges.getJSONArray("updatedataitemcolumnlist");
        ActionBlock actionBlock = new ActionBlock();
        Iterator itr = rowValueMap.keys();
        PropertyList enterdataitemPl = new PropertyList();
        String[] diKeycolids = new SDIData().getKeys("dataitem");
        if (ds == null) {
            ds = new DataSet();
        }
        if (touchDataSet == null) {
            touchDataSet = new DataSet();
        }
        for (int i2 = 1; i2 < diKeycolids.length; ++i2) {
            ds.addColumn(diKeycolids[i2], 0);
        }
        ds.addColumn("enteredtext", 0);
        ds.addColumn("calcexcludeflag", 0);
        ds.addColumn("updatestatus", 0);
        ds.addColumn("editstatus", 0);
        ds.addColumn("s_acoverriddenflagoriginal", 0);
        String[] editcolumns = new String[updatedataitemcolumnlist.length()];
        for (i = 0; i < editcolumns.length; ++i) {
            editcolumns[i] = updatedataitemcolumnlist.getString(i);
        }
        if (editcolumns.length == 0) {
            StringBuilder sb = new StringBuilder();
            Iterator it = columnIndexMap.keys();
            while (it.hasNext()) {
                String columnid = it.next().toString();
                sb.append(";" + columnid);
            }
            editcolumns = StringUtil.split(sb.substring(1), ";");
        }
        if (editcolumns.length > 0) {
            for (i = 0; i < editcolumns.length; ++i) {
                ds.addColumn(editcolumns[i], 0);
            }
        }
        int row = 0;
        while (itr.hasNext()) {
            int i3;
            String key = (String)itr.next();
            String rowStatus = rowStatusMap.getString(key);
            JSONArray currentrow = rowValueMap.getJSONArray(key);
            ds.addRow();
            for (i3 = 1; i3 < diKeycolids.length; ++i3) {
                ds.setString(row, diKeycolids[i3], currentrow.getString(columnIndexMap.getInt(diKeycolids[i3])));
            }
            if ("D".equals(rowStatus) || "B".equals(rowStatus)) {
                ds.setString(row, "updatestatus", "D");
            }
            if ("U".equals(rowStatus) || "B".equals(rowStatus)) {
                ds.setString(row, "editstatus", "U");
            }
            ds.setString(row, "enteredtext", currentrow.getString(columnIndexMap.getInt("enteredtext")));
            ds.setString(row, "calcexcludeflag", currentrow.getString(columnIndexMap.getInt("calcexcludeflag")));
            ds.setString(row, "s_acoverriddenflagoriginal", currentrow.getString(columnIndexMap.getInt("s_acoverriddenflagoriginal")));
            for (i3 = 0; i3 < editcolumns.length; ++i3) {
                ds.setString(row, editcolumns[i3], currentrow.getString(columnIndexMap.getInt(editcolumns[i3])));
            }
            ++row;
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("updatestatus", "D");
        DataSet dsEnterDataItem = ds.getFilteredDataSet(filter);
        if (dsEnterDataItem.getRowCount() > 0) {
            for (int i4 = 1; i4 < diKeycolids.length; ++i4) {
                enterdataitemPl.setProperty(diKeycolids[i4], dsEnterDataItem.getColumnValues(diKeycolids[i4], ";"));
            }
            enterdataitemPl.setProperty("enteredtext", dsEnterDataItem.getColumnValues("enteredtext", ";"));
            if (!"true".equals(hascalcexcludechanges)) {
                enterdataitemPl.setProperty("calcexcludeflag", "");
            } else {
                enterdataitemPl.setProperty("calcexcludeflag", dsEnterDataItem.getColumnValues("calcexcludeflag", ";"));
            }
            enterdataitemPl.setProperty("propsmatch", "Y");
            enterdataitemPl.setProperty("sdcid", sdcid);
            enterdataitemPl.setProperty("autorelease", isSaveAndRelease ? "Y" : "N");
            if (redosdcid != null && redosdcid.length() > 0) {
                enterdataitemPl.setProperty("redosdcid", redosdcid);
                enterdataitemPl.setProperty("redokeyid1", redokeyid1);
                enterdataitemPl.setProperty("redokeyid2", redokeyid2);
                enterdataitemPl.setProperty("redokeyid3", redokeyid3);
            }
            if (calculatemodifiedtestsonly.length() > 0) {
                enterdataitemPl.setProperty("calculatemodifiedtestsonly", calculatemodifiedtestsonly);
            }
            if (calculatemodifieddatasetsonly.length() > 0) {
                enterdataitemPl.setProperty("calculatemodifieddatasetsonly", calculatemodifieddatasetsonly);
            }
        }
        filter = new HashMap();
        filter.put("editstatus", "U");
        DataSet dsEditDataItem = ds.getFilteredDataSet(filter);
        PropertyList editDataItemPl = new PropertyList();
        HashSet<String> unreleasedSet = null;
        if (dsEditDataItem.getRowCount() > 0) {
            int i5;
            for (i5 = 1; i5 < diKeycolids.length; ++i5) {
                editDataItemPl.setProperty(diKeycolids[i5], dsEditDataItem.getColumnValues(diKeycolids[i5], ";"));
            }
            editDataItemPl.setProperty("propsmatch", "Y");
            editDataItemPl.setProperty("sdcid", sdcid);
            for (i5 = 0; i5 < editcolumns.length; ++i5) {
                if (!ds.isValidColumn(editcolumns[i5]) || "displayvalue".equals(editcolumns[i5]) || "isusercertified".equals(editcolumns[i5]) || "s_acoverriddenflagoriginal".equals(editcolumns[i5]) || "speccondition".equals(editcolumns[i5])) continue;
                String values = dsEditDataItem.getColumnValues(editcolumns[i5], ";");
                editDataItemPl.setProperty(editcolumns[i5], values);
                if (sdidataChanges.isNull("dataapproval") || !"releasedflag".equals(editcolumns[i5]) || values.indexOf("N") < 0) continue;
                unreleasedSet = new HashSet<String>();
                for (int r = 0; r < dsEditDataItem.getRowCount(); ++r) {
                    if (!"N".equals(dsEditDataItem.getValue(r, "releasedflag"))) continue;
                    unreleasedSet.add(dsEditDataItem.getValue(r, "keyid1") + ";" + dsEditDataItem.getValue(r, "keyid2") + ";" + dsEditDataItem.getValue(r, "keyid3") + ";" + dsEditDataItem.getValue(r, "paramlistid") + ";" + dsEditDataItem.getValue(r, "paramlistversionid") + ";" + dsEditDataItem.getValue(r, "variantid") + ";" + dsEditDataItem.getValue(r, "dataset"));
                }
            }
        }
        if (isSaveAndRelease) {
            if (dsEditDataItem.getRowCount() > 0) {
                actionBlock.setAction("EditDataItemGrid", "EditDataItem", "1", editDataItemPl);
            }
            if (dsEnterDataItem.getRowCount() > 0) {
                actionBlock.setAction("EnterDataItem", "EnterDataItem", "1", enterdataitemPl);
            }
        } else {
            enterdataitemPl.setProperty("hasunrelease", "Y");
            if (dsEnterDataItem.getRowCount() > 0) {
                actionBlock.setAction("EnterDataItem", "EnterDataItem", "1", enterdataitemPl);
            }
            if (dsEditDataItem.getRowCount() > 0) {
                actionBlock.setAction("EditDataItemGrid", "EditDataItem", "1", editDataItemPl);
            }
        }
        touchDataSet.addColumn("keyid1", 0);
        touchDataSet.addColumn("keyid2", 0);
        touchDataSet.addColumn("keyid3", 0);
        touchDataSet.addColumn("paramlistid", 0);
        touchDataSet.addColumn("paramlistversionid", 0);
        touchDataSet.addColumn("variantid", 0);
        touchDataSet.addColumn("dataset", 0);
        String[] dskeys = new String[]{"keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset"};
        String[] details = new String[]{"dataset", "dataitem", "dataapproval"};
        for (int d = 0; d < details.length; ++d) {
            String detailname = details[d];
            String datasetname = detailname.toLowerCase();
            if (sdidataChanges == null || sdidataChanges.length() <= 0 || sdidataChanges.isNull(detailname)) continue;
            JSONObject datasetJSON = sdidataChanges.getJSONObject(detailname);
            JSONArray updatecolumnlist = datasetJSON.getJSONArray("updatecolumnlist");
            datasetJSON.remove("updatecolumnlist");
            if (updatecolumnlist == null || updatecolumnlist.length() <= 0) continue;
            String[] dsKeycolids = new SDIData().getKeys(datasetname);
            Iterator keyItr = datasetJSON.keys();
            StringBuffer[] keyvalues = new StringBuffer[dsKeycolids.length];
            String[] columnids = new String[updatecolumnlist.length()];
            StringBuffer[] values = new StringBuffer[columnids.length];
            int rowindex = 0;
            while (keyItr.hasNext()) {
                String dskey = (String)keyItr.next();
                String[] dsKeyValues = StringUtil.split(dskey, ";");
                for (int k = 0; k < dsKeyValues.length; ++k) {
                    if (keyvalues[k] == null) {
                        keyvalues[k] = new StringBuffer();
                    }
                    keyvalues[k].append(";" + dsKeyValues[k]);
                    if (k >= 7) continue;
                    touchDataSet.addRow();
                    touchDataSet.setValue(rowindex, dskeys[k], dsKeyValues[k]);
                }
                JSONObject rowObject = datasetJSON.getJSONObject(dskey);
                for (int c = 0; c < updatecolumnlist.length(); ++c) {
                    String columnid = updatecolumnlist.getString(c);
                    String value = rowObject.getString(columnid);
                    if (rowindex == 0) {
                        columnids[c] = columnid;
                        values[c] = new StringBuffer();
                    }
                    value = value.indexOf(";") >= 0 ? StringUtil.replaceAll(value, ";", "#semicolon#") : value;
                    values[c].append(";" + value);
                }
                ++rowindex;
            }
            PropertyList editdatasetprops = new PropertyList();
            for (int k = 0; k < dsKeycolids.length - 1; ++k) {
                editdatasetprops.setProperty(dsKeycolids[k + 1], keyvalues[k].substring(1));
            }
            for (int c = 0; c < columnids.length; ++c) {
                if (columnids[c] == null) continue;
                editdatasetprops.setProperty(columnids[c], values[c].substring(1));
            }
            editdatasetprops.setProperty("sdcid", sdcid);
            editdatasetprops.setProperty("propsmatch", "Y");
            if ("dataset".equals(datasetname)) {
                actionBlock.setAction("EditDataSet", "EditDataSet", "1", editdatasetprops);
                continue;
            }
            if ("dataitem".equals(datasetname)) {
                actionBlock.setAction("EditDataItem", "EditDataItem", "1", editdatasetprops);
                continue;
            }
            if (!"dataapproval".equals(datasetname)) continue;
            if (unreleasedSet != null) {
                String[] approvalflags = StringUtil.split(editdatasetprops.getProperty("approvalflag"), ";");
                for (int i6 = 0; i6 < approvalflags.length; ++i6) {
                    String datasetKey;
                    if (!"P".equals(approvalflags[i6]) || !unreleasedSet.contains(datasetKey = StringUtil.split(editdatasetprops.getProperty("keyid1"), ";")[i6] + ";" + StringUtil.split(editdatasetprops.getProperty("keyid2"), ";")[i6] + ";" + StringUtil.split(editdatasetprops.getProperty("keyid3"), ";")[i6] + ";" + StringUtil.split(editdatasetprops.getProperty("paramlistid"), ";")[i6] + ";" + StringUtil.split(editdatasetprops.getProperty("paramlistversionid"), ";")[i6] + ";" + StringUtil.split(editdatasetprops.getProperty("variantid"), ";")[i6] + ";" + StringUtil.split(editdatasetprops.getProperty("dataset"), ";")[i6])) continue;
                    throw new SapphireException(new TranslationProcessor(connectionId).translate("Error: You are approving data set you just unreleased! (" + datasetKey + ")"));
                }
            }
            actionBlock.setAction("EditDataApproval", "EditDataApproval", "1", editdatasetprops);
        }
        String touchedkeyid1 = touchDataSet.getColumnValues("keyid1", ";");
        String touchedkeyid2 = touchDataSet.getColumnValues("keyid2", ";");
        String touchedkeyid3 = touchDataSet.getColumnValues("keyid3", ";");
        String dataentrykeyid1 = ds.getColumnValues("keyid1", ";");
        String dataentrykeyid2 = ds.getColumnValues("keyid2", ";");
        String dataentrykeyid3 = ds.getColumnValues("keyid3", ";");
        String dataentryOrEditkeyid1 = dataentrykeyid1 + (touchedkeyid1.length() > 0 ? ";" + touchedkeyid1 : "");
        String dataentryOrEditkeyid2 = dataentrykeyid2 + (touchedkeyid2.length() > 0 ? ";" + touchedkeyid2 : "");
        String dataentryOrEditkeyid3 = dataentrykeyid3 + (touchedkeyid3.length() > 0 ? ";" + touchedkeyid3 : "");
        int keycolscount = sdcKeycount.get(sdcid) != null ? (Integer)sdcKeycount.get(sdcid) : 1;
        boolean execCorePostSaveActions = true;
        if (execCorePostSaveActions && dataentryOrEditkeyid1.length() > 0) {
            String keyId2 = keycolscount >= 2 ? dataentryOrEditkeyid2 : "(null)";
            String keyId3 = keycolscount >= 3 ? dataentryOrEditkeyid3 : "(null)";
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", dataentryOrEditkeyid1);
            props.setProperty("keyid2", keyId2);
            props.setProperty("keyid3", keyId3);
            actionBlock.setAction("UpdateDataSetStatus", "UpdateDataSetStatus", "1", props);
            PropertyList syncSDIWIprops = new PropertyList();
            syncSDIWIprops.setProperty("sdcid", sdcid);
            syncSDIWIprops.setProperty("keyid1", dataentryOrEditkeyid1);
            syncSDIWIprops.setProperty("keyid2", keyId2);
            syncSDIWIprops.setProperty("keyid3", keyId3);
            actionBlock.setAction("SyncSDIWIStatus", "SyncSDIWIStatus", "1", syncSDIWIprops);
            if (isSyncPrimaryStatus && keycolscount == 1) {
                props = new PropertyList();
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", dataentryOrEditkeyid1);
                props.setProperty("keyid2", "(null)");
                props.setProperty("keyid3", "(null)");
                props.setProperty("sdistatus", "[sdistatus]");
                if (primaryStatusColumn.length() > 0) {
                    props.setProperty("statuscolid", primaryStatusColumn);
                }
                actionBlock.setAction("SyncSDIDataSetStatus", "SyncSDIDataSetStatus", "1", props);
            }
            if (isSyncAQCStatus && keycolscount == 1) {
                props = new PropertyList();
                String paramlistid = touchDataSet.getColumnValues("paramlistid", ";");
                String paramlistversionid = touchDataSet.getColumnValues("paramlistversionid", ";");
                String variantid = touchDataSet.getColumnValues("variantid", ";");
                String dataset = touchDataSet.getColumnValues("dataset", ";");
                String qcParamListIds = ds.getColumnValues("paramlistid", ";") + (paramlistid.length() > 0 ? ";" + paramlistid : "");
                String qcParamListVersionIds = ds.getColumnValues("paramlistversionid", ";") + (paramlistversionid.length() > 0 ? ";" + paramlistversionid : "");
                String qcVariantIds = ds.getColumnValues("variantid", ";") + (variantid.length() > 0 ? ";" + variantid : "");
                String qcDataSets = ds.getColumnValues("dataset", ";") + (dataset.length() > 0 ? ";" + dataset : "");
                props.setProperty("sdcid", sdcid);
                props.setProperty("keyid1", dataentryOrEditkeyid1);
                props.setProperty("paramlistid", qcParamListIds);
                props.setProperty("paramlistversionid", qcParamListVersionIds);
                props.setProperty("variantid", qcVariantIds);
                props.setProperty("dataset", qcDataSets);
                props.setProperty("saveandrelease", isSaveAndRelease ? "Y" : "N");
                actionBlock.setAction("ProcessQCBatch", "ProcessQCBatch", "1", props);
                String qcBatchIds = QCUtil.getLinkedQCBatchIds(sdcid, dataentryOrEditkeyid1, qcParamListIds, qcParamListVersionIds, qcVariantIds, qcDataSets, new QueryProcessor(connectionId));
                if (qcBatchIds.length() > 0) {
                    props = new PropertyList();
                    props.setProperty("sdcid", "QCBatch");
                    props.setProperty("keyid1", qcBatchIds);
                    props.put("postdataentry", "Y");
                    actionBlock.setAction("UpdateQCBatchStatus", "UpdateQCBatchStatus", "1", props);
                }
            }
        }
        return actionBlock;
    }

    public JSONObject retrieveJSONObjectCrossTabModel(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, PropertyList element) throws Exception {
        SDIProcessor sdiProcessor = new SDIProcessor(connectionid);
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        JSONObject jsonResponseObj = null;
        if (keyid1.length() > 0) {
            sdiRequest.setKeyid1List(keyid1);
            sdiRequest.setKeyid2List(keyid2 == null || keyid2.length() == 0 ? "" : keyid2);
            sdiRequest.setKeyid3List(keyid3 == null || keyid3.length() == 0 ? "" : keyid3);
            String requestStr = GWTDataEntry.buildRequest(element);
            if (requestStr.indexOf("[currentuser]") >= 0) {
                requestStr = StringUtil.replaceAll(requestStr, "[currentuser]", this.getConnectionProcessor().getConnectionInfo(connectionid).getSysuserId());
            }
            String[] datarequest = RequestParser.parseRequestItem(requestStr);
            for (int i = 0; i < datarequest.length; ++i) {
                sdiRequest.setRequestItem(datarequest[i].trim());
            }
            sdiRequest.setRetrieve(true);
            SDIData sdiInfo = sdiProcessor.getSDIData(sdiRequest);
            DataSet sdidataitemspec = sdiInfo.getDataset("dataspec");
            DataSet sdidata = sdiInfo.getDataset("dataset");
            DataSet primary = sdiInfo.getDataset("primary");
            DataSet sdidataitem = sdiInfo.getDataset("dataitem");
            sdidataitem.addColumn("protection", 0);
            GWTDataEntry.appendSecondarySDCDataSets(false, element, element.getPropertyList("secondarysdc"), primary, sdidata, sdidataitem, sdidataitemspec, this.getSDIProcessor(), this.getTranslationProcessor());
            DataItemCrossTabModel model = new DataItemCrossTabModel(element, null, connectionid);
            jsonResponseObj = model.toJSONObjectCrossTabModel(sdidataitem, sdidata, primary, sdidataitemspec);
            jsonResponseObj.put("sdcid", sdcid);
        }
        return jsonResponseObj;
    }

    private static void setAuditProps(ActionBlock actionBlock, String auditreason, String auditactivity, String auditsignedflag) throws Exception {
        int count = actionBlock.getActionCount();
        for (int i = 0; i < count; ++i) {
            actionBlock.setActionProperty(actionBlock.getActionName(i), "auditreason", auditreason);
            actionBlock.setActionProperty(actionBlock.getActionName(i), "auditactivity", auditactivity);
            actionBlock.setActionProperty(actionBlock.getActionName(i), "auditsignedflag", auditsignedflag);
        }
    }
}

