/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDIAlias
extends BaseAction
implements sapphire.action.DeleteSDIAlias {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = (String)properties.get("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("Unable to delete SDIAlias: No SDC specified.");
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
        String keycolid2 = sdc.getProperty("keycolid2");
        String keycolid3 = sdc.getProperty("keycolid3");
        DataSet ds = new DataSet();
        ds.addColumnValues("keyid1", 0, (String)properties.get("keyid1"), ";");
        ds.addColumnValues("keyid2", 0, (String)properties.get("keyid2"), ";", "(null)");
        ds.addColumnValues("keyid3", 0, (String)properties.get("keyid3"), ";", "(null)");
        ds.addColumnValues("aliasid", 0, (String)properties.get("aliasid"), ";");
        ds.addColumnValues("aliastype", 0, (String)properties.get("aliastype"), ";");
        ds.padColumns();
        String sql = "DELETE FROM sdialias WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND aliasid = ? AND aliastype = ?";
        try {
            PreparedStatement statement = this.database.prepareStatement("Delete sdialias", sql);
            statement.setString(1, sdcid);
            for (int i = 0; i < ds.size(); ++i) {
                String keyid1 = ds.getValue(i, "keyid1");
                String keyid2 = ds.getValue(i, "keyid2");
                String keyid3 = ds.getValue(i, "keyid3");
                String aliasid = ds.getValue(i, "aliasid");
                String aliastype = ds.getValue(i, "aliastype");
                if (aliastype.equals("")) {
                    aliastype = "Standard";
                }
                if (keyid1.length() <= 0 || aliasid.length() <= 0) continue;
                statement.setString(2, keyid1);
                statement.setString(3, keyid2);
                statement.setString(4, keyid3);
                statement.setString(5, aliasid);
                statement.setString(6, aliastype);
                statement.executeUpdate();
            }
            this.database.closeStatement("Delete sdialias");
        }
        catch (SQLException e) {
            throw new SapphireException("Unable to delete SDIAlias: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        if (!auditflag.equalsIgnoreCase("N")) {
            if (properties.getProperty("tracelogid", "").length() == 0 && reason != null && reason.length() > 0) {
                String keyid1 = ds.getColumnValues("keyid1", ";");
                String keyid2 = ds.getColumnValues("keyid2", ";");
                String keyid3 = ds.getColumnValues("keyid3", ";");
                this.logger.info("Generate the tracelog records");
                String standard = "Y";
                if (!promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S")) {
                    standard = "N";
                }
                AuditService audit = new AuditService(sapphireConnection);
                try {
                    tracelogid = Integer.parseInt(audit.addSDITraceLogEntry(sdcid, keyid1, keyid2, keyid3, reason, activity, signedflag, auditdt, "Delete SDI Alias", standard.equals("Y")));
                    properties.setProperty("tracelogid", String.valueOf(tracelogid));
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to add audit records", e);
                }
            }
            int keycolcount = 1;
            String[] keyid1prop = StringUtil.split(ds.getColumnValues("keyid1", ";"), ";");
            String[] keyid2prop = null;
            String[] keyid3prop = null;
            String updateSQL = "";
            if (keycolid2 != null && keycolid2.length() > 0) {
                keycolcount = 2;
                keyid2prop = StringUtil.split(ds.getColumnValues("keyid2", ";"), ";");
            }
            if (keycolid3 != null && keycolid3.length() > 0) {
                keycolcount = 3;
                keyid3prop = StringUtil.split(ds.getColumnValues("keyid3", ";"), ";");
            }
            updateSQL = tracelogid > 0 ? updateSQL + "UPDATE a_sdialias SET tracelogid = ?, modtool = ?  WHERE keyid1= ? AND tracelogid = 'DELETED'" : updateSQL + "UPDATE a_sdialias SET modtool = ?  WHERE keyid1= ? AND tracelogid = 'DELETED'";
            if (keycolid2 != null && keycolid2.length() > 0) {
                keycolcount = 2;
                updateSQL = updateSQL + " AND keyid2= ?";
            }
            if (keycolid3 != null && keycolid3.length() > 0) {
                keycolcount = 3;
                updateSQL = updateSQL + " AND keyid3= ?";
            }
            try {
                this.logger.info("Updating audit records using: " + updateSQL);
                PreparedStatement updateAuditPS = this.database.prepareStatement(updateSQL);
                for (int i = 0; i < keyid1prop.length; ++i) {
                    if (tracelogid > 0) {
                        updateAuditPS.setString(1, String.valueOf(tracelogid + i));
                        updateAuditPS.setString(2, "DeleteSDIAlias");
                        updateAuditPS.setString(3, keyid1prop[i]);
                        if (keycolcount > 1) {
                            updateAuditPS.setString(4, keyid2prop[i]);
                        }
                        if (keycolcount > 2) {
                            updateAuditPS.setString(5, keyid3prop[i]);
                        }
                    } else {
                        updateAuditPS.setString(1, "DeleteSDIAlias");
                        updateAuditPS.setString(2, keyid1prop[i]);
                        if (keycolcount > 1) {
                            updateAuditPS.setString(3, keyid2prop[i]);
                        }
                        if (keycolcount > 2) {
                            updateAuditPS.setString(4, keyid3prop[i]);
                        }
                    }
                    try {
                        int rows = updateAuditPS.executeUpdate();
                        if (rows == 1) continue;
                        this.logger.error("Update the tracelogid in the audit table, update returned: " + String.valueOf(rows));
                        continue;
                    }
                    catch (SQLException e) {
                        throw new SapphireException("EXECUTE_STMT_FAILED", "Error Updating the audit record. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())) + " executing " + updateSQL, e);
                    }
                }
            }
            catch (Exception e) {
                this.logger.info("DeleteSDIAlias", "Error Updating the audit record. Exception: " + e.getMessage() + " executing " + updateSQL);
            }
        }
    }
}

