/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.LV_ChangeRequest;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_ChangeLog
extends BaseSDCRules {
    public static final String SDC = "LV_ChangeLog";
    public static final String STATUS_CHECKED_OUT = "Checked Out";
    public static final String STATUS_CHECKED_IN = "Checked In";
    public static final String STATUS_DELETED = "Deleted";
    public static final String STATUS_RENAMED = "Renamed";
    public static final String STATUS_ROLLED_BACK = "Rolled Back";
    public static final String STATUS_CHECKOUT_ROLLED_BACK = "CheckOut Rolledback";
    public static final String STATUS_CHECKOUT_ABORTED = "CheckOut Aborted";
    public static final String PROPERTY_BYPASS_ISSUE_COMMUNICATION = "bypassissuecommunication";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String linkkeyid3;
            String linkkeyid2 = primary.getString(i, "linkkeyid2");
            if (OpalUtil.isEmpty(linkkeyid2)) {
                primary.setString(i, "linkkeyid2", "(null)");
            }
            if (!OpalUtil.isEmpty(linkkeyid3 = primary.getString(i, "linkkeyid3"))) continue;
            primary.setString(i, "linkkeyid3", "(null)");
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        ArrayList<String> inProgressRequestList = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            String changerequestid = primary.getString(i, "changerequestid", "");
            if (changerequestid.length() <= 0) continue;
            String changeRequestStatus = OpalUtil.getColumnValue(this.getQueryProcessor(), "changerequest", "changerequeststatus", "changerequestid=?", new String[]{changerequestid});
            if (changeRequestStatus.equals("Accepted") || changeRequestStatus.equals("In Progress") || changeRequestStatus.equals("Completed")) {
                if ("In Progress".equals(changeRequestStatus)) continue;
                inProgressRequestList.add(changerequestid);
                continue;
            }
            throw new SapphireException("LV_ChangeLog.preAdd", "VALIDATION", this.getTranslationProcessor().translate("Change Logs can only be added on Accepted, In Progress or Completed Change Requests."));
        }
        if (inProgressRequestList.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_ChangeRequest");
            props.setProperty("keyid1", OpalUtil.toDelimitedString(inProgressRequestList, ";"));
            props.setProperty("changerequeststatus", "In Progress");
            if (!"(system)".equalsIgnoreCase(this.connectionInfo.getSysuserId())) {
                props.setProperty("startedby", this.connectionInfo.getSysuserId());
            }
            props.setProperty("starteddt", "n");
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
            props.setProperty("auditdt", actionProps.getProperty("auditdt"));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        if (actionProps.getProperty(PROPERTY_BYPASS_ISSUE_COMMUNICATION, "N").equals("N") && this.isIssueRepositoryCommunicationEnabled(false)) {
            this.updateChangeRequestToIssueRepository(sdiData);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList list) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        String sysuserdepartmentlist = ";" + this.getConnectionProcessor().getSapphireConnection().getDepartmentList() + ";";
        ArrayList<String> editDeniedList = new ArrayList<String>();
        HashSet<String> changeRequestModifiedSet = new HashSet<String>();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "changerequestid")) continue;
            changeRequestModifiedSet.add(primary.getString(i, "changerequestid", ""));
            String changelogid = primary.getString(i, "changelogid", "");
            String changelogstatus = this.getOldPrimaryValue(primary, i, "changelogstatus");
            String checkedoutbyuserid = this.getOldPrimaryValue(primary, i, "checkedoutbyuserid");
            String checkedoutbydepartmentid = this.getOldPrimaryValue(primary, i, "checkedoutbydepartmentid");
            if (this.canUserEditChangeLog(sysuserid, sysuserdepartmentlist, changelogstatus, checkedoutbyuserid, checkedoutbydepartmentid)) continue;
            editDeniedList.add(changelogid);
        }
        if (editDeniedList.size() > 0) {
            this.setError(this.getTranslationProcessor().translate("Unauthorized Operation"), "VALIDATION", this.getTranslationProcessor().translate("User is not allowed to modify Change Request on Change Logs") + " (" + OpalUtil.toDelimitedString(editDeniedList, ", ") + ")");
        }
        if (changeRequestModifiedSet.size() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select changerequestid, changerequeststatus from changerequest where changerequestid in (" + safeSQL.addIn(changeRequestModifiedSet) + ")", safeSQL.getValues());
            if (ds != null) {
                DataSet inprogressDS = new DataSet();
                boolean allok = true;
                for (int i = 0; i < ds.size(); ++i) {
                    String changerequestid = ds.getString(i, "changerequestid");
                    String changerequeststatus = ds.getString(i, "changerequeststatus");
                    if ("Accepted".equals(changerequeststatus) || "In Progress".equals(changerequeststatus)) {
                        if (!"Accepted".equals(changerequeststatus)) continue;
                        int row = inprogressDS.addRow();
                        inprogressDS.setString(row, "changerequestid", changerequestid);
                        continue;
                    }
                    allok = false;
                    this.setError("Validation Failure", "VALIDATION", this.getTranslationProcessor().translate("Change Log can only be moved to a Change Request in a status of Accepted or In Progress."));
                }
                if (allok && inprogressDS.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_ChangeRequest");
                    props.setProperty("keyid1", inprogressDS.getColumnValues("changerequestid", ";"));
                    props.setProperty("changerequeststatus", "In Progress");
                    if (!"(system)".equalsIgnoreCase(this.connectionInfo.getSysuserId())) {
                        props.setProperty("startedby", this.connectionInfo.getSysuserId());
                    }
                    props.setProperty("starteddt", "n");
                    props.setProperty("auditreason", list.getProperty("auditreason"));
                    props.setProperty("auditactivity", list.getProperty("auditactivity"));
                    props.setProperty("auditdt", list.getProperty("auditdt"));
                    props.setProperty("auditsignedflag", list.getProperty("auditsignedflag"));
                    props.setProperty("__sdcruleconfirm", "Y");
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
            }
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty(PROPERTY_BYPASS_ISSUE_COMMUNICATION, "N").equals("N")) {
            boolean changeLogCheckedIn = false;
            DataSet primary = sdiData.getDataset("primary");
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "changelogstatus") || !primary.getString(i, "changelogstatus", "").equals(STATUS_CHECKED_IN)) continue;
                changeLogCheckedIn = true;
                break;
            }
            if (this.isIssueRepositoryCommunicationEnabled(changeLogCheckedIn)) {
                this.updateChangeRequestToIssueRepository(sdiData);
            }
        }
    }

    private boolean canUserEditChangeLog(String sysuserid, String sysuserdepartmentlist, String changelogstatus, String checkedoutbyuserid, String checkedoutbydepartmentid) {
        if (STATUS_CHECKED_OUT.equals(changelogstatus) && !sysuserid.equals(checkedoutbyuserid)) {
            if (checkedoutbydepartmentid.length() > 0) {
                return sysuserdepartmentlist.contains(";" + checkedoutbydepartmentid + ";");
            }
            String cmtadminroleid = CMTPolicy.getPolicy(this.getConnectionId(), "").getCMTAdminRoleID();
            return this.getConnectionProcessor().getSapphireConnection().hasRole(cmtadminroleid);
        }
        return true;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private void updateChangeRequestToIssueRepository(SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (!primary.isValidColumn("changerequestid")) {
            if (this.getBeforeEditImage() != null) {
                DataSet oldPrimary = this.getBeforeEditImage().getDataset("primary");
                LV_ChangeRequest.updateToIssueRepository(oldPrimary.getColumnValues("changerequestid", ";"), this.getConfigurationProcessor(), this.getActionProcessor());
            }
        } else {
            LV_ChangeRequest.updateToIssueRepository(primary.getColumnValues("changerequestid", ";"), this.getConfigurationProcessor(), this.getActionProcessor());
        }
    }

    private boolean isIssueRepositoryCommunicationEnabled(boolean changeLogCheckedIn) throws SapphireException {
        PropertyList repositoryProps = this.getConfigurationProcessor().getPolicy("IssueRepositoryPolicy", "CMTCommunication Custom");
        if (repositoryProps != null && repositoryProps.getProperty("enabled", "N").equals("Y")) {
            PropertyList changeRequestRepositoryProps = repositoryProps.getPropertyListNotNull("changerequestprops");
            String transferRule = changeRequestRepositoryProps.getProperty("changelogrule", "N");
            return transferRule.equals("Y") || changeLogCheckedIn && transferRule.equals(STATUS_CHECKED_IN);
        }
        return false;
    }
}

