/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupBuilderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupColumnConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public abstract class AbstractIntervalXYSeriesGroupConfiguration
implements SeriesGroupColumnConfiguration,
Serializable {
    private static final String DEFAULT_X_COLUMN = "createdt";
    private static final String DEFAULT_Y_COLUMN = "transformvalue";
    private final String xColumn;
    private final String yColumn;
    private final SeriesGroupBuilderConfiguration parent;

    public AbstractIntervalXYSeriesGroupConfiguration(PropertyList properties, SeriesGroupBuilderConfiguration parent) {
        if (properties == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.xColumn = properties.getProperty("xcolumn", DEFAULT_X_COLUMN);
        this.yColumn = properties.getProperty("ycolumn", DEFAULT_Y_COLUMN);
        this.parent = parent;
    }

    public String getYColumn() {
        return this.yColumn;
    }

    public String getXColumn() {
        return this.xColumn;
    }

    public SeriesGroupBuilderConfiguration getParent() {
        return this.parent;
    }

    @Override
    public String getDomainValueColumn() {
        return this.xColumn;
    }

    @Override
    public String getRangeValueColumn() {
        return this.yColumn;
    }
}

