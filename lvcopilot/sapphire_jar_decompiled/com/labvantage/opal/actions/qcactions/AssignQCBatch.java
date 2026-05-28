/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import java.sql.PreparedStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AssignQCBatch
extends BaseAction
implements sapphire.action.AssignQCBatch {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53295 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        ActionProcessor ap = this.getActionProcessor();
        String createWSRule = "";
        String qcBatchIds = properties.getProperty("qcbatchid", "");
        if (qcBatchIds.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("QCBatch Id not specified in the input."));
        }
        String assignTo = properties.getProperty("assignto", "");
        String assignToDepartment = properties.getProperty("assigntodepartment", "");
        if (assignTo.length() == 0 && assignToDepartment.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("Either Analyst Id or department must be specified in the input."));
        }
        String formId = properties.getProperty("formid", "");
        String formVersionId = properties.getProperty("formversionid", "");
        String createWorkSheet = properties.getProperty("createworksheet", "Y");
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = qp.getPreparedSqlDataSet("SELECT s_qcbatchid, createworksheetrule, documentid FROM s_qcbatch WHERE s_qcbatchid IN ( " + safeSQL.addIn(qcBatchIds.replaceAll(";", "','")) + ")", safeSQL.getValues());
        if (ds == null || ds.getRowCount() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "QCBatch Id(s) specified in the input are not valid");
        }
        PreparedStatement psmt = this.database.prepareStatement("SELECT formid, formversionid FROM sdiformrule  WHERE sdcid = 'QCBatch' AND keyid1 = ? AND formrule = 'default'");
        if ("Y".equalsIgnoreCase(createWorkSheet)) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                createWSRule = ds.getString(i, "createworksheetrule", "");
                if (!"On Assignment".equals(createWSRule) || ds.getString(i, "documentid", "").length() != 0) continue;
                String qcBatchId = ds.getString(i, "s_qcbatchid");
                String dsFormId = "";
                String dsFormVersionId = "";
                if (formId.length() == 0) {
                    try {
                        psmt.setString(1, qcBatchId);
                        DataSet dsDefaultForm = new DataSet(psmt.executeQuery());
                        if (dsDefaultForm != null && dsDefaultForm.getRowCount() > 0) {
                            dsFormId = dsDefaultForm.getString(0, "formid", "");
                            dsFormVersionId = dsDefaultForm.getString(0, "formversionid", "");
                        }
                    }
                    catch (Exception sq) {
                        throw new SapphireException(sq);
                    }
                }
                if (formId.length() == 0 && dsFormId.length() == 0) {
                    Logger.logInfo("AssignQCBatch No default/supplied Form found for the QC Batch " + qcBatchId);
                    continue;
                }
                PropertyList createWSProps = new PropertyList();
                if (formId.length() > 0) {
                    createWSProps.setProperty("formid", formId);
                    createWSProps.setProperty("formversionid", formVersionId);
                } else {
                    createWSProps.setProperty("formid", dsFormId);
                    createWSProps.setProperty("formversionid", dsFormVersionId);
                }
                createWSProps.setProperty("qcbatchid", qcBatchId);
                if (assignTo.length() > 0) {
                    createWSProps.setProperty("assignto", assignTo);
                }
                if (assignToDepartment.length() > 0) {
                    createWSProps.setProperty("assigntodepartment", assignToDepartment);
                }
                ap.processAction("CreateWorksheet", "1", createWSProps);
            }
        }
        PropertyList editSDIProps = new PropertyList();
        editSDIProps.setProperty("sdcid", "QCBatch");
        editSDIProps.setProperty("keyid1", qcBatchIds);
        if (assignTo.length() > 0) {
            editSDIProps.setProperty("assignedanalyst", assignTo);
        }
        if (assignToDepartment.length() > 0) {
            editSDIProps.setProperty("assigneddepartment", assignToDepartment);
        }
        ap.processAction("EditSDI", "1", editSDIProps);
        safeSQL.reset();
        DataSet batchds = qp.getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset FROM sdidata WHERE s_qcbatchid IN ( " + safeSQL.addIn(qcBatchIds.replaceAll(";", "','")) + ")", safeSQL.getValues());
        HashMap<String, String> editDataSetProps = new HashMap<String, String>();
        editDataSetProps.put("propsmatch", "Y");
        editDataSetProps.put("sdcid", batchds.getValue(0, "sdcid"));
        editDataSetProps.put("keyid1", batchds.getColumnValues("keyid1", ";"));
        editDataSetProps.put("keyid2", batchds.getColumnValues("keyid2", ";"));
        editDataSetProps.put("keyid3", batchds.getColumnValues("keyid3", ";"));
        editDataSetProps.put("paramlistid", batchds.getColumnValues("paramlistid", ";"));
        editDataSetProps.put("paramlistversionid", batchds.getColumnValues("paramlistversionid", ";"));
        editDataSetProps.put("variantid", batchds.getColumnValues("variantid", ";"));
        editDataSetProps.put("dataset", batchds.getColumnValues("dataset", ";"));
        editDataSetProps.put("createworksheet", "N");
        if (assignTo.length() > 0) {
            editDataSetProps.put("assignto", assignTo);
        }
        if (assignToDepartment.length() > 0) {
            editDataSetProps.put("s_assigneddepartment", assignToDepartment);
        }
        this.getActionProcessor().processAction("EditDataSet", "1", editDataSetProps);
    }
}

