/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidataapproval;

import com.labvantage.sapphire.DataSetUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteDataApprovalStep
extends BaseAction
implements sapphire.action.DeleteDataApprovalStep {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    public static final String ID = "DeleteDataApprovalStep";
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
        String[] approvalSteps = StringUtil.split(properties.getProperty("approvalstep", ""), ";");
        String reason = properties.getProperty("auditreason");
        String activity = properties.getProperty("auditactivity");
        String signedFlag = properties.getProperty("auditsignedflag");
        String auditdt = properties.getProperty("auditdt");
        String tracelogid = properties.getProperty("tracelogid", "").trim();
        StringBuilder sql = null;
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
            for (int j = 0; j < approvalTypeIds.length; ++j) {
                String approvalType = approvalTypeIds[j];
                for (int i = 0; i < approvalSteps.length; ++i) {
                    String approvalStep = approvalSteps[i];
                    sql = new StringBuilder();
                    sql.append("select COUNT(*) FROM SDIDATAAPPROVAL");
                    sql.append(" WHERE SDCID='" + sdcid + "' AND KEYID1='" + keyId1 + "'");
                    sql.append(" AND KEYID2='" + keyId2 + "' AND KEYID3='" + keyId3 + "'");
                    sql.append(" AND PARAMLISTID='" + paramListId + "' AND PARAMLISTVERSIONID='" + paramListVersionId + "'");
                    sql.append(" AND VARIANTID='" + variantId + "' AND DATASET='" + dataSet + "' AND COALESCE(APPROVALFLAG, 'U')!='U'");
                    sql.append(" AND APPROVALSTEP='" + approvalStep + "'");
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
                        token.put("dataset", dataSet);
                        throw new SapphireException("INVALID_PROPERTY", this.getTranslationProcessor().translate("Dataset: [sdcid], [keyid1], [keyid2], [paramlistid], [paramlistversionid], [variantid], [dataset]  already have have appoved approval, so can't be deleted.", token));
                    }
                    SafeSQL safeSQLKeyClause = new SafeSQL();
                    sql = new StringBuilder();
                    sql.append("DELETE FROM SDIDATAAPPROVAL ");
                    sql.append(" WHERE SDCID=" + safeSQLKeyClause.addVar(sdcid) + " AND KEYID1=" + safeSQLKeyClause.addVar(keyId1));
                    sql.append(" AND KEYID2=" + safeSQLKeyClause.addVar(keyId2) + " AND KEYID3=" + safeSQLKeyClause.addVar(keyId3));
                    sql.append(" AND PARAMLISTID=" + safeSQLKeyClause.addVar(paramListId) + " AND PARAMLISTVERSIONID=" + safeSQLKeyClause.addVar(paramListVersionId));
                    sql.append(" AND VARIANTID=" + safeSQLKeyClause.addVar(variantId) + " AND DATASET=" + safeSQLKeyClause.addVar(dataSet) + " AND APPROVALSTEP=" + safeSQLKeyClause.addVar(approvalStep));
                    this.logger.info("Deleting SDIDATAAPPROVAL record: " + sql.toString());
                    this.database.executePreparedUpdate(sql.toString(), safeSQLKeyClause.getValues());
                }
            }
            sql = new StringBuilder();
            sql.append("select COUNT(*) FROM SDIDATAAPPROVAL");
            sql.append(" WHERE SDCID='" + sdcid + "' AND KEYID1='" + keyId1 + "'");
            sql.append(" AND KEYID2='" + keyId2 + "' AND KEYID3='" + keyId3 + "'");
            sql.append(" AND PARAMLISTID='" + paramListId + "' AND PARAMLISTVERSIONID='" + paramListVersionId + "'");
            sql.append(" AND VARIANTID='" + variantId + "' AND DATASET='" + dataSet + "'");
            int numberOfSdiDataApprovals = this.getQueryProcessor().getCount(sql.toString());
            if (numberOfSdiDataApprovals == 0) {
                DataSet sdiDataDataSet = new DataSet();
                sdiDataDataSet.addRow();
                sdiDataDataSet.setString(0, "sdcid", sdcid);
                sdiDataDataSet.setString(0, "keyid1", keyId1);
                sdiDataDataSet.setString(0, "keyid2", keyId2);
                sdiDataDataSet.setString(0, "keyid3", keyId3);
                sdiDataDataSet.setString(0, "paramlistid", paramListId);
                sdiDataDataSet.setString(0, "paramlistversionid", paramListVersionId);
                sdiDataDataSet.setString(0, "variantid", variantId);
                sdiDataDataSet.setString(0, "dataset", dataSet);
                sdiDataDataSet.setString(0, "approvalflag", null);
                sdiDataDataSet.setString(0, "uniquenessflag", null);
                sdiDataDataSet.setString(0, "approvalsequenceflag", null);
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
            this.getActionProcessor().processAction("UpdateDatasetStatus", VERSIONID, props);
        }
    }
}

