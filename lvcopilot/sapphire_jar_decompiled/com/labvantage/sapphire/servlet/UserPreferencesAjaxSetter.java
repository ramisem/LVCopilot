/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class UserPreferencesAjaxSetter
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "");
        String propertyid = ajaxResponse.getRequestParameter("userpropertyid");
        String value = ajaxResponse.getRequestParameter("userpropertyvalue");
        PropertyList userPreferences = (PropertyList)request.getSession().getAttribute("userconfig");
        if (userPreferences == null) {
            userPreferences = new PropertyList();
            request.getSession().setAttribute("userconfig", (Object)userPreferences);
        }
        userPreferences.setProperty(propertyid.substring(11), value);
        ajaxResponse.print();
    }
}

