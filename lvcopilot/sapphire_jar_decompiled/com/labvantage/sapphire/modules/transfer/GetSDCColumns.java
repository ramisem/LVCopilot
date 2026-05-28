/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.transfer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetSDCColumns
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54071 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "GetSDCColumnsHandler");
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        try {
            if (sdcid.length() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT syscolumn.columnid FROM syscolumn, sdc WHERE syscolumn.tableid = sdc.tableid AND datatype='C' AND sdcid = " + safeSQL.addVar(sdcid) + " ORDER BY 1";
                DataSet data = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                ajaxResponse.addCallbackArgument("data", data);
                ajaxResponse.addCallbackArgument("rows", data.size());
            } else {
                ajaxResponse.setError("SDCID not specified");
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to process request. Reason: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

