/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class SQLValueConfiguration
implements Serializable {
    private static final String DEFAULT_VALUE_COLUMN_ID = "";
    private final ArgumentValueConfiguration parent;
    private final StringExpression sql;
    private final String valueColumnId;

    public SQLValueConfiguration(PropertyList sqlValueProps, ArgumentValueConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (sqlValueProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.sql = new StringExpression(sqlValueProps.getProperty("sql"));
        this.valueColumnId = sqlValueProps.getProperty("valuecolumnid", DEFAULT_VALUE_COLUMN_ID);
        this.parent = parent;
    }

    public ArgumentValueConfiguration getParent() {
        return this.parent;
    }

    public StringExpression getSql() {
        return this.sql;
    }

    public String getValueColumnId() {
        return this.valueColumnId;
    }
}

