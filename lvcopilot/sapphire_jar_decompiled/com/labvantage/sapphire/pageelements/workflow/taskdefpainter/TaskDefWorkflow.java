/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflowProperties;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.http.HttpUtil;
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
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefWorkflow
extends BaseElement {
    public static final String USERCONFIG_PREFIX = "taskdef_";
    public static final String JS_CLASS = "taskWorkflow";
    public static final String SESSION_PREFIX = "taskdef_";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_DESCENDANT = "descendant";
    public static final String DEFAULT_TASKCOLOR1 = "#e7f2ff";
    public static final String DEFAULT_TASKCOLOR2 = "#d4e3f2";
    public static final String DEFAULT_TASKCOLOR3 = "#c9d8ed";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private boolean descendant = false;
    private boolean devMode;
    PropertyList taskprops = null;

    public TaskDefWorkflow(PageContext pageContext, PropertyList pageproperties) {
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
            this.taskprops = null;
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
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.descendant = pagedata.getProperty(PROPERTY_DESCENDANT, "n").equalsIgnoreCase("y");
        this.logger.debug("descendant = " + this.descendant);
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
        this.userConfig = RequestContext.getInstance(request).getPropertyList("userconfig");
        if (this.userConfig == null) {
            throw new SapphireException("User configuration could not be obtained.");
        }
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
        html.append("<link rel=\"stylesheet\" href=\"" + HttpUtil.getCSS("WEB-CORE/elements/workflow/stylesheets/taskworkflow.css", this.pageContext) + "\" type=\"text/css\">");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/taskworkflow.js\"></script>");
        return html;
    }

    private StringBuffer getEndScript(boolean viewOnly, boolean descendant) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append(JS_CLASS).append(".viewonly=").append(viewOnly).append(";");
        html.append(JS_CLASS).append(".descendant=").append(descendant).append(";");
        html.append("</script>");
        return html;
    }

    public static String getConnectorsDisplayValue(StringBuffer vals, String connectorTypeId, SDIProcessor sdiProcessor) {
        PropertyListCollection connectors = null;
        String selectedSDCId = "";
        try {
            connectors = TaskDefWorkflow.getConnectors(sdiProcessor);
            if (connectors != null) {
                for (int i = 0; i < connectors.size(); ++i) {
                    PropertyList connector = connectors.getPropertyList(i);
                    if (vals.length() > 0) {
                        vals.append(";");
                    }
                    String con = connector.getProperty("connectortypeid", connector.getProperty("connectortypeid", connector.getId()));
                    vals.append(con).append("=").append(connector.getProperty("title", connector.getProperty("sdcid", con)));
                    if (connectorTypeId.length() <= 0 || !connector.getProperty("connectortypeid").equalsIgnoreCase(connectorTypeId)) continue;
                    selectedSDCId = connector.getProperty("sdcid", "");
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return selectedSDCId;
    }

    public static PropertyListCollection getConnectors(SDIProcessor sdi) {
        PropertyListCollection connectors;
        try {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("LV_ConnectorType");
            sdiRequest.setRequestItem("primary");
            sdiRequest.setQueryFrom("connectortype");
            SDIData sdidata = sdi.getSDIData(sdiRequest);
            if (sdidata != null && sdidata.getDataset("primary") != null) {
                connectors = new PropertyListCollection();
                DataSet pri = sdidata.getDataset("primary");
                for (int i = 0; i < pri.getRowCount(); ++i) {
                    PropertyList connector = new PropertyList();
                    connector.setProperty("connectortypeid", pri.getValue(i, "connectortypeid", ""));
                    connector.setProperty("title", pri.getValue(i, "title", ""));
                    connector.setProperty("sdcid", pri.getValue(i, "connectortypesdcid", ""));
                    connector.setProperty("validation", pri.getValue(i, "validation", ""));
                    connector.setProperty("color", pri.getValue(i, "color", ""));
                    connectors.add(connector);
                }
            } else {
                connectors = null;
            }
        }
        catch (Exception e) {
            connectors = null;
        }
        return connectors;
    }

    protected static String renderWindow(PropertyList taskprops, StringBuffer html, StringBuffer script, StringBuffer style, boolean visible, boolean viewonly, PropertyListCollection connectors, String connectionId, Browser browser, Logger logger) {
        Appearance appearance;
        String windowid = "workflowwindow";
        PropertyListCollection ios = taskprops.getCollection("taskio");
        if (ios == null) {
            ios = new PropertyListCollection();
            taskprops.setProperty("taskio", ios);
        }
        double x = 0.0;
        double y = 0.0;
        try {
            appearance = Appearance.valueOf(taskprops.getProperty("appearance", Appearance.OFFSET.toString()).toUpperCase());
        }
        catch (Exception e) {
            appearance = Appearance.OFFSET;
        }
        String windowclass = "";
        String windowimgclass = "";
        String windowlabelclass = "";
        String windowshadowclass = "";
        if (appearance != Appearance.OFFSET) {
            windowclass = "painter_window_" + appearance.toString().toLowerCase();
            windowimgclass = "painter_windowimg_" + appearance.toString().toLowerCase();
            windowlabelclass = "painter_windowlabel_" + appearance.toString().toLowerCase();
            windowshadowclass = "painter_windowshad_" + appearance.toString().toLowerCase();
        }
        String title = taskprops.getProperty("shorttitle", appearance == Appearance.INLINE || appearance == Appearance.OFFSET ? "No Title" : "");
        String image = appearance == Appearance.DECISION ? "WEB-CORE/elements/workflow/images/decision.png" : taskprops.getProperty("icon", "WEB-CORE/elements/workflow/images/NoImage.png");
        String color1 = taskprops.getProperty("taskcolor1", DEFAULT_TASKCOLOR1);
        String color2 = taskprops.getProperty("taskcolor2", DEFAULT_TASKCOLOR2);
        String color3 = taskprops.getProperty("taskcolor3", DEFAULT_TASKCOLOR3);
        String classname = windowid + "_" + (int)Math.floor(Math.random() * 1000.0);
        style.append("div.").append(classname).append("{");
        if (browser.isChrome() && browser.getVersion() > 10.0 || browser.isSafari() && browser.getVersion() > 5.1) {
            style.append("background: -webkit-linear-gradient(top,  ").append(color1).append(" 0%,").append(color2).append(" 38%,").append(color3).append(" 100%);");
            style.append("opacity:0.8;");
        } else if (browser.isChrome() || browser.isSafari() && browser.getVersion() > 4.0) {
            style.append("background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,").append(color1).append("), color-stop(38%,").append(color2).append("), color-stop(100%,").append(color3).append("));");
            style.append("opacity:0.8;");
        } else if (browser.isFireFox() && browser.getVersion() > 3.6) {
            style.append("background: -moz-linear-gradient(top,  ").append(color1).append(" 0%,").append(color2).append(" 38%,").append(color3).append(" 100%);");
            style.append("opacity:0.8;");
        } else if (browser.isIE() && browser.getVersion() > 9.0) {
            style.append("background: -ms-linear-gradient(top,  ").append(color1).append(" 0%,").append(color2).append(" 38%,").append(color3).append(" 100%);");
            style.append("opacity:0.8;");
        } else {
            style.append("background: ").append(color1).append(";");
            style.append("background: linear-gradient(to bottom,  ").append(color1).append(" 0%,").append(color2).append(" 38%,").append(color3).append(" 100%);");
        }
        style.append("}");
        if (browser.isIE() && browser.getVersion() == 8.0) {
            html.append("<div class=\"painter_windowshad").append(windowshadowclass.length() > 0 ? " " + windowshadowclass : "").append("\" style=\"display:none;").append("").append("top:").append(y - 5.0).append("px;left:").append(x - 5.0).append("px;\"></div>");
        }
        html.append("<div class=\"painter_window").append(windowclass.length() > 0 ? " " + windowclass : "").append(" ").append(classname).append("\" id=\"").append(windowid).append("\" style=\"").append("").append("top:").append(y).append("px;left:").append(x).append("px;").append(visible ? "" : "display:none;").append("\" windowid=\"").append(windowid).append("\" windowtype=\"").append("config").append("\"");
        if (!viewonly) {
            html.append("");
        }
        html.append(">");
        Image img = new Image();
        img.setConnectionId(connectionId);
        img.setImageSrc(image);
        if (appearance != Appearance.DECISION) {
            img.setDimensions(48, 48);
        } else {
            img.setDimensions(76, 76);
        }
        html.append("<div class=\"painter_windowimg").append(windowimgclass.length() > 0 ? " " + windowimgclass : "").append("\">").append(img.getHtml()).append("</div>").append("<div class=\"painter_windowlabel").append(windowlabelclass.length() > 0 ? " " + windowlabelclass : "").append("\"").append("").append(">").append(title).append("</div>");
        html.append("</div>");
        script.append(TaskDefWorkflow.getIOScript(windowid, ios, connectors, true, new ConnectionProcessor(connectionId).getSapphireConnection(), logger));
        return windowid;
    }

    public static String getIOScript(String windowid, PropertyListCollection ios, PropertyListCollection connectors, boolean labels, SapphireConnection sapphireConnection, Logger logger) {
        StringBuffer script = new StringBuffer();
        int outputtotal = 0;
        int inputtotal = 0;
        if (ios != null) {
            for (int i = 0; i < ios.size(); ++i) {
                PropertyList io = ios.getPropertyList(i);
                String ioid = io.getProperty("ioid", "");
                if (ioid.length() <= 0) continue;
                if (io.getProperty("ioflag", "o").equalsIgnoreCase("o")) {
                    ++outputtotal;
                    continue;
                }
                ++inputtotal;
            }
            int outputcount = 0;
            int inputcount = 0;
            for (int i = 0; i < ios.size(); ++i) {
                PropertyList io = ios.getPropertyList(i);
                String ioid = io.getProperty("ioid");
                if (ioid.length() > 0) {
                    double anc;
                    PropertyList connector = null;
                    String connectorid = io.getProperty("connectortypeid", "");
                    if (connectors != null && connectors.size() > 0 && connectorid.length() > 0) {
                        connector = connectors.find("connectortypeid", io.getProperty("connectortypeid", ""));
                    }
                    if (connector == null) {
                        connector = new PropertyList();
                    }
                    script.append("jsPlumb.addEndpoint('" + windowid + "',");
                    boolean output = io.getProperty("ioflag", "o").equalsIgnoreCase("o");
                    if (output) {
                        anc = 1.0 / (double)outputtotal * (double)outputcount + 1.0 / ((double)outputtotal * 2.0);
                        if (sapphireConnection != null && sapphireConnection.isRtl()) {
                            script.append("{anchor:[[0,").append(anc).append("],[").append(anc).append(",0]]},{");
                        } else {
                            script.append("{anchor:[[1,").append(anc).append("],[").append(anc).append(",1]]},{");
                        }
                        script.append("isSource:true,");
                        script.append("isTarget:false,");
                        ++outputcount;
                    } else {
                        anc = 1.0 / (double)inputtotal * (double)inputcount + 1.0 / ((double)inputtotal * 2.0);
                        if (sapphireConnection != null && sapphireConnection.isRtl()) {
                            script.append("{anchor:[[1,").append(anc).append("],[").append(anc).append(",1]]},{");
                        } else {
                            script.append("{anchor:[[0,").append(anc).append("],[").append(anc).append(",0]]},{");
                        }
                        script.append("isSource:false,");
                        script.append("isTarget:true,");
                        ++inputcount;
                    }
                    script.append("endpoint:[\"Dot\", {radius:12} ],");
                    script.append("reattach:false,");
                    script.append("paintStyle:{fillStyle:\"" + connector.getProperty("color", "#316b31") + "\", opacity:0.5}");
                    String scope = connectorid;
                    if (scope.length() > 0) {
                        script.append(",scope:\"" + connectorid + "\"");
                    }
                    String iotitle = io.getProperty("iodesc", "");
                    if (labels) {
                        if (sapphireConnection != null && sapphireConnection.isRtl()) {
                            script.append(",overlays:[[\"Label\",{location:").append(output ? "[-4,0.5]" : "[5,0.5]").append(",label:\"").append(iotitle.length() > 0 ? iotitle : "Untitled " + (output ? "Output" : "Input")).append("\",cssClass:\"painter_label\"}]]");
                        } else {
                            script.append(",overlays:[[\"Label\",{location:").append(output ? "[5,0.5]" : "[-4,0.5]").append(",label:\"").append(iotitle.length() > 0 ? iotitle : "Untitled " + (output ? "Output" : "Input")).append("\",cssClass:\"painter_label\"}]]");
                        }
                    }
                    script.append("}).setParameter(\"ioid\", \"" + io.getProperty("ioid") + "\");");
                    continue;
                }
                logger.warn("IO skipped due to blank IOId.");
            }
        }
        return script.toString();
    }

    private String getPainterHtml(PropertyList taskprops, boolean viewOnly, boolean devMode) {
        StringBuffer html = new StringBuffer();
        StringBuffer script = new StringBuffer();
        StringBuffer style = new StringBuffer();
        double top = 0.0;
        html.append("<div id=\"painter_canvas\" class=\"painter_canvas\" style=\"top:").append(top).append("px;\">");
        PropertyListCollection ios = taskprops.getCollection("taskio");
        if (ios == null) {
            ios = new PropertyListCollection();
            taskprops.setProperty("taskio", ios);
        }
        TaskDefWorkflow.renderWindow(taskprops, html, script, style, false, viewOnly, TaskDefWorkflow.getConnectors(this.getSDIProcessor()), this.getConnectionId(), this.browser, this.logger);
        html.append("</div>");
        html.append("<script>");
        html.append(JS_CLASS).append(".plumb.deploy = function( ){");
        html.append(script);
        html.append("};");
        html.append("</script>");
        html.append("<style>");
        html.append(style);
        html.append("</style>");
        return html.toString();
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.taskprops != null) {
            html.append(this.getScriptAndStyle());
            boolean rtl = this.getConnectionProcessor().getSapphireConnection().isRtl();
            html.append("<div id=\"painter_root_container\" style=\"min-width:800px;position:absolute;top:0;left:0;height:100%;width:100%;overflow-y:").append(this.viewonly ? "hidden" : "auto").append(";\">");
            int h = 250;
            html.append("<div id=\"painter_top_container\" style=\"height:").append(h).append("px;\">");
            StringBuffer sb = new StringBuffer();
            html.append("<div id=\"painter_content_container\" style=\"width:auto;height:100%;border-left:solid 1px #95A9C3;border-bottom:solid 1px #95A9C3;overflow:hidden;position:relative;\">");
            html.append(this.getPainterHtml(this.taskprops, this.viewonly, this.devMode));
            html.append("</div>");
            html.append("</div>");
            html.append("<div id=\"painter_bottom_container\" style=\"width:auto;display:").append("block").append(";height:auto;position:absolute;bottom:0;right:0;left:0;min-height:100px;top:").append(h + 1).append("px;\">");
            html.append("<div id=\"painter_bottombar\" style=\"width:auto;overflow:hidden;\">");
            html.append("<div align=\"").append(rtl ? "right" : "left").append("\" id=\"painter_properties_outer\">");
            html.append("<div align=\"left\" id=\"painter_properties_content\"  style=\"margin-left:4px;margin-right:4px;margin-bottom:2px;border-radius:4px;border:solid 2px #7F9DB9;background-color:#FFFFFF;overflow-y:hidden;overflow-x:hidden;width:auto;;height:auto;position:absolute;bottom:0;top:2px;left:0;right:0;\">");
            html.append(TaskDefWorkflow.getPropertiesHtml(TaskDefWorkflowProperties.ItemType.TASK, "", null, this.viewonly, this.descendant, this.getConnectionId(), this.userConfig, this.getTranslationProcessor(), this.pageContext.getSession(), this.browser));
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
            html.append(this.getEndScript(this.viewonly, this.descendant));
        } else {
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load properties for task.")).append("</font>");
        }
        return html.toString();
    }

    public static String getPropertiesHtml(TaskDefWorkflowProperties.ItemType type, String ioid, PropertyList props, boolean viewOnly, boolean descendant, String connectionId, PropertyList userConfig, TranslationProcessor tp, HttpSession session, Browser browser) {
        StringBuffer html = new StringBuffer();
        if (connectionId != null && connectionId.length() > 0) {
            if (tp == null) {
                tp = new TranslationProcessor(connectionId);
            }
            html.append("<iframe name=\"taskworkflow_props_frame\" id=\"taskworkflow_props_frame\" frameborder=0 scrolling=false src=\"").append(browser.getBlankSrc()).append("\" style=\"width:100%;height:100%;\"></iframe>");
            html.append("<form style=\"display:none\" method=\"post\" id=\"taskworkflow_props_form\" name=\"taskworkflow_props_form\" action=\"rc?command=file&file=WEB-CORE/elements/workflow/taskdefworkflowproperties.jsp\" target=\"taskworkflow_props_frame\">");
            html.append("<input type=\"hidden\" name=\"ioid\" value=\"").append(ioid).append("\">");
            html.append("<input type=\"hidden\" name=\"type\" value=\"").append(type.toString().toLowerCase()).append("\">");
            html.append("<input type=\"hidden\" name=\"viewonly\" value=\"").append(viewOnly ? "Y" : "N").append("\">");
            html.append("<input type=\"hidden\" name=\"descendant\" value=\"").append(descendant ? "Y" : "N").append("\">");
            html.append("<textarea style=\"display:none;\" name=\"properties\">").append(props != null ? props.toJSONString(false) : "{}").append("</textarea>");
            html.append("</form>");
        } else {
            html.append("<font style=\"color:red;\">No connection Id provided</font>");
        }
        return html.toString();
    }

    public static enum Appearance {
        OFFSET,
        IMAGE,
        INLINE,
        DECISION;

    }
}

