/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.controls.Panel;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.Browser;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Dashboard
extends BaseElement {
    public static final String PAGEDATA_PROPERTY = "pagedata";
    public static final String GIZMOPROPS_PROPERTY = "gizmoprops";
    public static final String TABS_PROPERTY = "tabs";
    public static final String GIZMOS_PROPERTY = "gizmos";
    public static final String GIZMOID_PROPERTY = "gizmoid";
    public static final String TITLE_PROPERTY = "title";
    public static final String VISIBLE_PROPERTY = "visible";
    public static final String SELECTEDTAB_PROPERTY = "selectedtab";
    public static final String ZINDEX_PROPERTY = "zindex";
    public static final String OBJECTNAME_PROPERTY = "objectname";
    public static final String PROPERTYTREEID_PROPERTY = "propertytreeid";
    public static final String X_PROPERTY = "x";
    public static final String Y_PROPERTY = "y";
    public static final String WSX_PROPERTY = "wsx";
    public static final String WSY_PROPERTY = "wsy";
    public static final String TABID_PROPERTY = "tabid";
    public static final String USETABS_PROPERTY = "usetabs";
    public static final String DISPLAYTYPE_PROPERTY = "displaytype";
    public static final String HIDEGIZMOTITLEBAR_PROPERTY = "hidegizmotitlebar";
    public static final String DEFAULTTITLE = "Default Tab";
    private boolean completeRender = true;
    private PropertyList pageProperties = null;
    private StringBuffer script = new StringBuffer("");
    private HttpServletRequest request = null;
    private PropertyList parameters = null;
    private boolean autoarrange = false;
    private StringBuffer autoarrangeScript = new StringBuffer();
    private boolean devMode = false;
    private TranslationProcessor tp;

    public Dashboard() {
    }

    @Override
    public TranslationProcessor getTranslationProcessor() {
        if (this.tp == null) {
            if (this.pageContext != null) {
                this.tp = super.getTranslationProcessor();
            } else if (this.getConnectionId() != null && this.getConnectionId().length() > 0) {
                this.tp = new TranslationProcessor(this.getConnectionId());
            }
        }
        return this.tp;
    }

    private String translate(String string) {
        TranslationProcessor tp = this.getTranslationProcessor();
        if (tp == null) {
            return string;
        }
        return tp.translate(string);
    }

    private void filterGizmos(PropertyList pageproperties) {
        PropertyListCollection gizmos = pageproperties.getPropertyList(PAGEDATA_PROPERTY).getCollection(GIZMOS_PROPERTY);
        if (gizmos != null && gizmos.size() > 0 || gizmos.getAttributes() != null && gizmos.getAttributes().containsKey("filtered") && gizmos.getAttributes().get("filtered").equals("Y")) {
            Iterator it = pageproperties.keySet().iterator();
            ArrayList<String> toremove = new ArrayList<String>();
            while (it.hasNext()) {
                PropertyList elementcolProp;
                String currentkey = it.next().toString();
                if (currentkey.equalsIgnoreCase(PAGEDATA_PROPERTY) || currentkey.equalsIgnoreCase("favourites") || currentkey.equalsIgnoreCase("layout") || currentkey.equalsIgnoreCase("userconfig") || currentkey.equalsIgnoreCase("bulletins") || currentkey.equalsIgnoreCase("hasmodule") || !pageproperties.isPropertyList(currentkey) || (elementcolProp = gizmos.find(GIZMOID_PROPERTY, currentkey)) != null) continue;
                toremove.add(currentkey);
            }
            if (toremove.size() > 0) {
                for (int i = 0; i < toremove.size(); ++i) {
                    pageproperties.remove(toremove.get(i));
                }
            }
        }
    }

    public Dashboard(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            this.pageProperties = pageproperties;
            pageproperties.setProperty("jsrequest", "Gizmo");
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            PropertyList pagedata = pageproperties.getPropertyList(PAGEDATA_PROPERTY);
            if (pagedata != null) {
                this.filterGizmos(pageproperties);
                this.setElementProperties(pagedata);
            }
        }
        catch (Exception e) {
            this.logger.error(e.getMessage());
        }
    }

    public Dashboard(JSONObject jsonProperties, boolean completeRender) {
        this.completeRender = completeRender;
        try {
            this.pageProperties = new PropertyList(jsonProperties);
            PropertyList pagedata = this.pageProperties.getPropertyList(PAGEDATA_PROPERTY);
            if (pagedata != null) {
                this.setElementProperties(pagedata);
            }
        }
        catch (Exception e) {
            this.logger.error(e.getMessage());
        }
    }

    public String getScript() {
        String out = "";
        if (this.script.length() > 0) {
            out = this.script.toString();
        } else {
            this.logger.warn("getScript can only be called after HTML has been rendered by getHTML.");
        }
        return out;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
        if (this.browser == null) {
            this.browser = new Browser(request);
        }
    }

    private int findPropertyValueInCollection(PropertyListCollection collection, String propertyid, String value) {
        int out = -1;
        for (int index = 0; index < collection.size(); ++index) {
            PropertyList list = collection.getPropertyList(index);
            if (!list.containsKey(propertyid) || !list.getProperty(propertyid, "").equalsIgnoreCase(value)) continue;
            out = index;
            break;
        }
        return out;
    }

    private PropertyListCollection getTabs(PropertyList pageProperties) {
        Iterator it = pageProperties.keySet().iterator();
        PropertyListCollection tabs = new PropertyListCollection();
        while (it.hasNext()) {
            PropertyList newgizmo;
            String tabid;
            PropertyList elementProp;
            PropertyList gizmoProp;
            String currentkey = it.next().toString();
            if (!pageProperties.isPropertyList(currentkey) || (gizmoProp = (elementProp = pageProperties.getPropertyList(currentkey)).getPropertyList(GIZMOPROPS_PROPERTY)) == null || !gizmoProp.containsKey(TABID_PROPERTY) || !gizmoProp.getProperty(VISIBLE_PROPERTY, "Y").equals("Y") || (tabid = gizmoProp.getProperty(TABID_PROPERTY, "")).length() <= 0) continue;
            int pos = this.findPropertyValueInCollection(tabs, TITLE_PROPERTY, this.translate(tabid));
            if (pos > -1) {
                PropertyList oldtab = tabs.getPropertyList(pos);
                PropertyListCollection oldgizmos = oldtab.getCollection(GIZMOS_PROPERTY);
                newgizmo = new PropertyList();
                newgizmo.setProperty(GIZMOID_PROPERTY, currentkey);
                oldgizmos.add(newgizmo);
                continue;
            }
            PropertyList newtab = new PropertyList();
            newtab.setProperty(TITLE_PROPERTY, this.translate(tabid));
            PropertyListCollection newgizmos = new PropertyListCollection();
            newgizmo = new PropertyList();
            newgizmo.setProperty(GIZMOID_PROPERTY, currentkey);
            newgizmos.add(newgizmo);
            newtab.setProperty(GIZMOS_PROPERTY, newgizmos);
            tabs.add(newtab);
        }
        if (tabs.size() == 0) {
            tabs = new PropertyListCollection();
            PropertyList proplist = new PropertyList();
            proplist.setProperty(TITLE_PROPERTY, this.translate(DEFAULTTITLE));
            tabs.add(proplist);
        } else {
            Collections.sort(tabs, new Comparator(){

                public int compare(Object o1, Object o2) {
                    if (o1 instanceof PropertyList && o2 instanceof PropertyList) {
                        return ((PropertyList)o1).getProperty(Dashboard.TITLE_PROPERTY, "").compareTo(((PropertyList)o2).getProperty(Dashboard.TITLE_PROPERTY, ""));
                    }
                    return 0;
                }
            });
        }
        return tabs;
    }

    private void renderTab(PropertyListCollection tabs, int index, StringBuffer content, StringBuffer script, boolean workspace) {
        script.append("dashboard.currentTabId = '").append(tabs.getPropertyList(index).getProperty(TITLE_PROPERTY, "")).append("';");
        PropertyListCollection gizmos = tabs.getPropertyList(index).getCollection(GIZMOS_PROPERTY);
        if (gizmos != null) {
            PropertyList elementProps;
            String gizmoid;
            PropertyList gizmo;
            int gizmoindex;
            for (gizmoindex = 0; gizmoindex < gizmos.size(); ++gizmoindex) {
                gizmo = gizmos.getPropertyList(gizmoindex);
                gizmoid = gizmo.getProperty(GIZMOID_PROPERTY, "");
                if (gizmoid.length() <= 0 || (elementProps = this.pageProperties.getPropertyList(gizmoid)) == null || !elementProps.getProperty(PROPERTYTREEID_PROPERTY).equalsIgnoreCase("parametergizmo")) continue;
                content.append(this.renderGizmo(gizmoid, index, gizmoindex, script, workspace));
            }
            for (gizmoindex = 0; gizmoindex < gizmos.size(); ++gizmoindex) {
                gizmo = gizmos.getPropertyList(gizmoindex);
                gizmoid = gizmo.getProperty(GIZMOID_PROPERTY, "");
                if (gizmoid.length() <= 0 || (elementProps = this.pageProperties.getPropertyList(gizmoid)) == null || elementProps.getProperty(PROPERTYTREEID_PROPERTY).equalsIgnoreCase("parametergizmo")) continue;
                content.append(this.renderGizmo(gizmoid, index, gizmoindex, script, workspace));
            }
        }
    }

    private void renderTabs(StringBuffer html, StringBuffer script, PropertyList pageProperties) {
        int selectedTab;
        TabGroup tabGroup = new TabGroup();
        tabGroup.setBodywidth("100%");
        tabGroup.setBodyheight("100%");
        tabGroup.setAppearance("standard");
        tabGroup.setId("db__tabgroup");
        tabGroup.setMultiTab(true);
        tabGroup.setUseChangeTab(false);
        PropertyListCollection tabs = this.getTabs(pageProperties);
        String sel = this.element.getProperty(SELECTEDTAB_PROPERTY, "0");
        try {
            selectedTab = Integer.parseInt(sel);
        }
        catch (Exception e) {
            selectedTab = 0;
        }
        if (selectedTab > tabs.size() - 1) {
            selectedTab = tabs.size() - 1;
        } else if (selectedTab < 0) {
            selectedTab = 0;
        }
        script.append("dashboard.currentTab = ").append(selectedTab).append(";");
        for (int index = 0; index < tabs.size(); ++index) {
            Tab tab = new Tab();
            tab.setBodyheight("100%");
            tab.setBodywidth("100%");
            tab.setExpandable("false");
            tab.setExpanded("false");
            tab.setHighlight("false");
            tab.setAppearance("standard");
            tab.setAction("dashboard.tabChanged();");
            tab.setId("db__tab" + index);
            String text = tabs.getPropertyList(index).getProperty(TITLE_PROPERTY, this.translate(DEFAULTTITLE));
            tab.setText(text);
            StringBuffer content = new StringBuffer();
            content.append("<div id=\"db_container").append(index).append("\" ").append("style=\"height:100%;position:relative;\"").append(" oncontextmenu=\"dashboard.doContext(event);\">");
            if (index == selectedTab) {
                this.renderTab(tabs, index, content, script, false);
            }
            content.append("</div>");
            tab.setContent(content.toString());
            tabGroup.setTab(tab);
            this.autoarrangeScript.append("dashboard.arrange[").append(index).append("]=true;");
        }
        Tab tab = new Tab();
        tab.setBodyheight("100%");
        tab.setBodywidth("100%");
        tab.setExpandable("false");
        tab.setExpanded("false");
        tab.setHighlight("false");
        tab.setAppearance("standard");
        tab.setId("db__tab1");
        tab.setText("<div height=14 width=\"100%\" align=center><img height=14 src=\"WEB-CORE/images/gif/Add.gif\"></div>");
        tab.setContent("&nbsp;");
        tab.setAction("dashboard.menuTabClick();");
        tabGroup.setSelectedTab(selectedTab);
        tabGroup.setTab(tab);
        html.append(tabGroup.getHtml());
    }

    private void renderOnePage(StringBuffer html, StringBuffer script, PropertyList pageProperties) {
        int selectedTab;
        html.append("<table style=\"width:100%;height:100%;\" border=\"0\" cellpadding=0 cellspacing=0>");
        html.append("<tr>");
        html.append("<td style=\"width:100%;height:100%;\">");
        html.append("<div id=\"db_container0").append("\" style=\"position:relative;border:solid 1px transparent;width:auto;height:100%;display:block;overflow:auto;\" oncontextmenu=\"dashboard.doContext(event);\">");
        PropertyListCollection tabs = this.getTabs(pageProperties);
        String sel = this.element.getProperty("forcetab", "0");
        try {
            selectedTab = Integer.parseInt(sel);
        }
        catch (Exception e) {
            selectedTab = 0;
        }
        if (selectedTab > tabs.size() - 1) {
            selectedTab = tabs.size() - 1;
        } else if (selectedTab < 0) {
            selectedTab = 0;
        }
        this.renderTab(tabs, selectedTab, html, script, false);
        html.append("</div>");
        html.append("<td>");
        html.append("<tr>");
        html.append("</table>");
        this.autoarrangeScript.append("dashboard.arrange[0]=true;");
    }

    private void renderWorkspace(StringBuffer html, StringBuffer script, PropertyList pageProperties) {
        int selectedTab;
        html.append("<div class=\"gridster\" style=\"opacity:0;\">");
        html.append("<ul id=\"grid\">");
        PropertyListCollection tabs = this.getTabs(pageProperties);
        String sel = this.element.getProperty("forcetab", "0");
        try {
            selectedTab = Integer.parseInt(sel);
        }
        catch (Exception e) {
            selectedTab = 0;
        }
        if (selectedTab > tabs.size() - 1) {
            selectedTab = tabs.size() - 1;
        } else if (selectedTab < 0) {
            selectedTab = 0;
        }
        this.renderTab(tabs, selectedTab, html, script, true);
        html.append("</ul>");
        html.append("</div>");
        this.autoarrangeScript.append("dashboard.arrange[0]=true;");
    }

    private void renderScript(StringBuffer html, PropertyList pageProperties, boolean workspace) {
        if (workspace) {
            PropertyListCollection plugins = new PropertyListCollection();
            PropertyList plugin = new PropertyList();
            plugin.setProperty("pluginid", "gridster");
            plugin.setProperty("css", "Y");
            plugin.setProperty("allowminimized", "Y");
            plugins.add(plugin);
            plugin = new PropertyList();
            plugin.setProperty("pluginid", "reveal");
            plugin.setProperty("css", "Y");
            plugin.setProperty("allowminimized", "Y");
            plugins.add(plugin);
            html.append(JavaScriptAPITag.getJQueryAPI(true, false, plugins, "", !this.devMode, this.pageContext));
            html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/workspace.css", this.pageContext) + "\">");
        } else {
            html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, "", !this.devMode, this.pageContext));
        }
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/modules/dashboard/scripts/dashboard.js\"></script>\n");
        html.append("<script  type=\"text/javascript\">\n");
        html.append("\tvar dashboard = new Dashboard();\n");
        String jsreq = pageProperties.getProperty("jsrequest", "").toLowerCase();
        if (jsreq.equals("full") || jsreq.indexOf("gizmo") > -1) {
            html.append("\tdashboard.propertyList = sapphire.page.request.data;\n");
        } else {
            this.logger.warn("The jsrequest was not set to allow element data to be rendered into the client side sapphire.page.request object and therefore the dashboard will not function correctly.");
            html.append("sapphire.events.attachEvent( window, 'onload', new Function( 'sapphire.ui.dialog.alert(\\'The sapphire.page.request object does not contain element data. The dashboard will not function correctly.\\')'));\n");
        }
        if (workspace) {
            html.append("\tdashboard.workspace = true;\n");
            html.append("sapphire.events.registerLoadListener( dashboard.grid.load, false, 500 );\n");
        }
        html.append("</script>\n");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/modules/webadmin/scripts/editors.js\"></script>");
        html.append("<script language=\"JavaScript\" src=\"WEB-CORE/modules/webadmin/scripts/propertyeditor.js\"></script>");
        html.append("<script language=\"JavaScript\">");
        html.append("function propertyChange(){}");
        html.append("</script>");
    }

    public void setParameters(JSONObject parameters) {
        if (parameters != null) {
            try {
                this.parameters = new PropertyList(parameters);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    public String getHtml() {
        PropertyList cu;
        Map in;
        StringBuffer html = new StringBuffer();
        this.script.setLength(0);
        this.script.append("sapphire.ui.animation.menuEnabled = false;");
        this.script.append("sapphire.ui.animation.resizeInOutEnabled = false;");
        this.script.append("sapphire.ui.panel.panels = new Array();");
        boolean workspace = this.element.getProperty(DISPLAYTYPE_PROPERTY, "").equalsIgnoreCase("workspace");
        if (this.completeRender) {
            this.renderScript(html, this.pageProperties, workspace);
            html.append("<div id=\"db__main\" ");
            if (workspace) {
                html.append("style=\"height: auto;width: auto;top: 5px;left: 5px;bottom: 0;position: absolute;\" ").append(">");
            } else {
                html.append("width=\"100%\" ").append("style=\"height:100%;\"").append(">\n");
            }
        }
        String useTabs = this.element.getProperty(USETABS_PROPERTY, "Y");
        Map map = this.request != null ? this.request.getParameterMap() : (in = this.pageContext != null ? this.pageContext.getRequest().getParameterMap() : null);
        if (this.parameters == null) {
            this.parameters = new PropertyList();
            PropertyListCollection items = new PropertyListCollection();
            this.parameters.setProperty("paramitems", items);
            if (in != null) {
                for (String key : in.keySet()) {
                    if (!(in.get(key) instanceof String[]) || key.equalsIgnoreCase("command") || key.equalsIgnoreCase("page") || key.equalsIgnoreCase("ajaxclass")) continue;
                    PropertyList param = new PropertyList();
                    param.setProperty("paramid", key);
                    param.setProperty("value", ((String[])in.get(key))[0].toString());
                    param.setProperty("datatype", EditorStyleField.getEditorStyleDataType("C"));
                    items.add(param);
                }
            }
        }
        if ((cu = this.parameters.getCollection("paramitems").find("paramid", "currentuser")) == null && this.connectionInfo != null) {
            cu = new PropertyList();
            cu.setProperty("paramid", "currentuser");
            cu.setProperty("value", this.connectionInfo.getSysuserId());
            cu.setProperty("datatype", EditorStyleField.getEditorStyleDataType("C"));
            this.parameters.getCollection("paramitems").add(cu);
        }
        if (workspace) {
            this.renderWorkspace(html, this.script, this.pageProperties);
        } else if (useTabs.equalsIgnoreCase("N")) {
            this.renderOnePage(html, this.script, this.pageProperties);
        } else {
            this.renderTabs(html, this.script, this.pageProperties);
        }
        this.script.append("sapphire.ui.panel.load();");
        this.script.append("dashboard.useTabs = ").append(!useTabs.equalsIgnoreCase("N")).append(";");
        this.script.append("dashboard.parameters = sapphire.util.propertyList.create(").append(this.parameters.toJSONString(false)).append(");");
        if (this.completeRender) {
            html.append("</div>");
            if (workspace) {
                html.append("<div id=\"ws_customize\" style=\"display:none;\">");
                Image image = new Image(this.pageContext);
                image.setImageId("Add");
                image.setDimensions(48, 48);
                image.setTitle(this.tp.translate("Add To Workspace"));
                html.append("<span id=\"btAdd\" onclick=\"dashboard.grid.buttons.add()\">");
                html.append(image.getHtml());
                html.append("</span>");
                image = new Image(this.pageContext);
                image.setImageId("UserRefresh");
                image.setDimensions(48, 48);
                image.setTitle(this.tp.translate("Reset Workspace"));
                html.append("<span id=\"btAdd\" onclick=\"dashboard.grid.buttons.reset()\">");
                html.append(image.getHtml());
                html.append("</span>");
                image = new Image(this.pageContext);
                image.setImageId("Gears");
                image.setDimensions(48, 48);
                image.setTitle(this.tp.translate("Customize Workspace"));
                html.append("<span id=\"btAdd\" onclick=\"dashboard.grid.buttons.customize()\">");
                html.append(image.getHtml());
                html.append("</span>");
                html.append("</div>");
                html.append("<div id=\"db_modalpopup\" class=\"reveal-modal\" style=\"height:400px;\">");
                html.append("<span id=\"db_modalpopup_content\">&nbsp;</span>");
                html.append("<a class=\"close-reveal-modal\">&#215;</a>");
                html.append("</div>");
            }
            html.append("<script>\n");
            html.append(this.getScript());
            if (this.autoarrange) {
                html.append(this.autoarrangeScript);
            }
            html.append("</script>\n");
        }
        return html.toString();
    }

    protected static String renderPreview(BaseGizmo baseGizmo, boolean small, int tabindex, String gizmoid) {
        StringBuffer html = new StringBuffer();
        html.append("<div class=\"ws_preview\" data_tabid=\"").append(tabindex).append("\" data_gizmoid=\"").append(gizmoid).append("\">");
        if (small) {
            baseGizmo.setGizmoStyle(BaseGizmo.GizmoStyle.LARGE);
            html.append(baseGizmo.getIconHtml());
        } else {
            baseGizmo.setGizmoStyle(BaseGizmo.GizmoStyle.LARGETEXT);
            html.append(baseGizmo.getIconHtml());
        }
        html.append("</div>");
        return html.toString();
    }

    private String renderGizmo(String gizmoid, int tabindex, int gizmocount, StringBuffer script, boolean workspace) {
        String out;
        block34: {
            out = "";
            PropertyList elementProps = this.pageProperties.getPropertyList(gizmoid);
            if (elementProps != null) {
                if (gizmoid.indexOf(" ") > -1) {
                    this.logger.warn("Gizmo " + gizmoid + " contains spaces and cannot be rendered.");
                } else {
                    String objectname = elementProps.getProperty(OBJECTNAME_PROPERTY);
                    try {
                        Object gizmoOb = Class.forName(objectname).newInstance();
                        if (gizmoOb instanceof BaseGizmo) {
                            BaseGizmo gizmoEl = (BaseGizmo)gizmoOb;
                            if (this.pageContext != null) {
                                gizmoEl.setPageContext(this.pageContext);
                                gizmoEl.setRequest(this.request != null ? this.request : (HttpServletRequest)this.pageContext.getRequest());
                            } else if (this.request != null) {
                                gizmoEl.setRequest(this.request);
                            }
                            gizmoEl.setConnectionId(this.getConnectionId());
                            gizmoEl.setElementid(gizmoid);
                            gizmoEl.setElementType(elementProps.getProperty(PROPERTYTREEID_PROPERTY, ""));
                            gizmoEl.setElementProperties(elementProps);
                            gizmoEl.setParameters(this.parameters);
                            gizmoEl.init();
                            gizmoEl.setBaseProperties();
                            int width = gizmoEl.getWidth();
                            int height = gizmoEl.getHeight();
                            boolean resizable = gizmoEl.getResizable();
                            boolean visible = gizmoEl.getVisible();
                            if (!visible) break block34;
                            if (gizmoEl.getTimeout() > 0) {
                                script.append("dashboard.startTimeout( ").append(tabindex).append(", '").append(gizmoid).append("' );");
                            }
                            PropertyList gizmoProps = elementProps.getPropertyList(GIZMOPROPS_PROPERTY);
                            if (workspace) {
                                int column;
                                int row;
                                if (gizmoProps.getProperty(WSX_PROPERTY).length() == 0 || gizmoProps.getProperty(WSY_PROPERTY).length() == 0) {
                                    double cols = 4.0;
                                    double rem = ((double)gizmocount + 1.0) / cols;
                                    row = (rem < Math.ceil(rem) ? (int)rem + 1 : (int)rem) - 0;
                                    column = (rem == 1.0 ? (int)cols : (int)((rem - Math.floor(rem)) * cols)) - 0;
                                } else {
                                    try {
                                        row = Integer.parseInt(gizmoProps.getProperty(WSY_PROPERTY));
                                        column = Integer.parseInt(gizmoProps.getProperty(WSX_PROPERTY));
                                    }
                                    catch (Exception e) {
                                        double cols = 4.0;
                                        double rem = ((double)gizmocount + 1.0) / cols;
                                        row = (rem < Math.ceil(rem) ? (int)rem + 1 : (int)rem) - 0;
                                        column = (rem == 1.0 ? (int)cols : (int)((rem - Math.floor(rem)) * cols)) - 0;
                                    }
                                }
                                width = (width + 50) / 100;
                                height = (height + 50) / 100;
                                StringBuffer workspaceHtml = new StringBuffer();
                                workspaceHtml.append("<li data-row=\"").append(row).append("\" data-col=\"").append(column).append("\" data-sizex=\"").append(width).append("\" data-sizey=\"").append(height).append("\" style=\"overflow:hidden;\" class=\"gs-w\" ");
                                workspaceHtml.append("id=\"ws_").append(tabindex).append("_").append(gizmoid).append("\" ");
                                workspaceHtml.append("gizmoid=\"").append(gizmoid).append("\" ");
                                workspaceHtml.append("tab=\"").append(tabindex).append("\" ");
                                workspaceHtml.append("gizmotimeout=\"").append(gizmoEl.getTimeout()).append("\" ");
                                workspaceHtml.append(">");
                                workspaceHtml.append("<header>").append(this.translate(gizmoProps.getProperty(TITLE_PROPERTY, ""))).append("</header>");
                                workspaceHtml.append("<div class=\"ws_outer\">");
                                workspaceHtml.append("<div id=\"ws_contents_").append(tabindex).append("_").append(gizmoid).append("\" class=\"ws_inner\">");
                                if (width < 3 && height < 2) {
                                    if (width < 2) {
                                        workspaceHtml.append(Dashboard.renderPreview(gizmoEl, true, tabindex, gizmoid));
                                    } else {
                                        workspaceHtml.append(Dashboard.renderPreview(gizmoEl, false, tabindex, gizmoid));
                                    }
                                } else {
                                    workspaceHtml.append(gizmoEl.getHtml());
                                }
                                if (this.completeRender) {
                                    workspaceHtml.append("<script>" + gizmoEl.getScript() + "</script>");
                                } else {
                                    script.append(gizmoEl.getScript());
                                }
                                workspaceHtml.append("</div>");
                                workspaceHtml.append("<div style=\"display:none;\" class=\"ws_gizmo_customize\">");
                                workspaceHtml.append("<div class=\"ws_gizmo_pseudo\">");
                                workspaceHtml.append("</div>");
                                Image im = new Image(this.pageContext);
                                im.setTitle("Remove Gizmo");
                                im.setImageId("Delete");
                                im.setDimensions(48, 48);
                                workspaceHtml.append("<div class=\"ws_gizmo_btn\" onclick=\"dashboard.grid.buttons.remove(").append(tabindex).append(",'").append(gizmoid).append("')\">");
                                workspaceHtml.append(im.getHtml());
                                workspaceHtml.append("</div>");
                                im = new Image(this.pageContext);
                                im.setTitle("Edit Gizmo Options");
                                im.setImageId("GearEdit");
                                im.setDimensions(48, 48);
                                workspaceHtml.append("<div class=\"ws_gizmo_btn\" onclick=\"dashboard.grid.buttons.options(").append(tabindex).append(",'").append(gizmoid).append("')\">");
                                workspaceHtml.append(im.getHtml());
                                workspaceHtml.append("</div>");
                                workspaceHtml.append("</div>");
                                workspaceHtml.append("<div style=\"display:none;\" class=\"ws_gizmo_cover\">");
                                workspaceHtml.append("</div>");
                                workspaceHtml.append("</div>");
                                workspaceHtml.append("</li>");
                                out = workspaceHtml.toString();
                                break block34;
                            }
                            String gizmoHTML = gizmoEl.getHtml();
                            String zIndex = gizmoProps.getProperty(ZINDEX_PROPERTY, "0");
                            if (gizmoProps.getProperty(X_PROPERTY).length() == 0 || gizmoProps.getProperty(Y_PROPERTY).length() == 0) {
                                this.autoarrange = true;
                            }
                            String left = gizmoProps.getProperty(X_PROPERTY, "" + (10 + 50 * gizmocount));
                            String top = gizmoProps.getProperty(Y_PROPERTY, "" + (10 + 50 * gizmocount));
                            int l = 0;
                            try {
                                l = Integer.parseInt(left);
                                if (l < 0) {
                                    left = "0";
                                }
                            }
                            catch (NumberFormatException rem) {
                                // empty catch block
                            }
                            int t = 0;
                            try {
                                t = Integer.parseInt(top);
                                if (t < 0) {
                                    top = "0";
                                }
                            }
                            catch (NumberFormatException numberFormatException) {
                                // empty catch block
                            }
                            Panel panel = new Panel();
                            panel.setId(tabindex + "_" + gizmoid);
                            panel.setBrowser(this.browser);
                            panel.setSnapDrag(25, 25);
                            panel.setSnapResize(25, 25);
                            panel.setHideTitleBar(this.element.getProperty(HIDEGIZMOTITLEBAR_PROPERTY, "N").equalsIgnoreCase("Y"));
                            if (gizmoEl.getUserProperties().size() > 0) {
                                panel.addButton("WEB-CORE/modules/dashboard/images/SettingsButton.gif", "dashboard.manageGizmo(" + tabindex + ", '" + gizmoid + "' )", this.translate("Open options for Gizmo") + " " + gizmoid);
                            }
                            panel.addButton("WEB-CORE/modules/dashboard/images/RefreshButton.gif", "dashboard.refreshGizmo( " + tabindex + ", '" + gizmoid + "' )", this.translate("Refresh Gizmo") + " " + gizmoid);
                            panel.addButton("WEB-CORE/modules/dashboard/images/ResetButton.gif", "dashboard.resetGizmo( " + tabindex + ", '" + gizmoid + "' )", this.translate("Reset Gizmo") + " " + gizmoid);
                            panel.addButton("WEB-CORE/modules/dashboard/images/RemoveButton.gif", "dashboard.removeGizmo(" + tabindex + ", '" + gizmoid + "' )", this.translate("Remove Gizmo") + " " + gizmoid);
                            panel.addCustomAttribute(GIZMOID_PROPERTY, gizmoid);
                            panel.addCustomAttribute("tab", "" + tabindex);
                            panel.addCustomAttribute("zindexchanged", "dashboard.changeGizmoZIndex");
                            panel.setResizable("" + resizable);
                            panel.setRegister("false");
                            panel.setDragable("true");
                            panel.setDropEvent("dashboard.dropGizmo");
                            if (gizmoEl.getRefreshOnResize()) {
                                panel.setResizeEvent("dashboard.resizeRefreshGizmo");
                            } else {
                                panel.setResizeEvent("dashboard.resizeGizmo");
                            }
                            script.append(panel.getRegisterScript());
                            panel.addCustomAttribute("gizmotimeout", "" + gizmoEl.getTimeout());
                            panel.setText(this.translate(gizmoProps.getProperty(TITLE_PROPERTY, "")));
                            panel.setBodyheight("" + height);
                            panel.setBodywidth("" + width);
                            panel.setTop(top);
                            panel.setLeft(left);
                            panel.setZIndex(zIndex);
                            if (this.completeRender) {
                                panel.setContent(gizmoHTML + "<script>" + gizmoEl.getScript() + "</script>");
                            } else {
                                panel.setContent(gizmoHTML);
                                script.append(gizmoEl.getScript());
                            }
                            out = panel.getHtml();
                            break block34;
                        }
                        this.logger.warn("The element '" + gizmoid + "' uses class '" + objectname + "' and is not a Gizmo element.");
                    }
                    catch (Exception e) {
                        this.logger.warn("Could not find the class '" + objectname + "' element '" + gizmoid + "'.");
                    }
                }
            } else {
                this.logger.warn("Gizmo element " + gizmoid + " not found in page.");
            }
        }
        return out;
    }
}

