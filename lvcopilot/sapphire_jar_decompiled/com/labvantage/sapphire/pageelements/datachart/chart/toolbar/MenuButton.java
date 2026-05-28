/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.AbstractButton;
import com.labvantage.sapphire.pageelements.datachart.chart.toolbar.MenuItem;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.MenuItemConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.MenuButtonConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MenuButton
extends AbstractButton {
    private final List<MenuItem> menuItemList;
    private int menuWidth;

    public MenuButton(MenuButtonConfiguration menuButtonConf) {
        super(menuButtonConf.getParent());
        this.menuWidth = menuButtonConf.getMenuWidth();
        this.menuItemList = new ArrayList<MenuItem>();
        for (MenuItemConfiguration menuItemConf : menuButtonConf.getMenuButtonItemConfigurationList()) {
            this.menuItemList.add(new MenuItem(menuItemConf));
        }
    }

    @Override
    public PropertyList getProps(ChartBindingMap chartBindingMap, PropertyList argumentValueList) throws SapphireException {
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        if (argumentValueList == null) {
            throw new IllegalArgumentException("Argument value list is null");
        }
        PropertyList buttonProps = super.getProps(chartBindingMap, argumentValueList);
        PropertyList menuButtonProps = new PropertyList();
        buttonProps.setProperty("menubuttonprops", menuButtonProps);
        menuButtonProps.setProperty("menuwidth", Integer.toString(this.getMenuWidth()));
        PropertyListCollection menuButtonItemCollection = new PropertyListCollection();
        menuButtonProps.setProperty("menubuttonitemcollection", menuButtonItemCollection);
        for (MenuItem menuItem : this.menuItemList) {
            menuButtonItemCollection.add(menuItem.getProps(chartBindingMap));
        }
        return buttonProps;
    }

    public int getMenuWidth() {
        return this.menuWidth;
    }
}

