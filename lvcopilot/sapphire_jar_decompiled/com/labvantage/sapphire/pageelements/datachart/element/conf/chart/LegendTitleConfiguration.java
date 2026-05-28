/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TitleConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class LegendTitleConfiguration
implements Serializable {
    private final TitleConfiguration parent;
    private final PaintConfiguration backgroundPaint;

    public LegendTitleConfiguration(PropertyList legendTitleProps, TitleConfiguration parent) {
        this.backgroundPaint = new PaintConfiguration(legendTitleProps.getPropertyListNotNull("backgroundpaintprops"));
        this.parent = parent;
    }

    public PaintConfiguration getBackgroundPaint() {
        return this.backgroundPaint;
    }

    public TitleConfiguration getParent() {
        return this.parent;
    }
}

