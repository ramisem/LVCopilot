/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.CustomAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.DataSetAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class AdapterConfiguration
implements Serializable {
    private static final String DEFAULT_ADAPTER_TYPE = AdapterType.STANDARD.getName();
    private static final String DEFAULT_ENABLE = "Y";
    private final DataSetAdapterConfiguration parent;
    private final StandardAdapterConfiguration standardAdapterConf;
    private final CustomAdapterConfiguration customAdapterConf;
    private final AdapterType adapterType;
    private final String adapterId;
    private final boolean enable;

    public AdapterConfiguration(PropertyList adapterProps, DataSetAdapterConfiguration parent) {
        if (adapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.adapterId = adapterProps.getProperty("adapterid");
        this.adapterType = AdapterType.fromString(adapterProps.getProperty("adaptertype", DEFAULT_ADAPTER_TYPE));
        if (this.adapterType == AdapterType.CUSTOM) {
            PropertyList customAdapterProps = adapterProps.getPropertyListNotNull("customadapterprops");
            this.customAdapterConf = new CustomAdapterConfiguration(customAdapterProps, this);
        } else {
            this.customAdapterConf = null;
        }
        if (this.adapterType == AdapterType.STANDARD) {
            PropertyList standardAdapterProps = adapterProps.getPropertyListNotNull("standardadapterprops");
            this.standardAdapterConf = new StandardAdapterConfiguration(standardAdapterProps, this);
        } else {
            this.standardAdapterConf = null;
        }
        this.enable = adapterProps.getProperty("enable", DEFAULT_ENABLE).toLowerCase().startsWith("y");
    }

    public boolean isEnabled() {
        return this.enable;
    }

    public String getAdapterId() {
        return this.adapterId;
    }

    public DataSetAdapterConfiguration getParent() {
        return this.parent;
    }

    public StandardAdapterConfiguration getStandardAdapterConfiguration() {
        if (this.adapterType != AdapterType.STANDARD) {
            throw new IllegalStateException("Adapter type is: " + (Object)((Object)this.adapterType));
        }
        return this.standardAdapterConf;
    }

    public CustomAdapterConfiguration getCustomAdapterConfiguration() {
        if (this.adapterType != AdapterType.CUSTOM) {
            throw new IllegalStateException("Adapter type is: " + (Object)((Object)this.adapterType));
        }
        return this.customAdapterConf;
    }

    public AdapterType getAdapterType() {
        return this.adapterType;
    }

    public static enum AdapterType {
        CUSTOM("Custom"),
        STANDARD("Standard");

        private final String name;

        private AdapterType(String name) {
            this.name = name;
        }

        public static AdapterType fromString(String name) {
            if (name != null) {
                for (AdapterType type : AdapterType.values()) {
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

