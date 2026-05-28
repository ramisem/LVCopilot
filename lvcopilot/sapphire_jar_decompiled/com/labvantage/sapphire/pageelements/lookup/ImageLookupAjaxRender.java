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
package com.labvantage.sapphire.pageelements.lookup;

import com.labvantage.sapphire.pageelements.lookup.ImageLookup;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ImageLookupAjaxRender
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "ImageLookupHandler");
        String categoryId = ajaxResponse.getRequestParameter("category", "");
        String searchText = ajaxResponse.getRequestParameter("search", "");
        String resolution = ajaxResponse.getRequestParameter("resolution", "");
        String media = ajaxResponse.getRequestParameter("media", "");
        boolean returnSVG = ajaxResponse.getRequestParameter("returnsvg", "N").equalsIgnoreCase("Y");
        boolean returnSVGContent = ajaxResponse.getRequestParameter("returnsvgcontent", "N").equalsIgnoreCase("Y");
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
        ImageLookup imageLookup = new ImageLookup();
        imageLookup.setReturnSVG(returnSVG);
        imageLookup.setReturnSVGContent(returnSVGContent);
        String html = imageLookup.getIcons(start, max, categoryId, searchText, media, resolution, this.getConnectionProcessor().getSapphireConnection());
        ajaxResponse.addCallbackArgument("html", html);
        ajaxResponse.addCallbackArgument("summary", imageLookup.getSummary());
        ajaxResponse.addCallbackArgument("count", start + imageLookup.getCount());
        ajaxResponse.addCallbackArgument("total", imageLookup.getTotal());
        ajaxResponse.print();
    }
}

