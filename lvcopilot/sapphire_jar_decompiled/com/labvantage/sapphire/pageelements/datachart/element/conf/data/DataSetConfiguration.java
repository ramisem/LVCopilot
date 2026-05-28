/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.PlotConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class DataSetConfiguration
implements Serializable {
    private static final String DEFAULT_SERIES_GROUP_ID_COLUMN = "seriesgroupid";
    private static final String DEFAULT_SERIES_ID_COLUMN = "seriesid";
    private static final String DEFAULT_PLOT_ID_COLUMN = "plotid";
    private final String plotIdColumn;
    private final String seriesGroupIdColumn;
    private final String seriesIdColumn;
    private final List<PlotConfiguration> plotConfList;
    private final DataConfiguration parent;
    private int nextPlotConfIndex;

    public DataSetConfiguration(PropertyList dataSetProps, DataConfiguration parent) {
        if (dataSetProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        PropertyListCollection plotCollection = dataSetProps.getCollectionNotNull("plotcollection");
        this.plotConfList = new ArrayList<PlotConfiguration>();
        for (int i = 0; i < plotCollection.size(); ++i) {
            this.plotConfList.add(new PlotConfiguration(plotCollection.getPropertyList(i), this));
        }
        if (this.plotConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.plotConfList) == 0) {
            this.plotConfList.add(new PlotConfiguration(new PropertyList(), this));
        }
        this.plotIdColumn = dataSetProps.getProperty("plotidcolumn", DEFAULT_PLOT_ID_COLUMN);
        this.seriesGroupIdColumn = dataSetProps.getProperty("seriesgroupidcolumn", DEFAULT_SERIES_GROUP_ID_COLUMN);
        this.seriesIdColumn = dataSetProps.getProperty("seriesidcolumn", DEFAULT_SERIES_ID_COLUMN);
        this.nextPlotConfIndex = 0;
        this.parent = parent;
    }

    public DataConfiguration getParent() {
        return this.parent;
    }

    public String getSeriesGroupIdColumn() {
        return this.seriesGroupIdColumn;
    }

    public String getSeriesIdColumn() {
        return this.seriesIdColumn;
    }

    public PlotConfiguration getPlotConfiguration(String plotId) {
        PlotConfiguration returnConf = ConfigurationUtil.getConf(this.plotConfList, plotId);
        if (returnConf == null) {
            int index = ConfigurationUtil.getAnonymousConf(this.plotConfList, this.nextPlotConfIndex);
            ++this.nextPlotConfIndex;
            returnConf = this.plotConfList.get(index);
        }
        return returnConf;
    }

    public String getPlotIdColumn() {
        return this.plotIdColumn;
    }
}

