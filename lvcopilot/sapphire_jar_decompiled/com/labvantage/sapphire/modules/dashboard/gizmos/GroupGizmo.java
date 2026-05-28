/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.layouts.modern.GizmoTargetAjaxManager;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.ButtonGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.GroupPickerGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.ParameterGizmo;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GroupGizmo
extends sapphire.pageelements.BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String HTML_PROPERTY = "html";
    private StringBuffer script;
    private int offsetX = 0;
    private int offsetY = 0;
    private boolean childgroup = false;
    private boolean adminmode = false;
    private PropertyList guiPolicy = null;
    private boolean adminuser = false;
    private boolean locked = false;
    private boolean hasCreateRole = false;
    private boolean canMoveItems = false;
    private boolean canAddAndRemoveItems = false;
    protected boolean dynamicgroup = false;
    protected int pagenum = 1;
    protected int pagesize = 10;
    protected int totalpages = 0;

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.setTimeout(-1);
        try {
            this.guiPolicy = new ConfigurationProcessor(this.pageContext).getPolicy("GUIPolicy", "Sapphire Custom");
        }
        catch (Exception e) {
            this.guiPolicy = null;
        }
        SapphireConnection sc = this.getConnectionProcessor().getSapphireConnection();
        List<String> roles = Arrays.asList(StringUtil.split(sc.getRoleList(), ";"));
        String role = this.guiPolicy != null ? this.guiPolicy.getProperty("groupadminrole", LABVANTAGE_CVS_ID) : LABVANTAGE_CVS_ID;
        this.hasCreateRole = role.length() == 0 || roles.contains(role);
        PropertyList options = this.getElementProperties().getPropertyList("groupoptions");
        if (options == null) {
            options = new PropertyList();
            this.getElementProperties().setProperty("groupoptions", options);
        }
        String user = sc.getSysuserId();
        this.adminuser = options.getProperty("owner").equals(user) || roles.contains(options.getProperty("role"));
        boolean bl = this.locked = this.guiPolicy != null && this.guiPolicy.getProperty("lockgroups", "N").equalsIgnoreCase("Y") || this.getElementProperties().getProperty("lockgroup", "N").equalsIgnoreCase("Y");
        this.canMoveItems = this.adminuser ? !this.locked : !this.locked && options.getProperty("public", "N").equalsIgnoreCase("Y") && !options.getProperty("editinglevel").equalsIgnoreCase("View Only");
        this.canAddAndRemoveItems = false;
        this.canAddAndRemoveItems = this.adminuser ? !this.locked : !this.locked && options.getProperty("public", "N").equalsIgnoreCase("Y") && options.getProperty("editinglevel").equalsIgnoreCase("Fully Editable");
        return true;
    }

    public static String getContainer(String content, String containerid, String containerclass, String containerstyle) {
        StringBuffer html = new StringBuffer();
        html.append("<div class=\"ui-state-default").append(containerclass.length() > 0 ? " " + containerclass : LABVANTAGE_CVS_ID).append("\" style=\"").append(containerstyle.length() > 0 ? containerstyle : LABVANTAGE_CVS_ID).append("\" ").append(containerid.length() > 0 ? " id=\"" + containerid + "\"" : LABVANTAGE_CVS_ID).append(">");
        html.append(content);
        html.append("</div>");
        return html.toString();
    }

    private String getGizmos(PropertyListCollection gizmos, GizmoTargetAjaxManager.GizmoType gizmoType, BaseGizmo.GizmoStyle gizmoStyle, StringBuffer script, PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        if (gizmos != null && gizmos.size() > 0) {
            if (gizmoType == GizmoTargetAjaxManager.GizmoType.SIDEBAR_CONTENTONLY) {
                gizmoType = GizmoTargetAjaxManager.GizmoType.SIDEBAR;
            } else if (gizmoType == GizmoTargetAjaxManager.GizmoType.TOPBAR_CONTENTONLY) {
                gizmoType = GizmoTargetAjaxManager.GizmoType.TOPBAR;
            }
            for (int i = 0; i < gizmos.size(); ++i) {
                PropertyList p = gizmos.getPropertyList(i);
                String gizmotargetid = p.getProperty("gizmoid");
                String id = p.getProperty("id", p.getId());
                if (id.length() == 0) {
                    id = gizmotargetid;
                }
                if (gizmotargetid.equalsIgnoreCase(this.getGizmoDefId()) || gizmotargetid.length() <= 0 || id.length() <= 0 || !p.getProperty("show", "Y").equalsIgnoreCase("Y")) continue;
                PropertyList customprops = p.containsKey("gizmoprops") ? p.getPropertyList("gizmoprops") : null;
                String ptreeid = p.getProperty("ptreeid", LABVANTAGE_CVS_ID);
                String extendnodeid = p.getProperty("extendnodeid", LABVANTAGE_CVS_ID);
                if (customprops != null && ptreeid.length() > 0 && extendnodeid.length() > 0) {
                    sapphire.pageelements.BaseGizmo gizmo = BaseGizmo.getTypeInstance(pageContext, ptreeid, extendnodeid);
                    if (gizmo != null) {
                        String uid = StringUtil.replaceAll(id, " ", "_") + (gizmoType == GizmoTargetAjaxManager.GizmoType.SIDEBAR || gizmoType == GizmoTargetAjaxManager.GizmoType.SIDEBAR_CONTENTONLY ? "s_" : (gizmoType == GizmoTargetAjaxManager.GizmoType.TOPBAR || gizmoType == GizmoTargetAjaxManager.GizmoType.TOPBAR_CONTENTONLY ? "t_" : "_x")) + ((int)(Math.random() * 100.0) + 1);
                        gizmo.setElementid(uid);
                        gizmo.setGizmoDefId(gizmotargetid);
                        PropertyList gizmoprops = gizmo.getElementProperties();
                        try {
                            gizmoprops.setPropertyList(customprops.toXMLString(), true, true);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        html.append(GizmoTargetAjaxManager.renderGizmo(id, gizmo, script, gizmoType, gizmoStyle, this.getTranslationProcessor()));
                        continue;
                    }
                    this.logger.warn("Gizmo for propertytreeid " + ptreeid + " could not be created.");
                    continue;
                }
                html.append(GizmoTargetAjaxManager.getGizmo(id, gizmotargetid, script, gizmoStyle, gizmoType, this.getConnectionId(), pageContext));
            }
        }
        return html.toString();
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        this.script = new StringBuffer();
        if (this.element != null) {
            PropertyListCollection gizmosColl = this.element.getCollectionNotNull("gizmos");
            boolean layout = this.element.getProperty("layout", "N").equalsIgnoreCase("Y");
            if (layout) {
                GizmoTargetAjaxManager.GizmoType gizmoType = GizmoTargetAjaxManager.GizmoType.SIDEBAR;
                try {
                    gizmoType = GizmoTargetAjaxManager.GizmoType.valueOf(this.element.getProperty("gizmotype", GizmoTargetAjaxManager.GizmoType.SIDEBAR.toString()).toUpperCase());
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (gizmoType != GizmoTargetAjaxManager.GizmoType.SIDEBAR_CONTENTONLY && gizmoType != GizmoTargetAjaxManager.GizmoType.TOPBAR_CONTENTONLY) {
                    html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/groupgizmo.js\"></script>");
                }
                if (gizmoType == GizmoTargetAjaxManager.GizmoType.SIDEBAR || gizmoType == GizmoTargetAjaxManager.GizmoType.SIDEBAR_CONTENTONLY) {
                    boolean contentOnly;
                    BaseGizmo.GizmoStyle gizmoStyle = BaseGizmo.GizmoStyle.getGizmoStyle(this.element.getProperty("gizmostyle", BaseGizmo.GizmoStyle.LARGETEXT.text), true);
                    String temp = this.getGizmos(gizmosColl, gizmoType, gizmoStyle, this.script, this.pageContext);
                    boolean bl = contentOnly = gizmoType == GizmoTargetAjaxManager.GizmoType.SIDEBAR_CONTENTONLY || gizmoType == GizmoTargetAjaxManager.GizmoType.TOPBAR_CONTENTONLY;
                    if (!contentOnly) {
                        html.append(GroupGizmo.getContainer(temp, "ws_sortable_side", "ws_sortable", LABVANTAGE_CVS_ID));
                    } else {
                        html.append(temp);
                    }
                    if (gizmoType != GizmoTargetAjaxManager.GizmoType.SIDEBAR_CONTENTONLY) {
                        html.append("<div id=\"emptysidebar\" onclick=\"modernLayout.gizmos.add(true, true)\" style=\"display:none;").append("\">");
                        html.append(this.getTranslationProcessor().translate("Empty Sidebar")).append("<br>");
                        Image image = new Image(this.pageContext);
                        image.setImageId("FlatWhitePlus1");
                        image.setDimensions(32, 32);
                        html.append(image.getHtml());
                        html.append("<br>");
                        html.append(this.getTranslationProcessor().translate("Start Adding Gizmos"));
                        html.append("</div>");
                    }
                } else {
                    boolean contentOnly;
                    String temp = this.getGizmos(gizmosColl, GizmoTargetAjaxManager.GizmoType.TOPBAR, BaseGizmo.GizmoStyle.SMALL, this.script, this.pageContext);
                    boolean bl = contentOnly = gizmoType == GizmoTargetAjaxManager.GizmoType.SIDEBAR_CONTENTONLY || gizmoType == GizmoTargetAjaxManager.GizmoType.TOPBAR_CONTENTONLY;
                    if (!contentOnly) {
                        html.append(GroupGizmo.getContainer(temp, "ws_sortable_top", LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID));
                    } else {
                        html.append(temp);
                    }
                }
                if (gizmoType == GizmoTargetAjaxManager.GizmoType.SIDEBAR_CONTENTONLY || gizmoType == GizmoTargetAjaxManager.GizmoType.SIDEBAR) {
                    this.script.append("modernLayout.sidebar.canEdit = ").append(this.canAddAndRemoveItems).append(";");
                    this.script.append("modernLayout.sidebar.canMove = ").append(this.canMoveItems).append(";");
                }
            } else {
                html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/groupgizmo.js\"></script>");
                html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/groupgizmo.css", this.pageContext) + "\">");
                this.getDashboard(html, this.script);
            }
        }
        return html.toString();
    }

    public static void populateCogMenu(PropertyList menuProps, String id, boolean childgroup, BaseGizmo.GizmoLocation location, PropertyList gizmoProps, PropertyList guiPolicy, boolean locked, boolean adminuser, boolean adminmode, boolean hasCreateRole, PageContext pageContext) {
        PropertyList cogitem;
        PropertyListCollection cogmenu = new PropertyListCollection();
        boolean sep = false;
        if (!childgroup) {
            if (location == BaseGizmo.GizmoLocation.DASHBOARD) {
                GroupPickerGizmo.addDashboardGroups(cogmenu, pageContext);
            } else if (location == BaseGizmo.GizmoLocation.SIDEBAR) {
                GroupPickerGizmo.addSidebarGroups(cogmenu, pageContext);
            }
            if (cogmenu.size() > 0) {
                sep = true;
            }
        }
        PropertyList options = gizmoProps.getPropertyList("groupoptions");
        boolean sep2 = false;
        if (!(guiPolicy == null || locked && location != BaseGizmo.GizmoLocation.SIDEBAR || !adminuser && location != BaseGizmo.GizmoLocation.SIDEBAR && (!options.getProperty("public", LABVANTAGE_CVS_ID).equalsIgnoreCase("Y") || options.getProperty("editinglevel", LABVANTAGE_CVS_ID).equalsIgnoreCase("View Only") || adminmode))) {
            if (sep) {
                cogitem = new PropertyList();
                cogitem.setProperty("id", "Sep");
                cogitem.setProperty("text", "-");
                cogmenu.add(cogitem);
                sep = false;
            }
            if (location == BaseGizmo.GizmoLocation.SIDEBAR || adminuser || options.getProperty("editinglevel", LABVANTAGE_CVS_ID).equalsIgnoreCase("Fully Editable")) {
                cogitem = new PropertyList();
                cogitem.setProperty("id", "Add");
                cogitem.setProperty("text", "Add Gizmo");
                if (location == BaseGizmo.GizmoLocation.DASHBOARD) {
                    cogitem.setProperty("link", "javascript:groupGizmo.grid.add('" + id + "')");
                } else if (location == BaseGizmo.GizmoLocation.SIDEBAR) {
                    cogitem.setProperty("link", "javascript:modernLayout.gizmos.add(true)");
                }
                cogmenu.add(cogitem);
            }
            if (!adminmode) {
                cogitem = new PropertyList();
                cogitem.setProperty("id", "Unlock");
                if (location == BaseGizmo.GizmoLocation.DASHBOARD) {
                    cogitem.setProperty("text", "Unlock " + (childgroup ? " Group" : "Dashboard"));
                    cogitem.setProperty("link", "javascript:groupGizmo.grid.toggleLock('" + id + "')");
                } else if (location == BaseGizmo.GizmoLocation.SIDEBAR) {
                    cogitem.setProperty("text", "Unlock Sidebar");
                    cogitem.setProperty("link", "javascript:modernLayout.toggleManage(true)");
                }
                cogmenu.add(cogitem);
            }
            if (!adminmode) {
                cogitem = new PropertyList();
                cogitem.setProperty("id", "Reset");
                if (location == BaseGizmo.GizmoLocation.DASHBOARD) {
                    cogitem.setProperty("text", "Reset " + (childgroup ? " Group" : "Dashboard"));
                    cogitem.setProperty("link", "javascript:groupGizmo.grid.reset('" + id + "')");
                } else if (location == BaseGizmo.GizmoLocation.SIDEBAR) {
                    cogitem.setProperty("text", "Reset Sidebar");
                    cogitem.setProperty("link", "javascript:modernLayout.gizmos.reset(modernLayout.groupGizmo)");
                }
                cogmenu.add(cogitem);
            }
            sep2 = true;
        }
        if (!childgroup) {
            PropertyList seperator;
            boolean sep3 = false;
            if (!locked && location == BaseGizmo.GizmoLocation.DASHBOARD && adminuser) {
                if (sep2 || sep) {
                    seperator = new PropertyList();
                    seperator.setProperty("text", "-");
                    cogmenu.add(seperator);
                    sep = false;
                    sep2 = false;
                }
                cogitem = new PropertyList();
                cogitem.setProperty("id", "AdminMode");
                cogitem.setProperty("text", adminmode ? "Turn Off Admin Mode" : "Turn On Admin Mode");
                cogitem.setProperty("link", "javascript:groupGizmo.grid.toggleAdminMode('" + id + "')");
                cogmenu.add(cogitem);
                if (adminmode) {
                    cogitem = new PropertyList();
                    cogitem.setProperty("id", "Options");
                    cogitem.setProperty("text", "Dashboard Options");
                    cogitem.setProperty("link", "javascript:groupGizmo.grid.showOptions('" + id + "')");
                    cogmenu.add(cogitem);
                }
                sep3 = true;
            }
            if (hasCreateRole && (guiPolicy == null || guiPolicy.getProperty("lockgroups", "N").equalsIgnoreCase("N"))) {
                if (sep2 || sep || sep3) {
                    seperator = new PropertyList();
                    seperator.setProperty("text", "-");
                    cogmenu.add(seperator);
                    sep2 = false;
                    sep = false;
                    sep3 = false;
                }
                cogitem = new PropertyList();
                if (location == BaseGizmo.GizmoLocation.DASHBOARD) {
                    cogitem.setProperty("id", "CreateDashboard");
                    cogitem.setProperty("text", "Create Dashboard");
                    cogitem.setProperty("link", "javascript:groupGizmo.grid.create()");
                } else if (location == BaseGizmo.GizmoLocation.SIDEBAR) {
                    cogitem.setProperty("id", "CreateSidebar");
                    cogitem.setProperty("text", "Create Sidebar");
                    cogitem.setProperty("link", "javascript:modernLayout.sidebar.create()");
                }
                cogmenu.add(cogitem);
            }
        }
        menuProps.setProperty("custommenu", cogmenu);
    }

    private void renderCogMenu(StringBuffer html, String gridid) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<div id=\"").append(this.getGizmoDefId()).append("_cogmenucontainer\" class=\"dashboardcogmenu\" gridid=\"").append(gridid).append("\">");
        StringBuffer temp = new StringBuffer();
        temp.append("<div class='dashboardcogmenu_div'>");
        Image image = new Image(this.pageContext);
        image.setImageId("FlatBlackCog");
        if (this.adminmode) {
            image.setColor("red");
        }
        image.setTitle("Manage Dashboard");
        image.setDimensions(16, 16);
        temp.append(image.getHtml());
        temp.append("</div>");
        PropertyList menuProps = new PropertyList();
        menuProps.setProperty("renderincludes", "Y");
        menuProps.setProperty("customhtml", temp.toString());
        MenuGizmo menuGizmo = new MenuGizmo();
        menuGizmo.setPageContext(this.pageContext);
        GroupGizmo.populateCogMenu(menuProps, gridid, this.childgroup, BaseGizmo.GizmoLocation.DASHBOARD, this.getElementProperties(), this.guiPolicy, this.locked, this.adminuser, this.adminmode, this.hasCreateRole, this.pageContext);
        menuProps.setProperty("forcepositioncss", "{\"right\":\"5px\",\"top\":\"25px\"}");
        menuProps.setProperty("menutype", "custom");
        menuProps.setProperty("click", "Y");
        menuProps.setProperty("mouseover", "N");
        menuProps.setProperty("customclass", "dashboardcogmenu_menudiv");
        String sid = StringUtil.replaceAll(StringUtil.replaceAll(this.getGizmoDefId(), " ", "_"), "&", "n");
        menuGizmo.setElementProperties(menuProps);
        menuGizmo.setElementid(sid + "_cogmenu");
        menuGizmo.init();
        buffer.append(menuGizmo.getHtml());
        buffer.append("</div>");
        if (menuProps.getCollection("custommenu").size() > 0) {
            html.append(buffer);
            this.script.append(menuGizmo.getScript());
        }
    }

    private void setUpParameters() {
        Map in = this.request != null ? this.request.getParameterMap() : (this.pageContext != null ? this.pageContext.getRequest().getParameterMap() : null);
        PropertyList parameters = this.getParameters();
        if (parameters == null) {
            parameters = new PropertyList();
            this.setParameters(parameters);
        }
        GroupGizmo.setUpParameters(in, parameters, this.connectionInfo);
    }

    private String[] pagination(int currentPage, int lastPage, int delta) {
        int current = currentPage;
        int last = lastPage;
        int left = current - delta;
        int right = current + delta;
        ArrayList<Integer> range = new ArrayList<Integer>();
        ArrayList<String> rangeWithDots = new ArrayList<String>();
        for (int i = 1; i <= last; ++i) {
            if (i != 1 && i != last && (i < left || i >= right)) continue;
            range.add(i);
        }
        int l = -1;
        Iterator iterator = range.iterator();
        while (iterator.hasNext()) {
            int i = (Integer)iterator.next();
            if (l > -1) {
                if (i - l == delta) {
                    rangeWithDots.add(LABVANTAGE_CVS_ID + (l + 1));
                } else if (i - l != 1) {
                    rangeWithDots.add("...");
                }
            }
            rangeWithDots.add(LABVANTAGE_CVS_ID + i);
            l = i;
        }
        return rangeWithDots.toArray(new String[0]);
    }

    private void drawPagination(StringBuilder html, int currentPage, int totalpages) {
        String[] range = this.pagination(currentPage, totalpages, 2);
        StringBuilder buffer = new StringBuilder();
        if (range.length > 1) {
            for (String r : range) {
                if (buffer.length() > 0) {
                    buffer.append("&nbsp;");
                }
                if (r.equalsIgnoreCase(LABVANTAGE_CVS_ID + currentPage)) {
                    buffer.append("<span class=\"groupgizmo_sel\">").append(r).append("</span>");
                    continue;
                }
                if (!r.equalsIgnoreCase("...")) {
                    buffer.append("<a href=\"javascript:groupGizmo.grid.gotoPage('").append(this.getGizmoDefId()).append("',").append(r).append(");\">").append(r).append("</a>");
                    continue;
                }
                buffer.append(r);
            }
        }
        html.append((CharSequence)buffer);
    }

    private void getDashboard(StringBuffer output, StringBuffer script) {
        this.setUpParameters();
        final String gridid = StringUtil.replaceAll(StringUtil.replaceAll(this.elementid, " ", "_"), "&", "n");
        this.childgroup = this.getElementProperties().getProperty("childgroup", "N").equalsIgnoreCase("Y");
        this.adminmode = this.requestContext.getProperty("applyuseroverrides").equalsIgnoreCase("N");
        PropertyListCollection gizmosColl = this.element.getCollectionNotNull("gizmos");
        boolean isDevMode = false;
        try {
            com.labvantage.sapphire.admin.system.ConfigurationProcessor config = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.getConnectionId());
            String devMode = config.getSysConfigProperty("devmode", "N");
            isDevMode = devMode.equalsIgnoreCase("Y");
        }
        catch (Exception config) {
            // empty catch block
        }
        PropertyListCollection plugins = new PropertyListCollection();
        PropertyList plugin = new PropertyList();
        plugin.setProperty("pluginid", "gridster");
        plugin.setProperty("css", "Y");
        plugin.setProperty("allowminimized", isDevMode ? "N" : "Y");
        plugins.add(plugin);
        output.append(JavaScriptAPITag.getJQueryAPI(true, false, plugins, LABVANTAGE_CVS_ID, !isDevMode, this.pageContext));
        this.renderCogMenu(output, gridid);
        StringBuilder dockedHTML = new StringBuilder();
        StringBuilder html = new StringBuilder();
        html.append("<div id=\"").append(gridid).append("\" class=\"gridster groupgizmo_grid\" childgroup=\"").append(this.childgroup ? "Y" : "N").append("\" dynamicgroup=\"").append(this.dynamicgroup ? "Y" : "N").append("\">");
        html.append("<ul id=\"grid_").append(gridid).append("\" class=\"groupgizmo_griditem\" gizmodefid=\"").append(this.getGizmoDefId()).append("\">");
        int columncount = 0;
        int rowcount = 0;
        ConnectionProcessor cp = this.pageContext != null ? new ConnectionProcessor(this.pageContext) : new ConnectionProcessor(this.getConnectionId());
        SapphireConnection sc = cp.getSapphireConnection();
        TreeMap<String, sapphire.pageelements.BaseGizmo> cacheToRender = new TreeMap<String, sapphire.pageelements.BaseGizmo>();
        for (int i = 0; i < gizmosColl.size(); ++i) {
            PropertyList p = gizmosColl.getPropertyList(i);
            String gid = p.getProperty("gizmoid");
            if (gid.length() <= 0 || !p.getProperty("show", "Y").equalsIgnoreCase("Y")) continue;
            String id = p.getProperty("id");
            PropertyList customprops = p.containsKey("gizmoprops") ? p.getPropertyList("gizmoprops") : null;
            String ptreeid = p.getProperty("ptreeid", LABVANTAGE_CVS_ID);
            String extendnodeid = p.getProperty("extendnodeid", LABVANTAGE_CVS_ID);
            sapphire.pageelements.BaseGizmo gizmo = null;
            if (customprops != null && !customprops.getProperty("_dynamicprops").equalsIgnoreCase("Y") && ptreeid.length() > 0 && extendnodeid.length() > 0) {
                gizmo = BaseGizmo.getTypeInstance(this.getConnectionId(), this.pageContext, ptreeid, extendnodeid, this.getParameters(), true);
                if (gizmo != null) {
                    if (gid != null && gid.length() > 0) {
                        gizmo.setGizmoDefId(gid);
                    }
                    PropertyList gizmoprops = gizmo.getElementProperties();
                    try {
                        gizmoprops.setPropertyList(customprops.toXMLString(), true, true);
                    }
                    catch (Exception exception) {}
                } else {
                    this.logger.warn("Gizmo for propertytreeid " + ptreeid + " could not be created.");
                }
            } else {
                gizmo = BaseGizmo.getInstance(this.pageContext, gid, this.getParameters(), true, true);
                if (customprops != null) {
                    try {
                        gizmo.getElementProperties().setPropertyList(customprops.toXMLString(), true);
                    }
                    catch (Exception gizmoprops) {
                        // empty catch block
                    }
                }
            }
            if (gizmo == null) continue;
            String uniId = id + "d_" + ((int)(Math.random() * 100.0) + 1);
            gizmo.setElementid(uniId);
            if (gizmo instanceof ParameterGizmo || gizmo instanceof ButtonGizmo) {
                gizmo = BaseGizmo.initalizeInstance(gizmo, this.getConnectionId(), sc, this.pageContext, this.getParameters());
                gizmo.setGizmoStyle(BaseGizmo.GizmoStyle.FULL);
                gizmo.setColor(Color.BLACK);
                gizmo.setGizmoLocation(BaseGizmo.GizmoLocation.DASHBOARD);
                dockedHTML.append("<div class=\"ws_gizmotarget ws_").append(gizmo instanceof ParameterGizmo ? "parameter" : "button").append("gizmo\" collectionid=\"").append(p.getProperty("id")).append("\" elementid=\"").append(gizmo.getElementid()).append("\" gizmoid=\"").append(gizmo.getElementid()).append("\" gizmodefid=\"").append(gizmo.getGizmoDefId()).append("\" id=\"ws_gizmotarget_").append(gizmo.getElementid()).append("\">");
                dockedHTML.append(gizmo.getHtml());
                dockedHTML.append("<div class=\"ws_gizmotarget_trash").append("\" onclick=\"groupGizmo.grid.remove('").append(gridid).append("','").append(gizmo.getElementid()).append("')\"></div>");
                dockedHTML.append("</div>");
                continue;
            }
            if (gizmo instanceof GroupGizmo) {
                if (this.childgroup) {
                    gizmo = null;
                } else {
                    gizmo.getElementProperties().setProperty("childgroup", "Y");
                }
            }
            if (gizmo == null) continue;
            cacheToRender.put(id, gizmo);
        }
        for (String id : cacheToRender.keySet()) {
            BaseGizmo gizmo = (BaseGizmo)cacheToRender.get(id);
            if (gizmo == null) continue;
            int width = -1;
            int height = -1;
            int row = -1;
            int column = -1;
            PropertyList gridProps = null;
            PropertyList gizmoPL = gizmosColl.find("id", id);
            if (gizmoPL != null) {
                gridProps = gizmoPL.getPropertyList("grid");
            }
            if (gridProps != null) {
                try {
                    width = Integer.parseInt(gridProps.getProperty("size_x"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    height = Integer.parseInt(gridProps.getProperty("size_y"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    column = Integer.parseInt(gridProps.getProperty("col"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    row = Integer.parseInt(gridProps.getProperty("row"));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (width < 1 || height < 1) {
                width = 2;
                height = 1;
            }
            BaseGizmo.GizmoStyle style = BaseGizmo.GizmoStyle.FULL;
            style = width < 3 & height < 2 || width < 2 && height < 3 ? (width < 2 && height < 2 ? BaseGizmo.GizmoStyle.LARGE : BaseGizmo.GizmoStyle.LARGETEXT) : BaseGizmo.GizmoStyle.FULL;
            if (style == BaseGizmo.GizmoStyle.FULL && !(gizmo instanceof GroupGizmo)) {
                BaseGizmo fake = new BaseGizmo(){

                    @Override
                    public String getScript() {
                        return GroupGizmo.LABVANTAGE_CVS_ID;
                    }

                    @Override
                    public String getHtml() {
                        return "<div class=\"gizmo_placeholder\" gridid=\"" + gridid + "\">Loading...</div>";
                    }
                };
                fake.setElementid(gizmo.getElementid());
                fake.setGizmoStyle(style);
                fake.setTitle(gizmo.getGizmoDefId() + LABVANTAGE_CVS_ID);
                fake.setGizmoDefId(gizmo.getGizmoDefId());
                fake.setElementType(gizmo.getElementType());
                fake.setElementProperties(gizmo.getElementProperties());
                gizmo = fake;
            } else {
                BaseGizmo.initalizeInstance((sapphire.pageelements.BaseGizmo)gizmo, this.getConnectionId(), sc, this.pageContext, this.getParameters());
            }
            gizmo.setGizmoStyle(style);
            gizmo.setColor(Color.BLACK);
            gizmo.setGizmoLocation(BaseGizmo.GizmoLocation.DASHBOARD);
            PropertyList gp = gizmo.getElementProperties().getPropertyList("gizmoprops");
            if (gp == null) {
                gp = new PropertyList();
                gizmo.getElementProperties().setProperty("gizmoprops", gp);
            }
            if (column < 0 || row < 0) {
                if (columncount - 1 + width > 10) {
                    column = 1;
                    columncount = 1;
                    rowcount = row += 2;
                } else {
                    column = columncount == 0 ? columncount + 1 : columncount + 2;
                    row = rowcount + 1;
                    columncount = column;
                    rowcount = row;
                }
            }
            gp.setProperty("column", LABVANTAGE_CVS_ID + column);
            gp.setProperty("row", LABVANTAGE_CVS_ID + row);
            gp.setProperty("size_x", LABVANTAGE_CVS_ID + width);
            gp.setProperty("size_y", LABVANTAGE_CVS_ID + height);
            html.append(GizmoTargetAjaxManager.renderGizmo(id, gizmo, script, GizmoTargetAjaxManager.GizmoType.DASHBOARD, style, this.getTranslationProcessor()));
            column += width;
        }
        html.append("</ul>");
        if (this.totalpages > 0) {
            html.append("<div id=\"").append(gridid).append("_pager\" class=\"groupgizmo_pager\">");
            this.drawPagination(html, this.pagenum, this.totalpages);
            html.append("</div>");
        }
        html.append("</div>");
        if (this.adminmode && this.adminuser) {
            script.append("groupGizmo.adminmode = true;");
        }
        PropertyList options = this.getElementProperties().getPropertyList("groupoptions");
        script.append("groupGizmo.grid.register('").append(gridid).append("','").append(this.getGizmoDefId()).append("',").append(this.getElementProperties().getProperty("showdashboardheaders", "Y").equalsIgnoreCase("Y")).append(", sapphire.util.propertyList.create(").append(this.getParameters().toJSONString(false, false)).append("),").append(this.childgroup).append(",").append(this.dynamicgroup).append(",").append(this.canMoveItems).append(",").append(this.canAddAndRemoveItems).append(",").append(this.getElementProperties().getPropertyList("groupoptions").getProperty("public", "N").equalsIgnoreCase("Y")).append(",").append("'").append(this.getElementProperties().getPropertyList("groupoptions").getProperty("editinglevel", LABVANTAGE_CVS_ID)).append("',").append("'").append(this.getTitle()).append("',").append(LABVANTAGE_CVS_ID).append(this.element.getAttribute("useroverrides").equalsIgnoreCase("Y")).append(");");
        if (dockedHTML.length() > 0) {
            output.append("<div id=\"").append(this.getGizmoDefId()).append("_parameters\" class=\"dashboardparameters\" gridid=\"").append(gridid).append("\">");
            output.append((CharSequence)dockedHTML);
            output.append("</div>");
        }
        output.append((CharSequence)html);
        output.append("<div id=\"emptydashboard\" onclick=\"groupGizmo.grid.add('").append(gridid).append("', true)\" style=\"display:none;").append("\">");
        output.append("<div>");
        output.append(this.getTranslationProcessor().translate("Empty Dashboard")).append("<br>");
        Image image = new Image(this.pageContext);
        image.setImageId("FlatWhitePlus1");
        image.setDimensions(128, 128);
        output.append(image.getHtml());
        output.append("<br>");
        output.append(this.getTranslationProcessor().translate("Start Adding Gizmos"));
        output.append("</div>");
        output.append("</div>");
    }

    private String getPopupHtml() {
        StringBuffer html = new StringBuffer();
        this.script = new StringBuffer();
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/groupgizmo.js\"></script>");
        if (this.element != null) {
            PropertyListCollection gizmosColl = this.element.getCollectionNotNull("gizmos");
            BaseGizmo.GizmoStyle gizmoStyle = BaseGizmo.GizmoStyle.TEXT;
            try {
                gizmoStyle = BaseGizmo.GizmoStyle.valueOf(this.element.getProperty("gizmostyle", gizmoStyle.toString()).toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (gizmoStyle == BaseGizmo.GizmoStyle.TEXT || gizmoStyle == BaseGizmo.GizmoStyle.SMALLTEXT) {
                for (int i = 0; i < gizmosColl.size(); ++i) {
                    sapphire.pageelements.BaseGizmo gizmo;
                    PropertyList p = gizmosColl.getPropertyList(i);
                    String gid = p.getProperty("gizmoid");
                    if (gid.length() <= 0 || (gizmo = BaseGizmo.getInstance(this.pageContext, gid, true)) instanceof GroupGizmo || gizmo == null || !p.getProperty("show", "Y").equalsIgnoreCase("Y")) continue;
                    html.append("<div class=\"groupgizmo_line groupgizmo_ll\">");
                    html.append("<div class=\"groupgizmo_item\">");
                    String id = "ggl_" + p.getProperty("id") + ((int)(Math.random() * 100.0) + 1);
                    gizmo.setElementid(id);
                    gizmo.setGizmoStyle(gizmoStyle);
                    gizmo.setPreviewMode(this.isPreviewMode());
                    html.append(gizmo.getIconHtml());
                    this.script.append(((BaseGizmo)gizmo).getScript());
                    html.append("</div>");
                    html.append("</div>");
                }
            } else {
                int largeCount = 0;
                boolean largeStart = false;
                int largeLine = 2;
                for (int i = 0; i < gizmosColl.size(); ++i) {
                    sapphire.pageelements.BaseGizmo gizmo;
                    PropertyList p = gizmosColl.getPropertyList(i);
                    String gid = p.getProperty("gizmoid");
                    if (gid.length() <= 0 || (gizmo = BaseGizmo.getInstance(this.pageContext, gid, true)) instanceof GroupGizmo || !p.getProperty("show", "Y").equalsIgnoreCase("Y")) continue;
                    if (largeCount == 0) {
                        html.append("<div class=\"groupgizmo_line groupgizmo_ll\">");
                        largeStart = true;
                    }
                    html.append("<div class=\"groupgizmo_item\">");
                    String id = "ggl_" + p.getProperty("id") + ((int)(Math.random() * 100.0) + 1);
                    gizmo.setElementid(id);
                    gizmo.setGizmoStyle(gizmoStyle);
                    gizmo.setPreviewMode(this.isPreviewMode());
                    html.append(gizmo.getIconHtml());
                    this.script.append(((BaseGizmo)gizmo).getScript());
                    html.append("</div>");
                    if (largeCount == largeLine) {
                        html.append("</div>");
                        largeStart = false;
                        largeCount = 0;
                        continue;
                    }
                    ++largeCount;
                }
                if (largeStart) {
                    html.append("</div>");
                }
            }
        }
        return html.toString();
    }

    @Override
    public String getScript() {
        if (this.script == null) {
            this.script = new StringBuffer();
        }
        boolean layout = this.element.getProperty("layout", "N").equalsIgnoreCase("Y");
        if (!this.isPreviewMode()) {
            this.script.append("groupGizmo.register('").append(this.elementid).append("' ");
            if (this.element.getProperty("click", "Y").equalsIgnoreCase("Y")) {
                this.script.append(", true");
            } else {
                this.script.append(", false");
            }
            if (this.element.getProperty("mouseover", "Y").equalsIgnoreCase("Y")) {
                this.script.append(", true");
            } else {
                this.script.append(", false");
            }
            this.script.append(",").append(this.offsetY).append(", ").append(this.offsetX).append(LABVANTAGE_CVS_ID);
            this.script.append(");");
        }
        return this.script.toString();
    }

    @Override
    public String getURL() {
        boolean dashboard = this.element.getProperty("dashboard", "N").equalsIgnoreCase("Y");
        return dashboard ? "rc?command=gizmo&gizmo=" + this.getGizmoDefId() : "javascript:top.modernLayout.setGroup('" + this.getGizmoDefId() + "')";
    }

    @Override
    public String getIconHtml() {
        BaseGizmo.GizmoStyle gizmoStyle = this.getGizmoStyle();
        StringBuffer html = new StringBuffer();
        PropertyListCollection gizmosCol = this.element.getCollectionNotNull("gizmos");
        boolean inlineGroup = this.element.getProperty("inlinegroup", "N").equalsIgnoreCase("Y");
        html.append("<div id=\"").append(this.elementid).append("\" class=\"groupgizmo ").append(gizmoStyle.size < 32 ? "groupgizmo_s" : "groupgizmo_l").append("\"");
        if (!inlineGroup) {
            html.append(" onclick=\"" + this.getNavigateJS() + "\"");
        }
        html.append(">");
        String h = this.getHelpText();
        h = SafeHTML.encodeForHTML(h, true);
        String t = this.getTitle();
        t = SafeHTML.encodeForHTML(t, true);
        if (inlineGroup) {
            Object o = ((HttpServletRequest)this.pageContext.getRequest()).getSession().getAttribute("userconfig");
            PropertyList userconfig = o != null ? (PropertyList)o : this.requestContext.getPropertyList("userconfig");
            boolean expanded = true;
            String gid = this.getGizmoDefId();
            if (gid.length() > 0) {
                expanded = userconfig.getProperty("inlinegroup_" + StringUtil.replaceAll(gid, " ", "_"), "Y").equalsIgnoreCase("Y");
            }
            if (gizmoStyle.showTitle) {
                html.append("<h3 class=\"groupgizmo_header\" onclick=\"").append("groupGizmo.toggleExpand('").append(this.elementid).append("',this)").append("\">");
                html.append("<span title=\"").append(h).append("\"").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_txt\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(t);
                html.append("&nbsp;");
                Image image = new Image(this.pageContext);
                image.setImageId("FlatBlackSortUp");
                image.setDimensions(10, 10);
                if (expanded) {
                    image.setStyle("display:inline");
                } else {
                    image.setStyle("display:none");
                }
                html.append(image.getHtml());
                image = new Image(this.pageContext);
                image.setImageId("FlatBlackSortDownDropdown");
                image.setDimensions(10, 10);
                if (expanded) {
                    image.setStyle("display:none");
                } else {
                    image.setStyle("display:inline");
                }
                html.append(image.getHtml());
                html.append("</span>");
                html.append("</h3>");
                if (this.script == null) {
                    this.script = new StringBuffer();
                }
                if (!expanded) {
                    this.script.append("$('#" + this.elementid + " .ws_sortable_item_s').hide();");
                }
            }
            BaseGizmo.GizmoStyle substyle = BaseGizmo.GizmoStyle.FULL;
            switch (gizmoStyle) {
                case LARGETEXT: 
                case MEDIUMTEXT: 
                case SMALLTEXT: {
                    substyle = BaseGizmo.GizmoStyle.SMALLTEXT;
                    break;
                }
                default: {
                    substyle = BaseGizmo.GizmoStyle.TEXT;
                }
            }
            String temp = this.getGizmos(gizmosCol, GizmoTargetAjaxManager.GizmoType.SIDEBAR, substyle, this.script, this.pageContext);
            boolean contentOnly = true;
            if (!contentOnly) {
                html.append(GroupGizmo.getContainer(temp, this.elementid + "_items", "groupgizmo_inline", LABVANTAGE_CVS_ID));
            } else {
                html.append(temp);
            }
        } else {
            if (gizmoStyle == BaseGizmo.GizmoStyle.SMALL || gizmoStyle == BaseGizmo.GizmoStyle.SMALLTEXT) {
                if (gizmoStyle == BaseGizmo.GizmoStyle.SMALL) {
                    this.offsetX = 0;
                    this.offsetY = 25;
                } else {
                    this.offsetX = 85;
                    this.offsetY = -80;
                }
                this.element.getPropertyList("gizmoprops").setProperty("image", this.element.getProperty("dashboard", "N").equalsIgnoreCase("Y") ? "FlatBlackBarHistogram" : "FlatBlackFolder2Closed");
                html.append("<span title=\"").append(h).append("\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_img\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(this.getIcon());
                html.append("</span>");
            } else if (gizmoStyle == BaseGizmo.GizmoStyle.MEDIUM || gizmoStyle == BaseGizmo.GizmoStyle.MEDIUMTEXT) {
                this.offsetX = 25;
                this.offsetY = -80;
                this.element.getPropertyList("gizmoprops").setProperty("image", this.element.getProperty("dashboard", "N").equalsIgnoreCase("Y") ? "FlatBlackBarHistogram" : "FlatBlackFolder2Closed");
                html.append("<span title=\"").append(h).append("\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_img\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(this.getIcon());
                html.append("</span>");
            } else if (gizmoStyle == BaseGizmo.GizmoStyle.TEXT) {
                this.offsetX = 85;
                this.offsetY = -70;
            } else {
                this.offsetX = 25;
                this.offsetY = -70;
                this.element.getPropertyList("gizmoprops").setProperty("image", this.element.getProperty("dashboard", "N").equalsIgnoreCase("Y") ? "FlatBlackBarHistogram" : "FlatBlackFolder2Closed");
                html.append("<span title=\"").append(h).append("\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_img\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(this.getIcon());
                html.append("</span>");
            }
            if (gizmoStyle.showTitle) {
                html.append("<span title=\"").append(h).append("\" onclick=\"").append(this.getNavigateJS()).append("\" ").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_txt\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(t);
                html.append("</span>");
            }
            html.append("<div id=\"cdiv_").append(this.elementid).append("\" class=\"groupgizmo_cover\" title=\"").append(h).append("\">");
            html.append("</div>");
            html.append("</div>");
            html.append("<div id=\"gdiv_").append(this.elementid).append("\" class=\"groupgizmo_div\" style=\"display:none;position:absolute;\">");
            if (!this.isPreviewMode()) {
                this.element.setProperty("layout", "N");
            }
        }
        html.append("</div>");
        return html.toString();
    }

    @Override
    public String getTitle() {
        String t = super.getTitle();
        if (t.contains("<") && t.contains(">")) {
            t = DOMUtil.convertChars(t);
        }
        return t;
    }
}

