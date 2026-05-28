/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.actions.Sdi;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

class SDIStatus
extends BaseCustom {
    private String __Sdcid;
    private String __Keyid1;
    private String __Statuscolid;
    private HashMap __SdiMap = new HashMap();
    private HashMap __receivedDTMap = new HashMap();
    private DBAccess database = null;
    private ConnectionInfo connectionInfo = null;
    boolean bypassWSCompleteCheck = false;

    public SDIStatus(ConnectionInfo connectionInfo, DBAccess database, boolean bypassWSCompletionCheck) {
        this.setConnectionId(connectionInfo.getConnectionId());
        this.database = database;
        this.connectionInfo = connectionInfo;
        this.bypassWSCompleteCheck = bypassWSCompletionCheck;
    }

    public String getSdcid() {
        return this.__Sdcid;
    }

    public void setSdcid(String sdcid) {
        this.__Sdcid = sdcid;
    }

    public String getKeyid1() {
        return this.__Keyid1;
    }

    public void setKeyid1(String keyid1) {
        this.__Keyid1 = keyid1;
    }

    public String getStatuscolid() {
        return this.__Statuscolid;
    }

    public void setStatuscolid(String statuscolid) {
        this.__Statuscolid = statuscolid;
    }

    public String getReceivedDT(String keyid1) {
        return (String)this.__receivedDTMap.get(keyid1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void populate() throws SapphireException {
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList(this.__Sdcid);
        DataSet columnsDS = this.getSDCProcessor().getColumnData(this.__Sdcid);
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("columnid", "receiveddt");
        boolean isReceiveddtExist = false;
        if (columnsDS.findRow(hm) >= 0) {
            isReceiveddtExist = true;
        }
        String tableid = sdcProps.getProperty("tableid");
        String keycolid1 = sdcProps.getProperty("keycolid1");
        String keycolid2 = sdcProps.getProperty("keycolid2");
        String keycolid3 = sdcProps.getProperty("keycolid3");
        DAMProcessor dam = this.getDAMProcessor();
        String rset_id = "";
        try {
            rset_id = BaseSDIDataAction.createRSet(this.__Sdcid, this.__Keyid1, null, null, this.database, this.connectionInfo, false);
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select distinct s.").append(keycolid1).append(", s.").append(this.__Statuscolid).append(", ds.s_datasetstatus");
            if (isReceiveddtExist) {
                sql.append(", s.receiveddt");
            }
            sql.append(" from ").append(tableid).append(" s, sdidata ds, rsetitems r");
            sql.append(" where s.").append(keycolid1).append(" = r.keyid1 and rsetid = ").append(safeSQL.addVar(rset_id));
            sql.append(" and s.").append(keycolid1).append(" = ds.keyid1");
            if (keycolid2.length() > 0) {
                sql.append(" and s.").append(keycolid2).append(" = ds.keyid2");
            }
            if (keycolid3.length() > 0) {
                sql.append(" and s.").append(keycolid3).append(" = ds.keyid3");
            }
            sql.append(" and ds.sdcid = ").append(safeSQL.addVar(this.__Sdcid));
            sql.append(" and ( ds.sourceworkitemid is null or ds.sourceworkitemid = '' )");
            sql.append(" union ");
            sql.append("select distinct s.").append(keycolid1).append(", s.").append(this.__Statuscolid).append(", wi.workitemstatus");
            if (isReceiveddtExist) {
                sql.append(", s.receiveddt");
            }
            sql.append(" from ").append(tableid).append(" s, sdiworkitem wi, rsetitems r");
            sql.append(" where s.").append(keycolid1).append(" = r.keyid1 and rsetid = ").append(safeSQL.addVar(rset_id));
            sql.append(" and s.").append(keycolid1).append("= wi.keyid1");
            if (keycolid2.length() > 0) {
                sql.append(" and s.").append(keycolid2).append(" = wi.keyid2");
            }
            if (keycolid3.length() > 0) {
                sql.append(" and s.").append(keycolid3).append(" = wi.keyid3");
            }
            sql.append(" and wi.sdcid = ").append(safeSQL.addVar(this.__Sdcid));
            sql.append(" and ( wi.groupid is null or wi.groupid = '' or  wi.workitemtypeflag = 'P' ) ");
            sql.append(" and wi.APPLIEDFLAG = 'Y' ");
            sql.append(" order by ").append(keycolid1);
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    Sdi sdi;
                    String keycolid1value = ds.getValue(i, keycolid1);
                    String sdistatus = ds.getValue(i, this.__Statuscolid);
                    if (isReceiveddtExist && !this.__receivedDTMap.containsKey(keycolid1value)) {
                        this.__receivedDTMap.put(keycolid1value, ds.getValue(i, "receiveddt"));
                    }
                    if (this.__SdiMap.containsKey(keycolid1value)) {
                        sdi = (Sdi)this.__SdiMap.get(keycolid1value);
                    } else {
                        sdi = new Sdi(keycolid1value, sdistatus);
                        this.__SdiMap.put(keycolid1value, sdi);
                        if ("Sample".equals(this.__Sdcid) && !this.bypassWSCompleteCheck) {
                            StringBuffer wssql = new StringBuffer();
                            wssql.append("select count(*) COUNT from worksheet ws, worksheetsdi wsdi").append(" where ws.worksheetstatus <> 'Complete' and wsdi.worksheetid = ws.worksheetid and wsdi.worksheetversionid = ws.worksheetversionid ").append(" and (  ( ws.blockflag = 'Y' or ws.blockflag is null or ws.blockflag = '' ) and  ( ws.blocksdcid = 'Sample' or ws.blocksdcid is null or ws.blocksdcid = '' ) and wsdi.sdcid = 'Sample' and wsdi.keyid1 = ? )");
                            DataSet worksheetds = this.getQueryProcessor().getPreparedSqlDataSet(wssql.toString(), (Object[])new String[]{keycolid1value});
                            int count = worksheetds.getInt(0, "COUNT");
                            sdi.setIncompleteWorksheetCount(count);
                        }
                    }
                    String datasetStatus = ds.getValue(i, "s_datasetstatus");
                    if (datasetStatus == null || datasetStatus.length() <= 0) continue;
                    sdi.add(ds.getValue(i, "s_datasetstatus"));
                }
            }
        }
        finally {
            dam.clearRSet(rset_id);
        }
    }

    public List evaluate() throws SapphireException {
        this.populate();
        ArrayList<Sdi> list = new ArrayList<Sdi>();
        Set keySet = this.__SdiMap.keySet();
        for (Object aKeySet : keySet) {
            String key = (String)aKeySet;
            Sdi sdi = (Sdi)this.__SdiMap.get(key);
            if (!sdi.evaluateStatus()) continue;
            list.add(sdi);
        }
        return list;
    }
}

