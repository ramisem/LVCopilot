/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.NumberAxis
 *  org.jfree.chart.axis.ValueAxis
 *  org.jfree.chart.labels.CategoryItemLabelGenerator
 *  org.jfree.chart.labels.StandardCategoryItemLabelGenerator
 *  org.jfree.chart.renderer.category.LineAndShapeRenderer
 */
package com.labvantage.sapphire.pageelements.datachart.chart.renderer.category;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.chart.ConfigurableCategoryItemLabelGenerator;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
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
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;

public class ConfigurableLineAndShapeRenderer
extends LineAndShapeRenderer
implements Serializable {
    private final RendererConfiguration rendererConf;
    private final SeriesGroupConfiguration seriesGroupConf;
    private final TraceableSeriesGroup traceableSeriesGroup;
    private final ChartBindingMap chartBindingMap;
    private final ConnectionInfo connectionInfo;
    private final Map<String, ValueAxis> rangeAxisMap;

    public ConfigurableLineAndShapeRenderer(RendererConfiguration rendererConf, SeriesGroupConfiguration seriesGroupConf, TraceableSeriesGroup traceableSeriesGroup, ConnectionInfo connectionInfo, ChartBindingMap chartBindingMap, Map<String, ValueAxis> rangeAxisMap) {
        if (rendererConf == null) {
            throw new IllegalArgumentException("Renderer configuration is null");
        }
        if (seriesGroupConf == null) {
            throw new IllegalArgumentException("Series group configuration is null");
        }
        if (traceableSeriesGroup == null) {
            throw new IllegalArgumentException("Traceable series group is null");
        }
        this.rendererConf = rendererConf;
        this.seriesGroupConf = seriesGroupConf;
        this.traceableSeriesGroup = traceableSeriesGroup;
        this.connectionInfo = connectionInfo;
        this.chartBindingMap = chartBindingMap;
        this.rangeAxisMap = rangeAxisMap;
    }

    public boolean getItemShapeFilled(int seriesIndex, int itemIndex) {
        boolean returnValue = super.getItemShapeFilled(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null) {
            returnValue = rendererItemStyleConf.getItemCategoryItemRendererConfiguration().getLineAndShapeConfiguration().isShapesFilled();
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
            returnValue = rendererItemStyleConf.getItemCategoryItemRendererConfiguration().getLineAndShapeConfiguration().isShapesVisible();
        }
        return returnValue;
    }

    public CategoryItemLabelGenerator getItemLabelGenerator(int seriesIndex, int itemIndex) {
        CategoryItemLabelGenerator returnValue = super.getItemLabelGenerator(seriesIndex, itemIndex);
        ItemStyleConfiguration rendererItemStyleConf = ItemStyleHelper.getActualItemStyleConfiguration(this.traceableSeriesGroup, this.seriesGroupConf, this.rendererConf, seriesIndex, itemIndex);
        if (rendererItemStyleConf != null && rendererItemStyleConf.getItemLabelConfiguration().getLabelsVisible() != null) {
            if (!rendererItemStyleConf.getItemLabelConfiguration().getLabelsVisible().booleanValue()) {
                returnValue = null;
            } else if (rendererItemStyleConf.getItemLabelConfiguration().getLabelType().equals("Standard")) {
                ValueAxis yAxis;
                StandardItemLabelConfiguration standardItemLabelConfiguration = rendererItemStyleConf.getItemLabelConfiguration().getStandardItemLabelConfiguration();
                try {
                    yAxis = this.rangeAxisMap.get(this.seriesGroupConf.getRangeAxisId().evaluate(this.chartBindingMap));
                }
                catch (SapphireException e) {
                    throw new IllegalArgumentException("Error while evaluating range axis ID expression: " + this.seriesGroupConf.getRangeAxisId().getExpression(), e);
                }
                NumberFormat yNumberFormat = NumberFormat.getNumberInstance();
                NumberFormat yPercentFormat = NumberFormat.getPercentInstance();
                if (yAxis instanceof NumberAxis) {
                    if (!standardItemLabelConfiguration.getNumberFormatStr().isEmpty()) {
                        yNumberFormat = new DecimalFormat(standardItemLabelConfiguration.getNumberFormatStr());
                    } else {
                        yNumberFormat = ((NumberAxis)yAxis).getNumberFormatOverride();
                        if (yNumberFormat == null) {
                            yNumberFormat = NumberFormat.getNumberInstance(I18nUtil.getConnectionLocale(this.connectionInfo));
                        }
                    }
                    if (!standardItemLabelConfiguration.getNumberFormatStr().isEmpty()) {
                        yPercentFormat = new DecimalFormat(standardItemLabelConfiguration.getPercentageNumberFormatStr());
                    } else {
                        yPercentFormat = ((NumberAxis)yAxis).getNumberFormatOverride();
                        if (yPercentFormat == null) {
                            yPercentFormat = NumberFormat.getPercentInstance(I18nUtil.getConnectionLocale(this.connectionInfo));
                        }
                    }
                }
                returnValue = new StandardCategoryItemLabelGenerator(rendererItemStyleConf.getItemLabelConfiguration().getStandardItemLabelConfiguration().getLabelString(), yNumberFormat, yPercentFormat);
            } else if (rendererItemStyleConf.getItemLabelConfiguration().getLabelType().equals("Expression")) {
                String labelStr = rendererItemStyleConf.getItemLabelConfiguration().getConfigurableLabelConfiguration().getLabelString();
                returnValue = new ConfigurableCategoryItemLabelGenerator(labelStr, this.chartBindingMap, this.traceableSeriesGroup);
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
}

