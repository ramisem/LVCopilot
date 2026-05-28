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
import org.json.JSONArray;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class GetExpressionTokens
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "getTokensHandler");
        String expression = ajaxResponse.getRequestParameter("expression");
        if (expression.length() > 0) {
            String[] tokens = StringUtil.getExpressionTokens(expression);
            StringBuffer buffer = new StringBuffer();
            JSONArray ja = new JSONArray();
            for (int i = 0; i < tokens.length; ++i) {
                buffer.append(";").append(tokens[i]);
                ja.put(tokens[i]);
            }
            ajaxResponse.addCallbackArgument("tokens", ja);
        } else {
            ajaxResponse.setError("Expression property not defined for service!");
        }
        ajaxResponse.print();
    }
}

