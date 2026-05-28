/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_ChangeRequest
extends BaseSDCRules {
    public static final String SDC = "LV_ChangeRequest";
    public static final String STATUS_INITIAL = "Initial";
    public static final String STATUS_ACCEPTED = "Accepted";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String PROPERTY_BYPASS_ISSUE_COMMUNICATION = "bypassissuecommunication";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if ("N".equals(CMTPolicy.getPolicy(this.getConnectionId(), "").getPolicyPropertyList().getPropertyListNotNull("changerequest").getProperty("requireacceptance", "Y"))) {
            primary.setString(-1, "changerequeststatus", STATUS_ACCEPTED);
        } else {
            primary.setString(-1, "changerequeststatus", STATUS_INITIAL);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (actionProps.getProperty(PROPERTY_BYPASS_ISSUE_COMMUNICATION, "N").equals("N") && this.isIssueRepositoryCommunicationEnabled(null)) {
            DataSet primary = sdiData.getDataset("primary");
            LV_ChangeRequest.updateToIssueRepository(primary.getColumnValues("changerequestid", ";"), this.getConfigurationProcessor(), this.getActionProcessor());
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "changerequeststatus")) continue;
            ArrayList<String> list = new ArrayList<String>();
            String changerequestid = primary.getString(i, "changerequestid");
            String changeRequestStatus = primary.getString(i, "changerequeststatus", "");
            if (STATUS_CANCELLED.equals(changeRequestStatus) && this.database.getPreparedCount("select count(changelogid) from changelog where changerequestid = ? and changelogstatus = 'Checked Out'", new String[]{changerequestid}) > 0) {
                throw new SapphireException(this.getTranslationProcessor().translate("Validation Failure"), "VALIDATION", this.getTranslationProcessor().translate("Change Request not allowed to be cancelled. Checked Out Change Logs found in Change Request.") + " [" + changerequestid + "]");
            }
            switch (changeRequestStatus) {
                case "Accepted": {
                    list.add(STATUS_INITIAL);
                    break;
                }
                case "In Progress": {
                    list.add(STATUS_APPROVED);
                    list.add(STATUS_ACCEPTED);
                    list.add(STATUS_COMPLETED);
                    break;
                }
                case "Completed": {
                    list.add(STATUS_IN_PROGRESS);
                    break;
                }
                case "Approved": {
                    list.add(STATUS_COMPLETED);
                    break;
                }
                case "Cancelled": {
                    list.add(STATUS_ACCEPTED);
                    list.add(STATUS_IN_PROGRESS);
                    list.add(STATUS_COMPLETED);
                }
            }
            String oldChangeRequestStatus = this.getOldPrimaryValue(primary, i, "changerequeststatus");
            if (list.contains(oldChangeRequestStatus)) continue;
            throw new SapphireException(this.getTranslationProcessor().translate("Validation Failure"), "VALIDATION", this.getTranslationProcessor().translate("Invalid Change Request status update") + " [" + oldChangeRequestStatus + " >> " + changeRequestStatus + "]");
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String changeRequestStatus = null;
        ArrayList<String> approvedList = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "changerequeststatus")) continue;
            String changerequestid = primary.getString(i, "changerequestid");
            changeRequestStatus = primary.getString(i, "changerequeststatus", "");
            if (!STATUS_APPROVED.equals(changeRequestStatus)) continue;
            approvedList.add(changerequestid);
        }
        if (approvedList.size() > 0) {
            DataSet ds = this.getQueryProcessor().getSqlDataSet("select sysuserid, propertyvalue from PROFILEPROPERTY where propertyid = 'changelogoptions'");
            for (String changerequestid : approvedList) {
                for (int i = 0; i < ds.size(); ++i) {
                    String sysuserid = ds.getString(i, "sysuserid", "");
                    String propertyvalue = ds.getString(i, "propertyvalue", "");
                    if (propertyvalue.length() <= 0) continue;
                    try {
                        JSONObject o = new JSONObject(propertyvalue);
                        if (!o.has("changerequestid") || !changerequestid.equalsIgnoreCase(o.getString("changerequestid"))) continue;
                        o.put("changerequestid", "");
                        o.put("changerequestdesc", "");
                        o.put("donotprompt", "N");
                        new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.getConnectionId()).setProfileProperty(sysuserid, "changelogoptions", o.toString());
                        continue;
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (actionProps.getProperty(PROPERTY_BYPASS_ISSUE_COMMUNICATION, "N").equals("N") && this.isIssueRepositoryCommunicationEnabled(changeRequestStatus)) {
            LV_ChangeRequest.updateToIssueRepository(primary.getColumnValues("changerequestid", ";"), this.getConfigurationProcessor(), this.getActionProcessor());
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    public static void updateToIssueRepository(String changeRequestIds, ConfigurationProcessor cp, ActionProcessor actionProcessor) throws SapphireException {
        String uniqueChangeRequestIds = String.join((CharSequence)";", new HashSet<String>(Arrays.asList(StringUtil.split(changeRequestIds, ";"))));
        PropertyList todoListProps = new PropertyList();
        todoListProps.setProperty("actionid", "UpdateChangeRequestInfoToIssue");
        todoListProps.setProperty("actionversionid", "1");
        todoListProps.setProperty("mode", "changerequest");
        todoListProps.setProperty("changerequestid", uniqueChangeRequestIds);
        actionProcessor.processAction("AddToDoListEntry", "1", todoListProps);
    }

    private boolean isIssueRepositoryCommunicationEnabled(String changeRequestStatus) throws SapphireException {
        PropertyList changeRequestRepositoryProps;
        String transferRule;
        PropertyList repositoryProps = this.getConfigurationProcessor().getPolicy("IssueRepositoryPolicy", "CMTCommunication Custom");
        return repositoryProps != null && repositoryProps.getProperty("enabled", "N").equals("Y") && ((transferRule = (changeRequestRepositoryProps = repositoryProps.getPropertyListNotNull("changerequestprops")).getProperty("changerequestrule", "N")).equals("Y") || transferRule.contains(changeRequestStatus));
    }
}

