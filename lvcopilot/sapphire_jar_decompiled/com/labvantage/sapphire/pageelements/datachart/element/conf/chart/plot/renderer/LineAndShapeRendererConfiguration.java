/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.AbstractLineAndShapeRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.CategoryItemRendererConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class LineAndShapeRendererConfiguration
extends AbstractLineAndShapeRendererConfiguration
implements Serializable {
    private final CategoryItemRendererConfiguration parent;

    public LineAndShapeRendererConfiguration(PropertyList lineAndShapeRendererProps, CategoryItemRendererConfiguration parent) {
        super(lineAndShapeRendererProps);
        if (lineAndShapeRendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
    }

    public LineAndShapeRendererConfiguration(PropertyList lineAndShapeRendererProps, CategoryItemRendererConfiguration parent, LineAndShapeRendererConfiguration defaultXYLineAndShapeRendererConf) {
        super(lineAndShapeRendererProps, (AbstractLineAndShapeRendererConfiguration)defaultXYLineAndShapeRendererConf);
        if (lineAndShapeRendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
    }

    public LineAndShapeRendererConfiguration(LineAndShapeRendererConfiguration copy, CategoryItemRendererConfiguration parent) {
        super(copy);
        this.parent = parent;
    }

    public LineAndShapeRendererConfiguration(LineAndShapeRendererConfiguration copy, LineAndShapeRendererConfiguration override, CategoryItemRendererConfiguration parent) {
        super(copy, (AbstractLineAndShapeRendererConfiguration)override);
        this.parent = parent;
    }

    public CategoryItemRendererConfiguration getParent() {
        return this.parent;
    }
}

