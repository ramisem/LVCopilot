/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.ui.TextAnchor
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import org.jfree.ui.TextAnchor;

public enum TextAnchorType {
    TOP_LEFT("Top Left", TextAnchor.TOP_LEFT),
    TOP_CENTER("Top Center", TextAnchor.TOP_CENTER),
    TOP_RIGHT("Top Right", TextAnchor.TOP_RIGHT),
    HALF_ASCENT_LEFT("Half Ascent Left", TextAnchor.HALF_ASCENT_LEFT),
    HALF_ASCENT_CENTER("Half Ascent Center", TextAnchor.HALF_ASCENT_CENTER),
    HALF_ASCENT_RIGHT("Half Ascent Right", TextAnchor.HALF_ASCENT_RIGHT),
    CENTER_LEFT("Center Left", TextAnchor.CENTER_LEFT),
    CENTER("Center", TextAnchor.CENTER),
    CENTER_RIGHT("Center Right", TextAnchor.CENTER_RIGHT),
    BASELINE_LEFT("Baseline Left", TextAnchor.BASELINE_LEFT),
    BASELINE_CENTER("Baseline Center", TextAnchor.BASELINE_CENTER),
    BASELINE_RIGHT("Baseline Right", TextAnchor.BASELINE_RIGHT),
    BOTTOM_LEFT("Bottom Left", TextAnchor.BOTTOM_LEFT),
    BOTTOM_CENTER("Bottom Center", TextAnchor.BOTTOM_CENTER),
    BOTTOM_RIGHT("Bottom Right", TextAnchor.BOTTOM_RIGHT);

    private final String name;
    private final TextAnchor textAnchor;

    private TextAnchorType(String name, TextAnchor textAnchor) {
        this.name = name;
        this.textAnchor = textAnchor;
    }

    public static TextAnchorType fromString(String name) {
        if (name != null) {
            for (TextAnchorType type : TextAnchorType.values()) {
                if (!name.equalsIgnoreCase(type.name)) continue;
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown name: " + name);
    }

    public static TextAnchorType fromTextAnchor(TextAnchor textAnchor) {
        if (textAnchor != null) {
            for (TextAnchorType type : TextAnchorType.values()) {
                if (textAnchor != type.textAnchor) continue;
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown text anchor: " + textAnchor);
    }

    public String getName() {
        return this.name;
    }

    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }
}

