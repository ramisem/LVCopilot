/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.SetArgumentValueConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ArgumentDetailsConfiguration
implements Serializable {
    private final SetArgumentValueConfiguration parent;
    private final String argumentId;
    private final StringExpression argumentValue;

    public ArgumentDetailsConfiguration(PropertyList argumentDetailsProps, SetArgumentValueConfiguration parent) {
        if (argumentDetailsProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        this.parent = parent;
        this.argumentId = argumentDetailsProps.getProperty("argumentid");
        this.argumentValue = new StringExpression(argumentDetailsProps.getProperty("value"));
    }

    public ArgumentDetailsConfiguration(ArgumentDetailsConfiguration copy, SetArgumentValueConfiguration parent) {
        this.parent = parent;
        this.argumentId = copy.argumentId;
        this.argumentValue = new StringExpression(copy.argumentValue);
    }

    public SetArgumentValueConfiguration getParent() {
        return this.parent;
    }

    public String getArgumentId() {
        return this.argumentId;
    }

    public StringExpression getArgumentValue() {
        return this.argumentValue;
    }
}

