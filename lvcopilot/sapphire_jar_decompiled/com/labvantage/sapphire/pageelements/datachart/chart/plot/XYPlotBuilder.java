/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.annotations.XYAnnotation
 *  org.jfree.chart.annotations.XYTextAnnotation
 *  org.jfree.chart.annotations.XYTitleAnnotation
 *  org.jfree.chart.axis.AxisLocation
 *  org.jfree.chart.axis.DateAxis
 *  org.jfree.chart.axis.NumberAxis
 *  org.jfree.chart.axis.ValueAxis
 *  org.jfree.chart.labels.AbstractXYItemLabelGenerator
 *  org.jfree.chart.labels.StandardXYItemLabelGenerator
 *  org.jfree.chart.labels.XYItemLabelGenerator
 *  org.jfree.chart.plot.DrawingSupplier
 *  org.jfree.chart.plot.Plot
 *  org.jfree.chart.plot.XYPlot
 *  org.jfree.chart.renderer.AbstractRenderer
 *  org.jfree.chart.renderer.xy.AbstractXYItemRenderer
 *  org.jfree.chart.renderer.xy.StandardXYBarPainter
 *  org.jfree.chart.renderer.xy.XYBarPainter
 *  org.jfree.chart.renderer.xy.XYBarRenderer
 *  org.jfree.chart.renderer.xy.XYItemRenderer
 *  org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
 *  org.jfree.chart.renderer.xy.XYStepRenderer
 *  org.jfree.chart.renderer.xy.YIntervalRenderer
 *  org.jfree.chart.title.Title
 *  org.jfree.chart.urls.XYURLGenerator
 *  org.jfree.data.general.Dataset
 *  org.jfree.data.xy.XYDataset
 */
package com.labvantage.sapphire.pageelements.datachart.chart.plot;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.chart.Chart;
import com.labvantage.sapphire.pageelements.datachart.chart.ConfigurableXYItemLabelGenerator;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.AbstractComponentPlotBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.ConfigurableDrawingSupplier;
import com.labvantage.sapphire.pageelements.datachart.chart.plot.trendline.XYTrendLineBuilder;
import com.labvantage.sapphire.pageelements.datachart.chart.renderer.xy.ConfigurableXYLineAndShapeRenderer;
import com.labvantage.sapphire.pageelements.datachart.chart.urls.URLGenerator;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ShapeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StandardItemLabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.AnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.XYAnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.XYTextAnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.XYTitleAnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.XYPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.SeriesStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYBarRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYItemRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYLineAndShapeRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.awt.Shape;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.chart.renderer.xy.YIntervalRenderer;
import org.jfree.chart.title.Title;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;

