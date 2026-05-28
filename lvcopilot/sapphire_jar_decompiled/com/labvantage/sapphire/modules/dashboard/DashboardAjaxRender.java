/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.modules.dashboard.Dashboard;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class DashboardAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "DashboardHandler");
        JSONObject job = ajaxResponse.getCallProperties();
        if (job != null) {
            try {
                Dashboard dashboard = new Dashboard(job.getJSONObject("properties"), false);
                dashboard.setRakFile(this.getRakFile());
                dashboard.setConnectionId(this.getConnectionId());
                dashboard.setRequest(request);
                dashboard.setPageContext(ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                dashboard.setParameters(job.getJSONObject("parameters"));
                ajaxResponse.addCallbackArgument("html", dashboard.getHtml());
                ajaxResponse.addCallbackArgument("script", dashboard.getScript());
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not create Dashboard."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

