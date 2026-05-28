/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.plot.SeriesRenderingOrder
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.AbstractComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CustomTrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.plot.SeriesRenderingOrder;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class XYPlotConfiguration
extends AbstractComponentPlotConfiguration
implements Serializable {
    private static final String DEFAULT_SERIES_RENDERING_ORDER = SeriesRenderingOrderType.fromSeriesRenderingOrder(SeriesRenderingOrder.REVERSE).getName();
    private final List<CustomTrendLineConfiguration> customTrendLineConfList = new ArrayList<CustomTrendLineConfiguration>();
    private final SeriesRenderingOrder seriesRenderingOrder;

    protected XYPlotConfiguration(PropertyList xyPlotProps, ConnectionInfo connectionInfo, PlotConfiguration parent) {
        super(xyPlotProps, connectionInfo, parent);
        PropertyListCollection customTrendLineCollection = xyPlotProps.getCollectionNotNull("customtrendlinecollection");
        for (int i = 0; i < customTrendLineCollection.size(); ++i) {
            PropertyList customTrendLineProps = customTrendLineCollection.getPropertyList(i);
            this.customTrendLineConfList.add(new CustomTrendLineConfiguration(customTrendLineProps, this));
        }
        this.seriesRenderingOrder = SeriesRenderingOrderType.fromString(xyPlotProps.getProperty("seriesrenderingorder", DEFAULT_SERIES_RENDERING_ORDER)).getSeriesRenderingOrder();
    }

    @Override
    protected RendererConfiguration.RendererType getDefaultRendererType() {
        return RendererConfiguration.RendererType.XY_LINE_AND_SHAPE;
    }

    protected XYPlotConfiguration(XYPlotConfiguration copy, PlotConfiguration parent) {
        super(copy, parent);
        for (CustomTrendLineConfiguration customTrendLineConfiguration : copy.customTrendLineConfList) {
            this.customTrendLineConfList.add(new CustomTrendLineConfiguration(customTrendLineConfiguration, this));
        }
        this.seriesRenderingOrder = copy.seriesRenderingOrder;
    }

    @Override
    protected MarkerConfiguration.MarkerType getDefaultRangeMarkerType() {
        return MarkerConfiguration.MarkerType.VALUE;
    }

    @Override
    protected MarkerConfiguration.MarkerType getDefaultDomainMarkerType() {
        return MarkerConfiguration.MarkerType.VALUE;
    }

    @Override
    protected AxisConfiguration.AxisType getDefaultRangeAxisType() {
        return AxisConfiguration.AxisType.VALUE;
    }

    @Override
    protected AxisConfiguration.AxisType getDefaultDomainAxisType() {
        return AxisConfiguration.AxisType.VALUE;
    }

    public SeriesRenderingOrder getSeriesRenderingOrder() {
        return this.seriesRenderingOrder;
    }

    public CustomTrendLineConfiguration getCustomTrendLineConfiguration(String trendLineId) {
        CustomTrendLineConfiguration returnConf = null;
        for (CustomTrendLineConfiguration trendLineConf : this.customTrendLineConfList) {
            if (!trendLineConf.getCustomTrendLineId().equals(trendLineId)) continue;
            returnConf = trendLineConf;
            break;
        }
        return returnConf;
    }

    public static enum SeriesRenderingOrderType {
        REVERSE("Reverse", SeriesRenderingOrder.REVERSE),
        FORWARD("Forward", SeriesRenderingOrder.FORWARD);

        private final String name;
        private final SeriesRenderingOrder seriesRenderingOrder;

        private SeriesRenderingOrderType(String name, SeriesRenderingOrder seriesRenderingOrder) {
            this.name = name;
            this.seriesRenderingOrder = seriesRenderingOrder;
        }

        public static SeriesRenderingOrderType fromSeriesRenderingOrder(SeriesRenderingOrder seriesRenderingOrder) {
            if (seriesRenderingOrder != null) {
                for (SeriesRenderingOrderType type : SeriesRenderingOrderType.values()) {
                    if (seriesRenderingOrder != type.seriesRenderingOrder) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown series rendering order: " + seriesRenderingOrder);
        }

        public static SeriesRenderingOrderType fromString(String name) {
            if (name != null) {
                for (SeriesRenderingOrderType type : SeriesRenderingOrderType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public SeriesRenderingOrder getSeriesRenderingOrder() {
            return this.seriesRenderingOrder;
        }

        public String getName() {
            return this.name;
        }
    }
}

