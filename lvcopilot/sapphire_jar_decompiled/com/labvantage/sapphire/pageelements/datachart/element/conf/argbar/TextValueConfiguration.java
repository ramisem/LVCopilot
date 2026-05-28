/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.argbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class TextValueConfiguration
implements Serializable {
    private final ArgumentValueConfiguration parent;
    private final StringExpression value;

    public TextValueConfiguration(PropertyList valueProps, ArgumentValueConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (valueProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.value = new StringExpression(valueProps.getProperty("value"));
        this.parent = parent;
    }

    public ArgumentValueConfiguration getParent() {
        return this.parent;
    }

    public StringExpression getValue() {
        return this.value;
    }
}

