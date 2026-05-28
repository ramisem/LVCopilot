/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.general.Dataset
 *  org.jfree.data.xy.YIntervalSeries
 *  org.jfree.data.xy.YIntervalSeriesCollection
 */
package com.labvantage.sapphire.pageelements.datachart.data;

import com.labvantage.sapphire.pageelements.datachart.data.SeriesGroupBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.YIntervalSeriesCollectionConfiguration;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import sapphire.util.DataSet;

public class YIntervalSeriesCollectionBuilder
implements SeriesGroupBuilder {
    private final DataSet dataSet;
    private final YIntervalSeriesCollectionConfiguration yIntervalSeriesCollectionConf;
    private final String plotId;
    private final String seriesGroupId;
    private TraceableSeriesGroup traceableSeriesGroup;

    public YIntervalSeriesCollectionBuilder(DataSet dataSet, YIntervalSeriesCollectionConfiguration yIntervalSeriesCollectionConf) {
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        if (yIntervalSeriesCollectionConf == null) {
            throw new IllegalArgumentException("Configuration is null");
        }
        this.dataSet = dataSet;
        this.yIntervalSeriesCollectionConf = yIntervalSeriesCollectionConf;
        this.plotId = dataSet.getValue(0, yIntervalSeriesCollectionConf.getParent().getParent().getParent().getParent().getPlotIdColumn());
        this.seriesGroupId = dataSet.getValue(0, yIntervalSeriesCollectionConf.getParent().getParent().getParent().getParent().getSeriesGroupIdColumn());
    }

    @Override
    public TraceableSeriesGroup getTraceableSeriesGroup() {
        if (this.traceableSeriesGroup == null) {
            SeriesGroupConfiguration seriesGroupConf = this.yIntervalSeriesCollectionConf.getParent().getParent();
            DataSetConfiguration dataSetSeriesConf = seriesGroupConf.getParent().getParent();
            String seriesIdColumn = dataSetSeriesConf.getSeriesIdColumn();
            HashMap<String, String> filter = new HashMap<String, String>();
            Set<String> seriesIdSet = Util.getColumnValueList(this.dataSet, seriesIdColumn);
            ArrayList<YIntervalSeries> yIntervalSeriesList = new ArrayList<YIntervalSeries>();
            ArrayList<DataSet> seriesDataSetList = new ArrayList<DataSet>();
            ArrayList<String> seriesIdList = new ArrayList<String>();
            for (String seriesId : seriesIdSet) {
                filter.clear();
                filter.put(seriesIdColumn, seriesId);
                DataSet seriesDataSet = this.dataSet.getFilteredDataSet(filter);
                seriesDataSetList.add(seriesDataSet);
                seriesIdList.add(seriesId);
                yIntervalSeriesList.add(YIntervalSeriesCollectionBuilder.createYIntervalSeries(seriesDataSet, seriesId, this.yIntervalSeriesCollectionConf));
            }
            YIntervalSeriesCollection yIntervalSeriesCollection = new YIntervalSeriesCollection();
            for (YIntervalSeries intervalSeries : yIntervalSeriesList) {
                yIntervalSeriesCollection.addSeries(intervalSeries);
            }
            this.traceableSeriesGroup = new TraceableSeriesGroup((Dataset)yIntervalSeriesCollection, seriesDataSetList, seriesIdList, this.seriesGroupId, this.plotId, this.yIntervalSeriesCollectionConf);
        }
        return this.traceableSeriesGroup;
    }

    private static YIntervalSeries createYIntervalSeries(DataSet seriesDataSet, String seriesId, YIntervalSeriesCollectionConfiguration conf) {
        String xColumn = conf.getXColumn();
        String yColumn = conf.getYColumn();
        String yLowColumn = conf.getYLowColumn();
        String yHighColumn = conf.getYHighColumn();
        int xColumnType = seriesDataSet.getColumnType(xColumn);
        int yColumnType = seriesDataSet.getColumnType(yColumn);
        int yLowColumnType = seriesDataSet.getColumnType(yLowColumn);
        int yHighColumnType = seriesDataSet.getColumnType(yHighColumn);
        if (xColumnType == -1) {
            throw new IllegalArgumentException("X column (" + xColumn + ") not found in data set: " + seriesDataSet.toXML());
        }
        if (yColumnType == -1) {
            throw new IllegalArgumentException("Y column (" + yColumn + ") not found in data set: " + seriesDataSet.toXML());
        }
        if (yHighColumnType == -1) {
            throw new IllegalArgumentException("Y High column (" + yColumn + ") not found in data set: " + seriesDataSet.toXML());
        }
        if (yLowColumnType == -1) {
            throw new IllegalArgumentException("Y Low column (" + yColumn + ") not found in data set: " + seriesDataSet.toXML());
        }
        YIntervalSeries yIntervalSeries = new YIntervalSeries((Comparable)((Object)seriesId));
        for (int i = 0; i < seriesDataSet.getRowCount(); ++i) {
            if (seriesDataSet.isNull(i, xColumn) || seriesDataSet.isNull(i, yColumn)) continue;
            double x = YIntervalSeriesCollectionBuilder.getColumnValue(seriesDataSet, xColumn, xColumnType, i, "X");
            double y = YIntervalSeriesCollectionBuilder.getColumnValue(seriesDataSet, yColumn, yColumnType, i, "Y");
            double yLow = YIntervalSeriesCollectionBuilder.getColumnValue(seriesDataSet, yLowColumn, yLowColumnType, i, "Y Low");
            double yHigh = YIntervalSeriesCollectionBuilder.getColumnValue(seriesDataSet, yHighColumn, yHighColumnType, i, "Y High");
            yIntervalSeries.add(x, y, yLow, yHigh);
        }
        return yIntervalSeries;
    }

    private static double getColumnValue(DataSet seriesDataSet, String column, int columnType, int i, String id) {
        double value;
        if (columnType == 1) {
            BigDecimal bigDecimalValue = seriesDataSet.getBigDecimal(i, column);
            value = bigDecimalValue.doubleValue();
        } else if (columnType == 2) {
            Calendar calendarValue = seriesDataSet.getCalendar(i, column);
            value = calendarValue.getTimeInMillis();
        } else if (columnType == 0) {
            String stringValue = seriesDataSet.getString(i, column);
            value = Double.valueOf(stringValue);
        } else {
            throw new IllegalArgumentException("Unsupported " + id + " column type " + columnType + " ");
        }
        return value;
    }
}

