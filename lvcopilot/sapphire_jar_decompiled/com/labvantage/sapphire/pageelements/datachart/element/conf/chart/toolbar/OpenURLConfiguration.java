/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.StandardOperationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class OpenURLConfiguration
implements Serializable {
    private final StandardOperationConfiguration parent;
    private final StringExpression url;

    public OpenURLConfiguration(PropertyList openURLProps, StandardOperationConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (openURLProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.url = new StringExpression(openURLProps.getProperty("url"));
        this.parent = parent;
    }

    public OpenURLConfiguration(OpenURLConfiguration copy, StandardOperationConfiguration parent) {
        this.parent = parent;
        this.url = copy.url;
    }

    public StandardOperationConfiguration getParent() {
        return this.parent;
    }

    public StringExpression getURL() {
        return this.url;
    }
}

