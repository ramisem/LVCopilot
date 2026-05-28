/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.data.adapter.AbstractDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDIDataItemAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDIDataItemFilterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDIWorkItemFilterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public final class SDIDataItemAdapter
extends AbstractDataSetAdapter {
    private final SDIDataItemAdapterConfiguration sdiDataItemAdapterConf;

    public SDIDataItemAdapter(String connectionId, DataBindingMap dataBindingMap, SDIDataItemAdapterConfiguration sdiDataItemAdapterConf) {
        super(connectionId, dataBindingMap);
        this.sdiDataItemAdapterConf = sdiDataItemAdapterConf;
    }

    @Override
    public void processDataSetAdapter(DataSet primaryDataSet) throws SapphireException {
        String sdcId = this.sdiDataItemAdapterConf.getSdcId();
        String rSetId = this.sdiDataItemAdapterConf.getRSetId().evaluate(this.getDataBindingMap());
        List<ColumnConfiguration> columnConfList = this.sdiDataItemAdapterConf.getColumnConfigurationList();
        List<SDIDataItemFilterConfiguration> sdiDataItemFilterConfList = this.sdiDataItemAdapterConf.getSDIDataItemFilterConfigurationList();
        List<SDIWorkItemFilterConfiguration> sdiWorkItemFilterConfList = this.sdiDataItemAdapterConf.getSDIWorkItemFilterConfigurationList();
        List<StringExpression> sortColumnConfList = this.sdiDataItemAdapterConf.getSortColumns();
        if (sdcId.isEmpty()) {
            throw new IllegalArgumentException("SDC ID is empty");
        }
        String sdcTable = this.getSDCProcessor().getProperty(sdcId, "tableid");
        if (sdcTable == null || sdcTable.equals("")) {
            throw new IllegalArgumentException("Invalid SDC ID: " + sdcId);
        }
        if (primaryDataSet.getRowCount() > 0) {
            boolean includeSpecs;
            try {
                includeSpecs = this.sdiDataItemAdapterConf.getIncludeSpecs().evaluate(this.getDataBindingMap());
            }
            catch (SapphireException e) {
                throw new IllegalArgumentException("Cannot evaluate include specs expression", e);
            }
            DataSet getResultsAndLimitsDs = this.getResultsAndLimitsDataSet(primaryDataSet, sdcId, columnConfList, sdiWorkItemFilterConfList, sdiDataItemFilterConfList, includeSpecs, rSetId);
            Set<String> primaryColumns = this.sdiDataItemAdapterConf.includeAllPrimaryColumns() ? this.getAllPrimaryColumns(primaryDataSet) : this.getPrimaryColumns(columnConfList, primaryDataSet, sdcId);
            this.appendPrimaryColumnsToResultDataSet(sdcId, primaryDataSet, getResultsAndLimitsDs, primaryColumns, this.sdiDataItemAdapterConf.includeAllPrimaryRows());
            StringBuilder sortColumns = new StringBuilder();
            for (StringExpression sortColumn : sortColumnConfList) {
                if (sortColumns.length() > 0) {
                    sortColumns.append(",");
                }
                sortColumns.append(sortColumn.evaluateNoException(this.getDataBindingMap()));
            }
            if (sortColumns.length() > 0) {
                getResultsAndLimitsDs.sort(sortColumns.toString());
            }
            this.setProcessedDataSet(getResultsAndLimitsDs);
        } else {
            this.setProcessedDataSet(primaryDataSet);
        }
    }

    private DataSet getResultsAndLimitsDataSet(DataSet dataSet, String sdcId, List<ColumnConfiguration> columnConfList, List<SDIWorkItemFilterConfiguration> sdiWorkItemFilterConfList, List<SDIDataItemFilterConfiguration> sdiDataItemFilterConfList, boolean includeSpecs, String rSetId) throws SapphireException {
        StringBuilder getResultsAndLimitsSql = new StringBuilder();
        ArrayList<String> workItemIdList = new ArrayList<String>();
        ArrayList<String> workItemVersionIdList = new ArrayList<String>();
        ArrayList<String> workItemInstanceList = new ArrayList<String>();
        String filterSeparator = this.sdiDataItemAdapterConf.getFilterSeparator();
        for (SDIWorkItemFilterConfiguration sdiWorkItemFilterConfiguration : sdiWorkItemFilterConfList) {
            String workItemInstance;
            String string;
            String workItemId;
            try {
                workItemId = sdiWorkItemFilterConfiguration.getWorkItemId().evaluate(this.getDataBindingMap());
                string = sdiWorkItemFilterConfiguration.getWorkItemVersionId().evaluate(this.getDataBindingMap());
                workItemInstance = sdiWorkItemFilterConfiguration.getWorkItemInstance().evaluate(this.getDataBindingMap());
            }
            catch (SapphireException e) {
                throw new IllegalArgumentException("Cannot evaluate SDI work item filter expression", e);
            }
            ArrayList<String> separatedWorkItemIdList = new ArrayList<String>();
            ArrayList<String> separatedWorkItemVersionIdList = new ArrayList<String>();
            ArrayList<String> separatedWorkItemInstanceList = new ArrayList<String>();
            if (!workItemId.isEmpty()) {
                separatedWorkItemIdList.addAll(Arrays.asList(workItemId.split(filterSeparator)));
            }
            if (!string.isEmpty()) {
                separatedWorkItemVersionIdList.addAll(Arrays.asList(string.split(filterSeparator)));
            }
            if (!workItemInstance.isEmpty()) {
                separatedWorkItemInstanceList.addAll(Arrays.asList(workItemInstance.split(filterSeparator)));
            }
            this.harmonizeSDIWorkItemFilterLists(separatedWorkItemIdList, separatedWorkItemVersionIdList, separatedWorkItemInstanceList);
            workItemIdList.addAll(separatedWorkItemIdList);
            workItemVersionIdList.addAll(separatedWorkItemVersionIdList);
            workItemInstanceList.addAll(separatedWorkItemInstanceList);
        }
        boolean filterByWorkItems = false;
        if (!(workItemIdList.isEmpty() && workItemVersionIdList.isEmpty() && workItemInstanceList.isEmpty())) {
            filterByWorkItems = true;
        }
        getResultsAndLimitsSql.append("SELECT ");
        LinkedHashSet<String> selectColumnSet = new LinkedHashSet<String>();
        selectColumnSet.add("sdidata.sdidataid");
        selectColumnSet.add("sdidataitem.sdidataitemid");
        selectColumnSet.add("sdidataitem.sdcid");
        selectColumnSet.add("sdidataitem.keyid1");
        selectColumnSet.add("sdidataitem.keyid2");
        selectColumnSet.add("sdidataitem.keyid3");
        selectColumnSet.add("sdidataitem.paramlistid");
        selectColumnSet.add("sdidataitem.paramlistversionid");
        selectColumnSet.add("sdidataitem.variantid");
        selectColumnSet.add("sdidataitem.dataset");
        selectColumnSet.add("sdidataitem.paramid");
        selectColumnSet.add("sdidataitem.paramtype");
        selectColumnSet.add("sdidataitem.replicateid");
        selectColumnSet.add("sdidataitem.enteredtext");
        selectColumnSet.add("sdidataitem.enteredvalue");
        selectColumnSet.add("sdidataitem.transformvalue");
        selectColumnSet.add("sdidataitem.displayvalue");
        if (includeSpecs) {
            selectColumnSet.add("specparamitems.specid");
            selectColumnSet.add("specparamitems.specversionid");
            selectColumnSet.add("specparamlimits.operator1");
            selectColumnSet.add("specparamlimits.operator2");
            selectColumnSet.add("specparamlimits.value1");
            selectColumnSet.add("specparamlimits.value2");
            selectColumnSet.add("speclimittype.limittypeid");
            selectColumnSet.add("sdidataitemspec.condition");
        }
        if (filterByWorkItems) {
            selectColumnSet.add("sdiworkitem.workitemid");
            selectColumnSet.add("sdiworkitem.workitemversionid");
            selectColumnSet.add("sdiworkitem.workiteminstance");
        }
        for (ColumnConfiguration columnConfiguration : columnConfList) {
            String tableId = columnConfiguration.getTableId();
            String columnId = columnConfiguration.getColumnId();
            if (tableId.isEmpty() || tableId.equals("primary") || columnId.isEmpty()) continue;
            selectColumnSet.add(tableId + "." + columnId);
        }
        StringBuilder getResultsAndLimitsSelectFragment = new StringBuilder();
        for (String column : selectColumnSet) {
            getResultsAndLimitsSelectFragment.append(", ").append(column);
        }
        getResultsAndLimitsSql.append(getResultsAndLimitsSelectFragment.substring(2));
        getResultsAndLimitsSql.append(" FROM ");
        getResultsAndLimitsSql.append("sdidata JOIN rsetitems ON sdidata.sdcid = rsetitems.sdcid AND sdidata.keyid1 = rsetitems.keyid1 AND sdidata.keyid2 = rsetitems.keyid2 AND sdidata.keyid3 = rsetitems.keyid3 JOIN sdidataitem ON sdidata.sdcid = sdidataitem.sdcid AND sdidata.keyid1 = sdidataitem.keyid1 AND sdidata.keyid2 = sdidataitem.keyid2 AND sdidata.keyid3 = sdidataitem.keyid3 AND sdidata.paramlistid = sdidataitem.paramlistid AND sdidata.paramlistversionid = sdidataitem.paramlistversionid AND sdidata.variantid = sdidataitem.variantid AND sdidata.dataset = sdidataitem.dataset");
        if (filterByWorkItems) {
            getResultsAndLimitsSql.append(" JOIN sdiworkitem ON sdidataitem.sdcid = sdiworkitem.sdcid AND sdidataitem.keyid1 = sdiworkitem.keyid1 AND sdidataitem.keyid2 = sdiworkitem.keyid2 AND sdidataitem.keyid3 = sdiworkitem.keyid3 JOIN sdiworkitemitem ON sdiworkitemitem.sdcid = sdiworkitem.sdcid AND sdiworkitemitem.keyid1 = sdiworkitem.keyid1 AND sdiworkitemitem.keyid2 = sdiworkitem.keyid2 AND sdiworkitemitem.keyid3 = sdiworkitem.keyid3 AND sdiworkitemitem.workitemid = sdiworkitem.workitemid AND sdiworkitemitem.workiteminstance = sdiworkitem.workiteminstance AND sdiworkitemitem.itemkeyid1 = sdidataitem.paramlistid AND sdiworkitemitem.itemkeyid2 = sdidataitem.paramlistversionid AND sdiworkitemitem.itemkeyid3 = sdidataitem.variantid AND sdiworkitemitem.iteminstance = sdidataitem.dataset");
        }
        if (includeSpecs) {
            String string = " LEFT OUTER JOIN sdidataitemspec ON sdidataitemspec.sdcid = sdidataitem.sdcid AND sdidataitemspec.keyid1 = sdidataitem.keyid1 AND sdidataitemspec.keyid2 = sdidataitem.keyid2 AND sdidataitemspec.keyid3 = sdidataitem.keyid3 AND sdidataitemspec.paramlistid = sdidataitem.paramlistid AND sdidataitemspec.paramlistversionid = sdidataitem.paramlistversionid AND sdidataitemspec.variantid = sdidataitem.variantid AND sdidataitemspec.dataset = sdidataitem.dataset AND sdidataitemspec.paramid = sdidataitem.paramid AND sdidataitemspec.paramtype = sdidataitem.paramtype AND sdidataitemspec.replicateid = sdidataitem.replicateid LEFT OUTER JOIN specparamitems ON specparamitems.specid = sdidataitemspec.specid AND specparamitems.specversionid = sdidataitemspec.specversionid AND (specparamitems.paramlistid = sdidataitemspec.paramlistid OR specparamitems.allowanyparamlistflag = 'Y') AND (specparamitems.paramlistversionid = sdidataitemspec.paramlistversionid OR specparamitems.allowanyparamlistflag IN ('Y', 'V', 'A')) AND (specparamitems.variantid = sdidataitemspec.variantid OR specparamitems.allowanyparamlistflag IN ('Y', 'A')) AND specparamitems.paramid = sdidataitemspec.paramid AND specparamitems.paramtype = sdidataitemspec.paramtype LEFT OUTER JOIN specparamlimits ON specparamlimits.specid = specparamitems.specid AND specparamlimits.specversionid = specparamitems.specversionid AND specparamlimits.paramlistid = specparamitems.paramlistid AND specparamlimits.paramlistversionid = specparamitems.paramlistversionid AND specparamlimits.variantid = specparamitems.variantid AND specparamlimits.paramid = specparamitems.paramid AND specparamlimits.paramtype = specparamitems.paramtype LEFT OUTER JOIN speclimittype speclimittype ON speclimittype.specid = specparamitems.specid AND speclimittype.specversionid = specparamitems.specversionid AND speclimittype.limittypesequence = specparamlimits.limittypesequence";
            getResultsAndLimitsSql.append(string);
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        getResultsAndLimitsSql.append(" WHERE ");
        getResultsAndLimitsSql.append("sdidata.sdcid = ? ");
        if (sdcId.equals("DataSet")) {
            arrayList.add("Sample");
        } else {
            arrayList.add(sdcId);
        }
        int sdiCount = 0;
        boolean clearRSet = false;
        if (rSetId.isEmpty()) {
            StringBuilder keyId1s = new StringBuilder();
            StringBuilder keyId2s = new StringBuilder();
            StringBuilder keyId3s = new StringBuilder();
            List<String> primaryKeyColumnList = this.getKeyColumns(sdcId);
            for (int i = 0; i < dataSet.getRowCount(); ++i) {
                String primaryKeyId1Column = primaryKeyColumnList.get(0);
                keyId1s.append(";").append(dataSet.getString(i, primaryKeyId1Column));
                ++sdiCount;
                if (primaryKeyColumnList.size() < 2) continue;
                String primaryKeyId2Column = primaryKeyColumnList.get(1);
                keyId2s.append(";").append(dataSet.getString(i, primaryKeyId2Column));
                if (primaryKeyColumnList.size() < 3) continue;
                String primaryKeyId3Column = primaryKeyColumnList.get(2);
                keyId3s.append(";").append(dataSet.getString(i, primaryKeyId3Column));
            }
            rSetId = this.getDAMProcessor().getAllDSRSet(sdcId, keyId1s.substring(1), keyId2s.length() > 0 ? keyId2s.substring(1) : "", keyId3s.length() > 0 ? keyId3s.substring(1) : "");
            clearRSet = true;
        }
        DataSet getResultsAndLimitsDs = new DataSet();
        if (sdiCount > 0 || !rSetId.isEmpty()) {
            getResultsAndLimitsSql.append("AND rsetitems.rsetid = ? ");
            arrayList.add(rSetId);
            if (this.sdiDataItemAdapterConf.isIgnoreCancelled()) {
                getResultsAndLimitsSql.append("AND sdidata.s_datasetstatus <> 'Cancelled' ");
            }
            if (this.sdiDataItemAdapterConf.isIgnoreRetested()) {
                getResultsAndLimitsSql.append("AND (sdidata.s_retestedflag = 'N' OR sdidata.s_retestedflag IS NULL)");
            }
            if (this.sdiDataItemAdapterConf.isIgnoreRemeasured()) {
                getResultsAndLimitsSql.append("AND (sdidata.s_remeasuredflag = 'N' OR sdidata.s_remeasuredflag IS NULL)");
            }
            try {
                if (!this.sdiDataItemAdapterConf.getIncludeOutliers().evaluate(this.getDataBindingMap()).booleanValue()) {
                    getResultsAndLimitsSql.append("AND (sdidataitem.calcexcludeflag = 'N' OR sdidataitem.calcexcludeflag IS NULL) ");
                }
            }
            catch (SapphireException e) {
                throw new IllegalArgumentException("Cannot evaluate include outliers flag: " + this.sdiDataItemAdapterConf.getIncludeOutliers().getExpression());
            }
            ArrayList<String> paramListIdList = new ArrayList<String>();
            ArrayList<String> paramListVersionIdList = new ArrayList<String>();
            ArrayList<String> variantIdList = new ArrayList<String>();
            ArrayList<String> dataSetIdList = new ArrayList<String>();
            ArrayList<String> paramIdList = new ArrayList<String>();
            ArrayList<String> paramTypeList = new ArrayList<String>();
            ArrayList<String> replicateIdList = new ArrayList<String>();
            for (SDIDataItemFilterConfiguration sdiDataItemFilterConf : sdiDataItemFilterConfList) {
                String replicateIdStr;
                String paramType;
                String paramId;
                String dataSetStr;
                String variantId;
                String paramListVersionId;
                String paramListId;
                String columnValues;
                String columnIdList;
                try {
                    columnIdList = sdiDataItemFilterConf.getColumnIdList().evaluate(this.getDataBindingMap());
                    columnValues = sdiDataItemFilterConf.getColumnValues().evaluate(this.getDataBindingMap());
                    paramListId = sdiDataItemFilterConf.getParamListId().evaluate(this.getDataBindingMap());
                    paramListVersionId = sdiDataItemFilterConf.getParamListVersionId().evaluate(this.getDataBindingMap());
                    variantId = sdiDataItemFilterConf.getVariantId().evaluate(this.getDataBindingMap());
                    dataSetStr = sdiDataItemFilterConf.getDataSet().evaluate(this.getDataBindingMap());
                    paramId = sdiDataItemFilterConf.getParamId().evaluate(this.getDataBindingMap());
                    paramType = sdiDataItemFilterConf.getParamType().evaluate(this.getDataBindingMap());
                    replicateIdStr = sdiDataItemFilterConf.getReplicateId().evaluate(this.getDataBindingMap());
                }
                catch (SapphireException e) {
                    throw new IllegalArgumentException("Cannot evaluate SDI data item filter expression", e);
                }
                ArrayList<String> separatedParamListIdList = new ArrayList<String>();
                ArrayList<String> separatedParamListVersionIdList = new ArrayList<String>();
                ArrayList<String> separatedVariantIdList = new ArrayList<String>();
                ArrayList<String> separatedDataSetList = new ArrayList<String>();
                ArrayList<String> separatedParamIdList = new ArrayList<String>();
                ArrayList<String> separatedParamTypeList = new ArrayList<String>();
                ArrayList<String> separatedReplicateIdList = new ArrayList<String>();
                if (!columnIdList.isEmpty()) {
                    String[] columnValuesList;
                    List<String> columnIds = Arrays.asList(StringUtil.split(columnIdList, "&"));
                    for (String columnValuesStr : columnValuesList = StringUtil.split(columnValues, ";")) {
                        String[] values = StringUtil.split(columnValuesStr, "&");
                        if (values.length != columnIds.size()) continue;
                        for (int i = 0; i < columnIds.size(); ++i) {
                            String column = columnIds.get(i);
                            String columnValue = values[i];
                            if (column.equals("paramlistid")) {
                                separatedParamListIdList.add(columnValue);
                            }
                            if (column.equals("paramlisversionid")) {
                                separatedParamListVersionIdList.add(columnValue);
                            }
                            if (column.equals("variantid")) {
                                separatedVariantIdList.add(columnValue);
                            }
                            if (column.equals("dataset")) {
                                separatedDataSetList.add(columnValue);
                            }
                            if (column.equals("paramid")) {
                                separatedParamIdList.add(columnValue);
                            }
                            if (column.equals("paramtype")) {
                                separatedParamTypeList.add(columnValue);
                            }
                            if (!column.equals("replicateid")) continue;
                            separatedReplicateIdList.add(columnValue);
                        }
                    }
                } else {
                    if (!paramListId.isEmpty()) {
                        separatedParamListIdList.addAll(Arrays.asList(paramListId.split(filterSeparator)));
                    }
                    if (!paramListVersionId.isEmpty()) {
                        separatedParamListVersionIdList.addAll(Arrays.asList(paramListVersionId.split(filterSeparator)));
                    }
                    if (!variantId.isEmpty()) {
                        separatedVariantIdList.addAll(Arrays.asList(variantId.split(filterSeparator)));
                    }
                    if (!dataSetStr.isEmpty()) {
                        separatedDataSetList.addAll(Arrays.asList(dataSetStr.split(filterSeparator)));
                    }
                    if (!paramId.isEmpty()) {
                        separatedParamIdList.addAll(Arrays.asList(paramId.split(filterSeparator)));
                    }
                    if (!paramType.isEmpty()) {
                        separatedParamTypeList.addAll(Arrays.asList(paramType.split(filterSeparator)));
                    }
                    if (!replicateIdStr.isEmpty()) {
                        separatedReplicateIdList.addAll(Arrays.asList(replicateIdStr.split(filterSeparator)));
                    }
                }
                this.harmonizeSDIDataItemFilterLists(separatedParamListIdList, separatedParamListVersionIdList, separatedVariantIdList, separatedDataSetList, separatedParamIdList, separatedParamTypeList, separatedReplicateIdList);
                paramListIdList.addAll(separatedParamListIdList);
                paramListVersionIdList.addAll(separatedParamListVersionIdList);
                variantIdList.addAll(separatedVariantIdList);
                dataSetIdList.addAll(separatedDataSetList);
                paramIdList.addAll(separatedParamIdList);
                paramTypeList.addAll(separatedParamTypeList);
                replicateIdList.addAll(separatedReplicateIdList);
            }
            StringBuilder getResultsAndLimitsSDIWorkItemWhereFragment = new StringBuilder();
            for (int i = 0; i < workItemIdList.size(); ++i) {
                String workItemId = (String)workItemIdList.get(i);
                String workItemVersionId = (String)workItemVersionIdList.get(i);
                String workItemInstance = (String)workItemInstanceList.get(i);
                if (workItemId.equals("") && workItemVersionId.equals("") && workItemInstance.equals("")) continue;
                getResultsAndLimitsSDIWorkItemWhereFragment.append("OR (");
                boolean isFirst = true;
                if (!workItemId.isEmpty()) {
                    getResultsAndLimitsSDIWorkItemWhereFragment.append("sdiworkitem.workitemid = ? ");
                    arrayList.add(workItemId);
                    isFirst = false;
                }
                if (!workItemVersionId.isEmpty()) {
                    if (!isFirst) {
                        getResultsAndLimitsSDIWorkItemWhereFragment.append("AND ");
                    }
                    getResultsAndLimitsSDIWorkItemWhereFragment.append("sdiworkitem.workitemversionid = ? ");
                    arrayList.add(workItemVersionId);
                    isFirst = false;
                }
                if (!workItemInstance.isEmpty()) {
                    if (!isFirst) {
                        getResultsAndLimitsSDIWorkItemWhereFragment.append("AND ");
                    }
                    getResultsAndLimitsSDIWorkItemWhereFragment.append("sdiworkitem.workiteminstance= ? ");
                    arrayList.add(workItemInstance);
                }
                getResultsAndLimitsSDIWorkItemWhereFragment.append(")");
            }
            if (getResultsAndLimitsSDIWorkItemWhereFragment.length() > 0) {
                getResultsAndLimitsSql.append("AND (").append(getResultsAndLimitsSDIWorkItemWhereFragment.substring(3)).append(") ");
                getResultsAndLimitsSql.append("AND sdiworkitemitem.itemsdcid = 'ParamList' ");
            }
            StringBuilder getResultsAndLimitsSDIDataItemWhereFragment = new StringBuilder();
            for (int i = 0; i < paramListIdList.size(); ++i) {
                String paramListId = (String)paramListIdList.get(i);
                String paramListVersionId = (String)paramListVersionIdList.get(i);
                String variantId = (String)variantIdList.get(i);
                String dataSetStr = (String)dataSetIdList.get(i);
                String paramId = (String)paramIdList.get(i);
                String paramType = (String)paramTypeList.get(i);
                String replicateIdStr = (String)replicateIdList.get(i);
                if (paramListId.equals("") && paramListVersionId.equals("") && variantId.equals("") && dataSetStr.equals("") && paramId.equals("") && paramType.equals("") && replicateIdStr.equals("")) continue;
                getResultsAndLimitsSDIDataItemWhereFragment.append("OR (");
                boolean isFirst = true;
                if (!paramListId.isEmpty()) {
                    getResultsAndLimitsSDIDataItemWhereFragment.append("sdidataitem.paramlistid = ? ");
                    arrayList.add(paramListId);
                    isFirst = false;
                }
                if (!paramListVersionId.isEmpty()) {
                    if (!isFirst) {
                        getResultsAndLimitsSDIDataItemWhereFragment.append("AND ");
                    }
                    getResultsAndLimitsSDIDataItemWhereFragment.append("sdidataitem.paramlistversionid = ? ");
                    arrayList.add(paramListVersionId);
                    isFirst = false;
                }
                if (!variantId.isEmpty()) {
                    if (!isFirst) {
                        getResultsAndLimitsSDIDataItemWhereFragment.append("AND ");
                    }
                    getResultsAndLimitsSDIDataItemWhereFragment.append("sdidataitem.variantid = ? ");
                    arrayList.add(variantId);
                    isFirst = false;
                }
                if (!dataSetStr.isEmpty()) {
                    if (!isFirst) {
                        getResultsAndLimitsSDIDataItemWhereFragment.append("AND ");
                    }
                    getResultsAndLimitsSDIDataItemWhereFragment.append("sdidataitem.dataset = ? ");
                    arrayList.add(dataSetStr);
                    isFirst = false;
                }
                if (!paramId.isEmpty()) {
                    if (!isFirst) {
                        getResultsAndLimitsSDIDataItemWhereFragment.append("AND ");
                    }
                    getResultsAndLimitsSDIDataItemWhereFragment.append("sdidataitem.paramid = ? ");
                    arrayList.add(paramId);
                    isFirst = false;
                }
                if (!paramType.isEmpty()) {
                    if (!isFirst) {
                        getResultsAndLimitsSDIDataItemWhereFragment.append("AND ");
                    }
                    getResultsAndLimitsSDIDataItemWhereFragment.append("sdidataitem.paramtype = ? ");
                    arrayList.add(paramType);
                    isFirst = false;
                }
                if (!replicateIdStr.isEmpty()) {
                    if (!isFirst) {
                        getResultsAndLimitsSDIDataItemWhereFragment.append("AND ");
                    }
                    getResultsAndLimitsSDIDataItemWhereFragment.append("sdidataitem.replicateid = ? ");
                    arrayList.add(replicateIdStr);
                }
                getResultsAndLimitsSDIDataItemWhereFragment.append(")");
            }
            if (getResultsAndLimitsSDIDataItemWhereFragment.length() > 0) {
                getResultsAndLimitsSql.append("AND (").append(getResultsAndLimitsSDIDataItemWhereFragment.substring(3)).append(") ");
            }
            if (this.sdiDataItemAdapterConf.isResultEntered()) {
                getResultsAndLimitsSql.append("AND sdidataitem.enteredtext IS NOT NULL ");
            }
            if (this.sdiDataItemAdapterConf.isReleasedOnly()) {
                getResultsAndLimitsSql.append("AND sdidataitem.releasedflag = ? ");
                arrayList.add("Y");
            }
            getResultsAndLimitsSql.append("ORDER BY ");
            getResultsAndLimitsSql.append("sdidataitem.keyid1, sdidataitem.keyid2, sdidataitem.keyid3, sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid, sdidataitem.dataset, sdidataitem.paramid, sdidataitem.paramtype, sdidataitem.replicateid");
            getResultsAndLimitsDs = this.getQueryProcessor().getPreparedSqlDataSet(getResultsAndLimitsSql.toString(), arrayList.toArray());
            if (clearRSet) {
                this.getDAMProcessor().clearRSet(rSetId);
            }
            if (getResultsAndLimitsDs == null) {
                throw new IllegalArgumentException("Failed to run SQL: " + getResultsAndLimitsSql.toString() + " with params " + ((Object)arrayList).toString());
            }
        }
        return getResultsAndLimitsDs;
    }

    private void harmonizeSDIDataItemFilterLists(List<String> paramListIdList, List<String> paramListVersionIdList, List<String> variantIdList, List<String> dataSetList, List<String> paramIdList, List<String> paramTypeList, List<String> replicateIdList) {
        int maxCount = 0;
        maxCount = Math.max(maxCount, paramListIdList.size());
        maxCount = Math.max(maxCount, paramListVersionIdList.size());
        maxCount = Math.max(maxCount, variantIdList.size());
        maxCount = Math.max(maxCount, dataSetList.size());
        maxCount = Math.max(maxCount, paramIdList.size());
        maxCount = Math.max(maxCount, paramTypeList.size());
        maxCount = Math.max(maxCount, replicateIdList.size());
        this.fillList(paramListIdList, maxCount);
        this.fillList(paramListVersionIdList, maxCount);
        this.fillList(variantIdList, maxCount);
        this.fillList(dataSetList, maxCount);
        this.fillList(paramIdList, maxCount);
        this.fillList(paramTypeList, maxCount);
        this.fillList(replicateIdList, maxCount);
    }

    private void harmonizeSDIWorkItemFilterLists(List<String> workItemIdList, List<String> workItemVersionIdList, List<String> workItemInstanceList) {
        int maxCount = 0;
        maxCount = Math.max(maxCount, workItemIdList.size());
        maxCount = Math.max(maxCount, workItemVersionIdList.size());
        maxCount = Math.max(maxCount, workItemInstanceList.size());
        this.fillList(workItemIdList, maxCount);
        this.fillList(workItemVersionIdList, maxCount);
        this.fillList(workItemInstanceList, maxCount);
    }

    private void fillList(List<String> list, int valueCount) {
        if (list.size() < valueCount) {
            int difference = valueCount - list.size();
            for (int i = 0; i < difference; ++i) {
                list.add("");
            }
        }
    }

    private Set<String> getAllPrimaryColumns(DataSet dataSet) {
        LinkedHashSet<String> primaryCols = new LinkedHashSet<String>();
        for (int i = 0; i < dataSet.getColumnCount(); ++i) {
            primaryCols.add(dataSet.getColumnId(i));
        }
        return primaryCols;
    }

    private Set<String> getPrimaryColumns(List<ColumnConfiguration> columnConfList, DataSet primaryData, String sdcId) {
        LinkedHashSet<String> primaryCols = new LinkedHashSet<String>();
        HashMap sdcProps = this.getSDCProcessor().getSDCProperties(sdcId);
        String primaryKeyCol1 = (String)sdcProps.get("keycolid1");
        String primaryKeyCol2 = (String)sdcProps.get("keycolid2");
        String primaryKeyCol3 = (String)sdcProps.get("keycolid3");
        String descCol = sdcProps.get("tableid") + "desc";
        for (ColumnConfiguration columnConf : columnConfList) {
            String columnId;
            String tableId = columnConf.getTableId();
            if (!tableId.equals("primary") || !primaryData.isValidColumn(columnId = columnConf.getColumnId().toLowerCase())) continue;
            primaryCols.add(columnId);
        }
        primaryCols.add(primaryKeyCol1);
        if (primaryKeyCol2 != null && !primaryKeyCol2.equals("")) {
            primaryCols.add(primaryKeyCol2);
            if (primaryKeyCol3 != null && !primaryKeyCol3.equals("")) {
                primaryCols.add(primaryKeyCol3);
            }
        }
        primaryCols.add(descCol);
        return primaryCols;
    }

    private void appendPrimaryColumnsToResultDataSet(String sdcId, DataSet primaryDataSet, DataSet resultAndLimitsDataSet, Set<String> primaryColumns, boolean includeAllPrimaryRows) {
        this.addColumnsToDataSet(resultAndLimitsDataSet, primaryColumns, primaryDataSet);
        List<String> keyColumns = this.getKeyColumns(sdcId);
        HashMap<String, Integer> index = new HashMap<String, Integer>();
        for (int i = 0; i < primaryDataSet.getRowCount(); ++i) {
            StringBuilder key = new StringBuilder();
            if (sdcId.equals("DataSet")) {
                key.append(primaryDataSet.getString(i, "sdidataid"));
            } else {
                for (String keyColumn : keyColumns) {
                    if (key.length() > 0) {
                        key.append(";");
                    }
                    key.append(primaryDataSet.getString(i, keyColumn));
                }
            }
            index.put(key.toString(), i);
        }
        int origRowCount = resultAndLimitsDataSet.getRowCount();
        for (int i = 0; i < origRowCount; ++i) {
            StringBuilder key = new StringBuilder();
            if (sdcId.equals("DataSet")) {
                key.append(resultAndLimitsDataSet.getString(i, "sdidataid"));
            } else {
                for (int j = 0; j < keyColumns.size(); ++j) {
                    if (key.length() > 0) {
                        key.append(";");
                    }
                    key.append(resultAndLimitsDataSet.getString(i, "keyid" + (j + 1)));
                }
            }
            if (includeAllPrimaryRows) {
                HashMap<String, String> filterMap = new HashMap<String, String>();
                int j = 0;
                for (String keyColumn : keyColumns) {
                    if (key.length() > 0) {
                        filterMap.put(keyColumn, resultAndLimitsDataSet.getString(i, "keyid" + (j + 1)));
                    }
                    ++j;
                }
                DataSet filteredDataSet = primaryDataSet.getFilteredDataSet(filterMap);
                for (int k = 0; k < filteredDataSet.getRowCount(); ++k) {
                    if (k == 0) {
                        this.addValuesToDataSet(resultAndLimitsDataSet, i, filteredDataSet, k, primaryColumns);
                        continue;
                    }
                    resultAndLimitsDataSet.copyRow(i, 1);
                    int newRowNum = resultAndLimitsDataSet.getRowCount() - 1;
                    this.addValuesToDataSet(resultAndLimitsDataSet, newRowNum, filteredDataSet, k, primaryColumns);
                }
                continue;
            }
            int primaryRowNum = (Integer)index.get(key.toString());
            this.addValuesToDataSet(resultAndLimitsDataSet, i, primaryDataSet, primaryRowNum, primaryColumns);
        }
    }

    private void addValuesToDataSet(DataSet resultDataSet, int resultRowNum, DataSet primaryDataSet, int primaryDataRowNum, Set<String> primaryColumns) {
        for (String columnId : primaryColumns) {
            Object value = primaryDataSet.getObject(primaryDataRowNum, columnId);
            resultDataSet.setObject(resultRowNum, columnId, value);
        }
    }

    private void addColumnsToDataSet(DataSet target, Set<String> primaryColumns, DataSet source) {
        for (String columnId : primaryColumns) {
            int columnType = source.getColumnType(columnId);
            if (columnType == -1) continue;
            target.addColumn(columnId, source.getColumnType(columnId));
        }
    }

    private List<String> getKeyColumns(String sdcId) {
        HashMap props = this.getSDCProcessor().getSDCProperties(sdcId);
        ArrayList<String> retVal = new ArrayList<String>();
        String keyColId1 = (String)props.get("keycolid1");
        String keyColId2 = (String)props.get("keycolid2");
        String keyColId3 = (String)props.get("keycolid3");
        retVal.add(keyColId1);
        if (keyColId2 != null && !keyColId2.equals("")) {
            retVal.add(keyColId2);
        }
        if (keyColId3 != null && !keyColId3.equals("")) {
            retVal.add(keyColId3);
        }
        return retVal;
    }
}

