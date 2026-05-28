/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleUtil;
import com.labvantage.sapphire.actions.sdiapproval.ResetSDIApproval;
import com.labvantage.sapphire.actions.sdiapproval.SubmitSDIForApproval;
import com.labvantage.sapphire.admin.ddt.misc.WhoDoneIt;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LV_MonitorGroup
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90138 $";
    public static final String SDCID = "LV_MonitorGroup";
    public static final String TABLEID = "monitorgroup";
    public static final String COLUMN_MONITORGROUPSTATUS = "monitorgroupstatus";
    public static final String COLUMN_MONITORGROUPID = "monitorgroupid";
    public static final String COLUMN_RELEASEDDT = "releaseddt";
    public static final String COLUMN_RELEASEDBY = "releasedby";
    public static final String COLUMN_MONITORGROUPTYPE = "monitorgrouptype";
    public static final String STATUS_INITIAL = "Initial";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_COLLECTED = "Collected";
    public static final String STATUS_RECEIVED = "Received";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_PENDINGRELEASE = "Pending Release";
    public static final String STATUS_RELEASED = "Released";
    public static final String STATUS_REJECTED = "Rejected";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String MONITORGROUPTYPE_ADHOC = "AdHoc";
    public static final String POLICY_MONITORGROUPPOLICY = "MonitorGroupPolicy";
    public static final String PENDINGRELEASERULE_LASTSAMPLECREATION = "LSC";
    public static final String PENDINGRELEASERULE_AFTERENDDATE = "ED";
    private static WhoDoneIt whoDoneIt = new WhoDoneIt();

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (!actionProps.getProperty("templateflag").equalsIgnoreCase("Y")) {
            this.setStatus(STATUS_INITIAL, primary);
        }
    }

    private void setStatus(String status, DataSet primary) {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            primary.setString(i, COLUMN_MONITORGROUPSTATUS, status);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        whoDoneIt.process(primary, this);
    }

    private void processPendingRelease(DataSet primary) throws SapphireException {
        StringBuilder toBeSubmittedForApproval = new StringBuilder();
        StringBuilder toBeAutoReleased = new StringBuilder();
        StringBuilder releasedBuffer = new StringBuilder();
        StringBuilder toBeUnReleased = new StringBuilder();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            boolean doAutoRelease;
            if (!primary.isValidColumn(COLUMN_MONITORGROUPSTATUS) || !this.hasPrimaryValueChanged(primary, i, COLUMN_MONITORGROUPSTATUS)) continue;
            String monitorGroupStatus = primary.getValue(i, COLUMN_MONITORGROUPSTATUS, "");
            String monitorGroupOldStatus = this.getOldPrimaryValue(primary, i, COLUMN_MONITORGROUPSTATUS);
            String monitorGroupType = primary.getValue(i, COLUMN_MONITORGROUPTYPE, this.getOldPrimaryValue(primary, i, COLUMN_MONITORGROUPTYPE));
            if (monitorGroupType == null || monitorGroupType.length() <= 0) continue;
            String monitorGroupId = primary.getValue(i, COLUMN_MONITORGROUPID, "");
            PropertyList monitorGroupTypeSettings = LV_MonitorGroup.getMonitorGroupTypeSettings(monitorGroupType, this.getConfigurationProcessor());
            if (monitorGroupStatus.equals(STATUS_COMPLETED)) {
                String pendingReleaseRule = monitorGroupTypeSettings.getProperty("pendingreleaserule", PENDINGRELEASERULE_AFTERENDDATE);
                if (PENDINGRELEASERULE_LASTSAMPLECREATION.equals(pendingReleaseRule)) {
                    toBeSubmittedForApproval.append(";").append(monitorGroupId);
                }
            } else if (monitorGroupStatus.equals(STATUS_PENDINGRELEASE) && (doAutoRelease = monitorGroupTypeSettings.getProperty("autorelease", "N").equals("Y")) && LV_MonitorGroup.isEligibleForAutoRelease(monitorGroupId, this.getQueryProcessor(), this.getConfigurationProcessor(), true)) {
                toBeAutoReleased.append(";").append(monitorGroupId);
                releasedBuffer.append(";").append(STATUS_RELEASED);
            }
            if (!STATUS_RELEASED.equals(monitorGroupOldStatus) && !STATUS_REJECTED.equals(monitorGroupOldStatus) || toBeUnReleased.indexOf(monitorGroupId) >= 0) continue;
            toBeUnReleased.append(";").append(monitorGroupId);
        }
        if (toBeUnReleased.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDCID);
            props.setProperty("keyid1", toBeUnReleased.substring(1));
            props.setProperty("ready", "N");
            this.getActionProcessor().processAction("ResetSDIApproval", "1", props);
            this.getActionProcessor().processActionClass(ResetSDIApproval.class.getName(), props);
        }
        if (toBeSubmittedForApproval.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDCID);
            props.setProperty("keyid1", toBeSubmittedForApproval.substring(1));
            props.setProperty("pendingapprovalstatus", STATUS_PENDINGRELEASE);
            props.setProperty("approvalstatus", STATUS_RELEASED);
            props.setProperty("approvalstatuscolumn", COLUMN_MONITORGROUPSTATUS);
            this.getActionProcessor().processActionClass(SubmitSDIForApproval.class.getName(), props);
        }
        if (toBeAutoReleased.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", SDCID);
            props.setProperty("keyid1", toBeAutoReleased.substring(1));
            props.setProperty(COLUMN_MONITORGROUPSTATUS, releasedBuffer.substring(1));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.cancelMonitorSamples(primary);
        this.processPendingRelease(primary);
        this.findCandidateForAutoRelease(primary);
        ApprovalRuleUtil.checkSDIAutoApprovalRule(primary, actionProps, SDCID, COLUMN_MONITORGROUPSTATUS, STATUS_PENDINGRELEASE, STATUS_RELEASED, COLUMN_RELEASEDBY, COLUMN_RELEASEDDT, this.database, this.connectionInfo, this.logger, this.getSDCProcessor(), true);
    }

    private void cancelMonitorSamples(DataSet primary) throws SapphireException {
        StringBuffer pendingMonitorGroupSamples = new StringBuffer();
        try {
            String sql = "select s_sampleid from s_sample where monitorgroupid=? and (samplestatus in ('Initial','Received','InProgress') or (samplestatus='Completed' AND reviewrequiredflag='Y'))";
            PreparedStatement monitorGroupSamplesStatement = this.database.prepareStatement("monitorgroupsamples", sql);
            for (int i = 0; i < primary.size(); ++i) {
                String monitorgroupid = primary.getString(i, COLUMN_MONITORGROUPID, "");
                String oldMonitorGroupStatus = this.getOldPrimaryValue(primary, i, COLUMN_MONITORGROUPSTATUS);
                if (!this.hasPrimaryValueChanged(primary, i, COLUMN_MONITORGROUPSTATUS) || !primary.getString(i, COLUMN_MONITORGROUPSTATUS, "").equalsIgnoreCase(STATUS_CANCELLED)) continue;
                if (!oldMonitorGroupStatus.equals(STATUS_RELEASED) && !oldMonitorGroupStatus.equals(STATUS_REJECTED)) {
                    monitorGroupSamplesStatement.setString(1, monitorgroupid);
                    DataSet monitorGroupSamples = new DataSet(monitorGroupSamplesStatement.executeQuery());
                    if (monitorGroupSamples.size() <= 0) continue;
                    pendingMonitorGroupSamples.append(";").append(monitorGroupSamples.getColumnValues("s_sampleid", ";"));
                    continue;
                }
                throw new SapphireException(this.getTranslationProcessor().translate("Cannot Cancel a Released/Rejected monitor-group"));
            }
            if (pendingMonitorGroupSamples.length() > 0) {
                ActionProcessor ap = this.getActionProcessor();
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", pendingMonitorGroupSamples.substring(1));
                props.setProperty("samplestatus", STATUS_CANCELLED);
                props.setProperty("statusrollup", "false");
                ap.processAction("EditSDI", "1", props);
            }
        }
        catch (SQLException e) {
            this.logger.error("Failed to retrieve monitorgroupsamples data:", e);
        }
    }

    public static boolean hasAllTestingFinished(String monitorGroupId, QueryProcessor qp) {
        StringBuffer sql = new StringBuffer("SELECT COUNT(*) samplesingroup, ");
        sql.append(" SUM(CASE ");
        sql.append(" WHEN s.samplestatus = '").append(STATUS_COMPLETED).append("' AND (s.reviewrequiredflag='N' OR s.reviewrequiredflag IS NULL) THEN 1 ");
        sql.append(" WHEN s.disposalstatus='Retained' OR s.samplestatus IN ('").append("Reviewed").append("', '").append("Reported").append("', '").append(STATUS_CANCELLED).append("', '").append("Disposed").append("') THEN 1");
        sql.append(" ELSE 0 ");
        sql.append(" END) numberoffinishedsamples ");
        sql.append(" FROM monitorgroup monitorgroup, s_sample s ");
        sql.append(" WHERE monitorgroup.monitorgroupid = ? ");
        sql.append(" AND s.monitorgroupid = monitorgroup.monitorgroupid ");
        sql.append(" GROUP BY monitorgroup.monitorgroupid");
        DataSet pendingRelease = qp.getPreparedSqlDataSet(sql.toString(), new Object[]{monitorGroupId});
        int numberOfFinishedSamples = pendingRelease.getInt(0, "numberoffinishedsamples");
        int samplesInGroup = pendingRelease.getInt(0, "samplesingroup");
        return numberOfFinishedSamples == samplesInGroup;
    }

    public static boolean isEligibleForAutoRelease(String monitorGroupId, QueryProcessor qp, ConfigurationProcessor cp, boolean doPreparedStatement) throws SapphireException {
        StringBuilder sql = new StringBuilder("SELECT condition FROM sdispec sp, s_sample s, ").append(TABLEID).append(" m");
        sql.append(" WHERE m.").append(COLUMN_MONITORGROUPID).append(" = ");
        sql.append("?");
        sql.append(" AND m.").append(COLUMN_MONITORGROUPSTATUS).append(" = '").append(STATUS_PENDINGRELEASE).append("'");
        sql.append(" AND s.").append(COLUMN_MONITORGROUPID).append(" = m.").append(COLUMN_MONITORGROUPID);
        sql.append(" AND sp.keyid1 = s.").append("s_sampleid");
        sql.append(" AND sp.sdcid = '").append("Sample").append("'");
        DataSet specs = qp.getPreparedSqlDataSet("GetSpecForMonitorGroupSamples", sql.toString(), new String[]{monitorGroupId});
        HashMap<String, String> filter = new HashMap<String, String>();
        PropertyListCollection specInterpretation = cp.getPolicy("DataEntryPolicy", "Sapphire Custom").getCollection("SpecConditions");
        Iterator iter = specInterpretation.iterator();
        String passCondition = "Pass";
        while (iter.hasNext()) {
            PropertyList condition = (PropertyList)iter.next();
            if (!condition.getProperty("interpretation").equals(passCondition)) continue;
            passCondition = condition.getProperty("SpecCond");
            break;
        }
        filter.put("condition", passCondition);
        DataSet passedSpecs = specs.getFilteredDataSet(filter);
        return specs.getRowCount() == passedSpecs.getRowCount() && specs.getRowCount() > 0;
    }

    @Override
    public void postApprove(DataSet dsApproval) {
        try {
            DataSet approvedDS = ApprovalUtil.getSDIApprovalFlags(this.database, dsApproval);
            DataSet dsProp = new DataSet();
            for (int i = 0; i < approvedDS.size(); ++i) {
                int newRow = dsProp.addRow();
                String approvalFlag = approvedDS.getValue(i, "approvalflag");
                String monitorGroupStatus = "Pass".equalsIgnoreCase(approvalFlag) ? STATUS_RELEASED : STATUS_REJECTED;
                dsProp.setString(newRow, "keyid1", approvedDS.getValue(i, "keyid1"));
                dsProp.setString(newRow, COLUMN_MONITORGROUPSTATUS, monitorGroupStatus);
            }
            if (dsProp.size() > 0) {
                PropertyList props = new PropertyList();
                props.put("sdcid", SDCID);
                props.put("keyid1", dsProp.getColumnValues("keyid1", ";"));
                props.put(COLUMN_MONITORGROUPSTATUS, dsProp.getColumnValues(COLUMN_MONITORGROUPSTATUS, ";"));
                props.put("tracelogid", dsApproval.getString(0, "tracelogid", ""));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
        catch (Exception e) {
            Logger.logInfo("Exception occured in post approve rule :" + e.getMessage());
        }
    }

    public static PropertyList getMonitorGroupTypeSettings(String monitorGroupType, ConfigurationProcessor cp) throws SapphireException {
        PropertyList monitorGroupPolicy = cp.getPolicy(POLICY_MONITORGROUPPOLICY, "Sapphire Custom");
        PropertyListCollection monitorGroupTypes = monitorGroupPolicy.getCollectionNotNull("monitorgrouptypecollection");
        for (int i = 0; i < monitorGroupTypes.size(); ++i) {
            PropertyList monitorGroupTypeProperties = monitorGroupTypes.getPropertyList(i);
            if (!monitorGroupTypeProperties.getProperty("type").equals(monitorGroupType)) continue;
            return monitorGroupTypeProperties;
        }
        return null;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private void findCandidateForAutoRelease(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            PropertyList monitorGroupTypeSettings;
            String monitorGroupType;
            String monitorGroupStatus;
            if (!primary.isValidColumn(COLUMN_MONITORGROUPSTATUS) || !(monitorGroupStatus = primary.getValue(i, COLUMN_MONITORGROUPSTATUS, "")).equals(STATUS_PENDINGRELEASE) || (monitorGroupType = primary.getValue(i, COLUMN_MONITORGROUPTYPE, this.getOldPrimaryValue(primary, i, COLUMN_MONITORGROUPTYPE))) == null || monitorGroupType.length() <= 0 || (monitorGroupTypeSettings = LV_MonitorGroup.getMonitorGroupTypeSettings(monitorGroupType, this.getConfigurationProcessor())) == null || !monitorGroupTypeSettings.getProperty("autorelease", "N").equals("Y")) continue;
            primary.setValue(i, "skipapprovalrulecheck", "Y");
        }
    }

    static {
        whoDoneIt.addColumnPair(COLUMN_RELEASEDDT, null, COLUMN_MONITORGROUPSTATUS, "=", STATUS_RELEASED);
    }
}

