/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.groovy;

import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.HashMap;
import sapphire.util.StringUtil;

public class GroovyBuilder {
    public static final String CONDITION_SEPARATOR = "|%|";
    public static final String CONDITION_PART_SEPARATOR = "#$#";
    public static final String IS_NOT_NULL = "is not null";
    public static final String IS_NULL = "is null";
    public static final String EQUAL = "=";
    public static final String NOT_EQUAL = "!=";
    public static final String IN = "in";
    public static final String NOT_IN = "not in";
    public static final String LIKE = "like";
    public static final String START_WITH = "start with";
    public static final String END_WITH = "end with";
    public static final String GREATER = ">";
    public static final String LESS = "<";
    public static final String GREATER_EQUAL = ">=";
    public static final String LESS_EQUAL = "<=";

    public static String buildExpression(String uiexpression, HashMap bindingMap) {
        if (uiexpression.indexOf("/*-GAP Editor Generated-*/;") >= 0) {
            uiexpression = uiexpression.substring(uiexpression.indexOf("/*-GAP Editor Generated-*/;") + "/*-GAP Editor Generated-*/;".length());
        }
        if (uiexpression.indexOf(CONDITION_PART_SEPARATOR) > 0) {
            String[] conditions = StringUtil.split(uiexpression, CONDITION_SEPARATOR);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < conditions.length; ++i) {
                String[] parts = StringUtil.split(conditions[i], CONDITION_PART_SEPARATOR);
                if (i != 0) {
                    sb.append(" && ");
                }
                if (parts.length == 3) {
                    sb.append(GroovyBuilder.buildBooleanExpression(parts[0], parts[1], parts[2]));
                    continue;
                }
                if (parts.length != 4) continue;
                sb.append(GroovyBuilder.buildBooleanExpression(parts[0], parts[1], parts[2], parts[3], bindingMap));
            }
            return sb.toString();
        }
        return uiexpression;
    }

    public static String buildBooleanExpression(String objectref, String operator, String values) {
        return GroovyBuilder.buildBooleanExpression(objectref, operator, values, null, null);
    }

    public static String buildBooleanExpression(String objectref, String operator, String values, String datatype, HashMap bindingMap) {
        String className = "java.lang.String";
        try {
            className = GroovyUtil.evaluate(objectref + ".getClass().getName()", bindingMap);
        }
        catch (Exception exception) {
            // empty catch block
        }
        String expr = "";
        expr = "date".equals(datatype) || "dateonly".equals(datatype) ? GroovyBuilder.getCalendarCompareExp(operator, className, "dateonly".equals(datatype)) : ("number".equals(datatype) ? GroovyBuilder.getNumberCompareExp(operator, className) : GroovyBuilder.getStringCompareExp(operator, className));
        expr = StringUtil.replaceAll(expr, "[objectref]", objectref);
        expr = StringUtil.replaceAll(expr, "[objectvalue]", StringUtil.replaceAll(values, "'", "\\'"));
        return expr;
    }

    private static String getStringCompareExp(String operator, String className) {
        String nullCheckExpr;
        String objRef = "com.labvantage.sapphire.modules.documents.Field".equals(className) ? "[objectref].getValue()" : "[objectref]";
        String string = nullCheckExpr = "com.labvantage.sapphire.modules.documents.Field".equals(className) ? "[objectref]!=null&&" + objRef + "!=null" : "[objectref]!=null";
        String expr = IS_NOT_NULL.equals(operator) ? "[objectref]!=null && " + objRef + ".length()>0" : (IS_NULL.equals(operator) ? "[objectref]==null || " + objRef + ".length()==0" : (EQUAL.equals(operator) ? nullCheckExpr + " && " + objRef + ".equals('[objectvalue]')" : (NOT_EQUAL.equals(operator) ? nullCheckExpr + " && !" + objRef + ".equals('[objectvalue]')" : (IN.equals(operator) ? nullCheckExpr + " && ';[objectvalue]'.indexOf(';' + " + objRef + ")>=0" : (NOT_IN.equals(operator) ? nullCheckExpr + " && ';[objectvalue]'.indexOf(';' + " + objRef + ")<0" : (LIKE.equals(operator) ? nullCheckExpr + " && " + objRef + ".indexOf('[objectvalue]')>=0" : (START_WITH.equals(operator) ? nullCheckExpr + " && " + objRef + ".indexOf('[objectvalue]')>=0" : (END_WITH.equals(operator) ? nullCheckExpr + " && " + objRef + ".indexOf('[objectvalue]')>=0" : ""))))))));
        return "(" + expr + ")";
    }

    private static String getCalendarCompareExp(String operator, String className, boolean isDateOnly) {
        String expr = "";
        if ("com.labvantage.sapphire.modules.documents.Field".equals(className)) {
            if (IS_NOT_NULL.equals(operator) || IS_NULL.equals(operator)) {
                expr = IS_NOT_NULL.equals(operator) ? "([objectref]!=null && [objectref].getValue() != null )" : "([objectref]==null || [objectref].getValue() == null)";
            } else {
                String compareExpression = "[objectref]!=null && [objectref].getValue()!=null && [objectref].getValue().compareTo( m18n.parseCalendar('[objectvalue]'" + (isDateOnly ? ",false" : "") + ") )";
                expr = GroovyBuilder.getCompareExpression(compareExpression, operator);
            }
        } else if ("java.lang.String".equals(className)) {
            if (IS_NOT_NULL.equals(operator) || IS_NULL.equals(operator)) {
                expr = IS_NOT_NULL.equals(operator) ? "([objectref]!=null && [objectref].length() > 0 )" : "([objectref]==null || [objectref].length() == 0 )";
            } else {
                String compareExpression = "[objectref]!=null && [objectref].length()>0 && m18n.parseCalendar( [objectref]).compareTo( m18n.parseCalendar('[objectvalue]'" + (isDateOnly ? ",false" : "") + ") )";
                expr = GroovyBuilder.getCompareExpression(compareExpression, operator);
            }
        }
        return expr;
    }

    private static String getNumberCompareExp(String operator, String className) {
        String expr = "";
        if ("com.labvantage.sapphire.modules.documents.Field".equals(className)) {
            if (IS_NOT_NULL.equals(operator) || IS_NULL.equals(operator)) {
                expr = IS_NOT_NULL.equals(operator) ? "([objectref]!=null && [objectref].getValue() != null )" : "([objectref]==null || [objectref].getValue() == null)";
            } else {
                String compareExpression = "[objectref]!=null && [objectref].getValue()!=null && [objectref].getValue().compareTo( m18n.parseBigDecimal('[objectvalue]') )";
                expr = GroovyBuilder.getCompareExpression(compareExpression, operator);
            }
        } else if ("java.lang.String".equals(className)) {
            if (IS_NOT_NULL.equals(operator) || IS_NULL.equals(operator)) {
                expr = IS_NOT_NULL.equals(operator) ? "([objectref]!=null && [objectref].length() > 0 )" : "([objectref]==null || [objectref].length() == 0 )";
            } else {
                String compareExpression = "[objectref]!=null && [objectref].length()>0 && m18n.parseBigDecimal([objectref]).compareTo( m18n.parseBigDecimal('[objectvalue]') )";
                expr = GroovyBuilder.getCompareExpression(compareExpression, operator);
            }
        }
        return expr;
    }

    private static String getCompareExpression(String compareExpression, String operator) {
        return "(" + compareExpression + (EQUAL.equals(operator) ? "==0" : (NOT_EQUAL.equals(operator) ? "!=0" : (GREATER.equals(operator) ? ">0" : (LESS.equals(operator) ? "<0" : (GREATER_EQUAL.equals(operator) ? ">=0" : (LESS_EQUAL.equals(operator) ? "<=0" : "==0")))))) + ")";
    }
}

