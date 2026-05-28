/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.ColumnConfigurationAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class PseudoColumnConfiguration
implements Serializable {
    private static final String DEFAULT_COLUMN_ID = "";
    private static final String DEFAULT_EXPRESSION = "";
    private static final String DEFAULT_COLUMN_TYPE = ColumnType.STRING.getName();
    private final ColumnConfigurationAdapterConfiguration parent;
    private final String columnId;
    private final StringExpression expression;
    private final ColumnType columnType;

    public PseudoColumnConfiguration(PropertyList pseudoColumnProps, ColumnConfigurationAdapterConfiguration parent) {
        if (pseudoColumnProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.columnId = pseudoColumnProps.getProperty("columnid", "");
        if (this.columnId.isEmpty()) {
            throw new IllegalArgumentException("Empty pseudo column ID");
        }
        this.expression = new StringExpression(pseudoColumnProps.getProperty("expression", ""));
        this.columnType = ColumnType.fromString(pseudoColumnProps.getProperty("columntype", DEFAULT_COLUMN_TYPE));
    }

    public ColumnType getColumnType() {
        return this.columnType;
    }

    public StringExpression getExpression() {
        return this.expression;
    }

    public String getColumnId() {
        return this.columnId;
    }

    public ColumnConfigurationAdapterConfiguration getParent() {
        return this.parent;
    }

    public static enum ColumnType {
        STRING("Text", 0),
        DATE("Date", 2),
        NUMBER("Number", 1);

        private final String name;
        private final int dataType;

        private ColumnType(String name, int dataType) {
            this.name = name;
            this.dataType = dataType;
        }

        public static ColumnType fromString(String name) {
            if (name != null) {
                for (ColumnType type : ColumnType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }

        public int getDataType() {
            return this.dataType;
        }
    }
}

