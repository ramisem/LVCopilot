/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.qcbatch;

import com.labvantage.opal.actions.qcactions.QCBaseAction;
import com.labvantage.opal.qcbatch.QCBatchItem;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.qcbatch.QCBatchSampleType;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.util.PLDataSet;
import com.labvantage.opal.util.SDIDataSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;

public class QCBatch {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 93027 $";
    public static final String STATUS_INITIAL = "Initial";
    public static final String STATUS_PREPPING = "Prepping";
    public static final String STATUS_READY = "Ready";
    public static final String STATUS_STARTED = "Started";
    public static final String STATUS_INPROGRESS = "InProgress";
    public static final String STATUS_DATAENTERED = "DataEntered";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_EVALUATED = "Evaluated";
    public static final String STATUS_REVIEWED = "Reviewed";
    public static final String EVAL_CONTINUOUS = "Continuous";
    public static final String EVAL_CONCLUSION = "Conclusion";
    public static final String EVAL_MANUAL = "Manual";
    public static final String EVAL_USER_SPECIFIED = "User Specified";
    public static final String EVAL_NONE = "None";
    private String __QCBatchID;
    private String __Status;
    private String __QCMethodID;
    private String __QCMethodVersionID;
    private String __QCBatchSDC;
    private String __EvalOption;
    private String __ActionSuccess;
    private String __ActionFailure;
    private String __QCBatchQueryId;
    private String __QCBatchBasedOnId;
    private String __ApproveOnReviewFlag;
    private String __ReviewOnPassFlag;
    private String __EvaluationDisposition;
    private String __ReviewDisposition;
    private String __ParamListType;
    private int __BatchQueryCount;
    private List __QCBatchItemList;
    private HashMap<String, QCBatchSampleType> __QCBatchSampleTypeMap;
    private QueryProcessor __QueryProcessor;
    private Calendar __ModDate;
    private String __CreateBy;
    private boolean __PreppedFlag;
    private boolean __CompletedFlag;
    private boolean __DataEnteredFlag;
    private boolean __InProgressFlag;
    private String __SpecCondition;
    private String __QCBatchType;
    private String __BlockFlag;
    private String __SiteDepartmentId;
    private SQLGenerator __SqlGenerator;
    private int __InstanceCount;
    private long __InstanceTimeMillis;
    private boolean primaryFlag = false;

    public QCBatch(QueryProcessor __QueryProcessor) {
        this.__QueryProcessor = __QueryProcessor;
    }

