/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.rest;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SapphireService;
import com.labvantage.sapphire.servlet.externalapp.ExternalAppConstants;
import com.labvantage.sapphire.servlet.externalapp.ExternalAuthenticationUtil;
import com.labvantage.sapphire.servlet.rest.ActionsNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.ApiNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.ConnectionsNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.RestConstants;
import com.labvantage.sapphire.servlet.rest.RestException;
import com.labvantage.sapphire.servlet.rest.RootNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.SdcNameSpaceHandler;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseNameSpaceHandler
implements RestConstants,
ExternalAppConstants,
CacheNames {
    public static final String REST_POLICY = "RESTPolicy";
    public static final String DEFAULT_POLICY_NODE = "Sapphire Custom";
    public static boolean allowCookies = true;
    private String restPolicyNodeid = "Sapphire Custom";
    private static String LOGHEADER = "REST";
    private static HashMap<String, PropertyTree> policyCache = new HashMap();
    private static HashMap<String, HashMap<String, DataSet>> databaseActionReturns = new HashMap();
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected JSONObject jsonRequest;
    private JSONObject jsonResponse = new JSONObject();
    protected SapphireConnection sapphireConnection;
    protected String servletName;
    private String[] nameSpaceSegments;
    private String nameSpaceRootURL;
    protected boolean isJUnit = false;
    private SDCProcessor sdcProcessor;
    private SDIProcessor sdiProcessor;
    private ActionProcessor actionProcessor;
    private QueryProcessor queryProcessor;

    public static void setDatabaseRESTPolicy(String connectionid, String databaseid) {
        try {
            PropertyTree propertyTree = new WebAdminProcessor(connectionid).getPropertyTree(REST_POLICY);
            policyCache.put(databaseid, propertyTree);
            databaseActionReturns.put(databaseid, null);
            ActionsNameSpaceHandler.policyChange(databaseid);
            SdcNameSpaceHandler.policyChange(databaseid);
        }
        catch (Exception e) {
            Trace.logError("Failed to load REST policy for database '" + databaseid + "'. Reason: " + e.getMessage(), e);
        }
    }

    public PropertyList getPolicyPropertyList() {
        PropertyTree propertyTree = policyCache.get(this.sapphireConnection.getDatabaseId());
        if (propertyTree == null) {
            BaseNameSpaceHandler.setDatabaseRESTPolicy(this.sapphireConnection.getConnectionId(), this.sapphireConnection.getDatabaseId());
            propertyTree = policyCache.get(this.sapphireConnection.getDatabaseId());
        }
        PropertyList props = null;
        try {
            props = propertyTree.getNodePropertyList(this.restPolicyNodeid, true);
        }
        catch (Exception e1) {
            try {
                this.restPolicyNodeid = DEFAULT_POLICY_NODE;
                props = propertyTree.getNodePropertyList(DEFAULT_POLICY_NODE, true);
            }
            catch (Exception e) {
                Trace.logError("Failed to load REST policy for database 'databaseid'. Reason: " + e.getMessage(), e);
            }
        }
        return props;
    }

    public PropertyList getActionOutput(String actionid, PropertyList actionProps) {
        DataSet actionReturn;
        HashMap<String, DataSet> actionReturns = databaseActionReturns.get(this.sapphireConnection.getDatabaseId());
        if (actionReturns == null) {
            actionReturns = new HashMap();
            databaseActionReturns.put(this.sapphireConnection.getDatabaseId(), actionReturns);
        }
        if ((actionReturn = actionReturns.get(actionid)) == null) {
            actionReturn = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM actionproperty WHERE actionid = ? AND actionversionid = ? AND propertytypeflag in ( 'O', 'B' ) ORDER BY usersequence", new Object[]{actionid, 1});
            actionReturns.put(actionid, actionReturn);
        }
        PropertyList output = new PropertyList();
        for (int i = 0; i < actionReturn.size(); ++i) {
            String propertyid = actionReturn.getValue(i, "propertyid");
            if (actionProps.getProperty(propertyid).length() <= 0) continue;
            output.setProperty(propertyid, actionProps.getProperty(propertyid));
        }
        return output;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public static BaseNameSpaceHandler getInstance(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String servletPath = request.getServletPath();
        String contextPath = request.getContextPath();
        String scheme = request.getScheme();
        String uri = request.getRequestURI();
        StringBuffer url = request.getRequestURL();
        String[] pathInfoParts = null;
        String className = null;
        String rootURL = null;
        String servletName = servletPath.startsWith("/") ? servletPath.substring(1) : servletPath;
        String pathInfo = uri.substring((contextPath + servletName).length() + 1);
        String forceExternalAppId = "";
        Trace.logInfo(LOGHEADER, request.getMethod() + ": " + pathInfo);
        if (pathInfo != null && pathInfo.length() > 0 && !pathInfo.equals("/")) {
            pathInfoParts = StringUtil.split(pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo, "/");
            if (pathInfoParts.length >= 1) {
                className = "com.labvantage.sapphire.servlet.rest." + StringUtil.initCaps(pathInfoParts[0].toLowerCase()) + "NameSpaceHandler";
                String servletPlusNameSpace = servletName + "/" + pathInfoParts[0];
                rootURL = url.substring(0, url.indexOf(servletPlusNameSpace) + servletPlusNameSpace.length());
            }
        } else {
            className = RootNameSpaceHandler.class.getName();
            rootURL = url.toString();
        }
        JSONObject jsonRequest = null;
        String contentType = request.getContentType();
        Trace.logInfo(LOGHEADER, "Content Type: " + contentType);
        if (contentType != null && (contentType.toLowerCase().contains("text/plain") || contentType.toLowerCase().contains("application/json"))) {
            StringBuilder sb = new StringBuilder();
            request.setCharacterEncoding("UTF-8");
            try (BufferedReader reader = request.getReader();){
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            if (sb.toString().startsWith("{")) {
                try {
                    jsonRequest = new JSONObject(sb.toString());
                }
                catch (Exception e) {
                    throw new RestException(400, "Malformed request body", "Failed to parse JSON request body");
                }
            }
        }
        if (jsonRequest == null) {
            jsonRequest = new JSONObject();
        }
        String authorization = request.getHeader("Authorization");
        String authorizationValue = "";
        if (authorization != null && authorization.toLowerCase().startsWith("token ")) {
            authorizationValue = authorization.substring(6);
        }
        String databaseid = request.getParameter("databaseid") != null ? request.getParameter("databaseid") : jsonRequest.optString("databaseid");
        String connectionid = null;
        boolean connectionidFromRequest = false;
        if (authorizationValue.length() == 0 || !ExternalAuthenticationUtil.isToken(authorizationValue)) {
            connectionid = request.getParameter("connectionid");
            if (connectionid != null && connectionid.length() > 0) {
                connectionidFromRequest = true;
            }
            if ((connectionid == null || connectionid.length() == 0) && authorizationValue.length() > 0) {
                connectionid = authorizationValue;
            }
            if ((connectionid == null || connectionid.length() == 0) && allowCookies) {
                HttpUtil httpUtil = new HttpUtil(request, response);
                connectionid = httpUtil.getCookieValue("connectionid");
            }
        }
        if (connectionid == null || connectionid.length() == 0) {
            String tokenValue = request.getParameter("token");
            if (tokenValue != null && tokenValue.length() > 0) {
                connectionidFromRequest = true;
            }
            if ((tokenValue == null || tokenValue.length() == 0) && authorizationValue.length() > 0) {
                tokenValue = authorizationValue;
            }
            if ((tokenValue == null || tokenValue.length() == 0) && allowCookies) {
                HttpUtil httpUtil = new HttpUtil(request, response);
                tokenValue = httpUtil.getCookieValue("token");
            }
            if (tokenValue != null && tokenValue.length() > 0) {
                String internalConnectionid;
                try {
                    internalConnectionid = SapphireService.getInternalConnectionIdForToken(tokenValue);
                }
                catch (SapphireException e) {
                    throw new RestException(401, "Missing, invalid or timed out connectionid", "Unable to validate token " + tokenValue);
                }
                SapphireConnection internalSapphireConnection = new ConnectionProcessor(internalConnectionid).getSapphireConnection();
                databaseid = internalSapphireConnection.getDatabaseId();
                QueryProcessor internalQP = new QueryProcessor(internalConnectionid);
                ConnectionProcessor internalCP = new ConnectionProcessor(internalConnectionid);
                DataSet tokenDS = ExternalAuthenticationUtil.getCachedTokenDataSet(tokenValue, databaseid, internalQP);
                String authtokenid = tokenDS.getValue(0, "authtokenid");
                if (!tokenDS.getValue(0, "tokenstatus").equals("Active")) {
                    ExternalAuthenticationUtil.clearTokenConnections(authtokenid, internalQP, internalCP);
                    throw new RestException(401, "Missing, invalid or timed out connectionid", "Token " + tokenValue + " in not active.");
                }
                Calendar expiryDate = tokenDS.getCalendar(0, "expirydt");
                if (expiryDate != null && expiryDate.before(Calendar.getInstance())) {
                    ExternalAuthenticationUtil.clearTokenConnections(authtokenid, internalQP, internalCP);
                    throw new RestException(401, "Missing, invalid or timed out connectionid", "The supplied token has expired. Please request a new or renewed token.");
                }
                forceExternalAppId = tokenDS.getValue(0, "externalappid");
                String externaluserid = tokenDS.getValue(0, "externaluserid");
                String processAsUserType = tokenDS.getValue(0, "processastypeflag", "N");
                if (!processAsUserType.equals("N")) {
                    if (processAsUserType.equals("U")) {
                        externaluserid = tokenDS.getValue(0, "processasuserid", externaluserid);
                    } else {
                        String requestedProcessAsUserid = request.getParameter("processas");
                        if (requestedProcessAsUserid == null || requestedProcessAsUserid.length() == 0) {
                            requestedProcessAsUserid = request.getHeader("ProcessAs");
                        }
                        if (requestedProcessAsUserid != null && requestedProcessAsUserid.length() > 0) {
                            externaluserid = requestedProcessAsUserid;
                        }
                        ExternalAuthenticationUtil.validateProcessAsUser(internalQP, tokenDS, requestedProcessAsUserid, processAsUserType);
                    }
                }
                Class<BaseNameSpaceHandler> clazz = BaseNameSpaceHandler.class;
                // MONITORENTER : com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler.class
                ConnectionProcessor vanillaConnnectionProcessor = new ConnectionProcessor();
                connectionid = (String)CacheUtil.get(databaseid, "AuthTokenConnectionid", tokenValue + ";" + externaluserid);
                if (connectionid != null && connectionid.length() > 0 && !vanillaConnnectionProcessor.checkConnection(connectionid)) {
                    connectionid = "";
                }
                if (connectionid == null || connectionid.length() == 0) {
                    connectionid = ExternalAuthenticationUtil.getConnectionidForExternalUser(authtokenid, tokenValue, databaseid, forceExternalAppId, externaluserid, internalSapphireConnection, "SapphireREST");
                }
                // MONITOREXIT : clazz
            }
        }
        boolean bypassDedicatedConnectionCheck = className.equals(RootNameSpaceHandler.class.getName()) || className.equals(ApiNameSpaceHandler.class.getName());
        SapphireConnection sapphireConnection = null;
        if (connectionid != null && connectionid.length() > 0) {
            ConnectionProcessor cp = new ConnectionProcessor(connectionid);
            boolean isValidConnection = cp.checkConnection(connectionid);
            SapphireConnection sapphireConnection2 = sapphireConnection = isValidConnection ? cp.getSapphireConnection() : null;
            if (sapphireConnection == null && (!className.equals(ConnectionsNameSpaceHandler.class.getName()) || className.equals(ConnectionsNameSpaceHandler.class.getName()) && !request.getMethod().equals("POST"))) {
                HttpUtil httpUtil = new HttpUtil(request, response);
                httpUtil.removeCookie("connectionid");
                httpUtil.removeCookie("token");
                throw new RestException(401, "Missing, invalid or timed out connectionid", "Resource request requires a valid connectionid, token or databaseid");
            }
            SecurityPolicyUtil.checkRESTServicesEnabled(connectionid, connectionidFromRequest, bypassDedicatedConnectionCheck, request.getScheme(), request.getRemoteHost(), request.getRemoteAddr());
        } else {
            if (databaseid == null) throw new RestException(401, "Missing, invalid or timed out connectionid", "Resource request requires a valid connectionid, token or databaseid");
            if (databaseid.length() <= 0) throw new RestException(401, "Missing, invalid or timed out connectionid", "Resource request requires a valid connectionid, token or databaseid");
            SecurityPolicyUtil.checkRESTServicesEnabled(databaseid, bypassDedicatedConnectionCheck, request.getScheme(), request.getRemoteHost(), request.getRemoteAddr());
        }
        try {
            Trace.logInfo(LOGHEADER, "Creating namespace handler: " + className);
            Class<?> c = Class.forName(className);
            BaseNameSpaceHandler handler = (BaseNameSpaceHandler)c.newInstance();
            if (handler.requiresConnection() && sapphireConnection == null) {
                throw new RestException(401, "Missing, invalid or timed out connectionid", "Resource request requires a connectionid");
            }
            if (sapphireConnection != null) {
                DataSet externalAppDS;
                String externalappid;
                String string = externalappid = forceExternalAppId.length() > 0 ? forceExternalAppId : sapphireConnection.getExternalAppId();
                if (externalappid.length() > 0 && (externalAppDS = ExternalAuthenticationUtil.getCachedExternalAppDS(externalappid, databaseid, new QueryProcessor(connectionid))).size() > 0) {
                    if (externalAppDS.getValue(0, "restflag", "N").equals("N")) {
                        throw new RestException(401, "Resource not available", "External App " + externalappid + " is not authenticated to use REST");
                    }
                    handler.restPolicyNodeid = externalAppDS.getValue(0, "restpolicynodeid", handler.restPolicyNodeid);
                }
            }
            handler.request = request;
            handler.response = response;
            handler.sapphireConnection = sapphireConnection;
            handler.jsonRequest = jsonRequest;
            handler.setResponseCode(200);
            handler.servletName = servletName;
            handler.nameSpaceSegments = pathInfoParts != null && pathInfoParts.length > 1 ? Arrays.copyOfRange(pathInfoParts, 1, pathInfoParts.length) : null;
            handler.nameSpaceRootURL = rootURL;
            handler.isJUnit = Configuration.isJunitServer();
            return handler;
        }
        catch (RestException e) {
            throw e;
        }
        catch (Exception e) {
            Trace.logError(LOGHEADER, (Object)("Failed to create NameSpaceHandler " + className), e);
            throw new RestException(400, "Malformed request URL", "Unrecognized resource in URL - failed to create resource handler", e);
        }
    }

    public String getRestPolicyNodeid() {
        return this.restPolicyNodeid;
    }

    public abstract boolean requiresConnection();

    public abstract void process() throws Exception;

    public void respond() throws IOException, JSONException {
        if (!this.response.isCommitted()) {
            this.response.setContentType("application/json");
            this.response.setCharacterEncoding("UTF-8");
            PrintWriter out = this.response.getWriter();
            out.print(this.jsonResponse.toString(1));
            out.close();
        }
    }

    protected int getNameSpaceSegmentCount() {
        return this.nameSpaceSegments == null ? 0 : this.nameSpaceSegments.length;
    }

    protected String getNameSpaceSegment(int index) throws RestException {
        if (this.nameSpaceSegments != null && index >= 0 && index < this.nameSpaceSegments.length) {
            return HttpUtil.decodeURIComponent(this.nameSpaceSegments[index]);
        }
        throw new RestException(400, "Malformed request URL", "Invalid node access (" + index + ") in request URL " + this.request.getRequestURL());
    }

    public String getNameSpaceRootURL() {
        return this.nameSpaceRootURL;
    }

    public void setResponseValue(String name, int value) throws RestException {
        try {
            this.jsonResponse.put(name, value);
        }
        catch (JSONException e) {
            throw new RestException(500, "Unexpected server error", "Failed to set response value (name=" + name + ", value=" + value + ")", e);
        }
    }

    public void setResponseValue(String name, String value) throws RestException {
        try {
            this.jsonResponse.put(name, value);
        }
        catch (JSONException e) {
            throw new RestException(500, "Unexpected server error", "Failed to set response value (name=" + name + ", value=" + value + ")", e);
        }
    }

    public void setResponseValue(String name, JSONObject value) throws RestException {
        try {
            this.jsonResponse.put(name, value);
        }
        catch (JSONException e) {
            throw new RestException(500, "Unexpected server error", "Failed to set response value (name=" + name + ", value=" + value + ")", e);
        }
    }

    public void setResponseValue(String name, JSONArray value) throws RestException {
        try {
            this.jsonResponse.put(name, value);
        }
        catch (JSONException e) {
            throw new RestException(500, "Unexpected server error", "Failed to set response value (name=" + name + ", value=" + value + ")", e);
        }
    }

    public void setResponseValue(String name, PropertyList value) throws RestException {
        try {
            JSONObject props = new JSONObject();
            for (Object o : value.keySet()) {
                String propertyid = (String)o;
                props.put(propertyid, value.getProperty(propertyid));
            }
            this.jsonResponse.put(name, props);
        }
        catch (JSONException e) {
            throw new RestException(500, "Unexpected server error", "Failed to set response value (name=" + name + ", value=" + value + ")", e);
        }
    }

    protected void setResponseHeader(String headername, String headervalue) throws IOException {
        this.response.setHeader(headername, headervalue);
    }

    protected void setResponseCode(int httpCode) throws IOException, RestException {
        this.setResponseCode(httpCode, null);
    }

    protected void setResponseCode(int httpCode, String message) throws IOException, RestException {
        this.setResponseValue("code", httpCode);
        this.setResponseValue("status", BaseNameSpaceHandler.getStatusFromCode(httpCode));
        if (message != null && message.length() > 0) {
            this.setResponseValue("message", message);
        }
        this.response.setStatus(httpCode);
    }

    public static String getStatusFromCode(int httpCode) {
        return httpCode == 200 ? "OK" : (httpCode == 201 ? "Created" : (httpCode == 204 ? "No Content" : (httpCode == 304 ? "Not Modified" : (httpCode == 400 ? "Bad Request" : (httpCode == 401 ? "Unauthorized" : (httpCode == 403 ? "Forbidden" : (httpCode == 404 ? "Not Found" : (httpCode == 405 ? "Method Not Allowed" : (httpCode == 409 ? "Conflict" : (httpCode == 500 ? "Server error" : "Server error"))))))))));
    }

    protected boolean doPost() {
        return this.request.getMethod().equals("POST");
    }

    protected boolean doGet() {
        return this.request.getMethod().equals("GET");
    }

    protected boolean doPut() {
        return this.request.getMethod().equals("PUT");
    }

    protected boolean doDelete() {
        return this.request.getMethod().equals("DELETE");
    }

    public String getDatabaseid() {
        return this.sapphireConnection == null ? null : this.sapphireConnection.getDatabaseId();
    }

    protected SDCProcessor getSDCProcessor() {
        if (this.sdcProcessor == null) {
            this.sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.sdcProcessor;
    }

    protected SDIProcessor getSDIProcessor() {
        if (this.sdiProcessor == null) {
            this.sdiProcessor = new SDIProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.sdiProcessor;
    }

    protected ActionProcessor getActionProcessor() {
        if (this.actionProcessor == null) {
            this.actionProcessor = new ActionProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.actionProcessor;
    }

    protected QueryProcessor getQueryProcessor() {
        if (this.queryProcessor == null) {
            this.queryProcessor = new QueryProcessor(this.sapphireConnection.getConnectionId());
        }
        return this.queryProcessor;
    }

    protected void logInfo(String message) {
        Trace.logInfo(LOGHEADER, message);
    }

    protected void logError(String message) {
        Trace.logError(LOGHEADER, message);
    }

    protected void logError(String message, Exception e) {
        Trace.logError(LOGHEADER, (Object)message, e);
    }
}

