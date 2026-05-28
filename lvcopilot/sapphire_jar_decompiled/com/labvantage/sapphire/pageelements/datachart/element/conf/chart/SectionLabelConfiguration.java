/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.FontConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.PaintConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StandardSectionItemLabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PiePlotConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class SectionLabelConfiguration
implements Serializable {
    private static final String DEFAULT_LABEL_TYPE = "Standard";
    private static final String DEFAULT_LABELS_VISIBLE = "Y";
    private static final String DEFAULT_BACKGROUND_COLOR = "fdffc2";
    private final String labelType;
    private final boolean labelsVisible;
    private final PaintConfiguration backgroundColor;
    private final FontConfiguration fontConf;
    private final StandardSectionItemLabelConfiguration standardSectionItemLabelConf;

    public SectionLabelConfiguration(PropertyList labelProps, PiePlotConfiguration parent) {
        if (labelProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.labelsVisible = labelProps.getProperty("labelsvisible", DEFAULT_LABELS_VISIBLE).toLowerCase().startsWith("y");
        this.labelType = labelProps.getProperty("labeltype", DEFAULT_LABEL_TYPE);
        labelProps.getPropertyListNotNull("colorprops").setProperty("color", labelProps.getPropertyListNotNull("colorprops").getProperty("color", DEFAULT_BACKGROUND_COLOR));
        this.backgroundColor = new PaintConfiguration(labelProps);
        this.fontConf = new FontConfiguration(labelProps.getPropertyListNotNull("fontprops"), parent.getParent().getParent());
        this.standardSectionItemLabelConf = new StandardSectionItemLabelConfiguration(labelProps.getPropertyListNotNull("standardsectionlabelprops"), this);
    }

    public SectionLabelConfiguration(SectionLabelConfiguration copy) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        this.labelType = copy.labelType;
        this.labelsVisible = copy.labelsVisible;
        this.standardSectionItemLabelConf = new StandardSectionItemLabelConfiguration(copy.standardSectionItemLabelConf, this);
        this.backgroundColor = new PaintConfiguration(copy.backgroundColor);
        this.fontConf = new FontConfiguration(copy.fontConf);
    }

    public boolean isLabelsVisible() {
        return this.labelsVisible;
    }

    public String getLabelType() {
        return this.labelType;
    }

    public StandardSectionItemLabelConfiguration getStandardSectionItemLabelConfiguration() {
        return this.standardSectionItemLabelConf;
    }

    public FontConfiguration getFontConf() {
        return this.fontConf;
    }

    public PaintConfiguration getBackgroundColor() {
        return this.backgroundColor;
    }
}