    public QCBatch(QueryProcessor queryProcessor, String qcbatchid) throws SapphireException {
        this.__QueryProcessor = queryProcessor;
        this.__QCBatchID = qcbatchid;
        this.populatePrimaryInfo();
        SafeSQL safeSQL = new SafeSQL();
        if (this.primaryFlag) {
            int i;
            this.__QCBatchSampleTypeMap = new HashMap();
            this.__QCBatchItemList = new ArrayList();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT S_QCBATCHSAMPLETYPEID FROM S_QCBATCHSAMPLETYPE");
            sql.append(" WHERE QCBATCHID = " + safeSQL.addVar(this.__QCBatchID));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (i = 0; i < ds.size(); ++i) {
                    String qcBatchSampleTypeId = ds.getValue(i, "S_QCBATCHSAMPLETYPEID");
                    this.__QCBatchSampleTypeMap.put(qcBatchSampleTypeId, new QCBatchSampleType(this.__QueryProcessor, qcBatchSampleTypeId));
                }
            }
            safeSQL.reset();
            sql.setLength(0);
            sql.append("SELECT T1.S_QCBATCHITEMID, T1.QCBATCHITEMDESC, T1.QCBATCHSAMPLETYPEID,");
            sql.append(" T1.USERSEQUENCE, T1.LINKTOQCBATCHITEMID");
            sql.append(" FROM S_QCBATCHITEM T1");
            sql.append(" WHERE T1.S_QCBATCHID = " + safeSQL.addVar(this.__QCBatchID));
            sql.append(" ORDER BY T1.USERSEQUENCE");
            ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (i = 0; i < ds.size(); ++i) {
                    QCBatchSampleType st;
                    String qcBatchItemId = ds.getValue(i, "S_QCBATCHITEMID");
                    String qcBatchSampleTypeId = ds.getValue(i, "QCBATCHSAMPLETYPEID");
                    QCBatchItem qcBatchItem = new QCBatchItem(this.__QueryProcessor, this.__QCBatchID, qcBatchItemId);
                    qcBatchItem.setQCBatchItemDesc(ds.getValue(i, "QCBATCHITEMDESC"));
                    qcBatchItem.setLinkedToBatchItemID(ds.getValue(i, "LINKTOQCBATCHITEMID"));
                    qcBatchItem.setItemPosition(ds.getInt(i, "USERSEQUENCE"));
                    if (qcBatchSampleTypeId != null && qcBatchSampleTypeId.length() > 0 && (st = this.__QCBatchSampleTypeMap.get(qcBatchSampleTypeId)) != null) {
                        qcBatchItem.setQCBatchSampleTypeID(qcBatchSampleTypeId);
                        qcBatchItem.setQCSampleType(st.getColumnValue("qcsampletype"));
                        qcBatchItem.setActionApply(st.getColumnValue("actionapply"));
                        qcBatchItem.setActionCalc(st.getColumnValue("actioncalc"));
                        qcBatchItem.setActionEval(st.getColumnValue("actioneval"));
                        qcBatchItem.setAQCStatus(st.getColumnValue("evalstatus"));
                        qcBatchItem.setStandardLevel(st.getColumnValue("standardlevel"));
                    }
                    this.__QCBatchItemList.add(qcBatchItem);
                }
            }
        } else {
            throw new SapphireException("No QCBatch exists with ID '" + qcbatchid + "'");
        }
        this.__InstanceTimeMillis = System.currentTimeMillis();
    }

    private void populatePrimaryInfo() {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT S_QCBATCHID, QCMETHODID, QCMETHODVERSIONID, QCBATCHSDCID, QCBATCHSTATUS, EVALOPTION, ACTIONSUCCESS,");
        sql.append(" ACTIONFAILURE, QCBATCHQUERYID, QCBATCHBASEDONID, QCBATCHQUERYCOUNT, MODDT, CREATEBY,");
        sql.append(" APPROVEONREVIEWFLAG, REVIEWDISPOSITION, EVALUATIONDISPOSITION, REVIEWONPASSFLAG, PARAMLISTTYPE,");
        sql.append(" SPECCONDITION, QCBATCHTYPE, BLOCKFLAG, SITEDEPARTMENTID FROM S_QCBATCH WHERE S_QCBATCHID = " + safeSQL.addVar(this.__QCBatchID));
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            this.__Status = ds.getValue(0, "QCBATCHSTATUS");
            this.__QCMethodID = ds.getValue(0, "QCMETHODID");
            this.__QCMethodVersionID = ds.getValue(0, "QCMETHODVERSIONID");
            this.__QCBatchSDC = ds.getValue(0, "QCBATCHSDCID");
            this.__EvalOption = ds.getValue(0, "EVALOPTION");
            this.__ActionSuccess = ds.getValue(0, "ACTIONSUCCESS");
            this.__ActionFailure = ds.getValue(0, "ACTIONFAILURE");
            this.__QCBatchQueryId = ds.getValue(0, "QCBATCHQUERYID");
            this.__QCBatchBasedOnId = ds.getValue(0, "QCBATCHBASEDONID");
            this.__ApproveOnReviewFlag = ds.getValue(0, "APPROVEONREVIEWFLAG");
            this.__ReviewOnPassFlag = ds.getValue(0, "REVIEWONPASSFLAG");
            this.__EvaluationDisposition = ds.getValue(0, "EVALUATIONDISPOSITION");
            this.__ReviewDisposition = ds.getValue(0, "REVIEWDISPOSITION");
            this.__BatchQueryCount = ds.getInt(0, "QCBATCHQUERYCOUNT");
            this.__ModDate = ds.getCalendar(0, "MODDT");
            this.__CreateBy = ds.getValue(0, "CREATEBY");
            this.__ParamListType = ds.getValue(0, "PARAMLISTTYPE");
            this.__SpecCondition = ds.getValue(0, "SPECCONDITION");
            this.__QCBatchType = ds.getValue(0, "QCBATCHTYPE");
            this.__BlockFlag = ds.getValue(0, "blockflag");
            this.__SiteDepartmentId = ds.getValue(0, "sitedepartmentid");
            this.primaryFlag = true;
        }
    }

    public void setQCBatchID(String __QCBatchID) {
        this.__QCBatchID = __QCBatchID;
    }

    public List getQCBatchItems() {
        return this.__QCBatchItemList;
    }

    public QCBatchItem getQCBatchItem(String qcBatchItemID) {
        QCBatchItem returnObject = null;
        for (int i = 0; i < this.__QCBatchItemList.size(); ++i) {
            QCBatchItem item = (QCBatchItem)this.__QCBatchItemList.get(i);
            if (!item.getQCBatchItemID().equals(qcBatchItemID)) continue;
            returnObject = item;
            break;
        }
        return returnObject;
    }

    public QCBatchItem getNextBatchItem(QCBatchItem qcBatchItem) {
        int index = this.__QCBatchItemList.indexOf(qcBatchItem);
        if (index != -1 && index < this.__QCBatchItemList.size() - 1) {
            return (QCBatchItem)this.__QCBatchItemList.get(++index);
        }
        return null;
    }

    public QCBatchItem getPreviousBatchItem(QCBatchItem qcBatchItem) {
        int index = this.__QCBatchItemList.indexOf(qcBatchItem);
        if (index != -1 && index > 0) {
            return (QCBatchItem)this.__QCBatchItemList.get(--index);
        }
        return null;
    }

    public List getQCBatchItemBracketItems(QCBatchItem qcBatchItem) {
        String applyActionId;
        if (qcBatchItem.isQCSample() && (applyActionId = qcBatchItem.getActionApply()) != null && !applyActionId.equals("")) {
            try {
                QCBaseAction.getParameterType(this.__QueryProcessor, applyActionId);
            }
            catch (Exception e) {
                Logger.logError("Error getting parameter type", e);
            }
        }
        return null;
    }

    public QCBatchItem getBracketParent(String qcBatchItemId) {
        QCBatchItem item = null;
        QCBatchItem qcBatchItem = new QCBatchItem(this.__QueryProcessor, this.__QCBatchID, qcBatchItemId);
        int index = this.__QCBatchItemList.indexOf(qcBatchItem);
        while (index > 0) {
            QCBatchItem _qcBatchItem;
            if (!this.isBracketAllowed(_qcBatchItem = (QCBatchItem)this.__QCBatchItemList.get(--index))) continue;
            item = _qcBatchItem;
            break;
        }
        return item;
    }

    public boolean isBracketAllowed(QCBatchItem qcBatchItem) {
        String calcActionId;
        boolean bool = false;
        if (this.__QCBatchItemList.contains(qcBatchItem) && qcBatchItem.isQCSample() && (calcActionId = qcBatchItem.getActionCalc()) != null && calcActionId.length() > 0) {
            try {
                bool = QCBaseAction.hasBracket(this.__QueryProcessor, calcActionId);
            }
            catch (Exception e) {
                Logger.logError(e.getMessage(), e);
            }
        }
        return bool;
    }

    public String getStatus() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__Status;
    }

    public String getSiteDepartmentId() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__SiteDepartmentId;
    }

    public boolean hasPreparationalDataSets() {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT T1.SDCID, T1.KEYID1");
        sql.append(" FROM SDIDATA T1, PARAMLIST T2");
        sql.append(" WHERE T1.SDCID = " + safeSQL.addVar(this.getQCBatchSDC()));
        sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
        sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
        sql.append(" AND T1.VARIANTID = T2.VARIANTID");
        sql.append(" AND T2.S_PARAMLISTTYPE = 'Preparation'");
        sql.append(" AND T1.S_QCBATCHID = " + safeSQL.addVar(this.getQCBatchID()));
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            return ds.size() != 0;
        }
        return false;
    }

    public int getPreparationalDataSetsCount() {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT T1.SDCID, T1.KEYID1");
        sql.append(" FROM SDIDATA T1, PARAMLIST T2");
        sql.append(" WHERE T1.SDCID = " + safeSQL.addVar(this.getQCBatchSDC()));
        sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
        sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
        sql.append(" AND T1.VARIANTID = T2.VARIANTID");
        sql.append(" AND T2.S_PARAMLISTTYPE = 'Preparation'");
        sql.append(" AND T1.S_QCBATCHID = " + safeSQL.addVar(this.getQCBatchID()));
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            return ds.size();
        }
        return 0;
    }

    public boolean isPrepped(boolean reevaluateStatus) {
        if (reevaluateStatus) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT T1.SDCID, T1.KEYID1");
            sql.append(" FROM SDIDATA T1, PARAMLIST T2, SDIDATAITEM T3");
            sql.append(" WHERE T1.SDCID = " + safeSQL.addVar(this.getQCBatchSDC()));
            sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
            sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
            sql.append(" AND T1.VARIANTID = T2.VARIANTID");
            sql.append(" AND T2.S_PARAMLISTTYPE = 'Preparation'");
            sql.append(" AND T1.S_QCBATCHID = " + safeSQL.addVar(this.getQCBatchID()));
            sql.append(" AND T3.SDCID = T1.SDCID");
            sql.append(" AND T3.KEYID1 = T1.KEYID1");
            sql.append(" AND T3.PARAMLISTID = T1.PARAMLISTID");
            sql.append(" AND T3.PARAMLISTVERSIONID = T1.PARAMLISTVERSIONID");
            sql.append(" AND T3.VARIANTID = T1.VARIANTID");
            sql.append(" AND T3.DATASET = T1.DATASET");
            sql.append(" AND ( T3.ENTEREDTEXT IS NULL OR T3.ENTEREDTEXT = '(null)' )");
            sql.append(" UNION ALL ");
            sql.append("SELECT T1.SDCID, T1.KEYID1");
            sql.append(" FROM SDIDATA T1, PARAMLIST T2, SDIDATAITEM T3");
            sql.append(" WHERE  T1.SDCID = 'QCBatch' ");
            sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
            sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
            sql.append(" AND T1.VARIANTID = T2.VARIANTID");
            sql.append(" AND T2.S_PARAMLISTTYPE = 'Preparation'");
            sql.append(" AND T1.KEYID1 = " + safeSQL.addVar(this.getQCBatchID()));
            sql.append(" AND T3.SDCID = T1.SDCID");
            sql.append(" AND T3.KEYID1 = T1.KEYID1");
            sql.append(" AND T3.PARAMLISTID = T1.PARAMLISTID");
            sql.append(" AND T3.PARAMLISTVERSIONID = T1.PARAMLISTVERSIONID");
            sql.append(" AND T3.VARIANTID = T1.VARIANTID");
            sql.append(" AND T3.DATASET = T1.DATASET");
            sql.append(" AND ( T3.ENTEREDTEXT IS NULL OR T3.ENTEREDTEXT = '(null)' )");
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                this.__PreppedFlag = ds.size() == 0;
            }
        }
        return this.__PreppedFlag;
    }

    public DataSet getPrepationalDataSetsInfo() {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT T1.SDCID, T1.KEYID1, T1.KEYID2, T1.KEYID3, T1.PARAMLISTID, T1.PARAMLISTVERSIONID,");
        sql.append(" T1.VARIANTID, T1.DATASET, T3.PARAMID, T3.REPLICATEID, T3.ENTEREDTEXT");
        sql.append(" FROM SDIDATA T1, PARAMLIST T2, SDIDATAITEM T3");
        sql.append(" WHERE T1.SDCID = " + safeSQL.addVar(this.getQCBatchSDC()));
        sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
        sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
        sql.append(" AND T1.VARIANTID = T2.VARIANTID");
        sql.append(" AND T2.S_PARAMLISTTYPE = 'Preparation'");
        sql.append(" AND T1.S_QCBATCHID = " + safeSQL.addVar(this.getQCBatchID()));
        sql.append(" AND T3.SDCID = T1.SDCID");
        sql.append(" AND T3.KEYID1 = T1.KEYID1");
        sql.append(" AND T3.PARAMLISTID = T1.PARAMLISTID");
        sql.append(" AND T3.PARAMLISTVERSIONID = T1.PARAMLISTVERSIONID");
        sql.append(" AND T3.VARIANTID = T1.VARIANTID");
        sql.append(" AND T3.DATASET = T1.DATASET");
        sql.append(" UNION ");
        sql.append("SELECT T1.SDCID, T1.KEYID1, T1.KEYID2, T1.KEYID3, T1.PARAMLISTID, T1.PARAMLISTVERSIONID,");
        sql.append(" T1.VARIANTID, T1.DATASET, T3.PARAMID, T3.REPLICATEID, T3.ENTEREDTEXT");
        sql.append(" FROM SDIDATA T1, PARAMLIST T2, SDIDATAITEM T3");
        sql.append(" WHERE  T1.SDCID = 'QCBatch' ");
        sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
        sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
        sql.append(" AND T1.VARIANTID = T2.VARIANTID");
        sql.append(" AND T2.S_PARAMLISTTYPE = 'Preparation'");
        sql.append(" AND  T1.KEYID1 = " + safeSQL.addVar(this.getQCBatchID()));
        sql.append(" AND T3.SDCID = T1.SDCID");
        sql.append(" AND T3.KEYID1 = T1.KEYID1");
        sql.append(" AND T3.PARAMLISTID = T1.PARAMLISTID");
        sql.append(" AND T3.PARAMLISTVERSIONID = T1.PARAMLISTVERSIONID");
        sql.append(" AND T3.VARIANTID = T1.VARIANTID");
        sql.append(" AND T3.DATASET = T1.DATASET");
        return this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    public boolean isCompleted(boolean reevaluateStatus) {
        if (reevaluateStatus) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT COUNT(*) COUNT");
            sql.append(" FROM SDIDATA T1, PARAMLIST T2");
            sql.append(" WHERE T1.SDCID = " + safeSQL.addVar(this.getQCBatchSDC()));
            sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
            sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
            sql.append(" AND T1.VARIANTID = T2.VARIANTID");
            sql.append(" AND T1.S_DATASETSTATUS <> 'Cancelled' AND ( T1.S_DATASETSTATUS <> 'Completed')");
            sql.append(" AND T1.AVAILABILITYFLAG = 'Y'");
            sql.append(" AND  T1.S_QCBATCHID = " + safeSQL.addVar(this.getQCBatchID()));
            sql.append(" UNION ALL ");
            sql.append("SELECT COUNT(*) COUNT");
            sql.append(" FROM SDIDATA T1, PARAMLIST T2");
            sql.append(" WHERE T1.SDCID = 'QCBatch' ");
            sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
            sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
            sql.append(" AND T1.VARIANTID = T2.VARIANTID");
            sql.append(" AND T1.S_DATASETSTATUS <> 'Cancelled' AND ( T1.S_DATASETSTATUS <> 'Completed')");
            sql.append(" AND  T1.KEYID1 = " + safeSQL.addVar(this.getQCBatchID()));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                this.__CompletedFlag = true;
                for (int r = 0; r < ds.getRowCount(); ++r) {
                    if (ds.getInt(r, "count") <= 0) continue;
                    this.__CompletedFlag = false;
                    break;
                }
            }
        }
        return this.__CompletedFlag;
    }

    public boolean isDataEntered(boolean reevaluateStatus) {
        if (reevaluateStatus) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT COUNT(*) COUNT");
            sql.append(" FROM SDIDATA T1, PARAMLIST T2");
            sql.append(" WHERE  T1.SDCID = " + safeSQL.addVar(this.getQCBatchSDC()));
            sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
            sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
            sql.append(" AND T1.VARIANTID = T2.VARIANTID");
            sql.append(" AND T1.S_DATASETSTATUS  IN ( 'Initial','");
            sql.append("InProgress' )");
            sql.append(" AND T1.AVAILABILITYFLAG = 'Y'");
            sql.append(" AND  T1.S_QCBATCHID = " + safeSQL.addVar(this.getQCBatchID()));
            sql.append(" UNION ALL ");
            sql.append("SELECT COUNT(*) COUNT");
            sql.append(" FROM SDIDATA T1, PARAMLIST T2");
            sql.append(" WHERE T1.SDCID = 'QCBatch' ");
            sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
            sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
            sql.append(" AND T1.VARIANTID = T2.VARIANTID");
            sql.append(" AND T1.S_DATASETSTATUS IN ( 'Initial','");
            sql.append("InProgress')");
            sql.append(" AND  T1.KEYID1 = " + safeSQL.addVar(this.getQCBatchID()));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                this.__DataEnteredFlag = true;
                for (int r = 0; r < ds.getRowCount(); ++r) {
                    if (ds.getInt(r, "count") <= 0) continue;
                    this.__DataEnteredFlag = false;
                    break;
                }
            }
        }
        return this.__DataEnteredFlag;
    }

    public boolean isInProgress(boolean reevaluateStatus) {
        if (reevaluateStatus) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT COUNT(*) COUNT");
            sql.append(" FROM SDIDATA T1, PARAMLIST T2");
            sql.append(" WHERE T1.SDCID = " + safeSQL.addVar(this.getQCBatchSDC()));
            sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
            sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
            sql.append(" AND T1.VARIANTID = T2.VARIANTID");
            sql.append(" AND T1.S_DATASETSTATUS <> 'Cancelled' AND T1.S_DATASETSTATUS IN ( 'InProgress', '");
            sql.append("DataEntered','Completed', '");
            sql.append("Released', 'Remeasured', '");
            sql.append("Retested') ");
            sql.append(" AND  T1.S_QCBATCHID = " + safeSQL.addVar(this.getQCBatchID()));
            sql.append(" UNION ALL ");
            sql.append("SELECT COUNT(*) COUNT");
            sql.append(" FROM SDIDATA T1, PARAMLIST T2");
            sql.append(" WHERE T1.SDCID = 'QCBatch' ");
            sql.append(" AND T1.PARAMLISTID = T2.PARAMLISTID");
            sql.append(" AND T1.PARAMLISTVERSIONID = T2.PARAMLISTVERSIONID");
            sql.append(" AND T1.VARIANTID = T2.VARIANTID");
            sql.append(" AND T1.S_DATASETSTATUS <> 'Cancelled' AND T1.S_DATASETSTATUS IN ( 'InProgress', '");
            sql.append("DataEntered','Completed', '");
            sql.append("Released', 'Remeasured', '");
            sql.append("Retested') ");
            sql.append(" AND T1.KEYID1 = " + safeSQL.addVar(this.getQCBatchID()));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                this.__InProgressFlag = false;
                for (int r = 0; r < ds.getRowCount(); ++r) {
                    if (ds.getInt(r, "count") <= 0) continue;
                    this.__InProgressFlag = true;
                    break;
                }
            }
        }
        return this.__InProgressFlag;
    }

    public String getQCMethodID() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__QCMethodID;
    }

    public String getQCMethodVersionID() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__QCMethodVersionID;
    }

    public String getQCBatchSDC() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__QCBatchSDC;
    }

    public String getEvalOption() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__EvalOption;
    }

    public String getActionSuccess() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__ActionSuccess;
    }

    public String getActionFailure() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__ActionFailure;
    }

    public String getQCBatchID() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__QCBatchID;
    }

    public String getQCBatchQueryId() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__QCBatchQueryId;
    }

    public String getQCBatchBasedOnId() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__QCBatchBasedOnId;
    }

    public int getBatchQueryCount() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__BatchQueryCount;
    }

    public String getReviewDisposition() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__ReviewDisposition;
    }

    public String getEvaluationDisposition() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__EvaluationDisposition;
    }

    public String getApproveOnReviewFlag() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__ApproveOnReviewFlag;
    }

    public String getReviewOnPassFlag() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__ReviewOnPassFlag;
    }

    public String getCreateBy() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__CreateBy;
    }

    public String getParamListType() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__ParamListType;
    }

    public String getSpecCondition() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__SpecCondition;
    }

    public String getQCBatchType() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__QCBatchType;
    }

    public String getBlockFlag() {
        if (!this.primaryFlag) {
            this.populatePrimaryInfo();
        }
        return this.__BlockFlag;
    }

    public List getDataSets(boolean onlyQCDataSets) {
        if (this.__QCBatchID == null) {
            return null;
        }
        ArrayList<SDIDataSet> list = new ArrayList<SDIDataSet>();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if (!onlyQCDataSets) {
            sql.append("SELECT SDCID, KEYID1, KEYID2, KEYID3, PARAMLISTID, PARAMLISTVERSIONID, VARIANTID, DATASET");
            sql.append(" FROM SDIDATA");
            sql.append(" WHERE S_QCBATCHID = " + safeSQL.addVar(this.__QCBatchID));
            sql.append(" ORDER BY KEYID1");
        } else {
            sql.append(this.__SqlGenerator.getQCDataSets(this.__QCBatchID, safeSQL));
        }
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                SDIDataSet sdiDataSet = new SDIDataSet(ds.getValue(i, "sdcid"), ds.getValue(i, "keyid1"), ds.getValue(i, "keyid2"), ds.getValue(i, "keyid3"), ds.getValue(i, "paramlistid"), ds.getValue(i, "paramlistversionid"), ds.getValue(i, "variantid"), ds.getValue(i, "dataset"), this.__QueryProcessor);
                list.add(sdiDataSet);
            }
        }
        return list;
    }

    public static List getQCDataSetList(QueryProcessor qp, String sdcid, String keyid1, List plDataSetList) throws SapphireException {
        ArrayList<SDIDataSet> qcDataSetList = new ArrayList<SDIDataSet>();
        if (sdcid != null && keyid1 != null) {
            DAMProcessor dam = new DAMProcessor(qp.getConnectionid());
            String rset_id = "";
            if (plDataSetList != null && plDataSetList.size() > 0) {
                StringBuffer plId = new StringBuffer();
                StringBuffer plVerId = new StringBuffer();
                StringBuffer plVarId = new StringBuffer();
                StringBuffer plDatasetId = new StringBuffer();
                for (int i = 0; i < plDataSetList.size(); ++i) {
                    PLDataSet plDataSet = (PLDataSet)plDataSetList.get(i);
                    plId.append(";").append(plDataSet.getParamListId());
                    plVerId.append(";").append(plDataSet.getParamListVersionId());
                    plVarId.append(";").append(plDataSet.getVariantId());
                    plDatasetId.append(";").append(plDataSet.getDataset());
                }
                long startTime = System.currentTimeMillis();
                rset_id = dam.createRSetDS(sdcid, keyid1, null, null, plId.substring(1), plVerId.substring(1), plVarId.substring(1), plDatasetId.substring(1), true, true, false);
                long endTime = System.currentTimeMillis();
                Logger.logDebug("Time taken in miliseconds to create Rset using createRsetDS... " + (endTime - startTime));
            } else {
                rset_id = dam.getAllDSRSet(sdcid, keyid1, null, null);
            }
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT SDCID, KEYID1, KEYID2, KEYID3, PARAMLISTID, PARAMLISTVERSIONID, VARIANTID, DATASET");
            sql.append(" FROM SDIDATA");
            sql.append(" WHERE SDCID = " + safeSQL.addVar(sdcid));
            sql.append(" AND keyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid = " + safeSQL.addVar(rset_id) + ")");
            sql.append(" AND KEYID2 = '(null)'");
            sql.append(" AND KEYID3 = '(null)'");
            sql.append(" AND S_QCBATCHID IS NOT NULL");
            DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            dam.clearRSet(rset_id);
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    SDIDataSet sdiDataSet = null;
                    String paramlistid = ds.getValue(i, "PARAMLISTID");
                    String paramlistversionid = ds.getValue(i, "PARAMLISTVERSIONID");
                    String variantid = ds.getValue(i, "VARIANTID");
                    String dataset = ds.getValue(i, "DATASET");
                    if (plDataSetList != null) {
                        PLDataSet plds = new PLDataSet(paramlistid, paramlistversionid, variantid, dataset);
                        if (!plDataSetList.contains(plds)) continue;
                        sdiDataSet = new SDIDataSet(ds.getValue(i, "SDCID"), ds.getValue(i, "KEYID1"), ds.getValue(i, "KEYID2"), ds.getValue(i, "KEYID3"), paramlistid, paramlistversionid, variantid, dataset, qp);
                        qcDataSetList.add(sdiDataSet);
                        continue;
                    }
                    sdiDataSet = new SDIDataSet(ds.getValue(i, "SDCID"), ds.getValue(i, "KEYID1"), ds.getValue(i, "KEYID2"), ds.getValue(i, "KEYID3"), paramlistid, paramlistversionid, variantid, dataset, qp);
                    qcDataSetList.add(sdiDataSet);
                }
            }
        } else {
            throw new SapphireException("Invalid input. One of the input is null.");
        }
        return qcDataSetList;
    }

    public int lowerInstanceCount(Object o) {
        if (o instanceof QCBatchPool && this.__InstanceCount > 0) {
            --this.__InstanceCount;
        }
        return this.__InstanceCount;
    }

    public int raiseInstanceCount(Object o) {
        if (o instanceof QCBatchPool) {
            this.__InstanceTimeMillis = System.currentTimeMillis();
            ++this.__InstanceCount;
        }
        return this.__InstanceCount;
    }

    public int getInstanceCount() {
        return this.__InstanceCount;
    }

    public long getInstanceTimeMillis() {
        return this.__InstanceTimeMillis;
    }

    public Calendar getModDate(boolean refresh) {
        if (refresh) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT MODDT FROM S_QCBATCH WHERE S_QCBATCHID = " + safeSQL.addVar(this.getQCBatchID()));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                return ds.getCalendar(0, "MODDT");
            }
        }
        return null;
    }

    public Timestamp getModDateTimeStamp(boolean refresh) {
        if (refresh) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT MODDT FROM S_QCBATCH WHERE S_QCBATCHID = " + safeSQL.addVar(this.getQCBatchID()));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                return ds.getTimestamp(0, "MODDT");
            }
        }
        return null;
    }

    public boolean isModified() {
        Calendar calendar = this.getModDate(true);
        if (this.__ModDate == null) {
            this.__ModDate = calendar;
            return true;
        }
        Timestamp currentmoddt_timestamp = this.getModDateTimeStamp(true);
        Timestamp moddt_timestamp = new Timestamp(this.__ModDate.getTime().getTime());
        return moddt_timestamp.before(currentmoddt_timestamp);
    }

    public void setSQLGenerator(SQLGenerator sqlGenerator) {
        this.__SqlGenerator = sqlGenerator;
    }

    public SQLGenerator getSQLGenerator() {
        return this.__SqlGenerator;
    }

    public void setQueryProcessor(QueryProcessor qp) {
        this.__QueryProcessor = qp;
        if (this.__QCBatchItemList != null && this.__QCBatchItemList.size() > 0) {
            for (int i = 0; i < this.__QCBatchItemList.size(); ++i) {
                QCBatchItem qcbi = (QCBatchItem)this.__QCBatchItemList.get(i);
                qcbi.setQueryProcessor(qp);
            }
        }
        if (this.__QCBatchSampleTypeMap != null) {
            Iterator<String> iterator = this.__QCBatchSampleTypeMap.keySet().iterator();
            while (iterator.hasNext()) {
                QCBatchSampleType batchSampleType = this.__QCBatchSampleTypeMap.get(iterator.next());
                batchSampleType.setQueryProcessor(qp);
            }
        }
    }

    public QueryProcessor getQueryProcessor() {
        return this.__QueryProcessor;
    }

    public QCBatchSampleType getQCBatchSampleType(String batchSampleTypeId) {
        return this.__QCBatchSampleTypeMap.get(batchSampleTypeId);
    }
}

