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

import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ForwardUtil;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class ActionRequest {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void processAction(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        boolean post = request.getMethod().equalsIgnoreCase("POST");
        String actionid = requestContext.getProperty("actionid");
        String versionid = requestContext.getProperty("actionversionid");
        String actionclass = requestContext.getProperty("actionclass");
        if (actionid == null || actionid.length() == 0) {
            actionid = requestContext.getProperty("action");
        }
        if (versionid == null || versionid.length() == 0) {
            versionid = "1";
        }
        String nextpage = requestContext.getProperty("nextpage");
        if (requestContext.getProperty("page").length() > 0 && nextpage.indexOf("&page=") == -1) {
            nextpage = nextpage + "&page=" + requestContext.getProperty("page");
        }
        if (requestContext.getProperty("file").length() > 0 && nextpage.indexOf("&file=") == -1) {
            nextpage = nextpage + "&file=" + requestContext.getProperty("file");
        }
        HashMap actionProps = HttpUtil.getRequestMap((ServletRequest)request);
        actionProps.remove("command");
        actionProps.remove("actionid");
        actionProps.remove("actionversionid");
        actionProps.remove("actionclass");
        actionProps.remove("nextpage");
        if (nextpage.length() <= 0) throw new ServletException("Nextpage property not defined when processing action '" + actionid + "'");
        ActionProcessor actionProcessor = new ActionProcessor(requestContext.getConnectionId());
        try {
            if (actionid != null && actionid.length() > 0) {
                if (!SecurityPolicyUtil.isActionPermitted(requestContext.getConnectionId(), "actioncommand", post ? "postactionprocessing" : "getactionprocessing", actionid, actionProps)) throw new ActionException("Failed to process action '" + actionid + "'. Reason: Action execution not permitted by security policy.");
                actionProcessor.processAction(actionid, versionid, actionProps);
            } else if (actionclass != null && actionclass.length() > 0) {
                if (!SecurityPolicyUtil.isActionClassPermitted(requestContext.getConnectionId(), "actioncommand", actionclass)) throw new ActionException("Failed to process action class '" + actionclass + "'. Reason: Action class execution not permitted by security policy.");
                PropertyList props = new PropertyList();
                props.putAll(actionProps);
                actionProcessor.processActionClass(actionclass, props);
            }
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter output = response.getWriter();
            output.println("Processed action. Redirecting...");
            ForwardUtil forward = new ForwardUtil((ServletRequest)request);
            forward.setProperties(actionProps);
            String form = forward.getForm("", nextpage, "post", true);
            output.println(form);
            output.close();
            return;
        }
        catch (IOException ioe) {
            throw new ServletException(ioe.getMessage());
        }
        catch (ActionException ae) {
            throw new ServletException("Failed to process action. Exception: " + ae.getMessage());
        }
    }
}

