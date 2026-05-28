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
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class FetchPageDirectives
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String propertyId = ajaxResponse.getRequestParameter("propertyId");
        String pageDirectives = (String)request.getSession().getAttribute(propertyId);
        JSONObject jsonObject = null;
        if (pageDirectives != null) {
            try {
                jsonObject = new JSONObject(pageDirectives);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("returnVal", jsonObject);
        ajaxResponse.print();
    }
}

