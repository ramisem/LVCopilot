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

import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ConvertUnits
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53252 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String fromunit = request.getParameter("fromunit");
        String tounit = request.getParameter("tounit");
        String value = request.getParameter("value");
        if (ConvertUnits.isNumeric(value) && !fromunit.equals(tounit)) {
            try {
                value = ConvertUnits.convertUnits(this.getQueryProcessor(), fromunit, tounit, value);
            }
            catch (SapphireException e) {
                value = "";
                this.logger.error(e.getMessage(), e);
            }
        }
        try {
            response.getWriter().write(value);
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    public static String convertUnits(QueryProcessor qp, String fromUnit, String toUnit, String value) throws SapphireException {
        String expression;
        String returnValue = null;
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = qp.getPreparedSqlDataSet(new StringBuffer().append("select expression from unitconversion where unitsid=").append(safeSQL.addVar(fromUnit)).append(" and tounits=").append(safeSQL.addVar(toUnit)).toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0 && (expression = ds.getString(0, "expression")) != null && expression.length() > 0) {
            expression = StringUtil.replaceAll(expression, "[this]", value);
            returnValue = ExpressionUtil.evaluate(expression, new HashMap());
        }
        if (returnValue == null) {
            throw new SapphireException("Unable to find conversion from from from " + fromUnit + " to " + toUnit);
        }
        return returnValue;
    }

    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}

