/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.AbstractIntervalXYSeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupBuilderConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class YIntervalSeriesCollectionConfiguration
extends AbstractIntervalXYSeriesGroupConfiguration
implements Serializable {
    private final String yLowColumn;
    private final String yHighColumn;

    public YIntervalSeriesCollectionConfiguration(PropertyList yIntervalSeriesConfigProps, SeriesGroupBuilderConfiguration parent) {
        super(yIntervalSeriesConfigProps, parent);
        String column = yIntervalSeriesConfigProps.getProperty("ylowcolumn");
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("Y Low column not defined!");
        }
        this.yLowColumn = column;
        column = yIntervalSeriesConfigProps.getProperty("yhighcolumn");
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("Y High column not defined!");
        }
        this.yHighColumn = column;
    }

    public String getYHighColumn() {
        return this.yHighColumn;
    }

    public String getYLowColumn() {
        return this.yLowColumn;
    }
}

