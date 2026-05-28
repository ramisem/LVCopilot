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

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.util.calculations.ExpressionPrefix;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class TestCalcExpression
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 59611 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "evaluateExpressionHandler");
        String expression = this.decodeExpression(ajaxResponse.getRequestParameter("expression"));
        if (expression.length() > 0) {
            try {
                boolean honorNullInRelationalExpressions = false;
                boolean honorNullInANDORConditionals = false;
                try {
                    honorNullInRelationalExpressions = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("honornullinrelationalexpressions", "N").equals("Y");
                }
                catch (SapphireException sapphireException) {
                    // empty catch block
                }
                try {
                    honorNullInANDORConditionals = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom").getProperty("honornullinandorconditionals", "N").equals("Y");
                }
                catch (SapphireException sapphireException) {
                    // empty catch block
                }
                JSONObject params = new JSONObject(ajaxResponse.getRequestParameter("params"));
                HashMap<String, Object> paramMap = new HashMap<String, Object>();
                Iterator it = params.keys();
                while (it.hasNext()) {
                    int i;
                    String dataType;
                    String param = (String)it.next();
                    String paramValue = ((String)params.get(param)).trim();
                    String[] valuePart = StringUtil.split(paramValue, "#DT#");
                    String value = valuePart[0];
                    String string = dataType = valuePart.length == 2 ? valuePart[1] : "N";
                    if ("DI".equals(dataType)) {
                        Object values;
                        DataSet ds;
                        SafeSQL safeSQL = new SafeSQL();
                        if (value.length() > 0) {
                            value = StringUtil.replaceAll(value, ";", "','");
                            ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdidataitem WHERE sdidataitemid in (" + safeSQL.addIn(value) + ")", safeSQL.getValues());
                        } else {
                            ds = this.getQueryProcessor().getSqlDataSet("SELECT * FROM sdidataitem WHERE 1=0");
                        }
                        if (ds.size() == 0) {
                            ds.addRow();
                            String[] columns = ds.getColumns();
                            for (i = 0; i < columns.length; ++i) {
                                ds.setValue(0, columns[i], "");
                            }
                        }
                        if (param.contains("*")) {
                            values = new HashMap[ds.size()];
                            for (i = 0; i < ds.size(); ++i) {
                                values[i] = (HashMap)ds.get(i);
                            }
                            paramMap.put(this.decodeReservedWords(param), values);
                            continue;
                        }
                        values = (HashMap)ds.get(0);
                        paramMap.put(this.decodeReservedWords(param), values);
                        continue;
                    }
                    if ("N".equals(dataType)) {
                        if (value.contains(" ")) {
                            String[] parts = value.trim().split(" ");
                            BigDecimal[] bd = new BigDecimal[parts.length];
                            for (int i2 = 0; i2 < parts.length; ++i2) {
                                try {
                                    bd[i2] = new BigDecimal(parts[i2]);
                                    continue;
                                }
                                catch (Exception ignore) {
                                    bd[i2] = null;
                                }
                            }
                            paramMap.put(this.decodeReservedWords(param), bd);
                            continue;
                        }
                        try {
                            paramMap.put(this.decodeReservedWords(param), new BigDecimal(value));
                            continue;
                        }
                        catch (NumberFormatException e) {
                            if (value.length() == 0) {
                                paramMap.put(this.decodeReservedWords(param), null);
                                continue;
                            }
                            throw new Exception("Invalid Entry");
                        }
                    }
                    if ("D".equals(dataType)) {
                        DateTimeUtil dtu = new DateTimeUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                        if (value.contains(";")) {
                            String[] parts = value.trim().split(" ");
                            Calendar[] values = new Calendar[parts.length];
                            for (i = 0; i < parts.length; ++i) {
                                try {
                                    values[i] = dtu.getCalendar(parts[i]);
                                    continue;
                                }
                                catch (Exception e) {
                                    throw new Exception("Invalid Entry");
                                }
                            }
                            paramMap.put(this.decodeReservedWords(param), values);
                            continue;
                        }
                        try {
                            if (value != null && value.length() > 0) {
                                Calendar cl = dtu.getCalendar(value);
                                paramMap.put(this.decodeReservedWords(param), cl);
                                continue;
                            }
                            paramMap.put(this.decodeReservedWords(param), null);
                            continue;
                        }
                        catch (Exception e) {
                            throw new Exception("Invalid Entry");
                        }
                    }
                    if (value.contains(";")) {
                        String[] values = StringUtil.split(value.trim(), ";");
                        paramMap.put(this.decodeReservedWords(param), values);
                        continue;
                    }
                    paramMap.put(this.decodeReservedWords(param), value);
                }
                String result = ExpressionUtil.evaluateSecure(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), expression, paramMap, honorNullInRelationalExpressions, honorNullInANDORConditionals);
                if (result == null || result.length() == 0) {
                    result = "[Null/Empty String]";
                }
                ajaxResponse.addCallbackArgument("result", "Result = " + result);
            }
            catch (Exception e) {
                String reason = ExpressionPrefix.stripExpressionPrefix(e.getMessage());
                ajaxResponse.setError("Failed to evaluate expression. Reason: " + reason, e);
            }
        } else {
            ajaxResponse.setError("Expression property not defined for service!");
        }
        ajaxResponse.print();
    }

    private String decodeReservedWords(String word) {
        if (word.equals("__this")) {
            return "this";
        }
        return word;
    }

    private String decodeExpression(String expression) {
        String decode = expression.replaceAll("__plus", "+");
        return decode;
    }
}

