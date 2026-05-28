/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.sapphire.modules.issuemanagement.IssueManagementUtil;
import java.util.Arrays;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.ext.BaseIssueHandler;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class UpdateChangeRequestInfoToIssue
extends BaseAction {
    public static final String PROPERTY_MODE = "mode";
    public static final String PROPERTY_CHANGEREQUESTID = "changerequestid";
    public static final String PROPERTY_ISSUEREPOSITORYID = "issuerepositoryid";
    public static final String MODE_CHANGEREQUEST = "changerequest";
    public static final String MODE_ALL = "all";
    public static final String MODE_FAILED = "failed";
    public static final String ID = "UpdateChangeRequestInfoToIssue";
    public static final String VERSIONID = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block5: {
            BaseIssueHandler baseIssueHandler;
            String issueRepositoryId;
            String userpassword;
            String mode;
            block4: {
                mode = properties.getProperty(PROPERTY_MODE, MODE_CHANGEREQUEST);
                userpassword = properties.getProperty("userpassword");
                issueRepositoryId = properties.getProperty(PROPERTY_ISSUEREPOSITORYID, "CMTCommunication Custom");
                this.logger.debug("Getting Policy Info.");
                PropertyList repositoryProps = IssueManagementUtil.getIssueRepositoryProperties(issueRepositoryId, this.getTranslationProcessor(), this.getConfigurationProcessor());
                String handlerClassName = repositoryProps.getProperty("handlerclass");
                this.logger.debug("Instantiating Issue Handler class: " + handlerClassName);
                baseIssueHandler = BaseIssueHandler.getInstance(handlerClassName, this.getConnectionProcessor().getSapphireConnection());
                baseIssueHandler.setDatabase(this.database);
                if (!mode.equals(MODE_CHANGEREQUEST)) break block4;
                HashSet<String> changeRequestIds = new HashSet<String>(Arrays.asList(StringUtil.split(properties.getProperty(PROPERTY_CHANGEREQUESTID), ";")));
                PropertyList transferProps = new PropertyList();
                transferProps.setProperty(PROPERTY_ISSUEREPOSITORYID, issueRepositoryId);
                transferProps.setProperty("userpassword", userpassword);
                for (String changeRequestId : changeRequestIds) {
                    if (changeRequestId.isEmpty()) continue;
                    transferProps.setProperty(PROPERTY_CHANGEREQUESTID, changeRequestId);
                    try {
                        baseIssueHandler.executeMethod("transferChangeRequestInfoToIssue", transferProps);
                    }
                    catch (SapphireException e) {
                        PropertyList editProps = new PropertyList();
                        editProps.setProperty("sdcid", "LV_ChangeRequest");
                        editProps.setProperty("keyid1", changeRequestId);
                        editProps.setProperty("transferfailedflag", "Y");
                        editProps.setProperty("bypassissuecommunication", "Y");
                        this.getActionProcessor().processAction("EditSDI", VERSIONID, editProps, true);
                        throw new SapphireException(e.getMessage());
                    }
                    String sql = "select transferfailedflag from changerequest where changerequestid = ?";
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{changeRequestId});
                    boolean wasFailed = ds.getString(0, "transferfailedflag", "N").startsWith("Y");
                    if (!wasFailed) continue;
                    PropertyList editProps = new PropertyList();
                    editProps.setProperty("sdcid", "LV_ChangeRequest");
                    editProps.setProperty("keyid1", changeRequestId);
                    editProps.setProperty("transferfailedflag", "(null)");
                    editProps.setProperty("bypassissuecommunication", "Y");
                    this.getActionProcessor().processAction("EditSDI", VERSIONID, editProps);
                }
                break block5;
            }
            if (!mode.equals(MODE_ALL) && !mode.equals(MODE_FAILED)) break block5;
            String sql = mode.equals(MODE_ALL) ? "select changerequestid from changerequest where externalreference is not null" : "select changerequestid from changerequest where externalreference is not null and transferfailedflag = 'Y'";
            DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
            PropertyList transferProps = new PropertyList();
            transferProps.setProperty(PROPERTY_ISSUEREPOSITORYID, issueRepositoryId);
            transferProps.setProperty("userpassword", userpassword);
            for (int i = 0; i < ds.getRowCount(); ++i) {
                transferProps.setProperty(PROPERTY_CHANGEREQUESTID, ds.getString(i, PROPERTY_CHANGEREQUESTID));
                baseIssueHandler.executeMethod("transferChangeRequestInfoToIssue", transferProps);
            }
        }
    }
}

