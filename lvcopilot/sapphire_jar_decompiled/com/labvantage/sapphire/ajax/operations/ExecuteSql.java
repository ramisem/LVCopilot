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

import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ExecuteSql
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "ExecuteSqlHandler");
        String sql = ajaxResponse.getRequestParameter("sql");
        try {
            if (sql.length() > 0) {
                if (SecurityPolicyUtil.isUnregisteredSQLPermitted(this.getConnectionid(), "ajax", "ExecuteSql", sql)) {
                    int rows = this.getQueryProcessor().execSQL(sql);
                    ajaxResponse.addCallbackArgument("rows", rows);
                } else {
                    ajaxResponse.setError("Failed to perform ajax request. Reason: ExecuteSql using unregistered SQL disabled in security policy");
                }
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to process sql: " + sql + ". Exception: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

