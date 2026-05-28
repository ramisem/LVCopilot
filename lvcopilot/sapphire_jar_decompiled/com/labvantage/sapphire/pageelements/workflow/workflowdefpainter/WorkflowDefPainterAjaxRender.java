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

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefWorkflow;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.Browser;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class WorkflowDefPainterAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            PropertyList workflowprops;
            try {
                workflowprops = new PropertyList(new JSONObject(props));
            }
            catch (Exception e) {
                workflowprops = new PropertyList();
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
            PropertyList toolsdata = TaskDefMaint.getTasksData(true, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
            boolean viewonly = ajaxResponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
            boolean painteronly = ajaxResponse.getRequestParameter("painteronly", "N").equalsIgnoreCase("Y");
            boolean translate = ajaxResponse.getRequestParameter("translate", "N").equalsIgnoreCase("Y");
            boolean renderHtml5 = ajaxResponse.getRequestParameter("renderhtml5", "N").equalsIgnoreCase("Y");
            int zoom = 100;
            try {
                zoom = Integer.parseInt(ajaxResponse.getRequestParameter("zoom", "" + zoom));
            }
            catch (Exception e) {
                zoom = 100;
            }
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
            boolean devMode = false;
            PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
            if (pageContext == null) {
                ajaxResponse.setError("Could not create page context.");
            } else {
                ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
                try {
                    devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
                }
                catch (Exception e) {
                    devMode = false;
                }
            }
            StringBuffer script = new StringBuffer();
            StringBuffer style = new StringBuffer();
            String html = WorkflowDefPainter.getPainterContents(workflowprops, toolsdata, connector, appearance, color, viewonly, painteronly, translate, devMode, zoom, renderHtml5, script, style, this.getSDIProcessor(), new Browser(request), pageContext, this.getConnectionProcessor().getSapphireConnection(), new TranslationProcessor(pageContext), this.logger);
            ajaxResponse.addCallbackArgument("html", html);
            ajaxResponse.addCallbackArgument("script", script);
            ajaxResponse.addCallbackArgument("style", style);
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

