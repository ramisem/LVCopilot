/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ToolbarConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ButtonSetConfiguration
implements Serializable {
    private final ToolbarConfiguration parent;
    private final String buttonSetId;
    private final String label;
    private final String tip;
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
        this.label = buttonSetProps.getProperty("label");
        this.tip = buttonSetProps.getProperty("tip");
        this.buttonConfList = new ArrayList<ButtonConfiguration>();
        PropertyListCollection buttonCollection = buttonSetProps.getCollectionNotNull("buttoncollection");
        for (int i = 0; i < buttonCollection.size(); ++i) {
            PropertyList buttonProps = buttonCollection.getPropertyList(i);
            this.buttonConfList.add(new ButtonConfiguration(buttonProps, this));
        }
    }

    public ButtonSetConfiguration(ButtonSetConfiguration copy, ToolbarConfiguration parent) {
        this.parent = parent;
        this.buttonConfList = new ArrayList<ButtonConfiguration>();
        for (ButtonConfiguration buttonConf : copy.buttonConfList) {
            this.buttonConfList.add(new ButtonConfiguration(buttonConf, this));
        }
        this.buttonSetId = copy.buttonSetId;
        this.label = copy.label;
        this.tip = copy.tip;
    }

    public String getTip() {
        return this.tip;
    }

    public String getButtonSetId() {
        return this.buttonSetId;
    }

    public ButtonConfiguration getButtonConfiguration(String buttonId) {
        if (buttonId == null) {
            throw new IllegalArgumentException("Button ID is null");
        }
        ButtonConfiguration returnConf = null;
        for (ButtonConfiguration buttonConf : this.buttonConfList) {
            if (!buttonConf.getButtonId().equals(buttonId)) continue;
            returnConf = buttonConf;
            break;
        }
        if (returnConf == null) {
            throw new IllegalArgumentException("Button with ID " + buttonId + " not found");
        }
        return returnConf;
    }

    public String getLabel() {
        return this.label;
    }

    public ToolbarConfiguration getParent() {
        return this.parent;
    }

    public List<ButtonConfiguration> getButtonConfigurationList() {
        return this.buttonConfList;
    }
}

