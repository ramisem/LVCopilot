/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import java.io.Serializable;
import sapphire.xml.PropertyList;

public class IncludeConfiguration
implements Serializable {
    private final String url;

    public IncludeConfiguration(PropertyList includeProps) {
        this.url = includeProps.getProperty("url");
    }

    public String getUrl() {
        return this.url;
    }
}

