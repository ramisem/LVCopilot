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
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ConvertUnit
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String fromunit = ajaxResponse.getRequestParameter("fromunit");
        String tounit = ajaxResponse.getRequestParameter("tounit");
        String value = ajaxResponse.getRequestParameter("value");
        if (this.isNumeric(value)) {
            if (!fromunit.equals(tounit) && OpalUtil.isEmpty(value = this.convertUnits(fromunit, tounit, value))) {
                message = this.getTranslationProcessor().translate("Invalid unit conversion: ") + fromunit + " to " + tounit;
            }
        } else {
            message = this.getTranslationProcessor().translate("Not a valid value for unit conversion") + ": " + value;
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("value", ajaxResponse.getRequestParameter("value"));
        ajaxResponse.addCallbackArgument("convertedvalue", value);
        ajaxResponse.addCallbackArgument("row", ajaxResponse.getRequestParameter("row"));
        ajaxResponse.print();
    }

    public String convertUnits(String fromUnit, String toUnit, String value) {
        String expression;
        String returnValue = "";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select expression from unitconversion where unitsid = ? and tounits = ?", (Object[])new String[]{fromUnit, toUnit});
        if (ds != null && ds.size() > 0 && (expression = ds.getString(0, "expression")) != null && expression.length() > 0) {
            expression = StringUtil.replaceAll(expression, "[this]", value);
            try {
                returnValue = ExpressionUtil.evaluate(expression, new HashMap());
            }
            catch (SapphireException e) {
                returnValue = "";
            }
        }
        return returnValue;
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}

