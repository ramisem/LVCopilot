/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import java.awt.Font;
import java.awt.Paint;
import java.awt.font.TextAttribute;
import java.io.Serializable;
import java.util.Map;
import sapphire.xml.PropertyList;

public final class FontConfiguration
implements Serializable {
    private final PropertyList fontProps;
    private Font font;

    public FontConfiguration(PropertyList fontProps, ChartConfiguration chartConfiguration) {
        if (fontProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.fontProps = fontProps;
        this.fontProps.setProperty("family", chartConfiguration == null ? "SansSerif" : chartConfiguration.getFontFamily());
        this.font = null;
    }

    public FontConfiguration(FontConfiguration copy) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        this.fontProps = copy.fontProps.copy();
        this.font = null;
    }

    public Font getFont() {
        if (this.font == null) {
            throw new IllegalStateException("Font not set yet.");
        }
        return this.font;
    }

    public Font getFont(Font baseFont, Paint defaultPaint) {
        if (baseFont == null) {
            throw new IllegalArgumentException("Base font is null");
        }
        if (defaultPaint == null) {
            throw new IllegalArgumentException("Default paint is null");
        }
        if (this.font == null) {
            this.font = this.getFont(this.fontProps, baseFont, defaultPaint);
        }
        return this.font;
    }

    private Font getFont(PropertyList fontProps, Font baseFont, Paint defaultPaint) {
        String size = fontProps.getProperty("size");
        String bold = fontProps.getProperty("bold");
        String italic = fontProps.getProperty("italic");
        String color = fontProps.getProperty("color");
        String family = fontProps.getProperty("family", "SansSerif");
        PaintConfiguration paintConf = new PaintConfiguration(fontProps.getPropertyListNotNull("paintprops"));
        Map<TextAttribute, ?> fontAttributes = baseFont.getAttributes();
        fontAttributes.put(TextAttribute.FAMILY, family);
        if (size.length() > 0) {
            try {
                fontAttributes.put(TextAttribute.SIZE, Float.valueOf(size.replace(',', '.')));
            }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid font size: " + size, ex);
            }
        }
        if (bold.length() > 0) {
            boolean isBold = bold.toLowerCase().startsWith("y");
            if (isBold) {
                fontAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            } else {
                fontAttributes.put(TextAttribute.WEIGHT, TextAttribute.WIDTH_REGULAR);
            }
        }
        if (italic.length() > 0) {
            boolean isItalic = italic.toLowerCase().startsWith("y");
            if (isItalic) {
                fontAttributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
            } else {
                fontAttributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
            }
        }
        if (color.length() > 0) {
            fontAttributes.put(TextAttribute.FOREGROUND, paintConf.getOrSetPaint(defaultPaint));
        }
        return new Font(fontAttributes);
    }
}

