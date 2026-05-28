/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  net.sf.jasperreports.j2ee.servlets.ImageServlet
 */
package com.labvantage.sapphire.servlet;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.workflow.SetTaskExecIncomplete;
import com.labvantage.sapphire.admin.security.LVDefault2FAHandler;
import com.labvantage.sapphire.admin.security.LVDefaultStatementHandler;
import com.labvantage.sapphire.admin.system.CheckConnectionProcessor;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.ajax.operations.ClientTranslation;
import com.labvantage.sapphire.ajax.operations.PingConnection;
import com.labvantage.sapphire.ajax.operations.PingRset;
import com.labvantage.sapphire.modules.search.UpdateSearchOperation;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.command.ActionRequest;
import com.labvantage.sapphire.servlet.command.AjaxRequest;
import com.labvantage.sapphire.servlet.command.AttachmentRequest;
import com.labvantage.sapphire.servlet.command.DownloadRequest;
import com.labvantage.sapphire.servlet.command.HandlerRequest;
import com.labvantage.sapphire.servlet.command.ImageRequest;
import com.labvantage.sapphire.servlet.command.JspRequest;
import com.labvantage.sapphire.servlet.command.Legacy;
import com.labvantage.sapphire.servlet.command.Login;
import com.labvantage.sapphire.servlet.command.LoginException;
import com.labvantage.sapphire.servlet.command.OperationRequest;
import com.labvantage.sapphire.servlet.command.PasswordException;
import com.labvantage.sapphire.servlet.command.Ping;
import com.labvantage.sapphire.servlet.command.ResourceRequest;
import com.labvantage.sapphire.servlet.command.TagRequest;
import com.labvantage.sapphire.servlet.command.UploadRequest;
import com.labvantage.sapphire.servlet.command.ViewLabel;
import com.labvantage.sapphire.servlet.command.ViewReport;
import com.labvantage.sapphire.servlet.command.Wizard;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import org.json.JSONObject;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseHttpServlet;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.ForwardUtil;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RequestController
extends BaseHttpServlet {
    public static final String APP_RESOURCE = "appresource";
    private static HashMap webAppLogons = new HashMap();
    private static boolean expirePages = false;
    private Login login = new Login();
    private JspRequest jspRequest = new JspRequest();
    private AttachmentRequest attachmentRequest = new AttachmentRequest();
    private ResourceRequest resourceRequest = new ResourceRequest();
    private UploadRequest uploadRequest = new UploadRequest();
    private DownloadRequest downloadRequest = new DownloadRequest();
    private Wizard wizard = new Wizard();
    private TagRequest tagRequest = new TagRequest();
    private Ping ping = new Ping();
    private ActionRequest actionRequest = new ActionRequest();
    private OperationRequest operationRequest = new OperationRequest();
    private AjaxRequest ajaxRequest = new AjaxRequest();
    private ImageRequest imageRequest = new ImageRequest();
    private HandlerRequest handlerRequest = new HandlerRequest();
    private ViewReport viewReport = new ViewReport();
    private ViewLabel viewLabel = new ViewLabel();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    private boolean isValidConnection(HttpServletRequest request, RequestContext requestContext, boolean ignoreLoginURL) throws LoginException {
        String connectionid = "";
        String msg = "In order to access this resource, you must login.";
        if (requestContext != null) {
            connectionid = requestContext.getConnectionId();
        }
        if (connectionid != null && connectionid.length() > 0) {
            if (new CheckConnectionProcessor(connectionid).isConnectionValid(connectionid)) {
                return true;
            }
            if (ignoreLoginURL) {
                throw new LoginException(request.getQueryString(), HttpUtil.getRequestMap((ServletRequest)request), msg, true);
            }
            throw new LoginException(request.getQueryString(), msg);
        }
        if (ignoreLoginURL) {
            throw new LoginException(request.getQueryString(), HttpUtil.getRequestMap((ServletRequest)request), msg, true);
        }
        throw new LoginException(request.getQueryString(), msg);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        block144: {
            Trace.startThreadMDCBlank("Browser");
            request.setCharacterEncoding("UTF-8");
            String command = request.getParameter("command");
            String requestURI = request.getRequestURI();
            if (requestURI.indexOf("/rc/login") > 0) {
                command = "login";
            }
            if (requestURI.indexOf("/jasperReport/image") > 0) {
                ImageServlet imageServlet = new ImageServlet();
                imageServlet.service(request, response);
            }
            if (FileUpload.getDefaultmaxsize() < 0L) {
                FileUpload.setDefaultMaxsize(FileUpload.getServletMaxsize(request.getServletContext()));
            }
            try {
                if (!"n".equalsIgnoreCase(request.getParameter("html"))) {
                    response.setContentType("text/html; charset=UTF-8");
                }
                response.setHeader("X-UA-Compatible", "IE=edge");
                if (new Browser(request).isChrome()) {
                    response.setHeader("X-XSS-Protection", "0");
                }
                SapphireConnection sapphireConnection = null;
                if (expirePages) {
                    com.labvantage.sapphire.util.http.HttpUtil.expireResponse(response);
                }
                this.doInit();
                if (request.getParameter("keepalive") != null && "N".equalsIgnoreCase(request.getParameter("keepalive"))) {
                    Trace.addNoKeepAliveThread();
                }
                if ((command = Legacy.checkCommand(command, request, this.getServletContext())) != null && command.length() > 0) {
                    RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
                    HttpUtil httpUtil = new HttpUtil(request, response);
                    String connectionid = "";
                    boolean rtl = false;
                    String ajaxclass = request.getParameter("ajaxclass");
                    boolean doNotLogRequest = PingConnection.class.getName().equals(ajaxclass) || PingRset.class.getName().equals(ajaxclass) || ClientTranslation.class.getName().equals(ajaxclass) || "image".equals(command) && request.getParameter("file") != null;
                    String qs = request.getQueryString();
                    if (qs == null) {
                        qs = "";
                    }
                    String commandParam = "";
                    try {
                        if (!(command.equalsIgnoreCase("login") || "changepassword".equals(command) || "resetpassword".equals(command) || "sendpasscode".equals(command) || "acknowledgeagreement".equals(command) || "processstatements".equals(command))) {
                            sapphireConnection = sapphireConnection != null ? sapphireConnection : SecurityPolicyUtil.getSapphireConnection(command, request, response);
                            connectionid = sapphireConnection != null ? sapphireConnection.getConnectionId() : "";
                            boolean bl = rtl = sapphireConnection != null && sapphireConnection.isRtl();
                            if (connectionid == null || connectionid.length() == 0) {
                                connectionid = httpUtil.getCookieValue("connectionid");
                                Trace.setThreadMDCConnectionid(connectionid);
                                if (!(connectionid != null && connectionid.length() != 0 || !this.login.isWebSSO() || "viewreport".equals(command) && "HEAD".equals(request.getMethod()))) {
                                    if ("logoff".equals(command)) {
                                        this.login.logoffWebSSO(request, response);
                                    } else {
                                        response.sendRedirect(request.getContextPath() + "/rc/login" + (qs.length() > 0 ? "?" + qs : ""));
                                    }
                                    return;
                                }
                            } else {
                                Trace.setThreadMDCConnectionid(connectionid);
                                httpUtil.setCookieValue("connectionid", connectionid, false, SecurityPolicyUtil.httpOnlyRequestControllerCookie);
                            }
                            if (!doNotLogRequest) {
                                Trace.logInfo("RequestController", "++++++++++ Received request " + qs + " for [" + connectionid + "] ++++++++++");
                                Trace.logInfo("RequestController", "[#" + Thread.currentThread().getId() + "] = " + Thread.currentThread().getName());
                            }
                        } else if (!doNotLogRequest) {
                            Trace.logInfo("RequestController", "++++++++++ Received request: " + qs + " ++++++++++");
                            Trace.logInfo("RequestController", "[#" + Thread.currentThread().getId() + "] = " + Thread.currentThread().getName());
                        }
                        boolean isValidSession = false;
                        try {
                            request.getSession().getAttribute("dummy");
                            isValidSession = true;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        if (requestContext == null) {
                            String cachedRequestContext = request.getParameter("__crc");
                            boolean removeAllCrc = "sdiform".equals(command);
                            if (cachedRequestContext != null && cachedRequestContext.length() > 0 || removeAllCrc) {
                                Enumeration e = request.getSession().getAttributeNames();
                                while (e.hasMoreElements()) {
                                    String attribute = (String)e.nextElement();
                                    if (!attribute.startsWith("crc_") || attribute.equals(cachedRequestContext) && !removeAllCrc) continue;
                                    request.getSession().removeAttribute(attribute);
                                }
                                if (!removeAllCrc && isValidSession) {
                                    requestContext = (RequestContext)request.getSession().getAttribute(cachedRequestContext);
                                }
                            }
                            if (requestContext == null) {
                                requestContext = new RequestContext(new PropertyList());
                                requestContext.setConnectionId(connectionid);
                                requestContext.setRtl(rtl);
                                requestContext.setProperty("__registeredpage", "false");
                                requestContext.setControlledPage(true);
                                requestContext.setRequestId(String.valueOf(System.currentTimeMillis()));
                            }
                            request.setAttribute("RequestContext", (Object)requestContext);
                        }
                        if (requestContext.copyRequestParameters()) {
                            Enumeration e = request.getParameterNames();
                            while (e.hasMoreElements()) {
                                String propertyid = (String)e.nextElement();
                                if (propertyid.equalsIgnoreCase("__pagedirectives") && !qs.contains("__pagedirectives=")) {
                                    String orgRequestOverrides = request.getParameter(propertyid);
                                    if (orgRequestOverrides == null || orgRequestOverrides.trim().length() == 0) {
                                        orgRequestOverrides = "{}";
                                    }
                                    try {
                                        PropertyList pageDirectivesFromSession;
                                        PropertyList pl = new PropertyList(new JSONObject(orgRequestOverrides));
                                        pl.setId("pagedirectives");
                                        if (SecurityPolicyUtil.allowPageDirectives(connectionid, "ajax")) {
                                            if (pl.get("pageDirId") != null && pl.get("pageDirId").toString() != "") {
                                                pageDirectivesFromSession = new PropertyList(new JSONObject(request.getSession().getAttribute(pl.get("pageDirId").toString()).toString()));
                                                String mergeXml = pl.toXMLString();
                                                pageDirectivesFromSession.setPropertyList(mergeXml, true);
                                                requestContext.setProperty("pagedirectives", pageDirectivesFromSession);
                                                continue;
                                            }
                                            requestContext.setProperty("pagedirectives", pl);
                                            continue;
                                        }
                                        if (pl.get("pageDirId") != null && pl.get("pageDirId").toString() != "") {
                                            pageDirectivesFromSession = new PropertyList(new JSONObject(request.getSession().getAttribute(pl.get("pageDirId").toString()).toString()));
                                            if (pl.get("restrictivewhere") != null) {
                                                pageDirectivesFromSession.remove("restrictivewhere");
                                                pageDirectivesFromSession.setProperty("restrictivewhere", pl.get("restrictivewhere").toString());
                                            }
                                            requestContext.setProperty("pagedirectives", pageDirectivesFromSession);
                                            continue;
                                        }
                                        throw new ServletException("Invalid request");
                                    }
                                    catch (Exception je) {
                                        this.logError("Could not load requestoverrides.", je);
                                        continue;
                                    }
                                }
                                requestContext.setProperty(propertyid, request.getParameter(propertyid));
                            }
                        }
                        commandParam = requestContext.getProperty(command);
                        if (command.equalsIgnoreCase("action")) {
                            if (commandParam.length() == 0) {
                                requestContext.getProperty("actionclass");
                            }
                        } else if (command.equalsIgnoreCase("operation")) {
                            if (commandParam.length() == 0) {
                                requestContext.getProperty("operationclass");
                            }
                        } else if (command.equalsIgnoreCase("ajax") && (commandParam = requestContext.getProperty("ajaxclass")).length() == 0) {
                            requestContext.getProperty("ajaxservice");
                        }
                        if (Trace.stats && !doNotLogRequest) {
                            Trace.startRequest(command, commandParam);
                        }
                        if (sapphireConnection != null) {
                            String rsetlist_cookie;
                            Set keys;
                            HashMap cookies;
                            PropertyList userPreferences;
                            String sysuserid = sapphireConnection.getSysuserId();
                            if (isValidSession) {
                                userPreferences = (PropertyList)request.getSession().getAttribute("userconfig");
                                if (userPreferences == null) {
                                    userPreferences = new PropertyList();
                                    request.getSession().setAttribute("userconfig", (Object)userPreferences);
                                    QueryProcessor qp = new QueryProcessor(connectionid);
                                    SafeSQL safeSQL = new SafeSQL();
                                    DataSet userConfigProps = qp.getPreparedSqlDataSet("SELECT propertyid, propertyvalue FROM profileproperty WHERE profileid='System' AND sysuserid=" + safeSQL.addVar(sysuserid) + " AND propertyid like ('userconfig_%' )", safeSQL.getValues());
                                    if (userConfigProps.size() > 500) {
                                        qp.execPreparedUpdate("DELETE FROM profileproperty WHERE profileid='System' AND sysuserid=? AND propertyid like ( 'userconfig_%' )", new Object[]{sysuserid});
                                    }
                                    for (int i = 0; i < userConfigProps.size(); ++i) {
                                        String propertyid = userConfigProps.getValue(i, "propertyid");
                                        String value = userConfigProps.getValue(i, "propertyvalue");
                                        userPreferences.setProperty(propertyid.substring(11), value);
                                    }
                                }
                            } else {
                                userPreferences = new PropertyList();
                            }
                            if (command.equals("logoff") || command.equals("page") || command.equals("file") || command.equals("ajax") && !doNotLogRequest) {
                                cookies = httpUtil.getCookies();
                                keys = cookies.keySet();
                                ConfigurationProcessor configProcessor = new ConfigurationProcessor(connectionid);
                                for (String cookieid : keys) {
                                    String cookieValue = (String)cookies.get(cookieid);
                                    if (!("connectionid".equals(cookieid) || "JSESSIONID".equals(cookieid) || cookieValue == null || cookieValue.equals("(null)"))) {
                                        Trace.logInfo("RequestController", "[Cookie: " + cookieid + "=" + cookieValue + "]");
                                    }
                                    if (cookieid.toLowerCase().startsWith("userconfig_")) {
                                        String newcookieid = cookieid.substring("userconfig_".length());
                                        if ("(null)".equals(cookieValue)) {
                                            userPreferences.deleteProperty(newcookieid);
                                            if (connectionid != null && connectionid.length() > 0) {
                                                configProcessor.setProfileProperty(sysuserid, cookieid, "");
                                            }
                                        } else {
                                            userPreferences.setProperty(newcookieid, cookieValue);
                                            if (connectionid != null && connectionid.length() > 0) {
                                                configProcessor.setProfileProperty(sysuserid, cookieid, cookieValue);
                                            }
                                        }
                                        httpUtil.removeCookie(cookieid);
                                        continue;
                                    }
                                    if (!cookieid.equals("setincompletetaskexecid")) continue;
                                    PropertyList actionProps = new PropertyList();
                                    actionProps.setProperty("taskexecid", cookieValue);
                                    ActionProcessor ap = new ActionProcessor(connectionid);
                                    ap.processActionClass(SetTaskExecIncomplete.class.getName(), actionProps);
                                    httpUtil.removeCookie(cookieid);
                                }
                            } else if (command.equals("logon")) {
                                cookies = httpUtil.getCookies();
                                keys = cookies.keySet();
                                for (String cookieid : keys) {
                                    if (!cookieid.toLowerCase().startsWith("userconfig_") && !cookieid.equals("setincompletetaskexecid")) continue;
                                    httpUtil.removeCookie(cookieid);
                                }
                            }
                            if (requestContext.getProperty("searchoperation").length() > 0 && requestContext.getProperty("searchindexitem").length() > 0 && (command.equals("page") || command.equals("file") || command.equals("viewattachment"))) {
                                ActionProcessor ap = new ActionProcessor(connectionid);
                                ap.processActionClass(UpdateSearchOperation.class.getName(), requestContext.getPropertyList());
                                requestContext.getPropertyList().remove("searchoperation");
                                requestContext.getPropertyList().remove("searchindexitem");
                            }
                            requestContext.setProperty("userconfig", userPreferences);
                            if (!(command.equalsIgnoreCase("ping") || command.equalsIgnoreCase("login") || command.equalsIgnoreCase("ajax") || (rsetlist_cookie = httpUtil.getCookieValue("rsetlist")) == null || rsetlist_cookie.length() <= 0 || rsetlist_cookie.equalsIgnoreCase("(null)"))) {
                                this.ping.clearRSets(request, this.getServletContext(), rsetlist_cookie);
                                httpUtil.setCookieValue("rsetlist", "");
                            }
                        }
                        if (isValidSession) {
                            String currentlayoutreplace;
                            String currentlayoutnode;
                            String currentlayout = request.getParameter("currentlayout");
                            if (currentlayout != null && currentlayout.length() > 0) {
                                request.getSession().setAttribute("currentlayout", (Object)currentlayout);
                            }
                            if ((currentlayoutnode = request.getParameter("currentlayoutnode")) != null && currentlayoutnode.length() > 0) {
                                request.getSession().setAttribute("currentlayoutnode", (Object)currentlayoutnode);
                            }
                            if ((currentlayoutreplace = request.getParameter("currentlayoutreplace")) != null && currentlayoutreplace.length() > 0) {
                                request.getSession().setAttribute("currentlayoutreplace", (Object)currentlayoutreplace);
                            }
                        }
                        if (command.equalsIgnoreCase("login")) {
                            this.login.login(request, response, this.getServletContext());
                            break block144;
                        }
                        if (command.equalsIgnoreCase("logoff")) {
                            this.login.logoff(request, response, this.getServletContext());
                            break block144;
                        }
                        if (command.equalsIgnoreCase("changepassword")) {
                            this.login.changePassword(request, response, this.getServletContext());
                            break block144;
                        }
                        if (command.equalsIgnoreCase("resetpassword")) {
                            this.login.resetPassword(request, response, this.getServletContext());
                            break block144;
                        }
                        if (command.equalsIgnoreCase("sendpasscode")) {
                            LVDefault2FAHandler lvDefault2FAHandler = (LVDefault2FAHandler)request.getSession().getAttribute("baseWebMFAHandler");
                            if (lvDefault2FAHandler != null) {
                                lvDefault2FAHandler.sendPasscode(request, response);
                                break block144;
                            }
                            throw new ServletException("Invalid request to send passcode to email");
                        }
                        if (command.equalsIgnoreCase("acknowledgeagreement")) {
                            String id = request.getParameter("statementid");
                            String versionid = request.getParameter("statementversionid");
                            String type = request.getParameter("statementtype");
                            String persistentflag = request.getParameter("persistentflag");
                            LVDefaultStatementHandler statementHandler = (LVDefaultStatementHandler)request.getSession().getAttribute("statementHandler");
                            DataSet statements = statementHandler.getStatements();
                            String user = statementHandler.getUsername();
                            statementHandler.setStatementId(id);
                            statementHandler.setStatementVersionId(versionid);
                            statementHandler.setStatementType(type);
                            if (!persistentflag.equals("Y")) {
                                String donotpromptagain = request.getParameter("donotpromptagain");
                                statementHandler.setDoNotPromptAgain(donotpromptagain);
                            }
                            String counter = statementHandler.getStatementCounter();
                            this.login.processAcknowledgement(request, response, this.getServletContext());
                            counter = statementHandler.getStatementCounter();
                            int ctr = Integer.parseInt(counter);
                            if (ctr >= statements.size()) {
                                String statementdb = statementHandler.getDatabase();
                                this.login.processStatements(request, response, this.getServletContext(), statementdb);
                                sapphireConnection = sapphireConnection != null ? sapphireConnection : SecurityPolicyUtil.getSapphireConnection(command, request, response);
                                connectionid = sapphireConnection != null ? sapphireConnection.getConnectionId() : "";
                                ConfigurationProcessor config = new ConfigurationProcessor(connectionid);
                                Browser b = new Browser(request);
                                String guiMode = b.getGUIMode().getId();
                                String nexturl = config.getProfileProperty(user, guiMode + "" + "logonpageurl");
                                if (nexturl != null && nexturl.isEmpty()) {
                                    nexturl = statementHandler.getAuthenticationProps().getAttribute("nexturl");
                                }
                                boolean promptForJobType = false;
                                String jobtypelist = sapphireConnection.getJobtypeList();
                                ForwardUtil forward = new ForwardUtil((ServletRequest)request);
                                if (sapphireConnection.getCurrentJobtype() != null) {
                                    String jobtypeoption = config.getProfileProperty(user, "jobtypeoption");
                                    promptForJobType = "alwaysprompt".equals(jobtypeoption);
                                    if (promptForJobType) {
                                        promptForJobType = jobtypelist != null && jobtypelist.contains(";");
                                    } else {
                                        request.getSession().removeAttribute(connectionid);
                                    }
                                }
                                if (promptForJobType) {
                                    forward.setProperty("jobtypelist", jobtypelist);
                                    RequestContext crc = (RequestContext)request.getAttribute("RequestContext");
                                    crc.setProperty("currentjobtype", sapphireConnection.getCurrentJobtype());
                                    forward.setProperty("currentjobtype", sapphireConnection.getCurrentJobtype());
                                    request.getSession().setAttribute(connectionid, request.getAttribute("RequestContext"));
                                    PrintWriter output = response.getWriter();
                                    String redirectURL = request.getContextPath() + "/rc?command=file&file=WEB-CORE/modules/security/jobtype.jsp";
                                    output.println(forward.getForm("", redirectURL, "post", true));
                                    output.close();
                                } else {
                                    response.sendRedirect(request.getContextPath() + "/" + nexturl);
                                }
                            } else {
                                request.getRequestDispatcher("/statements.jsp?").include((ServletRequest)request, (ServletResponse)response);
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("processstatements")) {
                            LVDefaultStatementHandler statementHandler = (LVDefaultStatementHandler)request.getSession().getAttribute("statementHandler");
                            String statementdb = statementHandler.getDatabase();
                            this.login.processStatements(request, response, this.getServletContext(), statementdb);
                            ConnectionProcessor connectionProcessor = new ConnectionProcessor(connectionid);
                            connectionProcessor.clearConnection(connectionid);
                            response.sendRedirect(request.getContextPath() + "/rc?command=logoff");
                            break block144;
                        }
                        if (command.equalsIgnoreCase("changejobtype")) {
                            this.login.changeJobtype(request, response, this.getServletContext());
                            break block144;
                        }
                        if (command.equalsIgnoreCase("ping")) {
                            this.ping.ping(request, response, this.getServletContext());
                            break block144;
                        }
                        if (command.equalsIgnoreCase("file") || command.equalsIgnoreCase("page") || command.equalsIgnoreCase("bulletin") || command.equalsIgnoreCase("favorite") || command.equalsIgnoreCase("gizmo")) {
                            if (this.isValidConnection(request, requestContext, true)) {
                                this.jspRequest.requestObject(command, request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("history") || command.equalsIgnoreCase("state")) {
                            this.log("State command found and should have been preprocessed by filter. Therefore assume connection has expired.");
                            throw new LoginException(request.getQueryString(), HttpUtil.getRequestMap((ServletRequest)request), "Connection timed out or invalid. All prior locks have been cleared. Please login again.", true);
                        }
                        if (command.equalsIgnoreCase(APP_RESOURCE)) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.resourceRequest.processRequest(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("viewattachment")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.attachmentRequest.view(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("attachment")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.attachmentRequest.manage(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("upload")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.uploadRequest.upload(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("download")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.downloadRequest.download(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("viewreport")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.viewReport.processRequest((Servlet)this, request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("viewlabel")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.viewLabel.processRequest((Servlet)this, request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("wizard")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.wizard.view((Servlet)this, request, response);
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("sdiform")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.tagRequest.sdiForm(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("action")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.actionRequest.processAction(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("operation")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.operationRequest.processOperation(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("requesthandler")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.handlerRequest.processRequest(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("ajax")) {
                            if (this.isValidConnection(request, requestContext, false)) {
                                this.ajaxRequest.processAjaxRequest(request, response, this.getServletContext(), this);
                            }
                            break block144;
                        }
                        if (command.equalsIgnoreCase("image")) {
                            if (request.getParameter("qrdata") != null && !request.getParameter("qrdata").isEmpty()) {
                                this.imageRequest.processRequest(request, response, this.getServletContext());
                            } else if (this.isValidConnection(request, requestContext, false)) {
                                this.imageRequest.processRequest(request, response, this.getServletContext());
                            }
                            break block144;
                        }
                        Trace.logError("Invalid command provided.");
                        throw new ServletException("Invalid request");
                    }
                    finally {
                        if (!doNotLogRequest) {
                            Trace.logInfo("RequestController", "[#" + Thread.currentThread().getId() + "] = " + Thread.currentThread().getName());
                            Trace.logInfo("RequestController", "++++++++++ Completed request: " + qs + " ++++++++++");
                            if (Trace.stats) {
                                Trace.endRequest(command, commandParam);
                            }
                        }
                    }
                }
                throw new ServletException("You need to pass a command");
            }
            catch (PasswordException pe) {
                request.setAttribute("javax.servlet.error.exception", (Object)pe);
                request.getRequestDispatcher("WEB-CORE/error/password.jsp").include((ServletRequest)request, (ServletResponse)response);
            }
            catch (LoginException le) {
                if (this.login.isWebSSOSession(request)) {
                    this.login.logoff(request, response, this.getServletContext());
                } else {
                    request.setAttribute("javax.servlet.error.exception", (Object)le);
                    String logonpage = RequestController.getLogonPage(this.getServletContext());
                    request.getRequestDispatcher(logonpage).include((ServletRequest)request, (ServletResponse)response);
                }
            }
            catch (ServletException se) {
                request.setAttribute("javax.servlet.error.exception", (Object)se);
                if (!response.isCommitted()) {
                    if (command != null && command.equalsIgnoreCase("ajax")) {
                        AjaxResponse.handleException(request, response, se);
                    } else {
                        request.getRequestDispatcher("/WEB-CORE/error/error.jsp").include((ServletRequest)request, (ServletResponse)response);
                    }
                }
            }
            catch (Throwable t) {
                request.setAttribute("javax.servlet.error.exception", (Object)new ServletException(t));
                if (!response.isCommitted()) {
                    if (command != null && command.equalsIgnoreCase("ajax")) {
                        AjaxResponse.handleException(request, response, t);
                    } else {
                        request.getRequestDispatcher("/WEB-CORE/error/error.jsp").include((ServletRequest)request, (ServletResponse)response);
                    }
                }
            }
            finally {
                Trace.clearNoKeepAliveThread();
                Trace.clearThreadMDC();
            }
        }
    }

    public static String getLogonPage(ServletContext servletContext) {
        String logonpage = (String)webAppLogons.get(servletContext.getServletContextName());
        if (logonpage == null || logonpage.length() == 0) {
            try {
                logonpage = servletContext.getInitParameter("logonpage");
                if (logonpage == null || logonpage.length() == 0) {
                    logonpage = "logon.jsp";
                    String[] metatags = StringUtil.getTokens(FileUtil.getFileString(new File(servletContext.getRealPath("index.html"))), "<meta", ">");
                    for (int i = 0; i < metatags.length; ++i) {
                        if (metatags[i].toLowerCase().indexOf("refresh") <= -1) continue;
                        int pos = metatags[i].indexOf("url");
                        pos = metatags[i].indexOf("=", pos);
                        logonpage = metatags[i].substring(pos + 1, metatags[i].indexOf("\"", pos)).trim();
                    }
                }
            }
            catch (Exception e) {
                Logger.logWarn("RequestController", "Failed to locate logon page - defaulting to logon.jsp. Reason: " + e.getMessage());
                logonpage = "logon.jsp";
            }
            webAppLogons.put(servletContext.getServletContextName(), logonpage);
        }
        return logonpage;
    }

    static {
        try {
            expirePages = "true".equals(ConfigService.getConfigProperty("com.labvantage.sapphire.server.expirepages", "false"));
        }
        catch (Exception e) {
            Trace.logInfo("Cannot get com.labvantage.sapphire.server.expirepages. Default to false.");
        }
    }
}

