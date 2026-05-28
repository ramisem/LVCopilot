/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_Array
extends BaseSDCRules {
    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet arraymethoditems = sdiData.getDataset("arrayarraymethoditem");
        if (arraymethoditems != null && arraymethoditems.getRowCount() > 0) {
            arraymethoditems.sort("usersequence D");
            String currentItemStatus = arraymethoditems.getString(0, "arraymethoditemstatus");
            if (currentItemStatus.equals("Initial")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "Applied");
            } else if (currentItemStatus.equals("Loaded")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "Loaded");
            } else if (currentItemStatus.equals("SentToInstrument")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "SentToInstrument");
            } else if (currentItemStatus.equals("DataEntered")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "DataEntered");
            } else if (currentItemStatus.equals("Reviewed")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "Reviewed");
            } else if (currentItemStatus.equals("Disposed")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "Disposed");
            } else if (currentItemStatus.equals("Cancelled")) {
                this.checkAndCancelArrayStatus(actionProps.getProperty("arrayid"), arraymethoditems);
            }
        }
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet arraymethoditems = sdiData.getDataset("arrayarraymethoditem");
        if (arraymethoditems != null && arraymethoditems.getRowCount() > 0) {
            arraymethoditems.sort("usersequence D");
            String currentItemStatus = arraymethoditems.getString(0, "arraymethoditemstatus");
            if (currentItemStatus.equals("Initial")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "Applied");
            } else if (currentItemStatus.equals("Loaded")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "Loaded");
            } else if (currentItemStatus.equals("SentToInstrument")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "SentToInstrument");
            } else if (currentItemStatus.equals("DataEntered")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "DataEntered");
            } else if (currentItemStatus.equals("Reviewed")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "Reviewed");
            } else if (currentItemStatus.equals("Disposed")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "Disposed");
            } else if (currentItemStatus.equals("Cancelled")) {
                this.checkAndCancelArrayStatus(actionProps.getProperty("arrayid"), arraymethoditems);
            } else if (currentItemStatus.equals("Completed")) {
                this.checkAndUpdateArrayStatus(actionProps.getProperty("arrayid"), "Completed");
            }
        }
    }

    private void checkAndCancelArrayStatus(String arrayid, DataSet arraymethoditems) throws SapphireException {
        String[] arrayidlist = StringUtil.split(arrayid, ";");
        String inClause = "";
        for (int i = 0; i < arrayidlist.length; ++i) {
            if (i != 0) {
                inClause = inClause + ", ";
            }
            inClause = inClause + "'" + arrayidlist[i] + "'";
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT arrayid from array WHERE arrayid IN ( " + safeSQL.addIn(inClause) + ") AND arraystatus != 'Cancelled'";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        DataSet cancelList = new DataSet();
        if (ds != null && ds.getRowCount() > 0) {
            for (int i = 0; i < arrayidlist.length; ++i) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("arrayid", arrayidlist[i]);
                DataSet currentarraymethoditems = arraymethoditems.getFilteredDataSet(filter);
                filter.put("arraymethoditemstatus", "Cancelled");
                DataSet currentCancelledItems = currentarraymethoditems.getFilteredDataSet(filter);
                if (currentCancelledItems.getRowCount() != currentarraymethoditems.getRowCount()) continue;
                int row = cancelList.addRow();
                cancelList.setString(row, "arrayid", arrayidlist[i]);
            }
            if (cancelList.getRowCount() > 0) {
                cancelList.setString(0, "arraystatus", "Cancelled");
                cancelList.padColumn("arraystatus");
                this.updateArrayStatus(cancelList.getColumnValues("arrayid", ";"), cancelList.getColumnValues("arraystatus", ";"), null);
            }
        }
    }

    private void checkAndUpdateArrayStatus(String arrayid, String arrayStatus) throws SapphireException {
        String[] arrayidlist = StringUtil.split(arrayid, ";");
        String inClause = "";
        for (int i = 0; i < arrayidlist.length; ++i) {
            if (i != 0) {
                inClause = inClause + ", ";
            }
            inClause = inClause + "'" + arrayidlist[i] + "'";
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT arrayid from array WHERE arrayid IN ( " + safeSQL.addIn(inClause) + " ) AND arraystatus != " + safeSQL.addVar(arrayStatus) + "";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        ds.setString(0, "arraystatus", arrayStatus);
        ds.padColumn("arraystatus");
        if (arrayStatus.equalsIgnoreCase("SentToInstrument")) {
            ds.setString(0, "deliveredrunfileflag", "Y");
            ds.padColumn("deliveredrunfileflag");
        }
        if (ds != null && ds.getRowCount() > 0) {
            this.updateArrayStatus(ds.getColumnValues("arrayid", ";"), ds.getColumnValues("arraystatus", ";"), ds.getColumnValues("deliveredrunfileflag", ";"));
        }
    }

    private void updateArrayStatus(String arrayid, String arrayStatus, String deliverredrunfileflag) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Array");
        props.setProperty("keyid1", arrayid);
        props.setProperty("arraystatus", arrayStatus);
        if (deliverredrunfileflag != null) {
            props.setProperty("deliveredrunfileflag", deliverredrunfileflag);
        }
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    @Override
    public void postDataEntry(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DataSet sdidata = sdiData.getDataset("dataset");
        if (primary.getRowCount() > 0) {
            String arrayId = primary.getValue(0, "arrayid");
            this.promoteArrayResults(arrayId, sdidata);
        }
    }

    @Override
    public boolean requiresDataEntryPrimary() {
        return true;
    }

    private void promoteArrayResults(String arrayId, DataSet sdidata) throws SapphireException {
        HashMap<String, Object> findmap;
        String arrayMethodInstance;
        String arrayMethodVersionId;
        String arrayMethodId;
        int i;
        String sql = "SELECT aami.arraymethodid, aami.arraymethodversionid, aami.arraymethodinstance, coalesce(aami.promoteresultsflag, 'D') promoteresultsflag  FROM arrayarraymethoditem aami  WHERE aami.arrayid = ? AND aami.usersequence=(select max(usersequence) from arrayarraymethoditem where arraymethoditemstatus <> 'Cancelled' and arrayid = aami.arrayid)";
        this.database.createPreparedResultSet("aami", sql, new Object[]{arrayId});
        DataSet allitems = new DataSet(this.database.getResultSet("aami"));
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("promoteresultsflag", "S");
        DataSet promoteItems = allitems.getFilteredDataSet(filter);
        filter.put("promoteresultsflag", "D");
        DataSet deferredItems = allitems.getFilteredDataSet(filter);
        this.database.closeResultSet("aami");
        DataSet actionProps = new DataSet();
        DataSet statusUpdates = new DataSet();
        for (i = 0; i < promoteItems.getRowCount(); ++i) {
            arrayMethodId = promoteItems.getString(i, "arraymethodid");
            arrayMethodVersionId = promoteItems.getString(i, "arraymethodversionid");
            arrayMethodInstance = promoteItems.getValue(i, "arraymethodinstance");
            findmap = new HashMap<String, Object>();
            findmap.put("arraymethodid", arrayMethodId);
            findmap.put("arraymethodversionid", arrayMethodVersionId);
            findmap.put("arraymethodinstance", new BigDecimal(arrayMethodInstance));
            if (sdidata.findRow(findmap) <= -1) continue;
            int r = actionProps.addRow();
            actionProps.setString(r, "arrayid", arrayId);
            actionProps.setString(r, "arraymethodid", arrayMethodId);
            actionProps.setString(r, "arraymethodversionid", arrayMethodVersionId);
            actionProps.setString(r, "arraymethodinstance", "" + arrayMethodInstance);
            int s = statusUpdates.addRow();
            statusUpdates.setString(s, "arrayid", arrayId);
            statusUpdates.setString(s, "arraymethodid", arrayMethodId);
            statusUpdates.setString(s, "arraymethodversionid", arrayMethodVersionId);
            statusUpdates.setString(s, "arraymethodinstance", "" + arrayMethodInstance);
            statusUpdates.setString(s, "arraymethoditemstatus", "Completed");
        }
        for (i = 0; i < deferredItems.getRowCount(); ++i) {
            arrayMethodId = deferredItems.getString(i, "arraymethodid");
            arrayMethodVersionId = deferredItems.getString(i, "arraymethodversionid");
            arrayMethodInstance = deferredItems.getValue(i, "arraymethodinstance");
            findmap = new HashMap();
            findmap.put("arraymethodid", arrayMethodId);
            findmap.put("arraymethodversionid", arrayMethodVersionId);
            findmap.put("arraymethodinstance", new BigDecimal(arrayMethodInstance));
            if (sdidata.findRow(findmap) <= -1) continue;
            int s = statusUpdates.addRow();
            statusUpdates.setString(s, "arrayid", arrayId);
            statusUpdates.setString(s, "arraymethodid", arrayMethodId);
            statusUpdates.setString(s, "arraymethodversionid", arrayMethodVersionId);
            statusUpdates.setString(s, "arraymethodinstance", "" + arrayMethodInstance);
            statusUpdates.setString(s, "arraymethoditemstatus", "DataEntered");
        }
        if (actionProps.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("arrayid", actionProps.getColumnValues("arrayid", ";"));
            props.setProperty("arraymethodid", actionProps.getColumnValues("arraymethodid", ";"));
            props.setProperty("arraymethodversionid", actionProps.getColumnValues("arraymethodversionid", ";"));
            this.getActionProcessor().processAction("PromoteArrayResults", "1", props);
        }
        if (statusUpdates.getRowCount() > 0) {
            ArrayList<DataSet> arraydata = statusUpdates.getGroupedDataSets("arrayid");
            PropertyList arrayarraymethoditemprops = new PropertyList();
            for (int i2 = 0; i2 < arraydata.size(); ++i2) {
                DataSet editArray = arraydata.get(i2);
                arrayarraymethoditemprops.setProperty("sdcid", "LV_Array");
                arrayarraymethoditemprops.setProperty("linkid", "Array ArrayMethod Item");
                arrayarraymethoditemprops.setProperty("arrayid", editArray.getString(0, "arrayid"));
                arrayarraymethoditemprops.setProperty("arraymethodid", editArray.getColumnValues("arraymethodid", ";"));
                arrayarraymethoditemprops.setProperty("arraymethodversionid", editArray.getColumnValues("arraymethodversionid", ";"));
                arrayarraymethoditemprops.setProperty("arraymethodinstance", editArray.getColumnValues("arraymethodinstance", ";"));
                arrayarraymethoditemprops.setProperty("arraymethoditemstatus", editArray.getColumnValues("arraymethoditemstatus", ";"));
                this.getActionProcessor().processAction("EditSDIDetail", "1", arrayarraymethoditemprops);
            }
        }
    }
}

