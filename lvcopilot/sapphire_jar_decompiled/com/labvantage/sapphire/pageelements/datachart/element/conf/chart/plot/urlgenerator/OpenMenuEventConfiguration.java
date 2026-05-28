/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.MenuItemConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.StandardEventConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class OpenMenuEventConfiguration
implements Serializable {
    private static final String DEFAULT_MENU_WIDTH = "100";
    private final StandardEventConfiguration parent;
    private final List<MenuItemConfiguration> menuItemConfList;
    private final int menuWidth;

    public OpenMenuEventConfiguration(PropertyList openMenuEventProps, StandardEventConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (openMenuEventProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.menuItemConfList = new ArrayList<MenuItemConfiguration>();
        PropertyListCollection menuButtonItemCollection = openMenuEventProps.getCollectionNotNull("menuitemcollection");
        for (int i = 0; i < menuButtonItemCollection.size(); ++i) {
            PropertyList menuButtonItemProps = menuButtonItemCollection.getPropertyList(i);
            this.menuItemConfList.add(new MenuItemConfiguration(menuButtonItemProps));
        }
        this.menuWidth = Integer.parseInt(openMenuEventProps.getProperty("menuwidth", DEFAULT_MENU_WIDTH));
    }

    public OpenMenuEventConfiguration(OpenMenuEventConfiguration copy, StandardEventConfiguration parent) {
        this.parent = parent;
        this.menuWidth = copy.menuWidth;
        this.menuItemConfList = new ArrayList<MenuItemConfiguration>();
        for (MenuItemConfiguration menuButtonItemConf : copy.menuItemConfList) {
            this.menuItemConfList.add(new MenuItemConfiguration(menuButtonItemConf));
        }
    }

    public StandardEventConfiguration getParent() {
        return this.parent;
    }

    public List<MenuItemConfiguration> getMenuItemConfigurationList() {
        return this.menuItemConfList;
    }

    public int getMenuWidth() {
        return this.menuWidth;
    }
}