public final class XYPlotBuilder
extends AbstractComponentPlotBuilder {
    private final XYPlot xyPlot;
    private final Map<String, XYItemRenderer> rendererMap = new HashMap<String, XYItemRenderer>();
    private final Map<String, ValueAxis> rangeAxisMap = new HashMap<String, ValueAxis>();
    private final Map<String, ValueAxis> domainAxisMap = new HashMap<String, ValueAxis>();

    public XYPlotBuilder(XYPlotConfiguration xyPlotConf, Data data, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo, boolean honorRTLmode) {
        super(xyPlotConf, data, chartBindingMap, connectionInfo, honorRTLmode);
        this.xyPlot = new XYPlot();
        if (!data.isEmpty()) {
            this.xyPlot.setDrawingSupplier((DrawingSupplier)new ConfigurableDrawingSupplier(xyPlotConf.getParent().getDrawingSupplierConfiguration()));
            if (!xyPlotConf.getParent().hasDrawingSupplier()) {
                xyPlotConf.getParent().setDrawingSupplier(this.xyPlot.getDrawingSupplier());
            }
            this.xyPlot.setForegroundAlpha(xyPlotConf.getParent().getDrawingSupplierConfiguration().getForegroundAlpha());
            XYTrendLineBuilder xyTrendLineBuilder = new XYTrendLineBuilder(data, chartBindingMap, xyPlotConf);
            data.resetTempTraceableSeriesGroups();
            List<TraceableSeriesGroup> trendLineTraceableSeriesGroupList = xyTrendLineBuilder.getTrendLineSeriesGroupList();
            for (TraceableSeriesGroup trendLineTraceableSeriesGroup : trendLineTraceableSeriesGroupList) {
                data.addTempTraceableSeriesGroup(trendLineTraceableSeriesGroup);
            }
            this.configureComponentPlot(data.getTraceableSeriesGroupList(this.getPlotId()));
            for (String annotationId : xyPlotConf.getAnnotationIdList()) {
                if (annotationId.isEmpty()) continue;
                AnnotationConfiguration annotationConf = xyPlotConf.getParent().getAnnotationConfiguration(annotationId);
                this.xyPlot.addAnnotation(this.createXYAnnotation(annotationConf));
            }
            this.xyPlot.setOrientation(xyPlotConf.getPlotOrientation());
            this.xyPlot.setRangeGridlinePaint(xyPlotConf.getRangeGridlinePaintConfiguration().getOrSetPaint(XYPlot.DEFAULT_GRIDLINE_PAINT));
            this.xyPlot.setDomainGridlinePaint(xyPlotConf.getDomainGridlinePaintConfiguration().getOrSetPaint(XYPlot.DEFAULT_GRIDLINE_PAINT));
            this.xyPlot.setRangeGridlinesVisible(xyPlotConf.isRangeGridlineVisible());
            this.xyPlot.setDomainGridlinesVisible(xyPlotConf.isDomainGridlineVisible());
            this.xyPlot.setRangeGridlineStroke(xyPlotConf.getRangeGridlineStrokeConfiguration().getStroke(XYPlot.DEFAULT_GRIDLINE_STROKE));
            this.xyPlot.setDomainGridlineStroke(xyPlotConf.getDomainGridlineStrokeConfiguration().getStroke(XYPlot.DEFAULT_GRIDLINE_STROKE));
            this.xyPlot.setDatasetRenderingOrder(xyPlotConf.getDatasetRenderingOrder());
            this.xyPlot.setSeriesRenderingOrder(xyPlotConf.getSeriesRenderingOrder());
        } else {
            this.xyPlot.setNoDataMessage(xyPlotConf.getParent().getNoDataMessage());
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private XYAnnotation createXYAnnotation(AnnotationConfiguration annotationConf) {
        AnnotationConfiguration.AnnotationType annotationType = annotationConf.getAnnotationType();
        if (annotationType != AnnotationConfiguration.AnnotationType.XY) throw new IllegalArgumentException("XY plot only supports XY annotations. Got annotation type: " + (Object)((Object)annotationType));
        XYAnnotationConfiguration xyAnnotationConf = annotationConf.getXYAnnotationConfiguration();
        XYAnnotationConfiguration.XYAnnotationType xyAnnotationType = xyAnnotationConf.getXYAnnotationType();
        if (xyAnnotationType == XYAnnotationConfiguration.XYAnnotationType.TEXT) {
            XYTextAnnotationConfiguration xyTextAnnotationConf = xyAnnotationConf.getXYTextAnnotationConfiguration();
            XYTextAnnotation xyTextAnnotation = new XYTextAnnotation(xyTextAnnotationConf.getText().evaluateNoException(this.getChartBindingMap()), xyTextAnnotationConf.getX().evaluateNoException(this.getChartBindingMap()).doubleValue(), xyTextAnnotationConf.getY().evaluateNoException(this.getChartBindingMap()).doubleValue());
            xyTextAnnotation.setTextAnchor(xyTextAnnotationConf.getTextAnchor());
            return xyTextAnnotation;
        }
        if (xyAnnotationType != XYAnnotationConfiguration.XYAnnotationType.TITLE) throw new IllegalArgumentException("Unknown XY annotation type: " + (Object)((Object)xyAnnotationType));
        XYTitleAnnotationConfiguration xyTitleAnnotationConf = xyAnnotationConf.getXYTitleAnnotationConfiguration();
        String titleId = xyTitleAnnotationConf.getTitleId();
        if (titleId.isEmpty()) throw new IllegalArgumentException("XY title annotation has empty title ID");
        TitleConfiguration titleConf = this.getComponentPlotConf().getParent().getParent().getTitleConfiguration(titleId);
        try {
            Title annotationTitle = Chart.createTitle(titleConf, this.getChartBindingMap(), (Plot)this.xyPlot, this.getComponentPlotConf(), this.getData(), this.getConnectionInfo());
            return new XYTitleAnnotation(xyTitleAnnotationConf.getX(), xyTitleAnnotationConf.getY(), annotationTitle, xyTitleAnnotationConf.getRectangleAnchor());
        }
        catch (SapphireException e) {
            throw new IllegalArgumentException("Cannot create title", e);
        }
    }

    private XYItemRenderer createRenderer(RendererConfiguration rendererConf, TraceableSeriesGroup traceableSeriesGroup, SeriesGroupConfiguration seriesGroupConf) {
        RendererConfiguration.RendererType rendererType = rendererConf.getRendererType();
        AbstractXYItemRenderer returnRenderer = this.getAbstractXYItemRenderer(rendererConf, traceableSeriesGroup, seriesGroupConf, rendererType);
        XYItemRendererConfiguration baseXYItemRendererConf = rendererConf.getBaseXYItemRendererConfiguration();
        PaintConfiguration basePaintConf = rendererConf.getBasePaintConfiguration();
        returnRenderer.setBasePaint(basePaintConf.getOrSetPaint(AbstractXYItemRenderer.DEFAULT_PAINT));
        returnRenderer.setAutoPopulateSeriesPaint(rendererConf.isAutoPopulateSeriesPaint());
        PaintConfiguration baseOutlinePaintConf = rendererConf.getBaseOutlinePaintConf();
        returnRenderer.setBaseOutlinePaint(baseOutlinePaintConf.getOrSetPaint(AbstractXYItemRenderer.DEFAULT_OUTLINE_PAINT));
        returnRenderer.setAutoPopulateSeriesOutlinePaint(rendererConf.isAutoPopulateSeriesOutlinePaint());
        PaintConfiguration baseFillPaintConf = rendererConf.getBaseFillPaintConf();
        returnRenderer.setBaseFillPaint(baseFillPaintConf.getOrSetPaint(AbstractXYItemRenderer.DEFAULT_PAINT));
        returnRenderer.setAutoPopulateSeriesFillPaint(rendererConf.isAutoPopulateSeriesFillPaint());
        ShapeConfiguration baseShapeConfiguration = rendererConf.getBaseShapeConfiguration();
        returnRenderer.setBaseShape(baseShapeConfiguration.getShape(AbstractRenderer.DEFAULT_SHAPE));
        returnRenderer.setAutoPopulateSeriesShape(rendererConf.isAutoPopulateSeriesShape());
        StrokeConfiguration baseStrokeConfiguration = rendererConf.getBaseStrokeConf();
        returnRenderer.setBaseStroke(baseStrokeConfiguration.getStroke(AbstractRenderer.DEFAULT_STROKE));
        returnRenderer.setAutoPopulateSeriesStroke(rendererConf.isAutoPopulateSeriesStroke());
        returnRenderer.setBaseSeriesVisible(rendererConf.isBaseSeriesVisible());
        returnRenderer.setBaseSeriesVisibleInLegend(rendererConf.isBaseSeriesVisibleInLegend());
        AbstractXYItemLabelGenerator baseItemLabelGenerator = this.getItemLabelGenerator(rendererConf.getBaseLabelConfiguration(), seriesGroupConf, traceableSeriesGroup);
        returnRenderer.setBaseItemLabelGenerator((XYItemLabelGenerator)baseItemLabelGenerator);
        if (rendererConf.getBaseLabelConfiguration().getLabelsVisible() != null) {
            returnRenderer.setBaseItemLabelsVisible(rendererConf.getBaseLabelConfiguration().getLabelsVisible());
        }
        returnRenderer.setBaseItemLabelFont(rendererConf.getBaseLabelConfiguration().getFontConf().getFont(returnRenderer.getBaseItemLabelFont(), returnRenderer.getBaseItemLabelPaint()));
        if (rendererType == RendererConfiguration.RendererType.XY_LINE_AND_SHAPE) {
            XYLineAndShapeRendererConfiguration baseXYLineAndShapeRendererConf = baseXYItemRendererConf.getXYLineAndShapeConfiguration();
            XYLineAndShapeRenderer xyLineAndShapeRenderer = (XYLineAndShapeRenderer)returnRenderer;
            xyLineAndShapeRenderer.setBaseShapesFilled(baseXYLineAndShapeRendererConf.isShapesFilled());
            xyLineAndShapeRenderer.setUseOutlinePaint(baseXYLineAndShapeRendererConf.isUseOutlinePaint() && !rendererConf.isAutoPopulateSeriesOutlinePaint());
            xyLineAndShapeRenderer.setUseFillPaint(baseXYLineAndShapeRendererConf.isUseFillPaint() && !rendererConf.isAutoPopulateSeriesFillPaint());
            xyLineAndShapeRenderer.setBaseShapesVisible(baseXYLineAndShapeRendererConf.isShapesVisible());
            xyLineAndShapeRenderer.setBaseLinesVisible(baseXYLineAndShapeRendererConf.isLinesVisible());
        }
        if (rendererType == RendererConfiguration.RendererType.XY_BAR) {
            XYBarRendererConfiguration baseBarRendererConfiguration = baseXYItemRendererConf.getXyBarRendererConf();
            XYBarRenderer xyBarRenderer = (XYBarRenderer)returnRenderer;
            xyBarRenderer.setShadowVisible(baseBarRendererConfiguration.isShadowsVisible());
            if (baseBarRendererConfiguration.getBarPainter().equals("Standard")) {
                xyBarRenderer.setBarPainter((XYBarPainter)new StandardXYBarPainter());
            }
            xyBarRenderer.setDrawBarOutline(baseBarRendererConfiguration.isDrawBarOutline());
            xyBarRenderer.setMargin(baseBarRendererConfiguration.getMargin());
        }
        String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
        for (int seriesIndex = 0; seriesIndex < traceableSeriesGroup.getSeriesCount(); ++seriesIndex) {
            AbstractXYItemLabelGenerator seriesItemLabelGenerator;
            StrokeConfiguration seriesStrokeConf;
            ShapeConfiguration seriesShapeConf;
            PaintConfiguration seriesOutlinePaintConf;
            PaintConfiguration seriesFillPaintConf;
            String seriesId = traceableSeriesGroup.getSeriesId(seriesIndex);
            this.getChartBindingMap().setActiveSeries(this.getPlotId(), seriesGroupId, seriesId);
            SeriesConfiguration seriesConf = seriesGroupConf.getSeriesConfiguration(seriesId);
            SeriesStyleConfiguration seriesStyleConf = rendererConf.getSeriesStyleConfiguration(seriesConf.getSeriesStyleId());
            XYItemRendererConfiguration seriesXYItemRendererConf = seriesStyleConf.getSeriesXYItemRendererConfiguration();
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
            if ((seriesItemLabelGenerator = this.getItemLabelGenerator(seriesStyleConf.getSeriesLabelConfiguration(), seriesGroupConf, traceableSeriesGroup)) != null) {
                returnRenderer.setSeriesItemLabelGenerator(seriesIndex, (XYItemLabelGenerator)seriesItemLabelGenerator);
            }
            if (seriesStyleConf.getSeriesLabelConfiguration().getLabelsVisible() != null) {
                returnRenderer.setSeriesItemLabelsVisible(seriesIndex, seriesStyleConf.getSeriesLabelConfiguration().getLabelsVisible());
            }
            returnRenderer.setSeriesItemLabelFont(seriesIndex, seriesStyleConf.getSeriesLabelConfiguration().getFontConf().getFont(returnRenderer.getBaseItemLabelFont(), returnRenderer.getBaseItemLabelPaint()));
            if (!(returnRenderer instanceof XYLineAndShapeRenderer)) continue;
            XYLineAndShapeRendererConfiguration seriesXYLineAndShapeRendererConf = seriesXYItemRendererConf.getXYLineAndShapeConfiguration();
            XYLineAndShapeRenderer xyLineAndShapeRenderer = (XYLineAndShapeRenderer)returnRenderer;
            xyLineAndShapeRenderer.setSeriesShapesFilled(seriesIndex, seriesXYLineAndShapeRendererConf.isShapesFilled());
            xyLineAndShapeRenderer.setSeriesLinesVisible(seriesIndex, seriesXYLineAndShapeRendererConf.isLinesVisible());
            xyLineAndShapeRenderer.setSeriesShapesVisible(seriesIndex, seriesXYLineAndShapeRendererConf.isShapesVisible());
        }
        this.attachRendererURLGenerator(returnRenderer, baseXYItemRendererConf);
        this.addRendererAnnotations(returnRenderer, baseXYItemRendererConf);
        return returnRenderer;
    }

    private void attachRendererURLGenerator(AbstractXYItemRenderer returnRenderer, XYItemRendererConfiguration baseXYItemRendererConf) {
        URLGenerator urlGenerator;
        String urlGeneratorId = baseXYItemRendererConf.getUrlGeneratorId();
        if (!urlGeneratorId.isEmpty() && (urlGenerator = this.getUrlGeneratorMap().get(urlGeneratorId)) != null) {
            returnRenderer.setURLGenerator((XYURLGenerator)urlGenerator);
        }
    }

    private void addRendererAnnotations(AbstractXYItemRenderer renderer, XYItemRendererConfiguration xyItemRendererConf) {
        List<String> annotationIdList = xyItemRendererConf.getAnnotationIdList();
        for (String annotationId : annotationIdList) {
            if (annotationId.isEmpty()) continue;
            AnnotationConfiguration annotationConf = this.getComponentPlotConf().getParent().getAnnotationConfiguration(annotationId);
            renderer.addAnnotation(this.createXYAnnotation(annotationConf));
        }
    }

    private AbstractXYItemLabelGenerator getItemLabelGenerator(LabelConfiguration labelConf, SeriesGroupConfiguration seriesGroupConf, TraceableSeriesGroup traceableSeriesGroup) {
        ConfigurableXYItemLabelGenerator standardXYItemLabelGenerator = null;
        if (labelConf.getLabelsVisible() == null || !labelConf.getLabelsVisible().booleanValue()) {
            standardXYItemLabelGenerator = null;
        } else if (labelConf.getLabelType().equals("Standard")) {
            ValueAxis xAxis;
            StandardItemLabelConfiguration standardItemLabelConfiguration = labelConf.getStandardItemLabelConfiguration();
            ValueAxis yAxis = null;
            try {
                yAxis = this.rangeAxisMap.get(seriesGroupConf.getRangeAxisId().evaluate(this.getChartBindingMap()));
            }
            catch (SapphireException e) {
                throw new IllegalArgumentException("Error while evaluating range axis ID expression: " + seriesGroupConf.getRangeAxisId().getExpression(), e);
            }
            NumberFormat yNumberFormat = null;
            NumberFormat xNumberFormat = null;
            DateFormat yDateFormat = null;
            DateFormat xDateFormat = null;
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
            }
            try {
                xAxis = this.domainAxisMap.get(seriesGroupConf.getDomainAxisId().evaluate(this.getChartBindingMap()));
            }
            catch (SapphireException e) {
                throw new IllegalArgumentException("Error while evaluating domain axis ID expression: " + seriesGroupConf.getDomainAxisId().getExpression(), e);
            }
            if (xAxis instanceof DateAxis) {
                xDateFormat = ((DateAxis)xAxis).getDateFormatOverride();
                if (xDateFormat == null) {
                    xDateFormat = DateFormat.getDateInstance(2, I18nUtil.getConnectionLocale(this.getConnectionInfo()));
                }
            } else if (xAxis instanceof NumberAxis) {
                if (!standardItemLabelConfiguration.getNumberFormatStr().isEmpty()) {
                    xNumberFormat = new DecimalFormat(standardItemLabelConfiguration.getNumberFormatStr());
                } else {
                    xNumberFormat = ((NumberAxis)xAxis).getNumberFormatOverride();
                    if (xNumberFormat == null) {
                        xNumberFormat = NumberFormat.getNumberInstance(I18nUtil.getConnectionLocale(this.getConnectionInfo()));
                    }
                }
            }
            String labelStr = labelConf.getStandardItemLabelConfiguration().getLabelString();
            standardXYItemLabelGenerator = xNumberFormat != null && yNumberFormat != null ? new StandardXYItemLabelGenerator(labelStr, xNumberFormat, yNumberFormat) : (xNumberFormat != null && yDateFormat != null ? new StandardXYItemLabelGenerator(labelStr, xNumberFormat, yDateFormat) : (xDateFormat != null && yDateFormat != null ? new StandardXYItemLabelGenerator(labelStr, xDateFormat, yDateFormat) : (xDateFormat != null && yNumberFormat != null ? new StandardXYItemLabelGenerator(labelStr, xDateFormat, yNumberFormat) : new StandardXYItemLabelGenerator())));
        } else if (labelConf.getLabelType().equals("Expression")) {
            String labelStr = labelConf.getConfigurableLabelConfiguration().getLabelString();
            standardXYItemLabelGenerator = new ConfigurableXYItemLabelGenerator(labelStr, this.getChartBindingMap(), traceableSeriesGroup);
        }
        return standardXYItemLabelGenerator;
    }

    private AbstractXYItemRenderer getAbstractXYItemRenderer(RendererConfiguration rendererConf, TraceableSeriesGroup traceableSeriesGroup, SeriesGroupConfiguration seriesGroupConf, RendererConfiguration.RendererType rendererType) {
        XYLineAndShapeRenderer returnRenderer;
        if (rendererType == RendererConfiguration.RendererType.XY_LINE_AND_SHAPE) {
            returnRenderer = seriesGroupConf.isItemStylesEnabled() ? new ConfigurableXYLineAndShapeRenderer(rendererConf, seriesGroupConf, traceableSeriesGroup, this.getConnectionInfo(), this.rangeAxisMap, this.domainAxisMap, this.getChartBindingMap()) : new XYLineAndShapeRenderer();
        } else if (rendererType == RendererConfiguration.RendererType.XY_STEP) {
            returnRenderer = new XYStepRenderer();
        } else if (rendererType == RendererConfiguration.RendererType.XY_BAR) {
            returnRenderer = new XYBarRenderer();
        } else if (rendererType == RendererConfiguration.RendererType.Y_INTERVAL) {
            returnRenderer = new YIntervalRenderer();
        } else {
            throw new IllegalArgumentException("Unknown renderer type: " + (Object)((Object)rendererType));
        }
        return returnRenderer;
    }

    public XYPlot getPlot() {
        return this.xyPlot;
    }

    @Override
    protected void initRenderer(int seriesIndex, int itemIndex, String rendererId, ItemStyleConfiguration itemStyleConf) {
        XYItemRenderer r = this.rendererMap.get(rendererId);
        Shape defaultShape = r.getItemShape(seriesIndex, itemIndex);
        if (itemStyleConf != null) {
            itemStyleConf.getItemShapeConfiguration().setDefaultShape(defaultShape);
        }
    }

    @Override
    protected void addRangeMarker(int seriesGroupIndex, MarkerConfiguration markerConf) throws SapphireException {
        this.xyPlot.addRangeMarker(seriesGroupIndex, this.createMarker(markerConf), markerConf.getLayerType().getLayer());
    }

    @Override
    protected void addDomainMarker(int seriesGroupIndex, MarkerConfiguration markerConf) throws SapphireException {
        this.xyPlot.addDomainMarker(seriesGroupIndex, this.createMarker(markerConf), markerConf.getLayerType().getLayer());
    }

    @Override
    protected int getItemCount(int seriesGroupIndex, int seriesIndex) {
        return this.xyPlot.getDataset(seriesGroupIndex).getItemCount(seriesIndex);
    }

    @Override
    protected int getSeriesCount(int seriesGroupIndex) {
        return this.xyPlot.getDataset(seriesGroupIndex).getSeriesCount();
    }

    @Override
    protected void setRangeAxisLocation(int rangeAxisIndex, AxisLocation axisLocation) {
        this.xyPlot.setRangeAxisLocation(rangeAxisIndex, axisLocation);
    }

    @Override
    protected void setDomainAxisLocation(int domainAxisIndex, AxisLocation axisLocation) {
        this.xyPlot.setDomainAxisLocation(domainAxisIndex, axisLocation);
    }

    @Override
    protected void mapDatasetToRangeAxis(int index, String rangeAxisId) {
        this.xyPlot.mapDatasetToRangeAxis(index, this.xyPlot.getRangeAxisIndex(this.rangeAxisMap.get(rangeAxisId)));
    }

    @Override
    protected void mapDatasetToDomainAxis(int index, String domainAxisId) {
        this.xyPlot.mapDatasetToDomainAxis(index, this.xyPlot.getDomainAxisIndex(this.domainAxisMap.get(domainAxisId)));
    }

    @Override
    protected void setPlotRangeAxisFromCache(int index, String rangeAxisId) {
        this.xyPlot.setRangeAxis(index, this.rangeAxisMap.get(rangeAxisId));
    }

    @Override
    protected void setPlotDomainAxisFromCache(int index, String domainAxisId) {
        this.xyPlot.setDomainAxis(index, this.domainAxisMap.get(domainAxisId));
    }

    @Override
    protected void cacheDomainAxis(String domainAxisId, AxisConfiguration axisConf) {
        this.domainAxisMap.put(domainAxisId, this.createValueAxis(axisConf, true));
    }

    @Override
    protected void cacheRangeAxis(String rangeAxisId, AxisConfiguration axisConf) {
        this.rangeAxisMap.put(rangeAxisId, this.createValueAxis(axisConf, false));
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
        this.xyPlot.setRenderer(index, this.rendererMap.get(rendererId));
    }

    @Override
    protected void cacheRenderer(String rendererId, RendererConfiguration plotRendererConf, TraceableSeriesGroup traceableSeriesGroup, SeriesGroupConfiguration seriesGroupConf) {
        this.rendererMap.put(rendererId, this.createRenderer(plotRendererConf, traceableSeriesGroup, seriesGroupConf));
    }

    @Override
    protected boolean hasCachedRenderer(String rendererId) {
        return this.rendererMap.get(rendererId) != null;
    }

    @Override
    protected void setPlotDataset(int index, Dataset seriesGroup) {
        this.xyPlot.setDataset(index, (XYDataset)seriesGroup);
    }
}

