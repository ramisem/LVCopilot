/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ButtonSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ToolbarConfiguration
implements Serializable {
    private final PlotConfiguration parent;
    private final List<ButtonSetConfiguration> buttonSetConfList;

    public ToolbarConfiguration(PropertyList toolbarProps, PlotConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (toolbarProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.buttonSetConfList = new ArrayList<ButtonSetConfiguration>();
        PropertyListCollection buttonSetCollection = toolbarProps.getCollectionNotNull("buttonsetcollection");
        for (int i = 0; i < buttonSetCollection.size(); ++i) {
            PropertyList buttonSetProps = buttonSetCollection.getPropertyList(i);
            this.buttonSetConfList.add(new ButtonSetConfiguration(buttonSetProps, this));
        }
    }

    public ToolbarConfiguration(ToolbarConfiguration copy, PlotConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.buttonSetConfList = new ArrayList<ButtonSetConfiguration>();
        for (ButtonSetConfiguration buttonSetConf : copy.buttonSetConfList) {
            this.buttonSetConfList.add(new ButtonSetConfiguration(buttonSetConf, this));
        }
    }

    public PlotConfiguration getParent() {
        return this.parent;
    }

    public List<ButtonSetConfiguration> getButtonSetConfigurationList() {
        return this.buttonSetConfList;
    }
}

