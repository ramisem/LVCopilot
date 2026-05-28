/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ToolbarConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ButtonSetConfiguration
implements Serializable {
    private static final String DEFAULT_VISIBLE = "Y";
    private final ToolbarConfiguration parent;
    private final String buttonSetId;
    private final boolean visible;
    private final List<ButtonConfiguration> buttonConfList;

    public ButtonSetConfiguration(PropertyList buttonSetProps, ToolbarConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (buttonSetProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.buttonSetId = buttonSetProps.getProperty("buttonsetid", "");
        this.visible = buttonSetProps.getProperty("visible", DEFAULT_VISIBLE).toLowerCase().startsWith("y");
        this.buttonConfList = new ArrayList<ButtonConfiguration>();
        PropertyListCollection buttonCollection = buttonSetProps.getCollectionNotNull("buttoncollection");
        for (int i = 0; i < buttonCollection.size(); ++i) {
            PropertyList buttonProps = buttonCollection.getPropertyList(i);
            this.buttonConfList.add(new ButtonConfiguration(buttonProps, this));
        }
    }

    public ButtonSetConfiguration(ButtonSetConfiguration copy, ToolbarConfiguration parent) {
        if (copy == null) {
            throw new IllegalArgumentException("Copy is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.buttonSetId = copy.buttonSetId;
        this.visible = copy.visible;
        this.buttonConfList = new ArrayList<ButtonConfiguration>();
        for (ButtonConfiguration buttonConf : copy.buttonConfList) {
            this.buttonConfList.add(new ButtonConfiguration(buttonConf, this));
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public String getButtonSetId() {
        return this.buttonSetId;
    }

    public ToolbarConfiguration getParent() {
        return this.parent;
    }

    public List<ButtonConfiguration> getButtonConfigurationList() {
        return this.buttonConfList;
    }
}

