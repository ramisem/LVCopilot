/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.util.http;

import com.labvantage.opal.layouts.LayoutUtil;
import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.layout.GenericLayout;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.tagext.PageTagInfo;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.JstlUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class HttpUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 91348 $";
    static final String PROPERTY_ANIMATIONS = "animations";
    static final String PROPERTY_HOTKEYOBJECTS = "__hotkeyobjects";
    static final String CONST_TITLE = "LabVantage";
    static final String CONST_VERSIONPREFIX = "";

    public static void expireResponse(HttpServletResponse response) {
        try {
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "Sat, 26 Jul 1997 05:00:00 GMT");
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String shortCutInUse(String toUse, boolean useShift, boolean hotKey, RequestContext requestContext) {
        String out = toUse;
        PropertyListCollection shortcuts = requestContext.getPropertyList().getCollection(PROPERTY_HOTKEYOBJECTS);
        if (shortcuts != null) {
            for (int i = 0; i < shortcuts.size(); ++i) {
                try {
                    PropertyList shortcut = shortcuts.getPropertyList(i);
                    String shortcutchar = shortcut.getProperty("character");
                    boolean shift = shortcut.getProperty("shift", "N").equalsIgnoreCase("Y");
                    if (!toUse.equalsIgnoreCase(shortcutchar) || useShift != shift) continue;
                    if (hotKey && !shortcut.getProperty("hot", "N").equalsIgnoreCase("Y")) {
                        String old = HttpUtil.incShortCutInUse(shortcutchar, shift, requestContext);
                        if (old.length() > 0) {
                            shortcut.setProperty("character", old);
                        } else {
                            shortcuts.remove(i);
                            --i;
                        }
                        out = shortcutchar;
                        break;
                    }
                    out = CONST_VERSIONPREFIX;
                    break;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return out;
    }

    private static String incShortCutInUse(String characters, boolean shift, RequestContext requestContext) {
        int inc = 1;
        String character = characters + inc;
        while (HttpUtil.shortCutInUse(character, shift, false, requestContext).length() == 0) {
            character = characters + ++inc;
            if (inc <= 9) continue;
            character = CONST_VERSIONPREFIX;
            break;
        }
        return character;
    }

    public static String getShortcutKeyHTML(RequestContext requestContext) {
        PropertyListCollection shortcuts;
        StringBuffer html = new StringBuffer();
        if (requestContext != null && (shortcuts = requestContext.getPropertyList().getCollection(PROPERTY_HOTKEYOBJECTS)) != null && shortcuts.size() > 0) {
            html.append("<script type=\"text/javascript\">\n");
            PropertyList temp = new PropertyList();
            temp.setProperty("temp", shortcuts);
            JSONObject job = temp.toJSONObject(false);
            try {
                html.append("sapphire.keyboard.addKeys(").append(job.getJSONArray("temp").toString()).append(");\n");
                requestContext.getPropertyList().remove(PROPERTY_HOTKEYOBJECTS);
            }
            catch (Exception exception) {
                // empty catch block
            }
            html.append("</script>\n");
        }
        return html.toString();
    }

    public static String addShortcutKeyObject(String prefferedCharacters, String titleOrTextOfItem, String objectId, String frameName, boolean shift, boolean hotKey, RequestContext requestContext) {
        String character = CONST_VERSIONPREFIX;
        if (objectId.length() > 0 && requestContext != null) {
            String found;
            if (prefferedCharacters.length() > 0) {
                character = prefferedCharacters;
                if (character.startsWith("#") && character.length() > 1) {
                    String found2;
                    Pattern p = Pattern.compile("([0-9]*)");
                    Matcher m = p.matcher(character.substring(1));
                    character = m.matches() ? ((found2 = HttpUtil.shortCutInUse(character, shift, hotKey, requestContext)).length() == 0 ? CONST_VERSIONPREFIX : found2) : CONST_VERSIONPREFIX;
                } else {
                    found = HttpUtil.shortCutInUse(character = character.toLowerCase(), shift, hotKey, requestContext);
                    if (found.length() == 0) {
                        hotKey = false;
                        character = HttpUtil.incShortCutInUse(character, shift, requestContext);
                    } else {
                        character = found;
                    }
                }
            } else if (titleOrTextOfItem.length() > 0) {
                if ((titleOrTextOfItem = titleOrTextOfItem.toLowerCase().trim()).length() > 0) {
                    try {
                        character = titleOrTextOfItem.charAt(0) + CONST_VERSIONPREFIX;
                    }
                    catch (Exception e) {
                        Trace.logWarn("Failed to select character for hotkey (2)");
                        character = CONST_VERSIONPREFIX;
                    }
                    found = HttpUtil.shortCutInUse(character, shift, hotKey, requestContext);
                    if (found.length() == 0) {
                        hotKey = false;
                        if (titleOrTextOfItem.trim().indexOf(" ") > -1) {
                            String[] t = StringUtil.split(titleOrTextOfItem, " ");
                            if (t.length > 1 && t[0].length() > 0 && t[1].length() > 0) {
                                try {
                                    character = CONST_VERSIONPREFIX + t[0].charAt(0) + t[1].charAt(0);
                                }
                                catch (Exception e) {
                                    Trace.logWarn("Failed to select character for hotkey (3)");
                                    character = CONST_VERSIONPREFIX;
                                }
                            } else {
                                Trace.logWarn("No text to select character for hotkey (3)");
                                character = CONST_VERSIONPREFIX;
                            }
                        } else {
                            String string = character = titleOrTextOfItem.length() > 1 ? titleOrTextOfItem.substring(0, 2) : CONST_VERSIONPREFIX;
                        }
                        if (HttpUtil.shortCutInUse(character, shift, false, requestContext).length() == 0) {
                            character = HttpUtil.incShortCutInUse(character, shift, requestContext);
                        }
                    } else {
                        character = found;
                    }
                } else {
                    character = CONST_VERSIONPREFIX;
                }
            } else {
                character = HttpUtil.incShortCutInUse(CONST_VERSIONPREFIX, shift, requestContext);
            }
            if (character.length() > 0) {
                PropertyListCollection shortcuts = requestContext.getPropertyList().getCollection(PROPERTY_HOTKEYOBJECTS);
                if (shortcuts == null) {
                    shortcuts = new PropertyListCollection();
                    Collections.synchronizedCollection(shortcuts);
                    requestContext.getPropertyList().setProperty(PROPERTY_HOTKEYOBJECTS, shortcuts);
                }
                PropertyList shortcut = new PropertyList();
                shortcut.setProperty("character", character.toLowerCase());
                shortcut.setProperty("shift", shift ? "Y" : "N");
                shortcut.setProperty("hot", hotKey ? "Y" : "N");
                shortcut.setProperty("objectid", objectId);
                shortcut.setProperty("frame", frameName);
                shortcuts.add(shortcut);
            }
        }
        return character;
    }

    public static boolean jsDebugEnabled(PageContext pageContext) {
        com.labvantage.sapphire.admin.system.ConfigurationProcessor config = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(pageContext);
        return HttpUtil.jsDebugEnabled(config);
    }

    public static String getCSS(String css, PageContext pageContext) {
        if (pageContext == null) {
            return css;
        }
        com.labvantage.sapphire.services.ConnectionInfo connectionInfo = sapphire.util.HttpUtil.getConnectionInfo(pageContext);
        if (connectionInfo == null) {
            return css;
        }
        return HttpUtil.getCSS(css, connectionInfo.isRtl(), connectionInfo.getUseFullIncludes());
    }

    public static String getCSS(String css, HttpServletRequest request) {
        if (request == null) {
            return css;
        }
        RequestContext rc = RequestContext.getRequestContext(request);
        if (rc != null && rc.getConnectionId().length() > 0) {
            ConnectionProcessor cp = new ConnectionProcessor(rc.getConnectionId());
            SapphireConnection sc = cp.getSapphireConnection();
            return HttpUtil.getCSS(css, sc.isRtl(), sc.getUseFullIncludes());
        }
        return HttpUtil.getCSS(css, false, false);
    }

    public static String getCSS(String css, boolean isRtl, boolean useFullIncludes) {
        if (isRtl) {
            css = StringUtil.replaceAll(css, "\\", "/");
            css = StringUtil.replaceAll(css, ".css", ".rtl.css");
            if (useFullIncludes) {
                css = css.substring(0, css.length() - 3) + "orig.css";
            }
            return css;
        }
        if (useFullIncludes) {
            css = css.substring(0, css.length() - 3) + "orig.css";
        }
        return css;
    }

    public static String getScript(String script, PageContext pageContext) {
        if (pageContext == null) {
            return script;
        }
        com.labvantage.sapphire.services.ConnectionInfo connectionInfo = sapphire.util.HttpUtil.getConnectionInfo(pageContext);
        return HttpUtil.getScript(script, connectionInfo.getUseFullIncludes());
    }

    public static String getScript(String script, HttpServletRequest request) {
        if (request == null) {
            return script;
        }
        RequestContext rc = RequestContext.getRequestContext(request);
        if (rc != null && rc.getConnectionId().length() > 0) {
            ConnectionProcessor cp = new ConnectionProcessor(rc.getConnectionId());
            SapphireConnection sc = cp.getSapphireConnection();
            return HttpUtil.getScript(script, sc.getUseFullIncludes());
        }
        return HttpUtil.getScript(script, false);
    }

    public static String getScript(String script, boolean useFullIncludes) {
        if (useFullIncludes) {
            return script.substring(0, script.length() - 2) + "js-orig";
        }
        return script;
    }

    public static boolean jsDebugEnabled(com.labvantage.sapphire.admin.system.ConfigurationProcessor config) {
        boolean out = false;
        try {
            String jsDebug = config.getSysConfigProperty("jsdebug", "N");
            if (jsDebug.equalsIgnoreCase("y")) {
                out = true;
            } else if (config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("y")) {
                out = true;
            }
        }
        catch (Exception e) {
            out = false;
        }
        return out;
    }

    public static String getSapphireCoreJSHTML(PageContext pageContext, RequestContext requestContext) {
        return HttpUtil.getSapphireCoreJSHTML(pageContext, requestContext, null);
    }

    public static String getHTMLTag(boolean rtl) {
        return HttpUtil.getDocType() + "\n<html " + (rtl ? " style=\"direction:rtl\"" : CONST_VERSIONPREFIX) + " class=\"html5body\">\n";
    }

    public static String getHTMLTag(PageContext pageContext) {
        return HttpUtil.getDocType() + "\n<html " + (pageContext != null && sapphire.util.HttpUtil.getConnectionInfo(pageContext) != null && sapphire.util.HttpUtil.getConnectionInfo(pageContext).isRtl() ? " style=\"direction:rtl\"" : CONST_VERSIONPREFIX) + " class=\"html5body\">\n";
    }

    public static String getMetaTags() {
        return "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">";
    }

    public static String getDocType() {
        return "<!DOCTYPE html>";
    }

    public static String getCoreFontStyle(PageContext pageContext) {
        try {
            String corefontstyle = CONST_VERSIONPREFIX;
            if (pageContext.getSession().getAttribute("__corefontstyle") == null) {
                ConfigurationProcessor cp = new ConfigurationProcessor(pageContext);
                PropertyList guiPolicy = cp.getPolicy("GUIPolicy", "Sapphire Custom");
                corefontstyle = guiPolicy != null ? guiPolicy.getProperty("corefontstyle", "Open Sans") : "Open Sans";
                pageContext.getSession().setAttribute("__corefontstyle", (Object)corefontstyle);
            } else {
                corefontstyle = pageContext.getSession().getAttribute("__corefontstyle").toString();
            }
            if (corefontstyle.length() > 0 && !corefontstyle.equalsIgnoreCase("Open Sans")) {
                if (corefontstyle.equalsIgnoreCase("Tahoma")) {
                    return "<style>body{font-family: Tahoma,Verdana,sans-serif !important;}</style>";
                }
                if (corefontstyle.equalsIgnoreCase("Arial")) {
                    return "<style>body{font-family: arial,helvetica,sans-serif !important;}</style>";
                }
                return CONST_VERSIONPREFIX;
            }
            return CONST_VERSIONPREFIX;
        }
        catch (Exception e) {
            return CONST_VERSIONPREFIX;
        }
    }

    public static String getCoreStyleSheets(boolean gwt, PageContext pageContext) {
        com.labvantage.sapphire.services.ConnectionInfo connectionInfo = pageContext != null ? sapphire.util.HttpUtil.getConnectionInfo(pageContext) : null;
        return HttpUtil.getCoreStyleSheets(gwt, pageContext != null ? new Browser(pageContext) : null, connectionInfo != null ? connectionInfo.isRtl() : false, connectionInfo != null ? connectionInfo.getUseFullIncludes() : false) + HttpUtil.getCoreFontStyle(pageContext);
    }

    public static String getCoreStyleSheets(boolean gwt, HttpServletRequest request) {
        RequestContext rc = RequestContext.getRequestContext(request);
        if (rc != null && rc.getConnectionId().length() > 0) {
            ConnectionProcessor cp = new ConnectionProcessor(rc.getConnectionId());
            SapphireConnection sc = cp.getSapphireConnection();
            return HttpUtil.getCoreStyleSheets(gwt, new Browser(request), sc.isRtl(), sc.getUseFullIncludes());
        }
        return HttpUtil.getCoreStyleSheets(gwt, null, false, false);
    }

    private static String getCoreStyleSheets(boolean gwt, Browser b, boolean isRtl, boolean useFullIncludes) {
        StringBuffer out = new StringBuffer();
        if (gwt) {
            out.append("<link rel=\"stylesheet\" href=\"").append(HttpUtil.getCSS("WEB-CORE/stylesheets/gwt.css", isRtl, useFullIncludes)).append("\" type=\"text/css\">");
        }
        out.append("<link rel=\"stylesheet\" href=\"").append(HttpUtil.getCSS("WEB-CORE/stylesheets/sapphire.css", isRtl, useFullIncludes)).append("\" type=\"text/css\">");
        if (b != null) {
            out.append("<link rel=\"stylesheet\" href=\"").append(HttpUtil.getCSS("WEB-CORE/stylesheets/fonts/opensans/opensans." + (b.isChrome() ? "woff2" : "woff") + ".css", isRtl, useFullIncludes) + "\" type=\"text/css\">");
        } else {
            out.append("<link rel=\"stylesheet\" href=\"").append(HttpUtil.getCSS("WEB-CORE/stylesheets/fonts/opensans/opensans.woff.css", isRtl, useFullIncludes)).append("\" type=\"text/css\">");
        }
        out.append("<link rel=\"stylesheet\" href=\"").append(HttpUtil.getCSS("WEB-CORE/stylesheets/labvantage.css", isRtl, useFullIncludes)).append("\" type=\"text/css\">");
        return out.toString();
    }

    public static String getSapphireCoreJSHTML(ConnectionInfo conInfo) {
        return HttpUtil.getSapphireCoreJSHTML(null, null, conInfo);
    }

    public static String getSapphireCoreJSHTML(PageContext pageContext, RequestContext requestContext, ConnectionInfo conInfo) {
        return JavaScriptAPITag.getJavaScriptAPI(pageContext, requestContext, conInfo);
    }

    public static String getGWTElementHTML(String elementid, String elementtype, PropertyList properties) {
        StringBuffer html = new StringBuffer();
        html.append("\n<script>");
        html.append("\nsapphire.gwt.addGWTElement( '").append(elementtype).append("', '").append(elementid).append("', ").append(properties.toJSONString(false)).append(" );");
        html.append("\n</script>");
        html.append("\n<div style=\"display:hidden; width:100%; height:100%; min-height: 100px;\" id=\"").append(elementid).append("\"></div>");
        return html.toString();
    }

    public static String getAnimationHTML() {
        return CONST_VERSIONPREFIX;
    }

    public static String getConnectionInfo(PageTagInfo pageInfo, PropertyList layout, TranslationProcessor tp, PageContext pageContext) {
        StringBuffer theHTML = new StringBuffer();
        PropertyList plUserOptions = layout.getPropertyList("loggedinuseroptions");
        if (plUserOptions != null) {
            theHTML.append(HttpUtil.getConnectionInfo(pageInfo, plUserOptions, true, tp, pageContext));
        }
        return theHTML.toString();
    }

    public static String getConnectionInfo(PageTagInfo pageInfo, PropertyList plUserOptions, boolean renderContainer, TranslationProcessor tp, PageContext pageContext) {
        StringBuffer theHTML = new StringBuffer();
        if (plUserOptions != null) {
            TimeZone tz = sapphire.util.HttpUtil.getSessionTimeZone(pageContext);
            boolean isDayLightSaving = tz.inDaylightTime(new Date());
            String timezoneid = tz.getDisplayName(isDayLightSaving, 0, sapphire.util.HttpUtil.getSessionLocale(pageContext));
            String userdesc = pageInfo.getProperty("sysuserdesc");
            String databaseid = pageInfo.getProperty("databaseid");
            String tip = userdesc != null && userdesc.length() > 0 ? tp.translate("User") + " " + userdesc + " (" + pageInfo.getProperty("sysuserid") + ")" : tp.translate("User") + " " + pageInfo.getProperty("sysuserid");
            tip = tip + " " + tp.translate("connected to database") + " " + pageInfo.getProperty("databaseid") + ".\n" + tp.translate("All times show in") + " " + tp.translate(tz.getID() + " time.") + "\n[time] " + tp.translate("left until the connection is cleared.");
            if (renderContainer) {
                theHTML.append("<div id=\"user_info_div\" class=\"layout_userinfo\" pretitle=\"").append(tip).append("\" title=\"\" onmouseenter=\"try{enterConnectionInfo( '").append(tp.translate("minutes")).append("', '").append(tp.translate("seconds")).append("', '").append(tp.translate("second")).append("')}catch(__e2_){}\" onmouseleave=\"try{leaveConnectionInfo()}catch(__e3_){}\">");
            }
            com.labvantage.sapphire.services.ConnectionInfo ci = sapphire.util.HttpUtil.getConnectionInfo(pageContext);
            String lastjobtype = ci.getCurrentJobtype();
            String jobtypelist = ci.getJobtypeList();
            StringBuffer text = new StringBuffer(plUserOptions.getProperty("sessiontext", CONST_VERSIONPREFIX));
            if (text.length() == 0) {
                String showDb;
                if (plUserOptions.getProperty("showuser").equalsIgnoreCase("UserId")) {
                    text.append(CONST_VERSIONPREFIX).append("[sysuserid]").append(" ");
                } else if (!plUserOptions.getProperty("showuser").equalsIgnoreCase("None")) {
                    text.append(CONST_VERSIONPREFIX).append("[currentuser]").append(" ");
                }
                if ("Y".equals(plUserOptions.getProperty("showjobtype")) && lastjobtype != null && lastjobtype.length() > 0) {
                    text.append("[jobtype]").append(" ");
                }
                if (databaseid != null && databaseid.length() > 0 && (showDb = plUserOptions.getProperty("showdatabase")).equalsIgnoreCase("Y")) {
                    if (userdesc != null && userdesc.length() > 0) {
                        text.append(tp.translate("ON")).append(" ");
                    }
                    text.append(CONST_VERSIONPREFIX).append("[databaseid]").append(" ");
                }
                if (timezoneid != null && timezoneid.length() > 0) {
                    text.append(tp.translate("TIME ZONE")).append(": ").append("[timezoneid] ");
                }
            }
            StringBuffer jobTypedHTML = new StringBuffer(CONST_VERSIONPREFIX);
            if (ci.getCurrentJobtype() != null && ci.getCurrentJobtype().length() > 0) {
                if (jobtypelist.indexOf(";") > 0) {
                    String[] jobtypes = StringUtil.split(jobtypelist, ";");
                    jobTypedHTML.append("<select name=\"jobtypeselector\" id=\"jobtypeselector\" onchange=\"document.location='rc?command=file&file=WEB-CORE/modules/security/jobtype.jsp&jobtype=' + encodeURIComponent( this.value ) + '&page=' + sapphire.page.getTop().sapphire.page.data.webpageid\">");
                    for (int i = 0; i < jobtypes.length; ++i) {
                        jobTypedHTML.append("<option value=\"" + jobtypes[i] + "\" " + (jobtypes[i].equals(lastjobtype) ? "selected" : CONST_VERSIONPREFIX) + ">" + jobtypes[i] + "</option>");
                    }
                    jobTypedHTML.append("</select>");
                } else {
                    jobTypedHTML.append(lastjobtype != null ? lastjobtype.toUpperCase() : CONST_VERSIONPREFIX).append(CONST_VERSIONPREFIX);
                }
            }
            String out = text.toString();
            String[] tokens = StringUtil.getExpressionTokens(out);
            PropertyList piP = pageInfo.getPropertyList();
            Configuration configuration = null;
            try {
                configuration = Configuration.getInstance();
            }
            catch (Exception exception) {
                // empty catch block
            }
            for (String jrrTolkien : tokens) {
                StringBuffer tokenrep = new StringBuffer();
                tokenrep.append("<span class=\"").append(jrrTolkien.equalsIgnoreCase("timezoneid") ? "layout_timezoneinfo" : "layout_userinfobold").append("\" userinfo=\"").append(jrrTolkien).append("\">");
                if (jrrTolkien.equalsIgnoreCase("currentuser")) {
                    tokenrep.append(userdesc.length() > 0 ? userdesc.toUpperCase() : piP.getProperty("sysuserid").toUpperCase());
                } else if (jrrTolkien.equalsIgnoreCase("databaseid")) {
                    tokenrep.append(databaseid);
                } else if (jrrTolkien.equalsIgnoreCase("timezoneid")) {
                    tokenrep.append(timezoneid.toUpperCase());
                } else if (jrrTolkien.equalsIgnoreCase("timezonedesc")) {
                    tokenrep.append(tp.translate("TIME ZONE"));
                } else if (jrrTolkien.equalsIgnoreCase("jobtype") && jobTypedHTML.length() > 0) {
                    tokenrep.append(tp.translate("AS")).append(" ");
                    tokenrep.append(jobTypedHTML);
                } else if (piP.containsKey(jrrTolkien) && piP.isSimple(jrrTolkien)) {
                    tokenrep.append(piP.getProperty(jrrTolkien, CONST_VERSIONPREFIX));
                } else if (jrrTolkien.equalsIgnoreCase("platform") && configuration != null) {
                    tokenrep.append(Configuration.getPlatformName(configuration.getPlatform()));
                } else if (jrrTolkien.equalsIgnoreCase("serverid") && configuration != null) {
                    tokenrep.append(configuration.getServerid());
                } else if (jrrTolkien.equalsIgnoreCase("servername") && configuration != null) {
                    try {
                        tokenrep.append(configuration.getServerHostName());
                    }
                    catch (Exception e) {
                        tokenrep.append("NA");
                    }
                } else if (jrrTolkien.equalsIgnoreCase("hostid") && configuration != null) {
                    try {
                        tokenrep.append(configuration.getHostid());
                    }
                    catch (Exception e) {
                        tokenrep.append("NA");
                    }
                } else if (jrrTolkien.startsWith("=")) {
                    String temp = jrrTolkien.substring(1);
                    tokenrep.append(tp.translate(temp));
                } else {
                    String methname = "get" + jrrTolkien;
                    for (Method method : ci.getClass().getMethods()) {
                        if (!methname.equalsIgnoreCase(method.getName()) || method.getParameterTypes().length != 0) continue;
                        try {
                            tokenrep.append(method.invoke(ci, null).toString());
                        }
                        catch (Exception exception) {}
                        break;
                    }
                }
                tokenrep.append("</span>");
                out = StringUtil.replaceAll(out, "[" + jrrTolkien + "]", tokenrep.toString(), true);
            }
            theHTML.append(out);
            if (renderContainer) {
                theHTML.append("</div>");
            }
        }
        if (theHTML.length() > 0) {
            return theHTML.toString();
        }
        return CONST_VERSIONPREFIX;
    }

    public static String getDevelopmentInfo(PageContext pageContext, boolean popup) {
        StringBuffer theHTML = new StringBuffer();
        if (pageContext != null) {
            try {
                com.labvantage.sapphire.admin.system.ConfigurationProcessor config = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(pageContext);
                TranslationProcessor tp = new TranslationProcessor(pageContext);
                boolean hasDevRecord = false;
                boolean isDevMode = false;
                boolean isSuspended = false;
                boolean isImplMode = false;
                boolean isHiddenMode = false;
                ArrayList<String> compDevCodes = Configuration.getCompDevCodes();
                String compcode = CONST_VERSIONPREFIX;
                if (config.getConnectionid().length() > 0) {
                    SapphireConnection sapphireConnection = new ConnectionProcessor(pageContext).getSapphireConnection();
                    String sysuserid = sapphireConnection == null ? CONST_VERSIONPREFIX : sapphireConnection.getSysuserId();
                    QueryProcessor queryProcessor = new QueryProcessor(pageContext);
                    hasDevRecord = queryProcessor.getSqlDataSet("SELECT propertyid, propertyvalue FROM sysconfig WHERE propertyid='devmode'").size() == 1;
                    String devMode = config.getSysConfigProperty("devmode", "N");
                    isDevMode = devMode.equals("Y");
                    isSuspended = devMode.equals("S");
                    isImplMode = "Y".equals(config.getSysConfigProperty("implmode", "N"));
                    isHiddenMode = "Y".equals(config.getProfileProperty(sysuserid, "viewhidden", "N"));
                    compcode = config.getSysConfigProperty("compcode");
                    if (!compDevCodes.contains(compcode)) {
                        compcode = CONST_VERSIONPREFIX;
                        config.setSysConfigProperty("compcode", compcode);
                        Configuration.setCompcode(sapphireConnection.getDatabaseId(), compcode);
                    }
                }
                if (isDevMode || isSuspended) {
                    if (!popup) {
                        theHTML.append("<font title=\"Double-click to toggle Dev Mode on and off\" ondblclick=\"sapphire.connection.toggleDevMode(event);\" id=\"devmode_info_div\" class=\"layout_devmodeinfo\" color=red style=\"font-family: arial black; font-weight:100; font-size: 10pt;background-color:black;padding:1px 8px 1px 8px;border-color: #418FBF;border-style: ridge;\">");
                        theHTML.append(isDevMode ? "DEVELOPMENT MODE" : "---SUSPENDED---").append(compDevCodes.size() > 0 ? " (c)" : CONST_VERSIONPREFIX);
                        theHTML.append("</font>");
                        theHTML.append("&nbsp;");
                    } else {
                        theHTML.append("&nbsp;&nbsp;");
                    }
                } else if (compDevCodes.size() > 0) {
                    if (!popup) {
                        theHTML.append("<font title=\"Double-click to " + (compDevCodes.size() == 1 ? "toggle" : "change") + " Component Development Mode\" ondblclick=\"sapphire.connection.toggleDevMode(event);\" id=\"devmode_info_div\" class=\"layout_devmodeinfo\" color=red style=\"font-family: arial black; font-weight:100; font-size: 10pt;background-color:black;padding:1px 8px 1px 8px;border-color: #418FBF;border-style: ridge;\">");
                        theHTML.append(compcode.length() > 0 ? "COMPONENT MODE: " + compcode : "---SUSPENDED---").append(hasDevRecord ? " (d)" : CONST_VERSIONPREFIX);
                        theHTML.append("</font>");
                        theHTML.append("&nbsp;");
                    } else {
                        theHTML.append("&nbsp;&nbsp;");
                    }
                } else if (isImplMode) {
                    if (!popup) {
                        theHTML.append("<font id=\"devmode_info_div\" class=\"layout_devmodeinfo\" color=red style=\"font-family: arial black; font-weight:100; font-size: 10pt;background-color:black;padding:1px 8px 1px 8px;border-color: #418FBF;border-style: ridge;\">");
                        theHTML.append("IMPLEMENTATION MODE");
                        theHTML.append("</font>");
                        theHTML.append("&nbsp;");
                    } else {
                        theHTML.append("&nbsp;&nbsp;");
                    }
                }
                if (isHiddenMode) {
                    theHTML.append("<font id=\"hiddenmode_info_div\" class=\"layout_devmodeinfo\" color=red style=\"font-family: arial black; font-weight:100; font-size: 10pt;background-color:black;padding:1px 8px 1px 8px;border-color: #418FBF;border-style: ridge;\">");
                    theHTML.append(tp.translate("Showing All Records"));
                    theHTML.append("</font>");
                    theHTML.append("&nbsp;");
                }
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
        }
        return theHTML.toString();
    }

    public static String getEvergreenLink(PageContext pageContext) {
        StringBuffer thehtml = new StringBuffer();
        if (pageContext != null) {
            try {
                com.labvantage.sapphire.admin.system.ConfigurationProcessor config = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(pageContext);
                Browser browser = new Browser(pageContext);
                String devMode = config.getConnectionid().length() > 0 ? config.getSysConfigProperty("devmode", "N") : "N";
                boolean isDevMode = !browser.isMobile() && devMode.equals("Y");
                boolean isSuspended = !browser.isMobile() && devMode.equals("S");
                String moduleList = ";" + ((RequestContext)pageContext.getRequest().getAttribute("RequestContext")).getProperty("modulelist") + ";";
                boolean hasEvergreenModule = !browser.isMobile() && (moduleList.indexOf("WPDPro") >= 0 || moduleList.indexOf("WPDStd") >= 0);
                boolean used = false;
                if (isDevMode || isSuspended || hasEvergreenModule) {
                    PropertyList layout;
                    PropertyList pagedata;
                    String webpageid = pageContext.getRequest().getParameter("page");
                    boolean hidebtns = false;
                    if ((webpageid == null || webpageid.length() == 0) && (pagedata = (PropertyList)pageContext.getRequest().getAttribute("pagedata")) != null) {
                        webpageid = pagedata.getProperty("page");
                    }
                    if ((layout = RequestContext.getRequestContext(pageContext).getPropertyList("layout")) != null && layout.getProperty("hidebuttons", "N").equalsIgnoreCase("Y")) {
                        hidebtns = true;
                    }
                    if (webpageid != null && webpageid.length() > 0 && !hidebtns) {
                        String edition = ((RequestContext)pageContext.getRequest().getAttribute("RequestContext")).getProperty("__productedition");
                        if (webpageid != null && webpageid.length() > 0) {
                            if (!used) {
                                thehtml.append("<td NOWRAP>");
                                used = true;
                            }
                            PropertyList pagedata2 = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", pageContext);
                            thehtml.append("&nbsp;");
                            Button b = new Button(pageContext);
                            b.setImg("WEB-CORE/images/gif/Edit.gif");
                            b.setTip("Edit Page " + (pagedata2 != null ? pagedata2.getProperty("page") : CONST_VERSIONPREFIX) + ". (This will only appear if you have access to the WebPage Designer)");
                            b.setAction("var webpageid = encodeURIComponent('" + webpageid + "');window.open( 'rc?command=page&page=PageMaintNavigator&pageid=' + webpageid + '&edition=" + edition + "');");
                            b.setStyle("height:22px;position:relative;top:-2;");
                            thehtml.append(b.getHtml());
                        }
                    }
                    if (used) {
                        thehtml.append("</td>");
                    }
                }
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
        }
        return thehtml.toString();
    }

    public static String getCompanyLogo(PageContext pageContext, boolean includeEvergreenLink) {
        PropertyList layout;
        RequestContext rc;
        StringBuffer thehtml = new StringBuffer();
        String appImage = "WEB-OPAL/layouts/images/logo_labvantage.png";
        if (pageContext != null && (rc = RequestContext.getRequestContext(pageContext)) != null && (layout = rc.getPropertyList("layout")) != null) {
            appImage = layout.getProperty("companylogo", appImage);
        }
        thehtml.append("<table cellpadding=0 cellspacing=0 border=0 class=\"logo_table\">\n");
        thehtml.append("<tr>\n");
        thehtml.append("<td class=\"logo_image\" valign=middle>\n");
        thehtml.append("<img alt=\"LV Logo\" title=\"");
        thehtml.append(CONST_TITLE).append(" ").append(CONST_VERSIONPREFIX).append(Build.getVersion()).append(" Build ").append(Build.getBuild()).append(" ").append(Build.getBuildDate()).append(" (Patch ").append(Build.getPatch()).append(") ").append("\" src=\"").append(appImage).append("\">\n");
        thehtml.append("</td>\n");
        if (includeEvergreenLink) {
            thehtml.append(HttpUtil.getEvergreenLink(pageContext));
        }
        thehtml.append("</tr>");
        thehtml.append("</table>");
        return thehtml.toString();
    }

    public static String getApplicationLogo(String appTitle, String appImage) {
        StringBuffer thehtml = new StringBuffer();
        appTitle = HttpUtil.getApplicationTitle(appTitle);
        thehtml.append("<table cellpadding=0 cellspacing=0 border=0 class=\"applogo_table\" style=\"height:30px;\">\n");
        thehtml.append("<tr style=\"height:30px;\">\n");
        if (appImage != null && appImage.trim().length() > 0) {
            thehtml.append("<td class=\"applogo_image\" nowrap valign=middle style=\"height:30px;\">\n");
            thehtml.append("<img title=\"");
            thehtml.append(appTitle).append("\" src=\"").append(appImage).append("\" style=\"height:28px;width:auto;\">\n");
            thehtml.append("&nbsp;\n");
            thehtml.append("</td>\n");
        }
        thehtml.append("<td class=\"applogo_text\" nowrap valign=middle>\n");
        thehtml.append(appTitle);
        thehtml.append("</td>\n");
        thehtml.append("</tr>");
        thehtml.append("</table>");
        return thehtml.toString();
    }

    public static String getApplicationTitle(String appTitle) {
        String out = appTitle;
        if (out != null && out.length() > 0) {
            String[] expressions;
            if (out.equals("SAPPHIRE R4.6") || out.equals("SAPPHIRE R4.7")) {
                out = "[title] [versionprefix][version]";
            }
            if (out.indexOf("[") > -1 && out.indexOf("]") > -1 && (expressions = StringUtil.getTokens(out)) != null && expressions.length > 0) {
                for (int i = 0; i < expressions.length; ++i) {
                    if (expressions[i].equalsIgnoreCase("version")) {
                        out = StringUtil.replaceAll(out, "[" + expressions[i] + "]", Build.getVersion(), false);
                        continue;
                    }
                    if (expressions[i].equalsIgnoreCase("iteration")) {
                        String t = Build.getVersion();
                        int index = t.indexOf("(I");
                        t = index > -1 ? t.substring(index + 2, t.indexOf(")")) : CONST_VERSIONPREFIX;
                        out = StringUtil.replaceAll(out, "[" + expressions[i] + "]", t, false);
                        continue;
                    }
                    if (expressions[i].equalsIgnoreCase("build")) {
                        out = StringUtil.replaceAll(out, "[" + expressions[i] + "]", Build.getBuild(), false);
                        continue;
                    }
                    if (expressions[i].equalsIgnoreCase("patch")) {
                        out = StringUtil.replaceAll(out, "[" + expressions[i] + "]", Build.getPatch(), false);
                        continue;
                    }
                    if (expressions[i].equalsIgnoreCase("title")) {
                        out = StringUtil.replaceAll(out, "[" + expressions[i] + "]", CONST_TITLE, false);
                        continue;
                    }
                    if (expressions[i].equalsIgnoreCase("applicationdesc")) {
                        try {
                            out = StringUtil.replaceAll(out, "[" + expressions[i] + "]", Configuration.getInstance().getApplicationdesc(), false);
                        }
                        catch (Exception e) {
                            out = StringUtil.replaceAll(out, "[" + expressions[i] + "]", CONST_VERSIONPREFIX, false);
                        }
                        continue;
                    }
                    if (!expressions[i].equalsIgnoreCase("versionprefix")) continue;
                    out = StringUtil.replaceAll(out, "[" + expressions[i] + "]", CONST_VERSIONPREFIX, false);
                }
            }
        } else {
            out = CONST_VERSIONPREFIX;
        }
        return out;
    }

    public static String getLinks(PropertyList plLayout, Browser clientBrowser, TranslationProcessor tp) {
        return HttpUtil.getLinks(null, plLayout, clientBrowser, tp, CONST_VERSIONPREFIX);
    }

    public static String getLinks(StringBuffer dropdowns, PropertyList plLayout, Browser clientBrowser, TranslationProcessor tp) {
        return HttpUtil.getLinks(dropdowns, plLayout, clientBrowser, tp, CONST_VERSIONPREFIX);
    }

    public static String getLinks(StringBuffer dropdowns, PropertyList plLayout, Browser clientBrowser, TranslationProcessor tp, String startupType) {
        PropertyListCollection plcLinks = plLayout.getCollection("links");
        if (plcLinks != null) {
            PropertyList plLink;
            for (int i = 0; i < plcLinks.size(); ++i) {
                plLink = plcLinks.getPropertyList(i);
                String prevGroup = plLink.getProperty("group", CONST_VERSIONPREFIX);
                String showLink = plLink.getProperty("show", "Y");
                String text = LayoutUtil.getTheLinkText(plLink, tp);
                String link = LayoutUtil.getTheLink(plLink, false);
                String id = plLink.getProperty("id", CONST_VERSIONPREFIX);
                String icon = plLink.getProperty("icon", "WEB-OPAL/layouts/generic/images/blank.gif");
                String ismobile = plLink.getProperty("ismobile", "Y");
                if ("Fixed".equals(startupType) && id.equals("setstartup")) {
                    showLink = "N";
                }
                id = id.length() == 0 ? "layout_link" + i : id + "_link";
                if (!"Y".equalsIgnoreCase(showLink)) {
                    plcLinks.remove(i);
                    --i;
                } else if (prevGroup.length() > 0) {
                    for (int l = i + 1; l < plcLinks.size(); ++l) {
                        PropertyList plcurrLink = plcLinks.getPropertyList(l);
                        String currentGroup = plcurrLink.getProperty("group", CONST_VERSIONPREFIX);
                        String currentShowLink = plcurrLink.getProperty("show", "Y");
                        if (!"Y".equalsIgnoreCase(currentShowLink) || !currentGroup.equals(prevGroup)) continue;
                        String currentText = LayoutUtil.getTheLinkText(plcurrLink, tp);
                        String currentLink = LayoutUtil.getTheLink(plcurrLink, false);
                        String currentId = plcurrLink.getProperty("id", CONST_VERSIONPREFIX);
                        String currentIsMobile = plcurrLink.getProperty("ismobile", "Y");
                        String currentIcon = plcurrLink.getProperty("icon", "WEB-OPAL/layouts/generic/images/blank.gif");
                        currentId = currentId.length() == 0 ? "layout_link" + l : currentId + "_link";
                        if (currentLink.length() > 0 && currentText.length() > 0) {
                            text = text + "|" + currentText;
                            link = link + "|" + currentLink;
                            id = id + "|" + currentId;
                            ismobile = ismobile + "|" + currentIsMobile;
                            icon = icon + "|" + currentIcon;
                        }
                        plcLinks.remove(l);
                        --l;
                    }
                }
                plLink.setProperty("id", id);
                plLink.setProperty("text", text);
                plLink.setProperty("link", link);
                plLink.setProperty("ismobile", ismobile);
                plLink.setProperty("icon", icon);
            }
            StringBuffer html = new StringBuffer();
            html.append("<table style=\"margin-top: 5px;text-align:left;\" cellspacing=\"").append(clientBrowser.isMobile() ? "2" : "0").append("\" cellpadding=\"0\"><tr>");
            if (clientBrowser.isMobile()) {
                for (int il = 0; il < plcLinks.size(); ++il) {
                    PropertyList plLink2 = plcLinks.getPropertyList(il);
                    String text = plLink2.getProperty("text");
                    String link = plLink2.getProperty("link");
                    String linkid = plLink2.getProperty("id", CONST_VERSIONPREFIX);
                    String icon = plLink2.getProperty("icon", CONST_VERSIONPREFIX);
                    if (text.indexOf("|") > -1 && link.indexOf("|") > -1) {
                        String[] texts = StringUtil.split(text, "|");
                        String[] links = StringUtil.split(link, "|");
                        String[] linkids = StringUtil.split(linkid, "|");
                        String[] icons = StringUtil.split(icon, "|");
                        String[] ismobile = StringUtil.split(plLink2.getProperty("ismobile", "Y"), "|");
                        if (!ismobile[0].equalsIgnoreCase("Y")) continue;
                        html.append("<td class=\"links_one\" valign=middle align=center id=\"layout_linkcell").append(il).append("_").append(0).append("\">");
                        html.append("<div style=\"display:none;position:absolute;z-index:200;\" class=\"link_dropdown_box\" id=\"link_dropdown").append(il).append("\" onmouseover=\"mouseOverLinkDropdown( ").append(il).append(" );\" onmouseout=\"mouseOutLinkDropdown( ").append(il).append(" );\">");
                        html.append("<table width=\"100%\" height=\"100%\" cellpadding=0 cellspaing=0 border=0 >");
                        for (int l = 0; l < texts.length; ++l) {
                            html.append("<tr >");
                            html.append("<td class=\"link_dropdown_box_cell\" onmouseover=\"this.className='link_dropdown_box_cell_over';this.parentElement.childNodes[1].className='link_dropdown_box_cell_over';\" onclick=\"").append(links[l]).append("\" onmouseout=\"this.className='link_dropdown_box_cell';this.parentElement.childNodes[1].className='link_dropdown_box_cell';\">");
                            html.append("<img title=\"").append(texts[l]).append("\" id=\"").append(linkids[l]).append("_img\" u_alias=\"layout_sublink").append(l).append("\" src=\"").append(icons[l]).append("\">");
                            html.append("</td>");
                            html.append("<td class=\"link_dropdown_box_cell\" onmouseover=\"this.className='link_dropdown_box_cell_over';this.parentElement.childNodes[0].className='link_dropdown_box_cell_over';\" onclick=\"").append(links[l]).append("\" onmouseout=\"this.className='link_dropdown_box_cell';this.parentElement.childNodes[0].className='link_dropdown_box_cell';\">");
                            html.append("<a class=\"layout_links_dropdown\" title=\"").append(texts[l]).append("\" id=\"").append(linkids[l]).append("\" u_alias=\"layout_sublink").append(l).append("\">").append(texts[l]).append("</a>");
                            html.append("</td>");
                            html.append("</tr>");
                        }
                        html.append("</table>");
                        html.append("</div>");
                        html.append("<a title=\"").append(texts[0]).append("\"  href=\"javascript:doLinkDropdown(").append(il).append(", true);\" id=\"").append(linkids[0]).append("\"  onmouseover=\"this.parentElement.className='links_one_over';\" onmouseout=\"this.parentElement.className='links_one';\">").append("<img src='").append(icons[0] + "' border=0 title='").append(texts[0]).append("' width=16 height=16 >").append("</a>");
                        html.append("</td>");
                        continue;
                    }
                    if (!plLink2.getProperty("ismobile", "Y").equalsIgnoreCase("Y")) continue;
                    html.append("<td class=\"links_one\" valign=middle align=center id=\"layout_linkcell").append(il).append("\">");
                    html.append("<a title=\"").append(text).append("\" href=\"").append(link).append("\" id=\"").append(linkid).append("\"  onmouseover=\"this.parentElement.className='links_one_over';\" onmouseout=\"this.parentElement.className='links_one';\">").append("<img src='").append(icon + "' border=0 title='").append(text).append("' width=16 height=16 >").append("</a>");
                    html.append("</td>");
                }
            } else if (plcLinks.size() == 1) {
                plLink = plcLinks.getPropertyList(0);
                String text = plLink.getProperty("text");
                String link = plLink.getProperty("link");
                String linkid = plLink.getProperty("id", CONST_VERSIONPREFIX);
                linkid = linkid.length() == 0 ? "layout_link0" : linkid + "_link";
                html.append("<td class=\"links_one\" valign=middle align=center id=\"layout_linkcell").append(0).append("\">");
                html.append("<a title=\"").append(text).append("\" href=\"").append(link).append(" id=\"").append(linkid).append("\"  onmouseover=\"this.parentElement.className='links_one_over';\" onmouseout=\"this.parentElement.className='links_one';\">").append(GenericLayout.getTheLinkImage(plLink, text)).append("</a>");
                html.append("</td>");
            } else {
                boolean first = true;
                String hint = tp.translate("Click for drop down links");
                for (int i = 0; i < plcLinks.size(); ++i) {
                    PropertyList plLink3 = plcLinks.getPropertyList(i);
                    String text = plLink3.getProperty("text");
                    String link = plLink3.getProperty("link");
                    String linkid = plLink3.getProperty("id", CONST_VERSIONPREFIX);
                    linkid = linkid.length() == 0 ? "layout_link" + i : linkid + "_link";
                    if (first) {
                        first = false;
                        GenericLayout.renderLinkHTML(html, dropdowns == null ? html : dropdowns, i, text, link, linkid, "links_left", "links_dropdown", plLink3);
                        continue;
                    }
                    if (i == plcLinks.size() - 1) {
                        GenericLayout.renderLinkHTML(html, dropdowns == null ? html : dropdowns, i, text, link, linkid, "links_right", "links_dropdownright", plLink3);
                        continue;
                    }
                    GenericLayout.renderLinkHTML(html, dropdowns == null ? html : dropdowns, i, text, link, linkid, "links_center", "links_dropdown", plLink3);
                }
            }
            html.append("</tr></table>");
            return html.toString();
        }
        return CONST_VERSIONPREFIX;
    }

    public static String htmlEncode(String inputString) {
        if (inputString == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(inputString.length());
        block6: for (int i = 0; i < inputString.length(); ++i) {
            char c = inputString.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') {
                buf.append(c);
                continue;
            }
            switch (c) {
                case '\"': {
                    buf.append("&quot;");
                    continue block6;
                }
                case '<': {
                    buf.append("&lt;");
                    continue block6;
                }
                case '>': {
                    buf.append("&gt;");
                    continue block6;
                }
                case '&': {
                    buf.append("&amp;");
                    continue block6;
                }
                default: {
                    buf.append("&#").append((int)c).append(";");
                }
            }
        }
        return buf.toString();
    }
}

