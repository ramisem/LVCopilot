/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.StandardEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class OpenURLEventConfiguration
implements Serializable {
    private final StandardEventConfiguration parent;
    private final StringExpression url;
    private final String target;

    public OpenURLEventConfiguration(PropertyList openURLEventProps, StandardEventConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (openURLEventProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.url = new StringExpression(openURLEventProps.getProperty("url"));
        this.target = openURLEventProps.getProperty("target");
        this.parent = parent;
    }

    public OpenURLEventConfiguration(OpenURLEventConfiguration copy, StandardEventConfiguration parent) {
        this.parent = parent;
        this.url = copy.url;
        this.target = copy.target;
    }

    public StringExpression getURL() {
        return this.url;
    }

    public StandardEventConfiguration getParent() {
        return this.parent;
    }

    public String getTarget() {
        return this.target;
    }
}

