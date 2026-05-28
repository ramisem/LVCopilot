/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.plot.DrawingSupplier
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ShapeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.CategoryItemRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYItemRendererConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.plot.DrawingSupplier;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SeriesStyleConfiguration
implements ConfigurationListItem,
Serializable {
    private static final String DEFAULT_SERIES_STYLE_ID = "";
    private final RendererConfiguration parent;
    private final String seriesStyleId;
    private final PaintConfiguration seriesPaintConf;
    private final PaintConfiguration seriesOutlinePaintConf;
    private final PaintConfiguration seriesFillPaintConf;
    private final Boolean seriesVisible;
    private final Boolean seriesVisibleInLegend;
    private final XYItemRendererConfiguration seriesXYItemRendererConf;
    private final StrokeConfiguration seriesStrokeConf;
    private final ShapeConfiguration seriesShapeConf;
    private final LabelConfiguration seriesLabelConf;
    private final CategoryItemRendererConfiguration seriesCategoryItemRendererConf;
    private final List<ItemStyleConfiguration> itemStyleConfList;
    private Map<List<String>, ItemStyleConfiguration> actualItemStyleConfMap;
    private Map<List<String>, List<String>> appliedItemStyleConfMap;

    public SeriesStyleConfiguration(PropertyList seriesStyleProps, RendererConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (seriesStyleProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        RendererConfiguration.RendererType rendererType = parent.getRendererType();
        PlotConfiguration.PlotType plotType = rendererType.getPlotType();
        if (plotType == PlotConfiguration.PlotType.XY_PLOT) {
            XYItemRendererConfiguration defaultXYItemRendererConf = parent.getBaseXYItemRendererConfiguration();
            this.seriesXYItemRendererConf = new XYItemRendererConfiguration(seriesStyleProps.getPropertyListNotNull("xyitemrendererprops"), rendererType, defaultXYItemRendererConf);
        } else {
            this.seriesXYItemRendererConf = null;
        }
        if (plotType == PlotConfiguration.PlotType.CATEGORY_PLOT) {
            CategoryItemRendererConfiguration defaultCategoryItemRendererConf = parent.getBaseCategoryItemRendererConfiguration();
            this.seriesCategoryItemRendererConf = new CategoryItemRendererConfiguration(seriesStyleProps.getPropertyListNotNull("categoryitemrendererprops"), rendererType, defaultCategoryItemRendererConf);
        } else {
            this.seriesCategoryItemRendererConf = null;
        }
        this.itemStyleConfList = new ArrayList<ItemStyleConfiguration>();
        PropertyListCollection seriesStyleCollection = seriesStyleProps.getCollectionNotNull("itemstylecollection");
        for (int i = 0; i < seriesStyleCollection.size(); ++i) {
            PropertyList itemStyleProps = seriesStyleCollection.getPropertyList(i);
            this.itemStyleConfList.add(new ItemStyleConfiguration(itemStyleProps, this));
        }
        this.resetActualItemStyleConfigurations();
        this.seriesStyleId = seriesStyleProps.getProperty("seriesstyleid", DEFAULT_SERIES_STYLE_ID);
        this.seriesPaintConf = new PaintConfiguration(seriesStyleProps.getPropertyListNotNull("paintprops"));
        this.seriesFillPaintConf = new PaintConfiguration(seriesStyleProps.getPropertyListNotNull("fillpaintprops"));
        this.seriesOutlinePaintConf = new PaintConfiguration(seriesStyleProps.getPropertyListNotNull("outlinepaintprops"));
        this.seriesShapeConf = new ShapeConfiguration(seriesStyleProps.getPropertyListNotNull("shapeprops"));
        this.seriesStrokeConf = new StrokeConfiguration(seriesStyleProps.getPropertyListNotNull("strokeprops"));
        String seriesVisibleStr = seriesStyleProps.getProperty("seriesvisible", DEFAULT_SERIES_STYLE_ID);
        this.seriesVisible = seriesVisibleStr.length() > 0 ? Boolean.valueOf(seriesVisibleStr.toLowerCase().startsWith("y")) : null;
        String seriesVisibleInLegendStr = seriesStyleProps.getProperty("seriesvisibleinlegend", DEFAULT_SERIES_STYLE_ID);
        this.seriesVisibleInLegend = seriesVisibleInLegendStr.length() > 0 ? Boolean.valueOf(seriesVisibleInLegendStr.toLowerCase().startsWith("y")) : null;
        this.seriesLabelConf = new LabelConfiguration(seriesStyleProps.getPropertyListNotNull("labelprops"), this.getParent());
    }

    public SeriesStyleConfiguration(SeriesStyleConfiguration copy, RendererConfiguration parent) {
        this(copy, copy.seriesStyleId, parent);
    }

    public SeriesStyleConfiguration(SeriesStyleConfiguration copy, String seriesStyleId, RendererConfiguration parent) {
        this.seriesStyleId = seriesStyleId;
        this.parent = parent;
        this.seriesPaintConf = new PaintConfiguration(copy.seriesPaintConf);
        this.seriesOutlinePaintConf = new PaintConfiguration(copy.seriesOutlinePaintConf);
        this.seriesFillPaintConf = new PaintConfiguration(copy.seriesFillPaintConf);
        this.seriesShapeConf = new ShapeConfiguration(copy.seriesShapeConf);
        this.seriesStrokeConf = new StrokeConfiguration(copy.seriesStrokeConf);
        this.seriesVisible = copy.seriesVisible;
        this.seriesVisibleInLegend = copy.seriesVisibleInLegend;
        this.seriesXYItemRendererConf = copy.seriesXYItemRendererConf != null ? new XYItemRendererConfiguration(copy.seriesXYItemRendererConf) : null;
        this.seriesCategoryItemRendererConf = copy.seriesCategoryItemRendererConf != null ? new CategoryItemRendererConfiguration(copy.seriesCategoryItemRendererConf) : null;
        this.resetActualItemStyleConfigurations();
        this.itemStyleConfList = new ArrayList<ItemStyleConfiguration>();
        for (ItemStyleConfiguration itemStyleConf : copy.itemStyleConfList) {
            this.itemStyleConfList.add(new ItemStyleConfiguration(itemStyleConf, this));
        }
        this.seriesLabelConf = new LabelConfiguration(copy.seriesLabelConf, this.getParent());
    }

    public ItemStyleConfiguration getItemStyleConfiguration(String seriesGroupId, String seriesId, int itemIndex) {
        ArrayList<String> itemKeyList = new ArrayList<String>();
        itemKeyList.add(seriesGroupId);
        itemKeyList.add(seriesId);
        itemKeyList.add(String.valueOf(itemIndex));
        List<String> actualItemStyleConfIdList = this.appliedItemStyleConfMap.get(itemKeyList);
        return this.actualItemStyleConfMap.get(actualItemStyleConfIdList);
    }

    public ItemStyleConfiguration getItemStyleConfiguration(String itemStyleId) {
        ItemStyleConfiguration returnConf = ConfigurationUtil.getConf(this.itemStyleConfList, itemStyleId);
        if (returnConf == null) {
            throw new IllegalArgumentException("Unknown item style ID: " + itemStyleId);
        }
        return returnConf;
    }

    public XYItemRendererConfiguration getSeriesXYItemRendererConfiguration() {
        if (this.seriesXYItemRendererConf == null) {
            throw new IllegalStateException("Renderer plot type is: " + (Object)((Object)this.parent.getRendererType().getPlotType()));
        }
        return this.seriesXYItemRendererConf;
    }

    public CategoryItemRendererConfiguration getSeriesCategoryItemRendererConf() {
        if (this.seriesCategoryItemRendererConf == null) {
            throw new IllegalStateException("Renderer plot type is: " + (Object)((Object)this.parent.getRendererType().getPlotType()));
        }
        return this.seriesCategoryItemRendererConf;
    }

    public PaintConfiguration getSeriesOutlinePaintConfiguration() {
        if (!this.seriesOutlinePaintConf.hasPaint() && this.getParent().isAutoPopulateSeriesOutlinePaint() && this.seriesPaintConf.hasPaint()) {
            this.seriesOutlinePaintConf.setPaint(this.seriesPaintConf.getPaint());
        }
        return this.seriesOutlinePaintConf;
    }

    public PaintConfiguration getSeriesFillPaintConfiguration() {
        if (!this.seriesFillPaintConf.hasPaint() && this.getParent().isAutoPopulateSeriesFillPaint() && this.seriesPaintConf.hasPaint()) {
            this.seriesFillPaintConf.setPaint(this.seriesPaintConf.getPaint());
        }
        return this.seriesFillPaintConf;
    }

    public PaintConfiguration getSeriesPaintConfiguration() {
        if (!this.seriesPaintConf.hasPaint() && this.getParent().isAutoPopulateSeriesPaint()) {
            DrawingSupplier drawingSupplier = this.getParent().getParent().getParent().getDrawingSupplier();
            this.seriesPaintConf.setPaint(drawingSupplier.getNextPaint());
        }
        return this.seriesPaintConf;
    }

    public ShapeConfiguration getSeriesShapeConfiguration() {
        if (!this.seriesShapeConf.hasShape() && this.getParent().isAutoPopulateSeriesShape()) {
            DrawingSupplier drawingSupplier = this.getParent().getParent().getParent().getDrawingSupplier();
            this.seriesShapeConf.setShape(drawingSupplier.getNextShape());
        }
        return this.seriesShapeConf;
    }

    public StrokeConfiguration getSeriesStrokeConfiguration() {
        if (!this.seriesStrokeConf.hasStroke() && this.getParent().isAutoPopulateSeriesStroke()) {
            DrawingSupplier drawingSupplier = this.getParent().getParent().getParent().getDrawingSupplier();
            this.seriesStrokeConf.setStroke(drawingSupplier.getNextStroke());
        }
        return this.seriesStrokeConf;
    }

    public Boolean getSeriesVisible() {
        return this.seriesVisible;
    }

    public Boolean getSeriesVisibleInLegend() {
        return this.seriesVisibleInLegend;
    }

    public String getSeriesStyleId() {
        return this.seriesStyleId;
    }

    public RendererConfiguration getParent() {
        return this.parent;
    }

    @Override
    public String getId() {
        return this.getSeriesStyleId();
    }

    public LabelConfiguration getSeriesLabelConfiguration() {
        return this.seriesLabelConf;
    }

    public List<ItemStyleConfiguration> getItemStyleConfList() {
        return this.itemStyleConfList;
    }

    public void addActualItemStyleConfiguration(List<String> actualItemStyleConfIdList, ItemStyleConfiguration itemStyleConfiguration, String seriesGroupId, String seriesId, int itemIndex) {
        this.actualItemStyleConfMap.put(actualItemStyleConfIdList, itemStyleConfiguration);
        ArrayList<String> itemKeyList = new ArrayList<String>();
        itemKeyList.add(seriesGroupId);
        itemKeyList.add(seriesId);
        itemKeyList.add(String.valueOf(itemIndex));
        this.appliedItemStyleConfMap.put(itemKeyList, actualItemStyleConfIdList);
    }

    public Map<List<String>, ItemStyleConfiguration> getActualItemStyleConfMap() {
        return this.actualItemStyleConfMap;
    }

    public void resetActualItemStyleConfigurations() {
        this.actualItemStyleConfMap = new HashMap<List<String>, ItemStyleConfiguration>();
        this.appliedItemStyleConfMap = new HashMap<List<String>, List<String>>();
    }
}

