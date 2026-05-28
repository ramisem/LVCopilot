/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BigDecimalExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class IntervalMarkerConfiguration
implements Serializable {
    private static final String DEFAULT_START = "0.0";
    private static final String DEFAULT_END = "1.0";
    private final BigDecimalExpression start;
    private final BigDecimalExpression end;
    private final MarkerConfiguration parent;

    public IntervalMarkerConfiguration(PropertyList intervalMarkerProps, MarkerConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (intervalMarkerProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.start = new BigDecimalExpression(intervalMarkerProps.getProperty("start", DEFAULT_START));
        this.end = new BigDecimalExpression(intervalMarkerProps.getProperty("end", DEFAULT_END));
        this.parent = parent;
    }

    public IntervalMarkerConfiguration(IntervalMarkerConfiguration copy, MarkerConfiguration parent) {
        this.start = new BigDecimalExpression(copy.start);
        this.end = new BigDecimalExpression(copy.end);
        this.parent = parent;
    }

    public BigDecimalExpression getStart() {
        return this.start;
    }

    public BigDecimalExpression getEnd() {
        return this.end;
    }

    public MarkerConfiguration getParent() {
        return this.parent;
    }
}

