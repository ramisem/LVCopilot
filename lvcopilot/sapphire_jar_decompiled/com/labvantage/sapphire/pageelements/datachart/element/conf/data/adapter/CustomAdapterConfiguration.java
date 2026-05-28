/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.InputConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.OutputConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AdapterConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class CustomAdapterConfiguration
implements Serializable {
    private static final String DEFAULT_CLASS_NAME = "";
    private final AdapterConfiguration parent;
    private final String className;
    private final List<InputConfiguration> adapterInputConfList;
    private final List<OutputConfiguration> adapterOutputConfList;

    public CustomAdapterConfiguration(PropertyList customAdapterProps, AdapterConfiguration parent) {
        if (customAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.className = customAdapterProps.getProperty("classname", DEFAULT_CLASS_NAME);
        this.parent = parent;
        PropertyListCollection inputCollection = customAdapterProps.getCollectionNotNull("inputcollection");
        this.adapterInputConfList = new ArrayList<InputConfiguration>();
        for (int i = 0; i < inputCollection.size(); ++i) {
            PropertyList inputProps = inputCollection.getPropertyList(i);
            this.adapterInputConfList.add(new InputConfiguration(inputProps));
        }
        PropertyListCollection outputCollection = customAdapterProps.getCollectionNotNull("outputcollection");
        this.adapterOutputConfList = new ArrayList<OutputConfiguration>();
        for (int i = 0; i < outputCollection.size(); ++i) {
            PropertyList outputProps = outputCollection.getPropertyList(i);
            this.adapterOutputConfList.add(new OutputConfiguration(outputProps));
        }
    }

    public AdapterConfiguration getParent() {
        return this.parent;
    }

    public String getClassName() {
        return this.className;
    }

    public List<InputConfiguration> getAdapterInputConfigurationList() {
        return this.adapterInputConfList;
    }

    public List<OutputConfiguration> getAdapterOutputConfigurationList() {
        return this.adapterOutputConfList;
    }
}

