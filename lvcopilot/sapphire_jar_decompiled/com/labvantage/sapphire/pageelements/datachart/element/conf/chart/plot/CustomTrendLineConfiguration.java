/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.LineTrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.XYPlotConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class CustomTrendLineConfiguration
implements Serializable {
    private static final String DEFAULT_CUSTOM_TREND_LINE_TYPE = CustomTrendLineType.LINE_TREND_LINE.getName();
    private final XYPlotConfiguration parent;
    private final LineTrendLineConfiguration lineTrendLineConf;
    private final CustomTrendLineType customTrendLineType;
    private final String customTrendLineId;

    protected CustomTrendLineConfiguration(PropertyList trendLineProps, XYPlotConfiguration parent) {
        if (trendLineProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.customTrendLineId = trendLineProps.getProperty("customtrendlineid", "");
        if (this.customTrendLineId.isEmpty()) {
            throw new IllegalArgumentException("Custom trend line ID is empty");
        }
        this.customTrendLineType = CustomTrendLineType.fromString(trendLineProps.getProperty("customtrendlinetype", DEFAULT_CUSTOM_TREND_LINE_TYPE));
        this.lineTrendLineConf = this.customTrendLineType == CustomTrendLineType.LINE_TREND_LINE ? new LineTrendLineConfiguration(trendLineProps.getPropertyListNotNull("linetrendlineprops"), this) : null;
        this.parent = parent;
    }

    protected CustomTrendLineConfiguration(CustomTrendLineConfiguration copy, XYPlotConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.customTrendLineId = copy.customTrendLineId;
        this.customTrendLineType = copy.customTrendLineType;
        this.lineTrendLineConf = copy.lineTrendLineConf != null ? new LineTrendLineConfiguration(copy.lineTrendLineConf, this) : null;
    }

    public String getCustomTrendLineId() {
        return this.customTrendLineId;
    }

    public LineTrendLineConfiguration getLineTrendLineConfiguration() {
        if (this.lineTrendLineConf == null) {
            throw new IllegalStateException("Trend line type is: " + (Object)((Object)this.customTrendLineType));
        }
        return this.lineTrendLineConf;
    }

    public XYPlotConfiguration getParent() {
        return this.parent;
    }

    public CustomTrendLineType getCustomTrendLineType() {
        return this.customTrendLineType;
    }

    public static enum CustomTrendLineType {
        LINE_TREND_LINE("Line");

        private final String name;

        private CustomTrendLineType(String name) {
            this.name = name;
        }

        public static CustomTrendLineType fromString(String name) {
            if (name != null) {
                for (CustomTrendLineType type : CustomTrendLineType.values()) {
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

