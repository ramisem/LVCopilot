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
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddSDIApprovalStep
extends BaseAction
implements sapphire.action.AddSDIApprovalStep {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String delimeter = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        TranslationProcessor tp = this.getTranslationProcessor();
        if (properties.getProperty("sdcid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No SDC specified."));
        }
        if (properties.getProperty("keyid1").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No Keyid1 specified."));
        }
        if (properties.getProperty("approvaltypeid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No Approval Type specified."));
        }
        if (properties.getProperty("approvalstep").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", tp.translate("No Approval Step specified."));
        }
        boolean hasSteps = properties.getProperty("approvalstep").length() > 0;
        Calendar now = DateTimeUtil.getNowCalendar();
        String function = properties.getProperty("approvalfunction");
        DataSet props = new DataSet();
        props.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), delimeter);
        props.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), delimeter);
        props.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), delimeter, "(null)");
        props.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), delimeter, "(null)");
        props.addColumnValues("approvaltypeid", 0, properties.getProperty("approvaltypeid"), delimeter);
        props.addColumnValues("approvalstep", 0, properties.getProperty("approvalstep"), delimeter);
        String traceLogId = properties.getProperty("tracelogid");
        String auditReason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity");
        String auditSignedFlag = properties.getProperty("auditsignedflag");
        props.padColumns();
        props.addColumnValues("roleid", 0, properties.getProperty("roleid"), delimeter);
        props.addColumnValues("assignedto", 0, properties.getProperty("assignedto"), delimeter);
        props.addColumnValues("mandatoryflag", 0, properties.getProperty("mandatory"), delimeter);
        props.addColumnValues("forcepeerflag", 0, properties.getProperty("forcepeerflag"), delimeter);
        props.addColumnValues("usersequence", 1, properties.getProperty("usersequence"), delimeter);
        props.setDate(-1, "createdt", now);
        props.setString(-1, "createtool", this.connectionInfo.getTool());
        props.setString(-1, "createby", this.connectionInfo.getSysuserId());
        props.setDate(-1, "moddt", now);
        props.setString(-1, "modtool", this.connectionInfo.getTool());
        props.setString(-1, "modby", this.connectionInfo.getSysuserId());
        props.addColumn("approvalstepinstance", 1);
        HashSet<String> approvalTypeExists = new HashSet<String>();
        boolean ready = true;
        props.sort("approvaltypeid");
        DataSet stepInstance = new DataSet();
        HashMap<String, String> findStepMap = new HashMap<String, String>();
        for (int i = 0; i < props.size(); ++i) {
            String sdcid = props.getValue(i, "sdcid");
            String keyid1 = props.getValue(i, "keyid1");
            String keyid2 = props.getValue(i, "keyid2");
            String keyid3 = props.getValue(i, "keyid3");
            String approvaltypeid = props.getValue(i, "approvaltypeid");
            String approvalstep = props.getValue(i, "approvalstep");
            findStepMap.put("sdcid", sdcid);
            findStepMap.put("keyid1", keyid1);
            findStepMap.put("keyid2", keyid2);
            findStepMap.put("keyid3", keyid3);
            findStepMap.put("approvaltypeid", approvaltypeid);
            findStepMap.put("approvalstep", approvalstep);
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            if (!sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N")) {
                if (traceLogId.length() == 0 && auditReason.length() > 0) {
                    this.logger.info("Generate the tracelog records");
                    String promptflag = sdcProcessor.getProperty(sdcid, "auditpromptflag");
                    String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
                    AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    try {
                        traceLogId = audit.addSDITraceLogEntry(sdcid, keyid1, keyid2, keyid3, auditReason, auditActivity, auditSignedFlag, properties.getProperty("auditdt"), "Data editing", standard.equals("Y"));
                        properties.setProperty("tracelogid", traceLogId);
                    }
                    catch (ServiceException e) {
                        throw new SapphireException(tp.translate("Failed to add audit records"), e);
                    }
                }
                if (traceLogId.length() > 0) {
                    props.setString(i, "tracelogid", traceLogId);
                }
            }
            if (!approvalTypeExists.contains(approvaltypeid)) {
                this.database.createPreparedResultSet("SELECT approvalflag, approvalfunction FROM sdiapproval WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? AND approvaltypeid=?", new String[]{sdcid, keyid1, keyid2, keyid3, approvaltypeid});
                if (this.database.getNext()) {
                    String existingFunction = this.database.getString("approvalfunction");
                    if (function.length() > 0 && !function.equalsIgnoreCase(existingFunction)) {
                        HashMap<String, String> valueMap = new HashMap<String, String>();
                        valueMap.put("approvaltypeid", approvaltypeid);
                        throw new SapphireException("GENERAL_ERROR", tp.translate("Same ApprovalType '[approvaltypeid]' cannot exists in multiple functions of the SDI.", valueMap));
                    }
                    ready = !"N".equals(this.database.getString("approvalflag"));
                } else {
                    if (function.length() > 0) {
                        this.database.createPreparedResultSet("checksdiapproval", "select 1 from sdiapproval WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? and approvalfunction = ?", new String[]{sdcid, keyid1, keyid2, keyid3, function});
                        boolean sdiapprovalExists = this.database.getNext("checksdiapproval");
                        this.database.closeResultSet("checksdiapproval");
                        if (sdiapprovalExists) {
                            throw new SapphireException("GENERAL_ERROR", tp.translate("Multiple ApprovalTypes are not allowed for the same approval function.") + " ");
                        }
                    }
                    HashMap<String, String> addProps = new HashMap<String, String>();
                    addProps.put("sdcid", sdcid);
                    addProps.put("keyid1", keyid1);
                    addProps.put("keyid2", keyid2);
                    addProps.put("keyid3", keyid3);
                    addProps.put("approvaltypeid", approvaltypeid);
                    addProps.put("addsteps", "N");
                    addProps.put("ready", "N");
                    addProps.put("tracelogid", traceLogId);
                    addProps.put("auditreason", auditReason);
                    addProps.put("auditactivity", auditActivity);
                    addProps.put("auditsignedflag", auditSignedFlag);
                    addProps.put("approvalfunction", function);
                    this.getActionProcessor().processAction("AddSDIApproval", "1", addProps);
                    ready = false;
                }
                approvalTypeExists.add(approvaltypeid);
            }
            this.database.createPreparedResultSet("SELECT max( approvalstepinstance ) instance FROM sdiapprovalstep WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=? and approvaltypeid=? and approvalstep=?", new String[]{sdcid, keyid1, keyid2, keyid3, approvaltypeid, approvalstep});
            int maxInstance = 0;
            int findRow = -1;
            if (this.database.getNext()) {
                maxInstance = this.database.getInt("instance");
            }
            if ((findRow = stepInstance.findRow(findStepMap)) > -1) {
                maxInstance = stepInstance.getInt(findRow, "approvalstepinstance");
            }
            props.setNumber(i, "approvalstepinstance", ++maxInstance);
            props.setString(i, "approvalflag", ready ? "U" : "N");
            if (findRow < 0) {
                int r = stepInstance.addRow();
                stepInstance.setString(r, "sdcid", sdcid);
                stepInstance.setString(r, "keyid1", keyid1);
                stepInstance.setString(r, "keyid2", keyid2);
                stepInstance.setString(r, "keyid3", keyid3);
                stepInstance.setString(r, "approvaltypeid", approvaltypeid);
                stepInstance.setString(r, "approvalstep", approvalstep);
                stepInstance.setNumber(r, "approvalstepinstance", maxInstance);
                continue;
            }
            stepInstance.setNumber(findRow, "approvalstepinstance", maxInstance);
        }
        DataSetUtil.insert(this.database, props, "sdiapprovalstep");
    }
}

