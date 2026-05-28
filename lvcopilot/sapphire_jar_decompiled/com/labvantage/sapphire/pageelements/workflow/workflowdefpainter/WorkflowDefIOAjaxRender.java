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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowDefIOAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                PropertyList taskprops = new PropertyList(new JSONObject(props));
                try {
                    String fromTask = ajaxResponse.getRequestParameter("fromtask", "");
                    String toTask = ajaxResponse.getRequestParameter("totask", "");
                    String fromIO = ajaxResponse.getRequestParameter("fromio", "");
                    String toIO = ajaxResponse.getRequestParameter("toio", "");
                    if (fromTask.length() > 0 && toTask.length() > 0 && fromIO.length() > 0 && toIO.length() > 0) {
                        PropertyListCollection tasks = taskprops.getCollection("tasks");
                        if (tasks != null) {
                            PropertyList task = tasks.find("taskdefitemid", fromTask);
                            if (task != null) {
                                boolean remove;
                                PropertyListCollection ios = task.getCollection("taskio");
                                if (ios == null) {
                                    ios = new PropertyListCollection();
                                    task.setProperty("taskio", ios);
                                }
                                if (remove = ajaxResponse.getRequestParameter("remove", "N").equalsIgnoreCase("Y")) {
                                    boolean fullDelete = false;
                                    int toDelete = -1;
                                    for (int i = 0; i < ios.size(); ++i) {
                                        PropertyList io = ios.getPropertyList(i);
                                        if (!io.getProperty("ioflag", "O").equalsIgnoreCase("O") || !io.getProperty("ioid").equals(fromIO)) continue;
                                        if (toDelete > -1) {
                                            fullDelete = true;
                                        }
                                        if (!io.getProperty("connecttaskdefitemid").equals(toTask) || !io.getProperty("connectioid").equals(toIO)) continue;
                                        toDelete = i;
                                    }
                                    if (toDelete > -1) {
                                        if (fullDelete) {
                                            ios.remove(toDelete);
                                        } else {
                                            PropertyList io = ios.getPropertyList(toDelete);
                                            io.setProperty("connecttaskdefitemid", "");
                                            io.setProperty("connectioid", "");
                                        }
                                    }
                                } else {
                                    PropertyList io = ios.find("ioid", fromIO);
                                    if (io != null) {
                                        if (io.getProperty("connecttaskdefitemid", "").length() > 0 && io.getProperty("connectioid", "").length() > 0) {
                                            io = io.copy();
                                            ios.add(io);
                                        }
                                        io.setProperty("connecttaskdefitemid", toTask);
                                        io.setProperty("connectioid", toIO);
                                        PropertyList totask = tasks.find("taskdefitemid", toTask);
                                        if (totask != null) {
                                            PropertyList toio;
                                            PropertyListCollection toios = totask.getCollection("taskio");
                                            if (toios == null) {
                                                toios = new PropertyListCollection();
                                                totask.setProperty("taskio", toios);
                                            }
                                            if ((toio = toios.find("ioid", toIO)) == null) {
                                                ajaxResponse.setError("Could not find to io " + toIO + ".");
                                            }
                                        } else {
                                            ajaxResponse.setError("Could not find to task " + toTask + ".");
                                        }
                                    } else {
                                        ajaxResponse.setError("Could not find from io " + fromIO + ".");
                                    }
                                }
                                ajaxResponse.addCallbackArgument("properties", taskprops.toJSONString(false));
                            } else {
                                ajaxResponse.setError("Could not find from task " + fromTask + ".");
                            }
                        } else {
                            ajaxResponse.setError("No tasks defined.");
                        }
                    } else {
                        ajaxResponse.setError("No from or to step provided.");
                    }
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("No type string provided."));
                }
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

