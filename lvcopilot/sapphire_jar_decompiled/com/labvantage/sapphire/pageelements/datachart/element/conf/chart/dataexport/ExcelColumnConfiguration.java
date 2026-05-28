/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport;

import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ExcelColumnConfiguration
implements Serializable {
    private static final String DEFAULT_COLUMN_ID = "";
    private final StringExpression columnId;

    public ExcelColumnConfiguration(PropertyList columnProps) {
        if (columnProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.columnId = new StringExpression(columnProps.getProperty("columnid", DEFAULT_COLUMN_ID));
    }

    public StringExpression getColumnId() {
        return this.columnId;
    }
}

