/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.MenuItemConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ButtonConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class MenuButtonConfiguration
implements Serializable {
    private static final String DEFAULT_MENU_WIDTH = "100";
    private final ButtonConfiguration parent;
    private final List<MenuItemConfiguration> menuButtonItemConfList;
    private final Integer menuWidth;

    public MenuButtonConfiguration(PropertyList menuButtonProps, ButtonConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (menuButtonProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.menuButtonItemConfList = new ArrayList<MenuItemConfiguration>();
        PropertyListCollection menuButtonItemCollection = menuButtonProps.getCollectionNotNull("menubuttonitemcollection");
        for (int i = 0; i < menuButtonItemCollection.size(); ++i) {
            PropertyList menuButtonItemProps = menuButtonItemCollection.getPropertyList(i);
            this.menuButtonItemConfList.add(new MenuItemConfiguration(menuButtonItemProps));
        }
        this.menuWidth = Integer.valueOf(menuButtonProps.getProperty("menuwidth", DEFAULT_MENU_WIDTH));
        this.parent = parent;
    }

    public MenuButtonConfiguration(MenuButtonConfiguration copy, ButtonConfiguration parent) {
        this.parent = parent;
        this.menuWidth = copy.menuWidth;
        this.menuButtonItemConfList = new ArrayList<MenuItemConfiguration>();
        for (MenuItemConfiguration menuButtonItemConf : copy.menuButtonItemConfList) {
            this.menuButtonItemConfList.add(new MenuItemConfiguration(menuButtonItemConf));
        }
    }

    public Integer getMenuWidth() {
        return this.menuWidth;
    }

    public ButtonConfiguration getParent() {
        return this.parent;
    }

    public List<MenuItemConfiguration> getMenuButtonItemConfigurationList() {
        return this.menuButtonItemConfList;
    }
}

