/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.QCBatchReagentSync;
import com.labvantage.opal.exception.QCPositioningException;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.qcbatch.QCPositioning;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.sql.common.Aqc;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.SDI;
import java.sql.PreparedStatement;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateQCBatch
extends BaseAction
implements sapphire.action.CreateQCBatch {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77415 $";
    public static final String QCSAMPLETYPE = "Unknown";
    private TranslationProcessor tp;

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        DataSet sampleDS;
        String[] keyIDArray;
        PreparedStatement psmtUnknownDatasets;
        this.tp = this.getTranslationProcessor();
        String keyIds = properties.getProperty("keyid");
        String sdidataids = properties.getProperty("sdidataids");
        String sdiworkitemids = properties.getProperty("sdiworkitemids");
        String qcBatchId = properties.getProperty("qcbatchid", "");
        sdidataids = sdidataids.trim();
        sdiworkitemids = sdiworkitemids.trim();
        String uniq_sampleIds = "";
        if (keyIds.length() == 0 && sdidataids.length() == 0 && sdiworkitemids.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.tp.translate("Either Sample Ids or SDIWorkItem Ids or SDIdata Ids  to be specified in the action input."));
        }
        String qcMethodId = properties.getProperty("qcmethodid");
        String qcMethodVersionId = properties.getProperty("qcmethodversionid");
        if (qcMethodId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.tp.translate("QCMethod Id not specified in the input."));
        }
        if (qcMethodVersionId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.tp.translate("QCMethod Version not specified in the input."));
        }
        QueryProcessor qp = this.getQueryProcessor();
        SQLGenerator __SqlGenerator = SQLFactory.getSqlGenerator(this.getConnectionProcessor().isOra());
        SDI sdi = new SDI("QCMethod", qcMethodId, qcMethodVersionId, null);
        SafeSQL safeSQL = new SafeSQL();
        DataSet dsQCMethod = qp.getPreparedSqlDataSet(Aqc.getQCMethodDetails(sdi, safeSQL), safeSQL.getValues());
        if (dsQCMethod == null || dsQCMethod.size() == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.tp.translate("The specified QC Method not found.  Cannot proceed further."));
        }
        String delimeter = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        String batchSdcId = dsQCMethod.getValue(0, "methodsdcid", "");
        String paramListType = dsQCMethod.getValue(0, "paramlisttype", "");
        boolean matchWorkItemItem = QCUtil.matchSDIWorkItemItem(this.getConfigurationProcessor());
        if (!matchWorkItemItem && sdiworkitemids.length() > 0) {
            throw new SapphireException(this.tp.translate("To create QCBatch with selected SDIWorkItem instances, please set Honor SDIWorkItemItem as 'Yes' in AQC policy."));
        }
        if (sdiworkitemids.length() > 0) {
            psmtUnknownDatasets = this.database.prepareStatement("unknowndatasets", QCUtil.getUnknownbasedOnSDIWorkItems(paramListType));
            keyIDArray = StringUtil.split(sdiworkitemids, delimeter);
        } else if (sdidataids.length() > 0) {
            psmtUnknownDatasets = this.database.prepareStatement("unknowndatasets", QCUtil.getUnknownbasedOnSDIDatas(paramListType, matchWorkItemItem));
            keyIDArray = StringUtil.split(sdidataids, delimeter);
        } else {
            psmtUnknownDatasets = this.database.prepareStatement("unknowndatasets", QCUtil.getDistinctUnknownDatasetsSQL(paramListType, matchWorkItemItem));
            keyIDArray = StringUtil.split(keyIds, delimeter);
        }
        try {
            psmtUnknownDatasets.setString(1, batchSdcId);
            String colId = sdiworkitemids.length() > 0 ? "sdiworkitemid" : "sdidataid";
            String sampleIds = "";
            for (int i = 0; i < keyIDArray.length; ++i) {
                int findRow;
                int k = 2;
                if (paramListType.trim().length() > 0) {
                    psmtUnknownDatasets.setString(k++, paramListType);
                }
                psmtUnknownDatasets.setString(k++, keyIDArray[i]);
                psmtUnknownDatasets.setString(k++, qcMethodId);
                psmtUnknownDatasets.setString(k, qcMethodVersionId);
                DataSet dsUnknown = new DataSet(psmtUnknownDatasets.executeQuery());
                if (dsUnknown.getRowCount() == 0) {
                    if (sdiworkitemids.length() > 0) {
                        throw new SapphireException("INVALID_PROPERTY", this.tp.translate("No Matching dataset found for the specified SDIWorkItem Ids") + ": " + sdiworkitemids);
                    }
                    if (sdidataids.length() <= 0) throw new SapphireException("INVALID_PROPERTY", keyIDArray[i] + " " + this.tp.translate("does not have the required dataset to be linked with the new QCBatch."));
                    throw new SapphireException("INVALID_PROPERTY", this.tp.translate("No Matching dataset found for the specified SDIData Ids") + ": " + sdidataids);
                }
                if (sdiworkitemids.length() <= 0 && sdidataids.length() <= 0 || (findRow = dsUnknown.findRow(colId, keyIDArray[i])) <= -1) continue;
                String keyid1 = dsUnknown.getValue(findRow, "keyid1");
                if (uniq_sampleIds.indexOf(";" + keyid1) < 0) {
                    uniq_sampleIds = uniq_sampleIds + ";" + keyid1;
                }
                sampleIds = sampleIds + ";" + keyid1;
            }
            if (sampleIds.startsWith(";")) {
                keyIds = sampleIds.substring(1);
                keyIDArray = StringUtil.split(keyIds, ";");
            }
            if (uniq_sampleIds.startsWith(";")) {
                uniq_sampleIds = uniq_sampleIds.substring(1);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            this.database.closeStatement("unknowndatasets");
            this.database.closeStatement("unknowndatasetsonworkitem");
        }
        ActionProcessor ap = this.getActionProcessor();
        QCPositioning qcbean = new QCPositioning();
        qcbean.setMethodID(qcMethodId);
        qcbean.setMethodVersionID(qcMethodVersionId);
        qcbean.setQueryProcessor(qp);
        String columns = "usersequence|linktoqcbatchitemid|keyid1|qcsampletype|qcbatchsampletypeid|qcbatchitemdesc|linkedto|s_qcbatchid|s_qcbatchitemid";
        qcbean.setUnknownSampleColumns(columns);
        keyIds = keyIds.replaceAll(delimeter, ";");
        if (StringUtil.split(keyIds, ";").length > 750) {
            String rsetid = this.getDAMProcessor().createRSet("Sample", uniq_sampleIds.length() > 0 ? uniq_sampleIds : keyIds, null, null);
            if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("PROCESSACTION_FAILED", "CreateQCBatch " + this.tp.translate("Unable to create RSET for samples."));
            safeSQL.reset();
            sampleDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_sampleid, sampledesc FROM s_sample WHERE s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = " + safeSQL.addVar(rsetid) + " )", safeSQL.getValues());
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            safeSQL.reset();
            sampleDS = qp.getPreparedSqlDataSet("SELECT s_sampleid, sampledesc FROM s_sample WHERE s_sampleid IN (" + safeSQL.addIn(this.database.isOracle() ? OpalUtil.getSqlWhereClause(keyIds) : OpalUtil.getSqlWhereClause(keyIds, true)) + ")", safeSQL.getValues());
        }
        StringBuffer sampleData = new StringBuffer();
        for (int i = 0; i < keyIDArray.length; ++i) {
            String sampleId = keyIDArray[i];
            String userSequence = "" + (i + 1);
            int findRow = sampleDS.findRow("s_sampleid", sampleId);
            if (findRow <= -1) continue;
            sampleId = sampleDS.getString(findRow, "s_sampleid").replaceAll(",", "#COMMA#");
            String desc = sampleDS.getString(findRow, "sampledesc", "").replaceAll(",", "#COMMA#");
            sampleData.append("|" + userSequence + ",," + sampleId + "," + QCSAMPLETYPE + ",," + desc + ",,,N");
        }
        qcbean.setUnknownSamplesData(sampleData.substring(1));
        StringBuffer finalSamplesList = new StringBuffer();
        try {
            qcbean.insertQCSamples();
            finalSamplesList.append(qcbean.getFinalSamplesList());
        }
        catch (QCPositioningException sampleId) {
            // empty catch block
        }
        String finalSamplesString = finalSamplesList.toString();
        String[] finalSamples = finalSamplesString.split("\\|");
        sampleData.setLength(0);
        for (int i = 0; i < finalSamples.length; ++i) {
            String userSequence = "" + (i + 1);
            String[] columnData = finalSamples[i].split(",");
            columnData[0] = userSequence;
            String linkedTo = columnData[6];
            if (linkedTo != null && linkedTo.trim().length() > 0) {
                int linktoBatchItem;
                try {
                    linktoBatchItem = linkedTo.startsWith("+") ? i + 1 + Integer.parseInt(linkedTo.substring(linkedTo.indexOf("+") + 1)) : (linkedTo.startsWith("-") ? i + 1 - Integer.parseInt(linkedTo.substring(linkedTo.indexOf("-") + 1)) : Integer.parseInt(linkedTo));
                }
                catch (NumberFormatException ne) {
                    throw new SapphireException("PROCESSACTION_FAILED", "CreateQCBatch " + this.tp.translate("Action failed for invalid LinkTo field") + " " + linkedTo + ". " + ErrorUtil.extractMessageFromException(ne, ErrorUtil.isUserAdmin(this.getConnectionId())), ne);
                }
                columnData[1] = "" + linktoBatchItem;
            }
            StringBuffer data = new StringBuffer();
            for (int j = 0; j < columnData.length; ++j) {
                if (columnData.length == 10 && j == 8) continue;
                data.append(",").append(columnData[j]);
            }
            sampleData.append("|").append(data.substring(1));
        }
        PropertyList actionProps = new PropertyList();
        if (qcBatchId.length() > 0 && !qcBatchId.contains("[")) {
            actionProps.setProperty("keyid1", qcBatchId);
            actionProps.setProperty("overrideautokey", "Y");
        }
        actionProps.setProperty("sdcid", "QCBatch");
        actionProps.setProperty("qcbatchdesc", properties.getProperty("qcbatchdesc"));
        actionProps.setProperty("assignedanalyst", properties.getProperty("assignedanalyst"));
        actionProps.setProperty("notes", properties.getProperty("qcbatchnotes"));
        actionProps.setProperty("qcmethodid", properties.getProperty("qcmethodid"));
        actionProps.setProperty("qcmethodversionid", properties.getProperty("qcmethodversionid"));
        try {
            ap.processAction("AddSDI", "1", actionProps);
            qcBatchId = actionProps.getProperty("newkeyid1");
            actionProps.clear();
            actionProps.setProperty("qcmethodid", qcMethodId);
            actionProps.setProperty("qcmethodversionid", qcMethodVersionId);
            actionProps.setProperty("qcbatchid", qcBatchId);
            actionProps.setProperty("allsamples", sampleData.substring(1));
            actionProps.setProperty("columns", columns);
            actionProps.setProperty("sdiworkitemids", properties.getProperty("sdiworkitemids"));
            actionProps.setProperty("sdidataids", properties.getProperty("sdidataids"));
            actionProps.setProperty("urlencodeddata", properties.getProperty("urlencodeddata"));
            ap.processAction("ApplyQCMethod", "1", actionProps);
            this.logger.info("New QCBatch created with QC Batch Id:" + qcBatchId);
            actionProps.clear();
            actionProps.setProperty("qcbatchid", qcBatchId);
            ap.processActionClass(QCBatchReagentSync.class.getName(), actionProps);
            properties.setProperty("qcbatchid", qcBatchId);
            return;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SapphireException("PROCESSACTION_FAILED", "CreateQCBatch " + this.tp.translate("Action failed:") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }
}

