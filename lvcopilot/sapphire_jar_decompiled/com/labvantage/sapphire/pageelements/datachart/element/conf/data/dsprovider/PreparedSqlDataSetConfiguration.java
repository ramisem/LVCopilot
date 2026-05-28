/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.StandardDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.PreparedSqlParamConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.IntegerExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PreparedSqlDataSetConfiguration
implements Serializable {
    private static final String DEFAULT_EXTENDED_DATA_TYPES = "N";
    private final StringExpression sdcId;
    private final StringExpression select;
    private final StringExpression from;
    private final StringExpression where;
    private final StringExpression groupBy;
    private final StringExpression orderBy;
    private final boolean extendedDataTypes;
    private final IntegerExpression maxRowCount;
    private final StandardDataSetProviderConfiguration parent;
    private final List<PreparedSqlParamConfiguration> preparedSqlParamConfList;

    public PreparedSqlDataSetConfiguration(PropertyList preparedSqlDataSetProps, StandardDataSetProviderConfiguration parent) {
        if (preparedSqlDataSetProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.sdcId = new StringExpression(preparedSqlDataSetProps.getProperty("sdcid"));
        this.select = new StringExpression(preparedSqlDataSetProps.getProperty("select"));
        this.from = new StringExpression(preparedSqlDataSetProps.getProperty("from"));
        this.where = new StringExpression(preparedSqlDataSetProps.getProperty("where"));
        this.groupBy = new StringExpression(preparedSqlDataSetProps.getProperty("groupby"));
        this.orderBy = new StringExpression(preparedSqlDataSetProps.getProperty("orderby"));
        this.extendedDataTypes = preparedSqlDataSetProps.getProperty("extendeddatatypes", DEFAULT_EXTENDED_DATA_TYPES).toLowerCase().startsWith("y");
        this.maxRowCount = new IntegerExpression(preparedSqlDataSetProps.getProperty("maxrowcount"));
        PropertyListCollection preparedSqlParamCollection = preparedSqlDataSetProps.getCollectionNotNull("preparedsqlparamcollection");
        this.preparedSqlParamConfList = new ArrayList<PreparedSqlParamConfiguration>();
        for (int i = 0; i < preparedSqlParamCollection.size(); ++i) {
            this.preparedSqlParamConfList.add(new PreparedSqlParamConfiguration(preparedSqlParamCollection.getPropertyList(i), this));
        }
    }

    public IntegerExpression getMaxRowCount() {
        return this.maxRowCount;
    }

    public boolean extendedDataTypes() {
        return this.extendedDataTypes;
    }

    public StringExpression getSdcId() {
        return this.sdcId;
    }

    public StringExpression getSelect() {
        return this.select;
    }

    public StringExpression getFrom() {
        return this.from;
    }

    public StringExpression getWhere() {
        return this.where;
    }

    public StringExpression getGroupBy() {
        return this.groupBy;
    }

    public StringExpression getOrderBy() {
        return this.orderBy;
    }

    public List<PreparedSqlParamConfiguration> getPreparedSqlParamConfList() {
        return this.preparedSqlParamConfList;
    }

    public StandardDataSetProviderConfiguration getParent() {
        return this.parent;
    }
}

