/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.layouts.modern;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.layouts.modern.GizmoTargetAjaxManager;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.GroupGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.json.JSONException;
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
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ModernLayout {
    private static final String TITLE = "LabVantage";
    public static final String LOADER = "WEB-CORE/images/spinners/flat_blue_spinner.svg";
    private static final String LOGO = "WEB-CORE/layouts/modern/images/logowhite.png";
    public static final String GUI_POLICY = "GUIPolicy";
    public final int HEADHEIGHT = 50;
    public final int SIDEBARWIDTH = 100;
    public final int SIDEPADDING = 2;
    public final int STATUSBARHEIGHT = 20;
    public final int NAVIGATIONBARHEIGHT = 29;
    private TranslationProcessor tp = null;
    private String processingMsg = "";
    private PropertyList plLayout = null;
    private PageTagInfo plPageinfo = null;
    private Browser clientBrowser = null;
    private RequestContext requestContext = null;
    private PropertyList userconfig = null;
    private boolean html5 = false;
    private String profilePicturePath = "";
    private boolean showSidebar = true;
    private boolean sidebarDocked = false;
    private int sidebarWidth = 100;
    private com.labvantage.sapphire.admin.system.ConfigurationProcessor cp = null;
    private ConnectionInfo connectionInfo = null;
    private PageContext pageContext;
    private DevModeState devMode = null;
    private PropertyList guiPolicy = null;
    private String gizmogroup = null;
    private boolean gizmogroupLocked = false;
    private List<String> roles = null;
    public final String PROFILEDEFAULT = "WEB-CORE/images/profiles/default46.png";
    public final String WALLPAPERDEFAULT = "none";
    public final String WALLPAPERCOLORDEFAULT = "#E4EAEE";
    public final String WALLPAPERGRADCOLOR1DEFAULT = "#FFFFFF";
    public final String WALLPAPERGRADCOLOR2DEFAULT = "#999";
    public final String WALLPAPERGRADCOLOR3DEFAULT = "#FFFFFF";
    public static final String SESSION_STARTSTATE = "_startstate_";
    public static final String SESSION_STATECACHE = "_statecache_";
    public static final String SESSION_GROUPCACHE = "_sidebarcache_";
    public static final String SESSION_MENUCACHE = "_menucache_";

    public int getSidebarTogWidth() {
        if (this.showSidebar) {
            if (this.clientBrowser != null && this.clientBrowser.hasTouch()) {
                return 18;
            }
            return 8;
        }
        return 0;
    }

    public boolean getHtml5() {
        return this.html5;
    }

    public String getStyleSheets() {
        StringBuffer sb = new StringBuffer();
        if (this.guiPolicy != null && this.guiPolicy.getCollection("stylesheets") != null) {
            PropertyListCollection styleSheetCollection = this.guiPolicy.getCollection("stylesheets");
            for (int i = 0; i < styleSheetCollection.size(); ++i) {
                PropertyList styleSheet = styleSheetCollection.getPropertyList(i);
                if (styleSheet == null || styleSheet.getProperty("url").trim().length() <= 0) continue;
                sb.append("<link id=\"").append(styleSheet.getProperty("id", "customstyle" + i)).append("\" rel=\"stylesheet\" href=\"").append(styleSheet.getProperty("url").trim());
                sb.append("\" type=\"text/css\"/>");
            }
        }
        return sb.toString();
    }

    public PropertyListCollection getStyleSheetsList() {
        return this.guiPolicy.getCollection("stylesheets");
    }

    public String getLoadingDiv() {
        StringBuffer html = new StringBuffer();
        String image = "";
        String msg = "";
        PropertyList lp = this.guiPolicy.getPropertyList("loadingpanel");
        if (lp != null) {
            msg = lp.getProperty("message", "").trim();
            image = ModernLayout.getLoaderImage(this.guiPolicy);
        } else {
            image = LOADER;
            msg = "";
        }
        html.append("<div id=\"ws_loading\" style=\"display:none;\"><div class=\"ws_loadcont\"><div class=\"ws_loadtxt\">" + msg + "</div><div class=\"ws_loadimg\"><img src=\"").append(image).append("\"></div></div></div>");
        return html.toString();
    }

    public boolean isDevMode() {
        if (this.devMode != null) {
            return this.devMode.isDevMode;
        }
        return false;
    }

    public String getScriptIncludes() {
        StringBuffer sb = new StringBuffer();
        if (this.guiPolicy != null && this.guiPolicy.getCollection("includes") != null) {
            sb.append(ModernLayout.getIncludes(this.guiPolicy.getCollection("includes"), ""));
        }
        return sb.toString();
    }

    public PropertyListCollection getScriptIncludeList() {
        return this.guiPolicy.getCollection("includes");
    }

    public static String getIncludes(PropertyListCollection includes, String position) {
        StringBuffer sb = new StringBuffer();
        if (includes != null) {
            for (int i = 0; i < includes.size(); ++i) {
                PropertyList include = includes.getPropertyList(i);
                if (include == null || include.getProperty("url").trim().length() <= 0 || !include.getProperty("position").equalsIgnoreCase("") && !include.getProperty("position").equalsIgnoreCase(position)) continue;
                sb.append("<script id=\"").append(include.getProperty("id", "customstyle" + i)).append("\" src=\"").append(include.getProperty("url").trim());
                sb.append("\" type=\"text/javascript\"></script>");
            }
        }
        return sb.toString();
    }

    public static String getLiteralInclude(PropertyList guiPolicy) {
        if (guiPolicy != null && guiPolicy.getProperty("literalsinclude").length() > 0) {
            return "<script type=\"text/javascript\" src='" + guiPolicy.getProperty("literalsinclude") + "'></script>";
        }
        return "<script type=\"text/javascript\" src='WEB-OPAL/scripts/language/literals_english.js'></script>";
    }

    public String getLiteralInclude() {
        return ModernLayout.getLiteralInclude(this.guiPolicy);
    }

    public boolean getSidebarDocked() {
        return this.hasGizmoGroup() && this.showSidebar ? this.sidebarDocked : false;
    }

    public int getSidebarWidth() {
        return this.showSidebar ? this.sidebarWidth : 0;
    }

    public String getApplicationTitle() {
        return HttpUtil.getApplicationTitle(this.guiPolicy.getProperty("applicationtitle", "[title]"));
    }

    public static String getProfilePicture(PageContext pageContext, ConnectionInfo connectionInfo, int size) {
        String out = "";
        QueryProcessor qp = new QueryProcessor(pageContext);
        try {
            int i = qp.getPreparedCount("SELECT COUNT(attachmentnum) FROM sdiattachment WHERE sdcid = 'User' AND keyid1=? AND attachmentclass='ProfilePicture'", new Object[]{connectionInfo.getSysuserId()});
            if (i > 0) {
                int r = new Random().nextInt(100) + 1;
                out = "rc?command=image&attachment=User;" + connectionInfo.getSysuserId() + ";;;ProfilePicture&" + (size > 0 ? "width=" + size + "&height=" + size : "") + "&_r" + r;
            } else {
                out = "";
            }
        }
        catch (Exception e) {
            out = "";
        }
        return out;
    }

    public ModernLayout(PageContext pageContext) {
        this.pageContext = pageContext;
        this.tp = new TranslationProcessor(pageContext);
        this.requestContext = RequestContext.getInstance((HttpServletRequest)pageContext.getRequest());
        Object o = ((HttpServletRequest)pageContext.getRequest()).getSession().getAttribute("userconfig");
        this.userconfig = o != null ? (PropertyList)o : this.requestContext.getPropertyList("userconfig");
        try {
            this.roles = Arrays.asList(StringUtil.split(this.requestContext.getProperty("rolelist"), ";"));
        }
        catch (Exception e1) {
            this.roles = new ArrayList<String>();
        }
        this.clientBrowser = new Browser(pageContext);
        this.html5 = this.requestContext.getProperty("html5").equalsIgnoreCase("Y");
        this.plLayout = this.requestContext.getPropertyList("layout");
        this.plPageinfo = new PageTagInfo(pageContext, this.requestContext);
        this.processingMsg = "";
        this.cp = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(pageContext);
        this.devMode = new DevModeState(this.cp);
        ConfigurationProcessor cp = new ConfigurationProcessor(pageContext);
        try {
            this.guiPolicy = cp.getPolicy(GUI_POLICY, "Sapphire Custom");
        }
        catch (Exception e) {
            this.guiPolicy = null;
        }
        this.profilePicturePath = ModernLayout.getProfilePicture(pageContext, this.connectionInfo, 46);
        if (this.clientBrowser.isEmbedded() || !this.clientBrowser.getGUIMode().getSidebar()) {
            this.sidebarDocked = false;
            this.sidebarWidth = 0;
            this.showSidebar = false;
        } else {
            this.showSidebar = true;
            this.sidebarDocked = this.clientBrowser.getGUIMode().isPhone() ? false : (this.clientBrowser.getGUIMode().isTablet() && this.clientBrowser.getViewPort().isPortrait() && this.clientBrowser.getViewPort().getDeviceWidth() < 800 ? false : (this.userconfig != null ? this.userconfig.getProperty("__modern__sidebar_docked", "N") : "N").equalsIgnoreCase("Y"));
            try {
                this.sidebarWidth = this.userconfig != null ? Integer.parseInt(this.userconfig.getProperty("__modern__sidebar_width", "100")) : 100;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.plLayout != null) {
            this.plLayout.setProperty("hidebuttons", "Y");
            if (this.plLayout.getPropertyList("processingdiv") != null) {
                this.processingMsg = this.plLayout.getPropertyList("processingdiv").getProperty("text");
            }
        }
        if (this.processingMsg.equalsIgnoreCase("")) {
            this.processingMsg = this.tp.translate("Processing, please wait...");
        }
    }

    public String getBodyClass() {
        return "standards" + (this.clientBrowser.getGUIMode() != null ? " " + this.clientBrowser.getGUIMode().getId() + (this.clientBrowser != null && this.clientBrowser.hasTouch() ? " hastouch" : "") : "");
    }

    public String getStartMenu() {
        String currentjobtype;
        String out = "";
        if (!this.guiPolicy.getProperty("skipjobtypeurl", "N").equalsIgnoreCase("Y") && (currentjobtype = this.getConnectionInfo().getCurrentJobtype()) != null && currentjobtype.length() > 0) {
            if (this.pageContext.getAttribute("jobtypedefaultmenu") == null) {
                DataSet jobtypedata = new QueryProcessor(this.pageContext).getPreparedSqlDataSet("SELECT defaultlogonurl, defaultmenu, defaultsidebar FROM jobtype WHERE jobtypeid=?", new Object[]{currentjobtype});
                if (jobtypedata != null && jobtypedata.getRowCount() > 0) {
                    out = jobtypedata.getValue(0, "defaultmenu");
                    this.pageContext.setAttribute("jobtypedefaultmenu", (Object)out);
                    this.pageContext.setAttribute("jobtypedefaultgroup", (Object)jobtypedata.getValue(0, "defaultsidebar"));
                } else {
                    this.pageContext.setAttribute("jobtypedefaultgroup", (Object)"");
                    this.pageContext.setAttribute("jobtypedefaultmenu", (Object)"");
                }
            } else {
                out = this.pageContext.getAttribute("jobtypedefaultmenu").toString();
            }
        }
        if (out.length() == 0) {
            try {
                out = this.cp.getProfileProperty(this.getConnectionInfo().getSysuserId(), this.clientBrowser.getGUIMode().getId() + "logonmenu", this.clientBrowser.getGUIMode().getStartupMenuGizmo());
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (out.length() == 0) {
                out = this.guiPolicy != null ? this.guiPolicy.getProperty("defaultmenugizmo", "") : "";
            }
        }
        return out;
    }

    public String getMenuGizmo() {
        String out = "";
        out = this.requestContext.getProperty("topmenuid");
        if (out.length() == 0) {
            Object o;
            String windowid = this.requestContext.getProperty("_wid");
            if (windowid.length() > 0 && this.pageContext != null && (o = this.pageContext.getSession().getAttribute(SESSION_MENUCACHE + windowid)) != null) {
                out = o.toString();
            }
            if (out.length() == 0) {
                o = this.pageContext.getSession().getAttribute("_menucache_last");
                if (o != null) {
                    out = o.toString();
                }
                if (out.length() == 0) {
                    out = this.getStartMenu();
                }
            }
        }
        return out;
    }

    public String getStartGizmoGroup() {
        String currentjobtype;
        String out = "";
        if (!this.guiPolicy.getProperty("skipjobtypeurl", "N").equalsIgnoreCase("Y") && (currentjobtype = this.getConnectionInfo().getCurrentJobtype()) != null && currentjobtype.length() > 0) {
            if (this.pageContext.getAttribute("jobtypedefaultgroup") == null) {
                DataSet jobtypedata = new QueryProcessor(this.pageContext).getPreparedSqlDataSet("SELECT defaultlogonurl, defaultmenu, defaultsidebar FROM jobtype WHERE jobtypeid=?", new Object[]{currentjobtype});
                if (jobtypedata != null && jobtypedata.getRowCount() > 0) {
                    out = jobtypedata.getValue(0, "defaultsidebar");
                    this.pageContext.setAttribute("jobtypedefaultgroup", (Object)out);
                    this.pageContext.setAttribute("jobtypedefaultmenu", (Object)jobtypedata.getValue(0, "defaultmenu"));
                } else {
                    this.pageContext.setAttribute("jobtypedefaultgroup", (Object)"");
                    this.pageContext.setAttribute("jobtypedefaultmenu", (Object)"");
                }
            } else {
                out = this.pageContext.getAttribute("jobtypedefaultgroup").toString();
            }
        }
        if (out.length() == 0) {
            try {
                out = this.cp.getProfileProperty(this.getConnectionInfo().getSysuserId(), this.clientBrowser.getGUIMode().getId() + "logongroup", this.clientBrowser.getGUIMode().getStartupGroupGizmo());
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (out.length() == 0) {
                out = this.guiPolicy != null ? this.guiPolicy.getProperty("defaultgroupgizmo", "") : "";
            }
        }
        return out;
    }

    public String getGizmoGroup() {
        String out = "";
        if (this.gizmogroup == null) {
            if (this.showSidebar && (out = this.requestContext.getProperty("sidebargroupid")).length() == 0) {
                Object o;
                String windowid = this.requestContext.getProperty("_wid");
                if (windowid.length() > 0 && this.pageContext != null && (o = this.pageContext.getSession().getAttribute(SESSION_GROUPCACHE + windowid)) != null) {
                    out = o.toString();
                }
                if (out.length() == 0) {
                    o = this.pageContext.getSession().getAttribute("_sidebarcache_last");
                    if (o != null) {
                        out = o.toString();
                    }
                    if (out.length() == 0) {
                        out = this.getStartGizmoGroup();
                    }
                }
            }
            this.gizmogroup = out;
        }
        return this.gizmogroup;
    }

    public boolean hasGizmoGroup() {
        return this.getGizmoGroup().length() > 0;
    }

    public RequestContext getRequestContext() {
        return this.requestContext;
    }

    public ConnectionInfo getConnectionInfo() {
        if (this.connectionInfo == null) {
            this.connectionInfo = new ConnectionProcessor(this.pageContext).getConnectionInfo(this.requestContext.getConnectionId());
        }
        return this.connectionInfo;
    }

    public String getStatusBar(StatusBarPosition currentPosition) {
        if (!this.clientBrowser.isEmbedded()) {
            StringBuffer html = new StringBuffer();
            StatusBarPosition sbp = this.getStatusBarPosition();
            if (sbp == currentPosition) {
                PropertyList sb = this.guiPolicy != null && this.guiPolicy.getPropertyList("statusbar") != null ? this.guiPolicy.getPropertyList("statusbar") : new PropertyList();
                String bcolor = sb.getProperty("color", "#344A5F");
                String fcolor = sb.getProperty("fontcolor", "#FFFFFF");
                html.append("<div class=\"layout_hidden\" id=\"layout_statusbar\" style=\"").append(currentPosition == StatusBarPosition.BOTTOM ? "z-index: 1;" : "").append("height:").append(20).append("px;").append(sbp == StatusBarPosition.BOTTOM ? "position: absolute;" : "").append("background-color: ").append(bcolor).append(";color:").append(fcolor).append(";\">");
                html.append("<div id=\"deviceinfo\" class=\"sb_left\">");
                html.append("</div>");
                html.append("<script>");
                html.append("sapphire.events.attachEvent(window,'onload',function(){modernLayout.changeGUIMode()});");
                html.append("</script>");
                html.append("<div id=\"labvantagestatusbar\" class=\"sb_left\">");
                html.append("&nbsp;");
                html.append("</div>");
                html.append("<div class=\"sb_right\">");
                String changelogoptions = new ConfigurationProcessor(this.getConnectionInfo().getConnectionId()).getProfileProperty("changelogoptions");
                if (changelogoptions != null && changelogoptions.length() > 0) {
                    try {
                        JSONObject o = new JSONObject(changelogoptions);
                        if (o.has("donotprompt") && "Y".equalsIgnoreCase(o.getString("donotprompt"))) {
                            html.append("<span id='cmt_checkoutoptions'>").append(this.getTranslationProcessor().translate("Check Out Options")).append("</span>");
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    html.append("<script>");
                    html.append("var __userchangelogoptions = ").append(changelogoptions).append(";");
                    html.append("</script>");
                } else {
                    html.append("<script>");
                    html.append("var __userchangelogoptions = {};");
                    html.append("</script>");
                }
                if (this.devMode != null) {
                    StringBuffer dm = new StringBuffer();
                    boolean l = false;
                    if (this.devMode.getIsSuspended()) {
                        dm.append("<div ondblclick=\"sapphire.connection.toggleDevMode(event)\" class=\"sb_dm_off\" title=\"Double click to toggle\">");
                        dm.append(this.clientBrowser.isMobile() && !this.clientBrowser.isTablet() ? "DM Sus" : "Dev Mode Suspended").append(this.devMode.getCompDevCodes().size() > 0 ? " (C)" : "");
                        dm.append("</div>");
                        l = true;
                    } else if (this.devMode.getIsDevMode()) {
                        dm.append("<div href=\"#\" ondblclick=\"sapphire.connection.toggleDevMode(event)\" class=\"sb_dm_on\" title=\"Double click to toggle\">");
                        dm.append(this.clientBrowser.isMobile() && !this.clientBrowser.isTablet() ? "DM On" : "Dev Mode On").append(this.devMode.getCompDevCodes().size() > 0 ? " (C)" : "");
                        dm.append("</div>");
                        l = true;
                    } else if (this.devMode.getCompDevCodes().size() > 0) {
                        dm.append("<div href=\"#\" ondblclick=\"sapphire.connection.toggleDevMode(event)\" class=\"").append(this.devMode.getCompCode().length() > 0 ? "sb_dm_on" : "sb_dm_off").append("\" title=\"Double click to toggle\">");
                        dm.append(this.clientBrowser.isMobile() && !this.clientBrowser.isTablet() ? "CM On" : "Component Mode On").append(this.devMode.getCompCode().length() > 0 ? " <strong>" + this.devMode.getCompCode() + "</strong> Enabled" : "Suspended").append(this.devMode.getHasDevRecord() ? " (D)" : "");
                        dm.append("</div>");
                        l = true;
                    } else if (this.devMode.getIsImplMode()) {
                        dm.append("<div ondblclick=\"sapphire.connection.toggleDevMode(event)\" class=\"sb_dm_off\" title=\"Double click to toggle\">");
                        dm.append(this.clientBrowser.isMobile() && !this.clientBrowser.isTablet() ? "IM On" : "Implementation Mode");
                        dm.append("</div>");
                        l = true;
                    }
                    if (this.devMode.getIsHiddenMode()) {
                        if (l) {
                            dm.append(",&nbsp;");
                        }
                        String click = this.roles.contains("View Hidden") || this.isDevMode() ? " ondblclick=\"sapphire.connection.toggleHiddenRecords()\" title=\"Double click to toggle\" " : " ";
                        dm.append("<div").append(click).append("class=\"sb_dm_off\">");
                        dm.append(this.getTranslationProcessor().translate("Showing All Records"));
                        dm.append("</div>");
                    }
                    if (dm.length() > 0) {
                        html.append("<div id=\"layout_devmodearea\">");
                        html.append(dm);
                        html.append("</div>");
                    }
                }
                html.append(HttpUtil.getConnectionInfo(this.plPageinfo, sb, false, this.tp, this.pageContext));
                html.append("</div>");
                html.append("</div>");
            }
            return html.toString();
        }
        return "";
    }

    public String getNavigationBar() {
        StringBuffer html = new StringBuffer();
        if (this.showNavigationBar()) {
            PropertyList nb = this.guiPolicy != null && this.guiPolicy.getPropertyList("navigationbar") != null ? this.guiPolicy.getPropertyList("navigationbar") : new PropertyList();
            String bcolor = nb.getProperty("color", "#FFFFFF");
            String fcolor = nb.getProperty("fontcolor", "#0970CE");
            html.append("<div id=\"layout_navbar\" class=\"layout_hidden\" style=\"height:").append(29).append("px;line-height:").append(29).append("px;").append(this.hasGizmoGroup() ? "margin-" + (this.getConnectionInfo().isRtl() ? "right" : "left") + ":" + (this.getSidebarDocked() ? this.getSidebarWidth() - 5 + this.getSidebarTogWidth() : this.getSidebarTogWidth()) + "px;" : "").append("\">");
            html.append("<div id=\"layout_navbar_content\"").append(">");
            html.append("</div>");
            if (this.getNavBarMode() == NavigationBarMode.USERDEFINED) {
                StringBuffer temp = new StringBuffer();
                temp.append("<div class='dashboardcogmenu_div'>");
                Image image = new Image(this.pageContext);
                image.setImageId("FlatBlackCog");
                image.setTitle("Toggle Manage Navigation Bar");
                image.setDimensions(16, 16);
                temp.append(image.getHtml());
                temp.append("</div>");
                MenuGizmo menuGizmo = new MenuGizmo();
                menuGizmo.init();
                menuGizmo.setPageContext(this.pageContext);
                PropertyList menuProps = new PropertyList();
                menuProps.setProperty("renderincludes", "N");
                menuProps.setProperty("customhtml", temp.toString());
                PropertyListCollection cogmenu = new PropertyListCollection();
                PropertyList cogitem = new PropertyList();
                cogitem.setProperty("text", "Related Items");
                cogitem.setProperty("link", "javascript:modernLayout.toggleNavBar(true)");
                cogmenu.add(cogitem);
                cogitem = new PropertyList();
                cogitem.setProperty("text", "Recent History");
                cogitem.setProperty("link", "javascript:modernLayout.toggleNavBar(false)");
                cogmenu.add(cogitem);
                menuProps.setProperty("custommenu", cogmenu);
                menuProps.setProperty("customy", this.clientBrowser.isIE() ? "-65" : "25");
                menuProps.setProperty("customx", "-55");
                menuProps.setProperty("menutype", "custom");
                menuProps.setProperty("click", "Y");
                menuProps.setProperty("mouseover", "N");
                menuGizmo.setElementProperties(menuProps);
                menuGizmo.setElementid("navbarcogmenu");
                html.append("<div id=\"layout_navbar_toggle\">");
                html.append(menuGizmo.getHtml());
                html.append("<script>" + menuGizmo.getScript() + "</script>");
                html.append("</div>");
            }
            html.append("</div>");
        }
        return html.toString();
    }

    public static String getLoaderImage(ConfigurationProcessor cp) {
        String image = LOADER;
        try {
            image = ModernLayout.getLoaderImage(cp.getPolicy(GUI_POLICY, "Sapphire Custom"));
        }
        catch (Exception e) {
            image = LOADER;
        }
        return image;
    }

    public static String getLoaderImage(PropertyList guiPolicy) {
        String image = LOADER;
        try {
            if (guiPolicy != null && guiPolicy.getPropertyList("loadingpanel") != null && !(image = guiPolicy.getPropertyList("loadingpanel").getProperty("image", LOADER)).endsWith(".gif") && image.endsWith(".svg")) {
                image = LOADER;
            }
        }
        catch (Exception e) {
            image = LOADER;
        }
        return image;
    }

    public String getCustomStyle() {
        StringBuffer html = new StringBuffer();
        html.append("#layout_header{");
        boolean submenu = false;
        if (submenu) {
            html.append("height:73px;");
        } else {
            html.append("height:53px;");
        }
        html.append("}");
        html.append("#layout_content_row{");
        int top = 53;
        if (this.clientBrowser.isEmbedded()) {
            top = 0;
        } else {
            if (this.getStatusBarPosition() == StatusBarPosition.TOP) {
                top += 20;
            }
            if (this.showNavigationBar()) {
                top += 31;
            }
        }
        html.append("top:").append(top).append("px;");
        if (this.getStatusBarPosition() == StatusBarPosition.BOTTOM) {
            html.append("bottom:").append(20).append("px;");
        }
        html.append("}");
        html.append("#ws_sidebar_tog{");
        html.append("background-image: url('WEB-CORE/imageref/flat/16/flat_black_chevron2_").append(this.getConnectionInfo().isRtl() ? "left" : "right").append(".svg');");
        html.append("}");
        html.append(".header_back{");
        html.append(this.getHeaderBackStyle());
        html.append("}");
        html.append(".ws_sidebar{");
        html.append("width: ").append(this.sidebarWidth).append("px;");
        html.append("}");
        html.append(".ui-resizable-").append(this.getConnectionInfo().isRtl() ? "w" : "e").append("{");
        html.append("z-index:").append(1).append(" !important;");
        html.append("width:").append(this.getSidebarTogWidth()).append("px !important;");
        html.append("}");
        html.append("#ws_sidebar_tog{");
        html.append(this.getSidebarDivStyle());
        html.append("width:").append(this.getSidebarTogWidth()).append("px;");
        html.append("}");
        html.append("#ws_sidebar_div{");
        html.append(this.getSidebarDivStyle());
        html.append("}");
        html.append(".standards .ws_pagecontent{");
        html.append("padding-bottom:").append(!this.clientBrowser.isEmbedded() && this.getStatusBarPosition() == StatusBarPosition.BOTTOM ? "20px" : "0");
        html.append(";");
        html.append("}");
        return html.toString();
    }

    private TranslationProcessor getTranslationProcessor() {
        if (this.tp == null) {
            this.tp = new TranslationProcessor(this.pageContext);
        }
        return this.tp;
    }

    private PropertyListCollection getProfileMenu() {
        boolean hasEvergreenModule;
        PropertyList p;
        boolean hasWap;
        PropertyListCollection out = new PropertyListCollection();
        String moduleList = ";" + this.requestContext.getProperty("modulelist") + ";";
        boolean bl = hasWap = moduleList.indexOf("WAP") >= 0;
        if ((this.guiPolicy == null || !this.guiPolicy.getProperty("hideviewcalendar", "N").equalsIgnoreCase("Y")) && hasWap) {
            p = new PropertyList();
            p.setProperty("link", "javascript:modernLayout.manageCalendar()");
            p.setProperty("text", this.getTranslationProcessor().translate("View My Calendar"));
            out.add(p);
        }
        if ((this.guiPolicy == null || !this.guiPolicy.getProperty("hideviewwork", "N").equalsIgnoreCase("Y")) && hasWap) {
            p = new PropertyList();
            p.setProperty("link", "javascript:modernLayout.manageWork()");
            p.setProperty("text", this.getTranslationProcessor().translate("View My Work"));
            out.add(p);
        }
        if (this.guiPolicy == null || !this.guiPolicy.getProperty("hidechangeprofilepic", "N").equalsIgnoreCase("Y")) {
            p = new PropertyList();
            p.setProperty("link", "javascript:modernLayout.changeProfilePic()");
            p.setProperty("text", this.getTranslationProcessor().translate("Change Profile Picture"));
            out.add(p);
        }
        if (this.guiPolicy == null || !this.guiPolicy.getProperty("hidechangepref", "N").equalsIgnoreCase("Y")) {
            p = new PropertyList();
            p.setProperty("link", "javascript:sapphire.lookup.open('rc?command=page&page=LV_UserPreferences&sdcid=User&keyid1=" + this.getConnectionInfo().getSysuserId() + "','User Preferences','',true,true)");
            p.setProperty("text", this.getTranslationProcessor().translate("Change Preferences"));
            out.add(p);
        }
        if (!this.guiPolicy.getProperty("hidechangepassword").equalsIgnoreCase("Y")) {
            p = new PropertyList();
            p.setProperty("link", "javascript:sapphire.lookup.open('rc?command=operation&operation=ChangePassword&operationclass=com.labvantage.sapphire.servlet.command.ChangePassword&nexturl=[close]&date=' + new Date(),'Change Password','width=460,height=625',true,true)");
            p.setProperty("text", this.getTranslationProcessor().translate("Change Password"));
            out.add(p);
            if ("Y".equals(this.pageContext.getSession().getAttribute("allowReset2FA"))) {
                p = new PropertyList();
                p.setProperty("link", "javascript:sapphire.lookup.open('rc?command=operation&operation=ChangePassword&operationclass=com.labvantage.sapphire.servlet.command.ChangePassword&mode=reset2fa&date=' + new Date(),'Reset 2FA','width=460,height=225',true,true)");
                p.setProperty("text", this.getTranslationProcessor().translate("Reset 2FA"));
                out.add(p);
            }
        }
        String startuptype = "user";
        try {
            startuptype = this.cp.getProfileProperty(this.getConnectionInfo().getSysuserId(), "startuptype", startuptype);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (!(startuptype.equalsIgnoreCase("fixed") || startuptype.equalsIgnoreCase("last") || this.guiPolicy != null && this.guiPolicy.getProperty("hidesetasstart", "N").equalsIgnoreCase("Y"))) {
            p = new PropertyList();
            p.setProperty("link", "javascript:modernLayout.setAsStartPage();");
            p.setProperty("text", this.getTranslationProcessor().translate("Set as Start Page"));
            out.add(p);
        }
        if (!(this.guiPolicy == null || this.guiPolicy.getProperty("lockgroups", "N").equalsIgnoreCase("Y") || this.guiPolicy != null && this.guiPolicy.getProperty("hideaddtosidebar", "N").equalsIgnoreCase("Y"))) {
            p = new PropertyList();
            p.setProperty("id", "AddToSidebar");
            p.setProperty("link", "javascript:modernLayout.addPageToGroup();");
            p.setProperty("text", this.getTranslationProcessor().translate("Add Page to Sidebar"));
            out.add(p);
        }
        if (this.devMode.getIsSuspended() || this.devMode.getIsDevMode() || this.devMode.getCompDevCodes().size() > 0) {
            p = new PropertyList();
            p.setProperty("link", "javascript:sapphire.connection.toggleDevMode(event)");
            p.setProperty("text", "Toggle Dev Mode");
            out.add(p);
        }
        if (this.roles.contains("View Hidden") || this.isDevMode()) {
            p = new PropertyList();
            p.setProperty("link", "javascript:sapphire.connection.toggleHiddenRecords(event)");
            p.setProperty("text", this.devMode.isHiddenMode ? "Hide Hidden Records" : "Show Hidden Records");
            out.add(p);
        }
        boolean bl2 = hasEvergreenModule = !this.clientBrowser.isMobile() && (moduleList.indexOf("WPDPro") >= 0 || moduleList.indexOf("WPDStd") >= 0);
        if (this.devMode.getIsDevMode() || this.devMode.getIsSuspended() || hasEvergreenModule) {
            p = new PropertyList();
            p.setId("_editpage");
            p.setProperty("id", "EditPage");
            p.setProperty("link", "javascript:modernLayout.doEditClick();");
            p.setProperty("text", this.tp.translate("Edit Page"));
            out.add(p);
        }
        if (this.guiPolicy != null && !this.guiPolicy.getProperty("hidebuildinfo", "N").equalsIgnoreCase("Y")) {
            p = new PropertyList();
            p.setProperty("id", "About");
            p.setProperty("link", "javascript:sapphire.ui.dialog.show( 'About', null, true, 'rc?command=page&page=LV_BuildInfo' )");
            p.setProperty("text", this.getTranslationProcessor().translate("About"));
            out.add(p);
        }
        if (this.guiPolicy == null || !this.guiPolicy.getProperty("hidelogoff", "N").equalsIgnoreCase("Y")) {
            p = new PropertyList();
            p.setProperty("link", "javascript:sapphire.connection.logOff()");
            boolean hk = this.guiPolicy != null ? !this.guiPolicy.getProperty("hotkeylogoff", "Y").equalsIgnoreCase("N") : true;
            p.setProperty("text", this.getTranslationProcessor().translate("Log Off") + (hk ? " (Ctrl+Q)" : ""));
            out.add(p);
        }
        return out;
    }

    public static String getCompanyLogo(String logoSrc, String logoClick) {
        StringBuffer html = new StringBuffer();
        html.append("<div align=\"left\" class=\"ws_inline ws_smalldevice ws_companylogo\" title=\"Go Home\"").append(logoClick != null && logoClick.length() > 0 ? " onclick=\"" + logoClick + "\"" : "").append(">");
        if (logoSrc == null || logoSrc.length() == 0) {
            logoSrc = LOGO;
        }
        String appImage = "WEB-OPAL/layouts/images/logo_labvantage.png";
        html.append("<img alt=\"LV Logo\" title=\"");
        html.append(TITLE).append(" ").append(Build.getVersion()).append(" Build ").append(Build.getBuild()).append("_").append(Build.getPatch()).append(" (").append(Build.getReleaseVersion()).append(") ").append(Build.getBuildDate()).append("\" src=\"").append(logoSrc).append("\">\n");
        html.append("</div>");
        return html.toString();
    }

    public String getHead() {
        StringBuffer html = new StringBuffer();
        html.append("<table cellpadding=0 cellspacing=0 border=0 style=\"width:100%;height:").append(50).append("px;\">");
        html.append("<tbody>");
        html.append("<tr>");
        boolean submenu = false;
        html.append("<td class=\"layout_mainheader ws_center\" style=\"width:auto;\">");
        html.append("<div class=\"layout_head_in\">");
        html.append(ModernLayout.getCompanyLogo(this.guiPolicy.getProperty("applicationimage", ""), "modernLayout.navigation.gotoStart(true, true)"));
        String currentMenu = this.getMenuGizmo();
        html.append(MenuGizmo.getStartStyleAndScript(this.connectionInfo.isRtl(), this.connectionInfo.getUseFullIncludes()));
        html.append(MenuGizmo.renderNavigationDiv("topmenu", "&nbsp;"));
        if (currentMenu.length() > 0) {
            html.append("<script>");
            html.append("modernLayout.startMenu = '").append(this.getStartMenu()).append("';");
            html.append("sapphire.events.attachEvent(window,'onload',function(){modernLayout.setMenu('").append(currentMenu).append("')});");
            html.append("</script>");
        }
        String className = "upa_none";
        String borderImage = "profile_none";
        StringBuffer icon = new StringBuffer();
        html.append("<div id=\"__userprofilearea\">");
        String text = this.requestContext.getProperty("sysuserinitals");
        if (text.length() == 0 || text.length() > 3) {
            String name = this.requestContext.getProperty("sysuserdesc");
            if (name.length() == 0) {
                name = this.requestContext.getProperty("sysuserid");
            }
            text = ModernLayout.generateInitials(name);
        } else {
            text = text.length() > 3 ? text.substring(0, 3) : text;
        }
        String textstyle = "";
        if (text.length() > 2) {
            textstyle = "font-size:10pt;margin-left:-5px;";
        } else if (text.length() == 1) {
            textstyle = "font-size:18pt;margin-left:-3px;";
        }
        StringBuffer temp = new StringBuffer();
        temp.append("<div class=\"layout_upa\">");
        temp.append("<div class=\"").append(className.length() > 0 ? "" + className + "" : "").append("\">");
        temp.append("<img src=\"").append(this.profilePicturePath.length() > 0 ? this.profilePicturePath : "WEB-CORE/images/profiles/default46.png").append("\" style=\"").append(this.profilePicturePath.length() > 0 ? "" : "display:none;").append("width:46px;\">");
        temp.append("<span style=\"").append(this.profilePicturePath.length() > 0 ? "display:none;" : "").append("width:46px;").append(textstyle).append("\">").append(text).append("</span>");
        temp.append("</div>");
        MenuGizmo menuGizmo = new MenuGizmo();
        menuGizmo.setPageContext(this.pageContext);
        PropertyList menuProps = new PropertyList();
        menuProps.setProperty("renderincludes", "N");
        menuProps.setProperty("customhtml", temp.toString());
        menuProps.setProperty("custommenu", this.getProfileMenu());
        menuProps.setProperty("customclass", "menugizmo_profilemenu menugizmo_nav");
        menuProps.setProperty("menutype", "custom");
        menuProps.setProperty("onshow", "modernLayout.onShowProfileMenu");
        if (this.guiPolicy != null && this.guiPolicy.getProperty("menumode").equalsIgnoreCase("mouseover")) {
            menuProps.setProperty("click", "Y");
            menuProps.setProperty("mouseover", "Y");
        } else {
            menuProps.setProperty("click", "Y");
            menuProps.setProperty("mouseover", "N");
        }
        menuGizmo.setElementProperties(menuProps);
        menuGizmo.setElementid("profilemenu");
        menuGizmo.init();
        html.append(menuGizmo.getHtml());
        html.append("<script>" + menuGizmo.getScript() + "</script>");
        html.append("</div>");
        html.append("</div>");
        html.append(icon);
        html.append("<div class=\"link_btns_cont\">");
        GroupGizmo groupGizmo = GizmoTargetAjaxManager.renderGizmoGroup(null, "", GizmoTargetAjaxManager.GizmoType.TOPBAR, null, this.guiPolicy, this.userconfig, this.pageContext);
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/groupgizmo.js\"></script>");
        html.append(GroupGizmo.getContainer("", "ws_sortable_top", "", ""));
        html.append("</div>");
        html.append("</div>");
        html.append("<script>");
        html.append("sapphire.events.attachEvent(window,'onload',function(){modernLayout.refreshTopbar()});");
        html.append("</script>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</tbody>");
        html.append("</table>");
        return html.toString();
    }

    public static String generateInitials(String name) {
        try {
            if (name.equalsIgnoreCase("(system)")) {
                return "SYS";
            }
            String[] textbits = StringUtil.split(name, " ", true);
            name = textbits.length > 1 ? (textbits[0].substring(0, 1) + textbits[textbits.length - 1].substring(0, 1)).toUpperCase() : (textbits[0].length() < 3 ? textbits[0].toUpperCase() : textbits[0].substring(0, 1).toUpperCase());
            return name;
        }
        catch (Exception e) {
            return "X";
        }
    }

    public String getSidebar() {
        StringBuffer html = new StringBuffer();
        if (this.hasGizmoGroup() && this.showSidebar) {
            html.append("<div id=\"ws_sidebarcontrols\">");
            String gg = this.getGizmoGroup();
            StringBuffer buffer = new StringBuffer();
            StringBuffer script = new StringBuffer();
            this.gizmogroupLocked = true;
            if (this.pageContext != null) {
                this.pageContext.getSession().setAttribute("_sidebarcache_last", (Object)gg);
            }
            html.append("<div id=\"ws_sidebartext\">");
            html.append("</div>");
            StringBuffer temp = new StringBuffer();
            temp.append("<div>");
            Image image = new Image(this.pageContext);
            image.setImageId("FlatBlackCog");
            image.setTitle(this.tp.translate("Toggle Manage Sidebar"));
            image.setDimensions(16, 16);
            temp.append(image.getHtml());
            temp.append("</div>");
            MenuGizmo menuGizmo = new MenuGizmo();
            menuGizmo.setPageContext(this.pageContext);
            PropertyList menuProps = new PropertyList();
            menuProps.setProperty("renderincludes", "N");
            menuProps.setProperty("customhtml", temp.toString());
            PropertyListCollection cogmenu = new PropertyListCollection();
            boolean sep = false;
            ConnectionInfo connectionInfo = this.getConnectionInfo();
            List<String> roles = Arrays.asList(StringUtil.split(connectionInfo.getRoleList(), ";"));
            String role = this.guiPolicy != null ? this.guiPolicy.getProperty("groupadminrole", "") : "";
            boolean sidebarhasCreaterole = role.length() == 0 || roles.contains(role);
            String user = connectionInfo.getSysuserId();
            boolean sidebarAdminuser = false;
            boolean sidebarlocked = true;
            GroupGizmo.populateCogMenu(menuProps, gg, false, BaseGizmo.GizmoLocation.SIDEBAR, new PropertyList(), this.guiPolicy, sidebarlocked, sidebarAdminuser, false, sidebarhasCreaterole, this.pageContext);
            menuProps.setProperty("customy", "");
            menuProps.setProperty("customx", "-90");
            menuProps.setProperty("menutype", "custom");
            menuProps.setProperty("click", "Y");
            menuProps.setProperty("mouseover", "N");
            menuProps.setProperty("onshow", "modernLayout.onShowSidebarCogMenu");
            menuGizmo.setElementProperties(menuProps);
            menuGizmo.setElementid("sidebarcogmenu");
            menuGizmo.init();
            html.append(menuGizmo.getHtml());
            html.append("<script>" + menuGizmo.getScript() + "</script>");
            Image i = new Image(this.pageContext);
            i.setImageId("FlatBlackDoubleChevron" + (this.getConnectionInfo().isRtl() ? "Right" : "Left"));
            i.setTitle(this.tp.translate("Collapse Sidebar"));
            html.append("<div class=\"sidebar_minimize_btn\" onclick=\"modernLayout.sidebar.undock()\">");
            html.append(i.getHtml());
            html.append("</div>");
            html.append("</div>");
            html.append(GroupGizmo.getContainer("", "ws_sortable_side", "ws_sortable", ""));
            html.append("<div class=\"ws_gizmotarget_side").append("\">&nbsp;</div>");
            html.append("<script>");
            html.append("modernLayout.startGroup = '").append(this.getStartGizmoGroup()).append("';");
            html.append("sapphire.events.attachEvent(window,'onload',function(){modernLayout.setGroup('").append(gg).append("')});");
            html.append("</script>");
        }
        return html.toString();
    }

    public StatusBarPosition getStatusBarPosition() {
        StatusBarPosition out = StatusBarPosition.BOTTOM;
        if (this.guiPolicy != null && this.guiPolicy.getPropertyList("statusbar") != null) {
            String s = this.guiPolicy.getPropertyList("statusbar").getProperty("show", out.toString());
            if (s.equalsIgnoreCase("no")) {
                s = StatusBarPosition.HIDDEN.toString();
            }
            try {
                out = StatusBarPosition.valueOf(s.toUpperCase());
            }
            catch (Exception e) {
                out = StatusBarPosition.BOTTOM;
            }
        }
        return out;
    }

    public boolean showNavigationBar() {
        boolean out = true;
        if (!this.clientBrowser.isEmbedded() && this.guiPolicy != null && this.guiPolicy.getPropertyList("navigationbar") != null) {
            String s = this.guiPolicy.getPropertyList("navigationbar").getProperty("show", "");
            out = s.length() > 0 ? !s.equalsIgnoreCase("N") : this.clientBrowser.getGUIMode().getNavigationBar();
        }
        return out;
    }

    public String getHeaderBackStyle() {
        StringBuffer style1 = new StringBuffer();
        style1.append("height: ").append(53).append("px;");
        if (this.getStatusBarPosition() == StatusBarPosition.TOP) {
            style1.append("top: 20px;");
        }
        return style1.toString();
    }

    public String getSidebarDivStyle() {
        StringBuffer style1 = new StringBuffer();
        boolean submenu = false;
        style1.append("top:").append(this.getStatusBarPosition() == StatusBarPosition.TOP ? "73" : "53").append("px;");
        style1.append(this.getConnectionInfo().isRtl() ? "right: " : "left: ").append(0).append(";");
        style1.append("bottom:").append(this.getStatusBarPosition() == StatusBarPosition.BOTTOM ? "20" : "0").append("px;");
        return style1.toString();
    }

    public static String getNavForm(HttpServletRequest request, String _nav, String sTarget) {
        StringBuffer html = new StringBuffer();
        html.append("<form id=\"_nav_form\" style=\"display:none;\" target=\"" + sTarget + "\" method=\"POST\" action=\"rc\">");
        try {
            for (Object key : request.getParameterMap().keySet()) {
                if (request.getParameter(key.toString()) == null || !(request.getParameter(key.toString()) instanceof String) || key.toString().equalsIgnoreCase("_nav") || key.toString().equalsIgnoreCase("bopassword")) continue;
                String v = request.getParameter(key.toString()).toString();
                if (v.contains("\"") || v.startsWith("{") && v.endsWith("}")) {
                    html.append("<textarea style=\"display:none;\" type=\"hidden\" name=\"").append(SafeHTML.encodeForHTMLAttribute("" + key)).append("\">").append(SafeHTML.encodeForHTML(v)).append("</textarea>");
                    continue;
                }
                if ("querywhere".equals(key) || "restrictivewhere".equals(key)) {
                    v = EncryptDecrypt.obfsql(v);
                }
                html.append("<input type=\"hidden\" name=\"").append(SafeHTML.encodeForHTMLAttribute("" + key)).append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(v)).append("\">");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        html.append("<input type=\"hidden\" name=\"").append("csrftoken").append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute((String)request.getSession().getAttribute("csrftoken"))).append("\">");
        html.append("<input type=\"hidden\" name=\"").append("_nav").append("\" value=\"").append(SafeHTML.encodeForHTMLAttribute(_nav)).append("\">");
        html.append("<input type=\"hidden\" name=\"").append("_viewport").append("\" value=\"").append("").append("\">");
        html.append("</form>");
        return html.toString();
    }

    public String getNavScript() {
        StringBuffer html = new StringBuffer();
        boolean doubleBuffer = true;
        if (this.guiPolicy != null && this.guiPolicy.getProperty("doublebuffer", "Y").equalsIgnoreCase("N")) {
            doubleBuffer = false;
        }
        html.append("<script type=\"text/javascript\">");
        html.append("if (window!=_lvtop){");
        html.append("_nav_form._nav.value = 'Y';");
        html.append("_nav_form.target = '_self';");
        html.append("}else{");
        html.append("if (top == _lvtop){");
        html.append("modernLayout.showLayout();");
        html.append("}");
        html.append("}");
        html.append("modernLayout.navigation.doubleBuffer = ").append(doubleBuffer).append(";");
        if (!doubleBuffer) {
            html.append("sapphire.page.setTop(_nav_frame1.contentWindow);");
        }
        if (this.clientBrowser.getOS() == 13 || this.clientBrowser.getOS() == 6 || this.clientBrowser.getOS() == 14) {
            html.append("sapphire.events.registerLoadListener(modernLayout.navigation.submitNavForm)");
        } else {
            html.append("modernLayout.navigation.submitNavForm();");
        }
        html.append("</script>");
        return html.toString();
    }

    public String getNavForm(HttpServletRequest request) {
        StringBuffer html = new StringBuffer();
        html.append(ModernLayout.getNavForm(request, "Y", "_nav_frame1"));
        html.append("<iframe class=\"nav_frame\" id=\"_nav_frame1\" frameborder=\"0\" name=\"_nav_frame1\"></iframe>");
        boolean doubleBuffer = true;
        if (this.guiPolicy != null && this.guiPolicy.getProperty("doublebuffer", "Y").equalsIgnoreCase("N")) {
            doubleBuffer = false;
        }
        if (doubleBuffer) {
            html.append("<iframe class=\"nav_frame_db\" id=\"_nav_frame2\" frameborder=\"0\" name=\"_nav_frame2\"></iframe>");
        }
        return html.toString();
    }

    private NavigationBarMode getNavBarMode() {
        if (this.guiPolicy.getPropertyList("navigationbar") != null && this.guiPolicy.getPropertyList("navigationbar").getProperty("mode").length() > 0) {
            NavigationBarMode navigationBarMode = NavigationBarMode.getNavigationBarMode(this.guiPolicy.getPropertyList("navigationbar").getProperty("mode"));
            return navigationBarMode;
        }
        NavigationBarMode navigationBarMode = NavigationBarMode.getNavigationBarMode(this.clientBrowser.getGUIMode().getNavigationBarMode());
        return navigationBarMode;
    }

    private String getNavigationBarMode() {
        NavigationBarMode navigationBarMode = this.getNavBarMode();
        if (navigationBarMode == NavigationBarMode.USERDEFINED) {
            if (this.userconfig.getProperty("__modern__navbar_relateditems").equalsIgnoreCase("Y")) {
                return NavigationBarMode.RELATEDITEMS.toString();
            }
            return NavigationBarMode.RECENTHISTORY.toString();
        }
        return navigationBarMode.toString();
    }

    public static JSONObject getSelectedWS(RequestContext requestContext) {
        String tborg = requestContext.getProperty("_selectedws");
        JSONObject temp = new JSONObject();
        try {
            String tdec = sapphire.util.HttpUtil.decodeURIComponent(tborg);
            String trot = EncryptDecrypt.rot13(tdec);
            String _selectedwscrc = requestContext.getProperty("_selectedwscrc");
            if (_selectedwscrc == null || _selectedwscrc.length() == 0) {
                throw new Exception("No CRC provided");
            }
            CRC32 crccheck = new CRC32();
            crccheck.update(trot.getBytes());
            long crc = Long.parseLong(requestContext.getProperty("_selectedwscrc"));
            if (crc != crccheck.getValue()) {
                throw new Exception("Validation failed");
            }
            temp = new JSONObject(trot);
        }
        catch (Exception e) {
            Logger.logError("Failed to decode selected ws", e);
        }
        return temp;
    }

    public String getScript(HttpSession session) {
        String windowid;
        StringBuffer html = new StringBuffer();
        html.append("if (window == _lvtop){");
        html.append("modernLayout.rtl = ").append(this.getConnectionInfo().isRtl()).append(";");
        int navMax = -1;
        try {
            navMax = this.clientBrowser.isChrome() ? Integer.parseInt(this.guiPolicy.getProperty("chromenavigationrefresh", "-1")) : -1;
        }
        catch (Exception exception) {
            // empty catch block
        }
        html.append("modernLayout.navigation.navMax = ").append(navMax).append(";");
        html.append("modernLayout.groupGizmo = '").append(this.getGizmoGroup()).append("';");
        html.append("modernLayout.root = '';");
        html.append("modernLayout.sidebar.docked = ").append(this.showSidebar ? this.sidebarDocked : false).append(";");
        html.append("modernLayout.sidebar.showing = ").append(this.showSidebar ? this.sidebarDocked : false).append(";");
        html.append("modernLayout.sidebar.width = ").append(this.showSidebar ? this.sidebarWidth : 0).append(";");
        html.append("modernLayout.sidebar.togwidth = ").append(this.getSidebarTogWidth()).append(";");
        html.append("modernLayout.sidebar.padwidth = ").append(2).append(";");
        html.append("modernLayout.navigation.navbarmode = '").append(this.getNavigationBarMode()).append("';");
        html.append("modernLayout.hkLogOff = ").append(this.guiPolicy != null ? !this.guiPolicy.getProperty("hotkeylogoff", "Y").equalsIgnoreCase("N") : true).append(";");
        html.append("modernLayout.hkLogOffConfirm = ").append(this.guiPolicy != null ? this.guiPolicy.getProperty("hotkeylogoffconfirm", "N").equalsIgnoreCase("Y") : false).append(";");
        html.append("modernLayout.texts.fetchingMenu = '").append(this.tp.translate("Fetching Menu")).append("';");
        html.append("modernLayout.texts.moreMenu = '").append(this.tp.translate("More")).append("';");
        html.append("modernLayout.texts.menu = '").append(this.tp.translate("Menu")).append("';");
        html.append("modernLayout.texts.editGizmo = '").append(this.tp.translate("Edit Gizmo")).append("';");
        html.append("modernLayout.texts.editGroup = '").append(this.tp.translate("Edit Group")).append("';");
        html.append("modernLayout.texts.editPage = '").append(this.tp.translate("Edit Page")).append("';");
        html.append("modernLayout.texts.editMenu = '").append(this.tp.translate("Edit Menu")).append("';");
        int showtime = 2000;
        if (this.guiPolicy != null && this.guiPolicy.getPropertyList("loadingpanel") != null) {
            try {
                showtime = Integer.parseInt(this.guiPolicy.getPropertyList("loadingpanel").getProperty("showtime", "2000"));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        html.append("modernLayout.loader.time = ").append(showtime).append(";");
        if (this.requestContext.getProperty("_selectedws").length() > 0) {
            try {
                JSONObject temp = ModernLayout.getSelectedWS(this.requestContext);
                html.append("modernLayout.navigation.restoreSelection(").append(temp.toString()).append(");");
            }
            catch (Exception temp) {
                // empty catch block
            }
        }
        if ((windowid = this.requestContext.getProperty("_wid")).length() > 0) {
            String state;
            StringBuffer cache = new StringBuffer();
            Object o = session.getAttribute(SESSION_STATECACHE + windowid);
            if (o == null) {
                o = session.getAttribute("_statecache__last");
            }
            if (o != null && (state = o.toString()).length() > 0) {
                try {
                    JSONObject js = new JSONObject(state);
                    cache.append("modernLayout.navigation.restoreState(").append(state).append(");");
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (cache.length() > 0) {
                html.append(cache);
            }
        }
        html.append("}");
        return html.toString();
    }

    public static enum NavigationBarMode {
        RECENTHISTORY("Recent History"),
        RELATEDITEMS("Related Items"),
        TITLEONLY("Title Only"),
        USERDEFINED("User Defined");

        String name;

        private NavigationBarMode(String name) {
            this.name = name;
        }

        public static NavigationBarMode getNavigationBarMode(String name) {
            NavigationBarMode out = USERDEFINED;
            for (NavigationBarMode n : NavigationBarMode.values()) {
                if (!n.getName().equalsIgnoreCase(name)) continue;
                out = n;
                break;
            }
            return out;
        }

        public String getName() {
            return this.name;
        }
    }

    public static enum StatusBarPosition {
        TOP,
        HIDDEN,
        BOTTOM;

    }

    private class DevModeState {
        private boolean hasDevRecord = false;
        private boolean isDevMode = false;
        private boolean isSuspended = false;
        private boolean isImplMode = false;
        private boolean isHiddenMode = false;
        private ArrayList<String> compDevCodes = Configuration.getCompDevCodes();
        private String compcode = "";

        public boolean getHasDevRecord() {
            return this.hasDevRecord;
        }

        public boolean getIsDevMode() {
            return this.isDevMode;
        }

        public boolean getIsSuspended() {
            return this.isSuspended;
        }

        public boolean getIsImplMode() {
            return this.isImplMode;
        }

        public boolean getIsHiddenMode() {
            return this.isHiddenMode;
        }

        public ArrayList<String> getCompDevCodes() {
            return this.compDevCodes;
        }

        public String getCompCode() {
            return this.compcode;
        }

        DevModeState(com.labvantage.sapphire.admin.system.ConfigurationProcessor config) {
            try {
                if (config.getConnectionid().length() > 0) {
                    String sysuserid = ModernLayout.this.getConnectionInfo().getSysuserId();
                    QueryProcessor queryProcessor = new QueryProcessor(ModernLayout.this.pageContext);
                    this.hasDevRecord = queryProcessor.getSqlDataSet("SELECT propertyid, propertyvalue FROM sysconfig WHERE propertyid='devmode'").size() == 1;
                    String devMode = config.getSysConfigProperty("devmode", "N");
                    this.isDevMode = devMode.equalsIgnoreCase("Y");
                    this.isSuspended = devMode.equalsIgnoreCase("S");
                    this.isImplMode = "Y".equalsIgnoreCase(config.getSysConfigProperty("implmode", "N"));
                    this.isHiddenMode = "Y".equalsIgnoreCase(config.getProfileProperty(sysuserid, "viewhidden", "N"));
                    this.compcode = config.getSysConfigProperty("compcode");
                    if (!this.compDevCodes.contains(this.compcode)) {
                        this.compcode = "";
                        config.setSysConfigProperty("compcode", this.compcode);
                        Configuration.setCompcode(ModernLayout.this.getConnectionInfo().getDatabaseId(), this.compcode);
                    }
                }
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
        }
    }
}

