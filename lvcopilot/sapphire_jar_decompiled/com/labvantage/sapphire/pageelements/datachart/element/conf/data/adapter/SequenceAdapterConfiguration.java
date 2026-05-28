/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class SequenceAdapterConfiguration
implements Serializable {
    private final StandardAdapterConfiguration parent;
    private final List<ColumnConfiguration> sequenceKeyColumnConfList;
    private final StringExpression sequenceColumnId;

    public SequenceAdapterConfiguration(PropertyList sequenceAdapterProps, StandardAdapterConfiguration parent) {
        if (sequenceAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.sequenceColumnId = new StringExpression(sequenceAdapterProps.getProperty("sequencecolumnid"));
        this.sequenceKeyColumnConfList = new ArrayList<ColumnConfiguration>();
        PropertyListCollection sequenceKeyColumnsCollection = sequenceAdapterProps.getCollectionNotNull("sequencekeycolumncollection");
        for (int i = 0; i < sequenceKeyColumnsCollection.size(); ++i) {
            PropertyList rowNumberKeyColumnProps = sequenceKeyColumnsCollection.getPropertyList(i);
            this.sequenceKeyColumnConfList.add(new ColumnConfiguration(rowNumberKeyColumnProps));
        }
    }

    public List<ColumnConfiguration> getSequenceKeyColumnConfList() {
        return this.sequenceKeyColumnConfList;
    }

    public StandardAdapterConfiguration getParent() {
        return this.parent;
    }

    public StringExpression getSequenceColumnId() {
        return this.sequenceColumnId;
    }
}

