/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.AxisLocation
 *  org.jfree.chart.plot.CombinedDomainXYPlot
 *  org.jfree.chart.plot.Plot
 *  org.jfree.chart.plot.XYPlot
 *  org.jfree.data.general.Dataset
 */
package com.labvantage.sapphire.pageelements.datachart.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.chart.plot.AbstractComponentPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.ConfigurableDrawingSupplier;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.XYPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CombinedDomainXYPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.XYPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.util.Set;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Dataset;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public class CombinedDomainXYPlotBuilder
extends AbstractComponentPlotBuilder {
    private final CombinedDomainXYPlot combinedDomainXYPlot = new CombinedDomainXYPlot();

    public CombinedDomainXYPlotBuilder(CombinedDomainXYPlotConfiguration combinedDomainXYPlotConf, Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo, PropertyList elements, boolean honorRTLmode) {
        super(combinedDomainXYPlotConf, data, chartBindingMap, connectionInfo, honorRTLmode);
        if (!data.isEmpty()) {
            ConfigurableDrawingSupplier drawingSupplier = new ConfigurableDrawingSupplier(combinedDomainXYPlotConf.getParent().getDrawingSupplierConfiguration());
            Set<String> plotIdSet = data.getPlotIdSet();
            for (String plotId : plotIdSet) {
                XYPlotConfiguration xyPlotConf;
                PlotConfiguration plotConf = combinedDomainXYPlotConf.getSubplotConfiguration(plotId);
                String elementId = plotConf.getElementId().evaluateNoException(chartBindingMap);
                if (elementId.isEmpty()) {
                    throw new IllegalArgumentException("Subplot element ID is empty for plot ID:" + plotId);
                }
                PropertyList subplotProps = elements.getPropertyListNotNull(elementId);
                if (!plotConf.isPlotCreatedFromElement(plotId, elementId)) {
                    plotConf.createPlotFromElement(subplotProps, elementId, connectionInfo);
                }
                if (!(xyPlotConf = plotConf.getXYPlotConfiguration()).getParent().hasDrawingSupplier()) {
                    xyPlotConf.getParent().setDrawingSupplier(drawingSupplier);
                }
                XYPlotBuilder xyPlotBuilder = new XYPlotBuilder(xyPlotConf, data, chartBindingMap, connectionInfo, honorRTLmode);
                this.combinedDomainXYPlot.add(xyPlotBuilder.getPlot());
            }
            AxisConfiguration domainAxisConf = combinedDomainXYPlotConf.getDomainAxisConfiguration();
            this.combinedDomainXYPlot.setDomainAxis(this.createValueAxis(domainAxisConf, true));
            this.combinedDomainXYPlot.setDomainAxisLocation(combinedDomainXYPlotConf.getDomainAxisLocation());
        } else {
            XYPlot noDataSubplot = new XYPlot();
            noDataSubplot.setNoDataMessage(combinedDomainXYPlotConf.getParent().getNoDataMessage());
            this.combinedDomainXYPlot.add(noDataSubplot);
            this.combinedDomainXYPlot.getDomainAxis().setVisible(false);
        }
    }

    public Plot getPlot() {
        return this.combinedDomainXYPlot;
    }

    @Override
    protected void initRenderer(int seriesIndex, int itemIndex, String rendererId, ItemStyleConfiguration itemStyleConf) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void addRangeMarker(int seriesGroupIndex, MarkerConfiguration markerConf) throws SapphireException {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void addDomainMarker(int seriesGroupIndex, MarkerConfiguration markerConf) throws SapphireException {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected int getItemCount(int seriesGroupIndex, int seriesIndex) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected int getSeriesCount(int seriesGroupIndex) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void setRangeAxisLocation(int rangeAxisIndex, AxisLocation axisLocation) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void setDomainAxisLocation(int domainAxisIndex, AxisLocation axisLocation) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void mapDatasetToRangeAxis(int index, String rangeAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void mapDatasetToDomainAxis(int index, String domainAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void setPlotRangeAxisFromCache(int index, String rangeAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void setPlotDomainAxisFromCache(int index, String domainAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void cacheRangeAxis(String rangeAxisId, AxisConfiguration axisConf) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void cacheDomainAxis(String domainAxisId, AxisConfiguration axisConf) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected boolean hasCachedRangeAxis(String rangeAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected boolean hasCachedDomainAxis(String domainAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void setPlotRendererFromCache(int index, String rendererId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void cacheRenderer(String rendererId, RendererConfiguration plotRendererConf, TraceableSeriesGroup traceableSeriesGroup, SeriesGroupConfiguration seriesGroupConf) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected boolean hasCachedRenderer(String rendererId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }

    @Override
    protected void setPlotDataset(int index, Dataset seriesGroup) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain XY plot");
    }
}

