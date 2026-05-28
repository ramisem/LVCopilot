/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.FontConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class TextTitleConfiguration
implements Serializable {
    private static final String DEFAULT_TEXT = "";
    private final StringExpression text;
    private final PaintConfiguration paintConf;
    private final TitleConfiguration parent;
    private final FontConfiguration fontConf;

    public TextTitleConfiguration(PropertyList textTitleProps, TitleConfiguration parent) {
        this.parent = parent;
        this.text = new StringExpression(textTitleProps.getProperty("text", DEFAULT_TEXT));
        this.paintConf = new PaintConfiguration(textTitleProps.getPropertyListNotNull("paintprops"));
        this.fontConf = new FontConfiguration(textTitleProps.getPropertyListNotNull("fontprops"), this.getParent().getParent());
    }

    public FontConfiguration getFontConfiguration() {
        return this.fontConf;
    }

    public PaintConfiguration getPaintConfiguration() {
        return this.paintConf;
    }

    public StringExpression getText() {
        return this.text;
    }

    public TitleConfiguration getParent() {
        return this.parent;
    }
}

