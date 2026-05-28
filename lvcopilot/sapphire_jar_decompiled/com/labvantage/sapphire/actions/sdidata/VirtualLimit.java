/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import java.math.BigDecimal;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;

public class VirtualLimit {
    BigDecimal virtualValue1 = null;
    BigDecimal virtualValue2 = null;
    String virtualOperator1 = "";
    String virtualOperator2 = "";
    boolean isRequired = true;
    FormatUtil formatUtil = FormatUtil.getInstance();

    public boolean isRequired() {
        return this.isRequired;
    }

    public void addLimit(BigDecimal compareValue, String operator1, String value1String) {
        this.addLimit(compareValue, operator1, value1String, "", "", "");
    }

    public void addLimit(BigDecimal compareValue, String operator1, String value1String, String enteredoperator) {
        this.addLimit(compareValue, operator1, value1String, "", "", enteredoperator);
    }

    public void addLimit(BigDecimal compareValue, String operator1, String value1String, String operator2, String value2String) {
        this.addLimit(compareValue, operator1, value1String, operator2, value2String, "");
    }

    public void addLimit(BigDecimal compareValue, String operator1, String value1String, String operator2, String value2String, String enteredoperator) {
        int i;
        DataSet values;
        if (compareValue == null || !this.isRequired) {
            this.isRequired = false;
            return;
        }
        if (operator1.equals("In")) {
            values = new DataSet();
            values.addColumn("value", 1);
            values.addColumnValues("value", 1, value1String, ";");
            values.sort("value");
            for (i = 0; i < values.getRowCount(); ++i) {
                this.addLimit(compareValue, "=", values.getValue(i, "value"), "", "", enteredoperator);
            }
        }
        if (operator1.equalsIgnoreCase("Not In")) {
            values = new DataSet();
            values.addColumn("value", 1);
            values.addColumnValues("value", 1, value1String, ";");
            values.sort("value");
            for (i = 0; i < values.getRowCount(); ++i) {
                if (i == 0) {
                    this.addLimit(compareValue, "<", values.getValue(i, "value"), "", "", enteredoperator);
                }
                if (i == values.getRowCount() - 1) {
                    this.addLimit(compareValue, ">", values.getValue(i, "value"), "", "", enteredoperator);
                }
                if (i == values.getRowCount() - 1) continue;
                this.addLimit(compareValue, ">", values.getValue(i, "value"), "<", values.getValue(i + 1, "value"), enteredoperator);
            }
        }
        operator1 = operator1 == null || operator1.length() == 0 ? "=" : operator1;
        operator2 = operator2 == null || operator2.length() == 0 ? "=" : operator2;
        BigDecimal value1 = null;
        BigDecimal value2 = null;
        try {
            value1 = this.formatUtil.parseBigDecimal(value1String);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            value2 = this.formatUtil.parseBigDecimal(value2String);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (value1 == null) {
            return;
        }
        if (value2 != null && value2.compareTo(value1) == -1) {
            BigDecimal tempValue = value1;
            String tempOperator = operator1;
            value1 = value2;
            operator1 = operator2;
            value2 = tempValue;
            operator2 = tempOperator;
        }
        if (value1 != null && compareValue.compareTo(value1) == 0) {
            if (operator1.contains("=") && enteredoperator.length() == 0) {
                this.isRequired = false;
                return;
            }
            if (operator1.equals(enteredoperator)) {
                this.isRequired = false;
                return;
            }
        }
        if (value2 != null && compareValue.compareTo(value2) == 0) {
            if (operator2.contains("=") && enteredoperator.length() == 0) {
                this.isRequired = false;
                return;
            }
            if (operator2.equals(enteredoperator)) {
                this.isRequired = false;
                return;
            }
        }
        if (value2 == null) {
            if (compareValue.compareTo(value1) == -1 && operator1.contains("<")) {
                this.isRequired = false;
                return;
            }
            if (compareValue.compareTo(value1) == 1 && operator1.contains(">")) {
                this.isRequired = false;
                return;
            }
        } else if (compareValue.compareTo(value1) == 1 && compareValue.compareTo(value2) == -1) {
            this.isRequired = false;
            return;
        }
        if (value1.compareTo(compareValue) == -1 || value1.compareTo(compareValue) == 0 && operator1.equals("<")) {
            String candidateOperator;
            BigDecimal candidate;
            if (value2 == null || value1.compareTo(value2) == 1) {
                candidate = value1;
                candidateOperator = operator1;
            } else {
                candidate = value2;
                candidateOperator = operator2;
            }
            if (this.virtualValue1 == null || candidate.compareTo(this.virtualValue1) == 1) {
                this.virtualValue1 = candidate;
                this.virtualOperator1 = candidateOperator.equals("=") ? ">" : (candidateOperator.equals("<") ? ">=" : (candidateOperator.equals("<=") ? ">" : "="));
            }
        } else if (this.virtualValue2 == null || value1.compareTo(this.virtualValue2) == -1) {
            this.virtualValue2 = value1;
            this.virtualOperator2 = operator1.equals("=") ? "<" : (operator1.equals(">") ? "<=" : (operator1.equals(">=") ? "<" : (operator1.equals("<") ? ">=" : (operator1.equals("<=") ? ">" : "="))));
        }
    }

    public VirtualLimitRange getLimitRange() {
        if (!this.isRequired()) {
            return null;
        }
        if (this.virtualValue1 == null && this.virtualValue2 == null) {
            return null;
        }
        if (this.virtualValue1 == null && this.virtualValue2 != null) {
            return new VirtualLimitRange(this.virtualOperator2, this.virtualValue2, "", null);
        }
        return new VirtualLimitRange(this.virtualOperator1, this.virtualValue1, this.virtualOperator2, this.virtualValue2);
    }

    public class VirtualLimitRange {
        public String operator1 = "";
        public BigDecimal value1 = null;
        public String operator2 = "";
        public BigDecimal value2 = null;

        public VirtualLimitRange(String operator1, BigDecimal value1, String operator2, BigDecimal value2) {
            this.operator1 = operator1;
            this.value1 = value1;
            this.operator2 = operator2;
            this.value2 = value2;
        }
    }
}

