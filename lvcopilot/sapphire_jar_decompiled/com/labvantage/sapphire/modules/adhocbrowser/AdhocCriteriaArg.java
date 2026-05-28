/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocArgument;

public class AdhocCriteriaArg
extends AdhocArgument {
    private String operator = "";
    private Object value = null;
    private String columntype = "";

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValueObject() {
        return this.value;
    }

    public void setValueObject(Object value) {
        this.value = value;
    }

    public String getColumntype() {
        return this.columntype;
    }

    public void setColumntype(String columntype) {
        this.columntype = columntype;
    }

    public boolean isValidCriteria() {
        boolean isValid = false;
        if ("is null".equals(this.operator) || "is not null".equals(this.operator)) {
            isValid = true;
        } else if (this.operator.length() > 0 && this.value != null) {
            isValid = true;
        }
        return isValid;
    }
}

