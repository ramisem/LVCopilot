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

import com.labvantage.sapphire.util.UnitsUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetUnitConvertedValue
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        QueryProcessor qp = this.getQueryProcessor();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String fromUnit = ajaxResponse.getRequestParameter("fromunit", "");
        String toUnit = ajaxResponse.getRequestParameter("tounit", "");
        String value = ajaxResponse.getRequestParameter("value", "");
        String containersize = ajaxResponse.getRequestParameter("containersize", "");
        String conatinerunit = ajaxResponse.getRequestParameter("conatinerunit", "");
        String error = "";
        String convertedValue = "";
        try {
            if (value.length() == 0 || fromUnit.trim().equalsIgnoreCase(toUnit.trim())) {
                convertedValue = value;
            } else if (fromUnit.trim().length() > 0 && toUnit.trim().length() > 0) {
                String sql = "SELECT expression FROM unitconversion WHERE unitsid=? AND tounits=?";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{fromUnit, toUnit});
                int noOfRows = ds.getRowCount();
                if (noOfRows == 0) {
                    error = "Unit conversion expression not defined";
                } else {
                    convertedValue = UnitsUtil.getConvertedValue(this.getQueryProcessor(), fromUnit, toUnit, value);
                }
            } else if (containersize.length() > 0) {
                if (fromUnit.trim().length() == 0) {
                    double convertedValueDouble = UnitsUtil.convertFromContainersToUnits(qp, containersize, conatinerunit, value, toUnit);
                    convertedValue = convertedValueDouble + "";
                } else if (toUnit.trim().length() == 0) {
                    double convertedValueDouble = UnitsUtil.covertFromUnitsToContainer(qp, containersize, conatinerunit, value, fromUnit);
                    convertedValue = convertedValueDouble + "";
                }
            }
        }
        catch (Exception e) {
            error = "Error in unit conversion:" + e.getMessage();
        }
        if (error.length() == 0) {
            ajaxResponse.addCallbackArgument("convertedValue", convertedValue);
        }
        ajaxResponse.print();
    }
}

