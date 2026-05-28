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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Drawable;

public class FailureCircleDrawer
implements Drawable {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private static final int CONSTANT_2 = 2;
    private static final int CONSTANT_3 = 3;

    public void draw(Graphics2D graphics2d, Rectangle2D rectangle2d) {
        Ellipse2D.Double double1 = new Ellipse2D.Double(rectangle2d.getX() - 2.0, rectangle2d.getY() - 2.0, rectangle2d.getWidth() + 3.0, rectangle2d.getHeight() + 3.0);
        graphics2d.setPaint(Color.black);
        graphics2d.setStroke(new BasicStroke(1.0f));
        graphics2d.draw(double1);
        Line2D.Double double2 = new Line2D.Double(rectangle2d.getCenterX(), rectangle2d.getMinY() - 2.0, rectangle2d.getCenterX(), rectangle2d.getMaxY() + 2.0);
        Line2D.Double double3 = new Line2D.Double(rectangle2d.getMinX() - 2.0, rectangle2d.getCenterY(), rectangle2d.getMaxX() + 2.0, rectangle2d.getCenterY());
        graphics2d.draw(double2);
        graphics2d.draw(double3);
    }
}

