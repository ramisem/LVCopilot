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
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentRequest;
import com.labvantage.sapphire.servlet.RequestProcessor;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class DocumentAjaxHandler
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestProcessor requestProcessor = new RequestProcessor(this.getRequestContext().getConnectionid());
        String callProperties = request.getParameter("callproperties");
        if (callProperties != null && callProperties.length() > 0) {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response, request.getParameter("callback"));
            try {
                HashMap<String, String> requestData = new HashMap<String, String>();
                requestData.put("requestcommand", "print");
                requestData.put("print", request.getParameter("callproperties"));
                HashMap returnProps = requestProcessor.processRequest(DocumentRequest.class.getName(), requestData);
                ajaxResponse.addCallbackArgument("html", returnProps.get("html"));
                ajaxResponse.print();
            }
            catch (Exception e) {
                ajaxResponse.setError("Error processing Sapphire AJAX request: " + e.getMessage());
            }
        } else {
            try {
                String jsonReturn;
                HashMap<String, String> returnProps = null;
                HashMap requestMap = HttpUtil.getRequestMap((ServletRequest)request);
                String requestCommand = (String)requestMap.get("requestcommand");
                if ("rsetping".equalsIgnoreCase(requestCommand)) {
                    PropertyList requestData = new PropertyList(new JSONObject((String)requestMap.get(requestCommand)));
                    String rsetid = requestData.getProperty("rsetid");
                    DAMProcessor dam = new DAMProcessor(this.getConnectionId());
                    dam.touchRSet(rsetid);
                    returnProps = new HashMap<String, String>();
                    returnProps.put("jsonreturn", "rsetid");
                } else {
                    returnProps = requestProcessor.processRequest(DocumentRequest.class.getName(), requestMap);
                }
                if (returnProps.get("nouserconfig") == null || ((String)returnProps.get("nouserconfig")).equals("N")) {
                    String sysuserid = (String)returnProps.get("sysuserid");
                    for (String key : returnProps.keySet()) {
                        if (!key.startsWith("userconfig_")) continue;
                        this.setUserConfigProperty(request, sysuserid, key.substring(11), (String)returnProps.get(key));
                    }
                }
                this.write((jsonReturn = (String)returnProps.get("jsonreturn")) != null && jsonReturn.length() > 0 ? jsonReturn : "");
            }
            catch (Exception e) {
                PropertyList documentObjects = new PropertyList();
                documentObjects.setProperty("status", "E");
                documentObjects.setProperty("returnmessage", "Failed to process document request. Exception: " + e.getMessage());
                this.write(documentObjects.toJSONString(false));
                this.logger.error("Error processing request: " + e.getMessage(), e);
            }
        }
    }

    private void setUserConfigProperty(HttpServletRequest request, String sysuserid, String propertyid, String propertyvalue) {
        try {
            PropertyList userConfig = (PropertyList)request.getSession().getAttribute("userconfig");
            userConfig.setProperty(propertyid, propertyvalue);
            ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getRequestContext().getConnectionid());
            configProcessor.setProfileProperty(sysuserid, "userconfig_" + propertyid, propertyvalue);
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
    }
}

