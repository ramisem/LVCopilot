/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdidata.DataEntryLimitsUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.evaluator.ExpressionEvaluator;
import com.labvantage.sapphire.util.evaluator.ParseException;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.SapphireException;
import sapphire.util.FormatUtil;
import sapphire.util.StringUtil;

public class ExpressionUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 80943 $";
    static final char decimalSeparator;
    static final Pattern usPattern;

    public static String evaluate(String expression, HashMap params) throws SapphireException {
        return ExpressionUtil.evaluate(expression, params, false, false);
    }

    public static String evaluate(String expression, HashMap params, boolean honorNullInRelationalExpressions, boolean honorNullInANDORConditionals) throws SapphireException {
        return ExpressionUtil.evaluateCommon(expression, params, honorNullInRelationalExpressions, honorNullInANDORConditionals, null, "N");
    }

    public static String evaluateSecure(ConnectionInfo connectionInfo, String expression, HashMap params) throws SapphireException {
        return ExpressionUtil.evaluateSecure(connectionInfo, expression, params, false, false, "N");
    }

    public static String evaluateSecure(ConnectionInfo connectionInfo, String expression, HashMap params, boolean honorNullInRelationalExpressions, boolean honorNullInANDORConditionals) throws SapphireException {
        return ExpressionUtil.evaluateCommon(expression, params, honorNullInRelationalExpressions, honorNullInANDORConditionals, connectionInfo, "N");
    }

    public static String evaluateSecure(ConnectionInfo connectionInfo, String expression, HashMap params, boolean honorNullInRelationalExpressions, boolean honorNullInANDORConditionals, String datatypes) throws SapphireException {
        return ExpressionUtil.evaluateCommon(expression, params, honorNullInRelationalExpressions, honorNullInANDORConditionals, connectionInfo, datatypes);
    }

    private static String evaluateCommon(String expression, HashMap params, boolean honorNullInRelationalExpressions, boolean honorNullInANDORConditionals, ConnectionInfo connectionInfo, String datatypes) throws SapphireException {
        try {
            String result;
            boolean isStringTypeResult = false;
            if (expression.startsWith("$G{") && expression.endsWith("}")) {
                Object value;
                HashMap groovyparams = new HashMap();
                int count = 0;
                for (String param : params.keySet()) {
                    value = params.get(param);
                    if (value instanceof BigDecimal[]) {
                        ArrayList<BigDecimal> values = new ArrayList<BigDecimal>();
                        BigDecimal[] bd = (BigDecimal[])value;
                        for (int i = 0; i < bd.length; ++i) {
                            values.add(bd[i]);
                        }
                        value = values;
                    }
                    groovyparams.put("p" + count, value);
                    expression = StringUtil.replaceAll(expression, "${" + param + "}", "params.p" + count);
                    ++count;
                }
                HashMap bindMap = new HashMap();
                bindMap.put("params", groovyparams);
                String string = result = connectionInfo != null ? GroovyUtil.getInstance(connectionInfo).evaluateSecure(expression, bindMap) : GroovyUtil.evaluate(expression, bindMap);
                if (datatypes.equals("A")) {
                    datatypes = DataEntryLimitsUtil.getApparentDatatype(result, new DateTimeUtil(), FormatUtil.getInstance());
                }
                if (datatypes.equals("N") || datatypes.equals("NC")) {
                    if (result.length() == 0) {
                        isStringTypeResult = true;
                    } else {
                        FormatUtil formatutil = FormatUtil.getInstance();
                        value = result;
                        String enteredUnit = "";
                        if (OpalUtil.isNotEmpty(result)) {
                            switch (result.charAt(0)) {
                                case '<': 
                                case '>': {
                                    if (result.length() > 1 && result.charAt(1) == '=') {
                                        value = result.substring(2);
                                        break;
                                    }
                                    value = result.substring(1);
                                    break;
                                }
                                case '=': {
                                    value = result.substring(1);
                                }
                            }
                        }
                        for (int i = ((String)value).length(); i > 0; --i) {
                            try {
                                formatutil.parseBigDecimal(((String)value).substring(0, i));
                                if (i == 0) break;
                                enteredUnit = ((String)value).substring(i).trim();
                                value = ((String)value).substring(0, i);
                                break;
                            }
                            catch (NumberFormatException numberFormatException) {
                                continue;
                            }
                        }
                        if (enteredUnit.length() == 0) {
                            try {
                                formatutil.parseBigDecimal((String)value);
                            }
                            catch (NumberFormatException nfe) {
                                isStringTypeResult = true;
                            }
                        }
                    }
                }
            } else {
                ExpressionEvaluator evaluator = new ExpressionEvaluator(new StringReader(""));
                evaluator.setHonorNullInRelationalExpressions(honorNullInRelationalExpressions);
                evaluator.setHonorNullInANDORConditionals(honorNullInANDORConditionals);
                result = evaluator.evaluate(expression, params);
                isStringTypeResult = evaluator.rtypeString;
            }
            if ((datatypes.equals("N") || datatypes.equals("NC")) && '.' != decimalSeparator && !isStringTypeResult) {
                return ExpressionUtil.convertToDefaultLocale(result);
            }
            return result;
        }
        catch (ParseException e) {
            throw new SapphireException("Failed to evaluate expression: " + expression + " with params: " + params.toString(), e);
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionInfo.getConnectionId())), e);
        }
    }

    private static String convertToDefaultLocale(String input) {
        Matcher matcher = usPattern.matcher(input);
        ArrayList<String> tokens = new ArrayList<String>();
        while (matcher.find()) {
            String matched = matcher.group();
            tokens.add(matched);
        }
        for (int i = 0; i < tokens.size(); ++i) {
            String t = (String)tokens.get(i);
            String nt = t.replace('.', decimalSeparator);
            input = StringUtil.replaceAll(input, t, nt);
        }
        return input;
    }

    static {
        usPattern = Pattern.compile("(\\d+\\\\.\\d+)|(\\.\\d)");
        decimalSeparator = new DecimalFormatSymbols(Locale.getDefault()).getDecimalSeparator();
    }
}

