/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDIDataItemFilterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDIWorkItemFilterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.BooleanExpression;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class SDIDataItemAdapterConfiguration
implements Serializable {
    private static final String DEFAULT_INCLUDE_SPECS = "N";
    private static final String DEFAULT_FILTER_SEPARATOR = ";";
    private static final String DEFAULT_INCLUDE_OUTLIERS = "Y";
    private final StandardAdapterConfiguration parent;
    private final String sdcId;
    private final boolean releasedOnly;
    private final boolean ignoreCancelled;
    private final boolean ignoreRetested;
    private final boolean ignoreRemeasured;
    private final boolean resultEntered;
    private final boolean isIncludeAllPrimaryRows;
    private final BooleanExpression includeSpecs;
    private final BooleanExpression includeOutliers;
    private final boolean includeAllPrimaryColumns;
    private final List<ColumnConfiguration> columns;
    private final List<StringExpression> sortColumns;
    private final List<SDIDataItemFilterConfiguration> sdiDataItemFilterConfList;
    private final List<SDIWorkItemFilterConfiguration> sdiWorkItemFilterConfList;
    private final String filterSeparator;
    private final StringExpression rSetId;

    public SDIDataItemAdapterConfiguration(PropertyList sdiDataItemAdapterProps, StandardAdapterConfiguration parent) {
        if (sdiDataItemAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (!sdiDataItemAdapterProps.containsKey("jUnit") && parent == null) {
            throw new IllegalArgumentException("Parent configuration is null ");
        }
        this.parent = parent;
        this.sdcId = sdiDataItemAdapterProps.getProperty("sdcid", "");
        PropertyList filters = sdiDataItemAdapterProps.getPropertyList("filters");
        this.releasedOnly = filters.getProperty("releasedonly", DEFAULT_INCLUDE_OUTLIERS).equals(DEFAULT_INCLUDE_OUTLIERS);
        this.ignoreCancelled = filters.getProperty("ignorecancelled", DEFAULT_INCLUDE_OUTLIERS).equals(DEFAULT_INCLUDE_OUTLIERS);
        this.ignoreRetested = filters.getProperty("ignoreretested", DEFAULT_INCLUDE_SPECS).equals(DEFAULT_INCLUDE_OUTLIERS);
        this.ignoreRemeasured = filters.getProperty("ignoreremeasured", DEFAULT_INCLUDE_SPECS).equals(DEFAULT_INCLUDE_OUTLIERS);
        this.resultEntered = filters.getProperty("resultentered", DEFAULT_INCLUDE_OUTLIERS).equals(DEFAULT_INCLUDE_OUTLIERS);
        this.includeOutliers = new BooleanExpression(filters.getProperty("includeoutliers", DEFAULT_INCLUDE_OUTLIERS));
        this.includeSpecs = new BooleanExpression(sdiDataItemAdapterProps.getPropertyListNotNull("specs").getProperty("includespecs", DEFAULT_INCLUDE_SPECS));
        this.includeAllPrimaryColumns = sdiDataItemAdapterProps.getProperty("includeallprimarycolumns", DEFAULT_INCLUDE_SPECS).equals(DEFAULT_INCLUDE_OUTLIERS);
        this.isIncludeAllPrimaryRows = sdiDataItemAdapterProps.getProperty("includeallprimaryrows", DEFAULT_INCLUDE_SPECS).equals(DEFAULT_INCLUDE_OUTLIERS);
        this.rSetId = new StringExpression(sdiDataItemAdapterProps.getProperty("rsetid"));
        this.columns = new ArrayList<ColumnConfiguration>();
        PropertyListCollection columnConfig = sdiDataItemAdapterProps.getCollectionNotNull("columns");
        for (int i = 0; i < columnConfig.size(); ++i) {
            this.columns.add(new ColumnConfiguration(columnConfig.getPropertyList(i)));
        }
        this.filterSeparator = sdiDataItemAdapterProps.getProperty("filterseparator", DEFAULT_FILTER_SEPARATOR);
        this.sdiDataItemFilterConfList = new ArrayList<SDIDataItemFilterConfiguration>();
        PropertyListCollection sdiDataItemFilterConfig = filters.getCollectionNotNull("paramlistfilters");
        for (int i = 0; i < sdiDataItemFilterConfig.size(); ++i) {
            this.sdiDataItemFilterConfList.add(new SDIDataItemFilterConfiguration(sdiDataItemFilterConfig.getPropertyList(i), this));
        }
        this.sdiWorkItemFilterConfList = new ArrayList<SDIWorkItemFilterConfiguration>();
        PropertyListCollection workItemFilterConfig = filters.getCollectionNotNull("workitemfilters");
        for (int i = 0; i < workItemFilterConfig.size(); ++i) {
            this.sdiWorkItemFilterConfList.add(new SDIWorkItemFilterConfiguration(workItemFilterConfig.getPropertyList(i), this));
        }
        this.sortColumns = new ArrayList<StringExpression>();
        PropertyListCollection sortColumnConfig = sdiDataItemAdapterProps.getCollectionNotNull("sortcolumns");
        for (int i = 0; i < sortColumnConfig.size(); ++i) {
            String sortColumn = sortColumnConfig.getPropertyList(i).getProperty("columnid", "");
            if (sortColumn.equals("")) continue;
            this.sortColumns.add(new StringExpression(sortColumn));
        }
    }

    public StringExpression getRSetId() {
        return this.rSetId;
    }

    public String getFilterSeparator() {
        return this.filterSeparator;
    }

    public String getSdcId() {
        return this.sdcId;
    }

    public boolean isReleasedOnly() {
        return this.releasedOnly;
    }

    public boolean isIgnoreCancelled() {
        return this.ignoreCancelled;
    }

    public boolean isIgnoreRetested() {
        return this.ignoreRetested;
    }

    public boolean isIgnoreRemeasured() {
        return this.ignoreRemeasured;
    }

    public boolean isResultEntered() {
        return this.resultEntered;
    }

    public List<ColumnConfiguration> getColumnConfigurationList() {
        return this.columns;
    }

    public List<StringExpression> getSortColumns() {
        return this.sortColumns;
    }

    public List<SDIDataItemFilterConfiguration> getSDIDataItemFilterConfigurationList() {
        return this.sdiDataItemFilterConfList;
    }

    public List<SDIWorkItemFilterConfiguration> getSDIWorkItemFilterConfigurationList() {
        return this.sdiWorkItemFilterConfList;
    }

    public BooleanExpression getIncludeSpecs() {
        return this.includeSpecs;
    }

    public BooleanExpression getIncludeOutliers() {
        return this.includeOutliers;
    }

    public boolean includeAllPrimaryColumns() {
        return this.includeAllPrimaryColumns;
    }

    public boolean includeAllPrimaryRows() {
        return this.isIncludeAllPrimaryRows;
    }

    public StandardAdapterConfiguration getParent() {
        return this.parent;
    }
}

