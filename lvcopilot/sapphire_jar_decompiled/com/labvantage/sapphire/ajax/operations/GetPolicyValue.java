/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GetPolicyValue
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String policyid = ajaxResponse.getRequestParameter("policyid", "ConsumablePolicy");
        String nodeid = ajaxResponse.getRequestParameter("nodeid", "Sapphire Custom");
        String propertyid = ajaxResponse.getRequestParameter("propertyid", "");
        String value = "";
        try {
            PropertyList policy = this.getConfigurationProcessor().getPolicy(policyid, nodeid);
            value = policy.getProperty(propertyid, "");
        }
        catch (SapphireException e) {
            ajaxResponse.setError("Unable to fetch Policy:" + policyid);
        }
        ajaxResponse.addCallbackArgument("value", value);
        ajaxResponse.print();
    }
}

