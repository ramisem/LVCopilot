/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupBuilderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupColumnConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class DefaultPieDatasetConfiguration
implements SeriesGroupColumnConfiguration,
Serializable {
    private static final String DEFAULT_KEY_COLUMN = "key";
    private static final String DEFAULT_VALUE_COLUMN = "value";
    private final String keyColumn;
    private final String valueColumn;
    private final SeriesGroupBuilderConfiguration parent;

    public DefaultPieDatasetConfiguration(PropertyList properties, SeriesGroupBuilderConfiguration parent) {
        if (properties == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.keyColumn = properties.getProperty("keycolumn", DEFAULT_KEY_COLUMN);
        this.valueColumn = properties.getProperty("valuecolumn", DEFAULT_VALUE_COLUMN);
        this.parent = parent;
    }

    public String getKeyColumn() {
        return this.keyColumn;
    }

    public String getValueColumn() {
        return this.valueColumn;
    }

    public SeriesGroupBuilderConfiguration getParent() {
        return this.parent;
    }

    @Override
    public String getDomainValueColumn() {
        return this.keyColumn;
    }

    @Override
    public String getRangeValueColumn() {
        return this.valueColumn;
    }
}

