/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.data.adapter.AbstractDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.BoundaryConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ExtractBoundariesAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public final class ExtractBoundariesAdapter
extends AbstractDataSetAdapter {
    private final ExtractBoundariesAdapterConfiguration extractBoundariesAdapterConf;

    public ExtractBoundariesAdapter(ExtractBoundariesAdapterConfiguration extractBoundariesAdapterConf, String connectionId, DataBindingMap bindingMap) {
        super(connectionId, bindingMap);
        if (extractBoundariesAdapterConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        this.extractBoundariesAdapterConf = extractBoundariesAdapterConf;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void processDataSetAdapter(DataSet dataSet) throws SapphireException {
        String val;
        String boundaryStr;
        int i;
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        DataSet resultDataSet = new DataSet();
        String seriesIdColumnId = this.extractBoundariesAdapterConf.getSeriesIdColumnId();
        String seriesGroupIdColumnId = this.extractBoundariesAdapterConf.getSeriesGroupIdColumnId();
        String primaryValueColumn = this.extractBoundariesAdapterConf.getPrimaryValueColumnId().evaluate(this.getDataBindingMap());
        boolean addEmptyValues = this.extractBoundariesAdapterConf.addEmptyValues();
        this.sanityChecks(dataSet, seriesIdColumnId, seriesGroupIdColumnId, primaryValueColumn);
        String boundaryKeyColumnId = this.extractBoundariesAdapterConf.getBoundaryKeyColumnId();
        if (boundaryKeyColumnId.isEmpty()) {
            throw new IllegalArgumentException("Main Boundary key column ID is empty");
        }
        if (!dataSet.isValidColumn(boundaryKeyColumnId)) {
            throw new IllegalArgumentException("Main Boundary key column ID is not a valid column in data set: " + boundaryKeyColumnId);
        }
        ArrayList<String> boundaryExtraKeyColumnList = new ArrayList<String>();
        for (ColumnConfiguration columnConfiguration : this.extractBoundariesAdapterConf.getBoundaryExtraKeyColumnConfList()) {
            String string = columnConfiguration.getColumnId();
            if (string.isEmpty()) {
                throw new IllegalArgumentException("Boundary key column ID is empty");
            }
            if (!dataSet.isValidColumn(string)) {
                throw new IllegalArgumentException("Boundary key column ID is not a valid column in data set: " + string);
            }
            boundaryExtraKeyColumnList.add(columnConfiguration.getColumnId());
        }
        ArrayList<String> primaryKeyColumnList = new ArrayList<String>();
        for (ColumnConfiguration columnConfiguration : this.extractBoundariesAdapterConf.getPrimaryKeyColumnConfList()) {
            String primaryKeyColumnId = columnConfiguration.getColumnId();
            if (primaryKeyColumnId.isEmpty()) {
                throw new IllegalArgumentException("Primary key column ID is empty");
            }
            if (!dataSet.isValidColumn(primaryKeyColumnId)) {
                throw new IllegalArgumentException("Primary key column ID is not a valid column in data set: " + primaryKeyColumnId);
            }
            primaryKeyColumnList.add(columnConfiguration.getColumnId());
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (ColumnConfiguration sortColumnConf : this.extractBoundariesAdapterConf.getSortColumnConfList()) {
            String sortColumnId = sortColumnConf.getColumnId();
            if (sortColumnId.isEmpty()) {
                throw new IllegalArgumentException("Sort column ID is empty");
            }
            if (!dataSet.isValidColumn(sortColumnId)) {
                throw new IllegalArgumentException("Sort column ID is not a valid column in data set: " + sortColumnId);
            }
            stringBuilder.append(",").append(sortColumnId);
        }
        List<BoundaryConfiguration> list = this.extractBoundariesAdapterConf.getBoundaryConfList();
        HashSet<String> activeSeriesForKeyVal = new HashSet<String>();
        HashSet<String> activeSpecs = new HashSet<String>();
        HashSet<String> activeSeries = new HashSet<String>();
        String oldKeyVal = "";
        String seriesId = "";
        boolean multiSeriesMode = false;
        for (i = 0; i < dataSet.getRowCount(); ++i) {
            String newSeriesId = dataSet.getString(i, seriesIdColumnId);
            if (seriesId.isEmpty()) {
                seriesId = newSeriesId;
            } else if (!seriesId.equals(newSeriesId)) {
                multiSeriesMode = true;
            }
            boundaryStr = "";
            for (String keyCol : boundaryExtraKeyColumnList) {
                val = dataSet.getString(i, keyCol, "");
                if (val.isEmpty()) continue;
                boundaryStr = boundaryStr + val + " ";
            }
            if (boundaryStr.isEmpty()) continue;
            activeSpecs.add(boundaryStr);
        }
        if (activeSpecs.size() == 0) {
            activeSpecs.add("");
        }
        for (i = 0; i < dataSet.getRowCount(); ++i) {
            void var23_35;
            String keyVal = "";
            for (String string : primaryKeyColumnList) {
                keyVal = keyVal + "|" + dataSet.getString(i, string, "");
            }
            if (!keyVal.equals(oldKeyVal) || keyVal.isEmpty()) {
                resultDataSet.copyRow(dataSet, i, 1);
            }
            this.getDataBindingMap().setActiveRow(i);
            String boundaryId = dataSet.getString(i, boundaryKeyColumnId, "");
            if (boundaryId.isEmpty()) continue;
            seriesId = dataSet.getString(i, seriesIdColumnId);
            boundaryStr = "";
            for (String keyCol : boundaryExtraKeyColumnList) {
                val = dataSet.getString(i, keyCol);
                if (val.isEmpty()) continue;
                boundaryStr = boundaryStr + val + " ";
            }
            String string = "";
            if (multiSeriesMode) {
                String string2 = seriesId + " ";
            }
            if (activeSpecs.size() > 1) {
                void var23_33;
                String string3 = (String)var23_33 + boundaryStr;
            }
            String string4 = (String)var23_35 + boundaryId;
            for (BoundaryConfiguration boundaryConf : list) {
                String boundaryIdCompare = boundaryConf.getBoundaryId().evaluate(this.getDataBindingMap());
                String boundarySeriesId = boundaryConf.getSeriesId().evaluate(this.getDataBindingMap());
                if (boundarySeriesId.isEmpty()) {
                    boundarySeriesId = string4;
                }
                if (!boundaryIdCompare.equals(boundaryId)) continue;
                String boundaryValueColumnId = boundaryConf.getValueColumnId().evaluate(this.getDataBindingMap());
                if (!dataSet.isValidColumn(boundaryValueColumnId)) {
                    throw new IllegalArgumentException("Boundary value column ID is not a valid column in data set: " + boundaryValueColumnId);
                }
                if (boundaryValueColumnId == null || boundaryValueColumnId.length() <= 0) continue;
                String boundaryValue = dataSet.getValue(i, boundaryValueColumnId);
                boolean seriesExists = activeSeries.contains(boundarySeriesId);
                if (!seriesExists && (boundaryValue == null || boundaryValue.length() <= 0)) continue;
                if (!seriesExists) {
                    activeSeries.add(boundarySeriesId);
                }
                activeSeriesForKeyVal.add(boundarySeriesId);
                int rowNum = this.addLimitRowToDataSet(dataSet, resultDataSet, seriesIdColumnId, seriesGroupIdColumnId, i, string4, boundaryConf);
                this.setResult(dataSet, resultDataSet, primaryValueColumn, i, boundaryValueColumnId, boundaryValue, rowNum);
            }
            String nextKeyVal = "";
            if (i + 1 < dataSet.getRowCount()) {
                for (String col3 : primaryKeyColumnList) {
                    nextKeyVal = nextKeyVal + "|" + dataSet.getString(i, col3, "");
                }
            }
            if (addEmptyValues && !keyVal.equals(nextKeyVal)) {
                this.addGapsToSeriesSpec(dataSet, resultDataSet, seriesIdColumnId, seriesGroupIdColumnId, primaryValueColumn, list, activeSeries, activeSeriesForKeyVal, activeSpecs, i, seriesId, multiSeriesMode);
                activeSeriesForKeyVal.clear();
            }
            oldKeyVal = keyVal;
        }
        if (stringBuilder.length() > 0) {
            resultDataSet.sort(stringBuilder.substring(1));
        }
        this.setProcessedDataSet(resultDataSet);
    }

    private void sanityChecks(DataSet dataSet, String seriesIdColumnId, String seriesGroupIdColumnId, String primaryValueColumn) {
        if (seriesIdColumnId.isEmpty()) {
            throw new IllegalArgumentException("Series ID column ID is empty");
        }
        if (!dataSet.isValidColumn(seriesIdColumnId)) {
            throw new IllegalArgumentException("Series ID column ID is not a valid column in data set: " + seriesIdColumnId);
        }
        if (seriesGroupIdColumnId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID column ID is empty");
        }
        if (!dataSet.isValidColumn(seriesGroupIdColumnId)) {
            throw new IllegalArgumentException("Series group ID column ID is not a valid column in data set: " + seriesGroupIdColumnId);
        }
        if (primaryValueColumn.equals("")) {
            throw new IllegalArgumentException("Primary value column ID is empty");
        }
        if (!dataSet.isValidColumn(primaryValueColumn)) {
            throw new IllegalArgumentException("Primary value column ID is not a valid column in data set: " + primaryValueColumn);
        }
    }

    private void setResult(DataSet dataSet, DataSet resultDataSet, String primaryValueColumn, int i, String boundaryValueColumnId, String boundaryValue, int rowNum) {
        if (!boundaryValue.isEmpty()) {
            if (resultDataSet.getColumnType(primaryValueColumn) == 1) {
                if (dataSet.getColumnType(boundaryValueColumnId) == 1) {
                    resultDataSet.setNumber(rowNum, primaryValueColumn, dataSet.getBigDecimal(i, boundaryValueColumnId));
                } else {
                    try {
                        Double d = Double.parseDouble(boundaryValue.replaceAll(",", "."));
                        resultDataSet.setNumber(rowNum, primaryValueColumn, d);
                    }
                    catch (NumberFormatException numberFormatException) {}
                }
            } else if (resultDataSet.getColumnType(primaryValueColumn) == 0) {
                resultDataSet.setValue(rowNum, primaryValueColumn, boundaryValue);
            }
        } else if (resultDataSet.getColumnType(primaryValueColumn) == 1) {
            resultDataSet.setNumber(rowNum, primaryValueColumn, (BigDecimal)null);
        } else if (resultDataSet.getColumnType(primaryValueColumn) == 0) {
            resultDataSet.setValue(rowNum, primaryValueColumn, "(null)");
        }
    }

    private void addGapsToSeriesSpec(DataSet dataSet, DataSet resultDataSet, String seriesIdCol, String seriesGroupIdColumn, String valueCol, List<BoundaryConfiguration> boundaryConfList, Set<String> activeSeries, Set<String> activeSeriesForKeyVal, Set<String> activeSpecs, int i, String seriesId, boolean multiSeriesMode) throws SapphireException {
        for (String specStr : activeSpecs) {
            for (BoundaryConfiguration boundaryConf : boundaryConfList) {
                String boundaryIdCompare = boundaryConf.getBoundaryId().evaluate(this.getDataBindingMap());
                String boundaryIdCompareWithSeriesAndSpec = "";
                if (multiSeriesMode) {
                    boundaryIdCompareWithSeriesAndSpec = seriesId + " ";
                }
                if (activeSpecs.size() > 1) {
                    boundaryIdCompareWithSeriesAndSpec = boundaryIdCompareWithSeriesAndSpec + specStr;
                }
                boundaryIdCompareWithSeriesAndSpec = boundaryIdCompareWithSeriesAndSpec + boundaryIdCompare;
                String boundarySeriesIdCompare = boundaryConf.getSeriesId().evaluate(this.getDataBindingMap());
                if (boundarySeriesIdCompare.isEmpty()) {
                    boundarySeriesIdCompare = boundaryIdCompareWithSeriesAndSpec;
                }
                if (activeSeriesForKeyVal.contains(boundarySeriesIdCompare) || !activeSeries.contains(boundarySeriesIdCompare)) continue;
                int rowNum = this.addLimitRowToDataSet(dataSet, resultDataSet, seriesIdCol, seriesGroupIdColumn, i, boundaryIdCompareWithSeriesAndSpec, boundaryConf);
                if (resultDataSet.getColumnType(valueCol) == 1) {
                    resultDataSet.setNumber(rowNum, valueCol, (BigDecimal)null);
                    continue;
                }
                if (resultDataSet.getColumnType(valueCol) != 0) continue;
                resultDataSet.setValue(rowNum, valueCol, "(null)");
            }
        }
    }

    private int addLimitRowToDataSet(DataSet dataSet, DataSet resultDataSet, String seriesIdCol, String seriesGroupIdColumn, int i, String boundaryIdWithSpec, BoundaryConfiguration boundaryConf) throws SapphireException {
        HashMap row = (HashMap)dataSet.get(i);
        resultDataSet.add(row.clone());
        int rowNum = resultDataSet.getRowCount() - 1;
        String boundarySeriesId = boundaryConf.getSeriesId().evaluate(this.getDataBindingMap());
        if (boundarySeriesId.isEmpty()) {
            boundarySeriesId = boundaryIdWithSpec;
        }
        resultDataSet.setValue(rowNum, seriesIdCol, boundarySeriesId);
        String boundarySeriesGroupId = boundaryConf.getSeriesGroupId().evaluate(this.getDataBindingMap());
        if (boundarySeriesGroupId.isEmpty()) {
            boundarySeriesGroupId = dataSet.getString(i, seriesIdCol);
        }
        resultDataSet.setValue(rowNum, seriesGroupIdColumn, boundarySeriesGroupId);
        return rowNum;
    }
}

