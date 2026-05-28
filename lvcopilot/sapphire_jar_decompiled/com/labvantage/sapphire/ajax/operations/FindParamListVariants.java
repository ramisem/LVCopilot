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
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class FindParamListVariants
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 59250 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String paramlistid = ajaxResponse.getRequestParameter("paramlistid");
        String versionid = ajaxResponse.getRequestParameter("versionid");
        SafeSQL safeSQL = new SafeSQL();
        QueryProcessor qp = this.getQueryProcessor();
        String sql = "SELECT DISTINCT variantid FROM paramlist WHERE paramlistid=" + safeSQL.addVar(paramlistid);
        if (versionid.length() > 0 && !versionid.equals("#") && !versionid.equals("*")) {
            sql = sql + " AND paramlistversionid=" + safeSQL.addVar(versionid);
        }
        sql = sql + " AND versionstatus != 'E'";
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        String list = ds.getColumnValues("variantid", ";");
        ajaxResponse.addCallbackArgument("list", list);
        ajaxResponse.print();
    }
}

