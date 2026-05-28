/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.QCBatchReagentSync;
import com.labvantage.opal.actions.qcactions.QCBaseAction;
import com.labvantage.opal.actions.qcactions.QCEvalAction;
import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.qcbatch.QCPositioning;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.sql.common.Aqc;
import com.labvantage.opal.util.DataItem;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.opal.util.SDIDataSet;
import com.labvantage.opal.util.SDIWorkItem;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.StringHolder;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCBatchActions
extends QCBaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 76596 $";
    public static final String SAMPLETYPE_UNKNOWN = "Unknown";
    private SQLGenerator __SqlGenerator;
    int rc = 1;
    boolean debug = true;

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.getConnectionProcessor().isOra());
        if (actionid.equals("ApplyQCMethod")) {
            this.rc = this.doApplyQCMethod(props);
        } else if (actionid.equals("ProcessQCBatch")) {
            this.rc = this.doProcessQCBatch(props);
        } else if (actionid.equals("RetestQCBatch")) {
            this.rc = this.doRetestQCBatch(props);
        } else if (actionid.equals("RunQCValidation")) {
            this.rc = this.doRunQCValidation(props);
        } else if (actionid.equals("UpdateQCBatchStatus")) {
            this.rc = this.doUpdateQCBatchStatus(props);
        } else if (actionid.equals("ReapplyQCPosition")) {
            this.rc = this.doReapplyQCBatchPositioning(props);
        } else if (actionid.equals("QCUpdateLimit")) {
            this.rc = this.doQCUpdateLimit(props);
        }
        return this.rc;
    }

    private int doApplyQCMethod(HashMap props) {
        String QCSAMPLEIDENTIFIER = "(Auto)";
        boolean guiMode = true;
        boolean insertMode = true;
        String finalSampleList = (String)props.get("allsamples");
        String sdidataids = (String)props.get("sdidataids");
        String sdiworkitemids = (String)props.get("sdiworkitemids");
        if (sdiworkitemids == null) {
            sdiworkitemids = "";
        }
        if (sdidataids == null) {
            sdidataids = "";
        }
        String columns = (String)props.get("columns");
        String newQCBatchId = (String)props.get("qcbatchid");
        String qcNotes = (String)props.get("qcbatchnotes");
        StringHolder rsetIdHolder = new StringHolder();
        HashMap<String, String> qcBatchDetails = new HashMap<String, String>();
        HashMap<String, String> qcBatchSampleTypeDetails = new HashMap<String, String>();
        HashMap<String, String> qcTypeMethSampleTypeIdMapping = new HashMap<String, String>();
        HashMap<String, String[]> sampleTypeIdMappings = new HashMap<String, String[]>();
        String qcMethodId = (String)props.get("qcmethodid");
        String qcMethodVersionId = (String)props.get("qcmethodversionid");
        boolean createWorksheet = false;
        boolean createLESWorksheet = false;
        String batchSdcId = "";
        boolean copySecurityDeptToSample = false;
        String qcBatchSecurityDept = "";
        ArrayList<String> sdiwiList = new ArrayList<String>();
        if (sdiworkitemids.length() > 0) {
            String[] sdiwi_array = StringUtil.split(sdiworkitemids, ";");
            for (int i = 0; i < sdiwi_array.length; ++i) {
                sdiwiList.add(sdiwi_array[i]);
            }
        }
        ArrayList<String> sdidataList = new ArrayList<String>();
        if (sdidataids.length() > 0) {
            String[] sdidata_array = StringUtil.split(sdidataids, ";");
            for (int i = 0; i < sdidata_array.length; ++i) {
                sdidataList.add(sdidata_array[i]);
            }
        }
        ActionProcessor aps = this.getActionProcessor();
        TranslationProcessor tp = this.getTranslationProcessor();
        if (newQCBatchId == null || newQCBatchId.equals("")) {
            this.rc = this.setError(tp.translate("The mandatory input - QCBatch ID was not available. Cannot proceed further."));
            return this.rc;
        }
        String formId = (String)props.get("formid");
        String formVersionId = (String)props.get("formversionid");
        DAMProcessor damProcessor = this.getDAMProcessor();
        SafeSQL safeSQL = new SafeSQL();
        if (damProcessor.createLockedRSet("QCBatch", newQCBatchId, "(null)", "(null)", rsetIdHolder) == 1) {
            QueryProcessor qp = this.getQueryProcessor();
            SDI batchSDI = new SDI("QCBatch", newQCBatchId, null, null);
            DataSet dsQcMethod = qp.getPreparedSqlDataSet(Aqc.getQCMethodIDAndQCBatchSDCID(batchSDI, safeSQL), safeSQL.getValues());
            String qcbatchSiteId = "";
            if (dsQcMethod != null && dsQcMethod.size() > 0) {
                qcMethodId = dsQcMethod.getValue(0, "qcmethodid");
                qcMethodVersionId = dsQcMethod.getValue(0, "qcmethodversionid");
                batchSdcId = dsQcMethod.getValue(0, "qcbatchsdcid");
                qcBatchSecurityDept = dsQcMethod.getValue(0, "securitydepartment");
                qcbatchSiteId = dsQcMethod.getValue(0, "sitedepartmentid");
            }
            if (finalSampleList == null || finalSampleList.equals("")) {
                guiMode = false;
                safeSQL.reset();
                DataSet dsInsertModeCheck = qp.getPreparedSqlDataSet(Aqc.getQCActionApply(batchSDI, safeSQL), safeSQL.getValues());
                if (dsInsertModeCheck != null && dsInsertModeCheck.size() > 0) {
                    insertMode = false;
                    int dsInsertModeCheckNumRows = dsInsertModeCheck.getRowCount();
                    for (int dsInsertModeCheckCount = 0; dsInsertModeCheckCount < dsInsertModeCheckNumRows; ++dsInsertModeCheckCount) {
                        String tempS_qcBatchSampleTypeId = dsInsertModeCheck.getValue(dsInsertModeCheckCount, "s_qcbatchsampletypeid");
                        String[] temp = new String[]{tempS_qcBatchSampleTypeId, dsInsertModeCheck.getValue(dsInsertModeCheckCount, "actionapply"), dsInsertModeCheck.getValue(dsInsertModeCheckCount, "workitemid"), dsInsertModeCheck.getValue(dsInsertModeCheckCount, "qctemplatekeyid1")};
                        sampleTypeIdMappings.put(tempS_qcBatchSampleTypeId, temp);
                    }
                }
            }
            try {
                String paramListType = "";
                boolean matchWorkItemItem = QCUtil.matchSDIWorkItemItem(this.getConfigurationProcessor());
                if (!matchWorkItemItem && sdiworkitemids.length() > 0) {
                    throw new SapphireException(tp.translate("To create QCBatch with selected SDIWorkItem instances, please set Honor SDIWorkItemItem as 'Yes' in AQC policy."));
                }
                String userSiteId = OpalUtil.getSiteIdFromUserDefaultDepartment(this.connectionInfo, this.database);
                if (insertMode) {
                    qcMethodId = (String)props.get("qcmethodid");
                    qcMethodVersionId = (String)props.get("qcmethodversionid");
                    SDI methodSDI = new SDI("QCMethod", qcMethodId, qcMethodVersionId, null);
                    safeSQL.reset();
                    DataSet dsQCMethod = qp.getPreparedSqlDataSet(Aqc.getQCMethodDetails(methodSDI, safeSQL), safeSQL.getValues());
                    if (dsQCMethod == null || dsQCMethod.size() == 0) {
                        if (rsetIdHolder.value != null && !rsetIdHolder.value.equals("")) {
                            damProcessor.clearRSet(rsetIdHolder.value);
                        }
                        this.rc = this.setError(tp.translate("The specified QC Method not found. Cannot proceed further."));
                        return this.rc;
                    }
                    String methodSdcId = dsQCMethod.getValue(0, "methodsdcid");
                    String methodQueryId = dsQCMethod.getValue(0, "methodqueryid");
                    String methodQueryBasedOnId = dsQCMethod.getValue(0, "methodquerybasedonid");
                    String methodQueryCount = dsQCMethod.getValue(0, "methodquerycount");
                    String evalOption = dsQCMethod.getValue(0, "evaloption");
                    String actionSuccess = dsQCMethod.getValue(0, "actionsuccess");
                    String actionFailure = dsQCMethod.getValue(0, "actionfailure");
                    String maxbatchsize = dsQCMethod.getValue(0, "maxbatchsize");
                    String reviewOnPassFlag = dsQCMethod.getValue(0, "reviewonpassflag");
                    String approveOnReviewFlag = dsQCMethod.getValue(0, "approveonreviewflag");
                    paramListType = dsQCMethod.getValue(0, "paramlisttype");
                    String batchType = dsQCMethod.getValue(0, "batchtype");
                    String createWSRule = dsQCMethod.getValue(0, "createworksheetrule");
                    String createLesRule = dsQCMethod.getValue(0, "createlesrule");
                    String paramListId = dsQCMethod.getValue(0, "paramlistid", "");
                    String paramListVersionId = dsQCMethod.getValue(0, "paramlistversionid", "");
                    String variantId = dsQCMethod.getValue(0, "variantid", "");
                    String specId = dsQCMethod.getValue(0, "specid", "");
                    String specVersionIdAttr = dsQCMethod.getValue(0, "specversionid", "");
                    if (createWSRule != null && "On Creation".equalsIgnoreCase(createWSRule)) {
                        createWorksheet = true;
                    }
                    if (createLesRule != null && "On Creation".equalsIgnoreCase(createLesRule)) {
                        createLESWorksheet = true;
                    }
                    qcBatchDetails.put("sdcid", "QCBatch");
                    qcBatchDetails.put("keyid1", newQCBatchId);
                    qcBatchDetails.put("qcmethodid", qcMethodId);
                    qcBatchDetails.put("qcmethodversionid", qcMethodVersionId);
                    qcBatchDetails.put("qcbatchsdcid", methodSdcId);
                    qcBatchDetails.put("qcbatchqueryid", methodQueryId);
                    qcBatchDetails.put("qcbatchbasedonid", methodQueryBasedOnId);
                    qcBatchDetails.put("qcbatchquerycount", methodQueryCount);
                    qcBatchDetails.put("evaloption", evalOption);
                    qcBatchDetails.put("actionsuccess", actionSuccess);
                    qcBatchDetails.put("actionfailure", actionFailure);
                    qcBatchDetails.put("sourcesdcid", "QCMethod");
                    qcBatchDetails.put("sourcekeyid1", qcMethodId);
                    qcBatchDetails.put("reviewonpassflag", reviewOnPassFlag);
                    qcBatchDetails.put("approveonreviewflag", approveOnReviewFlag);
                    qcBatchDetails.put("paramlisttype", paramListType);
                    qcBatchDetails.put("qcbatchtype", batchType);
                    qcBatchDetails.put("createworksheetrule", createWSRule);
                    qcBatchDetails.put("instrumenttypeid", dsQCMethod.getValue(0, "instrumenttypeid", ""));
                    qcBatchDetails.put("instrumentmodelid", dsQCMethod.getValue(0, "instrumentmodelid", ""));
                    SDCProcessor sdcp = this.getSDCProcessor();
                    batchSdcId = methodSdcId;
                    ConfigurationProcessor cp = this.getConfigurationProcessor();
                    if (qcBatchSecurityDept.length() > 0 && sdcp.getProperty("QCBatch", "accesscontrolledflag").equals("D") && sdcp.getProperty(batchSdcId, "accesscontrolledflag").equals("D")) {
                        copySecurityDeptToSample = QCUtil.copySecurityDeptToSample(cp);
                    }
                    ActionProcessor ap = this.getActionProcessor();
                    try {
                        ap.processAction("EditSDI", "1", qcBatchDetails);
                    }
                    catch (ActionException e) {
                        if (rsetIdHolder.value != null && !rsetIdHolder.value.equals("")) {
                            damProcessor.clearRSet(rsetIdHolder.value);
                        }
                        this.rc = this.setError(e.getMessage(), e);
                        return this.rc;
                    }
                    safeSQL.reset();
                    DataSet sdiFormRules = qp.getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3, formid, forminstance, formversionid, formrule, mandatoryflag, usersequence FROM sdiformrule WHERE sdcid ='QCMethod' AND keyid1 = " + safeSQL.addVar(qcMethodId) + " AND keyid2 = " + safeSQL.addVar(qcMethodVersionId), safeSQL.getValues());
                    if (createWorksheet && (formId == null || formId.length() == 0)) {
                        int defaultRow;
                        if (sdiFormRules != null && sdiFormRules.getRowCount() > 0 && (defaultRow = sdiFormRules.findRow("formrule", "default")) > -1) {
                            formId = sdiFormRules.getString(defaultRow, "formid");
                            formVersionId = sdiFormRules.getString(defaultRow, "formversionid");
                        }
                        if (formId == null || formId.length() == 0) {
                            throw new SapphireException("GENERAL_ERROR", " " + tp.translate("No default/supplied Form found for worksheet creation."));
                        }
                    }
                    if (sdiFormRules != null && sdiFormRules.getRowCount() > 0) {
                        this.copyFormRuleToBatch(newQCBatchId, sdiFormRules);
                    }
                    safeSQL.reset();
                    DataSet dsQCMethodSampleType = qp.getPreparedSqlDataSet(Aqc.getQCMethodSampleTypeDetails(methodSDI, safeSQL), safeSQL.getValues());
                    boolean attemptToCopyMethodSampleTypes = true;
                    if (dsQCMethodSampleType == null || dsQCMethodSampleType.size() == 0) {
                        attemptToCopyMethodSampleTypes = false;
                        this.logger.warn("No QCMethod Sample Types found for the specified QC Method.");
                    }
                    if (attemptToCopyMethodSampleTypes) {
                        String[] newQCBatchSampleTypeId = new String[dsQCMethodSampleType.size()];
                        StringBuffer tempQcMethodSampleTypeId = new StringBuffer();
                        StringBuffer tempQcMethodSampleTypeDesc = new StringBuffer();
                        StringBuffer tempQcEvalRuleId = new StringBuffer();
                        StringBuffer tempQcSampleType = new StringBuffer();
                        StringBuffer tempWorkItemId = new StringBuffer();
                        StringBuffer tempWorkItemVersionId = new StringBuffer();
                        StringBuffer tempActionApply = new StringBuffer();
                        StringBuffer tempActionCalc = new StringBuffer();
                        StringBuffer tempActionEval = new StringBuffer();
                        StringBuffer tempPositionType = new StringBuffer();
                        StringBuffer tempPositionStart = new StringBuffer();
                        StringBuffer tempPositionEnd = new StringBuffer();
                        StringBuffer tempPositionEvery = new StringBuffer();
                        StringBuffer tempPositionCount = new StringBuffer();
                        StringBuffer tempLinkedTo = new StringBuffer();
                        StringBuffer tempTemplateSdcId = new StringBuffer();
                        StringBuffer tempTemplateKeyId1 = new StringBuffer();
                        StringBuffer tempTemplateKeyId2 = new StringBuffer();
                        StringBuffer tempTemplateKeyId3 = new StringBuffer();
                        StringBuffer tempQcBatchId = new StringBuffer();
                        StringBuffer tempKeyId2 = new StringBuffer();
                        StringBuffer tempkeyid3 = new StringBuffer();
                        StringBuffer tempEvaluateParamType = new StringBuffer();
                        StringBuffer tempSpecId = new StringBuffer();
                        StringBuffer tempSpecVersionId = new StringBuffer();
                        StringBuffer temUserSequence = new StringBuffer();
                        StringBuffer tempStandardLevel = new StringBuffer();
                        for (int dsQCMSTrow = 0; dsQCMSTrow < dsQCMethodSampleType.size(); ++dsQCMSTrow) {
                            tempQcBatchId.append(newQCBatchId).append(";");
                            tempKeyId2.append("(null)").append(";");
                            tempkeyid3.append("(null)").append(";");
                            tempQcMethodSampleTypeId.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "s_qcmethodsampletypeid")).append(";");
                            tempQcMethodSampleTypeDesc.append(StringUtil.replaceAll(dsQCMethodSampleType.getValue(dsQCMSTrow, "qcmethoditemsampledesc"), ";", "#semicolon#")).append(";");
                            tempQcEvalRuleId.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "qcevalruleid")).append(";");
                            tempQcSampleType.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "qcsampletype")).append(";");
                            String workItemId = dsQCMethodSampleType.getValue(dsQCMSTrow, "workitemid");
                            tempWorkItemId.append(workItemId).append(";");
                            String workItemVersionId = dsQCMethodSampleType.getValue(dsQCMSTrow, "workitemversionid");
                            if (workItemId.length() > 0 && workItemVersionId.length() == 0) {
                                workItemVersionId = SdiInfo.getCurrentVersion("WorkItem", workItemId, null, new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                            }
                            tempWorkItemVersionId.append(workItemVersionId).append(";");
                            tempActionApply.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "actionapply")).append(";");
                            tempActionCalc.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "actioncalc")).append(";");
                            tempActionEval.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "actioneval")).append(";");
                            tempPositionType.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "positiontype")).append(";");
                            tempPositionStart.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "positionstart")).append(";");
                            tempPositionEnd.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "positionend")).append(";");
                            tempPositionEvery.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "positionevery")).append(";");
                            tempPositionCount.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "positioncount")).append(";");
                            tempLinkedTo.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "linkedto")).append(";");
                            tempTemplateSdcId.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "qctemplatesdcid")).append(";");
                            tempTemplateKeyId1.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "qctemplatekeyid1")).append(";");
                            tempTemplateKeyId2.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "qctemplatekeyid2")).append(";");
                            tempTemplateKeyId3.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "qctemplatekeyid3")).append(";");
                            tempEvaluateParamType.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "evaluateparamtype")).append(";");
                            tempSpecId.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "specid", "")).append(";");
                            tempSpecVersionId.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "specversionid", "")).append(";");
                            qcTypeMethSampleTypeIdMapping.put(dsQCMethodSampleType.getValue(dsQCMSTrow, "qcsampletype"), dsQCMethodSampleType.getValue(dsQCMSTrow, "s_qcmethodsampletypeid"));
                            temUserSequence.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "usersequence", "")).append(";");
                            tempStandardLevel.append(dsQCMethodSampleType.getValue(dsQCMSTrow, "standardlevel", "")).append(";");
                        }
                        String qcBatchIds = tempQcBatchId.substring(0, tempQcBatchId.length() - 1);
                        String keyId2s = tempKeyId2.substring(0, tempKeyId2.length() - 1);
                        String keyId3s = tempkeyid3.substring(0, tempkeyid3.length() - 1);
                        String qcMethodSampleTypeIds = tempQcMethodSampleTypeId.substring(0, tempQcMethodSampleTypeId.length() - 1);
                        String qcMethodSampleTypeDescs = tempQcMethodSampleTypeDesc.substring(0, tempQcMethodSampleTypeDesc.length() - 1);
                        String qcEvalRuleIds = tempQcEvalRuleId.substring(0, tempQcEvalRuleId.length() - 1);
                        String qcSampleTypes = tempQcSampleType.substring(0, tempQcSampleType.length() - 1);
                        String workItemIds = tempWorkItemId.substring(0, tempWorkItemId.length() - 1);
                        String workItemVersionIds = tempWorkItemVersionId.substring(0, tempWorkItemVersionId.length() - 1);
                        String actionApplys = tempActionApply.substring(0, tempActionApply.length() - 1);
                        String actionCalcs = tempActionCalc.substring(0, tempActionCalc.length() - 1);
                        String actionEvals = tempActionEval.substring(0, tempActionEval.length() - 1);
                        String positionTypes = tempPositionType.substring(0, tempPositionType.length() - 1);
                        String positionStarts = tempPositionStart.substring(0, tempPositionStart.length() - 1);
                        String positionEnds = tempPositionEnd.substring(0, tempPositionEnd.length() - 1);
                        String positionEverys = tempPositionEvery.substring(0, tempPositionEvery.length() - 1);
                        String positionCounts = tempPositionCount.substring(0, tempPositionCount.length() - 1);
                        String linkedTos = tempLinkedTo.substring(0, tempLinkedTo.length() - 1);
                        String templateSdcIds = tempTemplateSdcId.substring(0, tempTemplateSdcId.length() - 1);
                        String templateKeyId1s = tempTemplateKeyId1.substring(0, tempTemplateKeyId1.length() - 1);
                        String templateKeyId2s = tempTemplateKeyId2.substring(0, tempTemplateKeyId2.length() - 1);
                        String templateKeyId3s = tempTemplateKeyId3.substring(0, tempTemplateKeyId3.length() - 1);
                        String evaluateParamType = tempEvaluateParamType.substring(0, tempEvaluateParamType.length() - 1);
                        String specIds = tempSpecId.substring(0, tempSpecId.length() - 1);
                        String specVersionIds = tempSpecVersionId.substring(0, tempSpecVersionId.length() - 1);
                        String bstUsersequence = temUserSequence.substring(0, temUserSequence.length() - 1);
                        String standardLevels = tempStandardLevel.substring(0, tempStandardLevel.length() - 1);
                        qcBatchSampleTypeDetails.put("sdcid", "QCBatchSampleType");
                        qcBatchSampleTypeDetails.put("qcbatchid", qcBatchIds);
                        qcBatchSampleTypeDetails.put("keyid2", keyId2s);
                        qcBatchSampleTypeDetails.put("keyid3", keyId3s);
                        qcBatchSampleTypeDetails.put("qcbatchsampletypedesc", qcMethodSampleTypeDescs);
                        qcBatchSampleTypeDetails.put("qcsampletype", qcSampleTypes);
                        qcBatchSampleTypeDetails.put("workitemid", workItemIds);
                        qcBatchSampleTypeDetails.put("workitemversionid", workItemVersionIds);
                        qcBatchSampleTypeDetails.put("actionapply", actionApplys);
                        qcBatchSampleTypeDetails.put("actioncalc", actionCalcs);
                        qcBatchSampleTypeDetails.put("actioneval", actionEvals);
                        qcBatchSampleTypeDetails.put("positiontype", positionTypes);
                        qcBatchSampleTypeDetails.put("positionstart", positionStarts);
                        qcBatchSampleTypeDetails.put("positionend", positionEnds);
                        qcBatchSampleTypeDetails.put("positionevery", positionEverys);
                        qcBatchSampleTypeDetails.put("positioncount", positionCounts);
                        qcBatchSampleTypeDetails.put("linkedto", linkedTos);
                        qcBatchSampleTypeDetails.put("qctemplatesdcid", templateSdcIds);
                        qcBatchSampleTypeDetails.put("qctemplatekeyid1", templateKeyId1s);
                        qcBatchSampleTypeDetails.put("qctemplatekeyid2", templateKeyId2s);
                        qcBatchSampleTypeDetails.put("qctemplatekeyid3", templateKeyId3s);
                        qcBatchSampleTypeDetails.put("sourcesdcid", "QCMethodSampleType");
                        qcBatchSampleTypeDetails.put("evaluateparamtype", evaluateParamType);
                        qcBatchSampleTypeDetails.put("specid", specIds);
                        qcBatchSampleTypeDetails.put("specversionid", specVersionIds);
                        qcBatchSampleTypeDetails.put("qcmethodsampletypeid", qcMethodSampleTypeIds);
                        qcBatchSampleTypeDetails.put("sourcekeyid1", qcMethodSampleTypeIds);
                        qcBatchSampleTypeDetails.put("copydataset", "N");
                        qcBatchSampleTypeDetails.put("copies", String.valueOf(dsQCMethodSampleType.size()));
                        qcBatchSampleTypeDetails.put("usersequence", bstUsersequence);
                        qcBatchSampleTypeDetails.put("standardlevel", standardLevels);
                        ActionProcessor apQCB = this.getActionProcessor();
                        try {
                            apQCB.processAction("CopySDIDetail", "1", qcBatchSampleTypeDetails);
                        }
                        catch (ActionException e) {
                            if (rsetIdHolder.value != null && !rsetIdHolder.value.equals("")) {
                                damProcessor.clearRSet(rsetIdHolder.value);
                            }
                            this.rc = this.setError(e.getMessage(), e);
                            return this.rc;
                        }
                        String newkeyidsQCBatchSampleType = (String)qcBatchSampleTypeDetails.get("newkeyid1");
                        if (newkeyidsQCBatchSampleType != null && newkeyidsQCBatchSampleType.length() > 0) {
                            HashMap<String, String> copySDIDataSetProps = new HashMap<String, String>();
                            try {
                                copySDIDataSetProps.put("sdcid", "QCBatchSampleType");
                                copySDIDataSetProps.put("keyid1", newkeyidsQCBatchSampleType);
                                copySDIDataSetProps.put("sourcesdcid", "QCMethodSampleType");
                                copySDIDataSetProps.put("sourcekeyid1", qcMethodSampleTypeIds);
                                aps.processAction("CopySDIDataSet", "1", copySDIDataSetProps);
                            }
                            catch (ActionException e) {
                                this.rc = this.setError(e.getMessage(), e);
                                return this.rc;
                            }
                        }
                        String[] associatedActionApply = StringUtil.split(actionApplys, ";");
                        String[] qcEvalRuleId = StringUtil.split(qcEvalRuleIds, ";");
                        String[] workItemId = StringUtil.split(workItemIds, ";");
                        String[] workItemVersionId = StringUtil.split(workItemVersionIds, ";");
                        String[] qcMethodSampleTypeId = StringUtil.split(qcMethodSampleTypeIds, ";");
                        String[] qcSampleTemplates = StringUtil.split(templateKeyId1s, ";");
                        newQCBatchSampleTypeId = StringUtil.split(newkeyidsQCBatchSampleType, ";");
                        sampleTypeIdMappings = new HashMap();
                        StringBuffer sql = new StringBuffer();
                        sql.append("select a.violationcount, a.windowsize, a.sigmaabovecl, a.sigmabelowcl,");
                        sql.append(" a.rulepatternflag , a.insidelimitflag, a.warningflag ");
                        sql.append(" from s_qcevalruleitem a, s_qcevalrule b, s_qcmethodsampletype c ");
                        sql.append(" where a.s_qcevalruleid = b.s_qcevalruleid ");
                        sql.append(" and  b.s_qcevalruleid = c.qcevalruleid ");
                        sql.append(" and c.qcmethodid = ? and c.qcmethodversionid = ? and c.s_qcmethodsampletypeid = ? and enabledflag = 'Y' ");
                        sql.append(" order by a.S_QCEVALRULEITEMID ");
                        PreparedStatement psmtGetMSTRuleDetails = this.database.prepareStatement("GetMSTRuleDetails", sql.toString());
                        psmtGetMSTRuleDetails.setString(1, qcMethodId);
                        psmtGetMSTRuleDetails.setString(2, qcMethodVersionId);
                        sql.setLength(0);
                        sql.append(" select paramid, targetvalue, targetunits, sd ");
                        sql.append(" from s_qcmethodsampletypelimit ");
                        sql.append(" where s_qcmethodsampletypeid = ? ");
                        sql.append(" order by S_QCMETHODSAMPLETYPELIMITID ");
                        PreparedStatement psmtGetMSTLimitDetails = this.database.prepareStatement("GetMSTLimitDetails", sql.toString());
                        for (int dsQCMSTrow = 0; dsQCMSTrow < dsQCMethodSampleType.size(); ++dsQCMSTrow) {
                            String[] temp = new String[]{newQCBatchSampleTypeId[dsQCMSTrow], associatedActionApply[dsQCMSTrow], workItemId[dsQCMSTrow], qcSampleTemplates[dsQCMSTrow]};
                            sampleTypeIdMappings.put(qcMethodSampleTypeId[dsQCMSTrow], temp);
                            if (qcEvalRuleId[dsQCMSTrow] == null || qcEvalRuleId[dsQCMSTrow].equals("(null)") && qcEvalRuleId[dsQCMSTrow].trim().length() > 0) continue;
                            psmtGetMSTRuleDetails.setString(3, qcMethodSampleTypeId[dsQCMSTrow]);
                            DataSet dsEvalRuleItem = new DataSet(psmtGetMSTRuleDetails.executeQuery());
                            boolean attemptToCopyEvalRule = true;
                            if (dsEvalRuleItem == null || dsEvalRuleItem.size() == 0) {
                                attemptToCopyEvalRule = false;
                                this.logger.warn("No evaluation rule item found for the specified QC Method.  ");
                            }
                            String evalRuleSequences = new String();
                            if (attemptToCopyEvalRule) {
                                StringBuffer tempViolationCount = new StringBuffer();
                                StringBuffer tempWindowSize = new StringBuffer();
                                StringBuffer tempSigmaAboveCl = new StringBuffer();
                                StringBuffer tempSigmaBelowCl = new StringBuffer();
                                StringBuffer tempRulePatternFlag = new StringBuffer();
                                StringBuffer tempInsideLimitFlag = new StringBuffer();
                                StringBuffer tempQcBatchSampleType = new StringBuffer();
                                StringBuffer evalRuleSequence = new StringBuffer();
                                StringBuffer tempEvalRuleDesc = new StringBuffer();
                                StringBuffer tempWarningFlag = new StringBuffer();
                                for (int dsEvalRuleItemCount = 0; dsEvalRuleItemCount < dsEvalRuleItem.size(); ++dsEvalRuleItemCount) {
                                    tempQcBatchSampleType.append(newQCBatchSampleTypeId[dsQCMSTrow]).append(";");
                                    tempViolationCount.append(dsEvalRuleItem.getValue(dsEvalRuleItemCount, "violationcount")).append(";");
                                    tempWindowSize.append(dsEvalRuleItem.getValue(dsEvalRuleItemCount, "windowsize")).append(";");
                                    tempSigmaAboveCl.append(dsEvalRuleItem.getValue(dsEvalRuleItemCount, "sigmaabovecl")).append(";");
                                    tempSigmaBelowCl.append(dsEvalRuleItem.getValue(dsEvalRuleItemCount, "sigmabelowcl")).append(";");
                                    tempRulePatternFlag.append(dsEvalRuleItem.getValue(dsEvalRuleItemCount, "rulepatternflag")).append(";");
                                    tempInsideLimitFlag.append(dsEvalRuleItem.getValue(dsEvalRuleItemCount, "insidelimitflag")).append(";");
                                    tempEvalRuleDesc.append(dsEvalRuleItem.getValue(dsEvalRuleItemCount, "qcevalruledesc")).append(";");
                                    evalRuleSequence.append(OpalUtil.getNextSequence("s_qcbatchevalrule", this.getSequenceProcessor())).append(";");
                                    tempWarningFlag.append(dsEvalRuleItem.getValue(dsEvalRuleItemCount, "warningflag")).append(";");
                                }
                                String qcBatchSampleTypeIds = tempQcBatchSampleType.substring(0, tempQcBatchSampleType.length() - 1);
                                String violationCounts = tempViolationCount.substring(0, tempViolationCount.length() - 1);
                                String windowSizes = tempWindowSize.substring(0, tempWindowSize.length() - 1);
                                String sigmaAboveCls = tempSigmaAboveCl.substring(0, tempSigmaAboveCl.length() - 1);
                                String sigmaBelowCls = tempSigmaBelowCl.substring(0, tempSigmaBelowCl.length() - 1);
                                String rulePatternFlags = tempRulePatternFlag.substring(0, tempRulePatternFlag.length() - 1);
                                String insideLimitFlags = tempInsideLimitFlag.substring(0, tempInsideLimitFlag.length() - 1);
                                String qcevalruledesc = tempEvalRuleDesc.substring(0, tempEvalRuleDesc.length() - 1);
                                evalRuleSequences = evalRuleSequence.substring(0, evalRuleSequence.length() - 1);
                                String warningFlags = tempWarningFlag.substring(0, tempWarningFlag.length() - 1);
                                DataSet dsInsertEvalRule = new DataSet();
                                dsInsertEvalRule.addColumnValues("s_qcbatchsampletypeid", 0, qcBatchSampleTypeIds, ";");
                                dsInsertEvalRule.addColumnValues("s_qcbatchevalruleid", 0, evalRuleSequences, ";");
                                dsInsertEvalRule.addColumnValues("violationcount", 1, violationCounts, ";");
                                dsInsertEvalRule.addColumnValues("windowsize", 1, windowSizes, ";");
                                dsInsertEvalRule.addColumnValues("sigmaabovecl", 1, sigmaAboveCls, ";");
                                dsInsertEvalRule.addColumnValues("sigmabelowcl", 1, sigmaBelowCls, ";");
                                dsInsertEvalRule.addColumnValues("rulepatternflag", 0, rulePatternFlags, ";");
                                dsInsertEvalRule.addColumnValues("insidelimitflag", 0, insideLimitFlags, ";");
                                dsInsertEvalRule.addColumnValues("qcbatchevalruledesc", 0, qcevalruledesc, ";");
                                dsInsertEvalRule.addColumnValues("warningflag", 0, warningFlags, ";");
                                DataSetUtil.insert(this.database, dsInsertEvalRule, "s_qcbatchevalrule");
                                dsInsertEvalRule.clear();
                            }
                            psmtGetMSTLimitDetails.setString(1, qcMethodSampleTypeId[dsQCMSTrow]);
                            DataSet dsParamSet = new DataSet(psmtGetMSTLimitDetails.executeQuery());
                            boolean attemptToCopyLimits = true;
                            if (dsParamSet == null || dsParamSet.size() == 0) {
                                attemptToCopyLimits = false;
                                this.logger.warn("No evaluation limits found for the specified QC Method.");
                            }
                            if (!attemptToCopyLimits) continue;
                            StringBuffer tempParamId = new StringBuffer();
                            StringBuffer tempTargetValue = new StringBuffer();
                            StringBuffer tempTargetUnits = new StringBuffer();
                            StringBuffer tempSd = new StringBuffer();
                            StringBuffer paramSetSequence = new StringBuffer();
                            StringBuffer tempQcBatchSampleType = new StringBuffer();
                            for (int dsParamSetCount = 0; dsParamSetCount < dsParamSet.size(); ++dsParamSetCount) {
                                tempQcBatchSampleType.append(newQCBatchSampleTypeId[dsQCMSTrow]).append(";");
                                tempParamId.append(dsParamSet.getValue(dsParamSetCount, "paramid")).append(";");
                                tempTargetValue.append(dsParamSet.getValue(dsParamSetCount, "targetvalue")).append(";");
                                tempTargetUnits.append(dsParamSet.getValue(dsParamSetCount, "targetunits")).append(";");
                                tempSd.append(dsParamSet.getValue(dsParamSetCount, "sd")).append(";");
                                paramSetSequence.append(OpalUtil.getNextSequence("s_qcbatchparamset", this.getSequenceProcessor())).append(";");
                            }
                            String qcBatchSampleTypeIds = tempQcBatchSampleType.substring(0, tempQcBatchSampleType.length() - 1);
                            String paramIds = tempParamId.substring(0, tempParamId.length() - 1);
                            String targetValues = tempTargetValue.substring(0, tempTargetValue.length() - 1);
                            String targetUnits = tempTargetUnits.substring(0, tempTargetUnits.length() - 1);
                            String sds = tempSd.substring(0, tempSd.length() - 1);
                            String paramSetSequences = paramSetSequence.substring(0, paramSetSequence.length() - 1);
                            DataSet dsInsertParamSet = new DataSet();
                            dsInsertParamSet.addColumnValues("s_qcbatchsampletypeid", 0, qcBatchSampleTypeIds, ";");
                            dsInsertParamSet.addColumnValues("s_qcbatchparamsetid", 0, paramSetSequences, ";");
                            dsInsertParamSet.addColumnValues("paramid", 0, paramIds, ";");
                            dsInsertParamSet.addColumnValues("targetvalue", 0, targetValues, ";");
                            dsInsertParamSet.addColumnValues("targetunits", 0, targetUnits, ";");
                            dsInsertParamSet.addColumnValues("sd", 0, sds, ";");
                            DataSetUtil.insert(this.database, dsInsertParamSet, "s_qcbatchparamset");
                            dsInsertParamSet.clear();
                            if (!attemptToCopyLimits || !attemptToCopyEvalRule) continue;
                            String[] paramSetIds = StringUtil.split(paramSetSequences, ";");
                            String[] evalRuleIds = StringUtil.split(evalRuleSequences, ";");
                            StringBuffer tempParamSetIds = new StringBuffer();
                            StringBuffer tempEvalRuleIds = new StringBuffer();
                            StringBuffer tempEvalStatus = new StringBuffer();
                            tempQcBatchSampleType = new StringBuffer();
                            for (int dsEvalRuleParamCount = 0; dsEvalRuleParamCount < evalRuleIds.length; ++dsEvalRuleParamCount) {
                                for (int dsParamSetCount = 0; dsParamSetCount < paramSetIds.length; ++dsParamSetCount) {
                                    tempQcBatchSampleType.append(newQCBatchSampleTypeId[dsQCMSTrow]).append(";");
                                    tempParamSetIds.append(paramSetIds[dsParamSetCount]).append(";");
                                    tempEvalRuleIds.append(evalRuleIds[dsEvalRuleParamCount]).append(";");
                                    tempEvalStatus.append(";");
                                }
                            }
                            qcBatchSampleTypeIds = tempQcBatchSampleType.substring(0, tempQcBatchSampleType.length() - 1);
                            String paramSetIdsToInsert = tempParamSetIds.substring(0, tempParamSetIds.length() - 1);
                            String evalRuleIdsToInsert = tempEvalRuleIds.substring(0, tempEvalRuleIds.length() - 1);
                            String evalStatusToInsert = tempEvalStatus.substring(0, tempEvalStatus.length() - 1);
                            DataSet dsInsertEvalRuleParam = new DataSet();
                            dsInsertEvalRuleParam.addColumnValues("s_qcbatchsampletypeid", 0, qcBatchSampleTypeIds, ";");
                            dsInsertEvalRuleParam.addColumnValues("s_qcbatchparamsetid", 0, paramSetIdsToInsert, ";");
                            dsInsertEvalRuleParam.addColumnValues("s_qcbatchevalruleid", 0, evalRuleIdsToInsert, ";");
                            dsInsertEvalRuleParam.addColumnValues("evalstatus", 0, evalStatusToInsert, ";");
                            DataSetUtil.insert(this.database, dsInsertEvalRuleParam, "s_qcbatchevalruleparam");
                            dsInsertEvalRuleParam.clear();
                        }
                    }
                    if (paramListId.trim().length() > 0) {
                        PropertyList addDSProps = new PropertyList();
                        if (paramListVersionId.trim().length() == 0) {
                            paramListVersionId = "C";
                        }
                        addDSProps.setProperty("sdcid", "QCBatch");
                        addDSProps.setProperty("keyid1", newQCBatchId);
                        addDSProps.setProperty("paramlistid", paramListId);
                        addDSProps.setProperty("paramlistversionid", paramListVersionId);
                        addDSProps.setProperty("variantid", variantId);
                        aps.processAction("AddDataSet", "1", addDSProps);
                    }
                    if (specId.trim().length() > 0) {
                        if (specVersionIdAttr.trim().length() == 0) {
                            specVersionIdAttr = "C";
                        }
                        PropertyList addSpecProps = new PropertyList();
                        addSpecProps.setProperty("sdcid", "QCBatch");
                        addSpecProps.setProperty("keyid1", newQCBatchId);
                        addSpecProps.setProperty("specid", specId);
                        addSpecProps.setProperty("specversionid", specVersionIdAttr);
                        aps.processAction("AddSDISpec", "1", addSpecProps);
                    }
                }
                this.database.closeStatement("GetMSTLimitDetails");
                this.database.closeStatement("GetMSTRuleDetails");
                if (!guiMode) {
                    QCPositioning defaultPositioning;
                    safeSQL.reset();
                    DataSet dsUnknowns = qp.getPreparedSqlDataSet(Aqc.getDistinctUnknownSampleIds(batchSDI, safeSQL), safeSQL.getValues());
                    String unknownsString = "";
                    columns = "";
                    for (int columnCount = 0; columnCount < QCPositioning.REQUIRED_COLUMNS.length; ++columnCount) {
                        columns = columns + QCPositioning.REQUIRED_COLUMNS[columnCount] + "|";
                    }
                    if (columns.length() >= 1) {
                        columns = columns.substring(0, columns.length() - 1);
                    }
                    if (dsUnknowns != null && dsUnknowns.size() > 0) {
                        for (int dsUnknownsCount = 0; dsUnknownsCount < dsUnknowns.size(); ++dsUnknownsCount) {
                            for (int columnCount = 0; columnCount < QCPositioning.REQUIRED_COLUMNS.length; ++columnCount) {
                                unknownsString = QCPositioning.REQUIRED_COLUMNS[columnCount].equalsIgnoreCase("keyid1") || QCPositioning.REQUIRED_COLUMNS[columnCount].equalsIgnoreCase("qcbatchitemdesc") ? unknownsString + dsUnknowns.getValue(dsUnknownsCount, QCPositioning.REQUIRED_COLUMNS[columnCount]) + "," : unknownsString + ",";
                            }
                            unknownsString = unknownsString.substring(0, unknownsString.length() - 1);
                            unknownsString = unknownsString + "|";
                        }
                        if (unknownsString.length() > 0 && unknownsString.charAt(unknownsString.length() - 1) == '|') {
                            unknownsString = unknownsString.substring(0, unknownsString.length() - 1);
                        }
                    } else {
                        unknownsString = "";
                    }
                    if (!insertMode) {
                        defaultPositioning = new QCPositioning();
                        defaultPositioning.setReferToBatchNotMethod(true);
                        defaultPositioning.setBatchID(newQCBatchId);
                        defaultPositioning.setQueryProcessor(this.getQueryProcessor());
                        defaultPositioning.setUnknownSampleColumns(columns);
                        defaultPositioning.setUnknownSamplesData(unknownsString);
                        defaultPositioning.insertQCSamples();
                        finalSampleList = defaultPositioning.getFinalSamplesList();
                    } else {
                        defaultPositioning = new QCPositioning();
                        defaultPositioning.setMethodID(qcMethodId);
                        defaultPositioning.setMethodVersionID(qcMethodVersionId);
                        defaultPositioning.setQueryProcessor(this.getQueryProcessor());
                        defaultPositioning.setUnknownSampleColumns(columns);
                        defaultPositioning.setUnknownSamplesData(unknownsString);
                        defaultPositioning.insertQCSamples();
                        finalSampleList = defaultPositioning.getFinalSamplesList();
                    }
                }
                ElementColumns elementColumns = new ElementColumns(columns);
                ElementData elementData = new ElementData(elementColumns, finalSampleList.replaceAll("&#39;", "'"));
                String[] newSample = new String[elementData.size()];
                HashMap<String, String> qcBatchItemDetails = new HashMap<String, String>();
                ArrayList qcBatchItems = new ArrayList();
                DataSet dsApplyAction = new DataSet();
                dsApplyAction.addColumn("qcbatchsampletypeid", 0);
                dsApplyAction.addColumn("qcsampletype", 0);
                dsApplyAction.addColumn("qcsampletemplate", 0);
                dsApplyAction.addColumn("applyaction", 0);
                dsApplyAction.addColumn("locations", 0);
                String[] qcBatchItemDesc = StringUtil.split(elementData.getColumnDataBufferWithMultiCharDelimeter("qcbatchitemdesc", "#semicolon#").replaceAll("#COMMA#", ","), "#semicolon#");
                String[] qcSampleType = StringUtil.split(elementData.getColumnDataBuffer("qcsampletype", ";"), ";");
                String[] sampleId = StringUtil.split(elementData.getColumnDataBuffer("keyid1", ";").replaceAll("#COMMA#", ","), ";");
                String[] sampleIdOrigCopy = StringUtil.split(elementData.getColumnDataBuffer("keyid1", ";").replaceAll("#COMMA#", ","), ";");
                String[] qcMethodSampleTypeId_a = StringUtil.split(elementData.getColumnDataBuffer("qcbatchsampletypeid", ";"), ";");
                String[] linkTo = StringUtil.split(elementData.getColumnDataBuffer("linktoqcbatchitemid", ";"), ";");
                String[] linkedTo = StringUtil.split(elementData.getColumnDataBuffer("linkedto", ";"), ";");
                for (int i = 0; i < elementData.size(); ++i) {
                    if (!guiMode && (qcSampleType[i] == null || qcSampleType[i].trim().equals(""))) {
                        qcSampleType[i] = SAMPLETYPE_UNKNOWN;
                    }
                    if (qcMethodSampleTypeId_a[i] == null || qcMethodSampleTypeId_a[i].trim().length() == 0) {
                        qcMethodSampleTypeId_a[i] = (String)qcTypeMethSampleTypeIdMapping.get(qcSampleType[i]);
                    }
                    String tempSampleId = sampleId[i];
                    String[] tempMapping = null;
                    String tempQcBatchSampleTypeId = null;
                    if (!tempSampleId.startsWith("(Auto)")) continue;
                    tempMapping = (String[])sampleTypeIdMappings.get(qcMethodSampleTypeId_a[i]);
                    if (tempMapping == null) {
                        HashMap<String, String> token = new HashMap<String, String>();
                        token.put("qcsample", qcSampleType[i]);
                        throw new SapphireException("\n" + tp.translate("Cannot proceed. Selected QC Sample [qcsample] does not exist in the QCMethod", token) + ": " + qcMethodId + "," + qcMethodVersionId);
                    }
                    tempQcBatchSampleTypeId = insertMode ? tempMapping[0] : qcMethodSampleTypeId_a[i];
                    int r = dsApplyAction.addRow();
                    dsApplyAction.setString(r, "qcbatchsampletypeid", tempQcBatchSampleTypeId);
                    dsApplyAction.setString(r, "qcsampletype", qcSampleType[i]);
                    dsApplyAction.setString(r, "qcsampletemplate", tempMapping[3]);
                    dsApplyAction.setString(r, "applyaction", tempMapping[1]);
                    dsApplyAction.setString(r, "locations", "" + i);
                }
                dsApplyAction.sort("applyaction");
                ArrayList<DataSet> actionList = dsApplyAction.getGroupedDataSets("applyaction");
                for (int g = 0; g < actionList.size(); ++g) {
                    DataSet actionGroup = actionList.get(g);
                    actionGroup.sort("qcsampletemplate");
                    ArrayList<DataSet> templateList = actionGroup.getGroupedDataSets("qcsampletemplate");
                    for (int k = 0; k < templateList.size(); ++k) {
                        DataSet templateGrp = templateList.get(k);
                        int copies = templateGrp.getRowCount();
                        if (copies <= 0) continue;
                        HashMap<String, String> sampleDetails = new HashMap<String, String>();
                        sampleDetails.put("qcbatchid", newQCBatchId);
                        sampleDetails.put("resolvelink", "N");
                        sampleDetails.put("copies", "" + copies);
                        sampleDetails.put("qcsampletype", templateGrp.getColumnValues("qcsampletype", ";"));
                        sampleDetails.put("qcsampletemplate", templateGrp.getValue(0, "qcsampletemplate"));
                        sampleDetails.put("batchsdcid", batchSdcId);
                        if (userSiteId != null && userSiteId.length() > 0) {
                            sampleDetails.put("sitedepartmentid", userSiteId);
                        } else if (qcbatchSiteId.length() > 0) {
                            sampleDetails.put("sitedepartmentid", qcbatchSiteId);
                        }
                        if (copySecurityDeptToSample) {
                            sampleDetails.put("securitydepartment", qcBatchSecurityDept);
                        }
                        try {
                            aps.processAction(templateGrp.getValue(0, "applyaction"), "1", sampleDetails);
                        }
                        catch (ActionException e) {
                            if (rsetIdHolder.value != null && !rsetIdHolder.value.equals("")) {
                                damProcessor.clearRSet(rsetIdHolder.value);
                            }
                            this.rc = this.setError(e.getMessage(), e);
                            return this.rc;
                        }
                        String newKeyIds = (String)sampleDetails.get("newkeyid1");
                        String[] newSampleIds = StringUtil.split(newKeyIds, ";");
                        String[] indexes = StringUtil.split(templateGrp.getColumnValues("locations", ";"), ";");
                        for (int j = 0; j < indexes.length; ++j) {
                            int index = Integer.parseInt(indexes[j]);
                            sampleId[index] = newSampleIds[j];
                            newSample[index] = "Y";
                        }
                    }
                }
                DataSet dsInsert = new DataSet();
                DataSet dsUpdate = new DataSet();
                dsInsert.addColumn("s_qcbatchid", 0);
                dsInsert.addColumn("s_qcbatchitemid", 0);
                dsInsert.addColumn("qcbatchitemdesc", 0);
                dsInsert.addColumn("qcbatchsampletypeid", 0);
                dsInsert.addColumn("batchitemtype", 0);
                dsInsert.addColumn("usersequence", 1);
                dsUpdate.addColumn("s_qcbatchid", 0);
                dsUpdate.addColumn("s_qcbatchitemid", 0);
                dsUpdate.addColumn("qcbatchitemdesc", 0);
                dsUpdate.addColumn("batchitemtype", 0);
                dsUpdate.addColumn("usersequence", 1);
                PreparedStatement psmtGetQCBatchItemId = this.database.prepareStatement("GetQCBatchItemId", "select s_qcbatchitemid from sdidata  where sdcid = ?  and s_qcbatchid = ?  and keyid1 = ? ");
                psmtGetQCBatchItemId.setString(1, batchSdcId);
                psmtGetQCBatchItemId.setString(2, newQCBatchId);
                for (int l = 0; l < elementData.size(); ++l) {
                    String tempSampleId = sampleIdOrigCopy[l];
                    String[] tempMapping = null;
                    String tempQcBatchSampleTypeId = null;
                    if (tempSampleId.startsWith("(Auto)") || !SAMPLETYPE_UNKNOWN.equalsIgnoreCase(qcSampleType[l])) {
                        if (sampleTypeIdMappings.get(qcMethodSampleTypeId_a[l]) == null) continue;
                        tempMapping = (String[])sampleTypeIdMappings.get(qcMethodSampleTypeId_a[l]);
                        tempQcBatchSampleTypeId = insertMode ? tempMapping[0] : qcMethodSampleTypeId_a[l];
                    }
                    String batchItemSequence = null;
                    boolean isQcSample = sampleIdOrigCopy[l].startsWith("(Auto)");
                    boolean attemptInsertOrUpdate = true;
                    if (insertMode || isQcSample) {
                        batchItemSequence = OpalUtil.getNextSequence("s_qcbatchitem", this.getSequenceProcessor());
                    } else if (!isQcSample) {
                        psmtGetQCBatchItemId.setString(3, sampleId[l]);
                        DataSet dsQcBatchItemId = new DataSet(psmtGetQCBatchItemId.executeQuery());
                        if (dsQcBatchItemId == null || dsQcBatchItemId.size() == 0) {
                            attemptInsertOrUpdate = false;
                            this.logger.warn("QCBatchitem not found for the  sample " + sampleId[l] + " in the QC Batch.  ");
                        } else {
                            batchItemSequence = dsQcBatchItemId.getValue(0, "s_qcbatchitemid");
                        }
                    }
                    if (!attemptInsertOrUpdate) continue;
                    if (insertMode || isQcSample) {
                        dsInsert.addRow();
                        dsInsert.setValue(dsInsert.size() - 1, "s_qcbatchid", newQCBatchId);
                        dsInsert.setValue(dsInsert.size() - 1, "s_qcbatchitemid", batchItemSequence);
                        dsInsert.setValue(dsInsert.size() - 1, "qcbatchitemdesc", qcBatchItemDesc[l]);
                        dsInsert.setValue(dsInsert.size() - 1, "qcbatchsampletypeid", tempQcBatchSampleTypeId);
                        dsInsert.setValue(dsInsert.size() - 1, "batchitemtype", qcSampleType[l]);
                        dsInsert.setValue(dsInsert.size() - 1, "usersequence", String.valueOf(l + 1));
                    } else {
                        dsUpdate.addRow();
                        dsUpdate.setValue(dsUpdate.size() - 1, "s_qcbatchid", newQCBatchId);
                        dsUpdate.setValue(dsUpdate.size() - 1, "s_qcbatchitemid", batchItemSequence);
                        dsUpdate.setValue(dsUpdate.size() - 1, "qcbatchitemdesc", qcBatchItemDesc[l]);
                        dsUpdate.setValue(dsUpdate.size() - 1, "batchitemtype", SAMPLETYPE_UNKNOWN);
                        dsUpdate.setValue(dsUpdate.size() - 1, "usersequence", String.valueOf(l + 1));
                    }
                    qcBatchItemDetails.put("keyid1", sampleId[l]);
                    qcBatchItemDetails.put("qcbatchitemid", batchItemSequence);
                    qcBatchItemDetails.put("qcbatchitemdesc", qcBatchItemDesc[l]);
                    if (tempSampleId.startsWith("(Auto)") || !SAMPLETYPE_UNKNOWN.equalsIgnoreCase(qcSampleType[l])) {
                        qcBatchItemDetails.put("linkto", linkTo[l]);
                        qcBatchItemDetails.put("qcsampletype", qcSampleType[l]);
                        qcBatchItemDetails.put("linkedto", linkedTo[l]);
                        qcBatchItemDetails.put("qcbatchsampletypeid", tempMapping[0]);
                    } else {
                        qcBatchItemDetails.put("linkto", null);
                        qcBatchItemDetails.put("qcsampletype", null);
                        qcBatchItemDetails.put("linkedto", null);
                        qcBatchItemDetails.put("qcbatchsampletypeid", null);
                    }
                    qcBatchItemDetails.put("newsampleflag", newSample[l]);
                    qcBatchItems.add(new HashMap(qcBatchItemDetails));
                }
                this.database.closeStatement("GetQCBatchItemId");
                if (dsInsert.getRowCount() > 0) {
                    DataSetUtil.insert(this.database, dsInsert, "s_qcbatchitem");
                    dsInsert.clear();
                }
                if (dsUpdate.getRowCount() > 0) {
                    String[] keyCols = new String[]{"s_qcbatchid", "s_qcbatchitemid"};
                    DataSetUtil.update(this.database, dsUpdate, "s_qcbatchitem", keyCols);
                    dsUpdate.clear();
                }
                DataSet dsResolveLink = new DataSet();
                dsResolveLink.addColumn("keyid1", 0);
                dsResolveLink.addColumn("keyid2", 0);
                dsResolveLink.addColumn("keyid3", 0);
                dsResolveLink.addColumn("qcbatchitemid", 0);
                dsResolveLink.addColumn("linktoqcbatchitemid", 0);
                dsResolveLink.addColumn("linkedto", 0);
                dsResolveLink.addColumn("qcbatchsampletypeid", 0);
                dsResolveLink.addColumn("applyaction", 0);
                String unknownSDCId = new String();
                StringBuffer unknownkeyid1 = new StringBuffer();
                StringBuffer unknownkeyid2 = new StringBuffer();
                StringBuffer unknownkeyid3 = new StringBuffer();
                StringBuffer unknownParamlistid = new StringBuffer();
                StringBuffer unknownParamlistversionid = new StringBuffer();
                StringBuffer unknownVariantid = new StringBuffer();
                StringBuffer unknownDataset = new StringBuffer();
                StringBuffer unknownQCBatchId = new StringBuffer();
                StringBuffer unknownQCBatchItemId = new StringBuffer();
                int totalQCSamples = 0;
                HashMap<String, String> linkedToIdentification = new HashMap<String, String>();
                PreparedStatement psmtUnknownDatasets = this.database.prepareStatement("unknowndatasets", QCUtil.getDistinctUnknownDatasetsSQL(paramListType, matchWorkItemItem));
                psmtUnknownDatasets.setString(1, batchSdcId);
                StringBuffer nodataseterrorMsg = new StringBuffer();
                for (int n = 0; n < qcBatchItems.size(); ++n) {
                    int tempLoc;
                    HashMap tempQcBatchItem = (HashMap)qcBatchItems.get(n);
                    String tempSampleId = (String)tempQcBatchItem.get("keyid1");
                    String tempQCBatchItemId = (String)tempQcBatchItem.get("qcbatchitemid");
                    String tempQCBatchSampleTypeId = (String)tempQcBatchItem.get("qcbatchsampletypeid");
                    String tempQCSampleType = (String)tempQcBatchItem.get("qcsampletype");
                    if (tempSampleId.length() > 0 && !tempSampleId.startsWith("(Auto)") && insertMode) {
                        int k = 2;
                        if (paramListType.trim().length() > 0) {
                            psmtUnknownDatasets.setString(k++, paramListType);
                        }
                        psmtUnknownDatasets.setString(k++, tempSampleId);
                        psmtUnknownDatasets.setString(k++, qcMethodId);
                        psmtUnknownDatasets.setString(k, qcMethodVersionId);
                        DataSet dsUnknown = new DataSet(psmtUnknownDatasets.executeQuery());
                        boolean attemptUnknownsUpdate = true;
                        if (dsUnknown == null || dsUnknown.size() == 0) {
                            attemptUnknownsUpdate = false;
                            this.logger.warn("No dataset found for the specified QC Batch - qc sample");
                            if (tempQCBatchSampleTypeId == null) {
                                nodataseterrorMsg.append(",\n ").append(tempSampleId).append(" " + tp.translate("at position") + " " + (n + 1));
                            }
                        } else if (!(tempQCBatchSampleTypeId != null && tempQCBatchSampleTypeId.length() != 0 || sdiwiList.size() <= 0 && sdidataList.size() <= 0)) {
                            if (sdiworkitemids != null && sdiworkitemids.trim().length() > 0) {
                                if ((dsUnknown = QCUtil.filterBySDIWorkItem(dsUnknown, sdiwiList, tp)).getRowCount() == 0) {
                                    attemptUnknownsUpdate = false;
                                    nodataseterrorMsg.append(",\n ").append(tempSampleId).append(" " + tp.translate("at position") + " " + (n + 1));
                                }
                            } else if (sdidataids != null && sdidataids.trim().length() > 0 && (dsUnknown = QCUtil.filterBySDIData(dsUnknown, sdidataList, tp)).getRowCount() == 0) {
                                attemptUnknownsUpdate = false;
                                nodataseterrorMsg.append(",\n ").append(tempSampleId).append(" " + tp.translate("at position") + " " + (n + 1));
                            }
                        }
                        if (attemptUnknownsUpdate) {
                            int rowCount = dsUnknown.getRowCount();
                            unknownSDCId = dsUnknown.getValue(0, "sdcid");
                            StringBuffer tempUnknownkeyid1 = new StringBuffer();
                            StringBuffer tempUnknownkeyid2 = new StringBuffer();
                            StringBuffer tempUnknownkeyid3 = new StringBuffer();
                            StringBuffer tempParamlistid = new StringBuffer();
                            StringBuffer tempParamlistversionid = new StringBuffer();
                            StringBuffer tempVariantid = new StringBuffer();
                            StringBuffer tempDataset = new StringBuffer();
                            StringBuffer tempQCBatchId = new StringBuffer();
                            StringBuffer tempQCBatchItem = new StringBuffer();
                            for (int p = 0; p < rowCount; ++p) {
                                tempUnknownkeyid1.append(dsUnknown.getValue(p, "keyid1")).append(";");
                                tempUnknownkeyid2.append(dsUnknown.getValue(p, "keyid2")).append(";");
                                tempUnknownkeyid3.append(dsUnknown.getValue(p, "keyid3")).append(";");
                                tempParamlistid.append(dsUnknown.getValue(p, "paramlistid")).append(";");
                                tempParamlistversionid.append(dsUnknown.getValue(p, "paramlistversionid")).append(";");
                                tempVariantid.append(dsUnknown.getValue(p, "variantid")).append(";");
                                tempDataset.append(dsUnknown.getValue(p, "dataset")).append(";");
                                tempQCBatchItem.append(tempQCBatchItemId).append(";");
                                tempQCBatchId.append(newQCBatchId).append(";");
                            }
                            unknownkeyid1.append(tempUnknownkeyid1.toString());
                            unknownkeyid2.append(tempUnknownkeyid2.toString());
                            unknownkeyid3.append(tempUnknownkeyid3.toString());
                            unknownParamlistid.append(tempParamlistid.toString());
                            unknownParamlistversionid.append(tempParamlistversionid.toString());
                            unknownVariantid.append(tempVariantid.toString());
                            unknownDataset.append(tempDataset.toString());
                            unknownQCBatchItemId.append(tempQCBatchItem.toString());
                            unknownQCBatchId.append(tempQCBatchId.toString());
                        }
                    }
                    if (tempQCSampleType == null) continue;
                    String tempLinkTo = (String)tempQcBatchItem.get("linkto");
                    String tempLinkedTo = (String)tempQcBatchItem.get("linkedto");
                    ++totalQCSamples;
                    String tempKeyId1 = (String)tempQcBatchItem.get("keyid1");
                    if (tempLinkedTo == null && tempLinkedTo.trim().length() <= 0 || (tempLoc = this.validateLinkedToValue(tempLinkTo, tempLinkedTo, guiMode, n)) < 1 || tempLoc > qcBatchItems.size()) continue;
                    HashMap linkedToQCBatchItem = (HashMap)qcBatchItems.get(tempLoc - 1);
                    String tempLinkedToQCBatchItemId = (String)linkedToQCBatchItem.get("qcbatchitemid");
                    linkedToIdentification.put(tempQCBatchItemId, tempLinkedToQCBatchItemId);
                    if (tempKeyId1 != null && !tempKeyId1.trim().equals("")) continue;
                    tempKeyId1 = (String)linkedToQCBatchItem.get("keyid1");
                    tempQcBatchItem.put("keyid1", tempKeyId1);
                }
                if (nodataseterrorMsg.length() > 0) {
                    throw new SapphireException("\n" + tp.translate("No Matching dataset found for the selected ") + batchSdcId + ": " + nodataseterrorMsg.substring(1));
                }
                this.database.closeStatement("unknowndatasets");
                this.database.closeStatement("unknowndatasetsonworkitem");
                if (!unknownSDCId.toString().trim().equals("")) {
                    String unknownKeyid1Str = unknownkeyid1.substring(0, unknownkeyid1.length() - 1);
                    String unknownkeyid2Str = unknownkeyid2.substring(0, unknownkeyid2.length() - 1);
                    String unknownkeyid3Str = unknownkeyid3.substring(0, unknownkeyid3.length() - 1);
                    String unknownParamlistidStr = unknownParamlistid.substring(0, unknownParamlistid.length() - 1);
                    String unknownParamlistversionidStr = unknownParamlistversionid.substring(0, unknownParamlistversionid.length() - 1);
                    String unknownVariantidStr = unknownVariantid.substring(0, unknownVariantid.length() - 1);
                    String unknownDatasetStr = unknownDataset.substring(0, unknownDataset.length() - 1);
                    String unknownQCBatchItemIdStr = unknownQCBatchItemId.substring(0, unknownQCBatchItemId.length() - 1);
                    String unknownQCBatchIdStr = unknownQCBatchId.substring(0, unknownQCBatchId.length() - 1);
                    ActionProcessor apUpdate = this.getActionProcessor();
                    HashMap<String, String> unknownsEdit = new HashMap<String, String>();
                    unknownsEdit.put("propsmatch", "Y");
                    unknownsEdit.put("sdcid", unknownSDCId);
                    unknownsEdit.put("keyid1", unknownKeyid1Str);
                    unknownsEdit.put("keyid2", unknownkeyid2Str);
                    unknownsEdit.put("keyid3", unknownkeyid3Str);
                    unknownsEdit.put("paramlistid", unknownParamlistidStr);
                    unknownsEdit.put("paramlistversionid", unknownParamlistversionidStr);
                    unknownsEdit.put("variantid", unknownVariantidStr);
                    unknownsEdit.put("dataset", unknownDatasetStr);
                    unknownsEdit.put("s_qcbatchid", unknownQCBatchIdStr);
                    unknownsEdit.put("s_qcbatchitemid", unknownQCBatchItemIdStr);
                    try {
                        apUpdate.processAction("EditDataSet", "1", unknownsEdit);
                    }
                    catch (ActionException ae) {
                        if (rsetIdHolder.value != null && !rsetIdHolder.value.equals("")) {
                            damProcessor.clearRSet(rsetIdHolder.value);
                        }
                        return this.setError("Exception caught: " + ae.getMessage(), ae);
                    }
                }
                Collection actionApplyCollection = sampleTypeIdMappings.values();
                Iterator iter = actionApplyCollection.iterator();
                HashMap<String, String> qcApplyMappings = new HashMap<String, String>();
                ArrayList<String> qcApplyActions = new ArrayList<String>();
                HashMap<String, String> workItemMapping = new HashMap<String, String>();
                while (iter.hasNext()) {
                    String[] temp = (String[])iter.next();
                    if (!qcApplyActions.contains(temp[1])) {
                        qcApplyActions.add(temp[1]);
                    }
                    qcApplyMappings.put(temp[0], temp[1]);
                    try {
                        if (temp[2] == null || temp[2].trim().length() <= 0) continue;
                        workItemMapping.put(temp[0], temp[2]);
                    }
                    catch (ArrayIndexOutOfBoundsException unknownDatasetStr) {}
                }
                HashMap<String, HashMap> sampleDetailsHolder = new HashMap<String, HashMap>();
                HashMap batches = new HashMap();
                HashMap batchSampleTypeMapping = new HashMap();
                HashMap sampleTypeBatchMapping = new HashMap();
                HashMap batchsPrecedence = new HashMap();
                HashMap batchIdentification = new HashMap();
                int batchNumber = 1;
                while (batchIdentification.size() < totalQCSamples) {
                    for (int n = 0; n < qcBatchItems.size(); ++n) {
                        int tempLoc;
                        HashMap tempQcBatchItem = (HashMap)qcBatchItems.get(n);
                        String tempQCBatchItemId = (String)tempQcBatchItem.get("qcbatchitemid");
                        String tempQCBatchSampleTypeId = (String)tempQcBatchItem.get("qcbatchsampletypeid");
                        if (tempQCBatchSampleTypeId == null || batchIdentification.containsKey(tempQCBatchItemId)) continue;
                        String tempLinkTo = (String)tempQcBatchItem.get("linkto");
                        String tempLinkedTo = (String)tempQcBatchItem.get("linkedto");
                        if (workItemMapping.containsKey(tempQCBatchSampleTypeId)) {
                            String templinkedtoqcbatchitemid;
                            tempLoc = this.validateLinkedToValue(tempLinkTo, tempLinkedTo, guiMode, n);
                            if (tempLoc >= 1 && tempLoc <= qcBatchItems.size() && !(templinkedtoqcbatchitemid = (String)((HashMap)qcBatchItems.get(tempLoc - 1)).get("qcbatchitemid")).equals(tempQCBatchItemId)) {
                                tempQcBatchItem.put("linktoqcbatchitemid", templinkedtoqcbatchitemid);
                                tempQcBatchItem.put("linkedto", tempLinkedTo);
                            }
                            sampleDetailsHolder.put(tempQCBatchItemId, tempQcBatchItem);
                            batchNumber = this.checkAndAddOrUpdateBatch(batchNumber, tempQCBatchItemId, tempQCBatchSampleTypeId, batches, batchSampleTypeMapping, sampleTypeBatchMapping, batchIdentification, batchsPrecedence);
                            continue;
                        }
                        if (tempLinkedTo == null || tempLinkedTo.trim().length() == 0) {
                            tempQcBatchItem.remove("linkto");
                            tempQcBatchItem.remove("linkedto");
                            sampleDetailsHolder.put(tempQCBatchItemId, tempQcBatchItem);
                            batchNumber = this.checkAndAddOrUpdateBatch(batchNumber, tempQCBatchItemId, tempQCBatchSampleTypeId, batches, batchSampleTypeMapping, sampleTypeBatchMapping, batchIdentification, batchsPrecedence);
                            continue;
                        }
                        tempLoc = this.validateLinkedToValue(tempLinkTo, tempLinkedTo, guiMode, n);
                        if (tempLoc >= 1 && tempLoc <= qcBatchItems.size()) {
                            HashMap linkedToQCBatchItem = (HashMap)qcBatchItems.get(tempLoc - 1);
                            String tempLinkedToQCBatchItemId = (String)linkedToQCBatchItem.get("qcbatchitemid");
                            String tempLinkedToQCBatchSampleTypeId = (String)linkedToQCBatchItem.get("qcbatchsampletypeid");
                            if (tempLinkedToQCBatchItemId.equals(tempQCBatchItemId)) {
                                this.rc = this.setError(this.getTranslationProcessor().translate("QCBatchItem cannot be linked to self."));
                                return this.rc;
                            }
                            if (tempLinkedToQCBatchSampleTypeId == null) {
                                batchNumber = this.checkAndAddOrUpdateBatch(batchNumber, tempQCBatchItemId, tempQCBatchSampleTypeId, batches, batchSampleTypeMapping, sampleTypeBatchMapping, batchIdentification, batchsPrecedence);
                            } else if (batchIdentification.containsKey(tempLinkedToQCBatchItemId)) {
                                String tempLinkedToBatchName = (String)batchIdentification.get(tempLinkedToQCBatchItemId);
                                if (tempQCBatchSampleTypeId.equals(tempLinkedToQCBatchSampleTypeId)) {
                                    this.updateBatch(tempLinkedToBatchName, tempQCBatchItemId, null, batches, batchIdentification, batchsPrecedence);
                                } else {
                                    boolean createNewBatch = true;
                                    if (sampleTypeBatchMapping.containsKey(tempQCBatchSampleTypeId)) {
                                        String batchNames = (String)sampleTypeBatchMapping.get(tempQCBatchSampleTypeId);
                                        String[] batchNameArr = StringUtil.split(batchNames, ";");
                                        for (int j = 0; j < batchNameArr.length; ++j) {
                                            String batchName = batchNameArr[j];
                                            if (batchsPrecedence.containsKey(batchName)) {
                                                if (!this.checkPrecedence(batchsPrecedence, batchName, tempLinkedToBatchName)) continue;
                                                this.updateBatch(batchNameArr[j], tempQCBatchItemId, null, batches, batchIdentification, batchsPrecedence);
                                                createNewBatch = false;
                                            } else {
                                                if (batchsPrecedence.containsKey(tempLinkedToBatchName) && this.checkPrecedence(batchsPrecedence, tempLinkedToBatchName, batchName)) continue;
                                                this.updateBatch(batchNameArr[j], tempQCBatchItemId, tempLinkedToBatchName, batches, batchIdentification, batchsPrecedence);
                                                createNewBatch = false;
                                            }
                                            break;
                                        }
                                    } else {
                                        createNewBatch = true;
                                    }
                                    if (createNewBatch) {
                                        String batchName = "Batch" + batchNumber;
                                        ++batchNumber;
                                        this.createBatch(batchName, tempQCBatchItemId, tempQCBatchSampleTypeId, batches, tempLinkedToBatchName, batchSampleTypeMapping, sampleTypeBatchMapping, batchIdentification, batchsPrecedence);
                                    }
                                }
                            } else if (!batchIdentification.containsKey(tempLinkedToQCBatchItemId)) {
                                ArrayList<String> parentTrail = new ArrayList<String>();
                                parentTrail.add(tempQCBatchItemId);
                                if (this.isAnyCircularLink(tempLinkedToQCBatchItemId, parentTrail, linkedToIdentification)) {
                                    int rowNumber = n + 1;
                                    this.rc = this.setError(this.getTranslationProcessor().translate("Circular linking detected from row number") + " " + rowNumber);
                                    return this.rc;
                                }
                            }
                            tempQcBatchItem.put("linktoqcbatchitemid", tempLinkedToQCBatchItemId);
                            tempQcBatchItem.put("linkedto", tempLinkedTo);
                            sampleDetailsHolder.put(tempQCBatchItemId, tempQcBatchItem);
                            continue;
                        }
                        tempQcBatchItem.remove("linkto");
                        tempQcBatchItem.remove("linkedto");
                        sampleDetailsHolder.put(tempQCBatchItemId, tempQcBatchItem);
                        batchNumber = this.checkAndAddOrUpdateBatch(batchNumber, tempQCBatchItemId, tempQCBatchSampleTypeId, batches, batchSampleTypeMapping, sampleTypeBatchMapping, batchIdentification, batchsPrecedence);
                    }
                }
                ArrayList<String> parentBatches = new ArrayList<String>();
                ArrayList processedBatches = new ArrayList();
                while (batchsPrecedence.size() > 0) {
                    Collection finalBatches = batchsPrecedence.values();
                    for (String parentBatchName : finalBatches) {
                        if (batchsPrecedence.containsKey(parentBatchName) || parentBatches.contains(parentBatchName)) continue;
                        parentBatches.add(parentBatchName);
                        this.processBatchWithChilds(parentBatchName, newQCBatchId, batchSdcId, (String)batchSampleTypeMapping.get(parentBatchName), batchsPrecedence, batches, sampleDetailsHolder, qcApplyMappings, processedBatches, aps, dsResolveLink);
                    }
                    Iterator batchsPrecedenceItr = batchsPrecedence.keySet().iterator();
                    ArrayList<String> removePrecedence = new ArrayList<String>();
                    while (batchsPrecedenceItr.hasNext()) {
                        String batchName = (String)batchsPrecedenceItr.next();
                        if (!parentBatches.contains((String)batchsPrecedence.get(batchName))) continue;
                        removePrecedence.add(batchName);
                    }
                    for (int i = 0; i < removePrecedence.size(); ++i) {
                        batchsPrecedence.remove(removePrecedence.get(i));
                    }
                    batchsPrecedenceItr = null;
                }
                if (!batches.isEmpty()) {
                    for (String batchName : batches.keySet()) {
                        this.processBatch(batchName, newQCBatchId, batchSdcId, (String)batchSampleTypeMapping.get(batchName), batches, sampleDetailsHolder, qcApplyMappings, dsResolveLink);
                    }
                }
                dsResolveLink.sort("applyaction");
                ArrayList<DataSet> actionLst = dsResolveLink.getGroupedDataSets("applyaction");
                for (int lst = 0; lst < actionLst.size(); ++lst) {
                    DataSet actionGroup = actionLst.get(lst);
                    HashMap<String, String> properties = new HashMap<String, String>();
                    properties.put("qcbatchid", newQCBatchId);
                    properties.put("sdcid", batchSdcId);
                    properties.put("resolvelink", "Y");
                    properties.put("qcbatchsampletypeid", actionGroup.getColumnValues("qcbatchsampletypeid", ";"));
                    properties.put("qcbatchitemid", actionGroup.getColumnValues("qcbatchitemid", ";"));
                    properties.put("keyid1", actionGroup.getColumnValues("keyid1", ";"));
                    properties.put("keyid2", actionGroup.getColumnValues("keyid2", ";"));
                    properties.put("keyid3", actionGroup.getColumnValues("keyid3", ";"));
                    properties.put("linktoqcbatchitemid", actionGroup.getColumnValues("linktoqcbatchitemid", ";"));
                    properties.put("linkedto", actionGroup.getColumnValues("linkedto", ";"));
                    properties.put("createworksheet", createWorksheet ? "N" : "Y");
                    String applyActionId = actionGroup.getValue(0, "applyaction", "");
                    if (applyActionId.length() <= 0) continue;
                    try {
                        aps.processAction(applyActionId, "1", properties);
                        continue;
                    }
                    catch (ActionException e) {
                        this.logger.error("Error executing the action specified in actionapply column. " + e.getMessage(), e);
                    }
                }
            }
            catch (Exception e) {
                if (rsetIdHolder.value != null && !rsetIdHolder.value.equals("")) {
                    damProcessor.clearRSet(rsetIdHolder.value);
                }
                this.rc = this.setError(e.getMessage(), e);
                return this.rc;
            }
            try {
                QCUtil.addQCBatchSampleTypeParamSet(this.database, qp, this.__SqlGenerator, this.getSequenceProcessor(), newQCBatchId);
            }
            catch (Exception e) {
                this.rc = this.setError(e.getMessage(), e);
                return this.rc;
            }
            try {
                QCUtil.addCalcDataItems(newQCBatchId, qp, aps);
                QCBatchPool.releaseQCBatch(newQCBatchId);
                if (createWorksheet) {
                    PropertyList createWSProps = new PropertyList();
                    createWSProps.setProperty("formid", formId);
                    createWSProps.setProperty("formversionid", formVersionId);
                    createWSProps.setProperty("qcbatchid", newQCBatchId);
                    aps.processAction("CreateWorksheet", "1", createWSProps);
                    String documentId = createWSProps.getProperty("documentid");
                    String documentVersionId = createWSProps.getProperty("documentversionid");
                    PropertyList editSDIProps = new PropertyList();
                    editSDIProps.setProperty("sdcid", "QCBatch");
                    editSDIProps.setProperty("keyid1", newQCBatchId);
                    editSDIProps.setProperty("documentid", documentId);
                    editSDIProps.setProperty("documentversionid", documentVersionId);
                }
                if (createLESWorksheet) {
                    DataSet wsLes = qp.getPreparedSqlDataSet("select worksheetid, worksheetversionid, worksheetrule, authorflag from sdiworksheetrule where sdcid = ? and keyid1 = ? and keyid2 = ? ", (Object[])new String[]{"QCMethod", qcMethodId, qcMethodVersionId});
                    String wsId = "";
                    String wsVersionId = "";
                    for (int w = 0; w < wsLes.getRowCount(); ++w) {
                        if (!"default".equalsIgnoreCase(wsLes.getValue(w, "worksheetrule"))) continue;
                        wsId = wsLes.getValue(w, "worksheetid");
                        wsVersionId = wsLes.getValue(w, "worksheetversionid");
                        break;
                    }
                    if (wsId.length() > 0) {
                        String sysUserid = this.connectionInfo.getSysuserId();
                        if (sysUserid == null || "".equals(sysUserid)) {
                            sysUserid = "(system)";
                        }
                        if ("(system)".equals(sysUserid)) {
                            throw new SapphireException(tp.translate("Failed to create QCBatch because LES Worksheet Generation can not access Current User."));
                        }
                        PropertyList genWSActionProps = new PropertyList();
                        genWSActionProps.setProperty("qcbatchid", newQCBatchId);
                        genWSActionProps.setProperty("templateid", wsId);
                        genWSActionProps.setProperty("templateversionid", wsVersionId);
                        aps.processAction("GenerateQCBatchWorksheet", "1", genWSActionProps);
                    }
                }
            }
            catch (Exception e) {
                this.rc = this.setError(e.getMessage(), e);
                return this.rc;
            }
            HashMap<String, String> actionProps = new HashMap<String, String>();
            actionProps.put("qcbatchid", newQCBatchId);
            try {
                aps.processAction("UpdateQCBatchStatus", "1", actionProps);
            }
            catch (ActionException e) {
                this.logger.error("Error running UpdateQCBatchStatus", e);
            }
        } else {
            this.logger.warn("The createLockedRSet failed.");
        }
        props.put("qcbatchid", newQCBatchId);
        if (rsetIdHolder.value != null && !rsetIdHolder.value.equals("")) {
            damProcessor.clearRSet(rsetIdHolder.value);
        }
        return this.rc;
    }

    /*
     * Unable to fully structure code
     */
    private int doProcessQCBatch(HashMap props) {
        sdcid = (String)props.get("sdcid");
        keyid1 = (String)props.get("keyid1");
        sbEvalQCBatchIds = new StringBuffer();
        auditreason = (String)props.get("auditreason");
        auditactivity = (String)props.get("auditactivity");
        auditsignedflag = (String)props.get("auditsignedflag");
        saveAndRelease = "";
        qcBatchReleaseHolder = new ArrayList<QCBatch>();
        if (props.get("saveandrelease") != null) {
            saveAndRelease = (String)props.get("saveandrelease");
        }
        ap = this.getActionProcessor();
        qp = this.getQueryProcessor();
        if (sdcid != null && sdcid.equals("QCBatch")) {
            batchIds = StringUtil.split(keyid1, ";");
            for (i = 0; i < batchIds.length; ++i) {
                dsSpec = qp.getPreparedSqlDataSet("attributespec", "SELECT 1 FROM sdispec WHERE sdcid='QCBatch' AND keyid1=?", new String[]{batchIds[i]});
                if (dsSpec == null || dsSpec.getRowCount() <= 0) continue;
                qcBatch = QCBatchPool.getQCBatch(this.getQueryProcessor(), batchIds[i]);
                evalOption = qcBatch.getEvalOption();
                if (qcBatch.isDataEntered(true)) {
                    if (!evalOption.equals("Continuous") && !evalOption.equals("Conclusion")) continue;
                    sbEvalQCBatchIds.append(batchIds[i] + ";");
                    continue;
                }
                if (!evalOption.equals("Continuous")) continue;
                sbEvalQCBatchIds.append(batchIds[i] + ";");
            }
        } else {
            paramlistid = (String)props.get("paramlistid");
            paramlistversionid = (String)props.get("paramlistversionid");
            variantid = (String)props.get("variantid");
            dataset = (String)props.get("dataset");
            plDataSetList = null;
            dsList = null;
            actionprops = null;
            try {
                if (paramlistid != null && paramlistid.length() > 0) {
                    plDataSetList = OpalUtil.getPLDataSetList(paramlistid, paramlistversionid, variantid, dataset);
                    dsList = QCBatch.getQCDataSetList(this.getQueryProcessor(), sdcid, keyid1, plDataSetList);
                } else {
                    dsList = QCBatch.getQCDataSetList(this.getQueryProcessor(), sdcid, keyid1, null);
                }
            }
            catch (SapphireException e) {
                this.logger.error("[ProcessQCBatch] " + e.getMessage(), e);
                return 1;
            }
            BDCollection = new HashMap<String, ArrayList<E>>();
            this.logger.debug("[ProcessQCBatch] Creating BDCollection... [ DSList Size: " + dsList.size() + "]");
            for (i = 0; i < dsList.size(); ++i) {
                sdiDataSet = (SDIDataSet)dsList.get(i);
                if (sdiDataSet == null || (qcBatchID = sdiDataSet.getQCBatchProps(0)) == null) continue;
                if (!BDCollection.containsKey(qcBatchID)) {
                    BDCollection.put(qcBatchID, new ArrayList<E>());
                }
                list = (List)BDCollection.get(qcBatchID);
                index = Integer.parseInt(sdiDataSet.getQCBatchProps(2));
                insert = false;
                for (j = 0; j < list.size(); ++j) {
                    thisIndex = Integer.parseInt(((SDIDataSet)list.get(j)).getQCBatchProps(2));
                    if (index >= thisIndex) continue;
                    list.add(j, sdiDataSet);
                    insert = true;
                    break;
                }
                if (insert) continue;
                list.add(sdiDataSet);
            }
            keys = BDCollection.keySet();
            this.logger.debug("[ProcessQCBatch] Iterating through BDCollection...");
            actionCalcPropHolder = new HashMap<String, HashMap>();
            for (String qcBatchId : keys) {
                qcBatch = QCBatchPool.getQCBatch(this.getQueryProcessor(), qcBatchId);
                if (qcBatch == null) continue;
                if (!qcBatchReleaseHolder.contains(qcBatch)) {
                    qcBatchReleaseHolder.add(qcBatch);
                }
                evalOption = qcBatch.getEvalOption();
                qcBatchType = qcBatch.getQCBatchType();
                if (evalOption == null || evalOption.length() == 0) {
                    evalOption = "Manual";
                }
                list = (List)BDCollection.get(qcBatchId);
                queList = new ArrayList<SDIDataSet>();
                for (i = 0; i < list.size(); ++i) {
                    sdiDataSet = (SDIDataSet)list.get(i);
                    if (sdiDataSet == null) continue;
                    qcBatchItemId = sdiDataSet.getQCBatchProps(1);
                    qcBatchItem = qcBatch.getQCBatchItem(qcBatchItemId);
                    linkedBatchItemID = sdiDataSet.getQCBatchProps(4);
                    if (linkedBatchItemID != null && linkedBatchItemID.length() > 0 && !queList.contains(_sdiDataSet = (item = qcBatch.getQCBatchItem(linkedBatchItemID)).getSDIDataSet())) {
                        queList.add(_sdiDataSet);
                    }
                    if ((bracketParent = qcBatch.getBracketParent(qcBatchItemId)) != null && !queList.contains(_sdiDataSet = bracketParent.getSDIDataSet())) {
                        queList.add(_sdiDataSet);
                    }
                    if (!sdiDataSet.isQCDataSet() || (actionCalc = qcBatchItem.getActionCalc()) == null || actionCalc.length() <= 0 || QCUtil.isQCBItemCalcRuleDefined(qcBatchId, qcBatchItemId, actionCalc, this.getQueryProcessor(), this.getConnectionProcessor())) continue;
                    sdi = sdiDataSet.getSDI();
                    actionprops = new HashMap();
                    if (actionCalcPropHolder.containsKey(actionCalc)) {
                        actionprops = (HashMap)actionCalcPropHolder.get(actionCalc);
                        propsQCBatchid = new StringBuffer((String)actionprops.get("qcbatchid"));
                        propsQCBatchid.append(";" + qcBatchId);
                        propsQCBatchItemid = new StringBuffer((String)actionprops.get("qcbatchitemid"));
                        propsQCBatchItemid.append(";" + qcBatchItemId);
                        propsSDCId = new StringBuffer((String)actionprops.get("sdcid"));
                        propsSDCId.append(";" + sdi.getSdcid());
                        propsKeyId1 = new StringBuffer((String)actionprops.get("keyid1"));
                        propsKeyId1.append(";" + sdi.getKeyid1());
                        propsKeyId2 = new StringBuffer((String)actionprops.get("keyid2"));
                        propsKeyId2.append(";" + sdi.getKeyid2());
                        propsKeyId3 = new StringBuffer((String)actionprops.get("keyid3"));
                        propsKeyId3.append(";" + sdi.getKeyid3());
                        propsPramListId = new StringBuffer((String)actionprops.get("paramlistid"));
                        propsPramListId.append(";" + sdiDataSet.getParamListID());
                        propsPramListVersionId = new StringBuffer((String)actionprops.get("paramlistversionid"));
                        propsPramListVersionId.append(";" + sdiDataSet.getParamListVersionID());
                        propsVariantId = new StringBuffer((String)actionprops.get("variantid"));
                        propsVariantId.append(";" + sdiDataSet.getVariantID());
                        propsDataSet = new StringBuffer((String)actionprops.get("dataset"));
                        propsDataSet.append(";" + sdiDataSet.getDataSet());
                        actionprops.clear();
                        actionprops.put("qcbatchid", propsQCBatchid.toString());
                        actionprops.put("qcbatchitemid", propsQCBatchItemid.toString());
                        actionprops.put("sdcid", propsSDCId.toString());
                        actionprops.put("keyid1", propsKeyId1.toString());
                        actionprops.put("keyid2", propsKeyId2.toString());
                        actionprops.put("keyid3", propsKeyId3.toString());
                        actionprops.put("paramlistid", propsPramListId.toString());
                        actionprops.put("paramlistversionid", propsPramListVersionId.toString());
                        actionprops.put("variantid", propsVariantId.toString());
                        actionprops.put("dataset", propsDataSet.toString());
                        actionprops.put("auditreason", auditreason == null ? "" : auditreason);
                        actionprops.put("auditactivity", auditactivity == null ? "" : auditactivity);
                        actionprops.put("auditsignedflag", auditsignedflag == null ? "N" : auditsignedflag);
                        actionprops.put("saveandrelease", saveAndRelease);
                        actionCalcPropHolder.put(actionCalc, actionprops);
                        continue;
                    }
                    actionprops.put("qcbatchid", qcBatchId);
                    actionprops.put("qcbatchitemid", qcBatchItemId);
                    actionprops.put("sdcid", sdi.getSdcid());
                    actionprops.put("keyid1", sdi.getKeyid1());
                    actionprops.put("keyid2", sdi.getKeyid2());
                    actionprops.put("keyid3", sdi.getKeyid3());
                    actionprops.put("paramlistid", sdiDataSet.getParamListID());
                    actionprops.put("paramlistversionid", sdiDataSet.getParamListVersionID());
                    actionprops.put("variantid", sdiDataSet.getVariantID());
                    actionprops.put("dataset", sdiDataSet.getDataSet());
                    actionprops.put("auditreason", auditreason == null ? "" : auditreason);
                    actionprops.put("auditactivity", auditactivity == null ? "" : auditactivity);
                    actionprops.put("auditsignedflag", auditsignedflag == null ? "" : auditsignedflag);
                    actionprops.put("saveandrelease", saveAndRelease);
                    actionCalcPropHolder.put(actionCalc, actionprops);
                }
                if (qcBatchType != null && qcBatchType.equalsIgnoreCase("2D")) continue;
                if (qcBatch.isDataEntered(true)) {
                    if (!evalOption.equals("Continuous") && !evalOption.equals("Conclusion")) continue;
                    sbEvalQCBatchIds.append(qcBatchId + ";");
                    continue;
                }
                if (!evalOption.equals("Continuous")) continue;
                sbEvalQCBatchIds.append(qcBatchId + ";");
            }
            try {
                actionprops = null;
                if (actionCalcPropHolder.containsKey("QCCalcBlankSubtract")) {
                    actionprops = (HashMap)actionCalcPropHolder.get("QCCalcBlankSubtract");
                    ap.processAction("QCCalcBlankSubtract", "1", actionprops);
                    actionCalcPropHolder.remove("QCCalcBlankSubtract");
                }
                if ((iter = (set = actionCalcPropHolder.keySet()).iterator()) == null) ** GOTO lbl219
                while (iter.hasNext()) {
                    calcActionName = (String)iter.next();
                    actionprops = (HashMap)actionCalcPropHolder.get(calcActionName);
                    ap.processAction(calcActionName, "1", actionprops);
                }
            }
            catch (ActionException e) {
                this.logger.error("Error running ProcessQCBatch", e);
            }
        }
lbl219:
        // 4 sources

        if (sbEvalQCBatchIds.length() > 0) {
            actionProps = new HashMap<String, String>();
            sbEvalQCBatchIds.setLength(sbEvalQCBatchIds.length() - 1);
            actionProps.put("qcbatchid", sbEvalQCBatchIds.toString());
            actionProps.put("success", "Y");
            if (sdcid != null && sdcid.equals("QCBatch")) {
                actionProps.put("evalqcbatchattributes", "Y");
            }
            try {
                editProps = new HashMap<String, String>();
                m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                editProps.put("dataentrydt", m18NUtil.format(Calendar.getInstance()));
                editProps.put("sdcid", "QCBatch");
                editProps.put("keyid1", sbEvalQCBatchIds.toString());
                ap.processAction("EditSDI", "1", editProps);
                ap.processAction("EvaluateQCBatch", "1", actionProps);
            }
            catch (ActionException e) {
                this.logger.error("Error running EvaluateQCBatch", e);
            }
        }
        for (k = 0; k < qcBatchReleaseHolder.size(); ++k) {
            qcbatch = (QCBatch)qcBatchReleaseHolder.get(k);
            instance = QCBatchPool.releaseQCBatch(qcbatch);
            this.logger.debug("[ProcessQCBatch] Released " + qcbatch.getQCBatchID() + " - " + instance);
        }
        return this.rc;
    }

    private int doRetestQCBatch(HashMap props) {
        String qcbatchid = (String)props.get("qcbatchid");
        String batchitemids = (String)props.get("qcbatchitemid");
        String createnewbatch = (String)props.get("createnewqcbatch");
        String[] itemids = null;
        TranslationProcessor tp = this.getTranslationProcessor();
        if (qcbatchid == null || qcbatchid.trim().length() == 0) {
            return this.setError(tp.translate("RetestQCBatch action failed .'QCBatchId' is required."));
        }
        if (batchitemids != null && batchitemids.length() > 0) {
            itemids = StringUtil.split(batchitemids, ";");
        }
        ActionProcessor ap = this.getActionProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        HashMap<String, String> actionprops = new HashMap<String, String>();
        StringBuffer sql = new StringBuffer();
        DataSet dsinsert = null;
        DataSet dsupdate = null;
        String newqcbatchid = "";
        HashMap<String, String> linktoids = new HashMap<String, String>();
        ArrayList<String> oldbatchitemids = new ArrayList<String>();
        ArrayList<String> newbatchitemids = new ArrayList<String>();
        HashMap<String, String> mapsampltypeids = new HashMap<String, String>();
        int usersequence = 0;
        DataSet ds = null;
        try {
            SafeSQL safeSQL = new SafeSQL();
            SDI sdi = new SDI("QCBatch", qcbatchid, null, null);
            sql.append("SELECT QCBATCHDESC,QCMETHODID,QCMETHODVERSIONID, QCBATCHSDCID, EVALOPTION, ACTIONSUCCESS,");
            sql.append(" ACTIONFAILURE, QCBATCHQUERYID, QCBATCHBASEDONID, QCBATCHQUERYCOUNT, MODDT, CREATEBY,");
            sql.append(" APPROVEONREVIEWFLAG, REVIEWONPASSFLAG,NOTES,CREATEWORKSHEETRULE,");
            sql.append(" TEMPLATEFLAG,PARAMLISTTYPE,QCBATCHTYPE FROM S_QCBATCH");
            sql.append(" WHERE S_QCBATCHID = " + safeSQL.addVar(qcbatchid));
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds == null || ds.size() == 0) {
                return this.setError(tp.translate("RetestQCBatch action failed. Invalid 'QCBatchId'."));
            }
            String qcbatchdesc = "";
            String qcmethodid = "";
            String qcmethodversionid = "";
            String qcbatchsdcid = "";
            String evaloption = "";
            String actionsuccess = "";
            String actionfailure = "";
            String qcbatchqueryid = "";
            String qcbatchbasedonid = "";
            String qcbatchquerycount = "";
            String approveonreviewflag = "";
            String reviewonpassflag = "";
            String notes = "";
            String paramlisttype = "";
            String qcbatchtype = "";
            String createworksheetrule = "";
            for (int k = 0; k < ds.size(); ++k) {
                qcbatchdesc = ds.getValue(k, "qcbatchdesc");
                qcmethodid = ds.getValue(k, "qcmethodid");
                qcmethodversionid = ds.getValue(k, "qcmethodversionid");
                qcbatchsdcid = ds.getValue(k, "qcbatchsdcid");
                evaloption = ds.getValue(k, "evaloption");
                actionsuccess = ds.getValue(k, "actionsuccess");
                actionfailure = ds.getValue(k, "actionfailure");
                qcbatchqueryid = ds.getValue(k, "qcbatchqueryid");
                qcbatchbasedonid = ds.getValue(k, "qcbatchbasedonid");
                qcbatchquerycount = ds.getValue(k, "qcbatchquerycount");
                approveonreviewflag = ds.getValue(k, "approveonreviewflag");
                reviewonpassflag = ds.getValue(k, "reviewonpassflag");
                notes = ds.getValue(k, "notes");
                paramlisttype = ds.getValue(k, "paramlisttype");
                qcbatchtype = ds.getValue(k, "qcbatchtype");
                createworksheetrule = ds.getValue(k, "createworksheetrule");
            }
            sql.setLength(0);
            if (itemids != null && itemids.length > 0) {
                for (int x = 0; x < itemids.length; ++x) {
                    sql.append(itemids[x] + "','");
                }
                sql.setLength(sql.length() - 3);
            }
            if (createnewbatch != null && createnewbatch.equalsIgnoreCase("Y")) {
                actionprops.put("sdcid", "QCBatch");
                actionprops.put("qcbatchdesc", qcbatchdesc);
                actionprops.put("qcmethodid", qcmethodid);
                actionprops.put("qcmethodversionid", qcmethodversionid);
                actionprops.put("qcbatchsdcid", qcbatchsdcid);
                actionprops.put("evaloption", evaloption);
                actionprops.put("actionsuccess", actionsuccess);
                actionprops.put("actionfailure", actionfailure);
                actionprops.put("qcbatchqueryid", qcbatchqueryid);
                actionprops.put("qcbatchbasedonid", qcbatchbasedonid);
                actionprops.put("qcbatchquerycount", qcbatchquerycount);
                actionprops.put("approveonreviewflag", approveonreviewflag);
                actionprops.put("reviewonpassflag", reviewonpassflag);
                actionprops.put("notes", notes);
                actionprops.put("paramlisttype", paramlisttype);
                actionprops.put("qcbatchtype", qcbatchtype);
                actionprops.put("createworksheetrule", createworksheetrule);
                ap.processAction("AddSDI", "1", actionprops);
                newqcbatchid = (String)actionprops.get("newkeyid1");
                safeSQL.reset();
                ds = qp.getPreparedSqlDataSet(Aqc.getQCBatchSampleTypeIds(sdi, safeSQL), safeSQL.getValues());
                StringBuffer newsampletypeids = new StringBuffer();
                StringBuffer evalstatus = new StringBuffer();
                StringBuffer speccondition = new StringBuffer();
                actionprops.clear();
                actionprops.put("sdcid", "QCBatchSampleType");
                String[] oldqcsampletypeids = new String[ds.size()];
                String[] newqcsampletypeids = new String[ds.size()];
                for (int i = 0; i < ds.size(); ++i) {
                    oldqcsampletypeids[i] = ds.getString(i, "S_QCBATCHSAMPLETYPEID");
                    actionprops.put("templateid", oldqcsampletypeids[i]);
                    actionprops.put("qcbatchid", newqcbatchid);
                    ap.processAction("addSDI", "1", actionprops);
                    newqcsampletypeids[i] = (String)actionprops.get("newkeyid1");
                    newsampletypeids.append(newqcsampletypeids[i] + ";");
                    evalstatus.append("(null);");
                    speccondition.append("(null);");
                }
                if (newsampletypeids.length() > 0) {
                    newsampletypeids.setLength(newsampletypeids.length() - 1);
                    evalstatus.setLength(evalstatus.length() - 1);
                    speccondition.setLength(speccondition.length() - 1);
                    actionprops.put("keyid1", newsampletypeids.toString());
                    actionprops.put("evalstatus", evalstatus.toString());
                    actionprops.put("speccondition", speccondition.toString());
                    actionprops.remove("templateid");
                    ap.processAction("EditSDI", "1", actionprops);
                }
                HashMap<String, String> getSdiDataSetprops = new HashMap<String, String>();
                StringBuffer editkeyid1s = new StringBuffer();
                StringBuffer editkeyid2s = new StringBuffer();
                StringBuffer editkeyid3s = new StringBuffer();
                StringBuffer editparamlistids = new StringBuffer();
                StringBuffer editparamlistversionids = new StringBuffer();
                StringBuffer editvariantids = new StringBuffer();
                StringBuffer editdatasets = new StringBuffer();
                StringBuffer editparamids = new StringBuffer();
                StringBuffer editparamtypes = new StringBuffer();
                StringBuffer replicateids = new StringBuffer();
                StringBuffer enteredtexts = new StringBuffer();
                for (int i = 0; i < newqcsampletypeids.length; ++i) {
                    mapsampltypeids.put(oldqcsampletypeids[i], newqcsampletypeids[i]);
                    sdi = new SDI("QCBatchSampleType", oldqcsampletypeids[i], null, null);
                    getSdiDataSetprops.clear();
                    getSdiDataSetprops.put("sdcid", "QCBatchSampleType");
                    getSdiDataSetprops.put("keyid1", oldqcsampletypeids[i]);
                    getSdiDataSetprops.put("keyid2", null);
                    getSdiDataSetprops.put("keyid3", null);
                    ap.processAction("GetSDIDataSet", "1", getSdiDataSetprops);
                    String sdiDataSetParamListIds = (String)getSdiDataSetprops.get("paramlistid");
                    if (sdiDataSetParamListIds != null && sdiDataSetParamListIds.length() > 0) {
                        String[] paramlistids = StringUtil.split((String)getSdiDataSetprops.get("paramlistid"), ";");
                        String[] paramlistversionids = StringUtil.split((String)getSdiDataSetprops.get("paramlistversionid"), ";");
                        String[] variantids = StringUtil.split((String)getSdiDataSetprops.get("variantid"), ";");
                        String[] datasets = StringUtil.split((String)getSdiDataSetprops.get("dataset"), ";");
                        for (int idx = 0; idx < paramlistids.length; ++idx) {
                            SDIDataSet samTypesdidataset = new SDIDataSet(sdi, paramlistids[idx], paramlistversionids[idx], variantids[idx], datasets[idx], this.getQueryProcessor());
                            List dataitemList = samTypesdidataset.getDataItems(null);
                            for (int k = 0; k < dataitemList.size(); ++k) {
                                DataItem dataItem = (DataItem)dataitemList.get(k);
                                editkeyid1s.append(newqcsampletypeids[i] + ";");
                                editkeyid2s.append("(null);");
                                editkeyid3s.append("(null);");
                                editparamlistids.append(paramlistids[idx] + ";");
                                editparamlistversionids.append(paramlistversionids[idx] + ";");
                                editvariantids.append(variantids[idx] + ";");
                                editdatasets.append(datasets[idx] + ";");
                                editparamids.append(dataItem.getParamID() + ";");
                                editparamtypes.append(dataItem.getParamType() + ";");
                                replicateids.append(dataItem.getReplicate() + ";");
                                enteredtexts.append(dataItem.getEnteredValue() + ";");
                            }
                        }
                    }
                    StringBuffer s_qcbatchsampletypeid = new StringBuffer();
                    StringBuffer s_qcbatchevalruleid = new StringBuffer();
                    StringBuffer s_qcbatchparamsetid = new StringBuffer();
                    StringBuffer batchparamevalstatus = new StringBuffer();
                    SDI newBSTypesdi = new SDI("QCBatchSampleType", newqcsampletypeids[i], null, null);
                    safeSQL.reset();
                    ds = qp.getPreparedSqlDataSet(Aqc.getQCBatchParamSets(newBSTypesdi, safeSQL), safeSQL.getValues());
                    for (int k = 0; k < ds.size(); ++k) {
                        s_qcbatchsampletypeid.append(newqcsampletypeids[i] + ";");
                        s_qcbatchparamsetid.append(ds.getValue(k, "S_QCBATCHPARAMSETID") + ";");
                        batchparamevalstatus.append(";");
                    }
                    if (s_qcbatchsampletypeid.length() > 0) {
                        dsupdate = null;
                        dsupdate = new DataSet();
                        s_qcbatchsampletypeid.setLength(s_qcbatchsampletypeid.length() - 1);
                        s_qcbatchparamsetid.setLength(s_qcbatchparamsetid.length() - 1);
                        batchparamevalstatus.setLength(batchparamevalstatus.length() - 1);
                        dsupdate.addColumnValues("s_qcbatchsampletypeid", 0, s_qcbatchsampletypeid.toString(), ";");
                        dsupdate.addColumnValues("s_qcbatchparamsetid", 0, s_qcbatchparamsetid.toString(), ";");
                        dsupdate.addColumnValues("evalstatus", 0, batchparamevalstatus.toString(), ";");
                        String[] keycols = new String[]{"s_qcbatchparamsetid", "s_qcbatchsampletypeid"};
                        DataSetUtil.update(this.database, dsupdate, "s_qcbatchparamset", keycols);
                    }
                    safeSQL.reset();
                    ds = qp.getPreparedSqlDataSet(Aqc.getQCBatchEvalRuleParams(newBSTypesdi, safeSQL), safeSQL.getValues());
                    s_qcbatchsampletypeid.setLength(0);
                    s_qcbatchevalruleid.setLength(0);
                    s_qcbatchparamsetid.setLength(0);
                    batchparamevalstatus.setLength(0);
                    for (int k = 0; k < ds.size(); ++k) {
                        s_qcbatchsampletypeid.append(newqcsampletypeids[i] + ";");
                        s_qcbatchevalruleid.append(ds.getValue(k, "S_QCBATCHEVALRULEID") + ";");
                        s_qcbatchparamsetid.append(ds.getValue(k, "S_QCBATCHPARAMSETID") + ";");
                        batchparamevalstatus.append(";");
                    }
                    if (s_qcbatchsampletypeid.length() <= 0) continue;
                    dsupdate = null;
                    dsupdate = new DataSet();
                    s_qcbatchsampletypeid.setLength(s_qcbatchsampletypeid.length() - 1);
                    s_qcbatchevalruleid.setLength(s_qcbatchevalruleid.length() - 1);
                    s_qcbatchparamsetid.setLength(s_qcbatchparamsetid.length() - 1);
                    batchparamevalstatus.setLength(batchparamevalstatus.length() - 1);
                    dsupdate.addColumnValues("s_qcbatchsampletypeid", 0, s_qcbatchsampletypeid.toString(), ";");
                    dsupdate.addColumnValues("s_qcbatchevalruleid", 0, s_qcbatchevalruleid.toString(), ";");
                    dsupdate.addColumnValues("s_qcbatchparamsetid", 0, s_qcbatchparamsetid.toString(), ";");
                    dsupdate.addColumnValues("evalstatus", 0, batchparamevalstatus.toString(), ";");
                    String[] keycols = new String[]{"s_qcbatchevalruleid", "s_qcbatchparamsetid", "s_qcbatchsampletypeid"};
                    DataSetUtil.update(this.database, dsupdate, "s_qcbatchevalruleparam", keycols);
                }
                if (editkeyid1s.length() > 0) {
                    editkeyid1s.setLength(editkeyid1s.length() - 1);
                    editkeyid2s.setLength(editkeyid2s.length() - 1);
                    editkeyid3s.setLength(editkeyid3s.length() - 1);
                    editparamlistids.setLength(editparamlistids.length() - 1);
                    editparamlistversionids.setLength(editparamlistversionids.length() - 1);
                    editvariantids.setLength(editvariantids.length() - 1);
                    editdatasets.setLength(editdatasets.length() - 1);
                    editparamids.setLength(editparamids.length() - 1);
                    editparamtypes.setLength(editparamtypes.length() - 1);
                    replicateids.setLength(replicateids.length() - 1);
                    enteredtexts.setLength(enteredtexts.length() - 1);
                    HashMap<String, String> enterDataItemprops = new HashMap<String, String>();
                    enterDataItemprops.put("sdcid", "QCBatchSampleType");
                    enterDataItemprops.put("keyid1", editkeyid1s.toString());
                    enterDataItemprops.put("keyid2", editkeyid2s.toString());
                    enterDataItemprops.put("keyid3", editkeyid3s.toString());
                    enterDataItemprops.put("paramlistid", editparamlistids.toString());
                    enterDataItemprops.put("paramlistversionid", editparamlistversionids.toString());
                    enterDataItemprops.put("variantid", editvariantids.toString());
                    enterDataItemprops.put("dataset", editdatasets.toString());
                    enterDataItemprops.put("paramid", editparamids.toString());
                    enterDataItemprops.put("paramtype", editparamtypes.toString());
                    enterDataItemprops.put("replicateid", replicateids.toString());
                    enterDataItemprops.put("enteredtext", enteredtexts.toString());
                    ap.processAction("EnterDataItem", "1", enterDataItemprops);
                }
                sdi = new SDI("QCBatch", qcbatchid, null, null);
                safeSQL.reset();
                ds = qp.getPreparedSqlDataSet(Aqc.getQCBatchItems(sdi, sql.toString(), safeSQL), safeSQL.getValues());
                if (ds == null || ds.size() == 0) {
                    return this.setError(tp.translate("RetestQCBatch Action failed. Available 'QCBatchItems' are invalid."));
                }
                dsinsert = null;
                dsinsert = new DataSet();
                String s_qcbatchitemid = "";
                String qcbatchsampletypeid = "";
                usersequence = 1;
                for (int k = 0; k < ds.size(); ++k) {
                    s_qcbatchitemid = OpalUtil.getNextSequence("s_qcbatchitem", this.getSequenceProcessor());
                    qcbatchsampletypeid = ds.getValue(k, "QCBATCHSAMPLETYPEID");
                    dsinsert.addColumnValues("s_qcbatchitemid", 0, s_qcbatchitemid, ";");
                    dsinsert.addColumnValues("s_qcbatchid", 0, newqcbatchid, ";");
                    dsinsert.addColumnValues("qcbatchitemdesc", 0, ds.getValue(k, "QCBATCHITEMDESC"), ";");
                    if (qcbatchsampletypeid != null && qcbatchsampletypeid.trim().length() > 0) {
                        dsinsert.addColumnValues("qcbatchsampletypeid", 0, (String)mapsampltypeids.get(qcbatchsampletypeid), ";");
                    } else {
                        dsinsert.addColumnValues("qcbatchsampletypeid", 0, null, ";");
                    }
                    dsinsert.addColumnValues("usersequence", 1, new Integer(usersequence).toString(), ";");
                    dsinsert.addColumnValues("batchitemtype", 0, ds.getValue(k, "BATCHITEMTYPE"), ";");
                    DataSetUtil.insert(this.database, dsinsert, "s_qcbatchitem");
                    dsinsert.clear();
                    ++usersequence;
                    linktoids.put(ds.getValue(k, "S_QCBATCHITEMID"), ds.getValue(k, "LINKTOQCBATCHITEMID") + ";" + ds.getValue(k, "LINKEDTO"));
                    oldbatchitemids.add(ds.getValue(k, "S_QCBATCHITEMID"));
                    newbatchitemids.add(s_qcbatchitemid);
                }
                Set s = linktoids.keySet();
                Iterator keyitr = s.iterator();
                String[] arrvalue = new String[2];
                dsupdate = null;
                dsupdate = new DataSet();
                StringBuffer linktoqcbatchitemid = new StringBuffer();
                StringBuffer linkedto = new StringBuffer();
                StringBuffer s_qcbatchid = new StringBuffer();
                StringBuffer qcbatchitemid = new StringBuffer();
                boolean linkupdation = false;
                while (keyitr.hasNext()) {
                    String batchitemid = (String)keyitr.next();
                    arrvalue = StringUtil.split((String)linktoids.get(batchitemid), ";");
                    String linkid = arrvalue[0];
                    String linkto = arrvalue[1];
                    if (linkid == null || linkid.length() <= 0) continue;
                    int linkidx = oldbatchitemids.indexOf(linkid);
                    int batchidx = oldbatchitemids.indexOf(batchitemid);
                    if (linkidx < 0) continue;
                    String newbatchlinkto = null;
                    String newitemid = null;
                    try {
                        newbatchlinkto = (String)newbatchitemids.get(linkidx);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        newbatchlinkto = null;
                    }
                    try {
                        newitemid = (String)newbatchitemids.get(batchidx);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        newitemid = null;
                    }
                    if (newbatchlinkto == null || newbatchlinkto.length() <= 0 || newitemid == null || newitemid.length() <= 0) continue;
                    linktoqcbatchitemid.append(newbatchlinkto + ";");
                    if (linkto != null && linkto.length() > 0) {
                        int diffidx;
                        linkto = linkto.trim().startsWith("+") || linkto.trim().startsWith("-") ? ((diffidx = linkidx - batchidx) > 0 ? "+" + new Integer(diffidx).toString() : new Integer(diffidx).toString()) : new Integer(linkidx + 1).toString();
                    }
                    linkedto.append(linkto + ";");
                    s_qcbatchid.append(newqcbatchid + ";");
                    qcbatchitemid.append(newitemid + ";");
                    linkupdation = true;
                }
                if (linkupdation) {
                    linktoqcbatchitemid.setLength(linktoqcbatchitemid.length() - 1);
                    linkedto.setLength(linkedto.length() - 1);
                    s_qcbatchid.setLength(s_qcbatchid.length() - 1);
                    qcbatchitemid.setLength(qcbatchitemid.length() - 1);
                    dsupdate.addColumnValues("linktoqcbatchitemid", 0, linktoqcbatchitemid.toString(), ";");
                    dsupdate.addColumnValues("linkedto", 0, linkedto.toString(), ";");
                    dsupdate.addColumnValues("s_qcbatchid", 0, s_qcbatchid.toString(), ";");
                    dsupdate.addColumnValues("s_qcbatchitemid", 0, qcbatchitemid.toString(), ";");
                    String[] keycols = new String[]{"s_qcbatchid", "s_qcbatchitemid"};
                    DataSetUtil.update(this.database, dsupdate, "s_qcbatchitem", keycols);
                }
                safeSQL.reset();
                DataSet dsQCBatchParamList = qp.getPreparedSqlDataSet("SELECT paramlistid, paramlistversionid, variantid FROM sdidata WHERE sdcid = 'QCBatch' AND keyid1 = " + safeSQL.addVar(qcbatchid), safeSQL.getValues());
                safeSQL.reset();
                DataSet dsQCBatchSpec = qp.getPreparedSqlDataSet("SELECT specid, specversionid FROM sdispec WHERE sdcid = 'QCBatch' AND keyid1 = " + safeSQL.addVar(qcbatchid), safeSQL.getValues());
                if (dsQCBatchParamList.getRowCount() > 0) {
                    PropertyList addDSProps = new PropertyList();
                    addDSProps.setProperty("sdcid", "QCBatch");
                    addDSProps.setProperty("keyid1", newqcbatchid);
                    addDSProps.setProperty("paramlistid", dsQCBatchParamList.getValue(0, "paramlistid"));
                    addDSProps.setProperty("paramlistversionid", dsQCBatchParamList.getValue(0, "paramlistversionid"));
                    addDSProps.setProperty("variantid", dsQCBatchParamList.getValue(0, "variantid"));
                    ap.processAction("AddDataSet", "1", addDSProps);
                }
                if (dsQCBatchSpec.getRowCount() > 0) {
                    PropertyList addSpecProps = new PropertyList();
                    addSpecProps.setProperty("sdcid", "QCBatch");
                    addSpecProps.setProperty("keyid1", newqcbatchid);
                    addSpecProps.setProperty("specid", dsQCBatchSpec.getValue(0, "specid"));
                    addSpecProps.setProperty("specversionid", dsQCBatchSpec.getValue(0, "specversionid"));
                    ap.processAction("AddSDISpec", "1", addSpecProps);
                }
            }
            String sdcid = "";
            String keyid1 = "";
            String keyid2 = "";
            String keyid3 = "";
            String paramlistid = "";
            String paramlistversionid = "";
            String variantid = "";
            String dataset = "";
            String qcBatchItemID = "";
            StringBuffer retestParamlistids = new StringBuffer();
            StringBuffer retestParamlistversionids = new StringBuffer();
            StringBuffer retestVariantids = new StringBuffer();
            StringBuffer retestDatasets = new StringBuffer();
            StringBuffer remeasureParamlistids = new StringBuffer();
            StringBuffer remeasureParamlistversionids = new StringBuffer();
            StringBuffer remeasureVariantids = new StringBuffer();
            StringBuffer remeasureDatasets = new StringBuffer();
            StringBuffer sbkeyid1 = new StringBuffer();
            StringBuffer sbkeyid2 = new StringBuffer();
            StringBuffer sbkeyid3 = new StringBuffer();
            StringBuffer sbparamlistid = new StringBuffer();
            StringBuffer sbparamlistversionid = new StringBuffer();
            StringBuffer sbvariantid = new StringBuffer();
            StringBuffer sbdataset = new StringBuffer();
            StringBuffer sbbatchid = new StringBuffer();
            StringBuffer sbbatchitemid = new StringBuffer();
            safeSQL.reset();
            ds = qp.getPreparedSqlDataSet(Aqc.getQCBatchSdidata(sdi, sql.toString(), safeSQL), safeSQL.getValues());
            ds.sort("S_QCBATCHITEMID");
            ArrayList<DataSet> grps = ds.getGroupedDataSets("S_QCBATCHITEMID");
            ArrayList<SDIDataSet> sdidatasetHolder = new ArrayList<SDIDataSet>();
            for (int g = 0; g < grps.size(); ++g) {
                DataSet dsBatchItem = grps.get(g);
                qcBatchItemID = dsBatchItem.getValue(0, "S_QCBATCHITEMID");
                for (int i = 0; i < dsBatchItem.size(); ++i) {
                    sdcid = dsBatchItem.getValue(i, "SDCID");
                    sdi = new SDI(sdcid, keyid1 = dsBatchItem.getValue(i, "KEYID1"), keyid2 = dsBatchItem.getValue(i, "KEYID2"), keyid3 = dsBatchItem.getValue(i, "KEYID3"));
                    SDIDataSet sdidataset = new SDIDataSet(sdi, paramlistid = dsBatchItem.getValue(i, "PARAMLISTID"), paramlistversionid = dsBatchItem.getValue(i, "PARAMLISTVERSIONID"), variantid = dsBatchItem.getValue(i, "VARIANTID"), dataset = dsBatchItem.getValue(i, "DATASET"), qp);
                    SDIWorkItem sdiworkitem = sdidataset.getParentWorkItem();
                    if (sdiworkitem != null) {
                        if (!sdidataset.isDataSetRetestable(sdi, paramlistid, paramlistversionid, variantid, dataset, this.database)) continue;
                        retestParamlistids.append(paramlistid + ";");
                        retestParamlistversionids.append(paramlistversionid + ";");
                        retestVariantids.append(variantid + ";");
                        retestDatasets.append(dataset + ";");
                        sdidatasetHolder.add(sdidataset);
                        continue;
                    }
                    remeasureParamlistids.append(paramlistid + ";");
                    remeasureParamlistversionids.append(paramlistversionid + ";");
                    remeasureVariantids.append(variantid + ";");
                    remeasureDatasets.append(dataset + ";");
                    sdidatasetHolder.add(sdidataset);
                }
                if (sdidatasetHolder.size() == 0) {
                    return this.setError("Validation Error", "VALIDATION", tp.translate("RetestQCBatch Action failed. Retesting not allowed for the datasets in the QCBatchItem") + ": " + qcBatchItemID);
                }
                if (retestParamlistids.length() > 0) {
                    retestParamlistids.setLength(retestParamlistids.length() - 1);
                    retestParamlistversionids.setLength(retestParamlistversionids.length() - 1);
                    retestVariantids.setLength(retestVariantids.length() - 1);
                    retestDatasets.setLength(retestDatasets.length() - 1);
                    actionprops.clear();
                    actionprops.put("sdcid", sdcid);
                    actionprops.put("keyid1", keyid1);
                    actionprops.put("keyid2", keyid2);
                    actionprops.put("keyid3", keyid3);
                    actionprops.put("paramlistid", retestParamlistids.toString());
                    actionprops.put("paramlistversionid", retestParamlistversionids.toString());
                    actionprops.put("variantid", retestVariantids.toString());
                    actionprops.put("dataset", retestDatasets.toString());
                    ap.processAction("RetestDataSet", "1", actionprops);
                    retestParamlistids.setLength(0);
                    retestParamlistversionids.setLength(0);
                    retestVariantids.setLength(0);
                    retestDatasets.setLength(0);
                }
                if (remeasureParamlistids.length() > 0) {
                    remeasureParamlistids.setLength(remeasureParamlistids.length() - 1);
                    remeasureParamlistversionids.setLength(remeasureParamlistversionids.length() - 1);
                    remeasureVariantids.setLength(remeasureVariantids.length() - 1);
                    remeasureDatasets.setLength(remeasureDatasets.length() - 1);
                    actionprops.clear();
                    actionprops.put("sdcid", sdcid);
                    actionprops.put("keyid1", keyid1);
                    actionprops.put("keyid2", keyid2);
                    actionprops.put("keyid3", keyid3);
                    actionprops.put("paramlistid", remeasureParamlistids.toString());
                    actionprops.put("paramlistversionid", remeasureParamlistversionids.toString());
                    actionprops.put("variantid", remeasureVariantids.toString());
                    actionprops.put("dataset", remeasureDatasets.toString());
                    actionprops.put("newdsstatus", "Initial");
                    ap.processAction("RemeasureDataSet", "1", actionprops);
                    remeasureParamlistids.setLength(0);
                    remeasureParamlistversionids.setLength(0);
                    remeasureVariantids.setLength(0);
                    remeasureDatasets.setLength(0);
                }
                if (sdidatasetHolder.size() > 0 && createnewbatch != null && createnewbatch.equalsIgnoreCase("Y")) {
                    HashMap<String, DataSet> instanceMap = new HashMap<String, DataSet>();
                    int index = oldbatchitemids.indexOf(qcBatchItemID);
                    for (int k = 0; k < sdidatasetHolder.size(); ++k) {
                        SDIDataSet sdidataset = (SDIDataSet)sdidatasetHolder.get(k);
                        String mapKey = sdi.getSdcid() + sdidataset.getSDI().getKeyid1() + sdidataset.getSDI().getKeyid2() + sdidataset.getSDI().getKeyid3() + sdidataset.getParamListID() + sdidataset.getVariantID();
                        DataSet dsMaxInstances = null;
                        if (instanceMap.containsKey(mapKey)) {
                            dsMaxInstances = (DataSet)instanceMap.get(mapKey);
                        } else {
                            safeSQL.reset();
                            dsMaxInstances = qp.getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid,variantid, dataset FROM sdidata WHERE sdcid = " + safeSQL.addVar(sdidataset.getSDI().getSdcid()) + " AND keyid1 = " + safeSQL.addVar(sdidataset.getSDI().getKeyid1()) + " AND keyid2 = " + safeSQL.addVar(sdidataset.getSDI().getKeyid2()) + " AND keyid3 = " + safeSQL.addVar(sdidataset.getSDI().getKeyid3()) + " AND paramlistid = " + safeSQL.addVar(sdidataset.getParamListID()) + " AND  variantid=" + safeSQL.addVar(sdidataset.getVariantID()) + " AND ( s_qcbatchid is null OR s_qcbatchid = '' ) ORDER BY dataset desc", safeSQL.getValues());
                            if (dsMaxInstances.getRowCount() > 0) {
                                instanceMap.put(mapKey, dsMaxInstances);
                            }
                        }
                        if (dsMaxInstances.getRowCount() <= 0) continue;
                        sbkeyid1.append(sdidataset.getSDI().getKeyid1() + ";");
                        sbkeyid2.append(sdidataset.getSDI().getKeyid2() + ";");
                        sbkeyid3.append(sdidataset.getSDI().getKeyid3() + ";");
                        sbparamlistid.append(sdidataset.getParamListID() + ";");
                        sbparamlistversionid.append(dsMaxInstances.getValue(0, "paramlistversionid") + ";");
                        sbvariantid.append(sdidataset.getVariantID() + ";");
                        sbbatchid.append(newqcbatchid + ";");
                        sbbatchitemid.append((String)newbatchitemids.get(index) + ";");
                        sbdataset.append(dsMaxInstances.getValue(0, "dataset") + ";");
                        dsMaxInstances.remove(0);
                        if (dsMaxInstances.size() != 0) continue;
                        instanceMap.remove(mapKey);
                    }
                }
                sdidatasetHolder.clear();
            }
            if (sbkeyid1.length() > 0) {
                sbkeyid1.setLength(sbkeyid1.length() - 1);
                sbkeyid2.setLength(sbkeyid2.length() - 1);
                sbkeyid3.setLength(sbkeyid3.length() - 1);
                sbparamlistid.setLength(sbparamlistid.length() - 1);
                sbparamlistversionid.setLength(sbparamlistversionid.length() - 1);
                sbvariantid.setLength(sbvariantid.length() - 1);
                sbdataset.setLength(sbdataset.length() - 1);
                sbbatchid.setLength(sbbatchid.length() - 1);
                sbbatchitemid.setLength(sbbatchitemid.length() - 1);
                actionprops.clear();
                actionprops.put("sdcid", sdcid);
                actionprops.put("keyid1", sbkeyid1.toString());
                actionprops.put("keyid2", sbkeyid2.toString());
                actionprops.put("keyid3", sbkeyid3.toString());
                actionprops.put("paramlistid", sbparamlistid.toString());
                actionprops.put("paramlistversionid", sbparamlistversionid.toString());
                actionprops.put("variantid", sbvariantid.toString());
                actionprops.put("dataset", sbdataset.toString());
                actionprops.put("s_qcbatchitemid", sbbatchitemid.toString());
                actionprops.put("s_qcbatchid", sbbatchid.toString());
                actionprops.put("propsmatch", "Y");
                ap.processAction("EditDataSet", "1", actionprops);
            }
        }
        catch (Exception e) {
            this.logger.error("doRetestQCBatch error", e);
        }
        if (createnewbatch != null && createnewbatch.equalsIgnoreCase("Y")) {
            actionprops.clear();
            actionprops.put("qcbatchid", newqcbatchid);
            try {
                ap.processAction("UpdateQCBatchStatus", "1", actionprops);
                PropertyList actionPropLst = new PropertyList();
                actionPropLst.setProperty("qcbatchid", newqcbatchid);
                ap.processActionClass(QCBatchReagentSync.class.getName(), actionPropLst);
            }
            catch (ActionException e) {
                this.logger.error("[RetestQCBatch] Could not update status of QC Batch. " + e.getMessage(), e);
            }
        }
        props.put("newqcbatchid", newqcbatchid);
        return this.rc;
    }

    private int doRunQCValidation(HashMap props) {
        String target = (String)props.get("target");
        String sd = (String)props.get("sd");
        String ucl = (String)props.get("ucl");
        String lcl = (String)props.get("lcl");
        String violationCount = (String)props.get("violationcount");
        String windowSize = (String)props.get("windowsize");
        String sigmaabovecl = (String)props.get("sigmaabovecl");
        String sigmabelowcl = (String)props.get("sigmabelowcl");
        String withinLimits = (String)props.get("withinlimits");
        String rulepattern = (String)props.get("rulepattern");
        String data = (String)props.get("data");
        props.put("result", "Fail");
        try {
            String[] arrDataPointsResult = StringUtil.split(data, ";");
            boolean success = QCEvalAction.runQCValidation(data, target, sd, ucl, lcl, violationCount, windowSize, sigmaabovecl, sigmabelowcl, rulepattern, withinLimits, arrDataPointsResult);
            if (success) {
                props.put("result", "Pass");
            }
        }
        catch (Exception e) {
            return this.setError(e.getMessage());
        }
        return this.rc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int doUpdateQCBatchStatus(HashMap props) {
        boolean updateDataEntryDt;
        List<Object> qcbatch;
        boolean reevaluate;
        String traceLogId;
        String auditSignedFlag;
        String auditActivity;
        String auditReason;
        String evaluateRunTime;
        block20: {
            String qcBatchID;
            block22: {
                TranslationProcessor tp;
                String keyid1;
                String sdcid;
                block21: {
                    qcBatchID = (String)props.get("qcbatchid");
                    sdcid = (String)props.get("sdcid");
                    keyid1 = (String)props.get("keyid1");
                    evaluateRunTime = (String)props.get("evaluate");
                    auditReason = (String)props.get("auditreason");
                    auditActivity = (String)props.get("auditactivity");
                    auditSignedFlag = (String)props.get("auditsignedflag");
                    traceLogId = (String)props.get("tracelogid");
                    reevaluate = false;
                    qcbatch = new ArrayList();
                    updateDataEntryDt = false;
                    tp = this.getTranslationProcessor();
                    String postDataEntry = (String)props.get("postdataentry");
                    if ("Y".equalsIgnoreCase(postDataEntry)) {
                        updateDataEntryDt = true;
                    }
                    if (sdcid == null || !sdcid.equals("QCBatch")) break block21;
                    String[] batchIds = StringUtil.split(keyid1, ";");
                    for (int i = 0; i < batchIds.length; ++i) {
                        qcbatch.add(batchIds[i]);
                    }
                    break block20;
                }
                if (qcBatchID != null && qcBatchID.length() != 0) break block22;
                if (sdcid == null || keyid1 == null) {
                    return this.setError(tp.translate("Invalid Action Input"));
                }
                DAMProcessor dam = this.getDAMProcessor();
                String rset_id = null;
                try {
                    rset_id = BaseSDIDataAction.createRSet(sdcid, keyid1, null, null, this.database, this.connectionInfo, false);
                    StringBuffer sql = new StringBuffer();
                    SafeSQL safeSQL = new SafeSQL();
                    sql.append("SELECT DISTINCT S_QCBATCHID FROM SDIDATA WHERE SDCID = ");
                    sql.append(safeSQL.addVar(sdcid));
                    sql.append(" AND KEYID1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ").append(safeSQL.addVar(rset_id)).append(")");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    dam.clearRSet(rset_id);
                    if (ds != null && ds.size() > 0) {
                        for (int i = 0; i < ds.size(); ++i) {
                            String id = ds.getValue(i, "S_QCBATCHID");
                            if (id == null || id.length() <= 0) continue;
                            qcbatch.add(id);
                        }
                    }
                    dam.clearRSet(rset_id);
                }
                catch (SapphireException e) {
                    try {
                        this.setError("Failed to get RSET ID . Exception: " + e.getMessage());
                        dam.clearRSet(rset_id);
                    }
                    catch (Throwable throwable) {
                        dam.clearRSet(rset_id);
                        throw throwable;
                    }
                    break block20;
                }
                break block20;
            }
            qcbatch = OpalUtil.toUniqueList(qcBatchID, ";");
        }
        try {
            if (evaluateRunTime == null || evaluateRunTime.equals("Y")) {
                reevaluate = true;
            }
            if ((traceLogId == null || traceLogId.length() == 0) && (auditActivity != null && auditActivity.length() > 0 || auditReason != null && auditReason.length() > 0)) {
                AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                StringBuffer batchIds = new StringBuffer();
                for (int i = 0; i < qcbatch.size(); ++i) {
                    batchIds.append(";").append((String)qcbatch.get(i));
                }
                if (batchIds.length() > 0) {
                    try {
                        if (auditReason != null) {
                            traceLogId = audit.addSDITraceLogEntry("QCBatch", batchIds.substring(1), "", "", auditReason, auditActivity, auditSignedFlag == null ? "N" : auditSignedFlag, "now", "Update QCBatch status", true);
                        }
                    }
                    catch (ServiceException e) {
                        throw new SapphireException(e);
                    }
                }
            }
            for (int i = 0; i < qcbatch.size(); ++i) {
                this.syncQCBatchStatus((String)qcbatch.get(i), reevaluate, updateDataEntryDt, traceLogId);
            }
        }
        catch (ActionException e) {
            return this.setError(e.getMessage(), e);
        }
        catch (SapphireException e) {
            return this.setError(e.getMessage(), e);
        }
        return this.rc;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean syncQCBatchStatus(String qcBatchId, boolean reevaluate, boolean updateDataEntryDt, String traceLogId) throws ActionException {
        String newBatchStatus = null;
        String currentBatchStatus = null;
        Object currentDate = null;
        HashMap<String, String> actionProps = null;
        QCBatch qcBatch = new QCBatch(this.getQueryProcessor());
        qcBatch.setQCBatchID(qcBatchId);
        currentBatchStatus = qcBatch.getStatus();
        String paramListType = qcBatch.getParamListType();
        if (paramListType == null) {
            paramListType = "";
        }
        this.logger.debug("[syncQCBatchStatus] Current Status for QCBatch " + qcBatchId + ": " + currentBatchStatus);
        if (currentBatchStatus == null || currentBatchStatus.trim().equals("")) {
            DataSet ds = qcBatch.getPrepationalDataSetsInfo();
            if (ds != null) {
                int prepDataSetCount = ds.size();
                int preppedDataSetCount = 0;
                for (int i = 0; i < ds.size(); ++i) {
                    String enteredtext = ds.getValue(i, "enteredtext");
                    if (enteredtext == null || enteredtext.length() <= 0 || enteredtext.equals("(null)")) continue;
                    ++preppedDataSetCount;
                }
                newBatchStatus = prepDataSetCount > 0 && paramListType.trim().equals("") ? (preppedDataSetCount == prepDataSetCount ? "Ready" : (preppedDataSetCount > 0 ? "Prepping" : "Initial")) : "Initial";
            }
        } else if (currentBatchStatus.equals("Prepping")) {
            if (qcBatch.isCompleted(reevaluate)) {
                newBatchStatus = "Completed";
            } else if (qcBatch.isDataEntered(reevaluate)) {
                newBatchStatus = "DataEntered";
            } else if (qcBatch.isInProgress(reevaluate)) {
                newBatchStatus = "InProgress";
            } else if (qcBatch.isPrepped(true)) {
                newBatchStatus = "Ready";
            }
        } else if (currentBatchStatus.equals("Reviewed")) {
            boolean dataEntered = qcBatch.isDataEntered(reevaluate);
            if (dataEntered) return true;
            newBatchStatus = "InProgress";
        } else if (currentBatchStatus.equals("InProgress") || currentBatchStatus.equals("DataEntered") || currentBatchStatus.equals("Completed") || currentBatchStatus.equals("Evaluated")) {
            boolean dataEntered = qcBatch.isDataEntered(reevaluate);
            boolean completed = qcBatch.isCompleted(reevaluate);
            if (currentBatchStatus.equals("Evaluated")) {
                newBatchStatus = dataEntered ? "Evaluated" : "InProgress";
            } else if (currentBatchStatus.equals("Completed")) {
                if (!completed) {
                    newBatchStatus = !dataEntered ? "InProgress" : "DataEntered";
                }
            } else if (currentBatchStatus.equals("DataEntered")) {
                if (completed) {
                    newBatchStatus = "Completed";
                } else if (!dataEntered) {
                    newBatchStatus = "InProgress";
                }
            } else if (completed) {
                newBatchStatus = "Completed";
            } else if (dataEntered) {
                newBatchStatus = "DataEntered";
            }
        } else if (currentBatchStatus.equals("Initial")) {
            DataSet ds = qcBatch.getPrepationalDataSetsInfo();
            if (ds != null && ds.size() > 0) {
                int prepDataSetCount = ds.size();
                int preppedDataSetCount = 0;
                for (int i = 0; i < ds.size(); ++i) {
                    String enteredtext = ds.getValue(i, "enteredtext");
                    if (enteredtext == null || enteredtext.length() <= 0 || enteredtext.equals("(null)")) continue;
                    ++preppedDataSetCount;
                }
                newBatchStatus = qcBatch.isCompleted(reevaluate) ? "Completed" : (qcBatch.isDataEntered(reevaluate) ? "DataEntered" : (qcBatch.isInProgress(reevaluate) ? "InProgress" : (preppedDataSetCount == prepDataSetCount ? "Ready" : (preppedDataSetCount > 0 ? "Prepping" : "Initial"))));
            } else if (qcBatch.isCompleted(reevaluate)) {
                newBatchStatus = "Completed";
            } else if (qcBatch.isDataEntered(reevaluate)) {
                newBatchStatus = "DataEntered";
            } else if (qcBatch.isInProgress(reevaluate)) {
                newBatchStatus = "InProgress";
            }
        } else if (currentBatchStatus.equals("Ready") || currentBatchStatus.equals("Started")) {
            if (qcBatch.isCompleted(reevaluate)) {
                newBatchStatus = "Completed";
            } else if (qcBatch.isDataEntered(reevaluate)) {
                newBatchStatus = "DataEntered";
            } else if (qcBatch.isInProgress(reevaluate)) {
                newBatchStatus = "InProgress";
            }
        }
        actionProps = new HashMap<String, String>();
        actionProps.put("keyid1", qcBatchId);
        actionProps.put("sdcid", "QCBatch");
        if (updateDataEntryDt) {
            M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            actionProps.put("dataentrydt", m18NUtil.format(Calendar.getInstance()));
        }
        if (newBatchStatus != null && !newBatchStatus.equals(currentBatchStatus)) {
            actionProps.put("qcbatchstatus", newBatchStatus);
            if (currentBatchStatus.equals("Reviewed") && newBatchStatus.equals("InProgress")) {
                actionProps.put("reviewdisposition", null);
            }
        }
        if (!updateDataEntryDt && (newBatchStatus == null || newBatchStatus.equals(currentBatchStatus))) return true;
        actionProps.put("tracelogid", traceLogId);
        this.getActionProcessor().processAction("EditSDI", "1", actionProps);
        return true;
    }

    private int doReapplyQCBatchPositioning(HashMap props) {
        String qcBatchid = (String)props.get("qcbatchid");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (qcBatchid == null || qcBatchid.trim().length() == 0) {
            this.setError("Confirm Error", "CONFIRM", tp.translate("ReapplyQCPosition action failed. 'QCBatchId' is required."));
        }
        QCBatch qcBatch = new QCBatch(this.getQueryProcessor());
        qcBatch.setQCBatchID(qcBatchid);
        String currentBatchStatus = qcBatch.getStatus();
        String batchSDCId = qcBatch.getQCBatchSDC();
        String blockFlag = qcBatch.getBlockFlag();
        if (currentBatchStatus != null && !currentBatchStatus.equals("Initial")) {
            this.setError("Validation Error", "VALIDATION", tp.translate("ReapplyQCPosition action requires the QCBatch to be in Initial state."));
        }
        if (blockFlag != null && blockFlag.equalsIgnoreCase("Y")) {
            this.setError("Validation Error", "VALIDATION", tp.translate("QC positioning rule cannot be reapplied. QCBatch is blocked for this action."));
        }
        ActionProcessor ap = this.getActionProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        HashMap<String, String> actionprops = new HashMap<String, String>();
        try {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT DISTINCT sd.sdcid, sd.keyid1, qcb.s_qcbatchitemid, qcb.qcbatchsampletypeid, qcb.usersequence, qcb.qcbatchitemdesc").append(" FROM SDIDATA sd, s_qcbatchitem qcb ").append(" WHERE sd.sdcid = " + safeSQL.addVar(batchSDCId) + " AND sd.s_qcbatchid = qcb.s_qcbatchid ").append(" AND sd.s_qcbatchitemid = qcb.s_qcbatchitemid AND qcb.s_qcbatchid = " + safeSQL.addVar(qcBatchid));
            DataSet allBitems = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            DataSet dsBST = new DataSet();
            DataSet dsNoBST = new DataSet();
            StringBuffer sampleToDelete = new StringBuffer();
            StringBuffer removeBIFromSDidata = new StringBuffer();
            if (allBitems != null && allBitems.getRowCount() > 0) {
                for (int i = 0; i < allBitems.size(); ++i) {
                    String batchSampleTypeId = allBitems.getString(i, "qcbatchsampletypeid", "");
                    if (batchSampleTypeId.trim().length() > 0) {
                        dsBST.copyRow(allBitems, i, 1);
                        continue;
                    }
                    dsNoBST.copyRow(allBitems, i, 1);
                }
                PreparedStatement checkExistenceInOtherBatch = this.database.prepareStatement("checkExistenceInOtherBatch", "select s_qcbatchid from sdidata  where sdcid = ? and keyid1 = ? and s_qcbatchid != ?");
                for (int i = 0; i < dsBST.getRowCount(); ++i) {
                    String sampleId = dsBST.getString(i, "keyid1", "");
                    String batchItemId = dsBST.getString(i, "s_qcbatchitemid", "");
                    checkExistenceInOtherBatch.setString(1, batchSDCId);
                    checkExistenceInOtherBatch.setString(2, sampleId);
                    checkExistenceInOtherBatch.setString(3, qcBatchid);
                    DataSet dsExist = new DataSet(checkExistenceInOtherBatch.executeQuery());
                    if (dsExist.getRowCount() > 0 || dsNoBST.findRow("keyid1", sampleId) > -1) {
                        removeBIFromSDidata.append(";").append(batchItemId);
                        continue;
                    }
                    sampleToDelete.append(";").append(sampleId);
                }
                this.database.closeStatement("checkExistenceInOtherBatch");
            }
            if (sampleToDelete.length() > 0) {
                PropertyList delSDIProps = new PropertyList();
                delSDIProps.setProperty("sdcid", batchSDCId);
                delSDIProps.setProperty("keyid1", sampleToDelete.substring(1));
                ap.processAction("DeleteSDI", "1", delSDIProps);
            }
            if (removeBIFromSDidata.length() > 0) {
                sql.setLength(0);
                safeSQL.reset();
                sql.append("UPDATE sdidata set s_qcbatchid = null,s_qcbatchitemid = null WHERE sdcid =" + safeSQL.addVar(batchSDCId) + " AND s_qcbatchid =" + safeSQL.addVar(qcBatchid) + " AND s_qcbatchitemid IN(" + safeSQL.addIn(removeBIFromSDidata.substring(1), ";") + ")");
                this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
            }
            if (dsBST.getRowCount() > 0) {
                sql.setLength(0);
                safeSQL.reset();
                sql.append("DELETE FROM s_qcbatchitem WHERE S_QCBATCHID = " + safeSQL.addVar(qcBatchid) + " AND S_QCBATCHITEMID  IN ( " + safeSQL.addIn(dsBST.getColumnValues("s_qcbatchitemid", "','")) + ")");
                this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
            }
            if (dsNoBST.getRowCount() > 0) {
                dsNoBST.sort("Usersequence");
                int usersequence = 1;
                DataSet dsupdate = new DataSet();
                StringBuffer useq = new StringBuffer();
                StringBuffer s_qcbatchid = new StringBuffer();
                StringBuffer s_qcbatchitemid = new StringBuffer();
                StringBuffer qcbatchitemdesc = new StringBuffer();
                for (int i = 0; i < dsNoBST.getRowCount(); ++i) {
                    s_qcbatchid.append(qcBatchid + ";");
                    s_qcbatchitemid.append(dsNoBST.getValue(i, "S_QCBATCHITEMID") + ";");
                    qcbatchitemdesc.append(StringUtil.replaceAll(dsNoBST.getValue(i, "QCBATCHITEMDESC"), ";", "#semicolon#") + ";");
                    useq.append(usersequence + ";");
                    ++usersequence;
                }
                if (useq.length() > 0) {
                    useq.setLength(useq.length() - 1);
                    s_qcbatchid.setLength(s_qcbatchid.length() - 1);
                    s_qcbatchitemid.setLength(s_qcbatchitemid.length() - 1);
                    qcbatchitemdesc.setLength(qcbatchitemdesc.length() - 1);
                    dsupdate.addColumnValues("s_qcbatchid", 0, s_qcbatchid.toString(), ";");
                    dsupdate.addColumnValues("s_qcbatchitemid", 0, s_qcbatchitemid.toString(), ";");
                    dsupdate.addColumnValues("qcbatchitemdesc", 0, qcbatchitemdesc.toString(), ";");
                    dsupdate.addColumnValues("usersequence", 0, useq.toString(), ";");
                    String[] keycols = new String[]{"s_qcbatchid", "s_qcbatchitemid"};
                    DataSetUtil.update(this.database, dsupdate, "s_qcbatchitem", keycols);
                }
            }
            actionprops.clear();
            actionprops.put("qcbatchid", qcBatchid);
            ap.processAction("ApplyQCMethod", "1", actionprops);
        }
        catch (Exception ex) {
            this.logger.error("doReApplyBatchPositioning error", ex);
        }
        return this.rc;
    }

    private int doQCUpdateLimit(HashMap props) {
        String qcbatchsampletypeid = (String)props.get("qcbatchsampletypeid");
        String sdvalues = (String)props.get("sd");
        String targetvalues = (String)props.get("targetvalues");
        String batchparamsetids = (String)props.get("qcbatchparamsetids");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (qcbatchsampletypeid == null || qcbatchsampletypeid.length() == 0) {
            return this.setError(tp.translate("Action failed : QCBatchSample Type Id is required."));
        }
        if (sdvalues == null || sdvalues.length() == 0 || targetvalues == null || targetvalues.length() == 0 || batchparamsetids == null || batchparamsetids.length() == 0) {
            return this.setError(tp.translate("Action failed : Required inputs missing ."));
        }
        String[] paramsetids = StringUtil.split(batchparamsetids, ";");
        String[] arrsds = StringUtil.split(sdvalues, ";");
        String[] arrtargetvalues = StringUtil.split(targetvalues, ";");
        if (paramsetids.length != arrsds.length || paramsetids.length != arrtargetvalues.length) {
            return this.setError(tp.translate("Action failed : Required inputs not matching ."));
        }
        QueryProcessor qp = this.getQueryProcessor();
        StringBuffer sbMethodSampleTypeLimitIds = new StringBuffer();
        StringBuffer sbMethodSampleTypeIds = new StringBuffer();
        StringBuffer sbSDs = new StringBuffer();
        StringBuffer sbTargetValues = new StringBuffer();
        try {
            DataSet dsUpdate = new DataSet();
            StringBuffer sbBatchSampleTypeIds = new StringBuffer();
            for (int i = 0; i < paramsetids.length; ++i) {
                sbBatchSampleTypeIds.append(qcbatchsampletypeid).append(";");
            }
            if (sbBatchSampleTypeIds.length() > 0) {
                sbBatchSampleTypeIds.setLength(sbBatchSampleTypeIds.length() - 1);
            }
            String[] keyCols = new String[]{"s_qcbatchsampletypeid", "s_qcbatchparamsetid"};
            dsUpdate.addColumnValues("s_qcbatchsampletypeid", 0, sbBatchSampleTypeIds.toString(), ";");
            dsUpdate.addColumnValues("s_qcbatchparamsetid", 0, batchparamsetids, ";");
            dsUpdate.addColumnValues("sd", 0, sdvalues, ";");
            dsUpdate.addColumnValues("targetvalue", 0, targetvalues, ";");
            DataSetUtil.update(this.database, dsUpdate, "s_qcbatchparamset", keyCols);
            dsUpdate = null;
            SafeSQL safeSQL = new SafeSQL();
            DataSet dsQcMethodSampleTypeLimit = qp.getPreparedSqlDataSet(Aqc.getQCMethodSampleTypeLimitId(qcbatchsampletypeid, safeSQL), safeSQL.getValues());
            if (dsQcMethodSampleTypeLimit != null && dsQcMethodSampleTypeLimit.size() > 0) {
                for (int k = 0; k < dsQcMethodSampleTypeLimit.size(); ++k) {
                    sbMethodSampleTypeIds.append(dsQcMethodSampleTypeLimit.getValue(k, "s_qcmethodsampletypeid") + ";");
                    sbMethodSampleTypeLimitIds.append(dsQcMethodSampleTypeLimit.getValue(k, "s_qcmethodsampletypelimitid") + ";");
                    sbSDs.append(dsQcMethodSampleTypeLimit.getValue(k, "sd") + ";");
                    sbTargetValues.append(dsQcMethodSampleTypeLimit.getValue(k, "targetvalue") + ";");
                }
                if (sbMethodSampleTypeIds.length() > 0) {
                    dsUpdate = new DataSet();
                    sbMethodSampleTypeIds.setLength(sbMethodSampleTypeIds.length() - 1);
                    sbMethodSampleTypeLimitIds.setLength(sbMethodSampleTypeLimitIds.length() - 1);
                    sbSDs.setLength(sbSDs.length() - 1);
                    sbTargetValues.setLength(sbTargetValues.length() - 1);
                    dsUpdate.addColumnValues("s_qcmethodsampletypeid", 0, sbMethodSampleTypeIds.toString(), ";");
                    dsUpdate.addColumnValues("s_qcmethodsampletypelimitid", 0, sbMethodSampleTypeLimitIds.toString(), ";");
                    dsUpdate.addColumnValues("sd", 0, sbSDs.toString(), ";");
                    dsUpdate.addColumnValues("targetvalue", 0, sbTargetValues.toString(), ";");
                    String[] methodSampleTypeLimitKeyCols = new String[]{"s_qcmethodsampletypeid", "s_qcmethodsampletypelimitid"};
                    try {
                        DataSetUtil.update(this.database, dsUpdate, "s_qcmethodsampletypelimit", methodSampleTypeLimitKeyCols);
                    }
                    catch (Exception e) {
                        this.setError("Confirmation Error", "CONFIRM", tp.translate("Could not update standard deviation values to the QC method."));
                    }
                }
            } else {
                this.setError("Validation Error", "VALIDATION", tp.translate("Could not update standard deviation values to the QC method. Parameters are not added for the corresponding QC method sample type."));
            }
        }
        catch (Exception ex) {
            return this.setError(tp.translate("Could not save changes to the database."), ex);
        }
        return this.rc;
    }

    private void processBatchWithChilds(String parentBatchName, String qcBatchId, String sdcId, String qcBatchSampleTypeId, HashMap batchsPrecedence, HashMap batchsWRTSampleTypes, HashMap sampleDetailsHolder, HashMap qcApplyMappings, ArrayList processedBatches, ActionProcessor aps, DataSet dsApplyAction) throws SapphireException {
        if (!processedBatches.contains(parentBatchName)) {
            this.processBatch(parentBatchName, qcBatchId, sdcId, qcBatchSampleTypeId, batchsWRTSampleTypes, sampleDetailsHolder, qcApplyMappings, dsApplyAction);
            processedBatches.add(parentBatchName);
        }
        batchsWRTSampleTypes.remove(parentBatchName);
        String childBatch = this.findChildBatch(parentBatchName, batchsPrecedence);
        if (childBatch != null) {
            this.processBatchWithChilds(childBatch, qcBatchId, sdcId, qcBatchSampleTypeId, batchsPrecedence, batchsWRTSampleTypes, sampleDetailsHolder, qcApplyMappings, processedBatches, aps, dsApplyAction);
        }
    }

    private String findChildBatch(String parentBatchName, HashMap batchsPrecedence) {
        for (String childBatchName : batchsPrecedence.keySet()) {
            if (!((String)batchsPrecedence.get(childBatchName)).equals(parentBatchName)) continue;
            return childBatchName;
        }
        return null;
    }

    private boolean isAnyCircularLink(String qcBatchItemId, ArrayList parentTrail, HashMap linkedToIdentification) {
        if (linkedToIdentification.containsKey(qcBatchItemId)) {
            String linkedQCBatchItemId = (String)linkedToIdentification.get(qcBatchItemId);
            if (parentTrail.contains(linkedQCBatchItemId)) {
                return true;
            }
            parentTrail.add(qcBatchItemId);
            return this.isAnyCircularLink(linkedQCBatchItemId, parentTrail, linkedToIdentification);
        }
        return false;
    }

    private void createBatch(String batchName, String qcBatchItemId, String qcBatchSampleTypeId, HashMap batches, String linkedToBatchName, HashMap batchSampleTypeMapping, HashMap sampleTypeBatchMapping, HashMap batchIdentification, HashMap batchsPrecedence) {
        ArrayList<String> items = new ArrayList<String>();
        items.add(0, qcBatchItemId);
        batches.put(batchName, items);
        if (sampleTypeBatchMapping.containsKey(qcBatchSampleTypeId)) {
            String batchNames = (String)sampleTypeBatchMapping.get(qcBatchSampleTypeId);
            batchNames = batchNames + ";" + batchName;
            sampleTypeBatchMapping.put(qcBatchSampleTypeId, batchNames);
        } else {
            sampleTypeBatchMapping.put(qcBatchSampleTypeId, batchName);
        }
        batchSampleTypeMapping.put(batchName, qcBatchSampleTypeId);
        batchIdentification.put(qcBatchItemId, batchName);
        if (linkedToBatchName != null) {
            batchsPrecedence.put(batchName, linkedToBatchName);
        }
    }

    private int checkAndAddOrUpdateBatch(int batchNumber, String qcBatchItemId, String qcBatchSampleTypeId, HashMap batches, HashMap batchSampleTypeMapping, HashMap sampleTypeBatchMapping, HashMap batchIdentification, HashMap batchsPrecedence) {
        if (sampleTypeBatchMapping.containsKey(qcBatchSampleTypeId)) {
            String batchNames = (String)sampleTypeBatchMapping.get(qcBatchSampleTypeId);
            String[] batchNameArr = StringUtil.split(batchNames, ";");
            this.updateBatch(batchNameArr[0], qcBatchItemId, null, batches, batchIdentification, batchsPrecedence);
        } else {
            String batchName = "Batch" + batchNumber;
            ++batchNumber;
            this.createBatch(batchName, qcBatchItemId, qcBatchSampleTypeId, batches, null, batchSampleTypeMapping, sampleTypeBatchMapping, batchIdentification, batchsPrecedence);
        }
        return batchNumber;
    }

    private void updateBatch(String batchName, String qcBatchItemId, String linkedToBatchName, HashMap batches, HashMap batchIdentification, HashMap batchsPrecedence) {
        ArrayList items = (ArrayList)batches.get(batchName);
        items.add(qcBatchItemId);
        batchIdentification.put(qcBatchItemId, batchName);
        if (linkedToBatchName != null) {
            batchsPrecedence.put(batchName, linkedToBatchName);
        }
    }

    private void processBatch(String batchName, String qcBatchId, String sdcId, String qcBatchSampleTypeid, HashMap batchsWRTSampleTypes, HashMap sampleDetailsHolder, HashMap qcApplyMappings, DataSet dsApplyAction) throws SapphireException {
        ArrayList batchItems = (ArrayList)batchsWRTSampleTypes.get(batchName);
        String applyAction = (String)qcApplyMappings.get(qcBatchSampleTypeid);
        DataSet dsupdate = new DataSet();
        dsupdate.addColumn("linktoqcbatchitemid", 0);
        dsupdate.addColumn("linkedto", 0);
        dsupdate.addColumn("s_qcbatchid", 0);
        dsupdate.addColumn("s_qcbatchitemid", 0);
        for (int n = 0; n < batchItems.size(); ++n) {
            HashMap sampleDetails = (HashMap)sampleDetailsHolder.get((String)batchItems.get(n));
            Object objNewSampleFlag = sampleDetails.get("newsampleflag");
            if (objNewSampleFlag == null) {
                if (sampleDetails.get("linktoqcbatchitemid") == null) continue;
                String linkedBatchItemId = (String)sampleDetails.get("linktoqcbatchitemid");
                int rowidx = dsupdate.addRow();
                dsupdate.setValue(rowidx, "linktoqcbatchitemid", linkedBatchItemId);
                dsupdate.setValue(rowidx, "linkedto", (String)sampleDetails.get("linkedto"));
                dsupdate.setValue(rowidx, "s_qcbatchid", qcBatchId);
                dsupdate.setValue(rowidx, "s_qcbatchitemid", (String)sampleDetails.get("qcbatchitemid"));
                continue;
            }
            int row = dsApplyAction.addRow();
            dsApplyAction.setString(row, "keyid1", (String)sampleDetails.get("keyid1"));
            dsApplyAction.setString(row, "keyid2", (String)sampleDetails.get("keyid2"));
            dsApplyAction.setString(row, "keyid3", (String)sampleDetails.get("keyid3"));
            dsApplyAction.setString(row, "qcbatchitemid", (String)sampleDetails.get("qcbatchitemid"));
            dsApplyAction.setString(row, "qcbatchsampletypeid", qcBatchSampleTypeid);
            dsApplyAction.setString(row, "applyaction", applyAction);
            if (sampleDetails.get("linktoqcbatchitemid") != null) {
                dsApplyAction.setString(row, "linktoqcbatchitemid", (String)sampleDetails.get("linktoqcbatchitemid"));
                dsApplyAction.setString(row, "linkedto", (String)sampleDetails.get("linkedto"));
                continue;
            }
            dsApplyAction.setString(row, "linktoqcbatchitemid", "");
            dsApplyAction.setString(row, "linkedto", "");
        }
        if (dsupdate.size() > 0) {
            String[] keycols = new String[]{"s_qcbatchid", "s_qcbatchitemid"};
            DataSetUtil.update(this.database, dsupdate, "s_qcbatchitem", keycols);
        }
    }

    private boolean checkPrecedence(HashMap batchsPrecedence, String batchName, String checkBatchName) {
        String leaderBatchName = (String)batchsPrecedence.get(batchName);
        if (leaderBatchName.equals(checkBatchName)) {
            return true;
        }
        if (batchsPrecedence.containsKey(leaderBatchName)) {
            return this.checkPrecedence(batchsPrecedence, leaderBatchName, checkBatchName);
        }
        return false;
    }

    private int validateLinkedToValue(String tempLinkTo, String tempLinkedTo, boolean guiMode, int n) {
        int tempLoc = 0;
        try {
            if (!guiMode) {
                if (tempLinkedTo != null && (tempLinkedTo = tempLinkedTo.trim()).length() > 0) {
                    String calculateLinkedTo = tempLinkedTo;
                    if (tempLinkedTo.charAt(0) == '+') {
                        calculateLinkedTo = tempLinkedTo.substring(1, tempLinkedTo.length());
                    }
                    tempLoc = n + 1 + Integer.valueOf(calculateLinkedTo);
                }
            } else if (tempLinkTo != null && !tempLinkTo.trim().equals("")) {
                tempLoc = Integer.parseInt(tempLinkTo);
            }
        }
        catch (NumberFormatException nfe) {
            System.err.println("Error while parsing the linkto field" + nfe.getMessage());
            return 0;
        }
        return tempLoc;
    }

    private void copyFormRuleToBatch(String newQCBatchId, DataSet sdiFormRules) throws SapphireException {
        Calendar now = Calendar.getInstance();
        sdiFormRules.setString(-1, "sdcid", "QCBatch");
        sdiFormRules.setString(-1, "keyid1", newQCBatchId);
        sdiFormRules.setString(-1, "keyid2", "(null)");
        sdiFormRules.setString(-1, "keyid3", "(null)");
        sdiFormRules.setString(-1, "createtool", this.connectionInfo.getTool());
        sdiFormRules.setString(-1, "createby", this.connectionInfo.getSysuserId());
        sdiFormRules.setDate(-1, "createdt", now);
        sdiFormRules.setString(-1, "modtool", this.connectionInfo.getTool());
        sdiFormRules.setString(-1, "modby", this.connectionInfo.getSysuserId());
        sdiFormRules.setDate(-1, "moddt", now);
        DataSetUtil.insert(this.database, sdiFormRules, "sdiformrule");
    }
}

