/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

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

public class GetDataSet
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54553 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        HashMap params = (HashMap)ajaxResponse.getRequestParameters();
        String sql = (String)params.get("sql");
        try {
            int sqlcode = Integer.parseInt(sql);
            params.put("sqlcode", String.valueOf(sqlcode));
            sql = null;
        }
        catch (Exception sqlcode) {
            // empty catch block
        }
        if (sql != null && sql.length() > 0) {
            if (SecurityPolicyUtil.isUnregisteredSQLPermitted(this.getConnectionid(), "ajax", "getDataSet", sql)) {
                ajaxResponse.addCallbackArgument("ds", this.getQueryProcessor().getSqlDataSet(sql));
            } else {
                ajaxResponse.setError("Failed to perform ajax request. Reason: GetDataSet using unregistered SQL disabled in security policy");
            }
        } else {
            try {
                DataSet ds = ajaxResponse.getRegisteredSQLDataSet(this.getQueryProcessor(), params);
                ajaxResponse.addCallbackArgument("ds", ds);
            }
            catch (Exception e) {
                ajaxResponse.setError(e.getMessage());
                this.logError(e.getMessage(), e);
            }
        }
        Map map = ajaxResponse.getRequestParameters();
        for (String key : map.keySet()) {
            if ("sql".equals(key) || "timestamp".equals(key) || "sqlcode".equals(key) || "bindvars".equals(key)) continue;
            ajaxResponse.addCallbackArgument(key, ajaxResponse.getRequestParameter(key, ""));
        }
        ajaxResponse.print();
    }
}

