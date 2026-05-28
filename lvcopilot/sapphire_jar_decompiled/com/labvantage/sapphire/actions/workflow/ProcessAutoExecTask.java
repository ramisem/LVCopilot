/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.workflow.StartTaskExec;
import com.labvantage.sapphire.admin.ddt.LV_TaskDef;
import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.gwt.shared.JSONableString;
import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.modules.workflow.gwt.server.steprequests.ConverterStep;
import com.labvantage.sapphire.modules.workflow.gwt.server.steprequests.SplitterStep;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.shared.TaskManagerConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ProcessAutoExecTask
extends BaseAction
implements sapphire.action.ProcessAutoExecTask,
TaskManagerConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String tempTaskqueueid = properties.getProperty("taskqueueid");
        DataSet temp = new DataSet();
        temp.addColumnValues("taskqueueid", 0, tempTaskqueueid, ";");
        ArrayList<DataSet> blocks = temp.getSplitDataSets(1000);
        block0: for (DataSet block : blocks) {
            String taskqueueid = block.getColumnValues("taskqueueid", ";");
            boolean testmode = properties.getProperty("testmode", "N").equals("Y");
            SafeSQL safeSQL = new SafeSQL();
            this.database.createPreparedResultSet("SELECT taskqueueid, taskdefid, taskdefversionid, taskdefvariantid, ioid, workflowdefid, workflowdefversionid, workflowdefvariantid, workflowexecid, taskdefitemid, queuesdcid, queuekeyid1, queuekeyid2, queuekeyid3 FROM taskqueue WHERE taskqueueid IN ( " + (testmode ? safeSQL.addVar("xxx") : safeSQL.addIn(taskqueueid, ";")) + " )", safeSQL.getValues());
            DataSet taskqueue = new DataSet(this.database.getResultSet());
            if (testmode) {
                String taskdefid = properties.getProperty("taskdefid");
                String taskdefversionid = properties.getProperty("taskdefversionid");
                String taskdefvariantid = properties.getProperty("taskdefvariantid");
                String[] testkeyid1 = StringUtil.split(properties.getProperty("exampletaskqueuekeyid1"), ";");
                String[] testkeyid2 = StringUtil.split(properties.getProperty("exampletaskqueuekeyid2"), ";");
                String[] testkeyid3 = StringUtil.split(properties.getProperty("exampletaskqueuekeyid3"), ";");
                for (int i = 0; i < testkeyid1.length; ++i) {
                    taskqueue.addRow();
                    taskqueue.setString(i, "taskdefid", taskdefid);
                    taskqueue.setString(i, "taskdefversionid", taskdefversionid);
                    taskqueue.setString(i, "taskdefvariantid", taskdefvariantid);
                    taskqueue.setString(i, "queuekeyid1", testkeyid1[i]);
                }
            }
            if (taskqueue.size() <= 0) continue;
            TaskContext taskContext = new TaskContext();
            taskContext.setTaskdefid(taskqueue.getValue(0, "taskdefid"));
            taskContext.setTaskdefversionid(taskqueue.getValue(0, "taskdefversionid"));
            taskContext.setTaskdefvariantid(taskqueue.getValue(0, "taskdefvariantid"));
            taskContext.setWorkflowdefid(taskqueue.getValue(0, "workflowdefid"));
            taskContext.setWorkflowdefversionid(taskqueue.getValue(0, "workflowdefversionid"));
            taskContext.setWorkflowdefvariantid(taskqueue.getValue(0, "workflowdefvariantid"));
            taskContext.setTaskdefitemid(taskqueue.getValue(0, "taskdefitemid"));
            taskContext.setWorkflowexecid(taskqueue.getValue(0, "workflowexecid"));
            String ioid = taskqueue.getValue(0, "ioid");
            String sdcid = taskqueue.getValue(0, "queuesdcid");
            String queuekeyid1 = taskqueue.getColumnValues("queuekeyid1", ";");
            String queuekeyid2 = taskqueue.getColumnValues("queuekeyid2", ";");
            String queuekeyid3 = taskqueue.getColumnValues("queuekeyid3", ";");
            SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
            TaskDef taskDef = TaskDef.getInstance(sapphireConnection, taskContext.getWorkflowdefid(), taskContext.getWorkflowdefversionid(), taskContext.getWorkflowdefvariantid(), taskContext.getTaskdefitemid());
            PropertyListCollection taskio = taskDef.getTaskdef().getCollection("taskio");
            for (int i = 0; i < taskio.size(); ++i) {
                PropertyList io = taskio.getPropertyList(i);
                if (!io.getProperty("ioflag").equals("I") || !io.getProperty("ioid").equals(ioid)) continue;
                String variableid = io.getProperty("variableid");
                if (io.getProperty("autoexecprocessing", "A").equals("A")) {
                    this.processTaskExec(sapphireConnection, taskDef, taskContext, variableid, taskqueueid, sdcid, queuekeyid1, queuekeyid2, queuekeyid3);
                    continue block0;
                }
                String[] taskqueuelist = StringUtil.split(taskqueueid, ";");
                String[] keyid1list = StringUtil.split(queuekeyid1, ";");
                String[] keyid2list = StringUtil.split(queuekeyid2, ";");
                String[] keyid3list = StringUtil.split(queuekeyid3, ";");
                for (int j = 0; j < keyid1list.length; ++j) {
                    this.processTaskExec(sapphireConnection, taskDef, taskContext, variableid, taskqueuelist[j], sdcid, keyid1list[j], keyid2list[j], keyid3list[j]);
                }
                continue block0;
            }
        }
    }

    private boolean processTaskExec(SapphireConnection sapphireConnection, TaskDef taskDef, TaskContext taskContext, String variableid, String taskqueueid, String sdcid, String queuekeyid1, String queuekeyid2, String queuekeyid3) throws SapphireException {
        PropertyList startTaskExec = new PropertyList();
        startTaskExec.setProperty("taskdefid", taskContext.getTaskdefid());
        startTaskExec.setProperty("taskdefversionid", taskContext.getTaskdefversionid());
        startTaskExec.setProperty("taskdefvariantid", taskContext.getTaskdefvariantid());
        startTaskExec.setProperty("workflowdefid", taskContext.getWorkflowdefid());
        startTaskExec.setProperty("workflowdefversionid", taskContext.getWorkflowdefversionid());
        startTaskExec.setProperty("workflowdefvariantid", taskContext.getWorkflowdefvariantid());
        this.getActionProcessor().processActionClass(StartTaskExec.class.getName(), startTaskExec);
        String taskexecid = startTaskExec.getProperty("taskexecid");
        PropertyList addTaskExec = new PropertyList();
        addTaskExec.setProperty("taskexecid", taskexecid);
        addTaskExec.setProperty("taskqueueid", taskqueueid);
        addTaskExec.setProperty("queueusage", "E");
        this.getActionProcessor().processAction("AllocateTaskQueueItems", "1", addTaskExec);
        PropertyListCollection variables = taskDef.getTaskdef().getCollection("variables");
        JSONableMap taskVariables = taskDef.setupTaskVariables(variables, taskContext.getWorkflowexecid(), null, this.getQueryProcessor(), sapphireConnection);
        SDIList sdiList = new SDIList();
        sdiList.setSdcid(sdcid);
        sdiList.addSDIList(queuekeyid1, queuekeyid2, queuekeyid3);
        TaskDef.setTaskVariable(taskVariables, variableid, sdiList, this.connectionInfo.getSysuserId(), "autoexecinput");
        PropertyListCollection steps = taskDef.getTaskdef().getCollection("steps");
        if (steps != null && steps.size() > 0) {
            steps.index("stepid");
            String stepid = taskDef.getTaskdef().getProperty("startstepid", steps.getPropertyList(0).getProperty("stepid"));
            while (stepid.length() > 0) {
                stepid = this.executeStep(steps.getIndexedPropertyList(stepid), taskVariables, variables, sapphireConnection, taskContext);
            }
        }
        JSONableMap taskData = new JSONableMap();
        taskData.put("variables", taskVariables);
        return WorkflowManager.completeTask(sapphireConnection, (DBUtil)this.database, taskexecid, taskContext, taskData);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private String executeStep(PropertyList step, JSONableMap taskVariables, PropertyListCollection variables, SapphireConnection sapphireConnection, TaskContext taskContext) throws SapphireException {
        block30: {
            String type = step.getProperty("type");
            PropertyList stepType = this.resolvePropertyList(step.getPropertyList(type), taskVariables, variables, step);
            try {
                SDIList inputSDIList;
                String inputvariabletype;
                String inputvariableid;
                HashMap bindings = new HashMap();
                bindings.put("variables", TaskDef.getTaskVariablesProcessingMap(taskVariables, this.connectionInfo));
                if (type.equalsIgnoreCase("ActionBlock")) {
                    String actionblock = stepType.getProperty("processscript");
                    ActionBlock ab = new ActionBlock(actionblock);
                    ab.setDebugLog("");
                    ab.setGroovyBindings(bindings);
                    this.getActionProcessor().processActionBlock(ab);
                    String log = ab.getDebugLog();
                    PropertyList returnProps = new PropertyList(ab.getReturnProperties());
                    this.updateVariables(returnProps, taskVariables, variables, "step:" + step.getProperty("stepid") + ":actionblockreturn");
                    break block30;
                }
                if (type.equalsIgnoreCase("Groovy")) {
                    String groovyscript = stepType.getProperty("processscript");
                    StringBuffer groovyLog = new StringBuffer();
                    bindings.put("stepproperty", step.getPropertyList("stepprops"));
                    HashMap output = new HashMap();
                    bindings.put("output", output);
                    ProcessingUtil.processScript(sapphireConnection, groovyscript, bindings, groovyLog, "AUTOTASKPROCESSING");
                    String log = groovyLog.toString();
                    this.updateVariables(output, taskVariables, variables, "step:" + step.getProperty("stepid") + ":groovyoutput");
                    break block30;
                }
                if (type.equalsIgnoreCase("Converter")) {
                    String inputsdcid = stepType.getProperty("inputsdcid");
                    inputvariableid = stepType.getProperty("inputvariableid");
                    if (inputvariableid.length() == 0) {
                        throw new SapphireException("Input variable not specified in step properties!");
                    }
                    inputvariabletype = variables.getIndexedPropertyList(inputvariableid).getProperty("type", "string");
                    inputSDIList = null;
                    if (inputvariabletype.equalsIgnoreCase("sdilist")) {
                        inputSDIList = (SDIList)TaskDef.getTaskVariable(taskVariables, inputvariableid);
                    } else {
                        inputSDIList = new SDIList();
                        inputSDIList.setSdcid(inputsdcid);
                        inputSDIList.addSDIList(((JSONableString)TaskDef.getTaskVariable(taskVariables, inputvariableid)).toString());
                    }
                    String outputsdcid = stepType.getProperty("outputsdcid");
                    String outputvariableid = stepType.getProperty("outputvariableid");
                    if (outputvariableid.length() == 0) {
                        throw new SapphireException("Output variable not specified in step properties!");
                    }
                    String outputvariabletype = variables.getIndexedPropertyList(outputvariableid).getProperty("type", "string");
                    SDIList outputSDIList = new SDIList();
                    outputSDIList.setSdcid(outputsdcid);
                    ConverterStep.convertSDIList(this.getSDCProcessor(), this.getQueryProcessor(), this.getDAMProcessor(), inputSDIList, outputSDIList, stepType.getProperty("conversiontype"), stepType.getProperty("columnid"), stepType.getProperty("sql"));
                    if (outputvariabletype.equalsIgnoreCase("sdilist")) {
                        TaskDef.setTaskVariable(taskVariables, outputvariableid, outputSDIList, this.connectionInfo.getSysuserId(), "step:" + step.getProperty("stepid") + ":converter");
                        break block30;
                    } else {
                        TaskDef.setTaskVariable(taskVariables, outputvariableid, new JSONableString(outputSDIList.getKeyid1()), this.connectionInfo.getSysuserId(), "step:" + step.getProperty("stepid") + ":converter");
                    }
                    break block30;
                }
                if (type.equalsIgnoreCase("Splitter")) {
                    String splittersdcid = stepType.getProperty("sdcid");
                    inputvariableid = stepType.getProperty("inputvariableid");
                    if (inputvariableid.length() == 0) {
                        throw new SapphireException("Input variable not specified in step properties!");
                    }
                    inputvariabletype = variables.getIndexedPropertyList(inputvariableid).getProperty("type", "string");
                    inputSDIList = null;
                    if (inputvariabletype.equalsIgnoreCase("sdilist")) {
                        inputSDIList = (SDIList)TaskDef.getTaskVariable(taskVariables, inputvariableid);
                    } else {
                        inputSDIList = new SDIList();
                        inputSDIList.setSdcid(splittersdcid);
                        inputSDIList.addSDIList(((JSONableString)TaskDef.getTaskVariable(taskVariables, inputvariableid)).toString());
                    }
                    String matchvariableid = stepType.getProperty("matchvariableid");
                    if (matchvariableid.length() == 0) {
                        throw new SapphireException("Match variable not specified in step properties!");
                    }
                    String matchvariabletype = variables.getIndexedPropertyList(matchvariableid).getProperty("type", "string");
                    String nomatchvariableid = stepType.getProperty("nomatchvariableid");
                    if (nomatchvariableid.length() == 0) {
                        throw new SapphireException("Match variable not specified in step properties!");
                    }
                    String nomatchvariabletype = variables.getIndexedPropertyList(nomatchvariableid).getProperty("type", "string");
                    SDIList matchSDIList = new SDIList();
                    matchSDIList.setSdcid(splittersdcid);
                    SDIList nomatchSDIList = new SDIList();
                    nomatchSDIList.setSdcid(splittersdcid);
                    SplitterStep.splitSDIList(this.getSDIProcessor(), splittersdcid, inputSDIList, matchSDIList, nomatchSDIList, stepType.getProperty("conditionsoperator", "AND").equals("AND"), stepType.getCollection("conditions"));
                    if (matchvariabletype.equalsIgnoreCase("sdilist")) {
                        TaskDef.setTaskVariable(taskVariables, matchvariableid, matchSDIList, this.connectionInfo.getSysuserId(), "step:" + step.getProperty("stepid") + ":splitter");
                    } else {
                        TaskDef.setTaskVariable(taskVariables, matchvariableid, new JSONableString(matchSDIList.getKeyid1()), this.connectionInfo.getSysuserId(), "step:" + step.getProperty("stepid") + ":splitter");
                    }
                    if (nomatchvariabletype.equalsIgnoreCase("sdilist")) {
                        TaskDef.setTaskVariable(taskVariables, nomatchvariableid, nomatchSDIList, this.connectionInfo.getSysuserId(), "step:" + step.getProperty("stepid") + ":splitter");
                        break block30;
                    } else {
                        TaskDef.setTaskVariable(taskVariables, nomatchvariableid, new JSONableString(nomatchSDIList.getKeyid1()), this.connectionInfo.getSysuserId(), "step:" + step.getProperty("stepid") + ":splitter");
                    }
                    break block30;
                }
                if (!type.equalsIgnoreCase("Noop")) {
                    // empty if block
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to execute step '" + step.getProperty("stepid") + "' in autoexec task '" + LV_TaskDef.getText(taskContext.getTaskdefid(), taskContext.getTaskdefversionid(), taskContext.getTaskdefvariantid()) + ". Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        PropertyList next = step.getPropertyList("next");
        if (next == null) {
            return "";
        }
        next = this.resolvePropertyList(next, taskVariables, variables, step);
        String caseOn = next.getProperty("caseon");
        PropertyListCollection transitions = next.getCollection("transitions");
        if (transitions == null || transitions.size() == 0) {
            return "";
        }
        if (caseOn.length() == 0) {
            return transitions.getPropertyList(0).getProperty("stepid");
        }
        if (caseOn.startsWith("$G{")) {
            HashMap bindMap = WorkflowManager.getGroovyBindMap(sapphireConnection, (DBUtil)this.database, taskContext, taskVariables);
            try {
                String script = ProcessingUtil.insertHeaderCode(caseOn, "def html = sapphireobjects.html;", false);
                String nextstepid = this.getTransitionStep(transitions, GroovyUtil.getInstance(this.connectionInfo).evaluateSecure(script, bindMap));
                if (nextstepid.length() > 0) {
                    return nextstepid;
                }
                throw new SapphireException("Failed to derive next step from Groovy expression at step '" + step.getProperty("stepid") + "' in autoexec task '" + LV_TaskDef.getText(taskContext.getTaskdefid(), taskContext.getTaskdefversionid(), taskContext.getTaskdefvariantid()));
            }
            catch (Exception e) {
                throw new SapphireException("Failed to evaluate groovy caseon expression at step '" + step.getProperty("stepid") + "' in autoexec task '" + LV_TaskDef.getText(taskContext.getTaskdefid(), taskContext.getTaskdefversionid(), taskContext.getTaskdefvariantid()));
            }
        }
        String nextstepid = this.getTransitionStep(transitions, caseOn);
        if (nextstepid.length() > 0) {
            return nextstepid;
        }
        throw new SapphireException("Failed to derive next step at step '" + step.getProperty("stepid") + "' in autoexec task '" + LV_TaskDef.getText(taskContext.getTaskdefid(), taskContext.getTaskdefversionid(), taskContext.getTaskdefvariantid()));
    }

    private String getTransitionStep(PropertyListCollection transitions, String caseValue) {
        for (int i = 0; i < transitions.size(); ++i) {
            PropertyList transition = transitions.getPropertyList(i);
            if (transition.getProperty("case").equals(caseValue)) {
                return transition.getProperty("stepid");
            }
            if (!transition.getProperty("case").equalsIgnoreCase("(else)") || i != transitions.size() - 1) continue;
            return transition.getProperty("stepid");
        }
        return "";
    }

    private void updateVariables(HashMap output, JSONableMap taskVariables, PropertyListCollection variablesDef, String source) {
        for (Object outputkey : output.keySet()) {
            String outputid = outputkey instanceof JSONableString ? ((JSONableString)outputkey).toString() : (String)outputkey;
            Object outputvalue = output.get(outputkey);
            PropertyList variableDef = variablesDef.getIndexedPropertyList(outputid);
            if (variableDef != null) {
                if (variableDef.getProperty("type").equalsIgnoreCase("sdilist") && outputvalue instanceof SDIList) {
                    TaskDef.setTaskVariable(taskVariables, outputid, (SDIList)outputvalue, this.connectionInfo.getSysuserId(), source);
                    continue;
                }
                if (variableDef.getProperty("type").equalsIgnoreCase("sdilist") && (outputvalue instanceof JSONableString || outputvalue instanceof String)) {
                    SDIList value = new SDIList();
                    String keyid1 = outputvalue instanceof JSONableString ? ((JSONableString)outputvalue).toString() : (String)outputvalue;
                    if (keyid1 == null || keyid1.length() <= 0) continue;
                    value.addSDIList(keyid1);
                    TaskDef.setTaskVariable(taskVariables, outputid, value, this.connectionInfo.getSysuserId(), source);
                    continue;
                }
                if (!variableDef.getProperty("type").equalsIgnoreCase("string") || !(outputvalue instanceof JSONableString) && !(outputvalue instanceof String)) continue;
                TaskDef.setTaskVariable(taskVariables, outputid, outputvalue instanceof JSONableString ? (JSONableString)outputvalue : new JSONableString((String)outputvalue), this.connectionInfo.getSysuserId(), source);
                continue;
            }
            if (outputvalue instanceof String) {
                TaskDef.setTaskVariable(taskVariables, outputid, new JSONableString((String)outputvalue), this.connectionInfo.getSysuserId(), source);
                continue;
            }
            if (!(outputvalue instanceof JSONable)) continue;
            TaskDef.setTaskVariable(taskVariables, outputid, (JSONable)outputvalue, this.connectionInfo.getSysuserId(), source);
        }
    }

    private PropertyList resolvePropertyList(PropertyList propertyList, JSONableMap taskVariables, PropertyListCollection variables, PropertyList step) {
        PropertyList newPropertyList = new PropertyList();
        if (propertyList != null) {
            for (String propertyid : propertyList.keySet()) {
                if (propertyList.isSimple(propertyid)) {
                    newPropertyList.setProperty(propertyid, WorkflowManager.resolveTokens(propertyList.getProperty(propertyid), taskVariables, variables, step, this.connectionInfo));
                    continue;
                }
                if (propertyList.isPropertyList(propertyid)) {
                    newPropertyList.setProperty(propertyid, this.resolvePropertyList(propertyList.getPropertyList(propertyid), taskVariables, variables, step));
                    continue;
                }
                if (!propertyList.isCollection(propertyid)) continue;
                newPropertyList.setProperty(propertyid, this.resolveCollection(propertyList.getCollection(propertyid), taskVariables, variables, step));
            }
        }
        return newPropertyList;
    }

    private PropertyListCollection resolveCollection(PropertyListCollection collection, JSONableMap taskVariables, PropertyListCollection variables, PropertyList step) {
        PropertyListCollection newCollection = new PropertyListCollection();
        if (collection != null) {
            for (int i = 0; i < collection.size(); ++i) {
                newCollection.add(this.resolvePropertyList(collection.getPropertyList(i), taskVariables, variables, step));
            }
        }
        return newCollection;
    }
}

