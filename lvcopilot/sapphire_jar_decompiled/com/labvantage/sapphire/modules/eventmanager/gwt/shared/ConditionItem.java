/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.gwt.shared;

public class ConditionItem {
    public static final String STRING_OPERATORS = "S";
    public static final String NUMBER_OPERATORS = "N";
    public static final String DATE_OPERATORS = "D";
    public static final String ALL_OPERATORS = "A";
    public static final String GOES_TO = "goes to";
    public static final String EQUALS = "equals";
    public static final String NOT_EQUALS = "not equal";
    public static final String ISBLANK = "is blank";
    public static final String ISNOTBLANK = "is not blank";
    public static final String CONTAINS = "contains";
    public static final String STARTS_WITH = "starts with";
    public static final String ENDS_WITH = "ends with";
    public static final String BETWEEN = "between";
    public static final String GREATER_THAN = ">";
    public static final String GREATER_THAN_EQUAL = ">=";
    public static final String LESS_THAN = "<";
    public static final String LESS_THAN_EQUAL = "<=";
    public static final String CONDITIONITEM = "conditionitem";
    public static final String DISPLAYVALUE = "displayvalue";
    public static final String CONDITIONITEMSTATIC = "conditionitemstatic";
    public static final String OPERATORSTATIC = "operatorstatic";
    public static final String OPERATORS = "operators";
    public static final String VALUEEDITOR = "valueeditor";
    public static final String DYNAMICVALUEEDITOR = "dynamicvalueeditor";
    private String conditionitem;
    private String displayvalue;
    private String operators;
    private String valueEditor;
    private boolean staticConditionItem;
    private boolean staticOperator;
    private boolean dynamicValueEditor;

    public ConditionItem(String conditionitem, String operators, String valueEditor, boolean staticConditionItem, boolean staticOperator) {
        this.conditionitem = conditionitem;
        this.displayvalue = conditionitem;
        this.operators = operators;
        this.valueEditor = valueEditor;
        this.staticConditionItem = staticConditionItem;
        this.staticOperator = staticOperator;
    }

    public ConditionItem(String conditionitem, String displayvalue, String operators, String valueEditor) {
        this.conditionitem = conditionitem;
        this.displayvalue = displayvalue;
        this.operators = operators;
        this.valueEditor = valueEditor;
    }

    public ConditionItem(String conditionitem, String displayvalue, String operators, String valueEditor, boolean staticConditionItem, boolean staticOperator) {
        this.conditionitem = conditionitem;
        this.displayvalue = displayvalue;
        this.operators = operators;
        this.valueEditor = valueEditor;
        this.staticConditionItem = staticConditionItem;
        this.staticOperator = staticOperator;
    }

    public ConditionItem(String conditionitem, String operators, String valueEditor) {
        this.conditionitem = conditionitem;
        this.displayvalue = conditionitem;
        this.operators = operators;
        this.valueEditor = valueEditor;
    }

    public ConditionItem(String conditionitem, String displayvalue, String operators, boolean dynamicValueEditor) {
        this.conditionitem = conditionitem;
        this.displayvalue = displayvalue;
        this.operators = operators;
        this.dynamicValueEditor = dynamicValueEditor;
    }

    public String getConditionitem() {
        return this.conditionitem;
    }

    public String getDisplayvalue() {
        return this.displayvalue;
    }

    public String getOperators() {
        return this.operators;
    }

    public String getValueEditor() {
        return this.valueEditor;
    }

    public boolean isStaticConditionItem() {
        return this.staticConditionItem;
    }

    public boolean isStaticOperator() {
        return this.staticOperator;
    }

    public boolean isDynamicValueEditor() {
        return this.dynamicValueEditor;
    }

