/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.storageunit;

import com.labvantage.opal.actions.storageunit.SyncStorageLabelPath;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class StorageUnitOperations
extends BaseAction {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 86098 $";
    private SQLGenerator __SqlGenerator;
    private ArrayList __RemoveColumnList = new ArrayList();

    public StorageUnitOperations() {
        this.__RemoveColumnList.add("createdt");
        this.__RemoveColumnList.add("createby");
        this.__RemoveColumnList.add("createtool");
        this.__RemoveColumnList.add("modby");
        this.__RemoveColumnList.add("moddt");
        this.__RemoveColumnList.add("modtool");
    }

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        int rc = 1;
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.getConnectionProcessor().isOra());
        if (actionid.equals("CopyStorageUnit")) {
            rc = this.copyStorageUnit(props);
        } else if (actionid.equals("SyncStorageAncestor")) {
            rc = this.doSyncStorageUnit(props);
        }
        return rc;
    }

    private int copyStorageUnit(HashMap props) {
        int rc = 1;
        int copies = 1;
        Logger.logInfo("CopyStorageUnit action is deprecated. Use of this action is not recommended.");
        String sourceStorageUnitid = (String)props.get("storageunitid");
        String auditreason = (String)props.get("auditreason");
        String auditactivity = (String)props.get("auditactivity");
        String auditsignedflag = (String)props.get("auditsignedflag");
        if (sourceStorageUnitid != null && sourceStorageUnitid.trim().length() > 0) {
            SafeSQL safeSQL = this.__SqlGenerator.getStorageUnitHierarchySql(sourceStorageUnitid);
            String sqlStmt = safeSQL.getPreparedSQL();
            if (sqlStmt == null || sqlStmt.trim().length() == 0) {
                StringBuffer msg = new StringBuffer();
                msg.append("Create Storage unit SQL is not implemented ");
                msg.append("for this database.");
                this.logger.error(msg.toString());
                return 2;
            }
            try {
                int count;
                DataSet sourceDS;
                int rowCount;
                String strCopies = (String)props.get("copies");
                if (strCopies != null && strCopies.trim().length() > 0) {
                    copies = Integer.parseInt(strCopies);
                }
                if ((rowCount = (sourceDS = this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues())).getRowCount()) == 0) {
                    new Exception("Storageunit " + sourceStorageUnitid + " not found.");
                }
                this.removeUnwantedColumns(sourceDS);
                if (sourceDS.isValidColumn("linksdcid") && sourceDS.isValidColumn("linkkeyid1")) {
                    for (count = 0; count < sourceDS.getRowCount(); ++count) {
                        sourceDS.setValue(count, "linksdcid", "");
                        sourceDS.setValue(count, "linkkeyid1", "");
                    }
                }
                for (count = 0; count < copies; ++count) {
                    String newStorageUnitsIds = this.createStorageUnits(rowCount, auditreason, auditactivity, auditsignedflag);
                    this.setNewStorageUnitProperties(sourceDS, newStorageUnitsIds);
                }
                rc = 1;
            }
            catch (Exception ex) {
                this.logger.error(ex.getMessage(), ex);
                return 2;
            }
        }
        return rc;
    }

    private String createStorageUnits(int count, String auditreason, String auditactivity, String auditsignedflag) throws ActionException {
        String countStr = count + "";
        HashMap<String, String> props = new HashMap<String, String>();
        ActionBlock actionBlock = new ActionBlock();
        ActionProcessor actionProcessor = this.getActionProcessor();
        props.put("sdcid", "StorageUnitSDC");
        props.put("copies", countStr);
        if (auditreason != null && auditreason.trim().length() > 0) {
            props.put("auditreason", auditreason);
        }
        props.put("auditactivity", auditactivity == null ? "" : auditactivity);
        props.put("auditsignedflag", auditsignedflag == null ? "N" : auditsignedflag);
        actionBlock.setAction("createStorageUnit", "AddSDI", "1", props);
        actionProcessor.processActionBlock(actionBlock);
        String storageUnitIds = actionBlock.getActionProperty(0, "newkeyid1");
        return storageUnitIds;
    }

    private void removeUnwantedColumns(DataSet ds) {
        for (int count = 0; count < this.__RemoveColumnList.size(); ++count) {
            String columnName = (String)this.__RemoveColumnList.get(count);
            if (ds.remove(columnName.toUpperCase())) continue;
            StringBuffer msg = new StringBuffer();
            msg.append("Column ").append(columnName).append(" couldnot be removed.");
            this.logger.error(msg.toString());
        }
    }

    private void setNewStorageUnitProperties(DataSet sourceDS, String newStorageUnitIds) throws Exception {
        String errMsg = null;
        HashMap replaceMap = this.getReplaceMap(sourceDS, newStorageUnitIds);
        for (int count = 0; count < sourceDS.getRowCount(); ++count) {
            String newParentId;
            String currentStorageUnitId = sourceDS.getValue(count, "storageunitid");
            String currentParentId = sourceDS.getValue(count, "parentid");
            String newStorageUnitId = (String)replaceMap.get(currentStorageUnitId);
            sourceDS.setValue(count, "storageunitid", newStorageUnitId);
            if (currentParentId == null || currentParentId.trim().length() <= 0 || (newParentId = (String)replaceMap.get(currentParentId)) == null || newParentId.trim().length() <= 0) continue;
            sourceDS.setValue(count, "parentid", newParentId);
        }
        DataSetUtil.update(this.database, sourceDS, "storageunit", new String[]{"storageunitid"});
        if (errMsg != null && errMsg.trim().length() > 0) {
            throw new Exception(errMsg);
        }
    }

    private HashMap getReplaceMap(DataSet sourceDS, String newStorageUnitIds) throws Exception {
        HashMap<String, String> replaceMap = new HashMap<String, String>();
        String[] newStorageUnitsIdsArr = StringUtil.split(newStorageUnitIds, ";");
        if (newStorageUnitsIdsArr.length != sourceDS.getRowCount()) {
            StringBuffer msg = new StringBuffer();
            msg.append("Storage units count - created and existing in hierarchy doesnot match.");
            this.logger.error(msg.toString());
            throw new Exception(msg.toString());
        }
        for (int count = 0; count < sourceDS.getRowCount(); ++count) {
            String oldStorageUnitId = sourceDS.getValue(count, "storageunitid");
            String newStorageUnitId = newStorageUnitsIdsArr[count];
            replaceMap.put(oldStorageUnitId, newStorageUnitId);
        }
        return replaceMap;
    }

    private int doSyncStorageUnit(HashMap props) {
        int rc = 1;
        String storageunitid = (String)props.get("storageunitid");
        if (storageunitid == null || storageunitid.length() == 0) {
            return this.setError("Null Storage Unit Id.");
        }
        String[] arr = StringUtil.split(storageunitid, ";");
        try {
            for (String anArr : arr) {
                this.syncStorageUnitChildAncestor(anArr, props);
            }
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("storageunitid", storageunitid);
            actionProps.setProperty("auditreason", props.getOrDefault("auditreason", ""));
            actionProps.setProperty("auditactivity", props.getOrDefault("auditactivity", ""));
            actionProps.setProperty("auditsignedflag", props.getOrDefault("auditsignedflag", ""));
            this.getActionProcessor().processActionClass(SyncStorageLabelPath.class.getName(), actionProps);
        }
        catch (ActionException e) {
            this.logger.error(e.getMessage(), e);
            this.setErrors(e.getErrorHandler());
        }
        return rc;
    }

    private void syncStorageUnitChildAncestor(String storageunitid, HashMap actionProps) throws ActionException {
        DataSet hierarchy = StorageUnitSDC.populateChild(this.getQueryProcessor(), storageunitid, null, this.database.isOracle());
        if (hierarchy != null && hierarchy.size() > 0) {
            DataSet ds = new DataSet();
            ds.addColumn("storageunitid", 0);
            ds.addColumn("ancestorid", 0);
            String ancestorid = hierarchy.getString(0, "ancestorid", "");
            String storageenvid = hierarchy.getString(0, "storageenvid", "");
            if (StringUtil.getLen(storageenvid) == 0L) {
                StringBuilder sql = new StringBuilder();
                sql.append("select storageunitid, ancestorid, storageenvid from storageunit where storageunitid = (select su.parentid from storageunit su where su.storageunitid = ? )");
                DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{storageunitid});
                if (_ds != null && _ds.size() > 0) {
                    int row;
                    String _storageunitid = _ds.getString(0, "storageunitid", "");
                    String _environmentid = _ds.getString(0, "storageenvid", "");
                    String _ancestorid = _ds.getString(0, "ancestorid", "");
                    if (StringUtil.getLen(_environmentid) > 0L) {
                        ancestorid = _storageunitid;
                        row = ds.addRow();
                        ds.setString(row, "storageunitid", storageunitid);
                        ds.setString(row, "ancestorid", ancestorid);
                    } else if (StringUtil.getLen(_ancestorid) > 0L) {
                        ancestorid = _ancestorid;
                        row = ds.addRow();
                        ds.setString(row, "storageunitid", storageunitid);
                        ds.setString(row, "ancestorid", ancestorid);
                    } else {
                        if (StringUtil.getLen(ancestorid) > 0L) {
                            row = ds.addRow();
                            ds.setString(row, "storageunitid", storageunitid);
                            ds.setString(row, "ancestorid", "(null)");
                        }
                        ancestorid = "(null)";
                    }
                } else {
                    if (StringUtil.getLen(ancestorid) > 0L) {
                        int row = ds.addRow();
                        ds.setString(row, "storageunitid", storageunitid);
                        ds.setString(row, "ancestorid", "(null)");
                    }
                    ancestorid = "(null)";
                }
            } else if (StringUtil.getLen(storageenvid) > 0L) {
                int row = ds.addRow();
                ds.setString(row, "storageunitid", storageunitid);
                ds.setString(row, "ancestorid", "(null)");
                ancestorid = storageunitid;
            }
            ArrayList<String> skipParentList = new ArrayList<String>();
            for (int i = 1; i < hierarchy.size(); ++i) {
                String childstorageid = hierarchy.getString(i, "storageunitid");
                String parentid = hierarchy.getString(i, "parentid", "");
                String childstorageenvid = hierarchy.getString(i, "storageenvid", "");
                String childancestorid = hierarchy.getString(i, "ancestorid", "");
                if (!skipParentList.contains(parentid)) {
                    if (StringUtil.getLen(childstorageenvid) > 0L) {
                        skipParentList.add(childstorageid);
                        continue;
                    }
                    if (childancestorid.equals(ancestorid)) continue;
                    int row = ds.addRow();
                    ds.setString(row, "storageunitid", childstorageid);
                    ds.setString(row, "ancestorid", ancestorid);
                    continue;
                }
                skipParentList.add(childstorageid);
            }
            if (ds.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "StorageUnitSDC");
                props.setProperty("keyid1", ds.getColumnValues("storageunitid", ";"));
                props.setProperty("ancestorid", ds.getColumnValues("ancestorid", ";"));
                props.setProperty("auditreason", "Synchronization of Ancestor");
                props.setProperty("propsmatch", "Y");
                props.setProperty("__syncoperation", "Y");
                props.setProperty("auditreason", actionProps.getOrDefault("auditreason", ""));
                props.setProperty("auditactivity", actionProps.getOrDefault("auditactivity", ""));
                props.setProperty("auditsignedflag", actionProps.getOrDefault("auditsignedflag", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
    }
}

