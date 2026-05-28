/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ShapeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.CategoryItemRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.SeriesStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYItemRendererConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class RendererConfiguration
implements ConfigurationListItem,
Serializable {
    private static final String DEFAULT_SERIES_VISIBLE = "Y";
    private static final String DEFAULT_SERIES_VISIBLE_IN_LEGEND = "Y";
    private static final String DEFAULT_RENDERER_ID = "";
    private static final String DEFAULT_AUTO_POPULATE_SERIES_PAINT = "Y";
    private static final String DEFAULT_AUTO_POPULATE_SERIES_OUTLINE_PAINT = "N";
    private static final String DEFAULT_AUTO_POPULATE_SERIES_FILL_PAINT = "N";
    private static final String DEFAULT_AUTO_POPULATE_SERIES_SHAPE = "Y";
    private static final String DEFAULT_AUTO_POPULATE_SERIES_STROKE = "Y";
    private final RendererType rendererType;
    private final ComponentPlotConfiguration parent;
    private final List<SeriesStyleConfiguration> seriesStyleConfList;
    private final String rendererId;
    private final PaintConfiguration basePaintConf;
    private final PaintConfiguration baseFillPaintConf;
    private final PaintConfiguration baseOutlinePaintConf;
    private final boolean autoPopulateSeriesPaint;
    private final boolean autoPopulateSeriesFillPaint;
    private final boolean autoPopulateSeriesOutlinePaint;
    private final XYItemRendererConfiguration baseXYItemRendererConf;
    private final CategoryItemRendererConfiguration baseCategoryItemRendererConf;
    private final ShapeConfiguration baseShapeConf;
    private final boolean autoPopulateSeriesShape;
    private final StrokeConfiguration baseStrokeConf;
    private final boolean autoPopulateSeriesStroke;
    private final boolean baseSeriesVisible;
    private final boolean baseSeriesVisibleInLegend;
    private final LabelConfiguration baseLabelConfiguration;
    private int nextRendererSeriesStyleConfIndex;

    public RendererConfiguration(PropertyList rendererProps, RendererType defaultRendererType, ComponentPlotConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (rendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.rendererId = rendererProps.getProperty("rendererid", DEFAULT_RENDERER_ID);
        this.rendererType = RendererType.fromString(rendererProps.getProperty("renderertype", defaultRendererType.getName()));
        PlotConfiguration.PlotType plotType = parent.getParent().getPlotType();
        if (this.rendererType.getPlotType() != plotType) {
            throw new IllegalArgumentException("Plot type is " + (Object)((Object)plotType) + " but renderer " + (Object)((Object)this.rendererType) + " suggests plot type " + (Object)((Object)this.rendererType.getPlotType()));
        }
        this.baseXYItemRendererConf = this.rendererType.getPlotType() == PlotConfiguration.PlotType.XY_PLOT ? new XYItemRendererConfiguration(rendererProps.getPropertyListNotNull("xyitemrendererprops"), this.rendererType) : null;
        this.baseCategoryItemRendererConf = this.rendererType.getPlotType() == PlotConfiguration.PlotType.CATEGORY_PLOT ? new CategoryItemRendererConfiguration(rendererProps.getPropertyListNotNull("categoryitemrendererprops"), this.rendererType) : null;
        this.seriesStyleConfList = new ArrayList<SeriesStyleConfiguration>();
        PropertyListCollection seriesStyleCollection = rendererProps.getCollectionNotNull("seriesstylecollection");
        for (int i = 0; i < seriesStyleCollection.size(); ++i) {
            PropertyList seriesStyleProps = seriesStyleCollection.getPropertyList(i);
            this.seriesStyleConfList.add(new SeriesStyleConfiguration(seriesStyleProps, this));
        }
        if (this.seriesStyleConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.seriesStyleConfList) == 0) {
            this.seriesStyleConfList.add(new SeriesStyleConfiguration(new PropertyList(), this));
        }
        this.basePaintConf = new PaintConfiguration(rendererProps.getPropertyListNotNull("paintprops"));
        this.baseFillPaintConf = new PaintConfiguration(rendererProps.getPropertyListNotNull("fillpaintprops"));
        this.baseOutlinePaintConf = new PaintConfiguration(rendererProps.getPropertyListNotNull("outlinepaintprops"));
        this.autoPopulateSeriesPaint = rendererProps.getProperty("autopopulateseriespaint", "Y").toLowerCase().startsWith("y");
        this.autoPopulateSeriesOutlinePaint = rendererProps.getProperty("autopopulateseriesoutlinepaint", "N").toLowerCase().startsWith("y");
        this.autoPopulateSeriesFillPaint = rendererProps.getProperty("autopopulateseriesfillpaint", "N").toLowerCase().startsWith("y");
        this.nextRendererSeriesStyleConfIndex = 0;
        this.baseShapeConf = new ShapeConfiguration(rendererProps.getPropertyListNotNull("shapeprops"));
        this.autoPopulateSeriesShape = rendererProps.getProperty("autopopulateseriesshape", "Y").toLowerCase().startsWith("y");
        this.baseStrokeConf = new StrokeConfiguration(rendererProps.getPropertyListNotNull("strokeprops"));
        this.autoPopulateSeriesStroke = rendererProps.getProperty("autopopulateseriesstroke", "Y").toLowerCase().startsWith("y");
        this.baseSeriesVisible = rendererProps.getProperty("seriesvisible", "Y").toLowerCase().startsWith("y");
        this.baseSeriesVisibleInLegend = rendererProps.getProperty("seriesvisibleinlegend", "Y").toLowerCase().startsWith("y");
        this.baseLabelConfiguration = new LabelConfiguration(rendererProps.getPropertyListNotNull("labelprops"), this);
    }

    public RendererConfiguration(RendererConfiguration copy, String rendererId, ComponentPlotConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Source is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (rendererId == null) {
            throw new IllegalArgumentException("Renderer ID is null");
        }
        this.parent = parent;
        this.rendererId = rendererId;
        this.rendererType = copy.rendererType;
        this.baseXYItemRendererConf = copy.baseXYItemRendererConf != null ? new XYItemRendererConfiguration(copy.baseXYItemRendererConf) : null;
        this.baseCategoryItemRendererConf = copy.baseCategoryItemRendererConf != null ? new CategoryItemRendererConfiguration(copy.baseCategoryItemRendererConf) : null;
        this.seriesStyleConfList = new ArrayList<SeriesStyleConfiguration>();
        for (SeriesStyleConfiguration rendererSeriesStyleConf : copy.seriesStyleConfList) {
            this.seriesStyleConfList.add(new SeriesStyleConfiguration(rendererSeriesStyleConf, this));
        }
        this.basePaintConf = new PaintConfiguration(copy.basePaintConf);
        this.baseFillPaintConf = new PaintConfiguration(copy.baseFillPaintConf);
        this.baseOutlinePaintConf = new PaintConfiguration(copy.baseOutlinePaintConf);
        this.autoPopulateSeriesPaint = copy.autoPopulateSeriesPaint;
        this.autoPopulateSeriesFillPaint = copy.autoPopulateSeriesFillPaint;
        this.autoPopulateSeriesOutlinePaint = copy.autoPopulateSeriesOutlinePaint;
        this.baseShapeConf = copy.baseShapeConf;
        this.autoPopulateSeriesShape = copy.autoPopulateSeriesShape;
        this.baseStrokeConf = copy.baseStrokeConf;
        this.autoPopulateSeriesStroke = copy.autoPopulateSeriesStroke;
        this.baseSeriesVisible = copy.baseSeriesVisible;
        this.baseSeriesVisibleInLegend = copy.baseSeriesVisibleInLegend;
        this.baseLabelConfiguration = new LabelConfiguration(copy.baseLabelConfiguration, this);
    }

    public SeriesStyleConfiguration getSeriesStyleConfiguration(String seriesStyleId) {
        SeriesStyleConfiguration returnConf = ConfigurationUtil.getConf(this.seriesStyleConfList, seriesStyleId);
        if (returnConf == null) {
            int index = ConfigurationUtil.getAnonymousConf(this.seriesStyleConfList, this.nextRendererSeriesStyleConfIndex);
            ++this.nextRendererSeriesStyleConfIndex;
            returnConf = new SeriesStyleConfiguration(this.seriesStyleConfList.get(index), seriesStyleId, this);
            this.seriesStyleConfList.add(returnConf);
        }
        return returnConf;
    }

    public boolean isAutoPopulateSeriesPaint() {
        return this.autoPopulateSeriesPaint;
    }

    public boolean isAutoPopulateSeriesFillPaint() {
        return this.autoPopulateSeriesFillPaint;
    }

    public boolean isAutoPopulateSeriesOutlinePaint() {
        return this.autoPopulateSeriesOutlinePaint;
    }

    public PaintConfiguration getBasePaintConfiguration() {
        return this.basePaintConf;
    }

    public PaintConfiguration getBaseFillPaintConf() {
        return this.baseFillPaintConf;
    }

    public PaintConfiguration getBaseOutlinePaintConf() {
        return this.baseOutlinePaintConf;
    }

    public ShapeConfiguration getBaseShapeConfiguration() {
        return this.baseShapeConf;
    }

    public boolean isAutoPopulateSeriesShape() {
        return this.autoPopulateSeriesShape;
    }

    public StrokeConfiguration getBaseStrokeConf() {
        return this.baseStrokeConf;
    }

    public boolean isAutoPopulateSeriesStroke() {
        return this.autoPopulateSeriesStroke;
    }

    public boolean isBaseSeriesVisible() {
        return this.baseSeriesVisible;
    }

    public boolean isBaseSeriesVisibleInLegend() {
        return this.baseSeriesVisibleInLegend;
    }

    public XYItemRendererConfiguration getBaseXYItemRendererConfiguration() {
        if (this.baseXYItemRendererConf == null) {
            throw new IllegalStateException("Renderer plot type is: " + (Object)((Object)this.getParent().getParent().getPlotType()));
        }
        return this.baseXYItemRendererConf;
    }

    public CategoryItemRendererConfiguration getBaseCategoryItemRendererConfiguration() {
        if (this.baseCategoryItemRendererConf == null) {
            throw new IllegalStateException("Renderer plot type is: " + (Object)((Object)this.getParent().getParent().getPlotType()));
        }
        return this.baseCategoryItemRendererConf;
    }

    public String getRendererId() {
        return this.rendererId;
    }

    public RendererType getRendererType() {
        return this.rendererType;
    }

    public ComponentPlotConfiguration getParent() {
        return this.parent;
    }

    @Override
    public String getId() {
        return this.getRendererId();
    }

    public List<SeriesStyleConfiguration> getSeriesStyleConfList() {
        return this.seriesStyleConfList;
    }

    public LabelConfiguration getBaseLabelConfiguration() {
        return this.baseLabelConfiguration;
    }

    public static enum RendererType {
        XY_LINE_AND_SHAPE("XY Line and Shape", PlotConfiguration.PlotType.XY_PLOT),
        XY_STEP("XY Step", PlotConfiguration.PlotType.XY_PLOT),
        LINE_AND_SHAPE("Line and Shape", PlotConfiguration.PlotType.CATEGORY_PLOT),
        XY_BAR("XY Bar", PlotConfiguration.PlotType.XY_PLOT),
        BAR("Bar", PlotConfiguration.PlotType.CATEGORY_PLOT),
        BAR_3D("Bar (3D)", PlotConfiguration.PlotType.CATEGORY_PLOT),
        STACKED_BAR("Stacked Bar", PlotConfiguration.PlotType.CATEGORY_PLOT),
        STACKED_BAR_3D("Stacked Bar (3D)", PlotConfiguration.PlotType.CATEGORY_PLOT),
        Y_INTERVAL("Y Interval", PlotConfiguration.PlotType.XY_PLOT);

        private final String name;
        private final PlotConfiguration.PlotType plotType;

        private RendererType(String name, PlotConfiguration.PlotType plotType) {
            this.name = name;
            this.plotType = plotType;
        }

        public static RendererType fromString(String name) {
            if (name != null) {
                for (RendererType type : RendererType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }

        public PlotConfiguration.PlotType getPlotType() {
            return this.plotType;
        }
    }
}

