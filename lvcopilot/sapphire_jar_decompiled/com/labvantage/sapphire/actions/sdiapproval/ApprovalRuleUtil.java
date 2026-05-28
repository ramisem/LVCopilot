/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.admin.ddt.BatchLifeCycleUtil;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ApprovalRuleUtil {
    public static final String RULE_MULTIPLEANALYST = "multipleanalyst";
    public static final String RULE_OVERRIDDENCERTIFICATION = "overriddencertification";
    public static final String RULE_UNASSIGNEDANLYST = "unassignedanlyst";
    public static final String RULE_EXPIREDCONSUMABLE = "expiredconsumable";
    public static final String RULE_PASTDUEINSTRUMENT = "pastdueinstrument";
    public static final String RULE_RESULTFAILED = "resultfailed";
    public static final String RULE_RESULTWARNING = "resultwarning";
    public static final String RULE_RESULTMODIFIED = "resultmodified";
    public static final String RULE_RESULTINSTRUMENTMODIFIED = "resultinstrumentmodified";
    public static final String RULE_RESULTDYNAMICAUDITRECORD = "resultdynamicauditrecord";
    public static final String RULE_QCBTACHNOTREVIEWED = "qcbtachnotreviewed";
    public static final String RULE_DATASETRETEST = "datasetretest";
    public static final String RULE_DATASETCANCELLED = "datasetcancelled";
    public static final String RULE_REPLICATEADDED = "replicateadded";
    public static final String RULE_DATASETREJECTED = "datasetrejected";
    public static final String RULE_DATASETMANUALLYAPPROVED = "datasetmanuallyapproved";
    public static final String RULE_DATASETAUTOAPPROVED = "datasetautoapproved";
    public static final String RULE_SAMPLEREJECTED = "samplerejected";
    public static final String RULE_SAMPLEMANUALLYREVIEWED = "samplemanuallyreviewed";
    public static final String RULE_SAMPLEAUTOREVIEWED = "sampleautoreviewed";
    public static final String RULE_BATCHSTAGEREJECTED = "batchstagerejected";
    public static final String RULE_BATCHSTAGEMANUALLYAPPROVED = "batchstagemanuallyapproved";
    public static final String RULE_BATCHSTAGEAUTOAPPROVED = "batchstageautoapproved";
    public static final String RULE_INCIDENTFOUND = "incidentfound";
    public static final String RULE_OPENINCIDENTFOUND = "openincidentfound";
    private static final List<String> dsRuleList = new ArrayList<String>();
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_PARAMLISTID = "paramlistid";
    public static final String PROPERTY_PARAMLISTVERSIONID = "paramlistversionid";
    public static final String PROPERTY_VARIANTID = "variantid";
    public static final String PROPERTY_DATASET = "dataset";
    public static final String PROPERTY_PARAMID = "paramid";
    public static final String PROPERTY_PARAMTYPE = "paramtype";
    public static final String PROPERTY_REPLICATEID = "replicateid";
    public static final String PROPERTY_APPROVALTYPEID = "approvaltypeid";
    public static final String PROPERTY_APPROVALSTEP = "approvalstep";
    public static final String PROPERTY_APPROVALSTEPINSTANCE = "approvalstepinstance";
    public static final String PROPERTY_APPROVALFLAG = "approvalflag";
    public static final String PROPERTY_APPROVEDBY = "approvedby";
    public static final String PROPERTY_RULENAME = "rulename";
    public static final String PROPERTY_PASSRULE = "passrule";
    public static final String PROPERTY_SQLTEXT = "sqltext";
    public static final String PROPERTY_FAILUREMESSAGE = "failuremessage";
    public static final String PROPERTY_NONAUTOAPPROVALREASON = "nonautoapprovalreason";
    public static final String APPROVALRULESTATUS = "approvalrulestatus";
    public static final String APPROVALRULESTATUS_FAIL = "F";
    public static final String APPROVALRULESTATUS_PASS = "P";
    public static final String APPROVALRULESTATUS_UNRESOLVE = "U";
    public static final String DSSTATUSCOLUMN = "s_datasetstatus";
    public static final String APPROVEDBY_AUTOAP = "AutoAP";
    public static final String STATUS_PENDINGAP = "Pending AutoApprEval";
    public static final String SDC_SAMPLE = "Sample";
    public static final String SDC_BATCH = "Batch";
    public static final String SDC_BATCHSTAGE = "LV_BatchStage";
    public static final String SDC_REQUEST = "Request";
    public static final String SDC_MONITORGROUP = "LV_MonitorGroup";
    public static final String TABLE_SDIDATA = "sdidata";
    public static final String TABLE_SDIDATAITEM = "sdidataitem";
    public static final String SKIPAPPROVALRULECHECK = "skipapprovalrulecheck";
    private static boolean isOracle = true;

    private ApprovalRuleUtil() {
        throw new IllegalStateException("Utility class for Approval Rule");
    }

    public static void setIsOracle(boolean isOracle) {
        ApprovalRuleUtil.isOracle = isOracle;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void checkDatasetAutoApprovalRule(PropertyList properties, DBAccess database, ConnectionInfo connectionInfo, Logger logger, boolean asyncronous) throws SapphireException {
        isOracle = database.isOracle();
        if (!properties.getProperty(SKIPAPPROVALRULECHECK, "").equalsIgnoreCase("Y") && !properties.getProperty("modtool", "").equalsIgnoreCase("UndoSDIColumnValue")) {
            String datasetstatus = properties.getProperty(DSSTATUSCOLUMN, "");
            String sdcid = properties.getProperty(PROPERTY_SDCID, "");
            String keyid1 = properties.getProperty(PROPERTY_KEYID1, "");
            String keyid2 = properties.getProperty(PROPERTY_KEYID2, "");
            String keyid3 = properties.getProperty(PROPERTY_KEYID3, "");
            String paramlistid = properties.getProperty(PROPERTY_PARAMLISTID, "");
            String paramlistversionid = properties.getProperty(PROPERTY_PARAMLISTVERSIONID, "");
            String variantid = properties.getProperty(PROPERTY_VARIANTID, "");
            String dataset = properties.getProperty(PROPERTY_DATASET, "");
            if (datasetstatus.trim().length() > 0) {
                String[] statusArr = StringUtil.split(datasetstatus, ";");
                String[] keyid1Arr = StringUtil.split(keyid1, ";");
                String[] keyid2Arr = StringUtil.split(keyid2, ";");
                String[] keyid3Arr = StringUtil.split(keyid3, ";");
                String[] paramlistidArr = StringUtil.split(paramlistid, ";");
                String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
                String[] variantidArr = StringUtil.split(variantid, ";");
                String[] datasetArr = StringUtil.split(dataset, ";");
                PropertyList propsWithAPRules = new PropertyList();
                PreparedStatement apRulesStmnt = database.prepareStatement("aprules", ApprovalRuleUtil.getSDIDataAPRulesSQL());
                try {
                    for (int i = 0; i < statusArr.length; ++i) {
                        String status = statusArr[i];
                        if (!status.equalsIgnoreCase("Released")) continue;
                        apRulesStmnt.setString(1, paramlistidArr[i]);
                        apRulesStmnt.setString(2, paramlistversionidArr[i]);
                        apRulesStmnt.setString(3, variantidArr[i]);
                        DataSet apRulesDS = new DataSet(apRulesStmnt.executeQuery());
                        if (apRulesDS.getRowCount() <= 0 || !asyncronous) continue;
                        statusArr[i] = STATUS_PENDINGAP;
                        ApprovalRuleUtil.populateProps(propsWithAPRules, sdcid, keyid1Arr[i], keyid2Arr[i], keyid3Arr[i], paramlistidArr[i], paramlistversionidArr[i], variantidArr[i], datasetArr[i], DSSTATUSCOLUMN, "Released", "", "", "");
                    }
                }
                catch (Exception e) {
                    logger.error("Failed to check Approval Rule for DataSet-->" + e.getMessage());
                }
                finally {
                    database.closeStatement("aprules");
                }
                if (asyncronous && propsWithAPRules.size() > 0) {
                    ApprovalRuleUtil.setDatasetStatusToPendingAutoApprEval(propsWithAPRules, connectionInfo);
                    AutomationService ac = new AutomationService(new SapphireConnection(database.getConnection(), connectionInfo));
                    try {
                        ac.addToDoListEntry(null, "CheckDSApprovalRule", "1", propsWithAPRules, null, true, connectionInfo.getSysuserId(), "", "", "");
                    }
                    catch (Exception e) {
                        logger.error("Failed to add CheckDSApprovalRule action to ToDoList" + e.getMessage());
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void checkSDIAutoApprovalRule(DataSet primary, PropertyList actionProps, String sdcid, String statuscolumn, String pendingApprovalStatus, String approvedStatus, String approvedbycolumn, String approveddtcolumn, DBAccess database, ConnectionInfo connectionInfo, Logger logger, SDCProcessor sdcProcessor, boolean asyncronous) throws SapphireException {
        isOracle = database.isOracle();
        if (!actionProps.getProperty(SKIPAPPROVALRULECHECK, "").equalsIgnoreCase("Y") && !actionProps.getProperty("modtool", "").equalsIgnoreCase("UndoSDIColumnValue")) {
            ActionProcessor ap = new ActionProcessor(connectionInfo.getConnectionId());
            PropertyList sdcdetails = sdcProcessor.getPropertyList(sdcid);
            PreparedStatement apRulesStmnt = database.prepareStatement("aprules", ApprovalRuleUtil.getSDIAPRulesSQL());
            PreparedStatement apStepsStmnt = database.prepareStatement("apsteps", ApprovalRuleUtil.getSDIAPStepsSQL(sdcid));
            PropertyList propsWithAPRules = new PropertyList();
            PropertyList nonAutoApprovedSDIProps = new PropertyList();
            DataSet nonAutoApprovedSteps = new DataSet();
            DataSet autoApprovedSteps = new DataSet();
            DataSet sdiApprovalDS = new DataSet();
            ApprovalRuleUtil.populateColumns(nonAutoApprovedSteps, false, false);
            ApprovalRuleUtil.populateColumns(autoApprovedSteps, false, true);
            ApprovalRuleUtil.populateSDIApprovalColumns(sdiApprovalDS);
            PropertyList policy = new ConfigurationProcessor(connectionInfo.getConnectionId()).getPolicy("BatchSamplePolicy", "Sapphire Custom");
            QueryProcessor qp = new QueryProcessor(connectionInfo.getConnectionId());
            try {
                for (int row = 0; row < primary.size(); ++row) {
                    if ("Y".equalsIgnoreCase(primary.getString(row, SKIPAPPROVALRULECHECK, "N"))) continue;
                    String status = primary.getString(row, statuscolumn, "");
                    if (status.equalsIgnoreCase(pendingApprovalStatus)) {
                        String keyid1 = ApprovalRuleUtil.getKeyidValue(primary, row, sdcdetails, "keycolid1");
                        String keyid2 = ApprovalRuleUtil.getKeyidValue(primary, row, sdcdetails, "keycolid2");
                        String keyid3 = ApprovalRuleUtil.getKeyidValue(primary, row, sdcdetails, "keycolid3");
                        if (ApprovalRuleUtil.blockAutoApprovalRuleCheckAsPerPolicy(sdcid, keyid1, policy, qp)) continue;
                        apRulesStmnt.setString(1, sdcid);
                        apRulesStmnt.setString(2, keyid1);
                        apRulesStmnt.setString(3, keyid2);
                        apRulesStmnt.setString(4, keyid3);
                        DataSet apRulesDS = new DataSet(apRulesStmnt.executeQuery());
                        if (apRulesDS.isEmpty()) continue;
                        if (asyncronous) {
                            ApprovalRuleUtil.populateProps(propsWithAPRules, sdcid, keyid1, keyid2, keyid3, statuscolumn, pendingApprovalStatus, approvedStatus, approvedbycolumn, approveddtcolumn);
                            continue;
                        }
                        apRulesDS.setString(-1, APPROVALRULESTATUS, "");
                        SDIApprovalInfo sdiApprovalInfo = new SDIApprovalInfo(sdcid, keyid1, keyid2, keyid3, statuscolumn, pendingApprovalStatus, approvedStatus, approvedbycolumn, approveddtcolumn);
                        ApprovalRuleUtil.evaluateSDIApprovalRules(sdiApprovalInfo, apRulesDS, apStepsStmnt, nonAutoApprovedSteps, autoApprovedSteps, sdiApprovalDS, nonAutoApprovedSDIProps, null, primary, row, connectionInfo);
                        continue;
                    }
                    if (!status.equalsIgnoreCase(STATUS_PENDINGAP)) continue;
                    primary.setString(row, statuscolumn, pendingApprovalStatus);
                }
                ApprovalRuleUtil.clearSkipCheckApprovalRuleProp(primary);
            }
            catch (Exception e) {
                logger.error("Failed to check Approval Rule-->" + e.getMessage());
            }
            finally {
                database.closeStatement("aprules");
                database.closeStatement("apsteps");
            }
            if (asyncronous && propsWithAPRules.size() > 0) {
                propsWithAPRules.setProperty("statuscolumn", statuscolumn);
                propsWithAPRules.setProperty("approvedstatus", approvedStatus);
                propsWithAPRules.setProperty("pendingapprovalstatus", pendingApprovalStatus);
                propsWithAPRules.setProperty("approvedbycolumn", approvedbycolumn);
                propsWithAPRules.setProperty("approveddtcolumn", approveddtcolumn);
                ApprovalRuleUtil.setSDIStatusToPendingAutoApprEval(propsWithAPRules, connectionInfo);
                AutomationService ac = new AutomationService(new SapphireConnection(database.getConnection(), connectionInfo));
                try {
                    ac.addToDoListEntry(null, "CheckSDIApprovalRule", "1", propsWithAPRules, null, true, connectionInfo.getSysuserId(), "", "", "");
                }
                catch (Exception e) {
                    logger.error("Failed to add CheckDSApprovalRule action to ToDoList" + e.getMessage());
                }
            } else {
                if (nonAutoApprovedSteps.getRowCount() > 0) {
                    ApprovalRuleUtil.updateSDIApprovalSteps(database, nonAutoApprovedSteps);
                }
                if (autoApprovedSteps.getRowCount() > 0) {
                    ApprovalRuleUtil.updateSDIApprovalSteps(database, autoApprovedSteps);
                }
                if (sdiApprovalDS.getRowCount() > 0) {
                    ApprovalRuleUtil.updateSDIApproval(database, sdiApprovalDS);
                }
            }
        }
    }

    public static void populateProps(PropertyList props, String sdcid, String keyid1, String keyid2, String keyid3) {
        ApprovalRuleUtil.populateProps(props, sdcid, keyid1, keyid2, keyid3, "", "", "", "", "", "", "", "", "");
    }

    public static void populateProps(PropertyList props, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset) {
        ApprovalRuleUtil.populateProps(props, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, "", "", "", "", "");
    }

    public static void populateProps(PropertyList props, String sdcid, String keyid1, String keyid2, String keyid3, String statuscolumn, String status, String approvedstatus, String approvedbycolumn, String approveddtcolumn) {
        ApprovalRuleUtil.populateProps(props, sdcid, keyid1, keyid2, keyid3, "", "", "", "", statuscolumn, status, approvedstatus, approvedbycolumn, approveddtcolumn);
    }

    public static void populateProps(PropertyList props, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String statuscolumn, String status, String approvedstatus, String approvedbycolumn, String approveddtcolumn) {
        if (props.size() == 0) {
            props.setProperty(PROPERTY_SDCID, sdcid);
            props.setProperty(PROPERTY_KEYID1, keyid1);
            props.setProperty(PROPERTY_KEYID2, keyid2);
            props.setProperty(PROPERTY_KEYID3, keyid3);
            if (paramlistid.length() > 0) {
                props.setProperty(PROPERTY_PARAMLISTID, paramlistid);
                props.setProperty(PROPERTY_PARAMLISTVERSIONID, paramlistversionid);
                props.setProperty(PROPERTY_VARIANTID, variantid);
                props.setProperty(PROPERTY_DATASET, dataset);
            }
            if (statuscolumn.trim().length() > 0) {
                props.setProperty(statuscolumn, status);
                if (status.equalsIgnoreCase(approvedstatus)) {
                    props.setProperty(approvedbycolumn, APPROVEDBY_AUTOAP);
                    props.setProperty(approveddtcolumn, "n");
                    if (paramlistid.length() > 0) {
                        props.setProperty("completedby", APPROVEDBY_AUTOAP);
                        props.setProperty("completeddt", "n");
                        props.setProperty(PROPERTY_APPROVALFLAG, APPROVALRULESTATUS_PASS);
                    }
                }
            }
        } else {
            props.setProperty(PROPERTY_KEYID1, props.getProperty(PROPERTY_KEYID1) + ";" + keyid1);
            props.setProperty(PROPERTY_KEYID2, props.getProperty(PROPERTY_KEYID2) + ";" + keyid2);
            props.setProperty(PROPERTY_KEYID3, props.getProperty(PROPERTY_KEYID3) + ";" + keyid3);
            if (paramlistid.length() > 0) {
                props.setProperty(PROPERTY_PARAMLISTID, props.getProperty(PROPERTY_PARAMLISTID) + ";" + paramlistid);
                props.setProperty(PROPERTY_PARAMLISTVERSIONID, props.getProperty(PROPERTY_PARAMLISTVERSIONID) + ";" + paramlistversionid);
                props.setProperty(PROPERTY_VARIANTID, props.getProperty(PROPERTY_VARIANTID) + ";" + variantid);
                props.setProperty(PROPERTY_DATASET, props.getProperty(PROPERTY_DATASET) + ";" + dataset);
            }
            if (statuscolumn.trim().length() > 0) {
                props.setProperty(statuscolumn, props.getProperty(statuscolumn) + ";" + status);
                if (status.equalsIgnoreCase(approvedstatus)) {
                    props.setProperty(approvedbycolumn, props.getProperty(approvedbycolumn) + ";" + APPROVEDBY_AUTOAP);
                    props.setProperty(approveddtcolumn, props.getProperty(approveddtcolumn) + ";n");
                    if (paramlistid.length() > 0) {
                        props.setProperty("completedby", props.getProperty("completedby") + ";" + APPROVEDBY_AUTOAP);
                        props.setProperty("completeddt", props.getProperty("completeddt") + ";n");
                        props.setProperty(PROPERTY_APPROVALFLAG, props.getProperty(PROPERTY_APPROVALFLAG) + ";P");
                    }
                }
            }
        }
    }

    public static void evaluateAPRules(DataSet apRulesDS, PropertyList primarykeyProps, QueryProcessor qp, boolean isDataSet) {
        DataSet sdidatas = null;
        for (int rowid = 0; rowid < apRulesDS.size(); ++rowid) {
            String rulename = apRulesDS.getString(rowid, PROPERTY_RULENAME, "");
            if (isDataSet) {
                apRulesDS.setString(rowid, APPROVALRULESTATUS, ApprovalRuleUtil.resolveDSRule(apRulesDS, rowid, primarykeyProps, qp));
                continue;
            }
            if (ApprovalRuleUtil.isDataSetRule(rulename)) {
                if (sdidatas == null) {
                    sdidatas = ApprovalRuleUtil.getValidDataSet(primarykeyProps, qp);
                }
                PropertyList props = new PropertyList();
                props.setProperty(PROPERTY_SDCID, SDC_SAMPLE);
                props.setProperty(PROPERTY_KEYID1, sdidatas.getColumnValues(PROPERTY_KEYID1, ";"));
                props.setProperty(PROPERTY_KEYID2, sdidatas.getColumnValues(PROPERTY_KEYID2, ";"));
                props.setProperty(PROPERTY_KEYID3, sdidatas.getColumnValues(PROPERTY_KEYID3, ";"));
                props.setProperty(PROPERTY_PARAMLISTID, sdidatas.getColumnValues(PROPERTY_PARAMLISTID, ";"));
                props.setProperty(PROPERTY_PARAMLISTVERSIONID, sdidatas.getColumnValues(PROPERTY_PARAMLISTVERSIONID, ";"));
                props.setProperty(PROPERTY_VARIANTID, sdidatas.getColumnValues(PROPERTY_VARIANTID, ";"));
                props.setProperty(PROPERTY_DATASET, sdidatas.getColumnValues(PROPERTY_DATASET, ";"));
                apRulesDS.setString(rowid, APPROVALRULESTATUS, ApprovalRuleUtil.resolveDSRule(apRulesDS, rowid, props, qp));
                continue;
            }
            if (sdidatas == null) {
                sdidatas = ApprovalRuleUtil.getValidDataSet(primarykeyProps, qp);
            }
            if (rulename.equalsIgnoreCase(RULE_DATASETREJECTED)) {
                if (sdidatas.findRow(PROPERTY_APPROVALFLAG, APPROVALRULESTATUS_FAIL) > -1) {
                    apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_FAIL);
                    continue;
                }
                apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_PASS);
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_DATASETMANUALLYAPPROVED)) {
                if (ApprovalRuleUtil.isManaullyApprovedDSFound(sdidatas)) {
                    apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_FAIL);
                    continue;
                }
                apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_PASS);
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_DATASETAUTOAPPROVED)) {
                if (sdidatas.findRow(PROPERTY_APPROVEDBY, APPROVEDBY_AUTOAP) > -1) {
                    apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_FAIL);
                    continue;
                }
                apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_PASS);
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_DATASETCANCELLED)) {
                if (ApprovalRuleUtil.getValidDataSet(primarykeyProps, qp, true).getRowCount() > 0) {
                    apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_FAIL);
                    continue;
                }
                apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_PASS);
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_SAMPLEREJECTED)) {
                if (ApprovalRuleUtil.getRejectedSample(primarykeyProps, qp).getRowCount() > 0) {
                    apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_FAIL);
                    continue;
                }
                apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_PASS);
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_SAMPLEMANUALLYREVIEWED)) {
                apRulesDS.setString(rowid, APPROVALRULESTATUS, ApprovalRuleUtil.resolveAutoOrManuallyReviewedSampleRule(primarykeyProps, qp, false));
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_SAMPLEAUTOREVIEWED)) {
                apRulesDS.setString(rowid, APPROVALRULESTATUS, ApprovalRuleUtil.resolveAutoOrManuallyReviewedSampleRule(primarykeyProps, qp, true));
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_BATCHSTAGEREJECTED)) {
                if (ApprovalRuleUtil.getRejectedBatchStage(primarykeyProps, qp).getRowCount() > 0) {
                    apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_FAIL);
                    continue;
                }
                apRulesDS.setString(rowid, APPROVALRULESTATUS, APPROVALRULESTATUS_PASS);
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_BATCHSTAGEMANUALLYAPPROVED)) {
                apRulesDS.setString(rowid, APPROVALRULESTATUS, ApprovalRuleUtil.resolveAutoOrManuallyReleaseBSRule(primarykeyProps, qp, false));
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_BATCHSTAGEAUTOAPPROVED)) {
                apRulesDS.setString(rowid, APPROVALRULESTATUS, ApprovalRuleUtil.resolveAutoOrManuallyReleaseBSRule(primarykeyProps, qp, true));
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_INCIDENTFOUND)) {
                apRulesDS.setString(rowid, APPROVALRULESTATUS, ApprovalRuleUtil.getIncidentResult(primarykeyProps, qp, false));
                continue;
            }
            if (rulename.equalsIgnoreCase(RULE_OPENINCIDENTFOUND)) {
                apRulesDS.setString(rowid, APPROVALRULESTATUS, ApprovalRuleUtil.getIncidentResult(primarykeyProps, qp, true));
                continue;
            }
            apRulesDS.setString(rowid, APPROVALRULESTATUS, ApprovalRuleUtil.resolveDSRule(apRulesDS, rowid, primarykeyProps, qp));
        }
    }

    public static boolean isDataSetRule(String rulename) {
        return dsRuleList.contains(rulename);
    }

    public static DataSet getValidDataSet(PropertyList primarykeyProps, QueryProcessor qp) {
        return ApprovalRuleUtil.getValidDataSet(primarykeyProps, qp, false);
    }

    public static DataSet getValidDataSet(PropertyList primarykeyProps, QueryProcessor qp, boolean cancelleddataset) {
        String sdcid = primarykeyProps.getProperty(PROPERTY_SDCID);
        String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1);
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder("select * from sdidata,s_sample where sdidata.sdcid='Sample' and sdidata.keyid1=s_sample.s_sampleid ");
        if (sdcid.equalsIgnoreCase(SDC_SAMPLE)) {
            sql.append(" and sdidata.keyid1=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_BATCH)) {
            sql.append(" and s_sample.batchid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_BATCHSTAGE)) {
            sql.append(" and s_sample.batchstageid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_REQUEST)) {
            sql.append(" and s_sample.requestid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_MONITORGROUP)) {
            sql.append(" and s_sample.monitorgroupid=").append(safeSQL.addVar(keyid1));
        } else {
            return new DataSet();
        }
        if (cancelleddataset) {
            sql.append(" and sdidata.s_datasetstatus='Cancelled'");
        } else {
            sql.append(" and sdidata.s_datasetstatus!='Cancelled'");
        }
        sql.append(" and s_sample.samplestatus!='Cancelled'");
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    public static DataSet getRejectedSample(PropertyList primarykeyProps, QueryProcessor qp) {
        String sdcid = primarykeyProps.getProperty(PROPERTY_SDCID);
        String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1);
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder("select s_sampleid from s_sample");
        sql.append(" where samplestatus='Reviewed'");
        sql.append(" and reviewdisposition='Rejected'");
        if (sdcid.equalsIgnoreCase(SDC_BATCH)) {
            sql.append(" and batchid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_BATCHSTAGE)) {
            sql.append(" and batchstageid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_REQUEST)) {
            sql.append(" and requestid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_MONITORGROUP)) {
            sql.append(" and monitorgroupid=").append(safeSQL.addVar(keyid1));
        } else {
            return new DataSet();
        }
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    public static String resolveAutoOrManuallyReviewedSampleRule(PropertyList primarykeyProps, QueryProcessor qp, boolean autoreviewed) {
        DataSet ds;
        String sdcid = primarykeyProps.getProperty(PROPERTY_SDCID);
        String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1);
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder("select s_sampleid,samplestatus,reviewedby from s_sample");
        sql.append(" where samplestatus='Reviewed'");
        boolean sdcEligible = true;
        if (sdcid.equalsIgnoreCase(SDC_BATCH)) {
            sql.append(" and batchid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_BATCHSTAGE)) {
            sql.append(" and batchstageid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_REQUEST)) {
            sql.append(" and requestid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_MONITORGROUP)) {
            sql.append(" and monitorgroupid=").append(safeSQL.addVar(keyid1));
        } else {
            sdcEligible = false;
        }
        String result = APPROVALRULESTATUS_PASS;
        if (sdcEligible && (ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())).getRowCount() > 0) {
            for (int r = 0; r < ds.getRowCount(); ++r) {
                String reviewedby = ds.getString(r, "reviewedby", "");
                if (autoreviewed && reviewedby.equals(APPROVEDBY_AUTOAP)) {
                    result = APPROVALRULESTATUS_FAIL;
                    break;
                }
                if (autoreviewed || reviewedby.length() <= 0 || reviewedby.equals(APPROVEDBY_AUTOAP)) continue;
                result = APPROVALRULESTATUS_FAIL;
                break;
            }
        }
        return result;
    }

    public static String resolveAutoOrManuallyReleaseBSRule(PropertyList primarykeyProps, QueryProcessor qp, boolean autoreviewed) {
        String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1);
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder("select s_batchstageid,batchstagestatus,releasedby from s_batchstage");
        sql.append(" where batchid in (").append(safeSQL.addIn(keyid1)).append(")");
        sql.append(" and batchstagestatus='Released'");
        String result = APPROVALRULESTATUS_PASS;
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds.getRowCount() > 0) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("releasedby", APPROVEDBY_AUTOAP);
            DataSet autoApprovedDS = ds.getFilteredDataSet(hm);
            result = autoreviewed ? (autoApprovedDS.getRowCount() > 0 ? APPROVALRULESTATUS_FAIL : APPROVALRULESTATUS_PASS) : (autoApprovedDS.getRowCount() == ds.getRowCount() ? APPROVALRULESTATUS_PASS : APPROVALRULESTATUS_FAIL);
        }
        return result;
    }

    public static DataSet getRejectedBatchStage(PropertyList primarykeyProps, QueryProcessor qp) {
        String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1);
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder("select s_batchstageid from s_batchstage ");
        sql.append(" where batchid=").append(safeSQL.addVar(keyid1));
        sql.append(" and batchstagestatus='Rejected'");
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private static String getIncidentResult(PropertyList primarykeyProps, QueryProcessor qp, boolean onlyOpenIncident) {
        String result = APPROVALRULESTATUS_PASS;
        DataSet incidentDS = ApprovalRuleUtil.getIncidents(primarykeyProps, qp, onlyOpenIncident);
        if (incidentDS.getRowCount() > 0) {
            String incidentflagcount = incidentDS.getValue(0, "incidentflagcount", "");
            result = incidentflagcount.length() > 0 && Integer.parseInt(incidentflagcount) > 0 ? APPROVALRULESTATUS_FAIL : APPROVALRULESTATUS_PASS;
        }
        return result;
    }

    public static DataSet getIncidents(PropertyList primarykeyProps, QueryProcessor qp, boolean onlyOpenIncident) {
        String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1);
        String sdcid = primarykeyProps.getProperty(PROPERTY_SDCID);
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        if (sdcid.equalsIgnoreCase(SDC_SAMPLE)) {
            sql.append(" select ");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_SAMPLE, "ii.sourcekeyid1 = s_sample.s_sampleid", onlyOpenIncident));
            sql.append(" incidentflagcount from s_sample where s_sampleid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_BATCH)) {
            sql.append(" select ");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_BATCH, "ii.sourcekeyid1 = s_batch.s_batchid", onlyOpenIncident));
            sql.append("+");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_BATCHSTAGE, "ii.sourcekeyid1 in (select s_batchstageid from s_batchstage where s_batchstage.batchid=s_batch.s_batchid and s_batchstage.batchstagestatus!='Cancelled')", onlyOpenIncident));
            sql.append("+");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_SAMPLE, "ii.sourcekeyid1 in (select s_sampleid from s_sample where s_sample.batchid=s_batch.s_batchid and s_sample.samplestatus!='Cancelled')", onlyOpenIncident));
            sql.append(" incidentflagcount from s_batch where s_batchid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_BATCHSTAGE)) {
            sql.append(" select ");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_BATCHSTAGE, "ii.sourcekeyid1 = s_batchstage.s_batchstageid", onlyOpenIncident));
            sql.append("+");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_SAMPLE, "ii.sourcekeyid1 in (select s_sampleid from s_sample where s_sample.batchstageid=s_batchstage.s_batchstageid  and s_sample.samplestatus!='Cancelled')", onlyOpenIncident));
            sql.append(" incidentflagcount from s_batchstage where s_batchstageid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_REQUEST)) {
            sql.append(" select ");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_REQUEST, "ii.sourcekeyid1 = s_request.s_requestid", onlyOpenIncident));
            sql.append("+");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_SAMPLE, "ii.sourcekeyid1 in (select s_sampleid from s_sample where s_sample.requestid=s_request.s_requestid  and s_sample.samplestatus!='Cancelled')", onlyOpenIncident));
            sql.append(" incidentflagcount from s_request where s_requestid=").append(safeSQL.addVar(keyid1));
        } else if (sdcid.equalsIgnoreCase(SDC_MONITORGROUP)) {
            sql.append(" select ");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_MONITORGROUP, "ii.sourcekeyid1 = monitorgroup.monitorgroupid", onlyOpenIncident));
            sql.append("+");
            sql.append(ApprovalRuleUtil.getIncidentSubSQL(SDC_SAMPLE, "ii.sourcekeyid1 in (select s_sampleid from s_sample where s_sample.monitorgroupid=monitorgroup.monitorgroupid  and s_sample.samplestatus!='Cancelled')", onlyOpenIncident));
            sql.append(" incidentflagcount from monitorgroup where monitorgroupid=").append(safeSQL.addVar(keyid1));
        } else {
            return new DataSet();
        }
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private static String getIncidentSubSQL(String sdcid, String wherecluase, boolean onlyOpenIncident) {
        return "(SELECT Count (DISTINCT ii.sourcekeyid1)  FROM incident i, incidentitem ii WHERE ii.incidentid = i.incidentid  AND ii.sourcesdcid='" + sdcid + "'" + (wherecluase.length() > 0 ? " AND " + wherecluase : "") + (onlyOpenIncident ? " AND i.incidentstatus not in('Closed','Cancelled')" : "") + ")";
    }

    public static String resolveDSRule(DataSet apRulesDS, int rowid, PropertyList primarykeyProps, QueryProcessor qp) {
        String rulename = apRulesDS.getString(rowid, PROPERTY_RULENAME, "");
        String result = APPROVALRULESTATUS_UNRESOLVE;
        DataSet ds = null;
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        if (rulename.equalsIgnoreCase(RULE_MULTIPLEANALYST)) {
            sql.append("select ").append(ApprovalRuleUtil.getSelectClause(TABLE_SDIDATAITEM)).append(" from sdidataitem ");
            sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, TABLE_SDIDATAITEM, primarykeyProps, true));
            sql.append(" group by sdcid,keyid1,keyid2,keyid3,paramlistid,paramlistversionid,variantid,dataset having count(distinct s_analystid)>1");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else if (rulename.equalsIgnoreCase(RULE_OVERRIDDENCERTIFICATION)) {
            sql.append("select ").append(ApprovalRuleUtil.getSelectClause(TABLE_SDIDATAITEM)).append(" from sdidataitem ");
            sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, TABLE_SDIDATAITEM, primarykeyProps, true));
            sql.append(" and sdidataitem.s_acoverriddenflag='Y'");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else if (rulename.equalsIgnoreCase(RULE_UNASSIGNEDANLYST)) {
            sql.append("select ").append(ApprovalRuleUtil.getSelectClause(TABLE_SDIDATA)).append(" from sdidata,sdidataitem ");
            sql.append(" where sdidata.sdcid=sdidataitem.sdcid");
            sql.append(" and sdidata.keyid1=sdidataitem.keyid1");
            sql.append(" and sdidata.keyid2=sdidataitem.keyid2");
            sql.append(" and sdidata.keyid3=sdidataitem.keyid3");
            sql.append(" and sdidata.paramlistid=sdidataitem.paramlistid");
            sql.append(" and sdidata.paramlistversionid=sdidataitem.paramlistversionid");
            sql.append(" and sdidata.variantid=sdidataitem.variantid");
            sql.append(" and sdidata.dataset=sdidataitem.dataset");
            sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, TABLE_SDIDATA, primarykeyProps, false));
            sql.append(" and sdidata.s_assignedanalyst!=sdidataitem.s_analystid");
            sql.append(" and sdidata.s_assignedanalyst is not null");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else if (rulename.equalsIgnoreCase(RULE_PASTDUEINSTRUMENT)) {
            sql.append("select ").append(ApprovalRuleUtil.getSelectClause(TABLE_SDIDATA)).append(" from sdidata ");
            sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, TABLE_SDIDATA, primarykeyProps, true));
            sql.append(" and sdidata.s_icoverriddenflag='Y'");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        } else if (rulename.equalsIgnoreCase(RULE_EXPIREDCONSUMABLE)) {
            ds = ApprovalRuleUtil.getUsedExriredConsumableDS(primarykeyProps, qp);
        } else {
            if (rulename.equalsIgnoreCase(RULE_RESULTFAILED)) {
                return ApprovalRuleUtil.resolvedSpecResultCondition(primarykeyProps, qp, "Fail");
            }
            if (rulename.equalsIgnoreCase(RULE_RESULTWARNING)) {
                return ApprovalRuleUtil.resolvedSpecResultCondition(primarykeyProps, qp, "Warning");
            }
            if (rulename.equalsIgnoreCase(RULE_RESULTMODIFIED)) {
                ds = ApprovalRuleUtil.getModifiedResultDS(primarykeyProps, qp);
            } else {
                if (rulename.equalsIgnoreCase(RULE_RESULTINSTRUMENTMODIFIED)) {
                    return ApprovalRuleUtil.resolveRuleForUnSavedResult(primarykeyProps, qp, true);
                }
                if (rulename.equalsIgnoreCase(RULE_RESULTDYNAMICAUDITRECORD)) {
                    return ApprovalRuleUtil.resolveRuleForUnSavedResult(primarykeyProps, qp, false);
                }
                if (rulename.equalsIgnoreCase(RULE_QCBTACHNOTREVIEWED)) {
                    sql.append("select sdidata.sdcid,sdidata.keyid1,sdidata.keyid2,sdidata.keyid3, sdidata.paramlistid,sdidata.paramlistversionid,sdidata.variantid, sdidata.dataset,s_qcbatch.qcbatchstatus from sdidata,s_sample,s_qcbatch ");
                    sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, TABLE_SDIDATA, primarykeyProps, true));
                    sql.append(" and s_sample.s_sampleid=sdidata.keyid1");
                    sql.append(" and s_qcbatch.s_qcbatchid=sdidata.s_qcbatchid");
                    sql.append(" and s_sample.qcsampletype='Unknown'");
                    ds = ApprovalRuleUtil.filterSelectedDataSets(qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues()), primarykeyProps);
                    result = ds.isEmpty() ? APPROVALRULESTATUS_PASS : (ds.getString(0, "qcbatchstatus", "").equalsIgnoreCase("Reviewed") ? APPROVALRULESTATUS_PASS : APPROVALRULESTATUS_FAIL);
                } else {
                    if (rulename.equalsIgnoreCase(RULE_DATASETRETEST)) {
                        return ApprovalRuleUtil.resolvedReTestOrCancelledDS(primarykeyProps, qp, false);
                    }
                    if (rulename.equalsIgnoreCase(RULE_DATASETCANCELLED)) {
                        return ApprovalRuleUtil.resolvedReTestOrCancelledDS(primarykeyProps, qp, true);
                    }
                    if (rulename.equalsIgnoreCase(RULE_REPLICATEADDED)) {
                        sql.append("select di.sdcid,di.keyid1,di.paramlistid,di.paramlistversionid,di.variantid,di.dataset,di.paramid,di.paramtype from sdidataitem di, paramlistitem pli ");
                        sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, "di", primarykeyProps, true));
                        sql.append(" and di.paramlistid= pli.paramlistid");
                        sql.append(" and di.paramlistversionid= pli.paramlistversionid");
                        sql.append(" and di.variantid= pli.variantid");
                        sql.append(" and di.paramid= pli.paramid");
                        sql.append(" and di.paramtype= pli.paramtype");
                        sql.append(" and di.replicateid > pli.numreplicates");
                        ds = ApprovalRuleUtil.filterSelectedDataSets(qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues()), primarykeyProps);
                        result = ds.isEmpty() ? APPROVALRULESTATUS_PASS : APPROVALRULESTATUS_FAIL;
                    } else {
                        String sqltext = apRulesDS.getString(rowid, PROPERTY_SQLTEXT, "");
                        String passrule = apRulesDS.getString(rowid, PROPERTY_PASSRULE, "Row>=1");
                        if (!sqltext.isEmpty() && (ds = qp.getPreparedSqlDataSet(sqltext = ApprovalRuleUtil.resolveSubstitutions(sqltext, primarykeyProps), safeSQL.getValues())) != null) {
                            if (passrule.equalsIgnoreCase("Row>=1")) {
                                result = ds.getRowCount() >= 1 ? APPROVALRULESTATUS_FAIL : APPROVALRULESTATUS_PASS;
                            } else if (passrule.equalsIgnoreCase("Count>=1") && ds.getRowCount() > 0) {
                                String count = ds.getValue(0, "count", "");
                                result = count.length() > 0 && Integer.parseInt(count) >= 1 ? APPROVALRULESTATUS_FAIL : APPROVALRULESTATUS_PASS;
                            }
                        }
                    }
                }
            }
        }
        if (result.equalsIgnoreCase(APPROVALRULESTATUS_UNRESOLVE) && ds != null) {
            result = (ds = ApprovalRuleUtil.filterSelectedDataSets(ds, primarykeyProps)).getRowCount() > 0 ? APPROVALRULESTATUS_FAIL : APPROVALRULESTATUS_PASS;
        }
        return result;
    }

    private static String resolvedSpecResultCondition(PropertyList primarykeyProps, QueryProcessor qp, String specconditon) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(ApprovalRuleUtil.getSelectClause("sdidataitemspec")).append(",sdidataitemspec.condition from sdidataitemspec ");
        sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, "sdidataitemspec", primarykeyProps, true));
        DataSet ds = ApprovalRuleUtil.filterSelectedDataSets(qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues()), primarykeyProps);
        if (ds.getRowCount() > 0) {
            boolean specConditionFound = false;
            for (int indx = 0; indx < ds.getRowCount(); ++indx) {
                String condition = ds.getString(indx, "condition", "");
                if (!condition.equalsIgnoreCase(specconditon)) continue;
                specConditionFound = true;
                break;
            }
            if (specConditionFound) {
                return APPROVALRULESTATUS_FAIL;
            }
            return APPROVALRULESTATUS_PASS;
        }
        return APPROVALRULESTATUS_PASS;
    }

    private static DataSet getModifiedResultDS(PropertyList primarykeyProps, QueryProcessor qp) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(ApprovalRuleUtil.getSelectClause(TABLE_SDIDATAITEM)).append(",sdidataitem.enteredvalue,a_sdidataitem.enteredvalue from sdidataitem,a_sdidataitem ");
        sql.append(" where sdidataitem.sdcid=a_sdidataitem.sdcid ");
        sql.append(" and sdidataitem.keyid1=a_sdidataitem.keyid1 ");
        sql.append(" and sdidataitem.paramlistid=a_sdidataitem.paramlistid ");
        sql.append(" and sdidataitem.paramlistversionid=a_sdidataitem.paramlistversionid ");
        sql.append(" and sdidataitem.variantid=a_sdidataitem.variantid ");
        sql.append(" and sdidataitem.dataset=a_sdidataitem.dataset ");
        sql.append(" and sdidataitem.paramid=a_sdidataitem.paramid ");
        sql.append(" and sdidataitem.paramtype=a_sdidataitem.paramtype ");
        sql.append(" and sdidataitem.replicateid=a_sdidataitem.replicateid ");
        sql.append(" and sdidataitem.enteredvalue!=a_sdidataitem.enteredvalue ");
        sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, TABLE_SDIDATAITEM, primarykeyProps, false));
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private static String resolveRuleForUnSavedResult(PropertyList primarykeyProps, QueryProcessor qp, boolean isInstrumentResult) {
        String result = APPROVALRULESTATUS_PASS;
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        String sdcid = primarykeyProps.getProperty(PROPERTY_SDCID);
        String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1, "");
        String keyid2 = primarykeyProps.getProperty(PROPERTY_KEYID2, "");
        String keyid3 = primarykeyProps.getProperty(PROPERTY_KEYID3, "");
        String paramlistid = primarykeyProps.getProperty(PROPERTY_PARAMLISTID, "");
        String paramlistversionid = primarykeyProps.getProperty(PROPERTY_PARAMLISTVERSIONID, "");
        String variantid = primarykeyProps.getProperty(PROPERTY_VARIANTID, "");
        String dataset = primarykeyProps.getProperty(PROPERTY_DATASET, "");
        String detailkeyvalues = ApprovalRuleUtil.concatFields("a_sdidataitem.paramlistid", "a_sdidataitem.paramlistversionid", "a_sdidataitem.variantid", "a_sdidataitem.dataset", "a_sdidataitem.paramid", "a_sdidataitem.paramtype", "a_sdidataitem.replicateid");
        sql.append("select a_sdidataitem.sdcid,a_sdidataitem.keyid1,a_sdidataitem.keyid2,a_sdidataitem.keyid3, ");
        sql.append(" a_sdidataitem.paramlistid,a_sdidataitem.paramlistversionid,a_sdidataitem.variantid,a_sdidataitem.dataset, ");
        sql.append(" activitylog.detailkeyvalues,activitylog.columnid,activitylog.activitygroup from a_sdidataitem,activitylog ");
        sql.append(" where a_sdidataitem.sdcid=").append(safeSQL.addVar(sdcid));
        sql.append(" and a_sdidataitem.keyid1 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(keyid1))).append(")");
        sql.append(" and a_sdidataitem.keyid2 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(keyid2))).append(")");
        sql.append(" and a_sdidataitem.keyid3 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(keyid3))).append(")");
        sql.append(" and a_sdidataitem.paramlistid in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(paramlistid))).append(")");
        sql.append(" and a_sdidataitem.paramlistversionid in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(paramlistversionid))).append(")");
        sql.append(" and a_sdidataitem.variantid in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(variantid))).append(")");
        sql.append(" and a_sdidataitem.dataset in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(dataset))).append(")");
        sql.append(" and activitylog.keyid1=a_sdidataitem.keyid1");
        sql.append(" and activitylog.keyid2=a_sdidataitem.keyid2");
        sql.append(" and activitylog.keyid3=a_sdidataitem.keyid3");
        sql.append(" and activitylog.detailtableid='sdidataitem'");
        sql.append(" and activitylog.detailkeyvalues =").append(detailkeyvalues);
        sql.append(" and activitylog.newvalue!=a_sdidataitem.enteredtext");
        DataSet ds = ApprovalRuleUtil.filterSelectedDataSets(qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues()), primarykeyProps);
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("columnid", "enteredtext");
        DataSet filteredDS = ds.getFilteredDataSet(hm);
        hm.clear();
        for (int i = 0; i < filteredDS.getRowCount(); ++i) {
            String activitygroup = filteredDS.getString(i, "activitygroup");
            hm.clear();
            hm.put("activitygroup", activitygroup);
            hm.put("columnid", "instrumentid");
            if (isInstrumentResult) {
                if (ds.findRow(hm) <= -1) continue;
                result = APPROVALRULESTATUS_FAIL;
                break;
            }
            if (ds.findRow(hm) >= 0) continue;
            result = APPROVALRULESTATUS_FAIL;
            break;
        }
        return result;
    }

    private static String concatFields(String ... fields) {
        String cFNS = "{fn concat(";
        String cFNE = ")}";
        String specialDelimer = ";";
        String str = "";
        boolean firstItem = true;
        for (String f : fields) {
            if (firstItem) {
                str = isOracle ? f : "cast(" + f + " as nvarchar(100))";
                firstItem = false;
                continue;
            }
            str = cFNS + cFNS + str + ",'" + specialDelimer + "'" + cFNE + "," + (isOracle ? f : "cast(" + f + " as nvarchar(100))") + cFNE;
        }
        return str;
    }

    private static String resolveSubstitutions(String value, PropertyList primarykeyProps) {
        String startToken = "[";
        String endToken = "]";
        if (value != null && value.length() > 0 && value.contains(startToken) && value.contains(endToken)) {
            String[] tokens = StringUtil.getTokens(value, startToken, endToken);
            if (tokens.length > 0) {
                for (String token : tokens) {
                    String replaceWith = primarykeyProps.getProperty(token);
                    if (replaceWith == null) continue;
                    value = StringUtil.replaceAll(value, startToken + token + endToken, replaceWith);
                }
            }
            return value;
        }
        return value;
    }

    private static DataSet getUsedExriredConsumableDS(PropertyList primarykeyProps, QueryProcessor qp) {
        String sdcid = primarykeyProps.getProperty(PROPERTY_SDCID);
        String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1, "");
        String keyid2 = primarykeyProps.getProperty(PROPERTY_KEYID2, "");
        String keyid3 = primarykeyProps.getProperty(PROPERTY_KEYID3, "");
        String paramlistid = primarykeyProps.getProperty(PROPERTY_PARAMLISTID, "");
        String paramlistversionid = primarykeyProps.getProperty(PROPERTY_PARAMLISTVERSIONID, "");
        String variantid = primarykeyProps.getProperty(PROPERTY_VARIANTID, "");
        String dataset = primarykeyProps.getProperty(PROPERTY_DATASET, "");
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(ApprovalRuleUtil.getSelectClause("sdidatarelation"));
        sql.append(" FROM sdidatarelation  ");
        sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, "sdidatarelation", primarykeyProps, true));
        sql.append(" and sdidatarelation.usedexpiredconsumableflag='Y'");
        sql.append(" UNION ALL ");
        sql.append("SELECT ").append(ApprovalRuleUtil.getSelectClause("sdiworkitemitem"));
        sql.append(" FROM sdiworkitemrelation,sdiworkitemitem ");
        sql.append(" where sdiworkitemrelation.sdcid = sdiworkitemitem.sdcid");
        sql.append(" AND sdiworkitemrelation.keyid1 = sdiworkitemitem.keyid1");
        sql.append(" AND sdiworkitemrelation.keyid2 = sdiworkitemitem.keyid2 ");
        sql.append(" AND sdiworkitemrelation.keyid3 = sdiworkitemitem.keyid3 ");
        sql.append(" AND sdiworkitemrelation.workitemid = sdiworkitemitem.workitemid");
        sql.append(" AND sdiworkitemrelation.workiteminstance = sdiworkitemitem.workiteminstance ");
        sql.append(" AND sdiworkitemrelation.relationfunction = 'Reagent'");
        sql.append(" and sdiworkitemrelation.usedexpiredconsumableflag='Y'");
        sql.append(" and sdiworkitemitem.sdcid=").append(safeSQL.addVar(sdcid));
        sql.append(" and sdiworkitemitem.keyid1 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(keyid1))).append(")");
        sql.append(" and sdiworkitemitem.keyid2 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(keyid2))).append(")");
        sql.append(" and sdiworkitemitem.keyid3 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(keyid3))).append(")");
        sql.append(" and sdiworkitemitem.itemsdcid='ParamList'");
        sql.append(" and sdiworkitemitem.itemkeyid1 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(paramlistid))).append(")");
        sql.append(" and sdiworkitemitem.itemkeyid2 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(paramlistversionid))).append(")");
        sql.append(" and sdiworkitemitem.itemkeyid3 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(variantid))).append(")");
        sql.append(" and sdiworkitemitem.iteminstance in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(dataset))).append(")");
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    public static void populateColumns(DataSet ds, boolean isDataset, boolean autoApproved) {
        ds.addColumn(PROPERTY_SDCID, 0);
        ds.addColumn(PROPERTY_KEYID1, 0);
        ds.addColumn(PROPERTY_KEYID2, 0);
        ds.addColumn(PROPERTY_KEYID3, 0);
        if (isDataset) {
            ds.addColumn(PROPERTY_PARAMLISTID, 0);
            ds.addColumn(PROPERTY_PARAMLISTVERSIONID, 0);
            ds.addColumn(PROPERTY_VARIANTID, 0);
            ds.addColumn(PROPERTY_VARIANTID, 0);
            ds.addColumn(PROPERTY_DATASET, 0);
            ds.addColumn(PROPERTY_APPROVALSTEP, 0);
        } else {
            ds.addColumn(PROPERTY_APPROVALTYPEID, 0);
            ds.addColumn(PROPERTY_APPROVALSTEP, 0);
            ds.addColumn(PROPERTY_APPROVALSTEPINSTANCE, 0);
        }
        if (autoApproved) {
            ds.addColumn(PROPERTY_APPROVALFLAG, 0);
        } else {
            ds.addColumn(PROPERTY_RULENAME, 0);
            ds.addColumn(PROPERTY_NONAUTOAPPROVALREASON, 0);
        }
    }

    public static void populateSDIApprovalColumns(DataSet ds) {
        ds.addColumn(PROPERTY_SDCID, 0);
        ds.addColumn(PROPERTY_KEYID1, 0);
        ds.addColumn(PROPERTY_KEYID2, 0);
        ds.addColumn(PROPERTY_KEYID3, 0);
        ds.addColumn(PROPERTY_APPROVALTYPEID, 0);
        ds.addColumn(PROPERTY_APPROVALFLAG, 0);
    }

    public static void addToDS(DataSet ds, String sdcid, String keyid1, String keyid2, String keyid3, String approvaltypeid, String approvalstep, String approvalstepinstance, String rulename, String failuremessage) {
        ApprovalRuleUtil.addToDS(ds, sdcid, keyid1, keyid2, keyid3, "", "", "", "", approvaltypeid, approvalstep, approvalstepinstance, "", rulename, failuremessage, false);
    }

    public static void addToDS(DataSet ds, String sdcid, String keyid1, String keyid2, String keyid3, String approvaltypeid, String approvalstep, String approvalstepinstance, String approvalflag) {
        ApprovalRuleUtil.addToDS(ds, sdcid, keyid1, keyid2, keyid3, "", "", "", "", approvaltypeid, approvalstep, approvalstepinstance, approvalflag, "", "", false);
    }

    public static void addToDS(DataSet ds, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String approvalstep, String rulename, String failuremessage) {
        ApprovalRuleUtil.addToDS(ds, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, "", approvalstep, "", "", rulename, failuremessage, true);
    }

    public static void addToDS(DataSet ds, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String approvalstep, String approvalflag) {
        ApprovalRuleUtil.addToDS(ds, sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset, "", approvalstep, "", approvalflag, "", "", true);
    }

    public static void addToDS(DataSet ds, String sdcid, String keyid1, String keyid2, String keyid3, String paramlistid, String paramlistversionid, String variantid, String dataset, String approvaltypeid, String approvalstep, String approvalstepinstance, String approvalflag, String rulename, String failuremessage, boolean isDataSet) {
        int row = ds.addRow();
        ds.setValue(row, PROPERTY_SDCID, sdcid);
        ds.setValue(row, PROPERTY_KEYID1, keyid1);
        ds.setValue(row, PROPERTY_KEYID2, keyid2);
        ds.setValue(row, PROPERTY_KEYID3, keyid3);
        if (isDataSet) {
            ds.setValue(row, PROPERTY_PARAMLISTID, paramlistid);
            ds.setValue(row, PROPERTY_PARAMLISTVERSIONID, paramlistversionid);
            ds.setValue(row, PROPERTY_VARIANTID, variantid);
            ds.setValue(row, PROPERTY_DATASET, dataset);
            ds.setValue(row, PROPERTY_APPROVALSTEP, approvalstep);
        } else {
            ds.setValue(row, PROPERTY_APPROVALTYPEID, approvaltypeid);
            ds.setValue(row, PROPERTY_APPROVALSTEP, approvalstep);
            ds.setValue(row, PROPERTY_APPROVALSTEPINSTANCE, approvalstepinstance);
        }
        if (rulename.trim().length() > 0) {
            ds.setValue(row, PROPERTY_RULENAME, rulename);
            ds.setValue(row, PROPERTY_NONAUTOAPPROVALREASON, failuremessage);
        }
        if (approvalflag.trim().length() > 0) {
            ds.setValue(row, PROPERTY_APPROVALFLAG, approvalflag);
        }
    }

    public static void addToSDIApprovalDS(DataSet ds, String sdcid, String keyid1, String keyid2, String keyid3, String approvaltypeid, String approvalflag) {
        int row = ds.addRow();
        ds.setValue(row, PROPERTY_SDCID, sdcid);
        ds.setValue(row, PROPERTY_KEYID1, keyid1);
        ds.setValue(row, PROPERTY_KEYID2, keyid2);
        ds.setValue(row, PROPERTY_KEYID3, keyid3);
        ds.setValue(row, PROPERTY_APPROVALTYPEID, approvaltypeid);
        ds.setValue(row, PROPERTY_APPROVALFLAG, approvalflag);
    }

    public static void updateMap(Map<String, String> hm, String key, String value, String delimeter) {
        if (hm.containsKey(key)) {
            hm.put(key, hm.get(key) + delimeter + value);
        } else {
            hm.put(key, value);
        }
    }

    public static String[] getSteps(DataSet apStepsDS) {
        String[] arr = new String[apStepsDS.size()];
        for (int r = 0; r < apStepsDS.size(); ++r) {
            arr[r] = apStepsDS.getString(r, PROPERTY_APPROVALTYPEID, "") + "||" + apStepsDS.getString(r, PROPERTY_APPROVALSTEP, "");
        }
        return arr;
    }

    public static void updateSDIApprovalSteps(DBAccess database, DataSet ds) throws SapphireException {
        DataSetUtil.update(database, ds, "sdiapprovalstep", new String[]{PROPERTY_SDCID, PROPERTY_KEYID1, PROPERTY_KEYID2, PROPERTY_KEYID3, PROPERTY_APPROVALTYPEID, PROPERTY_APPROVALSTEP, PROPERTY_APPROVALSTEPINSTANCE});
    }

    public static void updateSDIDataApprovalSteps(DBAccess database, DataSet ds) throws SapphireException {
        DataSetUtil.update(database, ds, "sdidataapproval", new String[]{PROPERTY_SDCID, PROPERTY_KEYID1, PROPERTY_KEYID2, PROPERTY_KEYID3, PROPERTY_PARAMLISTID, PROPERTY_PARAMLISTVERSIONID, PROPERTY_VARIANTID, PROPERTY_DATASET, PROPERTY_APPROVALSTEP});
    }

    public static void updateSDIApproval(DBAccess database, DataSet ds) throws SapphireException {
        DataSetUtil.update(database, ds, "sdiapproval", new String[]{PROPERTY_SDCID, PROPERTY_KEYID1, PROPERTY_KEYID2, PROPERTY_KEYID3, PROPERTY_APPROVALTYPEID});
    }

    public static String getKeyidValue(DataSet primary, int row, PropertyList sdcdetails, String keycolid) {
        String keycolid1 = sdcdetails.getProperty(keycolid, "");
        if (keycolid1.length() > 0) {
            return primary.getString(row, keycolid1, "");
        }
        return "(null)";
    }

    public static String getSDIDataAPRulesSQL() {
        StringBuilder sql = new StringBuilder("select apr.* from paramlist pl,approvaltyperule apr");
        sql.append(" where pl.approvaltypeid=apr.approvaltypeid");
        sql.append(" and pl.paramlistid=?");
        sql.append(" and pl.paramlistversionid=?");
        sql.append(" and pl.variantid=?");
        return sql.toString();
    }

    public static String getSDIDataAPStepsSQL() {
        StringBuilder sql = new StringBuilder("select * from sdidataapproval ");
        sql.append(" where sdcid=?");
        sql.append(" and keyid1=?");
        sql.append(" and keyid2=?");
        sql.append(" and keyid3=?");
        sql.append(" and paramlistid=?");
        sql.append(" and paramlistversionid=?");
        sql.append(" and variantid=?");
        sql.append(" and dataset=?");
        return sql.toString();
    }

    public static String getSDIAPRulesSQL() {
        StringBuilder sql = new StringBuilder("select apr.* from sdiapproval sdiap,approvaltyperule apr");
        sql.append(" where sdiap.approvaltypeid=apr.approvaltypeid");
        sql.append(" and sdiap.sdcid=?");
        sql.append(" and sdiap.keyid1=?");
        sql.append(" and sdiap.keyid2=?");
        sql.append(" and sdiap.keyid3=?");
        return sql.toString();
    }

    public static String getSDIAPStepsSQL(String sdcid) {
        StringBuilder sql = new StringBuilder();
        if (sdcid.equalsIgnoreCase(SDC_REQUEST)) {
            sql.append("select sdiapprovalstep.* from sdiapprovalstep,sdiapproval ");
            sql.append(" where sdiapprovalstep.sdcid=sdiapproval.sdcid");
            sql.append(" and sdiapprovalstep.keyid1=sdiapproval.keyid1");
            sql.append(" and sdiapprovalstep.keyid2=sdiapproval.keyid2");
            sql.append(" and sdiapprovalstep.keyid3=sdiapproval.keyid3");
            sql.append(" and sdiapprovalstep.approvaltypeid=sdiapproval.approvaltypeid");
            sql.append(" and sdiapproval.approvalfunction='Release'");
            sql.append(" and sdiapprovalstep.sdcid=?");
            sql.append(" and sdiapprovalstep.keyid1=?");
            sql.append(" and sdiapprovalstep.keyid2=?");
            sql.append(" and sdiapprovalstep.keyid3=?");
        } else {
            sql.append("select * from sdiapprovalstep ");
            sql.append(" where sdcid=?");
            sql.append(" and keyid1=?");
            sql.append(" and keyid2=?");
            sql.append(" and keyid3=?");
        }
        return sql.toString();
    }

    public static void evaluateSDIApprovalRules(SDIApprovalInfo sdApprovalInfo, DataSet apRulesDS, PreparedStatement apStepsStmnt, DataSet nonAutoApprovedSteps, DataSet autoApprovedSteps, DataSet sdiApprovalDS, PropertyList nonAutoApprovedSDIProps, PropertyList autoApprovedSDIProps, DataSet primary, int row, ConnectionInfo connectionInfo) throws Exception {
        QueryProcessor qp = new QueryProcessor(connectionInfo.getConnectionId());
        HashMap<String, String> failureRuleNameMap = new HashMap<String, String>();
        HashMap<String, String> failureMsgMap = new HashMap<String, String>();
        String sdcid = sdApprovalInfo.getSdcid();
        String keyid1 = sdApprovalInfo.getKeyId1();
        String keyid2 = sdApprovalInfo.getKeyId2();
        String keyid3 = sdApprovalInfo.getKeyId3();
        String statuscolumn = sdApprovalInfo.getStatuscolumn();
        String pendingapprovalstatus = sdApprovalInfo.getPendingapprovalstatus();
        String approvedstatus = sdApprovalInfo.getApprovedstatus();
        String approvedbycolumn = sdApprovalInfo.getApprovedbycolumn();
        String approveddtcolumn = sdApprovalInfo.getApproveddtcolumn();
        apStepsStmnt.setString(1, sdcid);
        apStepsStmnt.setString(2, keyid1);
        apStepsStmnt.setString(3, keyid2);
        apStepsStmnt.setString(4, keyid3);
        DataSet apStepsDS = new DataSet(apStepsStmnt.executeQuery());
        String[] stepsArr = ApprovalRuleUtil.getSteps(apStepsDS);
        PropertyList primarykeyProps = new PropertyList();
        ApprovalRuleUtil.populateProps(primarykeyProps, sdcid, keyid1, keyid2, keyid3);
        ApprovalRuleUtil.evaluateAPRules(apRulesDS, primarykeyProps, qp, false);
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put(APPROVALRULESTATUS, APPROVALRULESTATUS_FAIL);
        DataSet failedAPRule = apRulesDS.getFilteredDataSet(hm);
        if (failedAPRule.getRowCount() > 0) {
            for (int r = 0; r < failedAPRule.size(); ++r) {
                String aptypeid = failedAPRule.getString(r, PROPERTY_APPROVALTYPEID, "");
                String approvalstep = failedAPRule.getString(r, PROPERTY_APPROVALSTEP, "");
                String rulename = failedAPRule.getString(r, PROPERTY_RULENAME, "");
                String failuremessage = failedAPRule.getString(r, PROPERTY_FAILUREMESSAGE, "");
                if (approvalstep.trim().length() > 0) {
                    ApprovalRuleUtil.updateMap(failureRuleNameMap, aptypeid + "||" + approvalstep, rulename, "||");
                    ApprovalRuleUtil.updateMap(failureMsgMap, aptypeid + "||" + approvalstep, failuremessage, "||");
                    continue;
                }
                for (String value : stepsArr) {
                    if (!aptypeid.equals(StringUtil.split(value, "||")[0])) continue;
                    ApprovalRuleUtil.updateMap(failureRuleNameMap, value, rulename, "||");
                    ApprovalRuleUtil.updateMap(failureMsgMap, value, failuremessage, "||");
                }
            }
            ApprovalRuleUtil.populateProps(nonAutoApprovedSDIProps, sdcid, keyid1, keyid2, keyid3, statuscolumn, pendingapprovalstatus, "", "", "");
        }
        HashMap<String, String> apWithRuleHM = new HashMap<String, String>();
        ArrayList<String> apWithoutRuleList = new ArrayList<String>();
        for (int r = 0; r < apStepsDS.size(); ++r) {
            String type = apStepsDS.getString(r, PROPERTY_APPROVALTYPEID, "");
            if (apWithRuleHM.containsKey(type) || apRulesDS.findRow(PROPERTY_APPROVALTYPEID, type) > -1) {
                if (!apWithRuleHM.containsKey(type)) {
                    apWithRuleHM.put(type, APPROVALRULESTATUS_PASS);
                }
                String step = apStepsDS.getString(r, PROPERTY_APPROVALSTEP, "");
                String instance = apStepsDS.getValue(r, PROPERTY_APPROVALSTEPINSTANCE, "");
                String approvaltypeidNstep = type + "||" + step;
                if (failureRuleNameMap.containsKey(approvaltypeidNstep)) {
                    apWithRuleHM.put(type, APPROVALRULESTATUS_FAIL);
                    String rulename = failureRuleNameMap.get(approvaltypeidNstep);
                    String failuremessage = failureMsgMap.get(approvaltypeidNstep);
                    ApprovalRuleUtil.addToDS(nonAutoApprovedSteps, sdcid, keyid1, keyid2, keyid3, type, step, instance, rulename, failuremessage);
                    continue;
                }
                ApprovalRuleUtil.addToDS(autoApprovedSteps, sdcid, keyid1, keyid2, keyid3, type, step, instance, APPROVALRULESTATUS_PASS);
                continue;
            }
            if (apWithoutRuleList.contains(type)) continue;
            apWithoutRuleList.add(type);
        }
        for (Map.Entry set : apWithRuleHM.entrySet()) {
            if (!((String)set.getValue()).equalsIgnoreCase(APPROVALRULESTATUS_PASS)) continue;
            ApprovalRuleUtil.addToSDIApprovalDS(sdiApprovalDS, sdcid, keyid1, keyid2, keyid3, (String)set.getKey(), (String)set.getValue());
        }
        if (apWithoutRuleList.isEmpty() && failedAPRule.isEmpty()) {
            if (primary != null) {
                primary.setString(row, statuscolumn, approvedstatus);
                primary.setString(row, approvedbycolumn, APPROVEDBY_AUTOAP);
                Calendar releaseddt = new M18NUtil(connectionInfo).getNowCalendar();
                primary.setDate(row, approveddtcolumn, releaseddt);
            } else if (autoApprovedSDIProps != null) {
                ApprovalRuleUtil.populateProps(autoApprovedSDIProps, sdcid, keyid1, keyid2, keyid3, statuscolumn, approvedstatus, approvedstatus, approvedbycolumn, approveddtcolumn);
            }
        } else if (!apWithoutRuleList.isEmpty() && failedAPRule.isEmpty()) {
            ApprovalRuleUtil.populateProps(nonAutoApprovedSDIProps, sdcid, keyid1, keyid2, keyid3, statuscolumn, pendingapprovalstatus, "", "", "");
        }
    }

    public static String getUniqueValues(String values) {
        String inClauseDelimeter = "','";
        String delimeter = ";";
        HashMap<String, String> hm = new HashMap<String, String>();
        if (values != null && values.trim().length() > 0 && values.contains(delimeter)) {
            String[] valuesArr;
            for (String v : valuesArr = StringUtil.split(values, delimeter)) {
                hm.put(v, "");
            }
            values = String.join((CharSequence)inClauseDelimeter, hm.keySet());
        }
        return values;
    }

    public static String buildSDIDataWhereCluase(SafeSQL safeSQL, String tableid, PropertyList primarykeyProps, boolean isStart) {
        return ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, tableid, primarykeyProps, isStart, false);
    }

    public static String buildSDIDataWhereCluase(SafeSQL safeSQL, String tableid, PropertyList primarykeyProps, boolean isStart, boolean excludeDataSetCheck) {
        StringBuilder sql = new StringBuilder();
        String sdcid = primarykeyProps.getProperty(PROPERTY_SDCID);
        String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1, "");
        String keyid2 = primarykeyProps.getProperty(PROPERTY_KEYID2, "");
        String keyid3 = primarykeyProps.getProperty(PROPERTY_KEYID3, "");
        String paramlistid = primarykeyProps.getProperty(PROPERTY_PARAMLISTID, "");
        String paramlistversionid = primarykeyProps.getProperty(PROPERTY_PARAMLISTVERSIONID, "");
        String variantid = primarykeyProps.getProperty(PROPERTY_VARIANTID, "");
        String dataset = primarykeyProps.getProperty(PROPERTY_DATASET, "");
        sql.append(isStart ? " where " : " and ").append(tableid).append(".sdcid=").append(safeSQL.addVar(sdcid));
        sql.append(" and ").append(tableid).append(".keyid1 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(keyid1))).append(")");
        sql.append(" and ").append(tableid).append(".keyid2 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(keyid2))).append(")");
        sql.append(" and ").append(tableid).append(".keyid3 in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(keyid3))).append(")");
        sql.append(" and ").append(tableid).append(".paramlistid in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(paramlistid))).append(")");
        sql.append(" and ").append(tableid).append(".paramlistversionid in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(paramlistversionid))).append(")");
        sql.append(" and ").append(tableid).append(".variantid in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(variantid))).append(")");
        if (!excludeDataSetCheck) {
            sql.append(" and ").append(tableid).append(".dataset in (").append(safeSQL.addIn(ApprovalRuleUtil.getUniqueValues(dataset))).append(")");
        }
        return sql.toString();
    }

    public static DataSet filterSelectedDataSets(DataSet ds, PropertyList primarykeyProps) {
        DataSet selectedDataSets = new DataSet();
        if (ds != null && ds.getRowCount() > 0) {
            String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1, "");
            if (keyid1.contains(";")) {
                String sdcid = primarykeyProps.getProperty(PROPERTY_SDCID);
                String keyid2 = primarykeyProps.getProperty(PROPERTY_KEYID2, "");
                String keyid3 = primarykeyProps.getProperty(PROPERTY_KEYID3, "");
                String paramlistid = primarykeyProps.getProperty(PROPERTY_PARAMLISTID, "");
                String paramlistversionid = primarykeyProps.getProperty(PROPERTY_PARAMLISTVERSIONID, "");
                String variantid = primarykeyProps.getProperty(PROPERTY_VARIANTID, "");
                String dataset = primarykeyProps.getProperty(PROPERTY_DATASET, "");
                String[] keyid1Arr = StringUtil.split(keyid1, ";");
                String[] keyid2Arr = StringUtil.split(keyid2, ";");
                String[] keyid3Arr = StringUtil.split(keyid3, ";");
                String[] paramlistidArr = StringUtil.split(paramlistid, ";");
                String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
                String[] variantidArr = StringUtil.split(variantid, ";");
                String[] datasetArr = StringUtil.split(dataset, ";");
                for (int i = 0; i < keyid1Arr.length; ++i) {
                    HashMap<String, Object> hm = new HashMap<String, Object>();
                    hm.put(PROPERTY_SDCID, sdcid);
                    hm.put(PROPERTY_KEYID1, keyid1Arr[i]);
                    hm.put(PROPERTY_KEYID2, keyid2Arr[i]);
                    hm.put(PROPERTY_KEYID3, keyid3Arr[i]);
                    hm.put(PROPERTY_PARAMLISTID, paramlistidArr[i]);
                    hm.put(PROPERTY_PARAMLISTVERSIONID, paramlistversionidArr[i]);
                    hm.put(PROPERTY_VARIANTID, variantidArr[i]);
                    hm.put(PROPERTY_DATASET, new BigDecimal(datasetArr[i]));
                    DataSet filteredDS = ds.getFilteredDataSet(hm);
                    if (filteredDS.getRowCount() <= 0) continue;
                    selectedDataSets.copyRow(filteredDS, -1, 1);
                }
                return selectedDataSets;
            }
            return ds;
        }
        return selectedDataSets;
    }

    private static String getSelectClause(String tableid) {
        if (tableid.equalsIgnoreCase("sdiworkitemitem")) {
            return tableid + "." + PROPERTY_SDCID + "," + tableid + "." + PROPERTY_KEYID1 + "," + tableid + "." + PROPERTY_KEYID2 + "," + tableid + "." + PROPERTY_KEYID3 + "," + tableid + ".itemkeyid1 " + PROPERTY_PARAMLISTID + "," + tableid + ".itemkeyid2 " + PROPERTY_PARAMLISTVERSIONID + "," + tableid + ".itemkeyid3 " + PROPERTY_VARIANTID + "," + tableid + ".iteminstance " + PROPERTY_DATASET;
        }
        return tableid + "." + PROPERTY_SDCID + "," + tableid + "." + PROPERTY_KEYID1 + "," + tableid + "." + PROPERTY_KEYID2 + "," + tableid + "." + PROPERTY_KEYID3 + "," + tableid + "." + PROPERTY_PARAMLISTID + "," + tableid + "." + PROPERTY_PARAMLISTVERSIONID + "," + tableid + "." + PROPERTY_VARIANTID + "," + tableid + "." + PROPERTY_DATASET;
    }

    private static void setSDIStatusToPendingAutoApprEval(PropertyList properties, ConnectionInfo connectionInfo) throws SapphireException {
        ActionProcessor ap = new ActionProcessor(connectionInfo.getConnectionId());
        String sdcid = properties.getProperty(PROPERTY_SDCID, "");
        String statuscolumn = properties.getProperty("statuscolumn", "");
        PropertyList props = new PropertyList();
        props.setProperty(PROPERTY_SDCID, sdcid);
        props.setProperty(PROPERTY_KEYID1, properties.getProperty(PROPERTY_KEYID1, ""));
        props.setProperty(PROPERTY_KEYID2, properties.getProperty(PROPERTY_KEYID2, ""));
        props.setProperty(PROPERTY_KEYID3, properties.getProperty(PROPERTY_KEYID3, ""));
        props.setProperty(statuscolumn, STATUS_PENDINGAP);
        ap.processAction("EditSDI", "1", props);
    }

    private static void setDatasetStatusToPendingAutoApprEval(PropertyList properties, ConnectionInfo connectionInfo) throws SapphireException {
        ActionProcessor ap = new ActionProcessor(connectionInfo.getConnectionId());
        String sdcid = properties.getProperty(PROPERTY_SDCID, "");
        PropertyList props = new PropertyList();
        props.setProperty(PROPERTY_SDCID, sdcid);
        props.setProperty(PROPERTY_KEYID1, properties.getProperty(PROPERTY_KEYID1, ""));
        props.setProperty(PROPERTY_KEYID2, properties.getProperty(PROPERTY_KEYID2, ""));
        props.setProperty(PROPERTY_KEYID3, properties.getProperty(PROPERTY_KEYID3, ""));
        props.setProperty(PROPERTY_PARAMLISTID, properties.getProperty(PROPERTY_PARAMLISTID, ""));
        props.setProperty(PROPERTY_PARAMLISTVERSIONID, properties.getProperty(PROPERTY_PARAMLISTVERSIONID, ""));
        props.setProperty(PROPERTY_VARIANTID, properties.getProperty(PROPERTY_VARIANTID, ""));
        props.setProperty(PROPERTY_DATASET, properties.getProperty(PROPERTY_DATASET, ""));
        props.setProperty(DSSTATUSCOLUMN, ApprovalRuleUtil.getRepeatedValues(STATUS_PENDINGAP, StringUtil.split(properties.getProperty(PROPERTY_PARAMLISTID, ""), ";").length));
        props.setProperty("propsmatch", "Y");
        ap.processAction("EditDataSet", "1", props);
    }

    public static String getRepeatedValues(String value, int r) {
        StringBuilder values = new StringBuilder(value);
        for (int i = 2; i <= r; ++i) {
            values.append(";").append(value);
        }
        return values.toString();
    }

    public static boolean blockAutoApprovalRuleCheckAsPerPolicy(String sdcid, String keyid1, PropertyList policy, QueryProcessor qp) throws SapphireException {
        if (sdcid.equalsIgnoreCase(SDC_BATCH)) {
            boolean syncBatchReleaseWithStage;
            String syncBatchReleaseWithStageString = policy.getProperty("syncbatchwithstage", "N");
            boolean bl = syncBatchReleaseWithStage = !syncBatchReleaseWithStageString.equalsIgnoreCase("N");
            if (syncBatchReleaseWithStage && !BatchLifeCycleUtil.isChildStagesReleased(keyid1, qp, syncBatchReleaseWithStageString)) {
                return true;
            }
        }
        String openIncidentPropName = "";
        if (sdcid.equalsIgnoreCase(SDC_BATCH)) {
            openIncidentPropName = "batchapprovalwithincidents";
        } else if (sdcid.equalsIgnoreCase(SDC_BATCHSTAGE)) {
            openIncidentPropName = "batchstageapprovalwithincidents";
        } else if (sdcid.equalsIgnoreCase(SDC_SAMPLE)) {
            openIncidentPropName = "sampleapprovalwithincidents";
        } else if (sdcid.equalsIgnoreCase(SDC_REQUEST)) {
            openIncidentPropName = "requestapprovalwithincidents";
        }
        if (openIncidentPropName.length() > 0 && policy.getProperty(openIncidentPropName, "Allow").equalsIgnoreCase("block")) {
            String incidentflagcount;
            PropertyList primarykey = new PropertyList();
            primarykey.setProperty(PROPERTY_KEYID1, keyid1);
            primarykey.setProperty(PROPERTY_SDCID, sdcid);
            DataSet incidentDS = ApprovalRuleUtil.getIncidents(primarykey, qp, true);
            if (incidentDS.getRowCount() > 0 && (incidentflagcount = incidentDS.getValue(0, "incidentflagcount", "")).length() > 0 && Integer.parseInt(incidentflagcount) > 0) {
                return true;
            }
        }
        return sdcid.equalsIgnoreCase(SDC_BATCH) && BatchLifeCycleUtil.isParentBatchesReleased(keyid1, qp, policy) > 0;
    }

    public static void clearSkipCheckApprovalRuleProp(DataSet primary) {
        if (primary.isValidColumn(SKIPAPPROVALRULECHECK)) {
            primary.removeColumn(SKIPAPPROVALRULECHECK);
        }
    }

    private static boolean isManaullyApprovedDSFound(DataSet sdidata) {
        boolean found = false;
        if (sdidata != null) {
            for (int r = 0; r < sdidata.getRowCount(); ++r) {
                String approvedby = sdidata.getString(r, PROPERTY_APPROVEDBY, "");
                String datasetstatus = sdidata.getString(r, DSSTATUSCOLUMN, "");
                String approvalpassrule = sdidata.getString(r, "approvalpassrule", "");
                if (approvedby.equals(APPROVEDBY_AUTOAP) || !datasetstatus.equalsIgnoreCase("Completed") || approvalpassrule.length() <= 0) continue;
                found = true;
                break;
            }
        }
        return found;
    }

    private static String resolvedReTestOrCancelledDS(PropertyList primarykeyProps, QueryProcessor qp, boolean isCancelled) {
        String result = APPROVALRULESTATUS_PASS;
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select sdidata.s_retestedflag,sdidata.s_remeasuredflag,sdidata.s_datasetstatus,").append(ApprovalRuleUtil.getSelectClause(TABLE_SDIDATA)).append(" from sdidata ");
        sql.append(ApprovalRuleUtil.buildSDIDataWhereCluase(safeSQL, TABLE_SDIDATA, primarykeyProps, true, true));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds.getRowCount() > 1) {
            String sdcid = primarykeyProps.getProperty(PROPERTY_SDCID);
            String keyid1 = primarykeyProps.getProperty(PROPERTY_KEYID1, "");
            String keyid2 = primarykeyProps.getProperty(PROPERTY_KEYID2, "");
            String keyid3 = primarykeyProps.getProperty(PROPERTY_KEYID3, "");
            String paramlistid = primarykeyProps.getProperty(PROPERTY_PARAMLISTID, "");
            String paramlistversionid = primarykeyProps.getProperty(PROPERTY_PARAMLISTVERSIONID, "");
            String variantid = primarykeyProps.getProperty(PROPERTY_VARIANTID, "");
            HashMap<String, String> hm = new HashMap<String, String>();
            if (paramlistid.contains(";")) {
                String[] keyid1Arr = StringUtil.split(keyid1, ";");
                String[] keyid2Arr = StringUtil.split(keyid2, ";");
                String[] keyid3Arr = StringUtil.split(keyid3, ";");
                String[] paramlistidArr = StringUtil.split(paramlistid, ";");
                String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
                String[] variantidArr = StringUtil.split(variantid, ";");
                for (int i = 0; i < paramlistidArr.length; ++i) {
                    hm.clear();
                    hm.put(PROPERTY_SDCID, sdcid);
                    hm.put(PROPERTY_KEYID1, keyid1Arr[i]);
                    hm.put(PROPERTY_KEYID2, keyid2Arr[i]);
                    hm.put(PROPERTY_KEYID3, keyid3Arr[i]);
                    hm.put(PROPERTY_PARAMLISTID, paramlistidArr[i]);
                    hm.put(PROPERTY_PARAMLISTVERSIONID, paramlistversionidArr[i]);
                    hm.put(PROPERTY_VARIANTID, variantidArr[i]);
                    DataSet filteredDS = ds.getFilteredDataSet(hm);
                    if (filteredDS.getRowCount() <= 1) continue;
                    if (isCancelled) {
                        hm.clear();
                        hm.put(DSSTATUSCOLUMN, "Cancelled");
                        if (filteredDS.findRow(hm) <= -1) continue;
                        result = APPROVALRULESTATUS_FAIL;
                    } else {
                        hm.clear();
                        hm.put("s_retestedflag", "Y");
                        if (filteredDS.findRow(hm) > -1) {
                            result = APPROVALRULESTATUS_FAIL;
                        } else {
                            hm.clear();
                            hm.put("s_remeasuredflag", "Y");
                            if (filteredDS.findRow(hm) <= -1) continue;
                            result = APPROVALRULESTATUS_FAIL;
                        }
                    }
                    break;
                }
            } else if (isCancelled) {
                hm.put(DSSTATUSCOLUMN, "Cancelled");
                if (ds.findRow(hm) > -1) {
                    result = APPROVALRULESTATUS_FAIL;
                }
            } else {
                hm.put("s_retestedflag", "Y");
                if (ds.findRow(hm) > -1) {
                    result = APPROVALRULESTATUS_FAIL;
                } else {
                    hm.clear();
                    hm.put("s_remeasuredflag", "Y");
                    if (ds.findRow(hm) > -1) {
                        result = APPROVALRULESTATUS_FAIL;
                    }
                }
            }
        }
        return result;
    }

    static {
        dsRuleList.add(RULE_MULTIPLEANALYST);
        dsRuleList.add(RULE_OVERRIDDENCERTIFICATION);
        dsRuleList.add(RULE_UNASSIGNEDANLYST);
        dsRuleList.add(RULE_EXPIREDCONSUMABLE);
        dsRuleList.add(RULE_PASTDUEINSTRUMENT);
        dsRuleList.add(RULE_RESULTFAILED);
        dsRuleList.add(RULE_RESULTWARNING);
        dsRuleList.add(RULE_RESULTMODIFIED);
        dsRuleList.add(RULE_RESULTINSTRUMENTMODIFIED);
        dsRuleList.add(RULE_RESULTDYNAMICAUDITRECORD);
        dsRuleList.add(RULE_DATASETRETEST);
        dsRuleList.add(RULE_REPLICATEADDED);
    }

    public static class SDIApprovalInfo {
        private final String sdcid;
        private final String keyid1;
        private final String keyid2;
        private final String keyid3;
        private final String statuscolumn;
        private final String pendingapprovalstatus;
        private final String approvedstatus;
        private final String approvedbycolumn;
        private final String approveddtcolumn;

        public SDIApprovalInfo(String sdcid, String keyid1, String keyid2, String keyid3, String statuscolumn, String pendingapprovalstatus, String approvedstatus, String approvedbycolumn, String approveddtcolumn) {
            this.sdcid = sdcid;
            this.keyid1 = keyid1;
            this.keyid2 = keyid2;
            this.keyid3 = keyid3;
            this.statuscolumn = statuscolumn;
            this.pendingapprovalstatus = pendingapprovalstatus;
            this.approvedstatus = approvedstatus;
            this.approvedbycolumn = approvedbycolumn;
            this.approveddtcolumn = approveddtcolumn;
        }

        public String getSdcid() {
            return this.sdcid;
        }

        public String getKeyId1() {
            return this.keyid1;
        }

        public String getKeyId2() {
            return this.keyid2;
        }

        public String getKeyId3() {
            return this.keyid3;
        }

        public String getStatuscolumn() {
            return this.statuscolumn;
        }

        public String getPendingapprovalstatus() {
            return this.pendingapprovalstatus;
        }

        public String getApprovedstatus() {
            return this.approvedstatus;
        }

        public String getApprovedbycolumn() {
            return this.approvedbycolumn;
        }

        public String getApproveddtcolumn() {
            return this.approveddtcolumn;
        }
    }
}

