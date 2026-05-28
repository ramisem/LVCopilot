/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.admin.ddt.BatchLifeCycleUtil;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_BatchStage
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public static String BATCHSTAGE_INITIAL = "Initial";
    public static String BATCHSTAGE_INPROGRESS = "InProgress";
    public static String BATCHSTAGE_PENDINGRELEASE = "Pending Release";
    public static String BATCHSTAGE_RELEASED = "Released";
    public static String BATCHSTAGE_REJECTED = "Rejected";
    public static String BATCHSTAGE_CANCELLED = "Cancelled";

    @Override
    public void postApprove(DataSet approve) throws SapphireException {
        DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, approve);
        DataSet dsProp = new DataSet();
        ArrayList<String> pendingReleaseList = new ArrayList<String>();
        StringBuilder autoReleaseBatches = new StringBuilder();
        for (int i = 0; i < approvedDS.size(); ++i) {
            int newRow = dsProp.addRow();
            String approvalFlag = approvedDS.getValue(i, "approvalflag");
            String reviewStatus = "Pass".equalsIgnoreCase(approvalFlag) ? BATCHSTAGE_RELEASED : BATCHSTAGE_REJECTED;
            dsProp.setString(newRow, "keyid1", approvedDS.getValue(i, "keyid1"));
            dsProp.setString(newRow, "batchstagestatus", reviewStatus);
            if (reviewStatus.equals(BATCHSTAGE_RELEASED)) {
                dsProp.setString(newRow, "releaseddt", new M18NUtil(this.connectionInfo).format(DateTimeUtil.getNowCalendar()));
                dsProp.setString(newRow, "releasedby", this.getConnectionInfo().getSysuserId());
                continue;
            }
            dsProp.setString(newRow, "releaseddt", "(null)");
            dsProp.setString(newRow, "releasedby", "(null)");
        }
        if (dsProp.size() > 0) {
            String sdcId = this.getSdcid();
            PropertyList props = new PropertyList();
            props.put("sdcid", sdcId);
            props.put("keyid1", dsProp.getColumnValues("keyid1", ";"));
            props.put("batchstagestatus", dsProp.getColumnValues("batchstagestatus", ";"));
            props.put("releaseddt", dsProp.getColumnValues("releaseddt", ";"));
            props.put("releasedby", dsProp.getColumnValues("releasedby", ";"));
            props.put("tracelogid", approve.getValue(0, "tracelogid"));
            this.getActionProcessor().processAction("EditSDI", "1", props, true);
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer("SELECT bs.batchid, b.batchstatus FROM s_batchstage bs, s_batch b WHERE bs.batchid=b.s_batchid and bs.s_batchstageid IN (");
            sql.append(safeSQL.addIn(dsProp.getColumnValues("keyid1", "','")));
            sql.append(")");
            DataSet batchDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
            String pendingReleaseDisplayValue = BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease");
            String releaseDisplayValue = BatchLifeCycleUtil.getBatchStateDisplayValue("released");
            for (int i = 0; i < batchDs.getRowCount(); ++i) {
                boolean isToBeAutoReleased;
                String syncBatchReleaseWithStageString;
                boolean hasOpenIncidents;
                String batchId = batchDs.getValue(i, "batchid", "");
                String batchStatus = batchDs.getValue(i, "batchstatus", "");
                if (batchStatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("initial")) || !BatchLifeCycleUtil.hasAllTestsFinished(batchId, this.getQueryProcessor())) continue;
                props.clear();
                props.put("sdcid", "Batch");
                props.put("keyid1", batchId);
                props.put("pendingapprovalstatus", pendingReleaseDisplayValue);
                props.put("approvalstatus", releaseDisplayValue);
                props.put("approvalstatuscolumn", "batchstatus");
                props.put("tracelogid", approve.getValue(0, "tracelogid"));
                String batchApprovalPolicy = policy.getProperty("batchapprovalwithincidents", "Allow");
                boolean sendToPendingReleaseList = false;
                if ((batchApprovalPolicy.equalsIgnoreCase("block") || batchApprovalPolicy.equalsIgnoreCase("warn")) && (hasOpenIncidents = BatchLifeCycleUtil.hasOpenIncidents(batchId, this.getQueryProcessor()))) {
                    sendToPendingReleaseList = true;
                    pendingReleaseList.add(batchId);
                }
                if (sendToPendingReleaseList) {
                    props.put("pendingapprovalstatus", pendingReleaseDisplayValue);
                    props.put("approvalstatus", pendingReleaseDisplayValue);
                }
                boolean syncBatchReleaseWithStage = !(syncBatchReleaseWithStageString = policy.getProperty("syncbatchwithstage", "N")).equalsIgnoreCase("N");
                boolean isChildStagesReleased = BatchLifeCycleUtil.isChildStagesReleased(batchId, this.getQueryProcessor(), syncBatchReleaseWithStageString);
                String sqlGetChildStagesCount = "select count(*) from s_batchstage bs where batchid = ? and ( batchstagestatus not in ('" + BATCHSTAGE_RELEASED + "','" + BATCHSTAGE_REJECTED + "','" + BATCHSTAGE_CANCELLED + "' ) and exists (select * from s_sample s where s.batchstageid=bs.s_batchstageid  having count(*)>0)  ) ";
                int childStageCount = this.getQueryProcessor().getPreparedCount(sqlGetChildStagesCount, new Object[]{batchId});
                boolean isBatchCancelled = batchStatus.equalsIgnoreCase("Cancelled");
                if (!(childStageCount != 0 && syncBatchReleaseWithStage || isBatchCancelled)) {
                    this.getActionProcessor().processAction("SubmitSDIForApproval", "1", props);
                }
                DataSet autoReleaseReqBatchDS = null;
                String skipLevel_Policy = policy.getProperty("skiplevel", "");
                if (skipLevel_Policy.length() > 0) {
                    safeSQL.reset();
                    sql = new StringBuffer("SELECT s_batch.s_batchid FROM s_product, s_batch WHERE ");
                    sql.append(" s_product.s_productid=s_batch.productid AND s_product.s_productversionid=s_batch.productversionid ");
                    sql.append(" AND s_product.autoreleaseflag = 'Y' ");
                    sql.append(" AND (s_batch.levelid IS NULL OR s_batch.levelid != ").append(safeSQL.addVar(skipLevel_Policy)).append(")");
                    sql.append(" AND s_batch.s_batchid = ").append(safeSQL.addVar(batchId));
                    autoReleaseReqBatchDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                }
                if (autoReleaseReqBatchDS == null || autoReleaseReqBatchDS.findRow("s_batchid", batchId) == -1 || sendToPendingReleaseList || !isChildStagesReleased || !(isToBeAutoReleased = BatchLifeCycleUtil.isToBeAutoReleased(batchId, policy, this.getQueryProcessor(), this.getConfigurationProcessor()))) continue;
                autoReleaseBatches.append(";").append(batchId);
            }
            if (autoReleaseBatches.length() > 0) {
                HashMap<String, String> prop = new HashMap<String, String>();
                prop.put("sdcid", "Batch");
                prop.put("keyid1", autoReleaseBatches.substring(1));
                prop.put("batchstatus", BatchLifeCycleUtil.getBatchStateDisplayValue("released"));
                prop.put("releasedby", "(system)");
                prop.put("releaseddt", new M18NUtil(this.connectionInfo).format(DateTimeUtil.getNowCalendar()));
                prop.put("disposition", "Passed");
                this.getActionProcessor().processAction("EditSDI", "1", prop);
            }
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.copyCancelStatusToChildren(primary, actionProps);
        ApprovalRuleUtil.checkSDIAutoApprovalRule(primary, actionProps, "LV_BatchStage", "batchstagestatus", "Pending Release", "Released", "releasedby", "releaseddt", this.database, this.connectionInfo, this.logger, this.getSDCProcessor(), true);
        this.reevaluateBatchStatus(primary, actionProps);
    }

    private void reevaluateBatchStatus(DataSet primary, PropertyList actionProps) throws SapphireException {
        if (primary.getColumnValues("batchstagestatus", ";").contains("Released")) {
            StringBuilder batchIdBuffer = new StringBuilder();
            DataSet beforeEditImagePrimary = this.getBeforeEditImage().getDataset("primary");
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String batchStageStatus = primary.getValue(i, "batchstagestatus", "");
                if (!this.hasPrimaryValueChanged(primary, i, "batchstagestatus") || !beforeEditImagePrimary.getValue(i, "batchstagestatus", "").equals("Pending AutoApprEval")) continue;
                SafeSQL safeSQL = new SafeSQL();
                String batchId = primary.getValue(i, "batchid", this.getOldPrimaryValue(primary, i, "batchid"));
                StringBuilder batchSql = new StringBuilder("select batchstatus from s_batch where s_batchid = ").append(safeSQL.addVar(batchId));
                DataSet batchStatusDs = this.getQueryProcessor().getPreparedSqlDataSet(batchSql.toString(), safeSQL.getValues());
                if (batchStatusDs == null || batchStatusDs.getRowCount() <= 0) continue;
                String batchStatus = batchStatusDs.getString(0, "batchstatus");
                PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
                String syncBatchReleaseWithStageString = policy.getProperty("syncbatchwithstage", "N");
                if (!batchStageStatus.equals("Released") || !BatchLifeCycleUtil.isChildStagesReleased(batchId, this.getQueryProcessor(), syncBatchReleaseWithStageString) || batchStatus.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("initial")) || batchId == null || batchId.length() <= 0) continue;
                batchIdBuffer.append(";").append(batchId);
            }
            if (batchIdBuffer.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Batch");
                props.setProperty("keyid1", batchIdBuffer.substring(1));
                props.setProperty("operation", "synchronizeonly");
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
    }

    private void copyCancelStatusToChildren(DataSet primary, PropertyList actionProps) throws SapphireException {
        PropertyList props = null;
        DataSet initialPrimary = this.getBeforeEditImage().getDataset("primary");
        PropertyList batchSamplePolicy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        String batchCancelBehavior = batchSamplePolicy.getProperty("cancelactionbehavior", "");
        boolean batchStatusRollup = !actionProps.getProperty("statusrollup").equals("false");
        SafeSQL safeSQL = new SafeSQL();
        if (batchCancelBehavior.equalsIgnoreCase("Cancel all Children") || batchCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
            String batchStageStatus;
            int i;
            if (primary.getColumnValues("batchstagestatus", ";").contains("Cancelled")) {
                String cancelledBatchStages = "";
                HashSet<String> uniqueBatchIdWhoseStageCancelled = new HashSet<String>();
                for (i = 0; i < primary.getRowCount(); ++i) {
                    batchStageStatus = primary.getValue(i, "batchstagestatus", "");
                    String batchStageId = primary.getValue(i, "s_batchstageid", "");
                    if (!this.hasPrimaryValueChanged(primary, i, "batchstagestatus") || !batchStageStatus.equals("Cancelled")) continue;
                    cancelledBatchStages = cancelledBatchStages + ";" + batchStageId;
                    String batchId = primary.getValue(i, "batchid", this.getOldPrimaryValue(primary, i, "batchid"));
                    if (!batchStatusRollup || batchId == null || batchId.length() <= 0) continue;
                    uniqueBatchIdWhoseStageCancelled.add(batchId);
                }
                if (cancelledBatchStages.length() > 0) {
                    DataSet wids;
                    DataSet samplesDS;
                    StringBuffer samplesDSsb = new StringBuffer();
                    String rsetId = BaseSDIDataAction.createBypassSecurityRSet("LV_BatchStage", cancelledBatchStages.substring(1), null, null, this.database, this.connectionInfo, false);
                    samplesDSsb.append("SELECT s_sample.s_sampleid FROM s_sample, rsetitems rs WHERE ");
                    samplesDSsb.append(" s_sample.batchstageid=rs.keyid1 ");
                    samplesDSsb.append(" AND rs.rsetid=").append(safeSQL.addVar(rsetId));
                    if (batchCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                        samplesDSsb.append(" AND s_sample.samplestatus IN ('Initial', 'InProgress', 'Received')");
                    }
                    if ((samplesDS = this.getQueryProcessor().getPreparedSqlDataSet(samplesDSsb.toString(), safeSQL.getValues())).size() > 0) {
                        props = new PropertyList();
                        props.setProperty("sdcid", "Sample");
                        props.setProperty("keyid1", samplesDS.getColumnValues("s_sampleid", ";"));
                        props.setProperty("samplestatus", "Cancelled");
                        props.setProperty("statusrollup", "false");
                        this.getActionProcessor().processAction("EditSDI", "1", props);
                    }
                    safeSQL.reset();
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdidata.sdcid, sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset FROM sdidata, rsetitems rs WHERE sdidata.sdcid = 'LV_BatchStage'  AND sdidata.keyid1=rs.keyid1 AND rs.rsetid=" + safeSQL.addVar(rsetId) + " AND (sdidata.sourceworkitemid is null OR sdidata.sourceworkitemid  = '') ", safeSQL.getValues());
                    if (ds.size() > 0) {
                        props = new PropertyList();
                        props.setProperty("sdcid", "LV_BatchStage");
                        props.setProperty("keyid1", ds.getColumnValues("keyid1", ";"));
                        props.setProperty("s_datasetstatus", "Cancelled");
                        props.setProperty("paramlistid", ds.getColumnValues("paramlistid", ";"));
                        props.setProperty("paramlistversionid", ds.getColumnValues("paramlistversionid", ";"));
                        props.setProperty("variantid", ds.getColumnValues("variantid", ";"));
                        props.setProperty("dataset", ds.getColumnValues("dataset", ";"));
                        if (batchCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                            props.setProperty("editincompleteonly", "Y");
                        }
                        this.getActionProcessor().processAction("EditDataSet", "1", props);
                    }
                    StringBuffer sdiwisb = new StringBuffer();
                    safeSQL.reset();
                    sdiwisb.append("SELECT sdiworkitem.sdcid, sdiworkitem.keyid1, sdiworkitem.keyid2, sdiworkitem.keyid3, sdiworkitem.workitemid, sdiworkitem.workiteminstance ").append(" FROM sdiworkitem, rsetitems rs WHERE sdiworkitem.sdcid = 'LV_BatchStage' ").append(" AND sdiworkitem.keyid1=rs.keyid1 ").append(" AND rs.rsetid=").append(safeSQL.addVar(rsetId));
                    if (batchCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                        sdiwisb.append(" AND sdiworkitem.workitemstatus IN ('Initial', 'InProgress', 'DataEntered', 'Released')");
                    }
                    if ((wids = this.getQueryProcessor().getPreparedSqlDataSet(sdiwisb.toString(), safeSQL.getValues())).size() > 0) {
                        props = new PropertyList();
                        props.setProperty("sdcid", "LV_BatchStage");
                        props.setProperty("keyid1", wids.getColumnValues("keyid1", ";"));
                        props.setProperty("propsmatch", "Y");
                        props.setProperty("workitemid", wids.getColumnValues("workitemid", ";"));
                        props.setProperty("workiteminstance", wids.getColumnValues("workiteminstance", ";"));
                        props.setProperty("syncsdistatus", "N");
                        props.setProperty("syncsdiworkitemgroupstatus", "N");
                        this.getActionProcessor().processAction("CancelSDIWorkItem", "1", props);
                    }
                    if (uniqueBatchIdWhoseStageCancelled.size() > 0) {
                        StringBuilder batchIdBuffer = new StringBuilder();
                        for (String batchId : uniqueBatchIdWhoseStageCancelled) {
                            batchIdBuffer.append(";").append(batchId);
                        }
                        if (batchIdBuffer.length() > 0) {
                            String traceLogId = actionProps.getProperty("tracelogid", "");
                            props = new PropertyList();
                            props.setProperty("sdcid", "Batch");
                            props.setProperty("keyid1", batchIdBuffer.substring(1));
                            props.setProperty("operation", "synchronizeonly");
                            if (traceLogId.length() > 0) {
                                props.setProperty("tracelogid", traceLogId);
                            }
                            props.setProperty("auditreason", "Stage Cancelled.");
                            this.getActionProcessor().processAction("EditSDI", "1", props);
                        }
                    }
                    this.getDAMProcessor().clearRSet(rsetId);
                }
            }
            String uncancelledBatchStages = "";
            StringBuilder batchIdBuffer = new StringBuilder();
            for (i = 0; i < primary.getRowCount(); ++i) {
                batchStageStatus = primary.getValue(i, "batchstagestatus", "");
                String initialBatchStageStatus = initialPrimary.getValue(i, "batchstagestatus", "");
                String batchStageId = primary.getValue(i, "s_batchstageid", "");
                String batchId = primary.getValue(i, "batchid", this.getOldPrimaryValue(primary, i, "batchid"));
                if (!this.hasPrimaryValueChanged(primary, i, "batchstagestatus") || !initialBatchStageStatus.equals("Cancelled")) continue;
                uncancelledBatchStages = uncancelledBatchStages + ";" + batchStageId;
                if (batchIdBuffer.indexOf(batchId) >= 0 || !batchStatusRollup) continue;
                batchIdBuffer.append(";").append(batchId);
            }
            if (uncancelledBatchStages.length() > 0) {
                safeSQL.reset();
                String rsetId = BaseSDIDataAction.createBypassSecurityRSet("LV_BatchStage", uncancelledBatchStages.substring(1), null, null, this.database, this.connectionInfo, false);
                DataSet samplesDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_sample.s_sampleid FROM s_sample, rsetitems rs WHERE s_sample.batchstageid=rs.keyid1 AND rs.rsetid=" + safeSQL.addVar(rsetId) + " AND s_sample.samplestatus = 'Cancelled' ", safeSQL.getValues());
                if (samplesDS.size() > 0) {
                    props = new PropertyList();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", samplesDS.getColumnValues("s_sampleid", ";"));
                    props.setProperty("statuscolumn", "samplestatus");
                    props.setProperty("validatestatuscolumnvalue", "Cancelled");
                    props.setProperty("statusrollup", "false");
                    this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                }
                safeSQL.reset();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdidata.sdcid, sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset FROM sdidata, rsetitems rs WHERE sdidata.sdcid = 'LV_BatchStage' and sdidata.keyid1=rs.keyid1 AND rs.rsetid=" + safeSQL.addVar(rsetId) + " AND sdidata.s_datasetstatus = 'Cancelled'", safeSQL.getValues());
                if (ds.size() > 0) {
                    ArrayList<DataSet> groupedDS = ds.getGroupedDataSets("sdcid,keyid1,keyid2,keyid3");
                    for (int j = 0; j < groupedDS.size(); ++j) {
                        DataSet revertDS = groupedDS.get(j);
                        props = new PropertyList();
                        props.setProperty("sdcid", revertDS.getValue(0, "sdcid", ""));
                        props.setProperty("keyid1", revertDS.getValue(0, "keyid1", ""));
                        props.setProperty("keyid2", revertDS.getValue(0, "keyid2", ""));
                        props.setProperty("keyid3", revertDS.getValue(0, "keyid3", ""));
                        props.setProperty("statuscolumn", "s_datasetstatus");
                        props.setProperty("validatestatuscolumnvalue", "Cancelled");
                        props.setProperty("paramlistid", revertDS.getColumnValues("paramlistid", ";"));
                        props.setProperty("paramlistversionid", revertDS.getColumnValues("paramlistversionid", ";"));
                        props.setProperty("variantid", revertDS.getColumnValues("variantid", ";"));
                        props.setProperty("dataset", revertDS.getColumnValues("dataset", ";"));
                        this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                    }
                }
                StringBuffer sdiwisb = new StringBuffer();
                safeSQL.reset();
                sdiwisb.append("SELECT sdiworkitem.sdcid, sdiworkitem.keyid1, sdiworkitem.keyid2, sdiworkitem.keyid3, sdiworkitem.workitemid, sdiworkitem.workiteminstance").append(" FROM sdiworkitem, rsetitems rs WHERE sdiworkitem.sdcid = 'LV_BatchStage' and sdiworkitem.keyid1=rs.keyid1").append(" AND rs.rsetid=").append(safeSQL.addVar(rsetId)).append(" AND sdiworkitem.workitemstatus = 'Cancelled'");
                DataSet wids = this.getQueryProcessor().getPreparedSqlDataSet(sdiwisb.toString(), safeSQL.getValues());
                if (wids.size() > 0) {
                    props = new PropertyList();
                    props.setProperty("sdcid", wids.getValue(0, "sdcid", ""));
                    props.setProperty("keyid1", wids.getColumnValues("keyid1", ";"));
                    props.setProperty("keyid2", wids.getColumnValues("keyid2", ";"));
                    props.setProperty("keyid3", wids.getColumnValues("keyid3", ";"));
                    props.setProperty("statuscolumn", "workitemstatus");
                    props.setProperty("validatestatuscolumnvalue", "Cancelled");
                    props.setProperty("workitemid", wids.getColumnValues("workitemid", ";"));
                    props.setProperty("workiteminstance", wids.getColumnValues("workiteminstance", ";"));
                    this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                }
                if (batchIdBuffer.length() > 0) {
                    String traceLogId = actionProps.getProperty("tracelogid", "");
                    props = new PropertyList();
                    props.setProperty("sdcid", "Batch");
                    props.setProperty("keyid1", batchIdBuffer.substring(1));
                    props.setProperty("operation", "synchronizeonly");
                    if (traceLogId.length() > 0) {
                        props.setProperty("tracelogid", traceLogId);
                    }
                    props.setProperty("auditreason", "Stage Uncancelled.");
                    this.getActionProcessor().processAction("EditSDI", "1", props);
                }
                this.getDAMProcessor().clearRSet(rsetId);
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setBatchStageStatus(primary, actionProps);
    }

    private void setBatchStageStatus(DataSet primary, PropertyList actionProps) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "batchstagestatus") || !BATCHSTAGE_RELEASED.equals(primary.getString(i, "batchstagestatus"))) continue;
            String releasedby = primary.getString(i, "releasedby", "");
            String releaseddt = primary.getValue(i, "releaseddt", "");
            if (releasedby.length() != 0 || releaseddt.length() != 0) continue;
            primary.addColumn("releasedby", 0);
            primary.addColumn("releaseddt", 2);
            primary.setValue(i, "releasedby", this.getConnectionInfo().getSysuserId());
            primary.setDate(i, "releaseddt", new M18NUtil(this.connectionInfo).format(DateTimeUtil.getNowCalendar()));
        }
    }
}

