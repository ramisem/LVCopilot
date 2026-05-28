/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class RadioButtonConfiguration
implements Serializable {
    private final ButtonConfiguration parent;
    private final String argumentId;
    private final StringExpression argumentValue;

    public RadioButtonConfiguration(PropertyList toggleButtonProps, ButtonConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (toggleButtonProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.argumentId = toggleButtonProps.getProperty("argumentid");
        this.argumentValue = new StringExpression(toggleButtonProps.getProperty("argumentvalue"));
    }

    public RadioButtonConfiguration(RadioButtonConfiguration copy, ButtonConfiguration parent) {
        this.parent = parent;
        this.argumentId = copy.argumentId;
        this.argumentValue = copy.argumentValue;
    }

    public ButtonConfiguration getParent() {
        return this.parent;
    }

    public String getArgumentId() {
        return this.argumentId;
    }

    public StringExpression getArgumentValue() {
        return this.argumentValue;
    }
}

