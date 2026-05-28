/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.Trace;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class CompareNumbers
extends BaseAction
implements sapphire.action.CompareNumbers {
    @Override
    public boolean isDatabaseRequired() {
        return false;
    }

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        float value1 = Float.parseFloat(properties.getProperty("number1"));
        float value2 = Float.parseFloat(properties.getProperty("number2"));
        String o = properties.getProperty("operator");
        if (o.equals("=")) {
            properties.setProperty("comparison", value1 == value2 ? "Yes" : "No");
        } else if (o.equals("<=")) {
            properties.setProperty("comparison", value1 <= value2 ? "Yes" : "No");
        } else if (o.equals("<")) {
            properties.setProperty("comparison", value1 < value2 ? "Yes" : "No");
        } else if (o.equals(">=")) {
            properties.setProperty("comparison", value1 >= value2 ? "Yes" : "No");
        } else if (o.equals(">")) {
            properties.setProperty("comparison", value1 > value2 ? "Yes" : "No");
        } else if (o.equals("<>")) {
            properties.setProperty("comparison", value1 != value2 ? "Yes" : "No");
        } else {
            throw new SapphireException("INVALID_PROPERTY", "The operator " + o + " was not recognized.");
        }
        if (Trace.on) {
            Trace.log("Setting return property value of " + properties.getProperty("comparison"));
        }
    }
}

