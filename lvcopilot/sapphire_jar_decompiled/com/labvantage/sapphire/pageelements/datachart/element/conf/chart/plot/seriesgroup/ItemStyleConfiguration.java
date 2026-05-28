/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.ItemConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class ItemStyleConfiguration
implements Serializable {
    private static final String DEFAULT_ITEM_STYLE_ID = "";
    private static final String DEFAULT_LEGEND_LABEL = "";
    private static final String DEFAULT_SHOW_LEGEND_ITEM = "Y";
    private final ItemConfiguration parent;
    private final String itemStyleId;
    private final BooleanExpression enable;
    private final String legendLabel;
    private final boolean showLegendItem;

    public ItemStyleConfiguration(PropertyList itemStyleProps, ItemConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (itemStyleProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.itemStyleId = itemStyleProps.getProperty("itemstyleid", "");
        this.enable = new BooleanExpression(itemStyleProps.getProperty("enable"));
        this.legendLabel = itemStyleProps.getProperty("legendlabel", "");
        this.showLegendItem = itemStyleProps.getProperty("showlegenditem", DEFAULT_SHOW_LEGEND_ITEM).toLowerCase().startsWith("y");
        this.parent = parent;
    }

    public ItemStyleConfiguration(ItemStyleConfiguration copy, ItemConfiguration parent) {
        this.itemStyleId = copy.itemStyleId;
        this.enable = copy.enable;
        this.legendLabel = copy.legendLabel;
        this.showLegendItem = copy.showLegendItem;
        this.parent = parent;
    }

    public BooleanExpression getEnable() {
        return this.enable;
    }

    public String getItemStyleId() {
        return this.itemStyleId;
    }

    public ItemConfiguration getParent() {
        return this.parent;
    }

    public String getLegendLabel() {
        return this.legendLabel;
    }

    public boolean showLegendItem() {
        return this.showLegendItem;
    }
}

