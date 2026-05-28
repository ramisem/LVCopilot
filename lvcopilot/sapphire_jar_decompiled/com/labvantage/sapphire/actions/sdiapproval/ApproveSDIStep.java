/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdiapproval;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleEvaluator;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ApproveSDIStep
extends BaseAction
implements sapphire.action.ApproveSDIStep {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean singleKey;
        String delimeter = properties.getProperty("separator", properties.getProperty("delimeter", ";"));
        if (properties.getProperty("sdcid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified.");
        }
        if (properties.getProperty("keyid1").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Keyid1 specified.");
        }
        if (properties.getProperty("approvaltypeid").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Approval Type specified.");
        }
        if (properties.getProperty("approvalstep").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Approval Step specified.");
        }
        if (properties.getProperty("approvalstepinstance").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Approval Step Instance specified.");
        }
        if (properties.getProperty("approvalflag").length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No Approval Step Flag specified.");
        }
        String sdcid = properties.getProperty("sdcid");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcProps = sdcProcessor.getPropertyList(sdcid);
        sdcid = sdcProps.getProperty("sdcid");
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        String effectivitydateflag = sdcProps.getProperty("versionuseeffectivedtflag");
        String qsreviewoption = properties.getProperty("qsreviewoption", "");
        DataSet props = new DataSet();
        props.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), delimeter);
        props.addColumnValues("keyid2", 0, properties.getProperty("keyid2"), delimeter, "(null)");
        props.addColumnValues("keyid3", 0, properties.getProperty("keyid3"), delimeter, "(null)");
        props.addColumnValues("approvaltypeid", 0, properties.getProperty("approvaltypeid"), delimeter);
        props.addColumnValues("approvalstep", 0, properties.getProperty("approvalstep"), delimeter);
        props.addColumnValues("approvalstepinstance", 1, properties.getProperty("approvalstepinstance"), delimeter, "1");
        props.addColumnValues("approvalflag", 0, properties.getProperty("approvalflag"), delimeter);
        props.addColumnValues("auditreason", 0, properties.getProperty("auditreason"), delimeter);
        props.addColumnValues("auditactivity", 0, properties.getProperty("auditactivity", ""), delimeter);
        props.addColumnValues("auditsignedflag", 0, properties.getProperty("auditsignedflag", "N"), delimeter);
        props.padColumns();
        props.addColumnValues("approvalnote", 0, properties.getProperty("approvalnote"), delimeter);
        props.addColumnValues("rejectionreason", 0, properties.getProperty("rejectionreason"), delimeter);
        Calendar now = DateTimeUtil.getNowCalendar();
        AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        String update = "UPDATE sdiapprovalstep SET approvalflag = ?, notes = ?, rejectionreason=?, modby=?, moddt=?, modtool=?, reviewedby=?, revieweddt=?, tracelogid=? WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=? and approvaltypeid=? and approvalstep=? and approvalstepinstance=?";
        PreparedStatement ps = this.database.prepareStatement(update);
        HashMap<String, String> tracelogCache = new HashMap<String, String>();
        try {
            for (int i = 0; i < props.size(); ++i) {
                int tracelogid;
                String keyid1 = props.getValue(i, "keyid1");
                String keyid2 = props.getValue(i, "keyid2");
                String keyid3 = props.getValue(i, "keyid3");
                String approvaltypeid = props.getValue(i, "approvaltypeid");
                String string = props.getValue(i, "approvalstep");
                int approvalstepinstance = props.getInt(i, "approvalstepinstance");
                String approvalFlag = props.getValue(i, "approvalflag");
                String approvalNote = props.getValue(i, "approvalnote");
                String rejectionReason = props.getValue(i, "rejectionreason", "");
                String auditReason = props.getValue(i, "auditreason");
                String auditActivity = props.getValue(i, "auditactivity");
                String auditSignedFlag = props.getValue(i, "auditsignedflag", "N");
                if (tracelogCache.containsKey(auditReason)) {
                    tracelogid = Integer.parseInt(tracelogCache.get(auditReason).toString());
                } else {
                    try {
                        String desc = "SDI Approval: Type=" + approvaltypeid + " Step=" + string + "[" + approvalstepinstance + "]";
                        if (desc.length() > 80) {
                            desc = desc.substring(77) + "..";
                        }
                        tracelogid = properties.getProperty("tracelogid", "").trim().length() == 0 ? Integer.parseInt(audit.addSDITraceLogEntry(sdcid, keyid1, keyid2, keyid3, auditReason, auditActivity, auditSignedFlag, "N", desc, true)) : Integer.parseInt(properties.getProperty("tracelogid"));
                        tracelogCache.put(auditReason, String.valueOf(tracelogid));
                        props.setString(-1, "tracelogid", String.valueOf(tracelogid));
                    }
                    catch (ServiceException e) {
                        throw new SapphireException("Failed to add audit records", e);
                    }
                }
                ps.setString(1, approvalFlag);
                ps.setString(2, approvalNote);
                ps.setString(3, rejectionReason);
                ps.setString(4, this.connectionInfo.getSysuserId());
                ps.setTimestamp(5, new Timestamp(now.getTime().getTime()));
                ps.setString(6, this.connectionInfo.getTool());
                ps.setString(7, "(system)".equals(this.connectionInfo.getSysuserId()) ? null : this.connectionInfo.getSysuserId());
                ps.setTimestamp(8, new Timestamp(now.getTime().getTime()));
                ps.setLong(9, tracelogid);
                ps.setString(10, sdcid);
                ps.setString(11, keyid1);
                ps.setString(12, keyid2);
                ps.setString(13, keyid3);
                ps.setString(14, approvaltypeid);
                ps.setString(15, string);
                ps.setInt(16, approvalstepinstance);
                ps.execute();
            }
            this.database.closeStatement();
        }
        catch (SQLException e) {
            throw new SapphireException("Failef to update sdiapprovalstep. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        props.sort("keyid1, keyid2, keyid3, approvaltypeid");
        ArrayList<DataSet> types = props.getGroupedDataSets("keyid1, keyid2, keyid3, approvaltypeid");
        StringBuffer newStatusList = new StringBuffer();
        DataSet updateApproval = new DataSet();
        DataSet updateSDIApproveDt = new DataSet();
        for (DataSet dataSet : types) {
            int newRow;
            String keyid1 = dataSet.getValue(0, "keyid1");
            String keyid2 = dataSet.getValue(0, "keyid2");
            String keyid3 = dataSet.getValue(0, "keyid3");
            String approvaltypeid = dataSet.getValue(0, "approvaltypeid");
            String traceLogId = dataSet.getString(0, "tracelogid", "");
            boolean honorAllMandatoryApprovalSteps = true;
            try {
                honorAllMandatoryApprovalSteps = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("honorallmandatoryapprovalsteps", "Y").equals("Y");
            }
            catch (Exception e) {
                this.logger.error("Failed to retrive DataEntryPolicy->honorallmandatoryapprovalsteps property");
            }
            this.database.createPreparedResultSet("SELECT approvalflag, passrule, approvalfunction FROM sdiapproval WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=? and approvaltypeid=?", new String[]{sdcid, keyid1, keyid2, keyid3, approvaltypeid});
            if (!this.database.getNext()) continue;
            String currentStatus = this.database.getString("approvalflag");
            String passRule = this.database.getString("passrule");
            String function = this.database.getString("approvalfunction");
            ApprovalRuleEvaluator evaluator = new ApprovalRuleEvaluator();
            this.database.createPreparedResultSet("SELECT mandatoryflag, approvalflag FROM sdiapprovalstep WHERE sdcid=? and keyid1=? and keyid2=? and keyid3=? and approvaltypeid=?", new String[]{sdcid, keyid1, keyid2, keyid3, approvaltypeid});
            while (this.database.getNext()) {
                evaluator.addStep(this.database.getString("approvalflag"), this.database.getString("mandatoryflag"));
            }
            String newStatus = evaluator.evaluateRule(passRule, honorAllMandatoryApprovalSteps);
            newStatusList.append(";").append(newStatus);
            if (!newStatus.equals(currentStatus)) {
                newRow = updateApproval.addRow();
                updateApproval.setString(newRow, "sdcid", sdcid);
                updateApproval.setString(newRow, "keyid1", keyid1);
                updateApproval.setString(newRow, "keyid2", keyid2);
                updateApproval.setString(newRow, "keyid3", keyid3);
                updateApproval.setString(newRow, "approvaltypeid", approvaltypeid);
                updateApproval.setString(newRow, "approvalflag", newStatus);
                updateApproval.setString(newRow, "tracelogid", traceLogId);
                updateApproval.setString(newRow, "approvalfunction", function);
            }
            if (!"Versioned".equalsIgnoreCase(function) || !"P".equals(newStatus)) continue;
            newRow = updateSDIApproveDt.addRow();
            updateSDIApproveDt.setDate(newRow, "versionapproveddt", DateTimeUtil.getNowCalendar());
            updateSDIApproveDt.setString(newRow, keycolid1, keyid1);
            updateSDIApproveDt.setString(newRow, keycolid2, keyid2);
            updateSDIApproveDt.setString(newRow, keycolid3, keyid3);
        }
        if (updateApproval.size() > 0) {
            BaseSDCRules preSDCRule = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, sdcProps, "PreApprove");
            preSDCRule.preApprove(updateApproval);
            for (BaseSDCRules customRules : preSDCRule.getCustomRuleList()) {
                customRules.preApprove(updateApproval);
            }
            preSDCRule.endRule();
            DataSetUtil.update(this.database, updateApproval, "sdiapproval", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "approvaltypeid"});
            if (qsreviewoption.length() > 0) {
                updateApproval.setString(-1, "qsreviewoption", qsreviewoption);
            }
            BaseSDCRules baseSDCRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, sdcProps, "PreApprove");
            baseSDCRules.postApprove(updateApproval);
            for (BaseSDCRules customRules : baseSDCRules.getCustomRuleList()) {
                customRules.postApprove(updateApproval);
            }
            baseSDCRules.endRule();
        }
        if (updateSDIApproveDt.size() > 0 && !(singleKey = sdcProps.getProperty("keycolumns").equals("1"))) {
            PropertyList propertyList = new PropertyList();
            propertyList.setProperty("sdcid", sdcid);
            propertyList.setProperty("keyid1", updateSDIApproveDt.getColumnValues(keycolid1, ";"));
            propertyList.setProperty("keyid2", updateSDIApproveDt.getColumnValues(keycolid2, ";"));
            propertyList.setProperty("keyid3", updateSDIApproveDt.getColumnValues(keycolid3, ";"));
            propertyList.setProperty("versionstatus", StringUtil.repeat("C", updateSDIApproveDt.getRowCount(), ";"));
            propertyList.setProperty("auditreason", properties.getProperty("auditreason"));
            propertyList.setProperty("auditactivity", properties.getProperty("auditactivity", ";"));
            propertyList.setProperty("auditsignedflag", properties.getProperty("auditsignedflag", ";"));
            this.getActionProcessor().processAction("SetSDIVersionStatus", "1", propertyList);
        }
        if (newStatusList.length() > 0) {
            properties.setProperty("newapprovalflag", newStatusList.substring(1));
        }
    }
}

