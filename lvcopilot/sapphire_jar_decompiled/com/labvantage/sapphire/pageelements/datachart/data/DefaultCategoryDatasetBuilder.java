/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.category.DefaultCategoryDataset
 *  org.jfree.data.general.Dataset
 */
package com.labvantage.sapphire.pageelements.datachart.data;

import com.labvantage.sapphire.pageelements.datachart.data.SeriesGroupBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DefaultCategoryDatasetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import sapphire.util.DataSet;

public class DefaultCategoryDatasetBuilder
implements SeriesGroupBuilder {
    private final DataSet dataSet;
    private final DataSet allDataSet;
    private final DefaultCategoryDatasetConfiguration defaultCategoryDatasetConf;
    private final String plotId;
    private final String seriesGroupId;
    private TraceableSeriesGroup traceableSeriesGroup;

    public DefaultCategoryDatasetBuilder(DataSet dataSet, DataSet allDataSet, DefaultCategoryDatasetConfiguration defaultCategoryDatasetConf) {
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        if (defaultCategoryDatasetConf == null) {
            throw new IllegalArgumentException("Configuration is null");
        }
        this.dataSet = dataSet;
        this.allDataSet = allDataSet;
        this.defaultCategoryDatasetConf = defaultCategoryDatasetConf;
        this.plotId = dataSet.getValue(0, defaultCategoryDatasetConf.getParent().getParent().getParent().getParent().getPlotIdColumn());
        this.seriesGroupId = dataSet.getValue(0, defaultCategoryDatasetConf.getParent().getParent().getParent().getParent().getSeriesGroupIdColumn());
    }

    @Override
    public TraceableSeriesGroup getTraceableSeriesGroup() {
        if (this.traceableSeriesGroup == null) {
            SeriesGroupConfiguration seriesGroupConf = this.defaultCategoryDatasetConf.getParent().getParent();
            DataSetConfiguration dataSetSeriesConf = seriesGroupConf.getParent().getParent();
            String seriesIdColumn = dataSetSeriesConf.getSeriesIdColumn();
            HashMap<String, String> filter = new HashMap<String, String>();
            ArrayList<String> seriesIdSet = Util.getOrderedColumnValueList(this.dataSet, seriesIdColumn);
            ArrayList<String> categoryIdSet = Util.getOrderedColumnValueList(this.allDataSet, this.defaultCategoryDatasetConf.getXColumn());
            DefaultCategoryDataset defaultCategoryDataset = new DefaultCategoryDataset();
            ArrayList<DataSet> seriesDataSetList = new ArrayList<DataSet>();
            ArrayList<String> seriesIdList = new ArrayList<String>();
            for (String seriesId : seriesIdSet) {
                filter.clear();
                filter.put(seriesIdColumn, seriesId);
                DataSet seriesDataSet = this.dataSet.getFilteredDataSet(filter);
                seriesDataSetList.add(seriesDataSet);
                seriesIdList.add(seriesId);
                this.addValues(defaultCategoryDataset, seriesDataSet, seriesId, categoryIdSet, this.defaultCategoryDatasetConf);
            }
            this.traceableSeriesGroup = new TraceableSeriesGroup((Dataset)defaultCategoryDataset, seriesDataSetList, seriesIdList, this.seriesGroupId, this.plotId, this.defaultCategoryDatasetConf);
        }
        return this.traceableSeriesGroup;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void addValues(DefaultCategoryDataset defaultCategoryDataset, DataSet seriesDataSet, String seriesId, ArrayList<String> categoryIdSet, DefaultCategoryDatasetConfiguration conf) {
        String xColumn = conf.getXColumn();
        String yColumn = conf.getYColumn();
        int xColumnType = seriesDataSet.getColumnType(xColumn);
        int yColumnType = seriesDataSet.getColumnType(yColumn);
        if (xColumnType == -1) {
            throw new IllegalArgumentException("X column (" + xColumn + ") not found in data set: " + seriesDataSet.toXML());
        }
        if (yColumnType == -1) {
            throw new IllegalArgumentException("Y column (" + yColumn + ") not found in data set: " + seriesDataSet.toXML());
        }
        for (String category : categoryIdSet) {
            defaultCategoryDataset.addValue(null, (Comparable)((Object)seriesId), (Comparable)((Object)category));
        }
        for (int i = 0; i < seriesDataSet.getRowCount(); ++i) {
            Double y;
            if (seriesDataSet.isNull(i, xColumn)) {
                seriesDataSet.setValue(i, xColumn, "(null)");
            }
            String x = seriesDataSet.getValue(i, xColumn);
            if (seriesDataSet.isNull(i, yColumn)) {
                Object var14_17 = null;
            }
            if (!seriesDataSet.isNull(i, yColumn)) {
                if (yColumnType == 1) {
                    BigDecimal bigDecimalValue = seriesDataSet.getBigDecimal(i, yColumn);
                    y = bigDecimalValue.doubleValue();
                } else if (yColumnType == 2) {
                    Calendar calendarValue = seriesDataSet.getCalendar(i, yColumn);
                    y = calendarValue.getTimeInMillis();
                } else {
                    if (yColumnType != 0) throw new IllegalArgumentException("Unsupported Y column type " + yColumnType + " ");
                    String stringValue = seriesDataSet.getString(i, yColumn);
                    y = Double.valueOf(stringValue);
                }
            } else {
                y = null;
            }
            defaultCategoryDataset.addValue(y, (Comparable)((Object)seriesId), (Comparable)((Object)x));
        }
    }
}

