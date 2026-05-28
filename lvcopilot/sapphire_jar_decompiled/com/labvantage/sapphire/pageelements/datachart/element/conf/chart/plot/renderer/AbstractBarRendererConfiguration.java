/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.renderer.category.BarRenderer
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer;

import java.io.Serializable;
import org.jfree.chart.renderer.category.BarRenderer;
import sapphire.xml.PropertyList;

public class AbstractBarRendererConfiguration
implements Serializable {
    private static final boolean DEFAULT_SHADOWS_VISIBLE = BarRenderer.getDefaultShadowsVisible();
    public static final String STANDARD_BAR_PAINTER = "Standard";
    private static final String DEFAULT_BAR_PAINTER = "Standard";
    private static final boolean DEFAULT_DRAW_BAR_OUTLINE = false;
    private final boolean shadowsVisible;
    private final String barPainter;
    private final boolean drawBarOutline;

    public AbstractBarRendererConfiguration(PropertyList abstractBarRendererProps) {
        this(abstractBarRendererProps, null);
    }

    public AbstractBarRendererConfiguration(AbstractBarRendererConfiguration copy) {
        if (copy == null) {
            throw new IllegalArgumentException("Source is null");
        }
        this.shadowsVisible = copy.shadowsVisible;
        this.barPainter = copy.barPainter;
        this.drawBarOutline = copy.drawBarOutline;
    }

    public AbstractBarRendererConfiguration(PropertyList abstractBarRendererProps, AbstractBarRendererConfiguration defaultAbstractBarRendererConfiguration) {
        if (abstractBarRendererProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        boolean defaultShadowsVisible = DEFAULT_SHADOWS_VISIBLE;
        if (defaultAbstractBarRendererConfiguration != null) {
            defaultShadowsVisible = defaultAbstractBarRendererConfiguration.isShadowsVisible();
        }
        this.shadowsVisible = abstractBarRendererProps.getProperty("shadowvisible", defaultShadowsVisible ? "Y" : "N").toLowerCase().startsWith("y");
        String defaultUseGradientPaint = "Standard";
        if (defaultAbstractBarRendererConfiguration != null) {
            defaultUseGradientPaint = defaultAbstractBarRendererConfiguration.getBarPainter();
        }
        this.barPainter = abstractBarRendererProps.getProperty("barpainter", defaultUseGradientPaint);
        boolean defaultDrawBarOutlines = false;
        if (defaultAbstractBarRendererConfiguration != null) {
            defaultDrawBarOutlines = defaultAbstractBarRendererConfiguration.isDrawBarOutline();
        }
        this.drawBarOutline = abstractBarRendererProps.getProperty("drawbaroutlines", defaultDrawBarOutlines ? "Y" : "N").toLowerCase().startsWith("y");
    }

    public boolean isShadowsVisible() {
        return this.shadowsVisible;
    }

    public String getBarPainter() {
        return this.barPainter;
    }

    public boolean isDrawBarOutline() {
        return this.drawBarOutline;
    }
}

