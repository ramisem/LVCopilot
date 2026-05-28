/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class InputConfiguration
implements Serializable {
    private static final String DEFAULT_PROPERTY_ID = "";
    private static final String DEFAULT_PROPERTY_VALUE = "";
    private final String propertyId;
    private final StringExpression propertyValue;

    public InputConfiguration(PropertyList inputProps) {
        if (inputProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        this.propertyId = inputProps.getProperty("propertyid", "");
        this.propertyValue = new StringExpression(inputProps.getProperty("propertyvalue", ""));
    }

    public String getPropertyId() {
        return this.propertyId;
    }

    public StringExpression getPropertyValue() {
        return this.propertyValue;
    }
}

