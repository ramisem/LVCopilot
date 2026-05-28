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

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SdiInfo;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetReportParams
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            SafeSQL safeSQL = new SafeSQL();
            String reportid = ajaxResponse.getRequestParameter("reportid", "");
            String reportversionid = ajaxResponse.getRequestParameter("reportversionid", "");
            String tablerow = ajaxResponse.getRequestParameter("tablerow", "");
            String paramid = ajaxResponse.getRequestParameter("paramid", "");
            SdiInfo sdiInfo = new SdiInfo();
            sdiInfo.setConnectionId(this.getConnectionId());
            if (OpalUtil.isNotEmpty(reportversionid) && reportversionid.equalsIgnoreCase("C")) {
                reportversionid = sdiInfo.getCurrentVersion("Report", reportid, "");
            }
            StringBuilder query = new StringBuilder();
            query.append(" SELECT * from reportparam").append(" WHERE reportid = ").append(safeSQL.addVar(reportid.trim()));
            if (OpalUtil.isNotEmpty(reportversionid)) {
                query.append(" AND reportversionid = ").append(safeSQL.addVar(reportversionid.trim()));
            }
            query.append(" order by usersequence");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(query.toString(), safeSQL.getValues());
            ajaxResponse.addCallbackArgument("reportParams", ds);
            ajaxResponse.addCallbackArgument("tablerow", tablerow);
            ajaxResponse.addCallbackArgument("paramid", paramid);
            ajaxResponse.print();
        }
        catch (Exception exp) {
            ajaxResponse.setError("Failed to get report params. Exception: " + exp.getMessage(), exp);
        }
    }
}

