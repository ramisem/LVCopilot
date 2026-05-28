/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.TrendLineConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class StandardTrendLineConfiguration
implements Serializable {
    private static final String DEFAULT_STANDARD_TREND_LINE_TYPE = StandardTrendLineType.LINEAR_TREND_LINE.getName();
    private final TrendLineConfiguration parent;
    private final StandardTrendLineType standardTrendLineType;

    public StandardTrendLineConfiguration(PropertyList standardTrendLineProps, TrendLineConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (standardTrendLineProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.standardTrendLineType = StandardTrendLineType.fromString(standardTrendLineProps.getProperty("standardtrendlinetype", DEFAULT_STANDARD_TREND_LINE_TYPE));
        this.parent = parent;
    }

    public StandardTrendLineConfiguration(StandardTrendLineConfiguration copy, TrendLineConfiguration parent) {
        this.standardTrendLineType = copy.standardTrendLineType;
        this.parent = parent;
    }

    public StandardTrendLineType getStandardTrendLineType() {
        return this.standardTrendLineType;
    }

    public TrendLineConfiguration getParent() {
        return this.parent;
    }

    public static enum StandardTrendLineType {
        LINEAR_TREND_LINE("Linear"),
        POWER_TREND_LINE("Power"),
        MOVING_AVERAGE_TREND_LINE("Moving Average"),
        POLYNOMIAL_TREND_LINE("Polynomial");

        private final String name;

        private StandardTrendLineType(String name) {
            this.name = name;
        }

        public static StandardTrendLineType fromString(String name) {
            if (name != null) {
                for (StandardTrendLineType type : StandardTrendLineType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

