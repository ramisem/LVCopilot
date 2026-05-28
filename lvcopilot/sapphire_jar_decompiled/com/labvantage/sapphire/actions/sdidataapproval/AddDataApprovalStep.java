/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidataapproval;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.AddSDITraceLog;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddDataApprovalStep
extends BaseAction
implements sapphire.action.AddDataApprovalStep {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    public static final String ID = "AddDataApprovalStep";
    public static final String VERSIONID = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "SDC not specified.");
        }
        String[] keyid1Arr = StringUtil.split(properties.getProperty("keyid1"), ";");
        if (keyid1Arr.length == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Key Ids not specified.");
        }
        String[] keyid2Arr = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3Arr = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] paramListIdArr = StringUtil.split(properties.getProperty("paramlistid"), ";");
        String[] paramListVersionIdArr = StringUtil.split(properties.getProperty("paramlistversionid"), ";");
        String[] variantIdArr = StringUtil.split(properties.getProperty("variantid"), ";");
        String[] dataSetArr = StringUtil.split(properties.getProperty("dataset"), ";");
        String[] approvalTypeIds = StringUtil.split(properties.getProperty("approvaltypeid", ""), ";");
        String reason = properties.getProperty("auditreason");
        String activity = properties.getProperty("auditactivity");
        String signedFlag = properties.getProperty("auditsignedflag");
        String auditdt = properties.getProperty("auditdt");
        String tracelogid = properties.getProperty("tracelogid", "").trim();
        String[] approvalSteps = StringUtil.split(properties.getProperty("approvalstep", ""), ";");
        String[] roleIds = StringUtil.split(properties.getProperty("roleid", ""), ";");
        String[] mandatoryFlags = StringUtil.split(properties.getProperty("mandatoryflag", ""), ";");
        String[] forcePeerFlags = StringUtil.split(properties.getProperty("forcepeerflag", ""), ";");
        if (approvalTypeIds.length > 1) {
            throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Only one approval type id is allowed to be passed."));
        }
        String approvalType = approvalTypeIds[0];
        StringBuilder sql = null;
        for (int i = 0; i < approvalSteps.length; ++i) {
            String approvalStep = approvalSteps[i];
            if (approvalStep != null && !approvalStep.isEmpty()) {
                sql = new StringBuilder();
                sql.append("SELECT COUNT(*) FROM APPROVALTYPESTEP WHERE APPROVALSTEP='" + approvalStep + "' AND APPROVALTYPEID='" + approvalType + "'");
                int isApprovalStepPresent = this.getQueryProcessor().getCount(sql.toString());
                if (isApprovalStepPresent != 0) continue;
                HashMap<String, String> token = new HashMap<String, String>();
                token.put("[step]", approvalStep);
                token.put("[approval_type]", approvalType);
                throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Approval step: [step] is not present for [approval_type]", token));
            }
            throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Approval step can't be blank"));
        }
        for (int x = 0; x < keyid1Arr.length; ++x) {
            String keyId1 = keyid1Arr[x];
            if (keyId1 == null || keyId1.trim().isEmpty()) {
                throw new SapphireException("INVALID_PROPERTY", "Keyid1 not specified.");
            }
            String keyId2 = keyid2Arr.length == 0 || keyid2Arr.length < keyid1Arr.length || keyid2Arr[x].length() == 0 ? "(null)" : keyid2Arr[x];
            String keyId3 = keyid3Arr.length == 0 || keyid3Arr.length < keyid1Arr.length || keyid3Arr[x].length() == 0 ? "(null)" : keyid3Arr[x];
            String paramListId = paramListIdArr[x];
            if (paramListId == null || paramListId.trim().isEmpty()) {
                throw new SapphireException("INVALID_PROPERTY", "paramListId not specified for keyid1");
            }
            String paramListVersionId = paramListVersionIdArr[x];
            if (paramListVersionId == null || paramListVersionId.trim().isEmpty()) {
                throw new SapphireException("INVALID_PROPERTY", "paramListVersionId not specified for keyid1:" + keyId1);
            }
            String variantId = variantIdArr[x];
            if (variantId == null || variantId.trim().isEmpty()) {
                throw new SapphireException("INVALID_PROPERTY", "variantId not specified for keyid1:" + keyId1);
            }
            String dataSet = dataSetArr[x];
            if (dataSet == null || dataSet.trim().isEmpty()) {
                throw new SapphireException("INVALID_PROPERTY", "dataSet not specified for keyid1:" + keyId1);
            }
            sql = new StringBuilder();
            sql.append("select COUNT(*) FROM SDIDATAAPPROVAL");
            sql.append(" WHERE SDCID='" + sdcid + "' AND KEYID1='" + keyId1 + "'");
            sql.append(" AND KEYID2='" + keyId2 + "' AND KEYID3='" + keyId3 + "'");
            sql.append(" AND PARAMLISTID='" + paramListId + "' AND PARAMLISTVERSIONID='" + paramListVersionId + "'");
            sql.append(" AND VARIANTID='" + variantId + "' AND DATASET='" + dataSet + "'");
            int numberOfSdiDataApprovals = this.getQueryProcessor().getCount(sql.toString());
            if (numberOfSdiDataApprovals == 0) {
                sql = new StringBuilder();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("SELECT UNIQUENESSFLAG,SEQUENCEFLAG FROM APPROVALTYPE WHERE APPROVALTYPEID=" + safeSQL.addVar(approvalType));
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                DataSet sdiDataDataSet = new DataSet();
                if (ds != null && ds.getRowCount() > 0) {
                    String uniquenessFlag = ds.getString(0, "uniquenessflag", null);
                    String sequenceflag = ds.getString(0, "sequenceflag", null);
                    sdiDataDataSet.addRow();
                    sdiDataDataSet.setString(0, "sdcid", sdcid);
                    sdiDataDataSet.setString(0, "keyid1", keyId1);
                    sdiDataDataSet.setString(0, "keyid2", keyId2);
                    sdiDataDataSet.setString(0, "keyid3", keyId3);
                    sdiDataDataSet.setString(0, "paramlistid", paramListId);
                    sdiDataDataSet.setString(0, "paramlistversionid", paramListVersionId);
                    sdiDataDataSet.setString(0, "variantid", variantId);
                    sdiDataDataSet.setString(0, "dataset", dataSet);
                    sdiDataDataSet.setString(0, "uniquenessflag", uniquenessFlag);
                    sdiDataDataSet.setString(0, "approvalsequenceflag", sequenceflag);
                }
                DataSetUtil.update(this.database, sdiDataDataSet, "sdidata", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset"});
            }
            if (reason.length() > 0 && tracelogid.length() == 0) {
                this.logger.info("Generate the tracelog record");
                PropertyList tracelogprops = new PropertyList();
                tracelogprops.setProperty("sdcid", sdcid);
                tracelogprops.setProperty("keyid1", keyId1);
                tracelogprops.setProperty("keyid2", keyId2);
                tracelogprops.setProperty("keyid3", keyId3);
                tracelogprops.setProperty("description", "Add SdiDataApproval");
                tracelogprops.setProperty("auditreason", reason);
                tracelogprops.setProperty("auditactivity", activity);
                tracelogprops.setProperty("auditsignedflag", signedFlag);
                tracelogprops.setProperty("auditdt", auditdt);
                ActionProcessor ap = this.getActionProcessor();
                ap.processActionClass(AddSDITraceLog.class.getName(), tracelogprops);
                tracelogid = tracelogprops.getProperty("tracelogid");
            }
            DataSet sdiDataApprovalDataSet = new DataSet();
            sdiDataApprovalDataSet.addColumn("sdcid", 0);
            sdiDataApprovalDataSet.addColumn("keyid1", 0);
            sdiDataApprovalDataSet.addColumn("keyid2", 0);
            sdiDataApprovalDataSet.addColumn("keyid3", 0);
            sdiDataApprovalDataSet.addColumn("paramlistid", 0);
            sdiDataApprovalDataSet.addColumn("paramlistversionid", 0);
            sdiDataApprovalDataSet.addColumn("variantid", 0);
            sdiDataApprovalDataSet.addColumn("dataset", 1);
            sdiDataApprovalDataSet.addColumn("approvalstep", 0);
            sdiDataApprovalDataSet.addColumn("approvalflag", 0);
            sdiDataApprovalDataSet.addColumn("mandatoryflag", 0);
            sdiDataApprovalDataSet.addColumn("forcepeerflag", 0);
            sdiDataApprovalDataSet.addColumn("roleid", 0);
            sdiDataApprovalDataSet.addColumn("usersequence", 1);
            sdiDataApprovalDataSet.addColumn("createdt", 2);
            sdiDataApprovalDataSet.addColumn("createby", 0);
            sdiDataApprovalDataSet.addColumn("createtool", 0);
            sdiDataApprovalDataSet.addColumn("moddt", 2);
            sdiDataApprovalDataSet.addColumn("modby", 0);
            sdiDataApprovalDataSet.addColumn("modtool", 0);
            sdiDataApprovalDataSet.addColumn("tracelogid", 1);
            Integer maxUserSequence = 0;
            for (int i = 0; i < approvalSteps.length; ++i) {
                Object safeSQL;
                String forcePeerFlag;
                String approvalStep = approvalSteps[i];
                String roleId = i >= roleIds.length ? null : roleIds[i];
                String mandatoryFlag = i >= mandatoryFlags.length ? null : mandatoryFlags[i];
                String string = forcePeerFlag = i >= forcePeerFlags.length ? null : forcePeerFlags[i];
                if (maxUserSequence == 0) {
                    sql = new StringBuilder();
                    safeSQL = new SafeSQL();
                    sql.append("select MAX(USERSEQUENCE) from SDIDATAAPPROVAL WHERE KEYID1=" + ((SafeSQL)safeSQL).addVar(keyId1));
                    sql.append(" AND KEYID2=" + ((SafeSQL)safeSQL).addVar(keyId2) + " AND KEYID3=" + ((SafeSQL)safeSQL).addVar(keyId3) + " AND PARAMLISTID=" + ((SafeSQL)safeSQL).addVar(paramListId) + " AND PARAMLISTVERSIONID=" + ((SafeSQL)safeSQL).addVar(paramListVersionId));
                    sql.append(" AND VARIANTID=" + ((SafeSQL)safeSQL).addVar(variantId) + " AND DATASET=" + ((SafeSQL)safeSQL).addVar(dataSet));
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), ((SafeSQL)safeSQL).getValues());
                    maxUserSequence = ds != null && ds.getRowCount() > 0 ? Integer.valueOf(ds.getInt(0, "usersequence") > 0 ? ds.getInt(0, "usersequence") + 1 : 1) : Integer.valueOf(1);
                } else {
                    safeSQL = maxUserSequence;
                    Integer n = maxUserSequence = Integer.valueOf(maxUserSequence + 1);
                }
                int rowid = sdiDataApprovalDataSet.addRow();
                sdiDataApprovalDataSet.setString(rowid, "sdcid", sdcid);
                sdiDataApprovalDataSet.setString(rowid, "keyid1", keyId1);
                sdiDataApprovalDataSet.setString(rowid, "keyid2", keyId2);
                sdiDataApprovalDataSet.setString(rowid, "keyid3", keyId3);
                sdiDataApprovalDataSet.setString(rowid, "paramlistid", paramListId);
                sdiDataApprovalDataSet.setString(rowid, "paramlistversionid", paramListVersionId);
                sdiDataApprovalDataSet.setString(rowid, "variantid", variantId);
                sdiDataApprovalDataSet.setNumber(rowid, "dataset", dataSet);
                sdiDataApprovalDataSet.setString(rowid, "approvalstep", approvalStep);
                sdiDataApprovalDataSet.setString(rowid, "approvalflag", "U");
                sdiDataApprovalDataSet.setString(rowid, "mandatoryflag", mandatoryFlag);
                sdiDataApprovalDataSet.setString(rowid, "forcepeerflag", forcePeerFlag);
                sdiDataApprovalDataSet.setString(rowid, "roleid", roleId);
                sdiDataApprovalDataSet.setNumber(rowid, "usersequence", maxUserSequence);
                sdiDataApprovalDataSet.setDate(rowid, "createdt", DateTimeUtil.getNowCalendar());
                sdiDataApprovalDataSet.setString(rowid, "createby", this.connectionInfo.getSysuserId());
                sdiDataApprovalDataSet.setString(rowid, "createtool", this.connectionInfo.getTool());
                sdiDataApprovalDataSet.setDate(rowid, "moddt", DateTimeUtil.getNowCalendar());
                sdiDataApprovalDataSet.setString(rowid, "modby", this.connectionInfo.getSysuserId());
                sdiDataApprovalDataSet.setString(rowid, "modtool", this.connectionInfo.getTool());
                sdiDataApprovalDataSet.setNumber(rowid, "tracelogid", tracelogid);
            }
            DataSetUtil.insert(this.database, sdiDataApprovalDataSet, "sdidataapproval");
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyId1);
            props.setProperty("keyid2", keyId2);
            props.setProperty("keyid3", keyId3);
            props.setProperty("auditreason", reason);
            props.setProperty("auditactivity", activity);
            props.setProperty("auditsignedflag", signedFlag);
            this.getActionProcessor().processAction("UpdateDatasetStatus", VERSIONID, props);
        }
    }
}

