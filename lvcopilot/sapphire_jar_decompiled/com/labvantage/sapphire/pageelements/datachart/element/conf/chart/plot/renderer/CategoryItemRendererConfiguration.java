/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.AbstractItemRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.BarRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.LineAndShapeRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class CategoryItemRendererConfiguration
extends AbstractItemRendererConfiguration
implements Serializable {
    private final LineAndShapeRendererConfiguration lineAndShapeRendererConf;
    private final BarRendererConfiguration barRendererConf;

    public CategoryItemRendererConfiguration(PropertyList categoryItemRendererProps, RendererConfiguration.RendererType rendererType) {
        super(categoryItemRendererProps, rendererType);
        this.lineAndShapeRendererConf = this.getRendererType() == RendererConfiguration.RendererType.LINE_AND_SHAPE ? new LineAndShapeRendererConfiguration(categoryItemRendererProps.getPropertyListNotNull("lineandshaperendererprops"), this) : null;
        this.barRendererConf = this.getRendererType() == RendererConfiguration.RendererType.BAR || this.getRendererType() == RendererConfiguration.RendererType.BAR_3D || this.getRendererType() == RendererConfiguration.RendererType.STACKED_BAR || this.getRendererType() == RendererConfiguration.RendererType.STACKED_BAR_3D ? new BarRendererConfiguration(categoryItemRendererProps.getPropertyListNotNull("barrendererprops"), this) : null;
    }

    public CategoryItemRendererConfiguration(PropertyList categoryItemRendererProps, RendererConfiguration.RendererType rendererType, CategoryItemRendererConfiguration defaultCategoryItemRendererConf) {
        super(categoryItemRendererProps, rendererType);
        this.lineAndShapeRendererConf = rendererType == RendererConfiguration.RendererType.LINE_AND_SHAPE ? (defaultCategoryItemRendererConf != null ? new LineAndShapeRendererConfiguration(categoryItemRendererProps.getPropertyListNotNull("lineandshaperendererprops"), this, defaultCategoryItemRendererConf.getLineAndShapeConfiguration()) : new LineAndShapeRendererConfiguration(categoryItemRendererProps.getPropertyListNotNull("lineandshaperendererprops"), this)) : null;
        this.barRendererConf = rendererType == RendererConfiguration.RendererType.BAR ? (defaultCategoryItemRendererConf != null ? new BarRendererConfiguration(categoryItemRendererProps.getPropertyListNotNull("barrendererprops"), this, defaultCategoryItemRendererConf.getBarRendererConfiguration()) : new BarRendererConfiguration(categoryItemRendererProps.getPropertyListNotNull("barrendererprops"), this)) : null;
    }

    public CategoryItemRendererConfiguration(CategoryItemRendererConfiguration copy) {
        super(copy);
        this.lineAndShapeRendererConf = copy.lineAndShapeRendererConf != null ? new LineAndShapeRendererConfiguration(copy.lineAndShapeRendererConf, this) : null;
        this.barRendererConf = copy.barRendererConf != null ? new BarRendererConfiguration(copy.barRendererConf, this) : null;
    }

    public CategoryItemRendererConfiguration(CategoryItemRendererConfiguration copy, CategoryItemRendererConfiguration override) {
        super(copy, override);
        this.lineAndShapeRendererConf = copy.lineAndShapeRendererConf != null && override.lineAndShapeRendererConf != null ? new LineAndShapeRendererConfiguration(copy.lineAndShapeRendererConf, override.lineAndShapeRendererConf, this) : (copy.lineAndShapeRendererConf != null ? new LineAndShapeRendererConfiguration(copy.lineAndShapeRendererConf, this) : (override.lineAndShapeRendererConf != null ? new LineAndShapeRendererConfiguration(override.lineAndShapeRendererConf, this) : null));
        this.barRendererConf = copy.barRendererConf != null && override.barRendererConf != null ? new BarRendererConfiguration(copy.barRendererConf, override.barRendererConf, this) : (copy.barRendererConf != null ? new BarRendererConfiguration(copy.barRendererConf, this) : (override.barRendererConf != null ? new BarRendererConfiguration(override.barRendererConf, this) : null));
    }

    public LineAndShapeRendererConfiguration getLineAndShapeConfiguration() {
        if (this.lineAndShapeRendererConf == null) {
            throw new IllegalStateException("Renderer type is: " + (Object)((Object)this.getRendererType()));
        }
        return this.lineAndShapeRendererConf;
    }

    public BarRendererConfiguration getBarRendererConfiguration() {
        if (this.barRendererConf == null) {
            throw new IllegalStateException("Renderer type is: " + (Object)((Object)this.getRendererType()));
        }
        return this.barRendererConf;
    }
}

