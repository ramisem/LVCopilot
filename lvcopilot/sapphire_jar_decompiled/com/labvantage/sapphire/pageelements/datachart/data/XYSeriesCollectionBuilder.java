/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.general.Dataset
 *  org.jfree.data.xy.XYSeries
 *  org.jfree.data.xy.XYSeriesCollection
 */
package com.labvantage.sapphire.pageelements.datachart.data;

import com.labvantage.sapphire.pageelements.datachart.data.SeriesGroupBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.XYSeriesCollectionConfiguration;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import sapphire.util.DataSet;

public class XYSeriesCollectionBuilder
implements SeriesGroupBuilder {
    private final DataSet dataSet;
    private final XYSeriesCollectionConfiguration xySeriesCollectionConf;
    private final String plotId;
    private final String seriesGroupId;
    private TraceableSeriesGroup traceableSeriesGroup;

    public XYSeriesCollectionBuilder(DataSet dataSet, XYSeriesCollectionConfiguration xySeriesCollectionConf) {
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        if (xySeriesCollectionConf == null) {
            throw new IllegalArgumentException("Configuration is null");
        }
        this.dataSet = dataSet;
        this.xySeriesCollectionConf = xySeriesCollectionConf;
        this.plotId = dataSet.getValue(0, xySeriesCollectionConf.getParent().getParent().getParent().getParent().getPlotIdColumn());
        this.seriesGroupId = dataSet.getValue(0, xySeriesCollectionConf.getParent().getParent().getParent().getParent().getSeriesGroupIdColumn());
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private XYSeries createXYSeries(DataSet seriesDataSet, String seriesId, XYSeriesCollectionConfiguration conf) {
        String xColumn = conf.getXColumn();
        String yColumn = conf.getYColumn();
        int xColumnType = seriesDataSet.getColumnType(xColumn);
        int yColumnType = seriesDataSet.getColumnType(yColumn);
        if (xColumnType == -1) {
            throw new IllegalArgumentException("X column " + xColumn + " not found in data set");
        }
        if (yColumnType == -1) {
            throw new IllegalArgumentException("Y column " + yColumn + " not found in data set");
        }
        XYSeries xySeries = new XYSeries((Comparable)((Object)seriesId));
        for (int i = 0; i < seriesDataSet.getRowCount(); ++i) {
            Double y;
            String stringValue;
            Calendar calendarValue;
            double x;
            BigDecimal bigDecimalValue;
            if (seriesDataSet.isNull(i, xColumn)) continue;
            if (xColumnType == 1) {
                bigDecimalValue = seriesDataSet.getBigDecimal(i, xColumn);
                x = bigDecimalValue.doubleValue();
            } else if (xColumnType == 2) {
                calendarValue = seriesDataSet.getCalendar(i, xColumn);
                x = calendarValue.getTimeInMillis();
            } else {
                if (xColumnType != 0) throw new IllegalArgumentException("Unsupported X column type " + xColumnType + " ");
                stringValue = seriesDataSet.getString(i, xColumn);
                x = Double.valueOf(stringValue);
            }
            if (!seriesDataSet.isNull(i, yColumn)) {
                if (yColumnType == 1) {
                    bigDecimalValue = seriesDataSet.getBigDecimal(i, yColumn);
                    y = bigDecimalValue.doubleValue();
                } else if (yColumnType == 2) {
                    calendarValue = seriesDataSet.getCalendar(i, yColumn);
                    y = calendarValue.getTimeInMillis();
                } else {
                    if (yColumnType != 0) throw new IllegalArgumentException("Unsupported Y column type " + yColumnType + " ");
                    stringValue = seriesDataSet.getString(i, yColumn);
                    y = Double.valueOf(stringValue);
                }
            } else {
                y = null;
            }
            xySeries.add(x, y);
        }
        return xySeries;
    }

    @Override
    public TraceableSeriesGroup getTraceableSeriesGroup() {
        if (this.traceableSeriesGroup == null) {
            SeriesGroupConfiguration seriesGroupConf = this.xySeriesCollectionConf.getParent().getParent();
            DataSetConfiguration dataSetSeriesConf = seriesGroupConf.getParent().getParent();
            String seriesIdColumn = dataSetSeriesConf.getSeriesIdColumn();
            HashMap<String, String> filter = new HashMap<String, String>();
            Set<String> seriesIdSet = Util.getColumnValueList(this.dataSet, seriesIdColumn);
            ArrayList<XYSeries> xySeriesList = new ArrayList<XYSeries>();
            ArrayList<DataSet> seriesDataSetList = new ArrayList<DataSet>();
            ArrayList<String> seriesIdList = new ArrayList<String>();
            for (String seriesId : seriesIdSet) {
                filter.clear();
                filter.put(seriesIdColumn, seriesId);
                DataSet seriesDataSet = this.dataSet.getFilteredDataSet(filter);
                seriesDataSetList.add(seriesDataSet);
                seriesIdList.add(seriesId);
                xySeriesList.add(this.createXYSeries(seriesDataSet, seriesId, this.xySeriesCollectionConf));
            }
            XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
            for (XYSeries xySeries : xySeriesList) {
                xySeriesCollection.addSeries(xySeries);
            }
            this.traceableSeriesGroup = new TraceableSeriesGroup((Dataset)xySeriesCollection, seriesDataSetList, seriesIdList, this.seriesGroupId, this.plotId, this.xySeriesCollectionConf);
        }
        return this.traceableSeriesGroup;
    }
}

