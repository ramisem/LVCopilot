/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LabelConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class StandardItemLabelConfiguration
implements Serializable {
    private static final String DEFAULT_PERC_NUMBER_FORMAT_STR = "";
    private static final String DEFAULT_NUMBER_FORMAT_STR = "";
    private static final String DEFAULT_LABEL_STRING = "{2}";
    private final LabelConfiguration parent;
    private final String numberFormatStr;
    private final String percentageNumberFormatStr;
    private final String labelString;

    public StandardItemLabelConfiguration(PropertyList standardItemLabelProps, LabelConfiguration parent) {
        if (standardItemLabelProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.numberFormatStr = standardItemLabelProps.getProperty("numberformat", "");
        this.percentageNumberFormatStr = standardItemLabelProps.getProperty("numberformatperc", "");
        this.labelString = standardItemLabelProps.getProperty("labelstring", DEFAULT_LABEL_STRING);
    }

    public StandardItemLabelConfiguration(StandardItemLabelConfiguration copy, LabelConfiguration parent) {
        this.numberFormatStr = copy.numberFormatStr;
        this.labelString = copy.labelString;
        this.percentageNumberFormatStr = copy.percentageNumberFormatStr;
        this.parent = parent;
    }

    public String getNumberFormatStr() {
        return this.numberFormatStr;
    }

    public String getPercentageNumberFormatStr() {
        return this.percentageNumberFormatStr;
    }

    public String getLabelString() {
        return this.labelString;
    }

    public LabelConfiguration getParent() {
        return this.parent;
    }
}

