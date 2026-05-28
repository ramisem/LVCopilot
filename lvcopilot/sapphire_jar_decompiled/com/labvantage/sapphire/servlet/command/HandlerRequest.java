/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.servlet.RequestProcessor;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.RequestContext;
import sapphire.util.ForwardUtil;
import sapphire.util.HttpUtil;

public class HandlerRequest {
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String requestHandler = requestContext.getProperty("requesthandler");
        String processedText = requestContext.getProperty("processedtext");
        String nextPage = requestContext.getProperty("nextpage");
        if (nextPage.length() == 0) {
            throw new ServletException("Nextpage property not defined for request handler '" + requestHandler + "'");
        }
        if (requestContext.getProperty("page").length() > 0 && nextPage.indexOf("&page=") == -1) {
            nextPage = nextPage + "&page=" + requestContext.getProperty("page");
        }
        if (requestContext.getProperty("file").length() > 0 && nextPage.indexOf("&file=") == -1) {
            nextPage = nextPage + "&file=" + requestContext.getProperty("file");
        }
        HashMap requestProps = HttpUtil.getRequestMap((ServletRequest)request);
        requestProps.remove("command");
        requestProps.remove("requesthandler");
        requestProps.remove("nextpage");
        RequestProcessor requestProcessor = new RequestProcessor(requestContext.getConnectionId());
        try {
            requestProps = requestProcessor.processRequest(requestHandler, requestProps);
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter output = response.getWriter();
            output.println((processedText.length() > 0 ? processedText : "Processed handler") + ". Redirecting...");
            ForwardUtil forward = new ForwardUtil((ServletRequest)request);
            forward.setProperties(requestProps);
            String form = forward.getForm("", nextPage, "post", true);
            output.println(form);
            output.close();
        }
        catch (Exception e) {
            throw new ServletException("Failed to process request handler '" + requestHandler + "'", (Throwable)e);
        }
    }
}

