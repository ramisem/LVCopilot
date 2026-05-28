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

public class AddDataApproval
extends BaseAction
implements sapphire.action.AddDataApproval {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("SDC not specified."));
        }
        String[] keyid1Arr = StringUtil.split(properties.getProperty("keyid1"), ";");
        if (keyid1Arr.length == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Key Ids not specified."));
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
        if (!propsmatch) {
            if (approvalTypeIds.length > 1) {
                throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Only one approval type id is allowed to be passed."));
            }
            String approvalTypeId = approvalTypeIds[0];
            if (approvalTypeId == null || approvalTypeId.trim().isEmpty()) {
                throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("ApprovalTypeId not specified "));
            }
            for (int x = 0; x < keyid1Arr.length; ++x) {
                String keyId1 = keyid1Arr[x];
                if (keyId1 == null || keyId1.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Keyid1 not specified."));
                }
                String keyId2 = keyid2Arr.length == 0 || keyid2Arr.length < keyid1Arr.length || keyid2Arr[x].length() == 0 ? "(null)" : keyid2Arr[x];
                String keyId3 = keyid3Arr.length == 0 || keyid3Arr.length < keyid1Arr.length || keyid3Arr[x].length() == 0 ? "(null)" : keyid3Arr[x];
                String paramListId = paramListIdArr[x];
                if (paramListId == null || paramListId.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("paramListId not specified for keyid1"));
                }
                String paramListVersionId = paramListVersionIdArr[x];
                if (paramListVersionId == null || paramListVersionId.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("paramListVersionId not specified for keyid1:" + keyId1));
                }
                String variantId = variantIdArr[x];
                if (variantId == null || variantId.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("variantId not specified for keyid1:" + keyId1));
                }
                String dataSet = dataSetArr[x];
                if (dataSet == null || dataSet.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("dataSet not specified for keyid1:" + keyId1));
                }
                this.addDataApproval(sdcid, keyId1, keyId2, keyId3, paramListId, paramListVersionId, variantId, dataSet, reason, tracelogid, activity, signedFlag, auditdt, approvalTypeId);
            }
        } else {
            if (approvalTypeIds.length != keyid1Arr.length) {
                throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("The number of approval types passed should match the number of keyid1 ids passed, when propsmatch is Y"));
            }
            for (int x = 0; x < keyid1Arr.length; ++x) {
                String keyId1 = keyid1Arr[x];
                if (keyId1 == null || keyId1.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Keyid1 can't be blank."));
                }
                String approvalTypeId = approvalTypeIds[x];
                if (approvalTypeId == null || approvalTypeId.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Approval type id can't be blank.."));
                }
                String keyId2 = keyid2Arr.length == 0 || keyid2Arr.length < keyid1Arr.length || keyid2Arr[x].length() == 0 ? "(null)" : keyid2Arr[x];
                String keyId3 = keyid3Arr.length == 0 || keyid3Arr.length < keyid1Arr.length || keyid3Arr[x].length() == 0 ? "(null)" : keyid3Arr[x];
                String paramListId = paramListIdArr[x];
                if (paramListId == null || paramListId.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("paramListId not specified for keyid1"));
                }
                String paramListVersionId = paramListVersionIdArr[x];
                if (paramListVersionId == null || paramListVersionId.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("paramListVersionId not specified for keyid1:" + keyId1));
                }
                String variantId = variantIdArr[x];
                if (variantId == null || variantId.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("variantId not specified for keyid1:" + keyId1));
                }
                String dataSet = dataSetArr[x];
                if (dataSet == null || dataSet.trim().isEmpty()) {
                    throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("dataSet not specified for keyid1:" + keyId1));
                }
                this.addDataApproval(sdcid, keyId1, keyId2, keyId3, paramListId, paramListVersionId, variantId, dataSet, reason, tracelogid, activity, signedFlag, auditdt, approvalTypeId);
            }
        }
    }

    private void addDataApproval(String sdcid, String keyId1, String keyId2, String keyId3, String paramListId, String paramListVersionId, String variantId, String dataset, String reason, String tracelogid, String activity, String signedFlag, String auditdt, String approvalTypeId) throws SapphireException {
        StringBuilder sql = new StringBuilder();
        sql.append("select COUNT(*) FROM SDIDATAAPPROVAL");
        sql.append(" WHERE SDCID='" + sdcid + "' AND KEYID1='" + keyId1 + "'");
        sql.append(" AND KEYID2='" + keyId2 + "' AND KEYID3='" + keyId3 + "'");
        sql.append(" AND PARAMLISTID='" + paramListId + "' AND PARAMLISTVERSIONID='" + paramListVersionId + "'");
        sql.append(" AND VARIANTID='" + variantId + "' AND DATASET='" + dataset + "'");
        int numberOfSdiDataApprovals = this.getQueryProcessor().getCount(sql.toString());
        if (numberOfSdiDataApprovals > 0) {
            HashMap<String, String> token = new HashMap<String, String>();
            token.put("sdcid", sdcid);
            token.put("keyid1", keyId1);
            token.put("keyid2", keyId2);
            token.put("keyid3", keyId3);
            token.put("paramlistid", paramListId);
            token.put("paramlistversionid", paramListVersionId);
            token.put("variantid", variantId);
            token.put("dataset", dataset);
            throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("SDIData: [sdcid], [keyid1], [keyid2], [paramlistid], [paramlistversionid], [variantid], [dataset]  already have approval.", token));
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
        SafeSQL safeSQL = new SafeSQL();
        sql = new StringBuilder();
        sql.append("select APPROVALSTEP,ROLEID,MANDATORYFLAG,FORCEPEERFLAG,USERSEQUENCE FROM APPROVALTYPESTEP WHERE APPROVALTYPEID=").append(safeSQL.addVar(approvalTypeId));
        String approvalStep = null;
        String roleId = null;
        String mandatoryFlag = null;
        String forcePeerFlag = null;
        Integer userSequence = null;
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
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
        for (int j = 0; j < ds.getRowCount(); ++j) {
            approvalStep = ds.getValue(j, "approvalstep");
            roleId = ds.getValue(j, "roleid");
            mandatoryFlag = ds.getValue(j, "mandatoryflag");
            forcePeerFlag = ds.getValue(j, "forcePeerFlag");
            userSequence = ds.getInt(j, "usersequence");
            if (approvalStep == null || roleId == null || mandatoryFlag == null) continue;
            int rowid = sdiDataApprovalDataSet.addRow();
            sdiDataApprovalDataSet.setString(rowid, "sdcid", sdcid);
            sdiDataApprovalDataSet.setString(rowid, "keyid1", keyId1);
            sdiDataApprovalDataSet.setString(rowid, "keyid2", keyId2);
            sdiDataApprovalDataSet.setString(rowid, "keyid3", keyId3);
            sdiDataApprovalDataSet.setString(rowid, "paramlistid", paramListId);
            sdiDataApprovalDataSet.setString(rowid, "paramlistversionid", paramListVersionId);
            sdiDataApprovalDataSet.setString(rowid, "variantid", variantId);
            sdiDataApprovalDataSet.setNumber(rowid, "dataset", dataset);
            sdiDataApprovalDataSet.setString(rowid, "approvalstep", approvalStep);
            sdiDataApprovalDataSet.setString(rowid, "approvalflag", "U");
            sdiDataApprovalDataSet.setString(rowid, "mandatoryflag", mandatoryFlag);
            sdiDataApprovalDataSet.setString(rowid, "forcepeerflag", forcePeerFlag);
            sdiDataApprovalDataSet.setString(rowid, "roleid", roleId);
            sdiDataApprovalDataSet.setNumber(rowid, "usersequence", userSequence);
            sdiDataApprovalDataSet.setDate(rowid, "createdt", DateTimeUtil.getNowCalendar());
            sdiDataApprovalDataSet.setString(rowid, "createby", this.connectionInfo.getSysuserId());
            sdiDataApprovalDataSet.setString(rowid, "createtool", this.connectionInfo.getTool());
            sdiDataApprovalDataSet.setDate(rowid, "moddt", DateTimeUtil.getNowCalendar());
            sdiDataApprovalDataSet.setString(rowid, "modby", this.connectionInfo.getSysuserId());
            sdiDataApprovalDataSet.setString(rowid, "modtool", this.connectionInfo.getTool());
            sdiDataApprovalDataSet.setNumber(rowid, "tracelogid", tracelogid);
        }
        DataSetUtil.insert(this.database, sdiDataApprovalDataSet, "sdidataapproval");
        sql = new StringBuilder();
        safeSQL = new SafeSQL();
        sql.append("SELECT UNIQUENESSFLAG,SEQUENCEFLAG FROM APPROVALTYPE WHERE APPROVALTYPEID=").append(safeSQL.addVar(approvalTypeId));
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        String uniquenessFlag = null;
        String sequenceFlag = null;
        if (ds.getRowCount() > 0) {
            uniquenessFlag = ds.getValue(0, "uniquenessflag");
            sequenceFlag = ds.getValue(0, "sequenceflag");
            DataSet sdiDataDataSet = new DataSet();
            sdiDataDataSet.addRow();
            sdiDataDataSet.setString(0, "sdcid", sdcid);
            sdiDataDataSet.setString(0, "keyid1", keyId1);
            sdiDataDataSet.setString(0, "keyid2", keyId2);
            sdiDataDataSet.setString(0, "keyid3", keyId3);
            sdiDataDataSet.setString(0, "paramlistid", paramListId);
            sdiDataDataSet.setString(0, "paramlistversionid", paramListVersionId);
            sdiDataDataSet.setString(0, "variantid", variantId);
            sdiDataDataSet.setString(0, "dataset", dataset);
            sdiDataDataSet.setString(0, "uniquenessflag", uniquenessFlag);
            sdiDataDataSet.setString(0, "approvalsequenceflag", sequenceFlag);
            DataSetUtil.update(this.database, sdiDataDataSet, "sdidata", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset"});
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", sdcid);
        props.setProperty("keyid1", keyId1);
        props.setProperty("keyid2", keyId2);
        props.setProperty("keyid3", keyId3);
        props.setProperty("auditreason", reason);
        props.setProperty("auditactivity", activity);
        props.setProperty("auditsignedflag", signedFlag);
        this.getActionProcessor().processAction("UpdateDatasetStatus", "1", props);
    }
}

