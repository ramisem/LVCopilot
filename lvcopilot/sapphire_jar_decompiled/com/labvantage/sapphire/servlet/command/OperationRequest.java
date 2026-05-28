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

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.servlet.command.BaseRequest;
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
import sapphire.xml.PropertyList;

public class OperationRequest {
    public void processOperation(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String operation = requestContext.getProperty("operation");
        String operationClass = requestContext.getProperty("operationclass");
        if (operation.length() == 0 && operationClass.length() == 0) {
            throw new ServletException("Operation or OperationClass property not defined when processing operation request");
        }
        try {
            HashMap operationProps = HttpUtil.getRequestMap((ServletRequest)request);
            operationProps.remove("command");
            operationProps.remove("operation");
            operationProps.remove("nextpage");
            PropertyList objectProps = new PropertyList();
            if (operationClass.length() == 0) {
                try {
                    WebAdminProcessor wap = new WebAdminProcessor(requestContext.getConnectionId());
                    objectProps.setPropertyTree(wap.getPropertyTreeValues(operation), "", false, wap.getPropertyDefinitionList(operation));
                    operationClass = wap.getPropertyTreeObject(operation);
                }
                catch (Exception e) {
                    throw new ServletException("Error getting property tree object", (Throwable)e);
                }
            }
            Class<?> c = Class.forName(operationClass);
            BaseRequest baseRequest = (BaseRequest)c.newInstance();
            baseRequest.setConnectionId(requestContext.getConnectionId());
            baseRequest.setProperties(objectProps);
            baseRequest.processRequest(request, response, servletContext);
            String nextpage = requestContext.getProperty("nextpage");
            try {
                PrintWriter output = response.getWriter();
                if (output != null) {
                    if (nextpage.length() > 0) {
                        if (nextpage.indexOf("command=page") > -1 && nextpage.indexOf("page=") == -1) {
                            nextpage = nextpage + "&page=" + requestContext.getProperty("page");
                        }
                        response.setContentType("text/html; charset=UTF-8");
                        output.println("Processed operation. Redirecting...");
                        ForwardUtil forward = new ForwardUtil((ServletRequest)request);
                        forward.setProperties(operationProps);
                        forward.setProperties(baseRequest.getProperties());
                        output.println(forward.getForm("", nextpage, "post", true));
                    }
                    output.close();
                }
            }
            catch (Exception exception) {}
        }
        catch (ClassNotFoundException e) {
            throw new ServletException("Class not found for operation '" + operation + "'. Exception: " + e.getMessage());
        }
        catch (InstantiationException e) {
            throw new ServletException("Class could not be instanciated for operation '" + operation + "'. Exception: " + e.getMessage());
        }
        catch (IllegalAccessException e) {
            throw new ServletException("Illegal access for operation '" + operation + "'. Exception: " + e.getMessage());
        }
    }
}

