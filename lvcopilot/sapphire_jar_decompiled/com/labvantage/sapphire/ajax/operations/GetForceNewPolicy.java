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
import sapphire.accessor.ConfigurationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GetForceNewPolicy
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            String currForceNew = ajaxResponse.getRequestParameter("currForceNew");
            ajaxResponse.addCallbackArgument("forceNew", GetForceNewPolicy.analyzeForceNewPolicy(currForceNew, this.getConfigurationProcessor()));
            ajaxResponse.print();
        }
        catch (SapphireException e) {
            this.logError("GetForceNewPolicy", e);
        }
    }

    public static String analyzeForceNewPolicy(String currentValue, ConfigurationProcessor configurationProcessor) throws SapphireException {
        PropertyList dataEntryPolicyProps = configurationProcessor.getPolicy("DataEntryPolicy", "Sapphire Custom");
        return GetForceNewPolicy.analyzeForceNewPolicy(currentValue, dataEntryPolicyProps);
    }

    public static String analyzeForceNewPolicy(String currentValue, PropertyList dataEntryPolicyProps) throws SapphireException {
        PropertyList forceNewProps = dataEntryPolicyProps.getPropertyList("workitemforcenew");
        String retainFlag = forceNewProps.getProperty("retainselectforcenew");
        String alwaysForceNew = forceNewProps.getProperty("alwaysforcenew");
        if ("Y".equalsIgnoreCase(retainFlag)) {
            return currentValue;
        }
        return alwaysForceNew;
    }
}

