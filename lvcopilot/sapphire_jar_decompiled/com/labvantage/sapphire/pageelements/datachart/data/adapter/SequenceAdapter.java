/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.data.adapter.AbstractDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SequenceAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public final class SequenceAdapter
extends AbstractDataSetAdapter {
    private final SequenceAdapterConfiguration sequenceAdapterConf;

    public SequenceAdapter(SequenceAdapterConfiguration sequenceAdapterConf, String connectionId, DataBindingMap bindingMap) {
        super(connectionId, bindingMap);
        if (sequenceAdapterConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        this.sequenceAdapterConf = sequenceAdapterConf;
    }

    @Override
    public void processDataSetAdapter(DataSet dataSet) throws SapphireException {
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        String sequenceColumnId = this.sequenceAdapterConf.getSequenceColumnId().evaluate(this.getDataBindingMap());
        if (dataSet.isValidColumn(sequenceColumnId)) {
            throw new IllegalArgumentException("Sequence column already exists in the data set: " + sequenceColumnId);
        }
        ArrayList<String> sequenceKeyColumnList = new ArrayList<String>();
        for (ColumnConfiguration sequenceKeyColumnConf : this.sequenceAdapterConf.getSequenceKeyColumnConfList()) {
            String sequenceKeyColumnId = sequenceKeyColumnConf.getColumnId();
            if (sequenceKeyColumnId.isEmpty()) {
                throw new IllegalArgumentException("Sequence key column ID is empty");
            }
            if (!dataSet.isValidColumn(sequenceKeyColumnId)) {
                throw new IllegalArgumentException("Sequence key column ID is not a valid column in data set: " + sequenceKeyColumnId);
            }
            sequenceKeyColumnList.add(sequenceKeyColumnConf.getColumnId());
        }
        String oldSequenceKeyVal = "";
        int uniqueKeyIdSequence = 0;
        for (int i = 0; i < dataSet.getRowCount(); ++i) {
            String sequenceKeyVal = "";
            for (String col : sequenceKeyColumnList) {
                sequenceKeyVal = sequenceKeyVal + "|" + dataSet.getValue(i, col, "");
            }
            if (!sequenceKeyVal.equals(oldSequenceKeyVal) || sequenceKeyVal.isEmpty()) {
                ++uniqueKeyIdSequence;
                if (i == 0) {
                    dataSet.addColumn(sequenceColumnId, 1);
                }
            }
            dataSet.setNumber(i, sequenceColumnId, uniqueKeyIdSequence);
            oldSequenceKeyVal = sequenceKeyVal;
        }
        this.setProcessedDataSet(dataSet);
    }
}

