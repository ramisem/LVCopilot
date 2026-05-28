/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class AccessControlAdapterConfiguration
implements Serializable {
    private static final String DEFAULT_SDC_ID = "Sample";
    private static final String DEFAULT_KEY_ID1_COLUMN = "keyid1";
    private static final String DEFAULT_KEY_ID2_COLUMN = "";
    private static final String DEFAULT_KEY_ID3_COLUMN = "";
    private final StandardAdapterConfiguration parent;
    private final String sdcId;
    private final String keyId1Column;
    private final String keyId2Column;
    private final String keyId3Column;

    public AccessControlAdapterConfiguration(PropertyList accessControlAdapterProps, StandardAdapterConfiguration parent) {
        if (accessControlAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.sdcId = accessControlAdapterProps.getProperty("sdcid", DEFAULT_SDC_ID);
        this.keyId1Column = accessControlAdapterProps.getProperty("keyid1column", DEFAULT_KEY_ID1_COLUMN);
        this.keyId2Column = accessControlAdapterProps.getProperty("keyid2column", "");
        this.keyId3Column = accessControlAdapterProps.getProperty("keyid3column", "");
        this.parent = parent;
    }

    public String getSdcId() {
        return this.sdcId;
    }

    public String getKeyId1Column() {
        return this.keyId1Column;
    }

    public String getKeyId2Column() {
        return this.keyId2Column;
    }

    public String getKeyId3Column() {
        return this.keyId3Column;
    }

    public StandardAdapterConfiguration getParent() {
        return this.parent;
    }
}

