/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.ItemConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class MarkerConfiguration
implements Serializable {
    private static final String DEFAULT_VISIBLE = "Y";
    private final ItemConfiguration parent;
    private final String markerId;
    private final BooleanExpression visible;

    public MarkerConfiguration(PropertyList markerProps, ItemConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (markerProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.markerId = markerProps.getProperty("markerid");
        this.visible = new BooleanExpression(markerProps.getProperty("visible", DEFAULT_VISIBLE));
        this.parent = parent;
    }

    public MarkerConfiguration(MarkerConfiguration copy, ItemConfiguration parent) {
        this.markerId = copy.markerId;
        this.visible = new BooleanExpression(copy.visible);
        this.parent = parent;
    }

    public BooleanExpression isVisible() {
        return this.visible;
    }

    public String getMarkerId() {
        return this.markerId;
    }

    public ItemConfiguration getParent() {
        return this.parent;
    }
}

