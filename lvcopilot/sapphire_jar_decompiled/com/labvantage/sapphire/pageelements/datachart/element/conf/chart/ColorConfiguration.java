/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.ChartColor
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import java.awt.Color;
import java.io.Serializable;
import org.jfree.chart.ChartColor;
import sapphire.xml.PropertyList;

public final class ColorConfiguration
implements Serializable {
    private static final String DEFAULT_COLOR = "";
    private final PaintConfiguration parent;
    private Color color;

    public ColorConfiguration(PropertyList colorProps, PaintConfiguration parent) {
        if (colorProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.color = ColorConfiguration.getColor(colorProps.getProperty("color", DEFAULT_COLOR));
        this.parent = parent;
    }

    public ColorConfiguration(ColorConfiguration copy, PaintConfiguration parent) {
        this.color = copy.color;
        this.parent = parent;
    }

    public ColorConfiguration(Color color, PaintConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        if (color == null) {
            throw new IllegalArgumentException("Color is null");
        }
        this.color = color;
        this.parent = parent;
    }

    private static Color getColor(String colorName) {
        if (colorName == null) {
            throw new IllegalArgumentException("Color name is null");
        }
        if (colorName.equalsIgnoreCase("DARK BLUE")) {
            return ChartColor.DARK_BLUE;
        }
        if (colorName.equalsIgnoreCase("DARK CYAN")) {
            return ChartColor.DARK_CYAN;
        }
        if (colorName.equalsIgnoreCase("DARK GREEN")) {
            return ChartColor.DARK_GREEN;
        }
        if (colorName.equalsIgnoreCase("DARK MAGENTA")) {
            return ChartColor.DARK_MAGENTA;
        }
        if (colorName.equalsIgnoreCase("DARK RED")) {
            return ChartColor.DARK_RED;
        }
        if (colorName.equalsIgnoreCase("DARK YELLOW")) {
            return ChartColor.DARK_YELLOW;
        }
        if (colorName.equalsIgnoreCase("LIGHT BLUE")) {
            return ChartColor.LIGHT_BLUE;
        }
        if (colorName.equalsIgnoreCase("LIGHT CYAN")) {
            return ChartColor.LIGHT_CYAN;
        }
        if (colorName.equalsIgnoreCase("LIGHT GREEN")) {
            return ChartColor.LIGHT_GREEN;
        }
        if (colorName.equalsIgnoreCase("LIGHT MAGENTA")) {
            return ChartColor.LIGHT_MAGENTA;
        }
        if (colorName.equalsIgnoreCase("LIGHT RED")) {
            return ChartColor.LIGHT_RED;
        }
        if (colorName.equalsIgnoreCase("LIGHT YELLOW")) {
            return ChartColor.LIGHT_YELLOW;
        }
        if (colorName.equalsIgnoreCase("VERY DARK  BLUE")) {
            return ChartColor.VERY_DARK_BLUE;
        }
        if (colorName.equalsIgnoreCase("VERY DARK CYAN")) {
            return ChartColor.VERY_DARK_CYAN;
        }
        if (colorName.equalsIgnoreCase("VERY DARK GREEN")) {
            return ChartColor.VERY_DARK_GREEN;
        }
        if (colorName.equalsIgnoreCase("VERY DARK MAGENTA")) {
            return ChartColor.VERY_DARK_MAGENTA;
        }
        if (colorName.equalsIgnoreCase("VERY DARK RED")) {
            return ChartColor.VERY_DARK_RED;
        }
        if (colorName.equalsIgnoreCase("VERY DARK YELLOW")) {
            return ChartColor.VERY_DARK_YELLOW;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT BLUE")) {
            return ChartColor.VERY_LIGHT_BLUE;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT CYAN")) {
            return ChartColor.VERY_LIGHT_CYAN;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT GREEN")) {
            return ChartColor.VERY_LIGHT_GREEN;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT MAGENTA")) {
            return ChartColor.VERY_LIGHT_MAGENTA;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT RED")) {
            return ChartColor.VERY_LIGHT_RED;
        }
        if (colorName.equalsIgnoreCase("VERY LIGHT YELLOW")) {
            return ChartColor.VERY_LIGHT_YELLOW;
        }
        if (colorName.equalsIgnoreCase("BLUE")) {
            return ChartColor.BLUE;
        }
        if (colorName.equalsIgnoreCase("CYAN")) {
            return ChartColor.CYAN;
        }
        if (colorName.equalsIgnoreCase("GREEN")) {
            return ChartColor.GREEN;
        }
        if (colorName.equalsIgnoreCase("MAGENTA")) {
            return ChartColor.MAGENTA;
        }
        if (colorName.equalsIgnoreCase("RED")) {
            return ChartColor.RED;
        }
        if (colorName.equalsIgnoreCase("YELLOW")) {
            return ChartColor.YELLOW;
        }
        if (colorName.equalsIgnoreCase("BLACK")) {
            return ChartColor.BLACK;
        }
        if (colorName.equalsIgnoreCase("DARK GRAY")) {
            return ChartColor.DARK_GRAY;
        }
        if (colorName.equalsIgnoreCase("GRAY")) {
            return ChartColor.GRAY;
        }
        if (colorName.equalsIgnoreCase("LIGHT GRAY")) {
            return ChartColor.LIGHT_GRAY;
        }
        if (colorName.equalsIgnoreCase("WHITE")) {
            return ChartColor.WHITE;
        }
        if (!colorName.contains("#") && !colorName.contains("0x")) {
            colorName = "#" + colorName;
        }
        try {
            return Color.decode(colorName);
        }
        catch (Exception ignored) {
            return null;
        }
    }

    public PaintConfiguration getParent() {
        return this.parent;
    }

    public Color getColor() {
        return this.color;
    }
}

