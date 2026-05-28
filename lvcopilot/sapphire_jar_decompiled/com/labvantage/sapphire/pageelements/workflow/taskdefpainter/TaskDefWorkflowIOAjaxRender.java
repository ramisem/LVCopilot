/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflow;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.Browser;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefWorkflowIOAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54600 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                PropertyListCollection taskio;
                PropertyList taskprops = new PropertyList(new JSONObject(props));
                StringBuffer html = new StringBuffer();
                StringBuffer script = new StringBuffer();
                StringBuffer style = new StringBuffer();
                if (taskprops.getCollection("taskio") == null) {
                    taskio = new PropertyListCollection();
                    taskprops.setProperty("taskio", taskio);
                } else {
                    taskio = taskprops.getCollection("taskio");
                }
                if (ajaxResponse.getRequestParameter("scriptonly", "N").equalsIgnoreCase("Y")) {
                    String windowid = ajaxResponse.getRequestParameter("windowid", "");
                    if (windowid.length() > 0) {
                        script.append(TaskDefWorkflow.getIOScript(windowid, taskio, TaskDefWorkflow.getConnectors(this.getSDIProcessor()), ajaxResponse.getRequestParameter("labels", "N").equalsIgnoreCase("Y"), this.getConnectionProcessor().getSapphireConnection(), this.logger));
                        ajaxResponse.addCallbackArgument("script", script.toString());
                    } else {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Windowid not provided."));
                    }
                } else {
                    TaskDefWorkflow.renderWindow(taskprops, html, script, style, true, false, TaskDefWorkflow.getConnectors(this.getSDIProcessor()), this.getConnectionId(), new Browser(request), this.logger);
                    ajaxResponse.addCallbackArgument("properties", taskprops.toJSONString(false));
                    ajaxResponse.addCallbackArgument("html", html.toString());
                    ajaxResponse.addCallbackArgument("script", script.toString());
                    ajaxResponse.addCallbackArgument("style", style.toString());
                }
                ajaxResponse.addCallbackArgument("ioid", ajaxResponse.getRequestParameter("ioid", ""));
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

