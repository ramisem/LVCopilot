/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.simplespec.util;

import com.labvantage.sapphire.BaseCustom;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SimpleSpecHelper
extends BaseCustom {
    public SimpleSpecHelper(String connectionId) throws SapphireException {
        if (connectionId == null) {
            throw new SapphireException("Connection ID is null");
        }
        if (connectionId.isEmpty()) {
            throw new SapphireException("Connection ID is empty");
        }
        this.setConnectionId(connectionId);
    }

    public List<String> getActualParamListVersionIdList(List<String> paramListIdList, List<String> paramListVersionIdList, List<String> variantIdList) throws SapphireException {
        if (paramListIdList == null) {
            throw new SapphireException("Parameter list ID list is null");
        }
        if (paramListVersionIdList == null) {
            throw new SapphireException("Parameter list version ID list is null");
        }
        if (variantIdList == null) {
            throw new SapphireException("Variant ID list is null");
        }
        ArrayList<String> actualParamListVersionIdList = new ArrayList();
        StringBuilder getVersionInfoWhereFragment = new StringBuilder();
        ArrayList<String> getVersionInfoParams = new ArrayList<String>();
        for (int i = 0; i < paramListVersionIdList.size(); ++i) {
            String paramListId = paramListIdList.get(i);
            String paramListVersionId = paramListVersionIdList.get(i);
            String variantId = variantIdList.get(i);
            if (paramListVersionId.isEmpty() || !paramListVersionId.equals("C")) continue;
            getVersionInfoWhereFragment.append(" or (paramlistid = ? AND variantid = ?)");
            getVersionInfoParams.add(paramListId);
            getVersionInfoParams.add(variantId);
        }
        if (!getVersionInfoParams.isEmpty()) {
            String getVersionInfoSql = "SELECT paramlistid, variantid, paramlistversionid, versionstatus FROM paramlist WHERE " + getVersionInfoWhereFragment.substring(4) + " ORDER BY paramlistid, variantid, CAST(paramlistversionid AS int), versionstatus";
            DataSet getVersionInfoDs = this.getQueryProcessor().getPreparedSqlDataSet(getVersionInfoSql, getVersionInfoParams.toArray());
            for (int i = 0; i < paramListVersionIdList.size(); ++i) {
                String paramListId = paramListIdList.get(i);
                String paramListVersionId = paramListVersionIdList.get(i);
                String variantId = variantIdList.get(i);
                if (paramListVersionId.equals("C")) {
                    HashMap<String, String> currentFilter = new HashMap<String, String>();
                    currentFilter.put("paramlistid", paramListId);
                    currentFilter.put("variantid", variantId);
                    currentFilter.put("versionstatus", "C");
                    DataSet filteredCurrentDs = getVersionInfoDs.getFilteredDataSet(currentFilter);
                    if (filteredCurrentDs.getRowCount() > 0) {
                        actualParamListVersionIdList.add(filteredCurrentDs.getString(0, "paramlistversionid"));
                        continue;
                    }
                    HashMap<String, String> provisionalFilter = new HashMap<String, String>();
                    provisionalFilter.put("paramlistid", paramListId);
                    provisionalFilter.put("variantid", variantId);
                    provisionalFilter.put("versionstatus", "P");
                    DataSet filteredProvisionalDs = getVersionInfoDs.getFilteredDataSet(provisionalFilter);
                    if (filteredProvisionalDs.getRowCount() <= 0) continue;
                    actualParamListVersionIdList.add(filteredProvisionalDs.getString(filteredProvisionalDs.getRowCount() - 1, "paramlistversionid"));
                    continue;
                }
                actualParamListVersionIdList.add(paramListVersionId);
            }
        } else {
            actualParamListVersionIdList = new ArrayList<String>(paramListVersionIdList);
        }
        return actualParamListVersionIdList;
    }

    public DataSet mergeSDIWorkItemsWithWorkItemItems(DataSet destination, DataSet source) throws SapphireException {
        if (destination == null) {
            throw new SapphireException("Destination data set is null");
        }
        if (source == null) {
            throw new IllegalArgumentException("Source data set is null");
        }
        return this.merge(destination, source, Collections.singletonList("workitemid"), "workitemversionid", "versionstatus", Collections.singletonList("workitemsequence"));
    }

    public DataSet mergeWorkItemsWithParamItems(DataSet destination, DataSet source) throws SapphireException {
        if (destination == null) {
            throw new SapphireException("Destination data set is null");
        }
        if (source == null) {
            throw new IllegalArgumentException("Source data set is null");
        }
        return this.merge(destination, source, Arrays.asList("paramlistid", "variantid"), "paramlistversionid", "versionstatus", new ArrayList<String>());
    }

    private DataSet merge(DataSet destination, DataSet source, List<String> keyIdColumnList, String versionIdColumn, String versionStatusColumn, List<String> noCopyColumnList) {
        DataSet returnDs = new DataSet(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        if (source.getRowCount() == 0) {
            returnDs = destination;
        } else {
            for (int i = 0; i < destination.getRowCount(); ++i) {
                int j;
                ArrayList<String> keyIdList = new ArrayList<String>();
                for (String keyColumn : keyIdColumnList) {
                    keyIdList.add(destination.getString(i, keyColumn));
                }
                String versionId = destination.getString(i, versionIdColumn);
                HashMap<String, String> findByVersionIdMap = new HashMap<String, String>();
                findByVersionIdMap.put(versionIdColumn, versionId);
                for (int j2 = 0; j2 < keyIdColumnList.size(); ++j2) {
                    findByVersionIdMap.put(keyIdColumnList.get(j2), (String)keyIdList.get(j2));
                }
                DataSet filteredDs = source.getFilteredDataSet(findByVersionIdMap);
                if (filteredDs.getRowCount() == 0) {
                    HashMap<String, String> findByVersionStatusMap = new HashMap<String, String>();
                    findByVersionStatusMap.put(versionStatusColumn, "C");
                    for (j = 0; j < keyIdColumnList.size(); ++j) {
                        findByVersionStatusMap.put(keyIdColumnList.get(j), (String)keyIdList.get(j));
                    }
                    filteredDs = source.getFilteredDataSet(findByVersionStatusMap);
                }
                if (filteredDs.getRowCount() == 0) {
                    HashMap<String, Object> provisionalFilter = new HashMap<String, Object>();
                    for (j = 0; j < keyIdColumnList.size(); ++j) {
                        provisionalFilter.put(keyIdColumnList.get(j), keyIdList.get(j));
                    }
                    provisionalFilter.put(versionStatusColumn, "P");
                    DataSet filteredProvisionalDs = source.getFilteredDataSet(provisionalFilter);
                    HashMap<String, String> findByLatestProvisionalVersionIdStatusMap = new HashMap<String, String>();
                    findByLatestProvisionalVersionIdStatusMap.put(versionIdColumn, filteredProvisionalDs.getString(filteredProvisionalDs.getRowCount() - 1, versionIdColumn));
                    for (int j3 = 0; j3 < keyIdColumnList.size(); ++j3) {
                        findByLatestProvisionalVersionIdStatusMap.put(keyIdColumnList.get(j3), (String)keyIdList.get(j3));
                    }
                    filteredDs = source.getFilteredDataSet(findByLatestProvisionalVersionIdStatusMap);
                }
                if (filteredDs.getRowCount() == 0) {
                    returnDs.copyRow(destination, i, 1);
                    continue;
                }
                for (int j4 = 0; j4 < filteredDs.getRowCount(); ++j4) {
                    returnDs.copyRow(destination, i, 1);
                    int row = returnDs.getRowCount() - 1;
                    for (int k = 0; k < filteredDs.getColumnCount(); ++k) {
                        Object value;
                        String columnId = filteredDs.getColumnId(k);
                        if (noCopyColumnList.contains(columnId)) continue;
                        int columnType = filteredDs.getColumnType(columnId);
                        if (!returnDs.isValidColumn(columnId)) {
                            returnDs.addColumn(columnId, columnType);
                        }
                        if (columnType == 0) {
                            value = filteredDs.getString(j4, columnId);
                            returnDs.setString(row, columnId, (String)value);
                            continue;
                        }
                        if (columnType == 1) {
                            value = filteredDs.getBigDecimal(j4, columnId);
                            returnDs.setNumber(row, columnId, (BigDecimal)value);
                            continue;
                        }
                        if (columnType == 2) {
                            value = filteredDs.getCalendar(j4, columnId);
                            returnDs.setDate(row, columnId, (Calendar)value);
                            continue;
                        }
                        value = filteredDs.getValue(j4, columnId);
                        returnDs.setValue(row, columnId, (String)value);
                    }
                }
            }
        }
        return returnDs;
    }

    public void completeConfiguredColumns(PropertyListCollection configuredColumnCollection, String tableName) throws SapphireException {
        if (configuredColumnCollection == null) {
            throw new SapphireException("Column collection is null");
        }
        if (tableName == null || tableName.isEmpty()) {
            throw new SapphireException("Table name is null or empty");
        }
        for (int i = 0; i < configuredColumnCollection.size(); ++i) {
            PropertyList columnProps = configuredColumnCollection.getPropertyList(i);
            String columnId = columnProps.getProperty("columnid");
            if (columnId.isEmpty()) continue;
            if (columnId.startsWith("(")) {
                columnProps.setProperty("columnalias", columnId.substring(columnId.lastIndexOf(41) + 1).trim());
                columnProps.setProperty("sql", columnId);
                continue;
            }
            columnProps.setProperty("columnalias", columnId);
            columnProps.setProperty("sql", tableName + "." + columnId);
        }
    }

    public DataSet getParamItems(List<String> paramListIdList, List<String> paramListVersionIdList, List<String> variantIdList, PropertyListCollection paramListItemColumnCollection, String filterParamType) throws SapphireException {
        if (paramListIdList == null) {
            throw new SapphireException("Param list ID list is null");
        }
        if (paramListVersionIdList == null) {
            throw new SapphireException("Param list version ID list is null");
        }
        if (variantIdList == null) {
            throw new SapphireException("Variant ID list is null");
        }
        if (paramListItemColumnCollection == null) {
            throw new SapphireException("Param list item column collection is null");
        }
        if (filterParamType == null) {
            throw new SapphireException("Filter param type is null");
        }
        this.completeConfiguredColumns(paramListItemColumnCollection, "paramlistitem");
        StringBuilder paramListItemColumns = new StringBuilder();
        paramListItemColumns.append("paramlistitem.entryreftypeid, paramlistitem.entrysdcid, paramlistitem.paramid, paramlistitem.displayunits, paramlistitem.paramtype, paramlistitem.datatypes, coalesce(paramlistitem.usersequence, 1) paramlistitemsequence");
        for (int i = 0; i < paramListItemColumnCollection.size(); ++i) {
            PropertyList columnProps = paramListItemColumnCollection.getPropertyList(i);
            paramListItemColumns.append(", ").append(columnProps.getProperty("sql"));
        }
        StringBuilder getParamItemsWhereFragment = new StringBuilder();
        ArrayList<String> getParamItemsParams = new ArrayList<String>();
        for (int i = 0; i < paramListIdList.size(); ++i) {
            getParamItemsWhereFragment.append(" OR (paramlist.paramlistid = ? AND paramlist.paramlistversionid = ? AND paramlist.variantid = ?)");
            getParamItemsParams.add(paramListIdList.get(i));
            getParamItemsParams.add(paramListVersionIdList.get(i));
            getParamItemsParams.add(variantIdList.get(i));
        }
        if (paramListIdList.size() > 0) {
            String getParamItemsSql = "SELECT paramlist.paramlistid, paramlist.paramlistversionid, paramlist.variantid, paramlist.versionstatus, " + paramListItemColumns + " FROM paramlist LEFT JOIN paramlistitem ON paramlist.paramlistid = paramlistitem.paramlistid AND paramlist.paramlistversionid = paramlistitem.paramlistversionid AND paramlist.variantid = paramlistitem.variantid " + (!filterParamType.isEmpty() ? " AND paramlistitem.paramtype = '" + filterParamType + "'" : "") + "  WHERE (" + getParamItemsWhereFragment.substring(4) + ")" + (!filterParamType.isEmpty() ? " AND paramlistitem.paramtype = '" + filterParamType + "' " : " ") + "ORDER BY paramlist.paramlistid, paramlist.variantid, paramlist.paramlistversionid,  paramlistitem.usersequence";
            return this.getQueryProcessor().getPreparedSqlDataSet(getParamItemsSql, getParamItemsParams.toArray());
        }
        return new DataSet();
    }

    public DataSet getWorkItemItems(List<String> workItemIdList, List<String> workItemVersionIdList, PropertyListCollection workItemColumnCollection) throws SapphireException {
        if (workItemIdList == null) {
            throw new SapphireException("Work item ID list is null");
        }
        if (workItemVersionIdList == null) {
            throw new SapphireException("Work item version ID list is null");
        }
        if (workItemColumnCollection == null) {
            throw new SapphireException("Work item column collection is null");
        }
        StringBuilder getWorkItemItemsWhereFragment = new StringBuilder();
        ArrayList<String> getWorkItemItemsParams = new ArrayList<String>();
        for (int i = 0; i < workItemIdList.size(); ++i) {
            getWorkItemItemsWhereFragment.append(" or (workitem.workitemid = ? AND workitem.workitemversionid = ?)");
            getWorkItemItemsParams.add(workItemIdList.get(i));
            getWorkItemItemsParams.add(workItemVersionIdList.get(i));
        }
        this.completeConfiguredColumns(workItemColumnCollection, "workitem");
        StringBuilder workItemSqlColumns = new StringBuilder();
        workItemSqlColumns.append("workitem.workitemid, workitem.workitemversionid, workitem.versionstatus, coalesce(workitem.usersequence, 1) workitemsequence");
        for (int i = 0; i < workItemColumnCollection.size(); ++i) {
            PropertyList columnProps = workItemColumnCollection.getPropertyList(i);
            workItemSqlColumns.append(", ").append(columnProps.getProperty("sql"));
        }
        if (getWorkItemItemsWhereFragment.length() > 0) {
            String getWorkItemItemsSql = "SELECT wii.keyid1 paramlistid, wii.keyid2 paramlistversionid, wii.keyid3 variantid, coalesce(wii.usersequence, 1) workitemitemsequence, " + workItemSqlColumns + " FROM workitem LEFT JOIN workitemitem wii ON wii.workitemid = workitem.workitemid AND wii.workitemversionid = workitem.workitemversionid AND wii.sdcid = 'ParamList' WHERE (" + getWorkItemItemsWhereFragment.substring(4) + ") ORDER BY wii.workitemid, wii.workitemversionid, wii.usersequence";
            return this.getQueryProcessor().getPreparedSqlDataSet(getWorkItemItemsSql, getWorkItemItemsParams.toArray());
        }
        return new DataSet();
    }

    public List<String> getActualWorkItemVersionIdList(List<String> workItemIdList, List<String> workItemVersionIdList) throws SapphireException {
        if (workItemIdList == null) {
            throw new SapphireException("Work item ID list is null");
        }
        if (workItemVersionIdList == null) {
            throw new SapphireException("Work item version ID list is null");
        }
        ArrayList<String> actualWorkItemVersionIdList = new ArrayList();
        StringBuilder getVersionInfoWhereFragment = new StringBuilder();
        ArrayList<String> getVersionInfoParams = new ArrayList<String>();
        for (int i = 0; i < workItemVersionIdList.size(); ++i) {
            String workItemId = workItemIdList.get(i);
            String workItemVersionId = workItemVersionIdList.get(i);
            if (!workItemVersionId.equals("C")) continue;
            getVersionInfoWhereFragment.append(" or workitemid = ?");
            getVersionInfoParams.add(workItemId);
        }
        if (!getVersionInfoParams.isEmpty()) {
            String getVersionInfoSql = "SELECT workitemid, workitemversionid, versionstatus FROM workitem WHERE " + getVersionInfoWhereFragment.substring(4) + " ORDER BY workitemid, CAST(workitemversionid AS int), versionstatus";
            DataSet getVersionInfoDs = this.getQueryProcessor().getPreparedSqlDataSet(getVersionInfoSql, getVersionInfoParams.toArray());
            for (int i = 0; i < workItemVersionIdList.size(); ++i) {
                String workItemId = workItemIdList.get(i);
                String workItemVersionId = workItemVersionIdList.get(i);
                if (workItemVersionId.equals("C")) {
                    HashMap<String, String> currentFilter = new HashMap<String, String>();
                    currentFilter.put("workitemid", workItemId);
                    currentFilter.put("versionstatus", "C");
                    DataSet filteredCurrentDs = getVersionInfoDs.getFilteredDataSet(currentFilter);
                    if (filteredCurrentDs.getRowCount() > 0) {
                        actualWorkItemVersionIdList.add(filteredCurrentDs.getString(0, "workitemversionid"));
                        continue;
                    }
                    HashMap<String, String> provisionalFilter = new HashMap<String, String>();
                    provisionalFilter.put("workitemid", workItemId);
                    provisionalFilter.put("versionstatus", "P");
                    DataSet filteredProvisionalDs = getVersionInfoDs.getFilteredDataSet(provisionalFilter);
                    if (filteredProvisionalDs.getRowCount() <= 0) continue;
                    actualWorkItemVersionIdList.add(filteredProvisionalDs.getString(filteredProvisionalDs.getRowCount() - 1, "workitemversionid"));
                    continue;
                }
                actualWorkItemVersionIdList.add(workItemVersionId);
            }
        } else {
            actualWorkItemVersionIdList = new ArrayList<String>(workItemVersionIdList);
        }
        return actualWorkItemVersionIdList;
    }

    public List<String> getCurrentWorkItemIdList(List<String> workItemIdList, List<String> workItemVersionIdList) {
        ArrayList<String> currentWorkItemIdList = new ArrayList<String>();
        for (int i = 0; i < workItemVersionIdList.size(); ++i) {
            String workItemVersionId = workItemVersionIdList.get(i);
            if (!workItemVersionId.equals("C")) continue;
            String workItemId = workItemIdList.get(i);
            currentWorkItemIdList.add(workItemId);
        }
        return currentWorkItemIdList;
    }

    public List<List<String>> getCurrentParamListKeyList(List<String> paramListIdList, List<String> variantIdList, List<String> paramListVersionIdList) throws SapphireException {
        if (paramListIdList == null) {
            throw new SapphireException("Param list ID list is null");
        }
        if (paramListVersionIdList == null) {
            throw new SapphireException("Param list version ID list is null");
        }
        if (variantIdList == null) {
            throw new SapphireException("Variant ID list is null");
        }
        ArrayList<List<String>> currentParamListKeyList = new ArrayList<List<String>>();
        for (int i = 0; i < paramListVersionIdList.size(); ++i) {
            String paramListVersionId = paramListVersionIdList.get(i);
            if (!paramListVersionId.equals("C")) continue;
            String paramListId = paramListIdList.get(i);
            String variantId = variantIdList.get(i);
            List<String> paramListKey = Arrays.asList(paramListId, variantId);
            currentParamListKeyList.add(paramListKey);
        }
        return currentParamListKeyList;
    }

    public String getPrimaryVersionStatus(String sdcId, String keyId1, String keyId2, String keyId3) throws SapphireException {
        if (sdcId == null) {
            throw new SapphireException("SDC ID is null");
        }
        if (sdcId.isEmpty()) {
            throw new SapphireException("SDC ID is empty");
        }
        if (keyId1 == null) {
            throw new SapphireException("Key ID1 is null");
        }
        if (keyId1.isEmpty()) {
            throw new SapphireException("Key ID1 is empty");
        }
        if (keyId2 == null) {
            throw new SapphireException("Key ID2 is null");
        }
        if (keyId3 == null) {
            throw new SapphireException("Key ID3 is null");
        }
        String versionStatus = "P";
        boolean isVersioned = new PropertyList(this.getSDCProcessor().getPropertyList(sdcId)).getProperty("versionedflag").toLowerCase().startsWith("y");
        if (isVersioned) {
            DataSet getPrimaryVersionStatusDs;
            PropertyList primaryProps = new PropertyList(this.getSDCProcessor().getSDCProperties(sdcId));
            String tableId = primaryProps.getProperty("tableid");
            String keyColId1 = primaryProps.getProperty("keycolid1");
            String keyColId2 = primaryProps.getProperty("keycolid2");
            String keyColId3 = primaryProps.getProperty("keycolid3");
            String getPrimaryVersionStatusSql = "SELECT p.versionstatus FROM " + tableId + " p WHERE p." + keyColId1 + " = ? " + (!keyColId2.isEmpty() ? " AND p." + keyColId2 + " = ?" : "") + (!keyColId3.isEmpty() ? " and p." + keyColId3 + " = ?" : "");
            int paramCount = 1;
            if (keyId2.length() > 0) {
                ++paramCount;
            }
            if (keyId3.length() > 0) {
                ++paramCount;
            }
            Object[] getPrimaryVersionStatusParams = new String[paramCount];
            getPrimaryVersionStatusParams[0] = keyId1;
            if (keyId2.length() > 0) {
                getPrimaryVersionStatusParams[1] = keyId2;
            }
            if (keyId3.length() > 0) {
                getPrimaryVersionStatusParams[2] = keyId3;
            }
            if ((getPrimaryVersionStatusDs = this.getQueryProcessor().getPreparedSqlDataSet(getPrimaryVersionStatusSql, getPrimaryVersionStatusParams)).getRowCount() == 0) {
                throw new SapphireException("Primary SDI not found: " + sdcId + ", " + keyId1 + ", " + keyId2 + ", " + keyId3);
            }
            versionStatus = getPrimaryVersionStatusDs.getString(0, "versionstatus", "P");
        }
        return versionStatus;
    }

    public void setParamCounts(Map<List<String>, Set<List<String>>> workItemParamMap, PropertyListCollection rowCollection) {
        if (workItemParamMap == null) {
            throw new IllegalArgumentException("Work item param map is null");
        }
        if (rowCollection == null) {
            throw new IllegalArgumentException("Row collection is null");
        }
        HashSet<List<String>> workItemKeySet = new HashSet<List<String>>();
        for (int i = 0; i < rowCollection.size(); ++i) {
            PropertyList rowProps = rowCollection.getPropertyList(i);
            String workItemId = rowProps.getProperty("workitemid");
            String workItemVersionId = rowProps.getProperty("workitemversionid");
            List<String> workItemKey = Arrays.asList(workItemId, workItemVersionId);
            Set<List<String>> paramSet = workItemParamMap.get(workItemKey);
            rowProps.setProperty("paramcount", Integer.toString(paramSet.size()));
            if (!workItemKeySet.contains(workItemKey)) {
                rowProps.setProperty("isfirst", "Y");
                workItemKeySet.add(workItemKey);
                continue;
            }
            rowProps.setProperty("isfirst", "N");
        }
    }

    public void addAllValues(DataSet from, int row, PropertyList to) {
        if (from == null) {
            throw new IllegalArgumentException("From is null");
        }
        if (to == null) {
            throw new IllegalArgumentException("To is null");
        }
        if (row < 0) {
            throw new IllegalArgumentException("Row < 0");
        }
        for (int j = 0; j < from.getColumnCount(); ++j) {
            String columnId = from.getColumnId(j);
            String value = from.getValue(row, columnId);
            to.setProperty(columnId, value);
            to.setProperty(columnId + "_original", value);
        }
    }
}

