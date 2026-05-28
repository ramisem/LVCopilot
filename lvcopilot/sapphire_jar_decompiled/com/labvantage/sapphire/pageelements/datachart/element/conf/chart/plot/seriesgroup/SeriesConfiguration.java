/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.ItemConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesGroupConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.TrendLineConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SeriesConfiguration
implements ConfigurationListItem,
Serializable {
    private static final String DEFAULT_SHOW_LEGEND_ITEM = "Y";
    private static final String DEFAULT_TRANSLATE_LEGEND_LABEL = "N";
    private final SeriesGroupConfiguration parent;
    private final String seriesId;
    private final String seriesStyleId;
    private final boolean showLegendItem;
    private final StringExpression legendLabel;
    private final boolean translateLegendLabel;
    private final List<TrendLineConfiguration> trendLineConfList;
    private final ItemConfiguration itemConf;

    public SeriesConfiguration(PropertyList seriesProps, SeriesGroupConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (seriesProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.seriesId = seriesProps.getProperty("seriesid", "");
        this.seriesStyleId = seriesProps.getProperty("seriesstyleid", this.seriesId);
        this.showLegendItem = seriesProps.getProperty("showlegenditem", DEFAULT_SHOW_LEGEND_ITEM).toLowerCase().startsWith("y");
        this.legendLabel = new StringExpression(seriesProps.getProperty("legendlabel"));
        this.translateLegendLabel = seriesProps.getProperty("translatelegendlabel", DEFAULT_TRANSLATE_LEGEND_LABEL).toLowerCase().startsWith("y");
        this.trendLineConfList = new ArrayList<TrendLineConfiguration>();
        PropertyListCollection trendLineCollection = seriesProps.getCollectionNotNull("trendlinecollection");
        for (int i = 0; i < trendLineCollection.size(); ++i) {
            PropertyList trendLineProps = trendLineCollection.getPropertyList(i);
            this.trendLineConfList.add(new TrendLineConfiguration(trendLineProps, this));
        }
        this.itemConf = new ItemConfiguration(seriesProps.getPropertyListNotNull("itemprops"), this);
        this.parent = parent;
    }

    public SeriesConfiguration(SeriesConfiguration copy, SeriesGroupConfiguration parent) {
        this(copy, copy.seriesId, parent);
    }

    public boolean translateLegendLabel() {
        return this.translateLegendLabel;
    }

    public SeriesConfiguration(SeriesConfiguration copy, String seriesId, SeriesGroupConfiguration parent) {
        this.seriesId = seriesId;
        this.seriesStyleId = copy.seriesStyleId.isEmpty() || copy.seriesStyleId.equals(seriesId) ? seriesId : copy.seriesStyleId;
        this.trendLineConfList = new ArrayList<TrendLineConfiguration>();
        for (TrendLineConfiguration trendLineConfiguration : copy.trendLineConfList) {
            this.trendLineConfList.add(new TrendLineConfiguration(trendLineConfiguration, this));
        }
        this.itemConf = new ItemConfiguration(copy.itemConf, this);
        this.showLegendItem = copy.showLegendItem;
        this.legendLabel = new StringExpression(copy.legendLabel);
        this.translateLegendLabel = copy.translateLegendLabel;
        this.parent = parent;
    }

    public StringExpression getLegendLabel() {
        return this.legendLabel;
    }

    public boolean showLegendItem() {
        return this.showLegendItem;
    }

    public String getSeriesId() {
        return this.seriesId;
    }

    public String getSeriesStyleId() {
        return this.seriesStyleId;
    }

    public SeriesGroupConfiguration getParent() {
        return this.parent;
    }

    @Override
    public String getId() {
        return this.getSeriesId();
    }

    public List<TrendLineConfiguration> getSeriesTrendLineConfigurationList() {
        return this.trendLineConfList;
    }

    public ItemConfiguration getItemConfiguration() {
        return this.itemConf;
    }
}

