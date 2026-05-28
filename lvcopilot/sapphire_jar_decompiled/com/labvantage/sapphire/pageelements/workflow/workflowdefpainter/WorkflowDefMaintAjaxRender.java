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

import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefMaint;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class WorkflowDefMaintAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block10: {
            ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
            String props = ajaxResponse.getRequestParameter("properties", "");
            if (props.length() > 0) {
                String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
                String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
                String keyid3 = ajaxResponse.getRequestParameter("keyid3", "1");
                try {
                    WorkflowDefMaint.Mode mode = WorkflowDefMaint.Mode.valueOf(ajaxResponse.getRequestParameter("mode", "edit"));
                }
                catch (Exception e) {
                    WorkflowDefMaint.Mode mode = WorkflowDefMaint.Mode.EDIT;
                }
                boolean viewonly = ajaxResponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
                PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
                if (pageContext == null) {
                    ajaxResponse.setError("Could not create page context.");
                }
                try {
                    WorkflowDefMaint.Tabs tab;
                    PropertyList propslist = new PropertyList(new JSONObject(props));
                    try {
                        tab = WorkflowDefMaint.Tabs.valueOf(ajaxResponse.getRequestParameter("tab", WorkflowDefMaint.Tabs.WORKFLOW.toString()).toUpperCase());
                    }
                    catch (Exception e) {
                        tab = WorkflowDefMaint.Tabs.WORKFLOW;
                    }
                    if (tab == WorkflowDefMaint.Tabs.VARIABLES) {
                        ajaxResponse.addCallbackArgument("html", WorkflowDefMaint.getVariablesTab(propslist, viewonly, this.getConnectionProcessor().getSapphireConnection(), pageContext, null, this.getSDIProcessor(), this.getTranslationProcessor(), this.logger));
                        break block10;
                    }
                    ajaxResponse.addCallbackArgument("html", "");
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

