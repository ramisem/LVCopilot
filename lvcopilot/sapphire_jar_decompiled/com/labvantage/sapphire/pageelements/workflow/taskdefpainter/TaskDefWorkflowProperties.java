/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.admin.propertytree.VariableEditor;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefVariables;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflow;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class TaskDefWorkflowProperties
extends BaseElement {
    public static final String USERCONFIG_PREFIX = "taskprops_";
    public static final String JS_CLASS = "taskWorkflowProps";
    public static final String XMLFILE = "buttons.xml";
    public static final String CACHE = "taskdef_buttons";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_IOID = "ioid";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_DESCENDANT = "descendant";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private boolean descendant = false;
    private boolean devMode;
    private PropertyList fullprops = null;
    private PropertyList ioprops = null;
    private ItemType type = ItemType.TASK;
    private String ioid = "";

    public TaskDefWorkflowProperties(PageContext pageContext, PropertyList pageproperties) {
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
            this.logger.error("Could not set up painter: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        String taskp;
        pagedata.setProperty("jsrequest", "exclude=properties");
        this.ioid = pagedata.getProperty(PROPERTY_IOID, "");
        this.logger.debug("ioid = " + this.ioid);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.logger.debug("viewonly = " + this.viewonly);
        this.descendant = pagedata.getProperty(PROPERTY_DESCENDANT, "n").equalsIgnoreCase("y");
        this.logger.debug("descendant = " + this.descendant);
        try {
            this.type = ItemType.valueOf(pagedata.getProperty(PROPERTY_TYPE, "task").toUpperCase());
        }
        catch (Exception e) {
            this.type = ItemType.TASK;
        }
        this.logger.debug("type = " + this.type.toString());
        if ((this.type != ItemType.IO || this.ioid.length() > 0) && (taskp = pagedata.getProperty(PROPERTY_PROPERTIES, "")).length() > 0) {
            try {
                this.fullprops = new PropertyList(new JSONObject(taskp));
                PropertyListCollection steps = this.fullprops.getCollection("taskio");
                if (steps != null) {
                    this.ioprops = steps.find(PROPERTY_IOID, this.ioid);
                    if (this.ioprops == null) {
                        this.logger.warn("Could not find io " + this.ioid);
                    }
                } else {
                    this.logger.warn("Could not find steps");
                }
            }
            catch (Exception e) {
                this.fullprops = null;
                this.ioprops = null;
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
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/taskworkflowprops.js\"></script>");
        html.append("\n<style>");
        html.append("\n.task_longtext { width:400px; }");
        html.append("\n</style>");
        return html;
    }

    private StringBuffer getEndScript() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        if (this.type != ItemType.IO || this.ioid.length() > 0) {
            // empty if block
        }
        html.append(JS_CLASS).append(".type = '").append(this.type.toString().toLowerCase()).append("';");
        html.append(JS_CLASS).append(".ioId = '").append(this.ioid).append("';");
        html.append(JS_CLASS).append(".descendant = ").append(this.descendant).append(";");
        html.append("</script>");
        return html;
    }

    private static void getIOInputHtml(StringBuffer html, PropertyList fullprops, PropertyList ioprops, boolean viewonly, boolean descendant, PropertyListCollection events, String connectionId, SDIProcessor sdiProcessor, PageContext pageContext) {
        boolean fullViewOnly = viewonly || descendant;
        boolean workflowprops = pageContext.getRequest().getParameter("file") != null ? pageContext.getRequest().getParameter("file").endsWith("workflowproperties.jsp") : false;
        String ioid = ioprops.getProperty(PROPERTY_IOID, "");
        DataSet dsFake = new DataSet();
        dsFake.addColumn(PROPERTY_IOID, 0);
        dsFake.addColumn("iodesc", 0);
        dsFake.addColumn("ioflag", 0);
        dsFake.addColumn("connectortypeid", 0);
        dsFake.addColumn("singleconnect", 0);
        dsFake.addColumn("variableid", 0);
        dsFake.addColumn("lookuppageid", 0);
        dsFake.addColumn("autoselect", 0);
        dsFake.addColumn("selectnum", 0);
        dsFake.addColumn("sort", 0);
        dsFake.addColumn("autoexecprocessing", 0);
        dsFake.addColumn("waittype", 0);
        dsFake.addColumn("eventbypass", 0);
        dsFake.addColumn("waittime", 0);
        dsFake.addColumn("waitunits", 0);
        dsFake.addColumn("exampletaskqueuekeyid1", 0);
        dsFake.addColumn("exampletaskqueuekeyid2", 0);
        dsFake.addColumn("exampletaskqueuekeyid3", 0);
        boolean autoexec = fullprops.getProperty("autoexec", "N").equalsIgnoreCase("Y");
        if (autoexec || ioprops.getProperty("autoselect", "").length() == 0) {
            ioprops.setProperty("autoselect", autoexec ? "Y" : "N");
        }
        if (autoexec && ioprops.getProperty("autoexecprocessing", "").length() == 0) {
            ioprops.setProperty("autoexecprocessing", "I");
        }
        String connectorTypeId = ioprops.getProperty("connectortypeid", "");
        dsFake.addRow();
        dsFake.setValue(0, PROPERTY_IOID, ioprops.getProperty(PROPERTY_IOID, ""));
        dsFake.setValue(0, "iodesc", ioprops.getProperty("iodesc", ""));
        dsFake.setValue(0, "ioflag", ioprops.getProperty("ioflag", ""));
        dsFake.setValue(0, "connectortypeid", connectorTypeId);
        dsFake.setValue(0, "singleconnect", ioprops.getProperty("singleconnect", "N"));
        dsFake.setValue(0, "variableid", ioprops.getProperty("variableid", ""));
        dsFake.setValue(0, "lookuppageid", ioprops.getProperty("lookuppageid", ""));
        dsFake.setValue(0, "autoselect", ioprops.getProperty("autoselect", autoexec ? "Y" : "N"));
        dsFake.setValue(0, "selectnum", ioprops.getProperty("selectnum", ""));
        dsFake.setValue(0, "sort", ioprops.getProperty("sort", ""));
        dsFake.setValue(0, "autoexecprocessing", ioprops.getProperty("autoexecprocessing", "I"));
        dsFake.setValue(0, "waittype", ioprops.getProperty("waittype", "none"));
        PropertyList eventdefProps = new PropertyList();
        eventdefProps.setProperty("readonly", fullViewOnly ? "Y" : "N");
        PropertyList event = ioprops.getPropertyList("event");
        if (ioprops.getProperty("waittype").equals("event") && event != null) {
            eventdefProps.setProperty("eventtypeid", event.getProperty("eventtypeid"));
            eventdefProps.setProperty("conditions", event.getCollection("conditions"));
            dsFake.setValue(0, "eventbypass", event.getProperty("eventbypass"));
        }
        dsFake.setValue(0, "waittime", ioprops.getProperty("waittime"));
        dsFake.setValue(0, "waitunits", ioprops.getProperty("waitunits"));
        dsFake.setValue(0, "exampletaskqueuekeyid1", ioprops.getProperty("exampletaskqueuekeyid1", ""));
        dsFake.setValue(0, "exampletaskqueuekeyid2", ioprops.getProperty("exampletaskqueuekeyid2", ""));
        dsFake.setValue(0, "exampletaskqueuekeyid3", ioprops.getProperty("exampletaskqueuekeyid3", ""));
        DataView maint = new DataView(pageContext, "primary", dsFake, "", connectionId);
        maint.setElementid("task_workflow_io_pri_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "FormWithFieldGroups");
        maintProps.setProperty("formcols", "2");
        if (viewonly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        String group = "Input Queue (" + ioprops.getProperty(PROPERTY_IOID, "") + ")";
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "iodesc");
        column.setProperty("title", "Title");
        column.setProperty("mode", !fullViewOnly ? "input" : "readonly");
        column.setProperty("events", events);
        column.setProperty("class", "task_longtext");
        column.setProperty("colspan", "1");
        column.setProperty("groupid", group);
        columns.add(column);
        StringBuffer vals = new StringBuffer();
        String selectedSDCId = TaskDefWorkflow.getConnectorsDisplayValue(vals, connectorTypeId, sdiProcessor);
        column = new PropertyList();
        column.setProperty("columnid", "connectortypeid");
        column.setProperty("title", "Connector");
        column.setProperty("mode", !fullViewOnly ? "dropdownlist" : "readonly");
        column.setProperty("displayvalue", vals.toString());
        column.setProperty("events", events);
        column.setProperty("groupid", group);
        column.setProperty("class", "mandatoryfield");
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "lookuppageid");
        column.setProperty("title", "Lookup Page");
        column.setProperty("mode", !fullViewOnly ? "lookup" : "readonly");
        column.setProperty("events", events);
        column.setProperty("groupid", group);
        PropertyList lookup = new PropertyList();
        lookup = new PropertyList();
        lookup.setProperty("href", "javascript:sapphire.page.getTop().taskMaint.lookupLink(this)");
        lookup.setProperty("tip", "Lookup Link");
        column.setProperty("lookuplink", lookup);
        column.setProperty("class", "task_longtext");
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "singleconnect");
        column.setProperty("title", "Connections");
        column.setProperty("mode", !fullViewOnly ? "radiobutton/horizontal" : "readonly");
        column.setProperty("displayvalue", "N=Multiple Connections;Y=Single Connection");
        column.setProperty("events", events);
        column.setProperty("groupid", group);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "autoselect");
        column.setProperty("title", "Selection");
        column.setProperty("mode", !fullViewOnly ? "radiobutton/horizontal" : "readonly");
        column.setProperty("displayvalue", "Y=Auto Select;N=Manual Select");
        if (autoexec) {
            column.setProperty("disable", "Y");
        }
        column.setProperty("events", events);
        column.setProperty("groupid", group);
        columns.add(column);
        boolean autoselect = ioprops.getProperty("autoselect", "Y").equalsIgnoreCase("Y");
        if (autoselect) {
            if (!autoexec) {
                column = new PropertyList();
                column.setProperty("columnid", "selectnum");
                column.setProperty("title", "Selection Number");
                column.setProperty("mode", !fullViewOnly ? "input" : "readonly");
                column.setProperty("events", events);
                column.setProperty("groupid", "Input Selection");
                columns.add(column);
            } else {
                column = new PropertyList();
                column.setProperty("columnid", "autoexecprocessing");
                column.setProperty("title", "Auto Execution Processing");
                column.setProperty("mode", !fullViewOnly ? "radiobutton/horizontal" : "readonly");
                column.setProperty("displayvalue", "I=Individually;A=All Together");
                column.setProperty("events", events);
                column.setProperty("groupid", "Input Selection");
                columns.add(column);
            }
            column = new PropertyList();
            column.setProperty("columnid", "__variableid");
            column.setProperty("title", "Load to Variable");
            column.setProperty("mode", !fullViewOnly ? "" : "readonly");
            VariableEditor ve = new VariableEditor();
            HashMap<String, String> attributes = new HashMap<String, String>();
            attributes.put("values", TaskDefVariables.getVariablesList(fullprops, false, false, false, new String[]{"", "SDIList"}, null));
            attributes.put("sdcid", selectedSDCId);
            attributes.put("readonly", "N");
            attributes.put("subvariables", "N");
            attributes.put("rootvariables", "Y");
            attributes.put("validtypes", "sdilist");
            attributes.put("customonchange", (workflowprops ? JS_CLASS : "taskProps") + ".maintFieldChange(event,this,'io','" + ioid + "')");
            PropertyValue pv = new PropertyValue("variableid", false, ioprops);
            pv.value = ioprops.getProperty("variableid", "");
            if (fullViewOnly) {
                column.setProperty("pseudocolumn", ioprops.getProperty("variableid", ""));
            } else {
                column.setProperty("pseudocolumn", ve.getEditor("pr0_variableid", pv, null, false, attributes, pageContext, false));
            }
            column.setProperty("groupid", "Input Selection");
            columns.add(column);
        }
        if (ioprops.getProperty("waittype").length() == 0) {
            ioprops.setProperty("waittype", "none");
        }
        column = new PropertyList();
        column.setProperty("columnid", "waittype");
        column.setProperty("title", "Wait Type");
        column.setProperty("mode", !fullViewOnly ? "radiobutton/horizontal" : "readonly");
        column.setProperty("colspan", "2");
        column.setProperty("displayvalue", "none=No Wait;event=Based on an Event;timer=Based on a Timer");
        column.setProperty("events", events);
        column.setProperty("defaultvalue", "none");
        column.setProperty("class", "task_longtext");
        column.setProperty("groupid", "Wait Conditions");
        columns.add(column);
        if (ioprops.getProperty("waittype").equals("event")) {
            column = new PropertyList();
            column.setProperty("columnid", "eventdef");
            column.setProperty("title", "Event Definition");
            column.setProperty("mode", "");
            column.setProperty("colspan", "2");
            column.setProperty("class", "task_longtext");
            column.setProperty("pseudocolumn", "<div id=\"eventdef\"></div>");
            column.setProperty("groupid", "Wait Conditions");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "eventbypass");
            column.setProperty("title", "Event Bypass");
            column.setProperty("mode", !fullViewOnly ? "inputarea" : "readonly");
            lookup = new PropertyList();
            lookup.setProperty("href", "javascript:" + (workflowprops ? JS_CLASS : "taskProps") + ".lookupGroovy(this,'" + column.getProperty("columnid") + "')");
            lookup.setProperty("tip", "Edit Script");
            lookup.setProperty("img", "WEB-CORE/elements/images/ellipsisblank.gif");
            lookup.setProperty("style", "button");
            column.setProperty("lookuplink", lookup);
            column.setProperty("events", events);
            column.setProperty("colspan", "2");
            column.setProperty("class", "task_longtext");
            column.setProperty("groupid", "Wait Conditions");
            columns.add(column);
        } else if (ioprops.getProperty("waittype").equals("timer")) {
            column = new PropertyList();
            column.setProperty("columnid", "waittime");
            column.setProperty("title", "Wait Time");
            column.setProperty("mode", !fullViewOnly ? "input" : "readonly");
            column.setProperty("events", events);
            column.setProperty("defaultvalue", "");
            column.setProperty("class", "task_longtext");
            column.setProperty("groupid", "Wait Conditions");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "waitunits");
            column.setProperty("title", "Wait Units");
            column.setProperty("mode", !fullViewOnly ? "dropdownlist" : "readonly");
            column.setProperty("displayvalue", "M=Minutes;H=Hours;D=Days");
            column.setProperty("events", events);
            column.setProperty("defaultvalue", "");
            column.setProperty("class", "task_longtext");
            column.setProperty("groupid", "Wait Conditions");
            columns.add(column);
        }
        String connector = ioprops.getProperty("connectortypeid", "");
        if (connector.length() > 0) {
            String keycount;
            int keycols = 3;
            if (selectedSDCId.length() > 0 && (keycount = new SDCProcessor(connectionId).getProperty(selectedSDCId, "keycolumns", keycols + "")).length() > 0) {
                try {
                    keycols = Integer.parseInt(keycount);
                }
                catch (Exception e) {
                    keycols = 3;
                }
            }
            column = new PropertyList();
            column.setProperty("columnid", "exampletaskqueuekeyid1");
            column.setProperty("title", "Example KeyId1");
            column.setProperty("mode", !viewonly ? "inputarea" : "readonly");
            lookup = new PropertyList();
            if (ioprops.getProperty("lookuppageid").length() > 0) {
                lookup.setProperty("href", ioprops.getProperty("lookuppageid"));
            } else {
                lookup.setProperty("sdcid", selectedSDCId);
            }
            lookup.setProperty("selectortype", "Checkbox");
            lookup.setProperty("tip", "Lookup Example");
            column.setProperty("lookuplink", lookup);
            column.setProperty("events", events);
            column.setProperty("class", "task_longtext");
            column.setProperty("groupid", "Example Input");
            columns.add(column);
            if (keycols > 1) {
                column = new PropertyList();
                column.setProperty("columnid", "exampletaskqueuekeyid2");
                column.setProperty("title", "Example KeyId2");
                column.setProperty("mode", !viewonly ? "inputarea" : "readonly");
                column.setProperty("events", events);
                column.setProperty("class", "task_longtext");
                column.setProperty("groupid", "Example Input");
                columns.add(column);
                if (keycols > 2) {
                    column = new PropertyList();
                    column.setProperty("columnid", "exampletaskqueuekeyid3");
                    column.setProperty("title", "Example KeyId3");
                    column.setProperty("mode", !viewonly ? "inputarea" : "readonly");
                    column.setProperty("events", events);
                    column.setProperty("class", "task_longtext");
                    column.setProperty("groupid", "Example Input");
                    columns.add(column);
                }
            }
        }
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        Browser browser = new Browser(pageContext);
        if (browser.isIE()) {
            html.append("<div style=\"padding: 0 0 0 0;\">");
        } else {
            html.append("<div style=\"padding: 5px 5px 5px 5px;\">");
        }
        html.append(maint.getHtml());
        html.append("<script>");
        html.append("sapphire.gwt.addGWTElement( 'eventdef', 'eventdef', ").append(eventdefProps.toJSONString(false)).append(" );");
        html.append("</script>");
        html.append("</div>");
    }

    private static void getIOOutputHtml(StringBuffer html, PropertyList fullprops, PropertyList ioprops, boolean viewonly, boolean descendant, PropertyListCollection events, String connectionId, SDIProcessor sdiProcessor, PageContext pageContext) {
        boolean workflowprops = pageContext.getRequest().getParameter("file") != null ? pageContext.getRequest().getParameter("file").endsWith("workflowproperties.jsp") : false;
        boolean fullViewOnly = viewonly || descendant;
        String ioid = ioprops.getProperty(PROPERTY_IOID, "");
        DataSet dsFake = new DataSet();
        dsFake.addColumn(PROPERTY_IOID, 0);
        dsFake.addColumn("iodesc", 0);
        dsFake.addColumn("ioflag", 0);
        dsFake.addColumn("connectortypeid", 0);
        dsFake.addColumn("variableid", 0);
        dsFake.addColumn("variableid2", 0);
        dsFake.addColumn("variableid3", 0);
        dsFake.addColumn("inputioid", 0);
        dsFake.addColumn("singleconnect", 0);
        dsFake.addColumn("outputtypeflag", 0);
        String connectorTypeId = ioprops.getProperty("connectortypeid", "");
        dsFake.addRow();
        dsFake.setValue(0, PROPERTY_IOID, ioprops.getProperty(PROPERTY_IOID, ""));
        dsFake.setValue(0, "iodesc", ioprops.getProperty("iodesc", ""));
        dsFake.setValue(0, "ioflag", ioprops.getProperty("ioflag", ""));
        dsFake.setValue(0, "connectortypeid", connectorTypeId);
        dsFake.setValue(0, "variableid", ioprops.getProperty("variableid", ""));
        dsFake.setValue(0, "variableid2", ioprops.getProperty("variableid2", ""));
        dsFake.setValue(0, "variableid3", ioprops.getProperty("variableid3", ""));
        dsFake.setValue(0, "singleconnect", ioprops.getProperty("singleconnect", "N"));
        dsFake.setValue(0, "outputtypeflag", ioprops.getProperty("outputtypeflag", ""));
        dsFake.setValue(0, "inputioid", ioprops.getProperty("inputioid", ""));
        DataView maint = new DataView(pageContext, "primary", dsFake, "", connectionId);
        maint.setElementid("task_workflow_io_pri_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "Form");
        maintProps.setProperty("formcols", "2");
        if (viewonly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        String group = "Output Queue";
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "iodesc");
        column.setProperty("title", "Title");
        column.setProperty("mode", !fullViewOnly ? "input" : "readonly");
        column.setProperty("events", events);
        column.setProperty("class", "task_longtext");
        column.setProperty("groupid", group);
        columns.add(column);
        String selectedSDCId = "";
        StringBuffer vals = new StringBuffer();
        try {
            PropertyListCollection connectors = TaskDefWorkflow.getConnectors(sdiProcessor);
            if (connectors != null) {
                for (int i = 0; i < connectors.size(); ++i) {
                    PropertyList connector = connectors.getPropertyList(i);
                    if (vals.length() > 0) {
                        vals.append(";");
                    }
                    String con = connector.getProperty("connectortypeid", connector.getProperty("connectortypeid", connector.getId()));
                    vals.append(con).append("=").append(connector.getProperty("title", connector.getProperty("sdcid", con)));
                    if (!connector.getProperty("connectortypeid").equalsIgnoreCase(connectorTypeId)) continue;
                    selectedSDCId = connector.getProperty("sdcid", "");
                }
            }
        }
        catch (Exception connectors) {
            // empty catch block
        }
        column = new PropertyList();
        column.setProperty("columnid", "connectortypeid");
        column.setProperty("title", "Connector");
        column.setProperty("mode", !fullViewOnly ? "dropdownlist" : "readonly");
        column.setProperty("displayvalue", vals.toString());
        column.setProperty("events", events);
        column.setProperty("groupid", group);
        column.setProperty("class", "mandatoryfield");
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "outputtypeflag");
        column.setProperty("title", "Type");
        column.setProperty("mode", !fullViewOnly ? "dropdownlist" : "readonly");
        column.setProperty("displayvalue", "F=Full Pass Through;I=Itemized Pass Through;G=Generated Items");
        column.setProperty("events", events);
        column.setProperty("groupid", group);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "singleconnect");
        column.setProperty("title", "Connections");
        column.setProperty("mode", !fullViewOnly ? "radiobutton/horizontal" : "readonly");
        column.setProperty("displayvalue", "N=Multiple Connections;Y=Single Connection");
        column.setProperty("events", events);
        column.setProperty("groupid", group);
        columns.add(column);
        if (ioprops.getProperty("outputtypeflag", "").equalsIgnoreCase("F")) {
            column = new PropertyList();
            column.setProperty("columnid", "inputioid");
            column.setProperty("title", "Input Id");
            column.setProperty("events", events);
            PropertyListCollection ios = fullprops.getCollection("taskio");
            StringBuffer dv = new StringBuffer();
            for (int i = 0; i < ios.size(); ++i) {
                PropertyList io = ios.getPropertyList(i);
                if (!io.getProperty("ioflag", "O").equalsIgnoreCase("I")) continue;
                if (dv.length() > 0) {
                    dv.append(";");
                }
                dv.append(io.getProperty(PROPERTY_IOID, "")).append("=").append(io.getProperty("iodesc", io.getProperty(PROPERTY_IOID, "")));
            }
            column.setProperty("mode", dv.length() > 0 && !fullViewOnly ? "dropdownlist" : "input");
            column.setProperty("disable", dv.length() > 0 && !fullViewOnly ? "N" : "Y");
            if (dv.length() > 0) {
                column.setProperty("displayvalue", dv.toString());
            }
            column.setProperty("groupid", group);
            columns.add(column);
        } else {
            PropertyListCollection vars;
            int varfields = 1;
            String variable = ioprops.getProperty("variableid");
            if (variable.length() > 0 && (vars = fullprops.getCollection("variables")) != null) {
                PropertyList var;
                String subvariable = "";
                int i = variable.indexOf(".");
                if (i > -1) {
                    subvariable = variable.substring(i + 1);
                    variable = variable.substring(0, i);
                }
                if ((var = vars.find("variableid", variable)) != null) {
                    String keycount;
                    String vartype;
                    String string = vartype = subvariable.length() == 0 ? var.getProperty(PROPERTY_TYPE, "") : "string";
                    if (!vartype.equalsIgnoreCase("sdilist") && selectedSDCId.length() > 0 && (keycount = new SDCProcessor(connectionId).getProperty(selectedSDCId, "keycolumns", varfields + "")).length() > 0) {
                        try {
                            varfields = Integer.parseInt(keycount);
                        }
                        catch (Exception e) {
                            varfields = 1;
                        }
                    }
                }
            }
            String variablesList = TaskDefVariables.getVariablesList(fullprops, true, false, true, null, null);
            column = new PropertyList();
            column.setProperty("columnid", "__variableid");
            column.setProperty("title", "Variable");
            column.setProperty("mode", !fullViewOnly ? "" : "readonly");
            VariableEditor ve = new VariableEditor();
            HashMap<String, String> attributes = new HashMap<String, String>();
            attributes.put("values", variablesList);
            attributes.put("subvariables", "Y");
            attributes.put("readonly", "N");
            attributes.put("rootvariables", "Y");
            attributes.put("validtypes", "sdilist;string");
            attributes.put("sdcid", selectedSDCId);
            attributes.put("customonchange", (workflowprops ? JS_CLASS : "taskProps") + ".maintFieldChange(event,this,'io','" + ioid + "')");
            PropertyValue pv = new PropertyValue("variableid", false, ioprops);
            pv.value = ioprops.getProperty("variableid", "");
            if (fullViewOnly) {
                column.setProperty("pseudocolumn", ioprops.getProperty("variableid", ""));
            } else {
                column.setProperty("pseudocolumn", ve.getEditor("pr0_variableid", pv, null, false, attributes, pageContext, false));
            }
            column.setProperty("groupid", group);
            columns.add(column);
            if (varfields > 1) {
                column = new PropertyList();
                column.setProperty("columnid", "__variableid2");
                column.setProperty("title", "Variable KeyId2");
                column.setProperty("mode", !fullViewOnly ? "" : "readonly");
                ve = new VariableEditor();
                attributes = new HashMap();
                attributes.put("values", variablesList);
                attributes.put("subvariables", "Y");
                attributes.put("readonly", "N");
                attributes.put("rootvariables", "Y");
                attributes.put("validtypes", "string");
                attributes.put("customonchange", (workflowprops ? JS_CLASS : "taskProps") + ".maintFieldChange(event,this,'io','" + ioid + "')");
                pv = new PropertyValue("variableid2", false, ioprops);
                pv.value = ioprops.getProperty("variableid2", "");
                column.setProperty("pseudocolumn", ve.getEditor("pr0_variableid2", pv, null, false, attributes, pageContext, false));
                column.setProperty("groupid", group);
                columns.add(column);
                if (varfields > 2) {
                    column = new PropertyList();
                    column.setProperty("columnid", "__variableid3");
                    column.setProperty("title", "Variable KeyId3");
                    column.setProperty("mode", !fullViewOnly ? "" : "readonly");
                    ve = new VariableEditor();
                    attributes = new HashMap();
                    attributes.put("values", variablesList);
                    attributes.put("subvariables", "Y");
                    attributes.put("rootvariables", "Y");
                    attributes.put("validtypes", "string");
                    attributes.put("customonchange", (workflowprops ? JS_CLASS : "taskProps") + ".maintFieldChange(event,this,'io','" + ioid + "')");
                    pv = new PropertyValue("variableid3", false, ioprops);
                    pv.value = ioprops.getProperty("variableid3", "");
                    column.setProperty("pseudocolumn", ve.getEditor("pr0_variableid3", pv, null, false, attributes, pageContext, false));
                    column.setProperty("groupid", group);
                    columns.add(column);
                }
            }
        }
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        Browser browser = new Browser(pageContext);
        if (browser.isIE()) {
            html.append("<div style=\"padding: 0 0 0 0;\">");
        } else {
            html.append("<div style=\"padding: 5px 5px 5px 5px;\">");
        }
        html.append(maint.getHtml());
        html.append("</div>");
    }

    protected static String getIOHtml(PropertyList fullprops, PropertyList ioprops, boolean viewonly, boolean descendant, PropertyListCollection events, String connectionId, SDIProcessor sdiProcessor, PageContext pageContext) {
        StringBuffer html = new StringBuffer();
        boolean input = ioprops.getProperty("ioflag", "o").equalsIgnoreCase("i");
        if (input) {
            TaskDefWorkflowProperties.getIOInputHtml(html, fullprops, ioprops, viewonly, descendant, events, connectionId, sdiProcessor, pageContext);
        } else {
            TaskDefWorkflowProperties.getIOOutputHtml(html, fullprops, ioprops, viewonly, descendant, events, connectionId, sdiProcessor, pageContext);
        }
        return html.toString();
    }

    private StringBuffer getTaskHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        DataSet dsFake = new DataSet();
        dsFake.addColumn("shorttitle", 0);
        dsFake.addColumn("icon", 0);
        dsFake.addColumn("taskcolor1", 0);
        dsFake.addColumn("taskcolor2", 0);
        dsFake.addColumn("taskcolor3", 0);
        dsFake.addColumn("appearance", 0);
        dsFake.addColumn("groupid", 0);
        String color1 = this.fullprops.getProperty("taskcolor1", "");
        String color2 = this.fullprops.getProperty("taskcolor2", "");
        String color3 = this.fullprops.getProperty("taskcolor3", "");
        dsFake.addRow();
        dsFake.setValue(0, "shorttitle", this.fullprops.getProperty("shorttitle", ""));
        dsFake.setValue(0, "icon", this.fullprops.getProperty("icon", ""));
        dsFake.setValue(0, "taskcolor1", color1);
        dsFake.setValue(0, "taskcolor2", color2);
        dsFake.setValue(0, "taskcolor3", color3);
        dsFake.setValue(0, "appearance", this.fullprops.getProperty("appearance", ""));
        dsFake.setValue(0, "groupid", this.fullprops.getProperty("groupid", ""));
        DataView maint = new DataView(this.pageContext, "primary", dsFake, "", this.getConnectionId());
        maint.setElementid("task_workflow_task_pri_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "FormWithFieldGroups");
        maintProps.setProperty("formcols", "3");
        if (this.viewonly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        if (!this.viewonly) {
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "taskWorkflowProps.maintFieldChange(event,this, 'task','')");
            events.add(event);
        }
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "shorttitle");
        column.setProperty("title", tp != null ? tp.translate("Icon Text") : "Icon Text");
        column.setProperty("mode", !this.viewonly ? "input" : "readonly");
        column.setProperty("class", "task_longtext");
        column.setProperty("events", events);
        column.setProperty("groupid", "Title &amp; Image");
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "icon");
        column.setProperty("title", tp != null ? tp.translate("Icon") : "Icon");
        column.setProperty("mode", !this.viewonly ? "icon" : "readonly");
        column.setProperty("events", events);
        column.setProperty("class", "task_longtext");
        column.setProperty("groupid", "Title &amp; Image");
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "appearance");
        column.setProperty("title", tp != null ? tp.translate("Appearance") : "Appearance");
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
        column = new PropertyList();
        column.setProperty("columnid", "_color");
        column.setProperty("title", tp != null ? tp.translate("Task Color") : "Task Color");
        column.setProperty("mode", "");
        StringBuffer colorHtml = new StringBuffer();
        colorHtml.append("<div style=\"display:inline").append(this.browser.isIE() ? "" : "-block").append(";height:20px;top:").append(this.browser.isIE() ? "0" : "3px").append(";position:relative;border:solid gray 1px;\">");
        colorHtml.append("<div id=\"_taskcolor1\" _default=\"").append("#e7f2ff").append("\" style=\"display:inline").append(this.browser.isIE() ? "" : "-block").append(";width:20px;height:20px;background-color:").append(color1.length() > 0 ? color1 : "#e7f2ff").append("\"></div>");
        colorHtml.append("<div id=\"_taskcolor2\" _default=\"").append("#d4e3f2").append("\" style=\"display:inline").append(this.browser.isIE() ? "" : "-block").append(";;width:20px;height:20px;background-color:").append(color2.length() > 0 ? color2 : "#d4e3f2").append("\"></div>");
        colorHtml.append("<div id=\"_taskcolor3\" _default=\"").append("#c9d8ed").append("\" style=\"display:inline").append(this.browser.isIE() ? "" : "-block").append(";;width:20px;height:20px;background-color:").append(color3.length() > 0 ? color3 : "#c9d8ed").append("\"></div>");
        colorHtml.append("</div>");
        if (!this.viewonly) {
            Button btn = new Button(this.pageContext);
            btn.setId("btTaskColor");
            btn.setText("...");
            btn.setAction("taskWorkflowProps.lookupTaskColor()");
            colorHtml.append("&nbsp;").append(btn.getHtml());
        }
        column.setProperty("pseudocolumn", colorHtml.toString());
        column.setProperty("events", events);
        column.setProperty("groupid", "Appearance");
        columns.add(column);
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append("<div style=\"padding: 5px 5px 5px 5px;\">");
        html.append(maint.getHtml());
        html.append("</div>");
        return html;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        html.append(this.getScriptAndStyle());
        TranslationProcessor tp = this.getTranslationProcessor();
        html.append("<div style=\"padding:5px 5px 5px 5px;\">");
        if (this.fullprops == null || this.fullprops.size() == 0) {
            html.append(tp.translate("No item selected"));
        } else if (this.type == ItemType.IO && this.ioprops == null) {
            html.append(tp.translate("Could not render properties"));
        } else if (this.type == ItemType.TASK) {
            html.append(this.getTaskHtml());
        } else {
            PropertyListCollection events = new PropertyListCollection();
            if (!this.viewonly) {
                PropertyList event = new PropertyList();
                event.setProperty("event", "onchange");
                event.setProperty("js", "taskWorkflowProps.maintFieldChange(event,this,'io','" + this.ioid + "')");
                events.add(event);
            }
            html.append(TaskDefWorkflowProperties.getIOHtml(this.fullprops, this.ioprops, this.viewonly, this.descendant, events, this.getConnectionId(), this.getSDIProcessor(), this.pageContext));
        }
        html.append("</div>");
        html.append(this.getEndScript());
        return html.toString();
    }

    public static enum ItemType {
        IO,
        TASK;

    }
}

