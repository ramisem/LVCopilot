/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class QueryArgumentConfiguration
implements Serializable {
    private final String argumentId;
    private final StringExpression argumentValue;
    private final String dateType;

    public QueryArgumentConfiguration(PropertyList inputProps) {
        if (inputProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        this.argumentId = inputProps.getProperty("queryparam");
        this.argumentValue = new StringExpression(inputProps.getProperty("value", ""));
        this.dateType = inputProps.getProperty("convertdate", "Y");
    }

    public String getArgumentId() {
        return this.argumentId;
    }

    public StringExpression getArgumentValue() {
        return this.argumentValue;
    }

    public boolean convertToSystemDate() {
        return this.dateType.equalsIgnoreCase("Y");
    }
}

