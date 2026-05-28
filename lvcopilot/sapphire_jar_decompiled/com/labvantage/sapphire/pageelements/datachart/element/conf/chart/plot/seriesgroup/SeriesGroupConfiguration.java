/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup;

import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationListItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.ConfigurationUtil;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.ComponentPlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SeriesGroupConfiguration
implements ConfigurationListItem,
Serializable {
    private static long uniquePrefix = 0L;
    private static final String DEFAULT_SHOW_LEGEND_ITEMS = "Y";
    private static final String DEFAULT_INCLUDE_IN_DATA_EXPORT = "Y";
    private final StringExpression domainAxisId;
    private final StringExpression rangeAxisId;
    private final String rendererId;
    private final String seriesGroupId;
    private final List<SeriesConfiguration> seriesConfList;
    private final List<String> rangeMarkerIdList;
    private final List<String> domainMarkerIdList;
    private final boolean showLegendItems;
    private final boolean includeInDataExport;
    private ComponentPlotConfiguration parent;
    private int nextSeriesGroupConfIndex;

    public SeriesGroupConfiguration(PropertyList seriesGroupProps, ComponentPlotConfiguration parent) {
        if (seriesGroupProps == null) {
            throw new IllegalArgumentException("Series group props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.seriesGroupId = seriesGroupProps.getProperty("seriesgroupid", "");
        String defaultComponentId = "";
        if (!this.seriesGroupId.isEmpty()) {
            String uniquePrefix = this.getUniquePrefix();
            defaultComponentId = this.seriesGroupId + uniquePrefix;
        }
        this.domainAxisId = new StringExpression(seriesGroupProps.getProperty("primarydomainaxisid", defaultComponentId));
        this.rangeAxisId = new StringExpression(seriesGroupProps.getProperty("primaryrangeaxisid", defaultComponentId));
        this.rangeMarkerIdList = new ArrayList<String>();
        PropertyListCollection rangeMarkerCollection = seriesGroupProps.getCollectionNotNull("rangemarkercollection");
        for (int i = 0; i < rangeMarkerCollection.size(); ++i) {
            PropertyList rangeMarkerProps = rangeMarkerCollection.getPropertyList(i);
            this.rangeMarkerIdList.add(rangeMarkerProps.getProperty("markerid"));
        }
        this.domainMarkerIdList = new ArrayList<String>();
        PropertyListCollection domainMarkerCollection = seriesGroupProps.getCollectionNotNull("domainmarkercollection");
        for (int i = 0; i < domainMarkerCollection.size(); ++i) {
            PropertyList domainMarkerProps = domainMarkerCollection.getPropertyList(i);
            this.domainMarkerIdList.add(domainMarkerProps.getProperty("markerid"));
        }
        this.rendererId = seriesGroupProps.getProperty("rendererid", defaultComponentId);
        this.showLegendItems = seriesGroupProps.getProperty("showlegenditems", "Y").toLowerCase().startsWith("y");
        this.includeInDataExport = seriesGroupProps.getProperty("includeindataexport", "Y").toLowerCase().startsWith("y");
        this.seriesConfList = new ArrayList<SeriesConfiguration>();
        PropertyListCollection seriesCollection = seriesGroupProps.getCollectionNotNull("seriescollection");
        for (int i = 0; i < seriesCollection.size(); ++i) {
            PropertyList seriesProps = seriesCollection.getPropertyList(i);
            this.seriesConfList.add(new SeriesConfiguration(seriesProps, this));
        }
        if (this.seriesConfList.isEmpty() || ConfigurationUtil.countAnonymousConfigurations(this.seriesConfList) == 0) {
            this.seriesConfList.add(new SeriesConfiguration(new PropertyList(), this));
        }
        this.nextSeriesGroupConfIndex = 0;
        this.parent = parent;
    }

    public SeriesGroupConfiguration(SeriesGroupConfiguration copy, String seriesGroupId, ComponentPlotConfiguration parent) {
        if (seriesGroupId == null) {
            throw new IllegalArgumentException("Series group ID is null");
        }
        this.rangeMarkerIdList = new ArrayList<String>();
        for (String rangeMarkerId : copy.rangeMarkerIdList) {
            this.rangeMarkerIdList.add(rangeMarkerId);
        }
        this.domainMarkerIdList = new ArrayList<String>();
        for (String domainMarkerId : copy.domainMarkerIdList) {
            this.domainMarkerIdList.add(domainMarkerId);
        }
        String uniquePrefix = this.getUniquePrefix();
        this.seriesGroupId = seriesGroupId;
        this.domainAxisId = copy.domainAxisId.getExpression().isEmpty() ? new StringExpression(seriesGroupId + uniquePrefix) : copy.domainAxisId;
        this.rangeAxisId = copy.rangeAxisId.getExpression().isEmpty() ? new StringExpression(seriesGroupId + uniquePrefix) : copy.rangeAxisId;
        this.rendererId = copy.rendererId.isEmpty() ? seriesGroupId + uniquePrefix : copy.rendererId;
        this.showLegendItems = copy.showLegendItems;
        this.includeInDataExport = copy.includeInDataExport;
        this.seriesConfList = new ArrayList<SeriesConfiguration>();
        for (SeriesConfiguration aSeriesConfList : copy.seriesConfList) {
            this.seriesConfList.add(new SeriesConfiguration(aSeriesConfList, this));
        }
        this.parent = parent;
    }

    private String getUniquePrefix() {
        return Long.toString(++uniquePrefix);
    }

    public boolean includeInDataExport() {
        return this.includeInDataExport;
    }

    public boolean showLegendItems() {
        return this.showLegendItems;
    }

    public String getSeriesGroupId() {
        return this.seriesGroupId;
    }

    public StringExpression getDomainAxisId() {
        return this.domainAxisId;
    }

    public StringExpression getRangeAxisId() {
        return this.rangeAxisId;
    }

    public String getRendererId() {
        return this.rendererId;
    }

    @Override
    public String getId() {
        return this.getSeriesGroupId();
    }

    public ComponentPlotConfiguration getParent() {
        return this.parent;
    }

    public SeriesConfiguration getSeriesConfiguration(String seriesId) {
        SeriesConfiguration returnConf = ConfigurationUtil.getConf(this.seriesConfList, seriesId);
        if (returnConf == null) {
            int aIndex = ConfigurationUtil.getAnonymousConf(this.seriesConfList, this.nextSeriesGroupConfIndex);
            ++this.nextSeriesGroupConfIndex;
            returnConf = new SeriesConfiguration(this.seriesConfList.get(aIndex), seriesId, this);
            this.seriesConfList.add(returnConf);
        }
        return returnConf;
    }

    public boolean isItemStylesEnabled() {
        boolean itemStylesEnabled = false;
        for (SeriesConfiguration seriesConf : this.seriesConfList) {
            if (seriesConf.getItemConfiguration().getItemStyleConfigurationList().size() <= 0) continue;
            itemStylesEnabled = true;
        }
        return itemStylesEnabled;
    }

    public List<String> getRangeMarkerIdList() {
        return this.rangeMarkerIdList;
    }

    public List<String> getDomainMarkerIdList() {
        return this.domainMarkerIdList;
    }

    public List<SeriesConfiguration> getSeriesConfList() {
        return this.seriesConfList;
    }
}

