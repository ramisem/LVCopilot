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

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.system.SQLRegister;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetSQLDataSet
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53244 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sql = ajaxResponse.getRequestParameter("sql");
        String sqlcode = ajaxResponse.getRequestParameter("sqlcode");
        DataSet ds = null;
        if (sql.length() > 0) {
            if (SecurityPolicyUtil.isUnregisteredSQLPermitted(this.getConnectionid(), "ajax", "getSQLDataSet", sql)) {
                ds = this.getQueryProcessor().getSqlDataSet(sql);
            } else {
                ajaxResponse.setError("Failed to perform ajax request. Reason: GetSQLDataSet using unregistered SQL disabled in security policy");
            }
        } else if (sqlcode.length() > 0) {
            try {
                QueryProcessor queryProcessor = this.getQueryProcessor();
                ds = ajaxResponse.getRegisteredSQLDataSet(queryProcessor, (HashMap)ajaxResponse.getRequestParameters());
                sql = SQLRegister.getSQL(SecurityService.getDatabaseId(queryProcessor.getConnectionid()), Integer.parseInt(sqlcode));
            }
            catch (Exception e) {
                ajaxResponse.setError(e.getMessage());
                this.logError(e.getMessage(), e);
            }
        } else {
            ds = new DataSet();
        }
        if (ds != null && ds.getColumnCount() > 0) {
            ajaxResponse.addCallbackArgument("ds", ds);
            String columns = GetSQLDataSet.getSelectColumns(sql);
            if ("*".equals(columns)) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < ds.getColumnCount(); ++i) {
                    list.add(ds.getColumnId(i));
                }
                columns = OpalUtil.toDelimitedString(list, ";");
            }
            ajaxResponse.addCallbackArgument("columns", columns);
        }
        Map map = ajaxResponse.getRequestParameters();
        for (Object o : map.keySet()) {
            String key = (String)o;
            if ("sql".equals(key) || "timestamp".equals(key) || "sqlcode".equals(key) || "bindvars".equals(key)) continue;
            ajaxResponse.addCallbackArgument(key, ajaxResponse.getRequestParameter(key, ""));
        }
        ajaxResponse.print();
    }

    public static String getSelectColumns(String sql) {
        String columns;
        try {
            String lastcolumn = "";
            String psql = sql.toLowerCase();
            psql = psql.substring(psql.indexOf("select") + 6);
            psql = StringUtil.replaceAll(psql, ",", " , ");
            psql = StringUtil.replaceAll(psql, "(", " ( ");
            psql = StringUtil.replaceAll(psql, ")", " ) ");
            StringTokenizer token = new StringTokenizer(psql);
            ArrayList<String> list = new ArrayList<String>();
            int i = 0;
            while (token.hasMoreTokens()) {
                String s = token.nextToken();
                if (s == null || s.length() <= 0) continue;
                if (s.startsWith("(")) {
                    ++i;
                    continue;
                }
                if (s.endsWith(")")) {
                    --i;
                    continue;
                }
                if (i != 0) continue;
                if ("from".equals(s)) {
                    if (lastcolumn.length() <= 0) break;
                    list.add(lastcolumn);
                    break;
                }
                if (s.indexOf(",") != -1) {
                    list.add(lastcolumn);
                    lastcolumn = "";
                    continue;
                }
                int index = s.indexOf(".");
                if (index != -1) {
                    s = s.substring(index + 1);
                }
                lastcolumn = StringUtil.replaceAll(s, "\"", "");
            }
            columns = OpalUtil.toDelimitedString(list, ";");
        }
        catch (Exception e) {
            columns = "";
        }
        return columns;
    }
}

