/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.modules.issuemanagement.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.attachment.Attachment;
import sapphire.ext.BaseIssueHandler;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DevJIRAHandler
extends BaseIssueHandler {
    static final String ID = DevJIRAHandler.class.getName();
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private String auth;
    private String endpoint;

    @Override
    public String submitIssue(String issueId, String issueRepositoryId, String loginCredentials) throws SapphireException {
        try {
            this.loadRepositoryProps(loginCredentials);
            return this.submitToJIRA(issueId);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    private void loadRepositoryProps(String loginCredentials) throws SapphireException {
        this.updateProgressStatus("Checking Policy properties.");
        String calcLoginCredentials = this.getLoginCredentials(this.repositoryProps, loginCredentials);
        String userName = StringUtil.split(calcLoginCredentials, "#;#")[0];
        String password = StringUtil.split(calcLoginCredentials, "#;#")[1];
        this.auth = Base64.encodeBase64String((byte[])(userName + ":" + password).getBytes());
        this.endpoint = this.getRepositoryExtraPropsValue(this.repositoryProps, "endpoint");
        if (this.endpoint == null || this.endpoint.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("JIRA REST Endpoint not defined."));
        }
        if (!this.endpoint.endsWith("/")) {
            this.endpoint = this.endpoint + "/";
        }
    }

    private String submitToJIRA(String issueId) throws IOException, JSONException, SapphireException, SQLException {
        String issueRefKey = this.createIssue(issueId);
        this.updateProgressStatus("Retrieving Attachment info.");
        String sql = "SELECT a.* FROM issue i JOIN sdiattachment a ON a.sdcid = 'LV_Issue' AND a.keyid1 = i.issueid WHERE i.issueid = ? AND typeflag IN ('U', 'S', 'R', 'F')";
        DataSet attachmentsDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{issueId});
        if (attachmentsDS == null) {
            throw new SapphireException(ID, "FAILURE", this.getTranslationProcessor().translate("Exception occurred while trying to retrieve Issue Attachments."));
        }
        if (attachmentsDS.getRowCount() > 0) {
            AttachmentProcessor attachmentProcessor = this.getRakFile() == null ? new AttachmentProcessor(this.getConnectionid()) : new AttachmentProcessor(this.getRakFile(), this.getConnectionid());
            for (int i = 0; i < attachmentsDS.getRowCount(); ++i) {
                this.updateProgressStatus("Uploading attachment (" + (i + 1) + " of " + attachmentsDS.getRowCount() + ")");
                int attNum = attachmentsDS.getInt(i, "attachmentnum");
                Attachment attachment = Attachment.getAttachment("LV_Issue", issueId, "", "", attNum);
                attachment = attachmentProcessor.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
                String attachmentType = attachmentsDS.getString(i, "typeflag", "");
                InputStream fileStream = null;
                String fileName = "";
                if ("U".equals(attachmentType) || "R".equals(attachmentType) || "F".equals(attachmentType)) {
                    fileName = attachmentsDS.getString(i, "sourcefilename", attachmentsDS.getString(i, "attachmentdesc"));
                    fileStream = attachment.getInputStream();
                } else if ("S".equalsIgnoreCase(attachmentType)) {
                    fileName = attachmentsDS.getString(i, "sourcefilename");
                    String dsId = issueId + "_Att_" + attNum;
                    this.database.createPreparedResultSet(dsId, "SELECT attachment FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND attachmentnum = ?", new Object[]{"LV_Issue", issueId, new Integer(attNum)});
                    if (this.database.getNext(dsId)) {
                        fileStream = this.database.getBlob(dsId, "attachment").getBinaryStream();
                    }
                    this.database.closeResultSet(dsId);
                }
                this.uploadAttachmentJavaURL(issueRefKey, fileName, fileStream);
            }
        }
        return issueRefKey;
    }

    private String createIssue(String issueId) throws IOException, JSONException, SapphireException {
        this.updateProgressStatus("Getting Issue Information.");
        String sql = "SELECT  i.* FROM issue i  WHERE i.issueid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{issueId}, true);
        if (ds == null || ds.getRowCount() == 0) {
            throw new SapphireException(ID, "FAILURE", this.getTranslationProcessor().translate("Exception occurred while trying to retrive Issue info."));
        }
        PropertyListCollection extraPropsList = this.repositoryProps.getCollection("extrapropslist");
        PropertyList issueTypeProps = extraPropsList.find("propertyid", "jira_issuetype");
        PropertyList projectProps = extraPropsList.find("propertyid", "jira_project");
        PropertyList fixVersionProps = extraPropsList.find("propertyid", "jira_fixversion");
        PropertyList functionalAreaProps = extraPropsList.find("propertyid", "jira_functionalarea");
        if (issueTypeProps == null || projectProps == null || fixVersionProps == null || functionalAreaProps == null) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing Mandatory Inputs for JIRA."));
        }
        String issueDesc = ds.getString(0, "issuesummary", "");
        String steps = ds.getString(0, "notes", "");
        String priority = ds.getString(0, "priority", "");
        String issueType = issueTypeProps.getProperty("propertyvalue", "");
        String project = projectProps.getProperty("propertyvalue", "");
        String fixVersion = fixVersionProps.getProperty("propertyvalue", "");
        String functionalArea = functionalAreaProps.getProperty("propertyvalue", "");
        if (issueDesc.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing Mandatory Inputs for JIRA: Description"));
        }
        if (steps.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing Mandatory Inputs for JIRA: Reproducible Steps"));
        }
        if (project.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing Mandatory Inputs for JIRA: Project"));
        }
        if (issueType.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing Mandatory Inputs for JIRA: Issue Type"));
        }
        if (fixVersion.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing Mandatory Inputs for JIRA: Fix Version"));
        }
        if (functionalArea.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing Mandatory Inputs for JIRA: Functional Area"));
        }
        if (priority.length() == 0) {
            throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Missing Mandatory Inputs for JIRA: Priority"));
        }
        switch (priority) {
            case "Low": {
                priority = "Low";
                break;
            }
            case "Normal": {
                priority = "Medium";
                break;
            }
            case "High": {
                priority = "High";
                break;
            }
            case "Emergency": {
                priority = "Critical";
                break;
            }
            default: {
                throw new SapphireException(ID, "VALIDATION", this.getTranslationProcessor().translate("Priority value is un-mappable to JIRA."));
            }
        }
        this.updateProgressStatus("Initiating connection to endpoint.");
        URL url = new URL(this.endpoint + "api/latest/issue");
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Basic " + this.auth);
        JSONObject input = new JSONObject();
        input.put("fields", new JSONObject().put("issuetype", new JSONObject().put("name", issueType)).put("project", new JSONObject().put("key", project)).put("fixVersions", new JSONArray().put(new JSONObject().put("name", fixVersion))).put("customfield_10609", new JSONObject().put("value", functionalArea)).put("summary", issueDesc).put("customfield_11402", new JSONObject().put("value", priority)).put("customfield_10618", steps));
        OutputStream os = conn.getOutputStream();
        os.write(input.toString().getBytes());
        os.flush();
        if (conn.getResponseCode() != 201) {
            String output = DevJIRAHandler.getResponseString(conn.getErrorStream());
            this.logger.error(output);
            String message = "Issue Not created: " + conn.getResponseCode() + ": " + conn.getResponseMessage();
            if (conn.getResponseCode() == 403) {
                message = "Access Forbidden. Please login to JIRA using a web browser.";
            }
            this.logger.error(message);
            throw new SapphireException(message);
        }
        String output = DevJIRAHandler.getResponseString(conn.getInputStream());
        conn.disconnect();
        this.logger.info("Issue Created: " + output.toString());
        this.updateProgressStatus("Issue Created.");
        JSONObject respObj = new JSONObject(output.toString());
        String issueRefId = respObj.getString("id");
        String issueRefKey = respObj.getString("key");
        return issueRefKey;
    }

    private void uploadAttachmentJavaURL(String issueRefKey, String fileName, InputStream fileStream) throws IOException, SapphireException {
        String output;
        String boundary = "" + issueRefKey + "#" + System.currentTimeMillis() + "";
        String charset = "UTF-8";
        String LINE_FEED = "\r\n";
        URL url = new URL(this.endpoint + "api/latest/issue/" + issueRefKey + "/attachments");
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=\"" + boundary + "\"");
        conn.setRequestProperty("Authorization", "Basic " + this.auth);
        conn.setRequestProperty("X-Atlassian-Token", "nocheck");
        OutputStream os = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, charset), true);
        String fileContentType = URLConnection.guessContentTypeFromName(fileName);
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Type: " + fileContentType).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=file; filename=\"" + fileName + "\"").append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();
        if (fileStream != null) {
            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            fileStream.close();
        }
        writer.append(LINE_FEED);
        writer.flush();
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();
        if (conn.getResponseCode() != 200) {
            output = DevJIRAHandler.getResponseString(conn.getErrorStream());
            this.logger.error("File Upload Failed: " + output);
            throw new SapphireException("File Upload Failed for: " + issueRefKey + " - " + conn.getResponseCode() + ": " + output);
        }
        output = DevJIRAHandler.getResponseString(conn.getInputStream());
        conn.disconnect();
        this.logger.info("File Uploaded: " + output.toString());
    }

    private static String getResponseString(InputStream inputStream) throws IOException {
        StringBuffer resString = new StringBuffer();
        if (inputStream != null) {
            String responseLine;
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((responseLine = inputReader.readLine()) != null) {
                resString.append(responseLine);
            }
            inputReader.close();
        }
        return resString.toString();
    }
}

