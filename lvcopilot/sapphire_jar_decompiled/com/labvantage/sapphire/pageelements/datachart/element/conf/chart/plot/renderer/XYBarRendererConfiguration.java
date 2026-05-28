/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.AbstractBarRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYItemRendererConfiguration;
import java.io.Serializable;
import java.math.BigDecimal;
import sapphire.xml.PropertyList;

public class XYBarRendererConfiguration
extends AbstractBarRendererConfiguration
implements Serializable {
    private final XYItemRendererConfiguration parent;
    private static final double DEFAULT_MARGIN = 0.0;
    private final double margin;

    public XYBarRendererConfiguration(PropertyList xYbarRendererConfiguration, XYItemRendererConfiguration parent) {
        super(xYbarRendererConfiguration);
        if (xYbarRendererConfiguration == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        double marginValue = 0.0;
        String marginStr = xYbarRendererConfiguration.getProperty("margin", "");
        if (marginStr.length() > 0) {
            try {
                marginValue = new BigDecimal(marginStr).doubleValue();
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("XY Bar renderer configuration: Not valid item margin: " + marginStr);
            }
        }
        this.margin = marginValue;
        this.parent = parent;
    }

    public XYBarRendererConfiguration(PropertyList xyBarRendererProps, XYItemRendererConfiguration parent, XYBarRendererConfiguration defaultBarRendererConf) {
        super(xyBarRendererProps, defaultBarRendererConf);
        if (xyBarRendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        double defaultMargin = 0.0;
        if (defaultBarRendererConf != null) {
            defaultMargin = defaultBarRendererConf.getMargin();
        }
        double marginValue = defaultMargin;
        String marginStr = xyBarRendererProps.getProperty("margin", "");
        if (marginStr.length() > 0) {
            try {
                marginValue = new BigDecimal(marginStr).doubleValue();
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Bar renderer configuration: Not valid item margin: " + marginStr);
            }
        }
        this.margin = marginValue;
    }

    public XYBarRendererConfiguration(XYBarRendererConfiguration copy, XYItemRendererConfiguration parent) {
        super(copy);
        this.parent = parent;
        this.margin = copy.margin;
    }

    public XYBarRendererConfiguration(XYBarRendererConfiguration copy, XYBarRendererConfiguration override, XYItemRendererConfiguration parent) {
        super(copy);
        this.parent = parent;
        this.margin = copy.margin;
    }

    public XYItemRendererConfiguration getParent() {
        return this.parent;
    }

    public double getMargin() {
        return this.margin;
    }
}

