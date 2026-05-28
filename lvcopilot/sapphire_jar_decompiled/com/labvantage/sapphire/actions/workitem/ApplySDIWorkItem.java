/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workitem;

import com.labvantage.opal.actions.CopySDIDetail;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.actions.sdidata.AddDataSet;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.actions.sms.ApplyWorkItemChildSamplePlan;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.WorkItemItemRuleEvaluator;
import com.labvantage.sapphire.util.cache.CacheNames;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ApplySDIWorkItem
extends BaseAction
implements CacheNames,
sapphire.action.ApplySDIWorkItem {
    String TEMP_CURRENTVERSIONID = "_currentversionid";
    private Map<String, Boolean> wicache = new HashMap<String, Boolean>();

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        DataSet sdiworkitems = new DataSet();
        DataSet sdiwi = new DataSet();
        DataSet sdiwiitem = new DataSet();
        DataSet sdiforms = new DataSet();
        DataSet sdiworksheets = new DataSet();
        DataSet sdiattributes = new DataSet();
        boolean deleterset = false;
        String rsetid = properties.getProperty("rsetid");
        String wirsetid = "";
        DAMProcessor dam = this.getDAMProcessor();
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rsetid = BaseSDIDataAction.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), this.database, this.connectionInfo, applylock);
            deleterset = true;
        }
        SafeSQL safeSQL = new SafeSQL();
        String selectWorkitems = "SELECT\tsdiworkitem.* FROM\tsdiworkitem, rsetitems WHERE\trsetitems.sdcid = " + safeSQL.addVar(sdcid) + " AND \t\trsetitems.rsetid = " + safeSQL.addVar(rsetid) + " AND \t\trsetitems.sdcid = sdiworkitem.sdcid AND \t\trsetitems.keyid1 = sdiworkitem.keyid1 AND \t\trsetitems.keyid2 = sdiworkitem.keyid2 AND \t\trsetitems.keyid3 = sdiworkitem.keyid3 " + (properties.getProperty("workitemid").length() > 0 ? " AND sdiworkitem.workitemid in (" + safeSQL.addIn(properties.getProperty("workitemid"), ";") + ") " : "") + " ORDER BY sdiworkitem.keyid1, sdiworkitem.keyid2, sdiworkitem.keyid3, sdiworkitem.usersequence, sdiworkitem.appliedflag, sdiworkitem.workitemtypeflag, sdiworkitem.groupid, sdiworkitem.groupinstance";
        this.database.createPreparedResultSet(selectWorkitems, safeSQL.getValues());
        sdiworkitems.setResultSet(this.database.getResultSet());
        int sdiworkitemcount = sdiworkitems.getRowCount();
        if (sdiworkitemcount > 0) {
            String currentWorkItemVersions = SdiInfo.getCurrentVersion("WorkItem", sdiworkitems.getColumnValues("workitemid", ";"), null, new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            sdiworkitems.addColumn(this.TEMP_CURRENTVERSIONID, 0);
            sdiworkitems.addColumnValues(this.TEMP_CURRENTVERSIONID, 0, currentWorkItemVersions, ";");
        }
        DataSet search = new DataSet();
        boolean hasInstanceProps = false;
        if (properties.getProperty("workitemid").length() > 0) {
            search.addColumnValues("workitemid", 0, properties.getProperty("workitemid"), ";");
            if (properties.getProperty("workiteminstance").length() > 0) {
                search.addColumnValues("workiteminstance", 0, properties.getProperty("workiteminstance"), ";", "1");
                hasInstanceProps = true;
            }
            search.padColumns();
        }
        boolean createWorksheetInputProp = "Y".equals(StringUtil.getYN(properties.getProperty("createworksheet", ""), "Y"));
        DataSet workItemForms = null;
        DataSet workItemWorksheets = new DataSet();
        if (createWorksheetInputProp && sdiworkitemcount > 0) {
            String workitemVersions = "";
            for (int w = 0; w < sdiworkitemcount; ++w) {
                workitemVersions = "".equals(sdiworkitems.getValue(w, "workitemversionid")) || "C".equalsIgnoreCase(sdiworkitems.getValue(w, "workitemversionid")) ? workitemVersions + ";" + sdiworkitems.getValue(w, this.TEMP_CURRENTVERSIONID) : workitemVersions + ";" + sdiworkitems.getValue(w, "workitemversionid");
            }
            if (workitemVersions.length() > 0) {
                workitemVersions = workitemVersions.substring(1);
            }
            String rsetWorkItems = this.getDAMProcessor().createRSet("WorkItem", sdiworkitems.getColumnValues("workitemid", ";"), workitemVersions, "", true, 1);
            workItemForms = WorkItemUtil.getWorkItemForms(sdiworkitems.getColumnValues("workitemid", ";"), workitemVersions, this.getQueryProcessor(), this.logger, rsetWorkItems);
            workItemWorksheets = WorkItemUtil.getWorkItemWorksheets(sdiworkitems.getColumnValues("workitemid", ";"), workitemVersions, this.getQueryProcessor(), this.logger, rsetWorkItems);
            dam.clearRSet(rsetWorkItems);
        } else {
            workItemForms = new DataSet();
        }
        boolean createWorksheet = true;
        String createWorkSheetRule = "";
        String formId = "";
        String formVersionId = "";
        boolean addToWorkflow = true;
        DataSet workItemWorkflowRule = null;
        if (addToWorkflow && sdiworkitemcount > 0) {
            String rsetWorkItems = this.getDAMProcessor().createRSet("WorkItem", sdiworkitems.getColumnValues("workitemid", ";"), sdiworkitems.getColumnValues("workitemversionid", ";"), "", true, 1);
            workItemWorkflowRule = WorkItemUtil.getWorkItemWorkflowRules(sdiworkitems.getColumnValues("workitemid", ";"), sdiworkitems.getColumnValues("workitemversionid", ";"), this.getQueryProcessor(), this.logger, rsetWorkItems);
            dam.clearRSet(rsetWorkItems);
        }
        HashMap<String, String> findmap = new HashMap<String, String>();
        PropertyList extraProps = new PropertyList();
        Calendar now = DateTimeUtil.getNowCalendar();
        for (int wi = 0; wi < sdiworkitemcount; ++wi) {
            String transactionid = this.connectionInfo.getSysuserId() + System.currentTimeMillis();
            AddDataSet.startTransactionCache(transactionid);
            if (sdiworkitems.getString(wi, "appliedflag").equals("N")) {
                String workitemid = sdiworkitems.getString(wi, "workitemid");
                String workitemversionid = sdiworkitems.getString(wi, "workitemversionid", sdiworkitems.getString(wi, this.TEMP_CURRENTVERSIONID));
                int workiteminstance = sdiworkitems.getInt(wi, "workiteminstance");
                findmap.put("workitemid", workitemid);
                if (hasInstanceProps) {
                    findmap.put("workiteminstance", sdiworkitems.getValue(wi, "workiteminstance"));
                }
                if (search.size() == 0 || search.findRow(findmap) >= 0) {
                    DataSet workitemsdef;
                    String testingdepartmentid;
                    if (sdiworkitems.getString(wi, "workitemtypeflag").equals("P")) {
                        for (int packageitem = wi + 1; packageitem < sdiworkitemcount && sdiworkitems.getString(wi, "groupid").equals(sdiworkitems.getString(packageitem, "groupid")) && sdiworkitems.getInt(wi, "groupinstance") == sdiworkitems.getInt(packageitem, "groupinstance"); ++packageitem) {
                            int newrow = sdiwiitem.addRow();
                            sdiwiitem.setString(newrow, "workitemitemid", "WI" + String.valueOf(packageitem));
                            sdiwiitem.setString(newrow, "workitemid", sdiworkitems.getString(wi, "workitemid"));
                            sdiwiitem.setNumber(newrow, "workiteminstance", sdiworkitems.getInt(wi, "workiteminstance"));
                            sdiwiitem.setString(newrow, "sdcid", sdcid);
                            sdiwiitem.setString(newrow, "keyid1", sdiworkitems.getString(wi, "keyid1"));
                            sdiwiitem.setString(newrow, "keyid2", sdiworkitems.getString(wi, "keyid2"));
                            sdiwiitem.setString(newrow, "keyid3", sdiworkitems.getString(wi, "keyid3"));
                            sdiwiitem.setString(newrow, "completeflag", "N");
                            sdiwiitem.setString(newrow, "mandatoryflag", "N");
                            sdiwiitem.setString(newrow, "itemsdcid", "WorkItem");
                            sdiwiitem.setString(newrow, "itemkeyid1", sdiworkitems.getString(packageitem, "workitemid"));
                            sdiwiitem.setString(newrow, "itemkeyid2", "(null)");
                            sdiwiitem.setString(newrow, "itemkeyid3", "(null)");
                            sdiwiitem.setNumber(newrow, "iteminstance", sdiworkitems.getInt(packageitem, "workiteminstance"));
                        }
                    }
                    boolean createLESWorksheet = false;
                    String worksheetId = properties.getProperty("worksheetid");
                    String worksheetVersionId = properties.getProperty("worksheetversionid");
                    String workbookId = properties.getProperty("workbookid");
                    String workbookVersionId = properties.getProperty("workbookversionid");
                    String lesCreateFlag = "";
                    String lesAuthorFlag = "";
                    if (createWorksheetInputProp) {
                        HashMap<String, String> formFilterMap = new HashMap<String, String>();
                        formFilterMap.put("workitemid", sdiworkitems.getString(wi, "workitemid"));
                        formFilterMap.put("workitemversionid", workitemversionid);
                        DataSet workItemForm = workItemForms.getFilteredDataSet(formFilterMap);
                        createWorkSheetRule = workItemForm.getString(0, "createworksheetrule", "");
                        String assignedAnalyst = sdiworkitems.getString(wi, "s_assignedanalyst", "");
                        String assignedDept = sdiworkitems.getString(wi, "s_assigneddepartment", "");
                        createWorksheet = "On Creation".equalsIgnoreCase(createWorkSheetRule) || "On Assignment".equalsIgnoreCase(createWorkSheetRule) && (assignedAnalyst.length() > 0 || assignedDept.length() > 0);
                        HashMap<String, String> worksheetFilterMap = new HashMap<String, String>();
                        worksheetFilterMap.put("workitemid", workitemid);
                        worksheetFilterMap.put("workitemversionid", workitemversionid);
                        worksheetFilterMap.put("worksheetrule", "default");
                        DataSet workItemWorksheet = workItemWorksheets.getFilteredDataSet(worksheetFilterMap);
                        if (workItemWorksheet.size() > 0) {
                            lesCreateFlag = workItemWorksheet.getString(0, "createlesrule", "");
                            lesAuthorFlag = workItemWorksheet.getString(0, "authorflag");
                            createLESWorksheet = "On Creation".equalsIgnoreCase(lesCreateFlag);
                            if (worksheetId.length() == 0) {
                                worksheetId = workItemWorksheet.getString(0, "worksheetid", "");
                                worksheetVersionId = workItemWorksheet.getString(0, "worksheetversionid", "");
                            }
                            if (workbookId.length() == 0) {
                                workbookId = workItemWorksheet.getString(0, "workbookid", "");
                                workbookVersionId = workItemWorksheet.getString(0, "workbookversionid", "");
                            }
                        }
                        if (createLESWorksheet) {
                            String keyid1 = sdiworkitems.getString(wi, "keyid1");
                            String keyid2 = sdiworkitems.getString(wi, "keyid2");
                            String keyid3 = sdiworkitems.getString(wi, "keyid3");
                            String sdiworkitemid = sdiworkitems.getString(wi, "sdiworkitemid");
                            WorkItemUtil.addWorksheets(sdiworksheets, sdcid, keyid1, keyid2, keyid3, workitemid, String.valueOf(workiteminstance), worksheetId, worksheetVersionId, workbookId, workbookVersionId, "A".equals(lesAuthorFlag) ? assignedAnalyst : "", sdiworkitemid, this.getTranslationProcessor());
                            extraProps.setProperty("createworksheet", "N");
                        }
                        if (createWorksheet) {
                            formId = properties.getProperty("formid", "");
                            formVersionId = properties.getProperty("formversionid", "");
                            formFilterMap.put("formrule", "default");
                            int row = workItemForm.findRow(formFilterMap);
                            if (formId.length() == 0 && row > -1) {
                                formId = workItemForm.getString(row, "formid", "");
                                formVersionId = workItemForm.getString(row, "formversionid", "");
                            }
                            WorkItemUtil.addDataForms(sdiforms, sdcid, sdiworkitems.getString(wi, "keyid1"), sdiworkitems.getString(wi, "keyid2"), sdiworkitems.getString(wi, "keyid3"), sdiworkitems.getString(wi, "workitemid"), String.valueOf(workiteminstance), formId, formVersionId, sdiworkitems.getString(wi, "s_assignedanalyst", ""), sdiworkitems.getString(wi, "s_assigneddepartment", ""), this.getTranslationProcessor());
                            extraProps.setProperty("createworksheet", "N");
                        }
                        if (!createWorksheet && !createLESWorksheet) {
                            extraProps.setProperty("createworksheet", "Y");
                        }
                    } else {
                        extraProps.setProperty("createworksheet", "N");
                    }
                    DataSet testMethodDef = WorkItemUtil.getTestMethodDefFromCache(this.database, this.connectionInfo, workitemid, workitemversionid);
                    String securityDept = properties.getProperty("securitydepartment", "");
                    String securityUser = properties.getProperty("securityuser", "");
                    if ("D".equalsIgnoreCase(sdcProcessor.getProperty("WorkItem", "accesscontrolledflag", "")) && securityDept.length() == 0) {
                        securityDept = testMethodDef.getString(0, "securitydepartment", "");
                    }
                    if (securityDept.length() > 0) {
                        extraProps.put("securitydepartment", securityDept);
                    }
                    if (securityUser.length() > 0) {
                        extraProps.put("securityuser", securityUser);
                    }
                    if ((testingdepartmentid = sdiworkitems.getString(wi, "testingdepartmentid", "")).length() > 0) {
                        extraProps.put("testingdepartmentid", testingdepartmentid);
                    }
                    if (testMethodDef.getRowCount() > 0 && "On Demand By DataSet".equalsIgnoreCase(testMethodDef.getValue(0, "createactivityrule"))) {
                        extraProps.put("wapstatus", "Pending");
                    }
                    if ((workitemsdef = WorkItemUtil.getDefFromCache(this.database, this.connectionInfo, workitemid, workitemversionid)).size() > 0) {
                        WorkItemUtil.applyWorkItem(transactionid, this.database, this.connectionInfo, this.logger, sdcid, sdiworkitems.getString(wi, "keyid1"), sdiworkitems.getString(wi, "keyid2"), sdiworkitems.getString(wi, "keyid3"), "", workitemid, workitemversionid, "" + workiteminstance, properties.getProperty("forcenew"), properties.getProperty("matchusersequence", "N"), sdiwiitem, true, extraProps);
                    }
                    int updaterow = sdiwi.addRow();
                    sdiwi.setString(updaterow, "sdcid", sdcid);
                    sdiwi.setString(updaterow, "keyid1", sdiworkitems.getString(wi, "keyid1"));
                    sdiwi.setString(updaterow, "keyid2", sdiworkitems.getString(wi, "keyid2"));
                    sdiwi.setString(updaterow, "keyid3", sdiworkitems.getString(wi, "keyid3"));
                    sdiwi.setString(updaterow, "sdiworkitemid", sdiworkitems.getString(wi, "sdiworkitemid"));
                    sdiwi.setString(updaterow, "workitemid", workitemid);
                    sdiwi.setString(updaterow, "workitemversionid", workitemversionid);
                    sdiwi.setNumber(updaterow, "workiteminstance", workiteminstance);
                    sdiwi.setString(updaterow, "appliedflag", "Y");
                    sdiwi.setString(updaterow, "appliedby", "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId());
                    sdiwi.setDate(updaterow, "applieddt", now);
                }
            }
            AddDataSet.endTransactionCache(transactionid);
        }
        if (sdiwi.size() > 0) {
            String reason = properties.getProperty("auditreason");
            String activity = properties.getProperty("auditactivity");
            String signedFlag = properties.getProperty("auditsignedflag");
            String auditdt = properties.getProperty("auditdt");
            if (reason.length() > 0 && properties.getProperty("tracelogid", "").trim().length() == 0) {
                this.logger.info("Generate the tracelog record");
                PropertyList tracelogprops = new PropertyList();
                tracelogprops.setProperty("sdcid", sdcid);
                tracelogprops.setProperty("description", "Applied workitems");
                tracelogprops.setProperty("auditreason", reason);
                tracelogprops.setProperty("auditactivity", activity);
                tracelogprops.setProperty("auditsignedflag", signedFlag);
                tracelogprops.setProperty("auditdt", auditdt);
                ActionProcessor ap = this.getActionProcessor();
                ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                int tracelogid = Integer.parseInt(tracelogprops.getProperty("tracelogid"));
                sdiwi.setString(-1, "tracelogid", String.valueOf(tracelogid));
                properties.setProperty("tracelogid", String.valueOf(tracelogid));
            }
            if (properties.getProperty("tracelogid", "").trim().length() > 0) {
                sdiwi.setString(-1, "tracelogid", properties.getProperty("tracelogid").trim());
            }
            this.logger.info("Processing the sdiworkitem updates: " + sdiwi);
            String[] keycols = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "workitemid", "workiteminstance"};
            DataSetUtil.update(this.database, sdiwi, "sdiworkitem", keycols);
            this.logger.info("Processing the sdiworkitemitems inserts: " + sdiwiitem);
            DataSetUtil.insert(this.database, sdiwiitem, "sdiworkitemitem");
            String evaluatedWorkitemitemid = properties.getProperty("__evaluatedworkitemitemidrule");
            if (evaluatedWorkitemitemid.length() > 0) {
                WorkItemItemRuleEvaluator.updatePackageWorkItemStatus(sdiwi, this.database, this.getActionProcessor());
            }
            WorkItemUtil.updateSDIDataWapColumns(sdiworkitems, this.database, this.getActionProcessor(), this.connectionInfo);
            WorkItemItemRuleEvaluator evalRule = new WorkItemItemRuleEvaluator();
            ArrayList<DataSet> sdiGroups = sdiworkitems.getGroupedDataSets("sdcid, keyid1, keyid2, keyid3");
            evalRule.evaluateRule(sdiGroups, sdcid, this.database, this.connectionInfo, sdcProcessor, null, null, null);
            if (sdiwi != null && sdiwi.getRowCount() > 0) {
                PreparedStatement getworkitemDataSetAttributes = this.database.prepareStatement("getworkitemdatasetattributes", "SELECT * FROM sdiattribute WHERE sdcid = ? and keyid1 = ? and keyid2 = ? and attributesdcid = ?");
                PreparedStatement getworkitemAttributes = this.database.prepareStatement("getworkitemattributes", "SELECT wia.* FROM sdiattribute wia WHERE wia.sdcid = ? and wia.keyid1 = ? and wia.keyid2 = ? AND wia.attributesdcid = ? AND coalesce( nullif( wia.attributetypeflag,''), 'E') !='S' AND NOT exists (SELECT 1 FROM sdiattribute WHERE attributeid = wia.attributeid  AND attributeinstance = wia.attributeinstance AND sdcid='SDIWorkItem' AND keyid1 = ?)");
                PreparedStatement getworkitemReagents = this.database.prepareStatement("getworkitemreagents", "SELECT * FROM workitemreagenttype WHERE workitemid = ? AND workitemversionid = ?");
                PreparedStatement getworkitemInstruments = this.database.prepareStatement("getworkiteminstruments", "SELECT * FROM workiteminstrument WHERE workitemid = ? AND workitemversionid = ?");
                DataSet allWorkItemDataSetAttributes = new DataSet();
                DataSet allWorkItemReagents = new DataSet();
                DataSet allWorkItemInstruments = new DataSet();
                DataSet allExistingTargetAttributes = new DataSet();
                DataSet sdiwiattributesToAdd = new DataSet();
                try {
                    for (int i = 0; i < sdiwi.getRowCount(); ++i) {
                        String workitemId = sdiwi.getValue(i, "workitemid");
                        String workitemVersionId = sdiwi.getValue(i, "workitemversionid");
                        String sdiworkitemid = sdiwi.getValue(i, "sdiworkitemid");
                        HashMap<String, String> findWorkItem = new HashMap<String, String>();
                        findWorkItem.put("keyid1", workitemId);
                        findWorkItem.put("keyid2", workitemVersionId);
                        DataSet workitemDataSetattributes = allWorkItemDataSetAttributes.getFilteredDataSet(findWorkItem);
                        if (workitemDataSetattributes.getRowCount() == 0) {
                            getworkitemDataSetAttributes.setString(1, "WorkItem");
                            getworkitemDataSetAttributes.setString(2, workitemId);
                            getworkitemDataSetAttributes.setString(3, workitemVersionId);
                            getworkitemDataSetAttributes.setString(4, "DataSet");
                            workitemDataSetattributes = new DataSet(getworkitemDataSetAttributes.executeQuery());
                            allWorkItemDataSetAttributes.copyRow(workitemDataSetattributes, -1, 1);
                        }
                        getworkitemAttributes.setString(1, "WorkItem");
                        getworkitemAttributes.setString(2, workitemId);
                        getworkitemAttributes.setString(3, workitemVersionId);
                        getworkitemAttributes.setString(4, "SDIWorkItem");
                        getworkitemAttributes.setString(5, sdiworkitemid);
                        DataSet workitemTestattributes = new DataSet(getworkitemAttributes.executeQuery());
                        DataSet targetExistingAttributes = this.getQueryProcessor().getPreparedSqlDataSet("select a.* from sdiattribute a where a.sdcid = ? and  a.keyid1 = ?  ", (Object[])new String[]{"SDIWorkItem", sdiworkitemid}, true);
                        allExistingTargetAttributes.copyRow(targetExistingAttributes, -1, 1);
                        findWorkItem.clear();
                        findWorkItem.put("workitemid", workitemId);
                        findWorkItem.put("workitemversionid", workitemVersionId);
                        if (allWorkItemReagents.getRowCount() == 0 || allWorkItemReagents.getFilteredDataSet(findWorkItem).getRowCount() == 0) {
                            getworkitemReagents.setString(1, workitemId);
                            getworkitemReagents.setString(2, workitemVersionId);
                            DataSet workitemReagents = new DataSet(getworkitemReagents.executeQuery());
                            allWorkItemReagents.copyRow(workitemReagents, -1, 1);
                        }
                        if (allWorkItemInstruments.getRowCount() == 0 || allWorkItemInstruments.getFilteredDataSet(findWorkItem).getRowCount() == 0) {
                            getworkitemInstruments.setString(1, workitemId);
                            getworkitemInstruments.setString(2, workitemVersionId);
                            DataSet workitemInstruments = new DataSet(getworkitemInstruments.executeQuery());
                            allWorkItemInstruments.copyRow(workitemInstruments, -1, 1);
                        }
                        if (workitemTestattributes.getRowCount() <= 0) continue;
                        HashMap<String, ArrayList<String>> hmskipped = new HashMap<String, ArrayList<String>>();
                        BaseSDIAttributeAction.coreCopyDownAttributes(sdiwiattributesToAdd, workitemTestattributes, this.getSDCProcessor().getPropertyList("SDIWorkItem"), sdiworkitemid, "", "", hmskipped, new M18NUtil(this.connectionInfo), this.logger);
                        BaseSDIAttributeAction.logSkipped(hmskipped, "SDIWorkItem", this.logger);
                    }
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
                finally {
                    this.database.closeStatement("getworkitemdatasetattributes");
                    this.database.closeStatement("getworkitemattributes");
                    this.database.closeStatement("getworkitemreagents");
                    this.database.closeStatement("getworkiteminstruments");
                }
                if (sdiwiattributesToAdd.getRowCount() > 0) {
                    this.logger.info("Processing the TestMethod sdiattributes inserts: \n" + sdiattributes);
                    CopySDIDetail.addEditAttributes(sdiwiattributesToAdd, new DataSet(), allExistingTargetAttributes, this.logger, this.database);
                }
                if (allWorkItemDataSetAttributes.getRowCount() > 0) {
                    BaseSDIAttributeAction.copyDownWorkItemAttributesToDataSet(sdiattributes, sdiwi, sdiwiitem, allWorkItemDataSetAttributes, this.connectionInfo, this.database, this.getSDCProcessor(), this.logger);
                }
                if (allWorkItemInstruments.getRowCount() > 0 || allWorkItemReagents.getRowCount() > 0) {
                    WorkItemUtil.addWorkItemDataSetRelations(sdiwiitem, sdiwi, this.database, this.getActionProcessor(), allWorkItemReagents, allWorkItemInstruments);
                }
            }
            if (sdiattributes.getRowCount() > 0) {
                this.logger.info("Processing the dataset sdiattributes inserts: \n" + sdiattributes);
                DataSetUtil.insert(this.database, sdiattributes, "sdiattribute");
            }
            HashSet<String> sdiworkitemidset = new HashSet<String>();
            for (int i = 0; i < sdiwi.size(); ++i) {
                String workitemversionid;
                String workitemid = sdiwi.getString(i, "workitemid");
                if (!this.applyChildPlanOnApply(workitemid, workitemversionid = sdiwi.getString(i, "workitemversionid"))) continue;
                sdiworkitemidset.add(sdiwi.getString(i, "sdiworkitemid"));
            }
            if (sdiworkitemidset.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdiworkitemid", OpalUtil.toDelimitedString(sdiworkitemidset, ";"));
                this.getActionProcessor().processActionClass(ApplyWorkItemChildSamplePlan.class.getName(), props);
            }
            if (addToWorkflow && workItemWorkflowRule != null) {
                WorkItemUtil.addToWorkflow(sdiwi, workItemWorkflowRule, sdcid, this.getActionProcessor());
            }
            if (sdiworksheets.getRowCount() > 0) {
                WorkItemUtil.createLESWorksheets(sdiworksheets, this.getActionProcessor(), this.logger);
            }
            if (sdiforms.getRowCount() > 0) {
                WorkItemUtil.createWorksheet(sdiforms, this.getActionProcessor(), this.logger);
            }
        }
        if (wirsetid.length() > 0) {
            dam.clearRSet(wirsetid);
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }

    private boolean applyChildPlanOnApply(String workitemid, String workitemversionid) {
        String key = workitemid + workitemversionid;
        if (!this.wicache.containsKey(key)) {
            boolean apply = false;
            StringBuilder sql = new StringBuilder();
            sql.append("select supportembeddedchildplanflag, applychildplanonapplywiflag from workitem where workitemid = ? and workitemversionid = ?");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{workitemid, workitemversionid});
            if (ds != null && ds.size() > 0) {
                apply = "Y".equals(ds.getString(0, "supportembeddedchildplanflag")) && "Y".equals(ds.getString(0, "applychildplanonapplywiflag"));
            }
            this.wicache.put(key, apply);
        }
        return this.wicache.get(key);
    }
}

