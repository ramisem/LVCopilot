/*
 * Decompiled with CFR 0.152.
 */
package sapphire.ext;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseIssueHandler
extends BaseCustom {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    public static final String ID = "BaseIssueHandler";
    public static final String VERSIONID = "1";
    public static final String METHOD_SUBMITISSUE = "submitIssue";
    public static final String METHOD_GETISSUE = "getIssue";
    public static final String METHOD_SEARCHISSUE = "searchIssue";
    public static final String METHOD_TRANSFERCHANGEREQUESTINFOTOISSUE = "transferChangeRequestInfoToIssue";
    public static final String PROPERTY_CHANGEREQUESTID = "changerequestid";
    public static final String PROPERTY_ISSUEID = "issueid";
    public static final String PROPERTY_ISSUEREPOSITORYID = "issuerepositoryid";
    public static final String RETURN_ISSUEPROPERTIES = "issueproperties";
    public static final String RETURN_SEARCHRESULTS = "searchresults";
    public static final String PROPERTY_USERPASSWORD = "userpassword";
    protected PropertyList repositoryProps;
    private static HashMap<String, String> submissionStatus = new HashMap();
    private String submissionId;
    protected DBAccess database;
    protected ConnectionInfo connectionInfo;

    public void setDatabase(DBAccess database) {
        this.database = database;
    }

    public static BaseIssueHandler getInstance(String handlerClass, SapphireConnection sapphireConnection) throws SapphireException {
        try {
            Class<?> clazz = Class.forName(handlerClass);
            BaseIssueHandler baseIssueHandler = (BaseIssueHandler)clazz.newInstance();
            baseIssueHandler.setConnectionId(sapphireConnection.getConnectionId());
            baseIssueHandler.connectionInfo = sapphireConnection;
            return baseIssueHandler;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public static String generateSubmissionId(String connectionId) {
        return connectionId + "_" + System.currentTimeMillis();
    }

    public static void deleteSubmissionId(String submissionId) {
        if (submissionId != null && submissionId.length() > 0) {
            submissionStatus.remove(submissionId);
        }
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public static String getSubmitProgress(String submissionId) {
        if (submissionStatus.containsKey(submissionId)) {
            return submissionStatus.get(submissionId);
        }
        return "";
    }

    protected void updateProgressStatus(String progressText) {
        this.logger.info("Progress Status: " + progressText);
        if (this.submissionId.length() > 0) {
            submissionStatus.put(this.submissionId, progressText);
        }
    }

    public final void executeMethod(String method, PropertyList properties) throws SapphireException {
        this.setSubmissionId(properties.getProperty("submissionid", ""));
        this.updateProgressStatus("Initiating Handler class...");
        try {
            String issueRepositoryId = properties.getProperty(PROPERTY_ISSUEREPOSITORYID);
            this.repositoryProps = this.getConfigurationProcessor().getPolicy("IssueRepositoryPolicy", issueRepositoryId);
            if (this.repositoryProps == null) {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Policy node not found: ") + issueRepositoryId);
            }
            if ("N".equals(this.repositoryProps.getProperty("enabled", "N"))) {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("The selected Repository is not enabled."));
            }
            switch (method) {
                case "submitIssue": {
                    this.logger.info("Invoking Handler Impl method for Submit Issue.");
                    String issueRefId = this.submitIssue(properties.getProperty(PROPERTY_ISSUEID), issueRepositoryId, properties.getProperty(PROPERTY_USERPASSWORD));
                    properties.setProperty("issuerefid", issueRefId);
                    this.updateProgressStatus("Issue Submitted. = " + issueRefId);
                    break;
                }
                case "getIssue": {
                    this.logger.info("Invoking Handler Impl method for Get Issue.");
                    PropertyList issueProps = this.getIssue(properties.getProperty(PROPERTY_ISSUEID), issueRepositoryId, properties.getProperty(PROPERTY_USERPASSWORD));
                    properties.setProperty(RETURN_ISSUEPROPERTIES, issueProps);
                    this.updateProgressStatus("Issue info acquired. = " + properties.getProperty(PROPERTY_ISSUEID));
                    break;
                }
                case "searchIssue": {
                    this.logger.info("Invoking Handler Impl method for Search Issue.");
                    PropertyList searchResultProps = this.searchIssue(properties.getProperty(PROPERTY_ISSUEID), issueRepositoryId, properties.getProperty(PROPERTY_USERPASSWORD));
                    properties.setProperty(RETURN_SEARCHRESULTS, searchResultProps);
                    this.updateProgressStatus("Search performed = " + properties.getProperty(PROPERTY_ISSUEID));
                    break;
                }
                case "transferChangeRequestInfoToIssue": {
                    this.logger.info("Invoking Handler Impl method for Transferring Change Request Info To Issue.");
                    this.transferChangeRequestInfoToIssue(properties.getProperty(PROPERTY_CHANGEREQUESTID), issueRepositoryId, properties.getProperty(PROPERTY_USERPASSWORD));
                    this.updateProgressStatus("Change Request info transferred = " + properties.getProperty(PROPERTY_CHANGEREQUESTID));
                    break;
                }
                default: {
                    throw new SapphireException("Unexpected method: " + method);
                }
            }
        }
        catch (SapphireException e) {
            this.updateProgressStatus("ERROR: " + e.getMessage());
            this.logger.stackTrace(e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }

    public String submitIssue(String issueId, String issueRepositoryId, String loginCredentials) throws SapphireException {
        throw new SapphireException(ID, "FAILURE", this.getTranslationProcessor().translate("Method not implemented: ") + METHOD_SUBMITISSUE);
    }

    public PropertyList getIssue(String issueId, String issueRepositoryId, String loginCredentials) throws SapphireException {
        throw new SapphireException(ID, "FAILURE", this.getTranslationProcessor().translate("Method not implemented: ") + METHOD_GETISSUE);
    }

    public PropertyList searchIssue(String issueId, String issueRepositoryId, String loginCredentials) throws SapphireException {
        throw new SapphireException(ID, "FAILURE", this.getTranslationProcessor().translate("Method not implemented: ") + METHOD_SEARCHISSUE);
    }

    public void transferChangeRequestInfoToIssue(String changeRequestId, String issueRepositoryId, String loginCredentials) throws SapphireException {
        throw new SapphireException(ID, "FAILURE", this.getTranslationProcessor().translate("Method not implemented: ") + METHOD_TRANSFERCHANGEREQUESTINFOTOISSUE);
    }

    protected String getRepositoryExtraPropsValue(PropertyList repositoryProps, String propertyId) throws SapphireException {
        PropertyListCollection extraPropsList = repositoryProps.getCollectionNotNull("extrapropslist");
        PropertyList extraProp = extraPropsList.find("propertyid", propertyId);
        if (extraProp == null || extraProp.size() == 0) {
            return "";
        }
        return extraProp.getProperty("propertyvalue");
    }

    protected String getLoginCredentials(PropertyList repositoryProps, String enteredLoginCredentials) throws SapphireException {
        String loginAuthType = repositoryProps.getProperty("loginauthtype");
        if (loginAuthType.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Login Authentication mode is undefined."));
        }
        if ("User".equals(loginAuthType)) {
            String userName = StringUtil.split(enteredLoginCredentials, "#;#")[0];
            String password = StringUtil.split(enteredLoginCredentials, "#;#")[1];
            if (userName.length() == 0 || password.length() == 0) {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Login information seems to be invalid. Please logout and log back in to refresh your login credentials"));
            }
            return enteredLoginCredentials;
        }
        if ("Global".equals(loginAuthType)) {
            PropertyList globalAuthProps = repositoryProps.getPropertyListNotNull("globalauthprops");
            String userName = globalAuthProps.getProperty("username");
            String password = globalAuthProps.getDecryptedProperty("password");
            if (userName.length() == 0 || password.length() == 0) {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Login information seems to be invalid. Please provide information in the Policy."));
            }
            return userName + "#;#" + password;
        }
        return "";
    }
}

