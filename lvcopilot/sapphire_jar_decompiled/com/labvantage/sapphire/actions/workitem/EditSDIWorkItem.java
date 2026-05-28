/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workitem;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.eln.GenerateTestMethodWorksheet;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.actions.wap.RemoveActivityWorkSDI;
import com.labvantage.sapphire.actions.wap.SetActivityStatus;
import com.labvantage.sapphire.actions.wap.UpdateActivityCompleteCount;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostEditWorkItemEventObject;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.WorkItemItemRuleEvaluator;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditSDIWorkItem
extends BaseAction
implements sapphire.action.EditSDIWorkItem {
    static final String LABVANTAGE_CVS_ID = "$Revision: 83737 $";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        DAMProcessor dam = this.getDAMProcessor();
        String sdcid = properties.getProperty("sdcid");
        String keyid1all = properties.getProperty("keyid1");
        String keyid2all = properties.getProperty("keyid2");
        String keyid3all = properties.getProperty("keyid3");
        String workItemIdall = properties.getProperty("workitemid");
        String workItemInstanceall = properties.getProperty("workiteminstance");
        boolean propsMatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        boolean applyLock = StringUtil.getYN(properties.getProperty("applylock", "N"), "N").equals("Y");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcProps = sdcProcessor.getPropertyList(sdcid);
        sdcid = sdcProps.getProperty("sdcid");
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        if (sdcid.length() == 0 || keyid1all.length() == 0 || workItemIdall.length() == 0 || workItemInstanceall.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Missing mandatory input"));
        }
        String rsetid = BaseSDIDataAction.createRSet(sdcid, keyid1all, keyid2all, keyid3all, this.database, this.connectionInfo, applyLock);
        if (rsetid.length() <= 0) throw new SapphireException("GENERAL_ERROR", tp.translate("Failed to create rset."));
        try {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer tableDefSql = new StringBuffer("SELECT columnid, datatype FROM syscolumn WHERE tableid = 'sdiworkitem' AND pkflag='N'");
            tableDefSql.append("AND columnid NOT IN (").append(safeSQL.addIn(DataSetUtil.getStandardSysColsClause(), "','")).append(")");
            DataSet tabledef = qp.getPreparedSqlDataSet(tableDefSql.toString(), safeSQL.getValues());
            String[] keyid1prop = StringUtil.split(keyid1all, ";");
            String[] keyid2prop = StringUtil.split(keyid2all, ";");
            String[] keyid3prop = StringUtil.split(keyid3all, ";");
            String[] workitemIdProp = StringUtil.split(workItemIdall, ";");
            String[] workItemInstanceProp = StringUtil.split(workItemInstanceall, ";");
            String[][] colvalprop = new String[tabledef.getRowCount()][];
            if (workitemIdProp.length != workItemInstanceProp.length) {
                throw new SapphireException("INVALID_PROPERTY", tp.translate("Missing mandatory input"));
            }
            DataSet sdiWorkItems = new DataSet(this.connectionInfo);
            block11: for (int col = 0; col < tabledef.getRowCount(); ++col) {
                String columnid = tabledef.getString(col, "columnid");
                String value = properties.getProperty(columnid);
                if (!properties.containsKey(columnid)) continue;
                colvalprop[col] = StringUtil.split(value, ";");
                switch (tabledef.getString(col, "datatype").charAt(0)) {
                    case 'C': {
                        sdiWorkItems.addColumn(columnid, 0);
                        continue block11;
                    }
                    case 'N': {
                        sdiWorkItems.addColumn(columnid, 1);
                        continue block11;
                    }
                    case 'R': {
                        sdiWorkItems.addColumn(columnid, 1);
                        continue block11;
                    }
                    case 'D': {
                        sdiWorkItems.addColumn(columnid, 2);
                        if (!"Y".equals(this.getSDCProcessor().getSDCColumnProperty("SDIWorkItem", columnid, "timezoneindependent"))) continue block11;
                        sdiWorkItems.setTimeZoneInsensitive(columnid);
                    }
                }
            }
            DataSet sdiWIForms = new DataSet(this.connectionInfo);
            DataSet workitemForms = null;
            boolean createWorksheetInputProp = StringUtil.getYN(properties.getProperty("createworksheet", "Y"), "Y").equals("Y");
            boolean createWorksheet = false;
            StringBuffer worksheetPresent = new StringBuffer();
            DataSet sdiWorkItemExistingDS = null;
            DataSet allWorkItemParamLists = this.getWorkItemParamLists(rsetid, workitemIdProp, workItemInstanceProp);
            DataSet sdiDataDS = new DataSet(this.connectionInfo);
            DataSet sdiDataColList = WorkItemUtil.getEditableColumnList(this.database, this.logger, "sdidata");
            sdiDataColList = WorkItemUtil.filterColumnIdsWithPropList(this.logger, sdiDataColList, properties);
            for (int i = 0; i < keyid1prop.length; ++i) {
                String keyid1 = keyid1prop[i];
                String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                for (int wi = 0; wi < workitemIdProp.length; ++wi) {
                    if (propsMatch) {
                        wi = i;
                    }
                    String workItemId = workitemIdProp[wi];
                    String workItemInstance = workItemInstanceProp.length == 0 || workItemInstanceProp.length < workitemIdProp.length || workItemInstanceProp[wi].length() == 0 ? "" : workItemInstanceProp[wi];
                    int newrow = sdiWorkItems.addRow();
                    sdiWorkItems.setString(newrow, "sdcid", sdcid);
                    sdiWorkItems.setString(newrow, "keyid1", keyid1);
                    sdiWorkItems.setString(newrow, "keyid2", keyid2);
                    sdiWorkItems.setString(newrow, "keyid3", keyid3);
                    sdiWorkItems.setString(newrow, "workItemId", workItemId);
                    sdiWorkItems.setNumber(newrow, "workiteminstance", Integer.parseInt(workItemInstance));
                    sdiWorkItems.setString(newrow, "modby", this.connectionInfo.getSysuserId());
                    sdiWorkItems.setString(newrow, "modtool", this.connectionInfo.getTool());
                    for (int col = 0; col < tabledef.getRowCount(); ++col) {
                        String colval = "";
                        String columnid = tabledef.getString(col, "columnid");
                        if (colvalprop[col] == null || colvalprop[col].length <= 0) continue;
                        String string = colval = colvalprop[col].length > wi ? colvalprop[col][wi] : colvalprop[col][colvalprop[col].length - 1];
                        if (colval.equals("(null)") || colval.equals("(none)")) {
                            colval = "";
                        }
                        sdiWorkItems.setValue(newrow, columnid, colval);
                    }
                    if (createWorksheetInputProp) {
                        if (sdiWorkItemExistingDS == null) {
                            String selectSDIWorkItems = "SELECT\tsdiwi.sdcid, sdiwi.keyid1, sdiwi.keyid2, sdiwi.keyid3, sdiwi.workitemid, sdiwi.workitemversionid, sdiwi.workiteminstance, sdiwi.documentid FROM\tsdiworkitem sdiwi, rsetitems WHERE\trsetitems.rsetid = ? AND \t\trsetitems.sdcid  = sdiwi.sdcid AND \t\trsetitems.keyid1 = sdiwi.keyid1 AND \t\trsetitems.keyid2 = sdiwi.keyid2 AND \t\trsetitems.keyid3 = sdiwi.keyid3 ";
                            this.database.createPreparedResultSet("SelectworkitemsFromRsetitem", selectSDIWorkItems, new Object[]{rsetid});
                            sdiWorkItemExistingDS = new DataSet(this.database.getResultSet("SelectworkitemsFromRsetitem"));
                        }
                        if (workitemForms == null) {
                            String rsetWorkItems = this.getDAMProcessor().createRSet("WorkItem", workItemIdall, "", "", true, 1);
                            workitemForms = WorkItemUtil.getWorkItemForms(workItemIdall, "", this.getQueryProcessor(), this.logger, rsetWorkItems);
                            dam.clearRSet(rsetWorkItems);
                        }
                        createWorksheet = this.prepareSDIWIFormRow(sdcid, keyid1, keyid2, keyid3, workItemId, workItemInstance, workitemForms, sdiWIForms, properties, worksheetPresent, sdiWorkItemExistingDS, sdiWorkItems, newrow);
                    }
                    this.populateEditDataSetDS(sdiDataDS, sdiWorkItems, newrow, wi, allWorkItemParamLists, sdiDataColList, properties, createWorksheet);
                    if (!propsMatch) continue;
                    wi = workitemIdProp.length;
                }
            }
            if (worksheetPresent.length() > 0) {
                this.setInfoError(this.getTranslationProcessor().translate("Worksheet already bound to the following sdiworkitems:") + " -" + worksheetPresent.substring(1) + "<br>" + this.getTranslationProcessor().translate("Continuing with assignment."));
            }
            if (properties.getProperty("tracelogid", "").trim().length() == 0) {
                String traceLogId = this.getTracelogid(sdcid, "Edited data in sdiworkitem", properties.getProperty("auditreason"), properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt"));
                sdiWorkItems.setString(-1, "tracelogid", traceLogId);
                properties.setProperty("tracelogid", traceLogId);
            } else {
                sdiWorkItems.setString(-1, "tracelogid", properties.getProperty("tracelogid", "").trim());
            }
            BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, "PreEditWorkItem");
            SDIData sdiData = new SDIData();
            sdiData.setDataset("sdiworkitem", sdiWorkItems);
            SDIData beforeEditImage = null;
            boolean requiresEditWorkItemPrimary = sdcPreRules.requiresEditWorkItemPrimary() || sdcPreRules.customRulesRequiresEditWorkItemPrimary();
            boolean requiresBeforeEditSDIDataImage = sdcPreRules.requiresBeforeEditWorkItemImage() || sdcPreRules.customRulesRequiresBeforeEditWorkItemImage();
            PostEditWorkItemEventObject postEditWorkItemEventObject = new PostEditWorkItemEventObject(sdcid, sdcProps, sdiData, properties);
            boolean requiresSupplementalData = EventManager.requiresSupplementalData(sapphireConnection, this.getErrorHandler(), postEditWorkItemEventObject);
            boolean bl = requiresBeforeEditSDIDataImage = requiresBeforeEditSDIDataImage || properties.containsKey("workitemstatus");
            if (requiresEditWorkItemPrimary || requiresBeforeEditSDIDataImage || requiresSupplementalData) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setRsetid(rsetid);
                sdiRequest.setRetainRsetid(true);
                if (requiresEditWorkItemPrimary || requiresSupplementalData) {
                    sdiRequest.setRequestItem("primary");
                }
                if (requiresBeforeEditSDIDataImage || requiresSupplementalData) {
                    sdiRequest.setRequestItem("sdiworkitem");
                    sdiRequest.setRequestItem("sdiworkitemitem");
                }
                SDIProcessor sdiProcessor = this.getSDIProcessor();
                beforeEditImage = sdiProcessor.getSDIData(sdiRequest);
                sdcPreRules.setBeforeEditImage(beforeEditImage);
                sdiData.setDataset("primary", beforeEditImage.getDataset("primary"));
                postEditWorkItemEventObject.setSupplementalData(beforeEditImage);
                postEditWorkItemEventObject.setRsetid(rsetid);
            }
            if (beforeEditImage != null && sdiWorkItems.isValidColumn("workitemstatus")) {
                this.setWapStatusToPendingOnUncancel(sdiWorkItems, sdcPreRules);
                this.setWapStatusToCancel(sdiWorkItems, sdcPreRules);
                this.setCompleteDtCompleteByNull(sdiWorkItems, sdcPreRules);
            }
            Trace.startBusinessRule(sdcid + "." + "PreEditWorkItem", true);
            sdcPreRules.preEditWorkItem(sdiData, properties);
            Trace.endBusinessRule(sdcid + "." + "PreEditWorkItem", true);
            Trace.startBusinessRule(sdcid + "." + "PreEditWorkItem", false);
            for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                customRules.preEditWorkItem(sdiData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PreEditWorkItem", false);
            sdcPreRules.endRule();
            DataSetUtil.update(this.database, sdiWorkItems, "sdiworkitem", new SDIData().getKeys("sdiworkitem"));
            if (sdiWorkItems.getColumnType("workitemstatus") != -1) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("workitemstatus", "Cancelled");
                DataSet cancelled = sdiWorkItems.getFilteredDataSet(filter);
                if (cancelled.size() > 0) {
                    StringBuffer sdiworkitemid = new StringBuffer();
                    for (int i = 0; i < cancelled.size(); ++i) {
                        String id;
                        this.database.createPreparedResultSet("SELECT sdiworkitemid FROM sdiworkitem WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ?", new Object[]{sdcid, cancelled.getString(i, "keyid1"), cancelled.getString(i, "keyid2"), cancelled.getString(i, "keyid3"), cancelled.getString(i, "workitemid"), cancelled.getBigDecimal(i, "workiteminstance")});
                        if (!this.database.getNext() || (id = this.database.getString("sdiworkitemid")) == null || id.length() <= 0) continue;
                        sdiworkitemid.append(";").append(id);
                    }
                    if (sdiworkitemid.length() > 0) {
                        PropertyList wf = new PropertyList();
                        wf.setProperty("sdcid", "SDIWorkItem");
                        wf.setProperty("keyid1", sdiworkitemid.substring(1));
                        this.getActionProcessor().processAction("RemoveFromWorkflow", "1", wf);
                    }
                }
            }
            WorkItemUtil.createWorksheet(sdiWIForms, this.getActionProcessor(), this.logger);
            EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), postEditWorkItemEventObject);
            BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, "PostEditWorkItem");
            sdcPostRules.setBeforeEditImage(beforeEditImage);
            if (beforeEditImage != null) {
                WorkItemItemRuleEvaluator ruleProcessor = new WorkItemItemRuleEvaluator();
                if (properties.containsKey("workitemstatus")) {
                    ruleProcessor.evaluateRuleOnSDIWorkItemStatusUpdate(sdcProcessor, sdcid, beforeEditImage, sdiData, this.database, this.connectionInfo);
                }
                this.autoStartStopWapActivity(sdiData.getDataset("sdiworkitem"), this.database, sdcPostRules);
            }
            if (!"Y".equalsIgnoreCase(properties.getProperty("postworksheetcreation")) && properties.containsKey("s_assignedanalyst")) {
                this.createLESWorksheetOnAssignment(sdiWorkItems);
            }
            Trace.startBusinessRule(sdcid + "." + "PostEditWorkItem", true);
            sdcPreRules.postEditWorkItem(sdiData, properties);
            Trace.endBusinessRule(sdcid + "." + "PostEditWorkItem", true);
            Trace.startBusinessRule(sdcid + "." + "PostEditWorkItem", false);
            for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                customRules.postEditWorkItem(sdiData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PostEditWorkItem", false);
            sdcPostRules.endRule();
            if (sdiDataDS == null || sdiDataDS.getRowCount() <= 0) return;
            sdiDataDS.sort("createworksheet");
            ArrayList<DataSet> groupedSDIDatas = sdiDataDS.getGroupedDataSets("createworksheet");
            PropertyList editDataSetProps = new PropertyList();
            for (DataSet tempSDIDataDS : groupedSDIDatas) {
                editDataSetProps.clear();
                editDataSetProps.setProperty("sdcid", sdcid);
                editDataSetProps.setProperty("keyid1", tempSDIDataDS.getColumnValues("keyid1", ";"));
                editDataSetProps.setProperty("keyid2", tempSDIDataDS.getColumnValues("keyid2", ";"));
                editDataSetProps.setProperty("keyid3", tempSDIDataDS.getColumnValues("keyid3", ";"));
                editDataSetProps.setProperty("paramlistid", tempSDIDataDS.getColumnValues("paramlistid", ";"));
                editDataSetProps.setProperty("paramlistversionid", tempSDIDataDS.getColumnValues("paramlistversionid", ";"));
                editDataSetProps.setProperty("variantid", tempSDIDataDS.getColumnValues("variantid", ";"));
                editDataSetProps.setProperty("dataset", tempSDIDataDS.getColumnValues("dataset", ";"));
                if (tempSDIDataDS.isValidColumn("s_assignedanalyst")) {
                    editDataSetProps.setProperty("s_assignedanalyst", tempSDIDataDS.getColumnValues("s_assignedanalyst", ";"));
                }
                if (tempSDIDataDS.isValidColumn("s_assigneddepartment")) {
                    editDataSetProps.setProperty("s_assigneddepartment", tempSDIDataDS.getColumnValues("s_assigneddepartment", ";"));
                }
                editDataSetProps.setProperty("propsmatch", "Y");
                editDataSetProps.setProperty("createworksheet", tempSDIDataDS.getString(0, "createworksheet", "N"));
                this.getActionProcessor().processAction("EditDataSet", "1", editDataSetProps);
            }
            return;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            dam.clearRSet(rsetid);
        }
    }

    private void setCompleteDtCompleteByNull(DataSet sdiwi, BaseSDCRules sdcPreRules) throws SapphireException {
        for (int i = 0; i < sdiwi.getRowCount(); ++i) {
            String oldStatus = sdcPreRules.getOldSDIWorkItemValue(sdiwi, i, "workitemstatus");
            String sdiwiStatus = sdiwi.getValue(i, "workitemstatus");
            if (!sdcPreRules.hasSDIDataValueChanged(sdiwi, i, "workitemstatus") || !"Completed".equalsIgnoreCase(oldStatus) || !"DataEntered".equalsIgnoreCase(sdiwiStatus) && !"InProgress".equalsIgnoreCase(sdiwiStatus) && !"Initial".equalsIgnoreCase(sdiwiStatus)) continue;
            sdiwi.setString(i, "completeddt", "");
            sdiwi.setString(i, "completedby", "");
        }
    }

    private void setWapStatusToPendingOnUncancel(DataSet sdiWorkItem, BaseSDCRules sdcPreRules) throws SapphireException {
        for (int i = 0; i < sdiWorkItem.getRowCount(); ++i) {
            String oldWorkItemStatus = sdcPreRules.getOldSDIWorkItemValue(sdiWorkItem, i, "workitemstatus");
            String oldWapStatus = sdcPreRules.getOldSDIWorkItemValue(sdiWorkItem, i, "wapstatus");
            if (!sdcPreRules.hasSDIWorkItemValueChanged(sdiWorkItem, i, "workitemstatus") || !"Cancelled".equalsIgnoreCase(oldWorkItemStatus) || !"Cancelled".equalsIgnoreCase(oldWapStatus)) continue;
            sdiWorkItem.setString(i, "wapstatus", "Pending");
        }
    }

    private void setWapStatusToCancel(DataSet sdiWorkItem, BaseSDCRules sdcPreRules) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select w.activityid from activityworksdi w, sdiworkitem s ").append(" where s.sdcid = ? and s.keyid1 = ? and s.keyid2 = ? and s.keyid3 = ?").append(" and s.workitemid = ? and s.workiteminstance = ? and w.worksdcid = 'SDIWorkItem' and w.workkeyid1 = s.sdiworkitemid");
        PreparedStatement getSDIWIActivity = this.database.prepareStatement("getsdiwiactivity", sql.toString());
        try {
            for (int i = 0; i < sdiWorkItem.getRowCount(); ++i) {
                String oldWapStatus = sdcPreRules.getOldSDIWorkItemValue(sdiWorkItem, i, "wapstatus");
                String workitemStatus = sdiWorkItem.getValue(i, "workitemstatus");
                if (!sdcPreRules.hasSDIWorkItemValueChanged(sdiWorkItem, i, "workitemstatus") || !"Cancelled".equalsIgnoreCase(workitemStatus) || !"Pending".equalsIgnoreCase(oldWapStatus)) continue;
                getSDIWIActivity.setString(1, sdiWorkItem.getValue(i, "sdcid"));
                getSDIWIActivity.setString(2, sdiWorkItem.getValue(i, "keyid1"));
                getSDIWIActivity.setString(3, sdiWorkItem.getValue(i, "keyid2"));
                getSDIWIActivity.setString(4, sdiWorkItem.getValue(i, "keyid3"));
                getSDIWIActivity.setString(5, sdiWorkItem.getValue(i, "workitemid"));
                getSDIWIActivity.setString(6, sdiWorkItem.getValue(i, "workiteminstance"));
                DataSet ds = new DataSet(getSDIWIActivity.executeQuery());
                if (ds.getRowCount() != 0) continue;
                sdiWorkItem.setString(i, "wapstatus", "Cancelled");
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            this.database.closeStatement("getsdiwiactivity");
        }
    }

    private void autoStartStopWapActivity(DataSet sdiworkitemData, DBAccess database, BaseSDCRules sdcPostRules) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select distinct a.activityid, a.activitystatus, s.sdiworkitemid from activity a, activityworksdi w, sdiworkitem s ").append(" where s.sdcid = ? and s.keyid1 = ? and s.keyid2 = ? and s.keyid3 = ?").append(" and s.workitemid = ? and s.workiteminstance = ? and w.worksdcid = 'SDIWorkItem' and w.workkeyid1 = s.sdiworkitemid and a.activityid = w.activityid ");
        PreparedStatement getSDIWIActivity = database.prepareStatement("getsdiwiactivity", sql.toString());
        StringBuffer startActivities = new StringBuffer();
        StringBuffer updateCompleteCountActivities = new StringBuffer();
        DataSet deleteActivityWorkSDIs = new DataSet();
        ArrayList<String> listStartActivities = new ArrayList<String>();
        ArrayList<String> listCompleteActivities = new ArrayList<String>();
        try {
            for (int r = 0; r < sdiworkitemData.getRowCount(); ++r) {
                boolean wapStatusPendingOrAssigned;
                String startedDt = sdiworkitemData.getValue(r, "starteddt");
                boolean started = startedDt.length() > 0 && sdcPostRules.hasSDIWorkItemValueChanged(sdiworkitemData, r, "starteddt");
                String workitemStatus = sdiworkitemData.getValue(r, "workitemstatus");
                String wapStatus = sdiworkitemData.getValue(r, "wapstatus");
                String oldwapStatus = sdcPostRules.getOldSDIWorkItemValue(sdiworkitemData, r, "wapstatus");
                boolean cancelled = sdcPostRules.hasSDIWorkItemValueChanged(sdiworkitemData, r, "workitemstatus") && "Cancelled".equalsIgnoreCase(workitemStatus);
                boolean uncancelled = sdcPostRules.hasSDIWorkItemValueChanged(sdiworkitemData, r, "workitemstatus") && "Cancelled".equalsIgnoreCase(sdcPostRules.getOldSDIWorkItemValue(sdiworkitemData, r, "workitemstatus"));
                boolean completedorcancelled = sdcPostRules.hasSDIWorkItemValueChanged(sdiworkitemData, r, "workitemstatus") && ("Cancelled".equalsIgnoreCase(workitemStatus) || "Completed".equalsIgnoreCase(workitemStatus));
                boolean bl = wapStatusPendingOrAssigned = !"Cancelled".equalsIgnoreCase(wapStatus) && ("Pending".equalsIgnoreCase(oldwapStatus) || "Assigned".equalsIgnoreCase(oldwapStatus));
                if (!wapStatusPendingOrAssigned || !started && !completedorcancelled && !uncancelled) continue;
                getSDIWIActivity.setString(1, sdiworkitemData.getValue(r, "sdcid"));
                getSDIWIActivity.setString(2, sdiworkitemData.getValue(r, "keyid1"));
                getSDIWIActivity.setString(3, sdiworkitemData.getValue(r, "keyid2"));
                getSDIWIActivity.setString(4, sdiworkitemData.getValue(r, "keyid3"));
                getSDIWIActivity.setString(5, sdiworkitemData.getValue(r, "workitemid"));
                getSDIWIActivity.setString(6, sdiworkitemData.getValue(r, "workiteminstance"));
                DataSet ds = new DataSet(getSDIWIActivity.executeQuery());
                if (ds.getRowCount() <= 0) continue;
                String activityid = ds.getValue(0, "activityid");
                String activityStatus = ds.getValue(0, "activitystatus");
                String sdiworkitemid = ds.getValue(0, "sdiworkitemid");
                if (started && "Activated".equalsIgnoreCase(activityStatus) && !listStartActivities.contains(activityid)) {
                    listStartActivities.add(activityid);
                }
                if ("Draft".equalsIgnoreCase(activityStatus) && cancelled) {
                    deleteActivityWorkSDIs.copyRow(ds, -1, 1);
                    continue;
                }
                if (!completedorcancelled && !uncancelled || listCompleteActivities.contains(activityid)) continue;
                listCompleteActivities.add(activityid);
                updateCompleteCountActivities.append(";").append(activityid);
            }
            for (int i = 0; i < listStartActivities.size(); ++i) {
                if (listCompleteActivities.contains(listStartActivities.get(i))) continue;
                startActivities.append(";").append(listStartActivities.get(i));
            }
            if (startActivities.length() > 0) {
                PropertyList setstatus = new PropertyList();
                setstatus.setProperty("activityid", startActivities.substring(1));
                setstatus.setProperty("status", "In Progress");
                this.getActionProcessor().processActionClass(SetActivityStatus.class.getName(), setstatus);
            }
            if (updateCompleteCountActivities.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("activityid", updateCompleteCountActivities.substring(1));
                this.getActionProcessor().processActionClass(UpdateActivityCompleteCount.class.getName(), props);
            }
            if (deleteActivityWorkSDIs.getRowCount() > 0) {
                deleteActivityWorkSDIs.sort("activityid");
                ArrayList<DataSet> activityGrps = deleteActivityWorkSDIs.getGroupedDataSets("activityid");
                for (int g = 0; g < activityGrps.size(); ++g) {
                    DataSet activityWorkSDIs = activityGrps.get(g);
                    PropertyList props = new PropertyList();
                    props.setProperty("activityid", activityWorkSDIs.getValue(0, "activityid"));
                    props.setProperty("worksdcid", "SDIWorkItem");
                    props.setProperty("workkeyid1", activityWorkSDIs.getColumnValues("sdiworkitemid", ";"));
                    props.setProperty("setcancelled", "Y");
                    this.getActionProcessor().processActionClass(RemoveActivityWorkSDI.class.getName(), props);
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            database.closeStatement("getsdiwiactivity");
        }
    }

    protected String getTracelogid(String sdcid, String desc, String auditReason, String auditActivity, String auditSignedFlag, String auditDt) throws SapphireException {
        String tracelogid = null;
        if (auditReason.length() > 0) {
            this.logger.info("Generate the tracelog record");
            PropertyList tracelogprops = new PropertyList();
            tracelogprops.setProperty("sdcid", sdcid);
            tracelogprops.setProperty("description", desc);
            tracelogprops.setProperty("auditreason", auditReason);
            tracelogprops.setProperty("auditactivity", auditActivity);
            tracelogprops.setProperty("auditsignedflag", auditSignedFlag);
            tracelogprops.setProperty("auditdt", auditDt);
            ActionProcessor ap = this.getActionProcessor();
            ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
            tracelogid = (String)tracelogprops.get("tracelogid");
        }
        return tracelogid;
    }

    private boolean prepareSDIWIFormRow(String sdcId, String keyId1, String keyId2, String keyId3, String workItemId, String workItemInstance, DataSet workItemForms, DataSet sdiWIForms, PropertyList properties, StringBuffer worksheetPresent, DataSet sdiWorkItemsExistingDS, DataSet sdiWorkItems, int currentRow) throws SapphireException {
        boolean createWorksheet = false;
        if (sdiWorkItems.getString(currentRow, "s_assignedanalyst", "").length() > 0 || sdiWorkItems.getString(currentRow, "s_assigneddepartment", "").length() > 0) {
            boolean isWorksheetAlreadyPresent;
            HashMap<String, Object> filterMap = new HashMap<String, Object>();
            filterMap.put("sdcid", sdcId);
            filterMap.put("keyid1", keyId1);
            filterMap.put("keyid2", keyId2);
            filterMap.put("keyid3", keyId3);
            filterMap.put("workitemid", workItemId);
            filterMap.put("workiteminstance", new BigDecimal(workItemInstance));
            int workItemRow = sdiWorkItemsExistingDS.findRow(filterMap);
            boolean bl = isWorksheetAlreadyPresent = workItemRow > -1 && sdiWorkItemsExistingDS.getString(workItemRow, "documentid", "").length() > 0;
            if (!isWorksheetAlreadyPresent) {
                String workItemVersionId = sdiWorkItemsExistingDS.getString(workItemRow, "workitemversionid");
                filterMap.clear();
                filterMap.put("workitemid", workItemId);
                filterMap.put("workitemversionid", workItemVersionId);
                DataSet workItemForm = workItemForms.getFilteredDataSet(filterMap);
                if ("On Assignment".equalsIgnoreCase(workItemForm.getString(0, "createworksheetrule"))) {
                    String formId = properties.getProperty("formid");
                    String formVersionId = properties.getProperty("formversionid");
                    filterMap.put("formrule", "default");
                    int row = workItemForm.findRow(filterMap);
                    if (formId.length() == 0 && row > -1) {
                        formId = workItemForm.getString(row, "formid");
                        formVersionId = workItemForm.getString(row, "formversionid");
                    }
                    int dataFormRow = WorkItemUtil.addDataForms(sdiWIForms, sdcId, keyId1, keyId2, keyId3, workItemId, workItemInstance, formId, formVersionId, sdiWorkItems.getString(currentRow, "s_assignedanalyst"), sdiWorkItems.getString(currentRow, "s_assigneddepartment"), this.getTranslationProcessor());
                    createWorksheet = true;
                }
            } else {
                worksheetPresent.append(",<br>&nbsp;&nbsp;").append(sdcId).append("|").append(keyId1).append("|").append(keyId2).append("|").append(keyId3).append("|").append(workItemId).append("|").append(workItemInstance);
                createWorksheet = false;
            }
        }
        return createWorksheet;
    }

    private DataSet getWorkItemParamLists(String rsetId, String[] workItemIdProp, String[] workItemInstanceProp) {
        DataSet paramLists = null;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT s.sdcid, s.keyid1, s.keyid2, s.keyid3, s.paramlistid, s.paramlistversionid, s.variantid, s.dataset").append(", s.sourceworkitemid, s.sourceworkiteminstance FROM sdidata s, rsetitems r").append(" WHERE s.sdcid = r.sdcid").append(" AND s.keyid1 = r.keyid1").append(" AND s.keyid2 = r.keyid2").append(" AND s.keyid3 = r.keyid3").append(" AND r.rsetid = " + safeSQL.addVar(rsetId));
        if (workItemIdProp.length > 0) {
            sql.append(" AND (");
            HashSet<String> workItems = new HashSet<String>();
            for (int i = 0; i < workItemIdProp.length; ++i) {
                String workItemId = workItemIdProp[i];
                String workItemInstId = workItemInstanceProp[i];
                if (workItems.contains(workItemId + ";" + workItemInstId)) continue;
                if (i != 0) {
                    sql.append(" OR ");
                }
                sql.append(" ( s.sourceworkitemid = ").append(safeSQL.addVar(workItemId)).append(" AND s.sourceworkiteminstance = ").append(safeSQL.addVar(workItemInstId)).append(" )");
                workItems.add(workItemId + ";" + workItemInstId);
            }
            sql.append(" )");
        }
        paramLists = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        return paramLists;
    }

    private void populateEditDataSetDS(DataSet sdiDataDS, DataSet sdiWorkItems, int sdiWorkItemRowNum, int colValPropCounter, DataSet allWorkItemParamLists, DataSet sdiDataColList, PropertyList properties, boolean createWorksheetCurrentRow) {
        String sdcId = sdiWorkItems.getString(sdiWorkItemRowNum, "sdcid");
        String keyId1 = sdiWorkItems.getString(sdiWorkItemRowNum, "keyid1");
        String keyId2 = sdiWorkItems.getString(sdiWorkItemRowNum, "keyid2");
        String keyId3 = sdiWorkItems.getString(sdiWorkItemRowNum, "keyid3");
        String workItemId = sdiWorkItems.getString(sdiWorkItemRowNum, "workitemid");
        String workItemInstanceId = String.valueOf(sdiWorkItems.getInt(sdiWorkItemRowNum, "workiteminstance"));
        boolean createWorksheetInputProp = StringUtil.getYN(properties.getProperty("createworksheet", "Y"), "Y").equals("Y");
        String assignedAnalyst = sdiWorkItems.getString(sdiWorkItemRowNum, "s_assignedanalyst", "");
        String assignedDept = sdiWorkItems.getString(sdiWorkItemRowNum, "s_assigneddepartment", "");
        int keyCols = Integer.parseInt(this.getSDCProcessor().getProperty(sdcId, "keycolumns"));
        if (assignedAnalyst.length() > 0 || assignedDept.length() > 0) {
            HashMap<String, Object> filterMap = new HashMap<String, Object>();
            filterMap.put("sdcid", sdcId);
            filterMap.put("keyid1", keyId1);
            if (keyCols > 1) {
                filterMap.put("keyid2", keyId2);
            }
            if (keyCols > 2) {
                filterMap.put("keyid3", keyId3);
            }
            filterMap.put("sourceworkitemid", workItemId);
            filterMap.put("sourceworkiteminstance", new BigDecimal(workItemInstanceId));
            DataSet workItemParamLists = allWorkItemParamLists.getFilteredDataSet(filterMap);
            for (int i = 0; i < workItemParamLists.getRowCount(); ++i) {
                if (sdiDataColList.getRowCount() <= 0) continue;
                int newRow = sdiDataDS.addRow();
                sdiDataDS.setString(newRow, "keyid1", keyId1);
                sdiDataDS.setString(newRow, "keyid2", keyId2);
                sdiDataDS.setString(newRow, "keyid3", keyId3);
                sdiDataDS.setString(newRow, "paramlistid", workItemParamLists.getString(i, "paramlistid"));
                sdiDataDS.setString(newRow, "paramlistversionid", workItemParamLists.getString(i, "paramlistversionid"));
                sdiDataDS.setString(newRow, "variantid", workItemParamLists.getString(i, "variantid"));
                sdiDataDS.setString(newRow, "dataset", String.valueOf(workItemParamLists.getInt(i, "dataset")));
                if (sdiWorkItems.isValidColumn("s_assignedanalyst")) {
                    sdiDataDS.setString(newRow, "s_assignedanalyst", assignedAnalyst);
                }
                if (sdiWorkItems.isValidColumn("s_assigneddepartment")) {
                    sdiDataDS.setString(newRow, "s_assigneddepartment", assignedDept);
                }
                if (createWorksheetInputProp) {
                    if (createWorksheetCurrentRow) {
                        sdiDataDS.setString(newRow, "createworksheet", "N");
                        continue;
                    }
                    sdiDataDS.setString(newRow, "createworksheet", "Y");
                    continue;
                }
                sdiDataDS.setString(newRow, "createworksheet", "N");
            }
        }
    }

    private void createLESWorksheetOnAssignment(DataSet sdiWorkItems) throws SapphireException {
        DataSet assignedSdiWorkitems = new DataSet();
        assignedSdiWorkitems.addColumn("workitemversionid", 0);
        assignedSdiWorkitems.addColumn("sdiworkitemid", 0);
        QueryProcessor qp = this.getQueryProcessor();
        TranslationProcessor tp = this.getTranslationProcessor();
        PreparedStatement getWOVersion = this.database.prepareStatement("getwoversion", "select sdiworkitemid, workitemversionid from sdiworkitem where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? and workiteminstance = ?");
        try {
            for (int i = 0; i < sdiWorkItems.getRowCount(); ++i) {
                if (sdiWorkItems.getValue(i, "s_assignedanalyst").length() <= 0 || qp.getPreparedCount("select count(1) from worksheetsdi where sdcid = ? and keyid1 = ?", new String[]{"SDIWorkItem", sdiWorkItems.getValue(i, "sdiworkitemid")}) != 0) continue;
                assignedSdiWorkitems.copyRow(sdiWorkItems, i, 1);
                getWOVersion.setString(1, sdiWorkItems.getValue(i, "sdcid"));
                getWOVersion.setString(2, sdiWorkItems.getValue(i, "keyid1"));
                getWOVersion.setString(3, sdiWorkItems.getValue(i, "keyid2"));
                getWOVersion.setString(4, sdiWorkItems.getValue(i, "keyid3"));
                getWOVersion.setString(5, sdiWorkItems.getValue(i, "workitemid"));
                getWOVersion.setString(6, sdiWorkItems.getValue(i, "workiteminstance"));
                DataSet dsVersion = new DataSet(getWOVersion.executeQuery());
                assignedSdiWorkitems.setValue(i, "workitemversionid", dsVersion.getValue(0, "workitemversionid"));
                assignedSdiWorkitems.setValue(i, "sdiworkitemid", dsVersion.getValue(0, "sdiworkitemid"));
            }
        }
        catch (Exception e) {
            throw new SapphireException(tp.translate("Failed to create LES worksheet on assignment of workitem: ") + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        finally {
            this.database.closeStatement("getwoversion");
        }
        if (assignedSdiWorkitems.getRowCount() > 0) {
            assignedSdiWorkitems.sort("workitemid,workitemversionid");
            ArrayList<DataSet> groups = assignedSdiWorkitems.getGroupedDataSets("workitemid,workitemversionid");
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT w.createlesrule, sdiworksheetrule.authorflag, sdiworksheetrule.worksheetid, sdiworksheetrule.worksheetversionid, sdiworksheetrule.workbookid, sdiworksheetrule.workbookversionid, ").append(" sdiworksheetrule.worksheetrule FROM workitem w, sdiworksheetrule ").append(" WHERE sdiworksheetrule.sdcid = ? AND sdiworksheetrule.keyid1 = w.workitemid AND sdiworksheetrule.keyid2 = w.workitemversionid ").append(" AND  w.workitemid = ? AND  w.workitemversionid = ?  AND w.createlesrule = ? AND sdiworksheetrule.worksheetrule = ?");
            PreparedStatement getWSTemplate = this.database.prepareStatement("getwstemplate", sql.toString());
            try {
                for (int grp = 0; grp < groups.size(); ++grp) {
                    DataSet dsGrp = groups.get(grp);
                    String workitemid = dsGrp.getValue(0, "workitemid");
                    String workitemversionid = dsGrp.getValue(0, "workitemversionid");
                    getWSTemplate.setString(1, "WorkItem");
                    getWSTemplate.setString(2, workitemid);
                    getWSTemplate.setString(3, workitemversionid);
                    getWSTemplate.setString(4, "On Assignment");
                    getWSTemplate.setString(5, "default");
                    DataSet dsTemplate = new DataSet(getWSTemplate.executeQuery());
                    if (dsTemplate.getRowCount() == 1) {
                        PropertyList createWSProps = new PropertyList();
                        createWSProps.setProperty("sdiworkitemid", dsGrp.getColumnValues("sdiworkitemid", ";"));
                        createWSProps.setProperty("templateid", dsTemplate.getString(0, "worksheetid"));
                        createWSProps.setProperty("templateversionid", dsTemplate.getString(0, "worksheetversionid"));
                        createWSProps.setProperty("workbookid", dsTemplate.getString(0, "workbookid"));
                        createWSProps.setProperty("workbookversionid", dsTemplate.getString(0, "workbookversionid"));
                        this.getActionProcessor().processActionClass(GenerateTestMethodWorksheet.class.getName(), createWSProps);
                        continue;
                    }
                    if (dsTemplate.getRowCount() <= 1) continue;
                    String msg = "LES worksheet could not be created on assignment of workitem. Multiple LES worksheet template found for " + workitemid + ";" + workitemversionid;
                    this.logger.info(msg);
                }
            }
            catch (Exception e) {
                throw new SapphireException(tp.translate("Failed to create LES worksheet on assignment of workitem: ") + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            finally {
                this.database.closeStatement("getwstemplate");
            }
        }
    }
}

