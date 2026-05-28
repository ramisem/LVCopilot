/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.data.adapter.AbstractDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfigurationAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.PseudoColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import com.labvantage.sapphire.tagext.SDITagUtil;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;

public final class ColumnConfigurationAdapter
extends AbstractDataSetAdapter {
    private final ColumnConfigurationAdapterConfiguration adapterConf;

    public ColumnConfigurationAdapter(String connectionId, DataBindingMap dataBindingMap, ColumnConfigurationAdapterConfiguration adapterConfig) {
        super(connectionId, dataBindingMap);
        if (adapterConfig == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        this.adapterConf = adapterConfig;
    }

    @Override
    public void processDataSetAdapter(DataSet dataSet) {
        DataBindingMap bindingMap = this.getDataBindingMap();
        TranslationProcessor translationProcessor = this.getTranslationProcessor();
        for (PseudoColumnConfiguration pseudoColumnConf : this.adapterConf.getPseudoColumnConfigurationList()) {
            String pseudoColumnId = pseudoColumnConf.getColumnId();
            if (pseudoColumnId.isEmpty()) {
                throw new IllegalArgumentException("Empty pseudo column ID");
            }
            if (dataSet.isValidColumn(pseudoColumnId)) {
                throw new IllegalArgumentException("Column already exists in the data set: " + pseudoColumnId);
            }
            dataSet.addColumn(pseudoColumnId, pseudoColumnConf.getColumnType().getDataType());
        }
        for (int i = 0; i < dataSet.getRowCount(); ++i) {
            bindingMap.setActiveRow(i);
            for (PseudoColumnConfiguration pseudoColumnConf : this.adapterConf.getPseudoColumnConfigurationList()) {
                String pseudoColumnValue;
                String pseudoColumnId = pseudoColumnConf.getColumnId();
                try {
                    pseudoColumnValue = pseudoColumnConf.getExpression().evaluate(bindingMap);
                }
                catch (SapphireException e) {
                    throw new IllegalArgumentException("Pseudo column expression could not be evaluated: " + pseudoColumnId, e);
                }
                dataSet.setValue(i, pseudoColumnId, pseudoColumnValue);
            }
            for (ColumnConfiguration columnConf : this.adapterConf.getColumnConfigurationList()) {
                String columnId = columnConf.getColumnId();
                String displayValueRule = columnConf.getDisplayValueRule();
                boolean translate = columnConf.translate();
                if (!dataSet.isValidColumn(columnId)) continue;
                String originalColumnValue = dataSet.getValue(i, columnId, "");
                String columnValue = originalColumnValue;
                if (!columnValue.isEmpty() && !displayValueRule.isEmpty()) {
                    columnValue = SDITagUtil.getDisplayValue(columnValue, displayValueRule);
                }
                if (!columnValue.isEmpty() && translate) {
                    columnValue = translationProcessor.translate(columnValue);
                }
                if (columnValue.equals(originalColumnValue)) continue;
                dataSet.setValue(i, columnId, columnValue);
            }
        }
        this.setProcessedDataSet(dataSet);
    }
}

