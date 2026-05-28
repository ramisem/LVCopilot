/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import java.io.Serializable;
import sapphire.xml.PropertyList;

public abstract class AbstractLineAndShapeRendererConfiguration
implements Serializable {
    private static final boolean DEFAULT_SHAPES_FILLED = true;
    private static final boolean DEFAULT_LINES_VISIBLE = true;
    private static final boolean DEFAULT_USE_FILL_PAINT = false;
    private static final boolean DEFAULT_USE_OUTLINE_PAINT = false;
    private static final boolean DEFAULT_SHAPES_VISIBLE = true;
    private final boolean shapesFilled;
    private final boolean useFillPaint;
    private final boolean useOutlinePaint;
    private final boolean linesVisible;
    private final boolean shapesVisible;

    public AbstractLineAndShapeRendererConfiguration(PropertyList abstractLineAndShapeRendererProps) {
        this(abstractLineAndShapeRendererProps, null);
    }

    public AbstractLineAndShapeRendererConfiguration(AbstractLineAndShapeRendererConfiguration copy) {
        if (copy == null) {
            throw new IllegalArgumentException("Source is null");
        }
        this.shapesFilled = copy.shapesFilled;
        this.useFillPaint = copy.useFillPaint;
        this.linesVisible = copy.linesVisible;
        this.useOutlinePaint = copy.useOutlinePaint;
        this.shapesVisible = copy.shapesVisible;
    }

    public AbstractLineAndShapeRendererConfiguration(PropertyList abstractLineAndShapeRendererProps, AbstractLineAndShapeRendererConfiguration defaultAbstractLineAndShapeConfiguration) {
        if (abstractLineAndShapeRendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        boolean defaultShapesFilled = true;
        if (defaultAbstractLineAndShapeConfiguration != null) {
            defaultShapesFilled = defaultAbstractLineAndShapeConfiguration.isShapesFilled();
        }
        this.shapesFilled = abstractLineAndShapeRendererProps.getProperty("shapesfilled", defaultShapesFilled ? "Y" : "N").toLowerCase().startsWith("y");
        boolean defaultSeriesLineVisible = true;
        if (defaultAbstractLineAndShapeConfiguration != null) {
            defaultSeriesLineVisible = defaultAbstractLineAndShapeConfiguration.isLinesVisible();
        }
        this.linesVisible = abstractLineAndShapeRendererProps.getProperty("linesvisible", defaultSeriesLineVisible ? "Y" : "N").toLowerCase().startsWith("y");
        boolean defaultUseFillPaint = false;
        if (defaultAbstractLineAndShapeConfiguration != null) {
            defaultUseFillPaint = defaultAbstractLineAndShapeConfiguration.isUseFillPaint();
        }
        this.useFillPaint = abstractLineAndShapeRendererProps.getProperty("usefillpaint", defaultUseFillPaint ? "Y" : "N").toLowerCase().startsWith("y");
        boolean defaultUseOutlinePaint = false;
        if (defaultAbstractLineAndShapeConfiguration != null) {
            defaultUseOutlinePaint = defaultAbstractLineAndShapeConfiguration.isUseOutlinePaint();
        }
        this.useOutlinePaint = abstractLineAndShapeRendererProps.getProperty("useoutlinepaint", defaultUseOutlinePaint ? "Y" : "N").toLowerCase().startsWith("y");
        boolean defaultSeriesShapesVisible = true;
        if (defaultAbstractLineAndShapeConfiguration != null) {
            defaultSeriesShapesVisible = defaultAbstractLineAndShapeConfiguration.isShapesVisible();
        }
        this.shapesVisible = abstractLineAndShapeRendererProps.getProperty("shapesvisible", defaultSeriesShapesVisible ? "Y" : "N").toLowerCase().startsWith("y");
    }

    public AbstractLineAndShapeRendererConfiguration(AbstractLineAndShapeRendererConfiguration copy, AbstractLineAndShapeRendererConfiguration override) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (override == null) {
            throw new IllegalArgumentException("Override is null");
        }
        this.shapesFilled = override.shapesFilled;
        this.useFillPaint = override.useFillPaint;
        this.linesVisible = override.linesVisible;
        this.useOutlinePaint = override.useOutlinePaint;
        this.shapesVisible = override.shapesVisible;
    }

    public boolean isShapesFilled() {
        return this.shapesFilled;
    }

    public boolean isLinesVisible() {
        return this.linesVisible;
    }

    public boolean isUseFillPaint() {
        return this.useFillPaint;
    }

    public boolean isUseOutlinePaint() {
        return this.useOutlinePaint;
    }

    public boolean isShapesVisible() {
        return this.shapesVisible;
    }
}

