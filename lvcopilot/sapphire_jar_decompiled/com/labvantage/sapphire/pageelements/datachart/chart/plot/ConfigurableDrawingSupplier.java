/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.ChartColor
 *  org.jfree.chart.plot.DefaultDrawingSupplier
 *  org.jfree.chart.plot.DrawingSupplier
 *  org.jfree.chart.renderer.AbstractRenderer
 */
package com.labvantage.sapphire.pageelements.datachart.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ShapeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StrokeConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.DrawingSupplierConfiguration;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.chart.ChartColor;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.renderer.AbstractRenderer;

public class ConfigurableDrawingSupplier
implements DrawingSupplier,
Serializable {
    private static final Paint DEFAULT_PAINT = Color.BLACK;
    private static final Shape DEFAULT_SHAPE = AbstractRenderer.DEFAULT_SHAPE;
    private static final Stroke DEFAULT_STROKE = AbstractRenderer.DEFAULT_STROKE;
    private final DefaultDrawingSupplier defaultDrawingSupplier;

    public Paint getNextPaint() {
        return this.defaultDrawingSupplier.getNextPaint();
    }

    public ConfigurableDrawingSupplier(DrawingSupplierConfiguration drawingSupplierConf) {
        List<StrokeConfiguration> list;
        List<ShapeConfiguration> shapeConfList;
        List<PaintConfiguration> paintConfList;
        List<Paint> fillPaintSequence = Arrays.asList(DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE);
        List<Paint> outlinePaintSequence = Arrays.asList(DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE);
        List<Stroke> strokeSequence = Arrays.asList(DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE);
        List<Stroke> outlineStrokeSequence = Arrays.asList(DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE);
        List<Shape> shapeSequence = Arrays.asList(DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
        List<Paint> paintSequence = Arrays.asList(DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE);
        if (drawingSupplierConf.getPaintConfType().equals("Color Scheme")) {
            paintSequence = Arrays.asList(ConfigurableDrawingSupplier.getStandardPaintArray(drawingSupplierConf.getColorSchemeId()));
        }
        if (!(paintConfList = drawingSupplierConf.getPaintConfigurationList()).isEmpty()) {
            paintSequence = new ArrayList<Paint>();
            for (PaintConfiguration paintConfiguration : paintConfList) {
                paintSequence.add(paintConfiguration.getOrSetPaint(DEFAULT_PAINT));
            }
        }
        if (!(shapeConfList = drawingSupplierConf.getShapeConfigurationList()).isEmpty()) {
            shapeSequence = new ArrayList<Shape>();
            for (ShapeConfiguration shapeConf : shapeConfList) {
                shapeSequence.add(shapeConf.getShape(DEFAULT_SHAPE));
            }
        }
        if (!(list = drawingSupplierConf.getStrokeConfigurationList()).isEmpty()) {
            strokeSequence = new ArrayList<Stroke>();
            for (StrokeConfiguration strokeConf : list) {
                strokeSequence.add(strokeConf.getStroke(DEFAULT_STROKE));
            }
        }
        this.defaultDrawingSupplier = new DefaultDrawingSupplier(paintSequence.toArray(new Paint[paintSequence.size()]), fillPaintSequence.toArray(new Paint[fillPaintSequence.size()]), outlinePaintSequence.toArray(new Paint[outlinePaintSequence.size()]), strokeSequence.toArray(new Stroke[strokeSequence.size()]), outlineStrokeSequence.toArray(new Stroke[outlineStrokeSequence.size()]), shapeSequence.toArray(new Shape[shapeSequence.size()]));
    }

    public Paint getNextOutlinePaint() {
        return this.defaultDrawingSupplier.getNextOutlinePaint();
    }

    public Paint getNextFillPaint() {
        return this.defaultDrawingSupplier.getNextFillPaint();
    }

    public Stroke getNextStroke() {
        return this.defaultDrawingSupplier.getNextStroke();
    }

    public Stroke getNextOutlineStroke() {
        return this.defaultDrawingSupplier.getNextOutlineStroke();
    }

    public Shape getNextShape() {
        return this.defaultDrawingSupplier.getNextShape();
    }

    public static Paint[] getStandardPaintArray(String standardPaintListId) {
        if (standardPaintListId.equals("Vibrant")) {
            return new Paint[]{new Color(56, 53, 157), new Color(189, 31, 141), new Color(247, 135, 0), new Color(31, 184, 18), new Color(6, 179, 252), new Color(253, 0, 137), new Color(44, 92, 179), new Color(241, 43, 18), new Color(105, 22, 144), new Color(5, 80, 148), new Color(253, 189, 110), new Color(0, 127, 171), new Color(178, 193, 248)};
        }
        if (standardPaintListId.equals("Cool")) {
            return new Paint[]{new Color(0, 65, 89), new Color(101, 168, 196), new Color(170, 206, 226), new Color(140, 101, 211), new Color(154, 147, 236), new Color(202, 185, 241), new Color(0, 82, 165), new Color(65, 59, 247), new Color(129, 203, 248), new Color(0, 173, 206), new Color(89, 219, 241), new Color(158, 231, 250), new Color(0, 197, 144), new Color(115, 235, 174), new Color(181, 249, 211)};
        }
        if (standardPaintListId.equals("Calm")) {
            return new Paint[]{new Color(224, 122, 146), new Color(234, 163, 180), new Color(0, 86, 137), new Color(2, 127, 177), new Color(108, 173, 222), new Color(0, 61, 80), new Color(0, 127, 152), new Color(117, 210, 231), new Color(0, 116, 123), new Color(0, 181, 173), new Color(63, 81, 111), new Color(86, 108, 144), new Color(143, 158, 176)};
        }
        if (standardPaintListId.equals("Metallic")) {
            return new Paint[]{new Color(31, 82, 99), new Color(45, 128, 132), new Color(69, 150, 140), new Color(52, 60, 96), new Color(62, 94, 141), new Color(85, 116, 160), new Color(62, 58, 108), new Color(88, 83, 138), new Color(100, 97, 141), new Color(68, 69, 92), new Color(118, 118, 144), new Color(146, 150, 152), new Color(91, 51, 62), new Color(134, 80, 76), new Color(146, 115, 101)};
        }
        if (standardPaintListId.equals("Black And White")) {
            return new Paint[]{new Color(0, 0, 0), new Color(33, 41, 48), new Color(65, 75, 86), new Color(96, 106, 116), new Color(134, 143, 152), new Color(168, 173, 180), new Color(195, 200, 205), new Color(210, 214, 217), new Color(203, 209, 212), new Color(178, 188, 192), new Color(153, 163, 166), new Color(123, 133, 138), new Color(79, 85, 89), new Color(61, 66, 66), new Color(50, 53, 49)};
        }
        if (standardPaintListId.equals("General")) {
            return new Paint[]{new Color(0, 38, 133), new Color(0, 126, 58), new Color(205, 30, 16), new Color(241, 171, 0), new Color(68, 154, 223), new Color(252, 0, 127), new Color(77, 199, 253), new Color(94, 83, 199), new Color(254, 121, 209), new Color(76, 222, 119), new Color(118, 57, 49), new Color(126, 119, 210), new Color(0, 0, 0), new Color(100, 209, 62)};
        }
        if (standardPaintListId.equals("Basic")) {
            return new Paint[]{new Color(178, 31, 53), new Color(0, 117, 58), new Color(0, 82, 165), new Color(104, 30, 126), new Color(255, 116, 53), new Color(216, 39, 53), new Color(0, 158, 71), new Color(0, 121, 231), new Color(125, 60, 181), new Color(255, 161, 53), new Color(22, 221, 54), new Color(6, 169, 252), new Color(189, 122, 246), new Color(255, 203, 53)};
        }
        if (standardPaintListId.equals("Sapphire")) {
            return new Paint[]{new Color(255, 85, 85), new Color(85, 85, 255), new Color(85, 255, 85), new Color(255, 255, 85), new Color(255, 85, 255), new Color(85, 255, 255), Color.pink, Color.gray, ChartColor.DARK_RED, ChartColor.DARK_BLUE, ChartColor.DARK_GREEN, ChartColor.DARK_YELLOW, ChartColor.DARK_MAGENTA, ChartColor.DARK_CYAN, Color.darkGray, ChartColor.LIGHT_RED, ChartColor.LIGHT_BLUE, ChartColor.LIGHT_GREEN, ChartColor.LIGHT_YELLOW, ChartColor.LIGHT_MAGENTA, ChartColor.LIGHT_CYAN, Color.lightGray, ChartColor.VERY_DARK_RED, ChartColor.VERY_DARK_BLUE, ChartColor.VERY_DARK_GREEN, ChartColor.VERY_DARK_YELLOW, ChartColor.VERY_DARK_MAGENTA, ChartColor.VERY_DARK_CYAN, ChartColor.VERY_LIGHT_RED, ChartColor.VERY_LIGHT_BLUE, ChartColor.VERY_LIGHT_GREEN, ChartColor.VERY_LIGHT_YELLOW, ChartColor.VERY_LIGHT_MAGENTA, ChartColor.VERY_LIGHT_CYAN};
        }
        return DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE;
    }
}

