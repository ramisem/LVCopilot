/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.qcactions.QCBaseAction;
import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.qcbatch.QCBatchSampleType;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.sql.common.Aqc;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SDIDataSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCEvalQCBatch
extends QCBaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53649 $";
    private int rc = 1;
    private SQLGenerator __SqlGenerator;
    private PropertyList __SpecInterpretationMap = new PropertyList();
    private String __SpecFailValue = "";
    private String __SpecWarningValue = "";
    private String __SpecPassValue = "";

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.getConnectionProcessor().isOra());
        if (actionid.equals("EvaluateQCBatch")) {
            try {
                this.__SpecInterpretationMap = OpalUtil.getSpecInterpretationMap(this.getConfigurationProcessor());
            }
            catch (Exception e) {
                this.logger.stackTrace(e);
            }
            this.__SpecFailValue = OpalUtil.getSpecCondition(this.__SpecInterpretationMap, "Fail").trim();
            if (this.__SpecFailValue.length() == 0) {
                this.__SpecFailValue = "Fail";
            }
            this.__SpecWarningValue = OpalUtil.getSpecCondition(this.__SpecInterpretationMap, "Warning").trim();
            if (this.__SpecWarningValue.length() == 0) {
                this.__SpecWarningValue = "Warning";
            }
            this.__SpecPassValue = OpalUtil.getSpecCondition(this.__SpecInterpretationMap, "Pass").trim();
            if (this.__SpecPassValue.length() == 0) {
                this.__SpecPassValue = "Pass";
            }
            this.rc = this.doEvaluateQCBatch(props);
        }
        return this.rc;
    }

    private int doEvaluateQCBatch(HashMap props) {
        String qcbatchid = (String)props.get("qcbatchid");
        String sdcid = (String)props.get("sdcid");
        String keyid1 = (String)props.get("keyid1");
        String keyid2 = (String)props.get("keyid2");
        String keyid3 = (String)props.get("keyid3");
        String paramlistid = (String)props.get("paramlistid");
        String paramlistversionid = (String)props.get("paramlistversionid");
        String variantid = (String)props.get("variantid");
        String dataset = (String)props.get("dataset");
        String updateStatus = (String)props.get("updatestatus");
        String success = (String)props.get("success");
        String evalQCBAttributes = (String)props.get("evalqcbatchattributes");
        List dslist = null;
        boolean updateStatusFlag = false;
        TranslationProcessor tp = this.getTranslationProcessor();
        if (qcbatchid == null || qcbatchid.trim().length() == 0) {
            return this.setError(tp.translate("[EvaluateQCBatch] Invalid input. 'QCBatchId' is required."));
        }
        String[] arrqcbatchid = StringUtil.split(qcbatchid, ";");
        String[] arrsdcid = StringUtil.split(sdcid, ";");
        String[] arrkeyid1 = StringUtil.split(keyid1, ";");
        String[] arrkeyid2 = StringUtil.split(keyid2, ";");
        String[] arrkeyid3 = StringUtil.split(keyid3, ";");
        String[] arrparamlistid = StringUtil.split(paramlistid, ";");
        String[] arrparamlistversionid = StringUtil.split(paramlistversionid, ";");
        String[] arrvariantid = StringUtil.split(variantid, ";");
        String[] arrdataset = StringUtil.split(dataset, ";");
        HashMap<String, QCBatch> qcBatchHolder = new HashMap<String, QCBatch>();
        QCBatch qcBatch = null;
        if (sdcid != null && sdcid.length() > 0 && (arrqcbatchid.length != arrsdcid.length || arrqcbatchid.length != arrkeyid1.length || arrqcbatchid.length != arrkeyid2.length || arrqcbatchid.length != arrkeyid3.length || arrqcbatchid.length != arrparamlistid.length || arrqcbatchid.length != arrparamlistversionid.length || arrqcbatchid.length != arrvariantid.length || arrqcbatchid.length != arrdataset.length)) {
            return this.setError(tp.translate("Invalid input: count of values sent in action properties not matching !"));
        }
        if (updateStatus != null && updateStatus.equalsIgnoreCase("Y")) {
            updateStatusFlag = true;
        }
        QueryProcessor qp = this.getQueryProcessor();
        for (int ind = 0; ind < arrqcbatchid.length; ++ind) {
            qcbatchid = arrqcbatchid[ind];
            if (qcbatchid != null && qcbatchid.length() > 0) {
                if (qcBatchHolder.containsKey(qcbatchid)) {
                    qcBatch = (QCBatch)qcBatchHolder.get(qcbatchid);
                } else {
                    qcBatch = QCBatchPool.getQCBatch(qp, qcbatchid);
                    if (qcBatch != null) {
                        qcBatchHolder.put(qcbatchid, qcBatch);
                    }
                }
            }
            if (qcBatch == null) {
                this.logger.debug("QC Batch does not exists: " + qcbatchid);
                continue;
            }
            qcBatch.setSQLGenerator(this.__SqlGenerator);
            String successAction = qcBatch.getActionSuccess();
            String failureAction = qcBatch.getActionFailure();
            if (evalQCBAttributes != null && evalQCBAttributes.equalsIgnoreCase("Y")) {
                this.setEvaluationStatus(qcbatchid, qp, successAction, failureAction, updateStatusFlag, true);
            } else {
                String qcBatchQueryId = qcBatch.getQCBatchQueryId();
                if (qcBatchQueryId == null || qcBatchQueryId.trim().length() == 0) {
                    this.logger.debug("[EvaluateQCBatch] Dataitems selection query not defined in QCBatch " + qcbatchid);
                }
                if ((dslist = sdcid == null || sdcid.length() == 0 ? qcBatch.getDataSets(true) : OpalUtil.getDataSetList(arrsdcid[ind], arrkeyid1[ind], arrkeyid2[ind], arrkeyid3[ind], arrparamlistid[ind], arrparamlistversionid[ind], arrvariantid[ind], arrdataset[ind], ";", qp)) != null) {
                    String keys = qcbatchid;
                    if (qcBatchQueryId != null && qcBatchQueryId.length() > 0) {
                        keys = qp.getKeyid1List("QCBatch", qcBatchQueryId, qcbatchid, null, null, null, null);
                        this.logger.debug("QCBatches returned by Query '" + qcBatchQueryId + "': " + keys);
                        if (keys == null || keys.length() == 0) {
                            this.logger.debug("QCBatch evaluation execution failed. QCBatch selection query failed to execute.");
                        }
                    }
                    if (keys != null && keys.length() > 0) {
                        ArrayList<String> qcBatchSampleTypeList = new ArrayList<String>();
                        for (int i = 0; i < dslist.size(); ++i) {
                            SDIDataSet sdiDataSet = (SDIDataSet)dslist.get(i);
                            String qcBatchSampleTypeId = sdiDataSet.getQCBatchProps(3);
                            if (qcBatchSampleTypeId == null || qcBatchSampleTypeId.length() <= 0 || qcBatchSampleTypeList.contains(qcBatchSampleTypeId)) continue;
                            qcBatchSampleTypeList.add(qcBatchSampleTypeId);
                        }
                        HashMap<String, String> actionProps = new HashMap<String, String>();
                        ActionProcessor ap = this.getActionProcessor();
                        for (int i = 0; i < qcBatchSampleTypeList.size(); ++i) {
                            String qcBatchSampleTypeId = (String)qcBatchSampleTypeList.get(i);
                            QCBatchSampleType qcBatchSampleType = new QCBatchSampleType(qp, qcBatchSampleTypeId);
                            String actionEval = qcBatchSampleType.getColumnValue("actioneval");
                            if (actionEval == null || actionEval.length() <= 0) continue;
                            actionProps.clear();
                            actionProps.put("qcbatchsampletypeid", qcBatchSampleTypeId);
                            actionProps.put("qcbatchid", keys);
                            try {
                                ap.processAction(actionEval, "1", actionProps);
                                continue;
                            }
                            catch (ActionException e) {
                                this.logger.error("Error running " + actionEval, e);
                            }
                        }
                        this.setEvaluationStatus(qcbatchid, qp, successAction, failureAction, updateStatusFlag, false);
                    } else {
                        this.logger.error(tp.translate("[EvaluateQCBatch] Error in returning QCBatches by selection query of the QCBatch ") + " " + qcbatchid);
                        this.setError(tp.translate("Information Error"), "INFORMATION", tp.translate("QCBatch evaluation failed to execute. Error in returning QCBatches by selection query of the QCBatch") + " " + qcbatchid);
                        if (success == null || !success.equals("Y")) {
                            return this.setError(tp.translate("QCBatch evaluation could not execute."));
                        }
                    }
                } else {
                    return this.setError(tp.translate("No Datasets found."));
                }
            }
            int instance = QCBatchPool.releaseQCBatch(qcBatch);
            this.logger.info("[EvaluateQCBatch] Released " + qcbatchid + " - " + instance);
        }
        return this.rc;
    }

    public void updateQCBatchEvaluationDisposition(String qcBatchId, String currentBatchEvalStatus, String currentBatchSpecCondition, QCBatch qcBatch) {
        boolean needToUpdateEvalDisposition = true;
        boolean needToUpdateSpecCondition = true;
        String qcEvalDisposition = qcBatch.getEvaluationDisposition();
        String qcSpecCondition = qcBatch.getSpecCondition();
        String qcBatchStatus = qcBatch.getStatus();
        if (!"Reviewed".equalsIgnoreCase(qcBatchStatus)) {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "QCBatch");
            actionProps.put("keyid1", qcBatchId);
            actionProps.put("qcbatchstatus", "Evaluated");
            if (currentBatchEvalStatus == null || currentBatchEvalStatus.trim().equals("")) {
                needToUpdateEvalDisposition = false;
            } else if (qcEvalDisposition != null && qcEvalDisposition.equalsIgnoreCase(currentBatchEvalStatus)) {
                needToUpdateEvalDisposition = false;
            } else {
                actionProps.put("evaluationdisposition", currentBatchEvalStatus);
            }
            if (currentBatchSpecCondition == null || currentBatchSpecCondition.trim().equals("")) {
                needToUpdateSpecCondition = false;
            } else if (qcSpecCondition != null && qcSpecCondition.equalsIgnoreCase(currentBatchSpecCondition)) {
                needToUpdateSpecCondition = false;
            } else {
                actionProps.put("speccondition", currentBatchSpecCondition);
            }
            try {
                if (!"Evaluated".equalsIgnoreCase(qcBatchStatus) || needToUpdateEvalDisposition || needToUpdateSpecCondition) {
                    this.getActionProcessor().processAction("EditSDI", "1", actionProps);
                }
            }
            catch (ActionException e) {
                this.logger.error("Error running EditSDI", e);
            }
        }
    }

    public void updateQCBatchReviewDisposition(String qcBatchId, String evalStatus, QCBatch qcBatch) {
        String passCondition = "Pass";
        String failCondition = "Fail";
        if (evalStatus.equals(passCondition)) {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "QCBatch");
            actionProps.put("keyid1", qcBatchId);
            try {
                actionProps.put("reviewdisposition", "Approved");
                actionProps.put("qcbatchstatus", "Reviewed");
                this.getActionProcessor().processAction("EditSDI", "1", actionProps);
            }
            catch (ActionException e) {
                this.logger.error("Error running EditSDI", e);
            }
        } else if (evalStatus.equals(failCondition)) {
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("sdcid", "QCBatch");
            actionProps.put("keyid1", qcBatchId);
            actionProps.put("reviewdisposition", null);
            actionProps.put("qcbatchstatus", "Evaluated");
            try {
                this.getActionProcessor().processAction("EditSDI", "1", actionProps);
            }
            catch (ActionException e) {
                this.logger.error("Error running EditSDI", e);
            }
        }
    }

    public void PassFailureAction(String qcBatchId, String RuleEvalStatus, String SpecCondition, String actionName) {
        HashMap<String, String> actionProps = new HashMap<String, String>();
        try {
            actionProps.put("qcbatchid", qcBatchId);
            actionProps.put("ruleevaluationdisposition", RuleEvalStatus);
            actionProps.put("speccondition", SpecCondition);
            this.getActionProcessor().processAction(actionName, "1", actionProps);
        }
        catch (ActionException e) {
            this.logger.error("Error running " + actionName, e);
        }
    }

    public void setEvaluationStatus(String qcbatchid, QueryProcessor qp, String successAction, String failureAction, boolean updateStatusFlag, boolean evalQCBAttributes) {
        StringBuffer errMesg;
        int count;
        DataSet dsSpec;
        DataSet dsAttributeSpec;
        int count2;
        String currentBatchEvalStatus = "";
        String currentBatchSpecCondition = "";
        QCBatch qcBatch = null;
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = qp.getPreparedSqlDataSet(Aqc.getQCBatchSampleTypeFailCount(qcbatchid, "Fail", safeSQL), safeSQL.getValues());
        TranslationProcessor tp = this.getTranslationProcessor();
        if (ds != null && (count2 = ds.getInt(0, "COUNT")) > 0) {
            currentBatchEvalStatus = "Fail";
        }
        if (currentBatchEvalStatus.length() == 0) {
            safeSQL.reset();
            ds = qp.getPreparedSqlDataSet(Aqc.getQCBatchSampleTypeWarningCount(qcbatchid, "Warning", safeSQL), safeSQL.getValues());
            if (ds != null && (count2 = ds.getInt(0, "COUNT")) > 0) {
                currentBatchEvalStatus = "Warning";
            }
        }
        if (currentBatchEvalStatus.length() == 0) {
            safeSQL.reset();
            ds = qp.getPreparedSqlDataSet(Aqc.getQCBatchSampleTypePassCount(qcbatchid, "Pass", safeSQL), safeSQL.getValues());
            if (ds != null && (count2 = ds.getInt(0, "COUNT")) > 0) {
                currentBatchEvalStatus = "Pass";
            }
        }
        if ((dsAttributeSpec = qp.getPreparedSqlDataSet("attributespeccondition", "SELECT condition FROM sdispec  WHERE sdcid = 'QCBatch' AND keyid1 = ?", new String[]{qcbatchid})) != null && dsAttributeSpec.getRowCount() > 0) {
            currentBatchSpecCondition = dsAttributeSpec.getValue(0, "condition", "");
        }
        if (!currentBatchSpecCondition.equals(this.__SpecFailValue)) {
            safeSQL.reset();
            dsSpec = qp.getPreparedSqlDataSet(Aqc.getQCBatchSampleTypeSpecFailCount(qcbatchid, this.__SpecFailValue, safeSQL), safeSQL.getValues());
            if (dsSpec != null && (count = dsSpec.getInt(0, "COUNT")) > 0) {
                currentBatchSpecCondition = this.__SpecFailValue;
            }
        }
        if (!currentBatchSpecCondition.equals(this.__SpecFailValue) && !currentBatchSpecCondition.equals(this.__SpecWarningValue)) {
            safeSQL.reset();
            dsSpec = qp.getPreparedSqlDataSet(Aqc.getQCBatchSampleTypeSpecWarningCount(qcbatchid, this.__SpecWarningValue, safeSQL), safeSQL.getValues());
            if (dsSpec != null && (count = dsSpec.getInt(0, "COUNT")) > 0) {
                currentBatchSpecCondition = this.__SpecWarningValue;
            }
        }
        if (!(currentBatchSpecCondition.equals(this.__SpecFailValue) || currentBatchSpecCondition.equals(this.__SpecWarningValue) || currentBatchSpecCondition.equals(this.__SpecPassValue))) {
            safeSQL.reset();
            dsSpec = qp.getPreparedSqlDataSet(Aqc.getQCBatchSampleTypeSpecPassCount(qcbatchid, this.__SpecPassValue), safeSQL.getValues());
            if (dsSpec != null && (count = dsSpec.getInt(0, "COUNT")) > 0) {
                currentBatchSpecCondition = this.__SpecPassValue;
            }
        }
        try {
            qcBatch = new QCBatch(qp, qcbatchid);
        }
        catch (SapphireException e) {
            this.logger.error(e.getMessage(), e);
        }
        String overallBatchStatus = "";
        if (currentBatchEvalStatus.length() > 0 || currentBatchSpecCondition.length() > 0) {
            if (currentBatchEvalStatus.equals("Fail") || currentBatchSpecCondition.equals(this.__SpecFailValue)) {
                overallBatchStatus = "Fail";
            } else if (currentBatchEvalStatus.equals("Warning") || currentBatchSpecCondition.equals(this.__SpecWarningValue)) {
                overallBatchStatus = "Warning";
            } else if (currentBatchEvalStatus.equals("Pass") || currentBatchSpecCondition.equals(this.__SpecPassValue)) {
                overallBatchStatus = "Pass";
            }
            this.updateQCBatchEvaluationDisposition(qcbatchid, currentBatchEvalStatus, currentBatchSpecCondition, qcBatch);
            String qcReviewDisposition = qcBatch.getReviewDisposition();
            String reviewOnPassFlag = qcBatch.getReviewOnPassFlag();
            if (overallBatchStatus.equals("Pass")) {
                if (reviewOnPassFlag != null && reviewOnPassFlag.equals("Y") && qcBatch.isDataEntered(true) && (qcReviewDisposition == null || !qcReviewDisposition.equalsIgnoreCase("Approved"))) {
                    this.updateQCBatchReviewDisposition(qcbatchid, "Pass", qcBatch);
                }
                if (successAction != null && successAction.length() > 0) {
                    this.PassFailureAction(qcbatchid, currentBatchEvalStatus, currentBatchSpecCondition, successAction);
                }
            } else if (overallBatchStatus.equals("Fail")) {
                if (reviewOnPassFlag != null && reviewOnPassFlag.equals("Y") && qcReviewDisposition != null && qcReviewDisposition.equalsIgnoreCase("Approved")) {
                    this.updateQCBatchReviewDisposition(qcbatchid, "Fail", qcBatch);
                }
                if (failureAction != null && failureAction.length() > 0) {
                    this.PassFailureAction(qcbatchid, currentBatchEvalStatus, currentBatchSpecCondition, failureAction);
                }
            } else if (overallBatchStatus.equals("Warning")) {
                // empty if block
            }
            Logger.logInfo("QC Batch " + qcbatchid + " is evaluated successfully.");
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("qcbatchid", qcbatchid);
            this.setError(tp.translate("Success Message"), "INFORMATION", tp.translate("QC Batch [qcbatchid] is evaluated successfully.", token));
        } else if (!evalQCBAttributes) {
            errMesg = new StringBuffer();
            errMesg.append(tp.translate("QCBatch evaluation could not execute for any one of the following reasons") + ":\n" + tp.translate("QC Evaluation Rule does not have any QC Rule Item, QC sample paramtype is not matching with the specified 'Evaluate Param Type'"));
            this.logger.debug(errMesg.toString());
            this.setError(tp.translate("Information Error"), "INFORMATION", errMesg.toString());
        } else {
            errMesg = new StringBuffer();
            errMesg.append(tp.translate("Spec evaluation result could not be obtained for the QCBatch Params."));
            this.logger.debug(errMesg.toString());
        }
        if (updateStatusFlag) {
            try {
                HashMap<String, String> actionProps = new HashMap<String, String>();
                actionProps.put("qcbatchid", qcbatchid);
                this.getActionProcessor().processAction("UpdateQCBatchStatus", "1", actionProps);
            }
            catch (ActionException e) {
                this.logger.error("Error running UpdateQCBatchStatus", e);
            }
        }
    }
}

