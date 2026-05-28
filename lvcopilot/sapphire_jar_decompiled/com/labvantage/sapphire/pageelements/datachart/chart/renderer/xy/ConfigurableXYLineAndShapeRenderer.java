/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.LegendItem
 *  org.jfree.chart.axis.DateAxis
 *  org.jfree.chart.axis.NumberAxis
 *  org.jfree.chart.axis.ValueAxis
 *  org.jfree.chart.labels.StandardXYItemLabelGenerator
 *  org.jfree.chart.labels.XYItemLabelGenerator
 *  org.jfree.chart.plot.XYPlot
 *  org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
 *  org.jfree.data.general.Dataset
 *  org.jfree.data.xy.XYDataset
 */
package com.labvantage.sapphire.pageelements.datachart.chart.renderer.xy;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.chart.ConfigurableXYItemLabelGenerator;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ShapeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StandardItemLabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleHelper;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;

public class ConfigurableXYLineAndShapeRenderer
extends XYLineAndShapeRenderer
implements Serializable {
    private final RendererConfiguration rendererConf;
    private final SeriesGroupConfiguration seriesGroupConf;
    private final TraceableSeriesGroup traceableSeriesGroup;
    private final ConnectionInfo connectionInfo;
    private final ChartBindingMap chartBindingMap;
    private final Map<String, ValueAxis> rangeAxisMap;
    private final Map<String, ValueAxis> domainAxisMap;

    public ConfigurableXYLineAndShapeRenderer(RendererConfiguration rendererConf, SeriesGroupConfiguration seriesGroupConf, TraceableSeriesGroup traceableSeriesGroup, ConnectionInfo connectionInfo, Map<String, ValueAxis> rangeAxisMap, Map<String, ValueAxis> domainAxisMap, ChartBindingMap chartBindingMap) {
        if (rendererConf == null) {
            throw new IllegalArgumentException("Renderer configuration is null");
        }
        if (seriesGroupConf == null) {
            throw new IllegalArgumentException("Series group configuration is null");
        }
        if (traceableSeriesGroup == null) {
            throw new IllegalArgumentException("Traceable series group is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        if (rangeAxisMap == null) {
            throw new IllegalArgumentException("Range axis map is null");
        }
        if (domainAxisMap == null) {
            throw new IllegalArgumentException("Domain axis map is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        this.rendererConf = rendererConf;
        this.seriesGroupConf = seriesGroupConf;
        this.traceableSeriesGroup = traceableSeriesGroup;
        this.connectionInfo = connectionInfo;
        this.rangeAxisMap = rangeAxisMap;
        this.domainAxisMap = domainAxisMap;
        this.chartBindingMap = chartBindingMap;
    }

    public boolean getItemShapeFilled(int seriesIndex, int itemIndex) {
        boolean returnValue = super.getItemShapeFilled(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null) {
            returnValue = rendererItemStyleConf.getItemXYItemRendererConfiguration().getXYLineAndShapeConfiguration().isShapesFilled();
        }
        return returnValue;
    }

    public Paint getItemPaint(int seriesIndex, int itemIndex) {
        PaintConfiguration paintConf;
        Paint returnValue = super.getItemPaint(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null && (paintConf = rendererItemStyleConf.getItemPaintConfiguration()).hasPaint()) {
            returnValue = paintConf.getPaint();
        }
        return returnValue;
    }

    public Paint getItemOutlinePaint(int seriesIndex, int itemIndex) {
        PaintConfiguration paintConf;
        Paint returnValue = super.getItemOutlinePaint(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null && (paintConf = rendererItemStyleConf.getItemOutlinePaintConfiguration()).hasPaint()) {
            returnValue = paintConf.getPaint();
        }
        return returnValue;
    }

    public Paint getItemFillPaint(int seriesIndex, int itemIndex) {
        PaintConfiguration paintConf;
        Paint returnValue = super.getItemFillPaint(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null && (paintConf = rendererItemStyleConf.getItemFillPaintConfiguration()).hasPaint()) {
            returnValue = paintConf.getPaint();
        }
        return returnValue;
    }

    public Shape getItemShape(int seriesIndex, int itemIndex) {
        ShapeConfiguration shapeConf;
        Shape returnValue = super.getItemShape(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null && (shapeConf = rendererItemStyleConf.getItemShapeConfiguration()).hasShape()) {
            returnValue = shapeConf.getShape();
        }
        return returnValue;
    }

    public boolean getItemShapeVisible(int seriesIndex, int itemIndex) {
        boolean returnValue = super.getItemShapeVisible(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null) {
            returnValue = rendererItemStyleConf.getItemXYItemRendererConfiguration().getXYLineAndShapeConfiguration().isShapesVisible();
        }
        return returnValue;
    }

    public XYItemLabelGenerator getItemLabelGenerator(int seriesIndex, int itemIndex) {
        XYItemLabelGenerator returnValue = super.getItemLabelGenerator(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null && rendererItemStyleConf.getItemLabelConfiguration().getLabelsVisible() != null) {
            if (!rendererItemStyleConf.getItemLabelConfiguration().getLabelsVisible().booleanValue()) {
                returnValue = null;
            } else {
                LabelConfiguration labelConf = rendererItemStyleConf.getItemLabelConfiguration();
                if (labelConf.getLabelType().equals("Standard")) {
                    ValueAxis xAxis;
                    ValueAxis yAxis;
                    StandardItemLabelConfiguration standardItemLabelConfiguration = labelConf.getStandardItemLabelConfiguration();
                    try {
                        yAxis = this.rangeAxisMap.get(this.seriesGroupConf.getRangeAxisId().evaluate(this.chartBindingMap));
                    }
                    catch (SapphireException e) {
                        throw new IllegalArgumentException("Error while evaluating range axis ID expression: " + this.seriesGroupConf.getRangeAxisId().getExpression(), e);
                    }
                    NumberFormat yNumberFormat = null;
                    NumberFormat xNumberFormat = null;
                    DateFormat yDateFormat = null;
                    DateFormat xDateFormat = null;
                    if (yAxis instanceof DateAxis) {
                        yDateFormat = ((DateAxis)yAxis).getDateFormatOverride();
                        if (yDateFormat == null) {
                            yDateFormat = DateFormat.getDateInstance(2, I18nUtil.getConnectionLocale(this.connectionInfo));
                        }
                    } else if (yAxis instanceof NumberAxis) {
                        if (!standardItemLabelConfiguration.getNumberFormatStr().isEmpty()) {
                            yNumberFormat = new DecimalFormat(standardItemLabelConfiguration.getNumberFormatStr());
                        } else {
                            yNumberFormat = ((NumberAxis)yAxis).getNumberFormatOverride();
                            if (yNumberFormat == null) {
                                yNumberFormat = NumberFormat.getNumberInstance(I18nUtil.getConnectionLocale(this.connectionInfo));
                            }
                        }
                    }
                    try {
                        xAxis = this.domainAxisMap.get(this.seriesGroupConf.getDomainAxisId().evaluate(this.chartBindingMap));
                    }
                    catch (SapphireException e) {
                        throw new IllegalArgumentException("Error while evaluating domain axis ID expression: " + this.seriesGroupConf.getDomainAxisId().getExpression(), e);
                    }
                    if (xAxis instanceof DateAxis) {
                        xDateFormat = ((DateAxis)xAxis).getDateFormatOverride();
                        if (xDateFormat == null) {
                            xDateFormat = DateFormat.getDateInstance(2, I18nUtil.getConnectionLocale(this.connectionInfo));
                        }
                    } else if (xAxis instanceof NumberAxis) {
                        if (!standardItemLabelConfiguration.getNumberFormatStr().isEmpty()) {
                            xNumberFormat = new DecimalFormat(standardItemLabelConfiguration.getNumberFormatStr());
                        } else {
                            xNumberFormat = ((NumberAxis)xAxis).getNumberFormatOverride();
                            if (xNumberFormat == null) {
                                xNumberFormat = NumberFormat.getNumberInstance(I18nUtil.getConnectionLocale(this.connectionInfo));
                            }
                        }
                    }
                    String labelStr = labelConf.getStandardItemLabelConfiguration().getLabelString();
                    returnValue = xNumberFormat != null && yNumberFormat != null ? new StandardXYItemLabelGenerator(labelStr, xNumberFormat, yNumberFormat) : (xNumberFormat != null && yDateFormat != null ? new StandardXYItemLabelGenerator(labelStr, xNumberFormat, yDateFormat) : (xDateFormat != null && yDateFormat != null ? new StandardXYItemLabelGenerator(labelStr, xDateFormat, yDateFormat) : (xDateFormat != null && yNumberFormat != null ? new StandardXYItemLabelGenerator(labelStr, xDateFormat, yNumberFormat) : new StandardXYItemLabelGenerator())));
                } else if (labelConf.getLabelType().equals("Expression")) {
                    String labelStr = labelConf.getConfigurableLabelConfiguration().getLabelString();
                    returnValue = new ConfigurableXYItemLabelGenerator(labelStr, this.chartBindingMap, this.traceableSeriesGroup);
                }
            }
        }
        return returnValue;
    }

    public boolean isItemLabelVisible(int seriesIndex, int itemIndex) {
        boolean returnValue = super.isItemLabelVisible(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null && rendererItemStyleConf.getItemLabelConfiguration().getLabelsVisible() != null) {
            returnValue = rendererItemStyleConf.getItemLabelConfiguration().getLabelsVisible();
        }
        return returnValue;
    }

    public Font getItemLabelFont(int seriesIndex, int itemIndex) {
        Font returnValue = super.getItemLabelFont(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null) {
            Paint defaultPaint = this.getSeriesItemLabelPaint(seriesIndex);
            if (defaultPaint == null) {
                defaultPaint = this.getBaseItemLabelPaint();
            }
            returnValue = rendererItemStyleConf.getItemLabelConfiguration().getFontConf().getFont(this.getSeriesItemLabelFont(seriesIndex), defaultPaint);
        }
        return returnValue;
    }

    public LegendItem getLegendItem(int datasetIndex, int series) {
        XYPlot plot = this.getPlot();
        if (plot == null) {
            return null;
        }
        LegendItem result = null;
        XYDataset dataset = plot.getDataset(datasetIndex);
        if (dataset != null) {
            for (int i = 0; i < dataset.getItemCount(series); ++i) {
                ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, series, i);
                if (!this.getItemVisible(series, i) || rendererItemStyleConf != null) continue;
                String label = this.getLegendItemLabelGenerator().generateLabel(dataset, series);
                String toolTipText = null;
                if (this.getLegendItemToolTipGenerator() != null) {
                    toolTipText = this.getLegendItemToolTipGenerator().generateLabel(dataset, series);
                }
                String urlText = null;
                if (this.getLegendItemURLGenerator() != null) {
                    urlText = this.getLegendItemURLGenerator().generateLabel(dataset, series);
                }
                boolean shapeIsVisible = this.getItemShapeVisible(series, i);
                Shape shape = this.lookupLegendShape(series);
                boolean shapeIsFilled = this.getItemShapeFilled(series, i);
                Paint fillPaint = this.getUseFillPaint() ? this.lookupSeriesFillPaint(series) : this.lookupSeriesPaint(series);
                boolean shapeOutlineVisible = this.getDrawOutlines();
                Paint outlinePaint = this.getUseOutlinePaint() ? this.lookupSeriesOutlinePaint(series) : this.lookupSeriesPaint(series);
                Stroke outlineStroke = this.lookupSeriesOutlineStroke(series);
                boolean lineVisible = this.getItemLineVisible(series, i);
                Stroke lineStroke = this.lookupSeriesStroke(series);
                Paint linePaint = this.lookupSeriesPaint(series);
                result = new LegendItem(label, label, toolTipText, urlText, shapeIsVisible, shape, shapeIsFilled, fillPaint, shapeOutlineVisible, outlinePaint, outlineStroke, lineVisible, this.getLegendLine(), lineStroke, linePaint);
                result.setLabelFont(this.lookupLegendTextFont(series));
                Paint labelPaint = this.lookupLegendTextPaint(series);
                if (labelPaint != null) {
                    result.setLabelPaint(labelPaint);
                }
                result.setSeriesKey(dataset.getSeriesKey(series));
                result.setSeriesIndex(series);
                result.setDataset((Dataset)dataset);
                result.setDatasetIndex(datasetIndex);
            }
        }
        return result;
    }
}

