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
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetExpressionList
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 69699 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        TranslationProcessor tp = this.getTranslationProcessor();
        String expressiontype = ajaxResponse.getRequestParameter("expressiontype");
        String sql = "";
        sql = expressiontype.equals("sapphireexpression") ? "SELECT expression expr FROM expression WHERE typeflag is null order by 1" : "SELECT namespace, expression expr FROM expression WHERE typeflag is not null order by 2";
        QueryProcessor qp = this.getQueryProcessor();
        DataSet ds = qp.getSqlDataSet(sql, true);
        String html = tp.translate("Expression library:") + "<br>\n";
        html = html + "<select dir=\"ltr\" id=\"expresionlist\" size=\"10\" onclick=\"insertExpression( this.value );\" style=\"width:100%\">\n";
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String namespace = ds.getString(i, "namespace");
            String expression = ds.getString(i, "expr");
            if (expressiontype.equals("groovyexpression") && namespace != null && namespace.length() > 0 && expression != null && expression.length() > 0) {
                String fullname = namespace + "." + expression;
                html = html + "<option value='" + fullname + "'>" + fullname + "</option>";
                continue;
            }
            html = html + "<option value='" + expression + "'>" + expression + "</option>";
        }
        html = html + "</select>";
        ajaxResponse.addCallbackArgument("html", html);
        ajaxResponse.print();
    }
}

