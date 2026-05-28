/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SubmitSDIForApproval
extends BaseAction
implements sapphire.action.SubmitSDIForApproval {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList property) throws SapphireException {
        String sdcid = property.getProperty("sdcid");
        String keyid1 = property.getProperty("keyid1");
        String keyid2 = property.getProperty("keyid2");
        String keyid3 = property.getProperty("keyid3");
        String pendingApprovalStatus = property.getProperty("pendingapprovalstatus");
        String approvalStatus = property.getProperty("approvalstatus");
        String approvalStatusColumn = property.getProperty("approvalstatuscolumn");
        String traceLogId = property.getProperty("tracelogid", "");
        String auditReason = property.getProperty("auditreason", "");
        String auditSignedFlag = property.getProperty("auditsignedflag", "N");
        String auditActivity = property.getProperty("auditactivity", "");
        String approvalFunction = property.getProperty("approvalfunction", "");
        String bypassEventPlan = property.getProperty("__bypasseventplan", "N");
        if (sdcid == null || sdcid.length() == 0) {
            throw new SapphireException("PROCESSACTION_FAILED", "SDC Id not passed into this action");
        }
        if (keyid1 == null || keyid1.length() == 0) {
            throw new SapphireException("PROCESSACTION_FAILED", "Keyid1 has not passed into this action");
        }
        if (pendingApprovalStatus == null || pendingApprovalStatus.length() == 0) {
            pendingApprovalStatus = "PendingApproval";
        }
        if (approvalStatus == null || approvalStatus.length() == 0) {
            approvalStatus = "Approved";
        }
        StringBuffer noApprovalKeyid1 = new StringBuffer();
        StringBuffer noApprovalKeyid2 = new StringBuffer();
        StringBuffer noApprovalKeyid3 = new StringBuffer();
        StringBuffer pendingApprovalKeyid1 = new StringBuffer();
        StringBuffer pendingApprovalKeyid2 = new StringBuffer();
        StringBuffer pendingApprovalKeyid3 = new StringBuffer();
        String[] keyid1Array = StringUtil.split(keyid1, ";");
        String[] keyid2Array = null;
        String[] keyid3Array = null;
        if (keyid2 != null && keyid2.length() > 0) {
            keyid2Array = StringUtil.split(keyid2, ";");
            if (keyid3 != null && keyid3.length() > 0) {
                keyid3Array = StringUtil.split(keyid3, ";");
            }
        }
        try {
            String sqlGetApprovals = "SELECT approvaltypeid FROM sdiapproval WHERE sdcid = '" + sdcid + "' AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?";
            if (approvalFunction.length() > 0) {
                sqlGetApprovals = sqlGetApprovals + " AND approvalfunction='" + approvalFunction + "'";
            }
            PreparedStatement sdiApprovalStatement = this.database.prepareStatement("getsdiapprovals", sqlGetApprovals);
            for (int i = 0; i < keyid1Array.length; ++i) {
                String key1 = keyid1Array[i];
                String key2 = "(null)";
                String key3 = "(null)";
                if (keyid2Array != null && keyid2Array.length > 0) {
                    key2 = keyid2Array[i];
                    if (keyid3Array != null && keyid3Array.length > 0) {
                        key3 = keyid3Array[i];
                    }
                }
                sdiApprovalStatement.setString(1, key1);
                sdiApprovalStatement.setString(2, key2);
                sdiApprovalStatement.setString(3, key3);
                DataSet sdiApprovalDs = new DataSet(sdiApprovalStatement.executeQuery());
                int approvalCount = sdiApprovalDs.getRowCount();
                if (approvalCount == 0) {
                    noApprovalKeyid1.append(";").append(key1);
                    noApprovalKeyid2.append(";").append(key2);
                    noApprovalKeyid3.append(";").append(key3);
                    continue;
                }
                pendingApprovalKeyid1.append(";").append(key1);
                pendingApprovalKeyid2.append(";").append(key2);
                pendingApprovalKeyid3.append(";").append(key3);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            this.database.closeStatement("getsdiapprovals");
        }
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        if (!sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N") && traceLogId.length() == 0 && auditReason.length() > 0) {
            this.logger.info("Generate the tracelog records");
            String promptflag = sdcProcessor.getProperty(sdcid, "auditpromptflag");
            String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
            AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            try {
                traceLogId = audit.addSDITraceLogEntry(sdcid, keyid1, keyid2, keyid3, auditReason, auditActivity, auditSignedFlag, property.getProperty("auditdt"), "Data editing", standard.equals("Y"));
                property.setProperty("tracelogid", traceLogId);
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add audit records", e);
            }
        }
        if (noApprovalKeyid1.length() > 0) {
            HashMap<String, String> editProps = new HashMap<String, String>();
            editProps.put("sdcid", sdcid);
            editProps.put("keyid1", noApprovalKeyid1.substring(1));
            editProps.put("keyid2", noApprovalKeyid2.substring(1));
            editProps.put("keyid3", noApprovalKeyid3.substring(1));
            editProps.put("tracelogid", traceLogId);
            editProps.put("auditreason", auditReason);
            editProps.put("auditsignedflag", auditSignedFlag);
            editProps.put("auditactivity", auditActivity);
            editProps.put(approvalStatusColumn, approvalStatus);
            if (bypassEventPlan.equalsIgnoreCase("Y")) {
                editProps.put("__bypasseventplan", "Y");
            }
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
            property.setProperty("newstatus", approvalStatus);
        }
        if (pendingApprovalKeyid1.length() > 0) {
            PropertyList plProps = new PropertyList();
            plProps.setProperty("sdcid", sdcid);
            plProps.setProperty("keyid1", keyid1);
            plProps.setProperty("keyid2", keyid2);
            plProps.setProperty("keyid3", keyid3);
            plProps.setProperty("ready", "Y");
            plProps.setProperty("tracelogid", traceLogId);
            plProps.setProperty("auditreason", auditReason);
            plProps.setProperty("auditsignedflag", auditSignedFlag);
            plProps.setProperty("auditactivity", auditActivity);
            plProps.setProperty("approvalfunction", approvalFunction);
            this.getActionProcessor().processAction("ResetSDIApproval", "1", plProps);
            HashMap<String, String> editProps = new HashMap<String, String>();
            editProps.put("sdcid", sdcid);
            editProps.put("keyid1", pendingApprovalKeyid1.substring(1));
            editProps.put("keyid2", pendingApprovalKeyid2.substring(1));
            editProps.put("keyid3", pendingApprovalKeyid3.substring(1));
            editProps.put("tracelogid", traceLogId);
            editProps.put("auditreason", auditReason);
            editProps.put("auditsignedflag", auditSignedFlag);
            editProps.put("auditactivity", auditActivity);
            editProps.put(approvalStatusColumn, pendingApprovalStatus);
            if (bypassEventPlan.equalsIgnoreCase("Y")) {
                editProps.put("__bypasseventplan", "Y");
            }
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
            property.setProperty("newstatus", pendingApprovalStatus);
        }
    }
}

