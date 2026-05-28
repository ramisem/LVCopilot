/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ThreadUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;

public class ThreadMonitorAjaxHandler
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103941 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxresponse = new AjaxResponse(request, response);
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo info = cp.getConnectionInfo(cp.getConnectionid());
        try {
            String dothis = ajaxresponse.getRequestParameter("dothis");
            if (dothis.equals("getstacktrace")) {
                this.getThreadStackTrace(ajaxresponse);
            }
        }
        catch (Exception e) {
            Trace.logError("Unable to execute LogViewer command", e);
            ajaxresponse.setError(e.getMessage());
        }
        ajaxresponse.print();
    }

    private void getThreadStackTrace(AjaxResponse ajaxresponse) {
        String threadid = ajaxresponse.getRequestParameter("threadid");
        String html = ThreadUtil.getThreadStackTrace(Long.parseLong(threadid));
        ajaxresponse.addCallbackArgument("html", html);
    }
}

