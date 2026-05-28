/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.util.DataItem;
import com.labvantage.opal.util.SDIWorkItem;
import com.labvantage.sapphire.SDI;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;

public class SDIDataSet {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90286 $";
    public static final String PARAMTYPE_ALL = "All";
    public static final String PARAMTYPE_STANDARD = "Standard";
    public static final String STATUS_INITIAL = "Initial";
    public static final String STATUS_INPROGRESS = "InProgress";
    public static final String STATUS_ENTERED = "DataEntered";
    public static final String STATUS_RELEASED = "Released";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_REMEASURED = "Remeasured";
    public static final String STATUS_RETESTED = "Retested";
    public static final int QCBATCHID = 0;
    public static final int QCBATCHITEMID = 1;
    public static final int QCBATCHUSERSEQUENCE = 2;
    public static final int QCBATCHSAMPLETYPEID = 3;
    public static final int LINKTOQCBATCHITEMID = 4;
    private SDI __SDI;
    private String __ParamListID;
    private String __ParamListVersionID;
    private String __VariantID;
    private String __DataSet;
    private String __QCBatchID;
    private String __QCBatchItemID;
    private String __QCBatchUserSequence;
    private String __QCBatchSampleTypeID;
    private String __LinkedToBatchItemID;
    private QueryProcessor __QueryProcessor;
    private SQLGenerator __SqlGenerator;
    private boolean qcBatchFlag;
    private boolean _IsOra;

    public SDIDataSet(SDI sdi, String paramlistid, String paramlistversionid, String variantid, String dataset, SQLGenerator sqlgenerator) {
        this.__SDI = sdi;
        this.__ParamListID = paramlistid;
        this.__ParamListVersionID = paramlistversionid;
        this.__VariantID = variantid;
        this.__DataSet = dataset;
        this.__SqlGenerator = sqlgenerator;
    }

    public SDIDataSet(SDI sdi, String paramlistid, String paramlistversionid, String variantid, String dataset, QueryProcessor queryProcessor) {
        this.__SDI = sdi;
        this.__ParamListID = paramlistid;
        this.__ParamListVersionID = paramlistversionid;
        this.__VariantID = variantid;
        this.__DataSet = dataset;
        this.__QueryProcessor = queryProcessor;
        ConnectionProcessor cp = new ConnectionProcessor(this.__QueryProcessor.getConnectionid());
        this.__SqlGenerator = SQLFactory.getSqlGenerator(cp.isOra());
        this._IsOra = cp.isOra();
    }

    public SDIDataSet(String sdcid, String keyid1, String keyid2, String keyid3, String __ParamListID, String __ParamListVersionID, String __VariantID, String __DataSet, QueryProcessor __QueryProcessor) {
        this.__SDI = new SDI(sdcid, keyid1, keyid2, keyid3);
        this.__ParamListID = __ParamListID;
        this.__ParamListVersionID = __ParamListVersionID;
        this.__VariantID = __VariantID;
        this.__DataSet = __DataSet;
        this.__QueryProcessor = __QueryProcessor;
        ConnectionProcessor cp = new ConnectionProcessor(this.__QueryProcessor.getConnectionid());
        this._IsOra = cp.isOra();
    }

