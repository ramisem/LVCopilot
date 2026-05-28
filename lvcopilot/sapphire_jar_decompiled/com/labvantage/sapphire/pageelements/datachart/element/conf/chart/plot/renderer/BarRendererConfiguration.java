/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.AbstractBarRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.CategoryItemRendererConfiguration;
import java.io.Serializable;
import java.math.BigDecimal;
import sapphire.xml.PropertyList;

public class BarRendererConfiguration
extends AbstractBarRendererConfiguration
implements Serializable {
    private static final double DEFAULT_ITEM_MARGIN = 0.2;
    private final CategoryItemRendererConfiguration parent;
    private final double itemMargin;

    public BarRendererConfiguration(PropertyList barRendererConfiguration, CategoryItemRendererConfiguration parent) {
        super(barRendererConfiguration);
        if (barRendererConfiguration == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        double itemMarginValue = 0.2;
        String itemMarginStr = barRendererConfiguration.getProperty("itemmargin", "");
        if (itemMarginStr.length() > 0) {
            try {
                itemMarginValue = new BigDecimal(itemMarginStr).doubleValue();
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Bar renderer configuration: Not valid item margin: " + itemMarginStr, e);
            }
        }
        this.itemMargin = itemMarginValue;
        this.parent = parent;
    }

    public BarRendererConfiguration(PropertyList barRendererProps, CategoryItemRendererConfiguration parent, BarRendererConfiguration defaultBarRendererConf) {
        super(barRendererProps, defaultBarRendererConf);
        if (barRendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        double defaultItemMargin = 0.2;
        if (defaultBarRendererConf != null) {
            defaultItemMargin = defaultBarRendererConf.getItemMargin();
        }
        double itemMarginValue = defaultItemMargin;
        String itemMarginStr = barRendererProps.getProperty("itemmargin", "");
        if (itemMarginStr.length() > 0) {
            try {
                itemMarginValue = new BigDecimal(itemMarginStr).doubleValue();
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Bar renderer configuration: Not valid item margin: " + itemMarginStr, e);
            }
        }
        this.itemMargin = itemMarginValue;
        this.parent = parent;
    }

    public BarRendererConfiguration(BarRendererConfiguration copy, BarRendererConfiguration override, CategoryItemRendererConfiguration parent) {
        super(copy);
        this.parent = parent;
        this.itemMargin = copy.itemMargin;
    }

    public BarRendererConfiguration(BarRendererConfiguration copy, CategoryItemRendererConfiguration parent) {
        super(copy);
        this.parent = parent;
        this.itemMargin = copy.itemMargin;
    }

    public CategoryItemRendererConfiguration getParent() {
        return this.parent;
    }

    public double getItemMargin() {
        return this.itemMargin;
    }
}

