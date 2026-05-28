/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.AbstractDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.AccessControlAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.AddSDINotesAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.ColumnConfigurationAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.CustomDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.DataItemParamListKeyAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.DataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.ExtractBoundariesAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.SDIDataItemAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.SDISpecAdapter;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.SequenceAdapter;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.InputConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.OutputConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.CustomAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public final class DataSetAdapterProcessor
extends BaseAccessor {
    private DataSet processedDataSet;
    private PropertyList outputProps;

    public DataSetAdapterProcessor(String connectionId) {
        super(connectionId);
        if (connectionId == null || connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection ID is null or empty: " + connectionId);
        }
        this.processedDataSet = null;
    }

    public PropertyList getOutputProps() {
        if (this.processedDataSet == null) {
            throw new IllegalStateException("Data set not processed yet.");
        }
        return this.outputProps;
    }

    public void processDataSetAdapter(AdapterConfiguration adapterConf, DataSet dataSet, DataBindingMap dataBindingMap) {
        DataSetAdapter dataSetAdapter;
        AdapterConfiguration.AdapterType adapterType = adapterConf.getAdapterType();
        if (adapterType == AdapterConfiguration.AdapterType.CUSTOM) {
            CustomAdapterConfiguration customAdapterConf = adapterConf.getCustomAdapterConfiguration();
            dataSetAdapter = this.processCustomAdapter(customAdapterConf, dataSet, dataBindingMap);
        } else if (adapterType == AdapterConfiguration.AdapterType.STANDARD) {
            StandardAdapterConfiguration standardAdapterConf = adapterConf.getStandardAdapterConfiguration();
            dataSetAdapter = this.processStandardAdapter(standardAdapterConf, dataSet, dataBindingMap);
        } else {
            throw new IllegalArgumentException("Unknown adapter type: " + (Object)((Object)adapterType));
        }
        if (dataSetAdapter != null) {
            this.processedDataSet = dataSetAdapter.getProcessedDataSet();
            this.outputProps = dataSetAdapter.getOutputProperties();
        }
    }

    private DataSetAdapter processStandardAdapter(StandardAdapterConfiguration standardAdapterConf, DataSet dataSet, DataBindingMap dataBindingMap) {
        AbstractDataSetAdapter dataSetAdapter = null;
        if (standardAdapterConf.getStandardAdapterType() != StandardAdapterConfiguration.StandardAdapterType.NONE) {
            if (standardAdapterConf.getStandardAdapterType() == StandardAdapterConfiguration.StandardAdapterType.SDI_DATA_ITEM_ADAPTER) {
                dataSetAdapter = new SDIDataItemAdapter(this.getConnectionid(), dataBindingMap, standardAdapterConf.getSdiDataItemAdapterConfiguration());
            } else if (standardAdapterConf.getStandardAdapterType() == StandardAdapterConfiguration.StandardAdapterType.EXTRACT_BOUNDARIES_ADAPTER) {
                dataSetAdapter = new ExtractBoundariesAdapter(standardAdapterConf.getExtractBoundariesAdapterConfiguration(), this.getConnectionid(), dataBindingMap);
            } else if (standardAdapterConf.getStandardAdapterType() == StandardAdapterConfiguration.StandardAdapterType.COLUMN_CONFIGURATION_ADAPTER) {
                dataSetAdapter = new ColumnConfigurationAdapter(this.getConnectionid(), dataBindingMap, standardAdapterConf.getColumnConfigurationAdapterConfiguration());
            } else if (standardAdapterConf.getStandardAdapterType() == StandardAdapterConfiguration.StandardAdapterType.SDI_SPEC_ADAPTER) {
                dataSetAdapter = new SDISpecAdapter(this.getConnectionid(), dataBindingMap, standardAdapterConf.getSdiSpecAdapterConfiguration());
            } else if (standardAdapterConf.getStandardAdapterType() == StandardAdapterConfiguration.StandardAdapterType.ACCESS_CONTROL_ADAPTER) {
                dataSetAdapter = new AccessControlAdapter(this.getConnectionid(), dataBindingMap, standardAdapterConf.getAccessControlAdapterConfiguration());
            } else if (standardAdapterConf.getStandardAdapterType() == StandardAdapterConfiguration.StandardAdapterType.SEQUENCE) {
                dataSetAdapter = new SequenceAdapter(standardAdapterConf.getSequenceAdapterConfiguration(), this.getConnectionid(), dataBindingMap);
            } else if (standardAdapterConf.getStandardAdapterType() == StandardAdapterConfiguration.StandardAdapterType.ADD_SDI_NOTES) {
                dataSetAdapter = new AddSDINotesAdapter(this.getConnectionid(), dataBindingMap, standardAdapterConf.getAddSDINotesAdapterConfiguration());
            } else if (standardAdapterConf.getStandardAdapterType() == StandardAdapterConfiguration.StandardAdapterType.ADD_DATAITEM_PARAMLIST_KEY) {
                dataSetAdapter = new DataItemParamListKeyAdapter(this.getConnectionid(), dataBindingMap, standardAdapterConf.getDataItemParamListKeyAdapterConf());
            } else {
                throw new IllegalArgumentException("Unknown standard data set adapter: " + (Object)((Object)standardAdapterConf.getStandardAdapterType()));
            }
            dataBindingMap.setActiveDataSet(dataSet);
            try {
                dataSetAdapter.processDataSetAdapter(dataSet);
            }
            catch (SapphireException e) {
                Logger.logError("Error while processing standard adapter: " + e);
            }
            dataBindingMap.addAdapterOutputProperties(standardAdapterConf.getParent().getAdapterId(), dataSetAdapter.getOutputProperties());
        }
        return dataSetAdapter;
    }

    private DataSetAdapter processCustomAdapter(CustomAdapterConfiguration customAdapterConf, DataSet dataSet, DataBindingMap dataBindingMap) {
        CustomDataSetAdapter customDataSetAdapter;
        Class<?> cl;
        String className = customAdapterConf.getClassName();
        try {
            cl = Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class not found: " + className);
        }
        try {
            customDataSetAdapter = (CustomDataSetAdapter)cl.newInstance();
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class not accessible: " + className);
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Class not instantiable: " + className);
        }
        PropertyList inputProperties = new PropertyList();
        List<InputConfiguration> adapterInputConfList = customAdapterConf.getAdapterInputConfigurationList();
        for (InputConfiguration adapterInputConf : adapterInputConfList) {
            inputProperties.setProperty(adapterInputConf.getPropertyId(), adapterInputConf.getPropertyValue().evaluateNoException(dataBindingMap));
        }
        PropertyList outputProperties = new PropertyList();
        List<OutputConfiguration> adapterOutputConfList = customAdapterConf.getAdapterOutputConfigurationList();
        for (OutputConfiguration adapterOutputConf : adapterOutputConfList) {
            outputProperties.setProperty(adapterOutputConf.getPropertyId(), adapterOutputConf.getVariable());
        }
        customDataSetAdapter.setConnectionId(this.getConnectionid());
        customDataSetAdapter.setInputProperties(inputProperties);
        customDataSetAdapter.setOutputProperties(outputProperties);
        try {
            customDataSetAdapter.processDataSetAdapter(dataSet);
        }
        catch (SapphireException e) {
            throw new IllegalStateException("Failed to process custom adapter", e);
        }
        return customDataSetAdapter;
    }

    public DataSet getProcessedDataSet() {
        if (this.processedDataSet == null) {
            throw new IllegalStateException("Data set not processed yet.");
        }
        return this.processedDataSet;
    }
}

