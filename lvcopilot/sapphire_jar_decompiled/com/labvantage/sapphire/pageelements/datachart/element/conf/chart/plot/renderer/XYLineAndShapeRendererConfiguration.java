/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.AbstractLineAndShapeRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYItemRendererConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class XYLineAndShapeRendererConfiguration
extends AbstractLineAndShapeRendererConfiguration
implements Serializable {
    private final XYItemRendererConfiguration parent;

    public XYLineAndShapeRendererConfiguration(PropertyList xyLineAndShapeRendererProps, XYItemRendererConfiguration parent) {
        this(xyLineAndShapeRendererProps, parent, null);
    }

    public XYLineAndShapeRendererConfiguration(XYLineAndShapeRendererConfiguration copy, XYItemRendererConfiguration parent) {
        super(copy);
        this.parent = parent;
    }

    public XYLineAndShapeRendererConfiguration(PropertyList xyLineAndShapeRendererProps, XYItemRendererConfiguration parent, XYLineAndShapeRendererConfiguration defaultXYLineAndShapeRendererConf) {
        super(xyLineAndShapeRendererProps, (AbstractLineAndShapeRendererConfiguration)defaultXYLineAndShapeRendererConf);
        if (xyLineAndShapeRendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
    }

    public XYLineAndShapeRendererConfiguration(XYLineAndShapeRendererConfiguration copy, XYLineAndShapeRendererConfiguration override, XYItemRendererConfiguration parent) {
        super(copy, (AbstractLineAndShapeRendererConfiguration)override);
        this.parent = parent;
    }

    public XYItemRendererConfiguration getParent() {
        return this.parent;
    }
}

