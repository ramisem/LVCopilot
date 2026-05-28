/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.ItemStyleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.MarkerConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.seriesgroup.SeriesConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ItemConfiguration
implements Serializable {
    private final SeriesConfiguration parent;
    private final List<ItemStyleConfiguration> itemStyleConfList;
    private final List<MarkerConfiguration> rangeMarkerConfList;
    private final List<MarkerConfiguration> domainMarkerConfList;

    public ItemConfiguration(PropertyList itemProps, SeriesConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (itemProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.itemStyleConfList = new ArrayList<ItemStyleConfiguration>();
        PropertyListCollection itemStyleCollection = itemProps.getCollectionNotNull("itemstylecollection");
        for (int i = 0; i < itemStyleCollection.size(); ++i) {
            PropertyList itemStyleProps = itemStyleCollection.getPropertyList(i);
            this.itemStyleConfList.add(new ItemStyleConfiguration(itemStyleProps, this));
        }
        this.rangeMarkerConfList = new ArrayList<MarkerConfiguration>();
        PropertyListCollection rangeMarkerCollection = itemProps.getCollectionNotNull("rangemarkercollection");
        for (int i = 0; i < rangeMarkerCollection.size(); ++i) {
            PropertyList rangeMarkerProps = rangeMarkerCollection.getPropertyList(i);
            this.rangeMarkerConfList.add(new MarkerConfiguration(rangeMarkerProps, this));
        }
        this.domainMarkerConfList = new ArrayList<MarkerConfiguration>();
        PropertyListCollection domainMarkerCollection = itemProps.getCollectionNotNull("domainmarkercollection");
        for (int i = 0; i < domainMarkerCollection.size(); ++i) {
            PropertyList domainMarkerProps = domainMarkerCollection.getPropertyList(i);
            this.domainMarkerConfList.add(new MarkerConfiguration(domainMarkerProps, this));
        }
    }

    public ItemConfiguration(ItemConfiguration copy, SeriesConfiguration parent) {
        this.itemStyleConfList = new ArrayList<ItemStyleConfiguration>();
        for (ItemStyleConfiguration itemStyleConf : copy.getItemStyleConfigurationList()) {
            this.itemStyleConfList.add(new ItemStyleConfiguration(itemStyleConf, this));
        }
        this.rangeMarkerConfList = new ArrayList<MarkerConfiguration>();
        for (MarkerConfiguration rangeMarkerConf : copy.getRangeMarkerConfigurationList()) {
            this.rangeMarkerConfList.add(new MarkerConfiguration(rangeMarkerConf, this));
        }
        this.domainMarkerConfList = new ArrayList<MarkerConfiguration>();
        for (MarkerConfiguration domainMarkerConf : copy.getDomainMarkerConfigurationList()) {
            this.domainMarkerConfList.add(new MarkerConfiguration(domainMarkerConf, this));
        }
        this.parent = parent;
    }

    public List<MarkerConfiguration> getRangeMarkerConfigurationList() {
        return this.rangeMarkerConfList;
    }

    public List<MarkerConfiguration> getDomainMarkerConfigurationList() {
        return this.domainMarkerConfList;
    }

    public List<ItemStyleConfiguration> getItemStyleConfigurationList() {
        return this.itemStyleConfList;
    }

    public SeriesConfiguration getParent() {
        return this.parent;
    }
}

