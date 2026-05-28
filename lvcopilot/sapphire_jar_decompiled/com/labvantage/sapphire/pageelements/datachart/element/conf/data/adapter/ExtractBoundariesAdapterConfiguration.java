/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.BoundaryConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ExtractBoundariesAdapterConfiguration
implements Serializable {
    private static final String DEFAULT_ADD_EMPTY_VALUES = "Y";
    private final StandardAdapterConfiguration parent;
    private final String seriesGroupIdColumnId;
    private final String seriesIdColumnId;
    private final StringExpression primaryValueColumnId;
    private final String boundaryKeyColumnId;
    private final boolean addEmptyValues;
    private final List<BoundaryConfiguration> boundaryConfList;
    private final List<ColumnConfiguration> boundaryExtraKeyColumnConfList;
    private final List<ColumnConfiguration> primaryKeyColumnConfList;
    private final List<ColumnConfiguration> sortColumnConfList;

    public ExtractBoundariesAdapterConfiguration(PropertyList extractBoundariesAdapterProps, StandardAdapterConfiguration parent) {
        if (extractBoundariesAdapterProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.seriesGroupIdColumnId = parent.getParent().getParent().getParent().getDataSetConfiguration().getSeriesGroupIdColumn();
        this.seriesIdColumnId = parent.getParent().getParent().getParent().getDataSetConfiguration().getSeriesIdColumn();
        this.primaryValueColumnId = new StringExpression(extractBoundariesAdapterProps.getProperty("valuecolumn"));
        this.addEmptyValues = extractBoundariesAdapterProps.getProperty("boundarygapsenabled", DEFAULT_ADD_EMPTY_VALUES).toLowerCase().startsWith("y");
        this.boundaryKeyColumnId = extractBoundariesAdapterProps.getProperty("boundarykeycolumn", "");
        this.boundaryConfList = new ArrayList<BoundaryConfiguration>();
        PropertyListCollection boundaryCollection = extractBoundariesAdapterProps.getCollectionNotNull("boundarycollection");
        for (int i = 0; i < boundaryCollection.size(); ++i) {
            PropertyList boundaryProps = boundaryCollection.getPropertyList(i);
            this.boundaryConfList.add(new BoundaryConfiguration(boundaryProps, this));
        }
        this.boundaryExtraKeyColumnConfList = new ArrayList<ColumnConfiguration>();
        PropertyListCollection boundaryKeyColumnCollection = extractBoundariesAdapterProps.getCollectionNotNull("boundarykeycolumns");
        for (int i = 0; i < boundaryKeyColumnCollection.size(); ++i) {
            PropertyList boundaryKeyColumnProps = boundaryKeyColumnCollection.getPropertyList(i);
            this.boundaryExtraKeyColumnConfList.add(new ColumnConfiguration(boundaryKeyColumnProps));
        }
        this.primaryKeyColumnConfList = new ArrayList<ColumnConfiguration>();
        PropertyListCollection primaryKeyColumnsCollection = extractBoundariesAdapterProps.getCollectionNotNull("keycolumns");
        for (int i = 0; i < primaryKeyColumnsCollection.size(); ++i) {
            PropertyList primaryKeyColumnProps = primaryKeyColumnsCollection.getPropertyList(i);
            this.primaryKeyColumnConfList.add(new ColumnConfiguration(primaryKeyColumnProps));
        }
        this.sortColumnConfList = new ArrayList<ColumnConfiguration>();
        PropertyListCollection sortColumnCollection = extractBoundariesAdapterProps.getCollectionNotNull("sortcolumns");
        for (int i = 0; i < sortColumnCollection.size(); ++i) {
            PropertyList sortColumnProps = sortColumnCollection.getPropertyList(i);
            this.sortColumnConfList.add(new ColumnConfiguration(sortColumnProps));
        }
    }

    public List<BoundaryConfiguration> getBoundaryConfList() {
        return this.boundaryConfList;
    }

    public String getSeriesGroupIdColumnId() {
        return this.seriesGroupIdColumnId;
    }

    public StringExpression getPrimaryValueColumnId() {
        return this.primaryValueColumnId;
    }

    public boolean addEmptyValues() {
        return this.addEmptyValues;
    }

    public List<ColumnConfiguration> getBoundaryExtraKeyColumnConfList() {
        return this.boundaryExtraKeyColumnConfList;
    }

    public List<ColumnConfiguration> getPrimaryKeyColumnConfList() {
        return this.primaryKeyColumnConfList;
    }

    public List<ColumnConfiguration> getSortColumnConfList() {
        return this.sortColumnConfList;
    }

    public StandardAdapterConfiguration getParent() {
        return this.parent;
    }

    public String getBoundaryKeyColumnId() {
        return this.boundaryKeyColumnId;
    }

    public String getSeriesIdColumnId() {
        return this.seriesIdColumnId;
    }
}

