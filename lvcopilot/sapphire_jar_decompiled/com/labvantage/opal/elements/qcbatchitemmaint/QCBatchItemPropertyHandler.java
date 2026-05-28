/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.qcbatchitemmaint;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.elements.detailmaint.BaseItem;
import com.labvantage.opal.elements.detailmaint.DBColumn;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchItem;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class QCBatchItemPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 102200 $";
    private static final String QCSAMPLEIDENTIFIER = "(Auto)";
    private boolean isOra;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void saveData() throws SapphireException {
        block86: {
            Exception exception = null;
            DataSet dsNewQCSamples = new DataSet();
            dsNewQCSamples.addColumn("keyid1", 0);
            dsNewQCSamples.addColumn("qcbatchsampletypeid", 0);
            dsNewQCSamples.addColumn("applyaction", 0);
            dsNewQCSamples.addColumn("linktoqcbatchitemid", 0);
            dsNewQCSamples.addColumn("linkedto", 0);
            dsNewQCSamples.addColumn("qcbatchitemid", 0);
            dsNewQCSamples.addColumn("linkedrowid", 1);
            dsNewQCSamples.addColumn("added", 0);
            StringBuffer keycols = new StringBuffer();
            this.isOra = this.connectionInfo.isOracle();
            String _tableid = (String)this._ElementProps.get("tableid");
            String _sdcid = (String)this._ElementProps.get("sdcid");
            ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
            ElementData elementData = new ElementData(elementColumns, this._Edata);
            String qcBatchId = this.getKeyid1();
            DBUtil db = new DBUtil();
            db.setConnection(this.sapphireConnection);
            String paramListType = "";
            String qcBatchSDCId = "";
            String qcBatchStatus = "";
            String batchSecurityDept = "";
            SafeSQL safeSQL = new SafeSQL();
            PreparedStatement psmtGetDataItems = db.prepareStatement("getdataitems", QCUtil.getBatchItemCalcDataItemsSQL());
            SDCProcessor sdcp = this.getSdcProcessor();
            boolean copySecurityDeptToSample = false;
            QCBatch qcBatch = QCBatchPool.getQCBatch(this.getQueryProcessor(), qcBatchId);
            List qcBatchItems = qcBatch.getQCBatchItems();
            paramListType = qcBatch.getParamListType();
            qcBatchSDCId = qcBatch.getQCBatchSDC();
            qcBatchStatus = qcBatch.getStatus();
            String qcbatchSiteId = qcBatch.getSiteDepartmentId();
            String userSiteId = OpalUtil.getSiteIdFromUserDefaultDepartment(this.connectionInfo, db);
            String sampleSiteDepartmentId = "";
            if (userSiteId != null && userSiteId.length() > 0) {
                sampleSiteDepartmentId = userSiteId;
            } else if (qcbatchSiteId.length() > 0) {
                sampleSiteDepartmentId = qcbatchSiteId;
            }
            ArrayList<String> bracketBatchItemIds = new ArrayList<String>();
            for (int lst = 0; lst < qcBatchItems.size(); ++lst) {
                QCBatchItem qcBatchItem = (QCBatchItem)qcBatchItems.get(lst);
                if (qcBatchItem == null || "Blank".equals(qcBatchItem.getQCSampleType()) || !QCUtil.inBlankBracket(qcBatch, qcBatchItem)) continue;
                bracketBatchItemIds.add(qcBatchItem.getQCBatchItemID());
            }
            if (sdcp.getPropertyList(qcBatchSDCId).getProperty("accesscontrolledflag").equals("D") && sdcp.getPropertyList("QCBatch").getProperty("accesscontrolledflag").equals("D")) {
                copySecurityDeptToSample = QCUtil.copySecurityDeptToSample(this.getConfigurationProcessor());
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select securitydepartment from s_qcbatch where s_qcbatchid = ?", (Object[])new String[]{qcBatch.getQCBatchID()});
                batchSecurityDept = ds.getValue(0, "securitydepartment");
            }
            boolean addedNewSample = false;
            String strRemoveKeys = "";
            boolean removedItem = false;
            boolean updatedItem = false;
            QueryProcessor qp = this.getQueryProcessor();
            ActionProcessor ap = this.getActionProcessor();
            BaseDetailPropertyHandler.Pair masterKey = new BaseDetailPropertyHandler.Pair((String)this._ElementProps.get("masterkeys"));
            List masterkeylist = masterKey.getKeysAsList();
            BaseDetailPropertyHandler.Key detailKey = new BaseDetailPropertyHandler.Key((String)this._ElementProps.get("detailkeys"));
            keycols.append(masterKey.getKeysAsString(";")).append(";").append(detailKey.getKeys());
            List<String> customColumnsList = OpalUtil.toList((String)this._ElementProps.get("customcolumns"), ";");
            List<String> defaultValueList = OpalUtil.toList((String)this._ElementProps.get("defaultvalues"), ";");
            for (String column : customColumnsList) {
                if (column == null || column.equals("keyid1")) continue;
                elementData.removeColumn(column);
            }
            elementData.removeAll(masterkeylist);
            elementData.removeColumn("createdt");
            elementData.removeColumn("createby");
            elementData.removeColumn("createtool");
            elementData.removeColumn("moddt");
            elementData.removeColumn("modby");
            elementData.removeColumn("modtool");
            List linktoList = elementData.getDataList("linktoqcbatchitemid");
            for (int listCounter = 0; listCounter < elementData.size(); ++listCounter) {
                if (elementData.getColumnData(listCounter, "__status") != null && elementData.getColumnData(listCounter, "__status").equalsIgnoreCase("N")) {
                    String nextSequence = OpalUtil.getNextSequence(_tableid, this.getSequenceProcessor());
                    elementData.setColumnValue(listCounter, "s_qcbatchitemid", nextSequence);
                    addedNewSample = true;
                }
                if (elementData.getColumnData(listCounter, "qcbatchitemdesc") == null || elementData.getColumnData(listCounter, "qcbatchitemdesc").length() != 0) continue;
                elementData.setColumnValue(listCounter, "qcbatchitemdesc", "(null)");
            }
            List columnsList = elementData.getColumnList();
            if (this._TableMD.doesColumnExists("usersequence") && columnsList.contains("usersequence")) {
                String[] userSequence;
                String[] stringArray = userSequence = elementData.getColumnDataBuffer("usersequence", ";", "E") != null && !elementData.getColumnDataBuffer("usersequence", ";", "E").trim().equals("") ? StringUtil.split(elementData.getColumnDataBuffer("usersequence", ";", "E"), ";", true) : null;
                if (userSequence != null && userSequence.length > 0) {
                    for (int i = 0; i < userSequence.length; ++i) {
                        for (int j = 0; j < linktoList.size(); ++j) {
                            if (!((String)linktoList.get(j)).trim().equals(userSequence[i]) || elementData.getColumnData(j, "__status") != null && elementData.getColumnData(j, "__status").equalsIgnoreCase("N")) continue;
                            elementData.setColumnValue(j, "__status", "E");
                        }
                    }
                }
            } else if (this._TableMD.doesColumnExists("usersequence") && !columnsList.contains("usersequence")) {
                elementData.addColumn("usersequence");
                for (int listCounter = 0; listCounter < elementData.size(); ++listCounter) {
                    String displayUserSequence = Integer.toString(listCounter + 1);
                    elementData.setColumnValue(listCounter, "usersequence", displayUserSequence);
                    if (elementData.getColumnData(listCounter, "__status").trim().equals("N") || elementData.getColumnData(listCounter, "__status").trim().equals("E")) continue;
                    elementData.setColumnValue(listCounter, "__status", "E");
                }
            }
            HashMap<String, Integer> newSampleDSMap = new HashMap<String, Integer>();
            for (int listCounter = 0; listCounter < elementData.size(); ++listCounter) {
                int row = -1;
                String columnData_qcbatchsampletypeid = elementData.getColumnData(listCounter, "qcbatchsampletypeid");
                String columnData_keyid1 = elementData.getColumnData(listCounter, "keyid1");
                String columnData_linkedto = elementData.getColumnData(listCounter, "linkedto");
                if (columnData_linkedto == null) {
                    columnData_linkedto = "";
                } else if (columnData_linkedto.length() > 0) {
                    columnData_linkedto = columnData_linkedto.trim();
                }
                int index_qcbatchsampletypeid = columnsList.indexOf("qcbatchsampletypeid");
                String defaultValue = String.valueOf(defaultValueList.get(index_qcbatchsampletypeid));
                String applyActionId = "";
                if (columnData_qcbatchsampletypeid != null && defaultValue != null) {
                    if (columnData_qcbatchsampletypeid.equalsIgnoreCase(defaultValue)) {
                        elementData.setColumnValue(listCounter, "qcbatchsampletypeid", "");
                    } else if (!columnData_qcbatchsampletypeid.equalsIgnoreCase(defaultValue) && columnData_keyid1 != null && columnData_keyid1.startsWith(QCSAMPLEIDENTIFIER)) {
                        String returnString = this.createQCSample(columnData_qcbatchsampletypeid, this.getKeyid1(), batchSecurityDept, sampleSiteDepartmentId);
                        row = dsNewQCSamples.addRow();
                        dsNewQCSamples.setNumber(row, "linkedrowid", -1);
                        dsNewQCSamples.setString(row, "added", "N");
                        int semicolonidx = returnString.indexOf(";");
                        String newKeyId1 = returnString.substring(0, semicolonidx);
                        applyActionId = returnString.substring(semicolonidx + 1);
                        elementData.setColumnValue(listCounter, "keyid1", newKeyId1);
                        dsNewQCSamples.setValue(row, "applyaction", applyActionId);
                        dsNewQCSamples.setValue(row, "qcbatchsampletypeid", columnData_qcbatchsampletypeid);
                        dsNewQCSamples.setValue(row, "qcbatchitemid", elementData.getColumnData(listCounter, "s_qcbatchitemid"));
                        if (newKeyId1 != null && newKeyId1.trim().length() > 0) {
                            dsNewQCSamples.setValue(row, "keyid1", newKeyId1);
                            newSampleDSMap.put(newKeyId1, new Integer(row));
                        }
                    }
                }
                if (columnData_linkedto == null || columnData_linkedto.length() <= 0) continue;
                int rowIndex = -1;
                char positionTypeChar = columnData_linkedto.charAt(0);
                String linkToColumnValue = " ";
                int linkPosition = -1;
                switch (positionTypeChar) {
                    case '+': {
                        try {
                            linkPosition = Integer.parseInt(columnData_linkedto.substring(1));
                        }
                        catch (NumberFormatException numberFormatException) {
                            if (Trace.on) {
                                Trace.log("Relative NumberformatException : " + numberFormatException.getMessage());
                            }
                            throw new SapphireException("Relative NumberformatException : " + ErrorUtil.extractMessageFromException(numberFormatException, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
                        }
                        rowIndex = listCounter + linkPosition;
                        break;
                    }
                    case '-': {
                        if (linkPosition == -1) {
                            try {
                                linkPosition = Integer.parseInt(columnData_linkedto);
                            }
                            catch (NumberFormatException numberFormatException) {
                                if (Trace.on) {
                                    Trace.log("Relative NumberformatException : " + numberFormatException.getMessage());
                                }
                                throw new SapphireException("Relative NumberformatException : " + ErrorUtil.extractMessageFromException(numberFormatException, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
                            }
                        }
                        rowIndex = listCounter + linkPosition;
                        break;
                    }
                    default: {
                        try {
                            rowIndex = Integer.parseInt(columnData_linkedto);
                            --rowIndex;
                            break;
                        }
                        catch (NumberFormatException numberFormatException) {
                            if (Trace.on) {
                                Trace.log("Absolute NumberformatException : " + numberFormatException.getMessage());
                            }
                            throw new SapphireException("Absolute NumberformatException : " + ErrorUtil.extractMessageFromException(numberFormatException, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
                        }
                    }
                }
                String linkedSampleId = "";
                if (rowIndex >= 0 && rowIndex != listCounter && rowIndex < elementData.size()) {
                    linkToColumnValue = elementData.getColumnData(rowIndex, "s_qcbatchitemid");
                    linkedSampleId = elementData.getColumnData(rowIndex, "keyid1");
                } else if (rowIndex == listCounter) {
                    linkToColumnValue = "";
                }
                elementData.setColumnValue(listCounter, "linktoqcbatchitemid", linkToColumnValue.trim());
                if (row != -1) {
                    dsNewQCSamples.setValue(row, "linktoqcbatchitemid", linkToColumnValue.trim());
                    dsNewQCSamples.setValue(row, "linkedto", columnData_linkedto);
                    if (rowIndex >= 0) {
                        dsNewQCSamples.setNumber(row, "linkedrowid", rowIndex);
                    }
                }
                if (elementData.getColumnData(listCounter, "keyid1") != null && !elementData.getColumnData(listCounter, "keyid1").trim().equals("")) continue;
                elementData.setColumnValue(listCounter, "keyid1", linkedSampleId);
                if (row == -1 || !dsNewQCSamples.getValue(row, "keyid1", "").equals("")) continue;
                dsNewQCSamples.setValue(row, "keyid1", linkedSampleId);
            }
            StringBuffer sdidataTableUpdateSQL = new StringBuffer();
            try {
                StringBuffer checkDataSetSQL;
                StringBuffer whereclause;
                block85: {
                    whereclause = new StringBuffer();
                    whereclause.append(masterKey.getWhereClause(elementData));
                    checkDataSetSQL = new StringBuffer();
                    try {
                        strRemoveKeys = HttpUtil.decodeURIComponent((String)this._ElementProps.get("eremove"));
                    }
                    catch (Exception e) {
                        if (!Trace.on) break block85;
                        Trace.log(e.getMessage());
                    }
                }
                String[] removekey = StringUtil.split(strRemoveKeys, ";");
                DataSet deleteDataItems = new DataSet();
                String deletedBatchItems = "";
                for (int i = 0; i < removekey.length - 1; ++i) {
                    String _removekey = removekey[i];
                    if (_removekey == null || _removekey.trim().length() <= 0) continue;
                    sdidataTableUpdateSQL.delete(0, sdidataTableUpdateSQL.length());
                    safeSQL.reset();
                    sdidataTableUpdateSQL.append(" WHERE ");
                    sdidataTableUpdateSQL.append(whereclause.toString());
                    if (_removekey.indexOf("|") == -1) {
                        for (int j = 0; j < detailKey.size(); ++j) {
                            sdidataTableUpdateSQL.append(" AND ");
                            sdidataTableUpdateSQL.append((String)detailKey.get(j));
                            sdidataTableUpdateSQL.append(" = ").append(safeSQL.addVar(_removekey)).append("");
                        }
                    } else {
                        String[] _removekeyarray = StringUtil.split(_removekey, "|");
                        for (int j = 0; j < detailKey.size(); ++j) {
                            sdidataTableUpdateSQL.append(" AND ");
                            sdidataTableUpdateSQL.append((String)detailKey.get(j));
                            sdidataTableUpdateSQL.append(" = ").append(safeSQL.addVar(_removekeyarray[j])).append("");
                        }
                    }
                    checkDataSetSQL.setLength(0);
                    checkDataSetSQL.append("SELECT count(keyid1) FROM sdidata ").append(sdidataTableUpdateSQL.substring(sdidataTableUpdateSQL.indexOf("WHERE")) + " AND s_datasetstatus not in ('Initial', 'Cancelled')");
                    if (db.getPreparedCount(checkDataSetSQL.toString(), safeSQL.getValues()) > 0) {
                        throw new SapphireException(this.getTranslationProcessor().translate("Selected Items are found with results entered and could not be deleted!"));
                    }
                    deletedBatchItems = deletedBatchItems + ";" + removekey[i];
                    removedItem = true;
                }
                if (removedItem && deletedBatchItems.length() > 0) {
                    PropertyList deleteSDIDetail = new PropertyList();
                    deleteSDIDetail.setProperty("linkid", "QCBatchItem");
                    deleteSDIDetail.setProperty("sdcid", "QCBatch");
                    deleteSDIDetail.setProperty("keyid1", qcBatchId);
                    deleteSDIDetail.setProperty("s_qcbatchitemid", deletedBatchItems.substring(1));
                    ap.processAction("DeleteSDIDetail", "1", deleteSDIDetail);
                }
                PropertyList insertProp = new PropertyList();
                PropertyList updateProp = new PropertyList();
                List columnList = elementData.getColumnList();
                for (int i = 0; i < elementData.size(); ++i) {
                    String qcbatchsampletypeid = elementData.getColumnData(i, "qcbatchsampletypeid");
                    String batchItemType = elementData.getColumnData(i, "batchitemtype");
                    if ("Unknown".equalsIgnoreCase(batchItemType)) {
                        elementData.setColumnValue(i, "qcbatchsampletypeid", "");
                        continue;
                    }
                    if ("Unknown".equalsIgnoreCase(batchItemType) || qcbatchsampletypeid != null && !qcbatchsampletypeid.trim().equals("")) continue;
                    DataSet dsQCBsampleTypeId = this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_qcbatchsampletypeid FROM s_qcbatchsampletype WHERE qcsampletype = " + safeSQL.addVar(batchItemType) + " and qcbatchid = " + safeSQL.addVar(qcBatchId), safeSQL.getValues());
                    safeSQL.reset();
                    if (dsQCBsampleTypeId == null || dsQCBsampleTypeId.size() <= 0) continue;
                    elementData.setColumnValue(i, "qcbatchsampletypeid", dsQCBsampleTypeId.getValue(0, "s_qcbatchsampletypeid", ""));
                }
                boolean editedRowExists = elementData.getStatusRowCount("E") > 0;
                boolean isInsert = false;
                boolean isUpdate = false;
                DataSet dataSet = new DataSet();
                for (int i = 0; i < columnList.size(); ++i) {
                    String columnid = (String)columnList.get(i);
                    String columndatatype = this._TableMD.getDataType(columnid);
                    if (columnid.equals("__status") || columnid.equals("keyid1")) continue;
                    String insertvaluelist = elementData.getColumnDataBuffer(columnid, ";", "N");
                    String updatevaluelist = elementData.getColumnDataBuffer(columnid, ";", "E");
                    if (!insertvaluelist.equals("")) {
                        insertProp.setProperty(columnid, insertvaluelist);
                        isInsert = true;
                    }
                    if (!editedRowExists) continue;
                    updateProp.setProperty(columnid, updatevaluelist);
                    isUpdate = true;
                }
                if (isInsert) {
                    if (this._TableMD.doesColumnExists("usersequence")) {
                        insertProp.setProperty("usersequence", elementData.getSequenceBuffer("N", ";"));
                    }
                    insertProp.setProperty("sdcid", _sdcid);
                    insertProp.setProperty("keyid1", qcBatchId);
                    insertProp.setProperty("linkid", "QCBatchItem");
                    ap.processAction("AddSDIDetail", "1", insertProp);
                    dataSet = new DataSet("s_qcbatchid", insertProp.getProperty("s_qcbatchid", ""));
                    dataSet.addColumnValues("s_qcbatchitemid", 0, insertProp.getProperty("s_qcbatchitemid", ""), ";");
                    try {
                        DataSet dsUnknown;
                        String[] insertedSamples;
                        String insertedSamplesBuffer = elementData.getColumnDataBuffer("keyid1", ";", "N");
                        boolean matchWorkItemItem = QCUtil.matchSDIWorkItemItem(this.getConfigurationProcessor());
                        if (insertedSamplesBuffer != null && insertedSamplesBuffer.length() > 0 && (insertedSamples = StringUtil.split(insertedSamplesBuffer, ";", true)) != null && insertedSamples.length > 0 && (dsUnknown = this.getUnknownDataSets(qcBatchId, qcBatchSDCId, paramListType, insertedSamplesBuffer, matchWorkItemItem)) != null && dsUnknown.size() > 0) {
                            dsUnknown.addColumn("S_QCBATCHID", 0);
                            dsUnknown.addColumn("S_QCBATCHITEMID", 0);
                            int i = 0;
                            while (i < dsUnknown.size()) {
                                String sampleId = dsUnknown.getValue(i, "keyid1").trim();
                                boolean isSampleIdInserted = false;
                                for (int j = 0; j < insertedSamples.length; ++j) {
                                    if (!insertedSamples[j].equals(sampleId)) continue;
                                    String qcBatchItemId = dataSet.getValue(j, "s_qcbatchitemid");
                                    dsUnknown.setObject(i, "S_QCBATCHITEMID", qcBatchItemId);
                                    dsUnknown.setObject(i, "S_QCBATCHID", qcBatchId);
                                    isSampleIdInserted = true;
                                    break;
                                }
                                if (isSampleIdInserted) {
                                    ++i;
                                    continue;
                                }
                                dsUnknown.deleteRow(i);
                            }
                            if (dsUnknown.size() > 0) {
                                PropertyList unknownsEdit = new PropertyList();
                                unknownsEdit.setProperty("propsmatch", "Y");
                                unknownsEdit.setProperty("sdcid", qcBatchSDCId);
                                unknownsEdit.setProperty("keyid1", dsUnknown.getColumnValues("keyid1", ";"));
                                unknownsEdit.setProperty("paramlistid", dsUnknown.getColumnValues("paramlistid", ";"));
                                unknownsEdit.setProperty("paramlistversionid", dsUnknown.getColumnValues("paramlistversionid", ";"));
                                unknownsEdit.setProperty("variantid", dsUnknown.getColumnValues("variantid", ";"));
                                unknownsEdit.setProperty("dataset", dsUnknown.getColumnValues("dataset", ";"));
                                unknownsEdit.setProperty("s_qcbatchid", dsUnknown.getColumnValues("S_QCBATCHID", ";"));
                                unknownsEdit.setProperty("s_qcbatchitemid", dsUnknown.getColumnValues("S_QCBATCHITEMID", ";"));
                                ap.processAction("EditDataSet", "1", unknownsEdit);
                            }
                        }
                    }
                    catch (Exception e) {
                        throw new SapphireException("Dataset failure");
                    }
                }
                if (dsNewQCSamples.size() > 0) {
                    int idx;
                    if (qcBatchSDCId == null || qcBatchSDCId.trim().equals("")) {
                        qcBatchSDCId = "Sample";
                    }
                    DataSet dsOrderOfExec = new DataSet();
                    for (idx = 0; idx < dsNewQCSamples.size(); ++idx) {
                        String sampleID = dsNewQCSamples.getValue(idx, "keyid1");
                        int linkedRowID = dsNewQCSamples.getInt(idx, "linkedrowid");
                        String linkedSampleID = "";
                        if (linkedRowID <= -1) continue;
                        linkedSampleID = elementData.getColumnData(linkedRowID, "keyid1");
                        if (sampleID != null && !sampleID.trim().equals("") && !sampleID.equals(QCSAMPLEIDENTIFIER)) continue;
                        sampleID = linkedSampleID;
                        dsNewQCSamples.setValue(idx, "keyid1", sampleID);
                    }
                    for (idx = 0; idx < dsNewQCSamples.size(); ++idx) {
                        this.addInSequence(elementData, dsNewQCSamples, dsOrderOfExec, idx, newSampleDSMap);
                    }
                    this.callApplyAction(dsOrderOfExec, qcBatchId, qcBatchSDCId);
                }
                if (isInsert) {
                    QCUtil.updateReagent(this.getQueryProcessor(), db, dataSet);
                    dataSet.clear();
                }
                if (isUpdate && elementData.getColumnDataBuffer("s_qcbatchid", ";", "E") != null && elementData.getColumnDataBuffer("s_qcbatchid", ";", "E").trim().length() != 0) {
                    if (this._TableMD.doesColumnExists("usersequence")) {
                        updateProp.put("usersequence", elementData.getSequenceBuffer("E", ";"));
                    }
                    updateProp.put("sdcid", _sdcid);
                    updateProp.put("keyid1", qcBatchId);
                    updateProp.put("linkid", "QCBatchItem");
                    ap.processAction("EditSDIDetail", "1", updateProp);
                    dataSet = new DataSet("s_qcbatchitemid", updateProp.getProperty("s_qcbatchitemid", ""));
                    updatedItem = true;
                }
                if (removedItem || addedNewSample || updatedItem) {
                    qcBatch = new QCBatch(this.getQueryProcessor(), qcBatchId);
                    QCBatchPool.renewQCBatchInPool(qcBatch);
                    qcBatchStatus = qcBatch.getStatus();
                }
                psmtGetDataItems.setString(1, qcBatchSDCId);
                psmtGetDataItems.setString(2, qcBatchId);
                for (int k = 0; k < dataSet.getRowCount(); ++k) {
                    String batchItemId = dataSet.getString(k, "s_qcbatchitemid", "");
                    QCBatchItem qcBatchItem = qcBatch.getQCBatchItem(batchItemId);
                    if ("Blank".equals(qcBatchItem.getQCSampleType()) || !bracketBatchItemIds.contains(batchItemId) || QCUtil.inBlankBracket(qcBatch, qcBatchItem)) continue;
                    psmtGetDataItems.setString(3, batchItemId);
                    DataSet ds = this.getDataItemsToRemove(psmtGetDataItems);
                    if (ds == null || ds.getRowCount() <= 0) continue;
                    for (int j = 0; j < ds.getRowCount(); ++j) {
                        if (!"BlankCorrected".equals(ds.getString(j, "paramtype", ""))) continue;
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
                dataSet.clear();
                if (deleteDataItems != null && deleteDataItems.getRowCount() > 0) {
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("sdcid", deleteDataItems.getString(0, "sdcid"));
                    actionProps.setProperty("keyid1", deleteDataItems.getColumnValues("keyid1", ";"));
                    actionProps.setProperty("keyid2", deleteDataItems.getColumnValues("keyid2", ";"));
                    actionProps.setProperty("keyid3", deleteDataItems.getColumnValues("keyid3", ";"));
                    actionProps.setProperty("paramlistid", deleteDataItems.getColumnValues("paramlistid", ";"));
                    actionProps.setProperty("paramlistversionid", deleteDataItems.getColumnValues("paramlistversionid", ";"));
                    actionProps.setProperty("variantid", deleteDataItems.getColumnValues("variantid", ";"));
                    actionProps.setProperty("dataset", deleteDataItems.getColumnValues("dataset", ";"));
                    actionProps.setProperty("paramid", deleteDataItems.getColumnValues("paramid", ";"));
                    actionProps.setProperty("paramtype", deleteDataItems.getColumnValues("paramtype", ";"));
                    actionProps.setProperty("replicateid", deleteDataItems.getColumnValues("replicateid", ";"));
                    try {
                        ap.processAction("DeleteDataItem", "1", actionProps);
                    }
                    catch (SapphireException ex) {
                        throw new SapphireException(ex);
                    }
                }
                if (updatedItem || addedNewSample || removedItem) {
                    QCUtil.addCalcDataItems(qcBatchId, qp, ap);
                }
                if (removedItem || addedNewSample && (qcBatchStatus.equalsIgnoreCase("Evaluated") || qcBatchStatus.equalsIgnoreCase("DataEntered") || qcBatchStatus.equalsIgnoreCase("Completed") || qcBatchStatus.equalsIgnoreCase("Reviewed"))) {
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("qcbatchid", qcBatchId);
                    props.put("postdataentry", "N");
                    props.put("tracelogid", this._TraceLogId);
                    this.getActionProcessor().processAction("UpdateQCBatchStatus", "1", props);
                }
            }
            catch (Exception e) {
                exception = e;
                Logger.logError("QCBatchItemPropertyHandler error: ", e);
            }
            finally {
                db.reset();
                if (exception == null) break block86;
                throw new SapphireException(exception);
            }
        }
    }

    protected String createQCSample(String qcBatchSampleTypeId, String qcBatchId, String batchSecurityDept, String sampleSiteId) throws SapphireException {
        String applyActionId = null;
        String newKeyId1 = null;
        String selectedDSSQL = null;
        SafeSQL safeSQL = new SafeSQL();
        String fromClause = " s_qcbatchsampletype ";
        StringBuffer whereClause = new StringBuffer();
        ArrayList<DBColumn> columns = new ArrayList<DBColumn>();
        ArrayList<String> whereList = new ArrayList<String>();
        DataSet qcBatchSampleTypeDS = null;
        HashMap sampleDetails = new HashMap();
        sampleDetails.put("qcbatchid", qcBatchId);
        sampleDetails.put("resolvelink", "N");
        sampleDetails.put("qcbatchsampletypeid", qcBatchSampleTypeId);
        whereClause.append(" S_QCBATCHSAMPLETYPEID = ");
        whereClause.append(safeSQL.addVar(qcBatchSampleTypeId));
        whereList.add(whereClause.toString());
        columns.add(0, new DBColumn("actionapply", false, this.isOra));
        columns.add(0, new DBColumn("qcsampletype", false, this.isOra));
        selectedDSSQL = BaseItem.getSelectSQL(columns, fromClause, whereList, null, false);
        qcBatchSampleTypeDS = this.getQueryProcessor().getPreparedSqlDataSet(selectedDSSQL, safeSQL.getValues());
        if (qcBatchSampleTypeDS != null && qcBatchSampleTypeDS.getRowCount() == 1) {
            applyActionId = qcBatchSampleTypeDS.getValue(0, "actionapply");
        }
        if (applyActionId == null || applyActionId.trim().equals("")) {
            if (Trace.on) {
                Trace.log("Apply actionid not available for QC Sample Creation;");
            }
            throw new SapphireException("Apply actionid not available for QC Sample Creation");
        }
        sampleDetails.put("qcsampletype", qcBatchSampleTypeDS.getValue(0, "qcsampletype"));
        qcBatchSampleTypeDS = null;
        columns = null;
        whereList = null;
        if (batchSecurityDept.length() > 0) {
            sampleDetails.put("securitydepartment", batchSecurityDept);
        }
        if (sampleSiteId.length() > 0) {
            sampleDetails.put("sitedepartmentid", sampleSiteId);
        }
        sampleDetails = this.executeAction(applyActionId, "1", sampleDetails);
        newKeyId1 = (String)sampleDetails.get("newkeyid1");
        sampleDetails = null;
        return newKeyId1 + ";" + applyActionId;
    }

    private void addInSequence(ElementData elementData, DataSet dsNewQCSamples, DataSet dsOrderOfExec, int idx, HashMap newSampleDSMap) {
        String linkedSampleID;
        int linkedRowID = dsNewQCSamples.getInt(idx, "linkedrowid");
        if (linkedRowID > -1 && newSampleDSMap.containsKey(linkedSampleID = elementData.getColumnData(linkedRowID, "keyid1"))) {
            int row = (Integer)newSampleDSMap.get(linkedSampleID);
            this.addInSequence(elementData, dsNewQCSamples, dsOrderOfExec, row, newSampleDSMap);
        }
        if (dsNewQCSamples.getString(idx, "added", "").equals("N")) {
            dsOrderOfExec.copyRow(dsNewQCSamples, idx, 1);
            dsNewQCSamples.setString(idx, "added", "Y");
        }
    }

    protected void callApplyAction(String actionId, HashMap properties) throws SapphireException {
        if (actionId != null && !actionId.equals("")) {
            this.executeAction(actionId, "1", properties);
        }
    }

    private void callApplyAction(DataSet ds, String qcBatchId, String qcBatchSDCId) throws SapphireException {
        StringBuffer keyids = new StringBuffer();
        StringBuffer qcbatchitemids = new StringBuffer();
        StringBuffer linkedtos = new StringBuffer();
        StringBuffer linktobatchitemids = new StringBuffer();
        for (int idx = 0; idx < ds.size(); ++idx) {
            String currentBatchSampleTypeId = ds.getValue(idx, "qcbatchsampletypeid");
            String applyAction = ds.getValue(idx, "applyaction");
            String sampleID = ds.getValue(idx, "keyid1");
            keyids.append(sampleID).append(";");
            qcbatchitemids.append(ds.getValue(idx, "qcbatchitemid")).append(";");
            linkedtos.append(ds.getValue(idx, "linkedto")).append(";");
            linktobatchitemids.append(ds.getValue(idx, "linktoqcbatchitemid")).append(";");
            if (idx != ds.size() - 1 && ds.getValue(idx + 1, "qcbatchsampletypeid", "").equals(currentBatchSampleTypeId) || keyids.length() <= 0) continue;
            HashMap<String, String> sampleDetails = new HashMap<String, String>();
            sampleDetails.put("qcbatchsampletypeid", currentBatchSampleTypeId);
            sampleDetails.put("qcbatchid", qcBatchId);
            sampleDetails.put("qcbatchitemid", qcbatchitemids.substring(0, qcbatchitemids.toString().length() - 1));
            sampleDetails.put("resolvelink", "Y");
            sampleDetails.put("sdcid", qcBatchSDCId);
            sampleDetails.put("keyid1", keyids.substring(0, keyids.toString().length() - 1));
            sampleDetails.put("linktoqcbatchitemid", linktobatchitemids.substring(0, linktobatchitemids.toString().length() - 1));
            sampleDetails.put("linkedto", linkedtos.substring(0, linkedtos.toString().length() - 1));
            this.callApplyAction(applyAction, sampleDetails);
            keyids.setLength(0);
            linkedtos.setLength(0);
            linktobatchitemids.setLength(0);
            qcbatchitemids.setLength(0);
        }
    }

    private DataSet getDataItemsToRemove(PreparedStatement psmtGetDataItems) throws Exception {
        DataSet ds = new DataSet(psmtGetDataItems.executeQuery());
        if (ds != null && ds.getRowCount() > 0) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                if (ds.getString(i, "enteredtext", "").length() <= 0) continue;
                throw new SapphireException(this.getTranslationProcessor().translate("Cannot continue. The operation requires deletion of Data Item(s) with results entered."));
            }
        }
        return ds;
    }

    private DataSet getUnknownDataSets(String qcBatchId, String qcBatchSDCId, String paramListType, String insertedSamplesBuffer, boolean matchWorkItemItem) throws SapphireException {
        ArrayList<DBColumn> columns = new ArrayList<DBColumn>();
        ArrayList<String> whereList = new ArrayList<String>();
        StringBuffer whereClause = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        String fromClause = "s_qcmethod_workitem, workitemitem, sdidata, s_qcbatch";
        if (matchWorkItemItem) {
            fromClause = fromClause + ", sdiworkitemitem";
        }
        whereClause.append(" S_QCBATCH.S_QCBATCHID = ");
        whereList.add("s_qcmethod_workitem.s_qcmethodid = s_qcbatch.qcmethodid");
        whereList.add("s_qcmethod_workitem.s_qcmethodversionid = s_qcbatch.qcmethodversionid");
        whereList.add("s_qcmethod_workitem.workitemid = workitemitem.workitemid");
        whereList.add("coalesce(s_qcmethod_workitem.workitemversionid,workitemitem.workitemversionid) = workitemitem.workitemversionid");
        whereList.add("workitemitem.keyid1=sdidata.paramlistid");
        whereList.add("(workitemitem.keyid2 ='C' or workitemitem.keyid2=sdidata.paramlistversionid)");
        whereList.add("workitemitem.keyid3=sdidata.variantid");
        whereList.add("sdidata.s_datasetstatus='Initial'");
        whereList.add("( sdidata.s_qcbatchitemid is null or sdidata.s_qcbatchitemid = '' )");
        if (paramListType.trim().length() > 0) {
            fromClause = fromClause + ", paramlist";
            whereList.add("sdidata.paramlistid = paramlist.paramlistid");
            whereList.add("sdidata.paramlistversionid = paramlist.paramlistversionid");
            whereList.add("sdidata.variantid = paramlist.variantid");
            whereList.add("paramlist.s_paramlisttype = " + safeSQL.addVar(paramListType));
        }
        whereClause.append(safeSQL.addVar(qcBatchId));
        if (matchWorkItemItem) {
            whereList.add(" sdiworkitemitem.sdcid = sdidata.sdcid and sdiworkitemitem.keyid1 = sdidata.keyid1 and sdiworkitemitem.keyid2 = sdidata.keyid2 and sdiworkitemitem.keyid3 = sdidata.keyid3 ");
            whereList.add(" sdiworkitemitem.workitemid = s_qcmethod_workitem.workitemid ");
            whereList.add(" sdiworkitemitem.itemkeyid1 = sdidata.paramlistid ");
            whereList.add(" sdiworkitemitem.itemkeyid2 = sdidata.paramlistversionid ");
            whereList.add(" sdiworkitemitem.itemkeyid3 = sdidata.variantid ");
            whereList.add(" sdiworkitemitem.iteminstance = sdidata.dataset");
        }
        whereList.add(whereClause.toString());
        columns.add(0, new DBColumn("sdidata.keyid1 keyid1", false, this.isOra));
        columns.add(1, new DBColumn("sdidata.paramlistid paramlistid", false, this.isOra));
        columns.add(2, new DBColumn("sdidata.paramlistversionid paramlistversionid", false, this.isOra));
        columns.add(3, new DBColumn("sdidata.variantid variantid", false, this.isOra));
        columns.add(4, new DBColumn("sdidata.dataset dataset", false, this.isOra));
        DAMProcessor dam = new DAMProcessor(this.sapphireConnection.getConnectionId());
        String rsetid = dam.getAllDSRSet(qcBatchSDCId, insertedSamplesBuffer, null, null);
        if (rsetid != null && rsetid.length() > 0) {
            fromClause = fromClause + ",rsetitems ";
            whereList.add(" sdidata.sdcid = rsetitems.sdcid ");
            whereList.add(" sdidata.keyid1 = rsetitems.keyid1 ");
            whereList.add(" rsetitems.rsetid = " + safeSQL.addVar(rsetid));
        }
        String selectedDSSQL = BaseItem.getSelectSQL(columns, fromClause, whereList, null, false);
        DataSet dsUnknown = this.getQueryProcessor().getPreparedSqlDataSet(selectedDSSQL, safeSQL.getValues());
        if (StringUtil.getLen(rsetid) > 0L) {
            dam.clearRSet(rsetid);
        }
        return dsUnknown;
    }
}

