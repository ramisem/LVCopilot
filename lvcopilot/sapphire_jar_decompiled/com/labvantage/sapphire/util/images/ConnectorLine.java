/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.images;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;

public class ConnectorLine {
    public static final int LINE_TYPE_SIMPLE = 0;
    public static final int LINE_TYPE_RECT_1BREAK = 1;
    public static final int LINE_TYPE_RECT_2BREAK = 2;
    public static final int CONNECT_LINE_TYPE_SIMPLE = 0;
    public static final int CONNECT_LINE_TYPE_RECTANGULAR = 1;
    public static final int LINE_START_HORIZONTAL = 0;
    public static final int LINE_START_VERTICAL = 1;
    public static int LINE_ARROW_WIDTH = 10;
    private Point p1;
    private Point p2;
    private int lineType = 0;
    private int lineStart = 0;
    private LineArrow lineArrow = LineArrow.NONE;
    private String lineText = "";

    public ConnectorLine(Point p1, Point p2) {
        this(p1, p2, 0, 0, LineArrow.NONE, "");
    }

    public ConnectorLine(Point p1, Point p2, int lineType, int lineStart, LineArrow lineArrow, String lineText) {
        this.p1 = p1;
        this.p2 = p2;
        this.lineType = lineType;
        this.lineStart = lineStart;
        this.lineArrow = lineArrow;
        this.lineText = lineText;
    }

    private void paint(Graphics2D g2d) {
        switch (this.lineType) {
            case 0: {
                this.paintSimple(g2d);
                break;
            }
            case 1: {
                this.paint1Break(g2d);
                break;
            }
            case 2: {
                this.paint2Breaks(g2d);
            }
        }
    }

    private void paintSimple(Graphics2D g2d) {
        g2d.drawLine(this.p1.x, this.p1.y, this.p2.x, this.p2.y);
        switch (this.lineArrow) {
            case DESTINATION: {
                this.paintArrow(g2d, this.p1, this.p2);
                break;
            }
            case SOURCE: {
                this.paintArrow(g2d, this.p2, this.p1);
                break;
            }
            case BOTH: {
                this.paintArrow(g2d, this.p1, this.p2);
                this.paintArrow(g2d, this.p2, this.p1);
            }
        }
        if (this.lineText != null && this.lineText.length() > 0) {
            g2d.setFont(new Font("Arial", 0, 12));
            int stringLen = (int)g2d.getFontMetrics().getStringBounds(this.lineText, g2d).getWidth();
            g2d.drawString(this.lineText, this.p1.x - stringLen / 2, this.p1.y + 24);
        }
    }

    private void paintArrow(Graphics2D g2d, Point p1, Point p2) {
        this.paintArrow(g2d, p1, p2, this.getRestrictedArrowWidth(p1, p2));
    }

    private void paintArrow(Graphics2D g2d, Point p1, Point p2, int width) {
        Point2D.Float pp1 = new Point2D.Float(p1.x, p1.y);
        Point2D.Float pp2 = new Point2D.Float(p2.x, p2.y);
        Point2D.Float left = ConnectorLine.getLeftArrowPoint(pp1, pp2, width);
        Point2D.Float right = ConnectorLine.getRightArrowPoint(pp1, pp2, width);
        g2d.drawLine(p2.x, p2.y, Math.round(left.x), Math.round(left.y));
        g2d.drawLine(p2.x, p2.y, Math.round(right.x), Math.round(right.y));
    }

