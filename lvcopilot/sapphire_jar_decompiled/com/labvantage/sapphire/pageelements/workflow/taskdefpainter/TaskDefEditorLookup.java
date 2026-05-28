/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefProperties;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefEditorLookup
extends BaseElement {
    public static final String JS_CLASS = "taskEditor";
    public static final String USERCONFIG_PREFIX = "taskeditor_";
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
    public static final String PROPERTY_APPEARANCE = "appearance";
    public static final String PROPERTY_COLOR = "color";
    public static final String PROPERTY_PROPERTYCONTAINER = "propertycontainer";
    public static final String PROPERTY_EMBEDDED = "embedded";
    public static final String PROPERTY_SHOWQUEUES = "showqueues";
    public static final String SDCID = "LV_WorkflowDef";
    public static final String DATACOL = "workflowdef";
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private PropertyList savedProps = null;
    private PropertyList userConfig;
    private boolean devMode;
    private PropertyList toolprops = null;
    private String fieldid = "";

    public TaskDefEditorLookup(PageContext pageContext, PropertyList pageproperties) {
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
        }
        catch (Exception e) {
            this.logger.error("Could not set up painter: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        pagedata.setProperty("jsrequest", "exclude=properties");
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
        String xml = pagedata.getProperty("taskdef", "");
        this.logger.debug("xml = " + xml);
        if (xml.length() > 0) {
            if (xml.startsWith("{|") && xml.endsWith("|}")) {
                xml = xml.substring(2, xml.length() - 2);
            }
            this.savedProps = new PropertyList();
            try {
                this.savedProps.setPropertyList(xml);
            }
            catch (Exception e) {
                this.logger.error("Failed to load XML", e);
            }
        }
        this.fieldid = pagedata.getProperty("fieldid", "");
        this.logger.debug("fieldid = " + this.fieldid);
        this.keyid1 = pagedata.getProperty(PROPERTY_KEYID1, "");
        this.logger.debug("keyid1 = " + this.keyid1);
        this.keyid2 = pagedata.getProperty(PROPERTY_KEYID2, "");
        this.logger.debug("keyid2 = " + this.keyid2);
        this.keyid3 = pagedata.getProperty(PROPERTY_KEYID3, "");
        this.logger.debug("keyid3 = " + this.keyid3);
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList("LV_TaskDef");
        this.toolprops = TaskDefMaint.getTasksData(true, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig == null) {
            throw new SapphireException("User configuration could not be obtained.");
        }
    }

    public static String getToolsHtml(PropertyList toolprops, String filterText, String connectionId, TranslationProcessor tp, M18NUtil m18n, PropertyList userConfig, HttpServletRequest request, Logger logger) {
        String[] filterBits;
        WorkflowDefPainter.ToolGrouping grouping;
        RequestContext rc;
        StringBuffer sb = new StringBuffer();
        PropertyListCollection tools = toolprops.getCollection("tasks");
        if (userConfig == null && request != null && (rc = RequestContext.getInstance(request)) != null) {
            if (connectionId == null || connectionId.length() == 0) {
                connectionId = rc.getConnectionId();
            }
            userConfig = rc.getPropertyList("userconfig");
        }
        boolean od = false;
        try {
            od = userConfig.getProperty("taskeditor_toolsorderby", "").equalsIgnoreCase("moddt");
        }
        catch (Exception e) {
            od = false;
        }
        final boolean orderByDate = od;
        try {
            grouping = WorkflowDefPainter.ToolGrouping.valueOf(userConfig.getProperty("taskeditor_toolsgroupby", WorkflowDefPainter.ToolGrouping.NONE.toString()).toUpperCase());
        }
        catch (Exception e) {
            grouping = WorkflowDefPainter.ToolGrouping.NONE;
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
                PropertyList tool = tools.getPropertyList(i);
                String taskid = tool.getProperty("taskdefid", "") + ";" + tool.getProperty("taskdefversionid") + ";" + tool.getProperty("taskdefvariantid", "");
                String text = tool.getProperty("shorttitle", tool.getProperty("longtitle", ""));
                if (text.length() <= 0) continue;
                if (filterBits != null) {
                    boolean found = true;
                    for (String s : filterBits) {
                        if (text.toLowerCase().contains(s)) continue;
                        found = false;
                    }
                    if (!found) continue;
                }
                if (!tool.getProperty("taskdefvariantid", "1").equals("1")) {
                    text = text + " (" + tool.getProperty("taskdefvariantid", "") + ")";
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
                    current.append("<div class=\"painter_tool").append("\" id=\"painter_tool").append(count).append("\" style=\"top:7px;left:20px;\" tooltype=\"task\" toolid=\"").append(taskid).append("\"><div class=\"painter_toolimg").append("\">").append(img.getHtml()).append("</div><div class=\"painter_toollabel").append("\">").append(text).append("</div></div>");
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
                current.append("<h3><a href=\"#\">").append(grouptext).append(" <span style=\"color:grey\">(").append(tmgroup.size()).append(")</span></a></h3>");
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
                sb.append("<p style=\"margin-left:5px;font-size:9pt;\">No Tasks Found</p>");
            } else {
                sb.append("<p style=\"margin-left:5px;font-size:9pt;\">No Tasks Available</p>");
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
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/workflow/stylesheets/painter.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/workflow/stylesheets/workflow.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/taskeditor").append("").append(".js\"></script>");
        return html;
    }

    private StringBuffer getEndScript(String keyid1, String keyid2, String keyid3) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        if (this.savedProps != null) {
            String tid = this.savedProps.getProperty("taskdefid") + ";" + this.savedProps.getProperty("taskdefversionid") + ";" + this.savedProps.getProperty("taskdefvariantid");
            html.append(JS_CLASS).append(".selected = $(\"div[toolid='").append(tid).append("']\")[0];");
            html.append("if (typeof(").append(JS_CLASS).append(".selected) != 'undefined'){");
            html.append(JS_CLASS).append(".selected.style.color = 'red';");
            html.append("}");
            html.append(JS_CLASS).append(".variables = sapphire.util.propertyListCollection.create('").append(this.savedProps.getCollection("variables").toJSONString(false, false)).append("');");
        }
        html.append(JS_CLASS).append(".fieldid='").append(this.fieldid).append("';");
        html.append(JS_CLASS).append(".keyid1='").append(keyid1).append("';");
        html.append(JS_CLASS).append(".keyid2='").append(keyid2).append("';");
        html.append(JS_CLASS).append(".keyid3='").append(keyid3).append("';");
        String toolgroups = this.userConfig.getProperty("taskeditor_toolgroups", "{}");
        if (toolgroups.length() == 0 || !toolgroups.startsWith("{") || !toolgroups.endsWith("}")) {
            toolgroups = "{}";
        }
        html.append(JS_CLASS).append(".toolGroups=").append(toolgroups).append(";");
        html.append(JS_CLASS).append(".toolGroup='").append(this.userConfig.getProperty("taskeditor_toolsgroupby", WorkflowDefPainter.ToolGrouping.NONE.toString()).toLowerCase()).append("';");
        html.append(JS_CLASS).append(".toolSort='").append(this.userConfig.getProperty("taskeditor_toolsorderby", "").toLowerCase()).append("';");
        html.append("</script>");
        return html;
    }

    public static String getInputsHTML(PropertyList task, String selected, String connectionId, TranslationProcessor translationProcessor, PropertyList userConfig, Logger logger) {
        StringBuffer html = new StringBuffer();
        if (task == null) {
            html.append(translationProcessor.translate("<div style=\"color:grey;\">Please select a task to continue.</div>"));
        } else {
            PropertyListCollection taskios;
            StringBuffer sb = new StringBuffer();
            PropertyListCollection propertyListCollection = taskios = task == null ? null : task.getCollection("taskio");
            if (taskios != null) {
                boolean found = false;
                for (int i = 0; i < taskios.size(); ++i) {
                    PropertyList taskio = taskios.getPropertyList(i);
                    String ioflag = taskio.getProperty("ioflag", "O");
                    String ioid = taskio.getProperty("ioid", "");
                    if (!ioflag.equalsIgnoreCase("I") || ioid.length() <= 0) continue;
                    String connectortypeid = taskio.getProperty("connectortypeid", "");
                    String iodesc = taskio.getProperty("iodesc", "");
                    sb.append("<tr>");
                    sb.append("<td class=\"gridmaint_field\">");
                    sb.append("<input type=\"radio\" name=\"selector\" value=\"").append(ioid).append("\"").append(selected.length() > 0 && selected.equalsIgnoreCase(ioid) ? " checked" : "").append(">");
                    sb.append("</td>");
                    sb.append("<td id=\"").append("input").append(i).append("_").append(ioid).append("_titlecell\" class=\"gridmaint_field\" style=\"\">");
                    sb.append(ioid);
                    sb.append("</td>");
                    sb.append("<td class=\"gridmaint_field\">");
                    sb.append(iodesc);
                    sb.append("</td>");
                    sb.append("<td class=\"gridmaint_field\">");
                    sb.append(connectortypeid);
                    sb.append("</td>");
                    sb.append("</tr>");
                    found = true;
                }
                if (found) {
                    html.append("<form name=\"workflow_props_inputs\" action=\"#\">");
                    html.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"gridmaint_table\">");
                    html.append("<thead>");
                    html.append("<tr class=\"gridmaint_tablehead\">");
                    html.append("<th class=\"\">").append("&nbsp;").append("</th>");
                    html.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Input Id")).append("</th>");
                    html.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Description")).append("</th>");
                    html.append("<th class=\"gridmaint_fieldtitle\" style=\"width:180px;\">").append(translationProcessor.translate("Connector")).append("</th>");
                    html.append("</tr>");
                    html.append("</thead>");
                    html.append("<tbody>");
                    html.append(sb);
                    html.append("</tbody>");
                    html.append("</table>");
                    html.append("</form>");
                } else {
                    html.append("No Inputs");
                }
            } else {
                html.append("No Inputs");
            }
        }
        return html.toString();
    }

    public static String getVariablesHTML(PropertyList task, PropertyListCollection variables, String connectionId, TranslationProcessor translationProcessor, PropertyList userConfig, PageContext pageContext, Logger logger) {
        StringBuffer html = new StringBuffer();
        if (task == null) {
            html.append(translationProcessor.translate("<div style=\"color:grey;\">Please select a task to continue.</div>"));
        } else {
            PropertyList newP = new PropertyList();
            newP.setProperty("variables", variables);
            html.append(WorkflowDefProperties.getTaskSetupVariables(task, newP, null, false, true, JS_CLASS, connectionId, null, translationProcessor, pageContext, logger));
        }
        return html.toString();
    }

    @Override
    public String getHtml() {
        int tbiheight;
        int lbiwidth;
        StringBuffer html = new StringBuffer();
        html.append(this.getScriptAndStyle());
        html.append("<div id=\"painter_root_container\" style=\"").append("min-width:800px;").append("position:absolute;top:0;left:0;height:auto;bottom:0;width:100%;overflow-y:").append("hidden").append(";\">");
        PropertyList task = null;
        if (this.savedProps != null) {
            task = WorkflowDefMaint.getTaskData(this.savedProps.getProperty("taskdefid"), this.savedProps.getProperty("taskdefversionid"), this.savedProps.getProperty("taskdefvariantid"), this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        }
        String lbwidth = this.userConfig.getProperty("taskeditor_left_width", "310");
        try {
            lbiwidth = Integer.parseInt(lbwidth);
        }
        catch (Exception e) {
            lbiwidth = 310;
        }
        String tbheight = this.userConfig.getProperty("taskeditor_top_height", "300");
        try {
            tbiheight = Integer.parseInt(tbheight);
        }
        catch (Exception e) {
            tbiheight = 300;
        }
        html.append("<div id=\"painter_top_container\" style=\"height:100%;\">");
        StringBuffer sb = new StringBuffer();
        html.append("<div id=\"painter_leftbar_collapsed\" class=\"painter_bar_collapsed\" style=\"background-position:0 -3px;width:20px;display:none;float:left;height:100%;\">");
        html.append("<div id=\"painter_leftbar_expand\" class=\"painter_bar_expand ").append(this.browser.isIE() ? "painter_leftbar_expand_ie" : "painter_leftbar_expand_nonie").append("\" onclick=\"").append(JS_CLASS).append(".ui.expandBar('left');\"><img src=\"WEB-CORE/elements/richtext/images/objects-").append(this.browser.isIE() ? "v" : "h").append(".gif\" width=\"17\" height=\"16\"").append(this.browser.isWebkit() ? " style=\"padding-top:1px;margin-bottom:-2px;\"" : "").append(">&nbsp;").append(this.getTranslationProcessor().translate("Tasks")).append("</div>");
        html.append("<div style=\"height:100%;\"></div>");
        html.append("</div>");
        html.append("<div id=\"painter_leftbar\" style=\"width:").append(lbiwidth).append("px;display:block;float:left;height:100%;\">");
        html.append("<div align=\"left\" id=\"painter_objects_outer\" style=\"display:").append("block").append(";background-color:#C3DAF9;overflow-y:hidden;top:0;overflow-x:hidden;width:100%;height:100%;").append(this.browser.isIE() ? "" : "").append("\">");
        html.append("<input onkeyup=\"").append(JS_CLASS).append(".onToolFilter(this,event)").append("\" onchange=\"").append(JS_CLASS).append(".onToolFilter(this,event)").append("\" onfocus=\"if(this.value==this.defaultValue){this.style.color='black';value='';}\" onblur=\"if(this.value.length==0){this.style.color='grey';this.value=this.defaultValue;}\" type=\"text\" id=\"painter_toolsfilter\" value=\"Enter Filter\" style=\"top:4px;").append("width:").append(lbiwidth - 20).append("px;").append("color:grey;\">");
        html.append("<div align=\"left\" id=\"painter_objects_content\"  style=\"top:24px;width:").append(lbiwidth - 12).append("px;height:auto;").append(this.browser.isIE() ? "" : "").append("\">");
        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        html.append(WorkflowDefPainter.getToolsHtml(null, this.toolprops, null, true, this.getConnectionId(), this.getTranslationProcessor(), m18n, this.userConfig, USERCONFIG_PREFIX, (HttpServletRequest)this.pageContext.getRequest(), this.logger));
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        html.append("<div id=\"painter_content_container\" style=\"").append("min-width:300px;").append("width:auto;height:100%;").append("border-left:solid 1px #95A9C3;border-bottom:solid 1px #95A9C3;").append("overflow:hidden;position:relative;\">");
        html.append("<div id=\"painter_content_container_top\" style=\"height:").append(tbiheight + "px").append(";position:relative;\">");
        html.append("<div align=\"left\" id=\"painter_task_outer\" style=\"border-left:solid 1px #95A9C3;background-color:#C3DAF9;overflow-y:hidden;overflow-x:hidden;width:auto;height:auto;position:absolute;bottom:0;top:0;left:0;right:0;\">");
        html.append("<div align=\"left\" id=\"painter_task_content\"  style=\"margin-left:1px;margin-right:4px;margin-bottom:2px;border-radius:4px;border:solid 2px #7F9DB9;border-left:none;background-color:#FFFFFF;overflow-y:hidden;overflow-x:hidden;width:auto;height:auto;position:absolute;bottom:0;top:2px;left:0;right:0;padding:10px;\">");
        html.append(TaskDefEditorLookup.getInputsHTML(task, this.savedProps != null ? this.savedProps.getProperty("ioid") : "", this.getConnectionId(), this.getTranslationProcessor(), this.userConfig, this.logger));
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        html.append("<div id=\"painter_content_container_bottom\" style=\"display:").append("block").append(";bottom:0;height:auto;position:absolute;left:0;right:0;top:").append(tbiheight).append("px;\">");
        html.append("<div align=\"left\" id=\"painter_properties_outer\" style=\"border-left:solid 1px #95A9C3;background-color:#C3DAF9;overflow-y:hidden;overflow-x:hidden;width:auto;height:auto;position:absolute;bottom:0;top:0;left:0;right:0;\">");
        html.append("<div align=\"left\" id=\"painter_properties_content\"  style=\"margin-left:1px;margin-right:4px;margin-bottom:2px;border-radius:4px;border:solid 2px #7F9DB9;border-left:none;background-color:#FFFFFF;overflow-y:hidden;overflow-x:hidden;width:auto;;height:auto;position:absolute;bottom:35px;top:2px;left:0;right:0;padding:10px;\">");
        html.append(TaskDefEditorLookup.getVariablesHTML(task, this.savedProps != null ? this.savedProps.getCollection("variables") : null, this.getConnectionId(), this.getTranslationProcessor(), this.userConfig, this.pageContext, this.logger));
        html.append("</div>");
        html.append("<div align=\"left\" id=\"painter_buttons_content\"  style=\"overflow-y:hidden;overflow-x:hidden;width:auto;;height:35px;position:absolute;bottom:-2px;;right:10px;\">");
        Button but = new Button(this.pageContext);
        but.setText(this.getTranslationProcessor().translate("OK"));
        but.setTip(this.getTranslationProcessor().translate("OK and return"));
        but.setId("taskeditor_btn_ok");
        but.setImg("WEB-CORE/images/gif/Confirm.gif");
        but.setAction("taskEditor.buttons.doOK()");
        but.setWidth("100");
        html.append(but.getHtml());
        html.append("&nbsp;&nbsp;");
        but = new Button(this.pageContext);
        but.setText(this.getTranslationProcessor().translate("Close"));
        but.setTip(this.getTranslationProcessor().translate("Close"));
        but.setId("taskeditor_btn_cancel");
        but.setImg("WEB-CORE/images/gif/Cancel.gif");
        but.setAction("taskEditor.buttons.doCancel()");
        but.setWidth("100");
        html.append(but.getHtml());
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        html.append(this.getEndScript(this.keyid1, this.keyid2, this.keyid3));
        return html.toString();
    }
}

