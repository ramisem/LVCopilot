/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.qcbatch;

import com.labvantage.opal.util.SDIDataSet;
import com.labvantage.sapphire.SDI;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class QCBatchItem {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53656 $";
    private String __QCBatchID;
    private String __QCBatchItemID;
    private String __QCBatchItemDesc;
    private String __QCBatchSampleTypeID;
    private String __LinkedToBatchItemID;
    private String __QCSampleType;
    private String __ActionApply;
    private String __ActionCalc;
    private String __ActionEval;
    private String __AQCStatus;
    private int __ItemPosition;
    private String __StandardLevel;
    private SDIDataSet __SDIDataSet;
    private QueryProcessor __QueryProcessor;

    public QCBatchItem(QueryProcessor queryProcessor, String qcBatchId, String qcbatchItemId) {
        this.__QueryProcessor = queryProcessor;
        this.__QCBatchID = qcBatchId;
        this.__QCBatchItemID = qcbatchItemId;
    }

    public String getQCBatchItemDesc() {
        return this.__QCBatchItemDesc;
    }

    public void setQCBatchItemDesc(String __QCBatchItemDesc) {
        this.__QCBatchItemDesc = __QCBatchItemDesc;
    }

    public String getQCBatchSampleTypeID() {
        return this.__QCBatchSampleTypeID;
    }

    public void setQCBatchSampleTypeID(String __QCBatchSampleTypeID) {
        this.__QCBatchSampleTypeID = __QCBatchSampleTypeID;
    }

    public String getLinkedToBatchItemID() {
        return this.__LinkedToBatchItemID;
    }

    public void setLinkedToBatchItemID(String __LinkedToBatchItemID) {
        this.__LinkedToBatchItemID = __LinkedToBatchItemID;
    }

    public String getQCSampleType() {
        return this.__QCSampleType;
    }

    public void setQCSampleType(String __QCSampleType) {
        this.__QCSampleType = __QCSampleType;
    }

    public String getStandardLevel() {
        return this.__StandardLevel;
    }

    public void setStandardLevel(String __StandardLevel) {
        this.__StandardLevel = __StandardLevel;
    }

    public String getActionApply() {
        return this.__ActionApply;
    }

    public void setActionApply(String __ActionApply) {
        this.__ActionApply = __ActionApply;
    }

    public String getActionCalc() {
        return this.__ActionCalc;
    }

    public void setActionCalc(String __ActionCalc) {
        this.__ActionCalc = __ActionCalc;
    }

    public String getActionEval() {
        return this.__ActionEval;
    }

    public void setActionEval(String __ActionEval) {
        this.__ActionEval = __ActionEval;
    }

    public String getAQCStatus() {
        return this.__AQCStatus;
    }

    public void setAQCStatus(String __AQCStatus) {
        this.__AQCStatus = __AQCStatus;
    }

    public String getQCBatchItemID() {
        return this.__QCBatchItemID;
    }

    public SDIDataSet getSDIDataSet() {
        if (this.__SDIDataSet == null) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT SDCID, KEYID1, KEYID2, KEYID3, PARAMLISTID, PARAMLISTVERSIONID, VARIANTID, DATASET");
            sql.append(" FROM SDIDATA ");
            sql.append(" WHERE S_QCBATCHITEMID = " + safeSQL.addVar(this.__QCBatchItemID));
            sql.append(" AND S_QCBATCHID = " + safeSQL.addVar(this.__QCBatchID));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                SDIDataSet dataset;
                SDI sdi = new SDI(ds.getValue(0, "SDCID"), ds.getValue(0, "KEYID1"), ds.getValue(0, "KEYID2"), ds.getValue(0, "KEYID3"));
                this.__SDIDataSet = dataset = new SDIDataSet(sdi, ds.getValue(0, "PARAMLISTID"), ds.getValue(0, "PARAMLISTVERSIONID"), ds.getValue(0, "VARIANTID"), ds.getValue(0, "DATASET"), this.__QueryProcessor);
            }
        }
        return this.__SDIDataSet;
    }

    public SDIDataSet getSDIDataSet(String paramlistid, String paramlistversionid, String variantid, String dataset) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT SDCID, KEYID1, KEYID2, KEYID3, PARAMLISTID, PARAMLISTVERSIONID, VARIANTID, DATASET");
        sql.append(" FROM SDIDATA ");
        sql.append(" WHERE S_QCBATCHITEMID = " + safeSQL.addVar(this.__QCBatchItemID));
        sql.append(" AND S_QCBATCHID = " + safeSQL.addVar(this.__QCBatchID));
        if (paramlistid != null && paramlistid.length() > 0) {
            sql.append(" AND PARAMLISTID = " + safeSQL.addVar(paramlistid));
        }
        if (paramlistversionid != null && paramlistversionid.length() > 0) {
            sql.append(" AND PARAMLISTVERSIONID = " + safeSQL.addVar(paramlistversionid));
        }
        if (variantid != null && variantid.length() > 0) {
            sql.append(" AND VARIANTID = " + safeSQL.addVar(variantid));
        }
        if (dataset != null && dataset.length() > 0) {
            sql.append(" AND DATASET = " + safeSQL.addVar(dataset));
        }
        sql.append(" ORDER BY DATASET ");
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            SDIDataSet sdidataset;
            this.__SDIDataSet = sdidataset = new SDIDataSet(ds.getValue(0, "SDCID"), ds.getValue(0, "KEYID1"), ds.getValue(0, "KEYID2"), ds.getValue(0, "KEYID3"), ds.getValue(0, "PARAMLISTID"), ds.getValue(0, "PARAMLISTVERSIONID"), ds.getValue(0, "VARIANTID"), ds.getValue(0, "DATASET"), this.__QueryProcessor);
        }
        return this.__SDIDataSet;
    }

    public void setSDIDataSet(SDIDataSet __SDIDataSet) {
        this.__SDIDataSet = __SDIDataSet;
    }

    public boolean isQCSample() {
        return this.__QCBatchSampleTypeID != null;
    }

    public int getItemPosition() {
        return this.__ItemPosition;
    }

    public void setItemPosition(int __ItemPosition) {
        this.__ItemPosition = __ItemPosition;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QCBatchItem)) {
            return false;
        }
        QCBatchItem qcBatchItem = (QCBatchItem)o;
        if (!this.__QCBatchID.equals(qcBatchItem.__QCBatchID)) {
            return false;
        }
        return this.__QCBatchItemID.equals(qcBatchItem.__QCBatchItemID);
    }

    public int hashCode() {
        int result = this.__QCBatchID.hashCode();
        result = 29 * result + this.__QCBatchItemID.hashCode();
        return result;
    }

    public String toString() {
        return "[QCBatchItem@" + this.hashCode() + ":" + this.__QCBatchID + "," + this.__QCBatchItemID + "," + this.__QCBatchSampleTypeID + "," + this.__QCSampleType + "]";
    }

    public void setQueryProcessor(QueryProcessor qp) {
        this.__QueryProcessor = qp;
    }

    public QueryProcessor getQueryProcessor() {
        return this.__QueryProcessor;
    }
}

