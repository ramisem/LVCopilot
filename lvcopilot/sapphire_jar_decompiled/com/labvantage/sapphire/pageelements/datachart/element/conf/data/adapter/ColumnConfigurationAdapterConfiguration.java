/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.PseudoColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ColumnConfigurationAdapterConfiguration
implements Serializable {
    private final StandardAdapterConfiguration parent;
    private final List<ColumnConfiguration> columnConfigList;
    private final List<PseudoColumnConfiguration> pseudoColumnConfigList;

    public ColumnConfigurationAdapterConfiguration(PropertyList columnConfigurationAdapterProps, StandardAdapterConfiguration parent) {
        if (columnConfigurationAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.columnConfigList = new ArrayList<ColumnConfiguration>();
        PropertyListCollection columns = columnConfigurationAdapterProps.getCollectionNotNull("columns");
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList columnProps = columns.getPropertyList(i);
            this.columnConfigList.add(new ColumnConfiguration(columnProps));
        }
        this.pseudoColumnConfigList = new ArrayList<PseudoColumnConfiguration>();
        PropertyListCollection pseudoColumnCollection = columnConfigurationAdapterProps.getCollectionNotNull("pseudocolumns");
        for (int i = 0; i < pseudoColumnCollection.size(); ++i) {
            PropertyList pseudoColumnProps = pseudoColumnCollection.getPropertyList(i);
            this.pseudoColumnConfigList.add(new PseudoColumnConfiguration(pseudoColumnProps, this));
        }
    }

    public StandardAdapterConfiguration getParent() {
        return this.parent;
    }

    public List<ColumnConfiguration> getColumnConfigurationList() {
        return this.columnConfigList;
    }

    public List<PseudoColumnConfiguration> getPseudoColumnConfigurationList() {
        return this.pseudoColumnConfigList;
    }
}

