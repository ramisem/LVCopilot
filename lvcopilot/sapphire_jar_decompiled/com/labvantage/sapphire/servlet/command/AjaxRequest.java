/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.admin.system.CheckConnectionProcessor;
import com.labvantage.sapphire.ajax.operations.AddSDI;
import com.labvantage.sapphire.ajax.operations.AddToDoListEntry;
import com.labvantage.sapphire.ajax.operations.ProcessAction;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.BaseHttpServlet;
import sapphire.servlet.RequestContext;
import sapphire.util.SafeHTML;

public class AjaxRequest {
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        this.processAjaxRequest(request, response, servletContext, null);
    }

    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, BaseHttpServlet servlet) throws ServletException {
        String ajaxservice;
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String ajaxclass = request.getParameter("ajaxclass");
        if ((ajaxclass == null || ajaxclass.length() == 0) && (ajaxservice = request.getParameter("ajaxservice")) != null && ajaxservice.length() > 0) {
            if (ajaxservice.equalsIgnoreCase("AddSDI")) {
                ajaxclass = AddSDI.class.getName();
            } else if (ajaxservice.equalsIgnoreCase("AddToDoListEntry")) {
                ajaxclass = AddToDoListEntry.class.getName();
            } else if (ajaxservice.equalsIgnoreCase("ProcessAction")) {
                ajaxclass = ProcessAction.class.getName();
            }
        }
        BaseAjaxRequest baseAjaxRequest = null;
        try {
            if (ajaxclass == null || ajaxclass.length() == 0) {
                throw new ServletException("No ajax class could be found.");
            }
            String connectionid = requestContext.getConnectionId();
            if (!new CheckConnectionProcessor().isConnectionValid(connectionid)) {
                throw new ServletException("Connectionid (" + SafeHTML.encodeForHTML(connectionid) + ") is missing or invalid.");
            }
            Class<?> c = Class.forName(ajaxclass);
            baseAjaxRequest = (BaseAjaxRequest)c.newInstance();
            if (!baseAjaxRequest.acceptContentType(request.getContentType())) {
                throw new ServletException("Invalid content type for Ajax request. Can only accept Form encoded request or JSON body.");
            }
            response.setHeader("Cache-Control", "no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Content-Type", "application/json");
            baseAjaxRequest.setRequestContext(requestContext);
            if (servlet != null) {
                baseAjaxRequest.setServlet(servlet);
            }
            baseAjaxRequest.setConnectionId(requestContext.getConnectionId());
            baseAjaxRequest.open(response.getWriter());
            baseAjaxRequest.processRequest(request, response, servletContext);
        }
        catch (Exception e) {
            if (!response.isCommitted()) {
                response.setHeader("Content-Type", "application/json");
                AjaxResponse.handleException(request, response, e);
            }
            throw new ServletException("Failed to process Ajax class '" + ajaxclass + "'", (Throwable)e);
        }
        finally {
            if (baseAjaxRequest != null && !response.isCommitted()) {
                baseAjaxRequest.close();
            }
        }
    }
}

