/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.function.Function2D
 *  org.jfree.data.function.LineFunction2D
 *  org.jfree.data.function.PowerFunction2D
 *  org.jfree.data.general.Dataset
 *  org.jfree.data.general.DatasetUtilities
 *  org.jfree.data.statistics.Regression
 *  org.jfree.data.statistics.Statistics
 *  org.jfree.data.xy.XYDataset
 *  org.jfree.data.xy.XYSeries
 *  org.jfree.data.xy.XYSeriesCollection
 */
package com.labvantage.sapphire.pageelements.datachart.chart.plot.trendline;

import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CustomTrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.LineTrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.XYPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.CustomTrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.StandardTrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.TrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.function.PolynomialFunction2D;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.function.PowerFunction2D;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import sapphire.util.DataSet;

public final class XYTrendLineBuilder {
    private final Data data;
    private final ChartBindingMap chartBindingMap;
    private final XYPlotConfiguration xyPlotConf;
    private final List<TraceableSeriesGroup> trendLineSeriesGroupList;
    private final String plotId;

    public XYTrendLineBuilder(Data data, ChartBindingMap chartBindingMap, XYPlotConfiguration xyPlotConf) {
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        if (xyPlotConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        this.data = data;
        this.chartBindingMap = chartBindingMap;
        this.xyPlotConf = xyPlotConf;
        this.plotId = xyPlotConf.getParent().getPlotId();
        this.trendLineSeriesGroupList = this.createTrendLineSeriesGroupList();
    }

    private List<TraceableSeriesGroup> createTrendLineSeriesGroupList() {
        ArrayList<TraceableSeriesGroup> returnList = new ArrayList<TraceableSeriesGroup>();
        for (TraceableSeriesGroup traceableSeriesGroup : this.data.getTraceableSeriesGroupList(this.plotId)) {
            String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
            com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration seriesGroupConf = this.xyPlotConf.getSeriesGroupConfiguration(seriesGroupId);
            for (int i = 0; i < traceableSeriesGroup.getSeriesCount(); ++i) {
                String seriesId = traceableSeriesGroup.getSeriesId(i);
                this.chartBindingMap.setActiveSeries(this.plotId, seriesGroupId, seriesId);
                SeriesConfiguration seriesConf = seriesGroupConf.getSeriesConfiguration(seriesId);
                returnList.addAll(this.createSeriesTrendLineSeriesGroupList(traceableSeriesGroup, seriesConf, i));
            }
        }
        return returnList;
    }

    private List<TraceableSeriesGroup> createSeriesTrendLineSeriesGroupList(TraceableSeriesGroup traceableSeriesGroup, SeriesConfiguration seriesConf, int seriesIndex) {
        ArrayList<TraceableSeriesGroup> returnList = new ArrayList<TraceableSeriesGroup>();
        List<TrendLineConfiguration> seriesTrendLineConfList = seriesConf.getSeriesTrendLineConfigurationList();
        for (TrendLineConfiguration trendLineConf : seriesTrendLineConfList) {
            TrendLineConfiguration.TrendLineType trendLineType = trendLineConf.getTrendLineType();
            if (trendLineType == TrendLineConfiguration.TrendLineType.CUSTOM_TREND_LINE) {
                TraceableSeriesGroup customSeriesGroup = this.createCustomTrendLineSeriesGroup(traceableSeriesGroup, seriesConf, trendLineConf.getCustomTrendLineConfiguration());
                if (customSeriesGroup == null) continue;
                returnList.add(customSeriesGroup);
                continue;
            }
            if (trendLineType == TrendLineConfiguration.TrendLineType.STANDARD_TREND_LINE) {
                TraceableSeriesGroup standardSeriesGroup = this.createStandardTrendLineSeriesGroup(traceableSeriesGroup, seriesIndex, trendLineConf.getStandardTrendLineConfiguration());
                returnList.add(standardSeriesGroup);
                continue;
            }
            throw new IllegalArgumentException("Unknown trend line type: " + (Object)((Object)trendLineType));
        }
        return returnList;
    }

    private TraceableSeriesGroup createStandardTrendLineSeriesGroup(TraceableSeriesGroup traceableSeriesGroup, int seriesIndex, StandardTrendLineConfiguration standardTrendLineConf) {
        XYDataset trendLineSeriesGroup;
        LineFunction2D function;
        double[] functionParams;
        StandardTrendLineConfiguration.StandardTrendLineType standardTrendLineType = standardTrendLineConf.getStandardTrendLineType();
        XYDataset seriesGroup = (XYDataset)traceableSeriesGroup.getSeriesGroup();
        BigDecimal rangeStart = new BigDecimal(DatasetUtilities.findMinimumDomainValue((XYDataset)seriesGroup).toString());
        BigDecimal rangeEnd = new BigDecimal(DatasetUtilities.findMaximumDomainValue((XYDataset)seriesGroup).toString());
        Number[] xData = new Number[seriesGroup.getItemCount(seriesIndex)];
        Number[] yData = new Number[seriesGroup.getItemCount(seriesIndex)];
        for (int i = 0; i < xData.length; ++i) {
            xData[i] = seriesGroup.getX(seriesIndex, i);
            yData[i] = seriesGroup.getY(seriesIndex, i);
        }
        String trendLineSeriesId = standardTrendLineConf.getParent().getTrendLineSeriesId().evaluateNoException(this.chartBindingMap);
        if (standardTrendLineType == StandardTrendLineConfiguration.StandardTrendLineType.LINEAR_TREND_LINE) {
            functionParams = Util.getPolynomialRegression(seriesGroup, seriesIndex, 1);
            function = new LineFunction2D(functionParams[0], functionParams[1]);
            Integer numberOfSamples = 2;
            trendLineSeriesGroup = DatasetUtilities.sampleFunction2D((Function2D)function, (double)rangeStart.doubleValue(), (double)rangeEnd.doubleValue(), (int)numberOfSamples, (Comparable)((Object)trendLineSeriesId));
        } else if (standardTrendLineType == StandardTrendLineConfiguration.StandardTrendLineType.POWER_TREND_LINE) {
            functionParams = Regression.getPowerRegression((XYDataset)seriesGroup, (int)seriesIndex);
            function = new PowerFunction2D(functionParams[0], functionParams[1]);
            Integer numberOfSamples = seriesGroup.getItemCount(seriesIndex);
            trendLineSeriesGroup = DatasetUtilities.sampleFunction2D((Function2D)function, (double)rangeStart.doubleValue(), (double)rangeEnd.doubleValue(), (int)numberOfSamples, (Comparable)((Object)trendLineSeriesId));
        } else if (standardTrendLineType == StandardTrendLineConfiguration.StandardTrendLineType.MOVING_AVERAGE_TREND_LINE) {
            double[][] movingAverage = Statistics.getMovingAverage((Number[])xData, (Number[])yData, (int)2);
            XYSeries trendLineSeries = new XYSeries((Comparable)((Object)trendLineSeriesId));
            for (double[] aMovingAverage : movingAverage) {
                trendLineSeries.add(aMovingAverage[0], aMovingAverage[1]);
            }
            trendLineSeriesGroup = new XYSeriesCollection(trendLineSeries);
        } else if (standardTrendLineType == StandardTrendLineConfiguration.StandardTrendLineType.POLYNOMIAL_TREND_LINE) {
            functionParams = Util.getPolynomialRegression(seriesGroup, seriesIndex, 2);
            double[] coefficientArr = Arrays.copyOfRange(functionParams, 0, functionParams.length - 1);
            PolynomialFunction2D function2 = new PolynomialFunction2D(coefficientArr);
            Integer numberOfSamples = seriesGroup.getItemCount(seriesIndex);
            trendLineSeriesGroup = DatasetUtilities.sampleFunction2D((Function2D)function2, (double)rangeStart.doubleValue(), (double)rangeEnd.doubleValue(), (int)numberOfSamples, (Comparable)((Object)trendLineSeriesId));
        } else {
            throw new IllegalArgumentException("Unknown standard trend line type: " + (Object)((Object)standardTrendLineType));
        }
        ArrayList<DataSet> seriesDataSetList = new ArrayList<DataSet>();
        ArrayList<String> seriesIdList = new ArrayList<String>();
        seriesDataSetList.add(this.createTrendLineDataSet(traceableSeriesGroup, trendLineSeriesGroup, standardTrendLineConf.getParent()));
        seriesIdList.add(trendLineSeriesId);
        TraceableSeriesGroup returnSeriesGroup = new TraceableSeriesGroup((Dataset)trendLineSeriesGroup, seriesDataSetList, seriesIdList, trendLineSeriesId, this.plotId, standardTrendLineConf.getParent());
        return returnSeriesGroup;
    }

    private TraceableSeriesGroup createCustomTrendLineSeriesGroup(TraceableSeriesGroup traceableSeriesGroup, SeriesConfiguration seriesConf, CustomTrendLineConfiguration seriesCustomTrendLineConf) {
        TraceableSeriesGroup returnSeriesGroup = null;
        String customTrendLineId = seriesCustomTrendLineConf.getCustomTrendLineId();
        com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.CustomTrendLineConfiguration customTrendLineConf = this.xyPlotConf.getParent().getXYPlotConfiguration().getCustomTrendLineConfiguration(customTrendLineId);
        if (customTrendLineConf != null) {
            CustomTrendLineConfiguration.CustomTrendLineType customTrendLineType = customTrendLineConf.getCustomTrendLineType();
            LineFunction2D function = null;
            if (customTrendLineType == CustomTrendLineConfiguration.CustomTrendLineType.LINE_TREND_LINE) {
                LineTrendLineConfiguration lineTrendLineConf = customTrendLineConf.getLineTrendLineConfiguration();
                BigDecimal intercept = lineTrendLineConf.getIntercept().evaluateNoException(this.chartBindingMap);
                BigDecimal slope = lineTrendLineConf.getSlope().evaluateNoException(this.chartBindingMap);
                if (intercept != null && slope != null) {
                    function = new LineFunction2D(intercept.doubleValue(), slope.doubleValue());
                }
            } else {
                throw new IllegalArgumentException("Unknown custom trend line type: " + (Object)((Object)customTrendLineType));
            }
            Integer numberOfSamples = 2;
            XYDataset seriesGroup = (XYDataset)traceableSeriesGroup.getSeriesGroup();
            BigDecimal rangeStart = new BigDecimal(DatasetUtilities.findMinimumDomainValue((XYDataset)seriesGroup).toString());
            BigDecimal rangeEnd = new BigDecimal(DatasetUtilities.findMaximumDomainValue((XYDataset)seriesGroup).toString());
            String trendLineSeriesId = seriesCustomTrendLineConf.getParent().getTrendLineSeriesId().evaluateNoException(this.chartBindingMap);
            if (function != null) {
                XYDataset trendLineSeriesGroup = DatasetUtilities.sampleFunction2D((Function2D)function, (double)rangeStart.doubleValue(), (double)rangeEnd.doubleValue(), (int)numberOfSamples, (Comparable)((Object)trendLineSeriesId));
                ArrayList<DataSet> seriesDataSetList = new ArrayList<DataSet>();
                ArrayList<String> seriesIdList = new ArrayList<String>();
                this.data.getTraceableSeriesGroup(this.plotId, seriesConf.getParent().getSeriesGroupId());
                seriesDataSetList.add(this.createTrendLineDataSet(traceableSeriesGroup, trendLineSeriesGroup, seriesCustomTrendLineConf.getParent()));
                seriesIdList.add(trendLineSeriesId);
                returnSeriesGroup = new TraceableSeriesGroup((Dataset)trendLineSeriesGroup, seriesDataSetList, seriesIdList, trendLineSeriesId, this.plotId, seriesCustomTrendLineConf.getParent());
            }
        }
        return returnSeriesGroup;
    }

    private DataSet createTrendLineDataSet(TraceableSeriesGroup traceableSeriesGroup, XYDataset trendLineSeriesGroup, TrendLineConfiguration trendLineConf) {
        DataSet trendLineDataSet = new DataSet();
        String seriesGroupIdColumn = this.data.getDataConfiguration().getDataSetConfiguration().getSeriesGroupIdColumn();
        String seriesIdColumn = this.data.getDataConfiguration().getDataSetConfiguration().getSeriesIdColumn();
        trendLineDataSet.addColumn(seriesGroupIdColumn, 0);
        trendLineDataSet.addColumn(seriesIdColumn, 0);
        SeriesGroupConfiguration seriesGroupConf = this.data.getDataConfiguration().getDataSetConfiguration().getPlotConfiguration(traceableSeriesGroup.getPlotId()).getSeriesGroupConfiguration(traceableSeriesGroup.getSeriesGroupId());
        SeriesGroupColumnConfiguration seriesGroupColumnConf = seriesGroupConf.getSeriesGroupBuilderConfiguration(this.data.getSeriesGroupBuilderId()).getSeriesGroupColumnConfiguration();
        String domainValueColumn = seriesGroupColumnConf.getDomainValueColumn();
        String rangeValueColumn = seriesGroupColumnConf.getRangeValueColumn();
        int domainValueColumnType = this.data.getDataSet().getColumnType(domainValueColumn);
        int rangeValueColumnType = this.data.getDataSet().getColumnType(rangeValueColumn);
        trendLineDataSet.addColumn(domainValueColumn, domainValueColumnType);
        trendLineDataSet.addColumn(rangeValueColumn, rangeValueColumnType);
        trendLineConf.setDomainValueColumn(domainValueColumn);
        trendLineConf.setRangeValueColumn(rangeValueColumn);
        for (int item = 0; item < trendLineSeriesGroup.getItemCount(0); ++item) {
            int row = trendLineDataSet.addRow();
            trendLineDataSet.setString(row, seriesGroupIdColumn, (String)((Object)trendLineSeriesGroup.getSeriesKey(0)));
            trendLineDataSet.setString(row, seriesIdColumn, (String)((Object)trendLineSeriesGroup.getSeriesKey(0)));
            Number xValue = trendLineSeriesGroup.getX(0, item);
            this.setDataSetItemValue(trendLineDataSet, domainValueColumnType, domainValueColumn, row, xValue);
            Number yValue = trendLineSeriesGroup.getY(0, item);
            this.setDataSetItemValue(trendLineDataSet, rangeValueColumnType, rangeValueColumn, row, yValue);
        }
        return trendLineDataSet;
    }

    private void setDataSetItemValue(DataSet trendLineDataSet, int valueColumnType, String valueColumn, int row, Number value) {
        if (valueColumnType == 2) {
            trendLineDataSet.setDate(row, valueColumn, value.longValue());
        } else if (valueColumnType == 1) {
            trendLineDataSet.setNumber(row, valueColumn, new BigDecimal(value.toString()));
        } else if (valueColumnType == 0) {
            trendLineDataSet.setString(row, valueColumn, value.toString());
        } else {
            throw new IllegalArgumentException("Unknown column type: " + valueColumnType);
        }
    }

    public List<TraceableSeriesGroup> getTrendLineSeriesGroupList() {
        return this.trendLineSeriesGroupList;
    }
}

