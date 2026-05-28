/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.InputConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.OutputConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CustomDataSetConfiguration
implements Serializable {
    private final String className;
    private final List<OutputConfiguration> outputConfList;
    private final List<InputConfiguration> inputConfList;
    private final DataSetProviderConfiguration parent;

    public CustomDataSetConfiguration(PropertyList customDataSetProps, DataSetProviderConfiguration parent) {
        if (customDataSetProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.className = customDataSetProps.getProperty("classname", "");
        this.parent = parent;
        PropertyListCollection inputCollection = customDataSetProps.getCollectionNotNull("inputcollection");
        this.inputConfList = new ArrayList<InputConfiguration>();
        for (int i = 0; i < inputCollection.size(); ++i) {
            PropertyList inputProps = inputCollection.getPropertyList(i);
            this.inputConfList.add(new InputConfiguration(inputProps));
        }
        PropertyListCollection outputCollection = customDataSetProps.getCollectionNotNull("outputcollection");
        this.outputConfList = new ArrayList<OutputConfiguration>();
        for (int i = 0; i < outputCollection.size(); ++i) {
            PropertyList outputProps = outputCollection.getPropertyList(i);
            this.outputConfList.add(new OutputConfiguration(outputProps));
        }
    }

    public String getClassName() {
        return this.className;
    }

    public List<InputConfiguration> getInputConfigurationList() {
        return this.inputConfList;
    }

    public List<OutputConfiguration> getOutputConfigurationList() {
        return this.outputConfList;
    }

    public DataSetProviderConfiguration getParent() {
        return this.parent;
    }
}

