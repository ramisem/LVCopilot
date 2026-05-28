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

import com.labvantage.sapphire.pageelements.gwt.server.GroovyProcessingPropertyHandler;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.servlet.RequestContext;
import sapphire.util.ActionBlock;

public class ActionBlockRequest
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            String actionblockxml = request.getParameter("actionblockxml");
            String actionid = request.getParameter("actionid");
            String testinputs = request.getParameter("testinputs");
            String mode = request.getParameter("mode");
            if ("execute".equals(mode)) {
                if (actionblockxml == null && actionid != null) {
                    actionblockxml = "";
                }
                ActionBlock ab = new ActionBlock(actionblockxml);
                if (testinputs != null && testinputs.length() > 0) {
                    JSONObject jsonObj = new JSONObject(testinputs);
                    Iterator itr = jsonObj.keys();
                    HashMap<String, Object> props = new HashMap<String, Object>();
                    while (itr.hasNext()) {
                        String key = (String)itr.next();
                        props.put(key, jsonObj.get(key));
                    }
                    props.put("fields", props);
                    ab.setGroovyBindings(props);
                    ab.setBlockProperties(props);
                }
                ActionProcessor ap = new ActionProcessor(requestContext.getConnectionId());
                ab.setDebugMode(true);
                try {
                    ap.processActionBlock(ab);
                    response.getWriter().write(ab.getDebugLog());
                }
                catch (ActionException ae) {
                    response.getWriter().write(ae.getErrorHandler().getEncodedString());
                }
            } else if ("checkgroovysyntax".equals(mode)) {
                String script = request.getParameter("groovyscript");
                try {
                    int i;
                    if (script.indexOf("$G{") == 0 && (i = (script = script.substring(3)).lastIndexOf("}")) > -1) {
                        script = script.substring(0, i);
                    }
                    GroovyUtil.parseScript(script);
                    response.getWriter().write("Check Syntax is Correct");
                }
                catch (SapphireException e) {
                    response.getWriter().write(e.getMessage());
                }
            } else if ("executegroovy".equals(mode)) {
                String script = request.getParameter("groovyscript");
                StringBuffer logBuffer = new StringBuffer();
                try {
                    HashMap<String, Object> props = new HashMap<String, Object>();
                    props.put("log", logBuffer);
                    props.put("script", script);
                    HashMap<String, Object> fields = new HashMap<String, Object>();
                    if (testinputs != null && testinputs.length() > 0) {
                        JSONObject jsonObj = new JSONObject(testinputs);
                        Iterator itr = jsonObj.keys();
                        while (itr.hasNext()) {
                            String key = (String)itr.next();
                            fields.put(key, jsonObj.get(key));
                        }
                    }
                    props.put("fields", fields);
                    new RequestProcessor(requestContext.getConnectionId()).processRequest(GroovyProcessingPropertyHandler.class.getName(), props);
                    response.getWriter().write(logBuffer.toString());
                }
                catch (SapphireException e) {
                    String errorMessage = logBuffer.toString();
                    if (errorMessage.indexOf("Expression Error: No such property: groups") >= 0) {
                        errorMessage = "Your groovy contains groups variable and can only be tested by submitting the actual form.";
                    }
                    response.getWriter().write(e.getMessage() + "\n" + errorMessage);
                }
            }
        }
        catch (Exception e) {
            try {
                response.getWriter().write(e.getMessage());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

