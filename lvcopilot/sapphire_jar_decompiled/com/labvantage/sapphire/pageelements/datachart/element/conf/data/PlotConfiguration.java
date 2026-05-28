/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PlotConfiguration
implements ConfigurationListItem,
Serializable {
    private final String plotId;
    private final List<SeriesGroupConfiguration> seriesGroupConfList;
    private final DataSetConfiguration parent;
    private int nextSeriesGroupConfIndex;

    public PlotConfiguration(PropertyList plotProps, DataSetConfiguration parent) {
        if (plotProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.plotId = plotProps.getProperty("plotid");
        PropertyListCollection seriesGroupCollection = plotProps.getCollectionNotNull("seriesgroupcollection");
        this.seriesGroupConfList = new ArrayList<SeriesGroupConfiguration>();
        for (int i = 0; i < seriesGroupCollection.size(); ++i) {
            this.seriesGroupConfList.add(new SeriesGroupConfiguration(seriesGroupCollection.getPropertyList(i), this));
        }
        if (this.seriesGroupConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.seriesGroupConfList) == 0) {
            this.seriesGroupConfList.add(new SeriesGroupConfiguration(new PropertyList(), this));
        }
        this.nextSeriesGroupConfIndex = 0;
        this.parent = parent;
    }

    @Override
    public String getId() {
        return this.getPlotId();
    }

    public SeriesGroupConfiguration getSeriesGroupConfiguration(String seriesGroupId) {
        SeriesGroupConfiguration returnConf = ConfigurationUtil.getConf(this.seriesGroupConfList, seriesGroupId);
        if (returnConf == null) {
            int index = ConfigurationUtil.getAnonymousConf(this.seriesGroupConfList, this.nextSeriesGroupConfIndex);
            ++this.nextSeriesGroupConfIndex;
            returnConf = this.seriesGroupConfList.get(index);
        }
        return returnConf;
    }

    public String getPlotId() {
        return this.plotId;
    }

    public DataSetConfiguration getParent() {
        return this.parent;
    }
}

