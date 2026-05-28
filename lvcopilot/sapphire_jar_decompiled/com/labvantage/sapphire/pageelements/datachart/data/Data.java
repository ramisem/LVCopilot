/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.general.Dataset
 */
package com.labvantage.sapphire.pageelements.datachart.data;

import com.labvantage.sapphire.pageelements.datachart.data.DefaultCategoryDatasetBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.DefaultPieDatasetBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.TimePeriodValuesCollectionBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.data.XYSeriesCollectionBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.YIntervalSeriesCollectionBuilder;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.DataSetAdapterProcessor;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.CustomDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.DataItemDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.DataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.PreparedSqlDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.QueryDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.SDIListDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.SqlDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.InputConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.OutputConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.SeriesGroupBuilderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.StandardDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.DataSetAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.CustomDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jfree.data.general.Dataset;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public final class Data
implements Serializable {
    private final ConnectionInfo connectionInfo;
    private final DataConfiguration dataConf;
    private final List<TraceableSeriesGroup> traceableSeriesGroupList;
    private final PropertyList adapterOutputProps;
    private final String seriesGroupBuilderId;
    private PropertyList dataSetProviderOutputProps;
    private DataSet dataSet;
    private List<TraceableSeriesGroup> tempTraceableSeriesGroupList;
    private PlotConfiguration.PlotType plotType;
    private Set<String> plotIdSet;
    private String dataSetProviderRSetId;

    public Data(DataConfiguration dataConf, DataBindingMap dataBindingMap, ConnectionInfo connectionInfo) throws SapphireException {
        this(dataConf, dataBindingMap, null, connectionInfo);
    }

    public Data(DataConfiguration dataConf, DataBindingMap dataBindingMap, DataSet dataset, ConnectionInfo connectionInfo) throws SapphireException {
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        if (dataConf == null) {
            throw new IllegalArgumentException("Data configuration is null");
        }
        if (dataBindingMap == null) {
            throw new IllegalArgumentException("Data binding map is null");
        }
        this.dataConf = dataConf;
        this.connectionInfo = connectionInfo;
        this.traceableSeriesGroupList = new ArrayList<TraceableSeriesGroup>();
        this.tempTraceableSeriesGroupList = new ArrayList<TraceableSeriesGroup>();
        this.adapterOutputProps = new PropertyList();
        this.dataSetProviderOutputProps = new PropertyList();
        this.dataSetProviderRSetId = "";
        this.dataSet = dataset == null ? this.buildDataSet(dataBindingMap) : dataset;
        this.plotIdSet = new LinkedHashSet<String>();
        this.seriesGroupBuilderId = dataConf.getSeriesGroupBuilderId().evaluate(dataBindingMap);
        if (this.dataSet == null) {
            this.dataSet = new DataSet();
        }
        if (this.dataSet.getRowCount() > 0) {
            this.dataSet = this.runDataSetAdapters(this.dataSet, dataBindingMap);
        }
        if (this.dataSet.getRowCount() > 0) {
            this.buildTraceableSeriesGroupList();
        } else {
            this.plotType = PlotConfiguration.PlotType.NO_DATA_PLOT;
        }
        if (!this.dataSetProviderRSetId.isEmpty()) {
            DAMProcessor damProcessor = new DAMProcessor(connectionInfo.getConnectionId());
            damProcessor.clearRSet(this.dataSetProviderRSetId);
        }
    }

    public Data(DataConfiguration dataConf, ConnectionInfo connectionInfo) {
        this.dataConf = dataConf;
        this.traceableSeriesGroupList = new ArrayList<TraceableSeriesGroup>();
        this.seriesGroupBuilderId = "";
        this.connectionInfo = connectionInfo;
        this.adapterOutputProps = new PropertyList();
        this.dataSetProviderOutputProps = new PropertyList();
        this.dataSetProviderRSetId = "";
        this.dataSet = new DataSet();
        this.tempTraceableSeriesGroupList = new ArrayList<TraceableSeriesGroup>();
        this.plotType = PlotConfiguration.PlotType.NO_DATA_PLOT;
        this.plotIdSet = new HashSet<String>();
    }

    private void buildTraceableSeriesGroupList() {
        DataSetConfiguration dataSetConf = this.dataConf.getDataSetConfiguration();
        String plotIdColumnId = dataSetConf.getPlotIdColumn();
        String seriesGroupColumnId = dataSetConf.getSeriesGroupIdColumn();
        if (this.dataSet.getColumnType(plotIdColumnId) == -1) {
            throw new IllegalArgumentException("Data set does not contain a column for plot ID: " + plotIdColumnId);
        }
        if (this.dataSet.getColumnType(seriesGroupColumnId) == -1) {
            throw new IllegalArgumentException("Data set does not contain a column for series group ID: " + seriesGroupColumnId);
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        Set<String> plotIdSet = Util.getColumnValueList(this.dataSet, plotIdColumnId);
        for (String plotId : plotIdSet) {
            filter.clear();
            filter.put(plotIdColumnId, plotId);
            DataSet plotDataSet = this.dataSet.getFilteredDataSet(filter);
            Set<String> seriesGroupIdSet = Util.getColumnValueList(this.dataSet, seriesGroupColumnId);
            for (String seriesGroupId : seriesGroupIdSet) {
                filter.clear();
                filter.put(seriesGroupColumnId, seriesGroupId);
                DataSet seriesGroupDataSet = plotDataSet.getFilteredDataSet(filter);
                SeriesGroupBuilderConfiguration seriesGroupBuilderConf = dataSetConf.getPlotConfiguration(plotId).getSeriesGroupConfiguration(seriesGroupId).getSeriesGroupBuilderConfiguration(this.seriesGroupBuilderId);
                SeriesGroupBuilderConfiguration.SeriesGroupBuilderType seriesGroupBuilderType = seriesGroupBuilderConf.getSeriesGroupBuilderType();
                if (seriesGroupDataSet.getRowCount() <= 0) continue;
                this.createTraceableSeriesGroup(seriesGroupBuilderConf, seriesGroupDataSet, seriesGroupBuilderType);
                this.supervisePlotType(seriesGroupBuilderType.getPlotType());
            }
        }
    }

    private void createTraceableSeriesGroup(SeriesGroupBuilderConfiguration seriesGroupBuilderConf, DataSet seriesGroupDataSet, SeriesGroupBuilderConfiguration.SeriesGroupBuilderType seriesGroupBuilderType) {
        TraceableSeriesGroup traceableSeriesGroup;
        if (seriesGroupBuilderType == SeriesGroupBuilderConfiguration.SeriesGroupBuilderType.XY_SERIES_COLLECTION) {
            XYSeriesCollectionBuilder xySeriesCollectionBuilder = new XYSeriesCollectionBuilder(seriesGroupDataSet, seriesGroupBuilderConf.getXYSeriesCollectionConfiguration());
            traceableSeriesGroup = xySeriesCollectionBuilder.getTraceableSeriesGroup();
        } else if (seriesGroupBuilderType == SeriesGroupBuilderConfiguration.SeriesGroupBuilderType.TIME_PERIOD_VALUES_COLLECTION) {
            TimePeriodValuesCollectionBuilder timePeriodValuesCollectionBuilder = new TimePeriodValuesCollectionBuilder(seriesGroupDataSet, seriesGroupBuilderConf.getTimePeriodValuesCollectionConfiguration(), this.connectionInfo);
            traceableSeriesGroup = timePeriodValuesCollectionBuilder.getTraceableSeriesGroup();
        } else if (seriesGroupBuilderType == SeriesGroupBuilderConfiguration.SeriesGroupBuilderType.DEFAULT_CATEGORY_DATASET) {
            DefaultCategoryDatasetBuilder defaultCategoryDatasetBuilder = new DefaultCategoryDatasetBuilder(seriesGroupDataSet, this.dataSet, seriesGroupBuilderConf.getDefaultCategoryDatasetConfiguration());
            traceableSeriesGroup = defaultCategoryDatasetBuilder.getTraceableSeriesGroup();
        } else if (seriesGroupBuilderType == SeriesGroupBuilderConfiguration.SeriesGroupBuilderType.DEFAULT_PIE_DATASET) {
            DefaultPieDatasetBuilder defaultPieDatasetBuilder = new DefaultPieDatasetBuilder(seriesGroupDataSet, seriesGroupBuilderConf.getDefaultPieDatasetConfiguration());
            traceableSeriesGroup = defaultPieDatasetBuilder.getTraceableSeriesGroup();
        } else if (seriesGroupBuilderType == SeriesGroupBuilderConfiguration.SeriesGroupBuilderType.Y_INTERVAL_COLLECTION) {
            YIntervalSeriesCollectionBuilder yIntervalSeriesCollectionBuilder = new YIntervalSeriesCollectionBuilder(seriesGroupDataSet, seriesGroupBuilderConf.getYIntervalSeriesCollectionConfiguration());
            traceableSeriesGroup = yIntervalSeriesCollectionBuilder.getTraceableSeriesGroup();
        } else {
            throw new IllegalArgumentException("Unknown series group type: " + (Object)((Object)seriesGroupBuilderType));
        }
        this.traceableSeriesGroupList.add(traceableSeriesGroup);
        this.plotIdSet.add(traceableSeriesGroup.getPlotId());
    }

    private void supervisePlotType(PlotConfiguration.PlotType seriesGroupPlotType) {
        if (this.plotType == null) {
            this.plotType = seriesGroupPlotType;
        } else if (this.plotType != seriesGroupPlotType) {
            throw new IllegalArgumentException("Series group plot type suggests " + (Object)((Object)seriesGroupPlotType) + " but another type of series group was already added to plot: " + (Object)((Object)this.plotType));
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private DataSet buildDataSet(DataBindingMap dataBindingMap) throws SapphireException {
        DataSetProvider dataSetProvider;
        DataSetProviderConfiguration dataSetConf = this.dataConf.getDataSetProviderConf();
        DataSetProviderConfiguration.DataSetType dataSetType = dataSetConf.getDataSetType();
        if (dataSetType == DataSetProviderConfiguration.DataSetType.STANDARD) {
            StandardDataSetProviderConfiguration standardDataSetConf = dataSetConf.getStandardDataSetProviderConfiguration();
            StandardDataSetProviderConfiguration.StandardDataSetType standardDataSetType = standardDataSetConf.getDataSetType();
            if (standardDataSetType == StandardDataSetProviderConfiguration.StandardDataSetType.SQL) {
                dataSetProvider = new SqlDataSetProvider(standardDataSetConf.getSqlDataSetConfiguration(), this.connectionInfo.getConnectionId(), dataBindingMap);
            } else if (standardDataSetType == StandardDataSetProviderConfiguration.StandardDataSetType.QUERY) {
                dataSetProvider = new QueryDataSetProvider(this.connectionInfo.getConnectionId(), standardDataSetConf.getQueryDataSetConfiguration(), dataBindingMap);
            } else if (standardDataSetType == StandardDataSetProviderConfiguration.StandardDataSetType.DATA_ITEM) {
                dataSetProvider = new DataItemDataSetProvider(this.connectionInfo.getConnectionId(), standardDataSetConf.getDataItemDataSetConf(), dataBindingMap);
            } else if (standardDataSetType == StandardDataSetProviderConfiguration.StandardDataSetType.PREPARED_SQL) {
                dataSetProvider = new PreparedSqlDataSetProvider(standardDataSetConf.getPreparedSqlDataSetConfiguration(), this.connectionInfo.getConnectionId(), dataBindingMap);
            } else {
                if (standardDataSetType != StandardDataSetProviderConfiguration.StandardDataSetType.SDI_LIST) throw new IllegalArgumentException("Unknown standard data set provider type: " + (Object)((Object)standardDataSetType));
                dataSetProvider = new SDIListDataSetProvider(standardDataSetConf.getSDIListDataSetConfiguration(), this.connectionInfo.getConnectionId(), dataBindingMap);
            }
        } else {
            if (dataSetType != DataSetProviderConfiguration.DataSetType.CUSTOM) throw new IllegalArgumentException("Unknown data set provider type: " + (Object)((Object)dataSetType));
            dataSetProvider = Data.createCustomDataSetProvider(dataSetConf.getCustomDataSetConfiguration(), this.connectionInfo.getConnectionId(), dataBindingMap);
        }
        DataSet returnDataSet = dataSetProvider.getDataSet();
        this.dataSetProviderOutputProps = dataSetProvider.getOutputProps();
        this.dataSetProviderRSetId = dataSetProvider.getRSetId();
        dataBindingMap.setDataSetProviderOutputProperties(this.dataSetProviderOutputProps);
        dataBindingMap.setDataSetProviderRSetId(this.dataSetProviderRSetId);
        return returnDataSet;
    }

    public String getSeriesGroupBuilderId() {
        return this.seriesGroupBuilderId;
    }

    public static DataSetProvider createCustomDataSetProvider(CustomDataSetConfiguration customDataSetConfiguration, String connectionId, DataBindingMap dataBindingMap) {
        CustomDataSetProvider customDataSetProvider;
        Class<?> cl;
        String className = customDataSetConfiguration.getClassName();
        try {
            cl = Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + className);
        }
        try {
            customDataSetProvider = (CustomDataSetProvider)cl.newInstance();
            customDataSetProvider.setConnectionId(connectionId);
            PropertyList inputProperties = new PropertyList();
            List<InputConfiguration> inputConfList = customDataSetConfiguration.getInputConfigurationList();
            for (InputConfiguration inputConf : inputConfList) {
                inputProperties.setProperty(inputConf.getPropertyId(), inputConf.getPropertyValue().evaluateNoException(dataBindingMap));
            }
            PropertyList outputProperties = new PropertyList();
            List<OutputConfiguration> outputConfList = customDataSetConfiguration.getOutputConfigurationList();
            for (OutputConfiguration outputConf : outputConfList) {
                outputProperties.setProperty(outputConf.getPropertyId(), outputConf.getVariable());
            }
            customDataSetProvider.setInputProperties(inputProperties);
            customDataSetProvider.setOutputProperties(outputProperties);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class not accessible: " + className);
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Class not instantiable: " + className);
        }
        return customDataSetProvider;
    }

    private DataSet runDataSetAdapters(DataSet ds, DataBindingMap dataBindingMap) {
        DataSetAdapterConfiguration dataSetAdapterConf = this.dataConf.getDatasetAdapterConf();
        List adapterConfList = (List)((ArrayList)dataSetAdapterConf.getAdapterConfigurationList()).clone();
        DataSetAdapterProcessor adapterProcessor = new DataSetAdapterProcessor(this.connectionInfo.getConnectionId());
        for (AdapterConfiguration adapterConf : adapterConfList) {
            if (!adapterConf.isEnabled() || adapterConf.getAdapterId().isEmpty()) continue;
            adapterProcessor.processDataSetAdapter(adapterConf, ds, dataBindingMap);
            ds = adapterProcessor.getProcessedDataSet();
            this.adapterOutputProps.setProperty(adapterConf.getAdapterId(), adapterProcessor.getOutputProps());
        }
        return ds;
    }

    public DataConfiguration getDataConfiguration() {
        return this.dataConf;
    }

    public DataSet getDataSet() {
        return this.dataSet;
    }

    public TraceableSeriesGroup getTraceableSeriesGroup(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index < 0");
        }
        if (index > this.traceableSeriesGroupList.size() + this.tempTraceableSeriesGroupList.size()) {
            throw new IllegalArgumentException("Index (" + index + ") > list size (" + (this.traceableSeriesGroupList.size() + this.tempTraceableSeriesGroupList.size()) + ")");
        }
        if (index < this.traceableSeriesGroupList.size()) {
            return this.traceableSeriesGroupList.get(index);
        }
        return this.tempTraceableSeriesGroupList.get(index - this.traceableSeriesGroupList.size());
    }

    public int getTraceableSeriesGroupCount() {
        return this.traceableSeriesGroupList.size() + this.tempTraceableSeriesGroupList.size();
    }

    public TraceableSeriesGroup getTraceableSeriesGroup(String plotId, String seriesGroupId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        if (seriesGroupId == null || seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is null or empty: " + seriesGroupId);
        }
        for (TraceableSeriesGroup traceableSeriesGroup : this.traceableSeriesGroupList) {
            if (!traceableSeriesGroup.getPlotId().equals(plotId) || !traceableSeriesGroup.getSeriesGroupId().equals(seriesGroupId)) continue;
            return traceableSeriesGroup;
        }
        for (TraceableSeriesGroup traceableSeriesGroup : this.tempTraceableSeriesGroupList) {
            if (!traceableSeriesGroup.getPlotId().equals(plotId) || !traceableSeriesGroup.getSeriesGroupId().equals(seriesGroupId)) continue;
            return traceableSeriesGroup;
        }
        throw new IllegalArgumentException("Series group ID not found: " + seriesGroupId);
    }

    public PlotConfiguration.PlotType getPlotType() {
        return this.plotType;
    }

    public TraceableSeriesGroup getTraceableSeriesGroup(Dataset dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset is null");
        }
        for (TraceableSeriesGroup traceableSeriesGroup : this.traceableSeriesGroupList) {
            if (!traceableSeriesGroup.getSeriesGroup().equals(dataset)) continue;
            return traceableSeriesGroup;
        }
        for (TraceableSeriesGroup traceableSeriesGroup : this.tempTraceableSeriesGroupList) {
            if (!traceableSeriesGroup.getSeriesGroup().equals(dataset)) continue;
            return traceableSeriesGroup;
        }
        throw new IllegalArgumentException("Dataset not found: " + dataset);
    }

    public boolean hasTraceableSeriesGroup(Dataset dataset) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset is null");
        }
        for (TraceableSeriesGroup traceableSeriesGroup : this.traceableSeriesGroupList) {
            if (!traceableSeriesGroup.getSeriesGroup().equals(dataset)) continue;
            return true;
        }
        for (TraceableSeriesGroup traceableSeriesGroup : this.tempTraceableSeriesGroupList) {
            if (!traceableSeriesGroup.getSeriesGroup().equals(dataset)) continue;
            return true;
        }
        return false;
    }

    public void resetTempTraceableSeriesGroups() {
        this.tempTraceableSeriesGroupList = new ArrayList<TraceableSeriesGroup>();
    }

    public void addTempTraceableSeriesGroup(TraceableSeriesGroup tempTraceableSeriesGroup) {
        if (tempTraceableSeriesGroup == null) {
            throw new IllegalArgumentException("Temporary traceable series group is null");
        }
        this.tempTraceableSeriesGroupList.add(tempTraceableSeriesGroup);
    }

    public PropertyList getAdapterOutputProperties() {
        return this.adapterOutputProps;
    }

    public PropertyList getDataSetProviderOutputProps() {
        return this.dataSetProviderOutputProps;
    }

    public boolean isEmpty() {
        return this.traceableSeriesGroupList.size() == 0;
    }

    public List<TraceableSeriesGroup> getTraceableSeriesGroupList(String plotId) {
        if (plotId == null) {
            throw new IllegalArgumentException("Plot ID is null");
        }
        if (plotId.isEmpty()) {
            throw new IllegalArgumentException("Plot ID is empty");
        }
        ArrayList<TraceableSeriesGroup> returnList = new ArrayList<TraceableSeriesGroup>();
        for (TraceableSeriesGroup traceableSeriesGroup : this.traceableSeriesGroupList) {
            if (!traceableSeriesGroup.getPlotId().equals(plotId)) continue;
            returnList.add(traceableSeriesGroup);
        }
        for (TraceableSeriesGroup traceableSeriesGroup : this.tempTraceableSeriesGroupList) {
            if (!traceableSeriesGroup.getPlotId().equals(plotId)) continue;
            returnList.add(traceableSeriesGroup);
        }
        return returnList;
    }

    public Set<String> getPlotIdSet() {
        return this.plotIdSet;
    }
}

