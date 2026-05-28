/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.actions.CreateDataCapture;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_DataCapture
extends BaseSDCRules
implements SDMSConstants {
    public static boolean hasEditableAccess(HashMap user, HashMap primary, String role, String viewonlyelement) {
        String datacapturestatus;
        boolean hasRole = false;
        String viewonly = "Y";
        ArrayList roleList = (ArrayList)user.get("rolelist");
        for (String userRole : roleList) {
            if (!userRole.equalsIgnoreCase(role)) continue;
            hasRole = true;
            break;
        }
        datacapturestatus = (datacapturestatus = (String)primary.get("datacapturestatus")) == null ? "" : datacapturestatus;
        String sdmscollectorid = (String)primary.get("sdmscollectorid");
        String string = sdmscollectorid = sdmscollectorid == null ? "" : sdmscollectorid;
        if (sdmscollectorid.length() > 0 && hasRole) {
            viewonly = "N";
        } else if (sdmscollectorid.length() == 0 && (datacapturestatus.equalsIgnoreCase("Capturing") || hasRole)) {
            viewonly = "N";
        }
        return viewonly.equalsIgnoreCase(viewonlyelement);
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            DataSet instrument;
            if (primary.getValue(i, "datacapturestatus", "").length() == 0) {
                primary.setValue(i, "datacapturestatus", "Capturing");
            }
            if (primary.getValue(i, "instrumentid", "").length() <= 0 || (instrument = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM instrument WHERE instrumentid=?", (Object[])new String[]{primary.getValue(i, "instrumentid", "")})) == null || instrument.getRowCount() <= 0) continue;
            if (!primary.isValidColumn("postponeprocessingflag")) {
                primary.addColumn("postponeprocessingflag", 0);
            }
            primary.setValue(i, "postponeprocessingflag", instrument.getValue(0, "postponeprocessingflag", "N"));
            this.logger.debug("Postponeprocessing set");
            this.copydownInstrumentDept(primary, i, instrument);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (primary.getValue(i, "instrumentid", "").length() <= 0) continue;
            CreateDataCapture.addAttachmentOperations(primary.getValue(i, "instrumentid", ""), primary.getValue(i, "datacaptureid"), false, this.getQueryProcessor(), this.database, this.logger);
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (!primary.isValidColumn("instrumentid")) continue;
            String newval = primary.getValue(i, "instrumentid", "");
            String oldval = this.getOldPrimaryValue(primary, i, "instrumentid");
            if ((newval.length() <= 0 || oldval.length() != 0) && oldval.equalsIgnoreCase(newval)) continue;
            CreateDataCapture.addAttachmentOperations(newval, primary.getValue(i, "datacaptureid"), oldval.length() > 0, this.getQueryProcessor(), this.database, this.logger);
            DataSet instrument = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM instrument WHERE instrumentid=?", (Object[])new String[]{newval});
            if (instrument == null || instrument.getRowCount() <= 0) continue;
            if (!primary.isValidColumn("postponeprocessingflag")) {
                primary.addColumn("postponeprocessingflag", 0);
            }
            primary.setValue(i, "postponeprocessingflag", instrument.getValue(0, "postponeprocessingflag", "N"));
            this.copydownInstrumentDept(primary, i, instrument);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (sdiData.getDataset("primary") != null && sdiData.getDataset("primary").getRowCount() == 1) {
            String newstatus = sdiData.getDataset("primary").getValue(0, "datacapturestatus", "");
            String oldstatus = this.getBeforeEditImage().getDataset("primary").getValue(0, "datacapturestatus", "");
            if (oldstatus.equalsIgnoreCase("Capturing") && newstatus.equalsIgnoreCase("Captured")) {
                String datacaptureid = actionProps.getProperty("keyid1");
                DataSet datacapture = this.getQueryProcessor().getPreparedSqlDataSet("SELECT postponeprocessingflag FROM datacapture WHERE datacaptureid=?", (Object[])new String[]{datacaptureid});
                if (datacapture != null && datacapture.getRowCount() == 1 && !datacapture.getValue(0, "postponeprocessingflag", "N").equalsIgnoreCase("Y")) {
                    this.processCapture(datacaptureid);
                } else {
                    SafeSQL safeSQL = new SafeSQL();
                    try {
                        this.database.executePreparedUpdate("UPDATE datacapture SET datacapturestatus=" + safeSQL.addVar("Pending Processing") + " WHERE datacaptureid=" + safeSQL.addVar(datacaptureid), safeSQL.getValues());
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to update data capture status to pending processing.");
                        throw new SapphireException(e);
                    }
                }
            } else if (!oldstatus.equalsIgnoreCase("Ready") && newstatus.equalsIgnoreCase("Ready")) {
                String datacaptureid = actionProps.getProperty("keyid1");
                DataSet datacapture = this.getQueryProcessor().getPreparedSqlDataSet("SELECT postponeprocessingflag FROM datacapture WHERE datacaptureid=?", (Object[])new String[]{datacaptureid});
                if (datacapture != null && datacapture.getRowCount() == 1 && datacapture.getValue(0, "postponeprocessingflag", "N").equalsIgnoreCase("Y")) {
                    this.processCapture(datacaptureid);
                }
            } else {
                if (newstatus.equalsIgnoreCase("Capturing") && !oldstatus.equalsIgnoreCase("Capturing")) {
                    SafeSQL safeSQL = new SafeSQL();
                    try {
                        this.database.executePreparedUpdate("UPDATE sdiattachmentoperation SET operationstatus=" + safeSQL.addVar("ready") + " WHERE sdcid=" + safeSQL.addVar("LV_DataCapture") + " AND keyid1=" + safeSQL.addVar(sdiData.getDataset("primary").getValue(0, "datacaptureid", "")), safeSQL.getValues());
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to update operation status to ready.");
                        throw new SapphireException(e);
                    }
                }
                if (newstatus.equalsIgnoreCase("Failure") && !oldstatus.equalsIgnoreCase("Failure")) {
                    String instrumentid;
                    ConfigurationProcessor cp = this.getConfigurationProcessor();
                    if (cp.getPolicy("SDMSPolicy", "Sapphire Custom").getPropertyListNotNull("errorhandling").getProperty("ondatacapturefailure").equals("P") && (instrumentid = this.getBeforeEditImage().getDataset("primary").getValue(0, "instrumentid")).length() > 0) {
                        this.pauseInstrument(instrumentid);
                    }
                    SafeSQL safeSQL = new SafeSQL();
                    try {
                        this.database.executePreparedUpdate("UPDATE sdiattachmentoperation SET operationstatus=" + safeSQL.addVar("ready") + " WHERE sdcid=" + safeSQL.addVar("LV_DataCapture") + " AND keyid1=" + safeSQL.addVar(sdiData.getDataset("primary").getValue(0, "datacaptureid", "")), safeSQL.getValues());
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to update operation status to ready.");
                        throw new SapphireException(e);
                    }
                }
            }
        }
    }

    private void pauseInstrument(String instrumentid) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Instrument");
        props.setProperty("keyid1", instrumentid);
        props.setProperty("sdmspausedflag", "Y");
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void processCapture(String datacaptureid) throws SapphireException {
        try {
            PropertyList props = new PropertyList();
            props.setProperty("actionid", "AttachmentOperationsProcessor");
            props.setProperty("actionversionid", "1");
            props.setProperty("sdcid", "LV_DataCapture");
            props.setProperty("keyid1", datacaptureid);
            props.setProperty("statusstart", "Processing");
            props.setProperty("statusend", "Processed");
            props.setProperty("statuscolumn", "datacapturestatus");
            this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props, false);
        }
        catch (Exception e) {
            this.logger.error("Failed to process data capture.", e);
            throw new SapphireException(e);
        }
    }

    private void copydownInstrumentDept(DataSet primary, int rowid, DataSet instrument) {
        boolean deptSecurityEnabled = "D".equalsIgnoreCase(this.getSDCProcessor().getProperty("LV_DataCapture", "accesscontrolledflag"));
        if (deptSecurityEnabled) {
            DataSet collector;
            String instDept = instrument.getString(0, "securitydepartment", "");
            if (instDept.length() == 0 && (instDept = instrument.getString(0, "testingdepartmentid", "")).length() == 0 && instrument.getValue(0, "sdmscollectorid").length() > 0 && (collector = this.getQueryProcessor().getPreparedSqlDataSet("SELECT testingdepartmentid,securitydepartment FROM sdmscollector WHERE sdmscollectorid=?", new Object[]{instrument.getValue(0, "sdmscollectorid")})) != null && collector.size() > 0 && (instDept = collector.getString(0, "securitydepartment", "")).length() == 0) {
                instDept = collector.getString(0, "testingdepartmentid", "");
            }
            if (instDept.length() > 0) {
                if (!primary.isValidColumn("securitydepartment")) {
                    primary.addColumn("securitydepartment", 0);
                }
                primary.setString(rowid, "securitydepartment", instDept);
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String sql = "DELETE FROM sdidatacapture WHERE datacaptureid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ? )";
        this.database.executePreparedUpdate(sql, new Object[]{rsetid});
        sql = "update sdidataitem set datacaptureid='' WHERE datacaptureid in (SELECT keyid1 FROM rsetitems WHERE rsetid = ? )";
        this.database.executePreparedUpdate(sql, new Object[]{rsetid});
    }
}

