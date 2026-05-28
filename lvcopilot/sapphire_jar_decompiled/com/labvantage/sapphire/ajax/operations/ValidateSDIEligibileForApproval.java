/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.admin.ddt.BatchLifeCycleUtil;
import com.labvantage.sapphire.admin.ddt.LV_MonitorGroup;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ValidateSDIEligibileForApproval
extends BaseAjaxRequest {
    public static final String POLICY_NAME = "BatchSamplePolicy";
    public static final String POLICY_BATCHSTAGESYNC = "syncbatchwithstage";
    public static final String POLICY_BATCHAPPROVAL = "batchapprovalwithincidents";
    public static final String POLICY_BATCHSTAGEAPPROVAL = "batchstageapprovalwithincidents";
    public static final String POLICY_SAMPLEAPPROVAL = "sampleapprovalwithincidents";
    public static final String POLICY_REQUESTAPPROVAL = "requestapprovalwithincidents";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        PropertyList props = new PropertyList();
        try {
            String sdcid = ajaxResponse.getRequestParameter("sdcid");
            String keyid1 = ajaxResponse.getRequestParameter("keyid1");
            String keyid2 = ajaxResponse.getRequestParameter("keyid2");
            String keyid3 = ajaxResponse.getRequestParameter("keyid3");
            if (keyid2 == null) {
                keyid2 = "";
            }
            if (keyid3 == null) {
                keyid3 = "";
            }
            if ("Batch".equals(sdcid)) {
                props.putAll(ValidateSDIEligibileForApproval.verifyBatch(keyid1, ajaxResponse, this.getConfigurationProcessor(), this.getQueryProcessor(), this.getDAMProcessor()));
            } else if ("LV_BatchStage".equals(sdcid)) {
                props.putAll(ValidateSDIEligibileForApproval.verifyBatchStages(keyid1, ajaxResponse, this.getConfigurationProcessor(), this.getQueryProcessor(), this.getDAMProcessor()));
            } else if ("Sample".equals(sdcid)) {
                props.putAll(ValidateSDIEligibileForApproval.verifySamples(keyid1, ajaxResponse, this.getConfigurationProcessor(), this.getQueryProcessor(), this.getDAMProcessor()));
                props.putAll(ValidateSDIEligibileForApproval.checkApprovalTypes(keyid1, this.getQueryProcessor()));
            } else if ("Request".equals(sdcid)) {
                props.putAll(ValidateSDIEligibileForApproval.verifyRequest(keyid1, this.getConfigurationProcessor(), this.getQueryProcessor(), this.getDAMProcessor()));
            } else if ("LV_MonitorGroup".equals(sdcid)) {
                props.putAll(this.verifyMonitorGroup(keyid1, this.getQueryProcessor()));
            }
            props.setProperty("selectedKeyId1", keyid1);
            props.setProperty("selectedKeyId2", keyid2);
            props.setProperty("selectedKeyId3", keyid3);
            props.setProperty("status", String.valueOf(1));
        }
        catch (SapphireException e) {
            props.setProperty("status", String.valueOf(2));
        }
        ajaxResponse.addCallbackArgument("response", props);
        ajaxResponse.print();
    }

    private Map verifyMonitorGroup(String monitorGroupId, QueryProcessor qp) {
        PropertyList responseProps = new PropertyList();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT monitorgroupstatus FROM monitorgroup WHERE monitorgroupid = " + safeSQL.addVar(monitorGroupId);
        DataSet monitorGroupDs = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (monitorGroupDs.getRowCount() > 0) {
            boolean statusOk = monitorGroupDs.getValue(0, "monitorgroupstatus", "").equals("Pending Release");
            responseProps.setProperty("isStatusOk", String.valueOf(statusOk));
        }
        responseProps.setProperty("isAllTestsFinished", String.valueOf(LV_MonitorGroup.hasAllTestingFinished(monitorGroupId, qp)));
        return responseProps;
    }

    public static PropertyList verifyBatch(String batchId, AjaxResponse ajaxResponse, ConfigurationProcessor cp, QueryProcessor qp, DAMProcessor dp) throws SapphireException {
        boolean syncBatchReleaseWithStage;
        PropertyList responseProps = new PropertyList();
        PropertyList policy = cp.getPolicy(POLICY_NAME, "Sapphire Custom");
        String batchApprovalPolicy = policy.getProperty(POLICY_BATCHAPPROVAL, "Allow");
        responseProps.setProperty("batchApprovalPolicy", batchApprovalPolicy);
        if (!"Allow".equalsIgnoreCase(batchApprovalPolicy)) {
            String rsetId = dp.createRSet("Batch", batchId, "", "");
            DataSet openBatchIncidents = ValidateSDIEligibileForApproval.getOpenSDIIncidents("Batch", batchId, "", "", rsetId, qp, dp);
            responseProps.setProperty("openBatchIncidents", String.valueOf(openBatchIncidents.getRowCount()));
            StringBuffer batchSamplesSQL = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            batchSamplesSQL.append("SELECT distinct s_sampleid, batchstageid, batchid FROM s_sample s, rsetitems r").append(" WHERE s.batchid = r.keyid1").append(" AND r.sdcid = 'Batch' AND r.rsetid = ").append(safeSQL.addVar(rsetId));
            DataSet batchSamples = qp.getPreparedSqlDataSet(batchSamplesSQL.toString(), safeSQL.getValues());
            String batchStages = ValidateSDIEligibileForApproval.getUniqueBatchStages(batchSamples);
            if (batchStages.length() > 0) {
                DataSet openBatchStageIncidents = ValidateSDIEligibileForApproval.getOpenSDIIncidents("LV_BatchStage", batchStages, "", "", null, qp, dp);
                responseProps.setProperty("openBatchStageIncidents", String.valueOf(openBatchStageIncidents.getRowCount()));
            } else {
                responseProps.setProperty("openBatchStageIncidents", "0");
            }
            if (batchSamples.getRowCount() > 0) {
                DataSet openSampleIncidents = ValidateSDIEligibileForApproval.getOpenSDIIncidents("Sample", batchSamples.getColumnValues("s_sampleid", ";"), "", "", null, qp, dp);
                responseProps.setProperty("openSampleIncidents", String.valueOf(openSampleIncidents.getRowCount()));
            } else {
                responseProps.setProperty("openSampleIncidents", "0");
            }
            dp.clearRSet(rsetId);
        }
        responseProps.setProperty("isAllTestsFinished", String.valueOf(BatchLifeCycleUtil.hasAllTestsFinished(batchId, qp)));
        responseProps.setProperty("parentBatchesStatus", String.valueOf(BatchLifeCycleUtil.isParentBatchesReleased(batchId, qp, policy)));
        String syncBatchReleaseWithStageString = policy.getProperty(POLICY_BATCHSTAGESYNC, "N");
        boolean bl = syncBatchReleaseWithStage = !syncBatchReleaseWithStageString.equalsIgnoreCase("N");
        if (syncBatchReleaseWithStage) {
            responseProps.setProperty("isChildStagesReleased", String.valueOf(BatchLifeCycleUtil.isChildStagesReleased(batchId, qp, syncBatchReleaseWithStageString)));
        } else {
            responseProps.setProperty("isChildStagesReleased", String.valueOf(true));
        }
        return responseProps;
    }

    public static PropertyList verifyBatchStages(String batchStageId, AjaxResponse ajaxResponse, ConfigurationProcessor cp, QueryProcessor qp, DAMProcessor dp) throws SapphireException {
        DataSet releaseableStages;
        PropertyList responseProps = new PropertyList();
        PropertyList policy = cp.getPolicy(POLICY_NAME, "Sapphire Custom");
        String batchStageApprovalPolicy = policy.getProperty(POLICY_BATCHSTAGEAPPROVAL, "Allow");
        responseProps.setProperty("batchStageApprovalPolicy", batchStageApprovalPolicy);
        String rsetId = dp.createRSet("LV_BatchStage", batchStageId, "", "");
        if (!"Allow".equalsIgnoreCase(batchStageApprovalPolicy)) {
            DataSet openBatchStageIncidents = ValidateSDIEligibileForApproval.getOpenSDIIncidents("LV_BatchStage", batchStageId, "", "", rsetId, qp, dp);
            responseProps.setProperty("openBatchStageIncidents", String.valueOf(openBatchStageIncidents.getRowCount()));
            StringBuffer batchStageSamplesSQL = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            batchStageSamplesSQL.append("SELECT distinct s_sampleid, batchstageid FROM s_sample s, rsetitems r").append(" WHERE s.batchstageid = r.keyid1").append(" AND r.sdcid = 'LV_BatchStage' AND r.rsetid = ").append(safeSQL.addVar(rsetId));
            DataSet batchStageSamples = qp.getPreparedSqlDataSet(batchStageSamplesSQL.toString(), safeSQL.getValues());
            if (batchStageSamples.getRowCount() > 0) {
                DataSet openSampleIncidents = ValidateSDIEligibileForApproval.getOpenSDIIncidents("Sample", batchStageSamples.getColumnValues("s_sampleid", ";"), "", "", null, qp, dp);
                responseProps.setProperty("openSampleIncidents", String.valueOf(openSampleIncidents.getRowCount()));
            } else {
                responseProps.setProperty("openSampleIncidents", "0");
            }
        }
        responseProps.setProperty("isStageReleaseable", String.valueOf((releaseableStages = BatchLifeCycleUtil.getReleaseableStages(rsetId, qp)).getRowCount() > 0));
        dp.clearRSet(rsetId);
        return responseProps;
    }

    public static PropertyList checkApprovalTypes(String sampleIds, QueryProcessor qp) {
        PropertyList responseProps = new PropertyList();
        sampleIds = ValidateSDIEligibileForApproval.getUniqueValues(sampleIds, ";");
        String[] sampleIdsArr = StringUtil.split(sampleIds, ";");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select distinct keyid1 from sdiapproval where sdcid='Sample' and keyid1 in (" + safeSQL.addIn(sampleIds, ";") + ")";
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            if (ds.getRowCount() == sampleIdsArr.length) {
                responseProps.setProperty("approvalmode", "Y");
            } else {
                responseProps.setProperty("approvalmode", "H");
            }
        } else {
            responseProps.setProperty("approvalmode", "N");
        }
        return responseProps;
    }

    public static String getUniqueValues(String values, String delimeter) {
        HashMap<String, String> hm = new HashMap<String, String>();
        if (values != null && values.trim().length() > 0 && values.contains(delimeter)) {
            String[] valuesArr;
            for (String v : valuesArr = StringUtil.split(values, delimeter)) {
                hm.put(v, "");
            }
            values = String.join((CharSequence)delimeter, hm.keySet());
        }
        return values;
    }

    public static PropertyList verifySamples(String sampleIds, AjaxResponse ajaxResponse, ConfigurationProcessor cp, QueryProcessor qp, DAMProcessor dp) throws SapphireException {
        PropertyList responseProps = new PropertyList();
        PropertyList policy = cp.getPolicy(POLICY_NAME, "Sapphire Custom");
        String sampleApprovalPolicy = policy.getProperty(POLICY_SAMPLEAPPROVAL, "Allow");
        responseProps.setProperty("sampleApprovalPolicy", sampleApprovalPolicy);
        if (!"Allow".equalsIgnoreCase(sampleApprovalPolicy)) {
            DataSet openSampleIncidents = ValidateSDIEligibileForApproval.getOpenSDIIncidents("Sample", sampleIds, "", "", null, qp, dp);
            responseProps.setProperty("openSampleIncidents", String.valueOf(openSampleIncidents.getRowCount()));
        }
        return responseProps;
    }

    public static PropertyList verifyRequest(String requestId, ConfigurationProcessor cp, QueryProcessor qp, DAMProcessor dp) throws SapphireException {
        PropertyList responseProps = new PropertyList();
        PropertyList policy = cp.getPolicy(POLICY_NAME, "Sapphire Custom");
        String requestApprovalPolicy = policy.getProperty(POLICY_REQUESTAPPROVAL, "Allow");
        responseProps.setProperty("requestApprovalPolicy", requestApprovalPolicy);
        if (!"Allow".equalsIgnoreCase(requestApprovalPolicy)) {
            String rsetId = dp.createRSet("Request", requestId, "", "");
            DataSet openRequestIncidents = ValidateSDIEligibileForApproval.getOpenSDIIncidents("Request", requestId, "", "", rsetId, qp, dp);
            responseProps.setProperty("openRequestIncidents", String.valueOf(openRequestIncidents.getRowCount()));
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer requestSamplesSQL = new StringBuffer();
            requestSamplesSQL.append("SELECT distinct s_sampleid, requestid FROM s_sample s, rsetitems r").append(" WHERE s.requestid = r.keyid1").append(" AND r.sdcid = 'Request' AND r.rsetid = ").append(safeSQL.addVar(rsetId));
            DataSet requestSamples = qp.getPreparedSqlDataSet(requestSamplesSQL.toString(), safeSQL.getValues());
            if (requestSamples.getRowCount() > 0) {
                DataSet openSampleIncidents = ValidateSDIEligibileForApproval.getOpenSDIIncidents("Sample", requestSamples.getColumnValues("s_sampleid", ";"), "", "", null, qp, dp);
                responseProps.setProperty("openSampleIncidents", String.valueOf(openSampleIncidents.getRowCount()));
            } else {
                responseProps.setProperty("openSampleIncidents", "0");
            }
            dp.clearRSet(rsetId);
        }
        return responseProps;
    }

    public static DataSet getOpenSDIIncidents(String sdcId, String keyId1, String keyId2, String keyId3, String rsetId, QueryProcessor qp, DAMProcessor damProcessor) throws SapphireException {
        DataSet openIncidents = null;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        String[] keyId1Prop = StringUtil.split(keyId1, ";");
        boolean rsetCreated = false;
        if (keyId1Prop.length > 1) {
            if (rsetId == null || rsetId.length() == 0) {
                rsetId = damProcessor.createRSet(sdcId, keyId1, keyId2, keyId3);
                rsetCreated = true;
            }
            sql.append("SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3").append(" FROM incidentitem ii, incident i, rsetitems r").append(" WHERE ii.sourcesdcid = r.sdcid").append(" AND ii.sourcekeyid1 = r.keyid1");
            if (keyId2.length() > 0) {
                sql.append(" AND ii.sourcekeyid2 = r.keyid2");
            }
            if (keyId3.length() > 0) {
                sql.append(" AND ii.sourcekeyid3 = r.keyid3");
            }
            sql.append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND i.incidentid = ii.incidentid").append(" AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed')");
        } else {
            sql.append("SELECT i.incidentid, i.incidentstatus, ii. incidentitemid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2, ii.sourcekeyid3").append(" FROM incidentitem ii, incident i").append(" WHERE ii.sourcesdcid = ").append(safeSQL.addVar(sdcId)).append(" AND ii.sourcekeyid1 = ").append(safeSQL.addVar(keyId1));
            if (keyId2.length() > 0) {
                sql.append(" AND ii.sourcekeyid2 = ").append(safeSQL.addVar(keyId2));
            }
            if (keyId3.length() > 0) {
                sql.append(" AND ii.sourcekeyid3 = ").append(safeSQL.addVar(keyId3));
            }
            sql.append(" AND i.incidentid = ii.incidentid").append(" AND i.incidentstatus NOT IN ('Completed','Cancelled','Closed')");
        }
        openIncidents = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (rsetCreated && rsetId != null) {
            damProcessor.clearRSet(rsetId);
        }
        return openIncidents;
    }

    private static String getUniqueBatchStages(DataSet batchSamples) {
        StringBuffer batchStages = new StringBuffer();
        HashSet<String> uniqueBatchStages = new HashSet<String>();
        for (int i = 0; i < batchSamples.getRowCount(); ++i) {
            String batchStageId = batchSamples.getString(i, "batchstageid", "");
            if (batchStageId.length() <= 0) continue;
            uniqueBatchStages.add(batchStageId);
        }
        for (String batchStageId : uniqueBatchStages) {
            batchStages.append(";").append(batchStageId);
        }
        return batchStages.length() > 0 ? batchStages.substring(1).toString() : "";
    }
}

