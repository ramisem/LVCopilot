/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class UtilityRequestHandler
extends PropertyHandler {
    public static final String PROPERTY_OPERATION = "operation";
    public static final String PROPERTY_OPERATION_EVALUATE_EXPRESSION = "evaluateexpression";
    public static final String PROPERTY_EXPRESSION = "expression";
    public static final String PROPERTY_PARAMS = "params";
    public static final String PROPERTY_OPERATION_CONVERTUNITS = "convertunits";
    public static final String PROPERTY_NUMBER = "number";
    public static final String PROPERTY_FROMUNIT = "fromunit";
    public static final String PROPERTY_TOUNIT = "tounit";
    public static final String PROPERTY_RETURNVALUE = "value";

    @Override
    public void processProperties(HashMap map) throws SapphireException {
        DBUtil database = new DBUtil();
        try {
            database.setConnection(this.sapphireConnection);
            String op = (String)map.get(PROPERTY_OPERATION);
            if (op.equals(PROPERTY_OPERATION_EVALUATE_EXPRESSION)) {
                String expression = (String)map.get(PROPERTY_EXPRESSION);
                HashMap params = (HashMap)map.get(PROPERTY_PARAMS);
                String val = this.evaluate(expression, params);
                map.put(PROPERTY_RETURNVALUE, val);
            } else if (op.equals(PROPERTY_OPERATION_CONVERTUNITS)) {
                BigDecimal number = (BigDecimal)map.get(PROPERTY_NUMBER);
                String fromunit = (String)map.get(PROPERTY_FROMUNIT);
                String tounit = (String)map.get(PROPERTY_TOUNIT);
                BigDecimal val = this.convertUnits(database, number, fromunit, tounit);
                map.put(PROPERTY_RETURNVALUE, val);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to generate report: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
        }
        finally {
            database.reset();
        }
    }

    private String evaluate(String expression, HashMap params) throws SapphireException {
        return ExpressionUtil.evaluate(expression, params, false, false);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private BigDecimal convertUnits(DBUtil database, BigDecimal value, String fromUnit, String toUnit) throws SapphireException {
        if (value == null) {
            return null;
        }
        if (fromUnit.equalsIgnoreCase(toUnit)) {
            return value;
        }
        BigDecimal returnValue = null;
        String sql = "select expression from unitconversion where unitsid = ? and tounits= ? ";
        database.createPreparedResultSet(sql, new String[]{fromUnit, toUnit});
        if (!database.getNext()) throw new SapphireException("Unable to find conversion rule from " + fromUnit + " to " + toUnit);
        String expression = database.getString(PROPERTY_EXPRESSION);
        try {
            if (expression == null) return returnValue;
            if (expression.length() <= 0) return returnValue;
            String returnStr = ExpressionUtil.evaluate(expression = StringUtil.replaceAll(expression, "[this]", value.toString()), new HashMap());
            if (returnStr == null) return returnValue;
            return new BigDecimal(returnStr);
        }
        catch (SapphireException e) {
            throw new SapphireException("Failed to convert " + value + " from " + fromUnit + " to " + toUnit + " using expresson:" + expression, e);
        }
    }
}

