/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.qcactions.QCApplyBaseAction;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.sql.common.Aqc;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.SDI;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class QCApplyAction
extends QCApplyBaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 100960 $";
    int rc = 1;
    private SQLGenerator __SqlGenerator;

    @Override
    public int processAction(String actionid, String versionid, HashMap props) {
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.getConnectionProcessor().isOra());
        if (actionid.equals("QCApplyAction")) {
            this.rc = this.doQCApplyAction(props);
        } else if (actionid.equals("QCApplyLinkTo")) {
            props.put("createqcsample", "N");
            this.rc = this.doQCApplyAction(props);
        }
        return this.rc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int doQCApplyAction(HashMap props) {
        String INVALID_QCBATCHID = "Invalid Input - QCBatchID";
        String INVALID_QCBATCHITEMID = "Invalid Input - QCBatchItemID";
        String INVALID_QCSAMPLETYPEID = "Invalid Input - QCSampleTypeID";
        String NO_WORKITEM_FOUND = "No Workitems found.";
        String qcBatchId = (String)props.get("qcbatchid");
        String qcBatchSampleTypeId = (String)props.get("qcbatchsampletypeid");
        String qcBatchItemId = (String)props.get("qcbatchitemid");
        String resolveLink = (String)props.get("resolvelink");
        String qcSampleType = (String)props.get("qcsampletype");
        String createQCSample = (String)props.get("createqcsample");
        String createWorksheet = (String)props.get("createworksheet");
        String securityDepartment = (String)props.get("securitydepartment");
        String siteDepartmentId = (String)props.get("sitedepartmentid");
        String sampleTemplateId = "";
        String batchSdcId = "Sample";
        if (props.get("qcsampletemplate") != null) {
            sampleTemplateId = (String)props.get("qcsampletemplate");
        }
        if (props.get("batchsdcid") != null) {
            batchSdcId = (String)props.get("batchsdcid");
        }
        if (createQCSample == null || createQCSample.trim().equals("")) {
            createQCSample = "Y";
        }
        if (createWorksheet == null || createWorksheet.trim().equals("")) {
            createWorksheet = "Y";
        }
        String errMessage = "";
        boolean resolveLinkFlag = false;
        SDI sdi = null;
        ActionProcessor ap = null;
        QueryProcessor qp = null;
        if (qcBatchId == null || qcBatchId.equals("")) {
            errMessage = errMessage + "Invalid Input - QCBatchID";
            return this.setError("Invalid Input - QCBatchID");
        }
        if (resolveLink != null && resolveLink.equalsIgnoreCase("Y")) {
            if (qcBatchItemId == null || qcBatchItemId.trim().equals("")) {
                errMessage = errMessage + "Invalid Input - QCBatchItemID";
                return this.setError("Invalid Input - QCBatchItemID");
            }
            if (qcBatchSampleTypeId == null || qcBatchSampleTypeId.equals("")) {
                errMessage = errMessage + "Invalid Input - QCSampleTypeID";
                return this.setError("Invalid Input - QCSampleTypeID");
            }
            resolveLinkFlag = true;
        }
        ap = this.getActionProcessor();
        qp = this.getQueryProcessor();
        if (!resolveLinkFlag) {
            String copies = (String)props.get("copies");
            DataSet ds = null;
            if (copies == null || copies.trim().equals("")) {
                copies = "1";
            }
            if ("N".equalsIgnoreCase(createQCSample)) {
                StringBuffer newKeys = new StringBuffer();
                for (int numCopies = Integer.parseInt(copies); numCopies > 0; --numCopies) {
                    newKeys.append(";");
                }
                props.put("newkeyid1", newKeys.substring(0, newKeys.length() - 1));
            } else {
                if (qcBatchSampleTypeId != null && (qcBatchSampleTypeId = qcBatchSampleTypeId.trim()).length() > 0) {
                    sdi = new SDI("QCBatchSampleType", qcBatchSampleTypeId, null, null);
                    SafeSQL safeSQL = new SafeSQL();
                    ds = qp.getPreparedSqlDataSet(Aqc.getQCBatchSampleTypeTemplate(sdi, qcBatchId, safeSQL), safeSQL.getValues());
                    if (ds == null || ds.getRowCount() == 0) {
                        errMessage = errMessage + "Illegal data.";
                        this.logger.warn("Illegal data.");
                    } else {
                        sampleTemplateId = ds.getValue(0, "qctemplatekeyid1", "");
                        batchSdcId = ds.getValue(0, "qcbatchsdcid", "");
                    }
                }
                HashMap<String, String> actionprops = new HashMap<String, String>();
                actionprops.put("sdcid", batchSdcId);
                if (sampleTemplateId != null && (sampleTemplateId = sampleTemplateId.trim()).length() > 0) {
                    actionprops.put("templateid", sampleTemplateId);
                }
                actionprops.put("copies", copies);
                actionprops.put("qcsampletype", qcSampleType);
                if (securityDepartment != null && securityDepartment.length() > 0) {
                    actionprops.put("securitydepartment", securityDepartment);
                }
                if (siteDepartmentId != null && siteDepartmentId.length() > 0) {
                    actionprops.put("sitedepartmentid", siteDepartmentId);
                }
                try {
                    actionprops.put("wapstatus", "Never");
                    ap.processAction("AddSDI", "1", actionprops);
                    props.put("newkeyid1", actionprops.get("newkeyid1"));
                }
                catch (ActionException e) {
                    errMessage = errMessage + e.getMessage();
                    return this.setError("Exception: " + e.getMessage(), e);
                }
            }
        } else {
            String linktoQcBatchItemId;
            String INVALID_SDI = "Invalid Action Input [SDI is not valid]";
            String[] qcBatchItemIdArr = StringUtil.split(qcBatchItemId, ";");
            String[] keyId1Arr = StringUtil.split((String)props.get("keyid1"), ";");
            String[] keyId2Arr = StringUtil.split((String)props.get("keyid2"), ";");
            String[] keyId3Arr = StringUtil.split((String)props.get("keyid3"), ";");
            String[] linkToQCBatchItemIdArr = StringUtil.split((String)props.get("linktoqcbatchitemid"), ";");
            String[] linkedToArr = StringUtil.split((String)props.get("linkedto"), ";");
            String[] batchSampleTypeIdArr = StringUtil.split((String)props.get("qcbatchsampletypeid"), ";");
            String sdcId = (String)props.get("sdcid");
            StringBuffer workitemIdQueryBuffer = new StringBuffer();
            DataSet ds = null;
            if (qcBatchItemIdArr.length != keyId1Arr.length || qcBatchItemIdArr.length != linkToQCBatchItemIdArr.length || qcBatchItemIdArr.length != linkedToArr.length) {
                return this.setError(" Properties are not matching.");
            }
            workitemIdQueryBuffer.append("SELECT DISTINCT S_QCBATCHSAMPLETYPEID, WORKITEMID, WORKITEMVERSIONID, SPECID, SPECVERSIONID ").append(" FROM S_QCBATCHSAMPLETYPE WHERE ").append(" qcbatchid = ? AND S_QCBATCHSAMPLETYPEID = ? ");
            StringBuffer keyid1ForAddSDIWorkItem = new StringBuffer();
            StringBuffer keyid2ForAddSDIWorkItem = new StringBuffer();
            StringBuffer keyid3ForAddSDIWorkItem = new StringBuffer();
            StringBuffer workitemidForAddSDIWorkItem = new StringBuffer();
            StringBuffer workitemVersionIdForAddSDIWorkItem = new StringBuffer();
            StringBuffer applyworkitemidForAddSDIWorkItem = new StringBuffer();
            HashMap<String, DataSet> batchItemsAndWorkItemsMapping = new HashMap<String, DataSet>();
            StringBuffer qcBatchIds = new StringBuffer();
            StringBuffer qcBatchItemIds = new StringBuffer();
            StringBuffer wapStatus = new StringBuffer();
            DataSet dsWorkItem = null;
            sdi = new SDI("QCBatch", qcBatchId, null, null);
            SafeSQL safeSQL = new SafeSQL();
            dsWorkItem = qp.getPreparedSqlDataSet(Aqc.getDistinctWorkitemIds(sdi, safeSQL), safeSQL.getValues());
            HashMap<String, DataSet> bstDetails = new HashMap<String, DataSet>();
            DataSet dsAddSDISpec = new DataSet();
            try {
                PreparedStatement psmtGetBSTWIdSpec = this.database.prepareStatement("getWorkItemAndSpec", workitemIdQueryBuffer.toString());
                psmtGetBSTWIdSpec.setString(1, qcBatchId);
                dsAddSDISpec.addColumn("keyid1", 0);
                dsAddSDISpec.addColumn("keyid2", 0);
                dsAddSDISpec.addColumn("keyid3", 0);
                dsAddSDISpec.addColumn("specid", 0);
                dsAddSDISpec.addColumn("specversionid", 0);
                PreparedStatement psmtSpecExists = this.database.prepareStatement("specexists", "SELECT keyid1 FROM sdispec WHERE sdcid = ? AND keyid1 = ?  AND keyid2 = ? AND keyid3 = ? AND specid = ? AND specversionid = ?");
                for (int h = 0; h < qcBatchItemIdArr.length; ++h) {
                    int r;
                    String keyId1 = keyId1Arr[h];
                    String keyId2 = null;
                    String keyId3 = null;
                    String batchSampleTypeId = "";
                    linktoQcBatchItemId = linkToQCBatchItemIdArr[h];
                    String string = batchSampleTypeId = batchSampleTypeIdArr.length == 1 ? batchSampleTypeIdArr[0] : batchSampleTypeIdArr[h];
                    if (!bstDetails.containsKey(batchSampleTypeId)) {
                        psmtGetBSTWIdSpec.setString(2, batchSampleTypeId);
                        bstDetails.put(batchSampleTypeId, new DataSet(psmtGetBSTWIdSpec.executeQuery()));
                    }
                    ds = (DataSet)bstDetails.get(batchSampleTypeId);
                    String workitemid = ds.getValue(0, "workitemid", "");
                    String workitemVersionId = ds.getValue(0, "workitemversionid", "");
                    String specId = ds.getValue(0, "specid", "");
                    String specVersionId = ds.getValue(0, "specversionid", "");
                    if (keyId2Arr.length > 0) {
                        keyId2 = keyId2Arr[h];
                    }
                    if (keyId3Arr.length > 0) {
                        keyId3 = keyId3Arr[h];
                    }
                    if (keyId2 == null || keyId2.trim().length() < 1) {
                        keyId2 = "(null)";
                    }
                    if (keyId3 == null || keyId3.trim().length() < 1) {
                        keyId3 = "(null)";
                    }
                    if (!(sdi = new SDI(sdcId, keyId1, keyId2, keyId3)).isValid()) {
                        errMessage = errMessage + "Invalid Action Input [SDI is not valid]";
                        int n = this.setError("Invalid Action Input [SDI is not valid]");
                        return n;
                    }
                    if (specId != null && specId.trim().length() > 0) {
                        if (specVersionId.trim().equals("") || specVersionId.equalsIgnoreCase("C")) {
                            this.database.createPreparedResultSet("CurrentVersion", "SELECT specversionid FROM spec WHERE specid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (specversionid as numeric) desc", new Object[]{specId});
                            if (this.database.getNext("CurrentVersion")) {
                                specVersionId = this.database.getString("CurrentVersion", "specversionid");
                            }
                        }
                        psmtSpecExists.setString(1, sdcId);
                        psmtSpecExists.setString(2, keyId1);
                        psmtSpecExists.setString(3, keyId2);
                        psmtSpecExists.setString(4, keyId3);
                        psmtSpecExists.setString(5, specId);
                        psmtSpecExists.setString(6, specVersionId);
                        HashMap<String, String> findMap = new HashMap<String, String>();
                        findMap.put("keyid1", keyId1);
                        findMap.put("keyid2", keyId2);
                        findMap.put("keyid3", keyId3);
                        findMap.put("specid", specId);
                        findMap.put("specversionid", specVersionId);
                        if (dsAddSDISpec.findRow(findMap) < 0) {
                            r = dsAddSDISpec.addRow();
                            dsAddSDISpec.setValue(r, "keyid1", keyId1);
                            dsAddSDISpec.setValue(r, "keyid2", keyId2);
                            dsAddSDISpec.setValue(r, "keyid3", keyId3);
                            dsAddSDISpec.setValue(r, "specid", specId);
                            dsAddSDISpec.setValue(r, "specversionid", specVersionId);
                        }
                    }
                    if (workitemid != null && workitemid.length() > 0) {
                        DataSet dsWIs = new DataSet();
                        r = dsWIs.addRow();
                        dsWIs.setString(r, "workitemid", workitemid);
                        dsWIs.setString(r, "workitemversionid", workitemVersionId);
                        batchItemsAndWorkItemsMapping.put(qcBatchItemIdArr[h], dsWIs);
                        keyid1ForAddSDIWorkItem.append(keyId1).append(";");
                        keyid2ForAddSDIWorkItem.append(keyId2).append(";");
                        keyid3ForAddSDIWorkItem.append(keyId3).append(";");
                        workitemidForAddSDIWorkItem.append(workitemid).append(";");
                        workitemVersionIdForAddSDIWorkItem.append(workitemVersionId).append(";");
                        applyworkitemidForAddSDIWorkItem.append("Y").append(";");
                        qcBatchIds.append(qcBatchId).append(";");
                        qcBatchItemIds.append(qcBatchItemIdArr[h]).append(";");
                        wapStatus.append(";").append("Never");
                        continue;
                    }
                    if (linktoQcBatchItemId == null || linktoQcBatchItemId.trim().length() == 0) {
                        if (dsWorkItem != null && dsWorkItem.getRowCount() > 0) {
                            batchItemsAndWorkItemsMapping.put(qcBatchItemIdArr[h], dsWorkItem);
                            for (int count = 0; count < dsWorkItem.getRowCount(); ++count) {
                                keyid1ForAddSDIWorkItem.append(keyId1).append(";");
                                keyid2ForAddSDIWorkItem.append(keyId2).append(";");
                                keyid3ForAddSDIWorkItem.append(keyId3).append(";");
                                workitemidForAddSDIWorkItem.append(dsWorkItem.getValue(count, "workitemid")).append(";");
                                workitemVersionIdForAddSDIWorkItem.append(dsWorkItem.getValue(count, "workitemversionid")).append(";");
                                applyworkitemidForAddSDIWorkItem.append("Y").append(";");
                                qcBatchIds.append(qcBatchId).append(";");
                                qcBatchItemIds.append(qcBatchItemIdArr[h]).append(";");
                                wapStatus.append(";").append("Never");
                            }
                            continue;
                        }
                        errMessage = errMessage + "No Workitems found.";
                        this.logger.warn("No Workitems found.");
                        continue;
                    }
                    DataSet workItems = null;
                    workItems = batchItemsAndWorkItemsMapping.containsKey(linktoQcBatchItemId) ? (DataSet)batchItemsAndWorkItemsMapping.get(linktoQcBatchItemId) : this.__findWorkItems(qcBatchId, linktoQcBatchItemId, qp);
                    if (workItems == null || workItems.getRowCount() == 0) {
                        errMessage = errMessage + "No workitems found.";
                        this.logger.warn("No workitems found.");
                        continue;
                    }
                    batchItemsAndWorkItemsMapping.put(qcBatchItemIdArr[h], workItems);
                    for (int i = 0; i < workItems.getRowCount(); ++i) {
                        keyid1ForAddSDIWorkItem.append(keyId1).append(";");
                        keyid2ForAddSDIWorkItem.append(keyId2).append(";");
                        keyid3ForAddSDIWorkItem.append(keyId3).append(";");
                        workitemidForAddSDIWorkItem.append(workItems.getValue(i, "workitemid")).append(";");
                        workitemVersionIdForAddSDIWorkItem.append(workItems.getValue(i, "workitemversionid")).append(";");
                        wapStatus.append(";").append("Never");
                        applyworkitemidForAddSDIWorkItem.append("Y").append(";");
                        qcBatchIds.append(qcBatchId).append(";");
                        qcBatchItemIds.append(qcBatchItemIdArr[h]).append(";");
                    }
                }
            }
            catch (Exception e) {
                errMessage = errMessage + e.getMessage();
                int psmtSpecExists = this.setError("Exception: " + e.getMessage(), e);
                return psmtSpecExists;
            }
            finally {
                this.database.closeStatement("getWorkItemAndSpec");
                this.database.closeStatement("specexists");
            }
            if (keyid1ForAddSDIWorkItem.toString().trim().length() > 0) {
                String keyid1ForASW = keyid1ForAddSDIWorkItem.substring(0, keyid1ForAddSDIWorkItem.length() - 1);
                String keyid2ForASW = keyid2ForAddSDIWorkItem.substring(0, keyid2ForAddSDIWorkItem.length() - 1);
                String keyid3ForASW = keyid3ForAddSDIWorkItem.substring(0, keyid3ForAddSDIWorkItem.length() - 1);
                String workitemidForASW = workitemidForAddSDIWorkItem.substring(0, workitemidForAddSDIWorkItem.length() - 1);
                String workitemVersionIdForASW = workitemVersionIdForAddSDIWorkItem.substring(0, workitemVersionIdForAddSDIWorkItem.length() - 1);
                String applyworkitemidForASW = applyworkitemidForAddSDIWorkItem.substring(0, applyworkitemidForAddSDIWorkItem.length() - 1);
                String qcBatchIdsForASW = qcBatchIds.substring(0, qcBatchIds.length() - 1);
                String qcBatchItemIdsForASW = qcBatchItemIds.substring(0, qcBatchItemIds.length() - 1);
                HashMap<String, String> actionprops = new HashMap<String, String>();
                try {
                    actionprops.put("propsmatch", "Y");
                    actionprops.put("sdcid", sdcId);
                    actionprops.put("keyid1", keyid1ForASW);
                    actionprops.put("workitemid", workitemidForASW);
                    actionprops.put("workitemversionid", workitemVersionIdForASW);
                    actionprops.put("applyworkitem", applyworkitemidForASW);
                    if ("N".equalsIgnoreCase(createQCSample)) {
                        actionprops.put("forcenew", "R");
                    }
                    actionprops.put("s_qcbatchid", qcBatchIdsForASW);
                    actionprops.put("s_qcbatchitemid", qcBatchItemIdsForASW);
                    actionprops.put("qcapplyaction", "Y");
                    actionprops.put("wapstatus", wapStatus.substring(1));
                    actionprops.put("createworksheet", createWorksheet);
                    ap.processAction("AddSDIWorkitem", "1", actionprops);
                }
                catch (SapphireException exception) {
                    errMessage = errMessage + exception.getMessage();
                    return this.setError("Exception: " + exception.getMessage(), exception);
                }
                if (dsAddSDISpec.getRowCount() > 0) {
                    dsAddSDISpec.sort("specid, specversionid");
                    ArrayList<DataSet> specList = dsAddSDISpec.getGroupedDataSets("specid, specversionid");
                    for (int i = 0; i < specList.size(); ++i) {
                        DataSet specGroup = specList.get(i);
                        actionprops.clear();
                        actionprops.put("sdcid", sdcId);
                        actionprops.put("keyid1", specGroup.getColumnValues("keyid1", ";"));
                        actionprops.put("keyid2", specGroup.getColumnValues("keyid2", ";"));
                        actionprops.put("keyid3", specGroup.getColumnValues("keyid3", ";"));
                        actionprops.put("specid", specGroup.getValue(0, "specid"));
                        actionprops.put("specversionid", specGroup.getValue(0, "specversionid"));
                        try {
                            ap.processAction("AddSDISpec", "1", actionprops);
                            continue;
                        }
                        catch (Exception exception) {
                            errMessage = errMessage + exception.getMessage();
                            return this.setError("Exception: " + exception.getMessage(), exception);
                        }
                    }
                }
            }
            DataSet dsupdate = new DataSet();
            dsupdate.addColumn("linktoqcbatchitemid", 0);
            dsupdate.addColumn("linkedto", 0);
            dsupdate.addColumn("s_qcbatchid", 0);
            dsupdate.addColumn("s_qcbatchitemid", 0);
            String paramListType = "";
            safeSQL.reset();
            DataSet dsParamListType = qp.getPreparedSqlDataSet("SELECT paramlisttype FROM s_qcbatch WHERE s_qcbatchid = " + safeSQL.addVar(qcBatchId), safeSQL.getValues());
            if (dsParamListType != null && dsParamListType.size() > 0) {
                paramListType = dsParamListType.getValue(0, "paramlisttype", "");
            }
            if (paramListType.trim().length() > 0) {
                try {
                    this.excludeDataSetFromQCBatch(qp, sdcId, qcBatchId, paramListType);
                }
                catch (SapphireException e) {
                    errMessage = errMessage + e.getMessage();
                    return this.setError("Exception: " + e.getMessage(), e);
                }
            }
            for (int h = 0; h < qcBatchItemIdArr.length; ++h) {
                String keyId1 = keyId1Arr[h];
                String keyId2 = null;
                String keyId3 = null;
                linktoQcBatchItemId = linkToQCBatchItemIdArr[h];
                String linkedTo = linkedToArr[h];
                if (keyId2Arr.length > 0) {
                    keyId2 = keyId2Arr[h];
                }
                if (keyId3Arr.length > 0) {
                    keyId3 = keyId3Arr[h];
                }
                if (keyId2 == null || keyId2.trim().length() < 1) {
                    keyId2 = "(null)";
                }
                if (keyId3 == null || keyId3.trim().length() < 1) {
                    keyId3 = "(null)";
                }
                if (!(sdi = new SDI(sdcId, keyId1, keyId2, keyId3)).isValid()) {
                    errMessage = errMessage + "Invalid Action Input [SDI is not valid]";
                    return this.setError("Invalid Action Input [SDI is not valid]");
                }
                if (linktoQcBatchItemId == null || linktoQcBatchItemId.length() <= 0) continue;
                int r = dsupdate.addRow();
                dsupdate.setValue(r, "linktoqcbatchitemid", linktoQcBatchItemId);
                dsupdate.setValue(r, "linkedto", linkedTo);
                dsupdate.setValue(r, "s_qcbatchid", qcBatchId);
                dsupdate.setValue(r, "s_qcbatchitemid", qcBatchItemIdArr[h]);
            }
            if (dsupdate.size() > 0) {
                String[] keycols = new String[]{"s_qcbatchid", "s_qcbatchitemid"};
                try {
                    DataSetUtil.update(this.database, dsupdate, "s_qcbatchitem", keycols);
                }
                catch (Exception e) {
                    errMessage = errMessage + e.getMessage();
                    return this.setError("Exception: " + e.getMessage(), e);
                }
            }
        }
        props.put("ACTION_ERRORLOG", errMessage);
        return this.rc;
    }

    private DataSet __findWorkItems(String qcBatchId, String linktoQcBatchItemId, QueryProcessor qp) {
        String linkToQcBatchItemIdAgain = "";
        try {
            SDI sdi = new SDI("QCBatch", qcBatchId, null, null);
            SafeSQL safeSQL = new SafeSQL();
            DataSet dsWorkItem = qp.getPreparedSqlDataSet(Aqc.getDistinctWorkitemIdsForLinkedQCBatchItemId(sdi, linktoQcBatchItemId, safeSQL), safeSQL.getValues());
            if (dsWorkItem == null || dsWorkItem.getRowCount() == 0) {
                safeSQL.reset();
                DataSet dsSampleId = qp.getPreparedSqlDataSet(Aqc.getSampleIDForQCBatchitemID(qcBatchId, linktoQcBatchItemId, safeSQL), safeSQL.getValues());
                if (dsSampleId == null || dsSampleId.getRowCount() == 0) {
                    return null;
                }
                safeSQL.reset();
                DataSet dsBatchItemId = qp.getPreparedSqlDataSet(Aqc.getLinkedQCBatchitemID(qcBatchId, linktoQcBatchItemId, safeSQL), safeSQL.getValues());
                if (dsBatchItemId == null || dsBatchItemId.getRowCount() == 0) {
                    return null;
                }
                linkToQcBatchItemIdAgain = dsBatchItemId.getValue(0, "linktoqcbatchitemid");
                return this.__findWorkItems(qcBatchId, linkToQcBatchItemIdAgain, qp);
            }
            return dsWorkItem;
        }
        catch (Exception ae) {
            this.setError("Exception:" + ae.getMessage());
            return null;
        }
    }

    private void excludeDataSetFromQCBatch(QueryProcessor qp, String sdcId, String qcBatchId, String paramListType) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        DataSet dsExclude = qp.getPreparedSqlDataSet("SELECT sd.sdcid, sd.keyid1, sd.keyid2, sd.keyid3, sd.paramlistid, sd.paramlistversionid, sd.variantid, sd.dataset, s_qcbatchid, s_qcbatchitemid  FROM sdidata sd, paramlist pm WHERE sd.paramlistid = pm.paramlistid AND sd.paramlistversionid = pm.paramlistversionid  AND sd.variantid = pm.variantid AND sd.sdcid = " + safeSQL.addVar(sdcId) + " AND pm.s_paramlisttype != " + safeSQL.addVar(paramListType) + " AND sd.s_qcbatchid = " + safeSQL.addVar(qcBatchId), safeSQL.getValues());
        if (dsExclude != null && dsExclude.size() > 0) {
            for (int row = 0; row < dsExclude.size(); ++row) {
                dsExclude.setValue(row, "s_qcbatchid", null);
                dsExclude.setValue(row, "s_qcbatchitemid", null);
            }
            String[] keyColIds = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset"};
            DataSetUtil.update(this.database, dsExclude, "sdidata", keyColIds);
        }
    }
}

