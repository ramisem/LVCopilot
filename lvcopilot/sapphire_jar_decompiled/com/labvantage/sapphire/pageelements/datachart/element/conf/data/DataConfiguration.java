/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.DataSetAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class DataConfiguration
implements Serializable {
    private static final String DEFAULT_SERIES_GROUP_BUILDER_ID = "defaultbuilder";
    private final DataSetProviderConfiguration dataSetProviderConf;
    private final DataSetConfiguration dataSetConf;
    private final DataSetAdapterConfiguration dataSetAdapterConf;
    private final StringExpression seriesGroupBuilderId;

    public DataConfiguration(PropertyList dataProps) {
        if (dataProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        this.dataSetConf = new DataSetConfiguration(dataProps.getPropertyListNotNull("datasetseriesprops"), this);
        this.dataSetProviderConf = new DataSetProviderConfiguration(dataProps.getPropertyListNotNull("datasetprops"), this);
        this.dataSetAdapterConf = new DataSetAdapterConfiguration(dataProps.getPropertyListNotNull("datasetadapterprops"), this);
        this.seriesGroupBuilderId = new StringExpression(dataProps.getProperty("seriesgroupbuilderid", DEFAULT_SERIES_GROUP_BUILDER_ID));
    }

    public DataSetProviderConfiguration getDataSetProviderConf() {
        return this.dataSetProviderConf;
    }

    public DataSetConfiguration getDataSetConfiguration() {
        return this.dataSetConf;
    }

    public DataSetAdapterConfiguration getDatasetAdapterConf() {
        return this.dataSetAdapterConf;
    }

    public StringExpression getSeriesGroupBuilderId() {
        return this.seriesGroupBuilderId;
    }
}

