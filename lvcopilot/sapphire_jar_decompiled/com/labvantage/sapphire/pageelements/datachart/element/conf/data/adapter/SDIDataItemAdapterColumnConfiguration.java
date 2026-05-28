/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDIDataItemAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class SDIDataItemAdapterColumnConfiguration
implements Serializable {
    private final SDIDataItemAdapterConfiguration parent;
    private final String tableId;
    private final StringExpression columnId;

    public SDIDataItemAdapterColumnConfiguration(PropertyList columnProps, SDIDataItemAdapterConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (columnProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.tableId = columnProps.getProperty("tableid", "");
        this.columnId = new StringExpression(columnProps.getProperty("columnid", ""));
    }

    public String getTableId() {
        return this.tableId;
    }

    public StringExpression getColumnId() {
        return this.columnId;
    }

    public SDIDataItemAdapterConfiguration getParent() {
        return this.parent;
    }
}

