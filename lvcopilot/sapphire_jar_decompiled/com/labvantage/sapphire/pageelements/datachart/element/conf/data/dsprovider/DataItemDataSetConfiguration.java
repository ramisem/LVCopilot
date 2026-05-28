/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.StandardDataSetProviderConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class DataItemDataSetConfiguration
implements Serializable {
    private final PropertyList queryDataSetProps;
    private final PropertyList extractBoundariesAdapterProps;
    private final PropertyList sdiDataItemAdapterProps;
    private final PropertyList addSdiNotesAdapterProps;
    private final PropertyList customDataSetProps;
    private final PropertyList sdiListDataSetProps;
    private final PropertyList dataItemParamListKeyProps;
    private final String mode;
    private final StandardDataSetProviderConfiguration parent;

    public DataItemDataSetConfiguration(PropertyList dataItemDataSetProps, StandardDataSetProviderConfiguration parent) {
        if (dataItemDataSetProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (!dataItemDataSetProps.containsKey("jUnit") && parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.queryDataSetProps = dataItemDataSetProps.getPropertyListNotNull("querydatasetprops");
        this.sdiListDataSetProps = dataItemDataSetProps.getPropertyListNotNull("sdilistdatasetprops");
        this.sdiDataItemAdapterProps = dataItemDataSetProps.getPropertyListNotNull("sdidataitemadapterprops");
        this.extractBoundariesAdapterProps = dataItemDataSetProps.getPropertyListNotNull("extractboundariesadapterprops");
        this.addSdiNotesAdapterProps = dataItemDataSetProps.getPropertyListNotNull("addsdinotesadapterprops");
        this.customDataSetProps = dataItemDataSetProps.getPropertyListNotNull("customdatasetprops");
        this.dataItemParamListKeyProps = dataItemDataSetProps.getPropertyListNotNull("dataitemparamlistkeyadapterprops");
        this.mode = dataItemDataSetProps.getProperty("datasetmode", StandardDataSetProviderConfiguration.StandardDataSetType.QUERY.getName());
    }

    public PropertyList getQueryDataSetProps() {
        return this.queryDataSetProps;
    }

    public PropertyList getExtractBoundariesAdapterProps() {
        return this.extractBoundariesAdapterProps;
    }

    public PropertyList getSdiDataItemAdapterProps() {
        return this.sdiDataItemAdapterProps;
    }

    public PropertyList getAddSdiNotesAdapterProps() {
        return this.addSdiNotesAdapterProps;
    }

    public PropertyList getSdiListDataSetProps() {
        return this.sdiListDataSetProps;
    }

    public PropertyList getDataItemParamListKeyProps() {
        return this.dataItemParamListKeyProps;
    }

    public PropertyList getCustomDataSetProps() {
        return this.customDataSetProps;
    }

    public String getMode() {
        return this.mode;
    }

    public StandardDataSetProviderConfiguration getParent() {
        return this.parent;
    }
}

