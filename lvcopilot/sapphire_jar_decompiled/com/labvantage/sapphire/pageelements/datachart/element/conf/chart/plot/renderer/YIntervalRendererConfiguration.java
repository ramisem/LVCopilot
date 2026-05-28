/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYItemRendererConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class YIntervalRendererConfiguration
implements Serializable {
    private final XYItemRendererConfiguration parent;

    public YIntervalRendererConfiguration(PropertyList yIntervalRendererProps, XYItemRendererConfiguration parent) {
        if (yIntervalRendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
    }

    public YIntervalRendererConfiguration(YIntervalRendererConfiguration copy, XYItemRendererConfiguration parent) {
        this.parent = parent;
    }

    public YIntervalRendererConfiguration(YIntervalRendererConfiguration copy, YIntervalRendererConfiguration override, XYItemRendererConfiguration parent) {
        this.parent = parent;
    }

    public XYItemRendererConfiguration getParent() {
        return this.parent;
    }
}