    public static String[] getOperators(String operator) {
        if (operator.equals(STRING_OPERATORS)) {
            return new String[]{EQUALS, NOT_EQUALS, ISBLANK, ISNOTBLANK, CONTAINS, STARTS_WITH, ENDS_WITH};
        }
        if (operator.equals(NUMBER_OPERATORS)) {
            return new String[]{EQUALS, NOT_EQUALS, ISBLANK, ISNOTBLANK, GREATER_THAN, LESS_THAN, GREATER_THAN_EQUAL, LESS_THAN_EQUAL, BETWEEN};
        }
        if (operator.equals(DATE_OPERATORS)) {
            return new String[]{EQUALS, NOT_EQUALS, ISBLANK, ISNOTBLANK, GREATER_THAN, LESS_THAN, GREATER_THAN_EQUAL, LESS_THAN_EQUAL, BETWEEN};
        }
        if (operator.equals(ALL_OPERATORS)) {
            return new String[]{EQUALS, NOT_EQUALS, ISBLANK, ISNOTBLANK, CONTAINS, STARTS_WITH, ENDS_WITH, GREATER_THAN, LESS_THAN, GREATER_THAN_EQUAL, LESS_THAN_EQUAL, BETWEEN};
        }
        return operator.split(";");
    }

    public static boolean conditionMatch(Object matchValue, Object value1, Object value2, String operator) {
        if (EQUALS.equals(operator) || "eq".equals(operator)) {
            return value1 == null && matchValue == null || value1 != null && matchValue != null && ((Comparable)matchValue).compareTo(value1) == 0;
        }
        if (NOT_EQUALS.equals(operator) || "ne".equals(operator)) {
            return value1 == null && matchValue != null || value1 != null && matchValue == null || value1 != null && matchValue != null && ((Comparable)matchValue).compareTo(value1) != 0;
        }
        if (ISBLANK.equalsIgnoreCase(operator) || "bl".equals(operator)) {
            return matchValue == null || matchValue.toString().length() == 0;
        }
        if (ISNOTBLANK.equalsIgnoreCase(operator) || "nb".equals(operator)) {
            return matchValue != null && matchValue.toString().length() > 0;
        }
        if (GREATER_THAN.equalsIgnoreCase(operator) || "gt".equals(operator)) {
            return value1 != null && matchValue != null && ((Comparable)matchValue).compareTo(value1) > 0;
        }
        if (GREATER_THAN_EQUAL.equalsIgnoreCase(operator) || "ge".equals(operator)) {
            return value1 != null && matchValue != null && ((Comparable)matchValue).compareTo(value1) >= 0;
        }
        if (LESS_THAN.equalsIgnoreCase(operator) || "lt".equals(operator)) {
            return value1 != null && matchValue != null && ((Comparable)matchValue).compareTo(value1) < 0;
        }
        if (LESS_THAN_EQUAL.equalsIgnoreCase(operator) || "le".equals(operator)) {
            return value1 != null && matchValue != null && ((Comparable)matchValue).compareTo(value1) <= 0;
        }
        if (BETWEEN.equalsIgnoreCase(operator) || "eb".equals(operator)) {
            return value1 != null && value2 != null && matchValue != null && ((Comparable)matchValue).compareTo(value1) > 0 && ((Comparable)matchValue).compareTo(value2) < 0;
        }
        if ("ib".equals(operator)) {
            return value1 != null && value2 != null && matchValue != null && ((Comparable)matchValue).compareTo(value1) >= 0 && ((Comparable)matchValue).compareTo(value2) <= 0;
        }
        if (CONTAINS.equalsIgnoreCase(operator) || "co".equals(operator)) {
            return value1 != null && matchValue != null && matchValue instanceof String && ((String)matchValue).contains((String)value1);
        }
        if (STARTS_WITH.equalsIgnoreCase(operator) || "sw".equals(operator)) {
            return value1 != null && matchValue != null && matchValue instanceof String && ((String)matchValue).startsWith((String)value1);
        }
        if (ENDS_WITH.equalsIgnoreCase(operator) || "ew".equals(operator)) {
            return value1 != null && matchValue != null && matchValue instanceof String && ((String)matchValue).endsWith((String)value1);
        }
        return false;
    }
}

