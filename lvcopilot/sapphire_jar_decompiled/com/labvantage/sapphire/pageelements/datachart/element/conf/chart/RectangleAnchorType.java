/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.ui.RectangleAnchor
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import org.jfree.ui.RectangleAnchor;

public enum RectangleAnchorType {
    BOTTOM("Bottom", RectangleAnchor.BOTTOM),
    BOTTOM_LEFT("Bottom Left", RectangleAnchor.BOTTOM_LEFT),
    BOTTOM_RIGHT("Bottom Right", RectangleAnchor.BOTTOM_RIGHT),
    CENTER("Center", RectangleAnchor.CENTER),
    LEFT("Left", RectangleAnchor.LEFT),
    RIGHT("Right", RectangleAnchor.RIGHT),
    TOP("Top", RectangleAnchor.TOP),
    TOP_LEFT("Top Left", RectangleAnchor.TOP_LEFT),
    TOP_RIGHT("Top Right", RectangleAnchor.TOP_RIGHT);

    private final String name;
    private final RectangleAnchor rectangleAnchor;

    private RectangleAnchorType(String name, RectangleAnchor rectangleAnchor) {
        this.name = name;
        this.rectangleAnchor = rectangleAnchor;
    }

    public static RectangleAnchorType fromString(String name) {
        if (name != null) {
            for (RectangleAnchorType type : RectangleAnchorType.values()) {
                if (!name.equalsIgnoreCase(type.name)) continue;
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown name: " + name);
    }

    public static RectangleAnchorType fromRectangleAnchor(RectangleAnchor rectangleAnchor) {
        if (rectangleAnchor != null) {
            for (RectangleAnchorType type : RectangleAnchorType.values()) {
                if (rectangleAnchor != type.rectangleAnchor) continue;
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown rectangle anchor: " + rectangleAnchor);
    }

    public String getName() {
        return this.name;
    }

    public RectangleAnchor getRectangleAnchor() {
        return this.rectangleAnchor;
    }
}

