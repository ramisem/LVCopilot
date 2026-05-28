/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class CategoryMarkerConfiguration
implements Serializable {
    private static final String DEFAULT_CATEGORY = "";
    private final StringExpression category;
    private final MarkerConfiguration parent;

    public CategoryMarkerConfiguration(PropertyList categoryMarkerProps, MarkerConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (categoryMarkerProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.category = new StringExpression(categoryMarkerProps.getProperty("category", DEFAULT_CATEGORY));
        this.parent = parent;
    }

    public CategoryMarkerConfiguration(CategoryMarkerConfiguration copy, MarkerConfiguration parent) {
        this.category = new StringExpression(copy.category);
        this.parent = parent;
    }

    public StringExpression getCategory() {
        return this.category;
    }

    public MarkerConfiguration getParent() {
        return this.parent;
    }
}

