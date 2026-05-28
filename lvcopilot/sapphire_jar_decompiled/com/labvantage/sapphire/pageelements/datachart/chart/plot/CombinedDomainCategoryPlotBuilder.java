/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.AxisLocation
 *  org.jfree.chart.plot.CategoryPlot
 *  org.jfree.chart.plot.CombinedDomainCategoryPlot
 *  org.jfree.chart.plot.Plot
 *  org.jfree.data.general.Dataset
 */
package com.labvantage.sapphire.pageelements.datachart.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.chart.plot.AbstractComponentPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.CategoryPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.ConfigurableDrawingSupplier;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CategoryPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CombinedDomainCategoryPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.util.Set;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.data.general.Dataset;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public class CombinedDomainCategoryPlotBuilder
extends AbstractComponentPlotBuilder {
    private final CombinedDomainCategoryPlot combinedDomainCategoryPlot = new CombinedDomainCategoryPlot();

    public CombinedDomainCategoryPlotBuilder(CombinedDomainCategoryPlotConfiguration combinedDomainCategoryPlotConf, Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo, PropertyList elements, boolean honorRTLmode) {
        super(combinedDomainCategoryPlotConf, data, chartBindingMap, connectionInfo, honorRTLmode);
        if (!data.isEmpty()) {
            ConfigurableDrawingSupplier drawingSupplier = new ConfigurableDrawingSupplier(combinedDomainCategoryPlotConf.getParent().getDrawingSupplierConfiguration());
            Set<String> plotIdSet = data.getPlotIdSet();
            for (String plotId : plotIdSet) {
                CategoryPlotConfiguration categoryPlotConf;
                PlotConfiguration plotConf = combinedDomainCategoryPlotConf.getSubplotConfiguration(plotId);
                String elementId = plotConf.getElementId().evaluateNoException(chartBindingMap);
                if (elementId.isEmpty()) {
                    throw new IllegalArgumentException("Subplot element ID is empty for plot ID:" + plotId);
                }
                PropertyList subplotProps = elements.getPropertyListNotNull(elementId);
                if (!plotConf.isPlotCreatedFromElement(plotId, elementId)) {
                    plotConf.createPlotFromElement(subplotProps, elementId, connectionInfo);
                }
                if (!(categoryPlotConf = plotConf.getCategoryPlotConfiguration()).getParent().hasDrawingSupplier()) {
                    categoryPlotConf.getParent().setDrawingSupplier(drawingSupplier);
                }
                CategoryPlotBuilder categoryPlotBuilder = new CategoryPlotBuilder(categoryPlotConf, data, chartBindingMap, connectionInfo, honorRTLmode);
                this.combinedDomainCategoryPlot.add(categoryPlotBuilder.getPlot());
            }
            AxisConfiguration domainAxisConf = combinedDomainCategoryPlotConf.getDomainAxisConfiguration();
            this.combinedDomainCategoryPlot.setDomainAxis(this.createCategoryAxis(domainAxisConf));
            this.combinedDomainCategoryPlot.setDomainAxisLocation(combinedDomainCategoryPlotConf.getDomainAxisLocation());
        } else {
            CategoryPlot noDataSubplot = new CategoryPlot();
            noDataSubplot.setNoDataMessage(combinedDomainCategoryPlotConf.getParent().getNoDataMessage());
            this.combinedDomainCategoryPlot.add(noDataSubplot);
            this.combinedDomainCategoryPlot.getDomainAxis().setVisible(false);
        }
    }

    public Plot getPlot() {
        return this.combinedDomainCategoryPlot;
    }

    @Override
    protected void initRenderer(int seriesIndex, int itemIndex, String rendererId, ItemStyleConfiguration itemStyleConf) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void addRangeMarker(int seriesGroupIndex, MarkerConfiguration markerConf) throws SapphireException {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void addDomainMarker(int seriesGroupIndex, MarkerConfiguration markerConf) throws SapphireException {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected int getItemCount(int seriesGroupIndex, int seriesIndex) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected int getSeriesCount(int seriesGroupIndex) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void setRangeAxisLocation(int rangeAxisIndex, AxisLocation axisLocation) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void setDomainAxisLocation(int domainAxisIndex, AxisLocation axisLocation) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void mapDatasetToRangeAxis(int index, String rangeAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void mapDatasetToDomainAxis(int index, String domainAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void setPlotRangeAxisFromCache(int index, String rangeAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void setPlotDomainAxisFromCache(int index, String domainAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void cacheRangeAxis(String rangeAxisId, AxisConfiguration axisConf) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void cacheDomainAxis(String domainAxisId, AxisConfiguration axisConf) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected boolean hasCachedRangeAxis(String rangeAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected boolean hasCachedDomainAxis(String domainAxisId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void setPlotRendererFromCache(int index, String rendererId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void cacheRenderer(String rendererId, RendererConfiguration plotRendererConf, TraceableSeriesGroup traceableSeriesGroup, SeriesGroupConfiguration seriesGroupConf) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected boolean hasCachedRenderer(String rendererId) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }

    @Override
    protected void setPlotDataset(int index, Dataset seriesGroup) {
        throw new UnsupportedOperationException("This operation is not supported by combined domain Category plot");
    }
}

