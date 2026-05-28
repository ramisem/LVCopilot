/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.TrendLineConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class CustomTrendLineConfiguration
implements Serializable {
    private final TrendLineConfiguration parent;
    private final String customTrendLineId;

    public CustomTrendLineConfiguration(PropertyList customTrendLineProps, TrendLineConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (customTrendLineProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.customTrendLineId = customTrendLineProps.getProperty("customtrendlineid", "");
        this.parent = parent;
    }

    public CustomTrendLineConfiguration(CustomTrendLineConfiguration copy, TrendLineConfiguration parent) {
        this.customTrendLineId = copy.customTrendLineId;
        this.parent = parent;
    }

    public String getCustomTrendLineId() {
        return this.customTrendLineId;
    }

    public TrendLineConfiguration getParent() {
        return this.parent;
    }
}

