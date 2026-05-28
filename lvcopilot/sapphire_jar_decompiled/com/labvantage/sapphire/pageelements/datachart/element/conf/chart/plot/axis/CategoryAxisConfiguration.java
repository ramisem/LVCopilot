/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.axis.CategoryLabelPositions
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.axis.AxisConfiguration;
import java.io.Serializable;
import org.jfree.chart.axis.CategoryLabelPositions;
import sapphire.xml.PropertyList;

public class CategoryAxisConfiguration
implements Serializable {
    private static final String DEFAULT_LABEL_POSITION_TYPE = LabelPositionType.STANDARD.getName();
    private static final String DEFAULT_LOWER_MARGIN = String.valueOf(0.05);
    private static final String DEFAULT_UPPER_MARGIN = String.valueOf(0.05);
    private final AxisConfiguration parent;
    private final CategoryLabelPositions categoryLabelPositions;
    private final double lowerMargin;
    private final double upperMargin;

    public CategoryAxisConfiguration(PropertyList categoryAxisProps, AxisConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (categoryAxisProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        LabelPositionType labelPositionType = LabelPositionType.fromString(categoryAxisProps.getProperty("labelpositiontype", DEFAULT_LABEL_POSITION_TYPE));
        this.categoryLabelPositions = labelPositionType.getCategoryLabelPositions();
        this.parent = parent;
        this.lowerMargin = Double.valueOf(categoryAxisProps.getProperty("lowermargin", DEFAULT_LOWER_MARGIN));
        this.upperMargin = Double.valueOf(categoryAxisProps.getProperty("uppermargin", DEFAULT_UPPER_MARGIN));
    }

    public CategoryAxisConfiguration(CategoryAxisConfiguration copy, AxisConfiguration parent) {
        this.categoryLabelPositions = copy.categoryLabelPositions;
        this.lowerMargin = copy.lowerMargin;
        this.upperMargin = copy.upperMargin;
        this.parent = parent;
    }

    public double getLowerMargin() {
        return this.lowerMargin;
    }

    public double getUpperMargin() {
        return this.upperMargin;
    }

    public CategoryLabelPositions getCategoryLabelPositions() {
        return this.categoryLabelPositions;
    }

    public AxisConfiguration getParent() {
        return this.parent;
    }

    public static enum LabelPositionType {
        DOWN_45("Down 45", CategoryLabelPositions.DOWN_45),
        DOWN_90("Down 90", CategoryLabelPositions.DOWN_90),
        STANDARD("Standard", CategoryLabelPositions.STANDARD),
        UP_45("Up 45", CategoryLabelPositions.UP_45),
        UP_90("Up 90", CategoryLabelPositions.UP_90);

        private final CategoryLabelPositions categoryLabelPositions;
        private final String name;

        private LabelPositionType(String name, CategoryLabelPositions categoryLabelPositions) {
            this.name = name;
            this.categoryLabelPositions = categoryLabelPositions;
        }

        public static LabelPositionType fromString(String name) {
            if (name != null) {
                for (LabelPositionType type : LabelPositionType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }

        public CategoryLabelPositions getCategoryLabelPositions() {
            return this.categoryLabelPositions;
        }
    }
}

