/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.xml.PropertyList;

public class SDCProcessorRequest
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            String sdcid = request.getParameter("sdcid");
            PropertyList pl = new SDCProcessor(requestContext.getConnectionId()).getPropertyList(sdcid);
            response.getWriter().write(pl.toJSONString());
        }
        catch (Exception e) {
            throw new ServletException((Throwable)e);
        }
    }
}

