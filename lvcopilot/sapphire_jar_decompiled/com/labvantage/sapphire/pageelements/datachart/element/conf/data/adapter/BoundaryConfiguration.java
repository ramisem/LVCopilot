/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ExtractBoundariesAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class BoundaryConfiguration
implements Serializable {
    private static final String DEFAULT_SERIES_ID = "";
    private final ExtractBoundariesAdapterConfiguration parent;
    private final StringExpression boundaryId;
    private final StringExpression valueColumnId;
    private final StringExpression seriesId;
    private final StringExpression seriesGroupId;

    public BoundaryConfiguration(PropertyList boundaryProps, ExtractBoundariesAdapterConfiguration parent) {
        if (boundaryProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.boundaryId = new StringExpression(boundaryProps.getProperty("boundaryid"));
        this.valueColumnId = new StringExpression(boundaryProps.getProperty("boundaryvaluecol"));
        this.seriesId = new StringExpression(boundaryProps.getProperty("boundaryseriesid", DEFAULT_SERIES_ID));
        this.seriesGroupId = new StringExpression(boundaryProps.getProperty("seriesgroupid"));
    }

    public StringExpression getBoundaryId() {
        return this.boundaryId;
    }

    public StringExpression getValueColumnId() {
        return this.valueColumnId;
    }

    public StringExpression getSeriesId() {
        return this.seriesId;
    }

    public StringExpression getSeriesGroupId() {
        return this.seriesGroupId;
    }

    public ExtractBoundariesAdapterConfiguration getParent() {
        return this.parent;
    }
}

