/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.JspTagException
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.XSS;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.BaseBodyTagSupport;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class JavaScriptAPITag
extends BaseBodyTagSupport {
    private boolean basic = false;
    private boolean notify = false;
    private static boolean isModernLayout = false;
    public static final String JSORIGEX = "js-orig";

    public void setBasic(String basic) {
        this.basic = basic.equalsIgnoreCase("true") || basic.equalsIgnoreCase("y");
    }

    public void setNotify(String notify) {
        this.notify = notify.equalsIgnoreCase("true") || notify.equalsIgnoreCase("y");
    }

    public int doStartTag() throws JspTagException {
        this.doInit();
        if (this.requestContext != null) {
            isModernLayout = "Y".equals(this.requestContext.getProperty("modernlayout"));
        }
        return 1;
    }

    @Override
    public int doEndTag() throws JspTagException {
        try {
            RequestContext rc = this.requestContext;
            StringBuffer html = new StringBuffer();
            if (this.basic) {
                html.append(HttpUtil.getSapphireCoreJSHTML(null, null, null));
            } else {
                html.append(HttpUtil.getSapphireCoreJSHTML(this.pageContext, rc, null));
            }
            if (html.length() > 0) {
                html.append("<script>");
                html.append("sapphire._startPings(").append(this.notify).append(");");
                html.append("var csrftoken ='").append(SafeHTML.encodeForJavaScript((String)this.pageContext.getSession().getAttribute("csrftoken"))).append("';");
                html.append("</script>");
                this.pageContext.getOut().print(html.toString());
            }
        }
        catch (Exception e) {
            this.logError("Could not render JS API.", e);
        }
        this.basic = false;
        this.notify = false;
        try {
            super.doEndTag();
        }
        catch (Exception e) {
            this.logError(e.getMessage());
        }
        return 6;
    }

    public static String getJQueryAPI(boolean ui, boolean mobile, PropertyListCollection plugins, boolean minimized) {
        return JavaScriptAPITag.getJQueryAPI(ui, mobile, plugins, "", minimized);
    }

    public static String getJQueryAPI(boolean ui, boolean mobile, PropertyListCollection plugins, String theme, boolean minimized) {
        return JavaScriptAPITag.getJQueryAPI(ui, mobile, plugins, theme, minimized, null);
    }

    private static void renderJQuery(HttpServletRequest request, StringBuffer html, ArrayList<String> scriptincludes, ArrayList<String> styleincludes) {
        PropertyList renderedJquery;
        PropertyList propertyList = renderedJquery = request != null && request.getAttribute("__renderedJquery") != null ? (PropertyList)request.getAttribute("__renderedJquery") : new PropertyList();
        if (renderedJquery != null) {
            PropertyListCollection plugins;
            String src;
            boolean ui;
            RequestContext rc;
            SapphireConnection sc = null;
            if (request != null && (rc = RequestContext.getInstance(request)) != null && rc.getConnectionId() != null && rc.getConnectionId().length() > 0) {
                ConnectionProcessor cp = new ConnectionProcessor(rc.getConnectionId());
                sc = cp.getSapphireConnection();
            }
            boolean isDevMode = false;
            if (sc != null) {
                try {
                    isDevMode = "Y".equals(new com.labvantage.sapphire.admin.system.ConfigurationProcessor(sc.getConnectionId()).getSysConfigProperty("devmode"));
                }
                catch (Exception cp) {
                    // empty catch block
                }
            }
            boolean minimized = false;
            minimized = isDevMode ? false : (renderedJquery.getProperty("minimized", "").length() > 0 && renderedJquery.getProperty("minimized", "N").equalsIgnoreCase("Y") && sc != null && !sc.getUseFullIncludes() ? true : sc == null || !sc.getUseFullIncludes());
            boolean bl = ui = renderedJquery.getProperty("ui", "N").equalsIgnoreCase("Y") && renderedJquery.getProperty("renderedui", "N").equalsIgnoreCase("N");
            if (ui) {
                String src2;
                String theme = renderedJquery.getProperty("theme", "");
                if (theme == null || theme.length() == 0) {
                    src2 = "WEB-CORE/extscripts/jquery/jquery-ui" + (!minimized ? "" : ".min") + ".css";
                    if (styleincludes != null) {
                        styleincludes.add(src2);
                    }
                    if (html != null) {
                        html.append("<link href=\"").append(src2).append("\" rel=\"stylesheet\" type=\"text/css\"/>");
                    }
                } else {
                    src2 = "WEB-CORE/extscripts/jquery/themes/" + theme.toLowerCase() + "/jquery-ui" + (!minimized ? "" : ".min") + ".css";
                    if (styleincludes != null) {
                        styleincludes.add(src2);
                    }
                    if (html != null) {
                        html.append("<link href=\"").append(src2).append("\" rel=\"stylesheet\" type=\"text/css\"/>");
                    }
                }
            }
            if (renderedJquery.getProperty("rendered", "N").equalsIgnoreCase("N")) {
                src = "WEB-CORE/extscripts/jquery/jquery" + (!minimized ? "" : ".min") + ".js";
                if (scriptincludes != null) {
                    scriptincludes.add(src);
                }
                if (html != null) {
                    html.append("<script type=\"text/javascript\" src=\"").append(src).append("\"></script>");
                }
            }
            if (ui) {
                src = "WEB-CORE/extscripts/jquery/jquery-ui" + (!minimized ? "" : ".min") + ".js";
                if (scriptincludes != null) {
                    scriptincludes.add(src);
                }
                if (html != null) {
                    html.append("<script type=\"text/javascript\" src=\"").append(src).append("\"></script>");
                }
                renderedJquery.setProperty("renderedui", "Y");
            }
            if (renderedJquery.getProperty("mobile", "N").equalsIgnoreCase("Y") && renderedJquery.getProperty("renderedmobile", "N").equalsIgnoreCase("N")) {
                String src1 = "WEB-CORE/extscripts/jquery/jquery.mobile" + (!minimized ? "" : ".min") + ".css";
                if (styleincludes != null) {
                    styleincludes.add(src1);
                }
                if (html != null) {
                    html.append("<link href=\"").append(src1).append("\" rel=\"stylesheet\" type=\"text/css\"/>");
                }
                String src2 = "WEB-CORE/extscripts/jquery/jquery.mobile" + (!minimized ? "" : ".min") + ".js";
                if (scriptincludes != null) {
                    scriptincludes.add(src2);
                }
                if (html != null) {
                    html.append("<script type=\"text/javascript\" src=\"").append(src2).append("\"></script>");
                }
                renderedJquery.setProperty("renderedmobile", "Y");
            }
            if ((plugins = renderedJquery.getCollection("plugins")) != null && plugins.size() > 0) {
                for (int i = 0; i < plugins.size(); ++i) {
                    boolean allowmin;
                    PropertyList plugin = plugins.getPropertyList(i);
                    if (!plugin.getProperty("rendered", "N").equalsIgnoreCase("N")) continue;
                    String id = plugin.getProperty("pluginid", plugin.getId());
                    boolean bl2 = allowmin = minimized && plugin.getProperty("allowminimized", "N").equalsIgnoreCase("Y");
                    if (plugin.getProperty("css", "N").equalsIgnoreCase("Y")) {
                        String src1 = "WEB-CORE/extscripts/jquery/plugins/jquery." + id + (!allowmin ? "" : ".min") + ".css";
                        if (styleincludes != null) {
                            styleincludes.add(src1);
                        }
                        if (html != null) {
                            html.append("<link href=\"").append(src1).append("\" rel=\"stylesheet\" type=\"text/css\"/>");
                        }
                    }
                    plugin.setProperty("rendered", "Y");
                    String src2 = "WEB-CORE/extscripts/jquery/plugins/jquery." + id + (!allowmin ? "" : ".min") + ".js";
                    if (scriptincludes != null) {
                        scriptincludes.add(src2);
                    }
                    if (html == null) continue;
                    html.append("<script type=\"text/javascript\" src=\"").append(src2).append("\"></script>");
                }
            }
            String src3 = "WEB-CORE/extscripts/jquery/jquery.labvantage" + (!minimized ? "" : ".min") + ".css";
            if (styleincludes != null) {
                styleincludes.add(src3);
            }
            if (html != null) {
                html.append("<link href=\"").append(src3).append("\" rel=\"stylesheet\" type=\"text/css\"/>");
            }
            renderedJquery.setProperty("rendered", "Y");
            if (request != null) {
                request.setAttribute("__renderedJquery", (Object)renderedJquery);
            }
        }
    }

    public static String getJQueryAPI(boolean ui, boolean mobile, PropertyListCollection plugins, PageContext pageContext) {
        boolean devMode = false;
        if (pageContext != null) {
            com.labvantage.sapphire.admin.system.ConfigurationProcessor cp = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(pageContext);
            try {
                devMode = cp.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                devMode = false;
            }
        }
        return JavaScriptAPITag.getJQueryAPI(ui, mobile, plugins, "", !devMode, pageContext);
    }

    public static void getJQueryIncludes(boolean ui, boolean mobile, PropertyListCollection plugins, String theme, boolean minimized, ArrayList<String> scriptincludes, ArrayList<String> styleincludes, HttpServletRequest request) {
        JavaScriptAPITag.getJQueryAPI(ui, mobile, plugins, theme, minimized, scriptincludes, styleincludes, request);
    }

    public static String getJQueryAPI(boolean ui, boolean mobile, PropertyListCollection plugins, String theme, boolean minimized, PageContext pageContext) {
        return JavaScriptAPITag.getJQueryAPI(ui, mobile, plugins, theme, minimized, null, null, pageContext != null ? (HttpServletRequest)pageContext.getRequest() : null);
    }

    public static String getJQueryAPI(boolean ui, boolean mobile, PropertyListCollection plugins, String theme, boolean minimized, ArrayList<String> scriptincludes, ArrayList<String> styleincludes, HttpServletRequest request) {
        PropertyListCollection pluginsColl;
        PropertyList renderedJquery;
        StringBuffer html = new StringBuffer();
        PropertyList propertyList = renderedJquery = request != null && request.getAttribute("__renderedJquery") != null ? (PropertyList)request.getAttribute("__renderedJquery") : null;
        if (renderedJquery == null) {
            renderedJquery = new PropertyList();
        }
        if (renderedJquery.getProperty("ui", "N").equalsIgnoreCase("N") && ui) {
            renderedJquery.setProperty("ui", "Y");
        }
        if (renderedJquery.getProperty("mobile", "N").equalsIgnoreCase("N") && mobile) {
            renderedJquery.setProperty("mobile", "Y");
        }
        if (renderedJquery.getProperty("theme", "").length() == 0 && theme != null && theme.length() > 0) {
            renderedJquery.setProperty("theme", theme);
        }
        if (renderedJquery.getProperty("minimized", "N").equalsIgnoreCase("N") && minimized) {
            renderedJquery.setProperty("minimized", "Y");
        }
        if ((pluginsColl = renderedJquery.getCollection("plugins")) == null) {
            pluginsColl = new PropertyListCollection();
            PropertyList plugin = new PropertyList();
            plugin.setProperty("pluginid", "sapphire");
            plugin.setProperty("css", "N");
            plugin.setProperty("allowminimized", "Y");
            pluginsColl.add(plugin);
            renderedJquery.setProperty("plugins", pluginsColl);
        }
        if (plugins != null) {
            for (int i = 0; i < plugins.size(); ++i) {
                PropertyList plugin = plugins.getPropertyList(i);
                PropertyList find = pluginsColl.find("pluginid", plugin.getProperty("pluginid"));
                if (find != null) continue;
                pluginsColl.add(plugin);
            }
        }
        if (request != null) {
            request.setAttribute("__renderedJquery", (Object)renderedJquery);
        }
        if (request != null) {
            boolean jsapirendered;
            boolean bl = jsapirendered = request != null && request.getAttribute("jsapiprocessedflag") != null && request.getAttribute("jsapiprocessedflag").toString().equalsIgnoreCase("Y");
            if (jsapirendered && renderedJquery.getProperty("rendered", "N").equalsIgnoreCase("N")) {
                JavaScriptAPITag.renderJQuery(request, html, scriptincludes, styleincludes);
            } else if (renderedJquery.getProperty("rendered", "N").equalsIgnoreCase("Y")) {
                JavaScriptAPITag.renderJQuery(request, html, scriptincludes, styleincludes);
            }
        } else {
            JavaScriptAPITag.renderJQuery(null, html, scriptincludes, styleincludes);
        }
        return html.toString();
    }

    private static StringBuffer renderConnectionJS(ServletContext servletContext, RequestContext requestContext, ConnectionInfo conInfo, com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor) {
        StringBuffer newscript = new StringBuffer();
        if (servletContext != null) {
            newscript.append("sapphire.connection.applicationRoot='';\n");
        }
        if (conInfo != null) {
            String home;
            com.labvantage.sapphire.admin.system.ConfigurationProcessor configuration = conInfo.getConnectionId().length() > 0 ? new com.labvantage.sapphire.admin.system.ConfigurationProcessor(conInfo.getConnectionId()) : null;
            try {
                home = configuration != null ? configuration.getProfileProperty(conInfo.getSysuserId(), "logonpageurl", configuration.getProfileProperty("(system)", "logonpageurl")) : "";
            }
            catch (Exception e) {
                home = "";
            }
            newscript.append("sapphire.connection.databaseId='").append(SafeHTML.encodeForJavaScript(conInfo.getDatabaseId())).append("';");
            newscript.append("sapphire.connection.sysUserId='").append(SafeHTML.encodeForJavaScript(conInfo.getSysuserId())).append("';");
            newscript.append("sapphire.connection.sysUserDesc='").append(SafeHTML.encodeForJavaScript(conInfo.getSysuserName())).append("';");
            newscript.append("sapphire.connection.sysUserRoleList='").append(SafeHTML.encodeForJavaScript(conInfo.getRoleList())).append("';");
            newscript.append("sapphire.connection.sysUserModuleList='").append(SafeHTML.encodeForJavaScript(conInfo.getModuleList())).append("';");
            newscript.append("sapphire.connection.sysUserDefaultDepartment='").append(SafeHTML.encodeForJavaScript(conInfo.getDefaultDepartment())).append("';");
            newscript.append("sapphire.connection.sysUserDepartmentList='").append(SafeHTML.encodeForJavaScript(conInfo.getDepartmentList())).append("';");
            newscript.append("sapphire.connection.jobTypeId='").append(conInfo.getCurrentJobtype() != null ? SafeHTML.encodeForJavaScript(conInfo.getCurrentJobtype()) : "").append("';");
            newscript.append("sapphire.connection.jobTypeList='").append(conInfo.getJobtypeList() != null ? SafeHTML.encodeForJavaScript(conInfo.getJobtypeList()) : "").append("';");
            newscript.append("sapphire.connection.rtl=").append(conInfo.isRtl() ? "true" : "false").append(";");
            newscript.append("sapphire.connection.home='").append(home).append("';");
            newscript.append("sapphire.connection.rtl=").append(conInfo.isRtl()).append(";");
            newscript.append("sapphire.connection.timezone='").append(JavaScriptAPITag.getTimezone(conInfo)).append("';");
            newscript.append("sapphire.connection.language='").append(conInfo.getLanguage()).append("';");
            FormatUtil fu = FormatUtil.getInstance(conInfo);
            if (fu != null) {
                newscript.append("sapphire.connection.decimalSeparator='").append(fu.getDecimalSeparator()).append("';");
                newscript.append("sapphire.connection.groupingSeparator='").append(StringUtil.replaceAll(String.valueOf(fu.getGroupingSeparator()), "'", "\\'")).append("';");
                newscript.append("sapphire.connection.groupingInterval=").append(fu.getGroupingInterval()).append(";");
            }
            newscript.append(JavaScriptAPITag.renderConfigurationJS(configurationProcessor));
        } else if (requestContext != null) {
            ConnectionProcessor connp;
            String conid = requestContext.getConnectionId();
            if (conid != null && conid.length() > 0 && (connp = new ConnectionProcessor()).checkConnection(conid)) {
                String home;
                com.labvantage.sapphire.admin.system.ConfigurationProcessor configuration = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(conid);
                try {
                    home = configuration != null ? configuration.getProfileProperty(conInfo.getSysuserId(), "logonpageurl", configuration.getProfileProperty("(system)", "logonpageurl")) : "";
                }
                catch (Exception e) {
                    home = "";
                }
                newscript.append("sapphire.connection.databaseId='").append(SafeHTML.encodeForJavaScript(StringUtil.split(conid, "|")[0])).append("';");
                newscript.append("sapphire.connection.sysUserId='").append(SafeHTML.encodeForJavaScript(requestContext.getProperty("sysuserid"))).append("';");
                newscript.append("sapphire.connection.sysUserDesc='").append(SafeHTML.encodeForJavaScript(requestContext.getProperty("sysuserdesc"))).append("';");
                newscript.append("sapphire.connection.sysUserRoleList='").append(SafeHTML.encodeForJavaScript(requestContext.getProperty("rolelist"))).append("';");
                newscript.append("sapphire.connection.sysUserModuleList='").append(SafeHTML.encodeForJavaScript(requestContext.getProperty("modulelist"))).append("';");
                newscript.append("sapphire.connection.sysUserDefaultDepartment='").append(SafeHTML.encodeForJavaScript(requestContext.getProperty("defaultdepartment"))).append("';");
                newscript.append("sapphire.connection.sysUserDepartmentList='").append(SafeHTML.encodeForJavaScript(requestContext.getProperty("departmentlist"))).append("';");
                newscript.append("sapphire.connection.rtl=").append(requestContext.getProperty("rtl").equals("Y") ? "true" : "false").append(";");
                newscript.append("sapphire.connection.home='").append(home).append("';");
                newscript.append("sapphire.connection.rtl=").append(conInfo != null ? conInfo.isRtl() : false).append(";");
                newscript.append("sapphire.connection.timezone='").append(JavaScriptAPITag.getTimezone(conInfo)).append("';");
                newscript.append("sapphire.connection.language='").append(conInfo.getLanguage()).append("';");
            }
            newscript.append(JavaScriptAPITag.renderConfigurationJS(configurationProcessor));
        }
        return newscript;
    }

    private static String getTimezone(ConnectionInfo connectionInfo) {
        String timezoneid = "";
        if (OpalUtil.isEmpty(connectionInfo.getTimeZone())) {
            TimeZone tz = I18nUtil.getConnectionTimeZone(connectionInfo);
            boolean isDayLightSaving = tz.inDaylightTime(new Date());
            timezoneid = tz.getDisplayName(isDayLightSaving, 0);
        } else {
            timezoneid = connectionInfo.getTimeZone();
        }
        return timezoneid;
    }

    private static StringBuffer renderConfigurationJS(com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor) {
        String timeToTimeout;
        String connectionTimeout;
        String pingConnectionFrqcy;
        String pingRsetFrqcy;
        StringBuffer html = new StringBuffer();
        if (configurationProcessor != null && configurationProcessor.getConnectionid() != null && configurationProcessor.getConnectionid().length() > 0) {
            try {
                pingRsetFrqcy = configurationProcessor.getSysConfigProperty("rsettimeout");
                pingConnectionFrqcy = configurationProcessor.getSysConfigProperty("timeoutpoll");
                connectionTimeout = configurationProcessor.getSysConfigProperty("connectiontimeout");
                timeToTimeout = configurationProcessor.getSysConfigProperty("connectiontimeout");
                if (pingRsetFrqcy == null || pingRsetFrqcy.length() == 0) {
                    pingRsetFrqcy = "60";
                }
                if (pingConnectionFrqcy == null || pingConnectionFrqcy.length() == 0) {
                    pingConnectionFrqcy = "60";
                }
                if (connectionTimeout == null || connectionTimeout.length() == 0) {
                    connectionTimeout = "3600";
                }
                if (timeToTimeout == null || timeToTimeout.length() == 0) {
                    timeToTimeout = "3600";
                }
                html.append("sapphire.jsdebug=").append(HttpUtil.jsDebugEnabled(configurationProcessor)).append(";");
                try {
                    if (configurationProcessor.getProfileProperty("(system)", "masterupdate", "N").equals("Y")) {
                        html.append("sapphire.connection.translateMasterUpdate=").append(true).append(";");
                        html.append("sapphire.events.attachEvent( window, 'onbeforeunload', sapphire.connection.saveToTransMasterTemp );");
                    }
                    if (XSS.isMock()) {
                        html.append("sapphire.events.attachEvent( window, 'onbeforeunload', registerWhitePage );");
                    }
                }
                catch (SapphireException sapphireException) {
                }
            }
            catch (Exception e) {
                pingRsetFrqcy = "60";
                pingConnectionFrqcy = "60";
                connectionTimeout = "3600";
                timeToTimeout = "3600";
            }
        } else {
            pingRsetFrqcy = "60";
            pingConnectionFrqcy = "60";
            connectionTimeout = "3600";
            timeToTimeout = "3600";
        }
        html.append("sapphire.connection.pingRsetFrqcy=").append(pingRsetFrqcy).append(";");
        html.append("sapphire.connection.pingConnectionFrqcy=").append(pingConnectionFrqcy).append(";");
        html.append("sapphire.connection.connectionTimeout=").append(connectionTimeout).append(";");
        html.append("sapphire.connection.timeToTimeout=").append(timeToTimeout).append(";");
        return html;
    }

    private static StringBuffer renderBrowserHTML(Browser browser) {
        StringBuffer newscript = new StringBuffer();
        if (browser != null) {
            newscript.append("sapphire.browser.id='").append(browser.getId()).append("';");
            newscript.append("sapphire.browser.name='").append(browser.getName()).append("';");
            newscript.append("sapphire.browser.osId='").append(browser.getOSId()).append("';");
            newscript.append("sapphire.browser.compat='").append(browser.getCompatibleId()).append("';");
            newscript.append("sapphire.browser.compatMode=").append(browser.getCompatibilityMode()).append(";");
            newscript.append("sapphire.browser.compatVersion=").append(browser.getCompatibleVersion()).append(";");
            newscript.append("sapphire.browser.version=").append(browser.getVersion()).append(";");
            newscript.append("sapphire.browser.mozillaVersion=").append(browser.getMozilla()).append(";");
            newscript.append("sapphire.browser.webkitVersion=").append(browser.getWebkit()).append(";");
            newscript.append("sapphire.browser.html=").append(browser.getHTMLVersion()).append(";");
            newscript.append("sapphire.browser.css=").append(browser.getCSSVersion()).append(";");
            newscript.append("sapphire.browser.mozilla=").append(browser.isMozilla()).append(";");
            newscript.append("sapphire.browser.webkit=").append(browser.isWebkit()).append(";");
            newscript.append("sapphire.browser.chromium=").append(browser.isChromium()).append(";");
            switch (browser.getBrowser()) {
                case 0: {
                    newscript.append("sapphire.browser.ie=true;");
                    break;
                }
                case 1: {
                    newscript.append("sapphire.browser.chrome=true;");
                    break;
                }
                case 9: {
                    newscript.append("sapphire.browser.edge=true;");
                    break;
                }
                case 2: {
                    newscript.append("sapphire.browser.firefox=true;");
                    break;
                }
                case 3: {
                    newscript.append("sapphire.browser.opera=true;");
                    break;
                }
                case 4: {
                    newscript.append("sapphire.browser.safari=true;");
                    break;
                }
                case 6: {
                    newscript.append("sapphire.browser.android=true;");
                    break;
                }
                case 7: {
                    newscript.append("sapphire.browser.skyfire=true;");
                    break;
                }
                case 8: {
                    newscript.append("sapphire.browser.blackberry=true;");
                }
            }
            newscript.append("sapphire.browser.setupDevice(").append(browser.isMobile()).append(",").append(browser.isTablet()).append(",").append(browser.isPhone()).append(",'").append(browser.getGUIMode() != null ? browser.getGUIMode().getId() : "").append("');");
            newscript.append("sapphire.browser.supported=").append(browser.isSupported()).append(";");
            newscript.append("sapphire.browser.gc=").append(browser.requiresGarbageCollection()).append(";");
        }
        return newscript;
    }

    private static StringBuffer renderBasicAPI(boolean gwtPageEntry, boolean rtl, boolean useFullIncludes) {
        StringBuffer html = new StringBuffer();
        html.append(sapphire.util.HttpUtil.getEncryptionJS(useFullIncludes));
        html.append("<script type=\"text/javascript\" src=\"").append(HttpUtil.getScript("WEB-CORE/scripts/json.js", useFullIncludes)).append("\"></script>");
        html.append("<script type=\"text/javascript\" src=\"").append(HttpUtil.getScript("WEB-CORE/scripts/sapphirecore.js", useFullIncludes)).append("\"></script>");
        html.append("<script type=\"text/javascript\" src=\"").append(HttpUtil.getScript("WEB-CORE/scripts/sapphireui.js", useFullIncludes)).append("\"></script>");
        html.append("<script type=\"text/javascript\" src=\"").append(HttpUtil.getScript("WEB-CORE/scripts/sapphirelookup.js", useFullIncludes)).append("\"></script>");
        html.append("<script type=\"text/javascript\">");
        html.append("if(typeof(sapphire)=='undefined')var sapphire=new Sapphire();");
        html.append("if(typeof(sapphire.ui)=='undefined')sapphire.ui=new SapphireUI();");
        html.append("if(typeof(sapphire.lookup)=='undefined')sapphire.lookup=new SapphireLookup();");
        html.append("if(typeof(sapphire.page.list)=='undefined')sapphire.page.list = {};");
        html.append("if(typeof(sapphire.page.maint)=='undefined')sapphire.page.maint = {};");
        html.append("if(typeof(sapphire.page.request)=='undefined')sapphire.page.request = {};");
        html.append("</script>");
        if (gwtPageEntry) {
            html.append("<script type=\"text/javascript\" language=\"javascript\" src=\"WEB-CORE/gwt/pageentry/pageentry.nocache.js?lv8807\"></script>");
        }
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/stylesheets/labvantageapi.css", rtl, useFullIncludes) + "\" type=\"text/css\">");
        html.append("<link rel=\"shortcut icon\" href=\"WEB-CORE/images/lvs.ico\">");
        return html;
    }

    private static StringBuffer renderAdvancedAPI(PropertyList js, boolean gwtPageEntry, boolean rtl, boolean useFullIncludes) {
        String classname;
        String objectname;
        String packagename;
        PropertyList pl;
        int index;
        StringBuffer html = new StringBuffer(sapphire.util.HttpUtil.getEncryptionJS(useFullIncludes));
        StringBuffer script = new StringBuffer();
        PropertyListCollection coll = js.getCollection("requiredincludes");
        for (index = 0; index < coll.size(); ++index) {
            pl = coll.getPropertyList(index);
            html.append("<script type=\"text/javascript\" src=\"").append(HttpUtil.getScript(pl.getProperty("url", ""), useFullIncludes)).append("\"></script>");
        }
        coll = js.getCollection("coreobjects");
        for (index = 0; index < coll.size(); ++index) {
            pl = coll.getPropertyList(index);
            html.append("<script type=\"text/javascript\" src=\"").append(HttpUtil.getScript(pl.getProperty("url", ""), useFullIncludes)).append("\"></script>");
            packagename = pl.getProperty("package", "");
            objectname = packagename.length() > 0 ? packagename + "." + pl.getProperty("objectname", "") : pl.getProperty("objectname", "");
            classname = pl.getProperty("classname", "");
            script.append("if(typeof(").append(objectname).append(")=='undefined')");
            if (packagename.length() == 0) {
                script.append("var ");
            }
            script.append(objectname).append("=new ").append(classname).append("();");
        }
        coll = js.getCollection("customobjects");
        for (index = 0; index < coll.size(); ++index) {
            pl = coll.getPropertyList(index);
            html.append("<script type=\"text/javascript\" src=\"").append(pl.getProperty("url", "")).append("\"></script>");
            packagename = pl.getProperty("package", "");
            objectname = packagename.length() > 0 ? "sapphire.custom." + packagename + "." + pl.getProperty("objectname", "") : "sapphire.custom." + pl.getProperty("objectname", "");
            classname = pl.getProperty("classname", "");
            script.append("if(typeof(").append(objectname).append(")=='undefined')");
            script.append(objectname).append(" = new ").append(classname).append("();");
        }
        html.append("<script type=\"text/javascript\">");
        html.append(script);
        html.append("</script>");
        if (gwtPageEntry) {
            html.append("<script type=\"text/javascript\" language=\"javascript\" src=\"WEB-CORE/gwt/pageentry/pageentry.nocache.js?lv8807\"></script>");
        }
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/stylesheets/labvantageapi.css", rtl, useFullIncludes) + "\" type=\"text/css\">");
        html.append("<link rel=\"shortcut icon\" href=\"WEB-CORE/images/lvs.ico\">");
        return html;
    }

    public static boolean isElementRequestRendered(PropertyList pagedata, String elementId) {
        String jsrequest = pagedata.getProperty("jsrequest");
        if (jsrequest.equals("full")) {
            return true;
        }
        if (jsrequest.contains("element")) {
            return true;
        }
        if (jsrequest.contains("pagedata")) {
            Object[] excludes = JavaScriptAPITag.getExcludes(jsrequest);
            if (excludes == null || excludes.length == 0) {
                return true;
            }
            return Arrays.binarySearch(excludes, elementId) < 0;
        }
        return false;
    }

    private static String[] getExcludes(String jsreq) {
        Object[] excludes = null;
        int s = jsreq.indexOf("exclude=");
        if (s > -1) {
            String t = jsreq.substring(s + 8);
            if ((s = t.indexOf(";")) > -1) {
                t = t.substring(0, s);
            }
            excludes = StringUtil.split(t + "|bopassword|connectionid", "|");
            Arrays.sort(excludes);
        } else {
            excludes = new String[]{"bopassword", "connectionid"};
        }
        return excludes;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static StringBuffer renderJSRequestScript(PropertyList pageinfoPl, PropertyList pagedata, String pageDirectives) {
        StringBuffer html = new StringBuffer();
        String pageName = "";
        String pageEdition = "";
        String output = "sapphire.util.propertyList.create()";
        if (pagedata != null && pageinfoPl != null) {
            PropertyList rp = pageinfoPl;
            PropertyList use = new PropertyList();
            Iterator it = pageinfoPl.keySet().iterator();
            String jsreq = rp.getProperty("jsrequest", pagedata.getProperty("jsrequest", "partial")).toLowerCase();
            if (!jsreq.equalsIgnoreCase("exclude=all")) {
                if (!jsreq.equalsIgnoreCase("full")) {
                    boolean element = jsreq.startsWith("element") || jsreq.indexOf(";element") > -1;
                    boolean gizmo = jsreq.startsWith("gizmo") || jsreq.indexOf(";gizmo") > -1;
                    boolean layout = jsreq.startsWith("layout") || jsreq.indexOf(";layout") > -1;
                    boolean padata = jsreq.startsWith("pagedata") || jsreq.indexOf(";pagedata") > -1;
                    Object[] excludes = JavaScriptAPITag.getExcludes(jsreq);
                    while (it.hasNext()) {
                        PropertyList pageData2;
                        Object keyob = it.next();
                        if (keyob == null || !(keyob instanceof String)) continue;
                        String key = keyob.toString();
                        if (excludes != null && excludes.length != 0 && Arrays.binarySearch(excludes, key) >= 0) continue;
                        Object val = rp.get(key);
                        if (val instanceof String || val instanceof Integer) {
                            use.setProperty(key, val.toString());
                            continue;
                        }
                        if (!(val instanceof PropertyList)) continue;
                        if (((PropertyList)val).getProperty("propertytreetype").equalsIgnoreCase("element") && element) {
                            use.setProperty(key, (PropertyList)val);
                        }
                        if (((PropertyList)val).getProperty("propertytreetype").equalsIgnoreCase("gizmo") && gizmo) {
                            use.setProperty(key, (PropertyList)val);
                            continue;
                        }
                        if (((PropertyList)val).getProperty("propertytreetype").equalsIgnoreCase("layout") && layout) {
                            use.setProperty(key, (PropertyList)val);
                            continue;
                        }
                        if (!key.equals("pagedata")) continue;
                        if (padata) {
                            use.setProperty(key, (PropertyList)val);
                            continue;
                        }
                        use.setProperty("pagedata", new PropertyList());
                        PropertyList propertyList = pageData2 = (PropertyList)val;
                        synchronized (propertyList) {
                            Iterator it2 = pageData2.keySet().iterator();
                            while (it2.hasNext()) {
                                Object val2;
                                String key2 = it2.next().toString();
                                if (excludes != null && excludes.length != 0 && Arrays.binarySearch(excludes, key2) >= 0 || !((val2 = pageData2.get(key2)) instanceof String) && !(val2 instanceof Integer)) continue;
                                use.getPropertyList("pagedata").setProperty(key2, val2.toString());
                            }
                        }
                    }
                } else {
                    use = rp;
                }
            }
            output = "sapphire.util.propertyList.create(" + use.toJSONString() + ")";
            pageName = pagedata.getProperty("page", pageinfoPl.getProperty("page", ""));
            pageEdition = pagedata.getProperty("__productedition", pageinfoPl.getProperty("__productedition", ""));
        }
        if (pageDirectives == null || pageDirectives.trim().length() == 0 || pageinfoPl.getPropertyList("pagedirectives") == null) {
            pageDirectives = "{}";
        }
        html.append("if(typeof(sapphire.page)!='undefined'){");
        html.append("sapphire.page.html5=").append(!pageinfoPl.getProperty("html5", "Y").equalsIgnoreCase("N")).append(";");
        html.append("sapphire.page.name='").append(pageName).append("';");
        html.append("sapphire.page.edition='").append(SafeHTML.encodeForJavaScript(pageEdition)).append("';");
        html.append("sapphire.page.stateid='").append(SafeHTML.encodeForJavaScript(pageinfoPl.getProperty("_wpsid", ""))).append("';");
        html.append("if(typeof(sapphire.page.request)!='undefined'){");
        html.append("sapphire.page.request.data=").append(output).append(";");
        html.append("sapphire.page.request.data.pagedirectives=").append(pageDirectives).append(";");
        html.append("if(sapphire.page.request.data!=null&&typeof(sapphire.page.request.data.pagedata)!='undefined'){\n");
        html.append("sapphire.page.data=sapphire.page.request.data.pagedata;");
        html.append("}");
        html.append("else{");
        html.append("sapphire.page.data=null;\n");
        html.append("}");
        html.append("}");
        html.append("}");
        return html;
    }

    private static StringBuffer renderAnimationScript(RequestContext rc, ConnectionInfo conInfo, com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor) {
        String fadeInOutAnimations;
        String resizeInOutAnimations;
        String menuAnimations;
        StringBuffer html = new StringBuffer();
        String sysuserid = null;
        if (rc != null) {
            sysuserid = rc.getProperty("sysuserid");
        } else if (conInfo != null) {
            sysuserid = conInfo.getSysuserId();
        }
        if (configurationProcessor != null && sysuserid != null && sysuserid.length() > 0) {
            try {
                menuAnimations = configurationProcessor.getProfileProperty(sysuserid, "menuanimation", configurationProcessor.getProfileProperty("(system)", "menuanimation", "Y"));
            }
            catch (Exception e1) {
                menuAnimations = "Y";
            }
            try {
                resizeInOutAnimations = configurationProcessor.getProfileProperty(sysuserid, "resizeanimation", configurationProcessor.getProfileProperty("(system)", "resizeanimation", "Y"));
            }
            catch (Exception e1) {
                resizeInOutAnimations = "Y";
            }
            try {
                fadeInOutAnimations = configurationProcessor.getProfileProperty(sysuserid, "fadeanimation", configurationProcessor.getProfileProperty("(system)", "fadeanimation", "Y"));
            }
            catch (Exception e1) {
                fadeInOutAnimations = "Y";
            }
        } else {
            fadeInOutAnimations = "Y";
            resizeInOutAnimations = "Y";
            menuAnimations = "Y";
        }
        html.append("if ( typeof(sapphire.ui) != 'undefined' ){");
        if (menuAnimations.equalsIgnoreCase("Y")) {
            html.append("sapphire.ui.animation.menuEnabled=true;");
        } else {
            html.append("sapphire.ui.animation.menuEnabled=false;");
        }
        if (resizeInOutAnimations.equalsIgnoreCase("Y")) {
            html.append("sapphire.ui.animation.resizeInOutEnabled=true;");
        } else {
            html.append("sapphire.ui.animation.resizeInOutEnabled=false;");
        }
        if (fadeInOutAnimations.equalsIgnoreCase("Y")) {
            html.append("sapphire.ui.animation.fadeInOutEnabled=true;");
        } else {
            html.append("sapphire.ui.animation.fadeInOutEnabled=false;");
        }
        html.append("}");
        return html;
    }

    public static String getJavaScriptAPI(PageContext pageContext, RequestContext requestContext, ConnectionInfo conInfo, boolean checkLayout) {
        if (!checkLayout || JavaScriptAPITag.checkAPI(requestContext)) {
            return JavaScriptAPITag.getJavaScriptAPI(pageContext, requestContext, conInfo);
        }
        return "";
    }

    public static boolean checkAPI(RequestContext requestContext) {
        if (requestContext != null) {
            PropertyList layout = requestContext.getPropertyList("layout");
            return layout == null || !layout.getProperty("objectname", "").contains("WEB-CORE") && !layout.getProperty("objectname", "").contains("WEB-OPAL") && layout.getProperty("rendersjavascriptapi", "n").equalsIgnoreCase("n");
        }
        return true;
    }

    public static String getJavaScriptAPI(PageContext pageContext) {
        return JavaScriptAPITag.getJavaScriptAPI(pageContext, null, null);
    }

    public static String getJavaScriptAPI(PageContext pageContext, RequestContext requestContext, boolean rtl, boolean useFullIncludes) {
        return JavaScriptAPITag.getJavaScriptAPI(null, null, null, false, rtl, useFullIncludes);
    }

    public static String getJavaScriptAPI(PageContext pageContext, RequestContext requestContext, ConnectionInfo conInfo) {
        return JavaScriptAPITag.getJavaScriptAPI(pageContext, requestContext, conInfo, false, false, false);
    }

    private static String getJavaScriptAPI(PageContext pageContext, RequestContext requestContext, ConnectionInfo conInfo, boolean dummy, boolean rtl, boolean useFullIncludes) {
        Object jsapiprocessedflag = null;
        if (pageContext != null) {
            jsapiprocessedflag = pageContext.getRequest().getAttribute("jsapiprocessedflag");
        }
        if (jsapiprocessedflag == null || !jsapiprocessedflag.toString().equalsIgnoreCase("Y")) {
            String path;
            boolean $usefullincludes;
            ConnectionProcessor cp;
            ServletContext servletContext = null;
            HttpServletRequest request = null;
            if (pageContext != null) {
                servletContext = pageContext.getServletContext();
                if (requestContext == null) {
                    requestContext = RequestContext.getRequestContext(pageContext);
                }
                if (pageContext.getRequest() instanceof HttpServletRequest) {
                    request = (HttpServletRequest)pageContext.getRequest();
                }
                if (conInfo == null) {
                    try {
                        conInfo = (ConnectionInfo)sapphire.util.HttpUtil.getConnectionInfo(pageContext);
                    }
                    catch (Exception e) {
                        conInfo = null;
                    }
                }
            } else if (requestContext != null && conInfo == null && (cp = new ConnectionProcessor(requestContext.getConnectionId())) != null && cp.getConnectionid() != null && cp.getConnectionid().length() > 0) {
                conInfo = cp.getConnectionInfo(requestContext.getConnectionId());
            }
            boolean bl = $usefullincludes = conInfo == null ? useFullIncludes : conInfo.getUseFullIncludes();
            if ($usefullincludes && !Files.exists(Paths.get(path = pageContext.getServletContext().getRealPath("/WEB-CORE/scripts/sapphirecore.js-orig"), new String[0]), new LinkOption[0])) {
                Trace.logWarn("Use unminified clientside sources set in policy, but cannot find original files. Reverting to using standard sources which may already be expanded.");
                $usefullincludes = false;
            }
            return JavaScriptAPITag.renderJavaScriptAPI(servletContext, request, requestContext, conInfo, conInfo == null ? rtl : conInfo.isRtl(), $usefullincludes);
        }
        return "";
    }

    public static String getJavaScriptAPI(ServletContext servletContext, HttpServletRequest request, RequestContext requestContext, ConnectionInfo conInfo) {
        Object jsapiprocessedflag = null;
        if (request != null) {
            jsapiprocessedflag = request.getAttribute("jsapiprocessedflag");
        }
        if (jsapiprocessedflag == null || !jsapiprocessedflag.toString().equalsIgnoreCase("Y")) {
            ConnectionProcessor cp;
            if (request != null && requestContext == null) {
                requestContext = RequestContext.getRequestContext(request);
            }
            if (requestContext != null && conInfo == null && (cp = new ConnectionProcessor(requestContext.getConnectionId())) != null && cp.getConnectionid() != null && cp.getConnectionid().length() > 0) {
                conInfo = cp.getConnectionInfo(requestContext.getConnectionId());
            }
            return JavaScriptAPITag.renderJavaScriptAPI(servletContext, request, requestContext, conInfo, conInfo == null ? false : conInfo.isRtl(), conInfo == null ? false : conInfo.isRtl());
        }
        return "";
    }

    public static String getSDCScript(PropertyList props, boolean renderTags) {
        StringBuffer out = new StringBuffer();
        if (props != null) {
            if (renderTags) {
                out.append("<script type=\"text/javascript\">");
            }
            if (props.containsKey("links")) {
                props.remove("links");
            }
            if (props.containsKey("attributes")) {
                props.remove("attributes");
            }
            if (props.containsKey("detaillinks")) {
                props.remove("detaillinks");
            }
            if (props.containsKey("reverselinks")) {
                props.remove("reverselinks");
            }
            if (props.containsKey("reversedetaillinks")) {
                props.remove("reversedetaillinks");
            }
            if (props.containsKey("tables")) {
                props.remove("tables");
            }
            out.append("if (typeof(sapphire.sdc)!='undefined'){");
            out.append("sapphire.sdc.setProperties(").append(props.toJSONString(false)).append(");");
            out.append("}");
            if (renderTags) {
                out.append("</script>");
            }
        }
        return out.toString();
    }

    public static String getDocumentMode(Browser browser) {
        if (browser.isIE()) {
            if (browser.getVersion() > 8.0) {
                return "";
            }
            if (browser.getVersion() > 7.0) {
                return "";
            }
            return "";
        }
        return "";
    }

    public static void setProcessed(HttpServletRequest request, boolean processed) {
        if (request != null) {
            request.setAttribute("jsapiprocessedflag", (Object)(processed ? "Y" : "N"));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String renderJavaScriptAPI(ServletContext servletContext, HttpServletRequest request, RequestContext requestContext, ConnectionInfo conInfo, boolean rtl, boolean useFullIncludes) {
        PropertyList pageinfo;
        StringBuffer newscript;
        StringBuffer html;
        boolean gwtPageEntry = requestContext == null || !requestContext.getProperty("gwtpageentry").equals("N");
        Object htmlob = null;
        if (servletContext != null && gwtPageEntry) {
            htmlob = servletContext.getAttribute("jsobjects_html" + (useFullIncludes ? "_unmin" : ""));
        }
        Browser browser = null;
        if (request != null) {
            browser = new Browser(request);
        }
        String weinre_url = "";
        try {
            weinre_url = ConfigService.getConfigProperty("com.labvantage.sapphire.server.weinredebug", "");
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (htmlob != null && htmlob instanceof StringBuffer) {
            html = (StringBuffer)htmlob;
        } else {
            html = new StringBuffer();
            if (weinre_url.length() > 0) {
                html.append("<script src=\"").append(weinre_url).append("\"></script>");
            }
            if (browser != null && browser.isIE()) {
                html.append(HttpUtil.getAnimationHTML());
            }
            try {
                PropertyList js = null;
                Object jsob = null;
                if (servletContext != null) {
                    jsob = servletContext.getAttribute("jsobjects");
                }
                if (jsob != null && jsob instanceof PropertyList) {
                    js = (PropertyList)jsob;
                } else {
                    URL url = null;
                    if (servletContext != null) {
                        url = servletContext.getResource("/WEB-CORE/scripts/jsobjects.xml");
                    }
                    if (url != null) {
                        StringBuffer finalBuffer = new StringBuffer();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));){
                            String str;
                            while ((str = reader.readLine()) != null) {
                                finalBuffer.append(str);
                            }
                        }
                        js = new PropertyList();
                        js.setPropertyList(finalBuffer.toString());
                        servletContext.setAttribute("jsobjects", (Object)js);
                    }
                }
                if (js != null && js.containsKey("requiredincludes")) {
                    html.append(JavaScriptAPITag.renderAdvancedAPI(js, gwtPageEntry, rtl, useFullIncludes));
                    if (servletContext != null) {
                        servletContext.setAttribute("jsobjects_html" + (useFullIncludes ? "_unmin" : ""), (Object)html);
                    }
                } else {
                    html.append(JavaScriptAPITag.renderBasicAPI(gwtPageEntry, rtl, useFullIncludes));
                }
            }
            catch (Exception e) {
                Logger.logWarn("Could not render advanced JS API.");
                html.append(JavaScriptAPITag.renderBasicAPI(gwtPageEntry, rtl, useFullIncludes));
            }
        }
        PropertyList securityPolicy = null;
        ConfigurationProcessor cp = null;
        com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor = null;
        String connectionId = "";
        if (requestContext != null) {
            connectionId = requestContext.getConnectionId();
        } else if (conInfo != null) {
            connectionId = conInfo.getConnectionId();
        }
        if (connectionId != null && connectionId.length() > 0) {
            configurationProcessor = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(connectionId);
        }
        Object newscripob = null;
        if (request != null && connectionId.length() > 0) {
            newscripob = request.getSession().getAttribute("jsobjects_new");
        }
        boolean newJS = false;
        if (newscripob != null && newscripob instanceof String[] && ((String[])newscripob).length == 2 && ((String[])newscripob)[0].equalsIgnoreCase(connectionId)) {
            newscript = new StringBuffer(((String[])newscripob)[1]);
        } else {
            Logger.logDebug("Connection script rendered using new session variable.");
            newJS = true;
            newscript = new StringBuffer();
            newscript.append("<script type=\"text/javascript\">");
            newscript.append("if(typeof(sapphire)!='undefined'){");
            newscript.append(JavaScriptAPITag.renderBrowserHTML(browser));
            newscript.append(JavaScriptAPITag.renderConnectionJS(servletContext, requestContext, conInfo, configurationProcessor));
            newscript.append(JavaScriptAPITag.renderAnimationScript(requestContext, conInfo, configurationProcessor));
            boolean showPageNames = false;
            if (connectionId != null && connectionId.length() > 0) {
                try {
                    if (cp == null) {
                        cp = new ConfigurationProcessor(connectionId);
                    }
                    if (securityPolicy == null && conInfo != null && conInfo.getDatabaseId() != null && conInfo.getDatabaseId().length() > 0) {
                        try {
                            securityPolicy = cp.getPolicy("SecurityPolicy", conInfo.getUserType().equalsIgnoreCase("I") || conInfo.getUserType().equalsIgnoreCase("V") ? "Virtual Custom" : "Sapphire Custom");
                        }
                        catch (Exception e) {
                            securityPolicy = null;
                        }
                    }
                    showPageNames = securityPolicy != null ? Boolean.valueOf(securityPolicy.getPropertyListNotNull("sessionmanagement").getProperty("showpagename", "N").equalsIgnoreCase("Y")) : null;
                }
                catch (Exception e) {
                    // empty catch block
                }
            }
            if (showPageNames) {
                newscript.append("sapphire.util.url.defaultExcludes['page']=true;");
            }
            newscript.append("}");
            newscript.append("</script>");
            if (requestContext != null && requestContext.getPropertyList().getProperty("language").length() > 0) {
                newscript.append("<script type=\"text/javascript\" src=\"").append("rc?command=ajax&ajaxclass=com.labvantage.sapphire.ajax.operations.ClientTranslation&language=").append(requestContext.getPropertyList().getProperty("language")).append("\"></script>");
            }
            if (request != null && connectionId.length() > 0) {
                request.getSession().setAttribute("jsobjects_new", (Object)new String[]{connectionId, newscript.toString()});
            }
        }
        StringBuffer finalbuf = new StringBuffer();
        finalbuf.append(html);
        finalbuf.append(newscript);
        finalbuf.append("<script type=\"text/javascript\">");
        boolean isDevMode = false;
        if (configurationProcessor != null) {
            try {
                String devMode = configurationProcessor.getSysConfigProperty("devmode", "N");
                isDevMode = devMode.equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                isDevMode = false;
            }
        }
        String compCode = "";
        if (conInfo != null && conInfo.getDatabaseId() != null && conInfo.getDatabaseId().length() > 0) {
            try {
                compCode = Configuration.getCompcode(conInfo.getDatabaseId());
            }
            catch (Exception e4) {
                compCode = "";
            }
        }
        boolean notifydebug = false;
        if (configurationProcessor != null) {
            try {
                notifydebug = "Y".equals(configurationProcessor.getSysConfigProperty("notificationdebug"));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        boolean allowPageDirectives = true;
        if (connectionId != null && connectionId.length() > 0 && !SecurityPolicyUtil.allowPageDirectives(connectionId, "ajax")) {
            allowPageDirectives = false;
        }
        finalbuf.append("sapphire.notification._debug=").append(notifydebug).append(";");
        finalbuf.append("sapphire.connection.isDevMode=").append(isDevMode).append(";");
        finalbuf.append("sapphire.connection.isCompDevMode=").append(compCode != null && compCode.length() > 0).append(";");
        finalbuf.append("sapphire.connection.componentCode='").append(compCode != null && compCode.length() > 0 ? compCode : "").append("';");
        finalbuf.append("sapphire.connection.allowPageDirectives=").append(allowPageDirectives).append(";");
        if (requestContext != null && requestContext.getPropertyList().containsKey("pagedata") && (pageinfo = requestContext.getPropertyList("pagedata")) != null) {
            if (pageinfo.containsKey("width")) {
                finalbuf.append("sapphire.lookup.width = '").append(pageinfo.getProperty("width")).append("';");
            }
            if (pageinfo.containsKey("height")) {
                finalbuf.append("sapphire.lookup.height = '").append(pageinfo.getProperty("height")).append("';");
            }
        }
        PropertyList pagedata = null;
        if (requestContext != null) {
            pagedata = requestContext.getPropertyList("pagedata");
        }
        String orgPageDirectives = null;
        if (request != null) {
            orgPageDirectives = request.getParameter("__pagedirectives");
        }
        if (pagedata != null || orgPageDirectives != null) {
            finalbuf.append(JavaScriptAPITag.renderJSRequestScript(requestContext.getPropertyList(), pagedata, orgPageDirectives));
        }
        if (weinre_url.length() > 0) {
            finalbuf.append("window.setTimeout('sapphire._setUpLogger()',1000);");
        }
        boolean showenviroment = false;
        if (securityPolicy == null && connectionId != null && connectionId.length() > 0) {
            try {
                if (cp == null) {
                    cp = new ConfigurationProcessor(connectionId);
                }
                securityPolicy = cp.getPolicy("SecurityPolicy", conInfo != null && (conInfo.getUserType().equalsIgnoreCase("I") || conInfo.getUserType().equalsIgnoreCase("V")) ? "Virtual Custom" : "Sapphire Custom");
                showenviroment = securityPolicy != null ? securityPolicy.getPropertyListNotNull("sessionmanagement").getProperty("showenviroment", "N").equalsIgnoreCase("Y") : false;
            }
            catch (Exception e) {
                securityPolicy = null;
            }
        }
        if (newJS && browser != null && showenviroment) {
            String cid;
            String string = connectionId != null ? connectionId : (conInfo != null ? conInfo.getConnectionId() : (cid = requestContext != null ? requestContext.getConnectionId() : ""));
            if (cid.length() > 0) {
                finalbuf.append("sapphire.logger.info('").append("LabVantage").append(" ").append(Build.getVersion()).append(" (").append(Build.getBuild()).append(" on ").append(Build.getBuildDate());
                if (Build.getPatch().length() > 0) {
                    finalbuf.append(" Patch ").append(Build.getPatch());
                }
                finalbuf.append(")');");
                finalbuf.append("sapphire.logger.info('");
                if (browser.isMobile()) {
                    finalbuf.append("Mobile Client: ");
                } else {
                    finalbuf.append("Desktop Client: ");
                }
                finalbuf.append("").append(browser.getName()).append(" ").append(browser.getVersion());
                if (browser.isIE() && browser.getCompatibilityMode()) {
                    finalbuf.append(" In Compatibility Mode");
                }
                if (browser.isSupported()) {
                    finalbuf.append(" (SUPPORTED)");
                } else {
                    finalbuf.append(" (UNSUPPORTED)");
                }
                finalbuf.append("');");
                finalbuf.append("sapphire.logger.info('Client OS: ").append(browser.getOSId()).append("');");
                try {
                    Configuration configuration = Configuration.getInstance();
                    finalbuf.append("sapphire.logger.info('Server: ").append(Configuration.getPlatformName(configuration.getPlatform())).append(" @ ").append(configuration.getHostid()).append(" (").append(configuration.getServerHostName()).append(")").append("');");
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        finalbuf.append("</script>");
        JavaScriptAPITag.renderJQuery(request, finalbuf, null, null);
        JavaScriptAPITag.setProcessed(request, true);
        return finalbuf.toString();
    }
}

