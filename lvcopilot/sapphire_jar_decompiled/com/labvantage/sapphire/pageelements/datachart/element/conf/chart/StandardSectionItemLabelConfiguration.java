/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.SectionLabelConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class StandardSectionItemLabelConfiguration
implements Serializable {
    private static final String DEFAULT_PERC_NUMBER_FORMAT = "0%";
    private static final String DEFAULT_NUMBER_FORMAT = "0";
    private static final String DEFAULT_SECTION_LABEL_STRING = "{0}: {1} ({2})";
    private final String numberFormat;
    private final String percentageNumberFormat;
    private final String labelString;
    private final SectionLabelConfiguration parent;

    public StandardSectionItemLabelConfiguration(PropertyList standardSectionItemLabelProps, SectionLabelConfiguration parent) {
        if (standardSectionItemLabelProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.numberFormat = standardSectionItemLabelProps.getProperty("numberformat", DEFAULT_NUMBER_FORMAT);
        this.percentageNumberFormat = standardSectionItemLabelProps.getProperty("numberformatperc", DEFAULT_PERC_NUMBER_FORMAT);
        this.labelString = standardSectionItemLabelProps.getProperty("labelstring", DEFAULT_SECTION_LABEL_STRING);
        this.parent = parent;
    }

    public StandardSectionItemLabelConfiguration(StandardSectionItemLabelConfiguration copy, SectionLabelConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.numberFormat = copy.numberFormat;
        this.percentageNumberFormat = copy.percentageNumberFormat;
        this.labelString = copy.labelString;
    }

    public String getNumberFormat() {
        return this.numberFormat;
    }

    public String getPercentageNumberFormat() {
        return this.percentageNumberFormat;
    }

    public String getLabelString() {
        return this.labelString;
    }

    public SectionLabelConfiguration getParent() {
        return this.parent;
    }
}

