/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.io.SerialUtilities
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.io.SerialUtilities;
import sapphire.xml.PropertyList;

public final class StrokeConfiguration
implements Serializable {
    private static final String DEFAULT_STROKE = "";
    private static final String DEFAULT_STROKE_WIDTH = "Standard";
    private transient Stroke stroke;

    public StrokeConfiguration(PropertyList strokeProps) {
        if (strokeProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.stroke = StrokeConfiguration.createStroke(strokeProps.getProperty("stroke", DEFAULT_STROKE), strokeProps.getProperty("strokewidth", DEFAULT_STROKE_WIDTH));
    }

    public StrokeConfiguration(StrokeConfiguration copy) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        this.stroke = copy.stroke;
    }

    private static BasicStroke createStroke(String strokeName, String strokeWidth) {
        if (strokeName == null) {
            throw new IllegalArgumentException("Stroke name is null");
        }
        if (strokeWidth == null) {
            strokeWidth = DEFAULT_STROKE_WIDTH;
        }
        float width = 1.0f;
        if (strokeWidth.equals("Bold")) {
            width = 1.5f;
        } else if (strokeWidth.equals("Light")) {
            width = 0.5f;
        }
        BasicStroke newStroke = strokeName.equals("Solid") ? new BasicStroke(width) : (strokeName.equals("Short Dash") ? new BasicStroke(width, 1, 1, 1.0f, new float[]{2.0f, 4.0f}, 0.0f) : (strokeName.equals("Long Dash") ? new BasicStroke(width, 1, 1, 1.0f, new float[]{4.0f, 2.0f}, 0.0f) : (strokeName.equals("Dotted") ? new BasicStroke(width, 1, 1, 1.0f, new float[]{0.5f, 4.0f}, 0.0f) : (strokeName.equals("Mixed Dash") ? new BasicStroke(width, 1, 1, 1.0f, new float[]{2.0f, 4.0f, 4.0f, 2.0f}, 0.0f) : null))));
        return newStroke;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Stroke getStroke() {
        return this.stroke;
    }

    public boolean hasStroke() {
        boolean returnValue = false;
        if (this.stroke != null) {
            returnValue = true;
        }
        return returnValue;
    }

    public Stroke getStroke(Stroke defaultStroke) {
        Stroke returnValue = defaultStroke;
        if (this.stroke != null) {
            returnValue = this.stroke;
        }
        return returnValue;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke((Stroke)this.stroke, (ObjectOutputStream)stream);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.stroke = SerialUtilities.readStroke((ObjectInputStream)stream);
    }
}

