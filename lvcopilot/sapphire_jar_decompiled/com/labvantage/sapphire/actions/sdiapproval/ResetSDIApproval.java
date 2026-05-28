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
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ResetSDIApproval
extends BaseAction
implements sapphire.action.ResetSDIApproval {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String delimeter = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        if (properties.getProperty("sdcid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified.");
        }
        if (properties.getProperty("keyid1").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Keyid1 specified.");
        }
        Calendar now = DateTimeUtil.getNowCalendar();
        DataSet props = new DataSet();
        props.addColumnValues("sdcid", 0, properties.getProperty("sdcid"), delimeter);
        props.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), delimeter);
        props.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), delimeter, "(null)");
        props.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), delimeter, "(null)");
        String approvalFunction = properties.getProperty("approvalfunction", "");
        String sql = "SELECT approvaltypeid FROM sdiapproval WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? ";
        if (approvalFunction.length() > 0) {
            sql = sql + " AND approvalfunction = '" + approvalFunction + "'";
        }
        if (properties.getProperty("approvaltypeid", "").length() == 0) {
            StringBuffer sbSdcid = new StringBuffer();
            StringBuffer sbKeyid1 = new StringBuffer();
            StringBuffer sbKeyid2 = new StringBuffer();
            StringBuffer sbKeyid3 = new StringBuffer();
            StringBuffer sbAprTypeIds = new StringBuffer();
            for (int i = 0; i < props.size(); ++i) {
                String sdcid = props.getValue(i, "sdcid", props.getValue(0, "sdcid"));
                String keyid1 = props.getValue(i, "keyid1");
                String keyid2 = props.getValue(i, "keyid2", "(null)");
                String keyid3 = props.getValue(i, "keyid3", "(null)");
                this.database.createPreparedResultSet(sql, new String[]{sdcid, keyid1, keyid2, keyid3});
                while (this.database.getNext()) {
                    sbSdcid.append(sdcid).append(";");
                    sbKeyid1.append(keyid1).append(";");
                    sbKeyid2.append(keyid2).append(";");
                    sbKeyid3.append(keyid3).append(";");
                    sbAprTypeIds.append(this.database.getString("approvaltypeid")).append(";");
                }
            }
            props.reset();
            if (sbAprTypeIds.length() > 0) {
                sbSdcid.setLength(sbSdcid.length() - 1);
                sbKeyid1.setLength(sbKeyid1.length() - 1);
                sbKeyid2.setLength(sbKeyid2.length() - 1);
                sbKeyid3.setLength(sbKeyid3.length() - 1);
                sbAprTypeIds.setLength(sbAprTypeIds.length() - 1);
                props.addColumnValues("sdcid", 0, sbSdcid.toString(), ";");
                props.addColumnValues("keyid1", 0, sbKeyid1.toString(), ";");
                props.addColumnValues("keyid2", 0, sbKeyid2.toString(), ";", "(null)");
                props.addColumnValues("keyid3", 0, sbKeyid3.toString(), ";", "(null)");
                props.addColumnValues("approvaltypeid", 0, sbAprTypeIds.toString(), ";");
            }
        } else {
            props.addColumnValues("approvaltypeid", 0, properties.getProperty("approvaltypeid"), delimeter);
        }
        if (props.size() > 0) {
            props.padColumns();
            String traceLogId = properties.getProperty("tracelogid", "");
            String auditReason = properties.getProperty("auditreason");
            String auditActivity = properties.getProperty("auditactivity");
            String auditSignedFlag = properties.getProperty("auditsignedflag");
            String sdcid = StringUtil.split(properties.getProperty("sdcid", ""), ";")[0];
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            if (!sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N") && traceLogId.length() == 0 && auditReason.length() > 0) {
                this.logger.info("Generate the tracelog records");
                String promptflag = sdcProcessor.getProperty(sdcid, "auditpromptflag");
                String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
                AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                try {
                    traceLogId = audit.addSDITraceLogEntry(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), auditReason, auditActivity, auditSignedFlag, properties.getProperty("auditdt"), "Data editing", standard.equals("Y"));
                    properties.setProperty("tracelogid", traceLogId);
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to add audit records", e);
                }
            }
            props.setString(-1, "tracelogid", traceLogId);
            boolean ready = properties.getProperty("ready", "Y").equals("Y");
            props.setString(-1, "approvalflag", ready ? "U" : "N");
            props.setDate(-1, "moddt", now);
            props.setString(-1, "modtool", this.connectionInfo.getTool());
            props.setString(-1, "modby", this.connectionInfo.getSysuserId());
            DataSetUtil.update(this.database, props, "sdiapproval", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "approvaltypeid"});
            props.setString(-1, "notes", null);
            props.setString(-1, "rejectionreason", null);
            props.setString(-1, "reviewedby", null);
            props.setDate(-1, "revieweddt", (Calendar)null);
            DataSetUtil.update(this.database, props, "sdiapprovalstep", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "approvaltypeid"});
        }
    }
}

