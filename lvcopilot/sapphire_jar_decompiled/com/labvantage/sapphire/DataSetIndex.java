/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.util.DataSet;

public class DataSetIndex
implements Serializable {
    private static final int DEFAULT_MIN_SIZE_FOR_INDEXING = 50;
    private static final int UPDATE_PENDING = -1;
    private static final boolean DEFAULT_AUTO_INDEXING = false;
    private final Map<Set<String>, Integer> lastIndexedRows;
    private final Set<Set<String>> indexIds;
    private final Set<Set<String>> indexUpdatePermissions;
    private final Set<String> indexedColumns;
    private final Map<Set<String>, Map<HashMap, List<Integer>>> index;
    private final DataSet dataSet;
    private final boolean isColumnCaseSensitive;
    private boolean autoIndexing;
    private int minSizeForIndexing;

    public DataSetIndex(DataSet dataSet) {
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        this.dataSet = dataSet;
        this.index = new HashMap<Set<String>, Map<HashMap, List<Integer>>>();
        this.lastIndexedRows = new HashMap<Set<String>, Integer>();
        this.indexIds = new HashSet<Set<String>>();
        this.indexUpdatePermissions = new HashSet<Set<String>>();
        this.indexedColumns = new HashSet<String>();
        this.isColumnCaseSensitive = dataSet.getColidCaseSensitive();
        this.minSizeForIndexing = 50;
        this.autoIndexing = false;
    }

    public DataSetIndex copy(DataSet dataSet) {
        if (dataSet == null) {
            throw new IllegalArgumentException("Data set is null");
        }
        DataSetIndex copy = new DataSetIndex(dataSet);
        if (!this.getIndexIds().isEmpty()) {
            for (Set<String> columnIndex : this.getIndexIds()) {
                copy.createIndex(columnIndex);
            }
        }
        copy.setMinSizeForIndexing(this.getMinimumSizeForIndexing());
        copy.setAutoIndexing(this.isAutoIndexing());
        return copy;
    }

    public void createIndex(String columnIds) {
        if (columnIds == null) {
            throw new IllegalArgumentException("Column is null");
        }
        HashSet<String> columnIdSet = new HashSet<String>(Arrays.asList(columnIds.split(";")));
        for (String columnId : columnIdSet) {
            if (this.dataSet.isValidColumn(columnId)) continue;
            throw new IllegalArgumentException("Column " + columnId + " is not a valid column");
        }
        this.createIndex(columnIdSet);
    }

    public void createIndex(Set<String> columnIds) {
        if (columnIds == null) {
            throw new IllegalArgumentException("Columns is null");
        }
        if (columnIds.isEmpty()) {
            throw new IllegalArgumentException("Columns is empty");
        }
        for (String column : columnIds) {
            if (column == null) {
                throw new IllegalArgumentException("Column is null");
            }
            if (this.dataSet.isValidColumn(column)) continue;
            throw new IllegalArgumentException("Column " + column + " is not a valid column");
        }
        this.toCaseAwareSet(columnIds);
        if (!this.indexIds.contains(columnIds)) {
            this.indexIds.add(columnIds);
            this.lastIndexedRows.put(columnIds, -1);
            this.indexedColumns.addAll(columnIds);
        }
    }

    public Set<Set<String>> getIndexIds() {
        return this.indexIds;
    }

    public boolean isIndexing(String columnId) {
        if (columnId == null) {
            throw new IllegalArgumentException("Column is null");
        }
        return this.getIndexedColumns().contains(this.getCaseAwareString(columnId));
    }

    public Set<String> getIndexedColumns() {
        HashSet<String> allIndexedColumns = new HashSet<String>();
        allIndexedColumns.addAll(this.indexedColumns);
        if (this.autoIndexing) {
            allIndexedColumns.addAll(this.dataSet.getKeyColumnSet());
        }
        return allIndexedColumns;
    }

    public void deleteIndex(Set<String> indexId) {
        if (this.isIndexing()) {
            if (indexId == null) {
                throw new IllegalArgumentException("Columns is null");
            }
            this.toCaseAwareSet(indexId);
            this.indexIds.remove(indexId);
            this.lastIndexedRows.remove(indexId);
            this.index.remove(indexId);
        }
        this.indexedColumns.clear();
        for (Set<String> existingIndexId : this.indexIds) {
            for (String columnId : existingIndexId) {
                this.indexedColumns.add(columnId);
            }
        }
    }

    public void deleteAllIndexes() {
        this.index.clear();
        this.lastIndexedRows.clear();
        this.indexIds.clear();
        this.indexedColumns.clear();
    }

    public void weakProtectIndex(String columnId, int row) {
        if (columnId == null) {
            throw new IllegalArgumentException("Column is null");
        }
        if (row < 0) {
            throw new IllegalArgumentException("Row is negative");
        }
        Set<Set<String>> applicableIndexIds = this.getApplicableIndexIds(columnId);
        for (Set<String> indexId : applicableIndexIds) {
            int lastIndexedRow = this.lastIndexedRows.get(indexId);
            if (lastIndexedRow < row) continue;
            throw new IllegalStateException("Cannot modify value that has already been indexed");
        }
    }

    public void renameIndexedColumn(String oldColumnId, String newColumnId) {
        if (this.isIndexing()) {
            String newCaseAwareColumnId;
            if (oldColumnId == null) {
                throw new IllegalArgumentException("Column is null");
            }
            if (newColumnId == null) {
                throw new IllegalArgumentException("New column is null");
            }
            String oldCaseAwareColumnId = this.getCaseAwareString(oldColumnId);
            if (!oldCaseAwareColumnId.equals(newCaseAwareColumnId = this.getCaseAwareString(newColumnId))) {
                Set<Set<String>> applicableIndexIds = this.getApplicableIndexIds(oldColumnId);
                for (Set<String> applicableIndexId : applicableIndexIds) {
                    this.index.remove(applicableIndexId);
                    this.indexIds.remove(applicableIndexId);
                    HashSet<String> newIndexId = new HashSet<String>();
                    newIndexId.addAll(applicableIndexId);
                    newIndexId.remove(oldCaseAwareColumnId);
                    newIndexId.add(newCaseAwareColumnId);
                    this.indexIds.add(newIndexId);
                    this.lastIndexedRows.remove(applicableIndexId);
                    this.lastIndexedRows.put(newIndexId, -1);
                }
            }
            this.indexedColumns.add(newCaseAwareColumnId);
            this.indexedColumns.remove(oldCaseAwareColumnId);
        }
    }

    public void flushAllIndexes() {
        this.index.clear();
        for (Set<String> indexId : this.lastIndexedRows.keySet()) {
            this.lastIndexedRows.put(indexId, -1);
        }
        this.indexUpdatePermissions.clear();
    }

    public void flushIndex(String columnId) {
        if (columnId == null) {
            throw new IllegalArgumentException("Column is null");
        }
        for (Set<String> indexId : this.indexIds) {
            if (!indexId.contains(columnId)) continue;
            Map<HashMap, List<Integer>> dataSetIndex = this.index.get(indexId);
            if (dataSetIndex != null) {
                dataSetIndex.clear();
            }
            this.indexUpdatePermissions.remove(indexId);
        }
        for (Set<String> indexId : this.lastIndexedRows.keySet()) {
            if (!indexId.contains(columnId)) continue;
            this.lastIndexedRows.put(indexId, -1);
        }
    }

    public boolean useIndex(HashMap filter) {
        Set<String> indexId;
        if (filter == null) {
            throw new IllegalArgumentException("Filter is null");
        }
        boolean useIndex = false;
        Set columnIds = filter.keySet();
        Iterator<Object> iterator = this.indexIds.iterator();
        while (iterator.hasNext() && !(useIndex = columnIds.containsAll(indexId = iterator.next()))) {
        }
        if (!useIndex && this.autoIndexing) {
            for (String columnId : this.dataSet.getKeyColumnSet()) {
                if (!columnIds.contains(columnId)) continue;
                useIndex = true;
                break;
            }
        }
        return useIndex;
    }

    public boolean isIndexing() {
        return !this.indexedColumns.isEmpty() || this.autoIndexing;
    }

    public DataSet getFilteredDataSet(HashMap filter) {
        DataSet ds = new DataSet();
        if (filter == null) {
            throw new IllegalArgumentException("Filter is null");
        }
        ArrayList<Integer> mostValuableIndexRows = new ArrayList<Integer>();
        Set<String> mostValuableIndex = this.findMostValuableIndex(mostValuableIndexRows, filter);
        HashMap tempFilter = new HashMap(filter);
        if (mostValuableIndex != null && !mostValuableIndexRows.isEmpty()) {
            for (String columnId : mostValuableIndex) {
                tempFilter.remove(columnId);
            }
            for (Integer filteredRow : mostValuableIndexRows) {
                ds.add(this.dataSet.get(filteredRow));
            }
        } else if (mostValuableIndex != null && mostValuableIndexRows.isEmpty()) {
            for (String columnId : mostValuableIndex) {
                tempFilter.remove(columnId);
            }
        } else if (mostValuableIndex == null) {
            for (String row : this.dataSet) {
                ds.add(row);
            }
        }
        if (tempFilter.size() > 0) {
            Set nonIndexedFilterEntrySet = tempFilter.entrySet();
            DataSet tempDataSet = new DataSet();
            for (Object rowObject : ds) {
                HashMap row = (HashMap)rowObject;
                Set rowEntrySet = row.entrySet();
                if (!rowEntrySet.containsAll(nonIndexedFilterEntrySet)) continue;
                tempDataSet.add(row);
            }
            ds = tempDataSet;
        }
        return ds;
    }

    public int getMinimumSizeForIndexing() {
        return this.minSizeForIndexing;
    }

    public void setMinSizeForIndexing(int minSizeForIndexing) {
        if (minSizeForIndexing < 0) {
            throw new IllegalArgumentException("Minimum size for indexing is negative");
        }
        this.minSizeForIndexing = minSizeForIndexing;
    }

    public int findRow(HashMap findMap) {
        int rowNumber = -1;
        DataSet ds = new DataSet();
        if (findMap == null) {
            throw new IllegalArgumentException("Find map is null");
        }
        ArrayList<Integer> mostValuableIndexRows = new ArrayList<Integer>();
        Set<String> mostValuableIndexId = this.findMostValuableIndex(mostValuableIndexRows, findMap);
        HashMap tempFindMap = new HashMap(findMap);
        HashMap<Integer, Integer> rowMap = new HashMap<Integer, Integer>();
        if (mostValuableIndexId != null && !mostValuableIndexRows.isEmpty()) {
            rowNumber = (Integer)mostValuableIndexRows.get(0);
            for (String columnId : mostValuableIndexId) {
                tempFindMap.remove(columnId);
            }
            for (Integer filteredRow : mostValuableIndexRows) {
                ds.add(this.dataSet.get(filteredRow));
                rowMap.put(ds.size() - 1, filteredRow);
            }
        } else if (mostValuableIndexId != null && mostValuableIndexRows.isEmpty()) {
            for (String columnId : mostValuableIndexId) {
                tempFindMap.remove(columnId);
            }
        } else if (mostValuableIndexId == null) {
            for (int i = 0; i < this.dataSet.size(); ++i) {
                ds.add(this.dataSet.get(i));
                rowMap.put(i, i);
            }
        }
        if (tempFindMap.size() > 0) {
            Set tempFindMapEntrySet = tempFindMap.entrySet();
            int tempRowNumber = -1;
            for (int i = 0; i < ds.size() && tempRowNumber < 0; ++i) {
                HashMap row = (HashMap)ds.get(i);
                Set rowEntrySet = row.entrySet();
                if (!rowEntrySet.containsAll(tempFindMapEntrySet)) continue;
                tempRowNumber = (Integer)rowMap.get(i);
            }
            rowNumber = tempRowNumber;
        }
        return rowNumber;
    }

    private Set<String> findMostValuableIndex(List<Integer> indexRows, HashMap filter) {
        List<Object> mostValuableIndexRows;
        Set<String> mostValuableIndexId;
        block6: {
            block5: {
                if (indexRows == null) {
                    throw new IllegalArgumentException("Index rows is null");
                }
                if (filter == null) {
                    throw new IllegalArgumentException("Find map is null");
                }
                mostValuableIndexId = null;
                mostValuableIndexRows = new ArrayList();
                if (!this.autoIndexing) break block5;
                Set<String> keyIndexId = this.getKeyIndexId(filter);
                if (!keyIndexId.isEmpty() && !this.indexIds.contains(keyIndexId)) {
                    this.createIndex(keyIndexId);
                }
                this.updateIndex(keyIndexId);
                Map<HashMap, List<Integer>> dataSetIndex = this.index.get(keyIndexId);
                if (dataSetIndex == null) break block6;
                mostValuableIndexId = keyIndexId;
                HashMap dataSetRowData = this.narrowDown(keyIndexId, filter, false);
                List<Integer> dataSetRows = dataSetIndex.get(dataSetRowData);
                mostValuableIndexRows = dataSetRows != null ? dataSetRows : new ArrayList();
                break block6;
            }
            Set<Set<String>> applicableIndexIds = this.getApplicableIndexIds(filter);
            this.updateIndexes(applicableIndexIds);
            for (Set<String> indexId : applicableIndexIds) {
                Map<HashMap, List<Integer>> dataSetIndex = this.index.get(indexId);
                if (dataSetIndex == null) continue;
                HashMap dataSetRowData = this.narrowDown(indexId, filter, false);
                List<Integer> dataSetRows = dataSetIndex.get(dataSetRowData);
                if (dataSetRows != null && (mostValuableIndexRows.isEmpty() || dataSetRows.size() < mostValuableIndexRows.size())) {
                    mostValuableIndexRows = dataSetRows;
                    mostValuableIndexId = indexId;
                }
                if (!mostValuableIndexRows.isEmpty()) continue;
                break;
            }
        }
        indexRows.addAll(mostValuableIndexRows);
        return mostValuableIndexId;
    }

    private void updateIndex(Set<String> indexId) {
        HashSet<Set<String>> indexIds = new HashSet<Set<String>>();
        indexIds.add(indexId);
        this.updateIndexes(indexIds);
    }

    private Set<String> getKeyIndexId(HashMap filter) {
        HashSet<String> indexId = new HashSet<String>();
        for (String columnId : this.dataSet.getKeyColumnSet()) {
            if (!filter.containsKey(columnId)) continue;
            indexId.add(columnId);
        }
        return indexId;
    }

    private Set<Set<String>> getApplicableIndexIds(String columnId) {
        if (columnId == null) {
            throw new IllegalArgumentException("Column is null");
        }
        HashSet<Set<String>> applicableIndexIds = new HashSet<Set<String>>();
        for (Set<String> indexId : this.indexIds) {
            if (!indexId.contains(columnId)) continue;
            applicableIndexIds.add(indexId);
        }
        return applicableIndexIds;
    }

    private void toCaseAwareSet(Set<String> set) {
        if (!this.isColumnCaseSensitive) {
            String[] array = set.toArray(new String[set.size()]);
            for (int i = 0; i < array.length; ++i) {
                array[i] = array[i].toLowerCase();
            }
            set.clear();
            set.addAll(Arrays.asList(array));
        }
    }

    private HashMap narrowDown(Set<String> columnIds, HashMap source, boolean isSourceCaseAware) {
        if (columnIds == null) {
            throw new IllegalArgumentException("Columns is null");
        }
        if (source == null) {
            throw new IllegalArgumentException("Source is null");
        }
        HashMap narrowedDown = new HashMap();
        for (Map.Entry entryObject : source.entrySet()) {
            if (entryObject == null) continue;
            Map.Entry entry = entryObject;
            String columnId = !isSourceCaseAware && !this.isColumnCaseSensitive ? entry.getKey().toString().toLowerCase() : entry.getKey().toString();
            if (!columnIds.contains(columnId)) continue;
            narrowedDown.put(columnId, entry.getValue());
        }
        return narrowedDown;
    }

    private Set<Set<String>> getApplicableIndexIds(HashMap filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter is null");
        }
        HashSet<String> columnIds = new HashSet<String>();
        for (Object columnId : filter.keySet()) {
            if (columnId == null) continue;
            columnIds.add(columnId.toString());
        }
        return this.getApplicableIndexIds(columnIds);
    }

    private String getCaseAwareString(String s) {
        return this.isColumnCaseSensitive ? s : s.toLowerCase();
    }

    private void updateIndexes(Set<Set<String>> updateIndexIds) {
        if (updateIndexIds == null) {
            throw new IllegalArgumentException("Index ids is null");
        }
        int dataSetSize = this.dataSet.size();
        if (!updateIndexIds.isEmpty() && dataSetSize > this.minSizeForIndexing) {
            int startIndexingRow = dataSetSize;
            HashSet<Set<String>> dirtyIndexIds = new HashSet<Set<String>>();
            for (Set<String> set : updateIndexIds) {
                if (this.indexUpdatePermissions.contains(set)) {
                    int lastIndexedRow = this.lastIndexedRows.get(set);
                    if (this.index.containsKey(set) && lastIndexedRow + 1 >= dataSetSize) continue;
                    dirtyIndexIds.add(set);
                    if (lastIndexedRow >= startIndexingRow) continue;
                    startIndexingRow = lastIndexedRow == -1 ? 0 : lastIndexedRow + 1;
                    continue;
                }
                this.indexUpdatePermissions.add(set);
            }
            if (!dirtyIndexIds.isEmpty() && startIndexingRow < dataSetSize) {
                for (int i = startIndexingRow; i < dataSetSize; ++i) {
                    HashMap hashMap = (HashMap)this.dataSet.get(i);
                    for (Set indexId : dirtyIndexIds) {
                        HashMap dataSetRowData;
                        List<Integer> dataSetRows;
                        Map<HashMap, List<Integer>> dataSetIndex = this.index.get(indexId);
                        if (dataSetIndex == null) {
                            dataSetIndex = new HashMap<HashMap, List<Integer>>();
                            this.index.put(indexId, dataSetIndex);
                        }
                        if ((dataSetRows = dataSetIndex.get(dataSetRowData = this.narrowDown(indexId, hashMap, true))) == null) {
                            dataSetRows = new ArrayList<Integer>();
                            dataSetIndex.put(dataSetRowData, dataSetRows);
                        }
                        dataSetRows.add(i);
                    }
                }
                for (Set set : dirtyIndexIds) {
                    this.lastIndexedRows.put(set, dataSetSize - 1);
                }
            }
        }
    }

    private Set<Set<String>> getApplicableIndexIds(Set<String> columnIds) {
        if (columnIds == null) {
            throw new IllegalArgumentException("Columns is null");
        }
        HashSet<Set<String>> applicableIndexIds = new HashSet<Set<String>>();
        for (Set<String> indexId : this.indexIds) {
            if (!columnIds.containsAll(indexId)) continue;
            applicableIndexIds.add(indexId);
        }
        return applicableIndexIds;
    }

    public boolean isAutoIndexing() {
        return this.autoIndexing;
    }

    public void setAutoIndexing(boolean autoIndexing) {
        this.autoIndexing = autoIndexing;
    }
}

