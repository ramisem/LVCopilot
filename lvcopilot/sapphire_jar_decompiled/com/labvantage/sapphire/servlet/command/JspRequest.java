/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.XSS;
import com.labvantage.sapphire.admin.system.CheckConnectionProcessor;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.servlet.command.CommandException;
import com.labvantage.sapphire.servlet.command.LoginException;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.LogContext;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class JspRequest {
    private static final String MESSAGE_ACCESS_DENIED = "Access to the requested resource has been denied";

    public void requestObject(String command, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        if (requestContext.getProperty("__cached").equals("Y")) {
            Trace.logInfo("JSPREQUEST", "Using cached requestContext: id=" + requestContext.getRequestId());
            Trace.setStartCodeBlock("JSPRequest(cachedprops)", command + ";" + requestContext.getProperty(command));
            if (requestContext.getPropertyList("pagedata") != null && requestContext.getPropertyList("pagedata").getProperty("propertytreeid").equalsIgnoreCase("MaintenanceForm")) {
                HttpUtil.expireResponse(response);
            }
            this.includeRequest(request, response, requestContext, requestContext.getProperty("__nextpage"));
            Trace.setEndCodeBlock("JSPRequest(cachedprops)", command + ";" + requestContext.getProperty(command));
        } else {
            String commandParam = requestContext.getProperty(command);
            if (commandParam != null && commandParam.length() > 0) {
                Trace.setStartCodeBlock("JSPRequest(nocache)", command + ";" + commandParam);
                String connectionid = requestContext.getConnectionId();
                if (new CheckConnectionProcessor().isConnectionValid(connectionid)) {
                    requestContext.setProperty(command, commandParam);
                    RequestProcessor requestProcessor = new RequestProcessor(requestContext.getConnectionId());
                    try {
                        String nextPage;
                        requestContext.setProperty("modernlayout", "Y");
                        if (request.getParameter("_nav") != null && request.getParameter("_nav").toString().equalsIgnoreCase("Y") && !"Y".equals(request.getParameter("_skipstate"))) {
                            String id;
                            String string = command.equalsIgnoreCase("page") ? requestContext.getProperty("page") : (id = command.equalsIgnoreCase("gizmo") ? requestContext.getProperty("gizmo") : "");
                            if (requestContext.getProperty("_wpsid").length() == 0) {
                                String stateid = requestProcessor.logPageState(id, request);
                                requestContext.setProperty("_wpsid", stateid);
                            }
                        }
                        PropertyList requestProps = requestContext.getPropertyList();
                        requestProps.putAll(requestProcessor.getConnectionProperties());
                        if (command.equalsIgnoreCase("file")) {
                            PropertyList securityPolicy;
                            String filterfilecommand;
                            boolean isVirtualUser;
                            requestContext.setProperty("__nextpage", commandParam);
                            nextPage = commandParam;
                            boolean isTestPage = false;
                            SapphireConnection sapphireConnection = new ConnectionProcessor(connectionid).getSapphireConnection();
                            boolean bl = isVirtualUser = "I".equals(sapphireConnection.getUserType()) || "V".equals(sapphireConnection.getUserType());
                            if (nextPage.toLowerCase().contains("web-inf") || nextPage.toLowerCase().contains("meta-inf") || nextPage.toLowerCase().endsWith("-orig")) {
                                throw new ServletException(MESSAGE_ACCESS_DENIED);
                            }
                            if (nextPage.toLowerCase().contains("testpages")) {
                                boolean enableTestPages = false;
                                try {
                                    enableTestPages = ConfigService.getConfigPropertyBoolean("com.labvantage.sapphire.server.enabletestpages", false);
                                }
                                catch (ServiceException serviceException) {
                                    // empty catch block
                                }
                                if (!enableTestPages) {
                                    throw new ServletException(MESSAGE_ACCESS_DENIED);
                                }
                                isTestPage = true;
                            }
                            if (!isTestPage && !isVirtualUser && "N".equals(filterfilecommand = (securityPolicy = new ConfigurationProcessor(connectionid).getPolicy("SecurityPolicy", "Sapphire Custom")).getPropertyListNotNull("ajax").getProperty("allowunregisteredfilecommand", "N"))) {
                                try {
                                    requestProcessor.processFileCommand(nextPage);
                                }
                                catch (Exception e) {
                                    String error = e.getMessage();
                                    if (error.contains("com.labvantage.sapphire.ejb.ManagerException:")) {
                                        error = error.substring(error.indexOf(":") + 1).trim();
                                    }
                                    Trace.logWarn("File Access Denied Error [" + nextPage + "] " + error);
                                    throw new ServletException("Access to the requested resource has been denied: " + new TranslationProcessor(connectionid).translate("See log for details") + "<hr>");
                                }
                            }
                        } else if (command.equalsIgnoreCase("page")) {
                            requestContext.setProperty("currentlayout", (String)request.getSession().getAttribute("currentlayout"));
                            requestContext.setProperty("currentlayoutnode", (String)request.getSession().getAttribute("currentlayoutnode"));
                            requestContext.setProperty("currentlayoutreplace", (String)request.getSession().getAttribute("currentlayoutreplace"));
                            PropertyList pageProps = requestProcessor.getWebPageProperties(commandParam, requestContext);
                            if (XSS.isMock()) {
                                XSS.setThreadPageURL(commandParam);
                            }
                            PropertyList elements = new PropertyList();
                            for (String propertyid : pageProps.keySet()) {
                                if (pageProps.isSimple(propertyid) && requestProps.getProperty(propertyid).length() == 0) {
                                    requestProps.setProperty(propertyid, pageProps.getProperty(propertyid));
                                    continue;
                                }
                                if (pageProps.isPropertyList(propertyid)) {
                                    PropertyList propertyList = pageProps.getPropertyList(propertyid);
                                    requestProps.setProperty(propertyid, propertyList);
                                    if (propertyList.getProperty("elementid").length() <= 0 || !propertyList.getProperty("propertytreetype").equals("Element")) continue;
                                    elements.setProperty(propertyList.getProperty("elementid"), propertyList);
                                    continue;
                                }
                                if (!pageProps.isCollection(propertyid)) continue;
                                requestProps.setProperty(propertyid, pageProps.getCollection(propertyid));
                            }
                            requestContext.setProperty("elements", elements);
                            nextPage = pageProps.getProperty("__filename");
                            requestContext.setProperty("__nextpage", nextPage);
                            requestContext.setProperty("__registeredpage", "true");
                            sapphire.util.HttpUtil.setRequestVariables(request, "requestdata", requestContext.getPropertyList());
                        } else if (command.equalsIgnoreCase("gizmo")) {
                            commandParam = "WEB-CORE/modules/dashboard/gizmodefhost.jsp";
                            command = "file";
                            nextPage = commandParam;
                            requestContext.setProperty("__nextpage", nextPage);
                            requestContext.setProperty("__registeredpage", "true");
                        } else if (command.equalsIgnoreCase("bulletin")) {
                            PropertyList bulletinProps = requestProcessor.getBulletinProperties("ViewBulletin", commandParam, requestContext);
                            requestContext.getPropertyList().putAll(bulletinProps);
                            nextPage = bulletinProps.getProperty("__filename");
                            requestContext.setProperty("__nextpage", nextPage);
                            requestContext.setProperty("__registeredpage", "true");
                        } else if (command.equalsIgnoreCase("favorite")) {
                            PropertyList favoriteProps = requestProcessor.getFavoriteProperties(commandParam, requestContext);
                            String url = favoriteProps.getProperty("__url");
                            if (StringUtil.getLen(url) > 0L) {
                                requestContext.setProperty("__nextpage", url);
                                requestContext.setProperty("__isurl", "true");
                                requestContext.getPropertyList().putAll(favoriteProps);
                            } else {
                                requestContext.setProperty("page", favoriteProps.getProperty("__webpageid"));
                                requestContext.setProperty("__nextpage", favoriteProps.getProperty("__filename"));
                                requestContext.setProperty("__registeredpage", "true");
                                requestContext.setProperty("__isurl", "false");
                                requestContext.getPropertyList().putAll(favoriteProps);
                            }
                            nextPage = requestContext.getProperty("__nextpage");
                        } else {
                            throw new CommandException("Invalid command", command);
                        }
                        requestProcessor.addPropertyData(requestContext);
                        requestContext.setProperty(command, commandParam);
                        if (requestContext.getPropertyList("pagedata") != null && requestContext.getPropertyList("pagedata").getProperty("propertytreeid").equalsIgnoreCase("MaintenanceForm")) {
                            HttpUtil.expireResponse(response);
                        }
                        this.includeRequest(request, response, requestContext, nextPage);
                    }
                    catch (SapphireException e) {
                        throw new ServletException("Failed process " + command + " with parameter " + commandParam + ". Reason: " + e.getMessage(), (Throwable)e);
                    }
                }
                String mode = "currenturl";
                try {
                    mode = ConfigService.getConfigProperty("com.labvantage.sapphire.server.timeoutredirectmode", "requesturl");
                }
                catch (ServiceException serviceException) {
                    // empty catch block
                }
                if (mode.equals("requesturl")) {
                    throw new LoginException(request.getQueryString(), sapphire.util.HttpUtil.getRequestMap((ServletRequest)request), "Connection timed out or invalid. All prior locks have been cleared. Please login again.", true);
                }
                throw new LoginException(request.getQueryString(), "Connection timed out or invalid. All prior locks have been cleared. Please login again.");
                Trace.setEndCodeBlock("JSPRequest(nocache)", command + ";" + commandParam);
            } else {
                throw new CommandException("You need to provide a " + command + " parameter", command);
            }
        }
    }

    private void includeRequest(HttpServletRequest request, HttpServletResponse response, RequestContext requestContext, String jspPage) throws ServletException {
        if (requestContext.copyRequestParameters()) {
            PropertyList pageData = requestContext.getPropertyList().getPropertyList("pagedata");
            if (pageData == null) {
                pageData = new PropertyList();
                requestContext.getPropertyList().setProperty("pagedata", pageData);
            }
            Enumeration e = request.getParameterNames();
            while (e.hasMoreElements()) {
                String propertyid = (String)e.nextElement();
                pageData.setProperty(propertyid, request.getParameter(propertyid));
            }
        }
        try {
            request.setAttribute("gwt.codesvr", (Object)"127.0.0.1:9997");
            Trace.logInfo("JSPREQUEST", "Including file: " + jspPage);
            Trace.logDebug("JSPREQUEST", "Properties: " + requestContext.getPropertyList().toString());
            if ("n".equalsIgnoreCase(request.getParameter("html"))) {
                request.getRequestDispatcher(jspPage).forward((ServletRequest)request, (ServletResponse)response);
            } else {
                request.getRequestDispatcher(jspPage).include((ServletRequest)request, (ServletResponse)response);
            }
        }
        catch (Exception e) {
            Trace.logError("JSPREQUEST", "Failed to include file: " + jspPage, e, new LogContext(requestContext.getConnectionId()));
            throw new ServletException((Throwable)e);
        }
    }
}

