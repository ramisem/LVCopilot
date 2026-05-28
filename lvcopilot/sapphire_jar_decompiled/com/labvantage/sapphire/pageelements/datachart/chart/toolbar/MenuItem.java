/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.Operation;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.MenuItemConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class MenuItem
implements Serializable {
    private final String itemId;
    private final String text;
    private final String tip;
    private final Operation operation;

    public MenuItem(MenuItemConfiguration menuItemConf) {
        if (menuItemConf == null) {
            throw new IllegalArgumentException("Menu item configuration is null");
        }
        this.itemId = menuItemConf.getItemId();
        this.text = menuItemConf.getText();
        this.tip = menuItemConf.getTip();
        this.operation = new Operation(menuItemConf.getOperationConfiguration());
    }

    public PropertyList getProps(ChartBindingMap chartBindingMap) throws SapphireException {
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        PropertyList menuItemProps = new PropertyList();
        menuItemProps.setProperty("itemid", this.getItemId());
        menuItemProps.setProperty("text", this.getText());
        menuItemProps.setProperty("tip", this.getTip());
        menuItemProps.setProperty("operationprops", this.operation.getProps(chartBindingMap));
        return menuItemProps;
    }

    public String getItemId() {
        return this.itemId;
    }

    public String getText() {
        return this.text;
    }

    public String getTip() {
        return this.tip;
    }
}

