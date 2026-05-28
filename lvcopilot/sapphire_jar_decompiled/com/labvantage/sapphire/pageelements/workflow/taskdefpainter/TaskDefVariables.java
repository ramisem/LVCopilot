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
import com.labvantage.sapphire.pageelements.controls.Tree;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefProperties;
import com.labvantage.sapphire.util.MiscUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ActionBlock;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefVariables
extends BaseElement {
    private static final String IMAGE_NODE_C = "WEB-CORE/elements/workflow/images/folder_c.gif";
    private static final String IMAGE_NODE_E = "WEB-CORE/elements/workflow/images/folder_e.gif";
    public static final String RELATIVEVARIABLES_HELP = "If the editor style for this variable returns multiple values (e.g. multiple keys) then enter a semicolon delimited list of variables which you want the return values to be mapped into.";
    private static final String GROUPNONE_TEXT = "Variables";
    public static final String USERCONFIG_PREFIX = "taskdef_";
    public static final String JS_CLASS = "taskVariables";
    public static final String SESSION_PREFIX = "taskdef_";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_FORMDATASTORE = "formdatastore";
    public static final String PROPERTY_SDIDATASTORE = "sdidatastore";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_DESCENDANT = "descendant";
    public static final String TYPE_FORM = "Form";
    public static final String TYPE_STRING = "String";
    public static final String TYPE_SDILIST = "SDIList";
    public static final String TYPE_SDIDATASTORE = "SDIDataStore";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private boolean descendant = false;
    private boolean devMode;
    PropertyList taskprops = null;

    public TaskDefVariables(PageContext pageContext, PropertyList pageproperties) {
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

    private static String checkId(String id) {
        String ret = "";
        if (id.length() > 0) {
            if (id.indexOf(" ") > -1) {
                ret = "The Id contains spaces.";
            } else {
                Pattern p = Pattern.compile("\\W");
                if (id.matches(p.pattern())) {
                    ret = "The Id contains invalid characters.";
                } else {
                    String lid;
                    p = Pattern.compile("\\d");
                    if ((id.charAt(0) + "").matches(p.pattern())) {
                        ret = "The Id cannot start with a number.";
                    } else if (ret.length() == 0 && ((lid = id.toLowerCase()) == "name" || lid == "this" || lid == "function" || lid == "return" || lid == "if" || lid == "else" || lid == "for" || lid == "while" || lid == "typeof" || lid == "var" || lid == "catch" || lid == "try" || lid == "finally" || lid == "catch" || lid == "true" || lid == "false")) {
                        ret = "The Id is a keyword and cannot be used.";
                    }
                }
            }
        }
        return ret;
    }

    public static PropertyList getFormDataStore(String formobjectXML) {
        PropertyList formobject = new PropertyList();
        try {
            PropertyList ret = new PropertyList();
            PropertyListCollection fields = new PropertyListCollection();
            ret.setProperty("fields", fields);
            formobject.setPropertyList(formobjectXML);
            if (formobject.containsKey("formproperties") && formobject.getPropertyList("formproperties").getCollection("fields") != null) {
                PropertyListCollection actualfields = formobject.getPropertyList("formproperties").getCollection("fields");
                for (int i = 0; i < actualfields.size(); ++i) {
                    PropertyList field = new PropertyList();
                    field.setProperty("fieldid", actualfields.getPropertyList(i).getProperty("fieldid", ""));
                    fields.add(field);
                }
            }
            return ret;
        }
        catch (Exception e) {
            return new PropertyList();
        }
    }

    public static String getVariablesList(PropertyList taskprops, boolean includeSubVariables, boolean includeKeys, boolean displayValueStyle, String[] validTypes, String[] exclude) {
        StringBuffer ret = new StringBuffer();
        PropertyListCollection variables = taskprops.getCollection("variables");
        if (variables != null) {
            for (int i = 0; i < variables.size(); ++i) {
                PropertyListCollection fields;
                PropertyList formdata;
                PropertyList variable = variables.getPropertyList(i);
                String varid = variable.getProperty("variableid", "");
                String type = variable.getProperty("type", "");
                if (varid.length() <= 0 || validTypes != null && validTypes.length != 0 && !MiscUtil.MiscArray.isStringInArray(validTypes, type, true) || exclude != null && exclude.length != 0 && MiscUtil.MiscArray.isStringInArray(exclude, varid, false)) continue;
                if (ret.length() > 0) {
                    ret.append(";");
                }
                if (displayValueStyle) {
                    ret.append(varid).append("=").append(varid);
                } else {
                    ret.append(varid);
                }
                if (!includeSubVariables) continue;
                if (type.equalsIgnoreCase("sdilist") && includeKeys) {
                    if (displayValueStyle) {
                        ret.append(varid).append(".keyid1").append("=").append(varid).append(".keyid1").append(";");
                        ret.append(varid).append(".keyid2").append("=").append(varid).append(".keyid2").append(";");
                        ret.append(varid).append(".keyid3").append("=").append(varid).append(".keyid3").append("");
                        continue;
                    }
                    ret.append(varid).append(".keyid1;").append(varid).append(".keyid2;").append(varid).append(".keyid3");
                    continue;
                }
                if (!type.equalsIgnoreCase("form") || (formdata = variable.getPropertyList(PROPERTY_FORMDATASTORE)) == null || (fields = formdata.getCollection("fields")) == null) continue;
                for (int k = 0; k < fields.size(); ++k) {
                    String fieldid = fields.getPropertyList(k).getProperty("fieldid", "");
                    if (fieldid.length() <= 0) continue;
                    if (ret.length() > 0) {
                        ret.append(";");
                    }
                    if (displayValueStyle) {
                        ret.append(varid).append(".").append(fieldid).append("=").append(varid).append(".").append(fieldid);
                        continue;
                    }
                    ret.append(varid).append(".").append(fieldid);
                }
            }
        }
        return ret.toString();
    }

    private static boolean syncStep(PropertyList step, PropertyListCollection variables, Logger logger) {
        boolean changed = false;
        PropertyList merged = step.getPropertyList("steptypemerged");
        if (merged != null) {
            String xml;
            PropertyList override = step.getPropertyList("steptypeoverrides");
            PropertyList steppropsM = merged.getPropertyList("stepprops");
            PropertyList steptypeM = merged.getPropertyList("steptype");
            if (step.getProperty("propertytreeid", "").equalsIgnoreCase("FormStep")) {
                String variableid = steptypeM.getProperty("variableid");
                if (variableid.length() > 0) {
                    PropertyList var = variables.find("variableid", variableid);
                    if (var != null) {
                        String type = var.getProperty("type", "");
                        if (type.length() == 0) {
                            type = "form";
                            var.setProperty("type", type);
                            changed = true;
                        }
                        if (type.equalsIgnoreCase("form")) {
                            changed = true;
                            String fs = steptypeM.getProperty("form", "");
                            var.setProperty(PROPERTY_FORMDATASTORE, TaskDefVariables.getFormDataStore(fs));
                        }
                    } else {
                        logger.warn("FormStep " + step.getProperty("stepid", "") + " using invalid variable id " + variableid + ".");
                    }
                }
            } else if (step.getProperty("propertytreeid", "").equalsIgnoreCase("ActionBlockStep") && (xml = steptypeM.getProperty("processscript", "")).length() > 0) {
                ActionBlock ab = new ActionBlock();
                try {
                    ab.setXML(xml);
                    for (int c = 0; c < ab.getCommandCount(); ++c) {
                        Object command = ab.getCommand(c);
                        if (command == null || !(command instanceof ActionBlock.ReturnProperty)) continue;
                        ActionBlock.ReturnProperty retprop = (ActionBlock.ReturnProperty)command;
                        if (retprop.propertyid.length() <= 0) continue;
                        PropertyList var = variables.find("variableid", retprop.propertyid, true);
                        if (var == null) {
                            var = new PropertyList();
                            var.setProperty("variableid", retprop.propertyid);
                            var.setProperty("type", "string");
                            variables.add(var);
                            changed = true;
                            continue;
                        }
                        logger.debug("Variable from action block already exists.");
                    }
                }
                catch (Exception e) {
                    logger.warn("Could not scan action block. Error reading xml.");
                }
            }
        }
        return changed;
    }

    public static boolean syncVariables(PropertyList taskprops, String stepId, Logger logger) {
        return TaskDefVariables.syncVariables(taskprops, stepId, null, logger);
    }

    public static boolean syncVariables(PropertyList taskprops, String stepId, SDCProcessor sdcProcessor, Logger logger) {
        boolean changed = false;
        PropertyListCollection variables = taskprops.getCollection("variables");
        if (variables == null) {
            variables = new PropertyListCollection();
            taskprops.setProperty("variables", variables);
        }
        for (int i = 0; i < variables.size(); ++i) {
            PropertyList var = variables.getPropertyList(i);
            String t = var.getProperty("type", "");
            if (t.equalsIgnoreCase(TYPE_FORM)) {
                if (stepId != null && stepId.length() != 0) continue;
                PropertyList ds = new PropertyList();
                ds.setProperty("fields", new PropertyListCollection());
                var.setProperty(PROPERTY_FORMDATASTORE, ds);
                changed = true;
                continue;
            }
            if (!t.equalsIgnoreCase(TYPE_SDILIST) || sdcProcessor == null) continue;
            int keys = 0;
            String sdcid = var.getProperty("sdcid", "");
            if (var.getProperty("keys", "").length() != 0 || sdcid.length() <= 0) continue;
            String k = sdcProcessor.getProperty(sdcid, "keycolumns");
            try {
                keys = Integer.parseInt(k);
                var.setProperty("keys", "" + k);
                changed = true;
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        PropertyListCollection tasksteps = taskprops.getCollection("steps");
        if (tasksteps != null && tasksteps.size() > 0) {
            if (stepId != null && stepId.length() > 0) {
                PropertyList step = tasksteps.find("stepid", stepId);
                if (step != null && TaskDefVariables.syncStep(step, variables, logger)) {
                    changed = true;
                }
            } else {
                for (int i = 0; i < tasksteps.size(); ++i) {
                    PropertyList step = tasksteps.getPropertyList(i);
                    if (!TaskDefVariables.syncStep(step, variables, logger)) continue;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static void addUsage(PropertyListCollection usages, String type, String itemid, String itemtext, String property) {
        PropertyList usage = new PropertyList();
        usage.setProperty("type", type);
        usage.setProperty("itemid", itemid);
        usage.setProperty("text", itemtext);
        usage.setProperty("property", property);
        usages.add(usage);
    }

    private static void searchUsage(PropertyListCollection usages, String variableId, PropertyList searchable, String type, String itemid, String itemtext, String propertypath, String[] exclude) {
        Iterator it = searchable.keySet().iterator();
        while (it.hasNext()) {
            String property = it.next().toString();
            String fullprop = propertypath.length() > 0 ? propertypath + "." + property : property;
            if (MiscUtil.MiscArray.isStringInArray(exclude, fullprop, true)) continue;
            Object o = searchable.get(property);
            if (o instanceof String) {
                String value = (String)o;
                if ((property.equalsIgnoreCase("variable") || property.equalsIgnoreCase("variableid")) && value.startsWith(variableId)) {
                    TaskDefVariables.addUsage(usages, type, itemid, itemtext, fullprop);
                    continue;
                }
                if (property.equalsIgnoreCase("processscript") && value.contains("<returnproperty id=\"" + variableId + "\">")) {
                    TaskDefVariables.addUsage(usages, type, itemid, itemtext, fullprop);
                    continue;
                }
                if (!value.contains("[variables." + variableId)) continue;
                TaskDefVariables.addUsage(usages, type, itemid, itemtext, fullprop);
                continue;
            }
            if (o instanceof PropertyList) {
                TaskDefVariables.searchUsage(usages, variableId, (PropertyList)o, type, itemid, itemtext, fullprop, exclude);
                continue;
            }
            if (!(o instanceof PropertyListCollection)) continue;
            PropertyListCollection col = (PropertyListCollection)o;
            for (int i = 0; i < col.size(); ++i) {
                TaskDefVariables.searchUsage(usages, variableId, col.getPropertyList(i), type, itemid, itemtext, fullprop + "[" + i + "]", exclude);
            }
        }
    }

    private static void findUsage(PropertyList variable, PropertyList taskprops, Logger logger) {
        PropertyListCollection steps;
        PropertyListCollection usages = new PropertyListCollection();
        String variableId = variable.getProperty("variableid", "");
        PropertyListCollection taskio = taskprops.getCollection("taskio");
        if (taskio != null) {
            for (int i = 0; i < taskio.size(); ++i) {
                PropertyList io = taskio.getPropertyList(i);
                if (io.getProperty("ioflag", "O").equalsIgnoreCase("I")) {
                    if (!io.getProperty("variableid", "").equals(variableId)) continue;
                    TaskDefVariables.addUsage(usages, "Input", io.getProperty("ioid", ""), io.getProperty("iodesc", io.getProperty("ioid", "")), "variableid");
                    continue;
                }
                if (io.getProperty("variableid", "").startsWith(variableId)) {
                    TaskDefVariables.addUsage(usages, "Output", io.getProperty("ioid", ""), io.getProperty("iodesc", io.getProperty("ioid", "")), "variableid");
                }
                if (io.getProperty("variableid2", "").startsWith(variableId)) {
                    TaskDefVariables.addUsage(usages, "Output", io.getProperty("ioid", ""), io.getProperty("iodesc", io.getProperty("ioid", "")), "variableid2");
                }
                if (!io.getProperty("variableid3", "").startsWith(variableId)) continue;
                TaskDefVariables.addUsage(usages, "Output", io.getProperty("ioid", ""), io.getProperty("iodesc", io.getProperty("ioid", "")), "variableid3");
            }
        }
        if ((steps = taskprops.getCollection("steps")) != null) {
            for (int i = 0; i < steps.size(); ++i) {
                PropertyList step = steps.getPropertyList(i);
                TaskDefVariables.searchUsage(usages, variableId, step, "Step", step.getProperty("stepid", ""), step.getProperty("shorttitle", step.getProperty("title", step.getProperty("stepid", ""))), "", new String[]{"steptypeoverrides", "steptypemerged.stepdef", "propertytreeid", "extendnodeid", "buttons.buttonid", "x", "y"});
            }
        }
        if (variable.containsKey("usages")) {
            variable.remove("usages");
        }
        variable.setProperty("usages", usages);
    }

    public static void findVariableUsage(PropertyList taskprops, String variableId, Logger logger) {
        PropertyListCollection variables = taskprops.getCollection("variables");
        if (variables != null) {
            if (variableId == null || variableId.length() == 0) {
                for (int i = 0; i < variables.size(); ++i) {
                    PropertyList var = variables.getPropertyList(i);
                    TaskDefVariables.findUsage(var, taskprops, logger);
                }
            } else {
                PropertyList var = variables.find("variableid", variableId);
                TaskDefVariables.findUsage(var, taskprops, logger);
            }
        }
    }

    private StringBuffer getScriptAndStyle() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/scripts/tags.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/taskvariables.js\"></script>");
        html.append("\n<style>");
        html.append("\n.task_longtext { width:400px; }");
        html.append("\n</style>");
        return html;
    }

    private StringBuffer getEndScript(boolean viewOnly) {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append(JS_CLASS).append(".viewonly=").append(viewOnly).append(";");
        html.append("</script>");
        return html;
    }

    public static StringBuffer getVariablePropertiesHTML(PropertyList taskprops, String variableid, boolean viewOnly, boolean descendant, String connectionId, PageContext pageContext, Logger logger) {
        PropertyListCollection events = new PropertyListCollection();
        String lookupcallback = "";
        if (!viewOnly) {
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "taskVariables.maintFieldChange(event,this,'','" + variableid + "')");
            events.add(event);
            lookupcallback = "taskVariables.variableLookupCallback";
        }
        return TaskDefVariables.getVariablePropertiesHTML(taskprops, variableid, viewOnly, descendant, events, lookupcallback, connectionId, pageContext, logger);
    }

    public static void setEditorStyleFieldMapping(EditorStyleField esf, String varId, String relatedvariableid, String lookupCallback) {
        PropertyListCollection llcols;
        PropertyList column = esf.getColumn();
        if (relatedvariableid.length() > 0 && column.getPropertyList("lookuplink") != null && (llcols = column.getPropertyList("lookuplink").getCollection("columns")) != null && llcols.size() > 0) {
            String[] mapfields = StringUtil.split(relatedvariableid, ";");
            int mf = 0;
            boolean first = true;
            for (int ic = 0; ic < llcols.size(); ++ic) {
                PropertyList llcol = llcols.getPropertyList(ic);
                String llmode = llcol.getProperty("mode", "Display and Return");
                if (!llmode.equalsIgnoreCase("Display and Return") && !llmode.equalsIgnoreCase("Return Only")) continue;
                if (first) {
                    llcol.setProperty("mapfieldid", "defaultvalue");
                    first = false;
                    continue;
                }
                llcol.setProperty("mapfieldid", mapfields[mf]);
                if (++mf >= mapfields.length) break;
            }
            column.getPropertyList("lookuplink").setProperty("lookupcallback", lookupCallback);
            column.getPropertyList("lookuplink").setProperty("lookupcallbackextra", varId);
        }
    }

    public static StringBuffer getVariablePropertiesHTML(PropertyList taskprops, String variableid, boolean viewOnly, boolean descendant, PropertyListCollection events, String lookupcallback, String connectionId, PageContext pageContext, Logger logger) {
        PropertyList variableProps;
        StringBuffer out = new StringBuffer();
        PropertyList propertyList = variableProps = taskprops.getCollection("variables") != null ? taskprops.getCollection("variables").find("variableid", variableid) : null;
        if (variableProps != null && variableProps.size() > 0) {
            boolean complex;
            boolean fullViewOnly = viewOnly || descendant;
            TranslationProcessor tp = new TranslationProcessor(pageContext);
            DataSet dsFake = new DataSet();
            dsFake.addColumn("variableid", 0);
            dsFake.addColumn("load", 0);
            dsFake.addColumn("type", 0);
            dsFake.addColumn("exposed", 0);
            dsFake.addColumn("setup", 0);
            dsFake.addColumn("editorstyleid", 0);
            dsFake.addColumn("relatedvariableid", 0);
            dsFake.addColumn("defaultvalue", 0);
            dsFake.addColumn("prompt", 0);
            dsFake.addColumn("sourcetype", 0);
            dsFake.addColumn("sourceid", 0);
            dsFake.addColumn("mandatory", 0);
            dsFake.addColumn("modifiable", 0);
            dsFake.addColumn("hidden", 0);
            dsFake.addColumn("queryable", 0);
            dsFake.addColumn("help", 0);
            dsFake.addColumn("description", 0);
            dsFake.addColumn("linkto", 0);
            dsFake.addColumn("group", 0);
            dsFake.addColumn("sdcid", 0);
            String type = variableProps.getProperty("type", "");
            boolean setup = variableProps.getProperty("setup", "N").equalsIgnoreCase("Y");
            boolean exposed = variableProps.getProperty("exposed", "N").equalsIgnoreCase("Y");
            boolean bl = complex = type.equalsIgnoreCase("form") || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE);
            if (setup && variableProps.getProperty("modifiable", "").length() == 0) {
                variableProps.setProperty("modifiable", "Y");
            }
            String editorstyle = variableProps.getProperty("editorstyleid", "");
            String relatedvariableid = variableProps.getProperty("relatedvariableid", "");
            String sourcetype = variableProps.getProperty("sourcetype", "");
            String sourceid = variableProps.getProperty("sourceid", "");
            dsFake.addRow();
            dsFake.setValue(0, "variableid", variableid);
            dsFake.setValue(0, "load", variableProps.getProperty("load", ""));
            dsFake.setValue(0, "type", type);
            dsFake.setValue(0, "exposed", exposed ? "Y" : "N");
            dsFake.setValue(0, "setup", setup ? "Y" : "N");
            dsFake.setValue(0, "editorstyleid", editorstyle);
            dsFake.setValue(0, "relatedvariableid", relatedvariableid);
            dsFake.setValue(0, "defaultvalue", variableProps.getProperty("defaultvalue", ""));
            dsFake.setValue(0, "prompt", variableProps.getProperty("prompt", ""));
            dsFake.setValue(0, "sourcetype", sourcetype);
            dsFake.setValue(0, "sourceid", sourceid);
            dsFake.setValue(0, "mandatory", variableProps.getProperty("mandatory", "N"));
            dsFake.setValue(0, "modifiable", variableProps.getProperty("modifiable", "Y"));
            dsFake.setValue(0, "hidden", variableProps.getProperty("hidden", "N"));
            dsFake.setValue(0, "queryable", variableProps.getProperty("queryable", "N"));
            dsFake.setValue(0, "help", variableProps.getProperty("help", ""));
            dsFake.setValue(0, "description", variableProps.getProperty("description", ""));
            dsFake.setValue(0, "linkto", variableProps.getProperty("linkto", ""));
            dsFake.setValue(0, "group", variableProps.getProperty("group", ""));
            dsFake.setValue(0, "sdcid", variableProps.getProperty("sdcid", ""));
            DataView maint = new DataView(pageContext, "primary", dsFake, "", connectionId);
            maint.setElementid("task_workflow_io_pri_dataview");
            maint.setSDCId("LV_TaskDef");
            maint.getSDIInfo().setSdcid("LV_TaskDef");
            PropertyList maintProps = new PropertyList();
            maintProps.setProperty("sdcid", "LV_TaskDef");
            maintProps.setProperty("style", "FormWithFieldGroups");
            maintProps.setProperty("formcols", "2");
            if (viewOnly) {
                maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
            }
            PropertyListCollection columns = new PropertyListCollection();
            if (events == null) {
                events = new PropertyListCollection();
            }
            PropertyList column = new PropertyList();
            column.setProperty("columnid", "variableid");
            column.setProperty("title", "Variable Id");
            column.setProperty("mode", "readonly");
            column.setProperty("events", events);
            column.setProperty("groupid", "Variable");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "type");
            column.setProperty("title", "Type");
            column.setProperty("mode", "readonly");
            column.setProperty("displayvalue", "string=String;sdidatastore=SDIDataStore;sdilist=SDIList;form=Form");
            column.setProperty("events", events);
            column.setProperty("groupid", "Variable");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "setup");
            column.setProperty("title", "Setup Variable");
            column.setProperty("mode", !viewOnly ? "checkbox" : "readonly");
            if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                column.setProperty("disable", "Y");
            }
            column.setProperty("displayvalue", "Y=Yes;N=No");
            column.setProperty("events", events);
            column.setProperty("groupid", "Variable");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "exposed");
            column.setProperty("title", "Exposed To Workflow");
            column.setProperty("mode", !viewOnly ? "checkbox" : "readonly");
            if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                column.setProperty("disable", "Y");
            }
            column.setProperty("displayvalue", "Y=Yes;N=No");
            column.setProperty("events", events);
            column.setProperty("groupid", "Variable");
            columns.add(column);
            boolean typeViewOnly = viewOnly;
            if (type.equalsIgnoreCase("string")) {
                column = new PropertyList();
                column.setProperty("columnid", "editorstyleid");
                column.setProperty("title", "Editor Style");
                column.setProperty("mode", !typeViewOnly ? "lookup" : "readonly");
                PropertyList lookup = new PropertyList();
                lookup.setProperty("href", "rc?command=page&page=LV_EditorStyleLookup");
                lookup.setProperty("tip", "Lookup Editor Style");
                lookup.setProperty("dialogtype", "Sapphire Dialog");
                if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                    column.setProperty("disable", "Y");
                } else {
                    lookup.setProperty("restrictivewhere", "editorstyle.datatype='" + EditorStyleField.getEditorStyleDataType(type) + "'");
                }
                column.setProperty("lookuplink", lookup);
                column.setProperty("events", events);
                column.setProperty("groupid", "Value Details");
                columns.add(column);
                column = new PropertyList();
                column.setProperty("columnid", "relatedvariableid");
                column.setProperty("title", "Related Variables");
                column.setProperty("mode", !typeViewOnly ? "input" : "readonly");
                if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                    column.setProperty("disable", "Y");
                }
                column.setProperty("events", events);
                column.setProperty("tip", tp.translate(RELATIVEVARIABLES_HELP));
                column.setProperty("class", "task_longtext");
                column.setProperty("groupid", "Value Details");
                columns.add(column);
                PropertyList esfCol = null;
                if (editorstyle.length() > 0) {
                    EditorStyleField esf = new EditorStyleField(pageContext, null, connectionId);
                    try {
                        esf.setEditorStyleId(editorstyle);
                        TaskDefVariables.setEditorStyleFieldMapping(esf, variableid, relatedvariableid, lookupcallback);
                        esfCol = esf.getColumn();
                    }
                    catch (Exception e) {
                        logger.warn("Invalid editor style");
                    }
                } else if (!(type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form"))) {
                    EditorStyleField esf = new EditorStyleField(pageContext, null, connectionId);
                    esf.setDefaultEditorStyleProperties(EditorStyleField.getEditorStyleDataType(type), "", "");
                    esfCol = esf.getColumn();
                }
                if (esfCol == null) {
                    column = new PropertyList();
                    column.setProperty("mode", !typeViewOnly ? "input" : "readonly");
                } else {
                    column = esfCol;
                    if (typeViewOnly) {
                        column.setProperty("mode", "readonly");
                    }
                }
                if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                    column.setProperty("disable", "Y");
                }
                column.setProperty("columnid", "defaultvalue");
                column.setProperty("title", "Default Value");
                column.setProperty("events", events);
                column.setProperty("groupid", "Value Details");
                column.setProperty("colspan", "2");
                columns.add(column);
                if (setup) {
                    column = new PropertyList();
                    column.setProperty("columnid", "prompt");
                    column.setProperty("title", "User Prompt");
                    column.setProperty("mode", !typeViewOnly ? "input" : "readonly");
                    if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                        column.setProperty("disable", "Y");
                    }
                    column.setProperty("events", events);
                    column.setProperty("class", "task_longtext");
                    column.setProperty("groupid", "Setup Details");
                    columns.add(column);
                    column = new PropertyList();
                    column.setProperty("columnid", "help");
                    column.setProperty("title", "Help");
                    column.setProperty("mode", !typeViewOnly ? "input" : "readonly");
                    if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                        column.setProperty("disable", "Y");
                    }
                    column.setProperty("events", events);
                    column.setProperty("class", "task_longtext");
                    column.setProperty("groupid", "Setup Details");
                    columns.add(column);
                    column = new PropertyList();
                    column.setProperty("columnid", "mandatory");
                    column.setProperty("title", "Mandatory");
                    column.setProperty("mode", !typeViewOnly ? "checkbox" : "readonly");
                    if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                        column.setProperty("disable", "Y");
                    }
                    column.setProperty("displayvalue", "Y=Yes;N=No");
                    column.setProperty("events", events);
                    column.setProperty("groupid", "Setup Details");
                    columns.add(column);
                    column = new PropertyList();
                    column.setProperty("columnid", "modifiable");
                    column.setProperty("title", "Modifiable");
                    column.setProperty("mode", !typeViewOnly ? "checkbox" : "readonly");
                    if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                        column.setProperty("disable", "Y");
                    }
                    column.setProperty("displayvalue", "Y=Yes;N=No");
                    column.setProperty("events", events);
                    column.setProperty("groupid", "Setup Details");
                    columns.add(column);
                    column = new PropertyList();
                    column.setProperty("columnid", "hidden");
                    column.setProperty("title", "Hidden");
                    column.setProperty("mode", !typeViewOnly ? "checkbox" : "readonly");
                    if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                        column.setProperty("disable", "Y");
                    }
                    column.setProperty("displayvalue", "Y=Yes;N=No");
                    column.setProperty("events", events);
                    column.setProperty("groupid", "Setup Details");
                    columns.add(column);
                }
                if (exposed) {
                    column = new PropertyList();
                    column.setProperty("columnid", "description");
                    column.setProperty("title", "Description");
                    column.setProperty("mode", !viewOnly ? "input" : "readonly");
                    if (type.length() == 0 || type.equalsIgnoreCase(PROPERTY_SDIDATASTORE) || type.equalsIgnoreCase("sdilist") || type.equalsIgnoreCase("form")) {
                        column.setProperty("disable", "Y");
                    }
                    column.setProperty("events", events);
                    column.setProperty("class", "task_longtext");
                    column.setProperty("groupid", "Exposed Details");
                    columns.add(column);
                }
            }
            if (type.equalsIgnoreCase("sdilist")) {
                column = new PropertyList();
                column.setProperty("columnid", "sdcid");
                column.setProperty("title", "SDC Id");
                column.setProperty("mode", "readonly");
                column.setProperty("sql", "select sdcid from sdc");
                column.setProperty("events", events);
                column.setProperty("groupid", "Type Definition");
                columns.add(column);
            }
            maintProps.setProperty("columns", columns);
            maint.setElementProperties(maintProps);
            out.append("<div style=\"padding: 5px 5px 5px 5px;\">");
            out.append(maint.getHtml());
            out.append("</div>");
        } else {
            out.append("No variable selected.");
        }
        return out;
    }

    public static StringBuffer getVariablesTreeHTML(PropertyList taskprops, String selectedId, boolean clickable, String className, boolean viewOnly, boolean descendant, TaskDefProperties.ItemType highlightType, String hightlightItem, PropertyList userConfig, PageContext pageContext, Logger logger) {
        Button btn;
        Grouping grouping;
        StringBuffer out = new StringBuffer();
        Tree tree = new Tree(pageContext);
        tree.setPadding(0);
        PropertyList treeProps = new PropertyList();
        treeProps.setProperty("includejs", "N");
        PropertyListCollection rootitems = new PropertyListCollection();
        PropertyListCollection variables = taskprops.getCollection("variables");
        if (variables == null) {
            variables = new PropertyListCollection();
        }
        SDCProcessor sdcProcessor = new SDCProcessor(pageContext);
        try {
            grouping = Grouping.valueOf(userConfig.getProperty("taskdef_variablesgroupby", Grouping.NONE.toString()).toUpperCase());
        }
        catch (Exception e) {
            grouping = Grouping.NONE;
        }
        if (grouping == Grouping.USAGE) {
            TaskDefVariables.findVariableUsage(taskprops, "", logger);
        }
        String sort = userConfig.getProperty("taskdef_variablessort", "");
        int longestVariable = 0;
        String longestVariableEl = "";
        if (variables.size() > 0) {
            HashMap<String, PropertyListCollection> groups = new HashMap<String, PropertyListCollection>();
            for (int i = 0; i < variables.size(); ++i) {
                String[] grouparray;
                PropertyList curVar = variables.getPropertyList(i);
                String varId = curVar.getProperty("variableid", "");
                if (varId.length() <= 0) continue;
                if (grouping == Grouping.TYPE) {
                    String group = curVar.getProperty("type", "No Type");
                    group = group.equalsIgnoreCase("sdilist") ? TYPE_SDILIST : (group.equalsIgnoreCase(PROPERTY_SDIDATASTORE) ? TYPE_SDIDATASTORE : group.substring(0, 1).toUpperCase() + group.substring(1));
                    grouparray = new String[]{group};
                } else if (grouping == Grouping.SCOPE) {
                    String group = "";
                    boolean setup = curVar.getProperty("setup", "N").equalsIgnoreCase("Y");
                    boolean exposed = curVar.getProperty("exposed", "N").equalsIgnoreCase("Y");
                    group = exposed && setup ? "Exposed Setup" : (setup && !exposed ? "Setup" : (!setup && exposed ? "Exposed" : "Local"));
                    grouparray = new String[]{group};
                } else if (grouping == Grouping.USAGE) {
                    PropertyListCollection usage = curVar.getCollection("usages");
                    if (usage != null && usage.size() > 0) {
                        ArrayList<String> usages = new ArrayList<String>();
                        for (int u = 0; u < usage.size(); ++u) {
                            PropertyList usageproplist = usage.getPropertyList(u);
                            String usagetext = usageproplist.getProperty("type", "") + ";" + usageproplist.getProperty("itemid", "") + ";" + usageproplist.getProperty("text", "");
                            if (usages.size() != 0 && usages.contains(usagetext)) continue;
                            usages.add(usagetext);
                        }
                        grouparray = new String[usages.size()];
                        usages.toArray(grouparray);
                    } else {
                        grouparray = new String[]{"Unused"};
                    }
                } else {
                    grouparray = new String[]{GROUPNONE_TEXT};
                }
                for (int g = 0; g < grouparray.length; ++g) {
                    String fid;
                    int k;
                    PropertyListCollection subitems;
                    String group = grouparray[g];
                    if (groups.containsKey(group)) {
                        subitems = (PropertyListCollection)groups.get(group);
                    } else {
                        subitems = new PropertyListCollection();
                        groups.put(group, subitems);
                    }
                    PropertyList subitem = new PropertyList();
                    boolean setup = curVar.getProperty("setup", "N").equalsIgnoreCase("Y");
                    boolean exposed = curVar.getProperty("exposed", "N").equalsIgnoreCase("Y");
                    String hint = (setup && exposed ? "Exposed Setup" : (setup && !exposed ? "Setup" : (!setup && exposed ? "Exposed" : "Local"))) + " variable " + varId + (curVar.getProperty("type", "").length() > 0 ? " of type " + curVar.getProperty("type", "") + "." : ".");
                    String id = "taskdef_ti" + g + "_" + varId;
                    if (varId.length() > longestVariable) {
                        longestVariable = varId.length();
                        longestVariableEl = id;
                    }
                    subitem.setProperty("prehtml", "<div id=\"" + id + "_container\"><div " + (className != null && className.length() > 0 ? "class=\"" + className + "\" " : "") + "id=\"" + id + "\" name=\"taskdef_ti_" + varId + "\" style=\"padding-left:5px; padding-top:2px; padding-bottom:2px;padding-right:5px;\" title=\"" + hint + "\">");
                    subitem.setProperty("text", varId);
                    subitem.setProperty("posthtml", "</div></div>");
                    subitem.setProperty("expanded", "N");
                    subitem.setProperty("translate", "N");
                    subitem.setProperty("expandedimage", "");
                    subitem.setProperty("collapsedimage", "");
                    if (subitems.size() == 0) {
                        subitems.add(subitem);
                    } else if (sort.length() > 0) {
                        int pos = -1;
                        for (int t = 0; t < subitems.size(); ++t) {
                            int c;
                            String subitemtext = subitems.getPropertyList(t).getProperty("text");
                            int n = c = sort.equalsIgnoreCase("za") ? subitemtext.compareToIgnoreCase(varId) : varId.compareToIgnoreCase(subitemtext);
                            if (c >= 0) continue;
                            pos = t;
                            break;
                        }
                        if (pos < 0) {
                            subitems.add(subitem);
                        } else {
                            subitems.add(pos, subitem);
                        }
                    } else {
                        subitems.add(subitem);
                    }
                    String type = curVar.getProperty("type", "");
                    if (type.equalsIgnoreCase(TYPE_FORM) && curVar.containsKey(PROPERTY_FORMDATASTORE)) {
                        PropertyListCollection fields = curVar.getPropertyList(PROPERTY_FORMDATASTORE).getCollection("fields");
                        if (fields != null && fields.size() > 0) {
                            PropertyListCollection formitems = new PropertyListCollection();
                            for (k = 0; k < fields.size(); ++k) {
                                PropertyList formitem = new PropertyList();
                                String fieldid = fields.getPropertyList(k).getProperty("fieldid", "");
                                fid = id + "__fi__" + fieldid;
                                if (fieldid.length() > longestVariable) {
                                    longestVariable = fieldid.length();
                                    longestVariableEl = fid;
                                }
                                formitem.setProperty("prehtml", "<div id=\"" + fid + "_container\"><div " + (className != null && className.length() > 0 ? "class=\"" + className + "\" " : "") + "id=\"" + fid + "\" name=\"taskdef_ti_" + varId + "__fi__" + fieldid + "\" style=\"padding-left:5px; padding-top:2px; padding-bottom:2px;padding-right:5px;\">");
                                formitem.setProperty("text", fieldid);
                                formitem.setProperty("posthtml", "</div></div>");
                                formitem.setProperty("showexpandcollapse", "N");
                                formitem.setProperty("expanded", "N");
                                formitem.setProperty("translate", "N");
                                formitem.setProperty("expandedimage", "");
                                formitem.setProperty("collapsedimage", "");
                                formitems.add(formitem);
                            }
                            subitem.setProperty("items", formitems);
                        }
                    } else if (type.equalsIgnoreCase(TYPE_SDILIST)) {
                        int keys = 0;
                        if (curVar.getProperty("keys", "").length() > 0) {
                            try {
                                keys = Integer.parseInt(curVar.getProperty("keys", ""));
                            }
                            catch (Exception formitems) {
                                // empty catch block
                            }
                        }
                        if (keys < 1) {
                            if (curVar.getProperty("sdcid", "").length() > 0) {
                                String k2 = sdcProcessor.getProperty(curVar.getProperty("sdcid", ""), "keycolumns");
                                try {
                                    keys = Integer.parseInt(k2);
                                    curVar.setProperty("keys", "" + k2);
                                }
                                catch (Exception e) {
                                    keys = 3;
                                }
                            } else {
                                keys = 3;
                            }
                        }
                        PropertyListCollection keyitems = new PropertyListCollection();
                        for (k = 0; k < keys; ++k) {
                            PropertyList keyitem = new PropertyList();
                            String kid = "keyid" + (k + 1);
                            fid = id + "__ki__" + kid;
                            if (kid.length() > longestVariable) {
                                longestVariable = kid.length();
                                longestVariableEl = fid;
                            }
                            keyitem.setProperty("prehtml", "<div id=\"" + fid + "_container\"><div " + (className != null && className.length() > 0 ? "class=\"" + className + "\" " : "") + "id=\"" + fid + "\" name=\"taskdef_ti_" + varId + "__ki__" + id + "\" style=\"padding-left:5px; padding-top:2px; padding-bottom:2px;padding-right:5px;\">");
                            keyitem.setProperty("text", kid);
                            keyitem.setProperty("posthtml", "</div></div>");
                            keyitem.setProperty("showexpandcollapse", "N");
                            keyitem.setProperty("expanded", "N");
                            keyitem.setProperty("translate", "N");
                            keyitem.setProperty("expandedimage", "");
                            keyitem.setProperty("collapsedimage", "");
                            keyitems.add(keyitem);
                        }
                        subitem.setProperty("items", keyitems);
                    }
                    subitem.setProperty("showexpandcollapse", subitem.getCollection("items") != null && subitem.getCollection("items").size() > 0 ? "Y" : "N");
                }
            }
            if (grouping == Grouping.NONE) {
                rootitems.add(TaskDefVariables.getNode(GROUPNONE_TEXT, grouping, highlightType, hightlightItem, taskprops, (PropertyListCollection)groups.get(GROUPNONE_TEXT)));
            } else if (grouping == Grouping.SCOPE) {
                boolean local = false;
                for (Object key : groups.keySet()) {
                    PropertyList node = TaskDefVariables.getNode((String)key, grouping, highlightType, hightlightItem, taskprops, (PropertyListCollection)groups.get(key));
                    if (rootitems.size() == 0 || ((String)key).equalsIgnoreCase("execution")) {
                        rootitems.add(node);
                        continue;
                    }
                    if (((String)key).equalsIgnoreCase("setup")) {
                        rootitems.add(local ? 1 : 0, node);
                        continue;
                    }
                    local = true;
                    rootitems.add(0, node);
                }
            } else if (grouping == Grouping.USAGE) {
                boolean unused = false;
                boolean output = false;
                for (String key : groups.keySet()) {
                    PropertyList node = TaskDefVariables.getNode(key, grouping, highlightType, hightlightItem, taskprops, (PropertyListCollection)groups.get(key));
                    int pos = -1;
                    String text = node.getProperty("text");
                    if (rootitems.size() == 0) {
                        rootitems.add(node);
                        continue;
                    }
                    if (text.startsWith("Unused ")) {
                        rootitems.add(node);
                        unused = true;
                        continue;
                    }
                    if (text.startsWith("Input ")) {
                        rootitems.add(0, node);
                        continue;
                    }
                    if (text.startsWith("Output ")) {
                        if (unused) {
                            rootitems.add(rootitems.size() - 2, node);
                        } else {
                            rootitems.add(node);
                        }
                        output = true;
                        continue;
                    }
                    for (int t = 0; t < rootitems.size(); ++t) {
                        int c;
                        String subitemtext = rootitems.getPropertyList(t).getProperty("text");
                        if (!subitemtext.startsWith("Step ") || (c = text.compareToIgnoreCase(subitemtext)) >= 0) continue;
                        pos = t;
                        break;
                    }
                    if (pos < 0) {
                        if (output) {
                            rootitems.add(unused ? rootitems.size() - 2 : rootitems.size() - 1, node);
                            continue;
                        }
                        if (unused) {
                            rootitems.add(rootitems.size() - 1, node);
                            continue;
                        }
                        rootitems.add(node);
                        continue;
                    }
                    rootitems.add(pos, node);
                }
            } else {
                for (String key : groups.keySet()) {
                    PropertyList node = TaskDefVariables.getNode(key, grouping, highlightType, hightlightItem, taskprops, (PropertyListCollection)groups.get(key));
                    int pos = -1;
                    String text = node.getProperty("text");
                    for (int t = 0; t < rootitems.size(); ++t) {
                        String subitemtext = rootitems.getPropertyList(t).getProperty("text");
                        int c = text.compareToIgnoreCase(subitemtext);
                        if (c >= 0) continue;
                        pos = t;
                        break;
                    }
                    if (pos < 0) {
                        rootitems.add(node);
                        continue;
                    }
                    rootitems.add(pos, node);
                }
            }
            treeProps.setProperty("rootitems", rootitems);
            if (userConfig != null) {
                tree.setUserConfig(userConfig);
            }
            tree.setId("taskdef_variablestree");
            tree.setElementProperties(treeProps);
            tree.setAttribute("_longestel", longestVariableEl);
            tree.setAttribute("_longestsize", "" + longestVariable);
            out.append(tree.getHtml());
        } else {
            out.append("<div style=\"margin-left: 10px; margin-top:10px;\">");
            out.append("No variables");
            out.append("</div>");
        }
        Browser browser = new Browser(pageContext);
        String style = browser.isIE() ? "height:23px;width:30px;padding: 0 0 0 0;" : "height:22px;";
        out.append("<div style=\"margin-left: 10px; margin-top:10px;\">");
        if (!viewOnly && !descendant) {
            btn = new Button(pageContext);
            btn.setId("btAddVarPainter");
            btn.setTip("Add Variable");
            btn.setImg("WEB-CORE/images/png/Add.png");
            btn.setAction("sapphire.page.getTop().taskdef_frame.taskDef.buttons.addVar()");
            btn.setStyle(style);
            out.append(btn.getHtml());
            if (sort.length() == 0) {
                btn = new Button(pageContext);
                btn.setId("btMoveVarUpPainter");
                btn.setTip("Move Variable Up");
                btn.setImg("WEB-CORE/images/gif/MoveUp.gif");
                btn.setAction("sapphire.page.getTop().taskdef_frame.taskDef.buttons.moveVarUp(event)");
                btn.setStyle(style);
                out.append(btn.getHtml());
                btn = new Button(pageContext);
                btn.setId("btMoveVarDownPainter");
                btn.setTip("Move Variable Down");
                btn.setImg("WEB-CORE/images/gif/MoveDown.gif");
                btn.setAction("sapphire.page.getTop().taskdef_frame.taskDef.buttons.moveVarDown(event)");
                btn.setStyle(style);
                out.append(btn.getHtml());
            }
        }
        btn = new Button(pageContext);
        btn.setTip("Change Grouping");
        btn.setId("btChangeGroupPainter");
        btn.setImg("WEB-CORE/elements/richtext/images/grouping.gif");
        btn.setAction("sapphire.page.getTop().taskdef_frame.taskDef.contextMenu.show(event);");
        btn.setStyle(style);
        out.append(btn.getHtml());
        out.append("</div>");
        return out;
    }

    private static PropertyList getNode(String group, Grouping grouping, TaskDefProperties.ItemType highlightType, String highlightItem, PropertyList taskprops, PropertyListCollection subitems) {
        String[] grouparr;
        PropertyList topitem = new PropertyList();
        topitem.setProperty("prehtml", "");
        String text = grouping == Grouping.USAGE ? ((grouparr = StringUtil.split(group, ";")).length == 3 ? grouparr[0] + " " + (grouparr[2].length() > 0 ? grouparr[2] : grouparr[1]) : group) : group;
        topitem.setProperty("text", text + (subitems != null ? " (" + subitems.size() + ")" : ""));
        topitem.setProperty("showexpandcollapse", "Y");
        topitem.setProperty("translate", "N");
        if (grouping == Grouping.USAGE) {
            String[] usagetext = StringUtil.split(group, ";");
            if (usagetext.length == 3) {
                String imge = IMAGE_NODE_E;
                String imgc = IMAGE_NODE_C;
                String style = "";
                if (highlightItem != null && highlightItem.length() > 0 && highlightType != null) {
                    if (highlightType == TaskDefProperties.ItemType.IO && (usagetext[0].equalsIgnoreCase("input") || usagetext[0].equalsIgnoreCase("output")) && highlightItem.equals(usagetext[1])) {
                        style = " style=\"font-weight:bolder;\"";
                    } else if (highlightType == TaskDefProperties.ItemType.STEP && usagetext[0].equalsIgnoreCase("step") && highlightItem.equals(usagetext[1])) {
                        style = " style=\"font-weight:bolder;\"";
                    }
                }
                topitem.setProperty("prehtml", "<div id=\"taskdef_ri_" + usagetext[0] + "_" + usagetext[1] + "\"" + style + ">");
                topitem.setProperty("posthtml", "</div>");
                if (usagetext[0].equalsIgnoreCase("input")) {
                    imgc = imge = "WEB-CORE/elements/workflow/images/input.png";
                } else if (usagetext[0].equalsIgnoreCase("output")) {
                    imgc = imge = "WEB-CORE/elements/workflow/images/output.png";
                } else {
                    PropertyList step;
                    PropertyListCollection steps = taskprops.getCollection("steps");
                    if (steps != null && steps.size() > 0 && (step = steps.find("stepid", usagetext[1])) != null) {
                        imge = step.getProperty("icon", imge);
                        imgc = step.getProperty("icon", imgc);
                    }
                }
                topitem.setProperty("expandedimage", imge);
                topitem.setProperty("collapsedimage", imgc);
            } else {
                topitem.setProperty("prehtml", "<div>");
                topitem.setProperty("posthtml", "</div>");
                topitem.setProperty("expandedimage", IMAGE_NODE_E);
                topitem.setProperty("collapsedimage", IMAGE_NODE_C);
            }
        } else {
            topitem.setProperty("prehtml", "<div>");
            topitem.setProperty("posthtml", "</div>");
            topitem.setProperty("expandedimage", IMAGE_NODE_E);
            topitem.setProperty("collapsedimage", IMAGE_NODE_C);
        }
        if (subitems != null && subitems.size() > 0) {
            topitem.setProperty("items", subitems);
        } else {
            PropertyList subitem = new PropertyList();
            subitem.setProperty("text", "No Variables");
            subitem.setProperty("expanded", "N");
            subitem.setProperty("translate", "N");
            subitem.setProperty("image", "");
            PropertyListCollection sis = new PropertyListCollection();
            sis.add(subitem);
            topitem.setProperty("items", sis);
        }
        return topitem;
    }

    private EditorStyleField getVariableField(String field, String mode, String variableid, int realrow, String value, String datatype, String reatedvariableid, String lookupcallback, boolean viewonly) {
        return TaskDefVariables.getVariableField(field, mode, variableid, realrow, value, datatype, reatedvariableid, lookupcallback, viewonly, "", this.getConnectionId(), this.pageContext, this.sdiInfo, this.getTranslationProcessor());
    }

    public static EditorStyleField getVariableField(String field, String mode, String variableid, int realrow, String value, String datatype, String relatedvariableid, String lookupcallback, boolean viewonly, String changeEvent, String connectionId, PageContext pageContext, SDITagInfo sdiInfo, TranslationProcessor tp) {
        PropertyListCollection llcols;
        EditorStyleField esf = new EditorStyleField(pageContext, sdiInfo, connectionId);
        esf.setDatasetname("attribute");
        PropertyList column = new PropertyList();
        column.setProperty("mode", mode);
        esf.setColumnDefinition(field, EditorStyleField.getEditorStyleDataType("C"), 100, false);
        if (mode.equalsIgnoreCase("checkbox")) {
            column.setProperty("displayvalue", "Y=Yes;N=No");
            if (viewonly) {
                esf.setReadonly(true);
            }
        } else if (mode.equalsIgnoreCase("lookup") && field.equalsIgnoreCase("editorstyleid")) {
            if (!viewonly) {
                PropertyList lookuplink = new PropertyList();
                lookuplink.setProperty("href", "rc?command=page&page=LV_EditorStyleLookup");
                lookuplink.setProperty("restrictivewhere", "datatype='" + EditorStyleField.getEditorStyleDataType(datatype) + "'");
                lookuplink.setProperty("tip", tp.translate("Lookup Editor Style"));
                lookuplink.setProperty("dialogtype", "Sapphire Dialog");
                column.setProperty("lookuplink", lookuplink);
                column.setProperty("size", "15");
                esf.setReadonly(true);
                esf.setLinkDefinition("LV_EditorStyle", 'F', "", false);
            } else {
                column.setProperty("mode", "input");
                esf.setReadonly(true);
            }
            column.setProperty("size", "15");
        } else if (viewonly) {
            column.setProperty("mode", "readonly");
        } else if (relatedvariableid.length() > 0 && column.getPropertyList("lookuplink") != null && (llcols = column.getPropertyList("lookuplink").getCollection("columns")) != null && llcols.size() > 0) {
            String[] mapfields = StringUtil.split(relatedvariableid, ";");
            int mf = 0;
            boolean first = true;
            for (int i = 0; i < llcols.size(); ++i) {
                PropertyList llcol = llcols.getPropertyList(i);
                String llmode = llcol.getProperty("mode", "Display and Return");
                if (!llmode.equalsIgnoreCase("Display and Return") && !llmode.equalsIgnoreCase("Return Only")) continue;
                if (first) {
                    llcol.setProperty("mapfieldid", "defaultvalue");
                    first = false;
                    continue;
                }
                llcol.setProperty("mapfieldid", mapfields[mf]);
                if (++mf >= mapfields.length) break;
            }
            if (lookupcallback.length() > 0) {
                column.getPropertyList("lookuplink").setProperty("lookupcallback", lookupcallback);
            }
        }
        esf.setColumn(column);
        esf.setFieldName("variable" + realrow + "_" + field);
        if (changeEvent.length() == 0) {
            esf.setChangeEvent("taskVariables.maintFieldChange(event,this,'" + variableid + "'," + realrow + ")");
        } else {
            esf.setChangeEvent(changeEvent);
        }
        esf.setUseNoNameAttribute(true);
        esf.setFieldValue(value);
        return esf;
    }

    public static String getVariableGrid(PropertyList taskprops, boolean viewonly, boolean setuponly, SDITagInfo sdiInfo, String connectionId, TranslationProcessor translationProcessor, PageContext pageContext, Logger logger) {
        StringBuffer html = new StringBuffer();
        StringBuffer tabhtml = new StringBuffer();
        StringBuffer tab1 = new StringBuffer();
        boolean descendant = setuponly;
        PropertyListCollection variables = taskprops.getCollection("variables");
        html.append("<form name=\"taskdef_variablesform\" action=\"#\">");
        if (variables == null) {
            variables = new PropertyListCollection();
            taskprops.setProperty("variables", variables);
        }
        String relHelp = translationProcessor.translate(RELATIVEVARIABLES_HELP);
        boolean foundVar = false;
        for (int i = 0; i < variables.size(); ++i) {
            PropertyList var = variables.getPropertyList(i);
            String varId = var.getProperty("variableid", "");
            String relatedvariableid = var.getProperty("relatedvariableid", "");
            boolean setup = var.getProperty("setup", "N").equalsIgnoreCase("Y");
            boolean exposed = var.getProperty("exposed", "N").equalsIgnoreCase("Y");
            String defaultvalue = var.getProperty("defaultvalue", "");
            String dataType = var.getProperty("type", "string");
            boolean complex = dataType.equalsIgnoreCase("form") || dataType.equalsIgnoreCase("sdilist") || dataType.equalsIgnoreCase(PROPERTY_SDIDATASTORE);
            String varidrep = StringUtil.replaceAll(varId, " ", "_");
            String editorstyle = var.getProperty("editorstyleid", "");
            if (setup) {
                String prompt = var.getProperty("prompt", "");
                String help = var.getProperty("help", "");
                tabhtml.append("<tr>");
                if (!viewonly && !descendant) {
                    tabhtml.append("<td class=\"gridmaint_field\">");
                    tabhtml.append("<input type=\"checkbox\" name=\"").append("variable").append("_selector").append("").append("\" id=\"__").append("variable").append(i).append("_").append(varidrep).append("\">");
                    tabhtml.append("</td>");
                }
                tabhtml.append("<td id=\"").append("variable").append(i).append("_").append(varidrep).append("_titlecell\" class=\"gridmaint_field\" style=\"\">");
                tabhtml.append(varId).append("");
                tabhtml.append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">");
                tabhtml.append(dataType);
                tabhtml.append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\" nowrap>");
                EditorStyleField esf = null;
                if (editorstyle.length() > 0) {
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
                    foundVar = true;
                    if (defaultvalue.length() > 0) {
                        esf.setFieldValue(defaultvalue);
                    }
                    if (var.getProperty("help", "").length() > 0) {
                        esf.setColumnProperty("tip", var.getProperty("help", ""));
                    }
                    esf.setChangeEvent("taskVariables.maintFieldChange(event,this,'" + varidrep + "'," + i + ")");
                    esf.setFieldName("variable" + i + "_defaultvalue");
                    if (viewonly) {
                        esf.setColumnProperty("disable", "Y");
                    }
                    TaskDefVariables.setEditorStyleFieldMapping(esf, varId, relatedvariableid, "taskVariables.variableLookupCallback");
                    tabhtml.append(esf.getHtml());
                }
                tabhtml.append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("setup", "checkbox", varidrep, i, setup ? "Y" : "N", "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("exposed", "checkbox", varidrep, i, exposed ? "Y" : "N", "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("prompt", "input", varidrep, i, prompt, "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("modifiable", "checkbox", varidrep, i, var.getProperty("modifiable", "Y"), "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("mandatory", "checkbox", varidrep, i, var.getProperty("mandatory", "N"), "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("hidden", "checkbox", varidrep, i, var.getProperty("hidden", "N"), "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("help", "input", varidrep, i, help, "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\" nowrap>").append(TaskDefVariables.getVariableField("editorstyleid", "lookup", varidrep, i, editorstyle, "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\" title=\"").append(relHelp).append("\">").append(TaskDefVariables.getVariableField("relatedvariableid", "input", varidrep, i, relatedvariableid, "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                if (exposed) {
                    tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("description", "input", varidrep, i, var.getProperty("description", ""), "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                } else {
                    tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
                }
                tabhtml.append("</tr>");
                continue;
            }
            tabhtml.append("<tr>");
            if (!viewonly && !descendant) {
                tabhtml.append("<td class=\"gridmaint_field\">");
                tabhtml.append("<input type=\"checkbox\" name=\"").append("variable").append("_selector").append("").append("\" id=\"__").append("variable").append(i).append("_").append(varidrep).append("\">");
                tabhtml.append("</td>");
            }
            tabhtml.append("<td id=\"").append("variable").append(i).append("_").append(varidrep).append("_titlecell\" class=\"gridmaint_field\" style=\"\">");
            tabhtml.append(varId).append("");
            tabhtml.append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">");
            tabhtml.append(dataType);
            tabhtml.append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\" nowrap>");
            if (!complex) {
                EditorStyleField esf = null;
                if (editorstyle.length() > 0) {
                    esf = new EditorStyleField(pageContext, null, connectionId);
                    try {
                        esf.setEditorStyleId(editorstyle);
                        esf.setColumnDefinition(varidrep, EditorStyleField.getEditorStyleDataType(dataType), 100, false, false);
                    }
                    catch (Exception e) {
                        logger.warn("Invalid editor style");
                    }
                } else {
                    esf = new EditorStyleField(pageContext, null, connectionId);
                    String dt = EditorStyleField.getEditorStyleDataType(dataType);
                    esf.setDefaultEditorStyleProperties(dt, "", "");
                    esf.setColumnDefinition(varidrep, dt, 100, false, false);
                }
                if (esf != null) {
                    foundVar = true;
                    if (defaultvalue.length() > 0) {
                        esf.setFieldValue(defaultvalue);
                    }
                    if (var.getProperty("help", "").length() > 0) {
                        esf.setColumnProperty("tip", var.getProperty("help", ""));
                    }
                    esf.setChangeEvent("taskVariables.maintFieldChange(event,this,'" + varidrep + "'," + i + ")");
                    esf.setFieldName("variable" + i + "_defaultvalue");
                    if (viewonly) {
                        esf.setColumnProperty("disable", "Y");
                    }
                    TaskDefVariables.setEditorStyleFieldMapping(esf, varId, relatedvariableid, "taskVariables.variableLookupCallback");
                    tabhtml.append(esf.getHtml());
                }
            }
            tabhtml.append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("setup", "checkbox", varidrep, i, setup ? "Y" : "N", "string", "", "", viewonly || complex, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("exposed", "checkbox", varidrep, i, exposed ? "Y" : "N", "string", "", "", viewonly || complex, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            if (!complex) {
                tabhtml.append("<td class=\"gridmaint_field\" nowrap>").append(TaskDefVariables.getVariableField("editorstyleid", "lookup", varidrep, i, editorstyle, "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\" title=\"").append(relHelp).append("\">").append(TaskDefVariables.getVariableField("relatedvariableid", "input", varidrep, i, relatedvariableid, "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
            } else {
                tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
                tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            }
            if (exposed) {
                tabhtml.append("<td class=\"gridmaint_field\">").append(TaskDefVariables.getVariableField("description", "input", varidrep, i, var.getProperty("description", ""), "string", "", "", viewonly, "", connectionId, pageContext, sdiInfo, translationProcessor).getHtml()).append("</td>");
            } else {
                tabhtml.append("<td class=\"gridmaint_field\">").append("").append("</td>");
            }
            tabhtml.append("</tr>");
            foundVar = true;
        }
        if (!viewonly && !descendant) {
            Button btn = new Button(pageContext);
            btn.setId("btnAddTaskVar");
            btn.setAction("taskVariables.buttons.addVar(document.getElementById('__variable_change'),null)");
            btn.setTip("Add Task Variable");
            btn.setImg("WEB-CORE/images/png/Add.png");
            tab1.append(btn.getHtml());
            tab1.append("&nbsp;");
            btn = new Button(pageContext);
            btn.setId("btnDeleteTaskVar");
            btn.setAction("taskVariables.buttons.deleteVar('')");
            btn.setTip("Delete Task Variable");
            btn.setDisabled(!foundVar);
            btn.setImg("WEB-CORE/images/png/Delete.png");
            tab1.append(btn.getHtml());
            tab1.append("&nbsp;");
            btn = new Button(pageContext);
            btn.setId("btnMoveUpTaskVar");
            btn.setAction("taskVariables.buttons.moveVarUp(event)");
            btn.setTip("Move Task Variable Up");
            btn.setDisabled(!foundVar);
            btn.setImg("WEB-CORE/images/gif/MoveUp.gif");
            tab1.append(btn.getHtml());
            tab1.append("&nbsp;");
            btn = new Button(pageContext);
            btn.setId("btnMoveDownTaskVar");
            btn.setAction("taskVariables.buttons.moveVarDown(event)");
            btn.setTip("Move Task Variable Down");
            btn.setDisabled(!foundVar);
            btn.setImg("WEB-CORE/images/gif/MoveDown.gif");
            tab1.append(btn.getHtml());
        }
        if (foundVar) {
            tab1.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" class=\"gridmaint_table\">");
            tab1.append("<thead>");
            tab1.append("<tr class=\"gridmaint_tablehead\">");
            if (!viewonly && !descendant) {
                tab1.append("<th class=\"gridmaint_fieldtitle\">").append("&nbsp;").append("</th>");
            }
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Variable")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Type")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\" style=\"width:180px;\">").append(translationProcessor.translate("Default Value")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Setup Variable")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Exposed")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Prompt")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Modifiable")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Mandatory")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Hidden")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Help Text")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Editor Style")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Related Variables")).append("</th>");
            tab1.append("<th class=\"gridmaint_fieldtitle\">").append(translationProcessor.translate("Description")).append("</th>");
            tab1.append("</tr>");
            tab1.append("</thead>");
            tab1.append("<tbody>");
            tab1.append(tabhtml);
            tab1.append("</tbody>");
            tab1.append("</table>");
            tab1.append("<br>");
        } else {
            tab1.append("<p>No Task Variables");
        }
        html.append("<section id=\"section_wv\" style=\"display:").append("block").append(";padding-bottom:15px;\">");
        html.append(tab1);
        html.append("</section>");
        html.append("<input type=hidden name=\"__variable_change\" id=\"__variable_change\" onchange=\"taskVariables.ui.refreshVariables()\">");
        html.append("</form>");
        return html.toString();
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.taskprops != null) {
            StringBuffer tabhtml = new StringBuffer();
            StringBuffer tab1 = new StringBuffer();
            html.append(this.getScriptAndStyle());
            html.append("<div id=\"taskvariables_container\" style=\"height:auto;width:auto;overflow:auto;position:absolute;left:0;right:0;bottom:0;top:0;\">");
            html.append(TaskDefVariables.getVariableGrid(this.taskprops, this.viewonly, this.descendant, this.sdiInfo, this.getConnectionId(), this.getTranslationProcessor(), this.pageContext, this.logger));
            html.append("</div>");
            html.append(this.getEndScript(this.viewonly));
        } else {
            html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate("Could not load properties for variables.")).append("</font>");
        }
        return html.toString();
    }

    public static enum Grouping {
        TYPE("Data Type"),
        USAGE("Usage"),
        SCOPE("Scope"),
        NONE("None");

        private String title;

        private Grouping(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }
    }
}

