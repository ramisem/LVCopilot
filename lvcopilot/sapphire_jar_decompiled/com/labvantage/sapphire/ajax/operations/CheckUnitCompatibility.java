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
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class CheckUnitCompatibility
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String fromUnit = ajaxResponse.getRequestParameter("fromunit");
        String toUnit = ajaxResponse.getRequestParameter("tounit");
        String prefix = ajaxResponse.getRequestParameter("prefix");
        String suffix = ajaxResponse.getRequestParameter("suffix");
        if (fromUnit != null && fromUnit.trim().length() > 0 && toUnit != null && toUnit.trim().length() > 0) {
            try {
                String sql = "SELECT expression FROM unitconversion WHERE unitsid=? AND tounits=?";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{fromUnit, toUnit});
                int noOfRows = ds.getRowCount();
                ajaxResponse.addCallbackArgument("exists", noOfRows > 0);
                ajaxResponse.addCallbackArgument("prefix", prefix);
                ajaxResponse.addCallbackArgument("suffix", suffix);
            }
            catch (Exception e) {
                ajaxResponse.setError("sUnitValue property not defined !");
            }
        }
        ajaxResponse.print();
    }
}

