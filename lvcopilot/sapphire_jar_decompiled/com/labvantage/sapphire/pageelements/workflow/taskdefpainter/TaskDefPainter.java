/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 *  org.apache.xpath.XPathAPI
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.PropertyTreeRenderer;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.workflow.StepUtil;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.controls.Tree;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefProperties;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefVariables;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflow;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.MiscUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.apache.xpath.XPathAPI;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefPainter
extends BaseElement {
    public static final String ELEMENT_PREFIX = "painter_element_";
    public static final String CACHE_TASKDEFOBJECTS = "taskdef_objects_pd";
    public static final String USERCONFIG_PREFIX = "taskdef_";
    public static final String JS_CLASS = "taskDef";
    public static final String SESSION_PREFIX = "taskdef_";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_PROPERTYCONTAINER = "propertycontainer";
    public static final String PROPERTY_ZOOM = "zoom";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_DESCENDANT = "descendant";
    public static final String PROPERTY_PAINTERONLY = "painteronly";
    public static final String PROPERTY_TOOLBOX = "toolbox";
    public static final String PROPERTY_RENDERHTML5 = "renderhtml5";
    public static final String PROPERTY_EMBEDDED = "embedded";
    public static final String SDCID = "LV_TaskDef";
    public static final String DATACOL = "taskdef";
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private String sdcid = "LV_TaskDef";
    private PropertyList userConfig;
    private int zoom = 100;
    private boolean viewonly = false;
    private boolean rtlFlag = false;
    private boolean descendant = false;
    private boolean painteronly = false;
    private boolean embedded = false;
    private String propertyContainer = "";
    private boolean devMode;
    PropertyList taskprops = null;
    PropertyList taskdefOverrides = null;
    private String toolAppearance = "";
    private WebAdminProcessor webAdminProcessor = null;
    private boolean renderHtml5 = false;

    private WebAdminProcessor getWebAdminProcessor() {
        if (this.webAdminProcessor == null) {
            this.webAdminProcessor = new WebAdminProcessor(this.getConnectionId());
        }
        return this.webAdminProcessor;
    }

    public TaskDefPainter(PageContext pageContext, PropertyList pageproperties) {
        this.setPageContext(pageContext);
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            try {
                this.devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
            }
            catch (Exception e) {
                this.devMode = false;
            }
            this.rtlFlag = this.getConnectionProcessor().getSapphireConnection().isRtl();
            this.setUpProperties(pageproperties, (HttpServletRequest)pageContext.getRequest());
            if (this.taskprops == null) {
                this.taskprops = TaskDefMaint.getTaskData(this.sdcid, this.keyid1, this.keyid2, this.keyid3, !this.viewonly, this.getSDIProcessor(), this.logger);
                if (this.taskdefOverrides != null) {
                    PropertyListCollection steps = this.taskprops.getCollection("steps");
                    steps.index("stepid");
                    PropertyListCollection stepsOverride = this.taskdefOverrides.getCollection("steps");
                    if (stepsOverride != null) {
                        for (int i = 0; i < stepsOverride.size(); ++i) {
                            PropertyList stepOverride = stepsOverride.getPropertyList(i);
                            PropertyList step = steps.getIndexedPropertyList(stepOverride.getProperty("stepid"));
                            for (String propertyid : stepOverride.keySet()) {
                                step.setProperty(propertyid, stepOverride.getProperty(propertyid));
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            this.taskprops = null;
            this.logger.error("Could not set up painter: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        String overrides;
        pagedata.setProperty("jsrequest", "exclude=properties");
        this.renderHtml5 = pagedata.getProperty(PROPERTY_RENDERHTML5, "N").equalsIgnoreCase("Y");
        String show = pagedata.getProperty("showtitle", "N");
        this.logger.debug("show = " + show);
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
        this.toolAppearance = pagedata.getProperty(PROPERTY_TOOLBOX, "");
        this.logger.debug("toolAppearance = " + this.toolAppearance);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.descendant = pagedata.getProperty(PROPERTY_DESCENDANT, "n").equalsIgnoreCase("y");
        this.logger.debug("descendant = " + this.descendant);
        this.painteronly = pagedata.getProperty(PROPERTY_PAINTERONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("painteronly = " + this.painteronly);
        this.embedded = pagedata.getProperty(PROPERTY_EMBEDDED, "n").equalsIgnoreCase("y");
        this.logger.debug("embedded = " + this.embedded);
        this.propertyContainer = pagedata.getProperty(PROPERTY_PROPERTYCONTAINER, "");
        this.logger.debug("propertyContainer = " + this.propertyContainer);
        String taskp = pagedata.getProperty(PROPERTY_PROPERTIES, "");
        if (taskp.length() > 0) {
            try {
                this.taskprops = new PropertyList(new JSONObject(taskp));
            }
            catch (Exception e) {
                this.taskprops = null;
                this.logger.warn("Invalid property definition provided.");
            }
        }
        if ((overrides = pagedata.getProperty("taskdefoverrides", "")).length() > 0) {
            try {
                this.taskdefOverrides = new PropertyList(new JSONObject(overrides));
            }
            catch (Exception e) {
                this.taskdefOverrides = null;
                this.logger.warn("Invalid taskdef overrides definition provided.");
            }
        }
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig == null) {
            throw new SapphireException("User configuration could not be obtained.");
        }
        try {
            this.zoom = Integer.parseInt(pagedata.getProperty(PROPERTY_ZOOM, this.userConfig.getProperty("taskdef_zoom", "100")));
        }
        catch (Exception e) {
            this.zoom = 100;
        }
    }

    public static String getToolsHtml(PropertyList props, SapphireConnection sapphireConnection, SDIProcessor sdi, TranslationProcessor tp, HttpServletRequest request, WebAdminProcessor wap, Logger logger) {
        return TaskDefPainter.getToolsHtml(props, "", sapphireConnection, sdi, tp, request, wap, logger);
    }

    public static PropertyList getStepTypeProperties(String steptypeid, String nodeid, WebAdminProcessor wap, Logger logger) {
        PropertyList steptypePropertyList = null;
        try {
            PropertyTree stepType = wap.getPropertyTree(steptypeid);
            if (stepType != null) {
                steptypePropertyList = stepType.getNodePropertyList(nodeid, true);
                String objectname = wap.getPropertyTreeObject(steptypeid);
                steptypePropertyList.setProperty("objectname", objectname);
            }
        }
        catch (Exception e) {
            logger.warn("Could not get property tree.");
        }
        if (steptypePropertyList == null) {
            logger.warn("Unable to load properties for step type " + steptypeid + " and node " + nodeid);
            steptypePropertyList = new PropertyList();
        }
        return steptypePropertyList;
    }

    private static PropertyListCollection getStepTypeNodeList(String steptypeid, WebAdminProcessor wap, Logger logger) {
        PropertyListCollection out = new PropertyListCollection();
        try {
            Node ptreenode = wap.loadPropertyTreeValues(steptypeid);
            if (ptreenode != null) {
                NodeList nl = XPathAPI.selectNodeList((Node)ptreenode, (String)".//node");
                for (int i = 0; i < nl.getLength(); ++i) {
                    Element node = (Element)nl.item(i);
                    String id = node.getAttribute("id");
                    PropertyList props = null;
                    if (id.equalsIgnoreCase("sapphire custom")) {
                        props = TaskDefPainter.getStepTypeProperties(steptypeid, id, wap, logger);
                    } else {
                        ArrayList childNodes = PropertyTreeRenderer.getChildNodes(node);
                        if (childNodes == null || childNodes.size() == 0) {
                            props = TaskDefPainter.getStepTypeProperties(steptypeid, id, wap, logger);
                        }
                    }
                    if (props == null || props.size() <= 0 || props.getPropertyList("stepdef") == null || !props.getPropertyList("stepdef").getProperty(PROPERTY_TOOLBOX, "Y").equalsIgnoreCase("Y")) continue;
                    props.setId(id);
                    out.add(props);
                }
            }
        }
        catch (Exception e) {
            logger.error("Could not get node list.", e);
        }
        return out;
    }

    protected static PropertyListCollection getStepTypes(SDIProcessor sdi, WebAdminProcessor wap, SapphireConnection sapphireConnection, Logger logger) {
        PropertyListCollection out;
        Object cache = null;
        if (cache != null && cache instanceof PropertyListCollection) {
            out = cache;
        } else {
            out = new PropertyListCollection();
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("PropertyTree");
            sdiRequest.setQueryFrom("propertytree");
            sdiRequest.setQueryWhere("propertytreetype='Step'");
            sdiRequest.setExtendedDataTypes(true);
            sdiRequest.setRequestItem("primary");
            SDIData sdiData = sdi.getSDIData(sdiRequest);
            if (sdi != null) {
                DataSet primary = sdiData.getDataset("primary");
                if (primary != null) {
                    for (int i = 0; i < primary.getRowCount(); ++i) {
                        PropertyListCollection nodes;
                        String ptreeid = primary.getValue(i, "propertytreeid", "");
                        if (ptreeid.length() <= 0 || (nodes = TaskDefPainter.getStepTypeNodeList(ptreeid, wap, logger)) == null || nodes.size() <= 0) continue;
                        PropertyList steptype = new PropertyList();
                        steptype.setProperty("steptypeid", ptreeid);
                        steptype.setProperty("steptype", primary.getValue(i, "objectname", ""));
                        steptype.setProperty("nodes", nodes);
                        out.add(steptype);
                    }
                } else {
                    logger.warn("Could not obtain propertytree for steptype.");
                }
            } else {
                logger.warn("Ppropertytree query failed for steptype.");
            }
        }
        return out;
    }

    public static String getToolsHtml(PropertyList props, String toolAppearance, SapphireConnection sapphireConnection, SDIProcessor sdi, TranslationProcessor tp, HttpServletRequest request, WebAdminProcessor wap, Logger logger) {
        String connectionId;
        RequestContext rc;
        StringBuffer sb = new StringBuffer();
        Comparator<String> comparator = new Comparator<String>(){

            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        };
        TreeMap<String, StringBuffer> groups = new TreeMap<String, StringBuffer>(comparator);
        boolean autoExec = props.getProperty("autoexec", "N").equalsIgnoreCase("Y");
        PropertyList userConfig = null;
        if (request != null && (rc = RequestContext.getInstance(request)) != null) {
            userConfig = rc.getPropertyList("userconfig");
        }
        if ((connectionId = sapphireConnection.getConnectionId()) != null && connectionId.length() > 0) {
            boolean grouped;
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            boolean bl = grouped = !toolAppearance.equalsIgnoreCase("old") && !toolAppearance.equalsIgnoreCase("ungrouped");
            if (toolAppearance.equalsIgnoreCase("ungrouped")) {
                toolAppearance = "";
            }
            PropertyListCollection stepTypes = TaskDefPainter.getStepTypes(sdi, wap, sapphireConnection, logger);
            String[] autoexecSteps = null;
            if (autoExec) {
                autoexecSteps = StepUtil.getAutoExecuteStepTypes();
            }
            int count = 0;
            for (int s = 0; s < stepTypes.size(); ++s) {
                StringBuffer current;
                String stepName;
                PropertyList stepType = stepTypes.getPropertyList(s);
                String stepTypeId = stepType.getProperty("steptypeid");
                String objectName = stepType.getProperty("steptype");
                String string = stepName = stepTypeId.endsWith("Step") ? stepTypeId.substring(0, stepTypeId.length() - 4) : stepTypeId;
                if (autoexecSteps != null && !MiscUtil.MiscArray.isStringInArray(autoexecSteps, objectName, false)) continue;
                String group = stepName;
                if (grouped) {
                    if (groups.size() == 0 || !groups.containsKey(group)) {
                        current = new StringBuffer();
                        groups.put(group, current);
                    } else {
                        current = (StringBuffer)groups.get(group);
                    }
                } else {
                    current = sb;
                }
                PropertyListCollection stepNodes = stepType.getCollection("nodes");
                for (int n = 0; n < stepNodes.size(); ++n) {
                    PropertyList stepTypeNode = stepNodes.getPropertyList(n);
                    if (stepTypeNode.size() <= 0 || stepTypeNode.getPropertyList("stepdef") == null) continue;
                    String nodename = stepTypeNode.getId();
                    if (nodename.equals("Sapphire Custom")) {
                        nodename = group;
                    } else {
                        int ni = nodename.lastIndexOf(" Custom");
                        if (ni > -1) {
                            nodename = nodename.substring(0, ni);
                        } else {
                            ni = nodename.lastIndexOf(" Product");
                            if (ni > -1) {
                                nodename = nodename.substring(0, ni);
                            }
                        }
                    }
                    String title = stepTypeNode.getPropertyList("stepdef").getProperty("title", nodename);
                    String image = stepTypeNode.getPropertyList("stepdef").getProperty("icon", "WEB-CORE/elements/workflow/images/NoImage16.png");
                    String key = stepTypeId + ";" + stepTypeNode.getId();
                    Image img = new Image();
                    img.setConnectionId(sapphireConnection.getConnectionId());
                    img.setImageSrc(image);
                    img.setDimensions(16, 16);
                    current.append("<div id=\"painter_tool").append(count).append("_container\" class=\"painter_tool_container").append(toolAppearance.length() > 0 ? "_" + toolAppearance : "").append("\">");
                    current.append("<div class=\"painter_tool_in painter_tool").append(toolAppearance.length() > 0 ? "_" + toolAppearance : "").append("\" id=\"painter_tool").append(count).append("\" toolid=\"").append(key).append("\"><div class=\"painter_toolimg").append(toolAppearance.length() > 0 ? "_" + toolAppearance : "").append("\">").append(img.getHtml()).append("</div><div class=\"painter_toollabel").append(toolAppearance.length() > 0 ? "_" + toolAppearance : "").append("\">").append(title).append("</div></div>");
                    current.append("</div>");
                    ++count;
                }
            }
            sb.append("<div id=\"painter_tools_accordian\">");
            sb.append("<div>");
            sb.append("<h3><a href=\"#\">").append(tp.translate("Drawing & Text Tools")).append("</a></h3>");
            sb.append("<div group=\"Drawing & Text Tools\">");
            for (Tools tool : Tools.values()) {
                String title = tool.getTitle();
                String image = tool.getImage();
                String key = tool.toString();
                Image img = new Image();
                img.setConnectionId(sapphireConnection.getConnectionId());
                img.setImageSrc(image);
                img.setDimensions(16, 16);
                sb.append("<div id=\"painter_tool").append(count).append("_container\" class=\"painter_tool_container").append(toolAppearance.length() > 0 ? "_" + toolAppearance : "").append("\">");
                sb.append("<div class=\"painter_tool_in painter_tool").append(toolAppearance.length() > 0 ? "_" + toolAppearance : "").append("\" id=\"painter_tool").append(count).append("\" toolid=\"").append(key).append("\"><div class=\"painter_toolimg").append(toolAppearance.length() > 0 ? "_" + toolAppearance : "").append("\">").append(img.getHtml()).append("</div><div class=\"painter_toollabel").append(toolAppearance.length() > 0 ? "_" + toolAppearance : "").append("\">").append(title).append("</div></div>");
                sb.append("</div>");
                ++count;
            }
            sb.append("</div>");
            sb.append("</div>");
            for (String group : groups.keySet()) {
                sb.append("<div>");
                sb.append("<h3><a href=\"#\">").append(group).append("</a></h3>");
                sb.append("<div group=\"" + group + "\">");
                sb.append((StringBuffer)groups.get(group));
                sb.append("</div>");
                sb.append("</div>");
            }
            sb.append("</div>");
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
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/workflow/stylesheets/painter.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/workflow/stylesheets/taskdef.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/taskdef").append("").append(".js\"></script>");
        html.append("<style>");
        if (this.rtlFlag) {
            html.append(".ui-accordion .ui-accordion-header .ui-accordion-header-icon{ right:.5em; background-position-x: -91px; }");
            html.append("div.painter_leftbar_expand_nonie{right:-50px;}");
        }
        html.append("</style>");
        html.append(Tree.getScriptInclude());
        return html;
    }

    public static int getNextIdIndex(PropertyListCollection collection, int current, String idProp, String prefix) {
        if (collection != null) {
            int next;
            int n = next = current == -1 ? collection.size() : current + 1;
            while (collection.find(idProp, prefix + next) != null) {
                ++next;
            }
            return next;
        }
        return -1;
    }

    private StringBuffer getEndScript(PropertyList props, String sdcid, String keyid1, String keyid2, String keyid3, String propertyContainer, boolean viewOnly, boolean descendant, boolean painterOnly, boolean embedded, int zoom) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        if (propertyContainer.length() > 0) {
            html.append(JS_CLASS).append(".properties=").append(propertyContainer).append(";");
        } else {
            html.append(JS_CLASS).append(".properties=sapphire.util.propertyList.create(").append(props.toJSONString(false)).append(");");
        }
        html.append(JS_CLASS).append(".viewonly=").append(viewOnly).append(";");
        html.append(JS_CLASS).append(".rtl=").append(this.rtlFlag).append(";");
        html.append(JS_CLASS).append(".descendant=").append(descendant).append(";");
        html.append(JS_CLASS).append(".painteronly=").append(painterOnly).append(";");
        html.append(JS_CLASS).append(".embedded=").append(embedded).append(";");
        html.append(JS_CLASS).append(".sdcid='").append(sdcid).append("';");
        html.append(JS_CLASS).append(".keyid1='").append(keyid1).append("';");
        html.append(JS_CLASS).append(".keyid2='").append(keyid2).append("';");
        html.append(JS_CLASS).append(".keyid3='").append(keyid3).append("';");
        html.append(JS_CLASS).append(".zoom=").append(this.browser.isIE() ? 100 : zoom).append(";");
        html.append(JS_CLASS).append(".canvasTop=").append(0).append(";");
        html.append(JS_CLASS).append(".stepIndex=").append(TaskDefPainter.getNextIdIndex(props.getCollection("steps"), -1, "stepid", "step")).append(";");
        String toolgroups = this.userConfig.getProperty("taskdef_toolgroups", "{}");
        if (toolgroups.length() == 0 || !toolgroups.startsWith("{") || !toolgroups.endsWith("}")) {
            toolgroups = "{}";
        }
        html.append(JS_CLASS).append(".toolGroups=").append(toolgroups).append(";");
        html.append(JS_CLASS).append(".variableGroup='").append(this.userConfig.getProperty("taskdef_variablesgroupby", TaskDefVariables.Grouping.NONE.toString())).append("';");
        html.append(JS_CLASS).append(".variableSort='").append(this.userConfig.getProperty("taskdef_variablessort", "")).append("';");
        html.append("</script>");
        return html;
    }

    private static String getProperty(PropertyList toProperties, PropertyList fromProperties, String toPropertyId, String fromPropertyId, String defaultProperty) {
        String out;
        if (toProperties.containsKey(toPropertyId)) {
            out = toProperties.getProperty(toPropertyId, defaultProperty);
        } else {
            toProperties.setProperty(toPropertyId, fromProperties.getProperty(fromPropertyId, defaultProperty));
            out = toProperties.getProperty(toPropertyId, defaultProperty);
        }
        return out;
    }

    private static String getProperty(PropertyList toProperties, PropertyList fromProperties, String propertyId, String defaultProperty) {
        return TaskDefPainter.getProperty(toProperties, fromProperties, propertyId, propertyId, defaultProperty);
    }

    private static void mergeButtons(PropertyListCollection toButtons, PropertyListCollection fromButtons, boolean all) {
        for (int i = 0; i < fromButtons.size(); ++i) {
            PropertyList buttonprop = fromButtons.getPropertyList(i);
            if (!all && !buttonprop.getProperty("autoadd", "N").equalsIgnoreCase("Y")) continue;
            PropertyList newBtn = buttonprop.copy();
            if (newBtn.containsKey("autoadd")) {
                newBtn.remove("autoadd");
            }
            String newid = newBtn.getProperty("buttonid", "button") + (int)Math.floor(Math.random() * 1000.0);
            newBtn.setProperty("buttonid", newid);
            toButtons.add(newBtn);
        }
    }

    protected static void removeWindow(PropertyList taskprops, String stepid) {
        PropertyListCollection steps = taskprops.getCollection("steps");
        if (steps != null) {
            PropertyList step = steps.find("stepid", stepid);
            if (step != null) {
                steps.remove(step);
            }
            for (int i = 0; i < steps.size(); ++i) {
                PropertyListCollection tranistions;
                PropertyList next = steps.getPropertyList(i).getPropertyList("next");
                if (next == null || (tranistions = next.getCollection("transitions")) == null) continue;
                PropertyList trans = tranistions.find("stepid", stepid);
                while (trans != null) {
                    tranistions.remove(trans);
                    trans = tranistions.find("stepid", stepid);
                }
            }
        }
    }

    protected static void removeElement(PropertyList taskprops, String elementid) {
        PropertyList element;
        PropertyListCollection elements = taskprops.getCollection("elements");
        if (elements != null && (element = elements.find("elementid", elementid)) != null) {
            elements.remove(element);
        }
    }

    protected static String renderElement(PropertyList elementProps, int index, StringBuffer html, StringBuffer script, boolean visible, boolean viewonly, boolean html5, boolean isSavedInRTL, Browser browser, String connectionId, TranslationProcessor tp, Logger logger) {
        Tools elementtype;
        String elementId = elementProps.getProperty("elementid");
        try {
            elementtype = Tools.valueOf(elementProps.getProperty("type", Tools.LABEL.toString()).toUpperCase());
        }
        catch (Exception e) {
            elementtype = Tools.LABEL;
        }
        if (elementId.length() > 0) {
            boolean rtl = new ConnectionProcessor(connectionId).getSapphireConnection().isRtl();
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
            String onclick = "taskDef.elementClick(this,event);sapphire.events.cancelEvent(event,true);";
            boolean showWorkflowMirror = false;
            showWorkflowMirror = rtl ? !isSavedInRTL : isSavedInRTL;
            if (elementtype != Tools.LINE) {
                html.append("<div class=\"painter_element").append(className).append("\" id=\"").append(windowid).append("\" name=\"").append(windowid).append("\" windowid=\"").append(elementId).append("\" windowtype=\"").append(elementtype.toString().toLowerCase()).append("\"");
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
                    html.append("<div id=\"").append(windowid).append("_div\" style=\"").append(borderStyle).append("").append(backStyle).append("font-size:").append(elementProps.getProperty("fontsize", "10")).append("pt;color:").append(elementProps.getProperty("color", "black")).append(";width:").append(elementProps.getProperty("width", "100")).append("px;height:").append(elementProps.getProperty("height", "30")).append("px;overflow:hidden;padding: 5px 5px 5px 5px;").append("\">");
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
                    img.setConnectionId(connectionId);
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

    protected static String renderWindow(PropertyList props, String stepTypeId, String stepTypeNode, int index, StringBuffer html, StringBuffer script, boolean visible, boolean viewonly, String startstepid, boolean devMode, boolean html5, boolean isSavedInRTL, HttpSession session, WebAdminProcessor wap, Browser browser, SapphireConnection sapphireConnection, Logger logger) {
        boolean rtl;
        boolean bl = rtl = sapphireConnection != null && sapphireConnection.isRtl();
        if (stepTypeId == null || stepTypeId.length() == 0) {
            stepTypeId = props.getProperty("propertytreeid", "");
        } else {
            props.setProperty("propertytreeid", stepTypeId);
        }
        if (stepTypeNode == null || stepTypeNode.length() == 0) {
            stepTypeNode = props.getProperty("extendnodeid", "");
        } else {
            props.setProperty("extendnodeid", stepTypeNode);
        }
        PropertyList stepType = TaskDefPainter.getStepTypeProperties(stepTypeId, stepTypeNode, wap, logger);
        if (stepType != null && stepType.size() > 0 && stepType.getPropertyList("stepdef") != null) {
            String bgcolor;
            String nodename;
            String stepid = props.getProperty("stepid", "");
            if (stepid.length() == 0) {
                props.setProperty("stepid", "step" + index);
                stepid = props.getProperty("stepid", "");
            }
            if (!props.containsKey("buttons") || props.getCollection("buttons") == null) {
                PropertyListCollection buttons = new PropertyListCollection();
                PropertyList buttonprops = TaskDefProperties.getDefaultStepButtons(devMode, logger, session);
                if (buttonprops != null && buttonprops.getCollection("buttons") != null) {
                    TaskDefPainter.mergeButtons(buttons, buttonprops.getCollection("buttons"), true);
                }
                if (stepType.getPropertyList("stepdef") != null && stepType.getPropertyList("stepdef").getCollection("buttontemplates") != null) {
                    TaskDefPainter.mergeButtons(buttons, stepType.getPropertyList("stepdef").getCollection("buttontemplates"), false);
                }
                props.setProperty("buttons", buttons);
            }
            if (!props.containsKey("next") || props.getPropertyList("next") == null) {
                PropertyList t;
                PropertyList next = new PropertyList();
                props.setProperty("next", next);
                if (stepType.getPropertyList("stepdef") != null && stepType.getPropertyList("stepdef").getPropertyList("transitions") != null && (t = stepType.getPropertyList("stepdef").getPropertyList("transitions")) != null) {
                    if (t.getProperty("precaseon").length() > 0 && next.getProperty("precaseon").length() == 0) {
                        next.setProperty("precaseon", t.getProperty("precaseon"));
                    }
                    if (t.getProperty("caseon").length() > 0 && next.getProperty("caseon").length() == 0) {
                        next.setProperty("caseon", t.getProperty("caseon"));
                    }
                    if (t.getCollection("cases") != null) {
                        PropertyListCollection cases = t.getCollection("cases");
                        for (int c = 0; c < cases.size(); ++c) {
                            String casetext = cases.getPropertyList(c).getProperty("case");
                            String caselabel = cases.getPropertyList(c).getProperty("label");
                            if (casetext.length() <= 0) continue;
                            PropertyList transition = null;
                            if (next.getCollection("transitions") != null) {
                                transition = next.getCollection("transitions").find("case", casetext);
                                if (transition == null) {
                                    transition = new PropertyList();
                                    next.getCollection("transitions").add(transition);
                                }
                            } else {
                                next.setProperty("transitions", new PropertyListCollection());
                                transition = new PropertyList();
                                next.getCollection("transitions").add(transition);
                            }
                            if (transition == null) continue;
                            if (transition.getProperty("case").length() == 0) {
                                transition.setProperty("case", casetext);
                            }
                            if (transition.getProperty("label").length() != 0) continue;
                            transition.setProperty("text", caselabel);
                        }
                    }
                }
            }
            if ((nodename = stepTypeNode).equals("Sapphire Custom")) {
                nodename = stepTypeId.endsWith("Step") ? stepTypeId.substring(0, stepTypeId.length() - 4) : stepTypeId;
            } else if (nodename.endsWith(" Custom")) {
                nodename = nodename.substring(0, nodename.length() - 7);
            } else if (nodename.endsWith(" Product")) {
                nodename = nodename.substring(0, nodename.length() - 8);
            }
            String group = stepTypeId.endsWith("Step") ? stepType.getProperty("objectname", stepTypeId) : stepTypeId;
            String hint = "" + group + " Step" + (nodename.length() > 0 && !nodename.equals(group) ? " (" + nodename + ")" : "") + "";
            String shorttitle = TaskDefPainter.getProperty(props, stepType.getPropertyList("stepdef"), "shorttitle", "icontext", "");
            String longtitle = TaskDefPainter.getProperty(props, stepType.getPropertyList("stepdef"), "title", "executiontitle", "");
            if (longtitle.length() == 0) {
                longtitle = nodename + " Step";
                props.setProperty("title", longtitle);
            }
            props.setProperty("instructions", stepType.getPropertyList("stepdef").getProperty("instructions", ""));
            String title = shorttitle.length() > 0 ? shorttitle : (longtitle.length() > 0 ? longtitle : stepid);
            double x = 0.0;
            double y = 0.0;
            try {
                x = Double.parseDouble(props.getProperty("x", "" + (index * 210 + 20)));
            }
            catch (Exception e1) {
                logger.warn("Invalid x coord for step " + stepid + ".");
            }
            try {
                y = Double.parseDouble(props.getProperty("y", "" + (index * 50 + 20)));
            }
            catch (Exception e1) {
                logger.warn("Invalid y coord for step " + stepid + ".");
            }
            String color = TaskDefPainter.getProperty(props, stepType.getPropertyList("stepdef"), "color", "");
            String winstyle = "";
            if (color.length() > 0) {
                winstyle = "-webkit-box-shadow: 0px 0px 17px " + color + ";box-shadow: 0px 0px 17px " + color + ";color:" + color + ";";
            }
            boolean startStep = startstepid.equals(stepid);
            if (browser.isIE() && (browser.getVersion() < 9.0 || !html5)) {
                winstyle = winstyle + "filter:none;";
            }
            boolean showWorkflowMirror = false;
            showWorkflowMirror = sapphireConnection != null && sapphireConnection.isRtl() ? !isSavedInRTL : isSavedInRTL;
            String string = bgcolor = stepType != null && stepType.getPropertyList("stepdef") != null ? stepType.getPropertyList("stepdef").getProperty("backgroundcolor", "") : "";
            if (bgcolor.length() == 0) {
                bgcolor = StepUtil.getStepTypeColor(stepType.getProperty("objectname", stepTypeId));
            }
            if (bgcolor.length() > 0) {
                winstyle = winstyle + "background:" + bgcolor + ";";
            }
            html.append("<div class=\"painter_window").append(startStep ? " painter_window_start" : "").append("").append(browser.isIE() && browser.getVersion() == 9.0 && html5 ? (startStep ? " painter_windowsvg_start" : " painter_windowsvg") : "").append("\" id=\"painter_window").append(index).append("\" style=\"").append(winstyle).append("top:").append(y).append("px;").append(showWorkflowMirror ? "right:" : "left:").append(x).append("px;").append(visible ? "" : "display:none;").append("\" windowid=\"").append(stepid).append("\" windowtype=\"").append("").append("\"");
            String onclick = "taskDef.stepClick(this, event);";
            String proponclick = TaskDefPainter.getProperty(props, stepType.getPropertyList("stepdef"), "onclick", "");
            if (proponclick.length() > 0) {
                onclick = onclick + proponclick;
            }
            String image = TaskDefPainter.getProperty(props, stepType.getPropertyList("stepdef"), "icon", "WEB-CORE/elements/workflow/images/NoImage16.png");
            html.append(" onclick=\"").append(onclick).append("\"");
            html.append(" title=\"").append(hint).append("\"");
            html.append(">");
            if (browser.isIE() && (browser.getVersion() < 9.0 || !html5)) {
                html.append("<div class=\"painter_windowbg").append(startStep ? "_start" : "").append("\" style=\"display:none;\"></div>");
            }
            Image img = new Image();
            img.setConnectionId(wap.getConnectionid());
            img.setImageSrc(image);
            img.setDimensions(16, 16);
            img.setTitle(hint);
            html.append("<div class=\"painter_windowimg\">").append(img.getHtml()).append("</div>").append("<div class=\"painter_windowlabel\"").append("").append(">").append(title).append("</div>");
            if (!viewonly) {
                html.append("<div class=\"painter_endpoint\"></div>");
            }
            String annotation = TaskDefPainter.getProperty(props, stepType.getPropertyList("stepdef"), "annotation", "");
            html.append("<div class=\"painter_annotate\">").append(annotation).append("</div>");
            html.append("</div>");
            if (!viewonly) {
                script.append("jsPlumb.addEndpoint('painter_window").append(index).append("', ").append(JS_CLASS).append(rtl ? ".plumb.anchorFixedRtl," : ".plumb.anchorFixed,").append(JS_CLASS).append(".plumb.endpoint);");
            }
            return stepid;
        }
        logger.error("No step type properties could be found");
        return "";
    }

    private String getPainterHtml(PropertyList taskDef, boolean viewOnly, boolean descendant, boolean embedded, boolean devMode, int zoom) {
        boolean fullViewOnly;
        StringBuffer html = new StringBuffer();
        StringBuffer script = new StringBuffer();
        double top = 0.0;
        boolean bl = fullViewOnly = viewOnly || descendant;
        if (this.painteronly) {
            html.append("<div id=\"painter_canvas_holder\" style=\"top:").append(top).append("px\">");
        } else {
            html.append("<div id=\"painter_canvas_holder\" class=\"painter_taskbox\" style=\"top:").append(top).append("px\">");
        }
        html.append("<div id=\"painter_canvas\" class=\"painter_canvas").append("").append("\" style=\"").append(fullViewOnly && this.painteronly ? "background-image:none;" : "").append("top:").append(top).append("px;zoom:").append(this.browser.isIE() ? 100 : zoom).append("%;\" onclick=\"taskDef.canvasClick(event)\">");
        PropertyListCollection steps = taskDef.getCollection("steps");
        int stepindex = -1;
        if (steps != null && steps.size() > 0) {
            PropertyList step;
            int i;
            HashMap<String, Integer> stepmap = new HashMap<String, Integer>(steps.size());
            for (i = 0; i < steps.size(); ++i) {
                step = steps.getPropertyList(i);
                String stepid = TaskDefPainter.renderWindow(step, null, null, i, html, script, true, fullViewOnly, this.taskprops.getProperty("startstepid", ""), devMode, this.renderHtml5, taskDef.getProperty("rtl", "N").equalsIgnoreCase("Y"), this.pageContext.getSession(), this.getWebAdminProcessor(), this.browser, this.getConnectionProcessor().getSapphireConnection(), this.logger);
                if (stepid.length() <= 0) continue;
                stepmap.put(stepid, i);
            }
            for (i = 0; i < steps.size(); ++i) {
                PropertyListCollection transitions;
                PropertyList next;
                step = steps.getPropertyList(i);
                String fromStepid = step.getProperty("stepid", "");
                if (fromStepid.length() <= 0 || (next = step.getPropertyList("next")) == null || (transitions = step.getPropertyList("next").getCollection("transitions")) == null || transitions.size() <= 0) continue;
                for (int k = 0; k < transitions.size(); ++k) {
                    PropertyList transition = transitions.getPropertyList(k);
                    String toStepid = transition.getProperty("stepid", "");
                    if (toStepid.length() > 0) {
                        String fromEP = "_ep" + i + "_out";
                        String fromWindow = "painter_window" + i;
                        Integer toStep = (Integer)stepmap.get(toStepid);
                        if (toStep == null || toStep <= -1) continue;
                        String toEP = "_ep" + toStep + "_in";
                        String toWindow = "painter_window" + toStep;
                        script.append("_" + fromStepid + "_to_" + toStepid + "=").append(JS_CLASS).append(".plumb.util.addTransition('painter_window").append(i).append("','painter_window").append(toStep).append("',true,").append(fullViewOnly).append(");");
                        String text = transition.getProperty("text", "");
                        String tcase = transition.getProperty("case", "");
                        if (text.length() <= 0 && tcase.length() <= 0) continue;
                        String label = StringUtil.replaceAll(text, "'", "\\'");
                        if (tcase.length() > 0) {
                            label = label + " (" + StringUtil.replaceAll(tcase, "'", "\\'") + ")";
                        }
                        script.append("_" + fromStepid + "_to_" + toStepid + ".addOverlay([ 'Label', {label: '").append(label).append("',location:0.2,cssClass:'painter_label").append(this.browser.isIE() ? " painter_label_ie" : "").append("'}]);");
                        continue;
                    }
                    this.logger.warn("Missing step id for transition " + k + " on step " + i + ".");
                }
            }
            stepindex = steps.size() - 1;
        }
        PropertyListCollection elements = taskDef.getCollection("elements");
        int elementindex = -1;
        if (elements != null && elements.size() > 0) {
            for (int i = 0; i < elements.size(); ++i) {
                PropertyList element = elements.getPropertyList(i);
                TaskDefPainter.renderElement(element, i, html, script, true, viewOnly, true, taskDef.getProperty("rtl", "N").equalsIgnoreCase("Y"), this.browser, this.getConnectionId(), this.getTranslationProcessor(), this.logger);
            }
            elementindex = elements.size() - 1;
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<script>");
        html.append(JS_CLASS).append(".stepindex = " + stepindex + ";");
        html.append(JS_CLASS).append(".plumb.deploy = function( endpoint1In, endpoint1Out ){");
        html.append(script);
        html.append("};");
        if (!this.painteronly) {
            html.append(JS_CLASS).append(".plumb.deployIOs = function( ){");
            html.append(TaskDefWorkflow.getIOScript("painter_canvas_holder", this.taskprops.getCollection("taskio"), TaskDefWorkflow.getConnectors(this.getSDIProcessor()), false, this.getConnectionProcessor().getSapphireConnection(), this.logger));
            html.append("};");
        }
        html.append("</script>");
        return html.toString();
    }

    private StringBuffer getVariablesHTML(PropertyList props) {
        StringBuffer html = new StringBuffer();
        html.append(TaskDefVariables.getVariablesTreeHTML(props, "", false, "painter_variable", this.viewonly, this.descendant, null, "", this.userConfig, this.pageContext, this.logger));
        return html;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.taskprops != null) {
            boolean toolboxselected;
            boolean showLeft;
            int tbiheight;
            int lbiwidth;
            html.append(this.getScriptAndStyle());
            html.append("<div id=\"painter_root_container\" style=\"overflow-y:").append(this.viewonly ? "hidden" : "auto").append(";\">");
            String lbwidth = this.userConfig.getProperty("taskdef_left_width", "200");
            try {
                lbiwidth = Integer.parseInt(lbwidth);
            }
            catch (Exception e) {
                lbiwidth = 200;
            }
            int vbiwidth = lbiwidth;
            String tbheight = this.userConfig.getProperty("taskdef_top_height", "300");
            try {
                tbiheight = Integer.parseInt(tbheight);
            }
            catch (Exception e) {
                tbiheight = 300;
            }
            boolean showVar = showLeft = this.viewonly || this.painteronly ? false : this.userConfig.getProperty("taskdef_left", this.viewonly ? "N" : "Y").equalsIgnoreCase("Y");
            StringBuffer sb = new StringBuffer();
            if (showLeft) {
                html.append("<div id=\"painter_leftbar_collapsed\" class=\"painter_bar_collapsed\">");
                html.append("<div title=\"").append(this.getTranslationProcessor().translate("Expand Toolbox & Variables")).append("\" id=\"painter_leftbar_expand\" class=\"painter_bar_expand ").append(this.browser.isIE() ? "painter_leftbar_expand_ie" : "painter_leftbar_expand_nonie").append("\" onclick=\"").append(JS_CLASS).append(".ui.expandBars();\">");
                html.append("<img src=\"WEB-CORE/elements/richtext/images/objects-").append(this.browser.isIE() ? "v" : "h").append(".gif\" width=\"17\" height=\"16\"").append(this.browser.isWebkit() ? " style=\"padding-top:1px;margin-bottom:-2px;\"" : "").append(">");
                html.append("&nbsp;").append(this.getTranslationProcessor().translate("Left Bar"));
                html.append("</div>");
                html.append("<div style=\"height:100%;\"></div>");
                html.append("</div>");
            } else {
                html.append("<div id=\"painter_leftbar_collapsed\" class=\"painter_bar_collapsed\" style=\"display:").append(this.viewonly || this.painteronly ? "none" : "block").append(";\">");
                html.append("<div title=\"").append(this.getTranslationProcessor().translate("Expand Toolbox & Variables")).append("\" id=\"painter_leftbar_expand\" class=\"painter_bar_expand ").append(this.browser.isIE() ? "painter_leftbar_expand_ie" : "painter_leftbar_expand_nonie").append("\" onclick=\"").append(JS_CLASS).append(".ui.expandBars();\">");
                html.append("<img src=\"WEB-CORE/elements/richtext/images/objects-").append(this.browser.isIE() ? "v" : "h").append(".gif\" width=\"17\" height=\"16\"").append(this.browser.isWebkit() ? " style=\"padding-top:1px;margin-bottom:-2px;\"" : "").append(">");
                html.append("&nbsp;").append(this.getTranslationProcessor().translate("Left Bar"));
                html.append("</div>");
                html.append("<div style=\"height:100%;\"></div>");
                html.append("</div>");
            }
            if (showLeft) {
                html.append("<div id=\"painter_leftbar\" style=\"width:").append(lbiwidth).append("px;display:block;\">");
            } else {
                html.append("<div id=\"painter_leftbar\" style=\"z-index:1;width:").append(lbiwidth).append("px;display:none;\">");
            }
            html.append("<table style=\"table-layout:fixed;width:100%;height:30px;\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"painter_bar\">");
            html.append("<tbody>");
            html.append("<tr class=\"layout_sidebar_tab_row\" ").append(this.embedded ? "style=\"\"" : "").append(">");
            if (this.taskprops.getCollection("steps") == null || this.taskprops.getCollection("steps").size() == 0) {
                toolboxselected = true;
                this.userConfig.setProperty("taskdef_tooltab", PROPERTY_TOOLBOX);
            } else {
                toolboxselected = this.userConfig.getProperty("taskdef_tooltab", PROPERTY_TOOLBOX).equalsIgnoreCase(PROPERTY_TOOLBOX);
            }
            if (this.descendant) {
                html.append("<td class=\"tab_back\" style=\"width:100%;\">");
                html.append("<table cellpadding=0 cellspacing=0 class=\"tab\" style=\"width:100%;\"><tbody><tr>");
                html.append("<td class=\"props_h_cell\" align=\"").append(this.rtlFlag ? "right" : "left").append("\">");
                html.append("<img src=\"WEB-CORE/elements/richtext/images/props-h.gif\" width=\"16\" height=\"17\">");
                html.append("</td>");
                html.append("<td align=\"").append(this.rtlFlag ? "right" : "left").append("\" valign=\"middle\" nowrap class=\"props_t_cell\">");
                html.append(this.getTranslationProcessor().translate("Variables"));
                html.append("</td>");
                html.append("</tr></tbody></table>");
                html.append("</td>");
            } else {
                html.append("<td class=\"tab_back").append(toolboxselected ? "_sel" : "").append("\" style=\"\">");
                html.append("<table cellpadding=0 cellspacing=0 class=\"tab").append(toolboxselected ? "_sel" : "").append("\" style=\"width:100%;\" onclick=\"").append(JS_CLASS).append(".ui.toolboxTab('toolbox');\" onmouseover=\"\" onmouseout=\"\"><tbody><tr>");
                html.append("<td align=\"").append(this.rtlFlag ? "right" : "left").append("\" class=\"objects_h_cell\">");
                html.append("<img src=\"WEB-CORE/elements/richtext/images/objects-h.gif\" width=\"16\" height=\"17\">");
                html.append("</td>");
                html.append("<td align=\"").append(this.rtlFlag ? "right" : "left").append("\" valign=\"middle\" nowrap class=\"objects_t_cell\">");
                html.append("<div class=\"tab_div").append(toolboxselected ? "_sel" : "").append("\">");
                html.append(this.getTranslationProcessor().translate("Steps"));
                html.append("</div>");
                html.append("</td>");
                html.append("</tr></tbody></table>");
                html.append("</td>");
                html.append("<td class=\"tab_back").append(toolboxselected ? "" : "_sel").append("\" style=\"\">");
                html.append("<table cellpadding=0 cellspacing=0 class=\"tab").append(toolboxselected ? "" : "_sel").append("\" style=\"width:100%;\" onclick=\"").append(JS_CLASS).append(".ui.toolboxTab('variables');\" onmouseover=\"\" onmouseout=\"\"><tbody><tr>");
                html.append("<td align=\"").append(this.rtlFlag ? "right" : "left").append("\" class=\"vars_h_cell\">");
                html.append("<img src=\"WEB-CORE/elements/richtext/images/props-h.gif\" width=\"16\" height=\"17\">");
                html.append("</td>");
                html.append("<td align=\"").append(this.rtlFlag ? "right" : "left").append("\" valign=\"middle\" nowrap class=\"vars_t_cell\">");
                html.append("<div class=\"tab_div").append(toolboxselected ? "" : "_sel").append("\">");
                html.append(this.getTranslationProcessor().translate("Variables"));
                html.append("</div>");
                html.append("</td>");
                html.append("</tr></tbody></table>");
                html.append("</td>");
            }
            html.append("<td class=\"tab_back\" style=\"width:20px;\">");
            html.append("<table cellpadding=0 cellspacing=0 class=\"tab\" style=\"height:100%;\" onmouseover=\"this.style.borderColor='#F49900';\" onmouseout=\"this.style.borderColor='';\"><tbody><tr>");
            html.append("<td align=\"").append(this.rtlFlag ? "left" : "right").append("\" valign=middle>");
            html.append("<img src=\"WEB-OPAL/layouts/generic/images/sidebar_").append(this.rtlFlag ? "expand" : "collapse").append(".gif\" title=\"Collapse\" id=\"painter_leftbar_collapse\" style=\"display:block;cursor: pointer;\" onclick=\"").append(JS_CLASS).append(".ui.collapseBars();\">");
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
            if (this.descendant) {
                html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_vars_outer\" style=\"display:").append("block").append(";").append(this.browser.isIE() ? "" : "").append("\">");
                html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_vars_content\"  style=\"width:").append(lbiwidth - 12).append("px;").append(this.browser.isIE() ? "" : "").append("\">");
                html.append(this.getVariablesHTML(this.taskprops));
                html.append("</div>");
                html.append("</div>");
            } else {
                html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_objects_outer\" style=\"display:").append(toolboxselected ? "block" : "none").append(";").append(this.browser.isIE() ? "" : "").append("\">");
                html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_objects_content\"  style=\"width:").append(lbiwidth - 12).append("px;").append(this.browser.isIE() ? "" : "").append("\">");
                html.append(TaskDefPainter.getToolsHtml(this.taskprops, this.toolAppearance, this.getConnectionProcessor().getSapphireConnection(), this.getSDIProcessor(), this.getTranslationProcessor(), (HttpServletRequest)this.pageContext.getRequest(), this.getWebAdminProcessor(), this.logger));
                html.append("</div>");
                html.append("</div>");
                html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_vars_outer\" style=\"display:").append(toolboxselected ? "none" : "block").append(";").append(this.browser.isIE() ? "" : "").append("\">");
                html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_vars_content\"  style=\"width:").append(lbiwidth - 12).append("px;").append(this.browser.isIE() ? "" : "").append("\">");
                html.append(this.getVariablesHTML(this.taskprops));
                html.append("</div>");
                html.append("</div>");
            }
            html.append("</div>");
            html.append("<div id=\"painter_top_container\" style=\"height:").append(this.painteronly ? "100%" : tbiheight + "px").append(";\">");
            html.append("<div id=\"painter_content_container\">");
            html.append(this.getPainterHtml(this.taskprops, this.viewonly, this.descendant, this.embedded, this.devMode, this.zoom));
            if (this.browser.isWebkit()) {
                html.append("<div align=center class=\"painter_zoom_container\" style=\"bottom:").append(this.painteronly ? "0" : "+20px").append(this.rtlFlag ? ";left:" : ";right:").append(this.painteronly ? "0" : "+40px").append(";height:").append("32").append("px;\"><div id=\"painter_zoom\"></div><label id=\"painter_zoom_label\">").append(this.zoom).append("%</label></div>");
            }
            html.append("</div>");
            html.append("</div>");
            html.append("<div id=\"painter_bottom_container\" style=\"display:").append(this.painteronly ? "none" : "block").append(";top:").append(tbiheight + 1).append("px;\">");
            html.append("<div id=\"painter_bottombar\" style=\"width:auto;overflow:hidden;\">");
            html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_properties_outer\" style=\"").append(this.rtlFlag ? "right:" : "left:").append(showVar ? vbiwidth + "px" : (this.viewonly ? "0" : "20px")).append(";\">");
            html.append("<div align=\"").append(this.rtlFlag ? "right" : "left").append("\" id=\"painter_properties_content\" >");
            html.append(TaskDefPainter.getPropertiesHtml(TaskDefProperties.ItemType.STEP, "", "", null, this.viewonly || this.descendant, "", this.getConnectionId(), this.userConfig, this.getTranslationProcessor(), this.browser, this.pageContext.getSession()));
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append(this.getEndScript(this.taskprops, this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.propertyContainer, this.viewonly, this.descendant, this.painteronly, this.embedded, this.zoom));
        } else {
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load properties for task.")).append("</font>");
        }
        return html.toString();
    }

    public static String getPropertiesHtml(TaskDefProperties.ItemType type, String id1, String id2, PropertyList props, boolean viewOnly, String selectedBtn, String connectionId, PropertyList userConfig, TranslationProcessor tp, Browser browser, HttpSession session) {
        StringBuffer html = new StringBuffer();
        String style = "background-color:transparent";
        boolean rtlFlag = new ConnectionProcessor(connectionId).getSapphireConnection().isRtl();
        html.append("<div align=\"").append(rtlFlag ? "right" : "left").append("\" id=\"painter_properties_cover\"  style=\"").append(style).append(";\">");
        html.append("</div>");
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            html.append("<iframe name=\"taskdef_props_frame\" id=\"taskdef_props_frame\" frameborder=0 scrolling=false src=\"").append(browser.getBlankSrc()).append("\" style=\"width:100%;height:100%;\"></iframe>");
            html.append("<form style=\"display:none\" method=\"post\" id=\"taskdef_props_form\" name=\"taskdef_props_form\" action=\"rc?command=file&file=WEB-CORE/elements/workflow/taskdefproperties.jsp\" target=\"taskdef_props_frame\">");
            html.append("<input type=\"hidden\" name=\"").append("id1").append("\" value=\"").append(id1).append("\">");
            html.append("<input type=\"hidden\" name=\"").append("id2").append("\" value=\"").append(id2).append("\">");
            html.append("<input type=\"hidden\" name=\"").append("selectedbutton").append("\" value=\"").append(selectedBtn).append("\">");
            html.append("<input type=\"hidden\" name=\"").append("selectedtab").append("\" value=\"").append("-1").append("\">");
            html.append("<input type=\"hidden\" name=\"").append("type").append("\" value=\"").append(type.toString().toLowerCase()).append("\">");
            html.append("<input type=\"hidden\" name=\"").append(PROPERTY_VIEWONLY).append("\" value=\"").append(viewOnly ? "Y" : "N").append("\">");
            html.append("<input type=\"hidden\" name=\"").append(PROPERTY_DESCENDANT).append("\" value=\"").append(viewOnly ? "Y" : "N").append("\">");
            html.append("<textarea style=\"display:none;\" name=\"properties\">").append(props != null ? props.toJSONString(false) : "{}").append("</textarea>");
            html.append("</form>");
            html.append("<script>taskdef_props_form.submit();</script>");
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
        IMAGE("Image", "Drawing", "rc?command=image&image=PhotoLandscape2", "{\"image\":\"rc?command=image&image=PhotoLandscape2&size=48\",\"showline\":\"Y\",\"linecolor\":\"#8C3836\",\"linewidth\":\"1\",\"linestyle\":\"solid\",\"showbackground\":\"N\",\"backgroundcolor\":\"white\",\"backgroundopacity\":\"1\"}");

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
}

