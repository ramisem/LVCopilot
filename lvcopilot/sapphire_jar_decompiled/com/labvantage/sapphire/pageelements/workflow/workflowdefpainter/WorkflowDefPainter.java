/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.propertybuilder.PropertyBuilder;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefPainter;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflow;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefProperties;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.GradientImageRequest;
import com.labvantage.sapphire.util.images.ImageRef;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowDefPainter
extends BaseElement {
    public static final String CACHE_WORKFLOWOBJECTS = "workflow_objects_pd";
    public static final String USERCONFIG_PREFIX = "workflow_";
    public static final String JS_CLASS = "workflow";
    public static final String SESSION_PREFIX = "workflow_";
    public static final String WINDOW_PREFIX = "painter_window_";
    public static final String ELEMENT_PREFIX = "painter_element_";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_ZOOM = "zoom";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_PAINTERONLY = "painteronly";
    public static final String PROPERTY_HIDEELEMENTS = "hideelements";
    public static final String PROPERTY_CONNECTOR = "connector";
    public static final String PROPERTY_RENDERHTML5 = "renderhtml5";
    public static final String PROPERTY_INHERITHTML5 = "inherithtml5";
    public static final String PROPERTY_APPEARANCE = "appearance";
    public static final String PROPERTY_COLOR = "color";
    public static final String PROPERTY_PROPERTYCONTAINER = "propertycontainer";
    public static final String PROPERTY_EMBEDDED = "embedded";
    public static final String PROPERTY_SHOWQUEUES = "showqueues";
    public static final String SDCID = "LV_WorkflowDef";
    public static final String DATACOL = "workflowdef";
    public static final int TASKDEFITEMIDMAXLEN = 40;
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String sdcid = "LV_WorkflowDef";
    private PropertyList userConfig;
    private int zoom = 100;
    private boolean viewonly = false;
    private boolean painteronly = false;
    private boolean hideelements = false;
    private boolean devMode;
    private PropertyList workflowprops = null;
    private PropertyList toolprops = null;
    private TaskDefWorkflow.Appearance forcedAppearance = null;
    private String[] forcedColor = null;
    private String propertyContainer = "";
    private boolean renderHtml5 = false;
    private boolean showqueues = false;
    private boolean print = false;
    private boolean translate = false;
    private boolean rtlFlag = false;

    public WorkflowDefPainter(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            this.setUpProperties(pageproperties, (HttpServletRequest)pageContext.getRequest());
            if (this.workflowprops == null) {
                PropertyList pbprops = new PropertyList();
                pbprops.setProperty("definitionxml", "workflowdef.xml");
                PropertyBuilder pb = new PropertyBuilder(pageContext, "workflowdef.xml");
                PropertyDefinitionList propertyDef = pb.getPropertyDefinition();
                this.workflowprops = WorkflowDefMaint.getWorkflowData(this.sdcid, this.keyid1, this.keyid2, this.keyid3, false, propertyDef, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger, this.showqueues, this.painteronly);
            }
        }
        catch (Exception e) {
            this.workflowprops = null;
            this.logger.error("Could not set up painter: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        pagedata.setProperty("jsrequest", "exclude=properties");
        this.renderHtml5 = pagedata.getProperty(PROPERTY_RENDERHTML5, "N").equalsIgnoreCase("Y");
        String show = pagedata.getProperty("showtitle", "N");
        this.logger.debug("show = " + show);
        this.showqueues = pagedata.getProperty(PROPERTY_SHOWQUEUES, "N").equalsIgnoreCase("Y");
        this.logger.debug("showqueues = " + this.showqueues);
        PropertyList layout = pagedata.getPropertyList("layout");
        if (layout != null) {
            layout.setProperty("hideshadow", "Y");
            if (show.equalsIgnoreCase("N")) {
                layout.setProperty("hidetitle", "Y");
            } else {
                layout.setProperty("hidetitle", "N");
            }
        }
        this.keyid1 = pagedata.getProperty(PROPERTY_KEYID1, "");
        this.logger.debug("keyid1 = " + this.keyid1);
        this.keyid2 = pagedata.getProperty(PROPERTY_KEYID2, "");
        this.logger.debug("keyid2 = " + this.keyid2);
        this.keyid3 = pagedata.getProperty(PROPERTY_KEYID3, "");
        this.logger.debug("keyid3 = " + this.keyid3);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.painteronly = pagedata.getProperty(PROPERTY_PAINTERONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("painteronly = " + this.painteronly);
        this.hideelements = pagedata.getProperty(PROPERTY_HIDEELEMENTS, "n").equalsIgnoreCase("y");
        this.logger.debug("hideelements = " + this.hideelements);
        this.propertyContainer = pagedata.getProperty(PROPERTY_PROPERTYCONTAINER, "");
        this.logger.debug("propertyContainer = " + this.propertyContainer);
        String workp = pagedata.getProperty(PROPERTY_PROPERTIES, "");
        if (workp.length() > 0) {
            try {
                this.workflowprops = new PropertyList(new JSONObject(workp));
            }
            catch (Exception e) {
                this.workflowprops = null;
                this.logger.warn("Invalid property definition provided.");
            }
        }
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList("LV_TaskDef");
        this.toolprops = TaskDefMaint.getTasksData(true, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        if (pagedata.getProperty(PROPERTY_APPEARANCE, "").length() > 0) {
            try {
                this.forcedAppearance = TaskDefWorkflow.Appearance.valueOf(pagedata.getProperty(PROPERTY_APPEARANCE, "").toUpperCase());
            }
            catch (Exception e) {
                this.forcedAppearance = null;
            }
        } else {
            this.forcedAppearance = null;
        }
        this.forcedColor = pagedata.getProperty(PROPERTY_COLOR, "").length() > 0 ? StringUtil.split(pagedata.getProperty(PROPERTY_COLOR, ""), ";") : null;
        if (this.requestContext.getProperty("print").equalsIgnoreCase("Y")) {
            this.print = true;
        }
        if (this.requestContext.getProperty("translate").equalsIgnoreCase("Y")) {
            this.translate = true;
        }
        this.rtlFlag = this.connectionInfo.isRtl();
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig == null) {
            throw new SapphireException("User configuration could not be obtained.");
        }
        if (!this.print) {
            try {
                this.zoom = Integer.parseInt(pagedata.getProperty(PROPERTY_ZOOM, this.userConfig.getProperty("workflow_zoom", "100")));
            }
            catch (Exception e) {
                this.zoom = 100;
            }
        } else {
            this.zoom = 100;
        }
    }

    public static String getToolsHtml(PropertyList props, PropertyList toolprops, String filterText, boolean standalone, String connectionId, TranslationProcessor tp, M18NUtil m18n, PropertyList userConfig, String userconfigPrefix, HttpServletRequest request, Logger logger) {
        String[] filterBits;
        ToolGrouping grouping;
        RequestContext rc;
        StringBuffer sb = new StringBuffer();
        PropertyListCollection tools = toolprops.getCollection("tasks");
        if (standalone) {
            PropertyListCollection temp = new PropertyListCollection();
            for (int i = 0; i < tools.size(); ++i) {
                PropertyList pl = tools.getPropertyList(i);
                if (!pl.getProperty("standaloneflag").equalsIgnoreCase("Y")) continue;
                temp.add(pl);
            }
            tools = temp;
        }
        if (userConfig == null && request != null && (rc = RequestContext.getInstance(request)) != null) {
            if (connectionId == null || connectionId.length() == 0) {
                connectionId = rc.getConnectionId();
            }
            userConfig = rc.getPropertyList("userconfig");
        }
        boolean od = false;
        try {
            od = userConfig.getProperty(userconfigPrefix + "toolsorderby", "").equalsIgnoreCase("moddt");
        }
        catch (Exception e) {
            od = false;
        }
        final boolean orderByDate = od;
        try {
            grouping = ToolGrouping.valueOf(userConfig.getProperty(userconfigPrefix + "toolsgroupby", ToolGrouping.NONE.toString()).toUpperCase());
        }
        catch (Exception e) {
            grouping = ToolGrouping.NONE;
        }
        Comparator<String> comparator = new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        };
        Comparator<Object[]> advComparator = new Comparator<Object[]>(){

            @Override
            public int compare(Object[] o1, Object[] o2) {
                if (orderByDate) {
                    Calendar cal2 = (Calendar)o2[1];
                    Calendar cal1 = (Calendar)o1[1];
                    int comp = cal2.compareTo(cal1);
                    if (comp == 0) {
                        return o1[0].toString().compareToIgnoreCase(o2[0].toString());
                    }
                    return comp;
                }
                return o1[0].toString().compareToIgnoreCase(o2[0].toString());
            }
        };
        TreeMap<String, TreeMap> groups = new TreeMap<String, TreeMap>(comparator);
        if (filterText != null && filterText.length() > 0) {
            filterText = filterText.toLowerCase();
            filterBits = StringUtil.split(filterText, " ");
        } else {
            filterText = null;
            filterBits = null;
        }
        String none = "__NONE";
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            int count = 0;
            for (int i = 0; i < tools.size(); ++i) {
                String[] grouparray;
                String text;
                PropertyList tool = tools.getPropertyList(i);
                String taskid = tool.getProperty("taskdefid", "") + ";" + tool.getProperty("taskdefversionid") + ";" + tool.getProperty("taskdefvariantid", "");
                if (tool.getProperty("basedontaskdefid").length() > 0 && (tool.getProperty("shorttitle").length() == 0 || tool.getProperty("icon").length() == 0)) {
                    for (int t = 0; t < tools.size(); ++t) {
                        PropertyList basedOnTask = tools.getPropertyList(t);
                        if (!basedOnTask.getProperty("taskdefid", "").equals(tool.getProperty("basedontaskdefid")) || !basedOnTask.getProperty("taskdefversionid", "").equals(tool.getProperty("basedontaskdefversionid")) || !basedOnTask.getProperty("taskdefvariantid", "").equals(tool.getProperty("basedontaskdefvariantid"))) continue;
                        if (tool.getProperty("shorttitle").length() == 0) {
                            tool.setProperty("shorttitle", basedOnTask.getProperty("shorttitle"));
                        }
                        if (tool.getProperty("icon").length() != 0) break;
                        tool.setProperty("icon", basedOnTask.getProperty("icon"));
                        break;
                    }
                }
                if ((text = tool.getProperty("shorttitle", tool.getProperty("longtitle", ""))).length() <= 0) continue;
                if (filterBits != null) {
                    boolean found = true;
                    for (String s : filterBits) {
                        if (text.toLowerCase().contains(s)) continue;
                        found = false;
                    }
                    if (!found) continue;
                }
                if (!tool.getProperty("taskdefvariantid", "1").equals("1")) {
                    text = text + " (" + tool.getProperty("taskdefvariantid", "") + ")\u200e";
                }
                boolean output = false;
                switch (grouping) {
                    case GROUP: {
                        grouparray = new String[]{tool.getProperty("groupid", none)};
                        break;
                    }
                    case CATEGORY: {
                        PropertyListCollection cats = tool.getCollection("categories");
                        if (cats != null && cats.size() > 0) {
                            ArrayList<String> grouplist = new ArrayList<String>();
                            for (int c = 0; c < cats.size(); ++c) {
                                PropertyList cat = cats.getPropertyList(c);
                                grouplist.add(cat.getProperty("categoryid", none));
                            }
                            grouparray = new String[grouplist.size()];
                            grouplist.toArray(grouparray);
                            break;
                        }
                        grouparray = new String[]{none};
                        break;
                    }
                    case VARIANT: {
                        grouparray = new String[]{tool.getProperty("taskdefvariantid")};
                        break;
                    }
                    case TASK: {
                        grouparray = new String[]{tool.getProperty("taskdefid")};
                        break;
                    }
                    case PARENT: {
                        if (tool.getProperty("basedontaskdefid").length() > 0) {
                            String g = tool.getProperty("basedontaskdefid") + " (" + tool.getProperty("basedontaskdefversionid") + " - " + tool.getProperty("basedontaskdefvariantid") + ")";
                            grouparray = new String[]{g};
                            break;
                        }
                        grouparray = new String[]{none};
                        break;
                    }
                    case OUTPUT: {
                        output = true;
                    }
                    case INPUT: {
                        PropertyListCollection ios = tool.getCollection("taskio");
                        if (ios != null && ios.size() > 0) {
                            ArrayList<String> grouplist = new ArrayList<String>();
                            boolean found = false;
                            for (int c = 0; c < ios.size(); ++c) {
                                String conType;
                                PropertyList io = ios.getPropertyList(c);
                                if (!io.getProperty("ioflag", "O").equalsIgnoreCase(output ? "O" : "I") || grouplist.contains(conType = io.getProperty("connectortypeid", none))) continue;
                                found = true;
                                grouplist.add(conType);
                            }
                            if (found) {
                                grouparray = new String[grouplist.size()];
                                grouplist.toArray(grouparray);
                                break;
                            }
                            grouparray = new String[]{none};
                            break;
                        }
                        grouparray = new String[]{none};
                        break;
                    }
                    default: {
                        grouparray = new String[]{none};
                    }
                }
                for (int g = 0; g < grouparray.length; ++g) {
                    TreeMap item;
                    String group = grouparray[g];
                    if (groups.size() == 0 || !groups.containsKey(group)) {
                        item = new TreeMap(advComparator);
                        groups.put(group, item);
                    } else {
                        item = (TreeMap)groups.get(group);
                    }
                    String toolkey = tool.getProperty("taskkey", tool.getProperty("taskdefid"));
                    StringBuffer current = new StringBuffer();
                    Image img = new Image();
                    img.setConnectionId(connectionId);
                    img.setImageSrc(tool.getProperty("icon", "WEB-CORE/elements/workflow/images/NoImage.png"));
                    img.setDimensions(16, 16);
                    current.append("<div id=\"painter_tool").append(count).append("_container\" class=\"painter_tool_container\" title=\"").append(tool.getProperty("taskdefid")).append(" (").append(tool.getProperty("taskdefversionid")).append(" - ").append(tool.getProperty("taskdefvariantid")).append(") - ").append(tool.getProperty("longtitle", tool.getProperty("shorttitle"))).append("\">");
                    current.append("<div class=\"painter_tool").append("\" id=\"painter_tool").append(count).append("\" tooltype=\"task\" toolid=\"").append(taskid).append("\"><div class=\"painter_toolimg").append("\">").append(img.getHtml()).append("</div><div class=\"painter_toollabel").append("\">").append(text).append("</div></div>");
                    current.append("</div>");
                    item.put(new Object[]{toolkey, m18n.parseCalendar(tool.getProperty("moddt"))}, current);
                    ++count;
                }
            }
            if (groups.size() > 0) {
                sb.append("<div id=\"painter_objects_accordian\">");
            }
            StringBuffer end = new StringBuffer();
            for (String groupKey : groups.keySet()) {
                String grouptext;
                StringBuffer current;
                if (groupKey.equals(none)) {
                    current = end;
                    switch (grouping) {
                        case GROUP: {
                            grouptext = "Ungrouped";
                            break;
                        }
                        case CATEGORY: {
                            grouptext = "Uncategorized";
                            break;
                        }
                        case VARIANT: {
                            grouptext = "No Variants";
                            break;
                        }
                        case TASK: {
                            grouptext = "No Tasks";
                            break;
                        }
                        case PARENT: {
                            grouptext = "No Parent";
                            break;
                        }
                        case INPUT: {
                            grouptext = "No Input Queues";
                            break;
                        }
                        case OUTPUT: {
                            grouptext = "No Output Queues";
                            break;
                        }
                        default: {
                            grouptext = "All Tasks";
                            break;
                        }
                    }
                } else {
                    current = sb;
                    grouptext = groupKey;
                }
                current.append("<div>");
                TreeMap tmgroup = (TreeMap)groups.get(groupKey);
                current.append("<h3><a href=\"#\">").append(grouptext).append(" <span style=\"color:grey\">(").append(tmgroup.size()).append(")\u200e</span></a></h3>");
                current.append("<div style=\"padding-bottom:8px;\" group=\"" + groupKey + "\">");
                for (Map.Entry entry : tmgroup.entrySet()) {
                    current.append(((StringBuffer)entry.getValue()).toString());
                }
                current.append("</div>");
                current.append("</div>");
            }
            if (end.length() > 0) {
                sb.append(end);
            }
            if (groups.size() > 0) {
                sb.append("</div>");
            } else if (filterText != null && filterText.length() > 0) {
                sb.append("<p class=\"filter_text\">No Tasks Found</p>");
            } else {
                sb.append("<p class=\"filter_text\">No Tasks Available</p>");
            }
        } else {
            String msg = "No connection Id provided.";
            if (tp != null) {
                msg = tp.translate(msg);
            }
            sb.append(msg);
        }
        return sb.toString();
    }

    public static String getElementsHTML(PropertyList props, String connectionId, TranslationProcessor tp, PropertyList userConfig, Logger logger) {
        StringBuffer sb = new StringBuffer();
        Comparator<String> comparator = new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        };
        TreeMap<String, StringBuffer> groups = new TreeMap<String, StringBuffer>(comparator);
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            int count = 0;
            for (Tools tool : Tools.values()) {
                StringBuffer current;
                String toolid = tool.toString().toLowerCase();
                String text = tool.getTitle();
                if (text.length() <= 0) continue;
                boolean output = false;
                String group = tool.getGroup();
                if (groups.size() == 0 || !groups.containsKey(group)) {
                    current = new StringBuffer();
                    groups.put(group, current);
                } else {
                    current = (StringBuffer)groups.get(group);
                }
                Image img = new Image();
                img.setConnectionId(connectionId);
                img.setImageSrc(tool.getImage());
                img.setDimensions(16, 16);
                current.append("<div id=\"painter_tool").append(count).append("_container\" class=\"painter_tool_container\">");
                current.append("<div class=\"painter_tool").append("\" id=\"painter_element").append(count).append("\" tooltype=\"element\" toolid=\"").append(toolid).append("\"><div class=\"painter_toolimg").append("\">").append(img.getHtml()).append("</div><div class=\"painter_toollabel").append("\">").append(text).append("</div></div>");
                current.append("</div>");
                ++count;
            }
            if (groups.size() > 0) {
                sb.append("<div id=\"painter_tools_accordian\">");
            }
            StringBuffer end = new StringBuffer();
            for (String group : groups.keySet()) {
                sb.append("<div>");
                sb.append("<h3><a href=\"#\">").append(group).append("</a></h3>");
                sb.append("<div style=\"padding-bottom:8px;\" group=\"" + group + "\">");
                sb.append((StringBuffer)groups.get(group));
                sb.append("</div>");
                sb.append("</div>");
            }
            if (end.length() > 0) {
                sb.append(end);
            }
            if (groups.size() > 0) {
                sb.append("</div>");
            } else {
                sb.append("<p class=\"painter_noelements\">No Tools Available</p>");
            }
        } else {
            String msg = "No connection Id provided.";
            if (tp != null) {
                msg = tp.translate(msg);
            }
            sb.append(msg);
        }
        return sb.toString();
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        PropertyList plugin = new PropertyList();
        plugin.setProperty("pluginid", "jsplumb");
        plugin.setProperty("css", "Y");
        plugin.setProperty("allowminimized", "Y");
        PropertyListCollection plugins = new PropertyListCollection();
        plugins.add(plugin);
        html.append(JavaScriptAPITag.getJQueryAPI(true, false, plugins, "", !this.devMode, this.pageContext));
        html.append("<link rel=\"stylesheet\" href=\"" + com.labvantage.sapphire.util.http.HttpUtil.getCSS("WEB-CORE/elements/workflow/stylesheets/painter.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<link rel=\"stylesheet\" href=\"" + com.labvantage.sapphire.util.http.HttpUtil.getCSS("WEB-CORE/elements/workflow/stylesheets/workflow.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/workflow").append("").append(".js\"></script>");
        html.append("<style id=\"painter_workflow_style\">");
        if (this.print && this.browser.isWebkit()) {
            html.append("@media print{@page {size: landscape;}}");
        }
        html.append(".painter_element{display:").append(this.hideelements ? "none" : "block").append(";}");
        if (this.rtlFlag) {
            html.append(".ui-accordion .ui-accordion-header .ui-accordion-header-icon{ right:.5em; background-position-x: -91px; }");
            html.append("div.painter_leftbar_expand_nonie{right:-50px;}");
        }
        html.append("</style>");
        if (this.print) {
            html.append("<script>");
            html.append("sapphire.events.attachEvent(window,'load',workflow.print);");
            html.append("</script>");
        }
        return html;
    }

    private StringBuffer getEndScript(PropertyList props, String sdcid, String keyid1, String keyid2, String keyid3, String propertyContainer, boolean viewOnly, boolean painteronly, boolean hideelements, int zoom) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        if (hideelements) {
            // empty if block
        }
        if (propertyContainer.length() > 0) {
            html.append(JS_CLASS).append(".properties=").append(propertyContainer).append(";");
        } else {
            html.append(JS_CLASS).append(".properties=sapphire.util.propertyList.create(").append(props.toJSONString(false)).append(");");
        }
        html.append(JS_CLASS).append(".rtl=").append(this.rtlFlag).append(";");
        html.append(JS_CLASS).append(".viewonly=").append(viewOnly).append(";");
        html.append(JS_CLASS).append(".painteronly=").append(painteronly).append(";");
        html.append(JS_CLASS).append(".translate=").append(this.translate).append(";");
        html.append(JS_CLASS).append(".sdcid='").append(sdcid).append("';");
        html.append(JS_CLASS).append(".keyid1='").append(keyid1).append("';");
        html.append(JS_CLASS).append(".keyid2='").append(keyid2).append("';");
        html.append(JS_CLASS).append(".keyid3='").append(keyid3).append("';");
        html.append(JS_CLASS).append(".zoom=").append(this.browser.isIE() || this.browser.isEdge() ? 100 : zoom).append(";");
        if (this.forcedColor != null) {
            html.append(JS_CLASS).append(".color='").append(StringUtil.arrayToString(this.forcedColor, ";")).append("';");
        }
        if (this.forcedAppearance != null) {
            html.append(JS_CLASS).append(".appearance='").append(this.forcedAppearance.toString().toLowerCase()).append("';");
        }
        html.append(JS_CLASS).append(".elementIndex=").append(TaskDefPainter.getNextIdIndex(props.getCollection("elements"), -1, "elementid", "element")).append(";");
        String toolgroups = this.userConfig.getProperty("workflow_toolgroups", "{}");
        if (toolgroups.length() == 0 || !toolgroups.startsWith("{") || !toolgroups.endsWith("}")) {
            toolgroups = "{}";
        }
        html.append(JS_CLASS).append(".toolGroups=").append(toolgroups).append(";");
        html.append(JS_CLASS).append(".toolGroup='").append(this.userConfig.getProperty("workflow_toolsgroupby", ToolGrouping.NONE.toString()).toLowerCase()).append("';");
        html.append(JS_CLASS).append(".toolSort='").append(this.userConfig.getProperty("workflow_toolsorderby", "").toLowerCase()).append("';");
        html.append("</script>");
        return html;
    }

    private static String getProperty(PropertyList taskprops, PropertyList toolprops, String propertyid, String defaultValue) {
        return taskprops.getProperty(propertyid, toolprops.getProperty(propertyid, defaultValue));
    }

    private static PropertyListCollection getCollection(PropertyList taskprops, PropertyList toolprops, String propertyid) {
        if (taskprops.getCollection(propertyid) != null) {
            return taskprops.getCollection(propertyid);
        }
        return toolprops.getCollection(propertyid);
    }

    private static ArrayList<PropertyList> findAllInCollection(PropertyListCollection collection, String propertyId, String value) {
        ArrayList<PropertyList> out = new ArrayList<PropertyList>();
        if (collection != null) {
            for (int i = 0; i < collection.size(); ++i) {
                PropertyList current = collection.getPropertyList(i);
                String propvalue = current.getProperty(propertyId, "");
                if (!propvalue.equals(value)) continue;
                out.add(current);
            }
        }
        return out;
    }

    protected static boolean copyAccrossProperties(PropertyDefinitionList def, PropertyList propTo, PropertyList propFrom) {
        boolean changed = false;
        for (int i = 0; i < def.size(); ++i) {
            PropertyList toitem;
            PropertyList fromitem;
            String prop;
            HashMap att;
            PropertyDefinition pd = (PropertyDefinition)def.get(i);
            if (pd.getType().equalsIgnoreCase("simple")) {
                att = pd.getAttributes();
                if (!att.containsKey("copydown") || !att.get("copydown").toString().equalsIgnoreCase("Y")) continue;
                prop = pd.getId();
                if (att.containsKey("overridable") && att.get("overridable").toString().equalsIgnoreCase("Y")) {
                    if (!propFrom.containsKey(prop)) continue;
                    if (!propTo.containsKey(prop)) {
                        propTo.setProperty(prop, propFrom.getProperty(prop, ""));
                        continue;
                    }
                    if (propTo.getProperty(prop).length() <= 0 || propTo.getProperty(prop).equals(propFrom.getProperty(prop))) continue;
                    changed = true;
                    continue;
                }
                if (!propFrom.containsKey(prop)) continue;
                propTo.setProperty(prop, propFrom.getProperty(prop, ""));
                continue;
            }
            if (pd.getType().equalsIgnoreCase("collection")) {
                PropertyListCollection tocol;
                PropertyListCollection fromcol;
                att = pd.getAttributes();
                if (!att.containsKey("copydown") || !att.get("copydown").toString().equalsIgnoreCase("Y") || !propFrom.containsKey(prop = pd.getId()) || (fromcol = propFrom.getCollection(prop)) == null) continue;
                String idprop = pd.getPropertyDefinitionList().size() > 0 ? ((PropertyDefinition)pd.getPropertyDefinitionList().get(0)).getId() : "id";
                PropertyListCollection newtocol = new PropertyListCollection();
                if (propTo.containsKey(prop)) {
                    tocol = propTo.getCollection(prop);
                    propTo.remove(prop);
                } else {
                    tocol = new PropertyListCollection();
                }
                newtocol.setId(fromcol.getId());
                for (int k = 0; k < fromcol.size(); ++k) {
                    PropertyList fromitem2 = fromcol.getPropertyList(k);
                    ArrayList<PropertyList> toitemArray = WorkflowDefPainter.findAllInCollection(tocol, idprop, fromitem2.getProperty(idprop, ""));
                    if (toitemArray.size() == 0) {
                        PropertyList toitem2 = new PropertyList();
                        toitem2.setId(fromitem2.getId());
                        toitem2.setProperty(idprop, fromitem2.getProperty(idprop, ""));
                        newtocol.add(toitem2);
                        if (!WorkflowDefPainter.copyAccrossProperties(pd.getPropertyDefinitionList(), toitem2, fromitem2)) continue;
                        changed = true;
                        continue;
                    }
                    for (PropertyList toitem3 : toitemArray) {
                        newtocol.add(toitem3);
                        if (!WorkflowDefPainter.copyAccrossProperties(pd.getPropertyDefinitionList(), toitem3, fromitem2)) continue;
                        changed = true;
                    }
                }
                propTo.setProperty(prop, newtocol);
                continue;
            }
            if (!pd.getType().equalsIgnoreCase("propertylist") || !(att = pd.getAttributes()).containsKey("copydown") || !att.get("copydown").toString().equalsIgnoreCase("Y") || (fromitem = propFrom.getPropertyList(prop = pd.getId())) == null) continue;
            if (propTo.containsKey(prop)) {
                toitem = propTo.getPropertyList(prop);
            } else {
                toitem = new PropertyList();
                propTo.setProperty(prop, toitem);
            }
            if (!WorkflowDefPainter.copyAccrossProperties(pd.getPropertyDefinitionList(), toitem, fromitem)) continue;
            changed = true;
        }
        return changed;
    }

    protected static String renderWindow(PropertyList taskprops, PropertyList toolprops, PropertyList workflowprops, PropertyDefinitionList propertydef, Connector connScript, TaskDefWorkflow.Appearance forcedAppearance, String[] forcedColor, int index, StringBuffer html, StringBuffer script, StringBuffer style, boolean visible, boolean viewonly, boolean html5, boolean isSavedInRTL, Browser browser, SDIProcessor sdi, SapphireConnection sapphireConnection, TranslationProcessor tp, Logger logger) {
        String taskid;
        PropertyListCollection taskios;
        String icon;
        String shorttitle;
        String longtitle;
        String webtaskvariantid;
        String webtaskversionid;
        String webtaskid;
        boolean prototype = taskprops != null && taskprops.getProperty("prototype", "N").equalsIgnoreCase("Y");
        boolean modified = false;
        if (!prototype && propertydef.getPropertyDef("tasks") != null) {
            PropertyDefinitionList tasksdef = propertydef.getPropertyDef("tasks").getPropertyDefinitionList();
            modified = WorkflowDefPainter.copyAccrossProperties(tasksdef, taskprops, toolprops);
        }
        if (prototype) {
            webtaskid = taskprops.getProperty("taskdefid", "");
            webtaskversionid = taskprops.getProperty("taskdefversionid", "");
            webtaskvariantid = taskprops.getProperty("taskdefvariantid", "");
            longtitle = taskprops.getProperty("longtitle", "Prototype Task");
            shorttitle = taskprops.getProperty("shorttitle", "Empty Task");
            icon = taskprops.getProperty("icon", "rc?command=image&image=DrawingUtensils&size=32");
            taskios = taskprops.getCollection("taskio");
            taskid = taskprops.getProperty("taskdefitemid", webtaskid);
        } else {
            webtaskid = WorkflowDefPainter.getProperty(taskprops, toolprops, "taskdefid", "");
            webtaskversionid = WorkflowDefPainter.getProperty(taskprops, toolprops, "taskdefversionid", "");
            webtaskvariantid = WorkflowDefPainter.getProperty(taskprops, toolprops, "taskdefvariantid", "");
            longtitle = WorkflowDefPainter.getProperty(taskprops, toolprops, "longtitle", "");
            shorttitle = WorkflowDefPainter.getProperty(taskprops, toolprops, "shorttitle", "");
            icon = WorkflowDefPainter.getProperty(taskprops, toolprops, "icon", "");
            taskios = WorkflowDefPainter.getCollection(taskprops, toolprops, "taskio");
            taskid = taskprops.getProperty("taskdefitemid", WorkflowDefPainter.generateId(webtaskid, webtaskversionid, webtaskvariantid, "TD", false, 40));
        }
        if (taskid.length() > 0) {
            ImageRef imageRef;
            String custclick;
            String color3;
            String color2;
            String color1;
            TaskDefWorkflow.Appearance appearance;
            String windowid = WINDOW_PREFIX + taskid;
            if (forcedAppearance != null) {
                appearance = forcedAppearance;
            } else {
                try {
                    appearance = prototype ? TaskDefWorkflow.Appearance.valueOf(taskprops.getProperty(PROPERTY_APPEARANCE, TaskDefWorkflow.Appearance.OFFSET.toString()).toUpperCase()) : TaskDefWorkflow.Appearance.valueOf(WorkflowDefPainter.getProperty(taskprops, toolprops, PROPERTY_APPEARANCE, TaskDefWorkflow.Appearance.OFFSET.toString()).toUpperCase());
                }
                catch (Exception e) {
                    appearance = TaskDefWorkflow.Appearance.OFFSET;
                }
            }
            if (appearance != TaskDefWorkflow.Appearance.OFFSET) {
                if (appearance == TaskDefWorkflow.Appearance.DECISION) {
                    icon = "WEB-CORE/elements/workflow/images/decision.png";
                } else if (icon.length() == 0) {
                    icon = "WEB-CORE/elements/workflow/images/NoImage.png";
                }
            } else if (icon.length() == 0) {
                icon = "WEB-CORE/images/blank.gif";
            }
            String windowclass = "";
            String windowimgclass = "";
            String windowlabelclass = "";
            String windowannotateclass = "";
            if (appearance != TaskDefWorkflow.Appearance.OFFSET) {
                windowclass = WINDOW_PREFIX + appearance.toString().toLowerCase();
                windowimgclass = "painter_windowimg_" + appearance.toString().toLowerCase();
                windowlabelclass = "painter_windowlabel_" + appearance.toString().toLowerCase();
                windowannotateclass = "painter_annotate_" + appearance.toString().toLowerCase();
            }
            if (shorttitle.length() == 0) {
                shorttitle = longtitle;
            }
            boolean locked = false;
            if (taskprops.getProperty("assignmentflag", "").length() == 0) {
                if (taskprops.getProperty("sysuserid").length() > 0) {
                    taskprops.setProperty("assignmentflag", "U");
                } else if (taskprops.getProperty("departmentid").length() > 0) {
                    taskprops.setProperty("assignmentflag", "D");
                } else if (taskprops.getProperty("roleid").length() > 0) {
                    taskprops.setProperty("assignmentflag", "R");
                }
            } else if (taskprops.getProperty("assignmentflag").equalsIgnoreCase("U")) {
                taskprops.setProperty("departmentid", "");
                taskprops.setProperty("roleid", "");
            } else if (taskprops.getProperty("assignmentflag").equalsIgnoreCase("D")) {
                taskprops.setProperty("sysuserid", "");
                taskprops.setProperty("roleid", "");
            } else if (taskprops.getProperty("assignmentflag").equalsIgnoreCase("R")) {
                taskprops.setProperty("sysuserid", "");
                taskprops.setProperty("departmentid", "");
            } else {
                taskprops.setProperty("sysuserid", "");
                taskprops.setProperty("departmentid", "");
                taskprops.setProperty("roleid", "");
            }
            if (forcedColor == null || forcedColor.length == 0 || forcedColor[0].equals("null")) {
                if (prototype) {
                    color1 = taskprops.getProperty("taskcolor1", "#ffffff");
                    color2 = taskprops.getProperty("taskcolor2", "#ffffff");
                    color3 = taskprops.getProperty("taskcolor3", "#ffffff");
                } else {
                    color1 = WorkflowDefPainter.getProperty(taskprops, toolprops, "taskcolor1", "#e7f2ff");
                    color2 = WorkflowDefPainter.getProperty(taskprops, toolprops, "taskcolor2", "#d4e3f2");
                    color3 = WorkflowDefPainter.getProperty(taskprops, toolprops, "taskcolor3", "#c9d8ed");
                }
            } else if (forcedColor.length > 0) {
                String string = color1 = forcedColor[0].length() > 0 ? forcedColor[0] : "#e7f2ff";
                if (forcedColor.length > 1) {
                    String string2 = color2 = forcedColor[1].length() > 0 ? forcedColor[1] : "#d4e3f2";
                    color3 = forcedColor.length > 2 ? (forcedColor[2].length() > 0 ? forcedColor[2] : "#c9d8ed") : (forcedColor[1].length() > 0 ? forcedColor[1] : "#d4e3f2");
                } else {
                    color2 = forcedColor[0].length() > 0 ? forcedColor[0] : "#e7f2ff";
                    color3 = forcedColor[0].length() > 0 ? forcedColor[0] : "#e7f2ff";
                }
            } else {
                color1 = "#e7f2ff";
                color2 = "#d4e3f2";
                color3 = "#c9d8ed";
            }
            double x = 0.0;
            double y = 0.0;
            try {
                x = Double.parseDouble(taskprops.getProperty("x", "" + (index * 210 + 20)));
            }
            catch (Exception e1) {
                logger.warn("Invalid x coord for task " + taskid + ".");
            }
            try {
                y = Double.parseDouble(taskprops.getProperty("y", "" + (index * 50 + 20)));
            }
            catch (Exception e1) {
                logger.warn("Invalid y coord for task " + taskid + ".");
            }
            String color = prototype ? taskprops.getProperty(PROPERTY_COLOR, "") : WorkflowDefPainter.getProperty(taskprops, toolprops, PROPERTY_COLOR, "");
            style.append("#").append(windowid).append("{");
            if (prototype) {
                style.append("border: solid 2px black;");
                if (browser.isWebkit()) {
                    style.append("-webkit-box-shadow: none;");
                } else if (browser.isMozilla()) {
                    style.append("-moz-box-shadow: none;");
                } else {
                    style.append("box-shadow: none;");
                }
            } else {
                String uri;
                double ver = browser.getVersion();
                if (browser.isChrome() && ver > 10.0 || browser.isSafari() && ver > 5.1) {
                    style.append("background: -webkit-linear-gradient(top,  ").append(color1).append(" 0%,").append(color2).append(" 38%,").append(color3).append(" 100%);");
                    if (color.length() > 0) {
                        style.append("-webkit-box-shadow: 0px 0px 17px " + color + ";color:" + color + ";");
                    }
                } else if (browser.isChrome() || browser.isSafari() && ver > 4.0) {
                    style.append("background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,").append(color1).append("), color-stop(38%,").append(color2).append("), color-stop(100%,").append(color3).append("));");
                    if (color.length() > 0) {
                        style.append("-webkit-box-shadow: 0px 0px 17px " + color + ";color:" + color + ";");
                    }
                } else if (browser.isFireFox() && ver > 3.6) {
                    style.append("background: -moz-linear-gradient(top,  ").append(color1).append(" 0%,").append(color2).append(" 38%,").append(color3).append(" 100%);");
                    if (color.length() > 0) {
                        style.append("-moz-box-shadow: 0px 0px 17px " + color + ";color:" + color + ";");
                    }
                } else if (browser.isIE() && ver > 9.0 && html5) {
                    if (ver > 10.0) {
                        style.append("background: linear-gradient(to bottom,  ").append(color1).append(" 0%,").append(color2).append(" 38%,").append(color3).append(" 100%);");
                    } else {
                        style.append("background: -ms-linear-gradient(top,  ").append(color1).append(" 0%,").append(color2).append(" 38%,").append(color3).append(" 100%);");
                    }
                    if (color.length() > 0) {
                        style.append("box-shadow: 0px 0px 17px " + color + ";color:" + color + ";");
                    }
                } else if (browser.isIE() && ver > 8.0 && html5) {
                    uri = "rc?command=operation&operationclass=" + GradientImageRequest.class.getName() + "&color1=" + HttpUtil.encodeURIComponent(color1) + "&color2=" + HttpUtil.encodeURIComponent(color2) + "&color3=" + HttpUtil.encodeURIComponent(color3);
                    style.append("background: url('").append(uri).append("');");
                    if (color.length() > 0) {
                        style.append("box-shadow: 0px 0px 17px " + color + ";color:" + color + ";");
                    }
                } else if (browser.isIE() && ver > 7.0) {
                    uri = "rc?command=operation&operationclass=" + GradientImageRequest.class.getName() + "&height=85&width=20&color1=" + HttpUtil.encodeURIComponent(color1) + "&color2=" + HttpUtil.encodeURIComponent(color2) + "&color3=" + HttpUtil.encodeURIComponent(color3) + "&png=Y";
                    style.append("background: url('").append(uri).append("');");
                    if (color.length() > 0) {
                        style.append("border: solid " + color + " 1px;");
                    } else {
                        style.append("border: solid #999999 1px;");
                    }
                } else if (browser.isIE() && ver > 6.0) {
                    style.append("filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='").append(color1).append("', endColorstr='").append(color3).append("',GradientType=0 );");
                    if (color.length() > 0) {
                        style.append("border: solid " + color + " 1px;");
                    } else {
                        style.append("border: solid #999999 1px;");
                    }
                } else {
                    style.append("background: ").append(color1).append(";");
                    style.append("background: linear-gradient(to bottom,  ").append(color1).append(" 0%,").append(color2).append(" 38%,").append(color3).append(" 100%);");
                    if (color.length() > 0) {
                        style.append("color:" + color + ";");
                    }
                }
            }
            style.append("}");
            boolean showWorkflowMirror = false;
            showWorkflowMirror = sapphireConnection != null && sapphireConnection.isRtl() ? !isSavedInRTL : isSavedInRTL;
            html.append("<div").append(sapphireConnection != null && sapphireConnection.isRtl() ? " dir=\"rtl\"" : " ").append("class=\"painter_window").append(windowclass.length() > 0 ? " " + windowclass : "").append("\" id=\"").append(windowid).append("\" style=\"top:").append(y).append("px;").append(showWorkflowMirror ? "right:" : "left:").append(x).append("px;").append(visible ? "" : "display:none;").append("\" windowid=\"").append(taskid).append("\" windowtype=\"").append(webtaskid).append(";").append(webtaskversionid).append(";").append(webtaskvariantid).append("\"");
            if (!prototype) {
                html.append(" title=\"").append(webtaskid).append(" (").append(webtaskversionid).append(" - ").append(webtaskvariantid).append(")\"");
            } else {
                html.append(" title=\"").append(tp != null ? tp.translate("Prototype Task") : "Prototype Task").append("\" ");
            }
            String onclick = "workflow.taskClick(this,event);sapphire.events.cancelEvent(event,true);";
            String string = custclick = prototype ? taskprops.getProperty("onclick", "") : WorkflowDefPainter.getProperty(taskprops, toolprops, "onclick", "");
            if (custclick.length() > 0) {
                onclick = onclick + custclick;
            }
            html.append(" onclick=\"").append(onclick).append("\"");
            html.append(">");
            Image img = new Image();
            img.setConnectionId(sdi.getConnectionid());
            img.setImageSrc(icon);
            if (appearance != TaskDefWorkflow.Appearance.DECISION) {
                img.setDimensions(32, 32);
            }
            if (locked && !viewonly) {
                img.setOverlay("Lock");
            }
            html.append("<div class=\"painter_windowimg").append(windowimgclass.length() > 0 ? " " + windowimgclass : "").append("\">").append(img.getHtml());
            html.append("<span class=\"painter_windowimg_notify\" title=\"").append(taskprops.getProperty("bubbletitle", taskprops.getProperty("bubbletext", ""))).append("\"");
            if (taskprops.getProperty("bubbletext", "").length() > 0) {
                html.append(" style=\"display:inline-block;\">");
                html.append(taskprops.getProperty("bubbletext", "1"));
            } else {
                html.append(" style=\"display:none;\">");
            }
            html.append("</span>");
            html.append("</div>").append("<div class=\"painter_windowlabel").append(windowlabelclass.length() > 0 ? " " + windowlabelclass : "").append("\"").append("").append(">").append(tp != null ? tp.translate(shorttitle) : shorttitle).append("</div>");
            String annotation = prototype ? taskprops.getProperty("annotation", "") : WorkflowDefPainter.getProperty(taskprops, toolprops, "annotation", "");
            html.append("<div class=\"painter_annotate").append(windowannotateclass.length() > 0 ? " " + windowannotateclass : "").append("\">").append(annotation).append("</div>");
            html.append("</div>");
            if (taskprops.getProperty("starttaskflag").equalsIgnoreCase("Y")) {
                imageRef = new ImageRef(sapphireConnection);
                imageRef.setImage("BulletTriangleGreen");
                imageRef.setDimensions(16, 16);
                script.append(JS_CLASS).append(".plumb.util.addStartTaskEndpoint('").append(taskid).append("','").append(windowid).append("','").append(imageRef.getSrc()).append("');");
            } else if (taskprops.getProperty("endtaskflag").equalsIgnoreCase("Y")) {
                imageRef = new ImageRef(sapphireConnection);
                imageRef.setImage("BulletSquareRed");
                imageRef.setDimensions(16, 16);
                script.append(JS_CLASS).append(".plumb.util.addEndTaskEndpoint('").append(taskid).append("','").append(windowid).append("','").append(imageRef.getSrc()).append("');");
            }
            if (taskprops.getProperty("autoexec").equalsIgnoreCase("Y")) {
                imageRef = new ImageRef(sapphireConnection);
                imageRef.setImage("FlashYellow");
                imageRef.setDimensions(16, 16);
                script.append(JS_CLASS).append(".plumb.util.addAutoExecEndpoint('").append(taskid).append("','").append(windowid).append("','").append(imageRef.getSrc()).append("');");
            }
            WorkflowDefPainter.renderIOs(script, taskios, taskid, windowid, connScript, html5, sdi, browser, sapphireConnection, logger);
            return taskid;
        }
        logger.warn("Task has missing id at " + index + ".");
        return "";
    }

    private static void renderIOs(StringBuffer script, PropertyListCollection taskios, String taskid, String windowid, Connector connScript, boolean html5, SDIProcessor sdi, Browser browser, SapphireConnection sapphireConnection, Logger logger) {
        if (taskios != null) {
            int outputtotal = 0;
            int inputtotal = 0;
            ArrayList<String> uniqueio = new ArrayList<String>();
            for (int i = 0; i < taskios.size(); ++i) {
                PropertyList io = taskios.getPropertyList(i);
                String ioid = io.getProperty("ioid", "");
                if (ioid.length() <= 0 || uniqueio.contains(ioid)) continue;
                uniqueio.add(ioid);
                if (io.getProperty("ioflag", "o").equalsIgnoreCase("o")) {
                    ++outputtotal;
                    continue;
                }
                ++inputtotal;
            }
            PropertyListCollection connectors = TaskDefWorkflow.getConnectors(sdi);
            int outputcount = 0;
            int inputcount = 0;
            uniqueio = new ArrayList();
            for (int i = 0; i < taskios.size(); ++i) {
                PropertyList io = taskios.getPropertyList(i);
                String ioid = io.getProperty("ioid");
                if (ioid.length() > 0) {
                    double anc;
                    if (uniqueio.contains(ioid)) {
                        logger.debug("Skipped duplicate io.");
                        continue;
                    }
                    uniqueio.add(ioid);
                    PropertyList connector = null;
                    String connectorid = io.getProperty("connectortypeid", "");
                    if (connectors != null && connectors.size() > 0 && connectorid.length() > 0) {
                        connector = connectors.find("connectortypeid", io.getProperty("connectortypeid", ""));
                    }
                    if (connector == null) {
                        connector = new PropertyList();
                    }
                    script.append("").append(JS_CLASS).append(".plumb.util.addEndpoint(\"").append(taskid).append("\",\"").append(ioid).append("\",\"").append(windowid).append("\",");
                    boolean output = io.getProperty("ioflag", "o").equalsIgnoreCase("o");
                    if (output) {
                        anc = 1.0 / (double)outputtotal * (double)outputcount + 1.0 / ((double)outputtotal * 2.0);
                        if (sapphireConnection != null && sapphireConnection.isRtl()) {
                            script.append("{anchor:[[0,").append(anc).append(",0,0]");
                            script.append(",[").append(anc).append(",1,0,0]");
                            script.append("]},{");
                        } else {
                            script.append("{anchor:[[1,").append(anc).append(",1,0],[").append(anc).append(",1,0,1]]},{");
                        }
                        script.append("isSource:true,");
                        script.append("isTarget:false,");
                        ++outputcount;
                    } else {
                        anc = 1.0 / (double)inputtotal * (double)inputcount + 1.0 / ((double)inputtotal * 2.0);
                        if (sapphireConnection != null && sapphireConnection.isRtl()) {
                            script.append("{anchor:[[1,").append(anc).append(",0,0]");
                            script.append(",[").append(anc).append(",0,0,-1]");
                            script.append("]},{");
                        } else {
                            script.append("{anchor:[[0,").append(anc).append(",-1,0],[").append(anc).append(",0,0,-1]]},{");
                        }
                        script.append("isSource:false,");
                        script.append("isTarget:true,");
                        ++inputcount;
                    }
                    script.append("paintStyle:{fillStyle:\"").append(connector.getProperty(PROPERTY_COLOR, "#316b31")).append("\",opacity:0.5}");
                    if (!output) {
                        script.append(",dropOptions:").append(JS_CLASS).append(".plumb.dropOptions");
                        script.append(",dragOptions:").append(JS_CLASS).append(".plumb.dragOptionsIn");
                        if (!io.getProperty("singleconnect", "N").equalsIgnoreCase("Y")) {
                            script.append(",maxConnections:-1");
                        }
                    } else {
                        if (!io.getProperty("singleconnect", "N").equalsIgnoreCase("Y")) {
                            script.append(",maxConnections:-1");
                        }
                        script.append(",dragOptions:").append(JS_CLASS).append(".plumb.dragOptionsOut");
                    }
                    script.append(",connector:").append(connScript.getConnector());
                    script.append(",connectorOverlays:[[\"Arrow\",{location:").append(connScript == Connector.FLOWCHART ? 0.99 : 0.5).append("}]]");
                    String scope = connectorid;
                    if (scope.length() > 0) {
                        script.append(",scope:\"").append(connectorid).append("\"");
                    }
                    String iotitle = io.getProperty("iodesc", "");
                    double ver = browser.getVersion();
                    if (!browser.isIE() || !(ver < 9.0) && html5) {
                        script.append(",overlays:[").append("[\"Label\",{location:");
                        if (output) {
                            if (sapphireConnection != null && sapphireConnection.isRtl()) {
                                script.append("[-4,0.5]");
                            } else {
                                script.append("[5,0.5]");
                            }
                        } else if (sapphireConnection != null && sapphireConnection.isRtl()) {
                            script.append("[5,0.5]");
                        } else {
                            script.append("[-4,0.5]");
                        }
                        script.append(",label:\"").append(iotitle.length() > 0 ? iotitle : "Untitled " + (output ? "Output" : "Input")).append("\",cssClass:\"painter_label\"}],").append("]");
                    }
                    script.append("}");
                    script.append(",").append(!output && io.getProperty("waittype").equalsIgnoreCase("event"));
                    if (io.getProperty("bubbletext", "").length() > 0) {
                        script.append(",").append("'").append(io.getProperty("bubbletext", "")).append("'");
                        script.append(",").append("'").append(io.getProperty("bubbletitle", io.getProperty("bubbletext", ""))).append("'");
                    } else {
                        script.append(",").append("''");
                        script.append(",").append("''");
                    }
                    script.append(");");
                    continue;
                }
                logger.warn("IO skipped due to blank IOId.");
            }
        } else {
            logger.debug("No IOS for task.");
        }
    }

    protected static String renderElement(PropertyList elementProps, int index, StringBuffer html, StringBuffer script, boolean visible, boolean viewonly, boolean html5, boolean savedInRtl, Browser browser, SDIProcessor sdi, TranslationProcessor tp, String connectionId, Logger logger) {
        Tools elementtype;
        String elementId = elementProps.getProperty("elementid");
        try {
            elementtype = Tools.valueOf(elementProps.getProperty("type", Tools.LABEL.toString()).toUpperCase());
        }
        catch (Exception e) {
            elementtype = Tools.LABEL;
        }
        if (elementId.length() > 0) {
            if (elementtype == Tools.TASK) {
                return elementId;
            }
            String windowid = ELEMENT_PREFIX + elementId;
            double x = 0.0;
            double y = 0.0;
            try {
                x = Double.parseDouble(elementProps.getProperty("x", "" + (index * 210 + 20)));
            }
            catch (Exception e1) {
                logger.warn("Invalid x coord for element " + elementId + ".");
            }
            try {
                y = Double.parseDouble(elementProps.getProperty("y", "" + (index * 50 + 20)));
            }
            catch (Exception e1) {
                logger.warn("Invalid y coord for element " + elementId + ".");
            }
            String className = "";
            switch (elementtype) {
                case TEXT: 
                case BOX: 
                case CIRCLE: 
                case IMAGE: {
                    className = " painter_element_resize";
                    break;
                }
            }
            String onclick = "workflow.elementClick(this,event);sapphire.events.cancelEvent(event,true);";
            boolean rtlFlag = new ConnectionProcessor(connectionId).getSapphireConnection().isRtl();
            boolean showWorkflowMirror = false;
            showWorkflowMirror = rtlFlag ? !savedInRtl : savedInRtl;
            if (elementtype != Tools.LINE) {
                html.append("<div").append(rtlFlag ? " dir=\"rtl\"" : " ").append("class=\"painter_element").append(className).append("\" id=\"").append(windowid).append("\" name=\"").append(windowid).append("\" windowid=\"").append(elementId).append("\" windowtype=\"").append(elementtype.toString().toLowerCase()).append("\"");
                html.append(" style=\"top:").append(y).append("px;").append(showWorkflowMirror ? "right:" : "left:").append(x).append("px;").append(visible ? "" : "display:none;").append("");
                html.append("\"");
                html.append(" onclick=\"").append(onclick).append("\"");
                html.append(">");
            }
            double ver = browser.getVersion();
            switch (elementtype) {
                case TEXT: {
                    String backStyle;
                    String borderStyle = elementProps.getProperty("showline", "Y").equalsIgnoreCase("N") ? "border:none;" : "border-style:" + elementProps.getProperty("linestyle", "solid").toLowerCase() + ";border-color:" + elementProps.getProperty("linecolor", "transparent") + ";border-width:" + elementProps.getProperty("linewidth", "2") + "px;";
                    if (elementProps.getProperty("showbackground", "Y").equalsIgnoreCase("N")) {
                        backStyle = "background:none;";
                    } else {
                        backStyle = "background-color:" + elementProps.getProperty("backgroundcolor", "white") + ";";
                        double o = 1.0;
                        try {
                            o = Double.parseDouble(elementProps.getProperty("backgroundopacity", "1"));
                        }
                        catch (Exception e) {
                            o = 1.0;
                        }
                        backStyle = backStyle + "opacity:" + o + ";";
                    }
                    html.append("<div id=\"").append(windowid).append("_div\" style=\"").append(borderStyle).append("").append(backStyle).append("font-size:").append(elementProps.getProperty("fontsize", "10")).append("pt;color:").append(elementProps.getProperty(PROPERTY_COLOR, "black")).append(";width:").append(elementProps.getProperty("width", "100")).append("px;height:").append(elementProps.getProperty("height", "30")).append("px;overflow:hidden;padding: 5px 5px 5px 5px;").append("\">");
                    String t = elementProps.getProperty("text", "No Text");
                    html.append(tp != null ? tp.translate(t) : t);
                    html.append("</div>");
                    break;
                }
                case IMAGE: {
                    String borderStyle = elementProps.getProperty("showline", "Y").equalsIgnoreCase("N") ? "border:none;" : "border-style:" + elementProps.getProperty("linestyle", "solid").toLowerCase() + ";border-color:" + elementProps.getProperty("linecolor", "transparent") + ";border-width:" + elementProps.getProperty("linewidth", "2") + "px;";
                    if (elementProps.getProperty("showbackground", "Y").equalsIgnoreCase("N")) {
                        String backStyle = "background:none;";
                    } else {
                        String backStyle = "background-color:" + elementProps.getProperty("backgroundcolor", "white") + ";";
                        double o = 1.0;
                        try {
                            o = Double.parseDouble(elementProps.getProperty("backgroundopacity", "1"));
                        }
                        catch (Exception e) {
                            o = 1.0;
                        }
                        backStyle = backStyle + "opacity:" + o + ";";
                    }
                    Image img = new Image();
                    img.setConnectionId(sdi.getConnectionid());
                    img.setImageSrc(elementProps.getProperty("image", "WEB-CORE/images/blank.gif"));
                    img.setDimensions(48, 48);
                    img.setStyle("width:100%;height:100%;");
                    html.append("<div id=\"").append(windowid).append("_div\" style=\"").append(borderStyle).append("width:").append(elementProps.getProperty("width", "50")).append("px;height:").append(elementProps.getProperty("height", "50")).append("px;").append("\">");
                    html.append(img.getHtml());
                    html.append("</div>");
                    break;
                }
                case BOX: {
                    String backStyle;
                    String borderStyle = elementProps.getProperty("showline", "Y").equalsIgnoreCase("N") ? "border:none;" : "border-style:" + elementProps.getProperty("linestyle", "solid").toLowerCase() + ";border-color:" + elementProps.getProperty("linecolor", "transparent") + ";border-width:" + elementProps.getProperty("linewidth", "2") + "px;";
                    if (elementProps.getProperty("showbackground", "Y").equalsIgnoreCase("N")) {
                        backStyle = "background:none;";
                    } else {
                        backStyle = "background-color:" + elementProps.getProperty("backgroundcolor", "white") + ";";
                        double o = 1.0;
                        try {
                            o = Double.parseDouble(elementProps.getProperty("backgroundopacity", "1"));
                        }
                        catch (Exception e) {
                            o = 1.0;
                        }
                        backStyle = backStyle + "opacity:" + o + ";";
                    }
                    html.append("<div id=\"").append(windowid).append("_div\" style=\"").append(borderStyle).append("width:").append(elementProps.getProperty("width", "100")).append("px;height:").append(elementProps.getProperty("height", "100")).append("px;").append("\">");
                    html.append("<div style=\"").append(backStyle).append("width:100%;height:100%;").append("\">");
                    html.append("</div>");
                    html.append("</div>");
                    break;
                }
                case CIRCLE: {
                    String backStyle;
                    String borderStyle = elementProps.getProperty("showline", "Y").equalsIgnoreCase("N") ? "border:none;" : "border-style:" + elementProps.getProperty("linestyle", "solid").toLowerCase() + ";border-color:" + elementProps.getProperty("linecolor", "transparent") + ";border-width:" + elementProps.getProperty("linewidth", "2") + "px;";
                    if (elementProps.getProperty("showbackground", "Y").equalsIgnoreCase("N")) {
                        backStyle = "background:none;";
                    } else {
                        backStyle = "background-color:" + elementProps.getProperty("backgroundcolor", "white") + ";";
                        double o = 1.0;
                        try {
                            o = Double.parseDouble(elementProps.getProperty("backgroundopacity", "1"));
                        }
                        catch (Exception e) {
                            o = 1.0;
                        }
                        backStyle = backStyle + "opacity:" + o + ";";
                    }
                    html.append("<div id=\"").append(windowid).append("_div\" style=\"").append(borderStyle).append("width:").append(elementProps.getProperty("width", "100")).append("px;height:").append(elementProps.getProperty("height", "100")).append("px;border-radius:100%;").append("\">");
                    html.append("<div style=\"").append(backStyle).append("border-radius:100%;width:100%;height:100%;").append("\">");
                    html.append("</div>");
                    html.append("</div>");
                    break;
                }
                case LINE: {
                    double x2 = 50.0;
                    double y2 = 50.0;
                    try {
                        x2 = Double.parseDouble(elementProps.getProperty("x2", "" + (x + 50.0)));
                    }
                    catch (Exception e1) {
                        logger.warn("Invalid x2 coord for element " + elementId + ".");
                    }
                    try {
                        y2 = Double.parseDouble(elementProps.getProperty("y2", "" + (y + 50.0)));
                    }
                    catch (Exception e1) {
                        logger.warn("Invalid y2 coord for element " + elementId + ".");
                    }
                    String linewidth = elementProps.getProperty("linewidth", "2");
                    if (linewidth.endsWith("px")) {
                        linewidth.substring(0, linewidth.length() - 2);
                    }
                    String overlays = "";
                    if (elementProps.getProperty("targetarrow", "N").equalsIgnoreCase("Y")) {
                        overlays = overlays + "[\"PlainArrow\",{location:0.99}]";
                    }
                    html.append("<div class=\"painter_element painter_element_line\" id=\"").append(windowid).append("__1\" name=\"").append(windowid).append("\" style=\"").append("top:").append(y).append("px;").append(showWorkflowMirror ? "right:" : "left:").append(x).append("px;border:").append(viewonly ? "none;cursor:default" : "solid 1px grey").append(";").append(visible ? "" : "display:none;").append("").append("\" onclick=\"").append(onclick).append("\" windowid=\"").append(elementId).append("\" windowtype=\"").append(elementtype.toString().toLowerCase()).append("\">");
                    html.append("</div>");
                    html.append("<div class=\"painter_element painter_element_line\" id=\"").append(windowid).append("__2\" name=\"").append(windowid).append("\" style=\"").append("top:").append(y2).append("px;").append(showWorkflowMirror ? "right:" : "left:").append(x2).append("px;border:").append(viewonly ? "none;cursor:default" : "solid 1px grey").append(";").append(visible ? "" : "display:none;").append("").append("\" onclick=\"").append(onclick).append("\" windowid=\"").append(elementId).append("\" windowtype=\"").append(elementtype.toString().toLowerCase()).append("\">");
                    html.append("</div>");
                    script.append("jsPlumb.connect({");
                    script.append("source:\"").append(windowid).append("__2\",");
                    script.append("target:\"").append(windowid).append("__1\",");
                    script.append("connector:\"Straight\",");
                    script.append("endpoint:\"Blank\",");
                    script.append("anchors:[\"Center\", \"Center\"],");
                    script.append("overlays :[").append(overlays).append("],");
                    script.append("paintStyle:{");
                    if (!elementProps.getProperty("linestyle", "solid").equalsIgnoreCase("solid")) {
                        String dash = elementProps.getProperty("linestyle", "solid").equalsIgnoreCase("dotted") ? "1 1" : "4 2";
                        script.append("dashstyle:\"").append(dash).append("\",");
                        script.append("joinstyle:\"").append("miter").append("\",");
                    }
                    script.append("lineWidth:").append(linewidth).append(",");
                    script.append("strokeStyle:\"").append(elementProps.getProperty("linecolor", "black")).append("\"");
                    script.append("}");
                    script.append("});");
                    break;
                }
                default: {
                    html.append("<span style=\"font-size:").append(elementProps.getProperty("fontsize", "10")).append("pt;color:").append(elementProps.getProperty("textcolor", "black")).append(";\">");
                    String p = elementProps.getProperty("text", "No Text");
                    html.append(tp != null ? tp.translate(p) : p);
                    html.append("</span>");
                }
            }
            if (elementtype != Tools.LINE) {
                html.append("</div>");
            }
            return elementId;
        }
        logger.warn("Element has missing id at " + index + ".");
        return "";
    }

    private String getPainterHtml(PropertyList workflowDef, PropertyList tooldef, Connector connScript, TaskDefWorkflow.Appearance forcedAppearance, String[] forcedColor) {
        StringBuffer html = new StringBuffer();
        StringBuffer script = new StringBuffer();
        StringBuffer style = new StringBuffer();
        double top = 0.0;
        html.append("<div ").append(this.rtlFlag ? "dir=\"rtl\"" : "").append("id=\"painter_canvas_cont\" class=\"painter_canvas_cont\" style=\"").append(this.viewonly && this.painteronly ? "background-image:none;" : "").append(this.print ? "overflow:visible;" : "").append("top:").append(top).append("px;zoom:").append(this.browser.isIE() || this.browser.isChrome() || this.browser.isEdge() ? 100 : this.zoom).append("%;\">");
        html.append("<div ").append(this.rtlFlag ? "dir=\"ltr\"" : "").append("id=\"painter_canvas\" class=\"painter_canvas\" style=\"").append("\" onclick=\"workflow.canvasClick(event)\">");
        html.append(WorkflowDefPainter.getPainterContents(workflowDef, tooldef, connScript, forcedAppearance, forcedColor, this.viewonly, this.painteronly, this.translate, this.devMode, this.zoom, this.renderHtml5, script, style, this.getSDIProcessor(), this.browser, this.pageContext, this.getConnectionProcessor().getSapphireConnection(), this.getTranslationProcessor(), this.logger));
        html.append("</div>");
        html.append("</div>");
        html.append("<style id=\"workflow_windowstyle\">");
        html.append(style);
        html.append("</style>");
        html.append("<script>");
        html.append(JS_CLASS).append(".plumb.deploy = function( ){");
        html.append(script);
        html.append("}");
        html.append("</script>");
        return html.toString();
    }

    public static String getPainterContents(PropertyList workflowDef, PropertyList tooldef, Connector connScript, TaskDefWorkflow.Appearance forcedAppearance, String[] forcedColor, boolean viewOnly, boolean painterOnly, boolean devMode, int zoom, boolean html5, StringBuffer script, StringBuffer style, SDIProcessor sdi, Browser browser, PageContext pageContext, SapphireConnection sapphireConnection, Logger logger) {
        return WorkflowDefPainter.getPainterContents(workflowDef, tooldef, connScript, forcedAppearance, forcedColor, viewOnly, painterOnly, false, devMode, zoom, html5, script, style, sdi, browser, pageContext, sapphireConnection, null, logger);
    }

    public static String getPainterContents(PropertyList workflowDef, PropertyList tooldef, Connector connScript, TaskDefWorkflow.Appearance forcedAppearance, String[] forcedColor, boolean viewOnly, boolean painterOnly, boolean translate, boolean devMode, int zoom, boolean html5, StringBuffer script, StringBuffer style, SDIProcessor sdi, Browser browser, PageContext pageContext, SapphireConnection sapphireConnection, TranslationProcessor tp, Logger logger) {
        StringBuffer html = new StringBuffer();
        PropertyBuilder pb = new PropertyBuilder(pageContext, "workflowdef.xml");
        PropertyDefinitionList propertyDef = pb.getPropertyDefinition();
        if (translate) {
            if (tp == null) {
                if (pageContext != null) {
                    tp = new TranslationProcessor(pageContext);
                } else if (sapphireConnection != null) {
                    tp = new TranslationProcessor(sapphireConnection.getConnectionId());
                } else {
                    tp = null;
                    translate = false;
                }
            }
        } else {
            tp = null;
        }
        PropertyListCollection tasks = workflowDef.getCollection("tasks");
        int taskindex = -1;
        if (tasks != null && tasks.size() > 0) {
            PropertyList task;
            int i;
            for (i = 0; i < tasks.size(); ++i) {
                task = tasks.getPropertyList(i);
                PropertyList tool = null;
                if (tooldef != null && tooldef.getCollection("tasks") != null && (tool = tooldef.getCollection("tasks").find("taskkey", task.getProperty("taskdefid", "") + ";" + task.getProperty("taskdefversionid", "1") + ";" + task.getProperty("taskdefvariantid", "1"))) == null) {
                    tool = tooldef.getCollection("tasks").find("taskkey", task.getProperty("taskdefid", "") + ";" + task.getProperty("taskdefversionid", "1"));
                }
                WorkflowDefPainter.renderWindow(task, tool == null ? new PropertyList() : tool, workflowDef, propertyDef, connScript, forcedAppearance, forcedColor, i, html, script, style, true, viewOnly, html5, workflowDef.getProperty("rtl", "N").equalsIgnoreCase("Y"), browser, sdi, sapphireConnection, tp, logger);
            }
            for (i = 0; i < tasks.size(); ++i) {
                PropertyListCollection fromios;
                task = tasks.getPropertyList(i);
                String fromTaskId = task.getProperty("taskdefitemid", "");
                if (fromTaskId.length() <= 0 || (fromios = task.getCollection("taskio")) == null || fromios.size() <= 0) continue;
                for (int k = 0; k < fromios.size(); ++k) {
                    PropertyList fromio = fromios.getPropertyList(k);
                    String fromIOId = fromio.getProperty("ioid", "");
                    String toTaskId = fromio.getProperty("connecttaskdefitemid", "");
                    String toIOId = fromio.getProperty("connectioid", "");
                    if (fromIOId.length() > 0 && toTaskId.length() > 0 && toIOId.length() > 0) {
                        PropertyList toIO;
                        PropertyList toTask = tasks.find("taskdefitemid", toTaskId);
                        if (toTask == null || toTask.getCollection("taskio") == null || (toIO = toTask.getCollection("taskio").find("ioid", toIOId)) == null) continue;
                        Connector overrideConnScript = connScript;
                        if (fromio.getProperty(PROPERTY_CONNECTOR).length() > 0) {
                            try {
                                overrideConnScript = Connector.valueOf(fromio.getProperty(PROPERTY_CONNECTOR).toUpperCase());
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            script.append(JS_CLASS).append(".plumb.util.connect(\"").append(fromTaskId).append("\",\"").append(fromIOId).append("\",\"").append(toTaskId).append("\",\"").append(toIOId).append("\",").append(overrideConnScript.getConnector()).append(");");
                            continue;
                        }
                        script.append(JS_CLASS).append(".plumb.util.connect(\"").append(fromTaskId).append("\",\"").append(fromIOId).append("\",\"").append(toTaskId).append("\",\"").append(toIOId).append("\");");
                        continue;
                    }
                    logger.warn("Missing step id for transition " + k + " on step " + i + ".");
                }
            }
            taskindex = tasks.size() - 1;
        }
        PropertyListCollection elements = workflowDef.getCollection("elements");
        int elementindex = -1;
        if (elements != null && elements.size() > 0) {
            for (int i = 0; i < elements.size(); ++i) {
                PropertyList element = elements.getPropertyList(i);
                WorkflowDefPainter.renderElement(element, i, html, script, true, viewOnly, html5, workflowDef.getProperty("rtl", "N").equalsIgnoreCase("Y"), browser, sdi, tp, sapphireConnection.getConnectionId(), logger);
            }
            elementindex = elements.size() - 1;
        }
        return html.toString();
    }

    public static String generateId(String webtaskid, String webversionid, String webvariantid, String prefix, boolean specialChars) {
        return WorkflowDefPainter.generateId(webtaskid, webversionid, webvariantid, prefix, specialChars, -1);
    }

    public static String generateId(String webtaskid, String webversionid, String webvariantid, String prefix, boolean specialChars, int maxlen) {
        int r = (int)(Math.random() * 100.0);
        if (!specialChars) {
            String id = "" + webtaskid.trim().replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", "_") + "" + webversionid.trim().replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", "_") + "" + (webvariantid.length() > 0 && !webvariantid.equalsIgnoreCase("1") ? webvariantid.trim().replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", "_") + "" : "") + r;
            if (maxlen > -1 && id.length() + prefix.length() > maxlen) {
                String t = id;
                id = "" + webtaskid.trim().replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", "_").substring(0, webtaskid.length() - (id.length() + prefix.length() - maxlen)) + webversionid.trim().replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", "_") + "" + (webvariantid.length() > 0 && !webvariantid.equalsIgnoreCase("1") ? webvariantid.trim().replaceAll("[^\\dA-Za-z ]", "").replaceAll("\\s+", "_") + "" : "") + r;
            }
            return prefix + id.trim();
        }
        String id = "" + webtaskid + "_" + webversionid + "_" + (webvariantid.length() > 0 && !webvariantid.equalsIgnoreCase("1") ? webvariantid + "_" : "") + r;
        if (maxlen > -1 && id.length() + prefix.length() > maxlen) {
            String t = id;
            id = "" + webtaskid.substring(0, webtaskid.length() - (id.length() + prefix.length() - maxlen)) + webversionid + "_" + (webvariantid.length() > 0 && !webvariantid.equalsIgnoreCase("1") ? webvariantid + "_" : "") + r;
        }
        return prefix + id.trim();
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.workflowprops != null) {
            Connector connector;
            boolean tasksselected;
            int tbiheight;
            int lbiwidth;
            html.append(this.getScriptAndStyle());
            html.append("<div id=\"painter_root_container\" style=\"").append(this.viewonly || this.painteronly ? "" : "min-width:800px;").append("overflow-y:").append(this.viewonly || this.painteronly ? "hidden" : "hidden").append(";\">");
            String lbwidth = this.userConfig.getProperty("workflow_left_width", "210");
            try {
                lbiwidth = Integer.parseInt(lbwidth);
            }
            catch (Exception e) {
                lbiwidth = 210;
            }
            String tbheight = this.userConfig.getProperty("workflow_top_height", "300");
            try {
                tbiheight = Integer.parseInt(tbheight);
            }
            catch (Exception e) {
                tbiheight = 300;
            }
            html.append("<div id=\"painter_top_container\" style=\"height:").append(this.viewonly || this.painteronly ? "100%" : "100%").append(";\">");
            boolean showLeft = this.viewonly || this.painteronly ? false : this.userConfig.getProperty("workflow_left", this.viewonly ? "N" : "Y").equalsIgnoreCase("Y");
            StringBuffer sb = new StringBuffer();
            if (showLeft) {
                html.append("<div id=\"painter_leftbar_collapsed\" class=\"painter_bar_collapsed\">");
                html.append("<div id=\"painter_leftbar_expand\" class=\"painter_bar_expand ").append(this.browser.isIE() ? "painter_leftbar_expand_ie" : "painter_leftbar_expand_nonie").append("\" onclick=\"").append(JS_CLASS).append(".ui.expandBar('left');\"><img src=\"WEB-CORE/elements/richtext/images/objects-").append(this.browser.isIE() ? "v" : "h").append(".gif\" width=\"17\" height=\"16\"").append(this.browser.isWebkit() ? " style=\"padding-top:1px;margin-bottom:-2px;\"" : "").append(">&nbsp;").append(this.getTranslationProcessor().translate("Tasks")).append("</div>");
                html.append("<div style=\"height:100%;\"></div>");
                html.append("</div>");
            } else {
                html.append("<div id=\"painter_leftbar_collapsed\" class=\"painter_bar_collapsed\" style=\"display:").append(this.viewonly || this.painteronly ? "none" : "block").append(";\">");
                html.append("<div id=\"painter_leftbar_expand\" class=\"painter_bar_expand ").append(this.browser.isIE() ? "painter_leftbar_expand_ie" : "painter_leftbar_expand_nonie").append("\" onclick=\"").append(JS_CLASS).append(".ui.expandBar('left');\"><img src=\"WEB-CORE/elements/richtext/images/objects-").append(this.browser.isIE() ? "v" : "h").append(".gif\" width=\"17\" height=\"16\"").append(this.browser.isWebkit() ? " style=\"padding-top:1px;margin-bottom:-2px;\"" : "").append(">&nbsp;").append(this.getTranslationProcessor().translate("Tasks")).append("</div>");
                html.append("<div style=\"height:100%;\"></div>");
                html.append("</div>");
            }
            if (showLeft) {
                html.append("<div id=\"painter_leftbar\" style=\"width:").append(lbiwidth).append("px;\">");
            } else {
                html.append("<div id=\"painter_leftbar\" style=\"display:none;width:").append(lbiwidth).append("px;\">");
            }
            html.append("<table style=\"table-layout:fixed;width:100%;height:30px;\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"painter_bar\">");
            html.append("<tbody>");
            html.append("<tr class=\"layout_sidebar_tab_row\" ").append("").append(">");
            if (this.workflowprops.getCollection("tasks") == null || this.workflowprops.getCollection("tasks").size() == 0) {
                tasksselected = true;
                this.userConfig.setProperty("workflow_tooltab", "tasks");
            } else {
                tasksselected = this.userConfig.getProperty("workflow_tooltab", "tasks").equalsIgnoreCase("tasks");
            }
            html.append("<td class=\"tab_back").append(tasksselected ? "_sel" : "").append("\">");
            html.append("<table cellpadding=0 cellspacing=0 class=\"tab").append(tasksselected ? "_sel" : "").append("\" style=\"width:100%;\" onclick=\"").append(JS_CLASS).append(".ui.toolboxTab('tasks');\" onmouseover=\"\" onmouseout=\"\"><tbody><tr>");
            html.append("<td align=\"").append(this.rtlFlag ? "right" : "left").append("\" class=\"objects_h_cell\">");
            html.append("<img src=\"WEB-CORE/elements/richtext/images/objects-h.gif\" width=\"16\" height=\"17\">");
            html.append("</td>");
            html.append("<td align=\"").append(this.rtlFlag ? "right" : "left").append("\" valign=\"middle\" nowrap class=\"objects_t_cell\">");
            html.append("<div class=\"tab_div").append(tasksselected ? "_sel" : "").append("\">");
            html.append(this.getTranslationProcessor().translate("Tasks"));
            html.append("</div>");
            html.append("<div id=\"painter_toolsmenu\" onclick=\"").append(JS_CLASS).append(".ui.showGroupingMenu(event)\" onmouseenter=\"this.style.backgroundColor='#F9F9F5';this.style.border='solid 1px #CECEC3';\" onmouseleave=\"this.style.backgroundColor='';this.style.border='';\"><img src=\"WEB-CORE/elements/richtext/images/grouping.gif\"><img src=\"WEB-CORE/elements/richtext/images/dropdownarrow.gif\"></div>");
            html.append("</td>");
            html.append("</tr></tbody></table>");
            html.append("</td>");
            html.append("<td class=\"tab_back").append(tasksselected ? "" : "_sel").append("\">");
            html.append("<table cellpadding=0 cellspacing=0 class=\"tab").append(tasksselected ? "" : "_sel").append("\" style=\"width:100%;\" onclick=\"").append(JS_CLASS).append(".ui.toolboxTab('tools');\" onmouseover=\"\" onmouseout=\"\"><tbody><tr>");
            html.append("<td align=\"").append(this.rtlFlag ? "right" : "left").append("\" class=\"props_h_cell\">");
            html.append("<img src=\"WEB-CORE/elements/richtext/images/props-h.gif\" width=\"16\" height=\"17\">");
            html.append("</td>");
            html.append("<td align=\"").append(this.rtlFlag ? "right" : "left").append("\" valign=\"middle\" nowrap class=\"props_t_cell\">");
            html.append("<div class=\"tab_div").append(!tasksselected ? "_sel" : "").append("\">");
            html.append(this.getTranslationProcessor().translate("Tools"));
            html.append("</div>");
            html.append("</td>");
            html.append("</tr></tbody></table>");
            html.append("</td>");
            html.append("<td class=\"tab_back\" style=\"width:20px;\">");
            html.append("<table cellpadding=0 cellspacing=0 class=\"tab\" style=\"height:100%;\" onmouseover=\"this.style.borderColor='#F49900';\" onmouseout=\"this.style.borderColor='';\"><tbody><tr>");
            html.append("<td align=right valign=middle>");
            html.append("<img src=\"WEB-OPAL/layouts/generic/images/sidebar_").append(this.rtlFlag ? "expand" : "collapse").append(".gif\" title=\"Collapse\" id=\"painter_leftbar_collapse\" style=\"display:block;cursor: pointer;\" onclick=\"").append(JS_CLASS).append(".ui.collapseBar('left');\">");
            html.append("</td>");
            html.append("</tr></tbody></table>");
            html.append("</td>");
            html.append("</tr>");
            html.append("<tr class=\"painter_merge\">");
            html.append("<td colspan=\"3\">");
            html.append("</td>");
            html.append("</tr>");
            html.append("</tbody>");
            html.append("</table>");
            html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_objects_outer\" style=\"display:").append(tasksselected ? "block" : "none").append(";").append(this.browser.isIE() ? "" : "").append("\">");
            html.append("<input onkeyup=\"").append(JS_CLASS).append(".onToolFilter(this,event)").append("\" onchange=\"").append(JS_CLASS).append(".onToolFilter(this,event)").append("\" onfocus=\"if(this.value==this.defaultValue){this.style.color='black';value='';}\" onblur=\"if(this.value.length==0){this.style.color='grey';this.value=this.defaultValue;}\" type=\"text\" id=\"painter_toolsfilter\" value=\"Enter Filter\" style=\"").append("width:").append(lbiwidth - 20).append("px;").append("color:grey;\">");
            html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_objects_content\"  style=\"width:").append(lbiwidth - 12).append("px;height:auto;").append(this.browser.isIE() ? "" : "").append("\">");
            M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
            html.append(WorkflowDefPainter.getToolsHtml(this.workflowprops, this.toolprops, null, false, this.getConnectionId(), this.getTranslationProcessor(), m18n, this.userConfig, "workflow_", (HttpServletRequest)this.pageContext.getRequest(), this.logger));
            html.append("</div>");
            html.append("</div>");
            html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_tools_outer\" style=\"display:").append(tasksselected ? "none" : "block").append(";").append(this.browser.isIE() ? "" : "").append("\">");
            html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_tools_content\"  style=\"width:").append(lbiwidth - 12).append("px;").append(this.browser.isIE() ? "" : "").append("\">");
            html.append(WorkflowDefPainter.getElementsHTML(this.workflowprops, this.getConnectionId(), this.getTranslationProcessor(), this.userConfig, this.logger));
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append("<div id=\"painter_content_container\" class=\"").append(this.print ? "" : "painter_content_container").append("\" style=\"").append(this.viewonly || this.painteronly ? "" : "min-width:300px;").append("").append(this.print ? "" : "").append("\">");
            String topheight = tbiheight + "px";
            if (this.viewonly) {
                topheight = "50%";
            } else if (this.painteronly) {
                topheight = "100%";
            }
            html.append("<div id=\"painter_content_container_top\" style=\"height:").append(topheight).append(";position:relative;\">");
            try {
                connector = Connector.valueOf(this.workflowprops.getProperty(PROPERTY_CONNECTOR, Connector.FLOWCHART.toString()));
            }
            catch (Exception e) {
                connector = Connector.FLOWCHART;
            }
            html.append(this.getPainterHtml(this.workflowprops, this.toolprops, connector, this.forcedAppearance, this.forcedColor));
            if (this.browser.isWebkit() && !this.browser.isEdge() && !this.print) {
                html.append("<div id=\"painter_zoom_container\" align=center style=\"\"><div id=\"painter_zoom\"></div><label id=\"painter_zoom_label\">").append(this.zoom).append("%</label></div>");
            }
            html.append("</div>");
            String stylebottom = "display:" + (this.painteronly ? "none" : "block") + ";top:" + tbiheight + "px";
            if (this.viewonly) {
                stylebottom = "display:block;bottom:0;height:50%;";
            }
            html.append("<div id=\"painter_content_container_bottom\" style=\"").append(stylebottom).append("\">");
            html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_properties_outer\">");
            html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_properties_content\">");
            html.append(WorkflowDefPainter.getPropertiesHtml("", "", "", "", this.workflowprops, this.viewonly, null, this.getConnectionId(), this.userConfig, this.getTranslationProcessor(), this.pageContext.getSession(), this.browser));
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append(this.getEndScript(this.workflowprops, this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.propertyContainer, this.viewonly, this.painteronly, this.hideelements, this.zoom));
        } else {
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load properties for workflow.")).append("</font>");
        }
        return html.toString();
    }

    public static String getPropertiesHtml(String source, String target, String output, String input, PropertyList props, boolean viewOnly, WorkflowDefProperties.ItemType type, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, Browser browser) {
        StringBuffer html = new StringBuffer();
        html.append("<div align=\"left\" id=\"painter_properties_cover\">");
        html.append("</div>");
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            html.append("<iframe name=\"workflow_props_frame\" id=\"workflow_props_frame\" frameborder=0 scrolling=false src=\"").append(browser.getBlankSrc()).append("\" style=\"width:100%;height:100%;\"></iframe>");
            html.append("<form style=\"display:none\" method=\"post\" id=\"workflow_props_form\" name=\"workflow_props_form\" action=\"rc?command=file&file=WEB-CORE/elements/workflow/workflowdefproperties.jsp\" target=\"workflow_props_frame\">");
            html.append("<input type=\"hidden\" name=\"source\" value=\"").append(source).append("\">");
            html.append("<input type=\"hidden\" name=\"target\" value=\"").append(target).append("\">");
            html.append("<input type=\"hidden\" name=\"input\" value=\"").append(input).append("\">");
            html.append("<input type=\"hidden\" name=\"output\" value=\"").append(output).append("\">");
            html.append("<input type=\"hidden\" name=\"type\" value=\"").append(type == null ? "" : type.toString().toLowerCase()).append("\">");
            html.append("<input type=\"hidden\" name=\"viewonly\" value=\"").append(viewOnly ? "Y" : "N").append("\">");
            html.append("<textarea style=\"display:none;\" name=\"properties\">").append(props != null ? props.toJSONString(false) : "{}").append("</textarea>");
            html.append("</form>");
            html.append("<script>workflow_props_form.submit();</script>");
        } else {
            html.append("<font style=\"color:red;\">No connection Id provided</font>");
        }
        return html.toString();
    }

    public static enum Tools {
        BOX("Box", "Drawing", "rc?command=image&image=ShapeSquare", "{\"showline\":\"Y\",\"linecolor\":\"#385D8A\",\"linewidth\":\"2\",\"linestyle\":\"solid\",\"showbackground\":\"Y\",\"backgroundcolor\":\"#4F81BD\",\"backgroundopacity\":\"0.2\"}"),
        LABEL("Label", "Text", "rc?command=image&image=Font", "{\"text\":\"A Label\",\"textcolor\":\"black\",\"fontsize\":\"10\"}"),
        LINE("Line", "Drawing", "rc?command=image&image=GraphEdge", "{\"sourcearrow\":\"N\",\"targetarrow\":\"N\",\"linecolor\":\"#a7b04b\",\"linewidth\":\"2\",\"linestyle\":\"solid\"}"),
        TEXT("Textbox", "Text", "rc?command=image&image=Text", "{\"text\":\"A Textbox\",\"textcolor\":\"black\",\"fontsize\":\"10\",\"showline\":\"Y\",\"linecolor\":\"grey\",\"linewidth\":\"2\",\"linestyle\":\"dashed\",\"showbackground\":\"Y\",\"backgroundcolor\":\"white\",\"backgroundopacity\":\"1\"}"),
        CIRCLE("Circle", "Drawing", "rc?command=image&image=ShapeCircle", "{\"showline\":\"Y\",\"linecolor\":\"#8C3836\",\"linewidth\":\"2\",\"linestyle\":\"solid\",\"showbackground\":\"Y\",\"backgroundcolor\":\"#C0504D\",\"backgroundopacity\":\"0.2\"}"),
        IMAGE("Image", "Drawing", "rc?command=image&image=PhotoLandscape2", "{\"image\":\"rc?command=image&image=PhotoLandscape2&size=48\",\"showline\":\"Y\",\"linecolor\":\"#8C3836\",\"linewidth\":\"1\",\"linestyle\":\"solid\",\"showbackground\":\"N\",\"backgroundcolor\":\"white\",\"backgroundopacity\":\"1\"}"),
        TASK("Empty Task", "Prototyping", "rc?command=image&image=DrawingUtensils", "{\"text\":\"Empty Task\",\"image\":\"rc?command=image&image=DrawingUtensils&size=32\"}");

        private String title;
        private String group;
        private String image;
        private PropertyList defaultProps;

        private Tools(String t, String g, String i, String p) {
            this.title = t;
            this.group = g;
            this.image = i;
            try {
                this.defaultProps = new PropertyList(new JSONObject(p));
            }
            catch (Exception e) {
                this.defaultProps = new PropertyList();
            }
            this.defaultProps.setProperty("type", this.toString().toLowerCase());
        }

        public String getTitle() {
            return this.title;
        }

        public String getGroup() {
            return this.group;
        }

        public String getImage() {
            return this.image;
        }

        public PropertyList getProperties() {
            return this.defaultProps.copy();
        }

        public PropertyList getProperties(String id, String x, String y) {
            double xd = 0.0;
            double yd = 0.0;
            try {
                xd = Double.parseDouble(x);
            }
            catch (Exception e1) {
                xd = 0.0;
            }
            try {
                yd = Double.parseDouble(y);
            }
            catch (Exception e1) {
                yd = 0.0;
            }
            PropertyList out = this.defaultProps.copy();
            out.setProperty("elementid", id);
            if (this == LINE) {
                out.setProperty("x", "" + (xd + 50.0));
                out.setProperty("y", "" + (yd + 0.0));
                out.setProperty("x2", "" + (xd + 0.0));
                out.setProperty("y2", "" + (yd + 50.0));
            } else {
                out.setProperty("x", "" + xd);
                out.setProperty("y", "" + yd);
            }
            return out;
        }
    }

    public static enum ToolGrouping {
        CATEGORY("Category"),
        GROUP("Task Group"),
        TASK("Task Id"),
        VARIANT("Variant Id"),
        INPUT("Input Queues"),
        OUTPUT("Output Queues"),
        PARENT("Parent Task"),
        NONE("None");

        private String title;

        private ToolGrouping(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }
    }

    public static enum Connector {
        CURVED("[\"StateMachine\",{curviness:-18,proximityLimit:100}]"),
        BEZIER("[\"Bezier\",{curviness:100}]"),
        STRAIGHT("[\"Straight\",{}]"),
        FLOWCHART("[\"Flowchart\",{stub:40}]");

        private String conn;

        private Connector(String c) {
            this.conn = c;
        }

        public String getConnector() {
            return this.conn;
        }
    }
}

