/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDIDataItemAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class SDIWorkItemFilterConfiguration
implements Serializable {
    private final SDIDataItemAdapterConfiguration parent;
    private final StringExpression workItemId;
    private final StringExpression workItemVersionId;
    private final StringExpression workItemInstance;

    public SDIWorkItemFilterConfiguration(PropertyList sdiWorkItemFilterProps, SDIDataItemAdapterConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (sdiWorkItemFilterProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.workItemId = new StringExpression(sdiWorkItemFilterProps.getProperty("workitemid", ""));
        this.workItemVersionId = new StringExpression(sdiWorkItemFilterProps.getProperty("workitemversionid", ""));
        this.workItemInstance = new StringExpression(sdiWorkItemFilterProps.getProperty("workiteminstance", ""));
    }

    public StringExpression getWorkItemId() {
        return this.workItemId;
    }

    public StringExpression getWorkItemVersionId() {
        return this.workItemVersionId;
    }

    public StringExpression getWorkItemInstance() {
        return this.workItemInstance;
    }

    public SDIDataItemAdapterConfiguration getParent() {
        return this.parent;
    }
}

