/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupBuilderConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SeriesGroupConfiguration
implements ConfigurationListItem,
Serializable {
    private final String seriesGroupId;
    private final PlotConfiguration parent;
    private final List<SeriesGroupBuilderConfiguration> seriesGroupBuilderConfList;
    private int nextSeriesGroupBuilderConfIndex;

    public SeriesGroupConfiguration(PropertyList seriesGroupProps, PlotConfiguration parent) {
        if (seriesGroupProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.seriesGroupId = seriesGroupProps.getProperty("seriesgroupid");
        PropertyListCollection seriesGroupBuilderCollection = seriesGroupProps.getCollectionNotNull("seriesgroupbuildercollection");
        this.seriesGroupBuilderConfList = new ArrayList<SeriesGroupBuilderConfiguration>();
        for (int i = 0; i < seriesGroupBuilderCollection.size(); ++i) {
            this.seriesGroupBuilderConfList.add(new SeriesGroupBuilderConfiguration(seriesGroupBuilderCollection.getPropertyList(i), this));
        }
        if (this.seriesGroupBuilderConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.seriesGroupBuilderConfList) == 0) {
            this.seriesGroupBuilderConfList.add(new SeriesGroupBuilderConfiguration(new PropertyList(), this));
        }
        this.nextSeriesGroupBuilderConfIndex = 0;
        this.parent = parent;
    }

    public PlotConfiguration getParent() {
        return this.parent;
    }

    public SeriesGroupBuilderConfiguration getSeriesGroupBuilderConfiguration(String seriesGroupBuilderId) {
        SeriesGroupBuilderConfiguration returnConf = ConfigurationUtil.getConf(this.seriesGroupBuilderConfList, seriesGroupBuilderId);
        if (returnConf == null) {
            int index = ConfigurationUtil.getAnonymousConf(this.seriesGroupBuilderConfList, this.nextSeriesGroupBuilderConfIndex);
            ++this.nextSeriesGroupBuilderConfIndex;
            returnConf = this.seriesGroupBuilderConfList.get(index);
        }
        return returnConf;
    }

    @Override
    public String getId() {
        return this.getSeriesGroupId();
    }

    public String getSeriesGroupId() {
        return this.seriesGroupId;
    }
}

