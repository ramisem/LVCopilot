/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelExportConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ExcelTitleConfiguration
implements Serializable {
    private static final String DEFAULT_TEXT = "";
    private static final String DEFAULT_COLUMN = "0";
    private static final String DEFAULT_ROW = "0";
    private static final String DEFAULT_TITLE_TYPE = TitleType.TEXT.getName();
    private final ExcelExportConfiguration parent;
    private final String titleId;
    private final int column;
    private final int row;
    private final TitleType titleType;

    public ExcelTitleConfiguration(PropertyList titleProps, ExcelExportConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (titleProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.titleId = titleProps.getProperty("titleid", DEFAULT_TEXT);
        this.column = Integer.valueOf(titleProps.getProperty("column", "0"));
        this.row = Integer.valueOf(titleProps.getProperty("row", "0"));
        this.titleType = TitleType.fromString(titleProps.getProperty("titletype", DEFAULT_TITLE_TYPE));
        this.parent = parent;
    }

    public TitleType getTitleType() {
        return this.titleType;
    }

    public int getRow() {
        return this.row;
    }

    public int getColumn() {
        return this.column;
    }

    public String getTitleId() {
        return this.titleId;
    }

    public ExcelExportConfiguration getParent() {
        return this.parent;
    }

    public static enum TitleType {
        TEXT("Text", 0),
        DATE("Date", 2),
        NUMBER("Number", 1);

        private final String name;
        private final int columnType;

        private TitleType(String name, int columnType) {
            this.name = name;
            this.columnType = columnType;
        }

        public static TitleType fromString(String name) {
            if (name != null) {
                for (TitleType type : TitleType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }

        public int getColumnType() {
            return this.columnType;
        }
    }
}

