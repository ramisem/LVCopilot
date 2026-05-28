/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefEditorLookup;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefEditorLookupAjaxRender
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block19: {
            VIEWTYPE type;
            ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
            String taskdefid = ajaxResponse.getRequestParameter("taskdefid", "");
            String taskdefversionid = ajaxResponse.getRequestParameter("taskdefversionid", "1");
            String taskdefvariantid = ajaxResponse.getRequestParameter("taskdefvariantid", "1");
            StringBuffer html = new StringBuffer();
            try {
                type = VIEWTYPE.valueOf(ajaxResponse.getRequestParameter("type", (Object)((Object)VIEWTYPE.INPUT) + "").toUpperCase());
            }
            catch (Exception e) {
                type = VIEWTYPE.INPUT;
            }
            PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
            if (pageContext == null) {
                ajaxResponse.setError("Could not create page context.");
            }
            if (taskdefid.length() > 0) {
                try {
                    PropertyList props = WorkflowDefMaint.getTaskData(taskdefid, taskdefversionid, taskdefvariantid, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                    if (type == VIEWTYPE.INPUT) {
                        html.append(TaskDefEditorLookup.getInputsHTML(props, "", this.getConnectionId(), this.getTranslationProcessor(), RequestContext.getInstance(request).getPropertyList("userconfig"), this.logger));
                        ajaxResponse.addCallbackArgument("html", html.toString());
                        ajaxResponse.addCallbackArgument("io", props.getCollection("taskio") != null ? props.getCollection("taskio").toJSONString(false, false) : "");
                        break block19;
                    }
                    if (type == VIEWTYPE.VARIABLES) {
                        String varprops = ajaxResponse.getRequestParameter("variables", "");
                        PropertyListCollection variables = new PropertyListCollection();
                        if (varprops.length() > 0) {
                            try {
                                variables.setJSONString(varprops);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        html.append(TaskDefEditorLookup.getVariablesHTML(props, variables, this.getConnectionId(), this.getTranslationProcessor(), RequestContext.getInstance(request).getPropertyList("userconfig"), pageContext, this.logger));
                        ajaxResponse.addCallbackArgument("html", html.toString());
                        ajaxResponse.addCallbackArgument("variables", variables != null ? variables.toJSONString(false, false) : "");
                        break block19;
                    }
                    String ioid = ajaxResponse.getRequestParameter("ioid", "");
                    String varprops = ajaxResponse.getRequestParameter("variables", "");
                    PropertyListCollection variables = new PropertyListCollection();
                    if (varprops.length() > 0) {
                        try {
                            variables.setJSONString(varprops);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    PropertyList output = new PropertyList();
                    output.setProperty("taskdefid", taskdefid);
                    output.setProperty("taskdefversionid", taskdefversionid);
                    output.setProperty("taskdefvariantid", taskdefvariantid);
                    output.setProperty("ioid", ioid);
                    output.setProperty("variables", variables);
                    ajaxResponse.addCallbackArgument("xml", output.toXMLString());
                    ajaxResponse.addCallbackArgument("json", output.toJSONString(false, false));
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
                }
            } else if (type == VIEWTYPE.INPUT) {
                html.append(TaskDefEditorLookup.getInputsHTML(null, "", this.getConnectionId(), this.getTranslationProcessor(), RequestContext.getInstance(request).getPropertyList("userconfig"), this.logger));
                ajaxResponse.addCallbackArgument("html", html.toString());
            } else if (type == VIEWTYPE.VARIABLES) {
                html.append(TaskDefEditorLookup.getVariablesHTML(null, null, this.getConnectionId(), this.getTranslationProcessor(), RequestContext.getInstance(request).getPropertyList("userconfig"), pageContext, this.logger));
                ajaxResponse.addCallbackArgument("html", html.toString());
            } else {
                ajaxResponse.addCallbackArgument("xml", "");
            }
        }
        ajaxResponse.print();
    }

    public static enum VIEWTYPE {
        VARIABLES,
        PROPERTIES,
        INPUT;

    }
}

