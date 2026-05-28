/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BigDecimalExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ValueMarkerConfiguration
implements Serializable {
    private static final String DEFAULT_VALUE = "0.0";
    private final BigDecimalExpression value;
    private final MarkerConfiguration parent;

    public ValueMarkerConfiguration(PropertyList valueMarkerProps, MarkerConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (valueMarkerProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.value = new BigDecimalExpression(valueMarkerProps.getProperty("value", DEFAULT_VALUE));
        this.parent = parent;
    }

    public ValueMarkerConfiguration(ValueMarkerConfiguration copy, MarkerConfiguration parent) {
        this.value = new BigDecimalExpression(copy.value);
        this.parent = parent;
    }

    public BigDecimalExpression getValue() {
        return this.value;
    }

    public MarkerConfiguration getParent() {
        return this.parent;
    }
}

