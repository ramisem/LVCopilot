/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.workflow;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.LV_TaskDef;
import com.labvantage.sapphire.admin.ddt.LV_WorkflowDef;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.gwt.shared.JSONableString;
import com.labvantage.sapphire.modules.documents.FieldSetter;
import com.labvantage.sapphire.modules.documents.FormValue;
import com.labvantage.sapphire.modules.documents.gwt.server.NewForm;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.shared.TaskManagerConstants;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeSet;
import org.json.JSONObject;
import org.json.JSONTokener;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDef
implements TaskManagerConstants,
CacheNames {
    private String taskdefid;
    private String taskdefversionid;
    private String taskdefvariantid;
    private DataSet taskdefPrimary;
    private PropertyList taskdef;
    private boolean autoexec = false;
    private boolean standalone = false;
    private boolean trainingrequired = false;
    private boolean overrideallowed = false;
    private boolean trainingexists = false;
    private boolean descendant = false;
    private PropertyListCollection setupVariables = new PropertyListCollection();

    private TaskDef() {
    }

    private static void fixPropertyList(PropertyList pl) {
        PropertyListCollection varscol;
        PropertyListCollection taskio;
        PropertyListCollection steps = pl.getCollection("steps");
        if (steps != null) {
            for (int s = 0; s < steps.size(); ++s) {
                PropertyList step = steps.getPropertyList(s);
                step.setId(step.getProperty("stepid"));
                PropertyListCollection buttons = step.getCollection("buttons");
                if (buttons == null) continue;
                for (int b = 0; b < buttons.size(); ++b) {
                    PropertyList button = buttons.getPropertyList(b);
                    button.setId(button.getProperty("buttonid"));
                    if (button.getSequence() >= 0L) continue;
                    button.setSequence(b);
                }
            }
        }
        if ((taskio = pl.getCollection("taskio")) != null && taskio.size() > 0) {
            for (int io = 0; io < taskio.size(); ++io) {
                PropertyListCollection conditions;
                PropertyList iopl = taskio.getPropertyList(io);
                iopl.setId(iopl.getProperty("ioid"));
                if (!iopl.getProperty("ioflag", "").equalsIgnoreCase("I") || !iopl.containsKey("event")) continue;
                PropertyListCollection propertyListCollection = conditions = iopl.getPropertyList("event") != null ? iopl.getPropertyList("event").getCollection("conditions") : null;
                if (conditions == null) continue;
                for (int c = 0; c < conditions.size(); ++c) {
                    PropertyList condition = conditions.getPropertyList(c);
                    String id = condition.getProperty("id");
                    if (id.length() == 0) {
                        id = condition.getProperty("conditionitem", "") + "_" + condition.getProperty("operator1") + "_" + condition.getProperty("value1") + "_" + condition.getProperty("operator2") + "_" + condition.getProperty("value2");
                    }
                    condition.setId(id);
                }
            }
        }
        if ((varscol = pl.getCollection("variables")) != null && varscol.size() > 0) {
            for (int v = 0; v < varscol.size(); ++v) {
                PropertyList var = varscol.getPropertyList(v);
                var.setId(var.getProperty("variableid"));
            }
        }
    }

    public static PropertyList getDescendantProperties(PropertyList parent, PropertyList child) {
        TaskDef.fixPropertyList(parent);
        TaskDef.fixPropertyList(child);
        PropertyList result = parent.copy();
        try {
            result.setPropertyList(child.toXMLString(), true);
        }
        catch (Exception e) {
            Trace.logError("Failed to merge descendant propertylists.");
        }
        result.setProperty("childprops", child);
        result.setProperty("parentprops", parent);
        return result;
    }

    public static TaskDef getInstance(SapphireConnection sapphireConnection, String workflowdefid, String workflowdefversionid, String workflowdefvariantid, String taskdefitemid) throws SapphireException {
        return TaskDef.getInstance(sapphireConnection, workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, false);
    }

    public static TaskDef getInstance(SapphireConnection sapphireConnection, String workflowdefid, String workflowdefversionid, String workflowdefvariantid, String taskdefitemid, boolean translate) throws SapphireException {
        return TaskDef.getInstance(sapphireConnection, "", "", "", workflowdefid, workflowdefversionid, workflowdefvariantid, taskdefitemid, null, translate);
    }

    public static TaskDef getInstance(SapphireConnection sapphireConnection, String taskdefid, String taskdefversionid, String taskdefvariantid) throws SapphireException {
        return TaskDef.getInstance(sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, false);
    }

    public static TaskDef getInstance(SapphireConnection sapphireConnection, String taskdefid, String taskdefversionid, String taskdefvariantid, boolean translate) throws SapphireException {
        return TaskDef.getInstance(sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid, "", "", "", "", null, translate);
    }

    private static TaskDef getInstance(SapphireConnection sapphireConnection, String taskdefid, String taskdefversionid, String taskdefvariantid, String workflowdefid, String workflowdefversionid, String workflowdefvariantid, String taskdefitemid, PropertyList directives, boolean translate) throws SapphireException {
        Trace.logInfo("Creating task instance for " + taskdefid + " (Ver:" + taskdefversionid + ") Var:" + taskdefvariantid);
        if (directives == null) {
            directives = new PropertyList();
        }
        boolean ignoreTrainingRecs = directives.getProperty("ignoretrainingrecs", "N").equals("Y");
        QueryProcessor queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
        SDIProcessor sdiProcessor = new SDIProcessor(sapphireConnection.getConnectionId());
        WebAdminProcessor webadminProcessor = new WebAdminProcessor(sapphireConnection.getConnectionId());
        TranslationProcessor translationProcessor = null;
        if (translate) {
            translationProcessor = new TranslationProcessor(sapphireConnection.getConnectionId());
        }
        PropertyListCollection workflowVariables = null;
        PropertyList workflowTaskOverrides = null;
        boolean multipleWorkflows = false;
        if ((taskdefid == null || taskdefid.length() == 0) && taskdefitemid != null && taskdefitemid.length() > 0) {
            SDIRequest wfRequest = new SDIRequest();
            wfRequest.setSDIList("LV_WorkflowDef", workflowdefid, workflowdefversionid, workflowdefvariantid);
            wfRequest.setRequestItem("primary");
            wfRequest.setRequestItem("workflowdeftask");
            wfRequest.setExtendedDataTypes(true);
            SDIData wfData = sdiProcessor.getSDIData(wfRequest);
            if (wfData == null || wfData.getDataset("primary") == null || wfData.getDataset("primary").size() == 0) {
                throw new SapphireException("Failed to find workflow def " + LV_WorkflowDef.getText(workflowdefid, workflowdefversionid, workflowdefvariantid));
            }
            DataSet wfPrimary = wfData.getDataset("primary");
            multipleWorkflows = wfPrimary.size() > 1;
            PropertyList workflowdef = new PropertyList();
            workflowdef.setPropertyList(wfPrimary.getValue(0, "workflowdef"));
            workflowVariables = workflowdef.getCollection("variables");
            DataSet wfTask = wfData.getDataset("workflowdeftask");
            int findRow = wfTask.findRow("taskdefitemid", taskdefitemid.contains(";") ? taskdefitemid.substring(0, taskdefitemid.indexOf(";")) : taskdefitemid);
            if (findRow > -1) {
                taskdefid = wfTask.getValue(findRow, "taskdefid");
                taskdefversionid = wfTask.getValue(findRow, "taskdefversionid");
                taskdefvariantid = wfTask.getValue(findRow, "taskdefvariantid");
                PropertyListCollection tasks = workflowdef.getCollection("tasks");
                for (int i = 0; i < tasks.size() && workflowTaskOverrides == null; ++i) {
                    PropertyList task = tasks.getPropertyList(i);
                    if (!task.getProperty("taskdefitemid").equals(taskdefitemid) || !task.getProperty("taskdefid").equals(taskdefid) || !task.getProperty("taskdefversionid").equals(taskdefversionid) || !task.getProperty("taskdefvariantid").equals(taskdefvariantid)) continue;
                    workflowTaskOverrides = task;
                }
            } else {
                throw new SapphireException("Failed to find task '" + taskdefitemid + "' in workflow def " + LV_WorkflowDef.getText(workflowdefid, workflowdefversionid, workflowdefvariantid));
            }
        }
        if (taskdefversionid != null && (taskdefversionid.length() == 0 || taskdefversionid.equalsIgnoreCase("C"))) {
            String sql = "SELECT taskdefversionid FROM taskdef WHERE taskdefid = ? AND taskdefvariantid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( taskdefversionid as integer ) DESC";
            DataSet taskdefversion = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{taskdefid, taskdefvariantid});
            if (taskdefversion.size() > 0) {
                taskdefversionid = taskdefversion.getValue(0, "taskdefversionid");
            } else {
                throw new SapphireException("Failed to find taskdef '" + taskdefid + "'");
            }
        }
        if (taskdefvariantid == null || taskdefvariantid.length() == 0) {
            taskdefvariantid = "1";
        }
        TaskDef taskDef = null;
        SDIRequest sdiRequest = new SDIRequest();
        SDIData sdiData = null;
        DataSet taskdefData = null;
        if (taskdefid != null && taskdefid.length() > 0) {
            PropertyListCollection steps;
            sdiRequest.setSDCid("LV_TaskDef");
            sdiRequest.setKeyid1List(taskdefid);
            sdiRequest.setKeyid2List(taskdefversionid);
            sdiRequest.setKeyid3List(taskdefvariantid);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRequestItem("taskdefstep");
            sdiRequest.setExtendedDataTypes(true);
            sdiData = sdiProcessor.getSDIData(sdiRequest);
            if (sdiData == null || sdiData.getDataset("primary") == null || sdiData.getDataset("primary").size() != 1) {
                throw new SapphireException("Failed to find taskdef " + LV_TaskDef.getText(taskdefid, taskdefversionid, taskdefvariantid));
            }
            taskdefData = sdiData.getDataset("primary");
            if (multipleWorkflows && !taskdefData.getValue(0, "scopeflag").equals("G")) {
                throw new SapphireException("Taskdef '" + taskdefid + "' does not have global scope but has multiple workflows!");
            }
            DataSet taskdefstepData = sdiData.getDataset("taskdefstep");
            taskDef = new TaskDef();
            taskDef.taskdefPrimary = taskdefData;
            taskDef.setTaskdefid(taskdefData.getValue(0, "taskdefid"));
            taskDef.setTaskdefversionid(taskdefData.getValue(0, "taskdefversionid"));
            taskDef.setTaskdefvariantid(taskdefData.getValue(0, "taskdefvariantid"));
            taskDef.setTaskdef(taskdefData.getClob(0, "taskdef"));
            if (taskdefData.getValue(0, "basedontaskdefid").length() > 0 && taskdefData.getValue(0, "basedontaskdefversionid").length() > 0 && taskdefData.getValue(0, "basedontaskdefvariantid").length() > 0) {
                TaskDef parentTaskDef = TaskDef.getInstance(sapphireConnection, taskdefData.getValue(0, "basedontaskdefid"), taskdefData.getValue(0, "basedontaskdefversionid"), taskdefData.getValue(0, "basedontaskdefvariantid"), translate);
                taskDef.taskdef = TaskDef.getDescendantProperties(parentTaskDef.getTaskdef(), taskDef.getTaskdef());
                taskDef.descendant = true;
                taskDef.setStandalone(parentTaskDef.standalone);
                taskDef.setAutoexec(parentTaskDef.autoexec);
            } else {
                taskDef.setStandalone(taskdefData.getValue(0, "standaloneflag").equals("Y"));
                taskDef.setAutoexec(taskdefData.getValue(0, "autoexecflag").equals("Y"));
            }
            taskDef.setTrainingrequired(taskdefData.getValue(0, "trainingreqflag", "N").equals("Y"));
            taskDef.setOverrideallowed(taskdefData.getValue(0, "overrideallowedflag", "N").equals("Y"));
            if (taskDef.isTrainingrequired()) {
                if (!ignoreTrainingRecs) {
                    DataSet certification = queryProcessor.getPreparedSqlDataSet("SELECT certificationstatus, expirationdt, graceperiod, graceperiodunits FROM s_sdicertification WHERE resourcesdcid = 'User' AND resourcekeyid1 = ? AND resourcekeyid2 = '(null)' AND resourcekeyid3 = '(null)' AND certifiedforsdcid = 'LV_TaskDef' AND certifiedforkeyid1 = ? AND certifiedforkeyid2 = ? AND certifiedforkeyid3 = ? AND certificationtype = 'Analyst Training'", new Object[]{sapphireConnection.getSysuserId(), taskDef.getTaskdefid(), taskDef.getTaskdefversionid(), taskDef.getTaskdefvariantid()});
                    if (certification.size() == 1 && certification.getValue(0, "certificationstatus").equals("Valid")) {
                        Calendar expirationdt = certification.getCalendar(0, "expirationdt");
                        if (expirationdt == null) {
                            taskDef.trainingexists = true;
                        } else {
                            int graceperiod = certification.getInt(0, "graceperiod");
                            if (graceperiod > 0) {
                                String graceperiodunits = certification.getString(0, "graceperiodunits");
                                if ("Days".equalsIgnoreCase(graceperiodunits)) {
                                    expirationdt.add(5, graceperiod);
                                } else if ("Weeks".equalsIgnoreCase(graceperiodunits)) {
                                    expirationdt.add(5, graceperiod * 7);
                                } else if ("Months".equalsIgnoreCase(graceperiodunits)) {
                                    expirationdt.add(2, graceperiod);
                                } else if ("Years".equalsIgnoreCase(graceperiodunits)) {
                                    expirationdt.add(1, graceperiod);
                                }
                            }
                            if (DateTimeUtil.getNowCalendar().before(expirationdt)) {
                                taskDef.trainingexists = true;
                            }
                        }
                    }
                } else {
                    taskDef.trainingexists = true;
                }
            }
            taskDef.getTaskdef().setProperty("trainingexists", taskDef.trainingexists ? "Y" : "N");
            taskDef.getTaskdef().setProperty("trainingreqflag", taskDef.trainingrequired ? "Y" : "N");
            taskDef.getTaskdef().setProperty("overrideallowedflag", taskDef.overrideallowed ? "Y" : "N");
            if (!taskDef.isDescendant()) {
                PropertyListCollection steps2 = taskDef.taskdef.getCollection("steps");
                if (steps2 != null) {
                    for (int i = 0; i < steps2.size(); ++i) {
                        PropertyList step = steps2.getPropertyList(i);
                        try {
                            String stepid = step.getProperty("stepid");
                            int stepRow = taskdefstepData.findRow("stepid", stepid);
                            if (stepRow >= 0) {
                                String propertytreeid = taskdefstepData.getValue(stepRow, "propertytreeid");
                                String extendnodeid = taskdefstepData.getValue(stepRow, "extendnodeid");
                                String stepType = webadminProcessor.getPropertyTreeObject(propertytreeid);
                                String cacheKey = taskdefid + ";" + taskdefversionid + ";" + taskdefvariantid + ";" + stepid;
                                PropertyList stepTypeProps = (PropertyList)CacheUtil.get(sapphireConnection.getDatabaseId(), "TaskStepTypeProps", cacheKey);
                                PropertyList stepProps = (PropertyList)CacheUtil.get(sapphireConnection.getDatabaseId(), "TaskStepProps", cacheKey);
                                if (stepTypeProps == null || stepProps == null) {
                                    stepTypeProps = new PropertyList();
                                    PropertyList overrides = new PropertyList();
                                    String valuetree = taskdefstepData.getClob(stepRow, "valuetree");
                                    if (valuetree != null) {
                                        overrides.setPropertyList(valuetree);
                                    }
                                    PropertyTree stepPropertyTree = webadminProcessor.getPropertyTree(propertytreeid);
                                    Node overrideNode = stepPropertyTree.getNode(extendnodeid);
                                    Node stepTypeNode = stepPropertyTree.createNode("_steptypeoverrides", overrideNode);
                                    stepTypeNode.setPropertyList(overrides);
                                    PropertyList stepTypePropsOverrides = stepPropertyTree.getNodePropertyList("_steptypeoverrides", true).getPropertyList("steptype");
                                    if (stepTypePropsOverrides != null) {
                                        stepTypeProps.setPropertyList(stepTypePropsOverrides.toXMLString());
                                    } else {
                                        stepTypeProps.setPropertyList(stepPropertyTree.getNodePropertyList(extendnodeid, true).getPropertyList("steptype").toXMLString());
                                    }
                                    stepProps = new PropertyList();
                                    PropertyList stepPropOverrides = stepPropertyTree.getNodePropertyList("_steptypeoverrides", true).getPropertyList("stepprops");
                                    if (stepPropOverrides != null) {
                                        stepProps.setPropertyList(stepPropOverrides.toXMLString());
                                    } else {
                                        stepProps.setPropertyList(stepPropertyTree.getNodePropertyList(extendnodeid, true).getPropertyList("stepprops").toXMLString());
                                    }
                                    CacheUtil.put(sapphireConnection.getDatabaseId(), "TaskStepTypeProps", cacheKey, stepTypeProps);
                                    CacheUtil.put(sapphireConnection.getDatabaseId(), "TaskStepProps", cacheKey, stepProps);
                                }
                                step.setProperty("type", stepType);
                                if (translationProcessor == null) {
                                    step.setProperty(stepType, stepTypeProps.copy());
                                    step.setProperty("stepprops", stepProps.copy());
                                    continue;
                                }
                                step.setProperty(stepType, stepTypeProps.copy(sapphireConnection.getLanguage(), translationProcessor));
                                step.setProperty("stepprops", stepProps.copy(sapphireConnection.getLanguage(), translationProcessor));
                                continue;
                            }
                            throw new SapphireException("Failed to find taskdefstep row for step '" + stepid + "'.");
                        }
                        catch (Exception e) {
                            throw new SapphireException("Failed to load step type properties. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
                        }
                    }
                }
                if (translate) {
                    TaskDef.translateProperties(sapphireConnection, translationProcessor, taskDef.taskdef);
                }
            }
            if ((steps = taskDef.taskdef.getCollection("steps")) != null) {
                for (int i = 0; i < steps.size(); ++i) {
                    PropertyListCollection buttons = steps.getPropertyList(i).getCollection("buttons");
                    if (buttons == null) continue;
                    for (int j = buttons.size() - 1; j >= 0; --j) {
                        PropertyList button = buttons.getPropertyList(j);
                        if (button.getAttribute("rolelist").length() > 0) {
                            if (button.copy(sapphireConnection.getRoleList(), "<ALL>") != null) continue;
                            buttons.remove(j);
                            continue;
                        }
                        if (!button.getProperty("show").equals("N")) continue;
                        buttons.remove(j);
                    }
                }
            }
            if (workflowVariables != null) {
                // empty if block
            }
            if (workflowTaskOverrides != null) {
                PropertyListCollection variableOverrides;
                if (workflowTaskOverrides.containsKey("shorttitle")) {
                    taskDef.taskdef.setProperty("shorttitle", workflowTaskOverrides.getProperty("shorttitle"));
                }
                if (workflowTaskOverrides.containsKey("longtitle")) {
                    taskDef.taskdef.setProperty("longtitle", workflowTaskOverrides.getProperty("longtitle"));
                }
                if (workflowTaskOverrides.containsKey("icon")) {
                    taskDef.taskdef.setProperty("icon", workflowTaskOverrides.getProperty("icon"));
                }
                if (workflowTaskOverrides.containsKey("appearance")) {
                    taskDef.taskdef.setProperty("appearance", workflowTaskOverrides.getProperty("appearance"));
                }
                if (workflowTaskOverrides.containsKey("taskcolor1")) {
                    taskDef.taskdef.setProperty("taskcolor1", workflowTaskOverrides.getProperty("taskcolor1"));
                }
                if (workflowTaskOverrides.containsKey("taskcolor2")) {
                    taskDef.taskdef.setProperty("taskcolor2", workflowTaskOverrides.getProperty("taskcolor2"));
                }
                if (workflowTaskOverrides.containsKey("taskcolor3")) {
                    taskDef.taskdef.setProperty("taskcolor3", workflowTaskOverrides.getProperty("taskcolor3"));
                }
                if ((variableOverrides = workflowTaskOverrides.getCollection("variables")) != null && variableOverrides.size() > 0) {
                    variableOverrides.index("variableid");
                    PropertyListCollection variables = taskDef.taskdef.getCollection("variables");
                    if (variables != null) {
                        for (int i = 0; i < variables.size(); ++i) {
                            PropertyList variable = variables.getPropertyList(i);
                            String variableid = variable.getProperty("variableid");
                            PropertyList variableOverride = variableOverrides.getIndexedPropertyList(variableid);
                            if (variableOverride == null || !variableOverride.containsKey("defaultvalue")) continue;
                            variable.setProperty("defaultvalue", variableOverride.getProperty("defaultvalue"));
                        }
                    }
                }
            }
        } else {
            throw new SapphireException("TaskDef load failed - no loading parameters specified!");
        }
        return taskDef;
    }

    private static void translateProperties(SapphireConnection sapphireConnection, TranslationProcessor translationProcessor, PropertyList properties) {
        for (String propertyid : properties.keySet()) {
            if (properties.isSimple(propertyid)) {
                String[] translations;
                String propertyvalue = properties.getProperty(propertyid);
                if (!propertyvalue.contains("{{") || (translations = StringUtil.getTokens(propertyvalue, "{{", "}}")) == null || translations.length <= 0) continue;
                String languageid = sapphireConnection.getLanguage();
                if (languageid != null && languageid.length() > 0) {
                    int i;
                    HashMap<String, String> transtext = new HashMap<String, String>();
                    for (i = 0; i < translations.length; ++i) {
                        transtext.put(translations[i], translations[i]);
                    }
                    translationProcessor.translateTable(languageid, transtext);
                    for (i = 0; i < translations.length; ++i) {
                        propertyvalue = StringUtil.replaceAll(propertyvalue, "{{" + translations[i] + "}}", (String)transtext.get(translations[i]));
                    }
                } else {
                    for (int i = 0; i < translations.length; ++i) {
                        propertyvalue = StringUtil.replaceAll(propertyvalue, "{{" + translations[i] + "}}", translations[i]);
                    }
                }
                properties.setProperty(propertyid, propertyvalue);
                continue;
            }
            if (properties.isPropertyList(propertyid)) {
                TaskDef.translateProperties(sapphireConnection, translationProcessor, properties.getPropertyList(propertyid));
                continue;
            }
            if (!properties.isCollection(propertyid)) continue;
            PropertyListCollection collection = properties.getCollection(propertyid);
            for (int i = 0; i < collection.size(); ++i) {
                TaskDef.translateProperties(sapphireConnection, translationProcessor, collection.getPropertyList(i));
            }
        }
    }

    public String getTaskdefid() {
        return this.taskdefid;
    }

    public void setTaskdefid(String taskdefid) {
        this.taskdefid = taskdefid;
    }

    public String getTaskdefversionid() {
        return this.taskdefversionid;
    }

    public void setTaskdefversionid(String taskdefversionid) {
        this.taskdefversionid = taskdefversionid;
    }

    public String getTaskdefvariantid() {
        return this.taskdefvariantid;
    }

    public void setTaskdefvariantid(String taskdefvariantid) {
        this.taskdefvariantid = taskdefvariantid;
    }

    public boolean isAutoexec() {
        return this.autoexec;
    }

    public void setAutoexec(boolean autoexec) {
        this.autoexec = autoexec;
    }

    public boolean isStandalone() {
        return this.standalone;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    public void setTrainingrequired(boolean trainingrequired) {
        this.trainingrequired = trainingrequired;
    }

    public boolean isTrainingrequired() {
        return this.trainingrequired;
    }

    public boolean isOverrideallowed() {
        return this.overrideallowed;
    }

    public void setOverrideallowed(boolean overrideallowed) {
        this.overrideallowed = overrideallowed;
    }

    public boolean isTrainingexists() {
        return this.trainingexists;
    }

    public boolean isDescendant() {
        return this.descendant;
    }

    public DataSet getTaskdefPrimary() {
        return this.taskdefPrimary;
    }

    public PropertyList getTaskdef() {
        return this.taskdef;
    }

    private void setTaskdef(String taskdef) throws SapphireException {
        PropertyListCollection variables;
        this.taskdef = new PropertyList();
        this.taskdef.setPropertyList(taskdef, false, false);
        boolean createStages = false;
        PropertyListCollection stages = this.taskdef.getCollection("stages");
        if (stages == null) {
            stages = new PropertyListCollection();
            this.taskdef.setProperty("stages", stages);
            createStages = true;
        }
        TreeSet<String> orderedStepGroups = new TreeSet<String>();
        PropertyListCollection steps = this.taskdef.getCollection("steps");
        if (steps != null) {
            String[] types = new String[]{"next", "back", "cancel", "complete"};
            String[] texts = new String[]{"Next", "Back", "Cancel", "Complete"};
            String[] titles = new String[]{"Goto next step", "Go back to previous step", "Cancel task", "Complete task"};
            String[] images = new String[]{"WEB-CORE/images/gif/Forward.gif", "WEB-CORE/images/gif/Back.gif", "WEB-CORE/images/png32/Cancel.png", "WEB-CORE/images/png32/Confirm.png"};
            String[] actions = new String[]{"sapphire.task.next()", "sapphire.task.back()", "sapphire.task.cancel()", "sapphire.task.complete()"};
            for (int i = 0; i < steps.size(); ++i) {
                PropertyList step = steps.getPropertyList(i);
                orderedStepGroups.add(step.getProperty("stepgroupid"));
                PropertyListCollection buttons = step.getCollection("buttons");
                if (buttons == null) continue;
                for (int j = 0; j < buttons.size(); ++j) {
                    PropertyList buttonProps = buttons.getPropertyList(j);
                    String type = buttonProps.getProperty("type");
                    for (int k = 0; k < types.length; ++k) {
                        if (!type.equals(types[k])) continue;
                        if (buttonProps.getProperty("text").length() == 0) {
                            buttonProps.setProperty("text", texts[k]);
                        }
                        if (buttonProps.getProperty("image").length() == 0) {
                            buttonProps.setProperty("image", images[k]);
                        }
                        if (buttonProps.getProperty("title").length() == 0) {
                            buttonProps.setProperty("title", titles[k]);
                        }
                        if (buttonProps.getProperty("action").length() != 0) continue;
                        buttonProps.setProperty("action", actions[k]);
                    }
                }
            }
            if (createStages) {
                for (String stageid : orderedStepGroups) {
                    if (stageid.length() <= 0 || stages.find("stageid", stageid) != null) continue;
                    PropertyList stage = new PropertyList();
                    stage.setProperty("stageid", stageid);
                    stage.setProperty("text", stageid);
                    stages.add(stage);
                }
            }
        }
        if ((variables = this.taskdef.getCollection("variables")) == null) {
            variables = new PropertyListCollection();
            this.taskdef.setProperty("variables", variables);
        }
        variables.index("variableid");
        PropertyListCollection taskio = this.taskdef.getCollection("taskio");
        if (taskio != null) {
            taskio.index("ioid");
            for (int i = 0; i < taskio.size(); ++i) {
                PropertyList io = taskio.getPropertyList(i);
                String ioid = io.getProperty("ioid");
                String variableid = io.getProperty("variableid");
                PropertyList propertyList = variables.getIndexedPropertyList(variableid);
            }
        }
    }

    public JSONableMap setupTaskVariables(PropertyListCollection variables, String workflowexecid, PropertyList inputs, QueryProcessor queryProcessor, SapphireConnection sapphireConnection) {
        JSONableMap taskVariables = new JSONableMap();
        ArrayList workflowVariables = null;
        if (variables != null) {
            int i;
            HashMap<String, String> variableStep = new HashMap<String, String>();
            PropertyListCollection steps = this.getTaskdef().getCollection("steps");
            if (steps != null && steps.size() > 0) {
                for (i = 0; i < steps.size(); ++i) {
                    String variableid;
                    PropertyList step = steps.getPropertyList(i);
                    String type = step.getProperty("type");
                    PropertyList stepType = step.getPropertyList(type);
                    if (!type.equalsIgnoreCase("Form") || (variableid = stepType.getProperty("variableid")).length() <= 0) continue;
                    variableStep.put(variableid, step.getProperty("stepid"));
                }
            }
            variables.index("variableid");
            for (i = 0; i < variables.size(); ++i) {
                PropertyList variable = variables.getPropertyList(i);
                String variableid = variable.getProperty("variableid");
                String value = null;
                String source = null;
                if (inputs != null && inputs.containsKey(variableid)) {
                    value = inputs.getProperty(variableid);
                    source = "externalinput";
                } else {
                    String defaultValue = variable.getProperty("defaultvalue");
                    if (defaultValue.length() > 0) {
                        value = defaultValue;
                        source = "defaultvalue";
                    }
                }
                if (value != null) {
                    DataSet wfexec;
                    if (value.contains("[workflow.variables.") && workflowVariables == null && workflowexecid.length() > 0 && (wfexec = queryProcessor.getPreparedSqlDataSet("SELECT workflowexec FROM workflowexec WHERE workflowexecid = ?", new Object[]{workflowexecid}, true)) != null && wfexec.size() == 1 && wfexec.getClob(0, "workflowexec") != null) {
                        PropertyList workflowexec = new PropertyList();
                        try {
                            workflowexec.setPropertyList(wfexec.getValue(0, "workflowexec"));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        workflowVariables = workflowexec.getCollection("variables");
                        if (workflowVariables != null) {
                            ((PropertyListCollection)workflowVariables).index("variableid");
                        }
                    }
                    if (value.length() > 0) {
                        String[] tokens = StringUtil.getTokens(value);
                        block6: for (int j = 0; j < tokens.length; ++j) {
                            String token = tokens[j];
                            if (!token.startsWith("workflow.variables.") || workflowVariables == null) continue;
                            for (int k = 0; k < workflowVariables.size(); ++k) {
                                PropertyList workflowVariable = ((PropertyListCollection)workflowVariables).getPropertyList(k);
                                if (!workflowVariable.getProperty("variableid").equals(token.substring(19))) continue;
                                value = StringUtil.replaceAll(value, "[" + token + "]", workflowVariable.getProperty("value"));
                                source = "workflowvar:" + workflowVariable.getProperty("variableid");
                                continue block6;
                            }
                        }
                    }
                    if (variable.getProperty("type").equalsIgnoreCase("string")) {
                        TaskDef.setTaskVariable(taskVariables, variableid, new JSONableString(value), sapphireConnection.getSysuserId(), source);
                    } else if (variable.getProperty("type").equalsIgnoreCase("sdilist")) {
                        SDIList sdiList = new SDIList();
                        sdiList.addSDIList(value);
                        TaskDef.setTaskVariable(taskVariables, variableid, sdiList, sapphireConnection.getSysuserId(), source);
                    }
                }
                if (variable.getProperty("setup").equals("Y")) {
                    PropertyList setupVariable = new PropertyList(variable);
                    this.setupVariables.add(setupVariable);
                }
                if (!variable.getProperty("type").equalsIgnoreCase("form")) continue;
                try {
                    NewForm newForm = new NewForm(sapphireConnection, false);
                    PropertyList requestData = new PropertyList();
                    requestData.setProperty("taskdefid", this.getTaskdefid());
                    requestData.setProperty("taskdefversionid", this.getTaskdefversionid());
                    requestData.setProperty("taskdefvariantid", this.getTaskdefvariantid());
                    requestData.setProperty("stepid", (String)variableStep.get(variableid));
                    HashMap responseData = newForm.execute(requestData);
                    JSONObject jsonObject = new JSONObject(new JSONTokener((String)responseData.get("jsonreturn")));
                    PropertyList response = new PropertyList(jsonObject);
                    if (response.getProperty("status").equals("E") || response.getProperty("status").startsWith("DUE")) continue;
                    FormValue formValue = new FormValue(response);
                    TaskDef.setTaskVariable(taskVariables, variableid, formValue, sapphireConnection.getSysuserId(), "setup");
                    continue;
                }
                catch (Exception e) {
                    Trace.logError("Failed to setup form variable for variableid '" + variableid + "' in task " + LV_TaskDef.getText(this.getTaskdefid(), this.getTaskdefversionid(), this.getTaskdefvariantid()) + ". Reason: " + e.getMessage(), e);
                }
            }
        }
        return taskVariables;
    }

    public PropertyListCollection getSetupVariables() {
        return this.setupVariables;
    }

    public static JSONable getTaskVariable(JSONableMap taskVariables, String variableid) {
        JSONableMap variable = (JSONableMap)taskVariables.get(variableid);
        return variable != null ? variable.get("value") : null;
    }

    public static void setTaskVariable(JSONableMap taskVariables, String variableid, JSONable value, String sysuserid, String source) {
        TaskDef.setTaskVariable(taskVariables, variableid, value, sysuserid, null, source);
    }

    public static void setTaskVariable(JSONableMap taskVariables, String variableid, JSONable value, String sysuserid, String timestamp, String source) {
        JSONableMap variable = new JSONableMap();
        variable.put("value", value);
        variable.put("sysuserid", sysuserid);
        variable.put("source", source);
        if (timestamp == null) {
            timestamp = String.valueOf(DateTimeUtil.getNowTimestamp().getTime());
        }
        try {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(timestamp));
            M18NUtil m18n = new M18NUtil();
            variable.put("datetime", m18n.getDefaultDateFormat().format(c.getTime()));
            variable.put("seconds", c.get(13) + "." + c.get(14));
        }
        catch (NumberFormatException nfe) {
            variable.put("datetime", "");
            variable.put("seconds", "");
        }
        variable.put("timestamp", timestamp != null ? timestamp : String.valueOf(DateTimeUtil.getNowTimestamp().getTime()));
        taskVariables.put(variableid, variable);
    }

    public static HashMap getTaskVariablesProcessingMap(JSONableMap taskVariables, ConnectionInfo connectionInfo) {
        HashMap<String, Object> processingMap = new HashMap<String, Object>();
        if (taskVariables != null) {
            for (String variableid : taskVariables.keySet()) {
                JSONableMap variable = (JSONableMap)taskVariables.get(variableid);
                if (variable == null) continue;
                JSONable variableValue = variable.get("value");
                if (variableValue != null) {
                    if (variableValue instanceof FormValue) {
                        FieldSetter.defineFieldMap((FormValue)variableValue, connectionInfo);
                    }
                    processingMap.put(variableid, variableValue instanceof JSONableString ? ((JSONableString)variableValue).toString() : variableValue);
                    continue;
                }
                processingMap.put(variableid, null);
            }
        }
        return processingMap;
    }
}

