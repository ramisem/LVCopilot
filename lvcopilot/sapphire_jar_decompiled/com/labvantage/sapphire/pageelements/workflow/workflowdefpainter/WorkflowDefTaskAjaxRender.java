/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.sapphire.pageelements.propertybuilder.PropertyBuilder;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflow;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.accessor.WorkflowProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowDefTaskAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54523 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block49: {
            ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
            String props = ajaxResponse.getRequestParameter("properties", "");
            if (props.length() > 0) {
                try {
                    PropertyList workflowprops = new PropertyList(new JSONObject(props));
                    try {
                        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
                        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
                        String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
                        boolean remove = ajaxResponse.getRequestParameter("remove", "N").equalsIgnoreCase("Y");
                        boolean refresh = ajaxResponse.getRequestParameter("refresh", "N").equalsIgnoreCase("Y");
                        boolean translate = ajaxResponse.getRequestParameter("translate", "N").equalsIgnoreCase("Y");
                        boolean swap = ajaxResponse.getRequestParameter("swap", "N").equalsIgnoreCase("Y");
                        PropertyListCollection tasks = workflowprops.getCollection("tasks");
                        if (remove) {
                            String taskid = ajaxResponse.getRequestParameter("taskdefitemid", "");
                            if (taskid.length() > 0) {
                                if (tasks == null) break block49;
                                PropertyList task = tasks.find("taskdefitemid", taskid);
                                if (task != null) {
                                    TaskContext taskContext = new TaskContext();
                                    taskContext.setWorkflowdefid(keyid1);
                                    taskContext.setWorkflowdefversionid(keyid2);
                                    taskContext.setWorkflowdefvariantid(keyid3);
                                    taskContext.setTaskdefid(task.getProperty("taskdefid"));
                                    taskContext.setTaskdefversionid(task.getProperty("taskdefversionid"));
                                    taskContext.setTaskdefvariantid(task.getProperty("taskdefvariantid"));
                                    WorkflowProcessor wfProcessor = new WorkflowProcessor(this.getConnectionId());
                                    DataSet ds = null;
                                    try {
                                        ds = wfProcessor.getTaskQueueItemsDataSet(taskContext, "", "W;Wevent;Wtimer;E;S;A;Aevent;Atimer");
                                    }
                                    catch (Exception e) {
                                        this.logger.warn("Could not obtain queue.");
                                    }
                                    if (ds != null && ds.getRowCount() > 0) {
                                        this.logDebug("Task queue found. Deleting...");
                                        wfProcessor.deleteTaskQueueItems(taskContext, "");
                                    }
                                    tasks.remove(task);
                                    ajaxResponse.addCallbackArgument("properties", workflowprops.toJSONString(false));
                                    break block49;
                                }
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find task to remove."));
                                break block49;
                            }
                            ajaxResponse.setError(this.getTranslationProcessor().translate("No task id provided."));
                            break block49;
                        }
                        if (refresh || swap) {
                            String taskid = ajaxResponse.getRequestParameter("taskdefitemid", ajaxResponse.getRequestParameter("taskid", ""));
                            if (taskid.length() > 0) {
                                if (tasks == null) break block49;
                                PropertyList task = tasks.find("taskdefitemid", taskid);
                                if (task != null) {
                                    boolean prototype = false;
                                    if (swap) {
                                        String swaptaskdefid = ajaxResponse.getRequestParameter("swaptaskdefid", "");
                                        String swaptaskdefversionid = ajaxResponse.getRequestParameter("swaptaskdefversionid", "1");
                                        String swaptaskdefvariantid = ajaxResponse.getRequestParameter("swaptaskdefvariantid", "1");
                                        prototype = ajaxResponse.getRequestParameter("swapprototype", "N").equalsIgnoreCase("Y");
                                        if (prototype) {
                                            String webtaskid = "__prototype" + (int)Math.floor(Math.random() * 1000.0);
                                            task.setProperty("prototype", "Y");
                                            task.setProperty("taskdefid", webtaskid);
                                            task.setProperty("taskdefversionid", "");
                                            task.setProperty("taskdefvariantid", "");
                                        } else {
                                            PropertyList swapTaskProps = TaskDefMaint.getTaskData("LV_TaskDef", swaptaskdefid, swaptaskdefversionid, swaptaskdefvariantid, false, this.getSDIProcessor(), this.logger);
                                            if (swapTaskProps != null && WorkflowDefMaint.isValidTaskSwap(task, swapTaskProps, tasks, false)) {
                                                task.setProperty("taskdefid", swaptaskdefid);
                                                task.setProperty("taskdefversionid", swaptaskdefversionid);
                                                task.setProperty("taskdefvariantid", swaptaskdefvariantid);
                                                ArrayList foundI = new ArrayList();
                                                ArrayList foundO = new ArrayList();
                                                PropertyListCollection oldTaskIOs = task.getCollection("taskio");
                                                PropertyListCollection newTaskIOs = (PropertyListCollection)swapTaskProps.getCollection("taskio").clone();
                                                if (oldTaskIOs != null && newTaskIOs != null) {
                                                    for (int old_io = 0; old_io < oldTaskIOs.size(); ++old_io) {
                                                        PropertyList oldTaskIO = oldTaskIOs.getPropertyList(old_io);
                                                        String old_ioid = oldTaskIO.getProperty("ioid");
                                                        String old_connectortypeid = oldTaskIO.getProperty("connectortypeid");
                                                        if (oldTaskIO.getProperty("ioflag", "I").equalsIgnoreCase("O")) {
                                                            String old_connecttaskdefitemid = oldTaskIO.getProperty("connecttaskdefitemid");
                                                            String old_connectioid = oldTaskIO.getProperty("connectioid");
                                                            String new_ioid = "";
                                                            String new_connectortypeid = "";
                                                            for (int new_io = 0; new_io < newTaskIOs.size(); ++new_io) {
                                                                PropertyList newTaskIO = newTaskIOs.getPropertyList(new_io);
                                                                if (!newTaskIO.getProperty("ioflag", "I").equalsIgnoreCase("O") || !old_connectortypeid.equals(newTaskIO.getProperty("connectortypeid"))) continue;
                                                                new_ioid = newTaskIO.getProperty("ioid");
                                                                new_connectortypeid = newTaskIO.getProperty("connectortypeid");
                                                                newTaskIOs.remove(new_io);
                                                                break;
                                                            }
                                                            if (new_ioid.length() <= 0) continue;
                                                            oldTaskIO.setProperty("ioid", new_ioid);
                                                            continue;
                                                        }
                                                        String new_ioid = "";
                                                        String new_connectortypeid = "";
                                                        for (int new_io = 0; new_io < newTaskIOs.size(); ++new_io) {
                                                            PropertyList newTaskIO = newTaskIOs.getPropertyList(new_io);
                                                            if (!newTaskIO.getProperty("ioflag", "I").equalsIgnoreCase("I") || !old_connectortypeid.equals(newTaskIO.getProperty("connectortypeid"))) continue;
                                                            new_ioid = newTaskIO.getProperty("ioid");
                                                            new_connectortypeid = newTaskIO.getProperty("connectortypeid");
                                                            newTaskIOs.remove(new_io);
                                                            break;
                                                        }
                                                        if (new_ioid.length() <= 0) continue;
                                                        oldTaskIO.setProperty("ioid", new_ioid);
                                                        for (int t = 0; t < tasks.size(); ++t) {
                                                            PropertyList foundTaskio;
                                                            PropertyList foundTask = tasks.getPropertyList(t);
                                                            PropertyListCollection foundTaskios = foundTask.getCollection("taskio");
                                                            if (foundTaskios == null || foundTaskios.size() <= 0 || (foundTaskio = foundTaskios.find("connectioid", old_ioid)) == null) continue;
                                                            foundTaskio.setProperty("connectioid", new_ioid);
                                                        }
                                                    }
                                                }
                                            } else {
                                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find task to swap or is invalid swap."));
                                            }
                                        }
                                    }
                                    if (swap && !prototype || ajaxResponse.getRequestParameter("reset").equalsIgnoreCase("Y")) {
                                        boolean starttaskflag = task.getProperty("starttaskflag").equalsIgnoreCase("Y");
                                        boolean endtaskflag = task.getProperty("endtaskflag").equalsIgnoreCase("Y");
                                        ArrayList<String> toremove = new ArrayList<String>();
                                        for (Object okey : task.keySet()) {
                                            String key = okey.toString();
                                            if (key.equalsIgnoreCase("taskdefid") || key.equalsIgnoreCase("taskdefversionid") || key.equalsIgnoreCase("taskdefvariantid") || key.equalsIgnoreCase("taskdefitemid") || key.equalsIgnoreCase("x") || key.equalsIgnoreCase("y") || key.equalsIgnoreCase("taskio") || swap && key.equalsIgnoreCase("bubbletext")) continue;
                                            toremove.add(key);
                                        }
                                        for (String key : toremove) {
                                            task.remove(key);
                                        }
                                        if (starttaskflag) {
                                            task.setProperty("starttaskflag", "Y");
                                        }
                                        if (endtaskflag) {
                                            task.setProperty("endtaskflag", "Y");
                                        }
                                    }
                                    PropertyList toolprops = WorkflowDefMaint.getTaskData(task.getProperty("taskdefid", ""), task.getProperty("taskdefversionid", ""), task.getProperty("taskdefvariantid", ""), this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                                    PropertyList pbprops = new PropertyList();
                                    pbprops.setProperty("definitionxml", "workflowdef.xml");
                                    PropertyBuilder pb = new PropertyBuilder(this.getConnectionId(), pbprops, request);
                                    PropertyDefinitionList propertyDef = pb.getPropertyDefinition();
                                    WorkflowDefPainter.Connector connector = WorkflowDefPainter.Connector.FLOWCHART;
                                    try {
                                        connector = WorkflowDefPainter.Connector.valueOf(ajaxResponse.getRequestParameter("connector", workflowprops.getProperty("connector", WorkflowDefPainter.Connector.FLOWCHART.toString())).toUpperCase());
                                    }
                                    catch (Exception e) {
                                        connector = WorkflowDefPainter.Connector.FLOWCHART;
                                    }
                                    TaskDefWorkflow.Appearance appearance = null;
                                    if (ajaxResponse.getRequestParameter("appearance", "").length() > 0) {
                                        try {
                                            appearance = TaskDefWorkflow.Appearance.valueOf(ajaxResponse.getRequestParameter("appearance", "").toUpperCase());
                                        }
                                        catch (Exception e) {
                                            appearance = null;
                                        }
                                    }
                                    String[] color = null;
                                    if (ajaxResponse.getRequestParameter("color", "").length() > 0) {
                                        color = StringUtil.split(ajaxResponse.getRequestParameter("color", ""), ";");
                                    }
                                    StringBuffer html = new StringBuffer();
                                    StringBuffer script = new StringBuffer();
                                    StringBuffer style = new StringBuffer();
                                    String id = WorkflowDefPainter.renderWindow(task, toolprops == null ? new PropertyList() : toolprops, workflowprops, propertyDef, connector, appearance, color, tasks.size() - 1, html, script, style, false, false, ajaxResponse.getRequestParameter("renderhtml5", "N").equalsIgnoreCase("Y"), workflowprops.getProperty("rtl", "N").equalsIgnoreCase("Y"), new Browser(request), this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), translate ? new TranslationProcessor(this.getConnectionId()) : null, this.logger);
                                    ajaxResponse.addCallbackArgument("properties", workflowprops.toJSONString(false));
                                    ajaxResponse.addCallbackArgument("html", html.toString());
                                    ajaxResponse.addCallbackArgument("script", script.toString());
                                    ajaxResponse.addCallbackArgument("style", style.toString());
                                    ajaxResponse.addCallbackArgument("id", id);
                                    break block49;
                                }
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find task to refresh."));
                                break block49;
                            }
                            ajaxResponse.setError(this.getTranslationProcessor().translate("No task id provided."));
                            break block49;
                        }
                        boolean prototype = false;
                        String webtaskid = ajaxResponse.getRequestParameter("taskdefid", "");
                        if (webtaskid.length() == 0) {
                            webtaskid = "__prototype" + (int)Math.floor(Math.random() * 1000.0);
                            prototype = true;
                        }
                        if (webtaskid.length() > 0) {
                            String webvariantid;
                            String webversionid = ajaxResponse.getRequestParameter("taskdefversionid", "1");
                            PropertyList toolprops = WorkflowDefMaint.getTaskData(webtaskid, webversionid, webvariantid = ajaxResponse.getRequestParameter("taskdefvariantid", "1"), this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                            if (toolprops != null || prototype) {
                                String x = ajaxResponse.getRequestParameter("x", "200");
                                String y = ajaxResponse.getRequestParameter("y", "200");
                                PropertyList task = new PropertyList();
                                task.setProperty("x", x);
                                task.setProperty("y", y);
                                if (tasks == null) {
                                    tasks = new PropertyListCollection();
                                    workflowprops.setProperty("tasks", tasks);
                                }
                                if (prototype) {
                                    task.setProperty("prototype", "Y");
                                    task.setProperty("taskdefid", webtaskid);
                                }
                                if (tasks.size() == 0) {
                                    task.setProperty("starttaskflag", "Y");
                                }
                                tasks.add(task);
                                StringBuffer html = new StringBuffer();
                                StringBuffer script = new StringBuffer();
                                StringBuffer style = new StringBuffer();
                                WorkflowDefPainter.Connector connector = WorkflowDefPainter.Connector.FLOWCHART;
                                try {
                                    connector = WorkflowDefPainter.Connector.valueOf(ajaxResponse.getRequestParameter("connector", workflowprops.getProperty("connector", WorkflowDefPainter.Connector.FLOWCHART.toString())).toUpperCase());
                                }
                                catch (Exception e) {
                                    connector = WorkflowDefPainter.Connector.FLOWCHART;
                                }
                                TaskDefWorkflow.Appearance appearance = null;
                                if (ajaxResponse.getRequestParameter("appearance", "").length() > 0) {
                                    try {
                                        appearance = TaskDefWorkflow.Appearance.valueOf(ajaxResponse.getRequestParameter("appearance", "").toUpperCase());
                                    }
                                    catch (Exception e) {
                                        appearance = null;
                                    }
                                }
                                String[] color = null;
                                if (ajaxResponse.getRequestParameter("color", "").length() > 0) {
                                    color = StringUtil.split(ajaxResponse.getRequestParameter("color", ""), ";");
                                }
                                PropertyList pbprops = new PropertyList();
                                pbprops.setProperty("definitionxml", "workflowdef.xml");
                                PropertyBuilder pb = new PropertyBuilder(this.getConnectionId(), pbprops, request);
                                PropertyDefinitionList propertyDef = pb.getPropertyDefinition();
                                String id = WorkflowDefPainter.renderWindow(task, toolprops, workflowprops, propertyDef, connector, appearance, color, tasks.size() - 1, html, script, style, false, false, ajaxResponse.getRequestParameter("renderhtml5", "N").equalsIgnoreCase("Y"), workflowprops.getProperty("rtl", "N").equalsIgnoreCase("Y"), new Browser(request), this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), translate ? new TranslationProcessor(this.getConnectionId()) : null, this.logger);
                                task.setProperty("taskdefitemid", id);
                                ajaxResponse.addCallbackArgument("properties", workflowprops.toJSONString(false));
                                ajaxResponse.addCallbackArgument("html", html.toString());
                                ajaxResponse.addCallbackArgument("script", script.toString());
                                ajaxResponse.addCallbackArgument("style", style.toString());
                                ajaxResponse.addCallbackArgument("id", "" + id);
                                break block49;
                            }
                            HashMap<String, String> valueMap = new HashMap<String, String>();
                            valueMap.put("webtaskid", webtaskid);
                            valueMap.put("webversionid", webversionid);
                            valueMap.put("webvariantid", webvariantid);
                            ajaxResponse.setError(this.getTranslationProcessor().translate("No tool with id [webtaskid] and version [webversionid] and variant [webvariantid] could be found.", valueMap));
                            break block49;
                        }
                        ajaxResponse.setError(this.getTranslationProcessor().translate("No task id provided."));
                    }
                    catch (Exception e) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Could not render task."));
                    }
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
            }
        }
        ajaxResponse.print();
    }
}

