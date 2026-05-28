/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.security;

import com.labvantage.sapphire.servlet.command.BaseRequest;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.StringUtil;

public class UserMaint
extends BaseRequest {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String action = requestContext.getProperty("action");
        if (!action.equalsIgnoreCase("forcechangepassword")) throw new ServletException("Unrecognized action in UserMaint");
        String[] sysuserid = StringUtil.split(requestContext.getProperty("keyid1"), ";");
        if (sysuserid.length <= 0) throw new ServletException("keyid1 (sysuserid) not specified in UserMaint");
        ConnectionProcessor cp = this.getConnectionProcessor();
        for (int i = 0; i < sysuserid.length; ++i) {
            cp.forceChangePassword(sysuserid[i]);
        }
    }
}

