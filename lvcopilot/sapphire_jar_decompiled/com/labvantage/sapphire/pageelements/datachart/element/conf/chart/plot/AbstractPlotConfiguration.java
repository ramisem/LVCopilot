/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfigurationInterface;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ToolbarConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public abstract class AbstractPlotConfiguration
implements PlotConfigurationInterface,
Serializable {
    private final PlotConfiguration parent;
    private ToolbarConfiguration toolbarConf;

    public AbstractPlotConfiguration(PropertyList toolbarPlotProps, PlotConfiguration parent) {
        if (toolbarPlotProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.toolbarConf = new ToolbarConfiguration(toolbarPlotProps.getPropertyListNotNull("toolbarprops"), parent);
        this.parent = parent;
    }

    public AbstractPlotConfiguration(AbstractPlotConfiguration copy, PlotConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        this.toolbarConf = new ToolbarConfiguration(copy.toolbarConf, parent);
        this.parent = parent;
    }

    @Override
    public ToolbarConfiguration getToolbarConfiguration() {
        return this.toolbarConf;
    }

    @Override
    public PlotConfiguration getParent() {
        return this.parent;
    }
}

