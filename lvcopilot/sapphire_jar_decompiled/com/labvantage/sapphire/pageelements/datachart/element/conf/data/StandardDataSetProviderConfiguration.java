/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.DataItemDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.QueryDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.SDIListDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.SqlDataSetConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class StandardDataSetProviderConfiguration
implements Serializable {
    private static final String DEFAULT_DATA_SET_TYPE = StandardDataSetType.SQL.getName();
    private final StandardDataSetType standardDataSetType;
    private final SqlDataSetConfiguration sqlDataSetConf;
    private final QueryDataSetConfiguration queryDataSetConf;
    private final DataItemDataSetConfiguration dataItemDataSetConf;
    private final PreparedSqlDataSetConfiguration preparedSqlDataSetConf;
    private final SDIListDataSetProviderConfiguration sdiListDataSetProviderConf;
    private final DataSetProviderConfiguration parent;

    public StandardDataSetProviderConfiguration(PropertyList standardDataSetProps, DataSetProviderConfiguration parent) {
        if (standardDataSetProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.standardDataSetType = StandardDataSetType.fromString(standardDataSetProps.getProperty("standarddatasettype", DEFAULT_DATA_SET_TYPE));
        this.sqlDataSetConf = this.standardDataSetType == StandardDataSetType.SQL ? new SqlDataSetConfiguration(standardDataSetProps.getPropertyListNotNull("sqldatasetprops"), this) : null;
        this.queryDataSetConf = this.standardDataSetType == StandardDataSetType.QUERY ? new QueryDataSetConfiguration(standardDataSetProps.getPropertyListNotNull("querydatasetprops"), this) : null;
        this.dataItemDataSetConf = this.standardDataSetType == StandardDataSetType.DATA_ITEM ? new DataItemDataSetConfiguration(standardDataSetProps.getPropertyListNotNull("dataitemdatasetprops"), this) : null;
        this.preparedSqlDataSetConf = this.standardDataSetType == StandardDataSetType.PREPARED_SQL ? new PreparedSqlDataSetConfiguration(standardDataSetProps.getPropertyListNotNull("preparedsqldatasetprops"), this) : null;
        this.sdiListDataSetProviderConf = this.standardDataSetType == StandardDataSetType.SDI_LIST ? new SDIListDataSetProviderConfiguration(standardDataSetProps.getPropertyListNotNull("sdilistdatasetprops"), this) : null;
        this.parent = parent;
    }

    public DataSetProviderConfiguration getParent() {
        return this.parent;
    }

    public SqlDataSetConfiguration getSqlDataSetConfiguration() {
        if (this.sqlDataSetConf == null) {
            throw new IllegalStateException("Data set type is: " + (Object)((Object)this.standardDataSetType));
        }
        return this.sqlDataSetConf;
    }

    public QueryDataSetConfiguration getQueryDataSetConfiguration() {
        if (this.queryDataSetConf == null) {
            throw new IllegalStateException("Data set type is: " + (Object)((Object)this.standardDataSetType));
        }
        return this.queryDataSetConf;
    }

    public PreparedSqlDataSetConfiguration getPreparedSqlDataSetConfiguration() {
        if (this.preparedSqlDataSetConf == null) {
            throw new IllegalStateException("Data set type is: " + (Object)((Object)this.standardDataSetType));
        }
        return this.preparedSqlDataSetConf;
    }

    public SDIListDataSetProviderConfiguration getSDIListDataSetConfiguration() {
        if (this.sdiListDataSetProviderConf == null) {
            throw new IllegalStateException("Data set type is: " + (Object)((Object)this.standardDataSetType));
        }
        return this.sdiListDataSetProviderConf;
    }

    public DataItemDataSetConfiguration getDataItemDataSetConf() {
        return this.dataItemDataSetConf;
    }

    public StandardDataSetType getDataSetType() {
        return this.standardDataSetType;
    }

    public static enum StandardDataSetType {
        SQL("SQL"),
        QUERY("Query"),
        DATA_ITEM("Data Item"),
        PREPARED_SQL("Prepared SQL"),
        SDI_LIST("SDI List");

        private final String name;

        private StandardDataSetType(String name) {
            this.name = name;
        }

        public static StandardDataSetType fromString(String name) {
            if (name != null) {
                for (StandardDataSetType type : StandardDataSetType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

