/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class AddSDIAlias
extends BaseAction
implements sapphire.action.AddSDIAlias {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block36: {
            String keyid3;
            String keyid2;
            String sdcid = (String)properties.get("sdcid");
            if (sdcid.length() == 0) {
                throw new SapphireException("Unable to insert SDIAlias: No SDC specified.");
            }
            SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
            this.logger.info("Getting SDC information");
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            PropertyList sdc = sdcProcessor.getPropertyList(sdcid);
            if (sdc == null) {
                throw new SapphireException("INVALID_PROPERTY", "Unrecognized SDC: " + sdcid);
            }
            String auditflag = sdc.getProperty("auditedflag");
            String promptflag = sdc.getProperty("auditpromptflag");
            int tracelogid = 0;
            String reason = properties.getProperty("auditreason");
            String activity = properties.getProperty("auditactivity");
            String signedflag = properties.getProperty("auditsignedflag");
            String auditdt = properties.getProperty("auditdt");
            boolean autoupdate = properties.getProperty("autoupdate", "N").equalsIgnoreCase("Y");
            DataSet ds = new DataSet();
            DataSet updateDS = new DataSet();
            DataSet removeDS = new DataSet();
            ds.addColumnValues("keyid1", 0, (String)properties.get("keyid1"), ";");
            updateDS.addColumn("keyid1", 0);
            removeDS.addColumn("keyid1", 0);
            ds.addColumnValues("keyid2", 0, (String)properties.get("keyid2"), ";", "(null)");
            updateDS.addColumn("keyid2", 0);
            removeDS.addColumn("keyid2", 0);
            ds.addColumnValues("keyid3", 0, (String)properties.get("keyid3"), ";", "(null)");
            updateDS.addColumn("keyid3", 0);
            removeDS.addColumn("keyid3", 0);
            ds.addColumnValues("aliastype", 0, (String)properties.get("aliastype"), ";");
            updateDS.addColumn("aliastype", 0);
            removeDS.addColumn("aliastype", 0);
            ds.addColumnValues("createtool", 0, (String)properties.get("createtool"), ";");
            updateDS.addColumn("createtool", 0);
            removeDS.addColumn("createtool", 0);
            ds.addColumnValues("modtool", 0, (String)properties.get("modtool"), ";");
            updateDS.addColumn("modtool", 0);
            removeDS.addColumn("modtool", 0);
            if (properties.getProperty("padalias", "Y").equalsIgnoreCase("Y")) {
                ds.addColumnValues("aliasid", 0, (String)properties.get("aliasid"), ";");
                updateDS.addColumn("aliasid", 0);
                removeDS.addColumn("aliasid", 0);
                ds.padColumns();
            } else {
                ds.padColumns();
                ds.addColumnValues("aliasid", 0, (String)properties.get("aliasid"), ";");
                updateDS.addColumn("aliasid", 0);
                removeDS.addColumn("aliasid", 0);
            }
            try {
                String sql = "INSERT INTO sdialias ( sdcid, keyid1, keyid2, keyid3, aliasid, aliastype, createdt, createby, createtool, modtool, tracelogid ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = this.database.prepareStatement("Insert sdialias", sql);
                try {
                    statement.setString(1, sdcid);
                    statement.setTimestamp(7, DateTimeUtil.getNowTimestamp());
                    statement.setString(8, this.connectionInfo.getSysuserId());
                    if (!auditflag.equalsIgnoreCase("N") && properties.getProperty("tracelogid", "").length() == 0 && reason != null && reason.length() > 0) {
                        String keyid1 = ds.getColumnValues("keyid1", ";");
                        String keyid22 = ds.getColumnValues("keyid2", ";");
                        String keyid32 = ds.getColumnValues("keyid3", ";");
                        this.logger.info("Generate the tracelog records");
                        String standard = "Y";
                        if (!promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S")) {
                            standard = "N";
                        }
                        AuditService audit = new AuditService(sapphireConnection);
                        try {
                            tracelogid = Integer.parseInt(audit.addSDITraceLogEntry(sdcid, keyid1, keyid22, keyid32, reason, activity, signedflag, auditdt, "Adding SDI Alias", standard.equals("Y")));
                        }
                        catch (ServiceException e) {
                            throw new SapphireException("Failed to add audit records", e);
                        }
                    }
                    for (int i = 0; i < ds.size(); ++i) {
                        String keyid1 = ds.getValue(i, "keyid1");
                        keyid2 = ds.getValue(i, "keyid2");
                        keyid3 = ds.getValue(i, "keyid3");
                        String aliasid = ds.getValue(i, "aliasid");
                        String aliastype = ds.getValue(i, "aliastype");
                        String createtool = ds.getValue(i, "createtool");
                        String modtool = ds.getValue(i, "modtool");
                        if (aliastype.equals("")) {
                            aliastype = "Standard";
                        }
                        int updateCount = 0;
                        if (autoupdate) {
                            SafeSQL safeSQL = new SafeSQL();
                            updateCount = this.database.getPreparedCount("SELECT count(aliasid) FROM sdialias WHERE sdcid=" + safeSQL.addVar(sdcid) + " AND keyid1=" + safeSQL.addVar(keyid1) + " AND keyid2=" + safeSQL.addVar(keyid2) + " AND keyid3=" + safeSQL.addVar(keyid3) + " AND aliastype=" + safeSQL.addVar(aliastype), safeSQL.getValues());
                        }
                        if (updateCount > 1) {
                            throw new SapphireException("Multiple Alias of the same Type found: " + aliastype);
                        }
                        if (updateCount > 0) {
                            if (aliasid.length() > 0) {
                                updateDS.copyRow(ds, i, 1);
                                continue;
                            }
                            removeDS.copyRow(ds, i, 1);
                            continue;
                        }
                        if (keyid1.length() <= 0 || aliasid.length() <= 0) continue;
                        statement.setString(2, keyid1);
                        statement.setString(3, keyid2);
                        statement.setString(4, keyid3);
                        statement.setString(5, aliasid);
                        statement.setString(6, aliastype);
                        statement.setString(9, createtool);
                        statement.setString(10, modtool);
                        statement.setString(11, tracelogid > 0 ? String.valueOf(tracelogid + i) : "");
                        statement.executeUpdate();
                    }
                }
                finally {
                    this.database.closeStatement("Insert sdialias");
                }
            }
            catch (SQLException e) {
                throw new SapphireException("Failed to add the alias", e);
            }
            try {
                if (updateDS.getRowCount() > 0) {
                    String deleteSql = "DELETE sdialias WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? AND aliastype=?";
                    PreparedStatement delStatement = this.database.prepareStatement("Update sdialias", deleteSql);
                    try {
                        DataSet reAddDS = new DataSet();
                        for (int i = 0; i < updateDS.getRowCount(); ++i) {
                            String keyid1 = updateDS.getValue(i, "keyid1");
                            String keyid23 = updateDS.getValue(i, "keyid2");
                            String keyid33 = updateDS.getValue(i, "keyid3");
                            String aliasid = updateDS.getValue(i, "aliasid");
                            String aliastype = updateDS.getValue(i, "aliastype");
                            if (aliastype.equals("")) {
                                aliastype = "Standard";
                            }
                            if (keyid1.length() <= 0 || aliasid.length() <= 0) continue;
                            delStatement.setString(1, sdcid);
                            delStatement.setString(2, keyid1);
                            delStatement.setString(3, keyid23);
                            delStatement.setString(4, keyid33);
                            delStatement.setString(5, aliastype);
                            delStatement.executeUpdate();
                            reAddDS.copyRow(updateDS, i, 1);
                        }
                        if (reAddDS.getRowCount() > 0) {
                            PropertyList addProps = properties.copy();
                            addProps.setProperty("sdcid", sdcid);
                            addProps.setProperty("keyid1", reAddDS.getColumnValues("keyid1", ";"));
                            addProps.setProperty("keyid2", reAddDS.getColumnValues("keyid2", ";"));
                            addProps.setProperty("keyid3", reAddDS.getColumnValues("keyid3", ";"));
                            addProps.setProperty("aliasid", reAddDS.getColumnValues("aliasid", ";"));
                            addProps.setProperty("aliastype", reAddDS.getColumnValues("aliastype", ";"));
                            addProps.setProperty("createtool", reAddDS.getColumnValues("createtool", ";"));
                            addProps.setProperty("modtool", reAddDS.getColumnValues("modtool", ";"));
                            addProps.setProperty("autoupdate", "N");
                            addProps.setProperty("auditreason", reason);
                            addProps.setProperty("auditactivity", activity);
                            addProps.setProperty("auditsignedflag", signedflag);
                            if (tracelogid > 0) {
                                addProps.setProperty("tracelogid", String.valueOf(tracelogid));
                            }
                            this.processAction(addProps);
                        }
                    }
                    finally {
                        this.database.closeStatement("Update sdialias");
                    }
                }
                if (removeDS.getRowCount() <= 0) break block36;
                String removesql = "DELETE sdialias WHERE  sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? AND aliastype=?";
                PreparedStatement removestatement = this.database.prepareStatement("Remove sdialias", removesql);
                try {
                    for (int i = 0; i < removeDS.getRowCount(); ++i) {
                        String keyid1 = removeDS.getValue(i, "keyid1");
                        keyid2 = removeDS.getValue(i, "keyid2");
                        keyid3 = removeDS.getValue(i, "keyid3");
                        String aliastype = removeDS.getValue(i, "aliastype");
                        if (aliastype.equals("")) {
                            aliastype = "Standard";
                        }
                        if (keyid1.length() <= 0) continue;
                        removestatement.setString(1, sdcid);
                        removestatement.setString(2, keyid1);
                        removestatement.setString(3, keyid2);
                        removestatement.setString(4, keyid3);
                        removestatement.setString(5, aliastype);
                        removestatement.executeUpdate();
                    }
                }
                finally {
                    this.database.closeStatement("Remove sdialias");
                }
            }
            catch (SQLException e2) {
                throw new SapphireException("Failed to update the alias", e2);
            }
        }
    }
}

