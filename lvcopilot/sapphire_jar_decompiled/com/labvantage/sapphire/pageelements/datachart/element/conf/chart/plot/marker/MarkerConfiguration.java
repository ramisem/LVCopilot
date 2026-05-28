/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.ui.Layer
 *  org.jfree.ui.RectangleAnchor
 *  org.jfree.ui.TextAnchor
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.FontConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.RectangleAnchorType;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TextAnchorType;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.CategoryMarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.IntervalMarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.marker.ValueMarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import sapphire.xml.PropertyList;

public final class MarkerConfiguration
implements Serializable {
    private static final String DEFAULT_MARKER_ID = "";
    private static final String DEFAULT_MARKER_LABEL = "";
    private static final String DEFAULT_VISIBLE = "Y";
    private static final String DEFAULT_ALPHA = "1";
    private static final String DEFAULT_LAYER = LayerType.BACKGROUND.getName();
    private static final String DEFAULT_LABEL_ANCHOR = RectangleAnchorType.fromRectangleAnchor(RectangleAnchor.TOP_LEFT).getName();
    private static final String DEFAULT_LABEL_TEXT_ANCHOR = TextAnchorType.fromTextAnchor(TextAnchor.CENTER).getName();
    private final ComponentPlotConfiguration parent;
    private final String markerId;
    private final StringExpression label;
    private final PaintConfiguration paintConf;
    private final StrokeConfiguration strokeConf;
    private final float alpha;
    private final MarkerType markerType;
    private final BooleanExpression visible;
    private final CategoryMarkerConfiguration categoryMarkerConf;
    private final ValueMarkerConfiguration valueMarkerConf;
    private final IntervalMarkerConfiguration intervalMarkerConf;
    private final LayerType layerType;
    private final RectangleAnchorType labelAnchor;
    private final TextAnchorType labelTextAnchor;
    private final FontConfiguration labelFontConf;
    private final PaintConfiguration labelPaintConf;

    public MarkerConfiguration(PropertyList markerProps, MarkerType defaultMarkerType, ComponentPlotConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (markerProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.markerId = markerProps.getProperty("markerid", "");
        this.markerType = MarkerType.fromString(markerProps.getProperty("markertype", defaultMarkerType.getName()));
        this.layerType = LayerType.fromString(markerProps.getProperty("layertype", DEFAULT_LAYER));
        this.label = new StringExpression(markerProps.getProperty("label", ""));
        this.labelAnchor = RectangleAnchorType.fromString(markerProps.getProperty("labelanchor", DEFAULT_LABEL_ANCHOR));
        this.labelTextAnchor = TextAnchorType.fromString(markerProps.getProperty("labeltextanchor", DEFAULT_LABEL_TEXT_ANCHOR));
        this.labelFontConf = new FontConfiguration(markerProps.getPropertyListNotNull("labelfontprops"), this.getParent().getParent().getParent());
        this.labelPaintConf = new PaintConfiguration(markerProps.getPropertyListNotNull("labelpaintprops"));
        this.categoryMarkerConf = this.markerType == MarkerType.CATEGORY ? new CategoryMarkerConfiguration(markerProps.getPropertyListNotNull("categorymarkerprops"), this) : null;
        this.valueMarkerConf = this.markerType == MarkerType.VALUE ? new ValueMarkerConfiguration(markerProps.getPropertyListNotNull("valuemarkerprops"), this) : null;
        this.intervalMarkerConf = this.markerType == MarkerType.INTERVAL ? new IntervalMarkerConfiguration(markerProps.getPropertyListNotNull("intervalmarkerprops"), this) : null;
        this.visible = new BooleanExpression(markerProps.getProperty("visible", DEFAULT_VISIBLE));
        this.paintConf = new PaintConfiguration(markerProps.getPropertyListNotNull("paintprops"));
        this.strokeConf = new StrokeConfiguration(markerProps.getPropertyListNotNull("strokeprops"));
        this.alpha = Float.parseFloat(markerProps.getProperty("alpha", DEFAULT_ALPHA));
    }

    public MarkerConfiguration(MarkerConfiguration copy, ComponentPlotConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.markerId = copy.markerId;
        this.markerType = copy.markerType;
        this.layerType = copy.layerType;
        this.label = new StringExpression(copy.label);
        this.labelAnchor = copy.labelAnchor;
        this.labelTextAnchor = copy.labelTextAnchor;
        this.labelFontConf = new FontConfiguration(copy.labelFontConf);
        this.labelPaintConf = new PaintConfiguration(copy.labelPaintConf);
        this.categoryMarkerConf = copy.categoryMarkerConf != null ? new CategoryMarkerConfiguration(copy.categoryMarkerConf, this) : null;
        this.valueMarkerConf = copy.valueMarkerConf != null ? new ValueMarkerConfiguration(copy.valueMarkerConf, this) : null;
        this.intervalMarkerConf = copy.intervalMarkerConf != null ? new IntervalMarkerConfiguration(copy.intervalMarkerConf, this) : null;
        this.alpha = copy.alpha;
        this.paintConf = new PaintConfiguration(copy.paintConf);
        this.strokeConf = new StrokeConfiguration(copy.strokeConf);
        this.visible = copy.visible;
        this.parent = parent;
    }

    public PaintConfiguration getLabelPaintConfiguration() {
        return this.labelPaintConf;
    }

    public FontConfiguration getLabelFontConfiguration() {
        return this.labelFontConf;
    }

    public RectangleAnchorType getLabelAnchor() {
        return this.labelAnchor;
    }

    public TextAnchorType getLabelTextAnchor() {
        return this.labelTextAnchor;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public PaintConfiguration getPaintConfiguration() {
        return this.paintConf;
    }

    public StrokeConfiguration getStrokeConfiguration() {
        return this.strokeConf;
    }

    public BooleanExpression isVisible() {
        return this.visible;
    }

    public String getMarkerId() {
        return this.markerId;
    }

    public ComponentPlotConfiguration getParent() {
        return this.parent;
    }

    public CategoryMarkerConfiguration getCategoryMarkerConfiguration() {
        if (this.categoryMarkerConf == null) {
            throw new IllegalStateException("Marker type is: " + (Object)((Object)this.markerType));
        }
        return this.categoryMarkerConf;
    }

    public ValueMarkerConfiguration getValueMarkerConfiguration() {
        if (this.valueMarkerConf == null) {
            throw new IllegalStateException("Marker type is: " + (Object)((Object)this.markerType));
        }
        return this.valueMarkerConf;
    }

    public IntervalMarkerConfiguration getIntervalMarkerConfiguration() {
        if (this.intervalMarkerConf == null) {
            throw new IllegalStateException("Marker type is: " + (Object)((Object)this.markerType));
        }
        return this.intervalMarkerConf;
    }

    public MarkerType getMarkerType() {
        return this.markerType;
    }

    public StringExpression getLabel() {
        return this.label;
    }

    public LayerType getLayerType() {
        return this.layerType;
    }

    public static enum LayerType {
        FOREGROUND("Foreground", Layer.FOREGROUND),
        BACKGROUND("Background", Layer.BACKGROUND);

        private final String name;
        private final Layer layer;

        private LayerType(String name, Layer layer) {
            this.name = name;
            this.layer = layer;
        }

        public static LayerType fromString(String name) {
            if (name != null) {
                for (LayerType type : LayerType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }

        public Layer getLayer() {
            return this.layer;
        }
    }

    public static enum MarkerType {
        CATEGORY("Category"),
        VALUE("Value"),
        INTERVAL("Interval");

        private final String name;

        private MarkerType(String name) {
            this.name = name;
        }

        public static MarkerType fromString(String name) {
            if (name != null) {
                for (MarkerType type : MarkerType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

