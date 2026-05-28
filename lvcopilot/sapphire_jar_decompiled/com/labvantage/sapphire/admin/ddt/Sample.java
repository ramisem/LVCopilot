/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.actions.CopySDIDetail;
import com.labvantage.opal.actions.SyncSDIDataSetStatus;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.FormUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import com.labvantage.sapphire.actions.eln.GenerateSampleWorksheet;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdi.DeleteSDISecuritySet;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.actions.sdidata.EditDataSet;
import com.labvantage.sapphire.actions.sms.ApplyChildSamplePlan;
import com.labvantage.sapphire.actions.sms.ApplyWorkItemChildSamplePlan;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.actions.wap.RemoveActivityWorkSDI;
import com.labvantage.sapphire.actions.wap.SetActivityStatus;
import com.labvantage.sapphire.actions.wap.UpdateActivityCompleteCount;
import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import com.labvantage.sapphire.actions.workitem.EditSDIWorkItem;
import com.labvantage.sapphire.actions.workitem.WorkItemUtil;
import com.labvantage.sapphire.admin.ddt.BatchLifeCycleUtil;
import com.labvantage.sapphire.admin.ddt.LV_BatchStage;
import com.labvantage.sapphire.admin.ddt.LV_MonitorGroup;
import com.labvantage.sapphire.admin.ddt.RequestManagementUtil;
import com.labvantage.sapphire.admin.ddt.RuleUtil;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.Study;
import com.labvantage.sapphire.admin.ddt.misc.WhoDoneIt;
import com.labvantage.sapphire.admin.ddt.rules.ActiveRCRule;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.admin.ddt.rules.EventLog;
import com.labvantage.sapphire.admin.ddt.rules.GLPRule;
import com.labvantage.sapphire.ajax.operations.GetForceNewPolicy;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.array.ArrayUtil;
import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Sample
extends BaseSDCRules {
    public static final String SDC_SAMPLE = "Sample";
    public static final String COLUMN_KEYID1 = "s_sampleid";
    public static final String COLUMN_AUTORECEIVEFLAG = "autoreceiveflag";
    public static final String COLUMN_STORAGESTATUS = "storagestatus";
    public static final String COLUMN_PREVSTORAGESTATUS = "previousstoragestatus";
    public static final String COLUMN_SAMPLESTATUS = "samplestatus";
    public static final String COLUMN_DISPOSALSTATUS = "disposalstatus";
    public static final String COLUMN_RECEIVEREQUIRED = "receiverequiredflag";
    public static final String COLUMN_RECEIVEDDT = "receiveddt";
    public static final String COLUMN_RECEIVEDBY = "receivedby";
    public static final String COLUMN_REVIEWREQUIRED = "reviewrequiredflag";
    public static final String COLUMN_TEMPLATEFLAG = "templateflag";
    public static final String COLUMN_STARTTESTINGDT = "starttestingdt";
    public static final String COLUMN_SAMPLETYPEID = "sampletypeid";
    public static final String COLUMN_SAMPLESUBTYPEID = "samplesubtypeid";
    public static final String COLUMN_REQUESTID = "requestid";
    public static final String COLUMN_REQUESTITEMID = "requestitemid";
    public static final String COLUMN_REQUESTITEMDETAILID = "requestitemdetailid";
    public static final String COLUMN_CLASSIFICATION = "classification";
    public static final String COLUMN_MONITORGROUPID = "monitorgroupid";
    public static final String COLUMN_COLLECTIONDT = "collectiondt";
    public static final String COLUMN_COMPLETEDT = "completedt";
    public static final String COLUMN_REVIEWEDBY = "reviewedby";
    public static final String COLUMN_REVIEWEDDT = "revieweddt";
    public static final String COLUMN_REVIEWDISPOSITION = "reviewdisposition";
    public static final String COLUMN_DISPOSALDT = "disposaldt";
    public static final String COLUMN_DISPOSEDBY = "disposedby";
    public static final String COLUMN_CONFIRMEDDT = "confirmeddt";
    public static final String COLUMN_CONFIRMEDBY = "confirmedby";
    public static final String COLUMN_CANCELLEDDT = "cancelleddt";
    public static final String COLUMN_CANCELLEDBY = "cancelledby";
    public static final String COLUMN_PROCESSINGSTARTDT = "processingstartdt";
    public static final String COLUMN_PROCESSINGENDDT = "processingenddt";
    public static final String COLUMN_DUEDT = "duedt";
    public static final String COLUMN_SUBMITTEDT = "submitteddt";
    public static final String COLUMN_DUEDTOVERRIDEFLAG = "duedtoverrideflag";
    public static final String STORAGESTATUS_DISPOSED = "Disposed";
    public static final String STORAGESTATUS_INCIRCULATION = "In Circulation";
    public static final String STORAGESTATUS_RECEIVED = "Received";
    public static final String STORAGESTATUS_VERIFICATIONNEEDED = "Verification Needed";
    public static final String STORAGESTATUS_ARCHIVED = "Archived";
    public static final String STORAGESTATUS_ALLOCATED = "Allocated";
    public static final String STORAGESTATUS_TEMPORARYINLAB = "Temporary In Lab";
    public static final String STORAGESTATUS_INPREP = "In Prep";
    public static final String STORAGESTATUS_THIRDPARTYTRANSFER = "3rd Party Transfer";
    public static final String SAMPLESTATUS_INITIAL = "Initial";
    public static final String SAMPLESTATUS_RECEIVED = "Received";
    public static final String SAMPLESTATUS_INPROGRESS = "InProgress";
    public static final String SAMPLESTATUS_COMPLETED = "Completed";
    public static final String SAMPLESTATUS_REVIEWED = "Reviewed";
    public static final String SAMPLESTATUS_REPORTED = "Reported";
    public static final String SAMPLESTATUS_CANCELLED = "Cancelled";
    public static final String SAMPLESTATUS_DISPOSED = "Disposed";
    public static final String CLASSIFICATION_BIOBANKING = "BioBanking";
    public static final String CLASSIFICATION_STABILITY = "Stability";
    public static final String CLASSIFICATION_QM = "QM";
    public static final String CLASSIFICATION_REQUEST = "Request";
    public static final String CLASSIFICATION_REAGENTQUALITY = "ReagentQuality";
    public static final String CLASSIFICATION_QCSAMPLE = "QCSample";
    public static final String CLASSIFICATION_MONITOR = "Monitor";
    public static final String SAMPLEDISPOSAL_STATUS = "Disposed";
    public static final String SAMPLETYPE_UNKNOWN = "Unknown";
    public static final String POLICY_SAMPLEBATCH = "BatchSamplePolicy";
    public static final String POLICY_HONORSAMPLERECEIVEFLAG = "honorsamplereceiveflags";
    public static final String LOCATION_PATH_DELIMITER = " / ";
    public static List<String> validStudySwitchStatusList = new ArrayList<String>();
    private static WhoDoneIt whoDoneIt;
    private static WhoDoneIt whoDoneItSdiWorkItem;
    private static WhoDoneIt whoDoneItSdiData;
    private HashMap<String, HashMap> productInfoMap = new HashMap();
    Map<String, DataSet> studyTestCache;
    private Map<String, Map<String, String>> sampleTypeFTCache = new HashMap<String, Map<String, String>>();
    private final Map<String, String> localCache = new HashMap<String, String>();
    private Map<String, Map<String, Boolean>> wicache = new HashMap<String, Map<String, Boolean>>();

    @Override
    public void postEditSDIData(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
        this.checkSampleTrackItemRule(primary, actionProps);
        boolean isBioBankSample = this.isBioBankSample(primary, true);
        if (isBioBankSample) {
            this.checkStudy(primary);
            this.setGLP(primary);
            this.checkSampleFamilyCreation(primary, actionProps, forceUpdate);
            String templateId = actionProps.getProperty("templateid").length() > 0 ? actionProps.getProperty("templateid") : actionProps.getProperty("templatekeyid1");
            boolean blnHasTemplate = true;
            if (templateId == null || templateId.equals("")) {
                blnHasTemplate = false;
            }
            if (!blnHasTemplate) {
                primary.addColumn(COLUMN_SAMPLESTATUS, 0);
            }
            primary.addColumn(COLUMN_STORAGESTATUS, 0);
            for (int i = 0; i < primary.size(); ++i) {
                String sampleStatus;
                String storageStatus = primary.getValue(i, COLUMN_STORAGESTATUS, "");
                if (storageStatus == null || storageStatus.equals("")) {
                    primary.setValue(i, COLUMN_STORAGESTATUS, STORAGESTATUS_ALLOCATED);
                }
                if (blnHasTemplate || (sampleStatus = primary.getValue(i, COLUMN_SAMPLESTATUS, "")) != null && !sampleStatus.equals("")) continue;
                primary.setValue(i, COLUMN_SAMPLESTATUS, SAMPLESTATUS_INITIAL);
            }
            this.validateProcessedDt(primary);
        }
        this.checkForReviewedTimePoint(primary, "Add");
        PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        boolean honorSampleReceiveFlag = "Y".equals(policy.getProperty(POLICY_HONORSAMPLERECEIVEFLAG, "N"));
        primary.addColumn(COLUMN_SAMPLESTATUS, 0);
        String[] statusActionProps = StringUtil.split(actionProps.getProperty(COLUMN_SAMPLESTATUS, ""), ";");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            boolean autoReceiveFlag;
            String passedInStatus;
            String templateid = actionProps.getProperty("templateid", "");
            if ("".equals(templateid)) {
                templateid = actionProps.getProperty("templatekeyid1", "");
            }
            String string = passedInStatus = statusActionProps.length > 1 ? statusActionProps[i] : statusActionProps[0];
            if (primary.getString(i, COLUMN_SAMPLESTATUS, "").length() != 0 && templateid.length() <= 0) continue;
            if (templateid.length() > 0 && passedInStatus.length() == 0) {
                primary.setString(i, COLUMN_SAMPLESTATUS, SAMPLESTATUS_INITIAL);
            }
            if ((autoReceiveFlag = "Y".equals(StringUtil.getYN(primary.getString(i, COLUMN_AUTORECEIVEFLAG), "N"))) && honorSampleReceiveFlag && (passedInStatus.length() == 0 || SAMPLESTATUS_INITIAL.equalsIgnoreCase(passedInStatus))) {
                primary.setString(i, COLUMN_SAMPLESTATUS, "Received");
                primary.setDate(i, COLUMN_RECEIVEDDT, DateTimeUtil.getNowTimestamp());
                primary.setString(i, COLUMN_RECEIVEDBY, this.getSysuserId());
            }
            if ("Y".equalsIgnoreCase(primary.getString(i, COLUMN_TEMPLATEFLAG, "")) && passedInStatus.length() == 0) {
                primary.setString(i, COLUMN_SAMPLESTATUS, SAMPLESTATUS_INITIAL);
            }
            if (StringUtil.getLen(primary.getValue(i, COLUMN_SAMPLESTATUS)) != 0L) continue;
            primary.setString(i, COLUMN_SAMPLESTATUS, SAMPLESTATUS_INITIAL);
        }
        this.setDefaultQCSampleType(primary);
        this.setTargetDisposalDT(primary, null);
        this.setSampleStatus(primary, null);
        this.setSampleClassification(primary, null);
        this.setLocationPath(primary);
        this.setSampleFamilyIdForRequestSample(primary);
        this.setSubmitterAndTargetDepartmentsForRequestSamples(primary);
    }

    private void setSampleFamilyIdForRequestSample(DataSet primary) throws SapphireException {
        PropertyList batchSamplePolicy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        boolean createSampleFamily = batchSamplePolicy.getProperty("createrequestsamplefamily", "Y").equalsIgnoreCase("y");
        if (createSampleFamily) {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String requestId = primary.getString(i, COLUMN_REQUESTID);
                String sstudyid = primary.getString(i, "sstudyid");
                if (StringUtil.getLen(requestId) <= 0L || StringUtil.getLen(sstudyid) != 0L) continue;
                String requestItemId = primary.getString(i, COLUMN_REQUESTITEMID, "");
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_SampleFamily");
                props.setProperty(COLUMN_REQUESTID, requestId);
                if (requestItemId.length() > 0) {
                    props.setProperty(COLUMN_REQUESTITEMID, requestItemId);
                }
                this.getActionProcessor().processAction("AddSDI", "1", props);
                String sampleFamilyId = (String)props.get("newkeyid1");
                primary.setString(i, "samplefamilyid", sampleFamilyId);
            }
        }
    }

    private void setSubmitterAndTargetDepartmentsForRequestSamples(DataSet primary) throws SapphireException {
        String requestIds;
        if (OpalUtil.isDeptSecurityEnabled(this.getSDCProcessor(), SDC_SAMPLE) && StringUtil.getLen(requestIds = primary.getColumnValues(COLUMN_REQUESTID, 0, primary.getRowCount(), "','", true)) > 0L) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder("select requesterid, submitbydepartmentid, sitedepartmentid, s_requestid from s_request where s_requestid in (");
            sql.append(safeSQL.addIn(requestIds)).append(")");
            DataSet requestDs = this.getQueryProcessor().getPreparedSqlDataSet("", sql.toString(), safeSQL.getValues(), false, 1000);
            if (requestDs.getRowCount() > 0) {
                String primarySampleDepartmentSecurityPolicy = RequestManagementUtil.getDepartmentSecuritySwitch(this.getConfigurationProcessor(), "primarysecuritydepartment", "requestsampledepartmentsecurity");
                HashMap<String, String> filter = new HashMap<String, String>();
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    String requestId = primary.getString(i, COLUMN_REQUESTID, "");
                    if (requestId.length() <= 0) continue;
                    filter.clear();
                    filter.put("s_requestid", requestId);
                    int row = requestDs.findRow(filter);
                    if (row == -1) continue;
                    String requesterid = requestDs.getString(row, "requesterid");
                    String submitbydepartmentid = requestDs.getString(row, "submitbydepartmentid");
                    String sitedepartmentid = requestDs.getString(row, "sitedepartmentid");
                    if (primarySampleDepartmentSecurityPolicy.equalsIgnoreCase("SD")) {
                        primary.setString(i, "securityuser", requesterid);
                        primary.setString(i, "securitydepartment", submitbydepartmentid);
                        continue;
                    }
                    if (!primarySampleDepartmentSecurityPolicy.equalsIgnoreCase("TD")) continue;
                    primary.setString(i, "securityuser", requesterid);
                    primary.setString(i, "securitydepartment", sitedepartmentid);
                }
            }
        }
    }

    private void setAuxillarySecurityDepartmentsForRequestSamples(DataSet primary) throws SapphireException {
        if (OpalUtil.isDeptSecurityEnabled(this.getSDCProcessor(), SDC_SAMPLE)) {
            StringBuffer toAddSdiSecurityDeparmentids = new StringBuffer("");
            StringBuffer keyid1s = new StringBuffer("");
            String auxillarySecurityDepartmentPolicySwitch = RequestManagementUtil.getDepartmentSecuritySwitch(this.getConfigurationProcessor(), "auxillarysecuritydepartment", "requestsampledepartmentsecurity");
            String requestIds = primary.getColumnValues(COLUMN_REQUESTID, 0, primary.getRowCount(), "','", true);
            if (StringUtil.getLen(requestIds) > 0L) {
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder("select requesterid, submitbydepartmentid, sitedepartmentid, s_requestid from s_request where s_requestid in (");
                sql.append(safeSQL.addIn(requestIds)).append(")");
                DataSet requestDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (requestDs.getRowCount() > 0) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    for (int i = 0; i < primary.getRowCount(); ++i) {
                        String sitedepartmentid;
                        String requestId = primary.getString(i, COLUMN_REQUESTID, "");
                        if (requestId.length() <= 0) continue;
                        filter.clear();
                        filter.put("s_requestid", requestId);
                        int row = requestDs.findRow(filter);
                        if (row == -1) continue;
                        if (auxillarySecurityDepartmentPolicySwitch.equalsIgnoreCase("SD")) {
                            String submitbydepartmentid = requestDs.getString(row, "submitbydepartmentid", "");
                            if (submitbydepartmentid.length() <= 0) continue;
                            keyid1s.append(";").append(primary.getString(i, COLUMN_KEYID1));
                            toAddSdiSecurityDeparmentids.append(";" + submitbydepartmentid);
                            continue;
                        }
                        if (!auxillarySecurityDepartmentPolicySwitch.equalsIgnoreCase("TD") || (sitedepartmentid = requestDs.getString(row, "sitedepartmentid", "")).length() <= 0) continue;
                        keyid1s.append(";").append(primary.getString(i, COLUMN_KEYID1));
                        toAddSdiSecurityDeparmentids.append(";" + sitedepartmentid);
                    }
                }
            }
            if (toAddSdiSecurityDeparmentids.toString().length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDC_SAMPLE);
                props.setProperty("keyid1", keyid1s.substring(1));
                props.setProperty("propsmatch", "Y");
                props.setProperty("departmentid", toAddSdiSecurityDeparmentids.substring(1));
                this.getActionProcessor().processAction("AddSDISecurityDept", "1", props);
            }
        }
    }

    private void setWapData(DataSet primary, boolean addMode, PropertyList actionProps) throws SapphireException {
        SafeSQL safeSql = new SafeSQL();
        HashMap<String, DataSet> dsMap = new HashMap<String, DataSet>();
        DataSet ds = new DataSet();
        DataSet beforeEditImage = new DataSet();
        if (!addMode) {
            beforeEditImage = this.getBeforeEditImage().getDataset("primary");
        }
        DataSet linkData = new DataSet();
        QueryProcessor qp = this.getQueryProcessor();
        SDCProcessor sdp = this.getSDCProcessor();
        boolean testingDepartmentFromProduct = false;
        boolean testingDepartmentFromSamplePoint = false;
        boolean deptSecurityEnabled = "D".equalsIgnoreCase(this.getSDCProcessor().getProperty(SDC_SAMPLE, "accesscontrolledflag"));
        List linkSDCColumnsList = this.getCopySiteFromRefSDC_Columnids();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String productId = primary.getString(i, "productid", "");
            String prodVersion = primary.getValue(i, "productversionid", "");
            String samplePointId = primary.getString(i, "samplepointid", "");
            String parent_locationId = primary.getString(i, "locationid", "");
            boolean siteDeptToSet = primary.getString(i, "sitedepartmentid", "").length() == 0;
            boolean wapStatusToSet = primary.getString(i, "wapstatus", "").length() == 0;
            safeSql.reset();
            if (samplePointId.length() > 0) {
                if (dsMap.containsKey("SamplePoint:" + samplePointId)) {
                    ds = (DataSet)dsMap.get("SamplePoint:" + samplePointId);
                } else {
                    ds = qp.getPreparedSqlDataSet("select * from s_samplepoint where s_samplepointid = " + safeSql.addVar(samplePointId), safeSql.getValues());
                    dsMap.put("SamplePoint:" + samplePointId, ds);
                    safeSql.reset();
                }
            } else if (parent_locationId.length() > 0) {
                if (dsMap.containsKey("Location:" + parent_locationId)) {
                    ds = (DataSet)dsMap.get("Location:" + parent_locationId);
                } else {
                    ds = qp.getPreparedSqlDataSet("select * from s_location where s_locationid = " + safeSql.addVar(parent_locationId), safeSql.getValues());
                    dsMap.put("Location:" + parent_locationId, ds);
                    safeSql.reset();
                }
            } else if (productId.length() > 0) {
                if (dsMap.containsKey("Product:" + productId + ";" + prodVersion)) {
                    ds = (DataSet)dsMap.get("Product:" + productId + ";" + prodVersion);
                } else {
                    ds = qp.getPreparedSqlDataSet("select * from s_product where s_productid = " + safeSql.addVar(productId) + " and s_productversionid=" + safeSql.addVar(prodVersion), safeSql.getValues());
                    dsMap.put("Product:" + productId + ";" + prodVersion, ds);
                    safeSql.reset();
                }
            }
            if (ds != null && ds.getRowCount() > 0) {
                String testingDepartmentId = ds.getValue(0, "testingdepartmentid");
                String workareaDepartmentId = ds.getValue(0, "workareadepartmentid");
                String createActivityRule = ds.getValue(0, "createactivityrule");
                String autoAssignRule = ds.getValue(0, "autoassignrule");
                String autoAssignAnalystId = ds.getValue(0, "autoassignanalystid");
                if (testingDepartmentId.length() > 0) {
                    primary.setString(i, "testingdepartmentid", testingDepartmentId);
                }
                if (workareaDepartmentId.length() > 0) {
                    primary.setString(i, "workareadepartmentid", workareaDepartmentId);
                }
                if (testingDepartmentId.length() > 0 || workareaDepartmentId.length() > 0) {
                    testingDepartmentFromSamplePoint = ds.getValue(0, "s_samplepointid").length() > 0;
                    boolean bl = testingDepartmentFromProduct = ds.getValue(0, "s_productid").length() > 0;
                }
                if ("On Demand".equalsIgnoreCase(createActivityRule) && wapStatusToSet) {
                    primary.setString(i, "wapstatus", "Pending");
                }
                if ("Analyst".equalsIgnoreCase(autoAssignRule) && autoAssignAnalystId.length() > 0) {
                    primary.setString(i, "assignedanalystid", autoAssignAnalystId);
                } else if ("Workarea".equalsIgnoreCase(autoAssignRule) && workareaDepartmentId.length() > 0) {
                    primary.setString(i, "assigneddepartmentid", workareaDepartmentId);
                }
            }
            String siteId = "";
            if (!siteDeptToSet) {
                siteId = primary.getString(i, "sitedepartmentid", "");
            }
            if (siteId.length() == 0 && beforeEditImage.getRowCount() > 0) {
                siteId = beforeEditImage.getString(i, "sitedepartmentid", "");
            }
            if (!siteDeptToSet || siteId.length() != 0) continue;
            String securityDeptId = primary.getValue(i, "securitydepartment");
            if (securityDeptId.length() == 0) {
                securityDeptId = beforeEditImage.getString(i, "securitydepartment", "");
            }
            if (securityDeptId.length() > 0 && deptSecurityEnabled) {
                siteId = this.resolveSampleSiteBySecurityDepartment(securityDeptId);
            }
            if (siteId.length() == 0) {
                if (linkData.getRowCount() == 0 && linkSDCColumnsList.size() > 0) {
                    safeSql.reset();
                    StringBuffer sql = new StringBuffer("SELECT sdclink.sdccolumnid, sdclink.sdccolumnid2, sdclink.sdccolumnid3, sdc.sdcid, sdc.tableid, sdc.accesscontrolledflag FROM sdclink, sdc WHERE sdc.sdcid = sdclink.linksdcid AND sdclink.sdcid = ");
                    sql.append(safeSql.addVar(SDC_SAMPLE)).append(" AND sdclink.sdccolumnid in (").append(safeSql.addIn(linkSDCColumnsList)).append(")");
                    linkData = qp.getPreparedSqlDataSet(sql.toString(), safeSql.getValues());
                }
                for (String linkedSDCColumn : linkSDCColumnsList) {
                    String locColumnId;
                    String locationId;
                    DataSet dsParent;
                    int findLinkRow = linkData.findRow("sdccolumnid", linkedSDCColumn);
                    if (findLinkRow <= -1) continue;
                    String linkedSDC = linkData.getValue(findLinkRow, "sdcid");
                    if (testingDepartmentFromSamplePoint && !"SamplePoint".equals(linkedSDC) || testingDepartmentFromProduct && !"Product".equals(linkedSDC)) continue;
                    String sdcColumnId = linkData.getValue(findLinkRow, "sdccolumnid");
                    String sdcColumnId2 = linkData.getValue(findLinkRow, "sdccolumnid2");
                    String sdcColumnId3 = linkData.getValue(findLinkRow, "sdccolumnid3");
                    String tableId = linkData.getValue(findLinkRow, "tableid");
                    boolean deptSecurityEnabledRefSDI = "D".equalsIgnoreCase(linkData.getValue(findLinkRow, "accesscontrolledflag"));
                    String fkLinkValue1 = "";
                    String fkLinkValue2 = "";
                    String fkLinkValue3 = "";
                    if (primary.isValidColumn(sdcColumnId)) {
                        fkLinkValue1 = primary.getValue(i, sdcColumnId);
                        fkLinkValue2 = primary.getValue(i, sdcColumnId2);
                        fkLinkValue3 = primary.getValue(i, sdcColumnId3);
                    } else if (beforeEditImage.getRowCount() > 0) {
                        fkLinkValue1 = beforeEditImage.getString(i, sdcColumnId, "");
                        fkLinkValue2 = beforeEditImage.getString(i, sdcColumnId2, "");
                        fkLinkValue3 = beforeEditImage.getString(i, sdcColumnId3, "");
                    }
                    if (fkLinkValue1.length() <= 0) continue;
                    String keycolid1 = sdp.getProperty(linkedSDC, "keycolid1");
                    String keycolid2 = sdp.getProperty(linkedSDC, "keycolid2");
                    String keycolid3 = sdp.getProperty(linkedSDC, "keycolid3");
                    safeSql.reset();
                    StringBuffer sql = new StringBuffer("SELECT * FROM " + tableId + " WHERE " + keycolid1 + " = " + safeSql.addVar(fkLinkValue1));
                    if (keycolid2.length() > 0) {
                        sql.append(" AND " + keycolid2 + " = " + safeSql.addVar(fkLinkValue2));
                    }
                    if (keycolid3.length() > 0) {
                        sql.append(" AND " + keycolid3 + " = " + safeSql.addVar(fkLinkValue3));
                    }
                    if ((dsParent = qp.getPreparedSqlDataSet(sql.toString(), safeSql.getValues())).getRowCount() <= 0) continue;
                    String parentObjectSiteId = dsParent.getValue(0, "sitedepartmentid");
                    String parentSecurityDept = dsParent.getValue(0, "securitydepartment");
                    if (parentObjectSiteId.length() > 0) {
                        siteId = parentObjectSiteId;
                        break;
                    }
                    if (linkedSDC.equals("Instrument")) {
                        String testingDepartmentId = dsParent.getValue(0, "testingdepartmentid");
                        DataSet departmentdef = WorkItemUtil.getDepartmentDefFromCache(this.database, this.connectionInfo, testingDepartmentId);
                        if (departmentdef.getRowCount() > 0) {
                            if ("Y".equalsIgnoreCase(departmentdef.getValue(0, "sitedepartmentflag"))) {
                                siteId = testingDepartmentId;
                            } else if (departmentdef.getValue(0, "parentdepartmentid").length() > 0) {
                                siteId = departmentdef.getValue(0, "parentdepartmentid");
                            }
                        }
                    } else if ((linkedSDC.equals("SamplePoint") || linkedSDC.equals("Location")) && (locationId = dsParent.getValue(0, locColumnId = linkedSDC.equals("SamplePoint") ? "locationid" : "s_locationid")).length() > 0) {
                        siteId = this.getParentLocationSiteId(locationId);
                    }
                    if (siteId.length() == 0 && deptSecurityEnabledRefSDI) {
                        if ((linkedSDC.equals("Location") || linkedSDC.equals("SamplePoint")) && parentSecurityDept.length() == 0) {
                            locColumnId = linkedSDC.equals("SamplePoint") ? "locationid" : "s_locationid";
                            parentSecurityDept = this.getNotNullParentSecurityDepartment(dsParent.getValue(0, locColumnId));
                        }
                        if (parentSecurityDept.length() > 0) {
                            siteId = this.resolveSampleSiteBySecurityDepartment(parentSecurityDept);
                        }
                    }
                    if (siteId.length() <= 0) continue;
                    break;
                }
            }
            if (siteId.length() == 0) {
                siteId = OpalUtil.getSiteIdFromUserDefaultTestingLab(this.connectionInfo, this.database);
            }
            if (siteId.length() == 0) {
                siteId = OpalUtil.getSiteIdFromUserDefaultDepartment(this.connectionInfo, this.database);
            }
            if (siteId.length() <= 0) continue;
            primary.setString(i, "sitedepartmentid", siteId);
        }
    }

    private List getCopySiteFromRefSDC_Columnids() throws SapphireException {
        ArrayList<String> linkSDCColumns = new ArrayList<String>();
        PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        PropertyListCollection refSDIs = policy.getCollectionNotNull("copysitefromreferencedsdi");
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSql = new SafeSQL();
        sql.append(" SELECT linksdcid, sdccolumnid FROM sdclink where sdcid = ").append(safeSql.addVar(SDC_SAMPLE));
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSql.getValues());
        for (int r = 0; r < refSDIs.size(); ++r) {
            int findRow;
            PropertyList pl = refSDIs.getPropertyList(r);
            String columnid = pl.getProperty("columnid");
            boolean turnOff = "Y".equalsIgnoreCase(pl.getProperty("turnoff", "N"));
            if (turnOff || columnid.length() <= 0 || columnid.indexOf("(") <= -1 || (findRow = ds.findRow("sdccolumnid", columnid = columnid.substring(columnid.indexOf("(") + 1, columnid.indexOf(")")).trim())) <= -1) continue;
            String linkSDCColumnId = ds.getValue(findRow, "sdccolumnid");
            linkSDCColumns.add(linkSDCColumnId);
        }
        return linkSDCColumns;
    }

    private String getParentLocationSiteId(String locationId) {
        String siteId = "";
        SafeSQL safesql = new SafeSQL();
        String sql = "";
        if (this.database.isOracle()) {
            sql = "SELECT loc.s_locationid, loc.sitedepartmentid FROM s_location loc WHERE loc.sitedepartmentid is not null  CONNECT BY PRIOR loc.PARENTLOCATIONID = loc.s_locationid START WITH loc.s_locationid = " + safesql.addVar(locationId);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safesql.getValues());
            if (ds.getRowCount() > 0) {
                siteId = ds.getValue(0, "sitedepartmentid");
            }
        } else {
            sql = "WITH n(s_locationid, parentlocationid, sitedepartmentid) AS (SELECT s_locationid, parentlocationid, sitedepartmentid  FROM s_location  WHERE  s_locationid = ? UNION ALL SELECT np.s_locationid, np.parentlocationid, np.sitedepartmentid FROM s_location as np, n  WHERE n.parentlocationid = np.s_locationid ) SELECT top 1 s_locationid, parentlocationid, sitedepartmentid FROM n where sitedepartmentid is not null ";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{locationId});
            if (ds.getRowCount() > 0) {
                siteId = ds.getValue(0, "sitedepartmentid");
            }
        }
        return siteId;
    }

    private String getNotNullParentSecurityDepartment(String locationId) {
        String siteId = "";
        SafeSQL safesql = new SafeSQL();
        String sql = "";
        if (this.database.isOracle()) {
            sql = "SELECT loc.s_locationid, loc.securitydepartment FROM s_location loc WHERE loc.securitydepartment is not null  CONNECT BY PRIOR loc.PARENTLOCATIONID = loc.s_locationid START WITH loc.s_locationid = " + safesql.addVar(locationId);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safesql.getValues());
            if (ds.getRowCount() > 0) {
                siteId = ds.getValue(0, "securitydepartment");
            }
        } else {
            sql = "WITH n(s_locationid, parentlocationid, securitydepartment) AS (SELECT s_locationid, parentlocationid, securitydepartment  FROM s_location  WHERE  s_locationid = ? UNION ALL SELECT np.s_locationid, np.parentlocationid, np.securitydepartment FROM s_location as np, n  WHERE n.parentlocationid = np.s_locationid ) SELECT top 1 s_locationid, parentlocationid, securitydepartment FROM n where securitydepartment is not null ";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{locationId});
            if (ds.getRowCount() > 0) {
                siteId = ds.getValue(0, "sitedepartmentid");
            }
        }
        return siteId;
    }

    private String resolveSampleSiteBySecurityDepartment(String securityDeptId) throws SapphireException {
        DataSet departmentdef;
        String siteId = "";
        if (securityDeptId.length() > 0 && (departmentdef = WorkItemUtil.getDepartmentDefFromCache(this.database, this.connectionInfo, securityDeptId)).getRowCount() > 0) {
            if ("Y".equalsIgnoreCase(departmentdef.getValue(0, "sitedepartmentflag"))) {
                siteId = securityDeptId;
            } else if ("Y".equalsIgnoreCase(departmentdef.getValue(0, "testingflag")) && departmentdef.getValue(0, "parentdepartmentid").length() > 0) {
                siteId = departmentdef.getValue(0, "parentdepartmentid");
            }
        }
        return siteId;
    }

    private void setLocationPath(DataSet primary) {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String locationId = primary.getString(i, "locationid", "");
            String samplePointId = primary.getString(i, "samplepointid", "");
            BigDecimal samplePointInstance = primary.getBigDecimal(i, "samplepointinstance");
            if (!locationId.isEmpty() || !samplePointId.isEmpty()) {
                String table;
                String columnId;
                ArrayList<String> sqlParams = new ArrayList<String>();
                if (!samplePointId.isEmpty()) {
                    columnId = "s_samplepointid";
                    table = "s_samplepoint";
                    sqlParams.add(samplePointId);
                } else {
                    columnId = "s_locationid";
                    table = "s_location";
                    sqlParams.add(locationId);
                }
                String selectFragment = this.database.isOracle() ? "LV_Query." : "dbo.LV_Query_";
                selectFragment = selectFragment + "GetLocPathLabel( " + columnId + ", '" + LOCATION_PATH_DELIMITER + "', null )";
                String sql = "SELECT " + selectFragment + " locationpath FROM " + table + " WHERE " + columnId + " = ?";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, sqlParams.toArray());
                if (ds.getRowCount() <= 0) continue;
                String locationPath = ds.getString(0, "locationpath", "");
                if (samplePointInstance != null && !locationPath.isEmpty()) {
                    locationPath = locationPath + LOCATION_PATH_DELIMITER + samplePointInstance.toPlainString();
                }
                primary.setString(i, "locationpath", locationPath);
                continue;
            }
            primary.setString(i, "locationpath", "");
        }
    }

    private void setSampleClassification(DataSet primary, DataSet beforeEditImage) {
        boolean classificationNotInPrimary = false;
        if (!primary.isValidColumn(COLUMN_CLASSIFICATION)) {
            primary.addColumn(COLUMN_CLASSIFICATION, 0);
            classificationNotInPrimary = true;
        }
        String qcSampleType = "";
        for (int i = 0; i < primary.size(); ++i) {
            String sampleid;
            int r;
            String oldClassification;
            if (beforeEditImage != null && classificationNotInPrimary && (oldClassification = beforeEditImage.getString(r = beforeEditImage.findRow(COLUMN_KEYID1, sampleid = primary.getString(i, COLUMN_KEYID1, "")), COLUMN_CLASSIFICATION, "")).length() > 0) {
                primary.setString(i, COLUMN_CLASSIFICATION, oldClassification);
                continue;
            }
            if (primary.getValue(i, COLUMN_CLASSIFICATION, "").length() != 0) continue;
            if (StringUtil.getLen(primary.getString(i, "sstudyid")) > 0L) {
                primary.setString(i, COLUMN_CLASSIFICATION, CLASSIFICATION_BIOBANKING);
                continue;
            }
            if (StringUtil.getLen(primary.getString(i, "studyid")) > 0L) {
                primary.setString(i, COLUMN_CLASSIFICATION, CLASSIFICATION_STABILITY);
                continue;
            }
            if (StringUtil.getLen(primary.getString(i, "batchid")) > 0L) {
                primary.setString(i, COLUMN_CLASSIFICATION, CLASSIFICATION_QM);
                continue;
            }
            if (StringUtil.getLen(primary.getString(i, COLUMN_REQUESTID)) > 0L) {
                primary.setString(i, COLUMN_CLASSIFICATION, CLASSIFICATION_REQUEST);
                continue;
            }
            qcSampleType = primary.getString(i, "QCSAMPLETYPE", "");
            if (qcSampleType.length() > 0 && !SAMPLETYPE_UNKNOWN.equals(qcSampleType)) {
                if (StringUtil.getLen(primary.getString(i, "reagentlotid")) > 0L && "Reagent Quality".equals(primary.getString(i, "QCSAMPLETYPE"))) {
                    primary.setString(i, COLUMN_CLASSIFICATION, CLASSIFICATION_REAGENTQUALITY);
                    primary.setString(i, "qcsampletype", SAMPLETYPE_UNKNOWN);
                    continue;
                }
                primary.setString(i, COLUMN_CLASSIFICATION, CLASSIFICATION_QCSAMPLE);
                continue;
            }
            if (primary.getString(i, "samplepointid", "").isEmpty() && primary.getString(i, "locationid", "").isEmpty() || !this.connectionInfo.hasModule("SampleMonitoring")) continue;
            primary.setString(i, COLUMN_CLASSIFICATION, CLASSIFICATION_MONITOR);
        }
    }

    @Override
    public void postAddKey(DataSet primary, PropertyList actionProps) {
        this.resolveSampleTypesAndSubTypes(primary, true);
    }

    private void resolveSampleTypesAndSubTypes(DataSet primary, boolean addOperation) {
        try {
            HashMap<String, String> parentSampleTypeCache = new HashMap<String, String>();
            String sql = new String("SELECT parentsampletypeid FROM s_sampletype WHERE s_sampletypeid = ?");
            PreparedStatement parentSampleTypeStatement = this.database.prepareStatement("parentsampletype", sql);
            for (int i = 0; i < primary.size(); ++i) {
                if (!addOperation && !this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESUBTYPEID)) continue;
                String sampleSubTypeId = primary.getValue(i, COLUMN_SAMPLESUBTYPEID, "");
                String sampleTypeId = primary.getValue(i, COLUMN_SAMPLETYPEID, (String)parentSampleTypeCache.get(sampleSubTypeId));
                if ((sampleTypeId == null || sampleTypeId.length() == 0) && sampleSubTypeId.length() > 0) {
                    parentSampleTypeStatement.setString(1, sampleSubTypeId);
                    DataSet parentSampleType = new DataSet(parentSampleTypeStatement.executeQuery());
                    if (parentSampleType.getRowCount() > 0) {
                        sampleTypeId = parentSampleType.getValue(0, "parentsampletypeid", "");
                        parentSampleTypeCache.put(sampleSubTypeId, sampleTypeId);
                    }
                }
                primary.setString(i, COLUMN_SAMPLETYPEID, sampleTypeId);
            }
        }
        catch (SapphireException e) {
            this.logger.error("Failed to create parentsampletype statement", e);
        }
        catch (SQLException e) {
            this.logger.error("Failed to retrieve parentsampletype data", e);
        }
    }

    private boolean isBioBankSample(DataSet primary, boolean add) {
        boolean isBioBankSample = false;
        if (primary.isValidColumn("sstudyid")) {
            isBioBankSample = true;
            for (int i = 0; i < primary.size(); ++i) {
                if (StringUtil.getLen(primary.getString(i, "sstudyid")) != 0L) continue;
                isBioBankSample = false;
                break;
            }
        } else if (!add && primary.size() > 0) {
            String s_sampleid = primary.getString(0, COLUMN_KEYID1);
            if (StringUtil.getLen(OpalUtil.getColumnValue(this.getQueryProcessor(), "s_sample", "sstudyid", "s_sampleid = ?", new String[]{s_sampleid})) > 0L) {
                isBioBankSample = true;
            }
        }
        return isBioBankSample;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String planLevels;
        DataSet primary = sdiData.getDataset("primary");
        this.createAlias(actionProps);
        if (this.isBioBankSample(primary, true)) {
            this.processStudyWorkitems(primary, actionProps);
            DataSet clinicalFlagds = BusinessRulesUtil.getClinicalFlagForStudy(this.getDAMProcessor(), this.getQueryProcessor(), OpalUtil.getUniqueValueList(primary.getColumnValues("sstudyid", ";"), ";"));
            if (!"Y".equalsIgnoreCase(clinicalFlagds.getValue(0, "clinicalflag"))) {
                this.addBlankDocumentsFromStudy(primary.getColumnValues(COLUMN_KEYID1, ";"), "add");
            }
        }
        this.calculateSampleDueDate(primary, actionProps, "Add");
        StringBuffer sampleIds = new StringBuffer();
        ArrayList<String> sampleFlags = new ArrayList<String>();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String sampleId = primary.getValue(i, COLUMN_KEYID1);
            String batchId = primary.getValue(i, "batchid");
            String classification = primary.getValue(i, COLUMN_CLASSIFICATION);
            String batchStageId = primary.getValue(i, "batchstageid");
            String requestId = primary.getValue(i, COLUMN_REQUESTID);
            String monitorGroupId = primary.getValue(i, COLUMN_MONITORGROUPID);
            if (CLASSIFICATION_QM.equals(classification) && (batchId.length() > 0 || batchStageId.length() > 0) || requestId.length() > 0 || monitorGroupId.length() > 0) {
                sampleIds.append(";").append(sampleId);
            }
            if (batchId.length() > 0) {
                sampleFlags.add("batch");
            }
            if (batchStageId.length() > 0) {
                sampleFlags.add("batchstage");
            }
            if (requestId.length() > 0) {
                sampleFlags.add("request");
            }
            if (monitorGroupId.length() <= 0) continue;
            sampleFlags.add("monitorgroup");
        }
        if (sampleIds.length() > 0) {
            PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
            String rsetid = null;
            try {
                rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, sampleIds.substring(1), null, null);
                String errorMessage = this.isSampleEditAllowed(policy, rsetid, primary, sampleFlags);
                if (errorMessage.length() > 0) {
                    throw new SapphireException(this.getTranslationProcessor().translate(errorMessage));
                }
                this.getDAMProcessor().clearRSet(rsetid);
            }
            catch (Throwable throwable) {
                this.getDAMProcessor().clearRSet(rsetid);
                throw throwable;
            }
        }
        this.addSamplesToBatch(sdiData);
        this.changeBatchStageStatusOnSampleAdd(primary);
        if (actionProps.containsKey("copyfromsamplingplanlevelid") && (planLevels = actionProps.getProperty("copyfromsamplingplanlevelid")).trim().length() > 0) {
            DataSet dsPrimaryCopy = new DataSet();
            dsPrimaryCopy.copyRow(primary, -1, 1);
            this.applySamplingPlanLevel(dsPrimaryCopy, planLevels, actionProps);
        }
        this.setAuxillarySecurityDepartmentsForRequestSamples(primary);
        DataSet dsAnalyst = new DataSet();
        DataSet dsWorkarea = new DataSet();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            int r;
            if (this.hasPrimaryValueChanged(primary, i, "assignedanalystid")) {
                r = dsAnalyst.addRow();
                dsAnalyst.setString(r, "sampleid", primary.getValue(i, COLUMN_KEYID1, ""));
                dsAnalyst.setString(r, "assignedanalystid", primary.getValue(i, "assignedanalystid", ""));
            }
            if (!this.hasPrimaryValueChanged(primary, i, "assigneddepartmentid")) continue;
            r = dsWorkarea.addRow();
            dsWorkarea.setString(r, "sampleid", primary.getValue(i, COLUMN_KEYID1, ""));
            dsWorkarea.setString(r, "assigneddepartmentid", primary.getValue(i, "assigneddepartmentid", ""));
        }
        if (dsAnalyst.getRowCount() > 0) {
            this.copyAnalystToDataSetsAndSDIWorkItems(dsAnalyst);
        }
        if (dsWorkarea.getRowCount() > 0) {
            this.copyWorkAreaToDataSetsAndSDIWorkItems(dsWorkarea);
        }
        if (RequestManagementUtil.isPolicyEnabled(this.getConfigurationProcessor()) && !actionProps.getProperty("statusrollup").equals("false")) {
            this.changeRequestStatus(sdiData);
        }
        if (this.isCMTImport()) {
            this.handleCMT(sdiData);
        }
    }

    /*
     * Unable to fully structure code
     */
    private void applySamplingPlanLevel(DataSet ds, String planLevels, PropertyList actionProps) throws SapphireException {
        tp = this.getTranslationProcessor();
        delimeter = actionProps.getProperty("separator", actionProps.getProperty("delimeter", ";"));
        ds.addColumnValues("copyfromsamplingplanlevel", 0, planLevels, delimeter);
        if (actionProps.containsKey("copyfromsamplingplanid")) {
            planIds = actionProps.getProperty("copyfromsamplingplanid");
            planVersionIds = actionProps.getProperty("copyfromsamplingplanversionid");
            ds.addColumnValues("copyfromsamplingplanid", 0, planIds, delimeter);
            ds.addColumnValues("copyfromsamplingplanversionid", 0, planVersionIds, delimeter);
            ds.padColumn("copyfromsamplingplanid");
            ds.padColumn("copyfromsamplingplanversionid");
        }
        ds.padColumn("copyfromsamplingplanlevel");
        getEmbeddedSamplingPlanFromProduct = this.database.prepareStatement("getproductsamplingplan", "SELECT embeddedsamplingplanid, embeddedsamplingplanversionid, embeddedspecid, embeddedspecversionid  FROM s_product  WHERE s_productid = ? AND s_productversionid = ?  AND productmodeflag = 'S' AND embeddedsamplingplanid IS NOT  NULL");
        getSamplingPlanFromBatch = this.database.prepareStatement("getbatchproductsamplingplan", "SELECT b.samplingplanid, b.samplingplanversionid, p.embeddedspecid, p.embeddedspecversionid  FROM s_product p, s_batch b  WHERE p.s_productid = b.productid AND p.s_productversionid = b.productversionid AND p.productmodeflag = 'S' AND b.s_batchid = ?");
        sql = new StringBuffer();
        sql.append("SELECT distinct spi.itemsdcid, spi.itemkeyid1, spi.itemkeyid2, spi.itemkeyid3, spi.usersequence spitemusersequence, spi.s_samplingplanitemno ").append(" FROM s_spitem spi, s_spdetail spd, s_spdetailitem spdi ").append(" WHERE spdi.s_samplingplanid = spd.s_samplingplanid AND spdi.s_samplingplanversionid = spd.s_samplingplanversionid ").append(" AND spdi.s_samplingplandetailno = spd.s_samplingplandetailno AND spdi.s_samplingplanitemno = spi.s_samplingplanitemno ").append(" AND spi.s_samplingplanid = spd.s_samplingplanid AND spi.s_samplingplanversionid = spd.s_samplingplanversionid ").append(" AND spd.s_samplingplanid = ? AND spd.s_samplingplanversionid = ?").append(" AND spd.levelid = ? order by spitemusersequence, spi.s_samplingplanitemno");
        getLevelData = this.database.prepareStatement("getleveldata", sql.toString());
        addSDIWorkItem = new DataSet();
        addSDISpec = new DataSet();
        cacheProductPlan = new HashMap<String, String>();
        cacheBatchPlan = new HashMap<String, String>();
        cachePlanLevelData = new HashMap<String, DataSet>();
        seq = 0;
        editBatch = new DataSet();
        for (i = 0; i < ds.getRowCount(); ++i) {
            level = ds.getString(i, "copyfromsamplingplanlevel", "");
            batchId = ds.getString(i, "batchid", "");
            productId = ds.getString(i, "productid", "");
            productVersionId = ds.getString(i, "productversionid", "");
            planId = ds.getString(i, "copyfromsamplingplanid", "");
            planVerId = ds.getString(i, "copyfromsamplingplanversionid", "");
            embeddedSpecId = "";
            embeddedSpecVerId = "";
            if (planId.length() > 0 && planVerId.length() == 0) {
                throw new SapphireException(tp.translate("In action property list, SamplingPlan Version is not provided for SamplingPlan") + " " + planId);
            }
            if (level.length() > 0) {
                sampleId = ds.getString(i, "s_sampleid", "");
                if (planId.length() == 0) {
                    if (productId.length() > 0) {
                        if (cacheProductPlan.containsKey(productId + ";" + productVersionId)) {
                            prodPlan = StringUtil.split((String)cacheProductPlan.get(productId + ";" + productVersionId), ";");
                            planId = prodPlan[0];
                            planVerId = prodPlan[1];
                            embeddedSpecId = prodPlan[2];
                            embeddedSpecVerId = prodPlan[3];
                        } else {
                            try {
                                getEmbeddedSamplingPlanFromProduct.setString(1, productId);
                                getEmbeddedSamplingPlanFromProduct.setString(2, productVersionId);
                                dsProduct = new DataSet(getEmbeddedSamplingPlanFromProduct.executeQuery());
                                if (dsProduct.getRowCount() <= 0) ** GOTO lbl85
                                planId = dsProduct.getString(0, "embeddedsamplingplanid", "");
                                planVerId = dsProduct.getString(0, "embeddedsamplingplanversionid", "");
                                embeddedSpecId = dsProduct.getString(0, "embeddedspecid", "");
                                embeddedSpecVerId = dsProduct.getString(0, "embeddedspecversionid", "");
                                cacheProductPlan.put(productId + ";" + productVersionId, planId + ";" + planVerId + ";" + embeddedSpecId + ";" + embeddedSpecVerId);
                            }
                            catch (Exception e) {
                                throw new SapphireException(e);
                            }
                        }
                    } else if (batchId.length() > 0) {
                        if (cacheBatchPlan.containsKey(batchId)) {
                            batchPlan = StringUtil.split((String)cacheBatchPlan.get(batchId), ";");
                            planId = batchPlan[0];
                            planVerId = batchPlan[1];
                            embeddedSpecId = batchPlan[2];
                            embeddedSpecVerId = batchPlan[3];
                        } else {
                            try {
                                getSamplingPlanFromBatch.setString(1, batchId);
                                dsBatchPlan = new DataSet(getSamplingPlanFromBatch.executeQuery());
                                if (dsBatchPlan.getRowCount() > 0) {
                                    planId = dsBatchPlan.getString(0, "samplingplanid", "");
                                    planVerId = dsBatchPlan.getString(0, "samplingplanversionid", "");
                                    embeddedSpecId = dsBatchPlan.getString(0, "embeddedspecid", "");
                                    embeddedSpecVerId = dsBatchPlan.getString(0, "embeddedspecversionid", "");
                                    cacheBatchPlan.put(batchId, planId + ";" + planVerId + ";" + embeddedSpecId + ";" + embeddedSpecVerId);
                                }
                            }
                            catch (Exception e) {
                                throw new SapphireException(e);
                            }
                        }
                    }
                }
lbl85:
                // 9 sources

                if (planId.length() > 0) {
                    levelDS = new DataSet();
                    if (cachePlanLevelData.containsKey(planId + ";" + planVerId + ";" + level)) {
                        levelDS = (DataSet)cachePlanLevelData.get(planId + ";" + planVerId + ";" + level);
                    } else {
                        try {
                            getLevelData.setString(1, planId);
                            getLevelData.setString(2, planVerId);
                            getLevelData.setString(3, level);
                            levelDS.setResultSet(getLevelData.executeQuery());
                            cachePlanLevelData.put(planId + ";" + planVerId + ";" + level, levelDS);
                        }
                        catch (Exception e) {
                            throw new SapphireException(e);
                        }
                    }
                    for (d = 0; d < levelDS.getRowCount(); ++d) {
                        itemSDCId = levelDS.getString(d, "itemsdcid", "");
                        itemKeyid1 = levelDS.getString(d, "itemkeyid1", "");
                        itemKeyid2 = levelDS.getString(d, "itemkeyid2", "");
                        spitemUseq = levelDS.getValue(d, "spitemusersequence", "0");
                        if (itemSDCId.equals("WorkItem")) {
                            r = addSDIWorkItem.addRow();
                            addSDIWorkItem.setString(r, "keyid1", sampleId);
                            addSDIWorkItem.setString(r, "workitemid", itemKeyid1);
                            addSDIWorkItem.setString(r, "workitemversionid", itemKeyid2);
                            addSDIWorkItem.setString(r, "spitemusersequence", Integer.toString(++seq));
                            addSDIWorkItem.setString(r, "planid", planId);
                            addSDIWorkItem.setString(r, "planversionid", planVerId);
                            addSDIWorkItem.setString(r, "batchid", batchId);
                            addSDIWorkItem.setString(r, "level", level);
                            continue;
                        }
                        if (!itemSDCId.equals("SpecSDC")) continue;
                        r = addSDISpec.addRow();
                        addSDISpec.setString(r, "keyid1", sampleId);
                        addSDISpec.setString(r, "specid", itemKeyid1);
                        addSDISpec.setString(r, "specversionid", itemKeyid2);
                        addSDISpec.setString(r, "spitemusersequence", spitemUseq);
                        addSDISpec.setString(r, "batchid", batchId);
                        addSDISpec.setString(r, "level", level);
                        addSDISpec.setString(r, "planid", planId);
                        addSDISpec.setString(r, "planversionid", planVerId);
                    }
                    if (embeddedSpecId.length() > 0) {
                        r = addSDISpec.addRow();
                        addSDISpec.setString(r, "keyid1", sampleId);
                        addSDISpec.setString(r, "specid", embeddedSpecId);
                        addSDISpec.setString(r, "specversionid", embeddedSpecVerId);
                        addSDISpec.setString(r, "batchid", batchId);
                        addSDISpec.setString(r, "level", level);
                        addSDISpec.setString(r, "planid", planId);
                        addSDISpec.setString(r, "planversionid", planVerId);
                    }
                }
            }
            if (batchId.length() <= 0 || editBatch.findRow("batchid", batchId) >= 0 || planId == null || planId.length() <= 0 || cacheBatchPlan.containsKey(batchId)) continue;
            this.database.createPreparedResultSet("checkbatchplan", "SELECT 1 FROM s_batch WHERE s_batchid = ? AND samplingplanid IS NULL", new String[]{batchId});
            if (this.database.getNext("checkbatchplan")) {
                row = editBatch.addRow();
                editBatch.setString(row, "keyid1", batchId);
                editBatch.setString(row, "samplingplanid", planId);
                editBatch.setString(row, "samplingplanversionid", planVerId);
            }
            this.database.closeResultSet("checkbatchplan");
        }
        ap = this.getActionProcessor();
        props = new PropertyList();
        if (addSDIWorkItem.getRowCount() > 0) {
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", addSDIWorkItem.getColumnValues("keyid1", ";"));
            props.setProperty("workitemid", addSDIWorkItem.getColumnValues("workitemid", ";"));
            props.setProperty("workitemversionid", addSDIWorkItem.getColumnValues("workitemversionid", ";"));
            props.setProperty("propsmatch", "Y");
            props.setProperty("propsmatchtestmethodorder", addSDIWorkItem.getColumnValues("spitemusersequence", ";"));
            ap.processAction("AddSDIWorkItem", "1", props);
        }
        if (addSDISpec.getRowCount() > 0) {
            addSDISpec.sort("spitemusersequence,specid,specversionid");
            groups = addSDISpec.getGroupedDataSets("spitemusersequence,specid,specversionid");
            for (grp = 0; grp < groups.size(); ++grp) {
                dsGrp = groups.get(grp);
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", dsGrp.getColumnValues("keyid1", ";"));
                props.setProperty("specid", dsGrp.getString(0, "specid", ""));
                props.setProperty("specversionid", dsGrp.getString(0, "specversionid", ""));
                ap.processAction("AddSDISpec", "1", props);
            }
        }
        if (editBatch.getRowCount() > 0) {
            props.clear();
            props.setProperty("sdcid", "Batch");
            props.setProperty("keyid1", editBatch.getColumnValues("keyid1", ";"));
            props.setProperty("samplingplanid", editBatch.getColumnValues("samplingplanid", ";"));
            props.setProperty("samplingplanversionid", editBatch.getColumnValues("samplingplanversionid", ";"));
            ap.processAction("EditSDI", "1", props);
        }
        this.database.closeStatement("getproductsamplingplan");
        this.database.closeStatement("getbatchproductsamplingplan");
        this.database.closeStatement("getleveldata");
    }

    @Override
    public void preEditWorkitem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet sdiwi = sdiData.getDataset("sdiworkitem");
        if (sdiwi.getRowCount() > 0) {
            whoDoneItSdiWorkItem.process(sdiwi, this);
        }
        if (sdiwi.isValidColumn("starteddt")) {
            for (int i = 0; i < sdiwi.size(); ++i) {
                if (sdiwi.getCalendar(i, "starteddt") != null) continue;
                String workitemid = sdiwi.getString(i, "workitemid");
                String workiteminstance = sdiwi.getValue(i, "workiteminstance");
                String sdcid = sdiwi.getString(i, "sdcid");
                String keyid1 = sdiwi.getString(i, "keyid1");
                String keyid2 = sdiwi.getString(i, "keyid2");
                String keyid3 = sdiwi.getString(i, "keyid3");
                int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                String sql = "select starteddt, startedby from sdiworkitem";
                sql = sql + " where workitemid=? and workiteminstance=?";
                sql = sql + " and sdcid=?";
                Object[] s = new String[]{};
                if (keycolumns == 1) {
                    sql = sql + " and keyid1=?";
                    s = new String[]{workitemid, workiteminstance, sdcid, keyid1};
                } else if (keycolumns == 2) {
                    sql = sql + " and keyid1=? and keyid2=?";
                    s = new String[]{workitemid, workiteminstance, sdcid, keyid1, keyid2};
                } else if (keycolumns == 3) {
                    sql = sql + " and keyid1=? and keyid2=? and keyid3=?";
                    s = new String[]{workitemid, workiteminstance, sdcid, keyid1, keyid2, keyid3};
                }
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, s);
                if (ds == null || ds.size() <= 0) continue;
                String startedby = ds.getString(0, "startedby", "");
                Calendar starteddt = ds.getCalendar(0, "starteddt");
                if (startedby.length() > 0 && sdiwi.isValidColumn("startedby")) {
                    sdiwi.setString(i, "startedby", startedby);
                }
                if (starteddt == null) continue;
                sdiwi.setDate(i, "starteddt", starteddt);
            }
        }
    }

    @Override
    public void preEditSDIData(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet sdiDataset = sdiData.getDataset("dataset");
        if (sdiDataset.getRowCount() > 0) {
            whoDoneItSdiData.process(sdiDataset, this);
        }
        if (sdiDataset.isValidColumn("starteddt")) {
            for (int i = 0; i < sdiDataset.size(); ++i) {
                if (sdiDataset.getCalendar(i, "starteddt") != null) continue;
                String paramlistid = sdiDataset.getString(i, "paramlistid");
                String paramlistversionid = sdiDataset.getString(i, "paramlistversionid");
                String variantid = sdiDataset.getValue(i, "variantid");
                String dataset = sdiDataset.getValue(i, "dataset");
                String sdcid = sdiDataset.getString(i, "sdcid");
                String keyid1 = sdiDataset.getString(i, "keyid1");
                String keyid2 = sdiDataset.getString(i, "keyid2");
                String keyid3 = sdiDataset.getString(i, "keyid3");
                int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
                String sql = "select starteddt, startedby from sdidata";
                sql = sql + " where paramlistid=? and paramlistversionid=? and variantid=? and dataset=?";
                sql = sql + " and sdcid=?";
                Object[] s = new String[]{};
                if (keycolumns == 1) {
                    sql = sql + " and keyid1=?";
                    s = new String[]{paramlistid, paramlistversionid, variantid, dataset, sdcid, keyid1};
                } else if (keycolumns == 2) {
                    sql = sql + " and keyid1=? and keyid2=?";
                    s = new String[]{paramlistid, paramlistversionid, variantid, dataset, sdcid, keyid1, keyid2};
                } else if (keycolumns == 3) {
                    sql = sql + " and keyid1=? and keyid2=? and keyid3=?";
                    s = new String[]{paramlistid, paramlistversionid, variantid, dataset, sdcid, keyid1, keyid2, keyid3};
                }
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, s);
                if (ds == null || ds.size() <= 0) continue;
                String startedby = ds.getString(0, "startedby", "");
                Calendar starteddt = ds.getCalendar(0, "starteddt");
                if (startedby.length() > 0 && sdiDataset.isValidColumn("startedby")) {
                    sdiDataset.setString(i, "startedby", startedby);
                }
                if (starteddt == null) continue;
                sdiDataset.setDate(i, "starteddt", starteddt);
            }
        }
    }

    @Override
    public void postEditWorkitem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet sdiwi = sdiData.getDataset("sdiworkitem");
        DataSet primary = sdiData.getDataset("primary");
        this.calculateSDIWorkItemDueDate(sdiwi, primary);
        HashSet<String> sampleSet = new HashSet<String>();
        ArrayList<String> cancelList = new ArrayList<String>();
        for (int i = 0; i < sdiwi.size(); ++i) {
            String policycancelactionbehavior;
            if (!this.hasSDIWorkItemValueChanged(sdiwi, i, "workitemstatus")) continue;
            String workitemstatus = sdiwi.getString(i, "workitemstatus", "");
            if (SAMPLESTATUS_CANCELLED.equals(workitemstatus) && ("Cancel all Children".equals(policycancelactionbehavior = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom").getProperty("cancelactionbehavior")) || "Cancel Incomplete Children".equals(policycancelactionbehavior))) {
                StringBuilder sql = new StringBuilder();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select s.s_sampleid, s.samplestatus, s.storagestatus");
                sql.append(" from s_sample s");
                sql.append(" where s.sourcesdiworkitemid in ( select w.sdiworkitemid");
                sql.append(" from sdiworkitem w");
                sql.append(" where w.sdcid = 'Sample'");
                sql.append(" and w.keyid1 = ").append(safeSQL.addVar(sdiwi.getString(i, "keyid1")));
                sql.append(" and w.workitemid = ").append(safeSQL.addVar(sdiwi.getString(i, "workitemid")));
                sql.append(" and w.workiteminstance = ").append(safeSQL.addVar(sdiwi.getInt(i, "workiteminstance"))).append(")");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    if ("Cancel all Children".equals(policycancelactionbehavior)) {
                        cancelList.addAll(OpalUtil.toList(ds.getColumnValues(COLUMN_KEYID1, ";"), ";"));
                    } else {
                        for (int j = 0; j < ds.size(); ++j) {
                            String samplestatus = ds.getString(j, COLUMN_SAMPLESTATUS, "");
                            String storagestatus = ds.getString(j, COLUMN_STORAGESTATUS, "");
                            if (!SAMPLESTATUS_INITIAL.equals(samplestatus) && !STORAGESTATUS_ALLOCATED.equals(storagestatus) && !STORAGESTATUS_INPREP.equals(storagestatus)) continue;
                            cancelList.add(ds.getString(j, COLUMN_KEYID1));
                        }
                    }
                }
            }
            if (!SDC_SAMPLE.equals(sdiwi.getString(i, "sdcid"))) continue;
            sampleSet.add(sdiwi.getString(i, "keyid1"));
        }
        if (cancelList.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_SAMPLE);
            props.setProperty("keyid1", OpalUtil.toDelimitedString(cancelList, ";"));
            props.setProperty(COLUMN_SAMPLESTATUS, SAMPLESTATUS_CANCELLED);
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        if (sampleSet.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_SAMPLE);
            props.setProperty("keyid1", OpalUtil.toDelimitedString(sampleSet, ";"));
            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
            this.getActionProcessor().processActionClass(SyncSDIDataSetStatus.class.getName(), props);
        }
    }

    private void calculateSDIWorkItemDueDate(DataSet sdiwi, DataSet primary) throws SapphireException {
        DataSet beforeEditImage = this.getBeforeEditImage().getDataset("sdiworkitem");
        PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        String workItemOffsetColumn = policy.getProperty("workitemduedateoffsetcolumn", "sdiworkitem.createdt");
        boolean matchSDIWorkItemDueDt = "Y".equalsIgnoreCase(policy.getProperty("matchworkitemduedatetosample", "N"));
        String offsetColumn = "";
        boolean offsetColFromSample = true;
        if (workItemOffsetColumn.startsWith("sdiworkitem.")) {
            offsetColFromSample = false;
            offsetColumn = workItemOffsetColumn.substring(workItemOffsetColumn.indexOf(".") + 1);
        }
        DataSet editDueDt = new DataSet();
        DataSet editDueDtOverrideFlag = new DataSet();
        for (int i = 0; i < sdiwi.getRowCount(); ++i) {
            String dueDTOverrideFlagPrev = beforeEditImage.getString(i, COLUMN_DUEDTOVERRIDEFLAG, "");
            Calendar currentDueDate = sdiwi.getCalendar(i, COLUMN_DUEDT);
            String dueDtOverrideFlagCurrent = sdiwi.getString(i, COLUMN_DUEDTOVERRIDEFLAG, "");
            int sampleRow = primary.findRow(COLUMN_KEYID1, sdiwi.getString(i, "keyid1"));
            Calendar sampleOffsetColDt = null;
            Calendar sampleDueDt = null;
            if (matchSDIWorkItemDueDt) {
                sampleDueDt = primary.getCalendar(sampleRow, COLUMN_DUEDT);
            }
            if (currentDueDate != null && !"N".equalsIgnoreCase(dueDtOverrideFlagCurrent)) {
                if ("Y".equalsIgnoreCase(dueDtOverrideFlagCurrent) || "Y".equalsIgnoreCase(dueDTOverrideFlagPrev)) continue;
                int r = editDueDtOverrideFlag.addRow();
                editDueDtOverrideFlag.setString(r, "keyid1", sdiwi.getString(i, "keyid1"));
                editDueDtOverrideFlag.setString(r, "workitemid", sdiwi.getString(i, "workitemid"));
                editDueDtOverrideFlag.setString(r, "workiteminstance", sdiwi.getValue(i, "workiteminstance"));
                editDueDtOverrideFlag.setString(r, COLUMN_DUEDTOVERRIDEFLAG, "Y");
                continue;
            }
            if ("Y".equalsIgnoreCase(dueDTOverrideFlagPrev)) continue;
            if (workItemOffsetColumn.startsWith("sample.")) {
                sampleOffsetColDt = primary.getCalendar(sampleRow, workItemOffsetColumn.substring(workItemOffsetColumn.indexOf(".") + 1));
            }
            if ((!offsetColFromSample || !this.hasSDIWorkItemValueChanged(sdiwi, i, "duedtoffset") && !this.hasSDIWorkItemValueChanged(sdiwi, i, "duedtoffsettimeunit")) && !this.hasSDIWorkItemValueChanged(sdiwi, i, offsetColumn) && !this.hasSDIWorkItemValueChanged(sdiwi, i, "duedtoffset") && !this.hasSDIWorkItemValueChanged(sdiwi, i, "duedtoffsettimeunit")) continue;
            String duedateOffset = sdiwi.getValue(i, "duedtoffset", "");
            String duedateOffsetUnit = sdiwi.getString(i, "duedtoffsettimeunit", "");
            Calendar offsetColumnValue = null;
            if (offsetColFromSample) {
                offsetColumnValue = sampleOffsetColDt;
            } else {
                offsetColumnValue = sdiwi.getCalendar(i, offsetColumn);
                if (offsetColumnValue == null) {
                    offsetColumnValue = beforeEditImage.getCalendar(i, offsetColumn);
                }
            }
            if (duedateOffset.length() == 0) {
                duedateOffset = beforeEditImage.getValue(i, "duedtoffset", "");
            }
            if (duedateOffsetUnit.length() == 0) {
                duedateOffsetUnit = beforeEditImage.getString(i, "duedtoffsettimeunit", "");
            }
            if (duedateOffset.length() == 0 || duedateOffsetUnit.length() == 0) {
                String wi = sdiwi.getString(i, "workitemid");
                String wiinstance = sdiwi.getValue(i, "workiteminstance");
                this.database.createPreparedResultSet("widuedtinfo", "select w.duedtoffset, w.duedtoffsettimeunit from workitem w, sdiworkitem s  where w.workitemid = s.workitemid and w.workitemversionid = s.workitemversionid and s.workitemid=? and s.workiteminstance=?", new String[]{wi, wiinstance});
                DataSet dsWI = new DataSet(this.database.getResultSet("widuedtinfo"));
                if (dsWI.getRowCount() > 0) {
                    if (duedateOffset.length() == 0) {
                        duedateOffset = dsWI.getValue(0, "duedtoffset", "");
                    }
                    if (duedateOffsetUnit.length() == 0) {
                        duedateOffsetUnit = dsWI.getString(0, "duedtoffsettimeunit", "");
                    }
                }
                this.database.closeResultSet("widuedtinfo");
            }
            Calendar previousDueDate = beforeEditImage.getCalendar(i, COLUMN_DUEDT);
            if (offsetColumnValue == null || duedateOffset.length() <= 0 || duedateOffsetUnit.length() <= 0) continue;
            Calendar calcDate = DateTimeUtil.getOffsetDate(offsetColumnValue, duedateOffsetUnit, new BigDecimal(duedateOffset));
            if (sampleDueDt != null && sampleDueDt.compareTo(calcDate) < 0) {
                calcDate = sampleDueDt;
            }
            if (previousDueDate != null && calcDate.compareTo(previousDueDate) == 0 && "N".equalsIgnoreCase(dueDTOverrideFlagPrev)) continue;
            int r = editDueDt.addRow();
            editDueDt.setString(r, "keyid1", sdiwi.getString(i, "keyid1"));
            editDueDt.setString(r, "workitemid", sdiwi.getString(i, "workitemid"));
            editDueDt.setString(r, "workiteminstance", sdiwi.getValue(i, "workiteminstance"));
            editDueDt.setString(r, COLUMN_DUEDT, new M18NUtil(this.connectionInfo).format(calcDate));
            editDueDt.setString(r, COLUMN_DUEDTOVERRIDEFLAG, "N");
        }
        if (editDueDtOverrideFlag.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_SAMPLE);
            props.setProperty("keyid1", editDueDtOverrideFlag.getColumnValues("keyid1", ";"));
            props.setProperty("workitemid", editDueDtOverrideFlag.getColumnValues("workitemid", ";"));
            props.setProperty("workiteminstance", editDueDtOverrideFlag.getColumnValues("workiteminstance", ";"));
            props.setProperty(COLUMN_DUEDTOVERRIDEFLAG, editDueDtOverrideFlag.getColumnValues(COLUMN_DUEDTOVERRIDEFLAG, ";"));
            this.getActionProcessor().processAction("EditSDIWorkItem", "1", props);
        }
        if (editDueDt.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_SAMPLE);
            props.setProperty("keyid1", editDueDt.getColumnValues("keyid1", ";"));
            props.setProperty("workitemid", editDueDt.getColumnValues("workitemid", ";"));
            props.setProperty("workiteminstance", editDueDt.getColumnValues("workiteminstance", ";"));
            props.setProperty(COLUMN_DUEDT, editDueDt.getColumnValues(COLUMN_DUEDT, ";"));
            props.setProperty(COLUMN_DUEDTOVERRIDEFLAG, editDueDt.getColumnValues(COLUMN_DUEDTOVERRIDEFLAG, ";"));
            this.getActionProcessor().processAction("EditSDIWorkItem", "1", props);
        }
    }

    private void calcSDIWorkItemDueDtBasedonSampleColumn(DataSet primary) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        String workItemOffsetColumn = policy.getProperty("workitemduedateoffsetcolumn", "sdiworkitem.createdt");
        boolean matchSDIWorkItemDueDt = "Y".equalsIgnoreCase(policy.getProperty("matchworkitemduedatetosample", "N"));
        if (workItemOffsetColumn.startsWith("sample.")) {
            workItemOffsetColumn = workItemOffsetColumn.substring(workItemOffsetColumn.indexOf(".") + 1);
            DataSet editSDIWI = new DataSet();
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, workItemOffsetColumn)) continue;
                String sampleId = primary.getString(i, COLUMN_KEYID1);
                Calendar offsetDt = primary.getCalendar(i, workItemOffsetColumn);
                this.database.createPreparedResultSet("sdiwi", "SELECT sample.duedt,s.workitemid,s.workiteminstance, s.sdcid,s.keyid1, COALESCE(s.duedtoffset, w.duedtoffset) offset, COALESCE(s.duedtoffsettimeunit, w.duedtoffsettimeunit) offsettimeunit FROM s_sample sample, sdiworkitem s, workitem w  WHERE sample.s_sampleid = s.keyid1 AND s.sdcid = 'Sample' AND s.keyid1 = ? AND (s.duedtoverrideflag IS NULL OR s.duedtoverrideflag != 'Y') AND w.workitemid = s.workitemid AND w.workitemversionid = s.workitemversionid", new String[]{sampleId});
                DataSet dsWI = new DataSet(this.database.getResultSet("sdiwi"));
                for (int w = 0; w < dsWI.getRowCount(); ++w) {
                    Calendar sampleDueDt = dsWI.getCalendar(w, COLUMN_DUEDT);
                    String dueDTOffset = dsWI.getValue(w, "offset", "");
                    String dueDTOffsetUnit = dsWI.getString(w, "offsettimeunit", "");
                    if (offsetDt == null || dueDTOffset.length() <= 0 || dueDTOffsetUnit.length() <= 0) continue;
                    Calendar calcDate = DateTimeUtil.getOffsetDate(offsetDt, dueDTOffsetUnit, new BigDecimal(dueDTOffset));
                    if (matchSDIWorkItemDueDt && (!matchSDIWorkItemDueDt || calcDate.after(sampleDueDt))) continue;
                    int r = editSDIWI.addRow();
                    editSDIWI.setString(r, "keyid1", sampleId);
                    editSDIWI.setString(r, "workitemid", dsWI.getString(w, "workitemid"));
                    editSDIWI.setString(r, "workiteminstance", dsWI.getValue(w, "workiteminstance"));
                    editSDIWI.setString(r, COLUMN_DUEDT, new M18NUtil(this.connectionInfo).format(calcDate));
                    editSDIWI.setString(r, COLUMN_DUEDTOVERRIDEFLAG, "N");
                }
            }
            this.database.closeResultSet("sdiwi");
            if (editSDIWI.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDC_SAMPLE);
                props.setProperty("keyid1", editSDIWI.getColumnValues("keyid1", ";"));
                props.setProperty("workitemid", editSDIWI.getColumnValues("workitemid", ";"));
                props.setProperty("workiteminstance", editSDIWI.getColumnValues("workiteminstance", ";"));
                props.setProperty(COLUMN_DUEDT, editSDIWI.getColumnValues(COLUMN_DUEDT, ";"));
                props.setProperty(COLUMN_DUEDTOVERRIDEFLAG, editSDIWI.getColumnValues(COLUMN_DUEDTOVERRIDEFLAG, ";"));
                this.getActionProcessor().processAction("EditSDIWorkItem", "1", props);
            }
        }
    }

    private void matchSDIWorkItemDueDate(DataSet primary) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        boolean matchSDIWorkItem = "Y".equalsIgnoreCase(policy.getProperty("matchworkitemduedatetosample", "N"));
        if (matchSDIWorkItem) {
            DataSet editSDIWI = new DataSet();
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_DUEDT)) continue;
                String sampleId = primary.getString(i, COLUMN_KEYID1);
                this.database.createPreparedResultSet("select distinct w.workitemid, w.workiteminstance from s_sample s, sdiworkitem w  where w.sdcid = 'Sample' and w.keyid1 = s.s_sampleid and w.duedt > s.duedt and (w.duedtoverrideflag IS NULL OR w.duedtoverrideflag !='Y') and s.s_sampleid = ?", new String[]{sampleId});
                DataSet dsWI = new DataSet(this.database.getResultSet());
                this.database.closeResultSet();
                for (int w = 0; w < dsWI.getRowCount(); ++w) {
                    int r = editSDIWI.addRow();
                    editSDIWI.setString(r, "keyid1", sampleId);
                    editSDIWI.setString(r, "workitemid", dsWI.getString(w, "workitemid"));
                    editSDIWI.setString(r, "workiteminstance", dsWI.getValue(w, "workiteminstance"));
                    editSDIWI.setString(r, COLUMN_DUEDT, new M18NUtil(this.connectionInfo).format(primary.getCalendar(i, COLUMN_DUEDT)));
                }
            }
            if (editSDIWI.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDC_SAMPLE);
                props.setProperty("keyid1", editSDIWI.getColumnValues("keyid1", ";"));
                props.setProperty("workitemid", editSDIWI.getColumnValues("workitemid", ";"));
                props.setProperty("workiteminstance", editSDIWI.getColumnValues("workiteminstance", ";"));
                props.setProperty(COLUMN_DUEDT, editSDIWI.getColumnValues(COLUMN_DUEDT, ";"));
                this.getActionProcessor().processAction("EditSDIWorkItem", "1", props);
            }
        }
    }

    private void calculateSampleDueDate(DataSet primary, PropertyList actionProps, String mode) throws SapphireException {
        DataSet editDueDtOverrideFlag = new DataSet();
        DataSet editDueDt = new DataSet();
        DataSet beforeEditImage = new DataSet();
        boolean addMode = "Add".equalsIgnoreCase(mode);
        if (!addMode) {
            beforeEditImage = this.getBeforeEditImage().getDataset("primary");
        }
        PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        String offsetColumn = policy.getProperty("sampleduedateoffsetcolumn", COLUMN_RECEIVEDDT);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            Calendar previousDueDate = null;
            String previousDueDtOverrideFlag = null;
            if (!addMode) {
                previousDueDate = beforeEditImage.getCalendar(i, COLUMN_DUEDT);
                previousDueDtOverrideFlag = beforeEditImage.getString(i, COLUMN_DUEDTOVERRIDEFLAG, "");
            }
            Calendar primaryDueDate = primary.getCalendar(i, COLUMN_DUEDT);
            String dueadateOverrideFlag = primary.getString(i, COLUMN_DUEDTOVERRIDEFLAG, "N");
            if (primaryDueDate != null && !"N".equalsIgnoreCase(dueadateOverrideFlag)) {
                if ("Y".equalsIgnoreCase(dueadateOverrideFlag) || "Y".equalsIgnoreCase(previousDueDtOverrideFlag)) continue;
                int r = editDueDtOverrideFlag.addRow();
                editDueDtOverrideFlag.setString(r, "keyid1", primary.getString(i, COLUMN_KEYID1));
                editDueDtOverrideFlag.setString(r, COLUMN_DUEDTOVERRIDEFLAG, "Y");
                continue;
            }
            if ("Y".equalsIgnoreCase(previousDueDtOverrideFlag) || !this.hasPrimaryValueChanged(primary, i, offsetColumn) && !this.hasPrimaryValueChanged(primary, i, "duedtoffset") && !this.hasPrimaryValueChanged(primary, i, "duedtoffsettimeunit")) continue;
            String duedateOffset = primary.getValue(i, "duedtoffset", "");
            String duedateOffsetUnit = primary.getString(i, "duedtoffsettimeunit", "");
            Calendar offsetColumnValue = primary.getCalendar(i, offsetColumn);
            if (!addMode) {
                if (duedateOffset.length() == 0) {
                    duedateOffset = beforeEditImage.getValue(i, "duedtoffset", "");
                }
                if (duedateOffsetUnit.length() == 0) {
                    duedateOffsetUnit = beforeEditImage.getString(i, "duedtoffsettimeunit", "");
                }
                if (offsetColumnValue == null) {
                    offsetColumnValue = beforeEditImage.getCalendar(i, offsetColumn);
                }
            }
            if (offsetColumnValue == null || duedateOffset.length() <= 0 || duedateOffsetUnit.length() <= 0) continue;
            Calendar calcDate = DateTimeUtil.getOffsetDate(offsetColumnValue, duedateOffsetUnit, new BigDecimal(duedateOffset));
            if (previousDueDate != null && calcDate.compareTo(previousDueDate) == 0 && !"N".equalsIgnoreCase(previousDueDtOverrideFlag)) continue;
            int r = editDueDt.addRow();
            editDueDt.setString(r, "keyid1", primary.getString(i, COLUMN_KEYID1));
            editDueDt.setString(r, COLUMN_DUEDT, new M18NUtil(this.connectionInfo).format(calcDate));
            editDueDt.setString(r, COLUMN_DUEDTOVERRIDEFLAG, "N");
        }
        if (editDueDtOverrideFlag.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_SAMPLE);
            props.setProperty("keyid1", editDueDtOverrideFlag.getColumnValues("keyid1", ";"));
            props.setProperty(COLUMN_DUEDTOVERRIDEFLAG, editDueDtOverrideFlag.getColumnValues(COLUMN_DUEDTOVERRIDEFLAG, ";"));
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
        if (editDueDt.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_SAMPLE);
            props.setProperty("keyid1", editDueDt.getColumnValues("keyid1", ";"));
            props.setProperty(COLUMN_DUEDT, editDueDt.getColumnValues(COLUMN_DUEDT, ";"));
            props.setProperty(COLUMN_DUEDTOVERRIDEFLAG, editDueDt.getColumnValues(COLUMN_DUEDTOVERRIDEFLAG, ";"));
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
    }

    private void processStudyWorkitems(DataSet primary, PropertyList actionProps) throws SapphireException {
        DataSet ds;
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select s.s_sampleid, s.sampletypeid, s.sstudyid, workitem.workitemid, workitem.workitemversionid, swi.embedchildsampleplanid,");
        sql.append(" swi.embedchildsampleplanversionid, swi.s_assigneddepartment, swi.reflexrule, swi.s_sampletypeid, swi.usersequence, ");
        sql.append(" (case when swi.applyonaddflag is not null then swi.applyonaddflag else 'Y' end) applyonaddflag,");
        sql.append(" (select count(s_childsampleplanversionid) from s_childsampleplanitem where s_childsampleplanid = workitem.embedchildsampleplanid and s_childsampleplanversionid = workitem.embedchildsampleplanversionid and plantype = 'Aliquot') aliquotplancount");
        sql.append(" from s_sample s, sdiworkitem swi, workitem");
        sql.append(" where swi.sdcid = 'Study'");
        sql.append(" and swi.keyid1 = s.sstudyid");
        sql.append(" and (swi.s_sampletypeid = s.sampletypeid or swi.s_sampletypeid is null)");
        sql.append(" and workitem.workitemid = swi.workitemid");
        sql.append(" and workitem.workitemversionid = (case when swi.workitemversionid is null then (select w.workitemversionid from workitem w where w.workitemid=swi.workitemid and w.versionstatus='C') else swi.workitemversionid end)");
        if (primary.size() > 1000) {
            String rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, primary.getColumnValues(COLUMN_KEYID1, ";"), null, null);
            sql.append(" and s.s_sampleid in (select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            sql.append(" order by s.sstudyid, swi.s_sampletypeid, s.s_sampleid, swi.usersequence");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (OpalUtil.isNotEmpty(rsetid)) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        } else {
            sql.append(" and s.s_sampleid in (").append(safeSQL.addIn(primary.getColumnValues(COLUMN_KEYID1, "','"))).append(")");
            sql.append(" order by s.sstudyid, swi.s_sampletypeid, s.s_sampleid, swi.usersequence");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        DataSet allExistingTargetAttributes = new DataSet();
        DataSet sdiattributesToAdd = new DataSet();
        DataSet sdiattributesToEdit = new DataSet();
        if (ds != null && ds.size() > 0) {
            ArrayList<DataSet> al = ds.getGroupedDataSets("sstudyid");
            for (int a = 0; a < al.size(); ++a) {
                DataSet dsStudy = al.get(a);
                DataSet actionds = new DataSet();
                boolean reflexRuleExists = false;
                String studyid = "";
                for (int i = 0; i < dsStudy.size(); ++i) {
                    int row = actionds.addRow();
                    actionds.setString(row, COLUMN_KEYID1, dsStudy.getString(i, COLUMN_KEYID1, ""));
                    actionds.setString(row, "workitemid", dsStudy.getString(i, "workitemid", ""));
                    actionds.setString(row, "workitemversionid", dsStudy.getString(i, "workitemversionid", ""));
                    actionds.setString(row, "sourcesstudyid", dsStudy.getString(i, "sstudyid", ""));
                    actionds.setString(row, "embedchildsampleplanid", dsStudy.getString(i, "embedchildsampleplanid", ""));
                    actionds.setString(row, "embedchildsampleplanversionid", dsStudy.getString(i, "embedchildsampleplanversionid", ""));
                    actionds.setString(row, "s_assigneddepartment", dsStudy.getString(i, "s_assigneddepartment", ""));
                    actionds.setString(row, "propsmatchtestmethodorder", dsStudy.getValue(i, "usersequence", ""));
                    int aliquotplancount = dsStudy.getInt(i, "aliquotplancount", 0);
                    if (actionProps.getProperty("parent_sampleid").length() > 0 && aliquotplancount > 0) {
                        actionds.setString(row, "applyworkitem", "N");
                    } else {
                        actionds.setString(row, "applyworkitem", dsStudy.getString(i, "applyonaddflag", "N"));
                    }
                    String reflexRule = dsStudy.getValue(i, "reflexrule");
                    if (reflexRuleExists || reflexRule.length() <= 0 || "null".equalsIgnoreCase(reflexRule)) continue;
                    reflexRuleExists = true;
                    studyid = dsStudy.getValue(i, "sstudyid");
                }
                if (actionds.size() <= 0) continue;
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDC_SAMPLE);
                if (reflexRuleExists) {
                    actionds.setString(-1, "workitemid", "_Reflex");
                    actionds.setString(-1, "workitemversionid", "1");
                    props.setProperty("__sourcesdcid", "Study");
                    props.setProperty("__sourcekeyids", studyid + ";(null);(null)");
                    actionds = this.removeDuplicateSampleRows(actionds);
                } else {
                    props.setProperty("applyworkitem", actionds.getColumnValues("applyworkitem", ";"));
                }
                props.setProperty("workitemid", actionds.getColumnValues("workitemid", ";"));
                props.setProperty("workitemversionid", actionds.getColumnValues("workitemversionid", ";"));
                props.setProperty("keyid1", actionds.getColumnValues(COLUMN_KEYID1, ";"));
                props.setProperty("sourcesstudyid", actionds.getColumnValues("sourcesstudyid", ";"));
                props.setProperty("embedchildsampleplanid", actionds.getColumnValues("embedchildsampleplanid", ";"));
                props.setProperty("embedchildsampleplanversionid", actionds.getColumnValues("embedchildsampleplanversionid", ";"));
                props.setProperty("s_assigneddepartment", actionds.getColumnValues("s_assigneddepartment", ";"));
                props.setProperty("propsmatchtestmethodorder", actionds.getColumnValues("propsmatchtestmethodorder", ";"));
                String forceNew = GetForceNewPolicy.analyzeForceNewPolicy("Y", this.getConfigurationProcessor());
                props.setProperty("forcenew", forceNew);
                props.setProperty("propsmatch", "Y");
                props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                if (reflexRuleExists) {
                    props.setProperty("bbsample", "Y");
                    if (actionProps.getProperty("parent_sampleid").length() > 0) {
                        props.setProperty("bbchildsample", "Y");
                    }
                }
                this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
                DataSet dsInstance = new DataSet(props.getProperty("newworkiteminstancexml"));
                if (dsInstance.getRowCount() <= 0) continue;
                if (OpalUtil.isEmpty(studyid) && (studyid = props.getProperty("sourcesstudyid", "")).contains(";")) {
                    studyid = studyid.substring(0, studyid.indexOf(";"));
                }
                String sourceSDCId = "Study";
                String source_keyid1 = studyid;
                String source_keyid2 = "(null)";
                String source_keyid3 = "(null)";
                QueryProcessor qp = this.getQueryProcessor();
                DataSet dsSourceSDIWIAttributes = qp.getPreparedSqlDataSet("select a.*, w.workitemid, w.workitemversionid from sdiattribute a, sdiworkitem w  where a.sdcid = ? and  a.keyid1 = w.sdiworkitemid and  w.sdcid = ? and w.keyid1 = ? and w.keyid2 = ? and w.keyid3 = ? ", (Object[])new String[]{"SDIWorkItem", sourceSDCId, source_keyid1, source_keyid2, source_keyid3}, true);
                if (reflexRuleExists) {
                    safeSQL.reset();
                    dsInstance = qp.getPreparedSqlDataSet("select * from sdiworkitem sw1, sdiworkitem sw2 where sw1.sdcid = sw2.sdcid and sw1.keyid1 = sw2.keyid1 and sw1.keyid2 = sw2.keyid2 and sw1.keyid3 = sw2.keyid3  and sw1.groupid = sw2.groupid and sw1.groupinstance = sw2.groupinstance and sw1.workitemtypeflag = " + safeSQL.addVar("W") + " and sw2.sdiworkitemid in(" + safeSQL.addIn(dsInstance.getColumnValues("sdiworkitemid", "','")) + ")", safeSQL.getValues());
                }
                if (dsSourceSDIWIAttributes.getRowCount() <= 0) continue;
                CopySDIDetail.copyDownSourceAttributes(dsSourceSDIWIAttributes, dsInstance, qp, this.getSDCProcessor(), allExistingTargetAttributes, new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.logger, new M18NUtil(this.connectionInfo), sdiattributesToAdd, sdiattributesToEdit);
            }
        }
        if (sdiattributesToAdd.getRowCount() > 0 || sdiattributesToEdit.getRowCount() > 0) {
            CopySDIDetail.addEditAttributes(sdiattributesToAdd, sdiattributesToEdit, allExistingTargetAttributes, this.logger, this.database);
        }
    }

    private DataSet removeDuplicateSampleRows(DataSet ds) {
        DataSet uniqueSampleDS = new DataSet();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (uniqueSampleDS.findRow(COLUMN_KEYID1, ds.getValue(i, COLUMN_KEYID1)) >= 0) continue;
            uniqueSampleDS.copyRow(ds, i, 1);
        }
        return uniqueSampleDS;
    }

    public void addBlankDocumentsFromStudy(String sampleIds, String mode) throws SapphireException {
        DataSet formds;
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select s.s_sampleid, s.sstudyid, sfr.formid, sfr.forminstance, sfr.formversionid, sfr.formrule,");
        sql.append(" ( select max(form.formversionid) from form where form.formid = sfr.formid ) defaultformversionid,");
        sql.append("  sf.studysiteid, s.samplestatus, sf.s_samplefamilyid, ");
        sql.append("  sf.subjectid ");
        sql.append(" from s_sample s, s_samplefamily sf, sdiformrule sfr");
        sql.append(" where sf.s_samplefamilyid = s.samplefamilyid");
        sql.append(" and sfr.sdcid = 'Study'");
        sql.append(" and sfr.keyid1 = s.sstudyid");
        if (sampleIds.length() > 750) {
            String rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, sampleIds, null, null);
            sql.append(" and s.s_sampleid in (select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            sql.append(" order by sfr.usersequence");
            formds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            sql.append(" and s.s_sampleid in (").append(safeSQL.addIn(sampleIds, ";")).append(")");
            sql.append(" order by sfr.usersequence");
            formds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        DataSet formsDataSet = new DataSet();
        if (formds != null && formds.size() > 0) {
            for (int i = 0; i < formds.size(); ++i) {
                String rule = formds.getValue(i, "formrule", "");
                if (!rule.toLowerCase().contains("sample") || !rule.contains(":")) continue;
                String ruledef = formds.getValue(i, "formrule").split(":")[1].trim();
                if ("add".equalsIgnoreCase(mode) && ruledef.toLowerCase().contains("allocation")) {
                    formsDataSet.copyRow(formds, i, 1);
                    continue;
                }
                if (!formds.getValue(i, COLUMN_SAMPLESTATUS).equalsIgnoreCase("received") || !ruledef.toLowerCase().contains("receipt")) continue;
                formsDataSet.copyRow(formds, i, 1);
            }
        }
        if (formsDataSet.size() > 0) {
            try {
                FormUtil.addBlankDocument(formsDataSet, this.getActionProcessor(), "LV_SampleFamily", "s_samplefamilyid");
            }
            catch (ActionException e) {
                this.logger.error("Unable to add Blank Document to Sample " + formsDataSet.getColumnValues(COLUMN_KEYID1, ";") + ". Exception raised is: " + e.getMessage(), e);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        int i;
        HashMap sampleInfoMap;
        DataSet primary;
        DataSet beforeEditImage;
        long start;
        block41: {
            block42: {
                ArrayList ds;
                boolean fixdatamode;
                block43: {
                    String rsetid;
                    if ("Y".equals(actionProps.getProperty("__samplePreEditRuleIgnore"))) {
                        return;
                    }
                    if ("Y".equals(actionProps.getProperty("__studyswitchedit"))) {
                        return;
                    }
                    start = System.currentTimeMillis();
                    Trace.logInfo("START: Sample.preEdit() [" + start + "]");
                    beforeEditImage = this.getBeforeEditImage().getDataset("primary");
                    primary = sdiData.getDataset("primary");
                    sampleInfoMap = this.getSampleInfoMap(primary);
                    this.checkSampleStatus(primary);
                    for (int i2 = 0; i2 < beforeEditImage.size(); ++i2) {
                        String classification = beforeEditImage.getString(i2, COLUMN_CLASSIFICATION);
                        if (classification != null && classification.trim().length() != 0) continue;
                        this.setSampleClassification(primary, beforeEditImage);
                        break;
                    }
                    boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
                    fixdatamode = "Y".equals(actionProps.getProperty("fixdatamode", "N"));
                    if (!this.isBioBankSample(primary, false)) break block41;
                    this.syncStorageStatus(primary, sampleInfoMap);
                    this.checkStorageStatus(primary, sampleInfoMap);
                    this.checkDisposedRule(primary, forceUpdate);
                    HashSet<String> studySwitchSet = new HashSet<String>();
                    for (int i3 = 0; i3 < primary.size(); ++i3) {
                        if (!this.hasPrimaryValueChanged(primary, i3, "sstudyid")) continue;
                        studySwitchSet.add(primary.getString(i3, COLUMN_KEYID1));
                    }
                    if (studySwitchSet.size() <= 0) break block42;
                    ds = null;
                    StringBuilder sql = new StringBuilder();
                    SafeSQL safeSQL = new SafeSQL();
                    sql.append("select s.s_sampleid, s.sstudyid, s.storagestatus,");
                    sql.append(" (select count(sm.destsampleid) from s_samplemap sm where sm.destsampleid = s.s_sampleid) childcount");
                    sql.append(" from s_sample s");
                    if (studySwitchSet.size() < 1000) {
                        sql.append(" where s_sampleid in ( ").append(safeSQL.addIn(studySwitchSet)).append(" )");
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    } else {
                        rsetid = null;
                        rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, OpalUtil.toDelimitedString(studySwitchSet, ";"), null, null);
                        sql.append(" where s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        if (OpalUtil.isNotEmpty(rsetid)) {
                            this.getDAMProcessor().clearRSet(rsetid);
                        }
                    }
                    break block43;
                    catch (SapphireException e) {
                        try {
                            e.printStackTrace();
                        }
                        catch (Throwable throwable) {
                            if (OpalUtil.isNotEmpty(rsetid)) {
                                this.getDAMProcessor().clearRSet(rsetid);
                            }
                            throw throwable;
                        }
                        if (OpalUtil.isNotEmpty(rsetid)) {
                            this.getDAMProcessor().clearRSet(rsetid);
                        }
                    }
                }
                if (ds != null && ds.size() > 0) {
                    for (int i4 = 0; i4 < ds.size(); ++i4) {
                        if (!fixdatamode && !validStudySwitchStatusList.contains(((DataSet)ds).getString(i4, COLUMN_STORAGESTATUS))) {
                            throw new SapphireException("Invalid Sample Study Switch", "VALIDATION", this.getTranslationProcessor().translate("Sample(s) are not in valid status for Study switch."));
                        }
                        if (((DataSet)ds).getInt(i4, "childcount") <= 0) continue;
                        throw new SapphireException("Invalid Sample Study Switch", "VALIDATION", this.getTranslationProcessor().translate("Switching study on child sample is not allowed."));
                    }
                }
            }
            this.validateProcessedDt(primary);
        }
        this.checkForReviewedTimePoint(primary, "Edit");
        StringBuffer sampleIds = new StringBuffer();
        ArrayList<String> sampleFlags = new ArrayList<String>();
        for (int i5 = 0; i5 < primary.getRowCount(); ++i5) {
            String sampleId = primary.getValue(i5, COLUMN_KEYID1);
            String batchId = "";
            String classification = "";
            String batchStageId = "";
            String requestId = "";
            String monitorGroupId = "";
            String studyId = "";
            batchId = primary.isValidColumn("batchid") ? primary.getValue(i5, "batchid") : beforeEditImage.getValue(i5, "batchid");
            classification = primary.isValidColumn(COLUMN_CLASSIFICATION) ? primary.getValue(i5, COLUMN_CLASSIFICATION) : beforeEditImage.getValue(i5, COLUMN_CLASSIFICATION);
            batchStageId = primary.isValidColumn("batchstageid") ? primary.getValue(i5, "batchstageid") : beforeEditImage.getValue(i5, "batchstageid");
            requestId = primary.isValidColumn(COLUMN_REQUESTID) ? primary.getValue(i5, COLUMN_REQUESTID) : beforeEditImage.getValue(i5, COLUMN_REQUESTID);
            monitorGroupId = primary.isValidColumn(COLUMN_MONITORGROUPID) ? primary.getValue(i5, COLUMN_MONITORGROUPID) : beforeEditImage.getValue(i5, COLUMN_MONITORGROUPID);
            studyId = primary.isValidColumn("studyid") ? primary.getValue(i5, "studyid") : beforeEditImage.getValue(i5, "studyid");
            if ((!CLASSIFICATION_QM.equals(classification) || batchId.length() <= 0 && batchStageId.length() <= 0) && requestId.length() <= 0 && monitorGroupId.length() <= 0 && studyId.length() <= 0) continue;
            sampleIds.append(";").append(sampleId);
            if (batchId.length() > 0) {
                sampleFlags.add("batch");
            }
            if (batchStageId.length() > 0) {
                sampleFlags.add("batchstage");
            }
            if (requestId.length() > 0) {
                sampleFlags.add("request");
            }
            if (monitorGroupId.length() > 0) {
                sampleFlags.add("monitorgroup");
            }
            if (studyId.length() <= 0) continue;
            sampleFlags.add("study");
        }
        PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        if (sampleIds.length() > 0) {
            String rsetid = null;
            try {
                rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, sampleIds.substring(1), null, null);
                String errorMessage = this.isSampleEditAllowed(policy, rsetid, primary, sampleFlags);
                if (errorMessage.length() > 0) {
                    throw new SapphireException(this.getTranslationProcessor().translate(errorMessage));
                }
                this.getDAMProcessor().clearRSet(rsetid);
            }
            catch (Throwable throwable) {
                this.getDAMProcessor().clearRSet(rsetid);
                throw throwable;
            }
        }
        if ("Y".equals(policy.getProperty(POLICY_HONORSAMPLERECEIVEFLAG))) {
            for (int i6 = 0; i6 < primary.getRowCount(); ++i6) {
                boolean autoReceiveFlag = "Y".equals(StringUtil.getYN(primary.getString(i6, COLUMN_AUTORECEIVEFLAG), "N"));
                String sampleStatus = beforeEditImage.getString(i6, COLUMN_SAMPLESTATUS, "");
                boolean templateFlag = "Y".equals(StringUtil.getYN(primary.getString(i6, COLUMN_TEMPLATEFLAG), "N"));
                if (!autoReceiveFlag || !sampleStatus.equals(SAMPLESTATUS_INITIAL) || templateFlag) continue;
                primary.setString(i6, COLUMN_SAMPLESTATUS, "Received");
                primary.setDate(i6, COLUMN_RECEIVEDDT, DateTimeUtil.getNowTimestamp());
                primary.setString(i6, COLUMN_RECEIVEDBY, this.getSysuserId());
            }
        }
        this.setTargetDisposalDT(primary, sampleInfoMap);
        this.setSampleStatus(primary, sampleInfoMap);
        this.setNullToCurrentProductVersion(primary);
        this.resolveSampleTypesAndSubTypes(primary, false);
        StringBuffer cancelList = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        for (int i7 = 0; i7 < primary.size(); ++i7) {
            if (!this.hasPrimaryValueChanged(primary, i7, COLUMN_SAMPLESTATUS)) continue;
            String wapStatus = primary.getValue(i7, "wapstatus");
            String oldWapStatus = this.getOldPrimaryValue(primary, i7, "wapstatus");
            String oldSampleStatus = this.getOldPrimaryValue(primary, i7, COLUMN_SAMPLESTATUS);
            String sampleId = primary.getValue(i7, COLUMN_KEYID1);
            String sampleStatus = primary.getString(i7, COLUMN_SAMPLESTATUS);
            if (SAMPLESTATUS_CANCELLED.equals(sampleStatus)) {
                primary.setDate(i7, COLUMN_CANCELLEDDT, "n");
                primary.setString(i7, COLUMN_CANCELLEDBY, this.getSysuserId());
                primary.setString(i7, COLUMN_STORAGESTATUS, SAMPLESTATUS_CANCELLED);
                cancelList.append(";").append(primary.getString(i7, COLUMN_KEYID1));
                if (!"Pending".equalsIgnoreCase(oldWapStatus)) continue;
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select w.activityid from activityworksdi w, s_sample s  where s.s_sampleid = " + safeSQL.addVar(sampleId) + " and  w.worksdcid = 'Sample' and w.workkeyid1 = s.s_sampleid", safeSQL.getValues());
                if (ds.getRowCount() == 0) {
                    primary.setString(i7, "wapstatus", SAMPLESTATUS_CANCELLED);
                }
                safeSQL.reset();
                continue;
            }
            if (SAMPLESTATUS_CANCELLED.equalsIgnoreCase(oldSampleStatus) && SAMPLESTATUS_CANCELLED.equalsIgnoreCase(oldWapStatus)) {
                primary.setString(i7, "wapstatus", "Pending");
                continue;
            }
            if (!SAMPLESTATUS_COMPLETED.equalsIgnoreCase(oldSampleStatus) || !SAMPLESTATUS_INPROGRESS.equals(sampleStatus) && !SAMPLESTATUS_INITIAL.equals(sampleStatus) && !"Received".equals(sampleStatus)) continue;
            primary.setString(i7, COLUMN_COMPLETEDT, "");
        }
        if (cancelList.length() > 0) {
            PropertyList wf = new PropertyList();
            wf.setProperty("sdcid", SDC_SAMPLE);
            wf.setProperty("keyid1", cancelList.substring(1));
            this.getActionProcessor().processAction("RemoveFromWorkflow", "1", wf);
        }
        boolean locationChanged = false;
        for (i = 0; i < primary.getRowCount(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "samplepointid") && !this.hasPrimaryValueChanged(primary, i, "locationid")) continue;
            locationChanged = true;
            break;
        }
        if (locationChanged) {
            this.setLocationPath(primary);
        }
        if (!this.isCMTImport()) {
            this.setWapData(primary, false, actionProps);
        }
        whoDoneIt.process(primary, this);
        if (primary.isValidColumn(COLUMN_STORAGESTATUS)) {
            for (i = 0; i < primary.size(); ++i) {
                String previousStorageStatus;
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_STORAGESTATUS) || !"Disposed".equals(previousStorageStatus = primary.getString(i, COLUMN_PREVSTORAGESTATUS))) continue;
                String sampleid = primary.getString(i, COLUMN_KEYID1);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select samplestatus from a_s_sample where s_sampleid = ? order by moddt desc", (Object[])new String[]{sampleid});
                boolean isdisposed = false;
                String previoussamplestatus = "Received";
                for (int row = 0; row < ds.size(); ++row) {
                    if (!isdisposed) {
                        if (!"Disposed".equals(ds.getString(row, COLUMN_SAMPLESTATUS, ""))) continue;
                        isdisposed = true;
                        continue;
                    }
                    previoussamplestatus = ds.getString(row, COLUMN_SAMPLESTATUS);
                    if (!"Disposed".equals(previoussamplestatus)) break;
                }
                primary.setString(i, COLUMN_SAMPLESTATUS, previoussamplestatus);
            }
        }
        if (!actionProps.getProperty("skipapprovalrulecheck", "").equalsIgnoreCase("Y")) {
            this.processApproval(primary);
        }
        Trace.logInfo("END: Sample.preEdit() [" + start + "]. Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private void processApproval(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String status = primary.getString(i, COLUMN_SAMPLESTATUS, "");
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) || !status.equalsIgnoreCase(SAMPLESTATUS_COMPLETED)) continue;
            PropertyList plProps = new PropertyList();
            plProps.setProperty("sdcid", SDC_SAMPLE);
            plProps.setProperty("keyid1", primary.getValue(i, COLUMN_KEYID1, ""));
            plProps.setProperty("ready", "Y");
            this.getActionProcessor().processAction("ResetSDIApproval", "1", plProps);
        }
    }

    private String checkSamplePlanItemReviewDisposition(String rsetid) {
        String errorMessage = "";
        SafeSQL safeSQL = new SafeSQL();
        DataSet reviewedSamples = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s.s_sampleid FROM s_sample s, rsetitems r, scheduleplanitem sp WHERE r.rsetid = " + safeSQL.addVar(rsetid) + " AND r.sdcid = 'Sample' AND s.s_sampleid = r.keyid1 AND sp.scheduleplanid = s.eventplan AND sp.scheduleplanitemid = s.eventplanitem AND sp.reviewdispositionflag IN( 'R','A' )", safeSQL.getValues());
        if (reviewedSamples.getRowCount() > 0) {
            errorMessage = this.getTranslationProcessor().translate("Samples under reviewed Study Timepoint cannot be edited") + ": <BR>" + reviewedSamples.getColumnValues(COLUMN_KEYID1, ", ");
        }
        return errorMessage;
    }

    public void checkForReviewedTimePoint(DataSet primary, String mode) throws SapphireException {
        PreparedStatement timepointStatusStmt = this.database.prepareStatement("gettimepointstatus", "select s.studyid, sp.reviewdispositionflag from study_scheduleplan s, scheduleplanitem sp where s.scheduleplanid = sp.scheduleplanid and sp.scheduleplanid = ? and sp.scheduleplanitemid = ?");
        try {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String reviewFlag;
                String eventPlan = primary.getValue(i, "eventplan");
                String eventPlanItem = primary.getValue(i, "eventplanitem");
                if (eventPlan.length() <= 0 || eventPlanItem.length() <= 0 || !mode.equals("Add") && !this.hasPrimaryValueChanged(primary, i, "eventplan") && !this.hasPrimaryValueChanged(primary, i, "eventplanitem")) continue;
                timepointStatusStmt.setString(1, eventPlan);
                timepointStatusStmt.setString(2, eventPlanItem);
                DataSet dsReviewStatus = new DataSet(timepointStatusStmt.executeQuery());
                if (dsReviewStatus.getRowCount() <= 0 || !"R".equalsIgnoreCase(reviewFlag = dsReviewStatus.getValue(0, "reviewdispositionflag")) && !"A".equalsIgnoreCase(reviewFlag)) continue;
                String studyId = dsReviewStatus.getValue(0, "studyid");
                HashMap<String, String> token = new HashMap<String, String>();
                token.put("study", studyId);
                token.put("plan", eventPlan);
                token.put("planitem", eventPlanItem);
                throw new SapphireException(this.getTranslationProcessor().translate("Sample cannot be added to a reviewed Study Timepoint Study:[study] Plan:[plan] PlanItem:[planitem] ", token));
            }
        }
        catch (SQLException e) {
            this.logger.error("Failed to retrieve Study timepoint status:", e);
        }
        this.database.closeStatement("gettimepointstatus");
    }

    private String isSampleEditAllowed(PropertyList batchSamplePolicy, String rsetid, DataSet primary, ArrayList sampleFlags) throws SapphireException {
        String errorMessage = "";
        boolean isOnlySampleDisposal = false;
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String disposalStatus = primary.getString(i, COLUMN_DISPOSALSTATUS, "");
            boolean bl = isOnlySampleDisposal = this.hasPrimaryValueChanged(primary, i, COLUMN_DISPOSALSTATUS) && (disposalStatus.trim().toLowerCase().equals("disposed") || disposalStatus.trim().toLowerCase().equals("marked for disposal"));
            if (!isOnlySampleDisposal) break;
        }
        PropertyList allowSampleEditPolicyProperty = batchSamplePolicy.getPropertyListNotNull("allowsampleedit");
        boolean doNotAllowForReleasedBatch = allowSampleEditPolicyProperty.getProperty("batchreleased", "N").equals("N");
        boolean doNotAllowForReleasedBatchStage = allowSampleEditPolicyProperty.getProperty("batchstagereleased", "N").equals("N");
        boolean doNotAllowForReleasedRequest = allowSampleEditPolicyProperty.getProperty("requestreleased", "N").equals("N");
        boolean doNotAllowForReleasedBatchWithoutApproval = allowSampleEditPolicyProperty.getProperty("noapprovalbatchreleased", "N").equals("N");
        boolean doNotAllowForReleasedBatchStageWithoutApproval = allowSampleEditPolicyProperty.getProperty("noapprovalstagereleased", "N").equals("N");
        boolean doNotAllowForReleasedRequestWithoutApproval = allowSampleEditPolicyProperty.getProperty("noapprovalrequestreleased", "N").equals("N");
        boolean doNotAllowForRejectedBatch = allowSampleEditPolicyProperty.getProperty("batchrejected", "N").equals("N");
        boolean doNotAllowForRejectedBatchStage = allowSampleEditPolicyProperty.getProperty("batchstagerejected", "N").equals("N");
        boolean doNotAllowForRejectedRequest = allowSampleEditPolicyProperty.getProperty("requestrejected", "N").equals("N");
        if (!isOnlySampleDisposal) {
            StringBuffer str;
            SafeSQL safeSQL;
            if (sampleFlags.contains("batch") && (doNotAllowForReleasedBatch || doNotAllowForReleasedBatchWithoutApproval || doNotAllowForRejectedBatch)) {
                safeSQL = new SafeSQL();
                DataSet batchDs = null;
                for (int i = 0; i < primary.size(); ++i) {
                    String batchId;
                    boolean isClassificationPresent = primary.isValidColumn(COLUMN_CLASSIFICATION);
                    String sampleClassification = "";
                    if (!isClassificationPresent) {
                        String sampleId = primary.getString(i, COLUMN_KEYID1, "");
                        if (sampleId.length() > 0) {
                            safeSQL.reset();
                            StringBuilder classificationSql = new StringBuilder("select classification from s_sample where s_sampleid=");
                            classificationSql.append(safeSQL.addVar(sampleId));
                            DataSet classificationDs = this.getQueryProcessor().getPreparedSqlDataSet(classificationSql.toString(), safeSQL.getValues());
                            if (classificationDs.getRowCount() == 1) {
                                sampleClassification = classificationDs.getString(i, COLUMN_CLASSIFICATION, "");
                            }
                        }
                    } else {
                        sampleClassification = primary.getString(i, COLUMN_CLASSIFICATION, "");
                    }
                    if (sampleClassification.length() <= 0 || !sampleClassification.equals(CLASSIFICATION_QM) || !this.hasPrimaryValueChanged(primary, i, "batchid") || (batchId = primary.getString(i, "batchid", "")).length() <= 0) continue;
                    safeSQL.reset();
                    StringBuilder batchStatusSql = new StringBuilder("select s_batchid, batchstatus, appr.approvaltypeid");
                    batchStatusSql.append(" from s_batch");
                    batchStatusSql.append(" LEFT OUTER JOIN sdiapproval appr ON appr.sdcid = 'Batch' AND appr.keyid1 = s_batchid ");
                    batchStatusSql.append(" where s_batchid=").append(safeSQL.addVar(batchId));
                    batchStatusSql.append(" AND batchstatus in (");
                    batchStatusSql.append(safeSQL.addVar(BatchLifeCycleUtil.getBatchStateDisplayValue("released"))).append(", ");
                    batchStatusSql.append(safeSQL.addVar(BatchLifeCycleUtil.getBatchStateDisplayValue("rejected"))).append(") ");
                    DataSet batchDataset = this.getQueryProcessor().getPreparedSqlDataSet(batchStatusSql.toString(), safeSQL.getValues());
                    if (batchDataset.getRowCount() <= 0) continue;
                    batchDs = batchDataset;
                    break;
                }
                if (batchDs == null) {
                    safeSQL.reset();
                    StringBuffer str2 = new StringBuffer("SELECT appr.approvaltypeid, s_batchid, batchstatus");
                    str2.append(" FROM (SELECT b.s_batchid, b.batchstatus FROM rsetitems r, s_sample s, s_batch b");
                    str2.append(" WHERE r.rsetid=").append(safeSQL.addVar(rsetid));
                    str2.append(" AND r.keyid1 = s.s_sampleid");
                    str2.append(" AND s.classification = ").append(safeSQL.addVar(CLASSIFICATION_QM));
                    str2.append(" AND s.batchid = b.s_batchid");
                    str2.append(" AND b.batchstatus IN ( ").append(safeSQL.addVar(BatchLifeCycleUtil.getBatchStateDisplayValue("released"))).append(", ");
                    str2.append(safeSQL.addVar(BatchLifeCycleUtil.getBatchStateDisplayValue("rejected"))).append(" ) ) batch ");
                    str2.append(" LEFT OUTER JOIN sdiapproval appr ON appr.sdcid = 'Batch' AND appr.keyid1 = s_batchid");
                    batchDs = this.getQueryProcessor().getPreparedSqlDataSet(str2.toString(), safeSQL.getValues());
                }
                if (batchDs.getRowCount() > 0) {
                    HashMap<String, String> findRow = new HashMap<String, String>();
                    findRow.put("batchstatus", BatchLifeCycleUtil.getBatchStateDisplayValue("rejected"));
                    if (batchDs.findRow(findRow) >= 0 && doNotAllowForRejectedBatch) {
                        errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a " + BatchLifeCycleUtil.getBatchStateDisplayValue("rejected") + " Batch. Check BatchSamplePolicy >> Allow any change to Child Sample >> For Rejected Batch settings for details.");
                    }
                    findRow = new HashMap();
                    findRow.put("batchstatus", BatchLifeCycleUtil.getBatchStateDisplayValue("released"));
                    if (batchDs.findRow(findRow) >= 0) {
                        findRow = new HashMap();
                        findRow.put("approvaltypeid", null);
                        int indexOfNullApprovalType = batchDs.findRow(findRow);
                        for (int i = 0; i < primary.getRowCount(); ++i) {
                            if (primary.isValidColumn(COLUMN_DISPOSALSTATUS) && this.hasPrimaryValueChanged(primary, i, COLUMN_DISPOSALSTATUS) && primary.getValue(i, COLUMN_DISPOSALSTATUS, "").equals("Disposed")) continue;
                            if (indexOfNullApprovalType >= 0) {
                                if (!doNotAllowForReleasedBatchWithoutApproval) continue;
                                errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a " + BatchLifeCycleUtil.getBatchStateDisplayValue("released") + " Batch. Check BatchSamplePolicy >> Allow any change to Child Sample >> For Released Batch (Without Approvals) settings for details.");
                                continue;
                            }
                            if (!doNotAllowForReleasedBatch) continue;
                            errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a " + BatchLifeCycleUtil.getBatchStateDisplayValue("released") + " Batch. Check BatchSamplePolicy >> Allow any change to Child Sample >> For Released Batch (With Approvals) settings for details.");
                        }
                    }
                }
            }
            if (sampleFlags.contains("batchstage") && (doNotAllowForReleasedBatchStage || doNotAllowForReleasedBatchStageWithoutApproval || doNotAllowForRejectedBatchStage)) {
                safeSQL = new SafeSQL();
                str = new StringBuffer("SELECT appr.approvaltypeid, s_batchstageid, batchstagestatus");
                str.append(" FROM (SELECT b.s_batchstageid, b.batchstagestatus FROM rsetitems r, s_sample s, s_batchstage b");
                str.append(" WHERE r.rsetid=").append(safeSQL.addVar(rsetid));
                str.append(" AND r.keyid1 = s.s_sampleid");
                str.append(" AND s.classification = '").append(CLASSIFICATION_QM).append("'");
                str.append(" AND s.batchstageid = b.s_batchstageid");
                str.append(" AND b.batchstagestatus IN ( '").append(LV_BatchStage.BATCHSTAGE_RELEASED).append("', '");
                str.append(LV_BatchStage.BATCHSTAGE_REJECTED).append("') ) batchstage ");
                str.append(" LEFT OUTER JOIN sdiapproval appr ON appr.sdcid = 'LV_BatchStage' AND appr.keyid1 = s_batchstageid");
                DataSet batchStageDs = this.getQueryProcessor().getPreparedSqlDataSet(str.toString(), safeSQL.getValues());
                if (batchStageDs.getRowCount() > 0) {
                    HashMap<String, String> findRow = new HashMap<String, String>();
                    findRow.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_REJECTED);
                    if (batchStageDs.findRow(findRow) >= 0 && doNotAllowForRejectedBatchStage) {
                        errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a " + LV_BatchStage.BATCHSTAGE_REJECTED + " Batch Stage. Check BatchSamplePolicy >> Allow any change to Child Sample >> For Rejected BatchStage settings for details.");
                    }
                    findRow = new HashMap();
                    findRow.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_RELEASED);
                    if (batchStageDs.findRow(findRow) >= 0) {
                        findRow = new HashMap();
                        findRow.put("approvaltypeid", null);
                        int indexOfNullApprovalType = batchStageDs.findRow(findRow);
                        if (indexOfNullApprovalType >= 0) {
                            if (doNotAllowForReleasedBatchStageWithoutApproval) {
                                errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a " + LV_BatchStage.BATCHSTAGE_RELEASED + " Batch Stage. Check BatchSamplePolicy >> Allow any change to Child Sample >> For Released BatchStage (Without Approvals) settings for details.");
                            }
                        } else if (doNotAllowForReleasedBatchStage) {
                            errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a " + LV_BatchStage.BATCHSTAGE_RELEASED + " Batch Stage. Check BatchSamplePolicy >> Allow any change to Child Sample >> For Released BatchStage (With Approvals) settings for details.");
                        }
                    }
                }
            }
            if (sampleFlags.contains("request") && (doNotAllowForReleasedRequest || doNotAllowForReleasedRequestWithoutApproval || doNotAllowForRejectedRequest)) {
                safeSQL = new SafeSQL();
                str = new StringBuffer("SELECT appr.approvaltypeid, s_requestid, requeststatus");
                str.append(" FROM (SELECT req.s_requestid, req.requeststatus FROM rsetitems r, s_sample s, s_request req");
                str.append(" WHERE r.rsetid=").append(safeSQL.addVar(rsetid));
                str.append(" AND r.keyid1 = s.s_sampleid");
                str.append(" AND s.requestid = req.s_requestid");
                str.append(" AND req.requeststatus IN ( '").append(RequestManagementUtil.RequestStatus.RELEASED.getStatusValue()).append("', '");
                str.append(RequestManagementUtil.RequestStatus.REJECTED.getStatusValue()).append("') ) request ");
                str.append(" LEFT OUTER JOIN sdiapproval appr ON appr.sdcid = 'Request' AND appr.keyid1 = s_requestid AND appr.approvalfunction = 'Release'");
                DataSet requestDs = this.getQueryProcessor().getPreparedSqlDataSet(str.toString(), safeSQL.getValues());
                if (requestDs.getRowCount() > 0) {
                    HashMap<String, String> findRow = new HashMap<String, String>();
                    findRow.put("requeststatus", RequestManagementUtil.RequestStatus.REJECTED.getStatusValue());
                    if (requestDs.findRow(findRow) >= 0 && doNotAllowForRejectedRequest) {
                        errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a " + RequestManagementUtil.RequestStatus.REJECTED.getStatusValue() + " Request. Check BatchSamplePolicy >> Allow any change to Child Sample >> For Rejected Request settings for details.");
                    }
                    findRow = new HashMap();
                    findRow.put("requeststatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
                    if (requestDs.findRow(findRow) >= 0) {
                        findRow = new HashMap();
                        findRow.put("approvaltypeid", null);
                        int indexOfNullApprovalType = requestDs.findRow(findRow);
                        if (indexOfNullApprovalType >= 0) {
                            if (doNotAllowForReleasedRequestWithoutApproval) {
                                errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a " + RequestManagementUtil.RequestStatus.RELEASED.getStatusValue() + " Request. Check BatchSamplePolicy >> Allow any change to Child Sample >> For Released Request (Without Approvals) settings for details.");
                            }
                        } else if (doNotAllowForReleasedRequest) {
                            errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a " + RequestManagementUtil.RequestStatus.RELEASED.getStatusValue() + " Request. Check BatchSamplePolicy >> Allow any change to Child Sample >> For Released Request (With Approvals) settings for details.");
                        }
                    }
                }
            }
            if (sampleFlags.contains("monitorgroup")) {
                safeSQL = new SafeSQL();
                str = new StringBuffer("SELECT appr.approvaltypeid, monitorgroupid, monitorgrouptype, monitorgroupstatus");
                str.append(" FROM (SELECT m.monitorgroupid, m.monitorgrouptype, m.monitorgroupstatus FROM rsetitems r, s_sample s, monitorgroup m");
                str.append(" WHERE r.rsetid=").append(safeSQL.addVar(rsetid));
                str.append(" AND r.keyid1 = s.s_sampleid");
                str.append(" AND s.monitorgroupid = m.monitorgroupid");
                str.append(" AND m.monitorgroupstatus IN ( '").append("Released").append("', '");
                str.append("Rejected").append("') ) mgroup ");
                str.append(" LEFT OUTER JOIN sdiapproval appr ON appr.sdcid = 'LV_MonitorGroup' AND appr.keyid1 = monitorgroupid");
                DataSet monitorGroupDs = this.getQueryProcessor().getPreparedSqlDataSet(str.toString(), safeSQL.getValues());
                if (monitorGroupDs.getRowCount() > 0) {
                    PropertyList monitorGroupTypeSettings;
                    boolean doNotAllowForRejectedMonitorGroup;
                    HashMap<String, String> findRow = new HashMap<String, String>();
                    findRow.put("monitorgroupstatus", "Rejected");
                    int indexOfRejectedMonitorGroup = monitorGroupDs.findRow(findRow);
                    if (indexOfRejectedMonitorGroup >= 0 && (doNotAllowForRejectedMonitorGroup = (monitorGroupTypeSettings = LV_MonitorGroup.getMonitorGroupTypeSettings(monitorGroupDs.getValue(indexOfRejectedMonitorGroup, "monitorgrouptype", ""), this.getConfigurationProcessor())).getPropertyListNotNull("allowsampleedit").getProperty("monitorgrouprejected", "N").equals("N"))) {
                        errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a Rejected MonitorGroup. Check MonitorGroupPolicy >> Monitor Group Types >> Allow any change to Child Sample >> For Rejected MonitorGroup settings for details.");
                    }
                    findRow = new HashMap();
                    findRow.put("monitorgroupstatus", "Released");
                    int indexOfReleasedMonitorGroup = monitorGroupDs.findRow(findRow);
                    if (indexOfReleasedMonitorGroup >= 0) {
                        PropertyList monitorGroupTypeSettings2;
                        findRow = new HashMap();
                        findRow.put("approvaltypeid", null);
                        int indexOfNullApprovalType = monitorGroupDs.findRow(findRow);
                        if (indexOfNullApprovalType >= 0) {
                            monitorGroupTypeSettings2 = LV_MonitorGroup.getMonitorGroupTypeSettings(monitorGroupDs.getValue(indexOfNullApprovalType, "monitorgrouptype", ""), this.getConfigurationProcessor());
                            boolean doNotAllowForReleasedMonitorGroupWithoutApproval = monitorGroupTypeSettings2.getPropertyListNotNull("allowsampleedit").getProperty("noapprovalmonitorgroupreleased", "N").equals("N");
                            if (doNotAllowForReleasedMonitorGroupWithoutApproval) {
                                errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a Released MonitorGroup. Check MonitorGroupPolicy >> Monitor Group Types >> Allow any change to Child Sample >> For Released MonitorGroup (Without Approvals) settings for details.");
                            }
                        } else {
                            monitorGroupTypeSettings2 = LV_MonitorGroup.getMonitorGroupTypeSettings(monitorGroupDs.getValue(indexOfReleasedMonitorGroup, "monitorgrouptype", ""), this.getConfigurationProcessor());
                            boolean doNotAllowForReleasedMonitorGroup = monitorGroupTypeSettings2.getPropertyListNotNull("allowsampleedit").getProperty("monitorgroupreleased", "N").equals("N");
                            if (doNotAllowForReleasedMonitorGroup) {
                                errorMessage = this.getTranslationProcessor().translate("Cannot add/edit samples of a Released MonitorGroup. Check MonitorGroupPolicy >> Monitor Group Types >> Allow any change to Child Sample >> For Released MonitorGroup (With Approvals) settings for details.");
                            }
                        }
                    }
                }
            }
            if (sampleFlags.contains("study")) {
                errorMessage = this.checkSamplePlanItemReviewDisposition(rsetid);
            }
        }
        return errorMessage;
    }

    private void checkSampleStatus(DataSet primary) {
        DataSet beforeImage = this.getBeforeEditImage().getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String oldSampleStatus = beforeImage.getValue(i, COLUMN_SAMPLESTATUS);
            String newSampleStatus = primary.getValue(i, COLUMN_SAMPLESTATUS);
            String receivedDt = primary.getValue(i, COLUMN_RECEIVEDDT);
            if (receivedDt != null && receivedDt.trim().length() > 0 && oldSampleStatus.equals(SAMPLESTATUS_INPROGRESS) && newSampleStatus.equals("Received")) {
                primary.setValue(i, COLUMN_SAMPLESTATUS, SAMPLESTATUS_INPROGRESS);
            }
            String oldStartTestingDt = beforeImage.getValue(i, COLUMN_STARTTESTINGDT);
            String newStartTestingDt = primary.getValue(i, COLUMN_STARTTESTINGDT);
            if (!(!newSampleStatus.equals(SAMPLESTATUS_COMPLETED) || oldStartTestingDt != null && oldStartTestingDt.length() != 0 || newStartTestingDt != null && newStartTestingDt.length() != 0)) {
                primary.setDate(i, COLUMN_STARTTESTINGDT, "n");
            }
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_STARTTESTINGDT) || oldStartTestingDt == null || oldStartTestingDt.length() <= 0 || newStartTestingDt == null || newStartTestingDt.length() <= 0) continue;
            primary.setValue(i, COLUMN_STARTTESTINGDT, oldStartTestingDt);
        }
    }

    private void setSampleStatus(DataSet primary, HashMap sampleInfoMap) throws ActionException {
        ArrayList list = new ArrayList();
        for (int i = 0; i < primary.size(); ++i) {
            HashMap map;
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_DISPOSALSTATUS) || !"Disposed".equals(primary.getString(i, COLUMN_DISPOSALSTATUS))) continue;
            primary.setString(i, COLUMN_SAMPLESTATUS, "Disposed");
            if (primary.getValue(i, COLUMN_DISPOSALDT, "").length() == 0) {
                primary.setDate(i, COLUMN_DISPOSALDT, "n");
            }
            if (primary.getString(i, COLUMN_DISPOSEDBY, "").length() == 0) {
                primary.setString(i, COLUMN_DISPOSEDBY, this.getSysuserId());
            }
            if (sampleInfoMap == null || ((String)(map = (HashMap)sampleInfoMap.get(primary.getString(i, COLUMN_KEYID1))).get("trackitemid")).length() <= 0) continue;
            list.add(map.get("trackitemid"));
        }
        if (sampleInfoMap != null && list.size() > 0) {
            PropertyList actionprops = new PropertyList();
            actionprops.setProperty("sdcid", "TrackItemSDC");
            actionprops.setProperty("keyid1", OpalUtil.toDelimitedString(list, ";"));
            actionprops.setProperty("currentstorageunitid", "");
            actionprops.setProperty("__sdcruleconfirm", "Y");
            actionprops.setProperty("auditreason", "Sample is Disposed");
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), actionprops);
        }
    }

    private HashMap getSampleInfoMap(DataSet primary) throws SapphireException {
        DataSet ds;
        HashMap sampleMap = new HashMap();
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT s.s_sampleid, p.s_productid, p.s_productversionid, s.disposalstatus, p.disposaloffset, p.disposaloffsetunits, p.disposalbasedonrule, t.trackitemid");
        sql.append(" , s.samplestatus, s.storagestatus, s.previousstoragestatus");
        sql.append(" FROM s_sample s LEFT OUTER JOIN s_product p ON p.s_productid = s.productid AND p.s_productversionid = s.productversionid LEFT OUTER JOIN trackitem t ON t.linkkeyid1 = s.s_sampleid and t.linksdcid = 'Sample'");
        if (primary.size() < 750) {
            sql.append(" WHERE s.s_sampleid in (").append(safeSQL.addIn(primary.getColumnValues(COLUMN_KEYID1, "','"))).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else {
            String rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, primary.getColumnValues(COLUMN_KEYID1, ";"), null, null);
            sql.append(" WHERE s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String sampleid = ds.getString(i, COLUMN_KEYID1, "");
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("trackitemid", ds.getString(i, "trackitemid", ""));
                map.put("productid", ds.getString(i, "s_productid", ""));
                map.put("productversionid", ds.getString(i, "s_productversionid", ""));
                map.put(COLUMN_DISPOSALSTATUS, ds.getString(i, COLUMN_DISPOSALSTATUS, ""));
                map.put("disposaloffset", ds.getValue(i, "disposaloffset", ""));
                map.put("disposaloffsetunits", ds.getString(i, "disposaloffsetunits", ""));
                map.put("disposalbasedonrule", ds.getString(i, "disposalbasedonrule", ""));
                map.put(COLUMN_SAMPLESTATUS, ds.getString(i, COLUMN_SAMPLESTATUS, ""));
                map.put(COLUMN_STORAGESTATUS, ds.getString(i, COLUMN_STORAGESTATUS, ""));
                map.put(COLUMN_PREVSTORAGESTATUS, ds.getString(i, COLUMN_PREVSTORAGESTATUS, ""));
                sampleMap.put(sampleid, map);
            }
        }
        return sampleMap;
    }

    private void setTargetDisposalDT(DataSet primary, HashMap sampleMap) throws SapphireException {
        if (sampleMap == null) {
            for (int i = 0; i < primary.size(); ++i) {
                boolean disposalRuleExist = false;
                String disposalstatus = primary.getString(i, COLUMN_DISPOSALSTATUS, "");
                if ("Retained".equals(disposalstatus)) continue;
                String productid = primary.getString(i, "productid", "");
                String productversionid = primary.getString(i, "productversionid", "");
                if (StringUtil.getLen(productid) > 0L) {
                    HashMap map = this.getProductInfo(productid, productversionid);
                    String disposaloffset = (String)map.get("disposaloffset");
                    String disposaloffsetunit = (String)map.get("disposaloffsetunits");
                    String disposalbasedon = (String)map.get("disposalbasedonrule");
                    if (StringUtil.getLen(disposalbasedon) > 0L && StringUtil.getLen(disposaloffset) > 0L && StringUtil.getLen(disposaloffsetunit) > 0L && primary.getCalendar(i, disposalbasedon) != null && (this.hasPrimaryValueChanged(primary, i, disposalbasedon) || this.hasPrimaryValueChanged(primary, i, "productid") || this.hasPrimaryValueChanged(primary, i, "productversionid"))) {
                        Calendar disposalbasedoncoldt = primary.getCalendar(i, disposalbasedon);
                        BigDecimal bd = new BigDecimal(disposaloffset);
                        Calendar targetdisposaldt = DateTimeUtil.getOffsetDate(disposalbasedoncoldt, disposaloffsetunit, bd);
                        primary.setDate(i, "disposaltargetdt", targetdisposaldt);
                        primary.setString(i, COLUMN_DISPOSALSTATUS, "Marked for Disposal");
                        disposalRuleExist = true;
                    }
                }
                if (disposalRuleExist || !this.hasPrimaryValueChanged(primary, i, "disposaltargetdt")) continue;
                if (primary.getCalendar(i, "disposaltargetdt") != null) {
                    primary.setString(i, COLUMN_DISPOSALSTATUS, "Marked for Disposal");
                    continue;
                }
                primary.setString(i, COLUMN_DISPOSALSTATUS, "");
            }
        } else {
            for (int i = 0; i < primary.size(); ++i) {
                String disposalstatus;
                HashMap map;
                boolean disposalRuleExist = false;
                String sampleid = primary.getValue(i, COLUMN_KEYID1, "");
                if (StringUtil.getLen(sampleid) == 0L || (map = (HashMap)sampleMap.get(sampleid)) == null || "Retained".equals(disposalstatus = (String)map.get(COLUMN_DISPOSALSTATUS))) continue;
                if ((this.hasPrimaryValueChanged(primary, i, "productid") || this.hasPrimaryValueChanged(primary, i, "productversionid")) && StringUtil.getLen(primary.getString(i, "productid")) > 0L) {
                    map = this.getProductInfo(primary.getString(i, "productid", ""), primary.getString(i, "productversionid", ""));
                }
                String disposaloffset = (String)map.get("disposaloffset");
                String disposaloffsetunit = (String)map.get("disposaloffsetunits");
                String disposalbasedon = (String)map.get("disposalbasedonrule");
                if (StringUtil.getLen(disposalbasedon) > 0L && StringUtil.getLen(disposaloffset) > 0L && StringUtil.getLen(disposaloffsetunit) > 0L && primary.getCalendar(i, disposalbasedon) != null && (this.hasPrimaryValueChanged(primary, i, disposalbasedon) || this.hasPrimaryValueChanged(primary, i, "productid") || this.hasPrimaryValueChanged(primary, i, "productversionid"))) {
                    Calendar disposalbasedoncoldt = primary.getCalendar(i, disposalbasedon);
                    BigDecimal bd = new BigDecimal(disposaloffset);
                    Calendar targetdisposaldt = DateTimeUtil.getOffsetDate(disposalbasedoncoldt, disposaloffsetunit, bd);
                    primary.setDate(i, "disposaltargetdt", targetdisposaldt);
                    primary.setString(i, COLUMN_DISPOSALSTATUS, "Marked for Disposal");
                    disposalRuleExist = true;
                }
                if (disposalRuleExist || !this.hasPrimaryValueChanged(primary, i, "disposaltargetdt")) continue;
                if (primary.getCalendar(i, "disposaltargetdt") != null) {
                    primary.setString(i, COLUMN_DISPOSALSTATUS, "Marked for Disposal");
                    continue;
                }
                primary.setString(i, COLUMN_DISPOSALSTATUS, "");
            }
        }
    }

    private HashMap getProductInfo(String productid, String productversionid) {
        if (!this.productInfoMap.containsKey(productid + ":" + productversionid)) {
            HashMap<String, String> map = new HashMap<String, String>();
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select disposaloffset, disposaloffsetunits, disposalbasedonrule from s_product where s_productid = " + safeSQL.addVar(productid) + " AND s_productversionid = " + safeSQL.addVar(productversionid), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                map.put("disposaloffset", ds.getValue(0, "disposaloffset", ""));
                map.put("disposaloffsetunits", ds.getString(0, "disposaloffsetunits", ""));
                map.put("disposalbasedonrule", ds.getString(0, "disposalbasedonrule", ""));
            } else {
                map.put("disposaloffset", "");
                map.put("disposaloffsetunits", "");
                map.put("disposalbasedonrule", "");
            }
            this.productInfoMap.put(productid + ":" + productversionid, map);
        }
        return this.productInfoMap.get(productid + ":" + productversionid);
    }

    @Override
    public boolean requiresDataEntryPrimary() {
        return true;
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__samplePostEditRuleIgnore"))) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: Sample.postEdit() [" + start + "]");
        boolean forceupdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
        DataSet primary = sdiData.getDataset("primary");
        if ("Y".equals(actionProps.getProperty("__studyswitchedit"))) {
            DataSet testds = new DataSet();
            ArrayList<String> docsSampleList = new ArrayList<String>();
            for (int i = 0; i < primary.size(); ++i) {
                String studyid;
                if (!this.hasPrimaryValueChanged(primary, i, "sstudyid") || !OpalUtil.isNotEmpty(studyid = primary.getString(i, "sstudyid", ""))) continue;
                docsSampleList.add(primary.getString(i, COLUMN_KEYID1));
                String sampletypeid = this.getOldPrimaryValue(primary, i, COLUMN_SAMPLETYPEID);
                if (!OpalUtil.isNotEmpty(sampletypeid)) continue;
                int row = testds.addRow();
                testds.setString(row, COLUMN_KEYID1, primary.getString(i, COLUMN_KEYID1));
                testds.setString(row, COLUMN_SAMPLETYPEID, sampletypeid);
                testds.setString(row, "studyid", studyid);
            }
            if (testds.size() > 0) {
                this.addTestToSamplesFromNewStudy(testds);
            }
            if (docsSampleList.size() > 0) {
                this.addBlankDocumentsFromStudy(OpalUtil.toDelimitedString(docsSampleList, ";"), "add");
            }
            Trace.logInfo("END: Sample.postEdit() [" + start + "]. Took " + (System.currentTimeMillis() - start) + "ms. __studyswitchedit is Yes.");
            return;
        }
        DataSet editFamilySampleTypeDS = new DataSet();
        DataSet editTrackItemFreezeThawDS = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            String samplesubtypeid;
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLETYPEID)) continue;
            String sampletypeid = primary.getString(i, COLUMN_SAMPLETYPEID, "").trim();
            if (this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESUBTYPEID) && (samplesubtypeid = primary.getString(i, COLUMN_SAMPLESUBTYPEID, "").trim()).length() <= 0) continue;
        }
        if (this.isBioBankSample(primary, false)) {
            String samplefamilyid;
            this.checkGLPRule(primary, forceupdate);
            if (BaseBioBankRule.isRuleActive("Active RC Rule", this.getConfigurationProcessor())) {
                this.checkActiveRCRule(primary);
            }
            try {
                this.processEventLogRule(primary);
            }
            catch (Exception e) {
                this.logger.error("[Event Logging Error] An error occured while logging events for Sample(s) (" + primary.getColumnValues(COLUMN_KEYID1, ";") + "): " + e.getMessage(), e);
            }
            DataSet ds = new DataSet();
            DataSet specimends = new DataSet();
            DataSet sampletypeds = new DataSet();
            for (int i = 0; i < primary.size(); ++i) {
                int row;
                if (this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLETYPEID)) {
                    String sampletypeid = primary.getString(i, COLUMN_SAMPLETYPEID, "").trim();
                    row = ds.addRow();
                    String sampleid = primary.getString(i, COLUMN_KEYID1);
                    if (StringUtil.getLen(sampleid) > 0L) {
                        ds.setString(row, COLUMN_KEYID1, sampleid);
                        Map<String, String> ft = this.getSampleTypeFreezeThaw(sampletypeid);
                        if (ft != null) {
                            ds.setString(row, "freezethawflag", ft.containsKey("freezethawflag") ? ft.get("freezethawflag") : "");
                            ds.setString(row, "freezethawcountmax", ft.containsKey("freezethawcountmax") ? ft.get("freezethawcountmax") : "");
                            ds.setString(row, "freezethawcountwarn", ft.containsKey("freezethawcountwarn") ? ft.get("freezethawcountwarn") : "");
                        } else {
                            ds.setString(row, "freezethawflag", "");
                            ds.setString(row, "freezethawcountmax", "");
                            ds.setString(row, "freezethawcountwarn", "");
                        }
                        if (this.database.getPreparedCount("select count(sourcesampleid) from s_samplemap where destsampleid = ?", new String[]{sampleid}) == 0) {
                            int familyRow = sampletypeds.addRow();
                            samplefamilyid = this.getOldPrimaryValue(primary, i, "samplefamilyid");
                            if (OpalUtil.isNotEmpty(samplefamilyid)) {
                                sampletypeds.setString(familyRow, "samplefamilyid", samplefamilyid);
                                sampletypeds.setString(familyRow, COLUMN_SAMPLETYPEID, sampletypeid);
                            }
                        }
                    }
                }
                if (!this.hasPrimaryValueChanged(primary, i, "specimentype")) continue;
                String specimentype = primary.getString(i, "specimentype", "");
                row = specimends.addRow();
                specimends.setString(row, COLUMN_KEYID1, primary.getString(i, COLUMN_KEYID1));
                specimends.setString(row, "containertypeid", specimentype);
            }
            if (ds.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDC_SAMPLE);
                props.setProperty("keyid1", ds.getColumnValues(COLUMN_KEYID1, ";"));
                props.setProperty("freezethawflag", ds.getColumnValues("freezethawflag", ";"));
                props.setProperty("freezethawcountmax", ds.getColumnValues("freezethawcountmax", ";"));
                props.setProperty("freezethawcountwarn", ds.getColumnValues("freezethawcountwarn", ";"));
                props.setProperty("auditreason", "Sample Type edited on Sample");
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
            }
            if (specimends.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDC_SAMPLE);
                props.setProperty("keyid1", specimends.getColumnValues(COLUMN_KEYID1, ";"));
                props.setProperty("containertypeid", specimends.getColumnValues("containertypeid", ";"));
                props.setProperty("auditreason", "Specimen Type edited on Sample");
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
            }
            if (sampletypeds.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_SampleFamily");
                props.setProperty("keyid1", sampletypeds.getColumnValues("samplefamilyid", ";"));
                props.setProperty(COLUMN_SAMPLETYPEID, sampletypeds.getColumnValues(COLUMN_SAMPLETYPEID, ";"));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            HashMap<String, String> studySwitchMap = new HashMap<String, String>();
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "sstudyid")) continue;
                studySwitchMap.put(primary.getString(i, COLUMN_KEYID1), primary.getString(i, "sstudyid", ""));
            }
            if (studySwitchMap.size() > 0) {
                DataSet studyTestDS = new DataSet();
                DataSet familyds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), SDC_SAMPLE, "select s_sampleid, samplefamilyid, sampletypeid from s_sample where s_sampleid in ([])", studySwitchMap.keySet());
                if (familyds != null && familyds.size() > 0) {
                    StringBuilder sql = new StringBuilder();
                    LinkedHashMap familyMap = new LinkedHashMap();
                    for (int i = 0; i < familyds.size(); ++i) {
                        String sampletypeid;
                        String sampleid;
                        samplefamilyid = familyds.getString(i, "samplefamilyid", "");
                        if (!familyMap.containsKey(samplefamilyid) && studySwitchMap.containsKey(sampleid = familyds.getString(i, COLUMN_KEYID1))) {
                            familyMap.put(samplefamilyid, studySwitchMap.get(sampleid));
                        }
                        if (!OpalUtil.isNotEmpty(sampletypeid = familyds.getString(i, COLUMN_SAMPLETYPEID, ""))) continue;
                        int row = studyTestDS.addRow();
                        studyTestDS.setString(row, COLUMN_KEYID1, familyds.getString(i, COLUMN_KEYID1));
                        studyTestDS.setString(row, COLUMN_SAMPLETYPEID, sampletypeid);
                        studyTestDS.setString(row, "studyid", (String)studySwitchMap.get(familyds.getString(i, COLUMN_KEYID1)));
                    }
                    if (studyTestDS.size() > 0) {
                        this.addTestToSamplesFromNewStudy(studyTestDS);
                    }
                    this.addBlankDocumentsFromStudy(familyds.getColumnValues(COLUMN_KEYID1, ";"), "add");
                    if (familyMap.size() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_SampleFamily");
                        props.setProperty("keyid1", OpalUtil.toDelimitedString(familyMap.keySet(), ";"));
                        props.setProperty("sstudyid", OpalUtil.toDelimitedString(familyMap.values(), ";"));
                        props.setProperty("__studyswitchedit", "Y");
                        props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                        DataSet sampleds = null;
                        sql.setLength(0);
                        sql.append("select s_sampleid, samplefamilyid, sstudyid");
                        sql.append(" from s_sample");
                        if (familyMap.size() > 1000) {
                            String rsetid = this.getDAMProcessor().createRSet("LV_SampleFamily", OpalUtil.toDelimitedString(familyMap.keySet(), ";"), null, null);
                            sql.append(" where samplefamilyid in (select r.keyid1 from rsetitems r where r.rsetid = ?)");
                            sampleds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                            this.getDAMProcessor().clearRSet(rsetid);
                        } else {
                            SafeSQL safeSQL = new SafeSQL();
                            sql.append(" where samplefamilyid in (").append(safeSQL.addIn(familyMap.keySet())).append(")");
                            sampleds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        }
                        if (sampleds != null && sampleds.size() > 0) {
                            DataSet updateds = new DataSet();
                            for (int i = 0; i < sampleds.size(); ++i) {
                                String samplefamilyid2;
                                String sampleid = sampleds.getString(i, COLUMN_KEYID1);
                                if (studySwitchMap.containsKey(sampleid) || !OpalUtil.isNotEmpty(samplefamilyid2 = sampleds.getString(i, "samplefamilyid", "")) || !familyMap.containsKey(samplefamilyid2)) continue;
                                String oldstudyid = sampleds.getString(i, "sstudyid", "");
                                String newstudyid = (String)familyMap.get(samplefamilyid2);
                                if (!OpalUtil.isNotEmpty(newstudyid) || newstudyid.equals(oldstudyid)) continue;
                                int row = updateds.addRow();
                                updateds.setString(row, COLUMN_KEYID1, sampleid);
                                updateds.setString(row, "sstudyid", newstudyid);
                            }
                            if (updateds.size() > 0) {
                                props.setProperty("sdcid", SDC_SAMPLE);
                                props.setProperty("keyid1", updateds.getColumnValues(COLUMN_KEYID1, ";"));
                                props.setProperty("sstudyid", updateds.getColumnValues("sstudyid", ";"));
                                props.setProperty("__studyswitchedit", "Y");
                                props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                            }
                        }
                    }
                }
            }
        }
        DataSet changedSampleIds = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS)) continue;
            changedSampleIds.copyRow(primary, i, 1);
        }
        if (changedSampleIds.size() > 0) {
            this.updateReagentStatus(changedSampleIds, actionProps);
        }
        this.addSamplesToBatch(sdiData);
        if (!actionProps.getProperty("statusrollup").equals("false")) {
            this.changeBatchStageStatusOnSampleEdit(primary);
            this.changeBatchStatus(sdiData);
        }
        StringBuffer sampleIds = new StringBuffer();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS)) continue;
            sampleIds.append(primary.getValue(i, COLUMN_KEYID1, ";")).append(";");
        }
        if (sampleIds.length() > 0) {
            sampleIds.setLength(sampleIds.length() - 1);
            DataSet clinicalFlagds = BusinessRulesUtil.getClinicalFlagForStudy(this.getDAMProcessor(), this.getQueryProcessor(), OpalUtil.getUniqueValueList(primary.getColumnValues("sstudyid", ";"), ";"));
            if (!clinicalFlagds.getValue(0, "clinicalflag").equalsIgnoreCase("Y")) {
                this.addBlankDocumentsFromStudy(sampleIds.toString(), "edit");
            }
        }
        DataSet initialPrimary = this.getBeforeEditImage().getDataset("primary");
        this.copyCancelStatusToDataSets(primary, initialPrimary, actionProps);
        if (RequestManagementUtil.isPolicyEnabled(this.getConfigurationProcessor()) && !actionProps.getProperty("statusrollup").equals("false")) {
            this.changeRequestStatus(sdiData);
            this.disposeRequestItemDetail(sdiData, actionProps);
        }
        this.calculateSampleDueDate(primary, actionProps, "Edit");
        this.calcSDIWorkItemDueDtBasedonSampleColumn(primary);
        this.matchSDIWorkItemDueDate(primary);
        HashSet<String> completedWISet = new HashSet<String>();
        for (int i = 0; i < primary.size(); ++i) {
            String sdiworkitemcompletionstatus;
            String sourceworkitemid;
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) && !this.hasPrimaryValueChanged(primary, i, COLUMN_STORAGESTATUS) || !OpalUtil.isNotEmpty(sourceworkitemid = this.getOldPrimaryValue(primary, i, "sourcesdiworkitemid")) || !OpalUtil.isNotEmpty(sdiworkitemcompletionstatus = this.getOldPrimaryValue(primary, i, "sdiworkitemcompletionstatus"))) continue;
            if (sdiworkitemcompletionstatus.equals(primary.getString(i, COLUMN_SAMPLESTATUS, "")) || sdiworkitemcompletionstatus.equals(primary.getString(i, COLUMN_STORAGESTATUS, ""))) {
                completedWISet.add(sourceworkitemid);
                continue;
            }
            if (!SAMPLESTATUS_CANCELLED.equals(primary.getString(i, COLUMN_SAMPLESTATUS, "")) && !"Disposed".equals(primary.getString(i, COLUMN_STORAGESTATUS, ""))) continue;
            completedWISet.add(sourceworkitemid);
        }
        if (completedWISet.size() > 0) {
            Object props;
            ArrayList<DataSet> list;
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select s.sourcesdiworkitemid, w.keyid1, w.workitemid, w.workiteminstance, w.workitemstatus, s.s_sampleid,");
            sql.append(" (CASE s.sdiworkitemcompletionstatus when s.samplestatus then 'Completed' when s.storagestatus then 'Completed' else 'Pending' end) status");
            sql.append(" from s_sample s, sdiworkitem w");
            sql.append(" where s.sourcesdiworkitemid = w.sdiworkitemid");
            sql.append(" and w.sdiworkitemid in (").append(safeSQL.addIn(completedWISet)).append(")");
            sql.append(" and s.samplestatus not in ('Disposed', 'Cancelled', 'Archived')");
            sql.append(" and (s.storagestatus is null or s.storagestatus not in ('Disposed', 'Cancelled', 'Archived'))");
            sql.append(" and w.workitemstatus != 'Completed'");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                DataSet inprogressds = new DataSet();
                DataSet completedds = new DataSet();
                ds.sort("sourcesdiworkitemid");
                list = ds.getGroupedDataSets("sourcesdiworkitemid");
                for (DataSet dataset : list) {
                    boolean completed = true;
                    for (int i = 0; i < dataset.size(); ++i) {
                        if (dataset.getString(i, "status").equals(SAMPLESTATUS_COMPLETED)) continue;
                        completed = false;
                    }
                    if (!completed) {
                        if (SAMPLESTATUS_CANCELLED.equals(dataset.getString(0, "workitemstatus", "")) || SAMPLESTATUS_INPROGRESS.equals(dataset.getString(0, "status", ""))) continue;
                        int row = inprogressds.addRow();
                        inprogressds.setString(row, "sdcid", SDC_SAMPLE);
                        inprogressds.setString(row, "keyid1", dataset.getString(0, "keyid1"));
                        inprogressds.setString(row, "workitemid", dataset.getString(0, "workitemid"));
                        inprogressds.setString(row, "workiteminstance", dataset.getValue(0, "workiteminstance"));
                        inprogressds.setString(row, "workitemstatus", SAMPLESTATUS_INPROGRESS);
                        continue;
                    }
                    if (SAMPLESTATUS_CANCELLED.equals(dataset.getString(0, "workitemstatus", ""))) continue;
                    String keyid1 = dataset.getString(0, "keyid1");
                    String workitemid = dataset.getString(0, "workitemid");
                    String workiteminstance = dataset.getValue(0, "workiteminstance");
                    int count = this.getQueryProcessor().getPreparedCount("select count(keyid1) from sdidata where keyid1 = ? and sourceworkitemid = ? and sourceworkiteminstance = ? and s_datasetstatus != 'Completed'", new String[]{keyid1, workitemid, workiteminstance});
                    if (count != 0) continue;
                    int row = completedds.addRow();
                    completedds.setString(row, "sdcid", SDC_SAMPLE);
                    completedds.setString(row, "keyid1", keyid1);
                    completedds.setString(row, "workitemid", workitemid);
                    completedds.setString(row, "workiteminstance", workiteminstance);
                    completedds.setString(row, "workitemstatus", SAMPLESTATUS_COMPLETED);
                }
                props = new PropertyList();
                if (inprogressds.size() > 0) {
                    ((PropertyList)props).setProperty("sdcid", SDC_SAMPLE);
                    ((PropertyList)props).setProperty("keyid1", inprogressds.getColumnValues("keyid1", ";"));
                    ((PropertyList)props).setProperty("workitemid", inprogressds.getColumnValues("workitemid", ";"));
                    ((PropertyList)props).setProperty("workiteminstance", inprogressds.getColumnValues("workiteminstance", ";"));
                    ((PropertyList)props).setProperty("propsmatch", "Y");
                    ((PropertyList)props).setProperty("workitemstatus", inprogressds.getColumnValues("workitemstatus", ";"));
                    this.getActionProcessor().processActionClass(EditSDIWorkItem.class.getName(), (PropertyList)props);
                }
                if (completedds.size() > 0) {
                    ((PropertyList)props).setProperty("sdcid", SDC_SAMPLE);
                    ((PropertyList)props).setProperty("keyid1", completedds.getColumnValues("keyid1", ";"));
                    ((PropertyList)props).setProperty("workitemid", completedds.getColumnValues("workitemid", ";"));
                    ((PropertyList)props).setProperty("workiteminstance", completedds.getColumnValues("workiteminstance", ";"));
                    ((PropertyList)props).setProperty("propsmatch", "Y");
                    ((PropertyList)props).setProperty("workitemstatus", completedds.getColumnValues("workitemstatus", ";"));
                    this.getActionProcessor().processActionClass(EditSDIWorkItem.class.getName(), (PropertyList)props);
                }
            } else {
                safeSQL = new SafeSQL();
                sql = new StringBuilder();
                sql.append(" select s.sourcesdiworkitemid, w.keyid1, w.workitemid, w.workiteminstance, w.workitemstatus, s.s_sampleid, s.samplestatus ");
                sql.append(" from s_sample s, sdiworkitem w ");
                sql.append(" where s.sourcesdiworkitemid = w.sdiworkitemid ");
                sql.append(" and w.sdiworkitemid in (").append(safeSQL.addIn(completedWISet)).append(")");
                sql.append(" and s.samplestatus in ('Disposed', 'Cancelled', 'Archived')");
                DataSet new_ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (new_ds.size() > 0) {
                    DataSet completedds = new DataSet();
                    new_ds.sort("sourcesdiworkitemid");
                    list = new_ds.getGroupedDataSets("sourcesdiworkitemid");
                    for (DataSet dataset : list) {
                        if (SAMPLESTATUS_CANCELLED.equals(dataset.getString(0, "workitemstatus", ""))) continue;
                        int row = completedds.addRow();
                        completedds.setString(row, "sdcid", SDC_SAMPLE);
                        completedds.setString(row, "keyid1", dataset.getString(0, "keyid1"));
                        completedds.setString(row, "workitemid", dataset.getString(0, "workitemid"));
                        completedds.setString(row, "workiteminstance", dataset.getValue(0, "workiteminstance"));
                        completedds.setString(row, "workitemstatus", SAMPLESTATUS_COMPLETED);
                    }
                    props = new PropertyList();
                    if (completedds.size() > 0) {
                        ((PropertyList)props).setProperty("sdcid", SDC_SAMPLE);
                        ((PropertyList)props).setProperty("keyid1", completedds.getColumnValues("keyid1", ";"));
                        ((PropertyList)props).setProperty("workitemid", completedds.getColumnValues("workitemid", ";"));
                        ((PropertyList)props).setProperty("workiteminstance", completedds.getColumnValues("workiteminstance", ";"));
                        ((PropertyList)props).setProperty("propsmatch", "Y");
                        ((PropertyList)props).setProperty("workitemstatus", completedds.getColumnValues("workitemstatus", ";"));
                        this.getActionProcessor().processActionClass(EditSDIWorkItem.class.getName(), (PropertyList)props);
                    }
                }
            }
        }
        if (!actionProps.getProperty("statusrollup").equals("false")) {
            this.changeMonitorGroupStatus(primary);
        }
        this.checkSpecAndSetExcursionFlag(primary);
        DataSet receivedSamples = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) || !"Received".equalsIgnoreCase(primary.getValue(i, COLUMN_SAMPLESTATUS))) continue;
            receivedSamples.copyRow(primary, i, 1);
        }
        if (receivedSamples.getRowCount() > 0) {
            this.createLESWorksheetOnReceive(receivedSamples);
        }
        this.autoStartStopWapActivity(primary);
        DataSet dsAnalyst = new DataSet();
        DataSet dsWorkarea = new DataSet();
        DataSet reviewRequiredSwitchOffDS = new DataSet();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (this.hasPrimaryValueChanged(primary, i, "assignedanalystid")) {
                int r = dsAnalyst.addRow();
                dsAnalyst.setString(r, "sampleid", primary.getValue(i, COLUMN_KEYID1, ""));
                dsAnalyst.setString(r, "assignedanalystid", primary.getValue(i, "assignedanalystid", ""));
            }
            if (this.hasPrimaryValueChanged(primary, i, "assigneddepartmentid")) {
                int r = dsWorkarea.addRow();
                dsWorkarea.setString(r, "sampleid", primary.getValue(i, COLUMN_KEYID1, ""));
                dsWorkarea.setString(r, "assigneddepartmentid", primary.getValue(i, "assigneddepartmentid", ""));
            }
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_REVIEWREQUIRED) || !primary.getString(i, COLUMN_REVIEWREQUIRED, "N").equalsIgnoreCase("N")) continue;
            String sampleid = primary.getString(i, COLUMN_KEYID1);
            this.database.createPreparedResultSet("SELECT approvaltypeid FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? ", new Object[]{SDC_SAMPLE, sampleid});
            while (this.database.getNext()) {
                int row = reviewRequiredSwitchOffDS.addRow();
                reviewRequiredSwitchOffDS.setString(row, COLUMN_KEYID1, sampleid);
                reviewRequiredSwitchOffDS.setString(row, "approvaltypeid", this.database.getValue("approvaltypeid"));
            }
        }
        if (!reviewRequiredSwitchOffDS.isEmpty()) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_SAMPLE);
            props.setProperty("keyid1", reviewRequiredSwitchOffDS.getColumnValues(COLUMN_KEYID1, ";"));
            props.setProperty("approvaltypeid", reviewRequiredSwitchOffDS.getColumnValues("approvaltypeid", ";"));
            this.getActionProcessor().processAction("DeleteSDIApproval", "1", props);
        }
        if (dsAnalyst.getRowCount() > 0) {
            this.copyAnalystToDataSetsAndSDIWorkItems(dsAnalyst);
        }
        if (dsWorkarea.getRowCount() > 0) {
            this.copyWorkAreaToDataSetsAndSDIWorkItems(dsWorkarea);
        }
        ApprovalRuleUtil.checkSDIAutoApprovalRule(primary, actionProps, SDC_SAMPLE, COLUMN_SAMPLESTATUS, SAMPLESTATUS_COMPLETED, SAMPLESTATUS_REVIEWED, COLUMN_REVIEWEDBY, COLUMN_REVIEWEDDT, this.database, this.connectionInfo, this.logger, this.getSDCProcessor(), true);
        if (this.isCMTImport()) {
            this.handleCMT(sdiData);
        }
        Trace.logInfo("END: Sample.postEdit() [" + start + "]. Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private void handleCMT(SDIData sdiData) throws ActionException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!"Y".equalsIgnoreCase(primary.getString(i, COLUMN_TEMPLATEFLAG, ""))) continue;
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_SAMPLE);
            props.setProperty("keyid1", primary.getString(i, COLUMN_KEYID1));
            this.getActionProcessor().processAction("SyncSDIDataCrossSDICalc", "1", props);
        }
    }

    private void autoStartStopWapActivity(DataSet primary) throws ActionException {
        int i;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer startActivities = new StringBuffer();
        ArrayList<String> listStartActivities = new ArrayList<String>();
        StringBuffer updateCompleteCountActivities = new StringBuffer();
        DataSet deleteActivityWorkSDIs = new DataSet();
        ArrayList<String> listCompleteActivities = new ArrayList<String>();
        for (i = 0; i < primary.size(); ++i) {
            boolean wapStatusPendingOrAssigned;
            String newStartTestingDt = primary.getValue(i, COLUMN_STARTTESTINGDT);
            boolean started = this.hasPrimaryValueChanged(primary, i, COLUMN_STARTTESTINGDT) && newStartTestingDt.length() > 0;
            String newStatus = primary.getValue(i, COLUMN_SAMPLESTATUS);
            String wapStatus = primary.getValue(i, "wapstatus");
            String oldwapStatus = this.getOldPrimaryValue(primary, i, "wapstatus");
            boolean cancelled = SAMPLESTATUS_CANCELLED.equalsIgnoreCase(newStatus) && this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS);
            boolean uncancelled = this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) && SAMPLESTATUS_CANCELLED.equalsIgnoreCase(this.getOldPrimaryValue(primary, i, COLUMN_SAMPLESTATUS));
            boolean completedorcancelled = this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) && (SAMPLESTATUS_CANCELLED.equalsIgnoreCase(newStatus) || SAMPLESTATUS_COMPLETED.equalsIgnoreCase(newStatus));
            boolean bl = wapStatusPendingOrAssigned = !SAMPLESTATUS_CANCELLED.equalsIgnoreCase(wapStatus) && ("Pending".equalsIgnoreCase(oldwapStatus) || "Assigned".equalsIgnoreCase(oldwapStatus));
            if (!wapStatusPendingOrAssigned || !started && !completedorcancelled && !uncancelled) continue;
            String sampleId = primary.getValue(i, COLUMN_KEYID1);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct a.activityid, a.activitystatus, s.s_sampleid from activity a, activityworksdi w, s_sample s  where s.s_sampleid = " + safeSQL.addVar(sampleId) + " and  w.worksdcid = 'Sample' and w.workkeyid1 = s.s_sampleid and a.activityid = w.activityid ", safeSQL.getValues());
            if (ds.getRowCount() > 0) {
                String activityid = ds.getValue(0, "activityid");
                String activityStatus = ds.getValue(0, "activitystatus");
                if (started && "Activated".equalsIgnoreCase(activityStatus) && !listStartActivities.contains(activityid)) {
                    listStartActivities.add(activityid);
                }
                if ("Draft".equalsIgnoreCase(activityStatus) && cancelled) {
                    deleteActivityWorkSDIs.copyRow(ds, -1, 1);
                } else if ((completedorcancelled || uncancelled) && !listCompleteActivities.contains(activityid)) {
                    listCompleteActivities.add(activityid);
                    updateCompleteCountActivities.append(";").append(activityid);
                }
            }
            safeSQL.reset();
        }
        for (i = 0; i < listStartActivities.size(); ++i) {
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
                props.setProperty("worksdcid", SDC_SAMPLE);
                props.setProperty("workkeyid1", activityWorkSDIs.getColumnValues(COLUMN_KEYID1, ";"));
                props.setProperty("setcancelled", "Y");
                this.getActionProcessor().processActionClass(RemoveActivityWorkSDI.class.getName(), props);
            }
        }
    }

    private void createLESWorksheetOnReceive(DataSet receivedSamples) throws SapphireException {
        String[] fKeyLinkIds = new String[]{"Sample Sub Type", "SampleTypeId"};
        String[] fKeyColumnIds = new String[2];
        String[] fKeyTables = new String[2];
        String[] fKeySDCS = new String[2];
        QueryProcessor qp = this.getQueryProcessor();
        receivedSamples.addColumn("tocreatews", 0);
        SDCProcessor sdcp = this.getSDCProcessor();
        TranslationProcessor tp = this.getTranslationProcessor();
        for (int lnk = 0; lnk < fKeyLinkIds.length; ++lnk) {
            DataSet templatelink = qp.getPreparedSqlDataSet("SELECT * FROM sdclink WHERE sdcid = ? AND linkid = ?", (Object[])new String[]{SDC_SAMPLE, fKeyLinkIds[lnk]});
            if (templatelink.getRowCount() != 1) continue;
            String linksdcid = templatelink.getValue(0, "linksdcid");
            String sdccolumnid = templatelink.getValue(0, "sdccolumnid");
            String sdccolumnid2 = templatelink.getValue(0, "sdccolumnid2");
            String sdccolumnid3 = templatelink.getValue(0, "sdccolumnid3");
            fKeySDCS[lnk] = linksdcid;
            fKeyTables[lnk] = sdcp.getProperty(linksdcid, "tableid");
            for (int i = 0; i < receivedSamples.getRowCount(); ++i) {
                String fkeyValue;
                String sampleId = receivedSamples.getValue(i, COLUMN_KEYID1);
                String string = fkeyValue = receivedSamples.getValue(i, sdccolumnid).length() > 0 ? receivedSamples.getValue(i, sdccolumnid) : this.getOldPrimaryValue(receivedSamples, i, sdccolumnid);
                if (fkeyValue.length() <= 0 || qp.getPreparedCount("select count(1) from worksheetsdi where sdcid = ? and keyid1 = ?", new String[]{SDC_SAMPLE, sampleId}) != 0) continue;
                receivedSamples.setString(i, sdccolumnid, fkeyValue);
                fKeyColumnIds[lnk] = sdccolumnid;
                if (sdccolumnid2.length() > 0) {
                    int n = lnk;
                    fKeyColumnIds[n] = fKeyColumnIds[n] + "," + sdccolumnid2;
                    receivedSamples.setString(i, sdccolumnid2, receivedSamples.getValue(i, sdccolumnid2).length() > 0 ? receivedSamples.getValue(i, sdccolumnid2) : this.getOldPrimaryValue(receivedSamples, i, sdccolumnid2));
                }
                if (sdccolumnid3.length() > 0) {
                    int n = lnk;
                    fKeyColumnIds[n] = fKeyColumnIds[n] + "," + sdccolumnid3;
                    receivedSamples.setString(i, sdccolumnid3, receivedSamples.getValue(i, sdccolumnid3).length() > 0 ? receivedSamples.getValue(i, sdccolumnid3) : this.getOldPrimaryValue(receivedSamples, i, sdccolumnid3));
                }
                receivedSamples.setString(i, "tocreatews", "Y");
            }
        }
        HashMap<String, String> createwsMap = new HashMap<String, String>();
        createwsMap.put("tocreatews", "Y");
        DataSet createwsSamples = receivedSamples.getFilteredDataSet(createwsMap);
        if (createwsSamples.getRowCount() > 0) {
            try {
                for (int i = 0; i < fKeyColumnIds.length; ++i) {
                    if (fKeyColumnIds[i] != null && fKeyColumnIds[i].length() > 0) {
                        createwsSamples.sort(fKeyColumnIds[i]);
                        ArrayList<DataSet> groups = createwsSamples.getGroupedDataSets(fKeyColumnIds[i]);
                        String[] fkeys = StringUtil.split(fKeyColumnIds[i], ",");
                        StringBuffer sql = new StringBuffer();
                        sql.append("SELECT t.createlesrule, sdiworksheetrule.authorflag, sdiworksheetrule.worksheetid, sdiworksheetrule.worksheetversionid, sdiworksheetrule.workbookid, sdiworksheetrule.workbookversionid, ").append(" sdiworksheetrule.worksheetrule FROM " + fKeyTables[i] + " t, sdiworksheetrule ").append(" WHERE sdiworksheetrule.sdcid = ? AND sdiworksheetrule.keyid1 = t.").append(sdcp.getProperty(fKeySDCS[i], "keycolid1"));
                        if (fkeys.length > 1) {
                            sql.append(" AND sdiworksheetrule.keyid2 = t.").append(sdcp.getProperty(fKeySDCS[i], "keycolid2"));
                        }
                        if (fkeys.length > 2) {
                            sql.append(" AND sdiworksheetrule.keyid3 = t.").append(sdcp.getProperty(fKeySDCS[i], "keycolid3"));
                        }
                        sql.append(" AND  t.").append(sdcp.getProperty(fKeySDCS[i], "keycolid1")).append(" = ? ");
                        if (fkeys.length > 1) {
                            sql.append(" AND  t.").append(sdcp.getProperty(fKeySDCS[i], "keycolid2")).append(" = ? ");
                        }
                        if (fkeys.length > 2) {
                            sql.append(" AND  t.").append(sdcp.getProperty(fKeySDCS[i], "keycolid3")).append(" = ? ");
                        }
                        sql.append(" AND t.createlesrule = ? AND sdiworksheetrule.worksheetrule = ?");
                        PreparedStatement getWSTemplate = this.database.prepareStatement("getwstemplate", sql.toString());
                        for (int grp = 0; grp < groups.size(); ++grp) {
                            DataSet dsGrp = groups.get(grp);
                            String fkeyValue = dsGrp.getValue(0, fkeys[0]);
                            if (fkeyValue.length() <= 0) continue;
                            getWSTemplate.setString(1, fKeySDCS[i]);
                            getWSTemplate.setString(2, fkeyValue);
                            int cnt = 3;
                            if (fkeys.length > 1) {
                                getWSTemplate.setString(cnt++, dsGrp.getValue(0, fkeys[1]));
                            }
                            if (fkeys.length > 2) {
                                getWSTemplate.setString(cnt++, dsGrp.getValue(0, fkeys[2]));
                            }
                            getWSTemplate.setString(cnt++, "On Receive");
                            getWSTemplate.setString(cnt, "default");
                            DataSet dsTemplate = new DataSet(getWSTemplate.executeQuery());
                            if (dsTemplate.getRowCount() == 1) {
                                PropertyList createWSProps = new PropertyList();
                                createWSProps.setProperty("sampleid", dsGrp.getColumnValues(COLUMN_KEYID1, ";"));
                                createWSProps.setProperty("templateid", dsTemplate.getString(0, "worksheetid"));
                                createWSProps.setProperty("templateversionid", dsTemplate.getString(0, "worksheetversionid"));
                                createWSProps.setProperty("workbookid", dsTemplate.getString(0, "workbookid"));
                                createWSProps.setProperty("workbookversionid", dsTemplate.getString(0, "workbookversionid"));
                                this.getActionProcessor().processActionClass(GenerateSampleWorksheet.class.getName(), createWSProps);
                                dsGrp.setValue(-1, "tocreatews", "N");
                                continue;
                            }
                            if (dsTemplate.getRowCount() <= 1) continue;
                            String msg = "LES worksheet could not be created. Multiple LES worksheet template found for " + fkeyValue;
                            msg = msg + (fkeys.length > 1 ? "," + dsGrp.getValue(0, fkeys[1]) : "");
                            msg = msg + (fkeys.length > 2 ? "," + dsGrp.getValue(0, fkeys[2]) : "");
                            this.logger.info(msg);
                        }
                    }
                    if ((createwsSamples = createwsSamples.getFilteredDataSet(createwsMap)).getRowCount() != 0) continue;
                    break;
                }
            }
            catch (Exception e) {
                throw new SapphireException(tp.translate("Failed to create LES worksheet on receiving of samples: ") + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            finally {
                this.database.closeStatement("getwstemplate");
            }
        }
    }

    private void checkSpecAndSetExcursionFlag(DataSet primary) throws SapphireException {
        if (primary.isValidColumn(COLUMN_SAMPLESTATUS)) {
            try {
                String sqlSpec = "SELECT condition FROM sdispec WHERE sdcid='Sample' AND  keyid1=?";
                PreparedStatement specsStatement = this.database.prepareStatement("specs", sqlSpec);
                String sqlMonitorSample = "SELECT samplepointid,locationid FROM s_sample WHERE s_sampleid=?";
                PreparedStatement monitorSampleStatement = this.database.prepareStatement("monitorsample", sqlMonitorSample);
                StringBuffer locations = new StringBuffer();
                StringBuffer samplepoints = new StringBuffer();
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    if (!CLASSIFICATION_MONITOR.equals(primary.getValue(i, COLUMN_CLASSIFICATION, this.getOldPrimaryValue(primary, i, COLUMN_CLASSIFICATION)))) continue;
                    String currentSampleStatus = primary.getValue(i, COLUMN_SAMPLESTATUS, "");
                    String sampleid = primary.getValue(i, COLUMN_KEYID1, "");
                    if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) || !currentSampleStatus.equalsIgnoreCase(SAMPLESTATUS_COMPLETED)) continue;
                    specsStatement.setString(1, sampleid);
                    DataSet specsDS = new DataSet(specsStatement.executeQuery());
                    if (specsDS.getRowCount() <= 0) continue;
                    HashMap<String, String> filter = new HashMap<String, String>();
                    PropertyListCollection specInterpretation = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getCollection("SpecConditions");
                    Iterator iter = specInterpretation.iterator();
                    String failCondition = "Fail";
                    while (iter.hasNext()) {
                        PropertyList condition = (PropertyList)iter.next();
                        if (!condition.getProperty("interpretation").equals(failCondition)) continue;
                        failCondition = condition.getProperty("SpecCond");
                        break;
                    }
                    filter.put("condition", failCondition);
                    DataSet failedSpecs = specsDS.getFilteredDataSet(filter);
                    if (failedSpecs.getRowCount() <= 0) continue;
                    monitorSampleStatement.setString(1, sampleid);
                    DataSet monitorSampleDS = new DataSet(monitorSampleStatement.executeQuery());
                    if (monitorSampleDS.getRowCount() <= 0) continue;
                    String samplepointid = monitorSampleDS.getString(0, "samplepointid", "");
                    String locationid = monitorSampleDS.getString(0, "locationid", "");
                    if (samplepointid.length() > 0) {
                        samplepoints.append(";").append(samplepointid);
                    }
                    if (locationid.length() <= 0) continue;
                    locations.append(";").append(locationid);
                }
                ActionProcessor ap = this.getActionProcessor();
                PropertyList props = new PropertyList();
                if (samplepoints.length() > 0) {
                    props.clear();
                    props.setProperty("sdcid", "SamplePoint");
                    props.setProperty("keyid1", samplepoints.substring(1));
                    props.setProperty("excursionflag", "Y");
                    ap.processAction("EditSDI", "1", props);
                }
                if (locations.length() > 0) {
                    props.clear();
                    props.setProperty("sdcid", "Location");
                    props.setProperty("keyid1", locations.substring(1));
                    props.setProperty("excursionflag", "Y");
                    ap.processAction("EditSDI", "1", props);
                }
            }
            catch (SQLException e) {
                this.logger.error("Failed to retrieve sdispec data(Sample.setExcursionFlag()):", e);
            }
        }
    }

    private void addTestToSamplesFromNewStudy(DataSet ds) throws SapphireException {
        if (ds != null && ds.size() > 0) {
            DataSet wids = new DataSet();
            for (int i = 0; i < ds.size(); ++i) {
                DataSet testds;
                String studyid = ds.getString(i, "studyid", "");
                String sampleid = ds.getString(i, COLUMN_KEYID1, "");
                String sampletypeid = ds.getString(i, COLUMN_SAMPLETYPEID, "");
                if (!OpalUtil.isNotEmpty(studyid) || !OpalUtil.isNotEmpty(sampleid) || !OpalUtil.isNotEmpty(sampletypeid) || (testds = this.getStudySampleTypeTests(studyid, sampletypeid)) == null) continue;
                for (int j = 0; j < testds.size(); ++j) {
                    String workitemid = testds.getString(j, "workitemid");
                    String workitemversionid = testds.getString(j, "workitemversionid");
                    if (!OpalUtil.isNotEmpty(sampleid) || !OpalUtil.isNotEmpty(workitemid) || !OpalUtil.isNotEmpty(workitemversionid)) continue;
                    int row = wids.addRow();
                    wids.setString(row, "keyid1", sampleid);
                    wids.setString(row, "workitemid", workitemid);
                    wids.setString(row, "workitemversionid", workitemversionid);
                    wids.setString(row, "sstudyid", studyid);
                }
            }
            if (wids.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDC_SAMPLE);
                props.setProperty("keyid1", wids.getColumnValues("keyid1", ";"));
                props.setProperty("workitemid", wids.getColumnValues("workitemid", ";"));
                props.setProperty("workitemversionid", wids.getColumnValues("workitemversionid", ";"));
                props.setProperty("sourcesstudyid", wids.getColumnValues("sstudyid", ";"));
                props.setProperty("forcenew", GetForceNewPolicy.analyzeForceNewPolicy("Y", this.getConfigurationProcessor()));
                props.setProperty("propsmatch", "Y");
                this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
            }
        }
    }

    private DataSet getStudySampleTypeTests(String studyid, String sampletypeid) {
        String key;
        if (this.studyTestCache == null) {
            this.studyTestCache = new HashMap<String, DataSet>();
        }
        if (!this.studyTestCache.containsKey(key = studyid + sampletypeid)) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct workitemid, workitemversionid from sdiworkitem where sdcid = 'Study' and keyid1 = '" + studyid + "' and ( s_sampletypeid = " + safeSQL.addVar(sampletypeid) + " or s_sampletypeid is null)", safeSQL.getValues());
            this.studyTestCache.put(key, ds);
        }
        return this.studyTestCache.get(key);
    }

    private void copyCancelStatusToDataSets(DataSet primary, DataSet initialPrimary, PropertyList actionProps) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        String sampleCancelBehavior = policy.getProperty("cancelactionbehavior", "");
        String auditReason = actionProps.getProperty("auditreason");
        String auditActivity = actionProps.getProperty("auditactivity", "");
        String auditSignedFlag = actionProps.getProperty("auditsignedflag", "N");
        if (sampleCancelBehavior.equalsIgnoreCase("Cancel all Children") || sampleCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
            DataSet wids;
            StringBuffer sdiwisb;
            DataSet ds;
            SafeSQL safeSQL;
            PropertyList props;
            String sampleid;
            int i;
            if (primary.getColumnValues(COLUMN_SAMPLESTATUS, ";").contains(SAMPLESTATUS_CANCELLED)) {
                String cancelledSamples = "";
                for (i = 0; i < primary.getRowCount(); ++i) {
                    String sampleStatus = primary.getValue(i, COLUMN_SAMPLESTATUS, "");
                    sampleid = primary.getValue(i, COLUMN_KEYID1, "");
                    if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) || !sampleStatus.equals(SAMPLESTATUS_CANCELLED)) continue;
                    cancelledSamples = cancelledSamples + ";" + sampleid;
                }
                if (cancelledSamples.length() > 0) {
                    String rset_id = BaseSDIDataAction.createBypassSecurityRSet(SDC_SAMPLE, cancelledSamples.substring(1), null, null, this.database, this.connectionInfo, false);
                    props = new PropertyList();
                    safeSQL = new SafeSQL();
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sd.sdcid, sd.keyid1, sd.keyid2, sd.keyid3, sd.paramlistid, sd.paramlistversionid, sd.variantid, sd.dataset  FROM sdidata sd, rsetitems r   WHERE sd.sdcid = 'Sample'   AND sd.keyid1 = r.keyid1   AND r.rsetid = " + safeSQL.addVar(rset_id) + "  AND (sd.sourceworkitemid is null OR sd.sourceworkitemid  = '') ", safeSQL.getValues());
                    if (ds.size() > 0) {
                        props.setProperty("sdcid", SDC_SAMPLE);
                        props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
                        props.setProperty("s_datasetstatus", SAMPLESTATUS_CANCELLED);
                        props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
                        props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
                        props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
                        props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
                        props.setProperty(COLUMN_CANCELLEDDT, "n");
                        props.setProperty(COLUMN_CANCELLEDBY, this.getSysuserId());
                        if (sampleCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                            props.setProperty("editincompleteonly", "Y");
                        }
                        props.setProperty("propsmatch", "Y");
                        props.setProperty("auditreason", auditReason);
                        props.setProperty("auditactivity", auditActivity);
                        props.setProperty("auditsignedflag", auditSignedFlag);
                        this.getActionProcessor().processAction("EditDataSet", "1", props);
                    }
                    sdiwisb = new StringBuffer();
                    safeSQL.reset();
                    sdiwisb.append("SELECT sw.sdcid, sw.keyid1, sw.keyid2, sw.keyid3, sw.workitemid, sw.workiteminstance ").append(" FROM sdiworkitem sw, rsetitems r ").append(" WHERE sw.sdcid = 'Sample' ").append(" AND sw.keyid1 = r.keyid1 ").append(" AND r.rsetid = " + safeSQL.addVar(rset_id));
                    if (sampleCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                        sdiwisb.append(" AND sw.workitemstatus IN ('Initial', 'InProgress', 'DataEntered', 'Released')");
                    }
                    if ((wids = this.getQueryProcessor().getPreparedSqlDataSet(sdiwisb.toString(), safeSQL.getValues())).size() > 0) {
                        props = new PropertyList();
                        props.setProperty("sdcid", SDC_SAMPLE);
                        props.setProperty("keyid1", wids.getColumnValues("keyid1", ";"));
                        props.setProperty("workitemid", wids.getColumnValues("workitemid", ";"));
                        props.setProperty("workiteminstance", wids.getColumnValues("workiteminstance", ";"));
                        props.setProperty("propsmatch", "Y");
                        props.setProperty("syncsdistatus", "N");
                        props.setProperty("syncsdiworkitemgroupstatus", "N");
                        props.setProperty("auditreason", auditReason);
                        props.setProperty("auditactivity", auditActivity);
                        props.setProperty("auditsignedflag", auditSignedFlag);
                        this.getActionProcessor().processAction("CancelSDIWorkItem", "1", props);
                    }
                    props.clear();
                    props.setProperty("sdcid", SDC_SAMPLE);
                    props.setProperty("keyid1", cancelledSamples.substring(1));
                    props.setProperty("auditreason", auditReason);
                    props.setProperty("auditactivity", auditActivity);
                    props.setProperty("auditsignedflag", auditSignedFlag);
                    this.getActionProcessor().processAction("UpdateQCBatchStatus", "1", props);
                    this.getDAMProcessor().clearRSet(rset_id);
                }
            }
            String uncancelledSamples = "";
            for (i = 0; i < primary.getRowCount(); ++i) {
                String initialSampleStatus = initialPrimary.getValue(i, COLUMN_SAMPLESTATUS, "");
                sampleid = primary.getValue(i, COLUMN_KEYID1, "");
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) || !initialSampleStatus.equals(SAMPLESTATUS_CANCELLED)) continue;
                uncancelledSamples = uncancelledSamples + ";" + sampleid;
            }
            if (uncancelledSamples.length() > 0) {
                String rset_id = BaseSDIDataAction.createBypassSecurityRSet(SDC_SAMPLE, uncancelledSamples.substring(1), null, null, this.database, this.connectionInfo, false);
                props = new PropertyList();
                safeSQL = new SafeSQL();
                safeSQL.reset();
                ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sd.sdcid, sd.keyid1, sd.keyid2, sd.keyid3, sd.paramlistid, sd.paramlistversionid, sd.variantid, sd.dataset FROM sdidata sd, rsetitems r  WHERE sd.sdcid = 'Sample'  AND sd.keyid1 = r.keyid1  AND r.rsetid = " + safeSQL.addVar(rset_id) + " AND sd.s_datasetstatus = 'Cancelled' AND (sd.sourceworkitemid is null OR sd.sourceworkitemid  = '') ", safeSQL.getValues());
                if (ds.size() > 0) {
                    props.setProperty("sdcid", ds.getValue(0, "sdcid", ""));
                    props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
                    props.setProperty("keyid2", ds.getColumnValues("keyid2", ";"));
                    props.setProperty("keyid3", ds.getColumnValues("keyid3", ";"));
                    props.setProperty("statuscolumn", "s_datasetstatus");
                    props.setProperty("validatestatuscolumnvalue", SAMPLESTATUS_CANCELLED);
                    props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
                    props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
                    props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
                    props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
                    props.setProperty("propsmatch", "Y");
                    this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                }
                sdiwisb = new StringBuffer();
                safeSQL.reset();
                sdiwisb.append("SELECT sw.sdcid, sw.keyid1, sw.keyid2, sw.keyid3, sw.workitemid, sw.workiteminstance ").append(" FROM sdiworkitem sw, rsetitems r ").append(" WHERE sw.sdcid = 'Sample' ").append(" AND sw.keyid1 = r.keyid1 ").append(" AND r.rsetid = " + safeSQL.addVar(rset_id)).append(" AND sw.workitemstatus = 'Cancelled'");
                wids = this.getQueryProcessor().getPreparedSqlDataSet(sdiwisb.toString(), safeSQL.getValues());
                if (wids.size() > 0) {
                    props.clear();
                    props.setProperty("sdcid", wids.getValue(0, "sdcid", ""));
                    props.setProperty("keyid1", wids.getColumnValues("keyid1", ";"));
                    props.setProperty("keyid2", wids.getColumnValues("keyid2", ";"));
                    props.setProperty("keyid3", wids.getColumnValues("keyid3", ";"));
                    props.setProperty("statuscolumn", "workitemstatus");
                    props.setProperty("validatestatuscolumnvalue", SAMPLESTATUS_CANCELLED);
                    props.setProperty("workitemid", wids.getColumnValues("workitemid", ";"));
                    props.setProperty("workiteminstance", wids.getColumnValues("workiteminstance", ";"));
                    props.setProperty("propsmatch", "Y");
                    this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                }
                props.clear();
                props.setProperty("sdcid", SDC_SAMPLE);
                props.setProperty("keyid1", uncancelledSamples.substring(1));
                props.setProperty("auditreason", auditReason);
                props.setProperty("auditactivity", auditActivity);
                props.setProperty("auditsignedflag", auditSignedFlag);
                this.getActionProcessor().processAction("UpdateQCBatchStatus", "1", props);
                this.getDAMProcessor().clearRSet(rset_id);
            }
        }
    }

    private void changeBatchStageStatusOnSampleAdd(DataSet primary) throws SapphireException {
        StringBuffer batchStageIdCollection = new StringBuffer();
        HashSet<String> stageSet = new HashSet<String>();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String batchStageId = primary.getValue(i, "batchstageid", "");
            if (batchStageId.length() <= 0 || stageSet.contains(batchStageId)) continue;
            batchStageIdCollection.append(";");
            batchStageIdCollection.append(batchStageId);
            stageSet.add(batchStageId);
        }
        if (batchStageIdCollection.length() > 0) {
            HashMap<String, String> props;
            String rsetid = this.getDAMProcessor().createRSet("LV_BatchStage", batchStageIdCollection.substring(1), "", "");
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer str = new StringBuffer("SELECT s_batchstageid, batchstagestatus FROM s_batchstage bs, rsetitems r");
            str.append(" WHERE r.rsetid=").append(safeSQL.addVar(rsetid));
            str.append(" AND r.sdcid='LV_BatchStage'");
            str.append(" AND r.keyid1 = bs.s_batchstageid");
            DataSet batchstages = this.getQueryProcessor().getPreparedSqlDataSet(str.toString(), safeSQL.getValues());
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_RELEASED);
            DataSet releasedBatchStages = batchstages.getFilteredDataSet(filter);
            filter.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_REJECTED);
            DataSet rejectedBatchStages = batchstages.getFilteredDataSet(filter);
            if (releasedBatchStages.getRowCount() > 0 || rejectedBatchStages.getRowCount() > 0) {
                this.getDAMProcessor().clearRSet(rsetid);
                String stagesToUnDisposition = "";
                if (releasedBatchStages.getRowCount() > 0) {
                    stagesToUnDisposition = stagesToUnDisposition + releasedBatchStages.getColumnValues("s_batchstageid", ";");
                }
                if (rejectedBatchStages.getRowCount() > 0) {
                    if (stagesToUnDisposition.length() > 0) {
                        stagesToUnDisposition = stagesToUnDisposition + ";";
                    }
                    stagesToUnDisposition = stagesToUnDisposition + rejectedBatchStages.getColumnValues("s_batchstageid", ";");
                }
                props = new HashMap<String, String>();
                props.put("sdcid", "LV_BatchStage");
                props.put("keyid1", stagesToUnDisposition);
                props.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_INPROGRESS);
                this.getActionProcessor().processAction("EditSDI", "1", props);
                props = new HashMap();
                props.put("sdcid", "LV_BatchStage");
                props.put("keyid1", stagesToUnDisposition);
                props.put("ready", "N");
                this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
            }
            filter.clear();
            filter.put("batchstagestatus", null);
            DataSet statuslessStages = batchstages.getFilteredDataSet(filter);
            if (statuslessStages.getRowCount() > 0) {
                props = new HashMap();
                props.put("sdcid", "LV_BatchStage");
                props.put("keyid1", statuslessStages.getColumnValues("s_batchstageid", ";"));
                props.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_INITIAL);
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
            filter.clear();
            filter.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_PENDINGRELEASE);
            DataSet pendingReleaseBatchStages = batchstages.getFilteredDataSet(filter);
            if (pendingReleaseBatchStages.getRowCount() > 0) {
                HashMap<String, String> props2 = new HashMap<String, String>();
                props2.put("sdcid", "LV_BatchStage");
                props2.put("keyid1", pendingReleaseBatchStages.getColumnValues("s_batchstageid", ";"));
                props2.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_INPROGRESS);
                this.getActionProcessor().processAction("EditSDI", "1", props2);
            }
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    private void changeBatchStageStatusOnSampleEdit(DataSet primary) throws SapphireException {
        StringBuffer str;
        String rsetid;
        StringBuffer inProgressBatchStages = new StringBuffer();
        StringBuffer pendingReleaseBatchStages = new StringBuffer();
        HashSet<String> inProgressStageSet = new HashSet<String>();
        HashSet<String> pendingReleaseStageSet = new HashSet<String>();
        DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
        String sampleIds = primary.getColumnValues(COLUMN_KEYID1, ";");
        String sampleRsetId = this.getRSet(SDC_SAMPLE, sampleIds, null, null, this.getDAMProcessor());
        String batchStageSql = "SELECT distinct s.batchstageid, bs.batchstagestatus, s.s_sampleid  FROM s_batchstage bs, s_sample s, rsetitems r  WHERE r.sdcid='Sample' AND r.keyid1 = s.s_sampleid AND s.batchstageid=bs.s_batchstageid AND r.rsetid= ?";
        DataSet sampleBatchStageStatuses = this.getQueryProcessor().getPreparedSqlDataSet(batchStageSql, (Object[])new String[]{sampleRsetId});
        if (OpalUtil.isNotEmpty(sampleRsetId)) {
            this.getDAMProcessor().clearRSet(sampleRsetId);
        }
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String batchStageId = primary.getValue(i, "batchstageid", "");
            if (batchStageId.length() == 0) {
                batchStageId = beforeEditImagePrimary.getValue(i, "batchstageid", "");
            }
            HashMap<String, String> find = new HashMap<String, String>();
            find.put(COLUMN_KEYID1, primary.getValue(i, COLUMN_KEYID1, ""));
            find.put("batchstageid", batchStageId);
            int row = sampleBatchStageStatuses.findRow(find);
            String batchstageStatus = "";
            if (row >= 0) {
                batchstageStatus = sampleBatchStageStatuses.getValue(row, "batchstagestatus", "");
            }
            boolean isSampleStatusChanged = this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS);
            boolean isDisposalStatusChanged = this.hasPrimaryValueChanged(primary, i, COLUMN_DISPOSALSTATUS);
            if (!isSampleStatusChanged && !isDisposalStatusChanged) continue;
            String sampleStatus = primary.getValue(i, COLUMN_SAMPLESTATUS, "");
            boolean retainFlag = false;
            retainFlag = primary.isValidColumn(COLUMN_DISPOSALSTATUS) ? primary.getString(i, COLUMN_DISPOSALSTATUS, "").equals("Retained") : beforeEditImagePrimary.getValue(i, COLUMN_DISPOSALSTATUS, "").equals("Retained");
            String sampleStatusPrev = beforeEditImagePrimary.getValue(i, COLUMN_SAMPLESTATUS, "");
            String reviewRequiredFlag = beforeEditImagePrimary.getValue(i, COLUMN_REVIEWREQUIRED, "N");
            if (retainFlag && (batchstageStatus.equalsIgnoreCase(LV_BatchStage.BATCHSTAGE_PENDINGRELEASE) || batchstageStatus.equalsIgnoreCase(LV_BatchStage.BATCHSTAGE_RELEASED))) continue;
            if (isSampleStatusChanged && (sampleStatus.equalsIgnoreCase("Received") || sampleStatus.equalsIgnoreCase(SAMPLESTATUS_INPROGRESS) && (!retainFlag || !batchstageStatus.equalsIgnoreCase(LV_BatchStage.BATCHSTAGE_PENDINGRELEASE) && !batchstageStatus.equalsIgnoreCase(LV_BatchStage.BATCHSTAGE_RELEASED)) || sampleStatus.equalsIgnoreCase(SAMPLESTATUS_COMPLETED) && (sampleStatusPrev.equalsIgnoreCase(SAMPLESTATUS_INITIAL) || sampleStatusPrev.equalsIgnoreCase("Received")))) {
                if (batchStageId.length() > 0 && !inProgressStageSet.contains(batchStageId)) {
                    inProgressBatchStages.append(";").append(batchStageId);
                    inProgressStageSet.add(batchStageId);
                }
            } else if (!sampleStatus.equalsIgnoreCase(SAMPLESTATUS_REVIEWED) && !sampleStatus.equalsIgnoreCase(SAMPLESTATUS_REPORTED) && !sampleStatus.equalsIgnoreCase(SAMPLESTATUS_CANCELLED) && !retainFlag && sampleStatus.equalsIgnoreCase(SAMPLESTATUS_COMPLETED) && reviewRequiredFlag.equals("Y") && (sampleStatusPrev.equalsIgnoreCase(SAMPLESTATUS_COMPLETED) || sampleStatusPrev.equalsIgnoreCase(SAMPLESTATUS_REVIEWED) || sampleStatusPrev.equalsIgnoreCase(SAMPLESTATUS_REPORTED) || sampleStatusPrev.equalsIgnoreCase(SAMPLESTATUS_CANCELLED)) && batchStageId.length() > 0 && !inProgressStageSet.contains(batchStageId)) {
                inProgressBatchStages.append(";").append(batchStageId);
                inProgressStageSet.add(batchStageId);
            }
            if (!(sampleStatus.equalsIgnoreCase(SAMPLESTATUS_COMPLETED) || sampleStatus.equalsIgnoreCase(SAMPLESTATUS_REVIEWED) || sampleStatus.equalsIgnoreCase(SAMPLESTATUS_REPORTED) || sampleStatus.equalsIgnoreCase("Disposed") && !batchstageStatus.equalsIgnoreCase(LV_BatchStage.BATCHSTAGE_CANCELLED) || sampleStatus.equalsIgnoreCase(SAMPLESTATUS_CANCELLED) && !batchstageStatus.equalsIgnoreCase(LV_BatchStage.BATCHSTAGE_CANCELLED)) && !retainFlag || batchStageId.length() <= 0 || pendingReleaseStageSet.contains(batchStageId)) continue;
            pendingReleaseBatchStages.append(";").append(batchStageId);
            pendingReleaseStageSet.add(batchStageId);
        }
        if (inProgressBatchStages.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            rsetid = BaseSDIDataAction.createBypassSecurityRSet("LV_BatchStage", inProgressBatchStages.substring(1), "", "", this.database, this.connectionInfo, false);
            str = new StringBuffer("SELECT bs.s_batchstageid, bs.batchstagestatus FROM s_batchstage bs, rsetitems r");
            str.append(" WHERE r.rsetid=").append(safeSQL.addVar(rsetid));
            str.append(" AND r.sdcid='LV_BatchStage'");
            str.append(" AND r.keyid1 = bs.s_batchstageid");
            str.append(" AND (bs.batchstagestatus != '").append(LV_BatchStage.BATCHSTAGE_INPROGRESS).append("' OR bs.batchstagestatus IS NULL)");
            DataSet toBeInprogress = this.getQueryProcessor().getPreparedSqlDataSet(str.toString(), safeSQL.getValues());
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_RELEASED);
            DataSet releasedBatchStages = toBeInprogress.getFilteredDataSet(filter);
            filter.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_REJECTED);
            DataSet rejectedBatchStages = toBeInprogress.getFilteredDataSet(filter);
            if (releasedBatchStages.getRowCount() > 0 || rejectedBatchStages.getRowCount() > 0) {
                String stagesToUnDisposition = "";
                if (releasedBatchStages.getRowCount() > 0) {
                    stagesToUnDisposition = stagesToUnDisposition + releasedBatchStages.getColumnValues("s_batchstageid", ";");
                }
                if (rejectedBatchStages.getRowCount() > 0) {
                    if (stagesToUnDisposition.length() > 0) {
                        stagesToUnDisposition = stagesToUnDisposition + ";";
                    }
                    stagesToUnDisposition = stagesToUnDisposition + rejectedBatchStages.getColumnValues("s_batchstageid", ";");
                }
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("sdcid", "LV_BatchStage");
                props.put("keyid1", stagesToUnDisposition);
                props.put("ready", "N");
                this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
            }
            if (toBeInprogress.getRowCount() > 0) {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("sdcid", "LV_BatchStage");
                props.put("keyid1", toBeInprogress.getColumnValues("s_batchstageid", ";"));
                props.put("batchstagestatus", LV_BatchStage.BATCHSTAGE_INPROGRESS);
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (pendingReleaseBatchStages.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            rsetid = BaseSDIDataAction.createBypassSecurityRSet("LV_BatchStage", pendingReleaseBatchStages.substring(1), "", "", this.database, this.connectionInfo, false);
            str = new StringBuffer("SELECT s_batchstageid FROM");
            str.append(" (SELECT b.s_batchstageid, COUNT(*) samplesinbatchstage, ");
            str.append(" SUM(CASE");
            str.append(" WHEN s.samplestatus = '").append(SAMPLESTATUS_COMPLETED).append("' AND (s.reviewrequiredflag='N' OR s.reviewrequiredflag IS NULL) THEN 1");
            str.append(" WHEN s.disposalstatus = 'Retained' OR s.samplestatus IN ('").append(SAMPLESTATUS_REVIEWED).append("', '").append(SAMPLESTATUS_REPORTED).append("', '").append("Disposed").append("', '").append(SAMPLESTATUS_CANCELLED).append("') THEN 1");
            str.append(" ELSE 0");
            str.append(" END) numfinishedsamples");
            str.append(" FROM s_sample s, s_batchstage b, rsetitems rs");
            str.append(" WHERE  rs.rsetid = ").append(safeSQL.addVar(rsetid)).append(" AND rs.keyid1 = b.s_batchstageid AND rs.sdcid='LV_BatchStage'");
            str.append(" AND s.batchstageid = b.s_batchstageid");
            str.append(" AND (b.batchstagestatus NOT IN ('").append(LV_BatchStage.BATCHSTAGE_PENDINGRELEASE).append("', '").append(LV_BatchStage.BATCHSTAGE_RELEASED).append("', '").append(LV_BatchStage.BATCHSTAGE_REJECTED).append("') OR batchstagestatus IS NULL)");
            str.append(" GROUP BY b.s_batchstageid) BATCHSTAGESAMPLES");
            str.append(" WHERE samplesinbatchstage = numfinishedsamples");
            DataSet pendingRelease = this.getQueryProcessor().getPreparedSqlDataSet(str.toString(), safeSQL.getValues());
            for (int i = 0; i < pendingRelease.getRowCount(); ++i) {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("sdcid", "LV_BatchStage");
                props.put("keyid1", pendingRelease.getValue(i, "s_batchstageid", ""));
                props.put("pendingapprovalstatus", LV_BatchStage.BATCHSTAGE_PENDINGRELEASE);
                props.put("approvalstatus", LV_BatchStage.BATCHSTAGE_RELEASED);
                props.put("approvalstatuscolumn", "batchstagestatus");
                props.put("tracelogid", primary.getValue(0, "tracelogid", ""));
                this.getActionProcessor().processAction("SubmitSDIForApproval", "1", props);
            }
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    private void addSamplesToBatch(SDIData sdiData) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
        if (policy != null && policy.getProperty("syncbatchstate", "N").equalsIgnoreCase("Y")) {
            DataSet primary = sdiData.getDataset("primary");
            String tracelogid = primary.getValue(0, "tracelogid", "");
            String activeDisplayValue = BatchLifeCycleUtil.getBatchStateDisplayValue("active");
            ArrayList<String> uniqueBatches = new ArrayList<String>();
            StringBuilder batchIdsToBeChanged = new StringBuilder();
            StringBuilder batchStatusesToBeChanged = new StringBuilder();
            String query = "SELECT batchstatus FROM s_batch WHERE s_batchid=?";
            PreparedStatement batchStatusStatement = this.database.prepareStatement("batchstatus", query);
            query = "SELECT reviewrequiredflag, samplestatus FROM s_sample WHERE s_sampleid=?";
            PreparedStatement reviewRequiredStatement = this.database.prepareStatement("reviewrequired", query);
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String batchid;
                String sampleClassification = primary.getValue(i, COLUMN_CLASSIFICATION, this.getOldPrimaryValue(primary, i, COLUMN_CLASSIFICATION));
                if (!CLASSIFICATION_QM.equals(sampleClassification) || !this.hasPrimaryValueChanged(primary, i, "batchid") || (batchid = primary.getValue(i, "batchid", "")).length() <= 0 || uniqueBatches.contains(batchid)) continue;
                uniqueBatches.add(batchid);
                try {
                    batchStatusStatement.setString(1, batchid);
                    DataSet batch = new DataSet(batchStatusStatement.executeQuery());
                    String batchStatus = batch.getValue(0, "batchstatus", "");
                    if (batchStatus.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease")) || batchStatus.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("preliminaryrelease"))) {
                        String sampleStatus;
                        boolean reviewRequiredFlag;
                        if (primary.isValidColumn(COLUMN_REVIEWREQUIRED) && primary.isValidColumn(COLUMN_SAMPLESTATUS)) {
                            reviewRequiredFlag = "Y".equals(StringUtil.getYN(primary.getValue(i, COLUMN_REVIEWREQUIRED), "N"));
                            sampleStatus = primary.getValue(i, COLUMN_SAMPLESTATUS, "");
                        } else {
                            String sampleid = primary.getValue(i, COLUMN_KEYID1, "");
                            reviewRequiredStatement.setString(1, sampleid);
                            DataSet sample = new DataSet(reviewRequiredStatement.executeQuery());
                            reviewRequiredFlag = "Y".equals(StringUtil.getYN(sample.getValue(0, COLUMN_REVIEWREQUIRED), "N"));
                            sampleStatus = sample.getValue(i, COLUMN_SAMPLESTATUS, "");
                        }
                        if (!reviewRequiredFlag && sampleStatus.equalsIgnoreCase(SAMPLESTATUS_COMPLETED) || reviewRequiredFlag && sampleStatus.equalsIgnoreCase(SAMPLESTATUS_REVIEWED) || sampleStatus.equalsIgnoreCase(SAMPLESTATUS_REPORTED)) continue;
                        batchIdsToBeChanged.append(";").append(batchid);
                        batchStatusesToBeChanged.append(";").append(activeDisplayValue);
                        continue;
                    }
                    if (!batchStatus.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("released")) && !batchStatus.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("rejected"))) continue;
                    batchIdsToBeChanged.append(";").append(batchid);
                    batchStatusesToBeChanged.append(";").append(activeDisplayValue);
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("sdcid", "Batch");
                    props.put("keyid1", batchid);
                    props.put("ready", "N");
                    this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
                    continue;
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (batchIdsToBeChanged.length() > 0) {
                this.editBatchSDI(batchIdsToBeChanged.substring(1), batchStatusesToBeChanged.substring(1), tracelogid);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void disposeRequestItemDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        try {
            DataSet primary = sdiData.getDataset("primary");
            StringBuffer requestItemDetailIdBuffer = new StringBuffer();
            String sql = "SELECT RID.s_requestitemdetailid FROM s_requestitemdetail RID, s_request R WHERE RID.linksdcid = 'Sample' AND  RID.linkkeyid1 = ? AND  RID.requestitemdetailstatus = '" + RequestManagementUtil.RequestItemDetailStatus.PENDING.getStatusValue() + "' AND  R." + "s_requestid" + " = RID.REQUESTID AND  R." + "requestclass" + " != 'Submission'";
            PreparedStatement pendingRequestItemDetails = this.database.prepareStatement("pendingrequestitemdetails", sql);
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String sampleStatus;
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) || !(sampleStatus = primary.getValue(i, COLUMN_SAMPLESTATUS, "")).equals("Disposed")) continue;
                pendingRequestItemDetails.setString(1, primary.getValue(i, COLUMN_KEYID1));
                DataSet pendingRequestItemDetailsDS = new DataSet(pendingRequestItemDetails.executeQuery());
                for (int j = 0; j < pendingRequestItemDetailsDS.getRowCount(); ++j) {
                    String requestItemDetailId = pendingRequestItemDetailsDS.getValue(j, "s_requestitemdetailid");
                    requestItemDetailIdBuffer.append(";").append(requestItemDetailId);
                }
            }
            if (requestItemDetailIdBuffer.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_RequestItemDetail");
                props.setProperty("keyid1", requestItemDetailIdBuffer.substring(1));
                String scannedDetails = actionProps.getProperty("scanneddetails");
                if (scannedDetails.length() > 0) {
                    props.setProperty("scanneddetails", scannedDetails);
                }
                props.setProperty("operation", "disposedetail");
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            this.database.closeStatement("pendingrequestitemdetails");
        }
    }

    private void changeRequestStatus(SDIData sdiData) throws SapphireException {
        PropertyList props;
        DataSet primary = sdiData.getDataset("primary");
        DataSet beforeEditImagePrimary = this.getBeforeEditImage() == null ? null : this.getBeforeEditImage().getDataset("primary");
        HashMap<String, RequestManagementUtil.RequestOperation> processedRequestMap = new HashMap<String, RequestManagementUtil.RequestOperation>();
        StringBuffer receiveRequests = new StringBuffer();
        StringBuffer unReceiveRequests = new StringBuffer();
        StringBuffer openRequests = new StringBuffer();
        StringBuffer completeRequests = new StringBuffer();
        StringBuffer unCompleteRequests = new StringBuffer();
        StringBuffer autoReleaseRequests = new StringBuffer();
        StringBuffer unReleaseRequests = new StringBuffer();
        block9: for (int i = 0; i < primary.getRowCount(); ++i) {
            String requestId;
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) || processedRequestMap.containsKey(requestId = primary.getValue(i, COLUMN_REQUESTID, beforeEditImagePrimary == null ? "" : beforeEditImagePrimary.getValue(i, COLUMN_REQUESTID, "")))) continue;
            PropertyList props2 = new PropertyList();
            props2.setProperty("sampleid", primary.getValue(i, COLUMN_KEYID1));
            props2.setProperty(COLUMN_REQUESTID, requestId);
            props2.setProperty("previoussamplestatus", beforeEditImagePrimary == null ? "" : beforeEditImagePrimary.getValue(i, COLUMN_SAMPLESTATUS));
            props2.setProperty("currentsamplestatus", primary.getValue(i, COLUMN_SAMPLESTATUS));
            RequestManagementUtil.RequestOperation requestOperation = RequestManagementUtil.getRequestOperation(props2, this.getQueryProcessor());
            requestId = props2.getProperty(COLUMN_REQUESTID, "");
            if (requestId.length() <= 0) continue;
            processedRequestMap.put(requestId, requestOperation);
            if (requestOperation == null) continue;
            switch (requestOperation) {
                case RECEIVE: {
                    receiveRequests.append(";").append(requestId);
                    continue block9;
                }
                case UNRECEIVE: {
                    unReceiveRequests.append(";").append(requestId);
                    continue block9;
                }
                case OPEN: {
                    openRequests.append(";").append(requestId);
                    continue block9;
                }
                case COMPLETE: {
                    completeRequests.append(";").append(requestId);
                    continue block9;
                }
                case AUTORELEASE: {
                    autoReleaseRequests.append(";").append(requestId);
                    continue block9;
                }
                case UNCOMPLETE: {
                    unCompleteRequests.append(";").append(requestId);
                    continue block9;
                }
                case UNRELEASE: {
                    unReleaseRequests.append(";").append(requestId);
                }
            }
        }
        ActionBlock actionBlock = new ActionBlock();
        if (receiveRequests.length() > 1) {
            props = new PropertyList();
            props.setProperty("sdcid", CLASSIFICATION_REQUEST);
            props.setProperty("keyid1", receiveRequests.substring(1));
            props.setProperty("operation", RequestManagementUtil.RequestOperation.RECEIVE.getOperationValue());
            actionBlock.setAction(RequestManagementUtil.RequestOperation.RECEIVE.getOperationValue(), "EditSDI", "1", props);
        }
        if (unReceiveRequests.length() > 1) {
            props = new PropertyList();
            props.setProperty("sdcid", CLASSIFICATION_REQUEST);
            props.setProperty("keyid1", unReceiveRequests.substring(1));
            props.setProperty("operation", RequestManagementUtil.RequestOperation.UNRECEIVE.getOperationValue());
            actionBlock.setAction(RequestManagementUtil.RequestOperation.UNRECEIVE.getOperationValue(), "EditSDI", "1", props);
        }
        if (openRequests.length() > 1) {
            props = new PropertyList();
            props.setProperty("sdcid", CLASSIFICATION_REQUEST);
            props.setProperty("keyid1", openRequests.substring(1));
            props.setProperty("operation", RequestManagementUtil.RequestOperation.OPEN.getOperationValue());
            props.setProperty("auditreason", "Samples testing started.");
            actionBlock.setAction(RequestManagementUtil.RequestOperation.OPEN.getOperationValue(), "EditSDI", "1", props);
        }
        if (completeRequests.length() > 1) {
            props = new PropertyList();
            props.setProperty("operation", RequestManagementUtil.RequestOperation.COMPLETE.getOperationValue());
            props.setProperty("auditreason", "Samples testing finished.");
            props.put("sdcid", CLASSIFICATION_REQUEST);
            props.put("keyid1", completeRequests.substring(1));
            props.put("pendingapprovalstatus", RequestManagementUtil.RequestStatus.COMPLETED.getStatusValue());
            props.put("approvalstatus", RequestManagementUtil.RequestStatus.RELEASED.getStatusValue());
            props.put("approvalstatuscolumn", "requeststatus");
            props.put("approvalfunction", "Release");
            actionBlock.setAction(RequestManagementUtil.RequestOperation.COMPLETE.getOperationValue(), "EditSDI", "1", props);
        }
        if (unCompleteRequests.length() > 1) {
            props = new PropertyList();
            props.setProperty("sdcid", CLASSIFICATION_REQUEST);
            props.setProperty("keyid1", unCompleteRequests.substring(1));
            props.setProperty("operation", RequestManagementUtil.RequestOperation.UNCOMPLETE.getOperationValue());
            props.setProperty("auditreason", "Sample un-completed");
            actionBlock.setAction(RequestManagementUtil.RequestOperation.UNCOMPLETE.getOperationValue(), "EditSDI", "1", props);
        }
        if (autoReleaseRequests.length() > 1) {
            props = new PropertyList();
            props.setProperty("sdcid", CLASSIFICATION_REQUEST);
            props.setProperty("keyid1", autoReleaseRequests.substring(1));
            props.setProperty("operation", RequestManagementUtil.RequestOperation.AUTORELEASE.getOperationValue());
            props.setProperty("auditreason", "Samples finished. Request Auto Released.");
            actionBlock.setAction(RequestManagementUtil.RequestOperation.AUTORELEASE.getOperationValue(), "EditSDI", "1", props);
        }
        if (unReleaseRequests.length() > 1) {
            props = new PropertyList();
            props.setProperty("sdcid", CLASSIFICATION_REQUEST);
            props.setProperty("keyid1", unReleaseRequests.substring(1));
            props.setProperty("operation", RequestManagementUtil.RequestOperation.UNRELEASE.getOperationValue());
            props.setProperty("auditreason", "Unrelease request");
            actionBlock.setAction(RequestManagementUtil.RequestOperation.UNRELEASE.getOperationValue(), "EditSDI", "1", props);
        }
        this.getActionProcessor().processActionBlock(actionBlock);
    }

    /*
     * Unable to fully structure code
     */
    private void changeBatchStatus(SDIData sdiData) throws SapphireException {
        policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        if (policy != null && policy.getProperty("syncbatchstate", "N").equalsIgnoreCase("Y")) {
            primary = sdiData.getDataset("primary");
            activeDisplayValue = BatchLifeCycleUtil.getBatchStateDisplayValue("active");
            receivedDisplayValue = BatchLifeCycleUtil.getBatchStateDisplayValue("received");
            initialDisplayValue = BatchLifeCycleUtil.getBatchStateDisplayValue("initial");
            pendingReleaseDisplayValue = BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease");
            releaseDisplayValue = BatchLifeCycleUtil.getBatchStateDisplayValue("released");
            rejectedDisplayValue = BatchLifeCycleUtil.getBatchStateDisplayValue("rejected");
            beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
            pendingReleaseCheck = false;
            batchIdsToBeUpdated = new StringBuilder();
            batchStatuses = new StringBuilder();
            autoReleaseBatches = new StringBuilder();
            samplesWithStatusChanged = new StringBuilder();
            pendingReleaseList = new ArrayList<String>();
            traceLogId = primary.getValue(0, "tracelogid", "");
            for (i = 0; i < primary.getRowCount(); ++i) {
                sampleClassification = primary.getValue(i, "classification", this.getOldPrimaryValue(primary, i, "classification"));
                if (!"QM".equals(sampleClassification) || !this.hasPrimaryValueChanged(primary, i, "samplestatus") && !this.hasPrimaryValueChanged(primary, i, "disposalstatus")) continue;
                samplesWithStatusChanged.append(";").append(primary.getValue(i, "s_sampleid", ""));
            }
            if (samplesWithStatusChanged.length() > 0) {
                safeSQL = new SafeSQL();
                rset_id = this.getRSet("Sample", samplesWithStatusChanged.substring(1), null, null, this.getDAMProcessor());
                sqlString = new StringBuffer("SELECT distinct s.batchid, b.batchstatus, s.s_sampleid  FROM s_batch b, s_sample s, rsetitems r WHERE r.sdcid='Sample' AND r.keyid1 = s.s_sampleid AND s.batchid=b.s_batchid AND r.rsetid=" + safeSQL.addVar(rset_id));
                sampleBatchStatus = this.getQueryProcessor().getPreparedSqlDataSet(sqlString.toString(), safeSQL.getValues());
                query = "SELECT s_sampleid, samplestatus FROM s_sample WHERE batchid=? AND s_sampleid != ?";
                selectSisterSamples = this.database.prepareStatement("sistersamples", query);
                for (i = 0; i < primary.getRowCount(); ++i) {
                    sampleid = primary.getValue(i, "s_sampleid");
                    sampleStatus = primary.getValue(i, "samplestatus");
                    sampleStatusChanged = this.hasPrimaryValueChanged(primary, i, "samplestatus");
                    row = sampleBatchStatus.findRow("s_sampleid", sampleid);
                    if (row <= -1) continue;
                    batchid = sampleBatchStatus.getValue(row, "batchid", "");
                    batchstatus = sampleBatchStatus.getValue(row, "batchstatus", "");
                    retainFlag = false;
                    retainFlag = primary.isValidColumn("disposalstatus") != false ? primary.getString(i, "disposalstatus", "").equals("Retained") : beforeEditImagePrimary.getValue(i, "disposalstatus", "").equals("Retained");
                    if (retainFlag && (batchstatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease")) || batchstatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("released"))) || batchstatus.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("onhold"))) continue;
                    if (sampleStatus.equalsIgnoreCase("Received") && !retainFlag || sampleStatus.equalsIgnoreCase("InProgress")) {
                        if (batchstatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("received"))) {
                            batchIdsToBeUpdated.append(";").append(batchid);
                            batchStatuses.append(";").append(activeDisplayValue);
                        }
                    } else if (sampleStatus.equalsIgnoreCase("Initial") && sampleStatusChanged) {
                        try {
                            selectSisterSamples.setString(1, batchid);
                            selectSisterSamples.setString(2, sampleid);
                            sisterSamples = new DataSet(selectSisterSamples.executeQuery());
                            allSisterSamplesInitial = true;
                            for (j = 0; j < sisterSamples.getRowCount(); ++j) {
                                if (sisterSamples.getValue(j, "samplestatus", "").equalsIgnoreCase("Initial")) continue;
                                allSisterSamplesInitial = false;
                                break;
                            }
                            if (!allSisterSamplesInitial || batchstatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("initial")) || batchstatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("received"))) ** GOTO lbl69
                            batchIdsToBeUpdated.append(";").append(batchid);
                            batchStatuses.append(";").append(receivedDisplayValue);
                        }
                        catch (SQLException e) {
                            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
                        }
                    } else if (sampleStatus.equalsIgnoreCase("Completed") != false || sampleStatus.equalsIgnoreCase("Reviewed") != false || sampleStatus.equalsIgnoreCase("Reported") != false || sampleStatus.equalsIgnoreCase("Disposed") != false && batchstatus.equalsIgnoreCase("Cancelled") == false || sampleStatus.equalsIgnoreCase("Cancelled") != false && batchstatus.equalsIgnoreCase("Cancelled") == false || retainFlag) {
                        pendingReleaseCheck = true;
                    }
lbl69:
                    // 6 sources

                    if (batchIdsToBeUpdated.toString().contains(batchid)) continue;
                    if (batchstatus.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease"))) {
                        beforeEditSampleStatus = beforeEditImagePrimary.getValue(i, "samplestatus");
                        if (!beforeEditSampleStatus.equalsIgnoreCase("Completed") && !beforeEditSampleStatus.equalsIgnoreCase("Reviewed") && !beforeEditSampleStatus.equalsIgnoreCase("Reported") && !beforeEditSampleStatus.equalsIgnoreCase("Disposed") && !beforeEditSampleStatus.equalsIgnoreCase("Cancelled") && !retainFlag || !OpalUtil.isNotEmpty(sampleStatus) || sampleStatus.equalsIgnoreCase("Completed") || sampleStatus.equalsIgnoreCase("Reviewed") || sampleStatus.equalsIgnoreCase("Reported") || sampleStatus.equalsIgnoreCase("Disposed") || sampleStatus.equalsIgnoreCase("Cancelled")) continue;
                        batchIdsToBeUpdated.append(";").append(batchid);
                        batchStatuses.append(";").append(activeDisplayValue);
                        continue;
                    }
                    if (!batchstatus.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("released")) && !batchstatus.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("rejected"))) continue;
                    reviewRequiredFlag = beforeEditImagePrimary.getValue(i, "reviewrequiredflag", "N");
                    if (!sampleStatusChanged || sampleStatus.equalsIgnoreCase("Completed") && reviewRequiredFlag.equals("N") || sampleStatus.equalsIgnoreCase("Reviewed") || sampleStatus.equalsIgnoreCase("Reported") || sampleStatus.equalsIgnoreCase("Disposed") || retainFlag) continue;
                    this.getDAMProcessor().clearRSet(rset_id);
                    batchIdsToBeUpdated.append(";").append(batchid);
                    batchStatuses.append(";").append(activeDisplayValue);
                    props = new HashMap<String, String>();
                    props.put("sdcid", "Batch");
                    props.put("keyid1", batchid);
                    props.put("ready", "N");
                    this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
                }
                if (pendingReleaseCheck) {
                    safeSQL.reset();
                    sql = new StringBuffer();
                    sql.append("SELECT b.s_batchid, b.batchstatus, COUNT(*) samplesinbatch,");
                    sql.append(" SUM(CASE");
                    sql.append(" WHEN s2.samplestatus = '").append("Completed").append("' AND (s2.reviewrequiredflag='N' OR s2.reviewrequiredflag IS NULL) THEN 1");
                    sql.append(" WHEN s2.disposalstatus='Retained' OR s2.samplestatus IN ('").append("Reviewed").append("', '").append("Reported").append("', '").append("Disposed").append("', '").append("Cancelled").append("') THEN 1");
                    sql.append(" ELSE 0");
                    sql.append(" END) NumberOfFinishedSampleInBatch");
                    sql.append(" FROM s_sample s, s_batch b, s_sample s2");
                    sql.append(" WHERE s.batchid = b.s_batchid AND s2.batchid = b.s_batchid");
                    sql.append(" AND s2.classification = '").append("QM").append("'");
                    sql.append(" AND b.batchstatus != ").append(safeSQL.addVar(initialDisplayValue)).append(" ");
                    sql.append(" AND b.batchstatus != ").append(safeSQL.addVar(rejectedDisplayValue)).append(" ");
                    sql.append(" AND s.s_sampleid IN (");
                    sql.append(" SELECT keyid1 FROM rsetitems WHERE rsetid = ").append(safeSQL.addVar(rset_id)).append(" )");
                    sql.append(" GROUP BY b.s_batchid, b.batchstatus");
                    pendingRelease = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (pendingRelease != null && pendingRelease.getRowCount() > 0) {
                        batches = pendingRelease.getColumnValues("s_batchid", ";");
                        autoReleaseReqBatchDS = null;
                        skipLevel_Policy = policy.getProperty("skiplevel", "");
                        if (batches.length() > 0 && skipLevel_Policy.length() > 0) {
                            safeSQL.reset();
                            batchRSet = this.getRSet("Batch", batches, null, null, this.getDAMProcessor());
                            sql = new StringBuffer("SELECT s_batch.s_batchid FROM s_product, s_batch WHERE ");
                            sql.append(" s_product.s_productid=s_batch.productid AND s_product.s_productversionid=s_batch.productversionid ");
                            sql.append(" AND s_product.autoreleaseflag = 'Y' ");
                            sql.append(" AND (s_batch.levelid IS NULL OR s_batch.levelid != ").append(safeSQL.addVar(skipLevel_Policy)).append(")");
                            sql.append(" AND s_batch.s_batchid IN ( ");
                            sql.append(" SELECT keyid1 FROM rsetitems WHERE rsetid = ").append(safeSQL.addVar(batchRSet)).append(" )");
                            autoReleaseReqBatchDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                            this.getDAMProcessor().clearRSet(batchRSet);
                        }
                        for (i = 0; i < pendingRelease.getRowCount(); ++i) {
                            numberOfFinishedSampleInBatch = pendingRelease.getInt(i, "NumberOfFinishedSampleInBatch");
                            samplesInBatch = pendingRelease.getInt(i, "samplesinbatch");
                            batchid = pendingRelease.getValue(i, "s_batchid", "");
                            if (numberOfFinishedSampleInBatch == samplesInBatch) {
                                if (pendingRelease.getValue(i, "batchstatus", "").equalsIgnoreCase(releaseDisplayValue)) continue;
                                syncBatchReleaseWithStageString = policy.getProperty("syncbatchwithstage", "N");
                                props = new HashMap<String, String>();
                                props.put("sdcid", "Batch");
                                props.put("keyid1", batchid);
                                props.put("pendingapprovalstatus", pendingReleaseDisplayValue);
                                props.put("approvalstatus", releaseDisplayValue);
                                props.put("approvalstatuscolumn", "batchstatus");
                                if (traceLogId.trim().length() > 0) {
                                    props.put("tracelogid", traceLogId);
                                }
                                batchApprovalPolicy = policy.getProperty("batchapprovalwithincidents", "Allow");
                                sendToPendingReleaseList = false;
                                if ((batchApprovalPolicy.equalsIgnoreCase("block") || batchApprovalPolicy.equalsIgnoreCase("warn")) && (hasOpenIncidents = this.hasOpenIncidents(batchid))) {
                                    sendToPendingReleaseList = true;
                                    pendingReleaseList.add(batchid);
                                }
                                if (sendToPendingReleaseList) {
                                    props.put("pendingapprovalstatus", pendingReleaseDisplayValue);
                                    props.put("approvalstatus", pendingReleaseDisplayValue);
                                }
                                syncBatchReleaseWithStage = syncBatchReleaseWithStageString.equalsIgnoreCase("N") == false;
                                isChildStagesReleased = BatchLifeCycleUtil.isChildStagesReleased(batchid, this.getQueryProcessor(), syncBatchReleaseWithStageString);
                                safeSQL.reset();
                                sqlGetApprovals = "SELECT approvaltypeid FROM sdiapproval WHERE sdcid = 'Batch' AND keyid1 = " + safeSQL.addVar(batchid);
                                sdiApprovalDs = this.getQueryProcessor().getPreparedSqlDataSet(sqlGetApprovals, safeSQL.getValues());
                                safeSQL.reset();
                                sqlGetChildStagesCount = "select count(*) from s_batchstage bs where batchid = ? and batchstagestatus not in ('" + LV_BatchStage.BATCHSTAGE_RELEASED + "','" + LV_BatchStage.BATCHSTAGE_REJECTED + "' ) ";
                                childStageCount = this.getQueryProcessor().getPreparedCount(sqlGetChildStagesCount, new Object[]{batchid});
                                if (isChildStagesReleased || !syncBatchReleaseWithStage || sdiApprovalDs.getRowCount() > 0 && childStageCount == 0) {
                                    this.getActionProcessor().processAction("SubmitSDIForApproval", "1", props);
                                } else {
                                    batchstatus = pendingRelease.getValue(i, "batchstatus", "");
                                    if (!batchstatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("active"))) {
                                        batchIdsToBeUpdated.append(";").append(batchid);
                                        batchStatuses.append(";").append(activeDisplayValue);
                                    }
                                }
                                if (autoReleaseReqBatchDS == null || autoReleaseReqBatchDS.findRow("s_batchid", batchid) == -1 || sendToPendingReleaseList) continue;
                                safeSQL.reset();
                                sql = new StringBuffer("SELECT sdispec.condition FROM sdispec,s_sample WHERE sdispec.sdcid='Sample' ");
                                sql.append("and sdispec.keyid1=s_sample.s_sampleid ");
                                sql.append("and s_sample.batchid=").append(safeSQL.addVar(batchid));
                                sql.append(" and (s_sample.disposalstatus is null or s_sample.disposalstatus ='' or s_sample.disposalstatus<>'Retained' or sdispec.condition = 'Fail')");
                                specs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                                filter = new HashMap<String, String>();
                                specInterpretation = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getCollection("SpecConditions");
                                iter = specInterpretation.iterator();
                                passCondition = "Pass";
                                while (iter.hasNext()) {
                                    condition = (PropertyList)iter.next();
                                    if (!condition.getProperty("interpretation").equals(passCondition)) continue;
                                    passCondition = condition.getProperty("SpecCond");
                                    break;
                                }
                                filter.put("condition", passCondition);
                                passedSpecs = specs.getFilteredDataSet(filter);
                                if (specs.getRowCount() <= 0 || specs.getRowCount() != passedSpecs.getRowCount() || !BatchLifeCycleUtil.isChildStagesReleased(batchid, this.getQueryProcessor(), syncBatchReleaseWithStageString)) continue;
                                autoReleaseBatches.append(";").append(batchid);
                                continue;
                            }
                            batchstatus = pendingRelease.getValue(i, "batchstatus", "");
                            if (batchstatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("active"))) continue;
                            batchIdsToBeUpdated.append(";").append(batchid);
                            batchStatuses.append(";").append(activeDisplayValue);
                        }
                    }
                }
                this.getDAMProcessor().clearRSet(rset_id);
            }
            if (batchIdsToBeUpdated.length() > 0) {
                this.editBatchSDI(batchIdsToBeUpdated.substring(1), batchStatuses.substring(1), traceLogId);
            }
            pendingReleaseBatchesWithOpenIncident = new StringBuilder();
            if (pendingReleaseList.size() > 0) {
                for (String batchid : pendingReleaseList) {
                    pendingReleaseBatchesWithOpenIncident.append(",").append(batchid);
                }
                this.setError("Pending Release Batches: ", "INFORMATION", this.getTranslationProcessor().translate("Open incidents found in child sample(s). Can not proceed to auto-release the parent batch(s): " + pendingReleaseBatchesWithOpenIncident.substring(1)));
            }
            if (autoReleaseBatches.length() > 0) {
                this.autoReleaseBatches(autoReleaseBatches.substring(1));
            }
        }
    }

    private boolean hasOpenIncidents(String batchid) throws SapphireException {
        boolean hasOpenIncidents = false;
        String numberOfOpenIncidentsInBatch = "SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3  FROM incidentitem ii, incident i  WHERE ii.sourcesdcid = 'Batch'  AND ii.sourcekeyid1 = ?  AND i.incidentid = ii.incidentid  AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed') ";
        this.database.createPreparedResultSet("openincidentsinbatch", numberOfOpenIncidentsInBatch, new String[]{batchid});
        DataSet ds = new DataSet(this.database.getResultSet("openincidentsinbatch"));
        if (ds.getRowCount() > 0) {
            hasOpenIncidents = true;
        } else {
            String numberOfOpenIncidentsInBatchStage = "SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3  FROM incidentitem ii, incident i  WHERE ii.sourcesdcid = 'LV_BatchStage'  AND ii.sourcekeyid1 in (select s_batchstageid from s_batchstage where batchid=?)  AND i.incidentid = ii.incidentid  AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed') ";
            this.database.createPreparedResultSet("openincidentsinbatchStage", numberOfOpenIncidentsInBatchStage, new String[]{batchid});
            DataSet openincidentsinbatchStageDs = new DataSet(this.database.getResultSet("openincidentsinbatchStage"));
            if (openincidentsinbatchStageDs.getRowCount() > 0) {
                hasOpenIncidents = true;
            } else {
                String numberOfOpenIncidentsInChildSamplesSql = "SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3  FROM incidentitem ii, incident i, s_sample s  WHERE s.batchid = ? AND ii.sourcesdcid = 'Sample' AND ii.sourcekeyid1 = s.s_sampleid AND i.incidentid = ii.incidentid AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed')";
                this.database.createPreparedResultSet("openincidentsinsamples", numberOfOpenIncidentsInChildSamplesSql, new String[]{batchid});
                DataSet numberOfOpenIncidentsInChildSamples = new DataSet(this.database.getResultSet("openincidentsinsamples"));
                if (numberOfOpenIncidentsInChildSamples.getRowCount() > 0) {
                    hasOpenIncidents = true;
                }
            }
        }
        return hasOpenIncidents;
    }

    private void editBatchSDI(String batchid, String batchstatus, String tracelogid) throws SapphireException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", "Batch");
        props.put("keyid1", batchid);
        props.put("batchstatus", batchstatus);
        if (tracelogid.trim().length() > 0) {
            props.put("tracelogid", tracelogid);
        }
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void autoReleaseBatches(String batchid) throws SapphireException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", "Batch");
        props.put("keyid1", batchid);
        props.put("batchstatus", BatchLifeCycleUtil.getBatchStateDisplayValue("released"));
        props.put("releasedby", "(system)");
        props.put("releaseddt", new M18NUtil(this.connectionInfo).format(DateTimeUtil.getNowCalendar()));
        props.put("disposition", "Passed");
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private String getRSet(String sdcid, String keyid1, String keyid2, String keyid3, DAMProcessor dam) {
        String rSetID = "";
        try {
            rSetID = dam.createRSet(sdcid, keyid1, keyid2, keyid3);
        }
        catch (SapphireException e) {
            Trace.logError("Failed to create RSET " + e.getMessage());
        }
        return rSetID;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.database.createPreparedResultSet("methodsampletypereference", "select distinct s.s_sampleid from s_sample s, s_qcmethodsampletype m, rsetitems r  where m.qctemplatekeyid1 = s.s_sampleid and s.s_sampleid = r.keyid1 and r.rsetid = ?", new String[]{rsetid});
        DataSet dsReferredSamples = new DataSet(this.database.getResultSet("methodsampletypereference"));
        this.database.closeResultSet("methodsampletypereference");
        if (dsReferredSamples.getRowCount() > 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Following sample(s) are being used as template in QCMethodSampleType:") + " " + dsReferredSamples.getColumnValues(COLUMN_KEYID1, ";"));
        }
        ArrayUtil.checkExistenceInArray(rsetid, actionProps, this.getQueryProcessor(), this.getTranslationProcessor(), false);
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select t.linkkeyid1");
        sql.append(" from reservestorageunit r, trackitem t");
        sql.append(" where r.trackitemid = t.trackitemid");
        sql.append(" and t.linksdcid = 'Sample'");
        sql.append(" and t.linkkeyid1 in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
        DataSet reserveds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (reserveds != null && reserveds.size() > 0) {
            if ("Y".equals(actionProps.getProperty("__sdcruleconfirm"))) {
                sql.setLength(0);
                sql.append("delete from reservestorageunit r");
                sql.append(" where r.trackitemid in ( select t.trackitemid from trackitem t where t.linksdcid = 'Sample'");
                sql.append(" and t.linkkeyid1 in ( select r.keyid1 from rsetitems r where r.rsetid = ?").append(" ) )");
                this.database.executePreparedUpdate(sql.toString(), new Object[]{rsetid});
            } else {
                throw new SapphireException("Reservations Found", "CONFIRM", this.getTranslationProcessor().translate("Reservations found for following sample(s)") + "<hr>" + reserveds.getColumnValues("linkkeyid1", "<br>") + "<hr>" + this.getTranslationProcessor().translate("Clear the reservations?"));
            }
        }
        HashSet<String> set = new HashSet<String>();
        sql.setLength(0);
        safeSQL.reset();
        sql.append("select s_sampleid, samplefamilyid, ");
        sql.append(" (select count(s.s_sampleid) from s_sample s where s.samplefamilyid = s_sample.samplefamilyid) sfcount");
        sql.append(" from s_sample");
        sql.append(" where samplefamilyid is not null");
        sql.append(" and s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                if (ds.getInt(i, "sfcount") != 1) continue;
                set.add(ds.getValue(i, "samplefamilyid"));
            }
            if (set.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_SampleFamily");
                props.setProperty("keyid1", OpalUtil.toDelimitedString(set, ";"));
                this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
            }
        }
        this.synchronizeBatchStatus(rsetid, actionProps, true);
        DataSet childds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct s_sampleid from s_childsample where s_sampleid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
        if (childds.size() > 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Child Samples found"), "VALIDATION", this.getTranslationProcessor().translate("Following samples have child samples. Delete not allowed.") + "<hr>" + childds.getColumnValues(COLUMN_KEYID1, ", "));
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String sampleid = actionProps.getProperty("keyid1");
        if (StringUtil.getLen(sampleid) > 0L) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid from trackitem where linksdcid = 'Sample' and linkkeyid1 in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
            if (ds != null && ds.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", ds.getColumnValues("trackitemid", ";"));
                this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
            }
            this.database.executePreparedUpdate("delete from s_samplemap where sourcesampleid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", new String[]{rsetid});
            this.database.executePreparedUpdate("delete from s_samplemap where destsampleid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", new String[]{rsetid});
            this.synchronizeBatchStatus(rsetid, actionProps, false);
        }
    }

    private void synchronizeBatchStatus(String rsetid, PropertyList actionProps, boolean preDeleteMode) throws SapphireException {
        if (preDeleteMode) {
            PropertyList policy = this.getConfigurationProcessor().getPolicy(POLICY_SAMPLEBATCH, "Sapphire Custom");
            if (policy != null && policy.getProperty("syncbatchstate", "N").equalsIgnoreCase("Y")) {
                SafeSQL safeSQL = new SafeSQL();
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT s.batchid FROM s_sample s, rsetitems r WHERE");
                sql.append(" r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" AND r.keyid1 = s.s_sampleid");
                DataSet batches = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (batches != null && batches.getRowCount() > 0) {
                    actionProps.setProperty("batchid", batches.getColumnValues("batchid", ";"));
                }
            }
        } else {
            String[] batches = StringUtil.split(actionProps.getProperty("batchid", ""), ";");
            StringBuffer batchBuffer = new StringBuffer();
            HashSet<String> uniqueBatchSet = new HashSet<String>();
            if (batches.length > 0) {
                for (String batchId : batches) {
                    if (batchId.length() <= 0 || !uniqueBatchSet.add(batchId)) continue;
                    batchBuffer.append(";").append(batchId);
                }
            }
            if (batchBuffer.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Batch");
                props.setProperty("keyid1", batchBuffer.substring(1));
                props.setProperty("operation", "synchronizeonly");
                props.setProperty("currentstatevalidation", BatchLifeCycleUtil.getBatchStateDisplayValue("active"));
                props.setProperty("releasevalidationrequired", "N");
                props.setProperty("auditreason", "Sample Deleted.");
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditWorkItemImage() {
        return true;
    }

    @Override
    public boolean requiresEditWorkItemPrimary() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditSDIDataImage() {
        return true;
    }

    private void setGLP(DataSet primary) {
        boolean isUserGLP = "Y".equals(OpalUtil.getColumnValue(this.getQueryProcessor(), "sysuser", "glpflag", "sysuserid=?", new String[]{this.getConnectionProcessor().getSapphireConnection().getSysuserId()}));
        if (!isUserGLP) {
            primary.setValue(-1, "glpflag", "N");
            return;
        }
        if (primary.isValidColumn("sstudyid")) {
            HashMap<String, String> studyMap = new HashMap<String, String>();
            primary.addColumn("glpflag", 0);
            for (int i = 0; i < primary.size(); ++i) {
                String studyid;
                if ("N".equals(primary.getValue(i, "glpflag")) || StringUtil.getLen(studyid = primary.getValue(i, "sstudyid", "")) <= 0L) continue;
                if (!studyMap.containsKey(studyid)) {
                    studyMap.put(studyid, Study.isGLP(this.getQueryProcessor(), studyid) ? "Y" : "N");
                }
                primary.setValue(i, "glpflag", (String)studyMap.get(studyid));
            }
            studyMap.clear();
        }
    }

    private void processEventLogRule(DataSet primary) throws SapphireException {
        if (primary != null) {
            EventLog eventLog = new EventLog(this.database, this.getSequenceProcessor());
            eventLog.setCurrentUser(this.getConnectionInfo().getSysuserId());
            HashMap sampleData = this.getSampleData(primary);
            for (int i = 0; i < primary.size(); ++i) {
                int row;
                String sampleid = primary.getValue(i, COLUMN_KEYID1);
                String tracelogid = primary.getValue(i, "tracelogid");
                HashMap primaryInfo = (HashMap)sampleData.get(sampleid);
                if (this.hasPrimaryValueChanged(primary, i, "glpflag")) {
                    row = eventLog.addRow();
                    eventLog.setString(row, "eventtype", "GLPChange");
                    eventLog.setString(row, "oldvalue", this.getOldPrimaryValue(primary, i, "glpflag"));
                    eventLog.setString(row, "newvalue", (String)primaryInfo.get("glpflag"));
                    eventLog.setString(row, "eventsdcid", SDC_SAMPLE);
                    eventLog.setString(row, "eventkeyid1", sampleid);
                    eventLog.setString(row, "eventkeyid2", "(null)");
                    eventLog.setString(row, "eventkeyid3", "(null)");
                    eventLog.setString(row, "tracelogid", tracelogid);
                    eventLog.setString(row, "departmentid", (String)primaryInfo.get("custodialdepartmentid"));
                }
                if (this.hasPrimaryValueChanged(primary, i, COLUMN_STORAGESTATUS)) {
                    row = eventLog.addRow();
                    eventLog.setString(row, "eventtype", "StorageStatusChange");
                    eventLog.setString(row, "oldvalue", this.getOldPrimaryValue(primary, i, COLUMN_STORAGESTATUS));
                    eventLog.setString(row, "newvalue", (String)primaryInfo.get(COLUMN_STORAGESTATUS));
                    eventLog.setString(row, "eventsdcid", SDC_SAMPLE);
                    eventLog.setString(row, "eventkeyid1", sampleid);
                    eventLog.setString(row, "eventkeyid2", "(null)");
                    eventLog.setString(row, "eventkeyid3", "(null)");
                    eventLog.setString(row, "tracelogid", tracelogid);
                    eventLog.setString(row, "departmentid", (String)primaryInfo.get("custodialdepartmentid"));
                }
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS)) continue;
                row = eventLog.addRow();
                eventLog.setString(row, "eventtype", "SampleStatusChange");
                eventLog.setString(row, "oldvalue", this.getOldPrimaryValue(primary, i, COLUMN_SAMPLESTATUS));
                eventLog.setString(row, "newvalue", (String)primaryInfo.get(COLUMN_SAMPLESTATUS));
                eventLog.setString(row, "eventsdcid", SDC_SAMPLE);
                eventLog.setString(row, "eventkeyid1", sampleid);
                eventLog.setString(row, "eventkeyid2", "(null)");
                eventLog.setString(row, "eventkeyid3", "(null)");
                eventLog.setString(row, "tracelogid", tracelogid);
                eventLog.setString(row, "departmentid", (String)primaryInfo.get("custodialdepartmentid"));
            }
            eventLog.process();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private HashMap getSampleData(DataSet primary) throws SapphireException {
        DataSet ds;
        HashMap dataMap = new HashMap();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select s.s_sampleid, s.samplestatus, s.storagestatus, s.glpflag, t.custodialdepartmentid, t.currentstorageunitid, s.sstudyid");
        sql.append(" from s_sample s, trackitem t");
        sql.append(" where t.linksdcid = 'Sample'");
        sql.append(" and t.linkkeyid1 = s.s_sampleid");
        if (primary.size() > 750) {
            safeSQL.reset();
            String rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, primary.getColumnValues(COLUMN_KEYID1, ";"), null, null);
            if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("Error creating RSET for samples");
            sql.append(" and s.s_sampleid in ( SELECT keyid1 FROM rsetitems WHERE rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            sql.append(" and s.s_sampleid in ( ").append(safeSQL.addIn(primary.getColumnValues(COLUMN_KEYID1, "','"))).append(" )");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (ds == null) return dataMap;
        for (int i = 0; i < ds.size(); ++i) {
            HashMap<String, String> map = new HashMap<String, String>();
            String sampleid = ds.getValue(i, COLUMN_KEYID1);
            map.put(COLUMN_KEYID1, sampleid);
            map.put(COLUMN_SAMPLESTATUS, ds.getValue(i, COLUMN_SAMPLESTATUS));
            map.put(COLUMN_STORAGESTATUS, ds.getValue(i, COLUMN_STORAGESTATUS));
            map.put("glpflag", ds.getValue(i, "glpflag"));
            map.put("custodialdepartmentid", ds.getValue(i, "custodialdepartmentid"));
            dataMap.put(sampleid, map);
        }
        return dataMap;
    }

    private void checkSampleFamilyCreation(DataSet primary, PropertyList actionProps, boolean forceUpdate) {
        String ignoreparticipant = "N";
        DataSet familyColumnDS = new DataSet();
        for (Object o : actionProps.keySet()) {
            String key = (String)o;
            if ("__samplefamily_ignoreparticipant".equals(key)) {
                ignoreparticipant = "Y";
                continue;
            }
            if (key.startsWith("__samplefamily_")) {
                familyColumnDS.addColumnValues(key.substring(15), 0, actionProps.getProperty(key, ""), ";");
                continue;
            }
            if (!"kitid".equals(key)) continue;
            familyColumnDS.addColumnValues("kittrackitem", 0, actionProps.getProperty(key, ""), ";");
        }
        DataSet ds = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            if (StringUtil.getLen(primary.getString(i, "samplefamilyid", "")) != 0L) continue;
            int row = ds.addRow();
            ds.setString(row, "sstudyid", primary.getString(i, "sstudyid", ""));
            ds.setString(row, COLUMN_SAMPLETYPEID, primary.getString(i, COLUMN_SAMPLETYPEID, ""));
            if (familyColumnDS.size() > 0) {
                int familyrow = 0;
                if (familyColumnDS.size() > 1 && row < familyColumnDS.size()) {
                    familyrow = i;
                }
                for (int col = 0; col < familyColumnDS.getColumnCount(); ++col) {
                    String columnid = familyColumnDS.getColumnId(col);
                    ds.setString(row, columnid, familyColumnDS.getString(familyrow, columnid));
                }
            }
            String requestId = primary.getString(i, COLUMN_REQUESTID, "");
            String requestItemId = primary.getString(i, COLUMN_REQUESTITEMID, "");
            if (StringUtil.getLen(requestId) > 0L) {
                ds.setString(row, COLUMN_REQUESTID, requestId);
            }
            if (StringUtil.getLen(requestItemId) <= 0L) continue;
            ds.setString(row, COLUMN_REQUESTITEMID, requestItemId);
        }
        try {
            if (ds.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_SampleFamily");
                props.setProperty("sstudyid", ds.getColumnValues("sstudyid", ";"));
                props.setProperty(COLUMN_SAMPLETYPEID, ds.getColumnValues(COLUMN_SAMPLETYPEID, ";"));
                props.setProperty(COLUMN_REQUESTID, ds.getColumnValues(COLUMN_REQUESTID, ";"));
                props.setProperty(COLUMN_REQUESTITEMID, ds.getColumnValues(COLUMN_REQUESTITEMID, ";"));
                if (familyColumnDS.size() > 0) {
                    for (int col = 0; col < familyColumnDS.getColumnCount(); ++col) {
                        String columnid = familyColumnDS.getColumnId(col);
                        props.setProperty(columnid, familyColumnDS.getColumnValues(columnid, ";"));
                    }
                }
                props.setProperty("copies", String.valueOf(ds.size()));
                props.setProperty("__ignoreparticipant", ignoreparticipant);
                props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
                String newFamilyIds = props.getProperty("newkeyid1");
                String[] sampleids = StringUtil.split(newFamilyIds, ";");
                int j = 0;
                for (int i = 0; i < primary.size(); ++i) {
                    if (StringUtil.getLen(primary.getValue(i, "samplefamilyid", "")) != 0L) continue;
                    primary.setString(i, "samplefamilyid", sampleids[j++]);
                }
            }
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
    }

    private void checkSampleTrackItemRule(DataSet primary, PropertyList actionProps) throws SapphireException {
        String parentsampleid;
        boolean addTIForAllSamples;
        HashSet<String> sampleTypeSet = new HashSet<String>();
        for (int i = 0; i < primary.size(); ++i) {
            String samplesubtypeid;
            String sampletypeid = primary.getString(i, COLUMN_SAMPLETYPEID, "").trim();
            if (sampletypeid.length() > 0) {
                sampleTypeSet.add(sampletypeid);
            }
            if ((samplesubtypeid = primary.getString(i, COLUMN_SAMPLESUBTYPEID, "").trim()).length() <= 0) continue;
            sampleTypeSet.add(samplesubtypeid);
        }
        DataSet sampleTypeWithFTDS = new DataSet();
        SafeSQL safeSQL = new SafeSQL();
        if (sampleTypeSet.size() > 0) {
            sampleTypeWithFTDS = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampletypeid, freezethawcountwarn, freezethawcountmax from s_sampletype where freezethawflag = 'Y' and s_sampletypeid in (" + safeSQL.addIn(sampleTypeSet) + ")", safeSQL.getValues());
        }
        boolean bl = addTIForAllSamples = primary.getString(0, "sstudyid", "").length() > 0 || "Y".equals(actionProps.getProperty("addtrackitem", "N"));
        if (!addTIForAllSamples && (parentsampleid = actionProps.getProperty("parent_sampleid", actionProps.getProperty("parentsampleid", "")).trim()).length() > 0) {
            safeSQL.reset();
            DataSet ds = null;
            if (parentsampleid.length() > 750) {
                String rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, parentsampleid, null, null);
                try {
                    rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, parentsampleid, null, null, true, 1);
                    StringBuilder sql = new StringBuilder("select trackitemid trackitemcount from rsetitems r, trackitem where linksdcid = 'Sample'");
                    sql.append(" and r.rsetid=").append(safeSQL.addVar(rsetid));
                    sql.append(" and r.keyid1=trackitem.linkkeyid1");
                    sql.append(" and r.sdcid=trackitem.linksdcid");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                }
                catch (SapphireException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            } else {
                String sql = "select trackitemid trackitemcount from trackitem where linksdcid = 'Sample' and linkkeyid1 in (" + safeSQL.addIn(parentsampleid, ";") + ")";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
            if (ds != null && ds.size() > 0) {
                addTIForAllSamples = true;
            }
        }
        DataSet addTrackItemDS = new DataSet();
        if (addTIForAllSamples) {
            DataSet trackitemColumnDS = new DataSet();
            for (Object o : actionProps.keySet()) {
                String key = (String)o;
                if (!key.startsWith("__trackitem_")) continue;
                trackitemColumnDS.addColumnValues(key.substring(12), 0, actionProps.getProperty(key, ""), ";");
            }
            for (int i = 0; i < primary.size(); ++i) {
                String sampleid = primary.getValue(i, COLUMN_KEYID1);
                int row = addTrackItemDS.addRow();
                addTrackItemDS.setString(row, "linksdcid", SDC_SAMPLE);
                addTrackItemDS.setString(row, "linkkeyid1", sampleid);
                if (trackitemColumnDS.size() > 0) {
                    int trackitemrow = 0;
                    if (trackitemColumnDS.size() > 1 && row < trackitemColumnDS.size()) {
                        trackitemrow = i;
                    }
                    for (int col = 0; col < trackitemColumnDS.getColumnCount(); ++col) {
                        String columnid = trackitemColumnDS.getColumnId(col);
                        addTrackItemDS.setString(row, columnid, trackitemColumnDS.getString(trackitemrow, columnid));
                    }
                }
                if (actionProps.containsKey("freezethawflag") || actionProps.containsKey("__trackitem_freezethawflag")) continue;
                boolean setFreezeThaw = false;
                String freezethawcountwarn = "";
                String freezethawcountmax = "";
                String sampletypeid = primary.getString(i, COLUMN_SAMPLETYPEID, "").trim();
                String samplesubtypeid = primary.getString(i, COLUMN_SAMPLESUBTYPEID, "").trim();
                if (sampletypeid.length() > 0) {
                    int subsampletyperow;
                    int sampletyperow = sampleTypeWithFTDS.findRow("s_sampletypeid", sampletypeid);
                    if (sampletyperow != -1) {
                        if (samplesubtypeid.length() > 0) {
                            subsampletyperow = sampleTypeWithFTDS.findRow("s_sampletypeid", samplesubtypeid);
                            if (subsampletyperow != -1) {
                                setFreezeThaw = true;
                                freezethawcountwarn = sampleTypeWithFTDS.getValue(subsampletyperow, "freezethawcountwarn", "");
                                freezethawcountmax = sampleTypeWithFTDS.getValue(subsampletyperow, "freezethawcountmax", "");
                            }
                        } else {
                            setFreezeThaw = true;
                            freezethawcountwarn = sampleTypeWithFTDS.getValue(sampletyperow, "freezethawcountwarn", "");
                            freezethawcountmax = sampleTypeWithFTDS.getValue(sampletyperow, "freezethawcountmax", "");
                        }
                    } else if (samplesubtypeid.length() > 0 && (subsampletyperow = sampleTypeWithFTDS.findRow("s_sampletypeid", samplesubtypeid)) != -1) {
                        setFreezeThaw = true;
                        freezethawcountwarn = sampleTypeWithFTDS.getValue(subsampletyperow, "freezethawcountwarn", "");
                        freezethawcountmax = sampleTypeWithFTDS.getValue(subsampletyperow, "freezethawcountmax", "");
                    }
                }
                if (setFreezeThaw) {
                    addTrackItemDS.setString(row, "freezethawflag", "Y");
                    addTrackItemDS.setString(row, "freezethawcountwarn", freezethawcountwarn);
                    addTrackItemDS.setString(row, "freezethawcountmax", freezethawcountmax);
                    continue;
                }
                addTrackItemDS.setString(row, "freezethawflag", "");
                addTrackItemDS.setString(row, "freezethawcountwarn", "");
                addTrackItemDS.setString(row, "freezethawcountmax", "");
            }
        } else if (sampleTypeWithFTDS.size() > 0) {
            for (int i = 0; i < primary.size(); ++i) {
                boolean hasFreezeThaw = false;
                String freezethawcountwarn = "";
                String freezethawcountmax = "";
                String sampletypeid = primary.getString(i, COLUMN_SAMPLETYPEID, "").trim();
                String samplesubtypeid = primary.getString(i, COLUMN_SAMPLESUBTYPEID, "").trim();
                if (sampletypeid.length() > 0) {
                    int subsampletyperow;
                    int sampletyperow = sampleTypeWithFTDS.findRow("s_sampletypeid", sampletypeid);
                    if (sampletyperow != -1) {
                        if (samplesubtypeid.length() > 0) {
                            subsampletyperow = sampleTypeWithFTDS.findRow("s_sampletypeid", samplesubtypeid);
                            if (subsampletyperow != -1) {
                                hasFreezeThaw = true;
                                freezethawcountwarn = sampleTypeWithFTDS.getValue(subsampletyperow, "freezethawcountwarn", "");
                                freezethawcountmax = sampleTypeWithFTDS.getValue(subsampletyperow, "freezethawcountmax", "");
                            }
                        } else {
                            hasFreezeThaw = true;
                            freezethawcountwarn = sampleTypeWithFTDS.getValue(sampletyperow, "freezethawcountwarn", "");
                            freezethawcountmax = sampleTypeWithFTDS.getValue(sampletyperow, "freezethawcountmax", "");
                        }
                    } else if (samplesubtypeid.length() > 0 && (subsampletyperow = sampleTypeWithFTDS.findRow("s_sampletypeid", samplesubtypeid)) != -1) {
                        hasFreezeThaw = true;
                        freezethawcountwarn = sampleTypeWithFTDS.getValue(subsampletyperow, "freezethawcountwarn", "");
                        freezethawcountmax = sampleTypeWithFTDS.getValue(subsampletyperow, "freezethawcountmax", "");
                    }
                }
                if (!hasFreezeThaw) continue;
                int row = addTrackItemDS.addRow();
                addTrackItemDS.setString(row, "linksdcid", SDC_SAMPLE);
                addTrackItemDS.setString(row, "linkkeyid1", primary.getString(i, COLUMN_KEYID1));
                addTrackItemDS.setString(row, "freezethawflag", "Y");
                addTrackItemDS.setString(row, "freezethawcountwarn", freezethawcountwarn);
                addTrackItemDS.setString(row, "freezethawcountmax", freezethawcountmax);
            }
        }
        if (addTrackItemDS.size() > 0) {
            try {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("copies", Integer.toString(addTrackItemDS.size()));
                for (int col = 0; col < addTrackItemDS.getColumnCount(); ++col) {
                    String columnid = addTrackItemDS.getColumnId(col);
                    props.setProperty(columnid, addTrackItemDS.getColumnValues(columnid, ";"));
                }
                if (primary.isValidColumn("specimentype")) {
                    props.setProperty("containertypeid", primary.getColumnValues("specimentype", ";"));
                }
                props.setProperty("__sampletrackitemrule", "Y");
                props.setProperty("__sdcruleconfirm", actionProps.getProperty("__sdcruleconfirm"));
                props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag", "N"));
                this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                actionProps.setProperty("newtrackitemid", props.getProperty("newkeyid1"));
            }
            catch (Exception ex) {
                this.setError("Sample Track Item Rule", "VALIDATION", this.getTranslationProcessor().translate("Failed to create trackitems for new Samples"));
            }
        }
    }

    private Map<String, String> getSampleTypeFreezeThaw(String sampletypeid) {
        if (StringUtil.getLen(sampletypeid) > 0L) {
            if (!this.sampleTypeFTCache.containsKey(sampletypeid)) {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "select s_sampletypeid, freezethawflag, freezethawcountmax, freezethawcountwarn from s_sampletype where s_sampletypeid = " + safeSQL.addVar(sampletypeid);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("freezethawflag", ds.getString(0, "freezethawflag", ""));
                    map.put("freezethawcountmax", ds.getValue(0, "freezethawcountmax", ""));
                    map.put("freezethawcountwarn", ds.getValue(0, "freezethawcountwarn", ""));
                    this.sampleTypeFTCache.put(sampletypeid, map);
                }
            }
            return this.sampleTypeFTCache.get(sampletypeid);
        }
        return null;
    }

    private void checkStudy(DataSet primary) throws SapphireException {
        HashMap<String, String> studyStatusMap = new HashMap<String, String>();
        boolean activeRCRuleRequired = this.isRuleActive("Active RC Rule");
        boolean allowcompletedstudysamplereceive = !"N".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("allowcompletedstudysamplereceive"));
        for (int i = 0; i < primary.size(); ++i) {
            String samplefamilyid;
            String studyid = primary.getValue(i, "sstudyid");
            if ((studyid == null || studyid.trim().length() == 0) && (samplefamilyid = primary.getValue(i, "samplefamilyid")).length() > 0) {
                primary.setString(i, "sstudyid", this.getSampleFamilyStudy(samplefamilyid));
            }
            studyid = primary.getValue(i, "sstudyid");
            if (activeRCRuleRequired && studyid != null && studyid.trim().length() > 0 && !"POOL".equals(studyid) && !this.studyHasActiveRC(studyid)) {
                throw new SapphireException("ActiveRCRule", this.getTranslationProcessor().translate("Study must have an active Restriction Class"));
            }
            if (studyStatusMap.get(studyid) != null) continue;
            String studystatus = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "studystatus", "s_studyid=?", new String[]{studyid});
            if ((SAMPLESTATUS_COMPLETED.equals(studystatus) || SAMPLESTATUS_CANCELLED.equals(studystatus)) && !allowcompletedstudysamplereceive) {
                throw new SapphireException(this.getTranslationProcessor().translate("Allocating Samples in Completed or Cancelled Study is not allowed."));
            }
            studyStatusMap.put(studyid, studystatus);
        }
    }

    private String getSampleFamilyStudy(String samplefamily) {
        String key = "samplefamilystudy_" + samplefamily;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_samplefamily", "sstudyid", "s_samplefamilyid=?", new String[]{samplefamily}));
        }
        return this.localCache.get(key);
    }

    private boolean isRuleActive(String ruleid) throws SapphireException {
        String key = "isruleactive_" + ruleid;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, BaseBioBankRule.isRuleActive(ruleid, this.getConfigurationProcessor()) ? "Y" : "N");
        }
        return "Y".equals(this.localCache.get(key));
    }

    private boolean studyHasActiveRC(String studyid) throws SapphireException {
        String key = "studyhasactiverc_" + studyid;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, Study.hasActiveRC(this.database, studyid) ? "Y" : "N");
        }
        return "Y".equals(this.localCache.get(key));
    }

    private void checkGLPRule(DataSet primary, boolean forceUpdate) throws SapphireException {
        ArrayList<String> samples = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "glpflag") && !this.hasPrimaryValueChanged(primary, i, "sstudyid")) continue;
            samples.add(primary.getValue(i, COLUMN_KEYID1));
        }
        if (samples.size() > 0) {
            new GLPRule(this.database, this.connectionInfo).processRule(samples, forceUpdate);
        }
    }

    private String getStudyStatus(String studyid) {
        String key = "studystatus_" + studyid;
        if (!this.localCache.containsKey(key)) {
            this.localCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "studystatus", "s_studyid=?", new String[]{studyid}));
        }
        return this.localCache.get(key);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void checkActiveRCRule(DataSet primary) throws SapphireException {
        DataSet ds;
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            if ((!this.hasPrimaryValueChanged(primary, i, COLUMN_STORAGESTATUS) || !STORAGESTATUS_ALLOCATED.equals(primary.getValue(i, COLUMN_STORAGESTATUS)) && !"Received".equals(primary.getValue(i, COLUMN_STORAGESTATUS))) && !this.hasPrimaryValueChanged(primary, i, "sstudyid")) continue;
            list.add(primary.getString(i, COLUMN_KEYID1));
        }
        if (list.size() <= 0) return;
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT s.s_sampleid, s.storagestatus, s.sstudyid");
        if (list.size() > 750) {
            String rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, OpalUtil.toDelimitedString(list, ";"), null, null);
            if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("Unable to create RSET for " + OpalUtil.toDelimitedString(list, ";"));
            sql.append(" from s_sample s, rsetitems r");
            sql.append(" where s.s_sampleid = r.keyid1");
            sql.append(" and r.rsetid = ").append(safeSQL.addVar(rsetid));
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            sql.append(" from s_sample s");
            sql.append(" where s.s_sampleid in ( ").append(safeSQL.addIn(list)).append(" )");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (ds == null) return;
        ActiveRCRule rule = new ActiveRCRule(this.database, this.connectionInfo);
        try {
            for (int row = 0; row < ds.size(); ++row) {
                rule.processRule(ds.getValue(row, COLUMN_KEYID1), ds.getValue(row, COLUMN_STORAGESTATUS), ds.getValue(row, "sstudyid"));
            }
            return;
        }
        catch (SapphireException se) {
            this.setError(rule.getClass().getName(), "VALIDATION", se.getMessage());
        }
    }

    private void checkStorageStatus(DataSet primary, Map<String, Map<String, String>> sampleInfoMap) throws SapphireException {
        boolean allowcompletedstudysamplereceive = !"N".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("allowcompletedstudysamplereceive"));
        primary.addColumn(COLUMN_PREVSTORAGESTATUS, 0);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String sstudyid;
            String studystatus;
            String sampleid = primary.getValue(i, COLUMN_KEYID1);
            if (StringUtil.getLen(sampleid) == 0L) continue;
            if ((this.hasPrimaryValueChanged(primary, i, COLUMN_STORAGESTATUS) && "Received".equals(primary.getString(i, COLUMN_STORAGESTATUS, "")) || this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS) && "Received".equals(primary.getString(i, COLUMN_SAMPLESTATUS, ""))) && (SAMPLESTATUS_COMPLETED.equals(studystatus = this.getStudyStatus(sstudyid = this.getOldPrimaryValue(primary, i, "sstudyid"))) || SAMPLESTATUS_CANCELLED.equals(studystatus)) && !allowcompletedstudysamplereceive) {
                throw new SapphireException(this.getTranslationProcessor().translate("Receiving Samples in Completed or Cancelled Study is not allowed."));
            }
            Map<String, String> map = sampleInfoMap.get(sampleid);
            if (map == null) continue;
            String previousstoragestatus = map.get(COLUMN_PREVSTORAGESTATUS);
            if (this.hasPrimaryValueChanged(primary, i, COLUMN_STORAGESTATUS)) {
                if (StringUtil.getLen(primary.getString(i, COLUMN_STORAGESTATUS)) == 0L || COLUMN_PREVSTORAGESTATUS.equals(primary.getString(i, COLUMN_STORAGESTATUS).toLowerCase())) {
                    primary.setString(i, COLUMN_STORAGESTATUS, previousstoragestatus);
                }
                primary.setString(i, COLUMN_PREVSTORAGESTATUS, this.getOldPrimaryValue(primary, i, COLUMN_STORAGESTATUS));
                continue;
            }
            primary.setString(i, COLUMN_PREVSTORAGESTATUS, previousstoragestatus);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkDisposedRule(DataSet primary, boolean forceUpdate) {
        try {
            ErrorHandler errorHandler;
            ArrayList<String> disposedSamples = new ArrayList<String>();
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_STORAGESTATUS) || !"Disposed".equals(primary.getValue(i, COLUMN_STORAGESTATUS))) continue;
                primary.addColumn("storagedisposalstatus", 0);
                primary.addColumn(COLUMN_DISPOSEDBY, 0);
                primary.addColumn(COLUMN_DISPOSALDT, 2);
                primary.addColumn("glpflag", 0);
                if (Sample.isConfirmed(this.database, primary.getString(i, COLUMN_KEYID1)) && "Missing".equals(primary.getValue(i, "storagedisposalstatus"))) {
                    primary.setValue(i, "glpflag", "N");
                } else {
                    primary.setValue(i, "glpflag", Sample.getGLPFlag(this.getQueryProcessor(), primary.getString(i, COLUMN_KEYID1)));
                }
                primary.setValue(i, COLUMN_DISPOSEDBY, this.getConnectionInfo().getSysuserId());
                primary.setDate(i, COLUMN_DISPOSALDT, "n");
                disposedSamples.add(primary.getString(i, COLUMN_KEYID1));
            }
            if (disposedSamples.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", SDC_SAMPLE);
                props.setProperty("keyid1", OpalUtil.toDelimitedString(disposedSamples, ";"));
                props.setProperty("custodialuserid", "(null)");
                props.setProperty("currentstorageunitid", "(null)");
                props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
                SafeSQL safeSQL = new SafeSQL();
                if (disposedSamples.size() <= 1000) {
                    this.database.executePreparedUpdate("delete from reservestorageunit where reservestorageunit.trackitemid in (select t.trackitemid from trackitem t where t.linksdcid = 'Sample' and t.linkkeyid1 in (" + safeSQL.addIn(OpalUtil.toDelimitedString(disposedSamples, "','")) + "))", safeSQL.getValues());
                } else {
                    String rsetid = null;
                    try {
                        rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, OpalUtil.toDelimitedString(disposedSamples, ";"), null, null);
                        if (StringUtil.getLen(rsetid) > 0L) {
                            this.database.executePreparedUpdate("delete from reservestorageunit where reservestorageunit.trackitemid in (select t.trackitemid from trackitem t where t.linksdcid = 'Sample' and t.linkkeyid1 in ( select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid) + "))", safeSQL.getValues());
                        }
                    }
                    finally {
                        if (StringUtil.getLen(rsetid) > 0L) {
                            this.getDAMProcessor().clearRSet(rsetid);
                        }
                    }
                }
            }
            if ((errorHandler = this.getActionProcessor().getErrorHandler()) != null && errorHandler.hasInfoErrors()) {
                this.setErrors(this.getActionProcessor().getErrorHandler());
            }
        }
        catch (ActionException e) {
            this.setErrors(e.getErrorHandler());
        }
        catch (Exception e) {
            this.setError("Exception in CheckDisposedRule", "VALIDATION", e.getMessage());
        }
    }

    private void syncStorageStatus(DataSet primary, HashMap sampleInfoMap) {
        for (int i = 0; i < primary.size(); ++i) {
            Map map;
            String sampleid;
            if (SAMPLESTATUS_CANCELLED.equals(primary.getValue(i, COLUMN_SAMPLESTATUS)) || StringUtil.getLen(sampleid = primary.getString(i, COLUMN_KEYID1)) == 0L || (map = (Map)sampleInfoMap.get(sampleid)) == null) continue;
            String storageStatus = (String)map.get(COLUMN_STORAGESTATUS);
            String sampleStatus = (String)map.get(COLUMN_SAMPLESTATUS);
            String primarySampleStatus = primary.getValue(i, COLUMN_SAMPLESTATUS);
            String primaryStorageStatus = primary.getValue(i, COLUMN_STORAGESTATUS);
            if (primarySampleStatus == null || primarySampleStatus.length() == 0) {
                primarySampleStatus = sampleStatus;
            }
            if (primaryStorageStatus == null || primaryStorageStatus.length() == 0) {
                primaryStorageStatus = storageStatus;
            }
            if (storageStatus.equals(primaryStorageStatus) && sampleStatus.equals(primarySampleStatus)) continue;
            if ("Received".equals(primaryStorageStatus) && SAMPLESTATUS_INITIAL.equals(primarySampleStatus)) {
                this.setSampleStatus("Received", primary, i);
                continue;
            }
            if (STORAGESTATUS_TEMPORARYINLAB.equals(primaryStorageStatus) && SAMPLESTATUS_INITIAL.equals(primarySampleStatus)) {
                this.setSampleStatus("Received", primary, i);
                continue;
            }
            if (STORAGESTATUS_INPREP.equals(primaryStorageStatus) && !SAMPLESTATUS_INITIAL.equals(primarySampleStatus)) {
                this.setSampleStatus(SAMPLESTATUS_INITIAL, primary, i);
                continue;
            }
            if (STORAGESTATUS_INCIRCULATION.equals(primaryStorageStatus) && STORAGESTATUS_INPREP.equals(storageStatus) && SAMPLESTATUS_INITIAL.equals(primarySampleStatus)) {
                this.setSampleStatus("Received", primary, i);
                continue;
            }
            if (STORAGESTATUS_TEMPORARYINLAB.equals(primaryStorageStatus) && STORAGESTATUS_INPREP.equals(storageStatus) && SAMPLESTATUS_INITIAL.equals(primarySampleStatus)) {
                this.setSampleStatus("Received", primary, i);
                continue;
            }
            if (!"Disposed".equals(primaryStorageStatus)) continue;
            primary.addColumn(COLUMN_DISPOSALSTATUS, 0);
            primary.setValue(i, COLUMN_DISPOSALSTATUS, "Disposed");
        }
    }

    private void createAlias(PropertyList actionProps) {
        if (actionProps.getProperty("externalid", "").length() > 0 && actionProps.getProperty("externalidtype", "").length() > 0) {
            String[] e0 = StringUtil.split(actionProps.getProperty("newkeyid1"), ";");
            String[] e1 = StringUtil.split(actionProps.getProperty("externalid"), ";");
            String[] e2 = StringUtil.split(actionProps.getProperty("externalidtype"), ";");
            DataSet ds = new DataSet();
            if (e0.length > 1 && e1.length == 1 && e2.length == 1) {
                String aliasid = e1[0];
                String aliastype = e2[0];
                if (OpalUtil.isNotEmpty(aliasid) && OpalUtil.isNotEmpty(aliastype)) {
                    for (String sampleid : e0) {
                        int row = ds.addRow();
                        ds.setString(row, "keyid1", sampleid);
                        ds.setString(row, "externalid", aliasid);
                        ds.setString(row, "externalidtype", aliastype);
                    }
                }
            } else {
                int index = 0;
                for (String sampleid : e0) {
                    if (e1.length > index && e2.length > index && StringUtil.getLen(e1[index]) > 0L && StringUtil.getLen(e2[index]) > 0L) {
                        int row = ds.addRow();
                        ds.setString(row, "keyid1", sampleid);
                        ds.setString(row, "externalid", e1[index]);
                        ds.setString(row, "externalidtype", e2[index]);
                    }
                    ++index;
                }
            }
            if (ds.size() > 0) {
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", SDC_SAMPLE);
                    props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
                    props.setProperty("aliasid", ds.getColumnValues("externalid", ";"));
                    props.setProperty("aliastype", ds.getColumnValues("externalidtype", ";"));
                    props.setProperty("__sdcruleconfirm", "Y");
                    this.getActionProcessor().processAction("AddSDIAlias", "1", props);
                }
                catch (SapphireException e) {
                    this.logger.stackTrace(e);
                }
            }
        }
    }

    private void setSampleStatus(String newSampleStatus, DataSet primary, int rowNum) {
        primary.addColumn(COLUMN_SAMPLESTATUS, 0);
        primary.setValue(rowNum, COLUMN_SAMPLESTATUS, newSampleStatus);
    }

    public static boolean isGLP(DBAccess database, String sampleid) throws SapphireException {
        String sql = "SELECT count(s_sampleid) FROM s_sample WHERE s_sampleid=? AND glpflag = 'TRUE'";
        return database.getPreparedCount(sql, new Object[]{sampleid}) > 0;
    }

    public static boolean isConfirmed(DBAccess database, String sampleid) throws SapphireException {
        String sql = "SELECT count(s_sampleid) FROM s_sample WHERE s_sampleid=? AND confirmedby is not null";
        return database.getPreparedCount(sql, new Object[]{sampleid}) > 0;
    }

    public static void clearGLPFlag(ActionProcessor actionProcessor, List<String> sampleid, boolean forceUpdate) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", SDC_SAMPLE);
        props.setProperty("keyid1", RuleUtil.getStringList(sampleid, ";"));
        props.setProperty("glpflag", "N");
        props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
        actionProcessor.processActionClass(EditSDI.class.getName(), props);
    }

    public static String getStudyid(DBAccess database, String sampleid) throws SapphireException {
        String studyid = "";
        String sql = "SELECT sstudyid FROM s_sample WHERE s_sampleid=?";
        database.createPreparedResultSet(sql, new Object[]{sampleid});
        if (database.getNext()) {
            studyid = database.getString("sstudyid");
        }
        database.closeResultSet();
        if (studyid == null) {
            studyid = "";
        }
        return studyid;
    }

    public static String getPackageId(QueryProcessor qp, String sampleid) {
        String packageSU;
        String packageId = null;
        String suId = StorageUnitSDC.getStorageUnitId(qp, SDC_SAMPLE, sampleid);
        if (StringUtil.getLen(suId) > 0L && (packageSU = StorageUnitSDC.getStorageNodeBySDC(qp, suId, "LV_Package")) != null && packageSU.length() > 0) {
            packageId = StorageUnitSDC.getLinkKeyid1ByStorageUnitId(qp, packageSU);
        }
        return packageId;
    }

    public static String getStorageStatus(QueryProcessor queryProcessor, String sampleid) {
        return OpalUtil.getColumnValue(queryProcessor, "s_sample", COLUMN_STORAGESTATUS, "s_sampleid=?", new String[]{sampleid});
    }

    public static String getGLPFlag(QueryProcessor queryProcessor, String sampleid) {
        return OpalUtil.getColumnValue(queryProcessor, "s_sample", "glpflag", "s_sampleid = ?", new String[]{sampleid});
    }

    private void updateReagentStatus(DataSet changedSampleDS, PropertyList actionProps) throws SapphireException {
        HashMap reagentSamples = new HashMap();
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select s.s_sampleid,s.reviewrequiredflag, s.reagentlotid,");
        sql.append(" ( select rl.reagentstatus from reagentlot rl where rl.reagentlotid = s.reagentlotid ) reagentstatus");
        sql.append(" from s_sample s where (s.qcsampletype = 'Reagent Quality' OR s.classification = 'ReagentQuality')");
        ArrayList ds = null;
        if (changedSampleDS.size() > 750) {
            String rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, changedSampleDS.getColumnValues(COLUMN_KEYID1, ";"), null, null);
            if (StringUtil.getLen(rsetid) > 0L) {
                sql.append(" and s.s_sampleid in ( select r.keyid1 from rsetitems r where rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                this.getDAMProcessor().clearRSet(rsetid);
            }
        } else {
            sql.append(" and s.s_sampleid in ( ").append(safeSQL.addIn(changedSampleDS.getColumnValues(COLUMN_KEYID1, "','"))).append(" )");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String sampleid = ((DataSet)ds).getValue(i, COLUMN_KEYID1);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("reagentlotid", ((DataSet)ds).getString(i, "reagentlotid", ""));
                map.put("reagentstatus", ((DataSet)ds).getString(i, "reagentstatus", ""));
                map.put(COLUMN_REVIEWREQUIRED, ((DataSet)ds).getString(i, COLUMN_REVIEWREQUIRED, ""));
                reagentSamples.put(sampleid, map);
            }
        }
        if (reagentSamples.size() > 0) {
            String[] qsreviewoptions = StringUtil.split(actionProps.getProperty("qsreviewoption", ""), "|");
            for (int i = 0; i < changedSampleDS.size(); ++i) {
                String sampleid = changedSampleDS.getValue(i, COLUMN_KEYID1);
                if (!reagentSamples.containsKey(sampleid)) continue;
                String sampleStatus = changedSampleDS.getValue(i, COLUMN_SAMPLESTATUS);
                String reviewDisposition = changedSampleDS.getValue(i, COLUMN_REVIEWDISPOSITION);
                HashMap map = (HashMap)reagentSamples.get(sampleid);
                String reagentlotid = (String)map.get("reagentlotid");
                String reagentstatus = (String)map.get("reagentstatus");
                String reviewrequiredflag = (String)map.get(COLUMN_REVIEWREQUIRED);
                if ("N".equalsIgnoreCase(reviewrequiredflag) && SAMPLESTATUS_COMPLETED.equals(sampleStatus) || (SAMPLESTATUS_REVIEWED.equals(sampleStatus) || "Disposed".equals(sampleStatus)) && "Approved".equals(reviewDisposition)) {
                    ReagentUtil.copyMatchingDataItems(reagentlotid, sampleid, this.getQueryProcessor(), this.getActionProcessor());
                }
                if (("N".equalsIgnoreCase(reviewrequiredflag) && SAMPLESTATUS_COMPLETED.equals(sampleStatus) || (SAMPLESTATUS_REVIEWED.equals(sampleStatus) || "Disposed".equals(sampleStatus)) && "Approved".equals(reviewDisposition)) && SAMPLESTATUS_INITIAL.equalsIgnoreCase(reagentstatus)) {
                    StringBuilder qualitySampleSql = new StringBuilder();
                    safeSQL.reset();
                    qualitySampleSql.append("select s_sampleid from s_sample ");
                    qualitySampleSql.append(" where reagentlotid=").append(safeSQL.addVar(reagentlotid));
                    qualitySampleSql.append(" and ( ");
                    qualitySampleSql.append(" ( (reviewrequiredflag='Y' or reviewrequiredflag is null) and samplestatus not in('Reviewed','Disposed','Cancelled') ) ");
                    qualitySampleSql.append(" or ( reviewrequiredflag='N' and samplestatus not in('Completed','Reviewed','Disposed','Cancelled')) ");
                    qualitySampleSql.append(" ) ");
                    qualitySampleSql.append(" and ( qcsampletype = 'Reagent Quality' OR classification = 'ReagentQuality')");
                    DataSet qualitySampleDS = this.getQueryProcessor().getPreparedSqlDataSet(qualitySampleSql.toString(), safeSQL.getValues());
                    if (qualitySampleDS == null || qualitySampleDS.getRowCount() != 0) continue;
                    try {
                        PropertyList reagentStatusProps = new PropertyList();
                        reagentStatusProps.setProperty("reagentlotid", reagentlotid);
                        reagentStatusProps.setProperty("qualitysampletested", "Y");
                        this.getActionProcessor().processAction("AdvanceReagentStatus", "1", reagentStatusProps);
                        continue;
                    }
                    catch (Exception e) {
                        throw new SapphireException("Unable to process Action AdvanceReagentStatus", e);
                    }
                }
                if (!SAMPLESTATUS_REVIEWED.equals(sampleStatus) && !"Disposed".equals(sampleStatus) || !"Rejected".equals(reviewDisposition) || qsreviewoptions.length <= i) continue;
                String qsreviewoption = qsreviewoptions[i];
                if (qsreviewoption.equalsIgnoreCase("R")) {
                    try {
                        PropertyList reagentStatusProps = new PropertyList();
                        reagentStatusProps.setProperty("sdcid", "LV_ReagentLot");
                        reagentStatusProps.setProperty("keyid1", reagentlotid);
                        reagentStatusProps.setProperty("reagentstatus", "Rejected");
                        this.getActionProcessor().processAction("EditSDI", "1", reagentStatusProps);
                        continue;
                    }
                    catch (Exception e) {
                        throw new SapphireException("Unable to process Action EditSDI", e);
                    }
                }
                if (!qsreviewoption.equalsIgnoreCase("S")) continue;
                try {
                    PropertyList sampleProps = new PropertyList();
                    sampleProps.setProperty("reagentlotid", reagentlotid);
                    sampleProps.setProperty(COLUMN_SAMPLESTATUS, SAMPLESTATUS_INITIAL);
                    this.getActionProcessor().processAction("AddReagentSample", "1", sampleProps);
                    continue;
                }
                catch (Exception e) {
                    throw new SapphireException("Unable to process Action AddReagentSample", e);
                }
            }
        }
    }

    private void setDefaultQCSampleType(DataSet primary) {
        primary.addColumn("qcsampletype", 0);
        for (int i = 0; i < primary.size(); ++i) {
            String qcSampleType = primary.getValue(i, "qcsampletype");
            if (qcSampleType != null && qcSampleType.trim().length() != 0) continue;
            primary.setValue(i, "qcsampletype", SAMPLETYPE_UNKNOWN);
        }
    }

    private void setNullToCurrentProductVersion(DataSet primary) {
        for (int i = 0; i < primary.size(); ++i) {
            String prodVersion = primary.getValue(i, "productversionid");
            if (!"C".equalsIgnoreCase(prodVersion)) continue;
            primary.setValue(i, "productversionid", null);
        }
    }

    private void resolveProductVersion(String templateId, DataSet primary) throws SapphireException {
        String templateProductVersion = "";
        String templateProductId = "";
        this.database.createPreparedResultSet("GetTemplateProductId", "select productid, productversionid from s_sample where s_sampleid=?", new Object[]{templateId});
        if (this.database.getNext("GetTemplateProductId")) {
            templateProductId = this.database.getString("GetTemplateProductId", "productid");
            templateProductVersion = this.database.getString("GetTemplateProductId", "productversionid");
            templateProductVersion = templateProductVersion == null || templateProductVersion.equals("(null)") ? "" : templateProductVersion;
        }
        this.database.closeResultSet("GetTemplateProductId");
        for (int i = 0; i < primary.size(); ++i) {
            String productId = primary.getValue(i, "productid", "");
            String prodVersion = primary.getValue(i, "productversionid", "");
            if (productId.length() <= 0 || !productId.equals(templateProductId) || prodVersion.length() != 0) continue;
            if (templateProductVersion.length() == 0) {
                templateProductVersion = SdiInfo.getCurrentVersion("Product", productId, null, new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            }
            primary.setValue(i, "productversionid", templateProductVersion);
        }
    }

    private void setAvailabilityFlag(SDIData sdiData) throws SapphireException {
        DataSet datasetData = sdiData.getDataset("dataset");
        DataSet ds = new DataSet();
        ds.addColumn("sdcid", 0);
        ds.addColumn("keyid1", 0);
        ds.addColumn("paramlistid", 0);
        ds.addColumn("paramlistversionid", 0);
        ds.addColumn("variantid", 0);
        ds.addColumn("dataset", 0);
        ds.addColumn("availabilityflag", 0);
        PropertyList props = new PropertyList();
        String sdcid = "";
        long start = System.currentTimeMillis();
        StringBuffer plTypeSQL = new StringBuffer();
        plTypeSQL.append("select pl.s_paramlisttype, ds.sourceworkitemid, ds.sourceworkiteminstance ").append(" from sdiworkitemitem wii, sdidata ds, paramlist pl ").append(" where ds.sdcid=? and ds.keyid1=?").append(" and ds.paramlistid=?").append(" and ds.paramlistversionid=?").append(" and ds.variantid=?").append(" and ds.dataset=?").append(" and wii.itemsdcid='ParamList'").append(" and wii.workitemid=ds.sourceworkitemid").append(" and wii.workiteminstance=ds.sourceworkiteminstance").append(" and wii.itemkeyid1=ds.paramlistid").append(" and wii.itemkeyid2=ds.paramlistversionid").append(" and wii.itemkeyid3=ds.variantid").append(" and wii.iteminstance = ds.dataset").append(" and ds.paramlistid = pl.paramlistid").append(" and ds.paramlistversionid = pl.paramlistversionid").append(" and ds.variantid = pl.variantid").append(" and wii.sdcid = ds.sdcid").append(" and wii.keyid1 = ds.keyid1").append(" and wii.keyid2 = ds.keyid2").append(" and wii.keyid3 = ds.keyid3");
        PreparedStatement plType = this.database.prepareStatement("plType", plTypeSQL.toString());
        StringBuffer wiSQL = new StringBuffer();
        wiSQL.append("select wii.workitemid, wii.workiteminstance").append(" from sdiworkitemitem wii, sdidata ds").append(" where ds.sdcid=? and ds.keyid1=?").append(" and ds.paramlistid=?").append(" and ds.paramlistversionid=?").append(" and ds.variantid=?").append(" and ds.dataset=?").append(" and wii.itemsdcid='ParamList'").append(" and wii.itemkeyid1=ds.paramlistid").append(" and wii.itemkeyid2=ds.paramlistversionid").append(" and wii.itemkeyid3=ds.variantid").append(" and wii.iteminstance = ds.dataset").append(" and wii.sdcid = ds.sdcid").append(" and wii.keyid1 = ds.keyid1").append(" and wii.keyid2 = ds.keyid2").append(" and wii.keyid3 = ds.keyid3");
        PreparedStatement getAllWI = this.database.prepareStatement("AllWI", wiSQL.toString());
        StringBuffer procSQL = new StringBuffer();
        procSQL.append("select pl.s_paramlisttype, ds.s_datasetstatus, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset").append(" from sdiworkitemitem wii, sdidata ds, paramlist pl").append(" where wii.itemsdcid='ParamList'").append(" and ds.sourceworkitemid=?").append(" and ds.sourceworkiteminstance=?").append(" and wii.workitemid=ds.sourceworkitemid").append(" and wii.workiteminstance=ds.sourceworkiteminstance").append(" and wii.itemkeyid1=ds.paramlistid").append(" and wii.itemkeyid2=ds.paramlistversionid").append(" and wii.itemkeyid3=ds.variantid").append(" and wii.iteminstance = ds.dataset").append(" and ds.paramlistid = pl.paramlistid").append(" and ds.paramlistversionid = pl.paramlistversionid").append(" and ds.variantid = pl.variantid").append(" and ? = wii.sdcid and wii.sdcid = ds.sdcid").append(" and ? = wii.keyid1 and wii.keyid1 = ds.keyid1");
        PreparedStatement getAllDS = this.database.prepareStatement("AllDS", procSQL.toString());
        HashSet<String> keySet = new HashSet<String>();
        try {
            for (int di = 0; di < datasetData.size(); ++di) {
                if (!datasetData.getValue(di, "s_datasetstatus").equalsIgnoreCase(SAMPLESTATUS_COMPLETED)) continue;
                sdcid = datasetData.getValue(di, "sdcid");
                String sampleid = datasetData.getValue(di, "keyid1");
                String paramlistid = datasetData.getValue(di, "paramlistid");
                String paramlistversionid = datasetData.getValue(di, "paramlistversionid");
                String variantid = datasetData.getValue(di, "variantid");
                String datasetNo = datasetData.getValue(di, "dataset");
                plType.setString(1, sdcid);
                plType.setString(2, sampleid);
                plType.setString(3, paramlistid);
                plType.setString(4, paramlistversionid);
                plType.setString(5, variantid);
                plType.setString(6, datasetNo);
                DataSet plTypeDS = new DataSet(plType.executeQuery());
                if (plTypeDS == null || !plTypeDS.getValue(0, "s_paramlisttype", "").equalsIgnoreCase("Preparation")) continue;
                getAllWI.setString(1, sdcid);
                getAllWI.setString(2, sampleid);
                getAllWI.setString(3, paramlistid);
                getAllWI.setString(4, paramlistversionid);
                getAllWI.setString(5, variantid);
                getAllWI.setString(6, datasetNo);
                DataSet allWIDS = new DataSet(getAllWI.executeQuery());
                for (int wi = 0; wi < allWIDS.getRowCount(); ++wi) {
                    String workitemid = allWIDS.getValue(wi, "workitemid", "");
                    BigDecimal workiteminstance = allWIDS.getBigDecimal(wi, "workiteminstance");
                    String key = sampleid + ";" + workitemid + ";" + workiteminstance;
                    if (keySet.contains(key)) continue;
                    keySet.add(key);
                    getAllDS.setString(1, workitemid);
                    getAllDS.setBigDecimal(2, workiteminstance);
                    getAllDS.setString(3, sdcid);
                    getAllDS.setString(4, sampleid);
                    DataSet dsAll = new DataSet(getAllDS.executeQuery());
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    filterMap.put("s_paramlisttype", "Preparation");
                    DataSet dsPrep = dsAll.getFilteredDataSet(filterMap);
                    boolean allPrepDSCompleted = true;
                    for (int i = 0; i < dsPrep.getRowCount(); ++i) {
                        String datasetstatus = dsPrep.getString(i, "s_datasetstatus", "");
                        if (datasetstatus.equalsIgnoreCase(SAMPLESTATUS_COMPLETED)) continue;
                        allPrepDSCompleted = false;
                        break;
                    }
                    if (!allPrepDSCompleted) continue;
                    filterMap.put("s_paramlisttype", "Procedural");
                    DataSet dsProc = dsAll.getFilteredDataSet(filterMap);
                    for (int i = 0; i < dsProc.getRowCount(); ++i) {
                        int row = ds.addRow();
                        ds.setValue(row, "keyid1", sampleid);
                        ds.setValue(row, "paramlistid", dsProc.getValue(i, "paramlistid"));
                        ds.setValue(row, "paramlistversionid", dsProc.getValue(i, "paramlistversionid"));
                        ds.setValue(row, "variantid", dsProc.getValue(i, "variantid"));
                        ds.setValue(row, "dataset", dsProc.getValue(i, "dataset"));
                        ds.setValue(row, "availabilityflag", "Y");
                    }
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            this.database.closeStatement("plType");
            this.database.closeStatement("AllDS");
        }
        if (Trace.isDebugEnabled()) {
            Trace.log("Time elapsed in processing postEditDataSet rule setAvailabilityFlag: " + (System.currentTimeMillis() - start) + " no. of dataset processed:" + ds.getRowCount());
        }
        if (ds.size() > 0) {
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
            props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
            props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
            props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
            props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
            props.setProperty("availabilityflag", ds.getColumnValues("availabilityflag", ";"));
            props.setProperty("propsmatch", "Y");
            this.getActionProcessor().processActionClass(EditDataSet.class.getName(), props);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void applyChildSamplePlan(DataSet primary, boolean edit) throws ActionException {
        PropertyList childsampleplan;
        PropertyList policy = null;
        try {
            policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        if (policy != null && "Y".equals((childsampleplan = policy.getPropertyListNotNull("childsampleplan")).getProperty("autoapply"))) {
            ArrayList ds = null;
            String storagestatus = childsampleplan.getProperty(COLUMN_STORAGESTATUS, "").trim();
            if (storagestatus.length() > 0) {
                for (int i = 0; i < primary.size(); ++i) {
                    if (!edit) continue;
                    String sampleid = primary.getString(i, COLUMN_KEYID1);
                    if (this.hasPrimaryValueChanged(primary, i, "specimentype")) {
                        String sampleStorageStatus = primary.getString(i, COLUMN_STORAGESTATUS, "");
                        if (sampleStorageStatus.length() == 0) {
                            sampleStorageStatus = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_sample", COLUMN_STORAGESTATUS, "s_sampleid = ?", new String[]{sampleid});
                        }
                        if (!storagestatus.equals(sampleStorageStatus)) continue;
                        if (ds == null) {
                            ds = new DataSet();
                        }
                        int row = ((DataSet)ds).addRow();
                        ((DataSet)ds).setString(row, COLUMN_KEYID1, sampleid);
                        continue;
                    }
                    if (!this.hasPrimaryValueChanged(primary, i, COLUMN_STORAGESTATUS) || !primary.getString(i, COLUMN_STORAGESTATUS, "").equals(storagestatus)) continue;
                    if (ds == null) {
                        ds = new DataSet();
                    }
                    int row = ((DataSet)ds).addRow();
                    ((DataSet)ds).setString(row, COLUMN_KEYID1, sampleid);
                }
            }
            if (ds != null && ds.size() > 0) {
                StringBuilder sql = new StringBuilder();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select s.s_sampleid, ed.childsampleplanid, ed.childsampleplanversionid");
                sql.append(" from s_sample s, s_samplefamily sf, s_participantevent pe, s_eventdefstspecimendef ed");
                sql.append(" where sf.s_samplefamilyid = s.samplefamilyid");
                sql.append(" and pe.s_participanteventid = sf.participanteventid");
                sql.append(" and ed.s_eventdefid = pe.eventdefid");
                sql.append(" and ed.s_sampletypeid = s.sampletypeid");
                sql.append(" and ed.specimentype = s.specimentype");
                ArrayList childPlanDataset = null;
                if (ds.size() < 1000) {
                    sql.append(" and s.s_sampleid in (").append(safeSQL.addIn(((DataSet)ds).getColumnValues(COLUMN_KEYID1, "','"))).append(")");
                    childPlanDataset = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                } else {
                    String rsetid = null;
                    try {
                        rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, ((DataSet)ds).getColumnValues(COLUMN_KEYID1, ";"), null, null);
                        sql.append(" and s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
                        childPlanDataset = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    }
                    catch (SapphireException e) {
                        this.logger.warn(e.getMessage());
                    }
                    finally {
                        if (StringUtil.getLen(rsetid) > 0L) {
                            this.getDAMProcessor().clearRSet(rsetid);
                        }
                    }
                }
                if (childPlanDataset != null && childPlanDataset.size() > 0) {
                    boolean asynchronous = "Y".equals(childsampleplan.getProperty("asynchronousflag"));
                    PropertyList props = new PropertyList();
                    for (int i = 0; i < childPlanDataset.size(); ++i) {
                        String childsampleplanid = ((DataSet)childPlanDataset).getString(i, "childsampleplanid", "").trim();
                        String childsampleplanversionid = ((DataSet)childPlanDataset).getValue(i, "childsampleplanversionid", "").trim();
                        String sampleid = ((DataSet)childPlanDataset).getString(i, COLUMN_KEYID1, "").trim();
                        if (childsampleplanid.length() <= 0 || childsampleplanversionid.length() <= 0 || sampleid.length() <= 0) continue;
                        props.clear();
                        props.setProperty("childsampleplanid", childsampleplanid);
                        props.setProperty("childsampleplanversionid", childsampleplanversionid);
                        props.setProperty("sampleid", sampleid);
                        if (asynchronous) {
                            props.setProperty("actionid", "ApplyChildSamplePlan");
                            props.setProperty("actionversionid", "1");
                            this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props, true);
                            continue;
                        }
                        this.getActionProcessor().processActionClass(ApplyChildSamplePlan.class.getName(), props);
                    }
                }
            }
        }
    }

    @Override
    public void preAddWorkItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet sdiworkitem = sdiData.getDataset("sdiworkitem");
        if (OpalUtil.isNotEmpty(sdiworkitem)) {
            String rsetworkitem = this.getDAMProcessor().createRSet("WorkItem", sdiworkitem.getColumnValues("workitemid", ";"), sdiworkitem.getColumnValues("workitemversionid", ";"), null);
            DataSet dsworkitem = this.getQueryProcessor().getPreparedSqlDataSet("select w.workitemid, w.workitemversionid, w.applicablesampletypeid from workitem w, rsetitems r where w.workitemid = r.keyid1 and w.workitemversionid = r.keyid2 and r.rsetid = ?", (Object[])new String[]{rsetworkitem});
            this.getDAMProcessor().clearRSet(rsetworkitem);
            HashMap<String, String> sampleMap = new HashMap<String, String>();
            String rsetsample = this.getDAMProcessor().createRSet(SDC_SAMPLE, OpalUtil.toUniqueString(sdiworkitem.getColumnValues("keyid1", ";"), ";"), null, null);
            DataSet dssample = this.getQueryProcessor().getPreparedSqlDataSet("select s.s_sampleid, s.sampletypeid from s_sample s, rsetitems r where s.s_sampleid = r.keyid1 and r.rsetid = ?", (Object[])new String[]{rsetsample});
            this.getDAMProcessor().clearRSet(rsetsample);
            if (OpalUtil.isNotEmpty(dssample)) {
                for (int i = 0; i < dssample.size(); ++i) {
                    sampleMap.put(dssample.getString(i, COLUMN_KEYID1), dssample.getString(i, COLUMN_SAMPLETYPEID, ""));
                }
            }
            ArrayList<String> errorList = new ArrayList<String>();
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < sdiworkitem.size(); ++i) {
                String applicablesampletypeid;
                String workitemid = sdiworkitem.getString(i, "workitemid");
                String workitemversionid = sdiworkitem.getString(i, "workitemversionid");
                filter.clear();
                filter.put("workitemid", workitemid);
                filter.put("workitemversionid", workitemversionid);
                int row = dsworkitem.findRow(filter);
                if (row == -1 || !OpalUtil.isNotEmpty(applicablesampletypeid = dsworkitem.getString(row, "applicablesampletypeid", "")) || applicablesampletypeid.equals(sampleMap.get(sdiworkitem.getString(i, "keyid1")))) continue;
                errorList.add("<b>" + workitemid + "</b> " + this.getTranslationProcessor().translate("service can only be applied on sample with sample type") + " <b>" + applicablesampletypeid + "</b>");
            }
            if (OpalUtil.isNotEmpty(errorList)) {
                throw new SapphireException(this.getTranslationProcessor().translate("Invalid Applicable Sample Type"), "VALIDATION", "<ul><li>" + OpalUtil.toDelimitedString(errorList, "</li><li>") + "</li></ul>");
            }
        }
    }

    @Override
    public void postAddWorkItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet ds;
        DataSet sdiworkitem = sdiData.getDataset("sdiworkitem");
        HashSet<String> sdiworkitemidset = new HashSet<String>();
        for (int i = 0; i < sdiworkitem.size(); ++i) {
            String workitemversionid;
            String workitemid = sdiworkitem.getString(i, "workitemid");
            if (!this.applyChildPlanOnApply(workitemid, workitemversionid = sdiworkitem.getString(i, "workitemversionid"), false)) continue;
            sdiworkitemidset.add(sdiworkitem.getString(i, "sdiworkitemid"));
        }
        if (sdiworkitemidset.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdiworkitemid", OpalUtil.toDelimitedString(sdiworkitemidset, ";"));
            this.getActionProcessor().processActionClass(ApplyWorkItemChildSamplePlan.class.getName(), props);
        }
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select s_sampleid from s_sample where samplestatus = 'Completed'");
        if (sdiworkitem.size() > 1000) {
            String rsetid = this.getDAMProcessor().createRSet(SDC_SAMPLE, sdiworkitem.getColumnValues("keyid1", ";"), null, null);
            sql.append(" and s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (OpalUtil.isNotEmpty(rsetid)) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        } else {
            sql.append(" and s_sampleid in (").append(safeSQL.addIn(sdiworkitem.getColumnValues("keyid1", "','"))).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        if (ds != null && ds.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDC_SAMPLE);
            props.setProperty("keyid1", ds.getColumnValues(COLUMN_KEYID1, ";"));
            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
            this.getActionProcessor().processActionClass(SyncSDIDataSetStatus.class.getName(), props);
        }
    }

    @Override
    public void preAddKey(DataSet primary, PropertyList actionProps) throws SapphireException {
        String templateId;
        this.setNullToCurrentProductVersion(primary);
        String string = templateId = actionProps.getProperty("templateid").length() > 0 ? actionProps.getProperty("templateid") : actionProps.getProperty("templatekeyid1");
        if (templateId.length() > 0) {
            this.resolveProductVersion(templateId, primary);
            this.sanitizeSampleData(primary);
        }
        for (int i = 0; i < primary.size(); ++i) {
            String productid = primary.getValue(i, "productid");
            String productversionid = primary.getValue(i, "productversionid");
            if (productid.length() <= 0 || productversionid.length() != 0) continue;
            String sql = "SELECT s_productversionid FROM s_product WHERE s_productid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( s_productversionid as integer ) DESC";
            DataSet products = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{productid});
            if (products.size() <= 0) continue;
            productversionid = products.getValue(0, "s_productversionid");
            primary.setString(i, "productversionid", productversionid);
        }
        if (!this.isCMTImport()) {
            this.setWapData(primary, true, actionProps);
        }
    }

    private boolean applyChildPlanOnApply(String workitemid, String workitemversionid, boolean isChildSample) {
        Map<String, Boolean> map;
        boolean apply;
        String key = workitemid + workitemversionid;
        if (!this.wicache.containsKey(key)) {
            boolean apply2 = false;
            int aliquotcount = 0;
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select supportembeddedchildplanflag, applychildplanonapplywiflag, (select count(csp.s_childsampleplanid) from s_childsampleplanitem csp where csp.s_childsampleplanid = workitem.embedchildsampleplanid and csp.s_childsampleplanversionid = workitem.embedchildsampleplanversionid and csp.plantype = 'Aliquot') aliquotcount from workitem where workitemid = ? and workitemversionid = ?", (Object[])new String[]{workitemid, workitemversionid});
            if (ds != null && ds.size() > 0) {
                apply2 = "Y".equals(ds.getString(0, "supportembeddedchildplanflag")) && "Y".equals(ds.getString(0, "applychildplanonapplywiflag"));
                aliquotcount = ds.getInt(0, "aliquotcount", 0);
            }
            HashMap<String, Boolean> map2 = new HashMap<String, Boolean>();
            map2.put("apply", apply2);
            map2.put("hasaliquotplan", aliquotcount > 0);
            this.wicache.put(key, map2);
        }
        if (apply = (map = this.wicache.get(key)).get("apply").booleanValue()) {
            boolean hasAliquotPlan = map.get("hasaliquotplan");
            if (isChildSample && hasAliquotPlan) {
                Trace.logDebug("This is child sample and Service has Aliquots so Service will not be applied automatically. It must be applied manually.");
                apply = false;
            }
        }
        return apply;
    }

    private String getSysuserId() {
        return "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId();
    }

    public void changeMonitorGroupStatus(DataSet primary) throws SapphireException {
        HashMap<String, String> monitorGroupStatusMap = new HashMap<String, String>();
        ConcurrentSkipListSet<String> toBeInitial = new ConcurrentSkipListSet<String>();
        ConcurrentSkipListSet<String> toBeActive = new ConcurrentSkipListSet<String>();
        ConcurrentSkipListSet<String> toBeCollected = new ConcurrentSkipListSet<String>();
        ConcurrentSkipListSet<String> toBeReceived = new ConcurrentSkipListSet<String>();
        ConcurrentSkipListSet<String> toBeCompleted = new ConcurrentSkipListSet<String>();
        ConcurrentSkipListSet<String> monitorGroupsWithCancelledSamples = new ConcurrentSkipListSet<String>();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String monitorGroupId;
            if (!CLASSIFICATION_MONITOR.equals(primary.getValue(i, COLUMN_CLASSIFICATION, this.getOldPrimaryValue(primary, i, COLUMN_CLASSIFICATION)))) continue;
            boolean hasSampleStatusChanged = this.hasPrimaryValueChanged(primary, i, COLUMN_SAMPLESTATUS);
            boolean hasMonitorGroupIdChanged = this.hasPrimaryValueChanged(primary, i, COLUMN_MONITORGROUPID);
            boolean hasCollectionDateChanged = this.hasPrimaryValueChanged(primary, i, COLUMN_COLLECTIONDT);
            if (!(primary.isValidColumn(COLUMN_SAMPLESTATUS) && hasSampleStatusChanged || primary.isValidColumn(COLUMN_MONITORGROUPID) && hasMonitorGroupIdChanged) && (!primary.isValidColumn(COLUMN_COLLECTIONDT) || !hasCollectionDateChanged) || (monitorGroupId = primary.getValue(i, COLUMN_MONITORGROUPID, this.getOldPrimaryValue(primary, i, COLUMN_MONITORGROUPID))) == null || monitorGroupId.isEmpty()) continue;
            String monitorGroupStatus = (String)monitorGroupStatusMap.get(monitorGroupId);
            if (monitorGroupStatus == null) {
                String sql = "SELECT monitorgroupstatus FROM monitorgroup WHERE monitorgroupid = ?";
                DataSet monitorGroupDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{monitorGroupId});
                if (monitorGroupDs != null && monitorGroupDs.getRowCount() > 0) {
                    monitorGroupStatus = monitorGroupDs.getValue(0, "monitorgroupstatus", "");
                }
                if (monitorGroupStatus == null || monitorGroupStatus.length() == 0) {
                    throw new SapphireException(this.getTranslationProcessor().translate("Failed to get monitorgroupstatus for " + monitorGroupId));
                }
                monitorGroupStatusMap.put(monitorGroupId, monitorGroupStatus);
            }
            if (hasSampleStatusChanged || hasMonitorGroupIdChanged) {
                String currentSampleStatus = primary.getValue(i, COLUMN_SAMPLESTATUS, this.getOldPrimaryValue(primary, i, COLUMN_SAMPLESTATUS));
                if (currentSampleStatus.equals(SAMPLESTATUS_INITIAL)) {
                    if (!monitorGroupStatus.equals(SAMPLESTATUS_INITIAL)) {
                        toBeInitial.add(monitorGroupId);
                    }
                } else if (currentSampleStatus.equals("Received")) {
                    toBeActive.add(monitorGroupId);
                } else if (currentSampleStatus.equals(SAMPLESTATUS_INPROGRESS)) {
                    toBeActive.add(monitorGroupId);
                } else if (currentSampleStatus.equals(SAMPLESTATUS_COMPLETED)) {
                    boolean sampleReviewRequired = "Y".equals(primary.getValue(i, COLUMN_REVIEWREQUIRED, this.getOldPrimaryValue(primary, i, COLUMN_REVIEWREQUIRED)));
                    if (!sampleReviewRequired && (monitorGroupStatus.equals(SAMPLESTATUS_INITIAL) || monitorGroupStatus.equals("Active") || monitorGroupStatus.equals("Collected") || monitorGroupStatus.equals("Received"))) {
                        toBeCompleted.add(monitorGroupId);
                    }
                } else if (currentSampleStatus.equals(SAMPLESTATUS_REVIEWED)) {
                    if (monitorGroupStatus.equals(SAMPLESTATUS_INITIAL) || monitorGroupStatus.equals("Active") || monitorGroupStatus.equals("Collected") || monitorGroupStatus.equals("Received")) {
                        toBeCompleted.add(monitorGroupId);
                    }
                } else if (currentSampleStatus.equals(SAMPLESTATUS_REPORTED)) {
                    if (monitorGroupStatus.equals(SAMPLESTATUS_INITIAL) || monitorGroupStatus.equals("Active") || monitorGroupStatus.equals("Collected") || monitorGroupStatus.equals("Received")) {
                        toBeCompleted.add(monitorGroupId);
                    }
                } else if (currentSampleStatus.equals("Disposed")) {
                    if (monitorGroupStatus.equals(SAMPLESTATUS_INITIAL) || monitorGroupStatus.equals("Active") || monitorGroupStatus.equals("Collected") || monitorGroupStatus.equals("Received")) {
                        toBeCompleted.add(monitorGroupId);
                    }
                } else if (currentSampleStatus.equals(SAMPLESTATUS_CANCELLED)) {
                    if (monitorGroupStatus.equals(SAMPLESTATUS_INITIAL) || monitorGroupStatus.equals("Active") || monitorGroupStatus.equals("Collected") || monitorGroupStatus.equals("Received")) {
                        toBeCompleted.add(monitorGroupId);
                    }
                    monitorGroupsWithCancelledSamples.add(monitorGroupId);
                }
            }
            if (!hasCollectionDateChanged) continue;
            if (!monitorGroupStatus.equals("Collected")) {
                toBeCollected.add(monitorGroupId);
            }
            if (monitorGroupStatus.equals("Active")) continue;
            toBeActive.add(monitorGroupId);
        }
        if (toBeInitial.size() > 0) {
            for (String monitorGroupId : toBeInitial) {
                String sql = "SELECT s_sampleid FROM s_sample WHERE (samplestatus != 'Initial' OR collectiondt IS NOT NULL) AND monitorgroupid = ?";
                DataSet monitorGroupSampleDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{monitorGroupId});
                if (monitorGroupSampleDs.getRowCount() < 0) continue;
                toBeInitial.remove(monitorGroupId);
                toBeActive.add(monitorGroupId);
            }
        }
        if (toBeActive.size() > 0) {
            for (String monitorGroupId : toBeActive) {
                String sql = "SELECT s_sampleid FROM s_sample WHERE samplestatus = 'Initial' AND monitorgroupid = ?";
                DataSet monitorGroupSampleDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{monitorGroupId});
                if (monitorGroupSampleDs.getRowCount() == 0) {
                    toBeActive.remove(monitorGroupId);
                    toBeCollected.remove(monitorGroupId);
                    toBeReceived.add(monitorGroupId);
                } else {
                    toBeCollected.add(monitorGroupId);
                }
                toBeCompleted.remove(monitorGroupId);
            }
        }
        if (toBeCollected.size() > 0) {
            for (String monitorGroupId : toBeCollected) {
                String sql = "SELECT s_sampleid FROM s_sample WHERE (collectiondt IS NULL AND samplestatus = 'Initial') AND monitorgroupid = ?";
                DataSet monitorGroupSampleDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{monitorGroupId});
                if (monitorGroupSampleDs.getRowCount() == 0) {
                    toBeActive.remove(monitorGroupId);
                    toBeCollected.add(monitorGroupId);
                } else {
                    toBeCollected.remove(monitorGroupId);
                }
                toBeCompleted.remove(monitorGroupId);
            }
        }
        if (toBeCompleted.size() > 0) {
            for (String monitorGroupId : toBeCompleted) {
                if (LV_MonitorGroup.hasAllTestingFinished(monitorGroupId, this.getQueryProcessor())) continue;
                toBeCompleted.remove(monitorGroupId);
            }
        }
        if (monitorGroupsWithCancelledSamples.size() > 0) {
            ConcurrentSkipListSet<String> allModifiable = new ConcurrentSkipListSet<String>((Collection<String>)toBeInitial);
            allModifiable.addAll(toBeActive);
            allModifiable.addAll(toBeCollected);
            allModifiable.addAll(toBeReceived);
            allModifiable.addAll(toBeCompleted);
            for (String monitorGroupId : monitorGroupsWithCancelledSamples) {
                if (allModifiable.contains(monitorGroupId)) continue;
                String sql = "SELECT s_sampleid FROM s_sample WHERE samplestatus = 'Initial' AND monitorgroupid = ?";
                DataSet monitorGroupSampleDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{monitorGroupId});
                if (monitorGroupSampleDs.getRowCount() == 0) {
                    toBeReceived.add(monitorGroupId);
                    continue;
                }
                sql = "SELECT s_sampleid FROM s_sample WHERE (collectiondt IS NULL AND samplestatus = 'Initial') AND monitorgroupid = ?";
                monitorGroupSampleDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{monitorGroupId});
                if (monitorGroupSampleDs.getRowCount() != 0) continue;
                toBeCollected.add(monitorGroupId);
            }
        }
        PropertyList props = new PropertyList();
        for (String monitorGroupId : toBeInitial) {
            props.setProperty("keyid1", props.getProperty("keyid1", "") + ";" + monitorGroupId);
            props.setProperty("monitorgroupstatus", props.getProperty("monitorgroupstatus", ";Initial"));
        }
        for (String monitorGroupId : toBeActive) {
            props.setProperty("keyid1", props.getProperty("keyid1", "") + ";" + monitorGroupId);
            props.setProperty("monitorgroupstatus", props.getProperty("monitorgroupstatus", ";Active"));
        }
        for (String monitorGroupId : toBeCollected) {
            props.setProperty("keyid1", props.getProperty("keyid1", "") + ";" + monitorGroupId);
            props.setProperty("monitorgroupstatus", props.getProperty("monitorgroupstatus", ";Collected"));
        }
        for (String monitorGroupId : toBeReceived) {
            props.setProperty("keyid1", props.getProperty("keyid1", "") + ";" + monitorGroupId);
            props.setProperty("monitorgroupstatus", props.getProperty("monitorgroupstatus", ";Received"));
        }
        for (String monitorGroupId : toBeCompleted) {
            props.setProperty("keyid1", props.getProperty("keyid1", "") + ";" + monitorGroupId);
            props.setProperty("monitorgroupstatus", props.getProperty("monitorgroupstatus", ";Completed"));
        }
        if (props.getProperty("keyid1", "").length() > 0) {
            props.setProperty("keyid1", props.getProperty("keyid1").substring(1));
            props.setProperty("monitorgroupstatus", props.getProperty("monitorgroupstatus").substring(1));
            props.setProperty("sdcid", "LV_MonitorGroup");
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
    }

    public void validateProcessedDt(DataSet primary) throws SapphireException {
        String startCol = COLUMN_PROCESSINGSTARTDT;
        String endCol = COLUMN_PROCESSINGENDDT;
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, startCol) && !this.hasPrimaryValueChanged(primary, i, endCol)) continue;
            Calendar startDt = this.hasPrimaryValueChanged(primary, i, startCol) ? primary.getCalendar(i, startCol) : this.getOldPrimaryCalendar(primary, i, startCol);
            Calendar endDt = this.hasPrimaryValueChanged(primary, i, endCol) ? primary.getCalendar(i, endCol) : this.getOldPrimaryCalendar(primary, i, endCol);
            if (startDt == null || endDt == null || startDt.compareTo(endDt) <= 0) continue;
            throw new SapphireException(SDC_SAMPLE, "VALIDATION", this.getTranslationProcessor().translate("Processing Start Date can't be later than End Date. Sample: ") + primary.getString(i, COLUMN_KEYID1));
        }
    }

    private void sanitizeSampleData(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String templateFlag = primary.getValue(i, COLUMN_TEMPLATEFLAG, "");
            primary.setValue(i, COLUMN_STARTTESTINGDT, "");
            primary.setValue(i, COLUMN_COMPLETEDT, "");
            primary.setValue(i, COLUMN_REVIEWEDDT, "");
            primary.setValue(i, COLUMN_REVIEWEDBY, "");
            primary.setValue(i, COLUMN_REVIEWDISPOSITION, "");
            primary.setValue(i, COLUMN_DISPOSALDT, "");
            primary.setValue(i, COLUMN_DISPOSEDBY, "");
            primary.setValue(i, COLUMN_CONFIRMEDDT, "");
            primary.setValue(i, COLUMN_CONFIRMEDBY, "");
            primary.setValue(i, COLUMN_CANCELLEDDT, "");
            primary.setValue(i, COLUMN_CANCELLEDBY, "");
            primary.setValue(i, COLUMN_PROCESSINGSTARTDT, "");
            primary.setValue(i, COLUMN_PROCESSINGENDDT, "");
            if (!templateFlag.equalsIgnoreCase("Y")) continue;
            primary.setValue(i, COLUMN_DUEDTOVERRIDEFLAG, "");
            primary.setValue(i, COLUMN_DUEDT, "");
            primary.setValue(i, COLUMN_RECEIVEDDT, "");
            primary.setValue(i, COLUMN_COLLECTIONDT, "");
            primary.setValue(i, COLUMN_SUBMITTEDT, "");
        }
    }

    @Override
    public void postDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        String linkId = actionProps.getProperty("linkid", "");
        if ("Auxiliary Study".equals(linkId) && "S".equals(this.getSDCProcessor().getProperty("Study", "accesscontrolledflag")) && "S".equals(this.getSDCProcessor().getProperty(SDC_SAMPLE, "accesscontrolledflag"))) {
            List<String> samples;
            List<String> studyids = OpalUtil.toList(actionProps.getProperty("s_studyid", ""), ";");
            HashMap<String, String> studySecuritySetMap = new HashMap<String, String>();
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_studyid, securityset from s_study where s_studyid in (" + safeSQL.addIn(studyids) + ") and securityset is not null", safeSQL.getValues());
            if (OpalUtil.isNotEmpty(ds)) {
                for (int i = 0; i < ds.size(); ++i) {
                    String securityset = ds.getString(i, "securityset", "");
                    if (securityset.length() <= 0) continue;
                    studySecuritySetMap.put(ds.getString(i, "s_studyid"), securityset);
                }
            }
            if (studySecuritySetMap.size() > 0 && OpalUtil.isNotEmpty(samples = OpalUtil.toUniqueList(actionProps.getProperty(COLUMN_KEYID1, actionProps.getProperty("keyid1", "")), ";"))) {
                DataSet deleteSecuritySetDS = new DataSet();
                for (String sample : samples) {
                    for (String studyid : studySecuritySetMap.keySet()) {
                        int row = deleteSecuritySetDS.addRow();
                        deleteSecuritySetDS.setString(row, "keyid1", sample);
                        deleteSecuritySetDS.setString(row, "securityset", (String)studySecuritySetMap.get(studyid));
                    }
                }
                if (deleteSecuritySetDS.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", SDC_SAMPLE);
                    props.setProperty("keyid1", deleteSecuritySetDS.getColumnValues("keyid1", ";"));
                    props.setProperty("securityset", deleteSecuritySetDS.getColumnValues("securityset", ";"));
                    props.setProperty("propsmatch", "Y");
                    this.getActionProcessor().processActionClass(DeleteSDISecuritySet.class.getName(), props);
                }
            }
        }
    }

    private void copyWorkAreaToDataSetsAndSDIWorkItems(DataSet dsWorkarea) throws SapphireException {
        DataSet workitems;
        String rsetid = this.getRSet(SDC_SAMPLE, dsWorkarea.getColumnValues("sampleid", ";"), null, null, this.getDAMProcessor());
        DataSet datasets = this.fetchInfoForDataset(rsetid);
        if (datasets.size() > 0) {
            PropertyList props = this.getPropertiesForEditDataset(datasets);
            props.setProperty("s_assigneddepartment", dsWorkarea.getColumnValues("assigneddepartmentid", ";"));
            this.getActionProcessor().processAction("EditDataSet", "1", props);
        }
        if ((workitems = this.fetchInfoForWorkitem(rsetid)).size() > 0) {
            PropertyList props = this.getPropertiesForEditSDIWorkitem(workitems);
            props.setProperty("s_assigneddepartment", dsWorkarea.getColumnValues("assigneddepartmentid", ";"));
            this.getActionProcessor().processAction("EditSDIWorkItem", "1", props);
        }
    }

    private void copyAnalystToDataSetsAndSDIWorkItems(DataSet dsAnalyst) throws SapphireException {
        DataSet workitems;
        String rsetid = this.getRSet(SDC_SAMPLE, dsAnalyst.getColumnValues("sampleid", ";"), null, null, this.getDAMProcessor());
        DataSet datasets = this.fetchInfoForDataset(rsetid);
        if (datasets.size() > 0) {
            PropertyList props = this.getPropertiesForEditDataset(datasets);
            props.setProperty("s_assignedanalyst", dsAnalyst.getColumnValues("assignedanalystid", ";"));
            this.getActionProcessor().processAction("EditDataSet", "1", props);
        }
        if ((workitems = this.fetchInfoForWorkitem(rsetid)).size() > 0) {
            PropertyList props = this.getPropertiesForEditSDIWorkitem(workitems);
            props.setProperty("s_assignedanalyst", dsAnalyst.getColumnValues("assignedanalystid", ";"));
            this.getActionProcessor().processAction("EditSDIWorkItem", "1", props);
        }
    }

    private PropertyList getPropertiesForEditSDIWorkitem(DataSet workitems) {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", SDC_SAMPLE);
        props.setProperty("keyid1", workitems.getColumnValues("keyid1", ";"));
        props.setProperty("workitemid", workitems.getColumnValues("workitemid", ";"));
        props.setProperty("workiteminstance", workitems.getColumnValues("workiteminstance", ";"));
        props.setProperty("propsmatch", "Y");
        return props;
    }

    private PropertyList getPropertiesForEditDataset(DataSet ds) {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", SDC_SAMPLE);
        props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
        props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
        props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
        props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
        props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
        props.setProperty("propsmatch", "Y");
        return props;
    }

    private DataSet fetchInfoForDataset(String rsetid) {
        SafeSQL safeSQL = new SafeSQL();
        return this.getQueryProcessor().getPreparedSqlDataSet("SELECT sd.sdcid, sd.keyid1, sd.keyid2, sd.keyid3, sd.paramlistid, sd.paramlistversionid, sd.variantid, sd.dataset  FROM sdidata sd, rsetitems r   WHERE sd.sdcid = 'Sample'   AND sd.s_assigneddepartment is null   AND sd.keyid1 = r.keyid1   AND r.rsetid = " + safeSQL.addVar(rsetid), safeSQL.getValues());
    }

    private DataSet fetchInfoForWorkitem(String rsetid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sdiwisb = new StringBuffer();
        sdiwisb.append("SELECT sw.sdcid, sw.keyid1, sw.keyid2, sw.keyid3, sw.workitemid, sw.workiteminstance ").append(" FROM sdiworkitem sw, rsetitems r ").append(" WHERE sw.sdcid = 'Sample' ").append(" AND sw.s_assigneddepartment is null ").append(" AND sw.keyid1 = r.keyid1 ").append(" AND r.rsetid = " + safeSQL.addVar(rsetid));
        return this.getQueryProcessor().getPreparedSqlDataSet(sdiwisb.toString(), safeSQL.getValues());
    }

    @Override
    public void postApprove(DataSet approve) throws SapphireException {
        try {
            DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, approve);
            DataSet dsProp = new DataSet();
            for (int i = 0; i < approvedDS.size(); ++i) {
                int newRow = dsProp.addRow();
                String approvalFlag = approvedDS.getValue(i, "approvalflag");
                String reviewdisposition = "Pass".equalsIgnoreCase(approvalFlag) ? "Approved" : "Rejected";
                dsProp.setString(newRow, "keyid1", approvedDS.getValue(i, "keyid1"));
                dsProp.setString(newRow, COLUMN_SAMPLESTATUS, SAMPLESTATUS_REVIEWED);
                dsProp.setString(newRow, COLUMN_REVIEWDISPOSITION, reviewdisposition);
            }
            if (dsProp.size() > 0) {
                PropertyList props = new PropertyList();
                props.put("sdcid", SDC_SAMPLE);
                props.put("keyid1", dsProp.getColumnValues("keyid1", ";"));
                props.put(COLUMN_SAMPLESTATUS, dsProp.getColumnValues(COLUMN_SAMPLESTATUS, ";"));
                props.put(COLUMN_REVIEWDISPOSITION, dsProp.getColumnValues(COLUMN_REVIEWDISPOSITION, ";"));
                props.put("qsreviewoption", approve.getString(0, "qsreviewoption", ""));
                props.put("tracelogid", approve.getString(0, "tracelogid", ""));
                props.put(COLUMN_REVIEWEDBY, this.getConnectionInfo().getSysuserId());
                props.put(COLUMN_REVIEWEDDT, new M18NUtil(this.connectionInfo).format(DateTimeUtil.getNowCalendar()));
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
        catch (SapphireException e) {
            throw e;
        }
        catch (Exception e) {
            Logger.logInfo("Exception occured in post approve rule :" + e.getMessage());
        }
    }

    static {
        validStudySwitchStatusList.add(STORAGESTATUS_ALLOCATED);
        validStudySwitchStatusList.add("Received");
        validStudySwitchStatusList.add(STORAGESTATUS_TEMPORARYINLAB);
        whoDoneIt = new WhoDoneIt();
        whoDoneItSdiWorkItem = new WhoDoneIt("sdiworkitem");
        whoDoneItSdiData = new WhoDoneIt("dataset");
        whoDoneIt.addColumnPair(COLUMN_CANCELLEDDT, COLUMN_CANCELLEDBY, COLUMN_SAMPLESTATUS, "!=", SAMPLESTATUS_CANCELLED);
        whoDoneItSdiWorkItem.addColumnPair("starteddt", "startedby", "workitemstatus", "!=", SAMPLESTATUS_INITIAL, "current");
        whoDoneItSdiWorkItem.addColumnPair(COLUMN_CANCELLEDDT, COLUMN_CANCELLEDBY, "workitemstatus", "=", SAMPLESTATUS_CANCELLED);
        whoDoneItSdiWorkItem.addColumnPair(COLUMN_CANCELLEDDT, COLUMN_CANCELLEDBY, "workitemstatus", "!=", SAMPLESTATUS_CANCELLED);
        whoDoneItSdiWorkItem.addColumnPair("completeddt", "completedby", "workitemstatus", "=", SAMPLESTATUS_COMPLETED);
        whoDoneItSdiData.addColumnPair("starteddt", "startedby", "s_datasetstatus", "!=", SAMPLESTATUS_INITIAL, "current");
    }
}

