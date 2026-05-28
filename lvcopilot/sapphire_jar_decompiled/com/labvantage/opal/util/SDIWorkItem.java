/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.sapphire.SDI;
import java.util.ArrayList;
import java.util.List;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class SDIWorkItem {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53832 $";
    private SDI __SDI;
    private String __WorkitemID;
    private String __WorkitemInstance;
    private String __WorkitemVersionID;
    private boolean isOra;

    public SDIWorkItem(SDI sdi, String workitemid, String workiteminstance, boolean isOra) {
        this.__SDI = sdi;
        this.__WorkitemID = workitemid;
        this.__WorkitemInstance = workiteminstance;
        this.isOra = isOra;
    }

    public SDIWorkItem(SDI sdi, String workitemid, String workitemversionid, String workiteminstance, boolean isOra) {
        this.__SDI = sdi;
        this.__WorkitemID = workitemid;
        this.__WorkitemVersionID = workitemversionid;
        this.__WorkitemInstance = workiteminstance;
        this.isOra = isOra;
    }

    public List getItems(QueryProcessor qp, String itemsdcid) {
        ArrayList<String> list = new ArrayList<String>();
        String sql = null;
        sql = this.isOra ? "SELECT NVL( ITEMKEYID1, '(null)' ) ITEMKEYID1, NVL( ITEMKEYID2, '(null)' ) ITEMKEYID2, NVL( ITEMKEYID3, '(null)' ) ITEMKEYID3, NVL( ITEMINSTANCE, '' ) ITEMINSTANCE" : "SELECT ISNULL( ITEMKEYID1, '(null)' ) ITEMKEYID1, ISNULL( ITEMKEYID2, '(null)' ) ITEMKEYID2, ISNULL( ITEMKEYID3, '(null)' ) ITEMKEYID3, ISNULL( ITEMINSTANCE, '' ) ITEMINSTANCE";
        sql = sql + " FROM SDIWORKITEMITEM WHERE WORKITEMID = ? AND WORKITEMINSTANCE = ? AND SDCID = ? AND KEYID1 = ? AND KEYID2 = ? AND KEYID3 = ? AND ITEMSDCID = ? ORDER BY ITEMKEYID1, ITEMKEYID2, ITEMKEYID3";
        Object[] p = new Object[]{this.getWorkitemID(), this.getWorkitemInstance(), this.__SDI.getSdcid(), this.__SDI.getKeyid1(), this.__SDI.getKeyid2(), this.__SDI.getKeyid3(), itemsdcid};
        DataSet ds = qp.getPreparedSqlDataSet(sql, p);
        for (int i = 0; i < ds.size(); ++i) {
            StringBuffer sb = new StringBuffer();
            sb.append(ds.getValue(i, "ITEMKEYID1")).append(";");
            sb.append(ds.getValue(i, "ITEMKEYID2")).append(";");
            sb.append(ds.getValue(i, "ITEMKEYID3")).append(";");
            sb.append(ds.getValue(i, "ITEMINSTANCE"));
            list.add(sb.toString());
        }
        return list;
    }

    public boolean isAnyDataEntered(QueryProcessor qp) {
        Object[] p;
        String sql = "SELECT T1.PARAMID FROM SDIDATAITEM T1, SDIWORKITEMITEM T2 WHERE T1.SDCID = T2.SDCID AND T1.KEYID1 = T2.KEYID1 AND T1.KEYID2 = T2.KEYID2 AND T1.KEYID3 = T2.KEYID3 AND T1.PARAMLISTID = T2.ITEMKEYID1 AND T1.PARAMLISTVERSIONID = T2.ITEMKEYID2 AND T1.VARIANTID = T2.ITEMKEYID3 AND T1.DATASET = T2.ITEMINSTANCE AND T2.SDCID = ? AND T2.KEYID1 = ? AND T2.KEYID2 = ? AND T2.KEYID3 = ? AND T2.ITEMSDCID = 'ParamList' AND T2.WORKITEMID = ? AND T2.WORKITEMINSTANCE = ? AND ENTEREDTEXT IS NOT NULL AND ENTEREDTEXT != '(null)' AND ENTEREDTEXT != ''";
        sql = this.isOra ? sql + " AND LENGTH( T1.ENTEREDTEXT ) > 0" : sql + " AND LEN( T1.ENTEREDTEXT ) > 0";
        DataSet ds = qp.getPreparedSqlDataSet(sql, p = new Object[]{this.__SDI.getSdcid(), this.__SDI.getKeyid1(), this.__SDI.getKeyid2(), this.__SDI.getKeyid3(), this.getWorkitemID(), this.getWorkitemInstance()});
        return ds.size() > 0;
    }

    public SDI getSDI() {
        return this.__SDI;
    }

    public String getWorkitemID() {
        return this.__WorkitemID;
    }

    public void setWorkitemID(String __WorkitemID) {
        this.__WorkitemID = __WorkitemID;
    }

    public String getWorkitemInstance() {
        return this.__WorkitemInstance;
    }

    public void setWorkitemInstance(String __WorkitemInstance) {
        this.__WorkitemInstance = __WorkitemInstance;
    }

    public String getWorkitemVersionID() {
        return this.__WorkitemVersionID;
    }

    public void setWorkitemVersionID(String __WorkitemVersionID) {
        this.__WorkitemVersionID = __WorkitemVersionID;
    }

    public String toString() {
        return "SDC ID: " + this.__SDI.getSdcid() + "\nKey 1: " + this.__SDI.getKeyid1() + "\nKey 2: " + this.__SDI.getKeyid2() + "\nKey 3: " + this.__SDI.getKeyid3() + "\nWorkitem: " + this.__WorkitemID + "\nInstance: " + this.__WorkitemInstance;
    }
}

