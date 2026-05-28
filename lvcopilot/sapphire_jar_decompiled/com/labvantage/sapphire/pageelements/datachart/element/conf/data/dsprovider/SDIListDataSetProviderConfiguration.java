/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.StandardDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class SDIListDataSetProviderConfiguration
implements Serializable {
    private final StandardDataSetProviderConfiguration parent;
    private final StringExpression sdcId;
    private final StringExpression keyId1s;
    private final StringExpression keyId2s;
    private final StringExpression keyId3s;

    public SDIListDataSetProviderConfiguration(PropertyList sdiListDataSetProps, StandardDataSetProviderConfiguration parent) {
        if (sdiListDataSetProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.sdcId = new StringExpression(sdiListDataSetProps.getProperty("sdcid"));
        this.keyId1s = new StringExpression(sdiListDataSetProps.getProperty("keyid1"));
        this.keyId2s = new StringExpression(sdiListDataSetProps.getProperty("keyid2"));
        this.keyId3s = new StringExpression(sdiListDataSetProps.getProperty("keyid3"));
        this.parent = parent;
    }

    public StandardDataSetProviderConfiguration getParent() {
        return this.parent;
    }

    public StringExpression getSdcId() {
        return this.sdcId;
    }

    public StringExpression getKeyId1s() {
        return this.keyId1s;
    }

    public StringExpression getKeyId2s() {
        return this.keyId2s;
    }

    public StringExpression getKeyId3s() {
        return this.keyId3s;
    }
}

