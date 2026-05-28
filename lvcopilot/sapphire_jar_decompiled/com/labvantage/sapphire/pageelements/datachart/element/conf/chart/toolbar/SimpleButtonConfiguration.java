/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.OperationConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class SimpleButtonConfiguration
implements Serializable {
    private final ButtonConfiguration parent;
    private final OperationConfiguration operationConf;

    public SimpleButtonConfiguration(PropertyList simpleButtonProps, ButtonConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (simpleButtonProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.operationConf = new OperationConfiguration(simpleButtonProps.getPropertyListNotNull("operationprops"));
        this.parent = parent;
    }

    public SimpleButtonConfiguration(SimpleButtonConfiguration copy, ButtonConfiguration parent) {
        this.parent = parent;
        this.operationConf = new OperationConfiguration(copy.operationConf);
    }

    public ButtonConfiguration getParent() {
        return this.parent;
    }

    public OperationConfiguration getOperationConfiguration() {
        return this.operationConf;
    }
}

