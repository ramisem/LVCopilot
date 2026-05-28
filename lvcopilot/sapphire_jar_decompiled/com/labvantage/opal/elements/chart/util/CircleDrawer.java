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

public class CircleDrawer
implements Drawable {
    private int seriesLocal = 0;

    public CircleDrawer(int series) {
        this.seriesLocal = series;
    }

    public Shape createDiamond(float x, float y, float s) {
        GeneralPath p0 = new GeneralPath();
        p0.moveTo(x + 2.0f, y - s);
        p0.lineTo(x + 2.0f + s, y);
        p0.lineTo(x + 2.0f, y + s);
        p0.lineTo(x + 2.0f - s, y);
        p0.closePath();
        return p0;
    }

    public Shape createUpTriangle(float x, float y, float s) {
        GeneralPath p0 = new GeneralPath();
        p0.moveTo(x + 2.0f, y - s);
        p0.lineTo(x + 2.0f + s, y + s);
        p0.lineTo(x + 2.0f - s, y + s);
        p0.closePath();
        return p0;
    }

    private void fill(Graphics2D graphics2d, Rectangle2D rectangle2dLocal, Paint fillPaint, Paint outlinePaint) {
        if (fillPaint != null) {
            graphics2d.setPaint(fillPaint);
            graphics2d.fill(rectangle2dLocal);
        }
        if (outlinePaint != null) {
            graphics2d.setPaint(outlinePaint);
            graphics2d.setStroke(new BasicStroke(1.0f));
            graphics2d.draw(rectangle2dLocal);
        }
    }

    public void draw(Graphics2D graphics2d, Rectangle2D rectangle2d) {
        Shape rectangle2dLocal = rectangle2d;
        if (this.seriesLocal == 0) {
            rectangle2dLocal = new Rectangle2D.Double(rectangle2d.getX(), rectangle2d.getY(), rectangle2d.getWidth(), rectangle2d.getHeight());
            this.fill(graphics2d, (Rectangle2D)rectangle2dLocal, Color.RED, Color.RED);
        } else if (this.seriesLocal == 1) {
            rectangle2dLocal = new Ellipse2D.Double(rectangle2d.getX(), rectangle2d.getY(), rectangle2d.getWidth(), rectangle2d.getHeight());
            this.fill(graphics2d, (Rectangle2D)rectangle2dLocal, Color.BLUE, Color.BLUE);
        } else if (this.seriesLocal == 2) {
            rectangle2dLocal = this.createUpTriangle((float)rectangle2d.getX(), (float)rectangle2d.getY(), 3.0f);
            this.fill(graphics2d, (Rectangle2D)rectangle2dLocal, Color.GREEN, Color.GREEN);
        } else if (this.seriesLocal == 3) {
            rectangle2dLocal = this.createDiamond((float)rectangle2d.getX(), (float)rectangle2d.getY(), 3.0f);
            this.fill(graphics2d, (Rectangle2D)rectangle2dLocal, Color.MAGENTA, Color.MAGENTA);
        } else if (this.seriesLocal == 4) {
            // empty if block
        }
    }
}

