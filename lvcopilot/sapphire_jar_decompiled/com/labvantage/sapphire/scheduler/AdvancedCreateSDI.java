/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.scheduler;

import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.modules.em.MonitorGroup;
import com.labvantage.sapphire.scheduler.ScheduleEvent;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseScheduleTask;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdvancedCreateSDI
extends BaseScheduleTask {
    public static final String LOG_NAME = "AdvancedCreateSDI";
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String instancenumberStr = "";

    @Override
    public void execute() {
        if (this.scheduleEvents == null) {
            return;
        }
        if (this.scheduleProperties == null) {
            this.scheduleProperties = new PropertyList();
        }
        for (Object oScheduleEvent : this.scheduleEvents) {
            SDCProcessor sdcProcessor;
            boolean isDepartmental;
            ScheduleEvent scheduleEvent = (ScheduleEvent)oScheduleEvent;
            String schedulePlanId = scheduleEvent.getSchedulePlanid();
            String schedulePlanItemId = scheduleEvent.getSchedulePlanitemid();
            String sdcId = scheduleEvent.getScheduleTemplateSdcId();
            String templateId = scheduleEvent.getScheduleTemplateKeydId1();
            String templateId2 = scheduleEvent.getScheduleTemplateKeydId2();
            String templateId3 = scheduleEvent.getScheduleTemplateKeydId3();
            String copies = scheduleEvent.getNumCopies();
            this.instancenumberStr = "";
            PropertyList instanceprops = this.scheduleProperties.getPropertyListNotNull("instanceprops");
            boolean instancesActive = instanceprops.getProperty("populateinstances", "N").startsWith("Y");
            String instanceNumberColumnId = instanceprops.getProperty("instancenumbercolumnid");
            if (instancesActive && (instanceNumberColumnId == null || instanceNumberColumnId.isEmpty())) {
                instancesActive = false;
            }
            if (instancesActive) {
                copies = this.getInstanceData(scheduleEvent, sdcId, copies, instanceNumberColumnId, instanceprops, this.instanceCount);
            }
            if (!this.scheduleProperties.getProperty("sdcid", sdcId).equals(sdcId)) {
                throw new RuntimeException(new SapphireException(this.getTranslationProcessor().translate("Schedule Plan Item SDC id and task SDC id do not match! " + schedulePlanId + ";" + schedulePlanItemId)));
            }
            String scheduleCondition = this.scheduleProperties.getProperty("schedulecondition");
            if (!scheduleCondition.equals("") && this.checkPreventExecution(scheduleEvent, sdcId, templateId, templateId2, templateId3, scheduleCondition)) {
                this.logger.info(LOG_NAME, "Conditional logic prevented creating Scheduled SDI. " + schedulePlanId + ";" + schedulePlanItemId);
                continue;
            }
            if (this.scheduleProperties.getPropertyListNotNull("merge").getProperty("merge", "N").equals("Y")) {
                SDIData templateSDI;
                if (!sdcId.equals("") && !templateId.equals("")) {
                    templateSDI = this.getTemplateSDI(sdcId, templateId, templateId2, templateId3);
                } else {
                    templateSDI = new SDIData();
                    templateSDI.setDataset("primary", new DataSet());
                    templateSDI.setDataset("sdiworkitem", new DataSet());
                    templateSDI.setDataset("dataset", new DataSet());
                    templateSDI.setDataset("sdispec", new DataSet());
                }
                this.fillExtraColumnsToTemplate(templateSDI, scheduleEvent);
                SDIData previousSDI = this.getPreviousSDI(scheduleEvent, sdcId, templateSDI);
                if (previousSDI != null && previousSDI.getDataset("primary").getRowCount() > 0) {
                    try {
                        this.mergeWithExistingSDI(sdcId, templateSDI, previousSDI);
                        continue;
                    }
                    catch (ActionException ae) {
                        this.logger.stackTrace(ae);
                        throw new RuntimeException(ae);
                    }
                }
            }
            PropertyList addSdiProps = new PropertyList();
            addSdiProps.setProperty("sdcid", sdcId);
            addSdiProps.setProperty("templatekeyid1", templateId);
            addSdiProps.setProperty("templatekeyid2", templateId2);
            addSdiProps.setProperty("templatekeyid3", templateId3);
            addSdiProps.setProperty("copies", copies);
            addSdiProps.setProperty("applyworkitems", "Y");
            if (instancesActive) {
                addSdiProps.setProperty(instanceNumberColumnId, this.instancenumberStr);
            }
            PropertyListCollection columns = this.scheduleProperties.getCollectionNotNull("columnvalues");
            for (int col = 0; col < columns.size(); ++col) {
                String colId = columns.getPropertyList(col).getProperty("columnid");
                String value = columns.getPropertyList(col).getProperty("value");
                boolean enabled = columns.getPropertyList(col).getProperty("enabled", "Y").startsWith("Y");
                if (!enabled || colId.equals("") || value.equals("")) continue;
                addSdiProps.setProperty(colId, this.replaceTokens(value, scheduleEvent));
            }
            boolean copySecurityDepartmentFromMonitorGroup = false;
            PropertyList copySecurityDepartmentProps = this.scheduleProperties.getPropertyListNotNull("copysecuritycolumnprops");
            boolean copySecurityDepartment = copySecurityDepartmentProps.getProperty("copysecuritycolumns", "N").startsWith("Y");
            if (copySecurityDepartment && (isDepartmental = (sdcProcessor = this.getSDCProcessor()).getProperty(sdcId, "accesscontrolledflag", "N").equals("D"))) {
                String copyFrom = copySecurityDepartmentProps.getProperty("copyfrom", "");
                if (copyFrom.equals("Template")) {
                    this.getSecurityColumns(addSdiProps, scheduleEvent.getScheduleTemplateSdcId(), scheduleEvent.getScheduleTemplateKeydId1(), scheduleEvent.getScheduleTemplateKeydId2(), scheduleEvent.getScheduleTemplateKeydId3());
                } else if (copyFrom.equals("Source")) {
                    this.getSecurityColumns(addSdiProps, scheduleEvent.getLinkSdcid(), scheduleEvent.getLinkKeyid1(), scheduleEvent.getLinkKeyid2(), scheduleEvent.getLinkKeyid3());
                } else if (copyFrom.equals("Schedule Plan Item")) {
                    this.getSecurityColumns(addSdiProps, "SchedulePlanItem", scheduleEvent.getSchedulePlanid(), scheduleEvent.getSchedulePlanitemid(), null);
                } else if (copyFrom.equals("Schedule Plan")) {
                    this.getSecurityColumns(addSdiProps, "SchedulePlan", scheduleEvent.getSchedulePlanid(), null, null);
                } else if (copyFrom.equals("Monitor Group")) {
                    if (this.monitorGroupId != null && !this.monitorGroupId.isEmpty()) {
                        this.getSecurityColumns(addSdiProps, "LV_MonitorGroup", this.monitorGroupId, null, null);
                    } else {
                        copySecurityDepartmentFromMonitorGroup = true;
                    }
                }
            }
            String newKeyid1 = "";
            try {
                this.getActionProcessor().processAction("AddSDI", "1", addSdiProps);
                newKeyid1 = addSdiProps.getProperty("newkeyid1");
                this.logger.info(LOG_NAME, "AdvancedCreateSDI added " + newKeyid1);
                if (newKeyid1 != null && this.newKeyidsMap != null) {
                    this.addNewKeyid(sdcId, newKeyid1);
                }
            }
            catch (ActionException ae) {
                this.logger.stackTrace(ae);
                throw new RuntimeException(ae);
            }
            if (!this.createMonitorGroup || this.monitorGroupId != null && !this.monitorGroupId.isEmpty()) continue;
            try {
                MonitorGroup monitorGroup = new MonitorGroup(this.getConnectionId());
                monitorGroup.setParent("SchedulePlan", scheduleEvent.getSchedulePlanid(), null, null);
                monitorGroup.linkMonitorGroup(sdcId, newKeyid1, addSdiProps.getProperty("newkeyid2"), addSdiProps.getProperty("newkeyid3"), copySecurityDepartmentFromMonitorGroup);
            }
            catch (SapphireException e) {
                this.logger.stackTrace(e);
                throw new RuntimeException("Cannot link to monitor group", e);
            }
        }
    }

    private String getInstanceData(ScheduleEvent scheduleEvent, String sdcId, String copies, String instanceNumberColumnId, PropertyList instanceprops, Integer instanceCnt) {
        String instanceCountColumnInSource = instanceprops.getProperty("instancecountcolumnid");
        boolean populateInstances = false;
        if (!instanceCountColumnInSource.isEmpty()) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(scheduleEvent.getLinkSdcid());
            sdiRequest.setKeyid1List(scheduleEvent.getLinkKeyid1());
            sdiRequest.setKeyid2List(scheduleEvent.getLinkKeyid2());
            sdiRequest.setKeyid3List(scheduleEvent.getLinkKeyid3());
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRetainRsetid(false);
            DataSet sourcePrimary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
            if (sourcePrimary.getRowCount() == 1 && sourcePrimary.isValidColumn(instanceCountColumnInSource)) {
                BigDecimal instanceBd = sourcePrimary.getBigDecimal(0, instanceCountColumnInSource);
                if (instanceBd != null) {
                    if (instanceCnt == null) {
                        instanceCnt = instanceBd.intValue();
                    }
                    populateInstances = true;
                } else {
                    instanceCnt = 1;
                }
            }
        }
        int oldInstances = 0;
        if (populateInstances) {
            PropertyListCollection matchInstanceColumnCollection = instanceprops.getCollectionNotNull("groupbycolumns");
            StringBuilder where = new StringBuilder();
            ArrayList<Object> params = new ArrayList<Object>();
            for (int i = 0; i < matchInstanceColumnCollection.size(); ++i) {
                PropertyList matchColumnProps = matchInstanceColumnCollection.getPropertyList(i);
                String columnId = matchColumnProps.getProperty("columnid");
                String matchToColumn = matchColumnProps.getProperty("comparetocolumn");
                if (matchToColumn.equals("eventdate")) {
                    where.append(" AND ").append(columnId).append(" = ?");
                    params.add(new Timestamp(scheduleEvent.getEventDt().getTime().getTime()));
                    continue;
                }
                String matchToValue = this.getTokenValue(matchColumnProps.getProperty("comparetocolumn"), scheduleEvent);
                if (columnId.isEmpty() || matchToValue.isEmpty()) continue;
                where.append(" AND ").append(columnId).append(" = ?");
                params.add(matchToValue);
            }
            String sql = "SELECT max(" + instanceNumberColumnId + ") maxinstance from " + this.getSDCProcessor().getProperty(sdcId, "tableid") + " WHERE " + where.toString().substring(4);
            DataSet maxInstanceDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, params.toArray());
            if (maxInstanceDs.getRowCount() > 0 && maxInstanceDs.getColumnType("maxinstance") == 1) {
                try {
                    BigDecimal maxInstance = maxInstanceDs.getBigDecimal(0, "maxinstance");
                    if (maxInstance != null) {
                        oldInstances = maxInstance.intValue();
                    }
                }
                catch (NumberFormatException e) {
                    oldInstances = 0;
                }
            }
        }
        Integer copiesInt = Integer.valueOf(copies);
        for (int i = 0; i < instanceCnt; ++i) {
            for (int j = 0; j < copiesInt; ++j) {
                this.instancenumberStr = this.instancenumberStr + ";" + (populateInstances ? String.valueOf(oldInstances + i + 1) : "");
            }
        }
        this.instancenumberStr = this.instancenumberStr.substring(1);
        copies = String.valueOf(copiesInt * instanceCnt);
        return copies;
    }

    private void fillExtraColumnsToTemplate(SDIData templateSDI, ScheduleEvent scheduleEvent) {
        if (templateSDI == null || templateSDI.getDataset("primary") == null || templateSDI.getDataset("primary").getRowCount() == 0) {
            return;
        }
        PropertyListCollection columns = this.scheduleProperties.getCollectionNotNull("columnvalues");
        for (int col = 0; col < columns.size(); ++col) {
            String colId = columns.getPropertyList(col).getProperty("columnid");
            String value = columns.getPropertyList(col).getProperty("value");
            boolean enabled = columns.getPropertyList(col).getProperty("enabled", "Y").startsWith("Y");
            if (!enabled || colId.equals("") || value.equals("")) continue;
            templateSDI.getDataset("primary").setValue(0, colId, this.replaceTokens(value, scheduleEvent));
        }
    }

    private void mergeWithExistingSDI(String sdcId, SDIData templateSDI, SDIData previousSDI) throws ActionException {
        String[] keyValues3;
        String[] keyFields;
        int i;
        DataSet primary = previousSDI.getDataset("primary");
        String keyId1 = primary.getString(0, this.getSDCProcessor().getProperty(sdcId, "keycolid1"));
        String keyId2 = primary.getString(0, this.getSDCProcessor().getProperty(sdcId, "keycolid2"), "(null)");
        String keyId3 = primary.getString(0, this.getSDCProcessor().getProperty(sdcId, "keycolid3"), "(null)");
        boolean forceNew = this.scheduleProperties.getPropertyListNotNull("merge").getProperty("forcenew", "N").equals("Y");
        DataSet sdiWorkItem = templateSDI.getDataset("sdiworkitem");
        PropertyList addSdiWorkItemProps = new PropertyList();
        ArrayList<DataSet> groupedds = sdiWorkItem.getGroupedDataSets("groupid");
        for (DataSet wiDs : groupedds) {
            for (i = 0; i < wiDs.size(); ++i) {
                String groupId = wiDs.getString(i, "groupid", "");
                String workItemTypeFlag = wiDs.getString(i, "workitemtypeflag", "W");
                if (!groupId.isEmpty() && !workItemTypeFlag.equals("P")) continue;
                keyFields = new String[]{"workitemid", "workitemversionid"};
                String workitemversionid = wiDs.getValue(i, "workitemversionid", "");
                String workitemid = wiDs.getValue(i, "workitemid", "");
                if (workitemversionid.isEmpty() || workitemversionid.equals("C")) {
                    try {
                        workitemversionid = SdiInfo.getCurrentVersion("WorkItem", workitemid, "", this.getConnectionId());
                    }
                    catch (SapphireException sapphireException) {
                        // empty catch block
                    }
                }
                keyValues3 = new String[]{workitemid, workitemversionid};
                if (!forceNew && this.containsDetailRow(previousSDI.getDataset("sdiworkitem"), keyFields, keyValues3)) continue;
                addSdiWorkItemProps.setProperty("sdcid", sdcId);
                addSdiWorkItemProps.setProperty("propsmatch", "Y");
                if (forceNew) {
                    addSdiWorkItemProps.setProperty("forcenew", "Y");
                }
                AdvancedCreateSDI.appendToPL(addSdiWorkItemProps, "keyid1", keyId1);
                AdvancedCreateSDI.appendToPL(addSdiWorkItemProps, "keyid2", keyId2);
                AdvancedCreateSDI.appendToPL(addSdiWorkItemProps, "keyid3", keyId3);
                AdvancedCreateSDI.appendToPL(addSdiWorkItemProps, "workitemid", wiDs.getString(i, "workitemid"));
                AdvancedCreateSDI.appendToPL(addSdiWorkItemProps, "workitemversionid", wiDs.getString(i, "workitemversionid", ""));
            }
        }
        if (!addSdiWorkItemProps.isEmpty()) {
            this.getActionProcessor().processAction("AddSDIWorkItem", "1", addSdiWorkItemProps);
        }
        DataSet sdiData = templateSDI.getDataset("dataset");
        PropertyList addDataSetProps = new PropertyList();
        for (i = 0; i < sdiData.size(); ++i) {
            String[] keyFields2 = new String[]{"paramlistid", "paramlistversionid", "variantid"};
            String paramlistid = sdiData.getValue(i, "paramlistid", "");
            String paramlistversionid = sdiData.getValue(i, "paramlistversionid", "");
            String variantid = sdiData.getValue(i, "variantid", "");
            if (paramlistversionid.isEmpty() || paramlistversionid.equals("C")) {
                try {
                    paramlistversionid = SdiInfo.getCurrentVersion("ParamList", paramlistid, variantid, this.getConnectionId());
                }
                catch (SapphireException workitemid) {
                    // empty catch block
                }
            }
            String[] keyValues2 = new String[]{paramlistid, paramlistversionid, variantid};
            if (!forceNew && this.containsDetailRow(previousSDI.getDataset("dataset"), keyFields2, keyValues2)) continue;
            addDataSetProps.setProperty("sdcid", sdcId);
            addDataSetProps.setProperty("propsmatch", "Y");
            addDataSetProps.setProperty("addnewonly", "N");
            AdvancedCreateSDI.appendToPL(addDataSetProps, "keyid1", keyId1);
            AdvancedCreateSDI.appendToPL(addDataSetProps, "keyid2", keyId2);
            AdvancedCreateSDI.appendToPL(addDataSetProps, "keyid3", keyId3);
            AdvancedCreateSDI.appendToPL(addDataSetProps, "paramlistid", sdiData.getString(i, "paramlistid"));
            AdvancedCreateSDI.appendToPL(addDataSetProps, "paramlistversionid", sdiData.getString(i, "paramlistversionid"));
            AdvancedCreateSDI.appendToPL(addDataSetProps, "variantid", sdiData.getString(i, "variantid"));
        }
        if (!addDataSetProps.isEmpty()) {
            this.getActionProcessor().processAction("AddDataSet", "1", addDataSetProps);
        }
        DataSet sdiSpec = templateSDI.getDataset("sdispec");
        PropertyList addSdiSpecProps = new PropertyList();
        for (int i2 = 0; i2 < sdiSpec.size(); ++i2) {
            keyFields = new String[]{"specid", "specversionid"};
            String specid = sdiSpec.getValue(i2, "specid", "");
            String specversionid = sdiSpec.getValue(i2, "specversionid", "");
            if (specversionid.isEmpty() || specversionid.equals("C")) {
                try {
                    specversionid = SdiInfo.getCurrentVersion("SpecSDC", specid, specversionid, this.getConnectionId());
                }
                catch (SapphireException keyValues3) {
                    // empty catch block
                }
            }
            keyValues3 = new String[]{specid, specversionid};
            if (this.containsDetailRow(previousSDI.getDataset("sdispec"), keyFields, keyValues3)) continue;
            addSdiSpecProps.setProperty("sdcid", sdcId);
            AdvancedCreateSDI.appendToPL(addSdiSpecProps, "keyid1", keyId1);
            AdvancedCreateSDI.appendToPL(addSdiSpecProps, "keyid2", keyId2);
            AdvancedCreateSDI.appendToPL(addSdiSpecProps, "keyid3", keyId3);
            AdvancedCreateSDI.appendToPL(addSdiSpecProps, "specid", sdiWorkItem.getString(i2, "specid"));
            AdvancedCreateSDI.appendToPL(addSdiSpecProps, "specversionid", sdiWorkItem.getString(i2, "specversionid"));
        }
        if (!addSdiSpecProps.isEmpty()) {
            this.getActionProcessor().processAction("AddSDISpec", "1", addSdiSpecProps);
        }
    }

    private boolean containsDetailRow(DataSet dataSet, String[] keyFields, String[] keyValues) {
        if (keyFields.length != keyValues.length) {
            throw new RuntimeException(new SapphireException("Invalid key/value arrays"));
        }
        if (dataSet == null || dataSet.getRowCount() == 0) {
            return false;
        }
        boolean retVal = false;
        for (int i = 0; i < dataSet.getRowCount(); ++i) {
            int matchingKeyFields = 0;
            for (int j = 0; j < keyFields.length; ++j) {
                if (!dataSet.getValue(i, keyFields[j], "").equals(keyValues[j])) continue;
                ++matchingKeyFields;
            }
            if (matchingKeyFields != keyFields.length) continue;
            retVal = true;
            break;
        }
        return retVal;
    }

    private SDIData getPreviousSDI(ScheduleEvent scheduleEvent, String sdcId, SDIData templateSDI) {
        String eventDateField;
        PropertyList mergeProperties = this.scheduleProperties.getPropertyListNotNull("merge");
        String queryWhere = mergeProperties.getProperty("querywhere", "");
        if (templateSDI != null) {
            DataSet templateData = templateSDI.getDataset("primary");
            PropertyListCollection matchColumns = mergeProperties.getCollectionNotNull("matchcolumns");
            for (int i = 0; i < matchColumns.size(); ++i) {
                String columnId = matchColumns.getPropertyList(i).getProperty("columnid");
                boolean enabled = matchColumns.getPropertyList(i).getProperty("enabled", "Y").equals("Y");
                if (columnId.equals("") || !enabled) continue;
                int columnType = templateData.getColumnType(columnId);
                if (columnType == 0) {
                    queryWhere = queryWhere + " AND " + columnId + "='" + templateData.getString(0, columnId, "") + "' ";
                    continue;
                }
                if (columnType == 1) {
                    queryWhere = queryWhere + " AND " + columnId + "=" + templateData.getInt(0, columnId, -99999) + " ";
                    continue;
                }
                if (columnType != 2) continue;
                queryWhere = this.getConnectionProcessor().isOra() ? queryWhere + " AND " + columnId + "=to_date('" + sdf.format(templateData.getCalendar(0, columnId, new GregorianCalendar())) + "','yyyy-mm-dd hh24:mi:ss') " : queryWhere + " AND " + columnId + "=cast(" + sdf.format(templateData.getCalendar(0, columnId, new GregorianCalendar())) + "' as datetime) ";
            }
        }
        if (!(eventDateField = mergeProperties.getProperty("eventdatefield", "")).equals("")) {
            String sGracePeriod = mergeProperties.getProperty("graceperiod", "0");
            int gracePeriod = 0;
            try {
                gracePeriod = Integer.parseInt(sGracePeriod);
            }
            catch (NumberFormatException columnId) {
                // empty catch block
            }
            int gracePeriodUnit = -1;
            String sGracePeriodUnit = mergeProperties.getProperty("graceperiodunit", "");
            if (sGracePeriodUnit.equalsIgnoreCase("second")) {
                gracePeriodUnit = 13;
            } else if (sGracePeriodUnit.equalsIgnoreCase("minute")) {
                gracePeriodUnit = 12;
            } else if (sGracePeriodUnit.equalsIgnoreCase("hour")) {
                gracePeriodUnit = 10;
            } else if (sGracePeriodUnit.equalsIgnoreCase("day")) {
                gracePeriodUnit = 5;
            } else {
                gracePeriod = 0;
            }
            if (gracePeriod == 0) {
                String eventDate = this.getConnectionProcessor().isOra() ? "to_date('" + sdf.format(scheduleEvent.getEventDt().getTime()) + "','yyyy-mm-dd hh24:mi:ss') " : "cast('" + sdf.format(scheduleEvent.getEventDt().getTime()) + "' as datetime) ";
                queryWhere = queryWhere + " AND " + eventDateField + "=" + eventDate + " ";
            } else {
                String sEndDate;
                String sStartDate;
                Calendar startDate = (Calendar)scheduleEvent.getEventDt().clone();
                startDate.add(gracePeriodUnit, -gracePeriod);
                Calendar endDate = (Calendar)scheduleEvent.getEventDt().clone();
                endDate.add(gracePeriodUnit, gracePeriod);
                if (this.getConnectionProcessor().isOra()) {
                    sStartDate = "to_date('" + sdf.format(startDate.getTime()) + "','yyyy-mm-dd hh24:mi:ss') ";
                    sEndDate = "to_date('" + sdf.format(endDate.getTime()) + "','yyyy-mm-dd hh24:mi:ss') ";
                } else {
                    sStartDate = "cast('" + sdf.format(startDate.getTime()) + "' as datetime) ";
                    sEndDate = "cast('" + sdf.format(endDate.getTime()) + "' as datetime) ";
                }
                queryWhere = queryWhere + " AND " + eventDateField + ">=" + sStartDate + " AND " + eventDateField + "<=" + sEndDate + " ";
            }
        }
        if ((queryWhere = queryWhere.trim()).toLowerCase().startsWith("and")) {
            queryWhere = queryWhere.substring(3);
        }
        SDIRequest prevSdiRequest = new SDIRequest();
        prevSdiRequest.setSDCid(sdcId);
        prevSdiRequest.setQueryFrom(this.getSDCProcessor().getProperty(sdcId, "tableid"));
        prevSdiRequest.setQueryWhere(queryWhere);
        prevSdiRequest.setRequestItem("primary");
        prevSdiRequest.setRequestItem("sdiworkitem");
        prevSdiRequest.setRequestItem("dataset");
        prevSdiRequest.setRequestItem("sdispec");
        return this.getSDIProcessor().getSDIData(prevSdiRequest);
    }

    private SDIData getTemplateSDI(String sdcId, String templateId, String templateId2, String templateId3) {
        SDIRequest templateRequest = new SDIRequest();
        templateRequest.setSDCid(sdcId);
        templateRequest.setKeyid1List(templateId);
        templateRequest.setKeyid2List(templateId2);
        templateRequest.setKeyid3List(templateId3);
        templateRequest.setRequestItem("primary");
        templateRequest.setRequestItem("sdiworkitem");
        templateRequest.setRequestItem("dataset");
        templateRequest.setRequestItem("sdispec");
        return this.getSDIProcessor().getSDIData(templateRequest);
    }

    private boolean checkPreventExecution(ScheduleEvent scheduleEvent, String sdcId, String templateId, String templateId2, String templateId3, String scheduleCondition) {
        HashMap<String, Object> bindingMap = new HashMap<String, Object>();
        bindingMap.put("queryProcessor", this.getQueryProcessor());
        bindingMap.put("qp", this.getQueryProcessor());
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcId);
        sdiRequest.setKeyid1List(templateId);
        sdiRequest.setKeyid2List(templateId2);
        sdiRequest.setKeyid3List(templateId3);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRetainRsetid(false);
        DataSet templatePrimary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        bindingMap.put("templatedataset", templatePrimary);
        PropertyList templatePropertyList = new PropertyList();
        for (int i = 0; i < templatePrimary.getColumnCount(); ++i) {
            String columnId = templatePrimary.getColumnId(i);
            templatePropertyList.setProperty(columnId, templatePrimary.getValue(0, columnId, ""));
        }
        bindingMap.put("template", templatePropertyList);
        sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(scheduleEvent.getLinkSdcid());
        sdiRequest.setKeyid1List(scheduleEvent.getLinkKeyid1());
        sdiRequest.setKeyid2List(scheduleEvent.getLinkKeyid2());
        sdiRequest.setKeyid3List(scheduleEvent.getLinkKeyid3());
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRetainRsetid(false);
        DataSet sourcePrimary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        bindingMap.put("sourcedataset", sourcePrimary);
        PropertyList sourcePropertyList = new PropertyList();
        for (int i = 0; i < sourcePrimary.getColumnCount(); ++i) {
            String columnId = sourcePrimary.getColumnId(i);
            sourcePropertyList.setProperty(columnId, sourcePrimary.getValue(0, columnId, ""));
        }
        bindingMap.put("source", sourcePropertyList);
        try {
            String executeAction = GroovyUtil.getInstance(this.connectionInfo).evaluateSecure(scheduleCondition, bindingMap);
            return executeAction.equalsIgnoreCase("n") || executeAction.equalsIgnoreCase("no") || executeAction.equalsIgnoreCase("false");
        }
        catch (SapphireException e) {
            this.logger.error(LOG_NAME, "Failed to process Groovy evaluation " + scheduleCondition, e);
            return false;
        }
    }

    private void getSecurityColumns(PropertyList addSdiProps, String sdcId, String keyid1, String keyid2, String keyid3) {
        boolean isDepartmental = this.getSDCProcessor().getProperty(sdcId, "accesscontrolledflag", "N").equals("D");
        if (isDepartmental) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcId);
            sdiRequest.setKeyid1List(keyid1);
            sdiRequest.setKeyid2List(keyid2);
            sdiRequest.setKeyid3List(keyid3);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRetainRsetid(false);
            DataSet primary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
            String securityDepartment = "";
            String securityUser = "";
            if (primary.isValidColumn("securitydepartment")) {
                if (primary.getRowCount() > 0) {
                    securityDepartment = primary.getValue(0, "securitydepartment");
                    securityUser = primary.getValue(0, "securityuser");
                }
                addSdiProps.setProperty("securitydepartment", securityDepartment);
                addSdiProps.setProperty("securityuser", securityUser);
            }
        }
    }

    public String replaceTokens(String value, ScheduleEvent event) {
        if (value.contains("[")) {
            String[] tokens;
            for (String token : tokens = StringUtil.getTokens(value)) {
                value = StringUtil.replaceAll(value, "[" + token + "]", this.getTokenValue(token, event));
            }
        }
        return value;
    }

    private String getTokenValue(String valueFrom, ScheduleEvent event) {
        String retVal = "";
        if ("eventdate".equals(valueFrom)) {
            M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            retVal = m18n.format(event.getEventDt());
        } else if ("sourcekeyid1".equals(valueFrom)) {
            retVal = event.getLinkKeyid1();
        } else if ("sourcekeyid2".equals(valueFrom)) {
            retVal = event.getLinkKeyid2();
        } else if ("sourcekeyid3".equals(valueFrom)) {
            retVal = event.getLinkKeyid3();
        } else if ("sourcesdcid".equals(valueFrom)) {
            retVal = event.getLinkSdcid();
        } else if (valueFrom.startsWith("planitem") && valueFrom.contains(".")) {
            retVal = this.replaceSourceParentToken(valueFrom, event);
        } else if ("planid".equals(valueFrom)) {
            retVal = event.getSchedulePlanid();
        } else if ("planitemid".equals(valueFrom)) {
            retVal = event.getSchedulePlanitemid();
        } else if ("planitemdesc".equals(valueFrom)) {
            retVal = event.getSchedulePlanItemDesc();
        } else if ("monitorgroupid".equals(valueFrom)) {
            retVal = this.monitorGroupId != null ? this.monitorGroupId : "(null)";
        } else if ("eventnum".equals(valueFrom)) {
            retVal = String.valueOf(event.getEventNum());
        }
        return retVal;
    }

    public static void appendToPL(PropertyList map, String key, String value) {
        String valueToBeSaved;
        if (value == null) {
            value = "";
        }
        if ((valueToBeSaved = value).contains(";")) {
            valueToBeSaved = valueToBeSaved.replaceAll(";", "#semicolon#");
        }
        if (!map.containsKey(key)) {
            map.setProperty(key, valueToBeSaved);
        } else {
            String s = map.getProperty(key);
            map.setProperty(key, s + ";" + valueToBeSaved);
        }
    }

    @Override
    public void setMonitorGroupId(String monitorGroupId) {
        this.monitorGroupId = monitorGroupId;
    }

    @Override
    public void setCreateMonitorGroup(boolean createMonitorGroup) {
        this.createMonitorGroup = createMonitorGroup;
    }

    @Override
    public void setInstanceCount(Integer instanceCount) {
        this.instanceCount = instanceCount;
    }

    @Override
    public void setNewKeyidsMap(HashMap<String, ArrayList<String>> newKeyids) {
        this.newKeyidsMap = newKeyids;
    }
}

