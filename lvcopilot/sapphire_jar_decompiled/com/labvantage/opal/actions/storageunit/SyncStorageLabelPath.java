/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.storageunit;

import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SyncStorageLabelPath
extends BaseAction
implements sapphire.action.SyncStorageLabelPath {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList props) throws SapphireException {
        DataSet ds;
        String storageunitid = props.getProperty("storageunitid");
        boolean syncall = "Y".equals(props.getProperty("syncall", "N"));
        if (syncall && (ds = this.getQueryProcessor().getSqlDataSet("select storageunitid from storageunit where parentid is null or parentid = ''")) != null && ds.size() > 0) {
            storageunitid = ds.getColumnValues("storageunitid", ";");
        }
        if (StringUtil.getLen(storageunitid) > 0L) {
            ds = null;
            StringBuilder sql = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select storageunitid, storageunitlabel, labelpath, parentid,");
            sql.append(" ( select su.labelpath from storageunit su where su.storageunitid = storageunit.parentid) parentlabelpath");
            sql.append(" from storageunit");
            if (StringUtil.getLen(storageunitid) > 2000L) {
                String rsetid = this.getDAMProcessor().createRSet("StorageUnitSDC", storageunitid, null, null);
                try {
                    sql.append(" where storageunitid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                }
                catch (Exception e) {
                    this.logger.error("Exception occurred", e);
                }
                finally {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            } else {
                sql.append(" where storageunitid in ( ").append(safeSQL.addIn(storageunitid, ";")).append(" )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null && ds.size() > 0) {
                DataSet storageds = new DataSet();
                storageds.addColumn("sdcid", 0);
                storageds.addColumn("keyid1", 0);
                storageds.addColumn("labelpath", 0);
                for (int i = 0; i < ds.size(); ++i) {
                    String id = ds.getValue(i, "storageunitid");
                    String newlabelpath = ds.getValue(i, "parentlabelpath", "") + "/" + ds.getValue(i, "storageunitlabel");
                    if (!newlabelpath.equals(ds.getValue(i, "labelpath"))) {
                        int row = storageds.addRow();
                        storageds.setValue(row, "keyid1", id);
                        storageds.setValue(row, "labelpath", newlabelpath);
                    }
                    this.syncStorageUnitLabelPath(id, newlabelpath, storageds);
                }
                if (storageds.size() > 0) {
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("sdcid", "StorageUnitSDC");
                    actionProps.setProperty("keyid1", storageds.getColumnValues("keyid1", ";"));
                    actionProps.setProperty("labelpath", storageds.getColumnValues("labelpath", ";"));
                    actionProps.setProperty("auditreason", props.getProperty("auditreason", "Synchronization of Labelpaths"));
                    actionProps.setProperty("__syncoperation", "Y");
                    actionProps.setProperty("auditreason", props.getProperty("auditreason", ""));
                    actionProps.setProperty("auditactivity", props.getProperty("auditactivity", ""));
                    actionProps.setProperty("auditsignedflag", props.getProperty("auditsignedflag", ""));
                    this.getActionProcessor().processAction("EditSDI", "1", actionProps);
                    props.setProperty("updatedstorageunitid", storageds.getColumnValues("keyid1", ";"));
                }
            }
        }
    }

    private void syncStorageUnitLabelPath(String storageUnitId, String labelpath, DataSet storageds) throws ActionException {
        int i;
        ArrayList<String> childidlist = new ArrayList<String>();
        ArrayList<String> childpathlist = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        sql.append("select storageunitid, storageunitlabel, labelpath,");
        sql.append(" ( select count(su.storageunitid) count from storageunit su where su.parentid = storageunit.storageunitid ) childcount");
        sql.append(" from storageunit");
        sql.append(" where parentid = ?");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{storageUnitId});
        if (ds != null) {
            for (i = 0; i < ds.size(); ++i) {
                String id = ds.getValue(i, "storageunitid");
                String newlabelpath = labelpath + "/" + ds.getValue(i, "storageunitlabel");
                if (!newlabelpath.equals(ds.getValue(i, "labelpath"))) {
                    int row = storageds.addRow();
                    storageds.setValue(row, "keyid1", id);
                    storageds.setValue(row, "labelpath", newlabelpath);
                }
                if (ds.getInt(i, "childcount") <= 0) continue;
                childidlist.add(id);
                childpathlist.add(newlabelpath);
            }
        }
        for (i = 0; i < childidlist.size(); ++i) {
            this.syncStorageUnitLabelPath((String)childidlist.get(i), (String)childpathlist.get(i), storageds);
        }
    }
}

