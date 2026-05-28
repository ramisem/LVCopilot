/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.xml.PropertyListUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkItemItemRuleEvaluator {
    static final String LABVANTAGE_CVS_ID = "$Revision: 99796 $";
    public static final String Unit_DAY = "Days";
    public static final String Unit_MONTH = "Months";
    public static final String Unit_YEAR = "Years";
    public static final String Unit_HOUR = "Hours";
    public static final String Unit_WEEK = "Weeks";
    public static final String Unit_MINUTES = "Minutes";
    private String default_prepAvailabilityRule = "{\"includerule\":[{\"includetimepoint\":\"includealways\"}],\"reflexrule\":[{\"reflexruletype\":\"Available\",\"timepoint\":\"Initially\"}]}";
    private String default_procAvailabilityRule = "{\"includerule\":[{\"includetimepoint\":\"includealways\"}],\"reflexrule\":[{\"reflexruletype\":\"Available\",\"timepoint\":\"when\",\"item\":\"LastParameterListType\", \"paramlisttype\":\"Preparation\",datasetstatusandlimit:\"hasstatus\", \"datasetstatus\":\"Completed\"}]}";

    public void evaluateRuleOnDataItemRelease(SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, SDIData sdiData, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        DataSet datasetapprovalData = sdiData.getDataset("dataapproval");
        DataSet datasetItemData = sdiData.getDataset("dataitem");
        DataSet dsReleasedDataItems = new DataSet();
        for (int r = 0; r < datasetItemData.getRowCount(); ++r) {
            if (!"Y".equalsIgnoreCase(datasetItemData.getValue(r, "releasedflag"))) continue;
            dsReleasedDataItems.copyRow(datasetItemData, r, 1);
        }
        if (dsReleasedDataItems.getRowCount() > 0) {
            dsReleasedDataItems.sort("sdcid, keyid1, keyid2, keyid3");
            ArrayList<DataSet> sdiGroups = dsReleasedDataItems.getGroupedDataSets("sdcid, keyid1, keyid2, keyid3");
            this.evaluateRule(sdiGroups, sdcid, database, connectionInfo, sdcProcessor, beforeEditImage, dsReleasedDataItems, datasetapprovalData);
        }
    }

    public void evaluateRuleOnDataSetStatusUpdate(SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, SDIData sdiData, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        DataSet datasetapprovalData = sdiData.getDataset("dataapproval");
        DataSet datasetData = sdiData.getDataset("dataset");
        DataSet modifiedDataSetStatus = new DataSet();
        for (int r = 0; r < datasetData.getRowCount(); ++r) {
            String sourceworkitemid;
            if (!this.hasSDIDataValueChanged(sdcProcessor, sdcid, beforeEditImage, datasetData, r, "s_datasetstatus") || (sourceworkitemid = this.getOldSDIDataValue(sdcProcessor, sdcid, beforeEditImage, datasetData, r, "sourceworkitemid")) == null || sourceworkitemid.length() <= 0) continue;
            modifiedDataSetStatus.copyRow(datasetData, r, 1);
        }
        if (modifiedDataSetStatus.getRowCount() > 0) {
            modifiedDataSetStatus.sort("sdcid, keyid1, keyid2, keyid3");
            ArrayList<DataSet> sdiGroups = modifiedDataSetStatus.getGroupedDataSets("sdcid, keyid1, keyid2, keyid3");
            this.evaluateRule(sdiGroups, sdcid, database, connectionInfo, sdcProcessor, beforeEditImage, modifiedDataSetStatus, datasetapprovalData);
        }
    }

    public void evaluateRuleOnSDIWorkItemStatusUpdate(SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, SDIData sdiData, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        DataSet sdiworkitemData = sdiData.getDataset("sdiworkitem");
        PreparedStatement getSDIWIGroupId = database.prepareStatement("getworkitemgroup", "select groupid from sdiworkitem where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? and workiteminstance = ?");
        DataSet modifiedWorkItemStatus = new DataSet();
        try {
            for (int r = 0; r < sdiworkitemData.getRowCount(); ++r) {
                if (!this.hasSDIWorkItemValueChanged(sdcProcessor, sdcid, beforeEditImage, sdiworkitemData, r, "workitemstatus")) continue;
                getSDIWIGroupId.setString(1, sdcid);
                getSDIWIGroupId.setString(2, sdiworkitemData.getString(r, "keyid1"));
                getSDIWIGroupId.setString(3, sdiworkitemData.getString(r, "keyid2"));
                getSDIWIGroupId.setString(4, sdiworkitemData.getString(r, "keyid3"));
                getSDIWIGroupId.setString(5, sdiworkitemData.getString(r, "workitemid"));
                getSDIWIGroupId.setString(6, sdiworkitemData.getValue(r, "workiteminstance"));
                DataSet ds = new DataSet(getSDIWIGroupId.executeQuery());
                if (ds.getValue(0, "groupid").length() <= 0) continue;
                modifiedWorkItemStatus.copyRow(sdiworkitemData, r, 1);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            database.closeStatement("getworkitemgroup");
        }
        if (modifiedWorkItemStatus.getRowCount() > 0) {
            modifiedWorkItemStatus.sort("sdcid, keyid1, keyid2, keyid3");
            ArrayList<DataSet> sdiGroups = modifiedWorkItemStatus.getGroupedDataSets("sdcid, keyid1, keyid2, keyid3");
            this.evaluateRule(sdiGroups, sdcid, database, connectionInfo, sdcProcessor, beforeEditImage, modifiedWorkItemStatus, null);
        }
    }

    public boolean hasSDIDataValueChanged(SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, DataSet newSDIData, int row, String columnId) {
        boolean hasChanged = false;
        if (newSDIData.isValidColumn(columnId)) {
            String newValue = newSDIData.getValue(row, columnId);
            String oldValue = this.getOldSDIDataValue(sdcProcessor, sdcid, beforeEditImage, newSDIData, row, columnId);
            if (oldValue == null && newValue != null || oldValue != null && newValue == null || oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    public String getOldSDIDataValue(SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, DataSet newSDIData, int row, String columnId) {
        int oldRow;
        DataSet oldSDIWI;
        String oldValue = "";
        String keyid1 = newSDIData.getValue(row, "keyid1");
        String keyid2 = newSDIData.getValue(row, "keyid2");
        String keyid3 = newSDIData.getValue(row, "keyid3");
        String paramListId = newSDIData.getValue(row, "paramlistid");
        String paramListVersionId = newSDIData.getValue(row, "paramlistversionid");
        String variantId = newSDIData.getValue(row, "variantid");
        BigDecimal datasetNum = newSDIData.getBigDecimal(row, "dataset");
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put("keyid1", keyid1);
        if (sdcProcessor.getProperty(sdcid, "keycolid2").length() > 0) {
            findMap.put("keyid2", keyid2);
        }
        if (sdcProcessor.getProperty(sdcid, "keycolid3").length() > 0) {
            findMap.put("keyid3", keyid3);
        }
        findMap.put("paramlistid", paramListId);
        findMap.put("paramlistversionid", paramListVersionId);
        findMap.put("variantid", variantId);
        findMap.put("dataset", datasetNum);
        if (beforeEditImage != null && (oldSDIWI = beforeEditImage.getDataset("dataset")) != null && (oldRow = oldSDIWI.findRow(findMap)) >= 0 && oldRow < oldSDIWI.size()) {
            oldValue = oldSDIWI.getValue(oldRow, columnId);
        }
        return oldValue;
    }

    public boolean hasSDIWorkItemValueChanged(SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, DataSet newSDIWI, int row, String columnId) {
        boolean hasChanged = false;
        if (newSDIWI.isValidColumn(columnId)) {
            String newValue = newSDIWI.getValue(row, columnId);
            String oldValue = this.getOldSDIWorkItemValue(sdcProcessor, sdcid, beforeEditImage, newSDIWI, row, columnId);
            if (oldValue == null && newValue != null || oldValue != null && newValue == null || oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                hasChanged = true;
            }
        }
        return hasChanged;
    }

    public String getOldSDIWorkItemValue(SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, DataSet newSDIWI, int row, String columnId) {
        int oldRow;
        DataSet oldSDIWI;
        String oldValue = "";
        String keyid1 = newSDIWI.getValue(row, "keyid1");
        String keyid2 = newSDIWI.getValue(row, "keyid2");
        String keyid3 = newSDIWI.getValue(row, "keyid3");
        String workItemId = newSDIWI.getValue(row, "workitemid");
        BigDecimal workItemInstance = newSDIWI.getBigDecimal(row, "workiteminstance");
        HashMap<String, Object> findMap = new HashMap<String, Object>();
        findMap.put("keyid1", keyid1);
        if (sdcProcessor.getProperty(sdcid, "keycolid2").length() > 0) {
            findMap.put("keyid2", keyid2);
        }
        if (sdcProcessor.getProperty(sdcid, "keycolid3").length() > 0) {
            findMap.put("keyid3", keyid3);
        }
        findMap.put("workitemid", workItemId);
        findMap.put("workiteminstance", workItemInstance);
        if (beforeEditImage != null && (oldSDIWI = beforeEditImage.getDataset("sdiworkitem")) != null && (oldRow = oldSDIWI.findRow(findMap)) >= 0 && oldRow < oldSDIWI.size()) {
            oldValue = oldSDIWI.getValue(oldRow, columnId);
        }
        return oldValue;
    }

    public void evaluateRule(ArrayList sdiGroups, String sdcid, DBAccess database, ConnectionInfo connectioninfo, SDCProcessor sdcProcessor, SDIData beforeEditImage, DataSet modifiedStatus, DataSet dsapprovals) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select wi.*, wii.workitemitemid, wii.itemsdcid, wii.itemkeyid1, wii.itemkeyid2, wii.itemkeyid3, wii.iteminstance, wii.usersequence sdiwii_usersequence, wii.workitemitemrule, wii.forcenewflag, pl.s_paramlisttype ").append(" from sdiworkitem wi, sdiworkitemitem wii left outer join paramlist pl on ( pl.paramlistid = wii.itemkeyid1 and pl.paramlistversionid = wii.itemkeyid2 and pl.variantid = wii.itemkeyid3 ) ").append(" where  wii.sdcid = wi.sdcid and wii.keyid1 = wi.keyid1 and wii.keyid2 = wi.keyid2 and wii.keyid3 = wi.keyid3 ").append(" and wii.workitemid = wi.workitemid and wii.workiteminstance = wi.workiteminstance").append(" and wi.sdcid = ? and wi.keyid1 =? and wi.keyid2 = ? and wi.keyid3 = ? and ( wii.itemsdcid = 'ParamList' OR wii.itemsdcid = 'WorkItem' )").append(" order by wii.workitemid, wi.workitemversionid, wi.workiteminstance, wii.usersequence");
        PreparedStatement getAllSDIWorkItems = database.prepareStatement("getAllSDIWorkItemDS", sql.toString());
        sql.setLength(0);
        sql.append("select ds.*, pl.s_paramlisttype, swii.workitemitemid, swii.workitemid, swii.workiteminstance from sdidata ds, paramlist pl, sdiworkitemitem swii where  pl.paramlistid = ds.paramlistid and pl.paramlistversionid = ds.paramlistversionid and pl.variantid = ds.variantid  and swii.sdcid = ds.sdcid and  swii.keyid1 = ds.keyid1 and  swii.keyid2 = ds.keyid2 and  swii.keyid3 = ds.keyid3 and swii.itemsdcid = 'ParamList' and swii.itemkeyid1 = ds.paramlistid and  swii.itemkeyid2 = ds.paramlistversionid and swii.itemkeyid3 = ds.variantid and swii.iteminstance = ds.dataset and ds.sdcid = ? and ds.keyid1 =? and ds.keyid2 = ? and ds.keyid3 = ? order by ds.usersequence");
        PreparedStatement getAllSDIData = database.prepareStatement("getAllSDIData", sql.toString());
        try {
            PropertyListCollection toMakeAvailableDS = new PropertyListCollection();
            PropertyList newQCDataSetProps = new PropertyList();
            for (int g = 0; g < sdiGroups.size(); ++g) {
                DataSet dsSDI = (DataSet)sdiGroups.get(g);
                String keyid1 = dsSDI.getString(0, "keyid1");
                String keyid2 = dsSDI.getString(0, "keyid2");
                String keyid3 = dsSDI.getString(0, "keyid3");
                getAllSDIWorkItems.setString(1, sdcid);
                getAllSDIWorkItems.setString(2, keyid1);
                getAllSDIWorkItems.setString(3, keyid2);
                getAllSDIWorkItems.setString(4, keyid3);
                DataSet dsSDIWII = new DataSet(getAllSDIWorkItems.executeQuery());
                if (dsSDIWII.getRowCount() <= 0) continue;
                getAllSDIData.setString(1, sdcid);
                getAllSDIData.setString(2, keyid1);
                getAllSDIData.setString(3, keyid2);
                getAllSDIData.setString(4, keyid3);
                DataSet dsSDIData = new DataSet(getAllSDIData.executeQuery());
                dsSDIWII.sort("workitemid, workitemversionid, workiteminstance");
                ArrayList<DataSet> workItemGroups = dsSDIWII.getGroupedDataSets("workitemid, workitemversionid, workiteminstance");
                HashMap<String, Object> filter = new HashMap<String, Object>();
                for (int w = 0; w < workItemGroups.size(); ++w) {
                    DataSet dsWIGrp = workItemGroups.get(w);
                    String workitemId = dsWIGrp.getValue(0, "workitemid");
                    String workitemInstance = dsWIGrp.getValue(0, "workiteminstance");
                    filter.clear();
                    filter.put("workitemid", workitemId);
                    filter.put("workiteminstance", new BigDecimal(workitemInstance));
                    DataSet sdidataDS = dsSDIData.getFilteredDataSet(filter);
                    for (int r = 0; r < dsWIGrp.getRowCount(); ++r) {
                        String rule = dsWIGrp.getValue(r, "workitemitemrule");
                        String itemSDC = dsWIGrp.getValue(r, "itemsdcid");
                        if (rule != null && rule.equals("null")) {
                            rule = "";
                        }
                        if (rule.length() != 0 && !rule.trim().equals("{}") || !"ParamList".equals(itemSDC)) continue;
                        rule = this.getDefaultAvailabilityRule(dsWIGrp.getValue(r, "s_paramlisttype"));
                        dsWIGrp.setString(r, "workitemitemrule", rule);
                    }
                    DataSet filteredSDI = new DataSet(connectioninfo);
                    if (OpalUtil.isNotEmpty(modifiedStatus)) {
                        int row;
                        HashMap<String, Object> filterMap = new HashMap<String, Object>();
                        HashSet<Integer> rownum = new HashSet<Integer>();
                        for (int count = 0; count < dsWIGrp.getRowCount(); ++count) {
                            filterMap.put("sdcid", dsWIGrp.getValue(count, "sdcid"));
                            filterMap.put("keyid1", dsWIGrp.getValue(count, "keyid1"));
                            filterMap.put("keyid2", dsWIGrp.getValue(count, "keyid2"));
                            filterMap.put("keyid3", dsWIGrp.getValue(count, "keyid3"));
                            filterMap.put("paramlistid", dsWIGrp.getValue(count, "itemkeyid1"));
                            filterMap.put("paramlistversionid", dsWIGrp.getValue(count, "itemkeyid2"));
                            filterMap.put("variantid", dsWIGrp.getValue(count, "itemkeyid3"));
                            filterMap.put("dataset", new BigDecimal(dsWIGrp.getValue(count, "iteminstance", "1")));
                            row = dsSDI.findRow(filterMap);
                            if (row <= -1) continue;
                            rownum.add(row);
                        }
                        Iterator iterator = rownum.iterator();
                        while (iterator.hasNext()) {
                            row = (Integer)iterator.next();
                            filteredSDI.copyRow(dsSDI, row, 1);
                        }
                    }
                    this.processWorkItemItemRule(dsWIGrp, sdidataDS, new SDI(sdcid, keyid1, keyid2, keyid3), dsSDIWII, database, connectioninfo, sdcProcessor, beforeEditImage, modifiedStatus, toMakeAvailableDS, newQCDataSetProps, dsapprovals, filteredSDI);
                }
            }
            if (toMakeAvailableDS.size() > 0) {
                String actionId = "EditDataSet";
                String actionVersion = "1";
                ActionProcessor actionProcessor = new ActionProcessor(connectioninfo.getConnectionId());
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("keyid1", "");
                actionProps.setProperty("keyid2", "");
                actionProps.setProperty("keyid3", "");
                actionProps.setProperty("paramlistid", "");
                actionProps.setProperty("paramlistversionid", "");
                actionProps.setProperty("variantid", "");
                actionProps.setProperty("dataset", "");
                actionProps.setProperty("availabilityflag", "");
                for (int i = 0; i < toMakeAvailableDS.size(); ++i) {
                    PropertyList toMergeProp = toMakeAvailableDS.getPropertyList(i);
                    toMergeProp.forEach((key, value) -> actionProps.merge(key, value, (v1, v2) -> ((String)v1).equalsIgnoreCase("") ? v2 : v1 + ";" + v2));
                }
                actionProps.setProperty("sdcid", sdcid);
                actionProps.setProperty("propsmatch", "Y");
                actionProcessor.processAction(actionId, actionVersion, actionProps);
                actionProps.clear();
            }
            if (newQCDataSetProps.size() > 0) {
                AutomationService ac = new AutomationService(new SapphireConnection(database.getConnection(), connectioninfo));
                ac.addToDoListEntry(null, "QCBatchReagentSync", "1", newQCDataSetProps, null, true, connectioninfo.getSysuserId(), "", "", "");
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            database.closeStatement("getAllSDIWorkItemDS");
            database.closeStatement("getAllSDIData");
        }
    }

    private void processWorkItemItemRule(DataSet dsSDIWII, DataSet allds, SDI sdi, DataSet allSDIWI, DBAccess database, ConnectionInfo connectioninfo, SDCProcessor sdcProcessor, SDIData beforeEditImage, DataSet modifiedStatus, PropertyListCollection toMakeAvailableDS, PropertyList newQCDataSetProps, DataSet dsapprovals, DataSet updateddsSDI) throws SapphireException {
        QueryProcessor qp = new QueryProcessor(connectioninfo.getConnectionId());
        DataSet filteredModifiedStatus = this.getFilteredModifiedStatus(updateddsSDI, dsapprovals);
        HashMap filteredClassMap = new HashMap();
        DataSet alldsws = new DataSet();
        alldsws.addColumn("worksheetid", 0);
        alldsws.addColumn("sdcid", 0);
        alldsws.addColumn("keyid1", 0);
        alldsws.addColumn("keyid2", 0);
        alldsws.addColumn("keyid3", 0);
        alldsws.addColumn("paramlistid", 0);
        alldsws.addColumn("paramlistversionid", 0);
        alldsws.addColumn("variantid", 0);
        alldsws.addColumn("dataset", 1);
        if (OpalUtil.isNotEmpty(filteredModifiedStatus)) {
            Logger.logDebug("#### filteredModifiedStatus before loop");
            long start = Calendar.getInstance().getTimeInMillis();
            for (int pl = 0; pl < filteredModifiedStatus.getRowCount(); ++pl) {
                StringBuilder query = new StringBuilder();
                SafeSQL safeSQL = new SafeSQL();
                query.append("select qc.s_qcbatchid, ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset from s_qcbatch qc, sdidata ds ").append(" where ds.sdcid = ").append(safeSQL.addVar(filteredModifiedStatus.getValue(pl, "sdcid"))).append(" and ds.keyid1 = ").append(safeSQL.addVar(filteredModifiedStatus.getValue(pl, "keyid1"))).append(" and ds.keyid2 = ").append(safeSQL.addVar(filteredModifiedStatus.getValue(pl, "keyid2"))).append(" and ds.keyid3 = ").append(safeSQL.addVar(filteredModifiedStatus.getValue(pl, "keyid3"))).append(" and ds.paramlistid = ").append(safeSQL.addVar(filteredModifiedStatus.getValue(pl, "paramlistid"))).append(" and ds.paramlistversionid = ").append(safeSQL.addVar(filteredModifiedStatus.getValue(pl, "paramlistversionid"))).append(" and ds.variantid = ").append(safeSQL.addVar(filteredModifiedStatus.getValue(pl, "variantid"))).append(" and ds.dataset = ").append(safeSQL.addVar(filteredModifiedStatus.getValue(pl, "dataset"))).append(" and qc.s_qcbatchid = ds.s_qcbatchid");
                DataSet qcbatchds = qp.getPreparedSqlDataSet(query.toString(), safeSQL.getValues());
                if (!OpalUtil.isNotEmpty(qcbatchds) || qcbatchds.getRowCount() <= 0) continue;
                String qcbatchid = qcbatchds.getString(0, "s_qcbatchid");
                FilteredClass filteredClass = new FilteredClass(qcbatchds.getValue(0, "sdcid"), qcbatchds.getValue(0, "keyid1"), qcbatchds.getValue(0, "keyid2"), qcbatchds.getValue(0, "keyid3"), qcbatchds.getValue(0, "paramlistid"), qcbatchds.getValue(0, "paramlistversionid"), qcbatchds.getValue(0, "variantid"), String.valueOf(qcbatchds.getValue(0, "dataset")));
                if (filteredClassMap.containsKey(qcbatchid)) {
                    ((List)filteredClassMap.get(qcbatchid)).add(filteredClass);
                    continue;
                }
                ArrayList<FilteredClass> filterClassList = new ArrayList<FilteredClass>();
                filterClassList.add(filteredClass);
                filteredClassMap.put(qcbatchid, filterClassList);
            }
            long end = Calendar.getInstance().getTimeInMillis();
            Logger.logDebug("#### processWorkItemItemRule qcbatch changes took " + (end - start));
        }
        for (Map.Entry entry : filteredClassMap.entrySet()) {
            long start = Calendar.getInstance().getTimeInMillis();
            String qcbatchid = (String)entry.getKey();
            List filteredClasses = (List)entry.getValue();
            for (FilteredClass filteredClass : filteredClasses) {
                StringBuilder query = new StringBuilder();
                SafeSQL safeSQL = new SafeSQL();
                query.append("select w.worksheetid from worksheetsdi w, s_qcbatch qc").append(" where w.sdcid = ").append(safeSQL.addVar("QCBatch")).append(" and w.keyid1 = qc.s_qcbatchid").append(" and qc.s_qcbatchid = ").append(safeSQL.addVar(qcbatchid));
                DataSet dsws = qp.getPreparedSqlDataSet(query.toString(), safeSQL.getValues());
                if (!OpalUtil.isNotEmpty(dsws) || dsws.getRowCount() <= 0) continue;
                for (int i = 0; i < dsws.getRowCount(); ++i) {
                    int row = alldsws.addRow();
                    alldsws.setValue(row, "worksheetid", dsws.getValue(i, "worksheetid"));
                    alldsws.setValue(row, "sdcid", filteredClass.getSdcid());
                    alldsws.setValue(row, "keyid1", filteredClass.getKeyid1());
                    alldsws.setValue(row, "keyid2", filteredClass.getKeyid2());
                    alldsws.setValue(row, "keyid3", filteredClass.getKeyid3());
                    alldsws.setValue(row, "paramlistid", filteredClass.getParamlistid());
                    alldsws.setValue(row, "paramlistversionid", filteredClass.getParamlistversionid());
                    alldsws.setValue(row, "variantid", filteredClass.getVariantid());
                    alldsws.setValue(row, "dataset", filteredClass.getDataset());
                }
            }
            long end = Calendar.getInstance().getTimeInMillis();
            Logger.logDebug("#### processWorkItemItemRule ws changes took " + (end - start));
        }
        try {
            String actualWorkitemitemId;
            int r;
            TranslationProcessor tp = new TranslationProcessor(connectioninfo.getConnectionId());
            dsSDIWII.addColumn("actualworkitemitemid", 0);
            for (r = 0; r < dsSDIWII.getRowCount(); ++r) {
                String workitemitemId = dsSDIWII.getValue(r, "workitemitemid");
                if (workitemitemId.indexOf(".") > -1) {
                    actualWorkitemitemId = workitemitemId.substring(0, workitemitemId.indexOf("."));
                    dsSDIWII.setValue(r, "actualworkitemitemid", actualWorkitemitemId);
                    continue;
                }
                dsSDIWII.setValue(r, "actualworkitemitemid", workitemitemId);
            }
            for (r = 0; r < allds.getRowCount(); ++r) {
                String workitemitemId = allds.getValue(r, "workitemitemid");
                if (workitemitemId.indexOf(".") > -1) {
                    actualWorkitemitemId = workitemitemId.substring(0, workitemitemId.indexOf("."));
                    allds.setString(r, "actualworkitemitemid", actualWorkitemitemId);
                    continue;
                }
                allds.setString(r, "actualworkitemitemid", workitemitemId);
            }
            ArrayList<String> processedWorkItemId = new ArrayList<String>();
            DataSet dsSDI = new DataSet();
            for (int r2 = 0; r2 < dsSDIWII.getRowCount(); ++r2) {
                String priorItemKeyid2;
                String priorItemKeyid1;
                HashMap<String, Object> findRow;
                String workitemitemId = dsSDIWII.getValue(r2, "actualworkitemitemid");
                if (processedWorkItemId.contains(workitemitemId)) continue;
                processedWorkItemId.add(workitemitemId);
                String rule = dsSDIWII.getValue(r2, "workitemitemrule");
                String workitemId = dsSDIWII.getValue(r2, "workitemid");
                String workitemVersionId = dsSDIWII.getValue(r2, "workitemversionid");
                String workitemInstance = dsSDIWII.getValue(r2, "workiteminstance");
                String sdiworkitemid = dsSDIWII.getValue(r2, "sdiworkitemid");
                String itemSDC = dsSDIWII.getValue(r2, "itemsdcid");
                String itemKeyid1 = dsSDIWII.getValue(r2, "itemkeyid1");
                String itemKeyid2 = dsSDIWII.getValue(r2, "itemkeyid2");
                String itemKeyid3 = dsSDIWII.getValue(r2, "itemkeyid3");
                String sdiwii_workitemitemid = dsSDIWII.getValue(r2, "actualworkitemitemid");
                boolean paramListItem = "ParamList".equalsIgnoreCase(itemSDC);
                if (rule.length() <= 0) continue;
                JSONObject json = new JSONObject(rule);
                boolean always = true;
                String columnId = "";
                String columnValue = "";
                String columnOperator = "";
                if (json.has("includerule")) {
                    JSONArray includeRuleRows = json.getJSONArray("includerule");
                    JSONObject includeRuleRow = (JSONObject)includeRuleRows.get(0);
                    always = "includealways".equalsIgnoreCase(WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "includetimepoint"));
                    columnId = WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "columnid");
                    columnValue = WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "columnvalue");
                    columnOperator = WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "columnoperator");
                }
                JSONArray reflexRuleRows = json.getJSONArray("reflexrule");
                JSONObject reflexRuleRow = (JSONObject)reflexRuleRows.get(0);
                String ruleType = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "reflexruletype");
                String rule_timePt = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "timepoint");
                String rule_sourceItem = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "item");
                boolean availableRule = "available".equalsIgnoreCase(ruleType);
                if (availableRule && "Initially".equalsIgnoreCase(rule_timePt)) continue;
                String rule_paramlistid = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "paramlistid");
                String rule_paramlistType = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "paramlisttype");
                String rule_statusandlimit = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, paramListItem ? "datasetstatusandlimit" : "workitemstatusandlimit");
                String rule_Status = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, paramListItem ? "datasetstatus" : "workitemstatus");
                String rule_workitemid = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "workitemid");
                String rule_workiteminstance = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "workiteminstance");
                boolean allItem = "all".equalsIgnoreCase(WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "allorany"));
                String specCondition = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, paramListItem ? "datasetcondition" : "workitemcondition");
                JSONArray parameterArray = WorkItemItemRuleEvaluator.getRuleItemArray(reflexRuleRow, "parameter");
                DataSet parameters = new DataSet();
                for (int index = 0; index < parameterArray.length(); ++index) {
                    String parameter = (String)parameterArray.get(index);
                    if ("parameters".equalsIgnoreCase(parameter)) continue;
                    int bracketIndex = parameter.lastIndexOf("(");
                    String paramId = parameter.substring(0, bracketIndex).trim();
                    String paramType = parameter.substring(bracketIndex + 1, parameter.lastIndexOf(")")).trim();
                    int row = parameters.addRow();
                    parameters.setString(row, "paramid", paramId);
                    parameters.setString(row, "paramtype", paramType);
                }
                String limitType = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "paramlimittype");
                String limitCondition = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "paramlimitcondition");
                String dataWhen = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "datawhen");
                String durationType = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "durationtype");
                String duration = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "duration");
                String durationTimeUnit = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "durationunit");
                String relativeTo = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "relativeto");
                String relativeToDateColumn = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "relativetodate");
                HashMap<String, String> tokenMap = new HashMap<String, String>();
                tokenMap.put("sdcid", sdi.getSdcid());
                tokenMap.put("keyid1", sdi.getKeyid1());
                tokenMap.put("keyid2", sdi.getKeyid2());
                tokenMap.put("keyid3", sdi.getKeyid3());
                tokenMap.put("keytext", sdi.getKeyText());
                tokenMap.put("workitemid", workitemId);
                tokenMap.put("workitemversionid", workitemVersionId);
                tokenMap.put("workiteminstance", workitemInstance);
                tokenMap.put("itemkeyid1", itemKeyid1);
                tokenMap.put("itemkeyid2", itemKeyid2);
                tokenMap.put("itemkeyid3", itemKeyid3);
                DataSet dsUnavailableDS = new DataSet();
                HashMap<String, Object> filter = new HashMap<String, Object>();
                boolean evaluateRule = false;
                int thisSDIWIIIndex = r2;
                DataSet dsThisSDIWII = new DataSet();
                dsThisSDIWII.copyRow(dsSDIWII, thisSDIWIIIndex, 1);
                DataSet dsNotCancelled = new DataSet();
                for (int i = 0; i < allds.getRowCount(); ++i) {
                    if (allds.isNull(i, "availabilityflag")) {
                        allds.setString(i, "availabilityflag", "N");
                    }
                    if ("Cancelled".equalsIgnoreCase(allds.getValue(i, "s_datasetstatus"))) continue;
                    dsNotCancelled.copyRow(allds, i, 1);
                }
                if (availableRule) {
                    filter.put("paramlistid", itemKeyid1);
                    filter.put("paramlistversionid", itemKeyid2);
                    filter.put("variantid", itemKeyid3);
                    filter.put("availabilityflag", "N");
                    filter.put("actualworkitemitemid", sdiwii_workitemitemid);
                    dsUnavailableDS = dsNotCancelled.getFilteredDataSet(filter);
                    if (dsUnavailableDS.getRowCount() > 0) {
                        evaluateRule = true;
                    }
                } else if ("".equals(dsThisSDIWII.getValue(0, "iteminstance"))) {
                    evaluateRule = true;
                    if (paramListItem) {
                        String sdcid = dsThisSDIWII.getValue(0, "sdcid");
                        String keyid1 = dsThisSDIWII.getValue(0, "keyid1");
                        String keyid2 = dsThisSDIWII.getValue(0, "keyid2");
                        String keyid3 = dsThisSDIWII.getValue(0, "keyid3");
                        String wid = dsThisSDIWII.getValue(0, "workitemid");
                        String wiinstance = dsThisSDIWII.getValue(0, "workiteminstance");
                        String workitemitemid = dsThisSDIWII.getValue(0, "workitemitemid");
                        database.createPreparedResultSet("getsdiwii", "select * from sdiworkitemitem where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ?  and workitemid = ? and workiteminstance = ?  and workitemitemid = ?", new String[]{sdcid, keyid1, keyid2, keyid3, wid, wiinstance, workitemitemid});
                        DataSet sdiwii = new DataSet(database.getResultSet("getsdiwii"));
                        String iteminstance = sdiwii.getValue(0, "iteminstance");
                        database.closeResultSet("getsdiwii");
                        if ("".equals(iteminstance)) {
                            String actualWorkItemItemId = dsThisSDIWII.getValue(0, "actualworkitemitemid");
                            filter.clear();
                            filter.put("actualworkitemitemid", actualWorkItemItemId);
                            int repeatCount = dsSDIWII.getFilteredDataSet(filter).getRowCount();
                            dsThisSDIWII.setNumber(0, "repeatcount", repeatCount);
                        } else {
                            evaluateRule = false;
                        }
                    } else {
                        String groupId = dsThisSDIWII.getValue(0, "workitemid");
                        String groupInstance = dsThisSDIWII.getValue(0, "workiteminstance");
                        String workItemId = dsThisSDIWII.getValue(0, "itemkeyid1");
                        String workItemVersion = dsThisSDIWII.getValue(0, "itemkeyid2");
                        SafeSQL safeSQL = new SafeSQL();
                        if (database.checkPreparedExists("select 1 from sdiworkitemitem where sdcid = " + safeSQL.addVar(dsThisSDIWII.getValue(0, "sdcid")) + " and keyid1 = " + safeSQL.addVar(dsThisSDIWII.getValue(0, "keyid1")) + " and keyid2 = " + safeSQL.addVar(dsThisSDIWII.getValue(0, "keyid2")) + " and  keyid3 = " + safeSQL.addVar(dsThisSDIWII.getValue(0, "keyid3")) + " and  workitemid = " + safeSQL.addVar(groupId) + " and workiteminstance = " + safeSQL.addVar(groupInstance) + " and itemsdcid = 'WorkItem' and itemkeyid1 = " + safeSQL.addVar(workItemId) + " and itemkeyid2 = " + safeSQL.addVar(workItemVersion) + " and workitemitemid = " + safeSQL.addVar(workitemitemId) + " and iteminstance is not null", safeSQL.getValues())) {
                            evaluateRule = false;
                        }
                    }
                } else {
                    if ("applywhen".equalsIgnoreCase(ruleType)) {
                        String sourceWorkItemId = dsThisSDIWII.getValue(0, "itemkeyid1");
                        BigDecimal sourceWorkItemInstance = dsThisSDIWII.getBigDecimal(0, "iteminstance");
                        SafeSQL safeSQL = new SafeSQL();
                        if (!database.checkPreparedExists("select 1 from sdidata where sdcid = " + safeSQL.addVar(dsThisSDIWII.getValue(0, "sdcid")) + " and keyid1 = " + safeSQL.addVar(dsThisSDIWII.getValue(0, "keyid1")) + " and keyid2 = " + safeSQL.addVar(dsThisSDIWII.getValue(0, "keyid2")) + " and  keyid3 = " + safeSQL.addVar(dsThisSDIWII.getValue(0, "keyid3")) + " and  sourceworkitemid = " + safeSQL.addVar(sourceWorkItemId) + " and sourceworkiteminstance = " + safeSQL.addVar(sourceWorkItemInstance), safeSQL.getValues())) {
                            evaluateRule = true;
                        }
                    }
                    filter.put("paramlistid", itemKeyid1);
                    filter.put("paramlistversionid", itemKeyid2);
                    filter.put("variantid", itemKeyid3);
                    filter.put("availabilityflag", "N");
                    filter.put("actualworkitemitemid", sdiwii_workitemitemid);
                    dsUnavailableDS = dsNotCancelled.getFilteredDataSet(filter);
                    if (dsUnavailableDS.getRowCount() > 0) {
                        evaluateRule = true;
                        if (!availableRule) {
                            rule_sourceItem = "lastparameterlisttype";
                            rule_timePt = "when";
                            rule_paramlistType = "Preparation";
                            rule_statusandlimit = "hasstatus";
                            rule_Status = "Completed";
                            durationType = "";
                            duration = "";
                            durationTimeUnit = "";
                        }
                    }
                }
                if (evaluateRule && !always) {
                    if (columnId.length() > 0 && columnValue.length() > 0 && columnOperator.length() > 0) {
                        if (dsSDI.getRowCount() == 0) {
                            dsSDI = WorkItemItemRuleEvaluator.getSDIColumnValues(sdi, connectioninfo.getConnectionId(), database);
                        }
                        if (dsSDI.getRowCount() > 0) {
                            boolean columnConditionMatches;
                            columnValue = StringUtil.replaceAll(columnValue, "#semicolon#", ";");
                            String[] colValues = StringUtil.split(columnValue, ";");
                            List<String> columnValueList = Arrays.asList(colValues);
                            boolean inOperator = "in".equalsIgnoreCase(columnOperator);
                            boolean bl = inOperator ? columnValueList.contains(dsSDI.getValue(0, columnId)) : (columnConditionMatches = !columnValueList.contains(dsSDI.getValue(0, columnId)));
                            if (!columnConditionMatches) {
                                if (availableRule) {
                                    rule_sourceItem = "lastparameterlisttype";
                                    rule_timePt = "when";
                                    rule_paramlistType = "Preparation";
                                    rule_statusandlimit = "hasstatus";
                                    rule_Status = "Completed";
                                    durationType = "";
                                    duration = "";
                                    durationTimeUnit = "";
                                } else if (dsUnavailableDS.getRowCount() == 0) {
                                    evaluateRule = false;
                                }
                            }
                        }
                    } else {
                        throw new SapphireException("INVALID_PROPERTY", tp.translate("WorkItemItem rule evaluation failed for [keytext]. Missing column id or column value or column operator in the rule for [workitemid],[workitemversionid],[workiteminstance] | [itemkeyid1],[itemkeyid2],[itemkeyid3]!", tokenMap));
                    }
                }
                if (!evaluateRule) continue;
                boolean started = false;
                boolean isSDIWIInWorksheet = false;
                if (paramListItem && modifiedStatus != null && modifiedStatus.isValidColumn("paramlistid")) {
                    if ("Completed".equalsIgnoreCase(rule_Status) && database.checkPreparedExists("select 1 from worksheetsdi where  sdcid = ? and keyid1 = ? ", new String[]{"SDIWorkItem", sdiworkitemid})) {
                        isSDIWIInWorksheet = true;
                    }
                    if ("datasetcompletion".equalsIgnoreCase(dataWhen) || "Completed".equalsIgnoreCase(rule_Status) || "Cancelled".equalsIgnoreCase(rule_Status)) {
                        String statusToCheck;
                        String string = statusToCheck = "datasetcompletion".equalsIgnoreCase(dataWhen) ? "Completed" : rule_Status;
                        if (beforeEditImage != null && !this.checkIfAnyDatasetStatusModified(sdcProcessor, sdi.getSdcid(), beforeEditImage, modifiedStatus, statusToCheck, isSDIWIInWorksheet, alldsws)) {
                            continue;
                        }
                    } else if ("Started".equalsIgnoreCase(rule_Status)) {
                        if (beforeEditImage != null && !this.checkIfAnyDatasetStatusModified(sdcProcessor, sdi.getSdcid(), beforeEditImage, modifiedStatus, "Started", isSDIWIInWorksheet, alldsws)) continue;
                        started = true;
                    }
                } else if ("workitemcompletion".equalsIgnoreCase(dataWhen) || "Completed".equalsIgnoreCase(rule_Status) || "Cancelled".equalsIgnoreCase(rule_Status)) {
                    String statusToCheck;
                    String string = statusToCheck = "workitemcompletion".equalsIgnoreCase(dataWhen) ? "Completed" : rule_Status;
                    if (beforeEditImage != null && !this.checkIfAnyWorkItemStatusModified(sdcProcessor, sdi.getSdcid(), beforeEditImage, modifiedStatus, statusToCheck)) {
                        continue;
                    }
                } else if ("Started".equalsIgnoreCase(rule_Status) && beforeEditImage != null && !this.checkIfAnyWorkItemStatusModified(sdcProcessor, sdi.getSdcid(), beforeEditImage, modifiedStatus, "Started")) continue;
                if ("hasstatus".equalsIgnoreCase(rule_statusandlimit) && (rule_Status == null || rule_Status.length() == 0)) {
                    throw new SapphireException("INVALID_PROPERTY", tp.translate("WorkItemItem rule evaluation failed for [keytext]. DataSet status to check not specified for workitemitem [workitemid],[workitemversionid],[workiteminstance] | [itemkeyid1],[itemkeyid2],[itemkeyid3]!", tokenMap));
                }
                if ("hasspeclimit".equalsIgnoreCase(rule_statusandlimit) && (specCondition == null || specCondition.length() == 0)) {
                    throw new SapphireException("INVALID_PROPERTY", tp.translate("WorkItemItem rule evaluation failed for [keytext]. Spec condition to check not specified for workitemitem [workitemid],[workitemversionid],[workiteminstance] | [itemkeyid1],[itemkeyid2],[itemkeyid3]!", tokenMap));
                }
                if ("hasparamlimit".equalsIgnoreCase(rule_statusandlimit) && (limitType == null || limitType.length() == 0 || limitCondition == null || limitCondition.length() == 0)) {
                    throw new SapphireException("INVALID_PROPERTY", tp.translate("WorkItemItem rule evaluation failed for [keytext]. Limit Type/Limit condition to check not specified for workitemitem [workitemid],[workitemversionid],[workiteminstance] | [itemkeyid1],[itemkeyid2],[itemkeyid3]!", tokenMap));
                }
                if ("lastparameterlisttype".equalsIgnoreCase(rule_sourceItem)) {
                    if (rule_paramlistType != null && rule_paramlistType.length() > 0) {
                        HashMap<String, String> filterDS = new HashMap<String, String>();
                        filterDS.put("s_paramlisttype", rule_paramlistType);
                        DataSet plDataSets = allds.getFilteredDataSet(filterDS);
                        if (rule_Status.length() > 0) {
                            boolean allDSConditionMet = true;
                            for (int i = 0; i < plDataSets.getRowCount(); ++i) {
                                String datasetstatus = plDataSets.getString(i, "s_datasetstatus", "");
                                if ("Started".equalsIgnoreCase(rule_Status)) {
                                    if (started) continue;
                                    allDSConditionMet = false;
                                    break;
                                }
                                if (!"Cancelled".equalsIgnoreCase(rule_Status)) {
                                    boolean isInWorksheet = isSDIWIInWorksheet;
                                    if (modifiedStatus != null && modifiedStatus.getRowCount() > 0 && "Completed".equalsIgnoreCase(rule_Status) && !isSDIWIInWorksheet) {
                                        isInWorksheet = this.isDataSetInWorksheet(modifiedStatus, plDataSets, i);
                                    }
                                    if ("Completed".equalsIgnoreCase(rule_Status) && isInWorksheet) {
                                        if ("Cancelled".equalsIgnoreCase(datasetstatus) || datasetstatus.equalsIgnoreCase(rule_Status) || datasetstatus.equalsIgnoreCase("Released")) continue;
                                        allDSConditionMet = false;
                                        break;
                                    }
                                    if ("Cancelled".equalsIgnoreCase(datasetstatus) || datasetstatus.equalsIgnoreCase(rule_Status)) continue;
                                    allDSConditionMet = false;
                                    break;
                                }
                                if (datasetstatus.equalsIgnoreCase(rule_Status)) continue;
                                allDSConditionMet = false;
                                break;
                            }
                            if (!allDSConditionMet) continue;
                            this.executeParamListAction(sdi, plDataSets, dsThisSDIWII, dsUnavailableDS, rule_Status, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, null, toMakeAvailableDS, newQCDataSetProps, database, connectioninfo);
                            continue;
                        }
                        this.removeCalcelledRows(plDataSets, "s_datasetstatus");
                        if (plDataSets.getRowCount() <= 0) continue;
                        if ("hasspeclimit".equalsIgnoreCase(rule_statusandlimit)) {
                            this.checkDataSetSpecCondition(dataWhen, plDataSets, allItem, specCondition, parameters, sdi, dsThisSDIWII, dsUnavailableDS, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, toMakeAvailableDS, newQCDataSetProps, database, connectioninfo);
                            continue;
                        }
                        if (!"hasparamlimit".equalsIgnoreCase(rule_statusandlimit)) continue;
                        this.checkParamLimit(dataWhen, plDataSets, allItem, limitType, limitCondition, parameters, sdi, dsThisSDIWII, dsUnavailableDS, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, toMakeAvailableDS, newQCDataSetProps, database, connectioninfo);
                        continue;
                    }
                    throw new SapphireException("INVALID_PROPERTY", tp.translate("WorkItemItem rule evaluation Failed for [keytext]. Last ParameterList type not specified for [workitemid],[workitemversionid],[workiteminstance] | [itemkeyid1],[itemkeyid2],[itemkeyid3]!", tokenMap));
                }
                if ("ParameterList".equalsIgnoreCase(rule_sourceItem)) {
                    if (rule_paramlistid != null && rule_paramlistid.length() > 0) {
                        String sourceParamList = rule_paramlistid;
                        String sourceParamListVariant = "";
                        filter.clear();
                        if (rule_paramlistid.indexOf("(Variant:") > -1) {
                            sourceParamList = rule_paramlistid.substring(0, rule_paramlistid.indexOf("(Variant:")).trim();
                            String sourcePLVariantStr = rule_paramlistid.substring(rule_paramlistid.indexOf("(Variant:"), rule_paramlistid.lastIndexOf(")"));
                            sourceParamListVariant = sourcePLVariantStr.substring(9).trim();
                            filter.put("variantid", sourceParamListVariant);
                        }
                        filter.put("paramlistid", sourceParamList);
                        DataSet plDataSets = allds.getFilteredDataSet(filter);
                        if (plDataSets.getRowCount() <= 0) continue;
                        if (rule_Status.length() > 0) {
                            boolean conditionMet = this.isDataSetStatusMet(plDataSets, rule_Status, modifiedStatus, sdcProcessor, sdi.getSdcid(), beforeEditImage, isSDIWIInWorksheet);
                            if (!conditionMet) continue;
                            this.executeParamListAction(sdi, plDataSets, dsThisSDIWII, dsUnavailableDS, rule_Status, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, null, toMakeAvailableDS, newQCDataSetProps, database, connectioninfo);
                            continue;
                        }
                        this.removeCalcelledRows(plDataSets, "s_datasetstatus");
                        if (plDataSets.getRowCount() <= 0) continue;
                        if ("hasspeclimit".equalsIgnoreCase(rule_statusandlimit)) {
                            this.checkDataSetSpecCondition(dataWhen, plDataSets, allItem, specCondition, parameters, sdi, dsThisSDIWII, dsUnavailableDS, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, toMakeAvailableDS, newQCDataSetProps, database, connectioninfo);
                            continue;
                        }
                        if (!"hasparamlimit".equalsIgnoreCase(rule_statusandlimit)) continue;
                        this.checkParamLimit(dataWhen, plDataSets, allItem, limitType, limitCondition, parameters, sdi, dsThisSDIWII, dsUnavailableDS, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, toMakeAvailableDS, newQCDataSetProps, database, connectioninfo);
                        continue;
                    }
                    throw new SapphireException("INVALID_PROPERTY", tp.translate("WorkItemItem rule Evaluation Failed for [keytext]. ParamList not specified for [workitemid],[workitemversionid],[workiteminstance] | [itemkeyid1],[itemkeyid2],[itemkeyid3]!", tokenMap));
                }
                if ("priorparameterlist".equalsIgnoreCase(rule_sourceItem)) {
                    int usersequence = dsThisSDIWII.getInt(0, "sdiwii_usersequence");
                    int priorPLIndex = -1;
                    findRow = new HashMap<String, Object>();
                    findRow.put("itemsdcid", "ParamList");
                    for (int priorWIIUserSequence = usersequence - 1; priorWIIUserSequence > 0; --priorWIIUserSequence) {
                        findRow.put("sdiwii_usersequence", new BigDecimal(priorWIIUserSequence));
                        priorPLIndex = dsSDIWII.findRow(findRow);
                        if (priorPLIndex > -1) break;
                    }
                    if (priorPLIndex <= -1) continue;
                    priorItemKeyid1 = dsSDIWII.getValue(priorPLIndex, "itemkeyid1");
                    priorItemKeyid2 = dsSDIWII.getValue(priorPLIndex, "itemkeyid2");
                    String priorItemKeyid3 = dsSDIWII.getValue(priorPLIndex, "itemkeyid3");
                    String priorWorkitemitemid = dsSDIWII.getValue(priorPLIndex, "actualworkitemitemid");
                    filter.clear();
                    filter.put("paramlistid", priorItemKeyid1);
                    filter.put("paramlistversionid", priorItemKeyid2);
                    filter.put("variantid", priorItemKeyid3);
                    filter.put("actualworkitemitemid", priorWorkitemitemid);
                    DataSet priorPLDataSets = allds.getFilteredDataSet(filter);
                    if (priorPLDataSets.getRowCount() <= 0) continue;
                    if (rule_Status.length() > 0) {
                        boolean conditionMet = this.isDataSetStatusMet(priorPLDataSets, rule_Status, modifiedStatus, sdcProcessor, sdi.getSdcid(), beforeEditImage, isSDIWIInWorksheet);
                        if (!conditionMet) continue;
                        this.executeParamListAction(sdi, priorPLDataSets, dsThisSDIWII, dsUnavailableDS, rule_Status, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, null, toMakeAvailableDS, newQCDataSetProps, database, connectioninfo);
                        continue;
                    }
                    this.removeCalcelledRows(priorPLDataSets, "s_datasetstatus");
                    if (priorPLDataSets.getRowCount() <= 0) continue;
                    if ("hasspeclimit".equalsIgnoreCase(rule_statusandlimit)) {
                        this.checkDataSetSpecCondition(dataWhen, priorPLDataSets, allItem, specCondition, parameters, sdi, dsThisSDIWII, dsUnavailableDS, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, toMakeAvailableDS, newQCDataSetProps, database, connectioninfo);
                        continue;
                    }
                    if (!"hasparamlimit".equalsIgnoreCase(rule_statusandlimit)) continue;
                    this.checkParamLimit(dataWhen, priorPLDataSets, allItem, limitType, limitCondition, parameters, sdi, dsThisSDIWII, dsUnavailableDS, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, toMakeAvailableDS, newQCDataSetProps, database, connectioninfo);
                    continue;
                }
                if ("workitem".equalsIgnoreCase(rule_sourceItem)) {
                    if (rule_workitemid != null && rule_workitemid.length() > 0) {
                        String sourceWorkItem = rule_workitemid;
                        filter.clear();
                        filter.put("workitemid", sourceWorkItem);
                        if (rule_workiteminstance != null && rule_workiteminstance.length() > 0) {
                            filter.put("workiteminstance", new BigDecimal(rule_workiteminstance));
                        }
                        String groupId = dsThisSDIWII.getValue(0, "groupid");
                        int groupInstance = dsThisSDIWII.getInt(0, "groupinstance");
                        filter.put("groupid", groupId);
                        filter.put("groupinstance", new BigDecimal(groupInstance));
                        DataSet workItemDS = allSDIWI.getFilteredDataSet(filter);
                        if (workItemDS.getRowCount() <= 0) continue;
                        if (rule_Status.length() > 0) {
                            boolean conditionMet = this.isWorkItemStatusMet(workItemDS, rule_Status, sdcProcessor, sdi.getSdcid(), beforeEditImage, modifiedStatus);
                            if (!conditionMet) continue;
                            this.executeWorkItemAction(sdi, workItemDS, dsThisSDIWII, rule_Status, ruleType, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, null, database, connectioninfo);
                            continue;
                        }
                        this.removeCalcelledRows(workItemDS, "workitemstatus");
                        if (workItemDS.getRowCount() <= 0 || !"hasspeclimit".equalsIgnoreCase(rule_statusandlimit)) continue;
                        this.checkSDIWorkItemSpecCondition(dataWhen, workItemDS, allItem, specCondition, parameters, sdi, dsThisSDIWII, ruleType, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, database, connectioninfo);
                        continue;
                    }
                    throw new SapphireException("INVALID_PROPERTY", tp.translate("WorkItemItem rule Evaluation Failed for [keytext]. WorkItem not specified for [workitemid],[workitemversionid],[workiteminstance] | [itemkeyid1],[itemkeyid2]!", tokenMap));
                }
                if (!"priorworkitem".equalsIgnoreCase(rule_sourceItem)) continue;
                int usersequence = dsThisSDIWII.getInt(0, "sdiwii_usersequence");
                int priorItemIndex = -1;
                findRow = new HashMap();
                findRow.put("itemsdcid", "WorkItem");
                for (int priorWIIUserSequence = usersequence - 1; priorWIIUserSequence > 0; --priorWIIUserSequence) {
                    findRow.put("sdiwii_usersequence", new BigDecimal(priorWIIUserSequence));
                    priorItemIndex = dsSDIWII.findRow(findRow);
                    if (priorItemIndex > -1) break;
                }
                if (priorItemIndex <= -1) continue;
                priorItemKeyid1 = dsSDIWII.getValue(priorItemIndex, "itemkeyid1");
                priorItemKeyid2 = dsSDIWII.getValue(priorItemIndex, "itemkeyid2");
                String priorItemInstance = dsSDIWII.getValue(priorItemIndex, "iteminstance", "-1");
                filter.clear();
                filter.put("workitemid", priorItemKeyid1);
                filter.put("workiteminstance", new BigDecimal(priorItemInstance));
                String groupId = dsThisSDIWII.getValue(0, "groupid");
                int groupInstance = dsThisSDIWII.getInt(0, "groupinstance");
                filter.put("groupid", groupId);
                filter.put("groupinstance", new BigDecimal(groupInstance));
                DataSet workItemDS = allSDIWI.getFilteredDataSet(filter);
                if (workItemDS.getRowCount() <= 0) continue;
                if (rule_Status.length() > 0) {
                    boolean conditionMet = this.isWorkItemStatusMet(workItemDS, rule_Status, sdcProcessor, sdi.getSdcid(), beforeEditImage, modifiedStatus);
                    if (!conditionMet) continue;
                    this.executeWorkItemAction(sdi, workItemDS, dsThisSDIWII, rule_Status, ruleType, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, null, database, connectioninfo);
                    continue;
                }
                this.removeCalcelledRows(workItemDS, "workitemstatus");
                if (workItemDS.getRowCount() <= 0 || !"hasspeclimit".equalsIgnoreCase(rule_statusandlimit)) continue;
                this.checkSDIWorkItemSpecCondition(dataWhen, workItemDS, allItem, specCondition, parameters, sdi, dsThisSDIWII, ruleType, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, database, connectioninfo);
            }
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
    }

    private DataSet getFilteredModifiedStatus(DataSet modifiedStatus, DataSet dsapprovals) {
        DataSet filteredModifiedStatus = new DataSet();
        HashMap<String, String> datasetfilter = new HashMap<String, String>();
        HashSet<FilteredClass> filteredClassSet = new HashSet<FilteredClass>();
        if (OpalUtil.isNotEmpty(modifiedStatus) && OpalUtil.isNotEmpty(dsapprovals)) {
            for (int i = 0; i < modifiedStatus.getRowCount(); ++i) {
                FilteredClass filteredClass;
                if (!this.notConataingNull(modifiedStatus.getValue(i, "paramlistid"), modifiedStatus.getValue(i, "paramlistversionid"), modifiedStatus.getValue(i, "variantid"), modifiedStatus.getValue(i, "dataset")) || filteredClassSet.contains(filteredClass = new FilteredClass(modifiedStatus.getValue(i, "sdcid"), modifiedStatus.getValue(i, "keyid1"), modifiedStatus.getValue(i, "keyid2"), modifiedStatus.getValue(i, "keyid3"), modifiedStatus.getValue(i, "paramlistid"), modifiedStatus.getValue(i, "paramlistversionid"), modifiedStatus.getValue(i, "variantid"), modifiedStatus.getValue(i, "dataset")))) continue;
                filteredClassSet.add(filteredClass);
                datasetfilter.clear();
                datasetfilter.put("sdcid", modifiedStatus.getValue(i, "sdcid"));
                datasetfilter.put("keyid1", modifiedStatus.getValue(i, "keyid1"));
                datasetfilter.put("keyid2", modifiedStatus.getValue(i, "keyid2"));
                datasetfilter.put("keyid3", modifiedStatus.getValue(i, "keyid3"));
                datasetfilter.put("paramlistid", modifiedStatus.getValue(i, "paramlistid"));
                datasetfilter.put("paramlistversionid", modifiedStatus.getValue(i, "paramlistversionid"));
                datasetfilter.put("variantid", modifiedStatus.getValue(i, "variantid"));
                if (dsapprovals.findRow(datasetfilter) <= -1) continue;
                filteredModifiedStatus.copyRow(modifiedStatus, i, 1);
            }
        }
        return filteredModifiedStatus;
    }

    private boolean notConataingNull(String paramlistid, String paramlistversionid, String variantid, String dataset) {
        return OpalUtil.isNotEmpty(paramlistid) && !paramlistid.equalsIgnoreCase("(null)") && OpalUtil.isNotEmpty(paramlistversionid) && !paramlistversionid.equalsIgnoreCase("(null)") && OpalUtil.isNotEmpty(variantid) && !variantid.equalsIgnoreCase("(null)") && OpalUtil.isNotEmpty(dataset) && !dataset.equalsIgnoreCase("(null)");
    }

    private boolean isDataSetInWorksheet(DataSet modifiedStatus, DataSet plDataSets, int plDataSetIndex) {
        HashMap<String, Object> find = new HashMap<String, Object>();
        find.put("sdcid", plDataSets.getValue(plDataSetIndex, "sdcid"));
        find.put("keyid1", plDataSets.getValue(plDataSetIndex, "keyid1"));
        find.put("keyid2", plDataSets.getValue(plDataSetIndex, "keyid2"));
        find.put("keyid3", plDataSets.getValue(plDataSetIndex, "keyid3"));
        find.put("paramlistid", plDataSets.getValue(plDataSetIndex, "paramlistid"));
        find.put("paramlistversionid", plDataSets.getValue(plDataSetIndex, "paramlistversionid"));
        find.put("variantid", plDataSets.getValue(plDataSetIndex, "variantid"));
        find.put("dataset", plDataSets.getBigDecimal(plDataSetIndex, "dataset"));
        find.put("inworksheet", "Y");
        return modifiedStatus.findRow(find) > -1;
    }

    private void removeCalcelledRows(DataSet ds, String statusColumn) {
        int d = 0;
        while (d < ds.getRowCount()) {
            if ("Cancelled".equalsIgnoreCase(ds.getValue(d, statusColumn))) {
                ds.remove(d);
                continue;
            }
            ++d;
        }
    }

    private boolean checkIfAnyDatasetStatusModified(SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, DataSet modifiedDataSetStatus, String dsStatus, boolean isSDIWIInWorksheet, DataSet dsInWS) throws SapphireException {
        long cstartTime = System.currentTimeMillis();
        boolean datasetModified = false;
        if ("Started".equalsIgnoreCase(dsStatus)) {
            for (int pl = 0; pl < modifiedDataSetStatus.getRowCount(); ++pl) {
                String prevDatasetStatus = this.getOldSDIDataValue(sdcProcessor, sdcid, beforeEditImage, modifiedDataSetStatus, pl, "s_datasetstatus");
                String updatedDatasetStatus = modifiedDataSetStatus.getValue(pl, "s_datasetstatus");
                if (!"Initial".equalsIgnoreCase(prevDatasetStatus) || updatedDatasetStatus.length() <= 0 || "Cancelled".equalsIgnoreCase(updatedDatasetStatus)) continue;
                datasetModified = true;
            }
        } else {
            for (int count = 0; count < modifiedDataSetStatus.getRowCount(); ++count) {
                boolean isInWorksheet = isSDIWIInWorksheet;
                if ("Completed".equalsIgnoreCase(dsStatus) && !isSDIWIInWorksheet) {
                    HashMap<String, Object> filter = null;
                    if (OpalUtil.isNotEmpty(dsInWS)) {
                        filter = new HashMap<String, Object>();
                    }
                    if (filter != null) {
                        filter.clear();
                        filter.put("sdcid", modifiedDataSetStatus.getValue(count, "sdcid"));
                        filter.put("keyid1", modifiedDataSetStatus.getValue(count, "keyid1"));
                        filter.put("keyid2", modifiedDataSetStatus.getValue(count, "keyid2"));
                        filter.put("keyid3", modifiedDataSetStatus.getValue(count, "keyid3"));
                        filter.put("paramlistid", modifiedDataSetStatus.getValue(count, "paramlistid"));
                        filter.put("paramlistversionid", modifiedDataSetStatus.getValue(count, "paramlistversionid"));
                        filter.put("variantid", modifiedDataSetStatus.getValue(count, "variantid"));
                        filter.put("dataset", new BigDecimal(modifiedDataSetStatus.getValue(count, "dataset")));
                        if (dsInWS.findRow(filter) > -1) {
                            isInWorksheet = true;
                            modifiedDataSetStatus.setString(count, "inworksheet", "Y");
                        }
                    }
                }
                String updatedDatasetStatus = modifiedDataSetStatus.getValue(count, "s_datasetstatus");
                if (datasetModified) continue;
                if ("Completed".equalsIgnoreCase(dsStatus) && isInWorksheet) {
                    if (!dsStatus.equalsIgnoreCase(updatedDatasetStatus) && !"Released".equalsIgnoreCase(updatedDatasetStatus)) continue;
                    datasetModified = true;
                    continue;
                }
                if (!dsStatus.equalsIgnoreCase(updatedDatasetStatus)) continue;
                datasetModified = true;
            }
        }
        return datasetModified;
    }

    private boolean checkIfAnyWorkItemStatusModified(SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, DataSet modifiedWorkItemStatus, String wiStatus) {
        if ("Started".equalsIgnoreCase(wiStatus)) {
            for (int w = 0; w < modifiedWorkItemStatus.getRowCount(); ++w) {
                String prevWorkItemStatus = this.getOldSDIWorkItemValue(sdcProcessor, sdcid, beforeEditImage, modifiedWorkItemStatus, w, "workitemstatus");
                String updatedWorkItemStatus = modifiedWorkItemStatus.getValue(w, "workitemstatus");
                if (!"Initial".equalsIgnoreCase(prevWorkItemStatus) || updatedWorkItemStatus.length() <= 0 || "Cancelled".equalsIgnoreCase(updatedWorkItemStatus)) continue;
                return true;
            }
        } else {
            for (int w = 0; w < modifiedWorkItemStatus.getRowCount(); ++w) {
                String updatedWorkItemStatus = modifiedWorkItemStatus.getValue(w, "workitemstatus");
                if (!wiStatus.equalsIgnoreCase(updatedWorkItemStatus)) continue;
                return true;
            }
        }
        return false;
    }

    private String getDefaultAvailabilityRule(String paramlistType) {
        if ("Preparation".equalsIgnoreCase(paramlistType)) {
            return this.default_prepAvailabilityRule;
        }
        if ("Procedural".equalsIgnoreCase(paramlistType)) {
            return this.default_procAvailabilityRule;
        }
        return "";
    }

    public static String getRuleItemValue(JSONObject reflexRuleRow, String ruleId) throws SapphireException {
        try {
            if (reflexRuleRow.has(ruleId)) {
                return reflexRuleRow.getString(ruleId);
            }
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
        return "";
    }

    public static JSONArray getRuleItemArray(JSONObject reflexRuleRow, String ruleId) throws SapphireException {
        try {
            if (reflexRuleRow.has(ruleId)) {
                return reflexRuleRow.getJSONArray(ruleId);
            }
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
        return new JSONArray();
    }

    private void checkDataSetSpecCondition(String dataWhen, DataSet plDataSets, boolean all, String matchSpecCondition, DataSet parameters, SDI sdi, DataSet dsThisSDIWII, DataSet dsUnavailableDS, String durationType, String duration, String durationTimeUnit, String relativeTo, String relativeToDateColumn, HashMap tokenMap, PropertyListCollection toMakeAvailableDS, PropertyList newQCDataSetProps, DBAccess database, ConnectionInfo connectonInfo) throws SapphireException {
        boolean conditionMet = this.isSpecConditionMet("ParamList", plDataSets, all, matchSpecCondition, dataWhen, parameters, database);
        if (conditionMet) {
            this.executeParamListAction(sdi, plDataSets, dsThisSDIWII, dsUnavailableDS, "", durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, all, toMakeAvailableDS, newQCDataSetProps, database, connectonInfo);
        }
    }

    private void checkSDIWorkItemSpecCondition(String dataWhen, DataSet sourceSDIWI, boolean all, String matchSpecCondition, DataSet parameters, SDI sdi, DataSet dsThisSDIWII, String ruleType, String durationType, String duration, String durationTimeUnit, String relativeTo, String relativeToDateColumn, HashMap tokenMap, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        boolean conditionMet = this.isSpecConditionMet("WorkItem", sourceSDIWI, all, matchSpecCondition, dataWhen, parameters, database);
        if (conditionMet) {
            this.executeWorkItemAction(sdi, sourceSDIWI, dsThisSDIWII, "", ruleType, durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, all, database, connectionInfo);
        }
    }

    private void checkParamLimit(String dataWhen, DataSet plDataSets, boolean allItem, String limitType, String matchLimitCondition, DataSet parameters, SDI sdi, DataSet dsThisSDIWII, DataSet dsUnavailableDS, String durationType, String duration, String durationTimeUnit, String relativeTo, String relativeToDateColumn, HashMap tokenMap, PropertyListCollection toMakeAvailableDS, PropertyList newQCDataSetProps, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        boolean conditionMet = this.isLimitConditionMatched(plDataSets, allItem, limitType, matchLimitCondition, dataWhen.equalsIgnoreCase("dataitemrelease"), parameters, database);
        if (conditionMet) {
            this.executeParamListAction(sdi, plDataSets, dsThisSDIWII, dsUnavailableDS, "", durationType, duration, durationTimeUnit, relativeTo, relativeToDateColumn, tokenMap, allItem, toMakeAvailableDS, newQCDataSetProps, database, connectionInfo);
        }
    }

    private boolean isLimitConditionMatched(DataSet plDataSets, boolean allLimit, String limitType, String limitCondition, boolean onDataItemRelease, DataSet parameters, DBAccess database) throws SapphireException {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("s_datasetstatus", "Completed");
        DataSet dsCompleted = plDataSets.getFilteredDataSet(filter);
        if (allLimit ? dsCompleted.getRowCount() != plDataSets.getRowCount() : !onDataItemRelease && dsCompleted.getRowCount() != plDataSets.getRowCount()) {
            return false;
        }
        String sql = "select sl.*, di.releasedflag from sdidataitem di, sdidataitemlimits sl where  di.sdcid = sl.sdcid and di.keyid1 = sl.keyid1 and di.keyid2 = sl.keyid2 and di.keyid3 = sl.keyid3 and di.paramlistid = sl.paramlistid and di.paramlistversionid = sl.paramlistversionid and di.variantid = sl.variantid  and di.dataset = sl.dataset and di.paramid = sl.paramid and di.paramtype = sl.paramtype and di.replicateid = sl.replicateid and sl.sdcid = ? and  sl.keyid1 = ? and sl.keyid2 = ? and sl.keyid3 = ? and sl.paramlistid = ? and sl.paramlistversionid = ?  and sl.variantid = ? and sl.dataset = ? and sl.limittypeid = ?";
        PreparedStatement getsdidataitemLimits = database.prepareStatement("getsdidataitemLimit", sql);
        DataSet allDataSetLimit = new DataSet();
        try {
            for (int i = 0; i < plDataSets.getRowCount(); ++i) {
                filter.clear();
                String sdcid = plDataSets.getValue(i, "sdcid");
                String keyid1 = plDataSets.getValue(i, "keyid1");
                String keyid2 = plDataSets.getValue(i, "keyid2");
                String keyid3 = plDataSets.getValue(i, "keyid3");
                String paramlistid = plDataSets.getValue(i, "paramlistid");
                String paramlistversionid = plDataSets.getValue(i, "paramlistversionid");
                String variantid = plDataSets.getValue(i, "variantid");
                String dataset = plDataSets.getValue(i, "dataset");
                getsdidataitemLimits.setString(1, sdcid);
                getsdidataitemLimits.setString(2, keyid1);
                getsdidataitemLimits.setString(3, keyid2);
                getsdidataitemLimits.setString(4, keyid3);
                getsdidataitemLimits.setString(5, paramlistid);
                getsdidataitemLimits.setString(6, paramlistversionid);
                getsdidataitemLimits.setString(7, variantid);
                getsdidataitemLimits.setString(8, dataset);
                getsdidataitemLimits.setString(9, limitType);
                DataSet dsDataItemLimits = new DataSet(getsdidataitemLimits.executeQuery());
                if (dsDataItemLimits.getRowCount() <= 0) continue;
                this.getDataItemsToCheck(dsDataItemLimits, parameters, allDataSetLimit);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            database.closeStatement("getsdidataitemLimit");
        }
        if (allDataSetLimit.getRowCount() > 0) {
            boolean met = "met".equalsIgnoreCase(limitCondition);
            return this.isParamLimitMatched(plDataSets, allDataSetLimit, allLimit, met, onDataItemRelease);
        }
        return false;
    }

    private boolean isParamLimitMatched(DataSet sourceDS, DataSet dsLimit, boolean all, boolean met, boolean onDataItemRelease) {
        boolean isMet = false;
        for (int d = 0; d < dsLimit.getRowCount(); ++d) {
            boolean statusFlagMatched;
            boolean released = "Y".equalsIgnoreCase(dsLimit.getValue(d, "releasedflag"));
            boolean bl = statusFlagMatched = met ? "Y".equalsIgnoreCase(dsLimit.getValue(d, "statusflag")) : "N".equalsIgnoreCase(dsLimit.getValue(d, "statusflag"));
            boolean bl2 = onDataItemRelease ? released && statusFlagMatched : (isMet = statusFlagMatched);
            if (isMet) {
                if (all) continue;
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("sdcid", dsLimit.getValue(d, "sdcid"));
                map.put("keyid1", dsLimit.getValue(d, "keyid1"));
                map.put("keyid2", dsLimit.getValue(d, "keyid2"));
                map.put("keyid3", dsLimit.getValue(d, "keyid3"));
                map.put("paramlistid", dsLimit.getValue(d, "paramlistid"));
                map.put("paramlistversionid", dsLimit.getValue(d, "paramlistversionid"));
                map.put("variantid", dsLimit.getValue(d, "variantid"));
                map.put("dataset", new BigDecimal(dsLimit.getValue(d, "dataset")));
                int findSource = sourceDS.findRow(map);
                if (findSource > -1) {
                    sourceDS.setString(findSource, "__sourceitem", "Y");
                }
                return isMet;
            }
            if (!all) continue;
            return isMet;
        }
        return isMet;
    }

    private boolean isSpecConditionMet(String itemSDC, DataSet sourceDS, boolean allSpec, String specCondition, String dataWhen, DataSet parameters, DBAccess database) throws SapphireException {
        boolean paramListSDC = "ParamList".equalsIgnoreCase(itemSDC);
        String statusColumn = paramListSDC ? "s_datasetstatus" : "workitemstatus";
        boolean onDataItemRelease = dataWhen.equalsIgnoreCase("dataitemrelease");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put(statusColumn, "Completed");
        DataSet dsCompleted = sourceDS.getFilteredDataSet(filter);
        if (allSpec ? dsCompleted.getRowCount() != sourceDS.getRowCount() : !onDataItemRelease && (paramListSDC || "WorkItemCompletion".equalsIgnoreCase(dataWhen)) && dsCompleted.getRowCount() != sourceDS.getRowCount()) {
            return false;
        }
        String sql = "";
        if (paramListSDC) {
            sql = "select sp.*, di.releasedflag from sdidataitem di, sdidataitemspec sp where  di.sdcid = sp.sdcid and di.keyid1 = sp.keyid1 and di.keyid2 = sp.keyid2 and di.keyid3 = sp.keyid3 and di.paramlistid = sp.paramlistid and di.paramlistversionid = sp.paramlistversionid and di.variantid = sp.variantid  and di.dataset = sp.dataset and di.paramid = sp.paramid and di.paramtype = sp.paramtype and di.replicateid = sp.replicateid and sp.sdcid = ? and  sp.keyid1 = ? and sp.keyid2 = ? and sp.keyid3 = ? and sp.paramlistid = ? and sp.paramlistversionid = ?  and sp.variantid = ? and sp.dataset = ? order by sp.paramid";
        } else {
            sql = "select sp.*, ds.s_datasetstatus, di.releasedflag, ds.sourceworkitemid, ds.sourceworkiteminstance from sdidataitem di, sdidataitemspec sp, sdidata ds where  di.sdcid = sp.sdcid and di.keyid1 = sp.keyid1 and di.keyid2 = sp.keyid2 and di.keyid3 = sp.keyid3 and di.paramlistid = sp.paramlistid and di.paramlistversionid = sp.paramlistversionid and di.variantid = sp.variantid  and di.dataset = sp.dataset and di.paramid = sp.paramid and di.paramtype = sp.paramtype and di.replicateid = sp.replicateid and sp.sdcid = ds.sdcid and sp.keyid1 = ds.keyid1 and sp.keyid2 = ds.keyid2 and sp.keyid3 = ds.keyid3 and sp.paramlistid = ds.paramlistid and sp.paramlistversionid = ds.paramlistversionid  and sp.variantid = ds.variantid and sp.dataset = ds.dataset and ds.sdcid = ? and ds.keyid1 = ? and ds.keyid2 = ? and ds.keyid3 = ? and  ds.sourceworkitemid = ? and ds.sourceworkiteminstance = ?";
            if (!paramListSDC && "datasetcompletion".equalsIgnoreCase(dataWhen)) {
                sql = sql + " and ds.s_datasetstatus = 'Completed'";
            }
            sql = sql + " order by sp.paramid";
        }
        PreparedStatement getsdidataitemSpec = database.prepareStatement("getsdidataitemSpec", sql);
        DataSet dsSpec = new DataSet();
        try {
            for (int i = 0; i < sourceDS.getRowCount(); ++i) {
                filter.clear();
                String sdcid = sourceDS.getValue(i, "sdcid");
                String keyid1 = sourceDS.getValue(i, "keyid1");
                String keyid2 = sourceDS.getValue(i, "keyid2");
                String keyid3 = sourceDS.getValue(i, "keyid3");
                getsdidataitemSpec.setString(1, sdcid);
                getsdidataitemSpec.setString(2, keyid1);
                getsdidataitemSpec.setString(3, keyid2);
                getsdidataitemSpec.setString(4, keyid3);
                if (paramListSDC) {
                    String paramlistid = sourceDS.getValue(i, "paramlistid");
                    String paramlistversionid = sourceDS.getValue(i, "paramlistversionid");
                    String variantid = sourceDS.getValue(i, "variantid");
                    String dataset = sourceDS.getValue(i, "dataset");
                    getsdidataitemSpec.setString(5, paramlistid);
                    getsdidataitemSpec.setString(6, paramlistversionid);
                    getsdidataitemSpec.setString(7, variantid);
                    getsdidataitemSpec.setString(8, dataset);
                } else {
                    String workitemid = sourceDS.getValue(i, "workitemid");
                    String workiteminstance = sourceDS.getValue(i, "workiteminstance");
                    getsdidataitemSpec.setString(5, workitemid);
                    getsdidataitemSpec.setString(6, workiteminstance);
                }
                DataSet dsDataItemSpec = new DataSet(getsdidataitemSpec.executeQuery());
                if (dsDataItemSpec.getRowCount() <= 0) continue;
                this.getDataItemsToCheck(dsDataItemSpec, parameters, dsSpec);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            database.closeStatement("getsdidataitemSpec");
        }
        if (dsSpec.getRowCount() > 0) {
            return this.isSpecCondMet(paramListSDC, sourceDS, dsSpec, allSpec, specCondition, onDataItemRelease);
        }
        return false;
    }

    private void getDataItemsToCheck(DataSet ds, DataSet parameters, DataSet dsFinal) {
        if (parameters.size() > 0) {
            for (int s = 0; s < ds.getRowCount(); ++s) {
                String paramid = ds.getValue(s, "paramid");
                String paramtype = ds.getValue(s, "paramtype");
                HashMap<String, String> findParam = new HashMap<String, String>();
                findParam.put("paramid", paramid);
                findParam.put("paramtype", paramtype);
                if (parameters.findRow(findParam) <= -1) continue;
                dsFinal.copyRow(ds, s, 1);
            }
        } else {
            dsFinal.copyRow(ds, -1, 1);
        }
    }

    private boolean isSpecCondMet(boolean paramlistSDC, DataSet sourceDS, DataSet dsSpec, boolean all, String condition, boolean onDataItemRelease) {
        boolean isMet = false;
        for (int d = 0; d < dsSpec.getRowCount(); ++d) {
            boolean released = "Y".equalsIgnoreCase(dsSpec.getValue(d, "releasedflag"));
            boolean conditionMet = condition.equalsIgnoreCase(dsSpec.getValue(d, "condition"));
            boolean bl = onDataItemRelease ? released && conditionMet : (isMet = conditionMet);
            if (isMet) {
                if (all) continue;
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("sdcid", dsSpec.getValue(d, "sdcid"));
                map.put("keyid1", dsSpec.getValue(d, "keyid1"));
                map.put("keyid2", dsSpec.getValue(d, "keyid2"));
                map.put("keyid3", dsSpec.getValue(d, "keyid3"));
                if (paramlistSDC) {
                    map.put("paramlistid", dsSpec.getValue(d, "paramlistid"));
                    map.put("paramlistversionid", dsSpec.getValue(d, "paramlistversionid"));
                    map.put("variantid", dsSpec.getValue(d, "variantid"));
                    map.put("dataset", new BigDecimal(dsSpec.getValue(d, "dataset")));
                } else {
                    map.put("workitemid", dsSpec.getValue(d, "sourceworkitemid"));
                    map.put("workiteminstance", new BigDecimal(dsSpec.getValue(d, "sourceworkiteminstance")));
                }
                int findSource = sourceDS.findRow(map);
                if (findSource > -1) {
                    sourceDS.setString(findSource, "__sourceitem", "Y");
                }
                return isMet;
            }
            if (!all) continue;
            return isMet;
        }
        return isMet;
    }

    private boolean isDataSetStatusMet(DataSet plDataSets, String datasetStatus, DataSet modifiedDataSetStatus, SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, boolean isSDIWIInWorksheet) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        boolean conditionMet = false;
        for (int i = 0; i < plDataSets.getRowCount(); ++i) {
            if ("Started".equalsIgnoreCase(datasetStatus)) {
                if (beforeEditImage != null) {
                    filter.clear();
                    filter.put("sdcid", plDataSets.getValue(i, "sdcid"));
                    filter.put("keyid1", plDataSets.getValue(i, "keyid1"));
                    filter.put("keyid2", plDataSets.getValue(i, "keyid2"));
                    filter.put("keyid3", plDataSets.getValue(i, "keyid3"));
                    filter.put("paramlistid", plDataSets.getValue(i, "paramlistid"));
                    filter.put("paramlistversionid", plDataSets.getValue(i, "paramlistversionid"));
                    filter.put("variantid", plDataSets.getValue(i, "variantid"));
                    filter.put("dataset", new BigDecimal(plDataSets.getValue(i, "dataset")));
                    DataSet modifiedDataSets = modifiedDataSetStatus.getFilteredDataSet(filter);
                    if (modifiedDataSets.getRowCount() <= 0) continue;
                    for (int pl = 0; pl < modifiedDataSets.getRowCount(); ++pl) {
                        String prevDatasetStatus = this.getOldSDIDataValue(sdcProcessor, sdcid, beforeEditImage, modifiedDataSets, pl, "s_datasetstatus");
                        String updatedDatasetStatus = modifiedDataSets.getValue(pl, "s_datasetstatus");
                        if (!"Initial".equalsIgnoreCase(prevDatasetStatus) || updatedDatasetStatus.length() <= 0 || "Cancelled".equalsIgnoreCase(updatedDatasetStatus)) continue;
                        plDataSets.setString(i, "__sourceitem", "Y");
                        return true;
                    }
                    continue;
                }
                String dsStatus = plDataSets.getValue(i, "s_datasetstatus");
                if (dsStatus.length() <= 0 || "Initial".equalsIgnoreCase(dsStatus) || "Cancelled".equalsIgnoreCase(dsStatus)) continue;
                plDataSets.setString(i, "__sourceitem", "Y");
                return true;
            }
            boolean isInWorksheet = isSDIWIInWorksheet;
            if (modifiedDataSetStatus != null && modifiedDataSetStatus.getRowCount() > 0 && "Completed".equalsIgnoreCase(datasetStatus) && !isInWorksheet) {
                isInWorksheet = this.isDataSetInWorksheet(modifiedDataSetStatus, plDataSets, i);
            }
            if ("Completed".equalsIgnoreCase(datasetStatus) && isInWorksheet) {
                if (datasetStatus.equalsIgnoreCase(plDataSets.getValue(i, "s_datasetstatus")) || "Released".equalsIgnoreCase(plDataSets.getValue(i, "s_datasetstatus"))) {
                    conditionMet = true;
                    continue;
                }
                return false;
            }
            if (datasetStatus.equalsIgnoreCase(plDataSets.getValue(i, "s_datasetstatus"))) {
                conditionMet = true;
                continue;
            }
            return false;
        }
        return conditionMet;
    }

    private boolean isWorkItemStatusMet(DataSet sdiwis, String status, SDCProcessor sdcProcessor, String sdcid, SDIData beforeEditImage, DataSet modifiedWorkItemStatus) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        boolean conditionMet = false;
        for (int i = 0; i < sdiwis.getRowCount(); ++i) {
            if ("Started".equalsIgnoreCase(status)) {
                if (beforeEditImage != null) {
                    filter.clear();
                    filter.put("sdcid", sdiwis.getValue(i, "sdcid"));
                    filter.put("keyid1", sdiwis.getValue(i, "keyid1"));
                    filter.put("keyid2", sdiwis.getValue(i, "keyid2"));
                    filter.put("keyid3", sdiwis.getValue(i, "keyid3"));
                    filter.put("workitemid", sdiwis.getValue(i, "workitemid"));
                    filter.put("workiteminstance", new BigDecimal(sdiwis.getValue(i, "workiteminstance")));
                    DataSet modifiedSDIWorkItems = modifiedWorkItemStatus.getFilteredDataSet(filter);
                    if (modifiedSDIWorkItems.getRowCount() <= 0) continue;
                    for (int w = 0; w < modifiedSDIWorkItems.getRowCount(); ++w) {
                        String prevWorkItemStatus = this.getOldSDIWorkItemValue(sdcProcessor, sdcid, beforeEditImage, modifiedSDIWorkItems, w, "workitemstatus");
                        String updatedWorkItemStatus = modifiedSDIWorkItems.getValue(w, "workitemstatus");
                        if (!"Initial".equalsIgnoreCase(prevWorkItemStatus) || updatedWorkItemStatus.length() <= 0 || "Cancelled".equalsIgnoreCase(updatedWorkItemStatus)) continue;
                        sdiwis.setString(i, "__sourceitem", "Y");
                        return true;
                    }
                    continue;
                }
                String wiStatus = sdiwis.getValue(i, "workitemstatus");
                if (wiStatus.length() <= 0 || "Initial".equalsIgnoreCase(wiStatus) || "Cancelled".equalsIgnoreCase(wiStatus)) continue;
                sdiwis.setString(i, "__sourceitem", "Y");
                return true;
            }
            if (status.equalsIgnoreCase(sdiwis.getValue(i, "workitemstatus"))) {
                conditionMet = true;
                continue;
            }
            return false;
        }
        return conditionMet;
    }

    private void executeWorkItemAction(SDI sdi, DataSet sourceSDIWI, DataSet dsCurrentSDIWorkItemItem, String workitemStatus, String ruleType, String durationType, String duration, String durationTimeUnit, String relativeTo, String relativeToColumn, HashMap tokenMap, Boolean allMode, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        TranslationProcessor tp = new TranslationProcessor(connectionInfo.getConnectionId());
        boolean immediately = "immediately".equalsIgnoreCase(durationType);
        if (!(durationType == null || durationType.length() <= 0 || immediately || (duration == null || duration.length() <= 0 || durationTimeUnit != null && durationTimeUnit.length() != 0) && (durationTimeUnit == null || durationTimeUnit.length() <= 0 || duration != null && duration.length() != 0))) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("WorkItemItem rule evaluation failed for [keytext]. Duration not specified for [workitemid],[workitemversionid],[workiteminstance] | [itemkeyid1],[itemkeyid2]", tokenMap));
        }
        ActionProcessor actionProcessor = new ActionProcessor(connectionInfo.getConnectionId());
        PropertyList actionProps = new PropertyList();
        String actionId = "";
        String actionVersion = "";
        if ("applywhen".equalsIgnoreCase(ruleType)) {
            actionId = "ApplySDIWorkItem";
            actionVersion = "1";
            actionProps.setProperty("sdcid", dsCurrentSDIWorkItemItem.getString(0, "sdcid"));
            actionProps.setProperty("keyid1", dsCurrentSDIWorkItemItem.getValue(0, "keyid1"));
            actionProps.setProperty("keyid2", dsCurrentSDIWorkItemItem.getValue(0, "keyid2"));
            actionProps.setProperty("keyid3", dsCurrentSDIWorkItemItem.getValue(0, "keyid3"));
            actionProps.setProperty("workitemid", dsCurrentSDIWorkItemItem.getValue(0, "itemkeyid1"));
            actionProps.setProperty("workiteminstance", dsCurrentSDIWorkItemItem.getValue(0, "iteminstance"));
            actionProps.setProperty("__evaluatedworkitemitemidrule", dsCurrentSDIWorkItemItem.getValue(0, "workitemitemid"));
        } else {
            String bbStudyId = sourceSDIWI.getValue(0, "sourcesstudyid");
            DataSet dsStudySDIWorkItem = new DataSet();
            if (bbStudyId.length() > 0) {
                database.createPreparedResultSet("getstudysdiworkitem", "select embedchildsampleplanid,embedchildsampleplanversionid, s_assigneddepartment from sdiworkitem where sdcid = 'Study' and keyid1 = ? and workitemid = ? and workitemversionid = ?", new String[]{bbStudyId, dsCurrentSDIWorkItemItem.getValue(0, "itemkeyid1"), dsCurrentSDIWorkItemItem.getValue(0, "itemkeyid2")});
                dsStudySDIWorkItem = new DataSet(database.getResultSet("getstudysdiworkitem"));
            }
            actionId = "AddSDIWorkItem";
            actionVersion = "1";
            actionProps.setProperty("sdcid", dsCurrentSDIWorkItemItem.getString(0, "sdcid"));
            actionProps.setProperty("keyid1", dsCurrentSDIWorkItemItem.getValue(0, "keyid1"));
            actionProps.setProperty("keyid2", dsCurrentSDIWorkItemItem.getValue(0, "keyid2"));
            actionProps.setProperty("keyid3", dsCurrentSDIWorkItemItem.getValue(0, "keyid3"));
            actionProps.setProperty("workitemid", dsCurrentSDIWorkItemItem.getValue(0, "itemkeyid1"));
            actionProps.setProperty("workitemversionid", dsCurrentSDIWorkItemItem.getValue(0, "itemkeyid2"));
            actionProps.setProperty("groupid", dsCurrentSDIWorkItemItem.getValue(0, "workitemid"));
            actionProps.setProperty("groupinstance", dsCurrentSDIWorkItemItem.getValue(0, "workiteminstance"));
            if (dsStudySDIWorkItem.getRowCount() > 0) {
                actionProps.setProperty("embedchildsampleplanid", dsStudySDIWorkItem.getValue(0, "embedchildsampleplanid"));
                actionProps.setProperty("embedchildsampleplanversionid", dsStudySDIWorkItem.getValue(0, "embedchildsampleplanversionid"));
                actionProps.setProperty("sourcesstudyid", bbStudyId);
                actionProps.setProperty("s_assigneddepartment", dsStudySDIWorkItem.getValue(0, "s_assigneddepartment"));
            }
            actionProps.setProperty("applyworkitem", "addandapply".equalsIgnoreCase(ruleType) ? "Y" : "N");
            actionProps.setProperty("__evaluatedworkitemitemidrule", dsCurrentSDIWorkItemItem.getValue(0, "workitemitemid"));
        }
        if (!immediately && duration != null && duration.length() > 0 && durationTimeUnit != null && durationTimeUnit.length() > 0) {
            this.resolveTodoListDueDate("WorkItem", sdi, sourceSDIWI, dsCurrentSDIWorkItemItem, workitemStatus, relativeTo, relativeToColumn, duration, durationTimeUnit, actionId, actionVersion, actionProps, actionProcessor, allMode, database, connectionInfo);
        } else {
            actionProcessor.processAction(actionId, actionVersion, actionProps);
        }
    }

    public static void updatePackageWorkItemStatus(DataSet sdiwi, DBAccess database, ActionProcessor ap) throws SapphireException {
        PropertyList props = new PropertyList();
        String sdcId = sdiwi.getValue(0, "sdcid");
        String keyid1 = sdiwi.getValue(0, "keyid1");
        String keyid2 = sdiwi.getValue(0, "keyid2");
        String keyid3 = sdiwi.getValue(0, "keyid3");
        String workitemid = sdiwi.getValue(0, "workitemid");
        String workiteminstance = sdiwi.getValue(0, "workitemversionid");
        database.createPreparedResultSet("getsdiworkitem", "select groupid, groupinstance from sdiworkitem where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? and workiteminstance = ?", new String[]{sdcId, keyid1, keyid2, keyid3, workitemid, workiteminstance});
        DataSet ds = new DataSet(database.getResultSet("getsdiworkitem"));
        String groupid = ds.getValue(0, "groupid");
        if (groupid.length() > 0) {
            props.setProperty("sdcid", sdcId);
            props.setProperty("keyid1", keyid1);
            props.setProperty("keyid2", keyid2);
            props.setProperty("keyid3", keyid3);
            props.setProperty("groupid", groupid);
            props.setProperty("groupinstance", ds.getValue(0, "groupinstance"));
            props.setProperty("workitemid", workitemid);
            props.setProperty("workiteminstance", workiteminstance);
            props.setProperty("syncsdiworkitemgroupstatusonly", "Y");
            ap.processAction("SyncSDIWIStatus", "1", props);
        }
    }

    private void executeParamListAction(SDI sdi, DataSet sourceSdidata, DataSet dsCurrentSDIWorkItemItem, DataSet dsToMakeAvailable, String datasetStatus, String durationType, String duration, String durationTimeUnit, String relativeTo, String relativeToColumn, HashMap tokenMap, Boolean allMode, PropertyListCollection toMakeAvailableDS, PropertyList newQCDataSetProps, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        TranslationProcessor tp = new TranslationProcessor(connectionInfo.getConnectionId());
        boolean immediately = "immediately".equalsIgnoreCase(durationType);
        if (!(durationType == null || durationType.length() <= 0 || immediately || (duration == null || duration.length() <= 0 || durationTimeUnit != null && durationTimeUnit.length() != 0) && (durationTimeUnit == null || durationTimeUnit.length() <= 0 || duration != null && duration.length() != 0))) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("WorkItemItem rule evaluation failed for [keytext]. Duration not specified for [workitemid],[workitemversionid],[workiteminstance] | [itemkeyid1],[itemkeyid2],[itemkeyid3]", tokenMap));
        }
        ActionProcessor actionProcessor = new ActionProcessor(connectionInfo.getConnectionId());
        PropertyList actionProps = new PropertyList();
        String actionId = "";
        String actionVersion = "";
        if (dsToMakeAvailable.getRowCount() > 0) {
            dsToMakeAvailable.setString(-1, "availabilityflag", "Y");
            actionProps.setProperty("sdcid", dsToMakeAvailable.getString(0, "sdcid"));
            actionProps.setProperty("keyid1", dsToMakeAvailable.getColumnValues("keyid1", ";"));
            actionProps.setProperty("keyid2", dsToMakeAvailable.getColumnValues("keyid2", ";"));
            actionProps.setProperty("keyid3", dsToMakeAvailable.getColumnValues("keyid3", ";"));
            actionProps.setProperty("paramlistid", dsToMakeAvailable.getColumnValues("paramlistid", ";"));
            actionProps.setProperty("paramlistversionid", dsToMakeAvailable.getColumnValues("paramlistversionid", ";"));
            actionProps.setProperty("variantid", dsToMakeAvailable.getColumnValues("variantid", ";"));
            actionProps.setProperty("dataset", dsToMakeAvailable.getColumnValues("dataset", ";"));
            actionProps.setProperty("availabilityflag", dsToMakeAvailable.getColumnValues("availabilityflag", ";"));
            actionProps.setProperty("propsmatch", "Y");
            actionId = "EditDataSet";
            actionVersion = "1";
        } else {
            boolean linkDataSetToQCBatch = QCUtil.linkDataSetAddedByReflexRuleToQCBatch(new ConfigurationProcessor(connectionInfo.getConnectionId()));
            int repeatCount = dsCurrentSDIWorkItemItem.getInt(0, "repeatcount");
            StringBuffer itemkeyid1 = new StringBuffer();
            StringBuffer itemkeyid2 = new StringBuffer();
            StringBuffer itemkeyid3 = new StringBuffer();
            StringBuffer addNewOnly = new StringBuffer();
            StringBuffer available = new StringBuffer();
            for (int c = 0; c < repeatCount; ++c) {
                itemkeyid1.append(";").append(dsCurrentSDIWorkItemItem.getValue(0, "itemkeyid1"));
                itemkeyid2.append(";").append(dsCurrentSDIWorkItemItem.getValue(0, "itemkeyid2"));
                itemkeyid3.append(";").append(dsCurrentSDIWorkItemItem.getValue(0, "itemkeyid3"));
                addNewOnly.append(";").append("Y".equalsIgnoreCase(dsCurrentSDIWorkItemItem.getValue(0, "forcenewflag")) ? "N" : "Y");
                available.append(";").append("Y");
            }
            actionProps.setProperty("sdcid", dsCurrentSDIWorkItemItem.getString(0, "sdcid"));
            actionProps.setProperty("keyid1", dsCurrentSDIWorkItemItem.getValue(0, "keyid1"));
            actionProps.setProperty("keyid2", dsCurrentSDIWorkItemItem.getValue(0, "keyid2"));
            actionProps.setProperty("keyid3", dsCurrentSDIWorkItemItem.getValue(0, "keyid3"));
            actionProps.setProperty("paramlistid", itemkeyid1.substring(1));
            actionProps.setProperty("paramlistversionid", itemkeyid2.substring(1));
            actionProps.setProperty("variantid", itemkeyid3.substring(1));
            actionProps.setProperty("addnewonly", addNewOnly.substring(1));
            actionProps.setProperty("available", available.substring(1));
            actionProps.setProperty("sourceworkitemid", dsCurrentSDIWorkItemItem.getString(0, "workitemid"));
            actionProps.setProperty("sourceworkiteminstance", dsCurrentSDIWorkItemItem.getValue(0, "workiteminstance"));
            if (OpalUtil.isNotEmpty(dsCurrentSDIWorkItemItem.getValue(0, "scheduleplanid"))) {
                actionProps.setProperty("scheduleplanid", dsCurrentSDIWorkItemItem.getValue(0, "scheduleplanid"));
            }
            if (OpalUtil.isNotEmpty(dsCurrentSDIWorkItemItem.getValue(0, "scheduleplanitemid"))) {
                actionProps.setProperty("scheduleplanitemid", dsCurrentSDIWorkItemItem.getValue(0, "scheduleplanitemid"));
            }
            if (linkDataSetToQCBatch && sourceSdidata.getValue(0, "s_qcbatchid").length() > 0) {
                actionProps.setProperty("s_qcbatchid", sourceSdidata.getValue(0, "s_qcbatchid"));
                actionProps.setProperty("s_qcbatchitemid", sourceSdidata.getValue(0, "s_qcbatchitemid"));
            }
            actionProps.setProperty("__evaluatedworkitemitemidrule", dsCurrentSDIWorkItemItem.getValue(0, "workitemitemid"));
            actionId = "AddDataSet";
            actionVersion = "1";
        }
        if (!immediately && duration != null && duration.length() > 0 && durationTimeUnit != null && durationTimeUnit.length() > 0) {
            this.resolveTodoListDueDate("ParamList", sdi, sourceSdidata, dsCurrentSDIWorkItemItem, datasetStatus, relativeTo, relativeToColumn, duration, durationTimeUnit, actionId, actionVersion, actionProps, actionProcessor, allMode, database, connectionInfo);
        } else if (actionId.equalsIgnoreCase("EditDataSet")) {
            toMakeAvailableDS.add(actionProps);
        } else {
            actionProps.setProperty("skipqcbatchreagentsync", "Y");
            actionProcessor.processAction(actionId, actionVersion, actionProps);
            this.addToNewQCDataSetProps(actionProps, newQCDataSetProps);
        }
    }

    private void addToNewQCDataSetProps(PropertyList actionProps, PropertyList newQCDataSetProps) {
        String s_qcbatchid = actionProps.getProperty("s_qcbatchid", "");
        if (s_qcbatchid.length() > 0) {
            DataSet newdataset = new DataSet(actionProps.getProperty("newdatasetinstancexml"));
            for (int i = 0; i < newdataset.getRowCount(); ++i) {
                String qcbatchid = s_qcbatchid;
                String keyid1 = newdataset.getString(i, "keyid1", "");
                String paramlistid = newdataset.getString(i, "paramlistid", "");
                String paramlistversionid = newdataset.getString(i, "paramlistversionid", "");
                String variantid = newdataset.getString(i, "variantid", "");
                String dataset = newdataset.getString(i, "dataset", "");
                if (newQCDataSetProps.size() > 0) {
                    qcbatchid = newQCDataSetProps.getProperty("qcbatchid") + ";" + qcbatchid;
                    keyid1 = newQCDataSetProps.getProperty("keyid1") + ";" + keyid1;
                    paramlistid = newQCDataSetProps.getProperty("paramlistid") + ";" + paramlistid;
                    paramlistversionid = newQCDataSetProps.getProperty("paramlistversionid") + ";" + paramlistversionid;
                    variantid = newQCDataSetProps.getProperty("variantid") + ";" + variantid;
                    dataset = newQCDataSetProps.getProperty("dataset") + ";" + dataset;
                }
                newQCDataSetProps.setProperty("qcbatchid", qcbatchid);
                newQCDataSetProps.setProperty("keyid1", keyid1);
                newQCDataSetProps.setProperty("paramlistid", paramlistid);
                newQCDataSetProps.setProperty("paramlistversionid", paramlistversionid);
                newQCDataSetProps.setProperty("variantid", variantid);
                newQCDataSetProps.setProperty("dataset", dataset);
            }
        }
    }

    private void resolveTodoListDueDate(String itemSDC, SDI sdi, DataSet sourceDS, DataSet dsCurrentSDIWorkItemItem, String itemStatus, String relativeTo, String relativeToColumn, String duration, String durationTimeUnit, String actionId, String actionVersion, PropertyList actionProps, ActionProcessor actionProcessor, Boolean allMode, DBAccess database, ConnectionInfo connectionInfo) throws SapphireException {
        if (this.alreadyInTodoList(actionProps, database, actionId)) {
            return;
        }
        if (relativeTo != null && !relativeTo.equalsIgnoreCase("now")) {
            Calendar targetDate = null;
            if ("Sample".equalsIgnoreCase(relativeTo)) {
                database.createPreparedResultSet("getdate", "select " + relativeToColumn + " from s_sample where s_sampleid=?", new String[]{sdi.getKeyid1()});
                DataSet dsDate = new DataSet(database.getResultSet("getdate"));
                targetDate = dsDate.getCalendar(0, relativeToColumn);
            } else if ("Test".equalsIgnoreCase(relativeTo)) {
                if ("WorkItem".equalsIgnoreCase(itemSDC)) {
                    for (int i = 0; i < sourceDS.getRowCount(); ++i) {
                        if ("Started".equalsIgnoreCase(itemStatus) || allMode != null && !allMode.booleanValue()) {
                            String sourceFlag = sourceDS.getValue(i, "__sourceitem", "N");
                            if (!"Y".equalsIgnoreCase(sourceFlag)) continue;
                            targetDate = sourceDS.getCalendar(i, relativeToColumn);
                            break;
                        }
                        Calendar relativeDt = sourceDS.getCalendar(i, relativeToColumn);
                        if (targetDate == null) {
                            targetDate = relativeDt;
                            continue;
                        }
                        int diff = targetDate.compareTo(relativeDt);
                        if (diff >= 0) continue;
                        targetDate = relativeDt;
                    }
                } else {
                    targetDate = dsCurrentSDIWorkItemItem.getCalendar(0, relativeToColumn);
                }
            } else if ("DataSet".equalsIgnoreCase(relativeTo)) {
                for (int i = 0; i < sourceDS.getRowCount(); ++i) {
                    if ("Started".equalsIgnoreCase(itemStatus) || allMode != null && !allMode.booleanValue()) {
                        String sourceFlag = sourceDS.getValue(i, "__sourceitem", "N");
                        if (!"Y".equalsIgnoreCase(sourceFlag)) continue;
                        targetDate = sourceDS.getCalendar(i, relativeToColumn);
                        break;
                    }
                    Calendar relativeDt = sourceDS.getCalendar(i, relativeToColumn);
                    if (targetDate == null) {
                        targetDate = relativeDt;
                        continue;
                    }
                    int diff = targetDate.compareTo(relativeDt);
                    if (diff >= 0) continue;
                    targetDate = relativeDt;
                }
            }
            if (targetDate != null) {
                this.addToDoListEntry(actionId, actionVersion, duration, durationTimeUnit, targetDate, actionProps, actionProcessor, connectionInfo);
            }
        } else {
            Calendar targetDate = DateTimeUtil.getNowCalendar();
            this.addToDoListEntry(actionId, actionVersion, duration, durationTimeUnit, targetDate, actionProps, actionProcessor, connectionInfo);
        }
    }

    private void addToDoListEntry(String actionId, String actionVersion, String duration, String durationTimeUnit, Calendar dueDate, PropertyList actionProps, ActionProcessor actionProcessor, ConnectionInfo connectionInfo) throws SapphireException {
        float frTimeDiff;
        float time = Float.parseFloat(duration);
        if (durationTimeUnit.equalsIgnoreCase(Unit_DAY)) {
            int integerTimePart = Math.round(time * 24.0f);
            dueDate.add(11, integerTimePart);
        } else if (durationTimeUnit.equalsIgnoreCase(Unit_MONTH)) {
            int integerTimePart = (int)time;
            dueDate.add(2, integerTimePart);
            frTimeDiff = time - (float)integerTimePart;
            if ((double)frTimeDiff > 0.0) {
                int fraction = Math.round(frTimeDiff * 30.0f);
                dueDate.add(5, fraction);
            }
        } else if (durationTimeUnit.equalsIgnoreCase(Unit_YEAR)) {
            int integerTimePart = (int)time;
            dueDate.add(1, integerTimePart);
            frTimeDiff = time - (float)integerTimePart;
            if ((double)frTimeDiff > 0.0) {
                int fraction = Math.round(frTimeDiff * 365.0f);
                dueDate.add(5, fraction);
            }
        } else if (durationTimeUnit.equalsIgnoreCase(Unit_HOUR)) {
            int integerTimePart = (int)time;
            dueDate.add(11, integerTimePart);
            frTimeDiff = time - (float)integerTimePart;
            if ((double)frTimeDiff > 0.0) {
                int fraction = Math.round(frTimeDiff * 60.0f);
                dueDate.add(12, fraction);
            }
        } else if (durationTimeUnit.equalsIgnoreCase(Unit_WEEK)) {
            int integerTimePart = (int)time;
            dueDate.add(4, integerTimePart);
            frTimeDiff = time - (float)integerTimePart;
            if ((double)frTimeDiff > 0.0) {
                int fraction = Math.round(frTimeDiff * 7.0f);
                dueDate.add(5, fraction);
            }
        } else if (durationTimeUnit.equalsIgnoreCase(Unit_MINUTES)) {
            int integerTimePart = (int)time;
            dueDate.add(12, integerTimePart);
            frTimeDiff = time - (float)integerTimePart;
            if ((double)frTimeDiff > 0.0) {
                int fraction = Math.round(frTimeDiff * 60.0f);
                dueDate.add(13, fraction);
            }
        }
        M18NUtil m18n = new M18NUtil(connectionInfo);
        actionProps.setProperty("actionid", actionId);
        actionProps.setProperty("actionversionid", actionVersion);
        actionProps.setProperty("duedate", m18n.format(dueDate));
        actionProps.setProperty("delete", "Y");
        actionProcessor.processAction("AddToDoListEntry", "1", actionProps);
    }

    private boolean alreadyInTodoList(PropertyList actionProps, DBAccess database, String actionId) throws SapphireException {
        StringBuffer sql = new StringBuffer("SELECT todolistid, actionid, propertyclob ").append(" FROM  todolist WHERE statusflag in( 'W', 'S' ) AND actionid = ? AND ").append(database.isSqlServer() ? "CHARINDEX(?, propertyclob, 1 )" : "INSTR( propertyclob, ?, 1)").append(">0");
        String keyid1 = actionProps.getProperty("keyid1");
        String search_keyid1 = "keyid1=" + keyid1;
        database.createPreparedResultSet("checkdefercreation", sql.toString(), new String[]{actionId, search_keyid1});
        DataSet ds = new DataSet();
        ds.setResultSet(database.getResultSet("checkdefercreation"), true, database.isOracle() ? "ORA" : "MSS");
        if (ds.getRowCount() == 0) {
            return false;
        }
        boolean exists = false;
        String[] propertyIds = new String[]{};
        if ("AddDataSet".equals(actionId)) {
            propertyIds = new String[]{"sdcid", "keyid2", "keyid3", "__evaluatedworkitemitemidrule", "sourceworkitemid", "sourceworkiteminstance", "paramlistid", "paramlistversionid", "variantid"};
        } else if ("EditDataSet".equals(actionId)) {
            propertyIds = new String[]{"sdcid", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "availabilityflag"};
        } else if ("AddSDIWorkItem".equals(actionId)) {
            propertyIds = new String[]{"sdcid", "keyid2", "keyid3", "__evaluatedworkitemitemidrule", "workitemid", "workitemversionid", "groupid", "groupinstance", "applyworkitem"};
        } else if ("ApplySDIWorkItem".equals(actionId)) {
            propertyIds = new String[]{"sdcid", "keyid2", "keyid3", "__evaluatedworkitemitemidrule", "workitemid", "workitemversionid"};
        }
        if (propertyIds.length > 0) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                PropertyList propertyList = new PropertyList();
                PropertyListUtil.setDelimiteredProps(propertyList, ds.getClob(i, "propertyclob"));
                if (!this.actionPropertiesMatched(propertyIds, actionProps, propertyList)) continue;
                return true;
            }
        }
        return exists;
    }

    private boolean actionPropertiesMatched(String[] propertyIds, PropertyList actionProps, PropertyList propertyList) {
        for (int p = 0; p < propertyIds.length; ++p) {
            if (actionProps.getProperty(propertyIds[p]).equals(propertyList.getProperty(propertyIds[p]))) continue;
            return false;
        }
        return true;
    }

    public static void updateSDIWorkItemItem(String datasetXml, String workitemId, String workitemInstance, String evaluatedWorkitemitemid, DBAccess database, ActionProcessor ap, ConnectionInfo connectionInfo, SDCProcessor sdcProcessor, Logger logger) throws SapphireException {
        DataSet dsInstances = new DataSet(datasetXml);
        int indexOfDot = evaluatedWorkitemitemid.indexOf(".");
        String workitemitemid = evaluatedWorkitemitemid.substring(0, indexOfDot);
        StringBuffer sql = new StringBuffer();
        sql.append("select wii.* from sdiworkitemitem wii").append(" where wii.sdcid = ? and wii.keyid1 = ? and wii.keyid2 = ? and wii.keyid3 = ? and wii.itemsdcid = 'ParamList'").append(" and wii.itemkeyid1 = ? and wii.itemkeyid2 = ? and wii.itemkeyid3 = ? and wii.workitemid = ? and wii.workiteminstance = ?");
        if (database.isOracle()) {
            sql.append(" and substr( wii.workitemitemid, 1, instr( wii.workitemitemid, '.', 1 ) - 1 ) = '" + workitemitemid + "'");
        } else {
            sql.append(" and substring( wii.workitemitemid, 1, charindex( '.', wii.workitemitemid, 1 ) - 1 ) = '" + workitemitemid + "'");
        }
        sql.append(" and wii.iteminstance is null order by wii.workitemitemid");
        PreparedStatement selectSDIWII = database.prepareStatement("selectsdiwii", sql.toString());
        String sdcId = dsInstances.getValue(0, "sdcid");
        String keyid1 = dsInstances.getValue(0, "keyid1");
        String keyid2 = dsInstances.getValue(0, "keyid2");
        String keyid3 = dsInstances.getValue(0, "keyid3");
        String paramlistid = dsInstances.getValue(0, "paramlistid");
        String paramlistversionid = dsInstances.getValue(0, "paramlistversionid");
        String variantid = dsInstances.getValue(0, "variantid");
        String dataset = dsInstances.getValue(0, "dataset");
        try {
            selectSDIWII.setString(1, sdcId);
            selectSDIWII.setString(2, keyid1);
            selectSDIWII.setString(3, keyid2);
            selectSDIWII.setString(4, keyid3);
            selectSDIWII.setString(5, paramlistid);
            selectSDIWII.setString(6, paramlistversionid);
            selectSDIWII.setString(7, variantid);
            selectSDIWII.setString(8, workitemId);
            selectSDIWII.setString(9, workitemInstance);
            DataSet dsSDIWII = new DataSet(selectSDIWII.executeQuery());
            if (dsSDIWII.getRowCount() > 0) {
                dsInstances.sort("dataset");
                DataSet dsUpdateSDIWII = new DataSet();
                dsUpdateSDIWII.addColumnValues("sdcid", 0, dsSDIWII.getColumnValues("sdcid", ";"), ";");
                dsUpdateSDIWII.addColumnValues("keyid1", 0, dsSDIWII.getColumnValues("keyid1", ";"), ";");
                dsUpdateSDIWII.addColumnValues("keyid2", 0, dsSDIWII.getColumnValues("keyid2", ";"), ";");
                dsUpdateSDIWII.addColumnValues("keyid3", 0, dsSDIWII.getColumnValues("keyid3", ";"), ";");
                dsUpdateSDIWII.addColumnValues("workitemid", 0, dsSDIWII.getColumnValues("workitemid", ";"), ";");
                dsUpdateSDIWII.addColumnValues("workiteminstance", 1, dsSDIWII.getColumnValues("workiteminstance", ";"), ";");
                dsUpdateSDIWII.addColumnValues("workitemitemid", 0, dsSDIWII.getColumnValues("workitemitemid", ";"), ";");
                dsUpdateSDIWII.addColumnValues("iteminstance", 1, dsInstances.getColumnValues("dataset", ";"), ";");
                String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "workitemid", "workiteminstance", "workitemitemid"};
                DataSetUtil.update(database, dsUpdateSDIWII, "sdiworkitemitem", keycols);
                dsSDIWII.addColumnValues("iteminstance", 1, dsInstances.getColumnValues("dataset", ";"), ";");
                WorkItemUtil.addWorkItemDataSetAttributeAndRelations(dsSDIWII, database, ap, workitemitemid, connectionInfo, sdcProcessor, logger);
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", sdcId);
                props.setProperty("keyid1", keyid1);
                ap.processAction("SyncSDIDataSetStatus", "1", props);
                props.clear();
                props.setProperty("sdcid", sdcId);
                props.setProperty("keyid1", keyid1);
                props.setProperty("keyid2", keyid2);
                props.setProperty("keyid3", keyid3);
                ap.processAction("SyncSDIWIStatus", "1", props);
            }
        }
        catch (Exception e) {
            throw new SapphireException("On evaluation of workitemitem rule, failed updating iteminstance in sdiworkitemitem." + e.getMessage());
        }
        finally {
            database.closeStatement("selectsdiwii");
        }
    }

    public static void updateSDIDataUserSequence(String datasetXml, String workitemId, String workitemInstance, DBAccess database) throws SapphireException {
        DataSet dsInstances = new DataSet(datasetXml);
        StringBuffer allSDIWISQL = new StringBuffer("select wii.* from sdiworkitemitem wii");
        allSDIWISQL.append(" where wii.sdcid = ? and wii.keyid1 = ? and wii.keyid2 = ? and wii.keyid3 = ? and wii.itemsdcid = 'ParamList' and wii.workitemid = ? and wii.workiteminstance = ? and  iteminstance is not null order by usersequence");
        PreparedStatement selectAllSDIWII = database.prepareStatement("selectallsdiwii", allSDIWISQL.toString());
        String sdcId = dsInstances.getValue(0, "sdcid");
        String keyid1 = dsInstances.getValue(0, "keyid1");
        String keyid2 = dsInstances.getValue(0, "keyid2");
        String keyid3 = dsInstances.getValue(0, "keyid3");
        try {
            selectAllSDIWII.setString(1, sdcId);
            selectAllSDIWII.setString(2, keyid1);
            selectAllSDIWII.setString(3, keyid2);
            selectAllSDIWII.setString(4, keyid3);
            selectAllSDIWII.setString(5, workitemId);
            selectAllSDIWII.setString(6, workitemInstance);
            DataSet dsAllSDIWII = new DataSet(selectAllSDIWII.executeQuery());
            DataSet dsUpdatesequence = new DataSet();
            if (dsAllSDIWII.getRowCount() > 1) {
                StringBuffer allds = new StringBuffer("select * from sdidata");
                allds.append(" where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ?  order by usersequence");
                PreparedStatement selectAlldataset = database.prepareStatement("selectalldataset", allds.toString() + " ");
                selectAlldataset.setString(1, sdcId);
                selectAlldataset.setString(2, keyid1);
                selectAlldataset.setString(3, keyid2);
                selectAlldataset.setString(4, keyid3);
                DataSet dsAll = new DataSet(selectAlldataset.executeQuery());
                if (dsAll.getRowCount() > 1) {
                    block5: for (int wii = 0; wii < dsAllSDIWII.getRowCount(); ++wii) {
                        String itemkeyid1 = dsAllSDIWII.getValue(wii, "itemkeyid1");
                        String itemkeyid2 = dsAllSDIWII.getValue(wii, "itemkeyid2");
                        String itemkeyid3 = dsAllSDIWII.getValue(wii, "itemkeyid3");
                        String iteminstance = dsAllSDIWII.getValue(wii, "iteminstance");
                        HashMap<String, String> findDSInstance = new HashMap<String, String>();
                        findDSInstance.put("paramlistid", itemkeyid1);
                        findDSInstance.put("paramlistversionid", itemkeyid2);
                        findDSInstance.put("variantid", itemkeyid3);
                        findDSInstance.put("dataset", iteminstance);
                        if (dsInstances.findRow(findDSInstance) <= -1) continue;
                        HashMap<String, Object> filter = new HashMap<String, Object>();
                        filter.put("sourceworkitemid", workitemId);
                        filter.put("sourceworkiteminstance", new BigDecimal(workitemInstance));
                        DataSet dscurrentWI = dsAll.getFilteredDataSet(filter);
                        for (int ds = wii; ds < dscurrentWI.getRowCount(); ++ds) {
                            String plid = dscurrentWI.getValue(ds, "paramlistid");
                            String plverid = dscurrentWI.getValue(ds, "paramlistversionid");
                            String varid = dscurrentWI.getValue(ds, "variantid");
                            String usersequence = dscurrentWI.getValue(ds, "usersequence");
                            String sdidataid = dscurrentWI.getValue(ds, "sdidataid");
                            if (itemkeyid1.equals(plid) && itemkeyid2.equals(plverid) && itemkeyid3.equals(varid)) continue block5;
                            int r = ds;
                            while (r < dscurrentWI.getRowCount() - 1) {
                                plid = dscurrentWI.getValue(++r, "paramlistid");
                                plverid = dscurrentWI.getValue(r, "paramlistversionid");
                                varid = dscurrentWI.getValue(r, "variantid");
                                if (!itemkeyid1.equals(plid) || !itemkeyid2.equals(plverid) || !itemkeyid3.equals(varid)) continue;
                                dscurrentWI.setValue(r, "usersequence", usersequence);
                                BigDecimal newUserSequence = new BigDecimal(usersequence).add(new BigDecimal(1));
                                dscurrentWI.setNumber(ds, "usersequence", newUserSequence);
                                dsUpdatesequence.copyRow(dscurrentWI, r, 1);
                                dsUpdatesequence.copyRow(dscurrentWI, ds, 1);
                                dsAll.sort("usersequence");
                                ds = dsAll.findRow("sdidataid", sdidataid);
                                ++ds;
                                while (ds < dsAll.getRowCount()) {
                                    newUserSequence = newUserSequence.add(new BigDecimal(1));
                                    dsAll.setNumber(ds, "usersequence", newUserSequence);
                                    dsUpdatesequence.copyRow(dsAll, ds, 1);
                                    ++ds;
                                }
                                continue block5;
                            }
                        }
                    }
                    if (dsUpdatesequence.getRowCount() > 0) {
                        DataSet dsupdate = new DataSet();
                        dsupdate.addColumnValues("sdcid", 0, dsUpdatesequence.getColumnValues("sdcid", ";"), ";");
                        dsupdate.addColumnValues("keyid1", 0, dsUpdatesequence.getColumnValues("keyid1", ";"), ";");
                        dsupdate.addColumnValues("keyid2", 0, dsUpdatesequence.getColumnValues("keyid2", ";"), ";");
                        dsupdate.addColumnValues("keyid3", 0, dsUpdatesequence.getColumnValues("keyid3", ";"), ";");
                        dsupdate.addColumnValues("paramlistid", 0, dsUpdatesequence.getColumnValues("paramlistid", ";"), ";");
                        dsupdate.addColumnValues("paramlistversionid", 0, dsUpdatesequence.getColumnValues("paramlistversionid", ";"), ";");
                        dsupdate.addColumnValues("variantid", 0, dsUpdatesequence.getColumnValues("variantid", ";"), ";");
                        dsupdate.addColumnValues("dataset", 1, dsUpdatesequence.getColumnValues("dataset", ";"), ";");
                        dsupdate.addColumnValues("usersequence", 1, dsUpdatesequence.getColumnValues("usersequence", ";"), ";");
                        DataSetUtil.update(database, dsupdate, "sdidata", new SDIData().getKeys("dataset"));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("On evaluation of workitemitem rule, failed updating usersequence in sdidata." + e.getMessage());
        }
        finally {
            database.closeStatement("selectallsdiwii");
            database.closeStatement("selectalldataset");
        }
    }

    public static void updatePkgWorkItemSDIWorkItemItem(String sdiworkitemXml, String evaluatedWorkitemitemid, DBAccess database, ActionProcessor ap) throws SapphireException {
        DataSet wiInstances = new DataSet(sdiworkitemXml);
        StringBuffer sql = new StringBuffer();
        sql.append("select sdcid, keyid1, keyid2, keyid3, workitemid, workiteminstance, workitemitemid, iteminstance from sdiworkitemitem  ").append(" where sdcid = ? and keyid1 = ? and keyid2 = ? and keyid3 = ? and workitemid = ? and workiteminstance = ? and workitemitemid = ? and itemsdcid = ? and itemkeyid1 = ? and itemkeyid2 = ? and iteminstance is null");
        PreparedStatement selectSDIWII = database.prepareStatement("selectsdiwii", sql.toString());
        try {
            String sdcId = wiInstances.getValue(0, "sdcid");
            String keyid1 = wiInstances.getValue(0, "keyid1");
            String keyid2 = wiInstances.getValue(0, "keyid2");
            String keyid3 = wiInstances.getValue(0, "keyid3");
            String workitemid = wiInstances.getValue(0, "groupid");
            String workiteminstance = wiInstances.getValue(0, "groupinstance");
            String itemSdcId = "WorkItem";
            String itemKeyid1 = wiInstances.getValue(0, "workitemid");
            String itemKeyid2 = wiInstances.getValue(0, "workitemversionid");
            selectSDIWII.setString(1, sdcId);
            selectSDIWII.setString(2, keyid1);
            selectSDIWII.setString(3, keyid2);
            selectSDIWII.setString(4, keyid3);
            selectSDIWII.setString(5, workitemid);
            selectSDIWII.setString(6, workiteminstance);
            selectSDIWII.setString(7, evaluatedWorkitemitemid);
            selectSDIWII.setString(8, itemSdcId);
            selectSDIWII.setString(9, itemKeyid1);
            selectSDIWII.setString(10, itemKeyid2);
            DataSet dsSDIWII = new DataSet(selectSDIWII.executeQuery());
            if (dsSDIWII.getRowCount() > 0) {
                dsSDIWII.setValue(0, "iteminstance", wiInstances.getValue(0, "workiteminstance"));
                String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "workitemid", "workiteminstance", "workitemitemid"};
                DataSetUtil.update(database, dsSDIWII, "sdiworkitemitem", keycols);
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", sdcId);
                props.setProperty("keyid1", keyid1);
                ap.processAction("SyncSDIDataSetStatus", "1", props);
                props.clear();
                props.setProperty("sdcid", sdcId);
                props.setProperty("keyid1", keyid1);
                props.setProperty("keyid2", keyid2);
                props.setProperty("keyid3", keyid3);
                props.setProperty("groupid", workitemid);
                props.setProperty("groupinstance", workiteminstance);
                props.setProperty("workitemid", itemKeyid1);
                props.setProperty("workiteminstance", itemKeyid2);
                props.setProperty("syncsdiworkitemgroupstatusonly", "Y");
                ap.processAction("SyncSDIWIStatus", "1", props);
            }
        }
        catch (Exception e) {
            throw new SapphireException("On evaluation of workitemitem rule, failed updating iteminstance in sdiworkitemitem of a package workitem." + e.getMessage());
        }
        finally {
            database.closeStatement("selectsdiwii");
        }
    }

    public static void evaluateSDIIncludeAvailabilityRule(DataSet dsWiiDef, SDI sdi, DataSet paramLists, DBAccess database, ConnectionInfo connectionInfo, boolean prepExists) throws SapphireException {
        int p;
        HashMap<String, String> workItemItemMap = new HashMap<String, String>();
        DataSet dsSDI = new DataSet();
        boolean preparationDataSetAdded = false;
        for (p = 0; p < paramLists.getRowCount(); ++p) {
            String workitemitemid = paramLists.getValue(p, "workitemitemid");
            String paramlistType = paramLists.getValue(p, "__paramlisttype");
            boolean dsIncluded = false;
            if (workItemItemMap.containsKey(workitemitemid)) {
                String flags = (String)workItemItemMap.get(workitemitemid);
                String include = StringUtil.split(flags, ";")[0];
                paramLists.setString(p, "__include", include);
                dsIncluded = "Y".equalsIgnoreCase(include);
                paramLists.setString(p, "available", StringUtil.split(flags, ";")[1]);
                paramLists.setString(p, "__includesdiwii", StringUtil.split(flags, ";")[2]);
                paramLists.setString(p, "__defaultAvalabilityRule", StringUtil.split(flags, ";")[3]);
            } else {
                int f = dsWiiDef.findRow("workitemitemid", workitemitemid);
                String rule = dsWiiDef.getValue(f, "workitemitemrule");
                HashMap flags = WorkItemItemRuleEvaluator.evaluateIncludeAvaillabilityRule(rule, sdi, database, connectionInfo, paramlistType, prepExists, dsSDI);
                String include = (String)flags.get("include");
                paramLists.setString(p, "__include", include);
                dsIncluded = "Y".equalsIgnoreCase(include);
                paramLists.setString(p, "available", (String)flags.get("availabilityflag"));
                paramLists.setString(p, "__includesdiwii", (String)flags.get("includesdiwii"));
                paramLists.setString(p, "__defaultAvalabilityRule", (String)flags.get("defaultAvalabilityRule"));
                workItemItemMap.put(workitemitemid, flags.get("include") + ";" + flags.get("availabilityflag") + ";" + flags.get("includesdiwii") + ";" + flags.get("defaultAvalabilityRule"));
            }
            if (!"Preparation".equalsIgnoreCase(paramlistType) || !dsIncluded) continue;
            preparationDataSetAdded = true;
        }
        if (!preparationDataSetAdded) {
            for (p = 0; p < paramLists.getRowCount(); ++p) {
                if (!"Y".equals(paramLists.getValue(p, "__defaultAvalabilityRule"))) continue;
                paramLists.setString(p, "available", "Y");
            }
        }
    }

    private static HashMap evaluateIncludeAvaillabilityRule(String rule, SDI sdi, DBAccess database, ConnectionInfo connectionInfo, String paramlistType, boolean prepExists, DataSet dsSDI) throws SapphireException {
        boolean defaultAvalabilityRule;
        boolean includesdiwii;
        boolean available;
        boolean include;
        block20: {
            include = false;
            available = false;
            includesdiwii = true;
            defaultAvalabilityRule = false;
            if (rule.length() == 0 || rule.trim().equals("{}")) {
                include = true;
                available = "Preparation".equalsIgnoreCase(paramlistType) ? true : ("Procedural".equalsIgnoreCase(paramlistType) ? !prepExists : true);
                defaultAvalabilityRule = true;
            } else {
                try {
                    JSONObject json = new JSONObject(rule);
                    JSONArray includeRuleRows = json.getJSONArray("includerule");
                    JSONObject includeRuleRow = (JSONObject)includeRuleRows.get(0);
                    JSONArray reflexRuleRows = json.getJSONArray("reflexrule");
                    JSONObject reflexRuleRow = (JSONObject)reflexRuleRows.get(0);
                    String ruleType = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "reflexruletype");
                    boolean addRule = "add".equalsIgnoreCase(ruleType);
                    if (!"add".equalsIgnoreCase(ruleType) && !"available".equalsIgnoreCase(ruleType)) break block20;
                    boolean initially = "Initially".equalsIgnoreCase(WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "timepoint"));
                    boolean always = "includealways".equalsIgnoreCase(WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "includetimepoint"));
                    boolean includewhenitem = "includewhenitem".equalsIgnoreCase(WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "includetimepoint"));
                    if (always) {
                        if (addRule) {
                            boolean bl = include = initially;
                            available = "Preparation".equalsIgnoreCase(paramlistType) ? true : ("Procedural".equalsIgnoreCase(paramlistType) ? !prepExists : true);
                            defaultAvalabilityRule = true;
                        } else {
                            available = initially;
                            include = true;
                        }
                        break block20;
                    }
                    String columnId = WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "columnid");
                    String columnValue = WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "columnvalue");
                    String columnOperator = WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "columnoperator");
                    String connectionId = connectionInfo.getConnectionId();
                    if (columnId.length() > 0 && columnValue.length() > 0 && columnOperator.length() > 0) {
                        boolean inOperator = "in".equalsIgnoreCase(columnOperator);
                        if (dsSDI.getRowCount() == 0) {
                            dsSDI = WorkItemItemRuleEvaluator.getSDIColumnValues(sdi, connectionId, database);
                        }
                        if (dsSDI.getRowCount() == 1) {
                            boolean columnConditionMatches;
                            columnValue = StringUtil.replaceAll(columnValue, "#semicolon#", ";");
                            String[] colValues = StringUtil.split(columnValue, ";");
                            List<String> columnValueList = Arrays.asList(colValues);
                            boolean bl = inOperator ? columnValueList.contains(dsSDI.getValue(0, columnId)) : (columnConditionMatches = !columnValueList.contains(dsSDI.getValue(0, columnId)));
                            if (addRule) {
                                if (columnConditionMatches) {
                                    include = initially;
                                } else {
                                    include = false;
                                    includesdiwii = false;
                                }
                                available = "Preparation".equalsIgnoreCase(paramlistType) ? true : ("Procedural".equalsIgnoreCase(paramlistType) ? !prepExists : true);
                                defaultAvalabilityRule = true;
                            } else {
                                include = true;
                                if (columnConditionMatches) {
                                    available = initially;
                                } else if (includewhenitem) {
                                    include = false;
                                    includesdiwii = false;
                                } else if (initially) {
                                    available = false;
                                } else {
                                    available = "Preparation".equalsIgnoreCase(paramlistType) ? true : ("Procedural".equalsIgnoreCase(paramlistType) ? !prepExists : true);
                                    defaultAvalabilityRule = true;
                                }
                            }
                        }
                        database.closeResultSet("getSDIColumns");
                        break block20;
                    }
                    TranslationProcessor tp = new TranslationProcessor(connectionId);
                    HashMap<String, String> tokenMap = new HashMap<String, String>();
                    tokenMap.put("keytext", sdi.getKeyText());
                    Logger.logError(tp.translate("WorkItemItem rule evaluation failed for [keytext]. DataSet cannot be added/made available as column id or column value or column operator is missing in the rule!", tokenMap));
                }
                catch (JSONException e) {
                    throw new SapphireException(e);
                }
            }
        }
        HashMap<String, String> returnValues = new HashMap<String, String>();
        returnValues.put("defaultAvalabilityRule", defaultAvalabilityRule ? "Y" : "N");
        returnValues.put("include", include ? "Y" : "N");
        returnValues.put("availabilityflag", available ? "Y" : "N");
        returnValues.put("includesdiwii", includesdiwii ? "Y" : "N");
        return returnValues;
    }

    public static String evaluateAddAndApplyWorkItemRule(String rule, SDI sdi, DBAccess database, ConnectionInfo connectionInfo, DataSet dsSDI) throws SapphireException {
        String addApplyFlag = "";
        try {
            JSONObject json = new JSONObject(rule);
            boolean always = true;
            String columnId = "";
            String columnValue = "";
            String columnOperator = "";
            if (json.has("includerule")) {
                JSONArray includeRuleRows = json.getJSONArray("includerule");
                JSONObject includeRuleRow = (JSONObject)includeRuleRows.get(0);
                always = "includealways".equalsIgnoreCase(WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "includetimepoint"));
                columnId = WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "columnid");
                columnValue = WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "columnvalue");
                columnOperator = WorkItemItemRuleEvaluator.getRuleItemValue(includeRuleRow, "columnoperator");
            }
            JSONArray reflexRuleRows = json.getJSONArray("reflexrule");
            JSONObject reflexRuleRow = (JSONObject)reflexRuleRows.get(0);
            String ruleType = WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "reflexruletype");
            boolean addApply = "addandapply".equalsIgnoreCase(ruleType);
            boolean addwithoutApply = "addwithoutapplying".equalsIgnoreCase(ruleType);
            boolean applyWhen = "applywhen".equalsIgnoreCase(ruleType);
            boolean initially = "Initially".equalsIgnoreCase(WorkItemItemRuleEvaluator.getRuleItemValue(reflexRuleRow, "timepoint"));
            String connectionId = connectionInfo.getConnectionId();
            SDCProcessor sdcProcessor = new SDCProcessor(connectionId);
            if (always) {
                addApplyFlag = reflexRuleRow.has("timepoint") ? (initially ? (addApply ? "AA" : "AO") : "DAN") : (addApply ? "AA" : "AO");
            } else if (columnId.length() > 0 && columnValue.length() > 0 && columnOperator.length() > 0) {
                boolean columnConditionMatches;
                int sdiRow;
                String sdcId = sdi.getSdcid();
                boolean inOperator = "in".equalsIgnoreCase(columnOperator);
                String keycolid1 = sdcProcessor.getProperty(sdcId, "keycolid1");
                String keycolid2 = sdcProcessor.getProperty(sdcId, "keycolid2");
                String keycolid3 = sdcProcessor.getProperty(sdcId, "keycolid3");
                HashMap<String, String> findSDI = new HashMap<String, String>();
                findSDI.put(keycolid1, sdi.getKeyid1());
                if (keycolid2.length() > 0) {
                    findSDI.put(keycolid2, sdi.getKeyid2());
                }
                if (keycolid3.length() > 0) {
                    findSDI.put(keycolid3, sdi.getKeyid3());
                }
                if ((sdiRow = dsSDI.findRow(findSDI)) < 0) {
                    dsSDI.copyRow(WorkItemItemRuleEvaluator.getSDIColumnValues(sdi, connectionId, database), -1, 1);
                    sdiRow = dsSDI.getRowCount() - 1;
                }
                columnValue = StringUtil.replaceAll(columnValue, "#semicolon#", ";");
                String[] colValues = StringUtil.split(columnValue, ";");
                List<String> columnValueList = Arrays.asList(colValues);
                boolean bl = inOperator ? columnValueList.contains(dsSDI.getValue(sdiRow, columnId)) : (columnConditionMatches = !columnValueList.contains(dsSDI.getValue(sdiRow, columnId)));
                addApplyFlag = columnConditionMatches ? (reflexRuleRow.has("timepoint") ? (initially ? (addApply ? "AA" : "AO") : "DAN") : (addApply ? "AA" : "AO")) : "NA";
            } else {
                TranslationProcessor tp = new TranslationProcessor(connectionId);
                HashMap<String, String> tokenMap = new HashMap<String, String>();
                tokenMap.put("keytext", sdi.getKeyText());
                Logger.logError(tp.translate("WorkItemItem rule evaluation failed for [keytext]. WorkItem cannot be added/applied as column id or column value or column operator is missing in the rule!", tokenMap));
            }
        }
        catch (JSONException e) {
            throw new SapphireException(e);
        }
        return addApplyFlag;
    }

    private static DataSet getSDIColumnValues(SDI sdi, String connectionId, DBAccess database) throws SapphireException {
        SDCProcessor sdcProcessor = new SDCProcessor(connectionId);
        String sdcId = sdi.getSdcid();
        String keyid1 = sdi.getKeyid1();
        String keyid2 = sdi.getKeyid2();
        String keyid3 = sdi.getKeyid3();
        String tableid = sdcProcessor.getProperty(sdcId, "tableid");
        String keycolid1 = sdcProcessor.getProperty(sdcId, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcId, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcId, "keycolid3");
        ArrayList<String> sqlParams = new ArrayList<String>();
        String sql = "select * from " + tableid + " t  where " + keycolid1 + " = ?";
        sqlParams.add(keyid1);
        if (keycolid2.length() > 0) {
            sql = sql + " and t." + keycolid2 + " = ?";
            sqlParams.add(keyid2);
        }
        if (keycolid3.length() > 0) {
            sql = sql + " and t." + keycolid3 + " = ?";
            sqlParams.add(keyid3);
        }
        database.createPreparedResultSet("getSDIColumns", sql, sqlParams.toArray());
        DataSet dsSDI = new DataSet(database.getResultSet("getSDIColumns"));
        return dsSDI;
    }

    private class FilteredClass {
        private String sdcid;
        private String keyid1;
        private String keyid2;
        private String keyid3;
        private String paramlistid;
        private String paramlistversionid;
        private String variantid;
        private String dataset;

        public String getSdcid() {
            return this.sdcid;
        }

        public void setSdcid(String sdcid) {
            this.sdcid = sdcid;
        }

        public String getKeyid1() {
            return this.keyid1;
        }

        public void setKeyid1(String keyid1) {
            this.keyid1 = keyid1;
        }

        public String getKeyid2() {
            return this.keyid2;
        }

        public void setKeyid2(String keyid2) {
            this.keyid2 = keyid2;
        }

        public String getKeyid3() {
            return this.keyid3;
        }

        public void setKeyid3(String keyid3) {
            this.keyid3 = keyid3;
        }

        public String getParamlistid() {
            return this.paramlistid;
        }

        public void setParamlistid(String paramlistid) {
            this.paramlistid = paramlistid;
        }

        public String getParamlistversionid() {
            return this.paramlistversionid;
        }

        public void setParamlistversionid(String paramlistversionid) {
            this.paramlistversionid = paramlistversionid;
        }

        public String getVariantid() {
            return this.variantid;
        }

        public void setVariantid(String variantid) {
            this.variantid = variantid;
        }

        public String getDataset() {
            return this.dataset;
        }

        public void setDataset(String dataset) {
            this.dataset = dataset;
        }

        public String toString() {
            StringBuilder val = new StringBuilder();
            val.append("sdcid='" + this.sdcid + "'");
            if (OpalUtil.isNotEmpty(this.keyid1)) {
                val.append(", keyid1='" + this.keyid1 + "'");
            }
            if (OpalUtil.isNotEmpty(this.keyid2)) {
                val.append(", keyid2='" + this.keyid2 + "'");
            }
            if (OpalUtil.isNotEmpty(this.keyid3)) {
                val.append(", keyid3='" + this.keyid3 + "'");
            }
            if (OpalUtil.isNotEmpty(this.paramlistid)) {
                val.append(", paramlistid='" + this.paramlistid + "'");
            }
            if (OpalUtil.isNotEmpty(this.paramlistversionid)) {
                val.append(", paramlistversionid='" + this.paramlistversionid + "'");
            }
            if (OpalUtil.isNotEmpty(this.variantid)) {
                val.append(", variantid='" + this.variantid + "'");
            }
            if (OpalUtil.isNotEmpty(this.dataset)) {
                val.append(", dataset='" + this.dataset + "'");
            }
            return val.toString();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            FilteredClass filteredClass = (FilteredClass)o;
            if (this.sdcid != null ? !this.sdcid.equals(filteredClass.sdcid) : filteredClass.sdcid != null) {
                return false;
            }
            if (this.keyid1 != null ? !this.keyid1.equals(filteredClass.keyid1) : filteredClass.keyid1 != null) {
                return false;
            }
            if (this.keyid2 != null ? !this.keyid2.equals(filteredClass.keyid2) : filteredClass.keyid2 != null) {
                return false;
            }
            if (this.keyid3 != null ? !this.keyid3.equals(filteredClass.keyid3) : filteredClass.keyid3 != null) {
                return false;
            }
            if (this.paramlistid != null ? !this.paramlistid.equals(filteredClass.paramlistid) : filteredClass.paramlistid != null) {
                return false;
            }
            if (this.paramlistversionid != null ? !this.paramlistversionid.equals(filteredClass.paramlistversionid) : filteredClass.paramlistversionid != null) {
                return false;
            }
            if (this.variantid != null ? !this.variantid.equals(filteredClass.variantid) : filteredClass.variantid != null) {
                return false;
            }
            return this.dataset != null ? !this.dataset.equals(filteredClass.dataset) : filteredClass.dataset != null;
        }

        public int hashCode() {
            int result = 31;
            result = 31 * result + (this.sdcid != null ? this.sdcid.hashCode() : 0);
            result = 31 * result + (this.keyid1 != null ? this.keyid1.hashCode() : 0);
            result = 31 * result + (this.keyid2 != null ? this.keyid2.hashCode() : 0);
            result = 31 * result + (this.keyid3 != null ? this.keyid3.hashCode() : 0);
            result = 31 * result + (this.paramlistid != null ? this.paramlistid.hashCode() : 0);
            result = 31 * result + (this.paramlistversionid != null ? this.paramlistversionid.hashCode() : 0);
            result = 31 * result + (this.variantid != null ? this.variantid.hashCode() : 0);
            result = 31 * result + (this.dataset != null ? this.dataset.hashCode() : 0);
            return result;
        }

        public FilteredClass() {
        }

        public FilteredClass(String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset) {
            this.sdcid = sdcid;
            this.keyid1 = keyid1;
            this.keyid2 = keyid2;
            this.keyid3 = keyid3;
            this.paramlistid = paramlistid;
            this.paramlistversionid = paramlistversionid;
            this.variantid = variantid;
            this.dataset = dataset;
        }
    }
}

