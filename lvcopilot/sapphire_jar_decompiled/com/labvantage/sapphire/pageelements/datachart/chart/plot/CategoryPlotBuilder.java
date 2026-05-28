/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.annotations.CategoryAnnotation
 *  org.jfree.chart.annotations.CategoryTextAnnotation
 *  org.jfree.chart.axis.AxisLocation
 *  org.jfree.chart.axis.CategoryAxis
 *  org.jfree.chart.axis.DateAxis
 *  org.jfree.chart.axis.NumberAxis
 *  org.jfree.chart.axis.ValueAxis
 *  org.jfree.chart.labels.CategoryItemLabelGenerator
 *  org.jfree.chart.labels.ItemLabelPosition
 *  org.jfree.chart.labels.StandardCategoryItemLabelGenerator
 *  org.jfree.chart.plot.CategoryPlot
 *  org.jfree.chart.plot.DrawingSupplier
 *  org.jfree.chart.renderer.AbstractRenderer
 *  org.jfree.chart.renderer.category.AbstractCategoryItemRenderer
 *  org.jfree.chart.renderer.category.BarPainter
 *  org.jfree.chart.renderer.category.BarRenderer
 *  org.jfree.chart.renderer.category.BarRenderer3D
 *  org.jfree.chart.renderer.category.CategoryItemRenderer
 *  org.jfree.chart.renderer.category.LineAndShapeRenderer
 *  org.jfree.chart.renderer.category.StackedBarRenderer
 *  org.jfree.chart.renderer.category.StackedBarRenderer3D
 *  org.jfree.chart.renderer.category.StandardBarPainter
 *  org.jfree.chart.urls.CategoryURLGenerator
 *  org.jfree.data.category.CategoryDataset
 *  org.jfree.data.general.Dataset
 */
package com.labvantage.sapphire.pageelements.datachart.chart.plot;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.AbstractComponentPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.ConfigurableDrawingSupplier;
import com.labvantage.sapphire.pageelements.datachart.chart.renderer.category.ConfigurableLineAndShapeRenderer;
import com.labvantage.sapphire.pageelements.datachart.chart.urls.URLGenerator;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ShapeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StandardItemLabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.AnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.CategoryAnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.CategoryTextAnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CategoryPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.BarRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.CategoryItemRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.LineAndShapeRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.SeriesStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.awt.Shape;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;

