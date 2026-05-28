/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DefaultCategoryDatasetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DefaultPieDatasetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.TimePeriodValuesCollectionConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.XYSeriesCollectionConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.YIntervalSeriesCollectionConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class SeriesGroupBuilderConfiguration
implements ConfigurationListItem,
Serializable {
    private static final String DEFAULT_SERIES_GROUP_TYPE = SeriesGroupBuilderType.TIME_PERIOD_VALUES_COLLECTION.getName();
    private final String seriesGroupBuilderId;
    private final SeriesGroupBuilderType seriesGroupBuilderType;
    private final TimePeriodValuesCollectionConfiguration timePeriodValuesCollectionConf;
    private final XYSeriesCollectionConfiguration xySeriesCollectionConf;
    private final YIntervalSeriesCollectionConfiguration yIntervalSeriesCollectionConf;
    private final DefaultCategoryDatasetConfiguration defaultCategoryDatasetConf;
    private final DefaultPieDatasetConfiguration defaultPieDatasetConf;
    private final SeriesGroupConfiguration parent;

    public SeriesGroupBuilderConfiguration(PropertyList seriesGroupBuilderProps, SeriesGroupConfiguration parent) {
        if (seriesGroupBuilderProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.seriesGroupBuilderId = seriesGroupBuilderProps.getProperty("seriesgroupbuilderid");
        this.seriesGroupBuilderType = SeriesGroupBuilderType.fromString(seriesGroupBuilderProps.getProperty("seriesgrouptype", DEFAULT_SERIES_GROUP_TYPE));
        this.timePeriodValuesCollectionConf = this.seriesGroupBuilderType == SeriesGroupBuilderType.TIME_PERIOD_VALUES_COLLECTION ? new TimePeriodValuesCollectionConfiguration(seriesGroupBuilderProps.getPropertyListNotNull("timeperiodvaluescollectionprops"), this) : null;
        this.xySeriesCollectionConf = this.seriesGroupBuilderType == SeriesGroupBuilderType.XY_SERIES_COLLECTION ? new XYSeriesCollectionConfiguration(seriesGroupBuilderProps.getPropertyListNotNull("xyseriescollectionprops"), this) : null;
        this.defaultCategoryDatasetConf = this.seriesGroupBuilderType == SeriesGroupBuilderType.DEFAULT_CATEGORY_DATASET ? new DefaultCategoryDatasetConfiguration(seriesGroupBuilderProps.getPropertyListNotNull("defaultcategorydatasetprops"), this) : null;
        this.defaultPieDatasetConf = this.seriesGroupBuilderType == SeriesGroupBuilderType.DEFAULT_PIE_DATASET ? new DefaultPieDatasetConfiguration(seriesGroupBuilderProps.getPropertyListNotNull("defaultpiedatasetprops"), this) : null;
        this.yIntervalSeriesCollectionConf = this.seriesGroupBuilderType == SeriesGroupBuilderType.Y_INTERVAL_COLLECTION ? new YIntervalSeriesCollectionConfiguration(seriesGroupBuilderProps.getPropertyListNotNull("yintervalseriescollectionprops"), this) : null;
        this.parent = parent;
    }

    public SeriesGroupConfiguration getParent() {
        return this.parent;
    }

    public SeriesGroupBuilderType getSeriesGroupBuilderType() {
        return this.seriesGroupBuilderType;
    }

    public String getSeriesGroupBuilderId() {
        return this.seriesGroupBuilderId;
    }

    public DefaultPieDatasetConfiguration getDefaultPieDatasetConfiguration() {
        if (this.defaultPieDatasetConf == null) {
            throw new IllegalStateException("Series group type is: " + (Object)((Object)this.seriesGroupBuilderType));
        }
        return this.defaultPieDatasetConf;
    }

    public TimePeriodValuesCollectionConfiguration getTimePeriodValuesCollectionConfiguration() {
        if (this.timePeriodValuesCollectionConf == null) {
            throw new IllegalStateException("Series group type is: " + (Object)((Object)this.seriesGroupBuilderType));
        }
        return this.timePeriodValuesCollectionConf;
    }

    public DefaultCategoryDatasetConfiguration getDefaultCategoryDatasetConfiguration() {
        if (this.defaultCategoryDatasetConf == null) {
            throw new IllegalStateException("Series group type is: " + (Object)((Object)this.seriesGroupBuilderType));
        }
        return this.defaultCategoryDatasetConf;
    }

    public XYSeriesCollectionConfiguration getXYSeriesCollectionConfiguration() {
        if (this.xySeriesCollectionConf == null) {
            throw new IllegalStateException("Series group type is: " + (Object)((Object)this.seriesGroupBuilderType));
        }
        return this.xySeriesCollectionConf;
    }

    public YIntervalSeriesCollectionConfiguration getYIntervalSeriesCollectionConfiguration() {
        if (this.yIntervalSeriesCollectionConf == null) {
            throw new IllegalStateException("Series group type is: " + (Object)((Object)this.seriesGroupBuilderType));
        }
        return this.yIntervalSeriesCollectionConf;
    }

    @Override
    public String getId() {
        return this.getSeriesGroupBuilderId();
    }

    public SeriesGroupColumnConfiguration getSeriesGroupColumnConfiguration() {
        if (this.seriesGroupBuilderType == SeriesGroupBuilderType.TIME_PERIOD_VALUES_COLLECTION) {
            return this.timePeriodValuesCollectionConf;
        }
        if (this.seriesGroupBuilderType == SeriesGroupBuilderType.DEFAULT_CATEGORY_DATASET) {
            return this.defaultCategoryDatasetConf;
        }
        if (this.seriesGroupBuilderType == SeriesGroupBuilderType.XY_SERIES_COLLECTION) {
            return this.xySeriesCollectionConf;
        }
        if (this.seriesGroupBuilderType == SeriesGroupBuilderType.DEFAULT_PIE_DATASET) {
            return this.defaultPieDatasetConf;
        }
        if (this.seriesGroupBuilderType == SeriesGroupBuilderType.Y_INTERVAL_COLLECTION) {
            return this.yIntervalSeriesCollectionConf;
        }
        throw new IllegalArgumentException("Unknown series group type: " + (Object)((Object)this.seriesGroupBuilderType));
    }

    public static enum SeriesGroupBuilderType {
        TIME_PERIOD_VALUES_COLLECTION("Time Period Values Collection", PlotConfiguration.PlotType.XY_PLOT),
        DEFAULT_CATEGORY_DATASET("Default Category Dataset", PlotConfiguration.PlotType.CATEGORY_PLOT),
        XY_SERIES_COLLECTION("XY Series Collection", PlotConfiguration.PlotType.XY_PLOT),
        DEFAULT_PIE_DATASET("Default Pie Dataset", PlotConfiguration.PlotType.PIE_PLOT),
        Y_INTERVAL_COLLECTION("Y Interval Series Collection", PlotConfiguration.PlotType.XY_PLOT);

        private final String name;
        private final PlotConfiguration.PlotType plotType;

        private SeriesGroupBuilderType(String name, PlotConfiguration.PlotType plotType) {
            this.name = name;
            this.plotType = plotType;
        }

        public static SeriesGroupBuilderType fromString(String name) {
            if (name != null) {
                for (SeriesGroupBuilderType type : SeriesGroupBuilderType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }

        public PlotConfiguration.PlotType getPlotType() {
            return this.plotType;
        }
    }
}

