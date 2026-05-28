/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.AbstractItemRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYBarRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYLineAndShapeRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.YIntervalRendererConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class XYItemRendererConfiguration
extends AbstractItemRendererConfiguration
implements Serializable {
    private final XYLineAndShapeRendererConfiguration xyLineAndShapeRendererConf;
    private final YIntervalRendererConfiguration yIntervalRendererConf;
    private final XYBarRendererConfiguration xyBarRendererConf;

    public XYItemRendererConfiguration(PropertyList xyItemRendererProps, RendererConfiguration.RendererType rendererType) {
        this(xyItemRendererProps, rendererType, null);
    }

    public XYItemRendererConfiguration(XYItemRendererConfiguration copy) {
        super(copy);
        this.xyLineAndShapeRendererConf = copy.xyLineAndShapeRendererConf != null ? new XYLineAndShapeRendererConfiguration(copy.xyLineAndShapeRendererConf, this) : null;
        this.yIntervalRendererConf = copy.yIntervalRendererConf != null ? new YIntervalRendererConfiguration(copy.yIntervalRendererConf, this) : null;
        this.xyBarRendererConf = copy.xyBarRendererConf != null ? new XYBarRendererConfiguration(copy.xyBarRendererConf, this) : null;
    }

    public XYItemRendererConfiguration(PropertyList xyItemRendererProps, RendererConfiguration.RendererType rendererType, XYItemRendererConfiguration defaultXYItemRendererConf) {
        super(xyItemRendererProps, rendererType);
        this.xyLineAndShapeRendererConf = rendererType == RendererConfiguration.RendererType.XY_LINE_AND_SHAPE ? (defaultXYItemRendererConf != null ? new XYLineAndShapeRendererConfiguration(xyItemRendererProps.getPropertyListNotNull("xylineandshaperendererprops"), this, defaultXYItemRendererConf.getXYLineAndShapeConfiguration()) : new XYLineAndShapeRendererConfiguration(xyItemRendererProps.getPropertyListNotNull("xylineandshaperendererprops"), this)) : null;
        this.yIntervalRendererConf = rendererType == RendererConfiguration.RendererType.Y_INTERVAL ? new YIntervalRendererConfiguration(xyItemRendererProps.getPropertyListNotNull("yintervalrendererprops"), this) : null;
        this.xyBarRendererConf = rendererType == RendererConfiguration.RendererType.XY_BAR ? (defaultXYItemRendererConf != null ? new XYBarRendererConfiguration(xyItemRendererProps.getPropertyListNotNull("barrendererprops"), this, defaultXYItemRendererConf.getXyBarRendererConf()) : new XYBarRendererConfiguration(xyItemRendererProps.getPropertyListNotNull("barrendererprops"), this)) : null;
    }

    public XYItemRendererConfiguration(XYItemRendererConfiguration copy, XYItemRendererConfiguration override) {
        super(copy, override);
        this.xyLineAndShapeRendererConf = copy.xyLineAndShapeRendererConf != null && override.xyLineAndShapeRendererConf != null ? new XYLineAndShapeRendererConfiguration(copy.xyLineAndShapeRendererConf, override.xyLineAndShapeRendererConf, this) : (copy.xyLineAndShapeRendererConf != null ? new XYLineAndShapeRendererConfiguration(copy.xyLineAndShapeRendererConf, this) : (override.xyLineAndShapeRendererConf != null ? new XYLineAndShapeRendererConfiguration(override.xyLineAndShapeRendererConf, this) : null));
        this.yIntervalRendererConf = copy.yIntervalRendererConf != null && override.yIntervalRendererConf != null ? new YIntervalRendererConfiguration(copy.yIntervalRendererConf, override.yIntervalRendererConf, this) : (copy.yIntervalRendererConf != null ? new YIntervalRendererConfiguration(copy.yIntervalRendererConf, this) : (override.yIntervalRendererConf != null ? new YIntervalRendererConfiguration(override.yIntervalRendererConf, this) : null));
        this.xyBarRendererConf = copy.xyBarRendererConf != null && override.xyBarRendererConf != null ? new XYBarRendererConfiguration(copy.xyBarRendererConf, override.xyBarRendererConf, this) : (copy.yIntervalRendererConf != null ? new XYBarRendererConfiguration(copy.xyBarRendererConf, this) : (override.xyBarRendererConf != null ? new XYBarRendererConfiguration(override.xyBarRendererConf, this) : null));
    }

    public XYLineAndShapeRendererConfiguration getXYLineAndShapeConfiguration() {
        if (this.xyLineAndShapeRendererConf == null) {
            throw new IllegalStateException("Renderer type is: " + (Object)((Object)this.getRendererType()));
        }
        return this.xyLineAndShapeRendererConf;
    }

    public XYBarRendererConfiguration getXyBarRendererConf() {
        if (this.xyBarRendererConf == null) {
            throw new IllegalStateException("Renderer type is: " + (Object)((Object)this.getRendererType()));
        }
        return this.xyBarRendererConf;
    }
}

