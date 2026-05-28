/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDIApproval
extends BaseAction
implements sapphire.action.AddSDIApproval {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "SDC not specified.");
        }
        String keyid1 = properties.getProperty("keyid1");
        if (keyid1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Keyid1 not specified.");
        }
        String keyid2 = properties.getProperty("keyid2", "(null)");
        String keyid3 = properties.getProperty("keyid3", "(null)");
        String approvalTypeIds = properties.getProperty("approvaltypeid", "");
        if (approvalTypeIds.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Approval Type specified.");
        }
        String traceLogId = properties.getProperty("tracelogid", "");
        String auditReason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity");
        String auditSignedFlag = properties.getProperty("auditsignedflag");
        String approvalFunction = properties.getProperty("approvalfunction");
        String createtool = properties.getProperty("createtool", this.connectionInfo.getTool());
        String modtool = properties.getProperty("modtool", this.connectionInfo.getTool());
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        if (!sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N") && traceLogId.length() == 0 && auditReason.length() > 0) {
            this.logger.info("Generate the tracelog records");
            String promptflag = sdcProcessor.getProperty(sdcid, "auditpromptflag");
            String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
            AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            try {
                traceLogId = audit.addSDITraceLogEntry(sdcid, keyid1, keyid2, keyid3, auditReason, auditActivity, auditSignedFlag, properties.getProperty("auditdt"), "Data editing", standard.equals("Y"));
                properties.setProperty("tracelogid", traceLogId);
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add audit records", e);
            }
        }
        boolean ready = properties.getProperty("ready", "Y").equals("Y");
        String delim = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        String[] arrApprovalTypes = StringUtil.split(approvalTypeIds, delim);
        boolean addSteps = properties.getProperty("addsteps", "Y").equals("Y");
        StringBuffer approvalTypeList = new StringBuffer();
        for (int i = 0; i < arrApprovalTypes.length; ++i) {
            approvalTypeList.append(arrApprovalTypes[i] + "','");
        }
        try {
            if (approvalTypeList.length() > 0) {
                approvalTypeList.setLength(approvalTypeList.length() - 3);
                SafeSQL safeSQL = new SafeSQL();
                String approvalTypes = "SELECT approvaltypeid, sequenceflag, passrule, uniquenessflag, extendableflag FROM approvaltype WHERE approvaltypeid IN  (" + safeSQL.addIn(approvalTypeList.toString()) + ")";
                DataSet approvalType = this.getQueryProcessor().getPreparedSqlDataSet(approvalTypes, safeSQL.getValues());
                Calendar now = DateTimeUtil.getNowCalendar();
                if (approvalType != null && approvalType.size() > 0) {
                    approvalType.setString(-1, "sdcid", sdcid);
                    approvalType.setString(-1, "keyid1", keyid1);
                    approvalType.setString(-1, "keyid2", keyid2);
                    approvalType.setString(-1, "keyid3", keyid3);
                    approvalType.setDate(-1, "createdt", now);
                    approvalType.setString(-1, "createtool", createtool);
                    approvalType.setString(-1, "createby", this.connectionInfo.getSysuserId());
                    approvalType.setDate(-1, "moddt", now);
                    approvalType.setString(-1, "modtool", modtool);
                    approvalType.setString(-1, "modby", this.connectionInfo.getSysuserId());
                    approvalType.setString(-1, "approvalflag", ready ? "U" : "N");
                    approvalType.setString(-1, "tracelogid", traceLogId);
                    approvalType.setString(-1, "approvalfunction", approvalFunction);
                    DataSetUtil.insert(this.database, approvalType, "sdiapproval");
                    if (addSteps) {
                        safeSQL.reset();
                        String approvalTypeSteps = "SELECT approvaltypeid, approvalstep, roleid, mandatoryflag, usersequence, forcepeerflag FROM approvaltypestep WHERE approvaltypeid in (" + safeSQL.addIn(approvalTypeList.toString()) + ")";
                        DataSet approvalSteps = this.getQueryProcessor().getPreparedSqlDataSet(approvalTypeSteps.toString(), safeSQL.getValues());
                        if (approvalSteps != null && approvalSteps.size() > 0) {
                            approvalSteps.setString(-1, "sdcid", sdcid);
                            approvalSteps.setString(-1, "keyid1", keyid1);
                            approvalSteps.setString(-1, "keyid2", keyid2);
                            approvalSteps.setString(-1, "keyid3", keyid3);
                            approvalSteps.setNumber(-1, "approvalstepinstance", 1);
                            approvalSteps.setDate(-1, "createdt", now);
                            approvalSteps.setString(-1, "createtool", createtool);
                            approvalSteps.setString(-1, "createby", this.connectionInfo.getSysuserId());
                            approvalSteps.setDate(-1, "moddt", now);
                            approvalSteps.setString(-1, "modtool", modtool);
                            approvalSteps.setString(-1, "modby", this.connectionInfo.getSysuserId());
                            approvalSteps.setString(-1, "approvalflag", ready ? "U" : "N");
                            approvalSteps.setString(-1, "tracelogid", traceLogId);
                            DataSetUtil.insert(this.database, approvalSteps, "sdiapprovalstep");
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", "Failed to add approval type(s) to the SDI : AddSDIApproval", e);
        }
    }
}