public final class CategoryPlotBuilder
extends AbstractComponentPlotBuilder {
    private final CategoryPlot categoryPlot;
    private final Map<String, CategoryItemRenderer> rendererMap = new HashMap<String, CategoryItemRenderer>();
    private final Map<String, ValueAxis> rangeAxisMap = new HashMap<String, ValueAxis>();
    private final Map<String, CategoryAxis> domainAxisMap = new HashMap<String, CategoryAxis>();

    public CategoryPlotBuilder(CategoryPlotConfiguration categoryPlotConf, Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo, boolean honorRTLmode) {
        super(categoryPlotConf, data, chartBindingMap, connectionInfo, honorRTLmode);
        this.categoryPlot = new CategoryPlot();
        if (!data.isEmpty()) {
            this.categoryPlot.setDrawingSupplier((DrawingSupplier)new ConfigurableDrawingSupplier(categoryPlotConf.getParent().getDrawingSupplierConfiguration()));
            if (!categoryPlotConf.getParent().hasDrawingSupplier()) {
                categoryPlotConf.getParent().setDrawingSupplier(this.categoryPlot.getDrawingSupplier());
            }
            this.categoryPlot.setForegroundAlpha(categoryPlotConf.getParent().getDrawingSupplierConfiguration().getForegroundAlpha());
            this.configureComponentPlot(data.getTraceableSeriesGroupList(this.getPlotId()));
            for (String annotationId : categoryPlotConf.getAnnotationIdList()) {
                if (annotationId.isEmpty()) continue;
                AnnotationConfiguration annotationConf = categoryPlotConf.getParent().getAnnotationConfiguration(annotationId);
                this.categoryPlot.addAnnotation(this.createCategoryAnnotation(annotationConf));
            }
            this.categoryPlot.setOrientation(categoryPlotConf.getPlotOrientation());
            this.categoryPlot.setRangeGridlinePaint(categoryPlotConf.getRangeGridlinePaintConfiguration().getOrSetPaint(CategoryPlot.DEFAULT_GRIDLINE_PAINT));
            this.categoryPlot.setDomainGridlinePaint(categoryPlotConf.getDomainGridlinePaintConfiguration().getOrSetPaint(CategoryPlot.DEFAULT_GRIDLINE_PAINT));
            this.categoryPlot.setRangeGridlinesVisible(categoryPlotConf.isRangeGridlineVisible());
            this.categoryPlot.setDomainGridlinesVisible(categoryPlotConf.isDomainGridlineVisible());
            this.categoryPlot.setRangeGridlineStroke(categoryPlotConf.getRangeGridlineStrokeConfiguration().getStroke(CategoryPlot.DEFAULT_GRIDLINE_STROKE));
            this.categoryPlot.setDomainGridlineStroke(categoryPlotConf.getDomainGridlineStrokeConfiguration().getStroke(CategoryPlot.DEFAULT_GRIDLINE_STROKE));
            this.categoryPlot.setDatasetRenderingOrder(categoryPlotConf.getDatasetRenderingOrder());
            this.categoryPlot.setColumnRenderingOrder(categoryPlotConf.getColumnRenderingOrder());
        } else {
            this.categoryPlot.setNoDataMessage(categoryPlotConf.getParent().getNoDataMessage());
        }
    }

    @Override
    protected void initRenderer(int seriesIndex, int itemIndex, String rendererId, ItemStyleConfiguration itemStyleConf) {
        CategoryItemRenderer r = this.rendererMap.get(rendererId);
        Shape defaultShape = r.getItemShape(seriesIndex, itemIndex);
        if (itemStyleConf != null) {
            itemStyleConf.getItemShapeConfiguration().setDefaultShape(defaultShape);
        }
    }

    @Override
    protected void addRangeMarker(int seriesGroupIndex, MarkerConfiguration markerConf) throws SapphireException {
        this.categoryPlot.addRangeMarker(seriesGroupIndex, this.createMarker(markerConf), markerConf.getLayerType().getLayer());
    }

    @Override
    protected void addDomainMarker(int seriesGroupIndex, MarkerConfiguration markerConf) throws SapphireException {
        this.categoryPlot.addDomainMarker(seriesGroupIndex, this.createCategoryMarker(markerConf), markerConf.getLayerType().getLayer());
    }

    @Override
    protected int getItemCount(int seriesGroupIndex, int seriesIndex) {
        return this.categoryPlot.getDataset(seriesGroupIndex).getColumnCount();
    }

    @Override
    protected int getSeriesCount(int seriesGroupIndex) {
        return this.categoryPlot.getDataset(seriesGroupIndex).getRowCount();
    }

    private CategoryAnnotation createCategoryAnnotation(AnnotationConfiguration annotationConf) {
        CategoryAnnotationConfiguration categoryAnnotationConf;
        AnnotationConfiguration.AnnotationType annotationType = annotationConf.getAnnotationType();
        if (annotationType == AnnotationConfiguration.AnnotationType.CATEGORY) {
            categoryAnnotationConf = annotationConf.getCategoryAnnotationConfiguration();
            CategoryAnnotationConfiguration.CategoryAnnotationType categoryAnnotationType = categoryAnnotationConf.getCategoryAnnotationType();
            if (categoryAnnotationType != CategoryAnnotationConfiguration.CategoryAnnotationType.TEXT) {
                throw new IllegalArgumentException("Unknown category annotation type: " + (Object)((Object)categoryAnnotationType));
            }
        } else {
            throw new IllegalArgumentException("Category plot only supports category annotations. Got annotation type: " + (Object)((Object)annotationType));
        }
        CategoryTextAnnotationConfiguration categoryTextAnnotationConf = categoryAnnotationConf.getCategoryTextAnnotationConfiguration();
        CategoryTextAnnotation categoryTextAnnotation = new CategoryTextAnnotation(categoryTextAnnotationConf.getText().evaluateNoException(this.getChartBindingMap()), (Comparable)((Object)categoryTextAnnotationConf.getCategory().evaluateNoException(this.getChartBindingMap())), categoryTextAnnotationConf.getValue().evaluateNoException(this.getChartBindingMap()).doubleValue());
        categoryTextAnnotation.setCategoryAnchor(categoryTextAnnotationConf.getCategoryAnchor());
        CategoryTextAnnotation categoryAnnotation = categoryTextAnnotation;
        return categoryAnnotation;
    }

    private CategoryItemRenderer createRenderer(RendererConfiguration rendererConf, TraceableSeriesGroup traceableSeriesGroup, SeriesGroupConfiguration seriesGroupConf, ConnectionInfo connectionInfo) {
        URLGenerator urlGenerator;
        LineAndShapeRenderer returnRenderer;
        RendererConfiguration.RendererType rendererType = rendererConf.getRendererType();
        if (rendererType == RendererConfiguration.RendererType.LINE_AND_SHAPE) {
            returnRenderer = seriesGroupConf.isItemStylesEnabled() ? new ConfigurableLineAndShapeRenderer(rendererConf, seriesGroupConf, traceableSeriesGroup, connectionInfo, this.getChartBindingMap(), this.rangeAxisMap) : new LineAndShapeRenderer();
        } else if (rendererType == RendererConfiguration.RendererType.BAR) {
            returnRenderer = new BarRenderer();
        } else if (rendererType == RendererConfiguration.RendererType.BAR_3D) {
            returnRenderer = new BarRenderer3D();
        } else if (rendererType == RendererConfiguration.RendererType.STACKED_BAR) {
            returnRenderer = new StackedBarRenderer();
        } else if (rendererType == RendererConfiguration.RendererType.STACKED_BAR_3D) {
            returnRenderer = new StackedBarRenderer3D();
        } else {
            throw new IllegalArgumentException("Unknown renderer type: " + (Object)((Object)rendererType));
        }
        CategoryItemRendererConfiguration categoryItemRendererConfiguration = rendererConf.getBaseCategoryItemRendererConfiguration();
        PaintConfiguration basePaintConf = rendererConf.getBasePaintConfiguration();
        returnRenderer.setBasePaint(basePaintConf.getOrSetPaint(AbstractRenderer.DEFAULT_PAINT));
        returnRenderer.setAutoPopulateSeriesPaint(rendererConf.isAutoPopulateSeriesPaint());
        PaintConfiguration baseOutlinePaintConf = rendererConf.getBaseOutlinePaintConf();
        returnRenderer.setBaseOutlinePaint(baseOutlinePaintConf.getOrSetPaint(AbstractCategoryItemRenderer.DEFAULT_OUTLINE_PAINT));
        returnRenderer.setAutoPopulateSeriesOutlinePaint(rendererConf.isAutoPopulateSeriesOutlinePaint());
        PaintConfiguration baseFillPaintConf = rendererConf.getBaseFillPaintConf();
        returnRenderer.setBaseFillPaint(baseFillPaintConf.getOrSetPaint(AbstractCategoryItemRenderer.DEFAULT_PAINT));
        returnRenderer.setAutoPopulateSeriesFillPaint(rendererConf.isAutoPopulateSeriesFillPaint());
        ShapeConfiguration baseShapeConfiguration = rendererConf.getBaseShapeConfiguration();
        returnRenderer.setBaseShape(baseShapeConfiguration.getShape(AbstractRenderer.DEFAULT_SHAPE));
        returnRenderer.setAutoPopulateSeriesShape(rendererConf.isAutoPopulateSeriesShape());
        StrokeConfiguration baseStrokeConfiguration = rendererConf.getBaseStrokeConf();
        returnRenderer.setBaseStroke(baseStrokeConfiguration.getStroke(AbstractRenderer.DEFAULT_STROKE));
        returnRenderer.setAutoPopulateSeriesStroke(rendererConf.isAutoPopulateSeriesStroke());
        returnRenderer.setBaseSeriesVisible(rendererConf.isBaseSeriesVisible());
        returnRenderer.setBaseSeriesVisibleInLegend(rendererConf.isBaseSeriesVisibleInLegend());
        StandardCategoryItemLabelGenerator baseItemLabelGenerator = this.getItemLabelGenerator(rendererConf.getBaseLabelConfiguration(), seriesGroupConf);
        returnRenderer.setBaseItemLabelGenerator((CategoryItemLabelGenerator)baseItemLabelGenerator);
        if (rendererConf.getBaseLabelConfiguration().getLabelsVisible() != null) {
            returnRenderer.setBaseItemLabelsVisible(rendererConf.getBaseLabelConfiguration().getLabelsVisible());
        }
        returnRenderer.setBasePositiveItemLabelPosition(new ItemLabelPosition());
        returnRenderer.setBaseItemLabelFont(rendererConf.getBaseLabelConfiguration().getFontConf().getFont(returnRenderer.getBaseItemLabelFont(), returnRenderer.getBaseItemLabelPaint()));
        if (rendererType == RendererConfiguration.RendererType.LINE_AND_SHAPE) {
            LineAndShapeRendererConfiguration baseLineAndShapeRendererConfiguration = categoryItemRendererConfiguration.getLineAndShapeConfiguration();
            LineAndShapeRenderer lineAndShapeRenderer = returnRenderer;
            lineAndShapeRenderer.setBaseShapesFilled(baseLineAndShapeRendererConfiguration.isShapesFilled());
            lineAndShapeRenderer.setUseOutlinePaint(baseLineAndShapeRendererConfiguration.isUseOutlinePaint() && !rendererConf.isAutoPopulateSeriesOutlinePaint());
            lineAndShapeRenderer.setUseFillPaint(baseLineAndShapeRendererConfiguration.isUseFillPaint() && !rendererConf.isAutoPopulateSeriesFillPaint());
            lineAndShapeRenderer.setBaseShapesVisible(baseLineAndShapeRendererConfiguration.isShapesVisible());
            lineAndShapeRenderer.setBaseLinesVisible(baseLineAndShapeRendererConfiguration.isLinesVisible());
        }
        if (rendererType == RendererConfiguration.RendererType.BAR || rendererType == RendererConfiguration.RendererType.BAR_3D || rendererType == RendererConfiguration.RendererType.STACKED_BAR || rendererType == RendererConfiguration.RendererType.STACKED_BAR_3D) {
            BarRendererConfiguration baseBarRendererConfiguration = categoryItemRendererConfiguration.getBarRendererConfiguration();
            BarRenderer barRenderer = (BarRenderer)returnRenderer;
            barRenderer.setShadowVisible(baseBarRendererConfiguration.isShadowsVisible());
            if (baseBarRendererConfiguration.getBarPainter().equals("Standard")) {
                barRenderer.setBarPainter((BarPainter)new StandardBarPainter());
            }
            barRenderer.setDrawBarOutline(baseBarRendererConfiguration.isDrawBarOutline());
            barRenderer.setItemMargin(baseBarRendererConfiguration.getItemMargin());
        }
        String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
        for (int seriesIndex = 0; seriesIndex < traceableSeriesGroup.getSeriesCount(); ++seriesIndex) {
            StandardCategoryItemLabelGenerator seriesItemLabelGenerator;
            StrokeConfiguration seriesStrokeConf;
            ShapeConfiguration seriesShapeConf;
            PaintConfiguration seriesOutlinePaintConf;
            PaintConfiguration seriesFillPaintConf;
            String seriesId = traceableSeriesGroup.getSeriesId(seriesIndex);
            this.getChartBindingMap().setActiveSeries(this.getPlotId(), seriesGroupId, seriesId);
            SeriesConfiguration seriesConf = seriesGroupConf.getSeriesConfiguration(seriesId);
            SeriesStyleConfiguration seriesStyleConf = rendererConf.getSeriesStyleConfiguration(seriesConf.getSeriesStyleId());
            CategoryItemRendererConfiguration seriesCategoryItemRendererConf = seriesStyleConf.getSeriesCategoryItemRendererConf();
            PaintConfiguration seriesPaintConf = seriesStyleConf.getSeriesPaintConfiguration();
            if (seriesPaintConf.hasPaint()) {
                returnRenderer.setSeriesPaint(seriesIndex, seriesPaintConf.getPaint());
            }
            if ((seriesFillPaintConf = seriesStyleConf.getSeriesFillPaintConfiguration()).hasPaint()) {
                returnRenderer.setSeriesFillPaint(seriesIndex, seriesFillPaintConf.getPaint());
            }
            if ((seriesOutlinePaintConf = seriesStyleConf.getSeriesOutlinePaintConfiguration()).hasPaint()) {
                returnRenderer.setSeriesOutlinePaint(seriesIndex, seriesOutlinePaintConf.getPaint());
            }
            if ((seriesShapeConf = seriesStyleConf.getSeriesShapeConfiguration()).hasShape()) {
                returnRenderer.setSeriesShape(seriesIndex, seriesShapeConf.getShape());
            }
            if ((seriesStrokeConf = seriesStyleConf.getSeriesStrokeConfiguration()).hasStroke()) {
                returnRenderer.setSeriesStroke(seriesIndex, seriesStrokeConf.getStroke());
            }
            if (seriesStyleConf.getSeriesVisible() != null) {
                returnRenderer.setSeriesVisible(seriesIndex, seriesStyleConf.getSeriesVisible());
            }
            if (seriesStyleConf.getSeriesVisibleInLegend() != null) {
                returnRenderer.setSeriesVisibleInLegend(seriesIndex, seriesStyleConf.getSeriesVisibleInLegend());
            }
            if ((seriesItemLabelGenerator = this.getItemLabelGenerator(seriesStyleConf.getSeriesLabelConfiguration(), seriesGroupConf)) != null) {
                returnRenderer.setSeriesItemLabelGenerator(seriesIndex, (CategoryItemLabelGenerator)seriesItemLabelGenerator);
                returnRenderer.setSeriesPositiveItemLabelPosition(seriesIndex, new ItemLabelPosition());
            }
            if (seriesStyleConf.getSeriesLabelConfiguration().getLabelsVisible() != null) {
                returnRenderer.setSeriesItemLabelsVisible(seriesIndex, seriesStyleConf.getSeriesLabelConfiguration().getLabelsVisible());
            }
            returnRenderer.setSeriesItemLabelFont(seriesIndex, seriesStyleConf.getSeriesLabelConfiguration().getFontConf().getFont(returnRenderer.getBaseItemLabelFont(), returnRenderer.getBaseItemLabelPaint()));
            if (!(returnRenderer instanceof LineAndShapeRenderer)) continue;
            LineAndShapeRendererConfiguration seriesLineAndShapeRendererConf = seriesCategoryItemRendererConf.getLineAndShapeConfiguration();
            LineAndShapeRenderer lineAndShapeRenderer = returnRenderer;
            lineAndShapeRenderer.setSeriesShapesFilled(seriesIndex, seriesLineAndShapeRendererConf.isShapesFilled());
            lineAndShapeRenderer.setSeriesLinesVisible(seriesIndex, seriesLineAndShapeRendererConf.isLinesVisible());
            lineAndShapeRenderer.setSeriesShapesVisible(seriesIndex, seriesLineAndShapeRendererConf.isShapesVisible());
        }
        String urlGeneratorId = rendererConf.getBaseCategoryItemRendererConfiguration().getUrlGeneratorId();
        if (!urlGeneratorId.isEmpty() && (urlGenerator = this.getUrlGeneratorMap().get(urlGeneratorId)) != null) {
            returnRenderer.setBaseItemURLGenerator((CategoryURLGenerator)urlGenerator);
        }
        return returnRenderer;
    }

    private StandardCategoryItemLabelGenerator getItemLabelGenerator(LabelConfiguration labelConf, SeriesGroupConfiguration seriesGroupConf) {
        StandardCategoryItemLabelGenerator standardCategoryItemLabelGenerator = null;
        if (labelConf.getLabelsVisible() == null || !labelConf.getLabelsVisible().booleanValue()) {
            standardCategoryItemLabelGenerator = null;
        } else if (labelConf.getLabelType().equals("Standard")) {
            ValueAxis yAxis;
            StandardItemLabelConfiguration standardItemLabelConfiguration = labelConf.getStandardItemLabelConfiguration();
            try {
                yAxis = this.rangeAxisMap.get(seriesGroupConf.getRangeAxisId().evaluate(this.getChartBindingMap()));
            }
            catch (SapphireException e) {
                throw new IllegalArgumentException("Error while evaluating range axis ID expression: " + seriesGroupConf.getRangeAxisId().getExpression(), e);
            }
            NumberFormat yNumberFormat = null;
            NumberFormat yPercentFormat = null;
            DateFormat yDateFormat = null;
            if (yAxis instanceof DateAxis) {
                yDateFormat = ((DateAxis)yAxis).getDateFormatOverride();
                if (yDateFormat == null) {
                    yDateFormat = DateFormat.getDateInstance(2, I18nUtil.getConnectionLocale(this.getConnectionInfo()));
                }
            } else if (yAxis instanceof NumberAxis) {
                if (!standardItemLabelConfiguration.getNumberFormatStr().isEmpty()) {
                    yNumberFormat = new DecimalFormat(standardItemLabelConfiguration.getNumberFormatStr());
                } else {
                    yNumberFormat = ((NumberAxis)yAxis).getNumberFormatOverride();
                    if (yNumberFormat == null) {
                        yNumberFormat = NumberFormat.getNumberInstance(I18nUtil.getConnectionLocale(this.getConnectionInfo()));
                    }
                }
                yPercentFormat = !standardItemLabelConfiguration.getPercentageNumberFormatStr().isEmpty() ? new DecimalFormat(standardItemLabelConfiguration.getPercentageNumberFormatStr()) : NumberFormat.getPercentInstance(I18nUtil.getConnectionLocale(this.getConnectionInfo()));
            }
            String labelStr = labelConf.getStandardItemLabelConfiguration().getLabelString();
            standardCategoryItemLabelGenerator = yNumberFormat != null ? new StandardCategoryItemLabelGenerator(labelStr, yNumberFormat, yPercentFormat) : (yDateFormat != null ? new StandardCategoryItemLabelGenerator(labelStr, yDateFormat) : new StandardCategoryItemLabelGenerator());
        }
        return standardCategoryItemLabelGenerator;
    }

    public CategoryPlot getPlot() {
        return this.categoryPlot;
    }

    @Override
    protected void setRangeAxisLocation(int rangeAxisIndex, AxisLocation axisLocation) {
        this.categoryPlot.setRangeAxisLocation(rangeAxisIndex, axisLocation);
    }

    @Override
    protected void setDomainAxisLocation(int domainAxisIndex, AxisLocation axisLocation) {
        this.categoryPlot.setDomainAxisLocation(domainAxisIndex, axisLocation);
    }

    @Override
    protected void mapDatasetToRangeAxis(int index, String rangeAxisId) {
        this.categoryPlot.mapDatasetToRangeAxis(index, this.categoryPlot.getRangeAxisIndex(this.rangeAxisMap.get(rangeAxisId)));
    }

    @Override
    protected void mapDatasetToDomainAxis(int index, String domainAxisId) {
        this.categoryPlot.mapDatasetToDomainAxis(index, this.categoryPlot.getDomainAxisIndex(this.domainAxisMap.get(domainAxisId)));
    }

    @Override
    protected void setPlotRangeAxisFromCache(int index, String rangeAxisId) {
        this.categoryPlot.setRangeAxis(index, this.rangeAxisMap.get(rangeAxisId));
    }

    @Override
    protected void setPlotDomainAxisFromCache(int index, String domainAxisId) {
        this.categoryPlot.setDomainAxis(index, this.domainAxisMap.get(domainAxisId));
    }

    @Override
    protected void cacheRangeAxis(String rangeAxisId, AxisConfiguration axisConf) {
        this.rangeAxisMap.put(rangeAxisId, this.createValueAxis(axisConf, false));
    }

    @Override
    protected void cacheDomainAxis(String domainAxisId, AxisConfiguration axisConf) {
        this.domainAxisMap.put(domainAxisId, this.createCategoryAxis(axisConf));
    }

    @Override
    protected boolean hasCachedRangeAxis(String rangeAxisId) {
        return this.rangeAxisMap.get(rangeAxisId) != null;
    }

    @Override
    protected boolean hasCachedDomainAxis(String domainAxisId) {
        return this.domainAxisMap.get(domainAxisId) != null;
    }

    @Override
    protected void setPlotRendererFromCache(int index, String rendererId) {
        this.categoryPlot.setRenderer(index, this.rendererMap.get(rendererId));
    }

    @Override
    protected void cacheRenderer(String rendererId, RendererConfiguration plotRendererConf, TraceableSeriesGroup traceableSeriesGroup, SeriesGroupConfiguration seriesGroupConf) {
        this.rendererMap.put(rendererId, this.createRenderer(plotRendererConf, traceableSeriesGroup, seriesGroupConf, this.getConnectionInfo()));
    }

    @Override
    protected boolean hasCachedRenderer(String rendererId) {
        return this.rendererMap.get(rendererId) != null;
    }

    @Override
    protected void setPlotDataset(int index, Dataset seriesGroup) {
        this.categoryPlot.setDataset(index, (CategoryDataset)seriesGroup);
    }
}

