/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.gwt.shared;

import com.labvantage.sapphire.modules.eventmanager.gwt.shared.ConditionItem;

public class Condition {
    public static final String CONDITIONID = "conditionid";
    public static final String CONDITIONITEM = "conditionitem";
    public static final String VALUE1 = "value1";
    public static final String VALUE2 = "value2";
    public static final String OPERATOR1 = "operator1";
    public static final String OPERATOR2 = "operator2";
    private ConditionItem[] itemValues;

    public Condition(ConditionItem[] itemValues) {
        this.itemValues = itemValues;
    }

    public Condition(ConditionItem itemValue) {
        this.itemValues = new ConditionItem[1];
        this.itemValues[0] = itemValue;
    }

    public ConditionItem[] getConditionItems() {
        return this.itemValues;
    }
}

