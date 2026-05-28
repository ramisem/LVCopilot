/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.StandardEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.ToolTipItemConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ToolTipEventConfiguration
implements Serializable {
    private final StandardEventConfiguration parent;
    private final List<ToolTipItemConfiguration> toolTipItemConfigList;
    private final String asString;

    public ToolTipEventConfiguration(PropertyList toolTipEventProps, StandardEventConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (toolTipEventProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        PropertyListCollection items = toolTipEventProps.getCollectionNotNull("tooltipitems");
        this.toolTipItemConfigList = new ArrayList<ToolTipItemConfiguration>();
        for (int i = 0; i < items.size(); ++i) {
            PropertyList toolTipItemProps = items.getPropertyList(i);
            ToolTipItemConfiguration toolTipItemConfig = new ToolTipItemConfiguration(toolTipItemProps, this);
            this.toolTipItemConfigList.add(toolTipItemConfig);
        }
        this.asString = toolTipEventProps.toJSONString();
        this.parent = parent;
    }

    public ToolTipEventConfiguration(ToolTipEventConfiguration copy, StandardEventConfiguration parent) {
        this.parent = parent;
        this.toolTipItemConfigList = new ArrayList<ToolTipItemConfiguration>();
        for (int i = 0; i < copy.toolTipItemConfigList.size(); ++i) {
            ToolTipItemConfiguration toolTipItemConfig = new ToolTipItemConfiguration(copy.toolTipItemConfigList.get(i), this);
            this.toolTipItemConfigList.add(toolTipItemConfig);
        }
        this.asString = copy.asString;
    }

    public List<ToolTipItemConfiguration> getToolTipItemConfiguration() {
        return this.toolTipItemConfigList;
    }

    public StandardEventConfiguration getParent() {
        return this.parent;
    }

    public String toJSONString() {
        return this.asString;
    }
}

