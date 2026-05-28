/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.groovy.AbstractBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.GroovyHelper;
import java.io.Serializable;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ChartBindingMap
extends AbstractBindingMap
implements Serializable {
    public static final String DATASET = "dataset";
    public static final String TRACEABLE_SERIES_GROUP = "seriesgroup";
    private static final String PLOT_ID = "plotid";
    public static final String SERIES_GROUP_ID = "seriesgroupid";
    public static final String SERIES_ID = "seriesid";
    public static final String ITEM_INDEX = "itemindex";
    public static final String GROOVY_HELPER = "groovyhelper";
    public static final String DATA = "data";
    private final Data data;
    private final String chartId;

    public ChartBindingMap(String chartId, Data data, PropertyList argumentValueList, String connectionId) {
        super(argumentValueList, connectionId);
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        this.data = data;
        this.chartId = chartId;
        this.putToBindingMap(DATASET, data.getDataSet());
        this.putToBindingMap("datasetprovideroutputprops", data.getDataSetProviderOutputProps());
        this.putToBindingMap("adapteroutputprops", data.getAdapterOutputProperties());
        this.putToBindingMap(GROOVY_HELPER, new GroovyHelper(data, connectionId));
        this.putToBindingMap(DATA, data);
        this.resetChartBindingMap();
    }

    public void resetChartBindingMap() {
        String plotIdColumn = this.data.getDataConfiguration().getDataSetConfiguration().getPlotIdColumn();
        String seriesGroupIdColumn = this.data.getDataConfiguration().getDataSetConfiguration().getSeriesGroupIdColumn();
        String seriesIdColumn = this.data.getDataConfiguration().getDataSetConfiguration().getSeriesIdColumn();
        if (this.data.getDataSet().size() > 0) {
            this.setActiveItem(this.data.getDataSet().getValue(0, plotIdColumn), this.data.getDataSet().getValue(0, seriesGroupIdColumn), this.data.getDataSet().getValue(0, seriesIdColumn), 0);
        }
    }

    public void setActiveItem(String plotId, String seriesGroupId, String seriesId, Integer itemIndex) {
        if (plotId == null || plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is null or empty: " + plotId);
        }
        if (seriesGroupId == null || seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is null or empty: " + seriesGroupId);
        }
        if (seriesId == null || seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is null or empty: " + seriesId);
        }
        if (itemIndex == null || itemIndex.compareTo(0) < 0) {
            throw new IllegalArgumentException("Item index is null or negative: " + itemIndex);
        }
        this.putToBindingMap(PLOT_ID, plotId);
        this.putToBindingMap(SERIES_GROUP_ID, seriesGroupId);
        this.putToBindingMap(SERIES_ID, seriesId);
        this.putToBindingMap(ITEM_INDEX, itemIndex);
        boolean found = false;
        for (TraceableSeriesGroup traceableSeriesGroup : this.data.getTraceableSeriesGroupList(plotId)) {
            if (!traceableSeriesGroup.getSeriesGroupId().equals(seriesGroupId)) continue;
            this.putToBindingMap(TRACEABLE_SERIES_GROUP, traceableSeriesGroup);
            found = true;
            break;
        }
        if (!found) {
            throw new IllegalArgumentException("Unknown series group ID: " + seriesGroupId);
        }
    }

    public void setActiveSeries(String plotId, String seriesGroupId, String seriesId) {
        if (plotId == null || plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is null or empty: " + plotId);
        }
        if (seriesGroupId == null || seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is null or empty: " + seriesGroupId);
        }
        if (seriesId == null || seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is null or empty: " + seriesId);
        }
        this.setActiveItem(plotId, seriesGroupId, seriesId, 0);
    }

    public void setActiveSeriesGroup(String plotId, String seriesGroupId) {
        if (plotId == null || plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is null or empty: " + plotId);
        }
        if (seriesGroupId == null || seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is null or empty: " + seriesGroupId);
        }
        TraceableSeriesGroup traceableSeriesGroup = null;
        for (TraceableSeriesGroup seriesGroup : this.data.getTraceableSeriesGroupList(plotId)) {
            if (!seriesGroup.getSeriesGroupId().equals(seriesGroupId)) continue;
            traceableSeriesGroup = seriesGroup;
        }
        if (traceableSeriesGroup == null) {
            throw new IllegalArgumentException("Series group ID not found: " + seriesGroupId);
        }
        this.setActiveItem(plotId, seriesGroupId, traceableSeriesGroup.getSeriesId(0), 0);
    }

    @Override
    public PropertyListCollection getTokenValues() {
        PropertyListCollection tokenValuesCollection = super.getTokenValues();
        PropertyList activeSeriesItemColumns = new PropertyList();
        String plotId = (String)this.getFromBindingMap(PLOT_ID);
        String seriesGroupId = (String)this.getFromBindingMap(SERIES_GROUP_ID);
        String seriesId = (String)this.getFromBindingMap(SERIES_ID);
        Integer itemIndex = (Integer)this.getFromBindingMap(ITEM_INDEX);
        DataSet dataSet = (DataSet)this.getFromBindingMap(DATASET);
        TraceableSeriesGroup traceableSeriesGroup = (TraceableSeriesGroup)this.getFromBindingMap(TRACEABLE_SERIES_GROUP);
        if (seriesGroupId != null || seriesId != null) {
            for (int i = 0; i < dataSet.getColumnCount(); ++i) {
                String columnId = dataSet.getColumnId(i);
                activeSeriesItemColumns.setProperty(columnId, traceableSeriesGroup.getSeriesDataSet(seriesId).getValue(itemIndex, columnId));
            }
            tokenValuesCollection.add(activeSeriesItemColumns);
            PropertyList keyValues = new PropertyList();
            keyValues.setProperty(PLOT_ID, plotId);
            keyValues.setProperty(SERIES_GROUP_ID, seriesGroupId);
            keyValues.setProperty(SERIES_ID, seriesId);
            keyValues.setProperty(ITEM_INDEX, itemIndex.toString());
            tokenValuesCollection.add(keyValues);
        }
        return tokenValuesCollection;
    }

    public String getDataChartJsObject() {
        return this.chartId + "_JS";
    }
}

