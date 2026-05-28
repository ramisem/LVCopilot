/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.StandardDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class SqlDataSetConfiguration
implements Serializable {
    private static final String DEFAULT_SQL = "";
    private final StringExpression sql;
    private final StandardDataSetProviderConfiguration parent;

    public SqlDataSetConfiguration(PropertyList sqlDataSetProps, StandardDataSetProviderConfiguration parent) {
        if (sqlDataSetProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.sql = new StringExpression(sqlDataSetProps.getProperty("sql", DEFAULT_SQL));
        this.parent = parent;
    }

    public StringExpression getSql() {
        return this.sql;
    }

    public StandardDataSetProviderConfiguration getParent() {
        return this.parent;
    }
}

