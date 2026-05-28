/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.general.Dataset
 *  org.jfree.data.time.Day
 *  org.jfree.data.time.Hour
 *  org.jfree.data.time.Millisecond
 *  org.jfree.data.time.Minute
 *  org.jfree.data.time.Month
 *  org.jfree.data.time.Quarter
 *  org.jfree.data.time.Second
 *  org.jfree.data.time.TimePeriod
 *  org.jfree.data.time.TimePeriodValues
 *  org.jfree.data.time.TimePeriodValuesCollection
 *  org.jfree.data.time.Week
 *  org.jfree.data.time.Year
 */
package com.labvantage.sapphire.pageelements.datachart.data;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.data.SeriesGroupBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.TimePeriodValuesCollectionConfiguration;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import org.jfree.data.general.Dataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;

public class TimePeriodValuesCollectionBuilder
implements SeriesGroupBuilder {
    private final DataSet dataSet;
    private final TimePeriodValuesCollectionConfiguration timePeriodValuesCollectionConf;
    private final String plotId;
    private final String seriesGroupId;
    private final Locale connectionLocale;
    private final TimeZone connectionTimeZone;
    private final Locale systemLocale;
    private final TimePeriodValuesCollectionConfiguration.TimePeriodType timePeriodType;
    private TraceableSeriesGroup traceableSeriesGroup;

    public TimePeriodValuesCollectionBuilder(DataSet dataSet, TimePeriodValuesCollectionConfiguration timePeriodValuesCollectionConf, ConnectionInfo connectionInfo) {
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        if (timePeriodValuesCollectionConf == null) {
            throw new IllegalArgumentException("Configuration is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        this.dataSet = dataSet;
        this.timePeriodValuesCollectionConf = timePeriodValuesCollectionConf;
        this.plotId = dataSet.getValue(0, timePeriodValuesCollectionConf.getParent().getParent().getParent().getParent().getPlotIdColumn());
        this.seriesGroupId = dataSet.getValue(0, timePeriodValuesCollectionConf.getParent().getParent().getParent().getParent().getSeriesGroupIdColumn());
        this.connectionLocale = I18nUtil.getConnectionLocale(connectionInfo);
        this.connectionTimeZone = I18nUtil.getConnectionTimeZone(connectionInfo);
        this.timePeriodType = timePeriodValuesCollectionConf.getTimePeriodType();
        this.systemLocale = I18nUtil.getSysLocale();
    }

    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private TimePeriodValues createTimePeriodValues(DataSet seriesDataSet, String seriesId, TimePeriodValuesCollectionConfiguration conf) {
        String xColumn = conf.getXColumn();
        String yColumn = conf.getYColumn();
        int xColumnType = seriesDataSet.getColumnType(xColumn);
        int yColumnType = seriesDataSet.getColumnType(yColumn);
        if (xColumnType == -1) {
            throw new IllegalArgumentException("X column " + xColumn + " not found in data set");
        }
        if (yColumnType == -1) {
            throw new IllegalArgumentException("Y column " + yColumn + " not found in data set: ");
        }
        TimePeriodValues timePeriodValues = new TimePeriodValues(seriesId);
        for (int i = 0; i < seriesDataSet.getRowCount(); ++i) {
            void var13_14;
            String stringValue;
            Calendar calendarValue;
            TimePeriod x;
            BigDecimal bigDecimalValue;
            if (seriesDataSet.isNull(i, xColumn)) continue;
            if (xColumnType == 1) {
                bigDecimalValue = seriesDataSet.getBigDecimal(i, xColumn);
                Date d = new Date(bigDecimalValue.longValue());
                x = this.createTimePeriod(d);
            } else if (xColumnType == 2) {
                calendarValue = seriesDataSet.getCalendar(i, xColumn);
                x = this.createTimePeriod(calendarValue.getTime());
            } else {
                if (xColumnType != 0) throw new IllegalArgumentException("Unsupported X column type " + xColumnType + " ");
                stringValue = seriesDataSet.getString(i, xColumn);
                DateFormat df = DateFormat.getDateTimeInstance(3, 3, this.systemLocale);
                try {
                    x = this.createTimePeriod(df.parse(stringValue));
                }
                catch (ParseException e) {
                    throw new IllegalArgumentException("X column format not supported");
                }
            }
            if (!seriesDataSet.isNull(i, yColumn)) {
                if (yColumnType == 1) {
                    bigDecimalValue = seriesDataSet.getBigDecimal(i, yColumn);
                    Double d = bigDecimalValue.doubleValue();
                } else if (yColumnType == 2) {
                    calendarValue = seriesDataSet.getCalendar(i, yColumn);
                    Long l = calendarValue.getTimeInMillis();
                } else {
                    if (yColumnType != 0) throw new IllegalArgumentException("Unsupported Y column type " + yColumnType + " ");
                    stringValue = seriesDataSet.getString(i, yColumn);
                    Double d = Double.valueOf(stringValue);
                }
            } else {
                Object var13_18 = null;
            }
            timePeriodValues.add(x, (Number)var13_14);
        }
        return timePeriodValues;
    }

    private TimePeriod createTimePeriod(Date d) {
        if (this.timePeriodType == TimePeriodValuesCollectionConfiguration.TimePeriodType.MILLISECOND) {
            return new Millisecond(d, this.connectionTimeZone, this.connectionLocale);
        }
        if (this.timePeriodType == TimePeriodValuesCollectionConfiguration.TimePeriodType.SECOND) {
            return new Second(d, this.connectionTimeZone, this.connectionLocale);
        }
        if (this.timePeriodType == TimePeriodValuesCollectionConfiguration.TimePeriodType.MINUTE) {
            return new Minute(d, this.connectionTimeZone, this.connectionLocale);
        }
        if (this.timePeriodType == TimePeriodValuesCollectionConfiguration.TimePeriodType.HOUR) {
            return new Hour(d, this.connectionTimeZone, this.connectionLocale);
        }
        if (this.timePeriodType == TimePeriodValuesCollectionConfiguration.TimePeriodType.DAY) {
            return new Day(d, this.connectionTimeZone, this.connectionLocale);
        }
        if (this.timePeriodType == TimePeriodValuesCollectionConfiguration.TimePeriodType.WEEK) {
            return new Week(d, this.connectionTimeZone, this.connectionLocale);
        }
        if (this.timePeriodType == TimePeriodValuesCollectionConfiguration.TimePeriodType.MONTH) {
            return new Month(d, this.connectionTimeZone, this.connectionLocale);
        }
        if (this.timePeriodType == TimePeriodValuesCollectionConfiguration.TimePeriodType.QUARTER) {
            return new Quarter(d, this.connectionTimeZone, this.connectionLocale);
        }
        if (this.timePeriodType == TimePeriodValuesCollectionConfiguration.TimePeriodType.YEAR) {
            return new Year(d, this.connectionTimeZone, this.connectionLocale);
        }
        throw new IllegalArgumentException("Unknown time period type: " + (Object)((Object)this.timePeriodType));
    }

    @Override
    public TraceableSeriesGroup getTraceableSeriesGroup() {
        if (this.traceableSeriesGroup == null) {
            SeriesGroupConfiguration seriesGroupConf = this.timePeriodValuesCollectionConf.getParent().getParent();
            DataSetConfiguration dataSetSeriesConf = seriesGroupConf.getParent().getParent();
            String seriesIdColumn = dataSetSeriesConf.getSeriesIdColumn();
            HashMap<String, String> filter = new HashMap<String, String>();
            Set<String> seriesIdSet = Util.getColumnValueList(this.dataSet, seriesIdColumn);
            if (seriesIdSet.isEmpty()) {
                throw new IllegalArgumentException("Data set cannot be split to series using series ID column: " + seriesIdColumn);
            }
            ArrayList<TimePeriodValues> timePeriodValuesList = new ArrayList<TimePeriodValues>();
            ArrayList<DataSet> seriesDataSetList = new ArrayList<DataSet>();
            ArrayList<String> seriesIdList = new ArrayList<String>();
            for (String seriesId : seriesIdSet) {
                filter.clear();
                filter.put(seriesIdColumn, seriesId);
                DataSet seriesDataSet = this.dataSet.getFilteredDataSet(filter);
                seriesDataSetList.add(seriesDataSet);
                seriesIdList.add(seriesId);
                timePeriodValuesList.add(this.createTimePeriodValues(seriesDataSet, seriesId, this.timePeriodValuesCollectionConf));
            }
            TimePeriodValuesCollection timePeriodValuesCollection = new TimePeriodValuesCollection();
            for (TimePeriodValues timePeriodValues : timePeriodValuesList) {
                timePeriodValuesCollection.addSeries(timePeriodValues);
            }
            this.traceableSeriesGroup = new TraceableSeriesGroup((Dataset)timePeriodValuesCollection, seriesDataSetList, seriesIdList, this.seriesGroupId, this.plotId, this.timePeriodValuesCollectionConf);
        }
        return this.traceableSeriesGroup;
    }
}

