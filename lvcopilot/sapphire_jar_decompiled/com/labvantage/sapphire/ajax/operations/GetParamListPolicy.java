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

public class GetParamListPolicy
extends BaseAjaxRequest {
    public static final String POLICY = "ParamListPolicy";
    public static final String DEFAULT_CONFIGPOLICYNODE = "Sapphire Custom";

    @Override
    public void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(httpServletRequest, httpServletResponse);
        try {
            this.doProcessAction(ar);
        }
        catch (SapphireException e) {
            ar.setError("Error while running GetConfigPolicy", e);
        }
        ar.print();
    }

    private void doProcessAction(AjaxResponse ar) throws SapphireException {
        String node = ar.getRequestParameter("policynode", DEFAULT_CONFIGPOLICYNODE);
        PropertyList configPolicy = this.getConfigurationProcessor().getPolicy(POLICY, node);
        String configPolicyJSON = configPolicy.toJSONString(true);
        ar.addCallbackArgument("configpolicy", configPolicyJSON);
    }
}

