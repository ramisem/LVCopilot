/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.pagetype.storage;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class EnvConditionHandler
extends PropertyHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 65607 $";

    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String[] cols;
        String environmentid = (String)props.get("forward_keyid1");
        String sdcid = "StorageEnvSDC";
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT * FROM STORAGEENVCOND ");
        sql.append("WHERE STORAGEENVID = ").append(safeSQL.addVar(environmentid));
        DBUtil dbutil = new DBUtil();
        dbutil.setConnection(this.sapphireConnection);
        dbutil.createPreparedResultSet("storageenvcond", sql.toString(), safeSQL.getValues());
        DataSet dsStorageEnvConds = new DataSet(dbutil.getResultSet("storageenvcond"));
        ArrayList<String> dataList = new ArrayList<String>();
        ArrayList<String> idList = new ArrayList<String>();
        for (String param : props.keySet()) {
            if (!param.startsWith("storagecond")) continue;
            if (param.endsWith("selector")) {
                idList.add(param.substring(param.indexOf(95) + 1, param.lastIndexOf(95)));
                continue;
            }
            dataList.add(param.substring(param.indexOf(95) + 1));
        }
        SequenceProcessor sequenceProcessor = new SequenceProcessor(this.getConnectionInfo().getConnectionId());
        ActionProcessor ap = new ActionProcessor(this.getConnectionInfo().getConnectionId());
        int year = Calendar.getInstance().get(1);
        DataSet saveds = new DataSet(this.sapphireConnection);
        DataSet insertds = new DataSet(this.sapphireConnection);
        for (int i = 0; i < idList.size(); ++i) {
            String id = (String)idList.get(i);
            String storageCondId = (String)props.get("__storagecond_" + id + "_id");
            String rowStatus = (String)props.get("rowstaus_" + id);
            if (storageCondId != null && storageCondId.length() > 0) {
                HashMap<String, String> findMap = new HashMap<String, String>();
                findMap.put("storageenvid", environmentid);
                findMap.put("storagecondid", storageCondId);
                int findRow = dsStorageEnvConds.findRow(findMap);
                if (findRow <= -1) continue;
                if ("U".equalsIgnoreCase(rowStatus)) {
                    int r = saveds.addRow();
                    saveds.setString(r, "storagecondid", storageCondId);
                    saveds.setString(r, "storagecondtypeid", (String)props.get(id));
                    this.createDataSet(id, saveds, dataList, props, r);
                }
                dsStorageEnvConds.remove(findRow);
                continue;
            }
            storageCondId = year + "_" + sequenceProcessor.getSequence("STORAGEENVCOND", Integer.toString(year));
            int r = insertds.addRow();
            insertds.setString(r, "storagecondid", storageCondId);
            insertds.setString(r, "storagecondtypeid", (String)props.get(id));
            this.createDataSet(id, insertds, dataList, props, r);
        }
        if (dsStorageEnvConds.getRowCount() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("keyid1", environmentid);
            actionProps.setProperty("linkid", "StorageEnvConditions");
            actionProps.setProperty("storagecondid", dsStorageEnvConds.getColumnValues("storagecondid", ";"));
            ap.processAction("DeleteSDIDetail", "1", actionProps);
        }
        if (saveds.getRowCount() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("keyid1", environmentid);
            actionProps.setProperty("linkid", "StorageEnvConditions");
            cols = saveds.getColumns();
            for (int c = 0; c < cols.length; ++c) {
                actionProps.setProperty(cols[c], saveds.getColumnValues(cols[c], ";"));
            }
            ap.processAction("EditSDIDetail", "1", actionProps);
        }
        if (insertds.getRowCount() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", sdcid);
            actionProps.setProperty("keyid1", environmentid);
            actionProps.setProperty("linkid", "StorageEnvConditions");
            cols = insertds.getColumns();
            for (int c = 0; c < cols.length; ++c) {
                actionProps.setProperty(cols[c], insertds.getColumnValues(cols[c], ";"));
            }
            ap.processAction("AddSDIDetail", "1", actionProps);
        }
    }

    private void createDataSet(String id, DataSet ds, List dataList, HashMap props, int row) {
        for (int j = 0; j < dataList.size(); ++j) {
            String s = (String)dataList.get(j);
            if (!s.startsWith(id)) continue;
            String column = s.substring(s.indexOf(95) + 1);
            String value = (String)props.get("storagecond_" + s);
            ds.setString(row, column, value);
        }
    }
}

