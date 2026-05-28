/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.ui.RectangleAnchor
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.RectangleAnchorType;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.XYAnnotationConfiguration;
import java.io.Serializable;
import org.jfree.ui.RectangleAnchor;
import sapphire.xml.PropertyList;

public final class XYTitleAnnotationConfiguration
implements Serializable {
    private static final String DEFAULT_X = "0.5";
    private static final String DEFAULT_Y = "0.5";
    private static final String DEFAULT_TITLE_ID = "";
    private static final String DEFAULT_RECTANGLE_ANCHOR = RectangleAnchorType.fromRectangleAnchor(RectangleAnchor.CENTER).getName();
    private final XYAnnotationConfiguration parent;
    private final String titleId;
    private final double x;
    private final double y;
    private final RectangleAnchor rectangleAnchor;

    public XYTitleAnnotationConfiguration(PropertyList xyTitleAnnotationProps, XYAnnotationConfiguration parent) {
        if (xyTitleAnnotationProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.titleId = xyTitleAnnotationProps.getProperty("titleid", DEFAULT_TITLE_ID);
        this.x = Double.valueOf(xyTitleAnnotationProps.getProperty("x", "0.5"));
        this.y = Double.valueOf(xyTitleAnnotationProps.getProperty("y", "0.5"));
        this.rectangleAnchor = RectangleAnchorType.fromString(xyTitleAnnotationProps.getProperty("rectangleanchor", DEFAULT_RECTANGLE_ANCHOR)).getRectangleAnchor();
    }

    public XYTitleAnnotationConfiguration(XYTitleAnnotationConfiguration copy, XYAnnotationConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.titleId = copy.titleId;
        this.x = copy.x;
        this.y = copy.y;
        this.rectangleAnchor = copy.rectangleAnchor;
    }

    public RectangleAnchor getRectangleAnchor() {
        return this.rectangleAnchor;
    }

    public String getTitleId() {
        return this.titleId;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public XYAnnotationConfiguration getParent() {
        return this.parent;
    }
}

