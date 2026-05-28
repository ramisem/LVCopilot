/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ResetVersionApproval
extends BaseAction {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        if (properties.getProperty("sdcid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified.");
        }
        if (properties.getProperty("keyid1").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Keyid1 specified.");
        }
        String sdcId = properties.getProperty("sdcid");
        String keyId1 = properties.getProperty("keyid1");
        String keyId2 = properties.getProperty("keyid2");
        String keyId3 = properties.getProperty("keyid3");
        String approvalFunction = properties.getProperty("approvalfunction", "");
        String approvalTypeId = properties.getProperty("approvaltypeid", "");
        String ready = properties.getProperty("ready", "");
        String traceLogId = properties.getProperty("tracelogid", "");
        String auditReason = properties.getProperty("auditreason", "");
        String auditSignedFlag = properties.getProperty("auditsignedflag", "N");
        String auditActivity = properties.getProperty("auditactivity", "");
        QueryProcessor qp = this.getQueryProcessor();
        StringBuilder sql = new StringBuilder();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(sdcId);
        boolean tripleKey = sdcPropertyList.getProperty("keycolumns").equals("3");
        DataSet ds = null;
        String rsetid = "";
        try {
            rsetid = this.getDAMProcessor().createRSet(sdcId, keyId1, keyId2, keyId3);
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select sdiapprovalstep.approvalflag from sdiapproval, sdiapprovalstep, rsetitems r where sdiapproval.sdcid = sdiapprovalstep.sdcid and sdiapproval.keyid1 = sdiapprovalstep.keyid1 ").append(" and sdiapproval.keyid2 = sdiapprovalstep.keyid2 and sdiapproval.keyid3 = sdiapprovalstep.keyid3 and sdiapproval.approvaltypeid = sdiapprovalstep.approvaltypeid and sdiapproval.approvalfunction = 'Versioned' ").append(" and sdiapprovalstep.sdcid = r.sdcid and sdiapprovalstep.keyid1 = r.keyid1 and sdiapprovalstep.keyid2 = r.keyid2 ");
            if (tripleKey) {
                sql.append("and sdiapprovalstep.keyid3 = r.keyid3");
            }
            sql.append(" and r.rsetid = ").append(safeSQL.addVar(rsetid));
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        catch (SapphireException e) {
            this.logger.info("ResetVersionApproval", "Error finiding sdiapprovals.");
        }
        finally {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (ds != null && ds.getRowCount() > 0) {
            if (!sdcProcessor.getProperty(sdcId, "auditedflag").equalsIgnoreCase("N") && traceLogId.length() == 0 && auditReason.length() > 0) {
                this.logger.info("Generate the tracelog records");
                String promptflag = sdcProcessor.getProperty(sdcId, "auditpromptflag");
                String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
                AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                try {
                    traceLogId = audit.addSDITraceLogEntry(sdcId, keyId1, keyId2, keyId3, auditReason, auditActivity, auditSignedFlag, properties.getProperty("auditdt"), "Data editing", standard.equals("Y"));
                    properties.setProperty("tracelogid", traceLogId);
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to add audit records", e);
                }
            }
            PropertyList propsResetApproval = new PropertyList();
            propsResetApproval.setProperty("sdcid", sdcId);
            propsResetApproval.setProperty("keyid1", keyId1);
            propsResetApproval.setProperty("keyid2", keyId2);
            propsResetApproval.setProperty("keyid3", keyId3);
            propsResetApproval.setProperty("approvalfunction", approvalFunction);
            propsResetApproval.setProperty("approvaltypeid", approvalTypeId);
            propsResetApproval.setProperty("ready", ready);
            propsResetApproval.setProperty("tracelogid", traceLogId);
            this.getActionProcessor().processAction("ResetSDIApproval", "1", propsResetApproval);
        }
        PropertyList propsEditSDI = new PropertyList();
        propsEditSDI.setProperty("sdcid", sdcId);
        propsEditSDI.setProperty("keyid1", keyId1);
        propsEditSDI.setProperty("keyid2", keyId2);
        propsEditSDI.setProperty("keyid3", keyId3);
        propsEditSDI.setProperty("versionapproveddt", "(null)");
        propsEditSDI.setProperty("tracelogid", traceLogId);
        this.getActionProcessor().processAction("EditSDI", "1", propsEditSDI);
    }
}

