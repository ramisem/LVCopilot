/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.CustomTrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.StandardTrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class TrendLineConfiguration
implements SeriesGroupColumnConfiguration,
Serializable {
    private static final String DEFAULT_TREND_LINE_TYPE = TrendLineType.STANDARD_TREND_LINE.getName();
    private final SeriesConfiguration parent;
    private final TrendLineType trendLineType;
    private final StandardTrendLineConfiguration standardTrendLineConf;
    private final CustomTrendLineConfiguration customTrendLineConf;
    private final StringExpression trendLineSeriesId;
    private String rangeValueColumn;
    private String domainValueColumn;

    public TrendLineConfiguration(PropertyList trendLineProps, SeriesConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (trendLineProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.trendLineType = TrendLineType.fromString(trendLineProps.getProperty("trendlinetype", DEFAULT_TREND_LINE_TYPE));
        this.standardTrendLineConf = this.trendLineType == TrendLineType.STANDARD_TREND_LINE ? new StandardTrendLineConfiguration(trendLineProps.getPropertyListNotNull("standardtrendlineprops"), this) : null;
        this.customTrendLineConf = this.trendLineType == TrendLineType.CUSTOM_TREND_LINE ? new CustomTrendLineConfiguration(trendLineProps.getPropertyListNotNull("customtrendlineprops"), this) : null;
        this.trendLineSeriesId = new StringExpression(trendLineProps.getProperty("trendlineseriesid", ""));
        if (this.trendLineSeriesId.getExpression().isEmpty()) {
            throw new IllegalArgumentException("Trend line series ID is empty");
        }
        this.parent = parent;
        this.rangeValueColumn = null;
        this.domainValueColumn = null;
    }

    public TrendLineConfiguration(TrendLineConfiguration copy, SeriesConfiguration parent) {
        this.trendLineType = copy.trendLineType;
        this.standardTrendLineConf = copy.standardTrendLineConf != null ? new StandardTrendLineConfiguration(copy.standardTrendLineConf, this) : null;
        this.customTrendLineConf = copy.customTrendLineConf != null ? new CustomTrendLineConfiguration(copy.customTrendLineConf, this) : null;
        this.trendLineSeriesId = copy.trendLineSeriesId;
        this.parent = parent;
    }

    public StringExpression getTrendLineSeriesId() {
        return this.trendLineSeriesId;
    }

    public TrendLineType getTrendLineType() {
        return this.trendLineType;
    }

    public SeriesConfiguration getParent() {
        return this.parent;
    }

    public StandardTrendLineConfiguration getStandardTrendLineConfiguration() {
        if (this.standardTrendLineConf == null) {
            throw new IllegalStateException("Trend line type is: " + (Object)((Object)this.trendLineType));
        }
        return this.standardTrendLineConf;
    }

    public CustomTrendLineConfiguration getCustomTrendLineConfiguration() {
        if (this.customTrendLineConf == null) {
            throw new IllegalStateException("Trend line type is: " + (Object)((Object)this.trendLineType));
        }
        return this.customTrendLineConf;
    }

    @Override
    public String getDomainValueColumn() {
        if (this.domainValueColumn == null) {
            throw new IllegalStateException("Domain value column not set yet.");
        }
        return this.domainValueColumn;
    }

    public void setDomainValueColumn(String domainValueColumn) {
        if (domainValueColumn == null || domainValueColumn.isEmpty()) {
            throw new IllegalArgumentException("Domain column is null or empty");
        }
        this.domainValueColumn = domainValueColumn;
    }

    @Override
    public String getRangeValueColumn() {
        if (this.rangeValueColumn == null) {
            throw new IllegalStateException("Range value column not set yet.");
        }
        return this.rangeValueColumn;
    }

    public void setRangeValueColumn(String rangeValueColumn) {
        if (rangeValueColumn == null || rangeValueColumn.isEmpty()) {
            throw new IllegalArgumentException("Range column is null or empty: " + rangeValueColumn);
        }
        this.rangeValueColumn = rangeValueColumn;
    }

    public static enum TrendLineType {
        STANDARD_TREND_LINE("Standard"),
        CUSTOM_TREND_LINE("Custom");

        private final String name;

        private TrendLineType(String name) {
            this.name = name;
        }

        public static TrendLineType fromString(String name) {
            if (name != null) {
                for (TrendLineType type : TrendLineType.values()) {
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

