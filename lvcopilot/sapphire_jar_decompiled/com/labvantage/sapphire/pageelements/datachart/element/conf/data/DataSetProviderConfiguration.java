/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.StandardDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.CustomDataSetConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class DataSetProviderConfiguration
implements Serializable {
    private static final String DEFAULT_DATA_SET_TYPE = DataSetType.STANDARD.getName();
    private final DataSetType dataSetType;
    private final StandardDataSetProviderConfiguration standardDataSetConf;
    private final CustomDataSetConfiguration customDataSetConf;
    private final DataConfiguration parent;

    public DataSetProviderConfiguration(PropertyList dataSetProps, DataConfiguration parent) {
        if (dataSetProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.dataSetType = DataSetType.fromString(dataSetProps.getProperty("datasettype", DEFAULT_DATA_SET_TYPE));
        this.standardDataSetConf = this.dataSetType == DataSetType.STANDARD ? new StandardDataSetProviderConfiguration(dataSetProps.getPropertyListNotNull("standarddatasetprops"), this) : null;
        this.customDataSetConf = this.dataSetType == DataSetType.CUSTOM ? new CustomDataSetConfiguration(dataSetProps.getPropertyListNotNull("customdatasetprops"), this) : null;
        this.parent = parent;
    }

    public DataConfiguration getParent() {
        return this.parent;
    }

    public StandardDataSetProviderConfiguration getStandardDataSetProviderConfiguration() {
        if (this.standardDataSetConf == null) {
            throw new IllegalStateException("Data set type is: " + (Object)((Object)this.dataSetType));
        }
        return this.standardDataSetConf;
    }

    public CustomDataSetConfiguration getCustomDataSetConfiguration() {
        if (this.customDataSetConf == null) {
            throw new IllegalStateException("Data set type is: " + (Object)((Object)this.dataSetType));
        }
        return this.customDataSetConf;
    }

    public DataSetType getDataSetType() {
        return this.dataSetType;
    }

    public static enum DataSetType {
        STANDARD("Standard"),
        CUSTOM("Custom");

        private final String name;

        private DataSetType(String name) {
            this.name = name;
        }

        public static DataSetType fromString(String name) {
            if (name != null) {
                for (DataSetType type : DataSetType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

