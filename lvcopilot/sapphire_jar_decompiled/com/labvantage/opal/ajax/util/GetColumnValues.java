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

import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetColumnValues
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53243 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sql = ajaxResponse.getRequestParameter("sql");
        String sqlcode = ajaxResponse.getRequestParameter("sqlcode");
        String columnid = ajaxResponse.getRequestParameter("columnid");
        DataSet ds = null;
        if (sql.length() > 0) {
            if (SecurityPolicyUtil.isUnregisteredSQLPermitted(this.getConnectionid(), "ajax", "getColumnValues", sql)) {
                ds = this.getQueryProcessor().getSqlDataSet(sql);
            } else {
                ajaxResponse.setError("Failed to perform ajax request. Reason: GetColumnValues using unregistered SQL disabled in security policy");
            }
        } else if (sqlcode.length() > 0) {
            try {
                ds = ajaxResponse.getRegisteredSQLDataSet(this.getQueryProcessor(), (HashMap)ajaxResponse.getRequestParameters());
            }
            catch (Exception e) {
                ajaxResponse.setError(e.getMessage());
                this.logError(e.getMessage(), e);
            }
        } else {
            ajaxResponse.addCallbackArgument("value", "");
        }
        if (ds != null) {
            if (columnid.length() > 0 && ds.isValidColumn(columnid)) {
                ajaxResponse.addCallbackArgument("value", ds.getColumnValues(columnid, ";"));
            } else {
                ajaxResponse.addCallbackArgument("value", ds.getColumnValues(ds.getColumnId(0), ";"));
            }
        }
        Map map = ajaxResponse.getRequestParameters();
        for (String key : map.keySet()) {
            if ("sql".equals(key) || "columnid".equals(key)) continue;
            ajaxResponse.addCallbackArgument(key, ajaxResponse.getRequestParameter(key, ""));
        }
        ajaxResponse.print();
    }
}

