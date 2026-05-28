/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ButtonSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ButtonConfiguration
implements Serializable {
    private static final String DEFAULT_VISIBLE = "Y";
    private final ButtonSetConfiguration parent;
    private final String buttonId;
    private final BooleanExpression visible;

    public ButtonConfiguration(PropertyList buttonProps, ButtonSetConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (buttonProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.buttonId = buttonProps.getProperty("buttonid", "");
        this.visible = new BooleanExpression(buttonProps.getProperty("visible", DEFAULT_VISIBLE));
        this.parent = parent;
    }

    public ButtonConfiguration(ButtonConfiguration copy, ButtonSetConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.buttonId = copy.buttonId;
        this.visible = copy.visible;
        this.parent = parent;
    }

    public BooleanExpression isVisible() {
        return this.visible;
    }

    public String getButtonId() {
        return this.buttonId;
    }

    public ButtonSetConfiguration getParent() {
        return this.parent;
    }
}

