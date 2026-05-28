/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.qcbatch.QCBatchItem;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.qcbatch.QCBatchSampleType;
import com.labvantage.opal.util.QCUtil;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCBatch
extends BaseSDCRules {
    @Override
    public void preDelete(String rsetid, PropertyList actionProperties) throws SapphireException {
        ActionProcessor ap = this.getActionProcessor();
        SafeSQL safeSQL = new SafeSQL();
        PropertyList deleteSDIDetail = new PropertyList();
        deleteSDIDetail.setProperty("linkid", "QCBatchItem");
        deleteSDIDetail.setProperty("sdcid", "QCBatch");
        deleteSDIDetail.setProperty("keyid1", actionProperties.getProperty("keyid1"));
        ap.processAction("DeleteSDIDetail", "1", deleteSDIDetail);
        DataSet qcBatchSampleTypeDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_qcbatchsampletypeid FROM s_qcbatchsampletype WHERE qcbatchid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )", safeSQL.getValues());
        if (qcBatchSampleTypeDS.size() > 0) {
            String qcBatchSampleTypeList = qcBatchSampleTypeDS.getColumnValues("s_qcbatchsampletypeid", ";");
            HashMap<String, String> properties = new HashMap<String, String>();
            properties.put("sdcid", "QCBatchSampleType");
            properties.put("keyid1", qcBatchSampleTypeList);
            ap.processAction("DeleteSDI", "1", properties);
        }
        this.updateSDIDataBlockFlag(actionProperties.getProperty("keyid1"), "N");
        this.deleteQCBatchInstrument(rsetid);
    }

    private void deleteQCBatchInstrument(String rsetid) throws SapphireException {
        ActionProcessor ap = this.getActionProcessor();
        SafeSQL safeSQL = new SafeSQL();
        DataSet qcBatchInstrDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_qcbatchinstrumentid FROM s_qcbatchinstrument WHERE qcbatchid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rsetid) + " )", safeSQL.getValues());
        if (qcBatchInstrDS.size() > 0) {
            HashMap<String, String> properties = new HashMap<String, String>();
            properties.put("sdcid", "LV_QCBatchInstrument");
            properties.put("keyid1", qcBatchInstrDS.getColumnValues("s_qcbatchinstrumentid", ";"));
            ap.processAction("DeleteSDI", "1", properties);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String blockString = actionProps.getProperty("blockflag");
        if (blockString != null && blockString.length() > 0) {
            String qcbatchids = actionProps.getProperty("keyid1");
            this.updateSDIDataBlockFlag(qcbatchids, blockString);
        }
    }

    private void updateSDIDataBlockFlag(String qcbatchids, String blockString) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT sdcid,keyid1, paramlistid, paramlistversionid, variantid, dataset FROM sdidata WHERE s_qcbatchid in( " + safeSQL.addIn(qcbatchids, ";") + ")";
        DataSet sdidata = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (sdidata.getRowCount() > 0) {
            PropertyList editDataSetProps = new PropertyList();
            editDataSetProps.setProperty("sdcid", sdidata.getString(0, "sdcid"));
            editDataSetProps.setProperty("keyid1", sdidata.getColumnValues("keyid1", ";"));
            editDataSetProps.setProperty("paramlistid", sdidata.getColumnValues("paramlistid", ";"));
            editDataSetProps.setProperty("paramlistversionid", sdidata.getColumnValues("paramlistversionid", ";"));
            editDataSetProps.setProperty("variantid", sdidata.getColumnValues("variantid", ";"));
            editDataSetProps.setProperty("dataset", sdidata.getColumnValues("dataset", ";"));
            editDataSetProps.setProperty("propsmatch", "Y");
            editDataSetProps.setProperty("blockflag", blockString);
            this.getActionProcessor().processAction("EditDataSet", "1", editDataSetProps);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String blockString = actionProps.getProperty("blockflag");
        String qcbatchids = actionProps.getProperty("keyid1");
        if (blockString != null && blockString.length() > 0) {
            this.updateSDIDataBlockFlag(qcbatchids, blockString);
        }
        if (actionProps.containsKey("assignedanalyst")) {
            this.createLESWorkSheetOnQCBatchAssignment(sdiData.getDataset("primary"));
        }
        if (actionProps.containsKey("instrumentid")) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT s.sdcid, s.keyid1, s.paramlistid, s.paramlistversionid, s.variantid, s.dataset, s.s_qcbatchid, q.instrumentid  FROM sdidata s, s_qcbatch q  WHERE s.s_qcbatchid = q.s_qcbatchid and q.s_qcbatchid in( " + safeSQL.addIn(qcbatchids, ";") + ") and q.instrumentid is not null";
            DataSet sdidata = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (sdidata.getRowCount() > 0) {
                PropertyList editDataSetProps = new PropertyList();
                editDataSetProps.setProperty("sdcid", sdidata.getString(0, "sdcid"));
                editDataSetProps.setProperty("keyid1", sdidata.getColumnValues("keyid1", ";"));
                editDataSetProps.setProperty("paramlistid", sdidata.getColumnValues("paramlistid", ";"));
                editDataSetProps.setProperty("paramlistversionid", sdidata.getColumnValues("paramlistversionid", ";"));
                editDataSetProps.setProperty("variantid", sdidata.getColumnValues("variantid", ";"));
                editDataSetProps.setProperty("dataset", sdidata.getColumnValues("dataset", ";"));
                editDataSetProps.setProperty("s_instrumentid", sdidata.getColumnValues("instrumentid", ";"));
                editDataSetProps.setProperty("propsmatch", "Y");
                this.getActionProcessor().processAction("EditDataSet", "1", editDataSetProps);
            }
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.validateQCBatchReview(primary);
    }

    private void validateQCBatchReview(DataSet primary) throws SapphireException {
        String policyValue;
        PropertyList policy = this.getConfigurationProcessor().getPolicy("AQCPolicy", "Sapphire Custom");
        if (policy != null && "Requires Dataset Approval".equalsIgnoreCase(policyValue = policy.getProperty("qcbatchreviewvalidation", "Independent of Dataset Approval"))) {
            StringBuffer qcBatches = new StringBuffer();
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "reviewdisposition") || !"Approved".equalsIgnoreCase(primary.getValue(i, "reviewdisposition"))) continue;
                String batchId = primary.getValue(i, "s_qcbatchid");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select 1 from sdidata where s_qcbatchid = ?  and s_datasetstatus not in ('Completed','Cancelled') ", (Object[])new String[]{batchId});
                if (ds.getRowCount() <= 0) continue;
                qcBatches.append(", ").append(batchId);
            }
            if (qcBatches.length() > 0) {
                throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("To approve following QCBatche(s) requires to be in 'Completed' status") + ": " + qcBatches.substring(1));
            }
        }
    }

    @Override
    public void preDeleteDetail(String rsetid, PropertyList actionProps) throws SapphireException {
        String linkid = actionProps.getProperty("linkid");
        if ("QCBatchItem".equalsIgnoreCase(linkid)) {
            this.removeCalculatedDataItems(rsetid, actionProps);
            this.updateSDIData(actionProps, rsetid);
        }
    }

    private void updateSDIData(PropertyList actionProps, String rsetid) throws SapphireException {
        if (actionProps.getProperty("s_qcbatchitemid", "").trim().length() == 0) {
            this.database.executePreparedUpdate("UPDATE sdidata SET s_qcbatchid = null, s_qcbatchitemid = null WHERE s_qcbatchid IN ( SELECT keyid1 FROM rsetitems WHERE rsetid = ?) ", new Object[]{rsetid});
        } else {
            String[] qcBatchIds = StringUtil.split(actionProps.getProperty("keyid1", ""), ";");
            String[] qcBatchItemIds = StringUtil.split(actionProps.getProperty("s_qcbatchitemid", ""), ";");
            String sql = "UPDATE sdidata set s_qcbatchid = null, s_qcbatchitemid = null WHERE s_qcbatchid = ? AND s_qcbatchitemid = ?";
            PreparedStatement updSDIData = this.database.prepareStatement("updateqcsdidata", sql);
            String updateLinkToSQL = "UPDATE s_qcbatchitem SET linktoqcbatchitemid = null WHERE s_qcbatchid = ? AND linktoqcbatchitemid = ?";
            PreparedStatement updQCBatchItem = this.database.prepareStatement("updateqcbatchitem", updateLinkToSQL);
            int itemCnt = qcBatchItemIds.length;
            int batchCnt = qcBatchIds.length;
            try {
                for (int i = 0; i < itemCnt; ++i) {
                    int k = i >= batchCnt ? batchCnt - 1 : i;
                    updSDIData.setString(1, qcBatchIds[k]);
                    updSDIData.setString(2, qcBatchItemIds[i]);
                    updSDIData.execute();
                    updQCBatchItem.setString(1, qcBatchIds[k]);
                    updQCBatchItem.setString(2, qcBatchItemIds[i]);
                    updQCBatchItem.execute();
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
            finally {
                this.database.closeStatement("updateqcsdidata");
                this.database.closeStatement("updateqcbatchitem");
            }
        }
    }

    private void removeCalculatedDataItems(String rsetid, PropertyList actionProps) throws SapphireException {
        block20: {
            String qcBatchItemIds = "";
            boolean checkBracketItems = false;
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT s_qcbatchid, s_qcbatchitemid, qcbatchsampletypeid, batchitemtype  FROM s_qcbatchitem, rsetitems WHERE s_qcbatchitem.s_qcbatchid = rsetitems.keyid1 AND rsetitems.rsetid = " + safeSQL.addVar(rsetid) + " ";
            qcBatchItemIds = actionProps.getProperty("s_qcbatchitemid", "");
            if (qcBatchItemIds.trim().length() > 0) {
                sql = sql + "AND s_qcbatchitemid IN(" + safeSQL.addIn(qcBatchItemIds, ";") + ")";
                checkBracketItems = true;
            }
            this.database.createPreparedResultSet("qcbItems", sql, safeSQL.getValues());
            DataSet dsBatchItems = new DataSet(this.database.getResultSet("qcbItems"));
            this.database.closeResultSet("qcbItems");
            DataSet deleteDataItems = new DataSet();
            PreparedStatement psmtGetDataItems = this.database.prepareStatement("getdataitems", QCUtil.getBatchItemCalcDataItemsSQL());
            try {
                for (int i = 0; i < dsBatchItems.getRowCount(); ++i) {
                    String qcBatchId = dsBatchItems.getString(i, "s_qcbatchid", "");
                    String batchItemId = dsBatchItems.getString(i, "s_qcbatchitemid", "");
                    String batchSampleTypeId = dsBatchItems.getString(i, "qcbatchsampletypeid", "");
                    String batchItemType = dsBatchItems.getString(i, "batchitemtype", "");
                    com.labvantage.opal.qcbatch.QCBatch qcBatch = QCBatchPool.getQCBatch(this.getQueryProcessor(), qcBatchId);
                    String batchSDCId = qcBatch.getQCBatchSDC();
                    ArrayList<String> pTypeList = new ArrayList<String>();
                    pTypeList.add("BlankCorrected");
                    DataSet ds = new DataSet();
                    if (batchSampleTypeId.length() > 0) {
                        int k;
                        if (checkBracketItems && "Blank".equals(batchItemType)) {
                            QCBatchItem batchItem = qcBatch.getQCBatchItem(batchItemId);
                            QCBatchItem nextQcBatchItem = qcBatch.getNextBatchItem(batchItem);
                            while (nextQcBatchItem != null && !"Blank".equalsIgnoreCase(nextQcBatchItem.getQCSampleType())) {
                                psmtGetDataItems.setString(1, batchSDCId);
                                psmtGetDataItems.setString(2, qcBatchId);
                                psmtGetDataItems.setString(3, nextQcBatchItem.getQCBatchItemID());
                                DataSet bracketDs = new DataSet(psmtGetDataItems.executeQuery());
                                if (bracketDs != null && bracketDs.getRowCount() > 0) {
                                    for (k = 0; k < bracketDs.getRowCount(); ++k) {
                                        if (!"BlankCorrected".equals(bracketDs.getString(k, "paramtype", ""))) continue;
                                        HashMap<String, Object> findMap = new HashMap<String, Object>();
                                        findMap.put("sdcid", bracketDs.getString(k, "sdcid"));
                                        findMap.put("keyid1", bracketDs.getString(k, "keyid1"));
                                        findMap.put("keyid2", bracketDs.getString(k, "keyid2"));
                                        findMap.put("keyid3", bracketDs.getString(k, "keyid3"));
                                        findMap.put("paramlistid", bracketDs.getString(k, "paramlistid"));
                                        findMap.put("paramlistversionid", bracketDs.getString(k, "paramlistversionid"));
                                        findMap.put("variantid", bracketDs.getString(k, "variantid"));
                                        findMap.put("dataset", bracketDs.getBigDecimal(k, "dataset"));
                                        findMap.put("paramid", bracketDs.getString(k, "paramid"));
                                        findMap.put("paramtype", bracketDs.getString(k, "paramtype"));
                                        findMap.put("replicateid", bracketDs.getBigDecimal(k, "replicateid"));
                                        if (deleteDataItems.findRow(findMap) >= 0) continue;
                                        deleteDataItems.copyRow(bracketDs, k, 1);
                                    }
                                }
                                nextQcBatchItem = qcBatch.getNextBatchItem(nextQcBatchItem);
                            }
                        }
                        if (!"Blank".equals(batchItemType)) {
                            QCBatchSampleType bsType = new QCBatchSampleType(this.getQueryProcessor(), batchSampleTypeId);
                            String calcParamType = bsType.getQCParameterType();
                            if (calcParamType != null && calcParamType.length() > 0) {
                                String[] pTypes = StringUtil.split(calcParamType, ";");
                                for (k = 0; k < pTypes.length; ++k) {
                                    if (pTypes[k].trim().length() <= 0) continue;
                                    pTypeList.add(pTypes[k]);
                                }
                            }
                            psmtGetDataItems.setString(1, batchSDCId);
                            psmtGetDataItems.setString(2, qcBatchId);
                            psmtGetDataItems.setString(3, batchItemId);
                            ds = new DataSet(psmtGetDataItems.executeQuery());
                        }
                    } else {
                        psmtGetDataItems.setString(1, batchSDCId);
                        psmtGetDataItems.setString(2, qcBatchId);
                        psmtGetDataItems.setString(3, batchItemId);
                        ds = new DataSet(psmtGetDataItems.executeQuery());
                    }
                    if (ds == null || ds.getRowCount() <= 0) continue;
                    for (int j = 0; j < ds.getRowCount(); ++j) {
                        String paramType = ds.getString(j, "paramtype", "");
                        if (!pTypeList.contains(paramType)) continue;
                        if (ds.getString(j, "enteredtext", "").length() > 0) {
                            throw new SapphireException(this.getTranslationProcessor().translate("Cannot continue. The operation requires deletion of Data Item(s) with results entered."));
                        }
                        HashMap<String, Object> findMap = new HashMap<String, Object>();
                        findMap.put("sdcid", ds.getString(j, "sdcid"));
                        findMap.put("keyid1", ds.getString(j, "keyid1"));
                        findMap.put("keyid2", ds.getString(j, "keyid2"));
                        findMap.put("keyid3", ds.getString(j, "keyid3"));
                        findMap.put("paramlistid", ds.getString(j, "paramlistid"));
                        findMap.put("paramlistversionid", ds.getString(j, "paramlistversionid"));
                        findMap.put("variantid", ds.getString(j, "variantid"));
                        findMap.put("dataset", ds.getBigDecimal(j, "dataset"));
                        findMap.put("paramid", ds.getString(j, "paramid"));
                        findMap.put("paramtype", ds.getString(j, "paramtype"));
                        findMap.put("replicateid", ds.getBigDecimal(j, "replicateid"));
                        if (deleteDataItems.findRow(findMap) >= 0) continue;
                        deleteDataItems.copyRow(ds, j, 1);
                    }
                }
                if (deleteDataItems == null || deleteDataItems.getRowCount() <= 0) break block20;
                PropertyList delProps = new PropertyList();
                delProps.setProperty("sdcid", deleteDataItems.getString(0, "sdcid"));
                delProps.setProperty("keyid1", deleteDataItems.getColumnValues("keyid1", ";"));
                delProps.setProperty("keyid2", deleteDataItems.getColumnValues("keyid2", ";"));
                delProps.setProperty("keyid3", deleteDataItems.getColumnValues("keyid3", ";"));
                delProps.setProperty("paramlistid", deleteDataItems.getColumnValues("paramlistid", ";"));
                delProps.setProperty("paramlistversionid", deleteDataItems.getColumnValues("paramlistversionid", ";"));
                delProps.setProperty("variantid", deleteDataItems.getColumnValues("variantid", ";"));
                delProps.setProperty("dataset", deleteDataItems.getColumnValues("dataset", ";"));
                delProps.setProperty("paramid", deleteDataItems.getColumnValues("paramid", ";"));
                delProps.setProperty("paramtype", deleteDataItems.getColumnValues("paramtype", ";"));
                delProps.setProperty("replicateid", deleteDataItems.getColumnValues("replicateid", ";"));
                try {
                    this.getActionProcessor().processAction("DeleteDataItem", "1", delProps);
                }
                catch (SapphireException ex) {
                    throw new SapphireException(ex);
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
            finally {
                this.database.closeStatement("getdataitems");
            }
        }
    }

    void createLESWorkSheetOnQCBatchAssignment(DataSet dsPrimary) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT m.createlesrule, sdiworksheetrule.authorflag, sdiworksheetrule.worksheetid, sdiworksheetrule.worksheetversionid, sdiworksheetrule.workbookid, sdiworksheetrule.workbookversionid, ").append(" sdiworksheetrule.worksheetrule FROM s_qcmethod m, s_qcbatch b, sdiworksheetrule ").append(" WHERE sdiworksheetrule.sdcid = ? AND sdiworksheetrule.keyid1 = m.s_qcmethodid AND sdiworksheetrule.keyid2 = m.s_qcmethodversionid ").append(" AND  m.s_qcmethodid = b.qcmethodid AND  m.s_qcmethodversionid =  b.qcmethodversionid  AND m.createlesrule = ? AND sdiworksheetrule.worksheetrule = ? AND b.s_qcbatchid = ?");
        for (int i = 0; i < dsPrimary.size(); ++i) {
            String qcbatchId;
            int wsCnt;
            String assignedto = dsPrimary.getValue(i, "assignedanalyst");
            if (!this.hasPrimaryValueChanged(dsPrimary, i, "assignedanalyst") || assignedto.length() <= 0 || (wsCnt = this.database.getPreparedCount("select 1 from worksheetsdi where sdcid = ? and keyid1 = ?", new String[]{"QCBatch", qcbatchId = dsPrimary.getValue(i, "s_qcbatchid")})) >= 1) continue;
            DataSet dsTemplate = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{"QCMethod", "On Assignment", "default", qcbatchId});
            if (dsTemplate.getRowCount() == 1) {
                PropertyList genWSActionProps = new PropertyList();
                genWSActionProps.setProperty("qcbatchid", qcbatchId);
                genWSActionProps.setProperty("templateid", dsTemplate.getString(0, "worksheetid"));
                genWSActionProps.setProperty("templateversionid", dsTemplate.getString(0, "worksheetversionid"));
                genWSActionProps.setProperty("workbookid", dsTemplate.getString(0, "workbookid"));
                genWSActionProps.setProperty("workbookversionid", dsTemplate.getString(0, "workbookversionid"));
                this.getActionProcessor().processAction("GenerateQCBatchWorksheet", "1", genWSActionProps);
                continue;
            }
            if (dsTemplate.getRowCount() <= 1) continue;
            throw new SapphireException(this.getTranslationProcessor().translate("More than one Worksheet Template found with the matching criteria. On Analyst assignment, QCBatch Worksheet could not be created."));
        }
    }
}

