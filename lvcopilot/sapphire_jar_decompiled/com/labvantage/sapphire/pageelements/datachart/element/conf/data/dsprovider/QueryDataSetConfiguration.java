/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.StandardDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.QueryArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class QueryDataSetConfiguration
implements Serializable {
    private final String basedOnId;
    private final String queryId;
    private final StandardDataSetProviderConfiguration parent;
    private final List<QueryArgumentConfiguration> queryArguments;
    private final String plotIdColumn;
    private final StringExpression fromClause;
    private final StringExpression whereClause;
    private final StringExpression orderByClause;
    private final String plotIdColumnValue;
    private static final String DEFAULT_PLOT_ID_COLUMN = "plotid";
    private static final String DEFAULT_PLOT_ID_VALUE = "1";

    public QueryDataSetConfiguration(PropertyList queryDataSetProps, StandardDataSetProviderConfiguration parent) {
        if (queryDataSetProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (!queryDataSetProps.containsKey("jUnit") && parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.basedOnId = queryDataSetProps.getProperty("basedonid", "");
        this.queryId = queryDataSetProps.getProperty("queryid", "");
        this.plotIdColumn = queryDataSetProps.getProperty("plotidcolumn", DEFAULT_PLOT_ID_COLUMN);
        this.plotIdColumnValue = queryDataSetProps.getProperty("plotidcolumnvalue", DEFAULT_PLOT_ID_VALUE);
        this.parent = parent;
        this.queryArguments = new ArrayList<QueryArgumentConfiguration>();
        PropertyListCollection queryArgsConfig = queryDataSetProps.getCollectionNotNull("queryparams");
        for (int i = 0; i < queryArgsConfig.size(); ++i) {
            PropertyList queryArgConfig = queryArgsConfig.getPropertyList(i);
            QueryArgumentConfiguration argumentConfiguration = new QueryArgumentConfiguration(queryArgConfig);
            this.queryArguments.add(argumentConfiguration);
        }
        this.fromClause = new StringExpression(queryDataSetProps.getProperty("fromclause"));
        this.whereClause = new StringExpression(queryDataSetProps.getProperty("whereclause"));
        this.orderByClause = new StringExpression(queryDataSetProps.getProperty("orderbyclause"));
    }

    public String getBasedOnId() {
        return this.basedOnId;
    }

    public String getQueryId() {
        return this.queryId;
    }

    public String getPlotIdColumn() {
        return this.plotIdColumn;
    }

    public String getPlotIdColumnValue() {
        return this.plotIdColumnValue;
    }

    public StringExpression getFromClause() {
        return this.fromClause;
    }

    public StringExpression getWhereClause() {
        return this.whereClause;
    }

    public StringExpression getOrderByClause() {
        return this.orderByClause;
    }

    public StandardDataSetProviderConfiguration getParent() {
        return this.parent;
    }

    public List<QueryArgumentConfiguration> getQueryArguments() {
        return this.queryArguments;
    }
}

