/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.OperationConfiguration;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class MenuItemConfiguration
implements Serializable {
    private static final String DEFAULT_TEXT = "Item";
    private final OperationConfiguration operationConf;
    private final String text;
    private final String tip;
    private final String itemId;

    public MenuItemConfiguration(PropertyList menuItemProps) {
        if (menuItemProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.itemId = menuItemProps.getProperty("itemid", "");
        if (this.itemId.isEmpty()) {
            throw new IllegalArgumentException("Menu button item ID is empty");
        }
        this.text = menuItemProps.getProperty("text", DEFAULT_TEXT);
        this.tip = menuItemProps.getProperty("tip", this.text);
        this.operationConf = new OperationConfiguration(menuItemProps.getPropertyListNotNull("operationprops"));
    }

    public MenuItemConfiguration(MenuItemConfiguration copy) {
        this.text = copy.text;
        this.tip = copy.tip;
        this.itemId = copy.itemId;
        this.operationConf = new OperationConfiguration(copy.operationConf);
    }

    public OperationConfiguration getOperationConfiguration() {
        return this.operationConf;
    }

    public String getTip() {
        return this.tip;
    }

    public String getText() {
        return this.text;
    }

    public String getItemId() {
        return this.itemId;
    }
}

