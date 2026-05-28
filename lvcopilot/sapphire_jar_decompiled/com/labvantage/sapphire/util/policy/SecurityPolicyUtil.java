/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.xml.soap.SOAPException
 */
package com.labvantage.sapphire.util.policy;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.ddt.GenerateDeleteScript;
import com.labvantage.sapphire.actions.report.GenerateReport;
import com.labvantage.sapphire.actions.report.ReprintReport;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.ajax.operations.SDITemp;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentAjaxHandler;
import com.labvantage.sapphire.pageelements.forms.FormBuilderFormletAjaxMerger;
import com.labvantage.sapphire.pageelements.forms.FormBuilderSaveAjaxConverter;
import com.labvantage.sapphire.pageelements.gwt.server.DynamicLookupRequest;
import com.labvantage.sapphire.pageelements.gwt.server.NavigatorRequest;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.servlet.rest.RestException;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.xml.PropertyTree;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ActionBlock;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SecurityPolicyUtil {
    public static final String SECURITY_POLICY = "SecurityPolicy";
    public static final String DEFAULT_POLICY_NODE = "Sapphire Custom";
    public static final String VIRTUAL_POLICY_NODE = "Virtual Custom";
    public static final String PORTAL_POLICY_NODE = "Portal Custom";
    public static final String POLICYSECTION_RESTSERVICES = "restservices";
    public static final String POLICYSECTION_WEBSERVICES = "webservices";
    public static final String POLICYSECTION_ACTIONCOMMAND = "actioncommand";
    public static final String POLICYSECTION_REMOTEJAVAAPI = "remotejavaapi";
    public static final String POLICYSECTION_AJAX = "ajax";
    private static final String ACTIONPROCESSING_NONE = "N";
    private static final String ACTIONPROCESSING_FULL = "F";
    private static final String ACTIONPROCESSING_RESTICTED = "R";
    private static final String ACTIONPROCESSING_ROLE = "P";
    private static final String VIOLATIONHANDLING = "violationhandling";
    private static final String VIOLATIONNOTIFICATION = "notification";
    public static boolean httpOnlyRequestControllerCookie = true;
    private static HashSet<String> doNotFilterParamList = new HashSet();
    private static HashSet<String> doNotFilterAjaxRequests = new HashSet();
    private static HashSet<String> webserviceInternalActions = new HashSet();
    private static HashSet<String> webserviceInternalClasses = new HashSet();
    private static HashSet<String> actioncommandInternalActions = new HashSet();
    private static HashSet<String> actioncommandInternalClasses = new HashSet();
    private static HashSet<String> ajaxInternalActions = new HashSet();
    private static HashSet<String> ajaxInternalClasses = new HashSet();
    private static HashSet<String> remotejavaapiInternalActions = new HashSet();
    private static HashSet<String> remotejavaapiInternalClasses = new HashSet();
    private static HashSet<String> jsInjectionTagList = new HashSet();

    public static void setDatabaseSecurityPolicy(String connectionid, String databaseid) {
        try {
            PropertyTree propertyTree = new WebAdminProcessor(connectionid).getPropertyTree(SECURITY_POLICY);
            Configuration.setDatabaseSecurityPolicy(databaseid, propertyTree.getNodePropertyList(DEFAULT_POLICY_NODE, true), propertyTree.getNodePropertyList(VIRTUAL_POLICY_NODE, true), propertyTree.getNodePropertyList(PORTAL_POLICY_NODE, true));
        }
        catch (Exception e) {
            Trace.logError("Failed to load security policy for database '" + databaseid + "'. Reason: " + e.getMessage(), e);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static SapphireConnection getSapphireConnection(String command, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url;
        PropertyList sessionmanagement;
        SapphireConnection sapphireConnection = null;
        String connectionid = request.getParameter("connectionid");
        HttpUtil httpUtil = new HttpUtil(request, response);
        PropertyList policy = null;
        if (connectionid != null && connectionid.length() > 0) {
            policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), false);
            if (policy == null) throw new ServletException("Failed to get the security policy due to invalid connectionid");
            sessionmanagement = policy.getPropertyList("sessionmanagement");
            if (sessionmanagement != null && sessionmanagement.getProperty("allowconnectioninrequest", ACTIONPROCESSING_NONE).equals(ACTIONPROCESSING_NONE)) {
                SecurityPolicyUtil.handleConnectionViolation(connectionid, policy, "Connectionid in request from command=" + command + "&" + command + "=" + request.getParameter(command));
            }
            httpUtil.setCookieValue("connectionid", connectionid, false, httpOnlyRequestControllerCookie);
        } else {
            connectionid = httpUtil.getCookieValue("connectionid");
        }
        if (connectionid == null || connectionid.length() <= 0 || (sapphireConnection = new ConnectionProcessor(connectionid).getSapphireConnection()) == null || sapphireConnection.getDeleteDt() != null) return sapphireConnection;
        if (policy == null) {
            policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
        }
        if (policy == null || (sessionmanagement = policy.getPropertyList("sessionmanagement")) == null) return sapphireConnection;
        if (sessionmanagement.getProperty("matchconnectioninsession", "Y").equals("Y")) {
            String sessionconnectionid = (String)request.getSession().getAttribute("connectionid");
            if (!(command.equalsIgnoreCase("logoff") || command.equalsIgnoreCase("resetpassword") || command.equalsIgnoreCase("changepassword") || sessionconnectionid == null || sessionconnectionid.length() <= 0 || connectionid.equals(sessionconnectionid))) {
                SecurityPolicyUtil.handleConnectionViolation(connectionid, policy, "Connectionid mismatch");
            }
        }
        String connectFrom = sapphireConnection.getConnectFrom();
        String sessionbinding = sessionmanagement.getProperty("sessionbinding");
        if (connectFrom != null && connectFrom.length() > 0 && !connectFrom.equals(sessionbinding.equals("I") ? request.getRemoteAddr() : (sessionbinding.equals("H") ? request.getRemoteHost() : ""))) {
            SecurityPolicyUtil.handleConnectionViolation(connectionid, policy, (sessionbinding.equals("I") ? "IP" : (sessionbinding.equals("H") ? "Hostname" : "")) + " session binding mismatch");
        }
        if (!(!sessionmanagement.getProperty("matchuseragentinsession", ACTIONPROCESSING_NONE).equals("Y") || command.equalsIgnoreCase("logoff") || command.equalsIgnoreCase("resetpassword") || command.equalsIgnoreCase("changepassword") || request.getHeader("User-Agent").equals((String)request.getSession().getAttribute("connectionuseragent")))) {
            SecurityPolicyUtil.handleConnectionViolation(connectionid, policy, "UserAgent mismatch");
        }
        if (request.isSecure() || !sessionmanagement.getProperty("dedicatedconnectionid", "Y").equals("Y") || sapphireConnection.getTool().equalsIgnoreCase("RequestController") || SecurityPolicyUtil.checkIfEmpowerURL(request, url = request.getRequestURI() + "?" + request.getQueryString())) return sapphireConnection;
        SecurityPolicyUtil.handleConnectionViolation(connectionid, policy, "Connection not originated from standard logon:" + url);
        return sapphireConnection;
    }

    public static boolean checkIfEmpowerURL(HttpServletRequest request, String url) {
        String page;
        if (url.contains("command=ajax&ajaxclass=com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequestAjaxHandler") || url.contains("command=file&file=WEB-OPAL/pagetypes/misc/closewindow.html") || url.contains("command=image&file=WEB-CORE/images/png/SelectAndReturn.png") || url.contains("command=image&file=WEB-CORE/images/png/Cancel.png")) {
            Trace.logDebug("Check if empower url and lets it pass on." + url);
            return true;
        }
        return url.toLowerCase().contains("empower") ? (url.contains("command=page") ? url.contains("command=page&page=EmpowerDownload") || url.contains("command=page&page=SDIWIEmpowerLookup") || url.contains("command=page&page=EmpowerUpload") || url.contains("command=page&page=QCBEmpowerLookup") || url.contains("command=page&page=ReagentEmpowerLookup") : (url.contains("command=wizard") ? url.contains("command=wizard&wizard=WEB-CORE/modules/empower/wizard_download.xml") || url.contains("command=wizard&wizard=WEB-CORE/modules/empower/wizard_upload.xml") : (url.contains("command=file&file=WEB-CORE/modules/empower/") ? url.contains("command=file&file=WEB-CORE/modules/empower/wiz_download_page1.jsp") || url.contains("command=file&file=WEB-CORE/modules/empower/wiz_download_page3.jsp") || url.contains("command=file&file=WEB-CORE/modules/empower/wiz_download_page4.jsp") || url.contains("command=file&file=WEB-CORE/modules/empower/wiz_download_page2.jsp") || url.contains("command=file&file=WEB-CORE/modules/empower/wiz_upload_page1.jsp") || url.contains("command=file&file=WEB-CORE/modules/empower/wiz_upload_page2.jsp") || url.contains("command=file&file=WEB-CORE/modules/empower/wiz_upload_page3.jsp") : url.contains("command=ajax&ajaxclass=com.labvantage.sapphire.modules.empower") && (url.contains("command=ajax&ajaxclass=com.labvantage.sapphire.modules.empower.DownloadOptionsAjaxGetter") || url.contains("command=ajax&ajaxclass=com.labvantage.sapphire.modules.empower.DownloadMappingPageAreaAjaxRender") || url.contains("command=ajax&ajaxclass=com.labvantage.sapphire.modules.empower.SampleSetMethodAjaxUpdater") || url.contains("command=ajax&ajaxclass=com.labvantage.sapphire.modules.empower.SampleListAjaxGetter") || url.contains("command=ajax&ajaxclass=com.labvantage.sapphire.modules.empower.ReagentListAjaxGetter") || url.contains("command=ajax&ajaxclass=com.labvantage.sapphire.modules.empower.EmpowerUploadDataAjaxSetter") || url.contains("command=ajax&ajaxclass=com.labvantage.sapphire.modules.empower.BlockQCBatchAjaxUpdate"))))) : url.endsWith("rc?command=page") && ((page = request.getParameter("page")).equals("SDIWIEmpowerLookup") || page.equals("ReagentEmpowerLookup"));
    }

    public static boolean isConnectionidEncryptionEnabled(String databaseid, boolean virtualUser, boolean portalUser) {
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(databaseid, virtualUser, portalUser);
        if (policy != null) {
            PropertyList sessionmanagement = policy.getPropertyList("sessionmanagement");
            return sessionmanagement != null && sessionmanagement.getProperty("encryptconnectionid", ACTIONPROCESSING_NONE).equals("Y");
        }
        return false;
    }

    public static String getSessionBinding(String databaseid, boolean virtualUser, boolean portalUser, String remoteIP, String remoteHost) {
        PropertyList sessionmanagement;
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(databaseid, virtualUser, portalUser);
        if (policy != null && (sessionmanagement = policy.getPropertyList("sessionmanagement")) != null) {
            String sessionbinding = sessionmanagement.getProperty("sessionbinding");
            return sessionbinding.equals("I") ? remoteIP : (sessionbinding.equals("H") ? remoteHost : "");
        }
        return "";
    }

    public static void isWebServicesEnabled(String databaseid) throws SOAPException {
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(databaseid, false, false);
        if (policy != null) {
            PropertyList webservices = policy.getPropertyList(POLICYSECTION_WEBSERVICES);
            if (webservices.getProperty("enable", ACTIONPROCESSING_NONE).equals(ACTIONPROCESSING_NONE)) {
                throw new SOAPException("Failed to perform web service request. Reason: Web services disabled in security policy");
            }
        } else {
            throw new SOAPException("Failed to perform web service request. Reason: Web services policy not found");
        }
    }

    public static void checkWebServicesEnabled(String connectionid) throws SOAPException {
        SecurityPolicyUtil.checkWebServicesEnabled(connectionid, false);
    }

    public static void checkWebServicesEnabled(String connectionid, boolean isToken) throws SOAPException {
        ConnectionProcessor cp;
        if (connectionid != null && connectionid.length() > 0 && !(cp = new ConnectionProcessor(connectionid)).checkConnection(connectionid)) {
            throw new SOAPException("The connectionid you are using is not recognized.");
        }
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
        if (policy != null) {
            ConnectionProcessor cp2;
            PropertyList webservices = policy.getPropertyList(POLICYSECTION_WEBSERVICES);
            if (webservices.getProperty("enable", ACTIONPROCESSING_NONE).equals(ACTIONPROCESSING_NONE)) {
                throw new SOAPException("Failed to perform web service request. Reason: Web services disabled in security policy");
            }
            PropertyList sessionmanagement = policy.getPropertyList("sessionmanagement");
            if (!(sessionmanagement == null || !sessionmanagement.getProperty("dedicatedconnectionid", "Y").equals("Y") || (cp2 = new ConnectionProcessor(connectionid)).getSapphireConnection().getTool().equals("SapphireWS") || isToken && cp2.getSapphireConnection().getTool().equals("Internal"))) {
                throw new SOAPException("Failed to perform web service request. Reason: Connectionid not generated via web service call");
            }
        }
    }

    public static void checkRESTServicesEnabled(String connectionid, boolean connectionidfromrequest, boolean bypassDedicatedConnectionCheck, String scheme, String remotehostid, String remoteip) throws RestException {
        SecurityPolicyUtil.checkRESTServicesEnabled(connectionid, connectionidfromrequest, null, bypassDedicatedConnectionCheck, scheme, remotehostid, remoteip);
    }

    public static void checkRESTServicesEnabled(String databaseid, boolean bypassDedicatedConnectionCheck, String scheme, String remotehostid, String remoteip) throws RestException {
        SecurityPolicyUtil.checkRESTServicesEnabled(null, false, databaseid, bypassDedicatedConnectionCheck, scheme, remotehostid, remoteip);
    }

    private static void checkRESTServicesEnabled(String connectionid, boolean connectionidFromRequest, String databaseid, boolean bypassDedicatedConnectionCheck, String scheme, String remotehostid, String remoteip) throws RestException {
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(connectionid != null && connectionid.length() > 0 ? SecurityService.getDatabaseId(connectionid) : databaseid, SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
        if (policy != null) {
            PropertyList sessionmanagement;
            boolean allowHttp;
            PropertyList restservices = policy.getPropertyList(POLICYSECTION_RESTSERVICES);
            if (!Configuration.isJunitServer() && restservices.getProperty("enable", ACTIONPROCESSING_NONE).equals(ACTIONPROCESSING_NONE)) {
                throw new RestException(404, "Resource not available", "REST services disabled in security policy");
            }
            boolean bl = allowHttp = Configuration.isJunitServer() || restservices.getProperty("enforcessl", "Y").equals(ACTIONPROCESSING_NONE);
            if (!allowHttp && !scheme.equalsIgnoreCase("https")) {
                throw new RestException(400, "Malformed request URL", "Request not using required HTTPS schema");
            }
            PropertyListCollection hostaccesslist = restservices.getCollection("hostaccesslist");
            if (hostaccesslist != null && hostaccesslist.size() > 0) {
                boolean permitted = false;
                for (int i = 0; !permitted && i < hostaccesslist.size(); ++i) {
                    PropertyList host = hostaccesslist.getPropertyList(i);
                    if (host.getProperty("type").equals("I") && remoteip != null && remoteip.equals(host.getProperty("value"))) {
                        permitted = true;
                        continue;
                    }
                    if (!host.getProperty("type").equals("H") || remotehostid == null || !remotehostid.equals(host.getProperty("value"))) continue;
                    permitted = true;
                }
                if (!permitted) {
                    throw new RestException(401, "Resource not available", "Request from an unrecognized host");
                }
            }
            if (connectionid != null && connectionid.length() > 0 && (sessionmanagement = policy.getPropertyList("sessionmanagement")) != null) {
                ConnectionProcessor cp;
                SapphireConnection sapphireConnection;
                if (connectionidFromRequest && sessionmanagement.getProperty("allowconnectioninrequest", ACTIONPROCESSING_NONE).equals(ACTIONPROCESSING_NONE)) {
                    throw new RestException(401, "Missing, invalid or timed out connectionid", "Request contains an illegal connectionid or token query parameter");
                }
                if (!bypassDedicatedConnectionCheck && sessionmanagement.getProperty("dedicatedconnectionid", "Y").equals("Y") && (sapphireConnection = (cp = new ConnectionProcessor(connectionid)).getSapphireConnection()) != null) {
                    if (!(Configuration.isJunitServer() || sapphireConnection.getTool().equalsIgnoreCase("SapphireREST") || sapphireConnection.getExternalAppId() != null && sapphireConnection.getExternalAppId().length() != 0)) {
                        throw new RestException(401, "Missing, invalid or timed out connectionid", "Connectionid not generated via REST service call");
                    }
                    String connectFrom = sapphireConnection.getConnectFrom();
                    String sessionbinding = sessionmanagement.getProperty("sessionbinding");
                    if (connectFrom != null && connectFrom.length() > 0 && !connectFrom.equals(sessionbinding.equals("I") ? remoteip : (sessionbinding.equals("H") ? remotehostid : ""))) {
                        throw new RestException(401, "Missing, invalid or timed out connectionid", (sessionbinding.equals("I") ? "IP" : (sessionbinding.equals("H") ? "Hostname" : "")) + " session binding mismatch");
                    }
                }
            }
        }
    }

    public static void checkWebServicesSessionManagement(String connectionid, String remotehostid, String remoteip) throws SOAPException {
        ConnectionProcessor cp;
        SapphireConnection sapphireConnection;
        PropertyList sessionmanagement;
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
        if (policy != null && (sessionmanagement = policy.getPropertyList("sessionmanagement")) != null && (sapphireConnection = (cp = new ConnectionProcessor(connectionid)).getSapphireConnection()) != null) {
            String connectFrom = sapphireConnection.getConnectFrom();
            String sessionbinding = sessionmanagement.getProperty("sessionbinding");
            if (connectFrom != null && connectFrom.length() > 0 && !connectFrom.equals(sessionbinding.equals("I") ? remoteip : (sessionbinding.equals("H") ? remotehostid : ""))) {
                throw new SOAPException("Failed to perform web service request. Reason: " + (sessionbinding.equals("I") ? "IP" : (sessionbinding.equals("H") ? "Hostname" : "")) + " session binding mismatch");
            }
        }
    }

    public static boolean isUnregisteredSQLPermitted(String connectionid, String policysection, String caller, String sql) {
        boolean permitted = true;
        try {
            PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
            if (policy != null) {
                PropertyList section = policy.getPropertyList(policysection);
                permitted = section.getProperty("allowunregisteredsql", ACTIONPROCESSING_NONE).equals("Y");
                if (Configuration.isJunitServer() && !permitted) {
                    permitted = true;
                } else if (!permitted) {
                    SecurityPolicyUtil.handleUnregisteredSQLViolation(connectionid, policy, caller, sql, SecurityPolicyUtil.getViolation(policysection));
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to access security policy. Reason: " + e.getMessage(), e);
        }
        return permitted;
    }

    public static boolean allowPageDirectives(String connectionid, String policysection) {
        boolean permitted = true;
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
        if (policy != null) {
            PropertyList section = policy.getPropertyList(policysection);
            permitted = section.getProperty("allowpagedirectives", "Y").equals("Y");
        }
        return permitted;
    }

    public static boolean isAllowedQueryWhere(ServletRequest request, String querywhere) {
        boolean permitted = true;
        try {
            if (request != null) {
                PropertyList querywherefilter;
                PropertyList policy;
                RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
                String connectionid = "";
                if (requestContext != null) {
                    connectionid = requestContext.getConnectionId();
                }
                if ((policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid))) != null && !(permitted = SecurityPolicyUtil.isQueryWherePermitted(querywherefilter = policy.getPropertyList("querywherefilter"), querywhere))) {
                    ConnectionInfo connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
                    if (connectionInfo.hasRole("Administrator")) {
                        permitted = true;
                    } else {
                        SecurityPolicyUtil.handleUnregisteredSQLViolation(connectionid, policy, "", querywhere, "Not Allowed Query Where");
                    }
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to access security policy. Reason: " + e.getMessage(), e);
        }
        return permitted;
    }

    public static boolean isQueryWherePermitted(PropertyList querywherefilter, String querywhere) {
        boolean permitted = true;
        if (querywherefilter != null && "Y".equals(querywherefilter.getProperty("enable")) && querywhere.trim().length() > 0) {
            boolean isInWhiteList = false;
            PropertyListCollection whitelist = querywherefilter.getCollectionNotNull("whitelist");
            for (int i = 0; i < whitelist.size(); ++i) {
                if (!querywhere.trim().equalsIgnoreCase(whitelist.getPropertyList(i).getProperty("expression"))) continue;
                isInWhiteList = true;
                break;
            }
            if (!isInWhiteList) {
                String[] literals = StringUtil.getTokens(querywhere, "'", "'");
                for (int i = 0; i < literals.length; ++i) {
                    querywhere = StringUtil.replaceAll(querywhere, "'" + literals[i] + "'", "");
                }
                PropertyListCollection blacklist = querywherefilter.getCollectionNotNull("blacklist");
                for (int i = 0; i < blacklist.size(); ++i) {
                    String expression = blacklist.getPropertyList(i).getProperty("expression");
                    if (expression.length() <= 0 || querywhere.toLowerCase().indexOf(expression.toLowerCase()) < 0) continue;
                    if (expression.trim().toLowerCase().equals("like")) {
                        permitted = false;
                        break;
                    }
                    if (querywhere.substring(querywhere.toLowerCase().indexOf(expression.toLowerCase()) + expression.length()).trim().indexOf("(") != 0) continue;
                    permitted = false;
                    break;
                }
            }
        }
        return permitted;
    }

    public static boolean isValidParam(String param) {
        return doNotFilterParamList.contains(param);
    }

    public static boolean isSafeAjaxRequest(String className) {
        return doNotFilterAjaxRequests.contains(className != null ? className : "");
    }

    public static boolean isActionPermitted(String connectionid, String policysection, String actionid, HashMap actionProps) {
        return SecurityPolicyUtil.isActionPermitted(connectionid, policysection, "actionprocessing", actionid, actionProps);
    }

    public static boolean isActionPermitted(String connectionid, String policysection, String actionprocessingproperty, String actionid, HashMap actionProps) {
        boolean permitted;
        block11: {
            permitted = true;
            try {
                HashSet<String> systemPermittedActions = SecurityPolicyUtil.getSystemPermittedActions(policysection);
                if (SecurityPolicyUtil.isSystemPermittedAction(actionid, actionProps, systemPermittedActions)) break block11;
                ConnectionProcessor cp = new ConnectionProcessor(connectionid);
                SapphireConnection sapphireConnection = cp.getSapphireConnection();
                if (sapphireConnection == null) {
                    return false;
                }
                PropertyList policy = Configuration.getDatabaseSecurityPolicy(sapphireConnection.getDatabaseId(), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
                if (policy == null) break block11;
                PropertyList section = policy.getPropertyList(policysection);
                String actionprocessing = section.getProperty(actionprocessingproperty, ACTIONPROCESSING_FULL);
                boolean bl = permitted = !actionprocessing.equals(ACTIONPROCESSING_NONE);
                if (actionprocessing.equals(ACTIONPROCESSING_RESTICTED)) {
                    permitted = false;
                    PropertyListCollection permittedactions = section.getCollection("permittedactions");
                    for (int i = 0; !permitted && i < permittedactions.size(); ++i) {
                        if (!permittedactions.getPropertyList(i).getProperty("actionid").equals(actionid)) continue;
                        permitted = true;
                    }
                } else if (actionprocessing.equals(ACTIONPROCESSING_ROLE)) {
                    permitted = false;
                    ArrayList<String> roles = (ArrayList<String>)CacheUtil.get(sapphireConnection.getDatabaseId(), "ActionRoles", actionid);
                    if (roles == null) {
                        roles = new ArrayList<String>();
                        QueryProcessor qp = new QueryProcessor(connectionid);
                        DataSet actionRoles = qp.getPreparedSqlDataSet("SELECT roleid FROM sdirole WHERE sdcid = ? AND keyid1 = ?", new Object[]{"Action", actionid});
                        for (int i = 0; i < actionRoles.size(); ++i) {
                            roles.add(actionRoles.getValue(i, "roleid"));
                        }
                        CacheUtil.put(sapphireConnection.getDatabaseId(), "ActionRoles", actionid, roles);
                    }
                    for (int i = 0; !permitted && i < roles.size(); ++i) {
                        if (!sapphireConnection.hasRole((String)roles.get(i))) continue;
                        permitted = true;
                    }
                }
                if (!permitted) {
                    SecurityPolicyUtil.handleActionViolation(connectionid, policy, actionid, SecurityPolicyUtil.getViolation(policysection));
                }
            }
            catch (Exception e) {
                Trace.logError("Failed to access security policy. Reason: " + e.getMessage(), e);
            }
        }
        return permitted;
    }

    private static boolean isSystemPermittedAction(String actionid, HashMap actionProps, HashSet<String> systemPermittedActions) {
        if (systemPermittedActions.contains(actionid)) {
            return true;
        }
        return actionProps != null && actionProps.containsKey("sdcid") && systemPermittedActions.contains(actionid + ";" + actionProps.get("sdcid"));
    }

    public static boolean isActionClassPermitted(String connectionid, String policysection, String actionclass) {
        boolean permitted = true;
        try {
            PropertyList section;
            PropertyList policy;
            HashSet<String> systemPermittedClasses = SecurityPolicyUtil.getSystemPermittedClasses(policysection);
            if (!systemPermittedClasses.contains(actionclass) && (policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid))) != null && !(permitted = (section = policy.getPropertyList(policysection)).getProperty("allowactionclassprocessing", "Y").equals("Y"))) {
                SecurityPolicyUtil.handleActionClassViolation(connectionid, policy, actionclass, SecurityPolicyUtil.getViolation(policysection));
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to access security policy. Reason: " + e.getMessage(), e);
        }
        return permitted;
    }

    public static boolean isActionBlockPermitted(String connectionid, String policysection, String actionprocessingproperty, ActionBlock actionBlock) {
        boolean permitted;
        block11: {
            permitted = true;
            try {
                HashSet<String> systemPermittedActions = SecurityPolicyUtil.getSystemPermittedActions(policysection);
                List<String> abActions = actionBlock.getDistinctActions();
                PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
                if (policy == null) break block11;
                PropertyList section = policy.getPropertyList(policysection);
                String actionprocessing = section.getProperty(actionprocessingproperty, ACTIONPROCESSING_FULL);
                String allowactionclassprocessing = section.getProperty("allowactionclassprocessing", "Y");
                if (allowactionclassprocessing.equals(ACTIONPROCESSING_NONE)) {
                    List<String> abActionClasses = actionBlock.getDistinctActionClasses();
                    if (abActionClasses.size() > 0 && abActionClasses.get(0).length() > 0) {
                        permitted = SecurityPolicyUtil.handleActionClassViolation(connectionid, policy, abActionClasses.get(0), SecurityPolicyUtil.getViolation(policysection));
                    }
                    break block11;
                }
                String restrictedAction = "";
                if (actionprocessing.equals(ACTIONPROCESSING_NONE)) {
                    for (int i = 0; permitted && i < abActions.size(); ++i) {
                        String actionid = abActions.get(i);
                        if (SecurityPolicyUtil.isSystemPermittedAction(actionid, null, systemPermittedActions)) continue;
                        restrictedAction = actionid;
                        permitted = false;
                    }
                } else if (actionprocessing.equals(ACTIONPROCESSING_RESTICTED)) {
                    int i;
                    HashSet<String> permittedActions = new HashSet<String>();
                    PropertyListCollection permittedactions = section.getCollection("permittedactions");
                    for (i = 0; i < permittedactions.size(); ++i) {
                        permittedActions.add(permittedactions.getPropertyList(i).getProperty("actionid"));
                    }
                    for (i = 0; permitted && i < abActions.size(); ++i) {
                        if (systemPermittedActions.contains(abActions.get(i)) || permittedActions.contains(abActions.get(i))) continue;
                        restrictedAction = abActions.get(i);
                        permitted = false;
                    }
                }
                if (!permitted) {
                    SecurityPolicyUtil.handleActionViolation(connectionid, policy, restrictedAction, SecurityPolicyUtil.getViolation(policysection));
                }
            }
            catch (Exception e) {
                Trace.logError("Failed to access security policy. Reason: " + e.getMessage(), e);
            }
        }
        return permitted;
    }

    public static boolean isJavaAttachmentsPermitted(String connectionid, String area) {
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
        if (policy != null) {
            PropertyList javaattachments = policy.getPropertyList("javaattachments");
            return javaattachments != null && javaattachments.getProperty(area, ACTIONPROCESSING_NONE).equals("Y");
        }
        return false;
    }

    public static boolean isRunAntPermitted(String connectionid) {
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
        if (policy != null) {
            PropertyList runant = policy.getPropertyList("runant");
            return runant == null || !runant.getProperty("runantenabled", "Y").equals(ACTIONPROCESSING_NONE);
        }
        return true;
    }

    public static boolean isServerSideBrowsingPermitted(String connectionid) {
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
        if (policy != null) {
            PropertyList filebrowser = policy.getPropertyList("filebrowser");
            return filebrowser == null || !filebrowser.getProperty("filebrowsingenabled", "Y").equals(ACTIONPROCESSING_NONE);
        }
        return true;
    }

    public static boolean canManageJavaAttachments(String databaseid, boolean virtualUser, boolean portalUser, ArrayList<String> roleList, String area) {
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(databaseid, virtualUser, portalUser);
        if (policy != null) {
            String role;
            PropertyList javaattachments = policy.getPropertyList("javaattachments");
            String string = role = javaattachments != null ? javaattachments.getProperty("roleid", "") : "";
            if (role.length() > 0) {
                if (roleList.contains(role)) {
                    if (area.equalsIgnoreCase("appresource")) {
                        return true;
                    }
                    return javaattachments != null && javaattachments.getProperty(area, ACTIONPROCESSING_NONE).equals("Y");
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private static String getViolation(String policysection) {
        return policysection.equals(POLICYSECTION_RESTSERVICES) ? "REST Services" : (policysection.equals(POLICYSECTION_WEBSERVICES) ? "Web Services" : (policysection.equals(POLICYSECTION_ACTIONCOMMAND) ? "Action Command" : (policysection.equals(POLICYSECTION_AJAX) ? "AJAX Request" : (policysection.equals(POLICYSECTION_REMOTEJAVAAPI) ? "Remote Java API" : "Unknown"))));
    }

    private static HashSet<String> getSystemPermittedActions(String policysection) {
        return policysection.equals(POLICYSECTION_WEBSERVICES) ? webserviceInternalActions : (policysection.equals(POLICYSECTION_ACTIONCOMMAND) ? actioncommandInternalActions : (policysection.equals(POLICYSECTION_AJAX) ? ajaxInternalActions : (policysection.equals(POLICYSECTION_REMOTEJAVAAPI) ? remotejavaapiInternalActions : new HashSet())));
    }

    private static HashSet<String> getSystemPermittedClasses(String policysection) {
        return policysection.equals(POLICYSECTION_WEBSERVICES) ? webserviceInternalClasses : (policysection.equals(POLICYSECTION_ACTIONCOMMAND) ? actioncommandInternalClasses : (policysection.equals(POLICYSECTION_AJAX) ? ajaxInternalClasses : (policysection.equals(POLICYSECTION_REMOTEJAVAAPI) ? remotejavaapiInternalClasses : new HashSet())));
    }

    private static boolean handleConnectionViolation(String connectionid, PropertyList policy, String violation) throws Exception {
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        SapphireConnection sapphireConnection = cp.getSapphireConnection();
        if (sapphireConnection != null && !SecurityPolicyUtil.handleViolation(connectionid, policy, "SESSION MANAGEMENT VIOLATION\tConnection\tViolation: " + violation + "\t\t\t", new SimpleDateFormat().format(DateTimeUtil.getNowCalendar().getTime()) + ": SESSION MANAGEMENT VIOLATION by '" + sapphireConnection.getSysuserId() + "' (" + connectionid + ") on database '" + sapphireConnection.getDatabaseId() + "' - violation is " + violation + ").")) {
            throw new ServletException("Request denied by server - security policy violation!");
        }
        return true;
    }

    private static boolean handleActionViolation(String connectionid, PropertyList policy, String actionid, String violation) throws Exception {
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        SapphireConnection sapphireConnection = cp.getSapphireConnection();
        if (sapphireConnection != null) {
            SecurityPolicyUtil.handleViolation(connectionid, policy, violation.toUpperCase() + " VIOLATION\tProcessAction\tAction is restricted\tactionid=" + actionid + "\t\t", new SimpleDateFormat().format(DateTimeUtil.getNowCalendar().getTime()) + ": " + violation.toUpperCase() + " VIOLATION by '" + sapphireConnection.getSysuserId() + "' (" + connectionid + ") on database '" + sapphireConnection.getDatabaseId() + "' - Action is restricted (actionid=" + actionid + ").");
        }
        return false;
    }

    private static boolean handleActionClassViolation(String connectionid, PropertyList policy, String actionclass, String violation) throws Exception {
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        SapphireConnection sapphireConnection = cp.getSapphireConnection();
        if (sapphireConnection != null) {
            SecurityPolicyUtil.handleViolation(connectionid, policy, violation.toUpperCase() + " VIOLATION\tProcessActionClass\tAction class processing is restricted\tactionclass=" + actionclass + "\t\t", new SimpleDateFormat().format(DateTimeUtil.getNowCalendar().getTime()) + ": " + violation.toUpperCase() + " VIOLATION by '" + sapphireConnection.getSysuserId() + "' (" + connectionid + ") on database '" + sapphireConnection.getDatabaseId() + "' - Action class processing is restricted (actionclass=" + actionclass + ").");
        }
        return false;
    }

    private static boolean handleUnregisteredSQLViolation(String connectionid, PropertyList policy, String caller, String sql, String violation) throws Exception {
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        SapphireConnection sapphireConnection = cp.getSapphireConnection();
        if (sapphireConnection != null) {
            SecurityPolicyUtil.handleViolation(connectionid, policy, violation.toUpperCase() + " VIOLATION\tUnregisteredSQL\tUnregistered SQL \"" + sql + "\" is restricted\tcaller=" + caller + "\t\t", new SimpleDateFormat().format(DateTimeUtil.getNowCalendar().getTime()) + ": " + violation.toUpperCase() + " VIOLATION by '" + sapphireConnection.getSysuserId() + "' (" + connectionid + ") on database '" + sapphireConnection.getDatabaseId() + "' - Unregistered SQL \"" + sql + "\" is restricted (caller=" + caller + ").");
        }
        return false;
    }

    public static boolean handleFilterViolation(String connectionid, PropertyList policy, boolean sanitizeRequest, HttpServletRequest request, String parameter, String reason, String input) throws ServletException {
        boolean ignore = true;
        if (connectionid != null && connectionid.length() > 0) {
            ConnectionProcessor cp = new ConnectionProcessor(connectionid);
            SapphireConnection sapphireConnection = cp.getSapphireConnection();
            if (sapphireConnection != null) {
                String command = SecurityPolicyUtil.getCommand(request);
                ignore = SecurityPolicyUtil.handleViolation(connectionid, policy, "REQUEST FILTER VIOLATION\t" + parameter + "\t" + reason + "\t" + parameter + "=" + input + "\t" + command + "\t" + SecurityPolicyUtil.getContext(command, request), new SimpleDateFormat().format(DateTimeUtil.getNowCalendar().getTime()) + ": REQUEST FILTER VIOLATION by '" + sapphireConnection.getSysuserId() + "' (" + connectionid + ") on database '" + sapphireConnection.getDatabaseId() + "' - " + reason + " (" + parameter + "=" + input + ")." + (sanitizeRequest ? "\n\nViolation sanitized as per policy" : ""));
                if (!ignore && !sanitizeRequest) {
                    throw new ServletException(reason + ". Contact your administrator for details");
                }
            }
        } else {
            throw new ServletException(input + " " + reason + ". Contact your administrator for details");
        }
        return ignore;
    }

    public static boolean handleViolation(String connectionid, PropertyList policy, String logMsg, String emailMsg) {
        boolean ignore = false;
        PropertyList violationhandling = policy.getPropertyList(VIOLATIONHANDLING);
        PropertyList notification = violationhandling.getPropertyList(VIOLATIONNOTIFICATION);
        Trace.logError(logMsg);
        if (notification.getPropertyList("email").getProperty("enable").equals("Y")) {
            ActionProcessor ap = new ActionProcessor(connectionid);
            try {
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("to", notification.getPropertyList("email").getProperty("emailto"));
                props.put("from", notification.getPropertyList("email").getProperty("emailfrom"));
                props.put("subject", "Security Policy Violation");
                props.put("message", emailMsg + (ignore ? "\n\nViolation ignored as per policy" : ""));
                ap.processAction("SendMail", "1", props);
            }
            catch (ActionException e) {
                Trace.logError("Failed to send security policy violation email. Reason: " + e.getMessage(), e);
            }
        }
        return ignore;
    }

    private static String getCommand(HttpServletRequest request) {
        Enumeration params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement().toString();
            if (!param.equals("command")) continue;
            return request.getParameter(param);
        }
        return "";
    }

    private static String getContext(String command, HttpServletRequest request) {
        Enumeration params = request.getParameterNames();
        return request.getQueryString();
    }

    private static HashSet<String> getJsInjections(PropertyListCollection jsInjections) {
        HashSet<String> jsInjectionList = new HashSet<String>();
        if (jsInjections != null) {
            for (int i = 0; i < jsInjections.size(); ++i) {
                String inject = jsInjections.getPropertyList(i).getProperty("jsinjection");
                if (inject.length() <= 0) continue;
                jsInjectionList.add(inject);
            }
            jsInjectionList.add("javascript:");
        }
        return jsInjectionList;
    }

    public static boolean hasJSTags(ServletRequest request, String value) {
        try {
            if (request != null) {
                PropertyList policy;
                RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
                String connectionid = "";
                if (requestContext != null) {
                    connectionid = requestContext.getConnectionId();
                }
                if ((policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), false)) != null) {
                    PropertyList requestfilter = policy.getPropertyList("requestfilter");
                    jsInjectionTagList = SecurityPolicyUtil.getJsInjections(requestfilter.getCollection("jsinjections"));
                    return SecurityPolicyUtil.jsTagPresent(value);
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to access security policy. Reason: " + e.getMessage(), e);
        }
        return false;
    }

    public static boolean hasJSTags(String connectionId, String value) {
        boolean hasJSTags = false;
        try {
            PropertyList policy;
            if (connectionId != null && OpalUtil.isNotEmpty(connectionId) && (policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionId), SecurityService.isVirtualUser(connectionId), false)) != null) {
                PropertyList requestfilter = policy.getPropertyList("requestfilter");
                jsInjectionTagList = SecurityPolicyUtil.getJsInjections(requestfilter.getCollection("jsinjections"));
                hasJSTags = SecurityPolicyUtil.jsTagPresent(policy, connectionId, value);
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to access security policy. Reason: " + e.getMessage(), e);
        }
        return hasJSTags;
    }

    public static boolean jsTagPresent(PropertyList policy, String connectionid, String original) throws SapphireException {
        ConnectionInfo connectionInfo;
        boolean jsTagPresent = false;
        if (connectionid != null && connectionid.length() > 0 && (connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid)) != null) {
            String temp = original.toLowerCase();
            String found = "";
            for (String key : jsInjectionTagList) {
                if (!temp.contains(key)) continue;
                found = key;
                break;
            }
            if (found.length() > 0) {
                int pos = temp.indexOf(found);
                jsTagPresent = true;
                if (pos > 0 && found.startsWith("on")) {
                    jsTagPresent = Character.isWhitespace(temp.charAt(pos - 1));
                }
                if (jsTagPresent) {
                    String body = pos > 100 ? "..." + temp.substring(pos - 100) : temp;
                    body = body.length() > 201 ? body.substring(0, 200) + "..." : body;
                    String message = "Found '" + found + "' in request: " + body;
                    SecurityPolicyUtil.handleViolation(connectionid, policy, "REQUEST FILTER VIOLATION\t" + message, new SimpleDateFormat().format(DateTimeUtil.getNowCalendar().getTime()) + ": REQUEST FILTER VIOLATION by '" + connectionInfo.getSysuserId() + "' (" + connectionid + ") on database '" + connectionInfo.getDatabaseId() + "' - " + message);
                }
            }
        }
        return jsTagPresent;
    }

    private static boolean jsTagPresent(String value) {
        for (String tag : jsInjectionTagList) {
            if (tag.length() <= 0 || !value.toLowerCase().contains(tag.toLowerCase())) continue;
            if (tag.trim().toLowerCase().indexOf("on") == 0) {
                return value.indexOf("=") > 0;
            }
            return true;
        }
        return false;
    }

    public static boolean isFileLocationPolicyEnforcedForAction(String connectionid) {
        PropertyList policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionid), SecurityService.isVirtualUser(connectionid), SecurityService.isPortalUser(connectionid));
        if (policy != null) {
            PropertyList filebrowser = policy.getPropertyList("filebrowser");
            return filebrowser == null || !filebrowser.getProperty("enforcefilelocationpolicyforaction", "Y").equals(ACTIONPROCESSING_NONE);
        }
        return true;
    }

    static {
        doNotFilterParamList.add("(poll)");
        doNotFilterParamList.add("(return)");
        doNotFilterParamList.add("formlayout");
        doNotFilterParamList.add("__list");
        doNotFilterParamList.add("commandrequest");
        doNotFilterAjaxRequests.add(DocumentAjaxHandler.class.getName());
        doNotFilterAjaxRequests.add(FormBuilderSaveAjaxConverter.class.getName());
        doNotFilterAjaxRequests.add(FormBuilderFormletAjaxMerger.class.getName());
        doNotFilterAjaxRequests.add(SDITemp.class.getName());
        doNotFilterAjaxRequests.add(NavigatorRequest.class.getName());
        doNotFilterAjaxRequests.add(DynamicLookupRequest.class.getName());
        actioncommandInternalActions.add("EditSDI;AdhocQuery");
        actioncommandInternalActions.add("EditSDI;ReportEvent");
        actioncommandInternalClasses.add(GenerateDeleteScript.class.getName());
        actioncommandInternalClasses.add(ReprintReport.class.getName());
        actioncommandInternalClasses.add(GenerateReport.class.getName());
        ajaxInternalActions.add("AddPackage");
        ajaxInternalActions.add("ApproveBiobankSample");
        ajaxInternalActions.add("SetSDIVersionStatus");
        ajaxInternalActions.add("AllocateTaskQueueItems");
    }
}

