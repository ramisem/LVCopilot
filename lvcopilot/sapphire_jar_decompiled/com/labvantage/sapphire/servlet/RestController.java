/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.RestConstants;
import com.labvantage.sapphire.servlet.rest.RestException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.BaseHttpServlet;
import sapphire.util.HttpUtil;

public class RestController
extends BaseHttpServlet
implements RestConstants {
    public void init() throws ServletException {
        super.init();
        this.getServletContext().log("REST: Servlet startup");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            BaseNameSpaceHandler handler = BaseNameSpaceHandler.getInstance(request, response);
            Trace.startThreadMDCByDatabaseid(handler.getDatabaseid(), "REST");
            handler.process();
            handler.respond();
        }
        catch (RestException e) {
            this.sendErrorResponse(request, response, e.getHttpCode(), e);
        }
        catch (Exception e) {
            this.sendErrorResponse(request, response, 500, e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, int httpCode, Throwable e) throws IOException, ServletException {
        ConnectionProcessor cp;
        SapphireConnection sapphireConnection;
        HttpUtil httpUtil = new HttpUtil(request, response);
        String connectionid = httpUtil.getCookieValue("connectionid");
        if (connectionid != null && connectionid.length() > 0 && (sapphireConnection = (cp = new ConnectionProcessor(connectionid)).getSapphireConnection()) != null && sapphireConnection.getTool().equalsIgnoreCase("RequestController")) {
            throw new ServletException(e);
        }
        String errormsg = "Request URI " + request.getRequestURI() + " failed. Reason: " + e.getMessage();
        if (e instanceof RestException && ((RestException)e).getError().equals("Resource not found")) {
            Trace.logError("REST", errormsg);
        } else {
            Trace.logError("REST", (Object)errormsg, e);
        }
        response.setStatus(httpCode);
        if (!response.isCommitted()) {
            PrintWriter out = response.getWriter();
            out.print("{\n \"request\": \"" + request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "") + "\", \n \"method\": \"" + request.getMethod() + "\", \n \"code\": " + httpCode + ", \n \"status\": \"" + BaseNameSpaceHandler.getStatusFromCode(httpCode) + "\", \n \"error\": \"" + (e instanceof RestException ? ((RestException)e).getError() : "Unexpected error") + "\", \n \"message\": \"" + e.getMessage() + "\"\n}");
            out.close();
        }
    }
}

