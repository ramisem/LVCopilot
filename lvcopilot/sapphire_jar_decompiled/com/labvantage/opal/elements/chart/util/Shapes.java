/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.ui.Drawable
 */
package com.labvantage.opal.elements.chart.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Drawable;

public class Shapes
implements Drawable {
    private int __SeriesLocal = 0;
    private String __AvgFlagLocal = new String("rep");
    private static final int CONSTANT_2 = 2;
    private static final int CONSTANT_3 = 3;
    private static final int SERIES_CONSTANT_0 = 0;
    private static final int SERIES_CONSTANT_1 = 1;
    private static final int SERIES_CONSTANT_2 = 2;
    private static final int SERIES_CONSTANT_3 = 3;
    private static final int SERIES_CONSTANT_4 = 4;
    private static final float FLOAT_VALUE_1 = 1.0f;
    private static final float FLOAT_VALUE_3 = 3.0f;
    private static final float FLOAT_VALUE_5 = 0.5f;

    public Shapes(int series) {
        this.__SeriesLocal = series;
    }

    public Shapes(int series, String avgFlag) {
        this.__SeriesLocal = series;
        this.__AvgFlagLocal = avgFlag;
    }

    public Shape createDiamond(float x, float y, float s) {
        GeneralPath p0 = new GeneralPath();
        p0.moveTo(x + 3.0f, y + 2.0f - s);
        p0.lineTo(x + 3.0f + s, y + 2.0f);
        p0.lineTo(x + 3.0f, y + 2.0f + s);
        p0.lineTo(x + 3.0f - s, y + 2.0f);
        p0.closePath();
        return p0;
    }

    public Shape createUpTriangle(float x, float y, float s) {
        GeneralPath p0 = new GeneralPath();
        p0.moveTo(x + 3.0f, y + 2.0f - s);
        p0.lineTo(x + 3.0f + s, y + 2.0f + s);
        p0.lineTo(x + 3.0f - s, y + 2.0f + s);
        p0.closePath();
        return p0;
    }

    private void fill(Graphics2D graphics2d, Shape rectangle2dLocal, Paint fillPaint, Paint outlinePaint) {
        if (this.__AvgFlagLocal.equalsIgnoreCase("rep")) {
            if (fillPaint != null) {
                graphics2d.setPaint(fillPaint);
                graphics2d.fill(rectangle2dLocal);
            }
            if (outlinePaint != null) {
                graphics2d.setPaint(outlinePaint);
                graphics2d.setStroke(new BasicStroke(1.0f));
                graphics2d.draw(rectangle2dLocal);
            }
        } else if (this.__AvgFlagLocal.equalsIgnoreCase("avg")) {
            if (fillPaint != null) {
                graphics2d.setPaint(fillPaint);
            }
            if (outlinePaint != null) {
                graphics2d.setPaint(outlinePaint);
                graphics2d.setStroke(new BasicStroke(1.0f));
                graphics2d.draw(rectangle2dLocal);
            }
        }
    }

    public void draw(Graphics2D graphics2d, Rectangle2D rectangle2d) {
        Shape rectangle2dLocal = rectangle2d;
        if (this.__SeriesLocal == 0) {
            rectangle2dLocal = new Rectangle2D.Double(rectangle2d.getX(), rectangle2d.getY(), rectangle2d.getWidth(), rectangle2d.getHeight());
            this.fill(graphics2d, rectangle2dLocal, Color.red, Color.red);
        } else if (this.__SeriesLocal == 1) {
            rectangle2dLocal = new Ellipse2D.Double(rectangle2d.getX(), rectangle2d.getY(), rectangle2d.getWidth(), rectangle2d.getHeight());
            this.fill(graphics2d, rectangle2dLocal, Color.blue, Color.blue);
        } else if (this.__SeriesLocal == 2) {
            rectangle2dLocal = this.createUpTriangle((float)rectangle2d.getX(), (float)rectangle2d.getY(), 3.0f);
            this.fill(graphics2d, rectangle2dLocal, Color.green, Color.green);
        } else if (this.__SeriesLocal == 3) {
            rectangle2dLocal = this.createDiamond((float)rectangle2d.getX(), (float)rectangle2d.getY(), 3.0f);
            this.fill(graphics2d, rectangle2dLocal, Color.yellow, Color.yellow);
        } else if (this.__SeriesLocal == 4) {
            rectangle2dLocal = new Rectangle2D.Double(rectangle2d.getX() - 0.5, rectangle2d.getY() + 1.0, rectangle2d.getHeight(), rectangle2d.getHeight() / 2.0);
            this.fill(graphics2d, rectangle2dLocal, Color.orange, Color.orange);
        }
    }
}

