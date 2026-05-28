/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ColorConfiguration;
import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class PaintConfiguration
implements Serializable {
    private static final String DEFAULT_PAINT_TYPE = PaintType.COLOR.getName();
    private ColorConfiguration colorConf;
    private PaintType paintType;

    public PaintConfiguration(PropertyList paintProps) {
        if (paintProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.paintType = PaintType.fromString(paintProps.getProperty("painttype", DEFAULT_PAINT_TYPE));
        if (this.paintType != PaintType.COLOR) {
            this.colorConf = null;
            throw new IllegalArgumentException("Unknown paint type: " + (Object)((Object)this.paintType));
        }
        this.colorConf = new ColorConfiguration(paintProps.getPropertyListNotNull("colorprops"), this);
    }

    public PaintConfiguration(PaintConfiguration copy) {
        this.paintType = copy.paintType;
        this.colorConf = copy.colorConf != null ? new ColorConfiguration(copy.colorConf, this) : null;
    }

    public PaintType getPaintType() {
        return this.paintType;
    }

    public Paint getPaint() {
        Paint paint = this.getPaintByType();
        if (paint == null) {
            throw new IllegalStateException("Paint not set yet");
        }
        return paint;
    }

    public boolean hasPaint() {
        return this.getPaintByType() != null;
    }

    public void setPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Paint is null");
        }
        if (!(paint instanceof Color)) {
            throw new IllegalArgumentException("Cannot resolve type for paint: " + paint);
        }
        this.colorConf = new ColorConfiguration((Color)paint, this);
    }

    public Paint getOrSetPaint(Paint defaultPaint) {
        Paint paint = this.getPaintByType();
        if (paint == null) {
            this.setPaint(defaultPaint);
            return defaultPaint;
        }
        return paint;
    }

    private Paint getPaintByType() {
        if (this.getPaintType() != PaintType.COLOR) {
            throw new IllegalArgumentException("Unknown paint type: " + (Object)((Object)this.paintType));
        }
        Color paint = this.colorConf.getColor();
        return paint;
    }

    public static enum PaintType {
        COLOR("Color");

        private final String name;

        private PaintType(String name) {
            this.name = name;
        }

        public static PaintType fromString(String name) {
            if (name != null) {
                for (PaintType type : PaintType.values()) {
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

