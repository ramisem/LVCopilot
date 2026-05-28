/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CustomTrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BigDecimalExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class LineTrendLineConfiguration
implements Serializable {
    private static final String DEFAULT_SLOPE = "1";
    private static final String DEFAULT_INTERCEPT = "1";
    private final CustomTrendLineConfiguration parent;
    private final BigDecimalExpression slope;
    private final BigDecimalExpression intercept;

    protected LineTrendLineConfiguration(PropertyList lineFunctionProps, CustomTrendLineConfiguration parent) {
        if (lineFunctionProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.slope = new BigDecimalExpression(lineFunctionProps.getProperty("slope", "1"));
        this.intercept = new BigDecimalExpression(lineFunctionProps.getProperty("intercept", "1"));
        this.parent = parent;
    }

    protected LineTrendLineConfiguration(LineTrendLineConfiguration copy, CustomTrendLineConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.slope = copy.slope;
        this.intercept = copy.intercept;
    }

    public BigDecimalExpression getSlope() {
        return this.slope;
    }

    public BigDecimalExpression getIntercept() {
        return this.intercept;
    }

    public CustomTrendLineConfiguration getParent() {
        return this.parent;
    }
}

