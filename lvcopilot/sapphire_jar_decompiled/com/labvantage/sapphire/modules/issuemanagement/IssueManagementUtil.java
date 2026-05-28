/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.modules.issuemanagement;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class IssueManagementUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    public static final String SDC_ISSUE = "LV_Issue";
    public static final String POLICY = "IssueRepositoryPolicy";
    public static final String ROLE_SUBMITTER = "IssueSubmitter";
    public static final String SNAPSHOT_CLASS = "Issue Log Snapshot";
    public static final String ISSUESTATUS_INITIAL = "Initial";
    public static final String ISSUESTATUS_INREVIEW = "In Review";
    public static final String ISSUESTATUS_CANCELLED = "Cancelled";
    public static final String ISSUESTATUS_SUBMITTED = "Submitted";
    public static final String ISSUESTATUS_COMPLETE = "Completed";
    public static final String CREDENTIAL_SEPERATOR = "#;#";
    public static final String SESSION_TOKEN_SEPERATOR = "#@#";

    public static String getCredentialsFromSession(HttpServletRequest request, String issueRepositoryId, String sysUserId) {
        HttpSession session = request.getSession();
        String loginCredentialsObj = (String)session.getAttribute("issueRepository#@#" + issueRepositoryId + SESSION_TOKEN_SEPERATOR + sysUserId);
        String credentials = "";
        if (loginCredentialsObj != null) {
            credentials = loginCredentialsObj;
        }
        return credentials;
    }

    public static DataSet getAllIssueRepositories(QueryProcessor qp, HttpServletRequest request, String sysUserId) {
        DataSet repositories = new DataSet();
        DataSet ds = qp.getPreparedSqlDataSet("SELECT propertytreeid, valuetree, definitiontree FROM propertytree where propertytreeid = 'IssueRepositoryPolicy'", (Object[])new String[0], true);
        if (ds != null && ds.size() > 0) {
            String valuetree = ds.getString(0, "valuetree");
            String defTree = ds.getString(0, "definitiontree");
            PropertyTree propertyTree = new PropertyTree();
            try {
                propertyTree.setValueXML(valuetree);
                propertyTree.setDefinitionXML(defTree);
                ArrayList allNodes = propertyTree.getAllNodes();
                for (Object o : allNodes) {
                    PropertyList nodeProps;
                    Node node = (Node)o;
                    String nodeid = node.getNodeId();
                    if (node.isProduct() || "Sapphire Custom".equals(nodeid) || !"Y".equals((nodeProps = propertyTree.getNodePropertyList(nodeid, true)).getProperty("enabled", "N"))) continue;
                    String repositoryName = nodeProps.getProperty("repositoryname", nodeid);
                    String loginAuthType = nodeProps.getProperty("loginauthtype");
                    String loginSessionCredentialsFlag = "";
                    if ("User".equals(loginAuthType)) {
                        String loginCredentials = IssueManagementUtil.getCredentialsFromSession(request, nodeid, sysUserId);
                        loginSessionCredentialsFlag = loginCredentials.length() == 0 ? "N" : "Y";
                    }
                    int newRow = repositories.addRow();
                    repositories.setString(newRow, "nodeid", nodeid);
                    repositories.setString(newRow, "repositoryname", repositoryName);
                    repositories.setString(newRow, "loginauthtype", loginAuthType);
                    repositories.setString(newRow, "iscredentialsexists", loginSessionCredentialsFlag);
                }
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
        }
        return repositories;
    }

    public static void setLoginAttributeValue(HttpSession session, String issueRepositoryId, String sysUserId, String userName, String password) {
        session.setAttribute("issueRepository#@#" + issueRepositoryId + SESSION_TOKEN_SEPERATOR + sysUserId, (Object)(userName + CREDENTIAL_SEPERATOR + password));
    }

    public static String getIssueManagerJSHTML() {
        String jsHtml = "";
        jsHtml = "<script language='JavaScript' src='WEB-CORE/modules/dashboard/scripts/issuegizmo.js'></script>";
        return jsHtml.toString();
    }

    public static String doGetRecentIssue(String sysUserId, QueryProcessor qp, TranslationProcessor tp) throws SapphireException {
        String sql = "SELECT issueid FROM issue WHERE issuestatus = 'Initial' and createby = ? ORDER BY createdt desc";
        DataSet ds = qp.getPreparedSqlDataSet(sql, new Object[]{sysUserId});
        if (ds == null) {
            throw new SapphireException(tp.translate("Exception occurred when trying to retrieve Latest Issue."));
        }
        String latestIssueId = "";
        if (ds.getRowCount() > 0) {
            latestIssueId = ds.getString(0, "issueid", "");
        }
        return latestIssueId;
    }

    public static String doCreateGizmoIssue(String envProps, Logger logger, ActionProcessor ap) throws SapphireException {
        try {
            JSONObject envPropsObj = new JSONObject(envProps);
            envProps = envPropsObj.toString(2);
        }
        catch (JSONException e) {
            logger.error("Error occurred when trying to parse the Environment Info.");
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", SDC_ISSUE);
        props.setProperty("envinfo", StringUtil.replaceAll(envProps, ";", "#semicolon#"));
        ap.processAction("AddSDI", "1", props);
        String issueId = props.getProperty("newkeyid1");
        return issueId;
    }

    public static String doGetIssueURL(String issueId, QueryProcessor qp, TranslationProcessor tp, ConfigurationProcessor cp) throws SapphireException {
        String sql = "SELECT issueid, issueref, repositorynode, repositoryname, issuestatus FROM issue WHERE issueid = ?";
        DataSet issueInfo = qp.getPreparedSqlDataSet(sql, new Object[]{issueId});
        if (issueInfo.getRowCount() == 0) {
            throw new SapphireException(tp.translate("No information found for provided Issue: ") + issueId);
        }
        String issueRefId = issueInfo.getString(0, "issueref", "");
        String repositoryNode = issueInfo.getString(0, "repositorynode", "");
        String repositoryName = issueInfo.getString(0, "repositoryname", repositoryNode);
        if (issueRefId.length() == 0 || repositoryNode.length() == 0) {
            throw new SapphireException(tp.translate("Issue doesn't have any reference to a Repository."));
        }
        PropertyList repositoryProps = cp.getPolicy(POLICY, repositoryNode);
        if (repositoryProps == null || repositoryProps.size() == 0) {
            throw new SapphireException(tp.translate("No Repository information found in Policy for: ") + repositoryName);
        }
        String issueUrl = repositoryProps.getProperty("issueviewurl");
        if (issueUrl.length() == 0) {
            throw new SapphireException(tp.translate("No URL defined in Policy for Repository: ") + repositoryName);
        }
        issueUrl = StringUtil.replaceAll(issueUrl, "[issuerefid]", issueRefId);
        return issueUrl;
    }

    public static PropertyList getIssueRepositoryProperties(String issueRepositoryId, TranslationProcessor tp, ConfigurationProcessor cp) throws SapphireException {
        PropertyList repositoryProps = cp.getPolicy(POLICY, issueRepositoryId);
        if (repositoryProps == null) {
            throw new SapphireException("FAILURE", tp.translate("Exception occurred while trying to retrive Issue Manager Info."));
        }
        if (repositoryProps.getProperty("enabled", "N").equals("N")) {
            throw new SapphireException("VALIDATION", tp.translate("The selected Repository is not enabled."));
        }
        return repositoryProps;
    }

    public static String replaceTokens(Map issueProps, String tokenString) {
        String replaced = StringUtil.replaceAll(tokenString, "\\[", "\u00a4\u00a4\u00a4\u00a4\u00a4");
        replaced = StringUtil.replaceAll(replaced, "\\]", "@@@@@");
        replaced = OpalUtil.searchAndReplaceTokens(replaced, OpalUtil.getKeywordTokens(replaced), issueProps, false);
        replaced = StringUtil.replaceAll(replaced, "\u00a4\u00a4\u00a4\u00a4\u00a4", "[");
        replaced = StringUtil.replaceAll(replaced, "@@@@@", "]");
        return replaced;
    }
}