    private void paint1Break(Graphics2D g2d) {
        if (this.lineStart == 0) {
            g2d.drawLine(this.p1.x, this.p1.y, this.p2.x, this.p1.y);
            g2d.drawLine(this.p2.x, this.p1.y, this.p2.x, this.p2.y);
            switch (this.lineArrow) {
                case DESTINATION: {
                    this.paintArrow(g2d, new Point(this.p2.x, this.p1.y), this.p2);
                    break;
                }
                case SOURCE: {
                    this.paintArrow(g2d, new Point(this.p2.x, this.p1.y), this.p1);
                    break;
                }
                case BOTH: {
                    this.paintArrow(g2d, new Point(this.p2.x, this.p1.y), this.p2);
                    this.paintArrow(g2d, new Point(this.p2.x, this.p1.y), this.p1);
                }
            }
            if (this.lineText != null && this.lineText.length() > 0) {
                g2d.setFont(new Font("Arial", 0, 12));
                int stringLen = (int)g2d.getFontMetrics().getStringBounds(this.lineText, g2d).getWidth();
                if (this.p1.x < this.p2.x) {
                    g2d.drawString(this.lineText, this.p1.x + 5, this.p1.y - 2);
                } else {
                    g2d.drawString(this.lineText, this.p1.x - stringLen - 5, this.p1.y + 16);
                }
            }
        } else if (this.lineStart == 1) {
            g2d.drawLine(this.p1.x, this.p1.y, this.p1.x, this.p2.y);
            g2d.drawLine(this.p1.x, this.p2.y, this.p2.x, this.p2.y);
            switch (this.lineArrow) {
                case DESTINATION: {
                    this.paintArrow(g2d, new Point(this.p1.x, this.p2.y), this.p2);
                    break;
                }
                case SOURCE: {
                    this.paintArrow(g2d, new Point(this.p1.x, this.p2.y), this.p1);
                    break;
                }
                case BOTH: {
                    this.paintArrow(g2d, new Point(this.p1.x, this.p2.y), this.p2);
                    this.paintArrow(g2d, new Point(this.p1.x, this.p2.y), this.p1);
                }
            }
            if (this.lineText != null && this.lineText.length() > 0) {
                g2d.setFont(new Font("Arial", 0, 12));
                int stringLen = (int)g2d.getFontMetrics().getStringBounds(this.lineText, g2d).getWidth();
                if (this.p1.y < this.p2.y) {
                    g2d.drawString(this.lineText, this.p1.x - stringLen / 2, this.p1.y + 24);
                } else {
                    g2d.drawString(this.lineText, this.p1.x - stringLen / 2, this.p1.y - 5);
                }
            }
        }
    }

    private void paint2Breaks(Graphics2D g2d) {
        if (this.lineStart == 0) {
            g2d.drawLine(this.p1.x, this.p1.y, this.p1.x + (this.p2.x - this.p1.x) / 2, this.p1.y);
            g2d.drawLine(this.p1.x + (this.p2.x - this.p1.x) / 2, this.p1.y, this.p1.x + (this.p2.x - this.p1.x) / 2, this.p2.y);
            g2d.drawLine(this.p1.x + (this.p2.x - this.p1.x) / 2, this.p2.y, this.p2.x, this.p2.y);
            switch (this.lineArrow) {
                case DESTINATION: {
                    this.paintArrow(g2d, new Point(this.p1.x + (this.p2.x - this.p1.x) / 2, this.p2.y), this.p2);
                    break;
                }
                case SOURCE: {
                    this.paintArrow(g2d, new Point(this.p1.x + (this.p2.x - this.p1.x) / 2, this.p1.y), this.p1);
                    break;
                }
                case BOTH: {
                    this.paintArrow(g2d, new Point(this.p1.x + (this.p2.x - this.p1.x) / 2, this.p2.y), this.p2);
                    this.paintArrow(g2d, new Point(this.p1.x + (this.p2.x - this.p1.x) / 2, this.p1.y), this.p1);
                }
            }
            if (this.lineText != null && this.lineText.length() > 0) {
                g2d.setFont(new Font("Arial", 0, 12));
                int stringLen = (int)g2d.getFontMetrics().getStringBounds(this.lineText, g2d).getWidth();
                if (this.p1.x < this.p2.x) {
                    g2d.drawString(this.lineText, this.p1.x + 5, this.p1.y - 2);
                } else {
                    g2d.drawString(this.lineText, this.p1.x - stringLen - 5, this.p1.y + 16);
                }
            }
        } else if (this.lineStart == 1) {
            g2d.drawLine(this.p1.x, this.p1.y, this.p1.x, this.p1.y + (this.p2.y - this.p1.y) / 2);
            g2d.drawLine(this.p1.x, this.p1.y + (this.p2.y - this.p1.y) / 2, this.p2.x, this.p1.y + (this.p2.y - this.p1.y) / 2);
            g2d.drawLine(this.p2.x, this.p1.y + (this.p2.y - this.p1.y) / 2, this.p2.x, this.p2.y);
            switch (this.lineArrow) {
                case DESTINATION: {
                    this.paintArrow(g2d, new Point(this.p2.x, this.p1.y + (this.p2.y - this.p1.y) / 2), this.p2);
                    break;
                }
                case SOURCE: {
                    this.paintArrow(g2d, new Point(this.p1.x, this.p1.y + (this.p2.y - this.p1.y) / 2), this.p1);
                    break;
                }
                case BOTH: {
                    this.paintArrow(g2d, new Point(this.p2.x, this.p1.y + (this.p2.y - this.p1.y) / 2), this.p2);
                    this.paintArrow(g2d, new Point(this.p1.x, this.p1.y + (this.p2.y - this.p1.y) / 2), this.p1);
                }
            }
            if (this.lineText != null && this.lineText.length() > 0) {
                g2d.setFont(new Font("Arial", 0, 12));
                int stringLen = (int)g2d.getFontMetrics().getStringBounds(this.lineText, g2d).getWidth();
                if (this.p1.y < this.p2.y) {
                    g2d.drawString(this.lineText, this.p1.x - stringLen / 2, this.p1.y + 24);
                } else {
                    g2d.drawString(this.lineText, this.p1.x - stringLen / 2, this.p1.y - 5);
                }
            }
        }
    }

