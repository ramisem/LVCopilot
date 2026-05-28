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
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.modules.dashboard.GizmoTargetList;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GizmoTargetListAjaxRender
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "GizmoTargetLookupHandler");
        boolean clearCache = ajaxResponse.getRequestParameter("clearcache", "N").equalsIgnoreCase("Y");
        String categoryId = ajaxResponse.getRequestParameter("category", "");
        String searchText = ajaxResponse.getRequestParameter("search", "");
        String type = ajaxResponse.getRequestParameter("type", "");
        PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
        if (pageContext == null) {
            ajaxResponse.setError("Could not create page context.");
        }
        int start = 0;
        int max = 200;
        try {
            start = Integer.parseInt(ajaxResponse.getRequestParameter("start", "" + start));
            max = Integer.parseInt(ajaxResponse.getRequestParameter("max", "" + max));
        }
        catch (NumberFormatException e) {
            start = 0;
            max = 200;
        }
        GizmoTargetList gizmoTargetList = new GizmoTargetList();
        gizmoTargetList.setPageContext(pageContext);
        gizmoTargetList.setGizmoType(type);
        gizmoTargetList.setCategory(categoryId);
        gizmoTargetList.setSearchString(searchText);
        if (clearCache) {
            gizmoTargetList.clearCache();
        }
        gizmoTargetList.getGizmoTargets();
        String html = gizmoTargetList.getHtml();
        ajaxResponse.addCallbackArgument("html", html);
        ajaxResponse.print();
    }
}

