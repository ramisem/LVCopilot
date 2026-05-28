/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonSetConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ToolbarConfiguration
implements Serializable {
    private static final String DEFAULT_FLOATING = "Y";
    private static final String DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.LEFT.getName();
    private static final String DEFAULT_COLLAPSE = "N";
    private final ChartConfiguration parent;
    private final boolean floating;
    private final HorizontalAlignment horizontalAlignment;
    private final boolean collapse;
    private final List<ButtonSetConfiguration> buttonSetConfList;

    public ToolbarConfiguration(PropertyList toolbarProps, ChartConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (toolbarProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.floating = toolbarProps.getProperty("floating", DEFAULT_FLOATING).toLowerCase().startsWith("y");
        this.horizontalAlignment = HorizontalAlignment.fromString(toolbarProps.getProperty("horizontalalignment", DEFAULT_HORIZONTAL_ALIGNMENT));
        this.collapse = toolbarProps.getProperty("collapse", DEFAULT_COLLAPSE).toLowerCase().startsWith("y");
        this.buttonSetConfList = new ArrayList<ButtonSetConfiguration>();
        PropertyListCollection buttonSetCollection = toolbarProps.getCollectionNotNull("buttonsetcollection");
        for (int i = 0; i < buttonSetCollection.size(); ++i) {
            PropertyList buttonSetProps = buttonSetCollection.getPropertyList(i);
            this.buttonSetConfList.add(new ButtonSetConfiguration(buttonSetProps, this));
        }
    }

    public ButtonSetConfiguration getButtonSetConfiguration(String buttonSetId) {
        if (buttonSetId == null) {
            throw new IllegalArgumentException("Button set ID is null");
        }
        ButtonSetConfiguration returnConf = null;
        for (ButtonSetConfiguration buttonSetConf : this.buttonSetConfList) {
            if (!buttonSetConf.getButtonSetId().equals(buttonSetId)) continue;
            returnConf = buttonSetConf;
            break;
        }
        if (returnConf == null) {
            throw new IllegalArgumentException("Button set with ID " + buttonSetId + " not found");
        }
        return returnConf;
    }

    public boolean isCollapse() {
        return this.collapse;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    public boolean isFloating() {
        return this.floating;
    }

    public ChartConfiguration getParent() {
        return this.parent;
    }

    public List<ButtonSetConfiguration> getButtonSetConfigurationList() {
        return this.buttonSetConfList;
    }

    public static enum HorizontalAlignment {
        LEFT("Left"),
        CENTER("Center"),
        RIGHT("Right");

        private final String name;

        private HorizontalAlignment(String name) {
            this.name = name;
        }

        public static HorizontalAlignment fromString(String name) {
            if (name != null) {
                for (HorizontalAlignment type : HorizontalAlignment.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

