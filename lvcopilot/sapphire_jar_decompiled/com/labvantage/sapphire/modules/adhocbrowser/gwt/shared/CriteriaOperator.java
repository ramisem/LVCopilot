/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser.gwt.shared;

public class CriteriaOperator {
    public static final String IS_NOT_NULL = "is not null";
    public static final String IS_NULL = "is null";
    public static final String EQUAL = "=";
    public static final String NOT_EQUAL = "!=";
    public static final String IN = "in";
    public static final String NOT_IN = "not in";
    public static final String LIKE = "like";
    public static final String START_WITH = "start with";
    public static final String END_WITH = "end with";
    public static final String BETWEEN = "between";
    public static final String GREATER = ">";
    public static final String LESS = "<";
    public static final String GREATER_EQUAL = ">=";
    public static final String LESS_EQUAL = "<=";
    protected static String[][] dateOpMap = new String[][]{{">", "after"}, {"<", "before"}, {">=", "on or after"}, {"<=", "on or before"}, {"between", "between"}, {"=", "is"}, {"!=", "is not"}, {"is null", "is empty"}, {"is not null", "is not empty"}};
    protected static String[][] numberOpMap = new String[][]{{">", ">"}, {"<", "<"}, {">=", ">="}, {"<=", "<="}, {"between", "between"}, {"=", "is"}, {"!=", "is not"}, {"is null", "is empty"}, {"is not null", "is not empty"}};
    protected static String[][] stringOpMap = new String[][]{{"like", "contains"}, {"=", "is"}, {"start with", "starts with"}, {"end with", "ends with"}, {"!=", "is not"}, {"is null", "is empty"}, {"is not null", "is not empty"}};
    protected static String[][] listOpMap = new String[][]{{"=", "is"}, {"in", "is one of"}, {"not in", "is not one of"}, {"!=", "is not"}, {"is null", "is empty"}, {"is not null", "is not empty"}};
    protected static String[][] stringAndListOpMap = new String[][]{{"=", "is"}, {"!=", "is not"}, {"is null", "is empty"}, {"is not null", "is not empty"}, {"in", "is one of"}, {"not in", "is not one of"}, {"like", "contains"}, {"start with", "starts with"}, {"end with", "ends with"}};
    protected static String[][] lookupOpMap = new String[][]{{"=", "is"}, {"!=", "is not"}, {"is null", "is empty"}, {"is not null", "is not empty"}};
}

