/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ToggleButtonConfiguration
implements Serializable {
    private final ButtonConfiguration parent;
    private final String argumentId;
    private final StringExpression onValue;
    private final StringExpression offValue;

    public ToggleButtonConfiguration(PropertyList toggleButtonProps, ButtonConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (toggleButtonProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.argumentId = toggleButtonProps.getProperty("argumentid");
        this.onValue = new StringExpression(toggleButtonProps.getProperty("onvalue"));
        this.offValue = new StringExpression(toggleButtonProps.getProperty("offvalue"));
    }

    public ToggleButtonConfiguration(ToggleButtonConfiguration copy, ButtonConfiguration parent) {
        this.parent = parent;
        this.argumentId = copy.argumentId;
        this.onValue = copy.onValue;
        this.offValue = copy.offValue;
    }

    public ButtonConfiguration getParent() {
        return this.parent;
    }

    public String getArgumentId() {
        return this.argumentId;
    }

    public StringExpression getOnValue() {
        return this.onValue;
    }

    public StringExpression getOffValue() {
        return this.offValue;
    }
}

