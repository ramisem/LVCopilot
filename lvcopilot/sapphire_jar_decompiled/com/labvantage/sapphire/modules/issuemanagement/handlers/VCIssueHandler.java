/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.rpc.ServiceException
 *  org.apache.axis.AxisProperties
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.modules.issuemanagement.handlers;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.admin.vc.PhoneHomeInfo;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.AttachmentTransport;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.SapphireWSServiceLocator;
import com.labvantage.sapphire.modules.issuemanagement.handlers.vcwsstub.SapphireWS_PortType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.zip.GZIPOutputStream;
import javax.xml.rpc.ServiceException;
import org.apache.axis.AxisProperties;
import org.apache.commons.codec.binary.Base64;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.attachment.Attachment;
import sapphire.ext.BaseIssueHandler;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class VCIssueHandler
extends BaseIssueHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    protected SapphireWS_PortType endpoint;
    protected String lvConnectionId = "";
    protected String targetSDCId = "";
    protected String attachmentPolicyNodeId = "";
    protected String endpointURL = "";
    protected String databaseId = "";

    public VCIssueHandler() {
        this.setEndpointURL("https://vantagecare.labvantage.com/vc/services/SapphireWS?wsdl");
        this.setDatabaseId("VantageCare");
        this.setTargetSDCId("Issue");
    }

    @Override
    public String submitIssue(String issueId, String issueRepositoryId, String loginCredentials) throws SapphireException {
        try {
            String issueRefId = null;
            String propertyBackup = AxisProperties.getProperty((String)"axis.socketSecureFactory");
            try {
                AxisProperties.setProperty((String)"axis.socketSecureFactory", (String)"com.labvantage.sapphire.modules.issuemanagement.handlers.JavaxFakeTrustSocketFactory");
                this.createWSEndpoint();
                this.createConnection(loginCredentials);
                issueRefId = this.createIssue(issueId);
                this.uploadAttachments(issueId, issueRefId);
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
            finally {
                AxisProperties.setProperty((String)"axis.socketSecureFactory", (String)propertyBackup);
                this.clearConnection();
            }
            return issueRefId;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    protected void createWSEndpoint() throws ServiceException, MalformedURLException, SapphireException {
        this.updateProgressStatus("Initiatizing Web Service endpoint.");
        SapphireWSServiceLocator serviceLocator = new SapphireWSServiceLocator();
        String policyEndpointURL = this.getRepositoryExtraPropsValue(this.repositoryProps, "endpoint");
        if (policyEndpointURL != null && policyEndpointURL.length() > 0) {
            this.endpointURL = policyEndpointURL;
            if (!this.endpointURL.toLowerCase().endsWith("?wsdl")) {
                this.endpointURL = this.endpointURL + "?wsdl";
            }
        }
        URL url = new URL(this.endpointURL);
        this.endpoint = serviceLocator.getSapphireWS(url);
    }

    protected void createConnection(String enteredLoginCredentials) throws SapphireException, RemoteException {
        this.updateProgressStatus("Creating connection to VantageCare.");
        String loginCredentials = this.getLoginCredentials(this.repositoryProps, enteredLoginCredentials);
        String userName = StringUtil.split(loginCredentials, "#;#")[0];
        String password = StringUtil.split(loginCredentials, "#;#")[1];
        String policyDatabase = this.getRepositoryExtraPropsValue(this.repositoryProps, "database");
        if (policyDatabase != null && policyDatabase.length() > 0) {
            this.databaseId = policyDatabase;
        }
        this.lvConnectionId = this.endpoint.getConnectionId(this.databaseId, userName, password);
    }

    protected String createIssue(String issueId) throws RemoteException, SapphireException {
        this.updateProgressStatus("Transmitting Issue Information.");
        String sql = "SELECT i.* FROM issue i  WHERE i.issueid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{issueId}, true);
        if (ds == null || ds.getRowCount() == 0) {
            throw new SapphireException("BaseIssueHandler", "FAILURE", this.getTranslationProcessor().translate("Exception occurred while trying to retrive Issue info."));
        }
        PropertyList actionProps = this.getCreateIssueProps(ds);
        this.logger.debug("Invoking VC CreateIssue action using properties: - " + actionProps.toJSONString());
        String returnXML = this.endpoint.processAction(this.lvConnectionId, "CreateIssue", "1", actionProps.toXMLString());
        PropertyList returnProps = new PropertyList();
        returnProps.setPropertyList(returnXML);
        this.updateProgressStatus("Issue created in target system.");
        return returnProps.getProperty("newissueid");
    }

    private void uploadAttachments(String issueId, String issueRefId) throws IOException, SapphireException {
        String sql = "SELECT sdcid, keyid1, keyid2, keyid3, attachmentnum, sourcefilename, attachmentdesc, typeflag, filename FROM issue i JOIN sdiattachment a ON a.sdcid = 'LV_Issue' AND a.keyid1 = i.issueid WHERE i.issueid = ?";
        DataSet attachmentsDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{issueId});
        if (attachmentsDS == null) {
            throw new SapphireException("BaseIssueHandler", "FAILURE", this.getTranslationProcessor().translate("Exception occurred while trying to retrieve Issue Attachments."));
        }
        for (int i = 0; i < attachmentsDS.getRowCount(); ++i) {
            this.updateProgressStatus("Uploading attachment (" + (i + 1) + " of " + attachmentsDS.getRowCount() + ")");
            AttachmentProcessor attachmentProcessor = this.getRakFile() == null ? new AttachmentProcessor(this.getConnectionid()) : new AttachmentProcessor(this.getRakFile(), this.getConnectionid());
            int attachmentNum = attachmentsDS.getInt(i, "attachmentnum");
            Attachment attachment = Attachment.getAttachment("LV_Issue", issueId, "", "", attachmentNum);
            try {
                attachmentProcessor.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
            }
            catch (Exception e) {
                throw new SapphireException("Error occurred when retrieving Attachment Info for Attachment Num#: " + attachmentNum + ". Reason: " + e.getMessage(), e);
            }
            if (attachment != null) {
                try (InputStream attachmentInStream = attachment.getInputStream();){
                    String fileName = attachmentsDS.getString(i, "filename", "");
                    String sourceFileName = attachmentsDS.getString(i, "sourcefilename", "");
                    String attDesc = attachmentsDS.getString(i, "attachmentdesc", "");
                    String typeFlag = attachmentsDS.getString(i, "typeflag", "");
                    AttachmentTransport attachmentTransport = new AttachmentTransport();
                    attachmentTransport.setFilename(fileName);
                    attachmentTransport.setSourceFilename(sourceFileName);
                    attachmentTransport.setDescription(attDesc);
                    attachmentTransport.setType(typeFlag);
                    attachmentTransport.setZipped(true);
                    attachmentTransport.setAttachmentPolicyNode(this.attachmentPolicyNodeId);
                    attachmentTransport.setData(VCIssueHandler.getZippedBytesInString(attachmentInStream));
                    this.endpoint.addSDIAttachment(this.lvConnectionId, this.targetSDCId, issueRefId, "", "", attachmentTransport);
                    continue;
                }
                catch (Exception e) {
                    throw new SapphireException("Error occurred when uploading Attachment Info for Attachment Num#: " + attachmentNum + ". Reason: " + e.getMessage(), e);
                }
            }
            throw new SapphireException("No Attachment Info found for Attachment Num#: " + attachmentNum);
        }
    }

    private void clearConnection() throws RemoteException {
        this.updateProgressStatus("Terminating Connection to VantageCare.");
        if (this.lvConnectionId.length() > 0) {
            this.endpoint.clearConnection(this.lvConnectionId);
        }
    }

    private static String getZippedBytesInString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        byte[] b = new byte[1024];
        while (inputStream.read(b) != -1) {
            gzip.write(b);
        }
        gzip.close();
        return Base64.encodeBase64String((byte[])out.toByteArray());
    }

    protected PropertyList getCreateIssueProps(DataSet ds) throws SapphireException {
        DataSet userDS;
        String licenseKeyId = PhoneHomeInfo.getInstance().getInfo().getProperty("licensekeyid");
        String build = "DM" + Build.getBuild() + "_" + Build.getPatch();
        String priority = ds.getString(0, "priority", "");
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", this.targetSDCId);
        actionProps.setProperty("licensekeyid", licenseKeyId);
        actionProps.setProperty("priority", priority);
        actionProps.setProperty("build", build);
        actionProps.setProperty("cust_trackingnum", ds.getString(0, "issueid", ""));
        actionProps.setProperty("histissuedesc", ds.getString(0, "issuesummary", ""));
        actionProps.setProperty("bugnotes", ds.getString(0, "notes", ""));
        actionProps.setProperty("env_info", ds.getString(0, "envinfo", ""));
        if ("Y".equalsIgnoreCase(StringUtil.getYN(this.getRepositoryExtraPropsValue(this.repositoryProps, "addtoemailcclist"), "N")) && (userDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT email FROM sysuser WHERE sysuserid = ?", new Object[]{this.connectionInfo.getSysuserId()})).getRowCount() > 0) {
            String sysUserEmailId = userDS.getString(0, "email", "");
            actionProps.setProperty("email_ms_clients", sysUserEmailId);
        }
        return actionProps;
    }

    public void setTargetSDCId(String targetSDCId) {
        this.targetSDCId = targetSDCId;
    }

    public void setAttachmentPolicyNodeId(String attachmentPolicyNodeId) {
        this.attachmentPolicyNodeId = attachmentPolicyNodeId;
    }

    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }
}

