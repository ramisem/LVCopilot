/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.elements.detailmaint.BaseItem;
import com.labvantage.opal.elements.detailmaint.DBColumn;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.qcbatch.QCBatchSampleType;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.QCUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddQCSample
extends BaseAction
implements sapphire.action.AddQCSample {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String batchSampleTypeId;
        String linkedToSample;
        int b;
        String qcBatchId = properties.getProperty("qcbatchid");
        TranslationProcessor tp = this.getTranslationProcessor();
        ActionProcessor ap = this.getActionProcessor();
        QueryProcessor qp = this.getQueryProcessor();
        if (qcBatchId.length() == 0) {
            throw new SapphireException(tp.translate("QCBatch Id not provided!"));
        }
        String qcBatchSampleTypeId = properties.getProperty("qcbatchsampletypeid");
        if (qcBatchSampleTypeId.length() == 0) {
            throw new SapphireException(tp.translate("QCBatchSampleType Id not provided!"));
        }
        DBUtil db = new DBUtil();
        db.setConnection(this.getConnectionProcessor().getSapphireConnection());
        boolean isOra = db.isOracle();
        QCBatch qcBatch = QCBatchPool.getQCBatch(this.getQueryProcessor(), qcBatchId);
        if (qcBatch == null) {
            throw new SapphireException(tp.translate("Invalid QCBatch Id entered") + ": " + qcBatchId);
        }
        String traceLogId = "";
        DataSet propsDS = new DataSet();
        propsDS.addColumnValues("batchsampletypeid", 0, qcBatchSampleTypeId, ";");
        propsDS.addColumnValues("position_input", 0, properties.getProperty("position"), ";");
        propsDS.addColumnValues("linkedto", 0, properties.getProperty("linkedto"), ";");
        propsDS.addColumnValues("linkedtosampleid", 0, properties.getProperty("linkedtosampleid"), ";");
        traceLogId = properties.getProperty("tracelogid");
        String qcBatchSDCId = qcBatch.getQCBatchSDC();
        String qcBatchStatus = qcBatch.getStatus();
        if (qcBatchStatus == null || qcBatchStatus.length() == 0) {
            qcBatch = new QCBatch(this.getQueryProcessor(), qcBatchId);
            QCBatchPool.renewQCBatchInPool(qcBatch);
            qcBatchStatus = qcBatch.getStatus();
        }
        String qcbatchSiteId = qcBatch.getSiteDepartmentId();
        if (qcBatchSDCId == null || qcBatchSDCId.length() == 0) {
            qcBatchSDCId = "Sample";
        }
        if (!(qcBatchStatus.equalsIgnoreCase("Initial") || qcBatchStatus.equalsIgnoreCase("InProgress") || qcBatchStatus.equalsIgnoreCase("DataEntered"))) {
            throw new SapphireException(tp.translate("QCBatch is not in Initial, InProgress or DataEntered status, hence cannot proceed."));
        }
        for (int b2 = 0; b2 < propsDS.getRowCount(); ++b2) {
            String batchSampleTypeId2 = propsDS.getValue(b2, "batchsampletypeid").trim();
            if (batchSampleTypeId2.length() == 0) {
                throw new SapphireException(tp.translate("BatchSampleType Id not entered for one item."));
            }
            QCBatchSampleType batchSampleType = qcBatch.getQCBatchSampleType(batchSampleTypeId2);
            if (batchSampleType == null) {
                throw new SapphireException(tp.translate("Invalid BatchSampletypeId entered") + ": " + batchSampleTypeId2);
            }
            propsDS.setValue(b2, "batchsampletypeid", batchSampleTypeId2);
            String linkedTo = propsDS.getValue(b2, "linkedto");
            String linkedToSample2 = propsDS.getValue(b2, "linkedtosampleid");
            String qcSampleType = batchSampleType.getColumnValue("qcsampletype");
            if ((linkedTo.length() > 0 || linkedToSample2.length() > 0) && (qcSampleType.equalsIgnoreCase("Control") || qcSampleType.equalsIgnoreCase("Blank"))) {
                throw new SapphireException(tp.translate("\"Linked To\" is not applicable to the BatchSampleType ") + ": " + qcSampleType);
            }
            propsDS.setValue(b2, "linkedto", linkedTo.trim());
            propsDS.setValue(b2, "linkedtosampleid", linkedToSample2.trim());
            propsDS.setValue(b2, "position_input", propsDS.getValue(b2, "position_input").trim());
        }
        DataSet dsQCBatchItems = qp.getPreparedSqlDataSet("select distinct qcbi.s_qcbatchid, qcbi.s_qcbatchitemid, qcbi.linktoqcbatchitemid, qcbi.linkedto, qcbi.usersequence, sdidata.keyid1 from s_qcbatchitem qcbi, sdidata  where sdidata.sdcid = ? and sdidata.s_qcbatchid = qcbi.s_qcbatchid  and sdidata.s_qcbatchitemid = qcbi.s_qcbatchitemid and sdidata.s_qcbatchid = ? order by usersequence", (Object[])new String[]{qcBatchSDCId, qcBatchId});
        int batchSize = dsQCBatchItems.getRowCount();
        String userSiteId = OpalUtil.getSiteIdFromUserDefaultDepartment(this.connectionInfo, db);
        String sampleSiteDepartmentId = "";
        if (userSiteId != null && userSiteId.length() > 0) {
            sampleSiteDepartmentId = userSiteId;
        } else if (qcbatchSiteId.length() > 0) {
            sampleSiteDepartmentId = qcbatchSiteId;
        }
        boolean copySecurityDeptToSample = false;
        String batchSecurityDept = "";
        SDCProcessor sdcp = this.getSDCProcessor();
        if (sdcp.getPropertyList(qcBatchSDCId).getProperty("accesscontrolledflag").equals("D") && sdcp.getPropertyList("QCBatch").getProperty("accesscontrolledflag").equals("D") && (copySecurityDeptToSample = QCUtil.copySecurityDeptToSample(this.getConfigurationProcessor()))) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select securitydepartment from s_qcbatch where s_qcbatchid = ?", (Object[])new String[]{qcBatch.getQCBatchID()});
            batchSecurityDept = ds.getValue(0, "securitydepartment");
        }
        DataSet dsNewQCSamples = new DataSet();
        dsNewQCSamples.addColumn("keyid1", 0);
        dsNewQCSamples.addColumn("qcbatchsampletypeid", 0);
        dsNewQCSamples.addColumn("applyaction", 0);
        dsNewQCSamples.addColumn("linktoqcbatchitemid", 0);
        dsNewQCSamples.addColumn("linkedto", 0);
        dsNewQCSamples.addColumn("qcbatchitemid", 0);
        dsNewQCSamples.addColumn("linkedrowid", 1);
        dsNewQCSamples.addColumn("added", 0);
        HashMap<String, Integer> newSampleDSMap = new HashMap<String, Integer>();
        DataSet dsInsertQCBatchItem = new DataSet();
        int addedCnt = 0;
        boolean updateQCBISequence = false;
        for (b = 0; b < propsDS.getRowCount(); ++b) {
            String position = propsDS.getValue(b, "position_input");
            String linkedTo = propsDS.getValue(b, "linkedto");
            linkedToSample = propsDS.getValue(b, "linkedtosampleid");
            if (linkedToSample.length() == 0 && linkedTo.length() > 0 && position.equals(linkedTo)) {
                HashMap<String, String> token = new HashMap<String, String>();
                token.put("position", position);
                token.put("linkedto", linkedTo);
                throw new SapphireException(tp.translate("Position [position] cannot be same with linked to value [linkedto]", token));
            }
            int pos = -1;
            if (position.length() > 0) {
                try {
                    pos = Integer.parseInt(position);
                }
                catch (NumberFormatException ne) {
                    throw new SapphireException(tp.translate("Position must be a valid integer number. Position provided: ") + position);
                }
            }
            propsDS.setNumber(b, "position", pos);
            propsDS.setString(b, "input_sequence", Integer.toString(b));
        }
        propsDS.sort("position");
        for (b = 0; b < propsDS.getRowCount(); ++b) {
            batchSampleTypeId = propsDS.getValue(b, "batchsampletypeid");
            int position = propsDS.getInt(b, "position");
            String linkedTo = propsDS.getValue(b, "linkedto");
            String linkedToSample3 = propsDS.getValue(b, "linkedtosampleid");
            int usersequence = 0;
            if (position == -1) {
                usersequence = batchSize + addedCnt + 1;
                ++addedCnt;
            } else {
                usersequence = position;
                ++addedCnt;
            }
            QCBatchSampleType batchSampleType = qcBatch.getQCBatchSampleType(batchSampleTypeId);
            String qcSampleType = batchSampleType.getColumnValue("qcsampletype");
            String returnString = AddQCSample.createQCSample(batchSampleTypeId, qcBatchId, batchSecurityDept, sampleSiteDepartmentId, qp, ap, isOra);
            int row = dsNewQCSamples.addRow();
            String qcBatchItemId = OpalUtil.getNextSequence("s_qcbatchitem", this.getSequenceProcessor());
            dsNewQCSamples.setNumber(row, "linkedrowid", -1);
            dsNewQCSamples.setString(row, "added", "N");
            int semicolonidx = returnString.indexOf(";");
            String newKeyId1 = returnString.substring(0, semicolonidx);
            String applyActionId = returnString.substring(semicolonidx + 1);
            dsNewQCSamples.setValue(row, "keyid1", newKeyId1);
            dsNewQCSamples.setValue(row, "applyaction", applyActionId);
            dsNewQCSamples.setValue(row, "qcbatchsampletypeid", batchSampleTypeId);
            dsNewQCSamples.setValue(row, "qcbatchitemid", qcBatchItemId);
            dsNewQCSamples.setString(row, "input_sequence", propsDS.getValue(b, "input_sequence"));
            if (newKeyId1 != null && newKeyId1.trim().length() > 0) {
                dsNewQCSamples.setValue(row, "keyid1", newKeyId1);
                newSampleDSMap.put(newKeyId1, new Integer(row));
            }
            int r = dsInsertQCBatchItem.addRow();
            dsInsertQCBatchItem.setString(r, "s_qcbatchid", qcBatchId);
            dsInsertQCBatchItem.setString(r, "s_qcbatchitemid", qcBatchItemId);
            dsInsertQCBatchItem.setString(r, "qcbatchsampletypeid", batchSampleTypeId);
            dsInsertQCBatchItem.setString(r, "linktoqcbatchitemid", "");
            dsInsertQCBatchItem.setString(r, "batchitemtype", qcSampleType);
            dsInsertQCBatchItem.setString(r, "linkedto", linkedTo);
            dsInsertQCBatchItem.setString(r, "linkedtosampleid", linkedToSample3);
            dsInsertQCBatchItem.setNumber(r, "usersequence", usersequence);
            if (usersequence <= dsQCBatchItems.getRowCount()) {
                updateQCBISequence = true;
                for (int index = usersequence - 1; index < dsQCBatchItems.getRowCount(); ++index) {
                    int qcbUserSequence = dsQCBatchItems.getInt(index, "usersequence");
                    dsQCBatchItems.setNumber(index, "usersequence", ++qcbUserSequence);
                }
            } else if (usersequence > dsQCBatchItems.getRowCount() + propsDS.getRowCount()) {
                throw new SapphireException(tp.translate("Specified position ") + position + tp.translate(" exceeds the QCBatch size."));
            }
            int newRow = dsQCBatchItems.addRow();
            dsQCBatchItems.setString(newRow, "s_qcbatchid", qcBatchId);
            dsQCBatchItems.setString(newRow, "s_qcbatchitemid", qcBatchItemId);
            dsQCBatchItems.setString(newRow, "keyid1", newKeyId1);
            dsQCBatchItems.setNumber(newRow, "usersequence", usersequence);
            if (linkedToSample3.length() == 0) {
                dsQCBatchItems.setString(newRow, "linkedto", linkedTo);
            }
            dsQCBatchItems.sort("usersequence");
        }
        for (int i = 0; i < dsInsertQCBatchItem.getRowCount(); ++i) {
            int f;
            batchSampleTypeId = dsInsertQCBatchItem.getValue(i, "qcbatchsampletypeid");
            String linkedTo = dsInsertQCBatchItem.getValue(i, "linkedto");
            linkedToSample = dsInsertQCBatchItem.getValue(i, "linkedtosampleid");
            String qcBatchItemId = dsInsertQCBatchItem.getValue(i, "s_qcbatchitemid");
            int findRow = dsQCBatchItems.findRow("s_qcbatchitemid", qcBatchItemId);
            int usersequence = dsQCBatchItems.getInt(findRow, "usersequence");
            QCBatchSampleType batchSampleType = qcBatch.getQCBatchSampleType(batchSampleTypeId);
            if (linkedTo.length() == 0 && linkedToSample.length() == 0) {
                linkedTo = batchSampleType.getColumnValue("linkedto");
            }
            if ((linkedTo == null || linkedTo.length() <= 0) && linkedToSample.length() <= 0) continue;
            String linkedBatchItemid = "";
            int rowIndex = -1;
            if (linkedToSample.length() > 0) {
                HashMap<String, String> findLinkQCBatchItems = new HashMap<String, String>();
                findLinkQCBatchItems.put("keyid1", linkedToSample);
                DataSet dsLinkedBI = dsQCBatchItems.getFilteredDataSet(findLinkQCBatchItems);
                String linkedItemUserSequence = "";
                if (dsLinkedBI.getRowCount() > 1) {
                    for (int l = 0; l < dsLinkedBI.getRowCount(); ++l) {
                        if (dsLinkedBI.getValue(l, "linktoqcbatchitemid").length() != 0) continue;
                        linkedBatchItemid = dsLinkedBI.getValue(l, "s_qcbatchitemid");
                        linkedItemUserSequence = dsLinkedBI.getValue(l, "usersequence");
                        break;
                    }
                } else {
                    linkedBatchItemid = dsLinkedBI.getValue(0, "s_qcbatchitemid");
                    linkedItemUserSequence = dsLinkedBI.getValue(0, "usersequence");
                }
                if (linkedBatchItemid.length() == 0) {
                    throw new SapphireException(tp.translate("No QCBatchItem could not be found using linked Sample id: ") + linkedToSample);
                }
                dsInsertQCBatchItem.setString(i, "linktoqcbatchitemid", linkedBatchItemid);
                dsInsertQCBatchItem.setString(i, "linkedto", linkedItemUserSequence);
                rowIndex = dsQCBatchItems.findRow("s_qcbatchitemid", linkedBatchItemid);
                dsNewQCSamples.setNumber(i, "linkedrowid", rowIndex);
                dsNewQCSamples.setString(i, "linkedto", linkedItemUserSequence);
                dsNewQCSamples.setString(i, "linktoqcbatchitemid", linkedBatchItemid);
                f = dsQCBatchItems.findRow("s_qcbatchitemid", qcBatchItemId);
                if (f <= -1) continue;
                dsQCBatchItems.setValue(f, "linktoqcbatchitemid", linkedBatchItemid);
                dsQCBatchItems.setValue(f, "linkedto", linkedItemUserSequence);
                continue;
            }
            if (linkedTo == null || linkedTo.length() <= 0) continue;
            char positionTypeChar = linkedTo.charAt(0);
            int linkPosition = -1;
            switch (positionTypeChar) {
                case '+': {
                    try {
                        linkPosition = Integer.parseInt(linkedTo.substring(1));
                    }
                    catch (NumberFormatException numberFormatException) {
                        if (Trace.on) {
                            Trace.log("Relative NumberformatException : " + numberFormatException.getMessage());
                        }
                        throw new SapphireException("Relative NumberformatException : " + ErrorUtil.extractMessageFromException(numberFormatException, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
                    }
                    rowIndex = usersequence + linkPosition - 1;
                    break;
                }
                case '-': {
                    try {
                        linkPosition = Integer.parseInt(linkedTo);
                    }
                    catch (NumberFormatException numberFormatException) {
                        if (Trace.on) {
                            Trace.log("Relative NumberformatException : " + numberFormatException.getMessage());
                        }
                        throw new SapphireException("Relative NumberformatException : " + ErrorUtil.extractMessageFromException(numberFormatException, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
                    }
                    rowIndex = usersequence + linkPosition - 1;
                    break;
                }
                default: {
                    try {
                        rowIndex = Integer.parseInt(linkedTo);
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
            if (rowIndex >= 0 && rowIndex < dsQCBatchItems.size()) {
                linkedBatchItemid = dsQCBatchItems.getValue(rowIndex, "s_qcbatchitemid");
                if (qcBatchItemId.equals(linkedBatchItemid)) {
                    throw new SapphireException(tp.translate("Cannot proceed. Addition of QCBatchItem at specified position is causing self linking in the QCBatchItem at position") + ": " + usersequence);
                }
                String linkedlinkedBatchItemid = dsQCBatchItems.getValue(rowIndex, "linktoqcbatchitemid");
                if (qcBatchItemId.equals(linkedlinkedBatchItemid)) {
                    HashMap<String, String> token = new HashMap<String, String>();
                    token.put("usersequence", String.valueOf(usersequence));
                    token.put("rowindex", String.valueOf(rowIndex + 1));
                    throw new SapphireException(tp.translate("Circular linking found between the added Samples at position [usersequence] and [rowindex], cannot proceed.", token));
                }
                dsInsertQCBatchItem.setString(i, "linktoqcbatchitemid", linkedBatchItemid);
                dsInsertQCBatchItem.setString(i, "linkedto", linkedTo);
                dsNewQCSamples.setNumber(i, "linkedrowid", rowIndex);
                dsNewQCSamples.setString(i, "linkedto", linkedTo);
                dsNewQCSamples.setString(i, "linktoqcbatchitemid", linkedBatchItemid);
                f = dsQCBatchItems.findRow("s_qcbatchitemid", qcBatchItemId);
                if (f <= -1) continue;
                dsQCBatchItems.setValue(f, "linktoqcbatchitemid", linkedBatchItemid);
                dsQCBatchItems.setValue(f, "linkedto", linkedTo);
                continue;
            }
            throw new SapphireException(tp.translate("Linked To position") + " " + linkedTo + " " + tp.translate("is greater than QCBatch size, cannot proceed."));
        }
        for (int idx = 0; idx < dsNewQCSamples.size(); ++idx) {
            String sampleID = dsNewQCSamples.getValue(idx, "keyid1");
            int linkedRowID = dsNewQCSamples.getInt(idx, "linkedrowid");
            String linkedSampleID = "";
            if (linkedRowID <= -1) continue;
            linkedSampleID = dsQCBatchItems.getValue(linkedRowID, "keyid1");
            if (sampleID != null && !sampleID.trim().equals("") && !sampleID.equals("(Auto)")) continue;
            sampleID = linkedSampleID;
            dsNewQCSamples.setValue(idx, "keyid1", sampleID);
        }
        if (dsInsertQCBatchItem.getRowCount() > 0) {
            dsInsertQCBatchItem.removeColumn("linkedtosampleid");
            DataSetUtil.insert(this.database, dsInsertQCBatchItem, "s_qcbatchitem");
        }
        DataSet dsOrderOfExec = new DataSet();
        for (int idx = 0; idx < dsNewQCSamples.size(); ++idx) {
            this.addInSequence(dsQCBatchItems, dsNewQCSamples, dsOrderOfExec, idx, newSampleDSMap);
        }
        this.callApplyAction(dsOrderOfExec, qcBatchId, qcBatchSDCId);
        if (updateQCBISequence) {
            dsQCBatchItems.removeColumn("keyid1");
            for (int k = 0; k < dsQCBatchItems.getRowCount(); ++k) {
                String qcbatchitemid;
                String linkedTo = dsQCBatchItems.getValue(k, "linkedto");
                int usersequence = dsQCBatchItems.getInt(k, "usersequence");
                int rowIndex = -1;
                if (linkedTo.length() <= 0) continue;
                char positionTypeChar = linkedTo.charAt(0);
                int linkPosition = -1;
                switch (positionTypeChar) {
                    case '+': {
                        try {
                            linkPosition = Integer.parseInt(linkedTo.substring(1));
                        }
                        catch (NumberFormatException numberFormatException) {
                            if (Trace.on) {
                                Trace.log("Relative NumberformatException : " + numberFormatException.getMessage());
                            }
                            throw new SapphireException("Relative NumberformatException : " + numberFormatException.getMessage());
                        }
                        rowIndex = usersequence + linkPosition - 1;
                        break;
                    }
                    case '-': {
                        try {
                            linkPosition = Integer.parseInt(linkedTo);
                        }
                        catch (NumberFormatException numberFormatException) {
                            if (Trace.on) {
                                Trace.log("Relative NumberformatException : " + numberFormatException.getMessage());
                            }
                            throw new SapphireException("Relative NumberformatException : " + numberFormatException.getMessage());
                        }
                        rowIndex = usersequence + linkPosition - 1;
                        break;
                    }
                    default: {
                        try {
                            rowIndex = Integer.parseInt(linkedTo);
                            --rowIndex;
                            break;
                        }
                        catch (NumberFormatException numberFormatException) {
                            if (Trace.on) {
                                Trace.log("Absolute NumberformatException : " + numberFormatException.getMessage());
                            }
                            throw new SapphireException("Absolute NumberformatException : " + numberFormatException.getMessage());
                        }
                    }
                }
                if (rowIndex <= -1) continue;
                String linkedbatchitemid = dsQCBatchItems.getValue(rowIndex, "s_qcbatchitemid");
                if (linkedbatchitemid.equals(qcbatchitemid = dsQCBatchItems.getValue(k, "s_qcbatchitemid"))) {
                    throw new SapphireException(tp.translate("Cannot proceed. Addition of QCBatchItem at specified position is causing self linking in the QCBatchItem at position") + ": " + usersequence);
                }
                dsQCBatchItems.setValue(k, "linktoqcbatchitemid", linkedbatchitemid);
            }
            DataSetUtil.update(this.database, dsQCBatchItems, "s_qcbatchitem", new String[]{"s_qcbatchid", "s_qcbatchitemid"});
        }
        qcBatch = new QCBatch(this.getQueryProcessor(), qcBatchId);
        QCBatchPool.renewQCBatchInPool(qcBatch);
        if (dsInsertQCBatchItem.size() > 0) {
            QCUtil.addCalcDataItems(qcBatchId, qp, ap);
            QCUtil.updateReagent(qp, this.database, dsInsertQCBatchItem);
        }
        if (qcBatchStatus.equalsIgnoreCase("DataEntered")) {
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("qcbatchid", qcBatchId);
            props.put("postdataentry", "N");
            props.put("tracelogid", traceLogId);
            this.getActionProcessor().processAction("UpdateQCBatchStatus", "1", props);
        }
        StringBuffer qcSampleId = new StringBuffer();
        StringBuffer qcBatchItemId = new StringBuffer();
        String[] bstInputArray = StringUtil.split(qcBatchSampleTypeId, ";");
        for (int i = 0; i < bstInputArray.length; ++i) {
            String batchSampletypeId = bstInputArray[i];
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("input_sequence", Integer.toString(i));
            findMap.put("qcbatchsampletypeid", batchSampletypeId.trim());
            int find = dsNewQCSamples.findRow(findMap);
            if (find > -1) {
                qcSampleId.append(";").append(dsNewQCSamples.getValue(find, "keyid1"));
                qcBatchItemId.append(";").append(dsNewQCSamples.getValue(find, "qcbatchitemid"));
                continue;
            }
            qcSampleId.append(";").append("");
            qcBatchItemId.append(";").append("");
        }
        if (qcSampleId.length() > 0) {
            properties.setProperty("newqcsampleid", qcSampleId.substring(1));
        }
        if (qcBatchItemId.length() > 0) {
            properties.setProperty("newqcbatchitemid", qcBatchItemId.substring(1));
        }
    }

    private String findLinkedsampleid(String linktoqcbatchitemid, DataSet dsNewQCSamples, DataSet dsQCBatchItems, DataSet dsOrderOfExec) {
        int findRow;
        String linkedSampleID = "";
        if (linktoqcbatchitemid.length() > 0 && (findRow = dsQCBatchItems.findRow("s_qcbatchitemid", linktoqcbatchitemid)) > -1) {
            linkedSampleID = dsQCBatchItems.getValue(findRow, "keyid1");
            if (linkedSampleID.length() == 0) {
                linktoqcbatchitemid = dsQCBatchItems.getValue(findRow, "linktoqcbatchitemid");
                linkedSampleID = this.findLinkedsampleid(linktoqcbatchitemid, dsNewQCSamples, dsQCBatchItems, dsOrderOfExec);
            }
            if (linkedSampleID.length() > 0) {
                int sourceBatchItemRow = dsQCBatchItems.findRow("linktoqcbatchitemid", linktoqcbatchitemid);
                String qcbatchItemId = dsQCBatchItems.getValue(sourceBatchItemRow, "s_qcbatchitemid");
                int newSamplerow = dsNewQCSamples.findRow("qcbatchitemid", qcbatchItemId);
                if (newSamplerow > -1 && !dsNewQCSamples.getValue(newSamplerow, "added").equals("Y")) {
                    dsNewQCSamples.setString(newSamplerow, "added", "Y");
                    if (dsNewQCSamples.getValue(newSamplerow, "keyid1").length() == 0) {
                        dsNewQCSamples.setString(newSamplerow, "keyid1", linkedSampleID);
                    }
                    dsOrderOfExec.copyRow(dsNewQCSamples, newSamplerow, 1);
                }
                HashMap<String, String> map = new HashMap<String, String>();
                DataSet matchRows = new DataSet();
                map.put("linktoqcbatchitemid", qcbatchItemId);
                DataSet matchLinkedBatchItems = dsQCBatchItems.getFilteredDataSet(map);
                for (int k = 0; k < matchLinkedBatchItems.getRowCount(); ++k) {
                    qcbatchItemId = matchLinkedBatchItems.getValue(k, "s_qcbatchitemid");
                    map = new HashMap();
                    map.put("qcbatchitemid", qcbatchItemId);
                    matchRows = dsNewQCSamples.getFilteredDataSet(map);
                    for (int p = 0; p < matchRows.getRowCount(); ++p) {
                        if (matchRows.getValue(p, "added").equals("Y")) continue;
                        matchRows.setString(p, "added", "Y");
                        if (matchRows.getValue(p, "keyid1").length() == 0) {
                            matchRows.setString(p, "keyid1", linkedSampleID);
                        }
                        dsOrderOfExec.copyRow(matchRows, p, 1);
                    }
                }
            }
        }
        return linkedSampleID;
    }

    private void callApplyAction(DataSet ds, String qcBatchId, String qcBatchSDCId) throws SapphireException {
        for (int idx = 0; idx < ds.size(); ++idx) {
            String currentBatchSampleTypeId = ds.getValue(idx, "qcbatchsampletypeid");
            String applyActionId = ds.getValue(idx, "applyaction");
            String sampleID = ds.getValue(idx, "keyid1");
            String qcbatchitemid = ds.getValue(idx, "qcbatchitemid");
            String linkedto = ds.getValue(idx, "linkedto");
            String linktobatchitemid = ds.getValue(idx, "linktoqcbatchitemid");
            if (sampleID.length() <= 0) continue;
            HashMap<String, String> sampleDetails = new HashMap<String, String>();
            sampleDetails.put("qcbatchsampletypeid", currentBatchSampleTypeId);
            sampleDetails.put("qcbatchid", qcBatchId);
            sampleDetails.put("qcbatchitemid", qcbatchitemid);
            sampleDetails.put("resolvelink", "Y");
            sampleDetails.put("sdcid", qcBatchSDCId);
            sampleDetails.put("keyid1", sampleID);
            sampleDetails.put("linktoqcbatchitemid", linktobatchitemid);
            sampleDetails.put("linkedto", linkedto);
            this.getActionProcessor().processAction(applyActionId, "1", sampleDetails);
        }
    }

    private void addInSequence(DataSet dsQCBatchItem, DataSet dsNewQCSamples, DataSet dsOrderOfExec, int idx, HashMap newSampleDSMap) {
        int linkedRowID = dsNewQCSamples.getInt(idx, "linkedrowid");
        String sampleID = dsNewQCSamples.getValue(idx, "keyid1");
        if (linkedRowID > -1) {
            String linkedSampleID = dsQCBatchItem.getValue(linkedRowID, "keyid1");
            String linktoqcbatchitemid = dsQCBatchItem.getValue(linkedRowID, "linktoqcbatchitemid");
            if (newSampleDSMap.containsKey(linkedSampleID)) {
                int row = (Integer)newSampleDSMap.get(linkedSampleID);
                this.addInSequence(dsQCBatchItem, dsNewQCSamples, dsOrderOfExec, row, newSampleDSMap);
            } else if (linktoqcbatchitemid.length() > 0 && (linkedSampleID = this.findLinkedsampleid(linktoqcbatchitemid, dsNewQCSamples, dsQCBatchItem, dsOrderOfExec)).length() > 0 && dsNewQCSamples.getString(idx, "added", "").equals("N")) {
                dsNewQCSamples.setString(idx, "added", "Y");
                if (sampleID.length() == 0) {
                    dsNewQCSamples.setString(idx, "keyid1", linkedSampleID);
                }
                dsOrderOfExec.copyRow(dsNewQCSamples, idx, 1);
                return;
            }
        }
        if (dsNewQCSamples.getString(idx, "added", "").equals("N")) {
            dsOrderOfExec.copyRow(dsNewQCSamples, idx, 1);
            dsNewQCSamples.setString(idx, "added", "Y");
        }
    }

    public static String createQCSample(String qcBatchSampleTypeId, String qcBatchId, String batchSecurityDept, String sampleSiteId, QueryProcessor qp, ActionProcessor ap, boolean isOra) throws SapphireException {
        String applyActionId = null;
        String newKeyId1 = null;
        String selectedDSSQL = null;
        SafeSQL safeSQL = new SafeSQL();
        String fromClause = " s_qcbatchsampletype ";
        StringBuffer whereClause = new StringBuffer();
        ArrayList<DBColumn> columns = new ArrayList<DBColumn>();
        ArrayList<String> whereList = new ArrayList<String>();
        DataSet qcBatchSampleTypeDS = null;
        HashMap<String, String> sampleDetails = new HashMap<String, String>();
        sampleDetails.put("qcbatchid", qcBatchId);
        sampleDetails.put("resolvelink", "N");
        sampleDetails.put("qcbatchsampletypeid", qcBatchSampleTypeId);
        whereClause.append(" S_QCBATCHSAMPLETYPEID = ");
        whereClause.append(safeSQL.addVar(qcBatchSampleTypeId));
        whereList.add(whereClause.toString());
        columns.add(0, new DBColumn("actionapply", false, isOra));
        columns.add(0, new DBColumn("qcsampletype", false, isOra));
        selectedDSSQL = BaseItem.getSelectSQL(columns, fromClause, whereList, null, false);
        qcBatchSampleTypeDS = qp.getPreparedSqlDataSet(selectedDSSQL, safeSQL.getValues());
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
        if (batchSecurityDept.length() > 0) {
            sampleDetails.put("securitydepartment", batchSecurityDept);
        }
        if (sampleSiteId.length() > 0) {
            sampleDetails.put("sitedepartmentid", sampleSiteId);
        }
        ap.processAction(applyActionId, "1", sampleDetails);
        newKeyId1 = (String)sampleDetails.get("newkeyid1");
        return newKeyId1 + ";" + applyActionId;
    }
}

