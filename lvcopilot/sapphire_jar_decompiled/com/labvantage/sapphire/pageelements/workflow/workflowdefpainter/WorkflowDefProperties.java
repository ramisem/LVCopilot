/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.sapphire.admin.propertytree.VariableEditor;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefVariables;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflow;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import com.labvantage.sapphire.tagext.SDITagUtil;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class WorkflowDefProperties
extends BaseElement {
    public static final String USERCONFIG_PREFIX = "workflowprops_";
    public static final String JS_CLASS = "workflowProps";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_TASKPROPERTIES = "taskproperties";
    public static final String PROPERTY_SOURCE = "source";
    public static final String PROPERTY_TARGET = "target";
    public static final String PROPERTY_INPUT = "input";
    public static final String PROPERTY_OUTPUT = "output";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private boolean devMode;
    private PropertyList fullprops = null;
    private PropertyList taskprops = null;
    private PropertyList elementprops = null;
    private PropertyList ioprops = null;
    private PropertyList basedOn = null;
    private ItemType type = null;
    private String sourceTask = "";
    private String targetTask = "";
    private String input = "";
    private String output = "";
    private WorkflowDefPainter.Tools elementType = WorkflowDefPainter.Tools.LABEL;
    private boolean prototypeTask = false;

    public WorkflowDefProperties(PageContext pageContext, PropertyList pageproperties) {
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
            this.fullprops = null;
            this.taskprops = null;
            this.ioprops = null;
            this.elementprops = null;
            this.logger.error("Could not set up painter: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        String workflowp;
        pagedata.setProperty("jsrequest", "exclude=properties");
        this.sourceTask = pagedata.getProperty(PROPERTY_SOURCE, "");
        this.logger.debug("sourceTask = " + this.sourceTask);
        this.targetTask = pagedata.getProperty(PROPERTY_TARGET, "");
        this.logger.debug("targetTask = " + this.targetTask);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        if (pagedata.getProperty(PROPERTY_TYPE, "").length() > 0) {
            try {
                this.type = ItemType.valueOf(pagedata.getProperty(PROPERTY_TYPE, "").toUpperCase());
            }
            catch (Exception e) {
                this.type = null;
            }
        }
        this.logger.debug("type = " + (this.type == null ? "null" : this.type.toString()));
        if ((this.type == null || this.type == ItemType.TASK && this.sourceTask.length() > 0 || this.type == ItemType.IO && this.sourceTask.length() > 0 && this.targetTask.length() > 0 || this.type == ItemType.ELEMENT && this.sourceTask.length() > 0) && (workflowp = pagedata.getProperty(PROPERTY_PROPERTIES, "")).length() > 0) {
            try {
                this.fullprops = new PropertyList(new JSONObject(workflowp));
                if (this.type != null) {
                    if (this.type == ItemType.ELEMENT) {
                        PropertyListCollection elements = this.fullprops.getCollection("elements");
                        if (elements != null) {
                            this.elementprops = elements.find("elementid", this.sourceTask);
                            if (this.elementprops == null) {
                                this.logger.warn("Could not find element " + this.sourceTask);
                            }
                        } else {
                            this.logger.warn("Could not find elements");
                        }
                    } else {
                        PropertyListCollection tasks = this.fullprops.getCollection("tasks");
                        if (tasks != null) {
                            this.taskprops = tasks.find("taskdefitemid", this.type == ItemType.IO ? this.targetTask : this.sourceTask);
                            if (this.taskprops != null) {
                                this.prototypeTask = this.taskprops.getProperty("prototype", "N").equalsIgnoreCase("Y");
                                PropertyList propertyList = this.basedOn = this.prototypeTask ? null : WorkflowDefMaint.getTaskData(this.taskprops.getProperty("taskdefid", ""), this.taskprops.getProperty("taskdefversionid", ""), this.taskprops.getProperty("taskdefvariantid", ""), this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                                if (this.basedOn == null && !this.prototypeTask) {
                                    this.basedOn = new PropertyList();
                                }
                                if (this.type == ItemType.IO) {
                                    this.input = pagedata.getProperty(PROPERTY_INPUT, "");
                                    this.logger.debug("input = " + this.input);
                                    this.output = pagedata.getProperty(PROPERTY_OUTPUT, "");
                                    this.logger.debug("output = " + this.output);
                                    PropertyListCollection ios = this.taskprops.getCollection("taskio");
                                    if (ios != null) {
                                        if (this.output.length() > 0) {
                                            this.ioprops = ios.find("ioid", this.input);
                                            if (ios == null) {
                                                this.logger.warn("Could not find io " + this.output);
                                            }
                                        }
                                    } else {
                                        this.logger.warn("Could not find ios");
                                    }
                                }
                            } else {
                                this.logger.warn("Could not find task " + this.sourceTask);
                            }
                        } else {
                            this.logger.warn("Could not find tasks");
                        }
                    }
                }
            }
            catch (Exception e) {
                this.fullprops = null;
                this.taskprops = null;
                this.ioprops = null;
                this.elementprops = null;
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
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/workflowprops.js\"></script>");
        html.append("\n<style>");
        html.append("\n.task_longtext { width:400px; }");
        html.append("\n</style>");
        return html;
    }

    private StringBuffer getEndScript() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append(JS_CLASS).append(".type = '").append(this.type != null ? SafeHTML.encodeForJavaScript(this.type.toString().toLowerCase()) : "workflow").append("';");
        html.append(JS_CLASS).append(".source = '").append(SafeHTML.encodeForJavaScript(this.sourceTask)).append("';");
        html.append(JS_CLASS).append(".target = '").append(SafeHTML.encodeForJavaScript(this.targetTask)).append("';");
        html.append(JS_CLASS).append(".input = '").append(SafeHTML.encodeForJavaScript(this.input)).append("';");
        html.append(JS_CLASS).append(".output = '").append(SafeHTML.encodeForJavaScript(this.output)).append("';");
        html.append(JS_CLASS).append(".viewonly = ").append(this.viewonly).append(";");
        html.append("</script>");
        return html;
    }

    private StringBuffer getTaskTaskTab() {
        StringBuffer html = new StringBuffer();
        DataSet dsFake = new DataSet();
        dsFake.addColumn("taskdefitemid", 0);
        dsFake.addColumn("longtitle", 0);
        dsFake.addColumn("shorttitle", 0);
        dsFake.addColumn("taskcolor1", 0);
        dsFake.addColumn("taskcolor2", 0);
        dsFake.addColumn("taskcolor3", 0);
        dsFake.addColumn("appearance", 0);
        dsFake.addColumn("icon", 0);
        String color1 = this.taskprops.getProperty("taskcolor1", "");
        String color2 = this.taskprops.getProperty("taskcolor2", "");
        String color3 = this.taskprops.getProperty("taskcolor3", "");
        dsFake.addRow();
        String taskid = this.taskprops.getProperty("taskdefitemid", "");
        dsFake.setValue(0, "taskdefitemid", taskid);
        dsFake.setValue(0, "longtitle", this.taskprops.getProperty("longtitle", ""));
        dsFake.setValue(0, "shorttitle", this.taskprops.getProperty("shorttitle", ""));
        dsFake.setValue(0, "icon", this.taskprops.getProperty("icon", ""));
        dsFake.setValue(0, "taskcolor1", color1);
        dsFake.setValue(0, "taskcolor2", color2);
        dsFake.setValue(0, "taskcolor3", color3);
        dsFake.setValue(0, "appearance", this.taskprops.getProperty("appearance", TaskDefWorkflow.Appearance.OFFSET.toString().substring(0, 1) + TaskDefWorkflow.Appearance.OFFSET.toString().toLowerCase().substring(1)));
        DataView maint = new DataView(this.pageContext, "primary", dsFake, "", this.getConnectionId());
        maint.setElementid("task_details_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "Form");
        maintProps.setProperty("formcols", "2");
        if (this.viewonly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        PropertyList event = new PropertyList();
        event.setProperty("event", "onchange");
        event.setProperty("js", "workflowProps.maintFieldChange(event,this,'task','" + taskid + "','')");
        events.add(event);
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "longtitle");
        column.setProperty("title", "Execution Title");
        column.setProperty("mode", !this.viewonly ? PROPERTY_INPUT : "readonly");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "shorttitle");
        column.setProperty("title", "Icon Text");
        column.setProperty("mode", !this.viewonly ? PROPERTY_INPUT : "readonly");
        column.setProperty("events", events);
        columns.add(column);
        if (!this.prototypeTask) {
            column = new PropertyList();
            column.setProperty("columnid", "appearance");
            column.setProperty("title", "Appearance");
            column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
            column.setProperty("events", events);
            StringBuffer values = new StringBuffer();
            for (TaskDefWorkflow.Appearance app : TaskDefWorkflow.Appearance.values()) {
                if (values.length() > 0) {
                    values.append(";");
                }
                String v = app.toString().toLowerCase();
                v = v.substring(0, 1).toUpperCase() + v.substring(1);
                values.append(v);
            }
            column.setProperty("dropdownvalues", values.toString());
            column.setProperty("groupid", "Appearance");
            columns.add(column);
        }
        column = new PropertyList();
        column.setProperty("columnid", "icon");
        column.setProperty("title", "Icon");
        column.setProperty("mode", !this.viewonly ? "icon" : "readonly");
        column.setProperty("events", events);
        column.setProperty("class", "task_longtext");
        column.setProperty("groupid", "Title &amp; Image");
        columns.add(column);
        if (!this.prototypeTask) {
            column = new PropertyList();
            column.setProperty("columnid", "_color");
            column.setProperty("title", "Task Color");
            column.setProperty("mode", "");
            StringBuffer colorHtml = new StringBuffer();
            colorHtml.append("<div style=\"display:inline").append("-block").append(";height:20px;top:").append(this.browser.isIE() ? "0" : "3px").append(";position:relative;border:solid gray 1px;\">");
            colorHtml.append("<div id=\"_taskcolor1\" _default=\"").append("#e7f2ff").append("\" style=\"display:inline").append("-block").append(";width:20px;height:20px;background-color:").append(color1.length() > 0 ? color1 : "#e7f2ff").append("\"></div>");
            colorHtml.append("<div id=\"_taskcolor2\" _default=\"").append("#d4e3f2").append("\" style=\"display:inline").append("-block").append(";;width:20px;height:20px;background-color:").append(color2.length() > 0 ? color2 : "#d4e3f2").append("\"></div>");
            colorHtml.append("<div id=\"_taskcolor3\" _default=\"").append("#c9d8ed").append("\" style=\"display:inline").append("-block").append(";;width:20px;height:20px;background-color:").append(color3.length() > 0 ? color3 : "#c9d8ed").append("\"></div>");
            colorHtml.append("</div>");
            if (!this.viewonly) {
                Button btn = new Button(this.pageContext);
                btn.setId("btTaskColor");
                btn.setText("...");
                btn.setAction("workflowProps.lookupTaskColor()");
                colorHtml.append("&nbsp;").append(btn.getHtml());
            }
            column.setProperty("pseudocolumn", colorHtml.toString());
            column.setProperty("events", events);
            column.setProperty("groupid", "Appearance");
            columns.add(column);
        }
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append("<div style=\"padding:5px 5px 5px 5px;\">");
        if (!this.viewonly) {
            Button btn = new Button(this.pageContext);
            btn.setId("btnStartTask");
            btn.setAction("workflowProps.toggleStartTask()");
            boolean starttask = this.taskprops.getProperty("starttaskflag", "N").equalsIgnoreCase("Y");
            boolean endtask = this.taskprops.getProperty("endtaskflag", "N").equalsIgnoreCase("Y");
            if (endtask && starttask) {
                this.taskprops.setProperty("endtaskflag", "N");
                endtask = false;
            }
            if (starttask) {
                btn.setText("Unset as Start Task");
            } else {
                btn.setText("Set as Start Task");
            }
            if (this.viewonly || endtask) {
                btn.setDisabled(true);
            }
            btn.setImg("rc?command=image&image=BulletTriangleGreen&size=16");
            html.append(btn.getHtml());
            html.append("&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId("btnEndTask");
            btn.setAction("workflowProps.toggleEndTask()");
            if (endtask) {
                btn.setText("Unset as End Task");
            } else {
                btn.setText("Set as End Task");
            }
            if (this.viewonly || starttask) {
                btn.setDisabled(true);
            }
            btn.setImg("rc?command=image&image=BulletSquareRed&size=16");
            html.append(btn.getHtml());
            html.append("&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId("btnReloadTask");
            btn.setAction("workflowProps.refreshTask()");
            btn.setText("Reset Task");
            if (this.prototypeTask) {
                btn.setDisabled(true);
            }
            btn.setImg("rc?command=image&image=Refresh&size=16");
            html.append(btn.getHtml());
        }
        html.append(maint.getHtml());
        html.append("</div>");
        return html;
    }

    private StringBuffer getTaskIOTab(boolean output) {
        StringBuffer html = new StringBuffer();
        DataSet dsFake = new DataSet();
        dsFake.addColumn("ioid", 0);
        dsFake.addColumn("iodesc", 0);
        dsFake.addColumn("ioflag", 0);
        dsFake.addColumn("connectortypeid", 0);
        PropertyListCollection taskios = this.taskprops.getCollection("taskio");
        if (taskios != null) {
            for (int i = 0; i < taskios.size(); ++i) {
                int r = dsFake.addRow();
                PropertyList io = taskios.getPropertyList(i);
                dsFake.setValue(r, "ioid", io.getProperty("ioid", ""));
                dsFake.setValue(r, "iodesc", io.getProperty("iodesc", ""));
                dsFake.setValue(r, "ioflag", io.getProperty("ioflag", ""));
                dsFake.setValue(r, "connectortypeid", io.getProperty("connectortypeid", ""));
            }
        }
        if (!this.viewonly) {
            if (!output) {
                Button btn = new Button(this.pageContext);
                btn.setText("Add Input");
                btn.setId("btAddInput");
                btn.setImg("rc?command=image&image=SignpostAdd");
                btn.setAction("workflowProps.addElementTaskIO('" + this.taskprops.getProperty("taskdefitemid") + "','I')");
                html.append(btn.getHtml());
                html.append("&nbsp;");
                btn = new Button(this.pageContext);
                btn.setText("Delete");
                btn.setId("btDeleteIO");
                btn.setImg("rc?command=image&image=SignpostDelete");
                btn.setAction("workflowProps.deleteElementTaskIO('" + this.taskprops.getProperty("taskdefitemid") + "','I')");
                html.append(btn.getHtml());
            } else {
                Button btn = new Button(this.pageContext);
                btn.setText("Add Output");
                btn.setId("btAddOutput");
                btn.setImg("rc?command=image&image=SignpostAdd");
                btn.setAction("workflowProps.addElementTaskIO('" + this.taskprops.getProperty("taskdefitemid") + "','O')");
                html.append(btn.getHtml());
                html.append("&nbsp;");
                btn = new Button(this.pageContext);
                btn.setText("Delete");
                btn.setId("btDeleteIO");
                btn.setImg("rc?command=image&image=SignpostDelete");
                btn.setAction("workflowProps.deleteElementTaskIO('" + this.taskprops.getProperty("taskdefitemid") + "','O')");
                html.append(btn.getHtml());
            }
        }
        if (dsFake.getRowCount() > 0) {
            StringBuffer vals = new StringBuffer();
            TaskDefWorkflow.getConnectorsDisplayValue(vals, "", this.getSDIProcessor());
            PropertyListCollection events = new PropertyListCollection();
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "workflowProps.elementTaskIOFieldChange(event,this,'" + this.taskprops.getProperty("taskdefitemid") + "')");
            events.add(event);
            PropertyListCollection columns = new PropertyListCollection();
            PropertyList column = new PropertyList();
            column.setProperty("columnid", "ioid");
            column.setProperty("title", "Id");
            column.setProperty("mode", "hidden");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "iodesc");
            column.setProperty("title", "Description");
            column.setProperty("mode", !this.viewonly ? PROPERTY_INPUT : "readonly");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "connectortypeid");
            column.setProperty("title", "Connector");
            column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
            column.setProperty("displayvalue", vals.toString());
            column.setProperty("events", events);
            columns.add(column);
            HashMap<String, String> filter = new HashMap<String, String>();
            if (!output) {
                filter.put("ioflag", "I");
                DataSet dsInput = dsFake.getFilteredDataSet(filter);
                html.append("<p>Inputs:</p><blockquote>");
                if (dsInput.getRowCount() > 0) {
                    DataView detailInput = new DataView(this.pageContext, "inputs", dsInput, "", this.getConnectionId());
                    detailInput.setSDCId("LV_TaskDef");
                    detailInput.getSDIInfo().setSdcid("LV_TaskDef");
                    PropertyList detailProps = new PropertyList();
                    detailProps.setId("element_details_i_dataview");
                    detailInput.setElementid(detailProps.getId());
                    detailProps.setProperty("sdcid", "LV_TaskDef");
                    detailProps.setProperty("style", "GridWithCheckBox");
                    detailProps.setProperty("columns", columns);
                    detailInput.setElementProperties(detailProps);
                    html.append(detailInput.getHtml());
                } else {
                    html.append("No Inputs defined.");
                }
                html.append("</blockquote>");
            } else {
                filter.put("ioflag", "O");
                DataSet dsOutput = dsFake.getFilteredDataSet(filter);
                html.append("<p>Outputs:</p><blockquote>");
                if (dsOutput.getRowCount() > 0) {
                    DataView detailOutput = new DataView(this.pageContext, "outputs", dsOutput, "", this.getConnectionId());
                    detailOutput.setElementid("element_details_o_dataview");
                    detailOutput.setSDCId("LV_TaskDef");
                    detailOutput.getSDIInfo().setSdcid("LV_TaskDef");
                    PropertyList detailProps = new PropertyList();
                    detailProps.setId("element_details_o_dataview");
                    detailOutput.setElementid(detailProps.getId());
                    detailProps.setProperty("sdcid", "LV_TaskDef");
                    detailProps.setProperty("style", "GridWithCheckBox");
                    detailProps.setProperty("columns", columns);
                    detailOutput.setElementProperties(detailProps);
                    html.append(detailOutput.getHtml());
                } else {
                    html.append("No Outputs defined.");
                }
                html.append("</blockquote>");
            }
        } else {
            html.append("<p>No Inputs or Outputs defined.</p>");
        }
        return html;
    }

    private StringBuffer getElementElementTab() {
        StringBuffer html = new StringBuffer();
        DataSet dsFake = new DataSet();
        dsFake.addColumn("elementid", 0);
        dsFake.addColumn("text", 0);
        dsFake.addColumn("fontsize", 0);
        dsFake.addColumn("textcolor", 0);
        dsFake.addColumn("showbackground", 0);
        dsFake.addColumn("backgroundcolor", 0);
        dsFake.addColumn("backgroundopacity", 0);
        dsFake.addColumn("showline", 0);
        dsFake.addColumn("linestyle", 0);
        dsFake.addColumn("linecolor", 0);
        dsFake.addColumn("linewidth", 0);
        dsFake.addColumn("sourcearrow", 0);
        dsFake.addColumn("targetarrow", 0);
        dsFake.addColumn("image", 0);
        dsFake.addRow();
        String elementid = this.elementprops.getProperty("elementid", "");
        dsFake.setValue(0, "elementid", elementid);
        if (this.elementType == WorkflowDefPainter.Tools.LABEL || this.elementType == WorkflowDefPainter.Tools.TEXT) {
            dsFake.setValue(0, "text", this.elementprops.getProperty("text", this.elementType == WorkflowDefPainter.Tools.LABEL ? "A Label" : "A Textbox"));
        }
        if (this.elementType == WorkflowDefPainter.Tools.LABEL || this.elementType == WorkflowDefPainter.Tools.TEXT) {
            dsFake.setValue(0, "fontsize", this.elementprops.getProperty("fontsize", ""));
            dsFake.setValue(0, "textcolor", this.elementprops.getProperty("textcolor", ""));
        }
        if (this.elementType == WorkflowDefPainter.Tools.IMAGE) {
            dsFake.setValue(0, "image", this.elementprops.getProperty("image", "WEB-CORE/images/blank.gif"));
        }
        if (this.elementType != WorkflowDefPainter.Tools.LABEL) {
            dsFake.setValue(0, "showbackground", this.elementprops.getProperty("showbackground", ""));
            dsFake.setValue(0, "backgroundcolor", this.elementprops.getProperty("backgroundcolor", ""));
            dsFake.setValue(0, "backgroundopacity", this.elementprops.getProperty("backgroundopacity", ""));
            dsFake.setValue(0, "showline", this.elementprops.getProperty("showline", ""));
            dsFake.setValue(0, "linestyle", this.elementprops.getProperty("linestyle", ""));
            dsFake.setValue(0, "linecolor", this.elementprops.getProperty("linecolor", ""));
            dsFake.setValue(0, "linewidth", this.elementprops.getProperty("linewidth", ""));
        }
        if (this.elementType == WorkflowDefPainter.Tools.LINE) {
            dsFake.setValue(0, "sourcearrow", this.elementprops.getProperty("sourcearrow", "N"));
            dsFake.setValue(0, "targetarrow", this.elementprops.getProperty("targetarrow", "N"));
        }
        DataView maint = new DataView(this.pageContext, "primary", dsFake, "", this.getConnectionId());
        maint.setElementid("element_details_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "Form");
        maintProps.setProperty("formcols", "2");
        if (this.viewonly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        PropertyList event = new PropertyList();
        event.setProperty("event", "onchange");
        event.setProperty("js", "workflowProps.maintFieldChange(event,this,'element','" + elementid + "','')");
        events.add(event);
        switch (this.elementType) {
            case IMAGE: {
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "image");
                column.setProperty("title", "Image");
                column.setProperty("mode", !this.viewonly ? "image" : "readonly");
                column.setProperty("events", events);
                columns.add(column);
            }
            case CIRCLE: 
            case BOX: {
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "showline");
                column.setProperty("title", "Show Border");
                column.setProperty("mode", !this.viewonly ? "checkbox" : "readonly");
                column.setProperty("displayvalue", "Y=Yes;N=No");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "linestyle");
                column.setProperty("title", "Border Style");
                column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "solid=Solid;dashed=Dashed;dotted=Dotted;");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "linewidth");
                column.setProperty("title", "Border Width");
                column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "1=1px;2=2px;4=4px;6=6px");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "linecolor");
                column.setProperty("title", "Border Color");
                column.setProperty("mode", !this.viewonly ? "color" : "readonly");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "showbackground");
                column.setProperty("title", "Show Background");
                column.setProperty("mode", !this.viewonly ? "checkbox" : "readonly");
                column.setProperty("displayvalue", "Y=Yes;N=No");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "backgroundcolor");
                column.setProperty("title", "Background Color");
                column.setProperty("mode", !this.viewonly ? "color" : "readonly");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "backgroundopacity");
                column.setProperty("title", "Opacity");
                column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "1=100%;0.8=80%;0.4=40%;0.2=20%");
                column.setProperty("events", events);
                columns.add(column);
                break;
            }
            case TEXT: {
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "text");
                column.setProperty("title", "Text");
                column.setProperty("mode", !this.viewonly ? PROPERTY_INPUT : "readonly");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "fontsize");
                column.setProperty("title", "Size");
                column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "8=8pt;9=9pt;10=10pt;12=12pt;14=14pt;18=18pt;24=24pt;30=30pt");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "textcolor");
                column.setProperty("title", "Color");
                column.setProperty("mode", !this.viewonly ? "color" : "readonly");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "showline");
                column.setProperty("title", "Show Border");
                column.setProperty("mode", !this.viewonly ? "checkbox" : "readonly");
                column.setProperty("displayvalue", "Y=Yes;N=No");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "linestyle");
                column.setProperty("title", "Border Style");
                column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "Solid=Solid;Dashed=Dashed;Dotted=Dotted");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "linewidth");
                column.setProperty("title", "Line Width");
                column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "1=1px;2=2px;4=4px;6=6px");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "linecolor");
                column.setProperty("title", "Line Color");
                column.setProperty("mode", !this.viewonly ? "color" : "readonly");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "showbackground");
                column.setProperty("title", "Show Background");
                column.setProperty("mode", !this.viewonly ? "checkbox" : "readonly");
                column.setProperty("displayvalue", "Y=Yes;N=No");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "backgroundcolor");
                column.setProperty("title", "Background Color");
                column.setProperty("mode", !this.viewonly ? "color" : "readonly");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "backgroundopacity");
                column.setProperty("title", "Opacity");
                column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "1=100%;0.8=80%;0.4=40%;0.2=20%");
                column.setProperty("events", events);
                columns.add(column);
                break;
            }
            case LABEL: {
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "text");
                column.setProperty("title", "Text");
                column.setProperty("mode", !this.viewonly ? PROPERTY_INPUT : "readonly");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "fontsize");
                column.setProperty("title", "Size");
                column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "8=8pt;9=9pt;10=10pt;12=12pt;14=14pt;18=18pt;24=24pt;30=30pt");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "textcolor");
                column.setProperty("title", "Color");
                column.setProperty("mode", !this.viewonly ? "color" : "readonly");
                column.setProperty("events", events);
                columns.add(column);
                break;
            }
            case LINE: {
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "linewidth");
                column.setProperty("title", "Line Width");
                column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
                column.setProperty("displayvalue", "1=1px;2=2px;4=4px;6=6px");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "linecolor");
                column.setProperty("title", "Line Color");
                column.setProperty("mode", !this.viewonly ? "color" : "readonly");
                column.setProperty("events", events);
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "targetarrow");
                column.setProperty("title", "Target Arrow");
                column.setProperty("mode", !this.viewonly ? "checkbox" : "readonly");
                column.setProperty("displayvalue", "Y=Yes;N=No");
                column.setProperty("events", events);
                columns.add(column);
            }
        }
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append("<div style=\"padding:5px 5px 5px 5px;\">");
        html.append(maint.getHtml());
        html.append("</div>");
        return html;
    }

    public static String getTaskSetupVariables(PropertyList basedOn, PropertyList taskprops, PropertyList fullprops, boolean viewonly, boolean valueOnly, String jsClass, String connectionId, SDITagInfo sdiInfo, TranslationProcessor translationProcessor, PageContext pageContext, Logger logger) {
        PropertyListCollection variables = basedOn == null ? null : basedOn.getCollection("variables");
        StringBuffer html = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        StringBuffer hid = new StringBuffer();
        if (variables != null) {
            boolean found = false;
            for (int i = 0; i < variables.size(); ++i) {
                int index;
                PropertyList overrideVar;
                PropertyList var = variables.getPropertyList(i);
                boolean setupVar = false;
                if (var.containsKey("setup")) {
                    setupVar = var.getProperty("setup", "N").equalsIgnoreCase("Y");
                } else if (var.containsKey("scope")) {
                    setupVar = var.getProperty("scope", "L").equalsIgnoreCase("S");
                }
                String varId = var.getProperty("variableid", "");
                String change = jsClass + ".variableFieldChange(event,this,'" + varId + "'," + i + ")";
                if (!setupVar || varId.length() <= 0) continue;
                PropertyList propertyList = overrideVar = taskprops.getCollection("variables") != null ? taskprops.getCollection("variables").find("variableid", varId) : null;
                if (overrideVar == null) {
                    overrideVar = new PropertyList();
                }
                String editorstyle = var.getProperty("editorstyleid", "");
                String defaultvalue = overrideVar.containsKey("defaultvalue") ? overrideVar.getProperty("defaultvalue", "") : var.getProperty("defaultvalue", "");
                String relatedvariableid = var.getProperty("relatedvariableid", "");
                boolean mandatory = var.getProperty("mandatory", "N").equalsIgnoreCase("Y");
                boolean modifiable = !var.getProperty("modifiable", "Y").equalsIgnoreCase("N");
                boolean hidden = var.getProperty("hidden", "N").equalsIgnoreCase("Y");
                String varidrep = StringUtil.replaceAll(varId, " ", "_");
                String dataType = var.getProperty(PROPERTY_TYPE, "string");
                String workflowVariableid = "";
                int n = index = defaultvalue.length() > 0 ? defaultvalue.indexOf("[workflow.variables.") : -1;
                if (index > -1) {
                    workflowVariableid = defaultvalue.substring(index + "[workflow.variables.".length());
                    workflowVariableid = workflowVariableid.substring(0, workflowVariableid.indexOf("]"));
                }
                if (hidden) {
                    hid.append(TaskDefVariables.getVariableField("defaultvalue", "hidden", varidrep, i, defaultvalue, "", "", "", viewonly, change, connectionId, pageContext, sdiInfo, translationProcessor).getHtml());
                    continue;
                }
                sb.append("<tr>");
                sb.append("<td id=\"").append("variable").append(i).append("_").append(varidrep).append("_titlecell\" class=\"maintform_fieldtitle\" style=\"border-left-color:#CCCCCC;color:").append(viewonly ? "red" : "black").append(";\"");
                if (var.getProperty("help", "").length() > 0) {
                    sb.append(" title=\"").append(var.getProperty("help", "")).append("\"");
                }
                sb.append(">");
                sb.append(var.getProperty("prompt", var.getProperty("variableid", "Variable " + i))).append(": ");
                sb.append("</td>");
                sb.append("<td class=\"maintform_field\" nowrap>");
                EditorStyleField esf = null;
                if (workflowVariableid.length() == 0 && editorstyle.length() > 0) {
                    esf = new EditorStyleField(pageContext, null, connectionId);
                    try {
                        esf.setEditorStyleId(editorstyle);
                        esf.setColumnDefinition(varidrep, EditorStyleField.getEditorStyleDataType(dataType), 100, false, false);
                    }
                    catch (Exception e) {
                        logger.warn("Invalid editor style");
                        esf = new EditorStyleField(pageContext, null, connectionId);
                        String dt = EditorStyleField.getEditorStyleDataType(dataType);
                        esf.setDefaultEditorStyleProperties(dt, "", "");
                        esf.setColumnDefinition(varidrep, dt, 100, false, false);
                    }
                } else {
                    esf = new EditorStyleField(pageContext, null, connectionId);
                    String dt = EditorStyleField.getEditorStyleDataType(dataType);
                    esf.setDefaultEditorStyleProperties(dt, "", "");
                    esf.setColumnDefinition(varidrep, dt, 100, false, false);
                }
                if (esf != null) {
                    found = true;
                    if (defaultvalue.length() > 0) {
                        esf.setFieldValue(defaultvalue);
                    }
                    if (var.getProperty("help", "").length() > 0) {
                        esf.setColumnProperty("tip", var.getProperty("help", ""));
                    }
                    if (viewonly || !modifiable) {
                        esf.setColumnProperty("disable", "Y");
                    }
                    String validation = esf.getColumnProperty("validation");
                    if (mandatory) {
                        validation = validation.length() > 0 ? "Mandatory;" + validation : "Mandatory";
                        esf.setColumnProperty("validation", validation);
                    }
                    esf.setChangeEvent(change);
                    esf.setFieldName("variable" + i + "_defaultvalue");
                    TaskDefVariables.setEditorStyleFieldMapping(esf, varId, relatedvariableid, jsClass + ".variableLookupCallback");
                    sb.append(esf.getHtml());
                }
                sb.append("</td>");
                if (!viewonly && !valueOnly) {
                    sb.append("<td class=\"maintform_field\">");
                    if (modifiable) {
                        VariableEditor ve = new VariableEditor();
                        HashMap<String, String> attributes = new HashMap<String, String>();
                        attributes.put("values", TaskDefVariables.getVariablesList(fullprops, false, false, false, new String[]{dataType}, null));
                        attributes.put("subvariables", "N");
                        attributes.put("readonly", "N");
                        attributes.put("rootvariables", "Y");
                        attributes.put("validtypes", dataType);
                        attributes.put("basedon", var.toJSONString(false));
                        attributes.put("customonchange", jsClass + ".variableFieldChange(event,this,'" + varidrep + "'," + i + ")");
                        PropertyValue pv = new PropertyValue("variableid", false, fullprops);
                        pv.value = workflowVariableid;
                        sb.append(ve.getEditor("variable" + i + "_workflowvariableid", pv, null, false, attributes, pageContext, false));
                    }
                    sb.append("</td>");
                }
                sb.append("</td>");
                sb.append("</tr>");
            }
            if (found) {
                html.append("<form name=\"workflow_props_setup\" action=\"#\">");
                html.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"maintform_table\">");
                html.append("<thead>");
                html.append("<tr class=\"gridmaint_tablehead\">");
                html.append("<th class=\"\">").append("&nbsp;").append("</th>");
                html.append("<th class=\"gridmaint_fieldtitle\" style=\"width:180px;\">").append(translationProcessor.translate("Value")).append("</th>");
                if (!viewonly && !valueOnly) {
                    html.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Load From Workflow Variable")).append("</th>");
                }
                html.append("</thead>");
                html.append("<tbody>");
                html.append(sb);
                html.append("</tbody>");
                html.append("</table>");
                html.append(hid);
                html.append("</form>");
            } else {
                html.append("No Setup Variables");
            }
        } else {
            html.append("No Setup Variables");
        }
        return html.toString();
    }

    private StringBuffer getTaskSetupTab() {
        StringBuffer html = new StringBuffer();
        html.append("<div style=\"padding:5px 5px 5px 5px;\">");
        html.append(WorkflowDefProperties.getTaskSetupVariables(this.basedOn, this.taskprops, this.fullprops, this.viewonly, false, JS_CLASS, this.getConnectionId(), this.sdiInfo, this.getTranslationProcessor(), this.pageContext, this.logger));
        html.append("</div>");
        return html;
    }

    private EditorStyleField getVariableField(String field, String mode, String variableid, int realrow, String value, String datatype, String relatedvariableid, String lookupcallback, boolean viewonly, String change) {
        return TaskDefVariables.getVariableField(field, mode, variableid, realrow, value, datatype, relatedvariableid, lookupcallback, viewonly, change, this.getConnectionId(), this.pageContext, this.sdiInfo, this.getTranslationProcessor());
    }

    private StringBuffer getTaskInputsTab() {
        StringBuffer html = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        html.append("<div style=\"padding:5px 5px 5px 5px;\">");
        PropertyListCollection taskios = this.taskprops == null ? null : this.taskprops.getCollection("taskio");
        boolean starttask = this.taskprops.getProperty("starttaskflag", "N").equalsIgnoreCase("Y");
        if (taskios != null) {
            boolean found = false;
            for (int i = 0; i < taskios.size(); ++i) {
                PropertyList taskio = taskios.getPropertyList(i);
                String ioflag = taskio.getProperty("ioflag", "O");
                String ioid = taskio.getProperty("ioid", "");
                if (!ioflag.equalsIgnoreCase("I") || ioid.length() <= 0) continue;
                String connectortypeid = taskio.getProperty("connectortypeid", "");
                String iodesc = taskio.getProperty("iodesc", "");
                boolean workflowioflag = starttask ? taskio.getProperty("workflowioflag", "Y").equalsIgnoreCase("Y") : false;
                String workflowioname = starttask ? taskio.getProperty("workflowioname", "") : "";
                sb.append("<tr>");
                sb.append("<td id=\"").append(PROPERTY_INPUT).append(i).append("_").append(ioid).append("_titlecell\" class=\"gridmaint_field\" style=\"\">");
                sb.append(ioid);
                sb.append("</td>");
                sb.append("<td class=\"gridmaint_field\">");
                sb.append(iodesc);
                sb.append("</td>");
                sb.append("<td class=\"gridmaint_field\">");
                sb.append(connectortypeid);
                sb.append("</td>");
                sb.append("<td class=\"gridmaint_field\">");
                SDITagUtil sdiTagUtil = new SDITagUtil(this.getConnectionId());
                PropertyList attributes = new PropertyList();
                attributes.setProperty("name", PROPERTY_INPUT + i + "_workflowioflag");
                attributes.setProperty("mode", "checkbox");
                attributes.setProperty("value", workflowioflag ? "Y" : "N");
                attributes.setProperty("displayvalue", "Y=Yes;N=No");
                attributes.setProperty("readonly", this.viewonly ? "true" : "false");
                if (!starttask) {
                    attributes.setProperty("disabled", "true");
                }
                attributes.setProperty("onchange", "workflowProps.ioFieldChange(event,this,'" + ioid + "'," + i + ")");
                sb.append(sdiTagUtil.getInputHtml(attributes, this.sdiInfo));
                sb.append("</td>");
                sb.append("<td class=\"gridmaint_field\">");
                sdiTagUtil = new SDITagUtil(this.getConnectionId());
                attributes = new PropertyList();
                attributes.setProperty("name", PROPERTY_INPUT + i + "_workflowioname");
                attributes.setProperty("mode", PROPERTY_INPUT);
                attributes.setProperty("value", workflowioname);
                attributes.setProperty("readonly", this.viewonly ? "true" : "false");
                if (!starttask || !workflowioflag) {
                    attributes.setProperty("disabled", "true");
                }
                attributes.setProperty("onchange", "workflowProps.ioFieldChange(event,this,'" + ioid + "'," + i + ")");
                attributes.setProperty("style", "width:125px;");
                sb.append(sdiTagUtil.getInputHtml(attributes, this.sdiInfo));
                sb.append("</td>");
                sb.append("</tr>");
                found = true;
            }
            if (found) {
                html.append("<form name=\"workflow_props_inputs\" action=\"#\">");
                html.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"gridmaint_table\">");
                html.append("<thead>");
                html.append("<tr class=\"gridmaint_tablehead\">");
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Input Id")).append("</th>");
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Description")).append("</th>");
                html.append("<th class=\"gridmaint_fieldtitle\" style=\"width:180px;\">").append(this.getTranslationProcessor().translate("Connector")).append("</th>");
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Workflow Input")).append("</th>");
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Workflow Input Name")).append("</th>");
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
        html.append("</div>");
        return html;
    }

    private StringBuffer getTaskTaskVariablesTab() {
        PropertyListCollection variables;
        StringBuffer html = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        StringBuffer hid = new StringBuffer();
        html.append("<div style=\"padding:5px 5px 5px 5px;\">");
        PropertyListCollection propertyListCollection = variables = this.basedOn == null ? null : this.basedOn.getCollection("variables");
        if (variables != null) {
            boolean found = false;
            for (int i = 0; i < variables.size(); ++i) {
                PropertyList overrideVar;
                PropertyList var = variables.getPropertyList(i);
                boolean exposedVar = false;
                if (var.containsKey("exposed")) {
                    exposedVar = var.getProperty("exposed", "N").equalsIgnoreCase("Y");
                } else if (var.containsKey("scope")) {
                    exposedVar = var.getProperty("scope", "L").equalsIgnoreCase("E");
                }
                String varId = var.getProperty("variableid", "");
                if (!exposedVar || varId.length() <= 0) continue;
                PropertyList propertyList = overrideVar = this.taskprops.getCollection("variables") != null ? this.taskprops.getCollection("variables").find("variableid", varId) : null;
                if (overrideVar == null) {
                    overrideVar = new PropertyList();
                }
                String varidrep = StringUtil.replaceAll(varId, " ", "_");
                String dataType = var.getProperty(PROPERTY_TYPE, "string");
                String workflowVariableid = overrideVar.getProperty("settoworkflowvariable", "");
                String description = var.getProperty("description", "");
                String change = "workflowProps.variableFieldChange(event,this,'" + varidrep + "'," + i + ")";
                sb.append("<tr>");
                sb.append("<td id=\"").append("variable").append(i).append("_").append(varidrep).append("_titlecell\" class=\"maintform_fieldtitle\" style=\"border-left-color:#CCCCCC;color:").append(this.viewonly ? "red" : "black").append(";\"");
                sb.append(">");
                sb.append(var.getProperty("variableid", "Variable " + i)).append(": ");
                sb.append("</td>");
                sb.append("<td class=\"gridmaint_field\">").append(this.getVariableField("description", "readonly", varidrep, i, description, "string", "", "", true, change).getHtml()).append("</td>");
                if (!this.viewonly) {
                    sb.append("<td class=\"maintform_field\">");
                    VariableEditor ve = new VariableEditor();
                    HashMap<String, String> attributes = new HashMap<String, String>();
                    attributes.put("values", TaskDefVariables.getVariablesList(this.fullprops, false, false, false, new String[]{dataType}, null));
                    attributes.put("subvariables", "N");
                    attributes.put("readonly", "N");
                    attributes.put("rootvariables", "Y");
                    attributes.put("validtypes", dataType);
                    attributes.put("basedon", var.toJSONString(false));
                    attributes.put("customonchange", change);
                    PropertyValue pv = new PropertyValue("settoworkflowvariable", false, this.fullprops);
                    pv.value = workflowVariableid;
                    sb.append(ve.getEditor("variable" + i + "_settoworkflowvariable", pv, null, false, attributes, this.pageContext, false));
                    sb.append("</td>");
                }
                sb.append("</td>");
                sb.append("</tr>");
                found = true;
            }
            if (found) {
                html.append("<form name=\"workflow_props_setup\" action=\"#\">");
                html.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"maintform_table\">");
                html.append("<thead>");
                html.append("<tr class=\"gridmaint_tablehead\">");
                html.append("<th class=\"\">").append("&nbsp;").append("</th>");
                html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Description")).append("</th>");
                if (!this.viewonly) {
                    html.append("<th class=\"gridmaint_fieldtitle\">").append(this.getTranslationProcessor().translate("Set To Workflow Variable")).append("</th>");
                }
                html.append("</thead>");
                html.append("<tbody>");
                html.append(sb);
                html.append("</tbody>");
                html.append("</table>");
                html.append(hid);
                html.append("</form>");
            } else {
                html.append("No Setup Variables");
            }
        } else {
            html.append("No Setup Variables");
        }
        html.append("</div>");
        return html;
    }

    private StringBuffer getTaskWorkTab() {
        StringBuffer html = new StringBuffer();
        DataSet dsFake = new DataSet();
        dsFake.addColumn("assignmentflag", 0);
        dsFake.addColumn("sysuserid", 0);
        dsFake.addColumn("roleid", 0);
        dsFake.addColumn("departmentid", 0);
        String assignmentflag = this.taskprops.getProperty("assignmentflag", "I");
        String user = this.taskprops.getProperty("sysuserid", "");
        String role = this.taskprops.getProperty("roleid", "");
        String department = this.taskprops.getProperty("departmentid", "");
        dsFake.addRow();
        String taskid = this.taskprops.getProperty("taskdefitemid", "");
        dsFake.setValue(0, "assignmentflag", assignmentflag);
        dsFake.setValue(0, "sysuserid", user);
        dsFake.setValue(0, "roleid", role);
        dsFake.setValue(0, "departmentid", department);
        DataView maint = new DataView(this.pageContext, "primary", dsFake, "", this.getConnectionId());
        maint.setElementid("task_work_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "Form");
        maintProps.setProperty("formcols", "2");
        if (this.viewonly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        PropertyList event = new PropertyList();
        event.setProperty("event", "onchange");
        event.setProperty("js", "workflowProps.maintFieldChange(event,this,'task','" + taskid + "','')");
        events.add(event);
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "assignmentflag");
        column.setProperty("title", "Assignment");
        column.setProperty("mode", !this.viewonly ? "dropdownlist" : "readonly");
        column.setProperty("displayvalue", "I=Inherit;U=User;D=Department;R=Role;A=All;M=Manual;C=Current User;T=Current Department");
        column.setProperty("groupid", "Assignment");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "sysuserid");
        column.setProperty("title", "User");
        column.setProperty("mode", !this.viewonly ? "lookup" : "readonly");
        PropertyList lookup = new PropertyList();
        lookup.setProperty("sdcid", "User");
        lookup.setProperty("tip", "Lookup User");
        column.setProperty("lookuplink", lookup);
        if (!assignmentflag.equalsIgnoreCase("U")) {
            column.setProperty("disable", "Y");
        }
        column.setProperty("groupid", "Assignment");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "roleid");
        column.setProperty("title", "Role");
        column.setProperty("mode", !this.viewonly ? "lookup" : "readonly");
        lookup = new PropertyList();
        lookup.setProperty("sdcid", "Role");
        lookup.setProperty("tip", "Lookup Role");
        column.setProperty("lookuplink", lookup);
        if (!assignmentflag.equalsIgnoreCase("R")) {
            column.setProperty("disable", "Y");
        }
        column.setProperty("groupid", "Assignment");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "departmentid");
        column.setProperty("title", "Department");
        column.setProperty("mode", !this.viewonly ? "lookup" : "readonly");
        lookup = new PropertyList();
        lookup.setProperty("sdcid", "Department");
        lookup.setProperty("tip", "Lookup Department");
        column.setProperty("lookuplink", lookup);
        if (!assignmentflag.equalsIgnoreCase("D")) {
            column.setProperty("disable", "Y");
        }
        column.setProperty("groupid", "Assignment");
        column.setProperty("events", events);
        columns.add(column);
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append("<div style=\"padding:5px 5px 5px 5px;\">");
        html.append(maint.getHtml());
        html.append("</div>");
        return html;
    }

    private StringBuffer getIOIOTab() {
        StringBuffer html = new StringBuffer();
        PropertyList taskio = null;
        try {
            TaskDef taskDef = TaskDef.getInstance(this.getConnectionProcessor().getSapphireConnection(), this.taskprops.getProperty("taskdefid"), this.taskprops.getProperty("taskdefversionid"), this.taskprops.getProperty("taskdefvariantid"));
            PropertyListCollection taskios = taskDef.getTaskdef().getCollection("taskio");
            taskio = taskios.find("ioid", this.ioprops.getProperty("ioid"));
        }
        catch (SapphireException taskDef) {
            // empty catch block
        }
        if (this.taskprops.getPropertyList("event") == null) {
            this.taskprops.setProperty("event", new PropertyList());
        }
        DataSet dsFake = new DataSet();
        dsFake.addColumn("waittype", 0);
        dsFake.addColumn("eventbypass", 0);
        dsFake.addRow();
        String waittype = taskio != null ? taskio.getProperty("waittype", "none") : "";
        dsFake.setValue(0, "waittype", waittype);
        PropertyList eventdefProps = new PropertyList();
        eventdefProps.setProperty("readonly", "Y");
        PropertyList ioevent = taskio.getPropertyList("event");
        if (waittype.equals("event") && ioevent != null) {
            eventdefProps.setProperty("eventtypeid", ioevent.getProperty("eventtypeid"));
            eventdefProps.setProperty("conditions", ioevent.getCollection("conditions"));
            dsFake.setValue(0, "eventbypass", ioevent.getProperty("eventbypass"));
        }
        DataView maint = new DataView(this.pageContext, "primary", dsFake, "", this.getConnectionId());
        maint.setElementid("task_io_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "Form");
        maintProps.setProperty("formcols", "2");
        if (this.viewonly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        PropertyList event = new PropertyList();
        event.setProperty("event", "onchange");
        event.setProperty("js", "workflowProps.maintFieldChange(event,this,'io','" + this.taskprops.getProperty("taskdefitemid", "") + "','" + this.ioprops.getProperty("ioid") + "')");
        events.add(event);
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "waittype");
        column.setProperty("title", "Wait Type");
        column.setProperty("mode", "readonly");
        column.setProperty("displayvalue", "none=No Wait;event=Based on an Event");
        column.setProperty("events", events);
        columns.add(column);
        if (waittype.equals("event")) {
            column = new PropertyList();
            column.setProperty("columnid", "eventdef");
            column.setProperty("title", "Event Definition");
            column.setProperty("mode", "");
            column.setProperty("colspan", "2");
            column.setProperty("class", "task_longtext");
            column.setProperty("pseudocolumn", "<div id=\"eventdef\"></div>");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "eventbypass");
            column.setProperty("title", "Event Bypass");
            column.setProperty("mode", "readonly");
            column.setProperty("colspan", "2");
            column.setProperty("class", "task_longtext");
            column.setProperty("events", events);
            columns.add(column);
        }
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append("<div style=\"padding:5px 5px 5px 5px;\">");
        html.append(maint.getHtml());
        html.append("<script>");
        html.append("sapphire.gwt.addGWTElement( 'eventdef', 'eventdef', ").append(eventdefProps.toJSONString(false)).append(" );");
        html.append("</script>");
        html.append("</div>");
        return html;
    }

    private Tab getTaskTab(TaskTabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        if (this.prototypeTask) {
            switch (currentTab) {
                case TASK: {
                    html.append(this.getTaskTaskTab());
                    break;
                }
                case INPUTS: {
                    html.append(this.getTaskIOTab(false));
                    break;
                }
                case OUTPUTS: {
                    html.append(this.getTaskIOTab(true));
                }
            }
        } else {
            switch (currentTab) {
                case TASK: {
                    html.append(this.getTaskTaskTab());
                    break;
                }
                case SETUP: {
                    html.append(this.getTaskSetupTab());
                    break;
                }
                case VARIABLES: {
                    html.append(this.getTaskTaskVariablesTab());
                    break;
                }
                case INPUTS: {
                    html.append(this.getTaskInputsTab());
                    break;
                }
                case WORK: {
                    html.append(this.getTaskWorkTab());
                }
            }
        }
        tab.setContent(html.toString());
        tab.setId("workflow_tab_task_" + currentTab.toString().toLowerCase());
        if (currentTab == TaskTabs.TASK) {
            String title = this.taskprops.getProperty("taskdefid") + " (" + this.taskprops.getProperty("taskdefversionid") + " - " + this.taskprops.getProperty("taskdefvariantid") + ") Details";
            tab.setText(title);
            tab.setTip(title);
        } else {
            tab.setText(currentTab.getTitle());
            tab.setTip(currentTab.getTitle());
        }
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setAction("workflowProps.changeTab('task','" + currentTab.toString().toLowerCase() + "')");
        return tab;
    }

    private Tab getElementTab(ElementTabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        switch (currentTab) {
            case ELEMENT: {
                html.append(this.getElementElementTab());
                tab.setText(currentTab.getTitle(this.elementType));
            }
        }
        tab.setContent(html.toString());
        tab.setId("workflow_tab_element_" + currentTab.toString().toLowerCase());
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setTip(currentTab.getTitle());
        tab.setAction("workflowProps.changeTab('element','" + currentTab.toString().toLowerCase() + "')");
        return tab;
    }

    private Tab getIOTab(IOTabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        switch (currentTab) {
            case IO: {
                html.append(this.getIOIOTab());
            }
        }
        tab.setContent(html.toString());
        tab.setId("workflow_tab_io_" + currentTab.toString().toLowerCase());
        tab.setText(currentTab.getTitle());
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setTip(currentTab.getTitle());
        tab.setAction("workflowProps.changeTab('io','" + currentTab.toString().toLowerCase() + "')");
        return tab;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append(this.getScriptAndStyle());
        TranslationProcessor tp = this.getTranslationProcessor();
        html.append("<div style=\"padding:5px 5px 5px 5px;\">");
        if (this.fullprops == null) {
            this.type = null;
        } else if (this.type == ItemType.TASK && (this.sourceTask.length() == 0 || this.taskprops == null)) {
            this.type = null;
        } else if (this.type == ItemType.ELEMENT && (this.sourceTask.length() == 0 || this.elementprops == null)) {
            this.type = null;
        } else if (this.type == ItemType.IO && this.ioprops == null) {
            this.type = null;
        }
        if (this.type == ItemType.TASK) {
            TabGroup tabGroup = new TabGroup();
            tabGroup.setPageContext(this.pageContext);
            tabGroup.setMultiTab(true);
            tabGroup.setId("task_props_task_tabgroup");
            tabGroup.setBodyheight("100%");
            tabGroup.setAppearance("modern");
            tabGroup.setContext("LV_WorkflowDef_props_task");
            for (TaskTabs tab : TaskTabs.values()) {
                Tab t = this.getTaskTab(tab);
                if (t == null || t.getContent().length() <= 0) continue;
                tabGroup.setTab(t);
            }
            tabGroup.setUseChangeTab(true);
            html.append(tabGroup.getHtml());
        } else if (this.type == ItemType.IO) {
            TabGroup tabGroup = new TabGroup();
            tabGroup.setPageContext(this.pageContext);
            tabGroup.setMultiTab(true);
            tabGroup.setId("task_props_io_tabgroup");
            tabGroup.setBodyheight("100%");
            tabGroup.setAppearance("modern");
            tabGroup.setContext("LV_WorkflowDef__props_io");
            for (IOTabs tab : IOTabs.values()) {
                tabGroup.setTab(this.getIOTab(tab));
            }
            tabGroup.setUseChangeTab(true);
            html.append(tabGroup.getHtml());
        } else if (this.type == ItemType.ELEMENT) {
            TabGroup tabGroup = new TabGroup();
            tabGroup.setPageContext(this.pageContext);
            tabGroup.setMultiTab(true);
            tabGroup.setId("task_props_element_tabgroup");
            tabGroup.setBodyheight("100%");
            tabGroup.setAppearance("modern");
            tabGroup.setContext("LV_WorkflowDef__props_element");
            try {
                this.elementType = WorkflowDefPainter.Tools.valueOf(this.elementprops.getProperty(PROPERTY_TYPE, WorkflowDefPainter.Tools.LABEL.toString()).toUpperCase());
            }
            catch (Exception e) {
                this.elementType = WorkflowDefPainter.Tools.LABEL;
            }
            if (this.elementType == WorkflowDefPainter.Tools.TASK) {
                for (ElementTabs tab : ElementTabs.values()) {
                    tabGroup.setTab(this.getElementTab(tab));
                }
            } else {
                tabGroup.setTab(this.getElementTab(ElementTabs.ELEMENT));
            }
            tabGroup.setUseChangeTab(true);
            html.append(tabGroup.getHtml());
        } else {
            html.append(this.getTranslationProcessor().translate(tp.translate("No item selected")));
        }
        html.append("</div>");
        html.append(this.getEndScript());
        return html.toString();
    }

    public static enum ItemType {
        IO,
        ELEMENT,
        TASK;

    }

    public static enum IOTabs {
        IO("Input Queue");

        private String title;

        private IOTabs(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }
    }

    public static enum ElementTabs {
        ELEMENT("Element Details");

        private String title;

        private ElementTabs(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }

        public String getTitle(WorkflowDefPainter.Tools elementType) {
            switch (elementType) {
                case IMAGE: {
                    return "Image Details";
                }
                case TEXT: {
                    return "Textbox Details";
                }
                case LABEL: {
                    return "Label Details";
                }
                case CIRCLE: 
                case LINE: 
                case BOX: {
                    return "Shape Details";
                }
            }
            return this.getTitle();
        }
    }

    public static enum TaskTabs {
        TASK("Task Details"),
        SETUP("Setup Variables"),
        VARIABLES("Exposed Variables"),
        INPUTS("Input Queues"),
        OUTPUTS("Output Queues"),
        WORK("Assignment");

        private String title;

        private TaskTabs(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }
    }
}

