/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ShapeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.CategoryItemRendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.SeriesStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.XYItemRendererConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class ItemStyleConfiguration
implements ConfigurationListItem,
Serializable {
    private static final String DEFAULT_ITEM_STYLE_ID = "";
    private final SeriesStyleConfiguration parent;
    private final String itemStyleId;
    private final XYItemRendererConfiguration itemXYItemRendererConf;
    private final CategoryItemRendererConfiguration categoryItemRendererConf;
    private final PaintConfiguration itemPaintConf;
    private final PaintConfiguration itemFillPaintConf;
    private final PaintConfiguration itemOutlinePaintConf;
    private final ShapeConfiguration itemShapeConf;
    private final LabelConfiguration itemLabelConfiguration;

    public ItemStyleConfiguration(PropertyList itemStyleProps, SeriesStyleConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (itemStyleProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.itemStyleId = itemStyleProps.getProperty("itemstyleid", DEFAULT_ITEM_STYLE_ID);
        RendererConfiguration.RendererType rendererType = parent.getParent().getRendererType();
        PlotConfiguration.PlotType plotType = rendererType.getPlotType();
        if (plotType == PlotConfiguration.PlotType.XY_PLOT) {
            XYItemRendererConfiguration defaultXYItemRendererConf = parent.getSeriesXYItemRendererConfiguration();
            this.itemXYItemRendererConf = new XYItemRendererConfiguration(itemStyleProps.getPropertyListNotNull("xyitemrendererprops"), rendererType, defaultXYItemRendererConf);
        } else {
            this.itemXYItemRendererConf = null;
        }
        if (plotType == PlotConfiguration.PlotType.CATEGORY_PLOT) {
            CategoryItemRendererConfiguration defaultCategoryItemRendererConf = parent.getSeriesCategoryItemRendererConf();
            this.categoryItemRendererConf = new CategoryItemRendererConfiguration(itemStyleProps.getPropertyListNotNull("categoryitemrendererprops"), rendererType, defaultCategoryItemRendererConf);
        } else {
            this.categoryItemRendererConf = null;
        }
        this.itemPaintConf = new PaintConfiguration(itemStyleProps.getPropertyListNotNull("paintprops"));
        this.itemFillPaintConf = new PaintConfiguration(itemStyleProps.getPropertyListNotNull("fillpaintprops"));
        this.itemOutlinePaintConf = new PaintConfiguration(itemStyleProps.getPropertyListNotNull("outlinepaintprops"));
        this.itemShapeConf = new ShapeConfiguration(itemStyleProps.getPropertyListNotNull("shapeprops"));
        this.itemLabelConfiguration = new LabelConfiguration(itemStyleProps.getPropertyListNotNull("labelprops"), this.getParent().getParent());
    }

    public ItemStyleConfiguration(ItemStyleConfiguration copy, SeriesStyleConfiguration parent) {
        this.itemStyleId = copy.itemStyleId;
        this.parent = parent;
        this.itemXYItemRendererConf = copy.itemXYItemRendererConf != null ? new XYItemRendererConfiguration(copy.itemXYItemRendererConf) : null;
        this.categoryItemRendererConf = copy.categoryItemRendererConf != null ? new CategoryItemRendererConfiguration(copy.categoryItemRendererConf) : null;
        this.itemPaintConf = new PaintConfiguration(copy.itemPaintConf);
        this.itemFillPaintConf = new PaintConfiguration(copy.itemFillPaintConf);
        this.itemOutlinePaintConf = new PaintConfiguration(copy.itemOutlinePaintConf);
        this.itemShapeConf = new ShapeConfiguration(copy.itemShapeConf);
        this.itemLabelConfiguration = new LabelConfiguration(copy.itemLabelConfiguration, parent.getParent());
    }

    public ItemStyleConfiguration(ItemStyleConfiguration copy, ItemStyleConfiguration override, SeriesStyleConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (override == null) {
            throw new IllegalArgumentException("Override is null");
        }
        this.parent = parent;
        String string = this.itemStyleId = copy.itemStyleId.equals(override.itemStyleId) ? copy.itemStyleId : DEFAULT_ITEM_STYLE_ID;
        this.itemXYItemRendererConf = copy.itemXYItemRendererConf != null && override.itemXYItemRendererConf != null ? new XYItemRendererConfiguration(copy.itemXYItemRendererConf, override.itemXYItemRendererConf) : (copy.itemXYItemRendererConf != null ? new XYItemRendererConfiguration(copy.itemXYItemRendererConf) : (override.itemXYItemRendererConf != null ? new XYItemRendererConfiguration(override.itemXYItemRendererConf) : null));
        this.categoryItemRendererConf = copy.categoryItemRendererConf != null && override.categoryItemRendererConf != null ? new CategoryItemRendererConfiguration(copy.categoryItemRendererConf, override.categoryItemRendererConf) : (copy.categoryItemRendererConf != null ? new CategoryItemRendererConfiguration(copy.categoryItemRendererConf) : (override.categoryItemRendererConf != null ? new CategoryItemRendererConfiguration(override.categoryItemRendererConf) : null));
        this.itemPaintConf = !override.itemPaintConf.hasPaint() ? new PaintConfiguration(copy.itemPaintConf) : new PaintConfiguration(override.itemPaintConf);
        this.itemFillPaintConf = !override.itemFillPaintConf.hasPaint() ? new PaintConfiguration(copy.itemFillPaintConf) : new PaintConfiguration(override.itemFillPaintConf);
        this.itemOutlinePaintConf = !override.itemOutlinePaintConf.hasPaint() ? new PaintConfiguration(copy.itemOutlinePaintConf) : new PaintConfiguration(override.itemOutlinePaintConf);
        this.itemShapeConf = !override.itemShapeConf.hasShapeOverride() ? new ShapeConfiguration(copy.itemShapeConf) : new ShapeConfiguration(copy.itemShapeConf, override.itemShapeConf);
        this.itemLabelConfiguration = override.itemLabelConfiguration.getLabelsVisible() != null ? new LabelConfiguration(copy.itemLabelConfiguration, parent == null ? null : parent.getParent()) : new LabelConfiguration(override.itemLabelConfiguration, parent == null ? null : parent.getParent());
    }

    public PaintConfiguration getItemPaintConfiguration() {
        return this.itemPaintConf;
    }

    public PaintConfiguration getItemFillPaintConfiguration() {
        return this.itemFillPaintConf;
    }

    public PaintConfiguration getItemOutlinePaintConfiguration() {
        return this.itemOutlinePaintConf;
    }

    public ShapeConfiguration getItemShapeConfiguration() {
        return this.itemShapeConf;
    }

    public XYItemRendererConfiguration getItemXYItemRendererConfiguration() {
        if (this.itemXYItemRendererConf == null) {
            throw new IllegalStateException("Renderer plot type is: " + (Object)((Object)this.parent.getParent().getRendererType().getPlotType()));
        }
        return this.itemXYItemRendererConf;
    }

    public CategoryItemRendererConfiguration getItemCategoryItemRendererConfiguration() {
        if (this.categoryItemRendererConf == null) {
            throw new IllegalStateException("Renderer plot type is: " + (Object)((Object)this.parent.getParent().getRendererType().getPlotType()));
        }
        return this.categoryItemRendererConf;
    }

    public String getItemStyleId() {
        return this.itemStyleId;
    }

    public SeriesStyleConfiguration getParent() {
        return this.parent;
    }

    public LabelConfiguration getItemLabelConfiguration() {
        return this.itemLabelConfiguration;
    }

    @Override
    public String getId() {
        return this.getItemStyleId();
    }
}

