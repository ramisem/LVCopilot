/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.general.Dataset
 */
package com.labvantage.sapphire.pageelements.datachart.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupColumnConfiguration;
import java.io.Serializable;
import java.util.List;
import org.jfree.data.general.Dataset;
import sapphire.util.DataSet;

public class TraceableSeriesGroup
implements Serializable {
    private final Dataset seriesGroup;
    private final String seriesGroupId;
    private final List<DataSet> seriesDataSetList;
    private final List<String> seriesIdList;
    private final SeriesGroupColumnConfiguration seriesGroupColumnConf;
    private final String plotId;

    public TraceableSeriesGroup(Dataset seriesGroup, List<DataSet> seriesDataSetList, List<String> seriesIdList, String seriesGroupId, String plotId, SeriesGroupColumnConfiguration seriesGroupColumnConf) {
        if (seriesGroup == null) {
            throw new IllegalArgumentException("Series group is null");
        }
        if (seriesDataSetList == null || seriesDataSetList.isEmpty()) {
            throw new IllegalArgumentException("Series data set list is null or empty: " + seriesDataSetList);
        }
        if (seriesIdList == null || seriesIdList.isEmpty() || seriesIdList.size() != seriesDataSetList.size()) {
            throw new IllegalArgumentException("Series ID list is null, empty or inconsistent with series data set list: " + seriesIdList);
        }
        if (seriesGroupId == null || seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is null or empty: " + seriesGroupId);
        }
        if (plotId == null || plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is null or empty: " + plotId);
        }
        if (seriesGroupColumnConf == null) {
            throw new IllegalArgumentException("Series group column configuration is null");
        }
        this.seriesGroup = seriesGroup;
        this.seriesGroupId = seriesGroupId;
        this.seriesGroupColumnConf = seriesGroupColumnConf;
        this.seriesDataSetList = seriesDataSetList;
        this.seriesIdList = seriesIdList;
        this.plotId = plotId;
    }

    public Dataset getSeriesGroup() {
        return this.seriesGroup;
    }

    public String getSeriesGroupId() {
        return this.seriesGroupId;
    }

    public int getSeriesCount() {
        return this.seriesIdList.size();
    }

    public String getSeriesId(int index) {
        return this.seriesIdList.get(index);
    }

    public DataSet getSeriesDataSet(String seriesId) {
        if (seriesId == null || seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is null or empty: " + seriesId);
        }
        return this.seriesDataSetList.get(this.getSeriesIndex(seriesId));
    }

    public boolean hasSeriesDataSet(String seriesId) {
        if (seriesId == null || seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is null or empty: " + seriesId);
        }
        int index = this.seriesIdList.indexOf(seriesId);
        return index != -1;
    }

    public int getSeriesIndex(String seriesId) {
        if (seriesId == null || seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is null or empty:" + seriesId);
        }
        int index = this.seriesIdList.indexOf(seriesId);
        if (index == -1) {
            throw new IllegalArgumentException("Series ID not found: " + seriesId);
        }
        return index;
    }

    public SeriesGroupColumnConfiguration getSeriesGroupColumnConfiguration() {
        return this.seriesGroupColumnConf;
    }

    public String getPlotId() {
        return this.plotId;
    }
}

