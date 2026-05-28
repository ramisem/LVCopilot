/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class CheckDates
extends BaseAction
implements sapphire.action.CheckDates {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcId = properties.getProperty("sdcid");
        String columnId = properties.getProperty("columnid");
        String processActionId = properties.getProperty("processactionid");
        String processActionVersionId = properties.getProperty("processactionversionid");
        String batch = properties.getProperty("batch");
        String asynchronous = properties.getProperty("asynchronous");
        if (sdcId == null || sdcId.length() == 0) {
            throw new SapphireException("Mandatory input parameters sdcid not specified");
        }
        if (columnId == null || columnId.length() == 0) {
            throw new SapphireException("Mandatory input parameters columnid not specified");
        }
        if (processActionId == null || processActionId.length() == 0) {
            throw new SapphireException("Mandatory input parameters processactionid not specified");
        }
        if (processActionVersionId == null || processActionVersionId.length() == 0) {
            processActionVersionId = "1";
        }
        SDCProcessor sdcP = this.getSDCProcessor();
        String tableId = sdcP.getProperty(sdcId, "tableid");
        String keyColId2 = "";
        String keyColId3 = "";
        int keyCount = Integer.parseInt(sdcP.getProperty(sdcId, "keycolumns"));
        String keyColId1 = sdcP.getProperty(sdcId, "keycolid1");
        if (keyCount > 1) {
            keyColId2 = sdcP.getProperty(sdcId, "keycolid2");
        }
        if (keyCount == 3) {
            keyColId3 = sdcP.getProperty(sdcId, "keycolid3");
        }
        String whereClause = properties.getProperty("whereclause");
        String selectColumns = keyColId1;
        if (keyCount > 1) {
            selectColumns = selectColumns + "," + keyColId2;
        }
        if (keyCount > 2) {
            selectColumns = selectColumns + "," + keyColId3;
        }
        String dateCol = "";
        dateCol = this.getConnectionProcessor().isOra() ? "SYSDATE" : "getdate()";
        String sql = "SELECT " + selectColumns + " FROM " + tableId + " WHERE " + columnId + " <=" + dateCol;
        if (whereClause != null && whereClause.length() > 0) {
            sql = sql + " AND " + whereClause;
        }
        this.database.createResultSet(sql);
        DataSet ds = new DataSet(this.database.getResultSet());
        if (ds.getRowCount() == 0) {
            return;
        }
        properties.remove("columnid");
        properties.remove("whereclause");
        properties.remove("actionid");
        properties.remove("processactionid");
        properties.remove("batch");
        properties.remove("asynchronous");
        if ("N".equalsIgnoreCase(batch)) {
            boolean exceptionFound = false;
            for (int row = 0; row < ds.getRowCount(); ++row) {
                PropertyList actionProps = properties.copy();
                actionProps.setProperty("keyid1", ds.getString(row, keyColId1));
                if (keyColId2.trim().length() > 0) {
                    actionProps.setProperty("keyid2", ds.getString(row, keyColId2));
                }
                if (keyColId3.trim().length() > 0) {
                    actionProps.setProperty("keyid3", ds.getString(row, keyColId3));
                }
                try {
                    if ("Y".equalsIgnoreCase(asynchronous)) {
                        AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                        ac.addToDoListEntry(null, processActionId, processActionVersionId, actionProps, null, true);
                        continue;
                    }
                    this.getActionProcessor().processAction(processActionId, processActionVersionId, actionProps);
                    continue;
                }
                catch (Exception e) {
                    this.logger.error("Failed to process action " + processActionId, e);
                    exceptionFound = true;
                }
            }
            if (exceptionFound) {
                String msg = "One or more of the notification actions failed";
                throw new SapphireException("PROCESSACTION_FAILED", msg);
            }
        } else {
            PropertyList actionProps = properties.copy();
            String keyId1List = ds.getColumnValues(keyColId1, ";");
            actionProps.setProperty("keyid1", keyId1List);
            if (keyColId2.trim().length() > 0) {
                String keyId2List = ds.getColumnValues(keyColId2, ";");
                actionProps.setProperty("keyid2", keyId2List);
            }
            if (keyColId3.trim().length() > 0) {
                String keyId3List = ds.getColumnValues(keyColId3, ";");
                actionProps.setProperty("keyid3", keyId3List);
            }
            try {
                if ("Y".equalsIgnoreCase(asynchronous)) {
                    AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    ac.addToDoListEntry(null, processActionId, processActionVersionId, actionProps, null, true);
                } else {
                    this.getActionProcessor().processAction(processActionId, "1", actionProps);
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to process action", e);
            }
        }
    }
}

