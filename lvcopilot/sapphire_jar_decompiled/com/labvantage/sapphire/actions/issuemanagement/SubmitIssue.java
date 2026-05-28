/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.issuemanagement;

import com.labvantage.opal.handler.ErrorUtil;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.ext.BaseIssueHandler;
import sapphire.xml.PropertyList;

public class SubmitIssue
extends BaseAction
implements sapphire.action.SubmitIssue {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String issueId = properties.getProperty("issueid");
        String issueRepositoryId = properties.getProperty("issuerepositoryid");
        if (issueId.length() == 0 && issueRepositoryId.length() == 0) {
            throw new SapphireException("SubmitIssue", "VALIDATION", this.getTranslationProcessor().translate("Missing Mandatory Input."));
        }
        if (issueRepositoryId.indexOf(";") > -1) {
            throw new SapphireException("SubmitIssue", "VALIDATION", this.getTranslationProcessor().translate("Can only submit to one Issue Repository at a time."));
        }
        try {
            this.logger.info("Getting Policy Info.");
            PropertyList repositoryProps = this.getConfigurationProcessor().getPolicy("IssueRepositoryPolicy", issueRepositoryId);
            if (repositoryProps == null) {
                throw new SapphireException("SubmitIssue", "FAILURE", this.getTranslationProcessor().translate("Exception occurred while trying to retrive Issue Manager Info."));
            }
            if ("N".equals(repositoryProps.getProperty("enabled", "N"))) {
                throw new SapphireException("SubmitIssue", "VALIDATION", this.getTranslationProcessor().translate("The selected Repository is not enabled."));
            }
            String handlerClassName = repositoryProps.getProperty("handlerclass");
            this.logger.info("Instantiating Issue Handler class: " + handlerClassName);
            BaseIssueHandler baseIssueHandler = BaseIssueHandler.getInstance(handlerClassName, this.getConnectionProcessor().getSapphireConnection());
            baseIssueHandler.setDatabase(this.database);
            this.logger.info("Invoking Base Issue Handler method for Submit Issue.");
            baseIssueHandler.executeMethod("submitIssue", properties);
            String issueRefId = properties.getProperty("issuerefid");
            this.logger.info("Issue Submitted. Ref#: " + issueRefId + ". Updating submission info to Issue SDI.");
            this.updateSubmitInfo(issueId, issueRepositoryId, repositoryProps.getProperty("repositoryname"), issueRefId);
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }

    private void updateSubmitInfo(String issueId, String issueRepositoryId, String repositoryName, String issueRefId) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Issue");
        props.setProperty("keyid1", issueId);
        props.setProperty("issuestatus", "Submitted");
        props.setProperty("submitdt", "n");
        props.setProperty("repositorynode", issueRepositoryId.length() > 80 ? issueRepositoryId.substring(0, 79) : issueRepositoryId);
        props.setProperty("repositoryname", repositoryName.length() > 80 ? repositoryName.substring(0, 79) : repositoryName);
        props.setProperty("issueref", issueRefId);
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }
}

