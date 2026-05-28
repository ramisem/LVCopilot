/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ConfigurableItemLabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.FontConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.StandardItemLabelConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.renderer.RendererConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class LabelConfiguration
implements Serializable {
    public static final String STANDARD_LABEL_TYPE = "Standard";
    public static final String EXPRESSION_LABEL_TYPE = "Expression";
    private static final String DEFAULT_LABEL_TYPE = "Standard";
    private final RendererConfiguration parent;
    private final String labelType;
    private final Boolean labelsVisible;
    private final StandardItemLabelConfiguration standardItemLabelConf;
    private final FontConfiguration fontConf;
    private final ConfigurableItemLabelConfiguration configurableLabelConf;

    public LabelConfiguration(PropertyList labelProps, RendererConfiguration parent) {
        if (labelProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        String labelVisibleStr = labelProps.getProperty("labelsvisible");
        this.labelsVisible = labelVisibleStr.isEmpty() ? null : Boolean.valueOf(labelVisibleStr.toLowerCase().equals("y"));
        this.labelType = labelProps.getProperty("labeltype", "Standard");
        this.standardItemLabelConf = new StandardItemLabelConfiguration(labelProps.getPropertyListNotNull("standarditemlabelprops"), this);
        this.configurableLabelConf = new ConfigurableItemLabelConfiguration(labelProps.getPropertyListNotNull("configurableitemlabelprops"), this);
        this.fontConf = new FontConfiguration(labelProps.getPropertyListNotNull("fontprops"), this.getParent() == null ? null : this.getParent().getParent().getParent().getParent());
    }

    public LabelConfiguration(LabelConfiguration copy, RendererConfiguration parent) {
        this.parent = parent;
        this.standardItemLabelConf = new StandardItemLabelConfiguration(copy.standardItemLabelConf, this);
        this.configurableLabelConf = new ConfigurableItemLabelConfiguration(copy.configurableLabelConf, this);
        this.labelsVisible = copy.labelsVisible;
        this.labelType = copy.labelType;
        this.fontConf = new FontConfiguration(copy.fontConf);
    }

    public RendererConfiguration getParent() {
        return this.parent;
    }

    public Boolean getLabelsVisible() {
        return this.labelsVisible;
    }

    public String getLabelType() {
        return this.labelType;
    }

    public StandardItemLabelConfiguration getStandardItemLabelConfiguration() {
        return this.standardItemLabelConf;
    }

    public FontConfiguration getFontConf() {
        return this.fontConf;
    }

    public ConfigurableItemLabelConfiguration getConfigurableLabelConfiguration() {
        return this.configurableLabelConf;
    }
}

