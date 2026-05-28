/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelExportConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ExcelSortConfiguration
implements Serializable {
    private static final String DEFAULT_ROW_SORT_DIRECTION = SortDirection.ASCENDING.getName();
    private static final String DEFAULT_COL_SORT_DIRECTION = SortDirection.ASCENDING.getName();
    private final ExcelExportConfiguration parent;
    private final List<ExcelColumnConfiguration> rowHeaderSortList;
    private final List<ExcelColumnConfiguration> colHeaderSortList;
    private final SortDirection rowSortDirection;
    private final SortDirection colSortDirection;

    public ExcelSortConfiguration(PropertyList excelSortProps, ExcelExportConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (excelSortProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.rowSortDirection = SortDirection.fromString(excelSortProps.getProperty("rowsortdirection", DEFAULT_ROW_SORT_DIRECTION));
        this.colSortDirection = SortDirection.fromString(excelSortProps.getProperty("colsortdirection", DEFAULT_COL_SORT_DIRECTION));
        PropertyListCollection colHeaderSortCollection = excelSortProps.getCollectionNotNull("colheadersortcollection");
        this.colHeaderSortList = new ArrayList<ExcelColumnConfiguration>();
        for (int i = 0; i < colHeaderSortCollection.size(); ++i) {
            PropertyList colHeaderSortProps = colHeaderSortCollection.getPropertyList(i);
            this.colHeaderSortList.add(new ExcelColumnConfiguration(colHeaderSortProps));
        }
        PropertyListCollection rowHeaderSortCollection = excelSortProps.getCollectionNotNull("rowheadersortcollection");
        this.rowHeaderSortList = new ArrayList<ExcelColumnConfiguration>();
        for (int i = 0; i < rowHeaderSortCollection.size(); ++i) {
            PropertyList rowHeaderSortProps = rowHeaderSortCollection.getPropertyList(i);
            this.rowHeaderSortList.add(new ExcelColumnConfiguration(rowHeaderSortProps));
        }
        this.parent = parent;
    }

    public SortDirection getRowSortDirection() {
        return this.rowSortDirection;
    }

    public SortDirection getColSortDirection() {
        return this.colSortDirection;
    }

    public ExcelExportConfiguration getParent() {
        return this.parent;
    }

    public List<ExcelColumnConfiguration> getColHeaderSortList() {
        return this.colHeaderSortList;
    }

    public List<ExcelColumnConfiguration> getRowHeaderSortList() {
        return this.rowHeaderSortList;
    }

    public static enum SortDirection {
        ASCENDING("Ascending"),
        DESCENDING("Descending");

        private final String name;

        private SortDirection(String name) {
            this.name = name;
        }

        public static SortDirection fromString(String name) {
            if (name != null) {
                for (SortDirection type : SortDirection.values()) {
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

