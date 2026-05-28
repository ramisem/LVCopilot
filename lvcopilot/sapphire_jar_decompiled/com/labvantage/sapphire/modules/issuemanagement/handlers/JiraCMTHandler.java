/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.modules.issuemanagement.handlers;

import com.labvantage.sapphire.modules.issuemanagement.IssueManagementUtil;
import com.labvantage.sapphire.tagext.SDITagUtil;
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
import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseIssueHandler;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class JiraCMTHandler
extends BaseIssueHandler {
    private static final String LV_HOST = "lvhost";
    private String auth;
    private String endpoint;
    private String descriptionFieldId = "";
    private String statusFieldId = "";
    private String lvhost = "";
    private String restrictivewhere = "";

    @Override
    public PropertyList getIssue(String issueId, String issueRepositoryId, String loginCredentials) throws SapphireException {
        try {
            this.loadRepositoryProps(loginCredentials);
            return this.getJiraIssueInfo(issueId);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    @Override
    public PropertyList searchIssue(String issueId, String issueRepositoryId, String loginCredentials) throws SapphireException {
        try {
            this.loadRepositoryProps(loginCredentials);
            return this.searchJiraIssue(issueId);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    @Override
    public void transferChangeRequestInfoToIssue(String changeRequestId, String issueRepositoryId, String loginCredentials) throws SapphireException {
        this.loadRepositoryProps(loginCredentials);
        if (this.descriptionFieldId.isEmpty()) {
            throw new SapphireException("Description field id missing");
        }
        String sql = "select externalreference from changerequest where changerequestid = ? union select externalreferenceid externalreference from changerequestextref where changerequestid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{changeRequestId, changeRequestId});
        if (ds.getRowCount() > 0) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                boolean newStatus;
                String externalReference = ds.getString(i, "externalreference", "");
                if (externalReference.isEmpty()) continue;
                PropertyList oldIssueInfo = this.getJiraIssueInfo(externalReference);
                PropertyList oldFields = oldIssueInfo.getPropertyList("fields");
                String oldInfo = oldFields.getProperty(this.descriptionFieldId, "");
                String oldStatus = oldFields.getProperty(this.statusFieldId, "");
                String changeRqSql = "select cr.* from changerequest cr where cr.externalreference = ? union select cr.* from changerequest cr inner join changerequestextref crer on cr.changerequestid = crer.changerequestid and crer.externalreferenceid = ?";
                DataSet changeRqDs = this.getQueryProcessor().getPreparedSqlDataSet(changeRqSql, (Object[])new String[]{externalReference, externalReference});
                PropertyList changeRequestProps = this.repositoryProps.getPropertyListNotNull("changerequestprops");
                PropertyList changeLogProps = changeRequestProps.getPropertyListNotNull("changelogs");
                String status = "";
                StringBuilder changeLogInfo = new StringBuilder();
                for (int j = 0; j < changeRqDs.getRowCount(); ++j) {
                    if (changeLogInfo.length() > 0) {
                        changeLogInfo.append("----\n");
                    }
                    String requestid = changeRqDs.getString(j, "changerequestid", "");
                    HashMap<String, String> dataset = this.getHashMapFromDataset(changeRqDs, j);
                    dataset.put(LV_HOST, this.lvhost);
                    this.getColumnText(dataset, changeRequestProps, changeLogInfo);
                    changeLogInfo.append("\r\n");
                    String crStatus = changeRqDs.getString(j, "changerequeststatus", "");
                    if (status.isEmpty() || crStatus.equals("In Progress")) {
                        status = crStatus;
                    }
                    this.collectRowInfo(changeLogProps, changeLogInfo, requestid);
                }
                String info = changeLogInfo.toString();
                boolean newInfo = !info.isEmpty() && !oldInfo.equals(info);
                boolean bl = newStatus = !this.statusFieldId.isEmpty() && !status.isEmpty() && !oldStatus.equals(status);
                if (!newInfo && !newStatus) continue;
                try {
                    JSONObject changeInfo = new JSONObject();
                    JSONObject fields = new JSONObject();
                    changeInfo.put("fields", fields);
                    if (newInfo) {
                        fields.put(this.descriptionFieldId, info);
                    }
                    if (newStatus) {
                        fields.put(this.statusFieldId, status);
                    }
                    String url = this.endpoint + "rest/api/latest/issue/" + externalReference;
                    this.communicateToJira(url, changeInfo, "PUT", 204);
                    continue;
                }
                catch (JSONException jSONException) {
                    // empty catch block
                }
            }
        }
    }

    private void loadRepositoryProps(String loginCredentials) throws SapphireException {
        this.updateProgressStatus("Checking Policy properties.");
        String calcLoginCredentials = this.getLoginCredentials(this.repositoryProps, loginCredentials);
        String userName = StringUtil.split(calcLoginCredentials, "#;#")[0];
        String password = StringUtil.split(calcLoginCredentials, "#;#")[1];
        this.auth = Base64.encodeBase64String((byte[])(userName + ":" + password).getBytes());
        this.endpoint = this.getRepositoryExtraPropsValue(this.repositoryProps, "endpoint");
        this.descriptionFieldId = this.getRepositoryExtraPropsValue(this.repositoryProps, "changelogfieldid");
        this.statusFieldId = this.getRepositoryExtraPropsValue(this.repositoryProps, "statusfieldid");
        this.lvhost = this.getRepositoryExtraPropsValue(this.repositoryProps, LV_HOST);
        this.restrictivewhere = this.getRepositoryExtraPropsValue(this.repositoryProps, "restrictivewhere");
        if (this.endpoint == null || this.endpoint.length() == 0) {
            throw new SapphireException("BaseIssueHandler", "VALIDATION", this.getTranslationProcessor().translate("JIRA REST Endpoint not defined."));
        }
        if (this.endpoint.endsWith("rest")) {
            this.endpoint = this.endpoint.substring(0, this.endpoint.lastIndexOf("rest"));
        }
        if (this.endpoint.endsWith("rest/")) {
            this.endpoint = this.endpoint.substring(0, this.endpoint.lastIndexOf("rest/"));
        }
        if (!this.endpoint.endsWith("/")) {
            this.endpoint = this.endpoint + "/";
        }
    }

    private PropertyList getJiraIssueInfo(String issueId) throws SapphireException {
        PropertyList returnProps;
        String url = this.endpoint + "rest/api/latest/issue/" + issueId;
        try {
            JSONObject info = this.communicateToJira(url, new JSONObject(), "GET", 200);
            returnProps = new PropertyList(info);
            String jiraIssueId = returnProps.getProperty("key");
            returnProps.setProperty("link", this.endpoint + "browse/" + jiraIssueId);
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
        return returnProps;
    }

    private PropertyList searchJiraIssue(String issueId) throws SapphireException {
        String url = this.endpoint + "rest/api/latest/issue/picker?query=" + issueId + "&showSubTasks=true&currentJQL=";
        if (!this.restrictivewhere.isEmpty()) {
            url = url + HttpUtil.encodeURIComponent(this.restrictivewhere);
        }
        PropertyList returnProps = new PropertyList();
        try {
            JSONObject searchResult = this.communicateToJira(url, new JSONObject(), "GET", 200);
            JSONArray sections = searchResult.optJSONArray("sections");
            if (sections != null) {
                for (int i = 0; i < sections.length(); ++i) {
                    JSONArray issues;
                    JSONObject section = sections.getJSONObject(i);
                    if (!section.optString("id", "").equals("cs") || (issues = section.optJSONArray("issues")) == null) continue;
                    returnProps = new PropertyList(sections.getJSONObject(i));
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
        return returnProps;
    }

    private void collectRowInfo(PropertyList changeLogProps, StringBuilder changeLogInfo, String requestid) {
        String changelogSql = "select * from changelog where changerequestid = ?";
        DataSet logDs = this.getQueryProcessor().getPreparedSqlDataSet(changelogSql, (Object[])new String[]{requestid});
        if (logDs.getRowCount() > 0) {
            PropertyListCollection cols = changeLogProps.getCollectionNotNull("columns");
            for (int k = 0; k < cols.size(); ++k) {
                PropertyList colInfo = cols.getPropertyList(k);
                boolean active = colInfo.getProperty("active", "Y").startsWith("Y");
                if (!active) continue;
                changeLogInfo.append("||").append(colInfo.getProperty("heading"));
            }
            changeLogInfo.append("||\r\n");
            for (int j = 0; j < logDs.getRowCount(); ++j) {
                HashMap<String, String> logRow = this.getHashMapFromDataset(logDs, j);
                logRow.put(LV_HOST, this.lvhost);
                for (int k = 0; k < cols.size(); ++k) {
                    PropertyList colInfo = cols.getPropertyList(k);
                    boolean active = colInfo.getProperty("active", "Y").startsWith("Y");
                    if (!active) continue;
                    PropertyList columnprops = colInfo.getPropertyListNotNull("columnprops");
                    StringBuilder subColText = new StringBuilder();
                    this.getColumnText(logRow, columnprops, subColText);
                    if (subColText.length() == 0) {
                        subColText.append(" ");
                    }
                    changeLogInfo.append("|").append((CharSequence)subColText);
                }
                changeLogInfo.append("|\n");
            }
        }
    }

    private void getColumnText(HashMap<String, String> rowData, PropertyList colInfo, StringBuilder subColText) {
        PropertyListCollection subCols = colInfo.getCollectionNotNull("subcolumns");
        for (int l = 0; l < subCols.size(); ++l) {
            String bypassStr;
            PropertyList subCol = subCols.getPropertyList(l);
            boolean show = true;
            String bybassinfo = subCol.getProperty("bybassifthisisempty", "");
            if (!bybassinfo.isEmpty() && ((bypassStr = IssueManagementUtil.replaceTokens(rowData, bybassinfo)).isEmpty() || bypassStr.equals("null") || bypassStr.equals("(null)"))) {
                show = false;
            }
            if (!show) continue;
            String columnRawText = IssueManagementUtil.replaceTokens(rowData, subCol.getProperty("column"));
            String displayvalue = subCol.getProperty("displayvalue");
            String columnText = !displayvalue.isEmpty() ? SDITagUtil.getDisplayValue(columnRawText, displayvalue) : columnRawText;
            if (subColText.length() > 0) {
                subColText.append(" ");
            }
            subColText.append(columnText);
        }
    }

    private synchronized JSONObject communicateToJira(String uri, JSONObject input, String method, int okHttpCode) throws SapphireException {
        return this.communicateToJira(uri, input, method, okHttpCode, null, null, null);
    }

    private synchronized JSONObject communicateToJira(String uri, JSONObject input, String method, int okHttpCode, String fileName, InputStream fileStream, String boundary) throws SapphireException {
        boolean isAttachmentUpload = fileName != null && !fileName.isEmpty() && fileStream != null;
        String charset = "UTF-8";
        String lineFeed = "\r\n";
        if (input == null) {
            input = new JSONObject();
        }
        JSONObject respObj = new JSONObject();
        HttpURLConnection conn = null;
        try {
            String output;
            OutputStream os;
            URL url = new URL(uri);
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod(method);
            conn.setRequestProperty("Authorization", "Basic " + this.auth);
            if (!isAttachmentUpload) {
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                if (input.length() > 0) {
                    conn.setDoOutput(true);
                    os = conn.getOutputStream();
                    os.write(input.toString().getBytes());
                    os.flush();
                }
            } else {
                conn.setUseCaches(false);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=\"" + boundary + "\"");
                conn.setRequestProperty("X-Atlassian-Token", "nocheck");
                os = conn.getOutputStream();
                try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, charset), true);){
                    int bytesRead;
                    String fileContentType = URLConnection.guessContentTypeFromName(fileName);
                    writer.append("--").append(boundary).append(lineFeed);
                    writer.append("Content-Type: ").append(fileContentType).append(lineFeed);
                    writer.append("Content-Disposition: form-data; name=file; filename=\"").append(fileName).append("\"").append(lineFeed);
                    writer.append("Content-Transfer-Encoding: binary").append(lineFeed).append(lineFeed);
                    writer.flush();
                    byte[] buffer = new byte[1024];
                    while ((bytesRead = fileStream.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                    fileStream.close();
                    writer.append(lineFeed);
                    writer.flush();
                    writer.append(lineFeed).flush();
                    writer.append("--").append(boundary).append("--").append(lineFeed);
                }
            }
            if (conn.getResponseCode() != okHttpCode) {
                output = JiraCMTHandler.getResponseString(conn.getErrorStream());
                this.logger.error(output);
                TranslationProcessor tp = this.getTranslationProcessor();
                String message = tp.translate("External server responded") + " " + conn.getResponseCode() + ": ";
                switch (conn.getResponseCode()) {
                    case 400: {
                        message = message + tp.translate("HTTP request call is invalid");
                        break;
                    }
                    case 401: {
                        message = message + tp.translate("The username and/or password were invalid") + ".";
                        break;
                    }
                    case 403: {
                        message = message + tp.translate("User does not have permission to complete this request") + ".";
                        break;
                    }
                    case 500: {
                        message = message + tp.translate("Internal Server Error");
                        break;
                    }
                    default: {
                        message = message + output;
                    }
                }
                this.logger.error(message);
                throw new SapphireException(message);
            }
            output = JiraCMTHandler.getResponseString(conn.getInputStream());
            conn.disconnect();
            if (!output.isEmpty()) {
                respObj = new JSONObject(output);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Could not communicate to JIRA: " + e.getMessage());
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return respObj;
    }

    private static String getResponseString(InputStream inputStream) throws IOException {
        StringBuilder resString = new StringBuilder();
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

    private HashMap<String, String> getHashMapFromDataset(DataSet ds, int row) {
        HashMap<String, String> hm = new HashMap<String, String>();
        for (int i = 0; i < ds.getColumnCount(); ++i) {
            String colId = ds.getColumnId(i);
            hm.put(colId, ds.getValue(row, colId, ""));
        }
        return hm;
    }
}

