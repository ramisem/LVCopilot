/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class SetUserProfileProperty
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.getConnectionId());
            configurationProcessor.setProfileProperty(this.getConnectionProcessor().getSapphireConnection().getSysuserId(), ajaxResponse.getRequestParameter("propertyid"), ajaxResponse.getRequestParameter("propertyvalue"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        ajaxResponse.print();
    }
}

