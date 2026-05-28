/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.opal.elements.advancedtoolbar.AdvancedToolbar;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.workflow.StepUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.pageelements.maint.DataView;
import com.labvantage.sapphire.pageelements.propertybuilder.PropertyBuilder;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefPainter;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefVariables;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflowProperties;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.MiscUtil;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.InputStream;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefProperties
extends BaseElement {
    public static final String USERCONFIG_PREFIX = "taskprops_";
    public static final String JS_CLASS = "taskProps";
    public static final String XMLFILE = "buttons.xml";
    public static final String CACHE = "taskdef_buttons";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_ID1 = "id1";
    public static final String PROPERTY_ID2 = "id2";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_VIEWONLY = "viewonly";
    public static final String PROPERTY_DESCENDANT = "descendant";
    public static final String PROPERTY_SELECTEDBUTTON = "selectedbutton";
    private PropertyList userConfig;
    private boolean viewonly = false;
    private boolean descendant = false;
    private boolean devMode;
    private PropertyList fullprops = null;
    private PropertyList parentfullprops = null;
    private PropertyList childfullprops = null;
    private PropertyList parentstepprops = null;
    private PropertyList stepprops = null;
    private PropertyList elementprops = null;
    private PropertyList childstepprops = null;
    private PropertyList variableprops = null;
    private PropertyList ioprops = null;
    private PropertyList tranistionprops = null;
    private ItemType type = ItemType.STEP;
    private String id1 = "";
    private String id2 = "";
    private String transitionid = "";
    private String selectedBtn = "";
    private int selectedTab = -1;

    public TaskDefProperties(PageContext pageContext, PropertyList pageproperties) {
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
            this.parentstepprops = null;
            this.childfullprops = null;
            this.stepprops = null;
            this.elementprops = null;
            this.childstepprops = null;
            this.tranistionprops = null;
            this.variableprops = null;
            this.ioprops = null;
            this.logger.error("Could not set up painter: " + e.getMessage(), e);
        }
        this.logger.debug("Set up completed.");
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        String taskp;
        pagedata.setProperty("jsrequest", "exclude=properties");
        this.id1 = pagedata.getProperty(PROPERTY_ID1, "");
        this.logger.debug("id1 = " + this.id1);
        this.id2 = pagedata.getProperty(PROPERTY_ID2, "");
        this.logger.debug("id2 = " + this.id2);
        this.selectedBtn = pagedata.getProperty(PROPERTY_SELECTEDBUTTON, "");
        this.logger.debug("selectedBtn = " + this.selectedBtn);
        this.viewonly = pagedata.getProperty(PROPERTY_VIEWONLY, "n").equalsIgnoreCase("y");
        this.descendant = pagedata.getProperty(PROPERTY_DESCENDANT, "n").equalsIgnoreCase("y");
        if (pagedata.containsKey("selectedtab")) {
            try {
                this.selectedTab = Integer.parseInt(pagedata.getProperty("selectedtab", "-1"));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            this.type = ItemType.valueOf(pagedata.getProperty(PROPERTY_TYPE, "step").toUpperCase());
        }
        catch (Exception e) {
            this.type = ItemType.STEP;
        }
        this.logger.debug("type = " + this.type.toString());
        if (this.id1.length() > 0 && (this.type != ItemType.TRANSITION || this.id2.length() > 0) && (taskp = pagedata.getProperty(PROPERTY_PROPERTIES, "")).length() > 0) {
            try {
                PropertyListCollection taskio;
                this.fullprops = new PropertyList(new JSONObject(taskp));
                this.childfullprops = this.descendant ? this.fullprops.getPropertyList("childprops") : null;
                PropertyList propertyList = this.parentfullprops = this.descendant ? this.fullprops.getPropertyList("parentprops") : null;
                if (this.type == ItemType.STEP || this.type == ItemType.TRANSITION) {
                    PropertyListCollection steps = this.fullprops.getCollection("steps");
                    if (steps != null) {
                        this.stepprops = steps.find("stepid", this.id1);
                        if (this.stepprops != null) {
                            PropertyList propertyList2 = this.descendant ? (this.childfullprops != null && this.childfullprops.getCollection("steps") != null ? this.childfullprops.getCollection("steps").find("stepid", this.id1) : null) : (this.childstepprops = null);
                            PropertyList propertyList3 = this.descendant ? (this.parentfullprops != null && this.parentfullprops.getCollection("steps") != null ? this.parentfullprops.getCollection("steps").find("stepid", this.id1) : null) : (this.parentstepprops = null);
                            if (this.type == ItemType.TRANSITION) {
                                PropertyList next = this.stepprops.getPropertyList("next");
                                if (next != null) {
                                    PropertyListCollection trans = next.getCollection("transitions");
                                    if (trans != null) {
                                        this.tranistionprops = trans.find("stepid", this.id2);
                                        if (this.tranistionprops != null) {
                                            this.transitionid = this.tranistionprops.getProperty("transitionid", "");
                                        } else {
                                            this.logger.warn("Could not find transition to step " + this.id2);
                                        }
                                    } else {
                                        this.logger.warn("Could not find transitions");
                                    }
                                } else {
                                    this.logger.warn("Could not find next properties");
                                }
                            }
                        } else {
                            this.logger.warn("Could not find step " + this.id1);
                        }
                    } else {
                        this.logger.warn("Could not find steps");
                    }
                } else if (this.type == ItemType.ELEMENT) {
                    PropertyListCollection elements = this.fullprops.getCollection("elements");
                    if (elements != null) {
                        this.elementprops = elements.find("elementid", this.id1);
                        if (this.elementprops == null) {
                            this.logger.warn("Could not find element " + this.id1);
                        }
                    }
                } else if (this.type == ItemType.VARIABLE) {
                    PropertyListCollection vars = this.fullprops.getCollection("variables");
                    if (vars != null) {
                        this.variableprops = vars.find("variableid", this.id1);
                        if (this.variableprops == null) {
                            this.logger.warn("Could not find variable " + this.id1);
                        }
                    }
                } else if (this.type == ItemType.IO && (taskio = this.fullprops.getCollection("taskio")) != null) {
                    this.ioprops = taskio.find("ioid", this.id1);
                    if (this.ioprops == null) {
                        this.logger.warn("Could not find IO " + this.id1);
                    }
                }
            }
            catch (Exception e) {
                this.fullprops = null;
                this.parentfullprops = null;
                this.childfullprops = null;
                this.stepprops = null;
                this.elementprops = null;
                this.childstepprops = null;
                this.parentstepprops = null;
                this.tranistionprops = null;
                this.variableprops = null;
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
        html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, "", !this.devMode, this.pageContext));
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/taskprops.js\"></script>");
        html.append("<script type=\"text/javascript\" src=\"WEB-CORE/elements/workflow/scripts/taskscriptapi.js\"></script>");
        html.append("\n<style>");
        html.append("\n.task_longtext { width:400px; }");
        html.append("\n.tab_standard_body { vertical-align:top; }");
        html.append("\n.task_field_dragover{border:solid 2px #FFD691;background-color:#FFF2BE;}");
        html.append("\n</style>");
        return html;
    }

    private StringBuffer getEndScript() {
        StringBuffer html = new StringBuffer();
        html.append("<script type=\"text/javascript\">");
        html.append(JS_CLASS).append(".type = '").append(this.type.toString().toLowerCase()).append("';");
        html.append(JS_CLASS).append(".id1 = '").append(this.id1).append("';");
        html.append(JS_CLASS).append(".id2 = '").append(this.id2).append("';");
        html.append(JS_CLASS).append(".viewonly = ").append(this.viewonly).append(";");
        html.append(JS_CLASS).append(".descendant = ").append(this.descendant).append(";");
        html.append("</script>");
        return html;
    }

    private StringBuffer getStepStepTab() {
        StringBuffer html = new StringBuffer();
        boolean fullViewOnly = this.viewonly || this.descendant;
        boolean selectedIsChild = false;
        DataSet dsFake = new DataSet();
        dsFake.addColumn("stepid", 0);
        dsFake.addColumn("title", 0);
        dsFake.addColumn("shorttitle", 0);
        dsFake.addColumn("instructions", 3);
        dsFake.addColumn("summary", 0);
        dsFake.addColumn("loadedscript", 0);
        dsFake.addColumn("stepgroupid", 0);
        dsFake.addColumn("allowcancel", 0);
        String stepid = this.stepprops.getProperty("stepid", "");
        dsFake.addRow();
        dsFake.setValue(0, "stepid", stepid);
        dsFake.setValue(0, "title", this.stepprops.getProperty("title", ""));
        dsFake.setValue(0, "shorttitle", this.stepprops.getProperty("shorttitle", ""));
        dsFake.setValue(0, "instructions", this.stepprops.getProperty("instructions", ""));
        dsFake.setValue(0, "summary", this.stepprops.getProperty("summary", ""));
        dsFake.setValue(0, "loadedscript", this.stepprops.getProperty("loadedscript", ""));
        dsFake.setValue(0, "stepgroupid", this.stepprops.getProperty("stepgroupid", ""));
        dsFake.setValue(0, "allowcancel", this.stepprops.getProperty("allowcancel", "Y"));
        DataView maint = new DataView(this.pageContext, "primary", dsFake, "", this.getConnectionId());
        maint.setElementid("task_step_dataview");
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
        if (!this.viewonly) {
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "taskProps.maintFieldChange(event,this,'step','" + this.id1 + "','','')");
            events.add(event);
        }
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "stepid");
        column.setProperty("title", "Step Id");
        column.setProperty("mode", "readonly");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "title");
        column.setProperty("title", "Execution Title");
        column.setProperty("mode", !this.viewonly ? "input" : "readonly");
        column.setProperty("events", events);
        PropertyListCollection steptypes = TaskDefPainter.getStepTypes(this.getSDIProcessor(), new WebAdminProcessor(this.getConnectionId()), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        String steptypeid = this.stepprops.getProperty("propertytreeid");
        PropertyList steptype = steptypes.find("steptypeid", steptypeid);
        if (steptype == null || !MiscUtil.MiscArray.isStringInArray(StepUtil.getAutoExecuteStepTypes(), steptype.getProperty("steptype"), false)) {
            column.setProperty("class", "task_longtext mandatoryfield");
        } else {
            column.setProperty("class", "task_longtext");
        }
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "shorttitle");
        column.setProperty("title", "Icon Text");
        column.setProperty("mode", !this.viewonly ? "input" : "readonly");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "stepgroupid");
        column.setProperty("title", "Stage Id");
        ArrayList<String> groups = new ArrayList<String>();
        if (this.fullprops != null && this.fullprops.getCollection("stages") != null) {
            PropertyListCollection stages = this.fullprops.getCollection("stages");
            for (int i = 0; i < stages.size(); ++i) {
                String currGroup = stages.getPropertyList(i).getProperty("stageid", "");
                if (currGroup.length() <= 0 || groups.contains(currGroup)) continue;
                groups.add(currGroup);
            }
        }
        StringBuffer displayvalue = new StringBuffer();
        for (String group : groups) {
            if (displayvalue.length() > 0) {
                displayvalue.append(";");
            }
            displayvalue.append(group).append("=").append(group);
        }
        column.setProperty("mode", !fullViewOnly ? (groups.size() > 0 ? "dropdownlist" : "readonly") : "readonly");
        if (groups.size() == 0) {
            column.setProperty("pseudocolumn", this.getTranslationProcessor().translate("No Stages Defined"));
        }
        column.setProperty("displayvalue", displayvalue.toString());
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "instructions");
        column.setProperty("title", "Instructions");
        column.setProperty("mode", !this.viewonly ? "formattedtext" : "html");
        column.setProperty("size", "auto;80");
        column.setProperty("colspan", "2");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "summary");
        column.setProperty("title", "Summary<br>Statement");
        column.setProperty("mode", !this.viewonly ? "inputarea" : "readonly");
        column.setProperty("events", events);
        column.setProperty("class", "task_longtext");
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "loadedscript");
        column.setProperty("title", "Loaded Script");
        column.setProperty("mode", !fullViewOnly ? "inputarea" : "readonly");
        PropertyList lookup = new PropertyList();
        lookup.setProperty("href", "javascript:taskProps.lookupScript(this,'" + column.getProperty("columnid") + "')");
        lookup.setProperty("tip", "Edit Script");
        lookup.setProperty("img", "WEB-CORE/elements/images/ellipsisblank.gif");
        lookup.setProperty("style", "button");
        column.setProperty("lookuplink", lookup);
        column.setProperty("events", events);
        column.setProperty("class", "task_longtext");
        columns.add(column);
        if (this.fullprops.getProperty("allowcancel", "Y").equalsIgnoreCase("N")) {
            column = new PropertyList();
            column.setProperty("columnid", "allowcancel");
            column.setProperty("title", "Allow Cancel");
            column.setProperty("pseudocolumn", this.getTranslationProcessor().translate("Not Cancellable Set At Task Level"));
            column.setProperty("events", events);
            columns.add(column);
        } else {
            column = new PropertyList();
            column.setProperty("columnid", "allowcancel");
            column.setProperty("title", "Allow Cancel");
            column.setProperty("mode", !fullViewOnly ? "checkbox" : "readonly");
            column.setProperty("displayvalue", "Y=Yes;N=No");
            column.setProperty("events", events);
            columns.add(column);
        }
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append("<div").append(this.browser.isIE() ? "" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
        if (!this.viewonly && !this.descendant) {
            Button btn = new Button(this.pageContext);
            btn.setId("btnStartStep");
            btn.setAction("taskProps.setStartStep()");
            btn.setText("Set as Start Step");
            btn.setDisabled(this.fullprops.getProperty("startstepid", "").equals(stepid) || fullViewOnly);
            btn.setImg("WEB-CORE/images/png/Forward.png");
            html.append(btn.getHtml());
        }
        html.append(maint.getHtml());
        html.append("</div>");
        return html;
    }

    private StringBuffer getVariablesVariableTab() {
        StringBuffer html = new StringBuffer();
        PropertyListCollection events = new PropertyListCollection();
        String lookupcallback = "";
        if (!this.viewonly) {
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "taskProps.maintFieldChange(event,this,'variable','" + this.id1 + "','','')");
            events.add(event);
            lookupcallback = "taskProps.variableLookupCallback";
        }
        html.append(TaskDefVariables.getVariablePropertiesHTML(this.fullprops, this.id1, this.viewonly, this.descendant, events, lookupcallback, this.getConnectionId(), this.pageContext, this.logger));
        return html;
    }

    private StringBuffer getTaskIOsIOTab() {
        StringBuffer html = new StringBuffer();
        PropertyListCollection events = new PropertyListCollection();
        if (!this.viewonly) {
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "taskProps.maintFieldChange(event,this,'io','" + this.id1 + "','','')");
            events.add(event);
        }
        html.append(TaskDefWorkflowProperties.getIOHtml(this.fullprops, this.ioprops, this.viewonly, this.descendant, events, this.getConnectionId(), this.getSDIProcessor(), this.pageContext));
        return html;
    }

    private StringBuffer getStepConfigTab() {
        StringBuffer html = new StringBuffer();
        boolean fullViewOnly = this.viewonly || this.descendant;
        html.append("<iframe name=\"taskdef_stepconfig_frame\" id=\"taskdef_stepconfig_frame\" frameborder=0 scrolling=false src=\"").append(this.browser.getBlankSrc()).append("\" style=\"width:100%;height:100%;\"></iframe>");
        html.append("<form style=\"display:none\" method=\"post\" id=\"taskdef_stepconfig_form\" name=\"taskdef_stepconfig_form\" action=\"").append("rc?command=file&file=WEB-CORE/elements/workflow/taskdefproperties_steptype.jsp").append("\" target=\"taskdef_stepconfig_frame\">");
        html.append("<input type=\"hidden\" name=\"").append("stepid").append("\" value=\"").append(this.id1).append("\">");
        html.append("<input type=\"hidden\" name=\"").append("steptypeid").append("\" value=\"").append(this.stepprops.getProperty("propertytreeid")).append("\">");
        html.append("<input type=\"hidden\" name=\"").append("steptypenode").append("\" value=\"").append(this.stepprops.getProperty("extendnodeid")).append("\">");
        html.append("<input type=\"hidden\" name=\"").append(PROPERTY_VIEWONLY).append("\" value=\"").append(fullViewOnly ? "Y" : "N").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("advanced").append("\" value=\"").append("N").append("\">");
        html.append("<input type=\"hidden\" name=\"").append("readonly").append("\" value=\"").append(fullViewOnly ? "Y" : "N").append("\">");
        html.append("<textarea style=\"display:none;\" name=\"").append("properties\">").append(this.stepprops != null ? this.stepprops.toJSONString(true) : "{}").append("</textarea>");
        html.append("</form>");
        html.append("<script>taskdef_stepconfig_form.submit();</script>");
        return html;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PropertyList getDefaultStepButtons(boolean devMode, Logger logger, HttpSession session) {
        PropertyList out;
        block8: {
            out = new PropertyList();
            Object ob = session.getAttribute(CACHE);
            if (ob == null || !(ob instanceof PropertyDefinitionList) || devMode) {
                logger.debug("Buttons not in cache, thus load.");
                try {
                    InputStream is = PropertyBuilder.getResourceStream(XMLFILE, logger, TaskDefProperties.class);
                    if (is != null) {
                        try {
                            out.setPropertyList(FileUtil.getInputStreamString(is));
                            break block8;
                        }
                        finally {
                            is.close();
                        }
                    }
                    logger.warn("Property definition could be obtained");
                }
                catch (Exception e) {
                    logger.warn("Could not load xml resource. File could not be parsed. " + e.getMessage());
                }
            } else {
                logger.debug("Obtained buttons from cache.");
            }
        }
        return out;
    }

    private StringBuffer getStepToolbarTab() {
        StringBuffer html = new StringBuffer();
        StringBuffer script = new StringBuffer();
        html.append(TaskDefProperties.getStepToolbarTab(this.id1, this.selectedBtn, this.stepprops, this.parentstepprops, this.viewonly, this.descendant, this.pageContext, this.getConnectionId(), script));
        if (script.length() > 0) {
            html.append("<script>");
            html.append("sapphire.events.attachEvent(window,'onload', function(){");
            html.append(script);
            html.append("});");
            html.append("</script>");
        }
        return html;
    }

    public static StringBuffer getStepToolbarTab(String fromstepid, String selectedBtn, PropertyList stepprops, PropertyList parentStepProps, boolean viewonly, boolean descendant, PageContext pageContext, String connectionId, StringBuffer script) {
        StringBuffer html = new StringBuffer();
        PropertyList selectedButtonProps = null;
        boolean fullViewOnly = viewonly;
        Browser browser = new Browser(pageContext);
        html.append("<div").append(browser.isIE() ? "" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
        AdvancedToolbar at = new AdvancedToolbar();
        at.setPageContext(pageContext);
        at.setElementid("advancedtoolbar");
        PropertyList toolbar = new PropertyList();
        toolbar.setProperty("rendermode", "Button");
        toolbar.setProperty("pagetitle", "");
        toolbar.setProperty("showtitle", "N");
        toolbar.setProperty("displaystyle", "Modern");
        PropertyListCollection buttons = new PropertyListCollection();
        if (script == null) {
            script = new StringBuffer();
        }
        boolean selectedIsChild = false;
        PropertyListCollection stepBtns = stepprops.getCollection("buttons");
        if (stepBtns == null) {
            stepBtns = new PropertyListCollection();
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < stepBtns.size(); ++i) {
            PropertyList stepBtn = stepBtns.getPropertyList(i);
            boolean childbtn = false;
            if (descendant && parentStepProps != null && parentStepProps.getCollection("buttons") != null) {
                childbtn = parentStepProps.getCollection("buttons").find("buttonid", stepBtn.getProperty("buttonid")) == null;
            }
            PropertyList btn = new PropertyList();
            btn.setProperty("id", stepBtn.getProperty("buttonid", ""));
            btn.setProperty("buttontype", "User");
            PropertyList common = new PropertyList();
            common.setProperty("text", stepBtn.getProperty("text") + (descendant && !childbtn ? " (D)" : ""));
            common.setProperty("image", stepBtn.getProperty("image"));
            common.setProperty("group", "Navigation");
            common.setProperty("ribbonstyle", "Large");
            btn.setProperty("commonprops", common);
            PropertyList user = new PropertyList();
            user.setProperty("action", "taskProps.buttonClick('" + fromstepid + "','" + btn.getProperty("id", "") + "')");
            user.setProperty("releaselock", "N");
            btn.setProperty("userbuttonprops", user);
            buttons.add(btn);
            Button btnEl = new Button(pageContext);
            btnEl.setAction("taskProps.buttonClick('" + fromstepid + "','" + btn.getProperty("id", "") + "')");
            btnEl.setText(stepBtn.getProperty("text"));
            btnEl.setElementid(btn.getProperty("id", ""));
            btnEl.setId(btn.getProperty("id", ""));
            btnEl.setImg(stepBtn.getProperty("image"));
            boolean selected = false;
            if (selectedBtn.length() == 0 && i == 0 && stepBtn.getProperty("buttonid", "").length() > 0) {
                selectedBtn = stepBtn.getProperty("buttonid");
                selectedButtonProps = stepBtn;
                selected = true;
            } else if (selectedBtn.equals(stepBtn.getProperty("buttonid"))) {
                selectedButtonProps = stepBtn;
                selected = true;
            }
            if (selected) {
                if (childbtn && descendant) {
                    selectedIsChild = true;
                }
                btnEl.setStyle("border-color: #005E8A;border-width:2px;" + (descendant && !childbtn ? "color:grey;background-color:#FFFFFF;" : ""));
            } else {
                btnEl.setStyle("" + (descendant && !childbtn ? "color:grey;background-color:#FFFFFF;" : ""));
            }
            buffer.append(btnEl.getHtml());
        }
        toolbar.setProperty("buttons", buttons);
        at.setElementProperties(toolbar);
        if (!viewonly) {
            Button bt = new Button(pageContext);
            bt.setId("__btTaskPropsAddBtn");
            bt.setImg("WEB-CORE/images/png/Add.png");
            bt.setTip("Add new button");
            bt.setAction("taskProps.buttons.addButton('" + fromstepid + "')");
            html.append(bt.getHtml());
            html.append("&nbsp;");
            bt = new Button(pageContext);
            bt.setId("__btTaskPropsRemoveBtn");
            bt.setImg("WEB-CORE/images/png/Delete.png");
            bt.setTip("Remove selected button");
            if (selectedBtn.length() == 0 || descendant && !selectedIsChild) {
                bt.setDisabled(true);
                bt.setStyle("opacity:0.3;");
            } else {
                bt.setAction("taskProps.buttons.deleteButton('" + fromstepid + "')");
            }
            html.append(bt.getHtml());
            html.append("&nbsp;");
            bt = new Button(pageContext);
            bt.setId("__btTaskPropsMoveLBtn");
            bt.setImg("WEB-CORE/images/png/MoveLeft.png");
            bt.setTip("Move selected button left");
            if (selectedBtn.length() == 0) {
                bt.setDisabled(true);
                bt.setStyle("opacity:0.3;");
            } else {
                bt.setAction("taskProps.buttons.moveButtonLeft('" + fromstepid + "')");
            }
            html.append(bt.getHtml());
            html.append("&nbsp;");
            bt = new Button(pageContext);
            bt.setId("__btTaskPropsMoveRBtn");
            bt.setImg("WEB-CORE/images/png/MoveRight.png");
            bt.setTip("Move selected button left");
            if (selectedBtn.length() == 0) {
                bt.setDisabled(true);
                bt.setStyle("opacity:0.3;");
            } else {
                bt.setAction("taskProps.buttons.moveButtonRight('" + fromstepid + "')");
            }
            html.append(bt.getHtml());
        }
        html.append("<div style=\"padding-top: 5px; padding-bottom: 5px;\">");
        html.append(buffer.toString());
        html.append("</div>");
        if (selectedButtonProps != null) {
            DataSet dsFake = new DataSet();
            dsFake.addColumn("buttonid", 0);
            dsFake.addColumn("text", 0);
            dsFake.addColumn("image", 0);
            dsFake.addColumn("callbeforeaction", 0);
            dsFake.addColumn("action", 0);
            dsFake.addColumn("callbackaction", 0);
            dsFake.addColumn("callbackoperation", 0);
            dsFake.addColumn("title", 0);
            dsFake.addColumn("show", 0);
            dsFake.addColumn("rolelist", 0);
            dsFake.addRow();
            dsFake.setValue(0, "buttonid", selectedButtonProps.getProperty("buttonid"));
            dsFake.setValue(0, "text", selectedButtonProps.getProperty("text"));
            dsFake.setValue(0, "image", selectedButtonProps.getProperty("image"));
            dsFake.setValue(0, "callbeforeaction", selectedButtonProps.getProperty("callbeforeaction"));
            dsFake.setValue(0, "action", selectedButtonProps.getProperty("action"));
            dsFake.setValue(0, "callbackaction", selectedButtonProps.getProperty("callbackaction"));
            dsFake.setValue(0, "callbackoperation", selectedButtonProps.getProperty("callbackoperation"));
            dsFake.setValue(0, "title", selectedButtonProps.getProperty("title"));
            dsFake.setValue(0, "show", selectedButtonProps.getProperty("show", "Y"));
            dsFake.setValue(0, "rolelist", selectedButtonProps.getAttribute("rolelist"));
            DataView maint = new DataView(pageContext, "primary", dsFake, "", connectionId);
            maint.setElementid("task_toolbar_dataview");
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
            PropertyListCollection events = new PropertyListCollection();
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "taskProps.buttonFieldChange(event,this,'" + fromstepid + "','" + selectedBtn + "')");
            events.add(event);
            PropertyList column = new PropertyList();
            column.setProperty("columnid", "text");
            column.setProperty("title", "Text");
            column.setProperty("mode", !viewonly && (!descendant || selectedIsChild) ? "input" : "readonly");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "buttonid");
            column.setProperty("title", "Id");
            column.setProperty("mode", !viewonly && (!descendant || selectedIsChild) ? "readonly" : "readonly");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "image");
            column.setProperty("title", "Image");
            column.setProperty("mode", !viewonly && (!descendant || selectedIsChild) ? "image" : "readonly");
            column.setProperty("events", events);
            column.setProperty("class", "task_longtext");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "title");
            column.setProperty("title", "Tip");
            column.setProperty("mode", !viewonly && (!descendant || selectedIsChild) ? "input" : "readonly");
            column.setProperty("events", events);
            column.setProperty("class", "task_longtext");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "callbeforeaction");
            column.setProperty("title", "Callbefore Action");
            column.setProperty("mode", !viewonly && (!descendant || selectedIsChild) ? "inputarea" : "readonly");
            PropertyList lookup = new PropertyList();
            lookup.setProperty("href", "javascript:taskProps.lookupScript(this,'" + column.getProperty("columnid") + "')");
            lookup.setProperty("tip", "Edit Script");
            lookup.setProperty("img", "WEB-CORE/elements/images/ellipsisblank.gif");
            lookup.setProperty("style", "button");
            column.setProperty("lookuplink", lookup);
            column.setProperty("events", events);
            column.setProperty("class", "task_longtext");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "action");
            column.setProperty("title", "Action");
            column.setProperty("mode", !viewonly && (!descendant || selectedIsChild) ? "inputarea" : "readonly");
            lookup = new PropertyList();
            lookup.setProperty("href", "javascript:taskProps.lookupScript(this,'" + column.getProperty("columnid") + "')");
            lookup.setProperty("tip", "Edit Script");
            lookup.setProperty("img", "WEB-CORE/elements/images/ellipsisblank.gif");
            lookup.setProperty("style", "button");
            column.setProperty("lookuplink", lookup);
            column.setProperty("events", events);
            column.setProperty("class", "task_longtext");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "callbackaction");
            column.setProperty("title", "Callback Action");
            column.setProperty("mode", !viewonly && (!descendant || selectedIsChild) ? "inputarea" : "readonly");
            lookup = new PropertyList();
            lookup.setProperty("href", "javascript:taskProps.lookupScript(this,'" + column.getProperty("columnid") + "')");
            lookup.setProperty("tip", "Edit Script");
            lookup.setProperty("img", "WEB-CORE/elements/images/ellipsisblank.gif");
            lookup.setProperty("style", "button");
            column.setProperty("lookuplink", lookup);
            column.setProperty("events", events);
            column.setProperty("class", "task_longtext");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "callbackoperation");
            column.setProperty("title", "Callback Operation");
            column.setProperty("mode", !viewonly && (!descendant || selectedIsChild) ? "inputarea" : "readonly");
            column.setProperty("events", events);
            column.setProperty("class", "task_longtext");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "show");
            column.setProperty("title", "Show");
            column.setProperty("displayvalue", "Y=Yes;N=No");
            column.setProperty("mode", !viewonly ? "checkbox" : "readonly");
            column.setProperty("events", events);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "rolelist");
            column.setProperty("title", "Roles");
            column.setProperty("mode", !fullViewOnly ? "inputarea" : "readonly");
            lookup = new PropertyList();
            lookup.setProperty("href", "javascript:taskProps.lookupRoles(this)");
            lookup.setProperty("tip", "Modify Roles");
            lookup.setProperty("img", "WEB-CORE/elements/images/ellipsisblank.gif");
            lookup.setProperty("style", "button");
            column.setProperty("lookuplink", lookup);
            column.setProperty("forcereadonly", "Y");
            column.setProperty("events", events);
            column.setProperty("class", "task_longtext");
            columns.add(column);
            maintProps.setProperty("columns", columns);
            maint.setElementProperties(maintProps);
            html.append("<div").append(browser.isIE() ? "" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
            html.append(maint.getHtml());
            html.append("</div>");
        }
        html.append("</div>");
        if (selectedBtn.length() > 0) {
            script.append(JS_CLASS).append(".selectButton('").append(fromstepid).append("','").append("").append(selectedBtn).append("');");
        }
        return html;
    }

    private StringBuffer getStepTransitionTab() {
        PropertyListCollection trans;
        StringBuffer html = new StringBuffer();
        boolean fullViewOnly = this.viewonly || this.descendant;
        DataSet dsFake = new DataSet();
        dsFake.addColumn("caseon", 0);
        dsFake.addRow();
        dsFake.setValue(0, "caseon", this.stepprops.getPropertyList("next") != null ? this.stepprops.getPropertyList("next").getProperty("caseon", "") : "");
        DataView maint = new DataView(this.pageContext, "primary", dsFake, "", this.getConnectionId());
        maint.setElementid("task_transition_pri_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "Form");
        maintProps.setProperty("formcols", "1");
        if (fullViewOnly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        if (!fullViewOnly) {
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "taskProps.maintFieldChange(event,this,'step','" + this.id1 + "','','next')");
            events.add(event);
        }
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "caseon");
        column.setProperty("title", "Transition Case Expression");
        column.setProperty("mode", !fullViewOnly ? "inputarea" : "readonly");
        PropertyList lookup = new PropertyList();
        lookup.setProperty("href", "javascript:taskProps.lookupGroovy(this)");
        lookup.setProperty("tip", "Edit Groovy");
        lookup.setProperty("img", "WEB-CORE/elements/images/ellipsisblank.gif");
        lookup.setProperty("style", "button");
        column.setProperty("lookuplink", lookup);
        column.setProperty("events", events);
        column.setProperty("class", "task_longtext");
        columns.add(column);
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append("<div").append(this.browser.isIE() ? "" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
        html.append(maint.getHtml());
        PropertyListCollection propertyListCollection = trans = this.stepprops.getPropertyList("next") != null ? this.stepprops.getPropertyList("next").getCollection("transitions") : null;
        if (trans != null && trans.size() > 0) {
            String stepid;
            DataSet dsCases = new DataSet();
            dsCases.addColumn("transitionid", 0);
            dsCases.addColumn("text", 0);
            dsCases.addColumn("stepid", 0);
            dsCases.addColumn("steptext", 0);
            dsCases.addColumn("oldstepid", 0);
            dsCases.addColumn("case", 0);
            dsCases.addColumn("oldcase", 0);
            PropertyListCollection steps = this.fullprops.getCollection("steps");
            for (int i = 0; i < trans.size(); ++i) {
                int r = dsCases.addRow();
                PropertyList tran = trans.getPropertyList(i);
                dsCases.setValue(r, "transitionid", tran.getProperty("transitionid", ""));
                dsCases.setValue(r, "text", tran.getProperty("text", ""));
                stepid = tran.getProperty("stepid", "");
                PropertyList step = steps != null ? steps.find("stepid", tran.getProperty("stepid", "")) : null;
                String steptext = step != null ? step.getProperty("shorttitle", step.getProperty("title")) : "";
                steptext = steptext.length() > 0 ? steptext + " (" + stepid + ")" : stepid;
                dsCases.setValue(r, "stepid", stepid);
                dsCases.setValue(r, "oldstepid", stepid);
                dsCases.setValue(r, "steptext", steptext);
                dsCases.setValue(r, "case", tran.getProperty("case", ""));
                dsCases.setValue(r, "oldcase", tran.getProperty("case", ""));
            }
            StringBuffer stepslist = new StringBuffer();
            for (int i = 0; i < steps.size(); ++i) {
                PropertyList step = steps.getPropertyList(i);
                if (stepslist.length() > 0) {
                    stepslist.append(";");
                }
                if ((stepid = step.getProperty("stepid", "")).equalsIgnoreCase(this.stepprops.getProperty("stepid"))) continue;
                String steptext = step.getProperty("shorttitle", step.getProperty("title"));
                steptext = steptext.length() > 0 ? steptext + " (" + stepid + ")" : stepid;
                stepslist.append(stepid).append("=").append(steptext);
            }
            DataView detail = new DataView(this.pageContext, "transition", dsCases, "", this.getConnectionId());
            detail.setElementid("task_transition_det_dataview");
            detail.setSDCId("LV_TaskDef");
            detail.getSDIInfo().setSdcid("LV_TaskDef");
            PropertyList detailProps = new PropertyList();
            detailProps.setProperty("sdcid", "LV_TaskDef");
            detailProps.setProperty("style", "Grid");
            PropertyListCollection detailColumns = new PropertyListCollection();
            PropertyListCollection detailEvents = new PropertyListCollection();
            if (!fullViewOnly) {
                PropertyList detailEvent = new PropertyList();
                detailEvent.setProperty("event", "onchange");
                detailEvent.setProperty("js", "taskProps.maintFieldChange(event,this,'step','" + this.id1 + "','transition[rowid]','next.transitions')");
                detailEvents.add(detailEvent);
            }
            PropertyList detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "stepid");
            detailColumn.setProperty("title", "Target Step");
            detailColumn.setProperty("tip", "[steptext]");
            detailColumn.setProperty("mode", !fullViewOnly ? "dropdownlist" : "readonly");
            if (!fullViewOnly) {
                detailColumn.setProperty("displayvalue", stepslist.toString());
                detailColumn.setProperty("events", detailEvents);
            }
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "oldstepid");
            detailColumn.setProperty("mode", "hidden");
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "case");
            detailColumn.setProperty("title", "Case");
            detailColumn.setProperty("mode", !fullViewOnly ? "input" : "readonly");
            detailColumn.setProperty("events", detailEvents);
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "oldcase");
            detailColumn.setProperty("mode", "hidden");
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "text");
            detailColumn.setProperty("title", "Label");
            detailColumn.setProperty("mode", !fullViewOnly ? "input" : "readonly");
            detailColumn.setProperty("events", detailEvents);
            detailColumns.add(detailColumn);
            detailProps.setProperty("columns", detailColumns);
            detail.setElementProperties(detailProps);
            html.append("<div").append(this.browser.isIE() ? " style=\"padding-top:10px;\"" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
            html.append(detail.getHtml());
            html.append("</div>");
        }
        html.append("</div>");
        return html;
    }

    private StringBuffer getStepIncludesTab() {
        boolean fullViewOnly;
        StringBuffer html = new StringBuffer();
        boolean bl = fullViewOnly = this.viewonly || this.descendant;
        if (!fullViewOnly) {
            Button bt = new Button(this.pageContext);
            bt.setId("__btTaskPropsAddInc");
            bt.setImg("WEB-CORE/images/png/Add.png");
            bt.setTip("Add new include");
            bt.setAction("taskProps.buttons.addInclude('" + this.id1 + "')");
            html.append(bt.getHtml());
            html.append("&nbsp;");
            bt = new Button(this.pageContext);
            bt.setId("__btTaskPropsRemoveInc");
            bt.setImg("WEB-CORE/images/png/Delete.png");
            bt.setTip("Remove selected include");
            bt.setAction("taskProps.buttons.deleteInclude('" + this.id1 + "')");
            html.append(bt.getHtml());
        }
        html.append("<div").append(this.browser.isIE() ? "" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
        DataSet dsIncludes = new DataSet();
        dsIncludes.addColumn("includeid", 0);
        dsIncludes.addColumn("href", 0);
        PropertyListCollection includes = this.stepprops.getCollection("includes");
        if (includes != null && includes.size() > 0) {
            for (int i = 0; i < includes.size(); ++i) {
                int r = dsIncludes.addRow();
                PropertyList include = includes.getPropertyList(i);
                dsIncludes.setValue(r, "includeid", include.getProperty("includeid", ""));
                dsIncludes.setValue(r, "href", include.getProperty("href", ""));
            }
            DataView detail = new DataView(this.pageContext, "includes", dsIncludes, "", this.getConnectionId());
            detail.setElementid("task_includes_det_dataview");
            detail.setSDCId("LV_TaskDef");
            detail.getSDIInfo().setSdcid("LV_TaskDef");
            PropertyList detailProps = new PropertyList();
            detailProps.setProperty("sdcid", "LV_TaskDef");
            detailProps.setProperty("style", "GridWithCheckbox");
            PropertyListCollection detailColumns = new PropertyListCollection();
            PropertyListCollection detailEvents = new PropertyListCollection();
            if (!fullViewOnly) {
                PropertyList detailEvent = new PropertyList();
                detailEvent.setProperty("event", "onchange");
                detailEvent.setProperty("js", "taskProps.maintFieldChange(event,this,'step','" + this.id1 + "','transition[rowid]','next.transitions')");
                detailEvents.add(detailEvent);
            }
            PropertyList detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "includeid");
            detailColumn.setProperty("title", "Include Id");
            detailColumn.setProperty("tip", "Include Id");
            detailColumn.setProperty("mode", !fullViewOnly ? "readonly" : "readonly");
            if (!fullViewOnly) {
                detailColumn.setProperty("events", detailEvents);
            }
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "href");
            detailColumn.setProperty("title", "Href");
            detailColumn.setProperty("tip", "Href");
            detailColumn.setProperty("mode", !fullViewOnly ? "input" : "readonly");
            if (!fullViewOnly) {
                detailColumn.setProperty("events", detailEvents);
            }
            detailColumns.add(detailColumn);
            detailProps.setProperty("columns", detailColumns);
            detail.setElementProperties(detailProps);
            html.append("<div").append(this.browser.isIE() ? " style=\"padding-top:10px;\"" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
            html.append(detail.getHtml());
            html.append("</div>");
        } else {
            html.append(this.getTranslationProcessor().translate("No Includes."));
        }
        html.append("<input type=\"hidden\" id=\"lookupinclde\" onchange=\"taskProps.buttons.addInclude_Callback(this)\">");
        html.append("</div>");
        return html;
    }

    private StringBuffer getStepPretransitionTab() {
        PropertyListCollection trans;
        StringBuffer html = new StringBuffer();
        boolean fullViewOnly = this.viewonly || this.descendant;
        DataSet dsFake = new DataSet();
        dsFake.addColumn("precaseon", 0);
        dsFake.addRow();
        dsFake.setValue(0, "precaseon", this.stepprops.getPropertyList("next") != null ? this.stepprops.getPropertyList("next").getProperty("precaseon", "") : "");
        DataView maint = new DataView(this.pageContext, "primary", dsFake, "", this.getConnectionId());
        maint.setElementid("task_pretransition_pri_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "Form");
        maintProps.setProperty("formcols", "1");
        if (fullViewOnly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        if (!fullViewOnly) {
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "taskProps.maintFieldChange(event,this,'step','" + this.id1 + "','','next')");
            events.add(event);
        }
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "precaseon");
        column.setProperty("title", "Pre-Transition Case Expression");
        column.setProperty("mode", !fullViewOnly ? "inputarea" : "readonly");
        PropertyList lookup = new PropertyList();
        lookup.setProperty("href", "javascript:taskProps.lookupGroovy(this)");
        lookup.setProperty("tip", "Edit Groovy");
        lookup.setProperty("img", "WEB-CORE/elements/images/ellipsisblank.gif");
        lookup.setProperty("style", "button");
        column.setProperty("lookuplink", lookup);
        column.setProperty("events", events);
        column.setProperty("class", "task_longtext");
        columns.add(column);
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append("<div").append(this.browser.isIE() ? "" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
        html.append(maint.getHtml());
        PropertyListCollection propertyListCollection = trans = this.stepprops.getPropertyList("next") != null ? this.stepprops.getPropertyList("next").getCollection("transitions") : null;
        if (trans != null && trans.size() > 0) {
            String stepid;
            DataSet dsCases = new DataSet();
            dsCases.addColumn("transitionid", 0);
            dsCases.addColumn("text", 0);
            dsCases.addColumn("stepid", 0);
            dsCases.addColumn("steptext", 0);
            dsCases.addColumn("oldstepid", 0);
            dsCases.addColumn("case", 0);
            dsCases.addColumn("oldcase", 0);
            PropertyListCollection steps = this.fullprops.getCollection("steps");
            for (int i = 0; i < trans.size(); ++i) {
                int r = dsCases.addRow();
                PropertyList tran = trans.getPropertyList(i);
                dsCases.setValue(r, "transitionid", tran.getProperty("transitionid", ""));
                dsCases.setValue(r, "text", tran.getProperty("text", ""));
                stepid = tran.getProperty("stepid", "");
                PropertyList step = steps != null ? steps.find("stepid", tran.getProperty("stepid", "")) : null;
                String steptext = step != null ? step.getProperty("shorttitle", step.getProperty("title")) : "";
                steptext = steptext.length() > 0 ? steptext + " (" + stepid + ")" : stepid;
                dsCases.setValue(r, "stepid", stepid);
                dsCases.setValue(r, "oldstepid", stepid);
                dsCases.setValue(r, "steptext", steptext);
                dsCases.setValue(r, "case", tran.getProperty("case", ""));
                dsCases.setValue(r, "oldcase", tran.getProperty("case", ""));
            }
            StringBuffer stepslist = new StringBuffer();
            for (int i = 0; i < steps.size(); ++i) {
                PropertyList step = steps.getPropertyList(i);
                if (stepslist.length() > 0) {
                    stepslist.append(";");
                }
                if ((stepid = step.getProperty("stepid", "")).equalsIgnoreCase(this.stepprops.getProperty("stepid"))) continue;
                String steptext = step.getProperty("shorttitle", step.getProperty("title"));
                steptext = steptext.length() > 0 ? steptext + " (" + stepid + ")" : stepid;
                stepslist.append(stepid).append("=").append(steptext);
            }
            DataView detail = new DataView(this.pageContext, "pretransition", dsCases, "", this.getConnectionId());
            detail.setElementid("task_transition_det_dataview");
            detail.setSDCId("LV_TaskDef");
            detail.getSDIInfo().setSdcid("LV_TaskDef");
            PropertyList detailProps = new PropertyList();
            detailProps.setProperty("sdcid", "LV_TaskDef");
            detailProps.setProperty("style", "Grid");
            PropertyListCollection detailColumns = new PropertyListCollection();
            PropertyListCollection detailEvents = new PropertyListCollection();
            if (!fullViewOnly) {
                PropertyList detailEvent = new PropertyList();
                detailEvent.setProperty("event", "onchange");
                detailEvent.setProperty("js", "taskProps.maintFieldChange(event,this,'step','" + this.id1 + "','pretransition[rowid]','next.transitions')");
                detailEvents.add(detailEvent);
            }
            PropertyList detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "stepid");
            detailColumn.setProperty("title", "Target Step");
            detailColumn.setProperty("tip", "[steptext]");
            detailColumn.setProperty("mode", !fullViewOnly ? "dropdownlist" : "readonly");
            if (!fullViewOnly) {
                detailColumn.setProperty("displayvalue", stepslist.toString());
                detailColumn.setProperty("events", detailEvents);
            }
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "oldstepid");
            detailColumn.setProperty("mode", "hidden");
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "case");
            detailColumn.setProperty("title", "Case");
            detailColumn.setProperty("mode", !fullViewOnly ? "input" : "readonly");
            detailColumn.setProperty("events", detailEvents);
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "oldcase");
            detailColumn.setProperty("mode", "hidden");
            detailColumns.add(detailColumn);
            detailColumn = new PropertyList();
            detailColumn.setProperty("columnid", "text");
            detailColumn.setProperty("title", "Label");
            detailColumn.setProperty("mode", !fullViewOnly ? "input" : "readonly");
            detailColumn.setProperty("events", detailEvents);
            detailColumns.add(detailColumn);
            detailProps.setProperty("columns", detailColumns);
            detail.setElementProperties(detailProps);
            html.append("<div").append(this.browser.isIE() ? " style=\"padding-top:10px;\"" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
            html.append(detail.getHtml());
            html.append("</div>");
        }
        html.append("</div>");
        return html;
    }

    private StringBuffer getTransitionTransitionTab() {
        StringBuffer html = new StringBuffer();
        boolean fullViewOnly = this.viewonly || this.descendant;
        DataSet dsFake = new DataSet();
        dsFake.addColumn("transitionid", 0);
        dsFake.addColumn("text", 0);
        dsFake.addColumn("case", 0);
        dsFake.addRow();
        dsFake.setValue(0, "transitionid", this.tranistionprops.getProperty("transitionid", ""));
        dsFake.setValue(0, "text", this.tranistionprops.getProperty("text", ""));
        dsFake.setValue(0, "case", this.tranistionprops.getProperty("case", ""));
        DataView maint = new DataView(this.pageContext, "primary", dsFake, "", this.getConnectionId());
        maint.setElementid("task_transition_dataview");
        maint.setSDCId("LV_TaskDef");
        maint.getSDIInfo().setSdcid("LV_TaskDef");
        PropertyList maintProps = new PropertyList();
        maintProps.setProperty("sdcid", "LV_TaskDef");
        maintProps.setProperty("style", "Form");
        maintProps.setProperty("formcols", "2");
        if (fullViewOnly) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        if (!fullViewOnly) {
            PropertyList event = new PropertyList();
            event.setProperty("event", "onchange");
            event.setProperty("js", "taskProps.maintFieldChange(event,this, 'transition','" + this.id1 + "','" + this.tranistionprops.getProperty("stepid", "") + "','')");
            events.add(event);
        }
        PropertyList column = new PropertyList();
        column.setProperty("columnid", "transitionid");
        column.setProperty("title", "Transition Id");
        column.setProperty("mode", "readonly");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "text");
        column.setProperty("title", "Label");
        column.setProperty("mode", !fullViewOnly ? "input" : "readonly");
        column.setProperty("events", events);
        columns.add(column);
        column = new PropertyList();
        column.setProperty("columnid", "case");
        column.setProperty("title", "Case");
        column.setProperty("mode", !fullViewOnly ? "input" : "readonly");
        column.setProperty("events", events);
        columns.add(column);
        maintProps.setProperty("columns", columns);
        maint.setElementProperties(maintProps);
        html.append("<div").append(this.browser.isIE() ? "" : " style=\"padding: 5px 5px 5px 5px;\"").append(">");
        html.append(maint.getHtml());
        html.append("</div>");
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
        TaskDefPainter.Tools elementType = TaskDefPainter.Tools.LABEL;
        try {
            elementType = TaskDefPainter.Tools.valueOf(this.elementprops.getProperty(PROPERTY_TYPE).toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        dsFake.addRow();
        String elementid = this.elementprops.getProperty("elementid", "");
        dsFake.setValue(0, "elementid", elementid);
        if (elementType == TaskDefPainter.Tools.LABEL || elementType == TaskDefPainter.Tools.TEXT) {
            dsFake.setValue(0, "text", this.elementprops.getProperty("text", elementType == TaskDefPainter.Tools.LABEL ? "A Label" : "A Textbox"));
        }
        if (elementType == TaskDefPainter.Tools.LABEL || elementType == TaskDefPainter.Tools.TEXT) {
            dsFake.setValue(0, "fontsize", this.elementprops.getProperty("fontsize", ""));
            dsFake.setValue(0, "textcolor", this.elementprops.getProperty("textcolor", ""));
        }
        if (elementType == TaskDefPainter.Tools.IMAGE) {
            dsFake.setValue(0, "image", this.elementprops.getProperty("image", "WEB-CORE/images/blank.gif"));
        }
        if (elementType != TaskDefPainter.Tools.LABEL) {
            dsFake.setValue(0, "showbackground", this.elementprops.getProperty("showbackground", ""));
            dsFake.setValue(0, "backgroundcolor", this.elementprops.getProperty("backgroundcolor", ""));
            dsFake.setValue(0, "backgroundopacity", this.elementprops.getProperty("backgroundopacity", ""));
            dsFake.setValue(0, "showline", this.elementprops.getProperty("showline", ""));
            dsFake.setValue(0, "linestyle", this.elementprops.getProperty("linestyle", ""));
            dsFake.setValue(0, "linecolor", this.elementprops.getProperty("linecolor", ""));
            dsFake.setValue(0, "linewidth", this.elementprops.getProperty("linewidth", ""));
        }
        if (elementType == TaskDefPainter.Tools.LINE) {
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
        if (this.viewonly || this.descendant) {
            maintProps.setProperty(PROPERTY_VIEWONLY, "Y");
        }
        PropertyListCollection columns = new PropertyListCollection();
        PropertyListCollection events = new PropertyListCollection();
        PropertyList event = new PropertyList();
        event.setProperty("event", "onchange");
        event.setProperty("js", "taskProps.maintFieldChange(event,this,'element','" + elementid + "','')");
        events.add(event);
        switch (elementType) {
            case IMAGE: {
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "image");
                column.setProperty("title", "Image");
                column.setProperty("mode", !this.viewonly ? "image" : "readonly");
                column.setProperty("events", events);
                columns.add(column);
            }
            case BOX: 
            case CIRCLE: {
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
                column.setProperty("mode", !this.viewonly ? "input" : "readonly");
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
                column.setProperty("mode", !this.viewonly ? "input" : "readonly");
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

    private Tab getStepTab(StepTabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        switch (currentTab) {
            case STEP: {
                html.append(this.getStepStepTab());
                break;
            }
            case CONFIG: {
                html.append(this.getStepConfigTab());
                break;
            }
            case TOOLBAR: {
                html.append(this.getStepToolbarTab());
                break;
            }
            case PRETRANSITION: {
                html.append(this.getStepPretransitionTab());
                break;
            }
            case TRANSITION: {
                html.append(this.getStepTransitionTab());
            }
        }
        tab.setContent(html.toString());
        tab.setId("task_tab_step_" + currentTab.toString().toLowerCase());
        if (currentTab == StepTabs.STEP) {
            String stepTypeId = this.stepprops.getProperty("propertytreeid", "");
            String stepTypeNode = this.stepprops.getProperty("extendnodeid", "");
            if (stepTypeNode.equals("Sapphire Custom")) {
                stepTypeNode = "";
            } else if (stepTypeNode.endsWith(" Custom")) {
                stepTypeNode = stepTypeNode.substring(0, stepTypeNode.length() - 7);
            } else if (stepTypeNode.endsWith(" Product")) {
                stepTypeNode = stepTypeNode.substring(0, stepTypeNode.length() - 8);
            }
            if (stepTypeId.endsWith("Step")) {
                stepTypeId = stepTypeId.substring(0, stepTypeId.length() - 4);
            }
            tab.setText("" + stepTypeId + (stepTypeNode.length() > 0 ? " (" + stepTypeNode + ")" : "") + " Details");
        } else {
            tab.setText(currentTab.getTitle());
        }
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setTip(currentTab.getTitle());
        tab.setAction("taskProps.changeTab('step','" + currentTab.toString().toLowerCase() + "')");
        return tab;
    }

    private Tab getTransitionTab(TransitionTabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        switch (currentTab) {
            case TRANSITION: {
                html.append(this.getTransitionTransitionTab());
            }
        }
        tab.setContent(html.toString());
        tab.setId("task_tab_transition_" + currentTab.toString().toLowerCase());
        tab.setText(currentTab.getTitle());
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setTip(currentTab.getTitle());
        tab.setAction("taskProps.changeTab('transition','" + currentTab.toString().toLowerCase() + "')");
        tab.setBodyheight("200");
        return tab;
    }

    private Tab getElementTab(ElementTabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        switch (currentTab) {
            case ELEMENT: {
                html.append(this.getElementElementTab());
            }
        }
        tab.setContent(html.toString());
        tab.setId("task_tab_element_" + currentTab.toString().toLowerCase());
        tab.setText(currentTab.getTitle());
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setTip(currentTab.getTitle());
        tab.setAction("taskProps.changeTab('element','" + currentTab.toString().toLowerCase() + "')");
        tab.setBodyheight("200");
        return tab;
    }

    private Tab getVariableTab(VariableTabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        switch (currentTab) {
            case VARIABLE: {
                html.append(this.getVariablesVariableTab());
            }
        }
        tab.setContent(html.toString());
        tab.setId("task_tab_variable_" + currentTab.toString().toLowerCase());
        tab.setText(currentTab.getTitle());
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setTip(currentTab.getTitle());
        tab.setAction("taskProps.changeTab('variable','" + currentTab.toString().toLowerCase() + "')");
        return tab;
    }

    private Tab getIOTab(IOTabs currentTab) {
        Tab tab = new Tab();
        tab.setPageContext(this.pageContext);
        StringBuffer html = new StringBuffer();
        switch (currentTab) {
            case IO: {
                html.append(this.getTaskIOsIOTab());
            }
        }
        tab.setContent(html.toString());
        tab.setId("task_tab_io_" + currentTab.toString().toLowerCase());
        tab.setText(currentTab.getTitle().replace("[type]", (this.ioprops != null ? this.ioprops.getProperty("ioflag", "O") : "O").equalsIgnoreCase("O") ? "Output" : "Input"));
        tab.setExpandable("false");
        tab.setExpanded("true");
        tab.setTip(currentTab.getTitle());
        tab.setAction("taskProps.changeTab('io','" + currentTab.toString().toLowerCase() + "')");
        return tab;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        html.append(this.getScriptAndStyle());
        html.append("<table border=0 cellpadding=5 cellspacing=0 style=\"width:100%;height:100%;\"><tbody><tr><td style=\"vertical-align:top;\">");
        if (this.fullprops == null || this.fullprops.size() == 0) {
            html.append(tp.translate("No item selected"));
        } else if (this.type == ItemType.STEP && this.stepprops == null || this.type == ItemType.VARIABLE && this.variableprops == null || this.type == ItemType.TRANSITION && this.tranistionprops == null) {
            html.append(tp.translate("Could not render properties"));
        } else if (this.type == ItemType.STEP) {
            TabGroup tabGroup = new TabGroup();
            tabGroup.setPageContext(this.pageContext);
            tabGroup.setMultiTab(true);
            tabGroup.setId("task_props_step_tabgroup");
            tabGroup.setBodyheight("100%");
            tabGroup.setAppearance("modern");
            if (this.selectedTab > -1) {
                tabGroup.setSelectedTab(this.selectedTab);
            } else {
                tabGroup.setContext("LV_TaskDef_props_step");
            }
            for (StepTabs tab : StepTabs.values()) {
                tabGroup.setTab(this.getStepTab(tab));
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
            if (this.selectedTab > -1) {
                tabGroup.setSelectedTab(this.selectedTab);
            } else {
                tabGroup.setContext("LV_TaskDef_props_element");
            }
            for (ElementTabs tab : ElementTabs.values()) {
                tabGroup.setTab(this.getElementTab(tab));
            }
            tabGroup.setUseChangeTab(true);
            html.append(tabGroup.getHtml());
        } else if (this.type == ItemType.TRANSITION) {
            TabGroup tabGroup = new TabGroup();
            tabGroup.setPageContext(this.pageContext);
            tabGroup.setMultiTab(true);
            tabGroup.setId("task_props_transition_tabgroup");
            tabGroup.setBodyheight("100%");
            tabGroup.setAppearance("modern");
            if (this.selectedTab > -1) {
                tabGroup.setSelectedTab(this.selectedTab);
            } else {
                tabGroup.setContext("LV_TaskDef__props_transition");
            }
            if (this.selectedTab > -1) {
                tabGroup.setSelectedTab(this.selectedTab);
            }
            for (TransitionTabs tab : TransitionTabs.values()) {
                tabGroup.setTab(this.getTransitionTab(tab));
            }
            tabGroup.setUseChangeTab(true);
            html.append(tabGroup.getHtml());
        } else if (this.type == ItemType.VARIABLE) {
            TabGroup tabGroup = new TabGroup();
            tabGroup.setPageContext(this.pageContext);
            tabGroup.setMultiTab(true);
            tabGroup.setId("task_props_variable_tabgroup");
            tabGroup.setBodyheight("100%");
            tabGroup.setAppearance("modern");
            if (this.selectedTab > -1) {
                tabGroup.setSelectedTab(this.selectedTab);
            } else {
                tabGroup.setContext("LV_TaskDef_props_variable");
            }
            if (this.selectedTab > -1) {
                tabGroup.setSelectedTab(this.selectedTab);
            }
            for (VariableTabs tab : VariableTabs.values()) {
                tabGroup.setTab(this.getVariableTab(tab));
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
            if (this.selectedTab > -1) {
                tabGroup.setSelectedTab(this.selectedTab);
            } else {
                tabGroup.setContext("LV_TaskDef_props_io");
            }
            if (this.selectedTab > -1) {
                tabGroup.setSelectedTab(this.selectedTab);
            }
            for (IOTabs tab : IOTabs.values()) {
                tabGroup.setTab(this.getIOTab(tab));
            }
            tabGroup.setUseChangeTab(true);
            html.append(tabGroup.getHtml());
        }
        html.append("</td></tr></tbody></table>");
        html.append(this.getEndScript());
        return html.toString();
    }

    public static enum ItemType {
        TRANSITION,
        STEP,
        IO,
        ELEMENT,
        VARIABLE;

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
    }

    public static enum IOTabs {
        IO("[type] Queue Details");

        private String title;

        private IOTabs(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }
    }

    public static enum VariableTabs {
        VARIABLE("Variable Details");

        private String title;

        private VariableTabs(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }
    }

    public static enum TransitionTabs {
        TRANSITION("Transition Details");

        private String title;

        private TransitionTabs(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }
    }

    public static enum StepTabs {
        STEP("Step Details"),
        CONFIG("Step Configuration"),
        TOOLBAR("Toolbar"),
        PRETRANSITION("Pre-Transition"),
        TRANSITION("Transition");

        private String title;

        private StepTabs(String t) {
            this.title = t;
        }

        public String getTitle() {
            return this.title;
        }
    }
}

