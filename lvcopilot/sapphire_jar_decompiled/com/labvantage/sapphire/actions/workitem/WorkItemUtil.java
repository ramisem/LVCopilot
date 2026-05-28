/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workitem;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.PropertyList;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.eln.GenerateTestMethodWorksheet;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.WorkItemItemRuleEvaluator;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class WorkItemUtil {
    public static final String COLUMN_ASSIGNEDANALYST = "s_assignedanalyst";
    public static final String COLUMN_ASSIGNEDDEPT = "s_assigneddepartment";
    public static final String COLUMN_CREATEWORKSHEETRULE = "createworksheetrule";
    public static final String COLUMN_CREATELESRULE = "createlesrule";
    public static final String COLUMN_FORMRULE = "formrule";
    public static final String COLUMN_WORKSHEETRULE = "worksheetrule";
    public static final String COLUMN_TESTINGDEPARTMENTID = "testingdepartmentid";
    public static final String COLUMN_WORKAREADEPARTMENTID = "workareadepartmentid";
    public static final String COLUMN_AUTOASSIGNEDANALYSTID = "autoassignanalystid";
    public static final String COLUMN_AUTOASSIGNRULE = "autoassignrule";
    public static final String COLUMN_WAPSTATUS = "wapstatus";
    public static final String COLUMN_CREATEACTIVITYRULE = "createactivityrule";
    public static final String COLUMN_TESTINGLABTYPE = "testinglabtype";
    public static final String COLUMN_WORKAREATYPE = "workareatype";
    public static final String ACTIVITYRULE_ONDEMAND_BY_WORKITEM = "On Demand By Workitem";
    public static final String ACTIVITYRULE_ONDEMAND_BY_DATASET = "On Demand By DataSet";
    public static final String ACTIVITYRULE_ONDEMAND = "On Demand";
    public static final String AUTOASSIGNRULE_WORKAREA = "Workarea";
    public static final String AUTOASSIGNRULE_ANALYST = "Analyst";
    public static final String WS_ONCREATION = "On Creation";
    public static final String WS_ONASSIGNMENT = "On Assignment";

    public static boolean applyWorkItem(String transactionid, DBAccess database, ConnectionInfo connectionInfo, Logger logger, String sdcid, String keyid1, String keyid2, String keyid3, String rsetid, String workitemid, String workItemVersionId, String instanceList, String forceNewProp, String matchusersequence, DataSet sdiwiitem, boolean extras, Map sdidataColsPropValues) throws SapphireException {
        boolean poll = false;
        String currentsdc = "";
        String oldsdc = "";
        StringBuffer instances = new StringBuffer("");
        StringBuffer includesdiworkitemitems = new StringBuffer("");
        sapphire.xml.PropertyList actionproperties = new sapphire.xml.PropertyList();
        if (instanceList == null || instanceList.length() == 0) {
            instanceList = "1";
        }
        actionproperties.setProperty("__transactionid", transactionid);
        actionproperties.setProperty("sdcid", sdcid);
        actionproperties.setProperty("keyid1", keyid1);
        actionproperties.setProperty("keyid2", keyid2);
        actionproperties.setProperty("keyid3", keyid3);
        actionproperties.setProperty("rsetid", rsetid);
        actionproperties.setProperty("propsmatch", "N");
        actionproperties.setProperty("matchusersequence", matchusersequence);
        actionproperties.setProperty("sourceworkitemid", workitemid);
        actionproperties.setProperty("sourceworkiteminstance", instanceList);
        if (extras) {
            for (String columnId : sdidataColsPropValues.keySet()) {
                String columnValue = (String)sdidataColsPropValues.get(columnId);
                actionproperties.put(columnId, columnValue);
            }
        }
        String itemkeyid1list = "";
        String itemkeyid2list = "";
        String itemkeyid3list = "";
        String addNewOnly = "";
        String workitemitemid = "";
        DataSet workitemsdef = WorkItemUtil.getDefFromCache(database, connectionInfo, workitemid, workItemVersionId);
        DataSet sdidataColList = WorkItemUtil.getEditableColumnList(database, logger, "sdidata");
        for (int wii = 0; wii < workitemsdef.size(); ++wii) {
            String forceNewFlag;
            currentsdc = workitemsdef.getString(wii, "sdcid");
            if (!currentsdc.equals(oldsdc)) {
                if (oldsdc.length() > 0) {
                    DataSet paramListData = new DataSet();
                    if ("paramlist".equalsIgnoreCase(oldsdc)) {
                        paramListData = WorkItemUtil.buildParamListDataSet(actionproperties, sdidataColList, itemkeyid1list, itemkeyid2list, itemkeyid3list, addNewOnly, workitemitemid);
                    }
                    poll |= WorkItemUtil.applyWorkItemItem(database, connectionInfo, logger, actionproperties, oldsdc, itemkeyid1list, itemkeyid2list, instances, includesdiworkitemitems, paramListData, workitemsdef);
                    itemkeyid1list = "";
                    itemkeyid2list = "";
                    itemkeyid3list = "";
                    addNewOnly = "";
                    workitemitemid = "";
                }
                oldsdc = currentsdc;
            }
            String string = forceNewFlag = (forceNewFlag = workitemsdef.getString(wii, "forcenewflag")) != null && forceNewFlag.length() > 0 ? forceNewFlag.toUpperCase() : "X";
            if (forceNewProp != null && forceNewProp.equals("Y")) {
                forceNewFlag = "Y";
            } else if ("R".equalsIgnoreCase(forceNewProp) && "N".equalsIgnoreCase(forceNewFlag)) {
                forceNewFlag = "Y";
            }
            int repeatCount = workitemsdef.getInt(wii, "repeatcount", 1);
            for (int i = 0; i < repeatCount; ++i) {
                itemkeyid1list = itemkeyid1list + ";" + workitemsdef.getString(wii, "keyid1");
                itemkeyid2list = itemkeyid2list + ";" + workitemsdef.getString(wii, "keyid2");
                itemkeyid3list = itemkeyid3list + ";" + workitemsdef.getString(wii, "keyid3");
                addNewOnly = addNewOnly + ";" + (forceNewFlag.equals("Y") ? "N" : "Y");
                workitemitemid = workitemitemid + ";" + workitemsdef.getString(wii, "workitemitemid", "");
            }
        }
        if (currentsdc.length() > 0) {
            DataSet paramListData = new DataSet();
            if ("paramlist".equalsIgnoreCase(currentsdc)) {
                paramListData = WorkItemUtil.buildParamListDataSet(actionproperties, sdidataColList, itemkeyid1list, itemkeyid2list, itemkeyid3list, addNewOnly, workitemitemid);
            }
            poll |= WorkItemUtil.applyWorkItemItem(database, connectionInfo, logger, actionproperties, currentsdc, itemkeyid1list, itemkeyid2list, instances, includesdiworkitemitems, paramListData, workitemsdef);
        }
        String[] instancevalues = StringUtil.split(instances.length() > 1 ? instances.substring(1) : "", ";");
        String[] includesdiworkitemitemsFlags = StringUtil.split(includesdiworkitemitems.length() > 1 ? includesdiworkitemitems.substring(1) : "", ";");
        if (instances.length() == 0 && !currentsdc.equalsIgnoreCase("workitem")) {
            throw new SapphireException("No workitem items found for workitem " + workitemid);
        }
        int instanceValueCount = 0;
        String[] keyid1prop = StringUtil.split(keyid1, ";");
        String[] keyid2prop = StringUtil.split(keyid2, ";");
        String[] keyid3prop = StringUtil.split(keyid3, ";");
        String[] wiinstanceprop = StringUtil.split(instanceList, ";");
        String statementName = "Update DataSet Workitem info";
        PreparedStatement updateSDIData = database.prepareStatement(statementName, "UPDATE sdidata SET sourceworkitemid=?, sourceworkiteminstance=? WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? AND paramlistid=? AND paramlistversionid=? AND variantid=? AND dataset=? AND sourceworkitemid is null");
        ArrayList<DataSet> groupsBySDCLst = workitemsdef.getGroupedDataSets("sdcid");
        for (int gr = 0; gr < groupsBySDCLst.size(); ++gr) {
            DataSet workitemsdefgrp = (DataSet)groupsBySDCLst.get(gr);
            for (int sdi = 0; sdi < keyid1prop.length; ++sdi) {
                String tempkeyid1 = keyid1prop[sdi];
                String tempkeyid2 = keyid2prop.length < keyid1prop.length || keyid2prop[sdi].length() == 0 ? "(null)" : keyid2prop[sdi];
                String tempkeyid3 = keyid3prop.length < keyid1prop.length || keyid3prop[sdi].length() == 0 ? "(null)" : keyid3prop[sdi];
                String tempwiinstance = wiinstanceprop.length < keyid1prop.length || wiinstanceprop[sdi].length() == 0 ? "1" : wiinstanceprop[sdi];
                for (int wii = 0; wii < workitemsdefgrp.size(); ++wii) {
                    String itemsdcid = workitemsdefgrp.getValue(wii, "sdcid");
                    String itemkeyid1 = workitemsdefgrp.getString(wii, "keyid1");
                    String itemkeyid2 = workitemsdefgrp.getString(wii, "keyid2", "(null)");
                    String itemkeyid3 = workitemsdefgrp.getString(wii, "keyid3", "(null)");
                    int repeatCount = workitemsdefgrp.getInt(wii, "repeatcount", 1);
                    String workitemitemRules = workitemsdefgrp.getString(wii, "workitemitemrule", "");
                    String usersequence = workitemsdefgrp.getValue(wii, "usersequence", "0");
                    String forcenewflag = workitemsdefgrp.getValue(wii, "forcenewflag", "");
                    for (int repeatCounter = 0; repeatCounter < repeatCount; ++repeatCounter) {
                        int instance;
                        if (itemsdcid.equals("WorkItem")) continue;
                        if ("ParamList".equals(itemsdcid) && "N".equalsIgnoreCase(includesdiworkitemitemsFlags[instanceValueCount])) {
                            ++instanceValueCount;
                            continue;
                        }
                        int newrow = sdiwiitem.addRow();
                        sdiwiitem.setString(newrow, "workitemitemid", workitemsdefgrp.getString(wii, "workitemitemid") + "." + (repeatCounter + 1));
                        sdiwiitem.setString(newrow, "workitemid", workitemid);
                        sdiwiitem.setNumber(newrow, "workiteminstance", Integer.parseInt(tempwiinstance));
                        sdiwiitem.setString(newrow, "sdcid", sdcid);
                        sdiwiitem.setString(newrow, "keyid1", tempkeyid1);
                        sdiwiitem.setString(newrow, "keyid2", tempkeyid2);
                        sdiwiitem.setString(newrow, "keyid3", tempkeyid3);
                        sdiwiitem.setString(newrow, "completeflag", "N");
                        sdiwiitem.setString(newrow, "mandatoryflag", workitemsdefgrp.getString(wii, "mandatoryflag"));
                        sdiwiitem.setString(newrow, "itemsdcid", itemsdcid);
                        sdiwiitem.setString(newrow, "itemkeyid1", itemkeyid1);
                        sdiwiitem.setString(newrow, "itemkeyid2", itemkeyid2);
                        sdiwiitem.setString(newrow, "itemkeyid3", itemkeyid3);
                        sdiwiitem.setString(newrow, "workitemitemrule", workitemitemRules);
                        sdiwiitem.setNumber(newrow, "usersequence", usersequence);
                        sdiwiitem.setString(newrow, "forcenewflag", forcenewflag);
                        try {
                            instance = Integer.parseInt(instancevalues[instanceValueCount]);
                        }
                        catch (Exception ex) {
                            instance = 0;
                        }
                        if (instance != -9999) {
                            sdiwiitem.setNumber(newrow, "iteminstance", instance);
                        }
                        ++instanceValueCount;
                    }
                }
            }
        }
        QueryProcessor queryProcessor = new QueryProcessor(connectionInfo.getConnectionId());
        WorkItemUtil.resolveCurrentVersionWIItems(sdiwiitem, database, connectionInfo, queryProcessor);
        database.closeStatement(statementName);
        return poll;
    }

    public static DataSet getDefFromCache(DBAccess database, ConnectionInfo connectionInfo, String workitemid, String workItemVersionId) throws SapphireException {
        DataSet workitemsdef = (DataSet)CacheUtil.get(connectionInfo.getDatabaseId(), "Workitem", workitemid + ";" + workItemVersionId);
        if (workitemsdef == null) {
            workitemsdef = new DataSet();
            try {
                String sql = "SELECT workitemid, workitemversionid, workitemitemid, sdcid, keyid1, keyid2, keyid3, mandatoryflag, forcenewflag, repeatcount, usersequence, workitemitemrule, workareadepartmentid, autoassignanalystid, autoassignrule FROM workitemitem WHERE\tworkitemid=? AND workitemversionid=? ORDER BY sdcid, usersequence, keyid1, keyid2, keyid3";
                database.createPreparedResultSet("workitemitem", sql, new Object[]{workitemid, workItemVersionId});
                workitemsdef.setResultSet(database.getResultSet("workitemitem"));
                CacheUtil.put(connectionInfo.getDatabaseId(), "Workitem", workitemid + ";" + workItemVersionId, workitemsdef);
            }
            catch (SapphireException e) {
                throw new SapphireException("CREATE_RESULTSET_FAILED", "Unable to load up workitem details for " + workitemid + ";" + workItemVersionId, e);
            }
        }
        return workitemsdef;
    }

    public static DataSet getTestMethodDefFromCache(DBAccess database, ConnectionInfo connectionInfo, String workitemid, String workItemVersionId) throws SapphireException {
        DataSet workitemsdef = (DataSet)CacheUtil.get(connectionInfo.getDatabaseId(), "TestMethod", workitemid + ";" + workItemVersionId);
        if (workitemsdef == null) {
            workitemsdef = new DataSet();
            try {
                String sql = "SELECT * FROM workitem WHERE\tworkitemid = ? AND workitemversionid = ? ORDER BY workitemid, workitemversionid";
                database.createPreparedResultSet("workitem", sql, new Object[]{workitemid, workItemVersionId});
                workitemsdef.setResultSet(database.getResultSet("workitem"));
                CacheUtil.put(connectionInfo.getDatabaseId(), "TestMethod", workitemid + ";" + workItemVersionId, workitemsdef);
            }
            catch (SapphireException e) {
                throw new SapphireException("CREATE_RESULTSET_FAILED", "Unable to load up workitem details for " + workitemid + ";" + workItemVersionId, e);
            }
        }
        return workitemsdef;
    }

    public static DataSet getDepartmentDefFromCache(DBAccess database, ConnectionInfo connectionInfo, String departmentId) throws SapphireException {
        DataSet departmentdef = (DataSet)CacheUtil.get(connectionInfo.getDatabaseId(), "Department", departmentId);
        if (departmentdef == null) {
            departmentdef = new DataSet();
            try {
                String sql = "SELECT * FROM department WHERE departmentid = ?";
                database.createPreparedResultSet("department", sql, new Object[]{departmentId});
                departmentdef.setResultSet(database.getResultSet("department"));
                CacheUtil.put(connectionInfo.getDatabaseId(), "Department", departmentId, departmentdef);
            }
            catch (SapphireException e) {
                throw new SapphireException("CREATE_RESULTSET_FAILED", "Unable to load up Department details for " + departmentId, e);
            }
        }
        return departmentdef;
    }

    public static DataSet getParamListDefFromCache(DBAccess database, ConnectionInfo connectionInfo, String plId, String version, String variant) throws SapphireException {
        String paramlistKey = plId + ";" + version + ";" + variant;
        DataSet pldef = (DataSet)CacheUtil.get(connectionInfo.getDatabaseId(), "ParamList", paramlistKey);
        if (pldef == null) {
            pldef = new DataSet();
            try {
                String sql = "SELECT * FROM paramlist WHERE paramlistid = ? and paramlistversionid = ? and variantid = ?";
                database.createPreparedResultSet("paramlist", sql, new Object[]{plId, version, variant});
                pldef.setResultSet(database.getResultSet("paramlist"));
                CacheUtil.put(connectionInfo.getDatabaseId(), "ParamList", paramlistKey, pldef);
            }
            catch (SapphireException e) {
                throw new SapphireException("CREATE_RESULTSET_FAILED", "Unable to load up ParamList details for " + paramlistKey, e);
            }
        }
        return pldef;
    }

    private static boolean applyWorkItemItem(DBAccess database, ConnectionInfo connectionInfo, Logger logger, sapphire.xml.PropertyList initactionproperties, String itemsdcid, String itemkeyid1, String itemkeyid2, StringBuffer instances, StringBuffer includesdiworkitemitems, DataSet paramListData, DataSet workitemsdef) throws SapphireException {
        sapphire.xml.PropertyList actionproperties = new sapphire.xml.PropertyList(initactionproperties);
        if (itemkeyid1.charAt(0) == ';') {
            itemkeyid1 = itemkeyid1.substring(1);
            itemkeyid2 = itemkeyid2.substring(1);
        }
        String keyid1 = actionproperties.getProperty("keyid1");
        int keycount = StringUtil.split(keyid1, ";").length;
        SapphireConnection sapphireConnection = new SapphireConnection(database.getConnection(), connectionInfo);
        ActionService ac = new ActionService(sapphireConnection);
        if (itemsdcid.equalsIgnoreCase("paramlist")) {
            DataSet dsSDIs = new DataSet();
            dsSDIs.addColumnValues("keyid1", 0, actionproperties.getProperty("keyid1"), ";");
            dsSDIs.addColumnValues("keyid2", 0, actionproperties.getProperty("keyid2"), ";", "(null)");
            dsSDIs.addColumnValues("keyid3", 0, actionproperties.getProperty("keyid3"), ";", "(null)");
            dsSDIs.padColumns();
            if (actionproperties.containsKey("sourceworkiteminstance")) {
                dsSDIs.addColumnValues("sourceworkiteminstance", 0, actionproperties.getProperty("sourceworkiteminstance"), ";");
            }
            DataSet dsAllDataSets = new DataSet();
            paramListData.addColumn("__include", 0);
            paramListData.addColumn("__instance", 0);
            paramListData.addColumn("__paramlisttype", 0);
            paramListData.addColumn("__includesdiwii", 0);
            paramListData.addColumn("usersequence", 1);
            paramListData.addColumn("available", 0);
            ArrayList<String> columnsToRemove = new ArrayList<String>();
            columnsToRemove.add("__instance");
            columnsToRemove.add("__include");
            columnsToRemove.add("__paramlisttype");
            columnsToRemove.add("__includesdiwii");
            columnsToRemove.add("workitemitemid");
            columnsToRemove.add("keyid1");
            columnsToRemove.add("keyid2");
            columnsToRemove.add("keyid3");
            columnsToRemove.add("tracelogid");
            columnsToRemove.add("usersequence");
            boolean prepExists = WorkItemUtil.resolveCurrentVersionAndGetParamListType(paramListData, sapphireConnection, database);
            for (int i = 0; i < dsSDIs.getRowCount(); ++i) {
                String sdcId = actionproperties.getProperty("sdcid");
                SDI sdi = new SDI(sdcId, dsSDIs.getValue(i, "keyid1"), dsSDIs.getValue(i, "keyid2"), dsSDIs.getValue(i, "keyid3"));
                WorkItemItemRuleEvaluator.evaluateSDIIncludeAvailabilityRule(workitemsdef, sdi, paramListData, database, connectionInfo, prepExists);
                for (int p = 0; p < paramListData.getRowCount(); ++p) {
                    String workitemitemid;
                    int wiiRow;
                    if (!"Y".equalsIgnoreCase(paramListData.getValue(p, "__include"))) {
                        paramListData.setString(p, "__instance", "-9999");
                    }
                    if ((wiiRow = workitemsdef.findRow("workitemitemid", workitemitemid = paramListData.getValue(p, "workitemitemid"))) <= -1) continue;
                    paramListData.setValue(p, "usersequence", workitemsdef.getValue(wiiRow, "usersequence"));
                }
                paramListData.setString(-1, "keyid1", dsSDIs.getValue(i, "keyid1"));
                paramListData.setString(-1, "keyid2", dsSDIs.getValue(i, "keyid2"));
                paramListData.setString(-1, "keyid3", dsSDIs.getValue(i, "keyid3"));
                if (dsSDIs.isValidColumn("sourceworkiteminstance")) {
                    paramListData.setString(-1, "sourceworkiteminstance", dsSDIs.getValue(i, "sourceworkiteminstance"));
                }
                dsAllDataSets.copyRow(paramListData, -1, 1);
                paramListData.setString(-1, "__instance", "");
                paramListData.setString(-1, "__include", "");
                paramListData.setString(-1, "available", "");
                paramListData.setString(-1, "__includesdiwii", "");
            }
            HashMap<String, String> includeMap = new HashMap<String, String>();
            includeMap.put("__include", "Y");
            DataSet dsInclude = dsAllDataSets.getFilteredDataSet(includeMap);
            if (dsInclude.getRowCount() > 0) {
                dsInclude.sort("paramlistid, paramlistversionid, variantid, available D");
                ArrayList<DataSet> actionGroups = dsInclude.getGroupedDataSets("paramlistid, paramlistversionid, variantid, available");
                if (!(dsInclude.getRowCount() != dsAllDataSets.getRowCount() || actionGroups.size() != 1 && WorkItemUtil.isMultipleAvailabilityFlagForSameParamListFound(actionGroups))) {
                    String availabilityFlags = "";
                    for (int pl = 0; pl < paramListData.getRowCount(); ++pl) {
                        HashMap<String, String> findAvailabilityFlag = new HashMap<String, String>();
                        findAvailabilityFlag.put("paramlistid", paramListData.getValue(pl, "paramlistid"));
                        findAvailabilityFlag.put("paramlistversionid", paramListData.getValue(pl, "paramlistversionid"));
                        findAvailabilityFlag.put("variantid", paramListData.getValue(pl, "variantid"));
                        int plRow = dsInclude.findRow(findAvailabilityFlag);
                        if (plRow <= -1) continue;
                        availabilityFlags = availabilityFlags + ";" + dsInclude.getValue(plRow, "available");
                    }
                    actionproperties.setProperty("paramlistid", paramListData.getColumnValues("paramlistid", ";"));
                    actionproperties.setProperty("paramlistversionid", paramListData.getColumnValues("paramlistversionid", ";"));
                    actionproperties.setProperty("variantid", paramListData.getColumnValues("variantid", ";"));
                    actionproperties.setProperty("addnewonly", paramListData.getColumnValues("addnewonly", ";"));
                    actionproperties.setProperty("available", availabilityFlags.substring(1));
                    try {
                        logger.info("Calling addSDIDataset using: " + actionproperties);
                        sapphire.xml.PropertyList pl = new sapphire.xml.PropertyList(actionproperties);
                        pl.setProperty("applyworkitem", "Y");
                        ac.processAction("AddDataSet", "1", pl);
                        for (int i = 0; i < keycount; ++i) {
                            instances.append(";").append(pl.getProperty("newds" + i));
                        }
                    }
                    catch (Exception e) {
                        throw new SapphireException("DB_ACTION_FAILED", "Error calling AddDataSet action. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionInfo.getConnectionId())), e);
                    }
                    includesdiworkitemitems.append(";").append(dsAllDataSets.getColumnValues("__includesdiwii", ";"));
                } else {
                    HashMap<String, Integer> trackdsStartRow = new HashMap<String, Integer>();
                    dsInclude.sort("keyid1,keyid2,keyid3,workitemitemid na");
                    actionGroups = dsInclude.getGroupedDataSets("keyid1,keyid2,keyid3,workitemitemid");
                    for (int g = 0; g < actionGroups.size(); ++g) {
                        DataSet dsSDIActionGroup = actionGroups.get(g);
                        String sourceWIInstance = "";
                        if (dsSDIActionGroup.isValidColumn("sourceworkiteminstance")) {
                            sourceWIInstance = dsSDIActionGroup.getColumnValues("sourceworkiteminstance", ";");
                            dsSDIActionGroup.removeColumn("sourceworkiteminstance");
                        }
                        sapphire.xml.PropertyList addDataSetActionProps = WorkItemUtil.getAddDatasetActionPropValues(dsSDIActionGroup, columnsToRemove);
                        try {
                            sapphire.xml.PropertyList pl = new sapphire.xml.PropertyList(actionproperties);
                            pl.addPropertyList(new PropertyList(addDataSetActionProps));
                            if (sourceWIInstance.length() > 0) {
                                pl.setProperty("sourceworkiteminstance", sourceWIInstance);
                            }
                            logger.info("Calling addSDIDataset using: " + pl);
                            pl.setProperty("applyworkitem", "Y");
                            pl.setProperty("propsmatch", "Y");
                            pl.deleteProperty("__transactionid");
                            ac.processAction("AddDataSet", "1", pl);
                            DataSet dsInstances = new DataSet(pl.getProperty("newdatasetinstancexml"));
                            dsInstances.sort("keyid1, keyid2, keyid3");
                            ArrayList<DataSet> sdiGrps = dsInstances.getGroupedDataSets("keyid1, keyid2, keyid3");
                            for (int gr = 0; gr < sdiGrps.size(); ++gr) {
                                DataSet grpDS = sdiGrps.get(gr);
                                int start = 0;
                                for (int ds = 0; ds < grpDS.getRowCount(); ++ds) {
                                    int row;
                                    HashMap<String, String> findRow = new HashMap<String, String>();
                                    findRow.put("keyid1", grpDS.getString(ds, "keyid1"));
                                    findRow.put("keyid2", grpDS.getString(ds, "keyid2"));
                                    findRow.put("keyid3", grpDS.getString(ds, "keyid3"));
                                    findRow.put("paramlistid", grpDS.getString(ds, "paramlistid"));
                                    findRow.put("paramlistversionid", grpDS.getString(ds, "paramlistversionid"));
                                    findRow.put("variantid", grpDS.getString(ds, "variantid"));
                                    findRow.put("__includesdiwii", "Y");
                                    String plKey = grpDS.getString(ds, "keyid1") + ";" + grpDS.getString(ds, "keyid2") + ";" + grpDS.getString(ds, "keyid3") + ";" + grpDS.getString(ds, "paramlistid") + ";" + grpDS.getString(ds, "paramlistversionid") + ";" + grpDS.getString(ds, "variantid");
                                    if (trackdsStartRow.containsKey(plKey)) {
                                        start = (Integer)trackdsStartRow.get(plKey);
                                    }
                                    if ((row = dsAllDataSets.findRow(findRow, start)) <= -1) continue;
                                    dsAllDataSets.setString(row, "__instance", grpDS.getValue(ds, "dataset"));
                                    start = row + 1;
                                    trackdsStartRow.put(plKey, new Integer(start));
                                }
                            }
                            continue;
                        }
                        catch (Exception e) {
                            throw new SapphireException("DB_ACTION_FAILED", "Error calling AddDataSet action. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionInfo.getConnectionId())), e);
                        }
                    }
                    if (dsAllDataSets.getRowCount() > 0) {
                        instances.append(";").append(dsAllDataSets.getColumnValues("__instance", ";"));
                        includesdiworkitemitems.append(";").append(dsAllDataSets.getColumnValues("__includesdiwii", ";"));
                    }
                }
            } else {
                instances.append(";").append(dsAllDataSets.getColumnValues("__instance", ";"));
                includesdiworkitemitems.append(";").append(dsAllDataSets.getColumnValues("__includesdiwii", ";"));
            }
        } else if (itemsdcid.equalsIgnoreCase("SpecSDC")) {
            actionproperties.setProperty("specid", itemkeyid1);
            actionproperties.setProperty("specversionid", itemkeyid2);
            try {
                logger.info("Calling addSDISpec using: " + actionproperties);
                sapphire.xml.PropertyList pl = new sapphire.xml.PropertyList(actionproperties);
                ac.processAction("AddSDISpec", "1", pl);
                int specsadded = StringUtil.split(itemkeyid1, ";").length;
                for (int sdi = 0; sdi < keycount; ++sdi) {
                    for (int i = 0; i < specsadded; ++i) {
                        instances.append(";-1");
                        includesdiworkitemitems.append(";Y");
                    }
                }
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", "Error calling AddSDISpec action. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionInfo.getConnectionId())), e);
            }
        }
        return true;
    }

    private static boolean isMultipleAvailabilityFlagForSameParamListFound(ArrayList plGroups) {
        ArrayList<String> paramListKeys = new ArrayList<String>();
        for (int gr = 0; gr < plGroups.size(); ++gr) {
            DataSet grpDS = (DataSet)plGroups.get(gr);
            String paramlistid = grpDS.getValue(0, "paramlistid");
            String paramlistversionid = grpDS.getValue(0, "paramlistversionid");
            String variantid = grpDS.getValue(0, "variantid");
            String plKey = paramlistid + ";" + paramlistversionid + ";" + variantid;
            if (paramListKeys.contains(plKey)) {
                return true;
            }
            paramListKeys.add(plKey);
        }
        return false;
    }

    private static sapphire.xml.PropertyList getAddDatasetActionPropValues(DataSet dsAddDatasetActionProps, ArrayList removeColumns) {
        sapphire.xml.PropertyList actionProp = new sapphire.xml.PropertyList();
        DataSet dsUniqKeys = new DataSet();
        for (int i = 0; i < dsAddDatasetActionProps.getRowCount(); ++i) {
            String keyid1 = dsAddDatasetActionProps.getValue(i, "keyid1");
            String keyid2 = dsAddDatasetActionProps.getValue(i, "keyid2");
            String keyid3 = dsAddDatasetActionProps.getValue(i, "keyid3");
            HashMap<String, String> findKey = new HashMap<String, String>();
            findKey.put("keyid1", keyid1);
            findKey.put("keyid2", keyid2);
            findKey.put("keyid3", keyid3);
            if (dsUniqKeys.findRow(findKey) >= 0) continue;
            int r = dsUniqKeys.addRow();
            dsUniqKeys.setString(r, "keyid1", keyid1);
            dsUniqKeys.setString(r, "keyid2", keyid2);
            dsUniqKeys.setString(r, "keyid3", keyid3);
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("keyid1", dsUniqKeys.getString(0, "keyid1"));
        map.put("keyid2", dsUniqKeys.getString(0, "keyid2"));
        map.put("keyid3", dsUniqKeys.getString(0, "keyid3"));
        DataSet singleSDIParamListDatas = dsAddDatasetActionProps.getFilteredDataSet(map);
        actionProp.setProperty("keyid1", dsUniqKeys.getColumnValues("keyid1", ";"));
        actionProp.setProperty("keyid2", dsUniqKeys.getColumnValues("keyid2", ";"));
        actionProp.setProperty("keyid3", dsUniqKeys.getColumnValues("keyid3", ";"));
        String[] columns = singleSDIParamListDatas.getColumns();
        for (int c = 0; c < columns.length; ++c) {
            if (removeColumns.contains(columns[c])) continue;
            actionProp.setProperty(columns[c], singleSDIParamListDatas.getColumnValues(columns[c], ";"));
        }
        return actionProp;
    }

    public static DataSet getEditableColumnList(DBAccess database, Logger logger, String tableId) throws SapphireException {
        DataSet tableDef = null;
        try {
            StringBuffer getTableDef = new StringBuffer();
            getTableDef.append("select columnid, datatype from syscolumn ").append("where tableid = ? ");
            if (tableId.equalsIgnoreCase("sdiworkitem")) {
                getTableDef.append(" and columnid not in ('sdcid','keyid1','keyid2','keyid3'").append(", 'workitemid', 'workitemversionid', 'workiteminstance', 'workitemtypeflag'").append(", 'groupid', 'groupinstance'").append(", 'appliedflag', 'completeflag', 'usersequence'").append(", 'auditsequence' ").append(", 'tracelogid'").append(", 'createdt', 'createby', 'createtool', 'modtool', 'modby', 'moddt'").append(")");
            } else if (tableId.equalsIgnoreCase("sdidata")) {
                getTableDef.append(" and columnid not in ('sdcid','keyid1','keyid2','keyid3'").append(", 'paramlistid','paramlistversionid','variantid'").append(", 'sourceworkiteminstance'").append(", 'createdt', 'createby', 'createtool', 'modtool', 'modby', 'moddt'").append(")");
            }
            database.createPreparedResultSet("getTableDef", getTableDef.toString(), new Object[]{tableId});
            tableDef = new DataSet(database.getResultSet("getTableDef"));
            database.closeResultSet("getTableDef");
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not retrieve editable column list =>" + e.getMessage(), e);
        }
        finally {
            return tableDef;
        }
    }

    public static DataSet filterColumnIdsWithPropList(Logger logger, DataSet sourceColumnIds, sapphire.xml.PropertyList properties) throws SapphireException {
        DataSet newDS = new DataSet();
        try {
            for (int i = 0; i < sourceColumnIds.getRowCount(); ++i) {
                if (!properties.containsKey(sourceColumnIds.getString(i, "columnid"))) continue;
                newDS.copyRow(sourceColumnIds, i, 1);
            }
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not filter column ids =>" + e.getMessage(), e);
        }
        finally {
            return newDS;
        }
    }

    public static void fillDSWithProps(SDCProcessor sdcProcessor, TranslationProcessor tp, DataSet columnList, sapphire.xml.PropertyList sourcePL, DataSet destinationDS, String sdcId, boolean isSingleValue, int dsRowNum, int keyNumCount, String defaultSeparator, boolean isAllPropsString, String excludedCols) throws SapphireException {
        try {
            if (defaultSeparator == null || defaultSeparator.length() == 0) {
                defaultSeparator = ";";
            }
            List<Object> excludedColList = excludedCols != null ? Arrays.asList(StringUtil.split(excludedCols, ";")) : new ArrayList();
            String keyCol1Name = "";
            if ("DataSet".equals(sdcId)) {
                keyCol1Name = "paramlistid";
            } else if ("SDIWorkItem".equals(sdcId)) {
                keyCol1Name = "workitemid";
            }
            int noOfKeyColValues = StringUtil.split(sourcePL.getProperty(keyCol1Name), defaultSeparator).length;
            block8: for (int columnListCount = 0; columnListCount < columnList.size(); ++columnListCount) {
                String columnId = columnList.getString(columnListCount, "columnid");
                String columnValue = sourcePL.getProperty(columnId);
                if (excludedColList.contains(columnId)) continue;
                if (isAllPropsString) {
                    if (isSingleValue) {
                        destinationDS.setString(dsRowNum, columnId, WorkItemUtil.getSingleValue(columnId, columnValue, destinationDS, dsRowNum, keyNumCount, defaultSeparator, noOfKeyColValues, keyCol1Name, tp));
                        continue;
                    }
                    destinationDS.addColumnValues(columnId, 0, columnValue, defaultSeparator);
                    continue;
                }
                switch (columnList.getString(columnListCount, "datatype").charAt(0)) {
                    case 'C': 
                    case 'T': {
                        if (isSingleValue) {
                            destinationDS.setString(dsRowNum, columnId, WorkItemUtil.getSingleValue(columnId, columnValue, destinationDS, dsRowNum, keyNumCount, defaultSeparator, noOfKeyColValues, keyCol1Name, tp));
                            continue block8;
                        }
                        if (columnValue.indexOf(defaultSeparator) > -1) {
                            destinationDS.addColumnValues(columnId, 0, columnValue, defaultSeparator);
                            continue block8;
                        }
                        destinationDS.setString(-1, columnId, columnValue);
                        continue block8;
                    }
                    case 'N': {
                        if (isSingleValue) {
                            destinationDS.setNumber(dsRowNum, columnId, WorkItemUtil.getSingleValue(columnId, columnValue, destinationDS, dsRowNum, keyNumCount, defaultSeparator, noOfKeyColValues, keyCol1Name, tp));
                            continue block8;
                        }
                        if (columnValue.indexOf(defaultSeparator) > -1) {
                            destinationDS.addColumnValues(columnId, 1, columnValue, defaultSeparator);
                            continue block8;
                        }
                        destinationDS.setNumber(-1, columnId, columnValue);
                        continue block8;
                    }
                    case 'R': {
                        if (isSingleValue) {
                            destinationDS.setNumber(dsRowNum, columnId, WorkItemUtil.getSingleValue(columnId, columnValue, destinationDS, dsRowNum, keyNumCount, defaultSeparator, noOfKeyColValues, keyCol1Name, tp));
                            continue block8;
                        }
                        if (columnValue.indexOf(defaultSeparator) > -1) {
                            destinationDS.addColumnValues(columnId, 1, columnValue, defaultSeparator);
                            continue block8;
                        }
                        destinationDS.setNumber(-1, columnId, columnValue);
                        continue block8;
                    }
                    case 'D': {
                        if (isSingleValue) {
                            destinationDS.setDate(dsRowNum, columnId, WorkItemUtil.getSingleValue(columnId, columnValue, destinationDS, dsRowNum, keyNumCount, defaultSeparator, noOfKeyColValues, keyCol1Name, tp));
                        } else if (columnValue.indexOf(defaultSeparator) > -1) {
                            destinationDS.addColumnValues(columnId, 2, columnValue, defaultSeparator);
                        } else {
                            destinationDS.setDate(-1, columnId, columnValue);
                        }
                        if (sdcId == null) continue block8;
                        destinationDS.setTimeZoneInsensitive(columnId);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not fill Dataset with PropList =>" + e);
        }
    }

    public static String getSingleValue(String columnId, String columnValue, DataSet destinationDS, int dsRowNum, int keyNumCount, String defaultSeparator, int noOfKeyColValues, String keyCol1Name, TranslationProcessor tp) throws SapphireException {
        String[] colValueArr = StringUtil.split(columnValue, defaultSeparator);
        String singleColumnValue = "";
        if (colValueArr.length == 1) {
            singleColumnValue = colValueArr[0];
        } else if (colValueArr.length == noOfKeyColValues) {
            singleColumnValue = colValueArr[keyNumCount];
        } else {
            HashMap<String, String> tokenMap = new HashMap<String, String>();
            tokenMap.put("customcolname", columnId);
            tokenMap.put("keycol1name", keyCol1Name);
            throw new SapphireException("Validation", "VALIDATION", tp.translate("No. of values for [customcolname] are inconsistent with the no of [keycol1name].", tokenMap));
        }
        return singleColumnValue;
    }

    public static void deleteFromWorkItemItem(String rsetId, sapphire.xml.PropertyList actionProps, ConnectionInfo connectionInfo, TranslationProcessor tp, Logger logger, String sdcId, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        Logger.logInfo("Start - Deleting " + sdcId + " from associated Workitems");
        DataSet deletedSDIs = new DataSet();
        deletedSDIs.addColumnValues("sdcid", 0, actionProps.getProperty("sdcid"), ";");
        deletedSDIs.addColumnValues("keyid1", 0, actionProps.getProperty("keyid1"), ";");
        deletedSDIs.addColumnValues("keyid2", 0, actionProps.getProperty("keyid2"), ";");
        deletedSDIs.addColumnValues("keyid3", 0, actionProps.getProperty("keyid3"), ";");
        deletedSDIs.padColumn("sdcid");
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer allCnPSDISql = new StringBuffer();
        if ("ParamList".equalsIgnoreCase(sdcId)) {
            allCnPSDISql.append("SELECT DISTINCT p.paramlistid keyid1, p.paramlistversionid keyid2, p.variantid keyid3, p.versionstatus FROM paramlist p").append(" JOIN rsetitems r ON p.paramlistid = r.keyid1 AND p.variantid = r.keyid3").append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" WHERE (p.versionstatus = 'C' or p.versionstatus = 'P')").append(" AND p.paramlistversionid NOT IN (SELECT r2.keyid2 FROM rsetitems r2 WHERE r2.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND r2.keyid1 = p.paramlistid AND r2.keyid3 = p.variantid )");
        } else if ("SpecSDC".equalsIgnoreCase(sdcId)) {
            allCnPSDISql.append("SELECT DISTINCT s.specid keyid1, s.specversionid keyid2, '(null)' keyid3, s.versionstatus FROM spec s").append(" JOIN rsetitems r ON s.specid = r.keyid1 ").append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" WHERE (s.versionstatus = 'C' or s.versionstatus = 'P')").append(" AND s.specversionid NOT IN (SELECT r2.keyid2 FROM rsetitems r2 WHERE r2.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND r2.keyid1 = s.specid )");
        } else if ("WorkItem".equalsIgnoreCase(sdcId)) {
            allCnPSDISql.append("SELECT DISTINCT w.workitemid keyid1, w.workitemversionid keyid2, '(null)' keyid3, w.versionstatus FROM workitem w").append(" JOIN rsetitems r ON w.workitemid = r.keyid1 ").append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" WHERE (w.versionstatus = 'C' or w.versionstatus = 'P')").append(" AND w.workitemversionid NOT IN (SELECT r2.keyid2 FROM rsetitems r2 WHERE r2.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND r2.keyid1 = w.workitemid )");
        }
        DataSet allCnPSDIs = qp.getPreparedSqlDataSet(allCnPSDISql.toString(), safeSQL.getValues());
        StringBuffer allReferencingTestssql = new StringBuffer();
        safeSQL.reset();
        if (connectionInfo.getDbms().equals("ORA")) {
            allReferencingTestssql.append("SELECT DISTINCT w.workitemid, w.workitemversionid, w.workitemitemid, w.sdcid, w.keyid1, w.keyid2, nvl(w.keyid3, '(null)') keyid3 FROM workitemitem w ").append(" JOIN rsetitems r ON w.sdcid = r.sdcid AND w.keyid1 = r.keyid1 ");
            allReferencingTestssql.append(" AND nvl( w.keyid3, '(null)' ) = nvl( r.keyid3, '(null)' ) ");
        } else {
            allReferencingTestssql.append("SELECT DISTINCT w.workitemid, w.workitemversionid, w.workitemitemid, w.sdcid, w.keyid1, w.keyid2, ISNULL(w.keyid3, '(null)') keyid3 FROM workitemitem w ").append(" JOIN rsetitems r ON w.sdcid = r.sdcid AND w.keyid1 = r.keyid1 ");
            allReferencingTestssql.append(" AND ISNULL(w.keyid3, '(null)') = ISNULL(w.keyid3, '(null)') ");
        }
        allReferencingTestssql.append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId));
        DataSet allReferencingTests = qp.getPreparedSqlDataSet(allReferencingTestssql.toString(), safeSQL.getValues());
        DataSet deletableWII = new DataSet();
        HashMap<String, String> filtermap = new HashMap<String, String>();
        int deletedSDIsRowCount = deletedSDIs.getRowCount();
        for (int deletedSDIIndex = 0; deletedSDIIndex < deletedSDIsRowCount; ++deletedSDIIndex) {
            String deletedSDCId = deletedSDIs.getString(deletedSDIIndex, "sdcid");
            String deletedKeyId1 = deletedSDIs.getString(deletedSDIIndex, "keyid1");
            String deletedKeyId2 = deletedSDIs.getString(deletedSDIIndex, "keyid2");
            String deletedKeyId3 = deletedSDIs.getString(deletedSDIIndex, "keyid3", "(null)");
            filtermap.clear();
            filtermap.put("sdcid", deletedSDCId);
            filtermap.put("keyid1", deletedKeyId1);
            filtermap.put("keyid2", deletedKeyId2);
            filtermap.put("keyid3", deletedKeyId3);
            DataSet tempFilteredDS = allReferencingTests.getFilteredDataSet(filtermap);
            for (int index = 0; index < tempFilteredDS.getRowCount(); ++index) {
                WorkItemUtil.copyUniqueDataSetRow(tempFilteredDS, deletableWII, index);
            }
            filtermap.clear();
            filtermap.put("sdcid", deletedSDCId);
            filtermap.put("keyid1", deletedKeyId1);
            filtermap.put("keyid2", "C");
            filtermap.put("keyid3", deletedKeyId3);
            DataSet cReferencingTests = allReferencingTests.getFilteredDataSet(filtermap);
            if (cReferencingTests.getRowCount() <= 0 || !WorkItemUtil.isSDILastCurrent(deletedKeyId1, deletedKeyId2, deletedKeyId3, allCnPSDIs)) continue;
            for (int index = 0; index < cReferencingTests.getRowCount(); ++index) {
                WorkItemUtil.copyUniqueDataSetRow(cReferencingTests, deletableWII, index);
            }
        }
        if (deletableWII.getRowCount() > 0) {
            boolean confirmUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
            if (confirmUpdate) {
                sapphire.xml.PropertyList deleteProps = new sapphire.xml.PropertyList();
                deleteProps.setProperty("sdcid", "WorkItem");
                deleteProps.setProperty("linkid", "WorkItemItems");
                deleteProps.setProperty("separator", ";");
                deleteProps.setProperty("workitemid", deletableWII.getColumnValues("workitemid", ";"));
                deleteProps.setProperty("workitemversionid", deletableWII.getColumnValues("workitemversionid", ";"));
                deleteProps.setProperty("workitemitemid", deletableWII.getColumnValues("workitemitemid", ";"));
                ap.processAction("DeleteSDIDetail", "1", deleteProps);
            } else {
                throw new SapphireException("Referencing Test Method", "CONFIRM", tp.translate("Referencing Test Methods found. Continue?") + "<br>" + deletableWII.toHTML());
            }
        }
        Logger.logInfo("End - Deleting " + sdcId + " from associated Workitems");
    }

    public static void checkWICurrentUsage(String rsetId, QueryProcessor qp, TranslationProcessor tp, ConnectionInfo connectionInfo) throws SapphireException {
        String workitemsRefdAsCSql = "";
        SafeSQL safeSQL = new SafeSQL();
        workitemsRefdAsCSql = connectionInfo.isOracle() ? "SELECT swi.sdcid, swi.keyid1, swi.keyid2, swi.keyid3, swi.workitemid, (swi.sdcid || ';' || swi.keyid1 || ';' || swi.keyid2 || ';' || swi.keyid3) sdiid FROM  sdiworkitem swi, rsetitems r, workitem w WHERE swi.workitemid = r.keyid1 AND w.workitemid = r.keyid1 AND w.workitemversionid = r.keyid2 AND w.versionstatus IN ('C', 'P') AND swi.workitemversionid IS NULL AND r.rsetid = " + safeSQL.addVar(rsetId) : "SELECT swi.sdcid, swi.keyid1, swi.keyid2, swi.keyid3, swi.workitemid, (swi.sdcid + ';' + swi.keyid1 + ';' + swi.keyid2 + ';' + swi.keyid3) sdiid FROM  sdiworkitem swi, rsetitems r, workitem w WHERE swi.workitemid = r.keyid1 AND w.workitemid = r.keyid1 AND w.workitemversionid = r.keyid2 AND w.versionstatus IN ('C', 'P') AND swi.workitemversionid IS NULL AND r.rsetid = " + safeSQL.addVar(rsetId);
        DataSet workitemsRefdAsC = qp.getPreparedSqlDataSet(workitemsRefdAsCSql, safeSQL.getValues());
        if (workitemsRefdAsC == null) {
            throw new SapphireException("WorkItemUtil", "FAILURE", tp.translate("Exception occurred while retrieving SDI Workitems."));
        }
        if (workitemsRefdAsC.getRowCount() == 0) {
            return;
        }
        DataSet workitemIdsRefdAsC = new DataSet();
        for (int i = 0; i < workitemsRefdAsC.getRowCount(); ++i) {
            String workItemId = workitemsRefdAsC.getValue(i, "workitemid");
            if (workitemIdsRefdAsC.findRow("workitemid", workItemId) != -1) continue;
            int newRow = workitemIdsRefdAsC.addRow();
            workitemIdsRefdAsC.setString(newRow, "workitemid", workItemId);
        }
        safeSQL.reset();
        String allCnPSDISql = "SELECT wi.workitemid, count(wi.workitemid) noofversions FROM workitem wi WHERE wi.versionstatus IN ('C', 'P') AND wi.workitemid IN (" + safeSQL.addIn(workitemIdsRefdAsC.getColumnValues("workitemid", "','")) + ") GROUP BY wi.workitemid";
        DataSet allCnPSDIs = qp.getPreparedSqlDataSet(allCnPSDISql, safeSQL.getValues());
        if (allCnPSDIs == null) {
            throw new SapphireException("WorkItemUtil", "FAILURE", tp.translate("Exception occurred while retrieving Workitem list."));
        }
        StringBuffer badWorkItems = new StringBuffer();
        HashMap<String, String> filterMap = new HashMap<String, String>();
        int errorMsgCounter = 0;
        for (int i = 0; i < allCnPSDIs.getRowCount(); ++i) {
            if (allCnPSDIs.getInt(i, "noofversions") < 2) {
                ++errorMsgCounter;
                String workitemId = allCnPSDIs.getValue(i, "workitemid");
                filterMap.clear();
                filterMap.put("workitemid", workitemId);
                badWorkItems.append("<br>").append(workitemId).append(": ").append(workitemsRefdAsC.getFilteredDataSet(filterMap).getColumnValues("sdiid", ", "));
            }
            if (errorMsgCounter <= 9) continue;
            badWorkItems.append("<br>...");
        }
        if (badWorkItems.length() > 2) {
            throw new SapphireException("WorkItemUtil", "VALIDATION", tp.translate("Some of the selected worktems are being referenced as Current with no other available versions. Cannot proceed with deletion: ") + badWorkItems);
        }
    }

    private static boolean isSDILastCurrent(String keyid1, String keyid2, String keyid3, DataSet allCnPsdis) {
        boolean isSDILastCurrent = true;
        HashMap<String, String> filtermap = new HashMap<String, String>();
        filtermap.put("keyid1", keyid1);
        filtermap.put("keyid3", keyid3);
        DataSet tempFilteredDS = allCnPsdis.getFilteredDataSet(filtermap);
        if (tempFilteredDS.getRowCount() > 0) {
            isSDILastCurrent = false;
        }
        return isSDILastCurrent;
    }

    private static void copyUniqueDataSetRow(DataSet fromDataSet, DataSet toDataSet, int rowNum) {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        String[] columnsArr = fromDataSet.getColumns();
        int columnCount = fromDataSet.getColumnCount();
        for (int i = 0; i < columnCount; ++i) {
            filterMap.put(columnsArr[i], fromDataSet.getString(rowNum, columnsArr[i]));
        }
        DataSet filterDS = toDataSet.getFilteredDataSet(filterMap);
        if (filterDS.getRowCount() == 0) {
            toDataSet.copyRow(fromDataSet, rowNum, 1);
        }
    }

    private static void resolveWorkItemItemCurrentVersion(DataSet plData, SapphireConnection sc, DBAccess database) throws SapphireException {
        for (int i = 0; i < plData.getRowCount(); ++i) {
            String plVersion = plData.getValue(i, "keyid2");
            if (!"c".equalsIgnoreCase(plVersion) && !"".equals(plVersion) && !"null".equals(plVersion)) continue;
            plData.setValue(i, "keyid2", "C");
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid2", "C");
        DataSet currentVersionPL = plData.getFilteredDataSet(filter);
        if (currentVersionPL.getRowCount() > 0) {
            String versionIdList = SdiInfo.getCurrentVersion("ParamList", currentVersionPL.getColumnValues("keyid1", ";"), currentVersionPL.getColumnValues("keyid3", ";"), sc);
            currentVersionPL.addColumnValues("keyid2", 0, versionIdList, ";");
        }
    }

    private static boolean resolveCurrentVersionAndGetParamListType(DataSet plData, SapphireConnection sc, DBAccess database) throws SapphireException {
        boolean prepExists = false;
        for (int i = 0; i < plData.getRowCount(); ++i) {
            String plVersion = plData.getValue(i, "paramlistversionid");
            if (!"c".equalsIgnoreCase(plVersion) && !"".equals(plVersion) && !"null".equals(plVersion)) continue;
            plData.setValue(i, "paramlistversionid", "C");
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("paramlistversionid", "C");
        DataSet currentVersionPL = plData.getFilteredDataSet(filter);
        if (currentVersionPL.getRowCount() > 0) {
            String versionIdList = SdiInfo.getCurrentVersion("ParamList", currentVersionPL.getColumnValues("paramlistid", ";"), currentVersionPL.getColumnValues("variantid", ";"), sc);
            currentVersionPL.addColumnValues("paramlistversionid", 0, versionIdList, ";");
        }
        PreparedStatement getParamListType = database.prepareStatement("getParamListType", "select s_paramlisttype from paramlist where paramlistid = ? and paramlistversionid = ? and variantid = ?");
        try {
            for (int i = 0; i < plData.getRowCount(); ++i) {
                String paramlistId = plData.getValue(i, "paramlistid");
                String paramlistVersionId = plData.getValue(i, "paramlistversionid");
                String variantId = plData.getValue(i, "variantid");
                getParamListType.setString(1, paramlistId);
                getParamListType.setString(2, paramlistVersionId);
                getParamListType.setString(3, variantId);
                DataSet dsPlType = new DataSet(getParamListType.executeQuery());
                if (dsPlType.getRowCount() > 0) {
                    String plType = dsPlType.getValue(0, "s_paramlisttype");
                    plData.setValue(i, "__paramlisttype", plType);
                    if (!"Preparation".equalsIgnoreCase(plType)) continue;
                    prepExists = true;
                    continue;
                }
                throw new SapphireException("ParamListType cannot be determined for ParamList: " + paramlistId + ";" + paramlistVersionId + ";" + variantId);
            }
        }
        catch (SQLException e) {
            throw new SapphireException("Unable to get paramlisttype of the ParamList.", e);
        }
        finally {
            database.closeStatement("getParamListType");
        }
        return prepExists;
    }

    public static void resolveCurrentVersionWIItems(DataSet sdiWorkItemItemDS, DBAccess database, ConnectionInfo connectionInfo, QueryProcessor queryProcessor) throws SapphireException {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        ArrayList<DataSet> sdiWiiGrouped = sdiWorkItemItemDS.getGroupedDataSets("itemsdcid");
        for (DataSet sdiWii : sdiWiiGrouped) {
            if (sdiWii.getRowCount() <= 0) continue;
            String sdcid = sdiWii.getString(0, "itemsdcid");
            filterMap.put("itemsdcid", sdcid);
            filterMap.put("itemkeyid2", "C");
            DataSet paramListItemsDS = sdiWii.getFilteredDataSet(filterMap);
            int rows = paramListItemsDS.getRowCount();
            if (rows <= 0) continue;
            String versionIdList = "";
            versionIdList = SdiInfo.getCurrentVersion(sdcid, paramListItemsDS.getColumnValues("itemkeyid1", ";"), paramListItemsDS.getColumnValues("itemkeyid3", ";"), new SapphireConnection(database.getConnection(), connectionInfo));
            paramListItemsDS.addColumnValues("itemkeyid2", 0, versionIdList, ";");
        }
    }

    public static int addDataForms(DataSet dataForms, String sdcId, String keyId1, String keyId2, String keyId3, String workItemId, String workItemInstance, String formId, String formVersionId, String assignedAnalyst, String assignedDept, TranslationProcessor tp) throws SapphireException {
        if (formId == null || formId.length() == 0) {
            throw new SapphireException("GENERAL_ERROR", tp.translate("No default/supplied Form found for") + " " + workItemId);
        }
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put("sdcid", sdcId);
        findMap.put("keyid1", keyId1);
        findMap.put("keyid2", keyId2);
        findMap.put("keyid3", keyId3);
        findMap.put("workitemid", workItemId);
        findMap.put("workiteminstance", new BigDecimal(workItemInstance));
        int newRow = dataForms.findRow(findMap);
        if (newRow == -1) {
            newRow = dataForms.addRow();
            dataForms.setString(newRow, "sdcid", sdcId);
            dataForms.setString(newRow, "keyid1", keyId1);
            dataForms.setString(newRow, "keyid2", keyId2);
            dataForms.setString(newRow, "keyid3", keyId3);
            dataForms.setString(newRow, "workitemid", workItemId);
            dataForms.setNumber(newRow, "workiteminstance", workItemInstance);
            dataForms.setString(newRow, "formid", formId);
            dataForms.setString(newRow, "formversionid", formVersionId);
            if (assignedAnalyst != null && assignedAnalyst.length() > 0) {
                dataForms.setString(newRow, COLUMN_ASSIGNEDANALYST, assignedAnalyst);
            }
            if (assignedDept != null && assignedDept.length() > 0) {
                dataForms.setString(newRow, COLUMN_ASSIGNEDDEPT, assignedDept);
            }
        }
        return newRow;
    }

    public static int addWorksheets(DataSet worksheets, String sdcId, String keyId1, String keyId2, String keyId3, String workItemId, String workItemInstance, String worksheetId, String worksheetVersionId, String workbookId, String workbookVersionId, String assignedAnalyst, String sdiworkitemid, TranslationProcessor tp) throws SapphireException {
        if (worksheetId == null || worksheetId.length() == 0) {
            throw new SapphireException("GENERAL_ERROR", tp.translate("No default/supplied worksheet found for") + " " + workItemId);
        }
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put("sdcid", sdcId);
        findMap.put("keyid1", keyId1);
        findMap.put("keyid2", keyId2);
        findMap.put("keyid3", keyId3);
        findMap.put("workitemid", workItemId);
        findMap.put("workiteminstance", new BigDecimal(workItemInstance));
        int newRow = worksheets.findRow(findMap);
        if (newRow == -1) {
            newRow = worksheets.addRow();
            worksheets.setString(newRow, "sdcid", sdcId);
            worksheets.setString(newRow, "keyid1", keyId1);
            worksheets.setString(newRow, "keyid2", keyId2);
            worksheets.setString(newRow, "keyid3", keyId3);
            worksheets.setString(newRow, "workitemid", workItemId);
            worksheets.setNumber(newRow, "workiteminstance", workItemInstance);
            worksheets.setString(newRow, "worksheetid", worksheetId);
            worksheets.setString(newRow, "worksheetversionid", worksheetVersionId);
            worksheets.setString(newRow, "workbookid", workbookId);
            worksheets.setString(newRow, "workbookversionid", workbookVersionId);
            worksheets.setString(newRow, "sdiworkitemid", sdiworkitemid);
            if (assignedAnalyst != null && assignedAnalyst.length() > 0) {
                worksheets.setString(newRow, COLUMN_ASSIGNEDANALYST, assignedAnalyst);
            }
        }
        return newRow;
    }

    public static void addToWorkflow(DataSet sdiwi, DataSet workItemWorkflowRule, String sdcid, ActionProcessor actionProcessor) throws SapphireException {
        ActionBlock addToWorkflowBlock = null;
        for (int i = 0; i < sdiwi.getRowCount(); ++i) {
            boolean appliedFlg = sdiwi.getValue(i, "appliedflag", "N").equalsIgnoreCase("Y");
            if (!appliedFlg) continue;
            HashMap<String, String> ruleFilterMap = new HashMap<String, String>();
            ruleFilterMap.put("workitemid", sdiwi.getValue(i, "workitemid"));
            ruleFilterMap.put("workitemversionid", sdiwi.getValue(i, "workitemversionid"));
            DataSet workflowrule = workItemWorkflowRule.getFilteredDataSet(ruleFilterMap);
            for (int wfr = 0; wfr < workflowrule.getRowCount(); ++wfr) {
                sapphire.xml.PropertyList addToWorkflowProps = new sapphire.xml.PropertyList();
                boolean addModeWorkItem = workflowrule.getValue(wfr, "addmodeflag", "P").equalsIgnoreCase("W");
                if (addModeWorkItem) {
                    addToWorkflowProps.setProperty("sdcid", "SDIWorkItem");
                    addToWorkflowProps.setProperty("keyid1", sdiwi.getValue(i, "sdiworkitemid"));
                } else {
                    addToWorkflowProps.setProperty("sdcid", sdcid);
                    addToWorkflowProps.setProperty("keyid1", sdiwi.getValue(i, "keyid1"));
                    if (!sdiwi.getValue(i, "keyid2", "(null)").equals("(null)")) {
                        addToWorkflowProps.setProperty("keyid2", sdiwi.getValue(i, "keyid2"));
                    }
                    if (!sdiwi.getValue(i, "keyid3", "(null)").equals("(null)")) {
                        addToWorkflowProps.setProperty("keyid3", sdiwi.getValue(i, "keyid3"));
                    }
                }
                addToWorkflowProps.setProperty("workflowdefid", workflowrule.getString(wfr, "workflowdefid"));
                addToWorkflowProps.setProperty("workflowdefversionid", workflowrule.getString(wfr, "workflowdefversionid"));
                addToWorkflowProps.setProperty("workflowdefvariantid", workflowrule.getString(wfr, "workflowdefvariantid"));
                addToWorkflowProps.setProperty("workflowexecid", workflowrule.getString(wfr, "workflowexecid"));
                addToWorkflowProps.setProperty("taskdefitemid", workflowrule.getString(wfr, "taskdefitemid"));
                addToWorkflowProps.setProperty("ioitemid", workflowrule.getString(wfr, "ioitemid"));
                if (addToWorkflowBlock == null) {
                    addToWorkflowBlock = new ActionBlock();
                }
                addToWorkflowBlock.setAction("AddToWorkflow" + i + "_" + wfr, "AddToWorkflow", "1", addToWorkflowProps);
            }
        }
        if (addToWorkflowBlock != null) {
            actionProcessor.processActionBlock(addToWorkflowBlock);
        }
    }

    public static void createWorksheet(DataSet sdiForms, ActionProcessor ap, Logger logger) throws SapphireException {
        logger.info("Start creating worksheet.");
        if (sdiForms.getRowCount() > 0) {
            sdiForms.sort("formid,formversionid,s_assignedanalyst,s_assigneddepartment");
            ArrayList<DataSet> groupedDataForms = sdiForms.getGroupedDataSets("formid,formversionid,s_assignedanalyst,s_assigneddepartment");
            for (int i = 0; i < groupedDataForms.size(); ++i) {
                DataSet tempWorkItem = (DataSet)groupedDataForms.get(i);
                sapphire.xml.PropertyList createWSProps = new sapphire.xml.PropertyList();
                createWSProps.setProperty("sdcid", tempWorkItem.getString(0, "sdcid"));
                createWSProps.setProperty("keyid1", tempWorkItem.getColumnValues("keyid1", ";"));
                createWSProps.setProperty("keyid2", tempWorkItem.getColumnValues("keyid2", ";"));
                createWSProps.setProperty("keyid3", tempWorkItem.getColumnValues("keyid3", ";"));
                createWSProps.setProperty("workitemid", tempWorkItem.getColumnValues("workitemid", ";"));
                createWSProps.setProperty("workiteminstance", tempWorkItem.getColumnValues("workiteminstance", ";"));
                createWSProps.setProperty("formid", tempWorkItem.getString(0, "formid", ""));
                createWSProps.setProperty("formversionid", tempWorkItem.getString(0, "formversionid", ""));
                if (sdiForms.isValidColumn(COLUMN_ASSIGNEDANALYST)) {
                    createWSProps.setProperty("assignto", tempWorkItem.getString(0, COLUMN_ASSIGNEDANALYST, ""));
                }
                if (sdiForms.isValidColumn(COLUMN_ASSIGNEDDEPT)) {
                    createWSProps.setProperty("assigntodepartment", tempWorkItem.getString(0, COLUMN_ASSIGNEDDEPT, ""));
                }
                ap.processAction("CreateWorksheet", "1", createWSProps);
                tempWorkItem.addColumnValues("documentid", 0, createWSProps.getProperty("documentid"), ";");
                tempWorkItem.addColumnValues("documentversionid", 0, createWSProps.getProperty("documentversionid"), ";");
            }
        } else {
            logger.info("No Data Forms present.");
        }
        logger.info("Finish creating worksheet.");
    }

    public static void createLESWorksheets(DataSet sdiWorksheets, ActionProcessor ap, Logger logger) throws SapphireException {
        logger.info("Start creating LES worksheets");
        if (sdiWorksheets.getRowCount() > 0) {
            sdiWorksheets.sort("workitemid,worksheetid,worksheetversionid,s_assignedanalyst");
            ArrayList<DataSet> groupedWorksheets = sdiWorksheets.getGroupedDataSets("workitemid,worksheetid,worksheetversionid,s_assignedanalyst");
            for (int i = 0; i < groupedWorksheets.size(); ++i) {
                DataSet tempWorkItem = (DataSet)groupedWorksheets.get(i);
                sapphire.xml.PropertyList createWSProps = new sapphire.xml.PropertyList();
                createWSProps.setProperty("sdiworkitemid", tempWorkItem.getColumnValues("sdiworkitemid", ";"));
                createWSProps.setProperty("templateid", tempWorkItem.getString(0, "worksheetid"));
                createWSProps.setProperty("templateversionid", tempWorkItem.getString(0, "worksheetversionid"));
                createWSProps.setProperty("workbookid", tempWorkItem.getString(0, "workbookid"));
                createWSProps.setProperty("workbookversionid", tempWorkItem.getString(0, "workbookversionid"));
                if (sdiWorksheets.isValidColumn(COLUMN_ASSIGNEDANALYST)) {
                    createWSProps.setProperty("authorid", tempWorkItem.getString(0, COLUMN_ASSIGNEDANALYST));
                }
                ap.processActionClass(GenerateTestMethodWorksheet.class.getName(), createWSProps);
            }
        } else {
            logger.info("No LES worksheets to create");
        }
        logger.info("Finish creating LES worksheets");
    }

    @Deprecated
    public static DataSet getWorkItemForms(String workItemIdProp, String workItemVersionIdProp, QueryProcessor qp, Logger logger) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT sdiformrule.keyid1 workitemid, sdiformrule.keyid2 workitemversionid, workitem.createworksheetrule, sdiformrule.formid, sdiformrule.formversionid, sdiformrule.formrule FROM workitem, sdiformrule WHERE workitem.workitemid = sdiformrule.keyid1 " + (workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? "AND workitem.workitemversionid = sdiformrule.keyid2" : "") + " AND sdiformrule.sdcid = 'WorkItem' AND (";
        String[] id = StringUtil.split(workItemIdProp, ";");
        String[] ver = workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? StringUtil.split(workItemVersionIdProp, ";") : null;
        StringBuffer clause = new StringBuffer();
        for (int i = 0; i < id.length; ++i) {
            clause.append(" OR ( keyid1 = ").append(safeSQL.addVar(id[i])).append(" ").append(ver != null && i < ver.length ? "AND keyid2 = " + safeSQL.addVar(ver[i]) + ")" : ")");
        }
        logger.info("WorkItem Form Rules sql: " + sql + clause.substring(4) + ")");
        return qp.getPreparedSqlDataSet(sql + clause.substring(4) + ")", safeSQL.getValues());
    }

    public static DataSet getWorkItemForms(String workItemIdProp, String workItemVersionIdProp, QueryProcessor qp, Logger logger, String rsetId) throws SapphireException {
        String sql = "SELECT sdiformrule.keyid1 workitemid, sdiformrule.keyid2 workitemversionid, workitem.createworksheetrule, sdiformrule.formid, sdiformrule.formversionid, sdiformrule.formrule  FROM workitem, sdiformrule, rsetitems  WHERE workitem.workitemid = sdiformrule.keyid1 " + (workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? "AND workitem.workitemversionid = sdiformrule.keyid2" : "") + " AND sdiformrule.sdcid = 'WorkItem'  AND workitem.workitemid = rsetitems.keyid1 " + (workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? "AND workitem.workitemversionid = rsetitems.keyid2" : "") + " AND rsetitems.sdcid = 'WorkItem' AND rsetitems.rsetid = ?";
        logger.info("WorkItem Worksheet Template Rules sql: " + sql);
        return qp.getPreparedSqlDataSet(sql, (Object[])new String[]{rsetId});
    }

    @Deprecated
    public static DataSet getWorkItemWorksheets(String workItemIdProp, String workItemVersionIdProp, QueryProcessor qp, Logger logger) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT sdiworksheetrule.keyid1 workitemid, sdiworksheetrule.keyid2 workitemversionid, workitem.createlesrule, sdiworksheetrule.authorflag, sdiworksheetrule.worksheetid, sdiworksheetrule.worksheetversionid, sdiworksheetrule.workbookid, sdiworksheetrule.workbookversionid, sdiworksheetrule.worksheetrule FROM workitem, sdiworksheetrule WHERE workitem.workitemid = sdiworksheetrule.keyid1 " + (workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? "AND workitem.workitemversionid = sdiworksheetrule.keyid2" : "") + " AND sdiworksheetrule.sdcid = 'WorkItem' AND (";
        String[] id = StringUtil.split(workItemIdProp, ";");
        String[] ver = workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? StringUtil.split(workItemVersionIdProp, ";") : null;
        StringBuffer clause = new StringBuffer();
        for (int i = 0; i < id.length; ++i) {
            clause.append(" OR ( keyid1 = ").append(safeSQL.addVar(id[i])).append(" ").append(ver != null && i < ver.length ? "AND keyid2 = " + safeSQL.addVar(ver[i]) + ")" : ")");
        }
        logger.info("WorkItem Worksheet Template Rules sql: " + sql + clause.substring(4) + ")");
        return qp.getPreparedSqlDataSet(sql + clause.substring(4) + ")", safeSQL.getValues());
    }

    public static DataSet getWorkItemWorksheets(String workItemIdProp, String workItemVersionIdProp, QueryProcessor qp, Logger logger, String rsetId) throws SapphireException {
        String sql = "SELECT sdiworksheetrule.keyid1 workitemid, sdiworksheetrule.keyid2 workitemversionid, workitem.createlesrule, sdiworksheetrule.authorflag, sdiworksheetrule.worksheetid, sdiworksheetrule.worksheetversionid, sdiworksheetrule.workbookid, sdiworksheetrule.workbookversionid, sdiworksheetrule.worksheetrule  FROM workitem, sdiworksheetrule, rsetitems  WHERE workitem.workitemid = sdiworksheetrule.keyid1 " + (workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? "AND workitem.workitemversionid = sdiworksheetrule.keyid2" : "") + " AND sdiworksheetrule.sdcid = 'WorkItem'  AND workitem.workitemid = rsetitems.keyid1 " + (workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? "AND workitem.workitemversionid = rsetitems.keyid2" : "") + " AND rsetitems.sdcid = 'WorkItem' AND rsetitems.rsetid = ?";
        logger.info("WorkItem Worksheet Template Rules sql: " + sql);
        return qp.getPreparedSqlDataSet(sql, (Object[])new String[]{rsetId});
    }

    @Deprecated
    public static DataSet getWorkItemWorkflowRules(String workItemIdProp, String workItemVersionIdProp, QueryProcessor qp, Logger logger) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT keyid1 workitemid, keyid2 workitemversionid, workflowdefid, workflowdefversionid, workflowdefvariantid, workflowexecid, taskdefitemid, ioitemid, addmodeflag FROM sdiworkflowrule WHERE sdcid = 'WorkItem' AND (";
        String[] id = StringUtil.split(workItemIdProp, ";");
        String[] ver = workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? StringUtil.split(workItemVersionIdProp, ";") : null;
        StringBuffer clause = new StringBuffer();
        for (int i = 0; i < id.length; ++i) {
            clause.append(" OR ( keyid1 = ").append(safeSQL.addVar(id[i])).append(" ").append(ver != null && i < ver.length ? "AND keyid2 = " + safeSQL.addVar(ver[i]) + ")" : ")");
        }
        logger.info("WorkItem WorkflowRules sql: " + sql + clause.substring(4) + ")");
        DataSet workItemWorkflowRules = qp.getPreparedSqlDataSet(sql + clause.substring(4) + ")", safeSQL.getValues());
        return workItemWorkflowRules;
    }

    public static DataSet getWorkItemWorkflowRules(String workItemIdProp, String workItemVersionIdProp, QueryProcessor qp, Logger logger, String rsetId) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT sdiworkflowrule.keyid1 workitemid, sdiworkflowrule.keyid2 workitemversionid, sdiworkflowrule.workflowdefid, sdiworkflowrule.workflowdefversionid, sdiworkflowrule.workflowdefvariantid, sdiworkflowrule.workflowexecid, sdiworkflowrule.taskdefitemid, sdiworkflowrule.ioitemid, sdiworkflowrule.addmodeflag FROM sdiworkflowrule, workitem, rsetitems WHERE sdiworkflowrule.sdcid = 'WorkItem'  AND workitem.workitemid = rsetitems.keyid1 " + (workItemVersionIdProp != null && workItemVersionIdProp.length() > 0 ? "AND workitem.workitemversionid = rsetitems.keyid2" : "") + " AND rsetitems.sdcid = 'WorkItem' AND rsetitems.rsetid = ?";
        logger.info("WorkItem WorkflowRules sql: " + sql);
        return qp.getPreparedSqlDataSet(sql, (Object[])new String[]{rsetId});
    }

    private static DataSet buildParamListDataSet(sapphire.xml.PropertyList actionProps, DataSet sdidataColList, String itemkeyid1, String itemkeyid2, String itemkeyid3, String addnewonly, String workitemitemid) {
        if (itemkeyid1.charAt(0) == ';') {
            itemkeyid1 = itemkeyid1.substring(1);
            itemkeyid2 = itemkeyid2.substring(1);
            itemkeyid3 = itemkeyid3.substring(1);
            addnewonly = addnewonly.substring(1);
            workitemitemid = workitemitemid.substring(1);
        }
        DataSet dsParamLists = new DataSet();
        dsParamLists.addColumnValues("paramlistid", 0, itemkeyid1, ";");
        dsParamLists.addColumnValues("paramlistversionid", 0, itemkeyid2, ";");
        dsParamLists.addColumnValues("variantid", 0, itemkeyid3, ";");
        dsParamLists.addColumnValues("addnewonly", 0, addnewonly, ";");
        dsParamLists.addColumnValues("workitemitemid", 0, workitemitemid, ";");
        for (int i = 0; i < sdidataColList.size(); ++i) {
            String columnId = sdidataColList.getString(i, "columnid");
            if (!actionProps.containsKey(columnId)) continue;
            dsParamLists.addColumnValues(columnId, 0, actionProps.getProperty(columnId), ";");
            dsParamLists.padColumn(columnId);
        }
        return dsParamLists;
    }

    public static void addWorkItemDataSetAttributeAndRelations(DataSet sdiwiwi, DBAccess database, ActionProcessor ap, String workitemitemid, ConnectionInfo connectionInfo, SDCProcessor sdcProcessor, Logger logger) throws SapphireException {
        String sql = "select sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance, workitemversionid from sdiworkitem where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? and workiteminstance = ?";
        PreparedStatement getSDIWI = database.prepareStatement("getsdiwi", sql);
        sdiwiwi.sort("sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance");
        ArrayList<DataSet> wiinstanceGrps = sdiwiwi.getGroupedDataSets("sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance");
        DataSet sdiwi = new DataSet();
        try {
            for (int i = 0; i < wiinstanceGrps.size(); ++i) {
                DataSet sdiwii = wiinstanceGrps.get(i);
                String sdcid = sdiwii.getValue(0, "sdcid");
                String keyid1 = sdiwii.getValue(0, "keyid1");
                String keyid2 = sdiwii.getValue(0, "keyid2");
                String keyid3 = sdiwii.getValue(0, "keyid3");
                String workItemId = sdiwii.getValue(0, "workitemid");
                String workItemInstance = sdiwii.getValue(0, "workiteminstance");
                getSDIWI.setString(1, sdcid);
                getSDIWI.setString(2, keyid1);
                getSDIWI.setString(3, keyid2);
                getSDIWI.setString(4, keyid3);
                getSDIWI.setString(5, workItemId);
                getSDIWI.setString(6, workItemInstance);
                DataSet dsSDIWorkItem = new DataSet(getSDIWI.executeQuery());
                sdiwi.copyRow(dsSDIWorkItem, -1, 1);
            }
            if (sdiwi.getRowCount() > 0) {
                DataSet sdiattributes = new DataSet();
                PreparedStatement getworkitemAttributes = database.prepareStatement("getworkitemattributes", "SELECT * FROM sdiattribute WHERE sdcid = ? and keyid1 = ? and keyid2 = ? and attributesdcid = ?");
                PreparedStatement getworkitemReagents = database.prepareStatement("getworkitemreagents", "SELECT * FROM workitemreagenttype WHERE workitemid = ? AND workitemversionid = ? AND workitemitemid = ? ");
                PreparedStatement getworkitemInstruments = database.prepareStatement("getworkiteminstruments", "SELECT * FROM workiteminstrument WHERE workitemid = ? AND workitemversionid = ? AND workitemitemid = ? ");
                DataSet allWorkItemAttributes = new DataSet();
                DataSet allWorkItemReagents = new DataSet();
                DataSet allWorkItemInstruments = new DataSet();
                for (int i = 0; i < sdiwi.getRowCount(); ++i) {
                    String workitemId = sdiwi.getValue(i, "workitemid");
                    String workitemVersionId = sdiwi.getValue(i, "workitemversionid");
                    getworkitemAttributes.setString(1, "WorkItem");
                    getworkitemAttributes.setString(2, workitemId);
                    getworkitemAttributes.setString(3, workitemVersionId);
                    getworkitemAttributes.setString(4, "DataSet");
                    DataSet workitemattributes = new DataSet(getworkitemAttributes.executeQuery());
                    allWorkItemAttributes.copyRow(workitemattributes, -1, 1);
                    getworkitemReagents.setString(1, workitemId);
                    getworkitemReagents.setString(2, workitemVersionId);
                    getworkitemReagents.setString(3, workitemitemid);
                    DataSet workitemReagents = new DataSet(getworkitemReagents.executeQuery());
                    allWorkItemReagents.copyRow(workitemReagents, -1, 1);
                    getworkitemInstruments.setString(1, workitemId);
                    getworkitemInstruments.setString(2, workitemVersionId);
                    getworkitemInstruments.setString(3, workitemitemid);
                    DataSet workitemInstruments = new DataSet(getworkitemInstruments.executeQuery());
                    allWorkItemInstruments.copyRow(workitemInstruments, -1, 1);
                }
                if (allWorkItemInstruments.getRowCount() > 0 || allWorkItemReagents.getRowCount() > 0) {
                    WorkItemUtil.addWorkItemDataSetRelations(sdiwiwi, sdiwi, database, ap, allWorkItemReagents, allWorkItemInstruments);
                }
                if (allWorkItemAttributes.getRowCount() > 0) {
                    BaseSDIAttributeAction.copyDownWorkItemAttributesToDataSet(sdiattributes, sdiwi, sdiwiwi, allWorkItemAttributes, connectionInfo, database, sdcProcessor, logger);
                    if (sdiattributes.getRowCount() > 0) {
                        logger.info("Processing the sdiattributes inserts: \n" + sdiattributes);
                        DataSetUtil.insert(database, sdiattributes, "sdiattribute");
                    }
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionInfo.getConnectionId())));
        }
        finally {
            database.closeStatement("getsdiwi");
        }
    }

    /*
     * Unable to fully structure code
     */
    public static void addWorkItemDataSetRelations(DataSet sdiwiwi, DataSet sdiwi, DBAccess database, ActionProcessor ap, DataSet allWorkItemReagent, DataSet allWorkItemInstrument) throws SapphireException {
        dsAddSDIDataRelation = new DataSet();
        dsAddSDIWorkItemRelation = new DataSet();
        filter = new HashMap<String, Object>();
        try {
            for (i = 0; i < sdiwi.getRowCount(); ++i) {
                sdcid = sdiwi.getValue(i, "sdcid");
                keyid1 = sdiwi.getValue(i, "keyid1");
                keyid2 = sdiwi.getValue(i, "keyid2");
                keyid3 = sdiwi.getValue(i, "keyid3");
                workItemId = sdiwi.getValue(i, "workitemid");
                workItemInstance = sdiwi.getValue(i, "workiteminstance");
                workItemVersionId = sdiwi.getValue(i, "workitemversionid");
                filter.clear();
                filter.put("sdcid", sdcid);
                filter.put("keyid1", keyid1);
                filter.put("keyid2", keyid2);
                filter.put("keyid3", keyid3);
                filter.put("workitemid", workItemId);
                filter.put("workiteminstance", new BigDecimal(workItemInstance));
                filter.put("itemsdcid", "ParamList");
                plSDIWII = sdiwiwi.getFilteredDataSet(filter);
                filter.clear();
                filter.put("workitemid", workItemId);
                filter.put("workitemversionid", workItemVersionId);
                dsReagent = allWorkItemReagent.getFilteredDataSet(filter);
                dsInstrument = allWorkItemInstrument.getFilteredDataSet(filter);
                for (r = 0; r < dsReagent.getRowCount(); ++r) {
                    linkedWorkitemitemid = dsReagent.getValue(r, "workitemitemid");
                    reagentType = dsReagent.getValue(r, "reagenttypeid");
                    reagentTypeVersion = dsReagent.getValue(r, "reagenttypeversionid");
                    mandatoryFlag = dsReagent.getValue(r, "mandatoryflag");
                    amount = dsReagent.getValue(r, "amount");
                    amountUnits = dsReagent.getValue(r, "amountunits");
                    amountUnitsType = dsReagent.getValue(r, "amountunitstype");
                    if (linkedWorkitemitemid.length() > 0) {
                        for (p = 0; p < plSDIWII.getRowCount(); ++p) {
                            if (plSDIWII.getValue(p, "iteminstance").length() == 0 || !linkedWorkitemitemid.equals((plWorkItemItemId = plSDIWII.getValue(p, "workitemitemid")).substring(0, plWorkItemItemId.indexOf(".")))) continue;
                            newRow = dsAddSDIDataRelation.addRow();
                            dsAddSDIDataRelation.setString(newRow, "sdcid", sdcid);
                            dsAddSDIDataRelation.setString(newRow, "keyid1", keyid1);
                            dsAddSDIDataRelation.setString(newRow, "keyid2", keyid2);
                            dsAddSDIDataRelation.setString(newRow, "keyid3", keyid3);
                            dsAddSDIDataRelation.setString(newRow, "paramlistid", plSDIWII.getValue(p, "itemkeyid1"));
                            dsAddSDIDataRelation.setString(newRow, "paramlistversionid", plSDIWII.getValue(p, "itemkeyid2"));
                            dsAddSDIDataRelation.setString(newRow, "variantid", plSDIWII.getValue(p, "itemkeyid3"));
                            dsAddSDIDataRelation.setString(newRow, "dataset", plSDIWII.getValue(p, "iteminstance"));
                            dsAddSDIDataRelation.setString(newRow, "sourcesdcid", "LV_ReagentType");
                            dsAddSDIDataRelation.setString(newRow, "sourcekeyid1", reagentType);
                            dsAddSDIDataRelation.setString(newRow, "sourcekeyid2", reagentTypeVersion);
                            dsAddSDIDataRelation.setString(newRow, "relationfunction", "Reagent");
                            dsAddSDIDataRelation.setString(newRow, "relationtype", reagentType);
                            dsAddSDIDataRelation.setString(newRow, "mandatoryflag", mandatoryFlag);
                            dsAddSDIDataRelation.setString(newRow, "requiredamount", amount);
                            dsAddSDIDataRelation.setString(newRow, "requiredamountunits", amountUnits);
                            dsAddSDIDataRelation.setString(newRow, "requiredamountunitstype", amountUnitsType);
                        }
                        continue;
                    }
                    newRow = dsAddSDIWorkItemRelation.addRow();
                    dsAddSDIWorkItemRelation.setString(newRow, "sdcid", sdcid);
                    dsAddSDIWorkItemRelation.setString(newRow, "keyid1", keyid1);
                    dsAddSDIWorkItemRelation.setString(newRow, "keyid2", keyid2);
                    dsAddSDIWorkItemRelation.setString(newRow, "keyid3", keyid3);
                    dsAddSDIWorkItemRelation.setString(newRow, "workitemid", workItemId);
                    dsAddSDIWorkItemRelation.setString(newRow, "workiteminstance", workItemInstance);
                    dsAddSDIWorkItemRelation.setString(newRow, "sourcesdcid", "LV_ReagentType");
                    dsAddSDIWorkItemRelation.setString(newRow, "sourcekeyid1", reagentType);
                    dsAddSDIWorkItemRelation.setString(newRow, "sourcekeyid2", reagentTypeVersion);
                    dsAddSDIWorkItemRelation.setString(newRow, "relationfunction", "Reagent");
                    dsAddSDIWorkItemRelation.setString(newRow, "relationtype", reagentType);
                    dsAddSDIWorkItemRelation.setString(newRow, "mandatoryflag", mandatoryFlag);
                    dsAddSDIWorkItemRelation.setString(newRow, "requiredamount", amount);
                    dsAddSDIWorkItemRelation.setString(newRow, "requiredamountunits", amountUnits);
                    dsAddSDIWorkItemRelation.setString(newRow, "requiredamountunitstype", amountUnitsType);
                }
                if (dsInstrument.getRowCount() <= 0) continue;
                getInstrumentModel = database.prepareStatement("getInstrumentModelunmanagedflag", "select unmanagedflag from instrumentmodel where instrumentmodelid = ? and instrumenttypeid = ?");
                getInstrumentType = database.prepareStatement("getInstrumentTypeunmanagedflag", "select unmanagedflag from instrumenttype where instrumenttypeid = ?");
lbl119:
                // 3 sources

                try {
                    for (r = 0; r < dsInstrument.getRowCount(); ++r) {
                        block33: {
                            linkedWorkitemitemid = dsInstrument.getValue(r, "workitemitemid");
                            instrumentType = dsInstrument.getValue(r, "instrumenttypeid");
                            instrumentModel = dsInstrument.getValue(r, "instrumentmodelid");
                            instrumentId = dsInstrument.getValue(r, "instrumentid");
                            mandatoryFlag = dsInstrument.getValue(r, "mandatoryflag");
                            instrCnt = dsInstrument.getInt(r, "instrumentcount", 1);
                            sourceSDCId = "";
                            sourceKeyid1 = "";
                            sourceKeyid2 = "";
                            toSDCId = "";
                            toKeyid1 = "";
                            managed = true;
                            if (instrumentId.length() > 0) {
                                sourceSDCId = "Instrument";
                                sourceKeyid1 = instrumentId;
                                toSDCId = "Instrument";
                                toKeyid1 = instrumentId;
                            } else if (instrumentModel.length() > 0) {
                                sourceSDCId = "LV_InstrumentModel";
                                sourceKeyid1 = instrumentModel;
                                sourceKeyid2 = instrumentType;
                                getInstrumentModel.setString(1, sourceKeyid1);
                                getInstrumentModel.setString(2, sourceKeyid2);
                                dsUnmanagedFlag = new DataSet(getInstrumentModel.executeQuery());
                                managed = "N".equalsIgnoreCase(dsUnmanagedFlag.getValue(0, "unmanagedflag", "N"));
                            } else {
                                sourceSDCId = "LV_InstrumentType";
                                sourceKeyid1 = instrumentType;
                                getInstrumentType.setString(1, sourceKeyid1);
                                dsUnmanagedFlag = new DataSet(getInstrumentType.executeQuery());
                                managed = "N".equalsIgnoreCase(dsUnmanagedFlag.getValue(0, "unmanagedflag", "N"));
                            }
                            v0 = rowNum = managed != false ? instrCnt : 1;
                            if (linkedWorkitemitemid.length() <= 0) break block33;
                            for (p = 0; p < plSDIWII.getRowCount(); ++p) {
                                if (plSDIWII.getValue(p, "iteminstance").length() == 0 || !linkedWorkitemitemid.equals((plWorkItemItemId = plSDIWII.getValue(p, "workitemitemid")).substring(0, plWorkItemItemId.indexOf(".")))) continue;
                                for (c = 0; c < rowNum; ++c) {
                                    newRow = dsAddSDIDataRelation.addRow();
                                    dsAddSDIDataRelation.setString(newRow, "sdcid", sdcid);
                                    dsAddSDIDataRelation.setString(newRow, "keyid1", keyid1);
                                    dsAddSDIDataRelation.setString(newRow, "keyid2", keyid2);
                                    dsAddSDIDataRelation.setString(newRow, "keyid3", keyid3);
                                    dsAddSDIDataRelation.setString(newRow, "paramlistid", plSDIWII.getValue(p, "itemkeyid1"));
                                    dsAddSDIDataRelation.setString(newRow, "paramlistversionid", plSDIWII.getValue(p, "itemkeyid2"));
                                    dsAddSDIDataRelation.setString(newRow, "variantid", plSDIWII.getValue(p, "itemkeyid3"));
                                    dsAddSDIDataRelation.setString(newRow, "dataset", plSDIWII.getValue(p, "iteminstance"));
                                    dsAddSDIDataRelation.setString(newRow, "sourcesdcid", sourceSDCId);
                                    dsAddSDIDataRelation.setString(newRow, "sourcekeyid1", sourceKeyid1);
                                    if (sourceKeyid2.length() > 0) {
                                        dsAddSDIDataRelation.setString(newRow, "sourcekeyid2", sourceKeyid2);
                                    }
                                    if (toSDCId.length() > 0) {
                                        dsAddSDIDataRelation.setString(newRow, "tosdcid", toSDCId);
                                        dsAddSDIDataRelation.setString(newRow, "tokeyid1", toKeyid1);
                                    }
                                    dsAddSDIDataRelation.setString(newRow, "relationfunction", "Instrument");
                                    dsAddSDIDataRelation.setString(newRow, "relationtype", instrumentType);
                                    dsAddSDIDataRelation.setString(newRow, "mandatoryflag", mandatoryFlag);
                                    dsAddSDIDataRelation.setString(newRow, "requiredamount", "" + instrCnt);
                                    dsAddSDIDataRelation.setString(newRow, "relationinstance", "" + (c + 1));
                                }
                            }
                            ** GOTO lbl119
                        }
                        for (c = 0; c < rowNum; ++c) {
                            newRow = dsAddSDIWorkItemRelation.addRow();
                            dsAddSDIWorkItemRelation.setString(newRow, "sdcid", sdcid);
                            dsAddSDIWorkItemRelation.setString(newRow, "keyid1", keyid1);
                            dsAddSDIWorkItemRelation.setString(newRow, "keyid2", keyid2);
                            dsAddSDIWorkItemRelation.setString(newRow, "keyid3", keyid3);
                            dsAddSDIWorkItemRelation.setString(newRow, "workitemid", workItemId);
                            dsAddSDIWorkItemRelation.setString(newRow, "workiteminstance", workItemInstance);
                            dsAddSDIWorkItemRelation.setString(newRow, "sourcesdcid", sourceSDCId);
                            dsAddSDIWorkItemRelation.setString(newRow, "sourcekeyid1", sourceKeyid1);
                            if (sourceKeyid2.length() > 0) {
                                dsAddSDIWorkItemRelation.setString(newRow, "sourcekeyid2", sourceKeyid2);
                            }
                            if (toSDCId.length() > 0) {
                                dsAddSDIWorkItemRelation.setString(newRow, "tosdcid", toSDCId);
                                dsAddSDIWorkItemRelation.setString(newRow, "tokeyid1", toKeyid1);
                            }
                            dsAddSDIWorkItemRelation.setString(newRow, "relationfunction", "Instrument");
                            dsAddSDIWorkItemRelation.setString(newRow, "relationtype", instrumentType);
                            dsAddSDIWorkItemRelation.setString(newRow, "mandatoryflag", mandatoryFlag);
                            dsAddSDIWorkItemRelation.setString(newRow, "requiredamount", "" + instrCnt);
                            dsAddSDIWorkItemRelation.setString(newRow, "relationinstance", "" + (c + 1));
                        }
                        ** GOTO lbl119
                    }
                    continue;
                }
                catch (SQLException se) {
                    throw new SapphireException(se);
                }
                finally {
                    database.closeStatement("getInstrumentModelunmanagedflag");
                    database.closeStatement("getInstrumentTypeunmanagedflag");
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
        finally {
            database.closeResultSet("getreagents");
            database.closeResultSet("getinstruments");
        }
        if (dsAddSDIDataRelation.getRowCount() > 0) {
            props = new sapphire.xml.PropertyList();
            props.setProperty("sdcid", dsAddSDIDataRelation.getValue(0, "sdcid"));
            props.setProperty("keyid1", dsAddSDIDataRelation.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", dsAddSDIDataRelation.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", dsAddSDIDataRelation.getColumnValues("keyid3", ";"));
            props.setProperty("paramlistid", dsAddSDIDataRelation.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", dsAddSDIDataRelation.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", dsAddSDIDataRelation.getColumnValues("variantid", ";"));
            props.setProperty("dataset", dsAddSDIDataRelation.getColumnValues("dataset", ";"));
            props.setProperty("relationfunction", dsAddSDIDataRelation.getColumnValues("relationfunction", ";"));
            props.setProperty("relationtype", dsAddSDIDataRelation.getColumnValues("relationtype", ";"));
            props.setProperty("sourcesdcid", dsAddSDIDataRelation.getColumnValues("sourcesdcid", ";"));
            props.setProperty("sourcekeyid1", dsAddSDIDataRelation.getColumnValues("sourcekeyid1", ";"));
            if (dsAddSDIDataRelation.isValidColumn("sourcekeyid2")) {
                props.setProperty("sourcekeyid2", dsAddSDIDataRelation.getColumnValues("sourcekeyid2", ";"));
            }
            if (dsAddSDIDataRelation.isValidColumn("tokeyid1")) {
                props.setProperty("tosdcid", dsAddSDIDataRelation.getColumnValues("tosdcid", ";"));
                props.setProperty("tokeyid1", dsAddSDIDataRelation.getColumnValues("tokeyid1", ";"));
            }
            props.setProperty("mandatoryflag", dsAddSDIDataRelation.getColumnValues("mandatoryflag", ";"));
            props.setProperty("requiredamount", dsAddSDIDataRelation.getColumnValues("requiredamount", ";"));
            props.setProperty("requiredamountunits", dsAddSDIDataRelation.getColumnValues("requiredamountunits", ";"));
            props.setProperty("requiredamountunitstype", dsAddSDIDataRelation.getColumnValues("requiredamountunitstype", ";"));
            props.setProperty("relationinstance", dsAddSDIDataRelation.getColumnValues("relationinstance", ";"));
            ap.processAction("AddSDIDataRelation", "1", props);
        }
        if (dsAddSDIWorkItemRelation.getRowCount() > 0) {
            props = new sapphire.xml.PropertyList();
            props.setProperty("sdcid", dsAddSDIWorkItemRelation.getValue(0, "sdcid"));
            props.setProperty("keyid1", dsAddSDIWorkItemRelation.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", dsAddSDIWorkItemRelation.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", dsAddSDIWorkItemRelation.getColumnValues("keyid3", ";"));
            props.setProperty("workitemid", dsAddSDIWorkItemRelation.getColumnValues("workitemid", ";"));
            props.setProperty("workiteminstance", dsAddSDIWorkItemRelation.getColumnValues("workiteminstance", ";"));
            props.setProperty("relationfunction", dsAddSDIWorkItemRelation.getColumnValues("relationfunction", ";"));
            props.setProperty("relationtype", dsAddSDIWorkItemRelation.getColumnValues("relationtype", ";"));
            props.setProperty("sourcesdcid", dsAddSDIWorkItemRelation.getColumnValues("sourcesdcid", ";"));
            props.setProperty("sourcekeyid1", dsAddSDIWorkItemRelation.getColumnValues("sourcekeyid1", ";"));
            if (dsAddSDIWorkItemRelation.isValidColumn("sourcekeyid2")) {
                props.setProperty("sourcekeyid2", dsAddSDIWorkItemRelation.getColumnValues("sourcekeyid2", ";"));
            }
            if (dsAddSDIWorkItemRelation.isValidColumn("tokeyid1")) {
                props.setProperty("tosdcid", dsAddSDIWorkItemRelation.getColumnValues("tosdcid", ";"));
                props.setProperty("tokeyid1", dsAddSDIWorkItemRelation.getColumnValues("tokeyid1", ";"));
            }
            props.setProperty("mandatoryflag", dsAddSDIWorkItemRelation.getColumnValues("mandatoryflag", ";"));
            props.setProperty("requiredamount", dsAddSDIWorkItemRelation.getColumnValues("requiredamount", ";"));
            props.setProperty("requiredamountunits", dsAddSDIWorkItemRelation.getColumnValues("requiredamountunits", ";"));
            props.setProperty("requiredamountunitstype", dsAddSDIWorkItemRelation.getColumnValues("requiredamountunitstype", ";"));
            props.setProperty("relationinstance", dsAddSDIWorkItemRelation.getColumnValues("relationinstance", ";"));
            ap.processAction("AddSDIWorkItemRelation", "1", props);
        }
    }

    public static void updateSDIDataWapColumns(DataSet sdiwi, DBAccess database, ActionProcessor ap, ConnectionInfo connectionInfo) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sdiwi.sort("workitemid,workitemversionid");
        ArrayList<DataSet> wiGroups = sdiwi.getGroupedDataSets("workitemid,workitemversionid");
        sql.append("select distinct ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset, swii.workitemitemid, ds.wapstatus from sdidata ds, sdiworkitemitem swii ").append(" where ds.sdcid = ? and ds.keyid1 = ? and ds.keyid2 = ? and ds.keyid3 = ? and ds.sourceworkitemid = ? and ds.sourceworkiteminstance = ? ");
        sql.append(" and swii.sdcid = ds.sdcid and  swii.keyid1 = ds.keyid1 and  swii.keyid2 = ds.keyid2 and  swii.keyid3 = ds.keyid3 and swii.itemsdcid = 'ParamList' and swii.itemkeyid1 = ds.paramlistid ").append(" and  swii.itemkeyid2 = ds.paramlistversionid and swii.itemkeyid3 = ds.variantid and swii.iteminstance = ds.dataset and swii.workitemid = ds.sourceworkitemid and  swii.workiteminstance = ds.sourceworkiteminstance");
        PreparedStatement selectSDIData = database.prepareStatement("selectsdidata", sql.toString());
        DataSet editDataset = new DataSet();
        HashMap<String, DataSet> sampleMap = new HashMap<String, DataSet>();
        SapphireConnection sapphireConnection = new SapphireConnection(database.getConnection(), connectionInfo);
        try {
            for (int g = 0; g < wiGroups.size(); ++g) {
                DataSet ds = wiGroups.get(g);
                String workitemid = ds.getValue(0, "workitemid");
                String workItemVersionId = ds.getValue(0, "workitemversionid", ds.getValue(0, "_currentversionid"));
                DataSet testMethodDef = WorkItemUtil.getTestMethodDefFromCache(database, connectionInfo, workitemid, workItemVersionId);
                DataSet workitemsdef = WorkItemUtil.getDefFromCache(database, connectionInfo, workitemid, workItemVersionId);
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("sdcid", "ParamList");
                DataSet dsParamList = workitemsdef.getFilteredDataSet(findMap);
                WorkItemUtil.resolveWorkItemItemCurrentVersion(dsParamList, sapphireConnection, database);
                if (dsParamList.getRowCount() <= 0) continue;
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    String sdcId = ds.getValue(i, "sdcid");
                    String keyid1 = ds.getValue(i, "keyid1");
                    String keyid2 = ds.getValue(i, "keyid2");
                    String keyid3 = ds.getValue(i, "keyid3");
                    String workiteminstance = ds.getValue(i, "workiteminstance");
                    String sdiworkitem_testlab = ds.getValue(i, COLUMN_TESTINGDEPARTMENTID);
                    String sdiworkitem_workarea = ds.getValue(i, COLUMN_WORKAREADEPARTMENTID);
                    String sdiworkitem_assignedanalyst = ds.getValue(i, COLUMN_ASSIGNEDANALYST);
                    String sdiworkitem_assigneddepartment = ds.getValue(i, COLUMN_ASSIGNEDDEPT);
                    String sdiworkitem_wapStatus = ds.getValue(i, COLUMN_WAPSTATUS);
                    selectSDIData.setString(1, sdcId);
                    selectSDIData.setString(2, keyid1);
                    selectSDIData.setString(3, keyid2);
                    selectSDIData.setString(4, keyid3);
                    selectSDIData.setString(5, workitemid);
                    selectSDIData.setString(6, workiteminstance);
                    DataSet dsSDIData = new DataSet(selectSDIData.executeQuery());
                    for (int d = 0; d < dsSDIData.getRowCount(); ++d) {
                        int findRow;
                        String sdidataWorkArea = "";
                        String sdidataTestLab = "";
                        String sdidataAssigneddept = "";
                        String sdidataAssignedAnalyst = "";
                        String paramListId = dsSDIData.getValue(d, "paramlistid");
                        String paramListVersionId = dsSDIData.getValue(d, "paramlistversionid");
                        String variantId = dsSDIData.getValue(d, "variantid");
                        String dataset = dsSDIData.getValue(d, "dataset");
                        String workitemitemId = dsSDIData.getValue(d, "workitemitemid");
                        String sdidatawapStatus = dsSDIData.getValue(d, COLUMN_WAPSTATUS);
                        HashMap<String, String> findPL = new HashMap<String, String>();
                        findPL.put("keyid1", paramListId);
                        findPL.put("keyid2", paramListVersionId);
                        findPL.put("keyid3", variantId);
                        if (workitemitemId.length() > 0) {
                            if (workitemitemId.indexOf(".") > -1) {
                                String actualWorkitemitemId = workitemitemId.substring(0, workitemitemId.indexOf("."));
                                findPL.put("workitemitemid", actualWorkitemitemId);
                            } else {
                                findPL.put("workitemitemid", workitemitemId);
                            }
                        }
                        if ((findRow = dsParamList.findRow(findPL)) <= -1) continue;
                        String workareaDept = dsParamList.getString(findRow, COLUMN_WORKAREADEPARTMENTID, testMethodDef.getValue(0, COLUMN_WORKAREADEPARTMENTID));
                        String testingDept = testMethodDef.getValue(0, COLUMN_TESTINGDEPARTMENTID);
                        boolean autoAssignWorkArea = AUTOASSIGNRULE_WORKAREA.equalsIgnoreCase(dsParamList.getValue(findRow, COLUMN_AUTOASSIGNRULE));
                        boolean autoAssignAnalyst = AUTOASSIGNRULE_ANALYST.equalsIgnoreCase(dsParamList.getValue(findRow, COLUMN_AUTOASSIGNRULE));
                        DataSet dsSample = new DataSet();
                        String sdiSiteId = "";
                        if ("Sample".equals(sdcId)) {
                            dsSample = WorkItemUtil.getSampleDataset(sampleMap, keyid1, database);
                            sdiSiteId = dsSample.getValue(0, "sitedepartmentid");
                        }
                        if (workareaDept.length() > 0) {
                            if (sdiworkitem_testlab.length() > 0) {
                                if (testingDept.equals(sdiworkitem_testlab)) {
                                    sdidataWorkArea = workareaDept;
                                    sdidataTestLab = sdiworkitem_testlab;
                                }
                            } else {
                                sdidataWorkArea = workareaDept;
                                sdidataTestLab = testingDept;
                            }
                        }
                        if (sdidataWorkArea.length() == 0 && sdiworkitem_workarea.length() > 0) {
                            sdidataWorkArea = sdiworkitem_workarea;
                            sdidataTestLab = sdiworkitem_testlab;
                        }
                        if (sdidataTestLab.length() == 0) {
                            sdidataTestLab = sdiworkitem_testlab;
                        }
                        String plAnalystId = "";
                        DataSet pldef = WorkItemUtil.getParamListDefFromCache(database, connectionInfo, paramListId, paramListVersionId, variantId);
                        String testingDepartmentId = pldef.getValue(0, COLUMN_TESTINGDEPARTMENTID);
                        String testinglabType = pldef.getValue(0, COLUMN_TESTINGLABTYPE);
                        String workareaType = pldef.getValue(0, COLUMN_WORKAREATYPE);
                        String workareaDepartmentId = pldef.getValue(0, COLUMN_WORKAREADEPARTMENTID);
                        String autoassignRule = pldef.getValue(0, COLUMN_AUTOASSIGNRULE);
                        String createActivityRule = pldef.getValue(0, COLUMN_CREATEACTIVITYRULE);
                        if (!autoAssignWorkArea && !autoAssignAnalyst) {
                            autoAssignWorkArea = AUTOASSIGNRULE_WORKAREA.equalsIgnoreCase(autoassignRule);
                            autoAssignAnalyst = AUTOASSIGNRULE_ANALYST.equalsIgnoreCase(autoassignRule);
                        }
                        if (sdidataWorkArea.length() == 0 || sdidataTestLab.length() == 0) {
                            plAnalystId = pldef.getValue(0, COLUMN_AUTOASSIGNEDANALYSTID);
                            if (sdidataTestLab.length() == 0 || sdidataTestLab.equals(testingDepartmentId)) {
                                if (workareaDepartmentId.length() > 0) {
                                    sdidataWorkArea = workareaDepartmentId;
                                    sdidataTestLab = testingDepartmentId;
                                } else if (testingDepartmentId.length() > 0) {
                                    sdidataTestLab = testingDepartmentId;
                                }
                            }
                            if (sdidataTestLab.length() == 0 && testinglabType.length() > 0 && sdiSiteId.length() > 0) {
                                database.createPreparedResultSet("gettestdept", "SELECT departmentid FROM department WHERE parentdepartmentid = ? AND testinglabtype = ?", new String[]{sdiSiteId, testinglabType});
                                DataSet testDept = new DataSet(database.getResultSet("gettestdept"));
                                if (testDept.getRowCount() > 0) {
                                    sdidataTestLab = testDept.getValue(0, "departmentid");
                                }
                            }
                            if (sdidataWorkArea.length() == 0 && workareaType.length() > 0 && sdidataTestLab.length() > 0) {
                                database.createPreparedResultSet("getworkareadept", "SELECT departmentid FROM department WHERE parentdepartmentid = ? AND workareatype = ?", new String[]{sdidataTestLab, workareaType});
                                DataSet woDept = new DataSet(database.getResultSet("getworkareadept"));
                                if (woDept.getRowCount() > 0 && woDept.getValue(0, "departmentid").length() > 0) {
                                    sdidataWorkArea = woDept.getValue(0, "departmentid");
                                }
                            }
                        }
                        if (sdiworkitem_wapStatus.length() == 0 && sdidatawapStatus.length() == 0 && ACTIVITYRULE_ONDEMAND.equalsIgnoreCase(createActivityRule)) {
                            sdidatawapStatus = "Pending";
                        }
                        if (sdidataWorkArea.length() == 0 && "Sample".equals(sdcId) && (sdidataTestLab.length() == 0 || sdidataTestLab.equals(dsSample.getValue(0, COLUMN_TESTINGDEPARTMENTID)))) {
                            sdidataTestLab = dsSample.getValue(0, COLUMN_TESTINGDEPARTMENTID);
                            sdidataWorkArea = dsSample.getValue(0, COLUMN_WORKAREADEPARTMENTID);
                        }
                        if (autoAssignWorkArea && sdidataWorkArea.length() > 0) {
                            sdidataAssigneddept = sdidataWorkArea;
                        }
                        if (autoAssignAnalyst) {
                            sdidataAssignedAnalyst = dsParamList.getString(findRow, COLUMN_AUTOASSIGNEDANALYSTID, "");
                        }
                        if (sdidataAssignedAnalyst.length() == 0) {
                            sdidataAssignedAnalyst = sdiworkitem_assignedanalyst.length() > 0 ? sdiworkitem_assignedanalyst : plAnalystId;
                        }
                        if (sdidataAssigneddept.length() == 0 && sdiworkitem_assigneddepartment.length() > 0) {
                            sdidataAssigneddept = sdiworkitem_assigneddepartment;
                        }
                        if ((sdidataAssigneddept.length() == 0 || sdidataAssignedAnalyst.length() == 0) && "Sample".equals(sdcId)) {
                            if (sdidataAssigneddept.length() == 0) {
                                sdidataAssigneddept = dsSample.getValue(0, "assigneddepartmentid");
                            }
                            if (sdidataAssignedAnalyst.length() == 0) {
                                sdidataAssignedAnalyst = dsSample.getValue(0, "assignedanalystid");
                            }
                        }
                        if (sdidataTestLab.length() <= 0 && sdidataWorkArea.length() <= 0 && sdidataAssigneddept.length() <= 0 && sdidataAssignedAnalyst.length() <= 0 && sdidatawapStatus.length() <= 0) continue;
                        int r = editDataset.addRow();
                        editDataset.setString(r, "sdcid", sdcId);
                        editDataset.setString(r, "keyid1", keyid1);
                        editDataset.setString(r, "keyid2", keyid2);
                        editDataset.setString(r, "keyid3", keyid3);
                        editDataset.setString(r, "paramlistid", paramListId);
                        editDataset.setString(r, "paramlistversionid", paramListVersionId);
                        editDataset.setString(r, "variantid", variantId);
                        editDataset.setString(r, "dataset", dataset);
                        editDataset.setString(r, COLUMN_TESTINGDEPARTMENTID, sdidataTestLab);
                        editDataset.setString(r, COLUMN_WORKAREADEPARTMENTID, sdidataWorkArea);
                        editDataset.setString(r, COLUMN_ASSIGNEDANALYST, sdidataAssignedAnalyst);
                        editDataset.setString(r, COLUMN_ASSIGNEDDEPT, sdidataAssigneddept);
                        editDataset.setString(r, COLUMN_WAPSTATUS, sdidatawapStatus);
                    }
                }
            }
            if (editDataset.getRowCount() > 0) {
                sapphire.xml.PropertyList props = new sapphire.xml.PropertyList();
                props.setProperty("sdcid", editDataset.getValue(0, "sdcid"));
                props.setProperty("keyid1", editDataset.getColumnValues("keyid1", ";"));
                props.setProperty("keyid2", editDataset.getColumnValues("keyid3", ";"));
                props.setProperty("keyid3", editDataset.getColumnValues("keyid3", ";"));
                props.setProperty("paramlistid", editDataset.getColumnValues("paramlistid", ";"));
                props.setProperty("paramlistversionid", editDataset.getColumnValues("paramlistversionid", ";"));
                props.setProperty("variantid", editDataset.getColumnValues("variantid", ";"));
                props.setProperty("dataset", editDataset.getColumnValues("dataset", ";"));
                props.setProperty(COLUMN_TESTINGDEPARTMENTID, editDataset.getColumnValues(COLUMN_TESTINGDEPARTMENTID, ";"));
                props.setProperty(COLUMN_WORKAREADEPARTMENTID, editDataset.getColumnValues(COLUMN_WORKAREADEPARTMENTID, ";"));
                props.setProperty(COLUMN_ASSIGNEDANALYST, editDataset.getColumnValues(COLUMN_ASSIGNEDANALYST, ";"));
                props.setProperty(COLUMN_ASSIGNEDDEPT, editDataset.getColumnValues(COLUMN_ASSIGNEDDEPT, ";"));
                props.setProperty(COLUMN_WAPSTATUS, editDataset.getColumnValues(COLUMN_WAPSTATUS, ";"));
                props.setProperty("propsmatch", "Y");
                ap.processAction("EditDataSet", "1", props);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed updating wap columns in sdidata table." + e.getMessage());
        }
        finally {
            database.closeStatement("selectsdidata");
            database.closeStatement("getfromsample");
        }
    }

    public static void updateSDIDataWapColumns(DataSet sdiwi, DataSet dsSDIData, DBAccess database, ActionProcessor ap, ConnectionInfo connectionInfo) throws SapphireException {
        HashMap<String, DataSet> sampleMap = new HashMap<String, DataSet>();
        SapphireConnection sapphireConnection = new SapphireConnection(database.getConnection(), connectionInfo);
        try {
            String workitemid = sdiwi.getValue(0, "workitemid");
            String workItemVersionId = sdiwi.getValue(0, "workitemversionid", sdiwi.getValue(0, "_currentversionid"));
            DataSet testMethodDef = WorkItemUtil.getTestMethodDefFromCache(database, connectionInfo, workitemid, workItemVersionId);
            DataSet workitemsdef = WorkItemUtil.getDefFromCache(database, connectionInfo, workitemid, workItemVersionId);
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("sdcid", "ParamList");
            DataSet dsParamList = workitemsdef.getFilteredDataSet(findMap);
            WorkItemUtil.resolveWorkItemItemCurrentVersion(dsParamList, sapphireConnection, database);
            if (dsParamList.getRowCount() > 0 && OpalUtil.isNotEmpty(sdiwi)) {
                for (int d = 0; d < dsSDIData.getRowCount(); ++d) {
                    int findRow;
                    String sdcId = sdiwi.getValue(0, "sdcid");
                    String keyid1 = sdiwi.getValue(0, "keyid1");
                    String sdiworkitem_testlab = sdiwi.getValue(0, COLUMN_TESTINGDEPARTMENTID);
                    String sdiworkitem_workarea = sdiwi.getValue(0, COLUMN_WORKAREADEPARTMENTID);
                    String sdiworkitem_assignedanalyst = sdiwi.getValue(0, COLUMN_ASSIGNEDANALYST);
                    String sdiworkitem_assigneddepartment = sdiwi.getValue(0, COLUMN_ASSIGNEDDEPT);
                    String sdiworkitem_wapStatus = sdiwi.getValue(0, COLUMN_WAPSTATUS);
                    String sdidataWorkArea = "";
                    String sdidataTestLab = "";
                    String sdidataAssigneddept = "";
                    String sdidataAssignedAnalyst = "";
                    String paramListId = dsSDIData.getValue(d, "paramlistid");
                    String paramListVersionId = dsSDIData.getValue(d, "paramlistversionid");
                    String variantId = dsSDIData.getValue(d, "variantid");
                    String workitemitemId = dsSDIData.getValue(d, "workitemitemid");
                    String sdidatawapStatus = dsSDIData.getValue(d, COLUMN_WAPSTATUS);
                    HashMap<String, String> findPL = new HashMap<String, String>();
                    findPL.put("keyid1", paramListId);
                    findPL.put("keyid2", paramListVersionId);
                    findPL.put("keyid3", variantId);
                    if (workitemitemId.length() > 0) {
                        if (workitemitemId.indexOf(".") > -1) {
                            String actualWorkitemitemId = workitemitemId.substring(0, workitemitemId.indexOf("."));
                            findPL.put("workitemitemid", actualWorkitemitemId);
                        } else {
                            findPL.put("workitemitemid", workitemitemId);
                        }
                    }
                    if ((findRow = dsParamList.findRow(findPL)) <= -1) continue;
                    String workareaDept = dsParamList.getString(findRow, COLUMN_WORKAREADEPARTMENTID, testMethodDef.getValue(0, COLUMN_WORKAREADEPARTMENTID));
                    String testingDept = testMethodDef.getValue(0, COLUMN_TESTINGDEPARTMENTID);
                    boolean autoAssignWorkArea = AUTOASSIGNRULE_WORKAREA.equalsIgnoreCase(dsParamList.getValue(findRow, COLUMN_AUTOASSIGNRULE));
                    boolean autoAssignAnalyst = AUTOASSIGNRULE_ANALYST.equalsIgnoreCase(dsParamList.getValue(findRow, COLUMN_AUTOASSIGNRULE));
                    DataSet dsSample = new DataSet();
                    String sdiSiteId = "";
                    if ("Sample".equals(sdcId)) {
                        dsSample = WorkItemUtil.getSampleDataset(sampleMap, keyid1, database);
                        sdiSiteId = dsSample.getValue(0, "sitedepartmentid");
                    }
                    if (workareaDept.length() > 0) {
                        if (sdiworkitem_testlab.length() > 0) {
                            if (testingDept.equals(sdiworkitem_testlab)) {
                                sdidataWorkArea = workareaDept;
                                sdidataTestLab = sdiworkitem_testlab;
                            }
                        } else {
                            sdidataWorkArea = workareaDept;
                            sdidataTestLab = testingDept;
                        }
                    }
                    if (sdidataWorkArea.length() == 0 && sdiworkitem_workarea.length() > 0) {
                        sdidataWorkArea = sdiworkitem_workarea;
                        sdidataTestLab = sdiworkitem_testlab;
                    }
                    if (sdidataTestLab.length() == 0) {
                        sdidataTestLab = sdiworkitem_testlab;
                    }
                    String plAnalystId = "";
                    DataSet pldef = WorkItemUtil.getParamListDefFromCache(database, connectionInfo, paramListId, paramListVersionId, variantId);
                    String testingDepartmentId = pldef.getValue(0, COLUMN_TESTINGDEPARTMENTID);
                    String testinglabType = pldef.getValue(0, COLUMN_TESTINGLABTYPE);
                    String workareaType = pldef.getValue(0, COLUMN_WORKAREATYPE);
                    String workareaDepartmentId = pldef.getValue(0, COLUMN_WORKAREADEPARTMENTID);
                    String autoassignRule = pldef.getValue(0, COLUMN_AUTOASSIGNRULE);
                    String createActivityRule = pldef.getValue(0, COLUMN_CREATEACTIVITYRULE);
                    if (!autoAssignWorkArea && !autoAssignAnalyst) {
                        autoAssignWorkArea = AUTOASSIGNRULE_WORKAREA.equalsIgnoreCase(autoassignRule);
                        autoAssignAnalyst = AUTOASSIGNRULE_ANALYST.equalsIgnoreCase(autoassignRule);
                    }
                    if (sdidataWorkArea.length() == 0 || sdidataTestLab.length() == 0) {
                        plAnalystId = pldef.getValue(0, COLUMN_AUTOASSIGNEDANALYSTID);
                        if (sdidataTestLab.length() == 0 || sdidataTestLab.equals(testingDepartmentId)) {
                            if (workareaDepartmentId.length() > 0) {
                                sdidataWorkArea = workareaDepartmentId;
                                sdidataTestLab = testingDepartmentId;
                            } else if (testingDepartmentId.length() > 0) {
                                sdidataTestLab = testingDepartmentId;
                            }
                        }
                        if (sdidataTestLab.length() == 0 && testinglabType.length() > 0 && sdiSiteId.length() > 0) {
                            database.createPreparedResultSet("gettestdept", "SELECT departmentid FROM department WHERE parentdepartmentid = ? AND testinglabtype = ?", new String[]{sdiSiteId, testinglabType});
                            DataSet testDept = new DataSet(database.getResultSet("gettestdept"));
                            if (testDept.getRowCount() > 0) {
                                sdidataTestLab = testDept.getValue(0, "departmentid");
                            }
                        }
                        if (sdidataWorkArea.length() == 0 && workareaType.length() > 0 && sdidataTestLab.length() > 0) {
                            database.createPreparedResultSet("getworkareadept", "SELECT departmentid FROM department WHERE parentdepartmentid = ? AND workareatype = ?", new String[]{sdidataTestLab, workareaType});
                            DataSet woDept = new DataSet(database.getResultSet("getworkareadept"));
                            if (woDept.getRowCount() > 0 && woDept.getValue(0, "departmentid").length() > 0) {
                                sdidataWorkArea = woDept.getValue(0, "departmentid");
                            }
                        }
                    }
                    if (sdiworkitem_wapStatus.length() == 0 && sdidatawapStatus.length() == 0 && ACTIVITYRULE_ONDEMAND.equalsIgnoreCase(createActivityRule)) {
                        sdidatawapStatus = "Pending";
                    }
                    if (sdidataWorkArea.length() == 0 && "Sample".equals(sdcId) && (sdidataTestLab.length() == 0 || sdidataTestLab.equals(dsSample.getValue(0, COLUMN_TESTINGDEPARTMENTID)))) {
                        sdidataTestLab = dsSample.getValue(0, COLUMN_TESTINGDEPARTMENTID);
                        sdidataWorkArea = dsSample.getValue(0, COLUMN_WORKAREADEPARTMENTID);
                    }
                    if (autoAssignWorkArea && sdidataWorkArea.length() > 0) {
                        sdidataAssigneddept = sdidataWorkArea;
                    }
                    if (autoAssignAnalyst) {
                        sdidataAssignedAnalyst = dsParamList.getString(findRow, COLUMN_AUTOASSIGNEDANALYSTID, "");
                    }
                    if (sdidataAssignedAnalyst.length() == 0) {
                        sdidataAssignedAnalyst = sdiworkitem_assignedanalyst.length() > 0 ? sdiworkitem_assignedanalyst : plAnalystId;
                    }
                    if (sdidataAssigneddept.length() == 0 && sdiworkitem_assigneddepartment.length() > 0) {
                        sdidataAssigneddept = sdiworkitem_assigneddepartment;
                    }
                    if ((sdidataAssigneddept.length() == 0 || sdidataAssignedAnalyst.length() == 0) && "Sample".equals(sdcId)) {
                        if (sdidataAssigneddept.length() == 0) {
                            sdidataAssigneddept = dsSample.getValue(0, "assigneddepartmentid");
                        }
                        if (sdidataAssignedAnalyst.length() == 0) {
                            sdidataAssignedAnalyst = dsSample.getValue(0, "assignedanalystid");
                        }
                    }
                    if (sdidataTestLab.length() <= 0 && sdidataWorkArea.length() <= 0 && sdidataAssigneddept.length() <= 0 && sdidataAssignedAnalyst.length() <= 0 && sdidatawapStatus.length() <= 0) continue;
                    dsSDIData.setString(d, COLUMN_TESTINGDEPARTMENTID, sdidataTestLab);
                    dsSDIData.setString(d, COLUMN_WORKAREADEPARTMENTID, sdidataWorkArea);
                    dsSDIData.setString(d, COLUMN_ASSIGNEDANALYST, sdidataAssignedAnalyst);
                    dsSDIData.setString(d, COLUMN_ASSIGNEDDEPT, sdidataAssigneddept);
                    dsSDIData.setString(d, COLUMN_WAPSTATUS, sdidatawapStatus);
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed updating wap columns in sdidata table." + e.getMessage());
        }
        finally {
            database.closeStatement("getfromsample");
        }
    }

    private static DataSet getSampleDataset(HashMap<String, DataSet> sampleMap, String keyid1, DBAccess database) throws SapphireException {
        DataSet dsSample = new DataSet();
        if (sampleMap.containsKey(keyid1)) {
            dsSample = sampleMap.get(keyid1);
        } else {
            database.createPreparedResultSet("getfromsample", "select * from s_sample where s_sampleid = ?", new String[]{keyid1});
            dsSample = new DataSet(database.getResultSet("getfromsample"));
            sampleMap.put(keyid1, dsSample);
        }
        return dsSample;
    }

    public static void resolveSDIWorkItemTestingDepartment(DataSet sdiwi, int newrow, sapphire.xml.PropertyList workItemSDCProps, DataSet workItemDS, String testingdepartmentid, String testinglabType, String sdiSiteId, String sdiTestingDepartmentId, String workareadepartmentid, String sdiWorkareaDepartmentId, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        DataSet departmentdef;
        DataSet departmentdef2;
        String securityDeptId;
        if (testingdepartmentid.length() > 0) {
            sdiwi.setString(newrow, COLUMN_TESTINGDEPARTMENTID, testingdepartmentid);
            if (sdiwi.getValue(newrow, COLUMN_WORKAREADEPARTMENTID).length() == 0 && workareadepartmentid.length() > 0) {
                sdiwi.setString(newrow, COLUMN_WORKAREADEPARTMENTID, workareadepartmentid);
            }
            return;
        }
        if ("D".equalsIgnoreCase(workItemSDCProps.getProperty("accesscontrolledflag", "")) && (securityDeptId = workItemDS.getString(0, "securitydepartment", "")).length() > 0 && (departmentdef2 = WorkItemUtil.getDepartmentDefFromCache(database, connectionInfo, securityDeptId)).getRowCount() > 0 && "Y".equalsIgnoreCase(departmentdef2.getValue(0, "testingflag"))) {
            sdiwi.setString(newrow, COLUMN_TESTINGDEPARTMENTID, securityDeptId);
            return;
        }
        if (testinglabType.length() > 0 && sdiSiteId.length() > 0) {
            database.createPreparedResultSet("gettestdept", "SELECT departmentid FROM department WHERE parentdepartmentid = ? AND testinglabtype = ?", new String[]{sdiSiteId, testinglabType});
            DataSet testDept = new DataSet(database.getResultSet("gettestdept"));
            if (testDept.getRowCount() > 0) {
                sdiwi.setString(newrow, COLUMN_TESTINGDEPARTMENTID, testDept.getValue(0, "departmentid"));
                return;
            }
        }
        if (sdiTestingDepartmentId.length() > 0) {
            sdiwi.setString(newrow, COLUMN_TESTINGDEPARTMENTID, sdiTestingDepartmentId);
            if (sdiwi.getValue(newrow, COLUMN_WORKAREADEPARTMENTID).length() == 0 && sdiWorkareaDepartmentId.length() > 0) {
                sdiwi.setString(newrow, COLUMN_WORKAREADEPARTMENTID, sdiWorkareaDepartmentId);
            }
            return;
        }
        database.createPreparedResultSet("getuserdefaulttestlab", "select departmentid from departmentsysuser where sysuserid = ? and defaulttestinglabflag = 'Y'", new String[]{connectionInfo.getSysuserId()});
        DataSet dsUserDefaultTestingLab = new DataSet(database.getResultSet("getuserdefaulttestlab"));
        if (dsUserDefaultTestingLab.getRowCount() > 0) {
            sdiwi.setString(newrow, COLUMN_TESTINGDEPARTMENTID, dsUserDefaultTestingLab.getValue(0, "departmentid"));
            return;
        }
        String userDefaultDepartmentId = connectionInfo.getDefaultDepartment();
        if (userDefaultDepartmentId != null && userDefaultDepartmentId.length() > 0 && (departmentdef = WorkItemUtil.getDepartmentDefFromCache(database, connectionInfo, userDefaultDepartmentId)).getRowCount() > 0 && "Y".equalsIgnoreCase(departmentdef.getValue(0, "testingflag"))) {
            sdiwi.setString(newrow, COLUMN_TESTINGDEPARTMENTID, userDefaultDepartmentId);
            return;
        }
    }
}

