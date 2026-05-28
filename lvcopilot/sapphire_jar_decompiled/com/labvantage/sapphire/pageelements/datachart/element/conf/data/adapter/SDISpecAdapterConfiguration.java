/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class SDISpecAdapterConfiguration
implements Serializable {
    private final StandardAdapterConfiguration parent;
    private final String sdcId;

    public String getSdcId() {
        return this.sdcId;
    }

    public SDISpecAdapterConfiguration(PropertyList sdiDataItemAdapterProps, StandardAdapterConfiguration parent) {
        if (sdiDataItemAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (!sdiDataItemAdapterProps.containsKey("jUnit") && parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.sdcId = sdiDataItemAdapterProps.getProperty("sdcid", "");
    }

    public StandardAdapterConfiguration getParent() {
        return this.parent;
    }
}

