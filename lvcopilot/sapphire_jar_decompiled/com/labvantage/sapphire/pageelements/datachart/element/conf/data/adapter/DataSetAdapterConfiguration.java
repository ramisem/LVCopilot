/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AdapterConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataSetAdapterConfiguration
implements Serializable {
    private final List<AdapterConfiguration> adapterConfList;
    private final DataConfiguration parent;

    public DataSetAdapterConfiguration(PropertyList dataSetAdapterProps, DataConfiguration parent) {
        if (dataSetAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.adapterConfList = new ArrayList<AdapterConfiguration>();
        PropertyListCollection adapterCollection = dataSetAdapterProps.getCollectionNotNull("adaptercollection");
        for (int i = 0; i < adapterCollection.size(); ++i) {
            PropertyList adapterProps = adapterCollection.getPropertyList(i);
            this.adapterConfList.add(new AdapterConfiguration(adapterProps, this));
        }
    }

    public DataConfiguration getParent() {
        return this.parent;
    }

    public List<AdapterConfiguration> getAdapterConfigurationList() {
        return this.adapterConfList;
    }
}

