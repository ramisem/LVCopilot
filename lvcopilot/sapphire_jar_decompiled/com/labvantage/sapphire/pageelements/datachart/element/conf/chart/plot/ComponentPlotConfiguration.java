/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.AxisLocation
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import java.util.List;
import org.jfree.chart.axis.AxisLocation;

public interface ComponentPlotConfiguration {
    public PlotConfiguration getParent();

    public List<RendererConfiguration> getRendererConfList();

    public SeriesGroupConfiguration getSeriesGroupConfiguration(String var1);

    public RendererConfiguration getRendererConfiguration(String var1);

    public MarkerConfiguration getRangeMarkerConfiguration(String var1);

    public MarkerConfiguration getDomainMarkerConfiguration(String var1);

    public AxisConfiguration getRangeAxisConfiguration(String var1);

    public AxisConfiguration getDomainAxisConfiguration(String var1);

    public AxisLocation getRangeAxisLocation(String var1);

    public AxisLocation getDomainAxisLocation(String var1);

    public PaintConfiguration getDomainGridlinePaintConfiguration();

    public PaintConfiguration getRangeGridlinePaintConfiguration();

    public boolean isDomainGridlineVisible();

    public boolean isRangeGridlineVisible();

    public StrokeConfiguration getDomainGridlineStrokeConfiguration();

    public StrokeConfiguration getRangeGridlineStrokeConfiguration();

    public List<SeriesGroupConfiguration> getSeriesGroupConfList();
}

