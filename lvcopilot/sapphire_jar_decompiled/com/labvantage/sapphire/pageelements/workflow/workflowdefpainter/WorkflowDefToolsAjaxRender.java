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
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefVariables;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class WorkflowDefToolsAjaxRender
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        StringBuffer html = new StringBuffer();
        PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
        if (pageContext == null) {
            ajaxResponse.setError("Could not create page context.");
        }
        boolean viewOnly = ajaxResponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
        boolean changed = false;
        PropertyList propslist = null;
        if (props.length() > 0) {
            try {
                propslist = new PropertyList(new JSONObject(props));
                PropertyList toolprops = TaskDefMaint.getTasksData(true, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                String filter = ajaxResponse.getRequestParameter("filter", "");
                String userConfigPrefix = ajaxResponse.getRequestParameter("userconfigprefix", "workflow_");
                boolean standalone = ajaxResponse.getRequestParameter("standalone", "N").equalsIgnoreCase("Y");
                M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                html.append(WorkflowDefPainter.getToolsHtml(propslist, toolprops, filter, standalone, this.getConnectionId(), this.getTranslationProcessor(), m18n, RequestContext.getInstance(request).getPropertyList("userconfig"), userConfigPrefix, request, this.logger));
                ajaxResponse.addCallbackArgument("html", html.toString());
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
        } else {
            html.append(TaskDefVariables.getVariablePropertiesHTML(null, "", viewOnly, false, this.getConnectionId(), pageContext, this.logger));
        }
        ajaxResponse.print();
    }
}

