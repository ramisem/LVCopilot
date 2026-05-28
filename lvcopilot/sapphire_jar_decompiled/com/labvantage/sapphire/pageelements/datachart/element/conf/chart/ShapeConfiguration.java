/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.io.SerialUtilities
 *  org.jfree.util.ShapeUtilities
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.io.SerialUtilities;
import org.jfree.util.ShapeUtilities;
import sapphire.xml.PropertyList;

public final class ShapeConfiguration
implements Serializable {
    private static final List<String> shapeNameList = new ArrayList<String>();
    private static final String DEFAULT_SHAPE = "";
    private static final String DEFAULT_SHAPE_SIZE = "";
    private transient Shape shape;
    private String shapeName;
    private String shapeSize;
    private String defaultShapeName;
    private String defaultShapeSize;

    public ShapeConfiguration(PropertyList shapeProps) {
        if (shapeProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.shapeName = shapeProps.getProperty("shape", "");
        this.shapeSize = shapeProps.getProperty("shapesize", "");
        this.defaultShapeName = "";
        this.defaultShapeSize = "";
        this.shape = ShapeConfiguration.createShape(this.shapeName, this.shapeSize);
    }

    public ShapeConfiguration(ShapeConfiguration copy) {
        this.shapeName = copy.shapeName;
        this.shapeSize = copy.shapeSize;
        this.shape = copy.shape;
        this.defaultShapeName = copy.defaultShapeName;
        this.defaultShapeSize = copy.defaultShapeSize;
    }

    public ShapeConfiguration(ShapeConfiguration copy, ShapeConfiguration override) {
        String shapeName = override.shapeName;
        String shapeSize = override.shapeSize;
        if (shapeName.isEmpty()) {
            shapeName = copy.shapeName;
        }
        if (shapeSize.isEmpty()) {
            shapeSize = copy.shapeSize;
        }
        if (shapeName.isEmpty()) {
            shapeName = copy.defaultShapeName;
        }
        if (shapeSize.isEmpty()) {
            shapeSize = copy.defaultShapeSize;
        }
        if (shapeName.isEmpty()) {
            shapeName = override.defaultShapeName;
        }
        if (shapeSize.isEmpty()) {
            shapeSize = override.defaultShapeSize;
        }
        this.shapeName = shapeName;
        this.shapeSize = shapeSize;
        this.defaultShapeName = shapeName;
        this.defaultShapeSize = shapeSize;
        this.shape = ShapeConfiguration.createShape(shapeName, shapeSize);
    }

    public void setDefaultShape(Shape defaultShape) {
        if (defaultShape == null) {
            throw new IllegalArgumentException("Default shape is null");
        }
        PropertyList defaultShapeProps = this.getShapeProperties(defaultShape);
        this.defaultShapeName = defaultShapeProps.getProperty("shape");
        this.defaultShapeSize = defaultShapeProps.getProperty("shapesize");
        String tmpShapeName = this.shapeName;
        String tmpShapeSize = this.shapeSize;
        if (tmpShapeName.isEmpty()) {
            tmpShapeName = this.defaultShapeName;
        }
        if (tmpShapeSize.isEmpty()) {
            tmpShapeSize = this.defaultShapeSize;
        }
        this.shape = ShapeConfiguration.createShape(tmpShapeName, tmpShapeSize);
    }

    private static Shape createShape(String shapeName, String shapeSize) {
        if (shapeName == null) {
            throw new IllegalArgumentException("Shape name is null");
        }
        if (shapeSize == null) {
            throw new IllegalArgumentException("Shape size is null");
        }
        Shape returnShape = null;
        if (!shapeName.isEmpty() && !shapeSize.isEmpty()) {
            double size = 6.0;
            if (shapeSize.equals("Small")) {
                size = 4.0;
            } else if (shapeSize.equals("Large")) {
                size = 8.0;
            }
            double delta = size / 2.0;
            if (shapeName.equalsIgnoreCase("Square")) {
                returnShape = new Rectangle2D.Double(-delta, -delta, size, size);
            } else if (shapeName.equalsIgnoreCase("Circle")) {
                returnShape = new Ellipse2D.Double(-delta, -delta, size, size);
            } else if (shapeName.equalsIgnoreCase("Up Triangle")) {
                int[] xPoints = ShapeConfiguration.intArray(0.0, delta, -delta);
                int[] yPoints = ShapeConfiguration.intArray(-delta, delta, delta);
                returnShape = new Polygon(xPoints, yPoints, 3);
            } else if (shapeName.equalsIgnoreCase("Diamond")) {
                int[] xPoints = ShapeConfiguration.intArray(0.0, delta, 0.0, -delta);
                int[] yPoints = ShapeConfiguration.intArray(-delta, 0.0, delta, 0.0);
                returnShape = new Polygon(xPoints, yPoints, 4);
            } else if (shapeName.equalsIgnoreCase("Horizontal Rectangle")) {
                returnShape = new Rectangle2D.Double(-delta, -delta / 2.0, size, size / 2.0);
            } else if (shapeName.equalsIgnoreCase("Down Triangle")) {
                int[] xPoints = ShapeConfiguration.intArray(-delta, delta, 0.0);
                int[] yPoints = ShapeConfiguration.intArray(-delta, -delta, delta);
                returnShape = new Polygon(xPoints, yPoints, 3);
            } else if (shapeName.equalsIgnoreCase("Horizontal Ellipse")) {
                returnShape = new Ellipse2D.Double(-delta, -delta / 2.0, size, size / 2.0);
            } else if (shapeName.equalsIgnoreCase("Right Triangle")) {
                int[] xPoints = ShapeConfiguration.intArray(-delta, delta, -delta);
                int[] yPoints = ShapeConfiguration.intArray(-delta, 0.0, delta);
                returnShape = new Polygon(xPoints, yPoints, 3);
            } else if (shapeName.equalsIgnoreCase("Vertical Rectangle")) {
                returnShape = new Rectangle2D.Double(-delta / 2.0, -delta, size / 2.0, size);
            } else if (shapeName.equalsIgnoreCase("Left Triangle")) {
                int[] xPoints = ShapeConfiguration.intArray(-delta, delta, delta);
                int[] yPoints = ShapeConfiguration.intArray(0.0, -delta, delta);
                returnShape = new Polygon(xPoints, yPoints, 3);
            } else {
                returnShape = shapeName.equalsIgnoreCase("Diagonal Cross") ? ShapeUtilities.createDiagonalCross((float)((float)delta), (float)0.1f) : (shapeName.equalsIgnoreCase("Cross") ? ShapeUtilities.createRegularCross((float)((float)delta), (float)0.1f) : null);
            }
        }
        return returnShape;
    }

    private static int[] intArray(double a, double b, double c) {
        return new int[]{(int)a, (int)b, (int)c};
    }

    private static int[] intArray(double a, double b, double c, double d) {
        return new int[]{(int)a, (int)b, (int)c, (int)d};
    }

    public Shape getShape() {
        return this.shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public boolean hasShape() {
        boolean returnValue = false;
        if (this.shape != null) {
            returnValue = true;
        }
        return returnValue;
    }

    public boolean hasShapeOverride() {
        boolean returnValue = false;
        if (!this.shapeName.isEmpty() || !this.shapeSize.isEmpty()) {
            returnValue = true;
        }
        return returnValue;
    }

    public Shape getShape(Shape defaultShape) {
        Shape returnValue = defaultShape;
        if (this.shape != null) {
            returnValue = this.shape;
        }
        return returnValue;
    }

    private PropertyList getShapeProperties(Shape defaultShape) {
        PropertyList shapeProps = new PropertyList();
        for (String tempShapeName : shapeNameList) {
            String tempShapeSize;
            if (ShapeUtilities.equal((Shape)defaultShape, (Shape)ShapeConfiguration.createShape(tempShapeName, tempShapeSize = "Small"))) {
                shapeProps.setProperty("shape", tempShapeName);
                shapeProps.setProperty("shapesize", tempShapeSize);
                break;
            }
            tempShapeSize = "Standard";
            if (ShapeUtilities.equal((Shape)defaultShape, (Shape)ShapeConfiguration.createShape(tempShapeName, tempShapeSize))) {
                shapeProps.setProperty("shape", tempShapeName);
                shapeProps.setProperty("shapesize", tempShapeSize);
                break;
            }
            tempShapeSize = "Large";
            if (!ShapeUtilities.equal((Shape)defaultShape, (Shape)ShapeConfiguration.createShape(tempShapeName, tempShapeSize))) continue;
            shapeProps.setProperty("shape", tempShapeName);
            shapeProps.setProperty("shapesize", tempShapeSize);
            break;
        }
        if (shapeProps.getProperty("shape").isEmpty()) {
            throw new IllegalArgumentException("Cannot find match for default shape: " + defaultShape);
        }
        return shapeProps;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape((Shape)this.shape, (ObjectOutputStream)stream);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.shape = SerialUtilities.readShape((ObjectInputStream)stream);
    }

    static {
        shapeNameList.add("Square");
        shapeNameList.add("Circle");
        shapeNameList.add("Up Triangle");
        shapeNameList.add("Diamond");
        shapeNameList.add("Horizontal Rectangle");
        shapeNameList.add("Down Triangle");
        shapeNameList.add("Horizontal Ellipse");
        shapeNameList.add("Right Triangle");
        shapeNameList.add("Vertical Rectangle");
        shapeNameList.add("Left Triangle");
        shapeNameList.add("Diagonal Cross");
        shapeNameList.add("Cross");
    }
}

