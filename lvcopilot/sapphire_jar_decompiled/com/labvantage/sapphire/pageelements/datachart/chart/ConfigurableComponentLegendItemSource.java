/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.LegendItem
 *  org.jfree.chart.LegendItemCollection
 *  org.jfree.chart.LegendItemSource
 *  org.jfree.chart.renderer.AbstractRenderer
 *  org.jfree.chart.renderer.category.BarRenderer
 *  org.jfree.chart.renderer.xy.XYBarRenderer
 *  org.jfree.data.general.Dataset
 */
package com.labvantage.sapphire.pageelements.datachart.chart;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.chart.MutableLegendItem;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.LineAndShapeRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.SeriesStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYLineAndShapeRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.general.Dataset;

public class ConfigurableComponentLegendItemSource
extends BaseCustom
implements LegendItemSource,
Serializable {
    private final LegendItemSource legendItemSource;
    private final ComponentPlotConfiguration componentPlotConf;
    private final Data data;
    private final ChartBindingMap chartBindingMap;

    public ConfigurableComponentLegendItemSource(LegendItemSource legendItemSource, ComponentPlotConfiguration componentPlotConf, Data data, ChartBindingMap chartBindingMap, String connectionId) {
        if (legendItemSource == null) {
            throw new IllegalArgumentException("Legend item source is null");
        }
        if (componentPlotConf == null) {
            throw new IllegalArgumentException("Component plot configuration is null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        if (connectionId == null) {
            throw new IllegalArgumentException("Connection ID is null");
        }
        this.legendItemSource = legendItemSource;
        this.componentPlotConf = componentPlotConf;
        this.data = data;
        this.chartBindingMap = chartBindingMap;
        this.setConnectionId(connectionId);
    }

    public LegendItemCollection getLegendItems() {
        LegendItemCollection filteredLegendItemCollection = new LegendItemCollection();
        LegendItemCollection legendItemCollection = this.legendItemSource.getLegendItems();
        this.filterOutLegendItems(filteredLegendItemCollection, legendItemCollection);
        for (RendererConfiguration rendererConf : this.componentPlotConf.getRendererConfList()) {
            if (rendererConf.getRendererId().isEmpty()) continue;
            for (SeriesStyleConfiguration seriesStyleConf : rendererConf.getSeriesStyleConfList()) {
                if (seriesStyleConf.getSeriesStyleId().isEmpty()) continue;
                Map<List<String>, com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration> actualItemStyleConfMap = seriesStyleConf.getActualItemStyleConfMap();
                Set<List<String>> actualItemStyleIdListSet = actualItemStyleConfMap.keySet();
                for (List<String> actualItemStyleIdList : actualItemStyleIdListSet) {
                    LegendItem legendItem;
                    com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration actualItemStyleConf = actualItemStyleConfMap.get(actualItemStyleIdList);
                    String legendItemLabel = this.createLegendItemLabel(rendererConf, actualItemStyleIdList);
                    if (legendItemLabel.isEmpty() || (legendItem = this.getLegendItem(rendererConf, seriesStyleConf, actualItemStyleConf, legendItemLabel)) == null) continue;
                    filteredLegendItemCollection.add(legendItem);
                }
            }
        }
        return filteredLegendItemCollection;
    }

    private LegendItem getLegendItem(RendererConfiguration rendererConf, SeriesStyleConfiguration seriesStyleConf, com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration actualItemStyleConf, String legendItemLabel) {
        LegendItem legendItem = null;
        if (this.componentPlotConf.getParent().getPlotType() == PlotConfiguration.PlotType.XY_PLOT) {
            if (rendererConf.getRendererType() == RendererConfiguration.RendererType.XY_LINE_AND_SHAPE) {
                legendItem = this.createXYLineAndShapeLegendItem(rendererConf, seriesStyleConf, actualItemStyleConf, legendItemLabel);
            } else if (rendererConf.getRendererType() == RendererConfiguration.RendererType.XY_BAR) {
                legendItem = this.createXYBarLegendItem(rendererConf, seriesStyleConf, actualItemStyleConf, legendItemLabel);
            }
        } else if (this.componentPlotConf.getParent().getPlotType() == PlotConfiguration.PlotType.CATEGORY_PLOT) {
            if (rendererConf.getRendererType() == RendererConfiguration.RendererType.LINE_AND_SHAPE) {
                legendItem = this.createCategoryLineAndShapeLegendItem(rendererConf, seriesStyleConf, actualItemStyleConf, legendItemLabel);
            } else if (rendererConf.getRendererType() == RendererConfiguration.RendererType.BAR) {
                legendItem = this.createCategoryBarLegendItem(rendererConf, seriesStyleConf, actualItemStyleConf, legendItemLabel);
            }
        }
        return legendItem;
    }

    private String createLegendItemLabel(RendererConfiguration rendererConf, List<String> actualItemStyleIdList) {
        StringBuilder completeItemStyleLegendLabel = new StringBuilder();
        if (actualItemStyleIdList.size() > 0) {
            String seriesStyleId = actualItemStyleIdList.get(0);
            for (int i = 1; i < actualItemStyleIdList.size(); ++i) {
                String actualItemStyleId = actualItemStyleIdList.get(i);
                String singleItemStyleLegendLabel = "";
                for (SeriesGroupConfiguration seriesGroupConf : this.componentPlotConf.getSeriesGroupConfList()) {
                    if (!seriesGroupConf.getRendererId().equals(rendererConf.getRendererId())) continue;
                    for (SeriesConfiguration seriesConf : seriesGroupConf.getSeriesConfList()) {
                        if (!seriesConf.getSeriesStyleId().equals(seriesStyleId)) continue;
                        for (ItemStyleConfiguration itemStyleConf : seriesConf.getItemConfiguration().getItemStyleConfigurationList()) {
                            if (!itemStyleConf.getItemStyleId().equals(actualItemStyleId) || !itemStyleConf.showLegendItem() || !(singleItemStyleLegendLabel = itemStyleConf.getLegendLabel()).isEmpty()) continue;
                            singleItemStyleLegendLabel = itemStyleConf.getItemStyleId();
                        }
                    }
                }
                if (singleItemStyleLegendLabel.isEmpty()) continue;
                completeItemStyleLegendLabel.append(", ").append(singleItemStyleLegendLabel);
            }
        }
        if (completeItemStyleLegendLabel.toString().isEmpty()) {
            return "";
        }
        return completeItemStyleLegendLabel.substring(2);
    }

    private void filterOutLegendItems(LegendItemCollection filteredLegendItemCollection, LegendItemCollection legendItemCollection) {
        for (LegendItem legendItem : legendItemCollection) {
            SeriesConfiguration seriesConf;
            Dataset seriesGroup = legendItem.getDataset();
            TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup(seriesGroup);
            String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
            String seriesId = (String)((Object)legendItem.getSeriesKey());
            SeriesGroupConfiguration seriesGroupConf = this.componentPlotConf.getSeriesGroupConfiguration(seriesGroupId);
            if (!seriesGroupConf.showLegendItems() || !(seriesConf = seriesGroupConf.getSeriesConfiguration(seriesId)).showLegendItem()) continue;
            String seriesLegendLabel = seriesConf.getLegendLabel().evaluateNoException(this.chartBindingMap);
            if (seriesLegendLabel.isEmpty()) {
                if (seriesConf.translateLegendLabel()) {
                    filteredLegendItemCollection.add((LegendItem)new MutableLegendItem(this.getTranslationProcessor().translate(legendItem.getLabel()), legendItem));
                    continue;
                }
                filteredLegendItemCollection.add(legendItem);
                continue;
            }
            MutableLegendItem mutableLegendItem = new MutableLegendItem(seriesLegendLabel, legendItem);
            if (seriesConf.translateLegendLabel()) {
                mutableLegendItem.setLabel(this.getTranslationProcessor().translate(mutableLegendItem.getLabel()));
            }
            filteredLegendItemCollection.add((LegendItem)mutableLegendItem);
        }
    }

    private LegendItem createXYBarLegendItem(RendererConfiguration rendererConf, SeriesStyleConfiguration seriesStyleConf, com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration actualItemStyleConf, String label) {
        String description = null;
        String toolTipText = null;
        String urlText = null;
        Rectangle2D.Double shape = new Rectangle2D.Double(-3.0, -5.0, 6.0, 10.0);
        Paint paint = seriesStyleConf.getSeriesPaintConfiguration().getOrSetPaint(rendererConf.getBasePaintConfiguration().getOrSetPaint(XYBarRenderer.DEFAULT_PAINT));
        Paint outlinePaint = actualItemStyleConf.getItemOutlinePaintConfiguration().getOrSetPaint(seriesStyleConf.getSeriesOutlinePaintConfiguration().getOrSetPaint(rendererConf.getBaseOutlinePaintConf().getOrSetPaint(AbstractRenderer.DEFAULT_OUTLINE_PAINT)));
        BasicStroke outlineStroke = new BasicStroke(1.0f);
        LegendItem legendItem = new LegendItem(label, description, toolTipText, urlText, (Shape)shape, paint, (Stroke)outlineStroke, outlinePaint);
        return legendItem;
    }

    private LegendItem createXYLineAndShapeLegendItem(RendererConfiguration rendererConf, SeriesStyleConfiguration seriesStyleConf, com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration actualItemStyleConf, String label) {
        String description = null;
        String toolTipText = null;
        String urlText = null;
        XYLineAndShapeRendererConfiguration xyLineAndShapeRendererConf = actualItemStyleConf.getItemXYItemRendererConfiguration().getXYLineAndShapeConfiguration();
        boolean shapeVisible = xyLineAndShapeRendererConf.isShapesVisible();
        boolean shapeFilled = xyLineAndShapeRendererConf.isShapesFilled();
        boolean lineVisible = xyLineAndShapeRendererConf.isLinesVisible();
        Shape shape = actualItemStyleConf.getItemShapeConfiguration().hasShape() ? actualItemStyleConf.getItemShapeConfiguration().getShape() : (seriesStyleConf.getSeriesShapeConfiguration().hasShape() ? seriesStyleConf.getSeriesShapeConfiguration().getShape() : (rendererConf.getBaseShapeConfiguration().hasShape() ? rendererConf.getBaseShapeConfiguration().getShape() : AbstractRenderer.DEFAULT_SHAPE));
        Paint fillPaint = actualItemStyleConf.getItemFillPaintConfiguration().hasPaint() ? actualItemStyleConf.getItemFillPaintConfiguration().getPaint() : (seriesStyleConf.getSeriesPaintConfiguration().hasPaint() ? seriesStyleConf.getSeriesPaintConfiguration().getPaint() : (rendererConf.getBasePaintConfiguration().hasPaint() ? rendererConf.getBasePaintConfiguration().getPaint() : AbstractRenderer.DEFAULT_PAINT));
        boolean shapeOutLineVisible = true;
        Paint outLinePaint = rendererConf.getBaseXYItemRendererConfiguration().getXYLineAndShapeConfiguration().isUseOutlinePaint() ? (actualItemStyleConf.getItemOutlinePaintConfiguration().hasPaint() ? actualItemStyleConf.getItemOutlinePaintConfiguration().getPaint() : (seriesStyleConf.getSeriesPaintConfiguration().hasPaint() ? seriesStyleConf.getSeriesPaintConfiguration().getPaint() : (rendererConf.getBasePaintConfiguration().hasPaint() ? rendererConf.getBasePaintConfiguration().getPaint() : AbstractRenderer.DEFAULT_OUTLINE_PAINT))) : fillPaint;
        BasicStroke outLineStroke = new BasicStroke(1.0f);
        Line2D.Double line = new Line2D.Double(-7.0, 0.0, 7.0, 0.0);
        Stroke lineStroke = seriesStyleConf.getSeriesStrokeConfiguration().getStroke(rendererConf.getBaseStrokeConf().getStroke());
        Paint linePaint = seriesStyleConf.getSeriesPaintConfiguration().getOrSetPaint(rendererConf.getBasePaintConfiguration().getOrSetPaint(AbstractRenderer.DEFAULT_PAINT));
        LegendItem legendItem = new LegendItem(label, description, toolTipText, urlText, shapeVisible, shape, shapeFilled, fillPaint, shapeOutLineVisible, outLinePaint, (Stroke)outLineStroke, lineVisible, (Shape)line, lineStroke, linePaint);
        return legendItem;
    }

    private LegendItem createCategoryLineAndShapeLegendItem(RendererConfiguration rendererConf, SeriesStyleConfiguration seriesStyleConf, com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration actualItemStyleConf, String label) {
        String description = null;
        String toolTipText = null;
        String urlText = null;
        LineAndShapeRendererConfiguration lineAndShapeRendererConf = actualItemStyleConf.getItemCategoryItemRendererConfiguration().getLineAndShapeConfiguration();
        boolean shapeVisible = lineAndShapeRendererConf.isShapesVisible();
        boolean shapeFilled = lineAndShapeRendererConf.isShapesFilled();
        boolean lineVisible = lineAndShapeRendererConf.isLinesVisible();
        Shape shape = actualItemStyleConf.getItemShapeConfiguration().hasShape() ? actualItemStyleConf.getItemShapeConfiguration().getShape() : (seriesStyleConf.getSeriesShapeConfiguration().hasShape() ? seriesStyleConf.getSeriesShapeConfiguration().getShape() : (rendererConf.getBaseShapeConfiguration().hasShape() ? rendererConf.getBaseShapeConfiguration().getShape() : AbstractRenderer.DEFAULT_SHAPE));
        Paint fillPaint = actualItemStyleConf.getItemFillPaintConfiguration().hasPaint() ? actualItemStyleConf.getItemFillPaintConfiguration().getPaint() : (seriesStyleConf.getSeriesPaintConfiguration().hasPaint() ? seriesStyleConf.getSeriesPaintConfiguration().getPaint() : (rendererConf.getBasePaintConfiguration().hasPaint() ? rendererConf.getBasePaintConfiguration().getPaint() : AbstractRenderer.DEFAULT_PAINT));
        boolean shapeOutLineVisible = true;
        Paint outLinePaint = rendererConf.getBaseCategoryItemRendererConfiguration().getLineAndShapeConfiguration().isUseOutlinePaint() ? (actualItemStyleConf.getItemOutlinePaintConfiguration().hasPaint() ? actualItemStyleConf.getItemOutlinePaintConfiguration().getPaint() : (seriesStyleConf.getSeriesPaintConfiguration().hasPaint() ? seriesStyleConf.getSeriesPaintConfiguration().getPaint() : (rendererConf.getBasePaintConfiguration().hasPaint() ? rendererConf.getBasePaintConfiguration().getPaint() : AbstractRenderer.DEFAULT_OUTLINE_PAINT))) : fillPaint;
        BasicStroke outLineStroke = new BasicStroke(1.0f);
        Line2D.Double line = new Line2D.Double(-7.0, 0.0, 7.0, 0.0);
        Stroke lineStroke = seriesStyleConf.getSeriesStrokeConfiguration().getStroke(rendererConf.getBaseStrokeConf().getStroke());
        Paint linePaint = seriesStyleConf.getSeriesPaintConfiguration().getOrSetPaint(rendererConf.getBasePaintConfiguration().getOrSetPaint(AbstractRenderer.DEFAULT_PAINT));
        LegendItem legendItem = new LegendItem(label, description, toolTipText, urlText, shapeVisible, shape, shapeFilled, fillPaint, shapeOutLineVisible, outLinePaint, (Stroke)outLineStroke, lineVisible, (Shape)line, lineStroke, linePaint);
        return legendItem;
    }

    private LegendItem createCategoryBarLegendItem(RendererConfiguration rendererConf, SeriesStyleConfiguration seriesStyleConf, com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration actualItemStyleConf, String label) {
        String description = null;
        String toolTipText = null;
        String urlText = null;
        Rectangle2D.Double shape = new Rectangle2D.Double(-3.0, -5.0, 6.0, 10.0);
        Paint paint = seriesStyleConf.getSeriesPaintConfiguration().getOrSetPaint(rendererConf.getBasePaintConfiguration().getOrSetPaint(BarRenderer.DEFAULT_PAINT));
        Paint outlinePaint = actualItemStyleConf.getItemOutlinePaintConfiguration().getOrSetPaint(seriesStyleConf.getSeriesOutlinePaintConfiguration().getOrSetPaint(rendererConf.getBaseOutlinePaintConf().getOrSetPaint(AbstractRenderer.DEFAULT_OUTLINE_PAINT)));
        BasicStroke outlineStroke = new BasicStroke(1.0f);
        LegendItem legendItem = new LegendItem(label, description, toolTipText, urlText, (Shape)shape, paint, (Stroke)outlineStroke, outlinePaint);
        return legendItem;
    }
}

