/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.general.Dataset
 *  org.jfree.data.general.DefaultPieDataset
 */
package com.labvantage.sapphire.pageelements.datachart.data;

import com.labvantage.sapphire.pageelements.datachart.data.SeriesGroupBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DefaultPieDatasetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import sapphire.util.DataSet;

public class DefaultPieDatasetBuilder
implements SeriesGroupBuilder {
    private final DataSet dataSet;
    private final DefaultPieDatasetConfiguration defaultPieDatasetConf;
    private final String plotId;
    private final String seriesGroupId;
    private TraceableSeriesGroup traceableSeriesGroup;

    public DefaultPieDatasetBuilder(DataSet dataSet, DefaultPieDatasetConfiguration defaultPieDatasetConf) {
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        if (defaultPieDatasetConf == null) {
            throw new IllegalArgumentException("Configuration is null");
        }
        this.dataSet = dataSet;
        this.defaultPieDatasetConf = defaultPieDatasetConf;
        this.plotId = dataSet.getValue(0, defaultPieDatasetConf.getParent().getParent().getParent().getParent().getPlotIdColumn());
        this.seriesGroupId = dataSet.getValue(0, defaultPieDatasetConf.getParent().getParent().getParent().getParent().getSeriesGroupIdColumn());
    }

    @Override
    public TraceableSeriesGroup getTraceableSeriesGroup() {
        if (this.traceableSeriesGroup == null) {
            SeriesGroupConfiguration seriesGroupConf = this.defaultPieDatasetConf.getParent().getParent();
            DataSetConfiguration dataSetSeriesConf = seriesGroupConf.getParent().getParent();
            String seriesIdColumn = dataSetSeriesConf.getSeriesIdColumn();
            Set<String> seriesIdSet = Util.getColumnValueList(this.dataSet, seriesIdColumn);
            if (seriesIdSet.size() > 1) {
                throw new IllegalArgumentException("Pie series group can contain only single series, series count: " + seriesIdSet.size());
            }
            DefaultPieDataset defaultPieDataset = new DefaultPieDataset();
            String keyColumn = this.defaultPieDatasetConf.getKeyColumn();
            String valueColumn = this.defaultPieDatasetConf.getValueColumn();
            int keyColumnType = this.dataSet.getColumnType(keyColumn);
            int valueColumnType = this.dataSet.getColumnType(valueColumn);
            if (keyColumnType == -1) {
                throw new IllegalArgumentException("Key column (" + keyColumn + ") not found in data set: " + this.dataSet.toXML());
            }
            if (valueColumnType == -1) {
                throw new IllegalArgumentException("Value column (" + valueColumn + ") not found in data set: " + this.dataSet.toXML());
            }
            for (int i = 0; i < this.dataSet.getRowCount(); ++i) {
                double value;
                if (this.dataSet.isNull(i, valueColumn)) continue;
                if (this.dataSet.isNull(i, keyColumn)) {
                    this.dataSet.setValue(i, keyColumn, "(null)");
                }
                String key = this.dataSet.getValue(i, keyColumn);
                if (valueColumnType == 1) {
                    BigDecimal bigDecimalValue = this.dataSet.getBigDecimal(i, valueColumn);
                    value = bigDecimalValue.doubleValue();
                } else if (valueColumnType == 2) {
                    Calendar calendarValue = this.dataSet.getCalendar(i, valueColumn);
                    value = calendarValue.getTimeInMillis();
                } else if (valueColumnType == 0) {
                    String stringValue = this.dataSet.getString(i, valueColumn);
                    value = Double.valueOf(stringValue);
                } else {
                    throw new IllegalArgumentException("Unsupported value column type " + valueColumnType + " ");
                }
                defaultPieDataset.setValue((Comparable)((Object)key), value);
            }
            ArrayList<DataSet> seriesDataSetList = new ArrayList<DataSet>();
            ArrayList<String> seriesIdList = new ArrayList<String>();
            seriesDataSetList.add(this.dataSet);
            seriesIdList.add(this.seriesGroupId);
            this.traceableSeriesGroup = new TraceableSeriesGroup((Dataset)defaultPieDataset, seriesDataSetList, seriesIdList, this.seriesGroupId, this.plotId, this.defaultPieDatasetConf);
        }
        return this.traceableSeriesGroup;
    }
}

