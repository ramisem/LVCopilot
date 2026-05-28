/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.category.CategoryDataset
 *  org.jfree.data.general.Dataset
 *  org.jfree.data.general.DefaultPieDataset
 *  org.jfree.data.statistics.Statistics
 *  org.jfree.data.xy.XYDataset
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.util.format.NumericFormatter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataset;
import sapphire.util.M18NUtil;

public final class GroovyHelper
extends BaseCustom
implements Serializable {
    private static final int DEFAULT_SCALE = 5;
    private static final String DEFAULT_DISPLAY_FORMAT = "0.#####";
    private final Data data;
    private final M18NUtil m18NUtil;

    public GroovyHelper(Data data, String connectionId) {
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        if (connectionId == null) {
            throw new IllegalArgumentException("Connection ID is null");
        }
        if (connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection ID is empty");
        }
        this.data = data;
        this.setConnectionId(connectionId);
        this.m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
    }

    private static List<BigDecimal> getDefaultPieValueList(DefaultPieDataset defaultPieDataset) {
        ArrayList<BigDecimal> valueList = new ArrayList<BigDecimal>();
        for (int itemIndex = 0; itemIndex < defaultPieDataset.getItemCount(); ++itemIndex) {
            Number value = defaultPieDataset.getValue(itemIndex);
            if (value == null) continue;
            valueList.add(new BigDecimal(value.toString()));
        }
        return valueList;
    }

    public BigDecimal getRelativeStandardDeviation(String plotId, String seriesGroupId, String seriesId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (seriesGroupId == null) {
            throw new IllegalArgumentException("Series group ID is null");
        }
        if (seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is empty");
        }
        if (seriesId == null) {
            throw new IllegalArgumentException("Series ID is null");
        }
        if (seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is empty");
        }
        BigDecimal standardDeviation = this.getStandardDeviation(plotId, seriesGroupId, seriesId);
        BigDecimal mean = this.getMean(plotId, seriesGroupId, seriesId);
        BigDecimal rds = standardDeviation == null || mean == null || mean.compareTo(BigDecimal.ZERO) == 0 ? null : standardDeviation.divide(mean, 4);
        return rds;
    }

    public BigDecimal getStandardDeviation(String plotId, String seriesGroupId, String seriesId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (seriesGroupId == null) {
            throw new IllegalArgumentException("Series group ID is null");
        }
        if (seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is empty");
        }
        if (seriesId == null) {
            throw new IllegalArgumentException("Series ID is null");
        }
        if (seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is empty");
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup(plotId, seriesGroupId);
        Number[] valueArray = GroovyHelper.getValueArray(traceableSeriesGroup, seriesId);
        BigDecimal standardDeviation = valueArray.length > 1 ? new BigDecimal(Statistics.getStdDev((Number[])valueArray)) : null;
        return standardDeviation;
    }

    public int getN(String plotId, String seriesGroupId, String seriesId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (seriesGroupId == null) {
            throw new IllegalArgumentException("Series group ID is null");
        }
        if (seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is empty");
        }
        if (seriesId == null) {
            throw new IllegalArgumentException("Series ID is null");
        }
        if (seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is empty");
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup(plotId, seriesGroupId);
        Dataset dataset = traceableSeriesGroup.getSeriesGroup();
        if (dataset instanceof XYDataset) {
            XYDataset xyDataset = (XYDataset)dataset;
            int seriesIndex = -1;
            for (int i = 0; i < xyDataset.getSeriesCount(); ++i) {
                if (!xyDataset.getSeriesKey(i).equals(seriesId)) continue;
                seriesIndex = i;
            }
            if (seriesIndex == -1) {
                throw new IllegalArgumentException("Series ID not found in dataset: " + seriesId);
            }
            return xyDataset.getItemCount(seriesIndex);
        }
        if (dataset instanceof CategoryDataset) {
            return traceableSeriesGroup.getSeriesDataSet(seriesId).getRowCount();
        }
        if (dataset instanceof DefaultPieDataset) {
            DefaultPieDataset pieDataSet = (DefaultPieDataset)dataset;
            return pieDataSet.getItemCount();
        }
        throw new IllegalArgumentException("Unknown dataset type: " + dataset);
    }

    public BigDecimal getSum(String plotId, String seriesGroupId, String seriesId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (seriesGroupId == null) {
            throw new IllegalArgumentException("Series group ID is null");
        }
        if (seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is empty");
        }
        if (seriesId == null) {
            throw new IllegalArgumentException("Series ID is null");
        }
        if (seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is empty");
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup(plotId, seriesGroupId);
        BigDecimal sum = BigDecimal.ZERO;
        List<BigDecimal> valueList = GroovyHelper.getValueList(traceableSeriesGroup, seriesId);
        for (BigDecimal value : valueList) {
            sum = sum.add(value);
        }
        return sum;
    }

    public BigDecimal getMax(String plotId, String seriesGroupId, String seriesId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (seriesGroupId == null) {
            throw new IllegalArgumentException("Series group ID is null");
        }
        if (seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is empty");
        }
        if (seriesId == null) {
            throw new IllegalArgumentException("Series ID is null");
        }
        if (seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is empty");
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup(plotId, seriesGroupId);
        BigDecimal max = null;
        List<BigDecimal> valueList = GroovyHelper.getValueList(traceableSeriesGroup, seriesId);
        if (valueList.size() > 0) {
            Collections.sort(valueList);
            max = valueList.get(valueList.size() - 1);
        }
        return max;
    }

    public BigDecimal getMin(String plotId, String seriesGroupId, String seriesId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (seriesGroupId == null) {
            throw new IllegalArgumentException("Series group ID is null");
        }
        if (seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is empty");
        }
        if (seriesId == null) {
            throw new IllegalArgumentException("Series ID is null");
        }
        if (seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is empty");
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup(plotId, seriesGroupId);
        BigDecimal min = null;
        List<BigDecimal> valueList = GroovyHelper.getValueList(traceableSeriesGroup, seriesId);
        if (valueList.size() > 0) {
            Collections.sort(valueList);
            min = valueList.get(0);
        }
        return min;
    }

    public BigDecimal getMedian(String plotId, String seriesGroupId, String seriesId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (seriesGroupId == null) {
            throw new IllegalArgumentException("Series group ID is null");
        }
        if (seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is empty");
        }
        if (seriesId == null) {
            throw new IllegalArgumentException("Series ID is null");
        }
        if (seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is empty");
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup(plotId, seriesGroupId);
        List<BigDecimal> valueList = GroovyHelper.getValueList(traceableSeriesGroup, seriesId);
        BigDecimal median = valueList.size() > 1 ? new BigDecimal(Statistics.calculateMedian(valueList, (boolean)true)) : null;
        return median;
    }

    public BigDecimal getMean(String plotId, String seriesGroupId, String seriesId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (seriesGroupId == null) {
            throw new IllegalArgumentException("Series group ID is null");
        }
        if (seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is empty");
        }
        if (seriesId == null) {
            throw new IllegalArgumentException("Series ID is null");
        }
        if (seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is empty");
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup(plotId, seriesGroupId);
        Number[] valueArray = GroovyHelper.getValueArray(traceableSeriesGroup, seriesId);
        BigDecimal mean = valueArray.length > 1 ? new BigDecimal(Statistics.calculateMean((Number[])valueArray)) : null;
        return mean;
    }

    public String formatNumber(BigDecimal number, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("Negative scale not allowed: " + scale);
        }
        if (number == null) {
            return "";
        }
        return this.m18NUtil.format(number.setScale(scale, 4));
    }

    public String formatNumber(BigDecimal number) {
        return this.formatNumber(number, 5);
    }

    public String formatNumber(BigDecimal number, String displayFormat) {
        String formattedNumber;
        if (number == null) {
            formattedNumber = "";
        } else {
            if (displayFormat == null) {
                throw new IllegalArgumentException("Display format is null");
            }
            if (displayFormat.isEmpty()) {
                displayFormat = DEFAULT_DISPLAY_FORMAT;
            }
            formattedNumber = NumericFormatter.formatNumber(number, displayFormat, I18nUtil.getConnectionLocale(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid())));
        }
        return formattedNumber;
    }

    public static BigDecimal[] getValueArray(TraceableSeriesGroup dataset, String seriesId) {
        List<BigDecimal> yList = GroovyHelper.getValueList(dataset, seriesId);
        return yList.toArray(new BigDecimal[yList.size()]);
    }

    public static List<BigDecimal> getValueList(TraceableSeriesGroup traceableSeriesGroup, String seriesId) {
        return GroovyHelper.getTraceableSeriesGroupValueList(traceableSeriesGroup, seriesId);
    }

    private static List<BigDecimal> getTraceableSeriesGroupValueList(TraceableSeriesGroup traceableSeriesGroup, String seriesId) {
        if (traceableSeriesGroup == null) {
            throw new IllegalArgumentException("Dataset is null");
        }
        if (seriesId == null) {
            throw new IllegalArgumentException("Series ID is null");
        }
        if (seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is empty");
        }
        Dataset dataset = traceableSeriesGroup.getSeriesGroup();
        if (dataset instanceof XYDataset) {
            return GroovyHelper.getXYValueList(seriesId, (XYDataset)dataset);
        }
        if (dataset instanceof CategoryDataset) {
            return GroovyHelper.getCategoryValueList(seriesId, (CategoryDataset)dataset);
        }
        if (dataset instanceof DefaultPieDataset) {
            return GroovyHelper.getDefaultPieValueList((DefaultPieDataset)dataset);
        }
        throw new IllegalArgumentException("Unknown dataset type: " + dataset);
    }

    private static List<BigDecimal> getCategoryValueList(String seriesId, CategoryDataset categoryDataset) {
        ArrayList<BigDecimal> valueList = new ArrayList<BigDecimal>();
        int seriesIndex = -1;
        for (int i = 0; i < categoryDataset.getRowCount(); ++i) {
            if (!categoryDataset.getRowKey(i).equals(seriesId)) continue;
            seriesIndex = i;
        }
        if (seriesIndex == -1) {
            throw new IllegalArgumentException("Series ID not found in dataset: " + seriesId);
        }
        for (int itemIndex = 0; itemIndex < categoryDataset.getColumnCount(); ++itemIndex) {
            Number value = categoryDataset.getValue(seriesIndex, itemIndex);
            if (value == null) continue;
            valueList.add(new BigDecimal(value.toString()));
        }
        return valueList;
    }

    private static List<BigDecimal> getXYValueList(String seriesId, XYDataset xyDataset) {
        ArrayList<BigDecimal> valueList = new ArrayList<BigDecimal>();
        int seriesIndex = -1;
        for (int i = 0; i < xyDataset.getSeriesCount(); ++i) {
            if (!xyDataset.getSeriesKey(i).equals(seriesId)) continue;
            seriesIndex = i;
        }
        if (seriesIndex == -1) {
            throw new IllegalArgumentException("Series ID not found in dataset: " + seriesId);
        }
        for (int itemIndex = 0; itemIndex < xyDataset.getItemCount(seriesIndex); ++itemIndex) {
            try {
                valueList.add(new BigDecimal(xyDataset.getYValue(seriesIndex, itemIndex)));
                continue;
            }
            catch (NumberFormatException e) {
                valueList.clear();
                break;
            }
        }
        return valueList;
    }
}

