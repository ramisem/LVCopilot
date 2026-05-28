/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 *  org.apache.commons.codec.digest.DigestUtils
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.security.LVDefaultStatementHandler;
import com.labvantage.sapphire.admin.security.MFAUtil;
import com.labvantage.sapphire.admin.security.StatementHandlerUtil;
import com.labvantage.sapphire.admin.system.CheckConnectionProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.eventmanager.NotifyManager;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.RequestController;
import com.labvantage.sapphire.servlet.command.CommandException;
import com.labvantage.sapphire.servlet.command.JspRequest;
import com.labvantage.sapphire.servlet.command.Legacy;
import com.labvantage.sapphire.servlet.command.LoginException;
import com.labvantage.sapphire.servlet.command.PasswordException;
import com.labvantage.sapphire.tagext.LogonPageTag;
import com.labvantage.sapphire.util.StatementsUtil;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BasePasswordValidator;
import sapphire.ext.BaseWebMFAHandler;
import sapphire.ext.BaseWebSSOHandler;
import sapphire.ext.LogonRequestValidator;
import sapphire.servlet.RequestContext;
import sapphire.util.ActionBlock;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.ForwardUtil;
import sapphire.util.HttpUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Login {
    public static final String TOOL_SAPPHIREADMIN = "SapphireAdmin";
    public static final String TOOL_REQUESTCONTROLLER = "RequestController";
    public static final String TOOL_STELLAR = "Stellar";
    private static String systemPassword;
    private final JspRequest jspRequest;
    private static BaseWebSSOHandler webSSOHandler;
    private LogonRequestValidator logonRequestValidator;

    public Login() {
        block19: {
            this.jspRequest = new JspRequest();
            this.logonRequestValidator = null;
            try {
                PropertyList logonProps = new PropertyList();
                LogonPageTag.setDefaults(logonProps);
                try {
                    LogonPageTag.addOverrides(logonProps, new File(Configuration.getInstance().getApplicationHome()));
                }
                catch (SapphireException e) {
                    Trace.logError("Failed to load sapphirelogon.xml overrides. Reason: " + e.getMessage(), e);
                }
                if (!"Y".equals(logonProps.getProperty("websso")) && !"Y".equals(ConfigService.getConfigProperty("com.labvantage.sapphire.server.websso.enable"))) break block19;
                String ssoHandlerClass = logonProps.getProperty("webssohandlerclass");
                if (ssoHandlerClass == null || ssoHandlerClass.length() == 0) {
                    ssoHandlerClass = ConfigService.getConfigProperty("com.labvantage.sapphire.server.websso.webssohandlerclass");
                }
                webSSOHandler = ssoHandlerClass == null || ssoHandlerClass.length() == 0 ? new BaseWebSSOHandler() : (BaseWebSSOHandler)Class.forName(ssoHandlerClass).newInstance();
                String logonrequestvalidatorclass = logonProps.getProperty("logonrequestvalidatorclass");
                if (logonrequestvalidatorclass != null && logonrequestvalidatorclass.length() > 0) {
                    try {
                        this.logonRequestValidator = (LogonRequestValidator)Class.forName(logonrequestvalidatorclass).newInstance();
                    }
                    catch (ClassNotFoundException classNotFoundException) {
                        // empty catch block
                    }
                }
                if (this.logonRequestValidator == null && webSSOHandler != null) {
                    this.logonRequestValidator = webSSOHandler;
                }
                if (logonProps.getProperty("webssodatabase").length() > 0) {
                    webSSOHandler.setWebssodatabase(logonProps.getProperty("webssodatabase"));
                } else {
                    webSSOHandler.setWebssodatabase(ConfigService.getConfigProperty("com.labvantage.sapphire.server.websso.databaseid"));
                }
                if (logonProps.getProperty("webssologoffurl").length() > 0) {
                    webSSOHandler.setWebssologoffurl(logonProps.getProperty("webssologoffurl"));
                } else {
                    webSSOHandler.setWebssologoffurl(ConfigService.getConfigProperty("com.labvantage.sapphire.server.websso.logoffurl"));
                }
                if (logonProps.getProperty("webssoesigurl").length() > 0) {
                    webSSOHandler.setWebssoesigurl(logonProps.getProperty("webssoesigurl"));
                } else {
                    webSSOHandler.setWebssoesigurl(ConfigService.getConfigProperty("com.labvantage.sapphire.server.websso.webssoesigurl"));
                }
                if (logonProps.getProperty("useridattributename").length() > 0) {
                    webSSOHandler.setUseridattributename(logonProps.getProperty("useridattributename"));
                } else {
                    webSSOHandler.setUseridattributename(ConfigService.getConfigProperty("com.labvantage.sapphire.server.websso.useridattributename"));
                }
                if (logonProps.getProperty("webssoallowheaderattributes").length() > 0) {
                    webSSOHandler.setAllowHeaderAttributes("Y".equals(logonProps.getProperty("webssoallowheaderattributes", "N")));
                } else {
                    webSSOHandler.setAllowHeaderAttributes("Y".equals(ConfigService.getConfigProperty("com.labvantage.sapphire.server.websso.allowheaderattributes", "N")));
                }
            }
            catch (Exception e) {
                Trace.logError("Unable to check websso or instantiate handler class:" + e.getMessage());
            }
        }
    }

    public void login(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String autocompleternd = request.getParameter("autocompleternd");
        String usernameParam = "username";
        String passwordParam = "password";
        if (autocompleternd != null && autocompleternd.length() > 0) {
            usernameParam = usernameParam + autocompleternd;
            passwordParam = passwordParam + autocompleternd;
        }
        String username = request.getParameter(usernameParam);
        String password = request.getParameter("command") != null && request.getParameter("command").equals("changepassword") && request.getParameter("newpassword") != null ? request.getParameter("newpassword") : request.getParameter(passwordParam);
        String database = request.getParameter("database");
        String jobtype = request.getParameter("jobtype");
        String page = request.getParameter("page");
        String nexturl = request.getParameter("nexturl");
        String ignoreLogonUrl = request.getParameter("ignorelogonurl");
        String tool = request.getParameter("tool");
        String ignoreExpiryWarning = request.getParameter("ignoreexpirywarning");
        String forceChangePassword = request.getParameter("forcechangepassword");
        String clearoldconnectionoption = request.getParameter("clearoldconnection");
        this.login(username, password, database, jobtype, page, nexturl, ignoreLogonUrl, tool, ignoreExpiryWarning, forceChangePassword, clearoldconnectionoption, request, response, servletContext);
    }

    private void login(String username, String password, String database, String jobtype, String page, String nexturl, String ignoreLogonUrl, String tool, String ignoreExpiryWarning, String forceChangePassword, String clearoldconnectionoption, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        if (this.isWebSSO()) {
            this.login(username, password, database, jobtype, page, nexturl, ignoreLogonUrl, tool, ignoreExpiryWarning, forceChangePassword, clearoldconnectionoption, request, response, servletContext, false);
        } else {
            this.login(username, password, database, jobtype, page, nexturl, ignoreLogonUrl, tool, ignoreExpiryWarning, forceChangePassword, clearoldconnectionoption, request, response, servletContext, true);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void login(String username, String password, String database, String jobtype, String page, String nexturl, String ignoreLogonUrl, String tool, String ignoreExpiryWarning, String forceChangePassword, String clearoldconnectionoption, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, boolean doLogoff) throws ServletException {
        block91: {
            boolean setConnectionidCookie;
            HttpUtil httpUtil;
            RequestContext requestContext;
            ConnectionProcessor cp;
            String connectionid;
            boolean isSecondaryAuthentication;
            block89: {
                String csrftoken;
                String webssouserid;
                String logonName;
                ConnectionInfo exconnInfo;
                CheckConnectionProcessor checkConnectionProcessor;
                String exconnectionid;
                block96: {
                    block97: {
                        block95: {
                            block93: {
                                HashMap<String, Object> options;
                                block94: {
                                    CheckConnectionProcessor checkConnectionProcessor2;
                                    String portalconnectionid;
                                    String webssouserid2;
                                    CheckConnectionProcessor checkConnectionProcessor3;
                                    block92: {
                                        boolean bl = isSecondaryAuthentication = (password == null || password.length() == 0) && request.getSession().getAttribute("baseWebMFAHandler") != null;
                                        if (!this.isWebSSO() && !isSecondaryAuthentication && (username == null || username.length() <= 0 || password == null || password.length() <= 0 || database == null || database.length() <= 0 || nexturl == null || nexturl.length() <= 0)) break block91;
                                        if (forceChangePassword != null) {
                                            if (!forceChangePassword.equals("false")) throw new PasswordException("CHANGE_PASSWORD", "", request.getParameter("nexturl") + "&sso=n", username, password, database);
                                        }
                                        connectionid = null;
                                        cp = new ConnectionProcessor();
                                        requestContext = (RequestContext)request.getAttribute("RequestContext");
                                        httpUtil = new HttpUtil(request, response);
                                        setConnectionidCookie = false;
                                        if (!isSecondaryAuthentication) break block92;
                                        BaseWebMFAHandler baseWebMFAHandler = (BaseWebMFAHandler)request.getSession().getAttribute("baseWebMFAHandler");
                                        if (baseWebMFAHandler == null) {
                                            throw new LoginException("Primary authentication has expired. Please start from primary log on again.");
                                        }
                                        connectionid = baseWebMFAHandler.getConnectionid();
                                        cp = new ConnectionProcessor(connectionid);
                                        break block89;
                                    }
                                    if (doLogoff) {
                                        this.logoff(requestContext.getConnectionId(), request, response, servletContext);
                                    }
                                    options = new HashMap<String, Object>();
                                    options.put("deviceid", "WebServer");
                                    options.put("superuser", "false");
                                    options.put("tool", tool == null || tool.length() == 0 ? TOOL_REQUESTCONTROLLER : tool);
                                    options.put("ignoreexpirywarning", ignoreExpiryWarning != null ? ignoreExpiryWarning : "false");
                                    if (jobtype != null && jobtype.length() > 0) {
                                        options.put("jobtype", jobtype);
                                    }
                                    options.put("remoteip", request.getRemoteAddr());
                                    options.put("remotehost", request.getRemoteHost());
                                    options.put("connectiontypeflag", "B");
                                    options.put("clearoldconnectionoption", clearoldconnectionoption);
                                    exconnectionid = httpUtil.getCookieValue("connectionid");
                                    if (username != null && username.length() > 0 && exconnectionid != null && exconnectionid.length() > 0 && !"changejobtype".equals(request.getParameter("command")) && (checkConnectionProcessor3 = new CheckConnectionProcessor(exconnectionid)).isConnectionValid(exconnectionid)) {
                                        ConnectionInfo exconnInfo2 = checkConnectionProcessor3.getConnectionInfo(exconnectionid);
                                        String exuserid = exconnInfo2.getSysuserId();
                                        if (exuserid.equalsIgnoreCase(username)) {
                                            if (!this.isWebSSOSession(request)) throw new LoginException("You are currently logged in with this browser window. You must <a href=\"rc?command=logoff\">log out</a> from the other session to log on again.", nexturl, username, database);
                                            try {
                                                response.sendRedirect(request.getContextPath() + "/logon.jsp");
                                            }
                                            catch (IOException ioe) {
                                                throw new LoginException("Error during SSO login, unable to redirect to logon.jsp", nexturl, username, database);
                                            }
                                        }
                                        if (!this.isWebSSOSession(request)) throw new LoginException("Another User, " + exuserid + ", is currently logged in using this browser window.", nexturl, username, database);
                                        try {
                                            response.sendRedirect(request.getContextPath() + "/logon.jsp?sso=n&errormsg=Another user is already logged into a SSO session.");
                                            throw new LoginException("Another User, " + exuserid + ", is currently logged in using this browser window.", nexturl, username, database);
                                        }
                                        catch (IOException ioe) {
                                            throw new LoginException("Error during SSO login, unable to redirect to logon.jsp", nexturl, username, database);
                                        }
                                    }
                                    if (exconnectionid != null && exconnectionid.length() > 0 && !"changepassword".equals(request.getParameter("command")) && (checkConnectionProcessor3 = new CheckConnectionProcessor(exconnectionid)).checkConnection(exconnectionid)) {
                                        ConnectionInfo exconnInfo3 = checkConnectionProcessor3.getConnectionInfo(exconnectionid);
                                        String exuserid = exconnInfo3.getSysuserId();
                                        if (this.isWebSSO()) {
                                            webssouserid2 = this.isWebSSO() ? webSSOHandler.getUserid(request) : null;
                                            String logonName2 = exconnInfo3.getLogonName();
                                            if (webssouserid2 != null && webssouserid2.length() > 0) {
                                                if (!exuserid.equalsIgnoreCase(webssouserid2)) {
                                                    if (logonName2 == null) throw new LoginException("Another User currently has an active session. You must <a href=\"rc?command=logoff\">log out</a> from the other session and then close all browser windows to log on again.", nexturl, username, database);
                                                    if (!logonName2.equalsIgnoreCase(webssouserid2)) throw new LoginException("Another User currently has an active session. You must <a href=\"rc?command=logoff\">log out</a> from the other session and then close all browser windows to log on again.", nexturl, username, database);
                                                }
                                                connectionid = exconnectionid;
                                                username = exuserid;
                                                request.getSession().setAttribute("WebSSOUsername", (Object)webssouserid2);
                                            }
                                        } else {
                                            if (exuserid.equalsIgnoreCase(username)) {
                                                throw new LoginException("You are currently logged in with this browser window. You must start a new browser window or <a href=\"rc?command=logoff\">log out</a> from the other session to log on again.", nexturl, username, database);
                                            }
                                            String sysconnectionid = new ConnectionProcessor().getConnectionid(database, "(system)", systemPassword);
                                            QueryProcessor queryProcessor = new QueryProcessor(sysconnectionid);
                                            DataSet sysuser = queryProcessor.getPreparedSqlDataSet("SELECT sysuserid, logonname FROM sysuser WHERE logonname = ?", new Object[]{username});
                                            cp.clearConnection(sysconnectionid);
                                            if (sysuser.getRowCount() != 1) throw new LoginException("Another User, " + exuserid + ", is currently logged in using this browser window. You must start a new browser window to log on.", nexturl, username, database);
                                            if (!exuserid.equals(sysuser.getValue(0, "sysuserid"))) throw new LoginException("Another User, " + exuserid + ", is currently logged in using this browser window. You must start a new browser window to log on.", nexturl, username, database);
                                            if (!username.equals(sysuser.getValue(0, "logonname"))) throw new LoginException("Another User, " + exuserid + ", is currently logged in using this browser window. You must start a new browser window to log on.", nexturl, username, database);
                                            throw new LoginException("You are currently logged in with this browser window. You must start a new browser window or <a href=\"rc?command=logoff\">log out</a> from the other session to log on again.", nexturl, username, database);
                                        }
                                    }
                                    if ((portalconnectionid = httpUtil.getCookieValue("stellarcid")) != null && portalconnectionid.length() > 0 && (checkConnectionProcessor2 = new CheckConnectionProcessor(portalconnectionid)).checkConnection(portalconnectionid)) {
                                        ConnectionInfo portalconnInfo = checkConnectionProcessor2.getConnectionInfo(portalconnectionid);
                                        String portaluserid = portalconnInfo.getSysuserId();
                                        throw new LoginException("A Portal User, " + portaluserid + ", is currently logged in using this browser window. You must start a new browser window to log on.", nexturl, username, database);
                                    }
                                    Browser browser = new Browser(request);
                                    browser.setGUIModes(database, request.getSession());
                                    options.put("useragent", browser.getUseragent());
                                    options.put("browser", browser.getName() + " (" + browser.getVersion() + ")");
                                    options.put("operatingsystem", browser.getOSId());
                                    Browser.GUIMode guiMode = browser.getGUIMode();
                                    options.put("guimode", guiMode.getId());
                                    if (this.isWebSSO() && connectionid == null && password == null && !"updatejobtype".equals(request.getParameter("command"))) {
                                        Trace.log("Login", "SSO Login, trying to get username from attribute:" + webSSOHandler.getUseridattributename());
                                        username = webSSOHandler.getUserid(request);
                                        if (username != null && username.length() > 0) {
                                            Trace.log("Login", "Got username " + username);
                                        } else {
                                            Enumeration e = request.getAttributeNames();
                                            Trace.log("Login", "User name is not retrieved from attributes:");
                                            while (e.hasMoreElements()) {
                                                String attributeid = (String)e.nextElement();
                                                Trace.log("Login", attributeid + "=" + request.getAttribute(attributeid));
                                            }
                                            if (webSSOHandler.isAllowHeaderAttributes()) {
                                                e = request.getHeaderNames();
                                                Trace.log("Login", "Allow Header Attributes. User name is not retrieved from headers:");
                                                while (e.hasMoreElements()) {
                                                    String headername = (String)e.nextElement();
                                                    Trace.log("Login", headername + "=" + request.getHeader(headername));
                                                }
                                            }
                                        }
                                        try {
                                            if (username != null && username.length() > 0) {
                                                if (webSSOHandler.getWebssoesigurl().length() > 0) {
                                                    String webSSOEsigURL = StringUtil.replaceAll(webSSOHandler.getWebssoesigurl(), "[esigpageurl]", request.getRequestURL().substring(0, request.getRequestURL().indexOf("rc/login")) + "esigsso.jsp");
                                                    String webSSOEsigURLGWT = StringUtil.replaceAll(webSSOHandler.getWebssoesigurl(), "[esigpageurl]", request.getRequestURL().substring(0, request.getRequestURL().indexOf("rc/login")) + "esigsso.jsp?source=GWT");
                                                    httpUtil.setCookieValue("webSSOEsigURL", webSSOEsigURL);
                                                    httpUtil.setCookieValue("webSSOEsigURLGWT", webSSOEsigURLGWT);
                                                }
                                                request.getSession().setAttribute("WebSSOUsername", (Object)username);
                                                if (database == null || database.length() == 0) {
                                                    String[] databases = new ConnectionProcessor().getDatabaseList();
                                                    if (databases.length == 1) {
                                                        database = databases[0];
                                                    } else {
                                                        if (webSSOHandler.getWebssodatabase().length() <= 0) {
                                                            response.sendRedirect(request.getRequestURI().substring(0, request.getRequestURI().indexOf("/rc")) + "/loggedoffmessage.jsp?username=" + username);
                                                            return;
                                                        }
                                                        database = webSSOHandler.getWebssodatabase();
                                                    }
                                                }
                                            } else {
                                                response.sendRedirect(request.getRequestURI().substring(0, request.getRequestURI().indexOf("/rc")) + "/loggedoffmessage.jsp?username=unknown");
                                                return;
                                            }
                                            options.put("tools", "websso");
                                            password = systemPassword;
                                            options.put("ssoattributes", webSSOHandler.getSSOAttributes(request));
                                        }
                                        catch (IOException databases) {
                                            // empty catch block
                                        }
                                    }
                                    if (connectionid != null && (connectionid.length() != 0 || "changejobtype".equals(request.getParameter("command")))) break block93;
                                    if (exconnectionid != null && !exconnectionid.trim().isEmpty()) {
                                        cp.clearConnection(exconnectionid);
                                        exconnectionid = "";
                                    }
                                    if (!this.isWebSSO() || !this.isWebSSOSession(request)) break block94;
                                    String string = webssouserid2 = this.isWebSSO() ? webSSOHandler.getUserid(request) : null;
                                    if (webssouserid2 != null && !webssouserid2.trim().isEmpty()) {
                                        if (exconnectionid.isEmpty()) {
                                            connectionid = cp.getConnectionid(database, username, password, options);
                                            setConnectionidCookie = true;
                                            break block89;
                                        } else {
                                            connectionid = exconnectionid;
                                            setConnectionidCookie = true;
                                        }
                                        break block89;
                                    } else {
                                        connectionid = cp.getConnectionid(database, username, password, options);
                                        setConnectionidCookie = true;
                                    }
                                    break block89;
                                }
                                connectionid = cp.getConnectionid(database, username, password, options);
                                if (!connectionid.isEmpty()) {
                                    setConnectionidCookie = true;
                                }
                                break block89;
                            }
                            checkConnectionProcessor = new CheckConnectionProcessor(exconnectionid);
                            exconnInfo = checkConnectionProcessor.getConnectionInfo(exconnectionid);
                            logonName = exconnInfo.getLogonName();
                            webssouserid = webSSOHandler.getUserid(request);
                            if (!username.equalsIgnoreCase(webssouserid) && (logonName == null || !logonName.equalsIgnoreCase(webssouserid))) break block95;
                            connectionid = exconnectionid;
                            cp = new ConnectionProcessor(connectionid);
                            if (this.isWebSSOSession(request)) break block96;
                            break block97;
                        }
                        Trace.logError("Another User, " + webssouserid + ", is currently logged in using this browser window.");
                        throw new LoginException(nexturl, "You are currently logged in with this browser window. You must start a new browser window or <a href=\"rc?command=logoff\">log out</a> from the other session to log on again.");
                    }
                    csrftoken = DigestUtils.md5Hex((String)(username + Math.round(Math.random() * 1.0E7 * 1000000.0)));
                    request.getSession().setAttribute("csrftoken", (Object)csrftoken);
                    break block89;
                }
                checkConnectionProcessor = new CheckConnectionProcessor(connectionid);
                exconnInfo = checkConnectionProcessor.getConnectionInfo(exconnectionid);
                logonName = exconnInfo.getLogonName();
                webssouserid = webSSOHandler.getUserid(request);
                if (!(username.equalsIgnoreCase(webssouserid) || logonName != null && logonName.equalsIgnoreCase(webssouserid))) {
                    csrftoken = DigestUtils.md5Hex((String)(username + Math.round(Math.random() * 1.0E7 * 1000000.0)));
                    request.getSession().setAttribute("csrftoken", (Object)csrftoken);
                    checkConnectionProcessor.clearConnection(exconnectionid);
                }
            }
            if (connectionid != null && connectionid.length() > 0) {
                boolean isSwitchingJobtype;
                Trace.log("Login", "Connectionid: " + connectionid);
                boolean bl = isSwitchingJobtype = password != null && (password.equals(systemPassword) || password.length() > 0 && request.getParameter("jobtypeoption") != null) && jobtype != null && jobtype.length() > 0;
                if (!isSwitchingJobtype && MFAUtil.isGlobalMFAEnabled(connectionid, this.isWebSSOSession(request), false)) {
                    try {
                        BaseWebMFAHandler baseWebMFAHandler;
                        if (isSecondaryAuthentication) {
                            baseWebMFAHandler = (BaseWebMFAHandler)request.getSession().getAttribute("baseWebMFAHandler");
                            if (baseWebMFAHandler == null) {
                                Trace.logError("BaseWebMFAHandler not found to verify response.");
                                throw new Exception("BaseWebMFAHandler not found to verify response.");
                            }
                            Trace.log("Login", "Verify 2FA prompt response.");
                            if (!baseWebMFAHandler.verifyResponse(request, response)) {
                                Trace.logWarn("Login", "2FA response verification failed.");
                                return;
                            }
                            Trace.log("Login", "2FA prompt response verified successfully.");
                            baseWebMFAHandler.completeLogin(request);
                        } else {
                            baseWebMFAHandler = MFAUtil.getWebMFAHandler(username, database, connectionid, request, response);
                            if (baseWebMFAHandler != null) {
                                Trace.log("Login", "MFA enabled with " + baseWebMFAHandler.getClass().getName() + ". Set connectionid pending and prompt for 2FA.");
                                setConnectionidCookie = false;
                                baseWebMFAHandler.renderPrompt(request, response);
                                return;
                            }
                        }
                    }
                    catch (Throwable t) {
                        cp.clearConnection(connectionid);
                        Trace.logError("Abort login due to unexpected Error :" + t.getMessage());
                        throw new ServletException(t);
                    }
                }
                if (setConnectionidCookie) {
                    httpUtil.setCookieValue("connectionid", connectionid);
                }
                if (!(this.isWebSSOSession(request) || isSwitchingJobtype || isSecondaryAuthentication)) {
                    try {
                        LVDefaultStatementHandler statementHandler = (LVDefaultStatementHandler)StatementHandlerUtil.getStatementHandler(cp.getSapphireConnection().getSysuserId(), database, connectionid, systemPassword, request, response);
                        PropertyList authenticationPL = new PropertyList();
                        authenticationPL.setAttribute("jobtype", jobtype);
                        authenticationPL.setAttribute("page", page);
                        authenticationPL.setAttribute("nexturl", nexturl);
                        statementHandler.init(authenticationPL, username, database, connectionid, systemPassword);
                        request.getSession().setAttribute("statementHandler", (Object)statementHandler);
                        if (!statementHandler.getStatements().isEmpty()) {
                            statementHandler.renderPrompt(request, response);
                            return;
                        }
                    }
                    catch (Exception ex) {
                        cp.clearConnection(connectionid);
                        Trace.logError("Abort login due to unexpected Error :" + ex.getMessage());
                        throw new ServletException((Throwable)ex);
                    }
                }
                if (this.logonRequestValidator != null) {
                    String failmessage = null;
                    if (this.logonRequestValidator.isRequireSysuserInfo()) {
                        String sysconnectionid = cp.getConnectionid(database, "(system)", systemPassword);
                        QueryProcessor queryProcessor = new QueryProcessor(sysconnectionid);
                        DataSet sysuser = queryProcessor.getPreparedSqlDataSet("SELECT * FROM sysuser WHERE lower( sysuserid ) = ? OR logonname = ?", new Object[]{username.toLowerCase(), username});
                        failmessage = this.logonRequestValidator.validateRequest(request, sysuser);
                        cp.clearConnection(sysconnectionid);
                    } else {
                        failmessage = this.logonRequestValidator.validateRequest(request);
                    }
                    if (failmessage != null && failmessage.length() > 0) {
                        try {
                            response.getWriter().write(failmessage);
                            return;
                        }
                        catch (IOException ioe) {
                            Trace.logError(ioe.getMessage());
                        }
                        return;
                    }
                }
                new HttpUtil(request, response).setCookieValue(request.getParameter("jobtypeid") != null ? "*connectionid" : "connectionid", connectionid, false, SecurityPolicyUtil.httpOnlyRequestControllerCookie);
                request.getSession().setAttribute("connectionid", (Object)connectionid);
                request.getSession().setAttribute("connectionuseragent", (Object)request.getHeader("User-Agent"));
                if (!this.isWebSSOSession(request) || request.getSession().getAttribute("csrftoken") == null) {
                    String csrftoken = DigestUtils.md5Hex((String)(username + Math.round(Math.random() * 1.0E7 * 1000000.0)));
                    request.getSession().setAttribute("csrftoken", (Object)csrftoken);
                    request.getSession().setAttribute("connectionuseragent", (Object)request.getHeader("User-Agent"));
                }
                com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
                ConnectionInfo cInfo = cp.getConnectionInfo(connectionid);
                try {
                    if ("Y".equalsIgnoreCase(configurationProcessor.getProfileProperty(cInfo.getSysuserId(), "viewhidden", "N")) && !Arrays.asList(StringUtil.split(cInfo.getRoleList(), ";")).contains("View Hidden")) {
                        configurationProcessor.setProfileProperty(cInfo.getSysuserId(), "viewhidden", "N");
                    }
                }
                catch (Exception e) {
                    Trace.logError("Failed to check hidden records.");
                }
                String requestURI = request.getRequestURI();
                try {
                    if (nexturl != null) {
                        nexturl = Legacy.checkURL(nexturl);
                    } else if (requestURI.indexOf("/rc/login") > 0 && request.getQueryString() != null && request.getQueryString().indexOf("command=page&page=") >= 0) {
                        nexturl = request.getQueryString();
                        ignoreLogonUrl = "Y";
                    } else {
                        nexturl = "";
                    }
                    Trace.log("Login", "Logon successful - Redirecting to " + requestURI + "?" + nexturl + "&connectionid=" + connectionid);
                    if (request.getAttribute("legacylogin") != null && request.getAttribute("legacylogin").equals("Y")) {
                        requestContext.setConnectionId(connectionid);
                        Enumeration e = request.getParameterNames();
                        while (e.hasMoreElements()) {
                            String propertyid = (String)e.nextElement();
                            requestContext.setProperty(propertyid, request.getParameter(propertyid));
                        }
                        if (page != null && page.length() > 0) {
                            this.jspRequest.requestObject("page", request, response, request.getSession().getServletContext());
                            return;
                        }
                        this.jspRequest.requestObject("file", request, response, request.getSession().getServletContext());
                        return;
                    }
                    ForwardUtil forward = new ForwardUtil((ServletRequest)request);
                    boolean promptForJobType = false;
                    String jobtypelist = cInfo.getJobtypeList();
                    if (cInfo.getCurrentJobtype() != null) {
                        try {
                            promptForJobType = "alwaysprompt".equals(configurationProcessor.getProfileProperty(cInfo.getSysuserId(), "jobtypeoption"));
                        }
                        catch (SapphireException e) {
                            Trace.log("Could not get jobtypeoption profile property." + e.getMessage());
                        }
                        if (promptForJobType && (jobtype == null || jobtype.length() == 0)) {
                            promptForJobType = jobtypelist != null && jobtypelist.indexOf(";") > 0;
                        } else {
                            request.getSession().removeAttribute(connectionid);
                            promptForJobType = false;
                        }
                    }
                    if (promptForJobType) {
                        forward.setProperty("jobtypelist", jobtypelist);
                        RequestContext crc = (RequestContext)request.getAttribute("RequestContext");
                        crc.setProperty("currentjobtype", cInfo.getCurrentJobtype());
                        forward.setProperty("currentjobtype", cInfo.getCurrentJobtype());
                        request.getSession().setAttribute(connectionid, request.getAttribute("RequestContext"));
                        PrintWriter output = response.getWriter();
                        String redirectURL = request.getContextPath() + "/rc?command=file&file=WEB-CORE/modules/security/jobtype.jsp";
                        output.println(forward.getForm("", redirectURL, "post", true));
                        output.close();
                        return;
                    }
                    this.sendToDefaultLogonUrl(nexturl, ignoreLogonUrl, cInfo, username, request, response);
                    return;
                }
                catch (IOException ioe) {
                    throw new ServletException((Throwable)ioe);
                }
            }
            String errorCode = cp.getLastErrorCode();
            String errorMsg = cp.getLastErrorMessage();
            if (this.isWebSSOSession(request)) {
                webSSOHandler.handleLVLogonError(errorMsg, response);
                return;
            }
            boolean detailedLoginMessages = false;
            try {
                detailedLoginMessages = ConfigService.getConfigPropertyBoolean("com.labvantage.sapphire.server.detailedloginmessages", false);
            }
            catch (ServiceException serviceException) {
                // empty catch block
            }
            if (errorCode.endsWith("EXPIRED_PASSWORD")) throw new PasswordException(errorCode, errorMsg, request.getParameter("nexturl"), username, password, database);
            if (errorCode.endsWith("PASSWORDEXPIRY_WARNING")) throw new PasswordException(errorCode, errorMsg, request.getParameter("nexturl"), username, password, database);
            if (errorCode.endsWith("MUST_CHANGE_PASSWORD")) throw new PasswordException(errorCode, errorMsg, request.getParameter("nexturl"), username, password, database);
            if (errorCode.endsWith("MUST_CHANGE_CASE_INSENSITIVE_PASSWORD")) throw new PasswordException(errorCode, errorMsg, request.getParameter("nexturl"), username, password, database);
            if (errorMsg.indexOf("Password expired for username") > -1) throw new PasswordException(errorCode, errorMsg, request.getParameter("nexturl"), username, password, database);
            if (errorMsg.indexOf("Password due to expire for username") > -1) throw new PasswordException(errorCode, errorMsg, request.getParameter("nexturl"), username, password, database);
            if (errorMsg.indexOf("must change their password") > -1) throw new PasswordException(errorCode, errorMsg, request.getParameter("nexturl"), username, password, database);
            if (errorMsg.indexOf("You must change your password to a case sensitive one.") > -1) {
                throw new PasswordException(errorCode, errorMsg, request.getParameter("nexturl"), username, password, database);
            }
            if (errorMsg.equals("You have attempted too many invalid logins. Try again later.")) throw new LoginException(errorMsg, nexturl, username, database);
            if (errorMsg.contains("users in the database exceeds the license key")) {
                throw new LoginException(errorMsg, nexturl, username, database);
            }
            if (errorMsg.indexOf("detailedLoginMessages:") == 0) {
                errorMsg = errorMsg.substring("detailedLoginMessages:".length());
                throw new LoginException(errorMsg, nexturl, username, database);
            }
            if (detailedLoginMessages) throw new LoginException(errorMsg, nexturl, username, database);
            errorMsg = "Invalid username or password specified";
            throw new LoginException(errorMsg, nexturl, username, database);
        }
        boolean first = true;
        String error = "To execute the login command, you must provide: ";
        if (username == null || username.length() == 0) {
            error = error + "a username";
            first = false;
        }
        if (password == null || password.length() == 0) {
            error = error + (first ? "" : ", ") + "a password";
            first = false;
        }
        if (database == null || database.length() == 0) {
            error = error + (first ? "" : ", ") + "a database";
            first = false;
        }
        if (nexturl != null) {
            if (nexturl.length() != 0) throw new CommandException(error, "Login");
        }
        error = error + (first ? "" : ", ") + "a nexturl";
        first = false;
        throw new CommandException(error, "Login");
    }

    public void logoff(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String nexturl = request.getParameter("url");
        if (nexturl == null || nexturl.length() == 0) {
            nexturl = RequestController.getLogonPage(servletContext);
        }
        if (nexturl != null && nexturl.length() > 0) {
            boolean isValidConnid;
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            boolean isWebSSOSession = this.isWebSSOSession(request);
            String connectionid = requestContext.getConnectionId();
            boolean bl = isValidConnid = request.getParameter("notify") != null || new CheckConnectionProcessor(connectionid).checkConnection(connectionid);
            if (!isWebSSOSession && webSSOHandler != null && request.getParameter("oldpassword") == null && request.getParameter("newpassword") == null && !isValidConnid) {
                isWebSSOSession = true;
            }
            if (isValidConnid) {
                this.logoff(connectionid, request, response, servletContext);
            }
            try {
                nexturl = Legacy.checkURL(nexturl);
                String app = request.getContextPath();
                app = app != null && app.length() > 1 ? app + "/" : "/";
                if (isWebSSOSession) {
                    this.logoffWebSSO(request, response);
                }
                String redirectUrl = ConfigService.getConfigProperty("com.labvantage.sapphire.server.http.redirecturl");
                if (redirectUrl != null) {
                    app = redirectUrl + app;
                }
                Trace.log("Login", "Logoff successful - Redirecting to " + app + nexturl);
                response.sendRedirect(response.encodeRedirectURL(app + nexturl + (this.isWebSSO() && !this.isWebSSOSession(request) && nexturl.indexOf("logon.jsp") >= 0 ? "?sso=n" : "?sso=n")));
            }
            catch (Exception ioe) {
                throw new ServletException((Throwable)ioe);
            }
        } else {
            throw new CommandException("To execute the logoff command, you must provide the url parameter", "Logoff");
        }
    }

    public void logoffWebSSO(HttpServletRequest request, HttpServletResponse response) {
        webSSOHandler.logoff(request, response);
    }

    private void logoff(String connectionid, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        this.logoff(connectionid, request, response, servletContext, true);
    }

    private void logoff(String connectionid, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, boolean invalidateSession) {
        if (connectionid != null && connectionid.length() > 0) {
            ConnectionProcessor cp = new ConnectionProcessor(connectionid);
            SapphireConnection sapphireConnection = cp.getSapphireConnection();
            boolean validcon = cp.checkConnection(connectionid);
            if (validcon && sapphireConnection != null) {
                try {
                    this.updateStartupInfo(connectionid, sapphireConnection.getSysuserId(), request.getSession());
                }
                catch (SapphireException e) {
                    Trace.logError("Failed to update startup information:" + e.getMessage());
                }
            }
            if (validcon) {
                cp.prepareToDeleteConnection(connectionid);
            }
            HttpUtil cookie = new HttpUtil(request, response);
            cookie.removeCookie("connectionid");
            if (sapphireConnection != null) {
                NotifyManager.notifyLogoff(sapphireConnection, true);
            }
        }
        if (invalidateSession) {
            try {
                HttpSession session = request.getSession();
                try {
                    session.getCreationTime();
                    session.invalidate();
                }
                catch (Exception exception) {}
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void changeJobtype(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws Exception {
        String nexturl;
        String defaultjobtypeid;
        String jobtypeid = request.getParameter("jobtypeid");
        String jobtypeoption = request.getParameter("jobtypeoption");
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String connectionid = requestContext.getConnectionId();
        RequestContext crc = (RequestContext)request.getSession().getAttribute(connectionid);
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        ConnectionInfo cInfo = cp.getConnectionInfo(connectionid);
        if (cInfo == null) {
            throw new Exception("To change job type you must log on first");
        }
        String username = cInfo.getSysuserId();
        com.labvantage.sapphire.admin.system.ConfigurationProcessor cf = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
        String jobtypeprofile = cf.getProfileProperty(username, "jobtypeoption");
        if (jobtypeoption != null && !jobtypeoption.equals(jobtypeprofile)) {
            cf.setProfileProperty(username, "jobtypeoption", jobtypeoption);
        }
        if ("defaultjobtype".equals(jobtypeoption) && (defaultjobtypeid = request.getParameter("defaultjobtypeid")) != null && cInfo.getJobtypeList().indexOf(defaultjobtypeid) != 0) {
            ActionProcessor ap = new ActionProcessor(connectionid);
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "User");
            props.put("keyid1", username);
            props.put("defaultjobtype", defaultjobtypeid);
            ap.processAction("EditSDI", "1", props);
        }
        String currentjobtype = cInfo.getCurrentJobtype();
        String database = cInfo.getDatabaseId();
        String tool = cInfo.getTool();
        String ignoreLogonUrl = "N";
        String string = nexturl = crc != null ? crc.getProperty("nexturl") : "rc?command=page&page=" + request.getParameter("frompageid");
        if (currentjobtype == null || !currentjobtype.equals(jobtypeid)) {
            String password = request.getParameter("password") != null ? request.getParameter("password") : (crc != null ? crc.getProperty("password") : request.getParameter("password"));
            String page = crc != null ? crc.getProperty("page") : "";
            String ignoreExpiryWarning = "true";
            if ((password == null || password.length() == 0) && cp.checkConnection(connectionid)) {
                password = systemPassword;
            }
            String webSSOUsername = (String)request.getSession().getAttribute("WebSSOUsername");
            Trace.log("Updating jobType from " + currentjobtype + " to " + jobtypeid);
            this.logoff(connectionid, request, response, servletContext, false);
            if (nexturl == null || nexturl.isEmpty()) {
                nexturl = "rc?command=page&page=SystemAdminTramline";
            }
            this.login(username, password, database, jobtypeid, page, nexturl, ignoreLogonUrl, tool, ignoreExpiryWarning, null, request.getParameter("clearoldconnection"), request, response, servletContext, false);
            if (webSSOUsername != null && !webSSOUsername.isEmpty()) {
                request.getSession().setAttribute("WebSSOUsername", (Object)webSSOUsername);
            }
        } else {
            this.sendToDefaultLogonUrl(nexturl, ignoreLogonUrl, cInfo, username, request, response);
        }
    }

    private void sendToDefaultLogonUrl(String nexturl, String ignoreLogonUrl, ConnectionInfo cInfo, String username, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ForwardUtil forward = new ForwardUtil((ServletRequest)request);
        nexturl = nexturl == null ? "" : Legacy.checkURL(nexturl);
        String connectionid = cInfo.getConnectionId();
        if (ignoreLogonUrl == null || ignoreLogonUrl.equals("N")) {
            DataSet jobtypedata;
            String currentjobtype;
            PropertyList guiPolicy;
            String userurl = "";
            ConfigurationProcessor cp = new ConfigurationProcessor(connectionid);
            if (cp != null) {
                try {
                    guiPolicy = cp.getPolicy("GUIPolicy", "Sapphire Custom");
                }
                catch (Exception e) {
                    guiPolicy = new PropertyList();
                }
            } else {
                guiPolicy = new PropertyList();
            }
            if (!guiPolicy.getProperty("skipjobtypeurl", "N").equalsIgnoreCase("Y") && (currentjobtype = cInfo.getCurrentJobtype()) != null && currentjobtype.length() > 0 && (jobtypedata = new QueryProcessor(connectionid).getPreparedSqlDataSet("SELECT defaultlogonurl FROM jobtype WHERE jobtypeid=?", new Object[]{currentjobtype})) != null && jobtypedata.getRowCount() > 0) {
                userurl = jobtypedata.getValue(0, "defaultlogonurl");
            }
            if (userurl == null || userurl.length() == 0) {
                com.labvantage.sapphire.admin.system.ConfigurationProcessor config = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
                try {
                    Browser b = new Browser(request);
                    String guiMode = b.getGUIMode().getId();
                    userurl = config.getProfileProperty(cInfo.getSysuserId(), guiMode + "" + "logonpageurl");
                    if (userurl != null && (userurl.contains("command=history") || userurl.contains("command=state") || userurl.contains("rc?history=")) && !this.validateHistoryCommand(connectionid, userurl)) {
                        userurl = null;
                    }
                    if (userurl == null || userurl.length() == 0) {
                        userurl = b.getGUIMode().getStartupUrl();
                    }
                    if (userurl == null || userurl.length() == 0) {
                        try {
                            userurl = guiPolicy.getProperty("defaulturl");
                        }
                        catch (Exception exception) {}
                    }
                }
                catch (SapphireException sapphireException) {
                    // empty catch block
                }
            }
            if (userurl.length() > 0) {
                nexturl = userurl.startsWith("rc?") ? userurl.substring(3) : userurl;
            }
        } else {
            forward.setProperties(HttpUtil.getRequestMap((ServletRequest)request));
        }
        if (this.isWebSSO() && nexturl.length() == 0) {
            nexturl = "rc?command=page&page=SystemAdminTramline";
        }
        PrintWriter output = response.getWriter();
        if (nexturl.length() == 0 || nexturl.indexOf("command=login") == 0) {
            output.write("No default log on URL.");
        } else {
            String requestURI = request.getRequestURI();
            if (requestURI.indexOf("/login") > 0) {
                requestURI = requestURI.substring(0, requestURI.indexOf("/login"));
            }
            String redirectURL = requestURI + "?" + nexturl;
            output.println(forward.getForm("", redirectURL, "post", true, false));
        }
        output.close();
    }

    private boolean validateHistoryCommand(String connectionid, String url) throws SapphireException {
        boolean valid = false;
        if (url != null) {
            int startpos = url.indexOf("history=");
            if (startpos > -1) {
                startpos += 8;
            } else {
                startpos = url.indexOf("state=");
                if (startpos > -1) {
                    startpos += 6;
                }
            }
            if (startpos > -1) {
                String t = url.substring(startpos);
                int endpos = t.indexOf("&");
                if (endpos == -1) {
                    endpos = t.length();
                }
                String webpagelogid = t.substring(0, endpos);
                String sql = "SELECT count(*) FROM webpagelog where webpagelogid=?";
                int count = new QueryProcessor(connectionid).getPreparedCount(sql, new Object[]{webpagelogid});
                if (count == 1) {
                    valid = true;
                }
            }
        }
        return valid;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void changePassword(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        boolean isSecondaryAuthentication;
        String database = request.getParameter("database");
        String username = request.getParameter("username");
        String oldpassword = request.getParameter("oldpassword");
        String newpassword = request.getParameter("newpassword");
        String nexturl = request.getParameter("nexturl");
        String login = request.getParameter("login");
        boolean bl = isSecondaryAuthentication = (username == null || username.length() == 0) && request.getSession().getAttribute("baseWebMFAHandler") != null;
        if (isSecondaryAuthentication) {
            this.login(request, response, servletContext);
            return;
        } else if (username != null && username.length() > 0 && oldpassword != null && oldpassword.length() > 0 && newpassword != null && newpassword.length() > 0 && database != null && database.length() > 0 && nexturl != null && nexturl.length() > 0) {
            ConnectionProcessor cp = new ConnectionProcessor();
            if (cp.changePassword("", database, username, oldpassword, newpassword) != 1) throw new PasswordException("CHANGEPASSWORD_FAILURE", "Failed to change password. Reason: " + cp.getLastErrorMessage(), nexturl, username, newpassword, database);
            String requestURI = request.getRequestURI();
            try {
                nexturl = Legacy.checkURL(nexturl);
                Trace.log("Login", "Change password successful - Redirecting to " + requestURI + "?" + nexturl);
                if (login != null && login.equals("true")) {
                    if ("WEB-CORE/blank.html".equals(nexturl)) {
                        this.logoff(request, response, servletContext);
                    }
                    this.login(request, response, servletContext);
                    return;
                }
                response.sendRedirect(response.encodeRedirectURL(requestURI + "?" + nexturl));
                return;
            }
            catch (IOException ioe) {
                throw new ServletException((Throwable)ioe);
            }
        } else {
            boolean first = true;
            String error = "To change password, you must provide: ";
            if (username == null || username.length() == 0) {
                error = error + "a username";
                first = false;
            }
            if (oldpassword == null || oldpassword.length() == 0) {
                error = error + (first ? "" : ", ") + "an oldpassword";
                first = false;
            }
            if (newpassword == null || newpassword.length() == 0) {
                error = error + (first ? "" : ", ") + "a new password";
                first = false;
            }
            if (database == null || database.length() == 0) {
                error = error + (first ? "" : ", ") + "a database";
                first = false;
            }
            if (nexturl != null && nexturl.length() != 0) throw new CommandException(error, "changePassword");
            error = error + (first ? "" : ", ") + "a nexturl";
            first = false;
            throw new CommandException(error, "changePassword");
        }
    }

    public void resetPassword(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException {
        ForwardUtil forward;
        block25: {
            String autocompleternd = request.getParameter("autocompleternd");
            String usernameParam = "username";
            if (autocompleternd != null && autocompleternd.length() > 0) {
                usernameParam = usernameParam + autocompleternd;
            }
            String database = request.getParameter("database");
            String username = request.getParameter(usernameParam);
            String email = request.getParameter("email");
            String key = request.getParameter("key");
            PropertyList logonProps = new PropertyList();
            LogonPageTag.setDefaults(logonProps);
            try {
                LogonPageTag.addOverrides(logonProps, new File(Configuration.getInstance().getApplicationHome()));
            }
            catch (SapphireException e) {
                Trace.logError("Failed to load sapphirelogon.xml overrides. Reason: " + e.getMessage(), e);
            }
            PropertyList text = logonProps.getPropertyList("text");
            forward = new ForwardUtil((ServletRequest)request);
            ConnectionProcessor cp = new ConnectionProcessor();
            String connectionid = cp.getConnectionid(database, "(system)", systemPassword);
            QueryProcessor queryProcessor = new QueryProcessor(connectionid);
            DataSet sysuser = queryProcessor.getPreparedSqlDataSet("SELECT sysuserid, email, moddt, disabledflag, authenticationtypeflag FROM sysuser WHERE lower( sysuserid ) = ? OR logonname = ?", new Object[]{username.toLowerCase(), username});
            if (sysuser.size() == 1) {
                if (sysuser.getValue(0, "disabledflag", "N").equals("N")) {
                    com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
                    boolean externalAuth = false;
                    String appbaseurl = "";
                    try {
                        WebAdminProcessor webAdminProcessor = new WebAdminProcessor(connectionid);
                        String authenticationPropertytree = configurationProcessor.getConfigProperty("com.labvantage.sapphire.server.authenticationpropertytree", "DefaultLDAPAuthentic");
                        String authenticationPropertytreeNode = configurationProcessor.getConfigProperty("com.labvantage.sapphire.server.authenticationpropertytreenode", "Default");
                        appbaseurl = configurationProcessor.getConfigProperty("com.labvantage.sapphire.server.webappbaseurl");
                        PropertyTree propertyTree = webAdminProcessor.getPropertyTree(authenticationPropertytree);
                        PropertyList propertyList = propertyTree.getNodePropertyList(authenticationPropertytreeNode, true);
                        if (propertyList.getProperty("enabled").equals("Y")) {
                            externalAuth = !sysuser.getValue(0, "authenticationtypeflag").equals("I");
                        }
                    }
                    catch (Exception e) {
                        externalAuth = true;
                    }
                    if (!externalAuth) {
                        int resetTimeout = 30;
                        try {
                            com.labvantage.sapphire.admin.system.ConfigurationProcessor configuration = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
                            String resetTimeoutStr = configuration.getProfileProperty("(system)", "PasswordResetMins", "30");
                            resetTimeout = Integer.parseInt(resetTimeoutStr);
                        }
                        catch (Exception e) {
                            Trace.logError("Exception in determining the Reset timeout system config property.", e);
                        }
                        Trace.logInfo("Password Reset Link Timeout duration: " + resetTimeout);
                        ActionProcessor actionProcessor = new ActionProcessor(connectionid);
                        String eKey = HttpUtil.encodeURIComponent(EncryptDecrypt.encrypt((username + ";" + sysuser.getValue(0, "moddt") + ";" + System.currentTimeMillis()).toLowerCase()));
                        if (key == null || key.length() == 0) {
                            if (sysuser.getValue(0, "email").equalsIgnoreCase(email)) {
                                String href = (appbaseurl.length() == 0 ? request.getRequestURL() : appbaseurl + "/rc") + "?command=resetpassword&username=" + username + "&database=" + database + "&key=" + eKey + "&sso=n";
                                HashMap<String, String> sendMailProps = new HashMap<String, String>();
                                sendMailProps.put("to", email);
                                sendMailProps.put("subject", text.getProperty("requestemailsubject"));
                                sendMailProps.put("mailformat", "html");
                                sendMailProps.put("message", StringUtil.replaceAll(StringUtil.replaceAll(text.getProperty("requestemailmessage"), "[resetpasswordlink]", href), "[resetlinkexpirytimeout]", String.valueOf(resetTimeout)));
                                try {
                                    actionProcessor.processAction("SendMail", "1", sendMailProps);
                                    forward.setProperty("errormsg", text.getProperty("resetpasswordgenericmessage"));
                                }
                                catch (Exception e) {
                                    Trace.logError("Failed to send reset password email. Reason: " + e.getMessage(), e);
                                    forward.setProperty("errormsg", text.getProperty("resetpasswordgenericmessage"));
                                }
                            } else {
                                Trace.logError("Failed to reset and send new password email for User: " + username + ". Reason: " + text.getProperty("invalidusernameemail"));
                                forward.setProperty("errormsg", text.getProperty("resetpasswordgenericmessage"));
                            }
                        } else {
                            String decryptedKey = EncryptDecrypt.decrypt(key);
                            String[] deKeyParts = StringUtil.split(decryptedKey, ";");
                            if (deKeyParts != null && deKeyParts.length == 3 && username.toLowerCase().equalsIgnoreCase(deKeyParts[0]) && sysuser.getValue(0, "moddt").toLowerCase().equalsIgnoreCase(deKeyParts[1])) {
                                try {
                                    long currentTimeMillis = System.currentTimeMillis();
                                    long resetTimeMillis = Long.parseLong(deKeyParts[2]);
                                    long elapsedMins = (currentTimeMillis - resetTimeMillis) / 60000L;
                                    if (elapsedMins < (long)resetTimeout) {
                                        String href = (appbaseurl.length() == 0 ? request.getRequestURL().substring(0, request.getRequestURL().indexOf("/rc")) : appbaseurl) + "/logon.jsp?sso=n";
                                        String passwordValidator = ConfigService.getConfigProperty("com.labvantage.sapphire.server.passwordvalidator", "DefaultPassword");
                                        String passwordValidatorNode = ConfigService.getConfigProperty("com.labvantage.sapphire.server.passwordvalidatornode", "Default");
                                        SafeSQL safeSQL = new SafeSQL();
                                        DataSet propertyTreeDS = queryProcessor.getPreparedSqlDataSet("SELECT objectname, valuetree FROM propertytree WHERE propertytreeid = " + safeSQL.addVar(passwordValidator), safeSQL.getValues(), true);
                                        BasePasswordValidator validator = (BasePasswordValidator)Class.forName(propertyTreeDS.getValue(0, "objectname")).newInstance();
                                        WebAdminProcessor webAdminProcessor = new WebAdminProcessor(connectionid);
                                        PropertyList properties = new PropertyList();
                                        properties.setPropertyTree(propertyTreeDS.getClob(0, "valuetree", ""), passwordValidatorNode, webAdminProcessor.getPropertyDefinitionList(passwordValidator));
                                        validator.startPasswordHandler();
                                        String newpassword = validator.generatePassword(properties);
                                        validator.endPasswordHandler();
                                        ActionBlock actionBlock = new ActionBlock();
                                        HashMap<String, String> editSDIProps = new HashMap<String, String>();
                                        editSDIProps.put("sdcid", "User");
                                        editSDIProps.put("keyid1", sysuser.getValue(0, "sysuserid"));
                                        editSDIProps.put("password", newpassword);
                                        editSDIProps.put("changepasswordflag", "Y");
                                        editSDIProps.put("bypassformatcheck", "Y");
                                        actionBlock.setAction("editsdi", "EditSDI", "1", editSDIProps);
                                        HashMap<String, String> sendMailProps = new HashMap<String, String>();
                                        sendMailProps.put("to", sysuser.getValue(0, "email"));
                                        sendMailProps.put("subject", text.getProperty("resetemailsubject"));
                                        sendMailProps.put("mailformat", "html");
                                        sendMailProps.put("message", StringUtil.replaceAll(StringUtil.replaceAll(text.getProperty("resetemailmessage"), "[resetpasswordlink]", href), "[newpassword]", newpassword));
                                        actionBlock.setAction("sendmail", "SendMail", "1", sendMailProps);
                                        actionProcessor.processActionBlock(actionBlock);
                                        forward.setProperty("resetmsg", text.getProperty("resetemailsent"));
                                        break block25;
                                    }
                                    forward.setProperty("resetmsg", text.getProperty("expiredresetlink"));
                                }
                                catch (Exception e) {
                                    Trace.logError("Failed to reset and send new password email. Reason: " + e.getMessage(), e);
                                    forward.setProperty("errormsg", text.getProperty("emailsendfail"));
                                }
                            } else {
                                forward.setProperty("resetmsg", text.getProperty("expiredresetlink"));
                            }
                        }
                    } else {
                        Trace.logError("Failed to reset and send new password email for User: " + username + ". Reason: " + text.getProperty("externalauthentication"));
                        forward.setProperty("errormsg", text.getProperty("resetpasswordgenericmessage"));
                    }
                } else {
                    Trace.logError("Failed to reset and send new password email for User: " + username + ". Reason: " + text.getProperty("forgotdisableduser"));
                    forward.setProperty("errormsg", text.getProperty("resetpasswordgenericmessage"));
                }
            } else {
                Trace.logError("Failed to reset and send new password email for User: " + username + ". Reason: " + text.getProperty("invalidusernameemail"));
                forward.setProperty("errormsg", text.getProperty("resetpasswordgenericmessage"));
            }
        }
        String redirectURL = request.getContextPath() + "/logon.jsp";
        forward.setProperty("mode", "rp");
        PrintWriter output = response.getWriter();
        output.println(forward.getForm("", redirectURL, "post", true));
        output.close();
    }

    private void updateStartupInfo(String connectionid, String userid, HttpSession session) throws SapphireException {
        com.labvantage.sapphire.admin.system.ConfigurationProcessor cp = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionid);
        String startupType = cp.getProfileProperty(userid, "startuptype");
        String startupURL = cp.getProfileProperty(userid, "logonpageurl");
        if ("Last".equals(startupType)) {
            String url;
            QueryProcessor queryProcessor = new QueryProcessor(connectionid);
            String sql = "SELECT webpagelog.webpagelogid, title FROM webpagelog, webpagelogtitle WHERE webpagelog.webpagelogid = webpagelogtitle.webpagelogid and webpagelog.sysuserid = ? order by requestdt desc";
            Object[] params = new Object[]{userid};
            DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, params);
            if (ds.getRowCount() > 0 && !(url = "rc?command=state&state=" + ds.getString(0, "webpagelogid")).equals(startupURL)) {
                String guimode = "";
                try {
                    ConnectionProcessor connectionProcessor = new ConnectionProcessor(connectionid);
                    ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionid);
                    guimode = connectionInfo.getGuiMode();
                }
                catch (Exception connectionProcessor) {
                    // empty catch block
                }
                String old = cp.getProfileProperty(userid, guimode + "logonpageurl", "");
                if (old.length() > 0 && (old.contains("&state=") || old.contains("&history="))) {
                    String webpagelog = "";
                    int i = old.indexOf("&state=");
                    webpagelog = i > -1 ? old.substring(i + 7) : old.substring(old.indexOf("&history=") + 9);
                    int o = webpagelog.indexOf("&");
                    if (o > -1) {
                        webpagelog.substring(0, webpagelog.indexOf("&"));
                    }
                    if (!webpagelog.equals(ds.getString(0, "webpagelogid"))) {
                        String updateSql1 = "UPDATE webpagelog SET logtypeflag='N' WHERE sysuserid = ? and logtypeflag='S' and webpagelogid=?";
                        queryProcessor.execPreparedUpdate(updateSql1, new Object[]{userid, webpagelog});
                        cp.setProfileProperty(userid, guimode + "logonpageurl", url);
                        String updateSql2 = "UPDATE webpagelog SET logtypeflag='S' WHERE webpagelogid=?";
                        queryProcessor.execPreparedUpdate(updateSql2, new Object[]{ds.getString(0, "webpagelogid")});
                    }
                } else {
                    cp.setProfileProperty(userid, guimode + "logonpageurl", url);
                    String updateSql2 = "UPDATE webpagelog SET logtypeflag='S' WHERE webpagelogid=?";
                    queryProcessor.execPreparedUpdate(updateSql2, new Object[]{ds.getString(0, "webpagelogid")});
                }
                Trace.log("Startup page set to:" + url);
            }
            try {
                Object o = session.getAttribute("_menucache_last");
                if (o != null) {
                    cp.setProfileProperty(userid, "logonmenu", o.toString());
                }
                if ((o = session.getAttribute("_sidebarcache_last")) != null) {
                    cp.setProfileProperty(userid, "logongroup", o.toString());
                }
            }
            catch (Exception e) {
                Trace.logWarn("Could not update startup menu and/or group.");
            }
        }
    }

    public void processAcknowledgement(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        int newrow;
        DataSet signedStatements;
        LVDefaultStatementHandler statementHandler = (LVDefaultStatementHandler)request.getSession().getAttribute("statementHandler");
        String username = statementHandler.getUsername();
        String statementid = statementHandler.getStatementId();
        String versionid = statementHandler.getStatementVersionId();
        String statementype = statementHandler.getStatementType();
        String doNotPromptAgain = statementHandler.getDoNotPromptAgain();
        String isPersistent = request.getParameter("persistentflag");
        if (doNotPromptAgain == null) {
            doNotPromptAgain = "N";
        }
        if ((signedStatements = (DataSet)request.getSession().getAttribute("signedStatements")) == null) {
            signedStatements = new DataSet();
        }
        if (statementype.equals("Agreement")) {
            newrow = signedStatements.addRow();
            signedStatements.setString(newrow, "sysuserid", username);
            signedStatements.setString(newrow, "statementid", statementid);
            signedStatements.setString(newrow, "statementversionid", versionid);
            signedStatements.setString(newrow, "acknowledgeddt", "n");
            request.getSession().setAttribute("signedStatements", (Object)signedStatements);
        } else if (statementype.equals("Announcement") && doNotPromptAgain.equals("Y") && !isPersistent.equals("Y")) {
            newrow = signedStatements.addRow();
            signedStatements.setString(newrow, "sysuserid", username);
            signedStatements.setString(newrow, "statementid", statementid);
            signedStatements.setString(newrow, "statementversionid", versionid);
            signedStatements.setString(newrow, "acknowledgeddt", "n");
            request.getSession().setAttribute("signedStatements", (Object)signedStatements);
        }
        String statementCounter = statementHandler.getStatementCounter();
        if (statementCounter == null) {
            statementCounter = "0";
        }
        int counter = Integer.parseInt(statementCounter);
        statementCounter = Integer.toString(++counter);
        statementHandler.setStatementCounter(statementCounter);
    }

    public void processStatements(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, String statementdb) throws ServletException {
        DataSet signedStatements = (DataSet)request.getSession().getAttribute("signedStatements");
        if (signedStatements == null) {
            return;
        }
        String sysconnectionid = new ConnectionProcessor().getConnectionid(statementdb, "(system)", systemPassword);
        try {
            StatementsUtil.addUserStatements(signedStatements, new ActionProcessor(sysconnectionid));
        }
        catch (ActionException aex) {
            aex.printStackTrace();
        }
    }

    public static void setSystemPassword(String systemPassword) {
        Login.systemPassword = systemPassword;
    }

    public boolean isWebSSO() {
        return webSSOHandler != null;
    }

    public boolean isWebSSOSession(HttpServletRequest request) {
        try {
            return request.getSession().getAttribute("WebSSOUsername") != null && ((String)request.getSession().getAttribute("WebSSOUsername")).length() > 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static BaseWebSSOHandler getWebSSOHandler() {
        return webSSOHandler;
    }

    static {
        webSSOHandler = null;
    }
}

