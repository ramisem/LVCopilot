/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.StandardEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ExpressionToolTipEventConfiguration
implements Serializable {
    private final StandardEventConfiguration parent;
    private final StringExpression toolTip;

    public ExpressionToolTipEventConfiguration(PropertyList toolTipEventProps, StandardEventConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (toolTipEventProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.toolTip = new StringExpression(toolTipEventProps.getProperty("tooltip"));
        this.parent = parent;
    }

    public ExpressionToolTipEventConfiguration(ExpressionToolTipEventConfiguration copy, StandardEventConfiguration parent) {
        this.parent = parent;
        this.toolTip = copy.toolTip;
    }

    public StringExpression getToolTip() {
        return this.toolTip;
    }

    public StandardEventConfiguration getParent() {
        return this.parent;
    }
}

