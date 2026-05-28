/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.CategoryAxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.ValueAxisConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public final class AxisConfiguration
implements ConfigurationListItem,
Serializable {
    private static final String DEFAULT_AXIS_LABEL = "";
    private static final String DEFAULT_AXIS_ID = "";
    private static final String DEFAULT_LABEL_ANGLE = "0";
    private static final String DEFAULT_MINOR_TICK_MARKS_VISIBLE = "N";
    private static final String DEFAULT_VISIBLE = "Y";
    private final String axisId;
    private final AxisType axisType;
    private final StringExpression label;
    private final CategoryAxisConfiguration categoryAxisConf;
    private final ComponentPlotConfiguration parent;
    private final ValueAxisConfiguration valueAxisConf;
    private final double labelAngle;
    private final boolean minorTickMarksVisible;
    private final boolean visible;
    private final PaintConfiguration labelPaintConf;
    private final PaintConfiguration tickLabelPaintConf;
    private final PaintConfiguration tickMarkPaintConf;
    private final PaintConfiguration axisLinePaintConf;

    public AxisConfiguration(PropertyList axisProps, ConnectionInfo connectionInfo, AxisType defaultAxisType, ComponentPlotConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (axisProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        this.axisId = axisProps.getProperty("axisid", "");
        this.axisType = AxisType.fromString(axisProps.getProperty("axistype", defaultAxisType.getName()));
        this.label = new StringExpression(axisProps.getProperty("label", ""));
        this.categoryAxisConf = this.axisType == AxisType.CATEGORY ? new CategoryAxisConfiguration(axisProps.getPropertyListNotNull("categoryaxisprops"), this) : null;
        this.valueAxisConf = this.axisType == AxisType.VALUE ? new ValueAxisConfiguration(axisProps.getPropertyListNotNull("valueaxisprops"), connectionInfo, this) : null;
        this.minorTickMarksVisible = axisProps.getProperty("minortickmarksvisible", DEFAULT_MINOR_TICK_MARKS_VISIBLE).toLowerCase().startsWith("y");
        this.visible = axisProps.getProperty("visible", DEFAULT_VISIBLE).toLowerCase().startsWith("y");
        this.labelAngle = Double.valueOf(axisProps.getProperty("labelangle", DEFAULT_LABEL_ANGLE));
        this.labelPaintConf = new PaintConfiguration(axisProps.getPropertyListNotNull("labelpaintprops"));
        this.tickLabelPaintConf = new PaintConfiguration(axisProps.getPropertyListNotNull("ticklabelpaintprops"));
        this.tickMarkPaintConf = new PaintConfiguration(axisProps.getPropertyListNotNull("tickmarkpaintprops"));
        this.axisLinePaintConf = new PaintConfiguration(axisProps.getPropertyListNotNull("axislinepaintprops"));
        this.parent = parent;
    }

    public AxisConfiguration(AxisConfiguration copy, String axisId, ComponentPlotConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (axisId == null) {
            throw new IllegalArgumentException("Axis ID is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.axisId = axisId;
        this.axisType = copy.axisType;
        this.label = new StringExpression(copy.label);
        this.categoryAxisConf = copy.categoryAxisConf != null ? new CategoryAxisConfiguration(copy.categoryAxisConf, this) : null;
        this.valueAxisConf = copy.valueAxisConf != null ? new ValueAxisConfiguration(copy.valueAxisConf, this) : null;
        this.labelPaintConf = new PaintConfiguration(copy.labelPaintConf);
        this.tickLabelPaintConf = new PaintConfiguration(copy.tickLabelPaintConf);
        this.tickMarkPaintConf = new PaintConfiguration(copy.tickMarkPaintConf);
        this.axisLinePaintConf = new PaintConfiguration(copy.axisLinePaintConf);
        this.minorTickMarksVisible = copy.minorTickMarksVisible;
        this.visible = copy.visible;
        this.labelAngle = copy.labelAngle;
        this.parent = parent;
    }

    public PaintConfiguration getLabelPaintConfiguration() {
        return this.labelPaintConf;
    }

    public PaintConfiguration getTickLabelPaintConfiguration() {
        return this.tickLabelPaintConf;
    }

    public PaintConfiguration getTickMarkPaintConfiguration() {
        return this.tickMarkPaintConf;
    }

    public PaintConfiguration getAxisLinePaintConfiguration() {
        return this.axisLinePaintConf;
    }

    public boolean isMinorTickMarksVisible() {
        return this.minorTickMarksVisible;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public double getLabelAngle() {
        return this.labelAngle;
    }

    public String getAxisId() {
        return this.axisId;
    }

    public ComponentPlotConfiguration getParent() {
        return this.parent;
    }

    public CategoryAxisConfiguration getCategoryAxisConfiguration() {
        if (this.categoryAxisConf == null) {
            throw new IllegalStateException("Axis type is: " + (Object)((Object)this.axisType));
        }
        return this.categoryAxisConf;
    }

    public ValueAxisConfiguration getValueAxisConfiguration() {
        if (this.valueAxisConf == null) {
            throw new IllegalStateException("Axis type is: " + (Object)((Object)this.axisType));
        }
        return this.valueAxisConf;
    }

    public AxisType getAxisType() {
        return this.axisType;
    }

    public StringExpression getLabel() {
        return this.label;
    }

    @Override
    public String getId() {
        return this.getAxisId();
    }

    public static enum AxisType {
        CATEGORY("Category"),
        VALUE("Value");

        private final String name;

        private AxisType(String name) {
            this.name = name;
        }

        public static AxisType fromString(String name) {
            if (name != null) {
                for (AxisType type : AxisType.values()) {
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

