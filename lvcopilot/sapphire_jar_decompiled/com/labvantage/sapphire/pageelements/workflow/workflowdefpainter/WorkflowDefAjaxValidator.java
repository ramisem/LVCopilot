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

import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowDefAjaxValidator
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 57245 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                PropertyListCollection tasks;
                WorkflowDefMaint.Mode mode;
                PropertyList workflowprops = new PropertyList(new JSONObject(props));
                String sdcid = ajaxResponse.getRequestParameter("sdcid", "LV_TaskDef");
                String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
                String keyid2 = ajaxResponse.getRequestParameter("keyid2", "1");
                String keyid3 = ajaxResponse.getRequestParameter("keyid3", "1");
                String callback = ajaxResponse.getRequestParameter("continuecallback", "");
                try {
                    mode = WorkflowDefMaint.Mode.valueOf(ajaxResponse.getRequestParameter("mode", "edit").toUpperCase());
                }
                catch (Exception e) {
                    mode = WorkflowDefMaint.Mode.EDIT;
                }
                StringBuffer msg = new StringBuffer();
                StringBuffer failedfields = new StringBuffer();
                StringBuffer passedfields = new StringBuffer();
                if (mode == WorkflowDefMaint.Mode.ADD) {
                    if (keyid1.length() == 0 || keyid3.length() == 0) {
                        msg.append("Please provide a workflow id and variant.");
                        failedfields.append("workflowdefid");
                        failedfields.append(";");
                        failedfields.append("workflowdefvariantid");
                    } else {
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT workflowdefid FROM workflowdef WHERE workflowdefid=? AND workflowdefvariantid=?", new Object[]{keyid1, keyid3});
                        if (ds == null) {
                            ajaxResponse.setError("Could not query existing tasks.");
                        } else if (ds.getRowCount() > 0) {
                            if (msg.length() > 0) {
                                msg.append("\n\n");
                            }
                            msg.append("The entered workflow id and variant already exists. Please enter an alternative.");
                            failedfields.append("workflowdefid");
                            failedfields.append(";");
                            failedfields.append("variantid");
                        } else {
                            passedfields.append("workflowdefid");
                            passedfields.append(";");
                            passedfields.append("variantid");
                        }
                    }
                }
                if ((tasks = workflowprops.getCollection("tasks")) != null && tasks.size() > 0) {
                    int foundStart = 0;
                    int foundEnd = 0;
                    int foundPrototype = 0;
                    boolean foundboth = false;
                    for (int i = 0; i < tasks.size(); ++i) {
                        PropertyListCollection basedOnVariables;
                        PropertyList task = tasks.getPropertyList(i);
                        String taskText = task.getProperty("shorttitle", task.getProperty("longtitle"));
                        taskText = taskText.length() > 0 ? taskText + " (" + task.getProperty("taskdefitemid") + ")" : task.getProperty("taskdefitemid");
                        boolean start = task.getProperty("starttaskflag", "N").equalsIgnoreCase("Y");
                        boolean end = task.getProperty("endtaskflag", "N").equalsIgnoreCase("Y");
                        boolean prototype = task.getProperty("prototype", "N").equalsIgnoreCase("Y");
                        if (start) {
                            ++foundStart;
                        }
                        if (end) {
                            ++foundEnd;
                        }
                        if (prototype) {
                            ++foundPrototype;
                        }
                        if (start && end) {
                            foundboth = true;
                        }
                        if (prototype) continue;
                        PropertyList basedOnTask = WorkflowDefMaint.getTaskData(task.getProperty("taskdefid", ""), task.getProperty("taskdefversionid", ""), task.getProperty("taskdefvariantid", ""), this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                        PropertyListCollection propertyListCollection = basedOnVariables = basedOnTask != null ? basedOnTask.getCollection("variables") : null;
                        if (basedOnTask != null) {
                            PropertyListCollection ios = task.getCollection("taskio");
                            if (ios != null && ios.size() > 0) {
                                PropertyListCollection basedOnIos = basedOnTask.getCollection("taskio");
                                if (basedOnIos != null && basedOnIos.size() > 0) {
                                    for (int io = 0; io < ios.size(); ++io) {
                                        PropertyList ioP = ios.getPropertyList(io);
                                        String ioid = ioP.getProperty("ioid");
                                        PropertyList findIO = basedOnIos.find("ioid", ioid);
                                        if (findIO != null) continue;
                                        msg.append("Task ").append(task.getProperty("taskdefid", "")).append("(").append(task.getProperty("taskdefversionid", "")).append(" - ").append(task.getProperty("taskdefid", "")).append(") has an invalid Input and/or Output queue. Try resetting task.");
                                        break;
                                    }
                                } else {
                                    msg.append("Task ").append(task.getProperty("taskdefid", "")).append("(").append(task.getProperty("taskdefversionid", "")).append(" - ").append(task.getProperty("taskdefid", "")).append(") has invalid number of Input and/or Output queues. Try resetting task.");
                                }
                            }
                            if (basedOnVariables == null || basedOnVariables.size() <= 0) continue;
                            PropertyListCollection variables = task.getCollection("variables");
                            for (int v = 0; v < basedOnVariables.size(); ++v) {
                                PropertyList basedOnVar = basedOnVariables.getPropertyList(v);
                                if (!basedOnVar.getProperty("mandatory", "N").equalsIgnoreCase("Y")) continue;
                                PropertyList var = variables != null ? variables.find("variableid", basedOnVar.getProperty("variableid")) : null;
                                String defaultVal = var == null || !var.containsKey("defaultvalue") ? basedOnVar.getProperty("defaultvalue", "") : var.getProperty("defaultvalue", "");
                                this.logger.info("Variable '" + basedOnVar.getProperty("variableid") + "' on task '" + taskText + "' is mandatory and will be prompted for on execution.");
                            }
                            continue;
                        }
                        if (basedOnTask != null) continue;
                        if (msg.length() > 0) {
                            msg.append("\n\n");
                        }
                        msg.append("Task ").append(task.getProperty("taskdefid", "")).append("(").append(task.getProperty("taskdefversionid", "")).append(" - ").append(task.getProperty("taskdefid", "")).append(") is used but does not exist and therefore workflow cannot be saved.");
                    }
                    if (foundPrototype == 0 && foundStart < 1) {
                        if (msg.length() > 0) {
                            msg.append("\n\n");
                        }
                        msg.append("There is no start task defined and therefore the workflow cannot be run.");
                    }
                    if (foundPrototype == 0 && foundEnd > 1) {
                        if (msg.length() > 0) {
                            msg.append("\n\n");
                        }
                        msg.append("Only one end task can be defined and therefore the workflow cannot be run.");
                    }
                    if (foundPrototype == 0 && foundboth) {
                        if (msg.length() > 0) {
                            msg.append("\n\n");
                        }
                        msg.append("A task has been set as both a start and an end task which is invalid.");
                    }
                    if (foundPrototype > 0 && mode == WorkflowDefMaint.Mode.EDIT) {
                        if (msg.length() > 0) {
                            msg.append("\n\n");
                        }
                        if (workflowprops.getProperty("__queue", "").length() > 0) {
                            msg.append("The workflow has been assigned items and therefore cannot contain prototype tasks. Please remove the prototype" + (foundPrototype > 1 ? "s" : "") + " before saving.");
                        } else if (workflowprops.getProperty("__exec", "N").equalsIgnoreCase("Y")) {
                            msg.append("The workflow is in an execution and cannot contain prototype tasks. Please remove the prototype" + (foundPrototype > 1 ? "s" : "") + " before saving.");
                        }
                    }
                }
                ajaxResponse.addCallbackArgument("msg", msg.toString());
                ajaxResponse.addCallbackArgument("failedfields", failedfields.toString());
                ajaxResponse.addCallbackArgument("passedfields", passedfields.toString());
                ajaxResponse.addCallbackArgument("continuecallback", callback);
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

