/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class IsStringEqual
extends BaseAction
implements sapphire.action.IsStringEqual {
    @Override
    public boolean isDatabaseRequired() {
        return false;
    }

    @Override
    public void processAction(PropertyList properties) {
        String value2;
        String value1 = properties.getProperty("property1");
        properties.setProperty("isstringequal", value1.equals(value2 = properties.getProperty("property2")) ? "Yes" : "No");
    }
}

