/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ShapeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DrawingSupplierConfiguration
implements Serializable {
    public static final String STANDARD_PAINT_CONF_TYPE = "Standard";
    public static final String COLOR_SCHEME_PAINT_CONF_TYPE = "Color Scheme";
    private static final String DEFAULT_PAINT_CONF_TYPE = "Standard";
    private final PlotConfiguration parent;
    private final List<PaintConfiguration> paintConfList;
    private final List<ShapeConfiguration> shapeConfList;
    private final List<StrokeConfiguration> strokeConfList;
    private final String paintConfType;
    private final String colorSchemeId;
    private final float foregroundAlpha;

    protected DrawingSupplierConfiguration(PropertyList drawingSupplierProps, PlotConfiguration parent) {
        int i;
        if (drawingSupplierProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        PropertyList paintConfProps = drawingSupplierProps.getPropertyListNotNull("paintsconfig");
        this.paintConfType = paintConfProps.getProperty("paintconftype", "Standard");
        this.colorSchemeId = paintConfProps.getProperty("colorscheme", "");
        this.foregroundAlpha = Float.parseFloat(paintConfProps.getProperty("foregroundalpha", "1.0f"));
        this.paintConfList = new ArrayList<PaintConfiguration>();
        if (this.paintConfType.equals("Standard")) {
            PropertyListCollection paintCollection = paintConfProps.getCollectionNotNull("paintcollection");
            for (i = 0; i < paintCollection.size(); ++i) {
                PropertyList paintProps = paintCollection.getPropertyList(i);
                this.paintConfList.add(new PaintConfiguration(paintProps));
            }
        }
        this.shapeConfList = new ArrayList<ShapeConfiguration>();
        PropertyListCollection shapeCollections = drawingSupplierProps.getCollectionNotNull("shapecollection");
        for (i = 0; i < shapeCollections.size(); ++i) {
            PropertyList shapeProps = shapeCollections.getPropertyList(i);
            this.shapeConfList.add(new ShapeConfiguration(shapeProps.getPropertyList("shapeprops")));
        }
        this.strokeConfList = new ArrayList<StrokeConfiguration>();
        PropertyListCollection strokeCollections = drawingSupplierProps.getCollectionNotNull("strokecollection");
        for (int i2 = 0; i2 < strokeCollections.size(); ++i2) {
            PropertyList strokeProps = strokeCollections.getPropertyList(i2);
            this.strokeConfList.add(new StrokeConfiguration(strokeProps.getPropertyList("strokeprops")));
        }
        this.parent = parent;
    }

    protected DrawingSupplierConfiguration(DrawingSupplierConfiguration copy, PlotConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        this.parent = parent;
        this.paintConfList = new ArrayList<PaintConfiguration>();
        for (PaintConfiguration paintConf : copy.paintConfList) {
            this.paintConfList.add(new PaintConfiguration(paintConf));
        }
        this.shapeConfList = new ArrayList<ShapeConfiguration>();
        for (ShapeConfiguration shapeConf : copy.shapeConfList) {
            this.shapeConfList.add(new ShapeConfiguration(shapeConf));
        }
        this.strokeConfList = new ArrayList<StrokeConfiguration>();
        for (StrokeConfiguration strokeConf : copy.strokeConfList) {
            this.strokeConfList.add(new StrokeConfiguration(strokeConf));
        }
        this.paintConfType = copy.paintConfType;
        this.colorSchemeId = copy.colorSchemeId;
        this.foregroundAlpha = copy.foregroundAlpha;
    }

    public List<PaintConfiguration> getPaintConfigurationList() {
        return this.paintConfList;
    }

    public List<ShapeConfiguration> getShapeConfigurationList() {
        return this.shapeConfList;
    }

    public List<StrokeConfiguration> getStrokeConfigurationList() {
        return this.strokeConfList;
    }

    public String getPaintConfType() {
        return this.paintConfType;
    }

    public String getColorSchemeId() {
        return this.colorSchemeId;
    }

    public PlotConfiguration getParent() {
        return this.parent;
    }

    public float getForegroundAlpha() {
        return this.foregroundAlpha;
    }
}