    public List getDataItems(QueryProcessor queryProcessor, String parameterType) {
        DataSet ds;
        ArrayList<DataItem> dataItemList = new ArrayList<DataItem>();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT PARAMID, PARAMTYPE, REPLICATEID, ISNULL( ENTEREDVALUE, '' ), MANDATORYFLAG, RELEASEDFLAG");
        sql.append(" FROM SDIDATA");
        sql.append(" WHERE SDCID = ").append(safeSQL.addVar(this.__SDI.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(this.__SDI.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(this.__SDI.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(this.__SDI.getKeyid3()));
        sql.append(" AND PARAMLISTID = ").append(safeSQL.addVar(this.__ParamListID));
        sql.append(" AND PARAMLISTVERSIONID = ").append(safeSQL.addVar(this.__ParamListVersionID));
        sql.append(" AND VARIANTID = ").append(safeSQL.addVar(this.__VariantID));
        sql.append(" AND DATASET = ").append(safeSQL.addVar(this.__DataSet));
        if (parameterType != null && !parameterType.equals(PARAMTYPE_ALL)) {
            sql.append(" AND PARAMTYPE = ").append(safeSQL.addVar(parameterType));
        }
        if ((ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String released;
                DataItem dataItem = new DataItem();
                dataItem.setSDIDataSet(this);
                dataItem.setParamID(ds.getValue(i, "PARAMID"));
                dataItem.setParamType(ds.getValue(i, "PARAMTYPE"));
                dataItem.setReplicate(ds.getValue(i, "REPLICATEID"));
                String mandatory = ds.getValue(i, "MANDATORYFLAG");
                if (mandatory != null && mandatory.equals("Y")) {
                    dataItem.setMandatory(true);
                }
                if ((released = ds.getValue(i, "RELEASEDFLAG")) != null && released.equals("Y")) {
                    dataItem.setReleased(true);
                }
                dataItemList.add(dataItem);
            }
        }
        return dataItemList;
    }

    public SDIWorkItem getParentWorkItem() {
        HashMap<String, SDIWorkItem> itemmap = new HashMap<String, SDIWorkItem>();
        SafeSQL safeSQL = this.__SqlGenerator.getParamlistWorkitems(this.__SDI, this.__ParamListID, this.__ParamListVersionID, this.__VariantID);
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        if (ds.size() > 0) {
            String instance;
            for (int i = 0; i < ds.size(); ++i) {
                if (!ds.getValue(i, "WORKITEMVERSIONID", "").isEmpty()) {
                    itemmap.put(ds.getValue(i, "ITEMINSTANCE"), new SDIWorkItem(this.__SDI, ds.getValue(i, "WORKITEMID"), ds.getValue(i, "WORKITEMVERSIONID"), ds.getValue(i, "WORKITEMINSTANCE"), this._IsOra));
                    continue;
                }
                itemmap.put(ds.getValue(i, "ITEMINSTANCE"), new SDIWorkItem(this.__SDI, ds.getValue(i, "WORKITEMID"), ds.getValue(i, "WORKITEMINSTANCE"), this._IsOra));
            }
            if (itemmap.containsKey(this.__DataSet)) {
                return (SDIWorkItem)itemmap.get(this.__DataSet);
            }
            int remeasureinstance = this.getRemeasureInstance();
            if (remeasureinstance != -1 && itemmap.containsKey(instance = Integer.toString(remeasureinstance))) {
                return (SDIWorkItem)itemmap.get(instance);
            }
        }
        return null;
    }

    public int getRemeasureInstance() {
        SafeSQL safeSQL = this.__SqlGenerator.getRemeasureInstance(this.__SDI, this.__ParamListID, this.__ParamListVersionID, this.__VariantID, this.__DataSet);
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        if (ds.size() > 0) {
            String instance = ds.getValue(0, "S_REMEASUREINSTANCE");
            return Integer.parseInt(instance);
        }
        return -1;
    }

    public String getSDIDataId() {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT sdidataid FROM sdidata ");
        sql.append(" WHERE SDCID = ").append(safeSQL.addVar(this.__SDI.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(this.__SDI.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(this.__SDI.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(this.__SDI.getKeyid3()));
        sql.append(" AND PARAMLISTID = ").append(safeSQL.addVar(this.__ParamListID));
        sql.append(" AND PARAMLISTVERSIONID = ").append(safeSQL.addVar(this.__ParamListVersionID));
        sql.append(" AND VARIANTID = ").append(safeSQL.addVar(this.__VariantID));
        sql.append(" AND DATASET = ").append(safeSQL.addVar(this.__DataSet));
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds.size() > 0) {
            String sdidataid = ds.getValue(0, "sdidataid");
            return sdidataid;
        }
        return "";
    }

    public int getMaxInstance() {
        int datasetnumber = -1;
        try {
            SafeSQL safeSQL = this.__SqlGenerator.getMaxSdiDataset(this.__SDI, this.__ParamListID, this.__ParamListVersionID, this.__VariantID);
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
            if (ds.size() > 0) {
                datasetnumber = ds.getInt(0, "DATASET");
            }
        }
        catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
        return datasetnumber;
    }

    public List getDataItems(String parameterType) {
        ArrayList<DataItem> dataItemList = new ArrayList<DataItem>();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT PARAMID, PARAMTYPE, REPLICATEID, MANDATORYFLAG, RELEASEDFLAG");
        sql.append(", ENTEREDVALUE , ENTEREDTEXT, TRANSFORMVALUE, DISPLAYFORMAT, USERSEQUENCE ");
        sql.append(" FROM SDIDATAITEM");
        sql.append(" WHERE SDCID = ").append(safeSQL.addVar(this.__SDI.getSdcid()));
        sql.append(" AND KEYID1 = ").append(safeSQL.addVar(this.__SDI.getKeyid1()));
        sql.append(" AND KEYID2 = ").append(safeSQL.addVar(this.__SDI.getKeyid2()));
        sql.append(" AND KEYID3 = ").append(safeSQL.addVar(this.__SDI.getKeyid3()));
        sql.append(" AND PARAMLISTID = ").append(safeSQL.addVar(this.__ParamListID));
        sql.append(" AND PARAMLISTVERSIONID = ").append(safeSQL.addVar(this.__ParamListVersionID));
        sql.append(" AND VARIANTID = ").append(safeSQL.addVar(this.__VariantID));
        sql.append(" AND DATASET = ").append(safeSQL.addVar(this.__DataSet));
        if (parameterType != null && !parameterType.equals(PARAMTYPE_ALL)) {
            sql.append(" AND PARAMTYPE = ").append(safeSQL.addVar(parameterType));
        }
        sql.append(" ORDER BY PARAMTYPE, PARAMID, REPLICATEID ");
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String released;
                DataItem dataItem = new DataItem();
                dataItem.setSDIDataSet(this);
                dataItem.setParamID(ds.getValue(i, "PARAMID"));
                dataItem.setParamType(ds.getValue(i, "PARAMTYPE"));
                dataItem.setReplicate(ds.getValue(i, "REPLICATEID"));
                dataItem.setEnteredValue(ds.getValue(i, "ENTEREDVALUE"));
                dataItem.setEnteredText(ds.getValue(i, "ENTEREDTEXT"));
                BigDecimal bigDecimalTrValue = ds.getBigDecimal(i, "TRANSFORMVALUE");
                if (bigDecimalTrValue != null) {
                    dataItem.setTransformValue(bigDecimalTrValue.toString());
                }
                dataItem.setDisplayFormat(ds.getValue(i, "DISPLAYFORMAT"));
                dataItem.setUserSequence(ds.getValue(i, "USERSEQUENCE"));
                String mandatory = ds.getValue(i, "MANDATORYFLAG");
                if (mandatory != null && mandatory.equals("Y")) {
                    dataItem.setMandatory(true);
                }
                if ((released = ds.getValue(i, "RELEASEDFLAG")) != null && released.equals("Y")) {
                    dataItem.setReleased(true);
                }
                dataItemList.add(dataItem);
            }
        }
        return dataItemList;
    }

    public SDI getSDI() {
        return this.__SDI;
    }

    public String getParamListID() {
        return this.__ParamListID;
    }

    public void setParamListID(String __ParamListID) {
        this.__ParamListID = __ParamListID;
    }

    public String getParamListVersionID() {
        return this.__ParamListVersionID;
    }

    public void setParamVersionListID(String __ParamListVersionID) {
        this.__ParamListVersionID = __ParamListVersionID;
    }

    public String getVariantID() {
        return this.__VariantID;
    }

    public void setVariantID(String __VariantID) {
        this.__VariantID = __VariantID;
    }

    public String getDataSet() {
        return this.__DataSet;
    }

    public void setDataSet(String __DataSet) {
        this.__DataSet = __DataSet;
    }

    public String getQCBatchProps(int prop) {
        String returnValue = null;
        if (!this.qcBatchFlag) {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT T2.S_QCBATCHID, T2.S_QCBATCHITEMID, T2.USERSEQUENCE,");
            sql.append(" T2.QCBATCHSAMPLETYPEID, T2.LINKTOQCBATCHITEMID");
            sql.append(" FROM SDIDATA T1, S_QCBATCHITEM T2");
            sql.append(" WHERE T2.S_QCBATCHITEMID = T1.S_QCBATCHITEMID AND T2.S_QCBATCHID = T1.S_QCBATCHID ");
            sql.append(" AND T1.SDCID = ?");
            sql.append(" AND T1.KEYID1 = ?");
            sql.append(" AND T1.KEYID2 = ?");
            sql.append(" AND T1.KEYID3 = ?");
            sql.append(" AND T1.PARAMLISTID = ?");
            sql.append(" AND T1.PARAMLISTVERSIONID = ?");
            sql.append(" AND T1.VARIANTID = ?");
            sql.append(" AND T1.DATASET = ?");
            Object[] p = new Object[]{this.__SDI.getSdcid(), this.__SDI.getKeyid1(), this.__SDI.getKeyid2(), this.__SDI.getKeyid3(), this.__ParamListID, this.__ParamListVersionID, this.__VariantID, this.__DataSet};
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), p);
            if (ds != null && ds.size() > 0) {
                this.__QCBatchID = ds.getValue(0, "S_QCBATCHID");
                this.__QCBatchItemID = ds.getValue(0, "S_QCBATCHITEMID");
                this.__QCBatchUserSequence = ds.getValue(0, "USERSEQUENCE");
                this.__QCBatchSampleTypeID = ds.getValue(0, "QCBATCHSAMPLETYPEID");
                this.__LinkedToBatchItemID = ds.getValue(0, "LINKTOQCBATCHITEMID");
            }
            this.qcBatchFlag = true;
        }
        switch (prop) {
            case 0: {
                returnValue = this.__QCBatchID;
                break;
            }
            case 1: {
                returnValue = this.__QCBatchItemID;
                break;
            }
            case 2: {
                returnValue = this.__QCBatchUserSequence;
                break;
            }
            case 3: {
                returnValue = this.__QCBatchSampleTypeID;
                break;
            }
            case 4: {
                returnValue = this.__LinkedToBatchItemID;
            }
        }
        return returnValue;
    }

    public boolean isQCDataSet() {
        String id = this.getQCBatchProps(3);
        return id != null && id.length() > 0;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ " + this.__SDI);
        sb.append(", ParamList ID: " + this.__ParamListID);
        sb.append(", ParamList Version: " + this.__ParamListVersionID);
        sb.append(", Variant: " + this.__VariantID);
        sb.append(", Dataset: " + this.__DataSet);
        sb.append(", QCBatch ID: " + this.__QCBatchID + " ]");
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SDIDataSet)) {
            return false;
        }
        SDIDataSet sdiDataSet = (SDIDataSet)o;
        if (!this.__DataSet.equals(sdiDataSet.__DataSet)) {
            return false;
        }
        if (!this.__ParamListID.equals(sdiDataSet.__ParamListID)) {
            return false;
        }
        if (!this.__ParamListVersionID.equals(sdiDataSet.__ParamListVersionID)) {
            return false;
        }
        if (!this.__SDI.equals(sdiDataSet.__SDI)) {
            return false;
        }
        return this.__VariantID.equals(sdiDataSet.__VariantID);
    }

    public int hashCode() {
        int result = this.__SDI.hashCode();
        result = 29 * result + this.__ParamListID.hashCode();
        result = 29 * result + this.__ParamListVersionID.hashCode();
        result = 29 * result + this.__VariantID.hashCode();
        result = 29 * result + this.__DataSet.hashCode();
        return result;
    }

    public boolean isDataSetRetestable(SDI sdi, String paramlistid, String paramlistversionid, String variantid, String dataset, DBAccess database) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT 1 FROM  WORKITEMITEM wii, SDIDATA ds, SDIWORKITEM sdiwi, SDIWORKITEMITEM sdiwii").append(" WHERE ds.SDCID = ? AND ds.KEYID1 = ? AND ds.KEYID2 = ? AND ds.KEYID3 = ?").append(" AND ds.PARAMLISTID = ? AND ds.PARAMLISTVERSIONID = ? AND ds.VARIANTID = ? AND ds.DATASET = ?").append(" AND sdiwii.sdcid = ds.sdcid AND sdiwii.keyid1= ds.keyid1 AND sdiwii.keyid2 = ds.keyid2 AND sdiwii.keyid3 = ds.keyid3 AND sdiwii.WORKITEMID = ds.SOURCEWORKITEMID AND sdiwii.WORKITEMINSTANCE = ds.SOURCEWORKITEMINSTANCE").append("  AND sdiwii.itemsdcid = 'ParamList' AND sdiwii.itemkeyid1 = ds.paramlistid AND sdiwii.itemkeyid2 = ds.paramlistversionid AND sdiwii.itemkeyid3 = ds.variantid AND sdiwii.iteminstance = ds.dataset").append(" AND sdiwi.sdcid = sdiwii.sdcid AND sdiwi.keyid1 = sdiwii.keyid1 AND sdiwi.keyid2 = sdiwii.keyid2 AND sdiwi.keyid3 = sdiwii.keyid3 AND sdiwi.WORKITEMID = sdiwii.WORKITEMID AND sdiwi.WORKITEMINSTANCE = sdiwii.WORKITEMINSTANCE").append(" AND wii.WORKITEMID = sdiwi.WORKITEMID AND wii.WORKITEMVERSIONID = sdiwi.WORKITEMVERSIONID").append(" AND wii.SDCID = 'ParamList' AND wii.KEYID1 = ? AND ( wii.KEYID2 = ? OR wii.KEYID2 = coalesce(wii.KEYID2,'C') ) AND wii.KEYID3 = ? ").append(" AND wii.workitemitemid = ").append(database.isOracle() ? "substr( sdiwii.workitemitemid, 1, instr( sdiwii.workitemitemid, '.', 1 ) - 1 )" : "substring( sdiwii.workitemitemid, 1, charindex( '.', sdiwii.workitemitemid, 1 ) - 1 )").append(" AND wii.FORCENEWFLAG != 'X'");
        Object[] p = new Object[]{sdi.getSdcid(), sdi.getKeyid1(), sdi.getKeyid2(), sdi.getKeyid3(), paramlistid, paramlistversionid, variantid, dataset, paramlistid, paramlistversionid, variantid};
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), p);
        return ds.getRowCount() > 0;
    }
}

