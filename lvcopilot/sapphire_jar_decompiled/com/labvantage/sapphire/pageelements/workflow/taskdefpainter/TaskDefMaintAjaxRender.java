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

import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.Browser;
import sapphire.xml.PropertyList;

public class TaskDefMaintAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 58856 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block12: {
            TaskDefMaint.Tabs tab;
            TaskDefMaint.Mode mode;
            ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
            String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
            String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
            String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
            boolean viewonly = ajaxResponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
            boolean descendant = ajaxResponse.getRequestParameter("descendant", "N").equalsIgnoreCase("Y");
            try {
                mode = TaskDefMaint.Mode.valueOf(ajaxResponse.getRequestParameter("mode", "edit").toUpperCase());
            }
            catch (Exception e) {
                mode = TaskDefMaint.Mode.EDIT;
            }
            PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
            if (pageContext == null) {
                ajaxResponse.setError("Could not create page context.");
            }
            try {
                tab = TaskDefMaint.Tabs.valueOf(ajaxResponse.getRequestParameter("tab", "task").toUpperCase());
            }
            catch (Exception e) {
                tab = TaskDefMaint.Tabs.TASK;
            }
            if (tab != TaskDefMaint.Tabs.ATTACHMENTS) {
                String props = ajaxResponse.getRequestParameter("properties", "");
                if (props.length() > 0) {
                    try {
                        PropertyList propslist = new PropertyList(new JSONObject(props));
                        if (tab == TaskDefMaint.Tabs.WORK) {
                            ajaxResponse.addCallbackArgument("html", TaskDefMaint.getWorkTab(propslist, viewonly, this.getConnectionId(), pageContext, this.logger));
                            break block12;
                        }
                        if (tab == TaskDefMaint.Tabs.INCLUDES) {
                            ajaxResponse.addCallbackArgument("html", TaskDefMaint.getIncludesTab(propslist, viewonly || descendant, this.getConnectionId(), this.getTranslationProcessor(), new Browser(pageContext), pageContext));
                            break block12;
                        }
                        ajaxResponse.addCallbackArgument("html", TaskDefMaint.getTaskTab(keyid1, keyid2, keyid3, propslist, viewonly, mode, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), pageContext, this.logger));
                    }
                    catch (Exception e) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
                    }
                } else {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
                }
            }
        }
        ajaxResponse.print();
    }
}

