/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import java.io.Serializable;
import sapphire.xml.PropertyList;

public class ColumnConfiguration
implements Serializable {
    private static final String DEFAULT_COLUMN_ID = "";
    private static final String DEFAULT_TRANSLATE = "N";
    private static final String DEFAULT_DISPLAY_VALUE_RULE = "";
    private static final String DEFAULT_TABLE_ID = "";
    private final String tableId;
    private final String columnId;
    private final boolean translate;
    private final String displayValueRule;

    public ColumnConfiguration(PropertyList columnProps) {
        if (columnProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.tableId = columnProps.getProperty("tableid", "");
        this.columnId = columnProps.getProperty("columnid", "");
        this.translate = columnProps.getProperty("translate", DEFAULT_TRANSLATE).toLowerCase().startsWith("y");
        this.displayValueRule = columnProps.getProperty("displayvalue", "");
    }

    public String getTableId() {
        return this.tableId;
    }

    public String getColumnId() {
        return this.columnId;
    }

    public boolean translate() {
        return this.translate;
    }

    public String getDisplayValueRule() {
        return this.displayValueRule;
    }
}

