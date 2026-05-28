/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workitem;

import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.actions.sdidata.AddDataSet;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.modules.eventmanager.EventManager;
import com.labvantage.sapphire.modules.eventmanager.eventobject.PostAddWorkItemEventObject;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.WorkItemItemRuleEvaluator;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.graceperiod.GRCPeriodUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDIWorkItem
extends BaseAction
implements CacheNames,
sapphire.action.AddSDIWorkItem {
    String TEMP_INDEX = "_index";
    String TEMP_ORIGINALSEQUENCE = "_originalsequence";
    String TEMP_NEWWORKITEMINSTANCE = "_newworkiteminstance";
    String PROPERTY_NEWWORKITEMINSTANCEXML = "newworkiteminstancexml";
    public static String LABVANTAGE_CVS_ID = "$Revision: 100960 $";
    private String transactionid = "";
    private static long transactionseq = 0L;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.transactionid = this.connectionInfo.getSysuserId() + transactionseq++;
        AddDataSet.startTransactionCache(this.transactionid);
        DAMProcessor dam = this.getDAMProcessor();
        String sdcid = properties.getProperty("sdcid");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcProps = sdcProcessor.getPropertyList(sdcid);
        sdcid = sdcProps.getProperty("sdcid");
        boolean isVersionedSDC = "Y".equalsIgnoreCase(sdcProps.getProperty("versionedflag"));
        String tableid = sdcProcessor.getProperty(sdcid, "tableid");
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        PropertyList workItemSDCProps = sdcProcessor.getPropertyList("WorkItem");
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        PropertyList sdiWIInstances = properties.containsKey("__sdiWIIInstances") ? (PropertyList)properties.get("__sdiWIIInstances") : new PropertyList();
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        String workItemOffsetColumn = policy.getProperty("workitemduedateoffsetcolumn", "sdiworkitem.createdt");
        boolean matchSDIWorkItemDueDt = "Y".equalsIgnoreCase(policy.getProperty("matchworkitemduedatetosample", "N"));
        boolean bbSamples = properties.getProperty("bbsample").length() > 0;
        boolean bbChildSamples = properties.getProperty("bbchildsample").length() > 0;
        DataSet sdiworkitems = new DataSet();
        DataSet sdiwi = new DataSet();
        DataSet sdiwiitem = new DataSet();
        DataSet sdiforms = new DataSet();
        DataSet sdiworksheets = new DataSet();
        DataSet sdiattributes = new DataSet();
        DataSet sdiRows = new DataSet();
        PropertyList sdiwiMaxUserSeq = new PropertyList();
        if (properties.getPropertyList("__sdiwiMaxUserSeq") != null) {
            sdiwiMaxUserSeq = properties.getPropertyList("__sdiwiMaxUserSeq");
        }
        boolean deleterset = false;
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        String wirsetid = "";
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rsetid = BaseSDIDataAction.createBypassSecurityRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), this.database, this.connectionInfo, applylock);
            deleterset = true;
        }
        String reason = properties.getProperty("auditreason");
        String activity = properties.getProperty("auditactivity");
        String signedFlag = properties.getProperty("auditsignedflag");
        String auditdt = properties.getProperty("auditdt");
        if (reason.length() > 0 && properties.getProperty("tracelogid", "").trim().length() == 0 && !"N".equalsIgnoreCase(sdcProps.getProperty("auditedflag"))) {
            int tracelogid;
            this.logger.info("Generate the tracelog record");
            PropertyList tracelogprops = new PropertyList();
            tracelogprops.setProperty("sdcid", sdcid);
            tracelogprops.setProperty("keyid1", properties.getProperty("keyid1", ""));
            if (properties.getProperty("keyid2", "").trim().length() > 0) {
                tracelogprops.setProperty("keyid2", properties.getProperty("keyid2"));
                if (properties.getProperty("keyid3", "").trim().length() > 0) {
                    tracelogprops.setProperty("keyid3", properties.getProperty("keyid3"));
                }
            }
            tracelogprops.setProperty("description", "Added workitems");
            tracelogprops.setProperty("auditreason", reason);
            tracelogprops.setProperty("auditactivity", activity);
            tracelogprops.setProperty("auditsignedflag", signedFlag);
            tracelogprops.setProperty("auditdt", auditdt);
            ActionService ac = new ActionService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            try {
                ac.processActionClass(AddSDITraceLog.class.getName(), tracelogprops, new ErrorHandler());
                tracelogid = Integer.parseInt(tracelogprops.getProperty("tracelogid"));
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", "Error adding tracelog", e);
            }
            properties.setProperty("tracelogid", String.valueOf(tracelogid));
        }
        DataSet sdiWIColList = WorkItemUtil.getEditableColumnList(this.database, this.logger, "sdiworkitem");
        sdiWIColList = WorkItemUtil.filterColumnIdsWithPropList(this.logger, sdiWIColList, properties);
        DataSet sdidataColList = WorkItemUtil.getEditableColumnList(this.database, this.logger, "sdidata");
        sdidataColList = WorkItemUtil.filterColumnIdsWithPropList(this.logger, sdidataColList, properties);
        DataSet allEditColList = new DataSet();
        allEditColList = this.copyDataSet(sdiWIColList, allEditColList);
        allEditColList = this.copyDataSet(sdidataColList, allEditColList);
        String selectWorkitems = "SELECT\tsdiworkitem.sdcid, sdiworkitem.keyid1, sdiworkitem.keyid2, sdiworkitem.keyid3, sdiworkitem.workitemid, sdiworkitem.workitemversionid, sdiworkitem.workiteminstance, sdiworkitem.usersequence, sdiworkitem.workitemstatus FROM\tsdiworkitem, rsetitems WHERE\trsetitems.sdcid = ? AND \t\trsetitems.rsetid = ? AND \t\trsetitems.sdcid = sdiworkitem.sdcid AND \t\trsetitems.keyid1 = sdiworkitem.keyid1 AND \t\trsetitems.keyid2 = sdiworkitem.keyid2 AND \t\trsetitems.keyid3 = sdiworkitem.keyid3 ORDER BY sdiworkitem.keyid1, sdiworkitem.keyid2, sdiworkitem.keyid3, sdiworkitem.workitemid, sdiworkitem.workiteminstance DESC";
        this.database.createPreparedResultSet(selectWorkitems, new Object[]{sdcid, rsetid});
        sdiworkitems.setResultSet(this.database.getResultSet());
        for (int i = 0; i < sdiworkitems.getRowCount(); ++i) {
            String sdiId = this.getSDIId(sdcid, sdiworkitems.getString(i, "keyid1"), sdiworkitems.getString(i, "keyid2"), sdiworkitems.getString(i, "keyid3"));
            int sdiUserSeqCache = Integer.parseInt(sdiwiMaxUserSeq.getProperty(sdiId, "0"));
            int sdiWIUserSeq = sdiworkitems.getInt(i, "usersequence");
            if (sdiWIUserSeq <= sdiUserSeqCache) continue;
            sdiwiMaxUserSeq.setProperty(sdiId, String.valueOf(sdiWIUserSeq));
        }
        boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        String testMethodOrder = properties.getProperty("propsmatchtestmethodorder", "");
        String forcenew = properties.getProperty("forcenew");
        if (!"Y".equalsIgnoreCase(forcenew)) {
            PropertyList dataEntryPolicyProps = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
            PropertyList forceNewProps = dataEntryPolicyProps.getPropertyList("workitemforcenew");
            String retainFlag = forceNewProps.getProperty("retainselectforcenew");
            String alwaysForceNew = forceNewProps.getProperty("alwaysforcenew");
            if ("N".equals(retainFlag) && "Y".equals(alwaysForceNew)) {
                properties.setProperty("forcenew", "Y");
            }
        }
        Calendar now = DateTimeUtil.getNowCalendar();
        properties.setProperty("workiteminstance", "");
        if (propsmatch) {
            int i;
            StringBuilder groupByCols = new StringBuilder();
            if (properties.getProperty("qcapplyaction", "N").equalsIgnoreCase("Y")) {
                groupByCols.append("workitemid, workitemversionid, applyworkitem, groupid, groupinstance, _index");
            } else {
                groupByCols.append("workitemid, workitemversionid, applyworkitem, groupid, groupinstance");
            }
            DataSet propsDS = new DataSet(this.connectionInfo);
            propsDS.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), ";");
            propsDS.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), ";", "(null)");
            propsDS.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), ";", "(null)");
            propsDS.addColumnValues("workitemid", 0, properties.getProperty("workitemid"), ";");
            propsDS.addColumnValues("workitemversionid", 0, properties.getProperty("workitemversionid"), ";", "C");
            propsDS.addColumnValues("applyworkitem", 0, properties.getProperty("applyworkitem"), ";", "Y");
            propsDS.addColumnValues("groupid", 0, properties.getProperty("groupid"), ";");
            propsDS.addColumnValues("groupinstance", 0, properties.getProperty("groupinstance"), ";");
            if (testMethodOrder.length() > 0) {
                propsDS.addColumnValues("testmethodorder", 1, testMethodOrder, ";");
            }
            this.sanitizeProperties(propsDS);
            propsDS.padColumns();
            propsDS.addColumnValues("offsetfromdt", 0, properties.getProperty("offsetfromdt"), ";");
            if (testMethodOrder.length() > 0) {
                ArrayList<BigDecimal> processedOrders = new ArrayList<BigDecimal>();
                HashMap<String, Object> map = new HashMap<String, Object>();
                StringBuffer groupStr = new StringBuffer("workitemid");
                for (int g = 0; g < propsDS.getRowCount(); ++g) {
                    BigDecimal order = propsDS.getBigDecimal(g, "testmethodorder");
                    if (processedOrders.contains(order)) continue;
                    processedOrders.add(order);
                    map.put("testmethodorder", order);
                    DataSet sameOrder = propsDS.getFilteredDataSet(map);
                    DataSet copy = sameOrder.copy();
                    copy.sort("keyid1,keyid2,keyid3");
                    ArrayList<DataSet> grps = copy.getGroupedDataSets("keyid1,keyid2,keyid3");
                    for (int grp = 0; grp < grps.size(); ++grp) {
                        DataSet ds;
                        DataSet sameKeyGroup = grps.get(grp);
                        int rowCnt = sameKeyGroup.getRowCount();
                        if (rowCnt <= 1) continue;
                        map.clear();
                        map.put("workitemid", sameKeyGroup.getString(0, "workitemid", ""));
                        String errMsg = "Same 'propsmatchtestmethodorder' is specified for different ";
                        if (properties.containsKey("workitemversionid") && sameKeyGroup.getString(0, "workitemversionid", "").length() > 0) {
                            groupStr.append(", workitemversionid");
                            map.put("workitemversionid", sameOrder.getString(0, "workitemversionid", ""));
                            errMsg = errMsg + " groups of ";
                        }
                        if ((ds = sameKeyGroup.getFilteredDataSet(map)).getRowCount() != rowCnt) {
                            errMsg = errMsg + "values in the following input properties: ";
                            throw new SapphireException("INVALID_PROPERTY", errMsg + groupStr);
                        }
                        map.clear();
                    }
                }
                groupByCols.insert(0, "testmethodorder,");
            }
            WorkItemUtil.fillDSWithProps(this.getSDCProcessor(), this.getTranslationProcessor(), allEditColList, properties, propsDS, null, false, 0, 0, ";", true, "");
            propsDS.setDate(-1, "createdt", now);
            propsDS.setString(-1, "createby", this.connectionInfo.getSysuserId());
            propsDS.setString(-1, "createtool", this.connectionInfo.getTool());
            propsDS.setDate(-1, "moddt", now);
            propsDS.setString(-1, "modby", this.connectionInfo.getSysuserId());
            propsDS.setString(-1, "modtool", this.connectionInfo.getTool());
            HashMap temp = new HashMap();
            int lastIndex = 0;
            propsDS.addColumn(this.TEMP_INDEX, 1);
            propsDS.addColumn(this.TEMP_ORIGINALSEQUENCE, 1);
            propsDS.setSequence(this.TEMP_ORIGINALSEQUENCE);
            propsDS.setSequence(this.TEMP_NEWWORKITEMINSTANCE);
            for (i = 0; i < propsDS.size(); ++i) {
                String workitemid = propsDS.getValue(i, "workitemid");
                BigDecimal index = (BigDecimal)temp.get(workitemid);
                if (index == null) {
                    index = new BigDecimal(lastIndex++);
                }
                propsDS.setNumber(i, this.TEMP_INDEX, index);
            }
            for (i = 0; i < allEditColList.size(); ++i) {
                groupByCols.append(", ").append(allEditColList.getString(i, "columnid"));
            }
            propsDS.sort(groupByCols.toString());
            ArrayList<DataSet> groups = propsDS.getGroupedDataSets(groupByCols.toString());
            SDIData sdiData = new SDIData();
            DataSet newWIInstances = new DataSet();
            for (DataSet dataSet : groups) {
                PropertyList pl = new PropertyList();
                pl.putAll(properties);
                pl.setProperty("propsmatch", "N");
                pl.setProperty("__propsmatch", "Y");
                pl.setProperty("keyid1", dataSet.getColumnValues("keyid1", ";"));
                pl.setProperty("keyid2", dataSet.getColumnValues("keyid2", ";"));
                pl.setProperty("keyid3", dataSet.getColumnValues("keyid3", ";"));
                pl.setProperty("workitemid", dataSet.getValue(0, "workitemid"));
                pl.setProperty("workitemversionid", dataSet.getValue(0, "workitemversionid"));
                pl.setProperty("applyworkitem", dataSet.getValue(0, "applyWorkitem"));
                pl.setProperty("groupid", dataSet.getValue(0, "groupid"));
                pl.setProperty("groupinstance", dataSet.getValue(0, "groupInstance"));
                for (int i2 = 0; i2 < allEditColList.size(); ++i2) {
                    String columnId = allEditColList.getString(i2, "columnid");
                    pl.setProperty(columnId, dataSet.getValue(0, columnId));
                }
                pl.setProperty("offsetfromdt", dataSet.getValue(0, "offsetfromdt"));
                this.processAction(pl);
                String newworkiteminstances = pl.getProperty("workiteminstance");
                String tempXML = pl.getProperty(this.PROPERTY_NEWWORKITEMINSTANCEXML);
                DataSet tempDS = new DataSet(tempXML);
                for (int i3 = 0; i3 < tempDS.size(); ++i3) {
                    newWIInstances.copyRow(tempDS, i3, 1);
                }
                dataSet.addColumnValues(this.TEMP_NEWWORKITEMINSTANCE, 1, newworkiteminstances, ";");
                SDIData callSDIData = (SDIData)pl.get("__sdidata");
                Set datasets = callSDIData.getDatasets();
                for (String dataset : datasets) {
                    DataSet callDataSet = callSDIData.getDataset(dataset);
                    if (callDataSet == null) continue;
                    DataSet ds = sdiData.getDataset(dataset);
                    if (ds == null) {
                        sdiData.setDataset(dataset, callDataSet);
                        continue;
                    }
                    ds.copyRow(callDataSet, -1, 1);
                }
            }
            propsDS.sort(this.TEMP_ORIGINALSEQUENCE);
            properties.setProperty("workiteminstance", propsDS.getColumnValues(this.TEMP_NEWWORKITEMINSTANCE, ";"));
            properties.setProperty(this.PROPERTY_NEWWORKITEMINSTANCEXML, newWIInstances.toXML());
            EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), new PostAddWorkItemEventObject(sdcid, sdcProps, sdiData, properties));
        } else {
            DataSet allWorkItemData;
            boolean propsMatchCall = properties.getProperty("__propsmatch").equals("Y");
            DataSet newWIInstances = new DataSet();
            String workitemversionidstr = properties.getProperty("workitemversionid", "").trim();
            String currentWorkItemVersions = properties.getProperty("workitemversionid", "").trim();
            if (workitemversionidstr.length() == 0 || workitemversionidstr.startsWith(";") || workitemversionidstr.endsWith(";") || workitemversionidstr.indexOf(";;") > -1 || workitemversionidstr.indexOf("C") > -1) {
                currentWorkItemVersions = SdiInfo.getCurrentVersion("WorkItem", properties.getProperty("workitemid", ";"), null, new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            }
            boolean wiiRuleToProcess = properties.containsKey("workitemitemrule");
            String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
            String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
            String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
            String[] workitemidprop = StringUtil.split(properties.getProperty("workitemid"), ";");
            String[] workitemversionidprop = StringUtil.split(properties.getProperty("workitemversionid"), ";");
            String[] workitemversionidcurrprop = StringUtil.split(currentWorkItemVersions, ";");
            String[] applyworkitemprop = StringUtil.split(properties.getProperty("applyworkitem"), ";");
            String[] groupidprop = StringUtil.split(properties.getProperty("groupid"), ";");
            String[] groupinstanceprop = StringUtil.split(properties.getProperty("groupinstance"), ";");
            String[] sdigroupidprop = StringUtil.split(properties.getProperty("sdigroupid"), ";");
            String[] sdigroupinstanceprop = StringUtil.split(properties.getProperty("sdigroupinstance"), ";");
            String[] usersequenceprop = StringUtil.split(properties.getProperty("usersequence"), ";");
            String[] sdiwi_sampletypid = StringUtil.split(properties.getProperty("sdiworkitem_sampletypeid"), ";");
            String[] workitemitemRules = new String[]{""};
            String[] workitemitemIds = new String[]{""};
            if (wiiRuleToProcess) {
                workitemitemRules = StringUtil.split(properties.getProperty("workitemitemrule"), ";");
                workitemitemIds = StringUtil.split(properties.getProperty("workitemitemid"), ";");
            }
            DataSet dtDs = new DataSet(this.connectionInfo);
            dtDs.addColumnValues("duedt", 2, properties.getProperty("duedt"), ";", "");
            dtDs.padColumns();
            dtDs.addColumnValues("offsetfromdt", 2, properties.getProperty("offsetfromdt"), ";", "");
            boolean createWorksheetInputProp = "Y".equals(properties.getProperty("createworksheet", "Y"));
            DataSet workItemForms = new DataSet();
            DataSet workItemWorksheets = new DataSet();
            String rsetWorkItems = this.getDAMProcessor().createRSet("WorkItem", properties.getProperty("workitemid"), currentWorkItemVersions, "", true, 1);
            if (createWorksheetInputProp) {
                workItemForms = WorkItemUtil.getWorkItemForms(properties.getProperty("workitemid"), currentWorkItemVersions, this.getQueryProcessor(), this.logger, rsetWorkItems);
                workItemWorksheets = WorkItemUtil.getWorkItemWorksheets(properties.getProperty("workitemid"), currentWorkItemVersions, this.getQueryProcessor(), this.logger, rsetWorkItems);
            }
            HashMap<String, String> findmap = new HashMap<String, String>();
            boolean addToWorkflow = true;
            DataSet workItemWorkflowRule = null;
            if (addToWorkflow) {
                workItemWorkflowRule = WorkItemUtil.getWorkItemWorkflowRules(properties.getProperty("workitemid"), currentWorkItemVersions, this.getQueryProcessor(), this.logger, rsetWorkItems);
            }
            if ((allWorkItemData = this.getQueryProcessor().getPreparedSqlDataSet("SELECT w.* FROM workitem w, rsetitems r  WHERE w.workitemid = r.keyid1 AND w.workitemversionid = r.keyid2 AND r.rsetid =?", (Object[])new String[]{rsetWorkItems})).getRowCount() == 0) {
                throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Invalid Workitem specified in action input"));
            }
            for (int wid = 0; wid < workitemidprop.length; ++wid) {
                String isVersionProtectEnabled;
                PropertyList versionprotection;
                if (allWorkItemData.findRow("workitemid", workitemidprop[wid]) < 0) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Invalid Workitem specified in action input:" + workitemidprop[wid]));
                }
                PropertyList paramListPolicy = this.getConfigurationProcessor().getPolicy("ParamListPolicy", "Sapphire Custom");
                if (paramListPolicy == null || (versionprotection = paramListPolicy.getPropertyListNotNull("expireddataprotection")) == null || versionprotection.size() <= 0 || !(isVersionProtectEnabled = versionprotection.getProperty("WorkItem".toLowerCase(), "")).equalsIgnoreCase("Y") || allWorkItemData.findRow("versionstatus", "E") < 0) continue;
                throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Expired Workitem specified in action input:" + workitemidprop[wid]));
            }
            DataSet allWorkItemAttributes = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s.* FROM sdiattribute s, rsetitems r WHERE s.keyid1 = r.keyid1 AND s.keyid2 = r.keyid2 AND s.sdcid = r.sdcid AND r.rsetid = ?", (Object[])new String[]{rsetWorkItems});
            DataSet allWorkItemReagents = this.getQueryProcessor().getPreparedSqlDataSet("SELECT wr.* FROM workitemreagenttype wr, rsetitems r WHERE wr.workitemid = r.keyid1 AND wr.workitemversionid = r.keyid2 AND r.sdcid = 'WorkItem' AND r.rsetid = ?", (Object[])new String[]{rsetWorkItems});
            DataSet allWorkItemInstruments = this.getQueryProcessor().getPreparedSqlDataSet("SELECT wi.* FROM workiteminstrument wi, rsetitems r WHERE wi.workitemid = r.keyid1 AND wi.workitemversionid = r.keyid2 AND r.sdcid = 'WorkItem' AND r.rsetid = ?", (Object[])new String[]{rsetWorkItems});
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer selectSDIs = new StringBuffer("select s.* from " + tableid + " s, rsetitems WHERE rsetitems.rsetid=" + safeSQL.addVar(rsetid) + "  AND rsetitems.sdcid =" + safeSQL.addVar(sdcid) + " AND s." + keycolid1 + " = rsetitems.keyid1");
            if (keycolid2.length() > 0) {
                selectSDIs.append(" AND s." + keycolid2 + " = rsetitems.keyid2");
            }
            if (keycolid3.length() > 0) {
                selectSDIs.append(" AND s." + keycolid3 + " = rsetitems.keyid3");
            }
            this.database.createPreparedResultSet(selectSDIs.toString(), safeSQL.getValues());
            sdiRows.setResultSet(this.database.getResultSet());
            dam.clearRSet(rsetWorkItems);
            for (int wi = 0; wi < workitemidprop.length; ++wi) {
                String instancelist;
                String[] pseudokeys;
                int groupinstanceint;
                String applyworkitem;
                String sdiwi_sampletype;
                HashMap<String, String> filterMap = new HashMap<String, String>();
                HashMap<String, String> filterAttibutesMap = new HashMap<String, String>();
                StringBuffer wiinstances = new StringBuffer();
                String workItemId = workitemidprop[wi];
                String workitemitemRule = "";
                String workitemitemId = "";
                if (wiiRuleToProcess) {
                    workitemitemRule = workitemidprop.length == workitemitemRules.length ? workitemitemRules[wi] : "";
                    workitemitemId = workitemidprop.length == workitemitemIds.length ? workitemitemIds[wi] : "";
                }
                String string = sdiwi_sampletype = workitemidprop.length == sdiwi_sampletypid.length ? sdiwi_sampletypid[wi] : "";
                String string2 = applyworkitemprop.length == 0 ? "" : (applyworkitem = applyworkitemprop.length < workitemidprop.length || applyworkitemprop[wi] == null || applyworkitemprop[wi].length() == 0 ? applyworkitemprop[0] : applyworkitemprop[wi]);
                if (applyworkitem.length() == 0) {
                    applyworkitem = "Y";
                }
                String workItemVerId = this.getWorkItemVersionId(workitemversionidprop, wi, workitemversionidcurrprop);
                String workItemVerIdProvided = this.getWorkItemVersionId(workitemversionidprop, wi, null);
                filterMap.put("workitemid", workItemId);
                filterMap.put("workitemversionid", workItemVerId);
                filterAttibutesMap.put("keyid1", workItemId);
                filterAttibutesMap.put("keyid2", workItemVerId);
                filterAttibutesMap.put("attributesdcid", "SDIWorkItem");
                DataSet workitemattributes = allWorkItemAttributes.getFilteredDataSet(filterAttibutesMap);
                for (int a = 0; a < workitemattributes.getRowCount(); ++a) {
                    if (workitemattributes.getValue(a, "attributetypeflag").length() != 0) continue;
                    workitemattributes.setValue(a, "attributetypeflag", "E");
                }
                Calendar dueDt = dtDs.getCalendar(wi, "duedt", null);
                boolean dueDtInActionInput = dueDt != null;
                DataSet workItemDS = allWorkItemData.getFilteredDataSet(filterMap);
                String workitemtypeflag = "W";
                String duedtoffset = "";
                String duedtoffsetunit = "";
                String testingdepartmentid = "";
                String testinglabType = "";
                String workareaType = "";
                String workareadepartmentid = "";
                String autoassignrule = "";
                String autoassignanalystid = "";
                String createactivityrule = "";
                String resolvedTestingDepartmentId = "";
                Calendar offsetDt = dtDs.getCalendar(wi, "offsetfromdt");
                if (workItemDS.getRowCount() > 0) {
                    workitemtypeflag = workItemDS.getString(0, "workitemtypeflag");
                    duedtoffset = workItemDS.getValue(0, "duedtoffset", "");
                    duedtoffsetunit = workItemDS.getString(0, "duedtoffsettimeunit", "");
                    testingdepartmentid = workItemDS.getString(0, "testingdepartmentid", "");
                    workareadepartmentid = workItemDS.getString(0, "workareadepartmentid", "");
                    autoassignrule = workItemDS.getString(0, "autoassignrule", "");
                    autoassignanalystid = workItemDS.getString(0, "autoassignanalystid", "");
                    createactivityrule = workItemDS.getString(0, "createactivityrule", "");
                    testinglabType = workItemDS.getString(0, "testinglabtype", "");
                    workareaType = workItemDS.getString(0, "workareatype", "");
                }
                String groupid = groupidprop.length == 0 || groupidprop.length < workitemidprop.length || groupidprop[wi].length() == 0 ? "" : groupidprop[wi];
                String groupinstance = groupinstanceprop.length == 0 || groupinstanceprop.length < workitemidprop.length || groupinstanceprop[wi].length() == 0 ? "1" : groupinstanceprop[wi];
                try {
                    groupinstanceint = Integer.parseInt(groupinstance);
                }
                catch (NumberFormatException nfe) {
                    groupinstanceint = 1;
                }
                StringBuffer groupidlist = new StringBuffer();
                StringBuffer groupinstancelist = new StringBuffer();
                boolean createWorksheet = true;
                boolean createLESWorksheet = true;
                String formId = "";
                String formVersionId = "";
                String createWorkSheetRule = "";
                String worksheetId = properties.getProperty("worksheetid");
                String worksheetVersionId = properties.getProperty("worksheetversionid");
                String workbookId = properties.getProperty("workbookid");
                String workbookVersionId = properties.getProperty("workbookversionid");
                String lesCreateFlag = "";
                String lesAuthorFlag = "";
                if (createWorksheetInputProp) {
                    HashMap<String, String> worksheetFilterMap = new HashMap<String, String>();
                    worksheetFilterMap.put("workitemid", workItemId);
                    worksheetFilterMap.put("workitemversionid", workItemVerId);
                    DataSet workItemForm = workItemForms.getFilteredDataSet(worksheetFilterMap);
                    createWorkSheetRule = workItemForm.getString(0, "createworksheetrule", "");
                    formId = properties.getProperty("formid", "");
                    formVersionId = properties.getProperty("formversionid", "");
                    worksheetFilterMap.put("formrule", "default");
                    int row = workItemForm.findRow(worksheetFilterMap);
                    if (formId.length() == 0 && row > -1) {
                        formId = workItemForm.getString(row, "formid", "");
                        formVersionId = workItemForm.getString(row, "formversionid", "");
                    }
                    worksheetFilterMap.remove("formrule");
                    worksheetFilterMap.put("worksheetrule", "default");
                    DataSet workItemWorksheet = workItemWorksheets.getFilteredDataSet(worksheetFilterMap);
                    if (workItemWorksheet.size() > 0) {
                        lesCreateFlag = workItemWorksheet.getString(0, "createlesrule", "");
                        lesAuthorFlag = workItemWorksheet.getString(0, "authorflag");
                        if (worksheetId.length() == 0) {
                            worksheetId = workItemWorksheet.getString(0, "worksheetid", "");
                            worksheetVersionId = workItemWorksheet.getString(0, "worksheetversionid", "");
                        }
                        if (workbookId.length() == 0) {
                            workbookId = workItemWorksheet.getString(0, "workbookid", "");
                            workbookVersionId = workItemWorksheet.getString(0, "workbookversionid", "");
                        }
                    }
                }
                if ((pseudokeys = ((DBUtil)this.database).getUUIDList(keyid1prop.length)) == null || pseudokeys.length != keyid1prop.length) {
                    throw new SapphireException("Unable to generate sdiworkitemid.");
                }
                DataSet addAndApplySDIs = new DataSet();
                addAndApplySDIs.addColumn("keyid1", 0);
                addAndApplySDIs.addColumn("keyid2", 0);
                addAndApplySDIs.addColumn("keyid3", 0);
                for (int sdi = 0; sdi < keyid1prop.length; ++sdi) {
                    String sdiAssignedAnalyst = "";
                    String sdiAssignedDepartment = "";
                    String sdiSiteId = "";
                    String sdiTestingDepartmentId = "";
                    String sdiWorkareaDepartmentId = "";
                    String sdiWapStatus = "";
                    String sampleType = "";
                    String keyid1 = keyid1prop[sdi];
                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[sdi].length() == 0 ? "(null)" : keyid2prop[sdi];
                    String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[sdi].length() == 0 ? "(null)" : keyid3prop[sdi];
                    boolean copyWapStatus = false;
                    boolean addSDIWI = true;
                    String addApplyFlag = "";
                    SDI thisSDI = new SDI(sdcid, keyid1, keyid2, keyid3);
                    HashMap<String, String> findSDI = new HashMap<String, String>();
                    findSDI.put(keycolid1, thisSDI.getKeyid1());
                    if (keycolid2.length() > 0) {
                        findSDI.put(keycolid2, thisSDI.getKeyid2());
                    }
                    if (keycolid3.length() > 0) {
                        findSDI.put(keycolid3, thisSDI.getKeyid3());
                    }
                    int sdiRow = sdiRows.findRow(findSDI);
                    boolean isSDITemplate = "Y".equalsIgnoreCase(sdiRows.getValue(sdiRow, "templateflag"));
                    if ("Sample".equals(sdcid) && sdiRow > -1) {
                        sdiAssignedAnalyst = sdiRows.getValue(sdiRow, "assignedanalystid");
                        sdiAssignedDepartment = sdiRows.getValue(sdiRow, "assigneddepartmentid");
                        sdiSiteId = sdiRows.getValue(sdiRow, "sitedepartmentid");
                        sdiTestingDepartmentId = sdiRows.getValue(sdiRow, "testingdepartmentid");
                        sdiWorkareaDepartmentId = sdiRows.getValue(sdiRow, "workareadepartmentid");
                        sdiWapStatus = sdiRows.getValue(sdiRow, "wapstatus");
                        sampleType = sdiRows.getValue(sdiRow, "sampletypeid");
                        if (!"Y".equalsIgnoreCase(sdiRows.getValue(sdiRow, "templateflag")) && (sdiWapStatus.length() == 0 || "Never".equalsIgnoreCase(sdiWapStatus))) {
                            copyWapStatus = true;
                        }
                    }
                    if (wiiRuleToProcess) {
                        int addRow;
                        int aliquotplancount;
                        if (bbSamples && sdiwi_sampletype.length() > 0 && !sampleType.equals(sdiwi_sampletype)) continue;
                        boolean childSampleWithAliquot = false;
                        if (bbChildSamples && (aliquotplancount = this.database.getPreparedCount("select count(s_childsampleplanversionid) cnt from s_childsampleplanitem, workitem where s_childsampleplanid = workitem.embedchildsampleplanid and s_childsampleplanversionid = workitem.embedchildsampleplanversionid and plantype = 'Aliquot' and workitem.workitemid = ? and workitem.workitemversionid = ?", new String[]{workItemId, workItemVerId})) > 0) {
                            childSampleWithAliquot = true;
                        }
                        if (workitemitemRule.length() > 0 && !workitemitemRule.trim().equals("{}") && !workitemitemRule.trim().equals("null")) {
                            addApplyFlag = WorkItemItemRuleEvaluator.evaluateAddAndApplyWorkItemRule(workitemitemRule, new SDI(sdcid, keyid1, keyid2, keyid3), this.database, this.connectionInfo, sdiRows);
                            if ("AA".equalsIgnoreCase(addApplyFlag) && childSampleWithAliquot) {
                                addApplyFlag = "AO";
                            }
                            if ("NA".equalsIgnoreCase(addApplyFlag)) continue;
                            if ("AA".equalsIgnoreCase(addApplyFlag)) {
                                addRow = addAndApplySDIs.addRow();
                                addAndApplySDIs.setString(addRow, "keyid1", keyid1);
                                addAndApplySDIs.setString(addRow, "keyid2", keyid2);
                                addAndApplySDIs.setString(addRow, "keyid3", keyid3);
                            } else if (!"AO".equalsIgnoreCase(addApplyFlag) && "DAN".equalsIgnoreCase(addApplyFlag)) {
                                addSDIWI = false;
                            }
                        } else if (childSampleWithAliquot) {
                            addApplyFlag = "AO";
                        } else {
                            addRow = addAndApplySDIs.addRow();
                            addAndApplySDIs.setString(addRow, "keyid1", keyid1);
                            addAndApplySDIs.setString(addRow, "keyid2", keyid2);
                            addAndApplySDIs.setString(addRow, "keyid3", keyid3);
                        }
                    }
                    String sdiworkitemid = pseudokeys[sdi];
                    if (sdigroupidprop.length > 0) {
                        groupid = sdigroupidprop.length < keyid1prop.length || sdigroupidprop[sdi].length() == 0 ? groupid : sdigroupidprop[sdi];
                        groupinstance = sdigroupinstanceprop.length < keyid1prop.length || sdigroupinstanceprop[sdi].length() == 0 ? groupinstance : sdigroupinstanceprop[sdi];
                        try {
                            groupinstanceint = Integer.parseInt(groupinstance);
                        }
                        catch (NumberFormatException addRow) {
                            // empty catch block
                        }
                    }
                    findmap.clear();
                    findmap.put("keyid1", keyid1);
                    findmap.put("keyid2", keyid2);
                    findmap.put("keyid3", keyid3);
                    sdiwi.sort("keyid1, keyid2, keyid3, usersequence D");
                    findmap.put("workitemid", workItemId);
                    String str_wiinstance = "";
                    if (addSDIWI) {
                        String sdiwi_testingDepartment;
                        int wiinstance = 0;
                        int wisequence = 1;
                        String sdiWIInstance = this.getSDIWIIInstance(sdiWIInstances, sdcid, keyid1, keyid2, keyid3, workItemId);
                        if (sdiWIInstance.length() == 0) {
                            int findrow = sdiworkitems.findRow(findmap);
                            if (findrow > -1) {
                                wiinstance = sdiworkitems.getBigDecimal(findrow, "workiteminstance").intValue();
                            }
                        } else {
                            wiinstance = Integer.parseInt(sdiWIInstance);
                        }
                        this.setSDIWIInstance(sdiWIInstances, sdcid, keyid1, keyid2, keyid3, workItemId, String.valueOf(++wiinstance));
                        boolean autoUserSequence = StringUtil.getYN(properties.getProperty("autousersequence"), "Y").equals("Y");
                        if (autoUserSequence) {
                            wisequence = Integer.parseInt(sdiwiMaxUserSeq.getProperty(this.getSDIId(sdcid, keyid1, keyid2, keyid3), "0")) + 1;
                            sdiwiMaxUserSeq.setProperty(this.getSDIId(sdcid, keyid1, keyid2, keyid3), String.valueOf(wisequence));
                        } else {
                            String usersequence = usersequenceprop[wi];
                            wisequence = new BigDecimal(usersequence).intValue();
                        }
                        wiinstances.append(';').append(wiinstance);
                        if ("P".equalsIgnoreCase(workitemtypeflag)) {
                            groupid = workItemId;
                            groupinstanceint = wiinstance;
                        }
                        int newrow = sdiwi.addRow();
                        WorkItemUtil.fillDSWithProps(this.getSDCProcessor(), this.getTranslationProcessor(), sdiWIColList, properties, sdiwi, "SDIWorkItem", true, newrow, wi, "", false, "");
                        this.logger.info("Adding sdiworkitem row for: " + keyid1 + ", " + keyid2 + ", " + keyid3 + "(" + sdiworkitemid + ")");
                        sdiwi.setString(newrow, "workitemid", workItemId);
                        sdiwi.setString(newrow, "workitemversionid", "Y".equals(applyworkitem) ? workItemVerId : workItemVerIdProvided);
                        sdiwi.setNumber(newrow, "workiteminstance", wiinstance);
                        sdiwi.setString(newrow, "workitemtypeflag", workitemtypeflag);
                        sdiwi.setString(newrow, "sdcid", sdcid);
                        sdiwi.setString(newrow, "keyid1", keyid1);
                        sdiwi.setString(newrow, "keyid2", keyid2);
                        sdiwi.setString(newrow, "keyid3", keyid3);
                        sdiwi.setString(newrow, "sdiworkitemid", sdiworkitemid);
                        sdiwi.setString(newrow, "completeflag", "N");
                        if ("AO".equalsIgnoreCase(addApplyFlag)) {
                            sdiwi.setString(newrow, "appliedflag", "N");
                        } else {
                            sdiwi.setString(newrow, "appliedflag", applyworkitem);
                            if ("Y".equals(applyworkitem)) {
                                sdiwi.setString(newrow, "appliedby", "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId());
                                sdiwi.setDate(newrow, "applieddt", now);
                            }
                        }
                        sdiwi.setNumber(newrow, "usersequence", wisequence);
                        if (groupid.length() > 0) {
                            sdiwi.setString(newrow, "groupid", groupid);
                            sdiwi.setNumber(newrow, "groupinstance", groupinstanceint);
                        }
                        sdiwi.setDate(newrow, "createdt", now);
                        sdiwi.setString(newrow, "createby", this.connectionInfo.getSysuserId());
                        sdiwi.setString(newrow, "createtool", this.connectionInfo.getTool());
                        sdiwi.setDate(newrow, "moddt", now);
                        sdiwi.setString(newrow, "modby", this.connectionInfo.getSysuserId());
                        sdiwi.setString(newrow, "modtool", this.connectionInfo.getTool());
                        sdiwi.setString(newrow, "workitemstatus", "Initial");
                        String duedtOverrideFlag = sdiwi.getString(newrow, "duedtoverrideflag", "");
                        boolean dueDtCalculated = false;
                        if (dueDt == null || "N".equalsIgnoreCase(duedtOverrideFlag)) {
                            if (offsetDt == null) {
                                if (workItemOffsetColumn.startsWith(sdcid.toLowerCase() + ".")) {
                                    if (sdiRow > -1) {
                                        offsetDt = sdiRows.getCalendar(sdiRow, workItemOffsetColumn.substring(workItemOffsetColumn.indexOf(".") + 1));
                                    }
                                } else {
                                    offsetDt = sdiwi.getCalendar(newrow, workItemOffsetColumn.substring(workItemOffsetColumn.indexOf(".") + 1));
                                }
                            }
                            if (offsetDt == null) {
                                offsetDt = Calendar.getInstance();
                            }
                            if (duedtoffset.length() > 0 && duedtoffsetunit != null && duedtoffsetunit.length() > 0) {
                                try {
                                    BigDecimal offset = new BigDecimal(duedtoffset);
                                    dueDtCalculated = true;
                                    dueDt = GRCPeriodUtil.getDueDate(offsetDt, duedtoffsetunit, offset);
                                }
                                catch (Exception e) {
                                    this.logger.info("Invalid or NULL duedtoffset value found for WorkItem: " + workItemId + ". Ignoring duedates.");
                                }
                            }
                        }
                        if (dueDt != null) {
                            Calendar sampleDt;
                            if (matchSDIWorkItemDueDt && (!dueDtInActionInput || "N".equalsIgnoreCase(duedtOverrideFlag)) && sdiRow > -1 && (sampleDt = sdiRows.getCalendar(sdiRow, "duedt")) != null && sampleDt.compareTo(dueDt) < 0) {
                                dueDt = sampleDt;
                            }
                            sdiwi.setDate(newrow, "duedt", dueDt);
                            sdiwi.setString(newrow, "duedtoverrideflag", "N".equalsIgnoreCase(duedtOverrideFlag) ? duedtOverrideFlag : (dueDtInActionInput ? "Y" : "N"));
                        } else {
                            sdiwi.setDate(newrow, "duedt", "");
                        }
                        sdiwi.setNumber(newrow, "duedtoffset", duedtoffset);
                        sdiwi.setString(newrow, "duedtoffsettimeunit", duedtoffsetunit);
                        if ((!isSDITemplate && sdcid.equalsIgnoreCase("Sample") || !sdcid.equalsIgnoreCase("Sample") && "Y".equalsIgnoreCase(applyworkitem)) && sdiwi.getValue(newrow, "testingdepartmentid").length() == 0) {
                            WorkItemUtil.resolveSDIWorkItemTestingDepartment(sdiwi, newrow, workItemSDCProps, workItemDS, testingdepartmentid, testinglabType, sdiSiteId, sdiTestingDepartmentId, workareadepartmentid, sdiWorkareaDepartmentId, this.database, this.connectionInfo);
                        }
                        if ((sdiwi_testingDepartment = sdiwi.getValue(newrow, "testingdepartmentid")).length() > 0 && sdiwi.getValue(newrow, "workareadepartmentid").length() == 0 && workareaType.length() > 0) {
                            this.database.createPreparedResultSet("getworkareadept", "SELECT departmentid FROM department WHERE parentdepartmentid = ? AND workareatype = ?", new String[]{sdiwi_testingDepartment, workareaType});
                            DataSet woDept = new DataSet(this.database.getResultSet("getworkareadept"));
                            if (woDept.getRowCount() > 0 && woDept.getValue(0, "departmentid").length() > 0) {
                                sdiwi.setString(newrow, "workareadepartmentid", woDept.getValue(0, "departmentid"));
                            }
                        }
                        if ("Workarea".equalsIgnoreCase(autoassignrule) && sdiwi.getString(newrow, "s_assigneddepartment", "").length() == 0 && sdiwi.getString(newrow, "workareadepartmentid", "").length() > 0) {
                            sdiwi.setString(newrow, "s_assigneddepartment", sdiwi.getString(newrow, "workareadepartmentid"));
                        } else if ("Analyst".equalsIgnoreCase(autoassignrule) && sdiwi.getString(newrow, "s_assignedanalyst", "").length() == 0 && autoassignanalystid.length() > 0) {
                            sdiwi.setString(newrow, "s_assignedanalyst", autoassignanalystid);
                        }
                        if (copyWapStatus && "On Demand By Workitem".equalsIgnoreCase(createactivityrule) && !properties.containsKey("wapstatus")) {
                            sdiwi.setString(newrow, "wapstatus", "Pending");
                        }
                        if (sdiAssignedAnalyst.length() > 0 && sdiwi.getValue(newrow, "s_assignedanalyst").length() == 0) {
                            sdiwi.setString(newrow, "s_assignedanalyst", sdiAssignedAnalyst);
                        }
                        if (sdiAssignedDepartment.length() > 0 && sdiwi.getValue(newrow, "s_assigneddepartment").length() == 0) {
                            sdiwi.setString(newrow, "s_assigneddepartment", sdiAssignedDepartment);
                        }
                        String assignedAnalyst = sdiwi.getString(newrow, "s_assignedanalyst", "");
                        String assignedDept = sdiwi.getString(newrow, "s_assigneddepartment", "");
                        createWorksheet = "On Creation".equalsIgnoreCase(createWorkSheetRule) || "On Assignment".equalsIgnoreCase(createWorkSheetRule) && (assignedAnalyst.length() > 0 || assignedDept.length() > 0);
                        createLESWorksheet = "On Creation".equalsIgnoreCase(lesCreateFlag) || "On Assignment".equalsIgnoreCase(lesCreateFlag) && assignedAnalyst.length() > 0;
                        if (createWorksheetInputProp && "Y".equals(applyworkitem)) {
                            if (createWorksheet) {
                                WorkItemUtil.addDataForms(sdiforms, sdcid, keyid1, keyid2, keyid3, workItemId, String.valueOf(wiinstance), formId, formVersionId, assignedAnalyst, assignedDept, this.getTranslationProcessor());
                            }
                            if (createLESWorksheet) {
                                WorkItemUtil.addWorksheets(sdiworksheets, sdcid, keyid1, keyid2, keyid3, workItemId, String.valueOf(wiinstance), worksheetId, worksheetVersionId, workbookId, workbookVersionId, "A".equals(lesAuthorFlag) ? assignedAnalyst : "", sdiworkitemid, this.getTranslationProcessor());
                            }
                        }
                        if (workitemattributes.getRowCount() > 0) {
                            boolean excludeAttributeFlag = false;
                            HashMap<String, String> excludeMap = new HashMap<String, String>();
                            if ("Y".equals(applyworkitem) && (!wiiRuleToProcess || addAndApplySDIs.getRowCount() > 0)) {
                                excludeMap.put("attributetypeflag", "S");
                            } else if (sdcid.equalsIgnoreCase("Sample") && isSDITemplate || sdcid.equalsIgnoreCase("Product") || sdcid.equalsIgnoreCase("SamplePoint") || sdcid.equalsIgnoreCase("LV_RequestItem") || sdcid.equalsIgnoreCase("Location") || sdcid.equalsIgnoreCase("Study") || "LV_ProductStage".equalsIgnoreCase(sdcid)) {
                                excludeMap.put("attributetypeflag", "E");
                            } else {
                                excludeAttributeFlag = true;
                            }
                            DataSet dsFilteredWIAttributes = workitemattributes.getFilteredDataSet(excludeMap, true);
                            if (dsFilteredWIAttributes.getRowCount() > 0 && !excludeAttributeFlag) {
                                HashMap<String, ArrayList<String>> hmskipped = new HashMap<String, ArrayList<String>>();
                                BaseSDIAttributeAction.coreCopyDownAttributes(sdiattributes, dsFilteredWIAttributes, this.getSDCProcessor().getPropertyList("SDIWorkItem"), sdiworkitemid, "", "", hmskipped, new M18NUtil(this.connectionInfo), this.logger);
                                BaseSDIAttributeAction.logSkipped(hmskipped, "SDIWorkItem", this.logger);
                            }
                        }
                        str_wiinstance = "" + wiinstance;
                    } else {
                        wiinstances.append(';').append(str_wiinstance);
                    }
                    if (groupid.length() > 0) {
                        groupidlist.append(";").append(groupid);
                        groupinstancelist.append(";").append(groupinstanceint);
                    }
                    int newDSRow = newWIInstances.addRow();
                    newWIInstances.setString(newDSRow, "sdcid", sdcid);
                    newWIInstances.setString(newDSRow, "keyid1", keyid1);
                    newWIInstances.setString(newDSRow, "keyid2", keyid2);
                    newWIInstances.setString(newDSRow, "keyid3", keyid3);
                    newWIInstances.setString(newDSRow, "workitemid", workItemId);
                    newWIInstances.setString(newDSRow, "workitemversionid", workItemVerId);
                    newWIInstances.setString(newDSRow, "sdiworkitemid", sdiworkitemid);
                    newWIInstances.setNumber(newDSRow, "workiteminstance", str_wiinstance);
                    newWIInstances.setString(newDSRow, "workitemveridprovided", workItemVerIdProvided);
                    if (wiiRuleToProcess && workitemitemId != null && workitemitemId.length() > 0) {
                        newWIInstances.setString(newDSRow, "workitemitemid", workitemitemId);
                    }
                    if (groupid.length() <= 0) continue;
                    newWIInstances.setString(newDSRow, "groupid", groupid);
                    newWIInstances.setNumber(newDSRow, "groupinstance", groupinstanceint);
                }
                properties.setProperty("__sdiwiMaxUserSeq", sdiwiMaxUserSeq);
                if ("Y".equals(applyworkitem) && (!wiiRuleToProcess || addAndApplySDIs.getRowCount() > 0)) {
                    DataSet workitemsdef;
                    HashMap<String, String> sdidataColsPropValues = new HashMap<String, String>();
                    for (int i = 0; i < sdidataColList.size(); ++i) {
                        String columnId = sdidataColList.getString(i, "columnid");
                        sdidataColsPropValues.put(columnId, properties.getProperty(columnId));
                    }
                    if (createWorksheetInputProp) {
                        if (createWorksheet) {
                            sdidataColsPropValues.put("createworksheet", "N");
                        } else {
                            sdidataColsPropValues.put("createworksheet", "Y");
                        }
                    } else {
                        sdidataColsPropValues.put("createworksheet", "N");
                    }
                    String securityDept = properties.getProperty("securitydepartment", "");
                    String securityUser = properties.getProperty("securityuser", "");
                    if ("D".equalsIgnoreCase(workItemSDCProps.getProperty("accesscontrolledflag", "")) && securityDept.length() == 0) {
                        securityDept = workItemDS.getString(0, "securitydepartment", "");
                    }
                    if (securityDept.length() > 0) {
                        sdidataColsPropValues.put("securitydepartment", securityDept);
                    }
                    if (securityUser.length() > 0) {
                        sdidataColsPropValues.put("securityuser", securityUser);
                    }
                    if ("On Demand By DataSet".equalsIgnoreCase(createactivityrule)) {
                        sdidataColsPropValues.put("wapstatus", "Pending");
                    }
                    if ((workitemsdef = WorkItemUtil.getDefFromCache(this.database, this.connectionInfo, workItemId, workItemVerId)).size() > 0) {
                        if (wiiRuleToProcess && addAndApplySDIs.getRowCount() != keyid1prop.length) {
                            WorkItemUtil.applyWorkItem(this.transactionid, this.database, this.connectionInfo, this.logger, sdcid, addAndApplySDIs.getColumnValues("keyid1", ";"), addAndApplySDIs.getColumnValues("keyid2", ";"), addAndApplySDIs.getColumnValues("keyid3", ";"), "", workItemId, workItemVerId, wiinstances.substring(1), forcenew, properties.getProperty("matchusersequence", "N"), sdiwiitem, true, sdidataColsPropValues);
                        } else {
                            WorkItemUtil.applyWorkItem(this.transactionid, this.database, this.connectionInfo, this.logger, sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), rsetid, workItemId, workItemVerId, wiinstances.substring(1), forcenew, properties.getProperty("matchusersequence", "N"), sdiwiitem, true, sdidataColsPropValues);
                        }
                    }
                }
                if ((instancelist = properties.getProperty("workiteminstance")).length() > 0) {
                    instancelist = instancelist + ";";
                }
                properties.setProperty("workiteminstance", instancelist + (wiinstances.length() > 0 ? wiinstances.substring(1).toString() : ""));
                if (!workitemtypeflag.equalsIgnoreCase("P")) continue;
                StringBuffer selectWIItems = new StringBuffer();
                safeSQL.reset();
                if ("_Reflex".equals(workItemId)) {
                    String sourceSDC = properties.getProperty("__sourcesdcid");
                    String[] sourceKeyids = StringUtil.split(properties.getProperty("__sourcekeyids"), ";");
                    selectWIItems.append("SELECT '_Reflex' workitemid,").append(" 'S' mandatoryflag,").append(this.database.isOracle() ? "to_char(usersequence)" : "cast( usersequence as varchar )").append(" workitemitemid, 'WorkItem' sdcid, workitemid keyid1,").append("coalesce(  workitemversionid, 'C') keyid2,").append(" keyid3, reflexrule workitemitemrule, s_sampletypeid, usersequence ").append(" FROM sdiworkitem WHERE sdcid = " + safeSQL.addVar(sourceSDC) + " AND keyid1 = " + safeSQL.addVar(sourceKeyids[0]) + " AND keyid2 = " + safeSQL.addVar(sourceKeyids[1]) + " AND keyid3 = " + safeSQL.addVar(sourceKeyids[2])).append(" AND workitemtypeflag = 'W' AND groupid IS null ORDER BY usersequence");
                } else {
                    selectWIItems.append("SELECT workitemid, workitemitemid, sdcid, keyid1,").append("coalesce(  keyid2, 'C') keyid2,").append(" keyid3, mandatoryflag, forcenewflag, workitemitemrule, usersequence ").append(" FROM workitemitem WHERE sdcid = 'WorkItem' AND workitemid = " + safeSQL.addVar(workItemId) + " AND workitemversionid = " + safeSQL.addVar(workItemVerId) + " order by usersequence");
                }
                this.database.createPreparedResultSet("workitemitems", selectWIItems.toString(), safeSQL.getValues());
                DataSet packageItems = new DataSet(this.database.getResultSet("workitemitems"));
                if (packageItems.size() > 0) {
                    PropertyList newProps = new PropertyList(properties);
                    for (int i = 0; i < allEditColList.getRowCount(); ++i) {
                        String columnId = allEditColList.getString(i, "columnid");
                        String val = WorkItemUtil.getSingleValue(columnId, properties.getProperty(columnId, ""), null, -1, wi, ";", workitemidprop.length, "Group WorkItem Ids", this.getTranslationProcessor());
                        if (val == null) continue;
                        newProps.setProperty(columnId, val);
                    }
                    newProps.setProperty("rset", rsetid);
                    newProps.setProperty("workitemid", packageItems.getColumnValues("keyid1", ";"));
                    newProps.setProperty("workitemversionid", packageItems.getColumnValues("keyid2", ";"));
                    newProps.setProperty("sdigroupid", groupidlist.substring(1));
                    newProps.setProperty("sdigroupinstance", groupinstancelist.substring(1));
                    newProps.put("__sdiWIIInstances", sdiWIInstances);
                    newProps.setProperty("workitemitemrule", packageItems.getColumnValues("workitemitemrule", ";"));
                    newProps.setProperty("workitemitemid", packageItems.getColumnValues("workitemitemid", ";"));
                    if (bbChildSamples) {
                        newProps.setProperty("bbchildsample", "Y");
                    }
                    if (bbSamples) {
                        newProps.setProperty("sdiworkitem_sampletypeid", packageItems.getColumnValues("s_sampletypeid", ";"));
                        newProps.setProperty("bbsample", "Y");
                    }
                    this.processAction(newProps);
                    sdiwiMaxUserSeq = newProps.getPropertyList("__sdiwiMaxUserSeq");
                    DataSet childWIs = new DataSet(newProps.getProperty(this.PROPERTY_NEWWORKITEMINSTANCEXML));
                    for (int r = 0; r < childWIs.getRowCount(); ++r) {
                        String itemKeyid1 = childWIs.getString(r, "workitemid");
                        String itemKeyid2 = childWIs.getString(r, "workitemveridprovided", "C");
                        String workitemitemid = childWIs.getString(r, "workitemitemid");
                        HashMap<String, String> findMap = new HashMap<String, String>();
                        findMap.put("keyid1", itemKeyid1);
                        findMap.put("keyid2", itemKeyid2);
                        findMap.put("workitemitemid", workitemitemid);
                        int pkgWIIRow = packageItems.findRow(findMap);
                        if (pkgWIIRow <= -1) continue;
                        int newrow = sdiwiitem.addRow();
                        itemKeyid2 = childWIs.getString(r, "workitemversionid");
                        sdiwiitem.setString(newrow, "sdcid", childWIs.getString(r, "sdcid"));
                        sdiwiitem.setString(newrow, "keyid1", childWIs.getString(r, "keyid1"));
                        sdiwiitem.setString(newrow, "keyid2", childWIs.getString(r, "keyid2"));
                        sdiwiitem.setString(newrow, "keyid3", childWIs.getString(r, "keyid3"));
                        sdiwiitem.setString(newrow, "workitemid", childWIs.getString(r, "groupid"));
                        sdiwiitem.setNumber(newrow, "workiteminstance", childWIs.getInt(r, "groupinstance"));
                        sdiwiitem.setString(newrow, "itemsdcid", "WorkItem");
                        sdiwiitem.setString(newrow, "itemkeyid1", itemKeyid1);
                        sdiwiitem.setString(newrow, "itemkeyid2", itemKeyid2);
                        sdiwiitem.setString(newrow, "itemkeyid3", "(null)");
                        sdiwiitem.setNumber(newrow, "iteminstance", childWIs.getValue(r, "workiteminstance"));
                        sdiwiitem.setString(newrow, "completeflag", "N");
                        sdiwiitem.setString(newrow, "mandatoryflag", packageItems.getString(pkgWIIRow, "mandatoryflag"));
                        sdiwiitem.setString(newrow, "workitemitemid", packageItems.getString(pkgWIIRow, "workitemitemid"));
                        sdiwiitem.setString(newrow, "workitemitemrule", packageItems.getString(pkgWIIRow, "workitemitemrule"));
                        sdiwiitem.setNumber(newrow, "usersequence", packageItems.getValue(pkgWIIRow, "usersequence"));
                    }
                }
                this.database.closeResultSet("workitemitems");
            }
            properties.setProperty(this.PROPERTY_NEWWORKITEMINSTANCEXML, newWIInstances.toXML());
            if (properties.getProperty("tracelogid").trim().length() > 0) {
                sdiwi.setString(-1, "tracelogid", properties.getProperty("tracelogid").trim());
            }
            SDIData sdiData = new SDIData();
            sdiData.setDataset("sdiworkitem", sdiwi);
            sdiData.setDataset("sdiworkitemitem", sdiwiitem);
            sdiData.setDataset("attribute", sdiattributes);
            DataSet appliedSDIWI = new DataSet();
            if (sdiwi != null && sdiwi.getRowCount() > 0) {
                DataSet dsInstances = new DataSet();
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("appliedflag", "Y");
                appliedSDIWI = sdiwi.getFilteredDataSet(filter);
                if (appliedSDIWI.getRowCount() > 0) {
                    if (sdiwiitem != null && sdiwiitem.getRowCount() > 0) {
                        filter.clear();
                        filter.put("itemsdcid", "ParamList");
                        DataSet plSDIWII = sdiwiitem.getFilteredDataSet(filter);
                        for (int p = 0; p < plSDIWII.getRowCount(); ++p) {
                            if (plSDIWII.getValue(p, "iteminstance").length() == 0) continue;
                            dsInstances.copyRow(plSDIWII, p, 1);
                        }
                    }
                    if (allWorkItemInstruments.getRowCount() > 0 || allWorkItemReagents.getRowCount() > 0) {
                        WorkItemUtil.addWorkItemDataSetRelations(dsInstances, appliedSDIWI, this.database, this.getActionProcessor(), allWorkItemReagents, allWorkItemInstruments);
                    }
                    if (allWorkItemAttributes.getRowCount() > 0) {
                        BaseSDIAttributeAction.copyDownWorkItemAttributesToDataSet(sdiattributes, appliedSDIWI, dsInstances, allWorkItemAttributes, this.connectionInfo, this.database, this.getSDCProcessor(), this.logger);
                    }
                }
            }
            BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, "PreAddWorkItem");
            PostAddWorkItemEventObject postAddWorkItemEventObject = new PostAddWorkItemEventObject(sdcid, sdcProps, sdiData, properties);
            SDIData beforeEditImage = null;
            boolean requiresSupplementalData = EventManager.requiresSupplementalData(sapphireConnection, this.getErrorHandler(), postAddWorkItemEventObject);
            if (sdcPreRules.requiresAddWorkItemPrimary() || sdcPreRules.customRulesRequiresAddWorkItemPrimary() || requiresSupplementalData) {
                BaseSDCRules[] sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setRsetid(rsetid);
                sdiRequest.setRetainRsetid(true);
                if (sdcPreRules.requiresAddWorkItemPrimary() || sdcPreRules.customRulesRequiresAddWorkItemPrimary() || requiresSupplementalData) {
                    sdiRequest.setRequestItem("primary");
                }
                SDIProcessor sdiProcessor = this.getSDIProcessor();
                beforeEditImage = sdiProcessor.getSDIData((SDIRequest)sdiRequest);
                sdcPreRules.setBeforeEditImage(beforeEditImage);
                sdiData.setDataset("primary", beforeEditImage.getDataset("primary"));
                postAddWorkItemEventObject.setSupplementalData(beforeEditImage);
                postAddWorkItemEventObject.setRsetid(rsetid);
            }
            Trace.startBusinessRule(sdcid + "." + "PreAddWorkItem", true);
            sdcPreRules.preAddWorkItem(sdiData, properties);
            Trace.endBusinessRule(sdcid + "." + "PreAddWorkItem", true);
            Trace.startBusinessRule(sdcid + "." + "PreAddWorkItem", false);
            for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                customRules.preAddWorkItem(sdiData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PreAddWorkItem", false);
            sdcPreRules.endRule();
            this.logger.info("Processing the sdiworkitem inserts: \n" + sdiwi);
            DataSetUtil.insert(this.database, sdiwi, "sdiworkitem");
            if (sdiattributes.getRowCount() > 0) {
                this.logger.info("Processing the sdiattributes inserts: \n" + sdiattributes);
                DataSetUtil.insert(this.database, sdiattributes, "sdiattribute");
            }
            this.logger.info("Processing the sdiworkitemitems inserts:\n" + sdiwiitem);
            DataSetUtil.insert(this.database, sdiwiitem, "sdiworkitemitem");
            String evaluatedWorkitemitemid = properties.getProperty("__evaluatedworkitemitemidrule");
            if (evaluatedWorkitemitemid.length() > 0) {
                WorkItemItemRuleEvaluator.updatePkgWorkItemSDIWorkItemItem(newWIInstances.toXML(), evaluatedWorkitemitemid, this.database, this.getActionProcessor());
            }
            if (appliedSDIWI.getRowCount() > 0) {
                WorkItemUtil.updateSDIDataWapColumns(appliedSDIWI, this.database, this.getActionProcessor(), this.connectionInfo);
                WorkItemItemRuleEvaluator evalRule = new WorkItemItemRuleEvaluator();
                appliedSDIWI.sort("sdcid, keyid1, keyid2, keyid3");
                ArrayList<DataSet> sdiGroups = appliedSDIWI.getGroupedDataSets("sdcid, keyid1, keyid2, keyid3");
                evalRule.evaluateRule(sdiGroups, sdcid, this.database, this.connectionInfo, sdcProcessor, null, null, null);
            }
            if (addToWorkflow && workItemWorkflowRule != null) {
                WorkItemUtil.addToWorkflow(sdiwi, workItemWorkflowRule, sdcid, this.getActionProcessor());
            }
            WorkItemUtil.createWorksheet(sdiforms, this.getActionProcessor(), this.logger);
            WorkItemUtil.createLESWorksheets(sdiworksheets, this.getActionProcessor(), this.logger);
            if (propsMatchCall) {
                properties.put("__sdidata", sdiData);
            } else {
                EventManager.generateEvent(sapphireConnection, this.getErrorHandler(), postAddWorkItemEventObject);
            }
            BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(sapphireConnection, this.getErrorHandler(), sdcid, sdcProps, "PostAddWorkItem");
            sdcPostRules.setBeforeEditImage(beforeEditImage);
            Trace.startBusinessRule(sdcid + "." + "PostAddWorkItem", true);
            sdcPostRules.postAddWorkItem(sdiData, properties);
            Trace.endBusinessRule(sdcid + "." + "PostAddWorkItem", true);
            Trace.startBusinessRule(sdcid + "." + "PostAddWorkItem", false);
            for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                customRules.postAddWorkItem(sdiData, properties);
            }
            Trace.endBusinessRule(sdcid + "." + "PostAddWorkItem", false);
            sdcPostRules.endRule();
        }
        if (wirsetid.length() > 0) {
            dam.clearRSet(wirsetid);
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
        AddDataSet.endTransactionCache(this.transactionid);
    }

    private void resolveSDIWorkItemTestingDepartment(DataSet sdiwi, int newrow, PropertyList workItemSDCProps, DataSet workItemDS, String testingdepartmentid, String testinglabType, String sdiSiteId, String sdiTestingDepartmentId, String workareadepartmentid, String sdiWorkareaDepartmentId, String workareaType) throws SapphireException {
        DataSet departmentdef;
        DataSet departmentdef2;
        String securityDeptId;
        if (testingdepartmentid.length() > 0) {
            sdiwi.setString(newrow, "testingdepartmentid", testingdepartmentid);
            if (sdiwi.getValue(newrow, "workareadepartmentid").length() == 0 && workareadepartmentid.length() > 0) {
                sdiwi.setString(newrow, "workareadepartmentid", workareadepartmentid);
            }
            return;
        }
        if ("D".equalsIgnoreCase(workItemSDCProps.getProperty("accesscontrolledflag", "")) && (securityDeptId = workItemDS.getString(0, "securitydepartment", "")).length() > 0 && (departmentdef2 = WorkItemUtil.getDepartmentDefFromCache(this.database, this.connectionInfo, securityDeptId)).getRowCount() > 0 && "Y".equalsIgnoreCase(departmentdef2.getValue(0, "testingflag"))) {
            sdiwi.setString(newrow, "testingdepartmentid", securityDeptId);
            return;
        }
        if (testinglabType.length() > 0 && sdiSiteId.length() > 0) {
            this.database.createPreparedResultSet("gettestdept", "SELECT departmentid FROM department WHERE parentdepartmentid = ? AND testinglabtype = ?", new String[]{sdiSiteId, testinglabType});
            DataSet testDept = new DataSet(this.database.getResultSet("gettestdept"));
            if (testDept.getRowCount() > 0) {
                sdiwi.setString(newrow, "testingdepartmentid", testDept.getValue(0, "departmentid"));
                return;
            }
        }
        if (sdiTestingDepartmentId.length() > 0) {
            sdiwi.setString(newrow, "testingdepartmentid", sdiTestingDepartmentId);
            if (sdiwi.getValue(newrow, "workareadepartmentid").length() == 0 && sdiWorkareaDepartmentId.length() > 0) {
                sdiwi.setString(newrow, "workareadepartmentid", sdiWorkareaDepartmentId);
            }
            return;
        }
        this.database.createPreparedResultSet("getuserdefaulttestlab", "select departmentid from departmentsysuser where sysuserid = ? and defaulttestinglabflag = 'Y'", new String[]{this.connectionInfo.getSysuserId()});
        DataSet dsUserDefaultTestingLab = new DataSet(this.database.getResultSet("getuserdefaulttestlab"));
        if (dsUserDefaultTestingLab.getRowCount() > 0) {
            sdiwi.setString(newrow, "testingdepartmentid", dsUserDefaultTestingLab.getValue(0, "departmentid"));
            return;
        }
        String userDefaultDepartmentId = this.connectionInfo.getDefaultDepartment();
        if (userDefaultDepartmentId != null && userDefaultDepartmentId.length() > 0 && (departmentdef = WorkItemUtil.getDepartmentDefFromCache(this.database, this.connectionInfo, userDefaultDepartmentId)).getRowCount() > 0 && "Y".equalsIgnoreCase(departmentdef.getValue(0, "testingflag"))) {
            sdiwi.setString(newrow, "testingdepartmentid", userDefaultDepartmentId);
            return;
        }
    }

    private void sanitizeProperties(DataSet propsDS) {
        if (propsDS == null || propsDS.getRowCount() == 0) {
            return;
        }
        for (int i = 0; i < propsDS.getRowCount(); ++i) {
            if (!propsDS.isNull(i, "workitemversionid")) continue;
            propsDS.setString(i, "workitemversionid", "C");
        }
    }

    private void setSDIWIInstance(PropertyList sdiwiInstances, String sdcid, String keyid1, String keyid2, String keyid3, String workitemId, String workitemInstance) {
        String key = sdcid + ";" + keyid1 + ";" + ("".equals(keyid2) ? "(null)" : keyid2) + ";" + ("".equals(keyid3) ? "(null)" : keyid3) + ";" + workitemId;
        String existingInstance = sdiwiInstances.getProperty(key, "");
        if (existingInstance.length() == 0 || Integer.parseInt(existingInstance) < Integer.parseInt(workitemInstance)) {
            sdiwiInstances.setProperty(key, workitemInstance);
        }
    }

    private String getSDIWIIInstance(PropertyList sdiwiInstances, String sdcid, String keyid1, String keyid2, String keyid3, String workitemId) {
        String key = sdcid + ";" + keyid1 + ";" + ("".equals(keyid2) ? "(null)" : keyid2) + ";" + ("".equals(keyid3) ? "(null)" : keyid3) + ";" + workitemId;
        return sdiwiInstances.get(key) == null ? "" : (String)sdiwiInstances.get(key);
    }

    private DataSet copyDataSet(DataSet sourceDS, DataSet destDS) {
        for (int i = 0; i < sourceDS.size(); ++i) {
            destDS.copyRow(sourceDS, i, 1);
        }
        return destDS;
    }

    private String getWorkItemVersionId(String[] workitemversionidprop, int counter, String[] workitemversionidcurrprop) throws SapphireException {
        try {
            String workItemVersionId = "";
            int wivPropLen = workitemversionidprop.length;
            workItemVersionId = wivPropLen == 0 || wivPropLen <= counter || workitemversionidprop[counter].length() == 0 || "C".equalsIgnoreCase(workitemversionidprop[counter]) ? (workitemversionidcurrprop != null ? workitemversionidcurrprop[counter] : "") : workitemversionidprop[counter];
            return workItemVersionId;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    private String getSDIId(String sdcId, String keyId1, String keyId2, String keyId3) {
        String sdiId = sdcId + ";" + keyId1 + ";" + ("(null".equalsIgnoreCase(keyId2) ? "" : keyId2) + ";" + ("(null".equalsIgnoreCase(keyId3) ? "" : keyId3);
        return sdiId;
    }
}

