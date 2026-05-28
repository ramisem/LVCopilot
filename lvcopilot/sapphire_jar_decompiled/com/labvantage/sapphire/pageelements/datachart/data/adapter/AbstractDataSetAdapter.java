/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.DataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public abstract class AbstractDataSetAdapter
extends BaseCustom
implements DataSetAdapter {
    private final PropertyList outputProps;
    private final DataBindingMap dataBindingMap;
    private DataSet processedDataSet;

    public AbstractDataSetAdapter(String connectionId, DataBindingMap dataBindingMap) {
        if (connectionId == null || connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection ID is null or empty: " + connectionId);
        }
        if (dataBindingMap == null) {
            throw new IllegalArgumentException("Data binding map is null");
        }
        this.setConnectionId(connectionId);
        this.processedDataSet = null;
        this.outputProps = new PropertyList();
        this.dataBindingMap = dataBindingMap;
    }

    public PropertyList getOutputProps() {
        return this.outputProps;
    }

    public DataBindingMap getDataBindingMap() {
        return this.dataBindingMap;
    }

    @Override
    public DataSet getProcessedDataSet() {
        if (this.processedDataSet == null) {
            throw new IllegalStateException("Adapter not processed yet.");
        }
        return this.processedDataSet;
    }

    public void setProcessedDataSet(DataSet processedDataSet) {
        if (processedDataSet == null) {
            throw new IllegalArgumentException("Processed data set is null");
        }
        this.processedDataSet = processedDataSet;
    }

    @Override
    public PropertyList getOutputProperties() {
        if (this.processedDataSet == null) {
            throw new IllegalStateException("Adapter not processed yet.");
        }
        return this.outputProps;
    }
}

