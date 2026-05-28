/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.EventConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class URLGeneratorConfiguration
implements Serializable {
    private final String urlGeneratorId;
    private final PlotConfiguration parent;
    private final List<EventConfiguration> eventConfList;

    public URLGeneratorConfiguration(PropertyList urlGeneratorProps, PlotConfiguration parent) {
        if (urlGeneratorProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.urlGeneratorId = urlGeneratorProps.getProperty("urlgeneratorid");
        this.eventConfList = new ArrayList<EventConfiguration>();
        PropertyListCollection eventCollection = urlGeneratorProps.getCollectionNotNull("eventcollection");
        for (int i = 0; i < eventCollection.size(); ++i) {
            PropertyList eventProps = eventCollection.getPropertyList(i);
            this.eventConfList.add(new EventConfiguration(eventProps, this));
        }
    }

    public URLGeneratorConfiguration(URLGeneratorConfiguration copy, PlotConfiguration parent) {
        this.parent = parent;
        this.urlGeneratorId = copy.urlGeneratorId;
        this.eventConfList = new ArrayList<EventConfiguration>();
        for (EventConfiguration eventConf : copy.eventConfList) {
            this.eventConfList.add(new EventConfiguration(eventConf, this));
        }
    }

    public String getUrlGeneratorId() {
        return this.urlGeneratorId;
    }

    public List<EventConfiguration> getEventConfigurationList() {
        return this.eventConfList;
    }

    public PlotConfiguration getParent() {
        return this.parent;
    }
}

