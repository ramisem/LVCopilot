/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.Axis
 *  org.jfree.chart.axis.AxisLocation
 *  org.jfree.chart.axis.CategoryAxis
 *  org.jfree.chart.axis.DateAxis
 *  org.jfree.chart.axis.LogAxis
 *  org.jfree.chart.axis.NumberAxis
 *  org.jfree.chart.axis.ValueAxis
 *  org.jfree.chart.plot.CategoryMarker
 *  org.jfree.chart.plot.IntervalMarker
 *  org.jfree.chart.plot.Marker
 *  org.jfree.chart.plot.ValueMarker
 *  org.jfree.data.general.Dataset
 */
package com.labvantage.sapphire.pageelements.datachart.chart.plot;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.chart.urls.URLGenerator;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.CategoryAxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.DateAxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.LogAxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.NumberAxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.ValueAxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.CategoryMarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.IntervalMarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.ValueMarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleHelper;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.ItemConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.URLGeneratorConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.general.Dataset;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;

public abstract class AbstractComponentPlotBuilder
extends BaseCustom {
    private static final Font DEFAULT_MARKER_LABEL_FONT = new Font("SansSerif", 0, 9);
    private static final Paint DEFAULT_MARKER_LABEL_COLOR = Color.BLACK;
    private final ComponentPlotConfiguration componentPlotConf;
    private final ChartBindingMap chartBindingMap;
    private final Data data;
    private final ConnectionInfo connectionInfo;
    private final Map<String, URLGenerator> urlGeneratorMap;
    private final String plotId;
    private boolean honorRTLmode;

    public AbstractComponentPlotBuilder(ComponentPlotConfiguration componentPlotConf, Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo, boolean honorRTLmode) {
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        if (componentPlotConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Binding map is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        this.setConnectionId(connectionInfo.getConnectionId());
        this.data = data;
        this.componentPlotConf = componentPlotConf;
        this.chartBindingMap = chartBindingMap;
        this.connectionInfo = connectionInfo;
        this.urlGeneratorMap = new HashMap<String, URLGenerator>();
        this.plotId = componentPlotConf.getParent().getPlotId();
        this.honorRTLmode = honorRTLmode;
        for (URLGeneratorConfiguration urlGeneratorConf : componentPlotConf.getParent().getURLGeneratorConfigurationList()) {
            URLGenerator urlGenerator = new URLGenerator(urlGeneratorConf, data, connectionInfo.getConnectionId(), chartBindingMap);
            this.urlGeneratorMap.put(urlGeneratorConf.getUrlGeneratorId(), urlGenerator);
        }
    }

    public ComponentPlotConfiguration getComponentPlotConf() {
        return this.componentPlotConf;
    }

    public ChartBindingMap getChartBindingMap() {
        return this.chartBindingMap;
    }

    public Data getData() {
        return this.data;
    }

    public ConnectionInfo getConnectionInfo() {
        return this.connectionInfo;
    }

    public Map<String, URLGenerator> getUrlGeneratorMap() {
        return this.urlGeneratorMap;
    }

    protected void buildAxis(Axis axis, AxisConfiguration axisConf) {
        axis.setLabel(axisConf.getLabel().evaluateNoException(this.chartBindingMap));
        axis.setLabelAngle(axisConf.getLabelAngle());
        axis.setVisible(axisConf.isVisible());
        axis.setMinorTickMarksVisible(axisConf.isMinorTickMarksVisible());
        axis.setLabelPaint(axisConf.getLabelPaintConfiguration().getOrSetPaint(Axis.DEFAULT_AXIS_LABEL_PAINT));
        axis.setTickLabelPaint(axisConf.getTickLabelPaintConfiguration().getOrSetPaint(Axis.DEFAULT_TICK_LABEL_PAINT));
        axis.setTickMarkPaint(axisConf.getTickMarkPaintConfiguration().getOrSetPaint(Axis.DEFAULT_TICK_MARK_PAINT));
        axis.setAxisLinePaint(axisConf.getAxisLinePaintConfiguration().getOrSetPaint(Axis.DEFAULT_AXIS_LINE_PAINT));
    }

    protected String getPlotId() {
        return this.plotId;
    }

    protected ValueAxis createValueAxis(AxisConfiguration axisConf, boolean isDomainAxis) {
        NumberAxis valueAxis;
        ValueAxisConfiguration valueAxisConf = axisConf.getValueAxisConfiguration();
        ValueAxisConfiguration.ValueAxisType valueAxisType = valueAxisConf.getValueAxisType();
        if (valueAxisType == ValueAxisConfiguration.ValueAxisType.NUMBER) {
            valueAxis = this.createNumberAxis(valueAxisConf.getNumberAxisConfiguration());
        } else if (valueAxisType == ValueAxisConfiguration.ValueAxisType.DATE) {
            valueAxis = this.createDateAxis(valueAxisConf.getDateAxisConfiguration());
        } else if (valueAxisType == ValueAxisConfiguration.ValueAxisType.LOG) {
            valueAxis = this.createLogAxis(valueAxisConf.getLogAxisConfiguration());
        } else {
            throw new IllegalArgumentException("Unknown axis type: " + (Object)((Object)valueAxisType));
        }
        if (this.honorRTLmode && isDomainAxis && this.getConnectionProcessor().getSapphireConnection().isRtl()) {
            valueAxis.setInverted(true);
        }
        this.buildAxis((Axis)valueAxis, axisConf);
        valueAxis.setAutoTickUnitSelection(valueAxisConf.isAutoTickUnitSelection());
        valueAxis.setLowerMargin(valueAxisConf.getLowerMargin());
        valueAxis.setUpperMargin(valueAxisConf.getUpperMargin());
        if (!valueAxisConf.isAutorangeYaxis()) {
            valueAxis.setRange(valueAxisConf.getRange());
        }
        valueAxis.setVerticalTickLabels(valueAxisConf.isVerticalTicklabels());
        return valueAxis;
    }

    protected CategoryAxis createCategoryAxis(AxisConfiguration axisConf) {
        CategoryAxis categoryAxis = new CategoryAxis();
        this.buildAxis((Axis)categoryAxis, axisConf);
        CategoryAxisConfiguration categoryAxisConfiguration = axisConf.getCategoryAxisConfiguration();
        categoryAxis.setCategoryLabelPositions(categoryAxisConfiguration.getCategoryLabelPositions());
        categoryAxis.setLowerMargin(categoryAxisConfiguration.getLowerMargin());
        categoryAxis.setUpperMargin(categoryAxisConfiguration.getUpperMargin());
        return categoryAxis;
    }

    private LogAxis createLogAxis(LogAxisConfiguration logAxisConf) {
        LogAxis logAxis = new LogAxis();
        logAxis.setBase(logAxisConf.getBase());
        logAxis.setStandardTickUnits(logAxisConf.getStandardTickUnitSource());
        if (logAxisConf.hasNumberFormatOverride()) {
            logAxis.setNumberFormatOverride(logAxisConf.getNumberFormatOverride());
        }
        return logAxis;
    }

    private NumberAxis createNumberAxis(NumberAxisConfiguration numberAxisConf) {
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setStandardTickUnits(numberAxisConf.getStandardTickUnitSource());
        Locale locale = I18nUtil.getConnectionLocale(this.connectionInfo);
        NumberFormat overrideNumberFormat = NumberFormat.getNumberInstance(locale);
        overrideNumberFormat.setMaximumFractionDigits(numberAxisConf.getMaximumFractionDigits());
        numberAxis.setNumberFormatOverride(overrideNumberFormat);
        numberAxis.setAutoRangeIncludesZero(numberAxisConf.isAutoRangeIncludesZero());
        return numberAxis;
    }

    private DateAxis createDateAxis(DateAxisConfiguration dateAxisConf) {
        DateAxis dateAxis = new DateAxis();
        Locale locale = I18nUtil.getConnectionLocale(this.connectionInfo);
        TimeZone timeZone = I18nUtil.getConnectionTimeZone(this.connectionInfo);
        dateAxis.setStandardTickUnits(DateAxis.createStandardDateTickUnits((TimeZone)timeZone, (Locale)locale));
        if (dateAxisConf.hasDateFormatOverride()) {
            dateAxis.setDateFormatOverride(dateAxisConf.getDateFormatOverride());
        }
        return dateAxis;
    }

    protected final void configureComponentPlot(List<TraceableSeriesGroup> traceableSeriesGroupList) {
        ItemStyleHelper.resetActualItemStyleConfigurations(this.componentPlotConf);
        int domainAxisIndex = 0;
        int rangeAxisIndex = 0;
        for (int seriesGroupIndex = 0; seriesGroupIndex < traceableSeriesGroupList.size(); ++seriesGroupIndex) {
            String domainAxisId;
            String rangeAxisId;
            TraceableSeriesGroup traceableSeriesGroup = traceableSeriesGroupList.get(seriesGroupIndex);
            String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
            this.chartBindingMap.setActiveSeriesGroup(this.plotId, seriesGroupId);
            SeriesGroupConfiguration seriesGroupConf = this.componentPlotConf.getSeriesGroupConfiguration(seriesGroupId);
            try {
                rangeAxisId = seriesGroupConf.getRangeAxisId().evaluate(this.chartBindingMap);
            }
            catch (SapphireException e) {
                throw new IllegalArgumentException("Error while evaluating range axis ID expression: " + seriesGroupConf.getRangeAxisId(), e);
            }
            try {
                domainAxisId = seriesGroupConf.getDomainAxisId().evaluate(this.chartBindingMap);
            }
            catch (SapphireException e) {
                throw new IllegalArgumentException("Error while evaluating domain axis ID expression: " + seriesGroupConf.getDomainAxisId(), e);
            }
            rangeAxisIndex = this.handleRangeAxis(rangeAxisIndex, seriesGroupIndex, rangeAxisId);
            domainAxisIndex = this.handleDomainAxis(domainAxisIndex, seriesGroupIndex, domainAxisId);
            String rendererId = seriesGroupConf.getRendererId();
            Dataset seriesGroup = traceableSeriesGroup.getSeriesGroup();
            this.setPlotDataset(seriesGroupIndex, seriesGroup);
            RendererConfiguration rendererConf = this.componentPlotConf.getRendererConfiguration(rendererId);
            if (!this.hasCachedRenderer(rendererId)) {
                this.cacheRenderer(rendererId, rendererConf, traceableSeriesGroup, seriesGroupConf);
            }
            this.setPlotRendererFromCache(seriesGroupIndex, rendererId);
            for (int seriesIndex = 0; seriesIndex < this.getSeriesCount(seriesGroupIndex); ++seriesIndex) {
                String seriesId = traceableSeriesGroup.getSeriesId(seriesIndex);
                for (int itemIndex = 0; itemIndex < this.getItemCount(seriesGroupIndex, seriesIndex); ++itemIndex) {
                    com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration markerConf;
                    this.chartBindingMap.setActiveItem(this.plotId, seriesGroupId, seriesId, itemIndex);
                    try {
                        ItemStyleConfiguration itemStyleConf = ItemStyleHelper.storeActualItemStyleConfiguration(traceableSeriesGroup, seriesGroupConf, rendererConf, this.chartBindingMap, seriesIndex, itemIndex);
                        this.initRenderer(seriesIndex, itemIndex, rendererId, itemStyleConf);
                    }
                    catch (SapphireException e) {
                        this.logger.error("Error while handling item style for series: " + seriesId, e);
                    }
                    SeriesConfiguration seriesConf = seriesGroupConf.getSeriesConfiguration(seriesId);
                    ItemConfiguration itemConf = seriesConf.getItemConfiguration();
                    for (MarkerConfiguration itemMarkerConf : itemConf.getRangeMarkerConfigurationList()) {
                        String rangeMarkerId = itemMarkerConf.getMarkerId();
                        try {
                            if (!itemMarkerConf.isVisible().evaluate(this.chartBindingMap).booleanValue() || !(markerConf = this.componentPlotConf.getRangeMarkerConfiguration(rangeMarkerId)).isVisible().evaluate(this.chartBindingMap).booleanValue()) continue;
                            this.addRangeMarker(seriesGroupIndex, markerConf);
                        }
                        catch (SapphireException e) {
                            this.logger.error("Error while adding item range marker: " + rangeMarkerId, e);
                        }
                    }
                    for (MarkerConfiguration itemMarkerConf : itemConf.getDomainMarkerConfigurationList()) {
                        String domainMarkerId = itemMarkerConf.getMarkerId();
                        try {
                            if (!itemMarkerConf.isVisible().evaluate(this.chartBindingMap).booleanValue() || !(markerConf = this.componentPlotConf.getDomainMarkerConfiguration(domainMarkerId)).isVisible().evaluate(this.chartBindingMap).booleanValue()) continue;
                            this.addDomainMarker(seriesGroupIndex, markerConf);
                        }
                        catch (SapphireException e) {
                            this.logger.error("Error while adding item domain marker: " + domainMarkerId, e);
                        }
                    }
                }
            }
            for (String rangeMarkerId : seriesGroupConf.getRangeMarkerIdList()) {
                com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration markerConf = this.componentPlotConf.getRangeMarkerConfiguration(rangeMarkerId);
                try {
                    if (!markerConf.isVisible().evaluate(this.chartBindingMap).booleanValue()) continue;
                    this.addRangeMarker(seriesGroupIndex, markerConf);
                }
                catch (SapphireException e) {
                    this.logger.error("Error while adding series range marker: " + rangeMarkerId, e);
                }
            }
            for (String domainMarkerId : seriesGroupConf.getDomainMarkerIdList()) {
                com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration markerConf = this.componentPlotConf.getDomainMarkerConfiguration(domainMarkerId);
                try {
                    if (!markerConf.isVisible().evaluate(this.chartBindingMap).booleanValue()) continue;
                    this.addDomainMarker(seriesGroupIndex, markerConf);
                }
                catch (SapphireException e) {
                    this.logger.error("Error while adding series domain marker: " + domainMarkerId, e);
                }
            }
        }
        this.chartBindingMap.resetChartBindingMap();
    }

    protected abstract void initRenderer(int var1, int var2, String var3, ItemStyleConfiguration var4);

    protected abstract void addRangeMarker(int var1, com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration var2) throws SapphireException;

    protected abstract void addDomainMarker(int var1, com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration var2) throws SapphireException;

    protected abstract int getItemCount(int var1, int var2);

    protected abstract int getSeriesCount(int var1);

    private int handleDomainAxis(int domainAxisIndex, int seriesGroupIndex, String domainAxisId) {
        if (!this.hasCachedDomainAxis(domainAxisId)) {
            AxisConfiguration axisConf = this.componentPlotConf.getDomainAxisConfiguration(domainAxisId);
            this.cacheDomainAxis(domainAxisId, axisConf);
            this.setPlotDomainAxisFromCache(domainAxisIndex, domainAxisId);
            AxisLocation axisLocation = this.componentPlotConf.getDomainAxisLocation(domainAxisId);
            if (axisLocation != null) {
                this.setDomainAxisLocation(domainAxisIndex, axisLocation);
            }
            ++domainAxisIndex;
        }
        this.mapDatasetToDomainAxis(seriesGroupIndex, domainAxisId);
        return domainAxisIndex;
    }

    private int handleRangeAxis(int rangeAxisIndex, int seriesGroupIndex, String rangeAxisId) {
        if (!this.hasCachedRangeAxis(rangeAxisId)) {
            AxisConfiguration axisConf = this.componentPlotConf.getRangeAxisConfiguration(rangeAxisId);
            this.cacheRangeAxis(rangeAxisId, axisConf);
            this.setPlotRangeAxisFromCache(rangeAxisIndex, rangeAxisId);
            AxisLocation axisLocation = this.componentPlotConf.getRangeAxisLocation(rangeAxisId);
            if (axisLocation != null) {
                this.setRangeAxisLocation(rangeAxisIndex, axisLocation);
            } else if (this.honorRTLmode && this.getConnectionProcessor().getSapphireConnection().isRtl()) {
                this.setRangeAxisLocation(rangeAxisIndex, AxisLocation.BOTTOM_OR_RIGHT);
            }
            ++rangeAxisIndex;
        }
        this.mapDatasetToRangeAxis(seriesGroupIndex, rangeAxisId);
        return rangeAxisIndex;
    }

    protected Marker createMarker(com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration markerConf) throws SapphireException {
        ValueMarker returnMarker;
        if (markerConf == null) {
            throw new IllegalArgumentException("Marker configuration is null");
        }
        MarkerConfiguration.MarkerType markerType = markerConf.getMarkerType();
        if (markerType == MarkerConfiguration.MarkerType.VALUE) {
            returnMarker = this.createValueMarker(markerConf.getValueMarkerConfiguration());
        } else if (markerType == MarkerConfiguration.MarkerType.CATEGORY) {
            returnMarker = this.createCategoryMarker(markerConf.getCategoryMarkerConfiguration());
        } else if (markerType == MarkerConfiguration.MarkerType.INTERVAL) {
            returnMarker = this.createIntervalMarker(markerConf.getIntervalMarkerConfiguration());
        } else {
            throw new IllegalArgumentException("Unknown marker type: " + (Object)((Object)markerType));
        }
        this.buildMarker((Marker)returnMarker, markerConf);
        return returnMarker;
    }

    private IntervalMarker createIntervalMarker(IntervalMarkerConfiguration intervalMarkerConf) throws SapphireException {
        return new IntervalMarker(intervalMarkerConf.getStart().evaluate(this.chartBindingMap).doubleValue(), intervalMarkerConf.getEnd().evaluate(this.chartBindingMap).doubleValue());
    }

    private CategoryMarker createCategoryMarker(CategoryMarkerConfiguration categoryMarkerConf) throws SapphireException {
        return new CategoryMarker((Comparable)((Object)categoryMarkerConf.getCategory().evaluate(this.chartBindingMap)));
    }

    private ValueMarker createValueMarker(ValueMarkerConfiguration valueMarkerConf) throws SapphireException {
        return new ValueMarker(valueMarkerConf.getValue().evaluate(this.chartBindingMap).doubleValue());
    }

    protected CategoryMarker createCategoryMarker(com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration markerConf) throws SapphireException {
        if (markerConf == null) {
            throw new IllegalArgumentException("Marker configuration is null");
        }
        Marker marker = this.createMarker(markerConf);
        if (!(marker instanceof CategoryMarker)) {
            throw new IllegalArgumentException("Marker must be category type has type: " + (Object)((Object)markerConf.getMarkerType()));
        }
        return (CategoryMarker)marker;
    }

    private void buildMarker(Marker marker, com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration markerConf) throws SapphireException {
        marker.setLabel(markerConf.getLabel().evaluate(this.chartBindingMap));
        marker.setLabelAnchor(markerConf.getLabelAnchor().getRectangleAnchor());
        marker.setLabelTextAnchor(markerConf.getLabelTextAnchor().getTextAnchor());
        marker.setLabelFont(markerConf.getLabelFontConfiguration().getFont(DEFAULT_MARKER_LABEL_FONT, DEFAULT_MARKER_LABEL_COLOR));
        if (markerConf.getLabelPaintConfiguration().hasPaint()) {
            marker.setLabelPaint(markerConf.getLabelPaintConfiguration().getPaint());
        }
        if (markerConf.getPaintConfiguration().hasPaint()) {
            marker.setPaint(markerConf.getPaintConfiguration().getPaint());
        }
        if (markerConf.getStrokeConfiguration().hasStroke()) {
            marker.setStroke(markerConf.getStrokeConfiguration().getStroke());
        }
        marker.setAlpha(markerConf.getAlpha());
    }

    protected abstract void setRangeAxisLocation(int var1, AxisLocation var2);

    protected abstract void setDomainAxisLocation(int var1, AxisLocation var2);

    protected abstract void mapDatasetToRangeAxis(int var1, String var2);

    protected abstract void mapDatasetToDomainAxis(int var1, String var2);

    protected abstract void setPlotRangeAxisFromCache(int var1, String var2);

    protected abstract void setPlotDomainAxisFromCache(int var1, String var2);

    protected abstract void cacheRangeAxis(String var1, AxisConfiguration var2);

    protected abstract void cacheDomainAxis(String var1, AxisConfiguration var2);

    protected abstract boolean hasCachedRangeAxis(String var1);

    protected abstract boolean hasCachedDomainAxis(String var1);

    protected abstract void setPlotRendererFromCache(int var1, String var2);

    protected abstract void cacheRenderer(String var1, RendererConfiguration var2, TraceableSeriesGroup var3, SeriesGroupConfiguration var4);

    protected abstract boolean hasCachedRenderer(String var1);

    protected abstract void setPlotDataset(int var1, Dataset var2);
}

