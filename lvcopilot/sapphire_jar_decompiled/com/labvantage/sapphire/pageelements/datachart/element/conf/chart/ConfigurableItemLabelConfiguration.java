/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.LabelConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class ConfigurableItemLabelConfiguration
implements Serializable {
    private static final String DEFAULT_LABEL_STRING = "{2}";
    private final LabelConfiguration parent;
    private final String labelString;

    public ConfigurableItemLabelConfiguration(PropertyList standardItemLabelProps, LabelConfiguration parent) {
        if (standardItemLabelProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.labelString = standardItemLabelProps.getProperty("labelstring", DEFAULT_LABEL_STRING);
    }

    public ConfigurableItemLabelConfiguration(ConfigurableItemLabelConfiguration copy, LabelConfiguration parent) {
        this.labelString = copy.labelString;
        this.parent = parent;
    }

    public String getLabelString() {
        return this.labelString;
    }

    public LabelConfiguration getParent() {
        return this.parent;
    }
}

