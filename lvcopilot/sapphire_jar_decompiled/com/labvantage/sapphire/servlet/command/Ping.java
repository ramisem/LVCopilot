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

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.DAMProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.StringUtil;

public class Ping {
    private static final int ACTION_TOUCH = 0;
    private static final int ACTION_CLEAR = 1;

    public void ping(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            PrintWriter output = response.getWriter();
            String rsetlist = request.getParameter("rsetlist");
            String interval = this.doDAMAction(request, servletContext, rsetlist, 0);
            if (interval != null && interval.length() > 0) {
                output.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
                output.println("<meta http-equiv=\"REFRESH\" content=\"" + interval + "; url=\" ></head><body>");
                output.println("</body></html>");
            } else {
                output.println("No rsetid");
            }
            output.close();
        }
        catch (IOException ioe) {
            throw new ServletException((Throwable)ioe);
        }
    }

    public void clearRSets(HttpServletRequest request, ServletContext servletContext, String rsetlist) throws ServletException {
        this.doDAMAction(request, servletContext, rsetlist, 1);
    }

    private String doDAMAction(HttpServletRequest request, ServletContext servletContext, String rsetlist, int action) throws ServletException {
        String interval = "";
        if (rsetlist != null && rsetlist.length() > 0) {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            try {
                DAMProcessor dam = new DAMProcessor(requestContext.getConnectionId());
                String[] rsetid = StringUtil.split(rsetlist, "|");
                block6: for (int i = 0; i < rsetid.length; ++i) {
                    switch (action) {
                        case 0: {
                            dam.touchRSet(rsetid[i]);
                            ConfigurationProcessor cp = new ConfigurationProcessor(requestContext.getConnectionId());
                            interval = cp.getSysConfigProperty("rsettimeout");
                            continue block6;
                        }
                        case 1: {
                            dam.clearRSet(rsetid[i]);
                        }
                    }
                }
            }
            catch (Exception e) {
                throw new ServletException((Throwable)e);
            }
        }
        return interval;
    }
}

