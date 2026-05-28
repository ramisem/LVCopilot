/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.annotations.XYTextAnnotation
 *  org.jfree.ui.TextAnchor
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TextAnchorType;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.annotation.XYAnnotationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BigDecimalExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.ui.TextAnchor;
import sapphire.xml.PropertyList;

public final class XYTextAnnotationConfiguration
implements Serializable {
    private static final String DEFAULT_X = "1";
    private static final String DEFAULT_Y = "1";
    private static final String DEFAULT_TEXT = "";
    private static final String DEFAULT_TEXT_ANCHOR = TextAnchorType.fromTextAnchor(XYTextAnnotation.DEFAULT_TEXT_ANCHOR).getName();
    private final XYAnnotationConfiguration parent;
    private final StringExpression text;
    private final BigDecimalExpression x;
    private final BigDecimalExpression y;
    private final TextAnchor textAnchor;

    public XYTextAnnotationConfiguration(PropertyList xyTextAnnotationProps, XYAnnotationConfiguration parent) {
        if (xyTextAnnotationProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.text = new StringExpression(xyTextAnnotationProps.getProperty("text", DEFAULT_TEXT));
        this.x = new BigDecimalExpression(xyTextAnnotationProps.getProperty("x", "1"));
        this.y = new BigDecimalExpression(xyTextAnnotationProps.getProperty("y", "1"));
        this.textAnchor = TextAnchorType.fromString(xyTextAnnotationProps.getProperty("textanchor", DEFAULT_TEXT_ANCHOR)).getTextAnchor();
    }

    public XYTextAnnotationConfiguration(XYTextAnnotationConfiguration copy, XYAnnotationConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.text = new StringExpression(copy.text);
        this.x = new BigDecimalExpression(copy.x);
        this.y = new BigDecimalExpression(copy.y);
        this.textAnchor = copy.textAnchor;
    }

    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }

    public StringExpression getText() {
        return this.text;
    }

    public BigDecimalExpression getX() {
        return this.x;
    }

    public BigDecimalExpression getY() {
        return this.y;
    }

    public XYAnnotationConfiguration getParent() {
        return this.parent;
    }
}

