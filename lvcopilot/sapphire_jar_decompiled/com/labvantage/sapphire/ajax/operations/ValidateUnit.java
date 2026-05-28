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

public class ValidateUnit
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 60204 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        String unitValue = ajaxResponse.getRequestParameter("unit");
        boolean nocategorycheck = ajaxResponse.getRequestParameter("nocategorycheck", "N").equalsIgnoreCase("Y");
        SafeSQL safeSQL = new SafeSQL();
        if (unitValue.length() > 0) {
            StringBuffer sql = new StringBuffer("select distinct keyid1 from categoryitem where sdcid = 'Units'");
            sql.append(nocategorycheck ? "" : " AND categoryid in ('MassUnits','VolumeUnits')");
            sql.append(" AND keyid1=" + safeSQL.addVar(unitValue));
            DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            ajaxResponse.addCallbackArgument("exists", ds.getRowCount() > 0);
        } else {
            ajaxResponse.setError("UnitValue property not defined !");
        }
        ajaxResponse.print();
    }
}

