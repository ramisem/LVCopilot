/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data;

import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class OutputConfiguration
implements Serializable {
    private static final String DEFAULT_PROPERTY_ID = "";
    private static final String DEFAULT_VARIABLE = "";
    private final String propertyId;
    private final String variable;

    public OutputConfiguration(PropertyList inputProps) {
        if (inputProps == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        this.propertyId = inputProps.getProperty("propertyid", "");
        this.variable = inputProps.getProperty("variable", "");
    }

    public String getPropertyId() {
        return this.propertyId;
    }

    public String getVariable() {
        return this.variable;
    }
}