    private int getLineType() {
        return this.lineType;
    }

    private void setLineType(int type) {
        this.lineType = type;
    }

    private int getLineStart() {
        return this.lineStart;
    }

    private void setLineStart(int start) {
        this.lineStart = start;
    }

    private LineArrow getLineArrow() {
        return this.lineArrow;
    }

    private void setLineArrow(LineArrow arrow) {
        this.lineArrow = this.lineArrow;
    }

    private Point getP1() {
        return this.p1;
    }

    private void setP1(Point p) {
        this.p1 = p;
    }

    private Point getP2() {
        return this.p2;
    }

    private void setP2(Point p) {
        this.p2 = p;
    }

    private static Point2D.Float getMidArrowPoint(Point2D.Float p1, Point2D.Float p2, float w) {
        Point2D.Float res = new Point2D.Float();
        float d = Math.round(Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
        res.x = p1.x < p2.x ? p2.x - w * Math.abs(p1.x - p2.x) / d : p2.x + w * Math.abs(p1.x - p2.x) / d;
        res.y = p1.y < p2.y ? p2.y - w * Math.abs(p1.y - p2.y) / d : p2.y + w * Math.abs(p1.y - p2.y) / d;
        return res;
    }

    private static Point2D.Float getLeftArrowPoint(Point2D.Float p1, Point2D.Float p2) {
        return ConnectorLine.getLeftArrowPoint(p1, p2, LINE_ARROW_WIDTH);
    }

    private static Point2D.Float getLeftArrowPoint(Point2D.Float p1, Point2D.Float p2, float w) {
        Point2D.Float res = new Point2D.Float();
        double alpha = 1.5707963267948966;
        if (p2.x != p1.x) {
            alpha = Math.atan((p2.y - p1.y) / (p2.x - p1.x));
        }
        float xShift = Math.abs(Math.round(Math.cos(alpha += 0.3141592653589793) * (double)w));
        float yShift = Math.abs(Math.round(Math.sin(alpha) * (double)w));
        res.x = p1.x <= p2.x ? p2.x - xShift : p2.x + xShift;
        res.y = p1.y < p2.y ? p2.y - yShift : p2.y + yShift;
        return res;
    }

    private static Point2D.Float getRightArrowPoint(Point2D.Float p1, Point2D.Float p2) {
        return ConnectorLine.getRightArrowPoint(p1, p2, LINE_ARROW_WIDTH);
    }

    private static Point2D.Float getRightArrowPoint(Point2D.Float p1, Point2D.Float p2, float w) {
        Point2D.Float res = new Point2D.Float();
        double alpha = 1.5707963267948966;
        if (p2.x != p1.x) {
            alpha = Math.atan((p2.y - p1.y) / (p2.x - p1.x));
        }
        float xShift = Math.abs(Math.round(Math.cos(alpha -= 0.3141592653589793) * (double)w));
        float yShift = Math.abs(Math.round(Math.sin(alpha) * (double)w));
        res.x = p1.x < p2.x ? p2.x - xShift : p2.x + xShift;
        res.y = p1.y <= p2.y ? p2.y - yShift : p2.y + yShift;
        return res;
    }

    private int getRestrictedArrowWidth(Point p1, Point p2) {
        return Math.min(LINE_ARROW_WIDTH, (int)Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)));
    }

    protected Rectangle getLineBounds() {
        int add = 10;
        int maxX = Math.max(this.getP1().x, this.getP2().x);
        int minX = Math.min(this.getP1().x, this.getP2().x);
        int maxY = Math.max(this.getP1().y, this.getP2().y);
        int minY = Math.min(this.getP1().y, this.getP2().y);
        Rectangle res = new Rectangle(minX - add, minY - add, maxX - minX + 2 * add, maxY - minY + 2 * add);
        return res;
    }

    private static ConnectorLine calculateLine(Rectangle rSource, Rectangle rDest, LineArrow lineArrow, String lineText) {
        ConnectorLine line;
        boolean lineType = true;
        if (rSource.intersects(rDest)) {
            line = null;
        } else {
            boolean yIntersect;
            boolean xIntersect = rSource.x <= rDest.x && rSource.x + rSource.width >= rDest.x || rDest.x <= rSource.x && rDest.x + rDest.width >= rSource.x;
            boolean bl = yIntersect = rSource.y <= rDest.y && rSource.y + rSource.height >= rDest.y || rDest.y <= rSource.y && rDest.y + rDest.height >= rSource.y;
            if (xIntersect) {
                int y2;
                int y1;
                int x1 = rSource.x + rSource.width / 2;
                int x2 = rDest.x + rDest.width / 2;
                if (rSource.y + rSource.height <= rDest.y) {
                    y1 = rSource.y + rSource.height;
                    y2 = rDest.y;
                } else {
                    y1 = rSource.y;
                    y2 = rDest.y + rDest.height;
                }
                line = new ConnectorLine(new Point(x1, y1), new Point(x2, y2), 2, 1, lineArrow, lineText);
                if (!lineType) {
                    line.setLineType(0);
                }
            } else if (yIntersect) {
                int x2;
                int x1;
                int y1 = rSource.y + rSource.height / 2;
                int y2 = rDest.y + rDest.height / 2;
                if (rSource.x + rSource.width <= rDest.x) {
                    x1 = rSource.x + rSource.width;
                    x2 = rDest.x;
                } else {
                    x1 = rSource.x;
                    x2 = rDest.x + rDest.width;
                }
                line = new ConnectorLine(new Point(x1, y1), new Point(x2, y2), 2, 0, lineArrow, lineText);
                if (!lineType) {
                    line.setLineType(0);
                }
            } else {
                int x2;
                int x1;
                int y2;
                int y1;
                if (rSource.y + rSource.height <= rDest.y) {
                    y1 = rSource.y + rSource.height / 2;
                    y2 = rDest.y;
                    x1 = rSource.x + rSource.width <= rDest.x ? rSource.x + rSource.width : rSource.x;
                    x2 = rDest.x + rDest.width / 2;
                } else {
                    y1 = rSource.y + rSource.height / 2;
                    y2 = rDest.y + rDest.height;
                    x1 = rSource.x + rSource.width <= rDest.x ? rSource.x + rSource.width : rSource.x;
                    x2 = rDest.x + rDest.width / 2;
                }
                line = new ConnectorLine(new Point(x1, y1), new Point(x2, y2), 1, 0, lineArrow, lineText);
                if (!lineType) {
                    line.setLineType(0);
                }
            }
        }
        return line;
    }

    public static void draw(Rectangle rSource, Rectangle rDest, Color lineColor, Graphics2D g2d) {
        ConnectorLine.draw(rSource, rDest, lineColor, LineArrow.NONE, "", g2d);
    }

    public static void draw(Rectangle rSource, Rectangle rDest, Color lineColor, LineArrow lineArrow, String lineText, Graphics2D g2d) {
        ConnectorLine line = ConnectorLine.calculateLine(rSource, rDest, lineArrow, lineText);
        if (line != null) {
            Shape oldClip = g2d.getClip();
            g2d.setColor(lineColor);
            line.paint(g2d);
            g2d.setClip(oldClip);
        }
    }

    public static enum LineArrow {
        NONE,
        SOURCE,
        DESTINATION,
        BOTH;

    }
}

