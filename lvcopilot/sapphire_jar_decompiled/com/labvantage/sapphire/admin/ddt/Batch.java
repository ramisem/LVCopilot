/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.actions.CopySDIDetail;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.admin.ddt.BatchLifeCycleUtil;
import com.labvantage.sapphire.admin.ddt.misc.WhoDoneIt;
import com.labvantage.sapphire.modules.reagent.ReagentUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.util.samplingplan.SamplingPlanUtil;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Batch
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 102116 $";
    public static final String SDCID = "Batch";
    public static final String COLUMN_BATCHSTATUS = "batchstatus";
    private boolean addMode = false;
    public static final String MODE_SPLIT = "Split";
    public static final String MODE_LINK = "Link";
    public static final String MODE_COMPOSITE = "Composite";
    public static final String MODE_FORMULATION = "Formulation";
    public static final String MODE_ADHOC = "Primary";
    public static final String MODE_PROMOTEDFORMULATION = "PromotedFormulation";
    public static final String COLUMN_LEVELID = "levelid";
    public static final String COLUMN_PRODUCTID = "productid";
    public static final String COLUMN_PRODUCTVERSIONID = "productversionid";
    public static final String COLUMN_S_BATCHID = "s_batchid";
    public static final String COLUMN_SAMPLINGPLANID = "samplingplanid";
    public static final String COLUMN_SAMPLINGPLANVERSIONID = "samplingplanversionid";
    private String ruleid = "BatchRule";
    public String COLUMN_MIXINGBATCHSIZE = "expectedbatchsize";
    public String COLUMN_MIXINGBATCHSIZEUNITS = "expectedbatchsizeunits";
    public String COLUMN_FINISHEDBATCHSIZE = "batchsize";
    public String COLUMN_FINISHEDBATCHSIZEUNITS = "batchsizeunits";
    public String BATCHGENEALOGY_LOWERBOUND = "amountlowertolerance";
    public String BATCHGENEALOGY_UPPERBOUND = "amountuppertolerance";
    public String PARAMTYPE_QUANTITY = "Quantity";
    public String PARAMTYPE_FRACTION = "Fraction";
    public String PARAMTYPE_LOWERBOUND = "Lower Tolerance";
    public String PARAMTYPE_UPPERBOUND = "Upper Tolerance";
    private String SDIDATAITEM_VALUEFIELD = "transformvalue";
    private String SDIDATAITEM_UNITFIELD = "displayunits";
    private String FORMULATIONSTATUS_PENIDNG = "Pending";
    private String FORMULATIONSTATUS_COMPLETED = "Completed";
    private FormatUtil formatUtil;
    private Set<String> batchesWithSimpleProduct;
    private static WhoDoneIt whoDoneIt = new WhoDoneIt();

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        DataSet primary = sdiData.getDataset("primary");
        if (actionProps.getProperty("templateid").length() == 0 && actionProps.getProperty("templatekeyid1").length() == 0 && !actionProps.getProperty("templateflag").equalsIgnoreCase("Y") && !actionProps.getProperty("batchmode", "").equalsIgnoreCase(MODE_FORMULATION) && policy != null && policy.getProperty("syncbatchstate", "N").equalsIgnoreCase("Y")) {
            this.setBatchStatus("initial", primary, policy, actionProps);
        }
    }

    private void setBatchMode(DataSet primary) {
        HashMap<String, DataSet> productMap = new HashMap<String, DataSet>();
        HashMap<String, DataSet> parentProductMap = new HashMap<String, DataSet>();
        for (int i = 0; i < primary.size(); ++i) {
            String batchMode = primary.getValue(i, "batchmode", MODE_ADHOC);
            String productid = primary.getValue(i, COLUMN_PRODUCTID);
            String productversionid = primary.getValue(i, COLUMN_PRODUCTVERSIONID);
            if (!MODE_ADHOC.equalsIgnoreCase(batchMode) || productid.length() <= 0 || productversionid.length() <= 0) continue;
            String tempKey = productid + "_" + productversionid;
            DataSet productDS = (DataSet)productMap.get(tempKey);
            if (productDS == null) {
                String sql = "SELECT parentproductid, formulationiterationflag, formulationprojectid FROM s_product WHERE s_productid=? AND s_productversionid=?";
                productDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{productid, productversionid});
                productMap.put(tempKey, productDS);
            }
            if (productDS.size() <= 0 || productDS.getRowCount() <= 0) continue;
            String parentProductId = productDS.getString(0, "parentproductid", "");
            String formulationIterationFlag = productDS.getString(0, "formulationiterationflag", "N");
            String formulationProjectId = productDS.getString(0, "formulationprojectid", "");
            if (parentProductId.length() > 0 && formulationIterationFlag.equals("N") && formulationProjectId.length() > 0) {
                primary.setValue(i, "batchmode", MODE_PROMOTEDFORMULATION);
                continue;
            }
            DataSet parentProductDS = (DataSet)parentProductMap.get(tempKey);
            if (parentProductDS == null) {
                String parentProductSQL = "SELECT parentproductid FROM s_productformulation WHERE s_productid=? AND s_productversionid=?";
                parentProductDS = this.getQueryProcessor().getPreparedSqlDataSet(parentProductSQL, new Object[]{productid, productversionid});
                parentProductMap.put(tempKey, parentProductDS);
            }
            if (parentProductDS.getRowCount() > 0) {
                primary.setValue(i, "batchmode", MODE_FORMULATION);
                continue;
            }
            primary.setValue(i, "batchmode", MODE_ADHOC);
        }
    }

    @Override
    public void preAddKey(DataSet primary, PropertyList actionProps) {
        for (int i = 0; i < primary.size(); ++i) {
            String productid = primary.getValue(i, COLUMN_PRODUCTID);
            String productversionid = primary.getValue(i, COLUMN_PRODUCTVERSIONID);
            if (productid.length() <= 0 || productversionid.length() != 0) continue;
            String sql = "SELECT s_productversionid FROM s_product WHERE s_productid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( s_productversionid as integer ) DESC";
            DataSet products = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{productid});
            if (products.size() <= 0) continue;
            productversionid = products.getValue(0, "s_productversionid");
            primary.setString(i, COLUMN_PRODUCTVERSIONID, productversionid);
        }
    }

    @Override
    public void postAddKey(DataSet primary, PropertyList actionProps) {
        this.setBatchMode(primary);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.addMode = true;
        DataSet primary = sdiData.getDataset("primary");
        this.createSamplesForBatch(sdiData, actionProps);
        this.createTrackItem(primary);
        HashMap<String, String> filter = new HashMap<String, String>();
        this.createFormulationRows(sdiData);
        filter.put("batchmode", MODE_FORMULATION);
        DataSet ds = primary.getFilteredDataSet(filter);
        if (ds.getRowCount() > 0) {
            this.editFormulationProdStatusOnAddBatch(ds);
        }
    }

    private void editFormulationProdStatusOnAddBatch(DataSet ds) throws SapphireException {
        DataSet products = new DataSet();
        try {
            for (int i = 0; i < ds.size(); ++i) {
                String productId = ds.getValue(i, COLUMN_PRODUCTID);
                String productVersionId = ds.getValue(i, COLUMN_PRODUCTVERSIONID);
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put(COLUMN_PRODUCTID, productId);
                filter.put(COLUMN_PRODUCTVERSIONID, productVersionId);
                if (products.findRow(filter) >= 0) continue;
                this.database.createPreparedResultSet("getformulation", "select 1  from s_product where s_productid = ? and s_productversionid = ? and formulationstatus = '" + this.FORMULATIONSTATUS_COMPLETED + "'", new String[]{productId, productVersionId});
                if (!this.database.getResultSet("getformulation").next()) continue;
                int r = products.addRow();
                products.setString(r, COLUMN_PRODUCTID, productId);
                products.setString(r, COLUMN_PRODUCTVERSIONID, productVersionId);
            }
            if (products.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Product");
                props.setProperty("keyid1", products.getColumnValues(COLUMN_PRODUCTID, ";"));
                props.setProperty("keyid2", products.getColumnValues(COLUMN_PRODUCTVERSIONID, ";"));
                props.setProperty("formulationstatus", this.FORMULATIONSTATUS_PENIDNG);
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeResultSet("getformulation");
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        DataSet primary = sdiData.getDataset("primary");
        if (policy != null) {
            if (policy.getProperty("syncbatchstate", "N").equalsIgnoreCase("Y")) {
                String operation = actionProps.getProperty("operation", "");
                Logger.logInfo("In preEdit. operation = " + operation);
                if (operation.length() > 0) {
                    this.setBatchStatus(actionProps.getProperty("operation"), primary, policy, actionProps);
                } else {
                    boolean releaseValidationRequired = actionProps.getProperty("releasevalidationrequired", "Y").equals("Y");
                    this.releaseOrReject(primary, policy, releaseValidationRequired);
                }
            }
            if (!actionProps.getProperty("operation").equals("synchronizeonly") && !actionProps.getProperty("auditreason").equals("Stage Cancelled.")) {
                this.cancelChildren(primary, policy, "UnCancel");
            }
        }
        whoDoneIt.process(primary, this);
    }

    private void setBatchStatus(String operation, DataSet primary, PropertyList policy, PropertyList actionProps) throws SapphireException {
        HashMap props;
        StringBuffer receiveBypassBatches = new StringBuffer();
        boolean deferOperationsOnReceive = this.deferOperationsOnReceiveRequired(policy, operation);
        for (int i = 0; i < primary.getRowCount(); ++i) {
            this.validateCurrentStatus(primary, i, actionProps);
            String status = "";
            status = deferOperationsOnReceive ? BatchLifeCycleUtil.getBatchStateDisplayValue("received") : this.getStatusForOperation(operation, primary, i, policy, false, actionProps);
            if (status == null) continue;
            if (!primary.isValidColumn(COLUMN_BATCHSTATUS)) {
                primary.addColumn(COLUMN_BATCHSTATUS, 0);
            }
            primary.setValue(i, COLUMN_BATCHSTATUS, status);
            if (operation.equalsIgnoreCase("receive")) {
                if (!status.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("received"))) {
                    receiveBypassBatches.append(";").append(primary.getValue(i, COLUMN_S_BATCHID, ""));
                }
                if (!primary.isValidColumn("receivedby")) {
                    primary.addColumn("receivedby", 0);
                }
                if (!primary.isValidColumn("receiveddt")) {
                    primary.addColumn("receiveddt", 2);
                }
                primary.setValue(i, "receivedby", this.getConnectionInfo().getSysuserId());
                primary.setDate(i, "receiveddt", new M18NUtil(this.connectionInfo).getNowCalendar());
            }
            if (operation.equalsIgnoreCase("preliminaryrelease")) {
                props = new HashMap();
                props.put("sdcid", SDCID);
                props.put("keyid1", primary.getValue(i, COLUMN_S_BATCHID, ""));
                props.put("pendingapprovalstatus", status);
                props.put("approvalstatus", status);
                props.put("approvalstatuscolumn", COLUMN_BATCHSTATUS);
                props.put("__bypasseventplan", "Y");
                this.getActionProcessor().processAction("SubmitSDIForApproval", "1", props);
            }
            if (!operation.equalsIgnoreCase("synchronizeonly") || status.equals(BatchLifeCycleUtil.getBatchStateDisplayValue("released"))) continue;
            if (!primary.isValidColumn("releasedby")) {
                primary.addColumn("releasedby", 0);
            }
            if (primary.isValidColumn("releaseddt")) continue;
            primary.addColumn("releaseddt", 2);
        }
        boolean releaseValidationRequired = actionProps.getProperty("releasevalidationrequired", "Y").equals("Y");
        this.releaseOrReject(primary, policy, releaseValidationRequired);
        if (receiveBypassBatches.length() > 0) {
            Logger.logInfo("In setBatchStatus(195). receiveBypassBatches = " + receiveBypassBatches.substring(1));
            String sdcId = SDCID;
            props = new PropertyList();
            props.put("sdcid", sdcId);
            props.put("keyid1", receiveBypassBatches.substring(1));
            props.put(COLUMN_BATCHSTATUS, BatchLifeCycleUtil.getBatchStateDisplayValue("received"));
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
    }

    private void validateCurrentStatus(DataSet primary, int primaryIndex, PropertyList actionProps) throws SapphireException {
        String currentStatus = primary.getValue(primaryIndex, COLUMN_BATCHSTATUS, this.getOldPrimaryValue(primary, primaryIndex, COLUMN_BATCHSTATUS));
        String[] validationStatus = StringUtil.split(actionProps.getProperty("validationstatus", ""), ",");
        String validationOperator = actionProps.getProperty("validationoperator", "IN");
        if (validationStatus.length > 0) {
            if (validationOperator.equals("IN")) {
                for (int i = 0; i < validationStatus.length; ++i) {
                    if (validationStatus[i].length() <= 0 || validationStatus[i].equals(currentStatus)) continue;
                    throw new SapphireException(this.getTranslationProcessor().translate("The batch has to be in ") + this.getTranslationProcessor().translate(actionProps.getProperty("validationstatus", "")) + this.getTranslationProcessor().translate(" for this operation"));
                }
            } else if (validationOperator.equals("NOT_IN")) {
                // empty if block
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private String getStatusForOperation(String operation, DataSet primary, int primaryIndex, PropertyList policy, boolean deferBatchReceive, PropertyList actionProps) throws SapphireException {
        HashMap<String, String> valueMap;
        String skipLevel = policy.getProperty("skiplevel", "");
        String status = null;
        String batchid = primary.getValue(primaryIndex, COLUMN_S_BATCHID, "");
        String currentStatus = primary.getValue(primaryIndex, COLUMN_BATCHSTATUS, BatchLifeCycleUtil.getBatchStateDisplayValue("initial"));
        String batchLevelid = "";
        if (primary.isValidColumn(COLUMN_LEVELID)) {
            batchLevelid = primary.getValue(primaryIndex, COLUMN_LEVELID, "");
        } else {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT levelid FROM s_batch WHERE s_batchid = " + safeSQL.addVar(batchid);
            batchLevelid = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues()).getValue(0, COLUMN_LEVELID, "");
        }
        if (operation.equalsIgnoreCase("initial")) {
            String batchMode = primary.getString(primaryIndex, "batchmode", "");
            if (MODE_SPLIT.equalsIgnoreCase(batchMode)) return status;
            if (MODE_LINK.equalsIgnoreCase(batchMode)) return status;
            if (MODE_COMPOSITE.equalsIgnoreCase(batchMode)) return status;
            if (BatchLifeCycleUtil.isStateTransitionValid(BatchLifeCycleUtil.getBatchStateId(currentStatus), "initial")) {
                return BatchLifeCycleUtil.getBatchStateDisplayValue("initial");
            }
            HashMap<String, String> valueMap2 = new HashMap<String, String>();
            valueMap2.put("state1", currentStatus);
            valueMap2.put("state2", BatchLifeCycleUtil.getBatchStateDisplayValue("initial"));
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot change the batch state from [state1] to [state2]", valueMap2));
        }
        if (operation.equalsIgnoreCase("receive")) {
            status = this.determineAndSetBatchStatus(primary, primaryIndex, policy, skipLevel, batchid, batchLevelid, false, operation);
            if (status == null && (deferBatchReceive || BatchLifeCycleUtil.isStateTransitionValid(BatchLifeCycleUtil.getBatchStateId(currentStatus), "received"))) {
                status = BatchLifeCycleUtil.getBatchStateDisplayValue("received");
            }
            if (status != null) return status;
            valueMap = new HashMap();
            valueMap.put("state1", currentStatus);
            valueMap.put("state2", BatchLifeCycleUtil.getBatchStateDisplayValue("received"));
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot change the batch state from [state1] to [state2]", valueMap));
        }
        if (operation.equalsIgnoreCase("onhold")) {
            if (BatchLifeCycleUtil.isStateTransitionValid(BatchLifeCycleUtil.getBatchStateId(currentStatus), "onhold")) {
                return BatchLifeCycleUtil.getBatchStateDisplayValue("onhold");
            }
            valueMap = new HashMap<String, String>();
            valueMap.put("state1", currentStatus);
            valueMap.put("state2", BatchLifeCycleUtil.getBatchStateDisplayValue("onhold"));
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot change the batch state from [state1] to [state2]", valueMap));
        }
        if (operation.equalsIgnoreCase("preliminaryrelease")) {
            if (BatchLifeCycleUtil.isStateTransitionValid(BatchLifeCycleUtil.getBatchStateId(currentStatus), "preliminaryrelease")) {
                return BatchLifeCycleUtil.getBatchStateDisplayValue("preliminaryrelease");
            }
            valueMap = new HashMap();
            valueMap.put("state1", currentStatus);
            valueMap.put("state2", BatchLifeCycleUtil.getBatchStateDisplayValue("preliminaryrelease"));
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot change the batch state from [state1] to [state2]", valueMap));
        }
        if (operation.equalsIgnoreCase("reject")) {
            if (BatchLifeCycleUtil.isStateTransitionValid(BatchLifeCycleUtil.getBatchStateId(currentStatus), "rejected")) {
                return BatchLifeCycleUtil.getBatchStateDisplayValue("rejected");
            }
            valueMap = new HashMap();
            valueMap.put("state1", currentStatus);
            valueMap.put("state2", BatchLifeCycleUtil.getBatchStateDisplayValue("rejected"));
            throw new SapphireException(this.getTranslationProcessor().translate("Cannot change the batch state from [state1] to [state2]", valueMap));
        }
        if (!operation.equalsIgnoreCase("unhold")) {
            String batchStatus;
            if (!operation.equalsIgnoreCase("synchronizeonly")) return status;
            String currentStateValidation = actionProps.getProperty("currentstatevalidation", "");
            boolean currentStateValidationFlag = true;
            if (currentStateValidation.length() > 0 && !currentStateValidation.equals(batchStatus = primary.getValue(primaryIndex, COLUMN_BATCHSTATUS, this.getOldPrimaryValue(primary, primaryIndex, COLUMN_BATCHSTATUS)))) {
                currentStateValidationFlag = false;
            }
            if (!currentStateValidationFlag) return status;
            return this.determineAndSetBatchStatus(primary, primaryIndex, policy, skipLevel, batchid, batchLevelid, false, operation);
        }
        status = this.determineAndSetBatchStatus(primary, primaryIndex, policy, skipLevel, batchid, batchLevelid, true, operation);
        if (status != null) return status;
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", SDCID);
        props.put("keyid1", batchid);
        props.put("statuscolumn", COLUMN_BATCHSTATUS);
        props.put("validatestatuscolumnvalue", BatchLifeCycleUtil.getBatchStateDisplayValue("onhold"));
        props.put("auditactivity", actionProps.getProperty("auditactivity"));
        props.put("auditreason", actionProps.getProperty("auditreason"));
        props.put("auditsignedflag", actionProps.getProperty("auditsignedflag"));
        props.put("tracelogid", String.valueOf(actionProps.getProperty("tracelogid", "")));
        this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
        return status;
    }

    private String determineAndSetBatchStatus(DataSet primary, int primaryIndex, PropertyList policy, String skipLevel, String batchid, String batchLevelid, boolean undoOperation, String operation) throws SapphireException {
        String status = null;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT COUNT(*) samplesinbatch FROM s_batch b, s_sample s ");
        sql.append(" WHERE b.s_batchid = ").append(safeSQL.addVar(batchid));
        sql.append(" AND s.batchid = b.s_batchid ");
        int noOfsamplesInBatch = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues()).getInt(0, "samplesinbatch", 0);
        if (batchLevelid.length() > 0 && skipLevel.equals(batchLevelid) && noOfsamplesInBatch == 0) {
            status = this.submitBatchForApproval(batchid, BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease"), BatchLifeCycleUtil.getBatchStateDisplayValue("released"));
            Logger.logInfo("In getStatusForOperation. batchid = " + batchid + ". Skip batch");
        } else if (undoOperation && noOfsamplesInBatch == 0) {
            status = null;
        } else {
            if (BatchLifeCycleUtil.hasAllTestsFinished(batchid, this.getQueryProcessor())) {
                String releasedStatus = BatchLifeCycleUtil.getBatchStateDisplayValue("released");
                String pendingReleaseStatus = BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease");
                PropertyList batchSamplepolicy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
                String batchApprovalPolicy = batchSamplepolicy.getProperty("batchapprovalwithincidents", "Allow");
                boolean sendToPendingReleaseList = false;
                if ((batchApprovalPolicy.equalsIgnoreCase("block") || batchApprovalPolicy.equalsIgnoreCase("warn")) && BatchLifeCycleUtil.hasOpenIncidents(batchid, this.getQueryProcessor())) {
                    sendToPendingReleaseList = true;
                    releasedStatus = pendingReleaseStatus;
                }
                status = this.submitBatchForApproval(batchid, pendingReleaseStatus, releasedStatus);
                this.logger.debug("In getStatusForOperation. batchid = " + batchid + ". status = " + status);
                if ((skipLevel != null || skipLevel.length() > 0) && !sendToPendingReleaseList && this.isToBeAutoReleased(batchid, policy)) {
                    Logger.logInfo("In getStatusForOperation. batchid = " + batchid + ". AutoReleased");
                    if (!primary.isValidColumn("releasedby")) {
                        primary.addColumn("releasedby", 0);
                    }
                    primary.setValue(primaryIndex, "releasedby", "(system)");
                    status = BatchLifeCycleUtil.getBatchStateDisplayValue("released");
                }
            }
            if (status == null) {
                safeSQL.reset();
                sql = new StringBuffer("SELECT s.samplestatus, b.batchtype FROM s_sample s, s_batch b WHERE s.batchid=b.s_batchid and ");
                sql.append(" batchid = ").append(safeSQL.addVar(batchid));
                DataSet sampleBatchStatus = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                String previousBatchReceivedDt = this.getOldPrimaryValue(primary, primaryIndex, "receiveddt");
                for (int i = 0; i < sampleBatchStatus.getRowCount(); ++i) {
                    String sampleStatus = sampleBatchStatus.getValue(i, "samplestatus", "");
                    String batchType = sampleBatchStatus.getValue(i, "batchtype", "");
                    if (sampleStatus.equals("Initial") && !batchType.equals("Finished")) continue;
                    status = BatchLifeCycleUtil.getBatchStateDisplayValue("active");
                    break;
                }
                if (status == null && operation.equals("synchronizeonly")) {
                    status = previousBatchReceivedDt.length() > 0 ? BatchLifeCycleUtil.getBatchStateDisplayValue("received") : BatchLifeCycleUtil.getBatchStateDisplayValue("initial");
                }
            }
        }
        return status;
    }

    private String submitBatchForApproval(String batchid, String pendingapprovalstatus, String approvalstatus) throws SapphireException {
        String sdcid = SDCID;
        SafeSQL safeSQL = new SafeSQL();
        String sqlGetApprovals = "select count(*) CNT from sdiapproval where sdcid = 'Batch' and keyid1 = " + safeSQL.addVar(batchid);
        QueryProcessor qp = this.getQueryProcessor();
        ActionProcessor ap = this.getActionProcessor();
        DataSet dsGetApprovals = qp.getPreparedSqlDataSet(sqlGetApprovals, safeSQL.getValues());
        int approvalCount = dsGetApprovals.getInt(0, "CNT", 0);
        if (approvalCount == 0) {
            return approvalstatus;
        }
        PropertyList plProps = new PropertyList();
        plProps.setProperty("sdcid", sdcid);
        plProps.setProperty("keyid1", batchid);
        plProps.setProperty("ready", "Y");
        ap.processAction("ResetSDIApproval", "1", plProps);
        return pendingapprovalstatus;
    }

    /*
     * Enabled aggressive block sorting
     */
    private void releaseOrReject(DataSet primary, PropertyList policy, boolean releaseValidationRequired) throws SapphireException {
        String releasedby = this.getConnectionInfo().getSysuserId();
        Calendar releaseddt = new M18NUtil(this.connectionInfo).getNowCalendar();
        String dispositionPassValue = "Passed";
        String dispositionFailValue = "Failed";
        int i = 0;
        while (true) {
            String batchid;
            String batchStatus;
            block21: {
                if (i >= primary.getRowCount()) {
                    return;
                }
                batchStatus = primary.getValue(i, COLUMN_BATCHSTATUS, "");
                batchid = primary.getValue(i, COLUMN_S_BATCHID, "");
                if (batchStatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("released"))) {
                    if (this.hasPrimaryValueChanged(primary, i, COLUMN_BATCHSTATUS)) {
                        if (releaseValidationRequired && !BatchLifeCycleUtil.hasAllTestsFinished(batchid, this.getQueryProcessor())) {
                            throw new SapphireException("ALL_SAMPLES_NOT_FINISHED: " + this.getTranslationProcessor().translate("Cannot release the batch") + " " + batchid + ". " + this.getTranslationProcessor().translate("All testing has to be finished before releasing."));
                        }
                        int parentReleasedCount = BatchLifeCycleUtil.isParentBatchesReleased(batchid, this.getQueryProcessor(), policy);
                        if (!releaseValidationRequired || parentReleasedCount == 0) {
                            boolean syncBatchReleaseWithStage;
                            String syncBatchReleaseWithStageString = policy.getProperty("syncbatchwithstage", "N");
                            boolean bl = syncBatchReleaseWithStage = !syncBatchReleaseWithStageString.equalsIgnoreCase("N");
                            if (!releaseValidationRequired || !syncBatchReleaseWithStage || syncBatchReleaseWithStage && BatchLifeCycleUtil.isChildStagesReleased(batchid, this.getQueryProcessor(), syncBatchReleaseWithStageString)) {
                                if (!primary.isValidColumn("releasedby")) {
                                    primary.addColumn("releasedby", 0);
                                }
                                if (!primary.isValidColumn("releaseddt")) {
                                    primary.addColumn("releaseddt", 2);
                                }
                                if (!primary.isValidColumn("disposition")) {
                                    primary.addColumn("disposition", 0);
                                }
                                if (!primary.getValue(i, "releasedby", "").equals("(system)") && !primary.getValue(i, "releasedby", "").equals("AutoAP")) {
                                    Logger.logInfo("The batch is not auto released but a normal release. batchid = " + batchid);
                                    primary.setValue(i, "releasedby", releasedby);
                                }
                                primary.setDate(i, "releaseddt", releaseddt);
                                primary.setValue(i, "disposition", dispositionPassValue);
                                break block21;
                            } else {
                                throw new SapphireException("CHILD_STAGES_NOT_RELEASED: " + this.getTranslationProcessor().translate("Cannot release the batch") + " " + batchid + ". " + this.getTranslationProcessor().translate("Child stages have to be released before proceeding."));
                            }
                        }
                        if (parentReleasedCount == 1) {
                            throw new SapphireException("PARENT_BATCH_NOT_RELEASED: " + this.getTranslationProcessor().translate("Cannot release the batch") + " " + batchid + ". " + this.getTranslationProcessor().translate("Parent batches have to be released before proceeding."));
                        }
                        throw new SapphireException("PARENT_BATCH_NOT_IDENTIFIED: " + this.getTranslationProcessor().translate("Cannot release the batch") + " " + batchid + ". " + this.getTranslationProcessor().translate("Parent batches have to be identified before proceeding."));
                    }
                } else if (batchStatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("rejected")) && this.hasPrimaryValueChanged(primary, i, COLUMN_BATCHSTATUS)) {
                    if (!primary.isValidColumn("disposition")) {
                        primary.addColumn("disposition", 0);
                    }
                    if (!primary.isValidColumn("releasedby")) {
                        primary.addColumn("releasedby", 0);
                    }
                    if (!primary.isValidColumn("releaseddt")) {
                        primary.addColumn("releaseddt", 2);
                    }
                    primary.setValue(i, "releasedby", releasedby);
                    primary.setDate(i, "releaseddt", releaseddt);
                    primary.setValue(i, "disposition", dispositionFailValue);
                }
            }
            String oldBatchStatus = this.getOldPrimaryValue(primary, i, COLUMN_BATCHSTATUS);
            if (batchStatus.length() > 0 && BatchLifeCycleUtil.getBatchStateDisplayValue("rejected").equalsIgnoreCase(oldBatchStatus) && !batchStatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("rejected"))) {
                primary.setString(i, "releasedby", "");
                primary.setDate(i, "releaseddt", "(null)");
                if (batchStatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease")) || batchStatus.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("preliminaryrelease"))) {
                    this.resetBatchApprovals(batchid, "Y");
                } else {
                    this.resetBatchApprovals(batchid, "N");
                }
            }
            ++i;
        }
    }

    private void resetBatchApprovals(String batchid, String ready) throws ActionException {
        ready = ready == null || ready.length() == 0 ? "N" : ready;
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", SDCID);
        props.put("keyid1", batchid);
        props.put("ready", ready);
        this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
    }

    private boolean isToBeAutoReleased(String batchid, PropertyList policy) throws SapphireException {
        String skipLevel = policy.getProperty("skiplevel", "");
        boolean returnval = false;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT COUNT(*) autoreleaseflag FROM s_product, s_batch WHERE ");
        sql.append(" s_batch.s_batchid = ").append(safeSQL.addVar(batchid));
        sql.append(" AND s_product.s_productid = s_batch.productid ");
        sql.append(" AND s_product.s_productversionid = s_batch.productversionid ");
        sql.append(" AND s_product.autoreleaseflag = 'Y' ");
        sql.append(" AND (s_batch.levelid IS NULL OR s_batch.levelid != ").append(safeSQL.addVar(skipLevel)).append(")");
        DataSet autoReleaseReqBatchDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (autoReleaseReqBatchDS != null && autoReleaseReqBatchDS.getInt(0, "autoreleaseflag") != 0) {
            PropertyListCollection specInterpretation = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getCollection("SpecConditions");
            Iterator iter = specInterpretation.iterator();
            String passCondition = "Pass";
            while (iter.hasNext()) {
                PropertyList condition = (PropertyList)iter.next();
                if (!condition.getProperty("interpretation").equals(passCondition)) continue;
                passCondition = condition.getProperty("SpecCond");
                break;
            }
            safeSQL.reset();
            sql = new StringBuffer("SELECT condition FROM sdispec WHERE sdcid = 'Sample' ");
            sql.append(" AND  keyid1 IN ( ");
            sql.append(" SELECT s_sampleid FROM s_sample WHERE batchid = " + safeSQL.addVar(batchid) + ")");
            DataSet specs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("condition", passCondition);
            DataSet passedSpecs = specs.getFilteredDataSet(filter);
            if (specs.getRowCount() > 0 && specs.getRowCount() == passedSpecs.getRowCount()) {
                returnval = true;
            }
        }
        return returnval;
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String sampleCreationOption;
        DataSet primary = sdiData.getDataset("primary");
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        if (policy != null && ((sampleCreationOption = policy.getProperty("samplecreationpolicy", "")).equalsIgnoreCase("Using Both") || sampleCreationOption.equalsIgnoreCase("Using Sampling Plan"))) {
            this.processSamplingPlan(primary, actionProps);
        }
        this.cancelChildren(primary, policy, "Cancel");
        String operation = actionProps.getProperty("operation", "");
        boolean deferOperationsOnReceive = this.deferOperationsOnReceiveRequired(policy, operation);
        if (deferOperationsOnReceive) {
            this.defferedOperationOnReceive(primary, policy, operation, actionProps);
        }
        this.updateIngredientTargetAmount(primary);
        this.resetTrackItemAmount(actionProps);
        this.setFormulationProductToEvalComplete(primary);
        this.createTrackItem(primary);
        this.findCandidateForAutoRelease(primary, policy);
        String releasedStatus = BatchLifeCycleUtil.getBatchStateDisplayValue("released");
        String pendingReleaseStatus = BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease");
        ApprovalRuleUtil.checkSDIAutoApprovalRule(primary, actionProps, SDCID, COLUMN_BATCHSTATUS, pendingReleaseStatus, releasedStatus, "releasedby", "releaseddt", this.database, this.connectionInfo, this.logger, this.getSDCProcessor(), true);
    }

    private void setFormulationProductToEvalComplete(DataSet primary) throws SapphireException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(COLUMN_BATCHSTATUS, "Released");
        DataSet dsReleased = primary.getFilteredDataSet(map);
        DataSet products = new DataSet();
        for (int i = 0; i < dsReleased.getRowCount(); ++i) {
            String batchid = primary.getValue(i, COLUMN_S_BATCHID, "");
            this.database.createPreparedResultSet("getbatches", "select b1.productid, b1.productversionid, b1.batchstatus  from s_batch b1, s_batch b where b1.productid = b.productid and b1.productversionid = b.productversionid   and b1.batchmode = b.batchmode and b.s_batchid = ? and b.batchmode = 'Formulation'", new String[]{batchid});
            DataSet dsBatches = new DataSet(this.database.getResultSet("getbatches"));
            int batchCnt = dsBatches.getRowCount();
            if (batchCnt <= 0) continue;
            int validBatchCnt = 0;
            String productId = dsBatches.getValue(0, COLUMN_PRODUCTID);
            String productVersionId = dsBatches.getValue(0, COLUMN_PRODUCTVERSIONID);
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put(COLUMN_PRODUCTID, productId);
            filter.put(COLUMN_PRODUCTVERSIONID, productVersionId);
            if (products.findRow(filter) >= 0) continue;
            for (int b = 0; b < batchCnt; ++b) {
                String batchStatus = dsBatches.getValue(b, COLUMN_BATCHSTATUS);
                if (!"Cancelled".equalsIgnoreCase(batchStatus) && !"rejected".equalsIgnoreCase(batchStatus) && !"released".equalsIgnoreCase(batchStatus)) continue;
                ++validBatchCnt;
            }
            if (batchCnt != validBatchCnt) continue;
            int r = products.addRow();
            products.setString(r, COLUMN_PRODUCTID, productId);
            products.setString(r, COLUMN_PRODUCTVERSIONID, productVersionId);
        }
        this.database.closeResultSet("getbatches");
        if (products.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Product");
            props.setProperty("keyid1", products.getColumnValues(COLUMN_PRODUCTID, ";"));
            props.setProperty("keyid2", products.getColumnValues(COLUMN_PRODUCTVERSIONID, ";"));
            props.setProperty("formulationstatus", this.FORMULATIONSTATUS_COMPLETED);
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
    }

    private boolean deferOperationsOnReceiveRequired(PropertyList policy, String operation) {
        PropertyList pl;
        String sampleCreationOption;
        boolean deferBatchReceive = false;
        if (operation.equalsIgnoreCase("receive") && ((sampleCreationOption = policy.getProperty("samplecreationpolicy", "")).equalsIgnoreCase("Using Both") || sampleCreationOption.equalsIgnoreCase("Using Sampling Plan")) && (pl = SamplingPlanUtil.getPolicyPropertyList(this.getSdcid(), this.getConfigurationProcessor())) != null) {
            deferBatchReceive = pl.getProperty("applyevent", "").equalsIgnoreCase("On Receipt");
        }
        return deferBatchReceive;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void defferedOperationOnReceive(DataSet primary, PropertyList policy, String operation, PropertyList actionProps) throws SapphireException {
        String releasedby = this.getConnectionInfo().getSysuserId();
        Calendar releaseddt = new M18NUtil(this.connectionInfo).getNowCalendar();
        String dispositionPassValue = "Passed";
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("keyid1", "");
        props.put("sdcid", SDCID);
        props.put(COLUMN_BATCHSTATUS, "");
        props.put("releaseddt", "");
        props.put("releasedby", "");
        props.put("disposition", "");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String status = this.getStatusForOperation(operation, primary, i, policy, true, actionProps);
            if (status.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("received"))) continue;
            String batchid = primary.getValue(i, COLUMN_S_BATCHID, "");
            if (status.equalsIgnoreCase(BatchLifeCycleUtil.getBatchStateDisplayValue("released"))) {
                if (!BatchLifeCycleUtil.hasAllTestsFinished(batchid, this.getQueryProcessor())) throw new SapphireException(this.getTranslationProcessor().translate("Cannot release the batch") + " " + batchid + ". " + this.getTranslationProcessor().translate("All testing has to be finished before releasing."));
                int parentReleasedCount = BatchLifeCycleUtil.isParentBatchesReleased(batchid, this.getQueryProcessor(), policy);
                if (parentReleasedCount == 0) {
                    boolean syncBatchReleaseWithStage;
                    String syncBatchReleaseWithStageString = policy.getProperty("syncbatchwithstage", "N");
                    boolean bl = syncBatchReleaseWithStage = !syncBatchReleaseWithStageString.equalsIgnoreCase("N");
                    if (syncBatchReleaseWithStage && (!syncBatchReleaseWithStage || !BatchLifeCycleUtil.isChildStagesReleased(batchid, this.getQueryProcessor(), syncBatchReleaseWithStageString))) throw new SapphireException(this.getTranslationProcessor().translate("Cannot release the batch") + " " + batchid + ". " + this.getTranslationProcessor().translate("Child stages have to be released before proceeding."));
                    if (!primary.getValue(i, "releasedby", "").equals("(system)") && !primary.getValue(i, "releasedby", "").equals("AutoAP")) {
                        Logger.logInfo("The batch is not auto released but a normal release. batchid = " + batchid);
                        primary.setValue(i, "releasedby", releasedby);
                    }
                    primary.setDate(i, "releaseddt", releaseddt);
                    primary.setValue(i, "disposition", dispositionPassValue);
                } else {
                    if (parentReleasedCount != 1) throw new SapphireException(this.getTranslationProcessor().translate("Cannot release the batch") + " " + batchid + ". " + this.getTranslationProcessor().translate("Parent batches have to be identified before proceeding."));
                    throw new SapphireException(this.getTranslationProcessor().translate("Cannot release the batch") + " " + batchid + ". " + this.getTranslationProcessor().translate("Parent batches have to be released before proceeding."));
                }
            }
            props.put("keyid1", (String)props.get("keyid1") + ";" + batchid);
            props.put(COLUMN_BATCHSTATUS, (String)props.get(COLUMN_BATCHSTATUS) + ";" + status);
            props.put("releaseddt", (String)props.get("releaseddt") + ";" + primary.getValue(i, "releaseddt", "(null)"));
            props.put("releasedby", (String)props.get("releasedby") + ";" + primary.getValue(i, "releasedby", "(null)"));
            props.put("disposition", (String)props.get("disposition") + ";" + primary.getValue(i, "disposition", "(null)"));
        }
        if (((String)props.get("keyid1")).length() <= 0) return;
        props.put("keyid1", ((String)props.get("keyid1")).substring(1));
        props.put(COLUMN_BATCHSTATUS, ((String)props.get(COLUMN_BATCHSTATUS)).substring(1));
        props.put("releaseddt", ((String)props.get("releaseddt")).substring(1));
        props.put("releasedby", ((String)props.get("releasedby")).substring(1));
        props.put("disposition", ((String)props.get("disposition")).substring(1));
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void cancelChildren(DataSet primary, PropertyList batchSamplePolicy, String mode) throws SapphireException {
        DataSet initialPrimary = this.getBeforeEditImage().getDataset("primary");
        String batchCancelBehavior = batchSamplePolicy.getProperty("cancelactionbehavior", "");
        if (batchCancelBehavior.equalsIgnoreCase("Cancel all Children") || batchCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
            SafeSQL safeSQL = new SafeSQL();
            if (mode.equals("Cancel")) {
                if (primary.getColumnValues(COLUMN_BATCHSTATUS, ";").contains("Cancelled")) {
                    String cancelledBatchids = "";
                    for (int i = 0; i < primary.getRowCount(); ++i) {
                        String batchStatus = primary.getValue(i, COLUMN_BATCHSTATUS, "");
                        String batchid = primary.getValue(i, COLUMN_S_BATCHID, "");
                        if (!this.hasPrimaryValueChanged(primary, i, COLUMN_BATCHSTATUS) || !batchStatus.equals("Cancelled")) continue;
                        cancelledBatchids = cancelledBatchids + ";" + batchid;
                    }
                    if (cancelledBatchids.length() > 0) {
                        DataSet wids;
                        StringBuffer batchStagesb = new StringBuffer();
                        String rsetId = BaseSDIDataAction.createBypassSecurityRSet(SDCID, cancelledBatchids.substring(1), null, null, this.database, this.connectionInfo, false);
                        batchStagesb.append("SELECT s_batchstage.s_batchstageid FROM s_batchstage, rsetitems rs WHERE ").append(" s_batchstage.batchid=rs.keyid1 ").append(" AND rs.rsetid=").append(safeSQL.addVar(rsetId));
                        if (batchCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                            batchStagesb.append(" AND batchstagestatus IN ('Initial', 'InProgress')");
                        }
                        DataSet bsDS = this.getQueryProcessor().getPreparedSqlDataSet(batchStagesb.toString(), safeSQL.getValues());
                        StringBuffer samplesDSsb = new StringBuffer();
                        safeSQL.reset();
                        samplesDSsb.append("SELECT s_sample.s_sampleid FROM s_sample, rsetitems rs WHERE ").append(" s_sample.batchid=rs.keyid1 ").append(" AND rs.rsetid=").append(safeSQL.addVar(rsetId)).append(" AND (s_sample.batchstageid is null OR s_sample.batchstageid = '')");
                        if (batchCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                            samplesDSsb.append(" AND s_sample.samplestatus IN ('Initial', 'InProgress', 'Received')");
                        }
                        DataSet samplesDS = this.getQueryProcessor().getPreparedSqlDataSet(samplesDSsb.toString(), safeSQL.getValues());
                        PropertyList props = null;
                        if (bsDS != null && bsDS.size() > 0) {
                            props = new PropertyList();
                            props.setProperty("sdcid", "LV_BatchStage");
                            props.setProperty("keyid1", bsDS.getColumnValues("s_batchstageid", ";"));
                            props.setProperty("batchstagestatus", "Cancelled");
                            props.setProperty("statusrollup", "false");
                            this.getActionProcessor().processAction("EditSDI", "1", props);
                        }
                        if (samplesDS != null && samplesDS.size() > 0) {
                            props = new PropertyList();
                            props.setProperty("sdcid", "Sample");
                            props.setProperty("keyid1", samplesDS.getColumnValues("s_sampleid", ";"));
                            props.setProperty("samplestatus", "Cancelled");
                            props.setProperty("statusrollup", "false");
                            this.getActionProcessor().processAction("EditSDI", "1", props);
                        }
                        safeSQL.reset();
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdidata.sdcid, sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset  FROM sdidata, rsetitems rs WHERE sdidata.sdcid = 'Batch' and sdidata.keyid1 = rs.keyid1 AND rs.rsetid=" + safeSQL.addVar(rsetId) + " AND (sdidata.sourceworkitemid is null OR sdidata.sourceworkitemid  = '') ", safeSQL.getValues());
                        if (ds.size() > 0) {
                            props = new PropertyList();
                            props.setProperty("sdcid", SDCID);
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
                        sdiwisb.append("SELECT sdiworkitem.sdcid, sdiworkitem.keyid1, sdiworkitem.keyid2, sdiworkitem.keyid3, sdiworkitem.workitemid, sdiworkitem.workiteminstance ").append(" FROM sdiworkitem, rsetitems rs WHERE sdiworkitem.sdcid = 'Batch' and sdiworkitem.keyid1=rs.keyid1").append(" AND rs.rsetid=").append(safeSQL.addVar(rsetId));
                        if (batchCancelBehavior.equalsIgnoreCase("Cancel Incomplete Children")) {
                            sdiwisb.append(" AND sdiworkitem.workitemstatus IN ('Initial', 'InProgress', 'DataEntered', 'Released')");
                        }
                        if ((wids = this.getQueryProcessor().getPreparedSqlDataSet(sdiwisb.toString(), safeSQL.getValues())).size() > 0) {
                            props = new PropertyList();
                            props.setProperty("sdcid", SDCID);
                            props.setProperty("keyid1", wids.getColumnValues("keyid1", ";"));
                            props.setProperty("workitemid", wids.getColumnValues("workitemid", ";"));
                            props.setProperty("workiteminstance", wids.getColumnValues("workiteminstance", ";"));
                            props.setProperty("propsmatch", "Y");
                            props.setProperty("syncsdistatus", "N");
                            props.setProperty("syncsdiworkitemgroupstatus", "N");
                            this.getActionProcessor().processAction("CancelSDIWorkItem", "1", props);
                        }
                        this.getDAMProcessor().clearRSet(rsetId);
                    }
                }
            } else {
                String uncancelledBatchids = "";
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    String initialBatchStatus = initialPrimary.getValue(i, COLUMN_BATCHSTATUS, "");
                    String batchid = primary.getValue(i, COLUMN_S_BATCHID, "");
                    if (!this.hasPrimaryValueChanged(primary, i, COLUMN_BATCHSTATUS) || !initialBatchStatus.equals("Cancelled")) continue;
                    uncancelledBatchids = uncancelledBatchids + ";" + batchid;
                }
                if (uncancelledBatchids.length() > 0) {
                    safeSQL.reset();
                    String rsetId = BaseSDIDataAction.createBypassSecurityRSet(SDCID, uncancelledBatchids.substring(1), null, null, this.database, this.connectionInfo, false);
                    DataSet bsDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_batchstage.s_batchstageid FROM s_batchstage, rsetitems rs WHERE s_batchstage.batchid=rs.keyid1 AND rs.rsetid=" + safeSQL.addVar(rsetId) + " AND s_batchstage.batchstagestatus = 'Cancelled'", safeSQL.getValues());
                    safeSQL.reset();
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdidata.sdcid, sdidata.keyid1, sdidata.keyid2, sdidata.keyid3, sdidata.paramlistid, sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset  FROM sdidata, rsetitems rs WHERE sdidata.sdcid = 'Batch' and sdidata.keyid1=rs.keyid1 AND rs.rsetid=" + safeSQL.addVar(rsetId) + " AND sdidata.s_datasetstatus = 'Cancelled'", safeSQL.getValues());
                    PropertyList props = null;
                    safeSQL.reset();
                    DataSet samplesDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_sample.s_sampleid FROM s_sample, rsetitems rs WHERE s_sample.batchid=rs.keyid1 AND rs.rsetid=" + safeSQL.addVar(rsetId) + " AND (s_sample.batchstageid is null OR s_sample.batchstageid = '') AND s_sample.samplestatus = 'Cancelled'", safeSQL.getValues());
                    if (samplesDS.size() > 0) {
                        props = new PropertyList();
                        props.setProperty("sdcid", "Sample");
                        props.setProperty("keyid1", samplesDS.getColumnValues("s_sampleid", ";"));
                        props.setProperty("statuscolumn", "samplestatus");
                        props.setProperty("validatestatuscolumnvalue", "Cancelled");
                        props.setProperty("statusrollup", "false");
                        this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                    }
                    if (bsDS.size() > 0) {
                        props = new PropertyList();
                        props.setProperty("sdcid", "LV_BatchStage");
                        props.setProperty("keyid1", bsDS.getColumnValues("s_batchstageid", ";"));
                        props.setProperty("statuscolumn", "batchstagestatus");
                        props.setProperty("validatestatuscolumnvalue", "Cancelled");
                        props.setProperty("statusrollup", "false");
                        this.getActionProcessor().processAction("UndoSDIColumnValue", "1", props);
                    }
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
                    sdiwisb.append("SELECT sdiworkitem.sdcid, sdiworkitem.keyid1, sdiworkitem.keyid2, sdiworkitem.keyid3, sdiworkitem.workitemid, sdiworkitem.workiteminstance ").append("  FROM sdiworkitem, rsetitems rs WHERE sdiworkitem.sdcid = 'Batch' and sdiworkitem.keyid1=rs.keyid1").append(" AND rs.rsetid=").append(safeSQL.addVar(rsetId)).append(" AND sdiworkitem.workitemstatus = 'Cancelled'");
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
                    this.getDAMProcessor().clearRSet(rsetId);
                }
            }
        }
    }

    private void processSamplingPlan(DataSet primary, PropertyList actionProps) throws SapphireException {
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
        String samplingPlanLevel = actionProps.getProperty("samplingplanlevelid");
        String batchMode = actionProps.getProperty("batchmode");
        if (batchMode.equalsIgnoreCase(MODE_LINK) || (batchMode.equalsIgnoreCase(MODE_SPLIT) || batchMode.equalsIgnoreCase(MODE_COMPOSITE)) && samplingPlanLevel.length() == 0) {
            return;
        }
        if (this.addMode && samplingPlanLevel.length() > 0) {
            if (this.batchesWithSimpleProduct == null) {
                this.getBatchesWithSimpleProducts(primary);
            }
            StringBuffer batchIdBuffer = new StringBuffer();
            StringBuffer samplingPlanIdBuffer = new StringBuffer();
            StringBuffer samplingPlanVersionIdBuffer = new StringBuffer();
            StringBuffer samplingPlanLevelBuffer = new StringBuffer();
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String batchId = primary.getValue(i, COLUMN_S_BATCHID, "");
                if (this.batchesWithSimpleProduct.contains(batchId)) continue;
                batchIdBuffer.append(";").append(batchId);
                samplingPlanIdBuffer.append(";").append(primary.getValue(i, COLUMN_SAMPLINGPLANID, ""));
                samplingPlanVersionIdBuffer.append(";").append(primary.getValue(i, COLUMN_SAMPLINGPLANVERSIONID, ""));
                samplingPlanLevelBuffer.append(";").append(samplingPlanLevel);
            }
            if (batchIdBuffer.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", this.getSdcid());
                props.setProperty("keyid1", batchIdBuffer.substring(1));
                props.setProperty(COLUMN_SAMPLINGPLANID, samplingPlanIdBuffer.substring(1));
                props.setProperty(COLUMN_SAMPLINGPLANVERSIONID, samplingPlanVersionIdBuffer.substring(1));
                props.setProperty("samplingplanlevel", samplingPlanLevelBuffer.substring(1));
                this.getActionProcessor().processAction("ApplySamplingPlan", "1", props);
                String message = props.getProperty("return_message");
                if (!message.equalsIgnoreCase("success")) {
                    this.setError("ProcessSamplingPlan", "INFORMATION", message);
                }
            }
        } else {
            PropertyList pl = SamplingPlanUtil.getPolicyPropertyList(this.getSdcid(), configProcessor);
            if (pl != null) {
                String applyEvent = pl.getProperty("applyevent", "");
                DataSet dsApplySamplingPlans = new DataSet();
                dsApplySamplingPlans.addColumn("batchid", 0);
                dsApplySamplingPlans.addColumn("prodvarianttype", 0);
                DataSet dsEvalProdVars = new DataSet();
                dsEvalProdVars.addColumn("batchid", 0);
                dsEvalProdVars.addColumn("prodvarianttype", 0);
                if (this.batchesWithSimpleProduct == null) {
                    this.getBatchesWithSimpleProducts(primary);
                }
                PreparedStatement batchTypePsmt = this.database.prepareStatement("getBatchType", "SELECT batchtype FROM s_batch WHERE s_batchid = ?");
                try {
                    DataSet dsGrp;
                    for (int i = 0; i < primary.getRowCount(); ++i) {
                        int row;
                        String batchId = primary.getValue(i, COLUMN_S_BATCHID);
                        if (!this.hasPrimaryValueChanged(primary, i, COLUMN_BATCHSTATUS) && !this.hasPrimaryValueChanged(primary, i, "disposition") || this.batchesWithSimpleProduct.contains(batchId)) continue;
                        String batchStatus = primary.getValue(i, COLUMN_BATCHSTATUS, "");
                        String prodVariantType = primary.getValue(i, "batchtype", "");
                        if (prodVariantType.trim().equals("")) {
                            batchTypePsmt.setString(1, batchId);
                            DataSet dsBatchType = new DataSet(batchTypePsmt.executeQuery());
                            if (dsBatchType.getRowCount() > 0) {
                                prodVariantType = dsBatchType.getValue(0, "batchtype", "");
                            }
                        }
                        if (batchStatus.length() > 0) {
                            String appliedSamplingPlanId = this.getOldPrimaryValue(primary, i, COLUMN_SAMPLINGPLANID);
                            if (this.addMode && applyEvent.equalsIgnoreCase("On Creation") || BatchLifeCycleUtil.getBatchStateDisplayValue("received").equalsIgnoreCase(batchStatus) && appliedSamplingPlanId.length() == 0 && applyEvent.equalsIgnoreCase("On Receipt")) {
                                if (prodVariantType.trim().equals("")) {
                                    Logger.logWarn("ApplySamplingPlan could not be applied. No batchtype found for the Batch :" + batchId);
                                    continue;
                                }
                                row = dsApplySamplingPlans.addRow();
                                String prodVariantId = primary.getValue(i, "prodvariantid", "");
                                if (!this.addMode && prodVariantId.length() == 0) {
                                    prodVariantId = this.getBeforeEditImage().getDataset("primary").getString(i, "prodvariantid", "");
                                }
                                dsApplySamplingPlans.setString(row, "batchid", batchId);
                                dsApplySamplingPlans.setString(row, "prodvarianttype", prodVariantType);
                                dsApplySamplingPlans.setString(row, "prodvariantid", prodVariantId);
                            }
                        }
                        if (!this.hasPrimaryValueChanged(primary, i, "disposition")) continue;
                        String disposition = primary.getValue(i, "disposition", "");
                        if (prodVariantType.trim().equals("")) {
                            Logger.logWarn("ProdVariant evaluation could not be done. No batchtype found for the Batch :" + batchId);
                            continue;
                        }
                        if (!"passed".equalsIgnoreCase(disposition) && !"failed".equalsIgnoreCase(disposition)) continue;
                        row = dsEvalProdVars.addRow();
                        dsEvalProdVars.setString(row, "batchid", batchId);
                        dsEvalProdVars.setString(row, "prodvarianttype", prodVariantType);
                    }
                    if (dsApplySamplingPlans.getRowCount() > 0) {
                        dsApplySamplingPlans.sort("prodvarianttype, prodvariantid ");
                        ArrayList<DataSet> groups = dsApplySamplingPlans.getGroupedDataSets("prodvarianttype, prodvariantid");
                        PropertyList props = new PropertyList();
                        for (int grp = 0; grp < groups.size(); ++grp) {
                            dsGrp = groups.get(grp);
                            props.setProperty("sdcid", this.getSdcid());
                            props.setProperty("keyid1", dsGrp.getColumnValues("batchid", ";"));
                            props.setProperty("prodvarianttype", dsGrp.getString(0, "prodvarianttype"));
                            if (dsGrp.getString(0, "prodvariantid", "").length() > 0) {
                                props.setProperty("prodvariantid", dsGrp.getString(0, "prodvariantid"));
                            }
                            this.getActionProcessor().processAction("ApplySamplingPlan", "1", props);
                            String message = props.getProperty("return_message");
                            if (!message.equalsIgnoreCase("success")) {
                                this.setError("ProcessSamplingPlan", "INFORMATION", message);
                            }
                            props.clear();
                        }
                    }
                    if (dsEvalProdVars.getRowCount() > 0) {
                        PropertyList props = new PropertyList();
                        dsEvalProdVars.sort("prodvarianttype");
                        ArrayList<DataSet> groups = dsEvalProdVars.getGroupedDataSets("prodvarianttype");
                        for (int grp = 0; grp < groups.size(); ++grp) {
                            dsGrp = groups.get(grp);
                            props.setProperty("sdcid", this.getSdcid());
                            props.setProperty("keyid1", dsGrp.getColumnValues("batchid", ";"));
                            props.setProperty("prodvarianttype", dsGrp.getString(0, "prodvarianttype"));
                            this.getActionProcessor().processAction("EvalProdVariantState", "1", props);
                            props.clear();
                        }
                    }
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
                finally {
                    this.database.closeStatement("getBatchType");
                }
            }
        }
    }

    private void createFormulationRows(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        int primaryRows = primary.getRowCount();
        String sql = "SELECT p.*, b.s_batchstageid  FROM s_productformulation p  LEFT OUTER JOIN s_batchstage b ON p.productstageid = b.productstageid and b.batchid = ? WHERE p.s_productid = ? AND p.s_productversionid = ? ORDER BY p.usersequence";
        PreparedStatement productFormulationPS = this.database.prepareStatement("parentproductquery", sql);
        String ingredientdataitemSQL = "select item.transformvalue, item.displayvalue, item.displayunits, item.paramid, item.paramtype, item.enteredvalue, item.enteredtext, item.enteredunits  from sdidataitem item, s_productformulation pf  where pf.s_productid = item.keyid1 and  pf.s_productversionid = item.keyid2 and  pf.s_productitemid = item.keyid3 and item.sdcid = 'LV_ProductIngredient'  and item.keyid1 = ? and item.keyid2 = ? and item.paramtype in ( ?, ?, ? ) ORDER BY pf.usersequence";
        PreparedStatement ingredientSdidataItemPS = this.database.prepareStatement("ingredientsdidataitem", ingredientdataitemSQL);
        PreparedStatement productPS = this.database.prepareStatement("product", "select * from s_product where s_productid=? and s_productversionid=?");
        try {
            for (int i = 0; i < primaryRows; ++i) {
                String batchId = primary.getValue(i, COLUMN_S_BATCHID, "");
                String batchMode = primary.getValue(i, "batchmode", "");
                if (!MODE_ADHOC.equalsIgnoreCase(batchMode) && !"".equalsIgnoreCase(batchMode) && !MODE_FORMULATION.equalsIgnoreCase(batchMode) && !MODE_PROMOTEDFORMULATION.equalsIgnoreCase(batchMode)) continue;
                double multifactor = this.getMultiplicationFactor(primary, i);
                String productID = primary.getValue(i, COLUMN_PRODUCTID, "");
                String productVersionID = primary.getValue(i, COLUMN_PRODUCTVERSIONID, "");
                productPS.setString(1, productID);
                productPS.setString(2, productVersionID);
                DataSet productDS = new DataSet(productPS.executeQuery());
                String recipetypeFlag = "";
                if (productDS.getRowCount() > 0) {
                    recipetypeFlag = productDS.getString(0, "recipetypeflag", "");
                }
                productFormulationPS.setString(1, batchId);
                productFormulationPS.setString(2, productID);
                productFormulationPS.setString(3, productVersionID);
                DataSet productFormulation = new DataSet(productFormulationPS.executeQuery());
                ingredientSdidataItemPS.setString(1, productID);
                ingredientSdidataItemPS.setString(2, productVersionID);
                if (recipetypeFlag.equalsIgnoreCase("A")) {
                    ingredientSdidataItemPS.setString(3, this.PARAMTYPE_QUANTITY);
                } else {
                    ingredientSdidataItemPS.setString(3, this.PARAMTYPE_FRACTION);
                }
                ingredientSdidataItemPS.setString(4, this.PARAMTYPE_LOWERBOUND);
                ingredientSdidataItemPS.setString(5, this.PARAMTYPE_UPPERBOUND);
                DataSet ingredientSdidataItem = new DataSet(ingredientSdidataItemPS.executeQuery());
                if (productFormulation.getRowCount() > 0) {
                    primary.setValue(i, "batchmode", MODE_FORMULATION);
                    String batchItemId = "";
                    String usersequence = "";
                    this.logger.info("Updating genealogy table.");
                    try {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", SDCID);
                        props.setProperty("linkid", "batch genealogy");
                        props.setProperty(COLUMN_S_BATCHID, primary.getValue(i, COLUMN_S_BATCHID, ""));
                        int j = 0;
                        while (j < productFormulation.getRowCount()) {
                            batchItemId = batchItemId + ";";
                            usersequence = usersequence + ";" + String.valueOf(++j);
                        }
                        props.setProperty("s_batchitemid", batchItemId.substring(1));
                        props.setProperty("usersequence", usersequence.substring(1));
                        String parentProduct = productFormulation.getColumnValues("parentproductid", ";");
                        String batchStageIds = productFormulation.getColumnValues("s_batchstageid", ";");
                        String[] parentProductArray = StringUtil.split(parentProduct, ";");
                        String[] parentProductVersionArray = StringUtil.split(productFormulation.getColumnValues("parentproductversionid", ";"), ";");
                        String parentProductVersion = "";
                        for (int j2 = 0; j2 < parentProductArray.length; ++j2) {
                            parentProductVersion = parentProductVersionArray.length == 0 || parentProductVersionArray[j2] == null || parentProductVersionArray[j2].equals("(null)") || parentProductVersionArray[j2].length() == 0 ? parentProductVersion + ";" : parentProductVersion + ";" + parentProductVersionArray[j2];
                        }
                        props.setProperty("parentproductid", parentProduct);
                        props.setProperty("parentbatchstageid", batchStageIds);
                        props.setProperty("parentproductversionid", parentProductVersion.substring(1));
                        props.setProperty("mandatoryflag", productFormulation.getColumnValues("mandatoryflag", ";"));
                        if (ingredientSdidataItem.getRowCount() > 0) {
                            this.populateTargetAmount(props, ingredientSdidataItem, multifactor, recipetypeFlag, primary, i, true);
                        } else {
                            String amount = this.applyMultiFactorOnProductFormulationAmount(productFormulation, "amount", multifactor);
                            props.setProperty("amounttarget", amount);
                            props.setProperty("amouttargettext", amount);
                            props.setProperty("amountunits", productFormulation.getColumnValues("amountunits", ";"));
                        }
                        props.setProperty("separator", ";");
                        this.getActionProcessor().processAction("AddSDIDetail", "1", props);
                        continue;
                    }
                    catch (SapphireException e) {
                        throw new SapphireException(e);
                    }
                }
                primary.setValue(i, "batchmode", MODE_ADHOC);
            }
        }
        catch (Exception e) {
            this.logger.error("Error in fetching product recipes", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("parentproductquery");
            this.database.closeStatement("ingredientsdidataitem");
            this.database.closeStatement("product");
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.verifyParentBatches(sdiData, actionProps);
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.verifyParentBatches(sdiData, actionProps);
    }

    private void verifyParentBatches(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet genealogyDataSet = sdiData.getDataset("s_batchgenealogy");
        SafeSQL safeSQL = new SafeSQL();
        if (genealogyDataSet != null && genealogyDataSet.getColumnValues("parentbatchid", ",") != null && genealogyDataSet.getColumnValues("parentbatchid", "").trim().length() > 0) {
            TranslationProcessor tp = this.getTranslationProcessor();
            String batchid = genealogyDataSet.getColumnValues(COLUMN_S_BATCHID, "','");
            String sql = null;
            sql = this.connectionInfo.getDbms().equalsIgnoreCase("ORA") ? "SELECT bg.parentbatchid batch,b.batchstatus status,'P' as relation FROM s_batchgenealogy bg,s_batch b WHERE b.s_batchid = bg.parentbatchid START WITH bg.s_batchid IN (" + safeSQL.addIn(batchid) + ") AND bg.parentbatchid IS NOT NULL CONNECT BY bg.s_batchid=prior bg.parentbatchid AND bg.parentbatchid IS NOT NULL UNION  SELECT bg.s_batchid batch,b.batchstatus status,'C' as relation FROM s_batchgenealogy bg,s_batch b  WHERE b.s_batchid = bg.s_batchid START WITH bg.parentbatchid IN (" + safeSQL.addIn(batchid) + ") AND bg.parentbatchid IS NOT NULL CONNECT BY bg.parentbatchid= prior bg.s_batchid AND bg.parentbatchid IS NOT NULL UNION ALL SELECT s_batchid batch,batchstatus status,'S' as relation FROM s_batch WHERE s_batchid IN (" + safeSQL.addIn(genealogyDataSet.getColumnValues("parentbatchid", "','")) + ")" : "WITH Childs AS(    SELECT e.s_batchid     FROM s_batchgenealogy AS e    WHERE parentbatchid IN (" + safeSQL.addIn(batchid) + ")    UNION ALL    SELECT e.s_batchid     FROM s_batchgenealogy AS e    INNER JOIN Childs AS d        ON e.parentbatchid= d.s_batchid),Parents AS(    SELECT e.parentbatchid            FROM s_batchgenealogy AS e    WHERE s_batchid IN (" + safeSQL.addIn(batchid) + ")    UNION ALL    SELECT e.parentbatchid    FROM s_batchgenealogy AS e    INNER JOIN Parents AS d        ON e.s_batchid= d.parentbatchid)  SELECT c.s_batchid as batch,b.batchstatus as status, 'C' as relation  FROM Childs AS c,s_batch AS b WHERE c.s_batchid = b.s_batchid AND c.s_batchid IS NOT NULL   UNION  SELECT p.parentbatchid as batch,b.batchstatus as status, 'P' as relation  FROM Parents AS p,s_batch AS b WHERE p.parentbatchid = b.s_batchid AND p.parentbatchid IS NOT NULL  UNION ALL SELECT s_batchid as batch,batchstatus as status,'S' as relation FROM s_batch WHERE s_batchid IN (" + safeSQL.addIn(genealogyDataSet.getColumnValues("parentbatchid", "','")) + ")";
            DataSet hierarchialBatches = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (hierarchialBatches != null && hierarchialBatches.getRowCount() > 0) {
                HashMap<String, String> filterMap = new HashMap<String, String>();
                for (int i = 0; i < genealogyDataSet.getRowCount(); ++i) {
                    String parentBatchId = genealogyDataSet.getValue(i, "parentbatchid");
                    if (parentBatchId.length() <= 0 || !this.isNewBatch(parentBatchId, genealogyDataSet.getValue(i, "s_batchitemid"))) continue;
                    boolean parentOfAnotherItem = this.isParentOfExistingBatchItem(parentBatchId);
                    filterMap.put("batch", parentBatchId);
                    DataSet tempDataSet = hierarchialBatches.getFilteredDataSet(filterMap);
                    HashMap<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("batchid", parentBatchId);
                    if (tempDataSet.getRowCount() > 1 && !parentOfAnotherItem) {
                        boolean isChild;
                        filterMap.clear();
                        filterMap.put("relation", "C");
                        boolean bl = isChild = tempDataSet.getFilteredDataSet(filterMap).getRowCount() > 0;
                        if (!isChild) continue;
                        throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("The selected batch '[batchid]' is already present in the hierarchy.", valueMap));
                    }
                    if (tempDataSet.getRowCount() > 0 && tempDataSet.getValue(0, "status", "").equalsIgnoreCase("Rejected")) {
                        throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("The selected batch '[batchid]' is 'Rejected'.", valueMap));
                    }
                    if (tempDataSet.getRowCount() <= 0 || !tempDataSet.getValue(0, "batch", "").equalsIgnoreCase(genealogyDataSet.getValue(i, COLUMN_S_BATCHID))) continue;
                    valueMap.put("batchid", genealogyDataSet.getValue(i, COLUMN_S_BATCHID));
                    throw new SapphireException("INVALID_PARAMETER", "VALIDATION", tp.translate("The selected batch '[batchid]' cannot be the parent of self.", valueMap));
                }
            }
        }
    }

    private boolean isNewBatch(String parentBatchId, String batchItemId) {
        DataSet beforeEditGenealogyDataSet = this.getBeforeEditImage().getDataset("s_batchgenealogy");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("parentbatchid", parentBatchId);
        filter.put("s_batchitemid", batchItemId);
        boolean flag = beforeEditGenealogyDataSet.findRow(filter) <= -1;
        return flag;
    }

    private boolean isParentOfExistingBatchItem(String parentBatchId) {
        boolean flag = false;
        DataSet beforeEditGenealogyDataSet = this.getBeforeEditImage().getDataset("s_batchgenealogy");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("parentbatchid", parentBatchId);
        flag = beforeEditGenealogyDataSet.findRow(filter) > -1;
        return flag;
    }

    private void createSamplesForBatch(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        PropertyList policy;
        DataSet primary = sdiData.getDataset("primary");
        DataSet dsNoFormulationProject = new DataSet();
        DataSet dsSimpleProduct = new DataSet();
        DataSet dsFormulationProject = new DataSet();
        PreparedStatement getProdDetails = this.database.prepareStatement("prodDetail", "SELECT p.productmodeflag, p.formulationprojectid FROM s_product p WHERE p.s_productid = ? AND p.s_productversionid = ?");
        if (primary.isValidColumn(COLUMN_PRODUCTID)) {
            HashMap product_StageSupport;
            try {
                for (int i = 0; i < primary.getRowCount(); ++i) {
                    getProdDetails.setString(1, primary.getValue(i, COLUMN_PRODUCTID));
                    getProdDetails.setString(2, primary.getValue(i, COLUMN_PRODUCTVERSIONID, "1"));
                    DataSet dsProd = new DataSet(getProdDetails.executeQuery());
                    if (dsProd.getRowCount() != 1) continue;
                    String formulationProjectId = dsProd.getValue(i, "formulationprojectid");
                    String productModeFlag = dsProd.getValue(i, "productmodeflag");
                    if (formulationProjectId.length() > 0) {
                        dsFormulationProject.copyRow(primary, i, 1);
                        continue;
                    }
                    if ("S".equals(productModeFlag)) {
                        dsSimpleProduct.copyRow(primary, i, 1);
                        continue;
                    }
                    dsNoFormulationProject.copyRow(primary, i, 1);
                }
            }
            catch (Exception e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
            finally {
                this.database.closeStatement("prodDetail");
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("batchmode", MODE_FORMULATION);
            DataSet ds = dsFormulationProject.getFilteredDataSet(filter);
            if (ds.getRowCount() > 0) {
                product_StageSupport = new HashMap();
                this.createSamplesForFormulationBatch(ds, product_StageSupport, MODE_FORMULATION);
                this.createFormulationBatchInstrumentandStages(ds, product_StageSupport, MODE_FORMULATION);
            }
            filter = new HashMap();
            filter.put("batchmode", MODE_PROMOTEDFORMULATION);
            ds = dsFormulationProject.getFilteredDataSet(filter);
            if (ds.getRowCount() > 0) {
                product_StageSupport = new HashMap();
                this.createSamplesForFormulationBatch(ds, product_StageSupport, MODE_PROMOTEDFORMULATION);
                this.createFormulationBatchInstrumentandStages(ds, product_StageSupport, MODE_PROMOTEDFORMULATION);
            }
        }
        if (dsSimpleProduct.getRowCount() > 0) {
            this.createSamplesForSimpleProducts(dsSimpleProduct);
        }
        if (dsNoFormulationProject.getRowCount() > 0 && (policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom")) != null) {
            String sampleCreationOption = policy.getProperty("samplecreationpolicy", "");
            if (sampleCreationOption.equalsIgnoreCase("Using Product")) {
                this.createSamplesUsingProduct(dsNoFormulationProject, false);
            } else if (sampleCreationOption.equalsIgnoreCase("Using Sampling Plan")) {
                this.processSamplingPlan(dsNoFormulationProject, actionProps);
            } else if (sampleCreationOption.equalsIgnoreCase("Using Both")) {
                this.createSamplesUsingProduct(primary, false);
                this.processSamplingPlan(dsNoFormulationProject, actionProps);
            }
        }
    }

    private void createSamplesForSimpleProducts(DataSet primary) throws SapphireException {
        this.createSamplesUsingProduct(primary, true);
    }

    private void createSamplesForFormulationBatch(DataSet ds, HashMap product_StageSupport, String batchMode) throws SapphireException {
        ActionProcessor ap = this.getActionProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        PreparedStatement getProdDetails = null;
        DataSet sdiattributesToAdd = new DataSet();
        DataSet sdiattributesToEdit = new DataSet();
        DataSet allExistingTargetAttributes = new DataSet();
        getProdDetails = batchMode.equals(MODE_PROMOTEDFORMULATION) ? this.database.prepareStatement("prodDetail", "SELECT p.batchsamplecount, p.batchsampletemplateid, p.embeddedspecid, p.embeddedspecversionid, p.formulationprojectid FROM s_product p WHERE p.s_productid = ? AND p.s_productversionid = ?") : this.database.prepareStatement("prodDetail", "select p.batchsamplecount, p.batchsampletemplateid, p.embeddedspecid, p.embeddedspecversionid, p.formulationprojectid, fp.individualstagesflag from s_product p, formulationproject fp where fp.formulationprojectid = p.formulationprojectid and p.s_productid = ? and p.s_productversionid = ?");
        PreparedStatement getWorkItem = this.database.prepareStatement("prod_workitems", "select workitemid, workitemversionid  from sdiworkitem where sdcid = 'Product' and keyid1 = ? and keyid2 = ? order by usersequence");
        PreparedStatement getSpec = this.database.prepareStatement("prod_specs", "select specid, specversionid  from sdispec where sdcid = 'Product' and keyid1 = ? and keyid2 = ? order by usersequence");
        try {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String individualStagesFlag;
                String batchId = ds.getValue(i, COLUMN_S_BATCHID);
                String productId = ds.getValue(i, COLUMN_PRODUCTID);
                String productVersionId = ds.getValue(i, COLUMN_PRODUCTVERSIONID, "1");
                getProdDetails.setString(1, productId);
                getProdDetails.setString(2, productVersionId);
                DataSet dsProd = new DataSet(getProdDetails.executeQuery());
                if (dsProd.getRowCount() <= 0) continue;
                if (batchMode.equals(MODE_FORMULATION) && "Y".equalsIgnoreCase(individualStagesFlag = dsProd.getValue(0, "individualstagesflag", ""))) {
                    product_StageSupport.put(productId + ";" + productVersionId, "Y");
                    continue;
                }
                int batchSampleCnt = dsProd.getBigDecimal(0, "batchsamplecount", new BigDecimal(0.0)).intValue();
                String embeddedSpecId = dsProd.getValue(0, "embeddedspecid", "");
                String embeddedSpecVersionId = dsProd.getValue(0, "embeddedspecversionid", "");
                if (batchSampleCnt <= 0) continue;
                String templateId = dsProd.getValue(0, "batchsampletemplateid", "");
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Sample");
                props.setProperty("copies", "" + batchSampleCnt);
                if (templateId.length() > 0) {
                    props.setProperty("templateid", templateId);
                }
                props.setProperty("batchid", batchId);
                props.setProperty(COLUMN_PRODUCTID, productId);
                props.setProperty(COLUMN_PRODUCTVERSIONID, productVersionId);
                ap.processAction("AddSDI", "1", props);
                String sampleIds = props.getProperty("newkeyid1", "");
                if (sampleIds.length() <= 0) continue;
                getWorkItem.setString(1, productId);
                getWorkItem.setString(2, productVersionId);
                DataSet dsWorkItems = new DataSet(getWorkItem.executeQuery());
                if (dsWorkItems.getRowCount() > 0) {
                    DataSet dsSourceSDIWIAttributes;
                    props.clear();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", sampleIds);
                    props.setProperty("workitemid", dsWorkItems.getColumnValues("workitemid", ";"));
                    props.setProperty("workitemversionid", dsWorkItems.getColumnValues("workitemversionid", ";"));
                    ap.processAction("AddSDIWorkItem", "1", props);
                    DataSet dsInstance = new DataSet(props.getProperty("newworkiteminstancexml"));
                    if (dsInstance.getRowCount() > 0 && (dsSourceSDIWIAttributes = qp.getPreparedSqlDataSet("select a.*, w.workitemid, w.workitemversionid from sdiattribute a, sdiworkitem w  where a.sdcid = ? and  a.keyid1 = w.sdiworkitemid and  w.sdcid = ? and w.keyid1 = ? and w.keyid2 = ? ", (Object[])new String[]{"SDIWorkItem", "Product", productId, productVersionId}, true)).getRowCount() > 0) {
                        CopySDIDetail.copyDownSourceAttributes(dsSourceSDIWIAttributes, dsInstance, qp, this.getSDCProcessor(), allExistingTargetAttributes, new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.logger, new M18NUtil(this.connectionInfo), sdiattributesToAdd, sdiattributesToEdit);
                    }
                    if (sdiattributesToAdd.getRowCount() > 0 || sdiattributesToEdit.getRowCount() > 0) {
                        CopySDIDetail.addEditAttributes(sdiattributesToAdd, sdiattributesToEdit, allExistingTargetAttributes, this.logger, this.database);
                    }
                }
                getSpec.setString(1, productId);
                getSpec.setString(2, productVersionId);
                DataSet dsSpecs = new DataSet(getSpec.executeQuery());
                String specIds = embeddedSpecId;
                String specVersionIds = embeddedSpecVersionId;
                if (dsSpecs.getRowCount() > 0) {
                    specIds = specIds + (specIds.length() > 0 ? ";" : "");
                    specIds = specIds + dsSpecs.getColumnValues("specid", ";");
                    specVersionIds = specVersionIds + (specVersionIds.length() > 0 ? ";" : "");
                    specVersionIds = specVersionIds + dsSpecs.getColumnValues("specversionid", ";");
                }
                if (specIds.length() <= 0) continue;
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", sampleIds);
                props.setProperty("specid", specIds);
                props.setProperty("specversionid", specVersionIds);
                ap.processAction("AddSDISpec", "1", props);
            }
        }
        catch (Exception e) {
            this.logger.error("Error while creating Samples for Formulation Batch", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("prodDetail");
            this.database.closeStatement("prod_workitems");
            this.database.closeStatement("prod_specs");
        }
    }

    private void createFormulationBatchInstrumentandStages(DataSet ds, HashMap product_StageSupport, String batchMode) throws SapphireException {
        PreparedStatement getProdStages = this.database.prepareStatement("prodStages", "select s_productstageid, productstagedesc, stagelabel, templatebatchstageid, templatesampleid, samplecount, embeddedspecid, embeddedspecversionid, usersequence from s_productstage where s_productid = ? and s_productversionid = ? order by templatebatchstageid");
        PreparedStatement getProdInstruments = this.database.prepareStatement("prodInstruments", "select * from s_productinstrument  where s_productid = ? and s_productversionid = ? ");
        ActionProcessor ap = this.getActionProcessor();
        DataSet dsAddInstrument = new DataSet();
        dsAddInstrument.addColumn("batchstageid", 0);
        dsAddInstrument.addColumn(COLUMN_S_BATCHID, 0);
        PropertyList props = new PropertyList();
        DataSet dsAddSDIWorkItem = new DataSet();
        DataSet sdiattributesToAdd = new DataSet();
        DataSet sdiattributesToEdit = new DataSet();
        DataSet allExistingTargetAttributes = new DataSet();
        DataSet dsSampleSourceSDIWIAttributes = new DataSet();
        QueryProcessor qp = this.getQueryProcessor();
        try {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String batchId = ds.getValue(i, COLUMN_S_BATCHID);
                String productId = ds.getValue(i, COLUMN_PRODUCTID);
                String productVersionId = ds.getValue(i, COLUMN_PRODUCTVERSIONID, "1");
                getProdInstruments.setString(1, productId);
                getProdInstruments.setString(2, productVersionId);
                DataSet dsProdInstr = new DataSet(getProdInstruments.executeQuery());
                dsProdInstr.setString(-1, COLUMN_S_BATCHID, batchId);
                dsAddInstrument.copyRow(dsProdInstr, -1, 1);
                if (!batchMode.equals(MODE_PROMOTEDFORMULATION) && !product_StageSupport.containsKey(productId + ";" + productVersionId)) continue;
                getProdStages.setString(1, productId);
                getProdStages.setString(2, productVersionId);
                DataSet dsStages = new DataSet(getProdStages.executeQuery());
                dsStages.addColumn("batchstageid", 0);
                if (dsStages.getRowCount() <= 0) continue;
                ArrayList<DataSet> grps = dsStages.getGroupedDataSets("templatebatchstageid");
                for (int g = 0; g < grps.size(); ++g) {
                    DataSet stageGroup = grps.get(g);
                    props.clear();
                    props.setProperty("batchstagedesc", stageGroup.getColumnValues("productstagedesc", ";"));
                    props.setProperty("label", stageGroup.getColumnValues("stagelabel", ";"));
                    props.setProperty("usersequence", stageGroup.getColumnValues("usersequence", ";"));
                    props.setProperty("productstageid", stageGroup.getColumnValues("s_productstageid", ";"));
                    props.setProperty("templateid", stageGroup.getValue(0, "templatebatchstageid"));
                    props.setProperty("copies", "" + stageGroup.getRowCount());
                    props.setProperty("batchstagestatus", "Initial");
                    props.setProperty("processstageinstance", "1");
                    props.setProperty("batchid", batchId);
                    props.setProperty(COLUMN_PRODUCTID, productId);
                    props.setProperty(COLUMN_PRODUCTVERSIONID, productVersionId);
                    props.setProperty("sdcid", "LV_BatchStage");
                    ap.processAction("AddSDI", "1", props);
                    String batchStageIds = props.getProperty("newkeyid1", "");
                    if (batchStageIds.length() <= 0) continue;
                    stageGroup.addColumnValues("batchstageid", 0, batchStageIds, ";");
                }
                dsStages.sort("usersequence");
                for (int s = 0; s < dsStages.getRowCount(); ++s) {
                    String productstageId = dsStages.getValue(s, "s_productstageid");
                    String batchstageId = dsStages.getValue(s, "batchstageid");
                    if (batchstageId.length() <= 0) continue;
                    String templatesampleId = dsStages.getValue(s, "templatesampleid");
                    int sampleCount = dsStages.getBigDecimal(s, "samplecount", new BigDecimal(0.0)).intValue();
                    String embeddedSpecId = "";
                    String embeddedSpecVersionId = "";
                    if (sampleCount > 0) {
                        String stageEmbeddedSpecId = dsStages.getValue(s, "embeddedspecid");
                        String stageEmbeddedSpecVersionId = dsStages.getValue(s, "embeddedspecversionid");
                        if (stageEmbeddedSpecId.length() > 0) {
                            embeddedSpecId = embeddedSpecId + stageEmbeddedSpecId;
                            embeddedSpecVersionId = embeddedSpecVersionId + stageEmbeddedSpecVersionId;
                        }
                        props.clear();
                        props.setProperty("sdcid", "Sample");
                        props.setProperty("copies", "" + sampleCount);
                        if (templatesampleId.length() > 0) {
                            props.setProperty("templateid", templatesampleId);
                        }
                        props.setProperty("batchid", batchId);
                        props.setProperty("batchstageid", batchstageId);
                        props.setProperty(COLUMN_PRODUCTID, productId);
                        props.setProperty(COLUMN_PRODUCTVERSIONID, productVersionId);
                        ap.processAction("AddSDI", "1", props);
                        String sampleIds = props.getProperty("newkeyid1", "");
                        if (sampleIds.length() > 0) {
                            PreparedStatement getWorkItem = this.database.prepareStatement("prodstage_workitems", "select workitemid, workitemversionid, usersequence  from sdiworkitem where sdcid = 'LV_ProductStage' and keyid1 = ? and keyid2 = ? and keyid3 = ? order by usersequence");
                            PreparedStatement getSpec = this.database.prepareStatement("prodstage_specs", "select specid, specversionid  from sdispec where sdcid = 'LV_ProductStage' and keyid1 = ? and keyid2 = ? and keyid3 = ? order by usersequence");
                            getWorkItem.setString(1, productId);
                            getWorkItem.setString(2, productVersionId);
                            getWorkItem.setString(3, productstageId);
                            DataSet dsWorkItems = new DataSet(getWorkItem.executeQuery());
                            String[] samples = StringUtil.split(sampleIds, ";");
                            if (dsWorkItems.getRowCount() > 0) {
                                DataSet dsSourceSDIWIAttributes = qp.getPreparedSqlDataSet("select a.*, w.workitemid, w.workitemversionid from sdiattribute a, sdiworkitem w  where a.sdcid = ? and  a.keyid1 = w.sdiworkitemid and  w.sdcid = ? and w.keyid1 = ? and w.keyid2 = ? and w.keyid3 = ? ", (Object[])new String[]{"SDIWorkItem", "LV_ProductStage", productId, productVersionId, productstageId}, true);
                                for (int sa = 0; sa < samples.length; ++sa) {
                                    dsWorkItems.setString(-1, "s_sampleid", samples[sa]);
                                    dsAddSDIWorkItem.copyRow(dsWorkItems, -1, 1);
                                    if (dsSourceSDIWIAttributes.getRowCount() <= 0) continue;
                                    dsSourceSDIWIAttributes.setString(-1, "s_sampleid", samples[sa]);
                                    dsSampleSourceSDIWIAttributes.copyRow(dsSourceSDIWIAttributes, -1, 1);
                                }
                            }
                            getSpec.setString(1, productId);
                            getSpec.setString(2, productVersionId);
                            getSpec.setString(3, productstageId);
                            DataSet dsSpecs = new DataSet(getSpec.executeQuery());
                            String specIds = embeddedSpecId;
                            String specVersionIds = embeddedSpecVersionId;
                            if (dsSpecs.getRowCount() > 0) {
                                specIds = specIds + (specIds.length() > 0 ? ";" : "");
                                specVersionIds = specVersionIds + (specVersionIds.length() > 0 ? ";" : "");
                                specIds = specIds + dsSpecs.getColumnValues("specid", ";");
                                specVersionIds = specVersionIds + dsSpecs.getColumnValues("specversionid", ";");
                            }
                            if (specIds.length() > 0) {
                                props.clear();
                                props.setProperty("sdcid", "Sample");
                                props.setProperty("keyid1", sampleIds);
                                props.setProperty("specid", specIds);
                                props.setProperty("specversionid", specVersionIds);
                                ap.processAction("AddSDISpec", "1", props);
                            }
                        }
                    }
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put(COLUMN_S_BATCHID, batchId);
                    filter.put("productstageid", productstageId);
                    DataSet dsStageInstr = dsAddInstrument.getFilteredDataSet(filter);
                    if (dsStageInstr.getRowCount() <= 0) continue;
                    dsStageInstr.setString(-1, "batchstageid", batchstageId);
                }
            }
            if (dsAddSDIWorkItem.getRowCount() > 0) {
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", dsAddSDIWorkItem.getColumnValues("s_sampleid", ";"));
                props.setProperty("workitemid", dsAddSDIWorkItem.getColumnValues("workitemid", ";"));
                props.setProperty("workitemversionid", dsAddSDIWorkItem.getColumnValues("workitemversionid", ";"));
                props.setProperty("propsmatchtestmethodorder", dsAddSDIWorkItem.getColumnValues("usersequence", ";"));
                props.setProperty("propsmatch", "Y");
                ap.processAction("AddSDIWorkItem", "1", props);
                DataSet dsInstance = new DataSet(props.getProperty("newworkiteminstancexml"));
                if (dsInstance.getRowCount() > 0 && dsSampleSourceSDIWIAttributes.getRowCount() > 0) {
                    dsSampleSourceSDIWIAttributes.sort("s_sampleid");
                    ArrayList<DataSet> sampleGroups = dsSampleSourceSDIWIAttributes.getGroupedDataSets("s_sampleid");
                    for (int g = 0; g < sampleGroups.size(); ++g) {
                        DataSet dsSourceSDIWIAttributes = sampleGroups.get(g);
                        HashMap<String, String> findSample = new HashMap<String, String>();
                        findSample.put("keyid1", dsSourceSDIWIAttributes.getValue(0, "s_sampleid"));
                        DataSet dsInstancesSample = dsInstance.getFilteredDataSet(findSample);
                        DataSet dsSampleSDIWIAttributes = new DataSet();
                        dsSampleSDIWIAttributes.copyRow(dsSourceSDIWIAttributes, -1, 1);
                        dsSampleSDIWIAttributes.removeColumn("s_sampleid");
                        CopySDIDetail.copyDownSourceAttributes(dsSampleSDIWIAttributes, dsInstancesSample, qp, this.getSDCProcessor(), allExistingTargetAttributes, new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.logger, new M18NUtil(this.connectionInfo), sdiattributesToAdd, sdiattributesToEdit);
                    }
                }
                if (sdiattributesToAdd.getRowCount() > 0 || sdiattributesToEdit.getRowCount() > 0) {
                    CopySDIDetail.addEditAttributes(sdiattributesToAdd, sdiattributesToEdit, allExistingTargetAttributes, this.logger, this.database);
                }
            }
            if (dsAddInstrument.getRowCount() > 0) {
                props.clear();
                props.setProperty("sdcid", "LV_BatchInstrument");
                props.setProperty("batchinstrumentdesc", dsAddInstrument.getColumnValues("productinstrumentdesc", ";"));
                props.setProperty("instrumentmodelid", dsAddInstrument.getColumnValues("instrumentmodelid", ";"));
                props.setProperty("instrumenttypeid", dsAddInstrument.getColumnValues("instrumenttypeid", ";"));
                props.setProperty("instrumentid", dsAddInstrument.getColumnValues("instrumentid", ";"));
                props.setProperty("usersequence", dsAddInstrument.getColumnValues("usersequence", ";"));
                props.setProperty(COLUMN_S_BATCHID, dsAddInstrument.getColumnValues(COLUMN_S_BATCHID, ";"));
                props.setProperty("batchstageid", dsAddInstrument.getColumnValues("batchstageid", ";"));
                ap.processAction("AddSDI", "1", props);
            }
        }
        catch (Exception e) {
            this.logger.error("Error while creating Stages and Instruments for Formulation Batch", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("prodStages");
            this.database.closeStatement("prodInstruments");
            this.database.closeStatement("prodstage_workitems");
            this.database.closeStatement("prodstage_specs");
        }
    }

    private void getBatchesWithSimpleProducts(DataSet primary) throws SapphireException {
        this.batchesWithSimpleProduct = new HashSet<String>();
        String productIds = null;
        String productVersionIds = null;
        String batchIds = null;
        if (primary.isValidColumn(COLUMN_PRODUCTID)) {
            productIds = primary.getColumnValues(COLUMN_PRODUCTID, ";");
            productVersionIds = primary.getColumnValues(COLUMN_PRODUCTVERSIONID, ";");
            batchIds = primary.getColumnValues(COLUMN_S_BATCHID, ";");
        } else {
            StringBuffer productIdBuffer = new StringBuffer();
            StringBuffer productVersionIdBuffer = new StringBuffer();
            StringBuffer batchIdBuffer = new StringBuffer();
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String tempProductId = this.getOldPrimaryValue(primary, i, COLUMN_PRODUCTID);
                if (tempProductId == null || tempProductId.length() <= 0) continue;
                productIdBuffer.append(";").append(tempProductId);
                productVersionIdBuffer.append(";").append(this.getOldPrimaryValue(primary, i, COLUMN_PRODUCTVERSIONID));
                batchIdBuffer.append(";").append(primary.getValue(i, COLUMN_S_BATCHID, ""));
            }
            if (productIdBuffer.length() > 0) {
                productIds = productIdBuffer.substring(1);
                productVersionIds = productVersionIdBuffer.substring(1);
                batchIds = batchIdBuffer.substring(1);
            }
        }
        if (productIds != null && productIds.length() > 0) {
            String rsetid = this.getDAMProcessor().createRSet("Product", productIds, productVersionIds, null);
            StringBuffer productSql = new StringBuffer();
            productSql.append("SELECT ");
            productSql.append("s_productid");
            productSql.append(", ").append("s_productversionid");
            productSql.append(", ").append("productmodeflag");
            productSql.append(" FROM ").append("s_product").append(", rsetitems");
            productSql.append(" WHERE ");
            productSql.append(" rsetitems.sdcid = '").append("Product").append("'");
            productSql.append(" AND rsetitems.keyid1 = ").append("s_product").append(".").append("s_productid");
            productSql.append(" AND rsetitems.keyid2 = ").append("s_product").append(".").append("s_productversionid");
            productSql.append(" AND ").append("productmodeflag").append(" = '").append("S").append("'");
            DataSet simpleProductDs = this.getQueryProcessor().getSqlDataSet("simpleproductds", productSql.toString());
            this.getDAMProcessor().clearRSet(rsetid);
            String[] batchIdArray = StringUtil.split(batchIds, ";");
            String[] productIdArray = StringUtil.split(productIds, ";");
            String[] productVersionArray = StringUtil.split(productVersionIds, ";");
            for (int i = 0; i < batchIdArray.length; ++i) {
                HashMap<String, String> find = new HashMap<String, String>();
                find.put("s_productid", productIdArray[i]);
                find.put("s_productversionid", productVersionArray[i]);
                int index = simpleProductDs.findRow(find);
                if (index < 0) continue;
                this.batchesWithSimpleProduct.add(batchIdArray[i]);
            }
        }
    }

    private void createSamplesUsingProduct(DataSet primary, boolean isSimpleProductMode) throws SapphireException {
        DataSet dsAddSDIProps = new DataSet();
        dsAddSDIProps.addColumn("copies", 1);
        dsAddSDIProps.addColumn("templateid", 0);
        dsAddSDIProps.addColumn(COLUMN_PRODUCTID, 0);
        dsAddSDIProps.addColumn(COLUMN_PRODUCTVERSIONID, 0);
        dsAddSDIProps.addColumn("sampletypeid", 0);
        dsAddSDIProps.addColumn("batchid", 0);
        dsAddSDIProps.addColumn("sampledesc", 0);
        String query = "SELECT s_productid, s_productversionid, productdesc, batchsamplecount, batchsampletemplateid FROM s_product WHERE s_productid= ? AND s_productversionid=?";
        if (isSimpleProductMode) {
            query = query + " AND productmodeflag = 'S'";
            dsAddSDIProps.addColumn("copyfromsamplingplanlevelid", 0);
        } else {
            query = query + " AND ( productmodeflag IS NULL OR productmodeflag != 'S')";
        }
        PreparedStatement prodStmt = this.database.prepareStatement("productquery", query);
        try {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String productId = primary.getValue(i, COLUMN_PRODUCTID, "");
                String productVersionId = primary.getValue(i, COLUMN_PRODUCTVERSIONID, "");
                String sampleTypeId = primary.getValue(i, "batchtype", "");
                String batchId = primary.getValue(i, COLUMN_S_BATCHID);
                int batchSampleCount = 0;
                String batchTemplateId = "";
                if (productId.trim().length() <= 0) continue;
                prodStmt.setString(1, productId);
                prodStmt.setString(2, productVersionId);
                DataSet prodDS = new DataSet(prodStmt.executeQuery());
                if (prodDS == null || prodDS.getRowCount() <= 0) continue;
                batchSampleCount = prodDS.getInt(0, "batchsamplecount", 0);
                batchTemplateId = prodDS.getValue(0, "batchsampletemplateid", "");
                if (batchSampleCount <= 0) continue;
                int row = dsAddSDIProps.addRow();
                String productDesc = prodDS.getValue(0, "productdesc", "");
                dsAddSDIProps.setString(row, COLUMN_PRODUCTID, productId);
                dsAddSDIProps.setString(row, COLUMN_PRODUCTVERSIONID, productVersionId);
                dsAddSDIProps.setString(row, "sampletypeid", sampleTypeId);
                dsAddSDIProps.setString(row, "batchid", batchId);
                dsAddSDIProps.setString(row, "templateid", batchTemplateId);
                dsAddSDIProps.setNumber(row, "copies", batchSampleCount);
                if (productDesc.length() > 0) {
                    dsAddSDIProps.setString(row, "sampledesc", productDesc);
                }
                if (!isSimpleProductMode) continue;
                String levelId = primary.getValue(i, COLUMN_LEVELID, this.getOldPrimaryValue(primary, i, COLUMN_LEVELID));
                dsAddSDIProps.setString(row, "copyfromsamplingplanlevelid", levelId);
            }
            dsAddSDIProps.sort("templateid");
            ArrayList<DataSet> dsAddSDIPropList = dsAddSDIProps.getGroupedDataSets("templateid");
            StringBuffer sql4MaxSeq = new StringBuffer();
            sql4MaxSeq.append("SELECT max(usersequence) maxusersequence FROM s_sample WHERE batchid = ?");
            PreparedStatement getMaxUserSequence = this.database.prepareStatement("maxuseq", sql4MaxSeq.toString());
            String sampleIds = "";
            for (int k = 0; k < dsAddSDIPropList.size(); ++k) {
                DataSet groupDataSet = dsAddSDIPropList.get(k);
                StringBuffer prodIds = new StringBuffer();
                StringBuffer prodVerIds = new StringBuffer();
                StringBuffer batchIds = new StringBuffer();
                StringBuffer sampleTypes = new StringBuffer();
                StringBuffer sampleDesc = new StringBuffer();
                StringBuffer userSequence = new StringBuffer();
                StringBuffer copyFromSamplingPlanLevelId = new StringBuffer();
                int sampleCount = 0;
                for (int i = 0; i < groupDataSet.getRowCount(); ++i) {
                    int noofSamples = groupDataSet.getInt(i, "copies");
                    sampleCount += noofSamples;
                    int nextSequence = 0;
                    if (noofSamples > 0) {
                        getMaxUserSequence.setString(1, groupDataSet.getString(i, "batchid"));
                        nextSequence = new DataSet(getMaxUserSequence.executeQuery()).getInt(0, "maxusersequence", 0);
                    }
                    while (noofSamples > 0) {
                        prodIds.append(";").append(groupDataSet.getString(i, COLUMN_PRODUCTID));
                        prodVerIds.append(";").append(groupDataSet.getString(i, COLUMN_PRODUCTVERSIONID));
                        batchIds.append(";").append(groupDataSet.getString(i, "batchid"));
                        sampleTypes.append(";").append(groupDataSet.getString(i, "sampletypeid"));
                        sampleDesc.append(";").append(groupDataSet.getString(i, "sampledesc", ""));
                        userSequence.append(";").append(++nextSequence);
                        if (isSimpleProductMode) {
                            copyFromSamplingPlanLevelId.append(";").append(groupDataSet.getString(i, "copyfromsamplingplanlevelid", ""));
                        }
                        --noofSamples;
                    }
                }
                String sampleTemplateId = groupDataSet.getValue(0, "templateid");
                PropertyList actionPropertyList = new PropertyList();
                actionPropertyList.setProperty("sdcid", "Sample");
                actionPropertyList.setProperty(COLUMN_PRODUCTID, prodIds.substring(1));
                actionPropertyList.setProperty(COLUMN_PRODUCTVERSIONID, prodVerIds.substring(1));
                actionPropertyList.setProperty("batchid", batchIds.substring(1));
                actionPropertyList.setProperty("sampletypeid", sampleTypes.substring(1));
                if (isSimpleProductMode) {
                    actionPropertyList.setProperty("copyfromsamplingplanlevelid", copyFromSamplingPlanLevelId.substring(1));
                }
                actionPropertyList.setProperty("copies", String.valueOf(sampleCount));
                actionPropertyList.setProperty("applyworkitems", "Y");
                if (sampleTemplateId.trim().length() > 0) {
                    actionPropertyList.setProperty("templateid", sampleTemplateId);
                }
                actionPropertyList.setProperty("usersequence", userSequence.substring(1));
                this.getActionProcessor().processAction("AddSDI", "1", actionPropertyList);
                sampleIds = sampleIds + ";" + actionPropertyList.getProperty("newkeyid1");
            }
            if (sampleIds.length() > 0 && !isSimpleProductMode) {
                sampleIds = sampleIds.substring(1);
                String rSetId = this.getRSet("Sample", sampleIds, null, null);
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT s_sampleid, productid, productversionid FROM s_sample WHERE s_sampleid IN( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rSetId) + ")";
                DataSet dsSampProdComb = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                this.getDAMProcessor().clearRSet(rSetId);
                if (dsSampProdComb != null && dsSampProdComb.getRowCount() > 0) {
                    PropertyList detailPropertyList = new PropertyList();
                    detailPropertyList.setProperty("sdcid", "Sample");
                    detailPropertyList.setProperty("keyid1", dsSampProdComb.getColumnValues("s_sampleid", ";"));
                    detailPropertyList.setProperty("sourcesdcid", "Product");
                    detailPropertyList.setProperty("sourcekeyid1", dsSampProdComb.getColumnValues(COLUMN_PRODUCTID, ";"));
                    detailPropertyList.setProperty("sourcekeyid2", dsSampProdComb.getColumnValues(COLUMN_PRODUCTVERSIONID, ";"));
                    detailPropertyList.setProperty("copydataset", "Y");
                    detailPropertyList.setProperty("copyspec", "Y");
                    detailPropertyList.setProperty("copyworkitem", "Y");
                    detailPropertyList.setProperty("usecurrentversion", "Y");
                    detailPropertyList.setProperty("applysourceworkitem", "Y");
                    this.getActionProcessor().processAction("CopySDIDetail", "1", detailPropertyList);
                }
            }
        }
        catch (Exception e) {
            this.logger.error("Error in executing method createSamplesUsingProduct", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("productquery");
        }
    }

    private String getRSet(String sdcid, String keyid1, String keyid2, String keyid3) throws SapphireException {
        return this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
    }

    @Override
    public void postApprove(DataSet approve) throws SapphireException {
        try {
            DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, approve);
            DataSet dsProp = new DataSet();
            for (int i = 0; i < approvedDS.size(); ++i) {
                int newRow = dsProp.addRow();
                String approvalFlag = approvedDS.getValue(i, "approvalflag");
                String batchstatus = "Pass".equalsIgnoreCase(approvalFlag) ? BatchLifeCycleUtil.getBatchStateDisplayValue("released") : BatchLifeCycleUtil.getBatchStateDisplayValue("rejected");
                dsProp.setString(newRow, "keyid1", approvedDS.getValue(i, "keyid1"));
                dsProp.setString(newRow, COLUMN_BATCHSTATUS, batchstatus);
            }
            if (dsProp.size() > 0) {
                String sdcId = SDCID;
                PropertyList props = new PropertyList();
                props.put("sdcid", sdcId);
                props.put("keyid1", dsProp.getColumnValues("keyid1", ";"));
                props.put(COLUMN_BATCHSTATUS, dsProp.getColumnValues(COLUMN_BATCHSTATUS, ";"));
                props.put("tracelogid", approve.getString(0, "tracelogid", ""));
                props.put("releasedby", this.getConnectionInfo().getSysuserId());
                props.put("releaseddt", new M18NUtil(this.connectionInfo).format(DateTimeUtil.getNowCalendar()));
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

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public boolean requiresBeforeEditDetailImage() {
        return true;
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.adjustParentBatchSize(sdiData, actionProps);
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.adjustParentBatchSize(sdiData, actionProps);
    }

    private void createTrackItem(DataSet primary) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        DataSet ds = new DataSet();
        String sqlProdDetails = "SELECT containertypeid,expectedbatchsize,expectedbatchsizeunits FROM s_product WHERE s_productid=? and  s_productversionid=?";
        PreparedStatement trackitemPS = this.database.prepareStatement("trackitemPS", "select trackitemid,qtycurrent,qtyunits from trackitem  where linksdcid=? and linkkeyid1=?");
        DataSet trackItemDS = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            String keyid1 = primary.getValue(i, COLUMN_S_BATCHID);
            String batchsize = primary.getValue(i, this.COLUMN_FINISHEDBATCHSIZE, "");
            String batchsizeunits = primary.getValue(i, this.COLUMN_FINISHEDBATCHSIZEUNITS, "");
            String productid = primary.getValue(i, COLUMN_PRODUCTID, "");
            String productversionid = primary.getValue(i, COLUMN_PRODUCTVERSIONID, "");
            try {
                trackitemPS.setString(1, SDCID);
                trackitemPS.setString(2, keyid1);
                trackItemDS = new DataSet(trackitemPS.executeQuery());
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            if (keyid1.trim().length() <= 0 || batchsize.trim().length() <= 0 || trackItemDS.getRowCount() > 0) continue;
            int row = ds.addRow();
            if (productid.length() > 0) {
                try {
                    this.database.createPreparedResultSet("productdetail", sqlProdDetails, new Object[]{productid, productversionid});
                    if (this.database.getNext("productdetail")) {
                        ds.setString(row, "containertypeid", this.database.getValue("productdetail", "containertypeid"));
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("Error in retrieving product detail.", e);
                }
                finally {
                    this.database.closeResultSet("productdetail");
                }
            }
            ds.setString(row, "linksdcid", SDCID);
            ds.setString(row, "linkkeyid1", keyid1);
            ds.setString(row, "qtycurrent", batchsize);
            ds.setString(row, "qtyunits", batchsizeunits);
            ds.setString(row, "trackitemstatus", "Valid");
        }
        this.database.closeStatement("trackitemPS");
        if (ds.size() > 0) {
            try {
                PropertyList trackitemProps = new PropertyList();
                trackitemProps.setProperty("sdcid", "TrackItemSDC");
                trackitemProps.setProperty("copies", Integer.toString(ds.size()));
                for (int col = 0; col < ds.getColumnCount(); ++col) {
                    String columnid = ds.getColumnId(col);
                    trackitemProps.setProperty(columnid, ds.getColumnValues(columnid, ";"));
                }
                this.getActionProcessor().processAction("AddSDI", "1", trackitemProps);
            }
            catch (Exception ex) {
                this.setError(this.ruleid, "VALIDATION", tp.translate("Failed to create trackitems for the new Batch"));
            }
        }
    }

    private double getMultiplicationFactor(DataSet primary, int rowIndx) throws SapphireException {
        double mulFactor = 1.0;
        double initialAmount = primary.getBigDecimal(rowIndx, this.COLUMN_MIXINGBATCHSIZE, new BigDecimal(0.0)).doubleValue();
        String initialAmountUnit = primary.getString(rowIndx, this.COLUMN_MIXINGBATCHSIZEUNITS, "");
        double expectedAmount = 0.0;
        String expectedAmountUnit = "";
        PreparedStatement productPS = this.database.prepareStatement("productps", "SELECT expectedbatchsize,expectedbatchsizeunits FROM s_product WHERE s_productid = ? AND s_productversionid = ? ORDER BY usersequence");
        try {
            productPS.setString(1, primary.getValue(rowIndx, COLUMN_PRODUCTID, ""));
            productPS.setString(2, primary.getValue(rowIndx, COLUMN_PRODUCTVERSIONID, ""));
            DataSet productDS = new DataSet(productPS.executeQuery());
            if (productDS.size() > 0) {
                expectedAmount = productDS.getBigDecimal(0, "expectedbatchsize", new BigDecimal(0.0)).doubleValue();
                expectedAmountUnit = productDS.getString(0, "expectedbatchsizeunits", "");
            }
        }
        catch (Exception e) {
            this.logger.error("Error in fetching product ", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("productps");
        }
        boolean isUnitMaches = initialAmountUnit.equalsIgnoreCase(expectedAmountUnit);
        if (initialAmount > 0.0 && expectedAmount > 0.0) {
            mulFactor = isUnitMaches ? initialAmount / expectedAmount : ReagentUtil.getConvertedValue(initialAmount, initialAmountUnit, expectedAmountUnit, this.database) / expectedAmount;
        }
        return mulFactor;
    }

    private void updateIngredientTargetAmount(DataSet primary) throws SapphireException {
        PreparedStatement productFormulationPS = this.database.prepareStatement("productformulation", "SELECT * FROM s_productformulation WHERE s_productid = ? AND s_productversionid = ? ORDER BY usersequence");
        PreparedStatement batchgenealogyPS = this.database.prepareStatement("batchgenealogy", "SELECT * FROM s_batchgenealogy WHERE s_batchid = ? ORDER BY usersequence");
        PreparedStatement ingredientSdidataItemPS = this.database.prepareStatement("ingredientsdidataitem", "select item.transformvalue,item.displayvalue,item.displayunits,item.paramid,item.paramtype,item.enteredvalue,item.enteredtext,item.enteredunits from sdidataitem item,s_productformulation pf where item.keyid1=pf.s_productid  and  item.keyid2=pf.s_productversionid and  item.keyid3=pf.s_productitemid and sdcid = 'LV_ProductIngredient'  and item.keyid1 = ? and item.keyid2 = ? and item.paramtype in(?,?,?) ORDER BY pf.usersequence");
        PreparedStatement productPS = this.database.prepareStatement("product", "select * from s_product where s_productid=? and s_productversionid=?");
        try {
            for (int rowIndx = 0; rowIndx < primary.size(); ++rowIndx) {
                if (!this.hasPrimaryValueChanged(primary, rowIndx, this.COLUMN_MIXINGBATCHSIZE) && !this.hasPrimaryValueChanged(primary, rowIndx, this.COLUMN_MIXINGBATCHSIZEUNITS)) continue;
                double multifactor = this.getMultiplicationFactor(primary, rowIndx);
                String batchmode = primary.getString(rowIndx, "batchmode", "");
                String batchid = primary.getString(rowIndx, COLUMN_S_BATCHID, "");
                String productid = primary.getString(rowIndx, COLUMN_PRODUCTID, "");
                String productversionid = primary.getString(rowIndx, COLUMN_PRODUCTVERSIONID, "");
                productFormulationPS.setString(1, productid);
                productFormulationPS.setString(2, productversionid);
                DataSet productFormulation = new DataSet(productFormulationPS.executeQuery());
                productPS.setString(1, primary.getValue(rowIndx, COLUMN_PRODUCTID, ""));
                productPS.setString(2, primary.getValue(rowIndx, COLUMN_PRODUCTVERSIONID, ""));
                DataSet productDS = new DataSet(productPS.executeQuery());
                String recipetypeFlag = "";
                if (productDS.getRowCount() > 0) {
                    recipetypeFlag = productDS.getString(0, "recipetypeflag", "");
                }
                ingredientSdidataItemPS.setString(1, primary.getValue(rowIndx, COLUMN_PRODUCTID, ""));
                ingredientSdidataItemPS.setString(2, primary.getValue(rowIndx, COLUMN_PRODUCTVERSIONID, ""));
                if (recipetypeFlag.equalsIgnoreCase("A")) {
                    ingredientSdidataItemPS.setString(3, this.PARAMTYPE_QUANTITY);
                } else {
                    ingredientSdidataItemPS.setString(3, this.PARAMTYPE_FRACTION);
                }
                ingredientSdidataItemPS.setString(4, this.PARAMTYPE_LOWERBOUND);
                ingredientSdidataItemPS.setString(5, this.PARAMTYPE_UPPERBOUND);
                DataSet ingredientSdidataItem = new DataSet(ingredientSdidataItemPS.executeQuery());
                batchgenealogyPS.setString(1, batchid);
                DataSet batchgenealogyDS = new DataSet(batchgenealogyPS.executeQuery());
                if (batchgenealogyDS.getRowCount() <= 0 || productFormulation.getRowCount() != batchgenealogyDS.getRowCount() && ingredientSdidataItem.getRowCount() != batchgenealogyDS.getRowCount()) continue;
                this.logger.info("Updating genealogy table on change of Initial Batch Size.");
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", SDCID);
                    props.setProperty("linkid", "batch genealogy");
                    props.setProperty(COLUMN_S_BATCHID, batchid);
                    props.setProperty("s_batchitemid", batchgenealogyDS.getColumnValues("s_batchitemid", ";"));
                    if (ingredientSdidataItem.getRowCount() > 0) {
                        this.populateTargetAmount(props, ingredientSdidataItem, multifactor, recipetypeFlag, primary, rowIndx, false);
                    } else {
                        String amount = this.applyMultiFactorOnProductFormulationAmount(productFormulation, "amount", multifactor);
                        props.setProperty("amounttarget", amount);
                        props.setProperty("amouttargettext", amount);
                    }
                    this.getActionProcessor().processAction("EditSDIDetail", "1", props);
                    continue;
                }
                catch (SapphireException e) {
                    throw new SapphireException(e);
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("productformulation");
            this.database.closeStatement("batchgenealogy");
            this.database.closeStatement("ingredientsdidataitem");
            this.database.closeStatement("product");
        }
    }

    private String applyMultiFactorOnProductFormulationAmount(DataSet ds, String fieldid, double multifactor) {
        StringBuffer amountStr = new StringBuffer();
        for (int i = 0; i < ds.size(); ++i) {
            if (ds.getValue(i, fieldid, "").trim().length() > 0) {
                BigDecimal getBigDecimalAmout = ds.getBigDecimal(i, fieldid, new BigDecimal("0.0"));
                BigDecimal amount = new BigDecimal(getBigDecimalAmout.doubleValue() * multifactor).setScale(3, 4);
                amountStr.append(";").append(amount.toString());
                continue;
            }
            amountStr.append(";").append("");
        }
        return amountStr.length() > 0 ? UnitsUtil.convertToLocateSeperated(amountStr.substring(1), "" + FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator()) : "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void adjustParentBatchSize(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet oldGenealogyDS = this.getBeforeEditImage() == null ? new DataSet() : this.getBeforeEditImage().getDataset("s_batchgenealogy");
        DataSet genealogyDS = sdiData.getDataset("s_batchgenealogy");
        DataSet trackitemDetails = new DataSet();
        trackitemDetails.addColumn("trackitemid", 0);
        trackitemDetails.addColumn("quantity", 0);
        trackitemDetails.addColumn("quantityunit", 0);
        try {
            PreparedStatement trackitemPS = this.database.prepareStatement("trackitemPS", "select trackitemid from trackitem  where linkkeyid1=?");
            if (genealogyDS != null && genealogyDS.size() > 0 && genealogyDS.isValidColumn("amountactual")) {
                for (int i = 0; i < genealogyDS.size(); ++i) {
                    String parentBatchId = genealogyDS.getString(i, "parentbatchid", "");
                    double amountActual = genealogyDS.getBigDecimal(i, "amountactual", new BigDecimal(0)).doubleValue();
                    String amountUnit = genealogyDS.getString(i, "amountunits", "");
                    String batchid = genealogyDS.getString(i, COLUMN_S_BATCHID, "");
                    String batchitemid = genealogyDS.getString(i, "s_batchitemid", "");
                    if (parentBatchId.length() > 0 && genealogyDS.getValue(i, "amountactual", "").length() > 0) {
                        this.populateTrackItemDetails(trackitemPS, trackitemDetails, amountActual, amountUnit, parentBatchId, true);
                    }
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put(COLUMN_S_BATCHID, batchid);
                    filter.put("s_batchitemid", batchitemid);
                    int oldInx = oldGenealogyDS.findRow(filter);
                    String oldParentBatchid = oldGenealogyDS.getString(oldInx, "parentbatchid", "");
                    double oldAmountActual = oldGenealogyDS.getBigDecimal(oldInx, "amountactual", new BigDecimal(0)).doubleValue();
                    String oldAmountUnit = oldGenealogyDS.getString(oldInx, "amountunits", "");
                    if (oldParentBatchid.length() <= 0 || oldGenealogyDS.getValue(oldInx, "amountactual", "").length() <= 0) continue;
                    this.populateTrackItemDetails(trackitemPS, trackitemDetails, oldAmountActual, oldAmountUnit, oldParentBatchid, false);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            this.database.closeStatement("trackitemPS");
        }
        if (trackitemDetails.size() > 0) {
            PropertyList trackItemProperties = new PropertyList();
            trackItemProperties.setProperty("trackitemid", trackitemDetails.getColumnValues("trackitemid", ";"));
            trackItemProperties.setProperty("quantity", trackitemDetails.getColumnValues("quantity", ";"));
            trackItemProperties.setProperty("quantityunit", trackitemDetails.getColumnValues("quantityunit", ";"));
            try {
                this.getActionProcessor().processAction("AdjustTrackItemInv", "1", trackItemProperties);
            }
            catch (SapphireException e) {
                this.setError(this.ruleid, "FAILURE", "Failed to adjust container amount. Check the use amount and units.Detail error:" + e.getMessage());
            }
        }
    }

    private void populateTrackItemDetails(PreparedStatement trackitemPS, DataSet parentBatchDetails, double amountActual, String amountUnit, String batchID, boolean deduct) throws SQLException {
        String trackitemid;
        trackitemPS.setString(1, batchID);
        DataSet trackitemDS = new DataSet(trackitemPS.executeQuery());
        if (trackitemDS.size() > 0 && (trackitemid = trackitemDS.getString(0, "trackitemid", "")).length() > 0) {
            int row = parentBatchDetails.addRow();
            parentBatchDetails.setString(row, "trackitemid", trackitemid);
            parentBatchDetails.setString(row, "quantity", "" + (deduct ? 0.0 - amountActual : amountActual));
            parentBatchDetails.setString(row, "quantityunit", amountUnit);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void resetTrackItemAmount(PropertyList actionProps) throws SapphireException {
        String availablebatchsize = actionProps.getProperty("availablebatchsize", "");
        String availablebatchsizeunit = actionProps.getProperty("availablebatchsizeunit", "");
        String batchid = actionProps.getProperty("keyid1", "");
        DataSet trackitemDetails = new DataSet();
        trackitemDetails.addColumn("trackitemid", 0);
        trackitemDetails.addColumn("qtycurrent", 0);
        trackitemDetails.addColumn("qtyunits", 0);
        if (availablebatchsize.length() > 0) {
            String[] batchidArr = StringUtil.split(batchid, ";");
            String[] availablebatchsizeArr = StringUtil.split(availablebatchsize, ";");
            String[] availablebatchsizeunitArr = StringUtil.split(availablebatchsizeunit, ";");
            try {
                PreparedStatement trackitemPS = this.database.prepareStatement("trackitemPS", "select trackitemid,qtycurrent,qtyunits from trackitem  where linksdcid=? and linkkeyid1=?");
                for (int i = 0; i < batchidArr.length; ++i) {
                    batchid = batchidArr[i];
                    availablebatchsize = availablebatchsizeArr[i];
                    availablebatchsizeunit = availablebatchsizeunitArr[i];
                    trackitemPS.setString(1, SDCID);
                    trackitemPS.setString(2, batchid);
                    DataSet trackItemDS = new DataSet(trackitemPS.executeQuery());
                    if (trackItemDS.size() <= 0) continue;
                    String oldAmount = trackItemDS.getValue(0, "qtycurrent", "");
                    String oldAmountUnit = trackItemDS.getString(0, "qtyunits", "");
                    if (oldAmount.equalsIgnoreCase(availablebatchsize) && oldAmountUnit.equalsIgnoreCase(availablebatchsizeunit)) continue;
                    int indx = trackitemDetails.addRow();
                    trackitemDetails.setString(indx, "trackitemid", trackItemDS.getString(0, "trackitemid", ""));
                    trackitemDetails.setString(indx, "qtycurrent", availablebatchsize);
                    trackitemDetails.setString(indx, "qtyunits", availablebatchsizeunit);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            finally {
                this.database.closeStatement("trackitemPS");
            }
            if (trackitemDetails.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("keyid1", trackitemDetails.getColumnValues("trackitemid", ";"));
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("qtycurrent", trackitemDetails.getColumnValues("qtycurrent", ";"));
                props.setProperty("qtyunits", trackitemDetails.getColumnValues("qtyunits", ";"));
                try {
                    this.getActionProcessor().processAction("EditSDI", "1", props);
                }
                catch (SapphireException e) {
                    this.setError(this.ruleid, "FAILURE", "Failed to adjust Trackitem amount. Check the Available Batch Size and Unit.Detail error:" + e.getMessage());
                }
            }
        }
    }

    private void populateTargetAmount(PropertyList props, DataSet ingredientSdidataItem, double multifactor, String recipetypeFlag, DataSet primary, int rowIndx, boolean isAddMode) throws SapphireException {
        this.formatUtil = FormatUtil.getInstance(this.connectionInfo);
        if (ingredientSdidataItem.getRowCount() > 0) {
            DataSet targetAmountDS;
            HashMap<String, String> hm = new HashMap<String, String>();
            if (recipetypeFlag.equalsIgnoreCase("A")) {
                hm.put("paramtype", this.PARAMTYPE_QUANTITY);
                targetAmountDS = ingredientSdidataItem.getFilteredDataSet(hm);
                String amount = this.applyMultiFactorOnProductFormulationAmount(targetAmountDS, this.SDIDATAITEM_VALUEFIELD, multifactor);
                props.setProperty("amounttarget", amount);
                props.setProperty("amouttargettext", amount);
                if (isAddMode) {
                    props.setProperty("amountunits", this.getUnits(targetAmountDS));
                }
            } else {
                hm.put("paramtype", this.PARAMTYPE_FRACTION);
                targetAmountDS = ingredientSdidataItem.getFilteredDataSet(hm);
                double initialAmount = primary.getBigDecimal(rowIndx, this.COLUMN_MIXINGBATCHSIZE, new BigDecimal(0.0)).doubleValue();
                String initialAmountUnit = primary.getString(rowIndx, this.COLUMN_MIXINGBATCHSIZEUNITS, "");
                StringBuffer amountStr = new StringBuffer();
                StringBuffer amountUnits = new StringBuffer();
                for (int i = 0; i < targetAmountDS.getRowCount(); ++i) {
                    double fraction = targetAmountDS.getBigDecimal(i, this.SDIDATAITEM_VALUEFIELD, new BigDecimal("0.0")).doubleValue();
                    String unit = targetAmountDS.getString(i, this.SDIDATAITEM_UNITFIELD, "");
                    double value = initialAmount * fraction;
                    if (unit.equals("%")) {
                        value /= 100.0;
                    }
                    BigDecimal amount = new BigDecimal(value).setScale(3, 4);
                    amountStr.append(";").append(amount.toString());
                    amountUnits.append(";").append(initialAmountUnit);
                }
                if (amountStr.length() > 0) {
                    String amount = UnitsUtil.convertToLocateSeperated(amountStr.substring(1), "" + FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator());
                    props.setProperty("amounttarget", amount);
                    props.setProperty("amouttargettext", amount);
                    if (isAddMode) {
                        props.setProperty("amountunits", amountUnits.substring(1));
                    }
                }
            }
            hm.clear();
            hm.put("paramtype", this.PARAMTYPE_LOWERBOUND);
            DataSet lowerToleranceDS = ingredientSdidataItem.getFilteredDataSet(hm);
            if (lowerToleranceDS.getRowCount() > 0) {
                props.setProperty(this.BATCHGENEALOGY_LOWERBOUND, this.calculateTolerances(lowerToleranceDS, targetAmountDS, multifactor, recipetypeFlag, props, true));
            }
            hm.clear();
            hm.put("paramtype", this.PARAMTYPE_UPPERBOUND);
            DataSet upperToleranceDS = ingredientSdidataItem.getFilteredDataSet(hm);
            if (upperToleranceDS.getRowCount() > 0) {
                props.setProperty(this.BATCHGENEALOGY_UPPERBOUND, this.calculateTolerances(upperToleranceDS, targetAmountDS, multifactor, recipetypeFlag, props, false));
            }
        }
    }

    private String calculateTolerances(DataSet toleranceDS, DataSet amountDS, double multifactor, String recipetypeFlag, PropertyList props, boolean isLowerTolerance) throws SapphireException {
        StringBuffer bound = new StringBuffer();
        String toleranceValue = "";
        String toleranceUnit = "";
        String amount = "";
        String amountUnit = "";
        String[] amounttargetArr = StringUtil.split(props.getProperty("amounttarget", ""), ";");
        String[] amountunitsArr = StringUtil.split(props.getProperty("amountunits", ""), ";");
        for (int i = 0; i < toleranceDS.getRowCount(); ++i) {
            toleranceValue = toleranceDS.getValue(i, this.SDIDATAITEM_VALUEFIELD, "");
            toleranceUnit = toleranceDS.getString(i, this.SDIDATAITEM_UNITFIELD, "");
            amount = amountDS.getValue(i, this.SDIDATAITEM_VALUEFIELD, "");
            amountUnit = amountDS.getString(i, this.SDIDATAITEM_UNITFIELD, "");
            if (toleranceValue.trim().length() > 0 && amount.trim().length() > 0) {
                double dAmounttarget;
                String sAmounttarget;
                BigDecimal toleranceAmount = toleranceDS.getBigDecimal(i, this.SDIDATAITEM_VALUEFIELD, new BigDecimal("0.0"));
                if (toleranceUnit.length() > 0) {
                    if (toleranceUnit.equals("%")) {
                        if (recipetypeFlag.equalsIgnoreCase("A")) {
                            BigDecimal amountBigDecimal = amountDS.getBigDecimal(i, this.SDIDATAITEM_VALUEFIELD, new BigDecimal("0.0"));
                            toleranceAmount = new BigDecimal(toleranceAmount.doubleValue() * amountBigDecimal.doubleValue() / 100.0);
                        } else if (amounttargetArr.length > i && (sAmounttarget = amounttargetArr[i]).length() > 0) {
                            dAmounttarget = this.formatUtil.parseBigDecimal(sAmounttarget).doubleValue() / multifactor;
                            toleranceAmount = new BigDecimal(toleranceAmount.doubleValue() * dAmounttarget / 100.0);
                        }
                    } else if (!toleranceUnit.equalsIgnoreCase(amountUnit)) {
                        try {
                            toleranceAmount = new BigDecimal(UnitsUtil.getConvertedValue(this.getQueryProcessor(), toleranceUnit, amountUnit, toleranceAmount.toString()));
                        }
                        catch (Exception e) {
                            throw new SapphireException("Error in unit conversion");
                        }
                    }
                    toleranceAmount = new BigDecimal(toleranceAmount.doubleValue() * multifactor);
                } else {
                    toleranceAmount = new BigDecimal(toleranceAmount.doubleValue() * multifactor);
                }
                if (amounttargetArr.length > i && (sAmounttarget = amounttargetArr[i]).length() > 0) {
                    dAmounttarget = this.formatUtil.parseBigDecimal(sAmounttarget).doubleValue();
                    toleranceAmount = new BigDecimal(Math.abs(toleranceAmount.doubleValue()));
                    toleranceAmount = isLowerTolerance ? new BigDecimal(dAmounttarget - toleranceAmount.doubleValue()) : new BigDecimal(dAmounttarget + toleranceAmount.doubleValue());
                }
                toleranceAmount = toleranceAmount.setScale(3, 4);
                bound.append(";").append(toleranceAmount.toString());
                continue;
            }
            bound.append(";").append("");
        }
        return bound.length() > 0 ? bound.substring(1) : "";
    }

    private String getUnits(DataSet ds) {
        String units = "";
        String unit = "";
        for (int i = 0; i < ds.getRowCount(); ++i) {
            unit = ds.getString(i, this.SDIDATAITEM_UNITFIELD, "");
            units = unit.length() > 0 ? units + ";" + unit : units + ";";
        }
        return units.length() > 1 ? units.substring(1) : units;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProperties) throws SapphireException {
        ActionProcessor ap = this.getActionProcessor();
        SafeSQL safeSQL = new SafeSQL();
        DataSet trackitemds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT trackitemid FROM trackitem WHERE linksdcid = 'Batch' and linkkeyid1 IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )", safeSQL.getValues());
        if (trackitemds.size() > 0) {
            String trackitemids = trackitemds.getColumnValues("trackitemid", ";");
            HashMap<String, String> properties = new HashMap<String, String>();
            properties.put("sdcid", "TrackItemSDC");
            properties.put("keyid1", trackitemids);
            ap.processAction("DeleteSDI", "1", properties);
        }
    }

    private void findCandidateForAutoRelease(DataSet primary, PropertyList policy) {
        if (policy != null) {
            String skipLevel = policy.getProperty("skiplevel", "");
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String pendingReleaseStatus;
                String batchstatus = primary.getString(i, COLUMN_BATCHSTATUS, "");
                if (!batchstatus.equals(pendingReleaseStatus = BatchLifeCycleUtil.getBatchStateDisplayValue("pendingrelease"))) continue;
                String batchid = primary.getString(i, COLUMN_S_BATCHID, "");
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder("SELECT autoreleaseflag FROM s_product, s_batch WHERE ");
                sql.append(" s_batch.s_batchid = ").append(safeSQL.addVar(batchid));
                sql.append(" AND s_product.s_productid = s_batch.productid ");
                sql.append(" AND s_product.s_productversionid = s_batch.productversionid ");
                sql.append(" AND s_product.autoreleaseflag = 'Y' ");
                sql.append(" AND (s_batch.levelid IS NULL OR s_batch.levelid != ").append(safeSQL.addVar(skipLevel)).append(")");
                DataSet autoReleaseReqBatchDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (autoReleaseReqBatchDS == null || autoReleaseReqBatchDS.getRowCount() <= 0) continue;
                primary.setString(i, "skipapprovalrulecheck", "Y");
            }
        }
    }

    static {
        whoDoneIt.addColumnPair("cancelleddt", "cancelledby", COLUMN_BATCHSTATUS, "=", "Cancelled");
        whoDoneIt.addColumnPair("cancelleddt", "cancelledby", COLUMN_BATCHSTATUS, "!=", "Cancelled");
        whoDoneIt.addColumnPair("holddt", "holdby", COLUMN_BATCHSTATUS, "=", BatchLifeCycleUtil.getBatchStateDisplayValue("onhold"));
        whoDoneIt.addColumnPair("holddt", "holdby", COLUMN_BATCHSTATUS, "!=", BatchLifeCycleUtil.getBatchStateDisplayValue("onhold"));
    }
}

